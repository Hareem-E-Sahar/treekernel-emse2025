import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.Font;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;

public class GalleryResource extends Resource {

    protected TextIcon smallIcon;

    protected TextIcon bigIcon;

    protected Document doc;

    public GalleryResource(DefaultValuesProvider parentDVP, URL resourceURL, File metadataFile) throws Exception {
        super(parentDVP, resourceURL, metadataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder b = dbf.newDocumentBuilder();
        doc = b.parse(resourceURL.openStream());
        Element root = doc.getDocumentElement();
        NodeList nl = root.getChildNodes();
        String stripped = new String();
        boolean first = true;
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) n;
                stripped += e.getTagName() + ": " + e.getAttribute("lcref") + "\n";
            }
        }
        Font baseFont = new Font(null, Font.PLAIN, 10);
        smallIcon = new TextIcon(baseFont.deriveFont(4), stripped, 64, 64);
        bigIcon = new TextIcon(baseFont.deriveFont(8), stripped, 256, 256);
    }

    public String getResourceType() {
        return "gallery";
    }

    public Icon getIcon() {
        return smallIcon;
    }

    public JComponent getPreview() {
        return new JLabel(bigIcon);
    }

    public String checkResource() {
        return "";
    }
}
