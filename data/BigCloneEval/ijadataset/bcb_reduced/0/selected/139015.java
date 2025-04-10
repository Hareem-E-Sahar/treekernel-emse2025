package org.plog4u.wiki.internal;

import java.io.*;
import java.util.*;
import java.net.URL;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * A Memento is a class independent container for persistence
 * info.  It is a reflection of 3 storage requirements.
 *
 * 1)   We need the ability to persist an object and restore it.  
 * 2)   The class for an object may be absent.  If so we would 
 *      like to skip the object and keep reading. 
 * 3)   The class for an object may change.  If so the new class 
 *      should be able to read the old persistence info.
 *
 * We could ask the objects to serialize themselves into an 
 * ObjectOutputStream, DataOutputStream, or Hashtable.  However 
 * all of these approaches fail to meet the second requirement.
 *
 * Memento supports binary persistance with a version ID.
 */
public final class XMLMemento implements IMemento {

    private Document factory;

    private Element element;

    /**
	 * Answer a memento for the document and element.  For simplicity
	 * you should use createReadRoot and createWriteRoot to create the initial
	 * mementos on a document.
	 */
    public XMLMemento(Document doc, Element el) {
        factory = doc;
        element = el;
    }

    /**
	 * @see IMemento.
	 */
    public IMemento createChild(String type) {
        Element child = factory.createElement(type);
        element.appendChild(child);
        return new XMLMemento(factory, child);
    }

    /**
	 * @see IMemento.
	 */
    public IMemento createChild(String type, String id) {
        Element child = factory.createElement(type);
        child.setAttribute(TAG_ID, id);
        element.appendChild(child);
        return new XMLMemento(factory, child);
    }

    /**
	 * Create a Document from a Reader and answer a root memento for reading 
	 * a document.
	 */
    protected static XMLMemento createReadRoot(Reader reader) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            document = parser.parse(new InputSource(reader));
            Node node = document.getFirstChild();
            if (node instanceof Element) return new XMLMemento(document, (Element) node);
        } catch (ParserConfigurationException e) {
        } catch (IOException e) {
        } catch (SAXException e) {
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
	 * Answer a root memento for writing a document.
	 */
    public static XMLMemento createWriteRoot(String type) {
        Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element element = document.createElement(type);
            document.appendChild(element);
            return new XMLMemento(document, element);
        } catch (ParserConfigurationException e) {
            throw new Error(e);
        }
    }

    /**
	 * @see IMemento.
	 */
    public IMemento getChild(String type) {
        NodeList nodes = element.getChildNodes();
        int size = nodes.getLength();
        if (size == 0) return null;
        for (int nX = 0; nX < size; nX++) {
            Node node = nodes.item(nX);
            if (node instanceof Element) {
                Element element2 = (Element) node;
                if (element2.getNodeName().equals(type)) return new XMLMemento(factory, element2);
            }
        }
        return null;
    }

    /**
	 * @see IMemento.
	 */
    public IMemento[] getChildren(String type) {
        NodeList nodes = element.getChildNodes();
        int size = nodes.getLength();
        if (size == 0) return new IMemento[0];
        ArrayList list = new ArrayList(size);
        for (int nX = 0; nX < size; nX++) {
            Node node = nodes.item(nX);
            if (node instanceof Element) {
                Element element2 = (Element) node;
                if (element2.getNodeName().equals(type)) list.add(element2);
            }
        }
        size = list.size();
        IMemento[] results = new IMemento[size];
        for (int x = 0; x < size; x++) {
            results[x] = new XMLMemento(factory, (Element) list.get(x));
        }
        return results;
    }

    /**
	 * Return the contents of this memento as a byte array.
	 *
	 * @return byte[]
	 */
    public byte[] getContents() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        save(out);
        return out.toByteArray();
    }

    /**
	 * Returns an input stream for writing to the disk with a local locale.
	 *
	 * @return java.io.InputStream
	 */
    public InputStream getInputStream() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        save(out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
	 * @see IMemento.
	 */
    public Float getFloat(String key) {
        Attr attr = element.getAttributeNode(key);
        if (attr == null) return null;
        String strValue = attr.getValue();
        try {
            return new Float(strValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
	 * @see IMemento.
	 */
    public String getId() {
        return element.getAttribute(TAG_ID);
    }

    /**
	 * @see IMemento.
	 */
    public String getName() {
        return element.getNodeName();
    }

    /**
	 * @see IMemento.
	 */
    public Integer getInteger(String key) {
        Attr attr = element.getAttributeNode(key);
        if (attr == null) return null;
        String strValue = attr.getValue();
        try {
            return new Integer(strValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
	 * @see IMemento.
	 */
    public String getString(String key) {
        Attr attr = element.getAttributeNode(key);
        if (attr == null) return null;
        return attr.getValue();
    }

    public List getNames() {
        NamedNodeMap map = element.getAttributes();
        int size = map.getLength();
        List list = new ArrayList();
        for (int i = 0; i < size; i++) {
            Node node = map.item(i);
            String name = node.getNodeName();
            list.add(name);
        }
        return list;
    }

    /**
	 * Loads a memento from the given filename.
	 *
	 * @param in java.io.InputStream
	 * @return org.eclipse.ui.IMemento
	 * @exception java.io.IOException
	 */
    public static IMemento loadMemento(InputStream in) {
        return createReadRoot(new InputStreamReader(in));
    }

    /**
	 * Loads a memento from the given filename.
	 *
	 * @param in java.io.InputStream
	 * @return org.eclipse.ui.IMemento
	 * @exception java.io.IOException
	 */
    public static IMemento loadCorruptMemento(InputStream in) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            document = parser.parse(in);
            Node node = document.getFirstChild();
            if (node instanceof Element) return new XMLMemento(document, (Element) node);
        } catch (ParserConfigurationException e) {
        } catch (IOException e) {
        } catch (SAXException e) {
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
	 * Loads a memento from the given filename.
	 *
	 * @param filename java.lang.String
	 * @return org.eclipse.ui.IMemento
	 * @exception java.io.IOException
	 */
    public static IMemento loadMemento(String filename) throws IOException {
        return XMLMemento.createReadRoot(new FileReader(filename));
    }

    /**
	 * Loads a memento from the given filename.
	 *
	 * @param url java.net.URL
	 * @return org.eclipse.ui.IMemento
	 * @exception java.io.IOException
	 */
    public static IMemento loadMemento(URL url) throws IOException {
        return XMLMemento.createReadRoot(new InputStreamReader(url.openStream()));
    }

    /**
	 * @see IMemento.
	 */
    private void putElement(Element element2) {
        NamedNodeMap nodeMap = element2.getAttributes();
        int size = nodeMap.getLength();
        for (int i = 0; i < size; i++) {
            Attr attr = (Attr) nodeMap.item(i);
            putString(attr.getName(), attr.getValue());
        }
        NodeList nodes = element2.getChildNodes();
        size = nodes.getLength();
        for (int i = 0; i < size; i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                XMLMemento child = (XMLMemento) createChild(node.getNodeName());
                child.putElement((Element) node);
            }
        }
    }

    /**
	 * @see IMemento.
	 */
    public void putFloat(String key, float f) {
        element.setAttribute(key, String.valueOf(f));
    }

    /**
	 * @see IMemento.
	 */
    public void putInteger(String key, int n) {
        element.setAttribute(key, String.valueOf(n));
    }

    /**
	 * @see IMemento.
	 */
    public void putMemento(IMemento memento) {
        XMLMemento xmlMemento = (XMLMemento) memento;
        putElement(xmlMemento.element);
    }

    /**
	 * @see IMemento.
	 */
    public void putString(String key, String value) {
        if (value == null) return;
        element.setAttribute(key, value);
    }

    /**
	 * Save this Memento to a Writer.
	 */
    public void save(Writer writer) throws IOException {
        Result result = new StreamResult(writer);
        Source source = new DOMSource(factory);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.transform(source, result);
        } catch (Exception e) {
            throw (IOException) (new IOException().initCause(e));
        }
    }

    /**
	 * Save this Memento to a Writer.
	 */
    public void save(OutputStream os) throws IOException {
        Result result = new StreamResult(os);
        Source source = new DOMSource(factory);
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.transform(source, result);
        } catch (Exception e) {
            throw (IOException) (new IOException().initCause(e));
        }
    }

    /**
	 * Saves the memento to the given file.
	 *
	 * @param filename java.lang.String
	 * @exception java.io.IOException
	 */
    public void saveToFile(String filename) throws IOException {
        Writer w = null;
        try {
            w = new FileWriter(filename);
            save(w);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e.getLocalizedMessage());
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public String saveToString() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        save(out);
        return out.toString("UTF-8");
    }

    public Boolean getBoolean(String key) {
        Attr attr = element.getAttributeNode(key);
        if (attr == null) return null;
        String strValue = attr.getValue();
        if ("true".equalsIgnoreCase(strValue)) return new Boolean(true); else return new Boolean(false);
    }

    public void putBoolean(String key, boolean value) {
        element.setAttribute(key, value ? "true" : "false");
    }
}
