import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.io.*;

/**
 * This is a class designed to test if the class loader can run concurrently to other threads.
 * It uses a class loader that delays class loading, and a thread that runs concurrently to it.
 *
 * @author Mathias Ricken
 */
public class ConcLoaderTest {

    public static void main(String[] args) {
        final boolean[] running = new boolean[] { true };
        Thread t = new Thread(new Runnable() {

            public void run() {
                while (running[0]) {
                    System.out.println("Thread running...");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
        DelayingClassLoader dcl = new DelayingClassLoader();
        try {
            Class<?> c = dcl.loadClass("ConcLoaderClient", true);
            Object i = c.getConstructor().newInstance();
            Method m = c.getMethod("doSomething");
            m.invoke(i);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        running[0] = false;
    }
}

class YetAnotherClass {

    public void doSomethingElse() {
        System.out.println("Yet another class is running...");
        BigInteger i = new BigInteger("abcd", 16);
        BigInteger j = new BigInteger("1234", 8);
        BigInteger k = i.add(j);
        System.out.println(i + " + " + j + " = " + k);
        System.out.println("Yet another class finishes...");
    }
}

class DelayingClassLoader extends ClassLoader {

    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        System.out.println("Load class: " + name);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Class c = findLoadedClass(name);
        if (c == null) {
            try {
                String fileName = name.replace('.', File.separatorChar) + ".class";
                try {
                    InputStream is = new FileInputStream(fileName);
                    try {
                        byte[] barr = new byte[is.available()];
                        int offset = 0, bytesRead;
                        while ((offset < barr.length) && ((bytesRead = is.read(barr, offset, barr.length - offset)) != -1)) {
                            offset += bytesRead;
                        }
                        c = defineClass(name, barr, 0, barr.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    c = findSystemClass(name);
                }
            } catch (ClassFormatError e) {
                System.err.println(e.getMessage());
                throw new ClassNotFoundException(name + ":" + e.getMessage(), e);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        System.out.println("Class loaded: " + name);
        ;
        return c;
    }
}
