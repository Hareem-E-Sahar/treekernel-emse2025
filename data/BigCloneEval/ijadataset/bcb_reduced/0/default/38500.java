import org.w3c.dom.*;
import javax.xml.xpath.*;
import javax.xml.namespace.*;
import java.io.*;
import javax.xml.parsers.*;

class XPathHelper {

    public static Node SelectNode(Node ParentNode, String xPath) {
        Node rtnNode = null;
        try {
            XPathFactory xpfactory = XPathFactory.newInstance();
            XPath myXpath;
            myXpath = xpfactory.newXPath();
            rtnNode = (Node) myXpath.evaluate(xPath, ParentNode, (QName) XPathConstants.class.getField("NODE").get(null));
        } catch (Exception Ex) {
        }
        return rtnNode;
    }

    public static NodeList SelectNodeList(Node ParentNode, String xPath) {
        NodeList rtnNodeList = null;
        try {
            XPathFactory xpfactory = XPathFactory.newInstance();
            XPath myXpath;
            myXpath = xpfactory.newXPath();
            rtnNodeList = (NodeList) myXpath.evaluate(xPath, ParentNode, (QName) XPathConstants.class.getField("NODESET").get(null));
        } catch (Exception Ex) {
        }
        return rtnNodeList;
    }

    public static String getString(Node ParentNode, String xPath) {
        String strReturn = null;
        try {
            XPathFactory xpfactory = XPathFactory.newInstance();
            XPath myXpath;
            myXpath = xpfactory.newXPath();
            strReturn = (String) myXpath.evaluate(xPath, ParentNode, (QName) XPathConstants.class.getField("STRING").get(null));
        } catch (Exception Ex) {
        }
        return strReturn;
    }

    public static float getFloat(Node ParentNode, String xPath, float defaultVal) {
        String sVal = getString(ParentNode, xPath);
        float fRet = defaultVal;
        try {
            fRet = Float.valueOf(sVal);
        } catch (Exception ex) {
            fRet = defaultVal;
        }
        return fRet;
    }

    public static Node LoadRoot(String path) {
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;
        File xmlFile = null;
        Document doc = null;
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            xmlFile = new File(path);
            doc = builder.parse(xmlFile);
        } catch (Exception Ex1) {
            System.out.println(Ex1.toString());
            System.exit(1);
        }
        return doc;
    }
}
