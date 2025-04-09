import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class GUI extends JFrame implements ActionListener, DocumentListener, MouseListener, ChangeListener, KeyListener {

    public JTextPane textArea = new JTextPane();

    Container pane = getContentPane();

    public JScrollPane scrollPane = new JScrollPane(textArea);

    JPanel row1, row2, row3;

    public JFileChooser chooser = new JFileChooser();

    public JFileChooser chooseImage = new JFileChooser();

    public File file = chooser.getSelectedFile();

    public File fileImage = chooseImage.getSelectedFile();

    JToolBar toolbar = new JToolBar();

    JToolBar find = new JToolBar();

    JToolBar style = new JToolBar();

    JToolBar main = new JToolBar();

    JComboBox font, size;

    JLabel count, findl;

    JButton exit, next, previous, colorbut, center, right, underline, left, justified, bold, italic, newb, open, save, print;

    JCheckBox highall, match;

    JTextField findfield = new JTextField(25);

    Calendar calendar = new GregorianCalendar();

    int car, index, length, curMin, saveInterval, textLength;

    PrinterJob job = PrinterJob.getPrinterJob();

    Color HILIT_COLOR = Color.YELLOW;

    Color CORRECT_COLOR = Color.GREEN;

    Color ERROR_COLOR = Color.RED;

    String CANCEL_ACTION = "cancel-search";

    Color findfieldBg;

    Highlighter hilit;

    Highlighter.HighlightPainter painter;

    JColorChooser colorChooser = new JColorChooser(Color.BLACK);

    JDialog dialog = new JDialog();

    JPopupMenu popupMenu;

    StyledDocument doc = (StyledDocument) textArea.getDocument();

    SimpleAttributeSet set = new SimpleAttributeSet();

    String s;

    String content;

    String[] contentarray;

    String[] words = { "Apple", "Bob", "Cat", "Dog" };

    ImageIcon leftimage = new ImageIcon("images/left.gif");

    ImageIcon rightimage = new ImageIcon("images/right.gif");

    ImageIcon centerimage = new ImageIcon("images/centered.gif");

    ImageIcon justimage = new ImageIcon("images/justified.gif");

    ImageIcon colorimage = new ImageIcon("images/color-chooser.png");

    ImageIcon underimage = new ImageIcon("images/underline.gif");

    ImageIcon boldimage = new ImageIcon("images/bold.gif");

    ImageIcon italicimage = new ImageIcon("images/italic.gif");

    ImageIcon exitimage = new ImageIcon("images/IconExit.gif");

    ImageIcon nextimage = new ImageIcon("images/next.gif");

    ImageIcon previousimage = new ImageIcon("images/previous.gif ");

    ImageIcon newimage = new ImageIcon("images/new.jpg");

    ImageIcon openimage = new ImageIcon("images/open.gif");

    ImageIcon saveimage = new ImageIcon("images/save.gif");

    ImageIcon printimage = new ImageIcon("images/print.gif");

    String defaultFont = "Purisa";

    String fileName = new String();

    boolean isChanged = false;

    ConfigReader config = new ConfigReader("settings/sw-settings-nathanfinch.sws");

    int saveMinInt;

    int intValueOfNum;

    String textString;

    public GUI() {
        super("untitled - Saturn Writer v0.1a");
        super.setPreferredSize(new Dimension(1000, 600));
        newFileName frame = new newFileName(this);
        textLengthTimer();
        autoSave();
        dialog.setSize(new Dimension(450, 300));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        pane.setLayout(layout);
        StyleConstants.setFontFamily(set, defaultFont);
        textArea.setParagraphAttributes(set, true);
        StyleConstants.setFontSize(set, 12);
        textArea.setParagraphAttributes(set, true);
        hilit = new DefaultHighlighter();
        painter = new DefaultHighlighter.DefaultHighlightPainter(HILIT_COLOR);
        textArea.setHighlighter(hilit);
        textArea.setMargin(new Insets(104, 124, 104, 124));
        row1 = new JPanel();
        row2 = new JPanel();
        row3 = new JPanel();
        row1.setLayout(new FlowLayout());
        row3.setLayout(new FlowLayout());
        this.setFocusable(true);
        this.addKeyListener(this);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        dialog.add(colorChooser);
        colorChooser.getSelectionModel().addChangeListener(this);
        colorbut = new JButton(colorimage);
        colorbut.addActionListener(this);
        colorbut.setActionCommand("Color");
        center = new JButton(centerimage);
        center.addActionListener(this);
        center.setActionCommand("Center");
        right = new JButton(rightimage);
        right.addActionListener(this);
        right.setActionCommand("Right");
        underline = new JButton(underimage);
        underline.addActionListener(this);
        underline.setActionCommand("Underline");
        left = new JButton(leftimage);
        left.addActionListener(this);
        left.setActionCommand("Left");
        justified = new JButton(justimage);
        justified.addActionListener(this);
        justified.setActionCommand("Justified");
        bold = new JButton(boldimage);
        bold.addActionListener(this);
        bold.setActionCommand("Bold");
        italic = new JButton(italicimage);
        italic.addActionListener(this);
        italic.setActionCommand("Italic");
        newb = new JButton(newimage);
        newb.addActionListener(this);
        newb.setActionCommand("New...");
        open = new JButton(openimage);
        open.addActionListener(this);
        open.setActionCommand("Open...");
        save = new JButton(saveimage);
        save.addActionListener(this);
        save.setActionCommand("Save");
        print = new JButton(printimage);
        print.addActionListener(this);
        print.setActionCommand("Print...");
        popupMenu = new JPopupMenu();
        String[] listFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String[] listSize = { "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32" };
        font = new JComboBox(listFonts);
        size = new JComboBox(listSize);
        font.setEditable(true);
        size.setEditable(true);
        font.addActionListener(this);
        size.addActionListener(this);
        style.add(font);
        style.add(size);
        find.setFloatable(false);
        toolbar.setFloatable(false);
        findl = new JLabel("Find: ");
        exit = new JButton(exitimage);
        next = new JButton(nextimage);
        previous = new JButton(previousimage);
        highall = new JCheckBox("Highlight All");
        match = new JCheckBox("Match Case");
        match.addActionListener(this);
        match.setActionCommand("Match");
        exit.addActionListener(this);
        exit.setActionCommand("Exitf");
        findfieldBg = findfield.getBackground();
        findfield.getDocument().addDocumentListener(this);
        ExampleFileFilter filter = new ExampleFileFilter("java");
        filter.setDescription("Java Source Files");
        chooser.addChoosableFileFilter(filter);
        filter = new ExampleFileFilter("class");
        filter.setDescription("Class Files");
        chooser.addChoosableFileFilter(filter);
        filter = new ExampleFileFilter("txt");
        filter.setDescription("Text Files");
        chooser.addChoosableFileFilter(filter);
        filter = new ExampleFileFilter("java");
        filter.addExtension("class");
        filter.setDescription("Source and Class Files");
        chooser.addChoosableFileFilter(filter);
        filter = new ExampleFileFilter("swd");
        filter.setDescription("Saturn Writer Document");
        chooser.addChoosableFileFilter(filter);
        JMenuBar menuBar = new JMenuBar();
        menu.buildMenu(menuBar, "data/default.menu", this);
        CaretListener listener = new CaretListener() {

            public void caretUpdate(CaretEvent caretEvent) {
                car = caretEvent.getDot();
                count = new JLabel("Caret Position: " + caretEvent.getDot());
                System.out.println("dot:" + caretEvent.getDot());
                System.out.println("mark" + caretEvent.getMark());
                style.add(count);
            }
        };
        textArea.addCaretListener(listener);
        find.add(exit);
        find.add(findl);
        find.add(findfield);
        find.add(next);
        find.add(previous);
        find.add(highall);
        find.add(match);
        count = new JLabel("Caret Position: " + car);
        count.setAlignmentX(RIGHT_ALIGNMENT);
        style.addSeparator();
        style.add(bold);
        style.add(italic);
        style.add(underline);
        style.addSeparator();
        style.add(left);
        style.add(center);
        style.add(right);
        style.add(justified);
        style.addSeparator();
        style.add(colorbut);
        main.add(newb);
        main.add(open);
        main.add(save);
        main.addSeparator();
        main.add(print);
        row1.add(toolbar);
        row2.add(find);
        pane.add(new JScrollPane(textArea));
        setJMenuBar(menuBar);
        toolbar.add(count);
        pane.add(row2, BorderLayout.SOUTH);
        pane.add(style, BorderLayout.NORTH);
        pane.add(main, BorderLayout.EAST);
        find.setVisible(false);
        setContentPane(pane);
        pack();
        setVisible(true);
    }

    /***************************************************************************
	 * The About popup thing *
	 **************************************************************************/
    public void showAbout() {
        JOptionPane.showMessageDialog(null, "This is Saturn Writer, a free word proccesor.\n" + "You are using version 0.1 Pre-Alpha.\n\n" + "Created by Nathan Finch.", "About Saturn Writer...", JOptionPane.INFORMATION_MESSAGE);
    }

    public void currentFeatures() {
        JOptionPane.showMessageDialog(null, "Created Right Now: Auto List(Command Line Version)\n" + "					  Inserting an Image\n" + "					  Time and Date Insertion\n" + "					  Font Changing\n" + "					  Word Count\n" + "					  AutoSave\n" + "Planned Features : Clip Art\n" + "					  Spell Check\n" + "					  Data Recovery\n" + "					  Real Page Setup\n", "Features", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showLetCount() {
        JOptionPane.showMessageDialog(null, "The Current count of letters is (incl. spaces): " + textArea.getCaretPosition(), "Letter Count", JOptionPane.INFORMATION_MESSAGE);
    }

    public void search() {
        hilit.removeAllHighlights();
        s = findfield.getText();
        if (s.length() <= 0) {
            findfield.setBackground(findfieldBg);
            return;
        }
        content = content.toLowerCase();
        int index = content.indexOf(s, 0);
        s = s.toLowerCase();
        if (index >= 0) {
            try {
                int end = index + s.length();
                hilit.addHighlight(index, end, painter);
                textArea.setCaretPosition(end);
                findfield.setBackground(CORRECT_COLOR);
                findfield.setForeground(Color.BLACK);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        } else {
            findfield.setBackground(ERROR_COLOR);
            findfield.setForeground(Color.WHITE);
        }
    }

    public void spellcheck() {
        content = textArea.getText();
        contentarray = content.split("\\s+");
        StyleConstants.setForeground(set, Color.RED);
        textArea.setParagraphAttributes(set, true);
    }

    public void wordCount() {
        content = textArea.getText();
        contentarray = content.split("\\s+");
        length = contentarray.length;
    }

    public void wordCountWindow() {
        content = textArea.getText();
        contentarray = content.split("\\s+");
        length = contentarray.length;
        String[] linearray = content.split("\n");
        int linelength = linearray.length;
        String[] pararray = content.split("\t");
        int paralength = pararray.length;
        JOptionPane.showMessageDialog(null, "Word Count: " + length + "\n" + "Lines: " + linelength + "\n" + "Paragraphs:" + paralength, "Word Count", JOptionPane.INFORMATION_MESSAGE);
    }

    public void youSure() {
        Object[] choices = { "Save", "Discard", "Cancel" };
        int choice = JOptionPane.showOptionDialog(null, "Are you sure you want to quit?", "Are you sure?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
        if (choice == 0) {
            int returnVal = chooser.showSaveDialog(GUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                fileio.write(textArea.getText(), file.getPath());
            }
            System.exit(1);
        } else if (choice == 1) {
            System.exit(1);
        }
    }

    public void textLengthTimer() {
        ActionListener actionListener1 = new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                textString = textArea.getText();
                textLength = textString.length();
            }
        };
        Timer timer = new Timer(1, actionListener1);
        timer.start();
    }

    public void autoSave() {
        ActionListener actionListener1 = new ActionListener() {

            public void actionPerformed(ActionEvent actionEvent) {
                fileio.write(textArea.getText(), "temps/" + fileName + ".temp");
                int hour = (calendar.get(Calendar.HOUR) == 0 ? 12 : calendar.get(Calendar.HOUR));
                String min = (calendar.get(Calendar.MINUTE) < 10 ? ("0" + calendar.get(Calendar.MINUTE)) : ("" + calendar.get(Calendar.MINUTE)));
                String sec = (calendar.get(Calendar.SECOND) < 10 ? ("0" + calendar.get(Calendar.SECOND)) : ("" + calendar.get(Calendar.SECOND)));
                String AMPM = (calendar.get(Calendar.AM_PM) == 0 ? "AM" : "PM");
            }
        };
        Timer timer = new Timer(saveInterval, actionListener1);
        timer.start();
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        Object src = e.getSource();
        if (command.equals("New...")) {
            System.out.println(saveInterval);
        }
        if (command.equals("Print...")) {
            PrintUtilities.printComponent(textArea);
        }
        if (command.equals("Exit")) {
            youSure();
        }
        if (command.equals("WW")) {
        }
        if (command.equals("About Saturn Writer...")) {
            showAbout();
        }
        if (command.equals("Features")) {
            currentFeatures();
        }
        if (command.equals("Word Count")) {
            wordCountWindow();
        }
        if (command.equals("Font")) {
            FontWindow frame = new FontWindow(this);
        }
        if (command.equals("Find")) {
            find.setVisible(true);
        }
        if (command.equals("Exitf")) {
            find.setVisible(false);
        }
        if (command.equals("FindNext")) {
        }
        if (command.equals("SelectAll")) {
            textArea.selectAll();
        }
        if (command.equals("Time and Date")) {
            TimenDate frame = new TimenDate(this);
        }
        if (command.equals("Normal")) {
            textArea.setMargin(new Insets(3, 124, 3, 124));
        }
        if (command.equals("WebLayout")) {
            textArea.setMargin(new Insets(3, 124, 3, 124));
        }
        if (command.equals("PrintLay")) {
            textArea.setMargin(new Insets(104, 124, 104, 124));
        }
        if (command.equals("Delete")) {
            textArea.setText(textArea.getText().substring(0, textArea.getSelectionStart()) + textArea.getText().substring(textArea.getSelectionEnd()));
        }
        if (command.equals("Copy")) {
            textArea.copy();
            System.out.println(textArea.getSelectedText());
        }
        if (command.equals("Cut")) {
            textArea.cut();
        }
        if (command.equals("Paste")) {
            textArea.paste();
        }
        if (command.equals("Save")) {
            int returnVal = chooser.showSaveDialog(GUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                fileio.write(textArea.getText(), file.getPath());
            }
        }
        if (command.equals("Save As")) {
            int returnVal = chooser.showSaveDialog(GUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                fileio.write(textArea.getText(), file.getPath());
            }
        }
        if (command.equals("Open...")) {
            int returnVal = chooser.showOpenDialog(GUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                textArea.setText(fileio.read(file.getPath()));
            }
        }
        if (command.equals("StatusBar")) {
        }
        if (command.equals("LetCount")) {
            showLetCount();
        }
        if (command.equals("Spell Check")) {
            spellcheck();
        }
        if (command.equals("Auto List")) {
            AutoList frame = new AutoList(this);
        }
        if (command.equals("Color")) {
            dialog.setVisible(true);
        }
        if (command == ("Left")) {
            StyleConstants.setAlignment(set, StyleConstants.ALIGN_LEFT);
            textArea.setParagraphAttributes(set, true);
        }
        if (command == ("Center")) {
            StyleConstants.setAlignment(set, StyleConstants.ALIGN_CENTER);
            textArea.setParagraphAttributes(set, true);
        }
        if (command == ("Right")) {
            StyleConstants.setAlignment(set, StyleConstants.ALIGN_RIGHT);
            textArea.setParagraphAttributes(set, true);
        }
        if (command == ("Justified")) {
            StyleConstants.setAlignment(set, StyleConstants.ALIGN_JUSTIFIED);
            textArea.setParagraphAttributes(set, true);
        }
        if (command == ("Underline")) {
            StyleConstants.setUnderline(set, !StyleConstants.isUnderline(set));
            textArea.setParagraphAttributes(set, true);
        }
        if (command == ("Bold")) {
            StyleConstants.setBold(set, !StyleConstants.isBold(set));
            textArea.setParagraphAttributes(set, true);
        }
        if (command == ("Italic")) {
            StyleConstants.setItalic(set, !StyleConstants.isItalic(set));
            textArea.setParagraphAttributes(set, true);
        }
        if (command.equals("Image...")) {
            int returnVal = chooseImage.showOpenDialog(GUI.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File fileImage = chooseImage.getSelectedFile();
                try {
                    Style style = doc.addStyle("StyleName", null);
                    StyleConstants.setIcon(style, new ImageIcon(fileImage.getPath()));
                    doc.insertString(textArea.getCaretPosition(), "ignored text", style);
                } catch (BadLocationException event) {
                }
            }
        }
        if (command.equals("Options...")) {
            Preferences frame = new Preferences(this);
        }
        if (command.equals("Clip Art")) {
            ClipArt frame = new ClipArt(this);
        }
        String newFont = (String) font.getSelectedItem();
        String newSize = (String) size.getSelectedItem();
        int newSizeC = Integer.parseInt(newSize);
        StyleConstants.setFontFamily(set, newFont);
        textArea.setParagraphAttributes(set, true);
        StyleConstants.setFontSize(set, newSizeC);
        textArea.setParagraphAttributes(set, true);
    }

    public void insertUpdate(DocumentEvent ev) {
        search();
        spellcheck();
        wordCount();
    }

    public void removeUpdate(DocumentEvent ev) {
        search();
        spellcheck();
        wordCount();
    }

    public void changedUpdate(DocumentEvent ev) {
        wordCount();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        Object src = e.getSource();
    }

    public void stateChanged(ChangeEvent e) {
        Color newColor = colorChooser.getColor();
        StyleConstants.setForeground(set, newColor);
        textArea.setParagraphAttributes(set, true);
    }

    public void keyPressed(KeyEvent e) {
        int id = e.getID();
        String keyString;
        if (id == KeyEvent.KEY_TYPED) {
            char c = e.getKeyChar();
            keyString = "key character = '" + c + "'";
            System.out.println(keyString);
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] arguments) {
        GUI frame = new GUI();
    }
}
