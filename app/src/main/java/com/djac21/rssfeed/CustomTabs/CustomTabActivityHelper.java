package com.djac21.rssfeed.CustomTabs;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;

import java.util.List;

public class CustomTabActivityHelper implements ServiceConnectionCallback {
    private CustomTabsSession mCustomTabsSession;
    private CustomTabsClient mCustomTabsClient;
    private CustomTabsServiceConnection mConnection;
    private CustomTabConnectionCallback mConnectionCallback;

    public interface CustomTabFallback {
        void openUri(Context activity, Uri uri);
    }

    public interface CustomTabConnectionCallback {
        void onCustomTabConnected();

        void onCustomTabDisconnected();
    }

    public static void openCustomTab(Context activity, CustomTabsIntent customTabsIntent,
                                     Uri uri, CustomTabFallback fallback) {
        String packageName = CustomTabsHelper.getPackageNameToUse(activity);

        if (packageName == null) {
            if (fallback != null) {
                fallback.openUri(activity, uri);
            }
        } else {
            customTabsIntent.intent.setPackage(packageName);
            customTabsIntent.launchUrl(activity, uri);
        }
    }

    public void bindCustomTabsService(Activity activity) {
        if (mCustomTabsClient != null) return;

        String packageName = CustomTabsHelper.getPackageNameToUse(activity);
        if (packageName == null) return;

        mConnection = new ServiceConnection(this);
        CustomTabsClient.bindCustomTabsService(activity, packageName, mConnection);
    }

    public void unbindCustomTabsService(Activity activity) {
        if (mConnection == null) return;
        activity.unbindService(mConnection);
        mCustomTabsClient = null;
        mCustomTabsSession = null;
        mConnection = null;
    }

    public CustomTabsSession getSession() {
        if (mCustomTabsClient == null) {
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            mCustomTabsSession = mCustomTabsClient.newSession(null);
        }
        return mCustomTabsSession;
    }

    public void setConnectionCallback(CustomTabConnectionCallback connectionCallback) {
        this.mConnectionCallback = connectionCallback;
    }

    public boolean mayLaunchUrl(Uri uri, Bundle extras, List<Bundle> otherLikelyBundles) {
        if (mCustomTabsClient == null) return false;

        CustomTabsSession session = getSession();
        if (session == null) return false;

        return session.mayLaunchUrl(uri, extras, otherLikelyBundles);
    }

    @Override
    public void onServiceConnected(CustomTabsClient client) {
        mCustomTabsClient = client;
        mCustomTabsClient.warmup(0L);
        if (mConnectionCallback != null) {
            mConnectionCallback.onCustomTabConnected();
        }
    }

    @Override
    public void onServiceDisconnected() {
        mCustomTabsClient = null;
        mConnection = null;
        if (mConnectionCallback != null) {
            mConnectionCallback.onCustomTabDisconnected();
        }
    }
}
