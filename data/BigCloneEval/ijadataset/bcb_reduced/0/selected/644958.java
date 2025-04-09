package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.services.*;
import static java.lang.String.format;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class PropertyShadowBuilderImpl implements PropertyShadowBuilder {

    private final ClassFactory classFactory;

    private final PropertyAccess propertyAccess;

    public PropertyShadowBuilderImpl(@Builtin ClassFactory classFactory, PropertyAccess propertyAccess) {
        this.classFactory = classFactory;
        this.propertyAccess = propertyAccess;
    }

    public <T> T build(Object source, String propertyName, Class<T> propertyType) {
        Class sourceClass = source.getClass();
        PropertyAdapter adapter = propertyAccess.getAdapter(sourceClass).getPropertyAdapter(propertyName);
        if (adapter == null) throw new RuntimeException(ServiceMessages.noSuchProperty(sourceClass, propertyName));
        if (!adapter.isRead()) throw new RuntimeException(ServiceMessages.readNotSupported(source, propertyName));
        if (!propertyType.isAssignableFrom(adapter.getType())) throw new RuntimeException(ServiceMessages.propertyTypeMismatch(propertyName, sourceClass, adapter.getType(), propertyType));
        ClassFab cf = classFactory.newClass(propertyType);
        cf.addField("_source", Modifier.PRIVATE | Modifier.FINAL, sourceClass);
        cf.addConstructor(new Class[] { sourceClass }, null, "_source = $1;");
        String body = format("return _source.%s();", adapter.getReadMethod().getName());
        MethodSignature sig = new MethodSignature(propertyType, "_delegate", null, null);
        cf.addMethod(Modifier.PRIVATE, sig, body);
        String toString = format("<Shadow: property %s of %s>", propertyName, source);
        cf.proxyMethodsToDelegate(propertyType, "_delegate()", toString);
        Class shadowClass = cf.createClass();
        try {
            Constructor cc = shadowClass.getConstructors()[0];
            Object instance = cc.newInstance(source);
            return propertyType.cast(instance);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
