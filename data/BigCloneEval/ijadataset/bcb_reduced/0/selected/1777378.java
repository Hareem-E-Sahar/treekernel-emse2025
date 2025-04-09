package org.geonetwork.gaap.domain.operation;

import junit.framework.TestCase;
import org.junit.Test;
import org.jibx.runtime.*;
import org.geonetwork.gaap.domain.util.MetadataPermissionsFactory;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.IgnoreTextAndAttributeValuesDifferenceListener;
import org.custommonkey.xmlunit.Diff;
import java.io.*;

/**
 * Test class for MetadataPermissions Jibx binding
 *
 * @author Jose
 */
public class MetadataPermissionsTest extends TestCase {

    @Test
    public void xtestUnmarshall() throws FileNotFoundException, JiBXException {
        FileInputStream fis = new FileInputStream(new File("src/test/resources/MetadataPermissionsTestData.xml"));
        IBindingFactory bfact = BindingDirectory.getFactory(MetadataPermissions.class);
        IUnmarshallingContext unMarshallingContext = bfact.createUnmarshallingContext();
        MetadataPermissions unMarshallingResult = (MetadataPermissions) unMarshallingContext.unmarshalDocument(fis, "UTF-8");
        MetadataPermissions expectedResult = MetadataPermissionsFactory.create();
        assertEquals("Unmarshalling MetadataPermissions", expectedResult, unMarshallingResult);
    }

    @Test
    public void testMarshall() throws JiBXException, SAXException, IOException {
        MetadataPermissions o = MetadataPermissionsFactory.create();
        IBindingFactory bfact = BindingDirectory.getFactory(MetadataPermissions.class);
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
        FileInputStream fis = new FileInputStream(new File("src/test/resources/MetadataPermissionsTestData.xml"));
        InputSource expectedResult = new InputSource(fis);
        DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        Diff diff = new Diff(expectedResult, marshallingResult);
        diff.overrideDifferenceListener(differenceListener);
        assertTrue("Marshalled MetadataPermissions matches expected XML " + diff, diff.similar());
    }
}
