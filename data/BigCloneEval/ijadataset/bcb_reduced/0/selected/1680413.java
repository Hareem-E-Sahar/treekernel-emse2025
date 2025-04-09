package dryven.view.engine.parser;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.InputSource;
import dryven.view.engine.component.ViewComponent;
import dryven.view.engine.component.annotations.ComponentStrategy;
import dryven.view.engine.component.metadata.ViewComponentMetaDataStrategy;

/** Maps namespaces to packages and does the lookup of components used in a viewfile */
public class ViewComponentLocator {

    private Map<String, String> _packages;

    public ViewComponentMetaDataStrategy resolveComponent(String namespace, String elementName) {
        if (_packages == null) {
            return null;
        }
        String packageName = _packages.get(namespace);
        if (packageName == null) {
            return null;
        }
        String className = convertToClassName(elementName, packageName);
        try {
            Class<?> type = Class.forName(className);
            if (ViewComponent.class.isAssignableFrom(type)) {
                ViewComponentMetaDataStrategy strategy = determineStrategyFromClass(type);
                return strategy;
            } else {
                throw new RuntimeException(String.format("%s does not inherit from %s", type.getName(), ViewComponent.class.getName()));
            }
        } catch (ClassNotFoundException e) {
            ViewComponentMetaDataStrategy fileStrategy = determineStrategyFromViewFile(packageName, elementName, "dry");
            if (fileStrategy == null) {
                fileStrategy = determineStrategyFromViewFile(packageName, elementName, "xml");
            }
            if (fileStrategy == null) {
                throw new RuntimeException(String.format("Element <%s:%s> corresponds to class %s but the class was not found. A corresponding view file was also not found.", namespace, elementName, className));
            } else {
                return fileStrategy;
            }
        }
    }

    protected ViewComponentMetaDataStrategy determineStrategyFromClass(Class<?> type) {
        Class<?> origType = type;
        do {
            ComponentStrategy strategyAnno = type.getAnnotation(ComponentStrategy.class);
            if (strategyAnno != null) {
                Class<?> strategyClass = strategyAnno.strategy();
                try {
                    Constructor<?> ctor = strategyClass.getConstructor(Class.class);
                    ViewComponentMetaDataStrategy strategy = (ViewComponentMetaDataStrategy) ctor.newInstance(new Object[] { origType });
                    return strategy;
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e.getCause());
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(String.format("All classes that implement %s must have a one argument constructor that accepts a %s. This does not seem to be the case for %s", ViewComponentMetaDataStrategy.class.getName(), Class.class.getName(), strategyClass.getName()), e);
                } catch (Exception e) {
                    throw new RuntimeException("error while instanciating strategy object", e);
                }
            }
            type = type.getSuperclass();
        } while (!type.equals(ViewComponent.class));
        throw new RuntimeException(String.format("It seems that the component %s or any of it's parent classes does not have a metadata strategy attached.", origType.getName()));
    }

    protected ViewComponentMetaDataStrategy determineStrategyFromViewFile(String packageName, String elementName, String extension) {
        String resourceName = "/" + packageName.replace('.', '/') + "/" + elementName + "." + extension;
        InputStream stream = getClass().getResourceAsStream(resourceName);
        try {
            stream.close();
        } catch (IOException e) {
        }
        if (stream == null) {
            return null;
        } else {
            return null;
        }
    }

    public void bindPackage(String namespace, String packageName) {
        if (_packages == null) {
            _packages = new HashMap<String, String>();
        }
        _packages.put(namespace, packageName);
    }

    private String convertToClassName(String elementName, String packageName) {
        return packageName + "." + elementName.substring(0, 1).toUpperCase() + elementName.substring(1);
    }
}
