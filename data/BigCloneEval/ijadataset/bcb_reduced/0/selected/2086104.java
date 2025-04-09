package com.googlecode.jerato.library.view.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.googlecode.jerato.core.view.ViewParameters;
import com.googlecode.jerato.core.view.ViewTransfer;
import com.googlecode.jerato.library.SystemException;

public class XmlViewNode {

    public static final int NodeTypeHtml = 0;

    public static final int NodeTypeTag = 1;

    protected ArrayList _nodeList = new ArrayList();

    protected String _html;

    protected XmlViewNode _parent;

    public XmlViewNode() {
    }

    public XmlViewNode(String html) {
        _html = html;
    }

    public Iterator iterator() {
        return _nodeList.iterator();
    }

    public XmlViewNode parent() {
        return _parent;
    }

    public void addNode(XmlViewNode node) {
        node._parent = this;
        _nodeList.add(node);
    }

    protected String replaceValue(String text, Map map) {
        Pattern pattern = Pattern.compile("\\#\\{[a-zA-Z_]+\\}", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        int beforeIndex = 0;
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            int startIndex = matcher.start();
            if (beforeIndex < startIndex) {
                String subText = text.substring(beforeIndex, startIndex);
                buffer.append(subText);
            }
            String dollText = text.substring(startIndex, matcher.end());
            String valueName = dollText.substring(2, dollText.length() - 1);
            Object value = map.get(valueName);
            if (value == null) {
                value = "";
            }
            buffer.append(value.toString());
            beforeIndex = matcher.end();
        }
        if (beforeIndex < text.length()) {
            String lastText = text.substring(beforeIndex);
            buffer.append(lastText);
        }
        return buffer.toString();
    }

    public void render(Writer writer, ViewTransfer trans, ViewParameters input, ViewParameters output) {
        render(writer, output);
    }

    public void render(Writer writer, Map map) {
        if (_html != null) {
            try {
                writer.write(replaceValue(_html, map));
            } catch (IOException ioe) {
                throw new SystemException("XmlView stream write failed.", ioe);
            }
        }
        for (Iterator itr = iterator(); itr.hasNext(); ) {
            XmlViewNode node = (XmlViewNode) itr.next();
            node.render(writer, map);
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (_html != null) {
            buffer.append(_html);
            buffer.append("\r\n");
        }
        for (Iterator itr = iterator(); itr.hasNext(); ) {
            XmlViewNode node = (XmlViewNode) itr.next();
            buffer.append(node.toString());
        }
        return buffer.toString();
    }
}
