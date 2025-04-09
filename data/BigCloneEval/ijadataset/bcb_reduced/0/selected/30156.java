package adv.tools;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Alberto Vilches Ratón
 * <p/>
 * Kenshira
 * <p/>
 * Fecha y hora de creación: 14-nov-2007 19:43:22
 */
public class Zip {

    public static Map<String, InputStream> fileArrayToMap(File[] filenames) throws FileNotFoundException {
        Map<String, InputStream> map = new LinkedHashMap<String, InputStream>(filenames.length);
        for (File f : filenames) {
            map.put(f.getName(), new FileInputStream(f));
        }
        return map;
    }

    public static void zip(Map<String, InputStream> map, OutputStream os) throws IOException {
        zip(map, os, -1, null);
    }

    public static void zip(Map<String, InputStream> map, OutputStream os, int level, String comment) throws IOException {
        ZipOutputStream out = new ZipOutputStream(os);
        if (level != -1) out.setLevel(level);
        if (comment != null) out.setComment(comment);
        byte[] buf = new byte[4096];
        for (Map.Entry<String, InputStream> entry : map.entrySet()) {
            BufferedInputStream in = null;
            try {
                in = new BufferedInputStream(entry.getValue());
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                out.putNextEntry(zipEntry);
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
            } finally {
                if (in != null) in.close();
            }
        }
        out.finish();
    }
}
