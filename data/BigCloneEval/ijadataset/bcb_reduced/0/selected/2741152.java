package org.tm4j.topicmap.ozone.index;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ozoneDB.OzoneObject;
import org.tm4j.topicmap.TopicMap;
import org.tm4j.topicmap.index.Index;
import org.tm4j.topicmap.index.IndexMeta;
import org.tm4j.topicmap.index.IndexMetaImpl;
import org.tm4j.topicmap.index.IndexProviderException;
import org.tm4j.topicmap.index.UnsupportedIndexException;
import java.io.Externalizable;
import java.lang.reflect.Constructor;

public abstract class OzoneIndexProviderImpl extends OzoneObject implements OzoneIndexProvider, Externalizable {

    private static final Log m_log = LogFactory.getLog("org.tm4j.index");

    private static final long serialVersionUID = 1L;

    private static final long subSerialVersionUID = 2L;

    protected String[] INTERFACE_NAMES;

    protected String[] IMPL_NAMES;

    protected IndexMeta[] IMPL_META;

    private Index[] indexes;

    private TopicMap m_tm;

    public OzoneIndexProviderImpl() {
        INTERFACE_NAMES = getInterfaceNames();
        IMPL_NAMES = getImplNames();
        IMPL_META = getImplMeta();
        indexes = new Index[INTERFACE_NAMES.length];
    }

    public abstract String[] getInterfaceNames();

    public abstract String[] getImplNames();

    public abstract IndexMeta[] getImplMeta();

    public void initialise(TopicMap tm) throws IndexProviderException {
        m_tm = tm;
    }

    public Index getIndex(String interfaceName) throws UnsupportedIndexException, IndexProviderException {
        m_log.debug("Looking for: " + interfaceName);
        for (int i = 0; i < INTERFACE_NAMES.length; i++) {
            m_log.debug("  Have: " + INTERFACE_NAMES[i]);
            if (INTERFACE_NAMES[i].equals(interfaceName)) {
                if (indexes[i] == null) {
                    try {
                        Class ixCls = Class.forName(IMPL_NAMES[i]);
                        m_log.debug("Implementation class is: " + ixCls.getName());
                        if (OzoneObject.class.isAssignableFrom(ixCls)) {
                            m_log.debug("Create index as OzoneObject...");
                            indexes[i] = (OzoneIndex) database().createObject(ixCls.getName());
                            ((OzoneIndex) indexes[i]).initialise(m_tm);
                        } else {
                            m_log.debug("Create index as standard object.");
                            Class[] paramTypes = { TopicMap.class };
                            Constructor ctor = ixCls.getConstructor(paramTypes);
                            Object[] params = { m_tm };
                            indexes[i] = (Index) ctor.newInstance(params);
                        }
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
                return IMPL_META[i];
            }
        }
        throw new UnsupportedIndexException(interfaceName);
    }

    public String[] getIndexNames() {
        return INTERFACE_NAMES;
    }

    public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {
        out.writeLong(subSerialVersionUID);
        writeArray(INTERFACE_NAMES, out);
        writeArray(IMPL_NAMES, out);
        writeArray(IMPL_META, out);
        writeArray(indexes, out);
        out.writeObject(m_tm);
    }

    public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {
        long version = in.readLong();
        if (version == 1L) {
            int len = in.readInt();
            INTERFACE_NAMES = new String[len];
            for (int i = 0; i < len; i++) {
                INTERFACE_NAMES[i] = (String) in.readObject();
            }
            len = in.readInt();
            IMPL_NAMES = new String[len];
            for (int i = 0; i < len; i++) {
                IMPL_NAMES[i] = (String) in.readObject();
            }
            IMPL_META = new IndexMeta[len];
            for (int i = 0; i < len; i++) {
                IMPL_META[i] = new IndexMetaImpl(false, true);
            }
            len = in.readInt();
            indexes = new Index[len];
            for (int i = 0; i < len; i++) {
                indexes[i] = (Index) in.readObject();
            }
        } else if (version == 2L) {
            int len = in.readInt();
            INTERFACE_NAMES = new String[len];
            for (int i = 0; i < len; i++) {
                INTERFACE_NAMES[i] = (String) in.readObject();
            }
            len = in.readInt();
            IMPL_NAMES = new String[len];
            for (int i = 0; i < len; i++) {
                IMPL_NAMES[i] = (String) in.readObject();
            }
            len = in.readInt();
            IMPL_META = new IndexMeta[len];
            for (int i = 0; i < len; i++) {
                IMPL_META[i] = (IndexMeta) in.readObject();
            }
            len = in.readInt();
            indexes = new Index[len];
            for (int i = 0; i < len; i++) {
                indexes[i] = (Index) in.readObject();
            }
        }
    }

    protected void writeArray(Object[] arry, java.io.ObjectOutput out) throws java.io.IOException {
        out.writeInt(arry.length);
        for (int i = 0; i < arry.length; i++) {
            out.writeObject(arry[i]);
        }
    }
}
