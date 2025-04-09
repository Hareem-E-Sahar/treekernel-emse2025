package spidr.webapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.*;
import spidr.datamodel.DailyData;
import spidr.datamodel.DataDescription;
import spidr.datamodel.DataSequence;
import spidr.datamodel.DataSequenceSet;
import spidr.datamodel.Station;
import spidr.dbaccess.LocalApi;
import spidr.dbload.UpdateMetadata;
import spidr.export.DSSExport;
import wdc.dbaccess.ConnectionPool;
import wdc.settings.Settings;
import wdc.utils.DateInterval;
import wdc.utils.Utilities;
import wdc.utils.WDCDay;
import wdc.utils.WDCTable;

public class DataServlet extends HttpServlet {

    private static final long serialVersionUID = 5149185978249423350L;

    private Logger log = Logger.getLogger("spidr.webapp.ExportAction");

    static final String SQL_LOAD_META_ELEMENTS = "SELECT CONCAT(elements_descr.element,\"@\",elements_descr.elemTable) AS elemKey, elements_descr.*, " + "min_yrMon*100+1 AS dateFrom, max_yrMon*100+31 AS dateTo " + "FROM elements_descr LEFT JOIN elements_periods ON " + "(elements_descr.elemTable=elements_periods.dataTable AND elements_descr.element=elements_periods.param) " + "ORDER BY elemTable, element";

    static final String SQL_LOAD_META_STATIONS = "SELECT stations_descr.*, " + "min_yrMon*100+1 AS dateFrom, max_yrMon*100+31 AS dateTo " + "FROM stations_descr LEFT JOIN stations_periods ON " + "(stations_descr.dataTable=stations_periods.dataTable AND stations_descr.stn=stations_periods.param) " + "ORDER BY dataTable, stName";

    static final String SQL_LOAD_PARAMS_AND_ELEMENTS = "SELECT CONCAT(element,\"@\",elemTable) AS elemKey, params_and_elements.* " + "FROM params_and_elements " + "ORDER BY viewGroup, elemTable, element";

    /**
	 * @param req
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
    public void doGetZip(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            int dayIdFrom = Integer.parseInt(req.getParameter("dateFrom"));
            int dayIdTo = Integer.parseInt(req.getParameter("dateTo"));
            WDCDay dateFrom = new WDCDay(dayIdFrom);
            WDCDay dateTo = new WDCDay(dayIdTo);
            DateInterval dateInterval = new DateInterval(dateFrom, dateTo);
            long curTime = (new java.util.Date()).getTime();
            res.addHeader("content-disposition", "attachment; filename=spidr_" + curTime + ".zip");
            String elements = req.getParameter("elem");
            String[] elemArray = elements.split(";");
            Connection con = null;
            Statement stmt = null;
            WDCTable metaElem = null;
            try {
                con = ConnectionPool.getConnection("metadata");
                stmt = con.createStatement();
                metaElem = new WDCTable(stmt, SQL_LOAD_META_ELEMENTS);
                stmt.close();
            } finally {
                try {
                    ConnectionPool.releaseConnection(con);
                } catch (Exception ignore) {
                }
            }
            LocalApi api = new LocalApi();
            DataSequenceSet dss = new DataSequenceSet("");
            for (int i = 0; i < elemArray.length; i++) {
                String[] s = elemArray[i].split(",");
                String elem = s[0];
                int sampling = Integer.parseInt(s[1]);
                if (sampling < 0) {
                    sampling = 0;
                }
                String table = s[2];
                Station stn = null;
                if (s.length == 4) {
                    stn = new Station(s[3], table, "");
                }
                int indElemElement = metaElem.getColumnIndex("element");
                int indElemTable = metaElem.getColumnIndex("elemTable");
                int indElemDescription = metaElem.getColumnIndex("description");
                int indElemMultiplier = metaElem.getColumnIndex("multiplier");
                int indElemMissingValue = metaElem.getColumnIndex("missingValue");
                int indElemUnits = metaElem.getColumnIndex("units");
                int elInd = metaElem.findRow(indElemTable, table, indElemElement, elem);
                String elemDescr = (String) metaElem.getValueAt(elInd, indElemDescription);
                String elemUnits = (String) metaElem.getValueAt(elInd, indElemUnits);
                float multiplier = Float.parseFloat((String) metaElem.getValueAt(elInd, indElemMultiplier));
                float missingValue = Float.parseFloat((String) metaElem.getValueAt(elInd, indElemMissingValue));
                DataDescription descr = new DataDescription(table, elem, "", elemDescr, elemUnits, elem);
                descr.setMultiplier(multiplier);
                descr.setMissingValue(missingValue);
                Vector v = api.getData(descr, stn, dateInterval, sampling);
                if (v != null && v.size() != 0) {
                    DataSequence ds = (DataSequence) v.get(0);
                    dss.add(ds);
                }
            }
            for (int i = 0; i < dss.size(); i++) {
                String group = "";
                DailyData dd = null;
                try {
                    dd = (DailyData) ((DataSequence) dss.elementAt(i)).elementAt(0);
                    DataDescription ddescr = dd.getDescription();
                    String table = ddescr.getTable();
                    String[] groupList = Utilities.splitString(Settings.get("viewGroups.groupOrder"));
                    boolean flag = false;
                    for (int k = 0; (k < groupList.length) && !flag; k++) {
                        String[] tables = UpdateMetadata.getTablesForGroup(groupList[k]);
                        for (int j = 0; (j < tables.length) && !flag; j++) {
                            if (tables[j].equals(table)) {
                                flag = true;
                                group = groupList[k];
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Unexpected error: " + e.toString());
                }
                ZipOutputStream zos = new ZipOutputStream(res.getOutputStream());
                try {
                    String spidrServerName = Settings.get("sites.localSite");
                    String spidrServerUrl = Settings.get("sites." + spidrServerName + ".url");
                    String metadataCollection = Settings.get("viewGroups." + group + ".metadataCollection");
                    String fileUrl = spidrServerUrl + (spidrServerUrl.endsWith("/") ? "" : "/") + "osproxy.do?specialRequest=document&docId=" + metadataCollection + dd.getStation().getStn().split("_")[0];
                    BufferedReader in = new BufferedReader(new InputStreamReader((new URL(fileUrl)).openStream()));
                    zos.putNextEntry(new ZipEntry(dd.getStation().getStn() + ".xml"));
                    int buf;
                    while ((buf = in.read()) != -1) {
                        zos.write(buf);
                    }
                    in.close();
                } catch (Exception e) {
                    log.error("Couldn't add metadata to ZIP file: " + e.toString());
                }
                String entryFileName = "spidr_" + curTime + "_" + i + ".txt";
                zos.putNextEntry(new ZipEntry(entryFileName));
                PrintWriter plt = new PrintWriter(zos);
                if (log.isDebugEnabled()) {
                    log.debug("asciiExportDataSet() files are ready");
                }
                String dateFormat = "yyyy-MM-dd HH:mm";
                TimeZone tz = new SimpleTimeZone(0, "GMT");
                Calendar utc = new GregorianCalendar(tz);
                utc.setTime(new java.util.Date());
                SimpleDateFormat df = new SimpleDateFormat(dateFormat, Locale.US);
                df.setTimeZone(tz);
                df.setCalendar(utc);
                plt.println("#Spidr data output file in ASCII format created at " + df.format(utc.getTime()));
                if (log.isDebugEnabled()) {
                    log.debug("asciiExportDataSet() datestamp is ready");
                }
                plt.println("#GMT time is used");
                plt.println("#");
                plt.println("#");
                plt.println("#--------------------------------------------------");
                for (int j = 0; j < dss.size(); j++) {
                    DSSExport.vec2stream(plt, (DataSequence) dss.elementAt(j));
                }
                plt.close();
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
	 * 
	 * @param req
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
    public void doGetRaw(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            res.setContentType("text/plain");
            PrintWriter out = res.getWriter();
            res.setHeader("Content-Language", "en");
            int dayIdFrom = Integer.parseInt(req.getParameter("dateFrom"));
            int dayIdTo = Integer.parseInt(req.getParameter("dateTo"));
            WDCDay dateFrom = new WDCDay(dayIdFrom);
            WDCDay dateTo = new WDCDay(dayIdTo);
            DateInterval dateInterval = new DateInterval(dateFrom, dateTo);
            String elements = req.getParameter("elem");
            String[] elemArray = elements.split(";");
            Connection con = null;
            Statement stmt = null;
            WDCTable metaElem = null;
            try {
                con = ConnectionPool.getConnection("metadata");
                stmt = con.createStatement();
                metaElem = new WDCTable(stmt, SQL_LOAD_META_ELEMENTS);
                stmt.close();
            } finally {
                try {
                    ConnectionPool.releaseConnection(con);
                } catch (Exception ignore) {
                }
            }
            LocalApi api = new LocalApi();
            DataSequenceSet dss = new DataSequenceSet("");
            for (int i = 0; i < elemArray.length; i++) {
                String[] s = elemArray[i].split(",");
                String elem = s[0];
                int sampling = Integer.parseInt(s[1]);
                if (sampling < 0) {
                    sampling = 0;
                }
                String table = s[2];
                Station stn = null;
                if (s.length == 4) {
                    stn = new Station(s[3], table, "");
                }
                int indElemElement = metaElem.getColumnIndex("element");
                int indElemTable = metaElem.getColumnIndex("elemTable");
                int indElemDescription = metaElem.getColumnIndex("description");
                int indElemMultiplier = metaElem.getColumnIndex("multiplier");
                int indElemMissingValue = metaElem.getColumnIndex("missingValue");
                int indElemUnits = metaElem.getColumnIndex("units");
                int elInd = metaElem.findRow(indElemTable, table, indElemElement, elem);
                String elemDescr = (String) metaElem.getValueAt(elInd, indElemDescription);
                String elemUnits = (String) metaElem.getValueAt(elInd, indElemUnits);
                float multiplier = Float.parseFloat((String) metaElem.getValueAt(elInd, indElemMultiplier));
                float missingValue = Float.parseFloat((String) metaElem.getValueAt(elInd, indElemMissingValue));
                DataDescription descr = new DataDescription(table, elem, "", elemDescr, elemUnits, elem);
                descr.setMultiplier(multiplier);
                descr.setMissingValue(missingValue);
                Vector v = api.getData(descr, stn, dateInterval, sampling);
                if (v != null && v.size() != 0) {
                    DataSequence ds = (DataSequence) v.get(0);
                    dss.add(ds);
                }
            }
            for (int i = 0; i < dss.size(); i++) {
                String group = "";
                DailyData dd = null;
                try {
                    dd = (DailyData) ((DataSequence) dss.elementAt(i)).elementAt(0);
                    DataDescription ddescr = dd.getDescription();
                    String table = ddescr.getTable();
                    String[] groupList = Utilities.splitString(Settings.get("viewGroups.groupOrder"));
                    boolean flag = false;
                    for (int k = 0; (k < groupList.length) && !flag; k++) {
                        String[] tables = UpdateMetadata.getTablesForGroup(groupList[k]);
                        for (int j = 0; (j < tables.length) && !flag; j++) {
                            if (tables[j].equals(table)) {
                                flag = true;
                                group = groupList[k];
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Unexpected error: " + e.toString());
                }
                String dateFormat = "yyyy-MM-dd HH:mm";
                TimeZone tz = new SimpleTimeZone(0, "GMT");
                Calendar utc = new GregorianCalendar(tz);
                utc.setTime(new java.util.Date());
                SimpleDateFormat df = new SimpleDateFormat(dateFormat, Locale.US);
                df.setTimeZone(tz);
                df.setCalendar(utc);
                out.println("#Spidr data output file in ASCII format created at " + df.format(utc.getTime()));
                out.println("#GMT time is used");
                out.println("#");
                out.println("#");
                out.println("#--------------------------------------------------");
                for (int j = 0; j < dss.size(); j++) {
                    DSSExport.vec2stream(out, (DataSequence) dss.elementAt(j));
                }
                out.close();
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
	 * 
	 * @param req
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
    public void doGetCsv(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            res.setContentType("text/plain");
            PrintWriter out = res.getWriter();
            res.setHeader("Content-Language", "en");
            int dayIdFrom = Integer.parseInt(req.getParameter("dateFrom"));
            int dayIdTo = Integer.parseInt(req.getParameter("dateTo"));
            WDCDay dateFrom = new WDCDay(dayIdFrom);
            WDCDay dateTo = new WDCDay(dayIdTo);
            DateInterval dateInterval = new DateInterval(dateFrom, dateTo);
            String elements = req.getParameter("elem");
            String[] elemArray = elements.split(";");
            Connection con = null;
            Statement stmt = null;
            WDCTable metaElem = null;
            try {
                con = ConnectionPool.getConnection("metadata");
                stmt = con.createStatement();
                metaElem = new WDCTable(stmt, SQL_LOAD_META_ELEMENTS);
                stmt.close();
            } finally {
                try {
                    ConnectionPool.releaseConnection(con);
                } catch (Exception ignore) {
                }
            }
            LocalApi api = new LocalApi();
            DataSequenceSet dss = new DataSequenceSet("");
            for (int i = 0; i < elemArray.length; i++) {
                String[] s = elemArray[i].split(",");
                String elem = s[0];
                int sampling = Integer.parseInt(s[1]);
                if (sampling < 0) {
                    sampling = 0;
                }
                String table = s[2];
                Station stn = null;
                if (s.length == 4) {
                    stn = new Station(s[3], table, "");
                }
                int indElemElement = metaElem.getColumnIndex("element");
                int indElemTable = metaElem.getColumnIndex("elemTable");
                int indElemDescription = metaElem.getColumnIndex("description");
                int indElemMultiplier = metaElem.getColumnIndex("multiplier");
                int indElemMissingValue = metaElem.getColumnIndex("missingValue");
                int indElemUnits = metaElem.getColumnIndex("units");
                int elInd = metaElem.findRow(indElemTable, table, indElemElement, elem);
                String elemDescr = (String) metaElem.getValueAt(elInd, indElemDescription);
                String elemUnits = (String) metaElem.getValueAt(elInd, indElemUnits);
                float multiplier = Float.parseFloat((String) metaElem.getValueAt(elInd, indElemMultiplier));
                float missingValue = Float.parseFloat((String) metaElem.getValueAt(elInd, indElemMissingValue));
                DataDescription descr = new DataDescription(table, elem, "", elemDescr, elemUnits, elem);
                descr.setMultiplier(multiplier);
                descr.setMissingValue(missingValue);
                Vector v = api.getData(descr, stn, dateInterval, sampling);
                if (v != null && v.size() != 0) {
                    DataSequence ds = (DataSequence) v.get(0);
                    dss.add(ds);
                }
            }
            for (int i = 0; i < dss.size(); i++) {
                String group = "";
                DailyData dd = null;
                try {
                    dd = (DailyData) ((DataSequence) dss.elementAt(i)).elementAt(0);
                    DataDescription ddescr = dd.getDescription();
                    String table = ddescr.getTable();
                    String[] groupList = Utilities.splitString(Settings.get("viewGroups.groupOrder"));
                    boolean flag = false;
                    for (int k = 0; (k < groupList.length) && !flag; k++) {
                        String[] tables = UpdateMetadata.getTablesForGroup(groupList[k]);
                        for (int j = 0; (j < tables.length) && !flag; j++) {
                            if (tables[j].equals(table)) {
                                flag = true;
                                group = groupList[k];
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Unexpected error: " + e.toString());
                }
                String dateFormat = "yyyy-MM-dd HH:mm";
                TimeZone tz = new SimpleTimeZone(0, "GMT");
                Calendar utc = new GregorianCalendar(tz);
                utc.setTime(new java.util.Date());
                SimpleDateFormat df = new SimpleDateFormat(dateFormat, Locale.US);
                df.setTimeZone(tz);
                df.setCalendar(utc);
                out.println("#Spidr data output file in CSV format created at " + df.format(utc.getTime()));
                out.println("#GMT time is used");
                out.println("#");
                out.println("#");
                out.println("#--------------------------------------------------");
                for (int j = 0; j < dss.size(); j++) {
                    DSSExport.vec2csv(out, (DataSequence) dss.elementAt(j));
                }
                out.close();
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
	 * 
	 */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String format = req.getParameter("format");
        if (format != null && format.equalsIgnoreCase("ascii")) {
            doGetRaw(req, res);
        } else if (format != null && format.equalsIgnoreCase("csv")) {
            doGetCsv(req, res);
        } else {
            doGetZip(req, res);
        }
    }
}
