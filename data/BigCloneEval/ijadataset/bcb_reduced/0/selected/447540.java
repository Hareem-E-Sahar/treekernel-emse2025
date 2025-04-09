package alp.client;

import alp.debug;

/**
 * @author niki.waibel{@}gmx.net
 */
public final class ALP implements alp.client.SessionListener, java.awt.event.ActionListener, java.awt.event.WindowListener {

    private static final String defRHost = "192.168.100.5";

    private static final int defRPort = 7009;

    private static final String defLHost = "";

    private static final int defLTPort = 0;

    private static final int defLUPort = 0;

    static final String authorInfo = "Niki W. Waibel";

    static final String versionInfo = "0.1.91";

    static final String copyrightInfo = "Copyright (C) 2009";

    static final String licenseInfo = "GNU General Public License" + " version 3 or later";

    public static final String appletInfo = authorInfo + ", " + versionInfo + ", " + copyrightInfo + ", " + licenseInfo;

    public static final String parameterInfo[][] = { { "lhost", "ip-address", "default local ip/host [empty=any]" }, { "lport", "0-65535", "default local TCP source port [0=any]" }, { "rhost", "ip-address", "default session manager ip/host [empty=127.0.0.1]" }, { "rport", "1-65535", "default session manager port [empty=7009]" } };

    private static final String defWinTitle = "SoftRay " + versionInfo;

    private int codeNum;

    private char codeChar;

    private MainWindow w;

    private SmartcardWindow scwin;

    private String[] args;

    final String[] getArgs() {
        return this.args;
    }

    private debug d;

    final debug getDebug() {
        return this.d;
    }

    private Session session;

    final Session getSession() {
        return this.session;
    }

    final void setRUPort(int port) {
        w.RUPort.setText(Integer.toString(port));
    }

    final void setMessage(String s) {
        w.Message.setText(s);
    }

    public ALP(String[] args) {
        this.args = args;
        parseArgs(args);
        this.d = new debug(debug.LEVEL.DEBUG);
        initGui();
        w.Button1.addActionListener(this);
        w.Button2.addActionListener(this);
        w.Button3.addActionListener(this);
        w.addWindowListener(this);
        session = new Session(this);
        session.addEventListener(this);
        w.setVisible(true);
    }

    private final void parseArgs(String[] args) {
        for (String arg : args) {
            System.err.printf("Argument: '%s'\n", arg);
        }
    }

    private void initGui() {
        d = new debug(debug.LEVEL.DEBUG);
        w = new MainWindow();
        w.setTitle(defWinTitle);
        w.setLocationByPlatform(true);
        w.Info.setText(defWinTitle);
        w.RHost.setText(defRHost);
        w.RPort.setText(Integer.toString(defRPort));
        try {
            java.util.Enumeration eth = java.net.NetworkInterface.getNetworkInterfaces();
            while (eth.hasMoreElements()) {
                java.net.NetworkInterface eth0 = (java.net.NetworkInterface) eth.nextElement();
                byte mac[] = eth0.getHardwareAddress();
                if (mac != null) {
                    String ss = "";
                    for (int i = 0; i < mac.length; i++) {
                        String sss = String.format("%02X", mac[i]);
                        if (i == 0) {
                            ss = sss;
                        } else {
                            ss += ((i % 2 == 0) ? " " : "") + sss;
                        }
                    }
                    w.Mac.addItem(ss);
                }
            }
        } catch (Exception e) {
            w.Mac.addItem(e.toString());
        }
        w.LHost.setText(defLHost);
        w.LTPort.setText(Integer.toString(defLTPort));
        w.RHost.setText(defRHost);
        w.RPort.setText(Integer.toString(defRPort));
        w.LUPort.setText(Integer.toString(defLUPort));
        w.RUPort.setText("");
        w.RUPort.setText("");
        w.Message.setText("");
        setCode(0, ' ');
    }

    public void destroy() {
        System.out.println("alp.ALP.destroy()");
        w.setVisible(false);
        w.dispose();
    }

    private final String checkMac(String s) throws java.text.ParseException {
        s = s.replaceAll("[^0-9a-fA-F]", "").toLowerCase();
        if (s.length() != 12) {
            throw new java.text.ParseException("Illegal MAC address given", 0);
        }
        return s;
    }

    final String getCode() {
        String s;
        if (codeChar == ' ') {
            s = String.format("%d", codeNum);
        } else {
            s = String.format("%d %c", codeNum, codeChar);
        }
        return s;
    }

    final void setCode(int i) {
        codeNum = i;
        w.Code.setText(getCode());
    }

    final void setCode(int i, char c) {
        codeNum = i;
        codeChar = c;
        w.Code.setText(getCode());
    }

    @Override
    public void sessionStateChanged(alp.client.SessionStateChangedEvent e) {
        System.out.println("ALP.sessionStateChanged(): " + e.getState().toString());
        w.SessionText.setText(e.getState().toString());
        switch(e.getState()) {
            case uninitialized:
                setCode(21);
                w.Button1.setText("Initialize");
                w.Button1.setVisible(true);
                w.Button2.setText("End");
                w.Button2.setVisible(true);
                w.Button3.setVisible(false);
                w.Mac.setEnabled(true);
                w.RHost.setEditable(true);
                w.RPort.setEditable(true);
                w.LHost.setEditable(true);
                w.LTPort.setEditable(true);
                break;
            case disconnected:
                setCode(22);
                w.Button1.setText("Connect");
                w.Button1.setVisible(true);
                w.Button2.setText("Uninitialize");
                w.Button2.setVisible(true);
                w.Button3.setVisible(false);
                w.Mac.setEnabled(false);
                w.RHost.setEditable(false);
                w.RPort.setEditable(false);
                w.LHost.setEditable(false);
                w.LTPort.setEditable(false);
                w.LHost.setText(session.getLocal().getAddress().getHostAddress());
                w.LTPort.setText(String.valueOf(session.getLocal().getPort()));
                w.RHost.setText(session.getRemote().getAddress().getHostAddress());
                w.RPort.setText(String.valueOf(session.getRemote().getPort()));
                w.LUPort.setEditable(true);
                break;
            case connected:
                w.Button1.setText("Smartcard");
                w.Button1.setVisible(true);
                w.Button2.setText("Disconnect");
                w.Button2.setVisible(true);
                w.Button3.setVisible(false);
                w.Mac.setEnabled(false);
                w.LUPort.setEditable(false);
                break;
        }
    }

    @Override
    public void sessionSecurity(alp.client.SessionSecurityEvent e) {
        System.out.printf("sessionSecurity: auth=%b encr=%b\n", e.isAuthenticated(), e.isEncrypted());
        w.Auth.setSelected(e.isAuthenticated());
        w.Encr.setSelected(e.isEncrypted());
        if (e.isAuthenticated() && e.isEncrypted()) {
            setCode(33);
        } else if (e.isAuthenticated() && !e.isEncrypted()) {
            setCode(31);
        } else if (!e.isAuthenticated() && e.isEncrypted()) {
            setCode(32);
        } else {
            setCode(34);
        }
    }

    @Override
    public void sessionException(alp.client.SessionExceptionEvent e) {
        System.out.println("sessionException: " + e.getDescription());
        w.Message.setText(e.getDescription());
    }

    @Override
    public void sessionPortChanged(SessionEvent e) {
        System.out.printf("ALP.sessionPortChanged(): port=%d\n", e.port);
        w.LUPort.setText(Integer.toString(e.port));
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        System.out.println("ALP.actionPerformed(): " + e.getActionCommand());
        if (e.getActionCommand().equals("End")) {
            this.destroy();
        } else if (e.getActionCommand().equals("Initialize")) {
            try {
                java.net.InetAddress la = null, ra;
                java.net.InetSocketAddress lsa = null, rsa;
                String rht = w.RHost.getText().trim();
                if (rht.equals("")) {
                    rht = "127.0.0.1";
                }
                ra = java.net.InetAddress.getByName(rht);
                String rpt = w.RPort.getText().trim();
                int rp = Integer.parseInt(rpt);
                if (rp == 0) {
                    rp = 7009;
                }
                rsa = new java.net.InetSocketAddress(ra, rp);
                String lht = w.LHost.getText().trim();
                String lpt = w.LTPort.getText().trim();
                int lp = Integer.parseInt(lpt);
                if (lht.equals("")) {
                    lsa = new java.net.InetSocketAddress(lp);
                } else {
                    la = java.net.InetAddress.getByName(lht);
                    lsa = new java.net.InetSocketAddress(la, lp);
                }
                String mac = (String) w.Mac.getSelectedItem();
                try {
                    String mac2 = checkMac(mac);
                    session.initialize(mac2, rsa, lsa);
                } catch (java.text.ParseException ex) {
                    w.Message.setText(ex.getMessage());
                }
            } catch (java.lang.Exception f) {
                System.err.println(f);
                w.Message.setText(f.toString());
            }
        } else if (e.getActionCommand().equals("Connect")) {
            try {
                session.connect();
            } catch (java.lang.Exception f) {
                System.err.println(f);
                w.Message.setText(f.toString());
            }
        } else if (e.getActionCommand().equals("Smartcard")) {
            scwin = new SmartcardWindow();
            scwin.setVisible(true);
            w.Button1.setText("rm Smartcard");
        } else if (e.getActionCommand().equals("rm Smartcard")) {
            scwin.dispose();
            w.Button1.setText("Smartcard");
        } else if (e.getActionCommand().equals("Disconnect")) {
            try {
                session.disconnect();
            } catch (java.lang.Exception f) {
                System.err.println(f);
                w.Message.setText(f.toString());
            }
        } else if (e.getActionCommand().equals("Uninitialize")) {
            try {
                session.uninitialize();
            } catch (java.lang.Exception f) {
                System.err.println(f);
                w.Message.setText(f.toString());
            }
        } else {
            System.err.println(e.getActionCommand() + ": UNHANDLED!");
        }
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        this.destroy();
    }

    @Override
    public void windowClosed(java.awt.event.WindowEvent e) {
        System.exit(0);
    }

    @Override
    public void windowIconified(java.awt.event.WindowEvent e) {
    }

    @Override
    public void windowDeiconified(java.awt.event.WindowEvent e) {
    }

    @Override
    public void windowOpened(java.awt.event.WindowEvent e) {
    }

    @Override
    public void windowActivated(java.awt.event.WindowEvent e) {
    }

    @Override
    public void windowDeactivated(java.awt.event.WindowEvent e) {
    }
}
