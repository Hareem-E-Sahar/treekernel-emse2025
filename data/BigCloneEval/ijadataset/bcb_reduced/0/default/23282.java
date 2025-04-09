import java.io.*;
import java.util.zip.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

class SXCParser {

    static int N = 0;

    Float note_f = new Float(0), note_m = new Float(0);

    SXCParser() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(true);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = null;
            InputStream istr = new ZipFile("essai1.sxc").getInputStream(new ZipEntry("content.xml"));
            doc = db.parse(istr, "file:///home/raph/DOM/SXC/nulldtd/office.dtd");
            FileWriter f1 = new FileWriter("save.xml");
            new org.apache.soap.util.xml.DOM2Writer().serializeAsXML(doc, f1);
            f1.close();
            ZipOutputStream zostr = new ZipOutputStream(new FileOutputStream("essai2.sxc"));
            zostr.putNextEntry(new ZipEntry("content.xml"));
            OutputStreamWriter osw = new OutputStreamWriter(zostr);
            new org.apache.soap.util.xml.DOM2Writer().serializeAsXML(doc, osw);
            osw.close();
            DisplayNotes(doc);
        } catch (ParserConfigurationException pce) {
            System.err.println(pce);
            System.exit(1);
        } catch (org.xml.sax.SAXException se) {
            System.err.println(se.getMessage());
            System.exit(1);
        } catch (IOException ioe) {
            System.err.println(ioe);
            System.exit(1);
        }
    }

    private void DisplayNotes(Node n) {
        if (n == null) return;
        if (n.getNodeName().equals("table:table-cell")) if (n.hasAttributes() && (n.getAttributes().getNamedItem("table:value") != null)) {
            n.appendChild(n.cloneNode(true));
            N = (N + 1) % 3;
            if (N == 1) {
                note_f = new Float(n.getAttributes().getNamedItem("table:value").getNodeValue());
            }
            if (N == 2) note_m = new Float(n.getAttributes().getNamedItem("table:value").getNodeValue());
            if (N == 0) {
                System.out.println("fran�ais " + note_f + " math " + note_m + " moyenne " + (note_f.floatValue() + note_m.floatValue()) / 2);
            }
        }
        NodeList nl = n.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) DisplayNotes(nl.item(i));
    }

    public static void main(String[] s) {
        System.out.println("$Id: SXCParser.java,v 1.3 2002/07/10 15:52:31 raph Exp $");
        new SXCParser();
        System.out.println("end...");
    }
}
