package com.dmurph.tracking;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketAddress;
import java.net.URL;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.MatchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common tracking calls are implemented as methods, but if you want to control
 * what data to send, then use {@link #makeCustomRequest(AnalyticsRequestData)}.
 * If you are making custom calls, the only requirements are:
 * <ul>
 * <li>If you are tracking an event,
 * {@link AnalyticsRequestData#setEventCategory(String)} and
 * {@link AnalyticsRequestData#setEventAction(String)} must both be populated.</li>
 * <li>If you are not tracking an event,
 * {@link AnalyticsRequestData#setPageURL(String)} must be populated</li>
 * </ul>
 * See the <a href=http://code.google.com/intl/en-US/apis/analytics/docs/tracking/gaTrackingTroubleshooting.html#gifParameters>
 * Google Troubleshooting Guide</a> for more info on the tracking parameters (although it doesn't seem to be fully updated).
 * <p>
 * The tracker can operate in three modes:
 * <ul>
 * <li>synchronous mode: The HTTP request is sent to GA immediately, before the track
 * method returns.
 * This may slow your application down if GA doesn't respond fast.
 * <li>multi-thread mode: Each track method call creates a new short-lived thread that sends
 * the HTTP request to GA in the background and terminates.
 * <li>single-thread mode (the default): The track method stores the request in a FIFO and returns
 * immediately. A single long-lived background thread consumes the FIFO content and sends the HTTP
 * requests to GA.
 * </ul>  
 * </p>
 * <p>
 * To halt the background thread safely, use the call {@link #stopBackgroundThread(long)}, where the parameter is the
 * timeout to wait for any remaining queued tracking calls to be made.  Keep in mind that if new tracking requests are made
 * after the thread is stopped, they will just be stored in the queue, and will not be sent to GA until the thread is started again with
 * {@link #startBackgroundThread()} (This is assuming you are in single-threaded mode to begin with).
 * </p>
 * @author Daniel Murphy, Stefan Brozinski
 */
public class JGoogleAnalyticsTracker {

    public static enum DispatchMode {

        /**
		 * Each tracking call will wait until the http request
		 * completes before returning
		 */
        SYNCHRONOUS, /**
		 * Each tracking call spawns a new thread to make the http request
		 */
        MULTI_THREAD, /**
		 * Each tracking request is added to a queue, and a single dispatch thread makes the requests.
		 */
        SINGLE_THREAD
    }

    private static Logger logger = LoggerFactory.getLogger(JGoogleAnalyticsTracker.class);

    private static final ThreadGroup asyncThreadGroup = new ThreadGroup("Async Google Analytics Threads");

    private static long asyncThreadsRunning = 0;

    private static Proxy proxy = Proxy.NO_PROXY;

    private static LinkedList<String> fifo = new LinkedList<String>();

    private static Thread backgroundThread = null;

    private static boolean backgroundThreadMayRun = false;

    static {
        asyncThreadGroup.setMaxPriority(Thread.MIN_PRIORITY);
        asyncThreadGroup.setDaemon(true);
    }

    public static enum GoogleAnalyticsVersion {

        V_4_7_2
    }

    private GoogleAnalyticsVersion gaVersion;

    private AnalyticsConfigData configData;

    private IGoogleAnalyticsURLBuilder builder;

    private DispatchMode mode;

    private boolean enabled;

    public JGoogleAnalyticsTracker(AnalyticsConfigData argConfigData, GoogleAnalyticsVersion argVersion) {
        this(argConfigData, argVersion, DispatchMode.SINGLE_THREAD);
    }

    public JGoogleAnalyticsTracker(AnalyticsConfigData argConfigData, GoogleAnalyticsVersion argVersion, DispatchMode argMode) {
        gaVersion = argVersion;
        configData = argConfigData;
        createBuilder();
        enabled = true;
        setDispatchMode(argMode);
    }

    /**
     * Sets the dispatch mode
     * @see DispatchMode
     * @param argMode the mode to to put the tracker in.  If this is null, the tracker
     * defaults to {@link DispatchMode#SINGLE_THREAD}
     */
    public void setDispatchMode(DispatchMode argMode) {
        if (argMode == null) {
            argMode = DispatchMode.SINGLE_THREAD;
        }
        if (argMode == DispatchMode.SINGLE_THREAD) {
            startBackgroundThread();
        }
        mode = argMode;
    }

    /**
     * Gets the current dispatch mode.  Default is {@link DispatchMode#SINGLE_THREAD}.
     * @see DispatchMode
     * @return
     */
    public DispatchMode getDispatchMode() {
        return mode;
    }

    /**
     * Convenience method to check if the tracker is in synchronous mode.
     * @return
     */
    public boolean isSynchronous() {
        return mode == DispatchMode.SYNCHRONOUS;
    }

    /**
     * Convenience method to check if the tracker is in single-thread mode
     * @return
     */
    public boolean isSingleThreaded() {
        return mode == DispatchMode.SINGLE_THREAD;
    }

    /**
     * Convenience method to check if the tracker is in multi-thread mode
     * @return
     */
    public boolean isMultiThreaded() {
        return mode == DispatchMode.MULTI_THREAD;
    }

    /**
     * Resets the session cookie.
     */
    public void resetSession() {
        builder.resetSession();
    }

    /**
     * Sets if the api dispatches tracking requests.
     * 
     * @param argEnabled
     */
    public void setEnabled(boolean argEnabled) {
        enabled = argEnabled;
    }

    /**
     * If the api is dispatching tracking requests (default of true).
     * 
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Define the proxy to use for all GA tracking requests.
     * <p>
     * Call this static method early (before creating any tracking requests).
     * 
     * @param argProxy  The proxy to use
     */
    public static void setProxy(Proxy argProxy) {
        proxy = (argProxy != null) ? argProxy : Proxy.NO_PROXY;
    }

    /**
     * Define the proxy to use for all GA tracking requests.
     * <p>
     * Call this static method early (before creating any tracking requests).
     * 
     * @param proxyAddr  "addr:port" of the proxy to use; may also be given as URL ("http://addr:port/").
     */
    public static void setProxy(String proxyAddr) {
        if (proxyAddr != null) {
            Scanner s = new Scanner(proxyAddr);
            proxyAddr = null;
            int proxyPort = 8080;
            try {
                s.findInLine("(http://|)([^:/]+)(:|)([0-9]*)(/|)");
                MatchResult m = s.match();
                if (m.groupCount() >= 2) {
                    proxyAddr = m.group(2);
                }
                if ((m.groupCount() >= 4) && (!m.group(4).isEmpty())) {
                    proxyPort = Integer.parseInt(m.group(4));
                }
            } finally {
                s.close();
            }
            if (proxyAddr != null) {
                SocketAddress sa = new InetSocketAddress(proxyAddr, proxyPort);
                setProxy(new Proxy(Type.HTTP, sa));
            }
        }
    }

    /**
     * Wait for background tasks to complete.
     * <p>
     * This works in queued and asynchronous mode.
     * 
     * @param timeoutMillis  The maximum number of milliseconds to wait.
     */
    public static void completeBackgroundTasks(long timeoutMillis) {
        boolean fifoEmpty = false;
        boolean asyncThreadsCompleted = false;
        long absTimeout = System.currentTimeMillis() + timeoutMillis;
        while (System.currentTimeMillis() < absTimeout) {
            synchronized (fifo) {
                fifoEmpty = (fifo.size() == 0);
            }
            synchronized (JGoogleAnalyticsTracker.class) {
                asyncThreadsCompleted = (asyncThreadsRunning == 0);
            }
            if (fifoEmpty && asyncThreadsCompleted) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Tracks a page view.
     * 
     * @param argPageURL
     *            required, Google won't track without it. Ex:
     *            <code>"org/me/javaclass.java"</code>, or anything you want as
     *            the page url.
     * @param argPageTitle
     *            content title
     * @param argHostName
     *            the host name for the url
     */
    public void trackPageView(String argPageURL, String argPageTitle, String argHostName) {
        trackPageViewFromReferrer(argPageURL, argPageTitle, argHostName, "http://www.dmurph.com", "/");
    }

    /**
     * Tracks a page view.
     * 
     * @param argPageURL
     *            required, Google won't track without it. Ex:
     *            <code>"org/me/javaclass.java"</code>, or anything you want as
     *            the page url.
     * @param argPageTitle
     *            content title
     * @param argHostName
     *            the host name for the url
     * @param argReferrerSite
     *            site of the referrer. ex, www.dmurph.com
     * @param argReferrerPage
     *            page of the referrer. ex, /mypage.php
     */
    public void trackPageViewFromReferrer(String argPageURL, String argPageTitle, String argHostName, String argReferrerSite, String argReferrerPage) {
        if (argPageURL == null) {
            throw new IllegalArgumentException("Page URL cannot be null, Google will not track the data.");
        }
        AnalyticsRequestData data = new AnalyticsRequestData();
        data.setHostName(argHostName);
        data.setPageTitle(argPageTitle);
        data.setPageURL(argPageURL);
        data.setReferrer(argReferrerSite, argReferrerPage);
        makeCustomRequest(data);
    }

    /**
     * Tracks a page view.
     * 
     * @param argPageURL
     *            required, Google won't track without it. Ex:
     *            <code>"org/me/javaclass.java"</code>, or anything you want as
     *            the page url.
     * @param argPageTitle
     *            content title
     * @param argHostName
     *            the host name for the url
     * @param argSearchSource
     *            source of the search engine. ex: google
     * @param argSearchKeywords
     *            the keywords of the search. ex: java google analytics tracking
     *            utility
     */
    public void trackPageViewFromSearch(String argPageURL, String argPageTitle, String argHostName, String argSearchSource, String argSearchKeywords) {
        if (argPageURL == null) {
            throw new IllegalArgumentException("Page URL cannot be null, Google will not track the data.");
        }
        AnalyticsRequestData data = new AnalyticsRequestData();
        data.setHostName(argHostName);
        data.setPageTitle(argPageTitle);
        data.setPageURL(argPageURL);
        data.setSearchReferrer(argSearchSource, argSearchKeywords);
        makeCustomRequest(data);
    }

    /**
     * Tracks an event. To provide more info about the page, use
     * {@link #makeCustomRequest(AnalyticsRequestData)}.
     * 
     * @param argCategory
     * @param argAction
     */
    public void trackEvent(String argCategory, String argAction) {
        trackEvent(argCategory, argAction, null, null);
    }

    /**
     * Tracks an event. To provide more info about the page, use
     * {@link #makeCustomRequest(AnalyticsRequestData)}.
     * 
     * @param argCategory
     * @param argAction
     * @param argLabel
     */
    public void trackEvent(String argCategory, String argAction, String argLabel) {
        trackEvent(argCategory, argAction, argLabel, null);
    }

    /**
     * Tracks an event. To provide more info about the page, use
     * {@link #makeCustomRequest(AnalyticsRequestData)}.
     * 
     * @param argCategory
     *            required
     * @param argAction
     *            required
     * @param argLabel
     *            optional
     * @param argValue
     *            optional
     */
    public void trackEvent(String argCategory, String argAction, String argLabel, Integer argValue) {
        AnalyticsRequestData data = new AnalyticsRequestData();
        data.setEventCategory(argCategory);
        data.setEventAction(argAction);
        data.setEventLabel(argLabel);
        data.setEventValue(argValue);
        makeCustomRequest(data);
    }

    /**
     * Makes a custom tracking request based from the given data.
     * 
     * @param argData
     * @throws NullPointerException
     *             if argData is null or if the URL builder is null
     */
    public synchronized void makeCustomRequest(AnalyticsRequestData argData) {
        if (!enabled) {
            logger.debug("Ignoring tracking request, enabled is false");
            return;
        }
        if (argData == null) {
            throw new NullPointerException("Data cannot be null");
        }
        if (builder == null) {
            throw new NullPointerException("Class was not initialized");
        }
        final String url = builder.buildURL(argData);
        switch(mode) {
            case MULTI_THREAD:
                Thread t = new Thread(asyncThreadGroup, "AnalyticsThread-" + asyncThreadGroup.activeCount()) {

                    public void run() {
                        synchronized (JGoogleAnalyticsTracker.class) {
                            asyncThreadsRunning++;
                        }
                        try {
                            dispatchRequest(url);
                        } finally {
                            synchronized (JGoogleAnalyticsTracker.class) {
                                asyncThreadsRunning--;
                            }
                        }
                    }
                };
                t.setDaemon(true);
                t.start();
                break;
            case SYNCHRONOUS:
                dispatchRequest(url);
                break;
            default:
                synchronized (fifo) {
                    fifo.addLast(url);
                    fifo.notify();
                }
                if (!backgroundThreadMayRun) {
                    logger.error("A tracker request has been added to the queue but the background thread isn't running.", url);
                }
                break;
        }
    }

    private static void dispatchRequest(String argURL) {
        try {
            URL url = new URL(argURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.error("JGoogleAnalyticsTracker: Error requesting url '{}', received response code {}", argURL, responseCode);
            } else {
                logger.debug("JGoogleAnalyticsTracker: Tracking success for url '{}'", new Object[] { argURL });
            }
        } catch (Exception e) {
            logger.error("Error making tracking request", e);
        }
    }

    private void createBuilder() {
        switch(gaVersion) {
            case V_4_7_2:
                builder = new GoogleAnalyticsV4_7_2(configData);
                break;
            default:
                builder = new GoogleAnalyticsV4_7_2(configData);
                break;
        }
    }

    /**
     * If the background thread for 'queued' mode is not running, start it now.
     */
    private static synchronized void startBackgroundThread() {
        if (backgroundThread == null) {
            backgroundThreadMayRun = true;
            backgroundThread = new Thread(asyncThreadGroup, "AnalyticsBackgroundThread") {

                public void run() {
                    logger.debug("AnalyticsBackgroundThread started");
                    while (backgroundThreadMayRun) {
                        try {
                            String url = null;
                            synchronized (fifo) {
                                if (fifo.isEmpty()) {
                                    fifo.wait();
                                }
                                if (!fifo.isEmpty()) {
                                    url = fifo.getFirst();
                                }
                            }
                            if (url != null) {
                                try {
                                    dispatchRequest(url);
                                } finally {
                                    synchronized (fifo) {
                                        fifo.removeFirst();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Got exception from dispatch thread", e);
                        }
                    }
                }
            };
            backgroundThread.setDaemon(true);
            backgroundThread.start();
        }
    }

    /**
     * Stop the long-lived background thread.
     * <p>
     * This method is needed for debugging purposes only. Calling it in an application is not really
     * required: The background thread will terminate automatically when the application exits.
     * 
     * @param timeoutMillis  If nonzero, wait for thread completion before returning.
     */
    public static void stopBackgroundThread(long timeoutMillis) {
        backgroundThreadMayRun = false;
        synchronized (fifo) {
            fifo.notify();
        }
        if ((backgroundThread != null) && (timeoutMillis > 0)) {
            try {
                backgroundThread.join(timeoutMillis);
            } catch (InterruptedException e) {
            }
            backgroundThread = null;
        }
    }
}
