package net.bull.javamelody;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Partie du rapport html pour les connections jdbc ouvertes.
 * @author Emeric Vernat
 */
class HtmlConnectionInformationsReport {

    private final List<ConnectionInformations> connectionsInformations;

    private final Writer writer;

    private final DateFormat dateTimeFormat = I18N.createDateAndTimeFormat();

    private final Map<Long, Thread> threadsById;

    private final Map<Thread, StackTraceElement[]> stackTracesByThread;

    HtmlConnectionInformationsReport(List<ConnectionInformations> connectionsInformations, Writer writer) {
        super();
        assert connectionsInformations != null;
        assert writer != null;
        this.connectionsInformations = connectionsInformations;
        this.writer = writer;
        if (JavaInformations.STACK_TRACES_ENABLED) {
            this.stackTracesByThread = Thread.getAllStackTraces();
            this.threadsById = new HashMap<Long, Thread>(stackTracesByThread.size());
            for (final Thread thread : stackTracesByThread.keySet()) {
                this.threadsById.put(thread.getId(), thread);
            }
        } else {
            this.stackTracesByThread = Collections.emptyMap();
            final List<Thread> threads = JavaInformations.getThreadsFromThreadGroups();
            this.threadsById = new HashMap<Long, Thread>(threads.size());
            for (final Thread thread : threads) {
                this.threadsById.put(thread.getId(), thread);
            }
        }
    }

    void toHtml() throws IOException {
        writeBackAndRefreshLinks();
        writeln("<br/>");
        writeln("<img width='24' height='24' src='?resource=db.png' alt='#Connexions_jdbc_ouvertes#' />&nbsp;");
        writeln("<b>#Connexions_jdbc_ouvertes#</b>");
        writeln("<br/><br/>#connexions_intro#<br/><br/>");
        writeConnections();
    }

    void writeConnections() throws IOException {
        if (connectionsInformations.isEmpty()) {
            writeln("#Aucune_connexion_jdbc_ouverte#");
            return;
        }
        writeln("<table class='sortable' width='100%' border='1' cellspacing='0' cellpadding='2' summary='#Connexions_jdbc_ouvertes#'>");
        write("<thead><tr><th class='sorttable_date'>#Date_et_stack_trace_ouverture#</th>");
        if (JavaInformations.STACK_TRACES_ENABLED) {
            write("<th>#Thread_et_stack_trace_actuelle#</th>");
        } else {
            write("<th>#Thread#</th>");
        }
        writeln("</tr></thead><tbody>");
        boolean odd = false;
        for (final ConnectionInformations connection : connectionsInformations) {
            if (odd) {
                write("<tr class='odd' onmouseover=\"this.className='highlight'\" onmouseout=\"this.className='odd'\">");
            } else {
                write("<tr onmouseover=\"this.className='highlight'\" onmouseout=\"this.className=''\">");
            }
            odd = !odd;
            writeConnection(connection);
            writeln("</tr>");
        }
        writeln("</tbody></table>");
        final int nbConnections = connectionsInformations.size();
        writeln("<div align='right'>" + I18N.getFormattedString("nb_connexions_ouvertes", nbConnections) + "</div>");
    }

    private void writeBackAndRefreshLinks() throws IOException {
        writeln("<div class='noPrint'>");
        writeln("<a href='javascript:history.back()'>");
        writeln("<img src='?resource=action_back.png' alt='#Retour#'/> #Retour#</a>");
        writeln("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        writeln("<a href='?part=connections'>");
        writeln("<img src='?resource=action_refresh.png' alt='#Actualiser#'/> #Actualiser#</a>");
        writeln("</div>");
    }

    private void writeConnection(ConnectionInformations connection) throws IOException {
        write("<td align='right'>");
        writeTextWithStackTrace(dateTimeFormat.format(connection.getOpeningDate()), connection.getOpeningStackTrace());
        write("</td><td>");
        final Thread thread = threadsById.get(connection.getThreadId());
        if (thread == null) {
            write("&nbsp;");
        } else {
            final StackTraceElement[] stackTrace = stackTracesByThread.get(thread);
            writeTextWithStackTrace(thread.getName(), stackTrace != null ? Arrays.asList(stackTrace) : null);
        }
        write("</td>");
    }

    private void writeTextWithStackTrace(String text, List<StackTraceElement> stackTrace) throws IOException {
        final String encodedText = htmlEncode(text);
        if (stackTrace != null && !stackTrace.isEmpty()) {
            writeln("<a class='tooltip'>");
            writeln("<em>");
            writer.write(encodedText);
            writeln("<br/>");
            for (final StackTraceElement stackTraceElement : stackTrace) {
                writeln(htmlEncode(stackTraceElement.toString()));
                writeln("<br/>");
            }
            writeln("</em>");
            writer.write(encodedText);
            writeln("</a>");
        } else {
            writer.write(encodedText);
        }
    }

    private static String htmlEncode(String text) {
        return I18N.htmlEncode(text, true);
    }

    private void write(String html) throws IOException {
        I18N.writeTo(html, writer);
    }

    private void writeln(String html) throws IOException {
        I18N.writelnTo(html, writer);
    }
}
