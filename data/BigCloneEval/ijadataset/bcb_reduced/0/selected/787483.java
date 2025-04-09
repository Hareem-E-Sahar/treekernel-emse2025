package it.aco.mandragora.common;

import java.lang.reflect.Method;
import java.util.*;
import java.lang.IllegalAccessException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.ojb.broker.util.configuration.Configurator;
import org.apache.ojb.broker.util.configuration.Configuration;
import it.aco.mandragora.comparator.FieldComparator;
import it.aco.mandragora.comparator.BeanFieldComparator;
import it.aco.mandragora.exception.DataAccessException;
import it.aco.mandragora.util.configuration.impl.MandragoraConfigurator;
import net.sf.navigator.menu.MenuRepository;
import net.sf.navigator.menu.MenuComponent;

public class Utils {

    private static org.apache.log4j.Category log = org.apache.log4j.Logger.getLogger(Utils.class.getName());

    /**
     * Create a new Date representing the same of the input date, less the number of days specified by days.
     * It has no side effect on date .
     * @param date: date to use to create the new Date less the number of days specified by days
     * @param days: number of days to subtract.
     * @return a new Date of number of days less than the input date
     */
    public static Date subtractDays(Date date, Integer days) throws Exception {
        GregorianCalendar objGC = new GregorianCalendar();
        objGC.setTime(date);
        objGC.add(Calendar.DATE, -(days.intValue()));
        return objGC.getTime();
    }

    /**
     * Create a new Date representing the same of the input date, more the number of days specified by days.
     * It has no side effect on date .
     * @param date: date to use to create the new Date more the number of days specified by days
     * @param days: number of days to add.
     * @return a new Date of number of days more than the input date
     */
    public static Date addDays(Date date, Integer days) throws Exception {
        GregorianCalendar objGC = new GregorianCalendar();
        objGC.setTime(date);
        objGC.add(Calendar.DATE, days.intValue());
        return objGC.getTime();
    }

    /**
     * Create a new Date representing the same of the input date, more the number of minutes specified by minutes.
     * It has no side effect on date .
     * @param date: date to use to create the new Date more the number of minutes specified by minutes
     * @param minutes: number of minutes to add.
     * @return a new Date of number of minutes more than the input date
     * @throws Exception
     */
    public static Date addMinutes(Date date, Integer minutes) throws Exception {
        GregorianCalendar objGC = new GregorianCalendar();
        objGC.setTime(date);
        objGC.add(Calendar.MINUTE, minutes.intValue());
        return objGC.getTime();
    }

    /**
     * Create a new Date representing the same of the input date, less the number of minutes specified by minutes.
     * It has no side effect on date .
     * @param date: date to use to create the new Date less the number of minutes specified by minutes
     * @param minutes: number of minutes to subtract.
     * @return  a new Date of number of minutes less than the input date
     * @throws Exception
     */
    public static Date subtractMinutes(Date date, Integer minutes) throws Exception {
        GregorianCalendar objGC = new GregorianCalendar();
        objGC.setTime(date);
        objGC.add(Calendar.MINUTE, -minutes.intValue());
        return objGC.getTime();
    }

    /**
     * Create a new Date representing the same of the input date, more the number of hours specified by hours.
     * It has no side effect on date .
     * @param date: date to use to create the new Date more the number of hours specified by hours
     * @param hours: number of hours to add.
     * @return  a new Date of number of hours more than the input date
     * @throws Exception
     */
    public static Date addHours(Date date, Integer hours) throws Exception {
        GregorianCalendar objGC = new GregorianCalendar();
        objGC.setTime(date);
        objGC.add(Calendar.HOUR_OF_DAY, hours.intValue());
        return objGC.getTime();
    }

    /**
     * Create a new Date representing the same of the input date, less the number of hours specified by hours.
     * It has no side effect on date .
     * @param date: date to use to create the new Date less the number of hours specified by hours
     * @param hours: number of hours to subtract.
     * @return a new Date of number of hours less than the input date
     * @throws Exception
     */
    public static Date subtractHours(Date date, Integer hours) throws Exception {
        GregorianCalendar objGC = new GregorianCalendar();
        objGC.setTime(date);
        objGC.add(Calendar.HOUR_OF_DAY, -hours.intValue());
        return objGC.getTime();
    }

    /**
     * Return the hour of  date
     * @param date: Date of which hour will be returned
     * @return the hour of  date
     * @throws Exception
     */
    public static int getHour(Date date) throws Exception {
        GregorianCalendar objGC = new GregorianCalendar();
        objGC.setTime(date);
        return objGC.get(Calendar.HOUR_OF_DAY);
    }

    /**
    * Create a new Date of the same parameter date with input hour and minute.
    * There's no side effect on date.
    * @param date: date to round
    * @return return date at time hour:minute:00
    */
    public static Date round(Date date, int hour, int minute) throws Exception {
        GregorianCalendar objGC = new GregorianCalendar();
        objGC.setTime(date);
        objGC.set(GregorianCalendar.HOUR_OF_DAY, hour);
        objGC.set(GregorianCalendar.MINUTE, minute);
        return objGC.getTime();
    }

    /**
    * Method to format the elapsed time in milliseconds
    * @param timeInMillis
    * @return return a string in format hh:mm:ss
    */
    public static String formatTiempoRestante(int timeInMillis) throws Exception {
        int hours, minutes, seconds, timeInSeconds;
        timeInSeconds = timeInMillis / 1000;
        hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hours * 3600);
        minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (minutes * 60);
        seconds = timeInSeconds;
        return (new Integer(hours).toString() + ":" + new Integer(minutes).toString() + ":" + new Integer(seconds).toString());
    }

    /**
    * Realiza una b�squeda binaria de un objeto en un vector, con una propiedad determinada.
    * Nota: los objetos deben estar ordenados en el vector seg�n la propiedad sobre la que buscaremos.
    * @param v Vector de objetos en el que se realizar� la b�squeda.
    * @param objp Valor de la propiedad por la que realizamos la b�squeda.
    * @param op Clase que se utilizar� para hacer la comparaci�n de la propiedad indicada en 'getter'
    * @param getter Nombre del m�todo para recuperar el valor de la propiedad del objeto.
    * @return Devuelve el �ndice del primer objeto que se encuentre que cumple la propiedad.  Devuelve -1 si no se ha encontrado el objeto.
    * @throws java.lang.IllegalArgumentException
    * @throws java.lang.reflect.InvocationTargetException
    * @deprecated use findInCollection
    */
    public static int findInVector(Vector v, Object objp, Orderable op, Method getter) throws IllegalArgumentException, InvocationTargetException {
        if (v == null || v.isEmpty()) return -1;
        if (v == null || objp == null || op == null) throw new IllegalArgumentException("null arg in Utils.findInVector(Vector v, Object objp, Orderable op)");
        int low = 0;
        int high = v.size() - 1;
        try {
            while (low <= high) {
                int mid = (low + high) / 2;
                int c = op.compareTo(objp, getter.invoke(v.elementAt(mid), null));
                if (c < 0) high = mid - 1; else if (c > 0) low = mid + 1; else return mid;
            }
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("IllegalArgumentException in Utils.findInVector");
        }
        return -1;
    }

    /**
     * Return a new instance of Vector containing all elements of treeSet.
     * There's no side effect on treeSet.
     * If treeSet is null a null is returned
     * @param treeSet Object we want use to create te Vector
     * @return The resulting Vector or null if treeset is null
     */
    public static Vector fromTreeSetToVector(TreeSet treeSet) throws Exception {
        if (treeSet == null) return null;
        Vector vector = new Vector();
        for (Iterator iterator = treeSet.iterator(); iterator.hasNext(); ) {
            vector.add(iterator.next());
        }
        return vector;
    }

    /**
     * Create a new Istance of Treeset containing all elements of vector oredered using comparator
     * if vector is null a null is returned
     * @param vector: vector containing object to fill the new Treeset with
     * @param comparator: The Comparator to use to order vector's elements.
     * @return the new Treeset or null if vector is null.
     */
    public static TreeSet fromVectorToTreeSet(Vector vector, Comparator comparator) throws Exception {
        if (vector == null) return null;
        TreeSet treeSet = new TreeSet(comparator);
        treeSet.addAll(vector);
        return treeSet;
    }

    /**
     * Elements of collection must have a field named <code>in_property</code>.</br>
     * This method will sort <code>in_collection</code> comparing the values of <code>in_property</code>.</br>
     * If <code>in_property</code> is null or empty, nothing is done.</br>
     *
     * @param in_collection: collection to sort
     * @param in_property: property of elements of  <code>in_collection</code> to compare to sort
     * @throws ClassCastException
     * @throws UnsupportedOperationException
     */
    public static void sortCollection(List in_collection, String in_property) throws ClassCastException, UnsupportedOperationException {
        if (in_property != null && in_property.length() != 0) {
            Collections.sort(in_collection, new FieldComparator(in_property));
        }
    }

    /**
     *
     * Elements of in_collection must have fields with all names in properties.
     * The method will sort in_collection comparing the values of in_properties in the order they appear in the array
     * if in_properties is null or empty nothing is done
     * @param in_collection: collection to sort
     * @param in_properties: properties of elements of  in_collection to use to sort
     * @throws ClassCastException
     * @throws UnsupportedOperationException
     */
    public static void sortCollection(List in_collection, String[] in_properties) throws ClassCastException, UnsupportedOperationException {
        if (in_properties != null && in_properties.length != 0) {
            Collections.sort(in_collection, new FieldComparator(in_properties));
        }
    }

    /**
     * @deprecated use selectDistinct(Collection collection, String[] properties)
     * @param collection
     * @param property
     * @param clazz
     * @return
     * @throws Exception
     */
    public static Collection selectDistinct(List collection, String property, Class clazz) throws Exception {
        Vector result = null;
        try {
            Method getter = clazz.getMethod("get" + property.substring(0, 1).toUpperCase() + property.substring(1), null);
            Object current = null;
            Object previous = null;
            sortCollection(collection, property);
            Iterator iterator = collection.iterator();
            if (iterator.hasNext()) {
                previous = getter.invoke(iterator.next(), null);
                result.add(previous);
            }
            while (iterator.hasNext()) {
                current = getter.invoke(iterator.next(), null);
                if (!current.equals(previous)) {
                    result.add(current);
                    previous = current;
                }
            }
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectDistinct(List collection, String property, Class clazz) ", e);
        }
        return result;
    }

    /**
     * Elements of collection must have fields with all names in properties.
     * Return all elments of collection where just one is taken with the same values for fields whose names are in properties.
     * If more elements in collection have the same values for properties, the first one iterating is taken.
     * If collection is null returns null.
     * The real class of collection will be a Treeset.
     * There's no side effectof collection
     * @param collection: Collection which elements have to be filtered to have just one elements with the same values of properties
     * @param properties: fields names of elements of collection to use to filter
     * @return all elments of collection where just one is taken with the same values for fields whose names are in properties. Returns null if collection is null
     * @throws Exception
     */
    public static Collection selectDistinct(Collection collection, String[] properties) throws Exception {
        TreeSet result = null;
        try {
            if (collection == null) return null;
            result = new TreeSet(new FieldComparator(properties));
            Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                result.add(iterator.next());
            }
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectDistinct(Collection collection, String[] properties) ", e);
        }
        return result;
    }

    /**
     * Act as {@link #selectDistinct(Collection collection, String[] properties) with just one property}
     * @param collection: collection which elements have to be filtered to have just one elements with the same value of property
     * @param property :field names of elements of collection to use to filter
     * @return all elments of collection where just one is taken with the same value for field  property. Returns null if collection is null
     * @throws Exception
     */
    public static Collection selectDistinct(Collection collection, String property) throws Exception {
        Collection result = null;
        try {
            String[] properties = new String[1];
            properties[0] = property;
            result = selectDistinct(collection, properties);
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectDistinct(Collection collection, String property) ", e);
        }
        return result;
    }

    /**
     * Elements of collection must have a field named as the parameter field.
     * Will be returned a collection with all values of the property field of all elements of collection.
     * NOTE that no distinct is applied.
     * if collection is null an empty vector will be returned
     * @param collection: collection of elements whose value of field has to be returned
     * @param field: property of elements of collection that has to be returned
     * @return a collection with all values of the property field of all elements of collection.
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Collection selectFieldFromCollection(Collection collection, String field) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Vector result = new Vector();
        try {
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            if (collection != null) {
                Iterator iterator = collection.iterator();
                while (iterator.hasNext()) {
                    result.add(propertyUtilsBean.getProperty(iterator.next(), field));
                }
            }
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("NoSuchMethodException in Utils.selectFieldFromCollection(Collection collection, String field):" + e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in Utils.selectFieldFromCollection(Collection collection, String field):" + e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        }
        return result;
    }

    /**
     * Elements of collection must have fields with all names in properties.
     * Will be returned all elements of collection which no one of its properties[i] has value equal to values[i] for each i.
     * If both property[i] value and values[i] are null they are considered equal.
     * if collection is null returns null;
     * @param collection: collection whose elements have to be returned if all properties[i] have value not equal to values[i]
     * @param properties: properties to be compared to values
     * @param values:  values to be compared to properties.
     * @return all elements of collection which no one of its properties[i] has value equal to values[i] for each i or null if collection is null
     * @throws Exception
     */
    public static Collection selectWhereFieldsNotEqualsTo(Collection collection, String[] properties, Object[] values) throws Exception {
        Vector result = null;
        boolean found;
        try {
            if (collection == null) return null;
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            Iterator iterator = collection.iterator();
            result = new Vector();
            while (iterator.hasNext()) {
                Object object = new Object();
                object = iterator.next();
                found = true;
                for (int i = 0; i < properties.length; i++) {
                    Comparable collectionElementField = ((Comparable) propertyUtilsBean.getProperty(object, properties[i]));
                    if ((collectionElementField == null && values[i] == null)) {
                        found = false;
                        break;
                    }
                    if (collectionElementField != null && values[i] != null && collectionElementField.compareTo(values[i]) == 0) {
                        found = false;
                        break;
                    }
                }
                if (found) result.add(object);
            }
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectWhereFieldsNotEqualsTo(Collection collection, String[] properties, Object[] values) ", e);
        }
        return result;
    }

    /**
     * Acts as  selectWhereFieldsNotEqualsTo(Collection collection, String[] properties, Object[] values) with just one property and one value.
     * @param collection:collection whose elements have to be returned if property has value not equal to value. If both null are considered equal
     * @param property: property of elements of collection to be compared to value
     * @param value: value to be compared to property of elements of collection
     * @return all elements of collection which  property has value not equal to value or null if collection is null
     * @throws Exception
     */
    public static Collection selectWhereFieldNotEqualsTo(Collection collection, String property, Object value) throws Exception {
        Collection result = null;
        try {
            String[] properties = new String[1];
            properties[0] = property;
            Object[] values = new Object[1];
            values[0] = value;
            result = selectWhereFieldsNotEqualsTo(collection, properties, values);
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectWhereFieldNotEqualsTo(Collection collection, String property, Object value)  ", e);
        }
        return result;
    }

    /**
     * Elements of collection must have fields with all names in properties.
     * Will be returned all elements of collection which all of its properties[i] has value equal to values[i] for each i.
     * If both property[i] value and values[i] are null they are considered equal.
     * if collection is null returns null;
     * @param collection: collection whose elements have to be returned if all properties[i] have value equal to values[i]
     * @param properties: properties to be compared to values
     * @param values:  values to be compared to properties.
     * @return all elements of collection which all of its properties[i] has value equal to values[i] for each i or null if collection is null
     * @throws Exception
     */
    public static Collection selectWhereFieldsEqualsTo(Collection collection, String[] properties, Object[] values) throws Exception {
        Vector result = null;
        boolean found;
        try {
            if (collection == null) return null;
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            Iterator iterator = collection.iterator();
            result = new Vector();
            while (iterator.hasNext()) {
                Object object = new Object();
                object = iterator.next();
                found = true;
                for (int i = 0; i < properties.length; i++) {
                    Comparable collectionElementField = ((Comparable) propertyUtilsBean.getProperty(object, properties[i]));
                    if ((collectionElementField != null && values[i] == null) || (collectionElementField == null && values[i] != null)) {
                        found = false;
                        break;
                    }
                    if (collectionElementField != null && values[i] != null && collectionElementField.compareTo(values[i]) != 0) {
                        found = false;
                        break;
                    }
                }
                if (found) result.add(object);
            }
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectWhereFieldsEqualsTo(Collection collection, String[] properties, Object[] values) ", e);
        }
        return result;
    }

    /**
     * Acts as selectWhereFieldsEqualsTo(Collection collection, String[] properties, Object[] values) with just one property and one value
     * @param collection: collection whose elements have to be returned if property has value  equal to value. If both null are considered equal
     * @param property: property of elements of collection to be compared to value
     * @param value: value to be compared to property of elements of collection
     * @return  all elements of collection which  property has value  equal to value, or null if collection is null
     * @throws Exception
     */
    public static Collection selectWhereFieldEqualsTo(Collection collection, String property, Object value) throws Exception {
        Collection result = null;
        try {
            String[] properties = new String[1];
            properties[0] = property;
            Object[] values = new Object[1];
            values[0] = value;
            result = selectWhereFieldsEqualsTo(collection, properties, values);
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectWhereFieldEqualsTo(Collection collection, String property, Object value)  ", e);
        }
        return result;
    }

    /**
     * Elements of collection must have fields with all names in properties.
     * Will be returned all elements of collection which all of its properties[i] has value greater than values[i] for each i.
     * If one of value of property[i] and values[i] is null element will be returned
     * if collection is null returns null;
     * todo if value of property[i] is null element should'nt be returned
     * @param collection: collection whose elements have to be returned if property has value  greater than value.
     * @param properties: properties to be compared to values
     * @param values: value to be compared to property of elements of collection
     * @return  all elements of collection which all of its properties[i] has value greater than values[i] for each i.
     * @throws Exception
     */
    public static Collection selectWhereFieldsGreaterThan(Collection collection, String[] properties, Object[] values) throws Exception {
        Vector result = null;
        boolean found;
        try {
            if (collection == null) return null;
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            Iterator iterator = collection.iterator();
            result = new Vector();
            while (iterator.hasNext()) {
                Object object = new Object();
                object = iterator.next();
                found = true;
                for (int i = 0; i < properties.length; i++) {
                    Comparable collectionElementField = ((Comparable) propertyUtilsBean.getProperty(object, properties[i]));
                    if (collectionElementField != null && values[i] != null && collectionElementField.compareTo(values[i]) <= 0) {
                        found = false;
                        break;
                    }
                }
                if (found) result.add(object);
            }
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectWhereFieldsGreaterThan(Collection collection, String[] properties, Object[] values) ", e);
        }
        return result;
    }

    /**
     * This method returns a new collection holding the elements of <code>collection</code> which attribute <code>property</code> assumes one of the
     * values in the input array parameter <code>values</code>. So each element of <code>collection</code> must have an attribute with name as specified by
     * the input string parameter <code>property</code>,
     * and will be returned all elements of <code>collection</code>  which attribute <code>property</code> has value equals to at least one of <code>values[i]</code> for each <code>i</code>.
     * If the <code>property</code> value of an element of <code>collection</code> is null, and some item of <code>values</code> is null too, the element of <code>collection</code>
     * will be added to the collection to return.</br>
     * If  <code>collection</code> is null, a null is returned; if <code>values</code> is null, an empty collection will be returned.</br>
     *
     * @param collection collection to extract elements from
     * @param property atrtibute name of the elements of <code>collection</code> that has to assume one of the values in <code>values</code>
     * @param values array of values one of which at least must be equals to the <code>property</code> value of an element of <code>collection</code>, to add such element to the collection to return.
     * @return  a new collection holding the elements of <code>collection</code> which attribute <code>property</code> assumes one of the values in the input array parameter <code>values</code>
     * @throws Exception - if some element of <code>collection</code> don't have an attribute named  <code>property</code>
     */
    public static Collection selectWhereFieldIn(Collection collection, String property, Object[] values) throws Exception {
        Vector result = null;
        boolean found;
        try {
            if (collection == null) return null;
            result = new Vector();
            if (values == null) return result;
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                Object object = new Object();
                object = iterator.next();
                Comparable collectionElementField = ((Comparable) propertyUtilsBean.getProperty(object, property));
                found = false;
                for (int i = 0; i < values.length; i++) {
                    if ((collectionElementField == null && values[i] == null) || (collectionElementField != null && values[i] != null && collectionElementField.compareTo(values[i]) == 0)) {
                        found = true;
                        break;
                    }
                }
                if (found) result.add(object);
            }
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectWhereFieldIn(Collection collection, String property, Object[] values) ", e);
        }
        return result;
    }

    /**
     * This method has the same behavior of {@link #selectWhereFieldIn(Collection collection, String property, Object[] values)}
     * where the array <code>values</code> is contituted by the keys of map that a re not mapped to null a value.</br>
     * If  <code>map</code> is null, <code>values</code> will be null as well.</br>
     * @param collection  ollection to extract elements from
     * @param property  atrtibute name of the elements of <code>collection</code> that has to assume one of the keys not mapped to a nul value in <code>map</code>
     * @param map its keys mapped to a not null value will be used to search in  <code>collection</code>
     * @return  a new collection holding the elements of <code>collection</code> which attribute <code>property</code> assumes one of the keys mapped to a not null value in <code>map</code>
     * @throws Exception if some element of <code>collection</code> don't have an attribute named  <code>property</code>
     */
    public static Collection selectWhereFieldIn(Collection collection, String property, Map map) throws Exception {
        Vector result = null;
        boolean found;
        try {
            Object[] keys = getKeysWithNotNullValue(map);
            return selectWhereFieldIn(collection, property, keys);
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectWhereFieldIn(Collection collection, String property,Map map) ", e);
        }
    }

    /**
     *
     * @param collection
     * @param property
     * @param value
     * @return
     * @throws Exception
     */
    public static Collection selectWhereFieldGreaterThan(Collection collection, String property, Object value) throws Exception {
        Collection result = null;
        try {
            String[] properties = new String[1];
            properties[0] = property;
            Object[] values = new Object[1];
            values[0] = value;
            result = selectWhereFieldsGreaterThan(collection, properties, values);
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectWhereFieldGreaterThan(Collection collection, String property, Object value)  ", e);
        }
        return result;
    }

    /**
     *
     * @param collection
     * @param properties
     * @param values
     * @return
     * @throws Exception
     */
    public static Collection selectWhereFieldsLessThan(Collection collection, String[] properties, Object[] values) throws Exception {
        Vector result = null;
        boolean found;
        try {
            if (collection == null) return null;
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            Iterator iterator = collection.iterator();
            result = new Vector();
            while (iterator.hasNext()) {
                Object object = new Object();
                object = iterator.next();
                found = true;
                for (int i = 0; i < properties.length; i++) {
                    Comparable collectionElementField = ((Comparable) propertyUtilsBean.getProperty(object, properties[i]));
                    if (collectionElementField != null && values[i] != null && collectionElementField.compareTo(values[i]) >= 0) {
                        found = false;
                        break;
                    }
                }
                if (found) result.add(object);
            }
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectWhereFieldsLessThan(Collection collection, String[] properties, Object[] values) ", e);
        }
        return result;
    }

    /**
     *
     * @param collection
     * @param property
     * @param value
     * @return
     * @throws Exception
     */
    public static Collection selectWhereFieldLessThan(Collection collection, String property, Object value) throws Exception {
        Collection result = null;
        try {
            String[] properties = new String[1];
            properties[0] = property;
            Object[] values = new Object[1];
            values[0] = value;
            result = selectWhereFieldsLessThan(collection, properties, values);
        } catch (Exception e) {
            throw new Exception("Error in Utils.selectWhereFieldsLessThan(Collection collection, String property, Object value)  ", e);
        }
        return result;
    }

    /**
     *
     * @param v
     * @param map
     * @param getter
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @deprecated use #removeFromCollectionNotInMap(Collection collection, HashMap map, String property)
     */
    public static void removeFromVectorNotInMap(Vector v, HashMap map, Method getter) throws IllegalAccessException, InvocationTargetException {
        try {
            if (v != null && !v.isEmpty() && map != null) {
                int j = 0;
                int max = v.size();
                for (int i = 0; i < max; i++) {
                    if (map.get(getter.invoke(v.elementAt(j), null)) == null) {
                        v.remove(j);
                    } else {
                        j++;
                    }
                }
            }
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in Utils.findInVector");
        }
    }

    /**
     * Elements of collection must have fields a field named property.
     * For all elements of collection the value of property will be used as key in map.
     * If this key in map have a null value associated the element will be removed form collection.
     * @param collection: collection which elements have to be removed from, if property value used as key, returns a null value from map
     * @param map: map where to search property value of collection's elements
     * @param property: collection element property which value have to be searched in map to determine if it has to be removed or not
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void removeFromCollectionNotInMap(Collection collection, HashMap map, String property) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        try {
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            if (collection != null && !collection.isEmpty() && map != null) {
                Iterator iterator = collection.iterator();
                while (iterator.hasNext()) {
                    if (map.get(propertyUtilsBean.getProperty(iterator.next(), property)) == null) iterator.remove();
                }
            }
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("NoSuchMethodException thrown in Utils.removeFromCollectionNotInMap(Collection collection, HashMap map, String property)" + e.toString());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in  Utils.removeFromCollectionNotInMap(Collection collection, HashMap map, String property)" + e.toString());
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e, "InvocationTargetException in Utils.removeFromCollectionNotInMap(Collection collection, HashMap map, String property)" + e.toString());
        }
    }

    public static void removeFromCollectionOfVectorNotInMap(Collection c, HashMap map, Method getter) throws IllegalAccessException, InvocationTargetException {
        try {
            Iterator iterator = c.iterator();
            while (iterator.hasNext()) {
                removeFromVectorNotInMap((Vector) iterator.next(), map, getter);
            }
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in Utils.findInVector");
        }
    }

    public static String disableField(String formName, String fieldName) {
        String result = "";
        result = "<script>disableField(" + formName + ",'" + fieldName + "');</script>\n";
        return result;
    }

    public static String disableField(String functionName, String formName, String fieldName) {
        String result = "";
        result = "<script>" + functionName + "(" + formName + ",'" + fieldName + "');</script>\n";
        return result;
    }

    public static Object getField(Object object, String field) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object result = null;
        try {
            result = object.getClass().getMethod("get" + field.substring(0, 1).toUpperCase() + field.substring(1), null).invoke(object, null);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("NoSuchMethodException in Utils.getField(Object object, String field):" + e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in Utils.getField(Object object, String field):" + e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        }
        return result;
    }

    /**
     * Call the object method setField().
     * @param object: object which method setField()has to be called
     * @param field: object must have a method setsetField().
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static void setField(Object object, String field) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            object.getClass().getMethod("set" + field.substring(0, 1).toUpperCase() + field.substring(1), null).invoke(object, null);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("NoSuchMethodException in Utils.setField(Object object, String field):" + e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in Utils.setField(Object object, String field):" + e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        }
    }

    public static void setField(Collection valueObjectsCollection, String pAttributeName) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            if (valueObjectsCollection == null) return;
            Iterator iterator = valueObjectsCollection.iterator();
            while (iterator.hasNext()) {
                Object object = iterator.next();
                object.getClass().getMethod("set" + pAttributeName.substring(0, 1).toUpperCase() + pAttributeName.substring(1), null).invoke(object, null);
            }
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("NoSuchMethodException in Utils.setField(Collection valueObjectsCollection, String pAttributeName):" + e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in Utils.setField(Collection valueObjectsCollection, String pAttributeName):" + e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        }
    }

    /**
     *
     * @param object
     * @param field
     * @param value
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static void setField(Object object, String field, Object value) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            if (value == null) {
                setField(object, field);
            } else {
                Class[] arrayOfClass = new Class[1];
                arrayOfClass[0] = value.getClass();
                Object[] arrayOfObject = new Object[1];
                arrayOfObject[0] = value;
                object.getClass().getMethod("set" + field.substring(0, 1).toUpperCase() + field.substring(1), arrayOfClass).invoke(object, arrayOfObject);
            }
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("NoSuchMethodException in Utils.setField(Object object, String field):" + e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in Utils.setField(Object object, String field):" + e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        }
    }

    /**
     * Update a Map with the values of a page of a collection.
     * The collection is the value of a property named collectionToGet of the bean valueObject. (valueObject.collectionToGet)
     * All elements of the collection must have a field named fieldToGetName.
     * For all elements of the collection from offset to offset+lengthPage (the page) it looks in the request for a parameter named  mapName(value of fieldToGetName).
     * It put this value in the map using as key the same value of fieldToGetName .
     * If fieldToSetName is not null it use the value of request parameter to set the property named fieldToSetName of the same collections's element.
     * @param valueObject: Bean holding the collection
     * @param map: Map to update
     * @param mapName:name of the mapped parameter in the request
     * @param collectionToGet: name of the collection in the bean
     * @param lengthPage: length of the page
     * @param offset: offset of the page
     * @param fieldToGetName: name of the field of collection's element which value will be used with mapName to determine which request parameter to read
     * @param fieldToSetName: name of the field of collection's element which has to be updated with te request value
     * @param request
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static void updateMapWithPageCollection(Object valueObject, HashMap map, String mapName, String collectionToGet, int lengthPage, int offset, String fieldToGetName, String fieldToSetName, HttpServletRequest request) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            updateMapWithPageCollection(map, mapName, (Collection) propertyUtilsBean.getNestedProperty(valueObject, collectionToGet), lengthPage, offset, fieldToGetName, fieldToSetName, request);
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("NoSuchMethodException in Utils.updateMapWithPageCollection(Object valueObject, HashMap map, String mapName, String collectionToGet, int lengthPage,int offset, String fieldToGetName,String fieldToSetName,HttpServletRequest request):" + e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in Utils.updateMapWithPageCollection(Object valueObject, HashMap map, String mapName, String collectionToGet, int lengthPage,int offset, String fieldToGetName,String fieldToSetName,HttpServletRequest request):" + e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        }
    }

    /**
     * Update a Map with the values of a page of a collection.
     * The page of the collection starts at offset and ends at offset+lengthPage.
     * All elements of the collection must have a field named fieldToGetName.
     * For all elements of the collection from offset to offset+lengthPage (the page) it looks in the request for a parameter named  mapName(value of fieldToGetName).
     * It put this value in the map using as key the same value of fieldToGetName .
     * If fieldToSetName is not null it use the value of request parameter to set the property named fieldToSetName of the same collections's element.
     * @param map: Map to update
     * @param mapName: name of the mapped parameter in the request
     * @param collectionToGet: collection to use to build te request mapped parameters to update the map
     * @param lengthPage: length of the page
     * @param offset: offset of the page
     * @param fieldToGetName: name of the field of collection's element which value will be used with mapName to determine which request parameter to read
     * @param fieldToSetName: name of the field of collection's element which has to be updated with te request value
     * @param request: request
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static void updateMapWithPageCollection(HashMap map, String mapName, Collection collectionToGet, int lengthPage, int offset, String fieldToGetName, String fieldToSetName, HttpServletRequest request) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            Object[] collectionToGetArray = collectionToGet.toArray();
            Object fieldValue = null;
            Object requestValue = null;
            for (int i = 0; i < lengthPage; i++) {
                if (i + offset < collectionToGetArray.length) {
                    fieldValue = propertyUtilsBean.getProperty(collectionToGetArray[i + offset], fieldToGetName);
                    requestValue = request.getParameter(mapName + "(" + fieldValue.toString() + ")");
                    map.put(fieldValue, requestValue);
                    if (fieldToSetName != null) BeanUtils.setProperty(collectionToGetArray[i + offset], fieldToSetName, requestValue);
                }
            }
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("NoSuchMethodException in Utils.updateMapWithPageCollection(Object valueObject, HashMap map, String mapName, String collectionToGet, int lengthPage,int offset, String fieldToGetName,String fieldToSetName,HttpServletRequest request):" + e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in Utils.updateMapWithPageCollection(Object valueObject, HashMap map, String mapName, String collectionToGet, int lengthPage,int offset, String fieldToGetName,String fieldToSetName,HttpServletRequest request):" + e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        }
    }

    public static void updateMapWithPageCollection(HashMap map, String mapName, Collection collectionToGet, int lengthPage, int offset, String fieldToGetName, String fieldToSetName, HttpServletRequest request, boolean parameterValues) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            Object[] collectionToGetArray = collectionToGet.toArray();
            Object fieldValue = null;
            Object requestValue = null;
            for (int i = 0; i < lengthPage; i++) {
                if (i + offset < collectionToGetArray.length) {
                    fieldValue = propertyUtilsBean.getProperty(collectionToGetArray[i + offset], fieldToGetName);
                    if (parameterValues) {
                        requestValue = request.getParameterValues(mapName + "(" + fieldValue.toString() + ")");
                    } else {
                        requestValue = request.getParameter(mapName + "(" + fieldValue.toString() + ")");
                    }
                    map.put(fieldValue, requestValue);
                    if (fieldToSetName != null) propertyUtilsBean.setProperty(collectionToGetArray[i + offset], fieldToSetName, requestValue);
                }
            }
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("NoSuchMethodException in Utils.updateMapWithPageCollection(Object valueObject, HashMap map, String mapName, String collectionToGet, int lengthPage,int offset, String fieldToGetName,String fieldToSetName,HttpServletRequest request):" + e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in Utils.updateMapWithPageCollection(Object valueObject, HashMap map, String mapName, String collectionToGet, int lengthPage,int offset, String fieldToGetName,String fieldToSetName,HttpServletRequest request):" + e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        }
    }

    /**
     * Update elements of the map correspondig at the collection's page starting at offset and ending at offset +  lengthPage.
     * Update the same collection's elements too
     * @param valueObject :Bean containing the collection
     * @param map  HashMap to update.
     * @param collectionToGet name of the collection to use to update.
     * @param fieldKey collection's field that acts as key of the map
     * @param lengthPage length of the collection page
     * @param offset offset of the collection page
     * @param methodName name of the method to apply to the map's element
     * @param collectionToGetElementPropertyToSet property of the element of the collection to set with the value of mapElementPropertyToGet
     * @param mapElementPropertyToGet property of the map's element to use to set the collection element
     */
    public static void alignCollectionWithMap(Object valueObject, HashMap map, String collectionToGet, String fieldKey, int lengthPage, int offset, String methodName, String collectionToGetElementPropertyToSet, String mapElementPropertyToGet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            Object collectionToGetElement = null;
            Object mapElement = null;
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            Object[] collectionToGetArray = ((Collection) propertyUtilsBean.getNestedProperty(valueObject, collectionToGet)).toArray();
            for (int i = 0; i < lengthPage; i++) {
                if (i + offset < collectionToGetArray.length) {
                    collectionToGetElement = collectionToGetArray[i + offset];
                    mapElement = map.get(propertyUtilsBean.getNestedProperty(collectionToGetArray[i + offset], fieldKey));
                    if (methodName != null && !methodName.equals("")) mapElement.getClass().getMethod(methodName, null).invoke(mapElement, null);
                    if (collectionToGetElement != null && !collectionToGetElement.equals("") && mapElementPropertyToGet != null && !mapElementPropertyToGet.equals("")) {
                        propertyUtilsBean.setProperty(collectionToGetElement, collectionToGetElementPropertyToSet, propertyUtilsBean.getNestedProperty(mapElement, mapElementPropertyToGet));
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("NoSuchMethodException in Utils.alignCollectionWithMap(Object valueObject,HashMap map,String collectionToGet, String fieldKey, int lengthPage,int offset, String methodName, String collectionToGetElementPropertyToSet, String  mapElementPropertyToGet ):" + e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in Utils.alignCollectionWithMap(Object valueObject,HashMap map,String collectionToGet, String fieldKey, int lengthPage,int offset, String methodName, String collectionToGetElementPropertyToSet, String  mapElementPropertyToGet ):" + e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        }
    }

    /**
     * Return an element of collection whose property named field has value value. If more than such an element exists it's no determined which one is returned
     * if such an element doesn't exist return null;
     * If collection is null or empty return null.
     * @param collection: collection where the element will be searched in
     * @param field: property of colelction's element that has to have value value
     * @param value: value of property field that must have the collection's element to be returned
     * @return an element of collection whose property named field has value value
     * @throws IllegalAccessException
     */
    public static Object findInCollection(Collection collection, String field, Object value) throws IllegalAccessException {
        Object result = null;
        try {
            if (collection == null || collection.isEmpty()) return null;
            ArrayList arrayList = Collections.list(Collections.enumeration(collection));
            sortCollection(arrayList, field);
            int index = Collections.binarySearch(arrayList, value, new BeanFieldComparator(field));
            if (index >= 0) result = arrayList.get(index);
        } catch (Exception e) {
            throw new IllegalAccessException("Exception in Utils.findInCollection(Collection collection, String field, Object value):" + e);
        }
        return result;
    }

    /**
     * Cycles until (new Date()).getTime() change
     */
    public static void waitUntilDateRefreshed() {
        long dateTime = (new Date()).getTime();
        while (dateTime == (new Date()).getTime()) {
        }
    }

    /**
     *
     * @param collection
     * @param property
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static Collection getCollectionProperty(Collection collection, String property) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        ArrayList result = new ArrayList();
        Object propertyValue = null;
        try {
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            if (collection == null) return null;
            Iterator iterator = collection.iterator();
            while (iterator.hasNext()) {
                propertyValue = new Object();
                propertyValue = propertyUtilsBean.getProperty(iterator.next(), property);
                result.add(propertyValue);
            }
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("NoSuchMethodException in Utils.getCollectionProperty(Collection collection, String property):" + e);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessException("IllegalAccessException in Utils.getCollectionProperty(Collection collection, String property):" + e);
        } catch (InvocationTargetException e) {
            throw new InvocationTargetException(e);
        }
        return result;
    }

    /**
     *
     * @param collection
     * @return
     * @throws Exception
     */
    public static Collection removeNullElementsFromCollection(Collection collection) throws Exception {
        if (collection == null) return null;
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == null) iterator.remove();
        }
        return collection;
    }

    /**
     *
     * @param repository
     * @param path
     * @param title
     * @param add
     * @param remove
     * @param action
     * @param image
     * @return
     * @throws Exception
     */
    public static boolean addRemoveMenuItem(MenuRepository repository, String[] path, String title, boolean add, boolean remove, String action, String image) throws Exception {
        if (repository == null || path == null || path.length == 0 || title == null || title.equals("")) {
            throw new Exception("Exception thrown in Utils.addRemoveMenuItem(MenuRepository repository, String[] path, String title, boolean add, boolean remove, String action, String image) : repository,path and title can't be null or empty");
        }
        Iterator iterator = null;
        MenuComponent menuComponent = repository.getMenu(path[0]);
        List menuComponents = menuComponent.getComponents();
        boolean found = false;
        for (int i = 0; i < path.length; i++) {
            if (menuComponents != null && !menuComponents.isEmpty()) {
                iterator = menuComponents.iterator();
                MenuComponent currMenuComponent;
                while (iterator.hasNext()) {
                    found = false;
                    currMenuComponent = (MenuComponent) iterator.next();
                    if (i < path.length - 1) {
                        if (currMenuComponent.getName().equals(path[i + 1])) {
                            menuComponents = currMenuComponent.getComponents();
                            menuComponent = currMenuComponent;
                            found = true;
                            break;
                        }
                    } else {
                        if (currMenuComponent.getTitle().equals(title)) {
                            if (remove) iterator.remove();
                            menuComponent = currMenuComponent;
                            found = true;
                            break;
                        }
                    }
                }
            }
            if (!found) break;
        }
        if ((add && (remove || !found))) {
            MenuComponent addMenuComponent = new MenuComponent();
            addMenuComponent.setTitle(title);
            addMenuComponent.setAction(action);
            addMenuComponent.setImage(image);
            menuComponent.addMenuComponent(addMenuComponent);
        }
        return found;
    }

    /**
     *
     * @param repository
     * @param path
     * @param title
     * @return
     * @throws Exception
     */
    public static boolean removeMenuItem(MenuRepository repository, String[] path, String title) throws Exception {
        return addRemoveMenuItem(repository, path, title, false, true, null, null);
    }

    public static Class getClassFromMandragoraProperties(String className) throws Exception {
        Class clazz = null;
        try {
            Configurator configurator = MandragoraConfigurator.getInstance();
            Configuration config = configurator.getConfigurationFor(null);
            clazz = config.getClass(className, null);
        } catch (Exception e) {
            throw new Exception("Error in  Utils.getClassFromMandragoraProperties(String className): " + e.toString(), e);
        }
        return clazz;
    }

    public static String getStringFromMandragoraProperties(String string) throws Exception {
        String result = null;
        try {
            Configurator configurator = MandragoraConfigurator.getInstance();
            Configuration config = configurator.getConfigurationFor(null);
            result = config.getString(string, null);
        } catch (Exception e) {
            throw new Exception("Error in  Utils.getStringFromMandragoraProperties(String string): " + e.toString(), e);
        }
        return result;
    }

    /**
     * Suppose you have a tree oganization starting at valueObjectOrCollection.
     * valueObjectOrCollection (or each element of it if it is a collection) must have a property named as the first token
     * of path (separator is the point), and so elements represented by this property must have a property namend as the
     * second token of path.
     * The method returns all the leaf of the tree satisfying the path. If a branch doesn't get the endof the path
     * it doens't bring any leaf.
     * Note that leafs can be beans, simple field, or collection too.
     * If path is null or empty or valueObjectOrCollection is null a null is returned
     * @deprecated use  {@link #getTreeLeafs(Object valueObjectOrCollection, String path)}
     * @param valueObjectOrCollection : staring point (root)
     * @param path: point separeted names of properties that walk on branch
     * @return leafs.
     * @throws Exception
     */
    public static Collection getLeafsTree(Object valueObjectOrCollection, String path) throws Exception {
        Collection result = null;
        String firstAttributeName = "";
        String remainingPath = "";
        try {
            if (path == null || path.trim().equals("")) return result;
            if (valueObjectOrCollection == null) return result;
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            log.debug("Utils.getLeafsTree:path=" + path);
            int indexOf = path.indexOf(".");
            if (indexOf == -1) {
                firstAttributeName = path;
            } else {
                firstAttributeName = path.substring(0, path.indexOf("."));
                remainingPath = path.substring(indexOf + 1);
                log.debug("Utils.getLeafsTree:remainingPath=" + remainingPath);
            }
            log.debug("Utils.getLeafsTree:remainingPath:firstAttributeName=" + firstAttributeName);
            if (Collection.class.isInstance(valueObjectOrCollection)) {
                log.debug("Utils.getLeafsTree: valueobjectOrCollection is a collection");
                result = new ArrayList();
                Iterator iterator = ((Collection) valueObjectOrCollection).iterator();
                if (indexOf >= 0) {
                    while (iterator.hasNext()) {
                        Collection collection = new ArrayList();
                        collection = getLeafsTree(propertyUtilsBean.getProperty(iterator.next(), firstAttributeName), remainingPath);
                        if (collection != null) {
                            result.addAll(collection);
                        }
                    }
                } else {
                    while (iterator.hasNext()) {
                        Object bean = propertyUtilsBean.getProperty(iterator.next(), firstAttributeName);
                        if (bean != null) {
                            result.add(bean);
                        }
                    }
                }
            } else {
                log.debug("Utils.getLeafsTree: valueobjectOrCollection is a valueobject");
                if (indexOf >= 0) {
                    result = getLeafsTree(propertyUtilsBean.getProperty(valueObjectOrCollection, firstAttributeName), remainingPath);
                } else {
                    Object bean = propertyUtilsBean.getProperty(valueObjectOrCollection, firstAttributeName);
                    if (bean != null) {
                        result = new ArrayList();
                        result.add(bean);
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            throw new Exception("NoSuchMethodException thrown in Utils.getLeafsTree(Object valueobjectOrCollection, String path)" + e.toString(), e);
        } catch (IllegalAccessException e) {
            throw new Exception("IllegalAccessException thrown in Utils.getLeafsTree(Object valueobjectOrCollection, String path)" + e.toString(), e);
        } catch (InvocationTargetException e) {
            throw new Exception("InvocationTargetException thrown in Utils.getLeafsTree(Object valueobjectOrCollection, String path)" + e.toString(), e);
        }
        return result;
    }

    /**
     * @deprecated
     */
    public static Collection getTreeLeafs(Object valueObjectOrCollection, String path) throws Exception {
        Collection result = null;
        String firstAttributeName = "";
        String remainingPath = "";
        try {
            if (path == null || path.trim().equals("")) return result;
            if (valueObjectOrCollection == null) return result;
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            log.debug("Utils.getTreeLeafs:path=" + path);
            int indexOf = path.indexOf(".");
            if (indexOf == -1) {
                firstAttributeName = path;
            } else {
                firstAttributeName = path.substring(0, path.indexOf("."));
                remainingPath = path.substring(indexOf + 1);
                log.debug("Utils.getTreeLeafs:remainingPath=" + remainingPath);
            }
            log.debug("Utils.getTreeLeafs:remainingPath:firstAttributeName=" + firstAttributeName);
            if (Collection.class.isInstance(valueObjectOrCollection)) {
                log.debug("Utils.getTreeLeafs: valueobjectOrCollection is a collection");
                result = new ArrayList();
                Iterator iterator = ((Collection) valueObjectOrCollection).iterator();
                if (indexOf >= 0) {
                    while (iterator.hasNext()) {
                        Collection collection = getTreeLeafs(propertyUtilsBean.getProperty(iterator.next(), firstAttributeName), remainingPath);
                        if (collection != null) {
                            result.addAll(collection);
                        }
                    }
                } else {
                    while (iterator.hasNext()) {
                        Object bean = propertyUtilsBean.getProperty(iterator.next(), firstAttributeName);
                        if (bean != null) {
                            result.add(bean);
                        }
                    }
                }
            } else {
                log.debug("Utils.getTreeLeafs: valueobjectOrCollection is a valueobject");
                if (indexOf >= 0) {
                    result = getTreeLeafs(propertyUtilsBean.getProperty(valueObjectOrCollection, firstAttributeName), remainingPath);
                } else {
                    Object bean = propertyUtilsBean.getProperty(valueObjectOrCollection, firstAttributeName);
                    if (bean != null) {
                        result = new ArrayList();
                        result.add(bean);
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            throw new Exception("NoSuchMethodException thrown in Utils.getTreeLeafs(Object valueobjectOrCollection, String path)" + e.toString(), e);
        } catch (IllegalAccessException e) {
            throw new Exception("IllegalAccessException thrown in Utils.getTreeLeafs(Object valueobjectOrCollection, String path)" + e.toString(), e);
        } catch (InvocationTargetException e) {
            throw new Exception("InvocationTargetException thrown in Utils.getTreeLeafs(Object valueobjectOrCollection, String path)" + e.toString(), e);
        }
        return result;
    }

    /**
     * @param iterator
     * @return a colllection with all elements remaining int the iterator. if iterator is null return null
     * @throws Exception
     */
    public static Collection getRemainingItems(Iterator iterator) throws Exception {
        ArrayList arrayList = new ArrayList();
        try {
            if (iterator == null) return null;
            while (iterator.hasNext()) {
                arrayList.add(iterator.next());
            }
        } catch (Exception e) {
            throw new Exception("Exception thrown in Utils.getRemainingItems(Iterator iterator)" + e.toString(), e);
        }
        return arrayList;
    }

    /**
     * This method splits in two part a dot separated list of tokens, that is the input string <code>path</code>.</br>
     * The first part is the same <code>path</code> except the last token, and the second part is the last token.
     * If <code>path</code> is null a null is returned.
     * @param path dot separated list of tokens
     * @return an array of two strings, where the first element is a string that is the dot separated list of tokens of input except the last token,
     *          and the second element is the last token. If <code>path</code> is just one token the first element is an empty string, and the second one
     *          is the same <code>path</code>. If <code>path</code> is null a null is returned.</br>
     * @throws Exception
     */
    public static String[] getExceptLastTokenAndLastToken(String path) throws Exception {
        String[] result;
        try {
            if (path == null) return null;
            result = new String[2];
            String exceptLastToken = "";
            String lastToken;
            int lastIndexOf = path.lastIndexOf(".");
            if (lastIndexOf == -1) {
                lastToken = path;
            } else {
                exceptLastToken = path.substring(0, lastIndexOf);
                lastToken = path.substring(lastIndexOf + 1);
            }
            log.debug("getExceptLastTokenAndLastToken(String path): path=" + path);
            log.debug("getExceptLastTokenAndLastToken(String path): exceptLastToken=" + exceptLastToken);
            log.debug("getExceptLastTokenAndLastToken(String path): lastToken=" + lastToken);
            result[0] = exceptLastToken;
            result[1] = lastToken;
        } catch (Exception e) {
            throw new Exception("Exception thrown in Utils.getExceptLastTokenAndLastToken(String path)" + e.toString(), e);
        }
        return result;
    }

    public static String[] getFirstAttributeNameAndRemainingPath(String path) throws Exception {
        String[] result;
        try {
            if (path == null) return null;
            result = new String[2];
            String firstAttributeName;
            String remainingPath = "";
            int indexOf = path.indexOf(".");
            if (indexOf == -1) {
                firstAttributeName = path;
            } else {
                firstAttributeName = path.substring(0, indexOf);
                remainingPath = path.substring(indexOf + 1);
            }
            log.debug("getFirstAttributeNameAndRemainingPath(String path): path=" + path);
            log.debug("getFirstAttributeNameAndRemainingPath(String path): firstAttributeName=" + firstAttributeName);
            log.debug("getFirstAttributeNameAndRemainingPath(String path): remainingPath=" + remainingPath);
            result[0] = firstAttributeName;
            result[1] = remainingPath;
        } catch (Exception e) {
            throw new Exception("Exception thrown in Utils.getFirstAttributeNameAndRemainingPath(String path)" + e.toString(), e);
        }
        return result;
    }

    /**
     * This method creates an array of objects of length the sum of the lengths of <code>array1</code> and <code>array2</code>, considering length = 0 for null array.
     * So if <code>array1</code> is null, the length of the created array is the length of <code>array2</code>, if <code>array2</code> is null, the length of the created array is the length of <code>array1</code>,
     * and if both <code>array1</code> and <code>array2</code>, is created an returned an array of length 0.</br>
     * The first <code>array1.length</code> position of the created array are loaded with <code>array1</code>, and the other ones with <code>array2</code>.</br>
     * @param array1
     * @param array2
     * @return A new array of objects  containing the elements of the two arrays.
     * @throws Exception
     */
    public static Object[] arrayUnion(Object[] array1, Object[] array2) throws Exception {
        try {
            ArrayList unionCollection = new ArrayList();
            if (array1 != null) {
                for (int i = 0; i < array1.length; i++) {
                    unionCollection.add(array1[i]);
                }
            }
            if (array2 != null) {
                for (int i = 0; i < array2.length; i++) {
                    unionCollection.add(array2[i]);
                }
            }
            if (array1 == null) {
                if (array2 == null) return new Object[0];
                return unionCollection.toArray(array2);
            }
            if (array2 == null) return unionCollection.toArray(array1);
            if (array1.getClass().equals(array2.getClass())) return unionCollection.toArray(array1);
            return unionCollection.toArray(new Object[unionCollection.size()]);
        } catch (Exception e) {
            throw new Exception("Exception thrown in Utils.arrayUnion(Object[] array1, Object[] array2)" + e.toString(), e);
        }
    }

    /**
     * This method creates an array of objects, holding all the elements of <code>array1</code> not present in  <code>array2</code>.</br>
     * The array will be an array of elements of the specified class <code>clazz</code>.</br>
     * If <code>array1</code>  is null a null is returned.</br>
     * If <code>array2</code> is is null a new array with all the elements of <code>array1</code> is returned.
     * @param array1  array1  array which elements will be held by the returned array if not present in <code>array2</code>.
     * @param array2  array that holds the elements that must not be present in the returned array.
     * @param clazz  Class of the elements of the returned array
     * @return    A new array with the elements of <code>array1</code> not present in <code>array2</code> .
     * @throws Exception for any  trouble
     */
    public static Object[] arraySubtract(Object[] array1, Object[] array2, Class clazz) throws Exception {
        try {
            if (array1 == null) return null;
            if (array2 == null) array2 = new Object[0];
            ArrayList subtract = new ArrayList();
            for (int i = 0; i < array1.length; i++) {
                boolean found = false;
                for (int j = 0; j < array2.length; j++) {
                    if (array1[i].equals(array2[j])) {
                        found = true;
                        break;
                    }
                }
                if (!found) subtract.add(array1[i]);
            }
            return subtract.toArray((Object[]) Array.newInstance(clazz, subtract.size()));
        } catch (Exception e) {
            throw new Exception("Exception thrown in Utils.arraySubtract(Object[] array1, Object[] array2)" + e.toString(), e);
        }
    }

    /**
     * This method returns false if at least one item of <code>array</code> is null, otherwise it returns true
     * @param array array of objects to check if are null
     * @return  true if all items of <code>array</code> are not null
     * @throws Exception  if array is null
     */
    public static boolean areAllArrayElementsNotNull(Object[] array) throws Exception {
        try {
            if (array == null) throw new Exception("Exception thrown in Utils.areAllArrayElementsNotNull (Object[] array) :array can't be null");
            for (int i = 0; i < array.length; i++) {
                if (array[i] == null) return false;
            }
            return true;
        } catch (Exception e) {
            throw new Exception("Exception thrown in Utils.areAllArrayElementsNotNull (Object[] array) " + e.toString(), e);
        }
    }

    /**
     * This method returns false if at least one attribute of <code>pAttributeNames</code> assumes value null in is <code>pInstance</code>, otherwise it returns true.</br>
     * @param pInstance value object holding the attributes whose names are specified by  <code>pAttributeNames</code> and which values are checked if are null or not
     * @param pAttributeNames attributes names of <code>pInstance</code> to check if are null or not
     * @return true if no one of the attributes specified by <code>pAttributeNames</code> are null in <code>pInstance</code>, false otherwise. </br>
     * @throws Exception if <code>pInstance</code> or   <code>pAttributeNames</code> are null
     */
    public static boolean areAllAttributesValuesNotNull(Object pInstance, String[] pAttributeNames) throws Exception {
        try {
            if (pInstance == null || pAttributeNames == null) throw new Exception("Exception thrown in Utils.areAllAttributesValuesNotNull(Object pInstance,String[] pAttributeNames): pInstance and pAttributeNames can't be null");
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            for (int i = 0; i < pAttributeNames.length; i++) {
                if (propertyUtilsBean.getProperty(pInstance, pAttributeNames[i]) == null) return false;
            }
            return true;
        } catch (IllegalAccessException e) {
            throw new Exception("Exception thrown in Utils.areAllArrayElementsNotNull (Object[] array) " + e.toString(), e);
        } catch (InvocationTargetException e) {
            throw new Exception("Exception thrown in Utils.areAllArrayElementsNotNull (Object[] array) " + e.toString(), e);
        } catch (NoSuchMethodException e) {
            throw new Exception("Exception thrown in Utils.areAllArrayElementsNotNull (Object[] array) " + e.toString(), e);
        }
    }

    /**
     * This method looks in map for all its key that are mapped to a not null value, and returns them in an array of Objects.</br>
     * Note that in the returned array there are the keys, and not the values..</br>
     * if <code>map</code> is null, a null will be returned.</br>
     * @param map Map which keys with not null value are returned in an array
     * @return  An array containing all the keys of <code>map</code> not mapped to a null value.
     * @throws Exception
     */
    public static Object[] getKeysWithNotNullValue(Map map) throws Exception {
        try {
            if (map == null) return null;
            ArrayList keys = new ArrayList();
            Iterator iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                Object key = iterator.next();
                if (map.get(key) != null) keys.add(key);
            }
            return keys.toArray();
        } catch (Exception e) {
            throw new Exception("Exception thrown in Utils.getKeysWithNotNullValue(Map map) : " + e.toString(), e);
        }
    }

    /**
     * @deprecated use {@link it.aco.mandragora.common.utils.BeanCollectionUtils#getPropertyIfNotNullOnPath(Object bean, String path)}
     *
     * @param bean
     * @param path
     * @return
     * @throws Exception
     */
    public static Object getPropertyIfNotNullOnPath(Object bean, String path) throws Exception {
        Object propertyValue = null;
        try {
            PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();
            String[] firstAttributeNameAndRemainingPath = Utils.getFirstAttributeNameAndRemainingPath(path);
            String firstAttributeName = firstAttributeNameAndRemainingPath[0];
            String remainingPath = firstAttributeNameAndRemainingPath[1];
            Object firstAttributeValue = propertyUtilsBean.getProperty(bean, firstAttributeName);
            if (remainingPath.equals("") || firstAttributeValue == null) {
                propertyValue = firstAttributeValue;
            } else {
                propertyValue = getPropertyIfNotNullOnPath(firstAttributeValue, remainingPath);
            }
        } catch (Exception e) {
            throw new Exception("Exception thrown in getPropertyIfNotNullOnPath(Object bean, String path): " + e.toString(), e);
        }
        return propertyValue;
    }

    public static String buildInString(List in) throws Exception {
        String result;
        try {
            if (in == null || in.isEmpty()) return "";
            result = "(" + in.get(0).toString();
            for (int i = 1; i < in.size(); i++) {
                result = result + "," + in.get(i).toString();
            }
            result = result + ")";
        } catch (Exception e) {
            throw new Exception("Exception thrown in Utils.buildInString(List in): " + e.toString(), e);
        }
        return result;
    }

    public static Class getGenericClass(Type type) throws Exception {
        Class genericClass = null;
        try {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type[] fieldArgTypes = parameterizedType.getActualTypeArguments();
                for (Type fieldArgType : fieldArgTypes) {
                    genericClass = (Class) fieldArgType;
                    log.debug("genericClass = " + genericClass);
                }
            }
        } catch (Exception e) {
            throw new Exception("Exception thrown in Utils.getGenericClass(Type type): " + e.toString(), e);
        }
        return genericClass;
    }

    public Method getGetter(Class realClass, String pAttributeName) throws DataAccessException {
        Method getter = null;
        try {
            if (realClass == null || pAttributeName == null || pAttributeName.trim().equals("")) {
                throw new DataAccessException("Error in  Utils.getGetter(Class realClass, String pAttributeName): realClass is null or pAttributeName is null or empty string");
            }
            getter = realClass.getDeclaredMethod("get" + pAttributeName.substring(0, 0).toUpperCase() + pAttributeName.substring(1));
        } catch (SecurityException e) {
            log.error("SecurityException caught : " + e.toString());
            throw new DataAccessException("Error in  Utils.getGetter(Class realClass, String pAttributeName): " + e.toString(), e);
        } catch (NoSuchMethodException e) {
            log.error("NoSuchMethodException caught : " + e.toString());
            throw new DataAccessException("Error in  Utils.getGetter(Class realClass, String pAttributeName): " + e.toString(), e);
        }
        return getter;
    }
}
