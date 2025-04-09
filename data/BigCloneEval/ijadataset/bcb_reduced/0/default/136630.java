import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.alveole.studio.web.data.LocalEntityResolver;

public class TestXsl {

    public static void main(String[] args) {
        try {
            DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            fact.setNamespaceAware(false);
            fact.setValidating(false);
            Transformer trs = TransformerFactory.newInstance().newTransformer(new StreamSource("xslt/importStruts2.xsl"));
            DocumentBuilder builder = fact.newDocumentBuilder();
            builder.setEntityResolver(new LocalEntityResolver());
            Document src = builder.parse(new File("/home/sylvain/struts.xml"));
            trs.transform(new DOMSource(src), new StreamResult(new File("/home/sylvain/target.xml")));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
