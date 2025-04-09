package org.itsnat.impl.comp.text;

import org.itsnat.comp.text.ItsNatFormattedTextField;
import java.lang.reflect.Constructor;
import java.text.ParseException;

/**
 *
 * @author jmarranz
 */
public class ItsNatFormatterDefaultImpl implements ItsNatFormattedTextField.ItsNatFormatter {

    /** Creates a new instance of ItsNatFormatterDefaultImpl */
    public ItsNatFormatterDefaultImpl() {
    }

    public Class getValueClass(ItsNatFormattedTextField comp) {
        Object value = comp.getValue();
        if (value != null) return value.getClass(); else return null;
    }

    public Object stringToValue(String str, ItsNatFormattedTextField comp) throws ParseException {
        Class vc = getValueClass(comp);
        if (vc != null) {
            Constructor cons;
            try {
                cons = vc.getConstructor(new Class[] { String.class });
            } catch (NoSuchMethodException nsme) {
                cons = null;
            }
            if (cons != null) {
                try {
                    return cons.newInstance(new Object[] { str });
                } catch (Exception ex) {
                    throw new ParseException("Error creating instance", 0);
                }
            }
        }
        return str;
    }

    public String valueToString(Object value, ItsNatFormattedTextField comp) throws ParseException {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}
