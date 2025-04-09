package net.sf.jdiskcatalog.analysis;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;
import net.sf.jdiskcatalog.api.Node;
import net.sf.jdiskcatalog.api.StreamAnalyser;

/**
 * Calculates CRC for the given binary content.
 *
 * @author Przemek Więch <pwiech@losthive.org>
 * @version $Id$
 */
public class CrcAnalyser implements StreamAnalyser {

    public static final String KEY_CRC = "crc";

    public Map<String, Object> analyse(InputStream stream, Node node) throws IOException {
        Map<String, Object> properties = new HashMap<String, Object>();
        CRC32 crc = new CRC32();
        int b = stream.read();
        while (b != -1) {
            crc.update(b);
            b = stream.read();
        }
        properties.put(KEY_CRC, crc.getValue());
        return properties;
    }
}
