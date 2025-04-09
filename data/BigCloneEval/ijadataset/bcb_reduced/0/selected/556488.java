package lrf.epub;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import lrf.Utils;
import org.apache.xpath.jaxp.XPathFactoryImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class EPUBDoc {

    private static Hashtable<String, EPUBDoc> books = new Hashtable<String, EPUBDoc>();

    private static boolean initialized = false;

    String opfPath, opf, tocPath, toc, opfDir;

    NSContext context = new NSContext();

    public File epubFile;

    public Hashtable<String, String> itemsID_MT = new Hashtable<String, String>();

    public Hashtable<String, String> itemsHR_ID = new Hashtable<String, String>();

    public Hashtable<String, String> itemsID_HR = new Hashtable<String, String>();

    public Vector<String> spines = new Vector<String>();

    long fileDate;

    public static void initHandler() {
        String current = System.getProperty("java.protocol.handler.pkgs");
        if (current == null) current = "";
        current += "|lrf";
        System.setProperty("java.protocol.handler.pkgs", current);
        initialized = true;
    }

    public static String toEPUBUrl(File f) {
        String fileName = null;
        try {
            fileName = f.getCanonicalPath();
        } catch (IOException e) {
            return null;
        }
        fileName = fileName.replace(" ", "%20");
        fileName = fileName.replace("\\", "/");
        return "epub://" + fileName + "/";
    }

    private EPUBDoc() {
    }

    public String getOPFDir() {
        return opfDir;
    }

    public static EPUBDoc load(String uri) {
        if (!initialized) initHandler();
        uri = uri.replace("%20", " ");
        if (uri.startsWith("epub://")) uri = uri.substring(7);
        int pos = uri.toLowerCase().indexOf(".epub/");
        if (pos < 0) return null;
        uri = uri.substring(0, pos + 5);
        File f = new File(uri);
        return load(f);
    }

    public static EPUBDoc load(File f) {
        if (!initialized) initHandler();
        EPUBDoc ret = null;
        try {
            String ap = f.getCanonicalPath();
            ret = books.get(ap);
            if (ret == null || ret.fileDate != f.lastModified()) {
                ret = new EPUBDoc();
                ret.fileDate = f.lastModified();
                books.put(ap, ret);
                ret.epubFile = f;
                while (ret.toc == null) {
                    FileInputStream fis = new FileInputStream(ret.epubFile);
                    ZipInputStream zis = new ZipInputStream(fis);
                    ZipEntry entry = null;
                    while ((entry = zis.getNextEntry()) != null) {
                        if (entry.getName().equals("META-INF/container.xml")) {
                            ret.opfPath = ret.getXPathVal("opf", zis, "/container/rootfiles/rootfile/@full-path");
                            int pos = ret.opfPath.lastIndexOf("/");
                            ret.opfDir = ret.opfPath.substring(0, pos + 1);
                        }
                        if (ret.opfPath != null && entry.getName().equals(ret.opfPath)) {
                            ret.opf = ret.getIS(zis);
                            String ncxItem = ret.getXPathVal("opf", ret.opf, "/package/spine/@toc");
                            ret.tocPath = ret.opfDir + ret.getXPathVal("opf", ret.opf, "//item[@id=\"" + ncxItem + "\"]/@href");
                        }
                        if (ret.tocPath != null && entry.getName().equals(ret.tocPath)) {
                            ret.toc = ret.getIS(zis);
                            break;
                        }
                    }
                    zis.close();
                }
                NodeList nl = ret.getNodeSet("opf", ret.opf, "/package/manifest/item");
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);
                    NamedNodeMap nnm = n.getAttributes();
                    ret.itemsID_MT.put(nnm.getNamedItem("id").getNodeValue(), nnm.getNamedItem("media-type").getNodeValue());
                    ret.itemsHR_ID.put(nnm.getNamedItem("href").getNodeValue(), nnm.getNamedItem("id").getNodeValue());
                    ret.itemsID_HR.put(nnm.getNamedItem("id").getNodeValue(), nnm.getNamedItem("href").getNodeValue());
                }
                nl = ret.getNodeSet("opf", ret.opf, "/package/spine/itemref");
                for (int i = 0; i < nl.getLength(); i++) {
                    Node n = nl.item(i);
                    NamedNodeMap nnm = n.getAttributes();
                    ret.spines.add(nnm.getNamedItem("idref").getNodeValue());
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }

    private String getXPathVal(String doct, ZipInputStream zis, String xp) throws Exception {
        String xmlDocStr = getIS(zis);
        return getXPathVal(doct, xmlDocStr, xp);
    }

    private String getXPathVal(String doct, String xmlDocStr, String expres) throws XPathExpressionException {
        xmlDocStr = xmlDocStr.replace("xmlns=", "xmlns:dEf=");
        context.getNSFromXMLDoc(doct, xmlDocStr);
        InputSource is = new InputSource(new StringReader(xmlDocStr));
        XPathFactory xpf = XPathFactoryImpl.newInstance();
        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(context);
        XPathExpression xpe = xpath.compile(expres);
        return (String) xpe.evaluate(is, XPathConstants.STRING);
    }

    private String getIS(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Utils.writeTo(zis, bos);
        return new String(bos.toByteArray());
    }

    public String firstDoc() {
        String href;
        try {
            String item = getXPathVal("opf", opf, "/package/spine/itemref[1]/@idref");
            href = getXPathVal("opf", opf, "/package/manifest/item[@id=\"" + item + "\"]/@href");
        } catch (XPathExpressionException e) {
            return null;
        }
        return href;
    }

    public String getAutor() {
        try {
            return getXPathVal("opf", opf, "//dc:creator/text()");
        } catch (XPathExpressionException e) {
            return null;
        }
    }

    public String getTitle() {
        String ret = null;
        try {
            ret = getXPathVal("opf", opf, "//dc:title/text()");
        } catch (XPathExpressionException e) {
            return null;
        }
        return ret;
    }

    public String getTOC() {
        return toc;
    }

    public String getOPF() {
        return opf;
    }

    public NodeList getNodeSet(String doct, String doc, String exp) throws XPathExpressionException {
        context.getNSFromXMLDoc(doct, doc);
        XPathFactory xpf = XPathFactoryImpl.newInstance();
        XPath xpath = xpf.newXPath();
        xpath.setNamespaceContext(context);
        InputSource is = new InputSource(new StringReader(doc.replace("xmlns=", "xmlns:dEf=")));
        XPathExpression xpe = xpath.compile(exp);
        return (NodeList) xpe.evaluate(is, XPathConstants.NODESET);
    }

    void appendNavPoint2(EPUBMetaData epmd, XMLNode padre, Node navpoint, String prepath) throws XPathExpressionException {
        String toctext = null;
        String url = null;
        Node n = navpoint.getFirstChild();
        XMLNode este = null;
        Vector<Node> navPointChilds = new Vector<Node>();
        while (n != null) {
            if (n.getNodeType() == Node.TEXT_NODE) {
                n = n.getNextSibling();
                continue;
            }
            if (n.getLocalName().equals("navLabel")) {
                for (Node m = n.getFirstChild(); m != null; m = m.getNextSibling()) {
                    if (m.getNodeType() == Node.TEXT_NODE) continue;
                    if (m.getLocalName().equals("text")) {
                        toctext = Utils.toUnhandText(m.getTextContent().trim());
                        break;
                    }
                }
            }
            if (n.getLocalName().equals("content")) {
                url = n.getAttributes().getNamedItem("src").getNodeValue();
            }
            if (este == null && toctext != null && url != null) {
                if (!toctext.equals("Top") && !url.endsWith("#top")) este = epmd.createNavPoint(toctext, prepath + url, padre);
            }
            if (n.getLocalName().equals("navPoint")) {
                navPointChilds.add(n);
            }
            n = n.getNextSibling();
        }
        for (int i = 0; i < navPointChilds.size(); i++) appendNavPoint2(epmd, este, navPointChilds.elementAt(i), prepath);
    }

    public void setNavPoint(EPUBMetaData epmd, String prepath, XMLNode padre) throws XPathExpressionException {
        NodeList nset = getNodeSet("ncx", toc, "//dEf:navPoint");
        for (int i = 0; i < nset.getLength(); i++) {
            appendNavPoint2(epmd, padre, nset.item(i), prepath);
        }
    }

    public NodeList getNavPoints() throws XPathExpressionException {
        return getNodeSet("ncx", toc, "/dEf:ncx/dEf:navMap/dEf:navPoint");
    }

    public InputStream getInputStream(String entName) throws IOException {
        FileInputStream fis = new FileInputStream(epubFile);
        ZipInputStream zis = new ZipInputStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.getName().equals(entName)) {
                Utils.writeTo(zis, baos);
                break;
            }
        }
        zis.close();
        int nt = getSpineIndex(entName);
        if (nt >= 0) lastServedSpine = nt;
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public int getSpineIndex(String ent) {
        ent = ent.substring(opfDir.length());
        String id = itemsHR_ID.get(ent);
        if (id == null) return -1;
        return spines.indexOf(id);
    }

    public int getNumOfDocs() {
        return spines.size();
    }

    int lastServedSpine = -1;

    public InputStream getInputStream(int i) throws IOException {
        if (i < 0 || i >= getNumOfDocs()) return null;
        lastServedSpine = i;
        return getInputStream(itemsID_HR.get(spines.elementAt(i)));
    }

    public int getLastServedSpine() {
        return lastServedSpine;
    }

    public String getRootURL() {
        return getURLForSpine(0);
    }

    public String getURLForSpine(int i) {
        return toEPUBUrl(epubFile) + opfDir + itemsID_HR.get(spines.get(i));
    }
}
