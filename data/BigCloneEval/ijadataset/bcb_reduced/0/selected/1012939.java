package is.hi.bok.deduplicator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;

/**
 * An implementation of a  {@link is.hi.bok.deduplicator.CrawlDataIterator}
 * capable of iterating over a Heritrix's style <code>crawl.log</code>.
 * 
 * @author Kristinn Sigur&eth;sson
 * @author Lars Clausen
 */
public class CrawlLogIterator extends CrawlDataIterator {

    /**
	 * The date format used in crawl.log files.
	 */
    protected final SimpleDateFormat crawlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    /**
     * The date format specified by the {@link CrawlDataItem} for dates 
     * entered into it (and eventually into the index)
     */
    protected final SimpleDateFormat crawlDataItemFormat = new SimpleDateFormat(CrawlDataItem.dateFormat);

    /** 
     * A reader for the crawl.log file being processed
     */
    protected BufferedReader in;

    /**
     * The next item to be issued (if ready) or null if the next item
     * has not been prepared or there are no more elements 
     */
    protected CrawlDataItem next;

    /** 
     * Create a new CrawlLogIterator that reads items from a Heritrix crawl.log
     *
     * @param source The path of a Heritrix crawl.log file.
     * @throws IOException If errors were found reading the log.
     */
    public CrawlLogIterator(String source) throws IOException {
        super(source);
        in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(source))));
    }

    /** 
     * Returns true if there are more items available.
     *
     * @return True if at least one more item can be fetched with next().
     */
    public boolean hasNext() throws IOException {
        if (next == null) {
            prepareNext();
        }
        return next != null;
    }

    /** 
     * Returns the next valid item from the crawl log.
     *
     * @return An item from the crawl log.  Note that unlike the Iterator
     *         interface, this method returns null if there are no more items 
     *         to fetch.
     * @throws IOException If there is an error reading the item *after* the
     *         item to be returned from the crawl.log.
     * @throws NoSuchElementException If there are no more items 
     */
    public CrawlDataItem next() throws IOException {
        if (hasNext()) {
            CrawlDataItem tmp = next;
            this.next = null;
            return tmp;
        }
        throw new NoSuchElementException("No more items");
    }

    /**
     * Ready the next item.  This method will skip over items that
     * getNextItem() rejects.  When the method returns, either next is non-null
     * or there are no more items in the crawl log.
     * <p>
     * Note: This method should only be called when <code>next==null<code>
     */
    protected void prepareNext() throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            next = parseLine(line);
            if (next != null) {
                return;
            }
        }
    }

    /** 
     * Parse the a line in the crawl log.
     * <p>
     * Override this method to change how individual crawl log
     * items are processed and accepted/rejected.  This method is called from
     * within the loop in prepareNext().
     *
     * @param line A line from the crawl log.  Must not be null.
     * @return A {@link CrawlDataItem} if the next line in the crawl log yielded 
     *         a usable item, null otherwise.
     */
    protected CrawlDataItem parseLine(String line) {
        if (line != null && line.length() > 42) {
            String[] lineParts = line.split("\\s+", 12);
            if (lineParts.length < 10) {
                return null;
            }
            String timestamp;
            try {
                timestamp = crawlDataItemFormat.format(crawlDateFormat.parse(lineParts[0]));
            } catch (ParseException e) {
                System.err.println("Error parsing date for: " + line);
                e.printStackTrace();
                return null;
            }
            String url = lineParts[3];
            String mime = lineParts[6];
            String digest = lineParts[9];
            if (digest.lastIndexOf(":") >= 0) {
                digest = digest.substring(digest.lastIndexOf(":") + 1);
            }
            String origin = null;
            boolean duplicate = false;
            if (lineParts.length == 12) {
                String annotation = lineParts[11];
                int startIndex = annotation.indexOf("duplicate:\"");
                if (startIndex >= 0) {
                    startIndex += 11;
                    int endIndex = annotation.indexOf('"', startIndex + 1);
                    origin = annotation.substring(startIndex, endIndex);
                    duplicate = true;
                } else if (annotation.contains("duplicate")) {
                    duplicate = true;
                }
            }
            return new CrawlDataItem(url, digest, timestamp, null, mime, origin, duplicate);
        }
        return null;
    }

    /**
     * Closes the crawl.log file.
     */
    public void close() throws IOException {
        in.close();
    }

    public String getSourceType() {
        return "Handles Heritrix style crawl.log files";
    }
}
