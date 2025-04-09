import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.Font;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;

public class TextResource extends Resource {

    protected TextIcon smallIcon;

    protected TextIcon bigIcon;

    protected Document doc;

    public TextResource(DefaultValuesProvider parentDVP, URL resourceURL, File metadataFile) throws Exception {
        super(parentDVP, resourceURL, metadataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder b = dbf.newDocumentBuilder();
        String stripped = new String();
        String file = resourceURL.getFile();
        if (file.endsWith(".text")) {
            doc = b.parse(resourceURL.openStream());
            Element root = doc.getDocumentElement();
            NodeIterator ni = ((org.apache.xerces.dom.DocumentImpl) doc).createNodeIterator(root, NodeFilter.SHOW_TEXT, null, false);
            boolean first = true;
            for (Node n = ni.nextNode(); n != null; n = ni.nextNode()) {
                stripped += ((Text) n).getData();
            }
        } else {
            InputStream is = resourceURL.openStream();
            byte[] buf = new byte[1024];
            is.read(buf, 0, 1024);
            stripped = new String(buf);
        }
        Font baseFont = new Font(null, Font.PLAIN, 10);
        smallIcon = new TextIcon(baseFont.deriveFont(4), stripped, 64, 64);
        bigIcon = new TextIcon(baseFont.deriveFont(8), stripped, 256, 256);
    }

    public String getResourceType() {
        return "text";
    }

    public String getStringIdentifier() {
        return resourceURL.toString();
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
