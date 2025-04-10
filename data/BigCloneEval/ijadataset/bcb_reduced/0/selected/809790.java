package org.apache.axis2.json;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.OMOutputFormat;
import org.codehaus.jettison.json.JSONTokener;
import org.codehaus.jettison.mapped.MappedXMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.HashMap;

/**
 * JSONDataSource keeps the JSON String inside and consumes it when needed. This is to be kept in
 * the OMSourcedElementImpl and can be used either to expand the tree or get the JSON String
 * directly without expanding. This uses the "Mapped" JSON convention.
 */
public class JSONDataSource implements OMDataSource {

    private InputStream jsonInputStream;

    private String jsonString;

    private boolean isRead = false;

    protected String localName;

    public JSONDataSource(InputStream jsonInputStream, String localName) {
        this.jsonInputStream = jsonInputStream;
        this.localName = localName;
    }

    /**
     * Writes JSON into the output stream. As this should write JSON, it directly gets the JSON
     * string and writes it without expanding the tree.
     *
     * @param outputStream   the stream to be written into
     * @param omOutputFormat format of the message, this is ignored.
     * @throws javax.xml.stream.XMLStreamException
     *          if there is an error while writing the message in to the output stream.
     */
    public void serialize(OutputStream outputStream, OMOutputFormat omOutputFormat) throws XMLStreamException {
        try {
            outputStream.write(getCompleteJOSNString().getBytes());
        } catch (IOException e) {
            throw new OMException();
        }
    }

    /**
     * Writes JSON through the writer. As this should write JSON, it directly gets the JSON string
     * and writes it without expanding the tree.
     *
     * @param writer         Writer to be written into
     * @param omOutputFormat format of the message, this is ignored.
     * @throws javax.xml.stream.XMLStreamException
     *          if there is an error while writing the message through the writer.
     */
    public void serialize(Writer writer, OMOutputFormat omOutputFormat) throws XMLStreamException {
        try {
            writer.write(getCompleteJOSNString());
        } catch (IOException e) {
            throw new OMException();
        }
    }

    /**
     * Writes XML through the XMLStreamWriter. As the input data source is JSON, this method needs
     * to get a StAX reader from that JSON String. Therefore this uses the getReader() method to get
     * the StAX reader writes the events into the XMLStreamWriter.
     *
     * @param xmlStreamWriter StAX writer to be written into
     * @throws javax.xml.stream.XMLStreamException
     *          if there is an error while writing the message through the StAX writer.
     */
    public void serialize(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        XMLStreamReader reader = getReader();
        xmlStreamWriter.writeStartDocument();
        while (reader.hasNext()) {
            int x = reader.next();
            switch(x) {
                case XMLStreamConstants.START_ELEMENT:
                    xmlStreamWriter.writeStartElement(reader.getPrefix(), reader.getLocalName(), reader.getNamespaceURI());
                    int namespaceCount = reader.getNamespaceCount();
                    for (int i = namespaceCount - 1; i >= 0; i--) {
                        xmlStreamWriter.writeNamespace(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
                    }
                    int attributeCount = reader.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        xmlStreamWriter.writeAttribute(reader.getAttributePrefix(i), reader.getAttributeNamespace(i), reader.getAttributeLocalName(i), reader.getAttributeValue(i));
                    }
                    break;
                case XMLStreamConstants.START_DOCUMENT:
                    break;
                case XMLStreamConstants.CHARACTERS:
                    xmlStreamWriter.writeCharacters(reader.getText());
                    break;
                case XMLStreamConstants.CDATA:
                    xmlStreamWriter.writeCData(reader.getText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    xmlStreamWriter.writeEndElement();
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    xmlStreamWriter.writeEndDocument();
                    break;
                case XMLStreamConstants.SPACE:
                    break;
                case XMLStreamConstants.COMMENT:
                    xmlStreamWriter.writeComment(reader.getText());
                    break;
                case XMLStreamConstants.DTD:
                    xmlStreamWriter.writeDTD(reader.getText());
                    break;
                case XMLStreamConstants.PROCESSING_INSTRUCTION:
                    xmlStreamWriter.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
                    break;
                case XMLStreamConstants.ENTITY_REFERENCE:
                    xmlStreamWriter.writeEntityRef(reader.getLocalName());
                    break;
                default:
                    throw new OMException();
            }
        }
        xmlStreamWriter.writeEndDocument();
    }

    /**
     * Gives the StAX reader using the "Mapped" formatted input JSON String.
     *
     * @return The XMLStreamReader according to the JSON String.
     * @throws javax.xml.stream.XMLStreamException
     *          if there is an error while making the StAX reader.
     */
    public XMLStreamReader getReader() throws XMLStreamException {
        HashMap XMLToJSNNamespaceMap = new HashMap();
        XMLToJSNNamespaceMap.put("", "");
        MappedXMLInputFactory inputFactory = new MappedXMLInputFactory(XMLToJSNNamespaceMap);
        String jsonString = "{" + localName + ":" + this.getJSONString();
        return inputFactory.createXMLStreamReader(new JSONTokener(jsonString));
    }

    protected String getJSONString() {
        if (isRead) {
            return jsonString;
        } else {
            try {
                char temp = (char) jsonInputStream.read();
                jsonString = "";
                while ((int) temp != 65535) {
                    jsonString += temp;
                    temp = (char) jsonInputStream.read();
                }
            } catch (IOException e) {
                throw new OMException();
            }
            isRead = true;
            return jsonString;
        }
    }

    public String getCompleteJOSNString() {
        return "{" + localName + ":" + getJSONString();
    }
}
