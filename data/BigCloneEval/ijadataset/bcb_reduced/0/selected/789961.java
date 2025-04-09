package alp;

/**
 * @author niki.waibel{@}gmx.net
 */
public final class ALP implements alp.SessionListener, java.awt.event.ActionListener, java.awt.event.WindowListener {

    static final String authorInfo = "Niki W. Waibel";

    static final String versionInfo = "0.1";

    static final String copyrightInfo = "Copyright (C) 2009";

    static final String licenseInfo = "GNU General Public License" + " version 3 or later";

    public static final String appletInfo = authorInfo + ", " + versionInfo + ", " + copyrightInfo + ", " + licenseInfo;

    public static final String parameterInfo[][] = { { "lhost", "ip-address", "default local ip/host [empty=any]" }, { "lport", "0-65535", "default local TCP source port [0=any]" }, { "rhost", "ip-address", "default session manager ip/host [empty=127.0.0.1]" }, { "rport", "1-65535", "default session manager port [empty=7009]" } };

    private static final String defSWinTitle = "ALP-Session";

    private static final String defRHost = "192.168.100.3";

    private static final int defRPort = 7009;

    private static final String defLHost = "";

    private static final int defLPort = 0;

    private int codeNum;

    private char codeChar;

    private alp.Session session;

    private debug d;

    private SessionWindow swin;

    private SmartcardWindow scwin;

    public ALP(String[] args) {
        parseArgs(args);
        initGui();
        swin.Button1.addActionListener(this);
        swin.Button2.addActionListener(this);
        swin.Button3.addActionListener(this);
        swin.addWindowListener(this);
        session = new alp.Session(d, 9999);
        session.addEventListener(this);
        swin.setVisible(true);
    }

    private void parseArgs(String[] args) {
        for (String arg : args) {
            System.err.printf("Argument: '%s'\n", arg);
        }
    }

    private void initGui() {
        d = new debug(debug.LEVEL.DEBUG);
        swin = new SessionWindow();
        swin.setTitle(defSWinTitle);
        swin.setLocationByPlatform(true);
        swin.Info.setText(defSWinTitle);
        swin.RHost.setText(defRHost);
        swin.RPort.setText(Integer.toString(defRPort));
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
                    swin.Mac.addItem(ss);
                }
            }
        } catch (Exception e) {
            swin.Mac.addItem(e.toString());
        }
        swin.LHost.setText(defLHost);
        swin.LPort.setText(Integer.toString(defLPort));
        swin.RHost.setText(defRHost);
        swin.RPort.setText(Integer.toString(defRPort));
        setCode(0, ' ');
    }

    public void destroy() {
        System.out.println("alp.ALP.destroy()");
        swin.setVisible(false);
        swin.dispose();
    }

    private final String checkMac(String s) throws java.text.ParseException {
        s = s.replaceAll("[^0-9a-fA-F]", "").toLowerCase();
        if (s.length() != 12) {
            throw new java.text.ParseException("Illegal MAC address given", 0);
        }
        return s;
    }

    private final String getCode() {
        String s;
        if (codeChar == ' ') {
            s = String.format("%d", codeNum);
        } else {
            s = String.format("%d %c", codeNum, codeChar);
        }
        return s;
    }

    private final void setCode(int i) {
        codeNum = i;
        swin.Code.setText(getCode());
    }

    private final void setCode(int i, char c) {
        codeNum = i;
        codeChar = c;
        swin.Code.setText(getCode());
    }

    public void sessionStateChanged(alp.SessionStateChangedEvent e) {
        System.out.println("ALP.sessionStateChanged(): " + e.getState().toString());
        swin.Session.setText(e.getState().toString());
        switch(e.getState()) {
            case uninitialized:
                setCode(21);
                swin.Button1.setText("Initialize");
                swin.Button1.setVisible(true);
                swin.Button2.setText("End");
                swin.Button2.setVisible(true);
                swin.Button3.setVisible(false);
                swin.Mac.setEnabled(true);
                swin.RHost.setEditable(true);
                swin.RPort.setEditable(true);
                swin.LHost.setEditable(true);
                swin.LPort.setEditable(true);
                break;
            case disconnected:
                setCode(22);
                swin.Button1.setText("Connect");
                swin.Button1.setVisible(true);
                swin.Button2.setText("Uninitialize");
                swin.Button2.setVisible(true);
                swin.Button3.setVisible(false);
                swin.Mac.setEnabled(false);
                swin.RHost.setEditable(false);
                swin.RPort.setEditable(false);
                swin.LHost.setEditable(false);
                swin.LPort.setEditable(false);
                swin.LHost.setText(session.getLocal().getAddress().getHostAddress());
                swin.LPort.setText(String.valueOf(session.getLocal().getPort()));
                swin.RHost.setText(session.getRemote().getAddress().getHostAddress());
                swin.RPort.setText(String.valueOf(session.getRemote().getPort()));
                break;
            case connected:
                swin.Button1.setText("Smartcard");
                swin.Button1.setVisible(true);
                swin.Button2.setText("Disconnect");
                swin.Button2.setVisible(true);
                swin.Button3.setVisible(false);
                swin.Mac.setEnabled(false);
                break;
        }
    }

    public void sessionSecurity(alp.SessionSecurityEvent e) {
        System.out.printf("sessionSecurity: auth=%b encr=%b\n", e.isAuthenticated(), e.isEncrypted());
        swin.Auth.setSelected(e.isAuthenticated());
        swin.Encr.setSelected(e.isEncrypted());
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

    public void sessionException(alp.SessionExceptionEvent e) {
        System.out.println("sessionException: " + e.getDescription());
        swin.Status.setText(e.getDescription());
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        System.out.println("ALP.actionPerformed(): " + e.getActionCommand());
        if (e.getActionCommand().equals("End")) {
            this.destroy();
        } else if (e.getActionCommand().equals("Initialize")) {
            try {
                java.net.InetAddress la = null, ra;
                java.net.InetSocketAddress lsa = null, rsa;
                String rht = swin.RHost.getText().trim();
                if (rht.equals("")) {
                    rht = "127.0.0.1";
                }
                ra = java.net.InetAddress.getByName(rht);
                String rpt = swin.RPort.getText().trim();
                int rp = Integer.parseInt(rpt);
                if (rp == 0) {
                    rp = 7009;
                }
                rsa = new java.net.InetSocketAddress(ra, rp);
                String lht = swin.LHost.getText().trim();
                String lpt = swin.LPort.getText().trim();
                int lp = Integer.parseInt(lpt);
                if (lht.equals("")) {
                    lsa = new java.net.InetSocketAddress(lp);
                } else {
                    la = java.net.InetAddress.getByName(lht);
                    lsa = new java.net.InetSocketAddress(la, lp);
                }
                String mac = (String) swin.Mac.getSelectedItem();
                try {
                    String mac2 = checkMac(mac);
                    session.initialize(mac2, rsa, lsa);
                } catch (java.text.ParseException ex) {
                    swin.Status.setText(ex.getMessage());
                }
            } catch (java.lang.Exception f) {
                System.err.println(f);
                swin.Status.setText(f.toString());
            }
        } else if (e.getActionCommand().equals("Connect")) {
            try {
                session.connect();
            } catch (java.lang.Exception f) {
                System.err.println(f);
                swin.Status.setText(f.toString());
            }
        } else if (e.getActionCommand().equals("Smartcard")) {
            scwin = new SmartcardWindow();
            scwin.setVisible(true);
            swin.Button1.setText("rm Smartcard");
        } else if (e.getActionCommand().equals("rm Smartcard")) {
            scwin.dispose();
            swin.Button1.setText("Smartcard");
        } else if (e.getActionCommand().equals("Disconnect")) {
            try {
                session.disconnect();
            } catch (java.lang.Exception f) {
                System.err.println(f);
                swin.Status.setText(f.toString());
            }
        } else if (e.getActionCommand().equals("Uninitialize")) {
            try {
                session.uninitialize();
            } catch (java.lang.Exception f) {
                System.err.println(f);
                swin.Status.setText(f.toString());
            }
        } else {
            System.err.println(e.getActionCommand() + ": UNHANDLED!");
        }
    }

    public void windowClosing(java.awt.event.WindowEvent e) {
        this.destroy();
    }

    public void windowClosed(java.awt.event.WindowEvent e) {
        System.exit(0);
    }

    public void windowIconified(java.awt.event.WindowEvent e) {
    }

    public void windowDeiconified(java.awt.event.WindowEvent e) {
    }

    public void windowOpened(java.awt.event.WindowEvent e) {
    }

    public void windowActivated(java.awt.event.WindowEvent e) {
    }

    public void windowDeactivated(java.awt.event.WindowEvent e) {
    }
}
