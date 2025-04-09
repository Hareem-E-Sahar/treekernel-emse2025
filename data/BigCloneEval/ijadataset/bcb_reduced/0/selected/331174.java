package net.sourceforge.ondex.plugin.introspect;

import net.sourceforge.ondex.plugin.*;
import net.sourceforge.ondex.plugin.validation.UseValidator;
import net.sourceforge.ondex.plugin.validation.Validator;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Introspection for plugins. This allows a <code>PluginInfo</code> data-structure to be manufactured for a plugin.
 * <p>
 * Use: <code>PluginInfo pInf = PluginIntrospector.getPluginInfo(pluginClass);</code>
 * </p>
 * 
 * @author Matthew Pocock
 */
public abstract class PluginIntrospector {

    /**
     * Method to actually perform the introspection.
     * <p>
     * This method should be implemented by anybody wishing to provide a custom plugin introspection scheme.
     * It is guaranteed that any class passed into here by a call to <code>getPluginInfo</code> will be annotated with
     * <code>@Plugin</code>. This method should not be called directly by user-code.
     *
     * @param pluginClass  the class of the plugin to introspect
     * @return  a <code>PluginInfo</code> summarising the plugin
     * @throws PluginIntrospectionException  if there was a problem introspecting the plugin class
     */
    public abstract PluginInfo introspect(Class<? extends OndexPlugin> pluginClass) throws PluginIntrospectionException;

    public static PluginInfo getPluginInfo(Class<? extends OndexPlugin> pluginClass) throws PluginIntrospectionException {
        return new Default().introspect(pluginClass);
    }

    public static class Default extends PluginIntrospector {

        @Override
        public PluginInfo introspect(Class<? extends OndexPlugin> pluginClass) throws PluginIntrospectionException {
            Plugin p = pluginClass.getAnnotation(Plugin.class);
            if (p == null) {
                throw new PluginIntrospectionException("Can not generate a PluginInfo for a class that is not annotated with @Plugin: " + pluginClass);
            }
            PluginIntrospector pi;
            try {
                pi = p.introspector().newInstance();
            } catch (InstantiationException e) {
                throw new PluginIntrospectionException("Could not create introspector for: " + pluginClass);
            } catch (IllegalAccessException e) {
                throw new PluginIntrospectionException("Could not create introspector for: " + pluginClass);
            }
            return pi.introspect(pluginClass);
        }
    }

    /**
     * The default introspector implementation.
     *
     * @author Matthew Pocock
     */
    public static class Bean extends PluginIntrospector {

        @Override
        public PluginInfo introspect(Class<? extends OndexPlugin> pluginClass) throws PluginIntrospectionException {
            BeanInfo bi;
            try {
                bi = Introspector.getBeanInfo(pluginClass);
            } catch (IntrospectionException e) {
                throw new PluginIntrospectionException("Could not get bean info for plugin class: " + pluginClass, e);
            }
            Plugin p = pluginClass.getAnnotation(Plugin.class);
            String name = p.name().equals("") ? bi.getBeanDescriptor().getName() : p.name();
            String description = p.description();
            String version = p.version();
            PluginType type = p.type();
            List<ArgumentDescriptor> arguments = new ArrayList<ArgumentDescriptor>();
            List<ProvidedDescriptor> provided = new ArrayList<ProvidedDescriptor>();
            List<ConsumedDescriptor> consumed = new ArrayList<ConsumedDescriptor>();
            for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
                Method setter = pd.getWriteMethod();
                if (setter != null) {
                    PluginArgument arg = setter.getAnnotation(PluginArgument.class);
                    if (arg != null) {
                        List<Validator> validators = new ArrayList<Validator>();
                        for (Annotation a : setter.getAnnotations()) {
                            UseValidator uv = a.getClass().getAnnotation(UseValidator.class);
                            if (uv != null) {
                                try {
                                    validators.add(uv.value().getConstructor(a.getClass()).newInstance(a));
                                } catch (NoSuchMethodException e) {
                                    throw new PluginIntrospectionException("Could not process validator: " + uv + " for " + a + " on " + setter.getName() + " for plugin " + name, e);
                                } catch (InvocationTargetException e) {
                                    throw new PluginIntrospectionException("Could not process validator: " + uv + " for " + a + " on " + setter.getName() + " for plugin " + name, e);
                                } catch (InstantiationException e) {
                                    throw new PluginIntrospectionException("Could not process validator: " + uv + " for " + a + " on " + setter.getName() + " for plugin " + name, e);
                                } catch (IllegalAccessException e) {
                                    throw new PluginIntrospectionException("Could not process validator: " + uv + " for " + a + " on " + setter.getName() + " for plugin " + name, e);
                                }
                            }
                        }
                        if (!validators.isEmpty() && pd.getReadMethod() == null) {
                            throw new PluginIntrospectionException("Unable to configure plugin " + name + " as property " + pd.getName() + " is annotated with a valiator, but has no getter method");
                        }
                        arguments.add(new ArgumentDescriptor(arg.name().equals("") ? pd.getName() : arg.name(), arg.description(), arg.optional(), arg.defaultValue(), validators, pd));
                    }
                    ProvidedByWorkflow pbw = setter.getAnnotation(ProvidedByWorkflow.class);
                    if (pbw != null) {
                        provided.add(new ProvidedDescriptor(pbw.name().equals("") ? pd.getName() : pbw.name(), pbw.description(), pd));
                    }
                }
                Method getter = pd.getReadMethod();
                if (getter != null) {
                    ConsumedByWorkflow cbw = getter.getAnnotation(ConsumedByWorkflow.class);
                    if (cbw != null) {
                        consumed.add(new ConsumedDescriptor(cbw.name().equals("") ? pd.getName() : cbw.name(), cbw.description(), pd));
                    }
                }
            }
            return new PluginInfo(name, description, version, type, bi.getBeanDescriptor(), arguments, provided, consumed);
        }
    }
}
