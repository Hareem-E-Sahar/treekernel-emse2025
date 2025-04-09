package rizsi.rcompile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UtilProcess {

    class PkgConfig {

        String name;

        public PkgConfig(String name) {
            super();
            this.name = name;
        }

        public List<String> getDepsDev() {
            return depsDev;
        }

        public List<String> getDepsBin() {
            return depsBin;
        }

        List<String> depsDev = new ArrayList<String>();

        List<String> depsBin = new ArrayList<String>();

        List<String> cFlags = null;

        String getFlagsForPackage(String pkgName, String flagType) throws IOException {
            String command = "pkg-config " + pkgName + " " + flagType;
            String ret = getOutput(command);
            return ret.replace('\r', ' ').replace('\n', ' ');
        }

        public List<String> getCFlags() throws IOException {
            if (cFlags == null) {
                cFlags = new ArrayList<String>();
                cFlags.add(getFlagsForPackage(name, "--cflags"));
            }
            return cFlags;
        }

        public List<String> getLddFlags() throws IOException {
            if (lddFlags == null) {
                lddFlags = new ArrayList<String>();
                lddFlags.add(getFlagsForPackage(name, "--libs"));
            }
            return lddFlags;
        }

        List<String> lddFlags = null;
    }

    public static String getOutput(String command) throws IOException {
        StringBuilder ret = new StringBuilder();
        Process p = Runtime.getRuntime().exec(command);
        InputStream is = p.getInputStream();
        try {
            int ch;
            while ((ch = is.read()) >= 0) {
                ret.append((char) ch);
            }
        } finally {
            is.close();
        }
        return ret.toString();
    }

    Map<String, PkgConfig> configs = new HashMap<String, PkgConfig>();

    public String getArgsForPackage(String pkgName) throws IOException {
        PkgConfig conf = checkPackage(pkgName);
        List<String> ret = new ArrayList<String>();
        ret.addAll(conf.getCFlags());
        ret.addAll(conf.getLddFlags());
        return UtilString.toString(ret);
    }

    public Collection<String> getDepsBin(String pkgDep) {
        PkgConfig conf = checkPackage(pkgDep);
        return conf.getDepsBin();
    }

    public Collection<String> getDepsDev(String pkgDep) {
        PkgConfig conf = checkPackage(pkgDep);
        return conf.getDepsDev();
    }

    private PkgConfig checkPackage(String pkgName) {
        if (configs.get(pkgName) == null) {
            throw new RuntimeException("Package is not defined! " + pkgName);
        }
        return configs.get(pkgName);
    }

    public void addPackageDecl(File f) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(f);
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        XPathExpression expr = xpath.compile("//pkg-config/pkg");
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) result;
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            String id = "" + node.getAttributes().getNamedItem("id").getNodeValue();
            PkgConfig conf = new PkgConfig(id);
            List<String> cflags = null;
            List<String> ldflags = null;
            {
                XPathExpression exp2 = xpath.compile("cflags");
                Object result2 = exp2.evaluate(node, XPathConstants.NODESET);
                NodeList nodes2 = (NodeList) result2;
                if (nodes2.getLength() > 0) {
                    cflags = new ArrayList<String>();
                }
            }
            {
                XPathExpression exp2 = xpath.compile("ldflags");
                Object result2 = exp2.evaluate(node, XPathConstants.NODESET);
                NodeList nodes2 = (NodeList) result2;
                if (nodes2.getLength() > 0) {
                    ldflags = new ArrayList<String>();
                }
            }
            {
                XPathExpression exp2 = xpath.compile("cflags/cflag");
                Object result2 = exp2.evaluate(node, XPathConstants.NODESET);
                NodeList nodes2 = (NodeList) result2;
                for (int j = 0; j < nodes2.getLength(); j++) {
                    Node n2 = nodes2.item(j);
                    cflags.add("" + n2.getTextContent());
                }
            }
            {
                XPathExpression exp2 = xpath.compile("ldflags/ldflag");
                Object result2 = exp2.evaluate(node, XPathConstants.NODESET);
                NodeList nodes2 = (NodeList) result2;
                for (int j = 0; j < nodes2.getLength(); j++) {
                    Node n2 = nodes2.item(j);
                    ldflags.add("" + n2.getTextContent());
                }
            }
            {
                XPathExpression exp2 = xpath.compile("depBin");
                Object result2 = exp2.evaluate(node, XPathConstants.NODESET);
                NodeList nodes2 = (NodeList) result2;
                for (int j = 0; j < nodes2.getLength(); j++) {
                    Node n2 = nodes2.item(j);
                    conf.depsBin.add("" + n2.getTextContent());
                }
            }
            {
                XPathExpression exp2 = xpath.compile("depDev");
                Object result2 = exp2.evaluate(node, XPathConstants.NODESET);
                NodeList nodes2 = (NodeList) result2;
                for (int j = 0; j < nodes2.getLength(); j++) {
                    Node n2 = nodes2.item(j);
                    conf.depsDev.add("" + n2.getTextContent());
                }
            }
            conf.cFlags = cflags;
            conf.lddFlags = ldflags;
            configs.put(id, conf);
        }
    }
}
