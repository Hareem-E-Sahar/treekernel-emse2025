package org.openliberty.xmltooling.pp.dst2_1;

import java.lang.reflect.Constructor;
import org.openliberty.xmltooling.dst2_1.DSTDate;
import org.opensaml.xml.AbstractXMLObjectBuilder;

/**
 * This is the generic Builder for all of the classes that extend DSTDate
 * 
 * @author asa
 *
 */
public class DSTDateTypeBuilder extends AbstractXMLObjectBuilder<DSTDate> {

    @Override
    public DSTDate buildObject(String namespaceURI, String localName, String namespacePrefix) {
        try {
            Class<?> clazz = Class.forName("org.openliberty.xmltooling.pp." + localName);
            Constructor<?> constructor = clazz.getConstructor(new Class[] { String.class, String.class, String.class });
            return (DSTDate) constructor.newInstance(new Object[] { namespaceURI, localName, namespacePrefix });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new DSTDate(namespaceURI, localName, namespacePrefix);
    }
}
