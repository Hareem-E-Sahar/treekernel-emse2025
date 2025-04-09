package sk.savba.ui.ie.xmlregex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sk.savba.ui.ie.results.Result;
import sk.savba.ui.ie.xmlregex.data.Group;

/**
 * @author Stefan Dlugolinsky
 * 
 */
public class XMLRegexPattern extends sk.savba.ui.ie.xmlregex.data.Pattern {

    protected Pattern pattern = null;

    protected Matcher matcher = null;

    public XMLRegexPattern() {
        initGroups();
    }

    public XMLRegexPattern(String name, String className) {
        this.name = name;
        this.clazz = className;
        initGroups();
    }

    public void setClassName(String value) {
        setClazz(value);
    }

    public String getClassName() {
        return getClazz();
    }

    private void initGroups() {
        if (groups == null) {
            groups = new XMLRegexGroups();
        }
    }

    public void correctGroupIds(int level) {
        for (Group group : groups.getGroup()) {
            group.setId(group.getId() + level);
        }
    }

    /**
	 * Finds beginning round brackets of regexp capturing groups.
	 * 
	 * @param pattern
	 * @return regexp groups in the pattern
	 */
    public static ArrayList<Integer> getGroups(String pattern) {
        ArrayList<Integer> captGroups = new ArrayList<Integer>();
        Pattern p1 = Pattern.compile("(?:(?:^|[^\\\\])(?:\\\\\\\\)*)(\\(+)(?!\\?)");
        Matcher m1 = p1.matcher(pattern);
        Pattern p2 = Pattern.compile("(?:^|[^\\\\])(?:\\\\\\\\)*(\\[[^\\]\\(]*\\([^\\]]*\\])");
        Matcher m2 = p2.matcher(pattern);
        Pattern p3 = Pattern.compile("\\(");
        ArrayList<Integer> brackets = new ArrayList<Integer>();
        while (m2.find()) {
            Matcher m3 = p3.matcher(m2.group(1));
            while (m3.find()) {
                brackets.add(m2.start(1) + m3.start());
            }
        }
        ArrayList<Integer> groups = new ArrayList<Integer>();
        while (m1.find()) {
            Matcher m3 = p3.matcher(m1.group(1));
            while (m3.find()) {
                groups.add(m1.start(1) + m3.start());
            }
        }
        for (int group : groups) {
            if (!brackets.contains(group)) {
                captGroups.add(group);
            }
        }
        return captGroups;
    }

    /**
	 * Counts regular expression groups in the given pattern. Counting is based
	 * on finding beginning round brackets of regexp capturing groups.
	 * 
	 * @param pattern
	 * @return number of regexp groups in the pattern
	 */
    public static int getGroupId(String pattern) {
        return getGroups(pattern).size();
    }

    /**
	 * Recursively builds regular expression pattern with groups meta data,
	 * where \p{group:index}(group) corresponds to groups(index) group.
	 * 
	 * @param in
	 * @param macros
	 * @param group
	 * @return regular expression
	 */
    private String applyMacro(XMLRegexPattern in, HashMap<String, XMLRegexPattern> macros) {
        String input = in.regex;
        int start = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        String temp = "";
        XMLRegexGroup group = null;
        XMLRegexGroup newGroup = null;
        ArrayList<Integer> captGroups = getGroups(input);
        if (in.groups.getGroup().size() > 0) {
            for (int i : captGroups) {
                j++;
                group = null;
                for (Group g : in.groups.getGroup()) {
                    if (g.getId() == j) {
                        group = (XMLRegexGroup) in.groups.getGroup().get(l);
                        break;
                    }
                }
                if (group == null) {
                    continue;
                }
                newGroup = new XMLRegexGroup(0, group.getClassName());
                groups.getGroup().add(newGroup);
                k = groups.getGroup().size() - 1;
                temp += input.substring(start, i) + "\\p{group:" + k + "}";
                start = i;
                l++;
            }
        }
        temp += input.substring(start, input.length());
        if (in.getClassName() != null) {
            newGroup = new XMLRegexGroup(0, in.getClassName());
            groups.getGroup().add(newGroup);
            k = groups.getGroup().size() - 1;
            temp = "\\p{group:" + k + "}(" + temp + ")";
        }
        Pattern p = Pattern.compile("\\\\p\\{pattern:([^}]+)\\}");
        Matcher m = p.matcher(temp);
        String out = "";
        start = 0;
        while (m.find()) {
            XMLRegexPattern macro = macros.get(m.group(1));
            out += temp.substring(start, m.start()) + applyMacro(macro, macros);
            start = m.end();
        }
        out += temp.substring(start, temp.length());
        return out;
    }

    /**
	 * Removes meta names of groups from regex and corrects groups ids.
	 */
    private void applyGroups() {
        Pattern p = Pattern.compile("\\\\p\\{group:(?:([0-9]+)|null)\\}");
        Matcher m = p.matcher(regex);
        String temp = "";
        int start = 0;
        while (m.find()) {
            String g1 = m.group(1);
            if (g1 != null && !g1.equals("")) {
                int id = getGroupId(regex.substring(0, m.start())) + 1;
                int group = Integer.valueOf(g1);
                groups.getGroup().get(group).setId(id);
            }
            temp += regex.substring(start, m.start());
            start = m.end();
        }
        temp += regex.substring(start, regex.length());
        regex = temp;
    }

    public void build(HashMap<String, XMLRegexPattern> macros) {
        int remove = groups.getGroup().size();
        regex = applyMacro(this, macros);
        applyGroups();
        while (remove > 0) {
            groups.getGroup().remove(0);
            remove--;
        }
        pattern = Pattern.compile(regex);
    }

    /**
	 * Returns a hash map, where keys are classes of all used patterns. Values
	 * are lists of extractions for corresponding patterns.
	 * 
	 * @param text
	 * @return hasmap of extractions
	 */
    public HashMap<String, ArrayList<ArrayList<Result>>> parse(String text) {
        HashMap<String, ArrayList<ArrayList<Result>>> regExpResultSet = new HashMap<String, ArrayList<ArrayList<Result>>>();
        matcher = pattern.matcher(text);
        while (matcher.find()) {
            ArrayList<Result> regExpResults = new ArrayList<Result>();
            for (Group g : groups.getGroup()) {
                int groupId = g.getId();
                String match = matcher.group(groupId);
                if (match != null) {
                    XMLRegexTextPosition pos = new XMLRegexTextPosition(text, matcher.start(groupId));
                    regExpResults.add(new Result(g.getClassName(), match, pos));
                }
            }
            if (regExpResults.size() > 0) {
                ArrayList<ArrayList<Result>> r = regExpResultSet.get(getClassName());
                if (r != null) {
                    r.add(regExpResults);
                } else {
                    ArrayList<ArrayList<Result>> rs = new ArrayList<ArrayList<Result>>();
                    rs.add(regExpResults);
                    regExpResultSet.put(getClassName(), rs);
                }
            }
        }
        return regExpResultSet;
    }

    public Set<Result> annotate(String text) {
        HashSet<Result> results = new HashSet<Result>();
        matcher = pattern.matcher(text);
        while (matcher.find()) {
            for (Group g : groups.getGroup()) {
                int groupId = g.getId();
                String match = matcher.group(groupId);
                if (match != null) {
                    XMLRegexTextPosition pos = new XMLRegexTextPosition(text, matcher.start(groupId));
                    results.add(new Result(g.getClassName(), match, pos));
                }
            }
        }
        return results;
    }

    public boolean isMacro() {
        return getName() != null && !getName().isEmpty();
    }
}
