package es.unizar.tecnodiscap.osgi4ami.gateway.xml;

import es.unizar.tecnodiscap.osgi4ami.device.Device;
import es.unizar.tecnodiscap.osgi4ami.device.sensor.SensorListener;
import es.unizar.tecnodiscap.osgi4ami.service.Service;
import es.unizar.tecnodiscap.osgi4ami.gateway.xml.tcp.TcpGatewayListener;
import es.unizar.tecnodiscap.util.sockets.tcp.TcpClientThread;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public abstract class XMLProcessor implements TcpGatewayListener {

    public static final String COMMANDS = "commands";

    public static final String SERVICE = "service";

    public static final String DEVICE = "device";

    public static final String CLUSTER = "cluster";

    public static final String METHOD = "method";

    public static final String PARAMETER = "parameter";

    public static final String ENTRY = "entry";

    public static final String KEY_CLASS = "key_class";

    public static final String KEY_VALUE = "key_value";

    public static final String VALUE_CLASS = "value_class";

    public static final String VALUE_VALUE = "value";

    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String CLASS = "class";

    public static final String LISTENER = "listener";

    public static final String STRING = "java.lang.String";

    public static final String MAP = "java.util.Map";

    public static final String EXECUTE_METHOD = "execute_method";

    public static final String REQUEST_DEVICE_LIST = "request_device_list";

    public static final String REQUEST_ALL_DEVICE_LIST = "request_all_device_list";

    public static final String REQUEST_SERVICE_LIST = "request_service_list";

    public static final String EVENTS = "events";

    public static final String DEVICE_LIST_REPORT = "device_list_report";

    public static final String SERVICE_LIST_REPORT = "service_list_report";

    public static final String EXECUTE_METHOD_RESULT = "execute_method_result";

    public static final String RESULT = "result";

    public static final String DEVICE_DESCRIPTION = "device_description";

    public static final String SERVICE_DESCRIPTION = "service_description";

    public static final String EVENT = "event";

    public static final String ERROR_CODE = "error_code";

    public static final int ERROR_CODE_OK = 0;

    public static final int ERROR_CODE_METHOD_EXECUTION = 1;

    public static final int ERROR_CODE_METHOD = 2;

    public static final int ERROR_CODE_CLUSTER = 3;

    public static final int ERROR_CODE_DEVICE = 4;

    public static final int ERROR_CODE_SERVICE = 5;

    public static final int ERROR_CODE_UNKNOWN_COMMAND = 6;

    public static final int ERROR_CODE_NO_DRIVER_FOUND = 7;

    public static final int ERROR_CODE_REGISTER_EVENT_LISTENER = 8;

    public static final int ERROR_CODE_UNREGISTER_EVENT_LISTENER = 9;

    protected org.w3c.dom.Document document;

    private TcpClientThread tct;

    @Override
    public synchronized void newCommand(org.w3c.dom.Document document, TcpClientThread tct) {
        this.document = document;
        this.tct = tct;
        String result = processDocument();
        tct.sendMessage(result);
    }

    /**
     * Scan through org.w3c.dom.Document document.
     */
    public String processDocument() {
        String result = "";
        org.w3c.dom.Element element = document.getDocumentElement();
        if ((element != null) && element.getTagName().equals(COMMANDS)) {
            result = visitElement_commands(element);
        }
        if ((element != null) && element.getTagName().equals(REQUEST_DEVICE_LIST)) {
            result = visitElement_request_device_list(element, false);
        }
        if ((element != null) && element.getTagName().equals(REQUEST_ALL_DEVICE_LIST)) {
            result = visitElement_request_device_list(element, true);
        }
        if ((element != null) && element.getTagName().equals(EXECUTE_METHOD)) {
            result = visitElement_execute_method(element);
        }
        if ((element != null) && element.getTagName().equals(REQUEST_SERVICE_LIST)) {
            result = visitElement_request_service_list(element, true);
        }
        return result;
    }

    /**
     * Scan through org.w3c.dom.Element named commands.
     */
    private String visitElement_commands(org.w3c.dom.Element element) {
        StringBuilder result = new StringBuilder("<" + EVENTS + ">");
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch(node.getNodeType()) {
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals(REQUEST_DEVICE_LIST)) {
                        result.append(visitElement_request_device_list(nodeElement, false));
                    } else if (nodeElement.getTagName().equals(REQUEST_ALL_DEVICE_LIST)) {
                        result.append(visitElement_request_device_list(nodeElement, true));
                    } else if (nodeElement.getTagName().equals(EXECUTE_METHOD)) {
                        result.append(visitElement_execute_method(nodeElement));
                    } else if (nodeElement.getTagName().equals(REQUEST_SERVICE_LIST)) {
                        result.append(visitElement_request_service_list(element, true));
                    } else {
                        result.append(("<" + XMLProcessor.ERROR_CODE + ">" + ERROR_CODE_UNKNOWN_COMMAND + "</" + XMLProcessor.ERROR_CODE + ">"));
                    }
                    break;
            }
        }
        result.append("</" + EVENTS + ">");
        return result.toString();
    }

    /**
     * Scan through org.w3c.dom.Element named request_device_list.
     */
    private String visitElement_request_device_list(org.w3c.dom.Element element, boolean allDetails) {
        StringBuilder result = new StringBuilder("<" + DEVICE_LIST_REPORT + ">");
        XMLCommandRequestListFromDriver requestList = new XMLCommandRequestListFromDriver();
        result.append(requestList.createXMLDeviceList(getDevices(), allDetails));
        result.append("</" + DEVICE_LIST_REPORT + ">");
        return result.toString();
    }

    /**
     * Scan through org.w3c.dom.Element named request_service_list.
     */
    private String visitElement_request_service_list(org.w3c.dom.Element element, boolean allDetails) {
        StringBuilder result = new StringBuilder("<" + SERVICE_LIST_REPORT + ">");
        XMLCommandRequestListServices requestList = new XMLCommandRequestListServices();
        result.append(requestList.createXMLServiceList(getServices(), allDetails));
        result.append("</" + SERVICE_LIST_REPORT + ">");
        return result.toString();
    }

    /**
     * Scan through org.w3c.dom.Element named execute_method.
     */
    private String visitElement_execute_method(org.w3c.dom.Element element) {
        StringBuilder result = new StringBuilder("<" + EXECUTE_METHOD_RESULT + ">");
        XMLCommandExecuteMethod executeMethod = new XMLCommandExecuteMethod();
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            switch(node.getNodeType()) {
                case org.w3c.dom.Node.ELEMENT_NODE:
                    org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                    if (nodeElement.getTagName().equals(DEVICE)) {
                        result.append(visitElement_device(nodeElement, executeMethod));
                    }
                    if (nodeElement.getTagName().equals(SERVICE)) {
                        result.append(visitElement_service(nodeElement, executeMethod));
                    }
                    break;
            }
        }
        result.append("</" + EXECUTE_METHOD_RESULT + ">");
        return result.toString();
    }

    /**
     * Scan through org.w3c.dom.Element named device.
     */
    private String visitElement_device(org.w3c.dom.Element element, XMLCommandExecuteMethod executeMethod) {
        StringBuilder result = new StringBuilder();
        String deviceID = "";
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals(ID)) {
                deviceID = attr.getValue();
            }
        }
        result.append("<" + DEVICE + " id=\"" + deviceID + "\">");
        if (!deviceID.equals("")) {
            Device device = getDevice(deviceID);
            if (device != null) {
                executeMethod.setObject(device);
                org.w3c.dom.NodeList nodes = element.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    org.w3c.dom.Node node = nodes.item(i);
                    switch(node.getNodeType()) {
                        case org.w3c.dom.Node.ELEMENT_NODE:
                            org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                            if (nodeElement.getTagName().equals(CLUSTER)) {
                                result.append(visitElement_cluster(nodeElement, executeMethod));
                            }
                            break;
                    }
                }
            } else {
                result.append(("<" + XMLProcessor.ERROR_CODE + ">" + ERROR_CODE_DEVICE + "</" + XMLProcessor.ERROR_CODE + ">"));
            }
        }
        result.append("</" + DEVICE + ">");
        return result.toString();
    }

    /**
     * Scan through org.w3c.dom.Element named service.
     */
    private String visitElement_service(org.w3c.dom.Element element, XMLCommandExecuteMethod executeMethod) {
        StringBuilder result = new StringBuilder();
        String serviceID = "";
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals(ID)) {
                serviceID = attr.getValue();
            }
        }
        result.append("<" + SERVICE + " id=\"" + serviceID + "\">");
        if (!serviceID.equals("")) {
            Service service = getService(serviceID);
            if (service != null) {
                executeMethod.setObject(service);
                org.w3c.dom.NodeList nodes = element.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    org.w3c.dom.Node node = nodes.item(i);
                    switch(node.getNodeType()) {
                        case org.w3c.dom.Node.ELEMENT_NODE:
                            org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                            if (nodeElement.getTagName().equals(CLUSTER)) {
                                result.append(visitElement_cluster(nodeElement, executeMethod));
                            }
                            break;
                    }
                }
            } else {
                result.append(("<" + XMLProcessor.ERROR_CODE + ">" + ERROR_CODE_SERVICE + "</" + XMLProcessor.ERROR_CODE + ">"));
            }
        }
        result.append("</" + SERVICE + ">");
        return result.toString();
    }

    /**
     * Scan through org.w3c.dom.Element named cluster.
     */
    private String visitElement_cluster(org.w3c.dom.Element element, XMLCommandExecuteMethod executeMethod) {
        StringBuilder result = new StringBuilder();
        String clusterID = "";
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals("id")) {
                clusterID = attr.getValue();
            }
        }
        result.append("<" + CLUSTER + " id=\"" + clusterID + "\">");
        if (!clusterID.equals("")) {
            if (executeMethod.setCluster(clusterID)) {
                org.w3c.dom.NodeList nodes = element.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    org.w3c.dom.Node node = nodes.item(i);
                    switch(node.getNodeType()) {
                        case org.w3c.dom.Node.ELEMENT_NODE:
                            org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                            if (nodeElement.getTagName().equals(METHOD)) {
                                result.append(visitElement_method(nodeElement, executeMethod));
                            }
                            break;
                    }
                }
            } else {
                result.append(("<" + XMLProcessor.ERROR_CODE + ">" + ERROR_CODE_CLUSTER + "</" + XMLProcessor.ERROR_CODE + ">"));
            }
        }
        result.append("</" + CLUSTER + ">");
        return result.toString();
    }

    /**
     * Scan through org.w3c.dom.Element named method.
     */
    private String visitElement_method(org.w3c.dom.Element element, XMLCommandExecuteMethod executeMethod) {
        StringBuilder result = new StringBuilder();
        boolean execute = true;
        String methodName = "";
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals(NAME)) {
                methodName = attr.getValue();
            }
        }
        result.append("<" + METHOD + " id=\"" + methodName + "\">");
        if (!methodName.equals("")) {
            if (executeMethod.isMethodOfCluster(methodName)) {
                Class[] argTypes = null;
                Object[] args = null;
                org.w3c.dom.NodeList nodes = element.getChildNodes();
                argTypes = new Class[nodes.getLength()];
                args = new Object[nodes.getLength()];
                for (int i = 0; i < nodes.getLength(); i++) {
                    org.w3c.dom.Node node = nodes.item(i);
                    switch(node.getNodeType()) {
                        case org.w3c.dom.Node.ELEMENT_NODE:
                            org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                            if (nodeElement.getTagName().equals(PARAMETER)) {
                                argTypes[i] = visitElement_parameterClass(nodeElement);
                                args[i] = visitElement_parameterObject(nodeElement);
                            }
                            break;
                    }
                }
                if (executeMethod.isEventListenerRegistering(methodName)) {
                    argTypes = new Class[1];
                    args = new Object[1];
                    if (executeMethod.getRegisteredListenerForDevice(methodName, tct, null, null) != null) {
                        result.append(("<" + XMLProcessor.ERROR_CODE + ">" + ERROR_CODE_REGISTER_EVENT_LISTENER + "</" + XMLProcessor.ERROR_CODE + ">"));
                        execute = false;
                    } else {
                        Object eventListener = executeMethod.getInstanceListener(methodName, tct, argTypes, args);
                        if (eventListener == null) {
                            result.append(("<" + XMLProcessor.ERROR_CODE + ">" + ERROR_CODE_REGISTER_EVENT_LISTENER + "</" + XMLProcessor.ERROR_CODE + ">"));
                            execute = false;
                        } else {
                            executeMethod.addListener(eventListener, argTypes[0], methodName, tct);
                        }
                    }
                } else if (executeMethod.isEventListenerUnregistering(methodName)) {
                    argTypes = new Class[1];
                    args = new Object[1];
                    Object listenerToRemove = executeMethod.getRegisteredListenerForDevice(methodName, tct, argTypes, args);
                    if (listenerToRemove != null) {
                        executeMethod.removeListener(listenerToRemove, tct);
                    } else {
                        result.append(("<" + XMLProcessor.ERROR_CODE + ">" + ERROR_CODE_UNREGISTER_EVENT_LISTENER + "</" + XMLProcessor.ERROR_CODE + ">"));
                        execute = false;
                    }
                }
                if (execute) {
                    result.append(executeMethod.runMethod(methodName, argTypes, args));
                }
            } else {
                result.append(("<" + XMLProcessor.ERROR_CODE + ">" + ERROR_CODE_METHOD + "</" + XMLProcessor.ERROR_CODE + ">"));
            }
        }
        result.append("</" + METHOD + ">");
        return result.toString();
    }

    /**
     * Scan through org.w3c.dom.Element named parameter.
     */
    private Class visitElement_parameterClass(org.w3c.dom.Element element) {
        Class parameter = null;
        String className = "";
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals(CLASS)) {
                className = attr.getValue();
            }
        }
        if (!className.equals("")) {
            try {
                parameter = Class.forName(className);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        return parameter;
    }

    /**
     * Scan through org.w3c.dom.Element named parameter.
     */
    private Object visitElement_parameterObject(org.w3c.dom.Element element) {
        Object parameter = null;
        String className = "";
        String keyClass = "";
        String valueClass = "";
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals(VALUE_CLASS)) {
                valueClass = attr.getValue();
            }
            if (attr.getName().equals(KEY_CLASS)) {
                keyClass = attr.getValue();
            }
            if (attr.getName().equals(CLASS)) {
                className = attr.getValue();
            }
        }
        if (!className.equals("")) {
            if (className.equals(MAP)) {
                parameter = new HashMap<Object, Object>();
                org.w3c.dom.NodeList nodes = element.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    org.w3c.dom.Node node = nodes.item(i);
                    switch(node.getNodeType()) {
                        case org.w3c.dom.Node.ELEMENT_NODE:
                            org.w3c.dom.Element nodeElement = (org.w3c.dom.Element) node;
                            if (nodeElement.getTagName().equals(ENTRY)) {
                                Object key = visitElement_entryKey(nodeElement, keyClass);
                                Object value = visitElement_entryValue(nodeElement, valueClass);
                                ((Map) parameter).put(key, value);
                            }
                            break;
                    }
                }
            } else {
                parameter = createObject(className, XMLTools.getNodeValue(element));
            }
        }
        return parameter;
    }

    /**
     * Scan through org.w3c.dom.Element named entry. That means that is a complex parameter(like java.util.Map)
     */
    private Object visitElement_entryKey(org.w3c.dom.Element element, String keyClass) {
        Object result = null;
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals(KEY_VALUE)) {
                result = createObject(keyClass, attr.getValue());
            }
        }
        return result;
    }

    /**
     * Scan through org.w3c.dom.Element named entry. That means that is a complex parameter(like java.util.Map)
     */
    private Object visitElement_entryValue(org.w3c.dom.Element element, String valueClass) {
        Object result = null;
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr) attrs.item(i);
            if (attr.getName().equals(VALUE_VALUE)) {
                result = createObject(valueClass, attr.getValue());
            }
        }
        return result;
    }

    private Object createObject(String className, String value) {
        Object obj = null;
        try {
            Class parametro = Class.forName(className);
            Class[] paramTypes = { String.class };
            Constructor cons = parametro.getConstructor(paramTypes);
            Object[] args = { value };
            obj = cons.newInstance(args);
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    protected abstract ArrayList<Device> getDevices();

    protected abstract Device getDevice(String id);

    protected abstract ArrayList<Service> getServices();

    protected abstract Service getService(String id);

    public static String getXMLCollection(Collection tmp) {
        StringBuilder xmlResult = new StringBuilder();
        Iterator it = tmp.iterator();
        xmlResult.append("<" + XMLProcessor.RESULT + ">");
        while (it.hasNext()) {
            Object value = (Object) it.next();
            xmlResult.append("<" + XMLProcessor.ENTRY + " " + XMLProcessor.CLASS + "=\"" + value.getClass().toString() + "\">" + value + "</" + XMLProcessor.ENTRY + ">");
        }
        xmlResult.append("</" + XMLProcessor.RESULT + ">");
        return xmlResult.toString();
    }

    public static String getXMLMap(Map tmp) {
        StringBuilder xmlResult = new StringBuilder();
        Iterator it = tmp.keySet().iterator();
        if (!tmp.keySet().isEmpty()) {
            xmlResult.append("<" + XMLProcessor.RESULT + " " + XMLProcessor.CLASS + "=\"" + tmp.getClass().getName() + "\" " + XMLProcessor.KEY_CLASS + "=\"" + tmp.keySet().toArray()[0].getClass().toString() + "\" " + XMLProcessor.VALUE_CLASS + "=\"" + (tmp.get(tmp.keySet().toArray()[0])).getClass().toString() + "\">");
            while (it.hasNext()) {
                Object key = (Object) it.next();
                Object value = tmp.get(key);
                xmlResult.append("<" + XMLProcessor.ENTRY + " " + XMLProcessor.KEY_VALUE + "=\"" + key + "\">" + value + "</" + XMLProcessor.ENTRY + ">");
            }
        } else {
            xmlResult.append("<" + XMLProcessor.RESULT + " " + XMLProcessor.CLASS + "=\"" + tmp.getClass().getName() + "\" " + XMLProcessor.KEY_CLASS + "=\"null\" " + XMLProcessor.VALUE_CLASS + "=\"null\">");
        }
        xmlResult.append("</" + XMLProcessor.RESULT + ">");
        return xmlResult.toString();
    }
}
