package org.geonetwork.domain.ebrim.test.informationmodel.core;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.XMLTestCase;
import org.geonetwork.domain.ebrim.informationmodel.core.Slot;
import org.geonetwork.domain.ebrim.test.utilities.core.SlotFactory;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Tests marshalling and unmarshalling.
 * 
 * @author heikki doeleman
 * 
 */
public class SlotTest extends XMLTestCase {

    public SlotTest(String name) {
        super(name);
    }

    /**
	 * Tests unmarshalling (XML is instantiated as Java object) for Slot.
	 * 
	 * @throws FileNotFoundException
	 * @throws JiBXException
	 */
    public void testUnmarshal() throws FileNotFoundException, JiBXException {
        FileInputStream fis = new FileInputStream(new File("src/test/resources/SlotTestData.xml"));
        IBindingFactory bfact = BindingDirectory.getFactory(Slot.class);
        IUnmarshallingContext unMarshallingContext = bfact.createUnmarshallingContext();
        Slot unMarshallingResult = (Slot) unMarshallingContext.unmarshalDocument(fis, "UTF-8");
        Slot expectedResult = SlotFactory.create();
        assertEquals("Unmarshalling Slot", expectedResult, unMarshallingResult);
    }

    /**
	 * Tests unmarshalling (XML is instantiated as Java object) for Slot. This test uses testdata
	 * where Slot has a ValueList containing AnyValues as defined in the Basic Extension package.
	 * 
	 * @throws FileNotFoundException
	 * @throws JiBXException
	 */
    public void testUnmarshal2() throws FileNotFoundException, JiBXException {
        FileInputStream fis = new FileInputStream(new File("src/test/resources/SlotTestData2.xml"));
        IBindingFactory bfact = BindingDirectory.getFactory(Slot.class);
        IUnmarshallingContext unMarshallingContext = bfact.createUnmarshallingContext();
        Slot unMarshallingResult = (Slot) unMarshallingContext.unmarshalDocument(fis, "UTF-8");
        Slot expectedResult = SlotFactory.create2();
        assertEquals("Unmarshalling Slot", expectedResult, unMarshallingResult);
    }

    /**
	 * Tests marshalling (Java object puts out XML) a Slot.
	 * 
	 * @throws JiBXException
	 * @throws SAXException
	 * @throws IOException
	 */
    public void testMarshal2() throws JiBXException, SAXException, IOException {
        Slot o = SlotFactory.create2();
        IBindingFactory bfact = BindingDirectory.getFactory(Slot.class);
        IMarshallingContext marshallingContext = bfact.createMarshallingContext();
        Writer outConsole = new BufferedWriter(new OutputStreamWriter(System.out));
        marshallingContext.setOutput(outConsole);
        marshallingContext.setIndent(3);
        marshallingContext.marshalDocument(o, "UTF-8", null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer out = new BufferedWriter(new OutputStreamWriter(outputStream));
        marshallingContext.setIndent(3);
        marshallingContext.setOutput(out);
        marshallingContext.marshalDocument(o, "UTF-8", null);
        InputSource marshallingResult = new InputSource(new ByteArrayInputStream(outputStream.toByteArray()));
        FileInputStream fis = new FileInputStream(new File("src/test/resources/SlotTestData2.xml"));
        InputSource expectedResult = new InputSource(fis);
        DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        Diff diff = new Diff(expectedResult, marshallingResult);
        diff.overrideDifferenceListener(differenceListener);
        assertTrue("Marshalled Slot matches expected XML " + diff, diff.similar());
    }

    /**
	 * Tests marshalling (Java object puts out XML) a Slot.
	 * 
	 * @throws JiBXException
	 * @throws SAXException
	 * @throws IOException
	 */
    public void testMarshal() throws JiBXException, SAXException, IOException {
        Slot o = SlotFactory.create();
        IBindingFactory bfact = BindingDirectory.getFactory(Slot.class);
        IMarshallingContext marshallingContext = bfact.createMarshallingContext();
        Writer outConsole = new BufferedWriter(new OutputStreamWriter(System.out));
        marshallingContext.setOutput(outConsole);
        marshallingContext.setIndent(3);
        marshallingContext.marshalDocument(o, "UTF-8", null);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer out = new BufferedWriter(new OutputStreamWriter(outputStream));
        marshallingContext.setIndent(3);
        marshallingContext.setOutput(out);
        marshallingContext.marshalDocument(o, "UTF-8", null);
        InputSource marshallingResult = new InputSource(new ByteArrayInputStream(outputStream.toByteArray()));
        FileInputStream fis = new FileInputStream(new File("src/test/resources/SlotTestData.xml"));
        InputSource expectedResult = new InputSource(fis);
        DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        Diff diff = new Diff(expectedResult, marshallingResult);
        diff.overrideDifferenceListener(differenceListener);
        assertTrue("Marshalled Slot matches expected XML " + diff, diff.similar());
    }
}
