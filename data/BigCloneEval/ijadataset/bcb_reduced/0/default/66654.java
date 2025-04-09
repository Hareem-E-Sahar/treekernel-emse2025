import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.io.File;

/**
 * Developer:   Cobra
 * Creation:    2003-7-19, 12:35:28
 * Usage:       
 */
public class Demo {

    public static void main(String[] args) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File("DomainModel.xml"));
            Element ele = doc.getDocumentElement();
            System.out.println(ele.getTagName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
