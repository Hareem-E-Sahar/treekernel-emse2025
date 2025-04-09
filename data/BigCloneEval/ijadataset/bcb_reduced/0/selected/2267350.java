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
import org.geonetwork.domain.ebrim.informationmodel.core.ExtrinsicObject;
import org.geonetwork.domain.ebrim.test.utilities.core.ExtrinsicObjectFactory;
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
public class ExtrinsicObjectTest extends XMLTestCase {

    public ExtrinsicObjectTest(String name) {
        super(name);
    }

    /**
	 * Tests unmarshalling (XML is instantiated as Java object) for ExtrinsicObject.
	 * 
	 * @throws FileNotFoundException
	 * @throws JiBXException
	 */
    public void testUnmarshal() throws FileNotFoundException, JiBXException {
        FileInputStream fis = new FileInputStream(new File("src/test/resources/ExtrinsicObjectTestData.xml"));
        IBindingFactory bfact = BindingDirectory.getFactory(ExtrinsicObject.class);
        IUnmarshallingContext unMarshallingContext = bfact.createUnmarshallingContext();
        ExtrinsicObject unMarshallingResult = (ExtrinsicObject) unMarshallingContext.unmarshalDocument(fis, "UTF-8");
        ExtrinsicObject expectedResult = ExtrinsicObjectFactory.create();
        assertEquals("Unmarshalling ExtrinsicObject", expectedResult, unMarshallingResult);
    }

    /**
	 * Tests marshalling (Java object puts out XML) a Slot.
	 * 
	 * @throws JiBXException
	 * @throws SAXException
	 * @throws IOException
	 */
    public void testMarshal() throws JiBXException, SAXException, IOException {
        ExtrinsicObject o = ExtrinsicObjectFactory.create();
        IBindingFactory bfact = BindingDirectory.getFactory(ExtrinsicObject.class);
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
        FileInputStream fis = new FileInputStream(new File("src/test/resources/ExtrinsicObjectTestData.xml"));
        InputSource expectedResult = new InputSource(fis);
        DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        Diff diff = new Diff(expectedResult, marshallingResult);
        diff.overrideDifferenceListener(differenceListener);
        assertTrue("Marshalled ExtrinsicObject matches expected XML " + diff, diff.similar());
    }
}
