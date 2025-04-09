package com.dfruits.queries.utils.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SQLPreparator implements ISQLPreparator {

    public static final String SQL_VAR_PREFIX = ":";

    public static final String SQL_VAR_CHARS_REGEX = "\\w\\.";

    public static final String SQL_VAR_BIND_REGEX = "\\B(" + SQL_VAR_PREFIX + "[" + SQL_VAR_CHARS_REGEX + "]+)\\b";

    protected static final int OFFSET = SQL_VAR_PREFIX.length();

    public void replaceVars(StringBuffer sqlString, Map<String, String> inputParams, Map<String, Object> varPool, Map<String, Object> hints) {
        for (String inputVarName : inputParams.keySet()) {
            String varPoolName = inputParams.get(inputVarName);
            Object varValue = varPool.get(varPoolName);
            String value = SQLConverter.getInstance().convert(varValue, hints);
            ReplaceData data = new ReplaceData();
            data.paramName = inputVarName;
            data.paramValue = value;
            data.poolVarName = varPoolName;
            data.poolVarValue = varValue;
            data.inputParams = inputParams;
            data.varPool = varPool;
            data.hints = hints;
            data.sqlBuf = sqlString;
            beforeReplaceVar(data);
            replaceNamedVar(sqlString, data.paramName, data.paramValue);
        }
    }

    public static class ReplaceData {

        public String paramName;

        public String paramValue;

        public String poolVarName;

        public Object poolVarValue;

        public Map<String, Object> hints;

        public Map<String, Object> varPool;

        public Map<String, String> inputParams;

        public StringBuffer sqlBuf;
    }

    protected void beforeReplaceVar(ReplaceData data) {
    }

    public void replaceNamedVar(StringBuffer sqlString, String sqlVar, String filterVarValue, int max) {
        if (max <= 0) {
            max = Integer.MAX_VALUE;
        }
        List<Integer> bounds = nextUnbound(sqlString, sqlVar);
        int count = 0;
        while (bounds != null && !bounds.isEmpty()) {
            sqlString.replace(bounds.get(0) - OFFSET, bounds.get(1) - OFFSET, filterVarValue);
            count++;
            if (count > max) {
                break;
            }
            bounds = nextUnbound(sqlString, sqlVar);
        }
    }

    public void replaceNamedVar(StringBuffer sqlString, String sqlVar, String filterVarValue) {
        replaceNamedVar(sqlString, sqlVar, filterVarValue, -1);
    }

    protected List<Integer> nextUnbound(StringBuffer sqlString, String sqlVar) {
        return nextUnbound(sqlString, sqlVar, 0);
    }

    protected List<Integer> nextUnbound(StringBuffer sqlString, String sqlVar, int start) {
        List<Integer> ret = new ArrayList<Integer>();
        String variable;
        Pattern pattern = null;
        String regex = SQL_VAR_BIND_REGEX;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException pex) {
            pex.printStackTrace();
        }
        Matcher matcher = pattern.matcher(sqlString.substring(start));
        while (matcher.find()) {
            variable = matcher.group().substring(OFFSET);
            if (sqlVar.toLowerCase().equals(variable.toLowerCase())) {
                ret.add(matcher.start() + OFFSET + start);
                ret.add(matcher.end() + OFFSET + start);
                break;
            }
        }
        return ret;
    }

    public Map<String, List<int[]>> findUnboundVars(StringBuffer sqlString) {
        return findUnboundVars(sqlString.toString());
    }

    public static Map<String, List<int[]>> findUnboundVars(String sqlStmt) {
        Map<String, List<int[]>> notReplaced = new HashMap<String, List<int[]>>();
        String variable;
        Pattern pattern = null;
        String regex = SQL_VAR_BIND_REGEX;
        try {
            pattern = Pattern.compile(regex);
        } catch (PatternSyntaxException pex) {
            pex.printStackTrace();
        }
        Matcher matcher = pattern.matcher(sqlStmt);
        while (matcher.find()) {
            variable = matcher.group().substring(1);
            List<int[]> boundsList = notReplaced.get(variable);
            if (boundsList == null) {
                boundsList = new ArrayList<int[]>();
                notReplaced.put(variable, boundsList);
            }
            int[] bounds = { matcher.start(), matcher.end() };
            boundsList.add(bounds);
        }
        return notReplaced;
    }

    public String prepare(String sqlString, Map<String, String> mappings, Map<String, Object> varpool, Map<String, Object> hints) {
        StringBuffer buf = new StringBuffer(sqlString);
        replaceVars(buf, mappings, varpool, hints);
        return buf.toString();
    }
}
