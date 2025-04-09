package info.repo.didl.impl.serialize;

import info.repo.didl.AttributeType;
import info.repo.didl.ComponentType;
import info.repo.didl.DIDLBaseType;
import info.repo.didl.DIDLException;
import info.repo.didl.DIDLFactoryType;
import info.repo.didl.DIDLInfoType;
import info.repo.didl.DIDLType;
import info.repo.didl.DescriptorType;
import info.repo.didl.ItemType;
import info.repo.didl.ResourceType;
import info.repo.didl.StatementType;
import info.repo.didl.impl.Asset;
import info.repo.didl.impl.AttributableBase;
import info.repo.didl.impl.DIDLBase;
import info.repo.didl.impl.DIDLFactory;
import info.repo.didl.impl.DIDLInfo;
import info.repo.didl.impl.tools.Base64;
import info.repo.didl.impl.tools.Strings;
import info.repo.didl.serialize.DIDLDeserializerType;
import info.repo.didl.serialize.DIDLRegistryType;
import info.repo.didl.serialize.DIDLSerializationException;
import info.repo.didl.serialize.DIDLStrategyType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * <code>DIDLHandler</code> is an XML deserialization implementation.
 * This class parses DIDL XML content to create a DIDLType instance.
 *
 * @author Patrick Hochstenbach <patrick.hochstenbach@ugent.be>
 */
public class DIDLHandler extends DefaultHandler2 {

    private static final String DIDL_NAMESPACE = "urn:mpeg:mpeg21:2002:02-DIDL-NS";

    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";

    private static final String DIDL_ELEMENT = "DIDL";

    private static final String DIDLINFO_ELEMENT = "DIDLInfo";

    private static final String ITEM_ELEMENT = "Item";

    private static final String COMPONENT_ELEMENT = "Component";

    private static final String DESCRIPTOR_ELEMENT = "Descriptor";

    private static final String STATEMENT_ELEMENT = "Statement";

    private static final String RESOURCE_ELEMENT = "Resource";

    private DIDLType didl;

    private Stack<DIDLBaseType> stack;

    private XMLRegistry registry;

    private XMLStrategy strategy;

    private boolean inline = false;

    private ByteArrayOutputStream inlineBuffer;

    private PrintWriter buffer;

    private DefaultHandler2 copier;

    private Class copierClass = MegginsonXMLCopier.class;

    private Fields fields;

    /**
     * <code>Fields</code> defines all Resource/Statement attributes
     *
     * @author Patrick Hochstenbach <patrick.hochstenbach@ugent.be>
     */
    class Fields {

        /** Namespace of inline data */
        public String namespace;

        /** MimeType of Resource/Statement content */
        public String mimeType;

        /** Encoding of inline data (i.e. base64) */
        public String encoding;

        /** Content Encoding of inline data (i.e. base64) */
        public String[] contentEncoding;

        public URI ref;

        public String toString() {
            return "namespace: " + namespace + " " + "mimeType: " + mimeType + " " + "encoding: " + encoding + " " + "contentEncoding: " + Strings.join(contentEncoding == null ? new String[] {} : contentEncoding, ",") + " " + "ref:" + (ref == null ? "" : ref.toString());
        }
    }

    /**
     * Creates a new DIDLHandler instance
     */
    public DIDLHandler() {
        this.strategy = new XMLStrategy();
        this.registry = new XMLRegistry();
        this.stack = new Stack<DIDLBaseType>();
    }

    /**
     * Gets the XMLRegistry instance
     * @return XMLRegistry as DIDLRegistryType
     */
    public DIDLRegistryType getRegistry() {
        return registry;
    }

    /**
     * Gets the XMLStrategy instance
     * @return XMLStrategy as DIDLStrategyType
     */
    public DIDLStrategyType getStrategy() {
        return strategy;
    }

    /**
     * set copier class
     */
    public void setCopierClass(String className) throws ClassNotFoundException {
        copierClass = Class.forName(className);
    }

    /**
     * get copier class
     *
     */
    public String getCopierClass() {
        return copierClass.getName();
    }

    /**
     * Gets the constructed DIDLType object
     */
    public DIDLType getDIDL() {
        return didl;
    }

    /**
     * Implements SAX Handler
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            if (DIDL_NAMESPACE.equals(uri) && DIDL_ELEMENT.equals(localName)) {
                DIDLFactoryType factory = new DIDLFactory();
                didl = factory.newDIDL();
                stack.push(didl);
                if (attributes.getValue("DIDLDocumentId") != null) {
                    didl.setDIDLDocumentId(new URI(attributes.getValue("DIDLDocumentId")));
                }
                processOtherAttributes(attributes);
            } else if (DIDL_NAMESPACE.equals(uri) && ITEM_ELEMENT.equals(localName)) {
                DIDLBaseType base = stack.peek();
                Method method = base.getClass().getMethod("addItem", ItemType.class);
                base = (DIDLBase) method.invoke(base, didl.newItem());
                stack.push(base);
                if (attributes.getValue("id") != null) base.setId(attributes.getValue("id"));
                processOtherAttributes(attributes);
            } else if (DIDL_NAMESPACE.equals(uri) && DIDLINFO_ELEMENT.equals(localName)) {
                DIDLBaseType base = stack.peek();
                Method method = base.getClass().getMethod("addDIDLInfo", DIDLInfoType.class);
                base = (DIDLBase) method.invoke(base, didl.newDIDLInfo());
                stack.push(base);
                if (attributes.getValue("id") != null) {
                    base.setId(attributes.getValue("id"));
                }
                startInline(attributes);
            } else if (DIDL_NAMESPACE.equals(uri) && DESCRIPTOR_ELEMENT.equals(localName)) {
                DIDLBaseType base = stack.peek();
                Method method = base.getClass().getMethod("addDescriptor", DescriptorType.class);
                base = (DIDLBase) method.invoke(base, didl.newDescriptor());
                stack.push(base);
                if (attributes.getValue("id") != null) {
                    base.setId(attributes.getValue("id"));
                }
                processOtherAttributes(attributes);
            } else if (DIDL_NAMESPACE.equals(uri) && COMPONENT_ELEMENT.equals(localName)) {
                DIDLBaseType base = stack.peek();
                Method method = base.getClass().getMethod("addComponent", ComponentType.class);
                base = (DIDLBase) method.invoke(base, didl.newComponent());
                stack.push(base);
                if (attributes.getValue("id") != null) {
                    base.setId(attributes.getValue("id"));
                }
                processOtherAttributes(attributes);
            } else if (DIDL_NAMESPACE.equals(uri) && STATEMENT_ELEMENT.equals(localName)) {
                DIDLBaseType base = stack.peek();
                Method method = base.getClass().getMethod("addStatement", StatementType.class);
                StatementType statement = (StatementType) method.invoke(base, didl.newStatement());
                stack.push(statement);
                startInline(attributes);
                processOtherAttributes(attributes);
            } else if (DIDL_NAMESPACE.equals(uri) && RESOURCE_ELEMENT.equals(localName)) {
                DIDLBaseType base = stack.peek();
                Method method = base.getClass().getMethod("addResource", ResourceType.class);
                ResourceType resource = (ResourceType) method.invoke(base, didl.newResource());
                stack.push(resource);
                startInline(attributes);
                processOtherAttributes(attributes);
            } else if (inline) {
                if (fields.namespace == null) {
                    fields.namespace = uri;
                }
                copier.startElement(uri, localName, qName, attributes);
            }
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    /**
     * Implements SAX Handler
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (DIDL_NAMESPACE.equals(uri) && (DIDL_ELEMENT.equals(localName) || ITEM_ELEMENT.equals(localName) || COMPONENT_ELEMENT.equals(localName) || DESCRIPTOR_ELEMENT.equals(localName))) {
                stack.pop();
            } else if (DIDL_NAMESPACE.equals(uri) && (DIDLINFO_ELEMENT.equals(localName) || STATEMENT_ELEMENT.equals(localName) || RESOURCE_ELEMENT.equals(localName))) {
                endInline();
                stack.pop();
            } else if (inline) {
                copier.endElement(uri, localName, qName);
            }
        } catch (Exception e) {
            throw new SAXException("SAXException in endElement", e);
        }
    }

    /**
     * Implements SAX Handler
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inline) {
            copier.characters(ch, start, length);
        }
    }

    /**
     * Implements SAX Handler
     */
    public void startCDATA() throws SAXException {
        if (inline) {
            copier.startCDATA();
        }
    }

    /**
     * Implements SAX Handler
     */
    public void endCDATA() throws SAXException {
        if (inline) {
            copier.endCDATA();
        }
    }

    /**
     * Implements SAX Handler
     */
    public void processingInstruction(String target, String data) throws SAXException {
        if (inline) {
            copier.processingInstruction(target, data);
        }
    }

    /**
     * Implements SAX Handler
     */
    public void comment(char[] ch, int start, int length) throws SAXException {
        if (inline) {
            copier.comment(ch, start, length);
        }
    }

    /**
     * Implements SAX Handler
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (inline) {
            copier.startPrefixMapping(prefix, uri);
        }
    }

    /**
     * Implements SAX Handler
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        if (inline) {
            copier.endPrefixMapping(prefix);
        }
    }

    private void processOtherAttributes(Attributes attributes) throws IOException, DIDLSerializationException {
        Map<String, Map> attMap = new HashMap<String, Map>();
        for (int i = 0; i < attributes.getLength(); i++) {
            String uri = attributes.getURI(i);
            String name = attributes.getLocalName(i);
            String value = attributes.getValue(i);
            if ("".equals(uri) || DIDL_NAMESPACE.equals(uri) || XSI_NAMESPACE.equals(uri)) {
                continue;
            }
            if (attMap.containsKey(uri)) {
                attMap.get(uri).put(name, value);
            } else {
                Map nvmap = new HashMap();
                nvmap.put(name, value);
                attMap.put(uri, nvmap);
            }
        }
        for (Iterator<Entry<String, Map>> it = attMap.entrySet().iterator(); it.hasNext(); ) {
            Entry<String, Map> e = it.next();
            String namespace = e.getKey();
            Class implClass = strategy.getAttributeImplementation(namespace);
            if (implClass == null) {
                throw new DIDLException(DIDLException.UNKNOWN_ERROR, "No matching attributeTypeClass found for " + namespace);
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bout);
            oo.writeObject(e.getValue());
            oo.close();
            bout.close();
            DIDLDeserializerType deserializer = registry.getDeserializer(implClass);
            if (deserializer == null) {
                throw new DIDLException(DIDLException.UNKNOWN_ERROR, "No matching deserializer found for " + implClass);
            }
            Object at = deserializer.read(new ByteArrayInputStream(bout.toByteArray()));
            DIDLBaseType base = stack.peek();
            if (base instanceof AttributableBase) {
                ((AttributableBase) base).getAttributes().add((AttributeType) at);
            }
        }
    }

    private void startInline(Attributes attributes) throws URISyntaxException, DIDLSerializationException {
        String mimeType = attributes.getValue("mimeType");
        String encoding = attributes.getValue("encoding");
        String[] contentEncoding = attributes.getValue("contentEncoding") == null ? null : attributes.getValue("contentEncoding").split("\\s+");
        URI ref = attributes.getValue("ref") == null ? null : new URI(attributes.getValue("ref"));
        fields = new Fields();
        fields.mimeType = mimeType;
        fields.encoding = encoding;
        fields.contentEncoding = contentEncoding;
        fields.ref = ref;
        inline = true;
        inlineBuffer = new ByteArrayOutputStream();
        buffer = new PrintWriter(inlineBuffer);
        try {
            Constructor c = copierClass.getConstructor(new Class[] { Writer.class });
            copier = (DefaultHandler2) (c.newInstance(buffer));
        } catch (Exception ex) {
            throw new DIDLSerializationException(ex);
        }
    }

    private void endInline() throws IOException, DIDLSerializationException {
        inline = false;
        buffer.close();
        inlineBuffer.close();
        Class implClass = strategy.getContentImplementation(null, fields.mimeType, fields.namespace);
        if (implClass == null) {
            throw new DIDLSerializationException("No matching contentTypeClass found for " + fields);
        }
        DIDLDeserializerType deserializer = registry.getDeserializer(implClass);
        if (deserializer == null) {
            throw new DIDLSerializationException("No matching deserializer found for " + implClass);
        }
        ByteArrayInputStream bin;
        if (fields.encoding != null && fields.encoding.equals("base64")) {
            bin = new ByteArrayInputStream(Base64.decode(inlineBuffer.toByteArray(), 0, inlineBuffer.size()));
        } else {
            bin = new ByteArrayInputStream(inlineBuffer.toByteArray());
        }
        Object content = deserializer.read(bin);
        Object obj = stack.peek();
        if (obj instanceof Asset) {
            Asset base = (Asset) obj;
            base.setContent(content);
            base.setMimeType(fields.mimeType);
            base.setEncoding(fields.encoding);
            base.setContentEncoding(fields.contentEncoding);
            base.setRef(fields.ref);
        } else if (obj instanceof DIDLInfo) {
            DIDLInfo dinfo = (DIDLInfo) obj;
            dinfo.setContent(content);
        }
        inlineBuffer = null;
        fields = null;
    }
}
