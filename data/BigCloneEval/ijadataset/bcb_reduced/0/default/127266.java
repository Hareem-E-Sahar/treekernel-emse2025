import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;

public class TestXsl {

    public static void main(String[] args) {
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            fact.setNamespaceAware(true);
            Transformer trs = TransformerFactory.newInstance().newTransformer(new StreamSource("xslt/struts2step1.xsl"));
            Document src = fact.newDocumentBuilder().parse(new File("project.aswp"));
            trs.transform(new DOMSource(src), new StreamResult(new File("target.xml")));
            trs = TransformerFactory.newInstance().newTransformer(new StreamSource("xslt/struts2step2.xsl"));
            src = fact.newDocumentBuilder().parse(new File("target.xml"));
            trs.transform(new DOMSource(src), new StreamResult(new File("target2.xml")));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
