import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class CDoxWeb extends HttpServlet {

    private static final String VERSION = "1.0.3";

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/html");
        String page = req.getPathInfo();
        log(req.getRemoteHost() + " with " + req.getHeader("user-agent") + " -> " + req.getRequestURI());
        try {
            if (page == null) {
                showPage("/home.xml", req, res);
                return;
            }
            if (!page.endsWith(".xml")) {
                showPage("/home.xml", req, res);
                return;
            }
            if (page.endsWith("download.xml")) {
                showDownloadPage(req, res);
            } else {
                page = page.substring(page.lastIndexOf("/"));
                showPage(page, req, res);
            }
        } catch (Exception e) {
            res.sendError(res.SC_INTERNAL_SERVER_ERROR, "Hardly anything works. PLEASE inform me...");
            e.printStackTrace();
        }
    }

    private void showDownloadPage(HttpServletRequest req, HttpServletResponse res) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer(new StreamSource(getClass().getResourceAsStream("/rss2html.xsl")));
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(getClass().getResourceAsStream("/skeleton.xml"));
        Document nav = db.parse(getClass().getResourceAsStream("/navigation.xml"));
        Node body = doc.getElementsByTagName("body").item(0);
        Node ndiv = doc.importNode(nav.getElementsByTagName("div").item(0), true);
        body.appendChild(ndiv);
        DOMResult result = new DOMResult(body);
        t.transform(new StreamSource("http://sourceforge.net/export/rss2_projfiles.php?group_id=48318"), result);
        sendDocument(doc, new PrintWriter(res.getWriter()), req, res);
    }

    private void showPage(String which, HttpServletRequest req, HttpServletResponse res) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(CDoxWeb.class.getResourceAsStream("/skeleton.xml"));
        Document nav = db.parse(CDoxWeb.class.getResourceAsStream("/navigation.xml"));
        Document page = db.parse(CDoxWeb.class.getResourceAsStream(which));
        Node body = doc.getElementsByTagName("body").item(0);
        Node pdiv = doc.importNode(page.getElementsByTagName("div").item(0), true);
        Node ndiv = doc.importNode(nav.getElementsByTagName("div").item(0), true);
        body.appendChild(ndiv);
        body.appendChild(pdiv);
        if (which.endsWith("download.xml")) updateDownloadPage(doc);
        sendDocument(doc, new PrintWriter(res.getWriter()), req, res);
    }

    private void sendDocument(Document doc, PrintWriter out, HttpServletRequest req, HttpServletResponse res) throws IOException, TransformerException {
        updateLinks(doc, req);
        Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(getClass().getResourceAsStream("/xml2html.xsl")));
        t.transform(new DOMSource(doc), new StreamResult(out));
    }

    private void updateDownloadPage(Document doc) {
        Node n = doc.getElementsByTagName("th").item(0);
        n.appendChild(doc.createTextNode("CDox version is " + VERSION));
        NodeList nl = doc.getElementsByTagName("a");
        int count = 1;
        for (int i = 0; i < nl.getLength(); i++) {
            n = nl.item(i).getAttributes().getNamedItem("href");
            if (n.getNodeValue().equals(" ")) {
                StringBuffer sb = new StringBuffer("ftp://sammael.kicks-ass.net/pub/cdox/cdox");
                switch(count) {
                    case 1:
                        sb.append(VERSION + ".exe");
                        break;
                    case 2:
                        sb.append("-1.0.3-1.noarch.rpm");
                        break;
                    case 3:
                        sb.append("-1.0.3-1.src.rpm");
                        break;
                    case 4:
                        ;
                    case 5:
                        ;
                    case 6:
                        sb.append(VERSION);
                        break;
                    case 7:
                        ;
                    case 8:
                        ;
                    case 9:
                        sb.append("_src" + VERSION);
                        break;
                    case 10:
                        ;
                    case 11:
                        ;
                    case 12:
                        sb.append("_doc" + VERSION);
                        break;
                }
                if (count > 3) {
                    switch(count % 3) {
                        case 0:
                            sb.append(".tar.bz2");
                            break;
                        case 1:
                            sb.append(".zip");
                            break;
                        case 2:
                            sb.append(".tgz");
                            break;
                    }
                }
                count++;
                n.setNodeValue(sb.toString());
            }
        }
    }

    private void updateLinks(Document doc, HttpServletRequest req) {
        Node n = doc.getElementsByTagName("title").item(0);
        n.appendChild(doc.createTextNode(doc.getElementsByTagName("h1").item(0).getFirstChild().getNodeValue()));
        NodeList nl = doc.getElementsByTagName("link");
        n = nl.item(0).getAttributes().getNamedItem("href");
        n.setNodeValue(req.getContextPath() + n.getNodeValue());
        nl = doc.getElementsByTagName("a");
        for (int i = 0; i < nl.getLength(); i++) {
            n = nl.item(i).getAttributes().getNamedItem("href");
            if (n.getNodeValue().startsWith("//")) n.setNodeValue(req.getContextPath() + n.getNodeValue().substring(1)); else if (n.getNodeValue().startsWith("/")) n.setNodeValue(req.getContextPath() + "/Show" + n.getNodeValue());
        }
    }
}
