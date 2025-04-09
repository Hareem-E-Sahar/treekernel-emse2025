package zen_group;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.SystemColor;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import zen_group.mdl.Part;
import zen_group.mdl.XlsFileFilter;
import zen_group.mdl.XlsPartReader;
import zen_group.mdl.XlsPartWriter;

public class Interface extends javax.swing.JFrame {

    Vector<Part> mParts = new Vector<Part>();

    boolean stopThread;

    int progress;

    private Simulate simulate;

    static class MatchingDocument extends PlainDocument {

        public MatchingDocument(String regex) {
            mRegex = regex;
        }

        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            try {
                String s = getText(0, getLength());
                s = s.substring(0, offset) + str + s.substring(offset);
                if (!s.matches(mRegex)) throw new Exception();
            } catch (Exception e) {
                return;
            }
            super.insertString(offset, str, a);
        }

        private String mRegex;
    }

    abstract static class TextChangeListener implements javax.swing.event.DocumentListener {

        public abstract void onChange(String text);

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            String s = "";
            try {
                s = e.getDocument().getText(0, e.getDocument().getLength());
            } catch (Exception ignore) {
                return;
            }
            onChange(s);
        }

        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            String s = "";
            try {
                s = e.getDocument().getText(0, e.getDocument().getLength());
            } catch (Exception ignore) {
                return;
            }
            onChange(s);
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            String s = "";
            try {
                s = e.getDocument().getText(0, e.getDocument().getLength());
            } catch (Exception ignore) {
                return;
            }
            onChange(s);
        }
    }

    static class PartCellRenderer extends javax.swing.JPanel implements javax.swing.ListCellRenderer {

        javax.swing.JLabel mTitle = new javax.swing.JLabel();

        javax.swing.JLabel mProb = new javax.swing.JLabel();

        public PartCellRenderer() {
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));
            setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
            mTitle.setOpaque(true);
            mProb.setOpaque(true);
            add(mTitle);
            add(javax.swing.Box.createHorizontalGlue());
            add(mProb);
        }

        public java.awt.Component getListCellRendererComponent(javax.swing.JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Part part = (Part) value;
            mTitle.setText(part.title());
            mProb.setText(Math.round(part.faultProb() * 100) + "%");
            if (isSelected) {
                mTitle.setForeground(list.getSelectionForeground());
                mTitle.setBackground(list.getSelectionBackground());
                mProb.setForeground(list.getSelectionForeground());
                mProb.setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
                setBackground(list.getSelectionBackground());
            } else {
                mTitle.setForeground(list.getForeground());
                mTitle.setBackground(list.getBackground());
                mProb.setForeground(list.getForeground());
                mProb.setBackground(list.getBackground());
                setForeground(list.getForeground());
                setBackground(list.getBackground());
            }
            mTitle.setEnabled(list.isEnabled());
            mTitle.setFont(list.getFont());
            mProb.setEnabled(list.isEnabled());
            mProb.setFont(list.getFont());
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            return this;
        }
    }

    public Interface() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        setResizable(false);
        initComponents();
        jProgressBar2.setVisible(false);
        jButtonCancel.setVisible(false);
        centerScreen();
        getContentPane().setBackground(SystemColor.control);
    }

    private void centerScreen() {
        Dimension screenSize = getToolkit().getScreenSize();
        setLocation((int) (screenSize.getWidth() - getWidth()) / 2, (int) (screenSize.getHeight() - getHeight()) / 2);
    }

    private void initComponents() {
        jFileChooser1 = new javax.swing.JFileChooser();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jCheckBox1 = new javax.swing.JCheckBox();
        jTextField3 = new javax.swing.JTextField();
        jProgressBar2 = new javax.swing.JProgressBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jButtonCancel = new javax.swing.JButton();
        jFileChooser1.setFileFilter(new XlsFileFilter());
        jFileChooser1.setAcceptAllFileFilterUsed(false);
        jFileChooser1.setMultiSelectionEnabled(false);
        jFileChooser1.setDialogTitle("Pasirinkite duomenų failą..");
        jFileChooser1.setApproveButtonToolTipText("Pasirinkti nurodytą failą");
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 100, Short.MAX_VALUE));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 100, Short.MAX_VALUE));
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        jLabel1.setText("Kaina:");
        jLabel3.setText("Gedimo tikmybė:");
        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jTextField1.setEditable(false);
        jTextField1.setDocument(new MatchingDocument("^\\d+(\\.\\d*)?$"));
        jTextField1.setText("1");
        jTextField2.setEditable(false);
        jTextField2.setDocument(new MatchingDocument("^\\d+(\\.\\d*)?$"));
        jTextField2.setText("1");
        jTextField2.getDocument().addDocumentListener(new TextChangeListener() {

            public void onChange(String text) {
                try {
                    Part.faultProbFactor(Float.parseFloat(text));
                    jScrollPane1.repaint();
                } catch (Exception ignore) {
                }
            }
        });
        jLabel6.setText("x");
        jLabel7.setText("x");
        jButton1.setText("Simuliuoti ateinantį ketvirtį 1 kartą");
        jButton1.setActionCommand("karta");
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jLabel2.setText("Simuliacijų skaičius:");
        jLabel9.setText("x");
        jButton2.setText("Simuliuoti ateinantį ketvirtį ");
        jButton2.setActionCommand("daug");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jList1.setCellRenderer(new PartCellRenderer());
        jList1.setBackground(java.awt.SystemColor.text);
        jList1.setEnabled(false);
        jScrollPane1.setViewportView(jList1);
        jCheckBox1.setText("Naudoti dinaminį remonto terminą");
        jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBox1.setEnabled(false);
        jCheckBox1.setSelected(true);
        jTextField3.setEditable(false);
        jTextField3.setDocument(new MatchingDocument("^\\d+$"));
        jTextField3.setText("100");
        jMenu1.setText("Failas");
        jMenuItem1.setText("Įkelti duomenis");
        jMenuItem1.setActionCommand("Open");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);
        jMenuBar1.add(jMenu1);
        jMenu2.setText("Pagalba");
        jMenuItem3.setText("Žinynas");
        jMenuItem3.setActionCommand("help");
        jMenu2.add(jMenuItem3);
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenuItem2.setText("Apie");
        jMenuItem2.setActionCommand("About");
        jMenu2.add(jMenuItem2);
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenuBar1.add(jMenu2);
        setJMenuBar(jMenuBar1);
        jButtonCancel.setText("Atšaukti");
        jButtonCancel.setActionCommand("cancel");
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 279, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(16, 16, 16).addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(22, 22, 22).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(12, 12, 12).addComponent(jLabel2).addGap(75, 75, 75).addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel9)).addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addGap(12, 12, 12).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel1).addComponent(jLabel3)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(jTextField2, javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel6)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel7))).addGap(15, 15, 15)).addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE))).addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addComponent(jProgressBar2, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE).addGap(5, 5, 5).addComponent(jButtonCancel, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel1).addComponent(jLabel6).addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel3).addComponent(jLabel7).addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(19, 19, 19).addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(17, 17, 17).addComponent(jButton1).addGap(28, 28, 28).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2).addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel9)).addGap(16, 16, 16).addComponent(jButton2).addGap(21, 21, 21).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jProgressBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jButtonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE));
        pack();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Klaida", JOptionPane.ERROR_MESSAGE);
    }

    public void showAbout() {
        JOptionPane.showMessageDialog(this, "dr._gordon_freeman v1.1\n\n" + "(c) 2007 zen_group", "Apie", JOptionPane.INFORMATION_MESSAGE);
    }

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getActionCommand() == "cancel") {
            if (mSimulation != null) mSimulation.cancel(false);
            jButtonCancel.setEnabled(false);
        }
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getActionCommand() == "daug") {
            System.out.println("Simuliuojam daug kartu");
            int count = 1;
            try {
                count = Integer.parseInt(jTextField3.getText());
                if (count <= 0) throw new Exception();
            } catch (Exception e) {
                showError("Nurodytas neteisingas simuliacijų skaičius.\n\n" + "Simuliacijų skaičius turi būti sveikas teigiamas skaičius.");
            }
            try {
                mSimulation = new SimulateMany(count);
                mSimulation.execute();
            } catch (Exception e) {
            }
        }
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getActionCommand() == "karta") {
            System.out.println("Simuliuojam karta");
            try {
                mSimulation = new SimulateOnce();
                mSimulation.execute();
            } catch (Exception e) {
            }
        }
    }

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getActionCommand() == "Open") {
            int retval = jFileChooser1.showOpenDialog(Interface.this);
            if (retval == JFileChooser.APPROVE_OPTION) {
                File dataFile = jFileChooser1.getSelectedFile();
                try {
                    mParts = (new XlsPartReader(dataFile)).getItems();
                    jButton1.setEnabled(true);
                    jTextField1.setEditable(true);
                    jTextField2.setEditable(true);
                    jList1.setEnabled(true);
                    jTextField3.setEditable(true);
                    jButton2.setEnabled(true);
                    jCheckBox1.setEnabled(true);
                    drawList();
                    jList1.setSelectionInterval(0, mParts.size() - 1);
                } catch (Exception e) {
                    showError("Nurodytas neteisingas duomenų failas.");
                    System.err.println(e.toString());
                }
            }
        }
    }

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getActionCommand() == "About") {
            showAbout();
        }
    }

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getActionCommand() == "help") {
            try {
                open(new File("help.chm"));
            } catch (Exception e) {
                e.printStackTrace();
                showError("Nepavyko atidaryti žinyno");
            }
        }
    }

    protected void open(File file) throws Exception {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            File fixedFile = new File(file.getAbsoluteFile().toString()) {

                public URI toURI() {
                    try {
                        return new URI("file://" + getAbsolutePath());
                    } catch (Exception e) {
                        return super.toURI();
                    }
                }
            };
            Desktop.getDesktop().open(fixedFile);
        }
    }

    abstract class Simulate extends SwingWorker<Void, Void> {

        public Void doInBackground() {
            try {
                float factor = Float.parseFloat(jTextField1.getText());
                if (factor <= 0.0) throw new Exception();
                Part.priceFactor();
            } catch (Exception e) {
                showError("Nurodytas neteisingas kainos daugiklis.\n\n" + "Nurodykite teigiamą skaičių.");
                return null;
            }
            try {
                float factor = Float.parseFloat(jTextField2.getText());
                if (factor <= 0.0) throw new Exception();
                Part.faultProbFactor(factor);
            } catch (Exception e) {
                showError("Nurodytas neteisingas gedimo tikimybės daugiklis.\n\n" + "Nurodykite teigiamą skaičių.");
                return null;
            }
            try {
                Part.warranty(jCheckBox1.isSelected());
                setProgress(0);
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                jProgressBar2.setVisible(true);
                jProgressBar2.setStringPainted(true);
                jButtonCancel.setEnabled(true);
                jButtonCancel.setVisible(true);
                Vector<Part> parts = selectedParts();
                jProgressBar2.setMaximum(parts.size());
                jMenuItem1.setEnabled(false);
                jButton1.setEnabled(false);
                jTextField1.setEditable(false);
                jTextField2.setEditable(false);
                jList1.setEnabled(false);
                jTextField3.setEditable(false);
                jButton2.setEnabled(false);
                jCheckBox1.setEnabled(false);
                doSimulation(parts);
            } catch (Exception e) {
                System.err.println(e.toString());
                e.printStackTrace();
            }
            jMenuItem1.setEnabled(true);
            jButton1.setEnabled(true);
            jTextField1.setEditable(true);
            jTextField2.setEditable(true);
            jList1.setEnabled(true);
            jTextField3.setEditable(true);
            jButton2.setEnabled(true);
            jCheckBox1.setEnabled(true);
            jProgressBar2.setVisible(false);
            jButtonCancel.setVisible(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            return null;
        }

        protected File selectResultsFile() throws Exception {
            JFileChooser fileChooser = getFileChooser();
            File selectedFile;
            while (true) {
                if (JFileChooser.APPROVE_OPTION != fileChooser.showSaveDialog(Interface.this)) break;
                selectedFile = fileChooser.getSelectedFile();
                if (!selectedFile.exists() || (selectedFile.exists() && confirmOverwrite(selectedFile))) {
                    return selectedFile;
                }
            }
            throw new Exception("Result file choose operation cancelled");
        }

        protected Vector<Part> selectedParts() {
            Object[] selected = jList1.getSelectedValues();
            Vector<Part> parts = new Vector<Part>(selected.length);
            try {
                for (Object obj : selected) {
                    if (null != obj) parts.add((Part) obj);
                }
            } catch (Exception ignore) {
            }
            return parts;
        }

        protected abstract void doSimulation(Vector<Part> parts);

        private boolean confirmOverwrite(File file) {
            return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null, "Nurodėte egzistuojantį failą." + " Ar tikrai norite jį perrašyti?", "Patvirtinkite pasirinkimą", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        }

        private JFileChooser getFileChooser() {
            JFileChooser mFileChooser = new JFileChooser();
            mFileChooser.setFileFilter(new XlsFileFilter());
            mFileChooser.setAcceptAllFileFilterUsed(false);
            mFileChooser.setMultiSelectionEnabled(false);
            mFileChooser.setCurrentDirectory(jFileChooser1.getSelectedFile().getParentFile());
            mFileChooser.setDialogTitle("Pasirinkite failą simuliacijos rezultatams..");
            mFileChooser.setApproveButtonToolTipText("Pasirinkti nurodytą failą");
            return mFileChooser;
        }

        protected boolean mCanceled = false;

        private JFileChooser mFileChooser;
    }

    class SimulateOnce extends Simulate {

        protected void doSimulation(Vector<Part> parts) {
            HashMap<Part, Part.Period> results = new HashMap<Part, Part.Period>(parts.size());
            for (int i = 0; !isCancelled() && i < parts.size(); ++i) {
                try {
                    Part p = parts.get(i);
                    System.out.println(i + ":" + p);
                    jProgressBar2.setValue(i);
                    results.put(p, p.simulateOnce());
                } catch (Exception ignore) {
                }
            }
            if (isCancelled()) return;
            while (true) {
                File file;
                try {
                    file = selectResultsFile();
                } catch (Exception e) {
                    return;
                }
                try {
                    (new XlsPartWriter(file)).write(results);
                    open(file);
                    return;
                } catch (FileNotFoundException e) {
                    showError("Nepavyko įrašyti rezultato į " + file.getAbsolutePath() + "\n\n" + "Patikrinkite ar failas nėra atidarytas kitoje programoje" + " arba kreipkitės į sistemos administratorių.");
                } catch (Exception e) {
                    System.err.println(e.toString());
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    class SimulateMany extends Simulate {

        private int mCount = 1;

        public SimulateMany(int count) {
            mCount = count;
        }

        protected void doSimulation(Vector<Part> parts) {
            HashMap<Part, Integer> results = new HashMap<Part, Integer>(parts.size());
            for (int i = 0; !isCancelled() && i < parts.size(); ++i) {
                try {
                    Part p = parts.get(i);
                    System.out.println(i + ":" + p);
                    jProgressBar2.setValue(i);
                    results.put(p, p.simulate(mCount));
                } catch (Exception ignore) {
                }
            }
            if (isCancelled()) return;
            while (true) {
                File file;
                try {
                    file = selectResultsFile();
                } catch (Exception e) {
                    return;
                }
                try {
                    (new XlsPartWriter(file)).write(results, mCount);
                    open(file);
                    return;
                } catch (FileNotFoundException e) {
                    showError("Nepavyko įrašyti rezultato į " + file.getAbsolutePath() + "\n\n" + "Patikrinkite ar failas nėra atidarytas kitoje programoje" + " arba kreipkitės į sistemos administratorių.");
                } catch (Exception e) {
                    System.err.println(e.toString());
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public void drawList() {
        jList1.setListData(mParts);
    }

    private Simulate mSimulation;

    private javax.swing.JButton jButton1;

    private javax.swing.JButton jButton2;

    private javax.swing.JCheckBox jCheckBox1;

    private javax.swing.JFileChooser jFileChooser1;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel6;

    private javax.swing.JLabel jLabel7;

    private javax.swing.JLabel jLabel9;

    private javax.swing.JList jList1;

    private javax.swing.JMenu jMenu1;

    private javax.swing.JMenu jMenu2;

    private javax.swing.JMenuBar jMenuBar1;

    private javax.swing.JMenuItem jMenuItem1;

    private javax.swing.JMenuItem jMenuItem2;

    private javax.swing.JMenuItem jMenuItem3;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JProgressBar jProgressBar2;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JSeparator jSeparator1;

    private javax.swing.JTextField jTextField1;

    private javax.swing.JTextField jTextField2;

    private javax.swing.JTextField jTextField3;

    private javax.swing.JButton jButtonCancel;
}
