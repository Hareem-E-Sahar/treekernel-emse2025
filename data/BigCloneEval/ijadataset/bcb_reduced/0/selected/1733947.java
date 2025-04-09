package de.fhkl.helloWorld.implementation.model.attribute;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.w3c.dom.Element;
import de.fhkl.helloWorld.interfaces.model.attribute.Attribute;
import de.fhkl.helloWorld.interfaces.model.attribute.IAttributeInstantiator;
import de.fhkl.helloWorld.interfaces.model.attribute.InvalidClassException;

public class AttributeInstantiator implements IAttributeInstantiator {

    private Class[] proto = { String.class };

    public Attribute getInstance(String attributeClass, String key) throws InvalidClassException {
        if (!(attributeClass.contains(PACKAGE_CONSTRAINT))) {
            throw new InvalidClassException();
        }
        Attribute a;
        try {
            Class<Attribute> c = (Class<Attribute>) Class.forName(attributeClass);
            Object[] params = new Object[1];
            params[0] = key;
            try {
                Constructor con = c.getConstructor(proto);
                a = (Attribute) con.newInstance(params);
            } catch (NoSuchMethodException e) {
                a = c.newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    public Attribute getInstance(Element node) throws InvalidClassException {
        String className = node.getAttribute(CLASS_IDENTIFIER);
        String key = node.getNodeName();
        Attribute a = getInstance(className, key);
        a.parseFromXML(node);
        return a;
    }
}
