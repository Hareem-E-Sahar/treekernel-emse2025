package org.mc4j.console.dashboard.components.html;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Greg Hinkle (ghinkle@users.sourceforge.net), Dec 21, 2005
 * @version $Revision: 570 $($Author: ghinkl $ / $Date: 2006-04-12 15:14:16 -0400 (Wed, 12 Apr 2006) $)
 */
public class DashboardHtmlTransformer {

    static int n;

    private Map<String, String> keys = new HashMap<String, String>();

    public String transform(String data, Map context) {
        Pattern pattern = Pattern.compile("\\$\\{([^\\}]*)\\}");
        String result = transformForEach(data, context);
        Matcher m = pattern.matcher(result);
        while (m.find()) {
            String identifier = m.group();
            identifier = identifier.substring(2, identifier.length() - 1);
            String key = "key" + ++n;
            result = m.replaceFirst("<span id=\"" + key + "\">loading...</span>");
            keys.put(key, identifier);
            m = pattern.matcher(result);
        }
        return result;
    }

    private String transformForEach(String data, Map context) {
        Pattern pattern = Pattern.compile("\\#foreach\\(([^\\)]*)\\)(.*)\\#endfor", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = pattern.matcher(data);
        while (m.find()) {
            StringBuffer allRows = new StringBuffer();
            String variableAssignment = m.group(1);
            String content = m.group(2);
            String[] vars = variableAssignment.split("\\:");
            String key = vars[0].trim();
            String contextKey = vars[1].trim();
            Collection c;
            try {
                contextKey = Pattern.compile("\\$\\{(.*)\\}").matcher(contextKey).replaceAll("$1");
                Object val = ognl.Ognl.getValue(contextKey, context);
                if (val.getClass().isArray()) {
                    c = Arrays.asList((Object[]) val);
                } else {
                    c = (Collection) val;
                }
            } catch (Exception e) {
                c = Collections.EMPTY_LIST;
            }
            key = key.replace("$", "\\$");
            Pattern p = Pattern.compile(key);
            for (Object row : c) {
                allRows.append(p.matcher(content).replaceAll(row.toString()));
            }
            data = data.substring(0, m.start()) + allRows.toString() + data.substring(m.end(), data.length());
            m = pattern.matcher(data);
        }
        return data;
    }

    public Map<String, String> getKeys() {
        return Collections.unmodifiableMap(keys);
    }

    public static void main3(String[] args) {
        String[] foo = new String[] { "a", "b" };
        Object bar = foo;
        System.out.println(Arrays.asList((Object[]) bar).size());
    }

    public static void main(String[] args) {
        String test = "<html> hello, world <p> \n  <b>${foo.bar}</b> ${baz} - [${biteme}] $100.50 {cool}\n" + "  #foreach($foo : ${bar[\"woa\"]})  \n" + "      <b> woa neat</b>\n" + "      Cool: ${$foo.bar.baz a.b[\"$foo\"]}\n" + "  #endfor\n" + " bah. ${last.one}";
        System.out.println("Start:\n" + test);
        Map ctx = new HashMap();
        ctx.put("bar", Arrays.asList(new String[] { "alpha", "beta", "gamma", "delta" }));
        System.out.println("Transform:\n" + new DashboardHtmlTransformer().transform(test, ctx));
    }

    public static void main2(String[] args) {
        Pattern p = Pattern.compile("\\#foreach\\((.*)\\)(.*)\\#endfor", Pattern.MULTILINE | Pattern.DOTALL);
        Matcher m = p.matcher("fdgh #foreach($a : ${b.c.d}) foo\n bar\n baz\n #endfor dfg ");
        System.out.println(m.find());
        System.out.println(m.group(1));
    }
}
