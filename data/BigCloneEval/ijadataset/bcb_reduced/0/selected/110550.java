package org.p2s.core;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BlogMusikMp3Stream {

    public static InputStream getBlogmusikStream(String puid, String filename) throws Exception {
        String url = "http://blogmusik.net/encapsulation.php?ID=" + puid;
        System.out.println("trying Blogmusik puid " + puid);
        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();
        if ("audio/mpeg".equals(conn.getHeaderField("Content-Type"))) return new InputStreamProxy(conn.getInputStream(), filename);
        conn.getInputStream().close();
        return null;
    }
}
