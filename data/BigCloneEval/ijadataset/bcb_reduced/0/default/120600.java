import java.io.File;
import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.*;

public class Gui extends JFrame {

    Ui oooeye;

    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private JButton OpenFile = null;

    private JScrollPane jScrollPane = null;

    private JTextArea jTextArea = null;

    void myMain(Ui yewI) {
        oooeye = yewI;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                Gui thisClass = new Gui();
                thisClass.oooeye = oooeye;
                oooeye.setGui(thisClass);
                thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                thisClass.setVisible(true);
            }
        });
    }

    void doAppend(String s) {
        jTextArea.append(s);
    }

    void guiOpenTheFile() {
        oooeye.apend("3");
        int sod = 0;
        File file = null;
        boolean gui = true;
        JFileChooser jFchooser = new JFileChooser();
        sod = jFchooser.showOpenDialog(this);
        file = jFchooser.getSelectedFile();
        jTextArea.append("Opening " + file + " ...\n");
        Pgn pgn = new Pgn();
        pgn.parsePgn(file, oooeye, gui);
        jTextArea.append("\n");
    }

    /**
     * This method initializes OpenFile
     *
     * @return javax.swing.JButton
     */
    private JButton getOpenFile() {
        if (OpenFile == null) {
            OpenFile = new JButton();
            OpenFile.setText("Open File");
            OpenFile.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    guiOpenTheFile();
                }
            });
        }
        return OpenFile;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextArea());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTextArea
     *
     * @return javax.swing.JTextArea
     */
    private JTextArea getJTextArea() {
        if (jTextArea == null) {
            jTextArea = new JTextArea();
        }
        return jTextArea;
    }

    /**
     * This is the default constructor
     */
    public Gui() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setSize(600, 800);
        this.setContentPane(getJContentPane());
        this.setTitle("pgn2fen");
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getOpenFile(), BorderLayout.NORTH);
            jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
        }
        return jContentPane;
    }
}
