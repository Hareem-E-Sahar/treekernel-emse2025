package org.nextframework.bean;

import java.lang.reflect.Constructor;
import org.nextframework.bean.editors.ValueBasedPropertyEditor;

/**
 * @author rogelgarcia
 * @since 23/01/2006
 * @version 1.1
 */
public class IndexValueResolverImpl implements IndexValueResolver {

    @SuppressWarnings("unchecked")
    public <E> E resolveName(String index, Class<E> requiredClass) {
        if (index.equals("<null>") || index.matches("\\w*((\\.\\w*)*)\\[((.)*)\\]")) {
            ValueBasedPropertyEditor propertyEditor = new ValueBasedPropertyEditor();
            propertyEditor.setAsText(index);
            return (E) propertyEditor.getValue();
        }
        if (Number.class.isAssignableFrom(requiredClass)) {
            try {
                Constructor constructor = requiredClass.getConstructor(String.class);
                Object number = constructor.newInstance(index);
                return (E) number;
            } catch (Exception e) {
            }
        }
        if (Object.class.isAssignableFrom(requiredClass)) {
            if ("d".equals(index)) {
                return (E) new Integer(0);
            }
            if ("f".equals(index)) {
                return (E) new Integer(1);
            }
        }
        if (String.class.isAssignableFrom(requiredClass)) {
            return (E) index;
        }
        return null;
    }
}
