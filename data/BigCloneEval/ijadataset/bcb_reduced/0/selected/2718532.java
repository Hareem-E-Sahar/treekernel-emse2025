package oqube.patchwork;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class MainTest extends TestCase {

    private File dir;

    private File jar;

    protected void setUp() throws Exception {
        super.setUp();
        jar = File.createTempFile("test", ".jar");
        jar.deleteOnExit();
        JarOutputStream jout = new JarOutputStream(new FileOutputStream(jar));
        ZipEntry zip = new ZipEntry("oqube/patchwork/Dummy.class");
        InputStream is = getClass().getResourceAsStream("/main.bin");
        jout.putNextEntry(zip);
        byte[] buf = new byte[1024];
        int rd = 0;
        while ((rd = is.read(buf, 0, 1024)) != -1) {
            jout.write(buf, 0, rd);
        }
        jout.closeEntry();
        jout.finish();
        jout.flush();
        jout.close();
        dir = jar.getParentFile();
    }

    public void test00NoEnv() {
        String[] args = new String[0];
        Launch l = new Launch();
        l.setArguments(args);
        l.run();
        assertEquals(1, l.getStatus());
    }

    public void testLaunchDummyMain() {
        System.setProperty("launcher.libdir", dir.getAbsolutePath());
        System.setProperty("launcher.main", "oqube.patchwork.Dummy");
        String[] args = new String[0];
        Launch l = new Launch();
        l.setArguments(args);
        l.run();
        assertEquals(0, l.getStatus());
    }

    public void test02DirNotExists() {
        System.setProperty("launcher.libdir", "");
        String[] args = new String[0];
        Launch l = new Launch();
        l.setArguments(args);
        l.run();
        assertEquals(2, l.getStatus());
    }

    public void test03NotADir() {
        System.setProperty("launcher.libdir", jar.getAbsolutePath());
        String[] args = new String[0];
        Launch l = new Launch();
        l.setArguments(args);
        l.run();
        assertEquals(2, l.getStatus());
    }

    public void test04MainClassSet() throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        System.setProperty("launcher.libdir", dir.getAbsolutePath());
        System.setProperty("launcher.main", "oqube.patchwork.Dummy");
        String[] args = new String[] { "1.2.3.4", "toto" };
        Launch l = new Launch();
        l.setArguments(args);
        l.run();
        assertEquals("Return code should be 0", 0, l.getStatus());
        checkDummy(l, args);
    }

    private void checkDummy(Launch l, String[] args) throws ClassNotFoundException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class cls = l.getLoader().loadClass("oqube.patchwork.Dummy");
        Field f = (Field) cls.getDeclaredField("args");
        String[] res = (String[]) f.get(null);
        for (int i = 1; i < args.length; i++) {
            if (!args[i].equals(res[i])) throw new AssertionFailedError("Expected " + args[i] + " but found " + res[i] + " in passed arguments from Main");
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        jar.delete();
    }
}
