package mediathek.gui.dialoge;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import mediathek.Konstanten;
import mediathek.beobachter.EscBeenden;
import mediathek.daten.Daten;
import mediathek.filme.DatenFilm;

public class DialogDatenFilm extends javax.swing.JDialog {

    private Daten daten;

    private DatenFilm aktFilm = null;

    private JTextField[] textarray = new JTextField[Konstanten.FILME_MAX_ELEM];

    /** Creates new form DialogSerienbrief
     * @param parent
     * @param modal
     * @param d Daten
     * @param aaktFilm 
     */
    public DialogDatenFilm(java.awt.Frame parent, boolean modal, Daten d) {
        super(parent, modal);
        initComponents();
        daten = d;
        jButtonBrowser.addActionListener(new BeobTitel());
        for (int i = 0; i < Konstanten.FILME_MAX_ELEM; ++i) {
            textarray[i] = new JTextField();
        }
        jButtonOk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                beenden();
            }
        });
        new EscBeenden(this) {

            @Override
            public void beenden_() {
                beenden();
            }
        };
    }

    public void setAktFilm(DatenFilm aaktFilm) {
        aktFilm = aaktFilm;
        jPanelExtra.removeAll();
        if (aktFilm != null) {
            setExtra();
            jPanelExtra.updateUI();
            jButtonBrowser.setToolTipText(textarray[Konstanten.FILM_URL_THEMA_NR].getText());
            jButtonBrowser.setEnabled(!textarray[Konstanten.FILM_URL_THEMA_NR].getText().equals(""));
        }
    }

    public void setVis() {
        this.setVisible(true);
        this.toFront();
    }

    private void setExtra() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 10, 10, 5);
        jPanelExtra.setLayout(gridbag);
        int zeile = 0;
        for (int i = 0; i < Konstanten.FILME_MAX_ELEM; ++i) {
            if (!daten.debug) {
                if (i == Konstanten.FILM_ZIEL_DATEI_NR || i == Konstanten.FILM_DATEI_NR || i == Konstanten.FILM_ZIEL_PFAD_NR || i == Konstanten.FILM_ZIEL_PFAD_DATEI_NR || i == Konstanten.FILM_URL_RTMP_NR || i == Konstanten.FILM_URL_ORG_NR) {
                    continue;
                }
            }
            addExtraFeld(i, gridbag, c, jPanelExtra, aktFilm.arr);
            ++zeile;
            c.gridy = zeile;
        }
    }

    private void addExtraFeld(int i, GridBagLayout gridbag, GridBagConstraints c, JPanel panel, String[] item) {
        c.gridx = 0;
        c.weightx = 0;
        JLabel label = new JLabel(Konstanten.FILME_COLUMN_NAMES[i] + ": ");
        gridbag.setConstraints(label, c);
        panel.add(label);
        c.gridx = 1;
        c.weightx = 10;
        textarray[i].setEditable(false);
        textarray[i].setText(item[i]);
        gridbag.setConstraints(textarray[i], c);
        panel.add(textarray[i]);
    }

    private void titelOeffnen() {
        if (!textarray[Konstanten.FILM_URL_THEMA_NR].getText().equals("")) {
            if (Desktop.isDesktopSupported()) {
                Desktop d = Desktop.getDesktop();
                try {
                    if (d.isSupported(Desktop.Action.BROWSE)) {
                        d.browse(new URI(textarray[Konstanten.FILM_URL_THEMA_NR].getText()));
                    }
                } catch (Exception ex) {
                }
            }
        }
    }

    private void beenden() {
        this.dispose();
    }

    private void initComponents() {
        jButtonBrowser = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanelExtra = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jButtonBrowser.setText("Thema im Browser öffnen");
        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanelExtra.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        javax.swing.GroupLayout jPanelExtraLayout = new javax.swing.GroupLayout(jPanelExtra);
        jPanelExtra.setLayout(jPanelExtraLayout);
        jPanelExtraLayout.setHorizontalGroup(jPanelExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 533, Short.MAX_VALUE));
        jPanelExtraLayout.setVerticalGroup(jPanelExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 388, Short.MAX_VALUE));
        jScrollPane1.setViewportView(jPanelExtra);
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 542, Short.MAX_VALUE).addContainerGap()));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE).addContainerGap()));
        jButtonOk.setText("Ok");
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addComponent(jButtonBrowser).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 251, Short.MAX_VALUE).addComponent(jButtonOk, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jButtonBrowser).addComponent(jButtonOk)).addGap(12, 12, 12)));
        pack();
    }

    private javax.swing.JButton jButtonBrowser;

    private javax.swing.JButton jButtonOk;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanelExtra;

    private javax.swing.JScrollPane jScrollPane1;

    private class BeobTitel implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            titelOeffnen();
        }
    }
}
