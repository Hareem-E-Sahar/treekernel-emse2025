package com.razie.pub.comms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import razie.assets.AssetLocation;
import razie.base.AttrAccess;
import com.razie.pub.base.data.ByteArray;
import com.razie.pub.base.log.Log;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

/**
 * communications utils
 * 
 * TODO detailed docs
 * 
 * @author razvanc99
 * 
 */
public class MoreComms extends Comms {

    /**
     * Stream the response of a URL.
     * 
     * @param url can be local or remote
     * @return a string containing the text read from the URL. can be the result of a servlet, a web
     *         page or the contents of a local file. It's null if i couldn't read the file.
     */
    public static InputStream poststreamUrl(String url, AttrAccess httpArgs, String content) {
        try {
            InputStream in = null;
            AssetLocation temploc = new AssetLocation(url);
            String cmd = "POST " + url.replace(temploc.toHttp(), "") + " HTTP/1.1";
            Socket remote = HttpHelper.sendPOST(temploc.getHost(), Integer.decode(temploc.getPort()), cmd, httpArgs, content);
            in = remote.getInputStream();
            return in;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        } catch (IOException e1) {
            throw new RuntimeException("Connection exception for url=" + url, e1);
        }
    }

    /**
     * Stream the response of a URL.
     * 
     * @param url can be local or remote
     * @return a string containing the text read from the URL. can be the result of a servlet, a web
     *         page or the contents of a local file. It's null if i couldn't read the file.
     */
    public static InputStream xpoststreamUrl2(String url, AttrAccess httpArgs, String content) {
        try {
            InputStream in = null;
            URLConnection uc = (new URL(url)).openConnection();
            uc.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(uc.getOutputStream());
            wr.write(content);
            wr.flush();
            logger.trace(3, "hdr: ", uc.getHeaderFields());
            String resCode = uc.getHeaderField(0);
            in = uc.getInputStream();
            if (!resCode.endsWith("200 OK")) {
                String msg = "Could not fetch data from url " + url + ", resCode=" + resCode;
                logger.trace(3, msg);
                RuntimeException rte = new RuntimeException(msg);
                if (uc.getContentType().endsWith("xml")) {
                    DOMParser parser = new DOMParser();
                    try {
                        parser.parse(new InputSource(in));
                    } catch (SAXException e) {
                        RuntimeException iex = new RuntimeException("Error while processing document at " + url);
                        iex.initCause(e);
                        throw iex;
                    }
                }
                throw rte;
            }
            return in;
        } catch (MalformedURLException e) {
            RuntimeException iex = new IllegalArgumentException();
            iex.initCause(e);
            throw iex;
        } catch (IOException e1) {
            throw new RuntimeException("Connection exception for url=" + url, e1);
        }
    }

    /**
     * Serialize to string the response of a URL, via POST rather than GET.
     * 
     * @param url MUST be remotec
     * @param httpArgs the args to be sent with the HTTP request
     * @param content the content of the POST
     * @return a string containing the text read from the URL. can be the result of a servlet, a web
     *         page or the contents of a local file. It's null if i couldn't read the file.
     */
    public static String readUrlPOST(String url, AttrAccess httpArgs, String content) {
        InputStream s = poststreamUrl(url, httpArgs, content);
        if (s == null) {
            return null;
        }
        return readStream(s);
    }

    static Log logger = Log.create(Comms.class.getName());
}
