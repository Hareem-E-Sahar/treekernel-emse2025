package com.liferay.portlet.wiki.translators;

import com.liferay.portal.kernel.util.DigesterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <a href="BaseTranslator.java.html"><b><i>View Source</i></b></a>
 *
 * @author Jorge Ferrer
 *
 */
public abstract class BaseTranslator {

    public String translate(String content) {
        _protectedMap.clear();
        content = preProcess(content);
        content = runRegexps(content);
        content = postProcess(content);
        return content;
    }

    protected String postProcess(String content) {
        return unprotectNowikiText(content);
    }

    protected String preProcess(String content) {
        content = _normalizeLineBreaks(content);
        for (String regexp : nowikiRegexps) {
            content = protectText(content, regexp);
        }
        return content;
    }

    protected String protectText(String content, String markupRegex) {
        Matcher matcher = Pattern.compile(markupRegex, Pattern.MULTILINE | Pattern.DOTALL).matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String protectedText = matcher.group();
            String hash = DigesterUtil.digest(protectedText);
            matcher.appendReplacement(sb, "$1" + hash + "$3");
            _protectedMap.put(hash, matcher.group(2));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    protected String runRegexps(String content) {
        for (String regexp : regexps.keySet()) {
            String replacement = regexps.get(regexp);
            content = runRegexp(content, regexp, replacement);
        }
        return content;
    }

    protected String runRegexp(String content, String regexp, String replacement) {
        Matcher matcher = Pattern.compile(regexp, Pattern.MULTILINE).matcher(content);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    protected String unprotectNowikiText(String content) {
        List<String> hashList = new ArrayList<String>(_protectedMap.keySet());
        for (int i = hashList.size() - 1; i >= 0; i--) {
            String hash = hashList.get(i);
            String protectedMarkup = _protectedMap.get(hash);
            content = content.replace(hash, protectedMarkup);
        }
        return content;
    }

    private String _normalizeLineBreaks(String content) {
        content = StringUtil.replace(content, new String[] { StringPool.RETURN_NEW_LINE, StringPool.RETURN }, new String[] { StringPool.NEW_LINE, StringPool.NEW_LINE });
        return content;
    }

    protected Map<String, String> regexps = new LinkedHashMap<String, String>();

    protected List<String> nowikiRegexps = new LinkedList<String>();

    private Map<String, String> _protectedMap = new LinkedHashMap<String, String>();
}
