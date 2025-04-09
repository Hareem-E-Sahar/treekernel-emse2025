package org.jage.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jage.config.ConfigurationException;
import org.jage.property.ClassPropertyContainer;
import org.jage.property.InvalidPropertyOperationException;
import org.jage.property.InvalidPropertyPathException;
import org.jage.property.MetaPropertiesSet;
import org.jage.property.MetaProperty;
import org.w3c.dom.Node;

/**
 * The class simplifies operations often performed using DOM package.
 * 
 * @author KrzS
 */
public class DomHelper {

    /**
	 * Finds a child node of the given node.
	 * 
	 * @param nodeName
	 *            child node's name
	 * @param aNode
	 *            a node with child nodes
	 * @return a child
	 */
    public static Node findChildNodeByName(String nodeName, Node aNode) {
        Node result = null;
        for (int i = 0; i < aNode.getChildNodes().getLength(); i++) if (aNode.getChildNodes().item(i).getNodeName().equals(nodeName)) {
            result = aNode.getChildNodes().item(i);
            break;
        }
        return result;
    }

    /**
	 * Finds a child node (of specific type) of the given node.
	 * 
	 * @param nodeName
	 *            child node's name
	 * @param aNode
	 *            a node with child nodes
	 * @param nodeType
	 *            type of child node (for instance Node.ELEMENT_NODE)
	 * @return a child
	 */
    public static Node findChildNodeByName(String nodeName, Node aNode, short nodeType) {
        Node result = null;
        for (int i = 0; i < aNode.getChildNodes().getLength(); i++) if (aNode.getChildNodes().item(i).getNodeType() == nodeType && aNode.getChildNodes().item(i).getNodeName().equals(nodeName)) {
            result = aNode.getChildNodes().item(i);
            break;
        }
        return result;
    }

    /**
	 * Finds all child nodes of the given node.
	 * 
	 * @param nodeName
	 *            child nodes' name
	 * @param aNode
	 *            a node with child nodes
	 * @return list of child nodes with the specified name
	 */
    public static List findChildNodeListByName(String nodeName, Node aNode) {
        ArrayList<Node> result = new ArrayList<Node>();
        for (int i = 0; i < aNode.getChildNodes().getLength(); i++) if (aNode.getChildNodes().item(i).getNodeName().equals(nodeName)) result.add(aNode.getChildNodes().item(i));
        return result;
    }

    /**
	 * Gets child nodes of a node stored in a map.
	 * 
	 * @param aNode
	 *            a node
	 * @return map of child nodes (names of nodes are the keys)
	 */
    public static Map<String, Node> getChildNodesAsMap(Node aNode) {
        Map<String, Node> result = new HashMap<String, Node>();
        for (int i = 0; i < aNode.getChildNodes().getLength(); i++) {
            Node node = aNode.getChildNodes().item(i);
            if (node.getNodeName() != null) result.put(node.getNodeName(), node);
        }
        return result;
    }

    /**
	 * Returns value of a node stored as a value of the first text child node.
	 * 
	 * @param aNode
	 *            the node to obtain a value
	 * @return value of the node
	 */
    public static String getNodeValue(Node aNode) {
        String result = null;
        if (aNode.getChildNodes().getLength() == 1 && aNode.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) result = aNode.getChildNodes().item(0).getNodeValue();
        return result;
    }

    /**
	 * Creates the object configured in XML. <BR>
	 * Sample configuration: <BR>
	 * 
	 * <PRE>
	 * 
	 * <connector name="socket"
	 * class="org.jage.connection.socket.SocketConnector"> <constructor>
	 * <parameter>12 </parameter> <parameter>test </parameter> </constructor>
	 * <properties><port>15202 </port> <active>true </active> </properties>
	 * </connector>
	 * 
	 * </PRE>
	 * 
	 * @param mainObjectNode
	 *            the main node of configuration of the object
	 * @param params
	 *            the first constructor parameters or <TT>null</TT> if there
	 *            are no required parameters
	 * @return the object configured in XML
	 * @throws ConfigurationException
	 *             occurs when configuration is wrong (incorrect
	 *             class/constructor/property etc.)
	 */
    public static Object createObjectFromXml(Node mainObjectNode, Object[] params) throws ConfigurationException {
        if (mainObjectNode == null || mainObjectNode.getNodeType() != Node.ELEMENT_NODE) throw new ConfigurationException("Incorrect type of node [" + (mainObjectNode == null ? "null" : mainObjectNode.getNodeName()));
        Node classAttrib = mainObjectNode.getAttributes().getNamedItem("class");
        if (classAttrib == null) throw new ConfigurationException("Class attribute has not been specified [node: " + mainObjectNode.getNodeName() + "]");
        Class objClass = null;
        try {
            objClass = Class.forName(classAttrib.getNodeValue());
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException(e);
        }
        if (!ClassPropertyContainer.class.isAssignableFrom(objClass)) throw new ConfigurationException("The class must extend ClassPropertyContainer class in order to be configurable from XML file [class: " + objClass.getName() + "]");
        Map mainSubNodeMap = DomHelper.getChildNodesAsMap(mainObjectNode);
        Node constructorNode = (Node) mainSubNodeMap.get("constructor");
        Node propertiesNode = (Node) mainSubNodeMap.get("properties");
        ClassPropertyContainer obj = null;
        try {
            if (constructorNode != null || params != null) {
                ArrayList<Object> parameters = new ArrayList<Object>();
                if (params != null) for (int i = 0; i < params.length; i++) parameters.add(params[i]);
                if (constructorNode != null) for (int i = 0; i < constructorNode.getChildNodes().getLength(); i++) {
                    Node node = constructorNode.getChildNodes().item(i);
                    if ((node.getNodeType() == Node.ELEMENT_NODE) && (node.getNodeName().equals("parameter")) && (node.getChildNodes().getLength() == 1) && (node.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE)) parameters.add(node.getChildNodes().item(0).getNodeValue());
                }
                Constructor constructor = null;
                Constructor[] constructors = objClass.getConstructors();
                Object[] arguments = new Object[parameters.size()];
                constructorLoop: for (int i = 0; i < constructors.length; i++) {
                    Class<?>[] paramTypes = constructors[i].getParameterTypes();
                    if (paramTypes.length == parameters.size()) {
                        for (int j = 0; j < parameters.size(); j++) {
                            try {
                                arguments[j] = ReflectHelper.convertValue((String) parameters.get(j), paramTypes[j]);
                            } catch (NumberFormatException e) {
                                break constructorLoop;
                            } catch (ParseException e) {
                                break constructorLoop;
                            }
                        }
                        constructor = constructors[i];
                        break;
                    }
                }
                if (constructor == null) throw new ConfigurationException("Cannot find appropriate constructor for " + objClass.getName());
                obj = (ClassPropertyContainer) constructor.newInstance(arguments);
            } else obj = (ClassPropertyContainer) objClass.newInstance();
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("Cannot create instance of " + classAttrib.getNodeValue(), e);
        } catch (InstantiationException e) {
            throw new ConfigurationException("Cannot create instance of " + classAttrib.getNodeValue(), e);
        } catch (InvocationTargetException e) {
            throw new ConfigurationException("Cannot create instance of " + classAttrib.getNodeValue(), e);
        }
        if (propertiesNode != null) {
            MetaPropertiesSet metaPropertyContainer = obj.getProperties().getMetaPropertiesSet();
            for (int j = 0; j < propertiesNode.getChildNodes().getLength(); j++) {
                Node propertyNode = propertiesNode.getChildNodes().item(j);
                if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
                    String propertyName = propertyNode.getNodeName();
                    if ((propertyNode.getChildNodes().getLength() != 1) || (propertyNode.getChildNodes().item(0).getNodeType() != Node.TEXT_NODE)) {
                        throw new ConfigurationException("Incorrect value of a property [property: " + propertyName + "]");
                    }
                    MetaProperty metaProperty = metaPropertyContainer.getMetaProperty(propertyName);
                    String propertyTextValue = propertyNode.getChildNodes().item(0).getNodeValue();
                    Object propertyValue = null;
                    try {
                        propertyValue = ReflectHelper.convertValue(propertyTextValue, metaProperty.getPropertyClass());
                    } catch (NumberFormatException e) {
                        throw new ConfigurationException("Incorrect format of value of the property [name: " + propertyName + ", value: " + propertyTextValue + ", expected type: " + metaProperty.getPropertyClass().getName() + "]", e);
                    } catch (ParseException e) {
                        throw new ConfigurationException("Incorrect format of value of the property [name: " + propertyName + ", value: " + propertyTextValue + ", expected type: " + metaProperty.getPropertyClass().getName() + "]", e);
                    }
                    try {
                        obj.getProperty(propertyName).setValue(propertyValue);
                    } catch (InvalidPropertyOperationException e) {
                        throw new ConfigurationException("Cannot set value of the property [property: " + propertyName + ", value: " + (propertyTextValue == null ? "null" : propertyTextValue) + "]", e);
                    } catch (InvalidPropertyPathException e) {
                        throw new ConfigurationException("Invalid property path [property: " + propertyName + ", value: " + (propertyTextValue == null ? "null" : propertyTextValue) + "]", e);
                    }
                }
            }
        }
        return obj;
    }
}
