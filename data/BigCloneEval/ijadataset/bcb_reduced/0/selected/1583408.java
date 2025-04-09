package de.kopis.jusenet.ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.hibernate.Session;
import org.hibernate.exception.JDBCConnectionException;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import de.kopis.jusenet.Application;
import de.kopis.jusenet.BuildVersion;
import de.kopis.jusenet.nntp.Article;
import de.kopis.jusenet.nntp.Group;
import de.kopis.jusenet.nntp.NNTPUtils;
import de.kopis.jusenet.nntp.exceptions.NntpNotConnectedException;
import de.kopis.jusenet.ui.menu.ArticlePopupMenu;
import de.kopis.jusenet.ui.menu.GroupPopupMenu;
import de.kopis.jusenet.ui.models.ArticleListModel;
import de.kopis.jusenet.ui.models.GroupListModel;
import de.kopis.jusenet.ui.renderer.ArticleCellRenderer;
import de.kopis.jusenet.ui.renderer.GroupCellRenderer;
import de.kopis.jusenet.utils.GuiUtils;
import de.kopis.jusenet.utils.HibernateUtils;
import de.kopis.utils.GlassPane;
import de.kopis.utils.SimpleGlassPane;

public class MainFrame extends JFrame {

    private static final long serialVersionUID = 9073977055564263060L;

    private static final int UPDATE_INTERVAL = 60 * 10 * 1000;

    private JTabbedPane groupTabs;

    private JList articleList;

    private JList allGroupList;

    private JTextArea articleText;

    private StatusBar statusbar;

    private JSplitPane groupSplitPane;

    private JSplitPane articleSplitPane;

    private JList newGroupList;

    private JList subscribedGroupList;

    private Timer updateTimer;

    public MainFrame() {
        super(BuildVersion.getTitle() + " " + BuildVersion.getVersion());
        Application.getInstance().setMainframe(this);
        createGUI();
        String tmp;
        int x, y, width, height;
        tmp = Application.getInstance().getProperty("window.x", "10");
        x = Integer.parseInt(tmp);
        tmp = Application.getInstance().getProperty("window.y", "10");
        y = Integer.parseInt(tmp);
        tmp = Application.getInstance().getProperty("window.width", "400");
        width = Integer.parseInt(tmp);
        tmp = Application.getInstance().getProperty("window.height", "300");
        height = Integer.parseInt(tmp);
        setBounds(x, y, width, height);
        initTimer();
        statusbar.setAction("Ready.");
    }

    /**
     * Initialise update timer to get new articles from subscribed Groups.
     *
     */
    private void initTimer() {
        updateTimer = new Timer(Integer.parseInt(Application.getInstance().getProperty("updateinterval", "" + UPDATE_INTERVAL)), new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                GroupListModel model = (GroupListModel) subscribedGroupList.getModel();
                for (int i = 0; i < model.getSize(); i++) {
                    Group g = (Group) model.getElementAt(i);
                    try {
                        NNTPUtils.getInstance().getArticles(g, g.getLastUpdate());
                    } catch (NntpNotConnectedException e1) {
                        GuiUtils.showError(e1);
                    }
                }
                System.out.println(new Date() + ": Subscribed Groups updated.");
            }
        });
        updateTimer.start();
    }

    private void createGUI() {
        setLayout(new BorderLayout());
        setGlassPane(new SimpleGlassPane("Click to unlock"));
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                Application.getInstance().quit(0);
            }
        });
        createMenu();
        createToolbar();
        ArticleListModel articlemodel = new ArticleListModel();
        articleList = new JList(articlemodel);
        articleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        articleList.setCellRenderer(new ArticleCellRenderer());
        articleList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                Article a = (Article) articleList.getSelectedValue();
                openArticle(a);
            }
        });
        articleList.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (articleList.getSelectedValue() != null) {
                        ArticlePopupMenu popup = new ArticlePopupMenu();
                        popup.setLocation(e.getPoint());
                        popup.show((Component) e.getSource(), e.getX(), e.getY());
                    }
                }
            }
        });
        NNTPUtils.getInstance().addArticleListener(articlemodel);
        articleText = new JTextArea();
        JPanel panel = new JPanel(new BorderLayout());
        groupSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(articleList), new JScrollPane(articleText));
        groupSplitPane.setDividerLocation(Integer.parseInt(Application.getInstance().getProperty("window.groupsplit", "10")));
        panel.add(groupSplitPane, BorderLayout.CENTER);
        createGroupTabs();
        articleSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, groupTabs, panel);
        articleSplitPane.setDividerLocation(Integer.parseInt(Application.getInstance().getProperty("window.articlesplit", "10")));
        add(articleSplitPane, BorderLayout.CENTER);
        setGroupDividerLocation(Integer.parseInt(Application.getInstance().getProperty("window.groupsplit", "100")));
        setArticleDividerLocation(Integer.parseInt(Application.getInstance().getProperty("window.articlesplit", "100")));
        statusbar = new StatusBar();
        add(statusbar, BorderLayout.SOUTH);
    }

    private void createGroupTabs() {
        groupTabs = new JTabbedPane();
        GroupListModel allgroupmodel = new GroupListModel();
        GroupListModel newgroupmodel = new GroupListModel();
        GroupListModel subscribedgroupmodel = new GroupListModel();
        try {
            Iterator<Group> it = HibernateUtils.listGroups().iterator();
            Group g;
            while (it.hasNext()) {
                g = it.next();
                allgroupmodel.addElement(g);
                if (g.isSubscribed()) {
                    subscribedgroupmodel.addElement(g);
                }
            }
        } catch (JDBCConnectionException e) {
            System.err.println("Cannot open connection to database.");
            System.err.println("Maybe you restarted to fast? Let HSQLDB recover from last session.");
            Application.getInstance().quit(Application.ERROR_CONNECTION_NOT_READY);
        }
        NNTPUtils.getInstance().addGroupListener(newgroupmodel);
        NNTPUtils.getInstance().addGroupListener(allgroupmodel);
        allGroupList = new JList(allgroupmodel);
        allGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        allGroupList.setCellRenderer(new GroupCellRenderer());
        allGroupList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                Group g = (Group) allGroupList.getSelectedValue();
                if (g == null) return;
                displayArticles(g.getId());
            }
        });
        allGroupList.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (allGroupList.getSelectedValue() != null) {
                        GroupPopupMenu popup = new GroupPopupMenu();
                        popup.setLocation(e.getPoint());
                        popup.show((Component) e.getSource(), e.getX(), e.getY());
                    }
                }
            }
        });
        groupTabs.addTab("All", new JScrollPane(allGroupList));
        newGroupList = new JList(newgroupmodel);
        newGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        newGroupList.setCellRenderer(new GroupCellRenderer());
        newGroupList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                Group g = (Group) newGroupList.getSelectedValue();
                if (g == null) return;
                displayArticles(g.getId());
            }
        });
        newGroupList.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (newGroupList.getSelectedValue() != null) {
                        GroupPopupMenu popup = new GroupPopupMenu();
                        popup.setLocation(e.getPoint());
                        popup.show((Component) e.getSource(), e.getX(), e.getY());
                    }
                }
            }
        });
        groupTabs.addTab("New", new JScrollPane(newGroupList));
        subscribedGroupList = new JList(subscribedgroupmodel);
        subscribedGroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subscribedGroupList.setCellRenderer(new GroupCellRenderer());
        subscribedGroupList.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) return;
                final Group g = (Group) subscribedGroupList.getSelectedValue();
                if (g == null) return;
                displayArticles(g.getId());
            }
        });
        subscribedGroupList.addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (subscribedGroupList.getSelectedValue() != null) {
                        GroupPopupMenu popup = new GroupPopupMenu();
                        popup.setLocation(e.getPoint());
                        popup.show((Component) e.getSource(), e.getX(), e.getY());
                    }
                }
            }
        });
        groupTabs.addTab("Subscribed", new JScrollPane(subscribedGroupList));
    }

    private void displayArticles(final Long gid) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Session session = HibernateUtils.getSessionFactory().getCurrentSession();
                session.beginTransaction();
                Group g = (Group) session.load(Group.class, gid);
                statusbar.setAction("Entering " + g + "...");
                ArticleListModel model = (ArticleListModel) articleList.getModel();
                model.clear();
                Iterator it = g.getArticles().iterator();
                while (it.hasNext()) {
                    model.addElement(it.next());
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        GuiUtils.showError(e);
                    }
                }
                session.getTransaction().commit();
                articleText.setText(null);
                statusbar.setAction("Articles for " + g + " loaded.");
            }
        });
    }

    private void openArticle(Article a) {
        if (a == null) return;
        if (!a.getRead()) {
            HibernateUtils.markRead(a, true);
        }
        StringBuffer buf = new StringBuffer();
        if (Boolean.parseBoolean(Application.getInstance().getProperty("gui.displayheaders", "true"))) {
            String[] headers = a.getHeaders();
            for (int i = 0; i < headers.length; i++) {
                buf.append(headers[i] + "\n");
            }
            buf.append("\n\n");
        }
        buf.append(a.getText());
        buf.append("\n\n-- \n");
        buf.append(a.getSignature());
        articleText.setText(buf.toString());
        articleText.setCaretPosition(0);
        statusbar.setAction("Displaying " + a);
    }

    public void lock() {
        try {
            Robot robot = new Robot();
            Rectangle bounds = getRootPane().getBounds();
            BufferedImage screen = robot.createScreenCapture(new Rectangle(getX() + bounds.x, getY() + bounds.y, getWidth(), getHeight()));
            ((GlassPane) getGlassPane()).setScreen(screen);
        } catch (AWTException e) {
            GuiUtils.showError(e);
        }
        getGlassPane().setVisible(true);
    }

    private void createToolbar() {
        JToolBar bar = new JToolBar("main");
        bar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
        bar.putClientProperty(PlasticLookAndFeel.IS_3D_KEY, Boolean.TRUE);
        bar.setRollover(true);
        bar.add(Application.getInstance().getAction("newarticle"));
        bar.addSeparator();
        bar.add(Application.getInstance().getAction("cut"));
        bar.add(Application.getInstance().getAction("copy"));
        bar.add(Application.getInstance().getAction("paste"));
        bar.addSeparator();
        bar.add(Application.getInstance().getAction("connect"));
        bar.add(Application.getInstance().getAction("disconnect"));
        bar.add(Application.getInstance().getAction("editserver"));
        bar.addSeparator();
        bar.add(Application.getInstance().getAction("find"));
        bar.add(Application.getInstance().getAction("getmessageid"));
        bar.addSeparator();
        bar.add(Application.getInstance().getAction("help"));
        bar.addSeparator();
        bar.add(Application.getInstance().getAction("exit"));
        add(bar, BorderLayout.NORTH);
    }

    private void createMenu() {
        JMenuBar bar = new JMenuBar();
        bar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
        bar.putClientProperty(PlasticLookAndFeel.IS_3D_KEY, Boolean.TRUE);
        JMenu menu;
        menu = new JMenu("File");
        menu.setMnemonic('F');
        menu.addSeparator();
        menu.add(new JMenuItem(Application.getInstance().getAction("exit")));
        bar.add(menu);
        menu = new JMenu("Server");
        menu.setMnemonic('S');
        menu.add(new JMenuItem(Application.getInstance().getAction("connect")));
        menu.add(new JMenuItem(Application.getInstance().getAction("disconnect")));
        menu.add(new JMenuItem(Application.getInstance().getAction("loadgroups")));
        menu.add(new JMenuItem(Application.getInstance().getAction("newgroups")));
        menu.addSeparator();
        menu.add(new JMenuItem(Application.getInstance().getAction("getmessageid")));
        menu.addSeparator();
        menu.add(new JMenuItem(Application.getInstance().getAction("editserver")));
        bar.add(menu);
        menu = new JMenu("Group");
        menu.setMnemonic('G');
        menu.add(new JMenuItem(Application.getInstance().getAction("subscribe")));
        menu.add(new JMenuItem(Application.getInstance().getAction("unsubscribe")));
        menu.addSeparator();
        menu.add(new JMenuItem(Application.getInstance().getAction("updategroup")));
        bar.add(menu);
        menu = new JMenu("Article");
        menu.setMnemonic('A');
        menu.add(new JMenuItem(Application.getInstance().getAction("markread")));
        menu.add(new JMenuItem(Application.getInstance().getAction("markunread")));
        bar.add(menu);
        menu = new JMenu("?");
        menu.setMnemonic('?');
        menu.add(new JMenuItem(Application.getInstance().getAction("help")));
        menu.addSeparator();
        menu.add(new JMenuItem(Application.getInstance().getAction("lock")));
        menu.addSeparator();
        menu.add(new JMenuItem(Application.getInstance().getAction("about")));
        bar.add(menu);
        setJMenuBar(bar);
    }

    public int getArticleDividerLocation() {
        return articleSplitPane.getDividerLocation();
    }

    private void setArticleDividerLocation(int loc) {
        articleSplitPane.setDividerLocation(loc);
    }

    public int getGroupDividerLocation() {
        return groupSplitPane.getDividerLocation();
    }

    private void setGroupDividerLocation(int loc) {
        groupSplitPane.setDividerLocation(loc);
    }

    public String getArticleSubject(int id) throws SQLException {
        String subject = null;
        return subject;
    }

    public void composeMessage() {
        ComposeMessageWindow cmw;
        Group g = (Group) allGroupList.getSelectedValue();
        if (g != null) {
            cmw = new ComposeMessageWindow(g);
        } else {
            cmw = new ComposeMessageWindow();
        }
        cmw.setLocationRelativeTo(this);
        cmw.setVisible(true);
    }

    public void newGroups() {
        Date date;
        try {
            date = SimpleDateFormat.getDateInstance().parse(Application.getInstance().getProperty("nntp.lastupdate", SimpleDateFormat.getDateInstance().format(new Date())));
            NNTPUtils.getInstance().newGroups(date);
            date = new Date();
            Application.getInstance().setProperty("nntp.lastupdate", SimpleDateFormat.getDateInstance().format(date));
        } catch (ParseException e) {
            GuiUtils.showError(e);
        } catch (NntpNotConnectedException e) {
            GuiUtils.showError(e);
        }
    }

    /**
     * Gets new articles for currently selected group.
     *
     */
    public void getNewArticles() {
        Group g = (Group) allGroupList.getSelectedValue();
        statusbar.setAction("Updating " + g);
        System.out.println("Getting new articles for " + g);
        try {
            NNTPUtils.getInstance().getArticles(g, g.getLastUpdate());
        } catch (NntpNotConnectedException e) {
            GuiUtils.showError(e);
        }
    }

    /**
     * Removes an Article from database.
     *
     */
    public void removeArticle() {
        Article a = (Article) articleList.getSelectedValue();
        HibernateUtils.remove(a);
    }

    /**
     * Marks an article read/unread.
     * 
     * @param read
     */
    public void markread(boolean read) {
        Article a = (Article) articleList.getSelectedValue();
        if (a == null) {
            GuiUtils.showError("No article selected.");
        } else {
            HibernateUtils.markRead(a, read);
        }
    }

    public void editServer() {
        SelectServerWindow window = new SelectServerWindow(this);
        window.setVisible(true);
    }

    public void subscribe() {
        Group g = null;
        if (allGroupList.isShowing()) {
            g = (Group) allGroupList.getSelectedValue();
        } else if (newGroupList.isShowing()) {
            g = (Group) newGroupList.getSelectedValue();
        } else if (subscribedGroupList.isShowing()) {
            g = (Group) subscribedGroupList.getSelectedValue();
        }
        if (g != null) {
            HibernateUtils.subscribe(g, true);
            ((GroupListModel) subscribedGroupList.getModel()).addElement(g);
        }
    }

    public void unsubscribe() {
        Group g = null;
        if (allGroupList.isShowing()) {
            g = (Group) allGroupList.getSelectedValue();
        } else if (newGroupList.isShowing()) {
            g = (Group) newGroupList.getSelectedValue();
        } else if (subscribedGroupList.isShowing()) {
            g = (Group) subscribedGroupList.getSelectedValue();
        }
        if (g != null) {
            HibernateUtils.subscribe(g, false);
            ((GroupListModel) subscribedGroupList.getModel()).removeElement(g);
        }
    }
}
