package pt.iscte.dsi.taa.policies.relationships.association.multiplicity;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pt.iscte.dsi.taa.qualifiers.InstancePrivate;

public class MultiplicityItem {

    public MultiplicityItem(String value) {
        if (!MultiplicityItem.syntaxIsValid(value)) throw new InvalidMultiplicityValueException("Invalid syntax of value.");
        String left_bound = "\\d+";
        String right_bound = "([\\d&&[^0]]\\d*|[*])";
        String bound = "((" + left_bound + "\\.\\." + right_bound + ")|" + left_bound + ")";
        Pattern pattern = Pattern.compile(bound);
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            addMultiplicityRange(value.substring(start, end));
        }
    }

    public boolean contains(final int value) {
        for (MultiplicityRange multiplicity_range : multiplicity_ranges) if (multiplicity_range.contains(value)) return true;
        return false;
    }

    @InstancePrivate
    private void addMultiplicityRange(final String range) {
        if (MultiplicityItem.isDoubleBounded(range)) {
            String lower_bound = MultiplicityItem.getLowerBound(range);
            String upper_bound = MultiplicityItem.getUpperBound(range);
            multiplicity_ranges.add(new MultiplicityRange(lower_bound, upper_bound));
        } else multiplicity_ranges.add(new MultiplicityRange(range));
    }

    public static boolean isDoubleBounded(final String range) {
        String left_bound = "\\d+";
        String right_bound = "([\\d&&[^0]]\\d*|[*])";
        String double_bound = "(" + left_bound + "\\.\\." + right_bound + ")";
        Pattern pattern = Pattern.compile(double_bound);
        Matcher matcher = pattern.matcher(range);
        return matcher.matches();
    }

    public static String getLowerBound(final String range) {
        String left_bound = "\\d+(?=\\.)";
        Pattern pattern = Pattern.compile(left_bound);
        Matcher matcher = pattern.matcher(range);
        matcher.find();
        int start = matcher.start();
        int end = matcher.end();
        return range.substring(start, end);
    }

    public static String getUpperBound(final String range) {
        String right_bound = "(?<=\\.)([\\d&&[^0]]\\d*|[*])";
        Pattern pattern = Pattern.compile(right_bound);
        Matcher matcher = pattern.matcher(range);
        matcher.find();
        int start = matcher.start();
        int end = matcher.end();
        return range.substring(start, end);
    }

    public static boolean syntaxIsValid(final String multiplicity) {
        String left_bound = "\\d+";
        String right_bound = "([\\d&&[^0]]\\d*|[*])";
        String bound = "((" + left_bound + "\\.\\." + right_bound + ")|" + left_bound + ")";
        String multiplicity_sintax = bound + "(," + bound + ")*";
        Pattern pattern = Pattern.compile(multiplicity_sintax);
        Matcher matcher = pattern.matcher(multiplicity);
        return matcher.matches();
    }

    public String toString() {
        String multiplicity_item = "";
        Iterator<MultiplicityRange> iterator = multiplicity_ranges.iterator();
        multiplicity_item += iterator.next().toString();
        while (iterator.hasNext()) multiplicity_item = multiplicity_item + "," + iterator.next().toString();
        return multiplicity_item;
    }

    @InstancePrivate
    private LinkedList<MultiplicityRange> multiplicity_ranges = new LinkedList<MultiplicityRange>();
}
