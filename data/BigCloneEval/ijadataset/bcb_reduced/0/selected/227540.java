package net.bull.javamelody;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Partie du rapport html pour les informations systèmes sur le serveur.
 * @author Emeric Vernat
 */
class HtmlJavaInformationsReport {

    private static final String[] OS = { "linux", "windows", "mac", "solaris", "hp", "ibm" };

    private static final String[] APPLICATION_SERVERS = { "tomcat", "glassfish", "jonas", "jetty", "oracle", "bea", "ibm" };

    private static final double MIN_VALUE = 0;

    private static final double MAX_VALUE = 100;

    private static final int PARTIAL_BLOCKS = 5;

    private static final int FULL_BLOCKS = 10;

    private static final double UNIT_SIZE = (MAX_VALUE - MIN_VALUE) / (FULL_BLOCKS * PARTIAL_BLOCKS);

    private static int uniqueByPageSequence;

    private final boolean noDatabase = Parameters.isNoDatabase();

    private final DecimalFormat integerFormat = I18N.createIntegerFormat();

    private final DecimalFormat decimalFormat = I18N.createPercentFormat();

    private final List<JavaInformations> javaInformationsList;

    private final Writer writer;

    HtmlJavaInformationsReport(List<JavaInformations> javaInformationsList, Writer writer) {
        super();
        assert javaInformationsList != null && !javaInformationsList.isEmpty();
        assert writer != null;
        this.javaInformationsList = javaInformationsList;
        this.writer = writer;
    }

    void toHtml() throws IOException {
        for (final JavaInformations javaInformations : javaInformationsList) {
            writeSummary(javaInformations);
        }
        if (!noDatabase) {
            write("<br/><br/>");
        }
        final String br = "<br/>";
        if (javaInformationsList.get(0).getSessionCount() >= 0) {
            write(br);
        }
        if (javaInformationsList.get(0).getSystemLoadAverage() >= 0) {
            write(br);
        }
        writeln("<br/><br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        writeShowHideLink("detailsJava", "#Details#");
        writeln("<br/><br/>");
        writeln("<div id='detailsJava' style='display: none;'><div>");
        final boolean repeatHost = javaInformationsList.size() > 1;
        for (final JavaInformations javaInformations : javaInformationsList) {
            writeDetails(javaInformations, repeatHost);
        }
        writeln("</div></div>");
    }

    private void writeSummary(JavaInformations javaInformations) throws IOException {
        final String lineEnd = "</td> </tr>";
        final String columnAndLineEnd = "</td><td>" + lineEnd;
        writeln("<table align='left' border='0' cellspacing='0' cellpadding='2' summary='#Informations_systemes#'>");
        writeln("<tr><td>#Host#: </td><td><b>" + javaInformations.getHost() + "</b>" + lineEnd);
        final MemoryInformations memoryInformations = javaInformations.getMemoryInformations();
        final long usedMemory = memoryInformations.getUsedMemory();
        final long maxMemory = memoryInformations.getMaxMemory();
        write("<tr><td>#memoire_utilisee#: </td><td>");
        writeGraph("usedMemory", integerFormat.format(usedMemory / 1024 / 1024));
        writeln(" #Mo# / " + integerFormat.format(maxMemory / 1024 / 1024) + " #Mo#&nbsp;&nbsp;&nbsp;</td><td>");
        writeln(toBar(memoryInformations.getUsedMemoryPercentage()));
        writeln(lineEnd);
        if (javaInformations.getSessionCount() >= 0) {
            write("<tr><td>#nb_sessions_http#: </td><td>");
            writeGraph("httpSessions", integerFormat.format(javaInformations.getSessionCount()));
            writeln(columnAndLineEnd);
        }
        write("<tr><td>#nb_threads_actifs#<br/>(#Requetes_http_en_cours#): </td><td>");
        writeGraph("activeThreads", integerFormat.format(javaInformations.getActiveThreadCount()));
        writeln(columnAndLineEnd);
        if (!noDatabase) {
            write("<tr><td>#nb_connexions_actives#: </td><td>");
            writeGraph("activeConnections", integerFormat.format(javaInformations.getActiveConnectionCount()));
            writeln(columnAndLineEnd);
            final int usedConnectionCount = javaInformations.getUsedConnectionCount();
            final int maxConnectionCount = javaInformations.getMaxConnectionCount();
            write("<tr><td>#nb_connexions_utilisees#<br/>(#ouvertes#): </td><td>");
            writeGraph("usedConnections", integerFormat.format(usedConnectionCount));
            if (maxConnectionCount > 0) {
                writeln(" / " + integerFormat.format(maxConnectionCount) + "&nbsp;&nbsp;&nbsp;</td><td>");
                writeln(toBar(javaInformations.getUsedConnectionPercentage()));
            }
            writeln(lineEnd);
        }
        if (javaInformations.getSystemLoadAverage() >= 0) {
            write("<tr><td>#Charge_systeme#</td><td>");
            writeGraph("systemLoad", decimalFormat.format(javaInformations.getSystemLoadAverage()));
            writeln(columnAndLineEnd);
        }
        writeln("</table>");
    }

    private void writeDetails(JavaInformations javaInformations, boolean repeatHost) throws IOException {
        final String columnEnd = "</td></tr>";
        writeln("<table align='left' border='0' cellspacing='0' cellpadding='2' summary='#Details_systeme#'>");
        if (repeatHost) {
            writeln("<tr><td>#Host#: </td><td><b>" + javaInformations.getHost() + "</b>" + columnEnd);
        }
        writeln("<tr><td>#OS#: </td><td>");
        final String osIconName = getOSIconName(javaInformations.getOS());
        if (osIconName != null) {
            writeln("<img src='?resource=servers/" + osIconName + "' alt='#OS#'/>");
        }
        writeln(javaInformations.getOS() + " (" + javaInformations.getAvailableProcessors() + " #coeurs#)" + columnEnd);
        writeln("<tr><td>#Java#: </td><td>" + javaInformations.getJavaVersion() + columnEnd);
        write("<tr><td>#JVM#: </td><td>" + javaInformations.getJvmVersion());
        if (javaInformations.getJvmVersion().contains("Client")) {
            write("&nbsp;&nbsp;&nbsp;<img src='?resource=alert.png' alt=\"#Client_JVM#\" title=\"#Client_JVM#\"/>");
        }
        writeln(columnEnd);
        writeln("<tr><td>#PID#: </td><td>" + javaInformations.getPID() + columnEnd);
        final long unixOpenFileDescriptorCount = javaInformations.getUnixOpenFileDescriptorCount();
        if (unixOpenFileDescriptorCount >= 0) {
            final long unixMaxFileDescriptorCount = javaInformations.getUnixMaxFileDescriptorCount();
            write("<tr><td>#nb_fichiers#</td><td>");
            writeGraph("fileDescriptors", integerFormat.format(unixOpenFileDescriptorCount));
            writeln(" / " + integerFormat.format(unixMaxFileDescriptorCount) + "&nbsp;&nbsp;&nbsp;");
            writeln(toBar(javaInformations.getUnixOpenFileDescriptorPercentage()));
            writeln(columnEnd);
        }
        writeServerInfoAndContextPath(javaInformations);
        writeln("<tr><td>#Demarrage#: </td><td>" + I18N.createDateAndTimeFormat().format(javaInformations.getStartDate()) + columnEnd);
        write("<tr><td valign='top'>#Arguments_JVM#: </td><td>");
        writer.write(I18N.htmlEncode(javaInformations.getJvmArguments(), false) + columnEnd);
        writeln("");
        if (javaInformations.getSessionCount() >= 0) {
            write("<tr><td>#httpSessionsMeanAge#: </td><td>");
            writeGraph("httpSessionsMeanAge", integerFormat.format(javaInformations.getSessionMeanAgeInMinutes()));
            writeln(columnEnd);
        }
        writeTomcatInformations(javaInformations.getTomcatInformationsList());
        writeMemoryInformations(javaInformations.getMemoryInformations());
        if (javaInformations.getFreeDiskSpaceInTemp() >= 0) {
            writeln("<tr><td>#Free_disk_space#: </td><td>" + integerFormat.format(javaInformations.getFreeDiskSpaceInTemp() / 1024 / 1024) + " #Mo# " + columnEnd);
        }
        writeDatabaseVersionAndDataSourceDetails(javaInformations);
        if (javaInformations.isDependenciesEnabled()) {
            writeln("<tr><td valign='top'>#Dependencies#: </td><td>");
            writeDependencies(javaInformations);
            writeln(columnEnd);
        }
        writeln("</table>");
    }

    private void writeServerInfoAndContextPath(JavaInformations javaInformations) throws IOException {
        final String serverInfo = javaInformations.getServerInfo();
        if (serverInfo != null) {
            final String columnEnd = " </td></tr>";
            writeln("<tr><td>#Serveur#: </td><td>");
            final String applicationServerIconName = getApplicationServerIconName(serverInfo);
            if (applicationServerIconName != null) {
                writeln("<img src='?resource=servers/" + applicationServerIconName + "' alt='#Serveur#'/>");
            }
            writeln(serverInfo + columnEnd);
            writeln("<tr><td>#Contexte_webapp#: </td><td>" + javaInformations.getContextPath() + columnEnd);
        }
    }

    private void writeDatabaseVersionAndDataSourceDetails(JavaInformations javaInformations) throws IOException {
        final String columnEnd = "</td></tr>";
        if (!noDatabase && javaInformations.getDataBaseVersion() != null) {
            writeln("<tr><td valign='top'>#Base_de_donnees#: </td><td>");
            writer.write(replaceEolWithBr(javaInformations.getDataBaseVersion()).replaceAll("[&]", "&amp;"));
            writeln(columnEnd);
        }
        if (javaInformations.getDataSourceDetails() != null) {
            writeln("<tr><td valign='top'>#DataSource_jdbc#: </td><td>");
            writer.write(replaceEolWithBr(javaInformations.getDataSourceDetails()) + "<a href='http://commons.apache.org/dbcp/apidocs/org/apache/commons/dbcp/BasicDataSource.html'" + " class='noPrint' target='_blank'>DataSource reference</a>");
            writeln(columnEnd);
        }
    }

    static String getOSIconName(String os) {
        final String tmp = os.toLowerCase(Locale.getDefault());
        for (final String anOS : OS) {
            if (tmp.contains(anOS)) {
                return anOS + ".png";
            }
        }
        return null;
    }

    static String getApplicationServerIconName(String appServer) {
        final String tmp = appServer.toLowerCase(Locale.getDefault());
        for (final String applicationServer : APPLICATION_SERVERS) {
            if (tmp.contains(applicationServer)) {
                return applicationServer + ".png";
            }
        }
        return null;
    }

    private void writeTomcatInformations(List<TomcatInformations> tomcatInformationsList) throws IOException {
        final List<TomcatInformations> list = new ArrayList<TomcatInformations>();
        for (final TomcatInformations tomcatInformations : tomcatInformationsList) {
            if (tomcatInformations.getRequestCount() > 0) {
                list.add(tomcatInformations);
            }
        }
        final boolean onlyOne = list.size() == 1;
        for (final TomcatInformations tomcatInformations : list) {
            writer.write("<tr><td valign='top'>Tomcat " + I18N.htmlEncode(tomcatInformations.getName(), false) + ": </td><td>");
            final int currentThreadsBusy = tomcatInformations.getCurrentThreadsBusy();
            writeln("#busyThreads# = ");
            if (onlyOne) {
                writeGraph("tomcatBusyThreads", integerFormat.format(currentThreadsBusy));
            } else {
                writeln(integerFormat.format(currentThreadsBusy));
            }
            writeln(" /  " + integerFormat.format(tomcatInformations.getMaxThreads()));
            writeln("&nbsp;&nbsp;&nbsp;");
            writeln(toBar(100d * currentThreadsBusy / tomcatInformations.getMaxThreads()));
            writeln("<br/>#bytesReceived# = ");
            if (onlyOne) {
                writeGraph("tomcatBytesReceived", integerFormat.format(tomcatInformations.getBytesReceived()));
            } else {
                writeln(integerFormat.format(tomcatInformations.getBytesReceived()));
            }
            writeln("<br/>#bytesSent# = ");
            if (onlyOne) {
                writeGraph("tomcatBytesSent", integerFormat.format(tomcatInformations.getBytesSent()));
            } else {
                writeln(integerFormat.format(tomcatInformations.getBytesSent()));
            }
            writeln("<br/>#requestCount# = ");
            writeln(integerFormat.format(tomcatInformations.getRequestCount()));
            writeln("<br/>#errorCount# = ");
            writeln(integerFormat.format(tomcatInformations.getErrorCount()));
            writeln("<br/>#processingTime# = ");
            writeln(integerFormat.format(tomcatInformations.getProcessingTime()));
            writeln("<br/>#maxProcessingTime# = ");
            writeln(integerFormat.format(tomcatInformations.getMaxTime()));
            writeln("</td> </tr>");
        }
    }

    private void writeMemoryInformations(MemoryInformations memoryInformations) throws IOException {
        final String columnEnd = "</td></tr>";
        writeln("<tr><td valign='top'>#Gestion_memoire#: </td><td>" + replaceEolWithBr(memoryInformations.getMemoryDetails()).replace(" Mo", " #Mo#") + columnEnd);
        final long usedPermGen = memoryInformations.getUsedPermGen();
        if (usedPermGen > 0) {
            final long maxPermGen = memoryInformations.getMaxPermGen();
            writeln("<tr><td>#Memoire_Perm_Gen#: </td><td>" + integerFormat.format(usedPermGen / 1024 / 1024) + " #Mo#");
            if (maxPermGen > 0) {
                writeln(" / " + integerFormat.format(maxPermGen / 1024 / 1024) + " #Mo#&nbsp;&nbsp;&nbsp;");
                writeln(toBar(memoryInformations.getUsedPermGenPercentage()));
            }
            writeln(columnEnd);
        }
    }

    private void writeDependencies(JavaInformations javaInformations) throws IOException {
        final int nbDependencies = javaInformations.getDependenciesList().size();
        writeln(I18N.getFormattedString("nb_dependencies", nbDependencies));
        if (nbDependencies > 0) {
            uniqueByPageSequence++;
            writeln(" ; &nbsp;&nbsp;&nbsp;");
            writeShowHideLink("detailsDependencies" + uniqueByPageSequence, "#Details#");
            if (javaInformations.doesPomXmlExists() && Parameters.isSystemActionsEnabled()) {
                writeln("&nbsp;&nbsp;&nbsp;<a href='?part=pom.xml' class='noPrint'>");
                writeln("<img src='?resource=xml.png' width='14' height='14' alt=\"#pom.xml#\"/> #pom.xml#</a>");
            }
            writeln("<br/>");
            writeln("<div id='detailsDependencies" + uniqueByPageSequence + "' style='display: none;'><div>");
            writeln(replaceEolWithBr(javaInformations.getDependencies()));
            writeln("</div></div>");
        }
    }

    private void writeGraph(String graph, String value) throws IOException {
        if (javaInformationsList.size() > 1) {
            write(value);
            return;
        }
        write("<a class='tooltip' href='?part=graph&amp;graph=");
        write(graph);
        write("'");
        write(" onmouseover=\"document.getElementById('");
        final String id = "id" + graph;
        write(id);
        write("').src='?graph=");
        write(graph);
        write("&amp;width=100&amp;height=50'; this.onmouseover=null;\" >");
        write("<em><img src='?resource=systeminfo.png' id='");
        write(id);
        write("' alt='graph'/></em>");
        writer.write(value);
        write("</a>");
    }

    static String toBar(double percentValue) {
        final double myPercent = Math.max(Math.min(percentValue, 100d), 0d);
        final StringBuilder sb = new StringBuilder();
        final String body = "<img src=''?resource=bar/rb_{0}.gif'' alt=''+'' title=''" + I18N.createPercentFormat().format(myPercent) + "%'' />";
        final int fullBlockCount = (int) Math.floor(myPercent / (UNIT_SIZE * PARTIAL_BLOCKS));
        final int partialBlockIndex = (int) Math.floor((myPercent - fullBlockCount * UNIT_SIZE * PARTIAL_BLOCKS) / UNIT_SIZE);
        sb.append(MessageFormat.format(body, fullBlockCount > 0 || partialBlockIndex > 0 ? "a" : "a0"));
        final String fullBody = MessageFormat.format(body, PARTIAL_BLOCKS);
        for (int i = 0; i < fullBlockCount; i++) {
            sb.append(fullBody);
        }
        if (partialBlockIndex > 0) {
            final String partialBody = MessageFormat.format(body, partialBlockIndex);
            sb.append(partialBody);
        }
        final int emptyBlocks = FULL_BLOCKS - fullBlockCount - (partialBlockIndex > 0 ? 1 : 0);
        final String emptyBody = MessageFormat.format(body, 0);
        for (int i = 0; i < emptyBlocks; i++) {
            sb.append(emptyBody);
        }
        sb.append(MessageFormat.format(body, fullBlockCount == FULL_BLOCKS ? "b" : "b0"));
        return sb.toString();
    }

    private static String replaceEolWithBr(String string) {
        return string.replaceAll("[\n]", "<br/>");
    }

    private void writeShowHideLink(String idToShow, String label) throws IOException {
        writeln("<a href=\"javascript:showHide('" + idToShow + "');\" class='noPrint'><img id='" + idToShow + "Img' src='?resource=bullets/plus.png' alt=''/> " + label + "</a>");
    }

    private void write(String html) throws IOException {
        I18N.writeTo(html, writer);
    }

    private void writeln(String html) throws IOException {
        I18N.writelnTo(html, writer);
    }
}
