package com.risertech.xdav.internal.webdav.xml.element;

import java.lang.reflect.Constructor;
import org.jdom.Element;
import com.risertech.xdav.internal.webdav.IPropertyUpdate;
import com.risertech.xdav.internal.webdav.xml.WebDAVConverter;
import com.risertech.xdav.internal.webdav.xml.XMLConverterRegistry;
import com.risertech.xdav.webdav.element.Prop;

public abstract class AbstractPropertyUpdateConverter extends WebDAVConverter {

    public AbstractPropertyUpdateConverter(Class<?> clazz, String elementName) {
        super(clazz, elementName);
    }

    @Override
    protected Element doCreateElement(Object object) {
        Element element = createElement();
        element.addContent(XMLConverterRegistry.getElement(((IPropertyUpdate) object).getProp()));
        return element;
    }

    @Override
    protected Object doCreateObject(Element element) {
        try {
            Constructor<?> constructor = getClassType().getConstructor(Prop.class);
            return constructor.newInstance(XMLConverterRegistry.getObject(element.getChild("prop")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
