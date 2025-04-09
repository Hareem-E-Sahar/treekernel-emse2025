package CLI;

import ij.plugin.PlugIn;
import ij.IJ;
import ij.macro.Interpreter;
import ij.WindowManager;
import ij.macro.MacroConstants;
import ij.gui.GenericDialog;
import ij.plugin.frame.Recorder;
import common.AbstractInterpreter;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JEditorPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusAdapter;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import java.util.ArrayList;
import javax.swing.JPopupMenu;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.Toolkit;
import java.awt.FileDialog;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.awt.Font;
import java.awt.Dimension;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.awt.Component;
import java.util.Arrays;

public class CLI_ extends AbstractInterpreter {

    String macro = "\n";

    final String l = "\n";

    final String pre = "> ";

    JPopupMenu popup_menu;

    String selection;

    PopupListener popup_listener = new PopupListener();

    static String user_dir = fixWindowsPath(System.getProperty("user.dir"));

    static final String file_separator = "/";

    static final String trash_can = user_dir + "/plugins/CLITrashCan";

    boolean magic = false;

    static final String dir_macros = user_dir + "/macros";

    boolean allow_print = true;

    String current_root_dir;

    static String[] all_macro_functions = new String[MacroConstants.functions.length + MacroConstants.numericFunctions.length + MacroConstants.stringFunctions.length + MacroConstants.arrayFunctions.length];

    static String fixWindowsPath(String path) {
        if (IJ.isWindows()) {
            char[] c = new char[path.length()];
            path.getChars(0, c.length, c, 0);
            for (int i = 0; i < c.length; i++) {
                if ('\\' == c[i]) {
                    c[i] = '/';
                }
            }
            return new String(c);
        }
        return path;
    }

    public void run(String arg) {
        setTitle("ImageJ Terminal v1.07");
        super.run(arg);
        println("-->  Welcome. Type   help   in the text field below.\n");
        if (IJ.isWindows()) {
            current_root_dir = user_dir.substring(0, 2);
        } else {
            current_root_dir = "/";
        }
        System.arraycopy(MacroConstants.functions, 0, all_macro_functions, 0, MacroConstants.functions.length);
        System.arraycopy(MacroConstants.numericFunctions, 0, all_macro_functions, MacroConstants.functions.length, MacroConstants.numericFunctions.length);
        int start_index = MacroConstants.functions.length + MacroConstants.numericFunctions.length;
        System.arraycopy(MacroConstants.stringFunctions, 0, all_macro_functions, start_index, MacroConstants.stringFunctions.length);
        start_index += MacroConstants.stringFunctions.length;
        System.arraycopy(MacroConstants.arrayFunctions, 0, all_macro_functions, start_index, MacroConstants.arrayFunctions.length);
    }

    protected void makeGUI() {
        super.makeGUI();
        popup_menu = new JPopupMenu();
        addPopupMenuItem("Execute Selection");
        addPopupMenuItem("Record");
        addPopupMenuItem("Copy");
        addPopupMenuItem("Save Selection");
        addPopupMenuItem("Save & Exec Selection");
    }

    void addPopupMenuItem(String name) {
        JMenuItem mi = new JMenuItem(name);
        mi.addActionListener(popup_listener);
        popup_menu.add(mi);
    }

    protected String getLineCommentMark() {
        return "//";
    }

    protected Object eval(String temp) {
        try {
            temp = temp.trim();
            if (0 == temp.length()) {
                return null;
            }
            if (isShellCommand(temp)) {
                return null;
            }
            temp = specialEditing(temp);
            if (0 == temp.length()) {
                return null;
            }
            temp += l;
            if (temp.lastIndexOf('\\') == temp.length() - 2) {
                String newline = "";
                if (1 == macro.length()) {
                    newline += l;
                }
                newline += temp.substring(0, temp.lastIndexOf('\\'));
                macro += newline;
                if (MacroRecord.isRecording()) {
                    MacroRecord.appendToCurrent(newline + l);
                }
            } else {
                macro += temp;
                if (MacroRecord.isRecording()) {
                    MacroRecord.appendToCurrent(temp);
                }
                execMacro(macro);
                macro = l;
            }
            allow_print = true;
            return null;
        } catch (Exception e) {
            println("Some error ocurred: " + e + "\n" + new TraceError(e));
            macro = "\n";
            return null;
        }
    }

    protected synchronized void doTab(ActionEvent ae) {
        String input = prompt.getText();
        if (null == input || input.equals("")) {
            return;
        }
        String command = null;
        String output = null;
        if (input.length() > 5 && equal(input.substring(0, 6), "open(\"")) {
            command = input.substring(0, 6);
            output = fixDir(input.substring(6));
        } else {
            int space = input.lastIndexOf(' ');
            if (-1 != space) {
                command = input.substring(0, space + 1);
                output = fixDir(input.substring(space + 1));
            }
        }
        if (null != output && 0 != output.length()) {
            String the_dir = current_root_dir;
            String file_part = "";
            int last_slash = output.lastIndexOf(file_separator);
            if (0 < last_slash || (1 == output.indexOf(":/") && 2 < last_slash)) {
                the_dir = output.substring(0, last_slash);
                file_part = output.substring(last_slash + 1);
            } else if ((0 == output.indexOf('/') || 1 == output.indexOf(':')) && 0 == last_slash) {
                the_dir = current_root_dir;
                file_part = output.substring(1);
            }
            File f_the_dir = new File(the_dir);
            String[] files = f_the_dir.list(new CustomFileFilter(file_part + "*"));
            if (null != files && files.length > 1) {
                println("-->  Possible files in " + f_the_dir.getName() + " folder:");
                for (int i = 0; i < files.length; i++) {
                    File f = new File(the_dir + file_separator + files[i]);
                    screen.append("\n-->  " + files[i]);
                    if (f.isDirectory()) {
                        screen.append("/");
                    }
                }
                println("");
                String expanded = getMaxExpanded(file_part, files);
                if (-1 == input.indexOf("..") && -1 == input.indexOf('/')) {
                    prompt.setText(command + expanded);
                } else if ((0 == output.indexOf('/') || 1 == output.indexOf(':')) && (0 == output.lastIndexOf('/') || 1 == output.lastIndexOf(':'))) {
                    prompt.setText(command + the_dir + expanded);
                } else {
                    prompt.setText(command + the_dir + file_separator + expanded);
                }
            } else if (1 == files.length) {
                String path = null;
                if (-1 == input.indexOf("..") && -1 == input.indexOf('/')) {
                    path = files[0];
                } else if ((the_dir.length() - 1) != the_dir.lastIndexOf('/')) {
                    path = the_dir + file_separator + files[0];
                } else {
                    path = the_dir + files[0];
                }
                File f = new File(user_dir + file_separator + path);
                if (f.exists() && f.isDirectory()) {
                    path += file_separator;
                }
                prompt.setText(command + path);
            }
        } else {
            String partial_macro_command = null;
            int start = -1;
            if (-1 != (start = input.lastIndexOf(' ')) || -1 != (start = input.lastIndexOf('(')) || -1 != (start = input.lastIndexOf(',')) || -1 != (start = input.lastIndexOf('=')) || -1 != (start = input.lastIndexOf('+')) || -1 != (start = input.lastIndexOf('-')) || -1 != (start = input.lastIndexOf('*')) || -1 != (start = input.lastIndexOf('/'))) {
                partial_macro_command = input.substring(start + 1);
            } else {
                partial_macro_command = input;
            }
            try {
                ArrayList possibles = new ArrayList();
                String expanded_macro_command = getMaxExpandedAndPossibleList(partial_macro_command, all_macro_functions, possibles);
                if (-1 != start) {
                    prompt.setText(input.substring(0, start + 1) + expanded_macro_command);
                } else {
                    prompt.setText(expanded_macro_command);
                }
                if (1 < possibles.size()) {
                    screen.append("\n-->  Possible macro commands:");
                    for (int i = 0; i < possibles.size(); i++) {
                        screen.append("\n-->    " + (String) possibles.get(i));
                    }
                    println("");
                }
            } catch (Exception e) {
                IJ.write("Error! " + new TraceError(e));
            }
        }
        screen.setCaretPosition(screen.getText().length());
    }

    String getMaxExpanded(String part, String[] names) {
        byte[] read = new byte[names.length];
        byte one = 1;
        Arrays.fill(read, one);
        int a = part.length();
        String expanded = part;
        String previous = null;
        int index = -1;
        int num_equals = 0;
        int i = 0;
        int steps = 0;
        while (true) {
            steps++;
            for (i = 0; i < names.length; i++) {
                if (1 == read[i] && a <= names[i].length() && equal(names[i].substring(0, a), expanded)) {
                    num_equals++;
                    index = i;
                } else {
                    read[i] = 0;
                }
            }
            if (names.length == num_equals && -1 != index) {
                a++;
                previous = expanded;
                expanded = names[index].substring(0, a);
            } else {
                if (1 == steps && 1 == num_equals) {
                    expanded = names[index];
                } else {
                    expanded = previous;
                }
                break;
            }
            index = -1;
            num_equals = 0;
        }
        return expanded;
    }

    String getMaxExpandedAndPossibleList(String part, String[] names, ArrayList possibles) {
        byte[] read = new byte[names.length];
        byte one = 1;
        Arrays.fill(read, one);
        int a = part.length();
        String expanded = part;
        String previous = null;
        int index = -1;
        int num_equals = 0;
        int i = 0;
        int steps = 0;
        while (true) {
            steps++;
            for (i = 0; i < names.length; i++) {
                if (1 == read[i] && a <= names[i].length() && equal(names[i].substring(0, a), expanded)) {
                    num_equals++;
                    index = i;
                    possibles.add(names[i]);
                } else {
                    read[i] = 0;
                }
            }
            if (names.length == num_equals && -1 != index) {
                a++;
                previous = expanded;
                expanded = names[index].substring(0, a);
            } else {
                if (1 == steps && 1 == num_equals) {
                    expanded = names[index];
                } else if (1 == steps) {
                    expanded = part;
                } else {
                    expanded = previous;
                }
                break;
            }
            index = -1;
            num_equals = 0;
            possibles.clear();
        }
        return expanded;
    }

    String specialEditing(String temp_) {
        String temp = fixWindowsPath(temp_);
        if (temp_.length() - 1 == temp_.lastIndexOf('\\')) {
            temp = temp.substring(0, temp.length() - 1) + "\\";
        }
        if (temp.length() > 4 && equal("open(", temp.substring(0, temp.indexOf('(') + 1))) {
            int first_quote_index = temp.indexOf('"');
            int second_quote_index = temp.indexOf('"', first_quote_index + 1);
            String path = temp.substring(first_quote_index + 1, second_quote_index);
            if (path.startsWith("/") || 1 == path.indexOf(":/")) {
            } else {
                path = user_dir + file_separator + path;
            }
            return "open(\"" + path + temp.substring(second_quote_index);
        }
        if (temp.length() > 3 && equal("open", temp.substring(0, 4))) {
            if (4 == temp.length()) {
                println("-->  Usage:  open <file_name>\n-->    Asterisks allowed:  open *name | *name* | *name\n-->    and multiple files:  open file1 file2");
                return "";
            }
            StringTokenizer stsp;
            if (-1 == temp.indexOf('\"')) {
                stsp = new StringTokenizer(temp, " ");
            } else {
                stsp = new StringTokenizer(temp, "\"");
            }
            String open = stsp.nextToken();
            String txt = "";
            boolean not_first = false;
            String dir_path;
            File dir;
            String[] image_file;
            String backslash = "\\";
            while (stsp.hasMoreElements()) {
                String name = stsp.nextToken();
                if (equal(name, backslash)) {
                    txt += backslash;
                    break;
                }
                if (name.startsWith(file_separator) || 1 == name.indexOf(":/")) {
                    int last_slash = name.lastIndexOf(file_separator);
                    if (0 == last_slash || (1 == name.indexOf(':') && 3 >= name.length())) {
                        dir_path = name.substring(0, last_slash);
                        dir = new File(dir_path);
                        image_file = dir.list(new ImageFileFilter(name.substring(1)));
                    } else {
                        dir_path = current_root_dir + name.substring(0, last_slash);
                        dir = new File(dir_path);
                        image_file = dir.list(new ImageFileFilter(name.substring(last_slash + 1)));
                    }
                } else if (-1 == name.indexOf(file_separator)) {
                    dir = new File(user_dir);
                    image_file = dir.list(new ImageFileFilter(name));
                } else {
                    int last_slash = name.lastIndexOf(file_separator);
                    dir_path = user_dir + file_separator + name.substring(0, last_slash);
                    dir = new File(dir_path);
                    image_file = dir.list(new ImageFileFilter(name.substring(last_slash + 1)));
                }
                if (0 == image_file.length) {
                    println("\n--> No such file/s.");
                } else {
                    for (int i = 0; i < image_file.length; i++) {
                        if (not_first) txt += l;
                        txt += "open(\"";
                        try {
                            txt += fixWindowsPath(dir.getCanonicalPath()) + file_separator + image_file[i] + "\");";
                        } catch (IOException ioe) {
                            IJ.showMessage("Can't find canonical path for " + dir.getName());
                        }
                        not_first = true;
                    }
                }
            }
            return txt;
        }
        if (magic) {
            String after_magic = temp.trim();
            boolean append_backslash = false;
            if ((after_magic.length()) - 1 == after_magic.lastIndexOf('\\')) {
                after_magic = after_magic.substring(0, after_magic.length() - 1).trim();
                append_backslash = true;
            }
            int a = 0;
            if (-1 == after_magic.indexOf('(') && 0 != after_magic.indexOf('{') && 0 != after_magic.indexOf('}')) {
                int first_space = after_magic.indexOf(" ");
                if (-1 != first_space) {
                    after_magic = after_magic.substring(0, first_space) + "(" + after_magic.substring(first_space + 1) + ");";
                } else {
                    after_magic += "();";
                }
            }
            a = after_magic.indexOf('(');
            int b = after_magic.indexOf("(\"");
            if (-1 != a && a != b && -1 == after_magic.indexOf('\"') && -1 == after_magic.indexOf(';') && 0 != after_magic.indexOf("for") && 0 != after_magic.indexOf("while") && 0 != after_magic.indexOf("if")) {
                int index_comma = after_magic.indexOf(',');
                int end = after_magic.lastIndexOf(')');
                String the_command = after_magic.substring(a + 1, end);
                if (0 == the_command.length()) {
                } else if (-1 == index_comma) {
                    try {
                        double num = Double.parseDouble(the_command);
                        after_magic = after_magic.substring(0, a) + "(" + the_command + after_magic.substring(end);
                    } catch (NumberFormatException nfe) {
                        after_magic = after_magic.substring(0, a) + "(\"" + the_command + "\"" + after_magic.substring(end);
                    }
                } else {
                    StringTokenizer st = new StringTokenizer(the_command, ",");
                    String multiple_command = "";
                    String quote = "\"";
                    String comma = ",";
                    while (st.hasMoreElements()) {
                        String token = st.nextToken().trim();
                        try {
                            double num = Double.parseDouble(token);
                            multiple_command += token;
                        } catch (NumberFormatException nfe) {
                            multiple_command += quote + token + quote;
                        }
                        multiple_command += comma;
                    }
                    multiple_command = multiple_command.substring(0, multiple_command.length() - 1);
                }
            }
            if (0 == after_magic.indexOf("dc(\"")) {
                after_magic = "doCommand" + after_magic.substring(2);
            }
            a = after_magic.indexOf("(\"");
            if (-1 != a && !equal(after_magic.substring(0, a), "open(") && -1 == after_magic.indexOf('[')) {
                int start = after_magic.indexOf("\"");
                int end = after_magic.indexOf("\"", start + 1);
                StringTokenizer st = new StringTokenizer(after_magic.substring(start + 1, end), " ");
                String the_command = "";
                while (st.hasMoreElements()) {
                    String c = st.nextToken();
                    the_command += c.substring(0, 1).toUpperCase() + c.substring(1) + " ";
                }
                the_command = the_command.trim();
                after_magic = after_magic.substring(0, start + 1) + the_command + after_magic.substring(end);
            }
            if (append_backslash) {
                after_magic += "\\";
            }
            return after_magic;
        }
        return temp;
    }

    void putTokens(String from, String separator, ArrayList al) {
        StringTokenizer st = new StringTokenizer(from, separator);
        while (st.hasMoreElements()) {
            String token = st.nextToken();
            if (null != token && 0 != token.length()) {
                al.add(token);
            }
        }
    }

    boolean isShellCommand(String input_line) {
        ArrayList al = new ArrayList();
        if (-1 != input_line.indexOf('(')) {
            return false;
        }
        if (-1 == input_line.indexOf('\"')) {
            StringTokenizer st = new StringTokenizer(input_line, " ");
            while (st.hasMoreElements()) {
                al.add(st.nextToken());
            }
        } else {
            int num_quotes = 0;
            int p = input_line.indexOf('\"');
            while (-1 != p) {
                num_quotes++;
                p = input_line.indexOf('\"', p + 1);
            }
            if (0 != num_quotes % 2.0) {
                println("\n-->  Wrong number of quotes!");
                return true;
            }
            String a_command = null;
            int first_space = input_line.indexOf(' ');
            if (-1 != first_space) {
                a_command = input_line.substring(0, first_space);
            }
            if (null != a_command) {
                if (-1 == a_command.indexOf('(') && -1 == a_command.indexOf('+') && -1 == a_command.indexOf('\"') && -1 == a_command.indexOf('-') && -1 == a_command.indexOf('*') && -1 == a_command.indexOf('/')) {
                } else {
                    return false;
                }
            } else {
                if (-1 == input_line.indexOf('(') && -1 == input_line.indexOf('+') && -1 == input_line.indexOf('\"') && -1 == input_line.indexOf('-') && -1 == input_line.indexOf('*') && -1 == input_line.indexOf('/')) {
                    a_command = input_line;
                } else {
                    return false;
                }
            }
            al.add(a_command);
            String args = null;
            if (-1 != first_space) {
                args = input_line.substring(first_space + 1).trim();
            }
            if (null != args && 0 != args.length()) {
                int start_quote = args.indexOf('\"');
                int end_quote = args.indexOf('\"', start_quote + 1);
                if (0 != start_quote) {
                    putTokens(args.substring(0, start_quote), " ", al);
                }
                int stopper = 3;
                while (-1 != start_quote) {
                    String one_arg = args.substring(start_quote + 1, end_quote);
                    if (null != one_arg && 0 != one_arg.length()) {
                        al.add(one_arg);
                    }
                    if (args.length() <= end_quote + 1) {
                        break;
                    }
                    int next_quote = args.indexOf('\"', end_quote + 1);
                    if (-1 != next_quote && (next_quote - 2) != end_quote) {
                        putTokens(args.substring(end_quote + 1, next_quote).trim(), " ", al);
                        start_quote = next_quote;
                        end_quote = args.indexOf('\"', next_quote + 1);
                    } else if (-1 == next_quote) {
                        putTokens(args.substring(end_quote + 1).trim(), " ", al);
                        start_quote = -1;
                    } else {
                        start_quote = next_quote;
                        end_quote = args.indexOf('\"', start_quote + 1);
                    }
                    stopper--;
                    if (stopper == 0) break;
                }
            }
        }
        String command = (String) al.get(0);
        if (equal(command, "cd")) {
            if (1 == al.size()) {
                println("\n-->  Usage:  cd <directory_name>");
                return true;
            }
            String dir = (String) al.get(1);
            int dir_last_index = dir.length() - 1;
            if (dir_last_index != 0 && dir.lastIndexOf('/') == dir_last_index) {
                dir = dir.substring(0, dir_last_index);
            }
            String new_dir = null;
            if (equal(dir, "..")) {
                if (IJ.isWindows()) {
                    if (equal(dir, current_root_dir + file_separator)) {
                        println("\n-->  Such directory doesn't make sense.");
                        return true;
                    }
                } else {
                    if (equal(dir, current_root_dir)) {
                        println("\n-->  Such directory doesn't make sense.");
                        return true;
                    }
                }
                int last_slash = user_dir.lastIndexOf("/");
                new_dir = user_dir.substring(0, last_slash);
                if (-1 == new_dir.indexOf('/')) {
                    new_dir += "/";
                }
            } else if (IJ.isWindows() && 4 < dir.length() && dir.startsWith(current_root_dir + file_separator + "..")) {
                println("\n-->  Such directory doesn't make sense.");
                return true;
            } else if (2 < dir.length() && dir.startsWith(current_root_dir + "..")) {
                println("\n-->  Such directory doesn't make sense.");
                return true;
            } else if (-1 != dir.indexOf("..")) {
                String target_dir = null;
                if (dir.startsWith("/") || 1 == dir.indexOf(":/")) {
                    target_dir = dir;
                } else {
                    target_dir = user_dir + file_separator + dir;
                }
                int two_points = target_dir.indexOf("..");
                while (-1 != two_points) {
                    String temp = target_dir.substring(0, two_points - 1);
                    int ending_slash = temp.lastIndexOf('/');
                    String parent_dir = temp.substring(0, ending_slash);
                    String trailing_stuff = "";
                    if (two_points + 3 < target_dir.length()) {
                        trailing_stuff = file_separator + target_dir.substring(two_points + 3);
                    }
                    target_dir = parent_dir + trailing_stuff;
                    two_points = target_dir.indexOf("..");
                }
                new_dir = target_dir;
            } else if (equal(dir, ".")) {
                return true;
            } else if (dir.startsWith("/") || 1 == dir.indexOf(":/")) {
                new_dir = dir;
            } else {
                new_dir = user_dir + file_separator + dir;
            }
            File f_new_dir = new File(new_dir);
            if (f_new_dir.exists() && f_new_dir.isDirectory()) {
                user_dir = new_dir;
            } else {
                println("\n-->  No such directory.");
                return true;
            }
            if (IJ.isWindows()) {
                current_root_dir = user_dir.substring(0, 2);
            } else {
                current_root_dir = "/";
            }
            String dir_name = user_dir;
            if (-1 == user_dir.lastIndexOf('/')) {
                dir_name += "/";
            }
            println("\n-->  changed directory to: " + dir_name);
            return true;
        } else if (equal(command, "pwd")) {
            println("\n-->  current directory:");
            String dir_name = user_dir;
            if (-1 == user_dir.lastIndexOf('/')) {
                dir_name += "/";
            }
            println("--> " + dir_name);
            return true;
        } else if (equal(command, "lsi")) {
            File f = new File(user_dir);
            String[] image_name;
            if (2 == al.size()) {
                image_name = f.list(new ImageFileFilter((String) al.get(1)));
            } else {
                image_name = f.list(new ImageFileFilter());
            }
            if (0 < image_name.length) {
                println("-->  Images in " + user_dir + " :");
                char space = ' ';
                for (int im = 0; im < image_name.length; im++) {
                    StringBuffer data = new StringBuffer();
                    String path = user_dir + file_separator + image_name[im];
                    data.append("\n-->  " + new File(path).length() / 1000.0 + " KB");
                    while (data.length() < 30) {
                        data.append(space);
                    }
                    data.append(image_name[im]);
                    screen.append(data.toString());
                }
            } else {
                screen.append(l + "-->  No [such] images in " + user_dir);
            }
            println("");
            return true;
        } else if (equal(command, "lsd")) {
            println("-->  Directories in " + user_dir + " :");
            File f = new File(user_dir);
            File[] all;
            if (2 == al.size()) {
                all = f.listFiles(new CustomFileFilter((String) al.get(1)));
            } else {
                all = f.listFiles();
            }
            boolean no_dirs = true;
            for (int i = 0; i < all.length; i++) {
                if (all[i].isDirectory()) {
                    screen.append("\n-->  \t" + all[i].getName() + file_separator);
                    no_dirs = false;
                }
            }
            if (no_dirs) {
                screen.append("\n-->  There are no [such] directories in " + user_dir);
            }
            println("");
            return true;
        } else if (equal(command, "ls")) {
            String dir = user_dir;
            if (-1 == user_dir.lastIndexOf('/')) {
                dir += "/";
            }
            println("-->  Files in " + dir + " :");
            File f = new File(dir);
            String[] file_name;
            if (2 == al.size()) {
                file_name = f.list(new CustomFileFilter((String) al.get(1)));
            } else {
                file_name = f.list();
            }
            if (0 == file_name.length) {
                println("-->  No [such] file/s.");
                return true;
            }
            String slash = "/";
            String nothing = "";
            char space = ' ';
            for (int im = 0; im < file_name.length; im++) {
                StringBuffer data = new StringBuffer();
                String path = user_dir + file_separator + file_name[im];
                File file = new File(path);
                if (file.isDirectory()) {
                    data.append("\n-->                          ");
                } else {
                    data.append("\n-->  " + file.length() / 1000.0 + " KB");
                    while (data.length() < 30) {
                        data.append(space);
                    }
                }
                data.append(file_name[im]);
                if (file.isDirectory()) {
                    data.append(slash);
                }
                screen.append(data.toString());
            }
            println("");
            return true;
        } else if (equal(command, "record")) {
            if (1 == al.size()) {
                println("-->  A name must be provided: 'record macroname'");
                return true;
            }
            MacroRecord.setRecording(true);
            String macro_name = (String) al.get(1);
            MacroRecord.makeNew(macro_name);
            println("-->  Recording to: " + macro_name);
            return true;
        } else if (equal(command, "stop")) {
            if (MacroRecord.isRecording()) {
                MacroRecord.setRecording(false);
                println("\n-->  Finished recording.");
            } else {
                println("\n-->  Nothing is being recorded.");
            }
            return true;
        } else if (equal(command, "exec")) {
            String the_macro = null;
            if (1 == al.size()) {
                the_macro = MacroRecord.getCurrentCode();
            } else {
                the_macro = MacroRecord.getCode((String) al.get(1));
            }
            if (the_macro != null) {
                println("\n-->  Executing" + ((al.size() > 1) ? " " + MacroRecord.autoCompleteName((String) al.get(1)) : MacroRecord.getCurrentName()) + ".");
                println(the_macro);
                execMacro(the_macro);
            } else if (1 < al.size()) {
                String[] the_macro2 = new String[2];
                if ((the_macro2 = findMacro((String) al.get(1))) != null) {
                    println("-->  Executing " + the_macro2[0]);
                    println(the_macro2[1]);
                    execMacro(the_macro2[1]);
                } else {
                    println("\n-->  No such macro: " + (String) al.get(1));
                }
            } else {
                println("n-->  No macros recorded.");
            }
            return true;
        } else if (equal(command, "list")) {
            String[] macro_name = MacroRecord.getList();
            if (0 == macro_name.length) {
                println("\n-->  Zero recorded macros.");
                return true;
            }
            println("\n-->  Recorded macros:");
            for (int i = 0; i < macro_name.length; i++) {
                println("\n-->  \t" + macro_name[i]);
            }
            println("\n");
            return true;
        } else if (equal(command, "save")) {
            if (1 == al.size()) {
                println("\n-->  A macro name must be specified.");
                return true;
            }
            MacroRecord mcr = MacroRecord.find((String) al.get(1));
            if (null == mcr) {
                println("\n-->  No recorded macro named " + (String) al.get(1));
                return true;
            }
            String macro_code = mcr.getCodeForSystem();
            saveMacro(macro_code);
            return true;
        } else if (equal(command, "rm")) {
            if (1 == al.size()) {
                println("\n-->  Usage:  rm <file_name>\n-->    No asterisks allowed.");
                return true;
            }
            File trashcan = new File(trash_can);
            if (!trashcan.exists()) {
                boolean check = trashcan.mkdir();
                if (!check) {
                    println("\n-->  Trash Can does not exist and could not be created.");
                }
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException ie) {
                }
            }
            if (1 == al.size()) {
                println("\n-->  rm : A file name must be specified.");
                return true;
            }
            String file_name = (String) al.get(1);
            if (-1 != file_name.indexOf('*')) {
                println("\n--> Wild cards '*' not allowed in rm command.");
                return true;
            }
            File f;
            if (file_name.startsWith("/")) {
                f = new File(file_name);
            } else {
                file_name = user_dir + file_separator + file_name;
                f = new File(file_name);
            }
            if (f.exists()) {
                if (f.isDirectory()) {
                    String[] list = f.list();
                    if (0 != list.length) {
                        if (2 == list.length) {
                            if (equal(list[0], ".") && equal(list[1], "..")) {
                            } else {
                                println("\n-->  " + file_name + " is a non-empty directory! Deleting stopped");
                                return true;
                            }
                        }
                    }
                }
                String trashed_name = trash_can + file_separator + f.getName();
                File file_trashed = new File(trashed_name);
                int i = 1;
                while (file_trashed.exists()) {
                    trashed_name = trash_can + file_separator + f.getName() + "_" + i;
                    file_trashed = new File(trashed_name);
                    i++;
                }
                if (f.renameTo(file_trashed)) {
                    println("\n-->  " + file_name.substring(file_name.lastIndexOf(file_separator) + 1) + " successfully moved to the trash can.");
                } else {
                    println("\n-->  " + file_name.substring(file_name.lastIndexOf(file_separator)) + " could NOT be trashed.");
                }
            } else {
                println("\n-->  " + file_name + " does not exist!");
            }
            return true;
        } else if (equal(command, "mkdir")) {
            if (1 == al.size()) {
                println("\n-->  Usage : mkdir <new_dir_name>");
                return true;
            }
            File f;
            String dir_name = (String) al.get(1);
            if (dir_name.startsWith("/")) {
                f = new File(dir_name);
            } else {
                dir_name = user_dir + file_separator + dir_name;
                f = new File(dir_name);
            }
            if (f.exists()) {
                println("\n-->  Directory " + dir_name + " already exists!");
            } else {
                if (f.mkdir()) {
                    println("\n-->  Directory " + dir_name + " sucessfully created");
                } else {
                    println("\n-->  Could NOT create the directory!");
                }
            }
            return true;
        } else if (equal(command, "magic")) {
            magic = !magic;
            println("\n-->  magic is " + (magic ? "ON" : "OFF"));
            return true;
        } else if (equal(command, "erase")) {
            if (al.size() < 2) {
                MacroRecord.eraseLinesFromCurrent(1);
                return true;
            }
            if (equal((String) al.get(1), "-l") && 2 == al.size()) {
                println("\n--> Line number not specified!");
                return true;
            }
            if (equal((String) al.get(1), "-l")) {
                try {
                    int line = Integer.parseInt((String) al.get(2));
                    if (MacroRecord.eraseLineFromCurrent(line)) {
                        println("\n-->  line " + line + " erased.");
                    } else {
                        println("\n--> line " + line + " out of range.\n");
                    }
                } catch (Exception e) {
                    println("\n--> Supplied argument is not a valid number.\n");
                }
            } else {
                try {
                    int num_lines = Integer.parseInt((String) al.get(1));
                    int erased_lines = MacroRecord.eraseLinesFromCurrent(num_lines);
                    if (-1 == erased_lines) {
                        println("\n-->  All lines erased.");
                    } else if (-2 == erased_lines) {
                        println("\n-->  No recorded macro to edit.");
                    } else {
                        println("\n-->  " + erased_lines + " lines erased.");
                    }
                } catch (Exception e) {
                    println("\n--> Supplied argument is not a valid number.");
                }
            }
            return true;
        } else if (equal(command, "front")) {
            boolean activate = false;
            if (al.size() < 2) {
                String[] list = MacroRecord.getList();
                if (list.length == 0) {
                    println("\n-->  No recorded macro.");
                } else {
                    println("\n-->  Front macro is " + MacroRecord.getCurrentName() + " and it is " + ((MacroRecord.isRecording()) ? "" : "not") + " being edited.");
                }
            } else {
                activate = MacroRecord.setActive((String) al.get(1));
            }
            if (activate) {
                println("\n-->  Now recording on: " + (String) al.get(1) + l);
            }
            return true;
        } else if (equal(command, "view")) {
            MacroRecord mc;
            if (al.size() == 1) {
                mc = MacroRecord.getCurrent();
            } else {
                mc = MacroRecord.find((String) al.get(1));
            }
            if (null != mc) {
                println("\n-->  Macro : " + mc.getName());
                println("\n" + mc.getCode());
            } else if (1 < al.size()) {
                String[] a_macro = null;
                if ((a_macro = findMacro((String) al.get(1))) != null) {
                    println("\n-->  Macro : " + a_macro[0]);
                    println(a_macro[1]);
                } else {
                    println("\n-->  No such macro: " + (String) al.get(1));
                }
            } else {
                println("\n-->  No macro recorded or no such macro.");
            }
            return true;
        } else if (equal(command, "help")) {
            println("\n-->  Command line interface for ImageJ");
            println("-->  -- Albert Cardona 2004 --");
            println("-->  Just type in any ImageJ macro code and it will be executed after pushing intro.");
            println("-->  Multiline macro commands can be typed by adding an ending \\");
            println("-->  Unix-like basic shell functions available.");
            println("-->  TAB key expands file names and macro functions names.");
            println("-->  UP and DOWN arrows bring back entered commands.");
            println("-->  Mouse selecting text brings contextual menu.");
            println("-->  \n-->  Macro Commands:");
            println("-->    record <macro_name> : start recording a macro.");
            println("-->    stop : stop the recording.");
            println("-->    view [<macro_name>] : print the macro code from macro macro_name without executing it, or from the front macro.\n-->       An attempt will be made to match uncompleted names\n-->       from the recorded list, the current directory, and the ImageJ macros directory.");
            println("-->    list : list all recorded macros.");
            println("-->    save <macro_name>: save recorded macro to a file.");
            println("-->    exec [<macro_name>] : execute a recorded macro macro_name, or the front macro.\n-->       An attempt will be made to match uncompleted names\n-->       from the recorded list, the current directory, and the ImageJ macros directory.");
            println("-->    front [<macro_name>] : start editing macro macro_name, or just print who is the front macro.");
            println("-->    erase [-l line_number]|[num_lines] : erase line line_number or erase num_lines starting from the end, or just the last line.");
            println("-->    toggle_edit : enable/disable direct screen editing.");
            println("-->    magic : toggle magic ON/OFF. When on, the program attempts to guess several things \n-->       and transform the input. Example: dc invert  -> doCommand(\"Invert\") ,\n-->       or makeRectangle 10,10,30,40 -> makeRectangle(10,10,30,40)");
            println("-->    doc [<url>]: show ImageJ website macro documentation pages, or a given url.");
            println("-->  \n-->  Shell-like Commands:");
            println("-->    open <image_file/s>|<directory> : open an image file or a list of space-separated image names or paths.\n-->      Accepts wildcard (*) at start, end, or both.\n-->      Will print the correct macro code to open the image.\n-->      Alternatively, it will open as a stack all images in the specified directory.\n-->      Without arguments, opens current directory images as a stack.");
            println("-->    ls [<file_name>]: list all files in working directory.");
            println("-->    lsi [<file_name>]: list images in working directory.");
            println("-->    lsd [<file_name>]: list directories in the working directory.");
            println("-->    pwd : print working directory.");
            println("-->    cd <directory> : change directory.");
            println("-->    rm <file_name> : move file_name to the trash can located at this plugin folder.");
            println("-->    empty_trash : empty the CLI Trash Can.");
            println("-->    clear : clear screen.");
            println("-->    screenshot [window_name [target_file_name [delay_in_seconds]]] : idem.");
            println("-->    show [directory [file [time]]]: slide show on current or specified directory,\n-->      of files <file> (accepts *) and every <time> (in seconds).");
            println("-->  \n-->  Contextual Menu:");
            println("-->    Select any piece of text from the screen.\n-->    Lines starting with '-->  ' will be ignored,\n-->    as well as the starting '> ' and ending '\\' characters.");
            println("-->      Execute Selection : idem");
            println("-->      Record : make a new macro from selection.");
            println("-->      Copy : copy selection to system paste buffer.");
            println("-->      Save Selection : open file dialog to save selection as a macro text file.");
            println("-->      Save & Exec Selection : idem.");
            println("-->  ");
            return true;
        } else if (equal(command, "empty_trash")) {
            File trash = new File(trash_can);
            File[] f = trash.listFiles();
            int failed = 0;
            for (int i = 0; i < f.length; i++) {
                String file_name = f[i].getName();
                boolean check = false;
                if (!equal(file_name, ".") && !equal(file_name, "..")) {
                    check = f[i].delete();
                }
                if (false == check) {
                    println("\n-->  Could not delete file " + file_name);
                    failed++;
                }
            }
            if (failed == 0) {
                println("\n-->  Trash can successfully emptied.");
            } else {
                println("\n-->  Some files may have not been deleted.");
            }
            return true;
        } else if (equal(command, "clear")) {
            screen.setText("");
            return true;
        } else if (equal(command, "screenshot")) {
            Screenshot s = null;
            if (1 == al.size()) {
                s = new Screenshot(null, 0, user_dir, null);
            } else if (2 == al.size()) {
                java.awt.Frame frame = WindowManager.getFrame((String) al.get(1));
                if (null != frame) {
                    s = new Screenshot(frame, 0, user_dir, null);
                } else {
                    println("\n-->  No such window: " + (String) al.get(1));
                }
            } else if (3 == al.size()) {
                java.awt.Frame frame = WindowManager.getFrame((String) al.get(1));
                if (null != frame) {
                    s = new Screenshot(frame, 0, user_dir, (String) al.get(2));
                } else {
                    println("\n-->  No such window: " + (String) al.get(1));
                }
            } else if (4 == al.size()) {
                java.awt.Frame frame = WindowManager.getFrame((String) al.get(1));
                if (null != frame) {
                    try {
                        s = new Screenshot(frame, Integer.parseInt((String) al.get(3)), user_dir, (String) al.get(2));
                    } catch (NumberFormatException nfe) {
                        println("\n-->  Wrong number format for seconds. Stopping.");
                    }
                } else {
                    println("\n-->  No such window: " + (String) al.get(1));
                }
            }
            s.setOut(screen);
            new Thread(s).start();
            println(s.getReport());
            return true;
        } else if (equal(command, "mv")) {
            try {
                if (2 < al.size()) {
                    String file_name = (String) al.get(1);
                    if (0 == file_name.indexOf('/') || 1 == file_name.indexOf(':')) {
                    } else {
                        file_name = user_dir + file_separator + file_name;
                    }
                    String new_file_name = fixDir((String) al.get(2));
                    if (null == new_file_name) {
                        println("\n-->  Incorrect target file_name or dir. File/s not moved.");
                        return true;
                    }
                    if (-1 != new_file_name.indexOf('*')) {
                        println("\n-->  No wildcards allowed in target file_name or dir");
                        return true;
                    }
                    File new_file = new File(new_file_name);
                    String files_dir = file_name.substring(0, file_name.lastIndexOf('/'));
                    File f_files_dir = new File(files_dir);
                    String[] file_names = f_files_dir.list(new CustomFileFilter(file_name.substring(file_name.lastIndexOf('/') + 1)));
                    if (0 == file_names.length) {
                        println("\n-->  No such file/s: \n-->  " + file_name);
                        return true;
                    }
                    if (new_file.exists() && new_file.isDirectory()) {
                        for (int i = 0; i < file_names.length; i++) {
                            File source_file = new File(files_dir + file_separator + file_names[i]);
                            String target_file_name = new_file_name + file_separator + file_names[i];
                            File target_file = new File(target_file_name);
                            if (target_file.exists()) {
                                println("\n-->  A file named \n-->  " + target_file.getName() + "\n-->  already exists in target directory \n-->  " + new_file_name);
                                continue;
                            }
                            boolean check = source_file.renameTo(target_file);
                            if (check) {
                                println("\n-->  File successfully moved to:\n-->  " + target_file_name);
                            } else {
                                println("\n-->  Could not move the file \n-->  " + target_file_name + "\n-->       into directory " + new_file_name);
                            }
                        }
                    } else if (1 == file_names.length && !new_file.isDirectory()) {
                        if (new_file.exists()) {
                            println("\n-->  A file named " + new_file.getName() + " already exists!\n-->  Not moving the file " + file_names[0]);
                            return true;
                        } else {
                            File source_file = new File(files_dir + file_separator + file_names[0]);
                            boolean check = source_file.renameTo(new_file);
                            if (check) {
                                println("\n-->  File successfully moved to:\n-->  " + new_file_name);
                            } else {
                                println("\n-->  Could not move the file \n-->  " + file_names[0] + "\n--> to file " + new_file_name);
                            }
                        }
                    }
                    return true;
                } else {
                    println("\n-->  Usage: mv <file_name> <dir | new_file_name>");
                    return true;
                }
            } catch (Exception e) {
                IJ.write("Some error ocurred:\n" + new TraceError(e));
            }
            return true;
        } else if (equal(command, "doc")) {
            String url = "http://rsb.info.nih.gov/ij/developer/macro/macros.html";
            if (al.size() == 2) {
                url = (String) al.get(1);
            }
            println("\n-->  Opening " + url);
            try {
                JEditorPane jep = new JEditorPane(url);
                jep.setPreferredSize(new Dimension(500, 600));
                jep.setEditable(false);
                jep.addHyperlinkListener(new HyperlinkAdapter(jep));
                JScrollPane scroll = new JScrollPane(jep);
                scroll.setPreferredSize(new Dimension(500, 600));
                JFrame f = new JFrame("Macro Functions List");
                f.setSize(new Dimension(500, 600));
                f.getContentPane().add(scroll);
                f.pack();
                f.show();
            } catch (Exception ioe) {
                println("\n-->  Dictionary could not be found at url:\n-->  " + url);
            }
            return true;
        } else if (equal(command, "open")) {
            if (al.size() < 2) {
                return false;
            }
            String dir_path = fixDir((String) al.get(1));
            File dir = new File(dir_path);
            if (!(dir.exists() && dir.isDirectory())) {
                return false;
            }
            OpenDirectory od = new OpenDirectory(dir_path, OpenDirectory.STACK);
            println("\n-->  " + od.getMessage());
            return true;
        } else if (equal(command, "show")) {
            String the_macro = null;
            if (al.size() < 2) {
                the_macro = "run(\"Slide Show\", \"folder=" + user_dir + " file=* time=4\")\n";
            } else {
                the_macro = "run(\"Slide Show\", \"folder=" + fixDir((String) al.get(1));
                if (al.size() > 2) {
                    the_macro += " file=" + (String) al.get(2);
                }
                if (al.size() > 3) {
                    the_macro += " time=" + (String) al.get(3);
                }
                the_macro += "\")\n";
            }
            println(the_macro);
            execMacro(the_macro);
            return true;
        }
        return false;
    }

    class HyperlinkAdapter implements HyperlinkListener {

        JEditorPane jep;

        HyperlinkAdapter(JEditorPane jep) {
            this.jep = jep;
        }

        public void hyperlinkUpdate(HyperlinkEvent he) {
            if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    jep.setPage(he.getURL());
                } catch (Exception e) {
                    IJ.showMessage("Can't follow link.");
                }
            }
        }
    }

    boolean equal(String a, String b) {
        return a.toLowerCase().hashCode() == b.toLowerCase().hashCode();
    }

    String fixDir(String dir) {
        String fixed_dir = dir;
        if (equal(dir, "..")) {
            if (equal(user_dir, current_root_dir)) {
                println("\n-->  Such directory doesn't make sense.");
                return null;
            }
            int last_slash = user_dir.lastIndexOf("/");
            fixed_dir = user_dir.substring(0, last_slash);
            if (-1 == fixed_dir.indexOf('/')) {
                fixed_dir += "/";
            }
        } else if (IJ.isWindows() && 4 < dir.length() && dir.startsWith(current_root_dir + file_separator + "..")) {
            println("\n-->  Such directory doesn't make sense.");
            return null;
        } else if (2 < dir.length() && dir.startsWith(current_root_dir + "..")) {
            println("\n-->  Such directory doesn't make sense.");
            return null;
        } else if (-1 != dir.indexOf("..")) {
            String target_dir = null;
            if (dir.startsWith("/") || 1 == dir.indexOf(":/")) {
                target_dir = dir;
            } else {
                target_dir = user_dir + file_separator + dir;
            }
            int two_points = target_dir.indexOf("..");
            while (-1 != two_points) {
                String temp = target_dir.substring(0, two_points - 1);
                int ending_slash = temp.lastIndexOf('/');
                String parent_dir = temp.substring(0, ending_slash);
                String trailing_stuff = "";
                if (two_points + 3 < target_dir.length()) {
                    trailing_stuff = file_separator + target_dir.substring(two_points + 3);
                }
                target_dir = parent_dir + trailing_stuff;
                two_points = target_dir.indexOf("..");
            }
            fixed_dir = target_dir;
            return fixed_dir;
        } else if (equal(dir, ".")) {
            return user_dir;
        } else if (dir.startsWith("/") || 1 == dir.indexOf(":/")) {
            fixed_dir = dir;
        } else {
            if (equal(user_dir, current_root_dir)) {
                if (IJ.isWindows()) {
                    fixed_dir = user_dir + file_separator + dir;
                } else {
                    fixed_dir = user_dir + dir;
                }
            } else {
                fixed_dir = user_dir + file_separator + dir;
            }
        }
        return fixed_dir;
    }

    String[] findMacro(String name) {
        String[] macro = null;
        if (null == macro) {
            macro = findMacro(user_dir, name);
        }
        if (null == macro) {
            macro = findMacro(dir_macros, name);
        }
        return macro;
    }

    String[] findMacro(String dir, String name) {
        String[] macro = new String[2];
        File f_dir_macros = new File(dir);
        String[] names = f_dir_macros.list();
        for (int i = 0; i < names.length; i++) {
            if (name.length() <= names[i].length() && names[i].startsWith(name)) {
                try {
                    String f_dir_macros_canonical_path = fixWindowsPath(f_dir_macros.getCanonicalPath());
                    if (IJ.isWindows()) {
                        f_dir_macros_canonical_path.replace('\\', '/');
                    }
                    macro[0] = names[i];
                    macro[1] = readFile(f_dir_macros_canonical_path + file_separator + names[i]);
                } catch (Exception e) {
                    println("\n-->  Macro file " + name + " or similar could not be found or read in directory " + f_dir_macros.getName());
                }
                return macro;
            }
        }
        return null;
    }

    class CustomMouseAdapter extends MouseAdapter {

        public void mouseReleased(MouseEvent me) {
            selection = screen.getSelectedText();
            if (null != selection && 0 < selection.length()) {
                popup_menu.show(screen, me.getX(), me.getY());
            }
        }
    }

    class PopupListener implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            String action = ae.getActionCommand();
            try {
                String macro = getCleanLinesFromSelection();
                if (null == macro || 0 == macro.length()) {
                    return;
                }
                if (equal(action, "Execute Selection")) {
                    execMacro(macro);
                } else if (equal(action, "Record")) {
                    String macro_name = promptForName(null, "Macro name: ");
                    while (MacroRecord.exists(macro_name)) {
                        macro_name = promptForName("Macro " + macro_name + " exists!", "Macro name: ");
                    }
                    if (null != macro_name) {
                        new MacroRecord(macro_name, macro);
                        println("\n-->  Recorded new macro as " + macro_name);
                    }
                } else if (equal(action, "Copy")) {
                    Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Transferable transfer = new StringSelection(macro);
                    cb.setContents(transfer, (ClipboardOwner) transfer);
                } else if (equal(action, "Save Selection")) {
                    saveMacro(macro);
                } else if (equal(action, "Save & Exec Selection")) {
                    saveMacro(macro);
                    execMacro(macro);
                }
            } catch (Exception e) {
                IJ.write("Problems in popup menu actions: " + new TraceError(e));
            }
        }
    }

    String promptForName(String extra, String label) {
        GenericDialog gd = new GenericDialog("Name it");
        if (null != extra) {
            gd.addMessage(extra);
        }
        gd.addStringField(label, "");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return null;
        }
        return gd.getNextString();
    }

    void execMacro(String macro) {
        String macrop = macro;
        if (0 != macro.length()) {
            try {
                if (new ImageFileFilter().accept(new File(user_dir), macro)) {
                    println(macrop);
                }
                new Interpreter().run(macrop);
            } catch (Exception e) {
                valid_lines.set(valid_lines.size() - 1, false);
                println("\n-->  macro not executable or canceled.\n");
                if (!magic) {
                    int ispace = macro.indexOf(' ');
                    if (-1 != ispace || ispace < macro.indexOf('(')) {
                        println("\n-->    Try to toggle magic ON by typing:  magic");
                    }
                }
                allow_print = false;
            }
            screen.setCaretPosition(screen.getText().length());
        }
    }

    void saveMacro(String macro) {
        FileDialog fd = new FileDialog(window, "Save", FileDialog.SAVE);
        fd.setDirectory(user_dir);
        fd.show();
        if (null == fd.getFile()) return;
        String file_path = fixWindowsPath(fd.getDirectory()) + fd.getFile();
        if (file_path.length() > 3 && !file_path.endsWith(".txt")) {
            file_path += ".txt";
        }
        boolean check = saveFile(file_path, macro);
        if (check) {
            println("\n-->  Macro saved as " + file_path);
        } else {
            println("\n-->  Macro NOT saved.");
        }
        screen.setCaretPosition(screen.getText().length());
    }

    boolean saveFile(String file_path, String file_contents) {
        try {
            File f = new File(file_path);
            int i = 1;
            int dot = file_path.lastIndexOf(".");
            String extension = file_path.substring(dot);
            while (f.exists()) {
                file_path = file_path.substring(0, dot) + "_" + i + extension;
                f = new File(file_path);
                i++;
            }
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f), file_contents.length()));
            dos.writeBytes(file_contents);
            dos.flush();
            return true;
        } catch (SecurityException se) {
            IJ.write(se + "\nError at d.o.s. SE.\n" + new TraceError(se));
        } catch (IOException ioe) {
            IJ.write(ioe + "\nError at d.o.s. IOE.\n" + new TraceError(ioe));
        }
        return false;
    }

    String readFile(String file_path) throws FileNotFoundException {
        File f = new File(file_path);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        String one_line = "";
        ArrayList macro_code = new ArrayList();
        do {
            try {
                one_line = br.readLine();
            } catch (Exception e) {
                println("\n-->  Error when reading file " + file_path);
            }
            if (one_line != null) {
                macro_code.add(one_line + l);
            }
        } while (one_line != null);
        try {
            br.close();
        } catch (Exception e) {
            println("\n-->  Error when closing reading buffer for " + file_path);
        }
        MacroRecord macro = new MacroRecord(f.getName(), macro_code);
        return macro.getCode();
    }

    String getCleanLinesFromSelection() {
        try {
            ArrayList ar = new ArrayList();
            if (-1 == selection.indexOf(l)) {
                ar.add(selection);
            } else {
                int start = selection.indexOf(l);
                int end = selection.indexOf(l, start + 1);
                ar.add(selection.substring(0, start));
                while (-1 != end) {
                    ar.add(selection.substring(start + 1, end));
                    start = end;
                    end = selection.indexOf(l, end + 1);
                }
                String last = selection.substring(start + 1);
                if (0 < last.length()) ar.add(last);
            }
            String macro = "";
            String newline = System.getProperty("line.separator");
            for (int i = 0; i < ar.size(); i++) {
                String line = (String) ar.get(i);
                if (0 == line.length()) {
                    continue;
                }
                if (line.length() > 4 && line.startsWith("-->  ")) {
                    continue;
                }
                if (line.startsWith("> ")) {
                    line = line.substring(2);
                }
                if (line.endsWith("\\")) {
                    line = line.substring(0, line.length() - 1);
                }
                macro += line + newline;
            }
            return macro;
        } catch (Exception e) {
            IJ.write("Problems at cleaning lines:\n" + new TraceError(e));
        }
        return null;
    }
}
