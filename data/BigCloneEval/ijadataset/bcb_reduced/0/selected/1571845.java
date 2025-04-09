package edu.isi.misd.image.gateway.conversion.zoomify.test;

import static org.junit.Assert.fail;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import edu.isi.misd.image.gateway.conversion.ImageSource;
import edu.isi.misd.image.gateway.conversion.loci.LociImageSource;
import edu.isi.misd.image.gateway.conversion.tile.ZoomLevelConfigurationImplementation;
import edu.isi.misd.image.gateway.conversion.zoomify.ZoomifyTileDirectoryExport;
import edu.isi.misd.image.gateway.conversion.zoomify.ZoomifyTileNamingConvention;

public class ZoomifyTileDirectoryExportTest {

    private String testDir;

    private static final String testFile = ZoomifyTileDirectoryExportTest.class.getResource("/big.jpg").getPath();

    private ZoomifyTileDirectoryExport export;

    private ImageSource imageSource;

    @Before
    public void setUp() throws Exception {
        final File d = new File("test_dir");
        d.setWritable(true);
        d.mkdir();
        testDir = d.getAbsolutePath();
        imageSource = new LociImageSource(testFile);
        export = new ZoomifyTileDirectoryExport(imageSource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_null1stArg() {
        new ZoomifyTileDirectoryExport(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExport_nullDir() throws Exception {
        export.export(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExport_nonExistantDir() throws Exception {
        export.export("/notadir");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetThumbnailFilename_nullArg() {
        export.getThumbnailFilename(null);
    }

    @Test
    public void testExport() throws Exception {
        final BufferedImage orig = ImageIO.read(new File(testFile));
        final ZoomLevelConfigurationImplementation config = new ZoomLevelConfigurationImplementation(orig.getWidth(), orig.getHeight(), export.getTileSize(), export.getZoomScale());
        final ZoomifyTileNamingConvention namingConvention = new ZoomifyTileNamingConvention();
        export.export(testDir);
        final long numberOfTiles = config.getTotalNumberOfTiles();
        final long numberOfTileGroups = numberOfTiles / 256;
        File tileGroupDir;
        for (int i = 0; i < numberOfTileGroups; i++) {
            tileGroupDir = new File(testDir + File.separator + "TileGroup" + i);
            if (!tileGroupDir.exists() || !tileGroupDir.isDirectory()) {
                fail("Expected TileGroup" + i + " directory doesn't exist.");
            }
        }
        File tileFile;
        for (int l = config.getNumberOfZoomLevels() - 1; l >= 0; l--) {
            for (int r = 0; r < config.getNumberOfRows(l); r++) {
                for (int c = 0; c < config.getNumberOfColumns(l); c++) {
                    tileFile = new File(testDir + File.separator + namingConvention.getTileFileName(l, r, c, "jpg", config));
                    if (!tileFile.exists() || !tileFile.isFile()) {
                        fail("Expected tile file " + tileFile.getAbsolutePath() + " doesn't exist.");
                    }
                }
            }
        }
        final File propFile = new File(testDir + File.separator + "ImageProperties.xml");
        if (!propFile.exists() || !propFile.isFile()) {
            fail("Expected metadata file " + propFile.getAbsolutePath() + " doesn't exist.");
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
