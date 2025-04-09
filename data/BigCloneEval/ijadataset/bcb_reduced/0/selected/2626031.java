package com.sf.ddao.alinker.factory;

import com.sf.ddao.alinker.ALinker;
import com.sf.ddao.alinker.Context;
import com.sf.ddao.alinker.Factory;
import com.sf.ddao.alinker.FactoryException;
import com.sf.ddao.alinker.inject.DependencyInjector;
import com.sf.ddao.alinker.inject.Inject;
import com.sf.ddao.utils.Annotations;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

/**
 * Created-By: Pavel Syrtsov
 * Date: Apr 10, 2008
 * Time: 9:39:39 PM
 */
public class DefaultFactory implements Factory {

    public Object create(ALinker aLinker, Context ctx) throws FactoryException {
        Class<?> clazz = ctx.getSubjClass();
        if (ALinker.class.equals(clazz)) {
            return aLinker;
        }
        ImplementedBy implementedBy = clazz.getAnnotation(ImplementedBy.class);
        if (implementedBy != null) {
            clazz = implementedBy.value();
        }
        try {
            final Constructor[] constructors = clazz.getConstructors();
            for (Constructor constructor : constructors) {
                final Annotation annotation = Annotations.findAnnotation(constructor, Inject.class);
                if (annotation != null) {
                    return DependencyInjector.injectConstructor(aLinker, constructor);
                }
            }
            return clazz.newInstance();
        } catch (Exception e) {
            throw new FactoryException("Failed to create instance of " + clazz, e);
        }
    }
}
