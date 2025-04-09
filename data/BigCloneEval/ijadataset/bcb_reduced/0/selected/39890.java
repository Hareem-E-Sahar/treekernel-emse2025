package nl.desiree;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import nl.desiree.resources.Resources;

public class Remark {

    private String source;

    private String condition;

    public Remark(final String source) {
        this.source = source;
    }

    public Remark(final String source, final String condition) {
        this.source = source;
        this.condition = condition;
    }

    private String calculate(final String from, final Resources resources) {
        StringBuffer output = new StringBuffer();
        Pattern resourceRegexp = Pattern.compile("\\$\\{[\\w\\.]+\\}");
        Matcher matcher = resourceRegexp.matcher(from);
        while (matcher.find()) {
            String resourceKey = from.substring(matcher.start() + 2, matcher.end() - 1);
            matcher.appendReplacement(output, resources.getResourceValue(resourceKey));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    public String makeOutput(final Resources resources) {
        return calculate(source, resources);
    }

    public boolean isAppropriate(Resources resources) {
        if (condition == null) {
            return true;
        }
        return calculate(condition, resources) != "";
    }
}
