package org.tm4j.topicmap.hibernate.index;

import org.tm4j.topicmap.TopicMap;
import org.tm4j.topicmap.hibernate.TopicMapImpl;
import org.tm4j.topicmap.hibernate.TopicMapProviderImpl;
import org.tm4j.topicmap.index.Index;
import org.tm4j.topicmap.index.IndexMeta;
import org.tm4j.topicmap.index.IndexMetaImpl;
import org.tm4j.topicmap.index.IndexProvider;
import org.tm4j.topicmap.index.IndexProviderException;
import org.tm4j.topicmap.index.UnsupportedIndexException;
import org.tm4j.topicmap.index.basic.AssociationTypesIndex;
import org.tm4j.topicmap.index.basic.BaseNameDataIndex;
import org.tm4j.topicmap.index.basic.MemberTypesIndex;
import org.tm4j.topicmap.index.basic.OccurrenceDataIndex;
import org.tm4j.topicmap.index.basic.OccurrenceLocatorIndex;
import org.tm4j.topicmap.index.basic.OccurrenceTypesIndex;
import org.tm4j.topicmap.index.basic.ThemesIndex;
import org.tm4j.topicmap.index.basic.TopicTypesIndex;
import org.tm4j.topicmap.index.basic.VariantDataIndex;
import org.tm4j.topicmap.index.basic.VariantLocatorIndex;
import java.io.Serializable;
import java.lang.reflect.Constructor;

public class HibernateBasicIndexProvider implements IndexProvider, Serializable {

    protected static final String[] INTERFACE_NAMES = { AssociationTypesIndex.class.getName(), MemberTypesIndex.class.getName(), OccurrenceTypesIndex.class.getName(), TopicTypesIndex.class.getName(), OccurrenceLocatorIndex.class.getName(), ThemesIndex.class.getName(), OccurrenceDataIndex.class.getName(), BaseNameDataIndex.class.getName(), VariantDataIndex.class.getName(), VariantLocatorIndex.class.getName() };

    protected static final String[] IMPL_NAMES = { HibernateAssociationTypesIndex.class.getName(), HibernateMemberTypesIndex.class.getName(), HibernateOccurrenceTypesIndex.class.getName(), HibernateTopicTypesIndex.class.getName(), HibernateOccurrenceLocatorIndex.class.getName(), HibernateThemesIndex.class.getName(), HibernateOccurrenceDataIndex.class.getName(), HibernateBaseNameDataIndex.class.getName(), HibernateVariantDataIndex.class.getName(), HibernateVariantLocatorIndex.class.getName() };

    private Index[] indexes = new Index[INTERFACE_NAMES.length];

    private TopicMapProviderImpl m_provider;

    private TopicMapImpl m_tm;

    public HibernateBasicIndexProvider(TopicMapProviderImpl provider) {
        m_provider = provider;
    }

    public void initialise(TopicMap tm) {
        m_tm = (TopicMapImpl) tm;
    }

    public Index getIndex(String interfaceName) throws UnsupportedIndexException, IndexProviderException {
        for (int i = 0; i < INTERFACE_NAMES.length; i++) {
            if (INTERFACE_NAMES[i].equals(interfaceName)) {
                if (indexes[i] == null) {
                    try {
                        Class[] paramTypes = { TopicMapProviderImpl.class, TopicMap.class };
                        Class ixCls = Class.forName(IMPL_NAMES[i]);
                        Constructor ctor = ixCls.getConstructor(paramTypes);
                        Object[] params = { m_provider, m_tm };
                        indexes[i] = (Index) ctor.newInstance(params);
                    } catch (Exception ex) {
                        throw new IndexProviderException("Error creating index: " + interfaceName + " - " + ex.toString());
                    }
                }
                return indexes[i];
            }
        }
        throw new UnsupportedIndexException(interfaceName);
    }

    public IndexMeta getIndexMeta(String interfaceName) throws UnsupportedIndexException {
        for (int i = 0; i < INTERFACE_NAMES.length; i++) {
            if (INTERFACE_NAMES[i].equals(interfaceName)) {
                return new IndexMetaImpl(true, true);
            }
        }
        throw new UnsupportedIndexException(interfaceName);
    }

    public String[] getIndexNames() {
        return INTERFACE_NAMES;
    }
}
