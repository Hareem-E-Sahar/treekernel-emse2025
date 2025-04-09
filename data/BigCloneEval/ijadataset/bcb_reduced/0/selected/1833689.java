package jscore.gui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import jscore.model.objects.BarType;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author marco
 */
class BarFactory {

    private static Map<BarType, Class> bars = new HashMap<BarType, Class>();

    static {
        bars.put(BarType.NORMAL, BarSymbol.class);
        bars.put(BarType.START, StartBar.class);
        bars.put(BarType.END, EndBar.class);
        bars.put(BarType.ENDLINE, EndLineBarSymbol.class);
    }

    @SuppressWarnings(value = "unchecked")
    public static BarSymbol createNewBar(BarType barType, JScoreConfig config) throws InternalGuiException {
        BarSymbol symbol = null;
        try {
            Class symbolClass = bars.get(barType);
            if (symbolClass == null) {
                throw new InternalGuiException("Cannot instantiate bar type " + barType);
            }
            Constructor<BarSymbol> symConstructor = symbolClass.getConstructor(JScoreConfig.class);
            symbol = symConstructor.newInstance(config);
        } catch (InstantiationException ex) {
            throw new InternalGuiException("Cannot instantiate bar type " + barType, ex);
        } catch (IllegalAccessException ex) {
            throw new InternalGuiException("Cannot instantiate bar type " + barType, ex);
        } catch (IllegalArgumentException ex) {
            throw new InternalGuiException("Cannot instantiate bar type " + barType, ex);
        } catch (InvocationTargetException ex) {
            throw new InternalGuiException("Cannot instantiate bar type " + barType, ex);
        } catch (NoSuchMethodException ex) {
            throw new InternalGuiException("Cannot instantiate bar type " + barType, ex);
        } catch (SecurityException ex) {
            throw new InternalGuiException("Cannot instantiate bar type " + barType, ex);
        }
        return symbol;
    }
}
