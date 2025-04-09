package org.xfeep.asura.bootstrap.config;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.w3c.dom.Node;
import org.xfeep.asura.core.config.LazyResolvedConfigMap;
import org.xfeep.asura.core.match.Matcher;
import com.sun.xml.internal.bind.api.JAXBRIContext;

/**
 * original from package org.xfeep.asura.core.config
 * @author zhang yuexiang
 *
 */
public class ConfigLazyMap implements LazyResolvedConfigMap {

    protected Map<String, Object> delegate;

    protected String configId;

    public static Class<?>[] listStringItemTypes = { String.class };

    public static Class<?>[] mapStringItemTypes = { String.class, String.class };

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public Map<String, Object> getDelegate() {
        return delegate;
    }

    public void setDelegate(Map<String, Object> delegate) {
        this.delegate = delegate;
    }

    JAXBContext jaxbContext;

    public ConfigLazyMap() {
    }

    public ConfigLazyMap(Map<String, Object> delegate, JAXBContext jaxbContext) {
        super();
        this.delegate = delegate;
        this.jaxbContext = jaxbContext;
    }

    public boolean isLazy() {
        return jaxbContext != null;
    }

    public Object getLazyResolvableProperty(String key, Class<?> type, Class<?>[] itemTypes) {
        if (isLazy()) {
            Object v = delegate.get(key);
            if (v == null) {
                return null;
            }
            if (v instanceof Node) {
                Node n = (Node) v;
                synchronized (n) {
                    v = delegate.get(key);
                    if (v instanceof Node) {
                        try {
                            if (List.class.isAssignableFrom(type)) {
                                Map<String, Object> jaxbConfig = new HashMap<String, Object>();
                                if (itemTypes == null) {
                                    itemTypes = listStringItemTypes;
                                }
                                jaxbConfig.put(JAXBRIContext.ANNOTATION_READER, new JaxbDirectListAnnotationReader(itemTypes[0]));
                                JAXBContext jaxbContextTmp = JAXBContext.newInstance(new Class[] { DirectListJaxbWrapper.class }, jaxbConfig);
                                v = jaxbContextTmp.createUnmarshaller().unmarshal(n, DirectListJaxbWrapper.class).getValue().list;
                            } else if (Map.class.isAssignableFrom(type)) {
                                if (itemTypes == null) {
                                    itemTypes = mapStringItemTypes;
                                }
                                Map<String, Object> jaxbConfig = new HashMap<String, Object>();
                                jaxbConfig.put(JAXBRIContext.ANNOTATION_READER, new JaxbDirectMapAnnotationReader(itemTypes[0], itemTypes[1]));
                                JAXBContext jaxbContextTmp = JAXBContext.newInstance(new Class[] { DirectMapJaxbWrapper.class }, jaxbConfig);
                                DirectMapJaxbWrapper dm = jaxbContextTmp.createUnmarshaller().unmarshal(n, DirectMapJaxbWrapper.class).getValue();
                                if (dm != null) {
                                    v = dm.getMap(type);
                                } else {
                                    v = null;
                                }
                            } else if (type.isArray()) {
                                if (itemTypes == null) {
                                    itemTypes = listStringItemTypes;
                                }
                                Map<String, Object> jaxbConfig = new HashMap<String, Object>();
                                jaxbConfig.put(JAXBRIContext.ANNOTATION_READER, new JaxbDirectListAnnotationReader(itemTypes[0]));
                                JAXBContext jaxbContextTmp = JAXBContext.newInstance(new Class[] { DirectListJaxbWrapper.class }, jaxbConfig);
                                DirectListJaxbWrapper dl = jaxbContextTmp.createUnmarshaller().unmarshal(n, DirectListJaxbWrapper.class).getValue();
                                if (dl != null && dl.list != null) {
                                    v = Array.newInstance(type.getComponentType(), dl.list.size());
                                    System.arraycopy(dl.list.toArray(), 0, v, 0, dl.list.size());
                                } else {
                                    v = null;
                                }
                            } else {
                                v = jaxbContext.createUnmarshaller().unmarshal(n, type).getValue();
                            }
                        } catch (JAXBException e) {
                            throw new IllegalArgumentException("can not parse config property : " + key + " whose type is" + type, e);
                        }
                        delegate.put(key, v);
                        return v;
                    } else {
                        return v;
                    }
                }
            } else {
                return v;
            }
        } else {
            return delegate.get(key);
        }
    }

    public void clear() {
        delegate.clear();
    }

    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return delegate.entrySet();
    }

    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    public Object get(Object key) {
        if (Matcher.ON_DEMAND_CONFIG_ID.equals(key)) {
            return configId;
        }
        return delegate.get(key);
    }

    public int hashCode() {
        return delegate.hashCode();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public Set<String> keySet() {
        return delegate.keySet();
    }

    public Object put(String key, Object value) {
        return delegate.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends Object> m) {
        delegate.putAll(m);
    }

    public Object remove(Object key) {
        return delegate.remove(key);
    }

    public int size() {
        return delegate.size();
    }

    public Collection<Object> values() {
        return delegate.values();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
