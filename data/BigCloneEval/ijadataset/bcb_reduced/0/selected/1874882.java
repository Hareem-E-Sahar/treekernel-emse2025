package tilemaster.io;

import itemizer.item.ItemConfiguration;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import tilemaster.tile.TileDescriptor;
import tilemaster.tile.TileSet;

/**
 * Tile zipfile IO. Can read and write tile sets as zip archives.
 *
 * @author Hj. Malthaner
 */
public class TicaZipIo implements FileTypeIO {

    static {
        IOPluginBroker.registerHandler(".tica.zip", new TicaZipIo());
    }

    public static final String CATALOG_FILE = "catalog.tica";

    public static final String CATALOG_XML = "catalog.xml";

    /**
     * Reads images from a tile set zipfile.
     *
     * @param filename The name of the file to read.
     * @return The created tile set.
     * @throws IOException In case of IO errors.
     */
    public TileSet read(String filename) throws IOException {
        ZipFile zipFile = new ZipFile(filename);
        ZipEntry catalog = zipFile.getEntry(CATALOG_FILE);
        InputStream in = zipFile.getInputStream(catalog);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        TileSet result = new TileSet(new ItemConfiguration(), 0);
        result.read(reader);
        in.close();
        Enumeration entries = zipFile.entries();
        String pattern = ".*\\.png";
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            if (entry.getName().matches(pattern)) {
                InputStream pngIn = zipFile.getInputStream(entry);
                BufferedImage img = ImageIO.read(pngIn);
                pngIn.close();
                String[] parts = entry.getName().split(",");
                String indexString = parts[0];
                int dotPos = indexString.indexOf('.');
                if (dotPos >= 0) {
                    indexString = indexString.substring(0, dotPos);
                }
                int index = Integer.parseInt(indexString);
                result.get(index).img = img;
            }
        }
        zipFile.close();
        return result;
    }

    /**
     * Writes images to a tile set zipfile.
     *
     * @param filename The name of the file to read.
     * @param tileSet The tiles to write.
     * @throws IOException In case of IO errors.
     */
    public void write(String filename, TileSet tileSet) throws IOException {
        File file = new File(filename);
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
        ZipEntry catalog = new ZipEntry(CATALOG_FILE);
        out.putNextEntry(catalog);
        OutputStreamWriter writer = new OutputStreamWriter(out);
        tileSet.write(writer);
        writer.flush();
        out.closeEntry();
        ZipEntry xmlCatalog = new ZipEntry(CATALOG_XML);
        out.putNextEntry(xmlCatalog);
        OutputStreamWriter xmlWriter = new OutputStreamWriter(out, "UTF-8");
        tileSet.writeXML(xmlWriter);
        xmlWriter.flush();
        out.closeEntry();
        final String format = "PNG";
        for (int i = 0; i < tileSet.size(); i++) {
            final TileDescriptor tld = tileSet.get(i);
            final String name = tld.getString(0) != null ? tld.getString(0) : "";
            final String pngfile = "" + i + "," + tld.tileId + "," + name + ".png";
            ZipEntry pngzip = new ZipEntry(pngfile);
            out.putNextEntry(pngzip);
            ImageIO.write(tld.img, format, out);
            out.closeEntry();
        }
        out.close();
    }

    /** Creates a new instance of TicaZipIo */
    private TicaZipIo() {
    }
}
