package drcl.ruv;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import drcl.comp.*;

/**
 * Implemnets a terminal.
 */
public class Dterm extends Term implements ActionListener, Wrapper {

    int ScrWidth = 80, ScrHeight = 24;

    JFrame frame;

    JScrollPane scrollPane;

    JMenuBar mBar;

    JMenu mFile, mEdit, mHelp;

    JTextArea ta, tf;

    JFileChooser chooser;

    JMenuItem lineWrap, outputEnable;

    File currentFile = null;

    MyKeyAdapter tt = new MyKeyAdapter();

    public Dterm() {
        super();
        setID("dterm");
        init();
    }

    public Dterm(String id_) {
        super(id_);
        init();
    }

    public String info() {
        return super.info() + "currentFile: " + currentFile + "\n";
    }

    void init() {
        Thread backgroundTask_ = new Thread() {

            public void run() {
                synchronized (Dterm.this) {
                    chooser = new JFileChooser();
                    javax.swing.filechooser.FileFilter filter_ = new javax.swing.filechooser.FileFilter() {

                        public boolean accept(File f_) {
                            return f_.isDirectory() || f_.getName().endsWith(".tcl");
                        }

                        public String getDescription() {
                            return "TCL Script (*.tcl)";
                        }
                    };
                    chooser.setFileFilter(filter_);
                    Dterm.this.notify();
                }
            }
        };
        synchronized (Dterm.this) {
            backgroundTask_.setPriority(Thread.MIN_PRIORITY);
            backgroundTask_.start();
            frame = new JFrame(title);
            frame.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e_) {
                    write(Shell.COMMAND_EXIT + "\n");
                    exit();
                }

                public void windowActivated(WindowEvent e_) {
                    isfocused = true;
                }

                public void windowDeactivated(WindowEvent e_) {
                    isfocused = false;
                }
            });
            mBar = new JMenuBar();
            mFile = new JMenu("File");
            mFile.setMnemonic(KeyEvent.VK_F);
            JMenuItem mi_ = new JMenuItem("Open...");
            mi_.setMnemonic(KeyEvent.VK_O);
            mi_.addActionListener(this);
            mFile.add(mi_);
            mFile.addSeparator();
            mi_ = new JMenuItem("Save");
            mi_.setMnemonic(KeyEvent.VK_S);
            mi_.addActionListener(this);
            mFile.add(mi_);
            mi_ = new JMenuItem("Save As...");
            mi_.setMnemonic(KeyEvent.VK_V);
            mi_.addActionListener(this);
            mFile.add(mi_);
            mi_ = new JMenuItem("Append To...");
            mi_.setMnemonic(KeyEvent.VK_A);
            mi_.addActionListener(this);
            mFile.add(mi_);
            mFile.addSeparator();
            mi_ = new JMenuItem("Exit");
            mi_.setMnemonic(KeyEvent.VK_X);
            mi_.addActionListener(this);
            mFile.add(mi_);
            mBar.add(mFile);
            mEdit = new JMenu("Edit");
            mEdit.setMnemonic(KeyEvent.VK_E);
            lineWrap = new JMenuItem("Set Line Wrap");
            lineWrap.setMnemonic(KeyEvent.VK_L);
            lineWrap.addActionListener(this);
            mEdit.add(lineWrap);
            outputEnable = new JMenuItem("Disable Terminal Display");
            outputEnable.setMnemonic(KeyEvent.VK_T);
            outputEnable.addActionListener(this);
            mEdit.add(outputEnable);
            mi_ = new JMenuItem("Copy");
            mi_.setMnemonic(KeyEvent.VK_C);
            mi_.addActionListener(this);
            mEdit.add(mi_);
            mi_ = new JMenuItem("Paste");
            mi_.setMnemonic(KeyEvent.VK_P);
            mi_.addActionListener(this);
            mEdit.add(mi_);
            mBar.add(mEdit);
            ta = new JTextArea(ScrHeight, ScrWidth);
            ta.setFont(new Font("Courier", Font.PLAIN, 12));
            tf = new JTextArea(1, ScrWidth);
            tf.setBorder(BorderFactory.createLineBorder(Color.gray));
            scrollPane = new JScrollPane(ta, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            frame.getContentPane().add(tf, BorderLayout.SOUTH);
            if (isTerminalDisplayEnabled()) frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
            frame.setJMenuBar(mBar);
            frame.pack();
            ta.addKeyListener(tt);
            tf.addKeyListener(tt);
            tf.requestFocus();
        }
    }

    public void setTerminalDisplayEnabled(boolean enabled_) {
        super.setTerminalDisplayEnabled(enabled_);
        if (isTerminalDisplayEnabled()) {
            frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
            if (frame.isShowing() && ta.getText().length() == 0) ta.append(getPrompt());
        } else frame.getContentPane().remove(scrollPane);
        frame.pack();
        outputEnable.setText(isTerminalDisplayEnabled() ? "Disable Terminal Display" : "Enable Terminal Display");
    }

    public void actionPerformed(ActionEvent evt) {
        Object arg = evt.getSource();
        if (arg == lineWrap) {
            ta.setLineWrap(!ta.getLineWrap());
            lineWrap.setText(ta.getLineWrap() ? "Unset Line Wrap" : "Set Line Wrap");
        } else if (arg == outputEnable) {
            setTerminalDisplayEnabled(!isTerminalDisplayEnabled());
        } else {
            arg = evt.getActionCommand();
            if (arg.equals("Copy")) {
                if (ta.getSelectedText() != null && ta.getSelectedText().length() > 0) {
                    ta.copy();
                } else if (tf.getSelectedText() != null && tf.getSelectedText().length() > 0) {
                    tf.copy();
                }
            } else if (arg.equals("Paste")) {
                tf.paste();
            } else if (arg.equals("Open...")) open(); else if (arg.equals("Save")) save(false); else if (arg.equals("Save...")) save(false); else if (arg.equals("Save As...")) saveas(); else if (arg.equals("Append To...")) save(true); else if (arg.equals("Exit")) {
                write(Shell.COMMAND_EXIT + "\n");
                exit();
            }
        }
    }

    public Object getObject() {
        return ta;
    }

    public void setTitle(String title_) {
        super.setTitle(title_);
        frame.setTitle(title_);
    }

    int maxlength = Integer.MAX_VALUE;

    protected void _write(String msg_) {
        synchronized (ta) {
            ta.append(msg_);
            ta.setCaretPosition(ta.getText().length());
        }
        try {
            if (ta.getLineCount() > maxNumLines) {
                int end_ = ta.getLineEndOffset(ta.getLineCount() - maxNumLines);
                ta.replaceRange(null, 0, end_);
            }
        } catch (javax.swing.text.BadLocationException e) {
            e.printStackTrace();
        }
    }

    /** Displays the terminal. */
    public void show() {
        write(getPrompt());
        frame.setVisible(true);
    }

    /** Hides the display of the terminal. */
    public void hide() {
        frame.setVisible(false);
    }

    /** Minimizes the display of the terminal. */
    public void minimize() {
        frame.setState(java.awt.Frame.ICONIFIED);
    }

    /** Restores the display of the terminal. */
    public void restore() {
        frame.setState(java.awt.Frame.NORMAL);
    }

    boolean isfocused = false;

    public boolean isFocused() {
        return isfocused;
    }

    public void exit() {
        super.exit();
        frame.dispose();
    }

    protected void setCommand(String cmd_, int pos_) {
        tf.setText(cmd_);
        tf.setCaretPosition(pos_);
    }

    String getCommand() {
        return tf.getText();
    }

    int getCommandPosition() {
        return tf.getCaretPosition();
    }

    String getPartialCommand() {
        return tf.getText().substring(0, tf.getCaretPosition());
    }

    boolean _getFile(String title_) {
        synchronized (Dterm.this) {
            try {
                if (chooser == null) Dterm.this.wait();
            } catch (Exception e_) {
                e_.printStackTrace();
            }
        }
        chooser.setCurrentDirectory(currentFile == null ? new File(".") : currentFile.getParentFile());
        if (chooser.showDialog(frame, title_) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            return true;
        } else return false;
    }

    void open() {
        if (_getFile("open")) readfile();
        currentFile = null;
    }

    void save(boolean append_) {
        if (currentFile == null || append_) {
            if (_getFile(append_ ? "Append To" : "Save")) {
                if (!append_) {
                }
                writefile(append_);
                _setTitle();
            }
        } else writefile(append_);
    }

    void saveas() {
        if (_getFile("SaveAs")) {
            writefile(false);
            _setTitle();
        }
    }

    void writefile(boolean append_) {
        try {
            BufferedWriter bw_ = new BufferedWriter(new FileWriter(currentFile.getPath(), append_));
            saveHistory(bw_);
        } catch (Exception e_) {
            write(e_ + "\n");
        }
    }

    void _setTitle() {
        int i = getTitle().indexOf(" - ");
        if (i < 0) setTitle(getTitle() + " - " + currentFile.getName()); else setTitle(getTitle().substring(0, i) + " - " + currentFile.getName());
    }

    void readfile() {
        try {
            evalFile(currentFile.getPath());
        } catch (Exception e_) {
            write(e_ + "\n");
        }
    }

    String partialCmd = null;

    public class MyKeyAdapter extends KeyAdapter {

        public void keyPressed(KeyEvent evt_) {
            int priority_ = Thread.currentThread().getPriority();
            if (priority_ != Thread.MAX_PRIORITY) {
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            }
            switch(evt_.getKeyCode()) {
                case KeyEvent.VK_UP:
                    String cmd_ = getPartialCommand();
                    if (partialCmd == null) partialCmd = cmd_;
                    if (evt_.isControlDown()) partialCmd = "";
                    cmd_ = partialCmd.length() == 0 ? getHistory(-1) : getHistoryUp(partialCmd);
                    if (cmd_ != null) setCommand(cmd_, cmd_.length());
                    evt_.consume();
                    break;
                case KeyEvent.VK_DOWN:
                    cmd_ = getPartialCommand();
                    if (partialCmd == null) partialCmd = cmd_;
                    if (evt_.isControlDown()) partialCmd = "";
                    cmd_ = partialCmd.length() == 0 ? getHistory(+1) : getHistoryDown(partialCmd);
                    if (cmd_ != null) setCommand(cmd_, cmd_.length());
                    evt_.consume();
                    break;
                case KeyEvent.VK_ENTER:
                    partialCmd = null;
                    evt_.consume();
                    cmd_ = getCommand();
                    if (evt_.isControlDown()) {
                        addCmdToHistory(cmd_);
                        setCommand("", 0);
                        write(cmd_ + "\n");
                        diagnoze(cmd_);
                        return;
                    }
                    synchronized (Dterm.this) {
                        if (evalCommand(cmd_, true)) {
                            addCmdToHistory(cmd_);
                            setCommand("", 0);
                        } else RUVOutputManager.SYSTEM_OUT.print(new String(new byte[] { 7 }));
                    }
                    break;
                case KeyEvent.VK_TAB:
                    evt_.consume();
                    cmd_ = getCommand();
                    if (cmd_.trim().length() > 0) autocomplete(cmd_, tf.getCaretPosition()); else setCommand(cmd_ + "    ", cmd_.length() + 4);
                    break;
                case KeyEvent.VK_LEFT:
                    if (tf.getCaretPosition() > 0) tf.setCaretPosition(tf.getCaretPosition() - 1);
                    evt_.consume();
                    break;
                case KeyEvent.VK_RIGHT:
                    if (tf.getCaretPosition() < tf.getText().length()) tf.setCaretPosition(tf.getCaretPosition() + 1);
                    evt_.consume();
                    break;
            }
        }

        boolean reentrance = false;

        public void keyTyped(KeyEvent evt_) {
            if (reentrance && evt_.getSource() == ta) {
                evt_.consume();
                return;
            }
            int c_ = (int) evt_.getKeyChar();
            switch(evt_.getKeyCode()) {
                case KeyEvent.VK_V:
                    if (evt_.isControlDown() && evt_.getSource() == ta) {
                        tf.dispatchEvent(new KeyEvent(tf, evt_.getID(), evt_.getWhen(), evt_.getModifiers(), evt_.getKeyCode(), evt_.getKeyChar()));
                        evt_.consume();
                        break;
                    }
                case KeyEvent.VK_C:
                    if (!evt_.isControlDown()) break; else c_ = KeyEvent.VK_CANCEL;
                default:
                    if (c_ == KeyEvent.VK_CANCEL) {
                        Object source_ = evt_.getSource();
                        String stext_ = source_ == ta ? ta.getSelectedText() : tf.getSelectedText();
                        if (stext_ != null && stext_.length() > 0) return;
                        evt_.consume();
                        write("^C\n");
                        interrupt();
                        partialCmd = null;
                        setCommand("", 0);
                        break;
                    }
                    partialCmd = null;
                    if (!evt_.isConsumed() && !evt_.isControlDown()) {
                        if (evt_.getSource() == ta) {
                            evt_.consume();
                            tf.requestFocus();
                            reentrance = true;
                            tf.dispatchEvent(new KeyEvent(tf, evt_.getID(), evt_.getWhen(), evt_.getModifiers(), evt_.getKeyCode(), evt_.getKeyChar()));
                            reentrance = false;
                        } else {
                        }
                    }
            }
        }
    }
}
