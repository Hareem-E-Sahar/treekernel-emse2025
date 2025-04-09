import java.lang.reflect.Constructor;

/**
 * http://stackoverflow.com/questions/936684/getting-the-class-name-from-a-static-method-in-java
 * http://stackoverflow.com/questions/601395/getclass-and-static-methods-what-is-the-best-practice
 * 
 * Good article on Reflection
 * http://java.sun.com/developer/technicalArticles/ALT/Reflection/
 * 
 * http://en.wikibooks.org/wiki/Java_Programming/Reflection/Dynamic_Invocation
 */
public class ReflectionTest {

    public static void main(String[] args) {
        System.out.println("ChildOne.getStaticClass : " + ChildOne.getStaticClass().getName());
        System.out.println("ChildTwo.getStaticClass : " + ChildTwo.getStaticClass().getName());
        System.out.println("ChildOne.create : " + ChildOne.create());
        System.out.println("ChildTwo.create : " + ChildTwo.create());
    }
}

class Parent {

    public static Parent create(Class klass) {
        String className = klass.toString();
        System.out.println(className);
        try {
            Constructor constructor = klass.getConstructor(null);
            return (Parent) constructor.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<Parent> getStaticClass() {
        return reallyGetStaticClass();
    }

    public static Class<Parent> reallyGetStaticClass() {
        Throwable t = new Throwable();
        t.fillInStackTrace();
        StackTraceElement[] elements = t.getStackTrace();
        StackTraceElement element = t.getStackTrace()[1];
        Class klass = null;
        try {
            klass = Class.forName(element.getClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return klass;
    }
}

class ChildOne extends Parent {

    private static Class<ChildOne> __klass = ChildOne.class;

    private static Class<Parent> __staticClass;

    public static ChildOne create() {
        return (ChildOne) Parent.create(__klass);
    }

    public static Class<Parent> getStaticClass() {
        if (__staticClass == null) __staticClass = reallyGetStaticClass();
        return __staticClass;
    }

    public ChildOne() {
    }
}

class ChildTwo extends Parent {

    private static Class<ChildTwo> __klass = ChildTwo.class;

    private static Class<Parent> __staticClass;

    public static ChildTwo create() {
        return (ChildTwo) Parent.create(__klass);
    }

    public static Class<Parent> getStaticClass() {
        if (__staticClass == null) __staticClass = reallyGetStaticClass();
        return __staticClass;
    }

    public ChildTwo() {
    }
}
