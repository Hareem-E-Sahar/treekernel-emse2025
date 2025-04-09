package org.geonetwork.domain.csw202.discovery;

import org.jibx.runtime.*;
import org.geonetwork.domain.ebrim.test.utilities.csw202.discovery.GetDomainResponseFactory;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.Diff;
import java.io.*;
import junit.framework.TestCase;

public class GetDomainResponseTest extends TestCase {

    @Test
    public void testUnmarshal() throws FileNotFoundException, JiBXException {
        FileInputStream fis = new FileInputStream(new File("src/test/resources/csw202-discovery/GetDomainResponseTestData.xml"));
        IBindingFactory bfact = BindingDirectory.getFactory(GetDomainResponse.class);
        IUnmarshallingContext unMarshallingContext = bfact.createUnmarshallingContext();
        GetDomainResponse unMarshallingResult = (GetDomainResponse) unMarshallingContext.unmarshalDocument(fis, "UTF-8");
        System.out.println("actual: " + unMarshallingResult.toString());
        GetDomainResponse expectedResult = GetDomainResponseFactory.create();
        System.out.println("expect: " + expectedResult.toString());
        assertEquals("Unmarshalling GetDomainResponse", expectedResult, unMarshallingResult);
    }

    @Test
    public void testMarshal() throws JiBXException, SAXException, IOException {
        GetDomainResponse o = GetDomainResponseFactory.create();
        IBindingFactory bfact = BindingDirectory.getFactory(GetDomainResponse.class);
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
        FileInputStream fis = new FileInputStream(new File("src/test/resources/csw202-discovery/GetDomainResponseTestData.xml"));
        InputSource expectedResult = new InputSource(fis);
        DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        Diff diff = new Diff(expectedResult, marshallingResult);
        diff.overrideDifferenceListener(differenceListener);
        assertTrue("Marshalled GetDomainResponse matches expected XML " + diff, diff.similar());
    }
}
