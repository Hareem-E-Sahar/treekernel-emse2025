package lrf.html;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lrf.Utils;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class HtmlOptimizer {

    class pair {

        String name;

        Object obj;

        pair(String s, ByteArrayOutputStream bos) {
            name = s;
            obj = bos;
        }

        pair(String s, InputStream is) {
            name = s;
            obj = is;
        }
    }

    Hashtable<String, String> anchorDest = new Hashtable<String, String>();

    ByteArrayOutputStream bos;

    String css = null;

    File dirOut;

    Hashtable<String, String> estilos;

    Hashtable<String, Integer> statStyleData;

    File fileHtml;

    Node head = null;

    Node html = null;

    HtmlDoc htmlDoc = null;

    int pageNumber;

    int paginateKB = 0;

    Vector<pair> pairs = new Vector<pair>();

    String pname = null;

    boolean procStyles;

    PrintWriter pw;

    int styleNumber;

    ZipOutputStream zosOut;

    public HtmlOptimizer(File html, File dirOut) {
        fileHtml = html;
        zosOut = null;
        htmlDoc = null;
        this.dirOut = dirOut;
    }

    public HtmlOptimizer(HtmlDoc htmlFile, File dirOut) {
        htmlDoc = htmlFile;
        this.dirOut = dirOut;
        zosOut = null;
    }

    public HtmlOptimizer(HtmlDoc htmlFile, ZipOutputStream zos) {
        htmlDoc = htmlFile;
        zosOut = zos;
        dirOut = null;
    }

    public String dumpNode(Node n) {
        String ret = openingTag(n);
        Node c = n.getFirstChild();
        while (c != null) {
            ret += dumpNode(c);
            c = c.getNextSibling();
        }
        ret += closingTag(n);
        return ret;
    }

    private void flushPairs() throws IOException {
        for (int i = 0; i < pairs.size(); i++) {
            pair p = pairs.elementAt(i);
            String s = p.name;
            if (p.obj instanceof InputStream) {
                InputStream is = (InputStream) p.obj;
                if (zosOut != null) {
                    ZipEntry z = new ZipEntry(s);
                    zosOut.putNextEntry(z);
                    Utils.writeTo(is, zosOut);
                } else if (dirOut != null) {
                    File f = new File(dirOut, s);
                    FileOutputStream fos = new FileOutputStream(f);
                    Utils.writeTo(is, fos);
                    fos.close();
                }
            } else if (p.obj instanceof ByteArrayOutputStream) {
                ByteArrayOutputStream bos = (ByteArrayOutputStream) p.obj;
                String cnt = new String(bos.toByteArray());
                for (Enumeration<String> ks = anchorDest.keys(); ks.hasMoreElements(); ) {
                    String k = ks.nextElement();
                    String v = anchorDest.get(k);
                    cnt = cnt.replace("href=\"#" + k, "href=\"" + v);
                }
                if (zosOut != null) {
                    ZipEntry z = new ZipEntry(s);
                    zosOut.putNextEntry(z);
                    zosOut.write(cnt.getBytes());
                } else if (dirOut != null) {
                    File f = new File(dirOut, s);
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(cnt.getBytes());
                    fos.close();
                }
            }
        }
    }

    public String getRealDest(String k) {
        return anchorDest.get(k);
    }

    public Hashtable<String, String> getStyles() {
        return estilos;
    }

    public void next(String s, ByteArrayOutputStream bos) throws Exception {
        pairs.add(new pair(s, bos));
    }

    public void next(String s, InputStream is) throws Exception {
        pairs.add(new pair(s, is));
    }

    public int optimize(boolean processStyles) throws Exception {
        if (htmlDoc != null) {
            fileHtml = htmlDoc.getHTMLFile();
        }
        procStyles = processStyles;
        DOMParser p = new DOMParser();
        String uriOfFile = "file:///" + fileHtml.getAbsolutePath();
        p.parse(uriOfFile);
        Document doc = p.getDocument();
        bos = new ByteArrayOutputStream();
        pw = new PrintWriter(bos);
        estilos = new Hashtable<String, String>();
        statStyleData = new Hashtable<String, Integer>();
        head = null;
        pageNumber = 1;
        styleNumber = 0;
        pname = fileHtml.getName();
        if (pname.lastIndexOf('.') > 0) {
            pname = pname.substring(0, pname.lastIndexOf('.'));
        }
        NodeList list = doc.getChildNodes();
        int numberChilds = list.getLength();
        for (int i = 0; i < numberChilds; i++) {
            Node nodo = list.item(i);
            recurse(nodo);
        }
        pw.print("</body></html>");
        pw.flush();
        next(pname + "-" + (pageNumber) + ".html", bos);
        flushPairs();
        return pageNumber;
    }

    void printClosingTag(Node tn) {
        pw.print(closingTag(tn));
    }

    String closingTag(Node n) {
        if (n.getNodeType() == Node.TEXT_NODE) return "";
        return "</" + n.getNodeName() + ">";
    }

    void printOpeningTag(Node tn) {
        pw.print(openingTag(tn));
    }

    String openingTag(Node n) {
        if (n.getNodeType() == Node.TEXT_NODE) return n.getTextContent();
        String ret = "<" + n.getNodeName();
        NamedNodeMap nnm = n.getAttributes();
        if (nnm != null) {
            for (int i = 0; i < nnm.getLength(); i++) {
                ret += " ";
                Node atr = nnm.item(i);
                if (atr.getUserData("class") != null && atr.getNodeName().equals("style")) {
                    ret += "class=\"" + atr.getUserData("class") + "\"";
                } else {
                    if (atr.getNodeName() != null) ret += atr.getNodeName();
                    String val = atr.getNodeValue();
                    if (val != null && val.trim().length() > 0) {
                        ret += "=";
                        if (!val.startsWith("\"")) val = "\"" + val;
                        if (!val.endsWith("\"")) val = val + "\"";
                        ret += val;
                    }
                }
            }
        }
        ret += ">";
        return ret;
    }

    private void procStyleAttr(Node tn) {
        NamedNodeMap nnm = tn.getAttributes();
        Node style = nnm.getNamedItem("style");
        String styleVal = style.getNodeValue();
        String cname = estilos.get(styleVal);
        if (cname == null) {
            cname = "st" + padZeros(++styleNumber);
            estilos.put(styleVal, cname);
        }
        if (statStyleData.get(cname) == null) statStyleData.put(cname, 1); else statStyleData.put(cname, statStyleData.get(cname) + 1);
        style.setUserData("class", cname, null);
    }

    private String padZeros(int s) {
        String ret = "" + s;
        while (ret.length() < 3) ret = "0" + ret;
        return ret;
    }

    private void recurse(Node nodo) throws Exception {
        boolean printTag = true;
        NamedNodeMap attr = nodo.getAttributes();
        if (nodo.getNodeType() == Node.TEXT_NODE) {
            String tc = nodo.getTextContent();
            tc = Utils.toXMLText(tc);
            pw.print(tc);
            return;
        }
        if (procStyles && attr != null && attr.getNamedItem("style") != null) {
            procStyleAttr(nodo);
        }
        String n = nodo.getNodeName();
        if (n.equals("HEAD")) {
            head = nodo;
        } else if (n.equals("IMG")) {
            try {
                String href = attr.getNamedItem("src").getNodeValue();
                if (href.toLowerCase().startsWith("file:")) href = href.substring("file:".length());
                File fimg = new File(htmlDoc.getHTMLFile().getParentFile(), href);
                FileInputStream fis = new FileInputStream(fimg);
                next(fimg.getPath().substring(1 + htmlDoc.getHTMLFile().getParentFile().getAbsolutePath().length()), fis);
            } catch (Exception e) {
            }
        } else if (n.equals("HTML")) {
            html = nodo;
        } else if (n.equals("A")) {
            Node nodName = attr.getNamedItem("name");
            if (nodName != null) {
                String name = nodName.getNodeValue();
                if (name != null) {
                    String newDest = pname + "-" + pageNumber + ".xhtml#" + name;
                    anchorDest.put(name, newDest);
                }
            }
        }
        if (printTag) printOpeningTag(nodo);
        NodeList list = nodo.getChildNodes();
        if (list != null) {
            for (int i = 0; i < list.getLength(); i++) {
                recurse(list.item(i));
            }
        }
        if (printTag) {
            printClosingTag(nodo);
            pw.flush();
            if (n.equals("DIV") || n.equals("P")) {
                if (paginateKB >= 50 && bos.size() > paginateKB * 1024) {
                    pw.println("</body></html>");
                    next(pname + "-" + (pageNumber) + ".html", bos);
                    pageNumber++;
                    bos = new ByteArrayOutputStream();
                    pw = new PrintWriter(bos);
                    printOpeningTag(html);
                    recurse(head);
                    pw.println("<body>");
                }
            }
        }
    }

    public void setCSSName(String cssName) {
        css = cssName;
    }

    public void setPaginateKB(int in) {
        paginateKB = in;
    }

    public void ratStyles(boolean removeLH) {
        int max = -1;
        String cn = "";
        Enumeration<String> style = estilos.keys();
        Hashtable<String, String> rev = new Hashtable<String, String>();
        for (int i = 0; i < statStyleData.size(); i++) {
            String ek = style.nextElement();
            String ev = estilos.get(ek);
            rev.put(ev, ek);
            if (ek.contains("font-size") && max < statStyleData.get(ev)) {
                max = statStyleData.get(ev);
                cn = ev;
            }
        }
        HtmlStyle mostUsed = new HtmlStyle(rev.get(cn));
        StyleItem fs = mostUsed.getStyle("font-size");
        float dividefactor = 1.0F;
        if (fs != null && (fs.unit == null || fs.unit.length() == 0)) {
            dividefactor = fs.number / 0.8f;
        }
        for (Enumeration<String> e = rev.keys(); e.hasMoreElements(); ) {
            String cs = e.nextElement();
            HtmlStyle hs = new HtmlStyle(rev.get(cs));
            if (removeLH) hs.removeStyle("line-height");
            StyleItem si = hs.getStyle("font-size");
            if (si != null) {
                si.number /= dividefactor;
                si.unit = "em";
                rev.put(cs, hs.getStyleContent(StyleItem.st_all));
            }
        }
        estilos = new Hashtable<String, String>();
        for (Enumeration<String> n = rev.keys(); n.hasMoreElements(); ) {
            String styleClassName = n.nextElement();
            String styleDef = rev.get(styleClassName);
            while (estilos.get(styleDef) != null) styleDef += " ";
            estilos.put(styleDef, styleClassName);
        }
    }
}
