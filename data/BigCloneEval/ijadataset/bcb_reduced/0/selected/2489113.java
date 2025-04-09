package test.core.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import junit.framework.Test;
import junit.framework.TestCase;
import org.ibex.js.Fountain;
import org.ibex.js.JSTestUtil;
import test.core.CoreTestCase;
import test.core.CoreTestSuite;
import test.js.exec.JSTestCase;
import test.js.exec.JSTestSuite;
import testdeployment.NanoHTTPD;

/**
 * @author mike
 */
public class TestDownload extends CoreTestSuite {

    public TestDownload() {
        super(TestDownload.class);
    }

    public static Test suite() {
        return JSTestSuite.suite(new TestDownload());
    }

    public static void main(String[] args) throws Throwable {
        CoreTestSuite cts = new TestDownload();
        TestCase t = cts.createTestCase(cts.getResourceDirs(), "splashtest.t");
        t.runBare();
    }

    public JSTestCase createTestCase(Fountain path, String fileName) {
        return new CoreTestCase(path, fileName) {

            protected void setUp() throws Exception {
                createDotVexi(JSTestUtil.getResourceFile(NanoHTTPD.class, ".txt"));
                NanoHTTPD.start(7070, Integer.MAX_VALUE);
            }

            protected void tearDown() throws Exception {
                NanoHTTPD.stop();
            }
        };
    }

    private static void createDotVexi(File root) throws IOException {
        File dotVexi = new File(root, "example.vexi");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(dotVexi));
        String[] filenames = new String[] { "gcd.t", "impl" };
        zipRecursively(out, root, "", filenames);
        out.close();
    }

    private static void zipRecursively(ZipOutputStream out, File dir, String zippath, String[] filenames) {
        byte[] buf = new byte[1024];
        try {
            for (int i = 0; i < filenames.length; i++) {
                File f = new File(dir, filenames[i]);
                if (f.isDirectory()) {
                    String[] subfiles = f.list();
                    zipRecursively(out, f, zippath + f.getName() + "/", subfiles);
                } else {
                    FileInputStream in = new FileInputStream(new File(dir, filenames[i]));
                    String entrypath = zippath + filenames[i];
                    out.putNextEntry(new ZipEntry(entrypath));
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.closeEntry();
                    in.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
