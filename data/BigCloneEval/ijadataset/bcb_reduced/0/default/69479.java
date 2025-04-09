import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

/** A server that will dispatch to Handlers. */
abstract class LocalServer implements Server.Debuggable {

    private DebugPanel debug = new DebugPanel();

    /** Maps handlers to handler panels. */
    private final Map<Handler, HandlerPanel> handlers2panels = new HashMap<Handler, HandlerPanel>();

    /** Override to return all the handlers. */
    abstract Handler[] getHandlers();

    public void note(Date d, Object msg, Color c) {
        debug.note(msg, c);
    }

    public void note(Handler handler) {
        HandlerPanel p = handlers2panels.get(handler);
        if (p != null) p.on();
    }

    protected final void warn(String msg) {
        System.err.println("WARN: " + msg);
    }

    /** Displays a URL and handles any errors. */
    protected final void displayURL(String url) {
        try {
            Browser.displayURL(url);
        } catch (IOException e) {
            handle(e);
        }
    }

    /** Demands an argument, and prints a message if not there. */
    protected final String demand(Map<String, String> args, String argName) {
        String arg = args.get(argName);
        if (arg == null || arg.equals("")) {
            throw new IllegalArgumentException("Required argument '" + argName + "'");
        }
        return arg;
    }

    protected final int getInt(Map<String, String> args, String argName) {
        String arg = demand(args, argName);
        return Integer.parseInt(arg);
    }

    /** Prints a little message */
    protected final void note(Object msg) {
        debug.note(msg, Constants.COLOR_DEBUG);
    }

    /** Handles an exception with something less than grace */
    protected final void handle(Throwable t) {
        t.printStackTrace();
    }

    /** Override this if you want to show the IP. */
    protected boolean showIP() {
        return false;
    }

    /** Override this to turn OFF debugging. */
    protected boolean dontDebug() {
        return false;
    }

    /** Override this to NOT show the 'Quit' frame. */
    protected boolean dontShowQuitFrame() {
        return false;
    }

    /** For testing. */
    final Server go(String args[]) {
        List<String> argList = new ArrayList<String>();
        for (String arg : args) argList.add(arg);
        processArgs(argList);
        final Server s = new Server(this);
        addHandlers(s);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                beforeWeStop();
                s.stop();
            }
        }));
        return s;
    }

    /** Main entry point. */
    public final void realMain(String args[]) {
        boolean menu = false;
        boolean debug = true;
        if (menu) new Menu().show(this);
        if (showIP()) showTheIP();
        final Server s = go(args);
        if (dontDebug()) debug = false;
        if (debug) {
            showDebugFrame(s);
            showHandlerFrame(s);
        } else if (!dontShowQuitFrame()) {
            showQuitFrame(s);
        }
        beforeWeStart(args);
        try {
            s.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
   * Called before we start doing things, subclasses can implement it
   * and don't need to call <code>super</code>.
   *
   * @param args command line args
   */
    protected void beforeWeStart(String[] args) {
    }

    /**
   * Called before we exit, subclasses can implement it and don't need
   * to call <code>super</code>.
   */
    protected void beforeWeStop() {
    }

    protected void processArgs(Iterable<String> args) {
    }

    public final String toString() {
        return getClass().getSimpleName();
    }

    /**
   * Formats properties as '|' delimited list of name,value pairs.
   */
    protected final String formatProps(Properties ps) {
        StringBuffer res = new StringBuffer();
        int max = -1;
        List<String> keys = new ArrayList<String>();
        for (Enumeration en = ps.propertyNames(); en.hasMoreElements(); ) {
            String key = (String) en.nextElement();
            keys.add(key);
            max = Math.max(key.length(), max);
        }
        Collections.sort(keys);
        for (String key : keys) {
            res.append(key);
            for (int i = 0, N = max - key.length(); i < N; i++) res.append(" ");
            res.append(" : ").append(ps.getProperty(key)).append("|");
        }
        return res.toString().replaceAll("\n", "").replaceAll("\r", "");
    }

    protected final String getArg(Map<String, String> args, String name) {
        String val = args.get(name);
        if (val == null) {
            throw new IllegalArgumentException("Required parameter " + name);
        }
        return val;
    }

    /**
   * Converts someMethodName    -> Some Method Name
   * Converts getSomeMethodName -> Some Method Name
   */
    protected final String convertCamelCase(String s) {
        String[] starts = { "is", "get" };
        for (String start : starts) {
            if (s.startsWith(start)) s = s.substring(start.length());
        }
        StringBuffer buf = new StringBuffer();
        buf.append(Character.toUpperCase(s.charAt(0)));
        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) buf.append(' ');
            buf.append(c);
        }
        return buf.toString();
    }

    /**
   * Returns a '|' delimited String of invoking the command 'cmd'.
   */
    protected final String invoke(String[] cmd) {
        String res = null;
        try {
            res = invokeInternal(cmd);
        } catch (Exception e) {
            final StringBuffer sb = new StringBuffer();
            e.printStackTrace(new PrintStream(new OutputStream() {

                public void write(int b) throws IOException {
                    char c = (char) b;
                    if (c == '\r') return;
                    if (c == '\n') c = '|';
                    sb.append(c);
                }
            }));
            res = sb.toString();
        }
        return res;
    }

    /** Handler for displaying URLs. */
    protected abstract class AbstractURLHandler extends AbstractHandler {

        private final String url;

        AbstractURLHandler(String url) {
            this.url = url;
        }

        public final String handle(Map<String, String> args) {
            displayURL(url);
            return OK;
        }
    }

    /**
   * Adds the fields of 'f' to 'props'.
   */
    protected final void addProps(Field f, Properties props) throws Exception {
        f.setAccessible(true);
        String name = f.getDeclaringClass().getSimpleName();
        Object vm = f.get(null);
        Method[] ms = vm.getClass().getMethods();
        for (Method m : ms) {
            m.setAccessible(true);
            if (Modifier.isAbstract(m.getModifiers())) continue;
            if (m.getParameterTypes().length > 0) continue;
            if (m.getReturnType().equals(void.class)) continue;
            props.put(convertCamelCase(m.getName()) + " [" + name + "]", m.invoke(vm, new Object[0]));
        }
    }

    /**
   * CGI decodes the string.
   *
   * @param s string to decode
   * @return CGI-decoded version of <code>s</code>
   */
    protected final String cgiDecode(String s) {
        try {
            s = URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            s = s.replace("%20", " ");
            e.printStackTrace();
        }
        return s;
    }

    /**
   * Shows an 'alert' prompt.
   *
   * @param msg message to show
   */
    protected final void alert(Object msg) {
        JOptionPane.showMessageDialog(null, String.valueOf(msg));
    }

    /**
   * URL-encodes the input string.
   *
   * @param str string to be URL encoded
   * @return URL-encoded version of str
   */
    protected final String urlEncode(String str) {
        try {
            return URLEncoder.encode(str);
        } catch (Exception e) {
            handle(e);
        }
        return null;
    }

    /**
   * URL-decodes the input string.
   *
   * @param str string to be URL decoded
   * @return URL-decoded version of str
   */
    protected final String urlDecode(String str) {
        try {
            return URLDecoder.decode(str);
        } catch (Exception e) {
            handle(e);
        }
        return null;
    }

    /**
   * Returns {#dateTime(Date)} with <code>new Date()</code>.
   *
   * @return {#dateTime(Date)} with <code>new Date()</code>.
   */
    protected final String dateTime() {
        return dateTime(new Date());
    }

    /**
   * Returns a String of the date and time.
   *
   * @param date the Date used
   * @return a String of the date and time
   */
    protected final String dateTime(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return dateFormat.format(date);
    }

    private String invokeInternal(String[] cmd) throws Exception {
        final Process proc = Runtime.getRuntime().exec(cmd);
        final BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        final StringBuffer res = new StringBuffer();
        Thread t = new Thread(new Runnable() {

            public void run() {
                while (true) {
                    try {
                        String line = in.readLine();
                        if (line == null) break;
                        res.append(line).append("|");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    proc.destroy();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        proc.waitFor();
        proc.exitValue();
        t.join();
        return res.toString();
    }

    private void showQuitFrame(final Server s) {
        JFrame f = new JFrame();
        f.getContentPane().add(new JButton(new AbstractAction("Quit") {

            public void actionPerformed(ActionEvent e) {
                quit(s);
            }
        }));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
    }

    private void quit(Server s) {
        if (s != null) s.stop();
        System.exit(0);
    }

    /**
   * A panel that is lit up when handling a request.
   */
    private class HandlerPanel extends JPanel {

        private final Color COLOR_ON = Color.red;

        private final Color COLOR_OFF = Color.black;

        private final Handler h;

        private final JLabel label;

        HandlerPanel(Handler h) {
            this.h = h;
            label = new JLabel(h.name());
            add(label);
        }

        void setValue(boolean on) {
            if (on) {
                label.setForeground(COLOR_ON);
                new Thread(new Runnable() {

                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ignore) {
                        }
                        off();
                    }
                }).start();
            } else {
                label.setForeground(COLOR_OFF);
            }
        }

        void on() {
            setValue(true);
        }

        void off() {
            setValue(false);
        }
    }

    private void showHandlerFrame(final Server s) {
        JFrame f = new JFrame();
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));
        for (Handler h : s.getHandlers()) {
            HandlerPanel p = new HandlerPanel(h);
            handlers2panels.put(h, p);
            f.getContentPane().add(p);
        }
        f.pack();
        f.setVisible(true);
    }

    private void showDebugFrame(final Server s) {
        JFrame f = new JFrame();
        JToolBar tb = new JToolBar();
        tb.add(new AbstractAction("Quit") {

            public void actionPerformed(ActionEvent e) {
                quit(s);
            }
        });
        tb.add(new AbstractAction("Clear") {

            public void actionPerformed(ActionEvent e) {
                debug.clear();
            }
        });
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(tb, BorderLayout.NORTH);
        f.getContentPane().add(debug, BorderLayout.CENTER);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
        note("Using server " + this);
        String ip = getIP();
        if (ip != null) {
            note("Have local IP:" + ip);
        }
    }

    private String getIP() {
        String ip = null;
        try {
            ip = String.valueOf(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ignore) {
        }
        return ip;
    }

    private void addHandlers(Server s) {
        for (Handler h : getHandlers()) {
            s.registerHandler(h);
        }
    }

    private void showTheIP() {
        String ip = null;
        try {
            ip = String.valueOf(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ignore) {
        }
        if (ip == null) {
            JOptionPane.showMessageDialog(null, "Couldn't find a local address", "Warning", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showInputDialog(null, "You local IP is", ip);
        }
    }
}
