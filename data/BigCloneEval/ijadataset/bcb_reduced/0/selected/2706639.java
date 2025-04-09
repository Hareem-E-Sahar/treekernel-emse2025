package uk.ac.ed.rapid.jsp;

import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ed.rapid.util.StringUtil;

public class Util {

    /**
	 * Generates code to get the value of a variable
	 * 
	 * @param name
	 *            Name of the variable
	 * @return String with code to retrieve a variable.
	 */
    public static String getVariable(String name) {
        return "<%=StringUtil.HTMLify(job.getVariable(" + name + ").resolve(rapidData.getJobData(selection.getjobID())).get(selection.getSubJobIndex()))%>";
    }

    /**
	 * Creates JSP code to create a new printer for a value
	 * 
	 * @param printer
	 *            Class name of the printer object to create. Subclass of
	 *            uk.ac.ed.rapid.jsp.ValuePrinter
	 * @param args
	 *            arguments to pass to the printers' constructor
	 * @return JSP code for creating a new printer
	 */
    public static String newPrinter(String printer, String... args) {
        String result = "<%printer = new " + printer + "(currentPage";
        for (int i = 0; i < args.length; i++) result += ", \"" + StringUtil.removeCR(args[i]) + "\"";
        result += ");%>\n";
        return result;
    }

    /**
	 * Calls the print method of a value object.
	 * 
	 * @param symbol
	 *            Symbol of the value
	 * @return JSP code
	 */
    public static String callPrinter(String symbol) {
        return "<%=symbolTable.getSymbol(\"" + symbol + "\").get(rapidData, selection.getJobID()).print(printer)%>";
    }

    public static String prettyPrint(String input) {
        int indent = 0;
        String regex = "(\\s*\n\\s*)+|(\\<\\%\\@.*\\%\\>)|(\\<\\%\\=.*\\%\\>)|(\\%\\>)|(\\<\\%)|(\\<[^\\>]*\\>)";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        int previous = 0;
        List<String> result = new Vector<String>();
        while (m.find()) {
            if (previous != m.start()) {
                String inBetween = input.substring(previous, m.start()).trim();
                if (!inBetween.isEmpty()) result.add(inBetween);
            }
            result.add(input.substring(m.start(), m.end()));
            previous = m.end();
        }
        if (input.length() != previous) result.add(input.substring(previous, input.length()));
        String pretty = "";
        boolean noindent = false;
        for (String out : result) {
            if ("<pre>".equals(out)) noindent = true;
            if ("</pre>".equals(out)) noindent = false;
            if (noindent) pretty += out; else if (out.matches("(\\s*\n\\s*)+")) {
                pretty += "\n";
            } else if (out.matches("\\<\\%\\=.*\\%\\>") || out.matches("\\<.*/\\>")) {
                pretty += out;
            } else {
                if (out.matches("^(\\%\\>)$")) indent = indent - 2;
                for (int rep = 0; rep < indent; rep++) {
                    pretty += " ";
                }
                if ("%>".equals(out)) pretty += "\n" + out; else if ("<%".equals(out)) {
                    indent = indent + 2;
                    pretty += out + "\n";
                } else pretty += out;
            }
        }
        return pretty;
    }

    public static boolean isOnlyWhiteSpace(String in) {
        boolean result = true;
        for (int i = 0; i < in.length(); i++) {
            result = result && (' ' == in.charAt(i));
        }
        return result;
    }

    public static String removeWhiteSpace(String in) {
        int i = 0;
        while (i < in.length() && in.charAt(i) == ' ') i++;
        int j = in.length() - 1;
        while (j > 0 && in.charAt(j) == ' ') j--;
        if (j < i) return ""; else return in.substring(i, j + 1);
    }

    /**
	 * Changes the CRs in a string into HTML line breaks
	 * 
	 * @param input
	 * @return
	 */
    public static String htmlify(String input) {
        String result = "";
        String[] lines = input.split("\n");
        for (String line : lines) {
            result += line + "<br>\n";
        }
        return result;
    }

    /**
	 * Inserts code to replace variables in a string, when necessary
	 * 
	 * @param input
	 *            input string
	 * @return output JSP code containing variable replacing code
	 */
    public static String insertVariables(String input) {
        if (input.contains("$")) {
            String result = "";
            String[] lines = input.split("(\n|\r\n)");
            for (String line : lines) {
                if (line.contains("$")) {
                    line = StringUtil.replaceQuotes(line);
                    result += "<%=VariableResolver.resolve(\"" + line + "\", rapidData.getJobData(selection.getJobID()), rapidData.getStaticTable(), selection.getSubJobIndex())%>";
                } else result += line + "\n";
            }
            return result;
        } else return input;
    }

    /**
	 * Replaces %URL(url) by context encoded URL
	 */
    public static String insertURLS(String input) {
        String regexp = "(\\%)+URL\\([^ \\)]*\\)";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(input);
        int start = 0;
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            int startGroup = matcher.start();
            int endGroup = matcher.end();
            String group = matcher.group();
            result.append(input.substring(start, startGroup));
            start = matcher.end();
            while (group.startsWith("%%")) {
                group = group.substring(2);
                startGroup = startGroup + 2;
                result.append("%");
            }
            if (group.startsWith("%URL")) {
                String url = input.substring(startGroup + 5, endGroup - 1);
                result.append("<%=renderResponse.encodeURL(renderRequest.getContextPath()+ \"" + url + "\")%>");
            } else result.append(input.substring(startGroup, endGroup));
        }
        if (input.length() > start) result.append(input.substring(start, input.length()));
        return result.toString();
    }
}
