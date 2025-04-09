import java.net.*;
import java.io.*;
import javax.swing.*;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.w3c.dom.traversal.*;

public class TextResource extends Resource {

    protected Icon smallIcon;

    protected JComponent preview;

    protected Document doc;

    public TextResource(DefaultValuesProvider parentDVP, File resourceFile, File metadataFile) throws Exception {
        super(parentDVP, resourceFile, metadataFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder b = dbf.newDocumentBuilder();
        String stripped = new String();
        String file = resourceFile.getName();
        if (file.endsWith(".text")) {
            doc = b.parse(resourceFile);
            Element root = doc.getDocumentElement();
            NodeIterator ni = ((org.apache.xerces.dom.DocumentImpl) doc).createNodeIterator(root, NodeFilter.SHOW_TEXT, null, false);
            boolean first = true;
            for (Node n = ni.nextNode(); n != null; n = ni.nextNode()) {
                stripped += ((Text) n).getData();
            }
        } else {
            InputStream is = new FileInputStream(resourceFile);
            byte[] buf = new byte[1024];
            is.read(buf, 0, 1024);
            stripped = new String(buf);
        }
        Font baseFont = new Font(null, Font.PLAIN, 10);
        smallIcon = new TextIcon(baseFont.deriveFont(4), stripped, 64, 64);
        if (file.endsWith(".html")) {
            JEditorPane prev = new JEditorPane("file:" + resourceFile);
            prev.setEditable(false);
            preview = new JScrollPane(prev);
            preview.setSize(256, 256);
            preview.setPreferredSize(new Dimension(256, 256));
            preview.setMinimumSize(new Dimension(256, 256));
        } else {
            preview = new JLabel(new TextIcon(baseFont.deriveFont(8), stripped, 256, 256));
        }
    }

    public String getResourceType() {
        return "text";
    }

    public Icon getIcon() {
        return smallIcon;
    }

    public JComponent getPreview() {
        return preview;
    }

    public String checkResource() {
        return "";
    }
}
