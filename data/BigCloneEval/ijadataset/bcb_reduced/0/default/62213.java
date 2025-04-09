import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.io.*;
import javax.swing.text.*;

public class Notepad extends JFrame implements ActionListener {

    JMenuBar menuBar;

    JMenu menu;

    JMenuItem save, open, copy, paste;

    JTextArea field;

    File file = new File("file.text");

    public Notepad(String title) {
        super(title);
        Container contentpane = getContentPane();
        contentpane.setLayout(new BorderLayout());
        this.setSize(640, 480);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        field = new JTextArea();
        field.setDragEnabled(true);
        field.setBackground(Color.GRAY);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        contentpane.add(field, "Center");
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menu = new JMenu("File");
        open = new JMenuItem("Open");
        save = new JMenuItem("Save");
        copy = new JMenuItem("Copy");
        paste = new JMenuItem("Paste");
        menu.add(save);
        menu.add(open);
        menu.add(copy);
        menu.add(paste);
        menuBar.add(menu);
        save.addActionListener(this);
        open.addActionListener(this);
        copy.addActionListener(this);
        paste.addActionListener(this);
    }

    public void actionPerformed(ActionEvent ae) {
        String cmd = (String) ae.getActionCommand();
        if (cmd.equals("Save")) save(); else if (cmd.equals("Open")) open(); else if (cmd.equals("Copy")) copy(); else if (cmd.equals("Paste")) paste();
    }

    public void paste() {
        Clipboard cb = this.getToolkit().getSystemClipboard();
        Transferable tr = cb.getContents(this);
        String s = null;
        try {
            s = (String) tr.getTransferData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(Notepad.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Notepad.class.getName()).log(Level.SEVERE, null, ex);
        }
        int start = field.getSelectionStart();
        int end = field.getSelectionEnd();
        field.replaceRange(s, start, end);
    }

    public void copy() {
        String s = field.getSelectedText();
        int start = field.getSelectionStart();
        int end = field.getSelectionEnd();
        StringSelection ss = new StringSelection(s);
        this.getToolkit().getSystemClipboard().setContents(ss, ss);
    }

    public void open() {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            String content = readFile(file);
            field.setText(content);
        }
    }

    public String readFile(File file) {
        StringBuffer strbuff;
        String content = null;
        String lnString;
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            strbuff = new StringBuffer();
            while ((lnString = br.readLine()) != null) {
                strbuff.append(lnString + "\n");
            }
            fr.close();
            content = strbuff.toString();
            String name = file.getName();
            if (name != null) {
                int extensionIndex = name.lastIndexOf('.');
                setTitle(name.substring(0, extensionIndex));
            }
        } catch (IOException e) {
            return null;
        }
        return content;
    }

    public boolean save() {
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
            if (file.exists()) {
                int response = JOptionPane.showConfirmDialog(null, "File already exists. Do you want to continue?", "Overwrit confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.CANCEL_OPTION) return false;
            }
            String fileName = file.getName();
            if (fileName != null) {
                int extensionIndex = fileName.lastIndexOf('.');
                setTitle(fileName.substring(0, extensionIndex));
            }
            return writeFile(file, field.getText());
        }
        return false;
    }

    public static boolean writeFile(File file, String content) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.print(content);
            pw.flush();
            pw.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
