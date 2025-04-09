package lektor.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zipper {

    public void createZipFile(File fileToAdd, File cssFile) {
        try {
            byte[] buf = new byte[4096];
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(getOutFile(fileToAdd)));
            out.setLevel(9);
            FileInputStream in = new FileInputStream(fileToAdd);
            out.putNextEntry(new ZipEntry(fileToAdd.getName()));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in = new FileInputStream(cssFile);
            out.putNextEntry(new ZipEntry(cssFile.getName()));
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            fileToAdd.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getOutFile(File fileToAdd) {
        int suffixIndex = fileToAdd.getAbsolutePath().lastIndexOf(".");
        String prefix = fileToAdd.getAbsolutePath().substring(0, suffixIndex);
        return new File(prefix + ".zip");
    }
}
