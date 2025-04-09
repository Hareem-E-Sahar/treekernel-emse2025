package au.edu.usq.utfx.framework;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.Logger;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import sax.helpers.AttributesImpl;
import au.edu.usq.utfx.util.XsltTestValidator;

/**
 * This SAX handler parsers UTF-X test definition file and calls the
 * TestFilesuiteAssembler to build a File suite and test cases.
 * 
 * <p>
 * Copyright &copy; 2004 - <a href="http://www.usq.edu.au"> University of
 * Southern Queensland. </a>
 * </p>
 * 
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the <a href="http://www.gnu.org/licenses/gpl.txt">GNU General
 * Public License v2 </a> as published by the Free Software Foundation.
 * </p>
 * 
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * </p>
 * 
 * <code>
 * $Source: /cvsroot/utf-x/utf-x/src/java/au/edu/usq/utfx/framework/TestFileHandler.java,v $
 * </code>
 * 
 * @author Jacek Radajewski
 * @author Oliver Lucido
 * @author Sally MacFarlane
 * @version $Revision: 1.6 $ $Date: 2005/02/20 00:01:10 $ $Name:  $
 */
public class TestFileHandler extends DefaultHandler implements LexicalHandler {

    /** logger */
    private Logger log;

    /** Default parser name. */
    protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

    protected static String doctypeString = "<!DOCTYPE test-result PUBLIC " + "\"-//UTF-X//DTD utfx-tests 1.0//EN\" " + "\"/dtd/utfx_tests.dtd\">\n";

    /** URI pointing to the DTD */
    protected static String sourceDtdUri;

    /** Print writer where we write output. */
    private PrintWriter fOut;

    /** Byte array output stream where we store output */
    private ByteArrayOutputStream os;

    /** XSLT Transformer factory */
    private TransformerFactory tf;

    /** SAX Parser */
    protected XMLReader parser = null;

    /** The name of the current element */
    protected String currentElementName;

    /** Indicates whether to normalize characters */
    private boolean escapeXML;

    /** The stylesheet being tested */
    private String stylesheet;

    /** Buffer for the current test name */
    private StringBuffer testName;

    /** Indicates that the parser is currently in a &lt;data&gt; element */
    private boolean readData;

    /** Indicates that the parser is currently in a &lt;source&gt; element */
    private boolean readSource;

    /** Indicates that the parser is currently in a &lt;expected&gt; element */
    private boolean readExpected;

    /** Buffer for the source XML input */
    private StringBuffer source;

    /** Buffer for the expected output */
    private StringBuffer expected;

    /** Buffer for the failure message */
    private StringBuffer message;

    String dtdDir = "";

    /** test file suite assembler */
    private TestFileSuiteAssembler assembler;

    /** The directory where the test definition document is */
    private File testFileDir;

    /** directory where the style sheet is, ie testFileDir.getParentFile() */
    private File xsltFileDir;

    private int testResultLength;

    private boolean useCallTemplate;

    private String templateName;

    private StringBuffer applyTemplatesXsl;

    private StringBuffer callTemplateXsl;

    private Transformer matchingTransformer;

    private Transformer calledTransformer;

    private String outputType = "";

    /**
     * we want to append the test counter to the test namefor easy find if
     * failure occurs.
     */
    private int testCount;

    /** Constructor */
    public TestFileHandler(TestFileSuiteAssembler assembler) {
        this.assembler = assembler;
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        log = Logger.getLogger("utfx.framework");
        testCount = 0;
        tf = TransformerFactory.newInstance();
        try {
            setParserAndHandlers(this);
            setDefaultFeatures();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the various handlers to the specified handler.
     * 
     * @param handler the handler for the SAX parser
     */
    protected void setParserAndHandlers(DefaultHandler handler) throws SAXException {
        CatalogResolver cr = new CatalogResolver();
        parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
        parser.setContentHandler(handler);
        parser.setErrorHandler(handler);
        log.debug("setting catalog resolver");
        parser.setEntityResolver(cr);
        parser.setDTDHandler(handler);
        parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
    }

    /**
     * Set the default features used for parsing.
     */
    protected void setDefaultFeatures() throws SAXException {
        parser.setFeature("http://xml.org/sax/features/" + "external-general-entities", false);
        parser.setFeature("http://xml.org/sax/features/" + "external-parameter-entities", false);
        parser.setFeature("http://apache.org/xml/features/nonvalidating/" + "load-dtd-grammar", true);
        parser.setFeature("http://apache.org/xml/features/nonvalidating/" + "load-external-dtd", false);
        parser.setFeature("http://apache.org/xml/features/scanner/" + "notify-builtin-refs", false);
        parser.setFeature("http://xml.org/sax/features/namespaces", true);
        parser.setFeature("http://xml.org/sax/features/validation", true);
        parser.setFeature("http://apache.org/xml/features/validation/schema", false);
        parser.setFeature("http://apache.org/xml/features/validation/" + "schema-full-checking", false);
    }

    /**
     * Initialises the output stream and writer.
     */
    private void resetOutputStream() {
        Writer writer;
        String encoding = "UTF8";
        try {
            os = new ByteArrayOutputStream();
            writer = new OutputStreamWriter(os, encoding);
            fOut = new PrintWriter(writer);
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    /**
     * Start element.
     * 
     * @param uri the uri of the element
     * @param local the local name of the element
     * @param raw the raw name of the element
     * @param attrs the attributes of the element
     * @exception SAXException if an error occurs
     */
    public void startElement(String uri, String local, String raw, Attributes attrs) throws SAXException {
        int len;
        StringBuffer elem;
        currentElementName = local;
        if ("stylesheet".equals(raw)) {
            stylesheet = attrs.getValue("src");
            outputType = attrs.getValue("output");
        } else if ("dtd".equals(raw)) {
            sourceDtdUri = dtdDir + attrs.getValue("system");
        } else if ("test".equals(raw)) {
            resetOutputStream();
            templateName = null;
            if (matchingTransformer == null || calledTransformer == null) {
                templateName = attrs.getValue("call-template");
                useCallTemplate = (templateName != null) && !"".equals(templateName);
                if (useCallTemplate) {
                    if (callTemplateXsl == null) {
                        callTemplateXsl = new StringBuffer();
                        generateCommonXsl(callTemplateXsl);
                        callTemplateXsl.append("<xsl:template match=\"test-result/*\">");
                        callTemplateXsl.append("<xsl:choose>");
                    }
                    callTemplateXsl.append("<xsl:when test=\"$template-name='" + templateName + "'\">");
                    callTemplateXsl.append("<xsl:call-template name=\"" + templateName + "\"/>");
                    callTemplateXsl.append("</xsl:when>");
                } else if (!useCallTemplate && (applyTemplatesXsl == null)) {
                    applyTemplatesXsl = new StringBuffer();
                    generateCommonXsl(applyTemplatesXsl);
                    applyTemplatesXsl.append("</xsl:stylesheet>");
                }
            }
        } else if ("name".equals(raw)) {
            testName = new StringBuffer(new Integer(++testCount) + ". ");
        } else if ("source".equals(raw)) {
            readSource = true;
            source = new StringBuffer();
            source.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            log.debug("doctype String = " + doctypeString);
            source.append("<test-result ");
            source.append("xmlns:wpp=\"http://good.usq.edu.au" + "/web-pre-processor\"" + " xmlns:fo=\"http://www.w3.org" + "/1999/XSL/Format\" xmlns:rx=" + "\"http://www.renderx.com/XSL/Extensions\">");
            testResultLength = source.toString().length();
        } else if ("expected".equals(raw)) {
            readExpected = true;
            expected = new StringBuffer();
            expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            if ("xhtml".equals(outputType)) {
                expected.append("<!DOCTYPE test-result SYSTEM \"" + XsltTestValidator.xhtmlDtdUri + "\">\n");
            } else if ("fo".equals(outputType)) {
                expected.append("<!DOCTYPE test-result SYSTEM \"" + XsltTestValidator.foDtdUri + "\">\n");
            }
            expected.append("<test-result xmlns:gpp=\"http://good.usq.edu.au" + "/pre-processor\"  xmlns:wpp=\"" + "http://good.usq.edu.au/web-pre-processor\"" + " xmlns:fo=\"http://www.w3.org" + "/1999/XSL/Format\" xmlns:rx=" + "\"http://www.renderx.com/XSL/Extensions\">");
        } else if ("data".equals(raw)) {
            readData = true;
            escapeXML = true;
        } else if ("file".equals(raw)) {
            getDataFromFile(attrs.getValue("src"), attrs.getValue("select"));
        } else if ("message".equals(raw)) {
            message = new StringBuffer();
        }
        if (readData && !"data".equals(raw)) {
            elem = new StringBuffer();
            elem.append('<');
            elem.append(raw);
            if (attrs != null) {
                attrs = sortAttributes(attrs);
                len = attrs.getLength();
                for (int i = 0; i < len; i++) {
                    elem.append(' ');
                    elem.append(attrs.getQName(i));
                    elem.append("=\"");
                    elem.append(attrs.getValue(i));
                    elem.append('"');
                }
            }
            elem.append('>');
            if (readSource) {
                source.append(elem);
            } else if (readExpected) {
                expected.append(elem);
            }
        }
    }

    /** Characters. */
    public void characters(char ch[], int start, int length) throws SAXException {
        if (readData) {
            if (readSource) {
                source.append(ch, start, length);
            } else if (readExpected) {
                expected.append(ch, start, length);
            }
        } else if ("name".equals(currentElementName)) {
            testName.append(ch, start, length);
        } else if ("message".equals(currentElementName)) {
            message.append(ch, start, length);
        }
    }

    /** End element. */
    public void endElement(String uri, String local, String raw) throws SAXException {
        if (readData && !"data".equals(raw)) {
            if (readSource) {
                source.append("</" + raw + ">");
            } else if (readExpected) {
                expected.append("</" + raw + ">");
            }
        }
        if ("test".equals(raw)) {
            createTestCase();
        } else if ("source".equals(raw)) {
            if (source.toString().trim().length() == testResultLength) {
                source.append("<no-source-data/>");
            }
            source.append("</test-result>");
            readSource = false;
        } else if ("expected".equals(raw)) {
            expected.append("</test-result>");
            readExpected = false;
        } else if ("data".equals(raw)) {
            readData = false;
            escapeXML = false;
        }
    }

    public void endDocument() {
    }

    /** Warning. */
    public void warning(SAXParseException ex) throws SAXException {
        printError("Warning", ex);
    }

    /** Error. */
    public void error(SAXParseException ex) throws SAXException {
        printError("Error", ex);
    }

    /** Fatal error. */
    public void fatalError(SAXParseException ex) throws SAXException {
        printError("Fatal Error", ex);
        throw ex;
    }

    /** Start DTD. */
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

    /** End DTD. */
    public void endDTD() throws SAXException {
    }

    /** Start entity. */
    public void startEntity(String name) throws SAXException {
        fOut.print("&" + name + ";");
    }

    /** End entity. */
    public void endEntity(String name) throws SAXException {
    }

    /** Start CDATA section. */
    public void startCDATA() throws SAXException {
    }

    /** End CDATA section. */
    public void endCDATA() throws SAXException {
    }

    /**
     * Comment. Just print the characters, don't normalize them otherwise any
     * entities get over-expanded.
     */
    public void comment(char ch[], int start, int length) throws SAXException {
        fOut.print("<!--");
        for (int i = 0; i < length; i++) {
            fOut.print(ch[start + i]);
        }
        fOut.print("-->");
    }

    /** Returns a sorted list of attributes. */
    protected Attributes sortAttributes(Attributes attrs) {
        AttributesImpl attributes = new AttributesImpl();
        int len = (attrs != null) ? attrs.getLength() : 0;
        for (int i = 0; i < len; i++) {
            String name = attrs.getQName(i);
            int count = attributes.getLength();
            int j = 0;
            while (j < count) {
                if (name.compareTo(attributes.getQName(j)) < 0) {
                    break;
                }
                j++;
            }
            attributes.insertAttributeAt(j, name, attrs.getType(i), attrs.getValue(i));
        }
        return attributes;
    }

    /** Normalizes and prints the given string. */
    protected void normalizeAndPrint(String s) {
        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            normalizeAndPrint(c);
        }
    }

    /** Normalizes and prints the given array of characters. */
    protected void normalizeAndPrint(char[] ch, int offset, int length) {
        for (int i = 0; i < length; i++) {
            normalizeAndPrint(ch[offset + i]);
        }
    }

    /** Normalizes and print the given character. */
    protected void normalizeAndPrint(char c) {
        if (!escapeXML) {
            fOut.print(c);
            return;
        }
        switch(c) {
            case '<':
                fOut.print("&lt;");
                break;
            case '>':
                fOut.print("&gt;");
                break;
            case '&':
                fOut.print("&amp;");
                break;
            case '"':
                fOut.print("&quot;");
                break;
            case '\r':
            case '\n':
            default:
                fOut.print(c);
                break;
        }
    }

    /** Prints the error message. */
    protected void printError(String type, SAXParseException ex) {
        StringBuffer err = new StringBuffer(50);
        String systemId = ex.getSystemId();
        err.append("[" + type + "] ");
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1) {
                systemId = systemId.substring(index + 1);
            }
            err.append(systemId);
        }
        err.append(':');
        err.append(ex.getLineNumber());
        err.append(':');
        err.append(ex.getColumnNumber());
        err.append(": ");
        err.append(ex.getMessage());
        err.append(" ");
        log.error(err.toString());
    }

    /**
     * Parses an XML test definition document.
     */
    public void parse(String filename) throws IOException, SAXException {
        resetOutputStream();
        escapeXML = true;
        readData = false;
        readSource = false;
        readExpected = false;
        testFileDir = new File(filename).getParentFile();
        xsltFileDir = testFileDir.getParentFile();
        parser.parse(new InputSource(new FileInputStream(filename)));
    }

    /**
     * Creates an XsltTestCase from the currently parsed data and adds it to the
     * vector of test cases.
     * 
     * TODO this should most likely be moved to the assembler
     */
    private void createTestCase() {
        XsltTestCase testCase;
        Templates templates;
        StreamSource xsl;
        try {
            if (message == null || message.length() == 0) {
                message = new StringBuffer("The transformed output did not " + "match the expected result");
            }
            testCase = new XsltTestCase(testName.toString(), templateName, source.toString(), expected.toString(), message.toString());
            testCase.setOutputType(outputType);
            if (!useCallTemplate) {
                if (matchingTransformer == null) {
                    xsl = new StreamSource(new StringReader(applyTemplatesXsl.toString()));
                    templates = tf.newTemplates(xsl);
                    matchingTransformer = templates.newTransformer();
                }
                testCase.setTransformer(matchingTransformer);
            }
            assembler.addTestCase(testCase);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates the common XSL header.
     */
    private void generateCommonXsl(StringBuffer buf) {
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        buf.append("<xsl:stylesheet version=\"1.0\" " + "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" " + "xmlns:gpp=\"http://good.usq.edu.au/pre-processor\">");
        buf.append("<xsl:import href=\"" + xsltFileDir + "/" + stylesheet + "\"/>");
        buf.append("<xsl:output method=\"xml\"" + " omit-xml-declaration=\"yes\"/>");
        buf.append("<xsl:param name=\"template-name\"/>");
        buf.append("<xsl:template match=\"/\">");
        buf.append("<test-result>");
        buf.append("<xsl:apply-templates/>");
        buf.append("</test-result>");
        buf.append("</xsl:template>");
    }

    /**
     * Gets the source or expected data specified by a <file>element. This
     * method works be generating a stylesheet to get a result tree from using
     * the specified XPath expression. A transformer uses this stylesheet to
     * apply the XPath and get the selected nodes verbatim. This result is then
     * used copied into the source or expected buffers as necessary.
     * 
     * @param src the input XML file
     * @param select the XPath expression
     */
    private void getDataFromFile(String src, String select) {
        try {
            PrintWriter writer;
            ByteArrayOutputStream xslOut = new ByteArrayOutputStream();
            Transformer transformer;
            String path = testFileDir.getPath() + "/" + src;
            FileInputStream srcFile = new FileInputStream(path);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(xslOut, "UTF8"));
            writer.println("<xsl:stylesheet version=\"1.0\" " + "xmlns:xsl=\"http://www.w3.org/1999/XSL" + "/Transform\">");
            fOut.println("<xsl:output method=\"xml\"" + " omit-xml-declaration=\"yes\"/>");
            writer.println("<xsl:template match=\"/\">");
            writer.println("<xsl:apply-templates select=\"" + select + "\"/>");
            writer.println("</xsl:template>");
            writer.println("<xsl:template match=\"@*|node()\">");
            writer.println("<xsl:copy>");
            writer.println("<xsl:apply-templates select=\"@*|node()\"/>");
            writer.println("</xsl:copy>");
            writer.println("</xsl:template>");
            writer.println("</xsl:stylesheet>");
            writer.flush();
            transformer = tf.newTransformer(new StreamSource(new ByteArrayInputStream(xslOut.toByteArray())));
            transformer.transform(new StreamSource(srcFile), new StreamResult(result));
            if (readSource) {
                source = new StringBuffer(result.toString("UTF8"));
            } else if (readExpected) {
                expected = new StringBuffer(result.toString("UTF8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
