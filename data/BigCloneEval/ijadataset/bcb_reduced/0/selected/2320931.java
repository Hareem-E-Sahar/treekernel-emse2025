package org.fao.waicent.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xerces.dom.DocumentImpl;
import org.fao.waicent.util.FileResource;
import org.fao.waicent.util.Log;
import org.fao.waicent.util.TableReader;
import org.fao.waicent.util.XMLUtil;
import org.fao.waicent.xmap2D.FeatureLayer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class RemoteDatasetCSVExternalizer extends AttributesExternalizer {

    String _attrName = "";

    String _home = "";

    Log _log;

    Vector _layerCodes = null;

    String _header = "";

    TableReader _tableReader = null;

    boolean _append = true;

    FeatureLayer _layer = null;

    Attributes _attributes = null;

    boolean _updateDataset = false;

    boolean _importDataset = false;

    private String remoteDatasetURL;

    private Attributes previousAttributes;

    public RemoteDatasetCSVExternalizer(TableReader reader, String name, Vector layerCodesVec, FeatureLayer layer) throws IOException {
        this(reader, name, layerCodesVec);
        this._layer = layer;
        setFileResource();
    }

    public RemoteDatasetCSVExternalizer(TableReader reader, String name, Vector layerCodesVec, FeatureLayer layer, Attributes attr) throws IOException {
        this(reader, name, layerCodesVec);
        this._layer = layer;
        this._attributes = attr;
    }

    public RemoteDatasetCSVExternalizer(TableReader reader, String name, Vector layerCodesVec) throws IOException {
        super(new FileResource(name));
        setName(name);
        this._tableReader = reader;
        this._layerCodes = layerCodesVec;
    }

    public RemoteDatasetCSVExternalizer(FileResource fileresource) throws IOException {
        super(fileresource);
    }

    public RemoteDatasetCSVExternalizer(Document doc, Element ele) throws IOException {
        super(doc, ele);
    }

    public RemoteDatasetCSVExternalizer(Document doc, Element ele, String lang) throws IOException {
        super(doc, ele, lang);
    }

    public void setHome(String home) {
        _home = home;
    }

    private void setFileResource() {
        String featureLayerHome = _layer.getHome();
        _home = featureLayerHome;
        String att_name = getName();
        String resource = FileResource.constructFilenameFromName(att_name) + ".zip";
        FileResource attributes_resource = new FileResource(att_name, resource, _home);
        setFileResource(attributes_resource);
    }

    public boolean fileExists() {
        FileResource objFr = getFileResource();
        if (objFr == null) {
            return false;
        }
        return super.getFileResource().fileExists();
    }

    public String getHome() {
        return _home;
    }

    public void load() {
    }

    public void load(Document doc, Element ele, String lang) throws IOException {
        XMLUtil.checkType(doc, ele, this);
        NamedNodeMap nnm = ele.getAttributes();
        String resource = ele.getAttribute("resource");
        String name = ele.getAttribute("name");
        if (name == null) {
            name = resource;
        }
        FileResource fileresource = new FileResource(name, resource);
        setFileResource(fileresource);
    }

    public void load(Document doc, Element ele) throws IOException {
        load(doc, ele, null);
    }

    public Class getObjectClass() throws ClassNotFoundException {
        return Class.forName("org.fao.waicent.attributes.AttributesExternalizer");
    }

    public Object loadObject() throws ExternalizerException {
        Attributes a = null;
        try {
            a = loadAttributes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ExternalizerException();
        }
        return a;
    }

    public void deleteObject() throws ExternalizerException {
    }

    public Attributes loadAttributes() throws IOException {
        Attributes a = loadAttributesCSV();
        _attributes = a;
        _attributes.postConstructorCheck();
        try {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return a;
    }

    public void saveObject(Object o) throws ExternalizerException {
        Attributes a = (Attributes) o;
        try {
            saveAttributes(a);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public void saveAttributes(Attributes a) throws IOException {
        String extension = fileresource.getUpperCaseExtension();
        if ("XML".equals(extension)) {
        } else {
            File file = new File(getBinaryFilename());
            if (!file.exists()) {
                File parent = new File(file.getParent());
                if (!parent.exists()) {
                    parent.mkdir();
                }
            }
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zout = new ZipOutputStream(fos);
            zout.putNextEntry(new ZipEntry("Attributes"));
            DataOutputStream out = new DataOutputStream(zout);
            saveAttributesBinary(out, a);
            out.close();
            fos.close();
        }
    }

    private void setName(String name) {
        _attrName = name;
    }

    public String getName() {
        return _attrName;
    }

    public void setPreviousAttributes(Attributes att) {
        this.previousAttributes = att;
    }

    protected Attributes loadAttributesCSV() throws IOException {
        System.out.println("SimpleCSVExternalizer.loadAttributesCSV() START");
        Attributes attributes = getAttributes();
        if (_importDataset) {
            if (attributes != null) {
                return attributes;
            } else {
            }
        } else {
            System.out.println("RemoteDatasetCSVExternalizer.loadAttributesCSV() to process csv...");
        }
        AttributesLoader a_loader = null;
        if (attributes != null) {
            attributes.setName(getName());
            a_loader = new AttributesLoader(attributes, _tableReader, _layerCodes);
        } else {
            if (_tableReader != null) {
                a_loader = new AttributesLoader(attributes, _tableReader, _layerCodes);
            }
        }
        a_loader.setAttributesName(getName());
        a_loader.setHeader(_header);
        _log = a_loader.parse();
        attributes = a_loader.getAttributes();
        int size = attributes.getExtents().size();
        if (previousAttributes != null) {
            System.out.println("\n --IN loading previous attributes");
            attributes.setPrecisionExtent(previousAttributes.getPrecisionExtent());
            attributes.setUnitExtent(previousAttributes.getUnitExtent());
            attributes.setDataLegendExtent(previousAttributes.getDataLegendExtent());
            attributes.setGraphExtent(previousAttributes.getGraphExtent());
            attributes.setSourceExtent(previousAttributes.getSourceExtent());
            attributes.setNoteExtent(previousAttributes.getNoteExtent());
            attributes.setLinkExtent(previousAttributes.getLinkExtent());
        } else if (!getHome().equals("") && fileExists()) {
            System.out.println("\n --IN --calling --loadReplaceAttributesBinary(attributes); = " + getHome() + "; " + getFileResource().getAbsoluteFilename());
            loadReplaceAttributesBinary(attributes);
        } else {
            System.out.println("\n --IN --FIRST TIME SITTING LEGENGD.....");
            if (attributes.getPrecisionExtent().getDefinitionSize() <= 0) {
                attributes.setPrecisionExtent(new PrecisionExtent(size));
            }
            if (attributes.getUnitExtent().getDefinitionSize() <= 0) {
                attributes.setUnitExtent(new UnitExtent(size));
            }
            if (attributes.getDataLegendExtent().getDefinitionSize() <= 0) {
                if (this.global_home == null) {
                    System.out.println("RemoteDatasetCSVExternalizer.loadAttributesCSV(): global home null");
                    attributes.setDataLegendExtent(new DataLegendExtent(size));
                } else {
                    attributes.setDataLegendExtent(new DataLegendExtent(size, global_home));
                }
            }
            if (attributes.getGraphExtent().getDefinitionSize() <= 0) {
                attributes.setGraphExtent(new GraphExtent(size));
            }
            if (attributes.getSourceExtent().getDefinitionSize() <= 0) {
                attributes.setSourceExtent(new SourceExtent(size));
            }
            if (attributes.getNoteExtent().getDefinitionSize() <= 0) {
                attributes.setNoteExtent(new NoteExtent(size));
            }
            if (attributes.getLinkExtent().getDefinitionSize() <= 0) {
                attributes.setLinkExtent(new LinkExtent(size));
            }
            attributes.setKeyedExtent(new KeyedExtent(attributes.getNoteExtent(), attributes.getSourceExtent(), attributes.getLinkExtent()));
        }
        attributes.setHeader(_header);
        return attributes;
    }

    public void save(Document doc, Element ele) throws IOException {
        XMLUtil.setType(doc, ele, "org.fao.waicent.attributes.AttributesExternalizer");
        ele.setAttribute("resource", getFileResource().getResource());
        if (getFileResource().getName() != null) {
            ele.setAttribute("name", getFileResource().getName());
        }
        ele.setAttribute("URL", "http://www.gvenkat.net");
    }

    public void appendObject(Object o) throws ExternalizerException {
    }

    public Log getLog() {
        return _log;
    }

    public boolean isSuccessfulParse() {
        if (hasLogErrors()) {
            return false;
        }
        return true;
    }

    public boolean hasLogErrors() {
        if (getLog() != null && getLog().errorsOccurred()) {
            String err = "An error occured in creating the dataset.  Please return to the first step and re-import the file.";
            err += getLog().dumptoString(Log.LEVEL_ERROR);
            return true;
        }
        return false;
    }

    public void setHeader(String str) {
        _header = str;
    }

    private Document getDefaultIndicatorDoc(int size) {
        Document doc = new DocumentImpl();
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Element ele = doc.createElement("ROOT");
            Element iele = doc.createElement("INDICATOR");
            iele.setAttribute("name", "Default Indicator");
            iele.setAttribute("code", "01");
            for (int i = 0; i < size; i++) {
                Element eele = doc.createElement("Extent");
                eele.setAttribute("name", "ExtentName");
                eele.setAttribute("id", "-1");
                iele.appendChild(eele);
            }
            ele.appendChild(iele);
            doc.appendChild(ele);
        } catch (Exception ex) {
            System.out.println("Error in creating default indicator:" + ex.getMessage());
            ex.printStackTrace();
        }
        return doc;
    }

    public void setAppend(boolean bool) {
        _append = bool;
    }

    public TableReader createTableReaderFromLayer() {
        try {
            _tableReader = _layer.getTableAttributes().getTableReader();
        } catch (Exception ex) {
            System.out.println("RemoteDatasetCSVExternalizer: Error in getting table reader from " + _layer.getName() + ":" + ex.getMessage());
            ex.printStackTrace();
        }
        return _tableReader;
    }

    public Attributes getAttributes() {
        return _attributes;
    }

    public void setImportDataset(boolean bool) {
        _importDataset = bool;
    }

    /**
	 * @param remoteDatasetUrl
	 */
    public void setURL(String remoteDatasetUrl) {
        this.remoteDatasetURL = remoteDatasetUrl;
    }

    protected void loadReplaceAttributesBinary(Attributes attributes) throws IOException {
        InputStream in = null;
        try {
            in = super.getFileResource().openInputStream();
            loadReplaceAttributesBinary(in, attributes);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            throw new IOException(e.getMessage());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    protected void loadReplaceAttributesBinary(InputStream in_strm, Attributes attributes) throws IOException {
        Attributes a = attributes;
        DataInputStream in = new DataInputStream(in_strm);
        String file_class_name = in.readUTF();
        if (a.getClass().getName().compareTo(file_class_name) != 0) {
            String message = getClass().getName() + " can't load " + file_class_name;
            throw new IOException(message);
        }
        int file_version = in.readInt();
        if (file_version > version) {
            String message = getClass().getName() + " version " + version + " can't load " + file_class_name + " " + file_version;
            throw new IOException(message);
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
        try {
            new Matrix(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            ex.printStackTrace();
        }
        if (file_version >= 10) {
            a.setSampleExtent(new SampleExtent(in, new Integer(file_version)));
        }
        if (file_version >= 12) {
            a.setPublishExtent(new PublishExtent(in, new Integer(file_version)));
        }
        return;
    }

    String global_home = "";

    public void setGlobalHome(String str) {
        global_home = str;
    }

    public String getGlobalHome() {
        return global_home;
    }
}
