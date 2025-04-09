package gnu.CORBA.CDR;

import java.lang.reflect.Constructor;

public class VMVio {

    /**
   * Allocates a new Object of type clazz but without running the default
   * constructor on it. It then calls the given constructor on it. The given
   * constructor method comes from the constr_clazz which is a super class of
   * the given clazz.
   */
    public static Object allocateObject(Class clazz, Class constr_clazz, Constructor constructor) throws InstantiationException {
        try {
            Constructor c = clazz.getConstructor(new Class[0]);
            c.setAccessible(true);
            return c.newInstance(new Object[0]);
        } catch (Exception ex) {
            try {
                Constructor c = clazz.getConstructor(new Class[] { String.class });
                return c.newInstance(new Object[] { "" });
            } catch (Exception ex2) {
                Constructor c[] = clazz.getConstructors();
                for (int i = 0; i < c.length; i++) {
                    try {
                        c[i].setAccessible(true);
                        Class[] args = c[i].getParameterTypes();
                        return c[i].newInstance(new Object[args.length]);
                    } catch (Exception ex3) {
                    }
                }
            }
            throw new InstantiationException(clazz.getName());
        }
    }
}
