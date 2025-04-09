package mfb2.tools.obclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mfb2.tools.obclipse.exceptions.ObclipseException;
import mfb2.tools.obclipse.util.PropertiesUtil;
import mfb2.tools.obclipse.util.PropertyHandler;
import mfb2.tools.obclipse.util.SystemInfo;

public class ObclipseProps {

    public static final String PACKAGE_PATHS = "packagePaths";

    public static final String ADDITIONAL_FILES_TO_FIX = "additionalFilesToFix";

    public static final String XML_FILES_TO_FIX_FILTER = "xmlFilesToFixFilter";

    public static final String OB_DEFAULT_PACKAGE_NAME = "obDefaultPackageName";

    public static final String PROGUARD_FILTER = "proguardFilter";

    public static final String PROGUARD_LIB_PLUGINS_FILTER = "proguardLibPluginsFilter";

    public static final String PROGUARD_ADDITIONAL_PARAMETER = "additionalProguardParameter";

    public static final String KEEP_MANIFEST_MF_IDS = "keepManifestMfIDs";

    public static final String KEEP_SCHEMA_EXSD_IDS = "keepSchemaExsdIDs";

    public static final String KEEP_PLUGIN_XML_IDS = "keepPluginXmlIDs";

    public static final String TEXT_FILE_ENCODING = "textFileEncoding";

    public static final String JAVA_HOME = "javaHome";

    public static final String OBFUSCATION_PARAMETER_FILE = "obfuscationParameterFile";

    public static final String OBFUSCATED_TEMP_DIR = "obfuscatedTempDir";

    public static final String OBFUSCATOR_CONFIG_FILE = "obfuscatorConfigFile";

    public static final String MAPPING_FILE_NAME = "mappingFileName";

    public static final String MAPPING_FILE_NAME_TO_APPLY = "mappingFileNameToApply";

    public static final String PLUGIN_LIB_SUB_DIRS = "pluginLibSubDirs";

    public static final String APPLICATION_PREFIX = "applicationPrefix";

    public static final String APPLICATION_PLUGINS = "applicationPlugins";

    public static final String APPLICATION_DIRECTORY = "applicationDirectory";

    public static final String APP_PLUGIN_DIR = "appPluginDir";

    private static Map<String, String> _properties;

    private PropertyHandler _propHandler;

    private static final String DEFAULT_PROP_FILE_NAME = SystemInfo.OBCLIPSE_BASE_DIR + "obclipse.properties";

    private static final String PROPERTY_FILE = "propertyFile";

    private final Map<String, String> _cmdLineProperties;

    public ObclipseProps(Map<String, String> cmdLineProperties) throws ObclipseException {
        _cmdLineProperties = cmdLineProperties;
        String obfuscationPropFile = cmdLineProperties.get(PROPERTY_FILE);
        if (obfuscationPropFile == null) {
            obfuscationPropFile = DEFAULT_PROP_FILE_NAME;
        }
        _propHandler = new PropertyHandler(new File(obfuscationPropFile));
        _propHandler.loadPropertyFile();
        _properties = new HashMap<String, String>();
    }

    public void initPropertyValues() throws ObclipseException {
        _properties.putAll((Map) _propHandler);
        _properties.putAll(_cmdLineProperties);
        boolean valueChanged = false;
        int savetyCounter = 0;
        do {
            valueChanged = false;
            for (Entry<String, String> entry : _properties.entrySet()) {
                String value = entry.getValue();
                String changedValue = replacePlaceholders(entry.getKey(), value);
                if (!value.equals(changedValue)) {
                    entry.setValue(changedValue);
                    valueChanged = true;
                }
            }
            if (savetyCounter++ > 100) {
                throw new ObclipseException("Cannot replace property placeholders correctly! Savety counter max reached!");
            }
        } while (valueChanged);
        _properties.put(APP_PLUGIN_DIR, _properties.get(APPLICATION_DIRECTORY) + File.separator + "plugins" + File.separator);
    }

    public void storeProperties() throws ObclipseException {
        _propHandler.storePropertyFile();
    }

    public static String get(String key) {
        return _properties.get(key);
    }

    public static boolean getBoolean(String key) {
        return Boolean.valueOf(_properties.get(key));
    }

    public static List<String> get(String key, String separator) {
        return parsePropertiesStringToList(_properties.get(key), separator);
    }

    public static List<String> getListComma(String key) {
        return parsePropertiesStringToList(_properties.get(key), ",");
    }

    public static List<String> getListSemicolon(String key) {
        return parsePropertiesStringToList(_properties.get(key), ";");
    }

    public static List<String> parsePropertiesStringToList(String propString, String delim) {
        StringTokenizer tokens = new StringTokenizer(propString, delim);
        List<String> propList = new ArrayList<String>();
        while (tokens.hasMoreTokens()) {
            String prop = tokens.nextToken();
            propList.add(prop.trim());
        }
        return propList;
    }

    public static Map<String, List<String>> getMap(String key) {
        List<String> pluginDependentPropertysStringList = parsePropertiesStringToList(key, ";");
        HashMap<String, List<String>> pluginDepPropList = new HashMap<String, List<String>>();
        for (String pluginWithProperties : pluginDependentPropertysStringList) {
            List<String> pluginWithPropList = parsePropertiesStringToList(pluginWithProperties, ",");
            String pluginName = pluginWithPropList.get(0);
            pluginWithPropList.remove(0);
            pluginDepPropList.put(pluginName, pluginWithPropList);
        }
        return pluginDepPropList;
    }

    private String replacePlaceholders(String key, String value) throws ObclipseException {
        Pattern fsPattern = Pattern.compile("\\$\\{[^(\\$\\{)^\\}]*\\}");
        Matcher matcher = fsPattern.matcher(value);
        String newValue = new String();
        int index = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            newValue += value.subSequence(index, start);
            String placeholder = value.substring(start + 2, end - 1);
            if (key.equals(placeholder)) {
                throw new ObclipseException("It is not possible to use a property key inside its poperty value as placeholder! Key: " + key);
            }
            String placeholderReplaceValue = System.getProperty(placeholder);
            if (placeholderReplaceValue == null) {
                Object object = _properties.get(placeholder);
                if (object != null) {
                    placeholderReplaceValue = (String) object;
                }
            }
            if (placeholderReplaceValue != null) {
                newValue += PropertiesUtil.trimEnclosingQuotes(placeholderReplaceValue);
            } else {
                throw new ObclipseException("Cannot replace placeholder '" + placeholder + "'! This placeholder property key is not defined!");
            }
            index = end;
        }
        newValue += value.subSequence(index, value.length());
        return newValue;
    }
}
