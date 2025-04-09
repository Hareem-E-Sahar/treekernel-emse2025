package org.dctemplate.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * 
 * @author jhammen
 * @author dcwatson
 * Just a note:  Due to the class variables currently used, this implementation is *not* Thread safe.
 */
public class TemplateRunner {

    private final Set<String> reservedWords = new HashSet<String>();

    private final Set<String> parameters = new HashSet<String>();

    private PrintStream out;

    private final OutputType outputType;

    private String lastVariableName = "";

    private Map<String, String> reservedAttributeMap = new HashMap<String, String>();

    private Map<String, String> reservedStyleMap = new HashMap<String, String>();

    private boolean forceOverwrite;

    public TemplateRunner() {
        outputType = OutputType.load("min");
        reservedWords.add("var");
        reservedWords.add("do");
        reservedWords.add("if");
        reservedWords.add("in");
        reservedWords.add("for");
        reservedWords.add("class");
        reservedWords.add("float");
        reservedWords.addAll(Arrays.asList(outputType.getReservedWords()));
        reservedAttributeMap.put("for", "htmlFor");
        reservedAttributeMap.put("class", "className");
        reservedAttributeMap.put("rowspan", "rowSpan");
        reservedAttributeMap.put("colspan", "colSpan");
        reservedStyleMap.put("float", "cssFloat");
    }

    public void setForceOverwrite(boolean forceOverwrite) {
        this.forceOverwrite = forceOverwrite;
    }

    public void run(String inputFolderName, String outputFolderName) {
        File inputFolder = new File(inputFolderName);
        if (!inputFolder.isDirectory()) {
            throw new DCTemplateException("Input folder is not a directory: " + inputFolderName);
        }
        File[] files = inputFolder.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].getName().endsWith(".js.xml")) {
                    run(files[i], outputFolderName);
                }
            }
        }
    }

    public void run(File templateFile, String outputFolder) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(templateFile);
            Element templateElement = doc.getDocumentElement();
            String templateName = templateElement.getAttribute("name");
            if (templateName.length() == 0) {
                throw new DCTemplateException("'template' tag requires the 'name' attribute");
            }
            String dataName = templateElement.getAttribute("data");
            String[] parameters = dataName.split(",\\s*");
            this.parameters.clear();
            for (String parameter : parameters) {
                for (String outputReserved : reservedWords) {
                    if (parameter.equals(outputReserved)) {
                        throw new DCTemplateException("'" + outputReserved + "' is a reserved variable and cannot be used in the template 'data' attribute");
                    }
                }
                this.parameters.add(parameter);
            }
            try {
                File outFile = new File(outputFolder + "/" + templateName + ".js");
                if (!forceOverwrite && outFile.exists() && outFile.lastModified() > templateFile.lastModified()) {
                    Logger.getLogger(TemplateRunner.class.getName()).log(Level.INFO, "skipping " + templateFile.getName() + " as output file is newer, forceOveride is false");
                } else {
                    out = new PrintStream(new FileOutputStream(outFile));
                    print(outputType.getHeader(templateName, dataName));
                    processNodes("_parent", templateElement.getChildNodes());
                    print(outputType.getFooter());
                }
            } catch (FileNotFoundException ex) {
                throw new DCTemplateException(ex);
            }
        } catch (Exception ex) {
            throw new DCTemplateException(ex);
        }
    }

    private void processElement(String parentName, Element element) {
        String name = element.getTagName();
        if ("script".equals(name)) {
            processScript(element);
        } else if ("if".equals(name)) {
            processIf(parentName, element);
        } else if ("else".equals(name)) {
            processElse(parentName, element);
        } else if ("for".equals(name)) {
            processFor(parentName, element);
        } else {
            processHTMLElement(parentName, element);
        }
    }

    private void processFor(String parentName, Element element) {
        String in = element.getAttribute("in");
        if (in.length() > 0) {
            String key = element.getAttribute("key");
            if (key.length() == 0) {
                key = nextVariableName();
            }
            print("for(var " + key + " in " + in + "){");
            String val = element.getAttribute("value");
            if (val.length() > 0) {
                print("var " + val + "=" + in + "[" + key + "];");
            }
        } else {
            String end = element.getAttribute("end");
            if (end.length() == 0) {
                throw new DCTemplateException("'for' tag must define either 'in' or 'end' attribute");
            }
            String begin = element.getAttribute("begin");
            String step = element.getAttribute("step");
            print("for(" + begin + ";" + end + ";");
            if (step.length() > 0) {
                print(step);
            }
            print("){");
        }
        processNodes(parentName, element.getChildNodes());
        print("}");
    }

    private void processIf(String parentName, Element element) {
        String test = element.getAttribute("test");
        if (test.length() == 0) {
            throw new DCTemplateException("'if' tag requires the 'test' attribute");
        }
        print("if(" + test + "){");
        processNodes(parentName, element.getChildNodes());
        print("}");
    }

    private void processElse(String parentName, Element element) {
        print("}else{");
        processNodes(parentName, element.getChildNodes());
    }

    private void processHTMLElement(String parentName, Element elem) {
        String name = nextVariableName();
        print(outputType.createElement(parentName, name, elem.getTagName()));
        NamedNodeMap attrMap = elem.getAttributes();
        for (int i = 0; i < attrMap.getLength(); i++) {
            Attr attr = (Attr) attrMap.item(i);
            String attrName = attr.getName();
            if (reservedAttributeMap.containsKey(attrName)) attrName = reservedAttributeMap.get(attrName);
            if (attrName.equals("style")) {
                processHTMLStyleAttribute(name, attr);
            } else {
                String value = expandText(attr.getValue());
                print(name + "." + attrName + "=" + value + ";");
            }
        }
        print(outputType.appendChild(parentName, name, elem.getTagName()));
        NodeList nodeList = elem.getChildNodes();
        processNodes(name, nodeList);
    }

    private void processHTMLStyleAttribute(String name, Attr attr) {
        for (String tok : attr.getValue().split(";")) {
            String[] styleDef = tok.split(":");
            if (styleDef.length == 2) {
                String value = expandText(styleDef[1].trim());
                String styleAttributeName = styleDef[0].trim();
                Pattern pattern = Pattern.compile("([^-]+)\\-([a-z])(.*)");
                Matcher matcher = pattern.matcher(styleAttributeName);
                if (matcher.matches()) {
                    styleAttributeName = matcher.group(1) + matcher.group(2).toUpperCase() + matcher.group(3);
                }
                if (reservedStyleMap.containsKey(styleAttributeName)) {
                    styleAttributeName = reservedStyleMap.get(styleAttributeName);
                }
                print(name + ".style." + styleAttributeName + "=" + value + ";");
            }
        }
    }

    private void processNodes(String name, NodeList nodeList) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                processElement(name, (Element) node);
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                processText(name, (Text) node);
            }
        }
    }

    private void processScript(Element scriptTag) {
        print(scriptTag.getTextContent());
    }

    private void processText(String parentName, Text node) {
        String text = node.getTextContent();
        String expandedtext = expandText(text);
        if (expandedtext.length() > 0) {
            Matcher matcher = Pattern.compile("%\\{([^\\}]+)\\}").matcher(text);
            if (matcher.find()) {
                print(outputType.innerHTML(parentName, expandedtext));
            } else {
                String name = nextVariableName();
                print(outputType.createTextNode(name, expandedtext));
                print(outputType.appendChild(parentName, name, null));
            }
        }
    }

    private String expandText(String text) {
        Pattern pattern = Pattern.compile("[\\$,%]\\{([^\\}]+)\\}");
        Matcher matcher = pattern.matcher(text);
        int matchEnd = 0;
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String subText = text.substring(matchEnd, matcher.start());
            subText = subText.replaceAll("\\s+", " ");
            if (subText.length() > 0) {
                if (result.length() > 0) {
                    result.append('+');
                }
                result.append('"');
                result.append(subText);
                result.append('"');
            }
            if (result.length() > 0) {
                result.append('+');
            }
            result.append(matcher.group(1));
            matchEnd = matcher.end();
        }
        String subText = text.substring(matchEnd);
        subText = subText.replaceAll("\\s+", " ");
        if (subText.length() > 0) {
            if (result.length() > 0) {
                result.append('+');
            }
            result.append('"');
            result.append(subText);
            result.append('"');
        }
        return result.toString();
    }

    private void print(String value) {
        out.print(value);
    }

    private String nextVariableName() {
        String next = incrementVariableName();
        while (reservedWords.contains(next) || parameters.contains(next)) {
            next = incrementVariableName();
        }
        return next;
    }

    private String incrementVariableName() {
        StringBuffer nextName = new StringBuffer(lastVariableName);
        int currentLength = lastVariableName.length();
        int index = currentLength;
        while (--index >= 0 && lastVariableName.charAt(index) == 122) {
        }
        if (index >= 0) {
            int lsd = lastVariableName.charAt(index);
            nextName.setCharAt(index, ((char) ++lsd));
            for (int i = index + 1; i < currentLength; i++) {
                nextName.setCharAt(i, 'a');
            }
        } else {
            nextName.setLength(currentLength + 1);
            for (int i = 0; i <= currentLength; i++) {
                nextName.setCharAt(i, 'a');
            }
        }
        return lastVariableName = nextName.toString();
    }

    public static void main(String[] argv) throws Exception {
        try {
            String inputFolderName = argv[0];
            String outputFolderName = argv.length > 1 ? argv[1] : inputFolderName;
            TemplateRunner tr = new TemplateRunner();
            tr.run(inputFolderName, outputFolderName);
        } catch (Exception ex) {
            Logger.getLogger(TemplateRunner.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }
}
