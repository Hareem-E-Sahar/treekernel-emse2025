package data.io;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import data.FileHandler;
import data.xmltree.XMLTag;
import data.xmltree.XMLTreeUtil;

public class ZippedXMLExporter implements Exporter {

    Document doc;

    ZipOutputStream zipOut;

    ArrayList<String> usedHandleNames = new ArrayList<String>();

    public ZippedXMLExporter(OutputStream os) {
        zipOut = new ZipOutputStream(os);
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            usedHandleNames.add("Main.xml");
        } catch (ParserConfigurationException ex) {
            System.out.println("Error creating document for ZippedXML export.  That could be real bad.");
            ex.printStackTrace();
        }
    }

    public DataOutputHandle requestDataOutputHandle(String nameRequest, String mimeType) {
        int mod = 1;
        String ext = MimeTypeExtensions.getExtension(mimeType);
        String name = new String(nameRequest) + "." + ext;
        while (usedHandleNames.contains(name)) {
            name = nameRequest + String.valueOf(mod) + "." + ext;
            mod++;
        }
        usedHandleNames.add(name);
        return new ZipDataOutputHandle(name);
    }

    public void export(XMLExportable e, String tagName) {
        XMLTag tag = new XMLTag(tagName);
        e.exportData(tag, this);
        Element elem = XMLTreeUtil.convertToXMLDocElement(doc, tag);
        doc.appendChild(elem);
        try {
            zipOut.putNextEntry(new ZipEntry("Main.xml"));
            FileHandler.writeDocument(doc, zipOut);
            zipOut.close();
        } catch (Exception ex) {
            System.out.println("Error writing main document");
            ex.printStackTrace();
        }
    }

    class ZipDataOutputHandle extends DataOutputHandle {

        public ZipDataOutputHandle(String name) {
            setName(name);
        }

        @Override
        public OutputStream openOutputStream() {
            try {
                ZipEntry entry = new ZipEntry(getName());
                zipOut.putNextEntry(entry);
                return zipOut;
            } catch (Exception ex) {
                System.out.println("Error acquiring output stream for binary data");
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        public void closeOutputStream() {
            try {
                zipOut.closeEntry();
            } catch (Exception ex) {
                System.out.println("Error closing zip entry:" + getName());
                ex.printStackTrace();
            }
        }
    }
}
