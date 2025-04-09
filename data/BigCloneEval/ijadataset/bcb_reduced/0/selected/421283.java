package com.michaelzanussi.genalyze.genesys.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import com.michaelzanussi.genalyze.genesys.attributes.Attribute;
import com.michaelzanussi.genalyze.genesys.messages.Message;
import com.michaelzanussi.genalyze.genesys.property.GenesysPropertyManager;
import com.michaelzanussi.genalyze.util.KeyValuePair;

/**
 * The Genesys factory, object instantiation via reflection.
 * 
 * @author <a href="mailto:admin@michaelzanussi.com">Michael Zanussi</a>
 * @version 1.0 (12 January 2009) 
 */
public class GenesysFactory extends AbstractGenesysFactory {

    public Message createMessage(String name) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (name == null) {
            throw new NullPointerException("Message name not specified.");
        }
        Constructor con = getConstructor(GenesysPropertyManager.getInstance().getMessagePackage() + "." + name);
        Message msg = (Message) con.newInstance(new Object[] { name });
        return msg;
    }

    public Attribute createAttribute(KeyValuePair kvp) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (kvp == null) {
            throw new NullPointerException("Attribute kvp not specified.");
        }
        Constructor con = getConstructor(GenesysPropertyManager.getInstance().getAttributePackage() + "." + kvp.getKey());
        Attribute attr = (Attribute) con.newInstance(new Object[] { kvp.getValue() });
        return attr;
    }
}
