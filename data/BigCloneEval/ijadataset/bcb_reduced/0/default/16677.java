import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import net.sf.compositor.App;
import net.sf.compositor.AppMac;
import net.sf.compositor.util.CommandLineParser;
import net.sf.compositor.util.Config;
import net.sf.compositor.util.DirectoryChooser;
import net.sf.compositor.util.Env;
import net.sf.compositor.util.Info;
import net.sf.compositor.util.MessageCache;
import net.sf.compositor.util.NativeEditor;

public class Examples extends AppMac {

    private static final String s_confFileName = "examples.properties";

    private static final ExamplesConfig s_config = ExamplesConfig.getInstance();

    private static class ExamplesConfig extends Config {

        private static final ExamplesConfig s_instance = new ExamplesConfig();

        private static ExamplesConfig getInstance() {
            return s_instance;
        }
    }

    private static String s_dir = ".";

    private static String s_preselect = null;

    static {
        System.setProperty(MessageCache.USE_MESSAGE_CACHE_PROPERTY, "true");
    }

    public JFrame x_main;

    public JList x_main_list;

    public JPanel x_main_buttonHolder;

    public JButton x_main_help;

    public JButton x_main_showMore;

    public JLabel x_main_growBoxDefender;

    public JPanel x_main_more;

    public JCheckBox x_main_gclog;

    public JCheckBox x_main_showout;

    public JTextField x_main_extraParams;

    public JTextField x_main_args;

    public JButton x_main_ellipsis1;

    public JButton x_main_ellipsis2;

    public static void main(final String[] args) {
        if (0 < args.length) {
            s_dir = args[0];
            if (1 < args.length) {
                s_preselect = args[1];
            }
        }
        final String fullConfName = s_dir + Env.FILE_SEP + s_confFileName;
        try {
            s_log.verbose("Loading config from " + fullConfName);
            s_config.load(fullConfName);
        } catch (final FileNotFoundException e) {
            s_log.debug("Config file [" + fullConfName + "] not found.");
        } catch (final Exception e) {
            s_log.error("Problem reading config file [" + fullConfName + "]: " + e);
        }
        new Examples();
    }

    public void doAbout() {
        new Info(getFrame("main"), "Examples - Help", new String[][] { { " ", "Examples", Info.BOLD, Info.NO_COLON }, { " ", "Use this to explore Compositor and its", Info.NO_COLON }, { " ", "sample apps", Info.NO_COLON }, { Info.SPACER }, { " ", "Choose one and you can:", Info.NO_COLON }, { "-", "run it", Info.NO_COLON }, { "-", "examine its XML descriptor", Info.NO_COLON }, { "-", "examine its Java code", Info.NO_COLON }, { Info.SPACER }, { " ", "Change directory to use other Compositor apps.", Info.NO_COLON }, { " ", "The playarea is meant for you to try out making", Info.NO_COLON }, { " ", "your own apps - have a go with Xide for this.", Info.NO_COLON } }, new Info.Extras("All about Examples", this, null));
    }

    public void main__onShow() {
        main_showMore_onPress();
    }

    public void main__onLoad() {
        final File dir = new File(s_dir);
        final Vector<String> apps = new Vector<String>();
        final FilenameFilter filter = new FilenameFilter() {

            public boolean accept(final File dir, final String name) {
                return name.endsWith(".xml") && !name.equals("Examples.xml");
            }
        };
        for (final String fileName : dir.list(filter)) {
            apps.add(fileName.substring(0, fileName.length() - 4));
        }
        x_main_list.setListData(apps);
        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).equals(s_preselect)) {
                x_main_list.setSelectedIndex(i);
                break;
            }
        }
        setMinimumSize();
    }

    private void setMinimumSize() {
        x_main.setMinimumSize(new Dimension(x_main.getSize().width, x_main_help.getLocationOnScreen().y - x_main.getLocationOnScreen().y + x_main_help.getHeight() + x_main_showMore.getHeight() + x_main_growBoxDefender.getHeight() + 20));
    }

    public void main_list_onDoubleClick() {
        main_run_onPress();
    }

    public void main_run_onPress() {
        if (-1 == x_main_list.getSelectedIndex()) {
            msgBox(getFrame("main"), "Please select an example app.");
            return;
        }
        invokeLater("runExample", x_main_list.getSelectedValue().toString());
    }

    public void main_showMore_onPress() {
        if (-1 == x_main_buttonHolder.getComponentZOrder(x_main_more)) {
            x_main_buttonHolder.add(x_main_more);
            x_main_showMore.setText("Less <<");
        } else {
            x_main_buttonHolder.remove(x_main_more);
            x_main_showMore.setText("More >>");
        }
        x_main.setMinimumSize(null);
        x_main.pack();
        setMinimumSize();
    }

    public void main_help_onPress() {
        doAbout();
    }

    public void main_cd_onPress() {
        final JFileChooser chooser = new DirectoryChooser(new File(s_dir));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(get("main.cd").getComponent())) {
            s_dir = chooser.getSelectedFile().getAbsolutePath();
        }
        restart("");
    }

    public void main_xml_onPress() {
        if (-1 == x_main_list.getSelectedIndex()) {
            msgBox(getFrame("main"), "Please select an example app.");
            return;
        }
        try {
            NativeEditor.open(new File(s_dir + Env.FILE_SEP + x_main_list.getSelectedValue().toString() + ".xml"));
        } catch (final IOException x) {
            msgBox(getFrame("main"), "Oops: " + x);
        }
    }

    public void main_java_onPress() {
        if (-1 == x_main_list.getSelectedIndex()) {
            msgBox(getFrame("main"), "Please select an example app.");
            return;
        }
        try {
            NativeEditor.open(new File(s_dir + Env.FILE_SEP + x_main_list.getSelectedValue().toString() + ".java"));
        } catch (final IOException x) {
            msgBox(getFrame("main"), "Oops: " + x);
        }
    }

    public void runExample(final String name) {
        final String extraParams = x_main_extraParams.getText();
        final String extraArgs = x_main_args.getText();
        final List<String> args = new LinkedList<String>() {

            {
                add("java");
                if (x_main_showout.isSelected()) {
                    add("-D" + MessageCache.USE_MESSAGE_CACHE_PROPERTY + "=true");
                }
                if (null != extraParams && 0 < extraParams.length()) for (final String element : CommandLineParser.parse(extraParams)) add(element);
                if (x_main_gclog.isSelected()) {
                    add("-verbose:gc");
                    add("-XX:+PrintGCDetails");
                    add("-XX:+PrintGCTimeStamps");
                    add("-Xloggc:" + name + "_gc.log");
                }
                add("-classpath");
                add(makeClasspath(name));
                add(name);
                if (null != extraArgs && 0 < extraArgs.length()) for (final String element : CommandLineParser.parse(extraArgs)) add(element);
            }
        };
        try {
            final ProcessBuilder pb = new ProcessBuilder(args.toArray(new String[args.size()]));
            for (final String s : pb.command()) {
                s_log.verbose(s);
            }
            pb.start();
            restart(name);
        } catch (final IOException x) {
            msgBox(getFrame("main"), "Could not run " + name + " - " + x.getMessage());
        }
    }

    private void restart(final String name) {
        try {
            final ProcessBuilder pb = new ProcessBuilder("java", "-classpath", makeClasspath(), "Examples", s_dir, name);
            pb.directory(new File(s_dir));
            for (final String s : pb.command()) {
                s_log.verbose(s);
            }
            pb.start();
            invokeLater("doExit", null);
        } catch (final IOException x) {
            msgBox(getFrame("main"), "Could not restart." + Env.NL + Env.NL + x.getMessage());
        }
    }

    private static String makeClasspath() {
        return makeClasspath(null);
    }

    private static String makeClasspath(final String appName) {
        final StringBuilder result = new StringBuilder();
        for (final String s : System.getProperty("java.class.path").split(Env.PATH_SEP)) {
            try {
                result.append(new File(s).getCanonicalPath());
            } catch (final IOException x) {
                throw new RuntimeException("Could not make class path: " + x, x);
            }
            result.append(Env.PATH_SEP);
        }
        result.append(additionalClasspath(appName));
        return result.toString();
    }

    private static String additionalClasspath(final String appName) {
        final StringBuilder result = new StringBuilder();
        final String acpConf = "additional.classpath." + appName + '.';
        if (App.s_verbose) {
            s_log.verbose("User dir: " + System.getProperty("user.dir"));
            s_log.verbose("=-=-=");
            for (final Map.Entry entry : s_config.entrySet()) {
                s_log.verbose(entry.getKey() + ": " + entry.getValue());
            }
            s_log.verbose("=-=-=");
        }
        for (int i = 1; s_config.containsKey(acpConf + i); i++) {
            result.append(Env.PATH_SEP + s_config.getProperty(acpConf + i));
        }
        return result.toString();
    }

    public void main_ellipsis1_onPress() {
        getMenu("main.options").show(x_main_ellipsis1, 0, x_main_ellipsis1.getHeight());
    }

    private void addOption(final String option) {
        x_main_extraParams.setText((x_main_extraParams.getText() + ' ' + option).trim());
        x_main_extraParams.requestFocusInWindow();
    }

    public void doSplash() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {

            public boolean accept(final File f) {
                final String name = f.getName().toLowerCase();
                final int dotPos = name.lastIndexOf('.');
                final String ext = -1 == dotPos ? "" : name.substring(dotPos + 1);
                return f.isDirectory() || ext.equals("gif") || ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg");
            }

            public String getDescription() {
                return "Image files";
            }
        });
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(x_main_extraParams)) addOption("\"-splash:" + chooser.getSelectedFile().getAbsolutePath() + '"');
    }

    public void doClient() {
        addOption("-client");
    }

    public void doServer() {
        addOption("-server");
    }

    public void doXint() {
        addOption("-Xint");
    }

    public void doDnameValue() {
        addOption("-Dname=value");
    }

    public void doEnableassertions() {
        addOption("-enableassertions");
    }

    public void doXms64M() {
        addOption("-Xms64M");
    }

    public void doXmx64M() {
        addOption("-Xmx64M");
    }

    public void doXss16M() {
        addOption("-Xss16M");
    }

    public void doXprof() {
        addOption("-Xprof");
    }

    public void doXXDisableExplicitGC() {
        addOption("-XX:-DisableExplicitGC");
    }

    public void doXXUseConcMarkSweepGC() {
        addOption("-XX:-UseConcMarkSweepGC");
    }

    public void doXXUseGCOverheadLimit() {
        addOption("-XX:+UseGCOverheadLimit");
    }

    public void doXXUseParallelGC() {
        addOption("-XX:-UseParallelGC");
    }

    public void doXXUseParallelOldGC() {
        addOption("-XX:-UseParallelOldGC");
    }

    public void doXXUseSerialGC() {
        addOption("-XX:-UseSerialGC");
    }

    public void doXXCITime() {
        addOption("-XX:-CITime");
    }

    public void doXXHeapDumpPath() {
        addOption("-XX:HeapDumpPath=filename");
    }

    public void doXXHeapDumpOnOutOfMemoryError() {
        addOption("-XX:-HeapDumpOnOutOfMemoryError");
    }

    public void doXXOnError() {
        addOption("-XX:OnError=\"<cmd args>;<cmd args>\"");
    }

    public void doXXOnOutOfMemoryError() {
        addOption("-XX:OnOutOfMemoryError=\"<cmd args>; <cmd args>\"");
    }

    public void doXXPrintCommandLineFlags() {
        addOption("-XX:-PrintCommandLineFlags");
    }

    public void doXXPrintCompilation() {
        addOption("-XX:-PrintCompilation");
    }

    public void doXXPrintGC() {
        addOption("-XX:-PrintGC");
    }

    public void doXXPrintGCDetails() {
        addOption("-XX:-PrintGCDetails");
    }

    public void doXXPrintGCTimeStamps() {
        addOption("-XX:-PrintGCTimeStamps");
    }

    public void doXXPrintTenuringDistribution() {
        addOption("-XX:-PrintTenuringDistribution");
    }

    public void doXXTraceClassLoading() {
        addOption("-XX:-TraceClassLoading");
    }

    public void doXXTraceClassLoadingPreorder() {
        addOption("-XX:-TraceClassLoadingPreorder");
    }

    public void doXXTraceClassResolution() {
        addOption("-XX:-TraceClassResolution");
    }

    public void doXXTraceClassUnloading() {
        addOption("-XX:-TraceClassUnloading");
    }

    public void doXXTraceLoaderConstraints() {
        addOption("-XX:-TraceLoaderConstraints");
    }

    public void main_ellipsis2_onPress() {
        final JFileChooser chooser = new JFileChooser();
        if (JFileChooser.APPROVE_OPTION == chooser.showDialog(x_main_args, "Add")) {
            x_main_args.setText((x_main_args.getText() + " \"" + chooser.getSelectedFile().getAbsolutePath() + '"').trim());
            x_main_args.requestFocusInWindow();
        }
    }
}
