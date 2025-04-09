package edu.mbhs.sclawren.games.sudoku;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;

/**
 * 
 * @author Scott Lawrence
 *
 */
public class SudokuFrame extends JFrame implements Printable {

    private static final long serialVersionUID = -2704565842909756916L;

    SudokuBoard board;

    SudokuFrame f;

    JButton[][] b;

    JLabel timerLabel;

    String[][] backup;

    JPanel[][] p;

    JPanel main, giant, right;

    JButton stop;

    boolean[][] original;

    int time;

    boolean canUpdateTimer;

    boolean timerIsUsed;

    Timer t;

    static String versionString = "6.0";

    public SudokuFrame() {
        super("Scott Lawrence's Sudoku Game Version " + versionString);
        timerIsUsed = true;
        setResizable(false);
        f = this;
        original = new boolean[9][9];
        File f = new File(System.getProperty("user.home") + File.separatorChar + "sudoku.su");
        try {
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream o = new ObjectInputStream(fos);
            if (o.readBoolean()) {
                int input = JOptionPane.showConfirmDialog(null, "Do you want to continue " + "your old game?", "New Game?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (input == JOptionPane.NO_OPTION) createBoard(); else {
                    time = o.readInt();
                    for (int r = 0; r < 9; r++) {
                        for (int c = 0; c < 9; c++) {
                            original[r][c] = o.readBoolean();
                        }
                    }
                    board = (SudokuBoard) o.readObject();
                    almostCreateBoard();
                }
            } else {
                createBoard();
            }
        } catch (Exception eas) {
            eas.printStackTrace();
            createBoard();
        }
    }

    private class WL implements WindowListener {

        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
            File f = new File(System.getProperty("user.home") + "/sudoku.su");
            saveGame(f);
        }

        public void windowClosing(WindowEvent e) {
            File f = new File(System.getProperty("user.home") + "/sudoku.su");
            saveGame(f);
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }
    }

    public void loadGame(File f) {
        if (!f.getName().endsWith(".su")) {
            try {
                f = new File(f.getCanonicalPath() + ".su");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream fos = new FileInputStream(f);
            ObjectInputStream o = new ObjectInputStream(fos);
            o.readBoolean();
            time = o.readInt();
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    original[r][c] = o.readBoolean();
                }
            }
            board = (SudokuBoard) o.readObject();
            t.cancel();
            almostCreateBoard();
        } catch (Exception eas) {
            eas.printStackTrace();
            JOptionPane.showMessageDialog(this, "Game could not be loaded, a new game is " + "being started.", "Error loading game", JOptionPane.ERROR_MESSAGE);
            createBoard();
        }
    }

    public void saveGame(File f) {
        if (!f.getName().endsWith(".su")) {
            try {
                f = new File(f.getCanonicalPath() + ".su");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream o = new ObjectOutputStream(fos);
            if (isDone()) o.writeBoolean(false); else o.writeBoolean(true);
            o.writeInt(time);
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    o.writeBoolean(original[r][c]);
                }
            }
            o.writeObject(board);
        } catch (Exception eas) {
        }
    }

    public String timeString() {
        String s = new String();
        s += "Timer: ";
        if (time >= 3600) {
            s += time / 3600;
            s += ':';
        } else {
            s += "   ";
        }
        s += ((time % 3600) / 60) < 10 ? "0" + (time % 3600) / 60 : (time % 3600) / 60;
        s += ':';
        s += (time % 60) < 10 ? "0" + time % 60 : time % 60;
        return s;
    }

    public void almostCreateBoard() {
        try {
            remove(main);
        } catch (Exception e) {
        }
        addWindowListener(new WL());
        main = new JPanel();
        giant = new JPanel();
        right = new JPanel();
        setContentPane(giant);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(Color.black);
        p = new JPanel[3][3];
        for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++) {
            p[r][c] = new JPanel();
            p[r][c].setBorder(BorderFactory.createLineBorder(Color.black, 2));
            p[r][c].setLayout(new GridLayout(3, 3));
            main.add(p[r][c]);
        }
        add(main);
        add(right);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        b = new JButton[9][9];
        backup = new String[9][9];
        main.setLayout(new GridLayout(3, 3));
        getContentPane().setLayout(new FlowLayout());
        t = new Timer(true);
        timerLabel = new JLabel(timeString());
        t.schedule(new TimerTask() {

            public void run() {
                if (!canUpdateTimer) return;
                time++;
                timerLabel.setText(timeString());
                pack();
            }
        }, 1000, 1000);
        timerLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        right.add(timerLabel);
        canUpdateTimer = true;
        stop = new JButton("Pause");
        stop.setAlignmentX(JButton.CENTER_ALIGNMENT);
        stop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (stop.getText().equals("Pause")) {
                    stop.setText("Start");
                    for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
                        backup[r][c] = b[r][c].getText();
                        b[r][c].setText(" ");
                    }
                    canUpdateTimer = false;
                } else {
                    for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
                        b[r][c].setText(backup[r][c]);
                    }
                    stop.setText("Pause");
                    canUpdateTimer = true;
                }
            }
        });
        right.add(stop);
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
            b[r][c] = new JButton();
            backup[r][c] = new String();
            b[r][c].setBackground(Color.white);
            b[r][c].setFocusable(false);
            b[r][c].setPressedIcon(null);
            b[r][c].setAlignmentX(JLabel.CENTER);
            b[r][c].setAlignmentY(JLabel.CENTER);
            Listener a = new Listener(r, c);
            for (MouseListener l : b[r][c].getMouseListeners()) {
                b[r][c].removeMouseListener(l);
            }
            for (MouseMotionListener l : b[r][c].getMouseMotionListeners()) {
                b[r][c].removeMouseMotionListener(l);
            }
            b[r][c].addMouseListener(a);
            b[r][c].addMouseMotionListener(a);
            b[r][c].setText(Character.toString(board.getCharAt(r, c)));
            if (original[r][c]) {
                b[r][c].setText("<html><font color=red size=6>" + b[r][c].getText() + "</font></html>");
            } else if (!b[r][c].getText().equals(" ")) {
                b[r][c].setText("<html><font size=6>" + b[r][c].getText() + "</font></html>");
            }
            b[r][c].setBorder(BorderFactory.createLineBorder(Color.black, 1));
            int d = 35;
            b[r][c].setMinimumSize(new Dimension(d, d));
            b[r][c].setPreferredSize(new Dimension(d, d));
            b[r][c].setMaximumSize(new Dimension(d, d));
            p[r / 3][c / 3].add(b[r][c]);
        }
        createMenus();
        pack();
        adjustLocation();
    }

    public void createBoard() {
        try {
            remove(main);
        } catch (Exception e) {
        }
        addWindowListener(new WL());
        main = new JPanel();
        giant = new JPanel();
        right = new JPanel();
        setContentPane(giant);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        board = SudokuBoard.create();
        original = new boolean[9][9];
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
            if (board.getIntAt(r, c) == 0) original[r][c] = false; else original[r][c] = true;
        }
        setBackground(Color.black);
        p = new JPanel[3][3];
        for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++) {
            p[r][c] = new JPanel();
            p[r][c].setBorder(BorderFactory.createLineBorder(Color.black, 2));
            p[r][c].setLayout(new GridLayout(3, 3));
            main.add(p[r][c]);
        }
        add(main);
        add(right);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        b = new JButton[9][9];
        backup = new String[9][9];
        main.setLayout(new GridLayout(3, 3));
        getContentPane().setLayout(new FlowLayout());
        t = new Timer(true);
        timerLabel = new JLabel(timeString());
        t.schedule(new TimerTask() {

            public void run() {
                if (!canUpdateTimer) return;
                time++;
                timerLabel.setText(timeString());
            }
        }, 1000, 1000);
        timerLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        right.add(timerLabel);
        time = 0;
        canUpdateTimer = true;
        stop = new JButton("Pause");
        stop.setAlignmentX(JButton.CENTER_ALIGNMENT);
        stop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (stop.getText().equals("Pause")) {
                    stop.setText("Start");
                    for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
                        backup[r][c] = b[r][c].getText();
                        b[r][c].setText(" ");
                    }
                    canUpdateTimer = false;
                } else {
                    for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
                        b[r][c].setText(backup[r][c]);
                    }
                    stop.setText("Pause");
                    canUpdateTimer = true;
                }
            }
        });
        right.add(stop);
        for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
            b[r][c] = new JButton();
            backup[r][c] = new String();
            b[r][c].setBackground(Color.white);
            b[r][c].setFocusable(false);
            b[r][c].setPressedIcon(null);
            b[r][c].setAlignmentX(JLabel.CENTER);
            b[r][c].setAlignmentY(JLabel.CENTER);
            Listener a = new Listener(r, c);
            for (MouseListener l : b[r][c].getMouseListeners()) {
                b[r][c].removeMouseListener(l);
            }
            for (MouseMotionListener l : b[r][c].getMouseMotionListeners()) {
                b[r][c].removeMouseMotionListener(l);
            }
            b[r][c].addMouseListener(a);
            b[r][c].addMouseMotionListener(a);
            b[r][c].setText(Character.toString(board.getCharAt(r, c)));
            if (original[r][c]) {
                b[r][c].setText("<html><font color=red size=6>" + b[r][c].getText() + "</font></html>");
            } else if (!b[r][c].getText().equals(" ")) {
                b[r][c].setText("<html><font size=6>" + b[r][c].getText() + "</font></html>");
            }
            b[r][c].setBorder(BorderFactory.createLineBorder(Color.black, 1));
            int d = 35;
            b[r][c].setMinimumSize(new Dimension(d, d));
            b[r][c].setPreferredSize(new Dimension(d, d));
            b[r][c].setMaximumSize(new Dimension(d, d));
            p[r / 3][c / 3].add(b[r][c]);
        }
        createMenus();
        pack();
        adjustLocation();
    }

    private void adjustLocation() {
        setLocation((int) (getToolkit().getScreenSize().getWidth() - getWidth()) / 2, (int) (getToolkit().getScreenSize().getHeight() - getHeight()) / 2);
    }

    public boolean isDone() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board.getCharAt(r, c) == ' ' && b[r][c].getText().equals(" ")) return false;
            }
        }
        return true;
    }

    private class Listener implements MouseListener, MouseMotionListener {

        int row, col;

        int y;

        int dif;

        public Listener(int r, int c) {
            row = r;
            col = c;
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            y = e.getY();
        }

        public void mouseReleased(MouseEvent e) {
            if (original[row][col]) return;
            dif = (y - e.getY()) / 7;
            int v = dif;
            if (v > 9) v = 9;
            if (v < 0) v = 0;
            if (v == board.getIntAt(row, col)) {
                return;
            }
            if (v == 0) {
                board.setIntAt(row, col, 0);
            } else {
                if (board.setIntAt(row, col, v)) {
                    b[row][col].setText(Integer.toString(v));
                    b[row][col].setText("<html><font size=6>" + b[row][col].getText() + "</font></html>");
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid input", "Naughty, naughty", JOptionPane.ERROR_MESSAGE);
                    b[row][col].setText(" ");
                    board.setIntAt(row, col, 0);
                }
            }
            if (isDone()) {
                handleDone();
            }
        }

        public boolean isDone() {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board.getCharAt(r, c) == ' ' && b[r][c].getText().equals(" ")) return false;
                }
            }
            return true;
        }

        public void mouseDragged(MouseEvent e) {
            dif = (y - e.getY()) / 7;
            if (original[row][col]) return;
            int v = dif;
            if (v > 9) v = 9;
            if (v < 0) v = 0;
            if (v != 0) b[row][col].setText("<html><font size=6>" + v + "</font></html>");
            if (v == 0) b[row][col].setText(" ");
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    public void handleDone() {
        t.cancel();
        try {
            BestTimes b = BestTimes.read();
            if (!b.isBest(time) || !timerIsUsed) {
                JOptionPane.showMessageDialog(f, "You're done.  Please accept these special " + "automated congratulations.", "Congratulations", JOptionPane.PLAIN_MESSAGE);
            } else {
                String name = JOptionPane.showInputDialog(f, "You're time was one of the best.  " + "\nPlease give your name", "Low Time - High Score", JOptionPane.PLAIN_MESSAGE);
                b.addTime(time, name);
                b.displayDialog(f);
            }
            BestTimes.write(b);
        } catch (Exception eas) {
            eas.printStackTrace();
            JOptionPane.showMessageDialog(f, "You're done.  Please accept these special " + "automated congratulations.", "Congratulations", JOptionPane.PLAIN_MESSAGE);
        }
    }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }
        graphics.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
        main.paint(graphics);
        return PAGE_EXISTS;
    }

    private class PrintListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            PrinterJob j = PrinterJob.getPrinterJob();
            if (!j.printDialog()) return;
            j.setJobName("Sudoku");
            j.setPrintable(f);
            j.setCopies(1);
            try {
                j.print();
            } catch (PrinterException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void createMenus() {
        JMenuBar menuBar = new JMenuBar();
        JMenu game = new JMenu("Game");
        final JMenu timer = new JMenu("Timer");
        JMenu help = new JMenu("Help");
        game.setMnemonic('G');
        timer.setMnemonic('T');
        help.setMnemonic('H');
        game.add(createMenuItem("New", 'N', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!isDone()) {
                    int a = JOptionPane.showConfirmDialog(null, "Are you sure you want to start a new game?", "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (a != JOptionPane.YES_OPTION) return;
                }
                t.cancel();
                createBoard();
            }
        }, "new", "Start a new game", KeyStroke.getKeyStroke("ctrl N")));
        game.add(createMenuItem("Load", 'L', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {

                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".su") || pathname.isDirectory();
                    }

                    public String getDescription() {
                        return "Sudoku Games (*.su)";
                    }
                };
                chooser.setFileFilter(filter);
                chooser.setDialogTitle("Load Game");
                chooser.setApproveButtonText("Load");
                int returnVal = chooser.showOpenDialog(f);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    loadGame(chooser.getSelectedFile());
                }
            }
        }, "load", "Reload an old game from a file", KeyStroke.getKeyStroke("ctrl L")));
        game.add(createMenuItem("Save", 'S', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {

                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".su") || pathname.isDirectory();
                    }

                    public String getDescription() {
                        return "Sudoku Games (*.su)";
                    }
                };
                chooser.setFileFilter(filter);
                chooser.setDialogTitle("Save Game");
                chooser.setApproveButtonText("Save");
                int returnVal = chooser.showSaveDialog(f);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    saveGame(chooser.getSelectedFile());
                }
            }
        }, "save", "Save this game to a file", KeyStroke.getKeyStroke("ctrl S")));
        game.add(createMenuItem("Reset", 'R', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!canUpdateTimer) {
                    JOptionPane.showMessageDialog(f, "The game cannot be reset while the \n" + "timer is stopped.", "Can't reset the game", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int a;
                if (((JMenuItem) (timer.getMenuComponent(2))).getText().equals("Hide")) a = JOptionPane.showConfirmDialog(null, "Are you sure you want to reset this game?\n" + "The timer will not be reset.", "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); else a = JOptionPane.showConfirmDialog(null, "Are you sure you want to reset this game?\n", "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (a != JOptionPane.YES_OPTION) return;
                for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) if (!original[r][c]) {
                    board.setIntAt(r, c, 0);
                    b[r][c].setText(" ");
                }
            }
        }, "reset", "Reset the game", KeyStroke.getKeyStroke("ctrl R")));
        game.addSeparator();
        game.add(createMenuItem("Print", 'P', new PrintListener(), "print", "Print the board", KeyStroke.getKeyStroke("ctrl P")));
        game.add(createMenuItem("Exit", 'x', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }, "exit", "Quit Sudoku", KeyStroke.getKeyStroke("ctrl C")));
        timer.add(createMenuItem("Pause", 'P', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (stop.getText().equals("Pause")) {
                    stop.setText("Start");
                    for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
                        backup[r][c] = b[r][c].getText();
                        b[r][c].setText(" ");
                    }
                    canUpdateTimer = false;
                } else {
                    return;
                }
            }
        }, "pause", "Pause the timer"));
        timer.add(createMenuItem("Start", 'S', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (stop.getText().equals("Pause")) {
                    return;
                } else {
                    for (int r = 0; r < 9; r++) for (int c = 0; c < 9; c++) {
                        b[r][c].setText(backup[r][c]);
                    }
                    stop.setText("Pause");
                    canUpdateTimer = true;
                }
            }
        }, "start", "Start the timer"));
        timer.add(createMenuItem("Hide", 'H', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (((JMenuItem) (timer.getMenuComponent(2))).getText().equals("Hide")) {
                    timerLabel.setVisible(false);
                    stop.setVisible(false);
                    f.pack();
                    f.adjustLocation();
                    ((JMenuItem) (timer.getMenuComponent(2))).setText("Show");
                    ((JMenuItem) (timer.getMenuComponent(2))).setMnemonic('h');
                    ((JMenuItem) (timer.getMenuComponent(2))).setToolTipText("Display " + "the timer");
                    timerIsUsed = false;
                } else {
                    timerLabel.setVisible(true);
                    stop.setVisible(true);
                    f.pack();
                    f.adjustLocation();
                    ((JMenuItem) (timer.getMenuComponent(2))).setText("Hide");
                    ((JMenuItem) (timer.getMenuComponent(2))).setMnemonic('H');
                    ((JMenuItem) (timer.getMenuComponent(2))).setToolTipText("Hide " + "the timer");
                    timerIsUsed = true;
                }
            }
        }, "hide", "Hide the timer", KeyStroke.getKeyStroke("ctrl H")));
        timer.add(createMenuItem("Best Times", 'B', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                BestTimes.read().displayDialog(f);
            }
        }, "best", "View the best times", KeyStroke.getKeyStroke("ctrl B")));
        help.add(createMenuItem("Playing Sudoku", 'P', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(f, "The goal of sudoku is to fill in every square " + "with a number between 1 and 9 \nas long as no row, column, or box has two " + "squares with the same number.  " + "\n\nClick on a square and drag up or down to select a " + "number.  Release the mouse \nbutton to choose that number.", "Sudoku Help", JOptionPane.INFORMATION_MESSAGE);
            }
        }, "help", "Get help playing Sudoku"));
        help.add(createMenuItem("About", 'A', new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(f, "Sudoku Version " + versionString + "\n\n" + "Copyleft (L) 2008 Scott Lawrence\nLicensed under the " + "GNU GPL V3.", "About Sudoku", JOptionPane.INFORMATION_MESSAGE);
            }
        }, "about", "About Scott Lawrence's Sudoku"));
        menuBar.add(game);
        menuBar.add(timer);
        menuBar.add(help);
        setJMenuBar(menuBar);
    }

    private JMenuItem createMenuItem(String name, char mnemonic, ActionListener l, String actionCommand, final String description) {
        JMenuItem item = new JMenuItem(name);
        item.setMnemonic(mnemonic);
        item.addActionListener(l);
        item.setActionCommand(actionCommand);
        item.setToolTipText(description);
        return item;
    }

    private JMenuItem createMenuItem(String name, char mnemonic, ActionListener l, String actionCommand, final String description, final KeyStroke ks) {
        JMenuItem item = new JMenuItem(name);
        item.setMnemonic(mnemonic);
        item.addActionListener(l);
        item.setActionCommand(actionCommand);
        item.setToolTipText(description);
        item.setAccelerator(ks);
        return item;
    }
}
