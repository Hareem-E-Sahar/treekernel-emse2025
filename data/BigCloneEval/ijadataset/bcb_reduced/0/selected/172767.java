package simple.template.layout;

import java.lang.reflect.Constructor;
import simple.http.serve.Context;

/**
 * The <code>LayoutFactory</code> retrieves a <code>Layout</code>
 * implementation for the system. This is used so that an arbitrary
 * layout can be imposed on the system using the command line. If
 * an template is referenced then an instance is retrieved and the
 * layout of that template is handled by the retrieved implementation.
 * This has a number of advantages. For one it enables the layout
 * to be configured without any changes to code, so only the layout
 * configuration needs to be modified to change the layout used.
 * <p>
 * In order to define a system wide implementation a property is
 * needed to define the object. This uses the <code>System</code>
 * properties to define the class name for the default instance.
 * The property is the <code>simple.template.layout</code> 
 * property that can be set using an argument to the VM.
 * <pre> 
 * java -Dsimple.template.layout=demo.example.DemoLayout 
 * </pre>
 * This will set the <code>System</code> property to the class 
 * name <code>demo.example.DemoLayout</code>. When the factory
 * method <code>getInstance</code> is invoked it will return an
 * implementation of this object or if the implementation cannot
 * be loaded by this classes class loader a default implementation,
 * <code>TileLayout</code>, is returned instead. 
 * 
 * @author Niall Gallagher
 */
public final class LayoutFactory {

    /**
    * This is used to produce the system wide <code>Layout</code>
    * implementation so that a layout can be imposed on templates.
    * This will use the <code>simple.template.layout</code>
    * system property to define the class of the implementation 
    * that will be used for the system wide <code>Layout</code>.
    * The property should contain the fully qualified class name
    * of the object and should be loadable by this classes class 
    * loader. If the specified class cannot be loaded then the
    * <code>TileLayout</code> implementation is used.
    *
    * @param factory this is used to produce the document object
    * @param context this is the context used for configuration
    *
    * @return the systems <code>Layout</code> implementation
    */
    public static Layout getInstance(ViewerFactory factory, Context context) {
        return getInstance(new DefaultDocumentFactory(factory, context), context);
    }

    /**
    * This is used to produce the system wide <code>Layout</code>
    * implementation so that a layout can be imposed on templates.
    * This will use the <code>simple.template.layout</code>
    * system property to define the class of the implementation 
    * that will be used for the system wide <code>Layout</code>.
    * The property should contain the fully qualified class name
    * of the object and should be loadable by this classes class 
    * loader. If the specified class cannot be loaded then the
    * <code>TileLayout</code> implementation is used.
    *
    * @param factory this is used to produce the document object
    * @param context this is the context used for configuration
    *
    * @return the systems <code>Layout</code> implementation
    */
    public static Layout getInstance(DocumentFactory factory, Context context) {
        String property = "simple.template.layout";
        String className = System.getProperty(property);
        if (className == null) {
            return new TileLayout(factory, context);
        }
        try {
            Object[] list = new Object[] { factory, context };
            return getInstance(className, list);
        } catch (Exception e) {
            return new TileLayout(factory, context);
        }
    }

    /**
    * This is used to create a <code>Layout</code> instance with
    * the issued class name. The class name issued represents the
    * fully qualified package name of the layout implementation.
    * The implementation must contain a constructor that takes a 
    * <code>DocumentFactory</code> and a <code>Context</code>
    * object. If there is any problem an exception is thrown.
    * 
    * @param className this is the name of the implementation
    * @param list represents the arguments to the constructor
    *
    * @return an instance of the <code>Layout</code> object
    */
    private static Layout getInstance(String className, Object[] list) throws Exception {
        Constructor method = getConstructor(className);
        return (Layout) method.newInstance(list);
    }

    /**
    * Here a <code>ClassLoader</code> is selected to load the class.
    * This will load the class specified using the loader used to 
    * load this class. If there are no problems in loading the class
    * a <code>Constructor</code> is created from the loaded class.
    * <p>
    * The constructor for any <code>Layout</code> implementation
    * must contain a two argument constructor that takes in order
    * a <code>DocumentFactory</code> and a <code>Context</code>.
    * If no such constructor exists an exception is thrown.
    * 
    * @param className the name of the layout implementation 
    *
    * @return this returns a constructor for the specified class
    */
    private static Constructor getConstructor(String className) throws Exception {
        return getConstructor(Class.forName(className, false, LayoutFactory.class.getClassLoader()));
    }

    /**
    * This loads the class for the <code>Layout</code> and returns
    * the standard constructor for the implementation. This will
    * load the <code>Class</code> for the implementation provided
    * that that implementation has a two argument constructor that
    * takes a <code>DocumentFactory</code> and a <code>Context</code>
    * object. If no such constructor exists an exception is thrown.
    * 
    * @param type this is implementation that is to be instantiated
    *
    * @return the standardized object <code>Constructor</code> 
    *
    * @exception Exception if the constructor could not be created
    */
    private static Constructor getConstructor(Class type) throws Exception {
        return type.getDeclaredConstructor(new Class[] { DocumentFactory.class, Context.class });
    }
}
