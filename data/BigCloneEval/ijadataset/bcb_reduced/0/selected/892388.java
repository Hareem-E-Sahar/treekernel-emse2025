package gridunit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipPacker {

    private static Map bundles = new HashMap();

    /**
     * Compress the files of a TestApplication.
     * @param dir The application to be compressed   
     * @return The zip file
     * @throws IOException
     */
    public static File pack(File dir) throws IOException {
        if (bundles.get(dir) == null) {
            bundles.put(dir, createZip(dir));
        }
        return (File) bundles.get(dir);
    }

    public static File createZip(File dir) throws IOException {
        File zipFile = File.createTempFile("gridunit", ".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream("/tmp/" + zipFile.getName()));
        createZip(dir, out);
        out.close();
        return zipFile;
    }

    private static final void createZip(File f, ZipOutputStream out) throws IOException {
        if (f.isFile()) {
            FileInputStream in = new FileInputStream(f);
            out.putNextEntry(new ZipEntry(f.getAbsolutePath().substring(1)));
            byte[] buf = new byte[2048];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
        } else if (f.isDirectory()) {
            File fs[] = f.listFiles();
            for (int i = 0; i < fs.length; i++) {
                createZip(fs[i], out);
            }
        }
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        ZipPacker.pack(new File("/local/alexandre/workspace/GridUnit"));
    }
}
