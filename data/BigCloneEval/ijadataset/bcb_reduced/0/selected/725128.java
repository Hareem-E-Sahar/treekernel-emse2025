package it.kion.util.ui.ulog2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SQLFormatter {

    private static List<String> findParamValueArray(String paramString) {
        List<String> l = new ArrayList<String>();
        try {
            Pattern Regex = Pattern.compile("(, )?(\\(String\\) |\\(Timestamp\\) |\\(int\\) |\\(long\\) |\\(BigDecimal\\) )", Pattern.CANON_EQ);
            Matcher RegexMatcher = Regex.matcher(paramString);
            int count = 0;
            int start = 0;
            int end = 0;
            String currentType = null;
            while (RegexMatcher.find()) {
                if (count > 0) {
                    end = RegexMatcher.start();
                    String unformattedParam = paramString.substring(start, end);
                    l.add(getFormattedParamValue(unformattedParam, currentType));
                }
                start = RegexMatcher.end();
                currentType = RegexMatcher.group(2);
                count++;
            }
            String unformattedParam = paramString.substring(start, paramString.length());
            l.add(getFormattedParamValue(unformattedParam, currentType));
        } catch (PatternSyntaxException ex) {
            throw new RuntimeException("Tipo non supportato");
        }
        return l;
    }

    public static String formatSql(String log) {
        log = log.replace('\t', ' ');
        log = log.replace('\n', '\t');
        log = log.replace('\r', '\t');
        Pattern p = Pattern.compile("(.*)\\[params=([^\\]]*)\\]$");
        System.out.println("formatSQL: INIZIO");
        Matcher m = p.matcher(log);
        if (m.find()) {
            log = m.group(1);
            String paramString = m.group(2);
            System.out.println("PARAMS:" + paramString);
            List<String> params = findParamValueArray(paramString);
            for (String current : params) {
                log = log.replaceFirst("\\?", current);
            }
        }
        log = log.replace('\t', '\n');
        int index = log.indexOf("select");
        if (index == -1) {
            index = log.indexOf("SELECT");
        }
        if (index == -1) {
            index = log.indexOf("Select");
        }
        if (index == -1) {
            index = log.indexOf("insert");
        }
        if (index == -1) {
            index = log.indexOf("Insert");
        }
        if (index == -1) {
            index = log.indexOf("INSERT");
        }
        if (index == -1) {
            index = log.indexOf("update");
        }
        if (index == -1) {
            index = log.indexOf("Update");
        }
        if (index == -1) {
            index = log.indexOf("UPDATE");
        }
        if (index == -1) {
            index = log.indexOf("delete");
        }
        if (index == -1) {
            index = log.indexOf("Delete");
        }
        if (index == -1) {
            index = log.indexOf("DELETE");
        }
        if (index != -1) {
            log = log.substring(index);
        }
        return log;
    }

    private static String getFormattedParamValue(String paramValue, String paramType) {
        int valuePos = paramType.indexOf(") ");
        String type = paramType.substring(1, valuePos);
        if ("String".equals(type)) {
            paramValue = "'" + paramValue + "'";
        }
        if ("Timestamp".equals(type)) {
            paramValue = "timestamp'" + paramValue + "'";
        }
        return paramValue;
    }
}
