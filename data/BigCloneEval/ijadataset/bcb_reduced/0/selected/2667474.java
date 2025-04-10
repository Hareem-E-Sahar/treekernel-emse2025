package com.sun.j2ee.blueprints.signon.web;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 * This class provides the data bindings for the screendefinitions.xml and the
 * requestmappings.xml file. The data obtained is maintained by the
 * ScreenFlowManager
 */
public class ConfigFileSignOnDAO {

    private final Logger logger = LoggerFactory.getLogger(ConfigFileSignOnDAO.class);

    public static final String SIGNON_FORM_LOGIN_PAGE = "signon-form-login-page";

    public static final String SIGNON_FORM_ERROR_PAGE = "signon-form-error-page";

    public static final String SECURITY_CONSTRAINT = "security-constraint";

    public static final String WEB_RESOURCE_COLLECTION = "web-resource-collection";

    public static final String WEB_RESOURCE_NAME = "web-resource-name";

    public static final String URL_PATTERN = "url-pattern";

    public static final String AUTH_CONSTRAINT = "auth-constraint";

    public static final String ROLE_NAME = "role-name";

    private String signOnLoginPage = null;

    private String signOnErrorPage = null;

    private Map protectedResources = null;

    public ConfigFileSignOnDAO(URL configURL) {
        Element root = loadDocument(configURL);
        protectedResources = getProtectedResources(root);
    }

    public String getSignOnPage() {
        return signOnLoginPage;
    }

    public String getSignOnErrorPage() {
        return signOnErrorPage;
    }

    public Map getProtectedResources() {
        return protectedResources;
    }

    private Element loadDocument(URL url) {
        Document doc = null;
        try {
            InputSource xmlInp = new InputSource(url.openStream());
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = docBuilderFactory.newDocumentBuilder();
            doc = parser.parse(xmlInp);
            Element root = doc.getDocumentElement();
            root.normalize();
            return root;
        } catch (SAXParseException err) {
            logger.error("ConfigFileSignOnDAO  ** Parsing error, line {}, uri {}", Integer.valueOf(err.getLineNumber()), err.getSystemId());
            logger.error("ConfigFileSignOnDAO  error: ", err);
        } catch (Exception pce) {
            logger.error("ConfigFileSignOnDAO  error: ", pce);
        }
        return null;
    }

    private Map getProtectedResources(Element root) {
        Map resources = new HashMap();
        signOnLoginPage = getTagValue(root, SIGNON_FORM_LOGIN_PAGE).trim();
        signOnErrorPage = getTagValue(root, SIGNON_FORM_ERROR_PAGE).trim();
        NodeList outterList = root.getElementsByTagName(SECURITY_CONSTRAINT);
        for (int outterLoop = 0; outterLoop < outterList.getLength(); outterLoop++) {
            Element element = (Element) outterList.item(outterLoop);
            ArrayList roles = new ArrayList();
            NodeList roleList = element.getElementsByTagName(AUTH_CONSTRAINT);
            for (int roleLoop = 0; (roleList != null) && roleLoop < roleList.getLength(); roleLoop++) {
                Node roleNode = roleList.item(roleLoop);
                String roleName = getSubTagValue(roleNode, ROLE_NAME);
                if ((roleName != null) && !roleName.equals("")) roles.add(roleName);
            }
            NodeList list = element.getElementsByTagName(WEB_RESOURCE_COLLECTION);
            for (int loop = 0; (list != null) && loop < list.getLength(); loop++) {
                Node node = list.item(loop);
                if (node != null) {
                    String resourceName = getSubTagValue(node, WEB_RESOURCE_NAME);
                    String urlPattern = getSubTagValue(node, URL_PATTERN);
                    ProtectedResource resource = new ProtectedResource(resourceName, urlPattern, roles);
                    if (!resources.containsKey(resourceName)) {
                        resources.put(resourceName, resource);
                    } else {
                        logger.warn("*** Non Fatal errror: Protected Resource {} defined more than once in screen definitions file", resourceName);
                    }
                }
            }
        }
        return resources;
    }

    private String getSubTagAttribute(Element root, String tagName, String subTagName, String attribute) {
        String returnString = "";
        NodeList list = root.getElementsByTagName(tagName);
        for (int loop = 0; (list != null) && loop < list.getLength(); loop++) {
            Node node = list.item(loop);
            if (node != null) {
                NodeList children = node.getChildNodes();
                for (int innerLoop = 0; innerLoop < children.getLength(); innerLoop++) {
                    Node child = children.item(innerLoop);
                    if ((child != null) && (child.getNodeName() != null) && child.getNodeName().equals(subTagName)) {
                        if (child instanceof Element) {
                            return ((Element) child).getAttribute(attribute);
                        }
                    }
                }
            }
        }
        return returnString;
    }

    private String getSubTagValue(Node node, String subTagName) {
        String returnString = "";
        if (node != null) {
            NodeList children = node.getChildNodes();
            for (int innerLoop = 0; (children != null) && innerLoop < children.getLength(); innerLoop++) {
                Node child = children.item(innerLoop);
                if ((child != null) && (child.getNodeName() != null) && child.getNodeName().equals(subTagName)) {
                    Node grandChild = child.getFirstChild();
                    if (grandChild.getNodeValue() != null) return grandChild.getNodeValue();
                }
            }
        }
        return returnString;
    }

    private String getSubTagValue(Element root, String tagName, String subTagName) {
        String returnString = "";
        NodeList list = root.getElementsByTagName(tagName);
        for (int loop = 0; (list != null) && loop < list.getLength(); loop++) {
            Node node = list.item(loop);
            if (node != null) {
                NodeList children = node.getChildNodes();
                for (int innerLoop = 0; innerLoop < children.getLength(); innerLoop++) {
                    Node child = children.item(innerLoop);
                    if ((child != null) && (child.getNodeName() != null) && child.getNodeName().equals(subTagName)) {
                        Node grandChild = child.getFirstChild();
                        if (grandChild.getNodeValue() != null) return grandChild.getNodeValue();
                    }
                }
            }
        }
        return returnString;
    }

    private String getTagValue(Element root, String tagName) {
        String returnString = "";
        NodeList list = root.getElementsByTagName(tagName);
        for (int loop = 0; (list != null) && loop < list.getLength(); loop++) {
            Node node = list.item(loop);
            if (node != null) {
                Node child = node.getFirstChild();
                if ((child != null) && child.getNodeValue() != null) return child.getNodeValue();
            }
        }
        return returnString;
    }
}
