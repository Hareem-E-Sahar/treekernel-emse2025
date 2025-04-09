package edu.isi.misd.image.gateway.tile.test;

import static org.junit.Assert.fail;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import edu.isi.misd.image.gateway.conversion.tile.JpegTileWriterImplementation;
import edu.isi.misd.image.gateway.conversion.tile.TileNamingConvention;
import edu.isi.misd.image.gateway.conversion.tile.ZoomLevelConfigurationImplementation;
import edu.isi.misd.image.gateway.conversion.zoomify.ZoomifyTileNamingConvention;

public class JpegTileWriterImplementationTest {

    private String testDir;

    private static final String testFile = JpegTileWriterImplementationTest.class.getResource("/test.jpg").getPath();

    private JpegTileWriterImplementation writer;

    private TileNamingConvention namingConvention;

    @Before
    public void setUp() {
        final File d = new File("test_dir");
        d.mkdir();
        d.setWritable(true);
        testDir = d.getAbsolutePath();
        namingConvention = new ZoomifyTileNamingConvention();
        writer = new JpegTileWriterImplementation(testDir, namingConvention, new ZoomLevelConfigurationImplementation(256, 256, 256, 2.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_null1stArg() {
        new JpegTileWriterImplementation(null, namingConvention, new ZoomLevelConfigurationImplementation(256, 256, 256, 2.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_null2ndArg() {
        new JpegTileWriterImplementation(testDir, null, new ZoomLevelConfigurationImplementation(256, 256, 256, 2.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_null3rdArg() {
        new JpegTileWriterImplementation(testDir, namingConvention, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_badDir() {
        new JpegTileWriterImplementation("/notadir", namingConvention, new ZoomLevelConfigurationImplementation(256, 256, 256, 2.0));
    }

    @Test
    public void testGetExtension() {
        if (writer.getExtension() != "jpg") {
            fail("getExtension() returned " + writer.getExtension() + ", expected jpg.");
        }
    }

    @Test
    public void testGetTileNamingConvention() {
        if (writer.getTileNamingConvention() != namingConvention) {
            fail("getTileNamingConvention() returned an unexpected object.");
        }
    }

    @Test
    public void testGetDestinationDirectory() {
        if (!writer.getDestinationDirectory().equals(testDir)) {
            fail("getDestinationDirectory() returned " + writer.getDestinationDirectory() + ", expected " + testDir);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrite_nullImage() throws Exception {
        writer.write(null, 0, 0, 0);
    }

    @Test
    public void testWrite() throws Exception {
        final BufferedImage image = ImageIO.read(new File(testFile));
        final String tileName = writer.write(image, 0, 0, 0);
        final File newFile = new File(tileName);
        if (newFile.exists()) {
            final BufferedImage newImage = ImageIO.read(newFile);
            if (newImage.getWidth() != image.getWidth() || newImage.getHeight() != image.getHeight()) {
                fail("write() wrote an image of size " + newImage.getWidth() + "x" + newImage.getHeight() + ", expected " + image.getWidth() + "x" + image.getHeight());
                newFile.delete();
            }
        } else {
            fail("write() did not create the file " + tileName);
        }
    }

    @After
    public void tearDown() {
        removeDir(new File(testDir));
    }

    private void removeDir(final File dir) {
        final File[] files = dir.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (f.isDirectory()) {
                    removeDir(f);
                } else {
                    f.delete();
                }
            }
        }
        dir.delete();
    }
}
