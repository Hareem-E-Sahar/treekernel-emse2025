import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.table.*;
import java.awt.image.*;

class IconCellRenderer extends DefaultTableCellRenderer {

    protected void setValue(Object value) {
        if (value instanceof Icon) {
            setIcon((Icon) value);
        } else {
            super.setValue(value);
        }
    }
}

class WarningDialog extends JDialog implements ActionListener {

    protected JButton abortActionButton = null;

    protected JButton continueActionButton = null;

    public boolean abort = false;

    protected Component createComponents(Vector previousBugReportsData) {
        JLabel warningMsg = new JLabel("Some user(s) reported a bug after an action similar to this one.  Would you like to abort the action?");
        abortActionButton = new JButton("Abort Action");
        abortActionButton.addActionListener(this);
        continueActionButton = new JButton("Continue Action");
        continueActionButton.addActionListener(this);
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(abortActionButton);
        buttonPane.add(continueActionButton);
        JLabel previousBugReportsLabel = new JLabel("Reports filed after an action similar to this one:");
        Vector columnNames = new Vector();
        columnNames.add("Similarity Score");
        columnNames.add("Report Filed");
        columnNames.add("Before Screenshot");
        columnNames.add("After Screenshot");
        JTable table = new JTable(previousBugReportsData, columnNames);
        table.getColumnModel().getColumn(table.convertColumnIndexToView(2)).setCellRenderer(new IconCellRenderer());
        table.getColumnModel().getColumn(table.convertColumnIndexToView(3)).setCellRenderer(new IconCellRenderer());
        table.setRowHeight(200);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(table.convertColumnIndexToView(0)).setPreferredWidth(160);
        table.getColumnModel().getColumn(table.convertColumnIndexToView(1)).setPreferredWidth(160);
        table.getColumnModel().getColumn(table.convertColumnIndexToView(2)).setPreferredWidth(320);
        table.getColumnModel().getColumn(table.convertColumnIndexToView(3)).setPreferredWidth(320);
        Dimension size = table.getPreferredScrollableViewportSize();
        table.setPreferredScrollableViewportSize(new Dimension(Math.max(table.getPreferredSize().width, size.width), size.height));
        JScrollPane scrollpane = new JScrollPane(table);
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        warningMsg.setAlignmentX(0.0f);
        buttonPane.setAlignmentX(0.0f);
        previousBugReportsLabel.setAlignmentX(0.0f);
        scrollpane.setAlignmentX(0.0f);
        pane.add(warningMsg);
        pane.add(buttonPane);
        pane.add(previousBugReportsLabel);
        pane.add(scrollpane);
        return pane;
    }

    public WarningDialog(Frame frame, String caption, Vector previousBugReportsData) {
        super(frame, true);
        setTitle(caption);
        Component contents = createComponents(previousBugReportsData);
        getContentPane().add(contents, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        abort = (e.getSource() == abortActionButton);
        dispose();
    }
}

class ReportDialog extends JDialog implements ActionListener {

    protected JButton okayButton = null;

    protected JButton cancelButton = null;

    protected JCheckBox check_box = null;

    protected JCheckBox warn_check_box = null;

    public boolean abort = false;

    String questionMsg = "";

    String warnOverrideMsg = "";

    ImageIcon beforeScreenshot = null;

    ImageIcon afterScreenshot = null;

    ImageIcon noScreenshot = null;

    JLabel beforeLabel = null;

    JLabel afterLabel = null;

    JTextArea text_area = null;

    protected Component createComponents() {
        warn_check_box = new JCheckBox(warnOverrideMsg, false);
        JLabel question = new JLabel(questionMsg);
        text_area = new JTextArea(5, 40);
        JScrollPane text_area_scroll = new JScrollPane(text_area);
        check_box = new JCheckBox("Include before/after screenshots", true);
        check_box.addActionListener(this);
        JPanel beforePane = new JPanel();
        beforePane.setLayout(new BoxLayout(beforePane, BoxLayout.Y_AXIS));
        JLabel labelb = new JLabel("Before Screenshot");
        beforeLabel = new JLabel(beforeScreenshot);
        labelb.setAlignmentX(0.0f);
        beforeLabel.setAlignmentX(0.0f);
        beforePane.add(labelb);
        beforePane.add(beforeLabel);
        JPanel afterPane = new JPanel();
        JLabel labela = new JLabel("After Screenshot");
        afterLabel = new JLabel(afterScreenshot);
        labela.setAlignmentX(0.0f);
        afterLabel.setAlignmentX(0.0f);
        afterPane.setLayout(new BoxLayout(afterPane, BoxLayout.Y_AXIS));
        afterPane.add(labela);
        afterPane.add(afterLabel);
        JPanel screenshotPane = new JPanel();
        screenshotPane.setLayout(new BoxLayout(screenshotPane, BoxLayout.X_AXIS));
        screenshotPane.add(beforePane);
        screenshotPane.add(afterPane);
        okayButton = new JButton("Okay");
        okayButton.addActionListener(this);
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(okayButton);
        buttonPane.add(cancelButton);
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        warn_check_box.setAlignmentX(0.0f);
        question.setAlignmentX(0.0f);
        text_area_scroll.setAlignmentX(0.0f);
        check_box.setAlignmentX(0.0f);
        screenshotPane.setAlignmentX(0.0f);
        buttonPane.setAlignmentX(0.0f);
        pane.add(warn_check_box);
        pane.add(question);
        pane.add(text_area_scroll);
        pane.add(check_box);
        pane.add(screenshotPane);
        pane.add(buttonPane);
        return pane;
    }

    public ReportDialog(Frame frame, String caption, String warnOverrideMsg, String questionMsg, String beforeScreenshotFn, String afterScreenshotFn) {
        super(frame, true);
        setTitle(caption);
        this.questionMsg = questionMsg;
        this.warnOverrideMsg = warnOverrideMsg;
        Image beforeScreenshot = Toolkit.getDefaultToolkit().getImage(beforeScreenshotFn);
        beforeScreenshot = beforeScreenshot.getScaledInstance(320, 200, Image.SCALE_SMOOTH);
        this.beforeScreenshot = new ImageIcon(beforeScreenshot);
        Image afterScreenshot = Toolkit.getDefaultToolkit().getImage(afterScreenshotFn);
        afterScreenshot = afterScreenshot.getScaledInstance(320, 200, Image.SCALE_SMOOTH);
        this.afterScreenshot = new ImageIcon(afterScreenshot);
        Image noScreenshot = Toolkit.getDefaultToolkit().getImage("logo.jpg");
        noScreenshot = noScreenshot.getScaledInstance(320, 200, Image.SCALE_SMOOTH);
        this.noScreenshot = new ImageIcon(noScreenshot);
        Component contents = createComponents();
        getContentPane().add(contents, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okayButton || e.getSource() == cancelButton) {
            abort = (e.getSource() == cancelButton);
            dispose();
        } else if (e.getSource() == check_box) {
            System.out.println("check_box.isSelected() == " + check_box.isSelected());
            if (check_box.isSelected()) {
                beforeLabel.setIcon(beforeScreenshot);
                afterLabel.setIcon(afterScreenshot);
            } else {
                beforeLabel.setIcon(noScreenshot);
                afterLabel.setIcon(noScreenshot);
            }
        }
    }
}

public class Stabilizer implements ActionListener, WindowListener {

    static Map appIndex = new HashMap();

    static Map appId = new HashMap();

    static DefaultListModel appListModel = new DefaultListModel();

    static JList appListBox = new JList(appListModel);

    static final String LOOKANDFEEL = null;

    JButton reportBugButton = null;

    JButton reportNotBugButton = null;

    public Component createComponents() {
        reportBugButton = new JButton("Report Bug");
        reportNotBugButton = new JButton("Report Not Bug");
        reportBugButton.addActionListener(this);
        reportNotBugButton.addActionListener(this);
        JScrollPane scrollPane = new JScrollPane(appListBox);
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
        buttonPane.add(Box.createVerticalGlue());
        buttonPane.add(reportBugButton);
        buttonPane.add(Box.createVerticalGlue());
        buttonPane.add(reportNotBugButton);
        buttonPane.add(Box.createVerticalGlue());
        JPanel appListPane = new JPanel();
        appListPane.setLayout(new BoxLayout(appListPane, BoxLayout.Y_AXIS));
        JLabel monitoring = new JLabel("Monitoring:");
        monitoring.setAlignmentX(0.0f);
        scrollPane.setAlignmentX(0.0f);
        appListPane.add(monitoring);
        appListPane.add(scrollPane);
        pane.add(buttonPane);
        pane.add(appListPane);
        return pane;
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
        try {
            out.close();
            in.close();
            stdIn.close();
            skt.close();
        } catch (Exception ex) {
        }
        System.exit(0);
    }

    public void windowClosing(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }

    static void shell(String cmd) {
        try {
            System.out.println(cmd);
            Process child = Runtime.getRuntime().exec(cmd);
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    static void takeReport(String screenshotname1, String screenshotname2, String caption, String warnOverrideMsg, String msg, String cmd, String prefix) {
        if (appListBox.getSelectedIndex() == -1 && appListModel.size() > 1) {
            JOptionPane.showMessageDialog(frame, "Select a monitored application first.", "Report Bug", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int index = appListBox.getSelectedIndex();
        if (index == -1) {
            index = 0;
        }
        if (take_screenshots) {
            shell("python takescreenshot.py " + temp_dir + " " + screenshotname2);
            shell("python compressscreenshot.py " + temp_dir + " " + screenshotname1 + " before");
            shell("python compressscreenshot.py " + temp_dir + " " + screenshotname2 + " after");
        }
        Integer app_id = (Integer) appId.get(new Integer(index));
        ReportDialog r = new ReportDialog(frame, caption, warnOverrideMsg, msg, temp_dir + "/beforeScreenshot.jpg", temp_dir + "/afterScreenshot.jpg");
        if (!take_screenshots) {
            r.check_box.setSelected(false);
            r.check_box.setEnabled(false);
            r.beforeLabel.setIcon(r.noScreenshot);
            r.afterLabel.setIcon(r.noScreenshot);
        }
        r.pack();
        r.setVisible(true);
        System.out.println("w.abort=" + r.abort);
        if (!r.abort) {
            String picked = r.text_area.getText().trim();
            if (picked.equals("")) {
                picked = "[no user comment]";
            }
            picked = prefix + ":  " + picked;
            System.out.println("..." + picked + " " + app_id);
            if (!r.check_box.isSelected()) {
                try {
                    FileCopy.copy("logo.jpg", temp_dir + "/beforeScreenshot.jpg", true);
                    FileCopy.copy("logo.jpg", temp_dir + "/afterScreenshot.jpg", true);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
            out.println(cmd + " " + picked.replace('\n', '@').replace(' ', '_') + " " + app_id + " " + r.warn_check_box.isSelected());
        }
    }

    static void reportBug() {
        takeReport("OnNewEvent", "OnReportBug", "Report Bug", "Always warn so I can decide if a bug is likely or not.", "What kind of problem did you encounter?", "report_bug", "Bug");
        out.println("report_dialog_down");
    }

    static void reportNotBug() {
        takeReport("OnWarning", "OnReportNotBug", "Report Not Bug", "Never warn about contexts most similar to this one again.", "So the most recent warning was wrong?", "report_not_bug", "Not Bug");
        out.println("report_dialog_down");
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == reportBugButton) {
            System.out.println("report bug");
            reportBug();
        } else if (e.getSource() == reportNotBugButton) {
            System.out.println("report not bug");
            reportNotBug();
        }
    }

    private static void initLookAndFeel() {
        String lookAndFeel = null;
        if (LOOKANDFEEL != null) {
            if (LOOKANDFEEL.equals("Metal")) {
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("System")) {
                lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            } else if (LOOKANDFEEL.equals("Motif")) {
                lookAndFeel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
            } else if (LOOKANDFEEL.equals("GTK+")) {
                lookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            } else {
                System.err.println("Unexpected value of LOOKANDFEEL specified: " + LOOKANDFEEL);
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }
            try {
                UIManager.setLookAndFeel(lookAndFeel);
            } catch (ClassNotFoundException e) {
                System.err.println("Couldn't find class for specified look and feel:" + lookAndFeel);
                System.err.println("Did you include the L&F library in the class path?");
                System.err.println("Using the default look and feel.");
            } catch (UnsupportedLookAndFeelException e) {
                System.err.println("Can't use the specified look and feel (" + lookAndFeel + ") on this platform.");
                System.err.println("Using the default look and feel.");
            } catch (Exception e) {
                System.err.println("Couldn't get specified look and feel (" + lookAndFeel + "), for some reason.");
                System.err.println("Using the default look and feel.");
                e.printStackTrace();
            }
        }
    }

    protected static JFrame frame = null;

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    static Socket skt = null;

    static PrintWriter out = null;

    static BufferedReader in = null;

    static BufferedReader stdIn = null;

    static Stabilizer app = null;

    private static void createAndShowGUI() {
        try {
            initLookAndFeel();
            JFrame.setDefaultLookAndFeelDecorated(true);
            app = new Stabilizer();
            JFrame frame = new JFrame("Stabilizer");
            frame.addWindowListener(app);
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            Component contents = app.createComponents();
            frame.getContentPane().add(contents, BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    protected static void handleAppStart(Vector args) {
        String app_name = (String) args.get(0);
        Integer app_id = new Integer((String) args.get(1));
        System.out.println("app start " + app_name + " " + app_id);
        appListModel.addElement(app_name);
        Integer index = new Integer(appListModel.size() - 1);
        appIndex.put(app_id, index);
        appId.put(index, app_id);
    }

    protected static void handleAppStop(Vector args) {
        Integer app_id = new Integer((String) args.get(0));
        System.out.println("app stop " + app_id);
        Integer index = (Integer) appIndex.get(app_id);
        appListModel.remove(index.intValue());
        appIndex.remove(app_id);
        appId.remove(index);
        int index_value = index.intValue();
        for (Iterator it = appIndex.keySet().iterator(); it.hasNext(); ) {
            Integer id = (Integer) it.next();
            int old_index = ((Integer) appIndex.get(id)).intValue();
            if (old_index > index_value) {
                appIndex.put(id, new Integer(old_index - 1));
            }
        }
        Map appId2 = new HashMap();
        for (Iterator it = appId.keySet().iterator(); it.hasNext(); ) {
            Integer ind = (Integer) it.next();
            int ind_value = ind.intValue();
            if (ind_value > index_value) {
                appId2.put(new Integer(ind_value - 1), appId.get(ind));
            } else {
                appId2.put(new Integer(ind_value), appId.get(ind));
            }
        }
        appId = appId2;
    }

    protected static void handleFormat(Vector args) {
        String format = (String) args.get(0);
        Integer app_id = new Integer((String) args.get(1));
        Integer index = (Integer) appIndex.get(app_id);
        String appname = (String) appListModel.get(index.intValue());
        appname += " [" + format + "]";
        appListModel.set(index.intValue(), appname);
    }

    protected static void handleNearestNeighbors(Vector args) {
        System.out.println("handleNearestNeighbors not implemented yet");
    }

    protected static void handleReportNotBugOnQuit(Vector args) {
        int reply = JOptionPane.showConfirmDialog(frame, "Would you like to \"Report not Bug\" in response to the last warning?", "Report Not Bug?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (reply == 0) {
            System.out.println("report not bug");
            reportNotBug();
        }
        out.println("stop");
    }

    static Image cropImage(Image im, int x, int y, int w, int h) {
        CropImageFilter crop = new CropImageFilter(x, y, w, h);
        ImageProducer src = im.getSource();
        Image im2 = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(src, crop));
        return im2;
    }

    protected static void handleWarnUser(Vector args) {
        Integer app_id = new Integer((String) args.get(0));
        String temp_dir = (String) args.get(1);
        int report_count = (new Integer((String) args.get(2))).intValue();
        String callback = (String) args.get(args.size() - 1);
        Vector previousBugReportsData = new Vector();
        int i = 3;
        int count = 1;
        for (int report = 0; report < report_count; report++) {
            String score = (String) args.get(i);
            String info = ((String) args.get(i + 1)).replace('_', ' ');
            int x1 = (new Integer((String) args.get(i + 2))).intValue();
            int y1 = (new Integer((String) args.get(i + 3))).intValue();
            int w1 = (new Integer((String) args.get(i + 4))).intValue();
            int h1 = (new Integer((String) args.get(i + 5))).intValue();
            int x2 = (new Integer((String) args.get(i + 6))).intValue();
            int y2 = (new Integer((String) args.get(i + 7))).intValue();
            int w2 = (new Integer((String) args.get(i + 8))).intValue();
            int h2 = (new Integer((String) args.get(i + 9))).intValue();
            System.out.println("score=" + score + " info=" + info);
            Vector v = new Vector();
            v.add(score);
            v.add(info);
            Image beforeScreenshot = Toolkit.getDefaultToolkit().getImage(temp_dir + "/before_screenshot_" + count + ".jpg");
            if (x1 == -1 && y1 == -1 && w1 == -1 && h1 == -1) {
            } else {
                beforeScreenshot = cropImage(beforeScreenshot, x1, y1, w1, h1);
            }
            beforeScreenshot = beforeScreenshot.getScaledInstance(320, 200, Image.SCALE_SMOOTH);
            Image afterScreenshot = Toolkit.getDefaultToolkit().getImage(temp_dir + "/after_screenshot_" + count + ".jpg");
            if (x2 == -1 && y2 == -1 && w2 == -1 && h2 == -1) {
            } else {
                afterScreenshot = cropImage(afterScreenshot, x2, y2, w2, h2);
            }
            afterScreenshot = afterScreenshot.getScaledInstance(320, 200, Image.SCALE_SMOOTH);
            v.add(new ImageIcon(beforeScreenshot));
            v.add(new ImageIcon(afterScreenshot));
            previousBugReportsData.add(v);
            i += 10;
            count += 1;
        }
        WarningDialog w = new WarningDialog(frame, "Warning: " + callback, previousBugReportsData);
        w.pack();
        w.setVisible(true);
        System.out.println("w.abort=" + w.abort);
        if (!w.abort) {
            JOptionPane.showMessageDialog(frame, "The action will now be continued.  If it turns out to be ok, please \"Report Not Bug\"", "Continue Action", JOptionPane.INFORMATION_MESSAGE);
        }
        if (w.abort) {
            out.println("abort");
        } else {
            out.println("proceed");
        }
    }

    static int port = 0;

    static String temp_dir = "";

    static boolean take_screenshots = false;

    public static void main(String[] a) {
        port = (new Integer(a[0])).intValue();
        System.out.println("Port " + port);
        try {
            skt = new Socket("localhost", port);
            out = new PrintWriter(skt.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
            out.println("app __GUI__");
            String line = in.readLine().trim();
            System.out.println("start line=" + line);
            StringTokenizer st = new StringTokenizer(line);
            temp_dir = st.nextToken();
            take_screenshots = st.nextToken().equals("True");
            createAndShowGUI();
            while (true) {
                System.out.println("waiting for input from client");
                line = in.readLine().trim();
                System.out.println("line=" + line);
                st = new StringTokenizer(line);
                if (st.countTokens() == 0) {
                    continue;
                }
                String cmd = st.nextToken().toLowerCase();
                Vector args = new Vector();
                while (st.hasMoreTokens()) {
                    args.addElement(st.nextToken());
                }
                if (cmd.equals("app_start")) {
                    handleAppStart(args);
                } else if (cmd.equals("format")) {
                    handleFormat(args);
                } else if (cmd.equals("app_stop")) {
                    handleAppStop(args);
                } else if (cmd.equals("warn_user")) {
                    handleWarnUser(args);
                } else if (cmd.equals("report_bug_on_shortcut")) {
                    reportBug();
                } else if (cmd.equals("report_not_bug_on_shortcut")) {
                    reportNotBug();
                } else if (cmd.equals("report_not_bug_on_quit?")) {
                    handleReportNotBugOnQuit(args);
                } else if (cmd.equals("nearest_neighbors")) {
                    handleNearestNeighbors(args);
                } else {
                    System.out.println("Unrecognized command " + cmd + ".");
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
