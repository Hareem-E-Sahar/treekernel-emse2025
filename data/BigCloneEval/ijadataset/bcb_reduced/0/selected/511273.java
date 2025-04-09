package net.sf.vietpad;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import net.sf.vietpad.inputmethod.*;

/**
 *@author     Quan Nguyen
 *@author     Gero Herrmann
 *@created    July 23, 2003
 *@version    1.0.9, 20 April 07
 */
public class VietPadWithFormat extends VietPadWithPrinting {

    private JCheckBoxMenuItem smartMark, vietMode, wordWrap;

    private boolean wordWrapOn, shortHandOn;

    private ShortHandDialog shortHandDlg;

    private ChangeCaseDialog changeCaseDlg;

    private Properties shortHandMap = new Properties();

    File shortHandFile = new File(supportDir, "shorthand.properties");

    /**
     *  Creates a new instance of VietPadWithFormat
     */
    public VietPadWithFormat() {
        super();
        wordWrapOn = prefs.getBoolean("wordWrap", true);
        shortHandOn = prefs.getBoolean("shortHand", false);
        m_editor.setLineWrap(wordWrapOn);
        try {
            if (shortHandFile.exists()) {
                shortHandMap.load(new FileInputStream(shortHandFile));
            } else {
                shortHandMap.setProperty("vn", "Việt Nam");
                shortHandMap.setProperty("hn", "Hà Nội");
                shortHandMap.setProperty("sg", "Sài Gòn");
                shortHandMap.setProperty("qh", "quê hương");
                shortHandMap.setProperty("sv", "sinh viên");
                shortHandMap.setProperty("cđ", "cộng đồng");
                shortHandMap.setProperty("nv", "người Việt");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        if (shortHandOn) {
            VietKeyListener.setMacroMap(shortHandMap);
        }
        menuBar.add(createFormatMenu(), menuBar.getMenuCount() - 1);
        menuBar.add(createKeyboardMenu(), menuBar.getMenuCount() - 1);
    }

    /**
     *  Creates the Format menu
     *
     *@return    the Menu Object
     */
    private JMenu createFormatMenu() {
        JMenu mFormat = new JMenu(myResources.getString("Format"));
        mFormat.setMnemonic('o');
        wordWrap = new JCheckBoxMenuItem(myResources.getString("Word_Wrap"), wordWrapOn);
        wordWrap.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                m_editor.setLineWrap(wordWrap.isSelected());
            }
        });
        mFormat.add(wordWrap);
        JMenuItem item = new JMenuItem(myResources.getString("Font") + "...");
        item.setMnemonic('o');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, MENU_MASK));
        ActionListener lst = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                FontDialog dlg = new FontDialog(VietPadWithFormat.this);
                dlg.setAttributes(m_font);
                dlg.setVisible(true);
                if (dlg.succeeded()) {
                    m_editor.setFont(m_font = dlg.getFont());
                    m_editor.validate();
                }
            }
        };
        item.addActionListener(lst);
        mFormat.add(item);
        mFormat.addSeparator();
        Action changeCaseAction = new AbstractAction(myResources.getString("Change_Case") + "...") {

            public void actionPerformed(ActionEvent e) {
                if (changeCaseDlg == null) {
                    changeCaseDlg = new ChangeCaseDialog(VietPadWithFormat.this, false);
                    changeCaseDlg.setSelectedCase(prefs.get("selectedCase", "Upper Case"));
                    changeCaseDlg.setLocation(prefs.getInt("changeCaseX", changeCaseDlg.getX()), prefs.getInt("changeCaseY", changeCaseDlg.getY()));
                }
                if (m_editor.getSelectedText() == null) {
                    m_editor.selectAll();
                }
                changeCaseDlg.setVisible(true);
            }
        };
        item = mFormat.add(changeCaseAction);
        item.setMnemonic('c');
        Action removeLineBreaksAction = new AbstractAction(myResources.getString("Remove_Line_Breaks")) {

            public void actionPerformed(ActionEvent e) {
                if (m_editor.getSelectedText() == null) {
                    m_editor.selectAll();
                    if (m_editor.getSelectedText() == null) {
                        return;
                    }
                }
                String result = m_editor.getSelectedText().replaceAll("(?<=\n|^)[\t ]+|[\t ]+(?=$|\n)", "").replaceAll("(?<=.)\n(?=.)", " ");
                undoSupport.beginUpdate();
                int start = m_editor.getSelectionStart();
                m_editor.replaceSelection(result);
                setSelection(start, start + result.length());
                undoSupport.endUpdate();
            }
        };
        item = mFormat.add(removeLineBreaksAction);
        item.setMnemonic('r');
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, MENU_MASK));
        return mFormat;
    }

    /**
     *  Changes case
     *
     *@param  typeOfCase  The type that the case should be changed to
     */
    protected void changeCase(String typeOfCase) {
        if (m_editor.getSelectedText() == null) {
            m_editor.selectAll();
            if (m_editor.getSelectedText() == null) {
                return;
            }
        }
        String result = m_editor.getSelectedText();
        if (typeOfCase.equals("UPPERCASE")) {
            result = result.toUpperCase();
        } else if (typeOfCase.equals("lowercase")) {
            result = result.toLowerCase();
        } else if (typeOfCase.equals("Title_Case")) {
            StringBuffer strB = new StringBuffer(result.toLowerCase());
            Pattern pattern = Pattern.compile("(?<!\\p{InCombiningDiacriticalMarks}|\\p{L})\\p{L}");
            Matcher matcher = pattern.matcher(result);
            while (matcher.find()) {
                int index = matcher.start();
                strB.setCharAt(index, Character.toTitleCase(strB.charAt(index)));
            }
            result = strB.toString();
        } else if (typeOfCase.equals("Sentence_case")) {
            StringBuffer strB = new StringBuffer(result.toUpperCase().equals(result) ? result.toLowerCase() : result);
            Matcher matcher = Pattern.compile("\\p{L}(\\p{L}+)").matcher(result);
            while (matcher.find()) {
                if (!(matcher.group(0).toUpperCase().equals(matcher.group(0)) || matcher.group(1).toLowerCase().equals(matcher.group(1)))) {
                    for (int i = matcher.start(); i < matcher.end(); i++) {
                        strB.setCharAt(i, Character.toLowerCase(strB.charAt(i)));
                    }
                }
            }
            final String QUOTE = "\"'`,<>«»‘-›";
            matcher = Pattern.compile("(?:[.?!‼-⁉][])}" + QUOTE + "]*|^|\n|:\\s+[" + QUOTE + "])[-=_*‐-―\\s]*[" + QUOTE + "\\[({]*\\p{L}").matcher(result);
            while (matcher.find()) {
                int i = matcher.end() - 1;
                strB.setCharAt(i, Character.toUpperCase(strB.charAt(i)));
            }
            result = strB.toString();
        }
        undoSupport.beginUpdate();
        int start = m_editor.getSelectionStart();
        m_editor.replaceSelection(result);
        setSelection(start, start + result.length());
        undoSupport.endUpdate();
    }

    /**
     *  Creates the Keyboard menu
     *
     *@return    the Menu Object
     */
    private JMenu createKeyboardMenu() {
        JMenu mKeyboard = new JMenu(myResources.getString("Keyboard"));
        mKeyboard.setMnemonic('k');
        vietMode = new JCheckBoxMenuItem(MAC_OS_X ? myResources.getString("Viet_Mode") + "     ⌥Shift" : myResources.getString("Viet_Mode"), vietModeOn);
        vietMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, KeyEvent.ALT_MASK, true));
        vietMode.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                if (vietMode.isSelected()) {
                    vietModeOn = true;
                    vietModeLabel.setText("V");
                } else {
                    vietModeOn = false;
                    vietModeLabel.setText("E");
                }
                VietKeyListener.setVietModeEnabled(vietModeOn);
                m_toolBar.repaint(0, 2, 2, 8, 32);
            }
        });
        mKeyboard.add(vietMode);
        mKeyboard.addSeparator();
        smartMark = new JCheckBoxMenuItem(myResources.getString("SmartMark"), smartMarkOn);
        smartMark.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                smartMarkOn = smartMark.isSelected();
                VietKeyListener.setSmartMark(smartMarkOn);
            }
        });
        mKeyboard.add(smartMark);
        m_toolBar.add(Box.createHorizontalStrut(15));
        m_toolBar.add(vietModeLabel);
        vietModeLabel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                vietMode.doClick();
            }
        });
        m_toolBar.add(Box.createHorizontalGlue());
        ActionListener imlst = new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                selectedInputMethod = ae.getActionCommand();
                VietKeyListener.setInputMethod(InputMethods.valueOf(selectedInputMethod));
            }
        };
        JMenu subMenu = new JMenu(myResources.getString("Input_Method"));
        ButtonGroup groupInputMethod = new ButtonGroup();
        String supportedInputMethods[] = InputMethods.getNames();
        for (int i = 0; i < supportedInputMethods.length; i++) {
            String inputMethod = supportedInputMethods[i];
            JRadioButtonMenuItem radioItem = new JRadioButtonMenuItem(inputMethod, selectedInputMethod.equals(inputMethod));
            radioItem.addActionListener(imlst);
            subMenu.add(radioItem);
            groupInputMethod.add(radioItem);
        }
        mKeyboard.add(subMenu);
        mKeyboard.addSeparator();
        Action shortHandAction = new AbstractAction(myResources.getString("Shorthand") + "...") {

            public void actionPerformed(ActionEvent e) {
                if (shortHandDlg == null) {
                    shortHandDlg = new ShortHandDialog(VietPadWithFormat.this, true);
                    shortHandDlg.setLocation(prefs.getInt("shortHandX", shortHandDlg.getX()), prefs.getInt("shortHandY", shortHandDlg.getY()));
                }
                shortHandDlg.setMacroMap(shortHandMap, shortHandOn);
                shortHandDlg.setVisible(true);
            }
        };
        JMenuItem item = mKeyboard.add(shortHandAction);
        item.setMnemonic('s');
        return mKeyboard;
    }

    /**
     *  Gets the shorthand map
     *
     *@param  map            shorthand map
     *@param  shortHandMode  shorthand on
     */
    public void setShortHandMap(Properties map, boolean shortHandMode) {
        shortHandMap = map;
        shortHandOn = shortHandMode;
        if (shortHandOn) {
            VietKeyListener.setMacroMap(shortHandMap);
        } else {
            VietKeyListener.setMacroMap(null);
        }
    }

    /**
     *  Updates UI component if changes in LAF
     *
     *@param  laf  The look and feel class name
     */
    protected void updateLaF(String laf) {
        super.updateLaF(laf);
        if (changeCaseDlg != null) {
            SwingUtilities.updateComponentTreeUI(changeCaseDlg);
            changeCaseDlg.pack();
        }
    }

    /**
     *  Quits the application
     */
    protected void quit() {
        prefs.putBoolean("wordWrap", wordWrap.isSelected());
        if (changeCaseDlg != null) {
            prefs.put("selectedCase", changeCaseDlg.getSelectedCase());
            prefs.putInt("changeCaseX", changeCaseDlg.getX());
            prefs.putInt("changeCaseY", changeCaseDlg.getY());
        }
        if (shortHandDlg != null) {
            prefs.putBoolean("shortHand", shortHandOn);
            prefs.putInt("shortHandX", shortHandDlg.getX());
            prefs.putInt("shortHandY", shortHandDlg.getY());
            shortHandFile.getParentFile().mkdirs();
            try {
                shortHandMap.store(new FileOutputStream(shortHandFile), "VietPad Shorthand File");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.quit();
    }
}
