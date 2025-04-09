package cmsc427.mw5;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains the x,y,z values and classifier for one gesture reading from the data set
 * @author tom
 *
 */
public class RawTrainingValues {

    public String classifier;

    public float[] values = new float[300];

    RawTrainingValues(String input) {
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher result = pattern.matcher(input);
        int i = 0;
        while (result.find()) {
            values[i++] = Float.parseFloat(input.substring(result.start(), result.end()));
        }
        pattern = Pattern.compile("([a-zA-Z]+)");
        result = pattern.matcher(input);
        if (result.find()) {
            classifier = input.substring(result.start(), result.end());
        }
    }
}
