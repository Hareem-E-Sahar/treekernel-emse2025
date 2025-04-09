import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class VncViewer extends java.applet.Applet implements java.lang.Runnable, WindowListener {

    boolean inAnApplet = true;

    boolean inSeparateFrame = false;

    public static void main(String[] argv) {
        VncViewer v = new VncViewer();
        v.mainArgs = argv;
        v.inAnApplet = false;
        v.inSeparateFrame = true;
        v.init();
        v.start();
    }

    String[] mainArgs;

    RfbProto rfb;

    Thread rfbThread;

    Frame vncFrame;

    Container vncContainer;

    ScrollPane desktopScrollPane;

    GridBagLayout gridbag;

    ButtonPanel buttonPanel;

    Label connStatusLabel;

    VncCanvas vc;

    OptionsFrame options;

    ClipboardFrame clipboard;

    RecordingFrame rec;

    Object recordingSync;

    String sessionFileName;

    boolean recordingActive;

    boolean recordingStatusChanged;

    String cursorUpdatesDef;

    String eightBitColorsDef;

    String socketFactory;

    String host;

    int port;

    String passwordParam;

    boolean showControls;

    boolean offerRelogin;

    boolean showOfflineDesktop;

    int deferScreenUpdates;

    int deferCursorUpdates;

    int deferUpdateRequests;

    int debugStatsExcludeUpdates;

    int debugStatsMeasureUpdates;

    public static java.applet.Applet refApplet;

    public void init() {
        readParameters();
        refApplet = this;
        if (inSeparateFrame) {
            vncFrame = new Frame("TightVNC");
            if (!inAnApplet) {
                vncFrame.add("Center", this);
            }
            vncContainer = vncFrame;
        } else {
            vncContainer = this;
        }
        recordingSync = new Object();
        options = new OptionsFrame(this);
        clipboard = new ClipboardFrame(this);
        if (RecordingFrame.checkSecurity()) rec = new RecordingFrame(this);
        sessionFileName = null;
        recordingActive = false;
        recordingStatusChanged = false;
        cursorUpdatesDef = null;
        eightBitColorsDef = null;
        if (inSeparateFrame) vncFrame.addWindowListener(this);
        rfbThread = new Thread(this);
        rfbThread.start();
    }

    public void update(Graphics g) {
    }

    public void run() {
        gridbag = new GridBagLayout();
        vncContainer.setLayout(gridbag);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        if (showControls) {
            buttonPanel = new ButtonPanel(this);
            gridbag.setConstraints(buttonPanel, gbc);
            vncContainer.add(buttonPanel);
        }
        try {
            connectAndAuthenticate();
            doProtocolInitialisation();
            if (options.autoScale && inSeparateFrame) {
                Dimension screenSize;
                try {
                    screenSize = vncContainer.getToolkit().getScreenSize();
                } catch (Exception e) {
                    screenSize = new Dimension(0, 0);
                }
                createCanvas(screenSize.width - 32, screenSize.height - 32);
            } else {
                createCanvas(0, 0);
            }
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            if (inSeparateFrame) {
                Panel canvasPanel = new Panel();
                canvasPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
                canvasPanel.add(vc);
                desktopScrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
                gbc.fill = GridBagConstraints.BOTH;
                gridbag.setConstraints(desktopScrollPane, gbc);
                desktopScrollPane.add(canvasPanel);
                vncFrame.add(desktopScrollPane);
                vncFrame.setTitle(rfb.desktopName);
                vncFrame.pack();
                vc.resizeDesktopFrame();
            } else {
                gridbag.setConstraints(vc, gbc);
                add(vc);
                validate();
            }
            if (showControls) buttonPanel.enableButtons();
            moveFocusToDesktop();
            processNormalProtocol();
        } catch (NoRouteToHostException e) {
            fatalError("Network error: no route to server: " + host, e);
        } catch (UnknownHostException e) {
            fatalError("Network error: server name unknown: " + host, e);
        } catch (ConnectException e) {
            fatalError("Network error: could not connect to server: " + host + ":" + port, e);
        } catch (EOFException e) {
            if (showOfflineDesktop) {
                e.printStackTrace();
                System.out.println("Network error: remote side closed connection");
                if (vc != null) {
                    vc.enableInput(false);
                }
                if (inSeparateFrame) {
                    vncFrame.setTitle(rfb.desktopName + " [disconnected]");
                }
                if (rfb != null && !rfb.closed()) rfb.close();
                if (showControls && buttonPanel != null) {
                    buttonPanel.disableButtonsOnDisconnect();
                    if (inSeparateFrame) {
                        vncFrame.pack();
                    } else {
                        validate();
                    }
                }
            } else {
                fatalError("Network error: remote side closed connection", e);
            }
        } catch (IOException e) {
            String str = e.getMessage();
            if (str != null && str.length() != 0) {
                fatalError("Network Error: " + str, e);
            } else {
                fatalError(e.toString(), e);
            }
        } catch (Exception e) {
            String str = e.getMessage();
            if (str != null && str.length() != 0) {
                fatalError("Error: " + str, e);
            } else {
                fatalError(e.toString(), e);
            }
        }
    }

    void createCanvas(int maxWidth, int maxHeight) throws IOException {
        vc = null;
        try {
            Class cl = Class.forName("java.awt.Graphics2D");
            cl = Class.forName("VncCanvas2");
            Class[] argClasses = { this.getClass(), Integer.TYPE, Integer.TYPE };
            java.lang.reflect.Constructor cstr = cl.getConstructor(argClasses);
            Object[] argObjects = { this, new Integer(maxWidth), new Integer(maxHeight) };
            vc = (VncCanvas) cstr.newInstance(argObjects);
        } catch (Exception e) {
            System.out.println("Warning: Java 2D API is not available");
        }
        if (vc == null) vc = new VncCanvas(this, maxWidth, maxHeight);
    }

    void processNormalProtocol() throws Exception {
        try {
            vc.processNormalProtocol();
        } catch (Exception e) {
            if (rfbThread == null) {
                System.out.println("Ignoring RFB socket exceptions" + " because applet is stopping");
            } else {
                throw e;
            }
        }
    }

    void connectAndAuthenticate() throws Exception {
        showConnectionStatus("Initializing...");
        if (inSeparateFrame) {
            vncFrame.pack();
            vncFrame.show();
        } else {
            validate();
        }
        showConnectionStatus("Connecting to " + host + ", port " + port + "...");
        rfb = new RfbProto(host, port, this);
        showConnectionStatus("Connected to server");
        rfb.readVersionMsg();
        showConnectionStatus("RFB server supports protocol version " + rfb.serverMajor + "." + rfb.serverMinor);
        rfb.writeVersionMsg();
        showConnectionStatus("Using RFB protocol version " + rfb.clientMajor + "." + rfb.clientMinor);
        int secType = rfb.negotiateSecurity();
        int authType;
        if (secType == RfbProto.SecTypeTight) {
            showConnectionStatus("Enabling TightVNC protocol extensions");
            rfb.setupTunneling();
            authType = rfb.negotiateAuthenticationTight();
        } else {
            authType = secType;
        }
        switch(authType) {
            case RfbProto.AuthNone:
                showConnectionStatus("No authentication needed");
                rfb.authenticateNone();
                break;
            case RfbProto.AuthVNC:
                showConnectionStatus("Performing standard VNC authentication");
                if (passwordParam != null) {
                    rfb.authenticateVNC(passwordParam);
                } else {
                    String pw = askPassword();
                    rfb.authenticateVNC(pw);
                }
                break;
            default:
                throw new Exception("Unknown authentication scheme " + authType);
        }
    }

    void showConnectionStatus(String msg) {
        if (msg == null) {
            if (vncContainer.isAncestorOf(connStatusLabel)) {
                vncContainer.remove(connStatusLabel);
            }
            return;
        }
        System.out.println(msg);
        if (connStatusLabel == null) {
            connStatusLabel = new Label("Status: " + msg);
            connStatusLabel.setFont(new Font("Helvetica", Font.PLAIN, 12));
        } else {
            connStatusLabel.setText("Status: " + msg);
        }
        if (!vncContainer.isAncestorOf(connStatusLabel)) {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.insets = new Insets(20, 30, 20, 30);
            gridbag.setConstraints(connStatusLabel, gbc);
            vncContainer.add(connStatusLabel);
        }
        if (inSeparateFrame) {
            vncFrame.pack();
        } else {
            validate();
        }
    }

    String askPassword() throws Exception {
        showConnectionStatus(null);
        AuthPanel authPanel = new AuthPanel(this);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.ipadx = 100;
        gbc.ipady = 50;
        gridbag.setConstraints(authPanel, gbc);
        vncContainer.add(authPanel);
        if (inSeparateFrame) {
            vncFrame.pack();
        } else {
            validate();
        }
        authPanel.moveFocusToDefaultField();
        String pw = authPanel.getPassword();
        vncContainer.remove(authPanel);
        return pw;
    }

    void doProtocolInitialisation() throws IOException {
        rfb.writeClientInit();
        rfb.readServerInit();
        System.out.println("Desktop name is " + rfb.desktopName);
        System.out.println("Desktop size is " + rfb.framebufferWidth + " x " + rfb.framebufferHeight);
        setEncodings();
        showConnectionStatus(null);
    }

    int[] encodingsSaved;

    int nEncodingsSaved;

    void setEncodings() {
        setEncodings(false);
    }

    void autoSelectEncodings() {
        setEncodings(true);
    }

    void setEncodings(boolean autoSelectOnly) {
        if (options == null || rfb == null || !rfb.inNormalProtocol) return;
        int preferredEncoding = options.preferredEncoding;
        if (preferredEncoding == -1) {
            long kbitsPerSecond = rfb.kbitsPerSecond();
            if (nEncodingsSaved < 1) {
                System.out.println("Using Tight/ZRLE encodings");
                preferredEncoding = RfbProto.EncodingTight;
            } else if (kbitsPerSecond > 2000 && encodingsSaved[0] != RfbProto.EncodingHextile) {
                System.out.println("Throughput " + kbitsPerSecond + " kbit/s - changing to Hextile encoding");
                preferredEncoding = RfbProto.EncodingHextile;
            } else if (kbitsPerSecond < 1000 && encodingsSaved[0] != RfbProto.EncodingTight) {
                System.out.println("Throughput " + kbitsPerSecond + " kbit/s - changing to Tight/ZRLE encodings");
                preferredEncoding = RfbProto.EncodingTight;
            } else {
                if (autoSelectOnly) return;
                preferredEncoding = encodingsSaved[0];
            }
        } else {
            if (autoSelectOnly) return;
        }
        int[] encodings = new int[20];
        int nEncodings = 0;
        encodings[nEncodings++] = preferredEncoding;
        if (options.useCopyRect) {
            encodings[nEncodings++] = RfbProto.EncodingCopyRect;
        }
        if (preferredEncoding != RfbProto.EncodingTight) {
            encodings[nEncodings++] = RfbProto.EncodingTight;
        }
        if (preferredEncoding != RfbProto.EncodingZRLE) {
            encodings[nEncodings++] = RfbProto.EncodingZRLE;
        }
        if (preferredEncoding != RfbProto.EncodingHextile) {
            encodings[nEncodings++] = RfbProto.EncodingHextile;
        }
        if (preferredEncoding != RfbProto.EncodingZlib) {
            encodings[nEncodings++] = RfbProto.EncodingZlib;
        }
        if (preferredEncoding != RfbProto.EncodingCoRRE) {
            encodings[nEncodings++] = RfbProto.EncodingCoRRE;
        }
        if (preferredEncoding != RfbProto.EncodingRRE) {
            encodings[nEncodings++] = RfbProto.EncodingRRE;
        }
        if (options.compressLevel >= 0 && options.compressLevel <= 9) {
            encodings[nEncodings++] = RfbProto.EncodingCompressLevel0 + options.compressLevel;
        }
        if (options.jpegQuality >= 0 && options.jpegQuality <= 9) {
            encodings[nEncodings++] = RfbProto.EncodingQualityLevel0 + options.jpegQuality;
        }
        if (options.requestCursorUpdates) {
            encodings[nEncodings++] = RfbProto.EncodingXCursor;
            encodings[nEncodings++] = RfbProto.EncodingRichCursor;
            if (!options.ignoreCursorUpdates) encodings[nEncodings++] = RfbProto.EncodingPointerPos;
        }
        encodings[nEncodings++] = RfbProto.EncodingLastRect;
        encodings[nEncodings++] = RfbProto.EncodingNewFBSize;
        boolean encodingsWereChanged = false;
        if (nEncodings != nEncodingsSaved) {
            encodingsWereChanged = true;
        } else {
            for (int i = 0; i < nEncodings; i++) {
                if (encodings[i] != encodingsSaved[i]) {
                    encodingsWereChanged = true;
                    break;
                }
            }
        }
        if (encodingsWereChanged) {
            try {
                rfb.writeSetEncodings(encodings, nEncodings);
                if (vc != null) {
                    vc.softCursorFree();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            encodingsSaved = encodings;
            nEncodingsSaved = nEncodings;
        }
    }

    void setCutText(String text) {
        try {
            if (rfb != null && rfb.inNormalProtocol) {
                rfb.writeClientCutText(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setRecordingStatus(String fname) {
        synchronized (recordingSync) {
            sessionFileName = fname;
            recordingStatusChanged = true;
        }
    }

    boolean checkRecordingStatus() throws IOException {
        synchronized (recordingSync) {
            if (recordingStatusChanged) {
                recordingStatusChanged = false;
                if (sessionFileName != null) {
                    startRecording();
                    return true;
                } else {
                    stopRecording();
                }
            }
        }
        return false;
    }

    protected void startRecording() throws IOException {
        synchronized (recordingSync) {
            if (!recordingActive) {
                cursorUpdatesDef = options.choices[options.cursorUpdatesIndex].getSelectedItem();
                eightBitColorsDef = options.choices[options.eightBitColorsIndex].getSelectedItem();
                options.choices[options.cursorUpdatesIndex].select("Disable");
                options.choices[options.cursorUpdatesIndex].setEnabled(false);
                options.setEncodings();
                options.choices[options.eightBitColorsIndex].select("No");
                options.choices[options.eightBitColorsIndex].setEnabled(false);
                options.setColorFormat();
            } else {
                rfb.closeSession();
            }
            System.out.println("Recording the session in " + sessionFileName);
            rfb.startSession(sessionFileName);
            recordingActive = true;
        }
    }

    protected void stopRecording() throws IOException {
        synchronized (recordingSync) {
            if (recordingActive) {
                options.choices[options.cursorUpdatesIndex].select(cursorUpdatesDef);
                options.choices[options.cursorUpdatesIndex].setEnabled(true);
                options.setEncodings();
                options.choices[options.eightBitColorsIndex].select(eightBitColorsDef);
                options.choices[options.eightBitColorsIndex].setEnabled(true);
                options.setColorFormat();
                rfb.closeSession();
                System.out.println("Session recording stopped.");
            }
            sessionFileName = null;
            recordingActive = false;
        }
    }

    void readParameters() {
        host = readParameter("HOST", !inAnApplet);
        if (host == null) {
            host = getCodeBase().getHost();
            if (host.equals("")) {
                fatalError("HOST parameter not specified");
            }
        }
        port = readIntParameter("PORT", 5900);
        readPasswordParameters();
        String str;
        if (inAnApplet) {
            str = readParameter("Open New Window", false);
            if (str != null && str.equalsIgnoreCase("Yes")) inSeparateFrame = true;
        }
        showControls = true;
        str = readParameter("Show Controls", false);
        if (str != null && str.equalsIgnoreCase("No")) showControls = false;
        offerRelogin = true;
        str = readParameter("Offer Relogin", false);
        if (str != null && str.equalsIgnoreCase("No")) offerRelogin = false;
        showOfflineDesktop = false;
        str = readParameter("Show Offline Desktop", false);
        if (str != null && str.equalsIgnoreCase("Yes")) showOfflineDesktop = true;
        deferScreenUpdates = readIntParameter("Defer screen updates", 20);
        deferCursorUpdates = readIntParameter("Defer cursor updates", 10);
        deferUpdateRequests = readIntParameter("Defer update requests", 0);
        debugStatsExcludeUpdates = readIntParameter("DEBUG_XU", 0);
        debugStatsMeasureUpdates = readIntParameter("DEBUG_CU", 0);
        socketFactory = readParameter("SocketFactory", false);
    }

    private void readPasswordParameters() {
        String encPasswordParam = readParameter("ENCPASSWORD", false);
        if (encPasswordParam == null) {
            passwordParam = readParameter("PASSWORD", false);
        } else {
            byte[] pw = { 0, 0, 0, 0, 0, 0, 0, 0 };
            int len = encPasswordParam.length() / 2;
            if (len > 8) len = 8;
            for (int i = 0; i < len; i++) {
                String hex = encPasswordParam.substring(i * 2, i * 2 + 2);
                Integer x = new Integer(Integer.parseInt(hex, 16));
                pw[i] = x.byteValue();
            }
            byte[] key = { 23, 82, 107, 6, 35, 78, 88, 7 };
            DesCipher des = new DesCipher(key);
            des.decrypt(pw, 0, pw, 0);
            passwordParam = new String(pw);
        }
    }

    public String readParameter(String name, boolean required) {
        if (inAnApplet) {
            String s = getParameter(name);
            if ((s == null) && required) {
                fatalError(name + " parameter not specified");
            }
            return s;
        }
        for (int i = 0; i < mainArgs.length; i += 2) {
            if (mainArgs[i].equalsIgnoreCase(name)) {
                try {
                    return mainArgs[i + 1];
                } catch (Exception e) {
                    if (required) {
                        fatalError(name + " parameter not specified");
                    }
                    return null;
                }
            }
        }
        if (required) {
            fatalError(name + " parameter not specified");
        }
        return null;
    }

    int readIntParameter(String name, int defaultValue) {
        String str = readParameter(name, false);
        int result = defaultValue;
        if (str != null) {
            try {
                result = Integer.parseInt(str);
            } catch (NumberFormatException e) {
            }
        }
        return result;
    }

    void moveFocusToDesktop() {
        if (vncContainer != null) {
            if (vc != null && vncContainer.isAncestorOf(vc)) vc.requestFocus();
        }
    }

    public synchronized void disconnect() {
        System.out.println("Disconnecting");
        if (vc != null) {
            double sec = (System.currentTimeMillis() - vc.statStartTime) / 1000.0;
            double rate = Math.round(vc.statNumUpdates / sec * 100) / 100.0;
            int nRealRects = vc.statNumPixelRects;
            int nPseudoRects = vc.statNumTotalRects - vc.statNumPixelRects;
            System.out.println("Updates received: " + vc.statNumUpdates + " (" + nRealRects + " rectangles + " + nPseudoRects + " pseudo), " + rate + " updates/sec");
            int numRectsOther = nRealRects - vc.statNumRectsTight - vc.statNumRectsZRLE - vc.statNumRectsHextile - vc.statNumRectsRaw - vc.statNumRectsCopy;
            System.out.println("Rectangles:" + " Tight=" + vc.statNumRectsTight + "(JPEG=" + vc.statNumRectsTightJPEG + ") ZRLE=" + vc.statNumRectsZRLE + " Hextile=" + vc.statNumRectsHextile + " Raw=" + vc.statNumRectsRaw + " CopyRect=" + vc.statNumRectsCopy + " other=" + numRectsOther);
            int raw = vc.statNumBytesDecoded;
            int compressed = vc.statNumBytesEncoded;
            if (compressed > 0) {
                double ratio = Math.round((double) raw / compressed * 1000) / 1000.0;
                System.out.println("Pixel data: " + vc.statNumBytesDecoded + " bytes, " + vc.statNumBytesEncoded + " compressed, ratio " + ratio);
            }
        }
        if (rfb != null && !rfb.closed()) rfb.close();
        options.dispose();
        clipboard.dispose();
        if (rec != null) rec.dispose();
        if (inAnApplet) {
            showMessage("Disconnected");
        } else {
            System.exit(0);
        }
    }

    public synchronized void fatalError(String str) {
        System.out.println(str);
        if (inAnApplet) {
            Thread.currentThread().stop();
        } else {
            System.exit(1);
        }
    }

    public synchronized void fatalError(String str, Exception e) {
        if (rfb != null && rfb.closed()) {
            System.out.println("RFB thread finished");
            return;
        }
        System.out.println(str);
        e.printStackTrace();
        if (rfb != null) rfb.close();
        if (inAnApplet) {
            showMessage(str);
        } else {
            System.exit(1);
        }
    }

    void showMessage(String msg) {
        vncContainer.removeAll();
        Label errLabel = new Label(msg, Label.CENTER);
        errLabel.setFont(new Font("Helvetica", Font.PLAIN, 12));
        if (offerRelogin) {
            Panel gridPanel = new Panel(new GridLayout(0, 1));
            Panel outerPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
            outerPanel.add(gridPanel);
            vncContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 16));
            vncContainer.add(outerPanel);
            Panel textPanel = new Panel(new FlowLayout(FlowLayout.CENTER));
            textPanel.add(errLabel);
            gridPanel.add(textPanel);
            gridPanel.add(new ReloginPanel(this));
        } else {
            vncContainer.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 30));
            vncContainer.add(errLabel);
        }
        if (inSeparateFrame) {
            vncFrame.pack();
        } else {
            validate();
        }
    }

    public void stop() {
        System.out.println("Stopping applet");
        rfbThread = null;
    }

    public void destroy() {
        System.out.println("Destroying applet");
        vncContainer.removeAll();
        options.dispose();
        clipboard.dispose();
        if (rec != null) rec.dispose();
        if (rfb != null && !rfb.closed()) rfb.close();
        if (inSeparateFrame) vncFrame.dispose();
    }

    public void enableInput(boolean enable) {
        vc.enableInput(enable);
    }

    public void windowClosing(WindowEvent evt) {
        System.out.println("Closing window");
        if (rfb != null) disconnect();
        vncContainer.hide();
        if (!inAnApplet) {
            System.exit(0);
        }
    }

    public void windowActivated(WindowEvent evt) {
    }

    public void windowDeactivated(WindowEvent evt) {
    }

    public void windowOpened(WindowEvent evt) {
    }

    public void windowClosed(WindowEvent evt) {
    }

    public void windowIconified(WindowEvent evt) {
    }

    public void windowDeiconified(WindowEvent evt) {
    }
}
