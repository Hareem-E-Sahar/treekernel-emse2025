import java.io.*;
import java.net.URL;

class GridSAMClient {

    public static void main(String[] args) {
        String s = File.separator;
        String classLocation = GridSAMClient.class.getName().replaceAll("\\.", s) + ".class";
        ClassLoader loader = GridSAMClient.class.getClassLoader();
        URL location = loader.getResource(classLocation);
        String gridsamHome = location.getFile().replace("GridSAMClient.class", "");
        if (!isValidCommand(args[0])) {
            System.out.println("Unknown command: " + args[0]);
            return;
        }
        StringBuffer a = new StringBuffer();
        for (int i = 1; i < args.length; i++) {
            a.append(args[i]);
            a.append(" ");
        }
        String ftpServerHost;
        if (System.getProperty("FtpServer.server.config.self.host") != null) {
            ftpServerHost = "-DFtpServer.server.config.self.host=" + System.getProperty("FtpServer.server.config.self.host") + " ";
        } else {
            ftpServerHost = "";
        }
        String command = "java -classpath " + getClassPath(gridsamHome + "lib") + "conf " + "-Djava.endorsed.dirs=endorsed " + ftpServerHost + "-Dorg.apache.ws.security.crypto.merlin.file=conf" + s + "gridsam-default.ks " + "-Dorg.apache.ws.security.crypto.merlin.crldir=conf" + s + "CRLs " + "-Dlog4j.appender.LOG.File=client.log " + "-Djava.util.logging.config.file=conf" + s + "logging.properties " + "-Dlog4j.configuration=file://" + gridsamHome + "conf" + s + "log4j.properties " + "-Daxis.ClientConfigFile=conf" + s + "client-config.wsdd " + "-Dgrid.config.dir=conf " + "org.icenigrid.gridsam.client.cli." + args[0] + " " + a.toString();
        String[] commandArgs = command.split(" +");
        ProcessBuilder pb = new ProcessBuilder(commandArgs);
        pb.directory(new File(gridsamHome));
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            InputStream is = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (java.io.IOException ioe) {
            System.out.println("Error processing command");
        }
    }

    private static String getClassPath(String directory) {
        try {
            File d = new File(directory);
            StringBuffer cp = new StringBuffer();
            for (File f : d.listFiles()) {
                if (f.isFile() && f.getName().endsWith(".jar")) {
                    cp.append(f);
                    cp.append(File.pathSeparator);
                }
            }
            return cp.toString();
        } catch (java.lang.NullPointerException npe) {
            return File.pathSeparator;
        }
    }

    private static boolean isValidCommand(String c) {
        return "BESCreateActivity".equals(c) || "BESGetActivityDocuments".equals(c) || "BESGetActivityStatuses".equals(c) || "BESGetFactoryAttributesDocument".equals(c) || "BESStartAcceptingNewActivities".equals(c) || "BESStopAcceptingNewActivities".equals(c) || "BESTerminateActivities".equals(c) || "GridSAMFileTransfer".equals(c) || "GridSAMFTPServer".equals(c) || "GridSAMGroovyBootstrap".equals(c) || "GridSAMMultiJSDLStressTest".equals(c) || "GridSAMRerun".equals(c) || "GridSAMStart".equals(c) || "GridSAMStatus".equals(c) || "GridSAMStatusSubscribe".equals(c) || "GridSAMStressTest".equals(c) || "GridSAMSubmit".equals(c) || "GridSAMTerminate".equals(c) || "GridSAMVersion".equals(c);
    }
}
