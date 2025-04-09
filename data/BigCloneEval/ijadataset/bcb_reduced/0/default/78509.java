import java.lang.reflect.Method;

/**
 * implementation of a generic function pointer
 *
 * @author Andreas Ziermann
 *
 */
class AzFunctionPointer {

    private Object ob;

    private String foo;

    AzFunctionPointer(Object ob, String foo) {
        this.ob = ob;
        this.foo = foo;
    }

    void callVoidVoid() {
        try {
            final Method method = ob.getClass().getMethod(foo);
            method.invoke(ob);
        } catch (Exception e) {
            System.out.println("AzFunctionPointer_Exception: " + e);
        }
    }
}
