package org.simplx.args.advanced;

import org.simplx.args.CommandLine;
import org.simplx.args.CommandOpt;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

/**
 * This class implements the standard mapping for array options. If you are
 * writing a custom mapper, you may find it useful to extend this; see {@link
 * MapperAdaptor} for an overview of why you would or would not write custom
 * mappers.
 */
public class ArrayMapper extends MapperAdaptor {

    /** Creates a new array mapper. */
    public ArrayMapper() {
    }

    /**
     * Adds the values in the strong to the array in the field, creating it if
     * need be.  The string is spilt into values using {@link
     * CommandLineAdvanced#getValueSplitter()}. If the result is a single empty
     * string, then the value string is interepreted as a zero-length array.
     * Otherwise, each element in the split-up string is passed to the mapper
     * for the field array's component type.
     * <p/>
     * Nested arrays are not supported.
     */
    @Override
    public Object map(CharSequence valStr, Field field, Class type, CommandOpt anno, CommandLine line) throws IllegalAccessException {
        CommandLineAdvanced adv = line.getAdvancedKnobs();
        Class compType = type.getComponentType();
        if (compType.isArray()) {
            throw new IllegalArgumentException("Multi-dimensional array fields not supported");
        }
        Object curArray = field.get(line.getHolder());
        Pattern p;
        String pat = anno.mode();
        if (pat.length() > 0) p = Pattern.compile(pat); else p = adv.getValueSplitter();
        String[] vals = p.split(valStr);
        if (vals.length == 1 && vals[0].equals("")) vals = new String[0];
        int curLength;
        if (curArray == null) {
            curLength = 0;
        } else {
            curLength = Array.getLength(curArray);
        }
        Object newArray = Array.newInstance(compType, curLength + vals.length);
        if (curLength > 0) {
            System.arraycopy(curArray, 0, newArray, 0, curLength);
        }
        for (int i = 0; i < vals.length; i++) {
            String val = vals[i];
            Object valObj = adv.getValueFor(val, compType, anno);
            Array.set(newArray, curLength + i, valObj);
        }
        return newArray;
    }

    /**
     * Returns the {@link Mapper#defaultValueName)} for the component type,
     * followed by the value splitter display string, followed by
     * <tt>"..."</tt>
     */
    @Override
    public CharSequence defaultValueName(Field field, Class type, CommandOpt anno, CommandLine line) {
        CommandLineAdvanced adv = line.getAdvancedKnobs();
        Class elemType = type.getComponentType();
        Mapper mapper = adv.getMapperFor(elemType);
        return mapper.defaultValueName(null, elemType, null, line) + adv.getValueSplitterDisplay() + "...";
    }
}
