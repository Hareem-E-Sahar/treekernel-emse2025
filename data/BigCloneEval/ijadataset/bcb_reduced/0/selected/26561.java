package org.encog.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import org.encog.EncogError;
import org.encog.parse.tags.Tag;
import org.encog.parse.tags.read.ReadHTML;
import org.encog.util.http.FormUtility;

/**
 * YahooSearch: Perform a search using Yahoo.
 */
public class YahooSearch {

    /**
	 * How many retries.
	 */
    private static final int MAX_TRIES = 5;

    /**
	 * How long to sleep between retry.
	 */
    private static final long RETRY_SLEEP = 5000;

    /**
	 * Do a search using the Yahoo search engine. Called internally.
	 * 
	 * @param url
	 *            The Yahoo URL.
	 * @return A collection of URL's.
	 * @throws IOException
	 *             An error occured communicating with Yahoo.
	 */
    private Collection<URL> doSearch(final URL url) throws IOException {
        final Collection<URL> result = new ArrayList<URL>();
        final InputStream is = url.openStream();
        final ReadHTML parse = new ReadHTML(is);
        final StringBuilder buffer = new StringBuilder();
        boolean capture = false;
        int ch;
        while ((ch = parse.read()) != -1) {
            if (ch == 0) {
                final Tag tag = parse.getTag();
                if (tag.getName().equalsIgnoreCase("url")) {
                    buffer.setLength(0);
                    capture = true;
                } else if (tag.getName().equalsIgnoreCase("/url")) {
                    result.add(new URL(buffer.toString()));
                    buffer.setLength(0);
                    capture = false;
                }
            } else {
                if (capture) {
                    buffer.append((char) ch);
                }
            }
        }
        return result;
    }

    /**
	 * Called to extract a list from the specified URL.
	 * 
	 * @param searchFor
	 *            What to search for.
	 * @return The URL's found for the specific search.
	 * @throws IOException
	 *             Error connecting to Yahoo.
	 */
    public Collection<URL> search(final String searchFor) throws IOException {
        Collection<URL> result = null;
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final FormUtility form = new FormUtility(bos, null);
        form.add("appid", "YahooDemo");
        form.add("results", "100");
        form.add("query", searchFor);
        form.complete();
        final URL url = new URL("http://search.yahooapis.com/WebSearchService/V1/webSearch?" + bos.toString());
        bos.close();
        int tries = 0;
        boolean done = false;
        while (!done) {
            try {
                result = doSearch(url);
                done = true;
            } catch (final IOException e) {
                if (tries == YahooSearch.MAX_TRIES) {
                    throw e;
                }
                try {
                    Thread.sleep(YahooSearch.RETRY_SLEEP);
                } catch (final InterruptedException e1) {
                    throw new EncogError("Interrupted");
                }
            }
            tries++;
        }
        return result;
    }
}
