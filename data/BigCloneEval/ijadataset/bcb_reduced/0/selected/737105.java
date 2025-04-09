package mil.army.usace.ehlschlaeger.digitalpopulations.tabletools;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mil.army.usace.ehlschlaeger.rgik.core.CSVTable;
import mil.army.usace.ehlschlaeger.rgik.core.DataException;

/**
 * Builds a result using a simple formatting syntax. Text is copied to output
 * except for patterns like "%{name}", which will copy the named column. For
 * example, "%{householdID}:%{realizationID}".
 * <p>
 * Copyright <a href="http://faculty.wiu.edu/CR-Ehlschlaeger2/">Charles R.
 * Ehlschlaeger</a>, work: 309-298-1841, fax: 309-298-3003, This software is
 * freely usable for research and educational purposes. Contact C. R.
 * Ehlschlaeger for permission for other purposes. Use of this software requires
 * appropriate citation in all published and unpublished documentation.
 * 
 * @author William R. Zwicky
 */
public class FormatGetter implements ColumnGetter {

    List<ColumnGetter> pieces;

    public FormatGetter(String format, List<String> colNames) {
        Pattern p = Pattern.compile("%\\{([^}]+)\\}");
        pieces = new ArrayList<ColumnGetter>();
        Matcher m = p.matcher(format);
        int end = 0;
        while (m.find()) {
            if (m.start() > end) pieces.add(new ConstantGetter(format.substring(end, m.start())));
            int mg1 = colNames.indexOf(m.group(1));
            if (mg1 < 0) throw new DataException(String.format("Column not found: %s", m.group(1)));
            pieces.add(new CleanGetter(mg1));
            end = m.end();
        }
        if (end < format.length()) pieces.add(new ConstantGetter(format.substring(end)));
    }

    public String get(String[] row) {
        StringBuffer buf = new StringBuffer();
        for (ColumnGetter piece : pieces) buf.append(piece.get(row));
        return buf.toString();
    }

    public String get(List<String> row) {
        StringBuffer buf = new StringBuffer();
        for (ColumnGetter piece : pieces) buf.append(piece.get(row));
        return buf.toString();
    }

    public String get(CSVTable table, int row) {
        StringBuffer buf = new StringBuffer();
        for (ColumnGetter piece : pieces) buf.append(piece.get(table, row));
        return buf.toString();
    }

    /**
     * Reports whether a format string actually has any variables in it to
     * expand. If it doesn't, you may want to interpret the string some other
     * way.
     * 
     * @param format string to test
     * @return true if 'format' contains any variables, false if none
     */
    public static boolean hasVars(String format) {
        return format.contains("%{");
    }
}

class ConstantGetter implements ColumnGetter {

    private String value;

    public ConstantGetter(String value) {
        this.value = value;
    }

    public String get(String[] row) {
        return value;
    }

    public String get(List<String> row) {
        return value;
    }

    public String get(CSVTable table, int row) {
        return value;
    }
}
