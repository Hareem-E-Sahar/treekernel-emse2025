package org.fao.waicent.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.DocumentImpl;
import org.fao.waicent.kids.Configuration;
import org.fao.waicent.util.Debug;
import org.fao.waicent.util.FileResource;
import org.fao.waicent.util.Translate;
import org.fao.waicent.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class AttributesExternalizer extends BaseAttributesExternalizer {

    final int version = 14;

    private int UPDATED_BY = -1;

    public FileResource fileresource = null;

    String ID = null;

    String themeID = null;

    /**
    * ali.safarnejad:20061031 added to fix problem with new datasets showing name of csv file,
    * instead of the chosen label of the dataset.
    */
    public AttributesExternalizer(FileResource fileresource, String name) throws IOException {
        this.fileresource = fileresource;
        labels = new Translate(name);
    }

    public AttributesExternalizer(FileResource fileresource) throws IOException {
        this.fileresource = fileresource;
        labels = new Translate(fileresource.getName());
    }

    public AttributesExternalizer(Document doc, Element ele) throws IOException {
        load(doc, ele);
    }

    public AttributesExternalizer(Document doc, Element ele, String lang) throws IOException {
        load(doc, ele, lang);
    }

    public void setFileResource(FileResource fileresource) {
        this.fileresource = fileresource;
    }

    public FileResource getFileResource() {
        return fileresource;
    }

    public void save(Document doc, Element ele) throws IOException {
        save(doc, ele, "en");
    }

    public void save(Document doc, Element ele, String lang) throws IOException {
        XMLUtil.setType(doc, ele, this);
        ele.setAttribute("resource", fileresource.getResource());
        if (fileresource.getName() != null) {
            ele.setAttribute("name", fileresource.getName());
        }
        saveLabel(doc, ele, lang);
    }

    public void saveLabel(Document doc, Element ele, String lang) {
        Element label_element = doc.createElement("Label");
        if (labels != null) {
            labels.appendToElement(label_element);
        } else {
            labels = new Translate(fileresource.getName(), label_element);
            labels.addLabel(lang, fileresource.getName());
            label_element.setAttribute(lang, fileresource.getName());
        }
        ele.appendChild(label_element);
    }

    public void load(Document doc, Element ele, String lang) throws IOException {
        XMLUtil.checkType(doc, ele, this);
        String resource = ele.getAttribute("resource");
        String name = ele.getAttribute("name");
        ID = ele.getAttribute("ID");
        themeID = ele.getAttribute("themeID");
        if (name == null) {
            name = resource;
        }
        loadLabel(doc, ele, name, lang);
        fileresource = new FileResource(name, resource);
    }

    public void loadLabel(Document doc, Element ele, String name, String lang) {
        try {
            Element label_element = XMLUtil.getChild(doc, ele, "Label");
            if (label_element != null) {
                labels = new Translate(name, label_element);
            } else {
                labels = new Translate(name);
                labels.addLabel(lang, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load(Document doc, Element ele) throws IOException {
        load(doc, ele, "en");
    }

    public Class getObjectClass() throws ClassNotFoundException {
        return Class.forName("org.fao.waicent.attributes.Attributes");
    }

    public void appendObject(Object o) throws ExternalizerException {
    }

    public Object loadObject() throws ExternalizerException {
        Attributes a = null;
        try {
            a = loadAttributes();
        } catch (IOException e) {
            throw new ExternalizerException();
        }
        return a;
    }

    public void deleteObject() throws ExternalizerException {
        fileresource.delete();
    }

    public void deleteObject(Object o) throws ExternalizerException {
    }

    public void saveObject(Object o) throws ExternalizerException {
        Attributes a = (Attributes) o;
        try {
            saveAttributes(a);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            throw new ExternalizerException();
        }
    }

    protected Attributes loadAttributes() throws IOException {
        Attributes a = null;
        String extension = fileresource.getUpperCaseExtension();
        if ("XML".equals(extension)) {
        } else {
            InputStream in = null;
            try {
                in = fileresource.openInputStream();
                a = loadAttributesBinary(in);
            } catch (Exception e) {
                e.printStackTrace(System.out);
                throw new IOException(e.getMessage());
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
        a.setIndicators(loadIndicatorsXML(a.getExtents()));
        try {
            if (loadXMLDocument("navigator.xml") != null) {
                a.setNavigatorXML(loadXMLDocument("navigator.xml"));
            }
        } catch (Exception e) {
        }
        a.setName(fileresource.getName());
        a.postConstructorCheck();
        return a;
    }

    public void saveExtentsXML(Attributes a) throws IOException {
        Runtime r = Runtime.getRuntime();
        Document doc = new DocumentImpl();
        a.getExtents().toXML(doc, this.configuration.getLanguageCode());
        r.gc();
        saveXMLDocument("extent.xml", doc);
        r.gc();
    }

    public void saveAttributeXML(Attributes a) throws IOException {
        try {
            Runtime r = Runtime.getRuntime();
            Document doc = new DocumentImpl();
            Element root_element = doc.createElement("ROOT");
            root_element.setAttribute("file_class_name", a.getClass().getName());
            root_element.setAttribute("file_version", Integer.toString(this.getVersion()));
            root_element.setAttribute("time_key_index", Integer.toString(a.getTimeKeyIndex()));
            root_element.setAttribute("name", a.getName());
            doc.appendChild(root_element);
            if (a.getPrecisionExtent() != null) {
                a.getPrecisionExtent().toXML(doc);
            }
            if (a.getUnitExtent() != null) {
                a.getUnitExtent().toXML(doc);
            }
            if (a.getNoteExtent() != null) {
                a.getNoteExtent().toXML(doc);
            }
            if (a.getLinkExtent() != null) {
                a.getLinkExtent().toXML(doc);
            }
            if (a.getSourceExtent() != null) {
                a.getSourceExtent().toXML(doc);
            }
            if (a.getGraphExtent() != null) {
                a.getGraphExtent().toXML(doc);
            }
            if (a.getDataLegendExtent() != null) {
                a.getDataLegendExtent().toXML(doc);
            }
            r.gc();
            saveXMLDocument("attributes.xml", doc);
            r.gc();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new IOException("Error :: AttributesExternalizerMultiSource :: saveAttributeXML :: " + e.getClass() + " " + e.getMessage());
        }
    }

    public void saveAttributes(Attributes a) throws IOException {
        String extension = fileresource.getUpperCaseExtension();
        saveExtentsXML(a);
        saveAttributeXML(a);
        if ("XML".equals(extension)) {
        } else {
            File file = new File(getBinaryFilename());
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zout = new ZipOutputStream(fos);
            zout.putNextEntry(new ZipEntry("Attributes"));
            DataOutputStream out = new DataOutputStream(zout);
            saveAttributesBinary(out, a);
            out.close();
            fos.close();
        }
        String extent_tree_filename_stem = "extent_tree_";
        for (int i = 0; i < a.getExtents().size(); i++) {
            try {
                saveXMLDocument(extent_tree_filename_stem + i + ".xml", a.getExtents().at(i).getExtentXML());
            } catch (Exception e) {
            }
        }
        if (a.getIndicators() != null) {
            saveIndicatorsXML(a.getIndicators());
        }
        if (a.getNavigatorXML() != null) {
            saveXMLDocument("navigator.xml", a.getNavigatorXML());
        }
    }

    protected Attributes loadAttributesBinary(InputStream in_strm) throws IOException {
        Attributes a = new Attributes();
        DataInputStream in = new DataInputStream(in_strm);
        String file_class_name = in.readUTF();
        if (a.getClass().getName().compareTo(file_class_name) != 0) {
            String message = getClass().getName() + " can't load " + file_class_name;
            throw new IOException(message);
        }
        int file_version = in.readInt();
        if (file_version > version) {
            System.out.println(getClass().getName() + " replacing file version " + file_version + " with " + version);
            file_version = version;
        }
        if (file_version > 2) {
            a.setTimeKeyIndex(in.readInt());
        }
        a.setName(in.readUTF());
        if (file_version >= 11) {
            a.setID(in.readUTF());
            a.setTheme(in.readUTF());
        }
        try {
            Document doc = this.loadXMLDocument("extent.xml");
            if (doc != null) {
                a.setExtents(new ExtentManager(in, doc, doc.getDocumentElement()));
            } else {
                a.setExtents(new ExtentManager(in));
            }
        } catch (Exception e) {
            e.printStackTrace();
            a.setExtents(new ExtentManager(in));
        }
        setXMLExtents((ExtentManager) a.getExtents());
        a.setMatrix(new Matrix(in));
        a.setPrecisionExtent(new PrecisionExtent(in, new Integer(file_version)));
        a.setUnitExtent(new UnitExtent(in, new Integer(file_version)));
        a.setDataLegendExtent(new DataLegendExtent(in, new Integer(file_version)));
        a.setGraphExtent(new GraphExtent(in, new Integer(file_version)));
        a.setSourceExtent(new SourceExtent(in, new Integer(file_version)));
        a.setNoteExtent(new NoteExtent(in, new Integer(file_version)));
        a.setLinkExtent(new LinkExtent(in, new Integer(file_version)));
        if (file_version >= 9) {
            a.setReferenceExtent(new ReferenceExtent(in, new Integer(file_version)));
        } else {
            a.setReferenceExtent(new ReferenceExtent(a.getExtents().size()));
        }
        a.setKeyedExtent(new KeyedExtent(a.getNoteExtent(), a.getSourceExtent(), a.getLinkExtent()));
        try {
            String header = in.readUTF();
            if (!header.equals(null)) {
                a.setHeader(header);
            }
        } catch (Exception ex) {
        }
        if (file_version >= 10) {
            a.setSampleExtent(new SampleExtent(in, new Integer(file_version)));
        }
        if (file_version >= 12) {
            a.setPublishExtent(new PublishExtent(in, new Integer(file_version)));
        }
        return a;
    }

    protected void saveAttributesBinary(DataOutputStream out, Attributes a) throws IOException {
        out.writeUTF(a.getClass().getName());
        out.writeInt(version);
        out.writeInt(a.getTimeKeyIndex());
        out.writeUTF(a.getName());
        if (this.version >= 11) {
            String temp_id = a.getID();
            if (temp_id == null) temp_id = "";
            out.writeUTF(temp_id);
            temp_id = a.getThemeID();
            if (temp_id == null) temp_id = "";
            out.writeUTF(temp_id);
        }
        a.getExtents().save(out);
        a.getMatrix().save(out);
        a.getPrecisionExtent().save(out);
        a.getUnitExtent().save(out);
        a.getDataLegendExtent().save(out);
        a.getGraphExtent().save(out);
        a.getSourceExtent().save(out);
        a.getNoteExtent().save(out);
        a.getLinkExtent().save(out);
        a.getReferenceExtent().save(out);
        try {
            out.writeUTF(a.getHeader());
        } catch (Exception ex) {
        }
        if (this.version >= 10) {
            a.getSampleExtent().save(out);
        }
        if (this.version >= 12) {
            a.getPublishExtent().save(out);
        }
    }

    protected String getBinaryFilename() {
        String filename = fileresource.getAbsoluteFilename();
        String extension = fileresource.getUpperCaseExtension();
        if ("XML".equals(extension)) {
        }
        return filename;
    }

    /** setXMLExtents((ExtentManager)a.getExtents());

     //        a.setMatrix(new MatrixDOM(loadXMLDocument("matrix.xml")));

     a.setPrecisionExtent( new PrecisionExtent((Element)XPathAPI.selectSingleNode(root_element,"PrecisionExtent")));
     a.setUnitExtent( new UnitExtent((Element)XPathAPI.selectSingleNode(root_element,"UnitExtent")));
     a.setDataLegendExtent( new DataLegendExtent((Element)XPathAPI.selectSingleNode(root_element,"DataLegendExtent")));
     a.setGraphExtent( new GraphExtent((Element)XPathAPI.selectSingleNode(root_element,"GraphExtent")));
     a.setSourceExtent( new SourceExtent((Element)XPathAPI.selectSingleNode(root_element,"SourceExtent")));
     a.setNoteExtent( new NoteExtent((Element)XPathAPI.selectSingleNode(root_element,"NoteExtent")));
     a.setLinkExtent( new LinkExtent((Element)XPathAPI.selectSingleNode(root_element,"LinkExtent")));
     //        a.setKeyedExtent( new KeyedExtentDOM(doc,file) );
     return a;
     }
     **/
    public void saveAttributeXML(DataInputStream in, int file_version) throws IOException, ParserConfigurationException, Exception {
        Runtime r = Runtime.getRuntime();
        Document doc = new DocumentImpl();
        Element root_element = doc.createElement("ROOT");
        in.readUTF();
        root_element.setAttribute("file_class_name", getClass().getName());
        root_element.setAttribute("file_version", Integer.toString(file_version));
        if (file_version > version) {
            throw new IOException(getClass().getName() + version + " can't load " + getClass().getName() + file_version);
        }
        if (file_version > 2) {
            root_element.setAttribute("time_key_index", Integer.toString(in.readInt()));
        }
        in.readInt();
        root_element.setAttribute("name", in.readUTF());
        doc.appendChild(root_element);
        saveExtentManagerXML(in);
        r.gc();
        saveMatrixXML(in);
        r.gc();
        new PrecisionExtent(in, doc);
        new UnitExtent(in, doc);
        new DataLegendExtent(in, doc);
        new GraphExtent(in, doc);
        SourceExtent source_extent = new SourceExtent(in, doc);
        NoteExtent note_extent = new NoteExtent(in, doc);
        LinkExtent link_extent = new LinkExtent(in);
        new ReferenceExtent(in, doc);
        new KeyedExtent(note_extent, source_extent, link_extent);
        r.gc();
        saveXMLDocument("attribute.xml", doc);
        r.gc();
    }

    public void saveExtentManagerXML(DataInputStream in) throws IOException, ParserConfigurationException {
        Document doc = new DocumentImpl();
        ExtentManager extents = new ExtentManager(in, doc);
        saveXMLDocument("extents.xml", doc);
    }

    public void saveMatrixXML(DataInputStream in) throws IOException, ParserConfigurationException, Exception {
        Document doc = new DocumentImpl();
        Matrix matrix = new Matrix(in, doc, getXMLPath());
        saveXMLDocument("matrix.xml", doc);
    }

    /**
     * alisaf: load the indicators object from the XML file
     */
    protected IndicatorTreeDOM loadIndicatorsXML(ExtentInterface extents) throws IOException {
        IndicatorTreeDOM indicators = null;
        try {
            indicators = new IndicatorTreeDOM(loadXMLDocument("indicator.xml"), extents);
        } catch (Exception e) {
        }
        return indicators;
    }

    protected void saveIndicatorsXML(IndicatorTreeInterface indicators) throws IOException {
        try {
            saveXMLDocument("indicator.xml", indicators.getDocument());
        } catch (Exception e) {
        }
    }

    protected boolean saveXML = true;

    protected void setSaveXML(boolean saveXML) {
        this.saveXML = saveXML;
    }

    protected String getXMLPath() {
        String path = path = fileresource.getFileDirectory();
        String extension = fileresource.getUpperCaseExtension();
        if (!"XML".equals(extension)) {
            path = fileresource.getFileDirectory() + File.separatorChar + fileresource.getFileName();
        }
        return path;
    }

    protected Document loadXMLDocument(String filename) throws IOException, ParserConfigurationException, SAXException {
        File file = new File(getXMLPath() + File.separatorChar + filename);
        if (!file.exists()) {
            return null;
        }
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docBuilder.parse(file);
        return doc;
    }

    protected void saveXMLDocument(String filename, Document doc) throws IOException {
        return;
    }

    public int getVersion() {
        return version;
    }

    public void changeLanguageForExtents(Attributes a, String language) {
        Document doc = null;
        try {
            File file = new File(fileresource.getAbsoluteFilepath() + File.separatorChar + language + File.separatorChar + "extent.xml");
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = docBuilder.parse(file);
            a.getExtents().loadLanguage(doc);
        } catch (Exception e) {
            System.err.println("EXCEPTION:\n" + Debug.getCallingMethod() + "\n" + e + "\n");
        }
    }

    public AttributesExternalizer() {
    }

    /**
     * This method attaches the extent tree document to the matching Extent object.
     *
     * @author     macdc
     * @date       09052003
     *
     */
    private void setXMLExtents(ExtentManager extents) {
        int size = extents.size();
        for (int i = 0; i < extents.size(); i++) {
            Extent extent = extents.at(i);
            String extent_tree_path = fileresource.getFileDirectory() + File.separatorChar + fileresource.getFileName() + File.separatorChar + "extent_tree_" + i + ".xml";
            try {
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = docBuilder.parse(new File(extent_tree_path));
                extent.setExtentXML(document);
            } catch (IOException e) {
            } catch (ParserConfigurationException e) {
                System.out.println("PARSER CONFIG EXCEPTION: " + e.getMessage());
            } catch (SAXException e) {
            }
        }
    }

    public void updateObject(Object o, String country) throws ExternalizerException {
    }

    Configuration configuration = null;

    public void setConfiguration(Configuration config) {
        configuration = config;
    }

    String global_home = "";

    public void setGlobalHome(String str) {
        global_home = str;
    }

    public String getGlobalHome() {
        return global_home;
    }
}
