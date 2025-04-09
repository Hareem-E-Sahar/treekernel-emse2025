package tuna;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Set;
import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import tuna.restlet.TunaApplication;

public class Tuna {

    public static Logger log;

    private static void initLogger() throws IOException {
        PatternLayout pattern = new PatternLayout("%d %r [%t] %-5p %c %x - %m%n");
        Appender appender;
        appender = new ConsoleAppender(pattern);
        Logger.getRootLogger().addAppender(appender);
        File logDir = TunaOptions.getLogDir();
        if (logDir != null) {
            File logFile = new File(logDir, "tuna.log");
            appender = new DailyRollingFileAppender(pattern, logFile.getAbsolutePath(), "yyyy-MM-dd");
            Logger.getRootLogger().addAppender(appender);
        }
        log = Logger.getLogger(Tuna.class);
    }

    public static void main(String[] args) throws NumberFormatException, Exception {
        TunaOptions.init(args);
        initLogger();
        log.info("Starting Tuna");
        initProfilesInSequence();
        initConnectionKeeper();
        int p = TunaOptions.getWsPort();
        log.info("Starting web server on port " + p);
        startWebServer(p);
        if (TunaOptions.autoOpenWebadmin()) {
            log.info("opening webadmin on default browser");
            openWebadmin(p);
        }
        log.info("Tuna started");
    }

    private static void initConnectionKeeper() {
        log.info("Initializing ConnectionKeeper");
        ConnectionKeeper ck = new ConnectionKeeper();
        ck.start();
    }

    private static void initProfilesInSequence() throws Exception {
        log.info("starting profiles that have an init sequence");
        Hashtable<String, Profile> list = Profiles.getInstance().getList();
        Set<String> names = list.keySet();
        ArrayList<Integer> seqs = new ArrayList<Integer>();
        HashMap<Integer, Profile> map = new HashMap<Integer, Profile>();
        for (String name : names) {
            Profile p = list.get(name);
            Integer seq = p.getInitSequence();
            if (seq != null) {
                if (map.containsKey(seq)) {
                    throw new Exception("there are two or more profiles sharing the init sequence [" + seq + "]");
                }
                seqs.add(seq);
                map.put(seq, p);
            }
        }
        if (seqs.size() == 0) {
            log.info("there is no init sequence of profiles");
            return;
        }
        Integer[] orderedSeq = new Integer[seqs.size()];
        seqs.toArray(orderedSeq);
        Arrays.sort(orderedSeq);
        for (Integer seq : orderedSeq) {
            log.info("starting profile in sequence [" + seq + "]");
            Profile p = map.get(seq);
            try {
                p.start();
            } catch (Exception e) {
                log.error("exception trying to start profile, leaving init sequence", e);
                break;
            }
        }
    }

    private static void openWebadmin(int p) {
        if (Desktop.isDesktopSupported()) {
            log.info("java desktop is supported");
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                log.info("java desktop browse is supported");
                URI uri;
                try {
                    uri = new URI("http://localhost:" + p + "/web/index.html");
                    try {
                        log.info("opening browser with URI:" + uri);
                        desktop.browse(uri);
                    } catch (IOException e) {
                        log.error("exception opening browser", e);
                    }
                } catch (URISyntaxException e) {
                    log.error("exception constructing webadmin URI", e);
                }
            } else {
                log.info("java desktop browse is not supported");
            }
        } else {
            log.info("java desktop is not supported");
        }
    }

    private static void startWebServer(int port) throws Exception {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, port);
        component.getClients().add(Protocol.FILE);
        component.getDefaultHost().attach("/tuna", new TunaApplication(component.getContext()));
        Application application = new Application(component.getContext()) {

            @Override
            public Restlet createRoot() {
                String dir = TunaOptions.getWebDir();
                log.info("Directorio web: " + dir);
                return new Directory(getContext(), dir);
            }
        };
        component.getDefaultHost().attach("/web", application);
        component.start();
    }
}
