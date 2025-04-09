package net.frede.toolbox;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.log4j.Logger;

/**
 * very convenient class able to perform actions on an instance with string
 * descriptions. this class is able to create a new instance with its name as a
 * String and creation arguments. it is also possible to execute a method on an
 * object with the method name and its arguments
 * 
 * @author $author$
 * @version $Revision: 1.2 $
 */
public class Invoker {

    /**
	 * the logger that will log any abnormal outputs out of this instance.
	 */
    static Logger logger = Logger.getLogger(Invoker.class);

    /**
	 * default constructor
	 */
    private Invoker() {
    }

    /**
	 * Invokes a method on an object
	 * 
	 * @param referer
	 *            the object from which method will be initiated
	 * @param params
	 *            the params class array of the invoked Method
	 * 
	 * @return the object returned from the invocation
	 */
    public static Object invokeConstructor(Object referer, Object[] params) {
        Object back = null;
        Class cl = referer.getClass();
        back = invokeConstructor(cl, params);
        return back;
    }

    /**
	 * Invokes a method on an object
	 * 
	 * @param classe
	 *            the object from which method will be initiated
	 * @param params
	 *            the params class array of the invoked Method
	 * 
	 * @return the object returned from the invocation
	 */
    public static Object invokeConstructor(String classe, Object[] params) {
        Object back = null;
        try {
            Class cl = Class.forName(classe);
            back = invokeConstructor(cl, params);
        } catch (ClassNotFoundException e_cnf) {
            logger.error("class " + classe + " not found");
        }
        return back;
    }

    /**
	 * Invokes a method on an object
	 * 
	 * @param classe
	 *            the object from which method will be initiated
	 * @param classes
	 *            DOCUMENT ME!
	 * @param params
	 *            the params class array of the invoked Method
	 * 
	 * @return the object returned from the invocation
	 */
    public static Object invokeConstructor(String classe, Class[] classes, Object[] params) {
        Object back = null;
        try {
            Class cl = Class.forName(classe);
            back = invokeConstructor(cl, classes, params);
        } catch (ClassNotFoundException e_cnf) {
            logger.error("class " + classe + " not found");
        }
        return back;
    }

    /**
	 * Invokes a method on an object
	 * 
	 * @param cl
	 *            the object from which method will be initiated
	 * @param params
	 *            the params class array of the invoked Method
	 * 
	 * @return the object returned from the invocation
	 */
    public static Object invokeConstructor(Class cl, Object[] params) {
        Class[] classes = null;
        if (params != null) {
            classes = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                classes[i] = params[i].getClass();
            }
        }
        return invokeConstructor(cl, classes, params);
    }

    /**
	 * Invokes a method on an object
	 * 
	 * @param cl
	 *            the object from which method will be initiated
	 * @param classes
	 *            the classes of the params array
	 * @param params
	 *            the params class array of the invoked Method
	 * 
	 * @return the object returned from the invocation
	 * 
	 * @throws NullPointerException
	 *             DOCUMENT ME!
	 */
    public static Object invokeConstructor(Class cl, Class[] classes, Object[] params) {
        Object back = null;
        try {
            Constructor constructor = cl.getConstructor(classes);
            back = constructor.newInstance(params);
        } catch (NoSuchMethodException e_nsm) {
            StringBuffer sb = new StringBuffer();
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    sb.append(" - ");
                    sb.append(classes[i]);
                    sb.append(" ");
                    sb.append(params[i]);
                }
            }
            logger.error(cl + " has no constructor with specified params " + sb.toString());
            throw new NullPointerException(e_nsm.toString());
        } catch (SecurityException e_s) {
            logger.error(cl + " constructor rose a security problem");
        } catch (InstantiationException e_i) {
            logger.error("construction of " + cl + " generated an instanciation exception:" + " either an interface or an abstract class");
        } catch (IllegalArgumentException e_ia) {
            logger.error(cl + " has not a good type for  params");
        } catch (IllegalAccessException e_iacc) {
            logger.error(cl + " constructor is not accessible");
        } catch (InvocationTargetException e_it) {
            logger.error(cl + " generated an exception : " + e_it.getTargetException());
            e_it.getTargetException().printStackTrace();
        }
        return back;
    }

    /**
	 * Invokes a method on an object that has no parameters.
	 * 
	 * @param referer
	 *            the object from which method will be initiated
	 * @param methodName
	 *            the name of the method that will be called
	 * 
	 * @return the object returned from the invocation
	 */
    public static Object invokeMethod(Object referer, String methodName) {
        return invokeMethod(referer, methodName, null);
    }

    /**
	 * Invokes a method on an object that has no parameters.
	 * 
	 * @param classe
	 *            the class of the referer that is supposed to have knowledge of
	 *            the method
	 * @param referer
	 *            the object from which method will be initiated
	 * @param methodName
	 *            the name of the method that will be called
	 * 
	 * @return the object returned from the invocation
	 */
    public static Object invokeMethod(Class classe, Object referer, String methodName) {
        return invokeMethod(classe, referer, methodName, null, null);
    }

    public static Object invokeMethod(Class classe, String methodName) {
        return invokeMethod(classe, null, methodName, new Object[0], null);
    }

    /**
	 * Invokes a method on an object (currently Entry and ClientServices)
	 * 
	 * @param referer
	 *            the object from which method will be initiated
	 * @param methodName
	 *            the name of the method that will be called
	 * @param params
	 *            the params object array of the invoked Method
	 * 
	 * @return the object returned from the invocation
	 */
    public static Object invokeMethod(Object referer, String methodName, Object[] params) {
        Class[] classes = null;
        if (params != null) {
            classes = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                classes[i] = params[i].getClass();
            }
        }
        return invokeMethod(referer, methodName, params, classes);
    }

    /**
	 * Invokes a method on an object
	 * 
	 * @param referer
	 *            the object from which method will be initiated
	 * @param methodName
	 *            the name of the method that will be called
	 * @param params
	 *            the params object array of the invoked Method
	 * @param classes
	 *            the classes object array of the invoked Method
	 * 
	 * @return the object returned from the invocation
	 */
    public static Object invokeMethod(Object referer, String methodName, Object[] params, Class[] classes) {
        Class cl = referer.getClass();
        return invokeMethod(cl, referer, methodName, params, classes);
    }

    /**
	 * Invokes a method on an object
	 * 
	 * @param params
	 *            the params object array of the invoked Method
	 * @param referer
	 *            the object from which method will be initiated
	 * @param methodName
	 *            the name of the method that will be called
	 * @param classes
	 *            the classes object array of the invoked Method
	 * 
	 * @return the object returned from the invocation
	 */
    public static Object invokeMethod(Class classe, Object referer, String methodName, Object[] params, Class[] classes) {
        Object back = null;
        try {
            Method method = classe.getMethod(methodName, classes);
            back = invokeInner(referer, method, params);
        } catch (NoSuchMethodException e_nsm) {
            StringBuffer sb = new StringBuffer();
            if (params != null) {
                sb.append(" and params ");
                for (int i = 0; i < params.length; i++) {
                    sb.append(params[i].getClass());
                    sb.append(" ");
                }
            } else {
                sb.append(" and no parameters");
            }
            logger.error(classe + " has not a method with name " + methodName + sb.toString());
        } catch (SecurityException e_s) {
            logger.error(classe + "." + methodName + " rose a security problem");
        } catch (IOException e_io) {
            logger.error(classe + "." + methodName + " generated an input/output exception");
        }
        return back;
    }

    /**
	 * performs a method execution.
	 * 
	 * @param referer
	 *            the object that will perform the method
	 * @param method
	 *            the method to perform
	 * @param params
	 *            the parameters of the method to perform
	 * 
	 * @return the return value of the method
	 * 
	 * @throws IOException
	 *             if an I/O exception occurred while performing the method
	 */
    private static Object invokeInner(Object referer, Method method, Object[] params) throws IOException {
        Object back = null;
        try {
            back = method.invoke(referer, params);
        } catch (IllegalArgumentException e_ia) {
            StringBuffer sb = new StringBuffer();
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    sb.append(params[i].getClass());
                    sb.append(" ");
                }
            } else {
                sb.append(" no parameters");
            }
            logger.error(method.toString() + " has not arguments as " + sb.toString() + " for instance " + referer.getClass().getName() + " " + referer.toString());
        } catch (IllegalAccessException e_iacc) {
            logger.error(method.getName() + " is not accessible");
        } catch (InvocationTargetException e_it) {
            logger.error(method.getName() + " generated an exception on " + referer + " : ");
            e_it.getCause().printStackTrace();
        }
        return back;
    }
}
