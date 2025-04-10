package de.knowwe.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplitUtility {

    /**
	 * Splits the text by the splitSymbol disregarding splitSymbols which are
	 * quoted
	 * 
	 * @param text
	 * @param splitSymbol
	 * @return
	 */
    public static List<StringFragment> splitUnquoted(String text, String splitSymbol) {
        boolean quoted = false;
        List<StringFragment> parts = new ArrayList<StringFragment>();
        StringBuffer actualPart = new StringBuffer();
        int startOfNewPart = 0;
        for (int i = 0; i < text.length(); i++) {
            if (isUnEscapedQuote(text, i)) {
                quoted = !quoted;
            }
            if (quoted) {
                actualPart.append(text.charAt(i));
                continue;
            }
            if ((i + splitSymbol.length() <= text.length()) && text.subSequence(i, i + splitSymbol.length()).equals(splitSymbol)) {
                parts.add(new StringFragment(actualPart.toString(), startOfNewPart, text));
                actualPart = new StringBuffer();
                i += splitSymbol.length() - 1;
                startOfNewPart = i + 1;
                continue;
            }
            actualPart.append(text.charAt(i));
        }
        if (!actualPart.toString().matches("\\s*")) {
            parts.add(new StringFragment(actualPart.toString(), startOfNewPart, text));
        }
        return parts;
    }

    private static boolean isUnEscapedQuote(String text, int i) {
        return text.charAt(i) == '"' && (i == 0 || text.charAt(i - 1) != '\\');
    }

    public static boolean containsUnquoted(String text, String symbol) {
        return splitUnquoted(text + "1", symbol).size() > 1;
    }

    public static String unquote(String text) {
        return KnowWEUtils.trimQuotes(text);
    }

    /**
	 * scans the 'text' for the (first) occurrence of 'symbol' which is not
	 * embedded in quotes ('"')
	 * 
	 * @param text
	 * @param symbol
	 * @return
	 */
    public static int indexOfUnquoted(String text, String symbol) {
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            if (isUnEscapedQuote(text, i)) {
                quoted = !quoted;
            }
            if (quoted) {
                continue;
            }
            if ((i + symbol.length() <= text.length()) && text.subSequence(i, i + symbol.length()).equals(symbol)) {
                return i;
            }
        }
        return -1;
    }

    /**
	 * return whether some position in a string is in quotes or not
	 * 
	 * @param text
	 * @param symbol
	 * @return
	 */
    public static boolean isQuoted(String text, int index) {
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            if (isUnEscapedQuote(text, i)) {
                quoted = !quoted;
            }
            if ((i == index)) {
                return quoted;
            }
        }
        return false;
    }

    /**
	 * scans the 'text' for the last occurrence of 'symbol' which is not
	 * embraced in quotes ('"')
	 * 
	 * @param text
	 * @param symbol
	 * @return
	 */
    public static int lastIndexOfUnquoted(String text, String symbol) {
        boolean quoted = false;
        int lastIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (isUnEscapedQuote(text, i)) {
                quoted = !quoted;
            }
            if (quoted) {
                continue;
            }
            if ((i + symbol.length() <= text.length()) && text.subSequence(i, i + symbol.length()).equals(symbol)) {
                lastIndex = i;
            }
        }
        return lastIndex;
    }

    /**
	 * For a given index of an opening symbol (usually brackets) it finds (char
	 * index of) the corresponding closing bracket/symbol
	 * 
	 * @param text
	 * @param openBracketIndex
	 * @param open
	 * @param close
	 * @return
	 */
    public static int findIndexOfClosingBracket(String text, int openBracketIndex, char open, char close) {
        if (text.charAt(openBracketIndex) == open) {
            boolean quoted = false;
            int closedBrackets = -1;
            for (int i = openBracketIndex + 1; i < text.length(); i++) {
                char current = text.charAt(i);
                if (isUnEscapedQuote(text, i)) {
                    quoted = !quoted;
                } else if (!quoted && current == open) {
                    closedBrackets--;
                } else if (!quoted && current == close) {
                    closedBrackets++;
                }
                if (closedBrackets == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
	 * Scans the 'text' for occurrences of 'symbol' which are not embraced by
	 * (unquoted) brackets (opening bracket 'open' and closing bracket 'close')
	 * Here the kind of bracket can be passed as char, however it will also work
	 * with char that are not brackets.. ;-)
	 * 
	 * @param text
	 * @param symbol
	 * @param open
	 * @param close
	 * @return
	 */
    public static List<Integer> findIndicesOfUnbraced(String text, String symbol, char open, char close) {
        List<Integer> result = new ArrayList<Integer>();
        boolean quoted = false;
        int openBrackets = 0;
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (isUnEscapedQuote(text, i)) {
                quoted = !quoted;
            } else if (!quoted && current == open) {
                openBrackets--;
            } else if (!quoted && current == close) {
                openBrackets++;
            } else if (openBrackets == 0 && !quoted) {
                if (text.substring(i).startsWith(symbol)) {
                    result.add(i);
                }
            }
        }
        return result;
    }

    public static String[] getCharacterChains(String text) {
        String content = text.trim();
        String[] entries = content.split(" ");
        List<String> nonEmpty = new ArrayList<String>();
        for (String string : entries) {
            if (!string.equals("")) {
                nonEmpty.add(string);
            }
        }
        return nonEmpty.toArray(new String[nonEmpty.size()]);
    }

    public static List<StringFragment> getLineFragmentation(String text) {
        List<StringFragment> result = new ArrayList<StringFragment>();
        Pattern pattern = Pattern.compile("\\r?\\n");
        Matcher m = pattern.matcher(text);
        int lastIndex = 0;
        while (m.find()) {
            result.add(new StringFragment(text.substring(lastIndex, m.start()), lastIndex, text));
            lastIndex = m.end();
        }
        return result;
    }

    public static StringFragment getFirstNonEmptyLineContent(String text) {
        List<StringFragment> lineFragmentation = getLineFragmentation(text);
        for (StringFragment stringFragment : lineFragmentation) {
            if (stringFragment.getContent().trim().length() > 0) return stringFragment;
        }
        return null;
    }
}
