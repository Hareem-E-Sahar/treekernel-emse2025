package org.apache.harmony.archive.tests.java.util.jar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import tests.support.Support_Exec;
import tests.support.resource.Support_Resources;

public class JarOutputStreamTest extends junit.framework.TestCase {

    /**
	 * @tests java.util.jar.JarOutputStream#putNextEntry(java.util.zip.ZipEntry)
	 */
    public void test_putNextEntryLjava_util_zip_ZipEntry() throws Exception {
        final String testClass = "hyts_mainClass.ser";
        final String entryName = "foo/bar/execjartest/MainClass.class";
        final String[] manifestMain = { "foo.bar.execjartest.MainClass", "foo/bar/execjartest/MainClass" };
        for (String element : manifestMain) {
            Manifest newman = new Manifest();
            Attributes att = newman.getMainAttributes();
            att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
            att.put(Attributes.Name.MAIN_CLASS, element);
            File outputJar = null;
            JarOutputStream jout = null;
            outputJar = File.createTempFile("hyts_", ".jar");
            jout = new JarOutputStream(new FileOutputStream(outputJar), newman);
            jout.putNextEntry(new JarEntry(entryName));
            File resources = Support_Resources.createTempFolder();
            Support_Resources.copyFile(resources, null, testClass);
            URL jarURL = new URL((new File(resources, testClass)).toURL().toString());
            InputStream jis = jarURL.openStream();
            byte[] bytes = new byte[1024];
            int len;
            while ((len = jis.read(bytes)) != -1) {
                jout.write(bytes, 0, len);
            }
            jout.flush();
            jout.close();
            jis.close();
            String res = null;
            String[] args = new String[2];
            args[0] = "-jar";
            args[1] = outputJar.getAbsolutePath();
            res = Support_Exec.execJava(args, null, true);
            assertTrue("Error executing JAR test on: " + element + ". Result returned was incorrect.", res.startsWith("TEST"));
            outputJar.delete();
        }
    }

    public void test_JarOutputStreamLjava_io_OutputStreamLjava_util_jar_Manifest() throws IOException {
        File fooJar = File.createTempFile("hyts_", ".jar");
        File barZip = File.createTempFile("hyts_", ".zip");
        FileOutputStream fos = new FileOutputStream(fooJar);
        Manifest man = new Manifest();
        Attributes att = man.getMainAttributes();
        att.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        att.put(Attributes.Name.MAIN_CLASS, "foo.bar.execjartest.Foo");
        att.put(Attributes.Name.CLASS_PATH, barZip.getName());
        fos.close();
        try {
            new JarOutputStream(fos, man);
            fail("IOException expected");
        } catch (IOException ee) {
        }
        try {
            new JarOutputStream(fos, null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
        }
    }

    public void test_JarOutputStreamLjava_io_OutputStream() throws IOException {
        File fooJar = File.createTempFile("hyts_", ".jar");
        FileOutputStream fos = new FileOutputStream(fooJar);
        ZipEntry ze = new ZipEntry("Test");
        try {
            JarOutputStream joutFoo = new JarOutputStream(fos);
            joutFoo.putNextEntry(ze);
            joutFoo.write(33);
        } catch (IOException ee) {
            fail("IOException is not expected");
        }
        fos.close();
        fooJar.delete();
        try {
            JarOutputStream joutFoo = new JarOutputStream(fos);
            joutFoo.putNextEntry(ze);
            fail("IOException expected");
        } catch (IOException ee) {
        }
    }

    @Override
    protected void setUp() {
    }

    @Override
    protected void tearDown() {
    }
}
