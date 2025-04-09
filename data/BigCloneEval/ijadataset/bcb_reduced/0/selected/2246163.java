package org.simplextensions.properties;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 
 * @author Tomasz Krzyzak, <a
 *         href="mailto:tomasz.krzyzak@gmail.com">tomasz.krzyzak@gmail.com</a>
 * @since 2010-05-10 15:02:50
 */
public class ReferencingProperties extends Properties {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1736307457508795962L;

    public static final String ReferenceRegex = "\\$\\{([\\w]++(.[\\w]++)*?)+?\\}";

    public ReferencingProperties() {
    }

    public ReferencingProperties(Properties defaults) {
        super(defaults);
    }

    public String getProperty(String propertyName, List<String> processedProperties) {
        checkCycle(propertyName, processedProperties);
        String property = super.getProperty(propertyName);
        if (property == null) {
            property = System.getProperty(propertyName);
        }
        if (property == null) {
            property = System.getenv(propertyName);
        }
        String result = property;
        if (result != null) {
            Pattern compile = Pattern.compile(ReferenceRegex);
            Matcher matcher = compile.matcher(result);
            if (matcher.find()) {
                do {
                    String refPropertyName = result.substring(matcher.start() + 2, matcher.end() - 1);
                    processedProperties.add(propertyName);
                    result = result.replace("${" + refPropertyName + "}", getProperty(refPropertyName, processedProperties));
                    processedProperties.remove(propertyName);
                    matcher = compile.matcher(result);
                } while (matcher.find());
            }
        }
        return result;
    }

    private void checkCycle(String key, List<String> processedProperties) {
        if (processedProperties.contains(key)) {
            StringBuilder sb = new StringBuilder();
            for (String s : processedProperties) {
                sb.append(s).append(" --> ");
            }
            sb.append(key);
            throw new PropertiesException("cycle in properties references found: " + sb.toString());
        }
    }

    public String getProperty(String key, Properties defaultValues) {
        return getProperty(key, new LinkedList<String>());
    }

    public String getProperty(String key, String defaultValue) {
        String property = getProperty(key);
        return property != null ? property : defaultValue;
    }

    /**
	 * 
	 */
    public String getProperty(String key) {
        return getProperty(key, new LinkedList<String>());
    }
}
