import java.security.*;
import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import netscape.javascript.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.awt.datatransfer.*;
import javax.swing.JOptionPane;
import javax.swing.JDialog;

public final class DOHRobot extends Applet {

    private JSObject window = null;

    private Robot robot = null;

    private static Thread previousThread = null;

    private static HashMap charMap = null;

    private Vector vkKeys = null;

    private boolean shift = false;

    private boolean altgraph = false;

    private boolean ctrl = false;

    private boolean alt = false;

    private boolean meta = false;

    private boolean numlockDisabled = false;

    private boolean jsready = false;

    private String keystring = "";

    public boolean firebugIgnore = true;

    private static String os = System.getProperty("os.name").toUpperCase();

    private static Toolkit toolkit = Toolkit.getDefaultToolkit();

    private SecurityManager securitymanager;

    private double key = -1;

    private boolean inited = false;

    private int docScreenX = -100;

    private int docScreenY = -100;

    private int docScreenXMax;

    private int docScreenYMax;

    private boolean mouseSecurity = false;

    private int lastMouseX;

    private int lastMouseY;

    JSObject dohrobot = null;

    public void stop() {
        window = null;
        dohrobot = null;
        if (key != -2) {
            key = -2;
            AccessController.doPrivileged(new PrivilegedAction() {

                public Object run() {
                    log("Stop");
                    securitymanager.checkTopLevelWindow(null);
                    log("Security manager reset");
                    return null;
                }
            });
        }
    }

    private final class onvisible extends ComponentAdapter {

        public void componentShown(ComponentEvent evt) {
            if (key != -1) {
                return;
            }
            Thread thread = new Thread() {

                public void run() {
                    window = (JSObject) JSObject.getWindow(applet());
                    AccessController.doPrivileged(new PrivilegedAction() {

                        public Object run() {
                            log("> init Robot");
                            try {
                                SecurityManager oldsecurity = System.getSecurityManager();
                                boolean needsSecurityManager = applet().getParameter("needsSecurityManager").equals("true");
                                log("Socket connections managed? " + needsSecurityManager);
                                try {
                                    securitymanager = oldsecurity;
                                    securitymanager.checkTopLevelWindow(null);
                                    if (charMap == null) {
                                        if (!confirm("DOH has detected that the current Web page is attempting to access DOH,\n" + "but belongs to a different domain than the one you agreed to let DOH automate.\n" + "If you did not intend to start a new DOH test by visiting this Web page,\n" + "press Cancel now and leave the Web page.\n" + "Otherwise, press OK to trust this domain to automate DOH tests.")) {
                                            stop();
                                            return null;
                                        }
                                    }
                                    log("Found old security manager");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    log("Making new security manager");
                                    securitymanager = new RobotSecurityManager(needsSecurityManager, oldsecurity);
                                    securitymanager.checkTopLevelWindow(null);
                                    System.setSecurityManager(securitymanager);
                                }
                                robot = new Robot();
                                robot.setAutoWaitForIdle(true);
                            } catch (Exception e) {
                                log("Error calling _init_: " + e.getMessage());
                                key = -2;
                                e.printStackTrace();
                            }
                            log("< init Robot");
                            return null;
                        }
                    });
                    if (key == -2) {
                        window.eval("doh.robot._appletDead=true;doh.run();");
                    } else {
                        log("_initRobot");
                        try {
                            dohrobot = (JSObject) window.eval("doh.robot");
                            dohrobot.call("_initRobot", new Object[] { applet() });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.start();
        }
    }

    public void init() {
        addComponentListener(new onvisible());
    }

    public void _setKey(double key) {
        if (key == -1) {
            return;
        } else if (this.key == -1) {
            this.key = key;
        }
    }

    private boolean mouseSecure() throws Exception {
        if (!mouseSecurity) {
            return true;
        }
        Class mouseInfoClass;
        Class pointerInfoClass;
        try {
            mouseInfoClass = Class.forName("java.awt.MouseInfo");
            pointerInfoClass = Class.forName("java.awt.PointerInfo");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return true;
        }
        Method getPointerInfo = mouseInfoClass.getMethod("getPointerInfo", new Class[0]);
        Method getLocation = pointerInfoClass.getMethod("getLocation", new Class[0]);
        Object pointer = null;
        try {
            pointer = getPointerInfo.invoke(pointerInfoClass, new Object[0]);
        } catch (java.lang.reflect.InvocationTargetException e) {
            e.getTargetException().printStackTrace();
        }
        Point mousePosition = (Point) (getLocation.invoke(pointer, new Object[0]));
        return mousePosition.x >= docScreenX && mousePosition.x <= docScreenXMax && mousePosition.y >= docScreenY && mousePosition.y <= docScreenYMax;
    }

    private boolean isSecure(double key) {
        boolean result = this.key != -1 && this.key != -2 && this.key == key;
        try {
            result = result && mouseSecure();
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        if (!result && this.key != -2) {
            this.key = -2;
            window.eval("doh.robot._appletDead=true;");
            log("User aborted test; mouse moved off of browser");
            alert("User aborted test; mouse moved off of browser.");
        }
        log("Key secure: " + result);
        return result;
    }

    public void _callLoaded(final double sec) {
        log("> _callLoaded Robot");
        Thread thread = new Thread() {

            public void run() {
                if (!isSecure(sec)) {
                    return;
                }
                AccessController.doPrivileged(new PrivilegedAction() {

                    public Object run() {
                        Point p = getLocationOnScreen();
                        log("Document root: ~" + p.toString());
                        int x = p.x + 16;
                        int y = p.y + 8;
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }
                        ;
                        robot.mouseMove(x, y);
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }
                        ;
                        robot.mousePress(InputEvent.BUTTON1_MASK);
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }
                        robot.mouseRelease(InputEvent.BUTTON1_MASK);
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                        }
                        log("< _callLoaded Robot");
                        return null;
                    }
                });
            }
        };
        thread.start();
    }

    private DOHRobot applet() {
        return this;
    }

    public void log(final String s) {
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                System.out.println((new Date()).toString() + ": " + s);
                return null;
            }
        });
    }

    private void alert(final String s) {
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                window.eval("top.alert(\"" + s + "\");");
                return null;
            }
        });
    }

    private boolean confirm(final String s) {
        JOptionPane pane = new JOptionPane(s, JOptionPane.DEFAULT_OPTION, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = pane.createDialog(this, "doh.robot");
        dialog.setLocationRelativeTo(this);
        dialog.show();
        return ((Integer) pane.getValue()).intValue() == JOptionPane.OK_OPTION;
    }

    public void setDocumentBounds(final double sec, int x, int y, int w, int h) throws Exception {
        log("> setDocumentBounds");
        if (!isSecure(sec)) return;
        if (!inited) {
            inited = true;
            this.lastMouseX = this.docScreenX = x;
            this.lastMouseY = this.docScreenY = y;
            this.docScreenXMax = x + w;
            this.docScreenYMax = y + h;
            mouseSecurity = true;
        }
        log("< setDocumentBounds");
    }

    private void _mapKey(char charCode, int keyindex, boolean shift, boolean altgraph) {
        log("_mapKey: " + charCode);
        if (!charMap.containsKey(new Integer(charCode))) {
            log("Notified: " + (char) charCode);
            KeyEvent event = new KeyEvent(applet(), 0, 0, (shift ? KeyEvent.SHIFT_MASK : 0) + (altgraph ? KeyEvent.ALT_GRAPH_MASK : 0), ((Integer) vkKeys.get(keyindex)).intValue(), (char) charCode);
            charMap.put(new Integer(charCode), event);
            log("Mapped char " + (char) charCode + " to KeyEvent " + event);
            if (((char) charCode) >= 'a' && ((char) charCode) <= 'z') {
                int uppercharCode = (int) Character.toUpperCase((char) charCode);
                event = new KeyEvent(applet(), 0, 0, KeyEvent.SHIFT_MASK + (altgraph ? KeyEvent.ALT_GRAPH_MASK : 0), ((Integer) vkKeys.get(keyindex)).intValue(), (char) uppercharCode);
                charMap.put(new Integer(uppercharCode), event);
                log("Mapped char " + (char) uppercharCode + " to KeyEvent " + event);
            }
        }
    }

    public void _notified(final double sec, final String chars) {
        Thread thread = new Thread("_notified") {

            public void run() {
                if (!isSecure(sec)) return;
                AccessController.doPrivileged(new PrivilegedAction() {

                    public Object run() {
                        try {
                            if (previousThread != null) {
                                previousThread.join();
                            }
                        } catch (Exception e) {
                        }
                        keystring += chars;
                        if (altgraph && !shift) {
                            shift = false;
                            robot.setAutoDelay(1);
                            try {
                                log(keystring);
                                int index = 0;
                                for (int i = 0; (i < vkKeys.size()) && (index < keystring.length()); i++) {
                                    char c = keystring.charAt(index++);
                                    _mapKey(c, i, false, false);
                                }
                                for (int i = 0; (i < vkKeys.size()) && (index < keystring.length()); i++) {
                                    char c = keystring.charAt(index++);
                                    _mapKey(c, i, true, false);
                                }
                                for (int i = 0; (i < vkKeys.size()) && (index < keystring.length()); i++) {
                                    char c = keystring.charAt(index++);
                                    _mapKey(c, i, false, true);
                                }
                                dohrobot.call("_onKeyboard", new Object[] {});
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        } else if (!shift) {
                            shift = true;
                        } else {
                            shift = false;
                            altgraph = true;
                        }
                        pressNext();
                        return null;
                    }
                });
            }
        };
        thread.start();
    }

    private void pressNext() {
        final Thread myPreviousThread = previousThread;
        Thread thread = new Thread("pressNext") {

            public void run() {
                try {
                    if (myPreviousThread != null) {
                        myPreviousThread.join();
                    }
                } catch (Exception e) {
                }
                log("starting up, " + shift + " " + altgraph);
                if (shift) {
                    robot.keyPress(KeyEvent.VK_SHIFT);
                    log("Pressing shift");
                }
                try {
                    if (altgraph) {
                        robot.keyPress(KeyEvent.VK_ALT_GRAPH);
                        log("Pressing alt graph");
                    }
                } catch (Exception e) {
                    log("Error pressing alt graph");
                    e.printStackTrace();
                    _notified(key, "");
                    return;
                }
                dohrobot.call("_nextKeyGroup", new Object[] { new Integer(vkKeys.size()) });
                for (int keyindex = 0; keyindex < vkKeys.size(); keyindex++) {
                    try {
                        log("Press " + ((Integer) vkKeys.get(keyindex)).intValue());
                        robot.keyPress(((Integer) vkKeys.get(keyindex)).intValue());
                        log("Release " + ((Integer) vkKeys.get(keyindex)).intValue());
                        robot.keyRelease(((Integer) vkKeys.get(keyindex)).intValue());
                        if (altgraph && (keyindex == (vkKeys.size() - 1))) {
                            robot.keyRelease(KeyEvent.VK_ALT_GRAPH);
                            log("Releasing alt graph");
                        }
                        if (shift && (keyindex == (vkKeys.size() - 1))) {
                            robot.keyRelease(KeyEvent.VK_SHIFT);
                            log("Releasing shift");
                        }
                    } catch (Exception e) {
                    }
                    try {
                        log("Press space");
                        robot.keyPress(KeyEvent.VK_SPACE);
                        log("Release space");
                        robot.keyRelease(KeyEvent.VK_SPACE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        previousThread = thread;
        thread.start();
    }

    public void _initWheel(final double sec) {
        log("> initWheel");
        Thread thread = new Thread() {

            public void run() {
                if (!isSecure(sec)) return;
                Thread.yield();
                int dir = 1;
                if (os.indexOf("MAC") != -1) {
                    dir = -1;
                }
                robot.mouseWheel(dir);
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
                log("< initWheel");
            }
        };
        thread.start();
    }

    public void _initKeyboard(final double sec) {
        log("> initKeyboard");
        if (charMap != null) {
            dohrobot.call("_onKeyboard", new Object[] {});
            return;
        }
        Thread thread = new Thread() {

            public void run() {
                if (!isSecure(sec)) return;
                AccessController.doPrivileged(new PrivilegedAction() {

                    public Object run() {
                        charMap = new HashMap();
                        KeyEvent event = new KeyEvent(applet(), 0, 0, 0, KeyEvent.VK_SPACE, ' ');
                        charMap.put(new Integer(32), event);
                        try {
                            vkKeys = new Vector();
                            for (char i = 'a'; i <= 'z'; i++) {
                                vkKeys.add(new Integer(KeyEvent.class.getField("VK_" + Character.toUpperCase((char) i)).getInt(null)));
                            }
                            for (char i = '0'; i <= '9'; i++) {
                                vkKeys.add(new Integer(KeyEvent.class.getField("VK_" + Character.toUpperCase((char) i)).getInt(null)));
                            }
                            int[] mykeys = new int[] { KeyEvent.VK_COMMA, KeyEvent.VK_MINUS, KeyEvent.VK_PERIOD, KeyEvent.VK_SLASH, KeyEvent.VK_SEMICOLON, KeyEvent.VK_LEFT_PARENTHESIS, KeyEvent.VK_NUMBER_SIGN, KeyEvent.VK_PLUS, KeyEvent.VK_RIGHT_PARENTHESIS, KeyEvent.VK_UNDERSCORE, KeyEvent.VK_EXCLAMATION_MARK, KeyEvent.VK_DOLLAR, KeyEvent.VK_CIRCUMFLEX, KeyEvent.VK_AMPERSAND, KeyEvent.VK_ASTERISK, KeyEvent.VK_QUOTEDBL, KeyEvent.VK_LESS, KeyEvent.VK_GREATER, KeyEvent.VK_BRACELEFT, KeyEvent.VK_BRACERIGHT, KeyEvent.VK_COLON, KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_QUOTE, KeyEvent.VK_OPEN_BRACKET, KeyEvent.VK_BACK_SLASH, KeyEvent.VK_CLOSE_BRACKET, KeyEvent.VK_EQUALS };
                            for (int i = 0; i < mykeys.length; i++) {
                                vkKeys.add(new Integer(mykeys[i]));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        robot.setAutoDelay(1);
                        int count = 0;
                        boolean waitingOnSpace = true;
                        do {
                            log("Pressed space");
                            robot.keyPress(KeyEvent.VK_SPACE);
                            robot.keyRelease(KeyEvent.VK_SPACE);
                            count++;
                            waitingOnSpace = ((Boolean) window.eval("doh.robot._spaceReceived")).equals(Boolean.FALSE);
                            log("JS still waiting on a space? " + waitingOnSpace);
                        } while (count < 500 && waitingOnSpace);
                        robot.keyPress(KeyEvent.VK_ENTER);
                        robot.keyRelease(KeyEvent.VK_ENTER);
                        robot.setAutoDelay(0);
                        log("< initKeyboard");
                        pressNext();
                        return null;
                    }
                });
            }
        };
        thread.start();
    }

    public void typeKey(double sec, final int charCode, final int keyCode, final boolean alt, final boolean ctrl, final boolean shift, final boolean meta, final int delay, final boolean async) {
        if (!isSecure(sec)) return;
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                try {
                    log("> typeKey Robot " + charCode + ", " + keyCode + ", " + async);
                    KeyPressThread thread = new KeyPressThread(charCode, keyCode, alt, ctrl, shift, meta, delay, async ? null : previousThread);
                    previousThread = async ? previousThread : thread;
                    thread.start();
                    log("< typeKey Robot");
                } catch (Exception e) {
                    log("Error calling typeKey");
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    public void upKey(double sec, final int charCode, final int keyCode, final int delay) {
        if (!isSecure(sec)) return;
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                log("> upKey Robot " + charCode + ", " + keyCode);
                KeyUpThread thread = new KeyUpThread(charCode, keyCode, delay, previousThread);
                previousThread = thread;
                thread.start();
                log("< upKey Robot");
                return null;
            }
        });
    }

    public void downKey(double sec, final int charCode, final int keyCode, final int delay) {
        if (!isSecure(sec)) return;
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                log("> downKey Robot " + charCode + ", " + keyCode);
                KeyDownThread thread = new KeyDownThread(charCode, keyCode, delay, previousThread);
                previousThread = thread;
                thread.start();
                log("< downKey Robot");
                return null;
            }
        });
    }

    public void pressMouse(double sec, final boolean left, final boolean middle, final boolean right, final int delay) {
        if (!isSecure(sec)) return;
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                log("> mousePress Robot " + left + ", " + middle + ", " + right);
                MousePressThread thread = new MousePressThread((left ? InputEvent.BUTTON1_MASK : 0) + (middle ? InputEvent.BUTTON2_MASK : 0) + (right ? InputEvent.BUTTON3_MASK : 0), delay, previousThread);
                previousThread = thread;
                thread.start();
                log("< mousePress Robot");
                return null;
            }
        });
    }

    public void releaseMouse(double sec, final boolean left, final boolean middle, final boolean right, final int delay) {
        if (!isSecure(sec)) return;
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                log("> mouseRelease Robot " + left + ", " + middle + ", " + right);
                MouseReleaseThread thread = new MouseReleaseThread((left ? InputEvent.BUTTON1_MASK : 0) + (middle ? InputEvent.BUTTON2_MASK : 0) + (right ? InputEvent.BUTTON3_MASK : 0), delay, previousThread);
                previousThread = thread;
                thread.start();
                log("< mouseRelease Robot");
                return null;
            }
        });
    }

    public void moveMouse(double sec, final int x1, final int y1, final int d, final int duration) {
        if (!isSecure(sec)) return;
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                int x = x1 + docScreenX;
                int y = y1 + docScreenY;
                if (x > docScreenXMax || y > docScreenYMax) {
                    log("Request to mouseMove denied");
                    return null;
                }
                int delay = d;
                log("> mouseMove Robot " + x + ", " + y);
                MouseMoveThread thread = new MouseMoveThread(x, y, delay, duration, previousThread);
                previousThread = thread;
                thread.start();
                log("< mouseMove Robot");
                return null;
            }
        });
    }

    public void wheelMouse(double sec, final int amount, final int delay, final int duration) {
        if (!isSecure(sec)) return;
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                MouseWheelThread thread = new MouseWheelThread(amount, delay, duration, previousThread);
                previousThread = thread;
                thread.start();
                return null;
            }
        });
    }

    private int getVKCode(int charCode, int keyCode) {
        int keyboardCode = 0;
        if (charCode >= 32) {
            KeyEvent event = (KeyEvent) charMap.get(new Integer(charCode));
            keyboardCode = event.getKeyCode();
        } else {
            switch(keyCode) {
                case 13:
                    keyboardCode = KeyEvent.VK_ENTER;
                    break;
                case 8:
                    keyboardCode = KeyEvent.VK_BACK_SPACE;
                    break;
                case 25:
                case 9:
                    keyboardCode = KeyEvent.VK_TAB;
                    break;
                case 12:
                    keyboardCode = KeyEvent.VK_CLEAR;
                    break;
                case 16:
                    keyboardCode = KeyEvent.VK_SHIFT;
                    break;
                case 17:
                    keyboardCode = KeyEvent.VK_CONTROL;
                    break;
                case 18:
                    keyboardCode = KeyEvent.VK_ALT;
                    break;
                case 63250:
                case 19:
                    keyboardCode = KeyEvent.VK_PAUSE;
                    break;
                case 20:
                    keyboardCode = KeyEvent.VK_CAPS_LOCK;
                    break;
                case 27:
                    keyboardCode = KeyEvent.VK_ESCAPE;
                    break;
                case 32:
                    log("it's a space");
                    keyboardCode = KeyEvent.VK_SPACE;
                    break;
                case 63276:
                case 33:
                    keyboardCode = KeyEvent.VK_PAGE_UP;
                    break;
                case 63277:
                case 34:
                    keyboardCode = KeyEvent.VK_PAGE_DOWN;
                    break;
                case 63275:
                case 35:
                    keyboardCode = KeyEvent.VK_END;
                    break;
                case 63273:
                case 36:
                    keyboardCode = KeyEvent.VK_HOME;
                    break;
                case 63234:
                case 37:
                    keyboardCode = KeyEvent.VK_LEFT;
                    break;
                case 63232:
                case 38:
                    keyboardCode = KeyEvent.VK_UP;
                    break;
                case 63235:
                case 39:
                    keyboardCode = KeyEvent.VK_RIGHT;
                    break;
                case 63233:
                case 40:
                    keyboardCode = KeyEvent.VK_DOWN;
                    break;
                case 63272:
                case 46:
                    keyboardCode = KeyEvent.VK_DELETE;
                    break;
                case 63289:
                case 144:
                    keyboardCode = KeyEvent.VK_NUM_LOCK;
                    break;
                case 63249:
                case 145:
                    keyboardCode = KeyEvent.VK_SCROLL_LOCK;
                    break;
                case 63236:
                case 112:
                    keyboardCode = KeyEvent.VK_F1;
                    break;
                case 63237:
                case 113:
                    keyboardCode = KeyEvent.VK_F2;
                    break;
                case 63238:
                case 114:
                    keyboardCode = KeyEvent.VK_F3;
                    break;
                case 63239:
                case 115:
                    keyboardCode = KeyEvent.VK_F4;
                    break;
                case 63240:
                case 116:
                    keyboardCode = KeyEvent.VK_F5;
                    break;
                case 63241:
                case 117:
                    keyboardCode = KeyEvent.VK_F6;
                    break;
                case 63242:
                case 118:
                    keyboardCode = KeyEvent.VK_F7;
                    break;
                case 63243:
                case 119:
                    keyboardCode = KeyEvent.VK_F8;
                    break;
                case 63244:
                case 120:
                    keyboardCode = KeyEvent.VK_F9;
                    break;
                case 63245:
                case 121:
                    keyboardCode = KeyEvent.VK_F10;
                    break;
                case 63246:
                case 122:
                    keyboardCode = KeyEvent.VK_F11;
                    break;
                case 63247:
                case 123:
                    keyboardCode = KeyEvent.VK_F12;
                    break;
                case 124:
                    keyboardCode = KeyEvent.VK_F13;
                    break;
                case 125:
                    keyboardCode = KeyEvent.VK_F14;
                    break;
                case 126:
                    keyboardCode = KeyEvent.VK_F15;
                    break;
                case 63302:
                case 45:
                    keyboardCode = KeyEvent.VK_INSERT;
                    break;
                case 47:
                    keyboardCode = KeyEvent.VK_HELP;
                    break;
                default:
                    keyboardCode = keyCode;
            }
        }
        log("Attempting to type " + (char) charCode + ":" + charCode + " " + keyCode);
        log("Converted to " + keyboardCode);
        return keyboardCode;
    }

    private boolean isUnsafe(int keyboardCode) {
        log("ctrl: " + ctrl + ", alt: " + alt + ", shift: " + shift);
        if (((ctrl || alt) && keyboardCode == KeyEvent.VK_ESCAPE) || (alt && keyboardCode == KeyEvent.VK_TAB) || (ctrl && alt && keyboardCode == KeyEvent.VK_DELETE)) {
            log("You are not allowed to press this key combination!");
            return true;
        } else {
            log("Safe to press.");
            return false;
        }
    }

    private boolean disableNumlock(int vk, boolean shift) {
        boolean result = !numlockDisabled && shift && os.indexOf("WINDOWS") != -1 && toolkit.getLockingKeyState(KeyEvent.VK_NUM_LOCK) && (vk == KeyEvent.VK_LEFT || vk == KeyEvent.VK_UP || vk == KeyEvent.VK_RIGHT || vk == KeyEvent.VK_DOWN || vk == KeyEvent.VK_HOME || vk == KeyEvent.VK_END || vk == KeyEvent.VK_PAGE_UP || vk == KeyEvent.VK_PAGE_DOWN);
        log("disable numlock: " + result);
        return result;
    }

    private void _typeKey(final int cCode, final int kCode, final boolean a, final boolean c, final boolean s, final boolean m) {
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                int charCode = cCode;
                int keyCode = kCode;
                boolean alt = a;
                boolean ctrl = c;
                boolean shift = s;
                boolean meta = m;
                boolean altgraph = false;
                log("> _typeKey Robot " + charCode + ", " + keyCode);
                try {
                    int keyboardCode = getVKCode(charCode, keyCode);
                    if (charCode >= 32) {
                        KeyEvent event = (KeyEvent) charMap.get(new Integer(charCode));
                        if (!shift) {
                            shift = event.isShiftDown();
                        }
                        altgraph = event.isAltGraphDown();
                        keyboardCode = event.getKeyCode();
                    }
                    boolean disableNumlock = disableNumlock(keyboardCode, shift || applet().shift);
                    if (!isUnsafe(keyboardCode)) {
                        if (shift) {
                            log("Pressing shift");
                            robot.keyPress(KeyEvent.VK_SHIFT);
                        }
                        if (alt) {
                            log("Pressing alt");
                            robot.keyPress(KeyEvent.VK_ALT);
                        }
                        if (altgraph) {
                            log("Pressing altgraph");
                            robot.keyPress(KeyEvent.VK_ALT_GRAPH);
                        }
                        if (ctrl) {
                            log("Pressing ctrl");
                            robot.keyPress(KeyEvent.VK_CONTROL);
                        }
                        if (meta) {
                            log("Pressing meta");
                            robot.keyPress(KeyEvent.VK_META);
                        }
                        if (disableNumlock) {
                            robot.keyPress(KeyEvent.VK_NUM_LOCK);
                            robot.keyRelease(KeyEvent.VK_NUM_LOCK);
                            numlockDisabled = true;
                        } else if (numlockDisabled && !(applet().shift || shift)) {
                            robot.keyPress(KeyEvent.VK_NUM_LOCK);
                            robot.keyRelease(KeyEvent.VK_NUM_LOCK);
                            numlockDisabled = false;
                        }
                        if (keyboardCode != KeyEvent.VK_SHIFT && keyboardCode != KeyEvent.VK_ALT && keyboardCode != KeyEvent.VK_ALT_GRAPH && keyboardCode != KeyEvent.VK_CONTROL && keyboardCode != KeyEvent.VK_META) {
                            try {
                                robot.keyPress(keyboardCode);
                                robot.keyRelease(keyboardCode);
                            } catch (Exception e) {
                                log("Error while actually typing a key");
                                e.printStackTrace();
                            }
                        }
                        if (ctrl) {
                            robot.keyRelease(KeyEvent.VK_CONTROL);
                            ctrl = false;
                        }
                        if (alt) {
                            robot.keyRelease(KeyEvent.VK_ALT);
                            alt = false;
                        }
                        if (altgraph) {
                            robot.keyRelease(KeyEvent.VK_ALT_GRAPH);
                            altgraph = false;
                        }
                        if (shift) {
                            log("Releasing shift");
                            robot.keyRelease(KeyEvent.VK_SHIFT);
                            shift = false;
                        }
                        if (meta) {
                            log("Releasing meta");
                            robot.keyRelease(KeyEvent.VK_META);
                            meta = false;
                        }
                    }
                } catch (Exception e) {
                    log("Error in _typeKey");
                    e.printStackTrace();
                }
                log("< _typeKey Robot");
                return null;
            }
        });
    }

    public boolean hasFocus() {
        try {
            return ((Boolean) window.eval("var result=false;if(window.parent.document.hasFocus){result=window.parent.document.hasFocus();}else{result=true;}result;")).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    private final class KeyPressThread extends Thread {

        private int charCode;

        private int keyCode;

        private boolean alt;

        private boolean ctrl;

        private boolean shift;

        private boolean meta;

        private int delay;

        private Thread myPreviousThread = null;

        public KeyPressThread(int charCode, int keyCode, boolean alt, boolean ctrl, boolean shift, boolean meta, int delay, Thread myPreviousThread) {
            log("KeyPressThread constructor " + charCode + ", " + keyCode);
            this.charCode = charCode;
            this.keyCode = keyCode;
            this.alt = alt;
            this.ctrl = ctrl;
            this.shift = shift;
            this.meta = meta;
            this.delay = delay;
            this.myPreviousThread = myPreviousThread;
        }

        public void run() {
            try {
                if (myPreviousThread != null) myPreviousThread.join();
                while (!hasFocus()) {
                    Thread.sleep(1000);
                }
                Thread.sleep(delay);
                log("> run KeyPressThread");
                _typeKey(charCode, keyCode, alt, ctrl, shift, meta);
            } catch (Exception e) {
                log("Bad parameters passed to _typeKey");
                e.printStackTrace();
            }
            log("< run KeyPressThread");
        }
    }

    private final class KeyDownThread extends Thread {

        private int charCode;

        private int keyCode;

        private int delay;

        private Thread myPreviousThread = null;

        public KeyDownThread(int charCode, int keyCode, int delay, Thread myPreviousThread) {
            log("KeyDownThread constructor " + charCode + ", " + keyCode);
            this.charCode = charCode;
            this.keyCode = keyCode;
            this.delay = delay;
            this.myPreviousThread = myPreviousThread;
        }

        public void run() {
            try {
                if (myPreviousThread != null) myPreviousThread.join();
                Thread.sleep(delay);
                log("> run KeyDownThread");
                while (!hasFocus()) {
                    Thread.sleep(1000);
                }
                int vkCode = getVKCode(charCode, keyCode);
                if (charCode >= 32) {
                    KeyEvent event = (KeyEvent) charMap.get(new Integer(charCode));
                    if (event.isShiftDown()) {
                        robot.keyPress(KeyEvent.VK_SHIFT);
                        shift = true;
                    }
                    if (event.isAltGraphDown()) {
                        robot.keyPress(KeyEvent.VK_ALT_GRAPH);
                        altgraph = true;
                    }
                } else {
                    if (vkCode == KeyEvent.VK_ALT) {
                        alt = true;
                    } else if (vkCode == KeyEvent.VK_CONTROL) {
                        ctrl = true;
                    } else if (vkCode == KeyEvent.VK_SHIFT) {
                        shift = true;
                    } else if (vkCode == KeyEvent.VK_ALT_GRAPH) {
                        altgraph = true;
                    } else if (vkCode == KeyEvent.VK_META) {
                        meta = true;
                    } else if (disableNumlock(vkCode, shift)) {
                        robot.keyPress(KeyEvent.VK_NUM_LOCK);
                        robot.keyRelease(KeyEvent.VK_NUM_LOCK);
                        numlockDisabled = true;
                    }
                }
                if (!isUnsafe(vkCode)) {
                    robot.keyPress(vkCode);
                }
            } catch (Exception e) {
                log("Bad parameters passed to downKey");
                e.printStackTrace();
            }
            log("< run KeyDownThread");
        }
    }

    private final class KeyUpThread extends Thread {

        private int charCode;

        private int keyCode;

        private int delay;

        private Thread myPreviousThread = null;

        public KeyUpThread(int charCode, int keyCode, int delay, Thread myPreviousThread) {
            log("KeyUpThread constructor " + charCode + ", " + keyCode);
            this.charCode = charCode;
            this.keyCode = keyCode;
            this.delay = delay;
            this.myPreviousThread = myPreviousThread;
        }

        public void run() {
            try {
                if (myPreviousThread != null) myPreviousThread.join();
                Thread.sleep(delay);
                log("> run KeyUpThread");
                while (!hasFocus()) {
                    Thread.sleep(1000);
                }
                int vkCode = getVKCode(charCode, keyCode);
                if (charCode >= 32) {
                    KeyEvent event = (KeyEvent) charMap.get(new Integer(charCode));
                    if (event.isShiftDown()) {
                        robot.keyRelease(KeyEvent.VK_SHIFT);
                        shift = false;
                    }
                    if (event.isAltGraphDown()) {
                        robot.keyRelease(KeyEvent.VK_ALT_GRAPH);
                        altgraph = false;
                    }
                } else {
                    if (vkCode == KeyEvent.VK_ALT) {
                        alt = false;
                    } else if (vkCode == KeyEvent.VK_CONTROL) {
                        ctrl = false;
                    } else if (vkCode == KeyEvent.VK_SHIFT) {
                        shift = false;
                        if (numlockDisabled) {
                            robot.keyPress(KeyEvent.VK_NUM_LOCK);
                            robot.keyRelease(KeyEvent.VK_NUM_LOCK);
                            numlockDisabled = false;
                        }
                    } else if (vkCode == KeyEvent.VK_ALT_GRAPH) {
                        altgraph = false;
                    } else if (vkCode == KeyEvent.VK_META) {
                        meta = false;
                    }
                }
                robot.keyRelease(vkCode);
            } catch (Exception e) {
                log("Bad parameters passed to upKey");
                e.printStackTrace();
            }
            log("< run KeyUpThread");
        }
    }

    private final class MousePressThread extends Thread {

        private int mask;

        private int delay;

        private Thread myPreviousThread = null;

        public MousePressThread(int mask, int delay, Thread myPreviousThread) {
            this.mask = mask;
            this.delay = delay;
            this.myPreviousThread = myPreviousThread;
        }

        public void run() {
            try {
                if (myPreviousThread != null) myPreviousThread.join();
                Thread.sleep(delay);
                log("> run MousePressThread");
                while (!hasFocus()) {
                    Thread.sleep(1000);
                }
                robot.mousePress(mask);
                robot.waitForIdle();
            } catch (Exception e) {
                log("Bad parameters passed to mousePress");
                e.printStackTrace();
            }
            log("< run MousePressThread");
        }
    }

    private final class MouseReleaseThread extends Thread {

        private int mask;

        private int delay;

        private Thread myPreviousThread = null;

        public MouseReleaseThread(int mask, int delay, Thread myPreviousThread) {
            this.mask = mask;
            this.delay = delay;
            this.myPreviousThread = myPreviousThread;
        }

        public void run() {
            try {
                if (myPreviousThread != null) myPreviousThread.join();
                Thread.sleep(delay);
                log("> run MouseReleaseThread ");
                while (!hasFocus()) {
                    Thread.sleep(1000);
                }
                robot.mouseRelease(mask);
                robot.waitForIdle();
            } catch (Exception e) {
                log("Bad parameters passed to mouseRelease");
                e.printStackTrace();
            }
            log("< run MouseReleaseThread ");
        }
    }

    private final class MouseMoveThread extends Thread {

        private int x;

        private int y;

        private int delay;

        private int duration;

        private Thread myPreviousThread = null;

        public MouseMoveThread(int x, int y, int delay, int duration, Thread myPreviousThread) {
            this.x = x;
            this.y = y;
            this.delay = delay;
            this.duration = duration;
            this.myPreviousThread = myPreviousThread;
        }

        public double easeInOutQuad(double t, double b, double c, double d) {
            t /= d / 2;
            if (t < 1) return c / 2 * t * t + b;
            t--;
            return -c / 2 * (t * (t - 2) - 1) + b;
        }

        ;

        public void run() {
            try {
                if (myPreviousThread != null) myPreviousThread.join();
                Thread.sleep(delay);
                log("> run MouseMoveThread " + x + ", " + y);
                while (!hasFocus()) {
                    Thread.sleep(1000);
                }
                int x1 = lastMouseX;
                int x2 = x;
                int y1 = lastMouseY;
                int y2 = y;
                if (x1 != x2) {
                    int dx = x - lastMouseX;
                    if (dx > 0) {
                        x1 += 1;
                        x2 -= 1;
                    } else {
                        x1 -= 1;
                        x2 += 1;
                    }
                }
                if (y1 != y2) {
                    int dy = y - lastMouseY;
                    if (dy > 0) {
                        y1 += 1;
                        y2 -= 1;
                    } else {
                        y1 -= 1;
                        y2 += 1;
                    }
                }
                robot.setAutoWaitForIdle(false);
                int intermediateSteps = duration == 1 ? 0 : ((((int) Math.ceil(Math.log(duration + 1))) | 1));
                int delay = duration / (intermediateSteps + 1);
                robot.mouseMove(lastMouseX, lastMouseY);
                long date, date2;
                date = new Date().getTime();
                lastMouseX = x1;
                lastMouseY = y1;
                for (int t = 0; t < intermediateSteps; t++) {
                    Thread.sleep(delay);
                    x1 = (int) easeInOutQuad((double) t, (double) lastMouseX, (double) x2 - lastMouseX, (double) intermediateSteps - 1);
                    y1 = (int) easeInOutQuad((double) t, (double) lastMouseY, (double) y2 - lastMouseY, (double) intermediateSteps - 1);
                    robot.mouseMove(x1, y1);
                }
                Thread.sleep(delay);
                robot.mouseMove(x, y);
                robot.setAutoWaitForIdle(true);
                date2 = new Date().getTime();
                lastMouseX = x;
                lastMouseY = y;
            } catch (Exception e) {
                log("Bad parameters passed to mouseMove");
                e.printStackTrace();
            }
            log("< run MouseMoveThread");
        }
    }

    private final class MouseWheelThread extends Thread {

        private int amount;

        private int delay;

        private int duration;

        private Thread myPreviousThread = null;

        public MouseWheelThread(int amount, int delay, int duration, Thread myPreviousThread) {
            this.amount = amount;
            this.delay = delay;
            this.duration = duration;
            this.myPreviousThread = myPreviousThread;
        }

        public void run() {
            try {
                if (myPreviousThread != null) myPreviousThread.join();
                Thread.sleep(delay);
                log("> run MouseWheelThread " + amount);
                while (!hasFocus()) {
                    Thread.sleep(1000);
                }
                int dir = 1;
                if (os.indexOf("MAC") != -1) {
                    dir = -1;
                }
                robot.setAutoDelay(Math.max(duration / Math.abs(amount), 1));
                for (int i = 0; i < Math.abs(amount); i++) {
                    robot.mouseWheel(amount > 0 ? dir : -dir);
                }
                robot.setAutoDelay(1);
            } catch (Exception e) {
                log("Bad parameters passed to mouseWheel");
                e.printStackTrace();
            }
            log("< run MouseWheelThread ");
        }
    }

    private final class RobotSecurityManager extends SecurityManager {

        private boolean isActive = false;

        private boolean needsSecurityManager = false;

        private SecurityManager oldsecurity = null;

        public RobotSecurityManager(boolean needsSecurityManager, SecurityManager oldsecurity) {
            this.needsSecurityManager = needsSecurityManager;
            this.oldsecurity = oldsecurity;
        }

        public boolean checkTopLevelWindow(Object window) {
            if (window == null) {
                isActive = !isActive;
                log("Active is now " + isActive);
            }
            return window == null ? true : oldsecurity.checkTopLevelWindow(window);
        }

        public void checkPermission(Permission p) {
            if (isActive && needsSecurityManager && java.net.SocketPermission.class.isInstance(p) && p.getActions().matches(".*resolve.*")) {
                throw new SecurityException("DOH: liveconnect resolve locks up Safari 3. Denying resolve request.");
            } else if (p.equals(new java.awt.AWTPermission("watchMousePointer"))) {
            } else {
                oldsecurity.checkPermission(p);
            }
        }

        public void checkPermission(Permission perm, Object context) {
            checkPermission(perm);
        }
    }

    public void setClipboardText(double sec, final String data) {
        if (!isSecure(sec)) return;
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                StringSelection ss = new StringSelection(data);
                getSystemClipboard().setContents(ss, ss);
                return null;
            }
        });
    }

    public void setClipboardHtml(double sec, final String data) {
        if (!isSecure(sec)) return;
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                String mimeType = "text/html;class=java.lang.String";
                TextTransferable transferable = new TextTransferable(mimeType, data);
                getSystemClipboard().setContents(transferable, transferable);
                return null;
            }
        });
    }

    private static java.awt.datatransfer.Clipboard getSystemClipboard() {
        return toolkit.getSystemClipboard();
    }

    private static class TextTransferable implements Transferable, ClipboardOwner {

        private String data;

        private static ArrayList htmlFlavors = new ArrayList();

        static {
            try {
                htmlFlavors.add(new DataFlavor("text/plain;charset=UTF-8;class=java.lang.String"));
                htmlFlavors.add(new DataFlavor("text/html;charset=UTF-8;class=java.lang.String"));
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        public TextTransferable(String mimeType, String data) {
            this.data = data;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return (DataFlavor[]) htmlFlavors.toArray(new DataFlavor[htmlFlavors.size()]);
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return htmlFlavors.contains(flavor);
        }

        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (String.class.equals(flavor.getRepresentationClass())) {
                return data;
            }
            throw new UnsupportedFlavorException(flavor);
        }

        public void lostOwnership(java.awt.datatransfer.Clipboard clipboard, Transferable contents) {
            data = null;
        }
    }
}
