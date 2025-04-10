package com.sun.j2ee.blueprints.waf.controller.web;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * This class provides the data bindings for the screendefinitions.xml
 * and the requestmappings.xml file.
 * The data obtained is maintained by the ScreenFlowManager
 */
public class URLMappingsXmlDAO {

    private static final Logger logger = LoggerFactory.getLogger(URLMappingsXmlDAO.class);

    public static final String URL_MAPPING = "url-mapping";

    public static final String EVENT_MAPPING = "event-mapping";

    public static final String EXCEPTION_MAPPING = "exception-mapping";

    public static final String URL = "url";

    public static final String LANGUAGE = "language";

    public static final String TEMPLATE = "template";

    public static final String RESULT = "result";

    public static final String NEXT_SCREEN = "screen";

    public static final String PROCESSS_ACTION = "isAction";

    public static final String REQUIRES_SIGNIN = "requiresSignin";

    public static final String USE_FLOW_HANDLER = "useFlowHandler";

    public static final String IS_TRANSACTIONAL = "isTransactional";

    public static final String FLOW_HANDLER_CLASS = "class";

    public static final String WEB_ACTION_CLASS = "web-action-class";

    public static final String EJB_ACTION_CLASS = "ejb-action-class";

    public static final String COMMAND_CLASS = "command-class";

    public static final String EVENT_CLASS = "event-class";

    public static final String HANDLER_RESULT = "handler-result";

    public static final String FLOW_HANDLER = "flow-handler";

    public static final String EXCEPTION_CLASS = "exception-class";

    public static final String DEFAULT_SCREEN = "default-screen";

    public static final String KEY = "key";

    public static final String VALUE = "value";

    public static final String DIRECT = "direct";

    public static final String SCREEN = "screen";

    public static final String SCREEN_NAME = "screen-name";

    public static final String PARAMETER = "parameter";

    public static Element loadDocument(String location) {
        Document doc = null;
        try {
            URL url = new URL(location);
            InputSource xmlInp = new InputSource(url.openStream());
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = docBuilderFactory.newDocumentBuilder();
            doc = parser.parse(xmlInp);
            Element root = doc.getDocumentElement();
            root.normalize();
            return root;
        } catch (SAXParseException err) {
            logger.error("URLMappingsXmlDAO ** Parsing error, line {}, uri {}.", Integer.valueOf(err.getLineNumber()), err.getSystemId());
            logger.error("URLMappingsXmlDAO error: ", err);
        } catch (Exception pce) {
            logger.error("URLMappingsXmlDAO error: ", pce);
        }
        return null;
    }

    public static ScreenFlowData loadScreenFlowData(String location) {
        Element root = loadDocument(location);
        List exceptionMappings = getExceptionMappings(root);
        String defaultScreen = getTagValue(root, DEFAULT_SCREEN);
        return new ScreenFlowData(exceptionMappings, defaultScreen);
    }

    public static Map loadRequestMappings(String location) {
        Element root = loadDocument(location);
        return getRequestMappings(root);
    }

    public static List loadExceptionMappings(String location) {
        Element root = loadDocument(location);
        return getExceptionMappings(root);
    }

    public static Map loadEventMappings(String location) {
        Element root = loadDocument(location);
        return getEventMappings(root);
    }

    private static String getSubTagAttribute(Element root, String tagName, String subTagName, String attribute) {
        String returnString = "";
        NodeList list = root.getElementsByTagName(tagName);
        for (int loop = 0; loop < list.getLength(); loop++) {
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

    public static List getExceptionMappings(Element root) {
        List exceptionMappings = new ArrayList();
        NodeList list = root.getElementsByTagName(EXCEPTION_MAPPING);
        for (int loop = 0; loop < list.getLength(); loop++) {
            Node node = list.item(loop);
            if (node != null) {
                String exceptionClassName = "";
                String screen = null;
                if (node instanceof Element) {
                    Element element = ((Element) node);
                    exceptionClassName = element.getAttribute(EXCEPTION_CLASS);
                    screen = element.getAttribute(SCREEN);
                    exceptionMappings.add(new ErrorMapping(exceptionClassName, screen));
                }
            }
        }
        return exceptionMappings;
    }

    public static Map getEventMappings(Element root) {
        Map eventMappings = new HashMap();
        NodeList list = root.getElementsByTagName(EVENT_MAPPING);
        for (int loop = 0; loop < list.getLength(); loop++) {
            Node node = list.item(loop);
            if (node != null) {
                String eventClassName = getSubTagValue(node, EVENT_CLASS);
                String commandClassName = getSubTagValue(node, EJB_ACTION_CLASS);
                if ((commandClassName == null) || commandClassName.equals("")) {
                    commandClassName = getSubTagValue(node, COMMAND_CLASS);
                }
                if ((eventClassName != null) && !eventClassName.equals("")) {
                    eventMappings.put(eventClassName, new EventMapping(eventClassName, commandClassName));
                }
            }
        }
        return eventMappings;
    }

    public static Map getRequestMappings(Element root) {
        Map urlMappings = new HashMap();
        NodeList list = root.getElementsByTagName(URL_MAPPING);
        for (int loop = 0; loop < list.getLength(); loop++) {
            Node node = list.item(loop);
            if (node != null) {
                String url = "";
                String screen = null;
                String useActionString = null;
                String useFlowHandlerString = null;
                String requiresSigninString = null;
                String isTransactionalString = null;
                String flowHandler = null;
                String webActionClass = null;
                String ejbActionClass = null;
                HashMap resultMappings = null;
                boolean useFlowHandler = false;
                boolean isAction = false;
                boolean requiresSignin = false;
                boolean isTransactional = false;
                if (node instanceof Element) {
                    Element element = ((Element) node);
                    url = element.getAttribute(URL);
                    screen = element.getAttribute(NEXT_SCREEN);
                    useActionString = element.getAttribute(PROCESSS_ACTION);
                    useFlowHandlerString = element.getAttribute(USE_FLOW_HANDLER);
                    isTransactionalString = element.getAttribute(IS_TRANSACTIONAL);
                }
                webActionClass = getSubTagValue(node, WEB_ACTION_CLASS);
                if (webActionClass != null) isAction = true;
                if ((useFlowHandlerString != null) && useFlowHandlerString.toLowerCase().equals("true")) useFlowHandler = true;
                if ((useFlowHandlerString != null) && useFlowHandlerString.toLowerCase().equals("true")) useFlowHandler = true;
                if ((isTransactionalString != null) && isTransactionalString.toLowerCase().equals("true")) isTransactional = true;
                if (useFlowHandler) {
                    if (node instanceof Element) {
                        Element element = (Element) node;
                        NodeList children = element.getElementsByTagName(FLOW_HANDLER);
                        Node flowHandlerNode = null;
                        if (children.getLength() >= 1) {
                            flowHandlerNode = children.item(0);
                        }
                        if (children.getLength() > 1) {
                            logger.warn("Non fatal error: There can be only one <{}> definition in a <{}>", FLOW_HANDLER, URL_MAPPING);
                        }
                        if (flowHandlerNode != null) {
                            if (flowHandlerNode instanceof Element) {
                                Element flowElement = (Element) flowHandlerNode;
                                flowHandler = flowElement.getAttribute(FLOW_HANDLER_CLASS);
                                NodeList results = flowElement.getElementsByTagName(HANDLER_RESULT);
                                if (results.getLength() > 0) {
                                    resultMappings = new HashMap();
                                }
                                for (int resultLoop = 0; resultLoop < results.getLength(); resultLoop++) {
                                    Node resultNode = results.item(resultLoop);
                                    if (resultNode instanceof Element) {
                                        Element resultElement = (Element) resultNode;
                                        String key = resultElement.getAttribute(RESULT);
                                        String value = resultElement.getAttribute(NEXT_SCREEN);
                                        if (!resultMappings.containsKey(key)) {
                                            resultMappings.put(key, value);
                                        } else {
                                            logger.warn("*** Non Fatal errror: Screen \"{}\" <{}> key \"{}\" defined more than one time", new Object[] { url, FLOW_HANDLER, key });
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                URLMapping mapping = new URLMapping(url, screen, isAction, useFlowHandler, isTransactional, webActionClass, flowHandler, resultMappings);
                if (!urlMappings.containsKey(url)) {
                    urlMappings.put(url, mapping);
                } else {
                    logger.warn("*** Non Fatal errror: Screen {} defined more than once in screen definitions file", url);
                }
            }
        }
        return urlMappings;
    }

    public static String getSubTagValue(Node node, String subTagName) {
        String returnString = "";
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
        return returnString;
    }

    public static String getSubTagValue(Element root, String tagName, String subTagName) {
        String returnString = "";
        NodeList list = root.getElementsByTagName(tagName);
        for (int loop = 0; loop < list.getLength(); loop++) {
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

    public static String getTagValue(Element root, String tagName) {
        String returnString = "";
        NodeList list = root.getElementsByTagName(tagName);
        for (int loop = 0; loop < list.getLength(); loop++) {
            Node node = list.item(loop);
            if (node != null) {
                Node child = node.getFirstChild();
                if ((child != null) && child.getNodeValue() != null) return child.getNodeValue();
            }
        }
        return returnString;
    }
}
