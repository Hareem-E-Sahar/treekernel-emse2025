package net.sf.sit.factory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import net.sf.seaf.exception.SeafInstantiationException;
import net.sf.seaf.factory.*;
import net.sf.seaf.factory.impl.CachingFactory;
import net.sf.seaf.factory.impl.DefaultInstantiatingFactory;

/**
 * SIT-internal, default implementation of {@link SitFactory}.
 */
public class InternalSitFactory implements SitFactory {

    Factory cachingFactory = new CachingFactory(new ConvertorInstantiatingFactory());

    Factory instantiatingFactory = new DefaultInstantiatingFactory();

    private class ConvertorInstantiatingFactory implements Factory {

        public <Type> Type getInstanceOf(Class<Type> type) {
            try {
                Constructor<Type> constructor = type.getConstructor(SitFactory.class);
                return constructor.newInstance(InternalSitFactory.this);
            } catch (Exception e) {
                throw new SeafInstantiationException(e);
            }
        }
    }

    public <Type> Type getConvertor(Class<Type> type) {
        return cachingFactory.getInstanceOf(type);
    }

    public <Type> Type getStructure(Class<Type> type) {
        return instantiatingFactory.getInstanceOf(type);
    }

    public <Type> Collection<Type> getCollection(Class<Type> type) {
        return new ArrayList<Type>();
    }
}
