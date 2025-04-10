package org.exist.security.xacml;

import com.sun.xacml.AbstractPolicy;
import com.sun.xacml.Indenter;
import com.sun.xacml.ParsingException;
import com.sun.xacml.Policy;
import com.sun.xacml.PolicyReference;
import com.sun.xacml.PolicySet;
import com.sun.xacml.PolicyTreeElement;
import com.sun.xacml.ProcessingException;
import com.sun.xacml.Target;
import com.sun.xacml.cond.Apply;
import com.sun.xacml.ctx.Status;
import com.sun.xacml.finder.PolicyFinderResult;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.exist.EXistException;
import org.exist.collections.Collection;
import org.exist.collections.IndexInfo;
import org.exist.collections.triggers.TriggerException;
import org.exist.dom.DefaultDocumentSet;
import org.exist.dom.DocumentImpl;
import org.exist.dom.DocumentSet;
import org.exist.dom.MutableDocumentSet;
import org.exist.dom.NodeSet;
import org.exist.dom.QName;
import org.exist.dom.StoredNode;
import org.exist.numbering.NodeId;
import org.exist.security.PermissionDeniedException;
import org.exist.storage.BrokerPool;
import org.exist.storage.DBBroker;
import org.exist.storage.NativeValueIndex;
import org.exist.storage.UpdateListener;
import org.exist.storage.txn.TransactionManager;
import org.exist.storage.txn.Txn;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.Constants;
import org.exist.xquery.XPathException;
import org.exist.xquery.value.AnyURIValue;
import org.exist.xquery.value.AtomicValue;
import org.exist.xquery.value.Sequence;
import org.exist.external.org.apache.commons.io.output.ByteArrayOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class contains utility methods for working with XACML
 * in eXist.
 */
public class XACMLUtil implements UpdateListener {

    private static final Logger LOG = Logger.getLogger(ExistPolicyModule.class);

    private static final Map<String, AbstractPolicy> POLICY_CACHE = Collections.synchronizedMap(new HashMap<String, AbstractPolicy>(8));

    private static final XmldbURI[] samplePolicyDocs = { XmldbURI.create("policies/main_modules_policy.xml"), XmldbURI.create("policies/builtin_policy.xml"), XmldbURI.create("policies/external_modules_policy.xml"), XmldbURI.create("policies/reflection_policy.xml") };

    private ExistPDP pdp;

    @SuppressWarnings("unused")
    private XACMLUtil() {
    }

    XACMLUtil(ExistPDP pdp) {
        if (pdp == null) throw new NullPointerException("ExistPDP cannot be null");
        this.pdp = pdp;
        pdp.getBrokerPool().getNotificationService().subscribe(this);
    }

    protected void initializePolicyCollection() {
        DBBroker broker = null;
        try {
            BrokerPool pool = pdp.getBrokerPool();
            broker = pool.get(pool.getSecurityManager().getSystemSubject());
            initializePolicyCollection(broker);
        } catch (PermissionDeniedException pde) {
            LOG.error(pde.getMessage(), pde);
        } catch (EXistException ee) {
            LOG.error("Could not get broker pool to initialize policy collection", ee);
        } finally {
            pdp.getBrokerPool().release(broker);
        }
    }

    private void initializePolicyCollection(DBBroker broker) throws PermissionDeniedException {
        Collection policyCollection = getPolicyCollection(broker);
        if (policyCollection == null) return;
        if (policyCollection.getDocumentCount(broker) == 0) {
            Boolean loadDefaults = (Boolean) broker.getConfiguration().getProperty(XACMLConstants.LOAD_DEFAULT_POLICIES_PROPERTY);
            if (loadDefaults == null || loadDefaults.booleanValue()) storeDefaultPolicies(broker);
        }
    }

    /**
	 * This method is called by the <code>NotificationService</code>
	 * when documents are updated in the databases.  If a document
	 * is removed or updated from the policy collection, it is removed
	 * from the policy cache.
	 */
    public void documentUpdated(DocumentImpl document, int event) {
        if (inPolicyCollection(document) && (event == UpdateListener.REMOVE || event == UpdateListener.UPDATE)) POLICY_CACHE.remove(document.getURI().toString());
    }

    public void nodeMoved(NodeId oldNodeId, StoredNode newNode) {
    }

    public void unsubscribe() {
    }

    /**
	 * Returns true if the specified document is in the policy collection.
	 * This does not check subcollections.
	 * 
	 * @param document The document in question
	 * @return if the document is in the policy collection
	 */
    public static boolean inPolicyCollection(DocumentImpl document) {
        return XACMLConstants.POLICY_COLLECTION_URI.equals(document.getCollection().getURI());
    }

    /**
	* Performs any necessary cleanup operations.  Generally only
	* called if XACML has been disabled.
	*/
    public void close() {
        pdp.getBrokerPool().getNotificationService().unsubscribe(this);
    }

    /**
	* Gets the policy (or policy set) specified by the given id.
	* 
	* @param type The type of id reference:
	*	PolicyReference.POLICY_REFERENCE for a policy reference
	*	or PolicyReference.POLICYSET_REFERENCE for a policy set
	*	reference.
	* @param idReference The id of the policy (or policy set) to
	*	retrieve
	* @param broker the broker to use to access the database
	* @return The referenced policy.
	* @throws ProcessingException if there is an error finding
	*	the policy (or policy set).
	* @throws XPathException
	*/
    public AbstractPolicy findPolicy(DBBroker broker, URI idReference, int type) throws ParsingException, ProcessingException, XPathException, PermissionDeniedException {
        QName idAttributeQName = getIdAttributeQName(type);
        if (idAttributeQName == null) throw new NullPointerException("Invalid reference type: " + type);
        DocumentImpl policyDoc = getPolicyDocument(broker, idAttributeQName, idReference);
        if (policyDoc == null) return null;
        return getPolicyDocument(policyDoc);
    }

    /**
	* This method returns all policy documents in the policies collection.
	* If recursive is true, policies in subcollections are returned as well.
	*
	* @param broker the broker to use to access the database
	* @param recursive true if policies in subcollections should be
	*	returned as well
	* @return All policy documents in the policies collection
	*/
    public static DocumentSet getPolicyDocuments(DBBroker broker, boolean recursive) throws PermissionDeniedException {
        Collection policyCollection = getPolicyCollection(broker);
        if (policyCollection == null) return null;
        int documentCount = policyCollection.getDocumentCount(broker);
        if (documentCount == 0) return null;
        MutableDocumentSet documentSet = new DefaultDocumentSet(documentCount);
        return policyCollection.allDocs(broker, documentSet, recursive);
    }

    /**
	 * Gets the policy collection or creates it if it does not exist.
	 * 
	 * @param broker The broker to use to access the database.
	 * @return A <code>Collection</code> object for the policy collection.
	 */
    public static Collection getPolicyCollection(DBBroker broker) {
        try {
            Collection policyCollection = broker.getCollection(XACMLConstants.POLICY_COLLECTION_URI);
            if (policyCollection == null) {
                TransactionManager transact = broker.getBrokerPool().getTransactionManager();
                Txn txn = transact.beginTransaction();
                try {
                    policyCollection = broker.getOrCreateCollection(txn, XACMLConstants.POLICY_COLLECTION_URI);
                    broker.saveCollection(txn, policyCollection);
                    transact.commit(txn);
                } catch (IOException e) {
                    transact.abort(txn);
                    LOG.error("Error creating policy collection", e);
                    return null;
                } catch (EXistException e) {
                    transact.abort(txn);
                    LOG.error("Error creating policy collection", e);
                    return null;
                } catch (PermissionDeniedException e) {
                    transact.abort(txn);
                    LOG.error("Error creating policy collection", e);
                    return null;
                } catch (TriggerException e) {
                    transact.abort(txn);
                    LOG.error("Error creating policy collection", e);
                    return null;
                }
            }
            return policyCollection;
        } catch (PermissionDeniedException e) {
            LOG.error("Error creating policy collection", e);
            return null;
        }
    }

    /**
	* Returns the single policy (or policy set) document that has the
	* attribute specified by attributeQName with the value
	* attributeValue, null if none match, or throws a
	* <code>ProcessingException</code> if more than one match.  This is
	* performed by a QName range index lookup and so it requires a range
	* index to be given on the attribute.
	* 
	* @param attributeQName The name of the attribute
	* @param attributeValue The value of the attribute
	* @param broker the broker to use to access the database
	* @return The referenced policy.
	* @throws ProcessingException if there is an error finding
	*	the policy (or policy set) documents.
	* @throws XPathException if there is an error performing
	*	the index lookup
	*/
    public DocumentImpl getPolicyDocument(DBBroker broker, QName attributeQName, URI attributeValue) throws ProcessingException, XPathException, PermissionDeniedException {
        DocumentSet documentSet = getPolicyDocuments(broker, attributeQName, attributeValue);
        int documentCount = (documentSet == null) ? 0 : documentSet.getDocumentCount();
        if (documentCount == 0) {
            LOG.warn("Could not find " + attributeQName.getLocalName() + " '" + attributeValue + "'", null);
            return null;
        }
        if (documentCount > 1) {
            throw new ProcessingException("Too many applicable policies for " + attributeQName.getLocalName() + " '" + attributeValue + "'");
        }
        return (DocumentImpl) documentSet.getDocumentIterator().next();
    }

    /**
	* Gets all policy (or policy set) documents that have the
	* attribute specified by attributeQName with the value
	* attributeValue.  This is performed by a QName range index
	* lookup and so it requires a range index to be given
	* on the attribute.
	* 
	* @param attributeQName The name of the attribute
	* @param attributeValue The value of the attribute
	* @param broker the broker to use to access the database
	* @return The referenced policy.
	* @throws ProcessingException if there is an error finding
	*	the policy (or policy set) documents.
	* @throws XPathException if there is an error performing the
	*	index lookup
	*/
    public DocumentSet getPolicyDocuments(DBBroker broker, QName attributeQName, URI attributeValue) throws ProcessingException, XPathException, PermissionDeniedException {
        if (attributeQName == null) return null;
        if (attributeValue == null) return null;
        AtomicValue comparison = new AnyURIValue(attributeValue);
        DocumentSet documentSet = getPolicyDocuments(broker, true);
        NodeSet nodeSet = documentSet.docsToNodeSet();
        NativeValueIndex valueIndex = broker.getValueIndex();
        Sequence results = valueIndex.find(Constants.EQ, documentSet, null, NodeSet.ANCESTOR, attributeQName, comparison);
        return (results == null) ? null : results.getDocumentSet();
    }

    /**
	* Gets the name of the attribute that specifies the policy
	* (if type == PolicyReference.POLICY_REFERENCE) or
	* the policy set (if type == PolicyReference.POLICYSET_REFERENCE).
	*
	* @param type The type of id reference:
	*	PolicyReference.POLICY_REFERENCE for a policy reference
	*	or PolicyReference.POLICYSET_REFERENCE for a policy set
	*	reference.
	* @return The attribute name for the reference type
	*/
    public static QName getIdAttributeQName(int type) {
        if (type == PolicyReference.POLICY_REFERENCE) return new QName(XACMLConstants.POLICY_ID_LOCAL_NAME, XACMLConstants.XACML_POLICY_NAMESPACE); else if (type == PolicyReference.POLICYSET_REFERENCE) return new QName(XACMLConstants.POLICY_SET_ID_LOCAL_NAME, XACMLConstants.XACML_POLICY_NAMESPACE); else return null;
    }

    /**
	* Convenience method for errors occurring while processing.  The message
	* and exception are logged and a <code>PolicyFinderResult</code> is
	* generated with Status.STATUS_PROCESSING_ERROR as the error condition
	* and the message as the message.
	*
	* @param message The message describing the error.
	* @param t The cause of the error, may be null
	* @return A <code>PolicyFinderResult</code> representing the error.
	*/
    public static PolicyFinderResult errorResult(String message, Throwable t) {
        LOG.warn(message, t);
        return new PolicyFinderResult(new Status(Collections.singletonList(Status.STATUS_PROCESSING_ERROR), message));
    }

    /**
	* Obtains a parsed representation of the specified XACML Policy or PolicySet
	* document.  If the document has already been parsed, this method returns the
	* cached <code>AbstractPolicy</code>.  Otherwise, it unmarshals the document into
	* an <code>AbstractPolicy</code> and caches it.
	*
	* @param policyDoc the policy (or policy set) document
	*	for which a parsed representation should be obtained
	* @return a parsed policy (or policy set)
	* @throws ParsingException if an error occurs while parsing the specified document
	*/
    public AbstractPolicy getPolicyDocument(DocumentImpl policyDoc) throws ParsingException {
        String name = policyDoc.getURI().toString();
        AbstractPolicy policy = (AbstractPolicy) POLICY_CACHE.get(name);
        if (policy == null) {
            policy = parsePolicyDocument(policyDoc);
            POLICY_CACHE.put(name, policy);
        }
        return policy;
    }

    /**
	* Parses a DOM representation of a policy document into an
	* <code>AbstractPolicy</code>.
	*
	* @param policyDoc The DOM <code>Document</code> representing
	*	the XACML policy or policy set.
	* @return The parsed policy
	* @throws ParsingException if there is an error parsing the document
	*/
    public AbstractPolicy parsePolicyDocument(Document policyDoc) throws ParsingException {
        Element root = policyDoc.getDocumentElement();
        String name = root.getTagName();
        if (name.equals(XACMLConstants.POLICY_SET_ELEMENT_LOCAL_NAME)) return PolicySet.getInstance(root, pdp.getPDPConfig().getPolicyFinder()); else if (name.equals(XACMLConstants.POLICY_ELEMENT_LOCAL_NAME)) return Policy.getInstance(root); else throw new ParsingException("The root element of the policy document must be '" + XACMLConstants.POLICY_SET_ID_LOCAL_NAME + "' or '" + XACMLConstants.POLICY_SET_ID_LOCAL_NAME + "', was: '" + name + "'");
    }

    /**
	 * Escapes characters that are not allowed in various places
	 * in XML by replacing all invalid characters with
	 * <code>getEscape(c)</code>.
	 * 
	 * @param buffer The <code>StringBuffer</code> containing
	 * the text to escape in place.
	 */
    public static void XMLEscape(StringBuffer buffer) {
        if (buffer == null) return;
        char c;
        String escape;
        for (int i = 0; i < buffer.length(); ) {
            c = buffer.charAt(i);
            escape = getEscape(c);
            if (escape == null) i++; else {
                buffer.replace(i, i + 1, escape);
                i += escape.length();
            }
        }
    }

    /**
	 * Escapes characters that are not allowed in various
	 * places in XML.  Characters are replaced by the
	 * corresponding entity.  The characters &amp;, &lt;,
	 * &gt;, &quot;, and &apos; are escaped.
	 * 
	 * @param c The character to escape.
	 * @return A <code>String</code> representing the
	 * 	escaped character or null if the character does
	 *  not need to be escaped.
	 */
    public static String getEscape(char c) {
        switch(c) {
            case '&':
                return "&amp;";
            case '<':
                return "&lt;";
            case '>':
                return "&gt;";
            case '\"':
                return "&quot;";
            case '\'':
                return "&apos;";
            default:
                return null;
        }
    }

    /**
	 * Escapes characters that are not allowed in various places
	 * in XML by replacing all invalid characters with
	 * <code>getEscape(c)</code>.
	 * 
	 * @param in The <code>String</code> containing
	 * the text to escape in place.
	 */
    public static String XMLEscape(String in) {
        if (in == null) return null;
        StringBuffer temp = new StringBuffer(in);
        XMLEscape(temp);
        return temp.toString();
    }

    /**
	 * Serializes the specified <code>PolicyTreeElement</code> to a
	 * <code>String</code> as XML.  The XML is indented if indent
	 * is true.
	 * 
	 * @param element The <code>PolicyTreeElement</code> to serialize
	 * @param indent If the XML should be indented
	 * @return The XML representation of the element
	 */
    public static String serialize(PolicyTreeElement element, boolean indent) {
        if (element == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (indent) element.encode(out, new Indenter()); else element.encode(out);
        return out.toString();
    }

    /**
	 * Serializes the specified <code>Target</code> to a
	 * <code>String</code> as XML.  The XML is indented if indent
	 * is true.
	 * 
	 * @param target The <code>Target</code> to serialize
	 * @param indent If the XML should be indented
	 * @return The XML representation of the target
	 */
    public static String serialize(Target target, boolean indent) {
        if (target == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (indent) target.encode(out, new Indenter()); else target.encode(out);
        return out.toString();
    }

    /**
	 * Serializes the specified <code>Apply</code> to a
	 * <code>String</code> as XML.  The XML is indented if indent
	 * is true.
	 * 
	 * @param apply The <code>Apply</code> to serialize
	 * @param indent If the XML should be indented
	 * @return The XML representation of the apply
	 */
    public static String serialize(Apply apply, boolean indent) {
        if (apply == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (indent) apply.encode(out, new Indenter()); else apply.encode(out);
        return out.toString();
    }

    /**
	 * Stores the default policies
	 * 
	 * @param broker The broker with which to access the database
	 */
    public static void storeDefaultPolicies(DBBroker broker) {
        LOG.debug("Storing default XACML policies");
        for (int i = 0; i < samplePolicyDocs.length; ++i) {
            XmldbURI docPath = samplePolicyDocs[i];
            try {
                storePolicy(broker, docPath);
            } catch (IOException ioe) {
                LOG.warn("IO Error storing default policy '" + docPath + "'", ioe);
            } catch (EXistException ee) {
                LOG.warn("IO Error storing default policy '" + docPath + "'", ee);
            }
        }
    }

    /**
	 * Stores the resource at docPath into the policies collection.
	 * 
	 * @param broker The broker with which to access the database
	 * @param docPath The location of the resource
	 * @throws EXistException
	 */
    public static void storePolicy(DBBroker broker, XmldbURI docPath) throws EXistException, IOException {
        XmldbURI docName = docPath.lastSegment();
        URL url = XACMLUtil.class.getResource(docPath.toString());
        if (url == null) return;
        String content = toString(url.openStream());
        if (content == null) return;
        Collection collection = getPolicyCollection(broker);
        if (collection == null) return;
        TransactionManager transact = broker.getBrokerPool().getTransactionManager();
        Txn txn = transact.beginTransaction();
        try {
            IndexInfo info = collection.validateXMLResource(txn, broker, docName, content);
            collection.store(txn, broker, info, content, false);
            transact.commit(txn);
        } catch (Exception e) {
            transact.abort(txn);
            if (e instanceof EXistException) throw (EXistException) e;
            throw new EXistException("Error storing policy '" + docPath + "'", e);
        }
    }

    /** Reads an <code>InputStream</code> into a string.
	 * @param in The stream to read into a string.
	 * @return The stream as a string
	 * @throws IOException
	 */
    public static String toString(InputStream in) throws IOException {
        if (in == null) return null;
        Reader reader = new InputStreamReader(in);
        char[] buffer = new char[100];
        CharArrayWriter writer = new CharArrayWriter(1000);
        int read;
        while ((read = reader.read(buffer)) > -1) writer.write(buffer, 0, read);
        return writer.toString();
    }

    public void debug() {
    }
}
