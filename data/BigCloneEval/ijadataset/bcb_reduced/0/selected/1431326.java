package de.schlund.pfixxml;

import java.net.URL;
import java.util.Map;
import junit.framework.TestCase;
import org.xml.sax.InputSource;

public class IncludePartsInfoParserTest extends TestCase {

    public void testParts() throws Exception {
        URL url = getClass().getClassLoader().getResource("de/schlund/pfixxml/parts.xml");
        InputSource source = new InputSource(url.openStream());
        source.setSystemId(url.toString());
        IncludePartsInfo info = IncludePartsInfoParser.parse(source);
        Map<String, IncludePartInfo> parts = info.getParts();
        assertEquals(3, parts.size());
        assertTrue(parts.containsKey("aaa"));
        assertTrue(parts.containsKey("bbb"));
        assertFalse(parts.containsKey("ccc"));
        assertTrue(parts.containsKey("ddd"));
    }
}
