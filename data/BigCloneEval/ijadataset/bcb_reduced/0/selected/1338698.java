package fw4ex_client.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import fw4ex_client.Activator;

public class ZipFiles {

    protected HashMap<String, String> files;

    static final int BUFFER = 2048;

    protected String outputfilename;

    private ResourceBundle bundle;

    private boolean zipComplete;

    public ZipFiles(HashMap<String, String> files, String outputfilename) {
        this.files = files;
        this.outputfilename = System.getProperty("java.io.tmpdir") + outputfilename;
        this.bundle = Activator.getDefault().getResourceBundle();
        this.zipComplete = false;
    }

    public String getOutputfilename() {
        return outputfilename;
    }

    public void setOutputfilename(String outputfilename) {
        this.outputfilename = outputfilename;
    }

    public HashMap<String, String> getFiles() {
        return files;
    }

    public void setFiles(HashMap<String, String> files) {
        this.files = files;
    }

    public boolean isZipComplete() {
        return zipComplete;
    }

    public void doZip() {
        String filename = "";
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(outputfilename);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];
            Set<String> keys = files.keySet();
            for (String key : keys) {
                filename = key;
                String fpath = files.get(key);
                FileInputStream fi = new FileInputStream(fpath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(key);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
            zipComplete = true;
        } catch (FileNotFoundException e) {
            Activator.getDefault().showMessage(bundle.getString("Missing_File") + ": " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
