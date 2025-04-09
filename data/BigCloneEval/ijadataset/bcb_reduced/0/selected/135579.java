package org.jaffa.beans.moulding.mapping;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jaffa.beans.moulding.data.domain.DomainDAO;
import org.jaffa.beans.moulding.mapping.GraphMapping;
import org.jaffa.beans.moulding.mapping.MappingFilter;
import org.jaffa.datatypes.DataTypeMapper;
import org.jaffa.datatypes.exceptions.InvalidForeignKeyException;
import org.jaffa.exceptions.ApplicationException;
import org.jaffa.exceptions.ApplicationExceptions;
import org.jaffa.exceptions.DomainObjectNotFoundException;
import org.jaffa.exceptions.FrameworkException;
import org.jaffa.exceptions.MultipleDomainObjectsFoundException;
import org.jaffa.persistence.Criteria;
import org.jaffa.persistence.IPersistent;
import org.jaffa.persistence.Persistent;
import org.jaffa.persistence.UOW;
import org.jaffa.persistence.util.PersistentHelper;
import org.jaffa.util.StringHelper;

/** Bean Moudler is used to mapp data between two Java Beans via a mapping file.
 * It has been specifcially coded to map between benas that extend/implement
 * DomainDAO and IPersistent for marshalling data to and from the database.
 *
 * @author  PaulE
 * @version 1.0
 *
 * @todo - Switch to use org.jaffa.datatypes.DataTypeMapper instead of org.jaffa.beans.moulding.mapping.DataTypeMapping
 */
public class BeanMoulder {

    private static Logger log = Logger.getLogger(BeanMoulder.class);

    /**
     * Display the properties of this JavaBean. If this bean has properties that implement
     * either DomainDAO or DomainDAO[], then also print this objects too.
     * @param source Javabean who's contents should be printed
     * @return multi-line string of this beans properties and their values
     */
    public static String printBean(Object source) {
        return printBean(source, null);
    }

    /**
     * Same as printBean(Object source), except the objectStack lists all the parent
     * objects its printed, and if this is one of them, it stops. This allows detection
     * of possible infinite recusion.
     * @param source Javabean who's contents should be printed
     * @param objectStack List of objects already traversed
     * @return multi-line string of this beans properties and their values
     */
    public static String printBean(Object source, List objectStack) {
        if (source == null) return null;
        if (objectStack != null) if (objectStack.contains(source)) return "Object Already Used. " + source.getClass().getName() + "@" + source.hashCode(); else objectStack.add(source); else {
            objectStack = new ArrayList();
            objectStack.add(source);
        }
        StringBuffer out = new StringBuffer();
        out.append(source.getClass().getName());
        out.append("\n");
        try {
            BeanInfo sInfo = Introspector.getBeanInfo(source.getClass());
            PropertyDescriptor[] sDescriptors = sInfo.getPropertyDescriptors();
            if (sDescriptors != null && sDescriptors.length != 0) for (int i = 0; i < sDescriptors.length; i++) {
                PropertyDescriptor sDesc = sDescriptors[i];
                Method sm = sDesc.getReadMethod();
                if (sm != null && sDesc.getWriteMethod() != null) {
                    if (!sm.isAccessible()) sm.setAccessible(true);
                    Object sValue = sm.invoke(source, (Object[]) null);
                    out.append("  ");
                    out.append(sDesc.getName());
                    if (source instanceof DomainDAO) {
                        if (((DomainDAO) source).hasChanged(sDesc.getName())) out.append("*");
                    }
                    out.append("=");
                    if (sValue == null) out.append("<--NULL-->\n"); else if (sm.getReturnType().isArray()) {
                        StringBuffer out2 = new StringBuffer();
                        out2.append("Array of ");
                        out2.append(sm.getReturnType().getComponentType().getName());
                        out2.append("\n");
                        Object[] a = (Object[]) sValue;
                        for (int j = 0; j < a.length; j++) {
                            out2.append("[");
                            out2.append(j);
                            out2.append("] ");
                            if (a[j] == null) out2.append("<--NULL-->"); else if (DomainDAO.class.isAssignableFrom(a[j].getClass())) out2.append(((DomainDAO) a[j]).toString(objectStack)); else out2.append(a[j].toString());
                        }
                        out.append(StringHelper.linePad(out2.toString(), 4, " ", true));
                    } else {
                        if (DomainDAO.class.isAssignableFrom(sValue.getClass())) out.append(StringHelper.linePad(((DomainDAO) sValue).toString(objectStack), 4, " ", true)); else {
                            out.append(StringHelper.linePad(sValue.toString(), 4, " ", true));
                            out.append("\n");
                        }
                    }
                }
            }
        } catch (IllegalAccessException e) {
            MouldException me = new MouldException(MouldException.ACCESS_ERROR, "???", e.getMessage());
            log.error(me.getLocalizedMessage(), e);
        } catch (InvocationTargetException e) {
            MouldException me = new MouldException(MouldException.INVOCATION_ERROR, "???", e);
            log.error(me.getLocalizedMessage(), me.getCause());
        } catch (IntrospectionException e) {
            MouldException me = new MouldException(MouldException.INTROSPECT_ERROR, "???", e.getMessage());
            log.error(me.getLocalizedMessage(), e);
        }
        return out.toString();
    }

    /**
     * Mould data from domain object and its related objects into a new JavaBean based
     * domain object graph, based on the defined mapping rules.
     *<p>
     * Same as {@link #moldFromDomain(Object source, Object target, GraphMapping graph, MappingFilter filter, String objectPath, boolean includeKeys)}
     * except the graph is derived from the target object, a default MappingFilter (that
     * just returns fields from the root object) and includeKeys is false.
     * The objectPath is also null, assuming this is the first object in the domain
     * object graph.
     *<p>
     * @param source Source object to mould data from, typically extends Persistent
     * @param target Target object to mould data to, typically extends DomainDAO
     * @throws ApplicationExceptions Thrown if one or more application logic errors are generated during moulding
     * @throws FrameworkException Thrown if any runtime moulding error has occured.
     */
    public static void moldFromDomain(Object source, Object target) throws ApplicationExceptions, FrameworkException {
        moldFromDomain(source, target, null, null, null, false);
    }

    /**
     * Mould data from domain object and its related objects into a new JavaBean based
     * domain object graph, based on the defined mapping rules.
     *<p>
     * Same as {@link #moldFromDomain(Object source, Object target, GraphMapping graph, MappingFilter filter, String objectPath, boolean includeKeys)}
     * except the graph is derived from the target object, and includeKeys is false.
     *<p>
     * @param source Source object to mould data from, typically extends Persistent
     * @param target Target object to mould data to, typically extends DomainDAO
     * @param filter Filter object that it is used to control what fields are populated or the target objects
     * @param objectPath  The path of this object being processed. This identifies possible parent
     * and/or indexed entries where this object is contained.
     * @throws ApplicationExceptions Thrown if one or more application logic errors are generated during moulding
     * @throws FrameworkException Thrown if any runtime moulding error has occured.
     */
    public static void moldFromDomain(Object source, Object target, MappingFilter filter, String objectPath) throws ApplicationExceptions, FrameworkException {
        moldFromDomain(source, target, null, filter, objectPath, false);
    }

    /**
     * Mould data from domain object and its related objects into a new JavaBean based
     * domain object graph, based on the defined mapping rules.
     * @param source Source object to mould data from, typically extends Persistent
     * @param target Target object to mould data to, typically extends DomainDAO
     * @param graph The mapping class with the rules of how to map this source object
     * @param filter Filter object that it is used to control what fields are populated or the target objects
     * @param objectPath  The path of this object being processed. This identifies possible parent
     * and/or indexed entries where this object is contained.
     * @param includeKeys true if key fields should be included in results regardless of the filters
     * @throws ApplicationExceptions Thrown if one or more application logic errors are generated during moulding
     * @throws FrameworkException Thrown if any runtime moulding error has occured.
     */
    public static void moldFromDomain(Object source, Object target, GraphMapping graph, MappingFilter filter, String objectPath, boolean includeKeys) throws ApplicationExceptions, FrameworkException {
        if (graph == null) graph = MappingFactory.getInstance(target);
        if (filter == null) filter = new MappingFilter(graph);
        try {
            String[] tFields = graph.getDataFieldNames();
            if (tFields != null && tFields.length != 0) for (int i = 0; i < tFields.length; i++) {
                String tName = tFields[i];
                String fullName = tName;
                if (objectPath != null) fullName = objectPath + "." + fullName;
                if (filter == null || filter.isFieldIncluded(fullName) || (includeKeys && graph.isKeyField(tName))) {
                    String sName = graph.getDomainFieldName(tName);
                    PropertyDescriptor tDesc = graph.getDataFieldDescriptor(tName);
                    PropertyDescriptor sDesc = graph.getDomainFieldDescriptor(tName);
                    if (sDesc == null) log.error("No Getter for " + tName + " in path " + fullName);
                    Method sm = sDesc.getReadMethod();
                    if (!sm.isAccessible()) sm.setAccessible(true);
                    Method tm = tDesc.getWriteMethod();
                    if (!tm.isAccessible()) tm.setAccessible(true);
                    Class tClass = tDesc.getPropertyType();
                    Class sClass = sDesc.getPropertyType();
                    if (tClass.isAssignableFrom(sClass)) {
                        Object sValue = sm.invoke(source, (Object[]) null);
                        if (sValue != null) {
                            tm.invoke(target, new Object[] { sValue });
                            log.debug("Set " + tName + " = " + sValue);
                        } else log.debug(tName + " no set, NULL value");
                    } else if (DataTypeMapper.instance().isMappable(sClass, tClass)) {
                        Object sValue = sm.invoke(source, (Object[]) null);
                        if (sValue != null) {
                            sValue = DataTypeMapper.instance().map(sValue, tClass);
                            tm.invoke(target, new Object[] { sValue });
                            log.debug("Set " + tName + " = " + sValue);
                        } else log.debug(tName + " no set, NULL value");
                    } else if (DomainDAO.class.isAssignableFrom(tClass) && IPersistent.class.isAssignableFrom(sClass)) {
                        if (graph.isForeignField(tName)) {
                            List foreignKeys = graph.getForeignKeys(tName);
                            List foreignKeyValues = new ArrayList();
                            boolean nullKey = false;
                            for (Iterator k = foreignKeys.iterator(); k.hasNext(); ) {
                                String doProp = (String) k.next();
                                Object value = null;
                                PropertyDescriptor doPd = graph.getRealDomainFieldDescriptor(doProp);
                                if (doPd != null && doPd.getReadMethod() != null) {
                                    Method m = doPd.getReadMethod();
                                    if (!m.isAccessible()) m.setAccessible(true);
                                    value = m.invoke(source, new Object[] {});
                                    if (value == null) nullKey = true;
                                    foreignKeyValues.add(value);
                                } else {
                                    throw new MouldException(MouldException.INVALID_FK_MAPPING, objectPath, doProp, graph.getDomainClassShortName());
                                }
                            }
                            if (nullKey) {
                                log.debug("Did not create skeleton object '" + tClass.getName() + "': one or more foreign key values missing.");
                            } else {
                                log.debug("Creating foreign object - " + tClass.getName());
                                Object newDAO = newDAO(tClass);
                                boolean createSkeleton = true;
                                if (filter.areSubFieldsIncluded(fullName)) {
                                    log.debug("Read foreign object '" + fullName + "' and mold");
                                    try {
                                        Object sValue = sm.invoke(source, (Object[]) null);
                                        if (sValue != null) {
                                            BeanMoulder.moldFromDomain(sValue, newDAO, null, filter, fullName, true);
                                            createSkeleton = false;
                                        }
                                    } catch (InvocationTargetException e) {
                                        if (e.getCause() != null && e.getCause() instanceof InvalidForeignKeyException) log.warn("All foreign keys present, but foreign object does not exist"); else throw e;
                                    }
                                }
                                if (createSkeleton) {
                                    log.debug("Set keys on skeleton foreign object only");
                                    GraphMapping graph2 = MappingFactory.getInstance(newDAO);
                                    Set keys = graph2.getKeyFields();
                                    if (keys == null || keys.size() != foreignKeyValues.size()) {
                                        throw new MouldException(MouldException.MISMATCH_FK_MAPPING, objectPath, target.getClass().getName(), newDAO.getClass().getName());
                                    }
                                    int k2 = 0;
                                    for (Iterator k = keys.iterator(); k.hasNext(); k2++) {
                                        String keyField = (String) k.next();
                                        Object keyValue = foreignKeyValues.get(k2);
                                        PropertyDescriptor pd = graph2.getDataFieldDescriptor(keyField);
                                        if (pd != null && pd.getWriteMethod() != null) {
                                            Method m = pd.getWriteMethod();
                                            if (!m.isAccessible()) m.setAccessible(true);
                                            m.invoke(newDAO, new Object[] { keyValue });
                                        } else {
                                            throw new MouldException(MouldException.CANT_SET_KEY_FIELD, objectPath, keyField, newDAO.getClass().getName());
                                        }
                                    }
                                }
                                tm.invoke(target, new Object[] { newDAO });
                                log.debug("Set " + tName + " = " + newDAO);
                            }
                        } else {
                            if (filter.areSubFieldsIncluded(fullName)) {
                                log.debug("Creating One-To-One object - " + tClass.getName());
                                Object newDAO = newDAO(tClass);
                                log.debug("Read related object '" + fullName + "' and mold");
                                Object sValue = sm.invoke(source, (Object[]) null);
                                if (sValue != null) {
                                    BeanMoulder.moldFromDomain(sValue, newDAO, null, filter, fullName, true);
                                } else {
                                    log.debug("Related object '" + fullName + "' not found. Ignore it!");
                                }
                                tm.invoke(target, new Object[] { newDAO });
                                log.debug("Set " + tName + " = " + newDAO);
                            } else log.debug("No subfields for object " + fullName + " included. Object not retrieved");
                        }
                    } else if (tClass.isArray() && DomainDAO.class.isAssignableFrom(tClass.getComponentType()) && filter.areSubFieldsIncluded(fullName)) {
                        log.debug("Target is an array of DAO's");
                        log.debug("Read related objects '" + fullName + "' and mold");
                        Object sValue = sm.invoke(source, (Object[]) null);
                        if (sClass.isArray() && IPersistent.class.isAssignableFrom(sClass.getComponentType())) {
                            log.debug("Source is an array of Persistent Objects");
                            Object[] sArray = (Object[]) sValue;
                            if (sArray.length > 0) {
                                Object[] tArray = (Object[]) Array.newInstance(tClass.getComponentType(), sArray.length);
                                log.debug("Translate Array of Size " + sArray.length);
                                for (int j = 0; j < sArray.length; j++) {
                                    Object newDAO = newDAO(tClass.getComponentType());
                                    BeanMoulder.moldFromDomain(sArray[j], newDAO, null, filter, fullName, false);
                                    tArray[j] = newDAO;
                                    log.debug("Add to array [" + j + "] : " + newDAO);
                                }
                                tm.invoke(target, new Object[] { (Object) tArray });
                                log.debug("Set Array " + tName);
                            } else log.debug("Source Array is empty! Do Nothing");
                        }
                    } else {
                        String err = "Can't Mold Property " + fullName + " from " + sClass.getName() + " to " + tClass.getName();
                        log.error(err);
                        throw new RuntimeException(err);
                    }
                }
            }
            if (target != null && target instanceof DomainDAO) ((DomainDAO) target).clearChanges();
        } catch (IllegalAccessException e) {
            MouldException me = new MouldException(MouldException.ACCESS_ERROR, objectPath, e.getMessage());
            log.error(me.getLocalizedMessage(), e);
            throw me;
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof FrameworkException) throw (FrameworkException) e.getCause();
                if (e.getCause() instanceof ApplicationExceptions) throw (ApplicationExceptions) e.getCause();
                if (e.getCause() instanceof ApplicationException) {
                    ApplicationExceptions aes = new ApplicationExceptions();
                    aes.add((ApplicationException) e.getCause());
                    throw aes;
                }
            }
            MouldException me = new MouldException(MouldException.INVOCATION_ERROR, objectPath, e);
            log.error(me.getLocalizedMessage(), me.getCause());
            throw me;
        } catch (InstantiationException e) {
            MouldException me = new MouldException(MouldException.INSTANTICATION_ERROR, objectPath, e.getMessage());
            log.error(me.getLocalizedMessage(), e);
            throw me;
        }
    }

    private static Object newDAO(Class clazz) throws InstantiationException {
        try {
            Constructor c = clazz.getConstructor(new Class[] {});
            if (c == null) throw new InstantiationException("No zero argument construtor found");
            Object dao = c.newInstance((Object[]) null);
            log.debug("Created Object : " + dao);
            return dao;
        } catch (InstantiationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Can't create DAO object - " + e.getMessage(), e);
            throw new InstantiationException(e.getMessage());
        }
    }

    /**
     * Take a source object and try and mold it back it its domain object
     * @param path The path of this object being processed. This identifies possible parent
     * and/or indexed entries where this object is contained.
     * @param source Source object to mould from, typically a DomainDAO
     * @param uow Transaction handle all creates/update will be performed within.
     * Throws an exception if null.
     * @param handler Possible bean handler to be used when processing this source object graph
     * @throws ApplicationExceptions Thrown if one or more application logic errors are generated during moulding
     * @throws FrameworkException Thrown if any runtime moulding error has occured.
     */
    public static void updateBean(String path, DomainDAO source, UOW uow, MouldHandler handler) throws ApplicationExceptions, FrameworkException {
        log.debug("Update Bean " + path);
        source.validate();
        ApplicationExceptions aes = new ApplicationExceptions();
        if (uow == null) {
            String err = "UOW Required";
            log.error(err);
            throw new RuntimeException(err);
        }
        try {
            IPersistent domainObject = null;
            GraphMapping mapping = MappingFactory.getInstance(source);
            Map keys = new LinkedHashMap();
            Class doClass = mapping.getDomainClass();
            boolean gotKeys = fillInKeys(path, source, mapping, keys);
            if (gotKeys) {
                Method[] ma = doClass.getMethods();
                Method findByPK = null;
                for (int i = 0; i < ma.length; i++) {
                    if (ma[i].getName().equals("findByPK")) {
                        if (ma[i].getParameterTypes().length == (keys.size() + 1) && (ma[i].getParameterTypes())[0] == UOW.class) {
                            findByPK = ma[i];
                            break;
                        }
                    }
                }
                if (findByPK == null) {
                    aes.add(new DomainObjectNotFoundException(doClass.getName() + " @ " + path));
                    throw aes;
                }
                Object[] inputs = new Object[keys.size() + 1];
                {
                    inputs[0] = uow;
                    int i = 1;
                    for (Iterator it = keys.values().iterator(); it.hasNext(); i++) {
                        inputs[i] = it.next();
                    }
                }
                domainObject = (IPersistent) findByPK.invoke(null, inputs);
            } else log.debug("Object " + path + " has either missing or null key values - Assume Create is needed");
            if (domainObject == null) {
                log.debug("DO '" + mapping.getDomainClassShortName() + "' not found with key, create a new one...");
                domainObject = (IPersistent) doClass.newInstance();
                for (Iterator it = keys.keySet().iterator(); it.hasNext(); ) {
                    String keyField = (String) it.next();
                    Object value = keys.get(keyField);
                    updateProperty(mapping.getDomainFieldDescriptor(keyField), value, domainObject);
                }
            } else {
                log.debug("Found DO '" + mapping.getDomainClassShortName() + "' with key,");
            }
            updateBeanData(path, source, uow, handler, mapping, domainObject);
        } catch (IllegalAccessException e) {
            MouldException me = new MouldException(MouldException.ACCESS_ERROR, path, e.getMessage());
            log.error(me.getLocalizedMessage(), e);
            throw me;
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof FrameworkException) throw (FrameworkException) e.getCause();
                if (e.getCause() instanceof ApplicationExceptions) throw (ApplicationExceptions) e.getCause();
                if (e.getCause() instanceof ApplicationException) {
                    aes.add((ApplicationException) e.getCause());
                    throw aes;
                }
            }
            MouldException me = new MouldException(MouldException.INVOCATION_ERROR, path, e);
            log.error(me.getLocalizedMessage(), me.getCause());
            throw me;
        } catch (InstantiationException e) {
            MouldException me = new MouldException(MouldException.INSTANTICATION_ERROR, path, e.getMessage());
            log.error(me.getLocalizedMessage(), e);
            throw me;
        }
    }

    /**
     * Take a source object and delete it or delete is children if it has any
     * @param path The path of this object being processed. This identifies possible parent
     * and/or indexed entries where this object is contained.
     * @param source Source object to mould from, typically a DomainDAO
     * @param uow Transaction handle all creates/update will be performed within.
     * Throws an exception if null.
     * @param handler Possible bean handler to be used when processing this source object graph
     * @throws ApplicationExceptions Thrown if one or more application logic errors are generated during moulding
     * @throws FrameworkException Thrown if any runtime moulding error has occured.
     */
    public static void deleteBean(String path, DomainDAO source, UOW uow, MouldHandler handler) throws ApplicationExceptions, FrameworkException {
        log.debug("Delete Bean " + path);
        source.validate();
        ApplicationExceptions aes = new ApplicationExceptions();
        if (uow == null) {
            String err = "UOW Required";
            log.error(err);
            throw new RuntimeException(err);
        }
        try {
            IPersistent domainObject = null;
            GraphMapping mapping = MappingFactory.getInstance(source);
            Map keys = new LinkedHashMap();
            Class doClass = mapping.getDomainClass();
            boolean gotKeys = fillInKeys(path, source, mapping, keys);
            if (gotKeys) {
                Method[] ma = doClass.getMethods();
                Method findByPK = null;
                for (int i = 0; i < ma.length; i++) {
                    if (ma[i].getName().equals("findByPK")) {
                        if (ma[i].getParameterTypes().length == (keys.size() + 1) && (ma[i].getParameterTypes())[0] == UOW.class) {
                            findByPK = ma[i];
                            break;
                        }
                    }
                }
                if (findByPK == null) {
                    aes.add(new DomainObjectNotFoundException(doClass.getName()));
                    throw aes;
                }
                Object[] inputs = new Object[keys.size() + 1];
                {
                    inputs[0] = uow;
                    int i = 1;
                    for (Iterator it = keys.values().iterator(); it.hasNext(); i++) {
                        inputs[i] = it.next();
                    }
                }
                domainObject = (IPersistent) findByPK.invoke(null, inputs);
            } else log.debug("Object " + path + " has either missing or null key values - Assume Create is needed");
            if (domainObject == null) {
                String label = doClass.getName();
                try {
                    label = PersistentHelper.getLabelToken(doClass.getName());
                } catch (Exception e) {
                }
                aes.add(new DomainObjectNotFoundException(label + " (path=" + path + ")"));
                throw aes;
            }
            deleteBeanData(path, source, uow, handler, mapping, domainObject);
        } catch (IllegalAccessException e) {
            MouldException me = new MouldException(MouldException.ACCESS_ERROR, path, e.getMessage());
            log.error(me.getLocalizedMessage(), e);
            throw me;
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof FrameworkException) throw (FrameworkException) e.getCause();
                if (e.getCause() instanceof ApplicationExceptions) throw (ApplicationExceptions) e.getCause();
                if (e.getCause() instanceof ApplicationException) {
                    aes.add((ApplicationException) e.getCause());
                    throw aes;
                }
            }
            MouldException me = new MouldException(MouldException.INVOCATION_ERROR, path, e);
            log.error(me.getLocalizedMessage(), me.getCause());
            throw me;
        } catch (InstantiationException e) {
            MouldException me = new MouldException(MouldException.INSTANTICATION_ERROR, path, e.getMessage());
            log.error(me.getLocalizedMessage(), e);
            throw me;
        }
    }

    /** Pass in an emty map and it fills it with Key = Value for the source
     * object. It returns false if one or more key values are null, or if this
     * object has no keys defined
     */
    private static boolean fillInKeys(String path, DomainDAO source, GraphMapping mapping, Map map) throws InvocationTargetException, MouldException {
        try {
            Set keys = mapping.getKeyFields();
            boolean nullKey = false;
            if (keys == null || keys.size() == 0) {
                log.debug("Object Has No KEYS! - " + source.getClass().getName());
                return false;
            }
            for (Iterator k = keys.iterator(); k.hasNext(); ) {
                String keyField = (String) k.next();
                PropertyDescriptor pd = mapping.getDataFieldDescriptor(keyField);
                if (pd != null && pd.getReadMethod() != null) {
                    Method m = pd.getReadMethod();
                    if (!m.isAccessible()) m.setAccessible(true);
                    Object value = m.invoke(source, new Object[] {});
                    map.put(keyField, value);
                    log.debug("Key " + keyField + "='" + value + "' on object '" + source.getClass().getName() + "'");
                    if (value == null) {
                        nullKey = true;
                    }
                } else {
                    MouldException me = new MouldException(MouldException.NO_KEY_ON_OBJECT, path, keyField, source.getClass().getName());
                    log.error(me.getLocalizedMessage());
                    throw me;
                }
            }
            return !nullKey;
        } catch (IllegalAccessException e) {
            MouldException me = new MouldException(MouldException.ACCESS_ERROR, path, e.getMessage());
            log.error(me.getLocalizedMessage(), e);
            throw me;
        }
    }

    private static void updateBeanData(String path, DomainDAO source, UOW uow, MouldHandler handler, GraphMapping mapping, IPersistent domainObject) throws InstantiationException, IllegalAccessException, InvocationTargetException, ApplicationExceptions, FrameworkException {
        try {
            if (handler != null) handler.startBean(path, source, domainObject);
            for (Iterator it = mapping.getFields().iterator(); it.hasNext(); ) {
                String field = (String) it.next();
                if (source.hasChanged(field)) {
                    Object value = getProperty(mapping.getDataFieldDescriptor(field), source);
                    updateProperty(mapping.getDomainFieldDescriptor(field), value, domainObject);
                }
            }
            for (Iterator it = mapping.getForeignFields().iterator(); it.hasNext(); ) {
                String field = (String) it.next();
                if (source.hasChanged(field)) {
                    Object value = getProperty(mapping.getDataFieldDescriptor(field), source);
                    if (value != null) {
                        List targetKeys = mapping.getForeignKeys(field);
                        GraphMapping fMapping = MappingFactory.getInstance(mapping.getDataFieldDescriptor(field).getPropertyType());
                        Set sourceKeys = fMapping.getKeyFields();
                        int i = 0;
                        for (Iterator i2 = sourceKeys.iterator(); i2.hasNext(); i++) {
                            String sourceFld = (String) i2.next();
                            String targetFld = (String) targetKeys.get(i);
                            log.debug("Copy Foreign Key Field from " + sourceFld + " to " + targetFld);
                            Object value2 = getProperty(fMapping.getDataFieldDescriptor(sourceFld), value);
                            updateProperty(mapping.getRealDomainFieldDescriptor(targetFld), value2, domainObject);
                        }
                    }
                }
            }
            if (domainObject.isDatabaseOccurence()) {
                log.debug("UOW.Update Domain Object");
                if (handler != null) handler.startBeanUpdate(path, source, domainObject);
                uow.update(domainObject);
                if (handler != null) handler.endBeanUpdate(path, source, domainObject);
            } else {
                log.debug("UOW.Add Domain Object");
                if (handler != null) handler.startBeanAdd(path, source, domainObject);
                uow.add(domainObject);
                if (handler != null) handler.endBeanAdd(path, source, domainObject);
            }
            for (Iterator it = mapping.getRelatedFields().iterator(); it.hasNext(); ) {
                String field = (String) it.next();
                if (source.hasChanged(field)) {
                    Object value = getProperty(mapping.getDataFieldDescriptor(field), source);
                    if (value != null) {
                        if (value.getClass().isArray()) {
                            Object[] values = (Object[]) value;
                            for (int i = 0; i < values.length; i++) {
                                DomainDAO dao = (DomainDAO) values[i];
                                if (dao != null) {
                                    updateChildBean(path + "." + field + "[" + i + "]", dao, uow, handler, domainObject, mapping, field);
                                }
                            }
                        } else {
                            DomainDAO dao = (DomainDAO) value;
                            updateChildBean(path + "." + field, dao, uow, handler, domainObject, mapping, field);
                        }
                    }
                }
            }
            if (handler != null) handler.endBean(path, source, domainObject);
        } catch (ApplicationException e) {
            ApplicationExceptions aes = new ApplicationExceptions();
            aes.add(e);
            throw aes;
        }
    }

    /**  Take a source object and try and mold it back it its domain object.
     *  This is the same as updateParent, except from the way it retrieved the
     *  record, and the way it creates a new record.
     */
    private static void updateChildBean(String path, DomainDAO source, UOW uow, MouldHandler handler, IPersistent parentDomain, GraphMapping parentMapping, String parentField) throws ApplicationExceptions, FrameworkException {
        log.debug("Update Child Bean " + path);
        source.validate();
        ApplicationExceptions aes = new ApplicationExceptions();
        if (uow == null) {
            String err = "UOW Required";
            log.error(err);
            throw new RuntimeException(err);
        }
        String relationshipName = parentMapping.getDomainFieldName(parentField);
        if (relationshipName.endsWith("Array")) relationshipName = relationshipName.substring(0, relationshipName.length() - 5);
        if (relationshipName.endsWith("Object")) relationshipName = relationshipName.substring(0, relationshipName.length() - 6);
        try {
            IPersistent domainObject = null;
            GraphMapping mapping = MappingFactory.getInstance(source);
            Map keys = new LinkedHashMap();
            Class doClass = mapping.getDomainClass();
            boolean gotKeys = false;
            if (mapping.getKeyFields() == null || mapping.getKeyFields().size() == 0) {
                log.debug("Find 'one-to-one' object - " + path);
                domainObject = (IPersistent) getProperty(parentMapping.getDomainFieldDescriptor(parentField), parentDomain);
                if (domainObject == null) log.debug("Not Found - " + path);
            } else {
                gotKeys = fillInKeys(path, source, mapping, keys);
                if (gotKeys) {
                    Method findCriteria = null;
                    String methodName = "find" + StringHelper.getUpper1(relationshipName) + "Criteria";
                    try {
                        findCriteria = parentDomain.getClass().getMethod(methodName, new Class[] {});
                    } catch (NoSuchMethodException e) {
                        log.error("Find method '" + methodName + "' not found!");
                    }
                    if (findCriteria == null) {
                        throw new MouldException(MouldException.METHOD_NOT_FOUND, path, methodName);
                    }
                    Criteria criteria = (Criteria) findCriteria.invoke(parentDomain, new Object[] {});
                    for (Iterator it = keys.keySet().iterator(); it.hasNext(); ) {
                        String keyField = (String) it.next();
                        Object value = keys.get(keyField);
                        keyField = StringHelper.getUpper1(mapping.getDomainFieldName(keyField));
                        criteria.addCriteria(keyField, value);
                        log.debug(path + "- Add to criteria:" + keyField + "=" + value);
                    }
                    Iterator itr = uow.query(criteria).iterator();
                    if (itr.hasNext()) domainObject = (IPersistent) itr.next();
                    if (itr.hasNext()) {
                        MultipleDomainObjectsFoundException m = new MultipleDomainObjectsFoundException(criteria.getTable() + " @ " + path);
                        aes.add(m);
                        throw aes;
                    }
                } else {
                    log.debug("Object " + path + " has either missing or null key values - Assume Create is needed");
                }
            }
            if (domainObject == null) {
                log.debug("DO '" + mapping.getDomainClassShortName() + "' not found with key, create a new one...");
                Method newObject = null;
                String methodName = "new" + StringHelper.getUpper1(relationshipName) + "Object";
                try {
                    newObject = parentDomain.getClass().getMethod(methodName, new Class[] {});
                } catch (NoSuchMethodException e) {
                    log.error("Method '" + methodName + "()' not found!");
                }
                if (newObject == null) throw new MouldException(MouldException.METHOD_NOT_FOUND, path, methodName);
                domainObject = (IPersistent) newObject.invoke(parentDomain, new Object[] {});
                for (Iterator it = keys.keySet().iterator(); it.hasNext(); ) {
                    String keyField = (String) it.next();
                    Object value = keys.get(keyField);
                    updateProperty(mapping.getDomainFieldDescriptor(keyField), value, domainObject);
                }
            } else {
                log.debug("Found DO '" + mapping.getDomainClassShortName() + "' with key,");
            }
            updateBeanData(path, source, uow, handler, mapping, domainObject);
        } catch (IllegalAccessException e) {
            MouldException me = new MouldException(MouldException.ACCESS_ERROR, path, e.getMessage());
            log.error(me.getLocalizedMessage(), e);
            throw me;
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof FrameworkException) throw (FrameworkException) e.getCause();
                if (e.getCause() instanceof ApplicationExceptions) throw (ApplicationExceptions) e.getCause();
                if (e.getCause() instanceof ApplicationException) {
                    aes.add((ApplicationException) e.getCause());
                    throw aes;
                }
            }
            MouldException me = new MouldException(MouldException.INVOCATION_ERROR, path, e);
            log.error(me.getLocalizedMessage(), me.getCause());
            throw me;
        } catch (InstantiationException e) {
            MouldException me = new MouldException(MouldException.INSTANTICATION_ERROR, path, e.getMessage());
            log.error(me.getLocalizedMessage(), e);
            throw me;
        }
    }

    /**  Take a source object and try and mold it back it its domain object.
     *  This is the same as updateParent, except from the way it retrieved the
     *  record, and the way it creates a new record.
     */
    private static void deleteChildBean(String path, DomainDAO source, UOW uow, MouldHandler handler, IPersistent parentDomain, GraphMapping parentMapping, String parentField) throws ApplicationExceptions, FrameworkException {
        log.debug("Delete Child Bean " + path);
        source.validate();
        ApplicationExceptions aes = new ApplicationExceptions();
        if (uow == null) {
            String err = "UOW Required";
            log.error(err);
            throw new RuntimeException(err);
        }
        String relationshipName = parentMapping.getDomainFieldName(parentField);
        if (relationshipName.endsWith("Array")) relationshipName = relationshipName.substring(0, relationshipName.length() - 5);
        if (relationshipName.endsWith("Object")) relationshipName = relationshipName.substring(0, relationshipName.length() - 6);
        try {
            IPersistent domainObject = null;
            GraphMapping mapping = MappingFactory.getInstance(source);
            Map keys = new LinkedHashMap();
            Class doClass = mapping.getDomainClass();
            boolean gotKeys = false;
            if (mapping.getKeyFields() == null || mapping.getKeyFields().size() == 0) {
                log.debug("Find 'one-to-one' object - " + path);
                domainObject = (IPersistent) getProperty(parentMapping.getDomainFieldDescriptor(parentField), parentDomain);
                if (domainObject == null) log.debug("Not Found - " + path);
            } else {
                gotKeys = fillInKeys(path, source, mapping, keys);
                if (gotKeys) {
                    Method findCriteria = null;
                    String methodName = "find" + StringHelper.getUpper1(relationshipName) + "Criteria";
                    try {
                        findCriteria = parentDomain.getClass().getMethod(methodName, new Class[] {});
                    } catch (NoSuchMethodException e) {
                        log.error("Find method '" + methodName + "' not found!");
                    }
                    if (findCriteria == null) throw new MouldException(MouldException.METHOD_NOT_FOUND, path, methodName);
                    Criteria criteria = (Criteria) findCriteria.invoke(parentDomain, new Object[] {});
                    for (Iterator it = keys.keySet().iterator(); it.hasNext(); ) {
                        String keyField = (String) it.next();
                        Object value = keys.get(keyField);
                        keyField = StringHelper.getUpper1(mapping.getDomainFieldName(keyField));
                        criteria.addCriteria(keyField, value);
                        log.debug(path + "- Add to criteria:" + keyField + "=" + value);
                    }
                    Iterator itr = uow.query(criteria).iterator();
                    if (itr.hasNext()) domainObject = (IPersistent) itr.next();
                    if (itr.hasNext()) {
                        MultipleDomainObjectsFoundException m = new MultipleDomainObjectsFoundException(criteria.getTable() + " @ " + path);
                        aes.add(m);
                        throw aes;
                    }
                }
            }
            if (domainObject == null) {
                aes.add(new DomainObjectNotFoundException(doClass.getName() + " @ " + path));
                throw aes;
            }
            deleteBeanData(path, source, uow, handler, mapping, domainObject);
        } catch (IllegalAccessException e) {
            MouldException me = new MouldException(MouldException.ACCESS_ERROR, path, e.getMessage());
            log.error(me.getLocalizedMessage(), e);
            throw me;
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                if (e.getCause() instanceof FrameworkException) throw (FrameworkException) e.getCause();
                if (e.getCause() instanceof ApplicationExceptions) throw (ApplicationExceptions) e.getCause();
                if (e.getCause() instanceof ApplicationException) {
                    aes.add((ApplicationException) e.getCause());
                    throw aes;
                }
            }
            MouldException me = new MouldException(MouldException.INVOCATION_ERROR, path, e);
            log.error(me.getLocalizedMessage(), me.getCause());
            throw me;
        } catch (InstantiationException e) {
            MouldException me = new MouldException(MouldException.INSTANTICATION_ERROR, path, e.getMessage());
            log.error(me.getLocalizedMessage(), e);
            throw me;
        }
    }

    private static void deleteBeanData(String path, DomainDAO source, UOW uow, MouldHandler handler, GraphMapping mapping, IPersistent domainObject) throws InstantiationException, IllegalAccessException, InvocationTargetException, ApplicationExceptions, FrameworkException {
        try {
            if (handler != null) handler.startBean(path, source, domainObject);
            boolean deleteChild = false;
            for (Iterator it = mapping.getRelatedFields().iterator(); it.hasNext(); ) {
                String field = (String) it.next();
                if (source.hasChanged(field)) {
                    Object value = getProperty(mapping.getDataFieldDescriptor(field), source);
                    if (value != null) {
                        if (value.getClass().isArray()) {
                            Object[] values = (Object[]) value;
                            for (int i = 0; i < values.length; i++) {
                                DomainDAO dao = (DomainDAO) values[i];
                                if (dao != null) {
                                    deleteChild = true;
                                    deleteChildBean(path + "." + field + "[" + i + "]", dao, uow, handler, domainObject, mapping, field);
                                }
                            }
                        } else {
                            DomainDAO dao = (DomainDAO) value;
                            deleteChild = true;
                            deleteChildBean(path + "." + field, dao, uow, handler, domainObject, mapping, field);
                        }
                    }
                }
            }
            if (!deleteChild) {
                log.debug("UOW.Delete Domain Object");
                if (handler != null) handler.startBeanDelete(path, source, domainObject);
                uow.delete(domainObject);
                if (handler != null) handler.endBeanDelete(path, source, domainObject);
            }
            if (handler != null) handler.endBean(path, source, domainObject);
        } catch (ApplicationException e) {
            ApplicationExceptions aes = new ApplicationExceptions();
            aes.add(e);
            throw aes;
        }
    }

    private static void setProperty(PropertyDescriptor pd, Object value, Object source) throws IllegalAccessException, InvocationTargetException, MouldException {
        if (pd != null && pd.getWriteMethod() != null) {
            Method m = pd.getWriteMethod();
            if (!m.isAccessible()) m.setAccessible(true);
            Class tClass = m.getParameterTypes()[0];
            if (value == null || tClass.isAssignableFrom(value.getClass())) {
                m.invoke(source, new Object[] { value });
                log.debug("Set property '" + pd.getName() + "=" + value + "' on object '" + source.getClass().getName() + "'");
            } else if (DataTypeMapper.instance().isMappable(value.getClass(), tClass)) {
                value = DataTypeMapper.instance().map(value, tClass);
                m.invoke(source, new Object[] { value });
                log.debug("Translate+Set property '" + pd.getName() + "=" + value + "' on object '" + source.getClass().getName() + "'");
            } else {
                throw new MouldException(MouldException.DATATYPE_MISMATCH, source.getClass().getName() + "." + m.getName(), tClass.getName(), value.getClass().getName());
            }
        } else {
            MouldException me = new MouldException(MouldException.NO_SETTER, null, pd == null ? "???" : pd.getName(), source.getClass().getName());
            log.error(me.getLocalizedMessage());
            throw me;
        }
    }

    private static Object getProperty(PropertyDescriptor pd, Object source) throws IllegalAccessException, InvocationTargetException, MouldException {
        if (pd != null && pd.getReadMethod() != null) {
            Method m = pd.getReadMethod();
            if (!m.isAccessible()) m.setAccessible(true);
            Object value = m.invoke(source, new Object[] {});
            log.debug("Get property '" + pd.getName() + "=" + value + "' on object '" + source.getClass().getName() + "'");
            return value;
        } else {
            MouldException me = new MouldException(MouldException.NO_GETTER, null, pd == null ? "???" : pd.getName(), source.getClass().getName());
            log.error(me.getLocalizedMessage());
            throw me;
        }
    }

    /** Set a value on a Bean, if its a persistent bean, try to use an update method first
     * (for v1.0 domain objects), otherwise use the setter (for v1.1 and above).
     */
    private static void updateProperty(PropertyDescriptor pd, Object value, Object source) throws IllegalAccessException, InvocationTargetException, MouldException {
        if (source instanceof Persistent) {
            if (pd != null && pd.getWriteMethod() != null) {
                try {
                    Method m = source.getClass().getMethod("update" + StringHelper.getUpper1(pd.getName()), pd.getWriteMethod().getParameterTypes());
                    if (!m.isAccessible()) m.setAccessible(true);
                    Class tClass = m.getParameterTypes()[0];
                    if (value == null || tClass.isAssignableFrom(value.getClass())) {
                        m.invoke(source, new Object[] { value });
                        log.debug("Update property '" + pd.getName() + "=" + value + "' on object '" + source.getClass().getName() + "'");
                    } else if (DataTypeMapper.instance().isMappable(value.getClass(), tClass)) {
                        value = DataTypeMapper.instance().map(value, tClass);
                        m.invoke(source, new Object[] { value });
                        log.debug("Translate+Update property '" + pd.getName() + "=" + value + "' on object '" + source.getClass().getName() + "'");
                    } else {
                        throw new MouldException(MouldException.DATATYPE_MISMATCH, source.getClass().getName() + "." + m.getName(), tClass.getName(), value.getClass().getName());
                    }
                } catch (NoSuchMethodException e) {
                    log.debug("No Updator, try Setter for DO property '" + pd.getName() + "' on object '" + source.getClass().getName() + "'");
                    setProperty(pd, value, source);
                }
            } else {
                MouldException me = new MouldException(MouldException.NO_SETTER, null, pd == null ? "???" : pd.getName(), source.getClass().getName());
                log.error(me.getLocalizedMessage());
                throw me;
            }
        } else setProperty(pd, value, source);
    }
}
