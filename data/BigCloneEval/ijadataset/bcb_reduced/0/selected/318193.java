package cxtable.core_comm;

import cxtable.*;
import cxtable.registry.*;
import java.net.*;
import java.io.*;

public class xServSocket extends Thread {

    public static boolean control_listening = true;

    private boolean listening;

    private boolean as_php = false;

    private String pservname = "";

    private String http = "";

    private xServerListener xsl;

    private xLinlyn xlin;

    private String file;

    private ServerSocket ss;

    private boolean withlinlyn;

    private long startserv, finserv;

    public xServSocket(String site, String user, String pw, String di, String fi, xServerListener x) {
        xsl = x;
        xlin = new xLinlyn(site, user, pw, di);
        file = fi;
        withlinlyn = true;
    }

    public xServSocket(String http_data, String sname, xServerListener x) {
        xsl = x;
        http = http_data;
        pservname = sname;
        withlinlyn = false;
        as_php = true;
    }

    public xServSocket(xServerListener x) {
        xsl = x;
        withlinlyn = false;
    }

    public void run() {
        if (withlinlyn == true) {
            try {
                xlin.erase(file);
            } catch (Exception e) {
                System.out.println("Error erasing");
            }
        } else if (as_php) {
            try {
                URL url = new URL(http + "REM:" + pservname);
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                System.out.println("Response from REMOVE:");
                String s;
                while ((s = br.readLine()) != null) {
                    System.out.println(s);
                }
                br.close();
            } catch (Exception e) {
                System.out.println("Error erasing/php!");
            }
        }
        try {
            InetAddress ia = InetAddress.getLocalHost();
            ss = new ServerSocket(0, 50, ia);
            startserv = System.currentTimeMillis();
            ss.setSoTimeout(0);
            String svname = ia.getHostAddress();
            System.out.println(svname + ":sv");
            String mssg = "<SERVER><IP>" + svname + "</IP><PORT>" + ss.getLocalPort() + "</PORT></SERVER>";
            if (withlinlyn == true) {
                try {
                    xlin.replace(file, mssg);
                    System.out.println("mssg:" + mssg + ", sent");
                } catch (Exception e) {
                    System.out.println("Error posting address");
                    return;
                }
            } else if (as_php) {
                try {
                    URL url = new URL(http + "ADD:" + svname + ":" + ss.getLocalPort() + ":" + pservname);
                    BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
                    String response = "";
                    String s;
                    while ((s = br.readLine()) != null) {
                        response = response + s + System.getProperty("line.separator");
                    }
                    br.close();
                    String resp = new xLineSplit().ssplit("REPLY", response);
                    if (!resp.equalsIgnoreCase("ADDED")) {
                        System.out.println("potential error posting via php:\nReponse was:\n" + response);
                    }
                } catch (Exception e) {
                    System.out.println("Error in posting php:" + e.toString());
                }
            }
            xsl.regserver(svname, new String("" + ss.getLocalPort()));
            Socket server = null;
            listening = true;
            while (listening) {
                server = ss.accept();
                if (server != null) {
                    xsl.add(server);
                    System.out.println("added connect");
                } else {
                    System.out.println("Received null socket");
                }
                server = null;
                listening = control_listening;
            }
            finserv = System.currentTimeMillis();
            long l = finserv - startserv;
            long m = l / 1000;
            System.err.println("Server socket has closed, time elapsed:" + m);
            System.out.println("Server socket has closed, time elapsed:" + m);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void kill_server() {
        listening = false;
    }
}
