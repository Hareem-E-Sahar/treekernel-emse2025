package com.kescom.matrix.core.series;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import com.freebss.sprout.core.utils.QueryStringUtils;
import com.kescom.matrix.core.db.IndexBase;
import com.kescom.matrix.core.ref.IRefGroup;
import com.kescom.matrix.core.ref.IRefValue;

public class DataPointType extends IndexBase implements IDataPointType {

    private String label;

    private String dataType;

    private String invalidFormatMessage;

    private Format format;

    private Map<String, String> listOfValues;

    private String minValue;

    private String maxValue;

    private Pattern validationRegex;

    private String formatClass;

    private String formatFormat;

    private String validationRegexPattern;

    private String listOfValuesJoined;

    private boolean isDouble;

    private boolean isLong;

    private boolean isText;

    private IRefGroup refGroup;

    private String props;

    private Map<String, String> propsMap;

    public class TrivialTextFormat extends Format {

        private static final long serialVersionUID = -8558598037778193452L;

        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(obj != null ? obj.toString() : "");
        }

        public Object parseObject(String source, ParsePosition pos) {
            pos.setIndex(-1);
            return new String(source);
        }
    }

    public class EnumTextFormat extends Format {

        private static final long serialVersionUID = -7237889464721114817L;

        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            if (refGroup != null) {
                for (IRefValue value : refGroup.getValues()) if (value.getValue().equals(obj)) {
                    obj = value.getName();
                    break;
                }
            } else if (listOfValues != null && obj != null) obj = listOfValues.get(obj.toString());
            return toAppendTo.append(obj != null ? obj.toString() : "");
        }

        public Object parseObject(String source, ParsePosition pos) {
            pos.setIndex(-1);
            if (refGroup != null) {
                for (IRefValue value : refGroup.getValues()) if (value.getName().equals(source)) return new String(value.getValue());
            } else if (listOfValues != null && source != null) {
                for (String key : listOfValues.keySet()) if (listOfValues.get(key).equals(source)) return new String(key);
            }
            return new String(source);
        }
    }

    private static Map<String, Object> exampleObjects = new LinkedHashMap<String, Object>();

    static {
        exampleObjects.put(DT_BOOLEAN, Boolean.TRUE);
        exampleObjects.put(DT_DOUBLE, new Double(12.34));
        exampleObjects.put(DT_LONG, new Long(1024));
        exampleObjects.put(DT_ENUM, "?enum?");
        exampleObjects.put(DT_STRING, "Text");
        exampleObjects.put(DT_TIME, new Date());
    }

    public String getListOfValuesJoined() {
        return listOfValuesJoined;
    }

    public void setListOfValuesJoined(String listOfValuesJoined) {
        this.listOfValuesJoined = listOfValuesJoined;
    }

    public String getFormatClass() {
        return formatClass;
    }

    public void setFormatClass(String formatClass) {
        this.formatClass = formatClass;
    }

    public String getFormatFormat() {
        return formatFormat;
    }

    public void setFormatFormat(String formatFormat) {
        this.formatFormat = formatFormat;
    }

    public String getValidationRegexPattern() {
        return validationRegexPattern;
    }

    public void setValidationRegexPattern(String validationRegexPattern) {
        this.validationRegexPattern = validationRegexPattern;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
        isDouble = dataType.equals(DT_DOUBLE);
        isLong = dataType.equals(DT_LONG);
        isText = dataType.equals(DT_STRING) || dataType.equals(DT_ENUM);
    }

    public synchronized Pattern getValidationRegex() {
        if (validationRegex == null && validationRegexPattern != null) validationRegex = Pattern.compile(validationRegexPattern);
        return validationRegex;
    }

    public void setValidationRegex(Pattern validationRegex) {
        this.validationRegex = validationRegex;
    }

    public String getInvalidFormatMessage() {
        return invalidFormatMessage;
    }

    public void setInvalidFormatMessage(String invalidFormatMessage) {
        this.invalidFormatMessage = invalidFormatMessage;
    }

    public synchronized Format getFormat() {
        if (format == null && formatClass != null) {
            try {
                if (formatFormat != null) {
                    Class<?>[] types = { String.class };
                    format = (Format) Class.forName(formatClass).getConstructor(types).newInstance(formatFormat);
                } else format = (Format) Class.forName(formatClass).newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (format == null) {
            if (dataType.equals(DT_ENUM)) format = new EnumTextFormat(); else format = new TrivialTextFormat();
        }
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public synchronized Map<String, String> getListOfValues() {
        if (listOfValues == null) {
            if (refGroup != null) {
                listOfValues = new LinkedHashMap<String, String>();
                for (IRefValue refValue : refGroup.getValues()) listOfValues.put(refValue.getValue(), refValue.getName());
            } else if (listOfValuesJoined != null) {
                listOfValues = new LinkedHashMap<String, String>();
                for (String tok : listOfValuesJoined.split(",")) {
                    String[] pair = tok.split("=");
                    if (pair.length > 1) listOfValues.put(pair[0], pair[1]); else listOfValues.put(pair[0], pair[0]);
                }
            }
        }
        return listOfValues;
    }

    public void setListOfValues(Map<String, String> listOfValues) {
        this.listOfValues = listOfValues;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public Object parse(String text) throws ParseException {
        if (!isText) text = StringUtils.trimToNull(text);
        if (text == null) return null;
        Object value = getFormat().parseObject(text);
        if (isDouble && !(value instanceof Double) && (value instanceof Number)) value = ((Number) value).doubleValue(); else if (isLong && !(value instanceof Long) && (value instanceof Number)) value = ((Number) value).longValue();
        return value;
    }

    public Object getExampleObject() {
        if (dataType.equals(DT_ENUM)) {
            Map<String, String> list = getListOfValues();
            if (list != null && list.size() > 0) {
                String[] values = list.values().toArray(new String[0]);
                return values[(new Random()).nextInt(values.length)];
            }
        }
        Object value = exampleObjects.get(dataType);
        return value != null ? value : new Double(0.0);
    }

    public IRefGroup getRefGroup() {
        return refGroup;
    }

    public void setRefGroup(IRefGroup refGroup) {
        this.refGroup = refGroup;
    }

    public String getProps() {
        return props;
    }

    public void setProps(String props) {
        this.props = props;
    }

    public String getProperty(String name, String defaultValue) {
        if (propsMap == null && props != null) propsMap = QueryStringUtils.decode(props);
        if (propsMap == null) return defaultValue; else if (!propsMap.containsKey(name)) return defaultValue; else return propsMap.get(name);
    }

    public Map<String, String> getPropsMap() {
        return propsMap;
    }

    public void setPropsMap(Map<String, String> propsMap) {
        this.propsMap = propsMap;
    }
}
