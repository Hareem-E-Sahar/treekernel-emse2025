package ognl;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;

/**
 * Implementation of PropertyAccessor that uses numbers and dynamic subscripts as properties to
 * index into Java arrays.
 * 
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class ArrayPropertyAccessor extends ObjectPropertyAccessor implements PropertyAccessor {

    public Object getProperty(Map context, Object target, Object name) throws OgnlException {
        Object result = null;
        if (name instanceof String) {
            if (name.equals("size") || name.equals("length")) {
                result = new Integer(Array.getLength(target));
            } else {
                if (name.equals("iterator")) {
                    result = Arrays.asList(target).iterator();
                } else {
                    if (name.equals("isEmpty") || name.equals("empty")) {
                        result = Array.getLength(target) == 0 ? Boolean.TRUE : Boolean.FALSE;
                    } else {
                        result = super.getProperty(context, target, name);
                    }
                }
            }
        } else {
            Object index = name;
            if (index instanceof DynamicSubscript) {
                int len = Array.getLength(target);
                switch(((DynamicSubscript) index).getFlag()) {
                    case DynamicSubscript.ALL:
                        result = Array.newInstance(target.getClass().getComponentType(), len);
                        System.arraycopy(target, 0, result, 0, len);
                        break;
                    case DynamicSubscript.FIRST:
                        index = new Integer((len > 0) ? 0 : -1);
                        break;
                    case DynamicSubscript.MID:
                        index = new Integer((len > 0) ? (len / 2) : -1);
                        break;
                    case DynamicSubscript.LAST:
                        index = new Integer((len > 0) ? (len - 1) : -1);
                        break;
                }
            }
            if (result == null) {
                if (index instanceof Number) {
                    int i = ((Number) index).intValue();
                    result = (i >= 0) ? Array.get(target, i) : null;
                } else {
                    throw new NoSuchPropertyException(target, index);
                }
            }
        }
        return result;
    }

    public void setProperty(Map context, Object target, Object name, Object value) throws OgnlException {
        Object index = name;
        boolean isNumber = (index instanceof Number);
        if (isNumber || (index instanceof DynamicSubscript)) {
            TypeConverter converter = ((OgnlContext) context).getTypeConverter();
            Object convertedValue;
            convertedValue = converter.convertValue(context, target, null, name.toString(), value, target.getClass().getComponentType());
            if (isNumber) {
                int i = ((Number) index).intValue();
                if (i >= 0) {
                    Array.set(target, i, convertedValue);
                }
            } else {
                int len = Array.getLength(target);
                switch(((DynamicSubscript) index).getFlag()) {
                    case DynamicSubscript.ALL:
                        System.arraycopy(target, 0, convertedValue, 0, len);
                        return;
                    case DynamicSubscript.FIRST:
                        index = new Integer((len > 0) ? 0 : -1);
                        break;
                    case DynamicSubscript.MID:
                        index = new Integer((len > 0) ? (len / 2) : -1);
                        break;
                    case DynamicSubscript.LAST:
                        index = new Integer((len > 0) ? (len - 1) : -1);
                        break;
                }
            }
        } else {
            if (name instanceof String) {
                super.setProperty(context, target, name, value);
            } else {
                throw new NoSuchPropertyException(target, index);
            }
        }
    }
}
