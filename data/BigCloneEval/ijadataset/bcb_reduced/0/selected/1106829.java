package net.sf.fixx;

import java.io.*;
import java.util.zip.*;
import java.util.*;

/**
 * Manage a distinct directory in java.io.tmp (default) for use as a fixture.
 * <p>2do: introduce a custom RuntimeException subclass for those who want to
 * distinguish these exceptions from unexpected runtime exceptions.
 * @author Michael R. Abato
 **/
public class DirectoryFixture {

    public class DirectoryFixtureException extends IOException {

        private DirectoryFixtureException(String message) {
            super(message);
        }
    }

    public static final File SYSTEM_TEMP_DIR = new File(System.getProperty("system.tmp", System.getProperty("java.io.tmpdir", "/tmp")));

    private final File dir;

    public DirectoryFixture(Class clazz) {
        this(clazz, SYSTEM_TEMP_DIR);
    }

    public DirectoryFixture(Class clazz, File base) {
        this(new File(base, clazz.getName()));
    }

    public DirectoryFixture(File dir) {
        this.dir = dir;
    }

    public void doSetup() throws DirectoryFixtureException {
        if (!dir.isDirectory()) {
            dir.mkdirs();
            if (!dir.isDirectory()) {
                throw new DirectoryFixtureException("Failed to create fixture directory '" + dir + "'");
            }
        }
    }

    public void doTeardown() throws DirectoryFixtureException {
        deleteDirectory(dir);
        if (dir.exists()) {
            throw new DirectoryFixtureException("Failed to delete fixture directory '" + dir + "'");
        }
    }

    public File createFile(String name, String contents) throws IOException {
        doSetup();
        return createFile(name, contents.getBytes());
    }

    public File createFile(String name, byte[] data) throws IOException {
        doSetup();
        File file = new File(dir, name);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.flush();
        fos.close();
        return file;
    }

    public File createFile(String name, InputStream in) throws IOException {
        doSetup();
        File file = new File(dir, name);
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[4000];
        while (true) {
            int n = in.read(buffer);
            if (n < 0) break;
            fos.write(buffer, 0, n);
        }
        fos.flush();
        fos.close();
        return file;
    }

    public File createFile(String name, long n) throws IOException {
        doSetup();
        File file = new File(dir, name);
        OutputStream fout = new FileOutputStream(file);
        BufferedOutputStream bout = new BufferedOutputStream(fout);
        DataOutputStream dout = new DataOutputStream(bout);
        Random r = new Random();
        long numlongs = n / 8;
        for (long i = 0; i < numlongs; ++i) {
            dout.writeLong(r.nextLong());
        }
        for (int i = 0; i < n % 8; ++i) {
            dout.writeByte(r.nextInt());
        }
        dout.flush();
        bout.flush();
        fout.close();
        return file;
    }

    public long checksumFile(String name) throws IOException {
        byte[] buffer = new byte[4000];
        InputStream in = new FileInputStream(new File(dir, name));
        Checksum cksum = new CRC32();
        while (true) {
            int n = in.read(buffer);
            if (n < 0) break;
            cksum.update(buffer, 0, n);
        }
        return cksum.getValue();
    }

    public DirectoryFixture createUniqueDirectory(String prefix) throws IOException {
        doSetup();
        File file = File.createTempFile(prefix, "", dir);
        file.delete();
        file.delete();
        return new DirectoryFixture(file);
    }

    public DirectoryFixture createDirectory(String name) {
        return new DirectoryFixture(new File(dir, name));
    }

    public static File getSystemTempDir() {
        return SYSTEM_TEMP_DIR;
    }

    public File getDir() {
        return dir;
    }

    public static String read(InputStream is) throws IOException {
        return new String(readBytes(is));
    }

    public static String read(File f) throws IOException {
        return new String(readBytes(f));
    }

    public static byte[] readBytes(File f) throws IOException {
        InputStream in = new FileInputStream(f);
        return readBytes(in);
    }

    public static byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4000];
        while (true) {
            int n = in.read(buffer);
            if (n < 0) break;
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }

    public static void deleteDirectory(File dir) {
        File[] kids = dir.listFiles();
        for (int i = 0; kids != null && i < kids.length; i++) {
            File kid = kids[i];
            if (kid.isDirectory()) {
                deleteDirectory(kid);
            } else {
                kid.delete();
            }
        }
        dir.delete();
    }

    public File createUniqueFile(String prefix, String contents) throws IOException {
        return createUniqueFile(prefix, "", contents);
    }

    public File createUniqueFile(String prefix, byte[] data) throws IOException {
        return createUniqueFile(prefix, "", data);
    }

    public File createUniqueFile(String prefix, String suffix, String contents) throws IOException {
        return createFile(File.createTempFile(prefix, suffix, dir).getName(), contents);
    }

    public File createUniqueFile(String prefix, String suffix, byte[] data) throws IOException {
        return createFile(File.createTempFile(prefix, suffix, dir).getName(), data);
    }

    public void execCmd(String cmd, String[] envp) throws Exception {
        execCmd(cmd, envp, dir);
    }

    private static void execCmd(String cmd, String[] envp, File dir) throws Exception {
        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(cmd, envp, dir);
        int exitValue = p.waitFor();
        if (exitValue != 0) {
            System.out.println();
            System.out.println("CMD: " + cmd);
            System.out.println("exit value: " + exitValue);
            System.out.println("STDOUT:");
            debugStream(p.getInputStream());
            System.out.println("STDERR:");
            debugStream(p.getErrorStream());
            new Exception("trace").printStackTrace();
        }
    }

    static void debugStream(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4000];
        while (true) {
            int n = is.read(buffer);
            if (n < 0) break;
            out.write(buffer, 0, n);
        }
        System.out.println(new String(out.toByteArray()));
    }

    public File copyFile(String src) throws IOException {
        doSetup();
        File srcFile = new File(src);
        File file = new File(dir, srcFile.getName());
        OutputStream fout = new FileOutputStream(file);
        InputStream in = new FileInputStream(srcFile);
        byte[] buffer = new byte[4000];
        while (true) {
            int n = in.read(buffer);
            if (n < 0) break;
            fout.write(buffer, 0, n);
        }
        fout.close();
        return file;
    }

    public String toString() {
        return "DirectoryFixture[" + getDir() + "]";
    }
}
