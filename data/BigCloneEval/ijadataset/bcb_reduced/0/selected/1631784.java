package org.apache.commons.cli;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/** 
 * A formatter of help messages for the current command line options
 *
 * @author Slawek Zachcial
 * @author John Keyes (john at integralsource.com)
 **/
public class HelpFormatter {

    /** default number of characters per line */
    public static final int DEFAULT_WIDTH = 74;

    /** default padding to the left of each line */
    public static final int DEFAULT_LEFT_PAD = 1;

    /**
     * the number of characters of padding to be prefixed
     * to each description line
     */
    public static final int DEFAULT_DESC_PAD = 3;

    /** the string to display at the begining of the usage statement */
    public static final String DEFAULT_SYNTAX_PREFIX = "usage: ";

    /** default prefix for shortOpts */
    public static final String DEFAULT_OPT_PREFIX = "-";

    /** default prefix for long Option */
    public static final String DEFAULT_LONG_OPT_PREFIX = "--";

    /** default name for an argument */
    public static final String DEFAULT_ARG_NAME = "arg";

    /**
     * number of characters per line
     *
     * @deprecated Scope will be made private for next major version
     * - use get/setWidth methods instead.
     */
    public int defaultWidth = DEFAULT_WIDTH;

    /**
     * amount of padding to the left of each line
     *
     * @deprecated Scope will be made private for next major version
     * - use get/setLeftPadding methods instead.
     */
    public int defaultLeftPad = DEFAULT_LEFT_PAD;

    /**
     * the number of characters of padding to be prefixed
     * to each description line
     *
     * @deprecated Scope will be made private for next major version
     * - use get/setDescPadding methods instead.
     */
    public int defaultDescPad = DEFAULT_DESC_PAD;

    /**
     * the string to display at the begining of the usage statement
     *
     * @deprecated Scope will be made private for next major version
     * - use get/setSyntaxPrefix methods instead.
     */
    public String defaultSyntaxPrefix = DEFAULT_SYNTAX_PREFIX;

    /**
     * the new line string
     *
     * @deprecated Scope will be made private for next major version
     * - use get/setNewLine methods instead.
     */
    public String defaultNewLine = System.getProperty("line.separator");

    /**
     * the shortOpt prefix
     *
     * @deprecated Scope will be made private for next major version
     * - use get/setOptPrefix methods instead.
     */
    public String defaultOptPrefix = DEFAULT_OPT_PREFIX;

    /**
     * the long Opt prefix
     *
     * @deprecated Scope will be made private for next major version
     * - use get/setLongOptPrefix methods instead.
     */
    public String defaultLongOptPrefix = DEFAULT_LONG_OPT_PREFIX;

    /**
     * the name of the argument
     *
     * @deprecated Scope will be made private for next major version
     * - use get/setArgName methods instead.
     */
    public String defaultArgName = DEFAULT_ARG_NAME;

    /**
     * Sets the 'width'.
     *
     * @param width the new value of 'width'
     */
    public void setWidth(int width) {
        this.defaultWidth = width;
    }

    /**
     * Returns the 'width'.
     *
     * @return the 'width'
     */
    public int getWidth() {
        return this.defaultWidth;
    }

    /**
     * Sets the 'leftPadding'.
     *
     * @param padding the new value of 'leftPadding'
     */
    public void setLeftPadding(int padding) {
        this.defaultLeftPad = padding;
    }

    /**
     * Returns the 'leftPadding'.
     *
     * @return the 'leftPadding'
     */
    public int getLeftPadding() {
        return this.defaultLeftPad;
    }

    /**
     * Sets the 'descPadding'.
     *
     * @param padding the new value of 'descPadding'
     */
    public void setDescPadding(int padding) {
        this.defaultDescPad = padding;
    }

    /**
     * Returns the 'descPadding'.
     *
     * @return the 'descPadding'
     */
    public int getDescPadding() {
        return this.defaultDescPad;
    }

    /**
     * Sets the 'syntaxPrefix'.
     *
     * @param prefix the new value of 'syntaxPrefix'
     */
    public void setSyntaxPrefix(String prefix) {
        this.defaultSyntaxPrefix = prefix;
    }

    /**
     * Returns the 'syntaxPrefix'.
     *
     * @return the 'syntaxPrefix'
     */
    public String getSyntaxPrefix() {
        return this.defaultSyntaxPrefix;
    }

    /**
     * Sets the 'newLine'.
     *
     * @param newline the new value of 'newLine'
     */
    public void setNewLine(String newline) {
        this.defaultNewLine = newline;
    }

    /**
     * Returns the 'newLine'.
     *
     * @return the 'newLine'
     */
    public String getNewLine() {
        return this.defaultNewLine;
    }

    /**
     * Sets the 'optPrefix'.
     *
     * @param prefix the new value of 'optPrefix'
     */
    public void setOptPrefix(String prefix) {
        this.defaultOptPrefix = prefix;
    }

    /**
     * Returns the 'optPrefix'.
     *
     * @return the 'optPrefix'
     */
    public String getOptPrefix() {
        return this.defaultOptPrefix;
    }

    /**
     * Sets the 'longOptPrefix'.
     *
     * @param prefix the new value of 'longOptPrefix'
     */
    public void setLongOptPrefix(String prefix) {
        this.defaultLongOptPrefix = prefix;
    }

    /**
     * Returns the 'longOptPrefix'.
     *
     * @return the 'longOptPrefix'
     */
    public String getLongOptPrefix() {
        return this.defaultLongOptPrefix;
    }

    /**
     * Sets the 'argName'.
     *
     * @param name the new value of 'argName'
     */
    public void setArgName(String name) {
        this.defaultArgName = name;
    }

    /**
     * Returns the 'argName'.
     *
     * @return the 'argName'
     */
    public String getArgName() {
        return this.defaultArgName;
    }

    /**
     * <p>Print the help for <code>options</code> with the specified
     * command line syntax.  This method prints help information to
     * System.out.</p>
     *
     * @param cmdLineSyntax the syntax for this application
     * @param options the Options instance
     */
    public void printHelp(String cmdLineSyntax, Options options) {
        printHelp(defaultWidth, cmdLineSyntax, null, options, null, false);
    }

    /**
     * <p>Print the help for <code>options</code> with the specified
     * command line syntax.  This method prints help information to 
     * System.out.</p>
     *
     * @param cmdLineSyntax the syntax for this application
     * @param options the Options instance
     * @param autoUsage whether to print an automatically generated 
     * usage statement
     */
    public void printHelp(String cmdLineSyntax, Options options, boolean autoUsage) {
        printHelp(defaultWidth, cmdLineSyntax, null, options, null, autoUsage);
    }

    /**
     * <p>Print the help for <code>options</code> with the specified
     * command line syntax.  This method prints help information to
     * System.out.</p>
     *
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the begining of the help
     * @param options the Options instance
     * @param footer the banner to display at the end of the help
     */
    public void printHelp(String cmdLineSyntax, String header, Options options, String footer) {
        printHelp(cmdLineSyntax, header, options, footer, false);
    }

    /**
     * <p>Print the help for <code>options</code> with the specified
     * command line syntax.  This method prints help information to 
     * System.out.</p>
     *
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the begining of the help
     * @param options the Options instance
     * @param footer the banner to display at the end of the help
     * @param autoUsage whether to print an automatically generated 
     * usage statement
     */
    public void printHelp(String cmdLineSyntax, String header, Options options, String footer, boolean autoUsage) {
        printHelp(defaultWidth, cmdLineSyntax, header, options, footer, autoUsage);
    }

    /**
     * <p>Print the help for <code>options</code> with the specified
     * command line syntax.  This method prints help information to
     * System.out.</p>
     *
     * @param width the number of characters to be displayed on each line
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the begining of the help
     * @param options the Options instance
     * @param footer the banner to display at the end of the help
     */
    public void printHelp(int width, String cmdLineSyntax, String header, Options options, String footer) {
        printHelp(width, cmdLineSyntax, header, options, footer, false);
    }

    /**
     * <p>Print the help for <code>options</code> with the specified
     * command line syntax.  This method prints help information to
     * System.out.</p>
     *
     * @param width the number of characters to be displayed on each line
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the begining of the help
     * @param options the Options instance
     * @param footer the banner to display at the end of the help
     * @param autoUsage whether to print an automatically generated 
     * usage statement
     */
    public void printHelp(int width, String cmdLineSyntax, String header, Options options, String footer, boolean autoUsage) {
        PrintWriter pw = new PrintWriter(System.out);
        printHelp(pw, width, cmdLineSyntax, header, options, defaultLeftPad, defaultDescPad, footer, autoUsage);
        pw.flush();
    }

    /**
     * <p>Print the help for <code>options</code> with the specified
     * command line syntax.</p>
     *
     * @param pw the writer to which the help will be written
     * @param width the number of characters to be displayed on each line
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the begining of the help
     * @param options the Options instance
     * @param leftPad the number of characters of padding to be prefixed
     * to each line
     * @param descPad the number of characters of padding to be prefixed
     * to each description line
     * @param footer the banner to display at the end of the help
     */
    public void printHelp(PrintWriter pw, int width, String cmdLineSyntax, String header, Options options, int leftPad, int descPad, String footer) {
        printHelp(pw, width, cmdLineSyntax, header, options, leftPad, descPad, footer, false);
    }

    /**
     * <p>Print the help for <code>options</code> with the specified
     * command line syntax.</p>
     *
     * @param pw the writer to which the help will be written
     * @param width the number of characters to be displayed on each line
     * @param cmdLineSyntax the syntax for this application
     * @param header the banner to display at the begining of the help
     * @param options the Options instance
     * @param leftPad the number of characters of padding to be prefixed
     * to each line
     * @param descPad the number of characters of padding to be prefixed
     * to each description line
     * @param footer the banner to display at the end of the help
     * @param autoUsage whether to print an automatically generated 
     * usage statement
     */
    public void printHelp(PrintWriter pw, int width, String cmdLineSyntax, String header, Options options, int leftPad, int descPad, String footer, boolean autoUsage) {
        if ((cmdLineSyntax == null) || (cmdLineSyntax.length() == 0)) {
            throw new IllegalArgumentException("cmdLineSyntax not provided");
        }
        if (autoUsage) {
            printUsage(pw, width, cmdLineSyntax, options);
        } else {
            printUsage(pw, width, cmdLineSyntax);
        }
        if ((header != null) && (header.trim().length() > 0)) {
            printWrapped(pw, width, header);
        }
        printOptions(pw, width, options, leftPad, descPad);
        if ((footer != null) && (footer.trim().length() > 0)) {
            printWrapped(pw, width, footer);
        }
    }

    /**
     * <p>Prints the usage statement for the specified application.</p>
     *
     * @param pw The PrintWriter to print the usage statement 
     * @param width The number of characters to display per line
     * @param app The application name
     * @param options The command line Options
     *
     */
    public void printUsage(PrintWriter pw, int width, String app, Options options) {
        StringBuffer buff = new StringBuffer(defaultSyntaxPrefix).append(app).append(" ");
        final Collection processedGroups = new ArrayList();
        Option option;
        List optList = new ArrayList(options.getOptions());
        Collections.sort(optList, new OptionComparator());
        for (Iterator i = optList.iterator(); i.hasNext(); ) {
            option = (Option) i.next();
            OptionGroup group = options.getOptionGroup(option);
            if (group != null) {
                if (!processedGroups.contains(group)) {
                    processedGroups.add(group);
                    appendOptionGroup(buff, group);
                }
            } else {
                appendOption(buff, option, option.isRequired());
            }
            if (i.hasNext()) {
                buff.append(" ");
            }
        }
        printWrapped(pw, width, buff.toString().indexOf(' ') + 1, buff.toString());
    }

    /**
     * Appends the usage clause for an OptionGroup to a StringBuffer.  
     * The clause is wrapped in square brackets if the group is required.
     * The display of the options is handled by appendOption
     * @param buff the StringBuffer to append to
     * @param group the group to append
     * @see #appendOption(StringBuffer,Option,boolean)
     */
    private static void appendOptionGroup(final StringBuffer buff, final OptionGroup group) {
        if (!group.isRequired()) {
            buff.append("[");
        }
        List optList = new ArrayList(group.getOptions());
        Collections.sort(optList, new OptionComparator());
        for (Iterator i = optList.iterator(); i.hasNext(); ) {
            appendOption(buff, (Option) i.next(), true);
            if (i.hasNext()) {
                buff.append(" | ");
            }
        }
        if (!group.isRequired()) {
            buff.append("]");
        }
    }

    /**
     * Appends the usage clause for an Option to a StringBuffer.  
     *
     * @param buff the StringBuffer to append to
     * @param option the Option to append
     * @param required whether the Option is required or not
     */
    private static void appendOption(final StringBuffer buff, final Option option, final boolean required) {
        if (!required) {
            buff.append("[");
        }
        if (option.getOpt() != null) {
            buff.append("-").append(option.getOpt());
        } else {
            buff.append("--").append(option.getLongOpt());
        }
        if (option.hasArg() && (option.getArgName() != null)) {
            buff.append(" <").append(option.getArgName()).append(">");
        }
        if (!required) {
            buff.append("]");
        }
    }

    /**
     * <p>Print the cmdLineSyntax to the specified writer, using the
     * specified width.</p>
     *
     * @param pw The printWriter to write the help to
     * @param width The number of characters per line for the usage statement.
     * @param cmdLineSyntax The usage statement.
     */
    public void printUsage(PrintWriter pw, int width, String cmdLineSyntax) {
        int argPos = cmdLineSyntax.indexOf(' ') + 1;
        printWrapped(pw, width, defaultSyntaxPrefix.length() + argPos, defaultSyntaxPrefix + cmdLineSyntax);
    }

    /**
     * <p>Print the help for the specified Options to the specified writer, 
     * using the specified width, left padding and description padding.</p>
     *
     * @param pw The printWriter to write the help to
     * @param width The number of characters to display per line
     * @param options The command line Options
     * @param leftPad the number of characters of padding to be prefixed
     * to each line
     * @param descPad the number of characters of padding to be prefixed
     * to each description line
     */
    public void printOptions(PrintWriter pw, int width, Options options, int leftPad, int descPad) {
        StringBuffer sb = new StringBuffer();
        renderOptions(sb, width, options, leftPad, descPad);
        pw.println(sb.toString());
    }

    /**
     * <p>Print the specified text to the specified PrintWriter.</p>
     *
     * @param pw The printWriter to write the help to
     * @param width The number of characters to display per line
     * @param text The text to be written to the PrintWriter
     */
    public void printWrapped(PrintWriter pw, int width, String text) {
        printWrapped(pw, width, 0, text);
    }

    /**
     * <p>Print the specified text to the specified PrintWriter.</p>
     *
     * @param pw The printWriter to write the help to
     * @param width The number of characters to display per line
     * @param nextLineTabStop The position on the next line for the first tab.
     * @param text The text to be written to the PrintWriter
     */
    public void printWrapped(PrintWriter pw, int width, int nextLineTabStop, String text) {
        StringBuffer sb = new StringBuffer(text.length());
        renderWrappedText(sb, width, nextLineTabStop, text);
        pw.println(sb.toString());
    }

    /**
     * <p>Render the specified Options and return the rendered Options
     * in a StringBuffer.</p>
     *
     * @param sb The StringBuffer to place the rendered Options into.
     * @param width The number of characters to display per line
     * @param options The command line Options
     * @param leftPad the number of characters of padding to be prefixed
     * to each line
     * @param descPad the number of characters of padding to be prefixed
     * to each description line
     *
     * @return the StringBuffer with the rendered Options contents.
     */
    protected StringBuffer renderOptions(StringBuffer sb, int width, Options options, int leftPad, int descPad) {
        final String lpad = createPadding(leftPad);
        final String dpad = createPadding(descPad);
        int max = 0;
        StringBuffer optBuf;
        List prefixList = new ArrayList();
        Option option;
        List optList = options.helpOptions();
        Collections.sort(optList, new OptionComparator());
        for (Iterator i = optList.iterator(); i.hasNext(); ) {
            option = (Option) i.next();
            optBuf = new StringBuffer(8);
            if (option.getOpt() == null) {
                optBuf.append(lpad).append("   " + defaultLongOptPrefix).append(option.getLongOpt());
            } else {
                optBuf.append(lpad).append(defaultOptPrefix).append(option.getOpt());
                if (option.hasLongOpt()) {
                    optBuf.append(',').append(defaultLongOptPrefix).append(option.getLongOpt());
                }
            }
            if (option.hasArg()) {
                if (option.hasArgName()) {
                    optBuf.append(" <").append(option.getArgName()).append(">");
                } else {
                    optBuf.append(' ');
                }
            }
            prefixList.add(optBuf);
            max = (optBuf.length() > max) ? optBuf.length() : max;
        }
        int x = 0;
        for (Iterator i = optList.iterator(); i.hasNext(); ) {
            option = (Option) i.next();
            optBuf = new StringBuffer(prefixList.get(x++).toString());
            if (optBuf.length() < max) {
                optBuf.append(createPadding(max - optBuf.length()));
            }
            optBuf.append(dpad);
            int nextLineTabStop = max + descPad;
            if (option.getDescription() != null) {
                optBuf.append(option.getDescription());
            }
            renderWrappedText(sb, width, nextLineTabStop, optBuf.toString());
            if (i.hasNext()) {
                sb.append(defaultNewLine);
            }
        }
        return sb;
    }

    /**
     * <p>Render the specified text and return the rendered Options
     * in a StringBuffer.</p>
     *
     * @param sb The StringBuffer to place the rendered text into.
     * @param width The number of characters to display per line
     * @param nextLineTabStop The position on the next line for the first tab.
     * @param text The text to be rendered.
     *
     * @return the StringBuffer with the rendered Options contents.
     */
    protected StringBuffer renderWrappedText(StringBuffer sb, int width, int nextLineTabStop, String text) {
        int pos = findWrapPos(text, width, 0);
        if (pos == -1) {
            sb.append(rtrim(text));
            return sb;
        }
        sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        final String padding = createPadding(nextLineTabStop);
        while (true) {
            text = padding + text.substring(pos).trim();
            pos = findWrapPos(text, width, nextLineTabStop);
            if (pos == -1) {
                sb.append(text);
                return sb;
            }
            sb.append(rtrim(text.substring(0, pos))).append(defaultNewLine);
        }
    }

    /**
     * Finds the next text wrap position after <code>startPos</code> for the 
     * text in <code>text</code> with the column width <code>width</code>.
     * The wrap point is the last postion before startPos+width having a 
     * whitespace character (space, \n, \r).
     *
     * @param text The text being searched for the wrap position
     * @param width width of the wrapped text
     * @param startPos position from which to start the lookup whitespace 
     * character
     * @return postion on which the text must be wrapped or -1 if the wrap 
     * position is at the end of the text
     */
    protected int findWrapPos(String text, int width, int startPos) {
        int pos = -1;
        if (((pos = text.indexOf('\n', startPos)) != -1 && pos <= width) || ((pos = text.indexOf('\t', startPos)) != -1 && pos <= width)) {
            return pos + 1;
        } else if ((startPos + width) >= text.length()) {
            return -1;
        }
        pos = startPos + width;
        char c;
        while ((pos >= startPos) && ((c = text.charAt(pos)) != ' ') && (c != '\n') && (c != '\r')) {
            --pos;
        }
        if (pos > startPos) {
            return pos;
        }
        pos = startPos + width;
        while ((pos <= text.length()) && ((c = text.charAt(pos)) != ' ') && (c != '\n') && (c != '\r')) {
            ++pos;
        }
        return (pos == text.length()) ? (-1) : pos;
    }

    /**
     * <p>Return a String of padding of length <code>len</code>.</p>
     *
     * @param len The length of the String of padding to create.
     *
     * @return The String of padding
     */
    protected String createPadding(int len) {
        StringBuffer sb = new StringBuffer(len);
        for (int i = 0; i < len; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * <p>Remove the trailing whitespace from the specified String.</p>
     *
     * @param s The String to remove the trailing padding from.
     *
     * @return The String of without the trailing padding
     */
    protected String rtrim(String s) {
        if ((s == null) || (s.length() == 0)) {
            return s;
        }
        int pos = s.length();
        while ((pos > 0) && Character.isWhitespace(s.charAt(pos - 1))) {
            --pos;
        }
        return s.substring(0, pos);
    }

    /**
     * <p>This class implements the <code>Comparator</code> interface
     * for comparing Options.</p>
     */
    private static class OptionComparator implements Comparator {

        /**
         * <p>Compares its two arguments for order. Returns a negative 
         * integer, zero, or a positive integer as the first argument 
         * is less than, equal to, or greater than the second.</p>
         *
         * @param o1 The first Option to be compared.
         * @param o2 The second Option to be compared.
         *
         * @return a negative integer, zero, or a positive integer as 
         * the first argument is less than, equal to, or greater than the 
         * second.
         */
        public int compare(Object o1, Object o2) {
            Option opt1 = (Option) o1;
            Option opt2 = (Option) o2;
            return opt1.getKey().compareToIgnoreCase(opt2.getKey());
        }
    }
}
