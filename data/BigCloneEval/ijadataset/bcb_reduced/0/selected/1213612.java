package wiki.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import util.TextUtils;

public class HtmlRenderer {

    public static void main(String[] args) {
        System.out.println(renderPage("..\nh1. Some *text*\n...\n--\n# 1"));
    }

    private static final StringBuilder css = new StringBuilder(2048);

    public static void loadCss(File file) {
        try {
            FileReader in = new FileReader(file);
            char[] cbuf = new char[512];
            int rlen;
            while ((rlen = in.read(cbuf)) != -1) {
                css.append(cbuf, 0, rlen);
            }
        } catch (IOException ex) {
        }
    }

    private static final int BLANK = 0;

    private static final int HEADING = 1;

    private static final int TEXT = 2;

    private static final int LIST = 3;

    /**
	 * See confluence markup
	 */
    public static String renderPage(String wikiText) {
        StringBuilder strb = new StringBuilder(wikiText.length() * 2);
        strb.append("<html>\n");
        strb.append("<head><style type=\"text/css\">\n");
        strb.append(css);
        strb.append("\n</style></head>");
        strb.append("<body>\n");
        String[] lines = wikiText.split("\r?\n");
        renderPageFragment(strb, lines);
        strb.append("<body>\n");
        strb.append("</html>");
        return strb.toString();
    }

    public static void renderPageFragment(StringBuilder strb, String[] lines) {
        int[] types = getTypes(lines);
        int eltNbLines;
        for (int i = 0; i < lines.length; i += eltNbLines) {
            String line = lines[i];
            int type = types[i];
            if (type == HEADING) {
                renderHeading(strb, line);
                eltNbLines = 1;
            } else if (type == TEXT) {
                eltNbLines = renderParagraph(strb, lines, types, i);
            } else if (type == LIST) {
                eltNbLines = renderList(strb, lines, types, i, 1);
            } else {
                eltNbLines = 1;
            }
        }
    }

    public static void renderHeading(StringBuilder strb, String line) {
        char level = line.charAt(1);
        if (level > '3') {
            throw new RuntimeException("Level must be 1, 2 or 3");
        }
        strb.append("<h").append(level).append(">");
        renderText(strb, line, 4, line.length());
        strb.append("</h").append(level).append(">\n");
    }

    /**
	 * @return the number of lines consumed
	 */
    public static int renderParagraph(StringBuilder strb, String[] lines, int[] types, int index) {
        int nbLines = 0;
        strb.append("<p>");
        while (index < lines.length && types[index] == TEXT) {
            if (nbLines > 0) {
                strb.append("<br>\n");
            }
            String line = lines[index];
            renderText(strb, line, 0, line.length());
            index++;
            nbLines++;
        }
        strb.append("</p>\n");
        return nbLines;
    }

    /**
	 * @return the number of lines consumed
	 */
    public static int renderList(StringBuilder strb, String[] lines, int[] types, int index, int level) {
        int nbLines = 0;
        String levelPath = lines[index].substring(0, level);
        char levelChar = levelPath.charAt(level - 1);
        String tag;
        String attrs = null;
        if (levelChar == '#') {
            tag = "ol";
        } else if (levelChar == '*') {
            tag = "ul";
        } else {
            throw new RuntimeException();
        }
        strb.append("<").append(tag).append((attrs != null ? attrs : "")).append(">\n");
        boolean openLi = false;
        while (index < lines.length && (types[index] == LIST || types[index] == BLANK)) {
            int iterationNbLines = 1;
            if (types[index] == LIST) {
                String line = lines[index];
                String listPath = line.substring(0, line.indexOf(' '));
                if (!listPath.startsWith(levelPath)) {
                    break;
                } else if (listPath.length() == levelPath.length()) {
                    if (openLi) {
                        strb.append("</li>\n");
                        openLi = false;
                    }
                    strb.append("<li>");
                    renderText(strb, line, level + 1, line.length());
                    openLi = true;
                } else {
                    if (!openLi) {
                        strb.append("<li>");
                        openLi = true;
                    }
                    strb.append("\n");
                    iterationNbLines = renderList(strb, lines, types, index, level + 1);
                }
            }
            index += iterationNbLines;
            nbLines += iterationNbLines;
        }
        if (openLi) {
            strb.append("</li>\n");
        }
        strb.append("</").append(tag).append(">\n");
        return nbLines;
    }

    public static void renderText(StringBuilder strb, String text, int start, int end) {
        parseTextInlinedMacro(strb, text, start, end);
    }

    /**
	 * Note: Recursive method
	 */
    private static int[] indexOfMacro(String text, int start, int end) {
        int i = TextUtils.indexOfNotEscaped(text, '{', start, '\\');
        if (i != -1 && i < end) {
            int j = TextUtils.indexOfNotEscaped(text, '}', i + 1, '\\');
            if (j != -1 && j < end && j - i > 1) {
                return new int[] { i, j };
            }
        }
        return null;
    }

    private static final Pattern COLON_SPLIT = Pattern.compile(":");

    private static final Pattern ARG_SPLIT = Pattern.compile("\\|");

    public static void parseTextInlinedMacro(StringBuilder strb, String text, int start, int end) {
        int i = start;
        int[] index;
        while ((index = indexOfMacro(text, i, end)) != null) {
            parseTextInlinedElement(strb, text, i, index[0]);
            String macro = text.substring(index[0] + 1, index[1]);
            String[] tokens = COLON_SPLIT.split(macro);
            String name = tokens[0];
            String[] args = (tokens.length == 1 ? null : ARG_SPLIT.split(tokens[1]));
            int macroEnd = text.indexOf("{" + name + "}", index[1] + 1);
            if (macroEnd == -1) {
                renderMacro(strb, name, args, null, 0, 0);
                i = index[1] + 1;
            } else {
                renderMacro(strb, name, args, text, index[1] + 1, macroEnd);
                i = macroEnd + name.length() + 2;
            }
        }
        parseTextInlinedElement(strb, text, i, end);
    }

    private static void renderMacro(StringBuilder strb, String name, String[] args, String text, int start, int end) {
        if (name.equals("noformat")) {
            if (start != end) {
                strb.append(text, start, end);
            }
        } else if (name.equals("link")) {
            String href = (args.length == 1 ? args[0] : args[1]);
            String alias = args[0];
            strb.append("<a href=\"").append(href).append("\">").append(alias).append("</a>");
        } else if (name.equals("image")) {
            String src = args[0];
            strb.append("<img src=\"").append(src).append("\"/>");
        } else {
            throw new RuntimeException("Unknown macro: " + name);
        }
    }

    /**
	 * Example: "..*some text*..." -> "..<b>some text</b>..."
	 */
    public static void parseTextInlinedElement(StringBuilder strb, String text, int start, int end) {
        Pattern boldPattern = Pattern.compile("(^|[\\. ,])([\\*\\+\\_])(.+)\\2($|[\\. ,])");
        Matcher matcher = boldPattern.matcher(text);
        matcher.region(start, end);
        int i = start, j, k;
        while (matcher.find()) {
            j = matcher.start(2);
            k = matcher.end(2);
            strb.append(text, i, j - 1);
            renderTextInlinedElement(strb, text, j, k);
            i = k + 1;
        }
        if (i < text.length()) {
            strb.append(text, i, end);
        }
    }

    private static final char BOLD = '*';

    private static final char UNDERLINE = '+';

    private static final char ITALIC = '_';

    private static void renderTextInlinedElement(StringBuilder strb, String text, int start, int end) {
        char eltChar = text.charAt(start);
        if (eltChar == BOLD) {
            strb.append("<b>").append(text, start, end).append("</b>");
        } else if (eltChar == UNDERLINE) {
            strb.append("<u>").append(text, start, end).append("</u>");
        } else if (eltChar == ITALIC) {
            strb.append("<i>").append(text, start, end).append("</i>");
        } else {
            throw new RuntimeException("Unknown formating: " + eltChar);
        }
    }

    private static final Pattern BLANK_PATTERN = Pattern.compile("\\s*");

    private static final Pattern HEADING_PATTERN = Pattern.compile("h\\d. ");

    private static final Pattern LIST_PATTERN = Pattern.compile("[#\\*]+ ");

    public static int[] getTypes(String[] lines) {
        int[] types = new int[lines.length];
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (HEADING_PATTERN.matcher(line).lookingAt()) {
                types[i] = HEADING;
            } else if (LIST_PATTERN.matcher(line).lookingAt()) {
                types[i] = LIST;
            } else if (!BLANK_PATTERN.matcher(line).matches()) {
                types[i] = TEXT;
            } else {
                types[i] = BLANK;
            }
        }
        return types;
    }
}
