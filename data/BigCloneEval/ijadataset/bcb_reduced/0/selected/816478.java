package weka.gui;

import weka.core.ClassDiscovery;
import weka.core.Trie;
import weka.core.Utils;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Creates a very simple command line for invoking the main method of
 * classes. System.out and System.err are redirected to an output area.
 * Features a simple command history -- use up and down arrows to move
 * through previous commmands. This gui uses only AWT (i.e. no Swing).
 *
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1.6 $
 */
public class SimpleCLIPanel extends JPanel implements ActionListener {

    /** for serialization. */
    private static final long serialVersionUID = -7377739469759943231L;

    /** The filename of the properties file. */
    protected static String FILENAME = "SimpleCLI.props";

    /** The default location of the properties file. */
    protected static String PROPERTY_FILE = "weka/gui/" + FILENAME;

    /** Contains the SimpleCLI properties. */
    protected static Properties PROPERTIES;

    static {
        try {
            PROPERTIES = Utils.readProperties(PROPERTY_FILE);
            java.util.Enumeration keys = (java.util.Enumeration) PROPERTIES.propertyNames();
            if (!keys.hasMoreElements()) {
                throw new Exception("Failed to read a property file for the SimpleCLI");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Could not read a configuration file for the SimpleCLI.\n" + "An example file is included with the Weka distribution.\n" + "This file should be named \"" + PROPERTY_FILE + "\" and\n" + "should be placed either in your user home (which is set\n" + "to \"" + System.getProperties().getProperty("user.home") + "\")\n" + "or the directory that java was started from\n", "SimpleCLI", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** The output area canvas added to the frame. */
    protected JTextArea m_OutputArea = new JTextArea();

    /** The command input area. */
    protected JTextField m_Input = new JTextField();

    /** The history of commands entered interactively. */
    protected Vector m_CommandHistory = new Vector();

    /** The current position in the command history. */
    protected int m_HistoryPos = 0;

    /** The new output stream for System.out. */
    protected PipedOutputStream m_POO = new PipedOutputStream();

    /** The new output stream for System.err. */
    protected PipedOutputStream m_POE = new PipedOutputStream();

    /** The thread that sends output from m_POO to the output box. */
    protected Thread m_OutRedirector;

    /** The thread that sends output from m_POE to the output box. */
    protected Thread m_ErrRedirector;

    /** The thread currently running a class main method. */
    protected Thread m_RunThread;

    /** The commandline completion. */
    protected CommandlineCompletion m_Completion;

    /**
   * A class that sends all lines from a reader to a TextArea component.
   * 
   * @author Len Trigg (trigg@cs.waikato.ac.nz)
   * @version $Revision: 1.6 $
   */
    class ReaderToTextArea extends Thread {

        /** The reader being monitored. */
        protected LineNumberReader m_Input;

        /** The output text component. */
        protected JTextArea m_Output;

        /**
     * Sets up the ReaderToTextArea.
     *
     * @param input the Reader to monitor
     * @param output the TextArea to send output to
     */
        public ReaderToTextArea(Reader input, JTextArea output) {
            setDaemon(true);
            m_Input = new LineNumberReader(input);
            m_Output = output;
        }

        /**
     * Sit here listening for lines of input and appending them straight
     * to the text component.
     */
        public void run() {
            while (true) {
                try {
                    m_Output.append(m_Input.readLine() + '\n');
                    m_Output.setCaretPosition(m_Output.getDocument().getLength());
                } catch (Exception ex) {
                    try {
                        sleep(100);
                    } catch (Exception e) {
                    }
                }
            }
        }
    }

    /**
   * A class that handles running the main method of the class
   * in a separate thread.
   * 
   * @author Len Trigg (trigg@cs.waikato.ac.nz)
   * @version $Revision: 1.6 $
   */
    class ClassRunner extends Thread {

        /** Stores the main method to call. */
        protected Method m_MainMethod;

        /** Stores the command line arguments to pass to the main method. */
        String[] m_CommandArgs;

        /**
     * Sets up the class runner thread.
     *
     * @param theClass the Class to call the main method of
     * @param commandArgs an array of Strings to use as command line args
     * @throws Exception if an error occurs
     */
        public ClassRunner(Class theClass, String[] commandArgs) throws Exception {
            setDaemon(true);
            Class[] argTemplate = { String[].class };
            m_CommandArgs = commandArgs;
            m_MainMethod = theClass.getMethod("main", argTemplate);
            if (((m_MainMethod.getModifiers() & Modifier.STATIC) == 0) || (m_MainMethod.getModifiers() & Modifier.PUBLIC) == 0) {
                throw new NoSuchMethodException("main(String[]) method of " + theClass.getName() + " is not public and static.");
            }
        }

        /**
     * Starts running the main method.
     */
        public void run() {
            PrintStream outOld = null;
            PrintStream outNew = null;
            String outFilename = null;
            if (m_CommandArgs.length > 2) {
                String action = m_CommandArgs[m_CommandArgs.length - 2];
                if (action.equals(">")) {
                    outOld = System.out;
                    try {
                        outFilename = m_CommandArgs[m_CommandArgs.length - 1];
                        outNew = new PrintStream(new File(outFilename));
                        System.setOut(outNew);
                        m_CommandArgs[m_CommandArgs.length - 2] = "";
                        m_CommandArgs[m_CommandArgs.length - 1] = "";
                    } catch (Exception e) {
                        System.setOut(outOld);
                        outOld = null;
                    }
                }
            }
            try {
                Object[] args = { m_CommandArgs };
                m_MainMethod.invoke(null, args);
                if (isInterrupted()) {
                    System.err.println("[...Interrupted]");
                }
            } catch (Exception ex) {
                if (ex.getMessage() == null) {
                    System.err.println("[...Killed]");
                } else {
                    System.err.println("[Run exception] " + ex.getMessage());
                }
            } finally {
                m_RunThread = null;
            }
            if (outOld != null) {
                outNew.flush();
                outNew.close();
                System.setOut(outOld);
                System.out.println("Finished redirecting output to '" + outFilename + "'.");
            }
        }
    }

    /**
   * A class for commandline completion of classnames.
   * 
   * @author  FracPete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 1.6 $
   */
    public static class CommandlineCompletion {

        /** all the available packages. */
        protected static Vector<String> m_Packages;

        /** a trie for storing the packages. */
        protected static Trie m_Trie;

        /** debug mode on/off. */
        protected boolean m_Debug = false;

        /**
     * default constructor.
     */
        public CommandlineCompletion() {
            super();
            if (m_Packages == null) {
                Vector list = ClassDiscovery.findPackages();
                HashSet<String> set = new HashSet<String>();
                for (int i = 0; i < list.size(); i++) {
                    String[] parts = ((String) list.get(i)).split("\\.");
                    for (int n = 1; n < parts.length; n++) {
                        String pkg = "";
                        for (int m = 0; m <= n; m++) {
                            if (m > 0) pkg += ".";
                            pkg += parts[m];
                        }
                        set.add(pkg);
                    }
                }
                m_Packages = new Vector<String>();
                m_Packages.addAll(set);
                Collections.sort(m_Packages);
                m_Trie = new Trie();
                m_Trie.addAll(m_Packages);
            }
        }

        /**
     * returns whether debug mode is on.
     * 
     * @return		true if debug is on
     */
        public boolean getDebug() {
            return m_Debug;
        }

        /**
     * sets debug mode on/off.
     * 
     * @param value	if true then debug mode is on
     */
        public void setDebug(boolean value) {
            m_Debug = value;
        }

        /**
     * tests whether the given partial string is the name of a class with
     * classpath - it basically tests, whether the string consists only
     * of alphanumeric literals, underscores and dots.
     * 
     * @param partial	the string to test
     * @return		true if it looks like a classname
     */
        public boolean isClassname(String partial) {
            return (partial.replaceAll("[a-zA-Z0-9\\-\\.]*", "").length() == 0);
        }

        /**
     * returns the packages part of the partial classname.
     * 
     * @param partial	the partial classname
     * @return		the package part of the partial classname
     */
        public String getPackage(String partial) {
            String result;
            int i;
            boolean wasDot;
            char c;
            result = "";
            wasDot = false;
            for (i = 0; i < partial.length(); i++) {
                c = partial.charAt(i);
                if (wasDot && ((c >= 'A') && (c <= 'Z'))) {
                    break;
                } else if (c == '.') {
                    wasDot = true;
                    result += "" + c;
                } else {
                    wasDot = false;
                    result += "" + c;
                }
            }
            if (result.endsWith(".")) result = result.substring(0, result.length() - 1);
            return result;
        }

        /**
     * returns the classname part of the partial classname.
     * 
     * @param partial	the partial classname
     * @return		the class part of the classname
     */
        public String getClassname(String partial) {
            String result;
            String pkg;
            pkg = getPackage(partial);
            if (pkg.length() + 1 < partial.length()) result = partial.substring(pkg.length() + 1); else result = "";
            return result;
        }

        /**
     * returns all the file/dir matches with the partial search string.
     * 
     * @param partial	the partial search string
     * @return		all the matches
     */
        public Vector<String> getFileMatches(String partial) {
            Vector<String> result;
            File file;
            File dir;
            File[] files;
            int i;
            String prefix;
            boolean caseSensitive;
            String name;
            boolean match;
            result = new Vector<String>();
            caseSensitive = (File.separatorChar != '\\');
            if (m_Debug) System.out.println("case-sensitive=" + caseSensitive);
            if (partial.startsWith("~")) partial = System.getProperty("user.home") + partial.substring(1);
            file = new File(partial);
            dir = null;
            prefix = null;
            if (file.exists()) {
                if (file.isDirectory()) {
                    dir = file;
                    prefix = null;
                } else {
                    dir = file.getParentFile();
                    prefix = file.getName();
                }
            } else {
                dir = file.getParentFile();
                prefix = file.getName();
            }
            if (m_Debug) System.out.println("search in dir=" + dir + ", prefix=" + prefix);
            if (dir != null) {
                files = dir.listFiles();
                if (files != null) {
                    for (i = 0; i < files.length; i++) {
                        name = files[i].getName();
                        if ((prefix != null) && caseSensitive) match = name.startsWith(prefix); else if ((prefix != null) && !caseSensitive) match = name.toLowerCase().startsWith(prefix.toLowerCase()); else match = true;
                        if (match) {
                            if (prefix != null) {
                                result.add(partial.substring(0, partial.length() - prefix.length()) + name);
                            } else {
                                if (partial.endsWith("\\") || partial.endsWith("/")) result.add(partial + name); else result.add(partial + File.separator + name);
                            }
                        }
                    }
                } else {
                    System.err.println("Invalid path: " + partial);
                }
            }
            if (result.size() > 1) Collections.sort(result);
            if (m_Debug) {
                System.out.println("file matches:");
                for (i = 0; i < result.size(); i++) System.out.println(result.get(i));
            }
            return result;
        }

        /**
     * returns all the class/package matches with the partial search string.
     * 
     * @param partial	the partial search string
     * @return		all the matches
     */
        public Vector<String> getClassMatches(String partial) {
            String pkg;
            String cls;
            Vector<String> result;
            Vector<String> list;
            int i;
            int index;
            Trie tmpTrie;
            HashSet set;
            String tmpStr;
            pkg = getPackage(partial);
            cls = getClassname(partial);
            if (getDebug()) System.out.println("\nsearch for: '" + partial + "' => package=" + pkg + ", class=" + cls);
            result = new Vector<String>();
            if (cls.length() == 0) {
                list = m_Trie.getWithPrefix(pkg);
                set = new HashSet();
                for (i = 0; i < list.size(); i++) {
                    tmpStr = list.get(i);
                    if (tmpStr.length() < partial.length()) continue;
                    if (tmpStr.equals(partial)) continue;
                    index = tmpStr.indexOf('.', partial.length() + 1);
                    if (index > -1) set.add(tmpStr.substring(0, index)); else set.add(tmpStr);
                }
                result.addAll(set);
                if (result.size() > 1) Collections.sort(result);
            }
            list = ClassDiscovery.find(Object.class, pkg);
            tmpTrie = new Trie();
            tmpTrie.addAll(list);
            list = tmpTrie.getWithPrefix(partial);
            result.addAll(list);
            if (result.size() > 1) Collections.sort(result);
            if (m_Debug) {
                System.out.println("class/package matches:");
                for (i = 0; i < result.size(); i++) System.out.println(result.get(i));
            }
            return result;
        }

        /**
     * returns all the matches with the partial search string, files or
     * classes.
     * 
     * @param partial	the partial search string
     * @return		all the matches
     */
        public Vector<String> getMatches(String partial) {
            if (isClassname(partial)) return getClassMatches(partial); else return getFileMatches(partial);
        }

        /**
     * returns the common prefix for all the items in the list.
     * 
     * @param list	the list to return the common prefix for
     * @return		the common prefix of all the items
     */
        public String getCommonPrefix(Vector<String> list) {
            String result;
            Trie trie;
            trie = new Trie();
            trie.addAll(list);
            result = trie.getCommonPrefix();
            if (m_Debug) System.out.println(list + "\n  --> common prefix: '" + result + "'");
            return result;
        }
    }

    /**
   * Constructor.
   *
   * @throws Exception if an error occurs
   */
    public SimpleCLIPanel() throws Exception {
        setLayout(new BorderLayout());
        add(new JScrollPane(m_OutputArea), "Center");
        add(m_Input, "South");
        m_Input.setFont(new Font("Monospaced", Font.PLAIN, 12));
        m_Input.addActionListener(this);
        m_Input.setFocusTraversalKeysEnabled(false);
        m_Input.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                doHistory(e);
                doCommandlineCompletion(e);
            }
        });
        m_OutputArea.setEditable(false);
        m_OutputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        PipedInputStream pio = new PipedInputStream(m_POO);
        System.setOut(new PrintStream(m_POO));
        Reader r = new InputStreamReader(pio);
        m_OutRedirector = new ReaderToTextArea(r, m_OutputArea);
        m_OutRedirector.start();
        PipedInputStream pie = new PipedInputStream(m_POE);
        System.setErr(new PrintStream(m_POE));
        r = new InputStreamReader(pie);
        m_ErrRedirector = new ReaderToTextArea(r, m_OutputArea);
        m_ErrRedirector.start();
        m_Completion = new CommandlineCompletion();
        System.out.println("\nWelcome to the WEKA SimpleCLI\n\n" + "Enter commands in the textfield at the bottom of \n" + "the window. Use the up and down arrows to move \n" + "through previous commands.\n" + "Command completion for classnames and files is \n" + "initiated with <Tab>. In order to distinguish \n" + "between files and classnames, file names must \n" + "be either absolute or start with '." + File.separator + "' or '~/'\n" + "(the latter is a shortcut for the home directory).\n" + "<Alt+BackSpace> is used for deleting the text\n" + "in the commandline in chunks.\n");
        runCommand("help");
        loadHistory();
    }

    /**
   * Executes a simple cli command.
   *
   * @param commands the command string
   * @throws Exception if an error occurs
   */
    public void runCommand(String commands) throws Exception {
        System.out.println("> " + commands + '\n');
        System.out.flush();
        String[] commandArgs = Utils.splitOptions(commands);
        if (commandArgs.length == 0) {
            return;
        }
        if (commandArgs[0].equals("java")) {
            commandArgs[0] = "";
            try {
                if (commandArgs.length == 1) {
                    throw new Exception("No class name given");
                }
                String className = commandArgs[1];
                commandArgs[1] = "";
                if (m_RunThread != null) {
                    throw new Exception("An object is already running, use \"break\"" + " to interrupt it.");
                }
                Class theClass = Class.forName(className);
                Vector argv = new Vector();
                for (int i = 2; i < commandArgs.length; i++) argv.add(commandArgs[i]);
                m_RunThread = new ClassRunner(theClass, (String[]) argv.toArray(new String[argv.size()]));
                m_RunThread.setPriority(Thread.MIN_PRIORITY);
                m_RunThread.start();
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        } else if (commandArgs[0].equals("cls")) {
            m_OutputArea.setText("");
        } else if (commandArgs[0].equals("history")) {
            System.out.println("Command history:");
            for (int i = 0; i < m_CommandHistory.size(); i++) System.out.println(m_CommandHistory.get(i));
            System.out.println();
        } else if (commandArgs[0].equals("break")) {
            if (m_RunThread == null) {
                System.err.println("Nothing is currently running.");
            } else {
                System.out.println("[Interrupt...]");
                m_RunThread.interrupt();
            }
        } else if (commandArgs[0].equals("kill")) {
            if (m_RunThread == null) {
                System.err.println("Nothing is currently running.");
            } else {
                System.out.println("[Kill...]");
                m_RunThread.stop();
                m_RunThread = null;
            }
        } else if (commandArgs[0].equals("exit")) {
            Container parent = getParent();
            Container frame = null;
            boolean finished = false;
            while (!finished) {
                if ((parent instanceof JFrame) || (parent instanceof Frame) || (parent instanceof JInternalFrame)) {
                    frame = parent;
                    finished = true;
                }
                if (!finished) {
                    parent = parent.getParent();
                    finished = (parent == null);
                }
            }
            if (frame != null) {
                if (frame instanceof JInternalFrame) ((JInternalFrame) frame).doDefaultCloseAction(); else ((Window) frame).dispatchEvent(new WindowEvent((Window) frame, WindowEvent.WINDOW_CLOSING));
            }
        } else {
            boolean help = ((commandArgs.length > 1) && commandArgs[0].equals("help"));
            if (help && commandArgs[1].equals("java")) {
                System.err.println("java <classname> <args>\n\n" + "Starts the main method of <classname> with " + "the supplied command line arguments (if any).\n" + "The command is started in a separate thread, " + "and may be interrupted with the \"break\"\n" + "command (friendly), or killed with the \"kill\" " + "command (unfriendly).\n" + "Redirecting can be done with '>' followed by the " + "file to write to, e.g.:\n" + "  java some.Class > ." + File.separator + "some.txt");
            } else if (help && commandArgs[1].equals("break")) {
                System.err.println("break\n\n" + "Attempts to nicely interrupt the running job, " + "if any. If this doesn't respond in an\n" + "acceptable time, use \"kill\".\n");
            } else if (help && commandArgs[1].equals("kill")) {
                System.err.println("kill\n\n" + "Kills the running job, if any. You should only " + "use this if the job doesn't respond to\n" + "\"break\".\n");
            } else if (help && commandArgs[1].equals("cls")) {
                System.err.println("cls\n\n" + "Clears the output area.\n");
            } else if (help && commandArgs[1].equals("history")) {
                System.err.println("history\n\n" + "Prints all issued commands.\n");
            } else if (help && commandArgs[1].equals("exit")) {
                System.err.println("exit\n\n" + "Exits the SimpleCLI program.\n");
            } else {
                System.err.println("Command must be one of:\n" + "\tjava <classname> <args> [ > file]\n" + "\tbreak\n" + "\tkill\n" + "\tcls\n" + "\thistory\n" + "\texit\n" + "\thelp <command>\n");
            }
        }
    }

    /**
   * Changes the currently displayed command line when certain keys
   * are pressed. The up arrow moves back through history entries
   * and the down arrow moves forward through history entries.
   *
   * @param e a value of type 'KeyEvent'
   */
    public void doHistory(KeyEvent e) {
        if (e.getSource() == m_Input) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    if (m_HistoryPos > 0) {
                        m_HistoryPos--;
                        String command = (String) m_CommandHistory.elementAt(m_HistoryPos);
                        m_Input.setText(command);
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if (m_HistoryPos < m_CommandHistory.size()) {
                        m_HistoryPos++;
                        String command = "";
                        if (m_HistoryPos < m_CommandHistory.size()) {
                            command = (String) m_CommandHistory.elementAt(m_HistoryPos);
                        }
                        m_Input.setText(command);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
   * performs commandline completion on packages and classnames.
   * 
   * @param e a value of type 'KeyEvent'
   */
    public void doCommandlineCompletion(KeyEvent e) {
        if (e.getSource() == m_Input) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_TAB:
                    if (e.getModifiers() == 0) {
                        m_Input.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        m_OutputArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        try {
                            String txt = m_Input.getText();
                            if (txt.trim().startsWith("java ")) {
                                int pos = m_Input.getCaretPosition();
                                int nonNameCharPos = -1;
                                for (int i = pos - 1; i >= 0; i--) {
                                    if ((txt.charAt(i) == '"') || (txt.charAt(i) == ' ')) {
                                        nonNameCharPos = i;
                                        break;
                                    }
                                }
                                if (nonNameCharPos > -1) {
                                    String search = txt.substring(nonNameCharPos + 1, pos);
                                    Vector<String> list = m_Completion.getMatches(search);
                                    String common = m_Completion.getCommonPrefix(list);
                                    if ((search.toLowerCase() + File.separator).equals(common.toLowerCase())) common = search;
                                    if (common.length() > search.length()) {
                                        try {
                                            m_Input.getDocument().remove(nonNameCharPos + 1, search.length());
                                            m_Input.getDocument().insertString(nonNameCharPos + 1, common, null);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    } else if (list.size() > 1) {
                                        System.out.println("\nPossible matches:");
                                        for (int i = 0; i < list.size(); i++) System.out.println("  " + list.get(i));
                                    } else {
                                    }
                                }
                            }
                        } finally {
                            m_Input.setCursor(null);
                            m_OutputArea.setCursor(null);
                        }
                    }
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    if (e.getModifiers() == KeyEvent.ALT_MASK) {
                        String txt = m_Input.getText();
                        int pos = m_Input.getCaretPosition();
                        int start = pos;
                        start--;
                        while (start >= 0) {
                            if ((txt.charAt(start) == '.') || (txt.charAt(start) == ' ') || (txt.charAt(start) == '\\') || (txt.charAt(start) == '/')) start--; else break;
                        }
                        int newPos = -1;
                        for (int i = start; i >= 0; i--) {
                            if ((txt.charAt(i) == '.') || (txt.charAt(i) == ' ') || (txt.charAt(i) == '\\') || (txt.charAt(i) == '/')) {
                                newPos = i;
                                break;
                            }
                        }
                        try {
                            m_Input.getDocument().remove(newPos + 1, pos - newPos - 1);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    /**
   * Only gets called when return is pressed in the input area, which
   * starts the command running.
   *
   * @param e a value of type 'ActionEvent'
   */
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == m_Input) {
                String command = m_Input.getText();
                int last = m_CommandHistory.size() - 1;
                if ((last < 0) || !command.equals((String) m_CommandHistory.elementAt(last))) {
                    m_CommandHistory.addElement(command);
                    saveHistory();
                }
                m_HistoryPos = m_CommandHistory.size();
                runCommand(command);
                m_Input.setText("");
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
   * loads the command history from the user's properties file.
   */
    protected void loadHistory() {
        int size;
        int i;
        String cmd;
        size = Integer.parseInt(PROPERTIES.getProperty("HistorySize", "50"));
        m_CommandHistory.clear();
        for (i = 0; i < size; i++) {
            cmd = PROPERTIES.getProperty("Command" + i, "");
            if (cmd.length() != 0) m_CommandHistory.add(cmd); else break;
        }
        m_HistoryPos = m_CommandHistory.size();
    }

    /**
   * saves the current command history in the user's home directory.
   */
    protected void saveHistory() {
        int size;
        int from;
        int i;
        String filename;
        BufferedOutputStream stream;
        size = Integer.parseInt(PROPERTIES.getProperty("HistorySize", "50"));
        from = m_CommandHistory.size() - size;
        if (from < 0) from = 0;
        PROPERTIES.setProperty("HistorySize", "" + size);
        for (i = from; i < m_CommandHistory.size(); i++) PROPERTIES.setProperty("Command" + (i - from), (String) m_CommandHistory.get(i));
        try {
            filename = System.getProperties().getProperty("user.home") + File.separatorChar + FILENAME;
            stream = new BufferedOutputStream(new FileOutputStream(filename));
            PROPERTIES.store(stream, "SimpleCLI");
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
   * Main method for testing this class.
   * 
   * @param args 	commandline arguments - ignored
   * @throws Exception	if initialization fails
   */
    public static void main(String[] args) throws Exception {
        SimpleCLIPanel panel = new SimpleCLIPanel();
        JFrame f = new JFrame();
        f.setTitle("SimpleCLI");
        f.getContentPane().add(panel);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.pack();
        f.setSize(600, 500);
        f.setVisible(true);
    }
}
