import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.StringTokenizer;
import org.apache.xml.serialize.*;

public class Metadata {

    public static final String DEFAULT_LANG = "it";

    protected DefaultValuesProvider dvp;

    protected File file;

    protected String lang;

    protected Document doc;

    protected Element root;

    boolean changed;

    public Metadata(DefaultValuesProvider dvp, File file) throws Exception {
        this.dvp = dvp;
        this.file = file;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder b = dbf.newDocumentBuilder();
        if (file.exists()) {
            doc = b.parse(file);
            changed = false;
        } else {
            doc = b.newDocument();
            Element root = doc.createElement("metadata");
            doc.appendChild(root);
            changed = true;
        }
        root = doc.getDocumentElement();
        lang = DEFAULT_LANG;
    }

    public boolean renameTo(File target) {
        try {
            file.renameTo(target);
            file = new File(target.toString());
            return true;
        } catch (Exception e) {
            System.err.println("Cannot rename " + file.toString() + " to " + target.toString());
            e.printStackTrace();
            return false;
        }
    }

    public boolean getChanged() {
        return changed;
    }

    public void save() throws IOException {
        File backup = new File(file.getAbsolutePath() + "~");
        ;
        OutputFormat format = new OutputFormat(doc);
        format.setEncoding("ISO-8859-15");
        format.setLineSeparator(LineSeparator.Unix);
        format.setIndenting(true);
        format.setLineWidth(0);
        format.setPreserveSpace(false);
        XMLSerializer serializer = new XMLSerializer(new FileWriter(backup), format);
        serializer.asDOMSerializer();
        serializer.serialize(doc);
        changed = false;
        backup.renameTo(file);
    }

    public String getCurrentLang() {
        return lang;
    }

    public void setCurrentLang(String lang) {
        this.lang = lang;
    }

    public Object get(String name) {
        if (name.equals("title") || name.equals("description") || name.equals("abstract") || name.equals("notes")) return getValue(lang, name); else return getValue(name);
    }

    public void set(String name, Object value) {
        if (name.equals("title") || name.equals("description") || name.equals("abstract") || name.equals("notes")) setValue(lang, name, value); else setValue(name, value);
    }

    protected Object getValue(String name) {
        Vector res = new Vector();
        NodeList nl = root.getElementsByTagName(name);
        Element deflang_el = null;
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            res.add(getTextForElement(e));
        }
        if (res.size() == 1) return res.get(0);
        if (res.size() > 1) return res;
        Object defaultValue = dvp.getDefaultValue(name);
        if (defaultValue != null) {
            setValue(name, defaultValue);
            changed = true;
        }
        return defaultValue;
    }

    protected Object getValue(String lang, String name) {
        Vector vlang = new Vector();
        Vector vdefl = new Vector();
        NodeList nl = root.getElementsByTagName(name);
        Element deflang_el = null;
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            if (e.getAttribute("lang").equals(lang)) vlang.add(getTextForElement(e));
            if (e.getAttribute("lang").equals(DEFAULT_LANG)) vdefl.add(getTextForElement(e));
        }
        if (vlang.size() == 1) return vlang.get(0);
        if (vlang.size() > 1) return vlang;
        changed = true;
        if (vdefl.size() > 0) {
            setValue(DEFAULT_LANG, name, vdefl);
            if (vdefl.size() == 1) return vdefl.get(0);
            if (vdefl.size() > 1) return vdefl;
        }
        Object defaultValue = dvp.getDefaultValue(name);
        if (defaultValue != null) setValue(DEFAULT_LANG, name, defaultValue);
        if (!lang.equals(DEFAULT_LANG)) setValue(lang, name, defaultValue);
        return defaultValue;
    }

    protected void setValue(String name, Object value) {
        changed = true;
        NodeList nl = root.getElementsByTagName(name);
        while (nl.getLength() > 0) root.removeChild(nl.item(0));
        if (value instanceof String) {
            addNode(name, (String) value);
        } else if (value instanceof Vector) {
            for (Iterator i = ((Vector) value).iterator(); i.hasNext(); ) {
                String s = (String) i.next();
                addNode(name, s);
            }
        } else System.err.println(("Unsupported type " + value.getClass() + ": cannot (yet) use in in metadata"));
    }

    protected void setValue(String lang, String name, Object value) {
        changed = true;
        NodeList nl = root.getElementsByTagName(name);
        while (nl.getLength() > 0) root.removeChild(nl.item(0));
        if (value instanceof String) {
            addNode(lang, name, (String) value);
        } else if (value instanceof Vector) {
            for (Iterator i = ((Vector) value).iterator(); i.hasNext(); ) {
                String s = (String) i.next();
                addNode(lang, name, s);
            }
        } else System.err.println(("Unsupported type " + value.getClass() + ": cannot (yet) use in in metadata"));
    }

    protected void addNode(String name, String value) {
        Element e = doc.createElement(name);
        setTextForElement(e, value);
        root.appendChild(e);
    }

    protected void addNode(String lang, String name, String value) {
        Element e = doc.createElement(name);
        e.setAttribute("lang", lang);
        setTextForElement(e, value);
        root.appendChild(e);
    }

    protected String getTextForElement(Element e) {
        NodeList nl = e.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) if (nl.item(i).getNodeType() == Node.TEXT_NODE) return ((Text) nl.item(i)).getData();
        return null;
    }

    protected void setTextForElement(Element e, String val) {
        NodeList nl = e.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
            ((Text) nl.item(i)).setData(val);
            return;
        }
        Text t = doc.createTextNode(val);
        e.appendChild(t);
    }
}
