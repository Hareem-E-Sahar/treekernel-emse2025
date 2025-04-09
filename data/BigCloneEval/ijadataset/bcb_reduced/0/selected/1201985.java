package gate.creole.numbers;

import static gate.creole.numbers.AnnotationConstants.NUMBER_ANNOTATION_NAME;
import static gate.creole.numbers.AnnotationConstants.TYPE_FEATURE_NAME;
import static gate.creole.numbers.AnnotationConstants.VALUE_FEATURE_NAME;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ExecutionInterruptedException;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.InvalidOffsetException;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A GATE PR which annotates Roman Numerals with their numeric value.
 * 
 * @see <a href="http://gate.ac.uk/userguide/sec:misc-creole:numbers:roman">The GATE User Guide</a>
 * @author Mark A. Greenwood
 * @author Valentin Tablan
 */
@CreoleResource(name = "Roman Numerals Tagger", comment = "Finds and annotates Roman numerals", icon = "roman.png", helpURL = "http://gate.ac.uk/userguide/sec:misc-creole:numbers:roman")
public class RomanNumeralsTagger extends AbstractLanguageAnalyser {

    private static final long serialVersionUID = 8568794158677464398L;

    private String outputAnnotationSetName;

    private boolean allowLowerCase = false;

    /**
   * The maximum numbers of characters allowed as a suffix. This is useful for
   * recognising references to document parts that also have a sub-part, such as
   * Table XIIa.
   */
    private int maxTailLength;

    /**
   * Converts a Roman letter into its value.
   * 
   * @param letter
   *          the Roman numeral character to convert into a number
   * @return returns the value of the character when treated as a Roman numeral,
   *         or 01 if the character isn't valid
   */
    private int letterToNumber(char letter) {
        switch(letter) {
            case 'I':
                return 1;
            case 'V':
                return 5;
            case 'X':
                return 10;
            case 'L':
                return 50;
            case 'C':
                return 100;
            case 'D':
                return 500;
            case 'M':
                return 1000;
            default:
                return -1;
        }
    }

    /**
   * A utility method to convert a Roman numeral into a decimal value.
   * 
   * @param roman
   *          the {@link String} representing the Roman numeral.
   * @return the value of the Roman numeral provided
   * @throws NumberFormatException
   *           if the provided string is not a valid Roman numeral.
   */
    private int romanToInt(String roman) throws NumberFormatException {
        if (roman.length() == 0) throw new NumberFormatException("An empty string does not define a Roman numeral.");
        roman = roman.toUpperCase();
        int i = 0;
        int arabic = 0;
        while (i < roman.length()) {
            char letter = roman.charAt(i);
            int number = letterToNumber(letter);
            if (number < 0) return number;
            i++;
            if (i == roman.length()) {
                arabic += number;
            } else {
                int nextNumber = letterToNumber(roman.charAt(i));
                if (nextNumber > number) {
                    arabic += (nextNumber - number);
                    i++;
                } else {
                    arabic += number;
                }
            }
        }
        if (arabic > 3999) throw new NumberFormatException("Roman numeral must have value 3999 or less.");
        return arabic;
    }

    public String getOutputASName() {
        return outputAnnotationSetName;
    }

    @RunTime
    @Optional
    @CreoleParameter(comment = "The name for annotation set used for the generated annotations")
    public void setOutputASName(String outputAnnotationSetName) {
        this.outputAnnotationSetName = outputAnnotationSetName;
    }

    public Integer getMaxTailLength() {
        return maxTailLength;
    }

    @RunTime
    @CreoleParameter(defaultValue = "0", comment = "The maximum numbers of characters allowed as a suffix. " + "This is useful for recognising references to document " + "parts that also have a sub-part, such as Table XIIa.")
    public void setMaxTailLength(Integer maxTailLength) {
        this.maxTailLength = maxTailLength;
    }

    public Boolean getAllowLowerCase() {
        return allowLowerCase;
    }

    @RunTime
    @CreoleParameter(defaultValue = "false", comment = "Should lower case Roman numerals, e.g. vi, be recognised?")
    public void setAllowLowerCase(Boolean allowLowerCase) {
        this.allowLowerCase = allowLowerCase;
    }

    @Override
    public void execute() throws ExecutionException {
        interrupted = false;
        if (document == null) throw new ExecutionException("No Document provided!");
        AnnotationSet outputAS = document.getAnnotations(outputAnnotationSetName);
        long startTime = System.currentTimeMillis();
        fireStatusChanged("Tagging Roman Numerals in " + document.getName());
        fireProgressChanged(0);
        Pattern pattern;
        if (allowLowerCase) {
            if (maxTailLength > 0) {
                pattern = Pattern.compile("\\b((?:[mdclxvi]+)|(?:[MDCLCVI]+))(\\w{0," + maxTailLength + "})\\b");
            } else {
                pattern = Pattern.compile("\\b((?:[mdclxvi]+)|(?:[MDCLCVI]+))\\b");
            }
        } else {
            if (maxTailLength > 0) {
                pattern = Pattern.compile("\\b([MDCLCVI]+)(\\w{0," + maxTailLength + "})\\b");
            } else {
                pattern = Pattern.compile("\\b([MDCLCVI]+)\\b");
            }
        }
        String content = document.getContent().toString();
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            if (isInterrupted()) {
                throw new ExecutionInterruptedException("The execution of the \"" + getName() + "\" Roman Numerals Tagger has been abruptly interrupted!");
            }
            int numStart = matcher.start(1);
            int numEnd = matcher.end(1);
            if (numStart >= 0 && numEnd > numStart) {
                String romanNumeral = content.substring(numStart, numEnd);
                int value = romanToInt(romanNumeral);
                if (value > 0) {
                    String tail = null;
                    if (maxTailLength > 0) {
                        int tailStart = matcher.start(2);
                        int tailEnd = matcher.end(2);
                        if (tailStart < tailEnd) {
                            tail = content.substring(tailStart, tailEnd);
                            numEnd = tailEnd;
                        }
                    }
                    FeatureMap fm = Factory.newFeatureMap();
                    fm.put(VALUE_FEATURE_NAME, Integer.valueOf(value).doubleValue());
                    fm.put(TYPE_FEATURE_NAME, "roman");
                    if (tail != null) fm.put("tail", tail);
                    try {
                        outputAS.add((long) numStart, (long) numEnd, NUMBER_ANNOTATION_NAME, fm);
                    } catch (InvalidOffsetException e) {
                    }
                }
            }
        }
        fireProcessFinished();
        fireStatusChanged(document.getName() + " tagged with Roman Numerals in " + NumberFormat.getInstance().format((double) (System.currentTimeMillis() - startTime) / 1000) + " seconds!");
    }
}
