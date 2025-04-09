package org.gaugebook;

import java.lang.reflect.Constructor;
import java.util.Vector;

/**
 * GaugeManager
 * 
 * The GaugeManager dynamically loads Gauges (deriving AbstractGauge).
 *  
 * @author Ants At War Team
 */
public class GaugeManager extends Object {

    protected Vector<AbstractGauge> gauges = new Vector<AbstractGauge>();

    public GaugeManager() {
        super();
    }

    /**
	 * 
	 */
    public static Object load(String strategy) {
        try {
            Class cl = Class.forName(strategy);
            boolean hasNoArgumentConstructor = false;
            Constructor[] constructors = cl.getConstructors();
            for (int i = 0; i < constructors.length; i++) {
                if (constructors[i].getParameterTypes().length == 0) {
                    hasNoArgumentConstructor = true;
                    break;
                }
            }
            if (!hasNoArgumentConstructor) return null;
            Object obj = cl.newInstance();
            return obj;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean validateStrategy(String strategy) {
        return (load(strategy) instanceof AbstractGauge);
    }

    public boolean loadGauge(String gauge) {
        Object obj = load(gauge);
        if (obj instanceof AbstractGauge) {
            AbstractGauge s = (AbstractGauge) obj;
            gauges.add(s);
            return true;
        } else {
            return false;
        }
    }

    public void clear() {
        gauges.removeAllElements();
    }

    public Object getProperty(AbstractGauge gauge, String property) {
        return gauge.getProperty(property);
    }

    public void setProperty(AbstractGauge gauge, String property, Object value) {
        gauge.setProperty(property, value);
    }

    public String toString() {
        return "(GaugeManager: " + gauges.size() + " strategies loaded)";
    }
}
