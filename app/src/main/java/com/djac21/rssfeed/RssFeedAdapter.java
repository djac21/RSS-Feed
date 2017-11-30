package com.djac21.rssfeed;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.djac21.rssfeed.CustomTabs.CustomTabActivityHelper;
import com.djac21.rssfeed.CustomTabs.WebViewActivity;

import java.util.List;

public class RssFeedAdapter extends RecyclerView.Adapter<RssFeedAdapter.FeedModelViewHolder> {
    private List<RssFeedModel> rssFeedModels;
    private Context context;

    public RssFeedAdapter(List<RssFeedModel> rssFeedModels, Context context) {
        this.rssFeedModels = rssFeedModels;
        this.context = context;
    }

    @Override
    public FeedModelViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rss_card_view, parent, false);
        FeedModelViewHolder holder = new FeedModelViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(FeedModelViewHolder holder, int position) {
        holder.title.setText(rssFeedModels.get(position).getRssTitle());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            holder.description.setText(Html.fromHtml(Html.fromHtml(rssFeedModels.get(position).getRssDescription(),1).toString(),1));
        else
            holder.description.setText(Html.fromHtml(Html.fromHtml(rssFeedModels.get(position).getRssDescription()).toString()));
    }

    @Override
    public int getItemCount() {
        return rssFeedModels.size();
    }

    public class FeedModelViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView title, description;

        public FeedModelViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            title = itemView.findViewById(R.id.titleText);
            description = itemView.findViewById(R.id.descriptionText);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            String url = rssFeedModels.get(position).getRssLink();

            if (validateUrl(url)) {
                Uri uri = Uri.parse(url);
                if (uri != null) {
                    openCustomChromeTab(uri);
                }
            } else {
                Toast.makeText(context, "Error with link: " + url, Toast.LENGTH_SHORT).show();
            }
        }

        private boolean validateUrl(String url) {
            return url != null && url.length() > 0 && (url.startsWith("http://") || url.startsWith("https://"));
        }

        private void openCustomChromeTab(Uri uri) {
            CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = intentBuilder.build();

            intentBuilder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

            CustomTabActivityHelper.openCustomTab(context, customTabsIntent, uri, new CustomTabActivityHelper.CustomTabFallback() {
                @Override
                public void openUri(Context activity, Uri uri) {
                    openWebView(uri);
                }
            });
        }

        private void openWebView(Uri uri) {
            Intent webViewIntent = new Intent(context, WebViewActivity.class);
            webViewIntent.putExtra(WebViewActivity.EXTRA_URL, uri.toString());
            context.startActivity(webViewIntent);
        }
    }
}