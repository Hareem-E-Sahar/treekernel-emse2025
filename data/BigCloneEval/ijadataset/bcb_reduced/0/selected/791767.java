package org.hibernate.persister;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cache.access.EntityRegionAccessStrategy;
import org.hibernate.cache.access.CollectionRegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.Mapping;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.persister.collection.BasicCollectionPersister;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.OneToManyPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;

/**
 * Factory for <tt>EntityPersister</tt> and <tt>CollectionPersister</tt> instances
 *
 * @author Gavin King
 */
public final class PersisterFactory {

    private PersisterFactory() {
    }

    private static final Class[] PERSISTER_CONSTRUCTOR_ARGS = new Class[] { PersistentClass.class, EntityRegionAccessStrategy.class, SessionFactoryImplementor.class, Mapping.class };

    private static final Class[] COLLECTION_PERSISTER_CONSTRUCTOR_ARGS = new Class[] { Collection.class, CollectionRegionAccessStrategy.class, Configuration.class, SessionFactoryImplementor.class };

    public static EntityPersister createClassPersister(PersistentClass model, EntityRegionAccessStrategy cacheAccessStrategy, SessionFactoryImplementor factory, Mapping cfg) throws HibernateException {
        Class persisterClass = model.getEntityPersisterClass();
        if (persisterClass == null || persisterClass == SingleTableEntityPersister.class) {
            return new SingleTableEntityPersister(model, cacheAccessStrategy, factory, cfg);
        } else if (persisterClass == JoinedSubclassEntityPersister.class) {
            return new JoinedSubclassEntityPersister(model, cacheAccessStrategy, factory, cfg);
        } else if (persisterClass == UnionSubclassEntityPersister.class) {
            return new UnionSubclassEntityPersister(model, cacheAccessStrategy, factory, cfg);
        } else {
            return create(persisterClass, model, cacheAccessStrategy, factory, cfg);
        }
    }

    public static CollectionPersister createCollectionPersister(Configuration cfg, Collection model, CollectionRegionAccessStrategy cacheAccessStrategy, SessionFactoryImplementor factory) throws HibernateException {
        Class persisterClass = model.getCollectionPersisterClass();
        if (persisterClass == null) {
            return model.isOneToMany() ? (CollectionPersister) new OneToManyPersister(model, cacheAccessStrategy, cfg, factory) : (CollectionPersister) new BasicCollectionPersister(model, cacheAccessStrategy, cfg, factory);
        } else {
            return create(persisterClass, cfg, model, cacheAccessStrategy, factory);
        }
    }

    private static EntityPersister create(Class persisterClass, PersistentClass model, EntityRegionAccessStrategy cacheAccessStrategy, SessionFactoryImplementor factory, Mapping cfg) throws HibernateException {
        Constructor pc;
        try {
            pc = persisterClass.getConstructor(PERSISTER_CONSTRUCTOR_ARGS);
        } catch (Exception e) {
            throw new MappingException("Could not get constructor for " + persisterClass.getName(), e);
        }
        try {
            return (EntityPersister) pc.newInstance(new Object[] { model, cacheAccessStrategy, factory, cfg });
        } catch (InvocationTargetException ite) {
            Throwable e = ite.getTargetException();
            if (e instanceof HibernateException) {
                throw (HibernateException) e;
            } else {
                throw new MappingException("Could not instantiate persister " + persisterClass.getName(), e);
            }
        } catch (Exception e) {
            throw new MappingException("Could not instantiate persister " + persisterClass.getName(), e);
        }
    }

    private static CollectionPersister create(Class persisterClass, Configuration cfg, Collection model, CollectionRegionAccessStrategy cacheAccessStrategy, SessionFactoryImplementor factory) throws HibernateException {
        Constructor pc;
        try {
            pc = persisterClass.getConstructor(COLLECTION_PERSISTER_CONSTRUCTOR_ARGS);
        } catch (Exception e) {
            throw new MappingException("Could not get constructor for " + persisterClass.getName(), e);
        }
        try {
            return (CollectionPersister) pc.newInstance(new Object[] { model, cacheAccessStrategy, cfg, factory });
        } catch (InvocationTargetException ite) {
            Throwable e = ite.getTargetException();
            if (e instanceof HibernateException) {
                throw (HibernateException) e;
            } else {
                throw new MappingException("Could not instantiate collection persister " + persisterClass.getName(), e);
            }
        } catch (Exception e) {
            throw new MappingException("Could not instantiate collection persister " + persisterClass.getName(), e);
        }
    }
}
