import java.awt.event.*;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.*;

public class UnistrokTrainer extends javax.swing.JFrame implements ActionListener {

    private javax.swing.JButton btnPrevious;

    private javax.swing.JButton btnNext;

    private javax.swing.JButton btnClear;

    private javax.swing.JLabel lblToWrite;

    private javax.swing.JLabel lblUnicodeCharacter;

    private javax.swing.JLabel lblCodepoint;

    private DrawPanel panelWriterRecognizer;

    private Unistrok unistrok;

    private int point = 0;

    private static UnistrokTrainer instance;

    public Unistrok getUnistrok() {
        return unistrok;
    }

    public Unistrok.Character getCurrentCharacter() {
        if (unistrok != null && unistrok.characters != null && unistrok.characters.get(point) != null) {
            return unistrok.characters.get(point);
        } else {
            return null;
        }
    }

    private void initComponents() {
        JMenuBar menuBar;
        JMenu fileMenu;
        JMenuItem open, save, exit;
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        open = new JMenuItem("Open");
        save = new JMenuItem("Save");
        exit = new JMenuItem("Exit");
        fileMenu.add(open);
        fileMenu.add(save);
        fileMenu.add(exit);
        open.addActionListener(this);
        save.addActionListener(this);
        exit.addActionListener(this);
        this.setJMenuBar(menuBar);
        panelWriterRecognizer = new DrawPanel();
        lblCodepoint = new javax.swing.JLabel();
        btnClear = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnPrevious = new javax.swing.JButton();
        lblUnicodeCharacter = new javax.swing.JLabel();
        lblToWrite = new javax.swing.JLabel();
        btnNext.setActionCommand("Next Character");
        btnPrevious.setActionCommand("Previous Character");
        btnClear.setActionCommand("Clear");
        btnClear.addActionListener(panelWriterRecognizer);
        btnNext.addActionListener(this);
        btnPrevious.addActionListener(this);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        lblCodepoint.setText("Unicode Codepoint: U+" + Integer.toHexString((int) point));
        btnClear.setText("Clear");
        btnNext.setText("→");
        btnPrevious.setText("←");
        lblUnicodeCharacter.setFont(new java.awt.Font("SimSun", 0, 48));
        lblToWrite.setText("Strokes to write: ");
        lblUnicodeCharacter.setText(String.valueOf(point));
        panelWriterRecognizer.setBackground(new java.awt.Color(204, 204, 204));
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(panelWriterRecognizer);
        panelWriterRecognizer.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 209, Short.MAX_VALUE));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 198, Short.MAX_VALUE));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(panelWriterRecognizer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(lblCodepoint).addComponent(lblUnicodeCharacter, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE))).addComponent(lblToWrite).addGroup(layout.createSequentialGroup().addComponent(btnPrevious).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 115, Short.MAX_VALUE).addComponent(btnClear).addGap(110, 110, 110).addComponent(btnNext))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(btnPrevious).addComponent(btnNext).addComponent(btnClear)).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE).addComponent(panelWriterRecognizer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createSequentialGroup().addGap(51, 51, 51).addComponent(lblCodepoint).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(lblUnicodeCharacter, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE))).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(lblToWrite).addContainerGap()));
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Next Character") && unistrok != null) {
            point++;
            lblCodepoint.setText("Unicode Codepoint: U+" + Integer.toHexString(unistrok.characters.get(point).codepoint));
        } else if (e.getActionCommand().equals("Previous Character") && unistrok != null) {
            point--;
            lblCodepoint.setText("Unicode Codepoint: U+" + Integer.toHexString(unistrok.characters.get(point).codepoint));
        } else if (e.getActionCommand().equals("Open")) {
            JFileChooser fc = new JFileChooser();
            int retval = fc.showOpenDialog(this);
            if (retval == JFileChooser.APPROVE_OPTION) {
                try {
                    unistrok = new Unistrok(fc.getSelectedFile().getPath());
                    point = 0;
                } catch (IOException ex) {
                    System.out.println("Problem loading " + fc.getSelectedFile().getName() + ": " + ex.getMessage());
                }
            }
        } else if (e.getActionCommand().equals("Save")) {
            JFileChooser fc = new JFileChooser();
            int retval = fc.showOpenDialog(this);
            if (retval == JFileChooser.APPROVE_OPTION) {
                try {
                    unistrok.saveFile(fc.getSelectedFile().getPath());
                } catch (IOException ex) {
                    System.out.println("Problem loading " + fc.getSelectedFile().getName() + ": " + ex.getMessage());
                }
            }
        } else if (e.getActionCommand().equals("Exit")) {
        }
        lblUnicodeCharacter.setText(String.valueOf((char) unistrok.characters.get(point).codepoint));
        updateStrokes();
    }

    private UnistrokTrainer() {
        point = 0;
        initComponents();
    }

    public static UnistrokTrainer getInstance() {
        if (UnistrokTrainer.instance == null) {
            instance = new UnistrokTrainer();
        }
        return instance;
    }

    public void updateStrokes() {
        String towrite = "Strokes to write: ";
        for (Integer i : getCurrentCharacter().strokes) {
            towrite = towrite.concat(i.toString()) + " ";
        }
        this.lblToWrite.setText(towrite);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                UnistrokTrainer.getInstance().setVisible(true);
            }
        });
    }
}
