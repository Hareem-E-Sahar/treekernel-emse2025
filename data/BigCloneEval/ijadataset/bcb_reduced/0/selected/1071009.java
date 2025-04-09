package com.byterefinery.rmbench.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.byterefinery.rmbench.extension.VariableDescriptor;

/**
 * The URLValueParser parses a JDBC-url and reads the values inserted in the url.
 * 
 * @author Hannes Niederhausen
 *
 */
public class URLValueParser implements IURLValueParser {

    private static Pattern PATTERN = Pattern.compile("\\$\\{([^\\}]*)\\}");

    private final Pattern URLPATTERN;

    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    private final VariableDescriptor descriptors[];

    /** String containing the beginning of the jdbc url (e.g. jdbc:mysql:)*/
    private String jdbcProtocol;

    private String template;

    private final Map<String, String> varMap = new HashMap<String, String>(5);

    private final List<String> variables = new ArrayList<String>(5);

    private final List<String> parts = new ArrayList<String>(5);

    private String options = "";

    public URLValueParser(String template, VariableDescriptor[] descriptors) {
        super();
        this.descriptors = descriptors;
        this.template = template;
        parseTemplate();
        URLPATTERN = Pattern.compile(jdbcProtocol + "(//([^:]+)(:([0-9]+))?/)?(\\p{Alnum}+)" + "(\\?(\\p{Alnum}+=\\p{Alnum}+)(\\&\\p{Alnum}+=\\p{Alnum}+)*)?");
    }

    /**
     * Resets the values to null or to default, if it exists.
     */
    public void reset() {
        options = "";
        for (int i = 0; i < descriptors.length; i++) {
            varMap.put(descriptors[i].getName(), descriptors[i].getDefaultValue());
        }
    }

    /**
     * parses a url template and saves all variables which are contained
     *
     */
    private void parseTemplate() {
        Pattern protocolPattern = Pattern.compile("[^:]*:[^:]*:");
        String template2 = "";
        Matcher matcher = protocolPattern.matcher(template);
        if (matcher.find()) {
            jdbcProtocol = template.substring(0, matcher.end());
            template2 = template.substring(matcher.end(), template.length());
        }
        matcher = PATTERN.matcher(template2);
        int start = 0;
        while (matcher.find()) {
            String part = template2.substring(start, matcher.start());
            parts.add(part);
            String var = matcher.group(1);
            variables.add(var);
            varMap.put(var, null);
            start = matcher.end();
        }
        if (start < template2.length()) {
            parts.add(template2.substring(start));
        }
    }

    /**
     * Checks if the given url is a valid instance of the template
     * @param url a jdbc url to check
     * @return 
     */
    public boolean isValidUrl(String url) {
        Matcher matcher = URLPATTERN.matcher(url);
        if (matcher.find()) return matcher.end() == url.length();
        return false;
    }

    public String getValidURL() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(jdbcProtocol);
        String val = getValue(descriptors[0].getName());
        if ((descriptors[0].isRequired()) || (val != null)) {
            buffer.append("//");
            if (val != null) {
                buffer.append(val);
                val = getValue(descriptors[1].getName());
                if ((descriptors[1].isRequired()) || (val != null)) {
                    buffer.append(":");
                    if (val != null) buffer.append(val);
                }
                buffer.append("/");
            }
        }
        val = getValue(descriptors[2].getName());
        if (val != null) {
            buffer.append(val);
        } else {
            buffer.append("???");
        }
        if (options.length() > 0) {
            buffer.append("?");
            buffer.append(options);
        }
        return buffer.toString();
    }

    /**
     * @return the original template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * @param name a variable name
     * @return the value currently used for the given variable
     */
    public String getValue(String name) {
        return varMap.get(name);
    }

    /**
     * @param number a numeric variable reference
     * @return the value currently used for the given variable
     */
    public String getValue(int number) {
        return varMap.get(descriptors[number].getName());
    }

    /**
    * @return the variable names (which might be numbers)
    */
    public String[] getVariableNames() {
        return variables.toArray(new String[variables.size()]);
    }

    /**
    * @return the number of variable references in the string
    */
    public int getVariableCount() {
        return variables.size();
    }

    /**
    * set a variable which has a numeric reference
    * @param number the value of the numeric reference
    * @param value the value to substitute
    */
    public void setVariable(int number, String value) {
        setValue(String.valueOf(number), value);
    }

    /**
    * Parses the given url and saves the values 
    * @param url a valid jdbc url
    */
    public boolean parseUrl(String url) {
        String split[] = url.split("\\?");
        if (split.length > 1) options = split[1];
        if (!isValidUrl(split[0])) return false;
        Matcher matcher = URLPATTERN.matcher(split[0]);
        if (matcher.find()) {
            String host = matcher.group(2);
            setValue(descriptors[0].getName(), host);
            String port = matcher.group(4);
            setValue(descriptors[1].getName(), port);
            String db = matcher.group(5);
            setValue(descriptors[2].getName(), db);
        }
        return true;
    }

    /**
    * set a variable which has a named (or numeric) reference
    * @param name the reference name or number
    * @param value the value to substitute
    * @throws IllegalArgumentException if the variable is not declared
    */
    public void setValue(String name, String value) {
        if (!varMap.containsKey(name)) {
            throw new IllegalArgumentException("undefined variable: " + name);
        }
        String oldValue = varMap.get(name);
        if ((oldValue != null) && (oldValue.equals(value))) return;
        varMap.put(name, value);
        propertySupport.firePropertyChange(name, oldValue, value);
    }

    public void addPropertyListener(String property, PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(property, listener);
    }

    public void addPropertyListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(propertyName, listener);
    }

    public String getURLOptions() {
        return options;
    }

    public void setUrlOptions(String options) {
        this.options = options;
    }
}
