package charismata.resource.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import charismata.util.CharismataException;
import charismata.util.VelocityUtil;

public class SqlFileUtil {

    public static List<String> getPlaceHolderList(String sqlBody) {
        ArrayList<String> resultList = new ArrayList();
        Pattern pat = Pattern.compile("[^\\\\]\\$\\{?[0-9a-zA-Z_]+\\}?", Pattern.CASE_INSENSITIVE);
        Matcher match = pat.matcher(sqlBody);
        while (match.find()) {
            String matchedChar = sqlBody.substring(match.start() + 1, match.end());
            if (matchedChar.length() > 3 && (matchedChar.charAt(0) == '{') && (matchedChar.charAt(matchedChar.length() - 1) == '}')) {
                matchedChar = matchedChar.substring(2, matchedChar.length() - 1);
            } else {
                matchedChar = matchedChar.substring(1, matchedChar.length());
            }
            if (!resultList.contains(matchedChar)) {
                resultList.add(matchedChar);
            }
        }
        return resultList;
    }

    public static Map genPlaceHolderMap(List<String> placeholderList, List<String> placeholderValue) {
        Map placeHolderMap = new HashMap();
        int iteration = 0;
        for (String placeHolderName : placeholderList) {
            placeHolderMap.put(placeHolderName, placeholderValue.get(iteration));
            iteration++;
        }
        return placeHolderMap;
    }

    public static String evaluateVelocityTemplate(String velocityTemplate, List<String> placeholderList, List<String> placeholderValue) {
        Map placeHolderMap = genPlaceHolderMap(placeholderList, placeholderValue);
        return evaluateVelocityTemplate(velocityTemplate, placeHolderMap);
    }

    public static String evaluateVelocityTemplate(String velocityTemplate, Map placeHolderMap) {
        try {
            return VelocityUtil.evaluateTemplate(velocityTemplate, placeHolderMap);
        } catch (CharismataException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String repTokenForQuery(String sql, List<String> placeholderList, List<String> placeholderValue) {
        int repCount = 0;
        for (String rep : placeholderList) {
            String replaceValue = placeholderValue.get(repCount);
            while (sql.indexOf(rep) != -1) {
                int repPos = sql.indexOf(rep);
                sql = sql.substring(0, repPos) + replaceValue + sql.substring(repPos + rep.length());
            }
            repCount++;
        }
        return sql;
    }

    public static String repTokenForQuery(String sql, List<String> repList) {
        int repCount = 0;
        for (String rep : repList) {
            String repStr = ":" + repCount;
            while (sql.indexOf(repStr) != -1) {
                int repPos = sql.indexOf(repStr);
                sql = sql.substring(0, repPos) + rep + sql.substring(repPos + repStr.length());
            }
            repCount++;
        }
        return sql;
    }

    public static void main(String argv[]) {
        System.out.println("Running Test");
        List<String> result = getPlaceHolderList("Hello $HelloWorld ${HowAreYou}Help ${HelloWorld}");
        for (String resultStr : result) {
            System.out.println(resultStr);
        }
    }
}
