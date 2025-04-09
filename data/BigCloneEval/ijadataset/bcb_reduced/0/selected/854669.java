package org.geonetwork.domain.csw202.record;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import junit.framework.TestCase;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.geonetwork.domain.ebrim.test.utilities.csw202.record.BriefRecordFactory;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BriefRecordTest extends TestCase {

    @Test
    public void testUnmarshal() throws FileNotFoundException, JiBXException {
        FileInputStream fis = new FileInputStream(new File("src/test/resources/csw202-record/BriefRecordTestData.xml"));
        IBindingFactory bfact = BindingDirectory.getFactory(BriefRecord.class);
        IUnmarshallingContext unMarshallingContext = bfact.createUnmarshallingContext();
        BriefRecord unMarshallingResult = (BriefRecord) unMarshallingContext.unmarshalDocument(fis, "UTF-8");
        System.out.println("actual: " + unMarshallingResult.toString());
        BriefRecord expectedResult = BriefRecordFactory.create();
        System.out.println("expect: " + expectedResult.toString());
        assertEquals("Unmarshalling BriefRecord", expectedResult, unMarshallingResult);
    }

    @Test
    public void testMarshal() throws JiBXException, SAXException, IOException {
        BriefRecord o = BriefRecordFactory.create();
        IBindingFactory bfact = BindingDirectory.getFactory(BriefRecord.class);
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
        FileInputStream fis = new FileInputStream(new File("src/test/resources/csw202-record/BriefRecordTestData.xml"));
        InputSource expectedResult = new InputSource(fis);
        DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        Diff diff = new Diff(expectedResult, marshallingResult);
        diff.overrideDifferenceListener(differenceListener);
        assertTrue("Marshalled BriefRecord matches expected XML " + diff, diff.similar());
    }
}
