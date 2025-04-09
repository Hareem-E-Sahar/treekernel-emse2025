import java.util.*;
import com.pandmservices.*;
import com.pandmservices.core.*;
import com.pandmservices.support.*;
import com.pandmservices.dbserver.*;
import com.pandmservices.web.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.Vector;
import java.text.*;
import java.lang.*;
import java.lang.Integer;
import java.lang.Math.*;
import java.lang.String;
import java.lang.Float;
import java.lang.Double;
import java.math.*;
import java.util.Date;
import java.sql.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Locale;
import java.util.ResourceBundle;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class servsys extends JFrame {

    private Font font = new Font("serif", Font.ITALIC + Font.BOLD, 36);

    protected ResourceBundle resbundle;

    protected AboutBox aboutBox;

    protected PrefPane prefs;

    protected Action newAction, openAction, closeAction, saveAction, saveAsAction, undoAction, cutAction, copyAction, pasteAction, clearAction, selectAllAction;

    static final JMenuBar mainMenuBar = new JMenuBar();

    protected JMenu fileMenu, editMenu;

    public static void main(String args[]) throws Exception {
        System.out.println("This code is meant to be run from Tomcat at this time. There is no user interface.\n");
        System.out.println("listnewcust - list cust without numbers \nsynccallslip - upload all info to server\n");
        System.out.println("convertinv - Convert inventory from old to new format\nsendcheckme - mail checkme reports neeeds: sendcheckme username listdate\n");
        System.out.println("Command Line Argument is:  " + args[0] + "");
        if (args[0].equalsIgnoreCase("sendweekly")) {
            String host = args[1];
            String database = args[2];
            String dbuser = args[3];
            String passwd = args[4];
            String efrom = args[5];
            String eserver = args[6];
            String euser = args[7];
            String epasswd = args[8];
            String sendto = args[9];
            System.out.println("Sendto: " + sendto + "\n");
            doWeeklyTimeTransmit(host, database, dbuser, passwd, efrom, eserver, euser, epasswd, sendto);
        } else if (args[0].equalsIgnoreCase("senddailytime")) {
            String host = args[1];
            String database = args[2];
            String dbuser = args[3];
            String passwd = args[4];
            String efrom = args[5];
            String eserver = args[6];
            String euser = args[7];
            String epasswd = args[8];
            String sendto = args[9];
            System.out.println("Sendto: " + sendto + "\n");
            doDailyTimeTransmit(host, database, dbuser, passwd, efrom, eserver, euser, epasswd, sendto);
        } else if (args[0].equalsIgnoreCase("gui")) {
            String localbundle = args[1];
            new servsys(localbundle);
        } else {
            System.out.println("no such choice\n");
        }
    }

    public static void doWeeklyTimeTransmit(String host, String database, String dbuser, String password, String efrom, String eserver, String euser, String epasswd, String sendto) throws Exception {
        Connection con;
        System.out.println("Weekly Time Report\n");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "", dbuser, password);
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
        Statement stmt2 = con.createStatement();
        Statement stmt = con.createStatement();
        ResultSet rs2 = stmt2.executeQuery("select department, transmit from tech_table where transmit=1 group by department;");
        Format formatter;
        Calendar now = Calendar.getInstance();
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        String s = formatter.format(date);
        Date expireationDate = null;
        expireationDate = dateFormatter.parse(s);
        Calendar dateToBeTested = Calendar.getInstance();
        dateToBeTested.setTime(expireationDate);
        dateToBeTested.add(Calendar.DAY_OF_YEAR, -1);
        String enddate = doFormatDate(dateToBeTested.getTime());
        dateToBeTested.add(Calendar.DAY_OF_YEAR, -8);
        String startdate = doFormatDate(dateToBeTested.getTime());
        System.out.println("Start Date: " + startdate + "\n");
        System.out.println("End Date: " + enddate + "\n");
        Vector u;
        String mbody = "";
        String mbody2 = "";
        mbody = combinestring(mbody, "<html><basefont size=-1>");
        mbody = combinestring(mbody, "<head><title>Time Sheet Report</title></head><body><h2>By Technician</h2>");
        u = UniTechInfo.getAllTransmitItems(con);
        int counter = 0;
        for (int iu = 0; iu < u.size(); iu++) {
            UniTechInfo tu = (UniTechInfo) u.elementAt(iu);
            String tech_init = tu.getTechInit();
            String lusername = tu.getUserName();
            String tech_name = tu.getTechName();
            String department = tu.getDepartment();
            Vector v;
            v = TimeSheetSummary.getLoginItemsMultiDate(con, lusername, doFormatDateDb(getDateDb(startdate)), doFormatDateDb(getDateDb(enddate)));
            mbody = combinestring(mbody, "<table border=1 width=\"75%\" align=\"left\">");
            mbody = combinestring(mbody, "<tr><td>Tech ID: " + lusername + " - " + tech_name + "</td></tr>");
            mbody = combinestring(mbody, "</table>");
            if (v.size() > 0) {
                mbody = combinestring(mbody, "<table border=1 width=\"75%\" align=\"left\">");
                mbody = combinestring(mbody, "<tr><td>Call-Type</td><td>Count</td><td>Total-Collected</td><td>Non-Commision-Billed</td><td>Commision-Billed</td><td>Commision</td><td>Time</td><td>Time-Without-Travel</td></tr>");
                for (int i = 0; i < v.size(); i++) {
                    TimeSheetSummary ts = (TimeSheetSummary) v.elementAt(i);
                    String tamount = ts.Amount();
                    String tcamount = ts.CAmount();
                    String tamount_collected = ts.AmountCollected();
                    String tcommision = ts.Commision();
                    String ctype = ts.CType();
                    String callcount = ts.CallCount();
                    String timewithtravel = ts.TimeWithTravel();
                    String timenotravel = ts.TimeNoTravel();
                    mbody = combinestring(mbody, "<tr><td>" + ctype + "</td><td>" + callcount + "</td><td>" + tamount_collected + "</td><td>" + tamount + "</td><td>" + tcamount + "</td><td>" + tcommision + "</td><td>" + timewithtravel + "</td><td>" + timenotravel + "</td></tr>");
                }
                ResultSet rs = stmt.executeQuery("select count(tsid) as callcount, sum(amount) as amount, sum(amount_collected) as amount_collected, sum(camount) as camount, sum(commision) as commision,  ucase(SEC_TO_TIME(sum(TIME_TO_SEC(subtime(time_out,dispatch_time))))) as time_with_travel,  ucase(SEC_TO_TIME(sum(TIME_TO_SEC(subtime(time_out,time_in))))) as time_no_travel from time_sheet where login='" + lusername + "' and tdate>='" + doFormatDateDb(getDateDb(startdate)) + "' and tdate<='" + doFormatDateDb(getDateDb(enddate)) + "';");
                while (rs.next()) {
                    String tamount = rs.getString("amount");
                    String tcamount = rs.getString("camount");
                    String tamount_collected = rs.getString("amount_collected");
                    String tcommision = rs.getString("commision");
                    String callcount = rs.getString("callcount");
                    String timewithtravel = rs.getString("time_with_travel");
                    String timenotravel = rs.getString("time_no_travel");
                    mbody = combinestring(mbody, "<tr><td>Total</td><td>" + callcount + "</td><td>" + tamount_collected + "</td><td>" + tamount + "</td><td>" + tcamount + "</td><td>" + tcommision + "</td><td>" + timewithtravel + "</td><td>" + timenotravel + "</td></tr>");
                }
                mbody = combinestring(mbody, "</table><br>");
            } else {
                mbody = combinestring(mbody, "<table border=0 width=\"75%\" align=\"left\">");
                mbody = combinestring(mbody, "<tr><td>**NO DATA TRANSMITTED</td></tr></table><br>");
            }
            mbody = combinestring(mbody, "<table border=0 width=\"75%\" align=\"left\">");
            mbody = combinestring(mbody, "<tr><td> </td></tr></table><br>");
        }
        mbody2 = combinestring(mbody2, "<html><basefont size=-1>");
        mbody2 = combinestring(mbody2, "<head><title>Time Sheet Report</title></head><body><h2>By Department</h2>");
        while (rs2.next()) {
            String sdepartment = rs2.getString("department");
            Vector v;
            v = TimeSheetSummary.getDepartmentItemsMultiDate(con, sdepartment, doFormatDateDb(getDateDb(startdate)), doFormatDateDb(getDateDb(enddate)));
            mbody2 = combinestring(mbody2, "<table border=1 width=\"75%\" align=\"left\">");
            mbody2 = combinestring(mbody2, "<tr><td>Department:  " + sdepartment + "</td></tr>");
            mbody2 = combinestring(mbody2, "</table>");
            if (v.size() > 0) {
                mbody2 = combinestring(mbody2, "<table border=1 width=\"75%\" align=\"left\">");
                mbody2 = combinestring(mbody2, "<tr><td>Call-Type</td><td>Count</td><td>Total-Collected</td><td>Non-Commision-Billed</td><td>Commision-Billed</td><td>Commision</td><td>Time</td><td>Time-Without-Travel</td></tr>");
                for (int i = 0; i < v.size(); i++) {
                    TimeSheetSummary ts = (TimeSheetSummary) v.elementAt(i);
                    String tamount = ts.Amount();
                    String tcamount = ts.CAmount();
                    String tamount_collected = ts.AmountCollected();
                    String tcommision = ts.Commision();
                    String ctype = ts.CType();
                    String callcount = ts.CallCount();
                    String timewithtravel = ts.TimeWithTravel();
                    String timenotravel = ts.TimeNoTravel();
                    mbody2 = combinestring(mbody2, "<tr><td>" + ctype + "</td><td>" + callcount + "</td><td>" + tamount_collected + "</td><td>" + tamount + "</td><td>" + tcamount + "</td><td>" + tcommision + "</td><td>" + timewithtravel + "</td><td>" + timenotravel + "</td></tr>");
                }
                ResultSet rs3 = stmt.executeQuery("select tech_table.department as department, count(tsid) as callcount, sum(amount) as amount, sum(amount_collected) as amount_collected, sum(camount) as camount, sum(commision) as commision,  ucase(SEC_TO_TIME(sum(TIME_TO_SEC(subtime(time_out,dispatch_time))))) as time_with_travel,  ucase(SEC_TO_TIME(sum(TIME_TO_SEC(subtime(time_out,time_in))))) as time_no_travel from time_sheet, tech_table  where time_sheet.login=tech_table.username and tdate>='" + doFormatDateDb(getDateDb(startdate)) + "' and tdate<='" + doFormatDateDb(getDateDb(enddate)) + "' and department='" + sdepartment + "' group by department;");
                while (rs3.next()) {
                    String tamount = rs3.getString("amount");
                    String tcamount = rs3.getString("camount");
                    String tamount_collected = rs3.getString("amount_collected");
                    String tcommision = rs3.getString("commision");
                    String callcount = rs3.getString("callcount");
                    String timewithtravel = rs3.getString("time_with_travel");
                    String timenotravel = rs3.getString("time_no_travel");
                    mbody2 = combinestring(mbody2, "<tr><td>Total</td><td>" + callcount + "</td><td>" + tamount_collected + "</td><td>" + tamount + "</td><td>" + tcamount + "</td><td>" + tcommision + "</td><td>" + timewithtravel + "</td><td>" + timenotravel + "</td></tr>");
                }
                mbody2 = combinestring(mbody2, "</table>");
            } else {
                mbody2 = combinestring(mbody2, "<table border=0 width=\"75%\" align=\"left\">");
                mbody2 = combinestring(mbody2, "<tr><td>**NO DATA TRANSMITTED</td></tr></table>");
            }
            mbody2 = combinestring(mbody2, "<table border=0 width=\"75%\" align=\"left\">");
            mbody2 = combinestring(mbody2, "<tr><td> </td></tr></table><br>");
        }
        mbody = "";
    }

    public static void doDailyTimeTransmit(String host, String database, String dbuser, String password, String efrom, String eserver, String euser, String epasswd, String sendto) throws Exception {
        Connection con;
        System.out.println("Daily Callslip Report\n\n");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "", dbuser, password);
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
        Statement stmt2 = con.createStatement();
        ResultSet rs2 = stmt2.executeQuery("select department, transmit from tech_table where transmit=1 group by department;");
        Format formatter;
        Calendar now = Calendar.getInstance();
        Date date = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        String s = formatter.format(date);
        String timesheetdate = formatter.format(date);
        Date expireationDate = null;
        expireationDate = dateFormatter.parse(s);
        Calendar dateToBeTested = Calendar.getInstance();
        dateToBeTested.setTime(expireationDate);
        dateToBeTested.add(Calendar.DAY_OF_YEAR, -1);
        timesheetdate = doFormatDate(dateToBeTested.getTime());
        System.out.println("new Time Sheet Date: " + timesheetdate + "\n\n");
        Vector u;
        String mbody = "";
        String mbody2 = "";
        mbody = combinestring(mbody, "<html><basefont size=-1>");
        mbody = combinestring(mbody, "<head><title>Time Sheet Report</title></head><body><h2>By Technician</h2>");
        u = UniTechInfo.getAllTransmitItems(con);
        int counter = 0;
        for (int iu = 0; iu < u.size(); iu++) {
            UniTechInfo tu = (UniTechInfo) u.elementAt(iu);
            String tech_init = tu.getTechInit();
            String lusername = tu.getUserName();
            String tech_name = tu.getTechName();
            String department = tu.getDepartment();
            Vector v;
            v = TimeSheetSummary.getLoginItemsSingleDate(con, lusername, doFormatDateDb(getDateDb(timesheetdate)));
            mbody = combinestring(mbody, "<table border=1 width=\"75%\" align=\"left\">");
            mbody = combinestring(mbody, "<tr><td>Tech ID: " + lusername + " - " + tech_name + "</td></tr>");
            mbody = combinestring(mbody, "</table>");
            if (v.size() > 0) {
                mbody = combinestring(mbody, "<table border=1 width=\"75%\" align=\"left\">");
                mbody = combinestring(mbody, "<tr><td>Call-Type</td><td>Count</td><td>Total-Collected</td><td>Non-Commision-Billed</td><td>Commision-Billed</td><td>Commision</td><td>Time</td><td>Time-Without-Travel</td></tr>");
                for (int i = 0; i < v.size(); i++) {
                    TimeSheetSummary ts = (TimeSheetSummary) v.elementAt(i);
                    String tamount = ts.Amount();
                    String tcamount = ts.CAmount();
                    String tamount_collected = ts.AmountCollected();
                    String tcommision = ts.Commision();
                    String ctype = ts.CType();
                    String callcount = ts.CallCount();
                    String timewithtravel = ts.TimeWithTravel();
                    String timenotravel = ts.TimeNoTravel();
                    mbody = combinestring(mbody, "<tr><td>" + ctype + "</td><td>" + callcount + "</td><td>" + tamount_collected + "</td><td>" + tamount + "</td><td>" + tcamount + "</td><td>" + tcommision + "</td><td>" + timewithtravel + "</td><td>" + timenotravel + "</td></tr>");
                }
                mbody = combinestring(mbody, "</table><br>");
            } else {
                mbody = combinestring(mbody, "<table border=0 width=\"75%\" align=\"left\">");
                mbody = combinestring(mbody, "<tr><td>**NO DATA TRANSMITTED</td></tr></table>");
            }
            mbody = combinestring(mbody, "<table border=0 width=\"75%\" align=\"left\">");
            mbody = combinestring(mbody, "<tr><td> </td></tr></table><br>");
        }
        mbody2 = combinestring(mbody2, "<html><basefont size=-1>");
        mbody2 = combinestring(mbody2, "<head><title>Time Sheet Report</title></head><body><h2>By Department</h2>");
        while (rs2.next()) {
            String sdepartment = rs2.getString("department");
            Vector v;
            v = TimeSheetSummary.getDepartmentItemsSingleDate(con, sdepartment, doFormatDateDb(getDateDb(timesheetdate)));
            mbody2 = combinestring(mbody2, "<table border=1 width=\"75%\" align=\"left\">");
            mbody2 = combinestring(mbody2, "<tr><td>Department:  " + sdepartment + "</td></tr>");
            mbody2 = combinestring(mbody2, "</table>");
            if (v.size() > 0) {
                mbody2 = combinestring(mbody2, "<table border=1 width=\"75%\" align=\"left\">");
                mbody2 = combinestring(mbody2, "<tr><td>Call-Type</td><td>Count</td><td>Total-Collected</td><td>Non-Commision-Billed</td><td>Commision-Billed</td><td>Commision</td><td>Time</td><td>Time-Without-Travel</td></tr>");
                for (int i = 0; i < v.size(); i++) {
                    TimeSheetSummary ts = (TimeSheetSummary) v.elementAt(i);
                    String tamount = ts.Amount();
                    String tcamount = ts.CAmount();
                    String tamount_collected = ts.AmountCollected();
                    String tcommision = ts.Commision();
                    String ctype = ts.CType();
                    String callcount = ts.CallCount();
                    String timewithtravel = ts.TimeWithTravel();
                    String timenotravel = ts.TimeNoTravel();
                    mbody2 = combinestring(mbody2, "<tr><td>" + ctype + "</td><td>" + callcount + "</td><td>" + tamount_collected + "</td><td>" + tamount + "</td><td>" + tcamount + "</td><td>" + tcommision + "</td><td>" + timewithtravel + "</td><td>" + timenotravel + "</td></tr>");
                }
                mbody2 = combinestring(mbody2, "</table><br>");
            } else {
                mbody2 = combinestring(mbody2, "<table border=0 width=\"75%\" align=\"left\">");
                mbody2 = combinestring(mbody2, "<tr><td>**NO DATA TRANSMITTED</td></tr></table>");
            }
            mbody2 = combinestring(mbody2, "<table border=0 width=\"75%\" align=\"left\">");
            mbody2 = combinestring(mbody2, "<tr><td> </td></tr></table><br>");
        }
        mbody = "";
    }

    public static String doGetTech_Email(Connection con, String lusername) throws Exception {
        Vector v;
        v = UniEmailAdd.getAllItems(con, lusername);
        int counter = 0;
        String tech_email = null;
        for (int i = 0; i < v.size(); i++) {
            UniEmailAdd t = (UniEmailAdd) v.elementAt(i);
            tech_email = t.getTechEmail();
        }
        return tech_email;
    }

    public static String combinestring(String oldstring, String newstring) {
        String changedstring = oldstring.concat(newstring);
        return changedstring;
    }

    public static Date getDate(String token) {
        Date visited;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ParsePosition pos;
        try {
            pos = new ParsePosition(0);
            visited = sdf.parse(token, pos);
        } catch (NullPointerException pe) {
            System.out.println("Cannot parse visited date! " + token);
            pos = new ParsePosition(0);
            visited = sdf.parse("16/06/97", pos);
        }
        return visited;
    }

    public static String doGetTechInfo_truck(Connection con, String iusername) throws Exception {
        Vector v;
        String truck_num = null;
        v = UniTechInfo.getAllItems(con, iusername);
        int counter = 0;
        for (int i = 0; i < v.size(); i++) {
            UniTechInfo t = (UniTechInfo) v.elementAt(i);
            truck_num = t.getTruckNum();
        }
        return truck_num;
    }

    public static String doGetTechInfo_init(Connection con, String iusername) throws Exception {
        Vector v;
        v = UniTechInfo.getAllItems(con, iusername);
        int counter = 0;
        String tech_init = null;
        for (int i = 0; i < v.size(); i++) {
            UniTechInfo t = (UniTechInfo) v.elementAt(i);
            tech_init = t.getTechInit();
        }
        return tech_init;
    }

    public static String doGetSvc_Email(Connection con, String lusername) throws Exception {
        Vector v;
        v = UniEmailAdd.getAllItems(con, lusername);
        int counter = 0;
        String svc_email = null;
        for (int i = 0; i < v.size(); i++) {
            UniEmailAdd t = (UniEmailAdd) v.elementAt(i);
            svc_email = t.getSvcEmail();
        }
        return svc_email;
    }

    public static String doGetSmtpUser(Connection con, String lusername) throws Exception {
        Vector v;
        v = UniEmailAdd.getAllItems(con, lusername);
        int counter = 0;
        String smtpuser = null;
        for (int i = 0; i < v.size(); i++) {
            UniEmailAdd t = (UniEmailAdd) v.elementAt(i);
            smtpuser = t.getSmtpUser();
        }
        return smtpuser;
    }

    public static String doGetSmtpServer(Connection con, String lusername) throws Exception {
        Vector v;
        v = UniEmailAdd.getAllItems(con, lusername);
        int counter = 0;
        String smtpserver = null;
        for (int i = 0; i < v.size(); i++) {
            UniEmailAdd t = (UniEmailAdd) v.elementAt(i);
            smtpserver = t.getSmtpServer();
        }
        return smtpserver;
    }

    public static String doFormatDateDb(Date visited) throws Exception {
        Date tdate;
        Format formatter;
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        String newdate = formatter.format(visited);
        return newdate;
    }

    public static Date getDateDb(String token) {
        Date visited;
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
        ParsePosition pos;
        try {
            pos = new ParsePosition(0);
            visited = sdf.parse(token, pos);
        } catch (NullPointerException pe) {
            System.out.println("Cannot parse visited date! " + token);
            pos = new ParsePosition(0);
            visited = sdf.parse("16/06/97", pos);
        }
        return visited;
    }

    public static String doGetTechInfo_name(Connection con, String iusername) throws Exception {
        Vector v;
        v = UniTechInfo.getAllItems(con, iusername);
        int counter = 0;
        String tech_name = null;
        for (int i = 0; i < v.size(); i++) {
            UniTechInfo t = (UniTechInfo) v.elementAt(i);
            tech_name = t.getTechName();
        }
        return tech_name;
    }

    public static String doGetTechInfo_nateid(Connection con, String iusername) throws Exception {
        Vector v;
        v = UniTechInfo.getAllItems(con, iusername);
        int counter = 0;
        String nate_id = null;
        for (int i = 0; i < v.size(); i++) {
            UniTechInfo t = (UniTechInfo) v.elementAt(i);
            nate_id = t.getNateID();
        }
        return nate_id;
    }

    public static String doGetSmtpPassword(Connection con, String lusername) throws Exception {
        Vector v;
        v = UniEmailAdd.getAllItems(con, lusername);
        int counter = 0;
        String smtppassword = null;
        for (int i = 0; i < v.size(); i++) {
            UniEmailAdd t = (UniEmailAdd) v.elementAt(i);
            smtppassword = t.getSmtpPassword();
        }
        return smtppassword;
    }

    public static void doMailSend(String smtpServer, String to, String from, String subject, String body, String smtpuser, String smtppassword) {
        try {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", smtpServer);
            props.put("mail.smtp.auth", "true");
            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            msg.setSubject(subject);
            msg.setContent(body, "text/html");
            msg.setHeader("X-Mailer", "LOTONtechEmail");
            msg.setSentDate(new Date());
            msg.saveChanges();
            Transport trans = session.getTransport("smtp");
            trans.connect(smtpServer, smtpuser, smtppassword);
            trans.sendMessage(msg, msg.getAllRecipients());
            trans.close();
            System.out.println("Message sent OK.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void doMailSendText(String smtpServer, String to, String from, String subject, String body, String smtpuser, String smtppassword) {
        try {
            Properties props = System.getProperties();
            props.put("mail.smtp.host", smtpServer);
            props.put("mail.smtp.auth", "true");
            Session session = Session.getDefaultInstance(props, null);
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            msg.setSubject(subject);
            msg.setText(body);
            msg.setHeader("X-Mailer", "LOTONtechEmail");
            msg.setSentDate(new Date());
            msg.saveChanges();
            Transport trans = session.getTransport("smtp");
            trans.connect(smtpServer, smtpuser, smtppassword);
            trans.sendMessage(msg, msg.getAllRecipients());
            trans.close();
            System.out.println("Message sent OK.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String doFormatDate(Date visited) throws Exception {
        Date tdate;
        Format formatter;
        formatter = new SimpleDateFormat("MM-dd-yyyy");
        String newdate = formatter.format(visited);
        return newdate;
    }

    public servsys(String localbundle) {
        super("");
        setTitle("ServSys GUI");
        this.getContentPane().setLayout(null);
        createActions();
        addMenus();
        setSize(310, 150);
        setVisible(true);
    }

    public void createActions() {
        int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        newAction = new newActionClass("New", KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutKeyMask));
        openAction = new openActionClass("Open", KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKeyMask));
        closeAction = new closeActionClass("Close", KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcutKeyMask));
        saveAction = new saveActionClass("Save", KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcutKeyMask));
        saveAsAction = new saveAsActionClass("Save As");
        undoAction = new undoActionClass("Undo", KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutKeyMask));
        cutAction = new cutActionClass("Cut", KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcutKeyMask));
        copyAction = new copyActionClass("Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutKeyMask));
        pasteAction = new pasteActionClass("Paste", KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutKeyMask));
        clearAction = new clearActionClass("Clear");
        selectAllAction = new selectAllActionClass("Select All", KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcutKeyMask));
    }

    public void addMenus() {
        fileMenu = new JMenu("File");
        fileMenu.add(new JMenuItem(newAction));
        fileMenu.add(new JMenuItem(openAction));
        fileMenu.add(new JMenuItem(closeAction));
        fileMenu.add(new JMenuItem(saveAction));
        fileMenu.add(new JMenuItem(saveAsAction));
        mainMenuBar.add(fileMenu);
        editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem(undoAction));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem(cutAction));
        editMenu.add(new JMenuItem(copyAction));
        editMenu.add(new JMenuItem(pasteAction));
        editMenu.add(new JMenuItem(clearAction));
        editMenu.addSeparator();
        editMenu.add(new JMenuItem(selectAllAction));
        mainMenuBar.add(editMenu);
        setJMenuBar(mainMenuBar);
    }

    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.blue);
        g.setFont(font);
        g.drawString("Message Here", 40, 80);
    }

    public class newActionClass extends AbstractAction {

        public newActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("New...");
        }
    }

    public class openActionClass extends AbstractAction {

        public openActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Open...");
        }
    }

    public class closeActionClass extends AbstractAction {

        public closeActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Close...");
        }
    }

    public class saveActionClass extends AbstractAction {

        public saveActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Save...");
        }
    }

    public class saveAsActionClass extends AbstractAction {

        public saveAsActionClass(String text) {
            super(text);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Save As...");
        }
    }

    public class undoActionClass extends AbstractAction {

        public undoActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Undo...");
        }
    }

    public class cutActionClass extends AbstractAction {

        public cutActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Cut...");
        }
    }

    public class copyActionClass extends AbstractAction {

        public copyActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Copy...");
        }
    }

    public class pasteActionClass extends AbstractAction {

        public pasteActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Paste...");
        }
    }

    public class clearActionClass extends AbstractAction {

        public clearActionClass(String text) {
            super(text);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Clear...");
        }
    }

    public class selectAllActionClass extends AbstractAction {

        public selectAllActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            System.out.println("Select All...");
        }
    }
}
