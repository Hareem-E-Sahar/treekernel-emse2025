package org.apache.xalan.xsltc.trax;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xalan.xsltc.DOM;
import org.apache.xalan.xsltc.DOMCache;
import org.apache.xalan.xsltc.DOMEnhancedForDTM;
import org.apache.xalan.xsltc.StripFilter;
import org.apache.xalan.xsltc.Translet;
import org.apache.xalan.xsltc.TransletException;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xalan.xsltc.compiler.util.ErrorMsg;
import org.apache.xalan.xsltc.dom.DOMWSFilter;
import org.apache.xalan.xsltc.dom.SAXImpl;
import org.apache.xalan.xsltc.dom.XSLTCDTMManager;
import org.apache.xalan.xsltc.runtime.AbstractTranslet;
import org.apache.xalan.xsltc.runtime.Hashtable;
import org.apache.xalan.xsltc.runtime.output.TransletOutputHandlerFactory;
import org.apache.xml.dtm.DTMWSFilter;
import org.apache.xml.utils.XMLReaderManager;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author Morten Jorgensen
 * @author G. Todd Miller
 * @author Santiago Pericas-Geertsen
 */
public final class TransformerImpl extends Transformer implements DOMCache, ErrorListener {

    private static final String EMPTY_STRING = "";

    private static final String NO_STRING = "no";

    private static final String YES_STRING = "yes";

    private static final String XML_STRING = "xml";

    private static final String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";

    private static final String NAMESPACE_FEATURE = "http://xml.org/sax/features/namespaces";

    /**
     * A reference to the translet or null if the identity transform.
     */
    private AbstractTranslet _translet = null;

    /**
     * The output method of this transformation.
     */
    private String _method = null;

    /**
     * The output encoding of this transformation.
     */
    private String _encoding = null;

    /**
     * The systemId set in input source.
     */
    private String _sourceSystemId = null;

    /**
     * An error listener for runtime errors.
     */
    private ErrorListener _errorListener = this;

    /**
     * A reference to a URI resolver for calls to document().
     */
    private URIResolver _uriResolver = null;

    /**
     * Output properties of this transformer instance.
     */
    private Properties _properties, _propertiesClone;

    /**
     * A reference to an output handler factory.
     */
    private TransletOutputHandlerFactory _tohFactory = null;

    /**
     * A reference to a internal DOM represenation of the input.
     */
    private DOM _dom = null;

    /**
     * Number of indent spaces to add when indentation is on.
     */
    private int _indentNumber;

    /**
     * A reference to the transformer factory that this templates
     * object belongs to.
     */
    private TransformerFactoryImpl _tfactory = null;

    /**
     * A reference to the output stream, if we create one in our code.
     */
    private OutputStream _ostream = null;

    /**
     * A reference to the XSLTCDTMManager which is used to build the DOM/DTM
     * for this transformer.
     */
    private XSLTCDTMManager _dtmManager = null;

    /**
     * A reference to an object that creates and caches XMLReader objects.
     */
    private XMLReaderManager _readerManager = XMLReaderManager.getInstance();

    /**
     * A flag indicating whether this transformer implements the identity 
     * transform.
     */
    private boolean _isIdentity = false;

    /**
     * State of the secure processing feature.
     */
    private boolean _isSecureProcessing = false;

    /**
     * A hashtable to store parameters for the identity transform. These
     * are not needed during the transformation, but we must keep track of 
     * them to be fully complaint with the JAXP API.
     */
    private Hashtable _parameters = null;

    /**
     * This class wraps an ErrorListener into a MessageHandler in order to
     * capture messages reported via xsl:message.
     */
    static class MessageHandler extends org.apache.xalan.xsltc.runtime.MessageHandler {

        private ErrorListener _errorListener;

        public MessageHandler(ErrorListener errorListener) {
            _errorListener = errorListener;
        }

        public void displayMessage(String msg) {
            if (_errorListener == null) {
                System.err.println(msg);
            } else {
                try {
                    _errorListener.warning(new TransformerException(msg));
                } catch (TransformerException e) {
                }
            }
        }
    }

    protected TransformerImpl(Properties outputProperties, int indentNumber, TransformerFactoryImpl tfactory) {
        this(null, outputProperties, indentNumber, tfactory);
        _isIdentity = true;
    }

    protected TransformerImpl(Translet translet, Properties outputProperties, int indentNumber, TransformerFactoryImpl tfactory) {
        _translet = (AbstractTranslet) translet;
        _properties = createOutputProperties(outputProperties);
        _propertiesClone = (Properties) _properties.clone();
        _indentNumber = indentNumber;
        _tfactory = tfactory;
    }

    /**
     * Return the state of the secure processing feature.
     */
    public boolean isSecureProcessing() {
        return _isSecureProcessing;
    }

    /**
     * Set the state of the secure processing feature.
     */
    public void setSecureProcessing(boolean flag) {
        _isSecureProcessing = flag;
    }

    /**
     * Returns the translet wrapped inside this Transformer or
     * null if this is the identity transform.
     */
    protected AbstractTranslet getTranslet() {
        return _translet;
    }

    public boolean isIdentity() {
        return _isIdentity;
    }

    /**
     * Implements JAXP's Transformer.transform()
     *
     * @param source Contains the input XML document
     * @param result Will contain the output from the transformation
     * @throws TransformerException
     */
    public void transform(Source source, Result result) throws TransformerException {
        if (!_isIdentity) {
            if (_translet == null) {
                ErrorMsg err = new ErrorMsg(ErrorMsg.JAXP_NO_TRANSLET_ERR);
                throw new TransformerException(err.toString());
            }
            transferOutputProperties(_translet);
        }
        final SerializationHandler toHandler = getOutputHandler(result);
        if (toHandler == null) {
            ErrorMsg err = new ErrorMsg(ErrorMsg.JAXP_NO_HANDLER_ERR);
            throw new TransformerException(err.toString());
        }
        if (_uriResolver != null && !_isIdentity) {
            _translet.setDOMCache(this);
        }
        if (_isIdentity) {
            transferOutputProperties(toHandler);
        }
        transform(source, toHandler, _encoding);
        if (result instanceof DOMResult) {
            ((DOMResult) result).setNode(_tohFactory.getNode());
        }
    }

    /**
     * Create an output handler for the transformation output based on 
     * the type and contents of the TrAX Result object passed to the 
     * transform() method. 
     */
    public SerializationHandler getOutputHandler(Result result) throws TransformerException {
        _method = (String) _properties.get(OutputKeys.METHOD);
        _encoding = (String) _properties.getProperty(OutputKeys.ENCODING);
        _tohFactory = TransletOutputHandlerFactory.newInstance();
        _tohFactory.setEncoding(_encoding);
        if (_method != null) {
            _tohFactory.setOutputMethod(_method);
        }
        if (_indentNumber >= 0) {
            _tohFactory.setIndentNumber(_indentNumber);
        }
        try {
            if (result instanceof SAXResult) {
                final SAXResult target = (SAXResult) result;
                final ContentHandler handler = target.getHandler();
                _tohFactory.setHandler(handler);
                LexicalHandler lexicalHandler = target.getLexicalHandler();
                if (lexicalHandler != null) {
                    _tohFactory.setLexicalHandler(lexicalHandler);
                }
                _tohFactory.setOutputType(TransletOutputHandlerFactory.SAX);
                return _tohFactory.getSerializationHandler();
            } else if (result instanceof DOMResult) {
                _tohFactory.setNode(((DOMResult) result).getNode());
                _tohFactory.setNextSibling(((DOMResult) result).getNextSibling());
                _tohFactory.setOutputType(TransletOutputHandlerFactory.DOM);
                return _tohFactory.getSerializationHandler();
            } else if (result instanceof StreamResult) {
                final StreamResult target = (StreamResult) result;
                _tohFactory.setOutputType(TransletOutputHandlerFactory.STREAM);
                final Writer writer = target.getWriter();
                if (writer != null) {
                    _tohFactory.setWriter(writer);
                    return _tohFactory.getSerializationHandler();
                }
                final OutputStream ostream = target.getOutputStream();
                if (ostream != null) {
                    _tohFactory.setOutputStream(ostream);
                    return _tohFactory.getSerializationHandler();
                }
                String systemId = result.getSystemId();
                if (systemId == null) {
                    ErrorMsg err = new ErrorMsg(ErrorMsg.JAXP_NO_RESULT_ERR);
                    throw new TransformerException(err.toString());
                }
                URL url = null;
                if (systemId.startsWith("file:")) {
                    url = new URL(systemId);
                    _tohFactory.setOutputStream(_ostream = new FileOutputStream(url.getFile()));
                    return _tohFactory.getSerializationHandler();
                } else if (systemId.startsWith("http:")) {
                    url = new URL(systemId);
                    final URLConnection connection = url.openConnection();
                    _tohFactory.setOutputStream(_ostream = connection.getOutputStream());
                    return _tohFactory.getSerializationHandler();
                } else {
                    url = new File(systemId).toURL();
                    _tohFactory.setOutputStream(_ostream = new FileOutputStream(url.getFile()));
                    return _tohFactory.getSerializationHandler();
                }
            }
        } catch (UnknownServiceException e) {
            throw new TransformerException(e);
        } catch (ParserConfigurationException e) {
            throw new TransformerException(e);
        } catch (IOException e) {
            throw new TransformerException(e);
        }
        return null;
    }

    /**
     * Set the internal DOM that will be used for the next transformation
     */
    protected void setDOM(DOM dom) {
        _dom = dom;
    }

    /**
     * Builds an internal DOM from a TrAX Source object
     */
    private DOM getDOM(Source source) throws TransformerException {
        try {
            DOM dom = null;
            if (source != null) {
                DTMWSFilter wsfilter;
                if (_translet != null && _translet instanceof StripFilter) {
                    wsfilter = new DOMWSFilter(_translet);
                } else {
                    wsfilter = null;
                }
                boolean hasIdCall = (_translet != null) ? _translet.hasIdCall() : false;
                if (_dtmManager == null) {
                    _dtmManager = (XSLTCDTMManager) _tfactory.getDTMManagerClass().newInstance();
                }
                dom = (DOM) _dtmManager.getDTM(source, false, wsfilter, true, false, false, 0, hasIdCall);
            } else if (_dom != null) {
                dom = _dom;
                _dom = null;
            } else {
                return null;
            }
            if (!_isIdentity) {
                _translet.prepassDocument(dom);
            }
            return dom;
        } catch (Exception e) {
            if (_errorListener != null) {
                postErrorToListener(e.getMessage());
            }
            throw new TransformerException(e);
        }
    }

    /**
     * Returns the {@link org.apache.xalan.xsltc.trax.TransformerFactoryImpl}
     * object that create this <code>Transformer</code>.
     */
    protected TransformerFactoryImpl getTransformerFactory() {
        return _tfactory;
    }

    /**
     * Returns the {@link org.apache.xalan.xsltc.runtime.output.TransletOutputHandlerFactory}
     * object that create the <code>TransletOutputHandler</code>.
     */
    protected TransletOutputHandlerFactory getTransletOutputHandlerFactory() {
        return _tohFactory;
    }

    private void transformIdentity(Source source, SerializationHandler handler) throws Exception {
        if (source != null) {
            _sourceSystemId = source.getSystemId();
        }
        if (source instanceof StreamSource) {
            final StreamSource stream = (StreamSource) source;
            final InputStream streamInput = stream.getInputStream();
            final Reader streamReader = stream.getReader();
            final XMLReader reader = _readerManager.getXMLReader();
            try {
                try {
                    reader.setProperty(LEXICAL_HANDLER_PROPERTY, handler);
                } catch (SAXException e) {
                }
                reader.setContentHandler(handler);
                InputSource input;
                if (streamInput != null) {
                    input = new InputSource(streamInput);
                    input.setSystemId(_sourceSystemId);
                } else if (streamReader != null) {
                    input = new InputSource(streamReader);
                    input.setSystemId(_sourceSystemId);
                } else if (_sourceSystemId != null) {
                    input = new InputSource(_sourceSystemId);
                } else {
                    ErrorMsg err = new ErrorMsg(ErrorMsg.JAXP_NO_SOURCE_ERR);
                    throw new TransformerException(err.toString());
                }
                reader.parse(input);
            } finally {
                _readerManager.releaseXMLReader(reader);
            }
        } else if (source instanceof SAXSource) {
            final SAXSource sax = (SAXSource) source;
            XMLReader reader = sax.getXMLReader();
            final InputSource input = sax.getInputSource();
            boolean userReader = true;
            try {
                if (reader == null) {
                    reader = _readerManager.getXMLReader();
                    userReader = false;
                }
                try {
                    reader.setProperty(LEXICAL_HANDLER_PROPERTY, handler);
                } catch (SAXException e) {
                }
                reader.setContentHandler(handler);
                reader.parse(input);
            } finally {
                if (!userReader) {
                    _readerManager.releaseXMLReader(reader);
                }
            }
        } else if (source instanceof DOMSource) {
            final DOMSource domsrc = (DOMSource) source;
            new DOM2TO(domsrc.getNode(), handler).parse();
        } else if (source instanceof XSLTCSource) {
            final DOM dom = ((XSLTCSource) source).getDOM(null, _translet);
            ((SAXImpl) dom).copy(handler);
        } else {
            ErrorMsg err = new ErrorMsg(ErrorMsg.JAXP_NO_SOURCE_ERR);
            throw new TransformerException(err.toString());
        }
    }

    /**
     * Internal transformation method - uses the internal APIs of XSLTC
     */
    private void transform(Source source, SerializationHandler handler, String encoding) throws TransformerException {
        try {
            if ((source instanceof StreamSource && source.getSystemId() == null && ((StreamSource) source).getInputStream() == null && ((StreamSource) source).getReader() == null) || (source instanceof SAXSource && ((SAXSource) source).getInputSource() == null && ((SAXSource) source).getXMLReader() == null) || (source instanceof DOMSource && ((DOMSource) source).getNode() == null)) {
                DocumentBuilderFactory builderF = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderF.newDocumentBuilder();
                String systemID = source.getSystemId();
                source = new DOMSource(builder.newDocument());
                if (systemID != null) {
                    source.setSystemId(systemID);
                }
            }
            if (_isIdentity) {
                transformIdentity(source, handler);
            } else {
                _translet.transform(getDOM(source), handler);
            }
        } catch (TransletException e) {
            if (_errorListener != null) postErrorToListener(e.getMessage());
            throw new TransformerException(e);
        } catch (RuntimeException e) {
            if (_errorListener != null) postErrorToListener(e.getMessage());
            throw new TransformerException(e);
        } catch (Exception e) {
            if (_errorListener != null) postErrorToListener(e.getMessage());
            throw new TransformerException(e);
        } finally {
            _dtmManager = null;
        }
        if (_ostream != null) {
            try {
                _ostream.close();
            } catch (IOException e) {
            }
            _ostream = null;
        }
    }

    /**
     * Implements JAXP's Transformer.getErrorListener()
     * Get the error event handler in effect for the transformation.
     *
     * @return The error event handler currently in effect
     */
    public ErrorListener getErrorListener() {
        return _errorListener;
    }

    /**
     * Implements JAXP's Transformer.setErrorListener()
     * Set the error event listener in effect for the transformation.
     * Register a message handler in the translet in order to forward
     * xsl:messages to error listener.
     *
     * @param listener The error event listener to use
     * @throws IllegalArgumentException
     */
    public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
        if (listener == null) {
            ErrorMsg err = new ErrorMsg(ErrorMsg.ERROR_LISTENER_NULL_ERR, "Transformer");
            throw new IllegalArgumentException(err.toString());
        }
        _errorListener = listener;
        if (_translet != null) _translet.setMessageHandler(new MessageHandler(_errorListener));
    }

    /**
     * Inform TrAX error listener of an error
     */
    private void postErrorToListener(String message) {
        try {
            _errorListener.error(new TransformerException(message));
        } catch (TransformerException e) {
        }
    }

    /**
     * Inform TrAX error listener of a warning
     */
    private void postWarningToListener(String message) {
        try {
            _errorListener.warning(new TransformerException(message));
        } catch (TransformerException e) {
        }
    }

    /**
     * The translet stores all CDATA sections set in the <xsl:output> element
     * in a Hashtable. This method will re-construct the whitespace separated
     * list of elements given in the <xsl:output> element.
     */
    private String makeCDATAString(Hashtable cdata) {
        if (cdata == null) return null;
        StringBuffer result = new StringBuffer();
        Enumeration elements = cdata.keys();
        if (elements.hasMoreElements()) {
            result.append((String) elements.nextElement());
            while (elements.hasMoreElements()) {
                String element = (String) elements.nextElement();
                result.append(' ');
                result.append(element);
            }
        }
        return (result.toString());
    }

    /**
     * Implements JAXP's Transformer.getOutputProperties().
     * Returns a copy of the output properties for the transformation. This is
     * a set of layered properties. The first layer contains properties set by
     * calls to setOutputProperty() and setOutputProperties() on this class,
     * and the output settings defined in the stylesheet's <xsl:output>
     * element makes up the second level, while the default XSLT output
     * settings are returned on the third level.
     *
     * @return Properties in effect for this Transformer
     */
    public Properties getOutputProperties() {
        return (Properties) _properties.clone();
    }

    /**
     * Implements JAXP's Transformer.getOutputProperty().
     * Get an output property that is in effect for the transformation. The
     * property specified may be a property that was set with setOutputProperty,
     * or it may be a property specified in the stylesheet.
     *
     * @param name A non-null string that contains the name of the property
     * @throws IllegalArgumentException if the property name is not known
     */
    public String getOutputProperty(String name) throws IllegalArgumentException {
        if (!validOutputProperty(name)) {
            ErrorMsg err = new ErrorMsg(ErrorMsg.JAXP_UNKNOWN_PROP_ERR, name);
            throw new IllegalArgumentException(err.toString());
        }
        return _properties.getProperty(name);
    }

    /**
     * Implements JAXP's Transformer.setOutputProperties().
     * Set the output properties for the transformation. These properties
     * will override properties set in the Templates with xsl:output.
     * Unrecognised properties will be quitely ignored.
     *
     * @param properties The properties to use for the Transformer
     * @throws IllegalArgumentException Never, errors are ignored
     */
    public void setOutputProperties(Properties properties) throws IllegalArgumentException {
        if (properties != null) {
            final Enumeration names = properties.propertyNames();
            while (names.hasMoreElements()) {
                final String name = (String) names.nextElement();
                if (isDefaultProperty(name, properties)) continue;
                if (validOutputProperty(name)) {
                    _properties.setProperty(name, properties.getProperty(name));
                } else {
                    ErrorMsg err = new ErrorMsg(ErrorMsg.JAXP_UNKNOWN_PROP_ERR, name);
                    throw new IllegalArgumentException(err.toString());
                }
            }
        } else {
            _properties = _propertiesClone;
        }
    }

    /**
     * Implements JAXP's Transformer.setOutputProperty().
     * Get an output property that is in effect for the transformation. The
     * property specified may be a property that was set with 
     * setOutputProperty(), or it may be a property specified in the stylesheet.
     *
     * @param name The name of the property to set
     * @param value The value to assign to the property
     * @throws IllegalArgumentException Never, errors are ignored
     */
    public void setOutputProperty(String name, String value) throws IllegalArgumentException {
        if (!validOutputProperty(name)) {
            ErrorMsg err = new ErrorMsg(ErrorMsg.JAXP_UNKNOWN_PROP_ERR, name);
            throw new IllegalArgumentException(err.toString());
        }
        _properties.setProperty(name, value);
    }

    /**
     * Internal method to pass any properties to the translet prior to
     * initiating the transformation
     */
    private void transferOutputProperties(AbstractTranslet translet) {
        if (_properties == null) return;
        Enumeration names = _properties.propertyNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = (String) _properties.get(name);
            if (value == null) continue;
            if (name.equals(OutputKeys.ENCODING)) {
                translet._encoding = value;
            } else if (name.equals(OutputKeys.METHOD)) {
                translet._method = value;
            } else if (name.equals(OutputKeys.DOCTYPE_PUBLIC)) {
                translet._doctypePublic = value;
            } else if (name.equals(OutputKeys.DOCTYPE_SYSTEM)) {
                translet._doctypeSystem = value;
            } else if (name.equals(OutputKeys.MEDIA_TYPE)) {
                translet._mediaType = value;
            } else if (name.equals(OutputKeys.STANDALONE)) {
                translet._standalone = value;
            } else if (name.equals(OutputKeys.VERSION)) {
                translet._version = value;
            } else if (name.equals(OutputKeys.OMIT_XML_DECLARATION)) {
                translet._omitHeader = (value != null && value.toLowerCase().equals("yes"));
            } else if (name.equals(OutputKeys.INDENT)) {
                translet._indent = (value != null && value.toLowerCase().equals("yes"));
            } else if (name.equals(OutputKeys.CDATA_SECTION_ELEMENTS)) {
                if (value != null) {
                    translet._cdata = null;
                    StringTokenizer e = new StringTokenizer(value);
                    while (e.hasMoreTokens()) {
                        translet.addCdataElement(e.nextToken());
                    }
                }
            }
        }
    }

    /**
     * This method is used to pass any properties to the output handler
     * when running the identity transform.
     */
    public void transferOutputProperties(SerializationHandler handler) {
        if (_properties == null) return;
        String doctypePublic = null;
        String doctypeSystem = null;
        Enumeration names = _properties.propertyNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            String value = (String) _properties.get(name);
            if (value == null) continue;
            if (name.equals(OutputKeys.DOCTYPE_PUBLIC)) {
                doctypePublic = value;
            } else if (name.equals(OutputKeys.DOCTYPE_SYSTEM)) {
                doctypeSystem = value;
            } else if (name.equals(OutputKeys.MEDIA_TYPE)) {
                handler.setMediaType(value);
            } else if (name.equals(OutputKeys.STANDALONE)) {
                handler.setStandalone(value);
            } else if (name.equals(OutputKeys.VERSION)) {
                handler.setVersion(value);
            } else if (name.equals(OutputKeys.OMIT_XML_DECLARATION)) {
                handler.setOmitXMLDeclaration(value != null && value.toLowerCase().equals("yes"));
            } else if (name.equals(OutputKeys.INDENT)) {
                handler.setIndent(value != null && value.toLowerCase().equals("yes"));
            } else if (name.equals(OutputKeys.CDATA_SECTION_ELEMENTS)) {
                if (value != null) {
                    StringTokenizer e = new StringTokenizer(value);
                    Vector uriAndLocalNames = null;
                    while (e.hasMoreTokens()) {
                        final String token = e.nextToken();
                        int lastcolon = token.lastIndexOf(':');
                        String uri;
                        String localName;
                        if (lastcolon > 0) {
                            uri = token.substring(0, lastcolon);
                            localName = token.substring(lastcolon + 1);
                        } else {
                            uri = null;
                            localName = token;
                        }
                        if (uriAndLocalNames == null) {
                            uriAndLocalNames = new Vector();
                        }
                        uriAndLocalNames.addElement(uri);
                        uriAndLocalNames.addElement(localName);
                    }
                    handler.setCdataSectionElements(uriAndLocalNames);
                }
            }
        }
        if (doctypePublic != null || doctypeSystem != null) {
            handler.setDoctype(doctypeSystem, doctypePublic);
        }
    }

    /**
     * Internal method to create the initial set of properties. There
     * are two layers of properties: the default layer and the base layer.
     * The latter contains properties defined in the stylesheet or by
     * the user using this API.
     */
    private Properties createOutputProperties(Properties outputProperties) {
        final Properties defaults = new Properties();
        setDefaults(defaults, "xml");
        final Properties base = new Properties(defaults);
        if (outputProperties != null) {
            final Enumeration names = outputProperties.propertyNames();
            while (names.hasMoreElements()) {
                final String name = (String) names.nextElement();
                base.setProperty(name, outputProperties.getProperty(name));
            }
        } else {
            base.setProperty(OutputKeys.ENCODING, _translet._encoding);
            if (_translet._method != null) base.setProperty(OutputKeys.METHOD, _translet._method);
        }
        final String method = base.getProperty(OutputKeys.METHOD);
        if (method != null) {
            if (method.equals("html")) {
                setDefaults(defaults, "html");
            } else if (method.equals("text")) {
                setDefaults(defaults, "text");
            }
        }
        return base;
    }

    /**
	 * Internal method to get the default properties from the
	 * serializer factory and set them on the property object.
	 * @param props a java.util.Property object on which the properties are set.
	 * @param method The output method type, one of "xml", "text", "html" ...
	 */
    private void setDefaults(Properties props, String method) {
        final Properties method_props = OutputPropertiesFactory.getDefaultMethodProperties(method);
        {
            final Enumeration names = method_props.propertyNames();
            while (names.hasMoreElements()) {
                final String name = (String) names.nextElement();
                props.setProperty(name, method_props.getProperty(name));
            }
        }
    }

    /**
     * Verifies if a given output property name is a property defined in
     * the JAXP 1.1 / TrAX spec
     */
    private boolean validOutputProperty(String name) {
        return (name.equals(OutputKeys.ENCODING) || name.equals(OutputKeys.METHOD) || name.equals(OutputKeys.INDENT) || name.equals(OutputKeys.DOCTYPE_PUBLIC) || name.equals(OutputKeys.DOCTYPE_SYSTEM) || name.equals(OutputKeys.CDATA_SECTION_ELEMENTS) || name.equals(OutputKeys.MEDIA_TYPE) || name.equals(OutputKeys.OMIT_XML_DECLARATION) || name.equals(OutputKeys.STANDALONE) || name.equals(OutputKeys.VERSION) || name.charAt(0) == '{');
    }

    /**
     * Checks if a given output property is default (2nd layer only)
     */
    private boolean isDefaultProperty(String name, Properties properties) {
        return (properties.get(name) == null);
    }

    /**
     * Implements JAXP's Transformer.setParameter()
     * Add a parameter for the transformation. The parameter is simply passed
     * on to the translet - no validation is performed - so any unused
     * parameters are quitely ignored by the translet.
     *
     * @param name The name of the parameter
     * @param value The value to assign to the parameter
     */
    public void setParameter(String name, Object value) {
        if (value == null) {
            ErrorMsg err = new ErrorMsg(ErrorMsg.JAXP_INVALID_SET_PARAM_VALUE, name);
            throw new IllegalArgumentException(err.toString());
        }
        if (_isIdentity) {
            if (_parameters == null) {
                _parameters = new Hashtable();
            }
            _parameters.put(name, value);
        } else {
            _translet.addParameter(name, value);
        }
    }

    /**
     * Implements JAXP's Transformer.clearParameters()
     * Clear all parameters set with setParameter. Clears the translet's
     * parameter stack.
     */
    public void clearParameters() {
        if (_isIdentity && _parameters != null) {
            _parameters.clear();
        } else {
            _translet.clearParameters();
        }
    }

    /**
     * Implements JAXP's Transformer.getParameter()
     * Returns the value of a given parameter. Note that the translet will not
     * keep values for parameters that were not defined in the stylesheet.
     *
     * @param name The name of the parameter
     * @return An object that contains the value assigned to the parameter
     */
    public final Object getParameter(String name) {
        if (_isIdentity) {
            return (_parameters != null) ? _parameters.get(name) : null;
        } else {
            return _translet.getParameter(name);
        }
    }

    /**
     * Implements JAXP's Transformer.getURIResolver()
     * Set the object currently used to resolve URIs used in document().
     *
     * @return  The URLResolver object currently in use
     */
    public URIResolver getURIResolver() {
        return _uriResolver;
    }

    /**
     * Implements JAXP's Transformer.setURIResolver()
     * Set an object that will be used to resolve URIs used in document().
     *
     * @param resolver The URIResolver to use in document()
     */
    public void setURIResolver(URIResolver resolver) {
        _uriResolver = resolver;
    }

    /**
     * This class should only be used as a DOMCache for the translet if the
     * URIResolver has been set.
     *
     * The method implements XSLTC's DOMCache interface, which is used to
     * plug in an external document loader into a translet. This method acts
     * as an adapter between TrAX's URIResolver interface and XSLTC's
     * DOMCache interface. This approach is simple, but removes the
     * possibility of using external document caches with XSLTC.
     *
     * @param baseURI The base URI used by the document call.
     * @param href The href argument passed to the document function.
     * @param translet A reference to the translet requesting the document
     */
    public DOM retrieveDocument(String baseURI, String href, Translet translet) {
        try {
            if (href.length() == 0) {
                href = new String(baseURI);
            }
            Source resolvedSource = _uriResolver.resolve(href, baseURI);
            if (resolvedSource == null) {
                StreamSource streamSource = new StreamSource(SystemIDResolver.getAbsoluteURI(href, baseURI));
                return getDOM(streamSource);
            }
            return getDOM(resolvedSource);
        } catch (TransformerException e) {
            if (_errorListener != null) postErrorToListener("File not found: " + e.getMessage());
            return (null);
        }
    }

    /**
     * Receive notification of a recoverable error. 
     * The transformer must continue to provide normal parsing events after
     * invoking this method. It should still be possible for the application
     * to process the document through to the end.
     *
     * @param e The warning information encapsulated in a transformer 
     * exception.
     * @throws TransformerException if the application chooses to discontinue
     * the transformation (always does in our case).
     */
    public void error(TransformerException e) throws TransformerException {
        Throwable wrapped = e.getException();
        if (wrapped != null) {
            System.err.println(new ErrorMsg(ErrorMsg.ERROR_PLUS_WRAPPED_MSG, e.getMessageAndLocation(), wrapped.getMessage()));
        } else {
            System.err.println(new ErrorMsg(ErrorMsg.ERROR_MSG, e.getMessageAndLocation()));
        }
        throw e;
    }

    /**
     * Receive notification of a non-recoverable error. 
     * The application must assume that the transformation cannot continue
     * after the Transformer has invoked this method, and should continue
     * (if at all) only to collect addition error messages. In fact,
     * Transformers are free to stop reporting events once this method has
     * been invoked.
     *
     * @param e The warning information encapsulated in a transformer
     * exception.
     * @throws TransformerException if the application chooses to discontinue
     * the transformation (always does in our case).
     */
    public void fatalError(TransformerException e) throws TransformerException {
        Throwable wrapped = e.getException();
        if (wrapped != null) {
            System.err.println(new ErrorMsg(ErrorMsg.FATAL_ERR_PLUS_WRAPPED_MSG, e.getMessageAndLocation(), wrapped.getMessage()));
        } else {
            System.err.println(new ErrorMsg(ErrorMsg.FATAL_ERR_MSG, e.getMessageAndLocation()));
        }
        throw e;
    }

    /**
     * Receive notification of a warning.
     * Transformers can use this method to report conditions that are not
     * errors or fatal errors. The default behaviour is to take no action.
     * After invoking this method, the Transformer must continue with the
     * transformation. It should still be possible for the application to
     * process the document through to the end.
     *
     * @param e The warning information encapsulated in a transformer
     * exception.
     * @throws TransformerException if the application chooses to discontinue
     * the transformation (never does in our case).
     */
    public void warning(TransformerException e) throws TransformerException {
        Throwable wrapped = e.getException();
        if (wrapped != null) {
            System.err.println(new ErrorMsg(ErrorMsg.WARNING_PLUS_WRAPPED_MSG, e.getMessageAndLocation(), wrapped.getMessage()));
        } else {
            System.err.println(new ErrorMsg(ErrorMsg.WARNING_MSG, e.getMessageAndLocation()));
        }
    }

    /**
     * This method resets  the Transformer to its original configuration
     * Transformer code is reset to the same state it was when it was
     * created
     * @since 1.5
     */
    public void reset() {
        _method = null;
        _encoding = null;
        _sourceSystemId = null;
        _errorListener = this;
        _uriResolver = null;
        _dom = null;
        _parameters = null;
        _indentNumber = 0;
        setOutputProperties(null);
    }
}
