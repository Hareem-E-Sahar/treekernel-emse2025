package src.utilities;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import src.Resources;
import src.Resources.XmlNames;

/**
 * XML Tools
 */
public final class XMLTools {

    /**
	 * Extracts all NameChars defined for XML 1.0.
	 * @see XmlNames
	 * @return Valid NameChar sequence
	 */
    public static final String getXmlNameChar(final String s) {
        StringBuilder out = new StringBuilder();
        Matcher mNameChar = Resources.XmlNames.patternNameChar.matcher(s);
        while (mNameChar.find()) {
            out.append(mNameChar.group());
        }
        return out.toString();
    }

    /**
	 * Extracts all NameChars defined for XML 1.0
	 * @return Valid NameChar sequence
	 */
    public static final String getXmlNameChar(final String s, final String extension) {
        if (extension.length() == 0) return getXmlNameChar(s);
        StringBuilder out = new StringBuilder();
        Matcher mNameChar = Pattern.compile("(?x)(" + Resources.XmlNames.nameChar + " | [" + extension + "])+").matcher(s);
        while (mNameChar.find()) {
            out.append(mNameChar.group());
        }
        return out.toString();
    }

    /**
	 * <p>Not testing for XML correctness! Simply merges equal arguments.</p>
	 * <p><strong>Example:</strong> Input is<br />
	 * <code>class="a" style="width: 10px;" class="b"</code><br />
	 * Output is<br />
	 * <code>class="a b" style="width: 10px;"</code><br />
	 * (for <code>valueSeparator = " "</code> and <code>argumentSeparator = " "</code>.)
	 * </p>
	 */
    public static final String mergeArguments(final String args, final String valueSeparator, final String argumentSeparator, final boolean acceptSingleArgs) {
        HashMap<String, String> argumentsMap = new HashMap<String, String>();
        StringBuilder arguments = new StringBuilder();
        String arg;
        Pattern keyValue = Pattern.compile("(?:^|\\s)([^\\\"\\s]+)=\"([^\\\"]+)\"");
        Matcher matcherKV = keyValue.matcher(args);
        int last = 0;
        while (matcherKV.find()) {
            arguments.append(args.subSequence(last, matcherKV.start()));
            last = matcherKV.end();
            if (argumentsMap.containsKey(matcherKV.group(1))) {
                arg = argumentsMap.get(matcherKV.group(1)) + valueSeparator + matcherKV.group(2);
                argumentsMap.put(matcherKV.group(1), arg);
            } else {
                argumentsMap.put(matcherKV.group(1), matcherKV.group(2));
            }
        }
        if (argumentsMap.size() == 0) return args;
        arguments.append(args.substring(last));
        if (!acceptSingleArgs) {
            arguments = new StringBuilder();
        }
        for (Entry<String, String> m : argumentsMap.entrySet()) {
            arguments.append(argumentSeparator + m.getKey() + "=\"" + m.getValue() + "\"");
        }
        return arguments.toString();
    }
}
