package ubibook.epub;

import ubibook.*;
import ubibook.meta.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/**
 * Describe class <code>EpubExporter</code> here.
 *
 * @author <a href="mailto:thomas.kleinbauer@dfki.de">Thomas Kleinbauer</a>
 * @version 1.0
 */
public class EpubExporter implements Exporter {

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final String MIME_TYPE_NAME = "mimetype";

    public static final String MIME_TYPE_VALUE = "application/epub+zip";

    public static final String CONTAINER_NAME = "META-INF/container.xml";

    public static final String PACKAGE_INFO_NAME = "content/content.opf";

    private Charset charset;

    private DocumentBuilder documentBuilder;

    private Transformer transformer;

    private Map<String, Exporter> exporters;

    public EpubExporter() throws ExporterConfigurationException {
        this(Charset.forName(DEFAULT_CHARSET));
    }

    public EpubExporter(Charset charset) throws ExporterConfigurationException {
        this.charset = charset;
        this.exporters = new HashMap<String, Exporter>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            documentBuilder = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            throw new ExporterConfigurationException(pce);
        }
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        } catch (TransformerConfigurationException tce) {
            throw new ExporterConfigurationException(tce);
        }
    }

    public String getFormat() {
        return "epub+zip";
    }

    public Exporter getExporter(String mediaType) throws FormatException {
        Exporter ret = exporters.get(mediaType);
        if (ret == null) {
            throw new FormatException("Unsupported media type: " + mediaType);
        }
        return ret;
    }

    public void write(Resource resource, File file) throws FileNotFoundException, ExportException {
        if (file == null) {
            throw new IllegalArgumentException("Can't export to 'null' file.");
        }
        write(resource, new FileOutputStream(file));
    }

    public void write(Resource resource, OutputStream stream) throws ExportException {
        if (resource == null) {
            throw new IllegalArgumentException("Can't export 'null' resource.");
        }
        if (stream == null) {
            throw new IllegalArgumentException("Can't export to 'null' stream.");
        }
        ZipOutputStream zip = new ZipOutputStream(stream);
        try {
            ZipEntry mimeType = new ZipEntry(MIME_TYPE_NAME);
            mimeType.setMethod(ZipEntry.STORED);
            zip.putNextEntry(mimeType);
            zip.write(MIME_TYPE_VALUE.getBytes(charset));
            zip.closeEntry();
            ZipEntry container = new ZipEntry(CONTAINER_NAME);
            zip.putNextEntry(container);
            writeContainer(resource, zip);
            zip.closeEntry();
            ZipEntry packageInfo = new ZipEntry(PACKAGE_INFO_NAME);
            zip.putNextEntry(packageInfo);
            writePackageInfo(resource, zip);
            zip.closeEntry();
            zip.finish();
        } catch (IOException io) {
            throw new ExportException(io);
        } catch (TransformerException t) {
            throw new ExportException(t);
        } catch (FormatException f) {
            throw new ExportException(f);
        }
    }

    protected void writeContainer(Resource resource, OutputStream out) throws IOException, TransformerException {
        Document doc = documentBuilder.newDocument();
        Element documentElement = doc.createElement("container");
        documentElement.setAttribute("version", "1.0");
        documentElement.setAttribute("xmlns", "urn:oasis:names:tc:opendocument:xmlns:container");
        doc.appendChild(documentElement);
        Element rootfiles = doc.createElement("rootfiles");
        documentElement.appendChild(rootfiles);
        Element rootfile = doc.createElement("rootfile");
        rootfile.setAttribute("full-path", PACKAGE_INFO_NAME);
        rootfile.setAttribute("media-type", "application/oebps-package+xml");
        rootfiles.appendChild(rootfile);
        transformer.transform(new DOMSource(doc), new StreamResult(out));
        out.flush();
    }

    protected void writePackageInfo(Resource resource, OutputStream out) throws IOException, TransformerException, FormatException {
        Document doc = documentBuilder.newDocument();
        Element documentElement = doc.createElement("package");
        documentElement.setAttribute("version", "2.0");
        documentElement.setAttribute("xmlns", "http://www.idpf.org/2007/opf");
        documentElement.setAttribute("unique-identifier", getValue(resource, DublinCore.IDENTIFIER, "UNKNOWN"));
        doc.appendChild(documentElement);
        Element metadata = doc.createElement("metadata");
        documentElement.appendChild(metadata);
        Element manifest = doc.createElement("manifest");
        Queue<Resource> queue = new LinkedList<Resource>(resource.getEmbeddedResources());
        while (!queue.isEmpty()) {
            Resource embedded = queue.poll();
            Element item = doc.createElement("item");
            String mediaType = embedded.getMediaType();
            String mediaSubtype = getExporter(mediaType).getFormat();
            item.setAttribute("media-type", mediaType + "/" + mediaSubtype);
        }
        documentElement.appendChild(manifest);
        Element spine = doc.createElement("spine");
        documentElement.appendChild(spine);
        Element guide = doc.createElement("guide");
        documentElement.appendChild(guide);
        transformer.transform(new DOMSource(doc), new StreamResult(out));
        out.flush();
    }

    private String getValue(Resource resource, String metadataName, String defaultValue) {
        Metadata metadata = resource.getMetadata(metadataName);
        String ret;
        if (metadata == null) {
            ret = defaultValue;
        } else {
            ret = metadata.getAttribute(Metadata.VALUE);
        }
        return ret;
    }

    public static void main(String[] args) throws Exception {
        EpubExporter main = new EpubExporter();
        FileOutputStream out = new FileOutputStream("/tmp/dummy.epub");
        Resource resource = null;
        main.write(resource, out);
        out.close();
    }
}
