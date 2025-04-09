package ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.ImageIcon;
import outils.Embosseur;
import outils.EmbosseurInstalle;
import outils.EmbosseurLDC;
import outils.FileToolKit;
import ui.ConfigurationsComboBoxRenderer;
import ui.Language;
import gestionnaires.GestionnaireOuvrir;
import gestionnaires.GestionnaireErreur;
import gestionnaires.GestionnaireOuvrirFenetre;
import nat.ConfigNat;
import nat.Nat;
import nat.NatThread;
import nat.OptNames;

/**
 * Fenêtre principale de l'interface graphique
 * @author bruno
 *
 */
public class FenetrePrinc extends JFrame implements ActionListener, FocusListener, WindowListener, ComponentListener {

    /** Textual contents */
    private Language texts = new Language("FenetrePrinc");

    /** numéro de version pour la sérialisation (inutilisé par NAT) */
    private static final long serialVersionUID = 1L;

    /** fichier temporaire pour le stockage du numéro de version */
    private static final String FICH_VERSION = ConfigNat.getUserTempFolder() + "version";

    /** JTextField pour l'adresse du fichier noir */
    private JTextField jtfNoir = new JTextField("./licence.txt", 30);

    /** Label associé à jtfNoir
     * @see FenetrePrinc#jtfNoir
     * */
    private JLabel lJtfNoir = new JLabel(texts.getText("handwrittenlabel"));

    /** Bouton ouvrant le JFileChooser pour l'adresse du fichier noir 
     * @see FenetrePrinc#jtfNoir
     * @see GestionnaireOuvrir
     * */
    private JButton btSelectNoir = new JButton(texts.getText("handwrittenbutton"), new ImageIcon("ui/icon/document-open.png"));

    /** JTextField pour l'adresse du fichier braille */
    private JTextField jtfBraille = new JTextField("", 30);

    /** Label associé à {@link #jtfBraille}
    * @see FenetrePrinc#jtfBraille
    * */
    private JLabel lJtfBraille = new JLabel(texts.getText("braillelabel"));

    /**
     * Label contenant l'icône correspondant la la transcription ou à la détranscription
     */
    private JLabel lIcone = new JLabel(new ImageIcon("ui/nat.png"));

    /** Bouton ouvrant le JFileChooser pour l'adresse du fichier braille 
     * @see FenetrePrinc#jtfBraille
     * @see GestionnaireOuvrir
     * */
    private JButton btSelectBraille = new JButton(texts.getText("braillefilebutton"), new ImageIcon("ui/icon/document-open.png"));

    /** Bouton lançant la transcription 
     * @see Nat#fabriqueTranscriptions
     * @see Nat#lanceScenario()
     * */
    private JButton btTranscrire = new JButton(texts.getText("transcribe"), new ImageIcon("ui/icon/system-run.png"));

    /** Bouton ouvrant l'éditeur de fichier transcrit
     * @see FenetrePrinc#afficheFichier(String adresse)
     */
    private JButton btEditeur = new JButton(texts.getText("editor"), new ImageIcon("ui/icon/gtk-edit.png"));

    /** Bouton ouvrant l'éditeur avec un fichier déjà transcrit
     * @see FenetrePrinc#afficheFichier(String adresse)
     * @since 2.0
     */
    private JButton btEditeurTrans = new JButton("editortrans", new ImageIcon("ui/icon/gtk-edit.png"));

    /**
     * filte xsl à utiliser pour la transcription
     * @deprecated 2.0
     */
    @Deprecated
    private JTextField filtre = new JTextField("xsl.xsl", 20);

    /**
     * pour activer le mode débugage
     * <p>Pas encore actif!</p>
     */
    private JCheckBox debug = new JCheckBox();

    /**
     * Afficheur graphique de logs
     * @since 2.0
     */
    private AfficheurJTASwing panneauLog;

    /**
     * Label associé à jcbConfig
     * @see FenetrePrinc#jcbConfig
     */
    private JLabel lConfig = new JLabel(texts.getText("configlabel"));

    /** JComboBox listant les configurations possibles  */
    private JComboBox jcbConfig = new JComboBox();

    /**
     * Bouton ouvrant la fenetre d'options
     * @see GestionnaireOuvrirFenetre
     */
    private JButton btOption = new JButton(texts.getText("option"), new ImageIcon("ui/icon/document-properties.png"));

    /**
     * Bouton ouvrant la fenetre d'aide
     * @see GestionnaireOuvrirFenetre
     */
    private JButton btAide = new JButton(texts.getText("help"), new ImageIcon("ui/icon/help-browser.png"));

    /**
     * Bouton ouvrant la fenetre de renseignements sur Nat
     * @see GestionnaireOuvrirFenetre
     */
    private JButton btAPropos = new JButton(texts.getText("about"), new ImageIcon("ui/icon/help-about.png"));

    /** Bouton permettant de quitter le programme */
    private JButton btQuitter = new JButton(texts.getText("quit"), new ImageIcon("ui/icon/exit.png"));

    /** Bouton activant l'interface de rapport d'erreur*/
    private JButton btSignalBug = new JButton(texts.getText("bug"), new ImageIcon("ui/icon/script-error.png"));

    /** Bouton activant le sens de transcription de noir vers braille */
    private JButton btSens = new JButton(new ImageIcon("ui/icon/go-down.png"));

    /** sense button help */
    private ContextualHelp chSens = new ContextualHelp();

    /** JMenu contenant les actions possibles sur la fenêtre principale  */
    private JMenu jmAction = new JMenu(texts.getText("menu"));

    /** JMenu contenant les raccourcis vers les actions d'information de la fenêtre principale  */
    private JMenu jmAPropos = new JMenu(texts.getText("helpmenu"));

    /** JMenuBar de la fenêtre principale */
    private JMenuBar jmb = new JMenuBar();

    /**
     * Elément de menu Choisir fichier noir
     * @see FenetrePrinc#jmAction
     * @see GestionnaireOuvrir
     */
    private JMenuItem jmiSelectNoir;

    /**
     * Elément de menu Choisir fichier braille
     * @see FenetrePrinc#jmAction
     * @see GestionnaireOuvrir
     */
    private JMenuItem jmiSelectBraille;

    /**
     * Elément de menu Ouvrir options
     * @see FenetrePrinc#jmAction
     * @see GestionnaireOuvrirFenetre
     */
    private JMenuItem jmiOptions;

    /**
     * Elément de menu Importer une configuration
     * @see FenetrePrinc#jmAction
     */
    private JMenuItem jmiImporter;

    /**
     * Elément de menu Importer une configuration
     * @see FenetrePrinc#jmAction
     */
    private JMenuItem jmiExporter;

    /**
     * Elément de menu Exporter une configuration
     * @see FenetrePrinc#jmAction
     */
    private JMenuItem jmiSensTr;

    /**
     * Elément de menu Transcrire
     * @see Nat#fabriqueTranscriptions
     * @see Nat#lanceScenario()
     */
    private JMenuItem jmiTranscrire;

    /**
     * Elément de menu Ouvrir l'éditeur
     * @see FenetrePrinc#jmAction
     * @see GestionnaireOuvrirFenetre
     */
    private JMenuItem jmiOuvrirTrans;

    /**
     * Elément de menu Quitter
     * @see FenetrePrinc#jmAction
     */
    private JMenuItem jmiQuitter;

    /**
     * Elément de menu Aide
     * @see FenetrePrinc#jmAPropos
     */
    private JMenuItem jmiAide;

    /**
     * Elément de menu A propos de NAT
     * @see FenetrePrinc#jmAPropos
     */
    private JMenuItem jmiAPropos;

    /**
     * Elément de menu Ouvrir fichier déjà transcrit
     * @see FenetrePrinc#jmAction
     * @see GestionnaireOuvrir
     */
    private JMenuItem jmiOuvrirDejaTrans;

    /**
	 * Elément de menu Signaler un Bug
	 * @see FenetrePrinc#jmAction
	 */
    private JMenuItem jmiSignalBug;

    /**
	 * Elément de menu détranscription
	 * @see FenetrePrinc#jmiTan
	 */
    private JMenuItem jmiTan;

    /**
     * Indique si le Thread du scénario de transcription est lancé
     */
    private boolean running = false;

    /** Le gestionnaire d'erreur de NAT utilisé */
    private GestionnaireErreur gestErreur;

    /** L'instance de Nat */
    private Nat nat;

    /**
     * Vrai si la fenêtre option est ouverte
     * @see GestionnaireOuvrirFenetre
     * @see Configuration
     */
    private boolean optionsOuvertes = false;

    /**
     * Constructeur de FenetrePrinc
     * @param n une instance de Nat
     */
    public FenetrePrinc(Nat n) {
        UIManager.put("FileChooser.lookInLabelMnemonic", new Integer(KeyEvent.VK_R));
        setIconImage(new ImageIcon("ui/nat.png").getImage());
        addComponentListener(this);
        nat = n;
        panneauLog = new AfficheurJTASwing(15, 40);
        panneauLog.setEditable(false);
        panneauLog.setLineWrap(true);
        panneauLog.setCaretPosition(0);
        gestErreur = n.getGestionnaireErreur();
        gestErreur.addAfficheur(panneauLog);
        fabriqueFenetre();
        if (ConfigNat.getCurrentConfig().getOptimize()) {
            gestErreur.afficheMessage(texts.getText("optimisationmsg"), Nat.LOG_SILENCIEUX);
            new NatThread(nat).start();
        }
        if (ConfigNat.getCurrentConfig().getUpdateCheck()) {
            if (nat.checkUpdate()) {
                if (nat.isUpdateAvailable()) {
                    panneauLog.setText("**************************************************\n" + texts.getText("pannel1") + texts.getText("pannel2") + "**************************************************\n");
                } else {
                    gestErreur.afficheMessage(texts.getText("lastversion"), Nat.LOG_VERBEUX);
                }
            }
        }
    }

    /** @return @link FenetrePrinc#entreeXML}*/
    public JTextField getEntree() {
        return jtfNoir;
    }

    /** @return @link FenetrePrinc#filtre}
     * @deprecated 2.0
     * */
    @Deprecated
    public JTextField getFiltre() {
        return filtre;
    }

    /** @return @link FenetrePrinc#sortie}*/
    public JTextField getSortie() {
        return jtfBraille;
    }

    /** 
     * Change la valeur du texte de {@link FenetrePrinc#jtfNoir}
     * @param entree la nouvelle entrée
     */
    public void setEntree(String entree) {
        jtfNoir.setText(entree);
        if (ConfigNat.getCurrentConfig().getSortieAuto() && !ConfigNat.getCurrentConfig().isReverseTrans()) {
            setSortieAuto(false);
        }
    }

    /** 
     * Change la valeur du texte de {@link FenetrePrinc#filtre}
     * @param f le nouveau filtre
     * @deprecated 2.0
     */
    @Deprecated
    public void setFiltre(String f) {
        filtre.setText(f);
    }

    /** 
     * Change la valeur du texte de {@link FenetrePrinc#jtfBraille}
     * et active le bouton {@link FenetrePrinc#btEditeur} si l'adresse donnée est valide
     * @param tgt la nouvelle sortie
     */
    public void setSortie(String tgt) {
        jtfBraille.setText(tgt);
        verifieBtEditeur();
        if (ConfigNat.getCurrentConfig().getSortieAuto() && ConfigNat.getCurrentConfig().isReverseTrans()) {
            setSortieAuto(true);
        }
    }

    /** @return {@link FenetrePrinc#optionsOuvertes}*/
    public boolean getOptionsOuvertes() {
        return optionsOuvertes;
    }

    /** @param oo la valeur de {@link FenetrePrinc#optionsOuvertes}*/
    public void setOptionsOuvertes(boolean oo) {
        optionsOuvertes = oo;
    }

    /** loads texts */
    public void loadTexts() {
        texts = new Language("FenetrePrinc");
        lJtfNoir.setText(texts.getText("handwrittenlabel"));
        btSelectNoir.setText(texts.getText("handwrittenbutton"));
        lJtfBraille.setText(texts.getText("braillelabel"));
        btSelectBraille.setText(texts.getText("braillefilebutton"));
        btTranscrire.setText(texts.getText("transcribe"));
        btEditeur.setText(texts.getText("editor"));
        btEditeurTrans.setText(texts.getText("editortrans"));
        lConfig.setText(texts.getText("configlabel"));
        btOption.setText(texts.getText("option"));
        btAide.setText(texts.getText("help"));
        btAPropos.setText(texts.getText("about"));
        btQuitter.setText(texts.getText("quit"));
        btSignalBug.setText(texts.getText("bug"));
        jmAction.setText(texts.getText("menu"));
        jmAPropos.setText(texts.getText("helpmenu"));
        jmiSelectNoir.setText(texts.getText("handwrittenbutton"));
        jmiSelectBraille.setText(texts.getText("braillefilebutton"));
        jmiOptions.setText(texts.getText("option"));
        jmiImporter.setText("Importer...");
        jmiExporter.setText("Exporter...");
        jmiSensTr.setText(texts.getText("reverse"));
        jmiTranscrire.setText(texts.getText("transcribe"));
        jmiOuvrirTrans.setText(texts.getText("opentranscriptionmenu"));
        jmiTan.setText(texts.getText("reverseeditor"));
        jmiOuvrirDejaTrans.setText(texts.getText("editortrans"));
        jmiSignalBug.setText(texts.getText("bug"));
        jmiQuitter.setText(texts.getText("quit"));
        jmiAide.setText(texts.getText("help"));
        jmiAPropos.setText(texts.getText("about"));
        setName(texts.getText("maintitle"));
        getAccessibleContext().setAccessibleDescription(texts.getText("maintitledesc"));
        jtfNoir.setName(texts.getText("handwrittenfile"));
        Context cJtfNoir = new Context("u", "TextField", "handwritten", texts);
        Context cBtSelectNoir = new Context("u", "Button", "handwrittenbutton", texts);
        Context cJtfBraille = new Context("r", "TextField", "braille", texts);
        Context cBtSelectBraille = new Context("r", "TextField", "braillefilebutton", texts);
        Context cBtTranscrire = new Context("t", "TextField", "transcribe", texts);
        Context cBtEditeur = new Context("l", "Button", "editor", texts);
        Context cBtEditeurTrans = new Context("d", "Button", "editortrans", texts);
        Context cscrollLog = new Context("", "ScrollPane", "scrolllog", texts);
        Context cBtOption = new Context("o", "Button", "option", texts);
        Context cBtAide = new Context("F6", "Button", "help", texts);
        Context cBtAPropos = new Context("F11", "Button", "about", texts);
        Context cBtSignalBug = new Context("p", "Button", "bug", texts);
        Context cBtQuitter = new Context("q", "Button", "quit", texts);
        Context cJcbConfig = new Context("c", "ComboBox", "config", texts);
        cJtfNoir.treat();
        cJtfNoir.addLabel(texts.getText("defaultname"), "handwrittenfile");
        cJtfNoir.setContextualHelp(jtfNoir, "mainfiles");
        cBtSelectNoir.treat();
        cBtSelectNoir.setContextualHelp(btSelectNoir, "mainfiles");
        cJtfBraille.treat();
        cJtfBraille.setContextualHelp(jtfBraille, "mainfiles");
        jtfBraille.setName(texts.getText("braillefile"));
        cJtfBraille.addLabel(texts.getText("defaultname"), "braillefile");
        cBtSelectBraille.treat();
        cBtSelectBraille.setContextualHelp(btSelectBraille, "mainfiles");
        btSens.getAccessibleContext().setAccessibleName(texts.getText("reversename"));
        chSens.setParameters(btSens, "mainfiles");
        chSens.setHelpName(btSens.getToolTipText());
        String[] chSensKeys = { "dir1ttt", "dir1name", "dir1iconettt", "dir2ttt", "dir2name", "dir2iconettt" };
        String[] chSensLabels = { texts.getText("natortantooltip") + " (NAT)", texts.getText("natortanaccessname") + " (NAT)", texts.getText("natortanicon") + " NAT", texts.getText("natortantooltip") + " (TAN)", texts.getText("natortanaccessname") + " (TAN)", texts.getText("natortanicon") + " TAN" };
        chSens.addLabels(chSensLabels, chSensKeys);
        chSens.addContext(cBtSelectBraille, false);
        setReverseTrans(ConfigNat.getCurrentConfig().isReverseTrans());
        cBtTranscrire.treat();
        String[] cBtTranscrireKeys = { "dir1buttonname", "dir2buttonname" };
        String[] cBtTranscrireLabels = { "Nom du bouton : sens NAT", "Nom du bouton : sens TAN" };
        cBtTranscrire.addLabels(cBtTranscrireLabels, cBtTranscrireKeys);
        cBtTranscrire.setContextualHelp(btTranscrire, "mainstartbutton");
        cBtEditeur.treat();
        cBtEditeur.setContextualHelp(btEditeur, "mainopen");
        cBtEditeurTrans.treat();
        cBtEditeurTrans.setContextualHelp(btEditeurTrans, "mainopenother");
        cscrollLog.treat();
        cscrollLog.setContextualHelp(panneauLog, "mainpane");
        cBtOption.treat();
        cBtOption.setContextualHelp(btOption, "guiOptions");
        cBtAide.treat();
        cBtAide.setContextualHelp(btAide, "FAQ");
        cBtAPropos.treat();
        cBtAPropos.setContextualHelp(btAPropos, "credits");
        cBtSignalBug.treat();
        cBtSignalBug.setContextualHelp(btSignalBug, "limites");
        cBtQuitter.treat();
        cBtQuitter.setContextualHelp(btQuitter, "mainbuttons");
        cJcbConfig.treat();
        cJcbConfig.setContextualHelp(jcbConfig, "mainconfigs");
        jcbConfig.setRenderer(new ConfigurationsComboBoxRenderer());
        debug.setText("Debugage actif");
    }

    /** Fabrique la fenêtre {@link FenetrePrinc} */
    private void fabriqueFenetre() {
        this.setTitle("NAT " + nat.getVersionLong());
        jmiSelectNoir = new JMenuItem(texts.getText("handwrittenbutton"));
        jmiSelectNoir.setMnemonic('e');
        jmiSelectNoir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiSelectNoir.addActionListener(new GestionnaireOuvrir(this, GestionnaireOuvrir.OUVRIR_SOURCE));
        jmAction.add(jmiSelectNoir);
        jmiSelectBraille = new JMenuItem(texts.getText("braillefilebutton"));
        jmiSelectBraille.setMnemonic('s');
        jmiSelectBraille.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiSelectBraille.addActionListener(new GestionnaireOuvrir(this, GestionnaireOuvrir.OUVRIR_SORTIE));
        jmAction.add(jmiSelectBraille);
        jmAction.addSeparator();
        jmiOptions = new JMenuItem(texts.getText("option"));
        jmiOptions.setMnemonic('o');
        jmiOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiOptions.addActionListener(new GestionnaireOuvrirFenetre(GestionnaireOuvrirFenetre.OUVRIR_OPTIONS, this));
        jmAction.add(jmiOptions);
        jmiImporter = new JMenuItem("Importer...");
        jmiImporter.setMnemonic('m');
        jmiImporter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiImporter.addActionListener(this);
        jmAction.add(jmiImporter);
        jmiExporter = new JMenuItem("Exporter...");
        jmiExporter.setMnemonic('m');
        jmiExporter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiExporter.addActionListener(this);
        jmAction.add(jmiExporter);
        jmAction.addSeparator();
        jmiSensTr = new JMenuItem(texts.getText("reverse"));
        jmiSensTr.setMnemonic('i');
        jmiSensTr.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiSensTr.addActionListener(this);
        jmAction.add(jmiSensTr);
        jmiTranscrire = new JMenuItem(texts.getText("transcribe"));
        jmiTranscrire.setMnemonic('t');
        jmiTranscrire.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiTranscrire.addActionListener(this);
        jmAction.add(jmiTranscrire);
        jmAction.addSeparator();
        jmiOuvrirTrans = new JMenuItem(texts.getText("opentranscriptionmenu"));
        jmiOuvrirTrans.setMnemonic('l');
        jmiOuvrirTrans.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiOuvrirTrans.addActionListener(this);
        jmAction.add(jmiOuvrirTrans);
        jmiTan = new JMenuItem(texts.getText("reverseeditor"));
        jmiTan.setMnemonic('n');
        jmiTan.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiTan.addActionListener(this);
        jmAction.add(jmiTan);
        jmiOuvrirDejaTrans = new JMenuItem(texts.getText("editortrans"));
        jmiOuvrirDejaTrans.setMnemonic('d');
        jmiOuvrirDejaTrans.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiOuvrirDejaTrans.addActionListener(new GestionnaireOuvrir(this, GestionnaireOuvrir.OUVRIR_TRANS));
        jmAction.add(jmiOuvrirDejaTrans);
        jmiSignalBug = new JMenuItem(texts.getText("bug"));
        jmiSignalBug.setMnemonic('p');
        jmiSignalBug.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiSignalBug.addActionListener(this);
        jmAction.add(jmiSignalBug);
        jmAction.addSeparator();
        jmiQuitter = new JMenuItem(texts.getText("quit"));
        jmiQuitter.setMnemonic('q');
        jmiQuitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiQuitter.addActionListener(this);
        jmAction.add(jmiQuitter);
        jmiAide = new JMenuItem(texts.getText("help"));
        jmiAide.setMnemonic(KeyEvent.VK_F6);
        jmiAide.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiAide.addActionListener(new GestionnaireOuvrirFenetre(GestionnaireOuvrirFenetre.OUVRIR_AIDE, this));
        jmAPropos.add(jmiAide);
        jmAPropos.addSeparator();
        jmiAPropos = new JMenuItem(texts.getText("about"));
        jmiAPropos.setMnemonic(java.awt.event.KeyEvent.VK_F11);
        jmiAPropos.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        jmiAPropos.addActionListener(new GestionnaireOuvrirFenetre(GestionnaireOuvrirFenetre.OUVRIR_APROPOS, this));
        jmAPropos.add(jmiAPropos);
        jmAction.setMnemonic('m');
        jmAPropos.setMnemonic('a');
        jmb.add(jmAction);
        jmb.add(jmAPropos);
        setName(texts.getText("maintitle"));
        getAccessibleContext().setAccessibleDescription(texts.getText("maintitledesc"));
        lJtfNoir.setLabelFor(jtfNoir);
        if (ConfigNat.getCurrentConfig().getFichNoir().length() > 0) {
            jtfNoir.setText(ConfigNat.getCurrentConfig().getFichNoir());
        }
        lJtfNoir.setDisplayedMnemonic('u');
        jtfNoir.setName(texts.getText("handwrittenfile"));
        jtfNoir.addFocusListener(this);
        btSelectNoir.addActionListener(new GestionnaireOuvrir(this, GestionnaireOuvrir.OUVRIR_SOURCE));
        btSelectNoir.setMnemonic('e');
        Action refreshAction = new AbstractAction() {

            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                loadTexts();
            }
        };
        InputMap im = btSelectNoir.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = btSelectNoir.getActionMap();
        am.put("refreshAction", refreshAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, true), "refreshAction");
        lJtfBraille.setLabelFor(jtfBraille);
        lJtfBraille.setDisplayedMnemonic('r');
        if (ConfigNat.getCurrentConfig().getFBraille().length() > 0) {
            jtfBraille.setText(ConfigNat.getCurrentConfig().getFBraille());
        }
        jtfBraille.setName(texts.getText("braillefile"));
        jtfBraille.addFocusListener(this);
        btSelectBraille.addActionListener(new GestionnaireOuvrir(this, GestionnaireOuvrir.OUVRIR_SORTIE));
        btSelectBraille.setMnemonic('s');
        btSens.addActionListener(this);
        btSens.setMnemonic('i');
        btTranscrire.addActionListener(this);
        btTranscrire.setMnemonic('t');
        btEditeur.addActionListener(this);
        btEditeur.setMnemonic('l');
        verifieBtEditeur();
        btEditeurTrans.addActionListener(new GestionnaireOuvrir(this, GestionnaireOuvrir.OUVRIR_TRANS));
        btEditeurTrans.setMnemonic('d');
        verifieBtEditeur();
        debug.setText("Debugage actif");
        JScrollPane scrollLog = new JScrollPane(panneauLog);
        scrollLog.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        btOption.addActionListener(new GestionnaireOuvrirFenetre(GestionnaireOuvrirFenetre.OUVRIR_OPTIONS, this));
        btOption.setMnemonic('O');
        btAide.addActionListener(new GestionnaireOuvrirFenetre(GestionnaireOuvrirFenetre.OUVRIR_AIDE, this));
        btAide.setMnemonic(java.awt.event.KeyEvent.VK_F6);
        btAPropos.addActionListener(new GestionnaireOuvrirFenetre(GestionnaireOuvrirFenetre.OUVRIR_APROPOS, this));
        btAPropos.setMnemonic(java.awt.event.KeyEvent.VK_F11);
        btSignalBug.addActionListener(this);
        btSignalBug.setMnemonic('p');
        btQuitter.addActionListener(this);
        btQuitter.setMnemonic('q');
        lConfig.setLabelFor(jcbConfig);
        lConfig.setDisplayedMnemonic('c');
        chargeConfigurations();
        jcbConfig.setEditable(false);
        this.loadTexts();
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        setJMenuBar(jmb);
        c.insets = new Insets(0, 3, 0, 3);
        c.anchor = GridBagConstraints.LINE_END;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        add(lJtfNoir, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 1;
        c.weightx = 1.0;
        add(jtfNoir, c);
        c.gridx = 2;
        c.weightx = 0.0;
        add(btSelectNoir, c);
        c.gridx = 1;
        c.gridy++;
        c.insets = new Insets(0, 3, 0, 3);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;
        add(btSens, c);
        c.insets = new Insets(0, 3, 3, 3);
        c.anchor = GridBagConstraints.LINE_END;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy++;
        add(lJtfBraille, c);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        add(jtfBraille, c);
        c.gridx = 2;
        add(btSelectBraille, c);
        c.insets = new Insets(10, 3, 3, 3);
        c.gridx = 0;
        c.gridy++;
        c.gridheight = 2;
        add(lIcone, c);
        c.gridx++;
        c.gridheight = 1;
        add(btTranscrire, c);
        c.gridx = 2;
        add(btEditeur, c);
        c.gridx = 1;
        c.gridy++;
        c.gridx = 2;
        add(btEditeurTrans, c);
        c.anchor = GridBagConstraints.LINE_END;
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0.0;
        add(lConfig, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = 1;
        c.weightx = 1.0;
        add(jcbConfig, c);
        c.gridx = 2;
        c.weightx = 0.0;
        add(btOption, c);
        c.gridx = 0;
        c.gridy++;
        JPanel panneauAffichage = new JPanel();
        panneauAffichage.add(scrollLog);
        panneauAffichage.getAccessibleContext().setAccessibleName("Panneau d'affichage");
        panneauAffichage.getAccessibleContext().setAccessibleDescription("Panneau d'affichage dynamique des traitements (logs)");
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        add(scrollLog, c);
        JPanel pBoutons = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.gridy++;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 3;
        c.insets = new Insets(3, 3, 3, 3);
        add(pBoutons, c);
        c.anchor = GridBagConstraints.LINE_START;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridx = 0;
        c.ipadx = 0;
        c.insets = new Insets(3, 20, 3, 20);
        pBoutons.add(btAide, c);
        c.gridx++;
        pBoutons.add(btAPropos, c);
        c.gridx++;
        c.weightx = 2.0;
        c.weighty = 2.0;
        pBoutons.add(btSignalBug, c);
        c.gridx++;
        pBoutons.add(btQuitter, c);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(this);
        if (ConfigNat.getCurrentConfig().getMemoriserFenetre()) {
            int x = ConfigNat.getCurrentConfig().getWidthPrincipal();
            int y = ConfigNat.getCurrentConfig().getHeightPrincipal();
            if (x + y != 0) {
                setPreferredSize(new Dimension(x, y));
            }
        }
        if (ConfigNat.getCurrentConfig().getCentrerFenetre()) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension size = this.getPreferredSize();
            screenSize.height = screenSize.height / 2;
            screenSize.width = screenSize.width / 2;
            size.height = size.height / 2;
            size.width = size.width / 2;
            int y = screenSize.height - size.height;
            int x = screenSize.width - size.width;
            setLocation(x, y);
        }
    }

    /** Ajoute les configurations des répertoires "configurations" et $temp/filters dans {@link FenetrePrinc#jcbConfig} 
     * et sélectionne la configuration active.
     */
    public void chargeConfigurations() {
        jcbConfig.removeActionListener(this);
        jcbConfig.removeAllItems();
        File repertoireSysteme = new File(ConfigNat.getSystemConfigFilterFolder());
        File repertoireUser = new File(ConfigNat.getUserConfigFilterFolder());
        ArrayList<File> both = new ArrayList<File>();
        try {
            Collections.addAll(both, repertoireSysteme.listFiles());
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        try {
            Collections.addAll(both, repertoireUser.listFiles());
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
        File[] listConfs = both.toArray(new File[] {});
        ArrayList<ConfigurationsListItem> lesConfs = new ArrayList<ConfigurationsListItem>();
        ConfigurationsListItem selectedConfig = null;
        for (int i = 0; i < listConfs.length; i++) {
            try {
                ConfigurationsListItem cli = new ConfigurationsListItem(listConfs[i].getCanonicalPath(), gestErreur);
                if (cli.getIsValid()) {
                    lesConfs.add(cli);
                    if (cli.getFilename().equals(ConfigNat.getCurrentConfig().getFichierConf())) {
                        selectedConfig = cli;
                    }
                }
            } catch (IOException ieo) {
                ieo.printStackTrace();
            }
        }
        Collections.sort(lesConfs);
        for (ConfigurationsListItem c : lesConfs) {
            jcbConfig.addItem(c);
        }
        jcbConfig.addActionListener(this);
        if (selectedConfig != null) {
            jcbConfig.setSelectedItem(selectedConfig);
        }
    }

    /**
     * Ouvre l'éditeur de fichier transcrit dans l'éditeur, en passant à l'éditeur une instance d' {@link Embosseur}
     * si nécessaire. Utilise l'encoding représenté par {@link nat.OptNames} dans {@link ConfigNat}
     * @param nomFichier Le nom du fichier transcrit à ouvrir
     */
    public void afficheFichier(String nomFichier) {
        Embosseur emb = null;
        if (ConfigNat.getCurrentConfig().getUtiliserCommandeEmbossage()) {
            emb = new EmbosseurLDC(nomFichier, gestErreur);
        } else if (ConfigNat.getCurrentConfig().getUtiliserEmbosseuse()) {
            emb = new EmbosseurInstalle(nomFichier, gestErreur);
        }
        Editeur2 editeur = new Editeur2(ConfigNat.getCurrentConfig().getLongueurLigne(), emb, gestErreur);
        String sourceEncoding = ConfigNat.getCurrentConfig().getBrailleEncoding();
        if (sourceEncoding.equals("automatique")) {
            sourceEncoding = nat.trouveEncodingSource(nomFichier);
            if (sourceEncoding == null || sourceEncoding.equals("")) {
                sourceEncoding = Charset.defaultCharset().name();
                gestErreur.afficheMessage(texts.getText("charsetnotfound") + sourceEncoding + "\n", Nat.LOG_NORMAL);
            } else {
                gestErreur.afficheMessage(texts.getText("charsetok") + sourceEncoding + "\n", Nat.LOG_SILENCIEUX);
            }
        }
        editeur.setEncodage(sourceEncoding);
        editeur.setAfficheLigneSecondaire(ConfigNat.getCurrentConfig().getAfficheLigneSecondaire());
        editeur.afficheFichier(nomFichier, ConfigNat.getCurrentConfig().getPoliceEditeur(), ConfigNat.getCurrentConfig().getTaillePolice(), ConfigNat.getCurrentConfig().getPolice2Editeur(), ConfigNat.getCurrentConfig().getTaillePolice2());
        editeur.pack();
        if (ConfigNat.getCurrentConfig().getMaximizedEditeur()) {
            editeur.setExtendedState(Frame.MAXIMIZED_BOTH);
        }
        editeur.setVisible(true);
    }

    /**
     * génère un nom de fichier de sortie automatiquement
     * @param reverse vrai si détranscription (donc renommage fichier noir)
     * et faux si transcription (donc renommage fichier braille)
     */
    public void setSortieAuto(boolean reverse) {
        if (!reverse) {
            jtfBraille.setText(FileToolKit.nomSortieAuto(jtfNoir.getText(), "txt"));
        } else {
            jtfNoir.setText(FileToolKit.nomSortieAuto(jtfBraille.getText(), "xhtml"));
        }
    }

    /**
     * Renvoie le gestionnaire d'erreur utilisé dnas cette fenêtre 
     * @return le gestionnaire d'erreur utilisé
     */
    public GestionnaireErreur getGestErreur() {
        return gestErreur;
    }

    /**
	 * Méthode d'accès à {@link #running}
	 * @param r valeur pour {@link #running}
	 */
    public void setRunning(boolean r) {
        running = r;
    }

    /**
	 * Méthode d'accès à {@link #running}
	 * @return true si {@link #running} est vrai
	 */
    public boolean getRunning() {
        return running;
    }

    /**
	 * Méthode d'accès à {@link #nat}
	 * @return l'instance de nat utilisée
	 */
    public Nat getNat() {
        return nat;
    }

    /**
	 * Méthode d'accès à {@link #btEditeur}
	 * @return le bouton {@link #btEditeur}
	 */
    public JButton getBtEditeur() {
        return btEditeur;
    }

    /**
	 * Active ou désactive les composants liés à la transcription
	 * @param b true si activation, false sinon
	 */
    public void activeTrans(boolean b) {
        btTranscrire.setEnabled(b);
        jmiTranscrire.setEnabled(b);
    }

    /** implémentation de actionPerformed(ActionEvent evt) de l'interface ActionListener
     * gère tous les boutons, tous les items des menus, et le changement de configuration par {@link FenetrePrinc#jcbConfig}
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == btTranscrire || evt.getSource() == jmiTranscrire) {
            boolean go = true;
            if (new File(jtfNoir.getText()).exists() && ConfigNat.getCurrentConfig().isReverseTrans()) {
                go = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, texts.getText("deletehandwritten"), texts.getText("existinghandwritten"), JOptionPane.YES_NO_OPTION);
            } else if (new File(jtfBraille.getText()).exists() && !ConfigNat.getCurrentConfig().isReverseTrans()) {
                go = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, texts.getText("deletebraille"), texts.getText("existingbraille"), JOptionPane.YES_NO_OPTION);
            }
            if (go && ConfigNat.getCurrentConfig().getAbreger() && ConfigNat.getCurrentConfig().getDemandeException()) {
                go = JOptionPane.YES_OPTION == DialogueIntegral.showDialog(this, true);
            }
            if (go) {
                Thread thr = new ThreadJPB();
                thr.start();
                btEditeur.setEnabled(false);
                this.repaint();
                if ((jtfNoir.getText().length() < 1) || (filtre.getText().length() < 1) || (jtfBraille.getText().length() < 1)) {
                    gestErreur.afficheMessage(texts.getText("missingfile"), Nat.LOG_SILENCIEUX);
                } else {
                    panneauLog.setText("");
                    panneauLog.paintImmediately(new Rectangle(0, 0, panneauLog.getWidth(), panneauLog.getHeight()));
                    gestErreur.setModeDebugage(debug.isSelected());
                    ArrayList<String> sources = new ArrayList<String>();
                    ArrayList<String> sorties = new ArrayList<String>();
                    sources.add(jtfNoir.getText());
                    sorties.add(jtfBraille.getText());
                    if (nat.fabriqueTranscriptions(sources, sorties)) {
                        Thread thrTrans = new ThreadTrans();
                        thrTrans.start();
                    }
                }
            } else {
                gestErreur.afficheMessage(texts.getText("abort"), Nat.LOG_SILENCIEUX);
            }
        } else if (evt.getSource() == btEditeur || evt.getSource() == jmiOuvrirTrans) {
            ouvrirEditeur();
        } else if (evt.getSource() == jmiTan) {
            ouvrirEditeur(true);
        } else if (evt.getSource() == btSignalBug || evt.getSource() == jmiSignalBug) {
            doBugReport();
        } else if (evt.getSource() == jcbConfig) {
            ConfigurationsListItem cli = (ConfigurationsListItem) jcbConfig.getSelectedItem();
            ConfigNat.charger(cli.getFilename());
        } else if (evt.getSource() == btSens || (evt.getSource() == jmiSensTr)) {
            setReverseTrans(!ConfigNat.getCurrentConfig().isReverseTrans());
            if (ConfigNat.getCurrentConfig().getSortieAuto()) {
                setSortieAuto(ConfigNat.getCurrentConfig().isReverseTrans());
            }
        } else if (evt.getSource() == jmiImporter) {
            importerConfig();
        } else if (evt.getSource() == jmiExporter) {
            exporterConfig();
        } else if (evt.getSource() == btQuitter || evt.getSource() == jmiQuitter) {
            quitter();
        }
    }

    /**
     * exporte la configuration active
     */
    private void exporterConfig() {
        JFileChooser jfc = new JFileChooser();
        jfc.setCurrentDirectory(new File(ConfigNat.getUserConfigFolder()));
        jfc.addChoosableFileFilter(new FiltreFichier(new String[] { "nca" }, "archive configuration nat (*.nca)"));
        jfc.setDialogTitle("Exporter la configuration courante");
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String fich = jfc.getSelectedFile().getCanonicalPath();
                if (!fich.endsWith(".nca")) {
                    fich += ".nca";
                }
                String fConf = ConfigNat.getCurrentConfig().getFichierConf();
                String fAbr = ConfigNat.getCurrentConfig().getRulesFrG2Perso();
                String fCoup = ConfigNat.getCurrentConfig().getDicoCoup();
                String fListeNoir = DialogueIntegral.listeFich;
                FileToolKit.saveStrToFile(ConfigNat.getSvnVersion() + "", FICH_VERSION);
                ArrayList<String> fileArchives = new ArrayList<String>();
                fileArchives.add(FICH_VERSION);
                fileArchives.add(fConf);
                if (new File(fListeNoir).exists()) {
                    fileArchives.add(fListeNoir);
                } else {
                    fileArchives.add("");
                }
                if (!fAbr.equals(ConfigNat.getInstallFolder() + "xsl/dicts/fr-g2.xml")) {
                    fileArchives.add(fAbr);
                } else {
                    fileArchives.add("");
                }
                if (!fCoup.equals(ConfigNat.getDicoCoupDefaut())) {
                    fileArchives.add(fCoup);
                } else {
                    fileArchives.add("");
                }
                byte[] buf = new byte[1024];
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(fich));
                int i = 1;
                for (String s : fileArchives) {
                    if (!s.equals("")) {
                        FileInputStream in = new FileInputStream(s);
                        out.putNextEntry(new ZipEntry(i + "_" + new File(s).getName()));
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                        out.closeEntry();
                        in.close();
                    }
                    i++;
                }
                out.close();
            } catch (IOException e) {
                gestErreur.afficheMessage("Erreur lors de lecture/écriture lors de l'exportation", Nat.LOG_SILENCIEUX);
                if (Nat.LOG_DEBUG == ConfigNat.getCurrentConfig().getNiveauLog()) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * importe une configuration complète
     */
    private void importerConfig() {
        JFileChooser jfc = new JFileChooser();
        jfc.setCurrentDirectory(new File(ConfigNat.getUserConfigFolder()));
        jfc.addChoosableFileFilter(new FiltreFichier(new String[] { "nca" }, "archive configuration nat (*.nca)"));
        jfc.setDialogTitle("Exporter la configuration courante");
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File tmpConf = new File(ConfigNat.getConfTempFolder());
                File[] files = tmpConf.listFiles();
                for (File f : files) {
                    if (f.isFile()) {
                        f.delete();
                    }
                }
                String inFilename = jfc.getSelectedFile().getCanonicalPath();
                ZipInputStream in = new ZipInputStream(new FileInputStream(inFilename));
                ZipEntry entry;
                while ((entry = in.getNextEntry()) != null) {
                    String outFilename = ConfigNat.getConfTempFolder() + entry.getName();
                    OutputStream out = new FileOutputStream(outFilename);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                }
                in.close();
                files = tmpConf.listFiles();
                ArrayList<File> al_Files = new ArrayList<File>();
                for (int i = 0; i < files.length; i++) {
                    al_Files.add(files[i]);
                }
                Collections.sort(al_Files);
                File fVersion = al_Files.get(0);
                File fConf = al_Files.get(1);
                File fAbr = new File("");
                File fCoup = new File("");
                for (int i = 3; i < al_Files.size(); i++) {
                    if (al_Files.get(i).getName().startsWith("4")) {
                        fAbr = al_Files.get(i);
                    } else if (al_Files.get(i).getName().startsWith("5")) {
                        fCoup = al_Files.get(i);
                    }
                }
                boolean restart = false;
                int version = Integer.parseInt(FileToolKit.loadFileToStr(fVersion.getCanonicalPath()).split("\n")[0]);
                if (version > ConfigNat.getSvnVersion()) {
                    System.out.println("update needed");
                } else {
                    if (version < ConfigNat.getSvnVersion()) {
                        restart = true;
                    }
                    Properties conf = new Properties();
                    FileInputStream fis = new FileInputStream(fConf);
                    conf.load(fis);
                    fis.close();
                    String nomFichierReglesG2 = conf.getProperty(OptNames.fi_litt_fr_abbreg_rules_filename_perso);
                    boolean abregerDefaut = nomFichierReglesG2 == null || nomFichierReglesG2.equals(ConfigNat.getCurrentConfig().getRulesFrG2());
                    if (!abregerDefaut) {
                        conf.setProperty(OptNames.fi_litt_fr_abbreg_rules_filename_perso, ConfigNat.getConfImportFolder() + fAbr.getName());
                        FileToolKit.copyFile(fAbr.getCanonicalPath(), ConfigNat.getConfImportFolder() + fAbr.getName());
                    }
                    String nomFichierCoup = conf.getProperty(OptNames.fi_hyphenation_rulefile_name);
                    boolean coupDefaut = nomFichierCoup == null || nomFichierCoup.equals(ConfigNat.getDicoCoupDefaut());
                    if (!coupDefaut) {
                        conf.setProperty(OptNames.fi_hyphenation_rulefile_name, ConfigNat.getConfImportFolder() + fCoup.getName());
                        FileToolKit.copyFile(fCoup.getCanonicalPath(), ConfigNat.getConfImportFolder() + fCoup.getName());
                    }
                    FileToolKit.copyFile(fConf.getCanonicalPath(), ConfigNat.getUserConfigFilterFolder() + fConf.getName());
                    chargeConfigurations();
                    if (restart) {
                        JOptionPane.showMessageDialog(this, texts.getText("confRestart"));
                    } else {
                        JOptionPane.showMessageDialog(this, texts.getText("confImportSuccessfull"));
                    }
                }
            } catch (IOException e) {
                gestErreur.afficheMessage("Erreur lors de l'importation", Nat.LOG_SILENCIEUX);
            }
        }
    }

    /**
     * Change les composants graphiques en fonction de <code>reverse</code>
     * <code>reverse</code> est vrai si il faut passer en mode détranscription et 
     * faux q'il faut passer en mode transcription.
     * Appelle également {@link ConfigNat#setReverseTrans(boolean)}
     * @param reverse true si passer en mode détranscription 
     * 
     */
    private void setReverseTrans(boolean reverse) {
        if (!reverse) {
            btTranscrire.setText(texts.getText("dir1buttonname"));
            btSens.setIcon(new ImageIcon("ui/icon/go-down.png"));
            btSens.setToolTipText(texts.getText("dir1ttt"));
            btSens.getAccessibleContext().setAccessibleName(texts.getText("dir1name"));
            btEditeur.setText(texts.getText("dir1edit"));
            btEditeurTrans.setText(texts.getText("dir1edittrans"));
            btEditeurTrans.setToolTipText(texts.getText("dir1edittransttt"));
            lIcone.setIcon(new ImageIcon("ui/nat.png"));
            lIcone.setToolTipText(texts.getText("dir1iconettt"));
            ConfigNat.getCurrentConfig().setReverseTrans(false);
        } else {
            btTranscrire.setText(texts.getText("dir2buttonname"));
            btSens.setIcon(new ImageIcon("ui/icon/go-up.png"));
            btSens.setToolTipText(texts.getText("dir2ttt"));
            btSens.getAccessibleContext().setAccessibleName(texts.getText("dir2name"));
            btEditeur.setText(texts.getText("dir2edit"));
            btEditeurTrans.setText(texts.getText("dir2edittrans"));
            btEditeurTrans.setToolTipText(texts.getText("dir2edittransttt"));
            lIcone.setIcon(new ImageIcon("ui/tan.png"));
            lIcone.setToolTipText(texts.getText("dir2iconettt"));
            ConfigNat.getCurrentConfig().setReverseTrans(true);
            setSortieAuto(true);
        }
        chSens.setHelpName(btSens.getToolTipText());
    }

    /** Méthode redéfinie de ComponentListener
	 * Ne fait rien
	 * @param arg0 Le ComponentEvent
	 */
    public void componentHidden(ComponentEvent arg0) {
    }

    /** Méthode redéfinie de ComponentListener
	 * Ne fait rien
	 * @param arg0 Le ComponentEvent
	 */
    public void componentMoved(ComponentEvent arg0) {
    }

    /** Méthode redéfinie de ComponentListener
	 * Ne fait rien
	 * @param arg0 Le ComponentEvent
	 */
    public void componentShown(ComponentEvent arg0) {
    }

    /** Méthode redéfinie de ComponentListener
	 * Mise à jour de l'affichage lors du redimensionement
	 * @param arg0 Le ComponentEvent
	 */
    public void componentResized(ComponentEvent arg0) {
        if (getExtendedState() == Frame.MAXIMIZED_BOTH) {
            ConfigNat.getCurrentConfig().setMaximizedPrincipal(true);
        } else {
            ConfigNat.getCurrentConfig().setWidthPrincipal(getWidth());
            ConfigNat.getCurrentConfig().setHeightPrincipal(getHeight());
            ConfigNat.getCurrentConfig().setMaximizedPrincipal(false);
        }
        repaint();
    }

    /** 
     * implémentation de focusGained de FocusListener; ne fait rien 
     * @param foc Le FocusEvent
     * */
    public void focusGained(FocusEvent foc) {
    }

    /**
	 * implémentation de focusLost de FocusListener;
	 * positionne le curseur sur le dernier caractère des textes contenus dans {@link #jtfNoir} et {@link #jtfBraille}
     * @param foc Le FocusEvent
	 */
    public void focusLost(FocusEvent foc) {
        if (foc.getSource() == jtfBraille) {
            jtfBraille.setCaretPosition(jtfBraille.getText().length());
            verifieBtEditeur();
            if (ConfigNat.getCurrentConfig().getSortieAuto() && ConfigNat.getCurrentConfig().isReverseTrans()) {
                setSortieAuto(true);
            }
        } else if (foc.getSource() == jtfNoir) {
            jtfNoir.setCaretPosition(jtfNoir.getText().length());
            if (ConfigNat.getCurrentConfig().getSortieAuto() && !ConfigNat.getCurrentConfig().isReverseTrans()) {
                setSortieAuto(false);
            }
        }
    }

    /** Vérifie si l'adresse contenu dans {@link #jtfBraille} est valide, et si c'est le cas dégrise {@link #btEditeur}*/
    private void verifieBtEditeur() {
        if (new File(jtfBraille.getText()).exists()) {
            btEditeur.setEnabled(true);
        } else {
            btEditeur.setEnabled(false);
        }
    }

    /** @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)*/
    public void windowActivated(WindowEvent arg0) {
    }

    /** implémentation de WindowsListener; quitte le programme
	 * @see #quitter()
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
    public void windowClosed(WindowEvent arg0) {
        quitter();
    }

    /**@see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)*/
    public void windowClosing(WindowEvent arg0) {
    }

    /**@see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)*/
    public void windowDeactivated(WindowEvent arg0) {
    }

    /** @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)*/
    public void windowDeiconified(WindowEvent arg0) {
    }

    /** @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)*/
    public void windowIconified(WindowEvent arg0) {
    }

    /**@see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)*/
    public void windowOpened(WindowEvent arg0) {
    }

    /** Quitte le programme en enregistrant les options de l'interface graphique et la configuration actuelle*/
    private void quitter() {
        gestErreur.afficheMessage(texts.getText("savemessage"), Nat.LOG_NORMAL);
        ConfigNat.getCurrentConfig().setFBraille(jtfBraille.getText());
        ConfigNat.getCurrentConfig().setFNoir(jtfNoir.getText());
        ConfigNat.getCurrentConfig().saveUiConf();
        gestErreur.afficheMessage(texts.getText("okbye"), Nat.LOG_NORMAL);
        System.exit(0);
    }

    /**
	 * Open the TAN editor with an empty doc (documents/nouveau.tan) if tan param set to true,
	 * else open the classic editor
	 * @param tan if true open the TAN editor with an empty doc, else open the classic editor
	 */
    private void ouvrirEditeur(boolean tan) {
        if (tan) {
            EditeurTan et = new EditeurTan(null, ConfigNat.fichTmpTan, nat);
            et.setExtendedState(Frame.MAXIMIZED_BOTH);
            et.setState(MAXIMIZED_BOTH);
            et.setVisible(true);
        } else {
            ouvrirEditeur();
        }
    }

    /** Ouvre le bon éditeur pour le fichier de sortie en fonction des options choisies */
    public void ouvrirEditeur() {
        if (ConfigNat.getCurrentConfig().getUseNatEditor()) {
            if (ConfigNat.getCurrentConfig().isReverseTrans()) {
                gestErreur.afficheMessage(texts.getText("copy"), Nat.LOG_DEBUG);
                FileToolKit.copyFile(jtfNoir.getText(), EditeurTan.tmpXHTML);
                EditeurTan et = new EditeurTan(null, jtfBraille.getText(), nat);
                et.setExtendedState(Frame.MAXIMIZED_BOTH);
                et.setState(MAXIMIZED_BOTH);
                et.setVisible(true);
            } else {
                afficheFichier(jtfBraille.getText());
            }
        } else if (ConfigNat.getCurrentConfig().getUseDefaultEditor()) {
            Desktop desktop = null;
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    try {
                        desktop.open(new File(jtfBraille.getText()));
                    } catch (IOException e) {
                        gestErreur.afficheMessage(texts.getText("buginout") + jtfBraille.getText(), Nat.LOG_SILENCIEUX);
                    }
                } else {
                    gestErreur.afficheMessage(texts.getText("editornotfound"), Nat.LOG_SILENCIEUX);
                }
            } else {
                gestErreur.afficheMessage(texts.getText("desktopnotsupported"), Nat.LOG_SILENCIEUX);
            }
        } else {
            File fsortie = new File(jtfBraille.getText());
            String[] cmd = new String[2];
            try {
                cmd[0] = ConfigNat.getCurrentConfig().getEditeur();
                cmd[1] = fsortie.getCanonicalPath();
                Runtime.getRuntime().exec(cmd);
            } catch (IOException ioe) {
                gestErreur.afficheMessage(texts.getText("buginout2") + cmd[0] + texts.getText("with") + cmd[1], Nat.LOG_SILENCIEUX);
            }
        }
    }

    /**
	 * Prépare la fenêtre de dialogue {@link FenetreBugReport} et récupère les informations contenues dans les fichiers
	 * temporaires, les logs, et la sortie
	 */
    private void doBugReport() {
        File tmp = new File(ConfigNat.getUserTempFolder() + "/nat_log.1");
        String fichiers = "";
        if (tmp.exists()) {
            fichiers = fichiers + "------ LOGS ----------------------------\n" + FileToolKit.loadFileToStr(tmp.getAbsolutePath());
        }
        fichiers = fichiers + "\n------------ fin -----------------\n";
        FenetreBugReport f = new FenetreBugReport(this, fichiers);
        f.setVisible(true);
    }

    /**
	 * Classe interne de {@link FenetrePrinc} permettant de jouer un son à intervalle régulier pendant la transcription
	 */
    public class ThreadJPB extends Thread {

        /**
	     * Joue un son toutes les 5 secondes et le son de fin quand running=false
	     * en fonction des options de l'interface graphique
	     */
        @Override
        public void run() {
            setRunning(true);
            AudioClip ac;
            int cycles = 0;
            try {
                File curDir = new File("");
                ac = Applet.newAudioClip(new URL("file:" + curDir.getAbsolutePath() + "/ui/sounds/tic.au"));
                while (getRunning()) {
                    if (cycles % 5 == 0 && ConfigNat.getCurrentConfig().getSonPendantTranscription()) {
                        ac.play();
                    }
                    try {
                        sleep(1000);
                        cycles++;
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
                if (ConfigNat.getCurrentConfig().getSonFinTranscription()) {
                    if (getGestErreur().getException() == null) {
                        ac = Applet.newAudioClip(new URL("file:" + curDir.getAbsolutePath() + "/ui/sounds/fin.au"));
                    } else {
                        ac = Applet.newAudioClip(new URL("file:" + curDir.getAbsolutePath() + "/ui/sounds/erreur.au"));
                    }
                    ac.play();
                }
                if (ConfigNat.getCurrentConfig().getOuvrirEditeur()) {
                    ouvrirEditeur();
                }
            } catch (MalformedURLException mue) {
                mue.printStackTrace();
            }
        }
    }

    /**
	 * Classe interne de {@link FenetrePrinc} permettant de jouer un son à intervalle régulier pendant la transcription
	 */
    public class ThreadTrans extends Thread {

        /**
	     * Joue un son toutes les 5 secondes et le son de fin quand running=false
	     * en fonction des options de l'interface graphique
	     */
        @Override
        public void run() {
            activeTrans(false);
            setRunning(true);
            getNat().lanceScenario();
            if (getGestErreur().getException() == null) {
                getBtEditeur().setEnabled(true);
            }
            activeTrans(true);
            setRunning(false);
        }
    }
}
