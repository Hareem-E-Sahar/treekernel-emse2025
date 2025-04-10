package pl.psnc.dl.ege.configuration;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.java.plugin.JpfException;
import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginClassLoader;
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.Extension.Parameter;
import org.java.plugin.standard.StandardPluginLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import pl.psnc.dl.ege.component.ConfigurableConverter;
import pl.psnc.dl.ege.component.Converter;
import pl.psnc.dl.ege.component.NamedConverter;
import pl.psnc.dl.ege.component.Recognizer;
import pl.psnc.dl.ege.component.Validator;
import pl.psnc.dl.ege.exception.EGEException;
import pl.psnc.dl.ege.utils.EGEIOUtils;
import pl.psnc.dl.ege.utils.IOResolver;
import pl.psnc.dl.ege.utils.ZipIOResolver;

/**
 * Configuration manager performs initialization of standard components: loads
 * JPF based extensions : Validators, Converters, Recognizers.
 * 
 * Implemented as Singleton.
 * 
 * @author mariuszs
 */
public class EGEConfigurationManager {

    private static final String EXTENSION_POINT_ID = "pl.psnc.dl.ege.root";

    private static final String DEFAULT_CONVERTER_NAME = "Nameless";

    private final PluginManager pluginManager;

    private DocumentBuilder documentBuilder;

    private static final Logger LOGGER = Logger.getLogger(EGEConfigurationManager.class.getName());

    private final IOResolver ioResolver;

    private static class EGEConfigurationManagerHolder {

        private static final EGEConfigurationManager INSTANCE = new EGEConfigurationManager();
    }

    private EGEConfigurationManager() {
        ioResolver = new ZipIOResolver(1);
        LOGGER.debug("EGEConfigurationManager construct...");
        pluginManager = ObjectFactory.newInstance().createManager();
        List<PluginLocation> plugins = new ArrayList<PluginLocation>();
        plugins.addAll(getLocationsList("META-INF/plugin.xml"));
        plugins.addAll(getLocationsList("plugin.xml"));
        plugins.addAll(getLocationsList("/META-INF/plugin.xml"));
        try {
            pluginManager.publishPlugins(plugins.toArray(new PluginLocation[0]));
        } catch (JpfException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
	 * Returns available instance of <code>EGEConfigurationManager</code>.
	 * 
	 * @return instance of manager
	 */
    public static EGEConfigurationManager getInstance() {
        return EGEConfigurationManagerHolder.INSTANCE;
    }

    private List<PluginLocation> getLocationsList(String dirRegex) {
        List<PluginLocation> pluginsLocationsList = new ArrayList<PluginLocation>();
        final ClassLoader myClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final Enumeration<URL> locs = myClassLoader.getResources(dirRegex);
            int counter = 0;
            while (locs.hasMoreElements()) {
                final URL location = locs.nextElement();
                String matchString = "^(jar:[a-zA-Z][a-zA-Z0-9\\+\\-\\.]*:.*\\!/).*plugin.xml.*{1}$";
                Pattern pattern = Pattern.compile(matchString);
                Matcher matcher = pattern.matcher(location.toExternalForm());
                LOGGER.debug("Found : " + location.toExternalForm());
                if (!matcher.find()) {
                    continue;
                }
                try {
                    String pluginDirectory = getDirectoryName(location, "plugin" + counter);
                    File directory = unpackZIP(location, pluginDirectory);
                    if (directory == null) {
                        continue;
                    }
                    pluginsLocationsList.add(createLocation(directory, findFile(directory, "plugin.xml")));
                } catch (Exception e) {
                    LOGGER.error("Could not load jar from URL: " + location != null ? location.toExternalForm() : "null", e);
                }
                counter++;
            }
        } catch (IOException e) {
            LOGGER.error("Could not find resources", e);
        }
        return pluginsLocationsList;
    }

    private String getDirectoryName(URL location, String defaultName) {
        String pluginDirectory = defaultName;
        try {
            if (documentBuilder == null) {
                documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            }
            Document document = documentBuilder.parse(location.openStream());
            Element elem = document.getDocumentElement();
            if (elem != null) {
                NamedNodeMap attrs = elem.getAttributes();
                if (attrs != null) {
                    Node node = attrs.getNamedItem("id");
                    String nodeValue = node.getNodeValue();
                    if (nodeValue != null) {
                        pluginDirectory = nodeValue;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not retrieve directory name from config file", e);
        }
        return pluginDirectory;
    }

    private File unpackZIP(URL url, String dirName) {
        try {
            URLConnection connection = url.openConnection();
            if (connection instanceof JarURLConnection) {
                File destDir = new File(getExtensionsDirectory() + EGEConstants.fS + dirName);
                destDir.mkdirs();
                EGEIOUtils.unzipFile(((JarURLConnection) connection).getJarFile(), destDir);
                return destDir;
            }
        } catch (IOException e) {
            LOGGER.error("Could not unzip jar file.", e);
        }
        return null;
    }

    private PluginLocation createLocation(File directory, String manifest) throws MalformedURLException {
        return new StandardPluginLocation(directory, manifest);
    }

    private String findFile(File directory, String string) {
        if (new File(directory, string).exists()) {
            return string;
        }
        if (new File(directory, "META-INF" + EGEConstants.fS + string).exists()) {
            return "META-INF" + EGEConstants.fS + string;
        }
        return null;
    }

    public File getExtensionsDirectory() {
        File dataDirectory = new File(EGEConstants.EGE_EXT_DIRECTORY);
        if (!dataDirectory.exists()) {
            dataDirectory.mkdir();
        }
        return dataDirectory;
    }

    /**
	 * Returns list of all available converters.
	 * 
	 * @return list of converters.
	 */
    public List<Converter> getAvailableConverters() {
        List<Converter> convs = new ArrayList<Converter>();
        ExtensionPoint ep = pluginManager.getRegistry().getExtensionPoint(EXTENSION_POINT_ID, "Converter");
        ExtensionPoint ep2 = pluginManager.getRegistry().getExtensionPoint(EXTENSION_POINT_ID, "XslConverter");
        List<PluginWrapper> plugs = getAllComponents(ep);
        plugs.addAll(getAllComponents(ep2));
        for (PluginWrapper e : plugs) {
            try {
                NamedConverter nc;
                Collection<Parameter> params = (Collection<Parameter>) e.getParams();
                if (e.getPlugin() instanceof ConfigurableConverter) {
                    String name = DEFAULT_CONVERTER_NAME;
                    Map<String, String> prms = new HashMap();
                    ConfigurableConverter cc = (ConfigurableConverter) e.getPlugin();
                    for (Parameter param : params) {
                        if (param.getId().equals("name")) {
                            name = param.valueAsString();
                        }
                        prms.put(param.getId(), param.valueAsString());
                    }
                    cc.configure(prms);
                    nc = new NamedConverter((Converter) cc, name);
                    convs.add(nc);
                } else {
                    boolean named = false;
                    for (Parameter param : params) {
                        if (param.getId().equals("name")) {
                            nc = new NamedConverter((Converter) e.getPlugin(), param.valueAsString());
                            named = true;
                            convs.add(nc);
                            break;
                        }
                    }
                    if (!named) {
                        nc = new NamedConverter((Converter) e.getPlugin(), DEFAULT_CONVERTER_NAME);
                        convs.add(nc);
                    }
                }
            } catch (ClassCastException ex) {
                LOGGER.error("Provided class does not implement Converter interface", ex);
            } catch (EGEException ex) {
                LOGGER.error(ex.getMessage() + " Plugin is not loaded.");
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage() + " Plugin is not loaded.");
            }
        }
        return convs;
    }

    /**
	 * Returns list of all available recognizers.
	 * 
	 * @return list of recognizers.
	 */
    public List<Recognizer> getAvailableRecognizers() {
        List<Recognizer> recogs = new ArrayList<Recognizer>();
        ExtensionPoint ep = pluginManager.getRegistry().getExtensionPoint(EXTENSION_POINT_ID, "Recognizer");
        List<PluginWrapper> plugs = getAllComponents(ep);
        for (PluginWrapper e : plugs) {
            try {
                recogs.add((Recognizer) e.getPlugin());
            } catch (ClassCastException ex) {
                LOGGER.debug("Provided class is not a Recognizer", ex);
                continue;
            }
        }
        return recogs;
    }

    /**
	 * Returns list of all available validators.
	 * 
	 * @return list of validators.
	 */
    public List<Validator> getAvailableValidators() {
        List<Validator> plugins = new ArrayList<Validator>();
        ExtensionPoint ep = pluginManager.getRegistry().getExtensionPoint(EXTENSION_POINT_ID, "Validator");
        List<PluginWrapper> plugs = getAllComponents(ep);
        for (PluginWrapper e : plugs) {
            try {
                plugins.add((Validator) e.getPlugin());
            } catch (ClassCastException ex) {
                LOGGER.debug("Provided class is not a Validator", ex);
                continue;
            }
        }
        return plugins;
    }

    /**
	 * Returns standard EGE input/output resolver.
	 * 
	 * @return
	 */
    public IOResolver getStandardIOResolver() {
        return ioResolver;
    }

    private List<PluginWrapper> getAllComponents(ExtensionPoint ep) {
        List<PluginWrapper> plugins = new ArrayList();
        for (Iterator iter = ep.getConnectedExtensions().iterator(); iter.hasNext(); ) {
            Extension element = (Extension) iter.next();
            try {
                pluginManager.activatePlugin(element.getDeclaringPluginDescriptor().getId());
                PluginClassLoader classLoader = pluginManager.getPluginClassLoader(element.getDeclaringPluginDescriptor());
                Class mf = classLoader.loadClass(element.getParameter("class").valueAsString());
                Object plugin = mf.newInstance();
                PluginWrapper pw = new PluginWrapper(plugin, element.getParameters());
                plugins.add(pw);
            } catch (ClassNotFoundException e) {
                LOGGER.debug("Plugin class has not been found", e);
                continue;
            } catch (ClassCastException e) {
                LOGGER.debug("Provided class is not an EGE Component", e);
                continue;
            } catch (InstantiationException e) {
                LOGGER.debug("Cannot instantiate EGE Component", e);
                continue;
            } catch (IllegalAccessException e) {
                LOGGER.debug("IllegalAccessException", e);
                continue;
            } catch (PluginLifecycleException e) {
                LOGGER.debug("PluginLifecycleException", e);
                continue;
            }
        }
        return plugins;
    }

    private static class PluginWrapper {

        private final Object plugin;

        private final Collection params;

        public PluginWrapper(Object o, Collection params) {
            this.plugin = o;
            this.params = params;
        }

        public Object getPlugin() {
            return plugin;
        }

        public Collection getParams() {
            return params;
        }
    }
}
