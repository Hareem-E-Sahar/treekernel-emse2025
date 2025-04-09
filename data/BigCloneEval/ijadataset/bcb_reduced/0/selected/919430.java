package org.maveryx.jviewer.viewMap;

import static org.maveryx.jviewer.viewer.SnapshotNamespaceInterface.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.jdom.Attribute;
import org.jdom.Element;
import org.maveryx.jviewer.viewer.JViewer;

/**
 * Definisce la descrizione dei valori numerici di un componente.
 * @author Giacomo Perreca
 */
public class ValueRange {

    protected Element el;

    /**
	 * Costruisce una descrizione dei valori numerici di un componente.
	 * @param element Elemento della snapshot creata con JViewer.
	 */
    public ValueRange(Element element) {
        super();
        if (element == null) throw new IllegalArgumentException("null element");
        if (element.getName().compareTo(TAG_ACCESSIBLE_VALUE) != 0) throw new IllegalArgumentException("element not a " + TAG_ACCESSIBLE_VALUE);
        el = element;
        return;
    }

    /**
	 * Fornisce il valore corrente del componente.
	 * @return Valore corrente.
	 */
    public Number current() {
        return getValue(TAG_CURRENT_VALUE);
    }

    /**
	 * Fornisce il valore massimo del componente.
	 * @return Valore massimo.
	 */
    public Number max() {
        return getValue(TAG_MAX_VALUE);
    }

    /**
	 * Fornisce il valore minimo del componente.
	 * @return Valore minimo.
	 */
    public Number min() {
        return getValue(TAG_MIN_VALUE);
    }

    private Number getValue(String tag) {
        Number curr;
        try {
            Element vv = (Element) el.getChild(tag);
            Attribute attr = vv.getAttribute(ATTRIBUTE_TYPE);
            Class type = Class.forName(attr.getValue());
            attr = vv.getAttribute(ATTRIBUTE_VALUE);
            Constructor constructor = type.getConstructor(new Class[] { String.class });
            curr = (Number) constructor.newInstance(new Object[] { attr.getValue() });
        } catch (ClassNotFoundException e) {
            curr = null;
        } catch (SecurityException e) {
            curr = null;
        } catch (NoSuchMethodException e) {
            curr = null;
        } catch (IllegalArgumentException e) {
            curr = null;
        } catch (InstantiationException e) {
            curr = null;
        } catch (InvocationTargetException e) {
            curr = null;
        } catch (IllegalAccessException e) {
            curr = null;
        }
        return curr;
    }
}
