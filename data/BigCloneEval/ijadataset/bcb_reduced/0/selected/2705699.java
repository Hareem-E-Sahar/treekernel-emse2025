package cx.fbn.nevernote.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.trolltech.qt.xml.QDomDocument;
import com.trolltech.qt.xml.QDomDocumentFragment;
import com.trolltech.qt.xml.QDomElement;
import com.trolltech.qt.xml.QDomNode;
import com.trolltech.qt.xml.QDomNodeList;
import com.trolltech.qt.xml.QDomText;

public class XMLInsertHilight {

    private final QDomDocument doc;

    List<String> terms;

    List<QDomNode> oldNodes;

    List<QDomNode> newNodes;

    public XMLInsertHilight(QDomDocument d, List<String> t) {
        doc = d;
        terms = t;
        oldNodes = new ArrayList<QDomNode>();
        newNodes = new ArrayList<QDomNode>();
        scanTags();
    }

    public QDomDocument getDoc() {
        for (int i = 0; i < oldNodes.size(); i++) {
            oldNodes.get(i).parentNode().replaceChild(newNodes.get(i), oldNodes.get(i));
        }
        return doc;
    }

    private void scanTags() {
        if (doc.hasChildNodes()) parseNodes(doc.childNodes());
        return;
    }

    private void parseNodes(QDomNodeList nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            QDomNode node = nodes.at(i);
            if (node.hasChildNodes()) {
                parseNodes(node.childNodes());
            }
            scanWords(node);
        }
    }

    public void parseChildren(QDomNode node) {
        for (; !node.isNull(); node = node.nextSibling()) {
            if (node.hasChildNodes()) {
                QDomNodeList l = node.childNodes();
                for (int i = 0; i < l.size(); i++) parseChildren(l.at(i));
            }
            if (node.isText()) {
                scanWords(node);
            }
        }
    }

    private void scanWords(QDomNode node) {
        String value = node.nodeValue();
        QDomDocumentFragment fragment = doc.createDocumentFragment();
        boolean matchFound = false;
        int previousPosition = 0;
        String valueEnd = "";
        String regex = buildRegex();
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            matchFound = true;
            String valueStart = "";
            int start = matcher.start();
            int end = matcher.end();
            if (value.substring(start).startsWith(" ")) start++;
            if (value.substring(start, end).endsWith(" ")) end--;
            if (matcher.start() > 0) {
                valueStart = value.substring(previousPosition, start);
            }
            String valueMiddle = value.substring(start, end);
            valueEnd = "";
            if (matcher.end() < value.length()) {
                valueEnd = value.substring(end);
            }
            previousPosition = end;
            if (!valueStart.equals("")) {
                QDomText startText = doc.createTextNode(valueStart);
                fragment.appendChild(startText);
            }
            QDomElement hilight = doc.createElement("en-hilight");
            hilight.appendChild(doc.createTextNode(valueMiddle));
            fragment.appendChild(hilight);
        }
        if (matchFound) {
            if (previousPosition != value.length()) {
                QDomText endText = doc.createTextNode(valueEnd);
                fragment.appendChild(endText);
            }
            newNodes.add(fragment);
            oldNodes.add(node);
        }
    }

    private String buildRegex() {
        StringBuffer regex = new StringBuffer();
        for (int j = terms.size() - 1; j >= 0; j--) {
            if (terms.get(j).trim().equals("")) terms.remove(j);
        }
        for (int i = 0; i < terms.size(); i++) {
            String term = terms.get(i);
            if (term.indexOf("*") > -1) {
                term = term.replace("*", "");
            } else {
                term = "\\b" + term + "\\b";
            }
            regex.append(term);
            if (i < terms.size() - 1) regex.append("|");
        }
        return regex.toString();
    }
}
