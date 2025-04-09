package com.datas.component;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;
import org.apache.log4j.Logger;
import com.datas.bean.enums.FieldType;

/**
 * 
 * AdvancedTextField extends JTextField's presentational and input validation capabilities. It determines the field type from its
 * element properties. According to the type of the field it is responsible for presenting the value in the appropriate manner.
 * 
 * @author kimi
 * 
 */
@SuppressWarnings("serial")
public class AdvancedTextField extends JTextField implements FocusListener {

    public static final Dimension txtFldMinimum = new Dimension(110, 20);

    public static final Dimension txtFldPreferd = new Dimension(110, 20);

    private static final String TIME_PATTERN = "HH:mm";

    /** Patterns used to validate input */
    private static final String VALIDATION_PATTERN_STRING = "";

    private static final String VALIDATION_PATTERN_FILE = "[^\\\\/?*|\":<>]*";

    private static final String VALIDATION_PATTERN_INTEGER = "(^-?\\d?[\\d,]*$)";

    private static final String VALIDATION_PATTERN_LONG = "(^-?\\d?[\\d,]*$)";

    private static final String VALIDATION_PATTERN_FLOAT = "(^-?\\d?[\\d,]*\\.[\\d,]*$)|(^-?\\d?[\\d,]*$)|(^-?\\.\\d?[\\d,]*$)";

    private static final String VALIDATION_PATTERN_DOUBLE = "(^-?\\d?[\\d,]*\\.[\\d,]*$)|(^-?\\d?[\\d,]*$)|(^-?\\.\\d?[\\d,]*$)";

    private static final String VALIDATION_PATTERN_BIGDECIMAL = "(^-?\\d?[\\d,]*\\.[\\d,]*$)|(^-?\\d?[\\d,]*$)|(^-?\\.\\d?[\\d,]*$)";

    private static final String VALIDATION_PATTERN_CURRENCY = "(^\\d?[\\d,]*\\.[\\d,]*$)|(^\\d?[\\d,]*$)|(^\\.\\d?[\\d,]*$)";

    private static final String VALIDATION_PATTERN_INTEGER_PERCENTAGE = "(^-?[\\d,]*%?$)";

    private static final String VALIDATION_PATTERN_TIME = "(^\\d{0,2}$)|(^\\d{1,2}:\\d{0,2}$)";

    /** Patterns as array of strings */
    private static final String[] validationPatterns = { VALIDATION_PATTERN_STRING, VALIDATION_PATTERN_FILE, VALIDATION_PATTERN_INTEGER, VALIDATION_PATTERN_LONG, VALIDATION_PATTERN_FLOAT, VALIDATION_PATTERN_DOUBLE, VALIDATION_PATTERN_BIGDECIMAL, VALIDATION_PATTERN_CURRENCY, VALIDATION_PATTERN_INTEGER_PERCENTAGE, VALIDATION_PATTERN_TIME };

    /** Name of fields as array of strings */
    public static final String[] FIELD_TYPE_NAMES = { FieldType.FIELD_TYPE_STRING.getName(), FieldType.FIELD_TYPE_FILE.getName(), FieldType.FIELD_TYPE_INTEGER.getName(), FieldType.FIELD_TYPE_LONG.getName(), FieldType.FIELD_TYPE_FLOAT.getName(), FieldType.FIELD_TYPE_DOUBLE.getName(), FieldType.FIELD_TYPE_BIGDECIMAL.getName(), FieldType.FIELD_TYPE_CURRENCY.getName(), FieldType.FIELD_TYPE_INTEGER_PERCENTAGE.getName(), FieldType.FIELD_TYPE_TIME.getName(), FieldType.FIELD_TYPE_DATE.getName(), FieldType.FIELD_TYPE_BOOLEAN.getName() };

    /** Type of field. The type of the field determines its presentational characteristics */
    protected int fieldType = FieldType.FIELD_TYPE_STRING.getType();

    /**
	 * Determines the maximum length that can be inputted or the maximum numeric value that can be inputted. The context is
	 * according to the type of the field. Numeric fields will be treated as maximum value and string fields will be treated as
	 * maximum length of field.
	 */
    protected int minLength = -1;

    protected int maxLength = -1;

    protected int digitNumber = 2;

    /** Determine if field should be formatted according to its type when losing focus from text field. */
    protected boolean formatted = true;

    /** Custom pattern validation string. Enables definining additional input validation. */
    protected String customValidationString = null;

    /** Hold Locale of Text Field. */
    protected Locale locale = Locale.getDefault();

    /** Hold Symbols instance according to the Locale. */
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(locale);

    /** Hold a reference to a component on which the status of the input is being written */
    JTextComponent statusComp = null;

    private static final Logger LOG = Logger.getLogger(AdvancedTextField.class);

    /** Inner class that extends PlainDocument capabilities to support characters validation. */
    public class PresentableDocument extends PlainDocument {

        private StringBuffer scratchBuffer;

        public PresentableDocument() {
            scratchBuffer = new StringBuffer();
        }

        /**
		 * Override insertString of the PlainDocument Class. Only if characters are valid, they are passed to the super class.
		 * 
		 * @param offset -
		 *            offset of the string
		 * @param text -
		 *            inputted text
		 * @param aset -
		 *            attributes for the inserted text
		 */
        public void insertString(int offset, String text, AttributeSet aset) throws BadLocationException {
            if ((text == null)) {
                return;
            }
            if (text.equals("")) {
                super.insertString(offset, text, aset);
            }
            scratchBuffer.setLength(0);
            try {
                scratchBuffer.append(getText(0, getLength()));
                scratchBuffer.insert(offset, text);
            } catch (BadLocationException ble) {
                LOG.error(ble);
                return;
            } catch (StringIndexOutOfBoundsException sioobe) {
                LOG.error(sioobe);
                return;
            }
            if (scratchBuffer.toString().equals("")) {
                return;
            }
            int textLength = scratchBuffer.length();
            if (customValidationString != null) {
                if (!validatePattern(customValidationString, scratchBuffer.toString())) {
                    writeStatus("Invalid input value");
                    return;
                }
            }
            if ((fieldType == FieldType.FIELD_TYPE_STRING.getType()) || (fieldType == FieldType.FIELD_TYPE_FILE.getType())) {
                if ((maxLength != -1) && (textLength > maxLength)) {
                    writeStatus("Maximum of " + maxLength + " characters are allowed in this field");
                    return;
                } else if (fieldType == FieldType.FIELD_TYPE_FILE.getType()) {
                    if (!isFileNameValid(scratchBuffer.toString())) {
                        writeStatus("Field of type File cannot have the following characters: \\ / : * ? \" < > |");
                        return;
                    }
                } else if (fieldType == FieldType.FIELD_TYPE_TIME.getType()) {
                    if (!(validatePattern(getValidationPattern(), scratchBuffer.toString()))) {
                        writeStatus("You entered invalid value for field of type " + getFieldTypeName());
                        return;
                    }
                }
            } else {
                try {
                    if ((minLength >= 0) && scratchBuffer.toString().startsWith("-")) {
                        writeStatus("Field must be positive");
                        return;
                    }
                    if (maxLength != -1) {
                        writeStatus("Value must be in range (" + minLength + " to " + maxLength + ")");
                        return;
                    }
                } catch (Exception e) {
                }
                if (fieldType == FieldType.FIELD_TYPE_CURRENCY.getType()) {
                    if (!((validatePattern(getValidationPattern(), scratchBuffer.toString())) || (scratchBuffer.toString().indexOf(decimalFormatSymbols.getCurrencySymbol()) > -1))) {
                        writeStatus("You entered invalid value for field of type " + getFieldTypeName());
                        return;
                    }
                } else if (!(validatePattern(getValidationPattern(), scratchBuffer.toString()))) {
                    writeStatus("You entered invalid value for field of type " + getFieldTypeName());
                    return;
                }
            }
            writeStatus("");
            super.insertString(offset, text, aset);
        }

        /**
		 * Override remove method of the Document in order to clear the status bar message when clearing characters.
		 * 
		 * @param offs -
		 *            offset.
		 * @param len -
		 *            length.
		 */
        public void remove(int offs, int len) throws BadLocationException {
            writeStatus("");
            super.remove(offs, len);
        }
    }

    /**
	 * Constructor.
	 * 
	 * @param fieldType -
	 *            Field type.
	 * @param maxLength -
	 *            Maximum input length.
	 */
    public AdvancedTextField(int fieldType, int maxLength) {
        setDocument(new PresentableDocument());
        addFocusListener(this);
        this.fieldType = fieldType;
        this.maxLength = maxLength;
    }

    /**
	 * Constructor.
	 * 
	 * @param fieldType -
	 *            Field type.
	 * @param maxLength -
	 *            Maximum input length.
	 */
    public AdvancedTextField(int fieldType, int maxLength, int digitNo) {
        setDocument(new PresentableDocument());
        addFocusListener(this);
        this.fieldType = fieldType;
        this.maxLength = maxLength;
        this.digitNumber = digitNo;
    }

    public AdvancedTextField(int filedType, int maxLength, String customPatternValidation) {
        this(filedType, maxLength);
        this.customValidationString = customPatternValidation;
    }

    /**
	 * Constructor.
	 * 
	 * @param text -
	 *            Initial text value.
	 * @param columns -
	 *            Width of field in columns.
	 * @param fieldType -
	 *            Type of field.
	 */
    public AdvancedTextField(String text, int columns, int fieldType) {
        setDocument(new PresentableDocument());
        setText(text);
        setColumns(columns);
        addFocusListener(this);
        this.fieldType = fieldType;
    }

    /**
	 * Constructor.
	 * 
	 * @param text -
	 *            Initial text value.
	 * @param columns -
	 *            Width of field in columns.
	 * @param fieldType -
	 *            Type of field.
	 * @param minLength -
	 *            Minimum allowed length / value.
	 * @param maxLength -
	 *            Maximum allowed length / value.
	 */
    public AdvancedTextField(String text, int columns, int fieldType, int minLength, int maxLength) {
        this(text, columns, fieldType);
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    /**
	 * Override set method of ancestor class. Set text to component from info. This method maks sure to format the value according
	 * to its type before setting it to the text field.
	 * 
	 * @param s -
	 *            Text to put in Text Field.
	 */
    public void setValue(String s) {
        setText(formatValue(s));
    }

    /**
	 * Override get method of ancestor class. This method make sure that if the field has a numeric characteristics, to clean it
	 * before passing the value to the info class.
	 * 
	 * @return string containg value needed to be passed to info class.
	 */
    public String getValue() {
        return extractNumber(getText(), false);
    }

    /**
	 * Utility Function. Extract only number from a given string. If the type of the field is string then this function will
	 * ignore the formatting request.
	 * 
	 * @param value -
	 *            string containg numeric value.
	 * @param isFullExtract -
	 *            if true then remove all undesired characters fully.
	 * @return - String containg a clean numeric value.
	 */
    private String extractNumber(String value, boolean isFullExtract) {
        if (!((fieldType == FieldType.FIELD_TYPE_INTEGER.getType()) || (fieldType == FieldType.FIELD_TYPE_LONG.getType()) || (fieldType == FieldType.FIELD_TYPE_FLOAT.getType()) || (fieldType == FieldType.FIELD_TYPE_DOUBLE.getType()) || (fieldType == FieldType.FIELD_TYPE_BIGDECIMAL.getType()) || (fieldType == FieldType.FIELD_TYPE_CURRENCY.getType()) || (fieldType == FieldType.FIELD_TYPE_INTEGER_PERCENTAGE.getType()))) {
            return value;
        }
        boolean isAddPrecChar = ((!isFullExtract) && (fieldType == FieldType.FIELD_TYPE_INTEGER_PERCENTAGE.getType()) && (value.endsWith("%")));
        boolean isPointFound = false;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < value.length(); i++) {
            if ((value.charAt(i) >= '0') && (value.charAt(i) <= '9')) {
                sb.append(value.charAt(i));
            } else {
                if (fieldType == FieldType.FIELD_TYPE_FLOAT.getType() || fieldType == FieldType.FIELD_TYPE_DOUBLE.getType() || fieldType == FieldType.FIELD_TYPE_BIGDECIMAL.getType() || fieldType == FieldType.FIELD_TYPE_CURRENCY.getType()) {
                    if ((i == 0) && (value.charAt(0) == '-')) {
                        sb.append("-");
                    }
                    if ((!isPointFound) && (value.charAt(i) == decimalFormatSymbols.getDecimalSeparator())) {
                        sb.append(value.charAt(i));
                        isPointFound = true;
                    }
                } else if (fieldType == FieldType.FIELD_TYPE_INTEGER.getType() || fieldType == FieldType.FIELD_TYPE_LONG.getType() || fieldType == FieldType.FIELD_TYPE_INTEGER_PERCENTAGE.getType()) {
                    if ((i == 0) && (value.charAt(0) == '-')) {
                        sb.append("-");
                    }
                }
            }
        }
        if (sb.toString().equals("-")) {
            return "";
        }
        if (isAddPrecChar) {
            sb.append("%");
        }
        return trimLeadingZeroes(sb.toString());
    }

    /**
	 * Utility Function. Format a given string according to a specific type.
	 * 
	 * @param value -
	 *            String value.
	 * @retrun formatted string.
	 */
    private String formatValue(String value) {
        if ((value == null) || (value.trim().equals(""))) {
            return "";
        }
        String showValue = value;
        if (fieldType == FieldType.FIELD_TYPE_INTEGER.getType()) {
            try {
                showValue = extractNumber(showValue, true);
                NumberFormat nf = NumberFormat.getIntegerInstance(locale);
                showValue = nf.format(Integer.valueOf(showValue));
            } catch (Exception e) {
                return "";
            }
        } else if (fieldType == FieldType.FIELD_TYPE_LONG.getType()) {
            try {
                showValue = extractNumber(showValue, true);
                NumberFormat nf = NumberFormat.getIntegerInstance(locale);
                showValue = nf.format(Long.valueOf(showValue));
            } catch (Exception e) {
                return "";
            }
        } else if (fieldType == FieldType.FIELD_TYPE_FLOAT.getType()) {
            try {
                showValue = extractNumber(showValue, true);
                NumberFormat nf = NumberFormat.getNumberInstance(locale);
                nf.setMinimumFractionDigits(2);
                nf.setMaximumFractionDigits(digitNumber);
                showValue = nf.format(Float.valueOf(showValue));
            } catch (Exception e) {
                return "";
            }
        } else if (fieldType == FieldType.FIELD_TYPE_DOUBLE.getType()) {
            try {
                showValue = extractNumber(showValue, true);
                NumberFormat nf = NumberFormat.getNumberInstance(locale);
                nf.setMinimumFractionDigits(2);
                nf.setMaximumFractionDigits(digitNumber);
                showValue = nf.format(Double.valueOf(showValue));
            } catch (Exception e) {
                return "";
            }
        } else if (fieldType == FieldType.FIELD_TYPE_BIGDECIMAL.getType()) {
            try {
                showValue = extractNumber(showValue, true);
                NumberFormat nf = NumberFormat.getNumberInstance(locale);
                nf.setMinimumFractionDigits(2);
                nf.setMaximumFractionDigits(digitNumber);
                showValue = nf.format(new BigDecimal(showValue));
            } catch (Exception e) {
                return "";
            }
        } else if (fieldType == FieldType.FIELD_TYPE_CURRENCY.getType()) {
            try {
                showValue = extractNumber(showValue, true);
                NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
                showValue = nf.format(new Float(showValue));
            } catch (Exception e) {
                return "";
            }
        } else if (fieldType == FieldType.FIELD_TYPE_INTEGER_PERCENTAGE.getType()) {
            try {
                boolean isPrec = value.endsWith("%");
                showValue = extractNumber(showValue, true);
                NumberFormat nf = NumberFormat.getIntegerInstance();
                showValue = nf.format(Integer.valueOf(showValue));
                if (isPrec) {
                    showValue += "%";
                }
            } catch (Exception e) {
                return "";
            }
        } else if (fieldType == FieldType.FIELD_TYPE_TIME.getType()) {
            try {
                String s = convertDate(value, TIME_PATTERN, TIME_PATTERN);
                showValue = (s != null ? s : "");
            } catch (Exception e) {
                return "";
            }
        }
        return showValue;
    }

    /**
	 * Private Utility function that validates a given pattern against a given set of characters.
	 * 
	 * @param pattern -
	 *            pattern that should be followed.
	 * @param charSet -
	 *            data that should be validated.
	 */
    private boolean validatePattern(String pattern, String charSet) {
        try {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(charSet);
            return m.matches();
        } catch (Exception e) {
            LOG.error("*** Failed in validatePattern ***");
            return false;
        }
    }

    /**
	 * Clear text field.
	 */
    public void clear() {
        setText("");
    }

    /**
	 * Return text of field.
	 * 
	 * @return text of string.
	 */
    public String toString() {
        return getText();
    }

    /**
	 * Get component name.
	 * 
	 * @return component name.
	 */
    public String getCompName() {
        return getName();
    }

    /**
	 * Implement focusLost of FocusListener interface. Make sure that after leaving component the value in the component will be
	 * formatted.
	 * 
	 * @param fe -
	 *            Object containing information of FocusEvent.
	 */
    public void focusLost(FocusEvent fe) {
        if (fieldType != FieldType.FIELD_TYPE_STRING.getType() && formatted) {
            setText(formatValue(getText()));
        }
    }

    /**
	 * Implement focusGained of FocusListener interface. Make sure the when entering the componet it will let the user only edit
	 * the value and not the formatted version of the value.
	 * 
	 * @param fe -
	 *            Object containing information of FocusEvent.
	 */
    public void focusGained(FocusEvent fe) {
        if ((fieldType != FieldType.FIELD_TYPE_STRING.getType()) && formatted) {
            setText(extractNumber(getText(), false));
        }
        selectAll();
    }

    /**
	 * Get the field type.
	 * 
	 * @return field type.
	 */
    public int getFieldType() {
        return fieldType;
    }

    /**
	 * Get the name of the field type.
	 * 
	 * @return field type name.
	 */
    public String getFieldTypeName() {
        return FIELD_TYPE_NAMES[fieldType];
    }

    /**
	 * Return the validation pattern of the field type. Note: Make sure that the validation pattern is always according to the
	 * current used Locale.
	 * 
	 * @return field type validation pattern.
	 */
    public String getValidationPattern() {
        String validationPattern = new String(validationPatterns[fieldType]);
        StringBuffer newValidationPattern = new StringBuffer(validationPattern.length());
        char ch;
        for (int i = 0; i < validationPattern.length(); i++) {
            ch = validationPattern.charAt(i);
            if (ch == '.') {
                newValidationPattern.append(decimalFormatSymbols.getDecimalSeparator());
            } else if (ch == ',') {
                newValidationPattern.append(decimalFormatSymbols.getGroupingSeparator());
            } else {
                newValidationPattern.append(ch);
            }
        }
        return newValidationPattern.toString();
    }

    /**
	 * Set formatted property of text field. This property determines if text field value should be formatted according to its
	 * type hen losing focus on the field.
	 * 
	 * @param formatted -
	 *            Determine if text field should be formatted or not.
	 */
    public void setFormatted(boolean formatted) {
        this.formatted = formatted;
    }

    /**
	 * This method writes to the status bar (is it exists) the status of the current inputted value (error notifications, allowed
	 * range etc').
	 */
    public void writeStatus(String msg) {
        if (statusComp != null) {
            statusComp.setText(msg);
        }
    }

    /**
	 * Validate if a file name is valid. This method is used only as a workaround for a problem (JDK 1.4.2) in validating value
	 * using the Regualr Expressions Engine of Java regarding "Hebrew" characters (I assume the problem will be in any other
	 * language that is not English).
	 * 
	 * @param str -
	 *            String containing file name.
	 * @return true if file name is valid.
	 */
    public static boolean isFileNameValid(String str) {
        final String allowedChars = "\\/?:<>|*\"";
        int j;
        for (int i = 0; i < str.length(); i++) {
            for (j = 0; j < allowedChars.length(); j++) {
                if (str.charAt(i) == allowedChars.charAt(j)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
	 * Set the locale of the Text Field. The Locale determines the behavoir of numeric inputs.
	 */
    public void setLocale(Locale locale) {
        this.locale = locale;
        decimalFormatSymbols = new DecimalFormatSymbols(locale);
    }

    /**
	 * Recalculate the data of a field. This method is useful if we would like to change a property of the Text field component
	 * that is relevant to the presentation of the value. For example, if we use a text field with currency type and we would like
	 * to change the Locale. In order to see the currency value with the new currency symbol we can activate this method.
	 */
    public void recalcValue() {
        if ((fieldType != FieldType.FIELD_TYPE_STRING.getType()) && formatted) {
            setText(extractNumber(getText(), false));
        }
        if (fieldType != FieldType.FIELD_TYPE_STRING.getType() && formatted) {
            setText(formatValue(getText()));
        }
    }

    /**
	 * Utility function. Take off leading zeroes from a string.
	 * 
	 * @param str -
	 *            string with leading zeroes.
	 * @return string without leading zeroes.
	 */
    private String trimLeadingZeroes(String str) {
        if (str == null) {
            return null;
        } else if (str.equals("")) {
            return "";
        }
        int i = 0;
        while (((str.charAt(i) == '0') || (((str.charAt(i) == '-') && i == 0))) && (i < str.length() - 1) && (str.charAt(i + 1) != '.')) {
            i++;
        }
        return ((str.startsWith("-") && (i > 0)) ? "-" : "") + str.substring(i, str.length());
    }

    /**
	 * Utility function. Convert a date in string style from one format to another format and returns the result as string.
	 * 
	 * @param textDate -
	 *            date as text in the format of inputFormat.
	 * @param inputFormat -
	 *            format of inputted date string.
	 * @param outputFormat -
	 *            format to output string.
	 * @return String date with the new format.
	 */
    private String convertDate(String textDate, String inputFormat, String outputFormat) {
        try {
            SimpleDateFormat sdfInput = new SimpleDateFormat(inputFormat);
            SimpleDateFormat sdfOutput = new SimpleDateFormat(outputFormat);
            Date date = sdfInput.parse(textDate);
            return sdfOutput.format(date);
        } catch (ParseException pe) {
            return null;
        }
    }
}
