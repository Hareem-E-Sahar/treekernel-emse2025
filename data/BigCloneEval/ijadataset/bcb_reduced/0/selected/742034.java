package de.knowwe.instantedit.table;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * Section finder to find wiki table markup as a section.
 * <p>
 * <b>Note:</b><br>
 * Previous implementation using RegexSectionFinder produced an
 * StackOverflowError on large Tables!
 * 
 * @author volker_belli
 * @created 16.03.2012
 */
public class TableSectionFinder implements SectionFinder {

    private static final String TABLE_LINE_REGEXP = "^\\|\\|?([^\\|].*)?$\\r?\\n?\\r?";

    @Override
    public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
        ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
        Pattern TABLE_LINE = Pattern.compile(TABLE_LINE_REGEXP, Pattern.MULTILINE);
        Matcher m = TABLE_LINE.matcher(text);
        int end = 0;
        int tableStart = -1;
        int tableEnd = -1;
        while (m.find(end)) {
            int start = m.start();
            end = m.end();
            if (tableEnd == start) {
                tableEnd = end;
            } else {
                addResultIfAvailable(result, tableStart, tableEnd);
                tableStart = start;
                tableEnd = end;
            }
            if (end >= text.length()) break;
        }
        addResultIfAvailable(result, tableStart, tableEnd);
        return result;
    }

    private void addResultIfAvailable(ArrayList<SectionFinderResult> result, int tableStart, int tableEnd) {
        if (tableStart != -1) {
            result.add(new SectionFinderResult(tableStart, tableEnd));
        }
    }
}
