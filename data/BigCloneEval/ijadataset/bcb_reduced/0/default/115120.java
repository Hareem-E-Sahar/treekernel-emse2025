import SearchAlgorithms.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class CustomAlgorithm extends Algorithm {

    private int[] tempArray;

    private Class loadedClass;

    private String customType;

    public static final int ALGORITHM_ARRAY = 1;

    public static final int ALGORITHM_ARRAYNUMBER = 2;

    public static final int ALGORITHM_NUMBER = 3;

    public CustomAlgorithm(String name, String javaFileName, String customType) {
        super(name, javaFileName);
        this.customType = customType;
        try {
            System.out.println("Start Compiling custom Algorithm");
            JCompiler.compile(this.getClassName(), getCurrentDir() + "");
            System.out.println("End Compiling custom Algorithm");
            try {
                URL classUrl;
                classUrl = new URL("file://" + getCurrentDir() + "/customAlgorithms/");
                System.out.println("cluassurl: " + classUrl.getPath());
                URL[] classUrls = { classUrl };
                URLClassLoader ucl = new URLClassLoader(classUrls);
                loadedClass = ucl.loadClass(this.getClassName());
                int[] array = new int[] { 3, 2, 1 };
                try {
                    Method meth = loadedClass.getMethod("run", int[].class);
                    meth.invoke(loadedClass.newInstance(), array);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(int[] array) {
        Method meth;
        try {
            meth = loadedClass.getMethod("run", int[].class);
            System.out.println("invoke method");
            meth.invoke(loadedClass.newInstance(), array);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(int number) {
        Method meth;
        try {
            meth = loadedClass.getMethod("run", int.class);
            meth.invoke(loadedClass.newInstance(), number);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(int[] array, int needle) {
        Method meth;
        try {
            meth = loadedClass.getMethod("run", int[].class, int.class);
            meth.invoke(loadedClass.newInstance(), array, needle);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public String getCustomType() {
        return this.customType;
    }

    public String getCurrentDir() {
        File file = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        return file.getParent();
    }
}
