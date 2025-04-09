package com.googlecode.jerato.library.view.xml;

import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import com.googlecode.jerato.core.StringMap;
import com.googlecode.jerato.library.StringHashMap;
import com.googlecode.jerato.library.SystemException;
import com.googlecode.jerato.library.ValueUtility;

public class XmlViewParser extends DefaultHandler {

    protected StringMap _map = new StringHashMap();

    protected XmlViewNode _rootNode = new XmlViewNode();

    protected XmlViewNode _currentNode = _rootNode;

    protected StringWriter _textBuffer;

    public XmlViewParser() {
    }

    public XmlViewNode getRootNode() {
        return _rootNode;
    }

    public StringMap getSettings() {
        return _map;
    }

    public void startDocument() throws SAXException {
    }

    protected void addTextNode(XmlViewNode target, String text) {
        Pattern pattern = Pattern.compile("\\#\\{[a-zA-Z_]+\\}", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        int beforeIndex = 0;
        while (matcher.find()) {
            int startIndex = matcher.start();
            if (beforeIndex < startIndex) {
                String subText = text.substring(beforeIndex, startIndex);
                XmlViewNode node = new XmlViewNode(subText);
                target.addNode(node);
            }
            String dollText = text.substring(startIndex, matcher.end());
            XmlViewNodeValue node = new XmlViewNodeValue();
            String valueName = dollText.substring(2, dollText.length() - 1);
            node.setValueName(valueName);
            target.addNode(node);
            beforeIndex = matcher.end();
        }
        if (beforeIndex < text.length()) {
            String lastText = text.substring(beforeIndex);
            XmlViewNode node = new XmlViewNode(lastText);
            target.addNode(node);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName == null) {
            return;
        }
        if (_textBuffer != null && _textBuffer.toString().length() > 0) {
            addTextNode(_currentNode, _textBuffer.toString());
            _textBuffer = null;
        }
        if ("jjp:html".equals(qName)) {
            return;
        } else if ("jjp:title".equals(qName)) {
            _map.setString("title", attributes.getValue("value"));
            return;
        } else if ("jjp:list".equals(qName)) {
            String listName = attributes.getValue("name");
            XmlViewNodeList node = new XmlViewNodeList();
            node.setListName(listName);
            _currentNode.addNode(node);
            _currentNode = node;
            return;
        } else if ("jjp:value".equals(qName)) {
            String valueName = attributes.getValue("name");
            XmlViewNodeValue node = new XmlViewNodeValue();
            node.setValueName(valueName);
            _currentNode.addNode(node);
            return;
        } else if (ValueUtility.equalsString("jjp:html", qName)) {
        } else if (qName.startsWith("jjp:")) {
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("<");
        buffer.append(qName);
        for (int attrIndex = 0; attrIndex < attributes.getLength(); attrIndex++) {
            String attrName = attributes.getQName(attrIndex);
            String attrValue = attributes.getValue(attrIndex);
            buffer.append(" ");
            buffer.append(attrName);
            buffer.append("=\"");
            buffer.append(attrValue);
            buffer.append("\"");
        }
        buffer.append(">");
        if (qName.equals("br")) {
            buffer = new StringBuffer("<br/>");
        }
        XmlViewNode node = new XmlViewNode(buffer.toString());
        _currentNode.addNode(node);
    }

    public void characters(char[] ch, int offset, int length) throws SAXException {
        String textData = new String(ch, offset, length);
        if (_textBuffer == null) {
            _textBuffer = new StringWriter();
        }
        _textBuffer.write(textData);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName == null) {
            return;
        }
        if (_textBuffer != null && _textBuffer.toString().length() > 0) {
            addTextNode(_currentNode, _textBuffer.toString());
            _textBuffer = null;
        }
        if ("jjp:html".equals(qName)) {
            return;
        } else if ("jjp:title".equals(qName)) {
            return;
        } else if ("jjp:list".equals(qName)) {
            if (_currentNode.parent() == null) {
                throw new SystemException("Invalid tag syntax.");
            }
            _currentNode = _currentNode.parent();
            return;
        } else if ("jjp:value".equals(qName)) {
            return;
        } else if (qName.startsWith("jjp:")) {
            return;
        }
        if (qName.equals("br")) {
            return;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("</");
        buffer.append(qName);
        buffer.append(">");
        XmlViewNode node = new XmlViewNode(buffer.toString());
        _currentNode.addNode(node);
    }
}
