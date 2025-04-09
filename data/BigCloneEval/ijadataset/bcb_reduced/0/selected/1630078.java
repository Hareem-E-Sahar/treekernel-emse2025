package com.g0dkar.leet.util.ognl.typeConversion.primitive;

import java.lang.reflect.Type;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.g0dkar.leet.util.ognl.OgnlUtils;
import com.g0dkar.leet.util.ognl.typeConversion.LeetTypeConverter;
import com.g0dkar.leet.util.ognl.typeConversion.TypeConverter;
import com.g0dkar.leet.util.ognl.typeConversion.TypeConverterManager;
import com.g0dkar.leet.util.reflection.ReflectionUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

@LeetTypeConverter
public class CollectionConverter implements TypeConverter {

    private static CollectionConverter INSTANCE = null;

    public static Gson SERIALIZER;

    static {
        JsonSerializer dateSerializer = new JsonSerializer() {

            private DateConverter converter = new DateConverter();

            public JsonElement serialize(Object src, Type typeOfSrc, JsonSerializationContext context) {
                if (converter.canConvert(src.getClass())) {
                    try {
                        return context.serialize(converter.toString(src, null));
                    } catch (Exception e) {
                        return context.serialize(src);
                    }
                }
                return context.serialize(src);
            }
        };
        SERIALIZER = new GsonBuilder().serializeNulls().serializeSpecialFloatingPointValues().registerTypeAdapter(Date.class, dateSerializer).registerTypeAdapter(Calendar.class, dateSerializer).registerTypeAdapter(java.sql.Date.class, dateSerializer).registerTypeAdapter(Time.class, dateSerializer).registerTypeAdapter(Timestamp.class, dateSerializer).create();
    }

    public static String asStringArray(String[] strings) {
        StringBuilder serialized = new StringBuilder(SERIALIZER.toJson(strings));
        serialized.replace(0, 1, "{");
        serialized.replace(serialized.length() - 1, serialized.length(), "}");
        return serialized.toString();
    }

    public static CollectionConverter getInstance() {
        synchronized (CollectionConverter.class) {
            if (INSTANCE == null) {
                INSTANCE = new CollectionConverter();
            }
        }
        return INSTANCE;
    }

    public String toString(Object value, Class<?>... genericTypes) throws Exception {
        if (canConvert(value.getClass())) {
            List objects = new ArrayList();
            if (genericTypes == null || genericTypes.length == 0 || genericTypes[0].equals(Object.class) || ReflectionUtils.isPrimitive(genericTypes[0])) {
                objects.addAll((Collection) value);
            } else {
                TypeConverter converter = TypeConverterManager.getConverterFor(genericTypes[0]);
                if (converter == null) {
                    objects.addAll((Collection) value);
                } else {
                    Collection valueList = (Collection) value;
                    for (Object object : valueList) {
                        objects.add(converter.toString(object, null));
                    }
                }
            }
            StringBuilder serialized = new StringBuilder(SERIALIZER.toJson(objects));
            serialized.replace(0, 1, "{");
            serialized.replace(serialized.length() - 1, serialized.length(), "}");
            return serialized.toString();
        }
        return null;
    }

    public Object fromString(String value, Class<?> toType, Class<?>... genericTypes) throws Exception {
        if (!canConvert(toType)) {
            return null;
        }
        if (genericTypes == null || genericTypes.length == 0) {
            return createOGNLCollection(value, toType);
        } else if (genericTypes[0].equals(Object.class) || ReflectionUtils.isPrimitive(genericTypes[0])) {
            return createOGNLCollection(value, toType);
        } else {
            Collection values = createOGNLCollection(value, toType);
            TypeConverter converter = TypeConverterManager.getConverterFor(genericTypes[0]);
            if (converter == null) {
                return values;
            } else {
                Collection collection = newCollectionInstance(toType, null, Set.class.isAssignableFrom(toType) ? HashSet.class : ArrayList.class);
                for (Object object : values) {
                    collection.add(converter.fromString(object.toString(), genericTypes[0]));
                }
                return collection;
            }
        }
    }

    public boolean canConvert(Class<?> toType) {
        return Collection.class.isAssignableFrom(toType);
    }

    public Object newInstance(Class<?> toType) throws Exception {
        if (canConvert(toType)) {
            return fromString("{}", toType);
        }
        return null;
    }

    /**
	 * Creates a {@link Collection} of {@link Object objects}. Uses OGNL to create a default collection. OGNL will
	 * understand most primitives and create objects according to it's type (e.g. <code>123</code> to {@link Integer},
	 * <code>"123"</code> to {@link String}).
	 * 
	 * @param value
	 *            The OGNL string for a Collection - <code>{ element1, element2, element3 }</code>
	 * @param toType
	 *            The final {@link Collection} type (List.class, Set.class, ArrayList.class, LinkedHashSet.class, ...)
	 * @return The {@link Collection}. The default for Collection and List is {@link ArrayList}. The default for Set is
	 *         {@link HashSet}.
	 */
    public Collection<?> createOGNLCollection(String value, Class<?> toType) {
        List values = (List) OgnlUtils.get(value, null);
        Collection colecao = null;
        if (List.class.isAssignableFrom(toType)) {
            colecao = newCollectionInstance(toType, values, ArrayList.class);
        } else if (Set.class.isAssignableFrom(toType)) {
            colecao = newCollectionInstance(toType, values, HashSet.class);
        } else {
            colecao = new ArrayList(values);
        }
        return colecao;
    }

    /**
	 * <p>
	 * Creates a new Collection through reflection.
	 * </p>
	 * 
	 * <p>
	 * First the code checks if there's a <code>Collection(Collection)</code> constructor. If it does, then it'll be
	 * called with <code>values</code> as the paratemer. If it doesn't, then the default noargs construction will be
	 * called. If neither can be called (if a Exception happens) then the this same process will be repeated for the
	 * <code>fallback</code> class with a recursive call to <code>newCollectionInstance(fallback, null, values)</code>.
	 * </p>
	 * 
	 * @param type
	 *            The Collection {@link Class}
	 * @param values
	 *            The initial values for the newly created Collection (will be passed as parameter to the
	 *            Collection(Collection<?>) construtor, if it exists)
	 * @param fallbacks
	 *            A fallback {@link Class} in case <code>type</code> cannot be instantiated ({@link ArrayList} is
	 *            recommended). Can be <code>null</code> should no fallback be used.
	 * @return The newly created Collection or <code>null</code> if it can't be built.
	 */
    public Collection<?> newCollectionInstance(Class<?> type, Collection<?> values, Class<?>... fallbacks) {
        Collection newCollection = null;
        try {
            if (values != null && type.getConstructor(Collection.class) != null) {
                newCollection = (Collection) ReflectionUtils.newInstance(type, values);
            } else {
                newCollection = (Collection) ReflectionUtils.newInstance(type);
                if (values != null) {
                    newCollection.addAll(values);
                }
            }
        } catch (Exception e) {
            if (fallbacks != null && fallbacks.length > 0) {
                if (fallbacks.length == 1) {
                    newCollection = newCollectionInstance(fallbacks[0], values);
                } else {
                    Class<?>[] theOtherFallbacks = new Class<?>[fallbacks.length - 1];
                    for (int i = 1; i < fallbacks.length; i++) {
                        theOtherFallbacks[i - 1] = fallbacks[i];
                    }
                    newCollection = newCollectionInstance(fallbacks[0], values, theOtherFallbacks);
                }
            }
        }
        return newCollection;
    }

    public String toDisplayableString(Object value, Class<?>... genericTypes) throws Exception {
        if (canConvert(value.getClass())) {
            if (!((Collection) value).isEmpty()) {
                StringBuilder str = new StringBuilder();
                List list = new ArrayList((Collection) value);
                for (int i = 0, size = list.size(); i < size; i++) {
                    str.append(TypeConverterManager.toDisplayableString(list.get(i), genericTypes));
                    if (i < size - 1) {
                        str.append(", ");
                    }
                }
                return str.toString();
            } else {
                return "";
            }
        }
        return null;
    }
}
