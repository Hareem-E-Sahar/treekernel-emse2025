import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Random;
import java.util.Properties;

public class JPractice extends JFrame implements ActionListener, KeyListener {

    JPanel panel;

    JBoard board;

    JPanel contents;

    JLabel lword;

    JLabel ltries;

    JTextField edit;

    JButton bok;

    JButton bopen;

    JButton bmacros;

    JMacros macros;

    JFileChooser openDialog;

    ArrayList session;

    Random random;

    String word;

    int at = 0;

    int ntries = 0;

    int ncorrect = 0;

    boolean[] correct;

    public JPractice() throws IOException {
        setTitle(JText.get("Practice"));
        setSize(500, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Dimension scs = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((scs.width - getWidth()) / 2, (scs.height - getHeight()) / 2);
        contents = new JPanel();
        contents.setLayout(new BorderLayout(8, 8));
        contents.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        getContentPane().add(contents);
        board = new JBoard();
        contents.add(board, BorderLayout.CENTER);
        panel = new JPanel();
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setBorder(border);
        panel.setLayout(new BorderLayout(8, 8));
        contents.add(panel, BorderLayout.PAGE_END);
        edit = new JTextField();
        edit.addKeyListener(this);
        panel.add(edit, BorderLayout.NORTH);
        bok = new JButton(JText.get("Ok"));
        bok.addActionListener(this);
        panel.add(bok, BorderLayout.WEST);
        lword = new JLabel();
        lword.setVisible(false);
        panel.add(lword, BorderLayout.CENTER);
        ltries = new JLabel();
        panel.add(ltries, BorderLayout.SOUTH);
        JPanel east = new JPanel();
        east.setLayout(new BorderLayout(8, 8));
        panel.add(east, BorderLayout.EAST);
        bopen = new JButton(JText.get("Open ..."));
        bopen.addActionListener(this);
        east.add(bopen, BorderLayout.EAST);
        bmacros = new JButton(JText.get("Macros ..."));
        bmacros.addActionListener(this);
        east.add(bmacros, BorderLayout.WEST);
        macros = new JMacros();
        openDialog = new JFileChooser();
        session = new ArrayList();
        random = new Random();
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_ENTER) okExec();
    }

    void okExec() {
        if (board.hasImage()) {
            lword.setText(word);
            applyMacros();
            ntries++;
            if (edit.getText().equals(word)) {
                lword.setForeground(Color.green);
                ncorrect++;
                correct[at] = true;
            } else {
                lword.setForeground(Color.red);
            }
            lword.setVisible(true);
            newWord();
        }
    }

    void applyMacros() {
        Properties props = macros.getMacros();
        Enumeration en = props.propertyNames();
        String caption = edit.getText();
        String froms, tos;
        int i;
        while (en.hasMoreElements()) {
            i = 0;
            froms = (String) en.nextElement();
            tos = props.getProperty(froms);
            while (-1 < (i = caption.indexOf(froms, i))) {
                caption = caption.substring(0, i) + tos + caption.substring(i + froms.length());
                i += tos.length();
            }
        }
        edit.setText(caption);
    }

    public void actionPerformed(ActionEvent e) {
        if (bopen == e.getSource()) {
            if (JFileChooser.APPROVE_OPTION == openDialog.showOpenDialog(this)) openSession(openDialog.getSelectedFile());
        } else if (bok == e.getSource()) {
            okExec();
        } else if (bmacros == e.getSource()) {
            macros.setVisible(true);
        }
    }

    public void openSession(File file) {
        String imgname;
        session.clear();
        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            String line;
            String s;
            while (null != (s = input.readLine())) {
                line = new String(s.getBytes(), "UTF-8");
                session.add(line);
            }
            imgname = file.getParentFile().getCanonicalPath() + File.separator + session.get(0);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, JText.get("Cannot open file: ") + file.getName());
            return;
        }
        if (!board.load(new File(imgname))) error(JText.get("Cannot load image: ") + imgname);
        repaint();
        lword.setVisible(false);
        correct = new boolean[session.size()];
        for (int i = 0; i < correct.length; ++i) correct[i] = false;
        ncorrect = 0;
        ntries = 0;
        at = 1;
        newWord();
    }

    void newWord() {
        if (ncorrect >= session.size() - 1) {
            refreshShape();
            JOptionPane.showMessageDialog(this, JText.get("Learned all words"));
            return;
        }
        int n = 1 + random.nextInt(session.size() - ncorrect - 1);
        while (0 < n) {
            at++;
            if (at >= session.size()) at = 1;
            if (!correct[at]) n--;
        }
        String str = ((String) session.get(at)).trim();
        int i = str.indexOf(')');
        if (0 > i) {
            error(JText.get("Bad line format: ") + at);
            return;
        }
        word = str.substring(i + 1).trim();
        str = str.substring(1, i);
        i = str.indexOf(',');
        int sw, sh, sl, st;
        try {
            sl = Integer.parseInt(str.substring(0, i).trim());
            str = str.substring(i + 1);
            i = str.indexOf(',');
            if (0 > i) {
                st = Integer.parseInt(str.trim());
                sl -= 16;
                st -= 16;
                sw = 32;
                sh = 32;
            } else {
                st = Integer.parseInt(str.substring(0, i).trim());
                str = str.substring(i + 1);
                i = str.indexOf(',');
                sw = Integer.parseInt(str.substring(0, i).trim());
                str = str.substring(i + 1);
                sh = Integer.parseInt(str.trim());
            }
        } catch (NumberFormatException ex) {
            error(JText.get("Bad line format: ") + at);
            return;
        }
        board.moveMarker(sl, st, sw, sh);
        edit.selectAll();
        refreshShape();
    }

    void refreshShape() {
        ltries.setText(ntries + JText.get(" tries"));
    }

    void error(String str) {
        JOptionPane.showMessageDialog(this, str, JText.get("Error"), JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            JPractice practice = new JPractice();
            practice.setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
