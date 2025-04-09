package mediathek.gui.menue;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import mediathek.Funktionen;
import mediathek.beobachter.Listener;
import mediathek.daten.Daten;
import mediathek.filme.FilmListener;
import mediathek.gui.PanelVorlage;
import mediathek.io.GetUrl;

public class PanelSenderLadenInfo extends PanelVorlage {

    private Listener seitenListener;

    private JProgressBar[] jProgressBarThread;

    private JProgressBar[] jProgressBarErledigt;

    private JLabel[] jLabel;

    private int[] maximus;

    private int[] progress;

    private int[] threads;

    private String start = "";

    private int[] prozent;

    String[] sendername;

    /**
     * Creates new form GuiFeed
     * @param d
     */
    public PanelSenderLadenInfo(Daten d) {
        super(d);
        initComponents();
        daten = d;
        setText();
        init();
        seitenListener = new Listener() {

            @Override
            public void progress() {
                setText();
            }
        };
        GetUrl.addAdListener(seitenListener);
    }

    @Override
    public void neuLaden() {
        setText();
    }

    private void init() {
        String[] tmp = daten.filmeLaden.getSenderNamen();
        sendername = new String[tmp.length + 1];
        for (int i = 0; i < tmp.length; ++i) {
            sendername[i] = tmp[i];
        }
        sendername[tmp.length] = "Podcast";
        int max = sendername.length;
        jProgressBarThread = new JProgressBar[max];
        jProgressBarErledigt = new JProgressBar[max];
        jLabel = new JLabel[max];
        maximus = new int[max];
        progress = new int[max];
        threads = new int[max];
        daten.filmeLaden.addAdListener(new BeobachterLaden());
        daten.feedReaderPods.addAdListener(new BeobachterLaden());
        jPanelExtra.removeAll();
        prozent = new int[max];
        for (int i = 0; i < max; ++i) {
            jProgressBarThread[i] = new JProgressBar();
            jProgressBarErledigt[i] = new JProgressBar();
            jProgressBarErledigt[i].setVisible(true);
            jProgressBarThread[i].setVisible(true);
            jProgressBarThread[i].setIndeterminate(false);
            jLabel[i] = new JLabel();
        }
        extra();
    }

    private void extra() {
        jPanelExtra.removeAll();
        jPanelExtra.updateUI();
        int maxX = sendername.length;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 10, 4, 10);
        jPanelExtra.setLayout(gridbag);
        addTitel(gridbag, c);
        for (int i = 0; i < maxX; ++i) {
            addExtraZeile(i, gridbag, c);
            jLabel[i].setText(sendername[i]);
        }
    }

    private void addTitel(GridBagLayout gridbag, GridBagConstraints c) {
        c.gridy = 0;
        c.gridx = 0;
        JLabel label;
        c.weightx = 1;
        gridbag.setConstraints(label = new JLabel(" Sender "), c);
        jPanelExtra.add(label);
        c.gridx = 1;
        c.weightx = 10;
        gridbag.setConstraints(label = new JLabel(" Threads "), c);
        jPanelExtra.add(label);
        c.gridx = 2;
        c.weightx = 100;
        gridbag.setConstraints(label = new JLabel(" Erledigt "), c);
        jPanelExtra.add(label);
    }

    private void addExtraZeile(int y, GridBagLayout gridbag, GridBagConstraints c) {
        c.gridy = y + 1;
        c.gridx = 0;
        c.weightx = 1;
        gridbag.setConstraints(jLabel[y], c);
        jPanelExtra.add(jLabel[y]);
        c.gridx = 1;
        c.weightx = 10;
        gridbag.setConstraints(jProgressBarThread[y], c);
        jPanelExtra.add(jProgressBarThread[y]);
        c.gridx = 2;
        c.weightx = 100;
        gridbag.setConstraints(jProgressBarErledigt[y], c);
        jPanelExtra.add(jProgressBarErledigt[y]);
    }

    private void setText() {
        jTextFieldSeiten.setMargin(new Insets(1, 5, 1, 5));
        jTextFieldSeiten.setText(String.valueOf(daten.filmeLaden.getSeitenZaehlerGesamt()));
    }

    private void initProgressBar(int nr, String sender, int threads, int max, int progress) {
        if (max == 0) {
            jProgressBarErledigt[nr].setIndeterminate(false);
            jProgressBarErledigt[nr].setMaximum(1);
            jProgressBarErledigt[nr].setMinimum(0);
            jProgressBarErledigt[nr].setValue(1);
            jProgressBarErledigt[nr].setStringPainted(true);
            jProgressBarErledigt[nr].setString("  " + daten.filmeLaden.getSeitenZaehler(sender) + " Seiten  /  100% ");
            prozent[nr] = 0;
        } else if (max == 1) {
            jProgressBarErledigt[nr].setIndeterminate(true);
            jProgressBarErledigt[nr].setMaximum(1);
            jProgressBarErledigt[nr].setMinimum(0);
            jProgressBarErledigt[nr].setValue(0);
            jProgressBarErledigt[nr].setStringPainted(false);
        } else {
            jProgressBarErledigt[nr].setIndeterminate(false);
            jProgressBarErledigt[nr].setMaximum(max);
            jProgressBarErledigt[nr].setMinimum(0);
            int proz = 0;
            if (progress != 0) {
                proz = progress * 100 / max;
                if (proz >= 100) {
                    proz = 99;
                }
            }
            prozent[nr] = proz;
            jProgressBarErledigt[nr].setString("  " + daten.filmeLaden.getSeitenZaehler(sender) + " Seiten  /  " + proz + "% von " + max + " Themen  ");
            jProgressBarErledigt[nr].setStringPainted(true);
            jProgressBarErledigt[nr].setValue(progress);
        }
        if (threads == 0) {
            jProgressBarThread[nr].setMaximum(0);
            jProgressBarThread[nr].setMinimum(0);
            jProgressBarThread[nr].setValue(0);
            jProgressBarThread[nr].setStringPainted(false);
        } else {
            jProgressBarThread[nr].setMaximum(6);
            jProgressBarThread[nr].setMinimum(0);
            if (threads == 1) {
                jProgressBarThread[nr].setString(" " + threads + " Thread ");
            } else {
                jProgressBarThread[nr].setString(" " + threads + " Threads ");
            }
            jProgressBarThread[nr].setStringPainted(true);
            jProgressBarThread[nr].setValue(threads);
        }
    }

    private int getNr(String sender) {
        int i = 0;
        int max = sendername.length;
        for (; i < max; ++i) {
            if (sendername[i].equals(sender)) {
                return i;
            }
        }
        return i;
    }

    private void progressLoeschen() {
        for (int i = 0; i < jProgressBarErledigt.length; ++i) {
            jProgressBarErledigt[i].setMaximum(0);
            jProgressBarErledigt[i].setMinimum(0);
            jProgressBarErledigt[i].setValue(0);
            jProgressBarErledigt[i].setString("");
            jProgressBarErledigt[i].setStringPainted(false);
        }
    }

    private synchronized void zeit() {
        try {
            Date today = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy, HH:mm");
            Date d = sdf.parse(start);
            long sekunden = Math.round((today.getTime() - d.getTime()) / (1000));
            int proz = 100;
            for (int i = 0; i < prozent.length; ++i) {
                if (prozent[i] > 0) {
                    if (prozent[i] < proz) {
                        proz = prozent[i];
                    }
                }
            }
            if (proz == 0 || proz == 100) {
                jTextFieldEnde.setText("");
            } else {
                sekunden = 100 * sekunden / proz;
                Date ziel = new Date(today.getTime() + sekunden * 1000);
                String output = sdf.format(ziel);
                jTextFieldEnde.setText(output);
            }
        } catch (ParseException ex) {
        }
    }

    private synchronized void zeitSetzen() {
        if (start.equals("")) {
            start = Funktionen.getJetzt_ddMMyyyy_HHmm();
            jTextFieldStart.setText(start);
            jTextFieldEnde.setText("");
        }
    }

    private synchronized void zeitLoeschen() {
        progressLoeschen();
        start = "";
        jTextFieldStart.setText("");
        jTextFieldEnde.setText("");
    }

    private void initComponents() {
        jLabel6 = new javax.swing.JLabel();
        jTextFieldSeiten = new javax.swing.JTextField();
        jPanelExtra = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldStart = new javax.swing.JTextField();
        jTextFieldEnde = new javax.swing.JTextField();
        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel6.setText("abgesuchte Webseiten:");
        jTextFieldSeiten.setEditable(false);
        jPanelExtra.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        javax.swing.GroupLayout jPanelExtraLayout = new javax.swing.GroupLayout(jPanelExtra);
        jPanelExtra.setLayout(jPanelExtraLayout);
        jPanelExtraLayout.setHorizontalGroup(jPanelExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 538, Short.MAX_VALUE));
        jPanelExtraLayout.setVerticalGroup(jPanelExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 0, Short.MAX_VALUE));
        jLabel1.setText("Startzeit:");
        jLabel2.setText("Endzeit:");
        jTextFieldStart.setEditable(false);
        jTextFieldEnde.setEditable(false);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jPanelExtra, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel6).addComponent(jLabel1).addComponent(jLabel2)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jTextFieldStart, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE).addComponent(jTextFieldSeiten, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE).addComponent(jTextFieldEnde, javax.swing.GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE)))).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel6).addComponent(jTextFieldSeiten, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel1).addComponent(jTextFieldStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2).addComponent(jTextFieldEnde, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jPanelExtra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    }

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel6;

    private javax.swing.JPanel jPanelExtra;

    private javax.swing.JTextField jTextFieldEnde;

    private javax.swing.JTextField jTextFieldSeiten;

    private javax.swing.JTextField jTextFieldStart;

    private class BeobachterLaden implements FilmListener {

        @Override
        public void start(String sender, int m) {
            if (!sender.equals("")) {
                int nr = getNr(sender);
                progress[nr] = 0;
                threads[nr] = 1;
                maximus[nr] = m;
                if (maximus[nr] == 0) {
                    initProgressBar(nr, sender, threads[nr], 1, progress[nr]);
                } else {
                    initProgressBar(nr, sender, threads[nr], maximus[nr], progress[nr]);
                }
                zeitSetzen();
            }
        }

        @Override
        public void progress(String sender, String text) {
            if (!sender.equals("")) {
                int nr = getNr(sender);
                if (text.equals("fertig")) {
                    progress[nr] = 0;
                    threads[nr] = 0;
                    maximus[nr] = 0;
                    initProgressBar(nr, sender, 0, 0, 0);
                } else {
                    if (!text.contains("*")) {
                        ++progress[nr];
                    } else {
                        text = text.replace("*", "");
                    }
                    if (progress[nr] > maximus[nr]) {
                        progress[nr] = maximus[nr];
                    }
                    initProgressBar(nr, sender, threads[nr], maximus[nr], progress[nr]);
                    zeit();
                }
            }
        }

        @Override
        public void progress(String text) {
        }

        @Override
        public synchronized void fertig(String sender, boolean stop) {
            if (!sender.equals("")) {
                int nr = getNr(sender);
                progress[nr] = 0;
                threads[nr] = 0;
                maximus[nr] = 0;
                initProgressBar(nr, sender, 0, 0, 0);
                zeitLoeschen();
            }
        }

        @Override
        public void addMax(String sender, int m) {
            if (!sender.equals("")) {
                int nr = getNr(sender);
                maximus[nr] += m;
                initProgressBar(nr, sender, threads[nr], maximus[nr], progress[nr]);
                zeit();
            }
        }

        @Override
        public void threads(String sender, int tthreads) {
            if (!sender.equals("")) {
                int nr = getNr(sender);
                threads[nr] = tthreads;
                initProgressBar(nr, sender, threads[nr], maximus[nr], progress[nr]);
            }
        }
    }
}
