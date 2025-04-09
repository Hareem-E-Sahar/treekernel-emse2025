package combinereport.report;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import property.Property;
import User.Uservalidation;
import control.Control;
import utility.ConvertToLower;
import utility.MessageGenerator;
import utility.PeriodDate;
import database.RecordSearch;
import dbmanager.Column;
import dbmanager.DBManager;
import dbmanager.GetIdName;

public class Schedule implements ReportInterface {

    String object;

    DBManager database;

    Hashtable<Object, Object> scheduletable = new Hashtable<Object, Object>();

    GetIdName gid;

    String startdate = "startdate";

    String enddate = "enddate";

    String priority = "priority";

    String currentdate = "currentdate";

    String pattern = "yyyy-MM-dd";

    String period = "period";

    String DAY = "day";

    String reportname;

    String value;

    String process;

    Hashtable<Object, Object> Reportmap = new Hashtable<Object, Object>();

    public void addReport() {
        combinereport.condition.typereport.Schedule autoupdate = new combinereport.condition.typereport.Schedule();
        autoupdate.setData("", "", database, Reportmap);
        String path = autoupdate.getPath(reportname);
        File file = new File(path);
        if (!file.exists() || file.length() == 0) process = "mod";
        try {
            PeriodDate perioddate = new PeriodDate(database);
            SimpleDateFormat sdm = new SimpleDateFormat(this.pattern);
            String date = sdm.format(new Date());
            Date d = sdm.parse(date);
            date = sdm.format(d);
            period = gid.getId(scheduletable.get(period).toString());
            if (scheduletable.containsKey(startdate)) {
                startdate = scheduletable.get(startdate).toString();
                currentdate = scheduletable.get(currentdate).toString();
                String nextstartdate = perioddate.getNextDate(period, startdate);
                int i = perioddate.compareDate(currentdate, nextstartdate);
                int j = perioddate.compareDate(date, startdate);
                currentdate = "currentdate";
                if (i > 0 || j > 0) {
                    String nextdate;
                    String user = "systemuser";
                    Uservalidation uservalidation = new Uservalidation(database);
                    String password = uservalidation.getPasword(user);
                    MessageGenerator mg = new MessageGenerator(database);
                    Hashtable<Object, Object> table = new Hashtable<Object, Object>();
                    table.put("startdate", startdate.trim());
                    String addrequest = mg.messagegerat("property_details", table, "add");
                    table.put("startdate", nextstartdate.trim());
                    String modrequest = mg.messagegerat("property_details", table, "mod");
                    String request = "11 property_details mod*" + gid.getItem(object) + modrequest + addrequest + "#" + password;
                    Control control = new Control(user, request, database);
                    control.messageProcessing();
                    control.requestProcess();
                }
            }
            if (scheduletable.containsKey(enddate)) {
                enddate = scheduletable.get(enddate).toString();
                int i = perioddate.compareDate(enddate, date);
                if (i < 0) {
                    Reportmap.clear();
                    scheduletable.clear();
                    database.getUpdate("update report_master set status=5 where tid='" + value + "' and report_name='" + reportname + "'");
                }
            }
            if (scheduletable.containsKey(currentdate)) {
                currentdate = scheduletable.get(currentdate).toString();
                String nextdate = perioddate.getNextDate(period, startdate);
                int i = perioddate.compareDate(nextdate, currentdate);
                if (i > 0) {
                    String user = "systemuser";
                    Uservalidation uservalidation = new Uservalidation(database);
                    String password = uservalidation.getPasword(user);
                    MessageGenerator mg = new MessageGenerator(database);
                    Hashtable<Object, Object> table = new Hashtable<Object, Object>();
                    table.put("currentdate", currentdate);
                    String addrequest = mg.messagegerat("property_details", table, "add");
                    table.put("currentdate", nextdate);
                    String modrequest = mg.messagegerat("property_details", table, "mod");
                    String request = "11 property_details mod*" + gid.getItem(object) + modrequest + addrequest + "#" + password;
                    Control control = new Control(user, request, database);
                    control.messageProcessing();
                    control.requestProcess();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] getBuffer() {
        return null;
    }

    public String getKey() {
        return null;
    }

    public Hashtable<Object, Object> getReportMap() {
        return Reportmap;
    }

    public String getReportName() {
        return null;
    }

    public void getResult() {
    }

    public String gettypeofreport() {
        return "schedule";
    }

    public void initializeData() {
        RecordSearch sr = new RecordSearch(database);
        scheduletable.clear();
        scheduletable.put("mid", object);
        scheduletable.put("td", "null");
        sr.setConditionMap(scheduletable);
        String line[] = sr.getArrayFromResultSet();
        for (int i = 0; i < line.length; i++) {
            String splitline[] = line[i].split("\t");
            String value = splitline[Column.pv_index - 1];
            if (value.equals("0")) value = splitline[Column.vt_index - 1]; else value = gid.getItem(value);
            scheduletable.put(gid.getItem(splitline[Column.pid_index - 1]), value);
        }
        scheduletable = ConvertToLower.convertHashKey(scheduletable);
    }

    public void setBuffer(String[] buffer) {
    }

    public void setDbmanager(DBManager database) {
        this.database = database;
        gid = new GetIdName(database);
        DAY = gid.getId(DAY);
    }

    public void setKey(String key) {
    }

    public void setObject(String Object) {
        this.object = Object;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public void setReportName(String ReportName) {
        this.reportname = ReportName;
    }

    public void setReportmap(Hashtable<Object, Object> table) {
        this.Reportmap = table;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void settypeofreport(String typeofreport) {
        typeofreport = "schedule";
    }

    public void updateReport() {
        addReport();
    }

    public void setrepoton(String reporton) {
    }

    public String getProcess() {
        return process;
    }

    public void addInReport_master() {
        String type = gid.getId("type");
        RecordSearch rs = new RecordSearch(database);
        String report_name = gid.getId(reportname);
        String report = gid.getId("report");
        String printreport = gid.getId("printreport");
        Hashtable<Object, Object> condition = new Hashtable<Object, Object>();
        condition.put("pv", report_name);
        condition.put("pid", report);
        rs.setConditionMap(condition);
        String result[] = rs.getArrayFromResultSet();
        Property property = new Property(database);
        for (int i = 0; i < result.length; i++) {
            String spliresult[] = result[i].split("\t");
            property.setMid(spliresult[Column.mid_index - 1]);
            property.setPid(type);
            String mastertype = property.getValue();
            if (mastertype.equals("printreport")) {
                condition.put("pv", report_name);
                condition.put("pid", printreport);
                rs.setConditionMap(condition);
                String line[] = rs.getArrayFromResultSet();
                if (line.length == 0) {
                    Hashtable<Object, Object> insertmap = new Hashtable<Object, Object>();
                    insertmap.put("report_name", reportname);
                    insertmap.put("process", "mod");
                    insertmap.put("status", "0");
                    database.getInsert("report_master", insertmap);
                }
            }
        }
    }
}
