package com.dfruits.queries.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.dfruits.queries.model.In;

public class StringUtils {

    public static final char COLUMN_SYMBOL = '$';

    public static final String SQL_VAR_PREFIX = ":";

    public static final String SQL_VAR_CHARS_REGEX = "\\w\\.";

    public static final String SQL_VAR_BIND_REGEX = "\\B(" + SQL_VAR_PREFIX + "[" + SQL_VAR_CHARS_REGEX + "]+)\\b";

    private static final int OFFSET = SQL_VAR_PREFIX.length();

    private StringBuffer sqlString;

    private String originalString;

    public StringUtils(String sqlString) {
        this.sqlString = new StringBuffer(sqlString);
        originalString = sqlString + "";
    }

    public static String replaceColSymbol(String text) {
        if (text == null) {
            return null;
        }
        String ret = text.trim();
        if (ret.length() > 1 && COLUMN_SYMBOL == text.charAt(0)) {
            ret = text.substring(1);
        }
        return ret.toLowerCase();
    }

    public void replaceVars(List<In> inputParams, Map<String, Object> varPool, boolean isUpdateStmt) {
        Iterator<In> iterator = inputParams.iterator();
        In ipt;
        String sqlVar;
        String filterVar;
        Object filterVarValue;
        while (iterator.hasNext()) {
            ipt = iterator.next();
            sqlVar = ipt.getName();
            filterVar = ipt.getValue();
            filterVarValue = varPool.get(filterVar);
            if (filterVarValue == null || "".equals(filterVarValue)) {
                if (isUpdateStmt) {
                    filterVarValue = "null";
                } else {
                    filterVarValue = "%";
                }
            }
            if (List.class.isInstance(filterVarValue) || SQLDataConverter.isSQLStatement(filterVarValue.toString())) {
                replaceLikeByIn(sqlVar);
            }
            String value;
            if (SQLDataConverter.isSQLStatement(filterVarValue.toString())) {
                String sql = (String) filterVarValue;
                StringUtils utils = new StringUtils(sql);
                List<In> localParams = new ArrayList<In>(inputParams.size() - 1);
                for (In in : inputParams) {
                    if (in.getName().equals(sqlVar)) {
                        continue;
                    }
                    localParams.add(in);
                }
                utils.replaceVars(localParams, varPool, false);
                value = utils.getBoundString();
            } else {
                value = SQLDataConverter.toSqlString(filterVarValue, isUpdateStmt);
            }
            replaceNamedVar(sqlVar, value, filterVar);
        }
    }

    public void replaceLikeByIn(String sqlVar) {
        List<Integer> bounds = nextUnbound(sqlVar);
        int lastStartIndex = -1;
        while (bounds != null && !bounds.isEmpty()) {
            int startIndex = bounds.get(0) - OFFSET - 1;
            if (startIndex == lastStartIndex - 2) {
                break;
            } else {
                lastStartIndex = startIndex;
            }
            String charAtStartIndex = sqlString.charAt(startIndex) + "";
            while (charAtStartIndex.matches("\\s*") && startIndex >= 0) {
                startIndex--;
                charAtStartIndex = sqlString.charAt(startIndex) + "";
            }
            while (charAtStartIndex.matches("\\w") && startIndex >= 0) {
                startIndex--;
                charAtStartIndex = sqlString.charAt(startIndex) + "";
            }
            boolean foundSomethingOnLeftSideOfVar = startIndex >= 0;
            if (foundSomethingOnLeftSideOfVar) {
                startIndex++;
                String leftSideOfVar = sqlString.substring(startIndex, bounds.get(0) - OFFSET);
                boolean leftSideIsLikeLiteral = leftSideOfVar.matches("[lL][iI][kK][eE]\\s*");
                if (leftSideIsLikeLiteral) {
                    sqlString.replace(startIndex, bounds.get(0) - OFFSET, "IN ");
                }
            }
            bounds = nextUnbound(sqlVar, bounds.get(0));
        }
    }

    public void replaceNamedVar(String sqlVar, String filterVarValue, String filterVarName, int max) {
        if (max <= 0) {
            max = Integer.MAX_VALUE;
        }
        List<Integer> bounds = nextUnbound(sqlVar);
        int count = 0;
        while (bounds != null && !bounds.isEmpty()) {
            sqlString.replace(bounds.get(0) - OFFSET, bounds.get(1) - OFFSET, filterVarValue + String.format(" /*%s%s*/", sqlVar, filterVarName == null ? "" : " (" + filterVarName + ")"));
            count++;
            if (count > max) {
                break;
            }
            bounds = nextUnbound(sqlVar);
        }
    }

    public void replaceNamedVar(String sqlVar, String filterVarValue, String filterVarName) {
        replaceNamedVar(sqlVar, filterVarValue, filterVarName, -1);
    }

    public void replaceNamedVar(String sqlVar, String filterVarValue) {
        replaceNamedVar(sqlVar, filterVarValue, null);
    }

    private List<Integer> nextUnbound(String sqlVar) {
        return nextUnbound(sqlVar, 0);
    }

    private List<Integer> nextUnbound(String sqlVar, int start) {
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

    public Map<String, List<int[]>> findUnboundVars() {
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

    public String getBoundString() {
        return sqlString.toString();
    }

    static String testSql = "UPDATE Users" + "\n" + "SET" + "\n" + "UserLanguage = :UserLanguage, -- :UserLanguage.old" + "\n" + "UserLanguage = :UserLanguage, -- :UserLanguage.old" + "\n" + "RoleName  = :Rolename, -- :Rolename.old" + "\n" + "Firstname = :Firstname, -- :Firstname.old" + "\n" + "Surname = :Surname -- :Surname.old" + "\n" + "WHERE  IDUser        = :IDUser.old" + "\n" + "and xyz like :Like";

    public static void main(String[] args) {
        StringUtils utils = new StringUtils(testSql);
        utils.replaceNamedVar("UserLanguage", "'UserLanguage'");
        utils.replaceNamedVar("UserLanguage.old", "'UserLanguage.old'");
        utils.replaceLikeByIn("Like");
        utils.replaceNamedVar("Like", "'replacedLike'");
        System.out.println(utils.getBoundString());
    }
}
