package tests.support.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import tests.support.Support_Configuration;

public class Support_Resources {

    public static final String RESOURCE_PACKAGE = "/tests/resources/";

    public static final String RESOURCE_PACKAGE_NAME = "tests.resources";

    public static InputStream getStream(String name) {
        return Support_Resources.class.getResourceAsStream(RESOURCE_PACKAGE + name);
    }

    public static String getURL(String name) {
        String folder = null;
        String fileName = name;
        File resources = createTempFolder();
        int index = name.lastIndexOf("/");
        if (index != -1) {
            folder = name.substring(0, index);
            name = name.substring(index + 1);
        }
        copyFile(resources, folder, name);
        URL url = null;
        String resPath = resources.toString();
        if (resPath.charAt(0) == '/' || resPath.charAt(0) == '\\') {
            resPath = resPath.substring(1);
        }
        try {
            url = new URL("file:/" + resPath + "/" + fileName);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url.toString();
    }

    public static File createTempFolder() {
        File folder = null;
        try {
            folder = File.createTempFile("hyts_resources", "", null);
            folder.delete();
            folder.mkdirs();
        } catch (IOException e) {
            e.printStackTrace();
        }
        folder.deleteOnExit();
        return folder;
    }

    public static void copyFile(File root, String folder, String file) {
        File f;
        if (folder != null) {
            f = new File(root.toString() + "/" + folder);
            if (!f.exists()) {
                f.mkdirs();
                f.deleteOnExit();
            }
        } else {
            f = root;
        }
        File dest = new File(f.toString() + "/" + file);
        InputStream in = Support_Resources.getStream(folder == null ? file : folder + "/" + file);
        try {
            copyLocalFileto(dest, in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File createTempFile(String suffix) throws IOException {
        return File.createTempFile("hyts_", suffix, null);
    }

    public static void copyLocalFileto(File dest, InputStream in) throws FileNotFoundException, IOException {
        if (!dest.exists()) {
            FileOutputStream out = new FileOutputStream(dest);
            int result;
            byte[] buf = new byte[4096];
            while ((result = in.read(buf)) != -1) {
                out.write(buf, 0, result);
            }
            in.close();
            out.close();
            dest.deleteOnExit();
        }
    }

    public static File getExternalLocalFile(String url) throws IOException, MalformedURLException {
        File resources = createTempFolder();
        InputStream in = new URL(url).openStream();
        File temp = new File(resources.toString() + "/local.tmp");
        copyLocalFileto(temp, in);
        return temp;
    }

    public static String getResourceURL(String resource) {
        return "http://" + Support_Configuration.TestResources + resource;
    }

    /**
     * Util method to load resource files
     * 
     * @param name - name of resource file
     * @return - resource input stream
     */
    public static InputStream getResourceStream(String name) {
        InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(name);
        if (is == null) {
            throw new RuntimeException("Failed to load resource: " + name);
        }
        return is;
    }

    /**
     * Util method to get absolute path to resource file
     * 
     * @param name - name of resource file
     * @return - path to resource
     */
    public static String getAbsoluteResourcePath(String name) {
        URL url = ClassLoader.getSystemClassLoader().getResource(name);
        if (url == null) {
            throw new RuntimeException("Failed to load resource: " + name);
        }
        try {
            return new File(url.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to load resource: " + name);
        }
    }
}
