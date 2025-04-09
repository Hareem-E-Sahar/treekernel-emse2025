package net.sourceforge.jcpusim;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

/**
 * The HTMLIfier convers ASM code into nice syntax colored html.
 * The generated HTML complies to W3C XHTML 1.0 Strict
 */
public class HTMLIfier {

    /** shared resources and methods */
    protected Core core;

    /** the Document Type Definition (DTD) */
    protected final String HTML_DTD = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";

    /** html space equiv */
    protected final String HTML_SPACE = "&nbsp;";

    /** html tab equiv (4 spaces) */
    protected final String HTML_TAB = "&nbsp;&nbsp;&nbsp;&nbsp;";

    /** something to identify html files as being created by this project.. */
    protected final String createdByText = "Created by JCPUSim";

    /** html output writer */
    protected PrintWriter out;

    /** */
    protected StringBuffer buffer;

    /** true if line numbers are to be displayed */
    protected boolean displayLineNumbers;

    /** input assembly file */
    protected File input;

    /** css file to include */
    protected File style;

    /** line count */
    protected int lineCount;

    /**
	 * Create a new instance of the HTMLIfier
	 * @param core - shared resources and methods
	 */
    public HTMLIfier(Core core) {
        this.core = core;
        buffer = new StringBuffer();
        displayLineNumbers = true;
        lineCount = 1;
    }

    /**
	 * Create HTML from the input file
	 * @param input the file to create html for
	 * @param style the css file to use
	 * @param output the file to write to
	 */
    public void HTMLIfy(File input, File style, File output) {
        core.log("HTMLIfying " + input.getName() + " using " + style.getName() + "..");
        this.input = input;
        this.style = style;
        try {
            BufferedReader in = new BufferedReader(new FileReader(input));
            while (in.readLine() != null) lineCount++;
        } catch (java.io.FileNotFoundException e) {
        } catch (java.io.IOException e) {
        }
        try {
            out = new PrintWriter(output);
        } catch (java.io.FileNotFoundException e) {
        }
        createHeader();
        createCSS();
        createContent();
        createFooter();
        save(output);
        core.log("HTMLIfication complete, saved to " + output.getName());
    }

    /**
	 * Create the HTML header
	 */
    protected void createHeader() {
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + HTML_DTD);
        out.println("<!-- " + createdByText + " -->");
        out.println("<html>\n\t<head>\n\t\t<title>" + input.getName() + "</title>");
    }

    /**
	 * Create the CSS part of the header,
	 * as defined by the parser in the name attributes of the syntax highlight file
	 */
    protected void createCSS() {
        out.println("\t\t<style type=\"text/css\">");
        try {
            BufferedReader in = new BufferedReader(new FileReader(style));
            String temp = in.readLine();
            while (temp != null) {
                out.println("\t\t\t" + temp);
                temp = in.readLine();
            }
        } catch (java.io.FileNotFoundException e) {
        } catch (java.io.IOException e) {
        }
        out.println("\t\t</style>\n\t</head>\n\t<body>");
    }

    /**
	 * Create the HTML content
	 */
    protected void createContent() {
        out.println("\t\t<div class=\"header\">" + input.getName() + "</div>");
        if (displayLineNumbers) {
            out.println("\t\t<div class=\"lineNumber\">");
            out.print("\t\t\t");
            for (int i = 1; i < lineCount; i++) {
                out.print(i);
                if (i < lineCount - 1) out.print("<br />");
            }
            out.println("\n\t\t</div>");
        }
        out.print("\t\t<div class=\"code\">");
        try {
            BufferedReader in = new BufferedReader(new FileReader(input));
            String lineBuffer = in.readLine();
            int end = 0;
            while (lineBuffer != null) {
                Pattern pattern;
                Matcher matcher;
                if (lineBuffer.trim().length() > 0) {
                    buffer.append("\n\t\t\t");
                    if (lineBuffer.trim().charAt(0) != ';') {
                        for (int i = 0; i < core.getSyntaxParser().elementCount; i++) {
                            if (i != core.getSyntaxParser().TYPE_COMMENT) {
                                pattern = Pattern.compile(core.getSyntaxParser().getRegularExpression(i), Pattern.CASE_INSENSITIVE);
                                matcher = pattern.matcher("\n" + lineBuffer.toUpperCase().split(";")[0] + "\n");
                                String restOfLine = null;
                                while (matcher.find()) {
                                    end = matcher.end();
                                    try {
                                        if (restOfLine != null) {
                                        }
                                        buffer.append("<span class=\"" + core.getSyntaxParser().syntaxData[i][0] + "\">" + lineBuffer.substring(matcher.start(), matcher.end() - 1) + "</span>");
                                        System.out.println("REST:" + lineBuffer.substring(matcher.end() - 1, lineBuffer.length()));
                                    } catch (java.lang.StringIndexOutOfBoundsException e) {
                                        buffer.append("-- DEBUG --> \"" + lineBuffer + "\" start:" + matcher.start() + " end:" + matcher.end() + "<br />");
                                    }
                                }
                            }
                        }
                    }
                }
                pattern = Pattern.compile(core.getSyntaxParser().getRegularExpression(core.getSyntaxParser().TYPE_COMMENT));
                matcher = pattern.matcher(lineBuffer);
                if (matcher.find()) {
                    end = lineBuffer.length();
                    buffer.append("<span class=\"comments\">" + lineBuffer + "</span>");
                }
                if (end < lineBuffer.length()) buffer.append(lineBuffer.substring(end, lineBuffer.length()));
                if (--lineCount > 1) buffer.append("\n\t\t\t<br />");
                lineBuffer = in.readLine();
            }
        } catch (java.io.FileNotFoundException e) {
        } catch (java.io.IOException e) {
        }
        String output = buffer.toString();
        output = output.replaceAll(" (?!/>|class=)", HTML_SPACE);
        out.println(output + "\n\t\t</div>");
    }

    /**
	 * Create the HTML footer
	 */
    protected void createFooter() {
        out.println("\t</body>\n</html>");
    }

    /**
	 * Save the HTML to a file
	 * @param file to save the HTML to
	 * @return true on success
	 */
    protected boolean save(File file) {
        out.flush();
        return false;
    }
}
