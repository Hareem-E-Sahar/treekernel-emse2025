package org.apache.batik.dom;

import java.io.File;
import java.net.URL;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class tests the non-deep cloneNode method for elements.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id: CloneElementTest.java 475477 2006-11-15 22:44:28Z cam $
 */
public class CloneElementTest extends AbstractTest {

    protected String testFileName;

    protected String rootTag;

    protected String targetId;

    public CloneElementTest(String file, String root, String id) {
        testFileName = file;
        rootTag = root;
        targetId = id;
    }

    public TestReport runImpl() throws Exception {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXDocumentFactory df = new SAXDocumentFactory(GenericDOMImplementation.getDOMImplementation(), parser);
        File f = (new File(testFileName));
        URL url = f.toURL();
        Document doc = df.createDocument(null, rootTag, url.toString(), url.openStream());
        Element e = doc.getElementById(targetId);
        if (e == null) {
            DefaultTestReport report = new DefaultTestReport(this);
            report.setErrorCode("error.get.element.by.id.failed");
            report.addDescriptionEntry("entry.key.id", targetId);
            report.setPassed(false);
            return report;
        }
        Element celt = (Element) e.cloneNode(false);
        NamedNodeMap attrs = e.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String ns = attr.getNamespaceURI();
            String name = (ns == null) ? attr.getNodeName() : attr.getLocalName();
            String val = attr.getNodeValue();
            String val2 = celt.getAttributeNS(ns, name);
            if (!val.equals(val2)) {
                DefaultTestReport report = new DefaultTestReport(this);
                report.setErrorCode("error.attr.comparison.failed");
                report.addDescriptionEntry("entry.attr.name", name);
                report.addDescriptionEntry("entry.attr.value1", val);
                report.addDescriptionEntry("entry.attr.value2", val2);
                report.setPassed(false);
                return report;
            }
        }
        return reportSuccess();
    }
}
