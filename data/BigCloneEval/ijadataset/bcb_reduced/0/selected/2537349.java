package org.magnos.asset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.magnos.asset.audio.AudioFormat;
import org.magnos.asset.audio.MidiFormat;
import org.magnos.asset.csv.CsvFormat;
import org.magnos.asset.dat.DatFormat;
import org.magnos.asset.font.FontFormat;
import org.magnos.asset.image.GifFormat;
import org.magnos.asset.image.ImageFormat;
import org.magnos.asset.java.ClassFormat;
import org.magnos.asset.java.JarFormat;
import org.magnos.asset.props.PropertyFormat;
import org.magnos.asset.text.TextFormat;
import org.magnos.asset.xml.XmlFormat;
import org.magnos.asset.zip.GzipFormat;
import org.magnos.asset.zip.ZipFormat;

/**
 * A simple utility class.
 * 
 * @author pdiffenderfer
 *
 */
public class FormatUtility {

    /**
	 * Drains the given InputStream of all data and returns an OutputStream
	 * which contains all of that data.
	 * 
	 * @param input
	 * 		The InputStream to drain of all data.
	 * @return
	 * 		The OutputStream containing all of the data drained.
	 * @throws IOException
	 * 		An error occurred reading from the given InputStream.
	 */
    public static ByteArrayOutputStream getOutput(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.available());
        int read = 0;
        byte[] data = new byte[4096];
        while ((read = input.read(data)) > 0) {
            output.write(data, 0, read);
        }
        output.flush();
        return output;
    }

    /**
	 * Drains the given InputStream of all data and returns a byte array which
	 * contains all of that data.
	 * 
	 * @param input
	 * 		The InputStream to drain of all data.
	 * @return
	 * 		The byte array containing all of the data.
	 * @throws IOException
	 * 		An error occurred reading from the given InputStream.
	 */
    public static byte[] getBytes(InputStream input) throws IOException {
        return getOutput(input).toByteArray();
    }

    /**
	 * Loads all formats in this project into the Assets class.
	 */
    public static void loadAll() {
        AssetFormat[] formats = { new AudioFormat(), new ClassFormat(), new CsvFormat(), new DatFormat(), new FontFormat(), new GifFormat(), new GzipFormat(), new ImageFormat(), new JarFormat(), new MidiFormat(), new PropertyFormat(), new TextFormat(), new XmlFormat(), new ZipFormat() };
        Assets.addFormats(formats);
    }
}
