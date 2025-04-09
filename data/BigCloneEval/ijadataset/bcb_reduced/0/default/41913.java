import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.dom.DOMSource;

public class EncoderXes extends Encoder {

    public mjbButton startIndex, clear, saveEnc;

    public JTextPane text;

    /**
   * constructor used by encoder for lists such as index and TOC
   * @param parent main xes class
   * @param str title
   */
    public EncoderXes(xes parent, String str) {
        try {
            this.parent = parent;
            setLayout(new BorderLayout());
            setBorder(new TitledBorder(str));
            addComponents(str);
            addListeners();
        } catch (Exception e) {
            parent.EerrorMsg.text.setText("EncoderXes constructor " + e);
            parent.jtp.setSelectedIndex(3);
        }
    }

    public void addComponents(String str) {
        JPanel bPanel = new JPanel();
        bPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        text = new JTextPane();
        text.setEditable(false);
        startIndex = new mjbButton("generate xes", KeyEvent.VK_G, this, this);
        saveEnc = new mjbButton("save to file", KeyEvent.VK_S, this, this);
        clear = new mjbButton("Clear", KeyEvent.VK_C, this, this);
        bPanel.add(startIndex);
        bPanel.add(saveEnc);
        bPanel.add(clear);
        add(new JScrollPane(text), BorderLayout.CENTER);
        add(bPanel, BorderLayout.SOUTH);
    }

    /** called when button pressed in this encoder
 * @param ae
 *
 */
    public void actionPerformed(ActionEvent ae) {
        Object object = ae.getSource();
        if (object == clear) {
            text.setText("");
        } else if (object == startIndex) {
            generateXES(parent.baseNode);
        } else if (object == saveEnc) {
            saveXES(parent.baseNode);
        }
    }

    public void addListeners() {
        if (startIndex != null) {
            startIndex.addMouseListener(new MyMouseListener());
        }
        if (saveEnc != null) {
            saveEnc.addMouseListener(new MyMouseListener());
        }
        clear.addMouseListener(new MyMouseListener());
    }

    public void saveXES(nodeBase baseNode) {
        try {
            JFileChooser jfc = new JFileChooser();
            if (EncoderLoad.directory == null) EncoderLoad.directory = new File(System.getProperty("user.dir"));
            jfc.setCurrentDirectory(EncoderLoad.directory);
            int retValue = jfc.showSaveDialog(parent);
            File baseDirectoryFile = new File("");
            if (retValue == JFileChooser.APPROVE_OPTION) {
                ((EncoderLoad) (parent.ELoad)).setBaseDir(jfc.getCurrentDirectory());
                baseDirectoryFile = jfc.getSelectedFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(baseDirectoryFile));
                out.write(text.getText());
                out.close();
            }
        } catch (Exception e) {
            parent.EerrorMsg.text.setText("EncoderXes.saveXES " + e);
            parent.jtp.setSelectedIndex(3);
        }
    }

    public void generateXES(nodeBase baseNode) {
        if (baseNode == null) {
            text.setText("source not loaded");
            return;
        }
        try {
            org.w3c.dom.Document doc = parent.generateDomTree();
            if (doc == null) {
                System.out.println("no program loaded ");
                return;
            }
            org.w3c.dom.Node root = doc.getFirstChild();
            String xsltStr = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" + "<xsl:output method=\"xml\" indent=\"yes\"/>" + "<xsl:template match=\"node() | @*\">" + "<xsl:copy>" + "<xsl:apply-templates select=\"@* | node()\"/>" + "</xsl:copy>" + "</xsl:template>" + "</xsl:stylesheet>";
            StringReader sr = new StringReader(xsltStr);
            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer xform = tfactory.newTransformer(new StreamSource(sr));
            DOMSource ds = new DOMSource(root);
            StringWriter sw = new StringWriter();
            xform.transform(ds, new StreamResult(sw));
            text.setText(sw.toString());
        } catch (Exception e) {
            parent.EerrorMsg.text.setText("EncoderXes.generateXES " + e);
            parent.jtp.setSelectedIndex(3);
        }
    }
}
