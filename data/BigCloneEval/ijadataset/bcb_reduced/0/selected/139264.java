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
import org.geonetwork.domain.ebrim.informationmodel.core.Identifiable;
import org.geonetwork.domain.ebrim.informationmodel.core.RegistryObject;
import org.geonetwork.domain.ebrim.informationmodel.core.RegistryObjectList;
import org.geonetwork.domain.ebrim.test.utilities.core.RegistryObjectFactory;
import org.geonetwork.domain.ebrim.test.utilities.core.RegistryObjectListFactory;
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
public class RegistryObjectListTest extends XMLTestCase {

    public RegistryObjectListTest(String name) {
        super(name);
    }

    /**
	 * Tests unmarshalling (XML is instantiated as Java object) for RegistryObjectList.
	 * 
	 * @throws FileNotFoundException
	 * @throws JiBXException
	 */
    public void testUnmarshal() throws FileNotFoundException, JiBXException {
        FileInputStream fis = new FileInputStream(new File("src/test/resources/RegistryObjectListTestData.xml"));
        IBindingFactory bfact = BindingDirectory.getFactory(RegistryObjectList.class);
        IUnmarshallingContext unMarshallingContext = bfact.createUnmarshallingContext();
        RegistryObjectList unMarshallingResult = (RegistryObjectList) unMarshallingContext.unmarshalDocument(fis, "UTF-8");
        RegistryObject expectedResult = RegistryObjectFactory.create();
        for (Identifiable identifiable : unMarshallingResult.getRegistryObjects()) {
            assertEquals("Unmarshalling RegistryObjectList", expectedResult, identifiable);
        }
    }

    /**
	 * Tests marshalling (Java object puts out XML) a RegistryObjectList.
	 * 
	 * @throws JiBXException
	 * @throws SAXException
	 * @throws IOException
	 */
    public void testMarshal() throws JiBXException, SAXException, IOException {
        RegistryObjectList o = RegistryObjectListFactory.create();
        IBindingFactory bfact = BindingDirectory.getFactory(RegistryObjectList.class);
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
        FileInputStream fis = new FileInputStream(new File("src/test/resources/RegistryObjectListTestData.xml"));
        InputSource expectedResult = new InputSource(fis);
        DifferenceListener differenceListener = new IgnoreTextAndAttributeValuesDifferenceListener();
        Diff diff = new Diff(expectedResult, marshallingResult);
        diff.overrideDifferenceListener(differenceListener);
        assertTrue("Marshalled RegistryObjectList matches expected XML " + diff, diff.similar());
    }
}
