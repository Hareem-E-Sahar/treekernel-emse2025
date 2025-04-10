package org.apache.xalan.xsltc.dom;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Hashtable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import org.apache.xalan.xsltc.DOM;
import org.apache.xalan.xsltc.DOMCache;
import org.apache.xalan.xsltc.DOMEnhancedForDTM;
import org.apache.xalan.xsltc.Translet;
import org.apache.xalan.xsltc.runtime.AbstractTranslet;
import org.apache.xalan.xsltc.runtime.BasisLibrary;
import org.apache.xalan.xsltc.runtime.Constants;
import org.apache.xml.utils.SystemIDResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Morten Jorgensen
 */
public final class DocumentCache implements DOMCache {

    private int _size;

    private Hashtable _references;

    private String[] _URIs;

    private int _count;

    private int _current;

    private SAXParser _parser;

    private XMLReader _reader;

    private XSLTCDTMManager _dtmManager;

    private static final int REFRESH_INTERVAL = 1000;

    public final class CachedDocument {

        private long _firstReferenced;

        private long _lastReferenced;

        private long _accessCount;

        private long _lastModified;

        private long _lastChecked;

        private long _buildTime;

        private DOMEnhancedForDTM _dom = null;

        /**
	 * Constructor - load document and initialise statistics
	 */
        public CachedDocument(String uri) {
            final long stamp = System.currentTimeMillis();
            _firstReferenced = stamp;
            _lastReferenced = stamp;
            _accessCount = 0;
            loadDocument(uri);
            _buildTime = System.currentTimeMillis() - stamp;
        }

        /**
	 * Loads the document and updates build-time (latency) statistics
	 */
        public void loadDocument(String uri) {
            try {
                final long stamp = System.currentTimeMillis();
                _dom = (DOMEnhancedForDTM) _dtmManager.getDTM(new SAXSource(_reader, new InputSource(uri)), false, null, true, false);
                _dom.setDocumentURI(uri);
                final long thisTime = System.currentTimeMillis() - stamp;
                if (_buildTime > 0) _buildTime = (_buildTime + thisTime) >>> 1; else _buildTime = thisTime;
            } catch (Exception e) {
                _dom = null;
            }
        }

        public DOM getDocument() {
            return (_dom);
        }

        public long getFirstReferenced() {
            return (_firstReferenced);
        }

        public long getLastReferenced() {
            return (_lastReferenced);
        }

        public long getAccessCount() {
            return (_accessCount);
        }

        public void incAccessCount() {
            _accessCount++;
        }

        public long getLastModified() {
            return (_lastModified);
        }

        public void setLastModified(long t) {
            _lastModified = t;
        }

        public long getLatency() {
            return (_buildTime);
        }

        public long getLastChecked() {
            return (_lastChecked);
        }

        public void setLastChecked(long t) {
            _lastChecked = t;
        }

        public long getEstimatedSize() {
            if (_dom != null) return (_dom.getSize() << 5); else return (0);
        }
    }

    /**
     * DocumentCache constructor
     */
    public DocumentCache(int size) throws SAXException {
        this(size, null);
        try {
            _dtmManager = (XSLTCDTMManager) XSLTCDTMManager.getDTMManagerClass().newInstance();
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    /**
     * DocumentCache constructor
     */
    public DocumentCache(int size, XSLTCDTMManager dtmManager) throws SAXException {
        _dtmManager = dtmManager;
        _count = 0;
        _current = 0;
        _size = size;
        _references = new Hashtable(_size + 2);
        _URIs = new String[_size];
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
                factory.setFeature(Constants.NAMESPACE_FEATURE, true);
            } catch (Exception e) {
                factory.setNamespaceAware(true);
            }
            _parser = factory.newSAXParser();
            _reader = _parser.getXMLReader();
        } catch (ParserConfigurationException e) {
            BasisLibrary.runTimeError(BasisLibrary.NAMESPACES_SUPPORT_ERR);
        }
    }

    /**
     * Returns the time-stamp for a document's last update
     */
    private final long getLastModified(String uri) {
        try {
            URL url = new URL(uri);
            URLConnection connection = url.openConnection();
            long timestamp = connection.getLastModified();
            if (timestamp == 0) {
                if ("file".equals(url.getProtocol())) {
                    File localfile = new File(URLDecoder.decode(url.getFile()));
                    timestamp = localfile.lastModified();
                }
            }
            return (timestamp);
        } catch (Exception e) {
            return (System.currentTimeMillis());
        }
    }

    /**
     *
     */
    private CachedDocument lookupDocument(String uri) {
        return ((CachedDocument) _references.get(uri));
    }

    /**
     *
     */
    private synchronized void insertDocument(String uri, CachedDocument doc) {
        if (_count < _size) {
            _URIs[_count++] = uri;
            _current = 0;
        } else {
            _references.remove(_URIs[_current]);
            _URIs[_current] = uri;
            if (++_current >= _size) _current = 0;
        }
        _references.put(uri, doc);
    }

    /**
     *
     */
    private synchronized void replaceDocument(String uri, CachedDocument doc) {
        CachedDocument old = (CachedDocument) _references.get(uri);
        if (doc == null) insertDocument(uri, doc); else _references.put(uri, doc);
    }

    /**
     * Returns a document either by finding it in the cache or
     * downloading it and putting it in the cache.
     */
    public DOM retrieveDocument(String baseURI, String href, Translet trs) {
        CachedDocument doc;
        String uri = href;
        if (baseURI != null && !baseURI.equals("")) {
            try {
                uri = SystemIDResolver.getAbsoluteURI(uri, baseURI);
            } catch (TransformerException te) {
            }
        }
        if ((doc = lookupDocument(uri)) == null) {
            doc = new CachedDocument(uri);
            if (doc == null) return null;
            doc.setLastModified(getLastModified(uri));
            insertDocument(uri, doc);
        } else {
            long now = System.currentTimeMillis();
            long chk = doc.getLastChecked();
            doc.setLastChecked(now);
            if (now > (chk + REFRESH_INTERVAL)) {
                doc.setLastChecked(now);
                long last = getLastModified(uri);
                if (last > doc.getLastModified()) {
                    doc = new CachedDocument(uri);
                    if (doc == null) return null;
                    doc.setLastModified(getLastModified(uri));
                    replaceDocument(uri, doc);
                }
            }
        }
        final DOM dom = doc.getDocument();
        if (dom == null) return null;
        doc.incAccessCount();
        final AbstractTranslet translet = (AbstractTranslet) trs;
        translet.prepassDocument(dom);
        return (doc.getDocument());
    }

    /**
     * Outputs the cache statistics
     */
    public void getStatistics(PrintWriter out) {
        out.println("<h2>DOM cache statistics</h2><center><table border=\"2\">" + "<tr><td><b>Document URI</b></td>" + "<td><center><b>Build time</b></center></td>" + "<td><center><b>Access count</b></center></td>" + "<td><center><b>Last accessed</b></center></td>" + "<td><center><b>Last modified</b></center></td></tr>");
        for (int i = 0; i < _count; i++) {
            CachedDocument doc = (CachedDocument) _references.get(_URIs[i]);
            out.print("<tr><td><a href=\"" + _URIs[i] + "\">" + "<font size=-1>" + _URIs[i] + "</font></a></td>");
            out.print("<td><center>" + doc.getLatency() + "ms</center></td>");
            out.print("<td><center>" + doc.getAccessCount() + "</center></td>");
            out.print("<td><center>" + (new Date(doc.getLastReferenced())) + "</center></td>");
            out.print("<td><center>" + (new Date(doc.getLastModified())) + "</center></td>");
            out.println("</tr>");
        }
        out.println("</table></center>");
    }
}
