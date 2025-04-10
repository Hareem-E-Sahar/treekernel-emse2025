package barde.writers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import barde.log.Message;
import barde.writers.split.NeverSplitStrategy;
import barde.writers.split.SplitStrategy;

/**
 * This writer shows up the messages in a HTML page, with changeable style thanks to CSS.<br>
 * It may be used to display a static view of a log, or to obtain an HTML table to insert into an existing page.<br>
 * For more functionnalities, use the {@link JavascriptWriter}.<br>
 * @see JavascriptWriter
 * @author cbonar
 */
public class HTMLWriter extends AbstractLogWriter {

    /** include a table header with the description of the columns */
    private boolean includeTableHeader = false;

    /** embed the stylesheet whithin the page */
    private boolean embedStyleSheet = false;

    /** the list of the stylesheets (CSS) to use */
    protected URL[] stylesheets;

    /** the current output stream */
    protected PrintStream out;

    /** if not null, the title of the HTML document */
    protected String title;

    /** false when at least one message has been written ; permits to delay I/O operations to the first call to <tt>write()</tt> */
    protected boolean firstTime = true;

    /** if not null, the file where it's currently writting to */
    protected File currentFile = null;

    /** the splitter used to sequence the output to several files */
    protected SplitStrategy split = new NeverSplitStrategy();

    /** the base URI for splits */
    protected URI firstFile = null;

    /**
	 * This is the preferred constructor.
	 * @param out the stream to write into
	 * @param stylesheets the CSS stylesheets to use (will be imported respect to the order in this array).
	 * @param title a title for this page
	 */
    public HTMLWriter(PrintStream out, URL[] stylesheets, String title) {
        this.out = out;
        this.stylesheets = stylesheets;
        this.title = title;
    }

    public HTMLWriter(OutputStream os, URL[] stylesheets, String title) {
        this(new PrintStream(os), stylesheets, title);
    }

    /**
	 * Allow the output to be split in several pages
	 */
    public HTMLWriter(File f, URL[] stylesheets, String title) throws FileNotFoundException {
        this(new FileOutputStream(f), stylesheets, title);
        this.currentFile = f;
        this.firstFile = f.toURI();
    }

    public void write(Message message) throws IOException {
        if (this.firstTime) {
            writeHeader();
            this.firstTime = false;
        }
        if (this.split.shouldSplit(message)) {
            beforeSplit();
            this.split.split();
        }
        String date = message.getDateFormat().format(message.getDate());
        String channel = message.getChannel();
        String source = message.getAvatar();
        String content = message.getContent();
        String hoverTRIEPatch = " onMouseOver=\"this.id='trhover'\" onMouseOut=\"this.id=''\"";
        String hoverTDIEPatch = " onMouseOver=\"this.id='tdhover'\" onMouseOut=\"this.id=''\"";
        this.out.print("<tr class=\"" + channel.replaceAll(" ", "_") + "\"" + hoverTRIEPatch + ">");
        this.out.print("<td class=\"date\" class=\"" + source + "\"" + hoverTDIEPatch + ">" + date + "</td>");
        this.out.print("<td class=\"channel\" class=\"" + source + "\"" + hoverTDIEPatch + ">" + channel + "</td>");
        this.out.print("<td class=\"source\" class=\"" + source + "\"" + hoverTDIEPatch + ">" + source + "</td>");
        this.out.print("<td class=\"message\" class=\"" + source + "\"" + hoverTDIEPatch + ">" + content + "</td>");
        this.out.println("</tr>");
    }

    /** Adds the HTML footer to the output, so that it forms a well-formed HTML page. */
    protected void writeHeader() throws IOException, UnsupportedOperationException {
        this.out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        this.out.println("<HTML>");
        this.out.println("<HEAD>");
        this.out.println("<!-- generated by " + getClass().getName() + " -->");
        this.out.println("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">");
        this.out.println("<TITLE>" + this.title + "</TITLE>");
        for (int css = 0; css < this.stylesheets.length; css++) {
            if (this.embedStyleSheet) {
                try {
                    this.out.println("<STYLE TYPE=\"text/css\">");
                    InputStream in = this.stylesheets[css].openStream();
                    for (int read = in.read(); read != -1; read = in.read()) this.out.write(read);
                    this.out.println("</STYLE>");
                } catch (IOException ioe) {
                    this.out.println("<!--");
                    ioe.printStackTrace(out);
                    this.out.println("-->");
                }
            } else this.out.println("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"" + this.stylesheets[css] + "\" charset=\"UTF-8\"/>");
        }
        this.out.println("</HEAD>");
        this.out.println("<BODY>");
        this.out.println("<TABLE>");
        if (this.includeTableHeader) this.out.println("<TR class=\"header\"><TH class=\"date\">DATE</TH><TH class=\"channel\">CHANNEL</TH><TH class=\"source\">SOURCE</TH><TH class=\"message\">MESSAGE</TH></TR>");
    }

    protected void writeFooter() throws IOException, UnsupportedOperationException {
        this.out.println("</TABLE>");
        this.out.println("</BODY>");
        this.out.println("</HTML>");
    }

    public void beforeSplit() throws IOException, UnsupportedOperationException {
        writeFooter();
        this.out.close();
    }

    public void afterSplit(File f) throws IOException, UnsupportedOperationException {
        this.currentFile = f;
        this.out = new PrintStream(new FileOutputStream(f));
        writeHeader();
    }

    public boolean commentsAreSupported() {
        return true;
    }

    public void writeComment(String comment) throws IOException, UnsupportedOperationException {
        this.out.println("<!-- " + comment.replaceAll("-->", "--\\>") + "-->");
    }

    public void close() throws IOException {
        writeFooter();
        this.out.close();
    }

    public LogWriter includeTableHeader(boolean yesno) {
        this.includeTableHeader = yesno;
        return this;
    }

    public LogWriter embedStyleSheet(boolean yesno) {
        this.embedStyleSheet = yesno;
        return this;
    }
}
