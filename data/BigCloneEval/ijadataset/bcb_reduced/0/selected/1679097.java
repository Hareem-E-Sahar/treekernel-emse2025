package org.icepdf.ri.util;

import java.io.*;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * This utility class simplifies the process of loading URLs.
 *
 * @since 1.3
 */
public class URLAccess {

    /**
     * Simple utility method to check if a URL is valid.
     * If it is invalid, then urlAccess.errorMessage will say why.
     * If it is valid, then urlAccess.url will be a valid URL object,
     * and urlAccess.inputStream will be opened to access the data from the URL.
     *
     * @param urlLocation
     * @return URLAccess urlAccess
     */
    public static URLAccess doURLAccess(String urlLocation) {
        URLAccess res = new URLAccess();
        res.urlLocation = urlLocation;
        try {
            res.url = new URL(urlLocation);
            PushbackInputStream in = new PushbackInputStream(new BufferedInputStream(res.url.openStream()), 1);
            int b = in.read();
            in.unread(b);
            res.inputStream = in;
        } catch (MalformedURLException e) {
            res.errorMessage = "Malformed URL";
        } catch (FileNotFoundException e) {
            res.errorMessage = "File Not Found";
        } catch (UnknownHostException e) {
            res.errorMessage = "Unknown Host";
        } catch (ConnectException e) {
            res.errorMessage = "Connection Timed Out";
        } catch (IOException e) {
            res.errorMessage = "IO exception";
        }
        return res;
    }

    /**
     * The given URL string given to doURLAccess().
     */
    public String urlLocation;

    /**
     * The resolved URL, if urlLocation was valid.
     */
    public URL url;

    /**
     * Access to the data at the URL, if the URL was valid.
     */
    public InputStream inputStream;

    /**
     * The reason why the URL was invalid.
     */
    public String errorMessage;

    private URLAccess() {
    }

    /**
     * Free up any resources used, immediately.
     */
    public void dispose() {
        urlLocation = null;
        url = null;
        errorMessage = null;
        closeConnection();
    }

    /**
     * Close the connection, but keep all the String
     * information about the connection.
     */
    public void closeConnection() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
            }
            inputStream = null;
        }
    }
}
