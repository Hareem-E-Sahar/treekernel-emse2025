package net.sourceforge.myvd.test.util;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPLocalException;
import com.novell.ldap.LDAPMessage;
import com.novell.ldap.LDAPSearchResult;
import com.novell.ldap.util.LDIFReader;

public class StartOpenDS {

    static HashMap<Integer, StartOpenDS> servers = new HashMap<Integer, StartOpenDS>();

    Process process;

    int port;

    public void stopServer() throws Exception {
        String exec = System.getenv("PROJ_DIR") + "/test/opends-1.0b7/bin/stop-ds";
        String[] nenv = new String[System.getenv().keySet().size() + 2];
        int i = 0, m;
        Iterator<String> keyIt = System.getenv().keySet().iterator();
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            nenv[i] = key + "=" + System.getenv(key);
            i++;
        }
        nenv[i] = "APACHEDS_HOME=" + System.getenv("PROJ_DIR") + "/test/apacheds102";
        i++;
        nenv[i] = "SERVER_HOME=" + System.getenv("PROJ_DIR") + "/test/apacheds102";
        process = Runtime.getRuntime().exec(exec, nenv);
        StreamReader reader = new StreamReader(process.getInputStream(), false);
        StreamReader errReader = new StreamReader(process.getErrorStream(), false);
        reader.start();
        errReader.start();
        for (i = 0, m = 100; i < m; i++) {
            try {
                LDAPConnection con = new LDAPConnection();
                con.connect("127.0.0.1", port);
                con.disconnect();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            } catch (LDAPException e) {
                servers.remove(port);
                break;
            }
        }
        Thread.sleep(10000);
    }

    private void clearData(String path) {
        String dataPath = path + "/db";
        File dir = new File(dataPath);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (int i = 0, m = files.length; i < m; i++) {
                    if (files[i].isDirectory()) {
                        clearDir(files[i].getAbsolutePath());
                    }
                    files[i].delete();
                }
            }
        } else {
            dir.mkdir();
        }
    }

    private void clearDir(String dataPath) {
        File dir = new File(dataPath);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (int i = 0, m = files.length; i < m; i++) {
                    if (files[i].isDirectory()) {
                        clearDir(files[i].getAbsolutePath());
                    }
                    files[i].delete();
                }
            }
        }
    }

    private void loadLDIF(String path, String adminDN, String adminPass, int port) throws LDAPException, FileNotFoundException, IOException {
        try {
            this.port = port;
            LDAPConnection con = new LDAPConnection();
            con.connect("localhost", port);
            con.bind(3, adminDN, adminPass.getBytes());
            LDIFReader reader = new LDIFReader(new FileInputStream(path + "/data.ldif"));
            LDAPMessage msg;
            while ((msg = reader.readMessage()) != null) {
                System.err.println("Msg : " + msg);
                con.add(((LDAPSearchResult) msg).getEntry());
            }
            con.disconnect();
        } catch (LDAPLocalException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (LDAPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean startServer(String fullPath, int port, String adminDN, String adminPass) throws IOException, Exception {
        return this.startServer(fullPath, port, adminDN, adminPass, 0);
    }

    private boolean startServer(String fullPath, int port, String adminDN, String adminPass, int num) throws IOException, Exception {
        LDAPConnection con = new LDAPConnection();
        try {
            con.connect("localhost", port);
            con.disconnect();
            if (!servers.containsKey(port)) {
                throw new Exception("Server on port " + port + "not stopped");
            } else {
                servers.get(port).stopServer();
            }
        } catch (LDAPException e) {
        } catch (Exception e) {
            throw e;
        }
        clearData(System.getenv("PROJ_DIR") + "/test/opends-1.0b7");
        String exec = System.getenv("PROJ_DIR") + "/test/opends-1.0b7/bin/start-ds";
        String[] nenv = new String[System.getenv().keySet().size() + 2];
        int i = 0, m;
        Iterator<String> keyIt = System.getenv().keySet().iterator();
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            nenv[i] = key + "=" + System.getenv(key);
            i++;
        }
        nenv[i] = "APACHEDS_HOME=" + System.getenv("PROJ_DIR") + "/test/apacheds102";
        i++;
        nenv[i] = "SERVER_HOME=" + System.getenv("PROJ_DIR") + "/test/apacheds102";
        process = Runtime.getRuntime().exec(exec, nenv);
        StreamReader reader = new StreamReader(process.getInputStream(), true);
        StreamReader errReader = new StreamReader(process.getErrorStream(), false);
        reader.start();
        errReader.start();
        for (i = 0, m = 50; i < m; i++) {
            con = new LDAPConnection();
            try {
                con.connect("localhost", port);
                con.disconnect();
                this.loadLDIF(fullPath, adminDN, adminPass, port);
                servers.put(port, this);
                return true;
            } catch (LDAPException e) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }
            }
        }
        if (num < 3) {
            Thread.sleep(30000);
            return this.startServer(fullPath, port, adminDN, adminPass, num + 1);
        } else {
            return false;
        }
    }

    private void createTestConf(String fullPath) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fullPath + "/slapd.conf")));
        StringBuffer buf = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null) {
            buf.append(line).append('\n');
        }
        String tmp = buf.toString().replaceAll("[%]PROJ_DIR[%]", System.getenv("PROJ_DIR"));
        tmp = tmp.replaceAll("[%]SCHEMA_DIR[%]", System.getenv("SCHEMA_DIR"));
        PrintWriter out = new PrintWriter(new FileWriter(fullPath + "/slapd-gen.conf"));
        out.println("# GENERATED FILE - DO NOT EDIT");
        out.print(tmp);
        out.close();
    }

    public static final void main(String[] args) throws Exception {
        StartOpenDS start = new StartOpenDS();
        boolean isStarted = start.startServer(System.getenv("PROJ_DIR") + "/test/EmbeddedGroups", 12389, "cn=Directory Manager", "secret");
        if (isStarted) {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            in.readLine();
            start.stopServer();
        }
    }
}
