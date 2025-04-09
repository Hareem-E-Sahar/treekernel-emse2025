package net.danielkvasnicka.flower.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Support for named groups in regexs
 * 
 * @author Eugene Kuleshov
 * @see http://www.jroller.com/eu/entry/named_matching_groups
 */
public class NamedPattern {

    private List<String> groups = new ArrayList<String>();

    private Pattern pattern;

    public NamedPattern(final String namedRegex, final int flags) {
        StringBuilder sb = new StringBuilder();
        Pattern p = Pattern.compile("\\((\\{(\\S+?)})");
        Matcher m = p.matcher(namedRegex);
        int pos = 0;
        while (m.find()) {
            groups.add(m.group(2));
            sb.append(namedRegex.substring(pos, m.start(1)));
            pos = m.end();
        }
        String regex = sb.append(namedRegex.substring(pos)).toString();
        pattern = Pattern.compile(regex, flags);
    }

    public Matcher matcher(final CharSequence input) {
        return pattern.matcher(input);
    }

    public List<String> getGroups() {
        return groups;
    }

    public String groupName(final int i) {
        return (String) groups.get(i);
    }

    public String group(final Matcher m, final String name) {
        int n = groups.indexOf(name);
        return n == -1 ? null : m.group(n + 1);
    }
}
