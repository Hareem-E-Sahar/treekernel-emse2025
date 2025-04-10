package org.ogre4j.examples;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.util.MissingResourceException;

/**
 * Takes care that resources (media.jar) are downloaded.
 * For Java 5 automatic downloading of webstart is used.
 * As cache handling has changed in Java 6 resources must
 * be downloaded manually then.
 */
public class WebStartLauncher {

    /**
	 * main.
	 * 
	 * @param args
	 *            arguments
	 */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("No arguments given for WebStartLauncher.");
        }
        String clazz = args[0];
        if (clazz == null || "".equals(clazz)) {
            throw new IllegalArgumentException("First argument of WebStartLauncher must be a full qualified class name.");
        }
        String renderSystem = args[1];
        if (renderSystem == null || "".equals(renderSystem)) {
            renderSystem = "OpenGL";
        }
        Class<?> mediaClass = null;
        try {
            mediaClass = Class.forName("org.ogre4j.examples.media.Media");
        } catch (ClassNotFoundException e) {
            throw new MissingResourceException("The OGRE media resources are missing.", "org.ogre4j.examples.media.Media", "");
        }
        URL url = mediaClass.getResource("");
        int javaVersion = Integer.parseInt(System.getProperty("java.version").substring(2, 3));
        if (javaVersion < 6) {
            String path = new URL(url.getPath()).getPath();
            int index = path.lastIndexOf('!');
            if (index != -1) {
                path = path.substring(0, index);
            }
            path = path.replace("%20", " ");
            File file = new File(path);
            run(clazz, file.getCanonicalPath(), renderSystem);
        } else {
            String remoteFile = url.getPath();
            int index = remoteFile.lastIndexOf('!');
            if (index != -1) {
                remoteFile = remoteFile.substring(0, index);
            }
            url = new URL(remoteFile);
            String downloadedFile = downloadMedia("media", url);
            run(clazz, downloadedFile, renderSystem);
        }
    }

    private static void run(String className, String mediazip, String renderSystem) throws Exception {
        Class<?> clazz = Class.forName(className);
        Method method = clazz.getMethod("mainws", new Class[] { String.class, String.class, String.class });
        method.invoke(null, new Object[] { mediazip, "Zip", renderSystem });
    }

    private static String downloadMedia(String mediadir, URL remoteFile) throws Exception, InterruptedException {
        File tmpDir = new File(System.getProperty("java.io.tmpdir") + "org.ogre4j.examples/" + mediadir);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        URLConnection urlConnection = remoteFile.openConnection();
        if (urlConnection.getConnectTimeout() != 0) {
            urlConnection.setConnectTimeout(0);
        }
        InputStream content = remoteFile.openStream();
        BufferedInputStream bin = new BufferedInputStream(content);
        String downloaded = tmpDir.getCanonicalPath() + File.separatorChar + new File(remoteFile.getFile()).getName();
        File file = new File(downloaded);
        BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(file));
        System.out.println("downloading file " + remoteFile + " ...");
        for (long i = 0; i < urlConnection.getContentLength(); i++) {
            bout.write(bin.read());
        }
        bout.close();
        bout = null;
        bin.close();
        return downloaded;
    }
}
