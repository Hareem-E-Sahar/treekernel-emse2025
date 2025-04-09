package be.vds.jtbdive.client.core.conversion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import be.vds.jtbdive.client.actions.ImpExDocumentHandler;
import be.vds.jtbdive.client.core.DiveSiteManagerFacade;
import be.vds.jtbdive.client.core.LogBookManagerFacade;
import be.vds.jtbdive.client.core.LogBookUtilities;
import be.vds.jtbdive.client.util.XsdValidator;
import be.vds.jtbdive.core.core.Dive;
import be.vds.jtbdive.core.core.DiveSite;
import be.vds.jtbdive.core.core.Diver;
import be.vds.jtbdive.core.core.LogBook;
import be.vds.jtbdive.core.core.catalogs.DocumentFormat;
import be.vds.jtbdive.core.core.material.Material;
import be.vds.jtbdive.core.exceptions.DataStoreException;
import be.vds.jtbdive.core.exceptions.XMLValidationException;
import be.vds.jtbdive.core.logging.Syslog;
import be.vds.jtbdive.core.xml.DiveSiteParser;
import be.vds.jtbdive.core.xml.DiverParser;
import be.vds.jtbdive.core.xml.LogBookParser;

/**
 * This parser is written to deal with UDDF standard V2.2.0. To see which tags
 * are filled, please refer to the write method.s
 * 
 * @author gautier
 */
public class ImpExJTBHandler implements ImpExDocumentHandler {

    private static final Syslog LOGGER = Syslog.getLogger(ImpExJTBHandler.class);

    private File file;

    public ImpExJTBHandler(File file) {
        this.file = file;
    }

    public void write(DiveSiteManagerFacade diveSiteManagerFacade, LogBookManagerFacade logBookManagerFacade, List<Dive> dives, LogBook logBook, File dest) throws IOException, XMLValidationException, DataStoreException {
        write(dives, logBook, dest, null, diveSiteManagerFacade, logBookManagerFacade);
    }

    /**
	 * 
	 * 
	 * @param dives
	 * @param owner
	 * @param outputStream
	 * @throws IOException
	 * @throws XMLValidationException
	 * @throws DataStoreException
	 */
    public void write(List<Dive> dives, LogBook logBook, File dest, InputStream xsdInputStream, DiveSiteManagerFacade diveSiteManagerFacade, LogBookManagerFacade logBookManagerFacade) throws IOException, XMLValidationException, DataStoreException {
        List<DiveSite> diveSites = LogBookUtilities.getDiveSites(dives, DiveSite.LOAD_MEDIUM, diveSiteManagerFacade);
        List<Diver> divers = LogBookUtilities.getDivers(dives, logBook.getOwner());
        List<Material> materials = LogBookUtilities.getMaterials(dives);
        String divesString = getLogBookXMLString(logBook, dives, diveSites, divers, materials, xsdInputStream);
        String diveSitesString = getDiveSitesXMLString(logBook, dives, diveSites, divers, xsdInputStream);
        String diversString = getDiversXMLString(logBook, dives, diveSites, divers, xsdInputStream);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dest));
        ZipEntry ze = new ZipEntry("logbook.xml");
        zos.putNextEntry(ze);
        zos.write(divesString.getBytes("UTF-8"));
        zos.flush();
        ze = new ZipEntry("divers.xml");
        zos.putNextEntry(ze);
        zos.write(diversString.getBytes("UTF-8"));
        zos.flush();
        ze = new ZipEntry("divesites.xml");
        zos.putNextEntry(ze);
        zos.write(diveSitesString.getBytes("UTF-8"));
        zos.flush();
        writeDiveSiteDocuments(zos, diveSites, diveSiteManagerFacade);
        writeDiveDocuments(zos, dives, logBookManagerFacade);
        zos.close();
        LOGGER.info("Export done.");
    }

    private String getDiveSitesXMLString(LogBook logBook, List<Dive> dives, List<DiveSite> diveSites, List<Diver> divers, InputStream xsdInputStream) throws IOException {
        DiveSiteParser p = new DiveSiteParser();
        Element dlsEl = new Element("divesites");
        for (DiveSite diveLocation : diveSites) {
            dlsEl.addContent(p.createDiveSiteElement(diveLocation));
        }
        Document doc = new Document();
        doc.setRootElement(dlsEl);
        Format format = Format.getPrettyFormat();
        format.setEncoding("UTF-8");
        XMLOutputter outputter = new XMLOutputter(format);
        return outputter.outputString(doc);
    }

    private String getDiversXMLString(LogBook logBook, List<Dive> dives, List<DiveSite> diveSites, List<Diver> divers, InputStream xsdInputStream) throws IOException {
        DiverParser pa = new DiverParser();
        Element diversEl = new Element("divers");
        for (Diver diver : divers) {
            diversEl.addContent(pa.createDiverElement(diver));
        }
        Document doc = new Document();
        doc.setRootElement(diversEl);
        Format format = Format.getPrettyFormat();
        format.setEncoding("UTF-8");
        XMLOutputter outputter = new XMLOutputter(format);
        return outputter.outputString(doc);
    }

    private void writeDiveDocuments(ZipOutputStream zos, List<Dive> dives, LogBookManagerFacade logBookManagerFacade) throws IOException {
        String base = "documents" + File.separatorChar + "dives";
        for (Dive dive : dives) {
            if (dive.getDocuments() != null) {
                ZipEntry ze = null;
                for (be.vds.jtbdive.core.core.Document doc : dive.getDocuments()) {
                    try {
                        ze = new ZipEntry(base + File.separatorChar + doc.getId() + "." + doc.getDocumentFormat().getExtension());
                        zos.putNextEntry(ze);
                        zos.write(logBookManagerFacade.loadDocumentContent(doc.getId(), doc.getDocumentFormat()));
                        zos.flush();
                    } catch (DataStoreException e) {
                        LOGGER.error(e);
                    }
                }
            }
        }
    }

    private void writeDiveSiteDocuments(ZipOutputStream zos, List<DiveSite> diveSites, DiveSiteManagerFacade diveSiteManagerFacade) throws IOException {
        String base = "documents" + File.separatorChar + "divesites";
        for (DiveSite diveSite : diveSites) {
            if (diveSite.getDocuments() != null) {
                ZipEntry ze = null;
                for (be.vds.jtbdive.core.core.Document doc : diveSite.getDocuments()) {
                    try {
                        ze = new ZipEntry(base + File.separatorChar + doc.getId() + "." + doc.getDocumentFormat().getExtension());
                        zos.putNextEntry(ze);
                        zos.write(diveSiteManagerFacade.loadDocumentContent(doc.getId(), doc.getDocumentFormat()));
                        zos.flush();
                    } catch (DataStoreException e) {
                        LOGGER.error(e);
                    }
                }
            }
        }
    }

    private String getLogBookXMLString(LogBook logBook, List<Dive> dives, List<DiveSite> diveSites, List<Diver> divers, List<Material> materials, InputStream xsdInputStream) throws XMLValidationException, IOException {
        Document doc = new Document();
        doc.setRootElement(getRootElement(logBook, dives, materials));
        if (null != xsdInputStream) {
            boolean b = XsdValidator.validXML(doc, xsdInputStream);
            if (!b) {
                throw new XMLValidationException("The XML generated doesn't fulfill the XSD udcf.xsd");
            }
        }
        Format format = Format.getPrettyFormat();
        format.setEncoding("UTF-8");
        XMLOutputter outputter = new XMLOutputter(format);
        return outputter.outputString(doc);
    }

    private Element getRootElement(LogBook logBook, List<Dive> dives, List<Material> materials) {
        LogBookParser lbP = new LogBookParser();
        return lbP.createLogBookRootElement(logBook, dives, materials);
    }

    public LogBook read() throws DataStoreException {
        LogBook logbook = null;
        try {
            SAXBuilder sb = new SAXBuilder();
            InputStream is = getResource("divesites.xml");
            Document doc = sb.build(is);
            Map<Long, DiveSite> diveLocationMap = new HashMap<Long, DiveSite>();
            DiveSiteParser dlp = new DiveSiteParser();
            for (Iterator iterator = doc.getRootElement().getChildren("divesite").iterator(); iterator.hasNext(); ) {
                Element dlEl = (Element) iterator.next();
                DiveSite dl = dlp.readDiveSiteElement(dlEl);
                diveLocationMap.put(dl.getId(), dl);
            }
            is = getResource("divers.xml");
            doc = sb.build(is);
            Map<Long, Diver> diverMap = new HashMap<Long, Diver>();
            DiverParser dp = new DiverParser();
            for (Iterator iterator = doc.getRootElement().getChildren("diver").iterator(); iterator.hasNext(); ) {
                Element dEl = (Element) iterator.next();
                Diver d = dp.readDiver(dEl);
                diverMap.put(d.getId(), d);
            }
            is = getResource("logbook.xml");
            doc = sb.build(is);
            LogBookParser p = new LogBookParser();
            logbook = p.readLogBook(doc.getRootElement(), diveLocationMap, diverMap);
        } catch (JDOMException e) {
            LOGGER.error("JDom Exception while reading jtb file : " + e.getMessage());
            throw new DataStoreException(e);
        } catch (ZipException e) {
            LOGGER.error("ZIP Exception while reading jtb file : " + e.getMessage());
            throw new DataStoreException(e);
        } catch (IOException e) {
            LOGGER.error("IO Exception while reading jtb file : " + e.getMessage());
            throw new DataStoreException(e);
        }
        return logbook;
    }

    private InputStream getResource(String name) throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(file);
        ZipEntry ze = zipFile.getEntry(name);
        if (ze == null) {
            throw new ZipException("Resource " + name + " not found in the zip file");
        }
        InputStream is = zipFile.getInputStream(ze);
        return is;
    }

    public byte[] getDiveSiteDocumentContent(long id, DocumentFormat documentFormat) throws DataStoreException {
        try {
            String fileName = "documents/divesites/" + id + "." + documentFormat.getExtension();
            InputStream is = getResource(fileName);
            return readDocumentContent(is);
        } catch (ZipException e) {
            LOGGER.error("ZIP Exception while reading jtb file : " + e.getMessage());
            throw new DataStoreException(e);
        } catch (IOException e) {
            LOGGER.error("ZIP Exception while reading jtb file : " + e.getMessage());
            throw new DataStoreException(e);
        }
    }

    public byte[] getDiveDocumentContent(long id, DocumentFormat documentFormat) throws DataStoreException {
        try {
            String fileName = "documents" + File.separatorChar + "dives" + File.separatorChar + id + "." + documentFormat.getExtension();
            InputStream is = getResource(fileName);
            return readDocumentContent(is);
        } catch (ZipException e) {
            LOGGER.error("ZIP Exception while reading jtb file : " + e.getMessage());
            throw new DataStoreException(e);
        } catch (IOException e) {
            LOGGER.error("ZIP Exception while reading jtb file : " + e.getMessage());
            throw new DataStoreException(e);
        }
    }

    private byte[] readDocumentContent(InputStream is) throws IOException {
        List<Integer> contentList = new ArrayList<Integer>();
        boolean read = true;
        while (read) {
            int b = is.read();
            if (b == -1) {
                read = false;
            } else {
                contentList.add(b);
            }
        }
        byte[] content = new byte[contentList.size()];
        int i = 0;
        for (Integer integer : contentList) {
            content[i] = integer.byteValue();
            i++;
        }
        return content;
    }
}
