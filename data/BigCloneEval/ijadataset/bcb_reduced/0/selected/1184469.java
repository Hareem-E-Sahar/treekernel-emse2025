package spidr.datamodel;

import java.util.*;
import java.util.zip.*;
import java.sql.*;
import java.io.*;
import java.text.*;
import org.apache.log4j.*;
import wdc.dbaccess.*;
import wdc.utils.*;
import wdc.settings.*;
import spidr.dbaccess.*;
import spidr.datamodel.*;
import spidr.export.*;
import spidr.swr.*;
import spidr.dbload.UpdateMetadata;

/** * Get and view SWR data */
public class SWRDataBean {

    private static Logger log = Logger.getLogger(SWRDataBean.class);

    static final int AVERAGE_WINDOW = 180;

    static final int VARIANCE_WINDOW = 10;

    static final int WIN_WIDTH = 480;

    static final String SQL_LOAD_META_ELEMENTS = "SELECT CONCAT(elements_descr.element,\"@\",elements_descr.elemTable) AS elemKey, elements_descr.*, " + "min_yrMon*100+1 AS dateFrom, max_yrMon*100+31 AS dateTo " + "FROM elements_descr LEFT JOIN elements_periods ON " + "(elements_descr.elemTable=elements_periods.dataTable AND elements_descr.element=elements_periods.param) " + "ORDER BY elemTable, element";

    static final String SQL_LOAD_META_STATIONS = "SELECT stations_descr.*, " + "min_yrMon*100+1 AS dateFrom, max_yrMon*100+31 AS dateTo " + "FROM stations_descr LEFT JOIN stations_periods ON " + "(stations_descr.dataTable=stations_periods.dataTable AND stations_descr.stn=stations_periods.param) " + "ORDER BY dataTable, stName";

    static final String SQL_LOAD_PARAMS_AND_ELEMENTS = "SELECT CONCAT(element,\"@\",elemTable) AS elemKey, params_and_elements.* " + "FROM params_and_elements " + "ORDER BY viewGroup, elemTable, element";

    /** Finds date coverage for a sequence of dailyRecords  */
    public static DateInterval findDateInterval(Vector dailyRecords) {
        if (dailyRecords == null || dailyRecords.size() == 0) return null;
        int dayIdMin = ((DailyData) dailyRecords.get(0)).getDayId();
        int dayIdMax = dayIdMin;
        for (int k = 0; k < dailyRecords.size(); k++) {
            int dayId = ((DailyData) dailyRecords.get(k)).getDayId();
            if (dayIdMin > dayId) dayIdMin = dayId;
            if (dayIdMax < dayId) dayIdMax = dayId;
        }
        return new DateInterval(new WDCDay(dayIdMin), new WDCDay(dayIdMax));
    }

    /** Creates files and adds data to html form to plot them.  * @param dss The dataSequenceSet object  * @return vector of parameters  */
    public static Vector plotData2(DataSequenceSet dss) throws Exception {
        return plotData2(dss, "ALL", 0, 0);
    }

    /** Creates files and adds data to html form to plot them.  * @param dss The dataSequenceSet object  * @param dataSrc The data source or table name: Geom or Intermag or ALL  * @param startPos The station to start from  * @param numStations The number of staions to plot  * @return vector of parameters  */
    public static Vector plotData2(DataSequenceSet dss, String dataSrc, int startPos, int numStations) throws Exception {
        if (dss == null) return null;
        if (numStations <= 0) numStations = 0;
        if (startPos <= 0) startPos = 0;
        if (dataSrc == null || dataSrc.trim().equals("")) dataSrc = "ALL";
        Vector params = new Vector();
        String exportPath = Settings.get("locations.localExportDir");
        String importPath = Settings.get("locations.httpExportDir");
        String appletPath = Settings.get("locations.httpRoot") + Settings.get("locations.appletDir");
        Connection con = null;
        Statement stmt = null;
        try {
            con = ConnectionPool.getConnection("metadata");
            stmt = con.createStatement();
            WDCTable metaPrm = Metadata.getDataSet("spidr_params_and_elements", stmt, SQL_LOAD_PARAMS_AND_ELEMENTS);
            int indPrmElemKey = metaPrm.getColumnIndex("elemKey");
            int indPrmWinTitle = metaPrm.getColumnIndex("winTitle");
            int indPrmScale = metaPrm.getColumnIndex("scale");
            int indPrmRatio = metaPrm.getColumnIndex("ratio");
            int indPrmLabel = metaPrm.getColumnIndex("label");
            WDCTable metaElem = Metadata.getDataSet("spidr_meta_elements", stmt, SQL_LOAD_META_ELEMENTS);
            int indElemElemKey = metaElem.getColumnIndex("elemKey");
            int indElemDescription = metaElem.getColumnIndex("description");
            int indElemMultiplier = metaElem.getColumnIndex("multiplier");
            int indElemMissingValue = metaElem.getColumnIndex("missingValue");
            int indElemUnits = metaElem.getColumnIndex("units");
            WDCTable metaStat = Metadata.getDataSet("spidr_meta_stations", stmt, SQL_LOAD_META_STATIONS);
            int indStatStation = metaStat.getColumnIndex("stn");
            int indStatTable = metaStat.getColumnIndex("dataTable");
            int indStatDescr = metaStat.getColumnIndex("stName");
            Vector stList = new Vector();
            String[] ELEMENTS = { "X, H", "Y, E, D", "Z" };
            for (int nDs = 0; nDs < dss.size(); nDs++) {
                Station st = ((DataSequence) dss.get(nDs)).getStation();
                if (st != null && stList.indexOf(st) == -1) stList.add(st);
            }
            if (startPos > stList.size()) startPos = stList.size();
            int endPos = (numStations == 0) ? stList.size() - 1 : Math.min(stList.size() - 1, startPos + numStations - 1);
            Vector vecDssWin = new Vector();
            Vector vecRatio = new Vector();
            float ratio = 0.1f * (endPos - startPos + 2);
            String scale = "std";
            final String WIN_TITLE_PREF = "Elements ";
            {
                for (int k = 0; k < ELEMENTS.length; k++) {
                    vecRatio.add(new Float(ratio));
                    vecDssWin.add(new DataSequenceSet(WIN_TITLE_PREF + ELEMENTS[k], scale));
                }
            }
            for (int nDs = 0; nDs < dss.size(); nDs++) {
                DataSequence ds = (DataSequence) dss.get(nDs);
                int stInd = stList.indexOf(ds.getStation());
                if (stInd < startPos || stInd > endPos) continue;
                String table = ds.getDescription().getTable();
                String element = ds.getDescription().getElement();
                String elemKey = element + "@" + table;
                if (!(dataSrc.equalsIgnoreCase("ALL") || dataSrc.equals(table))) continue;
                String winTitle = WIN_TITLE_PREF;
                for (int k = 0; k < ELEMENTS.length; k++) if (ELEMENTS[k].indexOf(element) != -1) {
                    winTitle += ELEMENTS[k];
                    break;
                }
                String label = element;
                int ind = metaPrm.findRow(indPrmElemKey, elemKey);
                ind = metaElem.findRow(indElemElemKey, elemKey);
                String elemDescr = (ind != -1) ? (String) metaElem.getValueAt(ind, indElemDescription) : "";
                String units = (ind != -1) ? (String) metaElem.getValueAt(ind, indElemUnits) : "";
                DataDescription descr = new DataDescription(table, element, "", elemDescr, units, label);
                if (ind != -1) {
                    try {
                        descr.setMultiplier(Float.parseFloat((String) metaElem.getValueAt(ind, indElemMultiplier)));
                    } catch (NumberFormatException e) {
                    }
                    try {
                        descr.setMissingValue(Float.parseFloat((String) metaElem.getValueAt(ind, indElemMissingValue)));
                    } catch (NumberFormatException e) {
                    }
                }
                ds.attachDescription(descr);
                if (ds.getStation() != null) {
                    String stCode = ds.getStation().getStn();
                    ind = metaStat.findRow(indStatStation, stCode, indStatTable, table);
                    String stDescr = (ind != -1) ? (String) metaStat.getValueAt(ind, indStatDescr) : stCode;
                    Station station = new Station(stCode, table, stDescr);
                    ds.attachStation(station);
                }
                boolean createNewDSS = true;
                for (int k = 0; k < vecDssWin.size(); k++) {
                    DataSequenceSet dssWin = (DataSequenceSet) vecDssWin.get(k);
                    if (winTitle.equals(dssWin.getTitle())) {
                        dssWin.add(ds);
                        createNewDSS = false;
                        break;
                    }
                }
                if (createNewDSS) {
                    DataSequenceSet dssWin = new DataSequenceSet(winTitle, scale);
                    dssWin.add(ds);
                    vecRatio.add(new Float(ratio));
                    vecDssWin.add(dssWin);
                }
            }
            for (int winCounter = 0; winCounter < vecDssWin.size(); winCounter++) {
                DataSequenceSet curDss = (DataSequenceSet) vecDssWin.get(winCounter);
                String firstTable = "unknown";
                String firstElement = "unknown";
                String stnCode = "unknown";
                for (int n = 0; n < curDss.size(); n++) {
                    DataSequence ds = (DataSequence) curDss.get(n);
                    for (int k = 0; k < ds.size(); k++) {
                        if (((DailyData) ds.get(k)).getData() != null) {
                            firstTable = ds.getDescription().getTable();
                            firstElement = ds.getDescription().getElement();
                            if (ds.getStation() != null) stnCode = ds.getStation().getStn();
                            break;
                        }
                    }
                }
                long curTime = (new java.util.Date()).getTime();
                String filename = "spidr_" + curTime + "_" + (int) (Math.random() * 10000) + "_" + winCounter + ".gif";
                float SHIFT = 1000;
                DSSExport.printDataSet2(curDss, exportPath + filename, WIN_WIDTH, (int) (WIN_WIDTH * ((Float) vecRatio.get(winCounter)).floatValue()), SHIFT);
                String paramStr = "plots[" + winCounter + "] = new plotDescr(\"" + dataSrc + "\", \"" + importPath + filename + "\", " + WIN_WIDTH + ", " + (int) (WIN_WIDTH * ((Float) vecRatio.get(winCounter)).floatValue()) + ", " + (endPos - startPos + 1) + ");";
                params.add(paramStr);
            }
            for (int k = startPos; k <= endPos; k++) {
                Station st = (Station) stList.get(k);
                String paramStr = "stns[" + (k - startPos) + "] = new stnDescr(\"" + st.getType() + "\", \"" + st.getStn() + "\", \"" + st.getName() + "\");";
                params.add(paramStr);
            }
            return params;
        } catch (Exception e) {
            throw new Exception("Error: " + e);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception e) {
            }
            ConnectionPool.releaseConnection(con);
        }
    }

    /** Inserts (replaces) data into the database.  * All passed data should have the same sampling and year.  * @param dailyRecords The vector of DailyData objects.  */
    public static void loadData(Vector dailyRecords) throws Exception {
        final float SWR_MISSING_VALUE = -1e+32f;
        if (dailyRecords == null || dailyRecords.size() == 0) return;
        int year = (((DailyData) dailyRecords.get(0)).getDayId()) / 10000;
        int month = ((((DailyData) dailyRecords.get(0)).getDayId()) / 100) % 100;
        int numData = (((DailyData) dailyRecords.get(0)).getData()).length;
        if (numData != 1440) throw new Exception("Wrong data: all data must have 1-minute sampling");
        int sampling = 1440 / numData;
        for (int k = 0; k < dailyRecords.size(); k++) {
            DailyData dd = (DailyData) dailyRecords.get(k);
            if (dd.getStation() == null) throw new Exception("No station: station required to load data");
            if ((dd.getDayId()) / 100 != year * 100 + month || (dd.getData()).length != numData) throw new Exception("Wrong data: all data must have the same sampling, year and month");
        }
        String tableName = "min" + year;
        Connection con = null;
        try {
            con = ConnectionPool.getConnection("SWR");
            String sqlStr = "REPLACE INTO " + tableName + "(stn,obsdate,origin,element,src,base,Nobs,gdata) VALUES(?,?,?,?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sqlStr);
            for (int nRec = 0; nRec < dailyRecords.size(); nRec++) {
                DailyData dd = (DailyData) dailyRecords.get(nRec);
                String stn = dd.getStation().getStn();
                WDCDay obsdate = new WDCDay(dd.getDayId());
                String element = dd.getDescription().getElement();
                String src = dd.getDescription().getTable();
                String origin = dd.getDescription().getOrigin();
                float missingValue = dd.getDescription().getMissingValue();
                float[] data = dd.getData();
                if (data == null) continue;
                ByteArrayOutputStream bostr = new ByteArrayOutputStream();
                DataOutputStream dostr = new DataOutputStream(bostr);
                final int baseValue = 0;
                int nObs = 0;
                for (int k = 0; k < data.length; k++) {
                    if (data[k] != missingValue) nObs++; else data[k] = SWR_MISSING_VALUE;
                    dostr.writeFloat(data[k]);
                }
                byte[] buf = bostr.toByteArray();
                ByteArrayInputStream binstr = new ByteArrayInputStream(buf);
                ps.setString(1, stn);
                if (obsdate.isValid()) ps.setString(2, obsdate.toString()); else ps.setString(2, "0000-00-00");
                ps.setString(3, origin);
                ps.setString(4, element);
                ps.setString(5, src);
                ps.setString(6, "" + baseValue);
                ps.setString(7, "" + nObs);
                ps.setBinaryStream(8, binstr, buf.length);
                ps.executeUpdate();
                binstr.close();
                dostr.close();
                bostr.close();
            }
            ps.close();
        } catch (Exception e) {
            throw new Exception("Data loading error: " + e.toString());
        } finally {
            ConnectionPool.releaseConnection(con);
        }
    }

    /** Normalize data to [-0.5, 0.5]  (so 0 is mean line) and makes shift  * @param ds Data  * @return coefficient of normalization  */
    public static float normalizeData(DataSequence ds, float missingValue, float shift) {
        float avrValue = 0;
        int counter = 0;
        for (int num = 0; num < ds.size(); num++) {
            float[] data = ((DailyData) ds.get(num)).getData();
            if (data != null) for (int k = 0; k < data.length; k++) if (data[k] != missingValue) {
                avrValue += data[k];
                counter++;
            }
        }
        if (counter > 0) avrValue /= counter;
        float maxValue = 0;
        for (int num = 0; num < ds.size(); num++) {
            float[] data = ((DailyData) ds.get(num)).getData();
            if (data != null) for (int k = 0; k < data.length; k++) if (data[k] != missingValue && maxValue < Math.abs(data[k] - avrValue)) maxValue = Math.abs(data[k] - avrValue);
        }
        maxValue *= 2;
        if (maxValue == 0) maxValue = 1;
        for (int num = 0; num < ds.size(); num++) {
            float[] data = ((DailyData) ds.get(num)).getData();
            if (data != null) for (int k = 0; k < data.length; k++) if (data[k] != missingValue) data[k] = (data[k] - avrValue) / maxValue + shift;
        }
        return maxValue;
    }

    /** Creates files and adds data to html form to plot, print or write values of elements from basket.  * @param dss The dataSequenceSet object  * @return vector of parameters  */
    public static Vector plotData(DataSequenceSet dss) throws Exception {
        if (dss == null) return null;
        Vector params = new Vector();
        String exportPath = Settings.get("locations.localExportDir");
        String importPath = Settings.get("locations.httpExportDir");
        String appletPath = Settings.get("locations.httpRoot") + Settings.get("locations.appletDir");
        Connection con = null;
        Statement stmt = null;
        try {
            con = ConnectionPool.getConnection("metadata");
            stmt = con.createStatement();
            WDCTable metaPrm = Metadata.getDataSet("spidr_params_and_elements", stmt, SQL_LOAD_PARAMS_AND_ELEMENTS);
            int indPrmElemKey = metaPrm.getColumnIndex("elemKey");
            int indPrmWinTitle = metaPrm.getColumnIndex("winTitle");
            int indPrmScale = metaPrm.getColumnIndex("scale");
            int indPrmRatio = metaPrm.getColumnIndex("ratio");
            int indPrmLabel = metaPrm.getColumnIndex("label");
            WDCTable metaElem = Metadata.getDataSet("spidr_meta_elements", stmt, SQL_LOAD_META_ELEMENTS);
            int indElemElemKey = metaElem.getColumnIndex("elemKey");
            int indElemDescription = metaElem.getColumnIndex("description");
            int indElemMultiplier = metaElem.getColumnIndex("multiplier");
            int indElemMissingValue = metaElem.getColumnIndex("missingValue");
            int indElemUnits = metaElem.getColumnIndex("units");
            WDCTable metaStat = Metadata.getDataSet("spidr_meta_stations", stmt, SQL_LOAD_META_STATIONS);
            int indStatStation = metaStat.getColumnIndex("stn");
            int indStatTable = metaStat.getColumnIndex("dataTable");
            int indStatDescr = metaStat.getColumnIndex("stName");
            Vector vecDssWin = new Vector();
            Vector vecRatio = new Vector();
            for (int nDs = 0; nDs < dss.size(); nDs++) {
                DataSequence ds = (DataSequence) dss.get(nDs);
                String table = ds.getDescription().getTable();
                String element = ds.getDescription().getElement();
                String elemKey = element + "@" + table;
                String winTitle = "";
                String scale = "";
                float ratio = 0.5f;
                String label = "";
                int ind = metaPrm.findRow(indPrmElemKey, elemKey);
                if (ind != -1) {
                    winTitle = (String) metaPrm.getValueAt(ind, indPrmWinTitle);
                    scale = (String) metaPrm.getValueAt(ind, indPrmScale);
                    ratio = Float.parseFloat((String) metaPrm.getValueAt(ind, indPrmRatio));
                    label = (String) metaPrm.getValueAt(ind, indPrmLabel);
                }
                ind = metaElem.findRow(indElemElemKey, elemKey);
                String elemDescr = (ind != -1) ? (String) metaElem.getValueAt(ind, indElemDescription) : "";
                String units = (ind != -1) ? (String) metaElem.getValueAt(ind, indElemUnits) : "";
                DataDescription descr = new DataDescription(table, element, "", elemDescr, units, label);
                if (ind != -1) {
                    try {
                        descr.setMultiplier(Float.parseFloat((String) metaElem.getValueAt(ind, indElemMultiplier)));
                    } catch (NumberFormatException e) {
                    }
                    try {
                        descr.setMissingValue(Float.parseFloat((String) metaElem.getValueAt(ind, indElemMissingValue)));
                    } catch (NumberFormatException e) {
                    }
                }
                ds.attachDescription(descr);
                if (ds.getStation() != null) {
                    String stCode = ds.getStation().getStn();
                    ind = metaStat.findRow(indStatStation, stCode, indStatTable, table);
                    String stDescr = (ind != -1) ? (String) metaStat.getValueAt(ind, indStatDescr) : stCode;
                    Station station = new Station(stCode, table, stDescr);
                    ds.attachStation(station);
                    winTitle += "@" + station.getName();
                }
                boolean createNewDSS = true;
                for (int k = 0; k < vecDssWin.size(); k++) {
                    DataSequenceSet dssWin = (DataSequenceSet) vecDssWin.get(k);
                    if (winTitle.equals(dssWin.getTitle())) {
                        dssWin.add(ds);
                        createNewDSS = false;
                        break;
                    }
                }
                if (createNewDSS) {
                    DataSequenceSet dssWin = new DataSequenceSet(winTitle, scale);
                    dssWin.add(ds);
                    vecRatio.add(new Float(ratio));
                    vecDssWin.add(dssWin);
                }
            }
            for (int winCounter = 0; winCounter < vecDssWin.size(); winCounter++) {
                DataSequenceSet curDss = (DataSequenceSet) vecDssWin.get(winCounter);
                boolean hasData = false;
                String firstTable = "unknown";
                String firstElement = "unknown";
                String stnCode = "unknown";
                for (int n = 0; n < curDss.size(); n++) {
                    DataSequence ds = (DataSequence) curDss.get(n);
                    for (int k = 0; k < ds.size(); k++) {
                        if (((DailyData) ds.get(k)).getData() != null) {
                            hasData = true;
                            firstTable = ds.getDescription().getTable();
                            firstElement = ds.getDescription().getElement();
                            if (ds.getStation() != null) stnCode = ds.getStation().getStn();
                            break;
                        }
                    }
                    if (hasData) break;
                }
                if (hasData) {
                    long curTime = (new java.util.Date()).getTime();
                    String filename = "spidr_" + curTime + "_" + (int) (Math.random() * 10000) + "_" + winCounter + ".gif";
                    DSSExport.printDataSet(curDss, exportPath + filename, WIN_WIDTH, (int) (WIN_WIDTH * ((Float) vecRatio.get(winCounter)).floatValue()));
                    String paramStr = "prms[" + winCounter + "] = new prmDescr(\"" + importPath + filename + "\", \"" + firstTable + "\", \"" + firstElement + "\", \"" + stnCode + "\");";
                    params.add(paramStr);
                } else {
                    String paramStr = "prms[" + winCounter + "] = new prmDescr(null, \"Preview unavailable\", null, null)";
                    params.add(paramStr);
                }
            }
            return params;
        } catch (Exception e) {
            throw new Exception("Error: " + e);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception e) {
            }
            ConnectionPool.releaseConnection(con);
        }
    }

    /** Creates input files for AMIE runs from basket.  * @param dss The dataSequenceSet object  * @return URL to the exported data  */
    public static String exportData(DataSequenceSet dss, String fileName) throws Exception {
        if (dss == null || fileName == null) return null;
        String exportPath = Settings.get("locations.localExportDir");
        String importPath = Settings.get("locations.httpExportDir");
        Connection con = null;
        Statement stmt = null;
        try {
            con = ConnectionPool.getConnection("metadata");
            stmt = con.createStatement();
            WDCTable metaPrm = Metadata.getDataSet("spidr_params_and_elements", stmt, SQL_LOAD_PARAMS_AND_ELEMENTS);
            int indPrmElemKey = metaPrm.getColumnIndex("elemKey");
            int indPrmWinTitle = metaPrm.getColumnIndex("winTitle");
            int indPrmScale = metaPrm.getColumnIndex("scale");
            int indPrmRatio = metaPrm.getColumnIndex("ratio");
            int indPrmLabel = metaPrm.getColumnIndex("label");
            WDCTable metaElem = Metadata.getDataSet("spidr_meta_elements", stmt, SQL_LOAD_META_ELEMENTS);
            int indElemElemKey = metaElem.getColumnIndex("elemKey");
            int indElemDescription = metaElem.getColumnIndex("description");
            int indElemMultiplier = metaElem.getColumnIndex("multiplier");
            int indElemMissingValue = metaElem.getColumnIndex("missingValue");
            int indElemUnits = metaElem.getColumnIndex("units");
            WDCTable metaStat = Metadata.getDataSet("spidr_meta_stations", stmt, SQL_LOAD_META_STATIONS);
            int indStatStation = metaStat.getColumnIndex("stn");
            int indStatTable = metaStat.getColumnIndex("dataTable");
            int indStatDescr = metaStat.getColumnIndex("stName");
            Vector vecDssWin = new Vector();
            Vector vecRatio = new Vector();
            for (int nDs = 0; nDs < dss.size(); nDs++) {
                DataSequence ds = (DataSequence) dss.get(nDs);
                String table = ds.getDescription().getTable();
                String element = ds.getDescription().getElement();
                String elemKey = element + "@" + table;
                String winTitle = "";
                String scale = "";
                float ratio = 0.5f;
                String label = "";
                int ind = metaPrm.findRow(indPrmElemKey, elemKey);
                if (ind != -1) {
                    winTitle = (String) metaPrm.getValueAt(ind, indPrmWinTitle);
                    scale = (String) metaPrm.getValueAt(ind, indPrmScale);
                    ratio = Float.parseFloat((String) metaPrm.getValueAt(ind, indPrmRatio));
                    label = (String) metaPrm.getValueAt(ind, indPrmLabel);
                }
                ind = metaElem.findRow(indElemElemKey, elemKey);
                String elemDescr = (ind != -1) ? (String) metaElem.getValueAt(ind, indElemDescription) : "";
                String units = (ind != -1) ? (String) metaElem.getValueAt(ind, indElemUnits) : "";
                DataDescription descr = new DataDescription(table, element, "", elemDescr, units, label);
                if (ind != -1) {
                    try {
                        descr.setMultiplier(Float.parseFloat((String) metaElem.getValueAt(ind, indElemMultiplier)));
                    } catch (NumberFormatException e) {
                    }
                    try {
                        descr.setMissingValue(Float.parseFloat((String) metaElem.getValueAt(ind, indElemMissingValue)));
                    } catch (NumberFormatException e) {
                    }
                }
                ds.attachDescription(descr);
                if (ds.getStation() != null) {
                    String stCode = ds.getStation().getStn();
                    ind = metaStat.findRow(indStatStation, stCode, indStatTable, table);
                    String stDescr = (ind != -1) ? (String) metaStat.getValueAt(ind, indStatDescr) : stCode;
                    Station station = new Station(stCode, table, stDescr);
                    ds.attachStation(station);
                    winTitle += "@" + station.getName();
                }
                boolean createNewDSS = true;
                for (int k = 0; k < vecDssWin.size(); k++) {
                    DataSequenceSet dssWin = (DataSequenceSet) vecDssWin.get(k);
                    if (winTitle.equals(dssWin.getTitle())) {
                        dssWin.add(ds);
                        createNewDSS = false;
                        break;
                    }
                }
                if (createNewDSS) {
                    DataSequenceSet dssWin = new DataSequenceSet(winTitle, scale);
                    dssWin.add(ds);
                    vecRatio.add(new Float(ratio));
                    vecDssWin.add(dssWin);
                }
            }
            writeData(vecDssWin, exportPath + fileName);
            return importPath + fileName;
        } catch (Exception e) {
            throw new Exception("Error: " + e);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception e) {
            }
            ConnectionPool.releaseConnection(con);
        }
    }

    /** Retrieves a vector of DailyData  * @param dayId day ID formatted like YYYYMMDD  * @return Vector of DSS objects containing data from SPIDR   */
    public static Vector getDailyRecords(int dayId) throws ApiException {
        return getDailyRecords(null, dayId, 0);
    }

    /** Retrieves a vector of DailyData  * @param dayId day ID formatted like YYYYMMDD  * @param sampling in minutes, if 0 then config defaults used  * @return Vector of DSS objects containing data from SPIDR   */
    public static Vector getDailyRecords(int dayId, int sampling) throws ApiException {
        return getDailyRecords(null, dayId, sampling);
    }

    /** Retrieves a vector of DailyData  * @param tableListStr optional (may be null) name of the DB tables with data  * @param dayId day ID formatted like YYYYMMDD  * @param sampling in minutes, if 0 then config defaults used  * @return Vector of DSS objects containing data from SPIDR   */
    public static Vector getDailyRecords(String list, int dayId, int sampling) throws ApiException {
        LocalApi api = new LocalApi();
        SwrApi swrApi = new SwrApi();
        if (list == null) list = "swrData.tableList";
        String tableListStr = Settings.get(list);
        if (tableListStr == null) return null;
        StringTokenizer st = new StringTokenizer(tableListStr, ",; \t\r\n");
        Vector tableList = new Vector();
        while (st.hasMoreTokens()) tableList.add(st.nextToken());
        Vector dailyRecords = new Vector();
        for (int nTable = 0; nTable < tableList.size(); nTable++) {
            String table = (String) tableList.get(nTable);
            String elemListStr = Settings.get("swrData." + table + ".elements");
            if (elemListStr == null) continue;
            st = new StringTokenizer(elemListStr, ",; \t\r\n");
            Vector elemList = new Vector();
            while (st.hasMoreTokens()) elemList.add(st.nextToken());
            if (sampling == 0) {
                try {
                    sampling = Integer.parseInt(Settings.get("swrData." + table + ".sampling"));
                } catch (Exception e) {
                }
            }
            String statListStr = Settings.get("swrData." + table + ".stations");
            if (statListStr == null) {
                statListStr = "";
            }
            Vector statList = new Vector();
            if (statListStr != null && !statListStr.equals("")) {
                statList = swrApi.getStationList(table);
                if (statList == null) {
                    statList = new Vector();
                    log.debug("SWRDataBean.getDailyRecords(): station list from API is null");
                }
                if (!statListStr.trim().equals("GET_ALL_STATIONS")) {
                    Vector statBuffer = new Vector();
                    st = new StringTokenizer(statListStr, ",; \t\r\n");
                    while (st.hasMoreTokens()) {
                        String stn = st.nextToken();
                        for (int i = 0; i < statList.size(); i++) {
                            Station stnTmp = (Station) statList.get(i);
                            if (stnTmp.getStn().equals(stn)) {
                                statBuffer.add(stnTmp);
                            }
                        }
                    }
                    statList = statBuffer;
                }
            }
            for (int nElem = 0; nElem < elemList.size(); nElem++) {
                String element = (String) elemList.get(nElem);
                DataDescription descr = api.getDescription(table, element);
                log.debug("SWRDataBean.getDailyRecords(): dayId " + dayId + " table " + table + " element " + element + " missing value " + descr.getMissingValue());
                for (int nStat = 0; nStat < statList.size(); nStat++) {
                    Station station = (Station) statList.get(nStat);
                    Vector partOfData = api.getData(descr, station, new DateInterval(new WDCDay(dayId), new WDCDay(dayId)), sampling);
                    if (partOfData != null && partOfData.size() > 0) {
                        DataSequence firstDS = (DataSequence) partOfData.elementAt(0);
                        Enumeration enumer = firstDS.elements();
                        while (enumer.hasMoreElements()) {
                            DailyData dd = (DailyData) enumer.nextElement();
                            dd.attachStation(station);
                        }
                        firstDS.attachStation(station);
                        dailyRecords.addAll(firstDS);
                        log.debug("SWRDataBean.getDailyRecords(): found station " + station.getStn());
                    }
                }
                if (statList.size() == 0) {
                    Vector partOfData = api.getData(descr, null, new DateInterval(new WDCDay(dayId), new WDCDay(dayId)), sampling);
                    if (partOfData != null && partOfData.size() > 0) {
                        DataSequence firstDS = (DataSequence) partOfData.elementAt(0);
                        dailyRecords.addAll(firstDS);
                    }
                }
            }
        }
        return dailyRecords;
    }

    /** Export object data into a plot format plain text file (no compression).  * @param dssList The list of dss objects  * @param filename The file name to export data  */
    public static void writeData(Vector dssList, String fileName) throws Exception {
        FileOutputStream fos = new FileOutputStream(fileName);
        PrintWriter plt = new PrintWriter(fos);
        plt.println("#Spidr data output file");
        plt.println("#GMT time is used");
        plt.println("#");
        plt.println("#");
        plt.println("#--------------------------------------------------");
        for (int n = 0; n < dssList.size(); n++) {
            DataSequenceSet dss = (DataSequenceSet) dssList.get(n);
            for (int i = 0; i < dss.size(); i++) DSSExport.vec2stream(plt, (DataSequence) dss.elementAt(i));
        }
        plt.close();
    }

    /** Create AMIE run config file and add it to the export data zip  * @param dayId The day of the AMIE run  * @param dataName The file name with SPIDR indices export data  * @param fileName The command file name  */
    public static String writeAmieRunTemplate(int dayId, int sampling, String dataName, String fileName) throws Exception {
        if (fileName == null || dataName == null) return null;
        String exportPath = Settings.get("locations.localExportDir");
        String importPath = Settings.get("locations.httpExportDir");
        FileOutputStream fos = new FileOutputStream(exportPath + fileName);
        PrintWriter plt = new PrintWriter(fos);
        WDCDay day = new WDCDay(dayId);
        plt.println("#STARTIME");
        plt.println(day.getYear());
        plt.println(day.getMonth());
        plt.println(day.getDay());
        plt.println("00");
        plt.println("00");
        plt.println("00");
        plt.println("");
        plt.println("#NORTH");
        plt.println("T");
        plt.println("");
        plt.println("#DT");
        if (sampling > 0) {
            plt.println(sampling * 60);
            plt.println(sampling * 60);
        } else {
            plt.println("??");
            plt.println("??");
        }
        plt.println("");
        plt.println("#NTIMES");
        if (sampling > 0) plt.println((int) (24 * 60 / sampling)); else plt.println("??");
        plt.println("");
        plt.println("#OUTPUT");
        plt.println("b" + (dayId - (dayId / 1000000) * 1000000) + "                         output_file");
        plt.println("F                               output_magnetometers");
        plt.println("");
        plt.println("#BACKGROUND");
        plt.println("./amie_files/                   statistical model directory");
        plt.println("weimer");
        plt.println("ihp");
        plt.println("kroehl");
        plt.println("");
        plt.println("#MAGNETOMETER");
        plt.println("./mag12.final");
        plt.println("./master_psi.dat");
        plt.println("T");
        plt.println("T");
        plt.println("1.0");
        plt.println("1.0");
        plt.println("1.0");
        plt.println("");
        plt.println("#NGDC_INDICES");
        plt.println(dataName);
        if (sampling > 0) plt.println((float) (60 * sampling)); else plt.println("??");
        plt.println("");
        plt.println("#END");
        plt.close();
        return importPath + fileName;
    }

    /** Create AMIE run config file and add it to the export data zip  * @param dayId The day of the AMIE run  * @param dataName The file name with SPIDR indices export data  * @param fileName The command file name  */
    public static String writeAmieRunTemplateNew(HashMap hm, String fileName, String runId, String scriptName) throws Exception {
        if (runId == null || fileName == null || hm == null || scriptName == null) return null;
        String exportPath = Settings.get("locations.localExportDir");
        String importPath = Settings.get("locations.httpExportDir");
        FileOutputStream fos = new FileOutputStream(exportPath + fileName);
        PrintWriter plt = new PrintWriter(fos);
        plt.println("export ID=" + runId);
        int dayId = ((Integer) hm.get("dayIdFrom")).intValue();
        if (dayId <= 0) dayId = 19980501;
        WDCDay day = new WDCDay(dayId);
        plt.print("" + scriptName);
        plt.print(" -y=" + day.getYear());
        plt.print(" -m=" + day.getMonth());
        plt.print(" -d=" + day.getDay());
        int timeStep = ((Integer) hm.get("timestep")).intValue();
        if (timeStep <= 0) timeStep = 1;
        plt.print(" -dt=" + timeStep);
        String magnetometers = (String) hm.get("magnetometers");
        if (magnetometers != null) plt.print(" -" + magnetometers);
        String hemisphere = (String) hm.get("hemisphere");
        if (hemisphere != null) plt.print(" -" + hemisphere);
        String efield = (String) hm.get("efield");
        if (efield != null) plt.print(" -" + efield);
        String particles = (String) hm.get("particles");
        if (particles != null) plt.print(" -" + particles);
        String conductance = (String) hm.get("conductance");
        if (conductance != null) plt.print(" -" + conductance);
        String remote = (String) hm.get("remote");
        if (remote != null) plt.print(" -" + remote);
        int ncpu = ((Integer) hm.get("ncpu")).intValue();
        if (ncpu <= 0) ncpu = 1;
        plt.print(" -np=" + ncpu);
        String[] plot = (String[]) hm.get("plot");
        if (plot != null && plot.length > 0) {
            plt.print(" -plot=" + plot[0]);
            for (int i = 1; i < plot.length; i++) plt.print("," + plot[i]);
        }
        plt.println("");
        plt.close();
        return importPath + fileName;
    }

    /** Despike dailyData object (supposed Z geomagnetic field)  * @param dz The dailyData object  * @return  The despiked one  */
    public static Vector despike(Vector dataVector) throws Exception {
        if (dataVector == null || dataVector.size() != 3) {
            log.error("SWRDataBean.despike(): null or wrong size of the data vector");
            return null;
        }
        DailyData d1 = (DailyData) dataVector.elementAt(0);
        if (d1 == null) {
            log.error("SWRDataBean.despike(): null 1st component object");
            return null;
        }
        DailyData d2 = (DailyData) dataVector.elementAt(1);
        if (d2 == null) {
            log.error("SWRDataBean.despike(): null 2nd component object");
            return null;
        }
        DailyData dz = (DailyData) dataVector.elementAt(2);
        if (dz == null) {
            log.error("SWRDataBean.despike(): null z component object");
            return null;
        }
        log.debug("SWRDataBean.despike(): got data " + dz);
        SwrStation stn = (SwrStation) dz.getStation();
        if (stn == null) return null;
        log.debug("SWRDataBean.despike(): station code is " + stn.getStn());
        float lat = stn.getLat();
        if (lat == stn.BADVALUE) return null;
        log.debug("SWRDataBean.despike(): station lat is " + lat);
        DataDescription descr = dz.getDescription();
        if (descr == null || !"Z".equalsIgnoreCase(descr.getElement())) return null;
        DataDescription descr1 = d1.getDescription();
        DataDescription descr2 = d2.getDescription();
        float missval = descr.getMissingValue();
        float missval1 = descr1.getMissingValue();
        float missval2 = descr2.getMissingValue();
        log.debug("SWRDataBean.despike(): missing values are z: " + missval + " 1: " + missval1 + " 2: " + missval2);
        float[] data1 = d1.getData();
        float[] data2 = d2.getData();
        float[] data = dz.getData();
        if (data == null || data1 == null || data2 == null || data1.length != data.length || data2.length != data.length) {
            log.error("SWRDataBean.despike(): incosistent lengths of the components");
            return null;
        }
        float[] average = new float[data.length];
        float[] dbdt = new float[data.length];
        float[] despikedData1 = new float[data.length];
        float[] despikedData2 = new float[data.length];
        float[] despikedData = new float[data.length];
        average[0] = data[0];
        for (int i = 1; i < data.length; i++) {
            int count = 0;
            float sum = 0;
            for (int j = 0; j < AVERAGE_WINDOW && i - j >= 0; j++) {
                if (data[i - j] != missval) {
                    sum += data[i - j];
                    count++;
                }
            }
            if (count > 0) average[i] = sum / count; else average[i] = missval;
        }
        dbdt[0] = missval;
        for (int i = 1; i < data.length; i++) {
            float db = 0;
            float dt = 0;
            if (data[i - 1] != missval && data[i] != missval) {
                db = data[i] - data[i - 1];
                dt = 1 * 60;
            } else if ((i - 2) >= 0 && data[i - 2] != missval) {
                db = data[i] - data[i - 2];
                dt = 2 * 60;
            } else if ((i - 3) >= 0 && data[i - 3] != missval) {
                db = data[i] - data[i - 3];
                dt = 3 * 60;
            }
            if (dt > 0) dbdt[i] = Math.abs(db / dt); else dbdt[i] = missval;
        }
        float amplThreshold = 400;
        float dbdtThreshold = 2;
        if (Math.abs(lat) < 85) {
            amplThreshold = 400;
            dbdtThreshold = 2;
        } else if (Math.abs(lat) < 80) {
            amplThreshold = 600;
            dbdtThreshold = 4;
        } else if (Math.abs(lat) < 75) {
            amplThreshold = 1200;
            dbdtThreshold = 9;
        } else if (Math.abs(lat) < 70) {
            amplThreshold = 1800;
            dbdtThreshold = 10;
        } else if (Math.abs(lat) < 65) {
            amplThreshold = 1500;
            dbdtThreshold = 6;
        } else if (Math.abs(lat) < 60) {
            amplThreshold = 1300;
            dbdtThreshold = 4;
        } else if (Math.abs(lat) < 55) {
            amplThreshold = 500;
            dbdtThreshold = 3;
        } else if (Math.abs(lat) < 50) {
            amplThreshold = 300;
            dbdtThreshold = 1;
        } else if (Math.abs(lat) < 40) {
            amplThreshold = 300;
            dbdtThreshold = 1;
        }
        if (Math.abs(lat) < 30) {
            amplThreshold = 200;
            dbdtThreshold = 0.5f;
        }
        if (data[0] == missval || data1[0] == missval1 || data2[0] == missval2 || average[1] == missval || Math.abs(average[1] - data[0]) > amplThreshold) {
            despikedData[0] = missval;
            despikedData1[0] = missval1;
            despikedData2[0] = missval2;
        } else {
            despikedData[0] = data[0];
            despikedData1[0] = data1[0];
            despikedData2[0] = data2[0];
        }
        for (int i = 1; i < data.length; i++) {
            if (data[i] == missval || data1[i] == missval1 || data2[i] == missval2 || average[i] == missval || Math.abs(average[i] - data[i]) > amplThreshold || (dbdt[i] != missval && dbdt[i] > dbdtThreshold)) {
                despikedData[i] = missval;
                despikedData1[i] = missval1;
                despikedData2[i] = missval2;
            } else {
                despikedData[i] = data[i];
                despikedData1[i] = data1[i];
                despikedData2[i] = data2[i];
            }
        }
        DailyData d1prim = new DailyData(dz.getDayId(), despikedData1, descr1, stn);
        DailyData d2prim = new DailyData(dz.getDayId(), despikedData2, descr2, stn);
        DailyData dzprim = new DailyData(dz.getDayId(), despikedData, descr, stn);
        Vector resultVector = new Vector();
        resultVector.add(d1prim);
        resultVector.add(d2prim);
        resultVector.add(dzprim);
        return resultVector;
    }

    /** Rotate 3-component variations from geomagnetic DHZ to geographic XYZ,   *  with the synchronized missing values in the rotated components   *  @dataVector Input vector of 3 DailyData objects with EHZ or DHZ components   *  @return  A vector of 3 DailyData objects with rotated XYZ components   */
    public static Vector rotate(Vector dataVector) throws Exception {
        if (dataVector == null || dataVector.size() != 3) return null;
        LocalApi api = new LocalApi();
        SwrStation stn = null;
        String stnCode = null;
        String table = null;
        DailyData dx = null;
        DataDescription descrx = null;
        float[] datax = null;
        DailyData dy = null;
        DataDescription descry = null;
        float[] datay = null;
        DailyData dz = null;
        DataDescription descrz = null;
        float[] dataz = null;
        DailyData dd = null;
        DataDescription descrd = null;
        float[] datad = null;
        DailyData dh = null;
        DataDescription descrh = null;
        float[] datah = null;
        DailyData dzprim = null;
        DataDescription descrzprim = null;
        float[] datazprim = null;
        dz = (DailyData) dataVector.elementAt(2);
        if (dz == null) {
            log.error("SWRDataBean.rotate(): null Z component object");
            return null;
        }
        int dayId = dz.getDayId();
        dataz = dz.getData();
        if (dataz == null) {
            log.error("SWRDataBean.rotate(): null Z component time series");
            return null;
        } else datazprim = new float[dataz.length];
        descrz = dz.getDescription();
        if (descrz == null) {
            log.error("SWRDataBean.rotate(): null Z component description");
            return null;
        }
        stn = (SwrStation) dz.getStation();
        if (stn == null) {
            log.error("SWRDataBean.rotate(): null Z component station");
            return null;
        }
        stnCode = stn.getStn();
        float stnMissing = stn.BADVALUE;
        float declination = stn.getDeclination() * (float) Math.PI / 180;
        if (declination == stn.BADVALUE) {
            log.error("SWRDataBean.rotate(): unknown station declination");
            return null;
        }
        table = descrz.getTable();
        descrzprim = api.getDescription(table, "Z");
        dzprim = new DailyData(dayId, null, descrzprim, stn);
        float missval = descrz.getMissingValue();
        log.info("SWRDataBean.rotate(): rotating from geomagnetic to geographic station " + stnCode + " declination " + declination);
        dd = (DailyData) dataVector.elementAt(0);
        if (dd == null) {
            log.error("SWRDataBean.rotate(): null D component object");
            return null;
        }
        datad = dd.getData();
        if (datad == null) {
            log.error("SWRDataBean.rotate(): null D component time series");
            return null;
        }
        descrd = dd.getDescription();
        if (descrd == null) {
            log.error("SWRDataBean.rotate(): null D component description");
            return null;
        }
        String elemd = descrd.getElement();
        if (elemd == null || !"E".equalsIgnoreCase(elemd)) {
            log.error("SWRDataBean.rotate(): wrong element in D object");
            return null;
        }
        dh = (DailyData) dataVector.elementAt(1);
        if (dh == null) {
            log.error("SWRDataBean.rotate(): null H component object");
            return null;
        }
        datah = dh.getData();
        if (datah == null) {
            log.error("SWRDataBean.rotate(): null H component time series");
            return null;
        }
        descrh = dh.getDescription();
        if (descrh == null) {
            log.error("SWRDataBean.rotate(): null H component description");
            return null;
        }
        String elemh = descrh.getElement();
        if (elemh == null || !"H".equalsIgnoreCase(elemh)) {
            log.error("SWRDataBean.rotate(): wrong element in H object");
            return null;
        }
        descrx = api.getDescription(table, "X");
        dx = new DailyData(dayId, null, descrx, stn);
        datax = new float[dataz.length];
        descry = api.getDescription(table, "Y");
        dy = new DailyData(dayId, null, descry, stn);
        datay = new float[dataz.length];
        for (int i = 0; i < dataz.length; i++) {
            if (dataz[i] != missval && datax[i] != missval && datay[i] != missval) {
                datax[i] = datad[i] * (float) Math.cos(-declination) + datah[i] * (float) Math.sin(-declination);
                datay[i] = datah[i] * (float) Math.cos(-declination) - datad[i] * (float) Math.sin(-declination);
                datazprim[i] = dataz[i];
            } else {
                datax[i] = missval;
                datay[i] = missval;
                datazprim[i] = missval;
            }
        }
        dx.attachData(datax);
        dy.attachData(datay);
        dzprim.attachData(datazprim);
        Vector resultVector = new Vector();
        resultVector.add(dx);
        resultVector.add(dy);
        resultVector.add(dzprim);
        return resultVector;
    }

    /** Subtract  quietData from dailyData based on the station latitude   *  if lat > 50, simle mean subtraction   *  if lat <= 50, waveform subtraction   *  @param subData The dailyData object with the subtracted time series   *  @return  The despiked one   */
    public static DailyData subtract(DailyData dailyData, DailyData quietData) throws Exception {
        if (dailyData == null || quietData == null) return null;
        log.debug("SWRDataBean.subtract(): got daily data " + dailyData);
        log.debug("SWRDataBean.subtract(): got quiet data " + quietData);
        SwrStation dailyStn = (SwrStation) dailyData.getStation();
        SwrStation quietStn = (SwrStation) quietData.getStation();
        if (dailyStn == null || quietStn == null) return null;
        log.debug("SWRDataBean.subtract(): daily station code is " + dailyStn.getStn());
        log.debug("SWRDataBean.subtract(): quiet station code is " + quietStn.getStn());
        if (!dailyStn.getStn().equalsIgnoreCase(quietStn.getStn())) return null;
        float lat = dailyStn.getLat();
        if (lat == dailyStn.BADVALUE) return null;
        log.debug("SWRDataBean.subtract(): station lat is " + lat);
        DataDescription dailyDescr = dailyData.getDescription();
        DataDescription quietDescr = quietData.getDescription();
        if (dailyDescr == null || quietDescr == null) return null;
        log.debug("SWRDataBean.subtract(): daily element is " + dailyDescr.getElement());
        log.debug("SWRDataBean.subtract(): quiet element is " + quietDescr.getElement());
        if (!dailyDescr.getElement().equalsIgnoreCase(quietDescr.getElement())) return null;
        float missval = dailyDescr.getMissingValue();
        log.debug("SWRDataBean.subtract(): missing value is " + missval);
        float[] ddata = dailyData.getData();
        float[] qdata = quietData.getData();
        if (ddata == null || qdata == null || ddata.length != qdata.length) {
            log.debug("SWRDataBean.subtract(): different data lengths in daily and quiet elements " + ddata.length + " compared with " + qdata.length);
            return null;
        }
        float[] average = new float[qdata.length];
        float[] result = new float[ddata.length];
        float mean = 0;
        int mcount = 0;
        average[0] = qdata[0];
        for (int i = 1; i < qdata.length; i++) {
            if (qdata[i] != missval) {
                mean += qdata[i];
                mcount++;
            }
            int count = 0;
            float sum = 0;
            for (int j = 0; j < AVERAGE_WINDOW && i - j >= 0; j++) {
                if (qdata[i - j] != missval) {
                    sum += qdata[i - j];
                    count++;
                }
            }
            if (count > 0) average[i] = sum / count; else average[i] = missval;
        }
        if (mcount > 0) mean = mean / mcount; else mean = missval;
        if (mean == missval) {
            log.error("SWRDataBean.subtract(): quiet data all missing");
            return null;
        }
        int goodCount = 0;
        if (Math.abs(lat) > 50) {
            for (int i = 0; i < ddata.length; i++) {
                if (Float.isNaN(ddata[i]) || ddata[i] == missval || average[i] == missval) result[i] = missval; else {
                    result[i] = ddata[i] - average[i];
                    goodCount++;
                }
            }
        } else {
            for (int i = 0; i < ddata.length; i++) {
                if (Float.isNaN(ddata[i]) || ddata[i] == missval) result[i] = missval; else {
                    result[i] = ddata[i] - mean;
                    goodCount++;
                }
            }
        }
        log.info("SWRDataBean.subtract(): made " + goodCount + " real subtractions");
        DailyData subData = new DailyData(dailyData.getDayId(), result, dailyDescr, dailyStn);
        return subData;
    }

    /** Creates input files for AMIE runs from the vector of quiet day subtracted   *  daily observations (one DailyData object per station per component)   *  @param subRecords The Vector of DailyData objects   *  @timestep sampling interval in minutes   *  @return URL to the exported data file   */
    public static String exportGeomagData(Vector indexRecords, Vector subRecords, Vector quietRecords, String fileName) throws Exception {
        if (indexRecords == null || subRecords == null || subRecords.size() == 0 || quietRecords == null || fileName == null) return null;
        WDCDay day = null;
        String sectionName = null;
        String sectionPrefix = null;
        String exportPath = Settings.get("locations.localExportDir");
        String importPath = Settings.get("locations.httpExportDir");
        if (indexRecords.size() > 0 && subRecords.size() > 0 && quietRecords.size() > 0) {
            FileOutputStream fos = new FileOutputStream(exportPath + fileName);
            ZipOutputStream zos = new ZipOutputStream(fos);
            PrintWriter plt = null;
            DateInterval dateInt = findDateInterval(subRecords);
            log.info("SWRDataBean.exportGeomagData(): dateIntervalDescr=\"" + dateInt.toNiceString() + "\";");
            DataSequenceSet dss = null;
            if (indexRecords != null) dss = UpdateMetadata.dailyRecordsToDSS(indexRecords);
            day = dateInt.getDateFrom();
            sectionPrefix = "" + day.getDayId();
            sectionName = sectionPrefix + "indices";
            zos.putNextEntry(new ZipEntry(sectionName + ".amie"));
            plt = new PrintWriter(zos);
            log.info("SWRDataBean.exportGeomagData(): new archive section " + sectionName);
            plt.println("#Spidr data output file");
            plt.println("#GMT time is used");
            plt.println("#");
            plt.println("#");
            plt.println("#--------------------------------------------------");
            for (int i = 0; i < dss.size(); i++) DSSExport.vec2stream(plt, (DataSequence) dss.elementAt(i));
            plt.flush();
            zos.closeEntry();
            Enumeration enumer = subRecords.elements();
            while (enumer.hasMoreElements()) {
                DailyData dailyd = (DailyData) enumer.nextElement();
                if (dailyd != null) {
                    SwrStation stn = (SwrStation) dailyd.getStation();
                    String stnCode = null;
                    String stnType = null;
                    if (stn != null) {
                        stnCode = stn.getStn();
                        stnType = stn.getDataType();
                    }
                    if (stnCode != null) {
                        float declination = stn.getDeclination();
                        String element = dailyd.getDescription().getElement();
                        if ("Z".equalsIgnoreCase(element)) {
                            log.info("SWRDataBean.exportGeomagData(): got z component for station " + stnCode + " type " + stnType);
                            Enumeration qenum = quietRecords.elements();
                            boolean qdayOk = true;
                            int qnumData = -99999;
                            float qstd1 = -99999, qstd2 = -99999, qstd3 = -99999;
                            float qmean1 = -99999, qmean2 = -99999, qmean3 = -99999;
                            while (qenum.hasMoreElements()) {
                                DailyData qdailyd = (DailyData) qenum.nextElement();
                                if (qdailyd != null) {
                                    SwrStation qstn = (SwrStation) qdailyd.getStation();
                                    String qstnCode = null;
                                    String qstnType = null;
                                    if (qstn != null) {
                                        qstnCode = qstn.getStn();
                                        qstnType = qstn.getDataType();
                                    }
                                    if (qstnCode != null && qstnCode.equalsIgnoreCase(stnCode)) {
                                        String qelement = qdailyd.getDescription().getElement();
                                        if ("Z".equalsIgnoreCase(qelement)) {
                                            log.info("SWRDataBean.exportGeomagData(): got z quiet day component for station " + stnCode);
                                            DailyData dz = qdailyd;
                                            DailyData dx = null;
                                            DailyData dy = null;
                                            DailyData dd = null;
                                            DailyData dh = null;
                                            DailyData de = null;
                                            Enumeration enumxyz = quietRecords.elements();
                                            while (enumxyz.hasMoreElements()) {
                                                DailyData dtmp = (DailyData) enumxyz.nextElement();
                                                Station stnTmp = dtmp.getStation();
                                                String stnCodeTmp = null;
                                                if (stnTmp != null && stnCode.equals(stnTmp.getStn())) {
                                                    String elementTmp = dtmp.getDescription().getElement();
                                                    log.debug("SWRDataBean.exportGeomagData(): found quiet component " + elementTmp);
                                                    if ("X".equalsIgnoreCase(elementTmp)) dx = dtmp; else if ("Y".equalsIgnoreCase(elementTmp)) dy = dtmp; else if ("D".equalsIgnoreCase(elementTmp)) if ("EHZ".equalsIgnoreCase(qstnType)) {
                                                        de = dtmp;
                                                        log.info("SWRDataBean.exportGeomagData(): quiet D component in fact is E in nT");
                                                    } else dd = dtmp; else if ("H".equalsIgnoreCase(elementTmp)) dh = dtmp; else if ("E".equalsIgnoreCase(elementTmp)) de = dtmp;
                                                }
                                            }
                                            boolean qxyz = (dx != null && dy != null && dz != null);
                                            boolean qdhz = (dd != null && dh != null && dz != null);
                                            boolean qehz = (de != null && dh != null && dz != null);
                                            int sampling = dz.getSampling();
                                            float missval = dz.getDescription().getMissingValue();
                                            float[] data1 = null;
                                            float[] data2 = null;
                                            float[] data3 = dz.getData();
                                            if (qxyz) {
                                                data1 = dx.getData();
                                                data2 = dy.getData();
                                                data3 = dz.getData();
                                            } else if (qdhz) {
                                                data1 = dd.getData();
                                                data2 = dh.getData();
                                                data3 = dz.getData();
                                            } else if (qehz) {
                                                data1 = de.getData();
                                                data2 = dh.getData();
                                                data3 = dz.getData();
                                            } else {
                                                log.info("SWRDataBean.exportGeomagData(): unknown quiet day station type - station was skipped");
                                                qdayOk = false;
                                                break;
                                            }
                                            if (data1 == null || data2 == null || data3 == null) {
                                                log.info("SWRDataBean.exportGeomagData(): one of data segments in quiet day data is null - station was skipped");
                                                qdayOk = false;
                                                break;
                                            }
                                            qnumData = data3.length;
                                            qmean1 = findMedian(data1, missval);
                                            qmean2 = findMedian(data2, missval);
                                            qmean3 = findMedian(data3, missval);
                                            if (qmean1 == missval || qmean2 == missval || qmean3 == missval) {
                                                log.info("SWRDataBean.exportGeomagData(): one of data segments in quiet day data is empty - station was skipped");
                                                qdayOk = false;
                                                break;
                                            }
                                            qstd1 = findStd(data1, missval);
                                            qstd2 = findStd(data2, missval);
                                            qstd3 = findStd(data3, missval);
                                        }
                                    }
                                }
                            }
                            if (!qdayOk) continue;
                            DailyData dz = dailyd;
                            DailyData dx = null;
                            DailyData dy = null;
                            DailyData dd = null;
                            DailyData dh = null;
                            DailyData de = null;
                            Enumeration enumxyz = subRecords.elements();
                            while (enumxyz.hasMoreElements()) {
                                DailyData dtmp = (DailyData) enumxyz.nextElement();
                                Station stnTmp = dtmp.getStation();
                                String stnCodeTmp = null;
                                if (stnTmp != null && stnCode.equals(stnTmp.getStn())) {
                                    String elementTmp = dtmp.getDescription().getElement();
                                    log.debug("SWRDataBean.exportGeomagData(): found component " + elementTmp);
                                    if ("X".equalsIgnoreCase(elementTmp)) dx = dtmp; else if ("Y".equalsIgnoreCase(elementTmp)) dy = dtmp; else if ("D".equalsIgnoreCase(elementTmp)) if ("EHZ".equalsIgnoreCase(stnType)) {
                                        de = dtmp;
                                        log.info("SWRDataBean.exportGeomagData(): daily D component in fact is E in nT");
                                    } else dd = dtmp; else if ("H".equalsIgnoreCase(elementTmp)) dh = dtmp; else if ("E".equalsIgnoreCase(elementTmp)) de = dtmp;
                                }
                            }
                            boolean xyz = (dx != null && dy != null && dz != null);
                            boolean dhz = (dd != null && dh != null && dz != null);
                            boolean ehz = (de != null && dh != null && dz != null);
                            TimeZone tz = new SimpleTimeZone(0, "GMT");
                            Calendar utc = new GregorianCalendar(tz);
                            day = new WDCDay(dz.getDayId());
                            utc.set(day.getYear(), day.getMonth() - 1, day.getDay(), 0, 0, 0);
                            utc.set(Calendar.MILLISECOND, 0);
                            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
                            df.setTimeZone(tz);
                            df.setCalendar(utc);
                            int sampling = dz.getSampling();
                            float missval = dz.getDescription().getMissingValue();
                            float[] data1 = null;
                            float[] data2 = null;
                            float[] data3 = null;
                            if (xyz) {
                                data1 = dx.getData();
                                data2 = dy.getData();
                                data3 = dz.getData();
                            } else if (dhz) {
                                data1 = dd.getData();
                                data2 = dh.getData();
                                data3 = dz.getData();
                            } else if (ehz) {
                                data1 = de.getData();
                                data2 = dh.getData();
                                data3 = dz.getData();
                            } else {
                                log.info("SWRDataBean.exportGeomagData(): unknown daily data station type - station was skipped");
                                continue;
                            }
                            int numData = 0;
                            if (data3 == null) {
                                log.info("SWRDataBean.exportGeomagData(): data3 segment in daily data is null");
                                if (data2 == null) {
                                    log.info("SWRDataBean.exportGeomagData(): data2 segment in daily data is null");
                                    if (data1 == null) {
                                        log.info("SWRDataBean.exportGeomagData(): data1 segment in daily data is null - station was skipped");
                                        continue;
                                    } else {
                                        numData = data1.length;
                                    }
                                } else {
                                    numData = data2.length;
                                }
                            } else {
                                numData = data3.length;
                            }
                            float mean1 = findMean(data1, missval);
                            float mean2 = findMean(data2, missval);
                            float mean3 = findMean(data3, missval);
                            if (mean1 == missval || mean2 == missval || mean3 == missval) {
                                log.info("SWRDataBean.exportGeomagData(): one of data segments in daily data is empty");
                            }
                            float std1 = findStd(data1, missval);
                            float std2 = findStd(data2, missval);
                            float std3 = findStd(data3, missval);
                            sectionName = sectionPrefix + stnCode;
                            zos.putNextEntry(new ZipEntry(sectionName + ".amie"));
                            plt = new PrintWriter(zos);
                            log.debug("SWRDataBean.exportGeomagData(): new archive section " + sectionName);
                            if (xyz) {
                                plt.println("X " + qnumData + " " + qmean1 + " " + qstd1);
                                plt.println("Y " + qnumData + " " + qmean2 + " " + qstd2);
                                plt.println("Z " + qnumData + " " + qmean3 + " " + qstd3);
                            } else if (dhz) {
                                plt.println("D " + qnumData + " " + qmean1 + " " + qstd1);
                                plt.println("H " + qnumData + " " + qmean2 + " " + qstd2);
                                plt.println("Z " + qnumData + " " + qmean3 + " " + qstd3);
                            } else if (ehz) {
                                plt.println("E " + qnumData + " " + qmean1 + " " + qstd1);
                                plt.println("H " + qnumData + " " + qmean2 + " " + qstd2);
                                plt.println("Z " + qnumData + " " + qmean3 + " " + qstd3);
                            }
                            for (int k = 0; k < numData; k++) {
                                String dstr = df.format(utc.getTime());
                                plt.print(dstr.substring(2, dstr.length()));
                                plt.print(stnCode + " ");
                                if (xyz) plt.print("XYZ "); else if (dhz) plt.print("DHZ "); else if (ehz) plt.print("EHZ ");
                                if (data1 != null && !Float.isNaN(data1[k]) && data1[k] != missval) plt.print(data1[k] + " "); else plt.print("-99999 ");
                                if (data2 != null && !Float.isNaN(data2[k]) && data2[k] != missval) plt.print(data2[k] + " "); else plt.print("-99999 ");
                                if (data3 != null && !Float.isNaN(data3[k]) && data3[k] != missval) plt.print(data3[k] + " "); else plt.print("-99999 ");
                                if (!Float.isNaN(std1) && std1 != missval) plt.print(std1 + " "); else plt.print("-99999 ");
                                if (!Float.isNaN(std2) && std2 != missval) plt.print(std2 + " "); else plt.print("-99999 ");
                                if (!Float.isNaN(std3) && std3 != missval) plt.print(std3 + " "); else plt.print("-99999 ");
                                plt.println("");
                                utc.add(Calendar.MINUTE, sampling);
                            }
                            plt.flush();
                            zos.closeEntry();
                        }
                    }
                }
            }
            zos.finish();
        }
        return importPath + fileName;
    }

    /** Creates input files for AMIE runs from the vector of quiet day subtracted   *  daily observations (one DailyData object per station per component)   *  @param subRecords The Vector of DailyData objects   *  @return URL to the exported data file   */
    public static String exportGeomagData(Vector subRecords, String fileName) throws Exception {
        if (subRecords == null || subRecords.size() == 0 || fileName == null) return null;
        WDCDay day = null;
        String sectionName = null;
        String sectionPrefix = null;
        String exportPath = Settings.get("locations.localExportDir");
        String importPath = Settings.get("locations.httpExportDir");
        if (subRecords.size() > 0) {
            FileOutputStream fos = new FileOutputStream(exportPath + fileName);
            ZipOutputStream zos = new ZipOutputStream(fos);
            PrintWriter plt = null;
            DateInterval dateInt = findDateInterval(subRecords);
            log.info("SWRDataBean.exportGeomagData(): dateIntervalDescr=\"" + dateInt.toNiceString() + "\";");
            sectionPrefix = "" + dateInt.getDateFrom().getDayId();
            Enumeration enumer = subRecords.elements();
            while (enumer.hasMoreElements()) {
                DailyData dailyd = (DailyData) enumer.nextElement();
                if (dailyd != null) {
                    SwrStation stn = (SwrStation) dailyd.getStation();
                    String stnCode = null;
                    String stnType = null;
                    if (stn != null) {
                        stnCode = stn.getStn();
                        stnType = stn.getDataType();
                    }
                    if (stnCode != null) {
                        float declination = stn.getDeclination();
                        String element = dailyd.getDescription().getElement();
                        if ("Z".equalsIgnoreCase(element)) {
                            log.info("SWRDataBean.exportGeomagData(): got z component for station " + stnCode + " type " + stnType);
                            int qnumData = -99999;
                            float qstd1 = -99999, qstd2 = -99999, qstd3 = -99999;
                            float qmean1 = -99999, qmean2 = -99999, qmean3 = -99999;
                            DailyData dz = dailyd;
                            DailyData dx = null;
                            DailyData dy = null;
                            DailyData dd = null;
                            DailyData dh = null;
                            DailyData de = null;
                            Enumeration enumxyz = subRecords.elements();
                            while (enumxyz.hasMoreElements()) {
                                DailyData dtmp = (DailyData) enumxyz.nextElement();
                                Station stnTmp = dtmp.getStation();
                                String stnCodeTmp = null;
                                if (stnTmp != null && stnCode.equals(stnTmp.getStn())) {
                                    String elementTmp = dtmp.getDescription().getElement();
                                    log.debug("SWRDataBean.exportGeomagData(): found component " + elementTmp);
                                    if ("X".equalsIgnoreCase(elementTmp)) dx = dtmp; else if ("Y".equalsIgnoreCase(elementTmp)) dy = dtmp; else if ("D".equalsIgnoreCase(elementTmp)) if ("EHZ".equalsIgnoreCase(stnType)) {
                                        de = dtmp;
                                        log.info("SWRDataBean.exportGeomagData(): daily D component in fact is E in nT");
                                    } else dd = dtmp; else if ("H".equalsIgnoreCase(elementTmp)) dh = dtmp; else if ("E".equalsIgnoreCase(elementTmp)) de = dtmp;
                                }
                            }
                            boolean xyz = (dx != null && dy != null && dz != null);
                            boolean dhz = (dd != null && dh != null && dz != null);
                            boolean ehz = (de != null && dh != null && dz != null);
                            TimeZone tz = new SimpleTimeZone(0, "GMT");
                            Calendar utc = new GregorianCalendar(tz);
                            day = new WDCDay(dz.getDayId());
                            utc.set(day.getYear(), day.getMonth() - 1, day.getDay(), 0, 0, 0);
                            utc.set(Calendar.MILLISECOND, 0);
                            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
                            df.setTimeZone(tz);
                            df.setCalendar(utc);
                            int sampling = dz.getSampling();
                            float missval = dz.getDescription().getMissingValue();
                            float[] data1 = null;
                            float[] data2 = null;
                            float[] data3 = null;
                            if (xyz) {
                                data1 = dx.getData();
                                data2 = dy.getData();
                                data3 = dz.getData();
                            } else if (dhz) {
                                data1 = dd.getData();
                                data2 = dh.getData();
                                data3 = dz.getData();
                            } else if (ehz) {
                                data1 = de.getData();
                                data2 = dh.getData();
                                data3 = dz.getData();
                            } else {
                                log.info("SWRDataBean.exportGeomagData(): unknown daily data station type - station was skipped");
                                continue;
                            }
                            int numData = 0;
                            if (data3 == null) {
                                log.info("SWRDataBean.exportGeomagData(): data3 segment in daily data is null");
                                if (data2 == null) {
                                    log.info("SWRDataBean.exportGeomagData(): data2 segment in daily data is null");
                                    if (data1 == null) {
                                        log.info("SWRDataBean.exportGeomagData(): data1 segment in daily data is null - station was skipped");
                                        continue;
                                    } else {
                                        numData = data1.length;
                                    }
                                } else {
                                    numData = data2.length;
                                }
                            } else {
                                numData = data3.length;
                            }
                            float mean1 = findMean(data1, missval);
                            float mean2 = findMean(data2, missval);
                            float mean3 = findMean(data3, missval);
                            if (mean1 == missval || mean2 == missval || mean3 == missval) {
                                log.info("SWRDataBean.exportGeomagData(): one of data segments in daily data is empty");
                            }
                            float std1 = findStd(data1, missval);
                            float std2 = findStd(data2, missval);
                            float std3 = findStd(data3, missval);
                            sectionName = sectionPrefix + stnCode;
                            zos.putNextEntry(new ZipEntry(sectionName + ".amie"));
                            plt = new PrintWriter(zos);
                            log.debug("SWRDataBean.exportGeomagData(): new archive section " + sectionName);
                            for (int k = 0; k < numData; k++) {
                                String dstr = df.format(utc.getTime());
                                plt.print(dstr.substring(2, dstr.length()));
                                plt.print(stnCode + " ");
                                if (xyz) plt.print("XYZ"); else if (dhz) plt.print("DHZ"); else if (ehz) plt.print("EHZ");
                                spidr.swr.Format format = new spidr.swr.Format("%10.2f");
                                if (data1 != null && !Float.isNaN(data1[k]) && data1[k] != missval) plt.print(format.form(data1[k])); else plt.print(format.form(-99999f));
                                if (data2 != null && !Float.isNaN(data2[k]) && data2[k] != missval) plt.print(format.form(data2[k])); else plt.print(format.form(-99999f));
                                if (data3 != null && !Float.isNaN(data3[k]) && data3[k] != missval) plt.print(format.form(data3[k])); else plt.print(format.form(-99999f));
                                plt.println("");
                                utc.add(Calendar.MINUTE, sampling);
                            }
                            plt.flush();
                            zos.closeEntry();
                        }
                    }
                }
            }
            zos.finish();
        }
        return importPath + fileName;
    }

    /** Creates input files for KRM runs from the vector of quiet day subtracted   *  daily observations (one DailyData object per station per component)   *  @param subRecords The Vector of DailyData objects   *  @timestep sampling interval in minutes   *  @return URL to the exported data file   */
    public static String exportGeomagDataKrm(Vector subRecords, String fileName) throws Exception {
        if (subRecords == null || subRecords.size() == 0 || fileName == null) return null;
        WDCDay day = null;
        String sectionName = null;
        String exportPath = Settings.get("locations.localExportDir");
        String importPath = Settings.get("locations.httpExportDir");
        DateInterval dateInt = findDateInterval(subRecords);
        log.debug("SWRDataBean.exportGeomagDataKrm(): dateIntervalDescr=\"" + dateInt.toNiceString() + "\";");
        int sampling = 0;
        int numData = 0;
        HashMap stnHash = new HashMap();
        HashMap xyzHash = new HashMap();
        Enumeration enumer = subRecords.elements();
        while (enumer.hasMoreElements()) {
            DailyData dailyd = (DailyData) enumer.nextElement();
            if (dailyd != null) {
                SwrStation stn = (SwrStation) dailyd.getStation();
                String stnCode = null;
                String stnType = null;
                if (stn != null) {
                    stnCode = stn.getStn();
                    if (stnCode != null) {
                        String element = dailyd.getDescription().getElement();
                        if ("Z".equalsIgnoreCase(element)) {
                            log.debug("SWRDataBean.exportGeomagDataKrm(): got z component for station " + stnCode + " type " + stnType);
                            DailyData dz = dailyd;
                            DailyData dx = null;
                            DailyData dy = null;
                            int sampTmp = dz.getSampling();
                            if (sampling == 0) {
                                log.debug("SWRDataBean.exportGeomagDataKrm(): data sampling is " + sampTmp);
                                sampling = sampTmp;
                            } else if (sampling != sampTmp) {
                                log.info("SWRDataBean.exportGeomagDataKrm(): z component has different sampling " + sampTmp);
                                continue;
                            }
                            Enumeration enumxyz = subRecords.elements();
                            while (enumxyz.hasMoreElements()) {
                                DailyData dtmp = (DailyData) enumxyz.nextElement();
                                Station stnTmp = dtmp.getStation();
                                String stnCodeTmp = null;
                                if (stnTmp != null && stnCode.equals(stnTmp.getStn())) {
                                    String elementTmp = dtmp.getDescription().getElement();
                                    log.debug("SWRDataBean.exportGeomagDataKrm(): found component " + elementTmp);
                                    if ("X".equalsIgnoreCase(elementTmp)) dx = dtmp; else if ("Y".equalsIgnoreCase(elementTmp)) dy = dtmp;
                                }
                            }
                            boolean xyz = (dx != null && dy != null && dz != null);
                            if (xyz) {
                                float[] data1 = dx.getData();
                                float[] data2 = dy.getData();
                                float[] data3 = dz.getData();
                                int numDataTmp = data3.length;
                                if (numData == 0) {
                                    log.info("SWRDataBean.exportGeomagDataKrm(): data length is " + numDataTmp);
                                    numData = numDataTmp;
                                } else if (data1.length != numData) {
                                    log.info("SWRDataBean.exportGeomagDataKrm(): x component has different length " + data1.length);
                                    continue;
                                } else if (data2.length != numData) {
                                    log.info("SWRDataBean.exportGeomagDataKrm(): y component has different length " + data2.length);
                                    continue;
                                } else if (data3.length != numData) {
                                    log.info("SWRDataBean.exportGeomagDataKrm(): z component has different length " + data3.length);
                                    continue;
                                }
                                log.info("SWRDataBean.exportGeomagDataKrm(): add station " + stnCode);
                                stnHash.put(stnCode, stn);
                                xyzHash.put(stnCode + ".x", dx);
                                xyzHash.put(stnCode + ".y", dy);
                                xyzHash.put(stnCode + ".z", dz);
                            }
                        }
                    }
                }
            }
        }
        FileOutputStream fos = new FileOutputStream(exportPath + fileName);
        ZipOutputStream zos = new ZipOutputStream(fos);
        PrintWriter plt = null;
        TimeZone tz = new SimpleTimeZone(0, "GMT");
        Calendar utc = new GregorianCalendar(tz);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        df.setTimeZone(tz);
        df.setCalendar(utc);
        day = dateInt.getDateFrom();
        utc.set(day.getYear(), day.getMonth() - 1, day.getDay(), 0, 0, 0);
        utc.set(Calendar.MILLISECOND, 0);
        Set stnKeys = stnHash.keySet();
        spidr.swr.Format dataFormat = new spidr.swr.Format("%7.2f");
        spidr.swr.Format numberFormat = new spidr.swr.Format("%3d");
        spidr.swr.Format latlonFormat = new spidr.swr.Format("%7.2f");
        for (int k = 0; k < numData; k++) {
            String dstr = df.format(utc.getTime());
            sectionName = "N" + dstr;
            zos.putNextEntry(new ZipEntry(sectionName));
            plt = new PrintWriter(zos);
            plt.println(dstr);
            Iterator iter1 = stnKeys.iterator();
            while (iter1.hasNext()) {
                String stnCode = (String) iter1.next();
                DailyData dx = (DailyData) xyzHash.get(stnCode + ".x");
                DailyData dy = (DailyData) xyzHash.get(stnCode + ".y");
                DailyData dz = (DailyData) xyzHash.get(stnCode + ".z");
                float missval = dz.getDescription().getMissingValue();
                float[] data1 = dx.getData();
                float[] data2 = dy.getData();
                float datax = (Float.isNaN(data1[k]) || data1[k] == missval) ? 9999 : data1[k];
                float datay = (Float.isNaN(data2[k]) || data2[k] == missval) ? 9999 : data2[k];
                plt.println(" " + dataFormat.form(datax) + " " + dataFormat.form(datay));
            }
            plt.flush();
            zos.closeEntry();
            sectionName = "S" + dstr;
            zos.putNextEntry(new ZipEntry(sectionName));
            plt = new PrintWriter(zos);
            Iterator iter2 = stnKeys.iterator();
            int stnNo = 1;
            while (iter2.hasNext()) {
                String stnCode = (String) iter2.next();
                SwrStation stn = (SwrStation) stnHash.get(stnCode);
                float lat = stn.getLat();
                float colat = (lat != Station.BADVALUE) ? 90 - lat : lat;
                float lon = stn.getLon();
                if (lon < 0) lon = lon + 360;
                float mlat = stn.getMaglat();
                float comlat = (mlat != Station.BADVALUE) ? 90 - mlat : mlat;
                float mlon = stn.getMaglon();
                if (mlon < 0) mlon = mlon + 360;
                plt.println("" + numberFormat.form(stnNo++) + "  " + stnCode + "                    " + latlonFormat.form(lon) + "     " + latlonFormat.form(colat) + "     " + latlonFormat.form(mlon) + "     " + latlonFormat.form(comlat));
            }
            plt.flush();
            zos.closeEntry();
            utc.add(Calendar.MINUTE, sampling);
        }
        zos.finish();
        return importPath + fileName;
    }

    static float findMean(float[] data, float missval) {
        float mean = 0;
        int mcount = 0;
        if (data == null) return missval;
        int numData = data.length;
        for (int i = 0; i < numData; i++) {
            if (!Float.isNaN(data[i]) && data[i] != missval) {
                mean += data[i];
                mcount++;
            }
        }
        if (mcount > 1) mean = mean / mcount; else mean = missval;
        return mean;
    }

    static float findStd(float[] data, float missval) {
        float std = 0;
        float mean = 0;
        int mcount = 0;
        if (data == null) return std;
        int numData = data.length;
        for (int i = 0; i < numData; i++) {
            if (!Float.isNaN(data[i]) && data[i] != missval) {
                mean += data[i];
                mcount++;
            }
        }
        if (mcount > 1) {
            mean = mean / mcount;
            for (int i = 0; i < numData; i++) {
                if (!Float.isNaN(data[i]) && data[i] != missval) std += (data[i] - mean) * (data[i] - mean);
            }
            std = (float) Math.sqrt((double) std / (mcount - 1));
        } else std = missval;
        return std;
    }

    static float findMedian(float[] data, float missval) {
        float median = missval;
        int mcount = 0;
        if (data == null) return median;
        int numData = data.length;
        float datatmp[] = new float[numData];
        for (int i = 0; i < numData; i++) {
            if (!Float.isNaN(data[i]) && data[i] != missval) {
                datatmp[mcount] = data[i];
                mcount++;
            }
        }
        if (mcount > 1) {
            for (int i = 0; i < mcount - 1; i++) for (int j = mcount - 1; j >= i + 1; j--) if (datatmp[j] > datatmp[j - 1]) {
                float t = datatmp[j];
                datatmp[j] = datatmp[j - 1];
                datatmp[j - 1] = t;
            }
            median = (datatmp[(mcount - 1) / 2] + datatmp[mcount / 2]) / 2;
        }
        return median;
    }
}
