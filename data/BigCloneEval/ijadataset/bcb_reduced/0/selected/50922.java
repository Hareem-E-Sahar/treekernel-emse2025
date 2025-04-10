package net.sourceforge.squirrel_sql.fw.xml;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.n3.nanoxml.*;

public class XMLBeanReader {

    private String[][] _fixStrings = new String[][] { { "com.bigfoot.colbell.squirrel", "net.sourceforge.squirrel_sql.client" }, { "com.bigfoot.colbell.fw", "net.sourceforge.squirrel_sql.fw" } };

    private ClassLoader _cl;

    private final List _beanColl = new ArrayList();

    public XMLBeanReader() {
        super();
    }

    public void load(File xmlFile) throws FileNotFoundException, XMLException {
        load(xmlFile, null);
    }

    public void load(File xmlFile, ClassLoader cl) throws FileNotFoundException, XMLException {
        if (!xmlFile.exists()) {
            throw new FileNotFoundException(xmlFile.getName());
        }
        load(xmlFile.getAbsolutePath(), cl);
    }

    public void load(String xmlFileName) throws FileNotFoundException, XMLException {
        load(xmlFileName, null);
    }

    public synchronized void load(String xmlFileName, ClassLoader cl) throws FileNotFoundException, XMLException {
        _cl = cl;
        _beanColl.clear();
        if (xmlFileName == null) {
            throw new XMLException("Null File name");
        }
        FileReader frdr = new FileReader(xmlFileName);
        try {
            load(frdr, cl);
        } finally {
            try {
                frdr.close();
            } catch (IOException ignore) {
            }
        }
    }

    public void load(Reader rdr) throws XMLException {
        load(rdr, null);
    }

    public void load(Reader rdr, ClassLoader cl) throws XMLException {
        try {
            final IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
            parser.setReader(new StdXMLReader(rdr));
            IXMLElement element = (IXMLElement) parser.parse();
            Enumeration en = element.enumerateChildren();
            while (en.hasMoreElements()) {
                final IXMLElement elem = (IXMLElement) en.nextElement();
                if (isBeanElement(elem)) {
                    _beanColl.add(loadBean(elem));
                }
            }
        } catch (Exception ex) {
            throw new XMLException(ex);
        }
    }

    public Iterator iterator() {
        return _beanColl.iterator();
    }

    private Object loadBean(IXMLElement beanElement) throws XMLException {
        try {
            String beanClassName = getClassNameFromElement(beanElement);
            beanClassName = fixClassName(beanClassName);
            Class beanClass = null;
            if (_cl == null) {
                beanClass = Class.forName(beanClassName);
            } else {
                beanClass = Class.forName(beanClassName, true, _cl);
            }
            Object bean = beanClass.newInstance();
            BeanInfo info = Introspector.getBeanInfo(bean.getClass(), Introspector.USE_ALL_BEANINFO);
            PropertyDescriptor[] propDesc = info.getPropertyDescriptors();
            Map props = new HashMap();
            for (int i = 0; i < propDesc.length; ++i) {
                props.put(propDesc[i].getName(), propDesc[i]);
            }
            final List children = beanElement.getChildren();
            for (Iterator it = children.iterator(); it.hasNext(); ) {
                final IXMLElement propElem = (IXMLElement) it.next();
                final PropertyDescriptor curProp = (PropertyDescriptor) props.get(propElem.getName());
                if (curProp != null) {
                    loadProperty(bean, curProp, propElem);
                }
            }
            return bean;
        } catch (Exception ex) {
            throw new XMLException(ex);
        }
    }

    private void loadProperty(Object bean, PropertyDescriptor propDescr, IXMLElement propElem) throws XMLException {
        final Method setter = propDescr.getWriteMethod();
        if (setter != null) {
            final Class parmType = setter.getParameterTypes()[0];
            final Class arrayType = parmType.getComponentType();
            final String value = propElem.getContent();
            if (isIndexedElement(propElem)) {
                Object[] data = loadIndexedProperty(bean, propDescr, propElem);
                try {
                    Object obj = Array.newInstance(arrayType, data.length);
                    System.arraycopy(data, 0, obj, 0, data.length);
                    setter.invoke(bean, new Object[] { obj });
                } catch (Exception ex) {
                    throw new XMLException(ex);
                }
            } else if (isBeanElement(propElem)) {
                Object data = loadBean(propElem);
                try {
                    setter.invoke(bean, new Object[] { data });
                } catch (Exception ex) {
                    throw new XMLException(ex);
                }
            } else if (parmType == boolean.class) {
                Object data = new Boolean(value);
                try {
                    setter.invoke(bean, new Object[] { data });
                } catch (Exception ex) {
                    throw new XMLException(ex);
                }
            } else if (parmType == int.class) {
                Object data = new Integer(value);
                try {
                    setter.invoke(bean, new Object[] { data });
                } catch (Exception ex) {
                    throw new XMLException(ex);
                }
            } else if (parmType == short.class) {
                Object data = new Short(value);
                try {
                    setter.invoke(bean, new Object[] { data });
                } catch (Exception ex) {
                    throw new XMLException(ex);
                }
            } else if (parmType == long.class) {
                Object data = new Long(value);
                try {
                    setter.invoke(bean, new Object[] { data });
                } catch (Exception ex) {
                    throw new XMLException(ex);
                }
            } else if (parmType == float.class) {
                Object data = new Float(value);
                try {
                    setter.invoke(bean, new Object[] { data });
                } catch (Exception ex) {
                    throw new XMLException(ex);
                }
            } else if (parmType == double.class) {
                Object data = new Double(value);
                try {
                    setter.invoke(bean, new Object[] { data });
                } catch (Exception ex) {
                    throw new XMLException(ex);
                }
            } else if (parmType == char.class) {
                Object data;
                if (value != null && value.length() > 0) {
                    data = new Character(value.charAt(0));
                } else {
                    data = new Character(' ');
                }
                try {
                    setter.invoke(bean, new Object[] { data });
                } catch (Exception ex) {
                    throw new XMLException(ex);
                }
            } else {
                Object data = value;
                try {
                    setter.invoke(bean, new Object[] { data });
                } catch (Exception ex) {
                    throw new XMLException(ex);
                }
            }
        }
    }

    private Object[] loadIndexedProperty(Object bean, PropertyDescriptor propDescr, IXMLElement beanElement) throws XMLException {
        final List beans = new ArrayList();
        final String propName = beanElement.getName();
        final List children = beanElement.getChildren();
        for (Iterator it = children.iterator(); it.hasNext(); ) {
            beans.add(loadBean((IXMLElement) it.next()));
        }
        return beans.toArray(new Object[beans.size()]);
    }

    private boolean isBeanElement(IXMLElement elem) {
        return elem.getAttribute(XMLConstants.CLASS_ATTRIBUTE_NAME, null) != null;
    }

    private boolean isIndexedElement(IXMLElement elem) {
        String att = elem.getAttribute(XMLConstants.INDEXED, "false");
        return att != null && att.equals("true");
    }

    private String getClassNameFromElement(IXMLElement elem) {
        return elem.getAttribute(XMLConstants.CLASS_ATTRIBUTE_NAME, null);
    }

    private String fixClassName(String className) {
        for (int i = 0; i < _fixStrings.length; ++i) {
            String from = _fixStrings[i][0];
            if (className.startsWith(from)) {
                className = _fixStrings[i][1] + className.substring(from.length());
                break;
            }
        }
        return className;
    }
}
