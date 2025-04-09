package net.sourceforge.swinguiloc.util;

import javax.swing.*;
import java.io.*;
import javax.swing.table.*;
import net.sourceforge.swinguiloc.trans.*;
import net.sourceforge.swinguiloc.beans.*;
import java.util.*;
import net.sourceforge.swinguiloc.beans.LLabel;
import javax.swing.JTextField;
import net.sourceforge.swinguiloc.beans.LButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.JPanel;
import java.util.zip.*;
import java.awt.*;
import net.sourceforge.swinguiloc.beans.LToolButton;

/**
 * This is the LanguageFileAdministrator. It can be used to create, modify and
 * translate language files and manage contents of resource files.
 * @author Rumen Tashev
 */
public class LanguageFileAdmin extends LFrame {

    private static final long serialVersionUID = 0L;

    private JPanel jContentPane = null;

    private JMenuBar mainMenu = null;

    private LMenu fileMenu = null;

    private LMenu helpMenu = null;

    private JPanel buttonToolbar = null;

    private LButton cmdClose = null;

    private LButton cmdSave = null;

    private LButton cmdAdd = null;

    private LButton cmdDel = null;

    private LMenuItem fileOpen = null;

    private LMenuItem fileSave = null;

    private LMenuItem fileNew = null;

    private LMenuItem fileExit = null;

    private LMenuItem helpAbout = null;

    private LanguageTable contents = null;

    private LMenuItem fileSaveAs = null;

    private LanguageFile langFile;

    private LanguageFile langFileTrans;

    private boolean untitled = false;

    private boolean translating = false;

    private boolean unsaved = false;

    private LMenu transMenu = null;

    private LMenuItem transStart = null;

    private LMenuItem transNormal = null;

    private LMenu langMenu = null;

    private LMenuItem langLoad = null;

    private JScrollPane jsp;

    private JPanel metaPanel = null;

    private LLabel labelLang = null;

    private LLabel labelCountry = null;

    private LLabel labelVar = null;

    private LLabel labelVer = null;

    private JTextField inLang = null;

    private JTextField inCountry = null;

    private JTextField inVar = null;

    private JTextField inVer = null;

    private LMenuItem transLoad = null;

    private LMenuItem langRefresh = null;

    private LString errorSavingFile = new LString("* Error saving file *");

    private LString errorOpeningFile = new LString("* Error opening file *");

    private LString fileDoesNotExist = new LString("* File doesn't exist *");

    private LString errorCreatingFile = new LString("* Error creating file *");

    private LString saveLangFileFirst = new LString("* Language file should be saved first! *");

    private LString errorCaption = new LString("* Error *");

    private LString inputCaption = new LString("* Input *");

    private LString createNewZip = new LString("Create new ZIP file");

    private LString yesOption = new LString("* Yes *");

    private LString noOption = new LString("* No *");

    private LString okOption = new LString("* OK *");

    private LString msgDiscardChanges = new LString("* Exit and discard changes? *");

    private LString msgUnsavedExists = new LString("* Unsaved settings exist *");

    private LString msgNewCaptionTag = new LString("* New caption tag: *");

    private LString labelCaptionTag = new LString("* Caption Tag *", "labelCaptionTag");

    private LString labelValue = new LString("* Value *", "labelValue");

    private LString labelTransValue = new LString("* TranslatedValue *", "labelTransValue");

    private LanguageVerifier languageVerifier = null;

    private CountryVerifier countryVerifier = null;

    private LMenu localeMenu = null;

    private LMenuItem localeDetails = null;

    private LLabel labelRes = null;

    private JTextField inRes = null;

    private LResizableButton cmdBrowse = null;

    private JToolBar tools = null;

    private LToolButton cmdToolsNew = null;

    private LToolButton cmdToolsOpen = null;

    private LToolButton cmdToolsSave = null;

    private LToolButton cmdToolsRefresh = null;

    private FindFrame findFrame = null;

    private LToolButton cmdToolsSearch = null;

    /**
     * This method initializes mainMenu	
     * 	
     * @return javax.swing.JMenuBar	
     */
    private JMenuBar getMainMenu() {
        if (mainMenu == null) {
            mainMenu = new JMenuBar();
            mainMenu.add(getFileMenu());
            mainMenu.add(getLangMenu());
            mainMenu.add(getLocaleMenu());
            mainMenu.add(getTransMenu());
            mainMenu.add(getHelpMenu());
        }
        return mainMenu;
    }

    /**
     * This method initializes fileMenu	
     * 	
     * @return javax.swing.LMenu	
     */
    private LMenu getFileMenu() {
        if (fileMenu == null) {
            fileMenu = new LMenu();
            fileMenu.setText("File");
            fileMenu.setCaptionTag("fileMenu");
            fileMenu.add(getFileNew());
            fileMenu.add(getFileOpen());
            fileMenu.add(getFileSave());
            fileMenu.add(getFileSaveAs());
            fileMenu.add(getFileExit());
        }
        return fileMenu;
    }

    /**
     * This method initializes LMenu	
     * 	
     * @return javax.swing.LMenu	
     */
    private LMenu getHelpMenu() {
        if (helpMenu == null) {
            helpMenu = new LMenu();
            helpMenu.setText("Help");
            helpMenu.setCaptionTag("helpMenu");
            helpMenu.add(getHelpAbout());
        }
        return helpMenu;
    }

    /**
     * This method initializes buttonToolbar	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getButtonToolbar() {
        if (buttonToolbar == null) {
            buttonToolbar = new JPanel();
            buttonToolbar.setLayout(null);
            buttonToolbar.setPreferredSize(new java.awt.Dimension(440, 155));
            buttonToolbar.setLocation(new java.awt.Point(3, 300));
            buttonToolbar.setSize(new java.awt.Dimension(440, 155));
            buttonToolbar.add(getCmdClose(), null);
            buttonToolbar.add(getCmdSave(), null);
            buttonToolbar.add(getCmdAdd(), null);
            buttonToolbar.add(getCmdDel(), null);
            buttonToolbar.add(getMetaPanel(), null);
        }
        return buttonToolbar;
    }

    /**
     * This method initializes cmdClose	
     * 	
     * @return javax.swing.LButton	
     */
    private LButton getCmdClose() {
        if (cmdClose == null) {
            cmdClose = new LButton();
            cmdClose.setLocation(new java.awt.Point(332, 126));
            cmdClose.setText("Close");
            cmdClose.setCaptionTag("cmdClose");
            cmdClose.setSize(new java.awt.Dimension(100, 25));
            cmdClose.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LanguageFileAdmin.this.onClose();
                }
            });
        }
        return cmdClose;
    }

    /**
     * This method initializes cmdSave	
     * 	
     * @return javax.swing.LButton	
     */
    private LButton getCmdSave() {
        if (cmdSave == null) {
            cmdSave = new LButton();
            cmdSave.setLocation(new java.awt.Point(224, 126));
            cmdSave.setText("Save");
            cmdSave.setEnabled(false);
            cmdSave.setSize(new java.awt.Dimension(100, 25));
            cmdSave.setCaptionTag("cmdSave");
            cmdSave.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LanguageFileAdmin.this.onSave();
                }
            });
        }
        return cmdSave;
    }

    /**
     * This method initializes cmdAdd	
     * 	
     * @return javax.swing.LButton	
     */
    private LButton getCmdAdd() {
        if (cmdAdd == null) {
            cmdAdd = new LButton();
            cmdAdd.setBounds(new java.awt.Rectangle(8, 126, 100, 25));
            cmdAdd.setEnabled(false);
            cmdAdd.setText("Add");
            cmdAdd.setCaptionTag("cmdAdd");
            cmdAdd.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LanguageFileAdmin.this.onAdd();
                }
            });
        }
        return cmdAdd;
    }

    /**
     * This method initializes cmdDel	
     * 	
     * @return javax.swing.LButton	
     */
    private LButton getCmdDel() {
        if (cmdDel == null) {
            cmdDel = new LButton();
            cmdDel.setBounds(new java.awt.Rectangle(116, 126, 100, 25));
            cmdDel.setEnabled(false);
            cmdDel.setText("Del");
            cmdDel.setCaptionTag("cmdDel");
            cmdDel.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LanguageFileAdmin.this.onDel();
                }
            });
        }
        return cmdDel;
    }

    /**
     * This method initializes Open	
     * 	
     * @return javax.swing.LMenuItem	
     */
    private LMenuItem getFileOpen() {
        if (fileOpen == null) {
            fileOpen = new LMenuItem();
            fileOpen.setText("Open");
            fileOpen.setCaptionTag("fileOpen");
            fileOpen.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LanguageFileAdmin.this.onOpen();
                }
            });
        }
        return fileOpen;
    }

    /**
     * This method initializes Save	
     * 	
     * @return javax.swing.LMenuItem	
     */
    private LMenuItem getFileSave() {
        if (fileSave == null) {
            fileSave = new LMenuItem();
            fileSave.setText("Save");
            fileSave.setCaptionTag("fileSave");
            fileSave.setEnabled(false);
            fileSave.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LanguageFileAdmin.this.onSave();
                }
            });
        }
        return fileSave;
    }

    /**
     * This method initializes fileNew	
     * 	
     * @return javax.swing.LMenuItem	
     */
    private LMenuItem getFileNew() {
        if (fileNew == null) {
            fileNew = new LMenuItem();
            fileNew.setText("New");
            fileNew.setCaptionTag("fileNew");
            fileNew.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LanguageFileAdmin.this.onNew();
                }
            });
        }
        return fileNew;
    }

    /**
     * This method initializes Exit	
     * 	
     * @return javax.swing.LMenuItem	
     */
    private LMenuItem getFileExit() {
        if (fileExit == null) {
            fileExit = new LMenuItem();
            fileExit.setText("Exit");
            fileExit.setPreferredImageSize(new java.awt.Dimension(15, 15));
            fileExit.setCaptionTag("fileExit");
            fileExit.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LanguageFileAdmin.this.onClose();
                }
            });
        }
        return fileExit;
    }

    /**
     * This method initializes helpAbout	
     * 	
     * @return javax.swing.LMenuItem	
     */
    private LMenuItem getHelpAbout() {
        if (helpAbout == null) {
            helpAbout = new LMenuItem();
            helpAbout.setCaptionTag("helpAbout");
            helpAbout.setText("About");
            helpAbout.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    HelpAbout ha = new HelpAbout(LanguageFileAdmin.this);
                    ha.fireLanguageSwitched();
                    ha.setVisible(true);
                }
            });
        }
        return helpAbout;
    }

    /**
     * This method initializes fileSaveAs	
     * 	
     * @return javax.swing.LMenuItem	
     */
    private LMenuItem getFileSaveAs() {
        if (fileSaveAs == null) {
            fileSaveAs = new LMenuItem();
            fileSaveAs.setEnabled(false);
            fileSaveAs.setText("Save As ...");
            fileSaveAs.setCaptionTag("fileSaveAs");
            fileSaveAs.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LanguageFileAdmin.this.onSaveAs();
                }
            });
        }
        return fileSaveAs;
    }

    /**
     * This method initializes transMenu	
     * 	
     * @return javax.swing.LMenu	
     */
    private LMenu getTransMenu() {
        if (transMenu == null) {
            transMenu = new LMenu();
            transMenu.setText("Translate");
            transMenu.setCaptionTag("transMenu");
            transMenu.add(getTransStart());
            transMenu.add(getTransNormal());
            transMenu.add(getTransLoad());
        }
        return transMenu;
    }

    /**
     * This method initializes transStart	
     * 	
     * @return javax.swing.LMenuItem	
     */
    private LMenuItem getTransStart() {
        if (transStart == null) {
            transStart = new LMenuItem();
            transStart.setText("Translation mode");
            transStart.setCaptionTag("transStart");
            transStart.setEnabled(false);
            transStart.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onTranslate();
                }
            });
        }
        return transStart;
    }

    /**
     * This method initializes transNormal	
     * 	
     * @return javax.swing.LMenuItem	
     */
    private LMenuItem getTransNormal() {
        if (transNormal == null) {
            transNormal = new LMenuItem();
            transNormal.setText("Normal mode");
            transNormal.setCaptionTag("transNormal");
            transNormal.setEnabled(false);
            transNormal.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onNormalMode();
                }
            });
        }
        return transNormal;
    }

    /**
     * This method initializes langMenu	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LMenu	
     */
    private LMenu getLangMenu() {
        if (langMenu == null) {
            langMenu = new LMenu();
            langMenu.setText("Language");
            langMenu.setCaptionTag("langMenu");
            langMenu.add(getLangLoad());
            langMenu.add(getLangRefresh());
        }
        return langMenu;
    }

    /**
     * This method initializes langLoad	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LMenuItem	
     */
    private LMenuItem getLangLoad() {
        if (langLoad == null) {
            langLoad = new LMenuItem();
            langLoad.setText("Load Language File ...");
            langLoad.setPreferredImageSize(new java.awt.Dimension(15, 15));
            langLoad.setCaptionTag("langLoad");
            langLoad.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onLangLoad();
                }
            });
        }
        return langLoad;
    }

    /**
     * This method initializes metaPanel	
     * 	
     * @return javax.swing.JPanel	
     */
    private JPanel getMetaPanel() {
        if (metaPanel == null) {
            labelRes = new LLabel();
            labelRes.setBounds(new java.awt.Rectangle(9, 74, 120, 25));
            labelRes.setCaptionTag("labelRes");
            labelRes.setText("Res File:");
            labelVer = new LLabel();
            labelVer.setBounds(new java.awt.Rectangle(191, 45, 120, 25));
            labelVer.setCaptionTag("labelVer");
            labelVer.setText("Language File Version:");
            labelVar = new LLabel();
            labelVar.setBounds(new java.awt.Rectangle(191, 16, 120, 25));
            labelVar.setCaptionTag("labelVar");
            labelVar.setText("Variant:");
            labelCountry = new LLabel();
            labelCountry.setBounds(new java.awt.Rectangle(9, 45, 120, 25));
            labelCountry.setCaptionTag("labelCountry");
            labelCountry.setText("Country:");
            labelLang = new LLabel();
            labelLang.setText("Language:");
            labelLang.setSize(new java.awt.Dimension(120, 25));
            labelLang.setCaptionTag("labelLang");
            labelLang.setLocation(new java.awt.Point(9, 16));
            metaPanel = new JPanel();
            metaPanel.setLayout(null);
            metaPanel.setBounds(new java.awt.Rectangle(3, 5, 433, 114));
            LTitledBorder border = new LTitledBorder("Test");
            border.setCaptionTag("LFA Frame.borderMeta");
            this.addTranslatable(border);
            metaPanel.setBorder(border);
            metaPanel.add(labelLang, null);
            metaPanel.add(labelCountry, null);
            metaPanel.add(labelVar, null);
            metaPanel.add(labelVer, null);
            metaPanel.add(getInLang(), null);
            metaPanel.add(getInCountry(), null);
            metaPanel.add(getInVar(), null);
            metaPanel.add(getInVer(), null);
            metaPanel.add(labelRes, null);
            metaPanel.add(getInRes(), null);
            metaPanel.add(getCmdBrowse(), null);
        }
        return metaPanel;
    }

    /**
     * This method initializes inLang	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getInLang() {
        if (inLang == null) {
            inLang = new JTextField();
            inLang.setBounds(new java.awt.Rectangle(128, 16, 57, 25));
            inLang.setInputVerifier(getLanguageVerifier());
            inLang.setEnabled(false);
            inLang.addFocusListener(new java.awt.event.FocusAdapter() {

                public void focusLost(java.awt.event.FocusEvent e) {
                    if ((!translating) && (langFile != null)) {
                        if ((langFile.getLanguageEntryFor("meta.lang") == null) || !(inLang.getText().equals(langFile.getLanguageEntryFor("meta.lang")))) {
                            langFile.addEntry("meta.lang", inLang.getText());
                            unsaved = true;
                            refreshTableContents();
                        }
                    }
                }
            });
        }
        return inLang;
    }

    /**
     * This method initializes inCountry	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getInCountry() {
        if (inCountry == null) {
            inCountry = new JTextField();
            inCountry.setBounds(new java.awt.Rectangle(128, 45, 57, 25));
            inCountry.setInputVerifier(getCountryVerifier());
            inCountry.setEnabled(false);
            inCountry.addFocusListener(new java.awt.event.FocusAdapter() {

                public void focusLost(java.awt.event.FocusEvent e) {
                    if ((!translating) && (langFile != null)) {
                        if ((langFile.getLanguageEntryFor("meta.country") == null) || !(inCountry.getText().equals(langFile.getLanguageEntryFor("meta.country")))) {
                            langFile.addEntry("meta.country", inCountry.getText());
                            unsaved = true;
                            refreshTableContents();
                        }
                    }
                }
            });
        }
        return inCountry;
    }

    /**
     * This method initializes inVar	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getInVar() {
        if (inVar == null) {
            inVar = new JTextField();
            inVar.setBounds(new java.awt.Rectangle(311, 16, 112, 25));
            inVar.setEnabled(false);
            inVar.addFocusListener(new java.awt.event.FocusAdapter() {

                public void focusLost(java.awt.event.FocusEvent e) {
                    if ((langFile.getLanguageEntryFor("meta.variant") == null) || !(inVar.getText().equals(langFile.getLanguageEntryFor("meta.variant")))) {
                        langFile.addEntry("meta.variant", inVar.getText());
                        unsaved = true;
                        refreshTableContents();
                    }
                }
            });
        }
        return inVar;
    }

    /**
     * This method initializes inVer	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getInVer() {
        if (inVer == null) {
            inVer = new JTextField();
            inVer.setBounds(new java.awt.Rectangle(311, 45, 57, 25));
            inVer.setEnabled(false);
            inVer.addFocusListener(new java.awt.event.FocusAdapter() {

                public void focusLost(java.awt.event.FocusEvent e) {
                    if ((langFile.getLanguageEntryFor("meta.version") == null) || !(inVer.getText().equals(langFile.getLanguageEntryFor("meta.version")))) {
                        langFile.addEntry("meta.version", inVer.getText());
                        unsaved = true;
                        refreshTableContents();
                    }
                }
            });
        }
        return inVer;
    }

    /**
     * This method initializes transLoad	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LMenuItem	
     */
    private LMenuItem getTransLoad() {
        if (transLoad == null) {
            transLoad = new LMenuItem();
            transLoad.setText("Load Translation");
            transLoad.setEnabled(false);
            transLoad.setCaptionTag("LFA Frame.transLoad");
            transLoad.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onLoadTranslation();
                }
            });
        }
        return transLoad;
    }

    /**
     * This method initializes langRefresh	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LMenuItem	
     */
    private LMenuItem getLangRefresh() {
        if (langRefresh == null) {
            langRefresh = new LMenuItem();
            langRefresh.setCaptionTag("langRefresh.refresh");
            langRefresh.setPreferredImageSize(new java.awt.Dimension(16, 16));
            langRefresh.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LanguageFileAdmin.this.getTranslator().reloadLanguageEntries();
                    LanguageFileAdmin.this.fireLanguageSwitched();
                }
            });
        }
        return langRefresh;
    }

    /**
     * This method initializes languageVerifier	
     * 	
     * @return net.sourceforge.swinguiloc.util.LanguageVerifier	
     */
    private LanguageVerifier getLanguageVerifier() {
        if (languageVerifier == null) {
            languageVerifier = new LanguageVerifier();
        }
        return languageVerifier;
    }

    /**
     * This method initializes countryVerifier	
     * 	
     * @return net.sourceforge.swinguiloc.util.CountryVerifier	
     */
    private CountryVerifier getCountryVerifier() {
        if (countryVerifier == null) {
            countryVerifier = new CountryVerifier();
        }
        return countryVerifier;
    }

    /**
     * This method initializes localeMenu	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LMenu	
     */
    private LMenu getLocaleMenu() {
        if (localeMenu == null) {
            localeMenu = new LMenu();
            localeMenu.setCaptionTag("LFA Frame.localeMenu");
            localeMenu.setText("Locale");
            localeMenu.add(getLocaleDetails());
        }
        return localeMenu;
    }

    /**
     * This method initializes localeDetails	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LMenuItem	
     */
    private LMenuItem getLocaleDetails() {
        if (localeDetails == null) {
            localeDetails = new LMenuItem();
            localeDetails.setCaptionTag("localeDetails");
            localeDetails.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onLocaleDetails();
                }
            });
        }
        return localeDetails;
    }

    /**
     * This method initializes inRes	
     * 	
     * @return javax.swing.JTextField	
     */
    private JTextField getInRes() {
        if (inRes == null) {
            inRes = new JTextField();
            inRes.setBounds(new java.awt.Rectangle(128, 74, 185, 25));
            inRes.setEnabled(false);
            inRes.addFocusListener(new java.awt.event.FocusAdapter() {

                public void focusLost(java.awt.event.FocusEvent e) {
                    if ((langFile.getLanguageEntryFor("meta.res") == null) || !(inRes.getText().equals(langFile.getLanguageEntryFor("meta.res")))) {
                        langFile.addEntry("meta.res", inRes.getText());
                        unsaved = true;
                        refreshTableContents();
                    }
                }
            });
        }
        return inRes;
    }

    /**
     * This method initializes cmdBrowse	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LButton	
     */
    private LButton getCmdBrowse() {
        if (cmdBrowse == null) {
            cmdBrowse = new LResizableButton();
            cmdBrowse.setLocation(new java.awt.Point(323, 74));
            cmdBrowse.setText("Browse");
            cmdBrowse.setCaptionTag("cmdBrowse");
            cmdBrowse.setEnabled(false);
            cmdBrowse.setSize(new java.awt.Dimension(100, 25));
            cmdBrowse.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (inRes.getText().trim().length() == 0) return;
                    File f = new File(inRes.getText());
                    if (f.exists()) {
                        try {
                            new ZipFile(f);
                        } catch (ZipException ze) {
                            if (!createNewZipFile(true, f)) return;
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                        ResourceBrowser rb = new ResourceBrowser(LanguageFileAdmin.this, f);
                        rb.fireLanguageSwitched();
                        rb.setVisible(true);
                    } else {
                        createNewZipFile(true, f);
                    }
                }
            });
        }
        return cmdBrowse;
    }

    /**
     * This method initializes tools	
     * 	
     * @return javax.swing.JToolBar	
     */
    private JToolBar getTools() {
        if (tools == null) {
            tools = new JToolBar();
            tools.setLocation(new java.awt.Point(0, 0));
            tools.setFloatable(false);
            tools.setSize(new java.awt.Dimension(445, 40));
            tools.setBorderPainted(false);
            tools.add(getCmdToolsNew());
            tools.addSeparator();
            tools.add(getCmdToolsOpen());
            tools.addSeparator();
            tools.add(getCmdToolsSave());
            tools.addSeparator();
            tools.add(getCmdToolsRefresh());
            tools.addSeparator();
            tools.add(getCmdToolsSearch());
        }
        return tools;
    }

    /**
     * This method initializes cmdToolsNew	
     * 	
     * @return net.sourceforge.swinguiloc.util.LToolButton	
     */
    private LToolButton getCmdToolsNew() {
        if (cmdToolsNew == null) {
            cmdToolsNew = new LToolButton();
            cmdToolsNew.setPreferredSize(new java.awt.Dimension(32, 32));
            cmdToolsNew.setMinimumSize(new java.awt.Dimension(32, 32));
            cmdToolsNew.setContentAreaFilled(false);
            cmdToolsNew.setCaptionTag("cmdToolsNew");
            cmdToolsNew.setBorderPainted(false);
            cmdToolsNew.setPreferredImageSize(new java.awt.Dimension(32, 32));
            cmdToolsNew.setMaximumSize(new java.awt.Dimension(32, 32));
            cmdToolsNew.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onNew();
                }
            });
        }
        return cmdToolsNew;
    }

    /**
     * This method initializes cmdToolsOpen	
     * 	
     * @return net.sourceforge.swinguiloc.util.LToolButton	
     */
    private LToolButton getCmdToolsOpen() {
        if (cmdToolsOpen == null) {
            cmdToolsOpen = new LToolButton();
            cmdToolsOpen.setMaximumSize(new java.awt.Dimension(32, 32));
            cmdToolsOpen.setPreferredSize(new java.awt.Dimension(32, 32));
            cmdToolsOpen.setContentAreaFilled(false);
            cmdToolsOpen.setCaptionTag("cmdToolsOpen");
            cmdToolsOpen.setBorderPainted(false);
            cmdToolsOpen.setPreferredImageSize(new java.awt.Dimension(32, 32));
            cmdToolsOpen.setMinimumSize(new java.awt.Dimension(32, 32));
            cmdToolsOpen.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onOpen();
                }
            });
        }
        return cmdToolsOpen;
    }

    /**
     * This method initializes cmdToolsSave	
     * 	
     * @return net.sourceforge.swinguiloc.util.LToolButton	
     */
    private LToolButton getCmdToolsSave() {
        if (cmdToolsSave == null) {
            cmdToolsSave = new LToolButton();
            cmdToolsSave.setPreferredSize(new java.awt.Dimension(32, 32));
            cmdToolsSave.setMinimumSize(new java.awt.Dimension(32, 32));
            cmdToolsSave.setContentAreaFilled(false);
            cmdToolsSave.setCaptionTag("cmdToolsSave");
            cmdToolsSave.setBorderPainted(false);
            cmdToolsSave.setPreferredImageSize(new java.awt.Dimension(32, 32));
            cmdToolsSave.setMaximumSize(new java.awt.Dimension(32, 32));
            cmdToolsSave.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onSave();
                }
            });
        }
        return cmdToolsSave;
    }

    /**
     * This method initializes cmdToolsRefresh	
     * 	
     * @return net.sourceforge.swinguiloc.util.LToolButton	
     */
    private LToolButton getCmdToolsRefresh() {
        if (cmdToolsRefresh == null) {
            cmdToolsRefresh = new LToolButton();
            cmdToolsRefresh.setMaximumSize(new java.awt.Dimension(32, 32));
            cmdToolsRefresh.setPreferredSize(new java.awt.Dimension(32, 32));
            cmdToolsRefresh.setContentAreaFilled(false);
            cmdToolsRefresh.setCaptionTag("cmdTools.refresh");
            cmdToolsRefresh.setPreferredImageSize(new java.awt.Dimension(32, 32));
            cmdToolsRefresh.setBorderPainted(false);
            cmdToolsRefresh.setMinimumSize(new java.awt.Dimension(32, 32));
            cmdToolsRefresh.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    LanguageFileAdmin.this.getTranslator().reloadLanguageEntries();
                    LanguageFileAdmin.this.fireLanguageSwitched();
                }
            });
        }
        return cmdToolsRefresh;
    }

    /**
     * This method initializes cmdToolsSearch	
     * 	
     * @return net.sourceforge.swinguiloc.beans.LToolButton	
     */
    private LToolButton getCmdToolsSearch() {
        if (cmdToolsSearch == null) {
            cmdToolsSearch = new LToolButton();
            cmdToolsSearch.setPreferredSize(new java.awt.Dimension(32, 32));
            cmdToolsSearch.setMinimumSize(new java.awt.Dimension(32, 32));
            cmdToolsSearch.setMaximumSize(new java.awt.Dimension(32, 32));
            cmdToolsSearch.setBorderPainted(false);
            cmdToolsSearch.setCaptionTag("cmdToolsSearch");
            cmdToolsSearch.setFocusPainted(false);
            cmdToolsSearch.setOpaque(false);
            cmdToolsSearch.setContentAreaFilled(false);
            cmdToolsSearch.setPreferredImageSize(new java.awt.Dimension(32, 32));
            cmdToolsSearch.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (findFrame == null) {
                        findFrame = new FindFrame(LanguageFileAdmin.this);
                        findFrame.setLocation(getLocation().x + 30, getLocation().y + 30);
                    }
                    findFrame.setTranslator(getTranslator());
                    findFrame.fireLanguageSwitched();
                    findFrame.setVisible(true);
                }
            });
        }
        return cmdToolsSearch;
    }

    /**
     * Launches the LanguageFileAdmin.
     * @param args Command-line parameters
     */
    public static void main(String[] args) {
        LanguageFileAdmin lfa = new LanguageFileAdmin();
        String languageFile = "langfiles/lfa_en.xml";
        String fileToOpen = null;
        String dumpFile = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("--")) {
                StringTokenizer st = new StringTokenizer(args[i].substring(2), "=");
                String tokenName = st.nextToken();
                String tokenValue = null;
                if (st.hasMoreTokens()) tokenValue = st.nextToken();
                if (tokenName.equalsIgnoreCase("language") && tokenValue != null) {
                    languageFile = tokenValue;
                } else if (tokenName.equalsIgnoreCase("open") && tokenValue != null) {
                    fileToOpen = tokenValue;
                } else if (tokenName.equalsIgnoreCase("dump") && tokenValue != null) {
                    dumpFile = tokenValue;
                }
            } else if (args[i].startsWith("-") && i != (args.length - 1)) {
                if (args[i].substring(1).equalsIgnoreCase("l") && args[i + 1].endsWith("xml")) {
                    languageFile = args[i + 1];
                } else if (args[i].substring(1).equalsIgnoreCase("o") && args[i + 1].endsWith("xml")) {
                    fileToOpen = args[i + 1];
                } else if (args[i].substring(1).equalsIgnoreCase("d")) {
                    dumpFile = args[i + 1];
                }
            } else {
                System.out.println("***Warning***: Unknown console option - check your input!");
                printUsage();
            }
        }
        try {
            Translator t = new Translator(languageFile);
            if (dumpFile != null) {
                TransEventLogger transEventLogger = new TransEventLogger(new File(dumpFile), false);
                t.addTransEventListener(transEventLogger);
            }
            lfa.setTranslator(t);
            lfa.fireLanguageSwitched();
        } catch (LanguageFileException lfe) {
            printUsage();
            System.out.println(lfe.getMessage());
            System.exit(1);
        }
        if (fileToOpen != null) lfa.onOpen(new File(fileToOpen));
        lfa.setVisible(true);
    }

    /**
     * This is the default constructor
     */
    public LanguageFileAdmin() {
        super();
        initialize();
    }

    /**
     * This method initializes this LanguageFileAdmin
     * 
     * @return void
     */
    private void initialize() {
        this.setSize(454, 512);
        this.setPreferredSize(new java.awt.Dimension(454, 512));
        this.setMinimumSize(new java.awt.Dimension(454, 512));
        this.setJMenuBar(getMainMenu());
        this.setContentPane(getJContentPane());
        this.setTitle("LanguageFileAdmin");
        this.setCaptionTag("LFA Frame");
        this.addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent e) {
                onClose();
            }
        });
        errorOpeningFile.setCaptionTag("msgbox.errorOpeningFile");
        errorSavingFile.setCaptionTag("msgbox.errorSavingFile");
        errorCreatingFile.setCaptionTag("msgbox.errorCreatingFile");
        saveLangFileFirst.setCaptionTag("msgbox.saveLangFileFirst");
        errorCaption.setCaptionTag("msgbox.ErrorCaption");
        inputCaption.setCaptionTag("msgbox.inputCaption");
        yesOption.setCaptionTag("msgbox.yes");
        noOption.setCaptionTag("msgbox.no");
        okOption.setCaptionTag("msgbox.ok");
        msgDiscardChanges.setCaptionTag("msgbox.discardChanges");
        msgUnsavedExists.setCaptionTag("msgbox.unsavedChangesExist");
        fileDoesNotExist.setCaptionTag("msgbox.fileDoesNotExist");
        createNewZip.setCaptionTag("msgbox.createNewZip");
        msgNewCaptionTag.setCaptionTag("msgbox.newCaptionTag");
        this.addTranslatable(errorOpeningFile);
        this.addTranslatable(errorSavingFile);
        this.addTranslatable(errorCreatingFile);
        this.addTranslatable(saveLangFileFirst);
        this.addTranslatable(errorCaption);
        this.addTranslatable(inputCaption);
        this.addTranslatable(yesOption);
        this.addTranslatable(noOption);
        this.addTranslatable(okOption);
        this.addTranslatable(msgDiscardChanges);
        this.addTranslatable(msgUnsavedExists);
        this.addTranslatable(fileDoesNotExist);
        this.addTranslatable(createNewZip);
        this.addTranslatable(msgNewCaptionTag);
        this.addTranslatable(labelCaptionTag);
        this.addTranslatable(labelValue);
        this.addTranslatable(labelTransValue);
        setLocation(Math.abs((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - getBounds().width / 2), Math.abs((int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2 - getBounds().height / 2));
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.add(getButtonToolbar(), null);
            jContentPane.add(getTools(), null);
            jContentPane.add(getJsp(), null);
            jContentPane.addComponentListener(new java.awt.event.ComponentAdapter() {

                public void componentResized(java.awt.event.ComponentEvent e) {
                    refreshTableContents();
                    int tmpHeight = e.getComponent().getHeight();
                    int tmpWidth = e.getComponent().getWidth();
                    buttonToolbar.setLocation((tmpWidth - buttonToolbar.getWidth()) / 2, tmpHeight - buttonToolbar.getHeight());
                    jsp.setSize(getContentPane().getWidth(), getContentPane().getHeight() - buttonToolbar.getHeight() - tools.getHeight());
                    jsp.setLocation(0, tools.getHeight());
                    tools.setSize(getContentPane().getWidth(), tools.getHeight());
                }
            });
        }
        return jContentPane;
    }

    /**
     *  Invoked when user closes the window (WindowClosing) or 
     *  chooses to exit the application (Exit button, Exit menu).
     */
    private void onClose() {
        if (!askForUnsavedChanges()) return;
        if (getTranslator() != null) getTranslator().end();
        if (findFrame != null) findFrame.dispose();
        this.dispose();
    }

    /**
     *  This method is executed to load to handle the user's wish to change
     *  the language file.
     */
    private void onLangLoad() {
        JFileChooser fc = new JFileChooser(".");
        LanguageFileFilter ff = new LanguageFileFilter();
        fc.addChoosableFileFilter(ff);
        fc.setFileFilter(ff);
        int status = fc.showOpenDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            this.switchLanguage(f);
        }
        if (findFrame != null) {
            findFrame.setTranslator(getTranslator());
            findFrame.fireLanguageSwitched();
        }
    }

    /**
     * This method is executed to handle the user's wish to open
     * an existing language file.
     */
    private void onOpen() {
        if (!askForUnsavedChanges()) return;
        JFileChooser fc = new JFileChooser(".");
        LanguageFileFilter ff = new LanguageFileFilter();
        fc.addChoosableFileFilter(ff);
        fc.setFileFilter(ff);
        int status = fc.showOpenDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            onOpen(f);
        }
    }

    /**
     * Opens the given language file.
     * @param f Language file to open
     */
    private void onOpen(File f) {
        try {
            langFile = new LanguageFile(f);
        } catch (LanguageFileException lfe) {
            showError(errorOpeningFile + " " + f.getName() + "!");
            return;
        }
        untitled = false;
        unsaved = false;
        fileSave.setEnabled(true);
        fileSaveAs.setEnabled(true);
        cmdAdd.setEnabled(true);
        cmdDel.setEnabled(true);
        cmdSave.setEnabled(true);
        cmdBrowse.setEnabled(true);
        transStart.setEnabled(true);
        transLoad.setEnabled(true);
        inLang.setEnabled(true);
        inCountry.setEnabled(true);
        inVar.setEnabled(true);
        inVer.setEnabled(true);
        inRes.setEnabled(true);
        inLang.setText(langFile.getLanguageEntryFor("meta.lang"));
        inCountry.setText(langFile.getLanguageEntryFor("meta.country"));
        inVar.setText(langFile.getLanguageEntryFor("meta.variant"));
        inVer.setText(langFile.getLanguageEntryFor("meta.version"));
        inRes.setText(langFile.getLanguageEntryFor("meta.res"));
        translating = false;
        langFileTrans = null;
        refreshTableContents();
    }

    /**
     * Displays a "Save as" dialog and upon choice saves the
     * currently modified language file.
     */
    private void onSaveAs() {
        LanguageFile fileToSave;
        if (translating) fileToSave = langFileTrans; else fileToSave = langFile;
        if (fileToSave == null) return;
        JFileChooser fc = new JFileChooser(".");
        LanguageFileFilter ff = new LanguageFileFilter();
        fc.addChoosableFileFilter(ff);
        fc.setFileFilter(ff);
        int status = fc.showSaveDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (LanguageFileFilter.getExtension(f) == null) {
                f = new File(f.getAbsoluteFile() + ".xml");
            }
            if (!LanguageFileFilter.getExtension(f).equals("xml")) {
                f = new File(f.getAbsoluteFile() + ".xml");
            }
            try {
                fileToSave.saveToFile(f);
                unsaved = false;
            } catch (IOException ioe) {
                showError(errorSavingFile + " " + f.getName() + "!");
            }
        }
    }

    /**
     * Saves the currently loaded language file.
     */
    private void onSave() {
        LanguageFile fileToSave;
        if (translating) fileToSave = langFileTrans; else fileToSave = langFile;
        if (fileToSave == null) return;
        if (untitled) {
            JFileChooser fc = new JFileChooser(".");
            LanguageFileFilter ff = new LanguageFileFilter();
            fc.addChoosableFileFilter(ff);
            fc.setFileFilter(ff);
            int status = fc.showSaveDialog(this);
            if (status == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (LanguageFileFilter.getExtension(f) == null) {
                    f = new File(f.getAbsoluteFile() + ".xml");
                }
                if (!LanguageFileFilter.getExtension(f).equals("xml")) {
                    f = new File(f.getAbsoluteFile() + ".xml");
                }
                try {
                    fileToSave.saveToFile(f);
                    unsaved = false;
                } catch (IOException ioe) {
                    showError(errorSavingFile + " " + f.getName() + "!");
                    return;
                }
                untitled = false;
            }
        } else {
            try {
                fileToSave.saveToFile();
                unsaved = false;
            } catch (IOException ioe) {
                showError(errorSavingFile + "!\n" + ioe.getLocalizedMessage());
            }
        }
    }

    /**
     * Creates a new unnamed language file.
     */
    private void onNew() {
        if (!askForUnsavedChanges()) return;
        try {
            langFile = new LanguageFile("Untitled.xml");
            untitled = true;
            unsaved = true;
            fileSave.setEnabled(true);
            fileSaveAs.setEnabled(true);
            cmdAdd.setEnabled(true);
            cmdBrowse.setEnabled(true);
            cmdDel.setEnabled(true);
            cmdSave.setEnabled(true);
            transStart.setEnabled(true);
            inLang.setEnabled(true);
            inCountry.setEnabled(true);
            inVar.setEnabled(true);
            inVer.setEnabled(true);
            inRes.setEnabled(true);
            inLang.setText(langFile.getLanguageEntryFor("meta.lang"));
            inCountry.setText(langFile.getLanguageEntryFor("meta.country"));
            inVar.setText(langFile.getLanguageEntryFor("meta.variant"));
            inVer.setText(langFile.getLanguageEntryFor("meta.version"));
            inRes.setText(langFile.getLanguageEntryFor("meta.res"));
            refreshTableContents();
        } catch (LanguageFileException lfe) {
            showError(errorCreatingFile + "!");
        }
    }

    /**
     * Adds a new entry to the table.
     */
    private void onAdd() {
        String newEntryKey = "New Entry";
        int i = 1;
        while (langFile.contains(newEntryKey)) newEntryKey = "New Entry" + i++;
        newEntryKey = (String) JOptionPane.showInputDialog(this, msgNewCaptionTag, inputCaption.toString(), JOptionPane.QUESTION_MESSAGE, null, null, newEntryKey);
        if (newEntryKey == null) return;
        langFile.addEntry(newEntryKey, "");
        int selRow = langFile.getRowForValue(newEntryKey);
        refreshTableContents();
        contents.changeSelection(selRow, 1, false, false);
        unsaved = true;
    }

    /**
     * Deletes the currently selected entry from the table.
     */
    private void onDel() {
        if (contents != null) {
            int selRow = contents.getSelectedRow();
            if (selRow > -1) {
                String keyToRemove = contents.getValueAt(selRow, 0).toString();
                langFile.deleteEntry(keyToRemove);
                refreshTableContents();
                unsaved = true;
                int numOfRows = contents.getRowCount();
                if (selRow < numOfRows) {
                    contents.changeSelection(selRow, 0, true, false);
                } else {
                    if (numOfRows > 0) contents.changeSelection(numOfRows - 1, 0, true, false);
                }
            }
        }
    }

    /**
     * This method is used to refresh and redisplay
     * the contents of the table.
     */
    private void refreshTableContents() {
        contents.createDefaultColumnsFromModel();
        contents.revalidate();
        jsp.revalidate();
        contents.repaint();
        jsp.repaint();
    }

    /**
     * Switch to translation mode.
     */
    private void onTranslate() {
        if (unsaved) {
            showError(saveLangFileFirst.toString());
            return;
        }
        try {
            langFileTrans = new LanguageFile("");
            for (int i = 0; i < langFile.getNumberOfEntries(); i++) {
                langFileTrans.addEntry(langFile.getTableValueFor(i, 0), "");
            }
        } catch (LanguageFileException lfe) {
            showError(errorCreatingFile.toString() + "!");
            return;
        }
        translating = true;
        transNormal.setEnabled(true);
        transStart.setEnabled(false);
        cmdAdd.setEnabled(false);
        cmdDel.setEnabled(false);
        inLang.setText(langFileTrans.getLanguageEntryFor("meta.lang"));
        inCountry.setText(langFileTrans.getLanguageEntryFor("meta.country"));
        inVar.setText(langFileTrans.getLanguageEntryFor("meta.variant"));
        inVer.setText(langFileTrans.getLanguageEntryFor("meta.version"));
        inRes.setText(langFileTrans.getLanguageEntryFor("meta.res"));
        refreshTableContents();
    }

    /**
     * Returns from translation mode.
     */
    private void onNormalMode() {
        if (!askForUnsavedChanges()) return;
        if (unsaved) {
            Object[] options = { yesOption.toString(), noOption.toString() };
            int status = JOptionPane.showOptionDialog(LanguageFileAdmin.this, msgDiscardChanges.toString(), msgUnsavedExists.toString(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            if (status == JOptionPane.NO_OPTION) return;
        }
        translating = false;
        langFileTrans = null;
        transStart.setEnabled(true);
        transNormal.setEnabled(false);
        transLoad.setEnabled(true);
        unsaved = false;
        inLang.setText(langFile.getLanguageEntryFor("meta.lang"));
        inCountry.setText(langFile.getLanguageEntryFor("meta.country"));
        inVar.setText(langFile.getLanguageEntryFor("meta.variant"));
        inVer.setText(langFile.getLanguageEntryFor("meta.version"));
        inRes.setText(langFile.getLanguageEntryFor("meta.res"));
        cmdAdd.setEnabled(true);
        cmdDel.setEnabled(true);
        refreshTableContents();
    }

    /**
     * Loads a xml file that contains translation of the currently
     * loaded file.
     */
    private void onLoadTranslation() {
        if (unsaved) {
            showError(saveLangFileFirst.toString());
            return;
        }
        JFileChooser fc = new JFileChooser(".");
        LanguageFileFilter ff = new LanguageFileFilter();
        fc.addChoosableFileFilter(ff);
        fc.setFileFilter(ff);
        int status = fc.showOpenDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                LanguageFile tmpLangFile = new LanguageFile(f);
                langFileTrans = new LanguageFile("");
                langFileTrans.setLangFile(tmpLangFile.getLangFile());
                for (int i = 0; i < langFile.getNumberOfEntries(); i++) {
                    String key = langFile.getTableValueFor(i, 0);
                    langFileTrans.addEntry(key, tmpLangFile.getLanguageEntryFor(key) != null ? tmpLangFile.getLanguageEntryFor(key) : "");
                }
            } catch (LanguageFileException lfe) {
                showError(errorOpeningFile + " " + f.getName());
                return;
            }
            translating = true;
            transStart.setEnabled(false);
            transNormal.setEnabled(true);
            cmdAdd.setEnabled(false);
            cmdDel.setEnabled(false);
            inLang.setText(langFileTrans.getLanguageEntryFor("meta.lang"));
            inCountry.setText(langFileTrans.getLanguageEntryFor("meta.country"));
            inVar.setText(langFileTrans.getLanguageEntryFor("meta.variant"));
            inVer.setText(langFileTrans.getLanguageEntryFor("meta.version"));
            inRes.setText(langFileTrans.getLanguageEntryFor("meta.res"));
            refreshTableContents();
        }
    }

    /**
     * This method is executed to load to handle the user's wish to 
     * display the details of the currently loaded locale.
     */
    private void onLocaleDetails() {
        Locale loc = null;
        if (langFile != null) {
            String lang = langFile.getLanguageEntryFor("meta.lang");
            String country = langFile.getLanguageEntryFor("meta.country");
            String variant = langFile.getLanguageEntryFor("meta.variant");
            if ((lang != null) && (country != null) && (variant != null)) {
                if (lang.trim().length() != 0) {
                    if (country.trim().length() != 0) {
                        if (variant.trim().length() != 0) {
                            loc = new Locale(lang, country, variant);
                        } else {
                            loc = new Locale(lang, country);
                        }
                    } else {
                        loc = new Locale(lang);
                    }
                }
            }
        }
        if (loc != null) {
            LocaleDisplay ld = new LocaleDisplay(this, loc);
            ld.fireLanguageSwitched();
            ld.setVisible(true);
        }
    }

    /**
     * Creates and returns the data model for the table showing
     * the language entries in a file.
     * @return Table data model
     */
    private TableModel getDataModel() {
        TableModel dm = new AbstractTableModel() {

            private static final long serialVersionUID = 0L;

            public int getRowCount() {
                if (langFile != null) return langFile.getNumberOfEntries(); else return 0;
            }

            public int getColumnCount() {
                if (translating) return 3; else return 2;
            }

            public Object getValueAt(int row, int col) {
                if (translating) {
                    if (col < 2) return langFile.getTableValueFor(row, col); else return langFileTrans.getTableValueFor(row, 1);
                } else return langFile.getTableValueFor(row, col);
            }

            public String getColumnName(int col) {
                Object colNames[] = { labelCaptionTag, labelValue, labelTransValue };
                if (col < colNames.length) return colNames[col].toString(); else return "";
            }

            public boolean isCellEditable(int row, int col) {
                if (translating) return (col == 2); else {
                    if ((col == 1) && (getValueAt(row, 0).toString().startsWith("meta"))) return false; else if (getValueAt(row, col).toString().startsWith("meta")) return false; else return true;
                }
            }

            public void setValueAt(Object o, int row, int col) {
                if (getValueAt(row, col).toString().equals(o.toString())) return;
                unsaved = true;
                if (translating) {
                    if (col == 2) {
                        langFileTrans.setValueForTableData(o.toString(), row, 1);
                    }
                } else {
                    langFile.setValueForTableData(o.toString(), row, col);
                }
                LanguageFileAdmin.this.contents.repaint();
            }
        };
        return dm;
    }

    /**
     * Displays a curstom error message.
     * @param msg Error message
     * @param caption Caption for the error message
     */
    private void showError(String msg, String caption) {
        Object[] options = { "OK" };
        JOptionPane.showOptionDialog(LanguageFileAdmin.this, msg, caption, JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
    }

    /**
     * Displays a curstom error message with the default caption "Error".
     * @param msg Error message
     */
    private void showError(String msg) {
        showError(msg, errorCaption.toString());
    }

    private boolean createNewZipFile(boolean ask, File f) {
        boolean status = ask;
        if (status) {
            Object[] options = { yesOption, noOption };
            int result = JOptionPane.showOptionDialog(this, createNewZip.toString(), fileDoesNotExist.toString(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            status = (result == JOptionPane.YES_OPTION);
        }
        if (status) {
            try {
                f.createNewFile();
                FileOutputStream fos = new FileOutputStream(f);
                ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(fos));
                ZipEntry newZipEntry = new ZipEntry("Readme.txt");
                newZipEntry.setComment("Important information");
                zos.putNextEntry(newZipEntry);
                OutputStreamWriter osw = new OutputStreamWriter(zos);
                osw.write("This is a sample resource file. All data like ");
                osw.write("images for icons and buttons sould be contained here. ");
                osw.write("This file must have at least one entry (ZIP files cannot ");
                osw.write("be empty). \n\n ");
                osw.write("Commentsto files are very important, as they are ");
                osw.write("matched with the caption tags of translatable objects.");
                osw.close();
                zos.close();
                fos.close();
                return true;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return false;
    }

    /**
     * This method warns the user if unsaved changes exist. User input is
     * then returned in form of a boolean labelValue.
     * @return True if user wishes to ignore changes.
     */
    private boolean askForUnsavedChanges() {
        if (unsaved) {
            Object[] options = { yesOption.toString(), noOption.toString() };
            int status = JOptionPane.showOptionDialog(LanguageFileAdmin.this, msgDiscardChanges.toString(), msgUnsavedExists.toString(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            if (status == JOptionPane.NO_OPTION) return false;
        }
        return true;
    }

    /** 
     * Print command line usage
     */
    private static void printUsage() {
        System.err.println("Usage: java LanguageFileAdmin [options]\n" + "Options:\n" + "--language=path-to/lang_file.xml or -l path-to/lang_file.xml\n\tlaunch application using other than the default (english) language file\n" + "--open=path-to/lang_file.xml or -o path-to/lang_file.xml\n\tload language file for editing" + "--dump=filename.txt or -d filename.txt\n\tCreate filename.txt and populate it with statistics");
        System.exit(-1);
    }

    /**
     * Searches in the currently loaded language entries for the
     * searchExpression, using the options specified. If a match is found
     * it gains focus in the table showing the contents.
     * 
     * @param serachExpression String to search for
     * @param caseSensitive Whether the search is case sensitive or not
     * @param wholeWords Whether only whole words are matched
     * @param forward Whether the search is in the forward or reverse direction
     * @param col Which column has to be searched
     */
    protected void find(String searchExpression, boolean caseSensitive, boolean wholeWords, boolean forward, int col) {
        int elementCount = contents.getRowCount();
        if (contents == null || elementCount < 1) return;
        String[] elements = new String[elementCount];
        for (int i = 0; i < elementCount; i++) elements[i] = contents.getValueAt(i, col).toString();
        int selRow = contents.getSelectedRow();
        if (selRow == -1) {
            if (forward) selRow = 0; else selRow = elementCount - 1;
        } else {
            if (forward && selRow < elementCount) selRow++; else if (!forward && selRow > 0) selRow--;
        }
        if (forward) {
            for (int i = selRow; i < elementCount; i++) {
                if (compareStrings(elements[i], searchExpression, caseSensitive, wholeWords)) {
                    contents.changeSelection(i, col, false, false);
                    return;
                }
            }
        } else {
            for (int i = selRow; i >= 0; i--) {
                if (compareStrings(elements[i], searchExpression, caseSensitive, wholeWords)) {
                    contents.changeSelection(i, col, false, false);
                    return;
                }
            }
        }
    }

    /**
     * Compares the two Strings arg1 and arg2 according to the two boolean
     * settings. The expressionSearched String is searched for occurances
     * of the searchExpression String. If any are found, this method returns true.
     * @param expressionSearched A String to search
     * @param searchExpression A String to search occurences of
     * @param caseSensitive Wheather the search is case sensitive or not
     * @param wholeWords Whether only whole words are matched
     * @return
     */
    private boolean compareStrings(String expressionSearched, String searchExpression, boolean caseSensitive, boolean wholeWords) {
        if (!caseSensitive && !wholeWords) {
            return expressionSearched.toLowerCase().indexOf(searchExpression.toLowerCase()) != -1;
        } else if (caseSensitive && !wholeWords) {
            return expressionSearched.indexOf(searchExpression) != -1;
        } else if (!caseSensitive && wholeWords) {
            if (expressionSearched.toLowerCase().matches("[.]*[^a-zA-Z]+" + searchExpression.toLowerCase() + "[^a-zA-Z]+[.]*") || expressionSearched.toLowerCase().matches(searchExpression.toLowerCase() + "[^a-zA-Z]+[.]*") || expressionSearched.toLowerCase().matches("[.]*[^a-zA-Z]+" + searchExpression.toLowerCase()) || expressionSearched.toLowerCase().equals(searchExpression.toLowerCase())) return true; else return false;
        } else {
            if (expressionSearched.matches("[.]*[^a-zA-Z]+" + searchExpression + "[^a-zA-Z]+[.]*") || expressionSearched.matches(searchExpression + "[^a-zA-Z]+[.]*") || expressionSearched.matches("[.]*[^a-zA-Z]+" + searchExpression) || expressionSearched.equals(searchExpression)) return true; else return false;
        }
    }

    public JScrollPane getJsp() {
        if (jsp == null) {
            jsp = new JScrollPane(getContents());
        }
        return jsp;
    }

    public LanguageTable getContents() {
        if (contents == null) {
            contents = new LanguageTable(getDataModel());
            contents.setAutoCreateColumnsFromModel(true);
            contents.setSurrendersFocusOnKeystroke(true);
            contents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        return contents;
    }
}
