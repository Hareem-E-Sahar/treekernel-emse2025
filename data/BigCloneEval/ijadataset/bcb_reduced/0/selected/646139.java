package spidr.webapp;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import spidr.applets.PlotDSS;
import spidr.applets.ptolemy.plot.Plot;
import spidr.datamodel.DailyData;
import spidr.datamodel.DataDescription;
import spidr.datamodel.DataSequence;
import spidr.datamodel.DataSequenceSet;
import spidr.datamodel.Station;
import spidr.dbaccess.DBAccess;
import spidr.dbaccess.LocalApi;
import spidr.dbload.UpdateMetadata;
import spidr.export.DSSExport;
import wdc.dbaccess.ApiException;
import wdc.dbaccess.ConnectionPool;
import wdc.settings.Settings;
import wdc.utils.DateInterval;
import wdc.utils.Utilities;
import wdc.utils.WDCDay;
import wdc.utils.WDCTable;

public class GetData extends HttpServlet {

    private static final long serialVersionUID = 485607464214305514L;

    static final int BUF_SIZE = 1024;

    static final String SQL_LOAD_META_ELEMENTS = "SELECT CONCAT(elements_descr.element,\"@\",elements_descr.elemTable) AS elemKey, elements_descr.*, " + "min_yrMon*100+1 AS dateFrom, max_yrMon*100+31 AS dateTo " + "FROM elements_descr LEFT JOIN elements_periods ON " + "(elements_descr.elemTable=elements_periods.dataTable AND elements_descr.element=elements_periods.param) " + "ORDER BY elemTable, element";

    static final String SQL_LOAD_META_STATIONS = "SELECT stations_descr.*, " + "min_yrMon*100+1 AS dateFrom, max_yrMon*100+31 AS dateTo " + "FROM stations_descr LEFT JOIN stations_periods ON " + "(stations_descr.dataTable=stations_periods.dataTable AND stations_descr.stn=stations_periods.param) " + "ORDER BY dataTable, stName";

    static final String SQL_LOAD_PARAMS_AND_ELEMENTS = "SELECT CONCAT(element,\"@\",elemTable) AS elemKey, params_and_elements.* " + "FROM params_and_elements " + "ORDER BY viewGroup, elemTable, element";

    private Logger log = Logger.getLogger("spidr.webapp.GetData");

    /**
	 * 
	 */
    public Vector<String> getParameter(String param) throws ServletException {
        String elem = null;
        String table = null;
        String station = null;
        Vector<String> result = new Vector<String>();
        if (param != null) {
            Connection con = null;
            try {
                String[] paramParts = param.split("\\.");
                con = ConnectionPool.getConnection("metadata");
                String paramName = null;
                String platform = null;
                String section = null;
                paramName = paramParts[0];
                if (paramParts.length >= 2) {
                    platform = paramParts[1];
                    if ("".equals(platform)) platform = null;
                }
                if (paramParts.length >= 3) {
                    section = paramParts[2];
                }
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT elem,`table`,station FROM translate WHERE param='" + paramName + "' AND (platform " + ((platform == null) ? "IS NULL" : "='" + platform + "'") + " OR platform ='*') AND section " + ((section == null) ? "IS NULL" : "='" + section + "'"));
                if (rs.next()) {
                    elem = rs.getString(1);
                    table = rs.getString(2);
                    station = rs.getString(3);
                    if ("*".equals(station)) station = platform;
                }
                result.add(elem);
                result.add(table);
                result.add(station);
                return result;
            } catch (Exception e) {
                log.error("ERROR! " + e.toString());
                throw new ServletException(e);
            } finally {
                ConnectionPool.releaseConnection(con);
            }
        } else return null;
    }

    /**
	 * @param req
	 * @return
	 * @throws ServletException
	 * @throws UnsupportedEncodingException
	 */
    protected static HashMap<String, String> parseQueryString(HttpServletRequest req) throws ServletException, UnsupportedEncodingException {
        HashMap<String, String> args = new HashMap<String, String>();
        String encoding = req.getCharacterEncoding() == null ? "UTF-8" : req.getCharacterEncoding();
        String queryString = req.getQueryString() == null ? "" : req.getQueryString();
        queryString = URLDecoder.decode(queryString, encoding);
        String[] qs = queryString.split("&");
        for (int i = 0; i < qs.length; i++) {
            if (qs[i] == null || qs[i].equals("")) {
                continue;
            }
            String[] kv = qs[i].split("=");
            if (kv == null) {
                throw new ServletException("Invalid query string");
            }
            if (kv.length == 1) {
                args.put(kv[0], "");
            } else if (kv.length == 2) {
                args.put(kv[0], kv[1]);
            }
        }
        return args;
    }

    /**
	 * 
	 */
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HashMap<String, String> args = parseQueryString(req);
        if (args.size() == 0 || args.containsKey("describe")) {
            doGetMapping(args, req, res);
            return;
        }
        long maxSizeDefault = 500000;
        long maxSize = 0;
        String s = Settings.get("metadata.maxDataSize");
        if (s != null) {
            maxSize = Long.parseLong(Settings.get("metadata.maxDataSize"));
        } else {
            maxSize = maxSizeDefault;
        }
        if (getSize(args) > maxSize) {
            res.sendError(506);
            return;
        }
        boolean compress = false;
        String compressStr = args.get("compress");
        if (compressStr != null) {
            compress = Boolean.parseBoolean(compressStr);
        }
        String format = args.get("format");
        if (format != null && format.equalsIgnoreCase("ascii")) {
            doGetRaw(args, res);
        } else if (format != null && format.equalsIgnoreCase("csv")) {
            doGetCsv(args, res, compress);
        } else if (format != null && format.equalsIgnoreCase("image")) {
            doGetImage(args, res);
        } else if (format != null && format.equalsIgnoreCase("zip")) {
            doGetZip(args, res);
        } else if (format != null && format.equalsIgnoreCase("json")) {
            doGetJSON(args, res, compress);
        } else if (format != null) {
            doGetRemote(args, res);
        } else {
            doGetZip(args, res);
        }
    }

    /**
	 * @param args
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
    @SuppressWarnings("unchecked")
    public void doGetRaw(HashMap<String, String> args, HttpServletResponse res) throws ServletException, IOException {
        try {
            res.setContentType("text/plain");
            PrintWriter out = res.getWriter();
            res.setHeader("Content-Language", "en");
            int dayIdFrom = Integer.parseInt(args.get("dateFrom"));
            int dayIdTo = Integer.parseInt(args.get("dateTo"));
            WDCDay dateFrom = new WDCDay(dayIdFrom);
            WDCDay dateTo = new WDCDay(dayIdTo);
            DateInterval dateInterval = new DateInterval(dateFrom, dateTo);
            String dataTimeFormat = args.get("timeFormat");
            int sampling = 0;
            String s = args.get("sampling");
            if (s != null) {
                sampling = Integer.parseInt(s);
            }
            String parameters = args.get("param");
            String[] paramArray = parameters.split(";");
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
            for (int i = 0; i < paramArray.length; i++) {
                Vector<?> p = getParameter(paramArray[i]);
                String elem = (String) p.get(0);
                String table = (String) p.get(1);
                String station = (String) p.get(2);
                Station stn = null;
                if (station != null) {
                    stn = new Station(station, table, "");
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
                Vector<?> v = api.getData(descr, stn, dateInterval, sampling);
                if (v != null && v.size() != 0) {
                    DataSequence ds = (DataSequence) v.get(0);
                    dss.add(ds);
                }
            }
            for (int i = 0; i < dss.size(); i++) {
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
                out.println("#param: " + parameters);
                out.println("#meta: http://spidr.ngdc.noaa.gov/spidr/GetMetadata?describe&param=" + parameters);
                out.println("#");
                out.println("#");
                out.println("#--------------------------------------------------");
                for (int j = 0; j < dss.size(); j++) {
                    DSSExport.vec2stream(out, (DataSequence) dss.elementAt(j), dataTimeFormat);
                }
            }
            out.close();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
	 * @param req
	 * @param res
	 * @param param
	 * @throws ApiException
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
    private void getTierTwoMapping(HttpServletRequest req, HttpServletResponse res, String param) throws ApiException, SQLException, ServletException, IOException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String mapping = new GetDataDescribeTierTwo(this).describe(req, res, param);
        if (mapping == null) {
            return;
        }
        req.setAttribute("mapping", mapping);
        req.setAttribute("param", param);
        getServletContext().getRequestDispatcher("/GetDataDescribeTierTwo.jsp").forward(req, res);
    }

    /**
	 * @param req
	 * @param res
	 * @param param
	 * @throws ApiException
	 * @throws SQLException
	 * @throws ServletException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws IOException
	 */
    private void getTierThreeMapping(HttpServletRequest req, HttpServletResponse res, String param) throws ApiException, SQLException, ServletException, SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
        String range = new GetDataDescribeTierThree().describe(req, res, param);
        req.setAttribute("param", param);
        req.setAttribute("range", range);
        getServletContext().getRequestDispatcher("/GetDataDescribeTierThree.jsp").forward(req, res);
    }

    /**
	 * Given a param string, determines if it is tier two
	 * 
	 * @param param
	 * @return true if tier two, false otherwise
	 */
    private boolean isTierTwo(String param) {
        if (param == null) {
            return false;
        }
        if (param.indexOf('.') < 0) {
            return true;
        }
        return false;
    }

    /**
	 * Given a param string, determines if it is tier three
	 * 
	 * @param param
	 * @return true if tier three, false otherwise
	 */
    private boolean isTierThree(String param) {
        if (param == null) {
            return false;
        }
        if (param.indexOf('.') >= 0) {
            return true;
        }
        return false;
    }

    /**
	 * Displays the initial describe page
	 * 
	 * @param req
	 * @param res
	 * @throws ApiException
	 * @throws SQLException
	 * @throws ServletException
	 * @throws IOException
	 */
    private void getTierOneMapping(HttpServletRequest req, HttpServletResponse res) throws ApiException, SQLException, ServletException, IOException {
        String mapping = new GetDataDescribe().getParameters(req);
        req.setAttribute("mapping", mapping);
        req.setAttribute("url", req.getRequestURL().toString());
        getServletContext().getRequestDispatcher("/GetDataDescribeTierOne.jsp").forward(req, res);
    }

    /**
	 * This method is responsible for outputting a legend/capabilities document. It's intended for client developers
	 * and users, so they may see what their options are, and ask subsequent questions about the web service.
	 * 
	 * @param req
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 * @throws SQLException 
	 * @throws ApiException 
	 */
    public void doGetMapping(HashMap<String, String> args, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        try {
            res.setContentType("text/html");
            res.setHeader("Content-Language", "en");
            String param = args.get("param");
            if (param == null) {
                getTierOneMapping(req, res);
            } else if (isTierTwo(param)) {
                getTierTwoMapping(req, res, param);
            } else if (isTierThree(param)) {
                getTierThreeMapping(req, res, param);
            } else {
                getTierOneMapping(req, res);
            }
        } catch (Exception x) {
            throw new ServletException(x);
        }
    }

    /**
	 * @param args
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
    @SuppressWarnings("unchecked")
    public void doGetCsv(HashMap<String, String> args, HttpServletResponse res, boolean compress) throws ServletException, IOException {
        try {
            if (compress) {
                res.setHeader("Content-Encoding", "gzip");
            }
            res.setContentType("text/plain");
            PrintWriter pw = null;
            GZIPOutputStream zip = null;
            if (compress) {
                zip = new GZIPOutputStream(res.getOutputStream());
                pw = new PrintWriter(zip);
            } else {
                pw = res.getWriter();
            }
            res.setHeader("Content-Language", "en");
            int dayIdFrom = Integer.parseInt(args.get("dateFrom"));
            int dayIdTo = Integer.parseInt(args.get("dateTo"));
            WDCDay dateFrom = new WDCDay(dayIdFrom);
            WDCDay dateTo = new WDCDay(dayIdTo);
            DateInterval dateInterval = new DateInterval(dateFrom, dateTo);
            String dataTimeFormat = args.get("timeFormat");
            boolean header = true;
            if (args.containsKey("header")) {
                header = Boolean.parseBoolean(args.get("header"));
            }
            boolean fillMissing = true;
            if (args.containsKey("fillmissing")) {
                fillMissing = Boolean.parseBoolean(args.get("fillmissing"));
            }
            int sampling = 0;
            String s = args.get("sampling");
            if (s != null) {
                sampling = Integer.parseInt(s);
            }
            String parameters = args.get("param");
            String[] paramArray = parameters.split(";");
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
            for (int i = 0; i < paramArray.length; i++) {
                Vector<?> p = getParameter(paramArray[i]);
                String elem = (String) p.get(0);
                String table = (String) p.get(1);
                String station = (String) p.get(2);
                Station stn = null;
                if (station != null) {
                    stn = new Station(station, table, "");
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
                Vector<?> v = api.getData(descr, stn, dateInterval, sampling);
                if (v != null && v.size() != 0) {
                    DataSequence ds = (DataSequence) v.get(0);
                    dss.add(ds);
                }
            }
            for (int i = 0; i < dss.size(); i++) {
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
                if (header) {
                    pw.println("#Spidr data output file in CSV format created at " + df.format(utc.getTime()));
                    pw.println("#GMT time is used");
                    pw.println("#param: " + parameters);
                    pw.println("#meta: http://spidr.ngdc.noaa.gov/spidr/GetMetadata?describe&param=" + parameters);
                    pw.println("#");
                    pw.println("#");
                    pw.println("#--------------------------------------------------");
                }
                for (int j = 0; j < dss.size(); j++) {
                    DSSExport.vec2csv(pw, (DataSequence) dss.elementAt(j), dataTimeFormat, header, fillMissing);
                }
                pw.close();
                if (compress) {
                    zip.finish();
                    zip.close();
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
	 * @param args
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
    @SuppressWarnings("unchecked")
    public void doGetZip(HashMap<String, String> args, HttpServletResponse res) throws ServletException, IOException {
        try {
            int dayIdFrom = Integer.parseInt(args.get("dateFrom"));
            int dayIdTo = Integer.parseInt(args.get("dateTo"));
            WDCDay dateFrom = new WDCDay(dayIdFrom);
            WDCDay dateTo = new WDCDay(dayIdTo);
            DateInterval dateInterval = new DateInterval(dateFrom, dateTo);
            String dataTimeFormat = args.get("timeFormat");
            int sampling = 0;
            String s = args.get("sampling");
            if (s != null) {
                sampling = Integer.parseInt(s);
            }
            String parameters = args.get("param");
            String[] paramArray = parameters.split(";");
            long curTime = (new java.util.Date()).getTime();
            res.addHeader("content-disposition", "attachment; filename=spidr_" + curTime + ".zip");
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
            for (int i = 0; i < paramArray.length; i++) {
                Vector<?> p = getParameter(paramArray[i]);
                String elem = (String) p.get(0);
                String table = (String) p.get(1);
                String station = (String) p.get(2);
                Station stn = null;
                if (station != null) {
                    stn = new Station(station, table, "");
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
                Vector<?> v = api.getData(descr, stn, dateInterval, sampling);
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
                for (int j = 0; j <= dss.size(); j++) {
                    dd = (DailyData) ((DataSequence) dss.elementAt(i)).elementAt(0);
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
                plt.println("#param: " + parameters);
                plt.println("#meta: http://spidr.ngdc.noaa.gov/spidr/GetMetadata?describe&param=" + parameters);
                plt.println("#");
                plt.println("#");
                plt.println("#--------------------------------------------------");
                for (int j = 0; j < dss.size(); j++) {
                    DSSExport.vec2stream(plt, (DataSequence) dss.elementAt(j), dataTimeFormat);
                }
                plt.close();
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void doGetJSON(HashMap<String, String> args, HttpServletResponse res, boolean compress) throws ServletException, IOException {
        if (compress) {
            res.setHeader("Content-Encoding", "gzip");
        }
        res.setContentType("text/plain");
        PrintWriter pw = null;
        GZIPOutputStream zip = null;
        if (compress) {
            zip = new GZIPOutputStream(res.getOutputStream());
            pw = new PrintWriter(zip);
        } else {
            pw = res.getWriter();
        }
        JSONObject json = new JSONObject(false);
        String callbackID = "-1";
        if (args.containsKey("callbackID")) {
            callbackID = args.get("callbackID");
        }
        json.accumulate("callbackID", callbackID);
        int dayIdFrom = Integer.parseInt(args.get("dateFrom"));
        int dayIdTo = Integer.parseInt(args.get("dateTo"));
        WDCDay dateFrom = new WDCDay(dayIdFrom);
        WDCDay dateTo = new WDCDay(dayIdTo);
        DateInterval dateInterval = new DateInterval(dateFrom, dateTo);
        int sampling = 0;
        String s = args.get("sampling");
        if (s != null) {
            sampling = Integer.parseInt(s);
        }
        String[] paramArray = null;
        try {
            String parameters = args.get("param");
            paramArray = parameters.split(";");
        } catch (NullPointerException npe) {
            throw new ServletException(npe);
        }
        Connection con = null;
        Statement stmt = null;
        WDCTable metaElem = null;
        try {
            con = ConnectionPool.getConnection("metadata");
            stmt = con.createStatement();
            metaElem = new WDCTable(stmt, SQL_LOAD_META_ELEMENTS);
            stmt.close();
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            try {
                ConnectionPool.releaseConnection(con);
            } catch (Exception ignore) {
            }
        }
        DataSequenceSet dss = new DataSequenceSet("");
        try {
            LocalApi api = new LocalApi();
            for (int i = 0; i < paramArray.length; i++) {
                Vector<?> p = getParameter(paramArray[i]);
                String elem = (String) p.get(0);
                String table = (String) p.get(1);
                String station = (String) p.get(2);
                Station stn = null;
                if (station != null) {
                    stn = new Station(station, table, "");
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
                Vector<?> v = api.getData(descr, stn, dateInterval, sampling);
                if (v != null && v.size() != 0) {
                    DataSequence ds = (DataSequence) v.get(0);
                    dss.add(ds);
                }
            }
        } catch (ApiException e) {
            throw new ServletException(e);
        }
        for (int nElem = 0; nElem < dss.size(); nElem++) {
            DataSequence ds = (DataSequence) dss.elementAt(nElem);
            DataDescription descr = ds.getDescription();
            JSONObject jsonVar = new JSONObject(false);
            jsonVar.accumulate("element", descr.getElement());
            jsonVar.accumulate("description", descr.getElemDescr());
            jsonVar.accumulate("units", descr.getUnits());
            jsonVar.accumulate("origin", descr.getOrigin());
            Station station = ds.getStation();
            if (station != null) {
                jsonVar.accumulate("station_code", station.getStn());
                jsonVar.accumulate("station_name", station.getName());
            }
            float missingValue = descr.getMissingValue();
            jsonVar.accumulate("sampling", DBAccess.samplingToNiceString(ds.getSampling()));
            jsonVar.accumulate("missing_value", missingValue);
            String timeFormat = args.get("timeFormat");
            if (timeFormat == null || timeFormat.trim().equals("")) {
                timeFormat = "yyyy-MM-dd HH:mm";
            }
            TimeZone tz = new SimpleTimeZone(0, "UTC");
            Calendar utc = new GregorianCalendar(tz);
            SimpleDateFormat df = null;
            if (!"JD".equals(timeFormat)) {
                df = new SimpleDateFormat(timeFormat, Locale.US);
                df.setTimeZone(tz);
                df.setCalendar(utc);
            }
            for (int num = 0; num < ds.size(); num++) {
                float[] data = ((DailyData) ds.get(num)).getData();
                if (data == null) {
                    continue;
                }
                String[] descrArray = ((DailyData) ds.get(num)).getDescrArray();
                String[] qualArray = ((DailyData) ds.get(num)).getQualArray();
                int[] times = ((DailyData) ds.get(num)).getTimes();
                int numData = (times != null) ? Math.min(data.length, times.length) : data.length;
                WDCDay day = new WDCDay(((DailyData) ds.get(num)).getDayId());
                utc.set(day.getYear(), day.getMonth() - 1, day.getDay(), 0, 0, 0);
                utc.set(Calendar.MILLISECOND, 0);
                float[] newData = new float[numData];
                String[] newDescrArray = new String[numData];
                String[] newQualArray = new String[numData];
                Object time = null;
                if ("JD".equals(timeFormat)) time = new double[numData]; else time = new String[numData];
                for (int k = 0; k < numData; k++) {
                    if (times != null) {
                        int h = times[k] / 60;
                        int m = times[k] - h * 60;
                        int sec = 0;
                        utc.set(Calendar.HOUR_OF_DAY, h);
                        utc.set(Calendar.MINUTE, m);
                        utc.set(Calendar.SECOND, sec);
                    }
                    newData[k] = data[k];
                    newDescrArray[k] = (descrArray == null) ? "" : descrArray[k];
                    newQualArray[k] = (qualArray == null) ? "" : qualArray[k];
                    if ("JD".equals(timeFormat)) ((double[]) time)[k] = Utilities.getJulianDate(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH) + 1, utc.get(Calendar.DAY_OF_MONTH), utc.get(Calendar.HOUR_OF_DAY), utc.get(Calendar.MINUTE), utc.get(Calendar.SECOND)); else ((String[]) time)[k] = df.format(utc.getTime());
                }
                if (times == null) {
                    utc.add(Calendar.MINUTE, sampling);
                }
                jsonVar.accumulate("values", JSONArray.fromObject(newData));
                jsonVar.accumulate("time", JSONArray.fromObject(time));
                jsonVar.accumulate("descr", JSONArray.fromObject(newDescrArray));
                jsonVar.accumulate("qual", JSONArray.fromObject(newQualArray));
            }
            json.accumulate("variables", jsonVar);
        }
        pw.print(json.toString(1));
        pw.close();
        if (compress) {
            zip.finish();
            zip.close();
        }
    }

    /**
	 * @param args
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
    @SuppressWarnings("unchecked")
    public void doGetImage(HashMap<String, String> args, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("image/png");
        try {
            int width = 800;
            int height = 400;
            String swidth = args.get("width");
            String sheight = args.get("height");
            if (swidth != null) width = Integer.parseInt(swidth);
            if (sheight != null) height = Integer.parseInt(sheight);
            String marks = args.get("marks");
            if (marks == null) marks = "none";
            String representation = args.get("representation");
            int dayIdFrom = Integer.parseInt(args.get("dateFrom"));
            int dayIdTo = Integer.parseInt(args.get("dateTo"));
            WDCDay dateFrom = new WDCDay(dayIdFrom);
            WDCDay dateTo = new WDCDay(dayIdTo);
            DateInterval dateInterval = new DateInterval(dateFrom, dateTo);
            int sampling = 0;
            String s = args.get("sampling");
            if (s != null) {
                sampling = Integer.parseInt(s);
            }
            String[] paramArray = null;
            try {
                String parameters = args.get("param");
                paramArray = parameters.split(";");
            } catch (NullPointerException npe) {
                throw new ServletException(npe);
            }
            String[] colorArray = null;
            try {
                String colors = args.get("color");
                colorArray = colors.split(";");
            } catch (NullPointerException ignore) {
            }
            if (colorArray != null) {
                if (paramArray.length != colorArray.length) {
                    throw new ServletException("color/param length mismatch");
                }
            }
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
            dss.setRepresentation(representation);
            dss.attributes.put("marks", marks);
            for (int i = 0; i < paramArray.length; i++) {
                Vector<?> p = getParameter(paramArray[i]);
                String elem = (String) p.get(0);
                String table = (String) p.get(1);
                String station = (String) p.get(2);
                Station stn = null;
                if (station != null) {
                    stn = new Station(station, table, "");
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
                Vector<?> v = api.getData(descr, stn, dateInterval, sampling);
                if (v != null && v.size() != 0) {
                    DataSequence ds = (DataSequence) v.get(0);
                    if (colorArray != null && !(colorArray.length <= i)) ds.attributes.put("color", colorArray[i]);
                    dss.add(ds);
                }
            }
            Plot plotPanel = new Plot(false);
            plotPanel.setSize(width, height);
            if (dateInterval == null || !dateInterval.isValid()) {
                PlotDSS.plotOnPanel(plotPanel, dss);
            } else {
                PlotDSS.plotOnPanel(plotPanel, dss, dateInterval.getDateFrom(), dateInterval.getDateTo());
            }
            BufferedImage screenDump = new BufferedImage(plotPanel.getWidth(), plotPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics sgrf = screenDump.getGraphics();
            plotPanel.paint(sgrf);
            ImageIO.write(screenDump, "png", res.getOutputStream());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
	 * @param args
	 * @param res
	 * @throws ServletException
	 * @throws IOException
	 */
    public void doGetRemote(HashMap<String, String> args, HttpServletResponse res) throws ServletException, IOException {
        try {
            res.setContentType("application/zip");
            OutputStream out = res.getOutputStream();
            res.setHeader("Content-Language", "en");
            int dayIdFrom = Integer.parseInt(args.get("dateFrom"));
            int dayIdTo = Integer.parseInt(args.get("dateTo"));
            WDCDay dateFrom = new WDCDay(dayIdFrom);
            WDCDay dateTo = new WDCDay(dayIdTo);
            DateInterval dateInterval = new DateInterval(dateFrom, dateTo);
            int sampling = 0;
            String s = args.get("sampling");
            if (s != null) {
                sampling = Integer.parseInt(s);
            }
            String format = args.get("format");
            String parameters = args.get("param");
            String[] paramArray = parameters.split(";");
            String zipFileName = "spidr_" + dateFrom.getDayId() + "_" + dateTo.getDayId() + "_" + (new java.util.Date()).getTime() + ".zip";
            res.addHeader("content-disposition", "attachment; filename=" + zipFileName);
            Vector<String> fileList = new Vector<String>();
            String exportPath = Settings.get("locations.localExportDir");
            for (int i = 0; i < paramArray.length; i++) {
                Vector<String> p = getParameter(paramArray[i]);
                String elem = (String) p.get(0);
                String table = (String) p.get(1);
                String station = (String) p.get(2);
                String result = DSSExport.remoteExport(table, station, elem, dateInterval, format, sampling);
                result = exportPath + result.substring(result.lastIndexOf('/') + 1);
                fileList.add(result);
            }
            Utilities.compressWithZip(fileList, exportPath + zipFileName);
            File f = new File(exportPath + zipFileName);
            FileInputStream fis = new FileInputStream(f);
            byte[] b = new byte[BUF_SIZE];
            int len;
            while ((len = fis.read(b)) > 0) {
                out.write(b, 0, len);
            }
            out.close();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
	 * @param args
	 * @return
	 */
    public long getSize(HashMap<String, String> args) {
        try {
            int sampling = 0;
            String s = args.get("sampling");
            if (s != null) {
                sampling = Integer.parseInt(s);
            }
            int dayIdFrom = Integer.parseInt(args.get("dateFrom"));
            int dayIdTo = Integer.parseInt(args.get("dateTo"));
            WDCDay dateFrom = new WDCDay(dayIdFrom);
            WDCDay dateTo = new WDCDay(dayIdTo);
            Calendar c1 = Calendar.getInstance(Locale.US);
            Calendar c2 = Calendar.getInstance(Locale.US);
            c1.setTimeZone(TimeZone.getTimeZone("UTC"));
            c2.setTimeZone(TimeZone.getTimeZone("UTC"));
            c1.set(dateFrom.getYear(), dateFrom.getMonth() - 1, dateFrom.getDay(), 0, 0, 0);
            c2.set(dateTo.getYear(), dateTo.getMonth() - 1, dateTo.getDay(), 0, 0, 0);
            long t1 = c1.getTimeInMillis();
            long t2 = c2.getTimeInMillis();
            long dateInterval = (t2 - t1) / 1000 / 60;
            long size = 0;
            String parameters = args.get("param");
            String[] paramArray = parameters.split(";");
            for (int i = 0; i < paramArray.length; i++) {
                if (sampling != 0) {
                    size += dateInterval / sampling * 4;
                } else {
                    Vector<String> p = getParameter(paramArray[i]);
                    String elem = (String) p.get(0);
                    String table = (String) p.get(1);
                    String station = (String) p.get(2);
                    String className = Settings.get(table + ".classGetter");
                    try {
                        DBAccess accessClass = (DBAccess) Class.forName(className).newInstance();
                        int minSampling = accessClass.getMinSampling(elem, station);
                        size += dateInterval / minSampling * 4;
                    } catch (Exception e) {
                        throw new ServletException("Failed to get min sampling for " + table + ": ", e);
                    }
                }
            }
            return size;
        } catch (Exception e) {
            log.error("Could not estimate request size: ", e);
            return -1;
        }
    }
}
