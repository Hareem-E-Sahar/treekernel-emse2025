package org.hoydaa.codesnippet.core.filter.docfrag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * 
 * @author Utku Utkan
 */
public class HighlightFilter extends DocumentFragmentFilter {

    private String regex;

    private String tagClass;

    private String tagStyle;

    public HighlightFilter(String regex) {
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getTagClass() {
        return tagClass;
    }

    public void setTagClass(String tagClass) {
        this.tagClass = tagClass;
    }

    public String getTagStyle() {
        return tagStyle;
    }

    public void setTagStyle(String tagStyle) {
        this.tagStyle = tagStyle;
    }

    protected void run(DocumentFragment docFrag, int tokenKind, String tokenImage) {
        Document document = docFrag.getOwnerDocument();
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        NodeList nodeList = null;
        try {
            XPathExpression expr = xpath.compile("//text()");
            nodeList = (NodeList) expr.evaluate(docFrag, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Node list cannot be obtained.", e);
        }
        Pattern pattern = Pattern.compile(regex);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Text text = (Text) nodeList.item(i);
            Node parent = text.getParentNode();
            String input = text.getTextContent();
            Matcher matcher = pattern.matcher(input);
            int prevEnd = 0;
            while (matcher.find()) {
                if (prevEnd != matcher.start()) {
                    parent.insertBefore(document.createTextNode(input.substring(prevEnd, matcher.start())), text);
                }
                Element span = document.createElement("span");
                span.setAttribute("class", tagClass);
                span.setAttribute("style", tagStyle);
                span.appendChild(document.createTextNode(matcher.group()));
                parent.insertBefore(span, text);
                prevEnd = matcher.end();
            }
            if (prevEnd < input.length()) {
                parent.insertBefore(document.createTextNode(input.substring(prevEnd)), text);
            }
            text.getParentNode().removeChild(text);
        }
    }
}
