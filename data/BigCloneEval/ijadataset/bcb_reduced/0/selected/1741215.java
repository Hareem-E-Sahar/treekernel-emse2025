package de.simplydevelop.mexs.domain;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import junit.framework.Assert;
import org.eclipse.persistence.tools.file.FileUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import com.sun.xml.xsom.XSSchemaSet;

public class XMLMTF_SchemaReaderBeanTest {

    private static XMLMTF_SchemaReaderBean testCandidate = new XMLMTF_SchemaReaderBean();

    private static File fields = new File("C:/projects/others/jsf/FFI-APP-11(Ch2)/ffi/fields.xsd");

    private static File sets = new File("C:/projects/others/jsf/FFI-APP-11(Ch2)/ffi/sets.xsd");

    private static File messages = new File("C:/projects/others/jsf/FFI-APP-11(Ch2)/ffi/messages.xsd");

    private static File composites = new File("C:/projects/others/jsf/FFI-APP-11(Ch2)/ffi/composites.xsd");

    private static XSSchemaSet schema;

    @BeforeClass
    public static void generateTestData() throws Exception {
        FileInputStream messagesStream = new FileInputStream(messages);
        FileInputStream setsStream = new FileInputStream(sets);
        FileInputStream compositesStream = new FileInputStream(composites);
        FileInputStream fieldsStream = new FileInputStream(fields);
        ByteArrayOutputStream messageOutStream = new ByteArrayOutputStream();
        ByteArrayOutputStream setsOutStream = new ByteArrayOutputStream();
        ByteArrayOutputStream compositeOutStream = new ByteArrayOutputStream();
        ByteArrayOutputStream fieldsOutStream = new ByteArrayOutputStream();
        FileUtil.copy(messagesStream, messageOutStream);
        FileUtil.copy(setsStream, setsOutStream);
        FileUtil.copy(compositesStream, compositeOutStream);
        FileUtil.copy(fieldsStream, fieldsOutStream);
        schema = testCandidate.readSchema(messageOutStream.toByteArray(), setsOutStream.toByteArray(), compositeOutStream.toByteArray(), fieldsOutStream.toByteArray());
    }

    @Test
    public void readingXMLMTFMessageSchemaFileShouldResultIn6Schemas() {
        Assert.assertEquals(6, schema.getSchemaSize());
    }

    @Test
    public void readingXMLMTFMessageSchemaFileShouldHaveFieldsScheamReaded() {
        Assert.assertNotNull(schema.getElementDecl("urn:int:nato:mtf:app-11(c):change02:ffi", "FriendlyForceInformation"));
    }
}
