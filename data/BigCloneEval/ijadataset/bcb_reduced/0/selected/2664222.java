package cdox.script;

import cdox.*;
import cdox.edit.*;
import cdox.gui.*;
import cdox.gui.action.*;
import cdox.util.*;
import cdox.util.script.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

/**
 *<p>
 * This class saves a CDox file. The following options are available:
 *</p>
 *<ol>
 *<li>Whether to ask the user for a filename or not. Default is NOT to ask the user
 *(true)</li>
 *<li>This is the FileParameter that can be used to set a different save filename.</li>
 *<li>Whether to update the document filename or not. Default is yes (true).</li>
 *<li>Whether to use the standard document filename or not. Default is no (false).</li>
 *</ol>
 *<p>
 * The default behaviour is like a standard "Save as" menu entry, by setting the last
 * parameter to true, a "Save" menu entry can be emulated.
 *</p>
 * @author <a href="mailto:cdox@gmx.net">Rutger Bezema, Andreas Schmitz</a>
 * @version January 16th 2004
 */
public class SaveAction implements Scriptable, CDCoverStandards {

    private Vector options;

    private Localizer lang;

    private CDoxFrame frame;

    private String name;

    /**
     * Constructs a new save action.
     */
    public SaveAction() {
        frame = CDoxFrame.getInstance();
        lang = CDox.getLocalizer();
        options = new Vector();
        Parameter file = new FileParameter(lang.get("saselectfile"), false);
        Vector v = new Vector();
        v.add(file.getComponent());
        YesNoParameter ask = new YesNoParameter(lang.get("saaskforfile"), true, v);
        YesNoParameter setSavefile = new YesNoParameter(lang.get("sasetsavename"), true);
        YesNoParameter useStandard = new YesNoParameter(lang.get("sausesavename"), false);
        options.add(ask);
        options.add(file);
        options.add(setSavefile);
        options.add(useStandard);
    }

    /**
     * Return an option (or four).
     * @return the Vector with the parameters.
     */
    public Vector getOptions() {
        return options;
    }

    public void play() {
        FileParameter filename = (FileParameter) options.elementAt(1);
        YesNoParameter ask = (YesNoParameter) options.elementAt(0);
        YesNoParameter setSavefile = (YesNoParameter) options.elementAt(2);
        YesNoParameter useStandard = (YesNoParameter) options.elementAt(3);
        if (ask.getAnswer() && new File(filename.getSelectedFile()).equals(frame.saveFile) && (!frame.isChanged())) return;
        boolean flag = true;
        File file = null;
        if (useStandard.getAnswer()) if (frame.saveFile != null) {
            file = frame.saveFile;
            flag = false;
        }
        if ((ask.getAnswer() && filename.getSelectedFile().equals("") && flag) || (!ask.getAnswer())) {
            int[] filters = { CDX };
            file = frame.getFileDialog().showDialog(frame, lang.get("filetitlecdoxsave"), lang.get("fileapprovesavebutton"), null, filters);
        } else if (flag) file = new File(filename.getSelectedFile());
        if (file == null) return;
        filename.setSelectedFile(file.getPath());
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            org.w3c.dom.Element e = doc.createElementNS("http://cdox.sf.net/schema/fileformat", "cdox");
            e.setAttribute("xmlns", "http://cdox.sf.net/schema/fileformat");
            e.setAttribute("version", "1");
            doc.appendChild(e);
            if (frame.getData() != null) {
                org.w3c.dom.Element d = (org.w3c.dom.Element) e.appendChild(doc.createElementNS("http://cdox.sf.net/schema/fileformat", "description"));
                frame.getData().storeData(d);
            }
            Cover[] c = frame.getEditPane().getCovers();
            for (int i = 0; i < c.length; i++) c[i].saveMyself(doc, out);
            frame.getEditPane().saveMyPreviewSelf(out);
            out.putNextEntry(new ZipEntry("documents.cdx"));
            Transformer t = TransformerFactory.newInstance().newTransformer();
            DOMSource src = new DOMSource(doc);
            StreamResult res = new StreamResult(out);
            t.transform(src, res);
            out.closeEntry();
            out.finish();
            out.close();
        } catch (IOException ioe) {
            CDoxFrame.handleError(ioe, false);
        } catch (ParserConfigurationException pce) {
            CDoxFrame.handleError(pce, true);
        } catch (TransformerConfigurationException tce) {
            CDoxFrame.handleError(tce, true);
        } catch (TransformerException te) {
            CDoxFrame.handleError(te, true);
        }
        frame.setSave(false);
        if (setSavefile.getAnswer()) {
            frame.saveFile = file;
            frame.setMyTitle(file.getName());
        }
    }

    public String toString() {
        if (name != null) return name;
        FileParameter filename = (FileParameter) options.elementAt(1);
        YesNoParameter ask = (YesNoParameter) options.elementAt(0);
        YesNoParameter setSavefile = (YesNoParameter) options.elementAt(2);
        YesNoParameter useStandard = (YesNoParameter) options.elementAt(3);
        if (useStandard.getAnswer()) return lang.get("sasavesavename");
        String str = new String();
        if (ask.getAnswer()) str += lang.get("sasave", new String[] { filename.getSelectedFile() }); else str += lang.get("saasksave");
        if (setSavefile.getAnswer()) str += lang.get("sadosetsavename"); else str += ".";
        return str;
    }

    public void load(org.w3c.dom.Element node) throws IOException, ClassNotFoundException {
        name = Script.loadAllParameters(options, node);
    }

    public void save(org.w3c.dom.Element node) throws IOException {
        Script.saveAllParameters(options, node);
    }

    public void setName(String name) {
        this.name = name;
    }
}
