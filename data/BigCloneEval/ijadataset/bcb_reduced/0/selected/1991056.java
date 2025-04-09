package se.oktad.permgencleaner.util;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import se.oktad.permgencleaner.exception.PermGenCleanerException;
import se.oktad.permgencleaner.module.test.TestModuleThread;
import sun.misc.URLClassPath;

public class TestUtil {

    public static ClassLoader createClassLoader() {
        ClassLoader cl = null;
        try {
            Field field = null;
            Class cl_class = PermGenCleanerUtil.getCurrentClassLoader().getClass();
            while (field == null && cl_class != null) {
                try {
                    field = cl_class.getDeclaredField("ucp");
                } catch (NoSuchFieldException e1) {
                    cl_class = cl_class.getSuperclass();
                }
            }
            if (field == null) {
                throw new PermGenCleanerException("Could not find member variable ucp in ClassLoader!");
            }
            field.setAccessible(true);
            URLClassPath ucp = (URLClassPath) field.get(PermGenCleanerUtil.getCurrentClassLoader());
            cl = new URLClassLoader(ucp.getURLs(), null);
            field = null;
            cl_class = null;
            ucp = null;
        } catch (SecurityException e) {
            throw new PermGenCleanerException(e);
        } catch (IllegalArgumentException e) {
            throw new PermGenCleanerException(e);
        } catch (IllegalAccessException e) {
            throw new PermGenCleanerException(e);
        }
        return cl;
    }

    public static Thread createModuleThread(ClassLoader cl, Class memoryLeakerClass, Class cleanerClass) {
        Thread t;
        try {
            Class cls = cl.loadClass(TestModuleThread.class.getName());
            Constructor constr = cls.getConstructor(new Class[] { Class.class, Class.class });
            t = (Thread) constr.newInstance(new Object[] { memoryLeakerClass, cleanerClass });
            t.setContextClassLoader(cl);
        } catch (ClassNotFoundException e) {
            throw new PermGenCleanerException(e);
        } catch (InstantiationException e) {
            throw new PermGenCleanerException(e);
        } catch (IllegalAccessException e) {
            throw new PermGenCleanerException(e);
        } catch (SecurityException e) {
            throw new PermGenCleanerException(e);
        } catch (IllegalArgumentException e) {
            throw new PermGenCleanerException(e);
        } catch (NoSuchMethodException e) {
            throw new PermGenCleanerException(e);
        } catch (InvocationTargetException e) {
            throw new PermGenCleanerException(e);
        }
        return t;
    }

    public static boolean runThreadAndReturnTrueIfGarbageCollected(Class memoryLeakerClass) {
        return runThreadAndReturnTrueIfGarbageCollected(memoryLeakerClass, null);
    }

    public static boolean runThreadAndReturnTrueIfGarbageCollected(Class memoryLeakerClass, Class cleanerClass) {
        ClassLoader cl = TestUtil.createClassLoader();
        System.out.println(cl);
        Thread thread = TestUtil.createModuleThread(cl, memoryLeakerClass, cleanerClass);
        System.out.println(cl);
        ReferenceQueue q = new ReferenceQueue();
        PhantomReference reference = new PhantomReference(cl, q);
        WeakReference weakReference = new WeakReference(cl);
        cl = null;
        thread.start();
        while (thread.isAlive()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
        thread = null;
        Reference ref = null;
        for (int i = 0; i < 30; i++) {
            System.out.println(i + 1);
            Runtime.getRuntime().gc();
            try {
                ref = q.remove(2000);
                System.out.println(weakReference.get());
            } catch (InterruptedException e) {
            }
            if (ref != null) {
                return true;
            }
        }
        return false;
    }

    public static Object initializeObject(String className) throws PermGenCleanerException {
        Object o;
        try {
            o = Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            throw new PermGenCleanerException(e);
        } catch (IllegalAccessException e) {
            throw new PermGenCleanerException(e);
        } catch (ClassNotFoundException e) {
            throw new PermGenCleanerException(e);
        }
        return o;
    }
}
