package mta.connect.four.ui.gui.themes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Yoav Aharoni
 */
public class ThemeBuilder {

    private ZipOutputStream zip;

    public ThemeBuilder(ZipOutputStream zipArg) {
        zip = zipArg;
    }

    public ThemeBuilder addResource(ThemeResource resource, byte[] resourceValue) throws IOException {
        return addResource(resource.getResourceKey(), resourceValue);
    }

    public ThemeBuilder addResource(String resourceKey, byte[] resourceValue) throws IOException {
        try {
            zip.putNextEntry(new ZipEntry(resourceKey));
            zip.write(resourceValue);
            zip.closeEntry();
        } catch (IOException ex) {
            System.err.println("Error when adding resource to theme: " + ex);
            throw ex;
        }
        return this;
    }

    public boolean close() {
        try {
            zip.close();
            return true;
        } catch (Exception ex) {
            System.err.println("Error when closing theme file: " + ex);
        }
        return false;
    }

    public void addResource(ThemeResource resource, String filePath) throws IOException {
        addResource(resource, readFile(filePath));
    }

    public static byte[] readFile(String filePath) {
        try {
            File file = new File(filePath);
            FileInputStream streamer = new FileInputStream(file);
            byte[] byteArray = new byte[streamer.available()];
            for (int j = 0; j < byteArray.length; j++) {
                byteArray[j] = (byte) streamer.read();
            }
            return byteArray;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Can't read " + filePath, e);
        }
    }
}
