package jaguar;

import ide.JaguarIDE;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;

/**
 * <p>
 * Jaguar, a <b>J</b>ava <b>A</b>daptable <b>G</b>raphical <b>U</b>ser <b>A</b>pplications <b>R</b>unner.
 * <p>
 * <img src="../resources/Jaguar.jpg">
 * <p> 
 * The main purpose of JAGUAR is regression/reference testing of entire systems,
 * it does this by running applications in a controlled environment.
 * <p>
 * @author peter <b>G</b>eneric <b>R</b>endering <b>O</b>f <b>O</b>rthogonal <b>T</b>iles <b>S</b>upported
 * <b>W</b>ith <b>A</b> <b>G</b>raphical <b>E</b>nvironment <b>R</b>unning <b>S</b>cript
 * 
 * <p>
 * For a railroad description of the scripting language 
 * @see JaguarVM#execute()
 */
public class Jaguar {

    public static final String version = "8.8";

    public static final String PALETTE_CHARS = "#BGCRMY=-bgcrmy ?";

    public static final int NONE = 0;

    public static final int LINE = 1;

    public static final int PAUSE = 2;

    private static final Color CANVASCOLOR = Color.PINK;

    public static final int LOGWHITE = 8;

    public static final int LOGBLACK = 1;

    public static final int LOGBLUE = 2;

    public static final int LOGGREEN = 4;

    public static final int LOGRED = 6;

    public static final int LOGYELLOW = 7;

    public static final int LOGBROWN = 14;

    public static final int LOGLIGHTYELLOW = 16;

    public static final int LOGGRAY = 15;

    public static final int LOGLIGHTGRAY = 17;

    public static final int LOGFIXED = 1;

    public static final int LOGPROPORTIONAL = 0;

    public static final int IDLE = 0;

    public static final int COMPILING = 1;

    public static final int RUNNING = 2;

    public static final int SLEEPING = 3;

    public static final int SCANNING = 4;

    private static final String[][] animatedChars = { { "-----", "=----", "-=---", "--=--", "---=-", "----=" }, { "_/_/_", "_|_|_", "_\\_\\_", "_|_|_" }, { " -|- ", " -/- ", " --- ", " -\\- " }, { "Zzzzz", "zZzzz", "zzZzz", "zzzZz", "zzzzZ", "zzzZz", "zzZzz", "zZzzz" }, { ".....", "|....", ".|...", "..|..", "...|.", "....|" } };

    private static final int MAX_VMCACHE_SIZE = 16;

    private static Robot robby;

    private static JTextArea ta;

    private static JFrame topFrame;

    private static JWindow topWindow;

    private static JaguarRectangle canvas;

    private static JaguarRectangle canvastile;

    private static JaguarRectangle canvascolumn;

    private static JaguarRectangle canvasline;

    private static JaguarOperatingSystem os;

    private static JaguarRectangle closebutton;

    private static JaguarRectangle screen;

    private static Rectangle topright;

    private static ArrayList tasktable;

    private static JaguarVM vm;

    private static Clipboard cb;

    private static Rectangle rectApp;

    private static int debugmode;

    private static String workingDir;

    private static JaguarRectangle screenbutton;

    private static String jagName;

    private static boolean list;

    private static int depth;

    private static File jagFile;

    private static int autoDelayForKeys;

    private static int autoDelayForMouse;

    private static boolean sleepOn;

    private static String topFrameCaption;

    private static int animationIndex;

    private static int animatedIconIndex;

    private static ImageIcon[][] animatedIcon;

    private static boolean animationEnabled;

    private static ArrayList vmcache;

    private static boolean ide;

    private static int autoDelayForType;

    private static boolean log;

    private static int loglines;

    private static BufferedWriter logwriter;

    private static boolean isWindows;

    private static int logmaxlines;

    private static int lognumber;

    private static String logprefix;

    private static int logForegroundColor;

    private static String logname;

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        tasktable = new ArrayList();
        debugmode = NONE;
        list = true;
        autoDelayForKeys = 0;
        autoDelayForType = 0;
        autoDelayForMouse = 0;
        cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        topFrame = new JFrame("Jaguar v" + version);
        topFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container topContentPane = topFrame.getContentPane();
        ta = new JTextArea("JAva Graphic User Adaptable Runner", 80, 25);
        ta.setFont(new Font("Courier", Font.BOLD, 14));
        JScrollPane scroller = new JScrollPane(ta);
        topContentPane.add(scroller);
        topFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        topFrame.pack();
        topFrame.setVisible(true);
        topFrame.toFront();
        topFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        topWindow = new JWindow(topFrame);
        setFocusable(true);
        try {
            robby = new Robot();
        } catch (AWTException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
        delays();
        os = new JaguarOperatingSystem();
        addLine("Jaguar version " + version + " running on " + os.getArch() + " " + os.getName() + " " + os.getVersion() + " with " + os.getAvailableProcessors() + " available processors");
        GraphicsConfiguration gc = topFrame.getGraphicsConfiguration();
        String device = gc.getDevice().getIDstring();
        ColorModel cm = gc.getColorModel();
        depth = cm.getPixelSize();
        String colormodel = cm.toString();
        Dimension dimScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screen = new JaguarRectangle("screen", 0, 0, dimScreenSize.width, dimScreenSize.height);
        rectApp = new Rectangle(screen.width / 4, screen.height / 4, screen.width / 2, screen.height / 2);
        addLine("Screen device is " + device + " with resolution " + dimScreenSize.width + "X" + dimScreenSize.height + " and colormodel " + colormodel);
        boolean numLockIsOn = presetLockKey("Num", KeyEvent.VK_NUM_LOCK);
        boolean capsLockIsOn = presetLockKey("Caps", KeyEvent.VK_CAPS_LOCK);
        boolean scrollLockIsOn = presetLockKey("Scroll", KeyEvent.VK_SCROLL_LOCK);
        Thread.yield();
        robby.waitForIdle();
        String s;
        if (args.length == 0) s = "autorun.jag"; else s = args[0];
        ide = false;
        if (s.startsWith("IDE:")) {
            ide = true;
            s = s.substring("IDE:".length());
        }
        jagFile = new File(s);
        jagName = jagFile.getName();
        topFrameCaption = "Jaguar v" + version + " - " + jagName;
        animationIndex = 0;
        animatedIconIndex = 0;
        animationEnabled = true;
        initAnimatedIcons();
        animateCaption(IDLE, 0, "idle");
        addLine("compiling " + s + " ...");
        workingDir = new File(s).getParent();
        logmaxlines = 0;
        loglines = 0;
        lognumber = 0;
        setLogPrefix(null);
        logname = null;
        logwriter = null;
        logForegroundColor = LOGBLACK;
        isWindows = (getOsName().toLowerCase().indexOf("windows") >= 0);
        vm = new JaguarVM(null, s);
        initScreen(vm.usesScreen());
        ta.addMouseListener(vm);
        vm.rectAdd(screen);
        vm.addRectLabels(screen);
        vmcache = new ArrayList();
        vmcache.add(vm);
        if (ide) vm.ide(); else vm.execute();
        robby.waitForIdle();
        resetLockKey("Scroll", KeyEvent.VK_SCROLL_LOCK, scrollLockIsOn);
        resetLockKey("Caps", KeyEvent.VK_CAPS_LOCK, capsLockIsOn);
        resetLockKey("Num", KeyEvent.VK_NUM_LOCK, numLockIsOn);
        waitForTasktableEmpty();
        topFrame.setVisible(false);
        System.exit(vm.getStatus());
    }

    private static void initAnimatedIcons() {
        animatedIcon = new ImageIcon[5][];
        animatedIcon[IDLE] = loadIcons("idle");
        animatedIcon[COMPILING] = loadIcons("compiling");
        animatedIcon[RUNNING] = loadIcons("running");
        animatedIcon[SLEEPING] = loadIcons("sleeping");
        animatedIcon[SCANNING] = loadIcons("scanning");
    }

    private static ImageIcon[] loadIcons(String name) {
        ArrayList icon = new ArrayList();
        for (int i = 0; true; i++) {
            String imgLocation = "images/" + name + String.valueOf(101 + i).substring(1) + ".jpg";
            URL imageURL = Jaguar.class.getResource(imgLocation);
            if (imageURL == null) break;
            icon.add(imageURL);
        }
        ImageIcon[] r = new ImageIcon[icon.size()];
        for (int i = 0; i < r.length; i++) r[i] = new ImageIcon((URL) icon.get(i), name);
        return r;
    }

    public static void animateCaption(int state, int pc, String line) {
        if (!animationEnabled) return;
        String[] a = (line + " NOP").trim().split("\\s+");
        if (animationIndex >= animatedChars[state].length) animationIndex = 0;
        String s = animatedChars[state][animationIndex];
        int plen = 3;
        if (state == RUNNING || state == SLEEPING) plen = String.valueOf(vm.getStmtSize()).length();
        String p = String.valueOf(10000 + pc).substring(5 - plen);
        String q = a[0].toUpperCase();
        if (q.length() > 16) q = q.substring(0, 16);
        while (q.length() < 16) q += " ";
        String animation = s + " " + p + " " + q;
        topFrame.setTitle(animation + " " + topFrameCaption);
        ++animationIndex;
        if (animatedIconIndex >= animatedIcon[state].length) animatedIconIndex = 0;
        topFrame.setIconImage(animatedIcon[state][animatedIconIndex].getImage());
        ++animatedIconIndex;
        boolean isfocusable = topWindow.isFocusable();
        topWindow.setFocusable(true);
        topWindow.getRootPane().invalidate();
        topWindow.getRootPane().repaint();
        Thread.yield();
        robby.waitForIdle();
        topWindow.setFocusable(isfocusable);
    }

    /**
	 * Intitialize screen for capturing
	 * 
	 * @param used	true means the application probably uses an interactive window so capture close button
	 */
    private static void initScreen(boolean used) {
        int x = screen.width;
        int y = screen.height;
        int w = x / 80;
        int h = y / 60;
        int dw = w / 4;
        int dh = h / 4;
        if (os.isMsWindows()) {
            dw = 1;
            dh = 1;
        }
        screenbutton = getRows("screenbutton", robby.createScreenCapture(new Rectangle(x - w - dw, dh, w, h)));
        closebutton = getRows("closebutton", robby.createScreenCapture(new Rectangle(x - w - dw, dh, w, h)));
        if (used) {
            addLine("Calibrating...");
            initPalette();
            createCanvas();
            addLine("+-canvas-+");
            for (int i = 0; i < canvastile.height; i++) addLine("| " + canvastile.getScanline(i) + " |");
            addLine("+--------+");
            addLine(canvasline.toString());
            addLine(canvascolumn.toString());
            String s = "close button";
            while (s.length() < w) s += "-";
            s = "+-" + s + "-+";
            addLine(s);
            for (int i = 0; i < closebutton.height; i++) {
                addLine("| " + closebutton.getScanline(i) + " |");
            }
            setFocusable(false);
            s = "";
            while (s.length() < w) s += "-";
            s = "+-" + s + "-+";
            addLine(s);
        }
    }

    /**
 * Helper method for initscreen()
 * TODO Figure out what is done here
 *
 */
    private static void createCanvas() {
        Color bg = ta.getBackground();
        String lines = ta.getText();
        ta.setBackground(CANVASCOLOR);
        ta.setText(null);
        ta.invalidate();
        topFrame.repaint();
        canvas = null;
        capture();
        canvastile = tile(canvas, canvas.width / 2, canvas.height / 2, 6, 6);
        int x = 0;
        while (x < canvas.width / 2 && !canvastile.isSameAsTile(screen, x, canvas.height / 2, 0)) ++x;
        int w = canvas.width - 6;
        while (w > canvas.width / 2 && !canvastile.isSameAsTile(screen, w, canvas.height / 2, 0)) --w;
        w += 6 - x;
        canvasline = tile(canvas, x, canvas.height / 2, w, 1);
        canvasline.setName("canvasrow");
        int y = canvas.height - 7;
        while (y > canvas.height / 2 && canvastile.isSameAsTile(screen, canvas.width / 2, y, 0)) --y;
        while (y > canvas.height / 2 && !canvastile.isSameAsTile(screen, canvas.width / 2, y, 0)) --y;
        y += 6;
        canvascolumn = tile(canvas, canvas.width / 2, 0, 1, y);
        canvascolumn.setName("canvascol");
        ta.setText(lines);
        ta.setBackground(bg);
        ta.invalidate();
        topFrame.repaint();
    }

    public static void setFocusable(boolean on) {
        topFrame.setFocusableWindowState(on);
        topFrame.setFocusable(on);
    }

    /**
	 * @param keyname - String
	 * @param keycode - int
	 * @return initial status of the key - boolean
	 */
    private static boolean presetLockKey(String keyname, int keycode) {
        boolean keyIsOn = getKeyState(keyname, keycode);
        if (keyIsOn) {
            keyPress(keycode);
            addLine(keyname + " Lock set off");
        } else addLine(keyname + " Lock is off");
        return keyIsOn;
    }

    /**
	 * @param keyname - String
	 * @param keycode - int
	 * @param keyIsOn - boolean
	 */
    private static void resetLockKey(String keyname, int keycode, boolean keyIsOn) {
        if (keyIsOn) {
            if (!getKeyState(keyname, keycode)) {
                keyPress(keycode);
                addLine(keyname + " Lock set on again");
            }
        } else {
            if (getKeyState(keyname, keycode)) {
                keyPress(keycode);
                addLine(keyname + " Lock set off again");
            }
        }
    }

    /**
	 * Helper method for presetLockKey and resetLockKey
	 * 
	 * @param keyname - String
	 * @param keycode - int
	 * @return on/off status of the key - boolean
	 */
    private static boolean getKeyState(String keyname, int keycode) {
        try {
            return Toolkit.getDefaultToolkit().getLockingKeyState(keycode);
        } catch (UnsupportedOperationException e) {
            switch(keycode) {
                case KeyEvent.VK_NUM_LOCK:
                    return keyEffect(KeyEvent.VK_KP_LEFT, '4');
                case KeyEvent.VK_CAPS_LOCK:
                    return keyEffect(KeyEvent.VK_A, 'A');
                default:
                    addLine("can not get the state of the " + keyname + " Lock key, unsupported by " + os.getName());
            }
        }
        return false;
    }

    /**
 * Helper method for getKeyState that gets keystate if java.awt.Toolkit can't do it.
 * This is done by some mysterious clicks and key presses in a temporary window.
 * 
 * @param vk (Caps Lock, Num Lock or Scroll Lock)
 * @param c
 * @return state of the virtual key
 */
    private static boolean keyEffect(int vk, char c) {
        JFrame keyframe = new JFrame("Jaguar v" + version + " - " + vk + " " + c);
        Container content = keyframe.getContentPane();
        Box vbox = Box.createVerticalBox();
        JTextField tf = new JTextField("= =");
        JTextField td = new JTextField("=" + c + "=");
        vbox.add(tf, BorderLayout.NORTH);
        vbox.add(td, BorderLayout.SOUTH);
        content.add(vbox);
        keyframe.pack();
        keyframe.setBounds(screen.width / 2 - 20, screen.height / 2 - 50, 40, 100);
        keyframe.setVisible(true);
        keyframe.toFront();
        robby.delay(100);
        Thread.yield();
        robby.waitForIdle();
        clickon(keyframe.getBounds());
        mouseAt(screen);
        keyPress(KeyEvent.VK_HOME);
        keyPress(KeyEvent.VK_EQUALS);
        keyPress(vk);
        keyPress(KeyEvent.VK_EQUALS);
        robby.delay(100);
        robby.waitForIdle();
        Thread.yield();
        boolean b = (tf.getText() + "   ").charAt(1) == c;
        keyframe.setVisible(false);
        return b;
    }

    /**
	 * Flash out the background with incourant color using a borderless window that fills
	 * the screen, and take a snapshot of the whole screen 
	 * 
	 * Helper method for Create canvas
	 * Also used in calibrate statement.
	 */
    public static void capture() {
        animationEnabled = false;
        setFocusable(false);
        sleep(100);
        if (robby != null) robby.waitForIdle();
        JFrame canvasFrame = new JFrame("Jaguar v" + version + " canvasFrame");
        canvasFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container topContentPane = canvasFrame.getContentPane();
        JPanel panel = new JPanel();
        Canvas c = new Canvas(canvasFrame.getGraphicsConfiguration());
        c.setBackground(CANVASCOLOR);
        c.setSize(screen.width, screen.height);
        panel.setBackground(CANVASCOLOR);
        panel.setSize(screen.width, screen.height);
        panel.add(c);
        topContentPane.add(panel);
        canvasFrame.setUndecorated(true);
        canvasFrame.pack();
        canvasFrame.setVisible(true);
        sleep(300);
        canvasFrame.toFront();
        canvasFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        canvasFrame.setLocation(0, 0);
        canvasFrame.setSize(screen.width, screen.height);
        sleep(600);
        if (canvas == null) {
            canvas = getRows("canvas", robby.createScreenCapture(screen));
        }
        swap(1);
        sleep(1000);
        screen = getRows("screen", robby.createScreenCapture(screen));
        canvasFrame.setVisible(false);
        canvasFrame.dispose();
        animationEnabled = true;
    }

    /**
	 * 
	 * @param vm
	 * @param name
	 */
    public static void calibrate(JaguarVM vm, String name) {
        String application = name;
        if (name == null || name.equals("")) application = "application";
        vm.setStatus(JaguarVM.OK);
        animationEnabled = false;
        topright = screen.coordinates(closebutton, 0, 0, null);
        animationEnabled = false;
        if (topright == null) {
            addLine("Could not find the close button of the " + application + " window, gonna look for the topright corner now");
            topright = findTopRight();
        }
        rectApp = findAppRect();
        JaguarRectangle r = vm.getRect("topright");
        r.x = topright.x;
        r.y = topright.y;
        r.width = topright.width;
        r.height = topright.height;
        vm.addRectLabels(r);
        debug(r.toString());
        JaguarRectangle appIcon = tile(screen, r.x, r.y, r.width, r.height);
        r = vm.getRect("topleft");
        r.x = rectApp.x;
        r.y = rectApp.y;
        r.width = topright.width;
        r.height = topright.height;
        vm.addRectLabels(r);
        debug(r.toString());
        r = vm.getRect("botleft");
        r.x = rectApp.x;
        r.y = rectApp.y + rectApp.height - topright.height;
        r.width = topright.width;
        r.height = topright.height;
        vm.addRectLabels(r);
        debug(r.toString());
        r = vm.getRect("botright");
        r.x = topright.x;
        r.y = rectApp.y + rectApp.height - topright.height;
        r.width = topright.width;
        r.height = topright.height;
        vm.addRectLabels(r);
        debug(r.toString());
        r = vm.getRect(application.toLowerCase());
        if (r == null) {
            r = new JaguarRectangle(application.toLowerCase(), 0, 0, 0, 0);
            vm.rectAdd(r);
        }
        r.setAppTopRight(appIcon);
        r.x = rectApp.x;
        r.y = rectApp.y;
        r.width = rectApp.width;
        r.height = rectApp.height;
        vm.addRectLabels(r);
        debug(r.toString());
        if (!application.toLowerCase().equals("application")) {
            r = vm.getRect("application");
            r.setAppTopRight(tile(screen, appIcon.x, appIcon.y, appIcon.width, appIcon.height));
            r.x = rectApp.x;
            r.y = rectApp.y;
            r.width = rectApp.width;
            r.height = rectApp.height;
            vm.addRectLabels(r);
            debug(r.toString());
        }
        animationEnabled = true;
    }

    /**
	 * @return r - Rectangle
	 */
    private static Rectangle findTopRight() {
        Rectangle r = new Rectangle();
        int x = canvasline.x;
        int y = canvascolumn.y;
        int w = canvasline.x + canvasline.width;
        int h = canvascolumn.y + canvascolumn.height;
        while (y < h && canvasline.isSameAsTile(screen, x, y, 0)) {
            ++y;
        }
        if (y == canvascolumn.y) y = screenbutton.y;
        r.y = y;
        y = canvascolumn.y;
        x = w - 1;
        while (x > 0 && canvascolumn.isSameAsTile(screen, x, y, 0)) {
            x--;
        }
        if (x == w - 1 || x == 0) x = screenbutton.x + screenbutton.width - 1;
        r.x = x + 1 - screenbutton.width;
        r.width = screenbutton.width;
        r.height = screenbutton.height;
        return r;
    }

    private static Rectangle findAppRect() {
        Rectangle r = new Rectangle();
        int x = canvas.x;
        int y = topright.y;
        int w = canvas.x + canvas.width;
        int h = canvas.y + canvas.height;
        r.y = y;
        while (y < h && !canvasline.isSameAsTile(screen, x, y, 0)) {
            ++y;
        }
        if (y == h) {
            y = topFrame.getY() + topFrame.getHeight();
        }
        r.height = y - r.y;
        y = canvas.y;
        while (x < w && canvascolumn.isSameAsTile(screen, x, y, 0)) {
            x++;
        }
        if (x == w) {
            x = topFrame.getX();
        }
        r.x = x;
        r.width = topright.x + topright.width - r.x;
        return r;
    }

    public static JaguarRectangle tile(JaguarRectangle b, int x, int y, int w, int h) {
        JaguarRectangle r = new JaguarRectangle(b.getName() + ".tile", x, y, w, h);
        for (int i = 0; i < h; i++) {
            String sl = b.getScanline(y + i);
            if (sl != null && x >= 0 && x + w <= b.width) sl = sl.substring(x, x + w);
            r.setScanline(i, sl);
        }
        return r;
    }

    private static char colorletter(ColorModel cm, int i) {
        int r, g, b, h, c;
        r = cm.getRed(i) >> 2;
        g = cm.getGreen(i) >> 2;
        b = cm.getBlue(i) >> 2;
        if (r == g && r == b) {
            if (r > 0x2f) return ' ';
            if (r > 0x1f) return '-';
            if (r > 0x0f) return '=';
            return '#';
        }
        if (r > 0x2f | g > 0x2f | b > 0x2f) h = 8; else h = 0;
        int rbit = r >> 5;
        int gbit = g >> 5;
        int bbit = b >> 5;
        if (rbit == gbit && rbit == bbit) {
            rbit = 1;
            gbit = 1;
            bbit = 1;
            if (r <= g && r <= b) rbit = 0;
            if (g <= r && g <= b) gbit = 0;
            if (b <= g && b <= r) bbit = 0;
        }
        c = (rbit << 2) | (gbit << 1) | bbit | h;
        return PALETTE_CHARS.charAt(c);
    }

    private static void initPalette() {
        ColorModel cm = robby.createScreenCapture(new Rectangle(0, 0, 1, 1)).getColorModel();
        String s = "'";
        s += colorletter(cm, 0x000000);
        s += colorletter(cm, 0x000080);
        s += colorletter(cm, 0x008000);
        s += colorletter(cm, 0x008080);
        s += colorletter(cm, 0x800000);
        s += colorletter(cm, 0x800080);
        s += colorletter(cm, 0x808000);
        s += colorletter(cm, 0x7f7f7f);
        s += colorletter(cm, 0xbfbfbf);
        s += colorletter(cm, 0x0000ff);
        s += colorletter(cm, 0x00ff00);
        s += colorletter(cm, 0x00ffff);
        s += colorletter(cm, 0xff0000);
        s += colorletter(cm, 0xff00ff);
        s += colorletter(cm, 0xffff00);
        s += colorletter(cm, 0xffffff);
        s += "'";
        addLine("'0123456789abcdef' Turbo palette");
        addLine(s);
    }

    public static void enter() {
        keyPress(KeyEvent.VK_ENTER);
        Thread.yield();
    }

    public static void keyPress(int vk) {
        Thread.yield();
        robby.waitForIdle();
        try {
            robby.keyPress(vk);
            robby.keyRelease(vk);
        } catch (IllegalArgumentException e) {
            vm.setError("invalid keycode " + vk);
        }
        robby.waitForIdle();
        Thread.yield();
    }

    public static void type(String text) {
        int saveAutoDelayForKeys = autoDelayForKeys;
        autoDelayForKeys = 0;
        char[] c = nlOrTab(text).toCharArray();
        for (int i = 0; i < c.length; i++) {
            int key = c[i];
            if (key >= 'a' && key <= 'z') keyPress(key - 'a' + 'A'); else if (key >= 'A' && key <= 'Z') shiftKeyPress(key); else if (key >= '0' && key <= '9') keyPress(key); else {
                switch(key) {
                    case '\n':
                        keyPress(KeyEvent.VK_ENTER);
                        break;
                    case '\t':
                        keyPress(KeyEvent.VK_TAB);
                        break;
                    case '`':
                        keyPress(KeyEvent.VK_BACK_QUOTE);
                        break;
                    case '-':
                        keyPress(KeyEvent.VK_MINUS);
                        break;
                    case '=':
                        keyPress(KeyEvent.VK_EQUALS);
                        break;
                    case '[':
                        keyPress(KeyEvent.VK_OPEN_BRACKET);
                        break;
                    case ']':
                        keyPress(KeyEvent.VK_CLOSE_BRACKET);
                        break;
                    case '\\':
                        keyPress(KeyEvent.VK_BACK_SLASH);
                        break;
                    case ';':
                        keyPress(KeyEvent.VK_SEMICOLON);
                        break;
                    case '\'':
                        keyPress(KeyEvent.VK_QUOTE);
                        break;
                    case ',':
                        keyPress(KeyEvent.VK_COMMA);
                        break;
                    case '.':
                        keyPress(KeyEvent.VK_PERIOD);
                        break;
                    case '/':
                        keyPress(KeyEvent.VK_SLASH);
                        break;
                    case ' ':
                        keyPress(KeyEvent.VK_SPACE);
                        break;
                    case '~':
                        shiftKeyPress(KeyEvent.VK_BACK_QUOTE);
                        break;
                    case '!':
                        shiftKeyPress(KeyEvent.VK_1);
                        break;
                    case '@':
                        shiftKeyPress(KeyEvent.VK_2);
                        break;
                    case '#':
                        shiftKeyPress(KeyEvent.VK_3);
                        break;
                    case '$':
                        shiftKeyPress(KeyEvent.VK_4);
                        break;
                    case '%':
                        shiftKeyPress(KeyEvent.VK_5);
                        break;
                    case '^':
                        shiftKeyPress(KeyEvent.VK_6);
                        break;
                    case '&':
                        shiftKeyPress(KeyEvent.VK_7);
                        break;
                    case '*':
                        shiftKeyPress(KeyEvent.VK_8);
                        break;
                    case '(':
                        shiftKeyPress(KeyEvent.VK_9);
                        break;
                    case ')':
                        shiftKeyPress(KeyEvent.VK_0);
                        break;
                    case '_':
                        shiftKeyPress(KeyEvent.VK_MINUS);
                        break;
                    case '+':
                        shiftKeyPress(KeyEvent.VK_EQUALS);
                        break;
                    case '{':
                        shiftKeyPress(KeyEvent.VK_OPEN_BRACKET);
                        break;
                    case '}':
                        shiftKeyPress(KeyEvent.VK_CLOSE_BRACKET);
                        break;
                    case '|':
                        shiftKeyPress(KeyEvent.VK_BACK_SLASH);
                        break;
                    case ':':
                        shiftKeyPress(KeyEvent.VK_SEMICOLON);
                        break;
                    case '"':
                        shiftKeyPress(KeyEvent.VK_QUOTE);
                        break;
                    case '<':
                        shiftKeyPress(KeyEvent.VK_COMMA);
                        break;
                    case '>':
                        shiftKeyPress(KeyEvent.VK_PERIOD);
                        break;
                    case '?':
                        shiftKeyPress(KeyEvent.VK_SLASH);
                        break;
                }
            }
            if (autoDelayForType > 0) sleep(autoDelayForType);
        }
        autoDelayForKeys = saveAutoDelayForKeys;
        delayForKeys();
    }

    /**
	 * @param text
	 * @return text with \n and \t replaced by newline and tab characters
	 * <p>
	 * care is taken to get the desired sequence "D:\temp\noname.txt"
	 * when the source was "D:\\temp\\noname.txt"
	 */
    private static String nlOrTab(String text) {
        String s = text;
        int i = s.indexOf("\\");
        while (i >= 0 && i + 1 < s.length()) {
            char ch = s.charAt(i + 1);
            switch(ch) {
                case 'n':
                    s = s.substring(0, i) + "\n" + s.substring(i + 2);
                    break;
                case 't':
                    s = s.substring(0, i) + "\t" + s.substring(i + 2);
                    break;
                case '\\':
                    s = s.substring(0, i) + "\\" + s.substring(i + 2);
                    break;
                default:
                    s = s.substring(0, i) + s.substring(i + 1);
                    break;
            }
            i = s.indexOf("\\", i + 1);
        }
        return s;
    }

    /**
	 * @param vk
	 */
    public static void altKeyPress(int vk) {
        modKeyPress(KeyEvent.VK_ALT, vk);
        delayForKeys();
    }

    private static void delays() {
        robby.setAutoWaitForIdle(true);
        robby.setAutoDelay(1);
    }

    private static void nodelays() {
        robby.setAutoWaitForIdle(false);
        robby.setAutoDelay(0);
    }

    /**
	 * @param vk
	 */
    public static void ctrlKeyPress(int vk) {
        modKeyPress(KeyEvent.VK_CONTROL, vk);
        delayForKeys();
    }

    private static void modKeyPress(int vk_mod, int vk) {
        boolean pause = (vk_mod != KeyEvent.VK_SHIFT);
        nodelays();
        Thread.yield();
        robby.waitForIdle();
        robby.keyPress(vk_mod);
        if (pause) {
            robby.delay(100);
            robby.waitForIdle();
        }
        Thread.yield();
        try {
            robby.keyPress(vk);
            robby.delay(20);
            robby.waitForIdle();
            Thread.yield();
            robby.keyRelease(vk);
        } catch (IllegalArgumentException e) {
            String mod = String.valueOf(vk_mod);
            String k = String.valueOf(vk);
            switch(vk_mod) {
                case KeyEvent.VK_SHIFT:
                    mod = "Shift";
                    break;
                case KeyEvent.VK_ALT:
                    mod = "Alt";
                    break;
                case KeyEvent.VK_CONTROL:
                    mod = "Ctrl";
                    break;
            }
            if (vk >= 'A' && vk <= 'Z') k = String.valueOf((char) vk);
            if (vk >= '0' && vk <= '9') k = String.valueOf((char) vk);
            vm.setError("invalid keycode " + mod + "+" + k);
        }
        if (pause) {
            robby.delay(50);
            robby.waitForIdle();
        }
        Thread.yield();
        robby.keyRelease(vk_mod);
        if (pause) {
            robby.delay(100);
        }
        Thread.yield();
        robby.waitForIdle();
        delays();
    }

    /**
	 * @param vk
	 */
    public static void shiftKeyPress(int vk) {
        modKeyPress(KeyEvent.VK_SHIFT, vk);
        delayForKeys();
    }

    public static void addLine(String string) {
        if (list) {
            int lines = ta.getLineCount();
            if (lines > 1500) {
                String txt = ta.getText();
                int i = txt.indexOf("\n") + 1;
                ta.setText(txt.substring(i));
            }
            ta.append("\n" + string);
            ta.setCaretPosition(ta.getText().lastIndexOf('\n') + 1);
            topFrame.repaint();
            System.out.println(string);
            if (ide && vm != null) {
                JaguarIDE jide = vm.getIDE();
                if (jide != null) jide.console(string + "\n");
            }
        }
        robby.waitForIdle();
        Thread.yield();
    }

    public static JaguarRectangle getRows(String name, BufferedImage bi) {
        int h = bi.getHeight();
        int w = bi.getWidth();
        ColorModel cm = bi.getColorModel();
        int[] c = bi.getRGB(0, 0, w, h, null, 0, w);
        JaguarRectangle r = new JaguarRectangle(name, 0, 0, w, h);
        char[] ry = new char[w];
        int cache = c[0];
        char cc = colorletter(cm, cache);
        int ci = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (c[ci] != cache) {
                    cache = c[ci];
                    cc = colorletter(cm, c[ci]);
                }
                ci++;
                ry[x] = cc;
            }
            r.setScanline(y, String.valueOf(ry));
        }
        return r;
    }

    public static void clickon(Rectangle d) {
        mouseAt(d);
        clickLeft();
    }

    public static void mouseAt(Rectangle d) {
        robby.waitForIdle();
        robby.mouseMove(d.x + d.width / 2, d.y + d.height / 2);
    }

    public static void clickLeft() {
        robby.waitForIdle();
        robby.mousePress(InputEvent.BUTTON1_MASK);
        robby.delay(20);
        robby.mouseRelease(InputEvent.BUTTON1_MASK);
        delayForMouse();
    }

    public static void clickMiddle() {
        robby.waitForIdle();
        robby.mousePress(InputEvent.BUTTON3_MASK);
        robby.delay(20);
        robby.mouseRelease(InputEvent.BUTTON3_MASK);
        delayForMouse();
    }

    public static void clickRight() {
        robby.waitForIdle();
        robby.mousePress(InputEvent.BUTTON2_MASK);
        robby.delay(20);
        robby.mouseRelease(InputEvent.BUTTON2_MASK);
        delayForMouse();
    }

    public static void dblclickLeft() {
        clickLeft();
        robby.delay(40);
        clickLeft();
        delayForMouse();
    }

    public static void dblclickMiddle() {
        clickMiddle();
        robby.delay(40);
        clickMiddle();
        delayForMouse();
    }

    public static void dblclickRight() {
        clickRight();
        robby.delay(40);
        clickRight();
        delayForMouse();
    }

    /**
	 * @param times
	 */
    public static void multyclickLeft(int times) {
        for (int i = 0; i < times; i++) {
            clickLeft();
            robby.delay(40);
        }
        delayForMouse();
    }

    /**
	 * @param times
	 */
    public static void multyclickMiddle(int times) {
        for (int i = 0; i < times; i++) {
            clickMiddle();
            robby.delay(40);
        }
        delayForMouse();
    }

    /**
	 * @param times
	 */
    public static void multyclickRight(int times) {
        for (int i = 0; i < times; i++) {
            clickRight();
            robby.delay(40);
        }
        delayForMouse();
    }

    public static void pressLeft() {
        robby.waitForIdle();
        robby.mousePress(InputEvent.BUTTON1_MASK);
        delayForMouse();
    }

    public static void pressMiddle() {
        robby.waitForIdle();
        robby.mousePress(InputEvent.BUTTON3_MASK);
        delayForMouse();
    }

    public static void pressRight() {
        robby.waitForIdle();
        robby.mousePress(InputEvent.BUTTON2_MASK);
        delayForMouse();
    }

    public static void releaseLeft() {
        robby.waitForIdle();
        robby.mouseRelease(InputEvent.BUTTON1_MASK);
        delayForMouse();
    }

    public static void releaseMiddle() {
        robby.waitForIdle();
        robby.mouseRelease(InputEvent.BUTTON3_MASK);
        delayForMouse();
    }

    public static void releaseRight() {
        robby.waitForIdle();
        robby.mouseRelease(InputEvent.BUTTON2_MASK);
        delayForMouse();
    }

    /**
	 * sleep autoDelayForKeys milliseconds
	 */
    private static void delayForKeys() {
        if (autoDelayForKeys > 0) sleep(autoDelayForKeys);
    }

    /**
	 * sleep autoDelayForMouse milliseconds
	 */
    private static void delayForMouse() {
        if (autoDelayForMouse > 0) sleep(autoDelayForMouse);
    }

    public static void sleep(int i) {
        robby.waitForIdle();
        Thread.yield();
        sleepOn = true;
        try {
            for (int t = 0; t < i; t += 20) {
                Thread.sleep(20);
                robby.waitForIdle();
                Thread.yield();
                if (!sleepOn) return;
                animateCaption(SLEEPING, vm.getPc() - 1, "WAIT");
            }
            Thread.sleep(i % 20);
            robby.waitForIdle();
            Thread.yield();
        } catch (Exception e) {
            robby.waitForIdle();
            Thread.yield();
        }
        robby.waitForIdle();
        Thread.yield();
    }

    /**
	 * 
	 * @param r - JaguarRectangle
	 * @param fill 0 boolean
	 * <p>
	 * prints the given rectangle of the captured screen
	 * @see #capture()
	 */
    public static void dump(JaguarRectangle r, boolean fill) {
        int x = r.x;
        int y = r.y;
        int w = r.width;
        int h = r.height;
        if (x < 0 || x >= screen.width) x = 0;
        if (y < 0 || y >= screen.height) y = 0;
        if (w < 1 || x + w >= screen.width) w = screen.width - x;
        if (h < 1 || y + h >= screen.height) h = screen.height - y;
        if (fill) r.copyScanlines(tile(screen, x, y, w, h));
        for (int i = 0; i < h; i++) addLine(screen.getScanline(y + i).substring(x, x + w));
    }

    public static void wheel(int i) {
        robby.mouseWheel(i);
        delayForMouse();
    }

    public static void fetch(JaguarRectangle r) {
        try {
            JaguarRectangle s = getRows(r.getName(), robby.createScreenCapture(r));
            setScanlines(r, s);
        } catch (Exception e) {
            e.printStackTrace();
            addLine(e.getMessage());
        }
    }

    private static void setScanlines(JaguarRectangle r, JaguarRectangle scanline) {
        for (int y = 0; y < r.height; y++) {
            r.setScanline(y, scanline.getScanline(y));
        }
    }

    public static void draw(JaguarRectangle r) {
        int w = String.valueOf(r.height - 1).length();
        for (int y = 0; y < r.height; y++) {
            String s = String.valueOf(y);
            while (s.length() < w) s = "0" + s;
            String scanline = r.getScanline(y);
            if (scanline == null) scanline = "null";
            addLine("draw " + r.getName() + " " + s + " " + scanline.replace(' ', '_'));
        }
    }

    public static JaguarImage createJaguarImage(JaguarRectangle r) {
        robby.delay(20);
        robby.waitForIdle();
        return new JaguarImage(robby.createScreenCapture(r));
    }

    /**
	 * @param string
	 */
    public static void debug(String string) {
        switch(debugmode) {
            case NONE:
                break;
            case LINE:
                addLine(string.replaceAll("\\\\n", "\n"));
                break;
            case PAUSE:
                addLine(string);
                new JaguarMessage("Debug:\n" + string.replaceAll("\\\\n", "\n") + " ");
                break;
        }
    }

    /**
	 * @return the robby
	 */
    public static Robot getRobby() {
        return robby;
    }

    public static String getJagName() {
        return jagName;
    }

    /**
	 * @return the os name in lowercase
	 */
    public static String getOsName() {
        return os.getName().toLowerCase();
    }

    /**
	 * @return the debugmode
	 */
    public static int getDebugmode() {
        return debugmode;
    }

    /**
	 * @return the debug status as as string 
	 */
    public static String getDebug() {
        switch(debugmode) {
            case NONE:
                return "OFF";
            case LINE:
                return "ON";
            case PAUSE:
                return "POPUP";
        }
        return "OFF";
    }

    /**
	 * @param debugmode the debugmode to set
	 */
    public static void setDebugmode(int debugmode) {
        Jaguar.debugmode = debugmode;
    }

    /**
	 * @return the cb
	 */
    public static Clipboard getCb() {
        return cb;
    }

    /**
	 * @return the rectApp
	 */
    public static Rectangle getRectApp() {
        return rectApp;
    }

    /**
	 * @return the code
	 */
    public static JaguarVM getCode() {
        return vm;
    }

    public static void start(String name, String args) {
        JaguarThread t = new JaguarThread(name, args);
        tasktable.add(t);
        t.start();
        Thread.yield();
        while (!t.isBusy()) {
            Thread.yield();
        }
        robby.waitForIdle();
        robby.delay(3500);
        robby.waitForIdle();
    }

    public static void stopped(String name, int rc) {
        vm.setStop(name + " ended, rc=" + rc);
        for (int i = 0; i < tasktable.size(); i++) {
            JaguarThread proc = (JaguarThread) tasktable.get(i);
            if (proc.getName().equals(name)) {
                tasktable.remove(i);
                return;
            }
        }
    }

    private static void waitForTasktableEmpty() {
        int checkMax = 5;
        while (tasktable.size() > 0 && checkMax > 0) {
            --checkMax;
            Thread.yield();
            for (int i = 0; i < tasktable.size(); i++) {
                JaguarThread proc = (JaguarThread) tasktable.get(i);
                try {
                    proc.join(1000);
                    if (proc.isAlive()) {
                        addLine(proc.getName() + " is running with priority " + proc.getPriority());
                    }
                } catch (InterruptedException e) {
                    addLine(proc.getName() + " " + e.getMessage());
                }
            }
        }
    }

    public static void stop(String name, String msg) {
        for (int i = 0; i < tasktable.size(); i++) {
            JaguarThread t = (JaguarThread) tasktable.get(i);
            if (t.getName().equals(name)) {
                addLine("stopping " + name + " " + msg);
                t.stop();
                return;
            }
        }
        vm.setError(name + " is not running");
    }

    /**
	 * @param r - JaguarRectangle
	 * @return true if appWindow is visible and has focus otherwise false
	 */
    public static boolean isAppWindow(JaguarRectangle r) {
        JaguarRectangle icon = r.getAppTopRight();
        if (icon == null && r.getName().startsWith("#")) {
            int x = r.x + r.width - closebutton.width;
            int y = r.y;
            int w = closebutton.width;
            int h = closebutton.height;
            int dw = screen.width / 320;
            int dh = screen.height / 240;
            if (os.isMsWindows()) {
                dw++;
                dh++;
            }
            icon = new JaguarRectangle(r.getName() + ".icon", x - dw, y + dh, w, h);
            icon.copyScanlines(closebutton);
            r.setAppTopRight(icon);
            robby.delay(500);
            robby.waitForIdle();
        }
        if (icon == null) return false;
        JaguarRectangle v = new JaguarRectangle(r.getName() + ".viewbox", r.x + r.width - 2 * icon.width, r.y, 2 * icon.width, 2 * icon.height);
        fetch(v);
        if (v.coordinates(icon, 0, 0, null) == null) return false;
        icon = r.getLogo();
        if (icon == null) return true;
        fetch(r);
        return r.coordinates(icon, 0, 0, null) != null;
    }

    /**
	 * 
	 * @param r - JaguarRectangle
	 * @param maxtabs - int
	 * <p>
	 * make sure the windowframe with rectangle r is on the screen 
	 */
    public static void makeTopWindow(JaguarRectangle r, int maxtabs) {
        setFocusable(true);
        int tabcount = 0;
        vm.setStatus(JaguarVM.OK);
        while (tabcount < maxtabs && !isAppWindow(r)) {
            tabcount++;
            altShiftTab();
            robby.delay(1000);
            robby.waitForIdle();
        }
        if (isAppWindow(r)) {
            JaguarRectangle logo = r.getLogo();
            if (logo != null) {
                clickon(logo);
            }
        } else {
            if (!r.getName().startsWith("#")) {
                if (r.getAppTopRight() == null) {
                    vm.setError("trying to make a non application rectangle the top window");
                    return;
                }
                addLine("can not make " + r.getName() + " the active window on top");
                vm.setStatus(JaguarVM.NOK);
            }
        }
        setFocusable(false);
    }

    private static void altShiftTab() {
        robby.keyPress(KeyEvent.VK_ALT);
        robby.waitForIdle();
        robby.delay(40);
        robby.keyPress(KeyEvent.VK_SHIFT);
        robby.delay(40);
        keyPress(KeyEvent.VK_TAB);
        robby.waitForIdle();
        robby.delay(40);
        robby.keyRelease(KeyEvent.VK_SHIFT);
        robby.delay(40);
        robby.keyRelease(KeyEvent.VK_ALT);
        robby.delay(500);
        robby.waitForIdle();
    }

    /**
	 * @param tabcount
	 */
    public static void swap(int tabcount) {
        robby.keyPress(KeyEvent.VK_ALT);
        for (int i = 0; i < tabcount; i++) {
            robby.waitForIdle();
            robby.delay(40);
            keyPress(KeyEvent.VK_TAB);
            robby.waitForIdle();
            robby.delay(40);
            delayForKeys();
        }
        robby.keyRelease(KeyEvent.VK_ALT);
        robby.delay(500);
        robby.waitForIdle();
    }

    public static void addMouseListener(JaguarVM sub) {
        ta.addMouseListener(sub);
    }

    public static void removeMouseListener(JaguarVM sub) {
        ta.removeMouseListener(sub);
    }

    /**
	 * @return the workingDir
	 */
    public static String getWorkingDir() {
        return workingDir;
    }

    /**
	 * @param r
	 */
    public static void select(JaguarRectangle r) {
        robby.mouseMove(r.x + 1, r.y + 1);
        pressLeft();
        sleep(50);
        robby.mouseMove(r.x + r.width / 2 - 1, r.y + r.height / 2 - 1);
        sleep(50);
        robby.mouseMove(r.x + r.width - 2, r.y + r.height - 2);
        sleep(50);
        releaseLeft();
        sleep(50);
    }

    public static JaguarProcess getTask(String name) {
        if (name.endsWith(".err") || name.endsWith(".out")) name = name.substring(0, name.length() - 4);
        for (int i = 0; i < tasktable.size(); i++) {
            JaguarThread proc = (JaguarThread) tasktable.get(i);
            if (proc.getName().equals(name)) {
                return proc.getProc();
            }
        }
        return null;
    }

    public static boolean isList() {
        return list;
    }

    public static void setList(boolean list) {
        Jaguar.list = list;
    }

    /**
	 * @return Returns the depth.
	 */
    public static int getDepth() {
        return depth;
    }

    /**
	 * @return Returns the jagFile.
	 */
    public static File getJagFile() {
        return jagFile;
    }

    /**
	 * @param i The autoDelayForKeys to set.
	 */
    public static void setAutoDelayForKeys(int i) {
        autoDelayForKeys = i;
    }

    /**
	 * @param i The autoDelayForMouse to set.
	 */
    public static void setAutoDelayForMouse(int i) {
        autoDelayForMouse = i;
    }

    /**
	 * @param i The autoDelayForType to set.
	 */
    public static void setAutoDelayForType(int i) {
        autoDelayForType = i;
    }

    /**
	 * got a signal from a VM that it is been interrupted
	 * so if this thread is looping in Sleep wake him up
	 *
	 */
    public static void wakeUp() {
        sleepOn = false;
    }

    /**
	 * @param caller 
	 * @return the JaguarVM with the vmID from the cache,
	 * create and cache a new one when not yet in cache or in IDE mode
	 */
    public static JaguarVM getVmcache(JaguarVM caller, String vmID) {
        if (ide) return new JaguarVM(caller, vmID);
        for (int i = 0; i < vmcache.size(); i++) {
            JaguarVM vm = (JaguarVM) vmcache.get(i);
            if (vmID.equalsIgnoreCase(vm.getVmID())) {
                if (i > 0) {
                    vmcache.remove(i);
                    vmcache.add(0, vm);
                }
                return vm;
            }
        }
        JaguarVM vm = new JaguarVM(caller, vmID);
        addVmcache(vm);
        return vm;
    }

    /**
	 * @param vm the JaguarVM to cache
	 */
    private static void addVmcache(JaguarVM vm) {
        vmcache.add(0, vm);
        while (vmcache.size() > MAX_VMCACHE_SIZE) vmcache.remove(MAX_VMCACHE_SIZE);
    }

    /**
	 * @return the log
	 */
    public static boolean isLog() {
        return log;
    }

    /**
	 * @param lg the log to set
	 */
    public static void setLog(boolean lg) {
        log = lg;
    }

    public static void setLogPrefix(String prefix) {
        if (prefix != null && prefix.length() == 0) logprefix = null; else logprefix = prefix;
        if (logprefix == null) {
            logprefix = getJagName();
            int i = logprefix.lastIndexOf('.');
            if (i > 0) logprefix = logprefix.substring(0, i);
        }
    }

    public static String getLogDate() {
        long msec = System.currentTimeMillis();
        Date today = new Date(msec);
        String output;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", new Locale("us US"));
        output = formatter.format(today);
        return output;
    }

    public static String getLogTime() {
        long msec = System.currentTimeMillis();
        Date today = new Date(msec);
        String output;
        SimpleDateFormat formatter = new SimpleDateFormat("HHmmss", new Locale("us US"));
        output = formatter.format(today);
        return output;
    }

    public static void logInit() throws IOException {
        String logdir = getWorkingDir() + File.separator + "logs";
        String yyyymmdd = getLogDate();
        String hhmmss = getLogTime();
        int yyyy = Integer.parseInt(yyyymmdd.substring(0, 4), 10);
        int mo = Integer.parseInt(yyyymmdd.substring(4, 6), 10);
        int dy = Integer.parseInt(yyyymmdd.substring(6), 10);
        int hr = Integer.parseInt(hhmmss.substring(0, 2), 10);
        int min = Integer.parseInt(hhmmss.substring(2, 4), 10);
        int sec = Integer.parseInt(hhmmss.substring(4), 10);
        ++lognumber;
        logname = logdir + File.separator + logprefix + ".jaglog" + yyyymmdd + hhmmss + "_" + lognumber + ".rtf";
        File logfile = new File(logname);
        new File(logdir).mkdir();
        logwriter = new BufferedWriter(new FileWriter(logfile));
        logwriter.write("{\\rtf1\\ansi\\ansicpg1252\\uc1\\deff0");
        logwriter.newLine();
        if (Jaguar.getOsName().toLowerCase().indexOf("linux") >= 0) {
            logwriter.write("{\\fonttbl{\\f0\\fnil\\fprq2{\\*\\panose 02020603050405020304}Times New Roman;}");
            logwriter.newLine();
            logwriter.write("{\\f1\\fnil\\fprq1{\\*\\panose 02070409020205020404}Courier{\\*\\falt Courier New};}");
        } else {
            logwriter.write("{\\fonttbl{\\f0\\fnil\\fprq2 DejaVu Sans;}");
            logwriter.newLine();
            logwriter.write("{\\f1\\fnil\\fprq1 Courier;}");
        }
        logwriter.newLine();
        logwriter.write("}{\\colortbl;" + "\\red0\\green0\\blue0;" + "\\red0\\green0\\blue255;" + "\\red0\\green255\\blue255;" + "\\red0\\green255\\blue0;" + "\\red255\\green0\\blue255;" + "\\red255\\green0\\blue0;" + "\\red255\\green255\\blue0;" + "\\red255\\green255\\blue255;" + "\\red0\\green0\\blue128;" + "\\red0\\green128\\blue128;" + "\\red0\\green128\\blue0;" + "\\red128\\green0\\blue128;" + "\\red128\\green0\\blue0;" + "\\red128\\green128\\blue0;" + "\\red128\\green128\\blue128;" + "\\red255\\green255\\blue220;" + "\\red192\\green192\\blue192;" + "}" + "{\\stylesheet");
        logwriter.newLine();
        logwriter.write("{\\ql\\fi0\\f0\\fs20\\i0\\b0\\cf1\\ul0\\strike0\\snext0 Standard;}");
        logwriter.newLine();
        logwriter.write("}{\\info {\\comment Generated by Jaguar v" + Jaguar.version + " RTF Logger }" + "{\\creatim\\yr" + yyyy + "\\mo" + mo + "\\dy" + dy + "\\hr" + hr + "\\min" + min + "\\sec" + sec + "}" + "{\\revtim\\yr1970\\mo1\\dy1\\hr1\\min0\\sec0}" + "{\\printim\\yr1970\\mo1\\dy1\\hr1\\min0\\sec0}}" + "\\paperw12240\\paperh15840\\margl800\\margr800\\margt720\\margb720" + "\\widowctrl\\ftnbj\\aenddoc\\formshade \\fet0\\sectd");
        logwriter.newLine();
        logwriter.write("\\pgnstart1\\pard\\plain\\pard\\plain\\s0\\ql\\fi0\\f0\\fs20\\i0\\b1\\cf1\\ul1\\strike0 {" + logname.replaceAll("\\\\", "\\\\\\\\") + "}");
        logwriter.newLine();
        loglines = 0;
    }

    public static String getLogName() {
        return logname;
    }

    public static void logClose() throws IOException {
        logwriter.write("}");
        logwriter.newLine();
        logwriter.flush();
        logwriter.close();
        log = false;
        logwriter = null;
    }

    public static void logLine(int cf, int cb, int f, String text, BufferedImage image, JaguarRectangle r) throws IOException {
        if (!log) return;
        ++loglines;
        if (loglines > logmaxlines && logmaxlines > 0) {
            logSwitch(cf, cb, f);
        }
        text = text.replaceAll("\\\\", "\\\\\\\\");
        if (text != null) {
            logwriter.write("\\par\\pard\\plain\\s0\\ql" + "\\fi0\\f" + f + "\\fs20\\i0\\b0" + "\\cf" + cf);
            if (cb != LOGWHITE) logwriter.write("\\cb" + cb);
            logwriter.write("\\ul0\\strike0 {" + text + "}");
            logwriter.newLine();
        }
        if (image != null) {
            logImage(image);
        }
        if (r != null) {
            image = r.getImage();
            if (image != null) {
                logImage(image);
            }
        }
    }

    public static void logSwitch(int cf, int cb, int f) throws IOException {
        logwriter.write("\\par\\pard\\plain\\s0\\ql" + "\\fi0\\f" + f + "\\fs20\\i0\\b0" + "\\cf" + cf);
        if (cb != LOGWHITE) logwriter.write("\\cb" + cb);
        logwriter.write("\\ul0\\strike0 {********** to be continued **********}");
        logwriter.newLine();
        logClose();
        log = true;
        logInit();
        logwriter.write("\\par\\pard\\plain\\s0\\ql" + "\\fi0\\f" + f + "\\fs20\\i0\\b0" + "\\cf" + cf);
        if (cb != LOGWHITE) logwriter.write("\\cb" + cb);
        logwriter.write("\\ul0\\strike0 {********** continued **********}");
    }

    /**
	 * @param image
	 * @throws IOException 
	 */
    private static void logImage(BufferedImage image) throws IOException {
        int w = image.getWidth();
        int h = image.getHeight();
        loglines += h + 1;
        if (h > 0 && w > 0) {
            int scale = 100, wgoal = 1000, hgoal;
            if (2 * w > wgoal) {
                scale = 1000;
                hgoal = (h * wgoal) / w;
            } else if (4 * w < wgoal) {
                wgoal = 4 * w;
                scale = 400;
                wgoal = (scale * w) / 100;
                hgoal = (scale * h) / 100;
            } else {
                scale = (100 * wgoal) / w;
                wgoal = (scale * w) / 100;
                hgoal = (scale * h) / 100;
            }
            logwriter.write("\\par\\pard\\plain\\par\\pard\\plain" + "\\s0\\ql\\fi0\\f0\\fs20\\i0\\b0\\cf1\\ul0\\strike0 {");
            logwriter.write("{\\pict\\jpegblip");
            logwriter.write("\\picscalex" + scale + "\\picscaley" + scale + "\\picw" + w + "\\pich" + h + "\\picwgoal" + wgoal + "\\pichgoal" + hgoal + " ");
            logwriter.newLine();
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            BufferedOutputStream output = new BufferedOutputStream(bo);
            if (isWindows) ImageIO.write(image, "JPG", output); else ImageIO.write(image, "jpeg", output);
            byte[] b = bo.toByteArray();
            int x = 0;
            for (int i = 0; i < b.length; i++) {
                int y = b[i];
                if (y < 0) y += 0x100;
                String s = Integer.toHexString(y);
                if (s.length() < 2) s = "0" + s;
                logwriter.write(s);
                ++x;
                if (x == 40) {
                    logwriter.newLine();
                    x = 0;
                }
            }
            logwriter.write("}}");
            logwriter.newLine();
        }
    }

    public static void disposeLog() {
        logwriter = null;
    }

    /**
	 * @return the logForegroundColor
	 */
    public static int getLogForegroundColor() {
        return logForegroundColor;
    }

    /**
	 * @param logForegroundColor the logForegroundColor to set
	 */
    public static void setLogForegroundColor(int logForegroundColor) {
        Jaguar.logForegroundColor = logForegroundColor;
    }

    public static boolean hasLogWritten() {
        return logwriter != null;
    }

    /**
	 * @return the logprefix
	 */
    public static String getLogprefix() {
        return logprefix;
    }

    /**
	 * @param logprefix the logprefix to set
	 */
    public static void setLogprefix(String logprefix) {
        Jaguar.logprefix = logprefix;
    }

    /**
	 * @param logmaxlines the logmaxlines to set
	 */
    public static void setLogmaxlines(int logmaxlines) {
        Jaguar.logmaxlines = logmaxlines;
    }
}
