package mediathek.gui.dialoge;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import mediathek.Konstanten;
import mediathek.beobachter.EscBeenden;
import mediathek.daten.Daten;

public class DialogHinweis extends javax.swing.JDialog {

    private Daten daten;

    private String url;

    private int check = Konstanten.SYSTEM_HINWEIS_ANZEIGEN_NR;

    private String text = "";

    /** Creates new form HilfeDialog
     * @param parent
     * @param modal
     * @param ddaten
     * @param ttext
     * @param uurl
     */
    public DialogHinweis(java.awt.Frame parent, boolean modal, Daten ddaten, String ttext, String uurl) {
        super(parent, modal);
        url = uurl;
        daten = ddaten;
        text = ttext;
        initComponents();
        this.setTitle("Hilfe");
        initBeob();
    }

    /**
     * 
     * @param parent
     * @param modal
     * @param ddaten
     * @param ttext
     * @param uurl
     * @param ttextCheck
     * @param ccheck
     * @param dialogTitel
     */
    public DialogHinweis(java.awt.Frame parent, boolean modal, Daten ddaten, String ttext, String uurl, String ttextCheck, int ccheck, String dialogTitel) {
        super(parent, modal);
        url = uurl;
        check = ccheck;
        daten = ddaten;
        text = ttext;
        this.setTitle(dialogTitel);
        initComponents();
        jCheckBoxHinweis.setText(ttextCheck);
        jCheckBoxHinweis.setSelected(Boolean.parseBoolean(daten.system[check]));
        initBeob();
    }

    private void initBeob() {
        jButtonOk.addActionListener(new BeobBeenden());
        jCheckBoxHinweis.addActionListener(new BeobHinweis());
        jTextArea1.setText(text);
        if (!url.equals("")) {
            jButtonUrl.setText(url);
            jButtonUrl.addActionListener(new BeobUrl());
        } else {
            jButtonUrl.setVisible(false);
        }
        new EscBeenden(this) {

            @Override
            public void beenden_() {
                beenden();
            }
        };
    }

    private void beenden() {
        this.dispose();
    }

    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButtonOk = new javax.swing.JButton();
        jCheckBoxHinweis = new javax.swing.JCheckBox();
        jButtonUrl = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jTextArea1.setText("\n\n");
        jScrollPane1.setViewportView(jTextArea1);
        jButtonOk.setText("OK");
        jCheckBoxHinweis.setSelected(true);
        jCheckBoxHinweis.setText("Hinweise anzeigen");
        jButtonUrl.setText("Url");
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE).addContainerGap()).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jButtonUrl, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addComponent(jCheckBoxHinweis).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 375, Short.MAX_VALUE).addComponent(jButtonOk, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))).addGap(16, 16, 16)))));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE).addGap(8, 8, 8).addComponent(jButtonUrl).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jButtonOk).addComponent(jCheckBoxHinweis)).addContainerGap()));
        pack();
    }

    private javax.swing.JButton jButtonOk;

    private javax.swing.JButton jButtonUrl;

    private javax.swing.JCheckBox jCheckBoxHinweis;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JTextArea jTextArea1;

    private class BeobUrl implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (Desktop.isDesktopSupported()) {
                Desktop d = Desktop.getDesktop();
                try {
                    if (d.isSupported(Desktop.Action.BROWSE)) {
                        d.browse(new URI(url));
                    }
                } catch (Exception ex) {
                    System.err.println("DialogHinweis.BeobUrl: " + ex.getMessage());
                }
            }
        }
    }

    private class BeobBeenden implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            beenden();
        }
    }

    private class BeobHinweis implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            daten.system[check] = Boolean.toString(jCheckBoxHinweis.isSelected());
            daten.setGeaendertSofort();
        }
    }
}
