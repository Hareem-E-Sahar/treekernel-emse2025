package net.sourceforge.processdash.tool.export.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import net.sourceforge.processdash.DashboardContext;
import net.sourceforge.processdash.ProcessDashboard;
import net.sourceforge.processdash.data.util.TopDownBottomUpJanitor;
import net.sourceforge.processdash.ev.EVDependencyCalculator;
import net.sourceforge.processdash.ev.EVTaskList;
import net.sourceforge.processdash.ev.EVTaskListMerged;
import net.sourceforge.processdash.templates.DashPackage;
import net.sourceforge.processdash.templates.TemplateLoader;
import net.sourceforge.processdash.tool.export.mgr.Cancellable;
import net.sourceforge.processdash.tool.export.mgr.CompletionStatus;
import net.sourceforge.processdash.tool.export.mgr.ExportFileEntry;
import net.sourceforge.processdash.util.HTMLUtils;
import net.sourceforge.processdash.util.StringUtils;
import net.sourceforge.processdash.util.ThreadThrottler;
import net.sourceforge.processdash.util.XMLUtils;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class ArchiveMetricsFileExporter implements Runnable, ArchiveMetricsXmlConstants, CompletionStatus.Capable, Cancellable {

    private static final String MERGED_PREFIX = "MERGED:";

    private static final String DATA_FILE_NAME = "data.xml";

    private static final String DEFECT_FILE_NAME = "defects.xml";

    private static final String TIME_FILE_NAME = "time.xml";

    private static final String EV_FILE_NAME = "ev.xml";

    private DashboardContext ctx;

    private ExportFileStream dest;

    private Collection filter;

    private List metricsIncludes;

    private List metricsExcludes;

    private List additionalEntries;

    private CompletionStatus completionStatus = CompletionStatus.NOT_RUN_STATUS;

    private static final Logger logger = Logger.getLogger(ArchiveMetricsFileExporter.class.getName());

    public ArchiveMetricsFileExporter(DashboardContext ctx, File dest, Collection filter) {
        this(ctx, dest, null, filter, null, null, null);
    }

    public ArchiveMetricsFileExporter(DashboardContext ctx, File dest, String url, Collection filter, List metricsIncludes, List metricsExcludes, List additionalEntries) {
        this.ctx = ctx;
        this.dest = new ExportFileStream(url, dest);
        this.filter = filter;
        this.metricsIncludes = metricsIncludes;
        this.metricsExcludes = metricsExcludes;
        this.additionalEntries = additionalEntries;
    }

    public CompletionStatus getCompletionStatus() {
        return completionStatus;
    }

    public void tryCancel() {
        dest.abort();
    }

    public void run() {
        try {
            doExport();
            completionStatus = new CompletionStatus(CompletionStatus.SUCCESS, dest.getTarget(), null);
        } catch (Exception ioe) {
            completionStatus = new CompletionStatus(CompletionStatus.ERROR, dest.getTarget(), ioe);
            ioe.printStackTrace();
            tryCancel();
        }
        ctx.getData().gc(filter);
    }

    private void doExport() throws IOException {
        OutputStream outStream = dest.getOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(outStream));
        EST_TIME_JANITOR.cleanup(ctx);
        Collection taskListNames = writeData(zipOut);
        if (!taskListNames.isEmpty()) writeTaskLists(zipOut, taskListNames);
        writeDefects(zipOut);
        writeTimeLogEntries(zipOut);
        writeAditionalEntries(zipOut);
        writeManifest(zipOut, !taskListNames.isEmpty());
        zipOut.close();
        dest.finish();
    }

    private void writeManifest(ZipOutputStream zipOut, boolean includeTaskLists) throws IOException {
        zipOut.putNextEntry(new ZipEntry(MANIFEST_FILE_NAME));
        XmlSerializer xml = null;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            xml = factory.newSerializer();
        } catch (XmlPullParserException xppe) {
            throw new RuntimeException("Couldn't obtain xml serializer", xppe);
        }
        xml.setOutput(zipOut, ENCODING);
        xml.startDocument(ENCODING, Boolean.TRUE);
        xml.ignorableWhitespace(NEWLINE + NEWLINE);
        xml.startTag(null, ARCHIVE_ELEM);
        xml.attribute(null, TYPE_ATTR, FILE_TYPE_ARCHIVE);
        xml.ignorableWhitespace(NEWLINE);
        writeManifestMetaData(xml);
        writeManifestFileEntry(xml, DATA_FILE_NAME, FILE_TYPE_METRICS, "1");
        writeManifestFileEntry(xml, DEFECT_FILE_NAME, FILE_TYPE_DEFECTS, "1");
        writeManifestFileEntry(xml, TIME_FILE_NAME, FILE_TYPE_TIME_LOG, "1");
        if (includeTaskLists) writeManifestFileEntry(xml, EV_FILE_NAME, FILE_TYPE_EARNED_VALUE, "1");
        if (additionalEntries != null) for (Iterator i = additionalEntries.iterator(); i.hasNext(); ) {
            ExportFileEntry file = (ExportFileEntry) i.next();
            writeManifestFileEntry(xml, file.getFilename(), file.getType(), file.getVersion());
        }
        xml.endTag(null, ARCHIVE_ELEM);
        xml.ignorableWhitespace(NEWLINE);
        xml.endDocument();
        zipOut.closeEntry();
    }

    private void writeManifestMetaData(XmlSerializer xml) throws IOException {
        xml.ignorableWhitespace(INDENT);
        xml.startTag(null, EXPORTED_TAG);
        String owner = ProcessDashboard.getOwnerName(ctx.getData());
        if (owner != null) xml.attribute(null, OWNER_ATTR, owner);
        xml.attribute(null, WHEN_ATTR, XMLUtils.saveDate(new Date()));
        List packages = TemplateLoader.getPackages();
        for (Iterator i = packages.iterator(); i.hasNext(); ) {
            DashPackage pkg = (DashPackage) i.next();
            xml.ignorableWhitespace(NEWLINE + INDENT + INDENT);
            xml.startTag(null, PACKAGE_ELEM);
            if (pkg.id != null) xml.attribute(null, PACKAGE_ID_ATTR, pkg.id);
            if (pkg.version != null) xml.attribute(null, VERSION_ATTR, pkg.version);
            xml.endTag(null, PACKAGE_ELEM);
        }
        xml.ignorableWhitespace(NEWLINE + INDENT);
        xml.endTag(null, EXPORTED_TAG);
        xml.ignorableWhitespace(NEWLINE);
    }

    private void writeManifestFileEntry(XmlSerializer xml, String filename, String type, String version) throws IOException {
        xml.ignorableWhitespace(INDENT);
        xml.startTag(null, FILE_ELEM);
        xml.attribute(null, FILE_NAME_ATTR, filename);
        if (StringUtils.hasValue(type)) xml.attribute(null, TYPE_ATTR, type);
        if (StringUtils.hasValue(version)) xml.attribute(null, VERSION_ATTR, version);
        xml.endTag(null, FILE_ELEM);
        xml.ignorableWhitespace(NEWLINE);
    }

    private Collection writeData(ZipOutputStream zipOut) throws IOException {
        zipOut.putNextEntry(new ZipEntry(DATA_FILE_NAME));
        ExportedDataValueIterator baseIter = new ExportedDataValueIterator(ctx.getData(), ctx.getHierarchy(), filter, metricsIncludes, metricsExcludes);
        DefaultDataExportFilter ddef;
        TaskListDataWatcher taskListWatcher;
        if (baseIter.isUsingExplicitNames()) {
            logger.fine("Using explicit name approach");
            Iterator taskListSearcher = new ExportedDataValueIterator(ctx.getData(), ctx.getHierarchy(), filter, null, Collections.singleton("."));
            taskListWatcher = new TaskListDataWatcher(taskListSearcher);
            while (taskListWatcher.hasNext()) {
                ThreadThrottler.tick();
                taskListWatcher.next();
            }
            ddef = new DefaultDataExportFilter(baseIter);
            ddef.setSkipProcessAutoData(false);
            ddef.setSkipToDateData(false);
            ddef.setSkipNodesAndLeaves(false);
            ddef.init();
        } else {
            logger.fine("Using pattern-based name approach");
            taskListWatcher = new TaskListDataWatcher(baseIter);
            ddef = new DefaultDataExportFilter(taskListWatcher);
            ddef.setIncludes(metricsIncludes);
            ddef.setExcludes(metricsExcludes);
            ddef.init();
        }
        DataExporter exp = new DataExporterXMLv1();
        exp.export(zipOut, ddef);
        baseIter.iterationFinished();
        zipOut.closeEntry();
        return taskListWatcher.getTaskListNames();
    }

    private void writeDefects(ZipOutputStream zipOut) throws IOException {
        zipOut.putNextEntry(new ZipEntry(DEFECT_FILE_NAME));
        DefectExporter exp = new DefectExporterXMLv1();
        exp.dumpDefects(ctx.getHierarchy(), filter, zipOut);
        zipOut.closeEntry();
    }

    private void writeTimeLogEntries(ZipOutputStream zipOut) throws IOException {
        zipOut.putNextEntry(new ZipEntry(TIME_FILE_NAME));
        TimeLogExporter exp = new TimeLogExporterXMLv1();
        exp.dumpTimeLogEntries(ctx.getTimeLog(), ctx.getData(), filter, zipOut);
        zipOut.closeEntry();
    }

    private void writeAditionalEntries(ZipOutputStream zipOut) throws IOException {
        if (additionalEntries != null) {
            additionalEntries = new ArrayList(additionalEntries);
            for (Iterator i = additionalEntries.iterator(); i.hasNext(); ) {
                ExportFileEntry file = (ExportFileEntry) i.next();
                if (writeAditionalEntry(zipOut, file) == false) i.remove();
            }
        }
    }

    private boolean writeAditionalEntry(ZipOutputStream zipOut, ExportFileEntry file) throws IOException {
        if (!StringUtils.hasValue(file.getHref())) {
            logger.severe("Missing href for additional export entry " + "when exporting file " + dest);
            return false;
        }
        if (!StringUtils.hasValue(file.getFilename())) {
            logger.severe("Missing filename for additional export entry " + "when exporting file " + dest);
            return false;
        }
        byte[] data;
        String uri = getAdditionalEntryUri(file);
        try {
            data = ctx.getWebServer().getRequest(uri, true);
        } catch (IOException ioe) {
            logger.severe("Encountered exception when exporting " + uri + " for export file " + dest);
            ioe.printStackTrace();
            return false;
        }
        zipOut.putNextEntry(new ZipEntry(file.getFilename()));
        zipOut.write(data);
        zipOut.closeEntry();
        return true;
    }

    private String getAdditionalEntryUri(ExportFileEntry file) {
        StringBuffer uri = new StringBuffer(file.getHref());
        if (!file.getHref().startsWith("/")) uri.insert(0, '/');
        if (filter.size() == 1) {
            String path = (String) filter.iterator().next();
            if (!path.startsWith("/")) path = "/" + path;
            if (!path.endsWith("/")) path = path + "/";
            String prefix = HTMLUtils.urlEncodePath(path);
            uri.insert(0, prefix);
        } else {
            for (Iterator i = filter.iterator(); i.hasNext(); ) {
                String path = (String) i.next();
                HTMLUtils.appendQuery(uri, "hierarchyPath", path);
            }
        }
        return uri.toString();
    }

    private void writeTaskLists(ZipOutputStream zipOut, Collection taskListNames) throws IOException {
        Map schedules = getEVSchedules(taskListNames);
        zipOut.putNextEntry(new ZipEntry(EV_FILE_NAME));
        EVExporter exp = new EVExporterXMLv1();
        exp.export(zipOut, schedules);
        zipOut.closeEntry();
    }

    private Map getEVSchedules(Collection taskListNames) {
        Map schedules = new TreeMap();
        for (Iterator iter = taskListNames.iterator(); iter.hasNext(); ) {
            boolean merged = false;
            String taskScheduleName = (String) iter.next();
            if (taskScheduleName.startsWith(MERGED_PREFIX)) {
                merged = true;
                taskScheduleName = taskScheduleName.substring(MERGED_PREFIX.length());
            }
            EVTaskList tl = EVTaskList.openExisting(taskScheduleName, ctx.getData(), ctx.getHierarchy(), ctx.getCache(), false);
            if (tl == null) continue;
            tl.setDependencyCalculator(new EVDependencyCalculator(ctx.getData(), ctx.getHierarchy(), ctx.getCache()));
            tl.recalc();
            if (merged) tl = new EVTaskListMerged(tl, false, false, null);
            schedules.put(taskScheduleName, tl);
        }
        return schedules;
    }

    private static final TopDownBottomUpJanitor EST_TIME_JANITOR = new TopDownBottomUpJanitor("Estimated Time");

    /**
     * Determine the date that an archive file was exported
     * @param archive a stream containing the contents of the archive
     * @return the date the file was exported. If the date cannot be determined
     *    for any reason (for example, if the parameter does not contain
     *    archived data), returns null.
     */
    public static Date getExportTime(InputStream archive) {
        ZipInputStream in = null;
        try {
            in = new ZipInputStream(new BufferedInputStream(archive));
            ZipEntry e;
            while ((e = in.getNextEntry()) != null) {
                if (MANIFEST_FILE_NAME.equals(e.getName())) {
                    Element xml = XMLUtils.parse(in).getDocumentElement();
                    Element exp = (Element) xml.getElementsByTagName(EXPORTED_TAG).item(0);
                    return XMLUtils.getXMLDate(exp, WHEN_ATTR);
                }
            }
        } catch (Exception e) {
        }
        if (in != null) {
            try {
                in.close();
            } catch (Exception e) {
            }
        }
        return null;
    }
}
