package org.adempiere.apps.graph;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import org.compiere.apps.AEnv;
import org.compiere.apps.AWindow;
import org.compiere.model.MAchievement;
import org.compiere.model.MGoal;
import org.compiere.model.MMeasureCalc;
import org.compiere.model.MProjectType;
import org.compiere.model.MQuery;
import org.compiere.model.MRequestType;
import org.compiere.model.MRole;
import org.compiere.swing.CMenuItem;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Ini;
import org.compiere.util.Msg;

/**
 * @author fcsku
 *
 */
public class HtmlDashboard extends JPanel implements MouseListener, ActionListener, HyperlinkListener {

    private static Dimension paneldimensionMin = new Dimension(80, 80);

    private JEditorPane html;

    private enum PAGE_TYPE {

        PAGE_HOME, PAGE_PERFORMANCE, PAGE_LOGO
    }

    ;

    private static CLogger log = CLogger.getCLogger(HtmlDashboard.class);

    MGoal[] m_goals = null;

    JPopupMenu popupMenu = new JPopupMenu();

    private CMenuItem mRefresh = new CMenuItem(Msg.getMsg(Env.getCtx(), "Refresh"), Env.getImageIcon("Refresh16.gif"));

    URL lastUrl = null;

    /**
	 * 	Constructor
	 */
    public HtmlDashboard(String url, MGoal[] m_goals, boolean scrolling) {
        super();
        setName("test title");
        this.setLayout(new BorderLayout());
        this.m_goals = m_goals;
        JEditorPane.registerEditorKitForContentType("text/html", "org.adempiere.apps.graph.FCHtmlEditorKit");
        html = new JEditorPane();
        html.setContentType("text/html");
        html.setEditable(false);
        htmlUpdate(url);
        JScrollPane scrollPane = null;
        if (scrolling) scrollPane = new JScrollPane(); else scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().add(html, BorderLayout.CENTER);
        this.add(scrollPane, BorderLayout.CENTER);
        this.setMinimumSize(paneldimensionMin);
        addMouseListener(this);
        html.addHyperlinkListener(this);
        mRefresh.addActionListener(this);
        popupMenu.add(mRefresh);
        html.addMouseListener(this);
        html.setBackground(getBackground());
    }

    public HtmlDashboard(String url) {
        new HtmlDashboard(url, null, true);
    }

    private String createHTML(PAGE_TYPE requestPage) {
        String result = "<html><head>";
        URL url = getClass().getClassLoader().getResource("org/compiere/images/PAPanel.css");
        InputStreamReader ins;
        try {
            ins = new InputStreamReader(url.openStream());
            BufferedReader bufferedReader = new BufferedReader(ins);
            String cssLine;
            while ((cssLine = bufferedReader.readLine()) != null) result += cssLine + "\n";
        } catch (IOException e1) {
            log.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
        }
        switch(requestPage) {
            case PAGE_LOGO:
                result += "</head><body class=\"header\">" + "<table width=\"100%\"><tr><td>" + "<img src=\"res:org/compiere/images/logo_ad.png\">" + "</td><td></td><td width=\"290\">" + "</td></tr></table>" + "</body></html>";
                break;
            case PAGE_HOME:
                result += "</head><body><div class=\"content\">\n";
                queryZoom = null;
                queryZoom = new ArrayList<MQuery>();
                String appendToHome = null;
                String sql = " SELECT x.AD_CLIENT_ID, x.NAME, x.DESCRIPTION, x.AD_WINDOW_ID, x.PA_GOAL_ID, x.LINE, x.HTML, m.AD_MENU_ID" + " FROM PA_DASHBOARDCONTENT x" + " LEFT OUTER JOIN AD_MENU m ON x.ad_window_id=m.ad_window_id" + " WHERE (x.AD_Client_ID=0 OR x.AD_Client_ID=?) AND x.IsActive='Y'" + " ORDER BY LINE";
                PreparedStatement pstmt = null;
                ResultSet rs = null;
                try {
                    pstmt = DB.prepareStatement(sql, null);
                    pstmt.setInt(1, Env.getAD_Client_ID(Env.getCtx()));
                    rs = pstmt.executeQuery();
                    while (rs.next()) {
                        appendToHome = rs.getString("HTML");
                        if (appendToHome != null) {
                            if (rs.getString("DESCRIPTION") != null) result += "<H2>" + rs.getString("DESCRIPTION") + "</H2>\n";
                            result += stripHtml(appendToHome, false) + "<br>\n";
                        }
                        if (rs.getInt("AD_MENU_ID") > 0) {
                            result += "<a class=\"hrefNode\" href=\"http:///window/node#" + String.valueOf(rs.getInt("AD_WINDOW_ID") + "\">" + rs.getString("DESCRIPTION") + "</a><br>\n");
                        }
                        result += "<br>\n";
                        if (rs.getInt("PA_GOAL_ID") > 0) result += goalsDetail(rs.getInt("PA_GOAL_ID"));
                    }
                } catch (SQLException e) {
                    log.log(Level.SEVERE, sql, e);
                } finally {
                    DB.close(rs, pstmt);
                    rs = null;
                    pstmt = null;
                }
                result += "<br><br><br>\n" + "</div>\n</body>\n</html>\n";
                break;
            default:
                log.warning("Unknown option - " + requestPage);
        }
        return result;
    }

    ArrayList<MQuery> queryZoom = null;

    private String goalsDetail(int AD_Table_ID) {
        String output = "";
        if (m_goals == null) return output;
        for (int i = 0; i < m_goals.length; i++) {
            MMeasureCalc mc = MMeasureCalc.get(Env.getCtx(), m_goals[i].getMeasure().getPA_MeasureCalc_ID());
            if (AD_Table_ID == m_goals[i].getPA_Goal_ID()) {
                output += "<table class=\"dataGrid\"><tr>\n<th colspan=\"3\" class=\"label\"><b>" + m_goals[i].getName() + "</b></th></tr>\n";
                output += "<tr><td class=\"label\">Target</td><td colspan=\"2\" class=\"tdcontent\">" + m_goals[i].getMeasureTarget() + "</td></tr>\n";
                output += "<tr><td class=\"label\">Actual</td><td colspan=\"2\" class=\"tdcontent\">" + m_goals[i].getMeasureActual() + "</td></tr>\n";
                BarGraph barPanel = new BarGraph(m_goals[i]);
                BarGraphColumn[] bList = barPanel.getBarGraphColumnList();
                MQuery query = null;
                output += "<tr><td rowspan=\"" + bList.length + "\" class=\"label\" valign=\"top\">" + m_goals[i].getXAxisText() + "</td>\n";
                for (int k = 0; k < bList.length; k++) {
                    BarGraphColumn bgc = bList[k];
                    if (k > 0) output += "<tr>";
                    if (bgc.getAchievement() != null) {
                        MAchievement a = bgc.getAchievement();
                        query = MQuery.getEqualQuery("PA_Measure_ID", a.getPA_Measure_ID());
                    } else if (bgc.getGoal() != null) {
                        MGoal goal = bgc.getGoal();
                        query = MQuery.getEqualQuery("PA_Measure_ID", goal.getPA_Measure_ID());
                    } else if (bgc.getMeasureCalc() != null) {
                        mc = bgc.getMeasureCalc();
                        query = mc.getQuery(m_goals[i].getRestrictions(false), bgc.getMeasureDisplay(), bgc.getDate(), MRole.getDefault());
                    } else if (bgc.getProjectType() != null) {
                        MProjectType pt = bgc.getProjectType();
                        query = pt.getQuery(m_goals[i].getRestrictions(false), bgc.getMeasureDisplay(), bgc.getDate(), bgc.getID(), MRole.getDefault());
                    } else if (bgc.getRequestType() != null) {
                        MRequestType rt = bgc.getRequestType();
                        query = rt.getQuery(m_goals[i].getRestrictions(false), bgc.getMeasureDisplay(), bgc.getDate(), bgc.getID(), MRole.getDefault());
                    }
                    output += "<td class=\"tdcontent\">" + bgc.getLabel() + "</td><td  class=\"tdcontent\">";
                    if (query != null) {
                        output += "<a class=\"hrefZoom\" href=\"http:///window/zoom#" + queryZoom.size() + "\">" + bgc.getValue() + "</a><br>\n";
                        queryZoom.add(query);
                    } else {
                        log.info("Nothing to zoom to - " + bgc);
                        output += bgc.getValue();
                    }
                    output += "</td></tr>";
                }
                output += "</tr>" + "<tr><td colspan=\"3\">" + m_goals[i].getDescription() + "<br>" + stripHtml(m_goals[i].getColorSchema().getDescription(), true) + "</td></tr>" + "</table>\n";
                bList = null;
                barPanel = null;
            }
        }
        return output;
    }

    private String stripHtml(String htmlString, boolean all) {
        htmlString = htmlString.replace("<html>", "").replace("</html>", "").replace("<body>", "").replace("</body>", "").replace("<head>", "").replace("</head>", "");
        if (all) htmlString = htmlString.replace(">", "&gt;").replace("<", "&lt;");
        return htmlString;
    }

    private void htmlUpdate(String url) {
        try {
            htmlUpdate(new URL(url));
        } catch (MalformedURLException e) {
            log.warning("Malformed URL: " + e);
        }
    }

    private void htmlUpdate(URL url) {
        if ((url == null) || (url.getPath().equals("/local/home"))) {
            html.setText(createHTML(PAGE_TYPE.PAGE_HOME));
            html.setCaretPosition(0);
            lastUrl = url;
        } else if (url.getPath().equals("/local/logo")) {
            html.setText(createHTML(PAGE_TYPE.PAGE_LOGO));
            html.setCaretPosition(0);
            lastUrl = url;
        } else if (url.getPath().equals("/local/performance")) {
            html.setText(createHTML(PAGE_TYPE.PAGE_PERFORMANCE));
        } else if (url.getPath().equals("/window/node")) {
            int AD_Window_ID = Integer.parseInt(url.getRef());
            AWindow frame = new AWindow();
            if (!frame.initWindow(AD_Window_ID, null)) return;
            AEnv.addToWindowManager(frame);
            if (Ini.isPropertyBool(Ini.P_OPEN_WINDOW_MAXIMIZED)) {
                AEnv.showMaximized(frame);
            } else {
                AEnv.showCenterScreen(frame);
            }
            frame = null;
            html.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        } else if (url.getPath().equals("/window/zoom")) {
            int index = Integer.parseInt(String.valueOf(url.getRef()));
            if ((index >= 0) && (index < queryZoom.size())) {
                html.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                AEnv.zoom(queryZoom.get(index));
                html.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        } else if (url != null) {
            Cursor cursor = html.getCursor();
            html.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            SwingUtilities.invokeLater(new PageLoader(html, url, cursor));
            lastUrl = url;
        }
    }

    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            htmlUpdate(event.getURL());
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) popupMenu.show((Component) e.getSource(), e.getX(), e.getY());
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == mRefresh) {
            if (m_goals != null) for (int i = 0; i < m_goals.length; i++) m_goals[i].updateGoal(true);
            htmlUpdate(lastUrl);
            Container parent = getParent();
            if (parent != null) parent.invalidate();
            invalidate();
            if (parent != null) parent.repaint(); else repaint();
        }
    }

    class PageLoader implements Runnable {

        private JEditorPane html;

        private URL url;

        private Cursor cursor;

        PageLoader(JEditorPane html, URL url, Cursor cursor) {
            this.html = html;
            this.url = url;
            this.cursor = cursor;
        }

        public void run() {
            if (url == null) {
                html.setCursor(cursor);
                Container parent = html.getParent();
                parent.repaint();
            } else {
                Document doc = html.getDocument();
                try {
                    html.setPage(url);
                } catch (IOException ioe) {
                    html.setDocument(doc);
                } finally {
                    url = null;
                    SwingUtilities.invokeLater(this);
                }
            }
        }
    }
}
