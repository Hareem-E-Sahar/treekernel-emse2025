package com.memetix.mst.examples.gui;

import com.memetix.mst.detect.Detect;
import com.memetix.mst.language.Language;
import com.memetix.mst.language.SpokenDialect;
import com.memetix.mst.speak.Speak;
import com.memetix.mst.translate.Translate;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public class Translator extends javax.swing.JFrame {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /** Creates new form Translator */
    public Translator() {
        Translate.setClientId("YOUR_CLIENT_ID_HERE");
        Translate.setClientSecret("YOUR_CLIENT_SECRET_HERE");
        initComponents();
        populateLocalizationMenu();
        localizeLabels();
        populateLanguageComboBoxes();
    }

    private void initComponents() {
        localizationGroup = new javax.swing.ButtonGroup();
        sourcePanel = new javax.swing.JPanel();
        sourceScrollPane = new javax.swing.JScrollPane();
        sourceText = new javax.swing.JTextArea();
        sourceLabel = new javax.swing.JLabel();
        controlPanel = new javax.swing.JPanel();
        translatePanel = new javax.swing.JPanel();
        translateButton = new javax.swing.JButton();
        translateLanguageBox = new javax.swing.JComboBox();
        speakPanel = new javax.swing.JPanel();
        speakButton = new javax.swing.JButton();
        speakDialectBox = new javax.swing.JComboBox();
        detectPanel = new javax.swing.JPanel();
        detectButton = new javax.swing.JButton();
        targetPanel = new javax.swing.JPanel();
        targetScrollPane = new javax.swing.JScrollPane();
        targetText = new javax.swing.JTextArea();
        targetLabel = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        quitMenuItem = new javax.swing.JMenuItem();
        localizationMenu = new javax.swing.JMenu();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Microsoft Translator - Java API");
        setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        sourceText.setColumns(20);
        sourceText.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        sourceText.setLineWrap(true);
        sourceText.setRows(5);
        sourceText.setWrapStyleWord(true);
        sourceText.setMargin(new java.awt.Insets(5, 5, 5, 5));
        sourceScrollPane.setViewportView(sourceText);
        sourceLabel.setFont(new java.awt.Font("Arial Unicode MS", 0, 12));
        sourceLabel.setText("Text to Translate");
        translateButton.setFont(new java.awt.Font("Arial Unicode MS", 0, 12));
        translateButton.setText("Translate To");
        translateButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                translateButtonActionPerformed(evt);
            }
        });
        translateLanguageBox.setFont(new java.awt.Font("Arial Unicode MS", 0, 12));
        translateLanguageBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        javax.swing.GroupLayout translatePanelLayout = new javax.swing.GroupLayout(translatePanel);
        translatePanel.setLayout(translatePanelLayout);
        translatePanelLayout.setHorizontalGroup(translatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(translateButton, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE).addGroup(translatePanelLayout.createSequentialGroup().addComponent(translateLanguageBox, 0, 127, Short.MAX_VALUE).addContainerGap()));
        translatePanelLayout.setVerticalGroup(translatePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(translatePanelLayout.createSequentialGroup().addComponent(translateButton).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE).addComponent(translateLanguageBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
        speakButton.setFont(new java.awt.Font("Arial Unicode MS", 0, 12));
        speakButton.setText("Speak Text");
        speakButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                speakButtonActionPerformed(evt);
            }
        });
        speakDialectBox.setFont(new java.awt.Font("Arial Unicode MS", 0, 12));
        speakDialectBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        javax.swing.GroupLayout speakPanelLayout = new javax.swing.GroupLayout(speakPanel);
        speakPanel.setLayout(speakPanelLayout);
        speakPanelLayout.setHorizontalGroup(speakPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(speakButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE).addComponent(speakDialectBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 143, Short.MAX_VALUE));
        speakPanelLayout.setVerticalGroup(speakPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(speakPanelLayout.createSequentialGroup().addComponent(speakButton).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE).addComponent(speakDialectBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
        detectButton.setFont(new java.awt.Font("Arial Unicode MS", 0, 12));
        detectButton.setText("Detect Language");
        detectButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detectButtonActionPerformed(evt);
            }
        });
        javax.swing.GroupLayout detectPanelLayout = new javax.swing.GroupLayout(detectPanel);
        detectPanel.setLayout(detectPanelLayout);
        detectPanelLayout.setHorizontalGroup(detectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, detectPanelLayout.createSequentialGroup().addContainerGap(124, Short.MAX_VALUE).addComponent(detectButton).addGap(164, 164, 164)));
        detectPanelLayout.setVerticalGroup(detectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(detectPanelLayout.createSequentialGroup().addComponent(detectButton).addContainerGap(60, Short.MAX_VALUE)));
        javax.swing.GroupLayout controlPanelLayout = new javax.swing.GroupLayout(controlPanel);
        controlPanel.setLayout(controlPanelLayout);
        controlPanelLayout.setHorizontalGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(controlPanelLayout.createSequentialGroup().addComponent(translatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(detectPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(speakPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()));
        controlPanelLayout.setVerticalGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(controlPanelLayout.createSequentialGroup().addContainerGap().addGroup(controlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(detectPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGroup(controlPanelLayout.createSequentialGroup().addComponent(speakPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap()).addComponent(translatePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))));
        javax.swing.GroupLayout sourcePanelLayout = new javax.swing.GroupLayout(sourcePanel);
        sourcePanel.setLayout(sourcePanelLayout);
        sourcePanelLayout.setHorizontalGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(sourcePanelLayout.createSequentialGroup().addContainerGap().addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(sourcePanelLayout.createSequentialGroup().addComponent(sourceLabel).addContainerGap(637, Short.MAX_VALUE)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sourcePanelLayout.createSequentialGroup().addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(sourceScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE).addComponent(controlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()))));
        sourcePanelLayout.setVerticalGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(sourcePanelLayout.createSequentialGroup().addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(sourceLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(sourceScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(controlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(92, 92, 92)));
        targetText.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        targetText.setColumns(20);
        targetText.setEditable(false);
        targetText.setFont(new java.awt.Font("Arial Unicode MS", 0, 14));
        targetText.setLineWrap(true);
        targetText.setRows(5);
        targetText.setWrapStyleWord(true);
        targetText.setFocusable(false);
        targetText.setMargin(new java.awt.Insets(5, 5, 5, 5));
        targetScrollPane.setViewportView(targetText);
        targetLabel.setFont(new java.awt.Font("Arial Unicode MS", 0, 12));
        targetLabel.setText("Translated Text");
        javax.swing.GroupLayout targetPanelLayout = new javax.swing.GroupLayout(targetPanel);
        targetPanel.setLayout(targetPanelLayout);
        targetPanelLayout.setHorizontalGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(targetPanelLayout.createSequentialGroup().addContainerGap().addGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(targetScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE).addComponent(targetLabel)).addContainerGap()));
        targetPanelLayout.setVerticalGroup(targetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(targetPanelLayout.createSequentialGroup().addContainerGap().addComponent(targetLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(targetScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE).addContainerGap()));
        fileMenu.setText("File");
        fileMenu.setFont(new java.awt.Font("Arial Unicode MS", 0, 12));
        quitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.ALT_MASK));
        quitMenuItem.setFont(new java.awt.Font("Arial Unicode MS", 0, 12));
        quitMenuItem.setText("Exit Translator");
        quitMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(quitMenuItem);
        menuBar.add(fileMenu);
        localizationMenu.setText("Localization");
        localizationMenu.setFont(new java.awt.Font("Arial Unicode MS", 0, 12));
        menuBar.add(localizationMenu);
        helpMenu.setText("Help");
        helpMenu.setFont(new java.awt.Font("Arial Unicode MS", 0, 12));
        aboutMenuItem.setFont(new java.awt.Font("Arial Unicode MS", 0, 12));
        aboutMenuItem.setText("About Microsoft Translator - Java API");
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(sourcePanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(targetPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(sourcePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(targetPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addContainerGap()));
        pack();
    }

    private void translateButtonActionPerformed(java.awt.event.ActionEvent evt) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                translateText();
            }
        });
    }

    private void speakButtonActionPerformed(java.awt.event.ActionEvent evt) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                triggerAudio();
            }
        });
    }

    private void detectButtonActionPerformed(java.awt.event.ActionEvent evt) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                detectLanguage();
            }
        });
    }

    private void quitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        System.exit(0);
    }

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane.showMessageDialog(this, "Copyright 2011 - Jonathan Griggs <jonathan.griggs@gmail.com>\n\n" + "Source Code:\n" + "       http://github.com/boatmeme/microsoft-translator-java-api", "About Microsoft Translator Java API", JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new Translator().setVisible(true);
            }
        });
    }

    private void triggerAudio() {
        try {
            String sWavUrl = Speak.execute(sourceText.getText(), (SpokenDialect) speakDialectBox.getSelectedItem());
            final URL waveUrl = new URL(sWavUrl);
            final HttpURLConnection uc = (HttpURLConnection) waveUrl.openConnection();
            playClip(uc.getInputStream());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Playing Speech : " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void translateText() {
        try {
            targetText.setText(Translate.execute(sourceText.getText().trim(), Language.AUTO_DETECT, (Language) translateLanguageBox.getSelectedItem()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Performing Localization : " + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void detectLanguage() {
        try {
            targetText.setText(Detect.execute(sourceText.getText().trim()).getName(defaultLocale));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Detecting Language : " + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void playClip(InputStream is) throws Exception {
        class AudioListener implements LineListener {

            private boolean done = false;

            public synchronized void update(LineEvent event) {
                Type eventType = event.getType();
                if (eventType == Type.STOP || eventType == Type.CLOSE) {
                    done = true;
                    notifyAll();
                }
            }

            public synchronized void waitUntilDone() throws InterruptedException {
                while (!done) {
                    wait();
                }
            }
        }
        AudioListener listener = new AudioListener();
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(is);
        try {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(listener);
            clip.open(audioInputStream);
            try {
                clip.start();
                listener.waitUntilDone();
            } finally {
                clip.close();
            }
        } finally {
            audioInputStream.close();
        }
    }

    private void populateLanguageComboBoxes() {
        final ComboBoxModel lModel = new DefaultComboBoxModel(Language.values());
        translateLanguageBox.setModel(lModel);
        translateLanguageBox.setRenderer(new LanguageRenderer());
        translateLanguageBox.removeItem(Language.AUTO_DETECT);
        translateLanguageBox.setSelectedItem(defaultTranslationLanguage);
        final ComboBoxModel dModel = new DefaultComboBoxModel(SpokenDialect.values());
        speakDialectBox.setModel(dModel);
        speakDialectBox.setRenderer(new SpokenDialectRenderer());
        speakDialectBox.setSelectedItem(defaultDialect);
    }

    private void populateLocalizationMenu() {
        try {
            localizationGroup = new ButtonGroup();
            localizationMenu.removeAll();
            for (Language lang : Language.values()) {
                if (lang != Language.AUTO_DETECT) {
                    final JRadioButtonMenuItem item = new JRadioButtonMenuItem(lang.getName(defaultLocale));
                    item.setFont(defaultFont);
                    item.setActionCommand(lang.toString());
                    item.addActionListener(localizationListener);
                    localizationGroup.add(item);
                    localizationMenu.add(item);
                    if (lang == defaultLocale) {
                        item.setSelected(true);
                        ;
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Rendering Language List: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    ActionListener localizationListener = new java.awt.event.ActionListener() {

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            defaultLocale = Language.fromString(evt.getActionCommand());
            populateLocalizationMenu();
            localizeLabels();
            repaint();
        }
    };

    private void localizeLabels() {
        String[] labels = { LABEL_TRANSLATE_SOURCE, LABEL_TRANSLATE_TARGET, LABEL_TRANSLATE_BUTTON, LABEL_DETECT_BUTTON, LABEL_SPEAK_BUTTON, LABEL_MENU_FILE, LABEL_MENU_LOCALIZATION, LABEL_MENU_HELP, LABEL_MENU_EXIT, LABEL_MENU_ABOUT };
        if (defaultLocale != Language.ENGLISH) {
            try {
                labels = Translate.execute(labels, defaultLocale);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Localizing Labels: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        sourceLabel.setText(labels[0]);
        targetLabel.setText(labels[1]);
        translateButton.setText(labels[2]);
        detectButton.setText(labels[3]);
        speakButton.setText(labels[4]);
        fileMenu.setText(labels[5]);
        localizationMenu.setText(labels[6]);
        helpMenu.setText(labels[7]);
        quitMenuItem.setText(labels[8]);
        aboutMenuItem.setText(labels[9]);
    }

    class LanguageRenderer extends BasicComboBoxRenderer {

        /**
		 * 
		 */
        private static final long serialVersionUID = -6708140393948840546L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                Language item = (Language) value;
                try {
                    setText(item.getName(defaultLocale));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Rendering Language List: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            return this;
        }
    }

    class SpokenDialectRenderer extends BasicComboBoxRenderer {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1295215348942235153L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                SpokenDialect item = (SpokenDialect) value;
                try {
                    setText(item.getName(defaultLocale));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Rendering Language List: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            return this;
        }
    }

    private Language defaultLocale = Language.ENGLISH;

    private Language defaultTranslationLanguage = Language.FRENCH;

    private SpokenDialect defaultDialect = SpokenDialect.ENGLISH_INDIA;

    private Font defaultFont = new Font("Arial Unicode MS", 0, 12);

    private final String LABEL_TRANSLATE_SOURCE = "Source Text";

    private final String LABEL_TRANSLATE_TARGET = "Translation";

    private final String LABEL_TRANSLATE_BUTTON = "Translate to";

    private final String LABEL_DETECT_BUTTON = "Detect Language";

    private final String LABEL_SPEAK_BUTTON = "Speak Text";

    private final String LABEL_MENU_FILE = "File";

    private final String LABEL_MENU_LOCALIZATION = "Localization";

    private final String LABEL_MENU_HELP = "Help";

    private final String LABEL_MENU_EXIT = "Exit Translator";

    private final String LABEL_MENU_ABOUT = "About Microsoft Translator - Java API";

    private javax.swing.JMenuItem aboutMenuItem;

    private javax.swing.JPanel controlPanel;

    private javax.swing.JButton detectButton;

    private javax.swing.JPanel detectPanel;

    private javax.swing.JMenu fileMenu;

    private javax.swing.JMenu helpMenu;

    private javax.swing.ButtonGroup localizationGroup;

    private javax.swing.JMenu localizationMenu;

    private javax.swing.JMenuBar menuBar;

    private javax.swing.JMenuItem quitMenuItem;

    private javax.swing.JLabel sourceLabel;

    private javax.swing.JPanel sourcePanel;

    private javax.swing.JScrollPane sourceScrollPane;

    private javax.swing.JTextArea sourceText;

    private javax.swing.JButton speakButton;

    private javax.swing.JComboBox speakDialectBox;

    private javax.swing.JPanel speakPanel;

    private javax.swing.JLabel targetLabel;

    private javax.swing.JPanel targetPanel;

    private javax.swing.JScrollPane targetScrollPane;

    private javax.swing.JTextArea targetText;

    private javax.swing.JButton translateButton;

    private javax.swing.JComboBox translateLanguageBox;

    private javax.swing.JPanel translatePanel;
}
