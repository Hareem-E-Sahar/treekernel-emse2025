package org.exist.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import org.apache.log4j.Logger;
import org.exist.EXistException;
import org.exist.Namespaces;
import org.exist.collections.Collection;
import org.exist.collections.IndexInfo;
import org.exist.collections.triggers.TriggerException;
import org.exist.debuggee.DebuggeeFactory;
import org.exist.dom.BinaryDocument;
import org.exist.dom.DefaultDocumentSet;
import org.exist.dom.DocumentImpl;
import org.exist.dom.DocumentMetadata;
import org.exist.dom.MutableDocumentSet;
import org.exist.dom.QName;
import org.exist.dom.XMLUtil;
import org.exist.http.servlets.HttpRequestWrapper;
import org.exist.http.servlets.HttpResponseWrapper;
import org.exist.http.servlets.ResponseWrapper;
import org.exist.memtree.ElementImpl;
import org.exist.memtree.NodeImpl;
import org.exist.memtree.SAXAdapter;
import org.exist.security.Permission;
import org.exist.security.PermissionDeniedException;
import org.exist.security.xacml.AccessContext;
import org.exist.source.DBSource;
import org.exist.source.Source;
import org.exist.source.StringSource;
import org.exist.source.URLSource;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.XQueryPool;
import org.exist.storage.lock.Lock;
import org.exist.storage.serializers.EXistOutputKeys;
import org.exist.storage.serializers.Serializer;
import org.exist.storage.serializers.Serializer.HttpContext;
import org.exist.storage.txn.TransactionException;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.util.LockException;
import org.exist.util.MimeTable;
import org.exist.util.MimeType;
import org.exist.util.VirtualTempFile;
import org.exist.util.VirtualTempFileInputSource;
import org.exist.util.serializer.SAXSerializer;
import org.exist.util.serializer.SerializerPool;
import org.exist.xmldb.XmldbURI;
import org.exist.xqj.Marshaller;
import org.exist.xquery.CompiledXQuery;
import org.exist.xquery.Constants;
import org.exist.xquery.NameTest;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQuery;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.functions.request.RequestModule;
import org.exist.xquery.functions.response.ResponseModule;
import org.exist.xquery.functions.session.SessionModule;
import org.exist.xquery.value.AnyURIValue;
import org.exist.xquery.value.DateTimeValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceIterator;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.exist.xupdate.Modification;
import org.exist.xupdate.XUpdateProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author wolf
 * @author ljo
 * @author adam
 * @author gev
 *
 */
public class RESTServer {

    protected static final Logger LOG = Logger.getLogger(RESTServer.class);

    protected static final Properties defaultProperties = new Properties();

    static {
        defaultProperties.setProperty(OutputKeys.INDENT, "yes");
        defaultProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
        defaultProperties.setProperty(OutputKeys.MEDIA_TYPE, MimeType.XML_TYPE.getName());
        defaultProperties.setProperty(EXistOutputKeys.EXPAND_XINCLUDES, "yes");
        defaultProperties.setProperty(EXistOutputKeys.HIGHLIGHT_MATCHES, "elements");
        defaultProperties.setProperty(EXistOutputKeys.PROCESS_XSL_PI, "yes");
    }

    protected static final Properties defaultOutputKeysProperties = new Properties();

    static {
        defaultOutputKeysProperties.setProperty(OutputKeys.INDENT, "yes");
        defaultOutputKeysProperties.setProperty(OutputKeys.ENCODING, "UTF-8");
        defaultOutputKeysProperties.setProperty(OutputKeys.MEDIA_TYPE, MimeType.XML_TYPE.getName());
    }

    private static final String QUERY_ERROR_HEAD = "<html>" + "<head>" + "<title>Query Error</title>" + "<style type=\"text/css\">" + ".errmsg {" + "  border: 1px solid black;" + "  padding: 15px;" + "  margin-left: 20px;" + "  margin-right: 20px;" + "}" + "h1 { color: #C0C0C0; }" + ".path {" + "  padding-bottom: 10px;" + "}" + ".high { " + "  color: #666699; " + "  font-weight: bold;" + "}" + "</style>" + "</head>" + "<body>" + "<h1>XQuery Error</h1>";

    private String formEncoding;

    private String containerEncoding;

    private boolean useDynamicContentType;

    private boolean safeMode = false;

    private SessionManager sessionManager;

    public RESTServer(BrokerPool pool, String formEncoding, String containerEncoding, boolean useDynamicContentType, boolean safeMode) {
        this.formEncoding = formEncoding;
        this.containerEncoding = containerEncoding;
        this.useDynamicContentType = useDynamicContentType;
        this.safeMode = safeMode;
        this.sessionManager = new SessionManager(pool);
    }

    /**
	 * Handle GET request. In the simplest case just returns the document or
	 * binary resource specified in the path. If the path leads to a collection,
	 * a listing of the collection contents is returned. If it resolves to a
	 * binary resource with mime-type "application/xquery", this resource will
	 * be loaded and executed by the XQuery engine.
	 *
	 * The method also recognizes a number of predefined parameters:
	 *
	 * <ul>
	 * <li>_xpath or _query: if specified, the given query is executed on the
	 * current resource or collection.</li>
	 *
	 * <li>_howmany: defines how many items from the query result will be
	 * returned.</li>
	 *
	 * <li>_start: a start offset into the result set.</li>
	 *
	 * <li>_wrap: if set to "yes", the query results will be wrapped into a
	 * exist:result element.</li>
	 *
	 * <li>_indent: if set to "yes", the returned XML will be pretty-printed.
	 * </li>
	 *
	 * <li>_source: if set to "yes" and a resource with mime-type
	 * "application/xquery" is requested then the xquery will not be executed,
	 * instead the source of the document will be returned. Must be enabled in
	 * descriptor.xml with the following syntax <xquery-app><allow-source><xquery
	 * path="/db/mycollection/myquery.xql"/></allow-source></xquery-app> </li>
	 *
	 * <li>_xsl: an URI pointing to an XSL stylesheet that will be applied to
	 * the returned XML.</li>
	 *
	 * @param broker
	 * @param request
	 * @param response
	 * @param path
	 * @throws BadRequestException
	 * @throws PermissionDeniedException
	 * @throws NotFoundException
	 */
    public void doGet(DBBroker broker, HttpServletRequest request, HttpServletResponse response, String path) throws BadRequestException, PermissionDeniedException, NotFoundException, IOException {
        if (request.getCharacterEncoding() == null) request.setCharacterEncoding(formEncoding);
        String option;
        if ((option = request.getParameter("_release")) != null) {
            int sessionId = Integer.parseInt(option);
            sessionManager.release(sessionId);
            if (LOG.isDebugEnabled()) LOG.debug("Released session " + sessionId);
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        int howmany = 10;
        int start = 1;
        boolean typed = false;
        boolean wrap = true;
        boolean source = false;
        boolean cache = false;
        Properties outputProperties = new Properties(defaultOutputKeysProperties);
        String query = null;
        if (!safeMode) {
            request.getParameter("_xpath");
            if (query == null) {
                query = request.getParameter("_query");
            }
        }
        String _var = request.getParameter("_variables");
        List namespaces = null;
        ElementImpl variables = null;
        try {
            if (_var != null) {
                NamespaceExtractor nsExtractor = new NamespaceExtractor();
                variables = parseXML(_var, nsExtractor);
                namespaces = nsExtractor.getNamespaces();
            }
        } catch (SAXException e) {
            XPathException x = new XPathException(e.toString());
            writeXPathException(response, HttpServletResponse.SC_BAD_REQUEST, "UTF-8", query, path, x);
        } catch (ParserConfigurationException e) {
            XPathException x = new XPathException(e.toString());
            writeXPathException(response, HttpServletResponse.SC_BAD_REQUEST, "UTF-8", query, path, x);
        }
        if ((option = request.getParameter("_howmany")) != null) {
            try {
                howmany = Integer.parseInt(option);
            } catch (NumberFormatException nfe) {
                throw new BadRequestException("Parameter _howmany should be an int");
            }
        }
        if ((option = request.getParameter("_start")) != null) {
            try {
                start = Integer.parseInt(option);
            } catch (NumberFormatException nfe) {
                throw new BadRequestException("Parameter _start should be an int");
            }
        }
        if ((option = request.getParameter("_typed")) != null) {
            if (option.toLowerCase().equals("yes")) typed = true;
        }
        if ((option = request.getParameter("_wrap")) != null) {
            wrap = option.equals("yes");
            outputProperties.setProperty("_wrap", option);
        }
        if ((option = request.getParameter("_cache")) != null) {
            cache = option.equals("yes");
        }
        if ((option = request.getParameter("_indent")) != null) {
            outputProperties.setProperty(OutputKeys.INDENT, option);
        }
        if ((option = request.getParameter("_source")) != null && !safeMode) {
            source = option.equals("yes");
        }
        if ((option = request.getParameter("_session")) != null) {
            outputProperties.setProperty(Serializer.PROPERTY_SESSION_ID, option);
        }
        String stylesheet;
        if ((stylesheet = request.getParameter("_xsl")) != null) {
            if (stylesheet.equals("no")) {
                outputProperties.setProperty(EXistOutputKeys.PROCESS_XSL_PI, "no");
                outputProperties.remove(EXistOutputKeys.STYLESHEET);
                stylesheet = null;
            } else {
                outputProperties.setProperty(EXistOutputKeys.STYLESHEET, stylesheet);
            }
        } else {
            outputProperties.setProperty(EXistOutputKeys.PROCESS_XSL_PI, "yes");
        }
        LOG.debug("stylesheet = " + stylesheet);
        LOG.debug("query = " + query);
        String encoding;
        if ((encoding = request.getParameter("_encoding")) != null) outputProperties.setProperty(OutputKeys.ENCODING, encoding); else encoding = "UTF-8";
        String mimeType = outputProperties.getProperty(OutputKeys.MEDIA_TYPE);
        if (query != null) {
            try {
                search(broker, query, path, namespaces, variables, howmany, start, typed, outputProperties, wrap, cache, request, response);
            } catch (XPathException e) {
                if (MimeType.XML_TYPE.getName().equals(mimeType)) {
                    writeXPathException(response, HttpServletResponse.SC_BAD_REQUEST, encoding, query, path, e);
                } else {
                    writeXPathExceptionHtml(response, HttpServletResponse.SC_BAD_REQUEST, encoding, query, path, e);
                }
            }
            return;
        }
        DocumentImpl resource = null;
        XmldbURI pathUri = XmldbURI.create(URLDecoder.decode(path, "UTF-8"));
        try {
            String xquery_mime_type = MimeType.XQUERY_TYPE.getName();
            String xproc_mime_type = MimeType.XPROC_TYPE.getName();
            resource = broker.getXMLResource(pathUri, Lock.READ_LOCK);
            if (null != resource && !isExecutableType(resource)) {
                writeResourceAs(resource, broker, stylesheet, encoding, null, outputProperties, request, response);
                return;
            }
            if (resource == null) {
                Collection collection = broker.getCollection(pathUri);
                if (collection != null) {
                    if (safeMode || !collection.getPermissions().validate(broker.getSubject(), Permission.READ)) throw new PermissionDeniedException("Not allowed to read collection");
                    try {
                        writeCollection(response, encoding, broker, collection);
                        return;
                    } catch (LockException le) {
                        if (MimeType.XML_TYPE.getName().equals(mimeType)) {
                            writeXPathException(response, HttpServletResponse.SC_BAD_REQUEST, encoding, query, path, new XPathException(le.getMessage(), le));
                        } else {
                            writeXPathExceptionHtml(response, HttpServletResponse.SC_BAD_REQUEST, encoding, query, path, new XPathException(le.getMessage(), le));
                        }
                    }
                } else if (source) {
                    throw new NotFoundException("Document " + path + " not found");
                }
            }
            XmldbURI servletPath = pathUri;
            while (null == resource) {
                servletPath = servletPath.removeLastSegment();
                if (servletPath == XmldbURI.EMPTY_URI) break;
                resource = broker.getXMLResource(servletPath, Lock.READ_LOCK);
                if (null != resource && isExecutableType(resource)) {
                    break;
                } else if (null != resource) {
                    throw new NotFoundException("Document " + path + " not found");
                }
            }
            if (null == resource) {
                throw new NotFoundException("Document " + path + " not found");
            }
            String pathInfo = pathUri.trimFromBeginning(servletPath).toString();
            Descriptor descriptor = Descriptor.getDescriptorSingleton();
            if (source) {
                if ((null != descriptor) && descriptor.allowSource(path) && resource.getPermissions().validate(broker.getSubject(), Permission.READ)) {
                    if (xquery_mime_type.equals(resource.getMetadata().getMimeType())) {
                        writeResourceAs(resource, broker, stylesheet, encoding, MimeType.TEXT_TYPE.getName(), outputProperties, request, response);
                    } else if (xproc_mime_type.equals(resource.getMetadata().getMimeType())) {
                        writeResourceAs(resource, broker, stylesheet, encoding, MimeType.XML_TYPE.getName(), outputProperties, request, response);
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Permission to view XQuery source for: " + path + " denied. Must be explicitly defined in descriptor.xml");
                    return;
                }
            } else {
                try {
                    if (xquery_mime_type.equals(resource.getMetadata().getMimeType())) {
                        executeXQuery(broker, resource, request, response, outputProperties, servletPath.toString(), pathInfo);
                    } else if (xproc_mime_type.equals(resource.getMetadata().getMimeType())) {
                        executeXProc(broker, resource, request, response, outputProperties, servletPath.toString(), pathInfo);
                    }
                } catch (XPathException e) {
                    if (LOG.isDebugEnabled()) LOG.debug(e.getMessage(), e);
                    if (MimeType.XML_TYPE.getName().equals(mimeType)) {
                        writeXPathException(response, HttpServletResponse.SC_BAD_REQUEST, encoding, query, path, e);
                    } else {
                        writeXPathExceptionHtml(response, HttpServletResponse.SC_BAD_REQUEST, encoding, query, path, e);
                    }
                }
            }
        } finally {
            if (resource != null) resource.getUpdateLock().release(Lock.READ_LOCK);
        }
    }

    public void doHead(DBBroker broker, HttpServletRequest request, HttpServletResponse response, String path) throws BadRequestException, PermissionDeniedException, NotFoundException, IOException {
        Properties outputProperties = new Properties(defaultOutputKeysProperties);
        @SuppressWarnings("unused") String mimeType = outputProperties.getProperty(OutputKeys.MEDIA_TYPE);
        String encoding;
        if ((encoding = request.getParameter("_encoding")) != null) outputProperties.setProperty(OutputKeys.ENCODING, encoding); else encoding = "UTF-8";
        DocumentImpl resource = null;
        XmldbURI pathUri = XmldbURI.create(path);
        try {
            resource = broker.getXMLResource(pathUri, Lock.READ_LOCK);
            if (resource != null) {
                if (!resource.getPermissions().validate(broker.getSubject(), Permission.READ)) {
                    throw new PermissionDeniedException("Permission to read resource " + path + " denied");
                }
                DocumentMetadata metadata = resource.getMetadata();
                response.setContentType(metadata.getMimeType());
                response.addHeader("Content-Length", Long.toString(resource.getContentLength()));
                setCreatedAndLastModifiedHeaders(response, metadata.getCreated(), metadata.getLastModified());
            } else {
                Collection col = broker.getCollection(pathUri);
                if (col == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "No resource at location: " + path);
                    return;
                }
                if (!col.getPermissions().validate(broker.getSubject(), Permission.READ)) {
                    throw new PermissionDeniedException("Permission to read resource " + path + " denied");
                }
                response.setContentType(MimeType.XML_TYPE.getName() + "; charset=" + encoding);
                setCreatedAndLastModifiedHeaders(response, col.getCreationTime(), col.getCreationTime());
            }
        } finally {
            if (resource != null) resource.getUpdateLock().release(Lock.READ_LOCK);
        }
    }

    /**
	 * Handles POST requests. If the path leads to a binary resource with
	 * mime-type "application/xquery", that resource will be read and executed
	 * by the XQuery engine. Otherwise, the request content is loaded and parsed
	 * as XML. It may either contain an XUpdate or a query request.
	 *
	 * @param broker
	 * @param request
	 * @param response
	 * @param path
	 * @throws BadRequestException
	 * @throws PermissionDeniedException
	 * @throws NotFoundException 
	 */
    public void doPost(DBBroker broker, HttpServletRequest request, HttpServletResponse response, String path) throws BadRequestException, PermissionDeniedException, IOException, NotFoundException {
        if (request.getCharacterEncoding() == null) request.setCharacterEncoding(formEncoding);
        Properties outputProperties = new Properties(defaultOutputKeysProperties);
        XmldbURI pathUri = XmldbURI.create(path);
        DocumentImpl resource = null;
        String encoding = outputProperties.getProperty(OutputKeys.ENCODING);
        String mimeType = outputProperties.getProperty(OutputKeys.MEDIA_TYPE);
        try {
            String xquery_mime_type = MimeType.XQUERY_TYPE.getName();
            String xproc_mime_type = MimeType.XPROC_TYPE.getName();
            resource = broker.getXMLResource(pathUri, Lock.READ_LOCK);
            XmldbURI servletPath = pathUri;
            while (null == resource) {
                servletPath = servletPath.removeLastSegment();
                if (servletPath == XmldbURI.EMPTY_URI) break;
                resource = broker.getXMLResource(servletPath, Lock.READ_LOCK);
                if (null != resource && (resource.getResourceType() == DocumentImpl.BINARY_FILE && xquery_mime_type.equals(resource.getMetadata().getMimeType()) || resource.getResourceType() == DocumentImpl.XML_FILE && xproc_mime_type.equals(resource.getMetadata().getMimeType()))) {
                    break;
                } else if (null != resource) {
                    resource.getUpdateLock().release(Lock.READ_LOCK);
                    resource = null;
                    break;
                }
            }
            if (resource != null) {
                if (resource.getResourceType() == DocumentImpl.BINARY_FILE && xquery_mime_type.equals(resource.getMetadata().getMimeType()) || resource.getResourceType() == DocumentImpl.XML_FILE && xproc_mime_type.equals(resource.getMetadata().getMimeType())) {
                    String pathInfo = pathUri.trimFromBeginning(servletPath).toString();
                    try {
                        if (xquery_mime_type.equals(resource.getMetadata().getMimeType())) {
                            executeXQuery(broker, resource, request, response, outputProperties, servletPath.toString(), pathInfo);
                        } else {
                            executeXProc(broker, resource, request, response, outputProperties, servletPath.toString(), pathInfo);
                        }
                    } catch (XPathException e) {
                        if (MimeType.XML_TYPE.getName().equals(mimeType)) {
                            writeXPathException(response, HttpServletResponse.SC_BAD_REQUEST, encoding, null, path, e);
                        } else {
                            writeXPathExceptionHtml(response, HttpServletResponse.SC_BAD_REQUEST, encoding, null, path, e);
                        }
                    }
                    return;
                }
            }
        } finally {
            if (resource != null) resource.getUpdateLock().release(Lock.READ_LOCK);
        }
        String requestType = request.getContentType();
        if (requestType != null) {
            int semicolon = requestType.indexOf(';');
            if (semicolon > 0) requestType = requestType.substring(0, semicolon).trim();
        }
        if (requestType == null || !requestType.equals(MimeType.URL_ENCODED_TYPE.getName())) {
            int howmany = 10;
            int start = 1;
            boolean typed = false;
            ElementImpl variables = null;
            boolean enclose = true;
            boolean cache = false;
            @SuppressWarnings("unused") String mime = MimeType.XML_TYPE.getName();
            String query = null;
            TransactionManager transact = broker.getBrokerPool().getTransactionManager();
            Txn transaction = transact.beginTransaction();
            try {
                String content = getRequestContent(request);
                NamespaceExtractor nsExtractor = new NamespaceExtractor();
                ElementImpl root = parseXML(content, nsExtractor);
                String rootNS = root.getNamespaceURI();
                if (rootNS != null && rootNS.equals(Namespaces.EXIST_NS)) {
                    if (root.getLocalName().equals("query")) {
                        String option = root.getAttribute("start");
                        if (option != null) try {
                            start = Integer.parseInt(option);
                        } catch (NumberFormatException e) {
                        }
                        option = root.getAttribute("max");
                        if (option != null) try {
                            howmany = Integer.parseInt(option);
                        } catch (NumberFormatException e) {
                        }
                        option = root.getAttribute("enclose");
                        if (option != null) {
                            if (option.equals("no")) enclose = false;
                        } else {
                            option = root.getAttribute("wrap");
                            if (option != null) {
                                if (option.equals("no")) enclose = false;
                            }
                        }
                        option = root.getAttribute("typed");
                        if (option != null) {
                            if (option.equals("yes")) typed = true;
                        }
                        option = root.getAttribute("mime");
                        mime = MimeType.XML_TYPE.getName();
                        if ((option != null) && (!option.equals(""))) {
                            mime = option;
                        }
                        if ((option = root.getAttribute("cache")) != null) {
                            cache = option.equals("yes");
                        }
                        if ((option = root.getAttribute("session")) != null && option.length() > 0) {
                            outputProperties.setProperty(Serializer.PROPERTY_SESSION_ID, option);
                        }
                        NodeList children = root.getChildNodes();
                        for (int i = 0; i < children.getLength(); i++) {
                            Node child = children.item(i);
                            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNamespaceURI().equals(Namespaces.EXIST_NS)) {
                                if (child.getLocalName().equals("text")) {
                                    StringBuilder buf = new StringBuilder();
                                    Node next = child.getFirstChild();
                                    while (next != null) {
                                        if (next.getNodeType() == Node.TEXT_NODE || next.getNodeType() == Node.CDATA_SECTION_NODE) buf.append(next.getNodeValue());
                                        next = next.getNextSibling();
                                    }
                                    query = buf.toString();
                                } else if (child.getLocalName().equals("variables")) {
                                    variables = (ElementImpl) child;
                                } else if (child.getLocalName().equals("properties")) {
                                    Node node = child.getFirstChild();
                                    while (node != null) {
                                        if (node.getNodeType() == Node.ELEMENT_NODE && node.getNamespaceURI().equals(Namespaces.EXIST_NS) && node.getLocalName().equals("property")) {
                                            Element property = (Element) node;
                                            String key = property.getAttribute("name");
                                            String value = property.getAttribute("value");
                                            LOG.debug(key + " = " + value);
                                            if (key != null && value != null) outputProperties.setProperty(key, value);
                                        }
                                        node = node.getNextSibling();
                                    }
                                }
                            }
                        }
                    }
                    if (query != null) {
                        try {
                            search(broker, query, path, nsExtractor.getNamespaces(), variables, howmany, start, typed, outputProperties, enclose, cache, request, response);
                            transact.commit(transaction);
                        } catch (XPathException e) {
                            if (MimeType.XML_TYPE.getName().equals(mimeType)) {
                                writeXPathException(response, HttpServletResponse.SC_ACCEPTED, encoding, null, path, e);
                            } else {
                                writeXPathExceptionHtml(response, HttpServletResponse.SC_ACCEPTED, encoding, null, path, e);
                            }
                        }
                    } else {
                        transact.abort(transaction);
                        throw new BadRequestException("No query specified");
                    }
                } else if (rootNS != null && rootNS.equals(XUpdateProcessor.XUPDATE_NS)) {
                    LOG.debug("Got xupdate request: " + content);
                    MutableDocumentSet docs = new DefaultDocumentSet();
                    Collection collection = broker.getCollection(pathUri);
                    if (collection != null) {
                        collection.allDocs(broker, docs, true);
                    } else {
                        DocumentImpl xupdateDoc = broker.getResource(pathUri, Permission.READ);
                        if (xupdateDoc != null) {
                            docs.add(xupdateDoc);
                        } else broker.getAllXMLResources(docs);
                    }
                    XUpdateProcessor processor = new XUpdateProcessor(broker, docs, AccessContext.REST);
                    Modification modifications[] = processor.parse(new InputSource(new StringReader(content)));
                    long mods = 0;
                    for (int i = 0; i < modifications.length; i++) {
                        mods += modifications[i].process(transaction);
                        broker.flush();
                    }
                    transact.commit(transaction);
                    writeXUpdateResult(response, encoding, mods);
                } else {
                    transact.abort(transaction);
                    throw new BadRequestException("Unknown XML root element: " + root.getNodeName());
                }
            } catch (SAXException e) {
                transact.abort(transaction);
                Exception cause = e;
                if (e.getException() != null) cause = e.getException();
                LOG.debug("SAX exception while parsing request: " + cause.getMessage(), cause);
                throw new BadRequestException("SAX exception while parsing request: " + cause.getMessage());
            } catch (ParserConfigurationException e) {
                transact.abort(transaction);
                throw new BadRequestException("Parser exception while parsing request: " + e.getMessage());
            } catch (XPathException e) {
                transact.abort(transaction);
                throw new BadRequestException("Query exception while parsing request: " + e.getMessage());
            } catch (IOException e) {
                transact.abort(transaction);
                throw new BadRequestException("IO exception while parsing request: " + e.getMessage());
            } catch (EXistException e) {
                transact.abort(transaction);
                throw new BadRequestException(e.getMessage());
            } catch (LockException e) {
                transact.abort(transaction);
                throw new PermissionDeniedException(e.getMessage());
            }
        } else {
            doGet(broker, request, response, path);
        }
    }

    private ElementImpl parseXML(String content, NamespaceExtractor nsExtractor) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        InputSource src = new InputSource(new StringReader(content));
        SAXParser parser = factory.newSAXParser();
        XMLReader reader = parser.getXMLReader();
        SAXAdapter adapter = new SAXAdapter();
        nsExtractor.setContentHandler(adapter);
        nsExtractor.setParent(reader);
        nsExtractor.parse(src);
        Document doc = adapter.getDocument();
        return (ElementImpl) doc.getDocumentElement();
    }

    private class NamespaceExtractor extends XMLFilterImpl {

        List<Namespace> namespaces = new ArrayList<Namespace>();

        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            if (!Namespaces.EXIST_NS.equals(uri)) {
                Namespace ns = new Namespace(prefix, uri);
                namespaces.add(ns);
            }
            super.startPrefixMapping(prefix, uri);
        }

        public List<Namespace> getNamespaces() {
            return namespaces;
        }
    }

    private class Namespace {

        private String prefix = null;

        private String uri = null;

        public Namespace(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getUri() {
            return uri;
        }
    }

    /**
	 * Creates an input source from a URL location with an optional known
	 * charset.
	 */
    private InputSource createInputSource(String charset, URI location) throws java.io.IOException {
        if (charset == null) {
            return new InputSource(location.toASCIIString());
        } else {
            InputSource source = new InputSource(new InputStreamReader(location.toURL().openStream(), charset));
            source.setSystemId(location.toASCIIString());
            return source;
        }
    }

    /**
	 * Handles PUT requests. The request content is stored as a new resource at
	 * the specified location. If the resource already exists, it is overwritten
	 * if the user has write permissions.
	 *
	 * The resource type depends on the content type specified in the HTTP
	 * header. The content type will be looked up in the global mime table. If
	 * the corresponding mime type is not a know XML mime type, the resource
	 * will be stored as a binary resource.
	 *
	 * @param broker
	 * @param tempFile
	 *            The temp file from which the PUT will get its content
	 * @param path
	 *            The path to which the file should be stored
	 * @param request
	 * @param response
	 * @throws BadRequestException
	 * @throws PermissionDeniedException
	 */
    public void doPut(DBBroker broker, File tempFile, XmldbURI path, HttpServletRequest request, HttpServletResponse response) throws BadRequestException, PermissionDeniedException, IOException {
        doPut(broker, (tempFile != null) ? new VirtualTempFile(tempFile) : null, path, request, response);
    }

    /**
	 * Handles PUT requests. The request content is stored as a new resource at
	 * the specified location. If the resource already exists, it is overwritten
	 * if the user has write permissions.
	 *
	 * The resource type depends on the content type specified in the HTTP
	 * header. The content type will be looked up in the global mime table. If
	 * the corresponding mime type is not a know XML mime type, the resource
	 * will be stored as a binary resource.
	 *
	 * @param broker
	 * @param vtempFile
	 *            The virtual temp file from which the PUT will get its content
	 * @param path
	 *            The path to which the file should be stored
	 * @param request
	 * @param response
	 * @throws BadRequestException
	 * @throws PermissionDeniedException
	 */
    public void doPut(DBBroker broker, VirtualTempFile vtempFile, XmldbURI path, HttpServletRequest request, HttpServletResponse response) throws BadRequestException, PermissionDeniedException, IOException {
        if (vtempFile == null) throw new BadRequestException("No request content found for PUT");
        TransactionManager transact = broker.getBrokerPool().getTransactionManager();
        Txn transaction = null;
        try {
            XmldbURI docUri = path.lastSegment();
            XmldbURI collUri = path.removeLastSegment();
            if (docUri == null || collUri == null) {
                throw new BadRequestException("Bad path: " + path);
            }
            Collection collection = broker.getCollection(collUri);
            if (collection == null) {
                LOG.debug("creating collection " + collUri);
                transaction = transact.beginTransaction();
                collection = broker.getOrCreateCollection(transaction, collUri);
                broker.saveCollection(transaction, collection);
            }
            MimeType mime;
            String contentType = request.getContentType();
            String charset = null;
            if (contentType != null) {
                int semicolon = contentType.indexOf(';');
                if (semicolon > 0) {
                    contentType = contentType.substring(0, semicolon).trim();
                    int equals = contentType.indexOf('=', semicolon);
                    if (equals > 0) {
                        String param = contentType.substring(semicolon + 1, equals).trim();
                        if (param.compareToIgnoreCase("charset=") == 0) {
                            charset = param.substring(equals + 1).trim();
                        }
                    }
                }
                mime = MimeTable.getInstance().getContentType(contentType);
            } else {
                mime = MimeTable.getInstance().getContentTypeFor(docUri);
                if (mime != null) contentType = mime.getName();
            }
            if (mime == null) {
                mime = MimeType.BINARY_TYPE;
                contentType = mime.getName();
            }
            if (transaction == null) transaction = transact.beginTransaction();
            if (mime.isXMLType()) {
                InputSource vtfis = new VirtualTempFileInputSource(vtempFile, charset);
                IndexInfo info = collection.validateXMLResource(transaction, broker, docUri, vtfis);
                info.getDocument().getMetadata().setMimeType(contentType);
                collection.store(transaction, broker, info, vtfis, false);
                response.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                InputStream is = vtempFile.getByteStream();
                try {
                    collection.addBinaryResource(transaction, broker, docUri, is, contentType, vtempFile.length());
                } finally {
                    is.close();
                }
                response.setStatus(HttpServletResponse.SC_CREATED);
            }
            transact.commit(transaction);
        } catch (SAXParseException e) {
            transact.abort(transaction);
            throw new BadRequestException("Parsing exception at " + e.getLineNumber() + "/" + e.getColumnNumber() + ": " + e.toString());
        } catch (TriggerException e) {
            transact.abort(transaction);
            throw new PermissionDeniedException(e.getMessage());
        } catch (SAXException e) {
            transact.abort(transaction);
            Exception o = e.getException();
            if (o == null) o = e;
            throw new BadRequestException("Parsing exception: " + o.getMessage());
        } catch (EXistException e) {
            transact.abort(transaction);
            throw new BadRequestException("Internal error: " + e.getMessage());
        } catch (LockException e) {
            transact.abort(transaction);
            throw new PermissionDeniedException(e.getMessage());
        }
        return;
    }

    public void doDelete(DBBroker broker, XmldbURI path, HttpServletResponse response) throws PermissionDeniedException, NotFoundException, IOException {
        TransactionManager transact = broker.getBrokerPool().getTransactionManager();
        Txn txn = null;
        try {
            Collection collection = broker.getCollection(path);
            if (collection != null) {
                LOG.debug("removing collection " + path);
                txn = transact.beginTransaction();
                broker.removeCollection(txn, collection);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                DocumentImpl doc = (DocumentImpl) broker.getResource(path, Permission.WRITE);
                if (doc == null) {
                    throw new NotFoundException("No document or collection found for path: " + path);
                } else {
                    LOG.debug("removing document " + path);
                    txn = transact.beginTransaction();
                    if (doc.getResourceType() == DocumentImpl.BINARY_FILE) doc.getCollection().removeBinaryResource(txn, broker, path.lastSegment()); else doc.getCollection().removeXMLResource(txn, broker, path.lastSegment());
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            }
            if (txn != null) transact.commit(txn);
        } catch (TriggerException e) {
            transact.abort(txn);
            throw new PermissionDeniedException("Trigger failed: " + e.getMessage());
        } catch (LockException e) {
            transact.abort(txn);
            throw new PermissionDeniedException("Could not acquire lock: " + e.getMessage());
        } catch (TransactionException e) {
            transact.abort(txn);
            LOG.warn("Transaction aborted: " + e.getMessage(), e);
        }
    }

    private String getRequestContent(HttpServletRequest request) throws IOException {
        String encoding = request.getCharacterEncoding();
        if (encoding == null) encoding = "UTF-8";
        InputStream is = request.getInputStream();
        Reader reader = new InputStreamReader(is, encoding);
        StringWriter content = new StringWriter();
        char ch[] = new char[4096];
        int len = 0;
        while ((len = reader.read(ch)) > -1) content.write(ch, 0, len);
        String xml = content.toString();
        return xml;
    }

    /**
	 * TODO: pass request and response objects to XQuery.
	 *
	 * @throws XPathException
	 */
    protected void search(DBBroker broker, String query, String path, List<Namespace> namespaces, ElementImpl variables, int howmany, int start, boolean typed, Properties outputProperties, boolean wrap, boolean cache, HttpServletRequest request, HttpServletResponse response) throws BadRequestException, PermissionDeniedException, XPathException {
        String sessionIdParam = outputProperties.getProperty(Serializer.PROPERTY_SESSION_ID);
        if (sessionIdParam != null) {
            try {
                int sessionId = Integer.parseInt(sessionIdParam);
                if (sessionId > -1) {
                    Sequence cached = sessionManager.get(query, sessionId);
                    if (cached != null) {
                        LOG.debug("Returning cached query result");
                        writeResults(response, broker, cached, howmany, start, typed, outputProperties, wrap);
                    } else {
                        LOG.debug("Cached query result not found. Probably timed out. Repeating query.");
                    }
                }
            } catch (NumberFormatException e) {
                throw new BadRequestException("Invalid session id passed in query request: " + sessionIdParam);
            }
        }
        XmldbURI pathUri = XmldbURI.create(path);
        try {
            Source source = new StringSource(query);
            XQuery xquery = broker.getXQueryService();
            XQueryPool pool = xquery.getXQueryPool();
            CompiledXQuery compiled = pool.borrowCompiledXQuery(broker, source);
            XQueryContext context;
            if (compiled == null) context = xquery.newContext(AccessContext.REST); else context = compiled.getContext();
            context.setStaticallyKnownDocuments(new XmldbURI[] { pathUri });
            context.setBaseURI(new AnyURIValue(pathUri.toString()));
            declareNamespaces(context, namespaces);
            declareVariables(context, variables, request, response);
            if (compiled == null) compiled = xquery.compile(context, source); else {
                compiled.getContext().updateContext(context);
                context.getWatchDog().reset();
            }
            try {
                long startTime = System.currentTimeMillis();
                Sequence resultSequence = xquery.execute(compiled, null, outputProperties);
                long queryTime = System.currentTimeMillis() - startTime;
                if (LOG.isDebugEnabled()) LOG.debug("Found " + resultSequence.getItemCount() + " in " + queryTime + "ms.");
                if (cache) {
                    int sessionId = sessionManager.add(query, resultSequence);
                    outputProperties.setProperty(Serializer.PROPERTY_SESSION_ID, Integer.toString(sessionId));
                    if (!response.isCommitted()) response.setIntHeader("X-Session-Id", sessionId);
                }
                writeResults(response, broker, resultSequence, howmany, start, typed, outputProperties, wrap);
            } finally {
                pool.returnCompiledXQuery(source, compiled);
            }
        } catch (IOException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    private void declareNamespaces(XQueryContext context, List<Namespace> namespaces) throws XPathException {
        if (namespaces == null) return;
        for (Namespace ns : namespaces) {
            context.declareNamespace(ns.getPrefix(), ns.getUri());
        }
    }

    /**
	 * Pass the request, response and session objects to the XQuery context.
	 *
	 * @param context
	 * @param request
	 * @param response
	 * @throws XPathException
	 */
    private HttpRequestWrapper declareVariables(XQueryContext context, ElementImpl variables, HttpServletRequest request, HttpServletResponse response) throws XPathException {
        HttpRequestWrapper reqw = new HttpRequestWrapper(request, formEncoding, containerEncoding);
        ResponseWrapper respw = new HttpResponseWrapper(response);
        context.declareVariable(RequestModule.PREFIX + ":request", reqw);
        context.declareVariable(ResponseModule.PREFIX + ":response", respw);
        context.declareVariable(SessionModule.PREFIX + ":session", reqw.getSession(false));
        if (variables != null) {
            declareExternalAndXQJVariables(context, variables);
        }
        return reqw;
    }

    private void declareExternalAndXQJVariables(XQueryContext context, ElementImpl variables) throws XPathException {
        ValueSequence varSeq = new ValueSequence();
        variables.selectChildren(new NameTest(Type.ELEMENT, new QName("variable", Namespaces.EXIST_NS)), varSeq);
        for (SequenceIterator i = varSeq.iterate(); i.hasNext(); ) {
            ElementImpl variable = (ElementImpl) i.nextItem();
            ElementImpl qname = (ElementImpl) variable.getFirstChild(new NameTest(Type.ELEMENT, new QName("qname", Namespaces.EXIST_NS)));
            String localname = null, prefix = null, uri = null;
            NodeImpl child = (NodeImpl) qname.getFirstChild();
            while (child != null) {
                if (child.getLocalName().equals("localname")) {
                    localname = child.getStringValue();
                } else if (child.getLocalName().equals("namespace")) {
                    uri = child.getStringValue();
                } else if (child.getLocalName().equals("prefix")) {
                    prefix = child.getStringValue();
                }
                child = (NodeImpl) child.getNextSibling();
            }
            if (uri != null && prefix != null) context.declareNamespace(prefix, uri);
            if (localname == null) continue;
            QName q;
            if (prefix != null && localname != null) {
                q = new QName(localname, uri, prefix);
            } else {
                q = new QName(localname, uri, XMLConstants.DEFAULT_NS_PREFIX);
            }
            NodeImpl value = variable.getFirstChild(new NameTest(Type.ELEMENT, Marshaller.ROOT_ELEMENT_QNAME));
            Sequence sequence;
            try {
                sequence = value == null ? Sequence.EMPTY_SEQUENCE : Marshaller.demarshall(value);
            } catch (XMLStreamException xe) {
                throw new XPathException(xe.toString());
            }
            if (prefix != null) {
                context.declareVariable(q.getPrefix() + ":" + q.getLocalName(), sequence);
            } else {
                context.declareVariable(q.getLocalName(), sequence);
            }
        }
    }

    /**
	 * Directly execute an XQuery stored as a binary document in the database.
	 * @throws PermissionDeniedException 
	 */
    private void executeXQuery(DBBroker broker, DocumentImpl resource, HttpServletRequest request, HttpServletResponse response, Properties outputProperties, String servletPath, String pathInfo) throws XPathException, BadRequestException, PermissionDeniedException {
        Source source = new DBSource(broker, (BinaryDocument) resource, true);
        XQuery xquery = broker.getXQueryService();
        XQueryPool pool = xquery.getXQueryPool();
        XQueryContext context;
        CompiledXQuery compiled = pool.borrowCompiledXQuery(broker, source);
        if (compiled == null) {
            response.setHeader("X-XQuery-Cached", "false");
            context = xquery.newContext(AccessContext.REST);
        } else {
            response.setHeader("X-XQuery-Cached", "true");
            context = compiled.getContext();
        }
        context.setModuleLoadPath(XmldbURI.EMBEDDED_SERVER_URI.append(resource.getCollection().getURI()).toString());
        context.setStaticallyKnownDocuments(new XmldbURI[] { resource.getCollection().getURI() });
        HttpRequestWrapper reqw = declareVariables(context, null, request, response);
        reqw.setServletPath(servletPath);
        reqw.setPathInfo(pathInfo);
        if (compiled == null) {
            try {
                compiled = xquery.compile(context, source);
            } catch (IOException e) {
                throw new BadRequestException("Failed to read query from " + resource.getURI(), e);
            }
        }
        DebuggeeFactory.checkForDebugRequest(request, context);
        boolean wrap = outputProperties.getProperty("_wrap") != null && outputProperties.getProperty("_wrap").equals("yes");
        try {
            Sequence result = xquery.execute(compiled, null, outputProperties);
            writeResults(response, broker, result, -1, 1, false, outputProperties, wrap);
        } finally {
            context.cleanupBinaryValueInstances();
            pool.returnCompiledXQuery(source, compiled);
        }
    }

    /**
	 * Directly execute an XProc stored as a XML document in the database.
	 * @throws PermissionDeniedException 
	 */
    private void executeXProc(DBBroker broker, DocumentImpl resource, HttpServletRequest request, HttpServletResponse response, Properties outputProperties, String servletPath, String pathInfo) throws XPathException, BadRequestException, PermissionDeniedException {
        URLSource source = new URLSource(this.getClass().getResource("run-xproc.xq"));
        XQuery xquery = broker.getXQueryService();
        XQueryPool pool = xquery.getXQueryPool();
        XQueryContext context;
        CompiledXQuery compiled = pool.borrowCompiledXQuery(broker, source);
        if (compiled == null) {
            context = xquery.newContext(AccessContext.REST);
        } else {
            context = compiled.getContext();
        }
        context.declareVariable("pipeline", resource.getURI().toString());
        String stdin = request.getParameter("stdin");
        context.declareVariable("stdin", stdin == null ? "" : stdin);
        String debug = request.getParameter("debug");
        context.declareVariable("debug", debug == null ? "0" : "1");
        String bindings = request.getParameter("bindings");
        context.declareVariable("bindings", bindings == null ? "<bindings/>" : bindings);
        String autobind = request.getParameter("autobind");
        context.declareVariable("autobind", autobind == null ? "0" : "1");
        String options = request.getParameter("options");
        context.declareVariable("options", options == null ? "<options/>" : options);
        context.setModuleLoadPath(XmldbURI.EMBEDDED_SERVER_URI.append(resource.getCollection().getURI()).toString());
        context.setStaticallyKnownDocuments(new XmldbURI[] { resource.getCollection().getURI() });
        HttpRequestWrapper reqw = declareVariables(context, null, request, response);
        reqw.setServletPath(servletPath);
        reqw.setPathInfo(pathInfo);
        if (compiled == null) {
            try {
                compiled = xquery.compile(context, source);
            } catch (IOException e) {
                throw new BadRequestException("Failed to read query from " + source.getURL(), e);
            }
        }
        try {
            Sequence result = xquery.execute(compiled, null, outputProperties);
            writeResults(response, broker, result, -1, 1, false, outputProperties, false);
        } finally {
            pool.returnCompiledXQuery(source, compiled);
        }
    }

    public void setCreatedAndLastModifiedHeaders(HttpServletResponse response, long created, long lastModified) {
        long lastModifiedMillisComp = lastModified % 1000;
        if (lastModifiedMillisComp > 0) {
            lastModified += 1000 - lastModifiedMillisComp;
        }
        long createdMillisComp = created % 1000;
        if (createdMillisComp > 0) {
            created += 1000 - createdMillisComp;
        }
        response.addDateHeader("Last-Modified", lastModified);
        response.addDateHeader("Created", created);
    }

    private void writeResourceAs(DocumentImpl resource, DBBroker broker, String stylesheet, String encoding, String asMimeType, Properties outputProperties, HttpServletRequest request, HttpServletResponse response) throws BadRequestException, PermissionDeniedException, IOException {
        if (!resource.getPermissions().validate(broker.getSubject(), Permission.READ)) {
            throw new PermissionDeniedException("Not allowed to read resource");
        }
        DocumentMetadata metadata = resource.getMetadata();
        long lastModified = metadata.getLastModified();
        setCreatedAndLastModifiedHeaders(response, metadata.getCreated(), lastModified);
        try {
            long ifModifiedSince = request.getDateHeader("If-Modified-Since");
            if (ifModifiedSince > -1) {
                if (ifModifiedSince <= System.currentTimeMillis()) {
                    if (lastModified <= ifModifiedSince) {
                        response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                        return;
                    }
                }
            }
        } catch (IllegalArgumentException iae) {
            LOG.warn("Illegal If-Modified-Since HTTP Header sent on request, ignoring. " + iae.getMessage(), iae);
        }
        if (resource.getResourceType() == DocumentImpl.BINARY_FILE) {
            if (asMimeType == null) {
                asMimeType = resource.getMetadata().getMimeType();
            }
            if (asMimeType.startsWith("text/")) {
                response.setContentType(asMimeType + "; charset=" + encoding);
            } else {
                response.setContentType(asMimeType);
            }
            response.addHeader("Content-Length", Long.toString(resource.getContentLength()));
            OutputStream os = response.getOutputStream();
            broker.readBinaryResource((BinaryDocument) resource, os);
            os.flush();
        } else {
            SAXSerializer sax = null;
            Serializer serializer = broker.getSerializer();
            serializer.reset();
            HttpContext httpContext = serializer.new HttpContext();
            HttpRequestWrapper reqw = new HttpRequestWrapper(request, formEncoding, containerEncoding);
            httpContext.setRequest(reqw);
            httpContext.setSession(reqw.getSession(false));
            serializer.setHttpContext(httpContext);
            try {
                sax = (SAXSerializer) SerializerPool.getInstance().borrowObject(SAXSerializer.class);
                if (stylesheet != null) {
                    serializer.setStylesheet(resource, stylesheet);
                }
                serializer.setProperties(outputProperties);
                serializer.prepareStylesheets(resource);
                if (asMimeType != null) {
                    response.setContentType(asMimeType + "; charset=" + encoding);
                } else {
                    if (serializer.isStylesheetApplied() || serializer.hasXSLPi(resource) != null) {
                        asMimeType = serializer.getStylesheetProperty(OutputKeys.MEDIA_TYPE);
                        if (!useDynamicContentType || asMimeType == null) asMimeType = MimeType.HTML_TYPE.getName();
                        if (LOG.isDebugEnabled()) LOG.debug("media-type: " + asMimeType);
                        response.setContentType(asMimeType + "; charset=" + encoding);
                    } else {
                        asMimeType = resource.getMetadata().getMimeType();
                        response.setContentType(asMimeType + "; charset=" + encoding);
                    }
                }
                if (asMimeType.equals(MimeType.HTML_TYPE.getName())) {
                    outputProperties.setProperty("method", "xhtml");
                    outputProperties.setProperty("media-type", "text/html; charset=" + encoding);
                    outputProperties.setProperty("ident", "yes");
                    outputProperties.setProperty("omit-xml-declaration", "no");
                }
                OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), encoding);
                sax.setOutput(writer, outputProperties);
                serializer.setSAXHandlers(sax, sax);
                serializer.toSAX(resource);
                writer.flush();
                writer.close();
            } catch (SAXException saxe) {
                LOG.warn(saxe);
                throw new BadRequestException("Error while serializing XML: " + saxe.getMessage());
            } catch (TransformerConfigurationException e) {
                LOG.warn(e);
                throw new BadRequestException(e.getMessageAndLocation());
            } finally {
                if (sax != null) {
                    SerializerPool.getInstance().returnObject(sax);
                }
            }
        }
    }

    /**
	 * @param response
	 * @param encoding
	 * @param query
	 * @param path
	 * @param e
	 *
	 */
    private void writeXPathExceptionHtml(HttpServletResponse response, int httpStatusCode, String encoding, String query, String path, XPathException e) throws IOException {
        if (!response.isCommitted()) {
            response.reset();
        }
        response.setStatus(httpStatusCode);
        response.setContentType(MimeType.HTML_TYPE.getName() + "; charset=" + encoding);
        OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), encoding);
        writer.write(QUERY_ERROR_HEAD);
        writer.write("<p class=\"path\"><span class=\"high\">Path</span>: ");
        writer.write("<a href=\"");
        writer.write(path);
        writer.write("\">");
        writer.write(path);
        writer.write("</a></p>");
        writer.write("<p class=\"errmsg\">");
        String message = e.getMessage() == null ? e.toString() : e.getMessage();
        writer.write(XMLUtil.encodeAttrMarkup(message));
        writer.write("</p>");
        if (query != null) {
            writer.write("<p><span class=\"high\">Query</span>:</p><pre>");
            writer.write(XMLUtil.encodeAttrMarkup(query));
            writer.write("</pre>");
        }
        writer.write("</body></html>");
        writer.flush();
        writer.close();
    }

    /**
	 * @param response
	 * @param encoding
	 * @param query
	 * @param path
	 * @param e
	 */
    private void writeXPathException(HttpServletResponse response, int httpStatusCode, String encoding, String query, String path, XPathException e) throws IOException {
        if (!response.isCommitted()) {
            response.reset();
        }
        response.setStatus(httpStatusCode);
        response.setContentType(MimeType.XML_TYPE.getName() + "; charset=" + encoding);
        OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), encoding);
        writer.write("<?xml version=\"1.0\" ?>");
        writer.write("<exception><path>");
        writer.write(path);
        writer.write("</path>");
        writer.write("<message>");
        String message = e.getMessage() == null ? e.toString() : e.getMessage();
        writer.write(XMLUtil.encodeAttrMarkup(message));
        writer.write("</message>");
        if (query != null) {
            writer.write("<query>");
            writer.write(XMLUtil.encodeAttrMarkup(query));
            writer.write("</query>");
        }
        writer.write("</exception>");
        writer.flush();
        writer.close();
    }

    /**
	 * @param response
	 * @param encoding
	 * @param updateCount
	 */
    private void writeXUpdateResult(HttpServletResponse response, String encoding, long updateCount) throws IOException {
        response.setContentType(MimeType.XML_TYPE.getName() + "; charset=" + encoding);
        OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), encoding);
        writer.write("<?xml version=\"1.0\" ?>");
        writer.write("<exist:modifications xmlns:exist=\"" + Namespaces.EXIST_NS + "\" count=\"" + updateCount + "\">");
        writer.write(updateCount + " modifications processed.");
        writer.write("</exist:modifications>");
        writer.flush();
        writer.close();
    }

    /**
	 * @param response
	 * @param encoding
	 * @param broker
	 * @param collection
	 */
    protected void writeCollection(HttpServletResponse response, String encoding, DBBroker broker, Collection collection) throws IOException, PermissionDeniedException, LockException {
        response.setContentType(MimeType.XML_TYPE.getName() + "; charset=" + encoding);
        setCreatedAndLastModifiedHeaders(response, collection.getCreationTime(), collection.getCreationTime());
        OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), encoding);
        SAXSerializer serializer = null;
        try {
            serializer = (SAXSerializer) SerializerPool.getInstance().borrowObject(SAXSerializer.class);
            serializer.setOutput(writer, defaultProperties);
            AttributesImpl attrs = new AttributesImpl();
            serializer.startDocument();
            serializer.startPrefixMapping("exist", Namespaces.EXIST_NS);
            serializer.startElement(Namespaces.EXIST_NS, "result", "exist:result", attrs);
            attrs.addAttribute("", "name", "name", "CDATA", collection.getURI().toString());
            try {
                DateTimeValue dtCreated = new DateTimeValue(new Date(collection.getCreationTime()));
                attrs.addAttribute("", "created", "created", "CDATA", dtCreated.getStringValue());
            } catch (XPathException e) {
                attrs.addAttribute("", "created", "created", "CDATA", String.valueOf(collection.getCreationTime()));
            }
            addPermissionAttributes(attrs, collection.getPermissions());
            serializer.startElement(Namespaces.EXIST_NS, "collection", "exist:collection", attrs);
            for (Iterator<XmldbURI> i = collection.collectionIterator(broker); i.hasNext(); ) {
                XmldbURI child = i.next();
                Collection childCollection = broker.getCollection(collection.getURI().append(child));
                if (childCollection != null && childCollection.getPermissions().validate(broker.getSubject(), Permission.READ)) {
                    attrs.clear();
                    attrs.addAttribute("", "name", "name", "CDATA", child.toString());
                    try {
                        DateTimeValue dtCreated = new DateTimeValue(new Date(childCollection.getCreationTime()));
                        attrs.addAttribute("", "created", "created", "CDATA", dtCreated.getStringValue());
                    } catch (XPathException e) {
                        attrs.addAttribute("", "created", "created", "CDATA", String.valueOf(childCollection.getCreationTime()));
                    }
                    addPermissionAttributes(attrs, childCollection.getPermissions());
                    serializer.startElement(Namespaces.EXIST_NS, "collection", "exist:collection", attrs);
                    serializer.endElement(Namespaces.EXIST_NS, "collection", "exist:collection");
                }
            }
            for (Iterator<DocumentImpl> i = collection.iterator(broker); i.hasNext(); ) {
                DocumentImpl doc = i.next();
                if (doc.getPermissions().validate(broker.getSubject(), Permission.READ)) {
                    XmldbURI resource = doc.getFileURI();
                    DocumentMetadata metadata = doc.getMetadata();
                    attrs.clear();
                    attrs.addAttribute("", "name", "name", "CDATA", resource.toString());
                    try {
                        DateTimeValue dtCreated = new DateTimeValue(new Date(metadata.getCreated()));
                        attrs.addAttribute("", "created", "created", "CDATA", dtCreated.getStringValue());
                    } catch (XPathException e) {
                        attrs.addAttribute("", "created", "created", "CDATA", String.valueOf(metadata.getCreated()));
                    }
                    try {
                        DateTimeValue dtLastModified = new DateTimeValue(new Date(metadata.getLastModified()));
                        attrs.addAttribute("", "last-mofified", "last-modified", "CDATA", dtLastModified.getStringValue());
                    } catch (XPathException e) {
                        attrs.addAttribute("", "last-modified", "last-modified", "CDATA", String.valueOf(metadata.getLastModified()));
                    }
                    addPermissionAttributes(attrs, doc.getPermissions());
                    serializer.startElement(Namespaces.EXIST_NS, "resource", "exist:resource", attrs);
                    serializer.endElement(Namespaces.EXIST_NS, "resource", "exist:resource");
                }
            }
            serializer.endElement(Namespaces.EXIST_NS, "collection", "exist:collection");
            serializer.endElement(Namespaces.EXIST_NS, "result", "exist:result");
            serializer.endDocument();
            writer.flush();
            writer.close();
        } catch (SAXException e) {
            LOG.warn("Error while serializing collection contents: " + e.getMessage(), e);
        } finally {
            if (serializer != null) {
                SerializerPool.getInstance().returnObject(serializer);
            }
        }
    }

    protected void addPermissionAttributes(AttributesImpl attrs, Permission perm) {
        attrs.addAttribute("", "owner", "owner", "CDATA", perm.getOwner().getName());
        attrs.addAttribute("", "group", "group", "CDATA", perm.getGroup().getName());
        attrs.addAttribute("", "permissions", "permissions", "CDATA", perm.toString());
    }

    protected void writeResults(HttpServletResponse response, DBBroker broker, Sequence results, int howmany, int start, boolean typed, Properties outputProperties, boolean wrap) throws BadRequestException {
        if (response.isCommitted()) return;
        if (!results.isEmpty()) {
            int rlen = results.getItemCount();
            if ((start < 1) || (start > rlen)) throw new BadRequestException("Start parameter out of range");
            if (((howmany + start) > rlen) || (howmany <= 0)) howmany = rlen - start + 1;
        } else {
            howmany = 0;
        }
        Serializer serializer = broker.getSerializer();
        serializer.reset();
        outputProperties.setProperty(Serializer.GENERATE_DOC_EVENTS, "false");
        SAXSerializer sax = null;
        try {
            sax = (SAXSerializer) SerializerPool.getInstance().borrowObject(SAXSerializer.class);
            String encoding = outputProperties.getProperty(OutputKeys.ENCODING);
            if (!response.containsHeader("Content-Type")) {
                String mimeType = outputProperties.getProperty(OutputKeys.MEDIA_TYPE);
                if (mimeType != null) {
                    int semicolon = mimeType.indexOf(';');
                    if (semicolon != Constants.STRING_NOT_FOUND) {
                        mimeType = mimeType.substring(0, semicolon);
                    }
                    if (wrap) {
                        mimeType = "application/xml";
                    }
                    response.setContentType(mimeType + "; charset=" + encoding);
                }
            }
            if (wrap) outputProperties.setProperty("method", "xml");
            Writer writer = new OutputStreamWriter(response.getOutputStream(), encoding);
            sax.setOutput(writer, outputProperties);
            serializer.setProperties(outputProperties);
            serializer.setSAXHandlers(sax, sax);
            serializer.toSAX(results, start, howmany, wrap, typed);
            writer.flush();
            writer.close();
        } catch (SAXException e) {
            LOG.warn(e);
            throw new BadRequestException("Error while serializing xml: " + e.toString(), e);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            throw new BadRequestException("Error while serializing xml: " + e.toString(), e);
        } finally {
            if (sax != null) {
                SerializerPool.getInstance().returnObject(sax);
            }
        }
    }

    private boolean isExecutableType(DocumentImpl resource) {
        if (resource != null && (MimeType.XQUERY_TYPE.getName().equals(resource.getMetadata().getMimeType()) || MimeType.XPROC_TYPE.getName().equals(resource.getMetadata().getMimeType()))) return true; else return false;
    }

    /**
	 * @param query
	 * @param path
	 * @param e
	 */
    private String formatXPathException(String query, String path, XPathException e) {
        StringWriter writer = new StringWriter();
        writer.write("<xpath:exception xmlns:xpath=\"http://exist-db.org/xpath\">");
        writer.write("<xpath:path>" + path + "</xpath:path>");
        writer.write("<xpath:message>" + e.getMessage() + "</xpath:message>");
        if (query != null) {
            writer.write("<xpath:query>" + query + "</xpath:query>");
        }
        writer.write("</xpath:exception>");
        return writer.toString();
    }
}
