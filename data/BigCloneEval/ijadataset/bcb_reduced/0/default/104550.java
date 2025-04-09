import java.lang.reflect.*;
import java.util.*;
import sun.misc.JIT;

class MicroBench {

    static int iterations = 5;

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("MicroBench {<benchmark>|all} [<iterations>]");
            return;
        }
        String benchmarkName = args[0];
        if (args.length == 2) {
            iterations = Integer.parseInt(args[1]);
        }
        new ImplementsMyInterface();
        new Child1();
        new Child2();
        Method[] methods;
        if (!benchmarkName.equals("all")) {
            methods = new Method[1];
            try {
                methods[0] = MicroBench.class.getDeclaredMethod("bench" + benchmarkName, null);
            } catch (NoSuchMethodException e) {
                methods[0] = null;
            }
            if (methods[0] == null) {
                System.err.println("Benchmark \"" + benchmarkName + "\" not found.");
                return;
            }
        } else {
            methods = MicroBench.class.getDeclaredMethods();
        }
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (!m.getName().startsWith("bench", 0)) {
                continue;
            }
            sun.misc.JIT.compileMethod(m, false);
            try {
                System.out.println("Starting " + m.getName() + "...");
                long starttime = System.currentTimeMillis();
                for (int j = 0; j < iterations; j++) {
                    m.invoke(null, null);
                }
                long totaltime = System.currentTimeMillis() - starttime;
                System.out.println("	Time spent: " + totaltime + " milliseconds.");
            } catch (Throwable e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public static void benchUByte() {
        int i = 50000000;
        int x;
        byte[] b = new byte[1];
        while (i > 0) {
            x = b[0] & 0xff;
            i--;
        }
    }

    public static void benchNew() {
        int i = 4000000;
        while (i > 0) {
            new Object();
            i--;
        }
    }

    public static void benchStringIndexOf() {
        int i = 600000;
        String str = "Find the very last char in this String";
        while (i > 0) {
            str.indexOf('g', 5);
            i--;
        }
    }

    public static void benchCheckInit() {
        int i = 4000000;
        while (i > 0) {
            StaticInitClass.staticMethod();
            i--;
        }
    }

    public static void benchArrayAssignmentCheckFastPath() {
        int i = 6600000;
        MicroBench o = new MicroBench();
        MicroBench[] array = new MicroBench[1];
        while (i > 0) {
            array[0] = o;
            i--;
        }
    }

    public static void benchArrayAssignmentCheckSlowPath() {
        int i = 2200000;
        Child1 o = new Child1();
        Parent[] array = new Parent[1];
        while (i > 0) {
            array[0] = o;
            i--;
        }
    }

    public static void benchArrayAssignmentCheckObjectPath() {
        int i = 5000000;
        MicroBench o = new MicroBench();
        Object[] array = new Object[1];
        while (i > 0) {
            array[0] = o;
            i--;
        }
    }

    public static void benchInvokeSync() {
        int i = 1500000;
        ImplementsMyInterface o = new ImplementsMyInterface();
        while (i > 0) {
            o.invokeSync();
            i--;
        }
    }

    public static void benchInvokeStaticSync() {
        int i = 1500000;
        ImplementsMyInterface o = new ImplementsMyInterface();
        while (i > 0) {
            o.invokeStaticSync();
            i--;
        }
    }

    public static void benchInvokeInterface() {
        int i = 2000000;
        MyInterface o = new ImplementsMyInterface();
        while (i > 0) {
            o.invokeinterface();
            i--;
        }
    }

    public static void benchInvokeInterfaceBadGuess() {
        int i = 2000000;
        MyInterface o1 = new ImplementsMyInterface();
        MyInterface o2 = new ImplementsMyInterface2();
        while (i > 0) {
            MyInterface o = o1;
            o.invokeinterface();
            i--;
            o1 = o2;
            o2 = o;
        }
    }

    public static void benchInstanceOf() {
        int i = 10000000;
        Object o = new MicroBench();
        while (i > 0) {
            if (o instanceof MicroBench) i--;
        }
    }

    public static void benchCheckCastGoodGuess() {
        int i = 10000000;
        Object o = new MicroBench();
        while (i > 0) {
            MicroBench m = (MicroBench) o;
            i--;
        }
    }

    public static void benchCheckCastBadGuess() {
        int i = 2200000;
        Object o1 = new Child1();
        Object o2 = new Child2();
        while (i > 0) {
            Parent m = (Parent) o1;
            i--;
            Object temp = o1;
            o1 = o2;
            o2 = temp;
        }
    }

    public static void benchMonitorEnterExit() {
        int i = 320000;
        Object o = new Object();
        while (i > 0) {
            synchronized (o) {
                i--;
            }
        }
    }
}

interface MyInterface {

    public void invokeinterface();
}

interface MyInterface2 {

    public void invokeinterface2();
}

interface MyInterface3 {

    public void invokeinterface3();
}

class ImplementsMyInterface implements MyInterface {

    public void invokeinterface() {
        return;
    }

    public synchronized void invokeSync() {
        return;
    }

    public static synchronized void invokeStaticSync() {
        return;
    }
}

class ImplementsMyInterface2 implements MyInterface2, MyInterface, MyInterface3 {

    public void invokeinterface() {
        return;
    }

    public void invokeinterface2() {
        return;
    }

    public void invokeinterface3() {
        return;
    }
}

class Parent {
}

class Child1 extends Parent {
}

class Child2 extends Parent {
}

class StaticInitClass {

    static {
        new Object();
    }

    static void staticMethod() {
    }
}
