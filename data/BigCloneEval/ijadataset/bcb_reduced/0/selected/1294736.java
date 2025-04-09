package com.loribel.commons.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.loribel.commons.util.collection.GB_ArrayList;

/**
 * Tools to use Regular expressions (regex).
 * 
 * See Test : GB_RegexToolsTest
 * 
 * @author Gregory Borelli
 */
public final class GB_RegexTools {

    /**
     * Escape every character that is a special character in a Java regex.
     */
    public static String escapeRegexChars(String regex) {
        char[] l_escapes = { '\\', '?', '^', '[', ']', '{', '}', '.', '$', '+', '*', '|' };
        for (int i = 0; i < l_escapes.length; i++) {
            regex = STools.replace(regex, String.valueOf(l_escapes[i]), "\\" + l_escapes[i]);
        }
        return regex;
    }

    /**
     * Use {1}...{n} into output expression.
     */
    public static String extract(String a_src, String a_findPattern, String a_templateOutput) {
        Pattern l_pattern = Pattern.compile(a_findPattern);
        Matcher l_matcher = l_pattern.matcher(a_src);
        int l_groupCount = l_matcher.groupCount();
        StringBuffer retour = new StringBuffer();
        while (l_matcher.find()) {
            String[] l_groups = new String[l_groupCount + 1];
            for (int i = 0; i < l_groupCount + 1; i++) {
                l_groups[i] = l_matcher.group(i);
            }
            String l_value = replaceGroupIntoExpression(a_templateOutput, l_groups);
            retour.append(l_value);
        }
        return retour.toString();
    }

    /**
     * Returns the group found with this pattern.
     * The length of the returned array is (found item * group count).
     * retour[] = {group1, .., groupN, group1, .., groupN, ...}
     * Attention ne retourne pas le group 0.
     */
    public static String[] extractToArray(String a_src, String a_findPattern) {
        return extractToArray(a_src, a_findPattern, false);
    }

    /**
     * Returns the group found with this pattern.
     * <ul>
     *   <li>If a_returnGroup0 == true: The length of the returned array is (found item * group count).
     *   retour[] = {group1, .., groupN, group1, .., groupN, ...}</li>
     *   <li>If a_returnGroup0 == false : The length of the returned array is (found item * (group count+1)).
     *   retour[] = {group0, group1, .., groupN, group0, group1, .., groupN, ...}</li>
     *   </ul> 
     */
    public static String[] extractToArray(String a_src, String a_findPattern, boolean a_returnGroup0) {
        Pattern l_pattern = Pattern.compile(a_findPattern);
        Matcher l_matcher = l_pattern.matcher(a_src);
        int l_groupCount = l_matcher.groupCount();
        int l_indexStart;
        if (a_returnGroup0) {
            l_indexStart = 0;
        } else {
            l_indexStart = 1;
        }
        Collection retour = new ArrayList();
        while (l_matcher.find()) {
            for (int i = l_indexStart; i < l_groupCount + 1; i++) {
                retour.add(l_matcher.group(i));
            }
        }
        return (String[]) retour.toArray(new String[retour.size()]);
    }

    public static String[] find(String a_src, String a_findPattern) {
        List retour = new ArrayList();
        Pattern l_pattern = Pattern.compile(a_findPattern);
        Matcher l_matcher = l_pattern.matcher(a_src);
        while (l_matcher.find()) {
            retour.add(l_matcher.group());
        }
        return (String[]) retour.toArray(new String[0]);
    }

    public static String[] find(String a_src, String a_findPattern, int a_groupIndex) {
        List retour = new ArrayList();
        Pattern l_pattern = Pattern.compile(a_findPattern);
        Matcher l_matcher = l_pattern.matcher(a_src);
        while (l_matcher.find()) {
            retour.add(l_matcher.group(a_groupIndex));
        }
        return (String[]) retour.toArray(new String[0]);
    }

    /**
     * Return null if not found.
     */
    public static String findFirst(String a_src, String a_findPattern, int a_groupIndex) {
        if (a_src == null) {
            return null;
        }
        Pattern l_pattern = Pattern.compile(a_findPattern);
        Matcher l_matcher = l_pattern.matcher(a_src);
        if (l_matcher.find()) {
            return l_matcher.group(a_groupIndex);
        }
        return null;
    }

    public static int indexOf(String a_src, String a_findPattern) {
        Pattern l_pattern = Pattern.compile(a_findPattern);
        Matcher l_matcher = l_pattern.matcher(a_src);
        if (l_matcher.find()) {
            return l_matcher.start();
        }
        return -1;
    }

    /**
     * Compiles the given regular expression and attempts to match the given
     * input against it.
     *
     * @param a_pattern String -
     * @param a_src String -
     *
     * @return boolean
     */
    public static boolean matches(String a_src, String a_pattern) {
        if (STools.isNull(a_pattern)) {
            return true;
        }
        if (a_src == null) {
            return false;
        }
        Pattern l_pattern = Pattern.compile(a_pattern);
        Matcher l_matcher = l_pattern.matcher(a_src);
        return l_matcher.matches();
    }

    /**
     * Replaces every subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     *
     * @param a_src String -
     * @param a_findPattern String -
     * @param a_replace String - Utiliser $1, $2 pour utiliser les groupes trouv�s
     *
     * @return String
     */
    public static String replace(String a_src, String a_findPattern, String a_replace) {
        Pattern l_pattern = Pattern.compile(a_findPattern);
        Matcher l_matcher = l_pattern.matcher(a_src);
        return l_matcher.replaceAll(a_replace);
    }

    public static String replaceAllRecurse(String a_content, String a_regex, String a_replace) {
        if (a_content == null) {
            return null;
        }
        String l_content = null;
        String retour = a_content;
        while (!retour.equals(l_content)) {
            l_content = retour;
            retour = l_content.replaceAll(a_regex, a_replace);
        }
        return retour;
    }

    /**
     * Remplacement par regex de a_findPattern par a_replace.
     * <ul>
     *   <li>retour[0] : String a_src transform�e par le find/replace</li>
     *   <li>retour[i] : Toutes les String trouv�es dans a_src 
     *   et remplac�es par a_replace</li>
     * </ul>
     */
    static String[] replaceAndExtract(String a_src, String a_findPattern, String a_replace) {
        String l_content = a_src;
        Pattern l_pattern = Pattern.compile(a_findPattern);
        Matcher l_matcher = l_pattern.matcher(l_content);
        List retour = new ArrayList();
        while (l_matcher.find()) {
            retour.add(l_matcher.group(0));
            l_content = l_matcher.replaceFirst(a_replace);
            l_matcher = l_pattern.matcher(l_content);
        }
        retour.add(0, l_content);
        return (String[]) retour.toArray(new String[retour.size()]);
    }

    /**
     * Replaces the first subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     *
     * @param a_src String -
     * @param a_findPattern String -
     * @param a_replace String -
     *
     * @return String
     */
    public static String replaceFirst(String a_src, String a_findPattern, String a_replace) {
        Pattern l_pattern = Pattern.compile(a_findPattern);
        Matcher l_matcher = l_pattern.matcher(a_src);
        return l_matcher.replaceFirst(a_replace);
    }

    /**
     * Remplacement dans a_src de a_replace (simple String) par 
     * les �l�ments du tableaux a_extract.
     * Cette m�thode est souvent utiliser pour ignorer des blocs 
     * dans un find/replace et l'utilisation de replaceAndExtract.
     */
    static String replaceFromExtract(String a_src, String a_replace, String[] a_extract, int a_startArrayIndex) {
        String retour = a_src;
        Pattern l_pattern = Pattern.compile(a_replace);
        Matcher l_matcher = l_pattern.matcher(retour);
        int l_index = a_startArrayIndex;
        while (l_matcher.find()) {
            retour = l_matcher.replaceFirst(a_extract[l_index]);
            l_matcher = l_pattern.matcher(retour);
            l_index++;
        }
        return retour;
    }

    static String replaceGroupIntoExpression(String a_expression, String[] a_groupValues) {
        int len = CTools.getSize(a_groupValues);
        String retour = a_expression;
        for (int i = len - 1; i > -1; i--) {
            retour = STools.replace(retour, "{" + i + "}", a_groupValues[i]);
        }
        return retour;
    }

    public static String replaceIgnoreBlocs(String a_content, String a_regexIgnore, String a_replaceTemp, String a_regexFind, String a_replace) {
        String[] l_values = replaceAndExtract(a_content, a_regexIgnore, a_replaceTemp);
        String retour = l_values[0];
        retour = retour.replaceAll(a_regexFind, a_replace);
        retour = replaceFromExtract(retour, a_replaceTemp, l_values, 1);
        return retour;
    }

    /**
     * Replaces every subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     *
     * You can use {0}, {1} into the replace String to use the value of group.
     *
     * @return String
     */
    public static String replaceWithGroup(String a_src, String a_findPattern, String a_replace) {
        StringBuffer retour = new StringBuffer();
        Pattern l_pattern = Pattern.compile(a_findPattern);
        String l_src = a_src;
        Matcher l_matcher = l_pattern.matcher(l_src);
        int l_groupCount = l_matcher.groupCount();
        while (l_matcher.find()) {
            String[] l_groups = new String[l_groupCount + 1];
            for (int i = 0; i < l_groupCount + 1; i++) {
                l_groups[i] = l_matcher.group(i);
            }
            String l_replace = replaceGroupIntoExpression(a_replace, l_groups);
            retour.append(l_src.substring(0, l_matcher.start(0)));
            retour.append(l_replace);
            l_src = l_src.substring(l_matcher.end(0));
            l_matcher = l_pattern.matcher(l_src);
        }
        retour.append(l_src);
        return retour.toString();
    }

    public static String replaceWithGroupRecurse(String a_src, String a_findPattern, String a_replace) {
        String retour = a_src;
        String l_content = null;
        while (!retour.equals(l_content)) {
            l_content = retour;
            retour = replaceWithGroup(l_content, a_findPattern, a_replace);
        }
        return retour;
    }

    /**
     * Splits the given input sequence around matches of this pattern.
     * <p>
     * Attention les s�parateurs ne sont pas retourn�s dans la collection
     * r�sultat.
     * <p>
     *
     * @param a_separatorPattern String -
     * @param a_src String -
     *
     * @return Collection
     */
    public static Collection split(String a_separatorPattern, String a_src) {
        Pattern l_pattern = Pattern.compile(a_separatorPattern);
        String[] l_array = l_pattern.split(a_src);
        GB_ArrayList retour = new GB_ArrayList();
        retour.addAll(l_array);
        return retour;
    }

    private GB_RegexTools() {
    }
}
