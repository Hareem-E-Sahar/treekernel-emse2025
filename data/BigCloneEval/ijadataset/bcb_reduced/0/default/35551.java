import java.awt.Color;
import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import net.sf.compositor.AppAction;
import net.sf.compositor.AppMac;
import net.sf.compositor.util.CommandLineParser;
import net.sf.compositor.util.Config;
import net.sf.compositor.util.Env;
import net.sf.compositor.util.Info;
import net.sf.compositor.util.ResourceLoader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Garbage collection stats viewer. This is intended to be less unpleasant than gcviewer.
 */
public class ShowGc extends AppMac {

    private static final String CONFIG_FILE_NAME = Env.USER_HOME + Env.FILE_SEP + "showgc.properties";

    private static final String VERSION = "0.7";

    private static final String RECENT_FILE_PREFIX = "recent.file.";

    private static final Config CONFIG = new Config();

    private static final Pattern LAST_PATTERN = Pattern.compile("(?x)                 #Comments and whitespace    \n" + "(?:[-0-9.T:+]+:\\ )? #Timestamp (ignored, sorry) \n" + "([0-9.]+):\\         #Start time                 \n" + ".*                   #whatever                   \n");

    private static final Pattern CG_PATTERN = Pattern.compile("(?x)                             #Comments and whitespace                 \n" + "(?:[-0-9.T:+]+:\\ )?             #Timestamp (ignored, sorry)              \n" + "([0-9.]+):\\                     #Start time           group 1            \n" + "\\[                              #Open bracket                            \n" + "  (?:                                                                     \n" + "    (?:Full\\ )?                                                          \n" + "    GC(?:--)?\\                                                           \n" + "    (?:\\(System\\)\\ )?                                                  \n" + "    (?:[0-9.]+:\\ )?                                                      \n" + "  )                              #Type                 ignored            \n" + "  (?:                                                                     \n" + "    \\[                                                                   \n" + "      (?:DefNew(?:\\ \\(promotion\\ failed\\)\\ )?|PSYoungGen)            \n" + "      :\\                                                                 \n" + "      ([0-9]+)                   #                      group 2           \n" + "      K->                                                                 \n" + "      ([0-9]+)                   #                      group 3           \n" + "      K\\(                                                                \n" + "        ([0-9]+)                 #                      group 4           \n" + "      K\\)                                                                \n" + "      (?:                                                                 \n" + "        ,\\ [0-9.]+\\ secs                                                \n" + "      )?                                                                  \n" + "    \\]                                                                   \n" + "  )?                             #DefNew               optional           \n" + "  (?:[0-9.]+:)?                  #Start time  ignored  optional           \n" + "  (?:\\ )?                       #Space       ignored  optional           \n" + "  (?:                                                                     \n" + "    \\[                                                                   \n" + "      (?:Tenured|PSOldGen)                                                \n" + "      :\\                                                                 \n" + "      ([0-9]+)                   #                     group 5            \n" + "      K->                                                                 \n" + "      ([0-9]+)                   #                     group 6            \n" + "      K\\(                                                                \n" + "        ([0-9]+)                 #                     group 7            \n" + "      K\\)                                                                \n" + "      (?:                                                                 \n" + "        ,\\ [0-9.]+\\ secs                                                \n" + "      )?                                                                  \n" + "    \\]                                                                   \n" + "  )?                             #Tenured              optional           \n" + "  \\ ?                                                                    \n" + "  ([0-9]+)                       #                     group 8            \n" + "  K->                                                                     \n" + "  ([0-9]+)                       #                     group 9            \n" + "  K\\(                                                                    \n" + "    ([0-9]+)                     #                     group 10           \n" + "  K\\),?\\                       #Total stats          required           \n" + "  (?:                                                                     \n" + "    \\[                                                                   \n" + "      (?:Perm|PSPermGen)\\ ?:\\                                           \n" + "      ([0-9]+)                   #                     group 11           \n" + "      K->                                                                 \n" + "      ([0-9]+)                   #                     group 12           \n" + "      K\\(                                                                \n" + "        ([0-9]+)K                #                     group 13           \n" + "      \\)                                                                 \n" + "    \\],\\                                                                \n" + "  )?                             #Perm                 optional           \n" + "  ([0-9.]+)\\ secs               #Elapsed              required group 14  \n" + "\\]                              #Close bracket                           \n" + "(?:.*?)?                         #Trailing guff        ignored            \n");

    private String m_mainTitle;

    private String m_fileName = "";

    private DefaultListModel m_unknowns = new DefaultListModel();

    private DefaultListModel m_recents = new DefaultListModel();

    private AppAction m_monitor;

    private MonitorThread m_monitorThread;

    private boolean m_showNew = true;

    private boolean m_showOld = true;

    private boolean m_showPerm = true;

    private boolean m_showTotal = true;

    private boolean m_showTimes = true;

    private boolean m_packed;

    private String m_scale;

    public JFrame x_main;

    public JPanel x_main_graph;

    public JLabel x_main_statusBar;

    public JDialog x_unknown;

    public JList x_unknown_list;

    public JDialog x_recent;

    public JList x_recent_files;

    public JDialog x_editRecent;

    public JTextArea x_editRecent_files;

    public JDialog x_options;

    public JCheckBox x_options_new;

    public JCheckBox x_options_old;

    public JCheckBox x_options_perm;

    public JCheckBox x_options_total;

    public JCheckBox x_options_times;

    public JRadioButton x_options_days;

    public JRadioButton x_options_hours;

    public JRadioButton x_options_minutes;

    public static void main(final String[] args) {
        try {
            CONFIG.load(CONFIG_FILE_NAME);
        } catch (final FileNotFoundException e) {
        } catch (final IOException e) {
            msgBox(null, "Could not load config file: " + e, "Error", ERROR_MESSAGE);
            System.exit(3);
        }
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                try {
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(CONFIG_FILE_NAME);
                        CONFIG.store(fos, "Saved by ShowGC " + VERSION);
                    } finally {
                        if (null != fos) fos.close();
                    }
                } catch (final IOException x) {
                    s_log.error("Problem saving config file " + CONFIG_FILE_NAME + " - " + x);
                }
            }
        });
        final ShowGc showGc = new ShowGc();
        if (0 < args.length) {
            if ("-p".equals(args[0])) {
                if (2 < args.length) {
                    showGc.invokeLater("loadFile", new Object[] { args[1], Boolean.TRUE });
                    showGc.invokeLater("saveAs", args[2]);
                    showGc.invokeLater("doExit");
                }
            } else {
                showGc.invokeLater("loadFile", args[0]);
            }
        }
    }

    protected ShowGc() {
        runAfterUiBuilt(new Runnable() {

            public void run() {
                m_mainTitle = x_main.getTitle();
                m_monitor = getAction("monitor");
                x_unknown_list.setModel(m_unknowns);
                x_recent_files.setModel(m_recents);
                m_scale = null == System.getProperty("scale") ? "seconds" : System.getProperty("scale");
            }
        });
    }

    public void writeStatus(final String message) {
        x_main_statusBar.setText(message);
    }

    public void doOpen() {
        final JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new FileFilter() {

            public boolean accept(final File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".log");
            }

            public String getDescription() {
                return "Log files";
            }
        });
        if (chooser.showOpenDialog(x_main) == JFileChooser.APPROVE_OPTION) {
            loadFile(chooser.getSelectedFile().getAbsolutePath(), true);
        }
    }

    public void doReload() {
        loadFile(m_fileName, false);
    }

    public void loadFile(final String filename, final boolean autoScale) {
        if (null == filename) {
            x_main.setTitle(m_mainTitle);
            return;
        }
        try {
            x_main.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            synchronized (m_fileName) {
                if (!filename.equals(m_fileName)) {
                    m_fileName = filename;
                    interruptMonitor();
                }
            }
            BufferedReader in = null;
            try {
                if (autoScale) autoScale(filename);
                final GcParser parser = new GcParser();
                final List<List<String>> stats = new LinkedList<List<String>>();
                final XYSeries defNew = new XYSeries("DefNew");
                final XYSeries defNewHeap = new XYSeries("DefNew Heap");
                final XYSeries tenured = new XYSeries("Tenured");
                final XYSeries tenuredHeap = new XYSeries("Tenured Heap");
                final XYSeries perm = new XYSeries("Perm");
                final XYSeries permHeap = new XYSeries("Perm Heap");
                final XYSeries total = new XYSeries("Total");
                final XYSeries totalHeap = new XYSeries("Total Heap");
                final XYSeries totalTime = new XYSeries("Total GC time");
                in = new BufferedReader(new FileReader(filename));
                m_unknowns.clear();
                for (String line = null; null != (line = in.readLine()); ) {
                    final List<String> row = parser.parse(line);
                    if (null != row) stats.add(parser.parse(line));
                }
                addRecentFile(filename);
                getAction("unknown").setEnabled(0 < m_unknowns.size());
                getAction("reload").setEnabled(true);
                getAction("monitor").setEnabled(true);
                getAction("editor").setEnabled(true);
                getAction("options").setEnabled(true);
                getAction("saveAs").setEnabled(true);
                for (final List<String> row : stats) {
                    final double seconds = Double.parseDouble(row.get(0));
                    final double minutes = seconds / 60.0;
                    final double hours = minutes / 60.0;
                    final double days = hours / 24.0;
                    final double time;
                    if ("days".equals(m_scale)) time = days; else if ("hours".equals(m_scale)) time = hours; else if ("minutes".equals(m_scale)) time = minutes; else time = seconds;
                    if (m_showNew && null != row.get(1)) {
                        defNew.add(time, parseMb(row, 1));
                        defNew.add(time, parseMb(row, 2));
                        defNewHeap.add(time, parseMb(row, 3));
                    }
                    if (m_showOld && null != row.get(4)) {
                        tenured.add(time, parseMb(row, 4));
                        tenured.add(time, parseMb(row, 5));
                        tenuredHeap.add(time, parseMb(row, 6));
                    }
                    if (m_showTotal) {
                        total.add(time, parseMb(row, 7));
                        total.add(time, parseMb(row, 8));
                        totalHeap.add(time, parseMb(row, 9));
                    }
                    if (m_showPerm && null != row.get(10)) {
                        perm.add(time, parseMb(row, 10));
                        perm.add(time, parseMb(row, 11));
                        permHeap.add(time, parseMb(row, 12));
                    }
                    if (m_showTimes) {
                        totalTime.add(time, Double.parseDouble(row.get(13)));
                    }
                }
                final XYSeriesCollection dataset1 = new XYSeriesCollection();
                final XYSeriesCollection dataset2 = new XYSeriesCollection();
                int oldIndex = 2;
                int totalIndex = 4;
                int permIndex = 6;
                if (m_showNew) {
                    dataset1.addSeries(defNew);
                    dataset1.addSeries(defNewHeap);
                } else {
                    oldIndex -= 2;
                    totalIndex -= 2;
                    permIndex -= 2;
                }
                if (m_showOld) {
                    dataset1.addSeries(tenured);
                    dataset1.addSeries(tenuredHeap);
                } else {
                    totalIndex -= 2;
                    permIndex -= 2;
                }
                if (m_showTotal) {
                    dataset1.addSeries(total);
                    dataset1.addSeries(totalHeap);
                } else {
                    permIndex -= 2;
                }
                if (m_showPerm) {
                    dataset1.addSeries(perm);
                    dataset1.addSeries(permHeap);
                }
                if (m_showTimes) {
                    dataset2.addSeries(totalTime);
                }
                final JFreeChart chart = ChartFactory.createXYLineChart("GC stats", m_scale, "Megabytes", dataset1, PlotOrientation.VERTICAL, true, true, false);
                final XYPlot plot = chart.getXYPlot();
                final NumberAxis axis2 = new NumberAxis("Seconds");
                plot.setRangeAxis(1, axis2);
                plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
                plot.setDataset(1, dataset2);
                plot.mapDatasetToRangeAxis(1, 1);
                plot.setRenderer(1, new XYLineAndShapeRenderer());
                plot.getRenderer(1).setSeriesPaint(0, Color.GRAY);
                final XYItemRenderer renderer = plot.getRenderer(0);
                if (m_showNew) {
                    renderer.setSeriesPaint(0, new Color(0x116611));
                    renderer.setSeriesPaint(1, new Color(0x11cc11));
                }
                if (m_showOld) {
                    renderer.setSeriesPaint(oldIndex, new Color(0x999911));
                    renderer.setSeriesPaint(oldIndex + 1, new Color(0xffff11));
                }
                if (m_showTotal) {
                    renderer.setSeriesPaint(totalIndex, new Color(0x661111));
                    renderer.setSeriesPaint(totalIndex + 1, new Color(0xcc1111));
                }
                if (m_showPerm) {
                    renderer.setSeriesPaint(permIndex, new Color(0x111166));
                    renderer.setSeriesPaint(permIndex + 1, new Color(0x1111cc));
                }
                x_main_graph.removeAll();
                x_main_graph.add(new ChartPanel(chart));
                if (!m_packed) {
                    x_main.pack();
                    m_packed = true;
                } else {
                    x_main_graph.validate();
                }
            } catch (final IOException f) {
                msgBox("Could not read file \"" + filename + "\"\n\n" + f, ERROR_MESSAGE);
            } finally {
                quietlyCloseReader(in);
            }
            x_main.setTitle(m_mainTitle + " - " + filename);
            final File f = new File(filename);
            writeStatus("Log file date: " + new Date(f.lastModified()) + ". size " + f.length());
        } finally {
            x_main.setCursor(null);
        }
    }

    private void quietlyCloseReader(final BufferedReader reader) {
        if (null != reader) try {
            reader.close();
        } catch (final IOException x) {
            x.printStackTrace();
        }
    }

    private void autoScale(final String filename) throws IOException {
        BufferedReader in = null;
        double endTime = 0;
        try {
            in = new BufferedReader(new FileReader(filename));
            for (String line; null != (line = in.readLine()); ) {
                final Matcher m = LAST_PATTERN.matcher(line);
                if (m.matches()) {
                    try {
                        endTime = Double.parseDouble(m.group(1));
                    } catch (final NumberFormatException x) {
                    }
                }
            }
        } finally {
            quietlyCloseReader(in);
        }
        if (endTime > 86400) m_scale = "days"; else if (endTime > 3600) m_scale = "hours"; else if (endTime > 60) m_scale = "minutes"; else m_scale = "seconds";
    }

    private void addRecentFile(final String addFilename) {
        final List<String> filenames = new ArrayList<String>();
        filenames.add(addFilename);
        for (int i = 0; ; ) {
            final String thisFilename = CONFIG.getProperty(RECENT_FILE_PREFIX + ++i);
            if (null == thisFilename) break;
            if (!thisFilename.equals(addFilename)) filenames.add(thisFilename);
        }
        CONFIG.clear();
        for (int i = 0; i < filenames.size(); ) {
            final String thisFilename = filenames.get(i);
            CONFIG.setProperty(RECENT_FILE_PREFIX + ++i, thisFilename);
        }
    }

    private static double parseMb(final List<String> row, final int col) {
        return Double.parseDouble(row.get(col)) / 1024d;
    }

    private class GcParser {

        private List<String> parse(final String line) {
            final List<String> result = new ArrayList<String>();
            final Matcher m = CG_PATTERN.matcher(line);
            if (!m.matches()) {
                m_unknowns.addElement(line);
                return null;
            }
            for (int i = 1; i <= 14; i++) {
                result.add(m.group(i));
            }
            return result;
        }
    }

    public void doAbout() {
        final String homePage = System.getProperty("ShowGc.home.page");
        new Info(x_main, "About " + getAppName(), new String[][] { { "What?", "Garbage collection stats viewer", Info.NO_COLON, Info.BOLD }, { "How?", "Start java with these options:", Info.NO_COLON }, { "", "-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:somefilename", Info.NO_COLON }, { "", "Then open the resulting file in this app.", Info.NO_COLON }, { Info.SPACER }, { "Note", "Doesn't work correctly with -XX:+UseConcMarkSweepGC." }, { Info.SPACER }, { "Params", "You can make " + getAppName() + " write a PNG image from the command line:" }, { "", "java -classpath showgc.jar;jcommon-1.0.16.jar;jfreechart-1.0.13.jar ShowGc -p <gclog> <imagefile>", Info.NO_COLON }, { Info.SPACER }, { "Version", VERSION }, { "Updates", null == homePage ? "http://sourceforge.net/projects/compositor/" : homePage, Info.LINK } }, new Info.Extras("Showing \"About\" box...", this, null, getSysProps(), ResourceLoader.getIcon("international_tidyman.png")));
    }

    public void doUnknown() {
        fixDialog(x_unknown, x_main);
    }

    public void unknown_ok_onPress() {
        x_unknown.dispose();
    }

    public void doRecent() {
        m_recents.clear();
        for (final String fileName : getRecentFileNames()) m_recents.addElement(fileName);
        x_recent_files.setSelectedIndex(0);
        fixDialog(x_recent, x_main);
    }

    private List<String> getRecentFileNames() {
        final List<String> result = new LinkedList<String>();
        for (int i = 1; ; i++) {
            final String f = CONFIG.getProperty(RECENT_FILE_PREFIX + i);
            if (null == f) break;
            result.add(f);
        }
        return result;
    }

    public void recent_files_onDoubleClick() {
        recent_open_onPress();
    }

    public void recent_open_onPress() {
        loadFile((String) m_recents.get(x_recent_files.getSelectedIndex()), true);
        x_recent.setVisible(false);
    }

    public void recent_edit_onPress() {
        final StringBuilder buf = new StringBuilder();
        for (final String fileName : getRecentFileNames()) buf.append(fileName).append(Env.NL);
        x_editRecent_files.setText(buf.toString());
        fixDialog(x_editRecent, x_recent);
    }

    public void editRecent_ok_onPress() {
        final String[] lines = x_editRecent_files.getText().split("\n");
        x_editRecent.setVisible(false);
        x_recent.setVisible(false);
        for (int i = 1; ; i++) {
            final String key = RECENT_FILE_PREFIX + i;
            if (!CONFIG.containsKey(key)) break;
            CONFIG.remove(key);
        }
        for (int i = 1; i <= lines.length; i++) {
            CONFIG.setProperty(RECENT_FILE_PREFIX + i, lines[i - 1].trim());
        }
        doRecent();
    }

    public void editRecent_cancel_onPress() {
        x_editRecent.setVisible(false);
    }

    public void recent_cancel_onPress() {
        x_recent.setVisible(false);
    }

    private class MonitorThread extends Thread {

        private String m_monitoredFileName;

        private MonitorThread(final String fileName) {
            m_monitoredFileName = fileName;
        }

        public void run() {
            final File file = new File(m_monitoredFileName);
            try {
                long fileMod = file.lastModified();
                long fileLen = file.length();
                while (m_monitor.isChecked() && !interrupted()) {
                    s_log.verbose("Monitor sleeping for " + m_monitoredFileName);
                    try {
                        Thread.sleep(10000L);
                    } catch (final InterruptedException x) {
                        s_log.verbose("Monitor interrupted for " + m_monitoredFileName);
                        return;
                    }
                    s_log.verbose("Monitor waking for " + m_monitoredFileName);
                    synchronized (m_fileName) {
                        final long newFileMod = file.lastModified();
                        final long newFileLen = file.length();
                        if (!m_fileName.equals(m_monitoredFileName)) return;
                        s_log.verbose("Checking " + m_monitoredFileName + ": " + new Date(newFileMod) + '>' + new Date(fileMod) + ' ' + (newFileMod > fileMod));
                        s_log.verbose("   ...or " + m_monitoredFileName + ": " + newFileLen + '>' + fileLen + ' ' + (newFileLen > fileLen));
                        if (newFileMod > fileMod || newFileLen > fileLen) {
                            fileMod = newFileMod;
                            fileLen = newFileLen;
                            s_log.verbose("Reloading...");
                            invokeLater("doReload");
                        }
                    }
                }
            } finally {
                s_log.verbose("Monitor closing for " + m_monitoredFileName);
                m_monitor.setChecked(false);
            }
        }
    }

    public void doEditor() {
        try {
            Runtime.getRuntime().exec(CommandLineParser.parse("cmd /c start \"Opening editor...\" \"" + m_fileName + '"'));
        } catch (final IOException x) {
            msgBox(x_main, "Oops: " + x);
        }
    }

    public void doMonitor() {
        m_monitor.setChecked(!m_monitor.isChecked());
        if (m_monitor.isChecked()) {
            m_monitorThread = new MonitorThread(m_fileName);
            m_monitorThread.start();
        } else {
            interruptMonitor();
        }
    }

    private void interruptMonitor() {
        if (null != m_monitorThread) {
            s_log.verbose("Interrupting monitor thread...");
            m_monitorThread.interrupt();
        }
    }

    public void doOptions() {
        fixDialog(x_options, x_main);
    }

    public void options_ok_onPress() {
        m_showNew = x_options_new.isSelected();
        m_showOld = x_options_old.isSelected();
        m_showPerm = x_options_perm.isSelected();
        m_showTotal = x_options_total.isSelected();
        m_showTimes = x_options_times.isSelected();
        if (x_options_days.isSelected()) m_scale = "days"; else if (x_options_hours.isSelected()) m_scale = "hours"; else if (x_options_minutes.isSelected()) m_scale = "minutes"; else m_scale = "seconds";
        x_options.setVisible(false);
        doReload();
    }

    public void options_cancel_onPress() {
        x_options.dispose();
    }

    public void doSaveAs() {
        final JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new FileFilter() {

            public boolean accept(final File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
            }

            public String getDescription() {
                return "PNG files";
            }
        });
        if (chooser.showSaveDialog(x_main) == JFileChooser.APPROVE_OPTION) {
            saveAs(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public void saveAs(final String fileName) {
        final String fileNameWithExt = fileName.toLowerCase().endsWith(".png") ? fileName : fileName + ".png";
        final File file = new File(fileNameWithExt);
        final ChartPanel panel = (ChartPanel) x_main_graph.getComponent(0);
        final JFreeChart chart = panel.getChart();
        try {
            ChartUtilities.saveChartAsPNG(file, chart, panel.getWidth(), panel.getHeight());
        } catch (final IOException x) {
            throw new RuntimeException("Could not save chart: " + x, x);
        }
    }
}
