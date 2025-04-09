package org.fao.waicent.attributes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.Vector;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xpath.XPathAPI;
import org.fao.waicent.util.Debug;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExtentManager extends Vector implements ExtentInterface {

    private Document document = null;

    private boolean dirty = false;

    public ExtentManager() {
        this.document = new DocumentImpl();
        Element root_element = this.document.createElement("ROOT");
        this.document.appendChild(root_element);
    }

    public Extent at(int i) {
        return (Extent) elementAt(i);
    }

    public void set(int i, Extent entry) {
        setElementAt(entry, i);
    }

    public int add(Extent entry) {
        addElement(entry);
        return size() - 1;
    }

    public Extent getFromName(String name, boolean insert) {
        return at(getIndexFromName(name, true));
    }

    /**
     *  alisaf: get the ExtentEntry code for a given key_index and extent_index.
     */
    public String getCodeFromIndex(int key_index, int extent_id) {
        return at(key_index).at(extent_id).getCode();
    }

    public String getNameFromIndex(int key_index, int extent_id) {
        return at(key_index).at(extent_id).getName();
    }

    public int getIndexFromName(String name, boolean insert) {
        int index = getIndexFromName(name);
        if (index == -1 && insert == true) {
            index = add(new Extent(name));
        }
        return index;
    }

    public int getIndexFromName(String name) {
        int index = -1;
        for (int i = 0; i < size(); i++) {
            if (name.toUpperCase().equals(at(i).getName().toUpperCase())) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void dump() {
        dump(null);
    }

    public void dump(PrintStream out) {
        for (int i = 0; i < size(); i++) {
            if (out == null) {
                if (at(i) == null) {
                    at(i).dump();
                }
            } else {
                out.println(at(i).getName());
                at(i).dump(out);
            }
        }
    }

    /**
     * alisaf: ctor generates an XML version and creates the objects
     * (should be used only if the XML does not already exist)
     *
     * @param  in  The datainputstream with data to build this object.
     * @param  doc  The XML document to build from the inputstream.
     */
    public ExtentManager(DataInputStream in, Document doc) throws IOException {
        int n = in.readInt();
        ensureCapacity(n);
        Element root_element = doc.createElement("ROOT");
        doc.appendChild(root_element);
        for (int i = 0; i < n; i++) {
            Extent ex = new Extent(in, doc, root_element, i + 1);
            addElement(ex);
        }
    }

    public ExtentManager(DataInputStream in, Document doc, Element root_element) throws IOException {
        int n = in.readInt();
        ensureCapacity(n);
        for (int i = 0; i < n; i++) {
            Extent ex = new Extent(in, doc, root_element, i + 1);
            addElement(ex);
        }
    }

    /**
     * alisaf: method to update the TOC with the extent years.
     *
     * @param  in  The datainputstream with data to build this object.
     * @param  doc  The XML document to build from the inputstream.
     * @param  element  The XML node to attach the results to.
     */
    public static void buildTOC(DataInputStream in, Document doc, Element element) throws IOException {
        int n = in.readInt();
        for (int i = 0; i < n - 1; i++) {
            Extent.buildTOC(in, doc, element, false);
        }
        Extent.buildTOC(in, doc, element, true);
    }

    public ExtentManager(DataInputStream in) throws IOException {
        int n = in.readInt();
        ensureCapacity(n);
        for (int i = 0; i < n; i++) {
            Extent ex = new Extent(in);
            addElement(ex);
        }
    }

    public Object clone() {
        ExtentManager x = (ExtentManager) super.clone();
        try {
            for (int i = 0; i < size(); i++) {
                Extent entry = (Extent) elementAt(i);
                if (entry != null) {
                    x.setElementAt(entry.clone(), i);
                }
            }
        } catch (Exception e) {
            System.err.println("EXCEPTION:\n" + Debug.getCallingMethod() + "\n" + e);
        }
        return x;
    }

    private String getCode(int[] path) {
        Vector vector = (Vector) this;
        for (int i = 0; i < path.length - 1; i++) {
            vector = (Vector) vector.elementAt(path[i]);
        }
        return ((ExtentEntry) vector.elementAt(path[path.length - 1])).getCode();
    }

    public String getAttribute(int[] path, String name) {
        if (name.equals("code")) {
            return this.getCode(path);
        } else if (name.equals("name")) {
            return this.getName(path);
        } else {
            return null;
        }
    }

    private String getName(int[] path) {
        Vector vector = (Vector) this;
        for (int i = 0; i < path.length - 1; i++) {
            vector = (Vector) vector.elementAt(path[i]);
        }
        if (vector.elementAt(path[path.length - 1]) instanceof Extent) {
            return ((Extent) vector.elementAt(path[path.length - 1])).getName();
        } else if (vector.elementAt(path[path.length - 1]) instanceof ExtentEntry) {
            return ((ExtentEntry) vector.elementAt(path[path.length - 1])).getName();
        } else {
            System.out.println(" Vector at " + (path.length - 1) + " is not an Extent nor ExtentEntry");
            return null;
        }
    }

    /**
     * Method to set an attribute at a particular path.
     *
     * @param path The path to the node wanted.
     * @param name The name of the attribute to set.
     * @param value The value of the attribute to set.
     * @return String The attribute we want.  Null if no attribute found.
     */
    public void setAttribute(int[] path, String name, String value) {
        if (name.equals("code")) {
            this.setCode(path, value);
        } else if (name.equals("name")) {
            this.setName(path, value);
        } else {
            System.out.println("  only attributes 'code' or 'name' can be set!!!");
        }
    }

    private void setCode(int[] path, String value) {
        Vector vector = (Vector) this;
        for (int i = 0; i < path.length - 1; i++) {
            vector = (Vector) vector.elementAt(path[i]);
        }
        ((ExtentEntry) vector.elementAt(path[path.length - 1])).setCode(value);
    }

    private void setName(int[] path, String value) {
        Vector vector = (Vector) this;
        for (int i = 0; i < path.length - 1; i++) {
            vector = (Vector) vector.elementAt(path[i]);
        }
        ((ExtentEntry) vector.elementAt(path[path.length - 1])).setName(value);
    }

    public void removeCode(int[] path, String name) {
        System.out.println("  ExtentManager.removeCode: implementation not tested yet!");
        setAttribute(path, name, null);
    }

    public void removeElement(int[] path) {
        Vector vector = (Vector) this;
        vector = (Vector) vector.elementAt(path[0]);
        vector.removeElementAt(path[1]);
    }

    public int size(int index) {
        int[] path = { index };
        return this.size(path);
    }

    public int size(int[] path) {
        Vector vector = (Vector) this;
        for (int i = 0; i < path.length; i++) {
            vector = (Vector) vector.elementAt(path[i]);
        }
        return vector.size();
    }

    public int getIndexFromCode(int path, String code) {
        return ((Extent) elementAt(path)).getIndexFromCode(code);
    }

    public int getIndexFromName(int[] path, String name) {
        System.out.println("  ExtentManager.getIndexFromName: implementation not tested yet!");
        Vector vector = (Vector) this;
        for (int i = 0; i < path.length - 1; i++) {
            vector = (Vector) vector.elementAt(path[i]);
        }
        int index = -1;
        for (int i = 0; i < vector.size(); i++) {
            if (name.toUpperCase().equals(((ExtentEntry) vector.elementAt(i)).getName().toUpperCase())) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void save(DataOutputStream out) {
        try {
            out.writeInt(size());
            for (int i = 0; i < size(); i++) {
                at(i).save(out);
            }
        } catch (Exception e) {
            System.err.println("  ExtentManager.save threw: " + e);
        }
    }

    /**
     *   Implementation of the interface method.
     */
    public Node getExtent(int index) {
        Document extent_document = new DocumentImpl();
        Element root_element = extent_document.createElement("Extent");
        for (int i = 0; i < at(index).size(); i++) {
            Element entry_element = extent_document.createElement("Entry");
            entry_element.setAttribute("name", at(index).at(i).getName());
            entry_element.setAttribute("code", at(index).at(i).getCode());
            entry_element.setAttribute("units", at(index).at(i).getUnits());
            root_element.appendChild(entry_element);
        }
        extent_document.appendChild(root_element);
        return extent_document.getDocumentElement();
    }

    /**
     *   Implementation of the interface method.
     */
    public int addExtentEntry(int axis_id, String code, String label) {
        return ((Extent) elementAt(axis_id)).add(code, label);
    }

    /**
     *  Simple method to get the list of indicators for a particular axis of
     *  the extent.  i.e. all the years for the time axis, or all the countries
     *  for the region axis.
     */
    public NodeList getListAt(int index) {
        System.out.println("\n***NOT IMPLEMENTED***\n" + Debug.getCallingMethod() + "\n");
        return null;
    }

    /**
     *  Simple method to return the axis (list of id's) of a particular extent.
     */
    public void getAxisAt(int index, Axis axis) {
        for (int i = 0; i < at(index).size(); i++) {
            axis.add(i);
        }
    }

    public void loadLanguage(Document doc) {
        for (int i = 0; i < size(); i++) {
            Node node = null;
            try {
                node = XPathAPI.selectSingleNode(doc, "/ROOT/Extent[" + (i + 1) + "]");
            } catch (Exception e) {
            }
            at(i).loadLanguage(node);
        }
    }

    public void toXML(Document doc, String lang_code) {
        Element root_element = doc.createElement("ROOT");
        doc.appendChild(root_element);
        for (int i = 0; i < this.size(); i++) {
            this.at(i).toXML(doc, root_element, lang_code);
        }
    }

    public void toXML(Document doc) {
        toXML(doc, "en");
    }

    public ExtentManager(Document doc) {
        Element root = (Element) (doc.getElementsByTagName("ROOT").item(0));
        NodeList extentsList = root.getElementsByTagName("Extent");
        for (int i = 0; i < extentsList.getLength(); i++) {
            Element ext_elem = (Element) extentsList.item(i);
            if (ext_elem.hasAttribute("class_name")) {
                try {
                    Class extent_class = Class.forName(ext_elem.getAttribute("class_name"));
                    Class param_types[] = { org.w3c.dom.Document.class, org.w3c.dom.Element.class };
                    Constructor constructor = extent_class.getConstructor(param_types);
                    Object params[] = { doc, ext_elem };
                    Extent extent = (Extent) constructor.newInstance(params);
                    this.add(extent);
                } catch (Exception cnfe) {
                    this.add(new Extent(doc, (Element) extentsList.item(i)));
                }
            } else {
                this.add(new Extent(doc, (Element) extentsList.item(i)));
            }
        }
    }

    public void changeLanguage(String language) {
        for (int i = 0; i < this.size(); i++) {
            this.at(i).changeLanguage(language);
        }
    }

    public int getExtentIndexFromName(String name) {
        int i = 0;
        for (; i < size(); i++) {
            if (((Extent) at(i)).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public void setDirty(boolean bool) {
        dirty = bool;
    }

    public boolean isDirty() {
        return dirty;
    }
}
