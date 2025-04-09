package javisynth.filterdef;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javisynth.PClip;

/**
 * Defines a parameter of a filter.
 * 
 * @author Chris Ennis (chris1ennis@gmail)
 * 
 * */
public class ParameterDef<T> implements Named {

    /**
    * Map of the supported types in avisynth. The keys to this map are the characters used to define
    * the types in the filter definitions of AviSynth.
    */
    public static final Map<Character, Class<?>> parameterTypeMap = new HashMap<Character, Class<?>>();

    public static final Map<String, Class<?>> xmlParameterTypeMap = new HashMap<String, Class<?>>();

    /**
    * The reverse of the paramterTypeMap.
    * 
    * @see parameterTypeMap
    */
    public static final Map<Class<?>, Character> reverseParameterTypeMap = new HashMap<Class<?>, Character>();

    public static final Map<Class<?>, String> xmlReverseParameterTypeMap = new HashMap<Class<?>, String>();

    {
        parameterTypeMap.put('b', Boolean.class);
        parameterTypeMap.put('i', Integer.class);
        parameterTypeMap.put('f', Float.class);
        parameterTypeMap.put('s', String.class);
        parameterTypeMap.put('.', Object.class);
        parameterTypeMap.put('c', PClip.class);
        xmlParameterTypeMap.put("bool", Boolean.class);
        xmlParameterTypeMap.put("int", Integer.class);
        xmlParameterTypeMap.put("float", Float.class);
        xmlParameterTypeMap.put("string", String.class);
        xmlParameterTypeMap.put("variant", Object.class);
        xmlParameterTypeMap.put("clip", PClip.class);
        for (Character c : parameterTypeMap.keySet()) {
            reverseParameterTypeMap.put(parameterTypeMap.get(c), c);
        }
        for (String c : xmlParameterTypeMap.keySet()) {
            xmlReverseParameterTypeMap.put(xmlParameterTypeMap.get(c), c);
        }
        reverseParameterTypeMap.put(FilterClip.class, 'c');
    }

    /** Name of the parameter as returned by AviSynthWrapper.getFunctionParameters(). */
    private final String name;

    /**
    * The class representing the type of this parameter.
    * <p>
    * The Types supported by AviSynthWrapper are as follows:
    * <ul>
    * <li>Integer <=> int
    * <li>Float <=> float
    * <li>String <=> char*
    * <li>FilterClip <=> PClip (TBD) note at runtime FilterClip(s) values are PClip(s)
    * <li>Boolean <=> bool
    * </ul>
    */
    private final Class<T> type;

    /**
    * The default value for this parameter. Note that if it is specified here, then the default
    * value will be provided to the filter preventing it from supplying the default value.
    */
    private final T defaultValue;

    /** Minimum value for the parameter value. */
    private final T min;

    /** Maximum value for the parameter value. */
    private final T max;

    private final String step;

    /**
    * Set to true if the parameter is optional. If the default value is specified in this class it
    * will be specified. Otherwise it will be left empty and if the plugin really requires a value
    * then an error will occur.
    */
    private final boolean optional;

    private final boolean paramlist;

    /** Set of all values that the parameter can be set to. */
    private final List<T> items;

    /** Constructor accepting the necessary information to define a paramer and its valid values. */
    public ParameterDef(String name, Class<T> type, T defaultValue, T min, T max, String step, boolean optional, boolean paramlist, List<T> items) throws InvalidParameter {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.min = min;
        this.max = max;
        this.step = step;
        this.optional = optional;
        this.paramlist = paramlist;
        this.items = new ArrayList<T>(items);
    }

    /**
    * Returns the type class for this paramter. See the comment for ParameterDef.type for a
    * description of supported types.
    * 
    * @return
    */
    public Class<T> getType() {
        return type;
    }

    public boolean isClip() {
        return getType().equals(FilterClip.class);
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    public String getStep() {
        return step;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isParamlist() {
        return paramlist;
    }

    public List<T> getItems() {
        return items;
    }

    @Override
    public String getName() {
        return name;
    }

    public String toString() {
        return "Parameter " + name + " [" + items.size() + "] " + type.getSimpleName();
    }

    String toLongString(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("Parameter [");
        sb.append("   name: " + name);
        sb.append(", ");
        sb.append("   type: " + type);
        sb.append(", ");
        sb.append("   default: " + defaultValue);
        sb.append(", ");
        sb.append("   min: " + min);
        sb.append(", ");
        sb.append("   max: " + max);
        sb.append(", ");
        sb.append("   step: " + step);
        sb.append(", ");
        sb.append("   optional: " + optional);
        sb.append(", ");
        sb.append("   parmlist: " + paramlist);
        sb.append(", ");
        sb.append(items.toString());
        return sb.toString();
    }

    /** Returns true if the value supplied can be converted to a the type defined by this parameter. */
    public boolean isValid(String value) {
        return whyInvalid(value) == null;
    }

    /** Returns the reason the value supplied is invalid or null if it is valid. */
    public String whyInvalid(String value) {
        try {
            getValue(value);
        } catch (InvalidParameter e) {
            return e.getMessage();
        }
        return null;
    }

    /**
    * Attempts to convert a string value to an object of the type defined in this class. If the
    * conversion fails, an InvalidParameter excepiton is thrown explaining the reason the conversion
    * failed.
    * 
    * @param v
    * @return
    * @throws InvalidParameter
    */
    @SuppressWarnings("unchecked")
    public Object getValue(String v) throws InvalidParameter {
        if (v == null) {
            if (this.isOptional()) {
                return null;
            } else if (this.getDefaultValue() != null) {
                return this.getDefaultValue();
            } else {
                throw new InvalidParameter(getName() + " is not optional");
            }
        }
        try {
            Constructor constructor = type.getConstructor(String.class);
            Object result = constructor.newInstance(v);
            if (min != null && !min.equals(max) && ((Comparable) min).compareTo(result) >= 0) {
                throw new InvalidParameter(getName() + " must be greater than " + min);
            } else if (max != null && !max.equals(min) && ((Comparable) max).compareTo(result) <= 0) {
                throw new InvalidParameter(getName() + " must be less than " + max);
            } else if (getItems().size() > 0) {
                if (!getItems().contains(result)) {
                    throw new InvalidParameter(getName() + " must be one of " + getItems());
                }
            }
            return result;
        } catch (Exception e) {
            throw new InvalidParameter("The value '" + v + "' is not valid for " + getName() + " : " + e.getMessage(), e);
        }
    }

    /** Exception for notifying callers of invalid parameter definitions. */
    public static class InvalidParameter extends Exception {

        public InvalidParameter(String message) {
            super(message);
        }

        public InvalidParameter(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
