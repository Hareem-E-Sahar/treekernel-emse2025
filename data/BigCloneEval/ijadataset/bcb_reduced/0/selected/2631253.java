package org.zkoss.io;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

/**
 * Convenience class for reading character files.
 *
 * @author tomyeh
 * @since 3.0.8
 */
public class URLReader extends InputStreamReader {

    /**
    * Creates a new FileReader, given the resource URL to read from.
    *
    * @param url the URL of the resource to read
    * @exception FileNotFoundException  if the resource does not exist,
	* is a directory rather than a regular file,
	* or for some other reason cannot be opened for reading.
    */
    public URLReader(URL url, String charset) throws IOException {
        super(openStream(url), charset);
    }

    private static InputStream openStream(URL url) throws IOException {
        final InputStream is = url.openStream();
        if (is == null) throw new FileNotFoundException(url.toExternalForm());
        return is;
    }
}
