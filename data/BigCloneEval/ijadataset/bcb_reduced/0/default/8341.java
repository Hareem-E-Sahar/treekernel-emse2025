import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
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

    public boolean getChanged() {
        return changed;
    }

    public void save() throws IOException {
        File backup = new File(file.getAbsolutePath() + "~");
        ;
        file.renameTo(backup);
        OutputFormat format = new OutputFormat(doc);
        format.setEncoding("ISO-8859-15");
        format.setLineSeparator(LineSeparator.Unix);
        format.setIndenting(true);
        format.setLineWidth(0);
        format.setPreserveSpace(false);
        XMLSerializer serializer = new XMLSerializer(new FileWriter(file), format);
        serializer.asDOMSerializer();
        serializer.serialize(doc);
        changed = false;
    }

    public String getCurrentLang() {
        return lang;
    }

    public void setCurrentLang(String lang) {
        this.lang = lang;
    }

    protected boolean hasElement(String name) {
        return root.getElementsByTagName(name).getLength() > 0;
    }

    protected Element getLangElement(String name) {
        NodeList nl = root.getElementsByTagName(name);
        Element deflang_el = null;
        for (int i = 0; i < nl.getLength(); i++) {
            Element e = (Element) nl.item(i);
            if (e.getAttribute("lang").equals(lang)) return e;
            if (e.getAttribute("lang").equals(DEFAULT_LANG)) deflang_el = e;
        }
        changed = true;
        if (deflang_el != null) {
            Element e = (Element) deflang_el.cloneNode(true);
            e.setAttribute("lang", lang);
            root.appendChild(e);
            return e;
        }
        Element e = doc.createElement(name);
        e.setAttribute("lang", DEFAULT_LANG);
        String defaultValue = dvp.getDefaultValue(name);
        if (defaultValue != null) {
            Text t = doc.createTextNode(defaultValue);
            e.appendChild(t);
        }
        root.appendChild(e);
        if (!lang.equals(DEFAULT_LANG)) {
            e = (Element) e.cloneNode(true);
            e.setAttribute("lang", lang);
            root.appendChild(e);
        }
        return e;
    }

    protected Element getNoLangElement(String name) {
        NodeList nl = root.getElementsByTagName(name);
        if (nl.getLength() > 0) return (Element) nl.item(0);
        changed = true;
        Element e = doc.createElement(name);
        String defaultValue = dvp.getDefaultValue(name);
        if (defaultValue != null) {
            Text t = doc.createTextNode(defaultValue);
            e.appendChild(t);
        }
        root.appendChild(e);
        return e;
    }

    protected Element getElement(String name) {
        if (name.equals("title") || name.equals("description") || name.equals("abstract") || name.equals("notes")) return getLangElement(name); else return getNoLangElement(name);
    }

    protected String getTextForElement(Element e) {
        NodeList nl = e.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) if (nl.item(i).getNodeType() == Node.TEXT_NODE) return ((Text) nl.item(i)).getData();
        return null;
    }

    protected String getTextForElement(String name) {
        return getTextForElement(getElement(name));
    }

    protected void setTextForElement(Element e, String val) {
        NodeList nl = e.getChildNodes();
        changed = true;
        for (int i = 0; i < nl.getLength(); i++) if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
            ((Text) nl.item(i)).setData(val);
            return;
        }
        Text t = doc.createTextNode(val);
        e.appendChild(t);
    }

    protected void setTextForElement(String name, String val) {
        setTextForElement(getElement(name), val);
    }

    protected String getTextForElements(String name) {
        String res = new String();
        NodeList nl = root.getElementsByTagName(name);
        for (int i = 0; i < nl.getLength(); i++) {
            if (i != 0) res += "\n";
            Element e = (Element) nl.item(i);
            res += getTextForElement(e);
        }
        return res;
    }

    protected void setTextForElements(String name, String val) {
        changed = true;
        NodeList nl = root.getElementsByTagName(name);
        while (nl.getLength() > 0) root.removeChild(nl.item(0));
        for (StringTokenizer st = new StringTokenizer(val, "\n"); st.hasMoreTokens(); ) {
            Element e = doc.createElement(name);
            setTextForElement(e, st.nextToken());
            root.appendChild(e);
        }
    }

    public String get(String name) {
        if (name.equals("subject")) return getTextForElements(name); else return getTextForElement(name);
    }

    public void set(String name, String val) {
        if (name.equals("subject")) setTextForElements(name, val); else setTextForElement(name, val);
    }
}
