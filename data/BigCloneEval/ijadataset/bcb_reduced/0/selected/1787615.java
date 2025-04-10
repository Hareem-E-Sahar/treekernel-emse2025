package com.example.android.rssreader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TwoLineListItem;
import android.util.Xml;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * The RssReader example demonstrates forking off a thread to download
 * rss data in the background and post the results to a ListView in the UI.
 * It also shows how to display custom data in a ListView
 * with a ArrayAdapter subclass.
 * 
 * <ul>
 * <li>We own a ListView
 * <li>The ListView uses our custom RSSListAdapter which 
 * <ul>
 * <li>The adapter feeds data to the ListView
 * <li>Override of getView() in the adapter provides the display view
 * used for selected list items
 * </ul>
 * <li>Override of onListItemClick() creates an intent to open the url for that
 * RssItem in the browser.
 * <li>Download = fork off a worker thread
 * <li>The worker thread opens a network connection for the rss data
 * <li>Uses XmlPullParser to extract the rss item data
 * <li>Uses mHandler.post() to send new RssItems to the UI
 * <li>Supports onSaveInstanceState()/onRestoreInstanceState() to save list/selection state on app
 * pause, so can resume seamlessly
 * </ul>
 */
public class RssReader extends ListActivity {

    /**
     * Custom list adapter that fits our rss data into the list.
     */
    private RSSListAdapter mAdapter;

    /**
     * Url edit text field.
     */
    private EditText mUrlText;

    /**
     * Status text field.
     */
    private TextView mStatusText;

    /**
     * Handler used to post runnables to the UI thread.
     */
    private Handler mHandler;

    /**
     * Currently running background network thread.
     */
    private RSSWorker mWorker;

    public static final int SNIPPET_LENGTH = 90;

    public static final String STRINGS_KEY = "strings";

    public static final String SELECTION_KEY = "selection";

    public static final String URL_KEY = "url";

    public static final String STATUS_KEY = "status";

    /**
     * Called when the activity starts up. Do activity initialization
     * here, not in a constructor.
     * 
     * @see Activity#onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rss_layout);
        List<RssItem> items = new ArrayList<RssItem>();
        mAdapter = new RSSListAdapter(this, items);
        getListView().setAdapter(mAdapter);
        mUrlText = (EditText) findViewById(R.id.urltext);
        mStatusText = (TextView) findViewById(R.id.statustext);
        Button download = (Button) findViewById(R.id.download);
        download.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                doRSS(mUrlText.getText());
            }
        });
        mHandler = new Handler();
    }

    /**
     * ArrayAdapter encapsulates a java.util.List of T, for presentation in a
     * ListView. This subclass specializes it to hold RssItems and display
     * their title/description data in a TwoLineListItem.
     */
    private class RSSListAdapter extends ArrayAdapter<RssItem> {

        private LayoutInflater mInflater;

        public RSSListAdapter(Context context, List<RssItem> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /**
         * This is called to render a particular item for the on screen list.
         * Uses an off-the-shelf TwoLineListItem view, which contains text1 and
         * text2 TextViews. We pull data from the RssItem and set it into the
         * view. The convertView is the view from a previous getView(), so
         * we can re-use it.
         * 
         * @see ArrayAdapter#getView
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TwoLineListItem view;
            if (convertView == null) {
                view = (TwoLineListItem) mInflater.inflate(android.R.layout.simple_list_item_2, null);
            } else {
                view = (TwoLineListItem) convertView;
            }
            RssItem item = this.getItem(position);
            view.getText1().setText(item.getTitle());
            String descr = item.getDescription().toString();
            descr = removeTags(descr);
            view.getText2().setText(descr.substring(0, Math.min(descr.length(), SNIPPET_LENGTH)));
            return view;
        }
    }

    /**
     * Simple code to strip out <tag>s -- primitive way to sortof display HTML as
     * plain text.
     */
    public String removeTags(String str) {
        str = str.replaceAll("<.*?>", " ");
        str = str.replaceAll("\\s+", " ");
        return str;
    }

    /**
     * Called when user clicks an item in the list. Starts an activity to
     * open the url for that item.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        RssItem item = mAdapter.getItem(position);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink().toString()));
        startActivity(intent);
    }

    /**
     * Resets the output UI -- list and status text empty.
     */
    public void resetUI() {
        List<RssItem> items = new ArrayList<RssItem>();
        mAdapter = new RSSListAdapter(this, items);
        getListView().setAdapter(mAdapter);
        mStatusText.setText("");
        mUrlText.requestFocus();
    }

    /**
     * Sets the currently active running worker. Interrupts any earlier worker,
     * so we only have one at a time.
     * 
     * @param worker the new worker
     */
    public synchronized void setCurrentWorker(RSSWorker worker) {
        if (mWorker != null) mWorker.interrupt();
        mWorker = worker;
    }

    /**
     * Is the given worker the currently active one.
     * 
     * @param worker
     * @return
     */
    public synchronized boolean isCurrentWorker(RSSWorker worker) {
        return (mWorker == worker);
    }

    /**
     * Given an rss url string, starts the rss-download-thread going.
     * 
     * @param rssUrl
     */
    private void doRSS(CharSequence rssUrl) {
        RSSWorker worker = new RSSWorker(rssUrl);
        setCurrentWorker(worker);
        resetUI();
        mStatusText.setText("Downloading…");
        worker.start();
    }

    /**
     * Runnable that the worker thread uses to post RssItems to the
     * UI via mHandler.post
     */
    private class ItemAdder implements Runnable {

        RssItem mItem;

        ItemAdder(RssItem item) {
            mItem = item;
        }

        public void run() {
            mAdapter.add(mItem);
        }
    }

    /**
     * Worker thread takes in an rss url string, downloads its data, parses
     * out the rss items, and communicates them back to the UI as they are read.
     */
    private class RSSWorker extends Thread {

        private CharSequence mUrl;

        public RSSWorker(CharSequence url) {
            mUrl = url;
        }

        @Override
        public void run() {
            String status = "";
            try {
                URL url = new URL(mUrl.toString());
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(10000);
                connection.connect();
                InputStream in = connection.getInputStream();
                parseRSS(in, mAdapter);
                status = "done";
            } catch (Exception e) {
                status = "failed:" + e.getMessage();
            }
            final String temp = status;
            if (isCurrentWorker(this)) {
                mHandler.post(new Runnable() {

                    public void run() {
                        mStatusText.setText(temp);
                    }
                });
            }
        }
    }

    /**
     * Populates the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, "Slashdot").setOnMenuItemClickListener(new RSSMenu("http://rss.slashdot.org/Slashdot/slashdot"));
        menu.add(0, 0, 0, "Google News").setOnMenuItemClickListener(new RSSMenu("http://news.google.com/?output=rss"));
        menu.add(0, 0, 0, "News.com").setOnMenuItemClickListener(new RSSMenu("http://news.com.com/2547-1_3-0-20.xml"));
        menu.add(0, 0, 0, "Bad Url").setOnMenuItemClickListener(new RSSMenu("http://nifty.stanford.edu:8080"));
        menu.add(0, 0, 0, "Reset").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            public boolean onMenuItemClick(MenuItem item) {
                resetUI();
                return true;
            }
        });
        return true;
    }

    /**
     * Puts text in the url text field and gives it focus. Used to make a Runnable
     * for each menu item. This way, one inner class works for all items vs. an
     * anonymous inner class for each menu item.
     */
    private class RSSMenu implements MenuItem.OnMenuItemClickListener {

        private CharSequence mUrl;

        RSSMenu(CharSequence url) {
            mUrl = url;
        }

        public boolean onMenuItemClick(MenuItem item) {
            mUrlText.setText(mUrl);
            mUrlText.requestFocus();
            return true;
        }
    }

    /**
     * Called for us to save out our current state before we are paused,
     * such a for example if the user switches to another app and memory
     * gets scarce. The given outState is a Bundle to which we can save
     * objects, such as Strings, Integers or lists of Strings. In this case, we
     * save out the list of currently downloaded rss data, (so we don't have to
     * re-do all the networking just because the user goes back and forth
     * between aps) which item is currently selected, and the data for the text views.
     * In onRestoreInstanceState() we look at the map to reconstruct the run-state of the
     * application, so returning to the activity looks seamlessly correct.
     * TODO: the Activity javadoc should give more detail about what sort of
     * data can go in the outState map.
     * 
     * @see android.app.Activity#onSaveInstanceState
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int count = mAdapter.getCount();
        ArrayList<CharSequence> strings = new ArrayList<CharSequence>();
        for (int i = 0; i < count; i++) {
            RssItem item = mAdapter.getItem(i);
            strings.add(item.getTitle());
            strings.add(item.getLink());
            strings.add(item.getDescription());
        }
        outState.putSerializable(STRINGS_KEY, strings);
        if (getListView().hasFocus()) {
            outState.putInt(SELECTION_KEY, Integer.valueOf(getListView().getSelectedItemPosition()));
        }
        outState.putString(URL_KEY, mUrlText.getText().toString());
        outState.putCharSequence(STATUS_KEY, mStatusText.getText());
    }

    /**
     * Called to "thaw" re-animate the app from a previous onSaveInstanceState().
     * 
     * @see android.app.Activity#onRestoreInstanceState
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (state == null) return;
        List<CharSequence> strings = (ArrayList<CharSequence>) state.getSerializable(STRINGS_KEY);
        List<RssItem> items = new ArrayList<RssItem>();
        for (int i = 0; i < strings.size(); i += 3) {
            items.add(new RssItem(strings.get(i), strings.get(i + 1), strings.get(i + 2)));
        }
        mAdapter = new RSSListAdapter(this, items);
        getListView().setAdapter(mAdapter);
        if (state.containsKey(SELECTION_KEY)) {
            getListView().requestFocus(View.FOCUS_FORWARD);
            getListView().setSelection(state.getInt(SELECTION_KEY));
        }
        mUrlText.setText(state.getCharSequence(URL_KEY));
        mStatusText.setText(state.getCharSequence(STATUS_KEY));
    }

    /**
     * Does rudimentary RSS parsing on the given stream and posts rss items to
     * the UI as they are found. Uses Android's XmlPullParser facility. This is
     * not a production quality RSS parser -- it just does a basic job of it.
     * 
     * @param in stream to read
     * @param adapter adapter for ui events
     */
    void parseRSS(InputStream in, RSSListAdapter adapter) throws IOException, XmlPullParserException {
        XmlPullParser xpp = Xml.newPullParser();
        xpp.setInput(in, null);
        int eventType;
        String title = "";
        String link = "";
        String description = "";
        eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tag = xpp.getName();
                if (tag.equals("item")) {
                    title = link = description = "";
                } else if (tag.equals("title")) {
                    xpp.next();
                    title = xpp.getText();
                } else if (tag.equals("link")) {
                    xpp.next();
                    link = xpp.getText();
                } else if (tag.equals("description")) {
                    xpp.next();
                    description = xpp.getText();
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                String tag = xpp.getName();
                if (tag.equals("item")) {
                    RssItem item = new RssItem(title, link, description);
                    mHandler.post(new ItemAdder(item));
                }
            }
            eventType = xpp.next();
        }
    }
}
