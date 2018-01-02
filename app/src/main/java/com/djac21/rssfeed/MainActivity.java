package com.djac21.rssfeed;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private EditText rssFeeEditText;
    private FloatingActionButton fetchFeedButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView feedTitleTextView, feedLinkTextView, feedDescriptionTextView;
    private List<RssFeedModel> mFeedModelList;
    private String feedTitle, feedLink, feedDescription;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        rssFeeEditText = findViewById(R.id.rssFeedEditText);
        fetchFeedButton = findViewById(R.id.fetchFeedButton);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        feedTitleTextView = findViewById(R.id.feedTitle);
        feedDescriptionTextView = findViewById(R.id.feedDescription);
        feedLinkTextView = findViewById(R.id.feedLink);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetData().execute((Void) null);
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(fetchFeedButton.getWindowToken(), 0);
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new GetData().execute((Void) null);
            }
        });
    }

    public List<RssFeedModel> parseFeed(InputStream inputStream) throws XmlPullParserException, IOException {
        String title = null;
        String link = null;
        String description = null;
        boolean isItem = false;
        List<RssFeedModel> items = new ArrayList<>();

        try {
            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlPullParser.setInput(inputStream, null);

            xmlPullParser.nextTag();
            while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                int eventType = xmlPullParser.getEventType();

                String name = xmlPullParser.getName();
                if (name == null)
                    continue;

                if (eventType == XmlPullParser.END_TAG) {
                    if (name.equalsIgnoreCase("item")) {
                        isItem = false;
                    }
                    continue;
                }

                if (eventType == XmlPullParser.START_TAG) {
                    if (name.equalsIgnoreCase("item")) {
                        isItem = true;
                        continue;
                    }
                }

                String result = "";
                if (xmlPullParser.next() == XmlPullParser.TEXT) {
                    result = xmlPullParser.getText();
                    xmlPullParser.nextTag();
                }

                if (name.equalsIgnoreCase("title"))
                    title = result;
                else if (name.equalsIgnoreCase("link"))
                    link = result;
                else if (name.equalsIgnoreCase("description"))
                    description = result;

                if (title != null && link != null && description != null) {
                    if (isItem) {
                        RssFeedModel item = new RssFeedModel(title, link, description);
                        items.add(item);
                    } else {
                        feedTitle = title;
                        feedLink = link;
                        feedDescription = description;
                    }
                    title = null;
                    link = null;
                    description = null;
                    isItem = false;
                }
            }
            return items;
        } finally {
            inputStream.close();
        }
    }

    private class GetData extends AsyncTask<Void, Void, Boolean> {
        private String urlLink;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            feedTitle = null;
            feedLink = null;
            feedDescription = null;
            feedTitleTextView.setText("Feed Title: " + feedTitle);
            feedDescriptionTextView.setText("Feed Description: " + feedDescription);
            feedLinkTextView.setText("Feed Link: " + feedLink);
            urlLink = rssFeeEditText.getText().toString();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (TextUtils.isEmpty(urlLink))
                return false;

            try {
                if (!urlLink.startsWith("http://") && !urlLink.startsWith("https://"))
                    urlLink = "http://" + urlLink;

                URL url = new URL(urlLink);
                InputStream inputStream = url.openConnection().getInputStream();
                mFeedModelList = parseFeed(inputStream);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error", e);
            } catch (XmlPullParserException e) {
                Log.e(TAG, "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
            if (success) {
                feedTitleTextView.setText("Feed Title: " + feedTitle);
                feedDescriptionTextView.setText("Feed Description: " + feedDescription);
                feedLinkTextView.setText("Feed Link: " + feedLink);
                recyclerView.setAdapter(new RssFeedAdapter(mFeedModelList, MainActivity.this));
            } else {
                Toast.makeText(MainActivity.this, "Enter a valid Rss feed url", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("About")
                    .setMessage("Enter a RSS feed URL and upon clicking the respected card the internal browser will be launched")
                    .setPositiveButton("OK", null);
            builder.create().show();
        } else if (id == R.id.action_refresh) {
            new GetData().execute();
        }
        return super.onOptionsItemSelected(item);
    }
}