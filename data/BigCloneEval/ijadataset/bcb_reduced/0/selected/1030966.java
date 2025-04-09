package org.google.translate.desktop.settings;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.google.translate.api.v2.core.model.Language;
import org.google.translate.desktop.GoogleTranslateDesktop;
import org.google.translate.desktop.RegisterHotKeyException;
import org.google.translate.desktop.main.TranslateForm;
import org.google.translate.desktop.settings.history.HistoryDialog;
import org.google.translate.desktop.utils.Cache;
import org.google.translate.desktop.utils.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SettingsDialog extends JDialog {

    private static final long serialVersionUID = 793917212207992953L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsDialog.class);

    private static final LookAndFeel[] SUPPORTED_LOOK_AND_FEEL = new LookAndFeel[] { new LookAndFeel(UIManager.getSystemLookAndFeelClassName()), new LookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()) };

    private JPanel contentPane;

    private JButton okButton;

    private JButton cancelButton;

    private JComboBox selectedOriginalLanguageComboBox;

    private JRadioButton selectedOriginalLanguageRadioButton;

    private JRadioButton lastOriginalUsedLanguageRadioButton;

    private JRadioButton lastTranslatedUsedLanguageRadioButton;

    private JComboBox selectedTranslatedLanguageComboBox;

    private JRadioButton selectedTranslatedLanguageRadioButton;

    private JCheckBox alwaysOnTopCheckBox;

    private JCheckBox useSystemTrayCheckBox;

    private JCheckBox advancedModeCheckBox;

    private JCheckBox wrapTextCheckBox;

    private JComboBox lookAndFeelComboBox;

    private JCheckBox checkForUpdatesOnCheckBox;

    private JButton checkForUpdatedNowButton;

    private JCheckBox startMinimizedCheckBox;

    private JTextField proxyHostTextField;

    private JTextField proxyPortTextField;

    private JTextField proxyUserTextField;

    private JPasswordField proxyPasswordTextField;

    private JButton translationFormPositionButton;

    private JTextField googleApiKeyTextField;

    private JButton helpButton;

    private JCheckBox enableHistoryCheckBox;

    private JButton clearHistoryButton;

    private JButton showHistoryButton;

    private JCheckBox hotKeyCtrlCheckBox;

    private JCheckBox hotKeyShiftCheckBox;

    private JCheckBox hotKeyAltCheckBox;

    private JCheckBox hotKeyWinCheckBox;

    private JComboBox hotKeyCharCombo;

    private JCheckBox hotKeyAutoPasteAndTranslateCheckBox;

    private JCheckBox hotKeyStayInSystemTrayCheckBox;

    private Settings settings;

    public SettingsDialog(JFrame parent, Settings settings) {
        super(parent, SwingUtils.getMessage("settingsDialog.title"), true);
        this.settings = settings;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(okButton);
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                okActionPerformed();
            }
        });
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancelActionPerformed();
            }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                cancelActionPerformed();
            }
        });
        contentPane.registerKeyboardAction(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancelActionPerformed();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        googleApiKeyTextField.setText(settings.getApiKey());
        Language defaultOriginalLanguage = prepareLanguageComponents(lastOriginalUsedLanguageRadioButton, selectedOriginalLanguageRadioButton, selectedOriginalLanguageComboBox, settings.getDefaultOriginalLanguage(), settings.getLastOriginalLanguage());
        Language defaultTranslatedLanguage = prepareLanguageComponents(lastTranslatedUsedLanguageRadioButton, selectedTranslatedLanguageRadioButton, selectedTranslatedLanguageComboBox, settings.getDefaultTranslatedLanguage(), settings.getLastTranslatedLanguage());
        DefaultComboBoxModel originalLanguageComboBoxModel = new DefaultComboBoxModel();
        DefaultComboBoxModel translatedLanguageComboBoxModel = new DefaultComboBoxModel();
        TranslateForm.initLanguageComboBoxes(defaultOriginalLanguage, defaultTranslatedLanguage, originalLanguageComboBoxModel, translatedLanguageComboBoxModel);
        selectedOriginalLanguageComboBox.setModel(originalLanguageComboBoxModel);
        selectedTranslatedLanguageComboBox.setModel(translatedLanguageComboBoxModel);
        advancedModeCheckBox.setSelected(settings.isAdvancedMode());
        alwaysOnTopCheckBox.setSelected(settings.isAlwaysOnTop());
        boolean isSystemTraySupported = SystemTray.isSupported();
        useSystemTrayCheckBox.setSelected(settings.isUseSystemTray() && isSystemTraySupported);
        if (!isSystemTraySupported) {
            SwingUtils.setEnabled(useSystemTrayCheckBox, false, "settingsDialog.useSystemTray.disabled");
        } else {
            useSystemTrayCheckBox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    evaluateHotKeyStayInSystemTray();
                }
            });
        }
        wrapTextCheckBox.setSelected(settings.isWrapText());
        for (LookAndFeel lookAndFeel : SUPPORTED_LOOK_AND_FEEL) {
            lookAndFeelComboBox.addItem(lookAndFeel);
            if (lookAndFeel.getClassName().equals(settings.getLookAndFeel())) {
                lookAndFeelComboBox.setSelectedItem(lookAndFeel);
            }
        }
        checkForUpdatesOnCheckBox.setSelected(settings.isCheckForUpdatesOnStartup());
        checkForUpdatedNowButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                checkForUpdatedNowActionPerformed();
            }
        });
        startMinimizedCheckBox.setSelected(settings.isStartMinimized());
        if (parent.getX() == settings.getTranslateFormX() && parent.getY() == settings.getTranslateFormY()) {
            translationFormPositionButton.setText(SwingUtils.getMessage("settingsDialog.defaultPosition"));
        }
        translationFormPositionButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                translationFormPositionActionPerformed();
            }
        });
        proxyHostTextField.setText(settings.getProxyHost());
        proxyPortTextField.setText(settings.getProxyPort());
        proxyUserTextField.setText(settings.getProxyUser());
        proxyPasswordTextField.setText(settings.getProxyPassword());
        enableHistoryCheckBox.setSelected(settings.isHistoryEnabled());
        helpButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                helpActionPerformed();
            }
        });
        clearHistoryButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                clearHistoryActionPerformed();
            }
        });
        showHistoryButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                showHistoryActionPerformed();
            }
        });
        hotKeyCharCombo.addItem(null);
        for (int ascii = 'A'; ascii <= 'Z'; ascii++) {
            hotKeyCharCombo.addItem((char) ascii);
        }
        if (settings.getHotKeyModifier() != null) {
            for (HotKeyModifier hotKeyModifier : settings.getHotKeyModifier()) {
                if (hotKeyModifier == HotKeyModifier.CTRL) {
                    hotKeyCtrlCheckBox.setSelected(true);
                } else if (hotKeyModifier == HotKeyModifier.ALT) {
                    hotKeyAltCheckBox.setSelected(true);
                } else if (hotKeyModifier == HotKeyModifier.SHIFT) {
                    hotKeyShiftCheckBox.setSelected(true);
                } else if (hotKeyModifier == HotKeyModifier.WINDOWS) {
                    hotKeyWinCheckBox.setSelected(true);
                } else {
                    throw new RuntimeException("Unsupported HotKeyModifier " + hotKeyModifier);
                }
            }
        }
        if (settings.getHotKeyChar() > 0) {
            hotKeyCharCombo.setSelectedItem((char) settings.getHotKeyChar());
        }
        hotKeyAutoPasteAndTranslateCheckBox.setSelected(settings.isHotKeyAutoPasteAndTranslate());
        hotKeyAutoPasteAndTranslateCheckBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                evaluateHotKeyStayInSystemTray();
            }
        });
        hotKeyStayInSystemTrayCheckBox.setSelected(settings.isHotKeyStayInSystemTray());
        ActionListener evaluateHotKeyEnabledListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                evaluateHotKeyEnabled();
            }
        };
        hotKeyCtrlCheckBox.addActionListener(evaluateHotKeyEnabledListener);
        hotKeyAltCheckBox.addActionListener(evaluateHotKeyEnabledListener);
        hotKeyShiftCheckBox.addActionListener(evaluateHotKeyEnabledListener);
        hotKeyWinCheckBox.addActionListener(evaluateHotKeyEnabledListener);
        hotKeyCharCombo.addActionListener(evaluateHotKeyEnabledListener);
        evaluateHotKeyEnabled();
        pack();
        setLocationRelativeTo(parent);
    }

    private void evaluateHotKeyEnabled() {
        boolean hotKeyEnabled = hotKeyCtrlCheckBox.isSelected() || hotKeyAltCheckBox.isSelected() || hotKeyShiftCheckBox.isSelected() || hotKeyWinCheckBox.isSelected() || hotKeyCharCombo.getSelectedItem() != null;
        SwingUtils.setEnabled(hotKeyAutoPasteAndTranslateCheckBox, hotKeyEnabled, "settingsDialog.hotKey.autoPateAndTranslate.disabled");
        evaluateHotKeyStayInSystemTray();
    }

    private void evaluateHotKeyStayInSystemTray() {
        SwingUtils.setEnabled(hotKeyStayInSystemTrayCheckBox, useSystemTrayCheckBox.isSelected() && hotKeyAutoPasteAndTranslateCheckBox.isSelected() && hotKeyAutoPasteAndTranslateCheckBox.isEnabled(), "settingsDialog.hotKey.stayInSystemTray.disabled", useSystemTrayCheckBox.getText(), hotKeyAutoPasteAndTranslateCheckBox.getText());
    }

    private void showHistoryActionPerformed() {
        HistoryDialog historyDialog = new HistoryDialog(this);
        historyDialog.setVisible(true);
    }

    private void clearHistoryActionPerformed() {
        int answer = JOptionPane.showConfirmDialog(this, SwingUtils.getMessage("settingsDialog.clearHistoryBody"), SwingUtils.getMessage("settingsDialog.clearHistoryTitle"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.OK_OPTION) {
            try {
                Cache.instance().clearHistory();
            } catch (Exception e) {
                SwingUtils.showError(this, e, "settingsDialog.cannotClearHistory");
            }
        }
    }

    private void helpActionPerformed() {
        boolean showMessage = !Desktop.isDesktopSupported();
        if (!showMessage) {
            try {
                Desktop.getDesktop().browse(new URI("http://code.google.com/apis/language/translate/v2/using_rest.html#auth"));
            } catch (Exception e) {
                LOGGER.error("Exception browsing to Google docs", e);
                showMessage = true;
            }
        }
        if (showMessage) {
            JOptionPane.showMessageDialog(this, SwingUtils.getMessage("settingsDialog.googleApiKeyHelp"));
        }
    }

    private void translationFormPositionActionPerformed() {
        int x = -1;
        int y = -1;
        boolean rememberPosition = translationFormPositionButton.getText().equals(SwingUtils.getMessage("settingsDialog.rememberPosition"));
        if (rememberPosition) {
            x = getParent().getX();
            y = getParent().getY();
            translationFormPositionButton.setText(SwingUtils.getMessage("settingsDialog.defaultPosition"));
        } else {
            translationFormPositionButton.setText(SwingUtils.getMessage("settingsDialog.rememberPosition"));
        }
        settings.setTranslateFormX(x);
        settings.setTranslateFormY(y);
        if (useSystemTrayCheckBox.isSelected()) {
            JOptionPane.showMessageDialog(this, SwingUtils.getMessage("settingsDialog.settingsWillApplyAfterRestart"), GoogleTranslateDesktop.APPLICATION_NAME, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void checkForUpdatedNowActionPerformed() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            GoogleTranslateDesktop.checkForUpdates(this, false);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private static Language prepareLanguageComponents(JRadioButton lastUsedLanguageRadioButton, JRadioButton selectedLanguageRadioButton, final JComboBox selectedLanguageComboBox, String defaultLanguage, String lastLanguage) {
        lastUsedLanguageRadioButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtils.setEnabled(selectedLanguageComboBox, false, "settingsDialog.selectedLanguage.disabled");
            }
        });
        selectedLanguageRadioButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtils.enable(selectedLanguageComboBox);
            }
        });
        if (defaultLanguage != null) {
            selectedLanguageRadioButton.doClick();
        } else {
            defaultLanguage = lastLanguage;
            lastUsedLanguageRadioButton.doClick();
        }
        return Cache.instance().getLanguage(defaultLanguage);
    }

    private void okActionPerformed() {
        try {
            String newApiKey = googleApiKeyTextField.getText();
            if (!newApiKey.equals(settings.getApiKey())) {
                GoogleTranslateDesktop.refreshTranslator(newApiKey);
                Cache.loadFromApi();
            }
            settings.setApiKey(newApiKey);
            String defaultOriginalLanguage = null;
            if (selectedOriginalLanguageRadioButton.isSelected()) {
                defaultOriginalLanguage = TranslateForm.getSelectedLanguageCode(selectedOriginalLanguageComboBox);
            }
            settings.setDefaultOriginalLanguage(defaultOriginalLanguage);
            String defaultTranslatedLanguage = null;
            if (selectedTranslatedLanguageRadioButton.isSelected()) {
                defaultTranslatedLanguage = TranslateForm.getSelectedLanguageCode(selectedTranslatedLanguageComboBox);
            }
            settings.setDefaultTranslatedLanguage(defaultTranslatedLanguage);
            settings.setAdvancedMode(advancedModeCheckBox.isSelected());
            settings.setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
            settings.setUseSystemTray(useSystemTrayCheckBox.isSelected());
            settings.setWrapText(wrapTextCheckBox.isSelected());
            LookAndFeel lookAndFeel = (LookAndFeel) lookAndFeelComboBox.getSelectedItem();
            settings.setLookAndFeel(lookAndFeel.getClassName());
            settings.setCheckForUpdatesOnStartup(checkForUpdatesOnCheckBox.isSelected());
            settings.setStartMinimized(startMinimizedCheckBox.isSelected());
            String proxyPort = proxyPortTextField.getText();
            if (!proxyPort.isEmpty() && !proxyPort.matches("([0-9]*)")) {
                throw new NumberFormatException(SwingUtils.getMessage("errorMessage.invalidPositiveIntegerValue", "Proxy port"));
            }
            settings.setProxyHost(proxyHostTextField.getText());
            settings.setProxyPort(proxyPort);
            settings.setProxyUser(proxyUserTextField.getText());
            settings.setProxyPassword(String.valueOf(proxyPasswordTextField.getPassword()));
            settings.setHistoryEnabled(enableHistoryCheckBox.isSelected());
            java.util.List<HotKeyModifier> hotKeyModifiers = new ArrayList<HotKeyModifier>();
            if (hotKeyCtrlCheckBox.isSelected()) {
                hotKeyModifiers.add(HotKeyModifier.CTRL);
            }
            if (hotKeyAltCheckBox.isSelected()) {
                hotKeyModifiers.add(HotKeyModifier.ALT);
            }
            if (hotKeyShiftCheckBox.isSelected()) {
                hotKeyModifiers.add(HotKeyModifier.SHIFT);
            }
            if (hotKeyWinCheckBox.isSelected()) {
                hotKeyModifiers.add(HotKeyModifier.WINDOWS);
            }
            if (!hotKeyModifiers.isEmpty()) {
                settings.setHotKeyModifier(hotKeyModifiers.toArray(new HotKeyModifier[hotKeyModifiers.size()]));
            } else {
                settings.setHotKeyModifier(null);
            }
            Character hotKeyChar = (Character) hotKeyCharCombo.getSelectedItem();
            if (hotKeyChar != null) {
                settings.setHotKeyChar(hotKeyChar);
            } else {
                settings.setHotKeyChar(0);
            }
            settings.setHotKeyAutoPasteAndTranslate(hotKeyAutoPasteAndTranslateCheckBox.isSelected());
            settings.setHotKeyStayInSystemTray(hotKeyStayInSystemTrayCheckBox.isSelected());
            try {
                GoogleTranslateDesktop.registerGlobalHotKeys(settings);
            } catch (RegisterHotKeyException e) {
                SwingUtils.showError(this, e, "errorMessage.cannotRegisterHotKeys");
            }
            settings.save();
            dispose();
        } catch (Exception e) {
            SwingUtils.showError(this, e, "errorMessage.cannotSaveSettings", Settings.SETTINGS_FILE.getAbsolutePath());
        }
    }

    private void cancelActionPerformed() {
        dispose();
    }

    {
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        okButton = new JButton();
        okButton.setText("OK");
        okButton.setMnemonic('O');
        okButton.setDisplayedMnemonicIndex(0);
        panel2.add(okButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        cancelButton = new JButton();
        cancelButton.setText("Cancel");
        cancelButton.setMnemonic('C');
        cancelButton.setDisplayedMnemonicIndex(0);
        panel2.add(cancelButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(5, 1, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel4.setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.translateFrom")));
        lastOriginalUsedLanguageRadioButton = new JRadioButton();
        this.$$$loadButtonText$$$(lastOriginalUsedLanguageRadioButton, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.lastUsedLanguage"));
        panel4.add(lastOriginalUsedLanguageRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        selectedOriginalLanguageComboBox = new JComboBox();
        panel5.add(selectedOriginalLanguageComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectedOriginalLanguageRadioButton = new JRadioButton();
        this.$$$loadButtonText$$$(selectedOriginalLanguageRadioButton, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.selectedLanguage"));
        panel5.add(selectedOriginalLanguageRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel6.setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.translateTo")));
        lastTranslatedUsedLanguageRadioButton = new JRadioButton();
        this.$$$loadButtonText$$$(lastTranslatedUsedLanguageRadioButton, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.lastUsedLanguage"));
        panel6.add(lastTranslatedUsedLanguageRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panel7, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        selectedTranslatedLanguageComboBox = new JComboBox();
        panel7.add(selectedTranslatedLanguageComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectedTranslatedLanguageRadioButton = new JRadioButton();
        this.$$$loadButtonText$$$(selectedTranslatedLanguageRadioButton, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.selectedLanguage"));
        panel7.add(selectedTranslatedLanguageRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel8 = new JPanel();
        panel8.setLayout(new GridLayoutManager(4, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel8, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel8.setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.windowOptions")));
        alwaysOnTopCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(alwaysOnTopCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.alwaysOnTop"));
        panel8.add(alwaysOnTopCheckBox, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        useSystemTrayCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(useSystemTrayCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.useSystemTray"));
        panel8.add(useSystemTrayCheckBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        advancedModeCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(advancedModeCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.advancedMode"));
        panel8.add(advancedModeCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        wrapTextCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(wrapTextCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.wrapText"));
        panel8.add(wrapTextCheckBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel9 = new JPanel();
        panel9.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel8.add(panel9, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        lookAndFeelComboBox = new JComboBox();
        panel9.add(lookAndFeelComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.lookAndFeel"));
        panel9.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startMinimizedCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(startMinimizedCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.startMinimized"));
        panel8.add(startMinimizedCheckBox, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        translationFormPositionButton = new JButton();
        this.$$$loadButtonText$$$(translationFormPositionButton, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.rememberPosition"));
        panel8.add(translationFormPositionButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel10 = new JPanel();
        panel10.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel10, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel10.setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.applicationOptions")));
        final JPanel panel11 = new JPanel();
        panel11.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(panel11, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        checkForUpdatesOnCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(checkForUpdatesOnCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.checkForUpdatesOnStartup"));
        panel11.add(checkForUpdatesOnCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkForUpdatedNowButton = new JButton();
        this.$$$loadButtonText$$$(checkForUpdatedNowButton, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.checkForUpdatesNow"));
        panel11.add(checkForUpdatedNowButton, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel12 = new JPanel();
        panel12.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel11.add(panel12, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel12.setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.history")));
        enableHistoryCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(enableHistoryCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.enableHistory"));
        panel12.add(enableHistoryCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        clearHistoryButton = new JButton();
        this.$$$loadButtonText$$$(clearHistoryButton, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.clearHistory"));
        panel12.add(clearHistoryButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        showHistoryButton = new JButton();
        this.$$$loadButtonText$$$(showHistoryButton, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.showHistory"));
        panel12.add(showHistoryButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel13 = new JPanel();
        panel13.setLayout(new GridLayoutManager(2, 5, new Insets(0, 0, 0, 0), -1, -1));
        panel11.add(panel13, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel13.setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.hotKey")));
        hotKeyCtrlCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(hotKeyCtrlCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.hotKey.Ctrl"));
        panel13.add(hotKeyCtrlCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hotKeyShiftCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(hotKeyShiftCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.hotKey.Shift"));
        panel13.add(hotKeyShiftCheckBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hotKeyAltCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(hotKeyAltCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.hotKey.Alt"));
        panel13.add(hotKeyAltCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hotKeyWinCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(hotKeyWinCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.hotKey.Win"));
        panel13.add(hotKeyWinCheckBox, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hotKeyCharCombo = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        hotKeyCharCombo.setModel(defaultComboBoxModel1);
        panel13.add(hotKeyCharCombo, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel14 = new JPanel();
        panel14.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel13.add(panel14, new GridConstraints(1, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        hotKeyAutoPasteAndTranslateCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(hotKeyAutoPasteAndTranslateCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.hotKey.autoPateAndTranslate"));
        panel14.add(hotKeyAutoPasteAndTranslateCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        hotKeyStayInSystemTrayCheckBox = new JCheckBox();
        this.$$$loadButtonText$$$(hotKeyStayInSystemTrayCheckBox, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.hotKey.stayInSystemTray"));
        panel14.add(hotKeyStayInSystemTrayCheckBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel15 = new JPanel();
        panel15.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel10.add(panel15, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel15.setBorder(BorderFactory.createTitledBorder("Proxy"));
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.proxyHost"));
        panel15.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        proxyHostTextField = new JTextField();
        panel15.add(proxyHostTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.proxyPort"));
        panel15.add(label3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        proxyPortTextField = new JTextField();
        panel15.add(proxyPortTextField, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$(label4, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.proxyUser"));
        panel15.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        proxyUserTextField = new JTextField();
        panel15.add(proxyUserTextField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label5 = new JLabel();
        this.$$$loadLabelText$$$(label5, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.proxyPassword"));
        panel15.add(label5, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        proxyPasswordTextField = new JPasswordField();
        panel15.add(proxyPasswordTextField, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel16 = new JPanel();
        panel16.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panel16, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel16.setBorder(BorderFactory.createTitledBorder(ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.googleApiCredentials")));
        final JLabel label6 = new JLabel();
        this.$$$loadLabelText$$$(label6, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.googleApiKey"));
        panel16.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        googleApiKeyTextField = new JTextField();
        panel16.add(googleApiKeyTextField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        helpButton = new JButton();
        this.$$$loadButtonText$$$(helpButton, ResourceBundle.getBundle("org/google/translate/desktop/resources/messages").getString("settingsDialog.help"));
        panel16.add(helpButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label1.setLabelFor(lookAndFeelComboBox);
        label2.setLabelFor(proxyHostTextField);
        label3.setLabelFor(proxyPortTextField);
        label4.setLabelFor(proxyUserTextField);
        label5.setLabelFor(proxyPasswordTextField);
        label6.setLabelFor(googleApiKeyTextField);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(selectedOriginalLanguageRadioButton);
        buttonGroup.add(lastOriginalUsedLanguageRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(lastTranslatedUsedLanguageRadioButton);
        buttonGroup.add(selectedTranslatedLanguageRadioButton);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

    private static class LookAndFeel {

        private String className;

        private LookAndFeel(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }

        @Override
        public String toString() {
            return getSimpleName(className);
        }

        public static String getSimpleName(String className) {
            return className.substring(className.lastIndexOf('.') + 1);
        }
    }
}
