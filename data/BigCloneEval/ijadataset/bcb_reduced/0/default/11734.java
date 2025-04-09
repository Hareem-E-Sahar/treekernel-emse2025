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

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/html");
        String page = req.getPathInfo();
        try {
            if (page == null) {
                showPage("/home.xml", req, res);
                return;
            }
            if (!page.endsWith(".xml")) {
                showPage("/home.xml", req, res);
                return;
            }
            page = page.substring(page.lastIndexOf("/"));
            System.out.println(page);
            showPage(page, req, res);
        } catch (Exception e) {
            res.sendError(res.SC_INTERNAL_SERVER_ERROR, "Hardly anything works. PLEASE inform me...");
            e.printStackTrace();
        }
    }

    private void showPage(String which, HttpServletRequest req, HttpServletResponse res) throws IOException, ParserConfigurationException, SAXException, TransformerException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(getClass().getResourceAsStream("/skeleton.xml"));
        Document nav = db.parse(getClass().getResourceAsStream("/navigation.xml"));
        Document page = db.parse(CDoxWeb.class.getResourceAsStream(which));
        Node body = doc.getElementsByTagName("body").item(0);
        Node pdiv = doc.importNode(page.getElementsByTagName("div").item(0), true);
        Node ndiv = doc.importNode(nav.getElementsByTagName("div").item(0), true);
        body.appendChild(ndiv);
        body.appendChild(pdiv);
        sendDocument(doc, new PrintWriter(res.getWriter()), req, res);
    }

    private void sendDocument(Document doc, PrintWriter out, HttpServletRequest req, HttpServletResponse res) throws IOException, TransformerException {
        Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(getClass().getResourceAsStream("/xml2html.xsl")));
        t.transform(new DOMSource(doc), new StreamResult(out));
    }
}
