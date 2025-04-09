package de.schwarzrot.install.app;

import java.applet.Applet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * used to launch the real installer application.
 * 
 * @author <a href="mailto:rmantey@users.sourceforge.net">Reinhard Mantey</a>
 */
public class AppLauncher extends Applet {

    public static final String INSTALL_DIR = "va-install";

    private static final String APPINFO = "\nVDRAssistant: Copyright (c) 2007-2011 Reinhard Mantey\n\n" + "This program comes with ABSOLUTELY NO WARRANTY;\n" + "This is free software, and you are welcome to redistribute " + "it under certain conditions.";

    private static boolean doHeadlessTest = false;

    private static final String KEY_TMP_DIR = "java.io.tmpdir";

    private static final long serialVersionUID = 713L;

    public static void cleanupInstall() {
        File instDir = new File(System.getProperty(KEY_TMP_DIR), INSTALL_DIR);
        removeDirectory(instDir);
        System.exit(0);
    }

    /**
     * @param args
     *            <ul>
     *            <li>cleanup - remove installation directory from last install</li>
     *            <li>dump - show bunch of infos about current machine</li>
     *            <li>force - just skip background services (is default now, as
     *            background services will be installed by bash-installer)</li>
     *            </ul>
     */
    public static void main(String[] args) {
        boolean skipBackgroundServices = false;
        boolean tryJREonly = false;
        boolean wantLogging = false;
        boolean skipDBValidation = false;
        AppLauncher.doHeadlessTest = false;
        System.err.println(APPINFO);
        System.err.println();
        Pattern appParam = Pattern.compile("^--?(\\S+)");
        for (int i = 0; i < args.length; i++) {
            Matcher m = appParam.matcher(args[i]);
            if (m.matches()) {
                String param = m.group(1);
                if (param.compareToIgnoreCase("help") == 0 || param.compareTo("?") == 0) {
                    usage();
                    System.exit(0);
                }
                if (param.compareToIgnoreCase("cleanup") == 0) {
                    cleanupInstall();
                    System.exit(0);
                }
                if (param.compareToIgnoreCase("charset") == 0) {
                    dumpCharsets();
                    System.exit(0);
                }
                if (param.compareToIgnoreCase("dump") == 0) {
                    dumpSystem();
                    System.exit(0);
                }
                if (param.compareToIgnoreCase("network") == 0) {
                    tellNetwork();
                    System.exit(0);
                }
                if (param.compareToIgnoreCase("log") == 0) {
                    wantLogging = true;
                }
                if (param.compareToIgnoreCase("skipDBValidation") == 0) {
                    System.err.println("gonna skip database validation!");
                    skipDBValidation = true;
                }
                if (param.compareTo("force") == 0) {
                    skipBackgroundServices = true;
                    tryJREonly = true;
                }
            }
        }
        if (!tryJREonly) {
            File jhSys = new File(System.getProperty("java.home"));
            if (jhSys.getName().compareToIgnoreCase("jre") == 0) jhSys = jhSys.getParentFile();
            File tmp = new File(jhSys, "include");
            if (!(tmp.exists() && tmp.isDirectory()) && (System.getProperty("os.name").compareTo("Linux") == 0 || skipBackgroundServices)) {
                System.err.println("\nWARNING: your java system does NOT look like a JDK!\n" + "If you like to use background services of VDRAssistant,\n" + "please install a real (sun-)Jdk or use -force");
                System.exit(-1);
            }
        }
        try {
            javax.swing.SwingUtilities.invokeLater(new InstallLauncher(doHeadlessTest, wantLogging, skipBackgroundServices, skipDBValidation));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static boolean removeDirectory(File dir) {
        boolean rv = false;
        if (dir != null && dir.exists() && dir.isDirectory()) {
            for (File cur : dir.listFiles()) {
                if (cur.isDirectory()) removeDirectory(cur);
                try {
                    cur.delete();
                } catch (Throwable t) {
                }
            }
            try {
                dir.delete();
            } catch (Throwable t) {
            }
        }
        return rv;
    }

    protected static void dumpCharsets() {
        System.err.println("available charsets for java applications follows:");
        System.err.println("\n\n-----------------------------------------------------------------------------------");
        System.err.println("\tavailable charsets for java applications follows ...");
        System.err.println("-----------------------------------------------------------------------------------");
        for (String cur : Charset.availableCharsets().keySet()) System.err.print(cur + "\t");
        System.err.println();
    }

    protected static void dumpSystem() {
        Properties props = System.getProperties();
        System.err.println("\n\n-----------------------------------------------------------------------------------");
        System.err.println("\tSystem properties look like ...");
        System.err.println("-----------------------------------------------------------------------------------");
        for (Object key : props.keySet()) {
            System.err.println(String.format("%30s==> %s", key, props.getProperty((String) key)));
        }
        System.err.println();
        dumpCharsets();
        System.err.println("\n\n-----------------------------------------------------------------------------------");
        System.err.println("\tSystem environment looks like ...");
        System.err.println("-----------------------------------------------------------------------------------");
        Map<String, String> env = System.getenv();
        for (String key : env.keySet()) {
            System.err.println(String.format("%30s==> %s", key, env.get(key)));
        }
        Enumeration<NetworkInterface> niList;
        System.err.println("\n\n-----------------------------------------------------------------------------------");
        System.err.println("\tnetwork interfaces as seen by java ...");
        System.err.println("-----------------------------------------------------------------------------------");
        try {
            niList = NetworkInterface.getNetworkInterfaces();
            while (niList.hasMoreElements()) {
                NetworkInterface ni = niList.nextElement();
                System.err.println("\n* * * * * * * * * * * * * * * * * *");
                System.err.println("network interface: " + ni.getName());
                System.err.println("has display name.: " + ni.getDisplayName());
                System.err.println("has HW-address...: " + ni.getHardwareAddress());
                if (ni.isLoopback()) System.err.println(" ... is loopback interface");
                if (ni.isUp()) System.err.println(" ... is up");
                if (ni.isVirtual()) System.err.println(" ... is virtual");
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    System.err.println("   address.......: " + addr.getHostAddress());
                    System.err.println("      hostname...: " + addr.getCanonicalHostName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static void tellNetwork() {
        InetAddress networkAddress = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback()) continue;
                if (!ni.isUp()) continue;
                if (ni.isVirtual()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address) networkAddress = address;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (networkAddress == null) System.out.println("Sorry, could not determine active network interface!"); else System.out.println(networkAddress.getHostAddress() + " " + networkAddress.getCanonicalHostName());
    }

    protected static void usage() {
        System.out.println("this is the installer for the GUI of VdrAssistant.\n" + "The installer works interactively and needs no parameters.\n" + "These options are supported for system investigation:\n");
        System.out.println("--help              the text you are reading\n" + "-?                  same as help\n" + "--dump              dump informations about your system\n" + "--charset           dumps a list of available charsets\n" + "--log               enable logging of installer\n" + "                    (put a log.properties beside installer)\n" + "--force             try installation on jre-only systems\n" + "--skipDBValidation  don't check database for schema update");
    }
}

final class InstallLauncher implements Runnable {

    private static final String KEY_INSTALL = "SRJInstaller.jar";

    private static final String KEY_JAVA_HOME = "java.home";

    private static final String KEY_LICENSE = "COPYING";

    private static final String KEY_MY_JAR = "java.class.path";

    private static final String KEY_README = "READ.ME";

    private static final String KEY_SILENCE = "silence.mp2";

    private static final String KEY_TMP_DIR = "java.io.tmpdir";

    private static final String KEY_VA_MAIN = "VdrAssistant.jar";

    private static final String KEY_VA_ICON = "VdrAssistant.ico";

    private static final String KEY_WHELPER = "createDesktopLink.vbs";

    public InstallLauncher(boolean doHeadlessTest, boolean wantLogging, boolean skipBackgroundServices, boolean skipDBValidation) {
        this.doHeadlessTest = doHeadlessTest;
        this.wantLogging = wantLogging;
        this.skipDBValidation = skipDBValidation;
        this.skipBackgroundServices = skipBackgroundServices;
    }

    @Override
    public void run() {
        System.out.println();
        File jvm = new File(System.getProperty(KEY_JAVA_HOME), "bin/java");
        File wd = null;
        File launcherFile = null;
        try {
            wd = new File(System.getProperty(KEY_TMP_DIR), AppLauncher.INSTALL_DIR);
            launcherFile = new File(System.getProperty(KEY_MY_JAR));
            JarFile jf = new JarFile(launcherFile.getAbsoluteFile());
            File work = new File(wd, "ext");
            Enumeration<JarEntry> entries = jf.entries();
            wd.mkdirs();
            work.mkdirs();
            while (entries.hasMoreElements()) {
                JarEntry cur = entries.nextElement();
                work = new File(wd, cur.getName());
                if (cur.getName().startsWith("lib") || cur.getName().startsWith("ext") || cur.getName().startsWith("sample") || cur.getName().startsWith("db")) {
                    if (cur.isDirectory()) {
                        work.mkdirs();
                    } else {
                        extractEntry(jf, cur, work);
                    }
                } else if (isWindows() && (cur.getName().endsWith(KEY_WHELPER) || cur.getName().endsWith(KEY_VA_ICON))) {
                    extractEntry(jf, cur, work);
                } else if (cur.getName().endsWith(KEY_INSTALL) || cur.getName().endsWith(KEY_README) || cur.getName().endsWith(KEY_LICENSE) || cur.getName().endsWith(KEY_SILENCE) || cur.getName().endsWith(KEY_VA_MAIN)) {
                    extractEntry(jf, cur, work);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (wd != null && wd.isDirectory() && launcherFile != null) {
            try {
                StringBuilder sb = new StringBuilder(jvm.getAbsolutePath());
                if (doHeadlessTest) sb.append(" -Djava.awt.headless=true");
                sb.append(" -Djava.util.prefs.syncInterval=1 ");
                if (wantLogging) {
                    File logConfig = new File(wd, "log.properties");
                    PrintWriter out = new PrintWriter(logConfig);
                    out.println("#");
                    out.println("# logging for SRAppFrame");
                    out.println("#");
                    out.println("handlers = java.util.logging.FileHandler, java.util.logging.ConsoleHandler");
                    out.println(".level = INFO");
                    out.println("java.util.logging.FileHandler.pattern = " + wd.getAbsolutePath() + "/vaiGUI.log");
                    out.println("java.util.logging.FileHandler.limit = 2000000");
                    out.println("java.util.logging.FileHandler.count = 10");
                    out.println("java.util.logging.FileHandler.formatter = de.schwarzrot.logging.SRSingleFormatter");
                    out.println("# Limit the message that are printed on the console to INFO and above.");
                    out.println("java.util.logging.ConsoleHandler.level = INFO");
                    out.println("java.util.logging.ConsoleHandler.formatter = de.schwarzrot.logging.SRFormatter");
                    out.println("org.springframework.level = SEVERE");
                    out.println("com.jgoodies.level = WARNING");
                    out.close();
                    if (logConfig.length() > 0) {
                        sb.append(" -Djava.util.logging.config.file=");
                        sb.append(logConfig.getAbsolutePath());
                    }
                }
                sb.append(" -jar ");
                sb.append(KEY_INSTALL);
                sb.append(" ");
                sb.append(launcherFile.getAbsolutePath());
                if (wantLogging) {
                    sb.append(" develop");
                    sb.append(" --logger org.apache.commons.logging.impl.Jdk14Logger");
                }
                if (skipBackgroundServices) {
                    sb.append(" ");
                    sb.append("--no-background-services");
                }
                if (skipDBValidation) {
                    sb.append(" ");
                    sb.append("--no-database-validation");
                }
                if (wantLogging) {
                    System.err.println("\nworking directory is: [" + wd.getAbsolutePath() + "]");
                    System.err.println("start installer with: [" + sb.toString() + "]");
                }
                Runtime.getRuntime().exec(sb.toString(), null, wd);
            } catch (Throwable e) {
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            System.err.println("something very bad happened. Please contact your system administrator!");
        }
        System.out.println();
        System.out.flush();
    }

    protected void extractEntry(JarFile jf, JarEntry cur, File work) {
        try {
            InputStream is = jf.getInputStream(cur);
            OutputStream os = new FileOutputStream(work);
            byte[] buf = new byte[4096];
            int n = 0;
            while ((n = is.read(buf)) > 0) {
                os.write(buf, 0, n);
            }
            is.close();
            os.close();
            System.out.print(".");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean isWindows() {
        String check = System.getProperty("os.name");
        return check.startsWith("Windows");
    }

    private boolean doHeadlessTest = false;

    private boolean skipBackgroundServices = false;

    private boolean skipDBValidation = false;

    private boolean wantLogging = false;
}

;
