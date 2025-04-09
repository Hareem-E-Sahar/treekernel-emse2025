package fr.enseirb.webxml.util;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import fr.enseirb.webxml.servlet.listener.WebAppInitializer;

public final class ServletToolkit {

    private static final Logger LOGGER = Logger.getLogger(ServletToolkit.class.getName());

    public static String readFile(String path) throws IOException {
        String fullPath = WebAppInitializer.ROOT + path;
        LOGGER.log(Level.INFO, "Reading file: " + fullPath);
        InputStream stream = new FileInputStream(fullPath);
        StringBuilder builder = new StringBuilder();
        int byt = -1;
        while ((byt = stream.read()) != -1) {
            builder.append((char) byt);
        }
        String stringifiedStream = builder.toString();
        stream.close();
        return new String(stringifiedStream.getBytes(), "UTF-8");
    }

    public static byte[] readRawFile(String path) throws IOException {
        String fullPath = WebAppInitializer.ROOT + path;
        LOGGER.log(Level.INFO, "Reading raw file: " + fullPath);
        InputStream stream = new FileInputStream(fullPath);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int byt = -1;
        while ((byt = stream.read()) != -1) {
            outStream.write(byt);
        }
        stream.close();
        LOGGER.log(Level.INFO, "File read");
        return outStream.toByteArray();
    }

    public static Properties parseURLParams(HttpServletRequest request) {
        String query = request.getQueryString();
        LOGGER.log(Level.INFO, "Parsing URL params for query: " + query);
        Properties props = new Properties();
        if (query != null) {
            query = query.replace("&", "\n\r");
            try {
                props.load(new StringReader(query));
            } catch (IOException e) {
                return null;
            }
        }
        for (Iterator<Object> keyIte = props.keySet().iterator(); keyIte.hasNext(); ) {
            String key = (String) keyIte.next();
            try {
                props.put(key, URLDecoder.decode((String) props.get(key), "utf-8"));
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.INFO, "Error while reading parameter with key: " + key, e);
            }
        }
        LOGGER.log(Level.INFO, "returning: " + props.toString());
        return props;
    }

    public static String getPostData(HttpServletRequest request) throws IOException {
        LOGGER.log(Level.INFO, "Reading post data");
        InputStream stream = request.getInputStream();
        StringBuilder builder = new StringBuilder();
        int byt = -1;
        while ((byt = stream.read()) != -1) {
            builder.append((char) byt);
        }
        String stringifiedStream = builder.toString();
        stream.close();
        stringifiedStream = new String(stringifiedStream.getBytes(), "UTF-8");
        LOGGER.log(Level.INFO, "Post Data are: " + stringifiedStream);
        return stringifiedStream;
    }

    public static byte[] getRawPostData(HttpServletRequest request) throws IOException {
        LOGGER.log(Level.INFO, "Reading raw post data");
        InputStream stream = request.getInputStream();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        int byt = -1;
        while ((byt = stream.read()) != -1) {
            outStream.write(byt);
        }
        stream.close();
        LOGGER.log(Level.INFO, "Raw post Data read");
        return outStream.toByteArray();
    }

    public static void writeResponse(HttpServletResponse response, String text) throws IOException {
        try {
            if (text == null) {
                text = "";
            }
            response.getOutputStream().write(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.INFO, "Problem while encoding response", e);
        }
    }
}
