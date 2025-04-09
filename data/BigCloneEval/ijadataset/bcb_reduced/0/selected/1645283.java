package org.fao.waicent.attributes;

import java.io.ByteArrayOutputStream;
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
import org.apache.xalan.serialize.Serializer;
import org.apache.xalan.serialize.SerializerFactory;
import org.apache.xalan.templates.OutputProperties;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xpath.XPathAPI;
import org.fao.waicent.util.Debug;
import org.fao.waicent.util.FileResource;
import org.fao.waicent.util.Translate;
import org.fao.waicent.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class AttributesExternalizerMultiSource extends AttributesExternalizer {

    final int version = 8;

    private int UPDATED_BY = -1;

    private FileResource fileresource = new FileResource();

    private FileResource fileResourceAttributes = null;

    private FileResource fileResourceExtent = null;

    private FileResource fileResourceMatrix = null;

    public AttributesExternalizerMultiSource() {
    }

    public AttributesExternalizerMultiSource(Document doc, Element ele) throws IOException {
        load(doc, ele);
    }

    public AttributesExternalizerMultiSource(Document doc, Element ele, String lang) throws IOException {
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
        if (fileResourceExtent.getName() != null) {
            ele.setAttribute("resourceExtent", fileResourceExtent.getResource());
        }
        if (fileResourceAttributes.getName() != null) {
            ele.setAttribute("resourceAttributes", fileResourceAttributes.getResource());
        }
        if (fileResourceMatrix.getName() != null) {
            ele.setAttribute("resourceMatrix", fileResourceMatrix.getResource());
        }
        ele.setAttribute("name", fileresource.getName());
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
        String resourceExtent = ele.getAttribute("resourceExtent");
        String resourceAttributes = ele.getAttribute("resourceAttributes");
        String resourceMatrix = ele.getAttribute("resourceMatrix");
        String name = ele.getAttribute("name");
        if (name == null) {
            name = resourceAttributes;
        }
        fileResourceExtent = new FileResource(name, resourceExtent);
        fileResourceAttributes = new FileResource(name, resourceAttributes);
        fileResourceMatrix = new FileResource(name, resourceMatrix);
        try {
            Element label_element = (Element) XPathAPI.selectSingleNode(ele, "Label");
            if (label_element != null) {
                labels = new Translate(name, label_element);
            } else {
                labels = new Translate(name);
                labels.addLabel(lang, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        fileresource = new FileResource(name, resourceAttributes);
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
        fileResourceExtent.delete();
        fileResourceAttributes.delete();
        fileResourceMatrix.delete();
    }

    public void saveObject(Object o) throws ExternalizerException {
        Attributes a = (Attributes) o;
        try {
            saveAttributes(a);
        } catch (IOException e) {
            throw new ExternalizerException();
        }
    }

    protected Attributes loadAttributes() throws IOException {
        Attributes a = new Attributes();
        try {
            fileResourceExtent.setHome(fileresource.getHome());
            fileResourceMatrix.setHome(fileresource.getHome());
            fileResourceAttributes.setHome(fileresource.getHome());
            String extension = fileResourceExtent.getUpperCaseExtension();
            if ("XML".equals(extension)) {
                try {
                    a = this.loadExtentsXML(a, fileResourceExtent);
                } catch (Exception e) {
                    System.out.println("!!!!!!!! Error " + e.getClass() + "  " + e.getMessage());
                }
            } else {
                InputStream in = null;
                try {
                    in = fileResourceExtent.openInputStream();
                    a = loadExtentsBinary(a, in);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
            extension = fileResourceMatrix.getUpperCaseExtension();
            if ("XML".equals(extension)) {
                a = this.loadMatrixXML(a, fileResourceMatrix);
            } else {
                InputStream in = null;
                try {
                    in = fileResourceMatrix.openInputStream();
                    a = loadMatrixBinary(a, in);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
            extension = fileResourceAttributes.getUpperCaseExtension();
            if ("XML".equals(extension)) {
                a = this.loadAttributesXML(a, fileResourceAttributes);
            } else {
                InputStream in = null;
                try {
                    in = fileResourceAttributes.openInputStream();
                    a = loadAttributesBinary(a, in);
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
            }
            String extent_tree_filename_stem = "extent_tree_";
            for (int i = 0; i < a.getExtents().size(); i++) {
                try {
                    Document doc = loadXMLDocument(extent_tree_filename_stem + i + ".xml");
                    if (doc != null) {
                        a.getExtents().at(i).setExtentXML(doc);
                    }
                } catch (Exception e) {
                }
            }
            a.postConstructorCheck();
        } catch (ParserConfigurationException e) {
            throw new IOException("Erro :: AttributesExternalizerMultiSource :: loadAttributes :: " + e.getClass() + " - " + e.getMessage());
        } catch (SAXException e) {
            throw new IOException("Erro :: AttributesExternalizerMultiSource :: loadAttributes :: " + e.getClass() + " - " + e.getMessage());
        } catch (Exception e) {
            throw new IOException("Erro :: AttributesExternalizerMultiSource :: loadAttributes :: " + e.getClass() + " - " + e.getMessage());
        }
        return a;
    }

    public void saveAttributes(Attributes a) throws IOException {
        String extension = fileResourceExtent.getUpperCaseExtension();
        if ("XML".equals(extension)) {
            this.saveExtentsXML(a);
        } else {
            File file = new File(fileResourceExtent.getAbsoluteFilename());
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zout = new ZipOutputStream(fos);
            zout.putNextEntry(new ZipEntry("Extents"));
            DataOutputStream out = new DataOutputStream(zout);
            saveExtentsBinary(out, a);
            out.close();
            fos.close();
        }
        extension = fileResourceMatrix.getUpperCaseExtension();
        if ("XML".equals(extension)) {
        } else {
            File file = new File(fileResourceMatrix.getAbsoluteFilename());
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zout = new ZipOutputStream(fos);
            zout.putNextEntry(new ZipEntry("Matrix"));
            DataOutputStream out = new DataOutputStream(zout);
            saveMatrixBinary(out, a);
            out.close();
            fos.close();
        }
        extension = fileResourceAttributes.getUpperCaseExtension();
        if ("XML".equals(extension)) {
            this.saveAttributeXML(a);
        } else {
            File file = new File(fileResourceAttributes.getAbsoluteFilename());
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
            String message = getClass().getName() + version + " can't load " + file_class_name + file_version;
            throw new IOException(message);
        }
        if (file_version > 2) {
            a.setTimeKeyIndex(in.readInt());
        }
        a.setName(in.readUTF());
        a.setExtents(new ExtentManager(in));
        a.setMatrix(new Matrix(in));
        a.setPrecisionExtent(new PrecisionExtent(in, new Integer(file_version)));
        a.setUnitExtent(new UnitExtent(in, new Integer(file_version)));
        a.setDataLegendExtent(new DataLegendExtent(in, new Integer(file_version)));
        a.setGraphExtent(new GraphExtent(in, new Integer(file_version)));
        a.setSourceExtent(new SourceExtent(in, new Integer(file_version)));
        a.setNoteExtent(new NoteExtent(in, new Integer(file_version)));
        a.setLinkExtent(new LinkExtent(in, new Integer(file_version)));
        a.setKeyedExtent(new KeyedExtent(a.getNoteExtent(), a.getSourceExtent(), a.getLinkExtent()));
        return a;
    }

    /** protected void saveAttributesBinary(DataOutputStream out, Attributes a)     throws IOException     {     out.writeUTF(a.getClass().getName());     out.writeInt(version);     out.writeInt(a.getTimeKeyIndex());     out.writeUTF(a.getName());     a.getExtents().save(out);     a.getMatrix().save(out);     a.getPrecisionExtent().save(out);     a.getUnitExtent().save(out);     a.getDataLegendExtent().save(out);     a.getGraphExtent().save(out);     a.getSourceExtent().save(out);     a.getNoteExtent().save(out);     a.getLinkExtent().save(out);     }**/
    protected String getBinaryFilename() {
        String filename = fileresource.getAbsoluteFilename();
        String extension = fileresource.getUpperCaseExtension();
        if ("XML".equals(extension)) {
        }
        return filename;
    }

    /*** -     protected Attributes loadAttributesXML()     throws IOException, ParserConfigurationException, SAXException, Exception     {     Attributes a = new Attributes();     Document doc = loadXMLDocument("attributes.xml");     Element root_element = doc.getDocumentElement();     //Do some initialization of Attribute parameters..     String file_class_name = root_element.getAttribute("file_class_name");     if (a.getClass().getName().compareTo(file_class_name)!=0) {     String message = getClass().getName()+" can't load "+file_class_name;     throw new IOException(message);     }     int file_version = Integer.parseInt(root_element.getAttribute("file_version"));     if (file_version > version) {     String message = getClass().getName()+version+" can't load "+file_class_name+file_version;     throw new IOException(message);     }     if (file_version > 2) {     a.setTimeKeyIndex(Integer.parseInt(root_element.getAttribute("time_key_index")));     }     a.setName(root_element.getAttribute("name"));     a.setExtents(new ExtentManagerDOM(loadXMLDocument("extents.xml")));     //        a.setMatrix(new MatrixDOM(loadXMLDocument("matrix.xml")));     a.setPrecisionExtent( new PrecisionExtent((Element)XPathAPI.selectSingleNode(root_element,"PrecisionExtent")));     a.setUnitExtent( new UnitExtent((Element)XPathAPI.selectSingleNode(root_element,"UnitExtent")));     a.setDataLegendExtent( new DataLegendExtent((Element)XPathAPI.selectSingleNode(root_element,"DataLegendExtent")));     a.setGraphExtent( new GraphExtent((Element)XPathAPI.selectSingleNode(root_element,"GraphExtent")));     a.setSourceExtent( new SourceExtent((Element)XPathAPI.selectSingleNode(root_element,"SourceExtent")));     a.setNoteExtent( new NoteExtent((Element)XPathAPI.selectSingleNode(root_element,"NoteExtent")));     a.setLinkExtent( new LinkExtent((Element)XPathAPI.selectSingleNode(root_element,"LinkExtent")));     //        a.setKeyedExtent( new KeyedExtentDOM(doc,file) );     return a;     }     **/
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

    /**     * alisaf: load the indicators object from the XML file     */
    protected IndicatorTreeDOM loadIndicatorsXML(ExtentInterface extents) throws IOException {
        IndicatorTreeDOM indicators = null;
        try {
            indicators = new IndicatorTreeDOM(loadXMLDocument("indicator.xml"), extents);
        } catch (Exception e) {
            System.out.println("WARNING: " + getXMLPath() + "\\indicator.xml is missing!\n");
        }
        return indicators;
    }

    protected void saveIndicatorsXML(IndicatorTreeInterface indicators) throws IOException {
        try {
            saveXMLDocument("indicator.xml", indicators.getDocument());
        } catch (Exception e) {
            System.out.println("WARNING: " + e + getXMLPath() + "\\indicator.xml not saved!\n");
        }
    }

    protected boolean saveXML = true;

    protected void setSaveXML(boolean saveXML) {
        this.saveXML = saveXML;
    }

    protected String getXMLPath() {
        String path = fileresource.getFileDirectory();
        String extension = fileresource.getUpperCaseExtension();
        if (!"XML".equals(extension)) {
            path = fileresource.getFileDirectory() + File.separatorChar + fileresource.getFileName();
        }
        return path;
    }

    protected Document loadXMLDocument(String filename) throws IOException, ParserConfigurationException, SAXException {
        File file = new File(filename);
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = docBuilder.parse(file);
        return doc;
    }

    protected void saveXMLDocument(String filename, Document doc) throws IOException {
        File file = new File(filename);
        FileOutputStream xml_output_stream = new FileOutputStream(file);
        Serializer serializer = SerializerFactory.getSerializer(OutputProperties.getDefaultMethodProperties("xml"));
        ByteArrayOutputStream bao_stream = new ByteArrayOutputStream();
        serializer.setOutputStream(bao_stream);
        serializer.asDOMSerializer().serialize(doc.getDocumentElement());
        bao_stream.writeTo(xml_output_stream);
        bao_stream.close();
        xml_output_stream.close();
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

    public FileResource getFileResourceAttributes() {
        return fileResourceAttributes;
    }

    public void setFileResourceAttributes(FileResource value) {
        fileResourceAttributes = value;
    }

    public FileResource getFileResourceExtent() {
        return fileResourceExtent;
    }

    public void setFileResourceExtent(FileResource value) {
        fileResourceExtent = value;
    }

    public FileResource getFileResourceMatrix() {
        return fileResourceMatrix;
    }

    public void setFileResourceMatrix(FileResource value) {
        fileResourceMatrix = value;
    }

    public Attributes loadAttributesXML(Attributes a, FileResource fileResource) throws IOException, ParserConfigurationException, SAXException, Exception {
        Document doc = loadXMLDocument(fileResource.getAbsoluteFilename());
        Element root_element = doc.getDocumentElement();
        String file_class_name = root_element.getAttribute("file_class_name");
        if (a.getClass().getName().compareTo(file_class_name) != 0) {
            String message = getClass().getName() + " can't load " + file_class_name;
            throw new IOException(message);
        }
        int file_version = Integer.parseInt(root_element.getAttribute("file_version"));
        if (file_version > version) {
            String message = getClass().getName() + version + " can't load " + file_class_name + file_version;
            throw new IOException(message);
        }
        if (file_version > 2) {
            a.setTimeKeyIndex(Integer.parseInt(root_element.getAttribute("time_key_index")));
        }
        a.setName(root_element.getAttribute("name"));
        long time = System.currentTimeMillis();
        long timet = System.currentTimeMillis();
        a.setPrecisionExtent(new PrecisionExtent((Element) XPathAPI.selectSingleNode(root_element, "PrecisionExtent")));
        time = System.currentTimeMillis();
        a.setUnitExtent(new UnitExtent((Element) XPathAPI.selectSingleNode(root_element, "UnitExtent")));
        time = System.currentTimeMillis();
        a.setSourceExtent(new SourceExtent((Element) XPathAPI.selectSingleNode(root_element, "SourceExtent")));
        time = System.currentTimeMillis();
        a.setNoteExtent(new NoteExtent((Element) XPathAPI.selectSingleNode(root_element, "NoteExtent")));
        time = System.currentTimeMillis();
        Element ref = (Element) XPathAPI.selectSingleNode(root_element, "ReferenceExtent");
        if (ref != null) {
            a.setReferenceExtent(new ReferenceExtent(ref));
        }
        time = System.currentTimeMillis();
        a.setLinkExtent(new LinkExtent((Element) XPathAPI.selectSingleNode(root_element, "LinkExtent")));
        time = System.currentTimeMillis();
        a.setGraphExtent(new GraphExtent((Element) XPathAPI.selectSingleNode(root_element, "GraphExtent")));
        time = System.currentTimeMillis();
        a.setDataLegendExtent(new DataLegendExtent((Element) XPathAPI.selectSingleNode(root_element, "DataLegendExtent")));
        time = System.currentTimeMillis();
        a.setKeyedExtent(new KeyedExtent(a.getNoteExtent(), a.getSourceExtent(), a.getLinkExtent()));
        return a;
    }

    public Attributes loadExtentsXML(Attributes a, FileResource fileResource) throws IOException, ParserConfigurationException, SAXException, Exception {
        Document doc = loadXMLDocument(fileResource.getAbsoluteFilename());
        long time = System.currentTimeMillis();
        a.setExtents(new ExtentManager(doc));
        return a;
    }

    public Attributes loadMatrixXML(Attributes a, FileResource fileResource) throws IOException, ParserConfigurationException, SAXException, Exception {
        Document doc = loadXMLDocument(fileResource.getAbsoluteFilename());
        System.out.println("\n***NOT IMPLEMENTED***\n" + Debug.getCallingMethod() + "\n");
        return a;
    }

    public Attributes loadAttributesBinary(Attributes a, InputStream in_strm) throws IOException {
        DataInputStream in = new DataInputStream(in_strm);
        String file_class_name = in.readUTF();
        if (a.getClass().getName().compareTo(file_class_name) != 0) {
            String message = getClass().getName() + " can't load " + file_class_name;
            throw new IOException(message);
        }
        int file_version = in.readInt();
        if (file_version > version) {
            String message = getClass().getName() + version + " can't load " + file_class_name + file_version;
            throw new IOException(message);
        }
        if (file_version > 2) {
            a.setTimeKeyIndex(in.readInt());
        }
        a.setName(in.readUTF());
        a.setPrecisionExtent(new PrecisionExtent(in, new Integer(file_version)));
        a.setUnitExtent(new UnitExtent(in, new Integer(file_version)));
        a.setDataLegendExtent(new DataLegendExtent(in, new Integer(file_version)));
        a.setGraphExtent(new GraphExtent(in, new Integer(file_version)));
        a.setSourceExtent(new SourceExtent(in, new Integer(file_version)));
        a.setNoteExtent(new NoteExtent(in, new Integer(file_version)));
        a.setLinkExtent(new LinkExtent(in, new Integer(file_version)));
        a.setKeyedExtent(new KeyedExtent(a.getNoteExtent(), a.getSourceExtent(), a.getLinkExtent()));
        return a;
    }

    public Attributes loadExtentsBinary(Attributes a, InputStream in_strm) throws IOException {
        DataInputStream in = new DataInputStream(in_strm);
        a.setExtents(new ExtentManager(in));
        return a;
    }

    public Attributes loadMatrixBinary(Attributes a, InputStream in_strm) throws IOException {
        DataInputStream in = new DataInputStream(in_strm);
        a.setMatrix(new Matrix(in));
        return a;
    }

    protected void saveExtentsBinary(DataOutputStream out, Attributes a) throws IOException {
        a.getExtents().save(out);
    }

    protected void saveMatrixBinary(DataOutputStream out, Attributes a) throws IOException {
        a.getMatrix().save(out);
    }

    protected void saveAttributesBinary(DataOutputStream out, Attributes a) throws IOException {
        out.writeUTF(a.getClass().getName());
        out.writeInt(version);
        out.writeInt(a.getTimeKeyIndex());
        out.writeUTF(a.getName());
        a.getPrecisionExtent().save(out);
        a.getUnitExtent().save(out);
        a.getDataLegendExtent().save(out);
        a.getGraphExtent().save(out);
        a.getSourceExtent().save(out);
        a.getNoteExtent().save(out);
        a.getLinkExtent().save(out);
    }

    public void saveExtentsXML(Attributes a) throws IOException {
        Runtime r = Runtime.getRuntime();
        Document doc = new DocumentImpl();
        a.getExtents().toXML(doc);
        r.gc();
        saveXMLDocument(fileResourceExtent.getAbsoluteFilename(), doc);
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
            if (a.getReferenceExtent() != null) {
                a.getReferenceExtent().toXML(doc);
            }
            r.gc();
            saveXMLDocument(fileResourceAttributes.getAbsoluteFilename(), doc);
            r.gc();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new IOException("Error :: AttributesExternalizerMultiSource :: saveAttributeXML :: " + e.getClass() + " " + e.getMessage());
        }
    }
}
