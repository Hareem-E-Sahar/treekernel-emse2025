package org.vizzini.example.videocatalog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.xml.transform.dom.DOMResult;
import org.vizzini.database.IDatabase;
import org.vizzini.util.FileUtilities;
import org.vizzini.util.GenericData;
import org.vizzini.util.xml.TransformUtilities;
import org.vizzini.util.xml.XMLUtilities;
import org.w3c.dom.Node;

/**
 * Provides an exporter to create a set of HTML pages for the contents the Video
 * Catalog database.
 *
 * @author   Jeffrey M. Thompson
 * @version  v0.4
 * @since    v0.4
 */
public class VideoExporter {

    /** Database select statement. */
    protected static final String SELECT = "select " + "v.TITLE, v.YEAR, v.RATING, v.GENRE, m.MEDIA, m.MEDIA_ID, v.LINK_ID, v.COMMENT" + " from VIDEO_TABLE v, MEDIA_TABLE m, VIDEO_TABLE_MEDIA_TABLE vm" + " where vm.ITEM1 = v.ID AND vm.ITEM2 = m.ID";

    /** Logger. */
    private static final Logger LOGGER = Logger.getLogger(VideoExporter.class.getName());

    /** Column names by which to sort. */
    private static final String[] SORT_COLUMNS = { "TITLE", "YEAR", "RATING", "GENRE", "MEDIA", "MEDIA_ID", "LINK_ID", "COMMENT" };

    /** HTML file header. */
    public static final String HTML_HEADER = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">";

    /** XSL transform filename. */
    private static final String XSLT_FILENAME = "videos.xsl";

    /** CSS style sheet filename. */
    private static final String STYLE_FILENAME = "videoStyle.css";

    /** Flag indicating whether to log profiling messages. */
    private static final boolean IS_PROFILING = false;

    /** XSL transform string. */
    private static final String XSLT;

    /** File utilities. */
    private static final FileUtilities FILE_UTILITIES = new FileUtilities();

    static {
        String xslt = null;
        try {
            xslt = FILE_UTILITIES.readFile(VideoExporter.class.getResourceAsStream(XSLT_FILENAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
        XSLT = xslt;
    }

    /**
     * @return  the maximum progress value.
     *
     * @since   v0.4
     */
    public static int getMaxProgress() {
        return 2 + (SORT_COLUMNS.length * 3);
    }

    /**
     * @return  the minimum progress value.
     *
     * @since   v0.4
     */
    public static int getMinProgress() {
        return -1;
    }

    /**
     * Export all records from the given database.
     *
     * @param   directory        Target directory for HTML files (required).
     * @param   database         Source database (required).
     * @param   progressMonitor  Progress monitor (optional).
     *
     * @throws  IOException   if there is an I/O problem.
     * @throws  SQLException  if there is a database problem.
     *
     * @since   v0.4
     */
    public void exportXml(File directory, IDatabase database, ProgressMonitor progressMonitor) throws IOException, SQLException {
        if (directory == null) {
            throw new IllegalArgumentException("directory == null");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("directory input is not a directory");
        }
        if (database == null) {
            throw new IllegalArgumentException("database == null");
        }
        long start0 = System.currentTimeMillis();
        copyStyleSheet(directory);
        if (progressMonitor != null) {
            if (progressMonitor.isCanceled()) {
                return;
            }
            notifyProgress(progressMonitor, "Copied style sheet", 0);
        }
        if (IS_PROFILING) {
            logElapsedTime("Copy style sheet file", start0);
        }
        long start1 = System.currentTimeMillis();
        List<GenericData> dataList = database.executeQuery(SELECT);
        if (LOGGER.isLoggable(Level.FINEST)) {
            for (int i = 0; i < dataList.size(); i++) {
                GenericData data = dataList.get(i);
                LOGGER.log(Level.FINEST, i + " " + data.toString());
            }
        }
        if (progressMonitor != null) {
            if (progressMonitor.isCanceled()) {
                return;
            }
            notifyProgress(progressMonitor, "Performed database select", 1);
        }
        if (IS_PROFILING) {
            logElapsedTime("Database select", start1);
        }
        int offset = 1;
        XMLUtilities xmlUtils = new XMLUtilities();
        TransformUtilities transformUtils = new TransformUtilities();
        for (int i = 0; i < SORT_COLUMNS.length; i++) {
            String columnName = SORT_COLUMNS[i];
            long start2 = System.currentTimeMillis();
            if (progressMonitor != null) {
                if (progressMonitor.isCanceled()) {
                    return;
                }
                notifyProgress(progressMonitor, columnName + ": Generating XML", offset + (3 * i));
            }
            String xml = generateXml(dataList, columnName);
            LOGGER.finest("xml = " + xml);
            if (IS_PROFILING) {
                logElapsedTime(columnName + ": Generate XML", start2);
            }
            long start3 = System.currentTimeMillis();
            if (progressMonitor != null) {
                if (progressMonitor.isCanceled()) {
                    return;
                }
                notifyProgress(progressMonitor, columnName + ": Transforming XML to HTML", offset + (3 * i) + 1);
            }
            DOMResult domResult = transformUtils.transform(xml, XSLT);
            Node node = domResult.getNode();
            String html = xmlUtils.convertToString(node, true, false);
            if (html.startsWith(XMLUtilities.XML_HEADER)) {
                html = html.substring(XMLUtilities.XML_HEADER.length());
                html = HTML_HEADER + html;
            }
            if (IS_PROFILING) {
                logElapsedTime(columnName + ": Transform XML to HTML", start3);
            }
            long start4 = System.currentTimeMillis();
            StringBuffer filename = new StringBuffer();
            filename.append("videos_").append(columnName).append(".html");
            if (progressMonitor != null) {
                if (progressMonitor.isCanceled()) {
                    return;
                }
                notifyProgress(progressMonitor, columnName + ": Writing file " + filename.toString(), offset + (3 * i) + 2);
            }
            File toFile = new File(directory, filename.toString());
            FILE_UTILITIES.writeFile(toFile, html);
            if (IS_PROFILING) {
                logElapsedTime(columnName + ": Write HTML to file", start4);
            }
        }
        if (progressMonitor != null) {
            notifyProgress(progressMonitor, "Done", getMaxProgress());
        }
        if (IS_PROFILING) {
            logElapsedTime("Export XML", start0);
        }
    }

    /**
     * Write a single property from the given record to the given writer.
     *
     * @param  sb            String buffer.
     * @param  record        Record.
     * @param  propertyName  Property name.
     *
     * @since  v0.4
     */
    protected void appendProperty(StringBuffer sb, GenericData record, String propertyName) {
        Object value = record.get(propertyName);
        Class<?> type = null;
        String valueStr = null;
        if (value != null) {
            type = value.getClass();
            XMLUtilities xmlUtils = new XMLUtilities();
            valueStr = xmlUtils.convertSpecialContent(value.toString());
        }
        sb.append("<property name='");
        sb.append(propertyName);
        sb.append("' type='");
        sb.append((type == null) ? "null" : type.getName());
        sb.append("'>");
        sb.append((valueStr == null) ? "null" : valueStr);
        sb.append("</property>");
    }

    /**
     * Export the given records, and their related records, from the given
     * database.
     *
     * @param  sb      String buffer.
     * @param  record  Record.
     *
     * @since  v0.4
     */
    protected void appendRecord(StringBuffer sb, GenericData record) {
        sb.append("<record>");
        Iterator<String> iter = record.getPropertyNames();
        while (iter.hasNext()) {
            String propertyName = iter.next();
            appendProperty(sb, record, propertyName);
        }
        sb.append("</record>");
    }

    /**
     * Copy the style sheet to the given directory.
     *
     * @param   directory  Target directory.
     *
     * @throws  IOException  if there is an I/O problem.
     *
     * @since   v0.4
     */
    protected void copyStyleSheet(File directory) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(STYLE_FILENAME);
        String styleSheet = FILE_UTILITIES.readFile(inputStream);
        File toFile = new File(directory, STYLE_FILENAME);
        FILE_UTILITIES.writeFile(toFile, styleSheet);
    }

    /**
     * @param   dataList           Data list.
     * @param   orderByColumnName  Order by column name.
     *
     * @return  the data list as XML.
     *
     * @since   v0.4
     */
    protected String generateXml(List<GenericData> dataList, String orderByColumnName) {
        if (dataList == null) {
            throw new IllegalArgumentException("dataList == null");
        }
        if (orderByColumnName == null) {
            throw new IllegalArgumentException("orderByColumnName == null");
        }
        if (orderByColumnName.length() == 0) {
            throw new IllegalArgumentException("orderByColumnName.length() == 0");
        }
        long start0 = System.currentTimeMillis();
        Comparator<GenericData> comparator = new MyComparator(orderByColumnName);
        Collections.sort(dataList, comparator);
        if (IS_PROFILING) {
            logElapsedTime(orderByColumnName + ": Data list sorted", start0);
        }
        long start1 = System.currentTimeMillis();
        XMLUtilities xmlUtils = new XMLUtilities();
        StringBuffer sb = new StringBuffer();
        sb.append(XMLUtilities.XML_HEADER);
        sb.append("<root>");
        sb.append(xmlUtils.format("orderByColumnName", orderByColumnName));
        int size0 = dataList.size();
        for (int i = 0; i < size0; i++) {
            GenericData data = dataList.get(i);
            LOGGER.log(Level.FINEST, data.toString());
            appendRecord(sb, data);
        }
        String dateStr = getDateNowFormatted();
        sb.append(xmlUtils.format("created", dateStr));
        sb.append("</root>");
        if (IS_PROFILING) {
            logElapsedTime(orderByColumnName + ": Fill string buffer", start1);
            logElapsedTime(orderByColumnName + ": Total generate XML", start0);
        }
        return sb.toString();
    }

    /**
     * @return  today's date in the proper format.
     *
     * @since   v0.4
     */
    protected String getDateNowFormatted() {
        SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateInstance();
        formatter.applyPattern("yyyy.MM.dd");
        Date date = new Date();
        String dateStr = formatter.format(date);
        return dateStr;
    }

    /**
     * Log the elapsed time.
     *
     * @param  message  Message.
     * @param  start    Start time.
     *
     * @since  v0.4
     */
    protected void logElapsedTime(String message, long start) {
        long end = System.currentTimeMillis();
        long elapsed = end - start;
        LOGGER.log(Level.FINE, message + " elapsed: " + elapsed);
    }

    /**
     * Notify the Swing thread of a progress change.
     *
     * @param  progressMonitor  Progress monitor.
     * @param  note             Note.
     * @param  progress         Progress.
     *
     * @since  v0.4
     */
    protected void notifyProgress(ProgressMonitor progressMonitor, String note, int progress) {
        Runnable runner = new MyRunner(progressMonitor, note, progress);
        try {
            SwingUtilities.invokeAndWait(runner);
        } catch (InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    /**
     * Provides a comparator for generic data records and a given property name.
     *
     * @author   Jeffrey M. Thompson
     * @version  v0.4
     * @since    v0.4
     */
    static class MyComparator implements Comparator<GenericData> {

        /** Sort column. */
        private String _sortColumn;

        /**
         * Construct this object with the given parameter.
         *
         * @param  sortColumn  Sort column.
         */
        public MyComparator(String sortColumn) {
            _sortColumn = sortColumn;
        }

        /**
         * @see  java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(GenericData object0, GenericData object1) {
            int answer = -1;
            GenericData data0 = object0;
            GenericData data1 = object1;
            Object value0 = data0.get(_sortColumn);
            Object value1 = data1.get(_sortColumn);
            if ((value0 == null) && (value1 == null)) {
                answer = 0;
            } else if ((value0 != null) && (value1 != null) && (value0 instanceof Comparable<?>)) {
                @SuppressWarnings("unchecked") Comparable<Object> comparable = (Comparable<Object>) value0;
                answer = comparable.compareTo(value1);
            } else if ((value0 == null) && (value1 != null)) {
                answer = 1;
            } else if ((value0 != null) && (value1 == null)) {
                answer = -1;
            }
            return answer;
        }
    }

    /**
     * Provides a runnable to update the progress monitor.
     *
     * @author   Jeffrey M. Thompson
     * @version  v0.4
     * @since    v0.4
     */
    static class MyRunner implements Runnable {

        /** Note. */
        private String _note;

        /** Progress. */
        private int _progress;

        /** Progress monitor. */
        private ProgressMonitor _progressMonitor;

        /**
         * Construct this object with the given parameters.
         *
         * @param  progressMonitor  Progress monitor.
         * @param  note             Note.
         * @param  progress         Progress.
         */
        public MyRunner(ProgressMonitor progressMonitor, String note, int progress) {
            if (progressMonitor == null) {
                throw new IllegalArgumentException("progressMonitor == null");
            }
            _progressMonitor = progressMonitor;
            _note = note;
            _progress = progress;
        }

        /**
         * @see  java.lang.Runnable#run()
         */
        public void run() {
            _progressMonitor.setNote(_note);
            _progressMonitor.setProgress(_progress);
        }
    }
}
