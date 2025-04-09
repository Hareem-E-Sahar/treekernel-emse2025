import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

public class MethodChainTest {

    public static void main(String[] args) {
        SimpleWrapper wrapper = new SimpleWrapper("mywrapper");
        log("main", " for (Object value : wrapper.callMe().andMeAlso().andFinally() )");
        for (Object value : wrapper.callMe().andMeAlso().andFinally()) {
            log("\tmain", "\t loop with value: " + value);
        }
        log("\n\nmain", "Class.forName() => " + SimpleWrapper.class.getName());
        try {
            Class klass = Class.forName("MethodChainTest$SimpleWrapper");
            Class[] constrpartypes = new Class[] { String.class };
            Constructor constr = klass.getConstructor(constrpartypes);
            Object dummyto = constr.newInstance(new String[] { "Java Programmer" });
            SimpleWrapper myWrapper = (SimpleWrapper) dummyto;
            myWrapper.setAge(28);
            log("myWrapper", myWrapper.getClass().getName() + " : " + myWrapper);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void log(String func, String msg) {
        System.out.println(func + "()" + msg);
    }

    static class SimpleWrapper {

        private int _age;

        private String _name;

        public SimpleWrapper(String name) {
            _name = name;
        }

        public void setAge(int age) {
            _age = age;
        }

        public String toString() {
            return _name + " @ " + _age;
        }

        public SimpleWrapper callMe() {
            log("\t" + getClass().getSimpleName() + "::callMe", "");
            return this;
        }

        public SimpleWrapper andMeAlso() {
            log("\t" + getClass().getSimpleName() + "::andMeAlso", "");
            return this;
        }

        public Simple andFinally() {
            log("\t" + getClass().getSimpleName() + "::andFinally", "");
            return new Simple();
        }
    }

    static class Simple implements Iterable<Object>, Iterator<Object> {

        private int size = 10;

        private int i = 0;

        public boolean hasNext() {
            log("\t" + getClass().getSimpleName() + "::hasNext", "");
            return i < size;
        }

        public Object next() {
            log("\t" + getClass().getSimpleName() + "::next", "");
            i++;
            return i;
        }

        public void remove() {
        }

        public Iterator<Object> iterator() {
            log("\t" + getClass().getSimpleName() + "::iterator", "");
            return this;
        }
    }
}
