package org.lightframework.mvc.internal.ognl;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.lightframework.mvc.binding.BindingException;
import org.lightframework.mvc.exceptions.InvalidFormatException;
import org.lightframework.mvc.internal.convert.Converter;
import org.lightframework.mvc.internal.reflect.ReflectField;
import org.lightframework.mvc.internal.reflect.ReflectType;
import org.lightframework.mvc.internal.reflect.ReflectUtils;

/**
 * simple object graph navigation lanauge parser
 * 
 * @author fenghm (fenghm@bingosoft.net)
 * 
 * @since 1.0.0
 */
public final class SimpleOgnl {

    private static final Map<String, SoftReference<OGNode[]>> cache = new ConcurrentHashMap<String, SoftReference<OGNode[]>>();

    public static void bindingField(Object parent, String name, Object param) {
        bindingField(parent, null, null, name, param);
    }

    @SuppressWarnings("unchecked")
    private static Object bindingField(Object parent, Object value, Class<?> componentType, String name, Object param) {
        ReflectField field = null;
        Object top = null != value ? value : parent;
        OGNode[] nodes = parse(name);
        if (null != nodes) {
            boolean match = true;
            int len = nodes.length;
            for (int i = 0; i < len; i++) {
                OGNode node = nodes[i];
                if (null != parent) {
                    ReflectType parentType = ReflectType.get(parent.getClass());
                    field = parentType.findField(node.prop);
                    if (null != field) {
                        if (!node.isArray() && i == len - 1) {
                            try {
                                field.setValue(parent, Converter.convert(field.getType(), field.getGenericType(), param));
                                break;
                            } catch (Exception e) {
                                throw new BindingException("binding field '" + parentType.getActualType().getName() + "." + field.getName() + "' error", e);
                            }
                        } else {
                            value = field.getValue(parent);
                            if (null == value) {
                                value = Converter.defaultInstance(field.getType());
                                if (null != value) {
                                    field.setValue(parent, value);
                                }
                            }
                        }
                    } else {
                        match = false;
                    }
                }
                if (match) {
                    if (node.index != null) {
                        if (value instanceof List) {
                            parent = value;
                            Class<?> elementType = null != field ? ReflectUtils.getActualType(field.getGenericType()) : componentType;
                            List list = (List) value;
                            if (node.index > list.size()) {
                                for (int j = list.size(); j < node.index; j++) {
                                    list.add(Converter.defaultValue(elementType));
                                }
                            }
                            if (i == len - 1) {
                                if (node.index >= list.size()) {
                                    list.add(Converter.convert(elementType, param));
                                } else {
                                    list.set(node.index, Converter.convert(elementType, param));
                                }
                            } else {
                                Object itemValue = node.index < list.size() ? list.get(node.index) : null;
                                if (null == itemValue) {
                                    itemValue = Converter.defaultInstance(elementType);
                                    if (node.index >= list.size()) {
                                        list.add(itemValue);
                                    } else {
                                        list.set(node.index, itemValue);
                                    }
                                }
                                parent = itemValue;
                            }
                        } else if (value.getClass().isArray()) {
                            componentType = value.getClass().getComponentType();
                            int arrayLength = Array.getLength(value);
                            if (node.index >= arrayLength) {
                                Object newArray = Array.newInstance(componentType, node.index + 1);
                                if (arrayLength > 0) {
                                    System.arraycopy(value, 0, newArray, 0, arrayLength);
                                }
                                value = newArray;
                                if (null != field) {
                                    field.setValue(parent, value);
                                } else if (i == 0) {
                                    top = value;
                                }
                                arrayLength = node.index + 1;
                            }
                            parent = value;
                            if (i == len - 1) {
                                Array.set(value, node.index, Converter.convert(componentType, param));
                            } else {
                                Object itemValue = node.index < arrayLength ? Array.get(value, node.index) : null;
                                if (null == itemValue) {
                                    itemValue = Converter.defaultInstance(componentType);
                                    Array.set(value, node.index, itemValue);
                                }
                                parent = itemValue;
                            }
                        }
                    } else if (node.name != null) {
                        parent = value;
                        if (value instanceof Map) {
                            Type[] genericTypes = ReflectUtils.getActualTypes(field.getGenericType());
                            Class<?> keyType = ReflectUtils.getActualType(genericTypes[0]);
                            Class<?> valueType = ReflectUtils.getActualType(genericTypes[1]);
                            Map propMap = (Map) value;
                            Object keyValue = Converter.convert(keyType, node.name);
                            if (i == len - 1) {
                                propMap.put(keyValue, Converter.convert(valueType, param));
                            } else {
                                parent = propMap.get(keyValue);
                                if (null == parent) {
                                    parent = Converter.defaultInstance(valueType);
                                    propMap.put(Converter.convert(keyType, node.name), parent);
                                }
                            }
                        } else {
                            ReflectType propType = ReflectType.get(field.getType());
                            ReflectField propField = propType.findField(node.name);
                            if (i == len - 1) {
                                if (null != propField) {
                                    propField.setValue(value, Converter.convert(propField.getType(), propField.getGenericType(), param));
                                }
                            } else {
                                if (null == propField) {
                                    break;
                                } else {
                                    parent = Converter.convert(propField.getType(), propField.getGenericType(), param);
                                }
                            }
                        }
                    } else if (i == len - 2) {
                        parent = value;
                        OGNode last = nodes[++i];
                        field = ReflectType.get(parent.getClass()).findField(last.prop);
                        if (null != field) {
                            field.setValue(parent, Converter.convert(field.getType(), field.getGenericType(), param));
                        }
                    } else {
                        parent = value;
                    }
                } else {
                    break;
                }
            }
        }
        return top;
    }

    public static Object bindingArrayOrList(String name, Class<?> type, Class<?> componentType, Map<String, Object> params) {
        Object value = Converter.defaultInstance(type);
        for (Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object param = entry.getValue();
            if (key.toLowerCase().startsWith(name.toLowerCase())) {
                value = bindingField(null, value, componentType, key, param);
            }
        }
        return value;
    }

    public static OGNode[] parse(String expression) {
        OGNode[] parsed = null;
        SoftReference<OGNode[]> ref = cache.get(expression);
        if (null != ref) {
            parsed = ref.get();
        } else {
            ref = null;
        }
        if (null == parsed) {
            char[] chars = expression.toCharArray();
            if (!isExpression(chars)) {
                return null;
            }
            parsed = compile(chars);
            cache.put(expression, new SoftReference<OGNode[]>(parsed));
        }
        return parsed;
    }

    public static String deparse(OGNode[] parsed) {
        StringBuilder buf = new StringBuilder();
        int len = parsed.length;
        for (int i = 0; i < len; i++) {
            OGNode item = parsed[i];
            buf.append(item.prop);
            if (item.index != null) {
                buf.append('[').append(item.index).append(']');
            } else if (item.name != null) {
                buf.append('[').append(item.name).append(']');
            }
            if (i < len - 1) {
                buf.append('.');
            }
        }
        return buf.toString();
    }

    private static boolean isExpression(char[] chars) {
        int len = chars.length;
        for (int i = 0; i < len; i++) {
            char c = chars[i];
            if (c == '.' && i > 0) {
                return true;
            } else if (c == '[') {
                while (++i < len) {
                    c = chars[i];
                    if (c == ']') {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static OGNode[] compile(char[] chars) {
        List<Object> items = new ArrayList<Object>();
        StringBuilder buf = new StringBuilder();
        int len = chars.length;
        for (int i = 0; i < len; i++) {
            char c = chars[i];
            if (c == '.') {
                if (buf.length() == 0) {
                    if (items.isEmpty()) {
                        throw new InvalidFormatException("invalid char '" + c + "' at position " + i);
                    } else {
                        continue;
                    }
                }
                items.add(new OGNode(buf.toString()));
                buf.delete(0, buf.length());
            } else if (c == '[') {
                if (buf.length() == 0) {
                    throw new InvalidFormatException("invalid char '" + c + "' at position " + i);
                }
                String property = buf.toString();
                String value = null;
                buf.delete(0, buf.length());
                while (++i < len) {
                    c = chars[i];
                    if (c == ']') {
                        value = buf.toString();
                        buf.delete(0, buf.length());
                        break;
                    }
                    buf.append(c);
                }
                if (null == value || value.length() == 0) {
                    throw new InvalidFormatException("invalid char '" + c + "' at position " + i);
                } else {
                    OGNode item = new OGNode(property);
                    item.prop = property;
                    try {
                        item.index = Integer.parseInt(value);
                    } catch (Exception e) {
                        if (value.startsWith("'") && value.endsWith("'")) {
                            item.name = value.substring(1, value.length() - 1);
                        } else {
                            item.name = value;
                        }
                    }
                    items.add(item);
                }
            } else {
                buf.append(c);
            }
        }
        if (buf.length() > 0) {
            items.add(new OGNode(buf.toString()));
        }
        return items.toArray(new OGNode[] {});
    }

    public static final class OGNode {

        public String prop;

        public Integer index;

        public String name;

        private OGNode(String prop) {
            this.prop = prop;
        }

        public boolean isArray() {
            return index != null || name != null;
        }
    }
}
