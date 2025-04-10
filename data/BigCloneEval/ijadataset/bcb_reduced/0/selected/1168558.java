package org.joda.time.tz;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.joda.time.DateTimeZone;

/**
 * Test cases for ZoneInfoCompiler.
 *
 * @author Brian S O'Neill
 */
public class TestCompiler extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static TestSuite suite() {
        return new TestSuite(TestCompiler.class);
    }

    static final String AMERICA_LOS_ANGELES_FILE = "# Rules for building just America/Los_Angeles time zone.\n" + "\n" + "Rule    US  1918    1919    -   Mar lastSun 2:00    1:00    D\n" + "Rule    US  1918    1919    -   Oct lastSun 2:00    0   S\n" + "Rule    US  1942    only    -   Feb 9   2:00    1:00    W # War\n" + "Rule    US  1945    only    -   Aug 14  23:00u  1:00    P # Peace\n" + "Rule    US  1945    only    -   Sep 30  2:00    0   S\n" + "Rule    US  1967    max -   Oct lastSun 2:00    0   S\n" + "Rule    US  1967    1973    -   Apr lastSun 2:00    1:00    D\n" + "Rule    US  1974    only    -   Jan 6   2:00    1:00    D\n" + "Rule    US  1975    only    -   Feb 23  2:00    1:00    D\n" + "Rule    US  1976    1986    -   Apr lastSun 2:00    1:00    D\n" + "Rule    US  1987    max -   Apr Sun>=1  2:00    1:00    D\n" + "\n" + "Rule    CA  1948    only    -   Mar 14  2:00    1:00    D\n" + "Rule    CA  1949    only    -   Jan  1  2:00    0   S\n" + "Rule    CA  1950    1966    -   Apr lastSun 2:00    1:00    D\n" + "Rule    CA  1950    1961    -   Sep lastSun 2:00    0   S\n" + "Rule    CA  1962    1966    -   Oct lastSun 2:00    0   S\n" + "\n" + "Zone America/Los_Angeles -7:52:58 - LMT 1883 Nov 18 12:00\n" + "            -8:00   US  P%sT    1946\n" + "            -8:00   CA  P%sT    1967\n" + "            -8:00   US  P%sT";

    private DateTimeZone originalDateTimeZone = null;

    public TestCompiler(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        originalDateTimeZone = DateTimeZone.getDefault();
        DateTimeZone.setDefault(DateTimeZone.UTC);
    }

    protected void tearDown() throws Exception {
        DateTimeZone.setDefault(originalDateTimeZone);
    }

    public void testCompile() throws Exception {
        Provider provider = compileAndLoad(AMERICA_LOS_ANGELES_FILE);
        DateTimeZone tz = provider.getZone("America/Los_Angeles");
        assertEquals("America/Los_Angeles", tz.getID());
        assertEquals(false, tz.isFixed());
        TestBuilder.testForwardTransitions(tz, TestBuilder.AMERICA_LOS_ANGELES_DATA);
        TestBuilder.testReverseTransitions(tz, TestBuilder.AMERICA_LOS_ANGELES_DATA);
    }

    private Provider compileAndLoad(String data) throws Exception {
        File tempDir = createDataFile(data);
        File destDir = makeTempDir();
        ZoneInfoCompiler.main(new String[] { "-src", tempDir.getAbsolutePath(), "-dst", destDir.getAbsolutePath(), "tzdata" });
        deleteOnExit(destDir);
        return new ZoneInfoProvider(destDir);
    }

    private File createDataFile(String data) throws IOException {
        File tempDir = makeTempDir();
        File tempFile = new File(tempDir, "tzdata");
        tempFile.deleteOnExit();
        InputStream in = new ByteArrayInputStream(data.getBytes("UTF-8"));
        FileOutputStream out = new FileOutputStream(tempFile);
        byte[] buf = new byte[1000];
        int amt;
        while ((amt = in.read(buf)) > 0) {
            out.write(buf, 0, amt);
        }
        out.close();
        in.close();
        return tempDir;
    }

    private File makeTempDir() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        tempDir = new File(tempDir, "joda-test-" + (new java.util.Random().nextInt() & 0xffffff));
        tempDir.mkdirs();
        tempDir.deleteOnExit();
        return tempDir;
    }

    private void deleteOnExit(File tempFile) {
        tempFile.deleteOnExit();
        if (tempFile.isDirectory()) {
            File[] files = tempFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteOnExit(files[i]);
            }
        }
    }
}
