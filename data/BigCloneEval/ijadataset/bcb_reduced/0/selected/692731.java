package de.huxhorn.lilith.data.eventsource.xml;

import de.huxhorn.lilith.data.eventsource.SourceIdentifier;
import de.huxhorn.lilith.data.eventsource.SourceInfo;
import de.huxhorn.sulky.stax.IndentingXMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class SourceInfoIOTest {

    private final Logger logger = LoggerFactory.getLogger(SourceInfoIOTest.class);

    private SourceInfoWriter sourceInfoWriter;

    private SourceInfoReader sourceInfoReader;

    @Before
    public void setUp() {
        sourceInfoWriter = new SourceInfoWriter();
        sourceInfoWriter.setWritingSchemaLocation(true);
        sourceInfoReader = new SourceInfoReader();
    }

    @Test
    public void minimal() throws XMLStreamException, UnsupportedEncodingException {
        SourceInfo obj = createMinimalSourceInfo();
        check(obj, true);
    }

    @Test
    public void full() throws XMLStreamException, UnsupportedEncodingException {
        SourceInfo obj = createMinimalSourceInfo();
        obj.setActive(true);
        check(obj, true);
    }

    @Test
    public void fullPrefix() throws XMLStreamException, UnsupportedEncodingException {
        sourceInfoWriter.setPreferredPrefix("foo");
        SourceInfo obj = createMinimalSourceInfo();
        obj.setActive(true);
        check(obj, true);
    }

    public SourceInfo createMinimalSourceInfo() {
        SourceInfo result = new SourceInfo();
        result.setNumberOfEvents(17);
        result.setOldestEventTimestamp(new Date());
        result.setSource(createMinimalEventSource());
        return result;
    }

    public SourceIdentifier createMinimalEventSource() {
        SourceIdentifier result = new SourceIdentifier();
        result.setIdentifier("primary");
        return result;
    }

    public void check(SourceInfo original, boolean indent) throws UnsupportedEncodingException, XMLStreamException {
        if (logger.isDebugEnabled()) logger.debug("Processing:\n{}", original);
        byte[] bytes = write(original, indent);
        String originalStr = new String(bytes, "UTF-8");
        if (logger.isDebugEnabled()) logger.debug("Marshalled to:\n{}", originalStr);
        SourceInfo read = read(bytes);
        if (logger.isDebugEnabled()) logger.debug("Read.");
        assertEquals(original, read);
        if (logger.isDebugEnabled()) logger.debug("Equal.");
        bytes = write(read, indent);
        String readStr = new String(bytes, "UTF-8");
        assertEquals(originalStr, readStr);
        if (logger.isDebugEnabled()) logger.debug("Strings equal.");
    }

    public byte[] write(SourceInfo source, boolean indent) throws XMLStreamException, UnsupportedEncodingException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(new OutputStreamWriter(out, "utf-8"));
        if (indent && writer.getClass().getName().equals("com.bea.xml.stream.XMLWriterBase")) {
            if (logger.isInfoEnabled()) logger.info("Won't indent because of http://jira.codehaus.org/browse/STAX-42");
            indent = false;
        }
        if (indent) {
            writer = new IndentingXMLStreamWriter(writer);
        }
        sourceInfoWriter.write(writer, source, true);
        writer.flush();
        return out.toByteArray();
    }

    public SourceInfo read(byte[] bytes) throws XMLStreamException, UnsupportedEncodingException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(new InputStreamReader(in, "utf-8"));
        return sourceInfoReader.read(reader);
    }
}
