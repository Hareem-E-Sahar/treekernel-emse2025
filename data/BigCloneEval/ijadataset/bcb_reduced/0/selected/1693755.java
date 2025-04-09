package jifx.message.ifx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import jifx.commons.messages.IMessage;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.transform.JDOMSource;
import org.sourceforge.ifx.basetypes.IBaseType;
import org.sourceforge.ifx.framework.element.IFX;
import org.sourceforge.ifx.utils.IFXDocumentHandler;
import org.sourceforge.ifx.utils.IFXException;

/**
 * @author Arya Baher, modify by Juan Pablo Bochard
 *
 */
public class IFXMessage implements IMessage {

    static Logger logger = Logger.getLogger(IFXMessage.class);

    private static final long serialVersionUID = 1L;

    IFX ifx;

    static final String PATH_DELIMITER = ".";

    static final String GETTER_PREFIX = "get";

    static final String SETTER_PREFIX = "set";

    static final String LEFT_BRACKET = "[";

    static final String RIGHT_BRACKET = "]";

    static final String IFX_URI = "http://sourceforge.net/ifx-framework/ifx";

    static final boolean VALIDATE = false;

    /**
	 *
	 */
    public IFXMessage() {
        super();
        ifx = new IFX();
    }

    public IFXMessage(String xmlString) {
        super();
        try {
            Map<String, String> props = new HashMap<String, String>();
            props.put(IFX_URI, "cfg/ifx/1.7/IFX170_extended.xsd");
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlString.getBytes());
            Document docIn = IFXDocumentHandler.read(bais, VALIDATE, props);
            ifx = (IFX) IFXDocumentHandler.parse(docIn);
            if (ifx == null) throw new IOException("Error al parsear documento IFX");
        } catch (Exception e) {
            logger.error("IFXMessage|" + e.getMessage() + " - " + e.getCause().getMessage() + " | ");
        }
    }

    /**
	 *  Crea un objeto IFX a partir de un inputStream que puede ser un FileInputStream
	 * @param istream - InputStream desde donde se lee el mensaje ifx
	 */
    public IFXMessage(InputStream istream) {
        super();
        try {
            Map<String, String> props = new HashMap<String, String>();
            props.put(IFX_URI, "cfg/ifx/1.7/IFX170_extended.xsd");
            Document docIn = IFXDocumentHandler.read(istream, VALIDATE, props);
            ifx = (IFX) IFXDocumentHandler.parse(docIn);
        } catch (Exception e) {
            logger.error("IFXMessage|" + e.getMessage() + "|");
        }
    }

    public String[] getIFX_Elements() {
        return ifx.ELEMENTS;
    }

    /**
	 *  Devuelve el valor del objeto asociado a la clave especificada por par�metro
	 * @param key 	- Clave del objeto en el mensaje
	 * @return
	 */
    public Object getElement(Object key) {
        try {
            Object object = getInternalElement(key);
            if (object != null) return ((IBaseType) object).getString();
        } catch (Exception e) {
            logger.error("IFXMessage|getElement(" + key.toString() + "): " + e.getMessage() + "|");
        }
        return null;
    }

    public Map<String, Object> getAllElements(Object key) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map = getAllChildElements(map, "", getInternalElement(key));
            return map;
        } catch (Exception e) {
            logger.error("IFXMessage|getAllElements(" + key.toString() + "): " + e.getMessage() + "|");
        }
        return map;
    }

    /**
	 * @param key 	- Clave �nica del objeto en el mensaje
	 * @param value - Valor a setear en el objeto
	 */
    public void setElement(Object key, Object value) {
        String objectPath = (String) key;
        String objectName = null;
        Object ifxElement = null;
        objectPath = objectPath.replaceAll("\\\\.", "_");
        ifxElement = ifx;
        int dotPosition = -1;
        dotPosition = objectPath.indexOf(PATH_DELIMITER);
        while (dotPosition != -1) {
            objectName = objectPath.substring(0, dotPosition);
            Object auxIfxElement = getChildElement(ifxElement, objectName);
            if (auxIfxElement == null) {
                ifxElement = createElement(ifxElement, objectName);
            } else ifxElement = auxIfxElement;
            objectPath = objectPath.substring(dotPosition + 1, objectPath.length());
            dotPosition = objectPath.indexOf(PATH_DELIMITER);
        }
        objectName = objectPath;
        Object auxIfxElement = getChildElement(ifxElement, objectName);
        if (auxIfxElement == null) {
            ifxElement = createElement(ifxElement, objectName);
        } else ifxElement = auxIfxElement;
        if (ifxElement instanceof IBaseType) ((IBaseType) ifxElement).setString(value.toString());
    }

    /**
	 * @param key 	- Clave �nica del objeto en el mensaje
	 * @param value - Valor a setear en el objeto
	 */
    public void setAllElement(Object key, Map<String, Object> value) {
        if (key != null) {
            for (Object parKey : value.keySet()) {
                setElement(key.toString() + "." + parKey.toString(), value.get(parKey.toString()));
            }
        }
    }

    private Map<String, Object> getAllChildElements(Map<String, Object> map, String path, Object father) {
        try {
            Class c = father.getClass();
            Field elements = c.getField("ELEMENTS");
            Object array = elements.get(father);
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                String child = Array.get(array, i).toString();
                Object value = getChildElement(father, child);
                if (value != null) {
                    if (value instanceof IBaseType) {
                        try {
                            if (((IBaseType) value).getString() != null) map.put(path + child, ((IBaseType) value).getString());
                        } catch (NullPointerException e1) {
                        }
                    } else {
                        value.getClass().getField("ELEMENTS");
                        map = getAllChildElements(map, path + child + PATH_DELIMITER, value);
                    }
                }
            }
            return map;
        } catch (SecurityException e) {
            logger.error("IFXMessage|getAllChildElements: SecurityException-" + e.getMessage() + "|");
        } catch (IllegalArgumentException e) {
            logger.error("IFXMessage|getAllChildElements: IllegalArgumentException-" + e.getMessage() + "|");
        } catch (IllegalAccessException e) {
            logger.error("IFXMessage|getAllChildElements: IllegalAccessException-" + e.getMessage() + "|");
        } catch (NoSuchFieldException e) {
        }
        return null;
    }

    /**
	 * Devuelve el objeto asociado a la clave especificada por par�metro
	 * @param key - Clave �nica del objeto en el mensaje
	 * @return
	 */
    private Object getInternalElement(Object key) {
        String objectPath = (String) key;
        String objectName = null;
        Object ifxElement = null;
        objectPath = objectPath.replaceAll("\\\\.", "_");
        ifxElement = ifx;
        int dotPosition = -1;
        dotPosition = objectPath.indexOf(PATH_DELIMITER);
        while (dotPosition != -1) {
            objectName = objectPath.substring(0, dotPosition);
            ifxElement = getChildElement(ifxElement, objectName);
            if (ifxElement == null) return null;
            objectPath = objectPath.substring(dotPosition + 1, objectPath.length());
            dotPosition = objectPath.indexOf(PATH_DELIMITER);
        }
        objectName = objectPath;
        ifxElement = getChildElement(ifxElement, objectName);
        return ifxElement;
    }

    private Object getChildElement(Object father, String childName) {
        Object child = null;
        int arrayIndex = 0;
        String methodName = null;
        Method method;
        Class[] parameterTypes = new Class[] {};
        Object[] arguments = new Object[] {};
        try {
            Class c = father.getClass();
            if (childName.indexOf(LEFT_BRACKET) != -1) {
                arrayIndex = (new Integer(childName.substring(childName.indexOf(LEFT_BRACKET) + 1, childName.indexOf(RIGHT_BRACKET)))).intValue();
                childName = childName.substring(0, childName.indexOf(LEFT_BRACKET));
            }
            methodName = GETTER_PREFIX.concat(childName);
            method = c.getMethod(methodName, parameterTypes);
            child = method.invoke(father, arguments);
            if (child != null && method.getReturnType().isArray()) {
                if (Array.getLength(child) < arrayIndex + 1) return null;
                child = Array.get(child, arrayIndex);
            }
        } catch (NoSuchMethodException e) {
            child = null;
        } catch (IllegalAccessException e) {
            child = null;
        } catch (InvocationTargetException e) {
            child = null;
        }
        return child;
    }

    private Object createElement(Object father, String childName) {
        Object child = null;
        int arrayIndex = 0;
        String methodName = null;
        String setMethodName = null;
        Method method;
        Method setMethod;
        Class[] parameterTypes = new Class[] {};
        Class[] setParameterTypes = new Class[] {};
        Object[] arguments = new Object[] {};
        Object[] setParameters = new Object[] {};
        Object object = null;
        try {
            Class c = father.getClass();
            if (childName.indexOf(LEFT_BRACKET) != -1) {
                arrayIndex = (new Integer(childName.substring(childName.indexOf(LEFT_BRACKET) + 1, childName.indexOf(RIGHT_BRACKET)))).intValue();
                childName = childName.substring(0, childName.indexOf(LEFT_BRACKET));
            }
            methodName = GETTER_PREFIX.concat(childName);
            method = c.getMethod(methodName, parameterTypes);
            if (method.invoke(father, arguments) == null) {
                try {
                    String classPath = method.getReturnType().getCanonicalName();
                    if (method.getReturnType().isArray()) {
                        classPath = classPath.substring(0, classPath.indexOf(LEFT_BRACKET));
                    }
                    Class classDefinition = Class.forName(classPath);
                    if (method.getReturnType().isArray()) {
                        object = Array.newInstance(classDefinition, arrayIndex + 1);
                        Array.set(object, 0, classDefinition.newInstance());
                    } else {
                        object = classDefinition.newInstance();
                    }
                    setParameters = new Object[] { object };
                    setParameterTypes = new Class[] { method.getReturnType() };
                    setMethodName = SETTER_PREFIX.concat(childName);
                    setMethod = c.getMethod(setMethodName, setParameterTypes);
                    setMethod.invoke(father, setParameters);
                } catch (Exception e) {
                    logger.error("IFXMessage|createElement(" + father.toString() + "," + childName + "): " + e.getMessage() + "|");
                }
            }
            if (method.getReturnType().isArray()) {
                Object array = null;
                Object auxArray = null;
                array = method.invoke(father, arguments);
                try {
                    if (Array.getLength(array) < arrayIndex + 1) {
                        auxArray = Array.newInstance(method.getReturnType().getComponentType(), Array.getLength(array));
                        System.arraycopy(array, 0, auxArray, 0, Array.getLength(array));
                        array = Array.newInstance(method.getReturnType().getComponentType(), arrayIndex + 1);
                        System.arraycopy(auxArray, 0, array, 0, Array.getLength(auxArray));
                        Array.set(array, arrayIndex, method.getReturnType().getComponentType().newInstance());
                        object = array;
                        setParameters = new Object[] { object };
                        setParameterTypes = new Class[] { method.getReturnType() };
                        setMethodName = SETTER_PREFIX.concat(childName);
                        setMethod = c.getMethod(setMethodName, setParameterTypes);
                        setMethod.invoke(father, setParameters);
                    } else {
                        if (Array.get(array, arrayIndex) == null) {
                            Array.set(array, arrayIndex, method.getReturnType().getComponentType().newInstance());
                        }
                    }
                } catch (Exception e) {
                    logger.error("IFXMessage|createElement(" + father.toString() + "," + childName + "): " + e.getMessage() + "|");
                }
                child = Array.get(array, arrayIndex);
            } else {
                child = method.invoke(father, arguments);
            }
        } catch (NoSuchMethodException e) {
            child = null;
        } catch (IllegalAccessException e) {
            child = null;
        } catch (InvocationTargetException e) {
            child = null;
        }
        return child;
    }

    /**
	 *  Persiste el objeto en formato xml en el OutputStream especificado
	 * @param ostream - OutputStream donde se persiste el mensaje
	 */
    private void writeObject(ObjectOutputStream ostream) throws IOException {
        try {
            Document docOut = IFXDocumentHandler.build(ifx, null, IFX_URI);
            IFXDocumentHandler.write(docOut, 0, null, ostream);
        } catch (Exception e) {
            logger.error("IFXMessage|writeObject()-" + e.getMessage() + "|");
        }
    }

    private void readObject(ObjectInputStream istream) throws IOException, ClassNotFoundException {
        Map<String, String> props = new HashMap<String, String>();
        props.put(IFX_URI, "cfg/ifx/1.7/IFX170_extended.xsd");
        try {
            Document docIn = IFXDocumentHandler.read(istream, false, props);
            ifx = (IFX) IFXDocumentHandler.parse(docIn);
        } catch (IFXException e) {
            logger.error("IFXMessage|readObject()-" + e.getMessage() + "|");
        }
    }

    /**
	 *  Devuelve el mensaje en formato xml en un objeto String
	 */
    public String toString() {
        String result = null;
        try {
            Document docOut = IFXDocumentHandler.build(ifx, "ifx", IFX_URI);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult sresult = new StreamResult(new StringWriter());
            JDOMSource source = new JDOMSource(docOut);
            transformer.transform(source, sresult);
            String xmlString = sresult.getWriter().toString();
            result = xmlString;
        } catch (Exception e) {
            logger.error("IFXMessage|toString()-" + e.getMessage() + "|");
        }
        return result;
    }
}
