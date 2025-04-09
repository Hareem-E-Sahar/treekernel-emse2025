package csimage.demo;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import csimage.TheMatrix;
import csimage.util.EasyInput;

/**
 * This program encodes TheMatrix image data in a zipped text file. Each pixel
 * is stored as a 32-bit RGBA value. A matching decode method recovers the
 * image. This encoding is not very space-efficient. A few tests show that it
 * results in files roughly 10 times larger than JPEG. It's meant only as an
 * example of Java file I/O.
 */
public class MatrixCodec4 {

    public static final String EXT = ".zip";

    public static void main(String[] args) {
        final String fname = "csimage/pictures/lake1.jpg";
        final String outname = "lake1_codec4";
        System.out.println("cwd = " + EasyInput.getcwd());
        TheMatrix m = new TheMatrix(fname);
        encode(m, outname);
        m = decode(outname);
        m.show();
    }

    /**
     * Creates a zipped text file representing the given matrix object. The
     * encoding method is simple. Each line of the output file consists of the
     * description of a single pixel's RGBA value stored as a single 32-bit int.
     * The height and width of the image are stored in the first line, and so we
     * don't need to store the (x, y) coordinates of the pixels. The resulting
     * file is very large --- this is only for educational purposes!
     * 
     * @param m
     *            TheMatrix object to encode
     * @param outFileName
     *            name of the output file to put the encoding in
     */
    public static void encode(TheMatrix m, String outFileName) {
        try {
            System.out.println("Compressing " + outFileName + " ...");
            ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(outFileName + EXT));
            zipout.setMethod(ZipOutputStream.DEFLATED);
            zipout.setLevel(9);
            ZipEntry entry = new ZipEntry(outFileName + ".txt");
            zipout.putNextEntry(entry);
            PrintWriter out = new PrintWriter(zipout);
            System.out.println("Writing data ...");
            out.println(m.getImageHeight() + " " + m.getImageWidth());
            for (int x = 0; x < m.getImageWidth(); ++x) {
                for (int y = 0; y < m.getImageHeight(); ++y) {
                    Color c = m.getColor(x, y);
                    out.println(c.getRGB());
                }
            }
            out.close();
            zipout.close();
            System.out.println("encoding done");
        } catch (IOException e) {
            throw new Error("Error: " + e);
        }
    }

    public static void decodeLine(TheMatrix m, String line, int x, int y) {
        int rgb = Integer.parseInt(line);
        m.setColor(x, y, new Color(rgb));
    }

    /**
     * Decodes a text file that has been encoded with the above encode method.
     * Warning: Does not check if the format of the file is correct!
     * 
     * @param fname
     *            name of the file to decode
     * @return TheMatrix object corresponding to the pixels described in the
     *         input file
     */
    public static TheMatrix decode(String fname) {
        try {
            System.out.println("Decoding " + fname + " ...");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fname + EXT));
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(bis));
            BufferedReader in = new BufferedReader(new InputStreamReader(zis));
            ZipEntry entry = zis.getNextEntry();
            System.out.println("Extracted: " + entry);
            String[] header = in.readLine().split(" ");
            int height = Integer.parseInt(header[0]);
            int width = Integer.parseInt(header[1]);
            TheMatrix m = new TheMatrix(width, height);
            System.out.println("decoding data ...");
            String line = in.readLine();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    decodeLine(m, line, x, y);
                    line = in.readLine();
                }
            }
            in.close();
            zis.close();
            System.out.println("decoding done");
            return m;
        } catch (IOException e) {
            throw new Error("Error: " + e);
        }
    }
}
