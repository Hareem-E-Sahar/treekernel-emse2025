package com.ebixio.virtmus;

import com.ebixio.virtmus.filefilters.SongFilter;
import com.ebixio.virtmus.imgsrc.GenericImg;
import com.ebixio.virtmus.imgsrc.ImgSrc;
import com.ebixio.virtmus.imgsrc.PdfImg;
import com.ebixio.virtmus.imgsrc.PdfRender;
import com.ebixio.virtmus.xml.MusicPageConverter;
import com.ebixio.virtmus.xml.PageOrderConverter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.io.xml.TraxSource;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.openide.ErrorManager;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author gburca
 */
@XStreamAlias("song")
public class Song implements Comparable<Song> {

    @XStreamAlias("pages")
    public final List<MusicPage> pageOrder = Collections.synchronizedList(new Vector<MusicPage>());

    public String name = null;

    public String tags = null;

    @XStreamAsAttribute
    private String version = MainApp.VERSION;

    private transient File sourceFile = null;

    private transient boolean isDirty = true;

    private transient List<PropertyChangeListener> propListeners = Collections.synchronizedList(new LinkedList<PropertyChangeListener>());

    private transient List<ChangeListener> pageListeners = Collections.synchronizedList(new LinkedList<ChangeListener>());

    private static transient HashMap<String, Song> instantiated = new HashMap<String, Song>();

    private static transient Transformer songXFormer;

    static {
        InputStream songXform = Song.class.getResourceAsStream("/com/ebixio/virtmus/xml/SongTransform.xsl");
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            songXFormer = factory.newTransformer(new StreamSource(songXform));
            songXFormer.setOutputProperty(OutputKeys.INDENT, "yes");
            songXFormer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        } catch (TransformerConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Song() {
    }

    /** Creates a new instance of Song from a file, or a directory of files
     * @param f
     */
    public Song(File f) {
        addPage(f);
    }

    /** Constructors are not called (and transients are not initialized)
     * when the object is deserialized !!! */
    private Object readResolve() {
        propListeners = Collections.synchronizedList(new LinkedList<PropertyChangeListener>());
        pageListeners = Collections.synchronizedList(new LinkedList<ChangeListener>());
        isDirty = false;
        version = MainApp.VERSION;
        return this;
    }

    public boolean isDirty() {
        synchronized (pageOrder) {
            for (MusicPage mp : pageOrder) {
                if (mp.isDirty) return true;
            }
        }
        return isDirty;
    }

    public void setDirty(boolean isDirty) {
        if (this.isDirty != isDirty) {
            this.isDirty = isDirty;
            notifyListeners();
        }
        if (MainApp.findInstance().saveAllAction != null) MainApp.findInstance().saveAllAction.updateEnable();
    }

    public boolean addPage() {
        final Frame mainWindow = WindowManager.getDefault().getMainWindow();
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(true);
        File sD = this.sourceFile.getParentFile();
        if (sD != null && sD.exists()) {
            fc.setCurrentDirectory(sD);
        } else {
            String songDir = NbPreferences.forModule(MainApp.class).get(MainApp.OptSongDir, "");
            sD = new File(songDir);
            if (sD != null && sD.exists()) fc.setCurrentDirectory(sD);
        }
        int returnVal = fc.showOpenDialog(mainWindow);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File files[] = fc.getSelectedFiles();
            for (File f : files) {
                addPage(f);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean addPage(File f) {
        if (f == null) {
            return false;
        } else if (f.isDirectory()) {
            File[] images = f.listFiles();
            for (int i = 0; i < images.length; i++) {
                if (images[i].isFile()) addPage(images[i]);
            }
        } else if (f.isFile()) {
            if (f.getName().toLowerCase().endsWith(".pdf")) {
                int pdfPages;
                org.icepdf.core.pobjects.Document doc = new org.icepdf.core.pobjects.Document();
                try {
                    doc.setFile(f.getCanonicalPath());
                    pdfPages = doc.getNumberOfPages();
                } catch (Exception e) {
                    pdfPages = 0;
                    JOptionPane.showMessageDialog(null, e.toString(), "PDF Error", JOptionPane.WARNING_MESSAGE);
                }
                doc.dispose();
                if (pdfPages > 0) {
                    String pageRange = JOptionPane.showInputDialog(f.getName() + "\nPage range?", "1-" + pdfPages);
                    String[] pages = pageRange.split("-");
                    Integer p1 = Integer.decode(pages[0]);
                    Integer p2 = Integer.decode(pages[1]);
                    for (int p = p1; p <= p2; p++) {
                        pageOrder.add(new MusicPageSVG(this, f, p - 1));
                    }
                }
            } else {
                pageOrder.add(new MusicPageSVG(this, f, null));
            }
        }
        setDirty(true);
        notifyListeners();
        return true;
    }

    public boolean addPage(MusicPage mp) {
        return addPage(mp, -1);
    }

    public boolean addPage(MusicPage mp, int index) {
        if (index < 0 || index > pageOrder.size()) index = pageOrder.size();
        pageOrder.add(index, mp);
        setDirty(true);
        notifyListeners();
        return true;
    }

    public boolean removePage(MusicPage[] mps) {
        boolean removed = false;
        for (MusicPage mp : mps) {
            if (pageOrder.remove(mp)) {
                removed = true;
                setDirty(true);
            }
        }
        notifyListeners();
        return removed;
    }

    public void reorder(int[] order) {
        MusicPage[] mp = new MusicPage[order.length];
        for (int i = 0; i < order.length; i++) {
            mp[order[i]] = pageOrder.get(i);
        }
        pageOrder.clear();
        for (MusicPage s : mp) {
            pageOrder.add(s);
        }
        setDirty(true);
        notifyListeners();
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public void setName(String name) {
        if (name == null) {
            if (this.name == null) return;
        } else if (name.equals(this.name)) {
            return;
        }
        String oldName = this.name;
        this.name = name;
        fire("nameProp", oldName, name);
        setDirty(true);
        notifyListeners();
    }

    public String getName() {
        if (name != null && name.length() > 0) {
            return name;
        } else if (this.sourceFile != null) {
            return this.sourceFile.getName().replaceFirst("\\.song\\.xml", "");
        } else {
            return "No name";
        }
    }

    public void setTags(String tags) {
        if (tags == null) {
            if (this.tags == null) return;
        } else if (tags.equals(this.tags)) {
            return;
        }
        String oldTags = this.tags;
        this.tags = tags;
        fire("tagsProp", oldTags, tags);
        setDirty(true);
        notifyListeners();
    }

    public String getTags() {
        return tags;
    }

    public boolean save() {
        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) {
            return saveAs();
        } else {
            return serialize();
        }
    }

    public boolean saveAs() {
        final Frame mainWindow = WindowManager.getDefault().getMainWindow();
        final JFileChooser fc = new JFileChooser();
        String songDir = NbPreferences.forModule(MainApp.class).get(MainApp.OptSongDir, "");
        File sD = new File(songDir);
        if (sD.exists()) {
            fc.setCurrentDirectory(sD);
        }
        fc.addChoosableFileFilter(new SongFilter());
        int returnVal = fc.showSaveDialog(mainWindow);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (!file.toString().endsWith(".song.xml")) {
                file = new File(file.toString().concat(".song.xml"));
            }
            if (file.exists()) {
                returnVal = JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Overwrite?", JOptionPane.YES_NO_OPTION);
                if (returnVal != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
            this.sourceFile = file;
            return serialize();
        } else {
            return false;
        }
    }

    public static Song open() {
        final Frame mainWindow = WindowManager.getDefault().getMainWindow();
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setMultiSelectionEnabled(false);
        String songDir = NbPreferences.forModule(MainApp.class).get(MainApp.OptSongDir, "");
        File sD = new File(songDir);
        if (sD.exists()) {
            fc.setCurrentDirectory(sD);
        }
        fc.addChoosableFileFilter(new SongFilter());
        int returnVal = fc.showOpenDialog(mainWindow);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            return deserialize(file);
        } else {
            return null;
        }
    }

    private static void configXStream(XStream xs) {
        xs.setMode(XStream.NO_REFERENCES);
        xs.processAnnotations(Song.class);
        xs.processAnnotations(MusicPageSVG.class);
        xs.processAnnotations(ImgSrc.class);
        xs.processAnnotations(PdfImg.class);
        xs.processAnnotations(PdfRender.class);
        xs.processAnnotations(GenericImg.class);
        xs.registerConverter(new MusicPageConverter(xs.getConverterLookup().lookupConverterForType(MusicPageSVG.class), xs.getReflectionProvider()));
        xs.registerLocalConverter(Song.class, "pageOrder", new PageOrderConverter(xs));
        xs.addDefaultImplementation(MusicPageSVG.class, MusicPage.class);
    }

    public boolean serialize() {
        return serialize(this.sourceFile);
    }

    public boolean serialize(File toFile) {
        XStream xstream = new XStream();
        configXStream(xstream);
        for (MusicPage mp : pageOrder) {
            mp.prepareToSave();
        }
        try {
            TraxSource traxSource = new TraxSource(this, xstream);
            OutputStreamWriter buffer = new OutputStreamWriter(new FileOutputStream(toFile), "UTF-8");
            synchronized (Song.class) {
                songXFormer.transform(traxSource, new StreamResult(buffer));
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        try {
            if (!Song.instantiated.containsKey(toFile.getCanonicalPath())) {
                Song.instantiated.put(toFile.getCanonicalPath(), this);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        setDirty(false);
        for (MusicPage mp : pageOrder) {
            mp.isDirty = false;
        }
        return true;
    }

    static Song deserialize(File f) {
        if (f == null || !f.getName().endsWith(".song.xml")) return null;
        Song s;
        String canonicalPath = "";
        try {
            canonicalPath = f.getCanonicalPath();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (Song.instantiated.containsKey(canonicalPath)) return Song.instantiated.get(canonicalPath);
        XStream xs = new XStream();
        configXStream(xs);
        FileInputStream fis = null;
        ByteArrayOutputStream xformed = null;
        try {
            fis = new FileInputStream(f);
            xformed = new ByteArrayOutputStream();
            synchronized (Song.class) {
                songXFormer.transform(new StreamSource(fis), new StreamResult(xformed));
            }
            xformed = convertReferences(new ByteArrayInputStream(xformed.toByteArray()));
            s = (Song) xs.fromXML(xformed.toString("UTF-8"));
        } catch (FileNotFoundException ex) {
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
            MainApp.log("Failed to deserialize " + canonicalPath);
            ErrorManager.getDefault().notify(ex);
            return null;
        } finally {
            if (fis != null) try {
                fis.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (xformed != null) try {
                xformed.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        s.sourceFile = new File(canonicalPath);
        synchronized (s.pageOrder) {
            for (MusicPage mp : s.pageOrder) mp.deserialize(s);
        }
        findPages(s);
        Song.instantiated.put(canonicalPath, s);
        return s;
    }

    static void convertReference(Document doc, String elem) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        NodeList nodes = doc.getElementsByTagName(elem);
        for (int i = 0; i < nodes.getLength(); i++) {
            NamedNodeMap attrs = nodes.item(i).getAttributes();
            Node ref = attrs.getNamedItem("reference");
            if (ref != null) {
                String refPtr = ref.getNodeValue();
                if (refPtr != null) {
                    try {
                        XPathExpression xExpr = xPath.compile(refPtr);
                        Node refNode = (Node) xExpr.evaluate(nodes.item(i), XPathConstants.NODE);
                        if (refNode != null) {
                            nodes.item(i).setTextContent(refNode.getTextContent());
                        }
                    } catch (XPathExpressionException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                attrs.removeNamedItem("reference");
            }
        }
    }

    static ByteArrayOutputStream convertReferences(InputStream stream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(stream);
            convertReference(doc, "rotation");
            convertReference(doc, "sourceFile");
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            StreamResult res = new StreamResult(baos);
            Source src = new DOMSource(doc);
            xformer.transform(src, res);
        } catch (TransformerException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ParserConfigurationException ex) {
            Exceptions.printStackTrace(ex);
        }
        return baos;
    }

    /** We store absolute path names in the song file. If the song has moved the paths
     * for the page files might no longer be valid. This function attempts to fix that.
     * It expects s.sourceFile to already be in canonical form.
     */
    static void findPages(Song s) {
        for (MusicPage mp : s.pageOrder) {
            File f = mp.getSourceFile();
            if (f != null && !f.exists()) {
                File newFile = Utils.findFileRelative(s.getSourceFile(), f);
                if (newFile != null) {
                    mp.setSourceFile(newFile);
                    s.isDirty = true;
                }
            }
        }
    }

    /**
     * Clears all deserialized songs so they can be re-loaded
     */
    public static void clearInstantiated() {
        instantiated.clear();
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propListeners.add(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propListeners.remove(pcl);
    }

    public void fire(String propertyName, Object old, Object nue) {
        PropertyChangeListener[] pcls = propListeners.toArray(new PropertyChangeListener[0]);
        for (int i = 0; i < pcls.length; i++) {
            pcls[i].propertyChange(new PropertyChangeEvent(this, propertyName, old, nue));
        }
    }

    public void addChangeListener(ChangeListener listener) {
        pageListeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        pageListeners.remove(listener);
    }

    public void notifyListeners() {
        ChangeListener[] cls = pageListeners.toArray(new ChangeListener[0]);
        for (int i = 0; i < cls.length; i++) cls[i].stateChanged(new ChangeEvent(this));
    }

    public int compareTo(Song other) {
        return getName().compareTo(other.getName());
    }
}
