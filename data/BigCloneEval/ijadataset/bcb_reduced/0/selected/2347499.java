package org.exist.xquery.functions.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import javax.xml.datatype.Duration;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.exist.EXistException;
import org.exist.Namespaces;
import org.exist.dom.BinaryDocument;
import org.exist.dom.DocumentImpl;
import org.exist.dom.DocumentSet;
import org.exist.dom.QName;
import org.exist.memtree.NodeImpl;
import org.exist.memtree.ReferenceNode;
import org.exist.memtree.SAXAdapter;
import org.exist.security.PermissionDeniedException;
import org.exist.security.UUIDGenerator;
import org.exist.source.DBSource;
import org.exist.source.FileSource;
import org.exist.source.Source;
import org.exist.source.SourceFactory;
import org.exist.source.StringSource;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.XQueryPool;
import org.exist.storage.lock.Lock;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.CompiledXQuery;
import org.exist.xquery.Dependency;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.LocalVariable;
import org.exist.xquery.Profiler;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQuery;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.BooleanValue;
import org.exist.xquery.value.DateTimeValue;
import org.exist.xquery.value.EmptySequence;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.Item;
import org.exist.xquery.value.NodeValue;
import org.exist.xquery.value.QNameValue;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.TimeUtils;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author wolf
 *
 */
public class Eval extends BasicFunction {

    protected static final FunctionParameterSequenceType EVAL_CONTEXT_ITEM = new FunctionParameterSequenceType("eval-context-item", Type.ITEM, Cardinality.ZERO_OR_ONE, "the context item against which the expression will be evaluated");

    private static final String evalArgumentText = "The expression to be evaluated.  If it is of type xs:string, the function " + "tries to execute this string as the query. If the first argument is of type xs:anyURI, " + "the function will try to load the query from the resource to which the URI resolves. " + "If the URI has no scheme, it is assumed that the query is stored in the db and the " + "URI is interpreted as a database path. This is the same as calling " + "util:eval(xs:anyURI('xmldb:exist:///db/test/test.xq')). " + "The query inherits the current execution context, i.e. all " + "namespace declarations and variable declarations are visible from within the " + "inner expression. " + "The function returns an empty sequence if a whitespace string is passed.";

    private static final String contextArgumentText = "The query inherits the context described by the XML fragment in this parameter. " + "It should have the format:\n" + "<static-context>\n" + "\t<output-size-limit value=\"-1\">\n" + "\t<unbind-namespace uri=\"http://exist.sourceforge.net/NS/exist\"/>\n" + "\t<current-dateTime value=\"dateTime\"/>\n" + "\t<implicit-timezone value=\"duration\"/>\n" + "\t<variable name=\"qname\">variable value</variable>\n" + "\t<default-context>explicitly provide default context here</default-context>\n" + "\t<mapModule namespace=\"uri\" uri=\"uri_to_module\"/>\n" + "</static-context>.\n";

    protected static final FunctionParameterSequenceType EVAL_ARGUMENT = new FunctionParameterSequenceType("expression", Type.ITEM, Cardinality.EXACTLY_ONE, evalArgumentText);

    protected static final FunctionParameterSequenceType INLINE_CONTEXT = new FunctionParameterSequenceType("inline-context", Type.ITEM, Cardinality.ZERO_OR_MORE, "The inline context");

    protected static final FunctionParameterSequenceType CONTEXT_ARGUMENT = new FunctionParameterSequenceType("context", Type.NODE, Cardinality.ZERO_OR_ONE, contextArgumentText);

    protected static final FunctionParameterSequenceType CACHE_FLAG = new FunctionParameterSequenceType("cache-flag", Type.BOOLEAN, Cardinality.EXACTLY_ONE, "The flag for whether the compiled query should be cached.  The cached query will be globally available within the db instance.");

    protected static final FunctionParameterSequenceType EXTERNAL_VARIABLE = new FunctionParameterSequenceType("external-variable", Type.ANY_TYPE, Cardinality.ZERO_OR_MORE, "External variables to be bound for the query that is being evaluated. Should be alternating variable QName and value.");

    protected static final FunctionReturnSequenceType RETURN_NODE_TYPE = new FunctionReturnSequenceType(Type.NODE, Cardinality.ZERO_OR_MORE, "the results of the evaluated XPath/XQuery expression");

    protected static final FunctionReturnSequenceType RETURN_THREADID_TYPE = new FunctionReturnSequenceType(Type.STRING, Cardinality.EXACTLY_ONE, "The ID of the asynchronously executing thread.");

    protected static final FunctionReturnSequenceType RETURN_ITEM_TYPE = new FunctionReturnSequenceType(Type.ITEM, Cardinality.ZERO_OR_MORE, "the results of the evaluated XPath/XQuery expression");

    private static final ExecutorService asyncExecutorService = Executors.newCachedThreadPool(new AsyncQueryThreadFactory());

    private static class AsyncQueryThreadFactory implements ThreadFactory {

        private int id = 0;

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "AsynchronousEval-" + getId());
        }

        private synchronized int getId() {
            return id++;
        }
    }

    public static final FunctionSignature signatures[] = { new FunctionSignature(new QName("eval", UtilModule.NAMESPACE_URI, UtilModule.PREFIX), "Dynamically evaluates an XPath/XQuery expression. ", new SequenceType[] { EVAL_ARGUMENT }, RETURN_NODE_TYPE), new FunctionSignature(new QName("eval-async", UtilModule.NAMESPACE_URI, UtilModule.PREFIX), "Dynamically evaluates an XPath/XQuery expression asynchronously. The ID of the executing thread is returned.", new SequenceType[] { EVAL_ARGUMENT }, RETURN_NODE_TYPE), new FunctionSignature(new QName("eval", UtilModule.NAMESPACE_URI, UtilModule.PREFIX), "Dynamically evaluates an XPath/XQuery expression. ", new SequenceType[] { EVAL_ARGUMENT, CACHE_FLAG }, RETURN_NODE_TYPE), new FunctionSignature(new QName("eval", UtilModule.NAMESPACE_URI, UtilModule.PREFIX), "Dynamically evaluates an XPath/XQuery expression. ", new SequenceType[] { EVAL_ARGUMENT, CACHE_FLAG, EXTERNAL_VARIABLE }, RETURN_NODE_TYPE), new FunctionSignature(new QName("eval-with-context", UtilModule.NAMESPACE_URI, UtilModule.PREFIX), "Dynamically evaluates an XPath/XQuery expression. " + "", new SequenceType[] { EVAL_ARGUMENT, CONTEXT_ARGUMENT, CACHE_FLAG }, RETURN_NODE_TYPE), new FunctionSignature(new QName("eval-with-context", UtilModule.NAMESPACE_URI, UtilModule.PREFIX), "Dynamically evaluates an XPath/XQuery expression.", new SequenceType[] { EVAL_ARGUMENT, CONTEXT_ARGUMENT, CACHE_FLAG, EVAL_CONTEXT_ITEM }, RETURN_NODE_TYPE), new FunctionSignature(new QName("eval-inline", UtilModule.NAMESPACE_URI, UtilModule.PREFIX), "Dynamically evaluates an XPath/XQuery expression.", new SequenceType[] { INLINE_CONTEXT, EVAL_ARGUMENT }, RETURN_ITEM_TYPE), new FunctionSignature(new QName("eval-inline", UtilModule.NAMESPACE_URI, UtilModule.PREFIX), "Dynamically evaluates an XPath/XQuery expression.", new SequenceType[] { INLINE_CONTEXT, EVAL_ARGUMENT, CACHE_FLAG }, RETURN_ITEM_TYPE) };

    /**
     * @param context
     * @param signature
     */
    public Eval(XQueryContext context, FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {
        if (isCalledAs("eval-async")) {
            String uuid = UUIDGenerator.getUUID();
            CallableEval asyncEval = new CallableEval(context, contextSequence, args);
            Future<Sequence> f = asyncExecutorService.submit(asyncEval);
            return new StringValue(uuid);
        } else {
            return doEval(context, contextSequence, args);
        }
    }

    private class CallableEval implements Callable<Sequence> {

        private final XQueryContext callersContext;

        private final Sequence contextSequence;

        private final Sequence args[];

        public CallableEval(XQueryContext context, Sequence contextSequence, Sequence args[]) {
            this.callersContext = context;
            this.contextSequence = contextSequence;
            this.args = args;
        }

        @Override
        public Sequence call() throws XPathException {
            BrokerPool db = null;
            DBBroker broker = null;
            try {
                db = BrokerPool.getInstance();
            } catch (EXistException e) {
                throw new XPathException("Unable to get new broker: " + e.getMessage(), e);
            }
            try {
                final XQueryContext context = callersContext.copyContext();
                broker = db.get(callersContext.getSubject());
                return doEval(context, contextSequence, args);
            } catch (EXistException ex) {
                throw new XPathException("Unable to get new broker: " + ex.getMessage(), ex);
            } finally {
                db.release(broker);
            }
        }
    }

    private Sequence doEval(XQueryContext evalContext, Sequence contextSequence, Sequence args[]) throws XPathException {
        if (evalContext.getProfiler().isEnabled()) {
            evalContext.getProfiler().start(this);
            evalContext.getProfiler().message(this, Profiler.DEPENDENCIES, "DEPENDENCIES", Dependency.getDependenciesName(this.getDependencies()));
            if (contextSequence != null) {
                evalContext.getProfiler().message(this, Profiler.START_SEQUENCES, "CONTEXT SEQUENCE", contextSequence);
            }
        }
        int argCount = 0;
        Sequence exprContext = null;
        Sequence initContextSequence = null;
        if (isCalledAs("eval-inline")) {
            exprContext = args[argCount++];
        }
        Item expr = args[argCount++].itemAt(0);
        Source querySource;
        if (Type.subTypeOf(expr.getType(), Type.ANY_URI)) {
            querySource = loadQueryFromURI(expr);
        } else {
            String queryStr = expr.getStringValue();
            if ("".equals(queryStr.trim())) {
                return new EmptySequence();
            }
            querySource = new StringSource(queryStr);
        }
        NodeValue contextInit = null;
        if (isCalledAs("eval-with-context")) {
            contextInit = (NodeValue) args[argCount++].itemAt(0);
        }
        boolean cache = false;
        if (argCount < getArgumentCount()) {
            cache = ((BooleanValue) args[argCount].itemAt(0)).effectiveBooleanValue();
        }
        evalContext.pushNamespaceContext();
        LocalVariable mark = evalContext.markLocalVariables(false);
        DocumentSet oldDocs = evalContext.getStaticallyKnownDocuments();
        if (exprContext != null) {
            evalContext.setStaticallyKnownDocuments(exprContext.getDocumentSet());
        }
        if (evalContext.isProfilingEnabled(2)) {
            evalContext.getProfiler().start(this, "eval: " + expr);
        }
        Sequence sequence = null;
        XQuery xqueryService = evalContext.getBroker().getXQueryService();
        final XQueryContext innerContext;
        if (contextInit != null) {
            innerContext = xqueryService.newContext(evalContext.getAccessContext());
            initContextSequence = initContext(contextInit.getNode(), innerContext);
        } else {
            innerContext = evalContext.copyContext();
            innerContext.setShared(true);
        }
        if (Type.subTypeOf(expr.getType(), Type.ANY_URI)) {
            String uri = null;
            Object key = querySource.getKey();
            if (key instanceof XmldbURI) {
                uri = XmldbURI.EMBEDDED_SERVER_URI.append(((XmldbURI) key).removeLastSegment()).toString();
            } else if (key instanceof String && querySource instanceof FileSource) {
                uri = ((FileSource) querySource).getFile().getParent();
            }
            if (uri != null) {
                innerContext.setModuleLoadPath(uri);
            }
        }
        if (isCalledAs("eval") && getArgumentCount() == 3) {
            if (!args[2].isEmpty()) {
                Sequence externalVars = args[2];
                for (int i = 0; i < externalVars.getItemCount(); i++) {
                    Item varName = externalVars.itemAt(i);
                    if (varName.getType() == Type.QNAME) {
                        Item varValue = externalVars.itemAt(++i);
                        innerContext.declareVariable(((QNameValue) varName).getQName(), varValue);
                    }
                }
            }
        }
        try {
            if (this.getArgumentCount() == 4) {
                NodeValue contextItem = (NodeValue) args[3].itemAt(0);
                if (contextItem != null) {
                    if (exprContext != null) {
                        LOG.warn("exprContext and contextItem are not null");
                    }
                    exprContext = contextItem.toSequence();
                }
            }
            if (initContextSequence != null) {
                LOG.info("there now");
                exprContext = initContextSequence;
            }
            try {
                return execute(evalContext.getBroker(), xqueryService, querySource, innerContext, exprContext, cache);
            } finally {
                cleanup(evalContext, innerContext, oldDocs, mark, expr, sequence);
            }
        } catch (XPathException e) {
            try {
                e.prependMessage("Error while evaluating expression: " + querySource.getContent() + ". ");
            } catch (IOException e1) {
            }
            e.setLocation(line, column);
            throw e;
        }
    }

    private void cleanup(XQueryContext evalContext, XQueryContext innerContext, DocumentSet oldDocs, LocalVariable mark, Item expr, Sequence sequence) {
        if (innerContext != evalContext) {
            innerContext.reset();
        }
        if (oldDocs != null) {
            evalContext.setStaticallyKnownDocuments(oldDocs);
        }
        evalContext.popLocalVariables(mark);
        evalContext.popNamespaceContext();
        if (evalContext.isProfilingEnabled(2)) {
            evalContext.getProfiler().end(this, "eval: " + expr, sequence);
        }
    }

    private Sequence execute(DBBroker broker, XQuery xqueryService, Source querySource, XQueryContext innerContext, Sequence exprContext, boolean cache) throws XPathException {
        CompiledXQuery compiled = null;
        XQueryPool pool = xqueryService.getXQueryPool();
        try {
            compiled = cache ? pool.borrowCompiledXQuery(broker, querySource) : null;
            if (compiled == null) {
                compiled = xqueryService.compile(innerContext, querySource);
            } else {
                compiled.getContext().updateContext(innerContext);
            }
            Sequence sequence = xqueryService.execute(compiled, exprContext, false);
            ValueSequence newSeq = new ValueSequence();
            newSeq.keepUnOrdered(unordered);
            boolean hasSupplements = false;
            for (int i = 0; i < sequence.getItemCount(); i++) {
                if (Type.subTypeOf(sequence.itemAt(i).getType(), Type.STRING)) {
                    newSeq.add(new StringValue(((StringValue) sequence.itemAt(i)).getStringValue(true)));
                    hasSupplements = true;
                } else {
                    newSeq.add(sequence.itemAt(i));
                }
            }
            if (hasSupplements) {
                sequence = newSeq;
            }
            return sequence;
        } catch (IOException ioe) {
            throw new XPathException(this, ioe);
        } catch (PermissionDeniedException e) {
            throw new XPathException(this, e);
        } finally {
            if (compiled != null) {
                if (cache) {
                    pool.returnCompiledXQuery(querySource, compiled);
                } else {
                    compiled.reset();
                }
            }
        }
    }

    /**
     * @param expr
     * @return
     * @throws XPathException
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    private Source loadQueryFromURI(Item expr) throws XPathException, NullPointerException, IllegalArgumentException {
        String location = expr.getStringValue();
        Source querySource = null;
        if (location.indexOf(':') < 0 || location.startsWith(XmldbURI.XMLDB_URI_PREFIX)) {
            try {
                XmldbURI locationUri = XmldbURI.xmldbUriFor(location);
                if (location.indexOf("/") < 0 || location.startsWith(".")) {
                    XmldbURI moduleLoadPathUri = XmldbURI.xmldbUriFor(context.getModuleLoadPath());
                    locationUri = moduleLoadPathUri.resolveCollectionPath(locationUri);
                }
                DocumentImpl sourceDoc = null;
                try {
                    sourceDoc = context.getBroker().getXMLResource(locationUri.toCollectionPathURI(), Lock.READ_LOCK);
                    if (sourceDoc == null) throw new XPathException(this, "source for module " + location + " not found in database");
                    if (sourceDoc.getResourceType() != DocumentImpl.BINARY_FILE || !sourceDoc.getMetadata().getMimeType().equals("application/xquery")) throw new XPathException(this, "source for module " + location + " is not an XQuery or " + "declares a wrong mime-type");
                    querySource = new DBSource(context.getBroker(), (BinaryDocument) sourceDoc, true);
                } catch (PermissionDeniedException e) {
                    throw new XPathException(this, "permission denied to read module source from " + location);
                } finally {
                    if (sourceDoc != null) sourceDoc.getUpdateLock().release(Lock.READ_LOCK);
                }
            } catch (URISyntaxException e) {
                throw new XPathException(this, e);
            }
        } else {
            try {
                querySource = SourceFactory.getSource(context.getBroker(), context.getModuleLoadPath(), location, true);
            } catch (MalformedURLException e) {
                throw new XPathException(this, "source location for query at " + location + " should be a valid URL: " + e.getMessage());
            } catch (IOException e) {
                throw new XPathException(this, "source for query at " + location + " not found: " + e.getMessage());
            } catch (PermissionDeniedException e) {
                throw new XPathException(this, "Permission denied to access query at " + location + " : " + e.getMessage());
            }
        }
        return querySource;
    }

    /**
	 * Read to optional static-context fragment to initialize
	 * the context.
	 *
	 * @param root
	 * @param innerContext
	 * @throws XPathException
	 */
    private Sequence initContext(Node root, XQueryContext innerContext) throws XPathException {
        NodeList cl = root.getChildNodes();
        Sequence result = null;
        for (int i = 0; i < cl.getLength(); i++) {
            Node child = cl.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && "variable".equals(child.getLocalName())) {
                Element elem = (Element) child;
                String qname = elem.getAttribute("name");
                String source = elem.getAttribute("source");
                NodeValue value;
                if (source != null && source.length() > 0) {
                    value = loadVarFromURI(source);
                } else {
                    value = (NodeValue) elem.getFirstChild();
                    if (value instanceof ReferenceNode) value = ((ReferenceNode) value).getReference();
                }
                String type = elem.getAttribute("type");
                if (type != null && Type.subTypeOf(Type.getType(type), Type.ATOMIC)) {
                    innerContext.declareVariable(qname, value.atomize().convertTo(Type.getType(type)));
                } else {
                    innerContext.declareVariable(qname, value);
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE && "output-size-limit".equals(child.getLocalName())) {
                Element elem = (Element) child;
                innerContext.getWatchDog().setMaxNodes(Integer.valueOf(elem.getAttribute("value")).intValue());
            } else if (child.getNodeType() == Node.ELEMENT_NODE && "current-dateTime".equals(child.getLocalName())) {
                Element elem = (Element) child;
                DateTimeValue dtv = new DateTimeValue(elem.getAttribute("value"));
                innerContext.setCalendar(dtv.calendar);
            } else if (child.getNodeType() == Node.ELEMENT_NODE && "implicit-timezone".equals(child.getLocalName())) {
                Element elem = (Element) child;
                Duration duration = TimeUtils.getInstance().newDuration(elem.getAttribute("value"));
                innerContext.setTimeZone(new SimpleTimeZone((int) duration.getTimeInMillis(new Date()), "XQuery context"));
            } else if (child.getNodeType() == Node.ELEMENT_NODE && "unbind-namespace".equals(child.getLocalName())) {
                Element elem = (Element) child;
                if (elem.getAttribute("uri") != null) {
                    innerContext.removeNamespace(elem.getAttribute("uri"));
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE && "staticallyKnownDocuments".equals(child.getLocalName())) {
                Element elem = (Element) child;
                NodeValue value = (NodeValue) elem.getFirstChild();
                if (value instanceof ReferenceNode) value = ((ReferenceNode) value).getReference();
                XmldbURI[] pathes = new XmldbURI[1];
                pathes[0] = XmldbURI.create(value.getStringValue());
                innerContext.setStaticallyKnownDocuments(pathes);
            } else if (child.getNodeType() == Node.ELEMENT_NODE && "default-context".equals(child.getLocalName())) {
                Element elem = (Element) child;
                NodeValue nodevalue = (NodeValue) elem;
                result = nodevalue.toSequence();
            }
        }
        return result;
    }

    private NodeImpl loadVarFromURI(String uri) throws XPathException {
        try {
            URL url = new URL(uri);
            InputStreamReader isr = new InputStreamReader(url.openStream(), "UTF-8");
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            InputSource src = new InputSource(isr);
            SAXParser parser = factory.newSAXParser();
            XMLReader xr = parser.getXMLReader();
            SAXAdapter adapter = new SAXAdapter(context);
            xr.setContentHandler(adapter);
            xr.setProperty(Namespaces.SAX_LEXICAL_HANDLER, adapter);
            xr.parse(src);
            isr.close();
            return (NodeImpl) adapter.getDocument();
        } catch (MalformedURLException e) {
            throw new XPathException(this, e);
        } catch (IOException e) {
            throw new XPathException(this, e);
        } catch (SAXException e) {
            throw new XPathException(this, e);
        } catch (ParserConfigurationException e) {
            throw new XPathException(this, e);
        }
    }
}
