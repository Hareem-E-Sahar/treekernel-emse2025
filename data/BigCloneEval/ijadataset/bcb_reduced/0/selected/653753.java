package picto.io;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import picto.core.*;
import picto.util.Resources;

/**
 *
 * @author davedes
 */
public class ArchiveIO {

    public void write(Archivable arcObj, OutputStream out) throws IOException, ArchiveException {
        ZipOutputStream zip = new ZipOutputStream(out);
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new ArchiveException("parser config error", ex);
        }
        Document doc = builder.newDocument();
        WriterContext context = new WriterContext(doc);
        Element root = arcObj.write(context);
        doc.appendChild(root);
        ZipEntry xmlEntry = new ZipEntry(ArchiveEntry.XML_ENTRY_NAME);
        zip.putNextEntry(xmlEntry);
        Result result = new StreamResult(new OutputStreamWriter(zip, "utf-8"));
        DOMSource source = new DOMSource(doc);
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            factory.setAttribute("indent-number", new Integer(4));
        } catch (IllegalArgumentException exc) {
        }
        try {
            Transformer xformer = factory.newTransformer();
            xformer.setOutputProperty(OutputKeys.INDENT, "yes");
            xformer.transform(source, result);
        } catch (TransformerException ex) {
            throw new ArchiveException("error transforming document", ex);
        }
        zip.closeEntry();
        byte[] buf = new byte[1024];
        for (int i = 0; i < context.entries.size(); i++) {
            ArchiveEntry entry = context.entries.get(i);
            InputStream in = entry.openStream();
            zip.putNextEntry(entry);
            int len;
            while ((len = in.read(buf)) > 0) {
                zip.write(buf, 0, len);
            }
            zip.closeEntry();
            in.close();
        }
        zip.close();
    }

    public ArchiveReader read(Archivable arcObj, InputStream in) throws IOException, ArchiveException {
        ZipInputStream zip = new ZipInputStream(in);
        ArrayList<ArchiveEntry> entries = new ArrayList<ArchiveEntry>();
        byte[] buf = new byte[1024];
        ZipEntry ze;
        ArchiveEntry lastXML = null;
        String xmlName = ArchiveEntry.XML_ENTRY_NAME;
        while ((ze = zip.getNextEntry()) != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int len;
            while ((len = zip.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            final byte[] result = out.toByteArray();
            ArchiveEntry entry = new ArchiveEntry(ze);
            entry.setInputProvider(new ArchiveEntry.Provider() {

                public InputStream createInputStream() {
                    return new ByteArrayInputStream(result);
                }
            });
            entries.add(entry);
            if (xmlName.equals(entry.getName())) {
                lastXML = entry;
            }
        }
        if (lastXML == null) throw new ArchiveException("archive did not contain description file '" + xmlName + "'");
        Document doc = null;
        try {
            InputStream docIn = lastXML.openStream();
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(docIn);
        } catch (SAXException ex) {
            throw new ArchiveException("parsing exception", ex);
        } catch (ParserConfigurationException ex) {
            throw new ArchiveException("parser config error", ex);
        }
        Element root = doc.getDocumentElement();
        ReaderContext context = new ReaderContext(doc, entries);
        arcObj.read(context, root);
        return context;
    }

    protected abstract class AbstractContext implements ArchiveReadWrite {

        protected final List<ArchiveEntry> entries;

        protected final Document doc;

        public AbstractContext(Document doc, List<ArchiveEntry> entries) {
            this.doc = doc;
            this.entries = entries;
        }

        public AbstractContext(Document doc) {
            this(doc, new ArrayList<ArchiveEntry>());
        }

        public Document getDocument() {
            return doc;
        }

        public ArchiveEntry getEntry(String name) {
            for (int i = 0; i < entries.size(); i++) {
                ArchiveEntry e = entries.get(i);
                if (e.getName().equals(name)) return e;
            }
            return null;
        }

        public int getEntryCount() {
            return entries.size();
        }
    }

    protected class ReaderContext extends AbstractContext implements ArchiveReader {

        public ReaderContext(Document doc, List<ArchiveEntry> entries) {
            super(doc, entries);
        }
    }

    protected class WriterContext extends AbstractContext implements ArchiveWriter {

        public WriterContext(Document doc) {
            super(doc);
        }

        public boolean addEntry(ArchiveEntry entry) {
            return entry != null ? entries.add(entry) : false;
        }

        public boolean removeEntry(ArchiveEntry entry) {
            return entry != null ? entries.remove(entry) : false;
        }
    }
}
