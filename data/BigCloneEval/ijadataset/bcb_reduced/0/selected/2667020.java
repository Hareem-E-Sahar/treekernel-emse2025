package net.sf.borg.control;

import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import net.sf.borg.common.Errmsg;
import net.sf.borg.common.PrefName;
import net.sf.borg.common.Prefs;
import net.sf.borg.common.Resource;
import net.sf.borg.common.SocketClient;
import net.sf.borg.common.SocketHandler;
import net.sf.borg.common.SocketServer;
import net.sf.borg.common.Warning;
import net.sf.borg.control.socketServer.SingleInstanceHandler;
import net.sf.borg.control.systemLogin.BorgNameLoginSystem;
import net.sf.borg.control.systemLogin.LoginSystem;
import net.sf.borg.model.AddressModel;
import net.sf.borg.model.AppointmentModel;
import net.sf.borg.model.LinkModel;
import net.sf.borg.model.MemoModel;
import net.sf.borg.model.TaskModel;
import net.sf.borg.model.beans.User;
import net.sf.borg.model.db.jdbc.JdbcDB;
import net.sf.borg.model.db.jdbc.SystemDB;
import net.sf.borg.ui.MultiView;
import net.sf.borg.ui.OptionsView;
import net.sf.borg.ui.SunTrayIconProxy;
import net.sf.borg.ui.calendar.TodoView;
import net.sf.borg.ui.popup.PopupView;
import net.sf.borg.ui.util.Banner;
import net.sf.borg.ui.util.ModalMessage;
import net.sf.borg.ui.util.NwFontChooserS;

public class Borg implements OptionsView.RestartListener, SocketHandler {

    private static Banner ban_ = null;

    private static String selectedDB;

    private static Borg singleton = null;

    public static Borg getReference() {
        if (singleton == null) singleton = new Borg();
        return (singleton);
    }

    public static void main(String args[]) {
        int port = Prefs.getIntPref(PrefName.SOCKETPORT);
        if (port != -1) {
            String resp;
            try {
                resp = SocketClient.sendMsg("localhost", port, "open");
                if (resp != null && resp.equals("ok")) {
                    System.exit(0);
                }
            } catch (IOException e) {
            }
        }
        Borg b = getReference();
        b.init(args);
    }

    public static void shutdown() {
        String backupdir = Prefs.getPref(PrefName.BACKUPDIR);
        if (backupdir != null && !backupdir.equals("")) {
            try {
                int ret = JOptionPane.showConfirmDialog(null, Resource.getResourceString("backup_notice") + " " + backupdir + "?", "BORG", JOptionPane.OK_CANCEL_OPTION);
                if (ret == JOptionPane.YES_OPTION) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                    String uniq = sdf.format(new Date());
                    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(backupdir + "/borg" + uniq + ".zip"));
                    Writer fw = new OutputStreamWriter(out, "UTF8");
                    out.putNextEntry(new ZipEntry("borg.xml"));
                    AppointmentModel.getReference().export(fw);
                    fw.flush();
                    out.closeEntry();
                    out.putNextEntry(new ZipEntry("task.xml"));
                    TaskModel.getReference().export(fw);
                    fw.flush();
                    out.closeEntry();
                    out.putNextEntry(new ZipEntry("addr.xml"));
                    AddressModel.getReference().export(fw);
                    fw.flush();
                    out.closeEntry();
                    if (MemoModel.getReference().hasMemos()) {
                        out.putNextEntry(new ZipEntry("memo.xml"));
                        MemoModel.getReference().export(fw);
                        fw.flush();
                        out.closeEntry();
                    }
                    if (LinkModel.getReference().hasLinks()) {
                        out.putNextEntry(new ZipEntry("link.xml"));
                        LinkModel.getReference().export(fw);
                        fw.flush();
                        out.closeEntry();
                    }
                    out.close();
                }
            } catch (Exception e) {
                Errmsg.errmsg(e);
            }
        }
        try {
            Banner ban = new Banner();
            ban.setText(Resource.getPlainResourceString("shutdown"));
            ban.setVisible(true);
            AppointmentModel.getReference().getDB().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Timer shutdownTimer = new java.util.Timer();
        shutdownTimer.schedule(new TimerTask() {

            public void run() {
                System.exit(0);
            }
        }, 3 * 1000, 28 * 60 * 1000);
    }

    private void closeSystem() {
        String backupdir = Prefs.getPref(PrefName.BACKUPDIR);
        if (backupdir != null && !backupdir.equals("")) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String uniq = sdf.format(new Date());
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(backupdir + "/borg" + uniq + ".zip"));
                Writer fw = new OutputStreamWriter(out, "UTF8");
                out.putNextEntry(new ZipEntry("borg.xml"));
                AppointmentModel.getReference().export(fw);
                fw.flush();
                out.closeEntry();
                out.putNextEntry(new ZipEntry("task.xml"));
                TaskModel.getReference().export(fw);
                fw.flush();
                out.closeEntry();
                out.putNextEntry(new ZipEntry("addr.xml"));
                AddressModel.getReference().export(fw);
                fw.flush();
                out.closeEntry();
                if (MemoModel.getReference().hasMemos()) {
                    out.putNextEntry(new ZipEntry("memo.xml"));
                    MemoModel.getReference().export(fw);
                    fw.flush();
                    out.closeEntry();
                }
                if (LinkModel.getReference().hasLinks()) {
                    out.putNextEntry(new ZipEntry("link.xml"));
                    LinkModel.getReference().export(fw);
                    fw.flush();
                    out.closeEntry();
                }
                out.close();
            } catch (Exception e) {
                Errmsg.errmsg(e);
            }
        }
        try {
            AppointmentModel.getReference().getDB().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void syncDBs() throws Exception {
        AppointmentModel.getReference().sync();
        AddressModel.getReference().sync();
        TaskModel.getReference().sync();
    }

    private Timer mailTimer_ = null;

    private ModalMessage modalMessage = null;

    private SocketServer socketServer_ = null;

    private java.util.Timer syncTimer_ = null;

    private boolean trayIcon = true;

    private Borg() {
    }

    public boolean hasTrayIcon() {
        return trayIcon;
    }

    public synchronized String processMessage(String msg) {
        if (msg.equals("sync")) {
            try {
                syncDBs();
                return ("sync success");
            } catch (Exception e) {
                e.printStackTrace();
                return ("sync error: " + e.toString());
            }
        } else if (msg.equals("shutdown")) {
            System.exit(0);
        } else if (msg.equals("open")) {
            MultiView.getMainView().toFront();
            MultiView.getMainView().setState(Frame.NORMAL);
            return ("ok");
        } else if (msg.startsWith("lock:")) {
            final String lockmsg = msg.substring(5);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    if (modalMessage == null || !modalMessage.isShowing()) {
                        modalMessage = new ModalMessage(lockmsg, false);
                        modalMessage.setVisible(true);
                    } else {
                        modalMessage.appendText(lockmsg);
                    }
                    modalMessage.setEnabled(false);
                    modalMessage.toFront();
                }
            });
            return ("ok");
        } else if (msg.equals("unlock")) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    if (modalMessage.isShowing()) {
                        modalMessage.setEnabled(true);
                    }
                }
            });
            return ("ok");
        } else if (msg.startsWith("<")) {
            return SingleInstanceHandler.execute(msg);
        }
        return ("Unknown msg: " + msg);
    }

    public void restart() {
        if (syncTimer_ != null) syncTimer_.cancel();
        if (mailTimer_ != null) mailTimer_.cancel();
        closeSystem();
        init(new String[0]);
        MultiView.getMainView().reinit();
    }

    private void init(String args[]) {
        OptionsView.setRestartListener(this);
        String testdb = null;
        String trayname = "BORG";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-trayname")) {
                i++;
                if (i >= args.length) {
                    System.out.println("Error: missing trayname argument");
                    System.exit(1);
                }
                trayname = args[i];
            } else if (args[i].equals("-db")) {
                i++;
                if (i >= args.length) {
                    System.out.println(Resource.getResourceString("-db_argument_is_missing"));
                    System.exit(1);
                }
                testdb = args[i];
            }
        }
        boolean splash = true;
        String spl = Prefs.getPref(PrefName.SPLASH);
        if (spl.equals("false")) {
            splash = false;
        }
        String deffont = Prefs.getPref(PrefName.DEFFONT);
        if (!deffont.equals("")) {
            Font f = Font.decode(deffont);
            NwFontChooserS.setDefaultFont(f);
        }
        String lnf = Prefs.getPref(PrefName.LNF);
        try {
            UIManager.setLookAndFeel(lnf);
            UIManager.getLookAndFeelDefaults().put("ClassLoader", getClass().getClassLoader());
        } catch (Exception e) {
        }
        String country = Prefs.getPref(PrefName.COUNTRY);
        String language = Prefs.getPref(PrefName.LANGUAGE);
        if (!language.equals("")) {
            Locale.setDefault(new Locale(language, country));
        }
        boolean success = SystemManager.doLogin();
        if (!success) {
            System.exit(1);
        }
        if (testdb != null) {
            JdbcDB.setDBPath(testdb);
            JdbcDB.buildDbDir();
        }
        if (splash) {
            ban_ = new Banner();
            ban_.setText(Resource.getResourceString("Initializing"));
            ban_.setVisible(true);
        }
        String dbdir = "";
        String loggedUser = BorgNameLoginSystem.getInstance().getUsername();
        JdbcDB.setDBPath(loggedUser + File.separator + loggedUser + "_");
        dbdir = JdbcDB.buildDbDir();
        try {
            if (splash) ban_.setText(Resource.getResourceString("Loading_Appt_Database"));
            AppointmentModel calmod = AppointmentModel.create();
            calmod.open_db(dbdir);
            Errmsg.console(false);
            if (splash) ban_.setText(Resource.getResourceString("Loading_Task_Database"));
            TaskModel taskmod = TaskModel.create();
            taskmod.open_db(dbdir);
            if (splash) ban_.setText(Resource.getResourceString("Opening_Address_Database"));
            AddressModel addrmod = AddressModel.create();
            addrmod.open_db(dbdir);
            if (splash) ban_.setText(Resource.getResourceString("Opening_Memo_Database"));
            MemoModel memomod = MemoModel.create();
            try {
                memomod.open_db(dbdir);
            } catch (Warning w) {
            }
            LinkModel attmod = LinkModel.create();
            try {
                attmod.open_db(dbdir);
            } catch (Warning w) {
            }
            if (splash) ban_.setText(Resource.getResourceString("Opening_Main_Window"));
            final String traynm = trayname;
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    swingStart(traynm);
                }
            });
            if (splash) ban_.dispose();
            ban_ = null;
            Calendar cal = new GregorianCalendar();
            int emailmins = Prefs.getIntPref(PrefName.EMAILTIME);
            int curmins = 60 * cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE);
            int mailtime = emailmins - curmins;
            if (mailtime < 0) {
                try {
                    EmailReminder.sendDailyEmailReminder(null);
                } catch (Exception e) {
                    final Exception fe = e;
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {
                            Errmsg.errmsg(fe);
                        }
                    });
                }
                mailtime += 24 * 60;
            }
            mailTimer_ = new java.util.Timer();
            mailTimer_.schedule(new TimerTask() {

                public void run() {
                    try {
                        EmailReminder.sendDailyEmailReminder(null);
                    } catch (Exception e) {
                        final Exception fe = e;
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                Errmsg.errmsg(fe);
                            }
                        });
                    }
                }
            }, mailtime * 60 * 1000, 24 * 60 * 60 * 1000);
            int syncmins = Prefs.getIntPref(PrefName.SYNCMINS);
            String dbtype = Prefs.getPref(PrefName.DBTYPE);
            if ((dbtype.equals("mysql") || dbtype.equals("jdbc")) && syncmins != 0) {
                syncTimer_ = new java.util.Timer();
                syncTimer_.schedule(new TimerTask() {

                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                try {
                                    syncDBs();
                                } catch (Exception e) {
                                    Errmsg.errmsg(e);
                                }
                            }
                        });
                    }
                }, syncmins * 60 * 1000, syncmins * 60 * 1000);
            }
            int port = Prefs.getIntPref(PrefName.SOCKETPORT);
            if (port != -1 && socketServer_ == null) {
                socketServer_ = new SocketServer(port, this);
            }
        } catch (Exception e) {
            Errmsg.errmsg(e);
            String es = e.toString();
            int i1 = es.indexOf("** BEGIN NESTED");
            int i2 = es.indexOf("** END NESTED");
            if (i1 != -1 && i2 != -1) {
                int i3 = es.indexOf('\n', i1);
                String newstring = es.substring(0, i3) + "\n-- removed --\n" + es.substring(i2);
                es = newstring;
            }
            es += Resource.getResourceString("db_set_to") + dbdir;
            es += Resource.getResourceString("bad_db_2");
            int ret = JOptionPane.showConfirmDialog(null, es, Resource.getResourceString("BORG_Error"), JOptionPane.YES_NO_OPTION);
            if (ret == JOptionPane.YES_OPTION) {
                if (ban_ != null) ban_.dispose();
                OptionsView.dbSelectOnly();
                return;
            }
            System.exit(1);
        }
    }

    private void startTodoView() {
        try {
            TodoView tg = TodoView.getReference();
            MultiView.getMainView().addView(tg);
        } catch (Exception e) {
            Errmsg.errmsg(e);
        }
    }

    private void swingStart(String trayname) {
        trayIcon = true;
        String usetray = Prefs.getPref(PrefName.USESYSTRAY);
        if (!usetray.equals("true")) {
            trayIcon = false;
        } else {
            try {
                SunTrayIconProxy tip = SunTrayIconProxy.getReference();
                tip.init(trayname);
            } catch (UnsatisfiedLinkError le) {
                le.printStackTrace();
                trayIcon = false;
            } catch (NoClassDefFoundError ncf) {
                ncf.printStackTrace();
                trayIcon = false;
            } catch (Exception e) {
                e.printStackTrace();
                trayIcon = false;
            }
        }
        PopupView.getReference();
        MultiView mv = MultiView.getMainView();
        mv.setVisible(true);
        if (AppointmentModel.getReference().haveTodos()) {
            startTodoView();
        }
    }
}
