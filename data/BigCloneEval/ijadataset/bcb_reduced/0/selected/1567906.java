package it.gashale.jacolib.util;

import it.gashale.jacolib.core.JacolibError;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.net.URL;

public class FileSystem {

    public static String getJarDirectoryName(Class aClass) throws JacolibError {
        String classPath = aClass.getName().replace('.', '/') + ".class";
        URL u = aClass.getClassLoader().getResource(classPath);
        if (!u.getProtocol().equals("jar")) return null;
        String s = u.getFile();
        if (!s.startsWith("file:")) throw new JacolibError("Jar file is not in the local file system");
        int end = s.indexOf("!");
        if (end == -1) throw new JacolibError("Bad jar URL");
        String path = s.substring(5, end);
        if (!path.endsWith(".jar")) throw new JacolibError("Jar file name does not end with .jar");
        path = path.replace('/', File.separatorChar);
        return new File(path).getParentFile().getPath();
    }

    public static File makeTempDirectory() throws JacolibError {
        File temp_dir = null;
        try {
            File dummyFile = File.createTempFile("Jacolib_", "");
            if (!dummyFile.delete()) throw new JacolibError("Error creating temp directory. Could not delete dummy file");
            temp_dir = new File(dummyFile.getAbsolutePath());
            if (!temp_dir.mkdir()) throw new JacolibError("Error creating temp directory. Could not create temporary directory");
            temp_dir.deleteOnExit();
        } catch (IOException e) {
            throw new JacolibError("Error creating temp directory. " + e);
        }
        return temp_dir;
    }

    public static String resourceToSting(String resource) throws IOException {
        InputStream in = FileSystem.class.getClassLoader().getResourceAsStream(resource);
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        byte[] aoBuffer = new byte[512];
        int nBytesRead;
        while ((nBytesRead = in.read(aoBuffer)) > 0) {
            out.write(aoBuffer, 0, nBytesRead);
        }
        in.close();
        return new String(out.toByteArray());
    }

    public static String fileToSting(String filename) throws IOException {
        InputStream in = new FileInputStream(filename);
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        byte[] aoBuffer = new byte[512];
        int nBytesRead;
        while ((nBytesRead = in.read(aoBuffer)) > 0) {
            out.write(aoBuffer, 0, nBytesRead);
        }
        in.close();
        return new String(out.toByteArray());
    }
}
