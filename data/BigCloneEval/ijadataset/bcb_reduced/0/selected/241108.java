package org.dom4j.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.xpp.ProxyXmlStartTag;
import org.gjt.xpp.XmlEndTag;
import org.gjt.xpp.XmlPullParser;
import org.gjt.xpp.XmlPullParserException;
import org.gjt.xpp.XmlPullParserFactory;

/**
 * <p>
 * <code>XPPReader</code> is a Reader of DOM4J documents that uses the fast <a
 * href="http://www.extreme.indiana.edu/soap/xpp/">XML Pull Parser 2.x </a>. It
 * does not currently support comments, CDATA or ProcessingInstructions or
 * validation but it is very fast for use in SOAP style environments.
 * </p>
 * 
 * @author <a href="mailto:jstrachan@apache.org">James Strachan </a>
 * @version $Revision: 1.7 $
 */
public class XPPReader {

    /** <code>DocumentFactory</code> used to create new document objects */
    private DocumentFactory factory;

    /** <code>XmlPullParser</code> used to parse XML */
    private XmlPullParser xppParser;

    /** <code>XmlPullParser</code> used to parse XML */
    private XmlPullParserFactory xppFactory;

    /** DispatchHandler to call when each <code>Element</code> is encountered */
    private DispatchHandler dispatchHandler;

    public XPPReader() {
    }

    public XPPReader(DocumentFactory factory) {
        this.factory = factory;
    }

    /**
     * <p>
     * Reads a Document from the given <code>File</code>
     * </p>
     * 
     * @param file
     *            is the <code>File</code> to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     *             if an error occurs during parsing.
     * @throws IOException
     *             if a URL could not be made for the given File
     * @throws XmlPullParserException
     *             DOCUMENT ME!
     */
    public Document read(File file) throws DocumentException, IOException, XmlPullParserException {
        String systemID = file.getAbsolutePath();
        return read(new BufferedReader(new FileReader(file)), systemID);
    }

    /**
     * <p>
     * Reads a Document from the given <code>URL</code>
     * </p>
     * 
     * @param url
     *            <code>URL</code> to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     *             if an error occurs during parsing.
     * @throws IOException
     *             DOCUMENT ME!
     * @throws XmlPullParserException
     *             DOCUMENT ME!
     */
    public Document read(URL url) throws DocumentException, IOException, XmlPullParserException {
        String systemID = url.toExternalForm();
        return read(createReader(url.openStream()), systemID);
    }

    /**
     * <p>
     * Reads a Document from the given URL or filename.
     * </p>
     * 
     * <p>
     * If the systemID contains a <code>':'</code> character then it is
     * assumed to be a URL otherwise its assumed to be a file name. If you want
     * finer grained control over this mechansim then please explicitly pass in
     * either a {@link URL}or a {@link File}instance instead of a {@link
     * String} to denote the source of the document.
     * </p>
     * 
     * @param systemID
     *            is a URL for a document or a file name.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     *             if an error occurs during parsing.
     * @throws IOException
     *             if a URL could not be made for the given File
     * @throws XmlPullParserException
     *             DOCUMENT ME!
     */
    public Document read(String systemID) throws DocumentException, IOException, XmlPullParserException {
        if (systemID.indexOf(':') >= 0) {
            return read(new URL(systemID));
        } else {
            return read(new File(systemID));
        }
    }

    /**
     * <p>
     * Reads a Document from the given stream
     * </p>
     * 
     * @param in
     *            <code>InputStream</code> to read from.
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     *             if an error occurs during parsing.
     * @throws IOException
     *             DOCUMENT ME!
     * @throws XmlPullParserException
     *             DOCUMENT ME!
     */
    public Document read(InputStream in) throws DocumentException, IOException, XmlPullParserException {
        return read(createReader(in));
    }

    /**
     * <p>
     * Reads a Document from the given <code>Reader</code>
     * </p>
     * 
     * @param reader
     *            is the reader for the input
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     *             if an error occurs during parsing.
     * @throws IOException
     *             DOCUMENT ME!
     * @throws XmlPullParserException
     *             DOCUMENT ME!
     */
    public Document read(Reader reader) throws DocumentException, IOException, XmlPullParserException {
        getXPPParser().setInput(reader);
        return parseDocument();
    }

    /**
     * <p>
     * Reads a Document from the given array of characters
     * </p>
     * 
     * @param text
     *            is the text to parse
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     *             if an error occurs during parsing.
     * @throws IOException
     *             DOCUMENT ME!
     * @throws XmlPullParserException
     *             DOCUMENT ME!
     */
    public Document read(char[] text) throws DocumentException, IOException, XmlPullParserException {
        getXPPParser().setInput(text);
        return parseDocument();
    }

    /**
     * <p>
     * Reads a Document from the given stream
     * </p>
     * 
     * @param in
     *            <code>InputStream</code> to read from.
     * @param systemID
     *            is the URI for the input
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     *             if an error occurs during parsing.
     * @throws IOException
     *             DOCUMENT ME!
     * @throws XmlPullParserException
     *             DOCUMENT ME!
     */
    public Document read(InputStream in, String systemID) throws DocumentException, IOException, XmlPullParserException {
        return read(createReader(in), systemID);
    }

    /**
     * <p>
     * Reads a Document from the given <code>Reader</code>
     * </p>
     * 
     * @param reader
     *            is the reader for the input
     * @param systemID
     *            is the URI for the input
     * 
     * @return the newly created Document instance
     * 
     * @throws DocumentException
     *             if an error occurs during parsing.
     * @throws IOException
     *             DOCUMENT ME!
     * @throws XmlPullParserException
     *             DOCUMENT ME!
     */
    public Document read(Reader reader, String systemID) throws DocumentException, IOException, XmlPullParserException {
        Document document = read(reader);
        document.setName(systemID);
        return document;
    }

    public XmlPullParser getXPPParser() throws XmlPullParserException {
        if (xppParser == null) {
            xppParser = getXPPFactory().newPullParser();
        }
        return xppParser;
    }

    public XmlPullParserFactory getXPPFactory() throws XmlPullParserException {
        if (xppFactory == null) {
            xppFactory = XmlPullParserFactory.newInstance();
        }
        return xppFactory;
    }

    public void setXPPFactory(XmlPullParserFactory xPPFactory) {
        this.xppFactory = xPPFactory;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return the <code>DocumentFactory</code> used to create document
     *         objects
     */
    public DocumentFactory getDocumentFactory() {
        if (factory == null) {
            factory = DocumentFactory.getInstance();
        }
        return factory;
    }

    /**
     * <p>
     * This sets the <code>DocumentFactory</code> used to create new
     * documents. This method allows the building of custom DOM4J tree objects
     * to be implemented easily using a custom derivation of
     * {@link DocumentFactory}
     * </p>
     * 
     * @param documentFactory
     *            <code>DocumentFactory</code> used to create DOM4J objects
     */
    public void setDocumentFactory(DocumentFactory documentFactory) {
        this.factory = documentFactory;
    }

    /**
     * Adds the <code>ElementHandler</code> to be called when the specified
     * path is encounted.
     * 
     * @param path
     *            is the path to be handled
     * @param handler
     *            is the <code>ElementHandler</code> to be called by the event
     *            based processor.
     */
    public void addHandler(String path, ElementHandler handler) {
        getDispatchHandler().addHandler(path, handler);
    }

    /**
     * Removes the <code>ElementHandler</code> from the event based processor,
     * for the specified path.
     * 
     * @param path
     *            is the path to remove the <code>ElementHandler</code> for.
     */
    public void removeHandler(String path) {
        getDispatchHandler().removeHandler(path);
    }

    /**
     * When multiple <code>ElementHandler</code> instances have been
     * registered, this will set a default <code>ElementHandler</code> to be
     * called for any path which does <b>NOT </b> have a handler registered.
     * 
     * @param handler
     *            is the <code>ElementHandler</code> to be called by the event
     *            based processor.
     */
    public void setDefaultHandler(ElementHandler handler) {
        getDispatchHandler().setDefaultHandler(handler);
    }

    protected Document parseDocument() throws DocumentException, IOException, XmlPullParserException {
        Document document = getDocumentFactory().createDocument();
        Element parent = null;
        XmlPullParser parser = getXPPParser();
        parser.setNamespaceAware(true);
        ProxyXmlStartTag startTag = new ProxyXmlStartTag();
        XmlEndTag endTag = xppFactory.newEndTag();
        while (true) {
            int type = parser.next();
            switch(type) {
                case XmlPullParser.END_DOCUMENT:
                    return document;
                case XmlPullParser.START_TAG:
                    {
                        parser.readStartTag(startTag);
                        Element newElement = startTag.getElement();
                        if (parent != null) {
                            parent.add(newElement);
                        } else {
                            document.add(newElement);
                        }
                        parent = newElement;
                        break;
                    }
                case XmlPullParser.END_TAG:
                    {
                        parser.readEndTag(endTag);
                        if (parent != null) {
                            parent = parent.getParent();
                        }
                        break;
                    }
                case XmlPullParser.CONTENT:
                    {
                        String text = parser.readContent();
                        if (parent != null) {
                            parent.addText(text);
                        } else {
                            String msg = "Cannot have text content outside of the " + "root document";
                            throw new DocumentException(msg);
                        }
                        break;
                    }
                default:
                    throw new DocumentException("Error: unknown type: " + type);
            }
        }
    }

    protected DispatchHandler getDispatchHandler() {
        if (dispatchHandler == null) {
            dispatchHandler = new DispatchHandler();
        }
        return dispatchHandler;
    }

    protected void setDispatchHandler(DispatchHandler dispatchHandler) {
        this.dispatchHandler = dispatchHandler;
    }

    /**
     * Factory method to create a Reader from the given InputStream.
     * 
     * @param in
     *            DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * 
     * @throws IOException
     *             DOCUMENT ME!
     */
    protected Reader createReader(InputStream in) throws IOException {
        return new BufferedReader(new InputStreamReader(in));
    }
}
