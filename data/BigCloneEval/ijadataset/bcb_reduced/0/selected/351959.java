package jmodnews.controller;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.OutputStream;

/**
 * Contains various utility methods.
 * 
 * @author Michael Schierl <schierlm@gmx.de>
 */
public class Util {

    private Util() {
    }

    public static String[] splitString(String string, String delimiter) {
        List tokens = new ArrayList();
        int pos;
        while ((pos = string.indexOf(delimiter)) != -1) {
            tokens.add(string.substring(0, pos));
            string = string.substring(pos + delimiter.length());
        }
        tokens.add(string);
        return (String[]) tokens.toArray(new String[tokens.size()]);
    }

    public static String loadFile(File f) throws FileNotFoundException, IOException, UnsupportedEncodingException {
        FileInputStream fis = new FileInputStream(f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        streamCopy(fis, baos);
        String res = new String(baos.toByteArray(), "ISO-8859-1");
        return res;
    }

    private static void streamCopy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[4096];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }

    /**
	 * @param c
	 *
	 */
    public static String colorToString(Color c) {
        String s = "000000" + Integer.toHexString(c.getRGB());
        return s.substring(s.length() - 6);
    }

    /**
	 * @param string
	 *
	 */
    public static Color colorFromString(String string) {
        if (string == null || string.equals("------")) return null;
        return new Color(Integer.parseInt(string, 16));
    }

    public static int getConfigInt(Map configMap, String name) {
        try {
            return Integer.parseInt((String) configMap.get(name));
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Unparsable integer " + name, ex);
        }
    }
}
