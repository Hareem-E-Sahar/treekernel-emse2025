package org.jtools.util.logging;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Configuration class for the Java Logging Framework providing System
 * Properties based configuration.
 *
 * @see java.util.logging.LogManager
 * @author Rainer Noack
 */
public class Configuration {

    /**
     * Property prefix for configuration.
     */
    public static final String PREFIX = Configuration.class.getName() + ".";

    private static final String j2sePrefix = "java.util.logging.";

    private static final String configClass = j2sePrefix + "config.class";

    private static final String config = "config";

    private static final String installHandlers = "install-handlers";

    private static final String handlers = "handlers";

    private static final String java_home = "java.home";

    private static final String libDir = "lib";

    private static final String configFile = j2sePrefix + "config.file";

    private static final String configFileName = "logging.properties";

    /**
     *
     * @throws IOException
     */
    public static void install() throws IOException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {

                public Void run() throws IOException {
                    Properties sys = System.getProperties();
                    String oldConfig = sys.getProperty(configClass, "").trim();
                    sys.setProperty(configClass, Configuration.class.getName());
                    if (oldConfig.length() > 0 && !Configuration.class.getName().equals(oldConfig)) sys.setProperty(PREFIX + config, oldConfig);
                    LogManager.getLogManager().readConfiguration();
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            if (e.getException() instanceof RuntimeException) throw (RuntimeException) e.getException();
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor used by {@link LogManager#readConfiguration()}.
     * @throws IOException
     */
    public Configuration() throws IOException {
        configure(getSystemProperties());
    }

    public static Properties getSystemProperties() {
        return AccessController.doPrivileged(new PrivilegedAction<Properties>() {

            public Properties run() {
                Properties src = System.getProperties();
                Properties dest = new Properties();
                for (Enumeration<?> propertyNames = src.propertyNames(); propertyNames.hasMoreElements(); ) {
                    String sName = String.valueOf(propertyNames.nextElement());
                    if (sName.startsWith(j2sePrefix) && !configClass.equals(sName)) dest.setProperty(sName, src.getProperty(sName)); else if (sName.startsWith(PREFIX)) dest.setProperty(sName.substring(PREFIX.length()), src.getProperty(sName));
                }
                System.getProperties().remove(PREFIX + installHandlers);
                System.getProperties().remove(PREFIX + config);
                return dest;
            }
        });
    }

    private static Logger getRootLogger() {
        Logger l = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        while (l.getParent() != null) l = l.getParent();
        return l;
    }

    public static void installHandler(Class<? extends Handler>... classes) throws IOException {
        StringBuilder sb = new StringBuilder(classes.length * 128);
        for (Class<? extends Handler> clazz : classes) sb.append(clazz.getName()).append(',');
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            System.setProperty(PREFIX + installHandlers, sb.toString());
            install();
        }
    }

    private static String[] parseClassNames(String classNames) {
        if (classNames == null) {
            return new String[0];
        }
        classNames = classNames.trim();
        int ix = 0;
        List<String> result = new ArrayList<String>();
        while (ix < classNames.length()) {
            int end = ix;
            while (end < classNames.length()) {
                if (Character.isWhitespace(classNames.charAt(end))) break;
                if (classNames.charAt(end) == ',') break;
                end++;
            }
            String word = classNames.substring(ix, end);
            ix = end + 1;
            word = word.trim();
            if (word.length() == 0) continue;
            result.add(word);
        }
        return result.toArray(new String[result.size()]);
    }

    private static class Configure implements PrivilegedExceptionAction<Void> {

        private final Properties override;

        public Configure(Properties override) {
            this.override = override;
        }

        public Void run() throws IOException {
            String fname = System.getProperty(configFile);
            if (fname == null) {
                fname = System.getProperty(java_home);
                if (fname == null) throw new Error("Can't find " + java_home + " ??");
                File f = new File(fname, libDir);
                f = new File(f, configFileName);
                fname = f.getCanonicalPath();
            }
            InputStream in = new FileInputStream(fname);
            BufferedInputStream bin = new BufferedInputStream(in);
            Properties props = new Properties();
            try {
                props.load(bin);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                    }
                }
            }
            props.putAll(override);
            String toInstall = props.getProperty(installHandlers, "").trim();
            if (toInstall.length() > 0) {
                String hndls = props.getProperty(handlers, "").trim();
                Set<String> hndlSet = new HashSet<String>();
                StringBuilder hndlList = new StringBuilder();
                for (String cn : parseClassNames(hndls)) if (hndlSet.add(cn)) hndlList.append(cn).append('.');
                for (String cn : parseClassNames(toInstall)) if (hndlSet.add(cn)) hndlList.append(cn).append('.');
                if (hndlList.length() > 0) hndlList.deleteCharAt(hndlList.length() - 1);
                props.setProperty(handlers, hndlList.toString());
            }
            props.remove(installHandlers);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(buf);
            props.list(ps);
            ps.flush();
            ps.close();
            ps = null;
            ByteArrayInputStream inbuf = new ByteArrayInputStream(buf.toByteArray());
            LogManager.getLogManager().readConfiguration(inbuf);
            return null;
        }
    }

    public static void configure(Properties override) throws IOException {
        try {
            AccessController.doPrivileged(new Configure(override));
        } catch (PrivilegedActionException e) {
            if (e.getException() instanceof RuntimeException) throw (RuntimeException) e.getException();
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            Logger.getLogger(Configuration.class.getName()).fine("Log FINE");
            Logger.getLogger(Configuration.class.getName()).info("Log INFO");
            System.setProperty("java.util.logging.ConsoleHandler.level", "ALL");
            install();
            Logger.getLogger(Configuration.class.getName()).fine("Log FINE");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
