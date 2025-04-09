package org.javasock;

import java.nio.ByteBuffer;
import java.util.Enumeration;

/**
  * Package class to generate options from a registration map and a byte buffer.
  */
public class OptionEnumeration implements Enumeration {

    public OptionEnumeration(ByteBuffer bb, OptionRegistry optionMap) {
        this(bb, optionMap, null);
    }

    /**
	  * Creates an enumeration which traverses the provided byte buffer, 
	  *   creating instances of the appropriate option from the option registry.
	  *   If the option is not found in the registry, an instance of the 
	  *   defaultClass is created.  Note that the traversal occurs on calls to
	  *   {@link #nextElement()}.
	  */
    public OptionEnumeration(ByteBuffer bb, OptionRegistry optionMap, Class defaultClass) {
        buffer = bb.slice();
        this.optionMap = optionMap;
        this.defaultClass = defaultClass;
    }

    private Class getOptionClass(byte headerByte) {
        Class result = optionMap.get(new Byte(headerByte));
        if (result == null) return defaultClass;
        return result;
    }

    public boolean hasMoreElements() {
        return buffer.hasRemaining() && buffer.get(buffer.position()) != 0;
    }

    public Object nextElement() {
        try {
            byte option = buffer.get(buffer.position());
            Class optionClass = getOptionClass(option);
            if (optionClass == null) {
                System.err.println("Option " + option + " not supported!");
                return null;
            }
            Class[] args = { ByteBuffer.class };
            java.lang.reflect.Constructor cons = optionClass.getConstructor(args);
            Object[] arg = { buffer };
            return cons.newInstance(arg);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private final ByteBuffer buffer;

    private final OptionRegistry optionMap;

    private final Class defaultClass;
}
