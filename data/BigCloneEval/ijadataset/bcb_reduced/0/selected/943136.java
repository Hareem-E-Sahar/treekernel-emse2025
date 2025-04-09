package org.geonetwork.domain.csw202.discovery;

import junit.framework.TestCase;
import java.io.*;
import org.jibx.runtime.*;
import org.geonetwork.domain.ebrim.test.utilities.csw202.discovery.GetCapabilitiesFactory;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.Diff;

public class GetCapabilitiesTest extends TestCase {

    @Test
    public void xtestUnmarshal() throws FileNotFoundException, JiBXException {
        FileInputStream fis = new FileInputStream(new File("src/test/resources/csw202-discovery/GetCapabilitiesTestData.xml"));
        IBindingFactory bfact = BindingDirectory.getFactory(GetCapabilities.class);
        IUnmarshallingContext unMarshallingContext = bfact.createUnmarshallingContext();
        GetCapabilities unMarshallingResult = (GetCapabilities) unMarshallingContext.unmarshalDocument(fis, "UTF-8");
        System.out.println("actual: " + unMarshallingResult.toString());
        GetCapabilities expectedResult = GetCapabilitiesFactory.create();
        System.out.println("expect: " + expectedResult.toString());
        assertEquals("Unmarshalling GetCapabilities", expectedResult, unMarshallingResult);
    }

    @Test
    public void testMarshal() throws JiBXException, SAXException, IOException {
        GetCapabilities o = GetCapabilitiesFactory.create();
        IBindingFactory bfact = BindingDirectory.getFactory(GetCapabilities.class);
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
        FileInputStream fis = new FileInputStream(new File("src/test/resources/csw202-discovery/GetCapabilitiesTestData.xml"));
        InputSource expectedResult = new InputSource(fis);
        DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        Diff diff = new Diff(expectedResult, marshallingResult);
        diff.overrideDifferenceListener(differenceListener);
        assertTrue("Marshalled Capabilities matches expected XML " + diff, diff.similar());
    }
}
