import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.util.*;
import java.text.*;
import antlr.CommonAST;
import antlr.debug.misc.ASTFrame;

/**
 * 
 * @author Team STL
 * The STLGui class was initially written for testing purposes, but is now a convenient
 * way to present the language.  It should not be considered a substitute for running the
 * interpreter by itself, as its multithreaded nature and additional features can easily
 * introduce confusing errors into otherwise valid STL programs.
 *
 */
public final class STLGui extends JFrame implements KeyListener, ActionListener, AdjustmentListener {

    String testdir = "./test/";

    double money;

    Hashtable portfolio;

    PrintStream other_stream;

    STLLexer lexer;

    STLParser parser;

    CommonAST parseTree;

    STLWalker walker;

    STLDataType dt;

    ASTFrame frame;

    JPanel top;

    JMenuBar menu;

    JTextPane input, output;

    JLabel cash;

    JList stock_list;

    JButton quit, save, load, run, clear, parse, test;

    JToggleButton toggle, onin;

    DefaultStyledDocument input_doc, output_doc;

    JScrollPane inScroll, outScroll;

    JSplitPane io, listdiv;

    FilteredStream err_redirect, out_redirect;

    /**
     * Contructor for the STLString Class. Initialize str.
     * @param   tmp
     * @return  none
     * @see STLDataType
     */
    public STLGui() {
        try {
            stock_list = new JList();
            money = 0;
            out_redirect = new FilteredStream(new Color(255, 255, 255));
            err_redirect = new FilteredStream(new Color(255, 0, 0));
            other_stream = System.out;
            System.setOut(new PrintStream(out_redirect, true));
            System.setErr(new PrintStream(err_redirect, true));
            top = new JPanel();
            top.setLayout(new BoxLayout(top, BoxLayout.PAGE_AXIS));
            input_doc = new STLCode();
            output_doc = new DefaultStyledDocument();
            input = new JTextPane(input_doc);
            output = new JTextPane(output_doc);
            menu = new JMenuBar();
            quit = new JButton("Quit");
            save = new JButton("Save");
            load = new JButton("Open");
            run = new JButton("Run");
            clear = new JButton("Clear");
            parse = new JButton("Parse");
            test = new JButton("Tests");
            toggle = new JToggleButton("IO");
            onin = new JToggleButton("AutoScroll");
            cash = new JLabel("$0");
            save.addActionListener(this);
            load.addActionListener(this);
            quit.addActionListener(this);
            run.addActionListener(this);
            clear.addActionListener(this);
            parse.addActionListener(this);
            test.addActionListener(this);
            toggle.addActionListener(this);
            onin.addActionListener(this);
            menu.add(test);
            menu.add(clear);
            menu.add(run);
            menu.add(parse);
            menu.add(load);
            menu.add(save);
            menu.add(quit);
            menu.add(toggle);
            menu.add(onin);
            menu.add(cash);
            setJMenuBar(menu);
            inScroll = new JScrollPane(input);
            inScroll.setColumnHeaderView(new JLabel("Input"));
            outScroll = new JScrollPane(output);
            outScroll.setColumnHeaderView(new JLabel("Output"));
            outScroll.getVerticalScrollBar().addAdjustmentListener(this);
            input.setBackground(Color.white);
            output.setBackground(Color.white);
            output.setEditable(false);
            io = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            listdiv = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            io.setTopComponent(outScroll);
            io.setBottomComponent(inScroll);
            output.setToolTipText("Output from the interpreter");
            input.setToolTipText("Enter code (execute with F1, open file with F12, save with F11)");
            input.addKeyListener(this);
            listdiv.setLeftComponent(io);
            listdiv.setRightComponent(stock_list);
            top.add(listdiv);
            add(top);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setMinimumSize(new Dimension(640, 480));
            pack();
            io.setDividerLocation(.5);
            listdiv.setDividerLocation(.75);
            String r[] = { "<no stocks>" };
            stock_list.setListData(r);
            setVisible(true);
        } catch (Exception e) {
        }
    }

    public void adjustmentValueChanged(AdjustmentEvent ev) {
        if (onin.isSelected()) {
            outScroll.getVerticalScrollBar().setValue(outScroll.getVerticalScrollBar().getMaximum());
        }
    }

    public void actionPerformed(ActionEvent ev) {
        if (ev.getActionCommand() == "Save") {
            save();
        } else if (ev.getActionCommand() == "Open") {
            load();
        } else if (ev.getActionCommand() == "Quit") {
            quit();
        } else if (ev.getActionCommand() == "Run") {
            run();
        } else if (ev.getActionCommand() == "Clear") {
            try {
                input_doc.remove(0, input_doc.getLength());
            } catch (Exception e) {
            }
        } else if (ev.getActionCommand() == "Parse") {
            parse();
        } else if (ev.getActionCommand() == "Tests") {
            tests();
        } else if (ev.getActionCommand() == "IO") {
            PrintStream temp = System.out;
            System.setOut(other_stream);
            System.setErr(other_stream);
            other_stream = temp;
        } else if (ev.getActionCommand() == "AutoScroll") {
        }
    }

    void tests() {
        String files[] = new File(testdir).list();
        for (int i = 0; i < files.length; i++) {
            if (files[i].endsWith(".stl")) {
                System.out.print(files[i] + "\n");
                File f = new File(testdir + files[i]);
                loadin(f);
                run();
                System.out.print("\n*************************\n");
                try {
                    input_doc.remove(0, input_doc.getLength());
                } catch (Exception e) {
                }
            }
        }
    }

    void load() {
        try {
            JFileChooser fc = new JFileChooser();
            fc.showOpenDialog(this);
            File file = fc.getSelectedFile();
            if (file.isFile()) {
                System.out.println(file.toString());
                loadin(file);
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    void loadin(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while (reader.ready()) {
                input_doc.insertString(input_doc.getEndPosition().getOffset(), reader.readLine() + "\n", null);
            }
        } catch (Exception e) {
        }
    }

    void save() {
        try {
            JFileChooser fc = new JFileChooser();
            fc.showSaveDialog(this);
            File file = fc.getSelectedFile();
            System.out.println(file.toString());
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            String text = input_doc.getText(0, input_doc.getLength());
            writer.write(text, 0, text.length());
            writer.flush();
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    void parse() {
        try {
            lexer = new STLLexer(new StringReader(input.getText()));
            parser = new STLParser(lexer);
            parser.program();
            parseTree = (CommonAST) parser.getAST();
            frame = new ASTFrame("AST from the STL parser", parseTree);
            frame.setVisible(true);
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    void run() {
        try {
            lexer = new STLLexer(new StringReader(input.getText()));
            parser = new STLParser(lexer);
            parser.program();
            parseTree = (CommonAST) parser.getAST();
            walker = new STLWalker();
            if (portfolio != null) {
                walker.intpret.stockSource.portfolio = portfolio;
            }
            walker.intpret.stockSource.money = money;
            dt = walker.expr(parseTree);
            cash.setText(" $" + walker.intpret.stockSource.money + " ");
            Object l[] = walker.intpret.stockSource.stocks();
            portfolio = walker.intpret.stockSource.portfolio;
            money = walker.intpret.stockSource.money;
            if (l != null) {
                for (int c = 0; c < l.length; c++) {
                    l[c] = l[c] + " " + walker.intpret.stockSource.stocks((String) (l[c]));
                }
                stock_list.setListData(l);
            } else {
                String r[] = { "<no stocks>" };
                stock_list.setListData(r);
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    void quit() {
        System.exit(0);
    }

    public void keyReleased(KeyEvent ev) {
    }

    public void keyTyped(KeyEvent ev) {
    }

    public void keyPressed(KeyEvent ev) {
        if (ev.getKeyCode() == 112) {
            run();
        } else if (ev.getKeyCode() == 123) {
            load();
        } else if (ev.getKeyCode() == 122) {
            save();
        }
    }

    class FilteredStream extends FilterOutputStream {

        MutableAttributeSet style;

        Color def;

        public FilteredStream(Color c) {
            super(new ByteArrayOutputStream());
            style = new SimpleAttributeSet();
            StyleConstants.setBackground(style, c);
        }

        public void write(byte b[]) throws IOException {
            try {
                String aString = new String(b);
                output_doc.insertString(output_doc.getEndPosition().getOffset(), aString, style);
                flush();
            } catch (Exception e) {
            }
        }

        public void write(byte b[], int off, int len) throws IOException {
            try {
                String aString = new String(b, off, len);
                output_doc.insertString(output_doc.getEndPosition().getOffset(), aString, style);
                flush();
            } catch (Exception e) {
            }
        }
    }

    class STLCode extends DefaultStyledDocument {

        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            try {
                super.insertString(offset, str, a);
            } catch (Exception e) {
            }
        }
    }

    public static void main(String[] args) {
        STLGui mw = new STLGui();
    }
}
