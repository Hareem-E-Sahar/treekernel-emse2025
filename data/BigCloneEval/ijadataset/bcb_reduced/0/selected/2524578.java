package com.jPianoBar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: vincent
 * Date: 7/17/11
 * Time: 1:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class PandoraSongDownloader {

    public byte[] downloadSong(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        ByteArrayOutputStream bais = new ByteArrayOutputStream();
        InputStream is = null;
        try {
            is = url.openStream();
            byte[] byteChunk = new byte[4096];
            int n;
            while ((n = is.read(byteChunk)) > 0) {
                bais.write(byteChunk, 0, n);
            }
        } catch (IOException e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return bais.toByteArray();
    }
}
