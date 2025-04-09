package org.dspace.core;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Array;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * The Plugin Manager is a very simple component container.  It creates and
 * organizes components (plugins), and helps select a plugin in the cases
 * where there are many possible choices.  It also gives some limited
 * control over the lifecycle of a plugin.  It manages three different types
 * (usage patterns) of plugins:
 * <p>
 * <ol><li> Singleton Plugin
 * <br>  There is only one implementation class for the plugin.  It is indicated
 *   in the configuration.  This type of plugin chooses an implementations of
 *   a service, for the entire system, at configuration time.  Your
 *   application just fetches the plugin for that interface and gets the
 *   configured-in choice.
 *
 * <p><li> Sequence Plugins
 *  <br> You need a sequence or series of plugins, to implement a mechanism like
 *   StackableAuthenticationMethods or a pipeline, where each plugin is
 *   called in order to contribute its implementation of a process to the
 *   whole.
 *  <p><li> Named Plugins
 *  <br> Use a named plugin when the application has to choose one plugin
 *   implementation out of many available ones.  Each implementation is bound
 *   to one or more names (symbolic identifiers) in the configuration.
 *  </ol><p>
 *  The name is just a <code>String</code> to be associated with the
 *  combination of implementation class and interface.  It may contain
 *  any characters except for comma (,) and equals (=).  It may contain
 *  embedded spaces.  Comma is a special character used to separate
 *  names in the configuration entry.
 *
 * @author Larry Stone
 * @version $Revision: 1470 $
 * @see SelfNamedPlugin
 */
public class PluginManager {

    /** log4j category */
    private static Logger log = Logger.getLogger(PluginManager.class);

    /**
     * Prefixes of names of properties to look for in DSpace Configuration
     */
    private static final String SINGLE_PREFIX = "plugin.single.";

    private static final String SEQUENCE_PREFIX = "plugin.sequence.";

    private static final String NAMED_PREFIX = "plugin.named.";

    private static final String SELFNAMED_PREFIX = "plugin.selfnamed.";

    private static final String REUSABLE_PREFIX = "plugin.reusable.";

    private static final String SEP = "\034";

    private static HashMap cacheMeCache = new HashMap();

    private static boolean cacheMe(Class implClass) {
        if (cacheMeCache.containsKey(implClass)) {
            return ((Boolean) cacheMeCache.get(implClass)).booleanValue();
        } else {
            String key = REUSABLE_PREFIX + implClass.getName();
            boolean reusable = ConfigurationManager.getBooleanProperty(key, true);
            cacheMeCache.put(implClass, new Boolean(reusable));
            return reusable;
        }
    }

    /**
     * Returns an instance of the singleton (single) plugin implementing
     * the given interface.  There must be exactly one single plugin
     * configured for this interface, otherwise the
     * <code>PluginConfigurationError</code> is thrown.
     * <p>
     * Note that this is the only "get plugin" method which throws an
     * exception.  It is typically used at initialization time to set up
     * a permanent part of the system so any failure is fatal.
     *
     * @param interfaceClass interface Class object
     * @return instance of plugin
     * @throws PluginConfigurationError
     */
    public static Object getSinglePlugin(Class interfaceClass) throws PluginConfigurationError, PluginInstantiationException {
        String iname = interfaceClass.getName();
        String classname = ConfigurationManager.getProperty(SINGLE_PREFIX + iname);
        if (classname != null) return getAnonymousPlugin(classname.trim()); else throw new PluginConfigurationError("No Single Plugin configured for interface \"" + iname + "\"");
    }

    private static HashMap sequenceConfig = new HashMap();

    /**
     * Returns instances of all plugins that implement the interface
     * intface, in an Array.  Returns an empty array if no there are no
     * matching plugins.
     * <p>
     * The order of the plugins in the array is the same as their class
     * names in the configuration's value field.
     *
     * @param intfc interface for which to find plugins.
     * @return an array of plugin instances; if none are
     *   available an empty array is returned.
     */
    public static Object[] getPluginSequence(Class intfc) throws PluginInstantiationException {
        String iname = intfc.getName();
        String classname[] = null;
        if (!sequenceConfig.containsKey(iname)) {
            String val = ConfigurationManager.getProperty(SEQUENCE_PREFIX + iname);
            if (val == null) {
                log.warn("No Configuration entry found for Sequence Plugin interface=" + iname);
                return new Object[0];
            }
            classname = val.trim().split("\\s*,\\s*");
            sequenceConfig.put(iname, classname);
        } else classname = (String[]) sequenceConfig.get(iname);
        Object result[] = (Object[]) Array.newInstance(intfc, classname.length);
        for (int i = 0; i < classname.length; ++i) {
            log.debug("Adding Sequence plugin for interface= " + iname + ", class=" + classname[i]);
            result[i] = getAnonymousPlugin(classname[i]);
        }
        return result;
    }

    private static HashMap anonymousInstanceCache = new HashMap();

    private static Object getAnonymousPlugin(String classname) throws PluginInstantiationException {
        try {
            Class pluginClass = Class.forName(classname);
            if (cacheMe(pluginClass)) {
                Object cached = anonymousInstanceCache.get(pluginClass);
                if (cached == null) {
                    cached = pluginClass.newInstance();
                    anonymousInstanceCache.put(pluginClass, cached);
                }
                return cached;
            } else return pluginClass.newInstance();
        } catch (ClassNotFoundException e) {
            throw new PluginInstantiationException("Cannot load plugin class: " + e.toString(), e);
        } catch (InstantiationException e) {
            throw new PluginInstantiationException(e);
        } catch (IllegalAccessException e) {
            throw new PluginInstantiationException(e);
        }
    }

    private static HashMap namedPluginClasses = new HashMap();

    private static HashMap namedInstanceCache = new HashMap();

    private static void configureNamedPlugin(String iname) throws ClassNotFoundException {
        int found = 0;
        if (!namedPluginClasses.containsKey(iname)) {
            String namedVal = ConfigurationManager.getProperty(NAMED_PREFIX + iname);
            if (namedVal != null) {
                namedVal = namedVal.trim();
                log.debug("Got Named configuration for interface=" + iname + ", config=" + namedVal);
                Pattern classnameEqual = Pattern.compile("([\\w\\p{Sc}\\.]+)\\s*\\=");
                int prevEnd = -1;
                String prevClassName = null;
                Matcher classMatcher = classnameEqual.matcher(namedVal);
                while (classMatcher.find()) {
                    if (prevClassName != null) found += installNamedConfigs(iname, prevClassName, namedVal.substring(prevEnd, classMatcher.start()).trim().split("\\s*,\\s*"));
                    prevClassName = classMatcher.group(1);
                    prevEnd = classMatcher.end();
                }
                if (prevClassName != null) found += installNamedConfigs(iname, prevClassName, namedVal.substring(prevEnd).trim().split("\\s*,\\s*"));
            }
            String selfNamedVal = ConfigurationManager.getProperty(SELFNAMED_PREFIX + iname);
            if (selfNamedVal != null) {
                String classnames[] = selfNamedVal.trim().split("\\s*,\\s*");
                for (int i = 0; i < classnames.length; ++i) {
                    try {
                        Class pluginClass = Class.forName(classnames[i]);
                        String names[] = (String[]) pluginClass.getMethod("getPluginNames", null).invoke(null, null);
                        if (names == null || names.length == 0) log.error("Self-named plugin class \"" + classnames[i] + "\" returned null or empty name list!"); else found += installNamedConfigs(iname, classnames[i], names);
                    } catch (NoSuchMethodException e) {
                        log.error("Implementation Class \"" + classnames[i] + "\" is not a subclass of SelfNamedPlugin, it has no getPluginNames() method.");
                    } catch (Exception e) {
                        log.error("While configuring self-named plugin: " + e.toString());
                    }
                }
            }
            namedPluginClasses.put(iname, "org.dspace.core.marker");
            if (found == 0) log.error("No named plugins found for interface=" + iname);
        }
    }

    private static int installNamedConfigs(String iname, String classname, String names[]) throws ClassNotFoundException {
        int found = 0;
        for (int i = 0; i < names.length; ++i) {
            String key = iname + SEP + names[i];
            if (namedPluginClasses.containsKey(key)) log.error("Name collision in named plugin, implementation class=\"" + classname + "\", name=\"" + names[i] + "\""); else namedPluginClasses.put(key, classname);
            log.debug("Got Named Plugin, intfc=" + iname + ", name=" + names[i] + ", class=" + classname);
            ++found;
        }
        return found;
    }

    /**
     * Returns an instance of a plugin that implements the interface
     * intface and is bound to a name matching name.  If there is no
     * matching plugin, it returns null.  The names are matched by
     * String.equals().
     *
     * @param intfc the interface class of the plugin
     * @param name under which the plugin implementation is configured.
     * @return instance of plugin implementation, or null if there is no match or an error.
     */
    public static Object getNamedPlugin(Class intfc, String name) throws PluginInstantiationException {
        try {
            String iname = intfc.getName();
            configureNamedPlugin(iname);
            String key = iname + SEP + name;
            String cname = (String) namedPluginClasses.get(key);
            if (cname == null) log.warn("Cannot find named plugin for interface=" + iname + ", name=\"" + name + "\""); else {
                Class pluginClass = Class.forName(cname);
                if (cacheMe(pluginClass)) {
                    String nkey = pluginClass.getName() + SEP + name;
                    Object cached = namedInstanceCache.get(nkey);
                    if (cached == null) {
                        log.debug("Creating cached instance of: " + cname + " for interface=" + iname + " pluginName=" + name);
                        cached = pluginClass.newInstance();
                        if (cached instanceof SelfNamedPlugin) ((SelfNamedPlugin) cached).setPluginInstanceName(name);
                        namedInstanceCache.put(nkey, cached);
                    }
                    return cached;
                } else {
                    log.debug("Creating UNcached instance of: " + cname + " for interface=" + iname + " pluginName=" + name);
                    Object result = pluginClass.newInstance();
                    if (result instanceof SelfNamedPlugin) ((SelfNamedPlugin) result).setPluginInstanceName(name);
                    return result;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new PluginInstantiationException("Cannot load plugin class: " + e.toString(), e);
        } catch (InstantiationException e) {
            throw new PluginInstantiationException(e);
        } catch (IllegalAccessException e) {
            throw new PluginInstantiationException(e);
        }
        return null;
    }

    /**
     * Returns all of the names under which a named plugin implementing
     * the interface intface can be requested (with getNamedPlugin()).
     * The array is empty if there are no matches.  Use this to populate
     * a menu of plugins for interactive selection, or to document what
     * the possible choices are.
     * <p>
     * NOTE: The names are NOT returned in any deterministic order.
     *
     * @param intfc plugin interface for which to return names.
     * @return an array of strings with every name; if none are
     *   available an empty array is returned.
     */
    public static String[] getAllPluginNames(Class intfc) {
        try {
            String iname = intfc.getName();
            configureNamedPlugin(iname);
            String prefix = iname + SEP;
            ArrayList result = new ArrayList();
            Iterator ki = namedPluginClasses.keySet().iterator();
            while (ki.hasNext()) {
                String key = (String) ki.next();
                if (key.startsWith(prefix)) result.add(key.substring(prefix.length()));
            }
            if (result.size() == 0) log.error("Cannot find any names for named plugin, interface=" + iname);
            return (String[]) result.toArray(new String[result.size()]);
        } catch (ClassNotFoundException e) {
            return new String[0];
        }
    }

    /**
     * Tells the Plugin Manager to let go of any references to a
     * reusable plugin, to prevent it from being given out again and to
     * allow the object to be garbage-collected.  Call this when a
     * plugin instance must be taken out of circulation.
     *
     * @param plugin the object to release, must have been created by
     *   <code>getNamedPlugin</code> etc.
     */
    public static void releasePlugin(Object plugin) {
        forgetInstance(plugin, namedInstanceCache);
        forgetInstance(plugin, anonymousInstanceCache);
    }

    private static void forgetInstance(Object plugin, Map cacheMap) {
        Collection values = cacheMap.values();
        Iterator ci = values.iterator();
        while (ci.hasNext()) {
            Object val = ci.next();
            if (val == plugin) values.remove(val);
        }
    }

    private static boolean checkClassname(String iname, String msg) {
        try {
            Class intf = Class.forName(iname);
            return true;
        } catch (ClassNotFoundException ce) {
            log.error("No class definition found for " + msg + ": \"" + iname + "\"");
        }
        return false;
    }

    private static boolean checkSelfNamed(String iname) {
        try {
            if (!checkSelfNamed(Class.forName(iname))) log.error("The class \"" + iname + "\" is NOT a subclass of SelfNamedPlugin but it should be!");
        } catch (ClassNotFoundException ce) {
            log.error("No class definition found for self-named class interface: \"" + iname + "\"");
        }
        return false;
    }

    private static boolean checkSelfNamed(Class cls) {
        Class sup = cls.getSuperclass();
        if (sup == null) return false; else if (sup.equals(SelfNamedPlugin.class)) return true; else return checkSelfNamed(sup);
    }

    private static void checkNames(String iname) {
        try {
            configureNamedPlugin(iname);
        } catch (ClassNotFoundException ce) {
        }
    }

    /**
     * Validate the entries in the DSpace Configuration relevant to
     * PluginManager.  Look for inconsistencies, illegal syntax, etc.
     * Announce violations with "log.error" so they appear in the log
     * or in the standard error stream if this is run interactively.
     * <ul>
     * <li>Look for duplicate keys (by parsing the config file)
     * <li>Interface in plugin.single, plugin.sequence, plugin.named, plugin.selfnamed is valid.
     * <li>Classname in plugin.reusable exists and matches a plugin config.
     * <li>Classnames in config values exist.
     * <li>Classnames in plugin.selfnamed loads and is subclass of <code>SelfNamedPlugin</code>
     * <li>Implementations of named plugin have no name collisions.
     * <li>Named plugin entries lacking names.
     * </ul>
     */
    public static void checkConfiguration() throws IOException {
        Map singleKey = new HashMap();
        Map sequenceKey = new HashMap();
        Map namedKey = new HashMap();
        Map selfnamedKey = new HashMap();
        Map reusableKey = new HashMap();
        File config = ConfigurationManager.getConfigurationFile();
        BufferedReader cr = new BufferedReader(new FileReader(config));
        String line = null;
        boolean continued = false;
        HashMap keyMap = new HashMap();
        Pattern keyPattern = Pattern.compile("([^\\s\\=\\:]+)");
        while ((line = cr.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("!") || line.startsWith("#")) continued = false; else {
                if (!continued && line.startsWith("plugin.")) {
                    Matcher km = keyPattern.matcher(line);
                    if (km.find()) {
                        String key = line.substring(0, km.end(1));
                        if (keyMap.containsKey(key)) log.error("Duplicate key \"" + key + "\" in DSpace configuration file=" + config.toString()); else keyMap.put(key, key);
                        if (key.startsWith(SINGLE_PREFIX)) singleKey.put(key.substring(SINGLE_PREFIX.length()), key); else if (key.startsWith(SEQUENCE_PREFIX)) sequenceKey.put(key.substring(SEQUENCE_PREFIX.length()), key); else if (key.startsWith(NAMED_PREFIX)) namedKey.put(key.substring(NAMED_PREFIX.length()), key); else if (key.startsWith(SELFNAMED_PREFIX)) selfnamedKey.put(key.substring(SELFNAMED_PREFIX.length()), key); else if (key.startsWith(REUSABLE_PREFIX)) reusableKey.put(key.substring(REUSABLE_PREFIX.length()), key); else log.error("Key with unknown prefix \"" + key + "\" in DSpace configuration file=" + config.toString());
                    }
                }
                continued = line.length() > 0 && line.charAt(line.length() - 1) == '\\';
            }
        }
        Enumeration pne = ConfigurationManager.propertyNames();
        HashSet pn = new HashSet();
        while (pne.hasMoreElements()) {
            String nk = (String) pne.nextElement();
            if (nk.startsWith("plugin.")) {
                pn.add(nk);
                if (!keyMap.containsKey(nk)) log.error("Key is in ConfigurationManager.propertyNames() but NOT text crawl: \"" + nk + "\"");
            }
        }
        Iterator pi = keyMap.keySet().iterator();
        while (pi.hasNext()) {
            String key = (String) pi.next();
            if (!pn.contains(key)) log.error("Key is in text crawl but NOT ConfigurationManager.propertyNames(): \"" + key + "\"");
        }
        ArrayList allInterfaces = new ArrayList();
        allInterfaces.addAll(singleKey.keySet());
        allInterfaces.addAll(sequenceKey.keySet());
        allInterfaces.addAll(namedKey.keySet());
        allInterfaces.addAll(selfnamedKey.keySet());
        allInterfaces.addAll(reusableKey.keySet());
        Iterator ii = allInterfaces.iterator();
        while (ii.hasNext()) checkClassname((String) ii.next(), "key interface or class");
        Map allImpls = new HashMap();
        ii = singleKey.keySet().iterator();
        while (ii.hasNext()) {
            String key = (String) ii.next();
            String val = ConfigurationManager.getProperty(SINGLE_PREFIX + key);
            if (val == null) log.error("Single plugin config not found for: " + SINGLE_PREFIX + key); else {
                val = val.trim();
                if (checkClassname(val, "implementation class")) allImpls.put(val, val);
            }
        }
        ii = sequenceKey.keySet().iterator();
        while (ii.hasNext()) {
            String key = (String) ii.next();
            String val = ConfigurationManager.getProperty(SEQUENCE_PREFIX + key);
            if (val == null) log.error("Sequence plugin config not found for: " + SEQUENCE_PREFIX + key); else {
                val = val.trim();
                String classname[] = val.split("\\s*,\\s*");
                for (int i = 0; i < classname.length; ++i) if (checkClassname(classname[i], "implementation class")) allImpls.put(classname[i], classname[i]);
            }
        }
        ii = selfnamedKey.keySet().iterator();
        while (ii.hasNext()) {
            String key = (String) ii.next();
            String val = ConfigurationManager.getProperty(SELFNAMED_PREFIX + key);
            if (val == null) log.error("Selfnamed plugin config not found for: " + SELFNAMED_PREFIX + key); else {
                val = val.trim();
                String classname[] = val.split("\\s*,\\s*");
                for (int i = 0; i < classname.length; ++i) if (checkClassname(classname[i], "selfnamed implementation class")) {
                    allImpls.put(classname[i], classname[i]);
                    checkSelfNamed(classname[i]);
                }
                checkNames(key);
            }
        }
        ii = namedKey.keySet().iterator();
        Pattern classnameEqual = Pattern.compile("([\\w\\p{Sc}\\.]+)\\s*\\=");
        while (ii.hasNext()) {
            String key = (String) ii.next();
            String val = ConfigurationManager.getProperty(NAMED_PREFIX + key);
            if (val == null) log.error("Named plugin config not found for: " + NAMED_PREFIX + key); else {
                checkNames(key);
                val = val.trim();
                Matcher classMatcher = classnameEqual.matcher(val);
                while (classMatcher.find()) {
                    String classname = classMatcher.group(1);
                    if (checkClassname(classname, "implementation class")) allImpls.put(classname, classname);
                }
            }
        }
        Iterator ri = reusableKey.keySet().iterator();
        while (ri.hasNext()) {
            String rk = (String) ri.next();
            if (!(allImpls.containsKey(rk))) log.error("In plugin.reusable configuration, class \"" + rk + "\" is NOT a plugin implementation class.");
        }
    }

    /**
     * Invoking this class from the command line just runs
     * <code>checkConfiguration</code> and shows the results.
     * There are no command-line options.
     */
    public static void main(String[] argv) throws Exception {
        checkConfiguration();
    }
}
