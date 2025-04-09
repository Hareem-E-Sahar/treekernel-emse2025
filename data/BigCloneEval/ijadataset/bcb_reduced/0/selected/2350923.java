package com.jipes.cm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Remaps an identifier (e.g. a schema or object name) to another name.
 * @author Matt Pouttu-Clarke
 *
 */
public class RemapIdentifier {

    private static final Log LOG = LogFactory.getLog(RemapIdentifier.class);

    private RemapIdentifier() {
        super();
    }

    /**
	 * Replaces every occurrence of before string with after string within the 
	 * passed SQL.  Comparison is based on an exact match of the before string
	 * using required non-word characters before and after the match.
	 * @param before
	 * @param after
	 * @param sql
	 * @return value after replace
	 */
    public static String remap(String before, String after, String sql) {
        if (LOG.isTraceEnabled()) LOG.trace("Remapping before: " + before + ", after: " + after + ", sql: " + sql);
        StringBuilder out = new StringBuilder(sql.length() + 128);
        Pattern pattern = Pattern.compile("(^|\\W+)(" + before + ")(\\W+|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        int lastIndex = 0;
        while (matcher.find()) {
            if (LOG.isTraceEnabled()) LOG.trace("Found match at index " + matcher.start(2));
            out.append(sql.substring(lastIndex, matcher.start(2)));
            out.append(after);
            lastIndex = matcher.end(2);
        }
        out.append(sql.substring(lastIndex));
        if (LOG.isTraceEnabled()) LOG.trace("Result: " + out);
        return out.toString();
    }

    /**
	 * Simple unit test
	 * @param args
	 */
    public static void main(String[] args) {
        System.out.println("Result: " + remap("MATT", "MATT2", "CREATE TABLE \"MATT\".TEST_MATT AS SELECT * FROM MATT.MATT_TEST WHERE 1=2"));
        System.out.println("Result: " + remap("MATT", "MATT2", "matt"));
    }
}
