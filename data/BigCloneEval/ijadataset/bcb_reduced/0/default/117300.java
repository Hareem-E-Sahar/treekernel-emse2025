import SearchAlgorithms.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SearchAlgorithm extends Algorithm {

    private int[] tempArray;

    private Class loadedClass;

    public SearchAlgorithm(String name, String className) {
        super(name, className);
        try {
            loadedClass = loadClass("SearchAlgorithms." + this.getClassName() + "");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
	 * Give the necessary data to run the algorithm
	 * @param array
	 */
    public void prepareForRun(int[] array) {
        this.tempArray = array;
    }

    public void run(int[] array) {
        Method meth;
        int needle = 1;
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

    @Override
    public void run(int number) {
    }

    @Override
    public void run(int[] array, int needle) {
    }
}
