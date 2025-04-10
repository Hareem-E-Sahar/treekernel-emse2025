public class Test {    private void sendResponse(boolean lock) throws XMLStreamException, IOException {
        Writer x = response.getWriter();
        XMLStreamWriter writer = new VDXMLStreamWriter(x);
        writer.writeStartDocument();
        writer.writeStartElement("prop");
        writer.writeStartElement("lockdiscovery");
        writer.writeStartElement("activelock");
        writer.writeStartElement("locktype");
        writer.writeEmptyElement(m_writeLock ? "write" : "read");
        writer.writeEndElement();
        writer.writeStartElement("lockscope");
        writer.writeEmptyElement(m_exclusiveLock ? "exclusive" : "shared");
        writer.writeEndElement();
        writer.writeStartElement("depth");
        writer.writeCharacters("0");
        writer.writeEndElement();
        writer.writeStartElement("owner");
        writer.writeCData(m_owner);
        writer.writeEndElement();
        writer.writeStartElement("timeout");
        writer.writeCharacters("Second-" + Long.toString(m_timeout));
        writer.writeEndElement();
        writer.writeStartElement("locktoken");
        writer.writeStartElement("href");
        writer.writeCharacters("opaquelocktoken:" + UUID.randomUUID().toString());
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.flush();
        writer.close();
    }
}