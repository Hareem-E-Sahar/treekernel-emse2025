import org.simplericity.macify.eawt.Application;
import org.simplericity.macify.eawt.ApplicationEvent;
import org.simplericity.macify.eawt.ApplicationListener;
import org.simplericity.macify.eawt.DefaultApplication;
import java.util.Formatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class MultiBot extends JFrame implements ApplicationListener {

    String proxyHost = System.getProperty("http.proxyHost");

    String proxyPort = System.getProperty("http.proxyPort");

    String uid = "";

    String pwd = "";

    String sMsgTxt = "";

    int cty = 1;

    boolean doLogin = false;

    boolean loginerror = false;

    int[] collectTime = { 600, 1800, 3600, 10800, 21600, 32400, 43200 };

    int CollTime = 0;

    String[] sLoginCity = { "Hamburg", "Berlin", "M�nchen" };

    double value = 0;

    double lastvalue, newvalue;

    MultiBot mainFrame = this;

    protected ResourceBundle resbundle;

    protected PennerInfo pennerInfo;

    protected PennerStats pennerStats;

    protected Connection pennerConnection;

    protected AboutBox aboutBox;

    protected PrefPane prefs;

    protected LoginBox logBox;

    protected ProxyBox proBox;

    private Application application;

    protected Action loginAction, logoutAction, newAction;

    static final JMenuBar mainMenuBar = new JMenuBar();

    protected JMenu fileMenu, editMenu;

    private JLabel SellLabel1;

    private JToggleButton SellToggleButton1, CollectToggleButton1;

    private JPanel StatisticsPanel, InfoPanel, CollectPanel, SellPanel;

    private JPanel PennerBildPanel;

    private JPanel jPanel2;

    private JPanel jPanel1;

    private JTabbedPane jTabbedPane1;

    private JLabel PennerBildLabel, CashBildLabel, BeerBildLabel, BookBildLabel, AttBildLabel, CrapBildLabel, CrapsBildLabel, CollectLabel, SellLabel, EarnLabel, ReloginLabel, StudyLabel, PayLabel, DummyLabel, PennerName, StatusLabel, EffectivityLabel;

    private JLabel CashLabel, BeerLabel, BookLabel, AttLabel, CrapLabel, CrapsLabel, ReloginCountLabel, CollectAmountLabel, SellAmountLabel, EarnAmountLabel, StudyAmountLabel, PayAmountLabel, StatusTextLabel, StatusEffectivityLabel;

    private JTextField jTextField1;

    private JTextField SellTextField1;

    private JLabel SellLabel3;

    private JLabel SellLabel2;

    private JLabel jLabel1;

    private JProgressBar jProgressBar1;

    private JPanel StatsGroup, InfoGroup, CollectGroup, SellGroup;

    private TitledBorder infoTitle;

    private javax.swing.Timer activityMonitor, activitySellMonitor;

    static CollectActivity collectActivity;

    private String sCollectAction = "Collect";

    private SellActivity sellActivity;

    private String sSellAction = "Sell";

    private Boolean activityMonitorfinish;

    private String addHint = "";

    private AboutAction aboutAction;

    private ExitAction exitAction;

    private PreferencesAction preferencesAction;

    static {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
    }

    public static void main(String[] args) {
        Application application = new DefaultApplication();
        MultiBot multibot = new MultiBot();
        multibot.setApplication(application);
        multibot.init();
    }

    private void setApplication(Application application) {
        this.application = application;
    }

    public void init() {
        application.addApplicationListener(this);
        application.addPreferencesMenuItem();
        application.setEnabledPreferencesMenu(true);
        resbundle = ResourceBundle.getBundle("strings", Locale.getDefault());
        setTitle(resbundle.getString("frameConstructor"));
        this.getContentPane().setLayout(null);
        createActions();
        preferencesAction = new PreferencesAction("Preferences");
        aboutAction = new AboutAction("About");
        exitAction = new ExitAction("Exit");
        pennerStats = new PennerStats();
        pennerInfo = new PennerInfo(pennerStats);
        pennerConnection = new Connection();
        addMenus();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent event) {
                setVisible(false);
            }
        });
        initGUI(this);
    }

    public void handleAbout(ApplicationEvent e) {
        if (aboutBox == null) {
            aboutBox = new AboutBox();
        }
        aboutAction.actionPerformed(null);
        e.setHandled(true);
    }

    public void handleOpenApplication(ApplicationEvent e) {
    }

    public void handleOpenFile(ApplicationEvent e) {
    }

    public void handlePreferences(ApplicationEvent e) {
        if (prefs == null) {
            prefs = new PrefPane();
        }
        preferencesAction.actionPerformed(null);
    }

    public void handlePrintFile(ApplicationEvent e) {
    }

    public void handleQuit(ApplicationEvent e) {
        exitAction.actionPerformed(null);
    }

    public void handleReOpenApplication(ApplicationEvent event) {
        JOptionPane.showMessageDialog(this, "Application reopened");
        System.out.println("hiu");
        setVisible(true);
    }

    class AboutAction extends AbstractAction {

        public AboutAction(String title) {
            super(title);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            aboutBox.setResizable(false);
            aboutBox.setVisible(true);
        }
    }

    class ExitAction extends AbstractAction {

        public ExitAction(String title) {
            super(title);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            MultiBot.this.dispose();
            System.exit(0);
        }
    }

    class PreferencesAction extends AbstractAction {

        public PreferencesAction(String title) {
            super(title);
        }

        public void actionPerformed(ActionEvent actionEvent) {
            int reload = JOptionPane.showConfirmDialog(MultiBot.this, "Sample Option?");
            if (reload == JOptionPane.YES_OPTION) {
            } else if (reload == JOptionPane.NO_OPTION) {
            }
        }
    }

    public void createActions() {
        int shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        loginAction = new loginActionClass(resbundle.getString("loginItem"), KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcutKeyMask));
        logoutAction = new logoutActionClass(resbundle.getString("logoutItem"), KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKeyMask));
        newAction = new newActionClass(resbundle.getString("newItem"), KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcutKeyMask));
    }

    public void addMenus() {
        fileMenu = new JMenu(resbundle.getString("pennerMenu"));
        fileMenu.add(new JMenuItem(newAction));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem(loginAction));
        fileMenu.add(new JMenuItem(logoutAction));
        if (!application.isMac()) {
            fileMenu.add(preferencesAction);
            fileMenu.add(exitAction);
        }
        mainMenuBar.add(fileMenu);
        setJMenuBar(mainMenuBar);
    }

    public class loginActionClass extends AbstractAction {

        public loginActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            boolean logBoxFinished = false;
            while (!logBoxFinished) {
                if (logBox == null) {
                    logBox = new LoginBox(mainFrame, pennerConnection);
                }
                logBox.setResizable(false);
                logBox.setVisible(true);
                logBoxFinished = logBox.getBoxCanceled();
                if (doLogin & !pennerConnection.LogedIn & !logBoxFinished) {
                    try {
                        pennerConnection.login(uid, pwd, cty);
                    } catch (Exception e1) {
                        pennerStats.setStatus(resbundle.getString("login_error"));
                        sMsgTxt = e1.getMessage();
                        if (sMsgTxt != null) {
                            Pattern p = Pattern.compile("HTTP response code: 407");
                            Matcher matcher = p.matcher(sMsgTxt);
                            if (matcher.find()) {
                                if (!pennerConnection.useProxy) {
                                    if (proBox == null) {
                                        proBox = new ProxyBox(mainFrame, pennerConnection);
                                    }
                                    proBox.setResizable(false);
                                    proBox.setVisible(true);
                                    if (!proBox.getBoxCanceled()) {
                                        pennerConnection.setProxy(true);
                                    }
                                }
                            }
                        }
                    }
                }
                if (pennerConnection.LogedIn) {
                    pennerStats.setStatus(resbundle.getString("logedin"));
                    pennerInfo.getInfo(pennerConnection);
                    pennerStats.setInitialBottles(pennerInfo.craps);
                    ((TitledBorder) infoTitle).setTitle(sLoginCity[cty]);
                    logBoxFinished = true;
                } else {
                    pennerStats.setStatus(resbundle.getString("login_error"));
                }
                InfoGroup.repaint();
            }
            updateStats();
            final Timer LoggedInTimer = new Timer();
            TimerTask LoggedInTask = new TimerTask() {

                public void run() {
                    if (pennerConnection.LogedIn) {
                        pennerStats.loggedInTime++;
                        pennerStats.valuechanged = true;
                    }
                }
            };
            LoggedInTimer.scheduleAtFixedRate(LoggedInTask, 0, 600000);
            final Timer getInfoTimer = new Timer();
            TimerTask getInfoTask = new TimerTask() {

                public void run() {
                    if (pennerConnection.LogedIn) {
                        pennerInfo.getInfo(pennerConnection);
                        pennerStats.collectBottles(pennerInfo.craps);
                    }
                }
            };
            getInfoTimer.scheduleAtFixedRate(getInfoTask, 0, 60000);
            final Timer infoTimer = new Timer();
            TimerTask infoTask = new TimerTask() {

                public void run() {
                    if (pennerConnection.LogedIn) {
                        updateInfos();
                    }
                }
            };
            infoTimer.scheduleAtFixedRate(infoTask, 0, 1000);
            final Timer statsTimer = new Timer();
            TimerTask statsTask = new TimerTask() {

                public void run() {
                    if (pennerConnection.LogedIn) {
                        updateStats();
                    }
                }
            };
            statsTimer.scheduleAtFixedRate(statsTask, 0, 1000);
        }
    }

    public class logoutActionClass extends AbstractAction {

        public logoutActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                pennerConnection.logout(cty);
            } catch (Exception e1) {
                pennerStats.setStatus("Logout Error");
            }
            if (!pennerConnection.LogedIn) {
                try {
                    pennerConnection.getPennerImage("http://img.pennergame.de/cache/bl_DE/avatare/standard.jpg");
                } catch (IOException e1) {
                    pennerStats.setStatus("Picture Error");
                }
                PennerBildLabel.setIcon(null);
                pennerStats.resetStats();
                pennerInfo.resetInfo();
                updateStats();
                updateInfos();
                InfoGroup.repaint();
            }
        }
    }

    public class newActionClass extends AbstractAction {

        public newActionClass(String text, KeyStroke shortcut) {
            super(text);
            putValue(ACCELERATOR_KEY, shortcut);
        }

        public void actionPerformed(ActionEvent e) {
            try {
                new MultiBot();
            } catch (Exception e1) {
                pennerStats.setStatus("New Error");
            }
        }
    }

    public void setUID(String userID) {
        uid = userID;
        doLogin = true;
    }

    public void setPWD(char[] userPWD) {
        pwd = String.valueOf(userPWD);
    }

    public void setCTY(int loginCTY) {
        cty = loginCTY;
    }

    public String int2Time(int allSeconds) {
        String sTime = "-/-";
        if (allSeconds > 0) {
            sTime = "";
            int days = allSeconds / 86400;
            int hours = (allSeconds - (days * 86400)) / 3600;
            int minutes = (allSeconds - (days * 86400) - (hours * 3600)) / 60;
            int seconds = allSeconds - (days * 86400) - (hours * 3600) - (minutes * 60);
            if (days > 1) sTime = String.valueOf(days) + " Tage ";
            if (days == 1) sTime = String.valueOf(days) + " Tag ";
            sTime = sTime + String.valueOf(hours) + ":";
            if (minutes < 10) {
                sTime = sTime + "0";
            }
            sTime = sTime + String.valueOf(minutes) + ":";
            if (seconds < 10) {
                sTime = sTime + "0";
            }
            sTime = sTime + String.valueOf(seconds);
        }
        return sTime;
    }

    public void updateInfos() {
        ImageIcon img = new ImageIcon("gfx/Penner.jpg");
        PennerBildLabel.setIcon(img);
        PennerName.setText(pennerInfo.pennerName);
        Formatter f = new Formatter();
        f.format("%6.2f", pennerInfo.cash);
        CashLabel.setText(String.valueOf(f.toString() + " �"));
        f = new Formatter();
        f.format("%1.2f", pennerInfo.permil);
        BeerLabel.setText(String.valueOf(f.toString() + " �"));
        BookLabel.setText(int2Time(pennerInfo.studyTime));
        AttLabel.setText(int2Time(pennerInfo.attTime));
        f = new Formatter();
        f.format("%1.2f", (double) pennerInfo.crapShare / 100);
        CrapLabel.setText(String.valueOf(f.toString() + " �"));
        CrapsLabel.setText(String.valueOf(pennerInfo.craps));
    }

    public void updateStats() {
        CollectAmountLabel.setText(String.valueOf(pennerStats.collectedBottles));
        SellAmountLabel.setText(String.valueOf(pennerStats.soldBottles));
        Formatter f = new Formatter();
        f.format("%6.2f", pennerStats.earnedMoney);
        EarnAmountLabel.setText(String.valueOf(f.toString()) + " �");
        StudyAmountLabel.setText(String.valueOf(pennerStats.startedStudies));
        f = new Formatter();
        f.format("%6.2f", pennerStats.paidMoney);
        PayAmountLabel.setText(String.valueOf(f.toString()) + " �");
        ReloginCountLabel.setText(String.valueOf(pennerStats.reloginCount));
        f = new Formatter();
        int gC = pennerStats.goodCaptchas;
        int lT = pennerStats.loggedInTime;
        if (gC > lT) {
            gC = lT;
        }
        if (pennerStats.valuechanged) {
            if (lT > 0) {
                newvalue = Math.round(gC * 100 / lT);
                if (lastvalue > 0) {
                    value = (lastvalue + newvalue) / 2;
                }
            } else {
                value = 0;
            }
            lastvalue = newvalue;
            pennerStats.valuechanged = false;
        }
        f.format("%3.0f", value);
        if (!pennerConnection.isServerError()) {
            pennerStats.setStatus(resbundle.getString("server_error"));
        } else if (pennerConnection.LogedIn) {
            pennerStats.setStatus(resbundle.getString("logedin"));
        }
        StatusEffectivityLabel.setText(String.valueOf(f.toString()) + " % (" + String.valueOf(gC) + "/" + String.valueOf(lT) + ")");
        StatusTextLabel.setText(pennerStats.Status);
    }

    private void initGUI(JFrame mainwindow) {
        try {
            mainwindow.setSize(580, 525);
            {
                jTabbedPane1 = new JTabbedPane();
                jTabbedPane1.setBounds(5, 210, 570, 265);
                {
                    jPanel1 = new JPanel(null);
                    jTabbedPane1.addTab(resbundle.getString("Bottles"), null, jPanel1, null);
                    {
                        CollectGroup = new JPanel(null);
                        CollectGroup.setBounds(0, 0, 275, 220);
                        CollectGroup.setBorder(BorderFactory.createTitledBorder(resbundle.getString("Collect")));
                        {
                            CollectPanel = new JPanel(null);
                            CollectPanel.setBounds(5, 15, 265, 200);
                            {
                                jLabel1 = new JLabel();
                                jLabel1.setBounds(5, 35, 250, 25);
                                CollectPanel.add(jLabel1);
                                jLabel1.setText("Status: deaktiviert");
                                jLabel1.setHorizontalAlignment(0);
                            }
                            {
                                jProgressBar1 = new JProgressBar(0, collectTime[CollTime]);
                                jProgressBar1.setBounds(20, 80, 220, 25);
                                jProgressBar1.setString("-/-");
                                jProgressBar1.setStringPainted(true);
                                CollectPanel.add(jProgressBar1);
                            }
                            {
                                CollectToggleButton1 = new JToggleButton();
                                CollectToggleButton1.setBounds(5, 150, 250, 25);
                                CollectPanel.add(CollectToggleButton1);
                                CollectToggleButton1.setText(resbundle.getString(sCollectAction));
                                CollectToggleButton1.setHorizontalAlignment(0);
                            }
                            CollectToggleButton1.addActionListener(new ActionListener() {

                                @SuppressWarnings("deprecation")
                                public void actionPerformed(ActionEvent event) {
                                    jProgressBar1.setMaximum(collectTime[CollTime]);
                                    if (sCollectAction == "Collect") {
                                        try {
                                            collectActivity = new CollectActivity(collectTime[CollTime], pennerConnection, pennerInfo, pennerStats, resbundle);
                                        } catch (IOException e) {
                                            pennerStats.setStatus("Sammel Fehler");
                                        }
                                        activityMonitorfinish = false;
                                        collectActivity.start();
                                        activityMonitor.start();
                                        addHint = "";
                                        sCollectAction = "Deactivate";
                                        jLabel1.setText("Status: aktiviert");
                                        CollectToggleButton1.setText(resbundle.getString(sCollectAction));
                                    } else {
                                        collectActivity.stop();
                                        activityMonitorfinish = true;
                                        sCollectAction = "Collect";
                                        addHint = " (wird beendet)";
                                        CollectToggleButton1.setText(resbundle.getString(sCollectAction));
                                    }
                                }
                            });
                            activityMonitor = new javax.swing.Timer(500, new ActionListener() {

                                public void actionPerformed(ActionEvent event) {
                                    int current = collectActivity.getCurrent();
                                    if ((current <= 0) & (activityMonitorfinish)) {
                                        jLabel1.setText("Status: deaktiviert");
                                        jProgressBar1.setValue(current);
                                        jProgressBar1.setString(int2Time(jProgressBar1.getValue()));
                                        activityMonitor.stop();
                                    } else {
                                        if (current > jProgressBar1.getMaximum()) {
                                            jProgressBar1.setMaximum(current);
                                        }
                                        if (current <= collectTime[CollTime]) {
                                            jProgressBar1.setMaximum(collectTime[CollTime]);
                                        }
                                        jProgressBar1.setValue(current);
                                        jProgressBar1.setString(int2Time(jProgressBar1.getValue()));
                                        jLabel1.setText("Status: " + collectActivity.getStatusLine() + addHint);
                                    }
                                }
                            });
                        }
                        CollectGroup.add(CollectPanel);
                    }
                    jPanel1.add(CollectGroup);
                    {
                        SellGroup = new JPanel(null);
                        SellGroup.setBounds(276, 0, 275, 220);
                        SellGroup.setBorder(BorderFactory.createTitledBorder(resbundle.getString("Sell")));
                        {
                            SellPanel = new JPanel(null);
                            SellPanel.setBounds(5, 15, 265, 200);
                            {
                                SellLabel2 = new JLabel();
                                SellLabel2.setText("minimaler Flaschenkurs: ");
                                SellLabel2.setBounds(15, 30, 175, 25);
                                SellPanel.add(SellLabel2);
                            }
                            {
                                SellLabel3 = new JLabel();
                                SellLabel3.setText("maximaler Kontostand: ");
                                SellLabel3.setBounds(15, 60, 175, 25);
                                SellPanel.add(SellLabel3);
                            }
                            {
                                SellLabel1 = new JLabel();
                                SellLabel1.setText("Status: deaktiviert");
                                SellLabel1.setBounds(20, 110, 220, 25);
                                SellLabel1.setHorizontalAlignment(0);
                                SellPanel.add(SellLabel1);
                            }
                            {
                                SellTextField1 = new JTextField();
                                SellTextField1.setText("0.18");
                                SellTextField1.setBounds(170, 30, 80, 25);
                                SellPanel.add(SellTextField1);
                            }
                            {
                                jTextField1 = new JTextField();
                                jTextField1.setText("100.00");
                                jTextField1.setBounds(170, 60, 80, 25);
                                SellPanel.add(jTextField1);
                            }
                            {
                                SellToggleButton1 = new JToggleButton();
                                SellToggleButton1.setText("Verkaufen");
                                SellToggleButton1.setBounds(5, 150, 250, 25);
                                SellToggleButton1.setHorizontalAlignment(0);
                                SellPanel.add(SellToggleButton1);
                            }
                            SellToggleButton1.addActionListener(new ActionListener() {

                                @SuppressWarnings("deprecation")
                                public void actionPerformed(ActionEvent event) {
                                    if (sSellAction == "Sell") {
                                        sellActivity = new SellActivity(pennerConnection, pennerInfo, pennerStats);
                                        sellActivity.setTargetShare((int) (Double.valueOf(SellTextField1.getText()) * 100));
                                        sellActivity.setLimit((int) (Double.valueOf(jTextField1.getText()) * 100));
                                        sellActivity.start();
                                        activitySellMonitor.start();
                                        sSellAction = "Deactivate";
                                        SellLabel1.setText("Status: aktiviert");
                                        SellToggleButton1.setText(resbundle.getString(sSellAction));
                                    } else {
                                        sellActivity.stop();
                                        activitySellMonitor.stop();
                                        sSellAction = "Sell";
                                        SellLabel1.setText("Status: deaktiviert");
                                        SellToggleButton1.setText(resbundle.getString(sSellAction));
                                    }
                                }
                            });
                            activitySellMonitor = new javax.swing.Timer(5000, new ActionListener() {

                                public void actionPerformed(ActionEvent event) {
                                    sellActivity.setTargetShare((int) (Double.valueOf(SellTextField1.getText()) * 100));
                                    sellActivity.setLimit((int) (Double.valueOf(jTextField1.getText()) * 100));
                                    SellLabel1.setText("Status: " + sellActivity.getStatusLine());
                                }
                            });
                        }
                        SellGroup.add(SellPanel);
                    }
                    jPanel1.add(SellGroup);
                }
                mainwindow.add(jTabbedPane1);
                {
                    jPanel2 = new JPanel();
                    jTabbedPane1.addTab(resbundle.getString("Study"), null, jPanel2, null);
                    {
                        DummyLabel = new JLabel();
                        DummyLabel.setText("Momentan noch nicht implementiert");
                        DummyLabel.setBounds(20, 110, 220, 25);
                        DummyLabel.setHorizontalAlignment(0);
                        jPanel2.add(DummyLabel);
                    }
                }
            }
            {
                InfoGroup = new JPanel(null);
                InfoGroup.setBounds(10, 0, 325, 205);
                infoTitle = BorderFactory.createTitledBorder(sLoginCity[cty]);
                InfoGroup.setBorder(infoTitle);
                {
                    Font pennerFont = new Font("SansSerif", Font.BOLD, 18);
                    PennerName = new JLabel();
                    PennerName.setBounds(5, 15, 315, 20);
                    PennerName.setText(pennerInfo.pennerName);
                    PennerName.setHorizontalAlignment(0);
                    PennerName.setFont(pennerFont);
                    InfoGroup.add(PennerName);
                }
                {
                    InfoPanel = new JPanel(null);
                    InfoPanel.setBounds(150, 35, 170, 165);
                    {
                        ImageIcon Cashimage = new ImageIcon("gfx" + File.separator + "cash.png");
                        CashBildLabel = new JLabel(Cashimage);
                        CashBildLabel.setBounds(5, 5, 25, 25);
                        InfoPanel.add(CashBildLabel);
                        CashLabel = new JLabel("0,00 �");
                        CashLabel.setBounds(40, 5, 150, 25);
                        CashLabel.setForeground(Color.black);
                        InfoPanel.add(CashLabel);
                        ImageIcon Beerimage = new ImageIcon("gfx" + File.separator + "bier.png");
                        BeerBildLabel = new JLabel(Beerimage);
                        BeerBildLabel.setBounds(5, 25, 25, 25);
                        InfoPanel.add(BeerBildLabel);
                        BeerLabel = new JLabel("0,00 �");
                        BeerLabel.setBounds(40, 25, 150, 25);
                        BeerLabel.setForeground(Color.black);
                        InfoPanel.add(BeerLabel);
                        ImageIcon Bookimage = new ImageIcon("gfx" + File.separator + "book.png");
                        BookBildLabel = new JLabel(Bookimage);
                        BookBildLabel.setBounds(5, 45, 22, 25);
                        InfoPanel.add(BookBildLabel);
                        BookLabel = new JLabel("-/-");
                        BookLabel.setBounds(40, 45, 150, 25);
                        BookLabel.setForeground(Color.black);
                        InfoPanel.add(BookLabel);
                        ImageIcon Attimage = new ImageIcon("gfx" + File.separator + "att.png");
                        AttBildLabel = new JLabel(Attimage);
                        AttBildLabel.setBounds(5, 65, 22, 25);
                        InfoPanel.add(AttBildLabel);
                        AttLabel = new JLabel("-/-");
                        AttLabel.setBounds(40, 65, 150, 25);
                        AttLabel.setForeground(Color.black);
                        InfoPanel.add(AttLabel);
                        ImageIcon Crapimage = new ImageIcon("gfx" + File.separator + "crap.png");
                        CrapBildLabel = new JLabel(Crapimage);
                        CrapBildLabel.setBounds(5, 85, 20, 25);
                        InfoPanel.add(CrapBildLabel);
                        CrapLabel = new JLabel("0,00 �");
                        CrapLabel.setBounds(40, 85, 150, 25);
                        CrapLabel.setForeground(Color.black);
                        InfoPanel.add(CrapLabel);
                        ImageIcon Crapsimage = new ImageIcon("gfx" + File.separator + "craps.png");
                        CrapsBildLabel = new JLabel(Crapsimage);
                        CrapsBildLabel.setBounds(5, 125, 20, 25);
                        InfoPanel.add(CrapsBildLabel);
                        CrapsLabel = new JLabel("0");
                        CrapsLabel.setBounds(40, 125, 150, 25);
                        CrapsLabel.setForeground(Color.black);
                        InfoPanel.add(CrapsLabel);
                    }
                    InfoGroup.add(InfoPanel);
                }
                {
                    PennerBildPanel = new JPanel();
                    PennerBildPanel.setBounds(5, 35, 150, 165);
                    {
                        ImageIcon image = new ImageIcon("gfx" + File.separator + "standard.jpg");
                        PennerBildLabel = new JLabel(image);
                        PennerBildPanel.add(PennerBildLabel);
                        PennerBildLabel.setVerticalTextPosition(JLabel.CENTER);
                        PennerBildLabel.setHorizontalTextPosition(JLabel.CENTER);
                        InfoGroup.add(PennerBildPanel);
                    }
                }
                mainwindow.add(InfoGroup);
            }
            {
                StatsGroup = new JPanel(null);
                StatsGroup.setBounds(340, 0, 230, 205);
                StatsGroup.setBorder(BorderFactory.createTitledBorder(resbundle.getString("Statistics")));
                {
                    StatisticsPanel = new JPanel(null);
                    StatisticsPanel.setBounds(5, 15, 220, 185);
                    {
                        CollectLabel = new JLabel(resbundle.getString("Collected") + " :");
                        CollectLabel.setBounds(5, 5, 100, 25);
                        StatisticsPanel.add(CollectLabel);
                        CollectAmountLabel = new JLabel(String.valueOf(pennerStats.collectedBottles));
                        CollectAmountLabel.setBounds(105, 5, 110, 25);
                        CollectAmountLabel.setForeground(Color.black);
                        StatisticsPanel.add(CollectAmountLabel);
                        SellLabel = new JLabel(resbundle.getString("Sold") + " :");
                        SellLabel.setBounds(5, 25, 100, 25);
                        StatisticsPanel.add(SellLabel);
                        SellAmountLabel = new JLabel(String.valueOf(pennerStats.soldBottles));
                        SellAmountLabel.setBounds(105, 25, 110, 25);
                        SellAmountLabel.setForeground(Color.black);
                        StatisticsPanel.add(SellAmountLabel);
                        EarnLabel = new JLabel(resbundle.getString("Earned") + " :");
                        EarnLabel.setBounds(5, 45, 100, 25);
                        StatisticsPanel.add(EarnLabel);
                        Formatter f = new Formatter();
                        f.format("%6.2f", pennerStats.earnedMoney);
                        EarnAmountLabel = new JLabel(String.valueOf(f.toString()) + " �");
                        EarnAmountLabel.setBounds(105, 45, 110, 25);
                        EarnAmountLabel.setForeground(Color.black);
                        StatisticsPanel.add(EarnAmountLabel);
                        StudyLabel = new JLabel(resbundle.getString("Study") + " :");
                        StudyLabel.setBounds(5, 70, 100, 25);
                        StatisticsPanel.add(StudyLabel);
                        StudyAmountLabel = new JLabel(String.valueOf(pennerStats.startedStudies));
                        StudyAmountLabel.setBounds(105, 70, 110, 25);
                        StudyAmountLabel.setForeground(Color.black);
                        StatisticsPanel.add(StudyAmountLabel);
                        PayLabel = new JLabel(resbundle.getString("Paid") + " :");
                        PayLabel.setBounds(5, 90, 100, 25);
                        StatisticsPanel.add(PayLabel);
                        f = new Formatter();
                        f.format("%6.2f", pennerStats.paidMoney);
                        PayAmountLabel = new JLabel(String.valueOf(f.toString()) + " �");
                        PayAmountLabel.setBounds(105, 90, 110, 25);
                        PayAmountLabel.setForeground(Color.black);
                        StatisticsPanel.add(PayAmountLabel);
                        ReloginLabel = new JLabel(resbundle.getString("relogincount") + " :");
                        ReloginLabel.setBounds(5, 115, 100, 25);
                        StatisticsPanel.add(ReloginLabel);
                        ReloginCountLabel = new JLabel(String.valueOf(pennerStats.reloginCount));
                        ReloginCountLabel.setBounds(105, 115, 110, 25);
                        ReloginCountLabel.setForeground(Color.black);
                        StatisticsPanel.add(ReloginCountLabel);
                        EffectivityLabel = new JLabel(resbundle.getString("effectivity") + " :");
                        EffectivityLabel.setBounds(5, 135, 100, 25);
                        StatisticsPanel.add(EffectivityLabel);
                        f = new Formatter();
                        int gC = pennerStats.goodCaptchas;
                        int lT = pennerStats.loggedInTime;
                        double value;
                        if (gC > lT) {
                            gC = lT;
                        }
                        if (pennerStats.loggedInTime > 0) {
                            value = Math.round(gC * 100 / lT);
                        } else {
                            value = 0;
                        }
                        lastvalue = value;
                        f.format("%3.0f", value);
                        StatusEffectivityLabel = new JLabel(String.valueOf(f.toString() + " %"));
                        StatusEffectivityLabel.setText(String.valueOf(f.toString()) + " % (" + String.valueOf(gC) + "/" + String.valueOf(lT) + ")");
                        StatusEffectivityLabel.setBounds(110, 135, 150, 25);
                        StatusEffectivityLabel.setForeground(Color.black);
                        StatisticsPanel.add(StatusEffectivityLabel);
                        StatusLabel = new JLabel(resbundle.getString("Status") + " :");
                        StatusLabel.setBounds(5, 155, 50, 25);
                        StatisticsPanel.add(StatusLabel);
                        StatusTextLabel = new JLabel(pennerStats.Status);
                        StatusTextLabel.setBounds(60, 155, 150, 25);
                        StatusTextLabel.setForeground(Color.black);
                        StatisticsPanel.add(StatusTextLabel);
                    }
                    StatsGroup.add(StatisticsPanel);
                }
                mainwindow.add(StatsGroup);
            }
            mainwindow.setVisible(true);
            mainwindow.setResizable(false);
        } catch (Exception e) {
            pennerStats.setStatus("GUI Fehler");
        }
    }
}
