package hu.ihash.common.gui.properties.renderer;

import hu.ihash.common.gui.properties.model.BoundProperty;
import hu.ihash.common.gui.properties.model.PropertyContext.PropertyType;
import hu.ihash.database.entities.FilePath;
import hu.ihash.database.entities.Keyword;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for creating property renderers. Each renderer is assigned to a
 * class by a hashmap in this factory.
 * 
 * @author Gergely Kiss
 * 
 */
public abstract class RendererFactory {

    private static final Logger log = LoggerFactory.getLogger(RendererFactory.class);

    private static HashMap<String, Class<? extends BoundPropertyRenderer>> renderers;

    static {
        renderers = new HashMap<String, Class<? extends BoundPropertyRenderer>>();
        {
            addRenderer(String.class, StringRenderer.class);
            addRenderer(FilePath.class, true, FilePathCollectionRenderer.class);
            addRenderer(Keyword.class, true, KeywordCollectionRenderer.class);
            addRenderer(byte[].class, ByteArrayRenderer.class);
            addRenderer(Date.class, DatePropertyRenderer.class);
        }
    }

    private static void addRenderer(Type type, Class<? extends BoundPropertyRenderer> renderer) {
        addRenderer(type, false, renderer);
    }

    private static void addRenderer(Type type, boolean isCollection, Class<? extends BoundPropertyRenderer> renderer) {
        String key = isCollection ? ("C]" + type) : type.toString();
        renderers.put(key, renderer);
    }

    public static Class getRenderer(Type type) {
        return getRenderer(type, false);
    }

    public static Class getRenderer(Type type, boolean isCollection) {
        String key = isCollection ? ("C]" + type.toString()) : type.toString();
        return renderers.get(key);
    }

    /**
	 * Returns a new renderer instance for the property.
	 * 
	 * @param prop
	 * @return
	 */
    public static BoundPropertyRenderer createRendererFor(BoundProperty prop) {
        if (prop == null) {
            log.error("Property shouldn't be null!");
            return null;
        }
        if (prop.getParamType() == null) {
            log.error("Param type for property " + prop.getName() + " shouldn't be null!");
            return null;
        }
        Class<?> prClass = null;
        if (Collection.class.isAssignableFrom(prop.getParamType())) {
            Type collType = prop.getGetter().getGenericReturnType();
            Type[] elemTypes = ((ParameterizedType) collType).getActualTypeArguments();
            if (elemTypes.length == 1) {
                prClass = getRenderer(elemTypes[0], true);
            } else {
                log.warn("Collection property: " + prop.getParamType().getName() + " should have exactly one type parameter. Creating default renderer.");
            }
        } else {
            prClass = getRenderer(prop.getParamType());
        }
        if (prClass == null) {
            log.warn("Renderer for property: " + prop.getParamType().getName() + " does not exist. Creating default renderer.");
            return new DefaultRenderer(prop);
        }
        return createRenderer(prop, prClass);
    }

    /**
	 * Returns a new renderer instance for the property type.
	 * 
	 * @param type
	 * @return
	 */
    public static BoundPropertyRenderer createRendererFor(BoundProperty prop, PropertyType type) {
        if (type == null) {
            log.error("Property type shouldn't be null!");
            return null;
        }
        if (type.getRendererClass() == null) {
            log.error("Param type for property " + type + " shouldn't be null!");
            return null;
        }
        Class<?> prClass = type.getRendererClass();
        if (prClass == null) {
            log.warn("Renderer for property type: " + type + " does not exist. Creating default renderer.");
            return new DefaultRenderer(prop);
        }
        return createRenderer(prop, prClass);
    }

    /**
	 * Creates a renderer object using the specified property as an input value,
	 * and prClass as the renderer's class.
	 * 
	 * @param prop
	 * @param cls
	 */
    @SuppressWarnings("unchecked")
    private static BoundPropertyRenderer createRenderer(BoundProperty prop, Class cls) {
        try {
            Class[] clsses = { BoundProperty.class };
            return (BoundPropertyRenderer) cls.getConstructor(clsses).newInstance(new Object[] { prop });
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("Unable to create renderer for property: " + prop.getName() + ". Creating default renderer.");
        }
        return new DefaultRenderer(prop);
    }
}
