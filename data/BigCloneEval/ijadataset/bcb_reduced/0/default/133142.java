import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import guidoengine.*;

public class guidoviewer {

    public static void main(String[] args) {
        JFrame win = new guidoviewerGUI();
        win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        win.setVisible(true);
    }
}

class scorePanel extends Canvas implements Printable {

    static {
        try {
            System.loadLibrary("jniGUIDOEngine");
            guido.Init("Guido2", "Times");
            if (guido.xml2gmn()) System.out.println("libMusicXML v." + guido.musicxmlversion() + " with GMN converter v." + guido.musicxml2guidoversion());
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load.\n" + e);
        }
    }

    guidoscore m_gmnscore;

    public scorePanel() {
        m_gmnscore = new guidoscore();
    }

    public Dimension getPreferredSize() {
        return new Dimension(500, 600);
    }

    public void setGMN(String str, boolean gmncode) {
        m_gmnscore.close();
        int err = gmncode ? m_gmnscore.ParseString(str) : m_gmnscore.ParseFile(str);
        if (err != guido.guidoNoErr) {
            String msg = gmncode ? new String("Error reading string:\n") + guido.GetErrorString(err) : new String("Error opening ") + str + ":\n" + guido.GetErrorString(err);
            if (err == guido.guidoErrParse) {
                if (guido.xml2gmn() && !gmncode) {
                    System.out.println("try xml 2 guido conversion:");
                    String gmn = guido.xml2gmn(str);
                    setGMN(gmn, true);
                    return;
                }
                msg += " line " + guido.GetParseErrorLine();
            }
            JOptionPane.showMessageDialog(this, msg);
            m_gmnscore = null;
        } else {
            err = m_gmnscore.AR2GR();
            if (err != guido.guidoNoErr) {
                JOptionPane.showMessageDialog(this, "Error converting AR to GR " + str + ":\n" + guido.GetErrorString(err));
                m_gmnscore = null;
            } else {
                m_gmnscore.ResizePageToMusic();
                repaint();
            }
        }
    }

    public void paint(Graphics g) {
        if (m_gmnscore.fGRHandler != 0) {
            guidodrawdesc desc = new guidodrawdesc(getSize().width, getSize().height);
            int ret = m_gmnscore.Draw(g, getSize().width, getSize().height, desc, new guidopaint());
            if (ret != guido.guidoNoErr) System.err.println("error drawing score: " + guido.GetErrorString(ret));
        }
    }

    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page >= m_gmnscore.GetPageCount()) return NO_SUCH_PAGE;
        if (m_gmnscore.fGRHandler != 0) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());
            int w = (int) pf.getImageableWidth();
            int h = (int) pf.getImageableHeight();
            guidodrawdesc desc = new guidodrawdesc(w, h);
            desc.fPage = page + 1;
            int ret = m_gmnscore.Draw(g, w, h, desc, new guidopaint());
            if (ret != guido.guidoNoErr) System.err.println("error printing score: " + guido.GetErrorString(ret));
        }
        return PAGE_EXISTS;
    }
}

class guidoviewerGUI extends JFrame {

    String m_Title = "Guido Viewer Java";

    Menu m_fileMenu = new Menu("File");

    MenuItem m_openItem = new MenuItem("Open");

    MenuItem m_printItem = new MenuItem("Print");

    MenuItem m_quitItem = new MenuItem("Quit");

    scorePanel m_score = new scorePanel();

    public guidoviewerGUI() {
        m_openItem.addActionListener(new OpenAction(this));
        m_printItem.addActionListener(new PrintAction(m_score));
        m_quitItem.addActionListener(new QuitAction());
        MenuBar menubar = new MenuBar();
        menubar.add(m_fileMenu);
        m_fileMenu.add(m_openItem);
        m_fileMenu.add(m_printItem);
        m_fileMenu.addSeparator();
        m_fileMenu.add(m_quitItem);
        JPanel content = new JPanel();
        content.setBackground(Color.white);
        content.setLayout(new BorderLayout());
        content.add(m_score, BorderLayout.CENTER);
        this.setContentPane(content);
        this.setMenuBar(menubar);
        this.setTitle(m_Title);
        this.pack();
    }

    public void setGMNFile(File file) {
        this.setTitle(m_Title + " - " + file.getName());
        m_score.setGMN(file.getPath(), false);
    }

    class OpenAction implements ActionListener {

        guidoviewerGUI m_viewer;

        public OpenAction(guidoviewerGUI viewer) {
            m_viewer = viewer;
        }

        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) m_viewer.setGMNFile(chooser.getSelectedFile());
        }
    }

    class PrintAction implements ActionListener {

        scorePanel m_score;

        public PrintAction(scorePanel score) {
            m_score = score;
        }

        public void actionPerformed(ActionEvent e) {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(m_score);
            boolean ok = job.printDialog();
            if (ok) {
                try {
                    job.print();
                } catch (PrinterException ex) {
                    System.err.println("printing exception: " + ex);
                }
            }
        }
    }

    class QuitAction implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
}
