package net.dnsalias.pcb.texteditor.gui;

import java.awt.event.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.event.*;
import net.dnsalias.pcb.utilities.HelperClass;

/**
 * Brings up a modal dialog
 */
public class SearchDialog extends JDialog implements ActionListener, ItemListener, CaretListener {

    public static final long serialVersionUID = 19650221L;

    private JFrame owner;

    private Texteditor texteditor;

    private Box box1;

    private JButton findButton, replaceButton, replaceAllButton, closeButton;

    private JLabel tfLabel, replaceLabel;

    private JTextField textField, replaceField;

    private String title, searchString, replaceString;

    private String textToSearchFor;

    private String textToSearchIn;

    private JCheckBox matchCaseCb, holeWordsCb;

    private int index, firstIndex;

    private int continueAnswer = JOptionPane.YES_OPTION;

    private boolean searchChanged = true;

    private boolean matchCase = true;

    private boolean holeWords = true;

    private boolean formerSelectionDeleted = false;

    private Matcher m;

    private Pattern p;

    private JTextArea area;

    private JOptionPane dialogPane;

    private ResourceBundle SearchDialogResources;

    private static final String resourceSrc = "resources/SearchDialogResources";

    public SearchDialog(JFrame owner, boolean modal, String searchString, String replaceString, boolean matchCase, boolean holeWords, JTextArea area, Texteditor texteditor) {
        this(owner, owner.getTitle(), modal, searchString, replaceString, matchCase, holeWords, area, texteditor);
    }

    public SearchDialog(JFrame owner, String title, boolean modal, String searchString, String replaceString, boolean matchCase, boolean holeWords, JTextArea area, Texteditor texteditor) {
        super(owner, title, modal);
        this.title = title;
        this.texteditor = texteditor;
        this.owner = owner;
        this.area = area;
        this.searchString = searchString;
        this.replaceString = replaceString;
        this.matchCase = matchCase;
        this.holeWords = holeWords;
        SearchDialogResources = HelperClass.getResourceBundleFromJar(resourceSrc, "UTF-8");
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        findButton = new JButton(SearchDialogResources.getString("Find"));
        findButton.setMnemonic(SearchDialogResources.getString("Find_Mnemonic").charAt(0));
        findButton.setActionCommand(Enums.SearchOperation.FIND.toString());
        findButton.addActionListener(this);
        replaceButton = new JButton(SearchDialogResources.getString("Replace"));
        replaceButton.setMnemonic(SearchDialogResources.getString("Replace_Mnemonic").charAt(0));
        replaceButton.setActionCommand(Enums.SearchOperation.REPLACE.toString());
        replaceButton.addActionListener(this);
        replaceAllButton = new JButton(SearchDialogResources.getString("ReplaceAll"));
        replaceAllButton.setMnemonic(SearchDialogResources.getString("ReplaceAll_Mnemonic").charAt(0));
        replaceAllButton.setActionCommand(Enums.SearchOperation.REPLACEALL.toString());
        replaceAllButton.addActionListener(this);
        closeButton = new JButton(SearchDialogResources.getString("Close"));
        closeButton.setMnemonic(SearchDialogResources.getString("Close_Mnemonic").charAt(0));
        closeButton.setActionCommand(Enums.SearchOperation.CLOSE.toString());
        closeButton.addActionListener(this);
        JButton[] options = { findButton, replaceButton, replaceAllButton, closeButton };
        dialogPane = new JOptionPane(null, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
        tfLabel = new JLabel(SearchDialogResources.getString("SearchString"));
        textField = new JTextField(20);
        if ((searchString != null) && !(searchString.equals(""))) {
            textField.setText(searchString);
            textField.setCaretPosition(0);
            textField.moveCaretPosition(textField.getText().length());
        }
        textField.setEditable(true);
        textField.addCaretListener(this);
        replaceLabel = new JLabel(SearchDialogResources.getString("ReplaceString"));
        replaceField = new JTextField(20);
        if ((replaceString != null) && !(replaceString.equals(""))) {
            replaceField.setText(replaceString);
        }
        replaceField.setEditable(true);
        replaceField.addCaretListener(this);
        box1 = Box.createVerticalBox();
        box1.add(tfLabel);
        box1.add(textField);
        box1.add(Box.createVerticalStrut(16));
        box1.add(replaceLabel);
        box1.add(replaceField);
        matchCaseCb = new JCheckBox(SearchDialogResources.getString("MatchCase"), matchCase);
        matchCaseCb.setMnemonic(SearchDialogResources.getString("MatchCase_Mnemonic").charAt(0));
        matchCaseCb.addItemListener(this);
        box1.add(Box.createVerticalStrut(5));
        box1.add(matchCaseCb);
        holeWordsCb = new JCheckBox(SearchDialogResources.getString("HoleWords"), holeWords);
        holeWordsCb.setMnemonic(SearchDialogResources.getString("HoleWords_Mnemonic").charAt(0));
        holeWordsCb.addItemListener(this);
        box1.add(Box.createVerticalStrut(5));
        box1.add(holeWordsCb);
        box1.add(Box.createVerticalStrut(5));
        dialogPane.add(box1, 1);
        setContentPane(dialogPane);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        textField.grabFocus();
    }

    public void findAction() {
        if (index >= 0) {
            if (searchChanged == true) {
                index = area.getCaretPosition();
                textToSearchFor = textField.getText();
                textToSearchIn = area.getText();
                if (matchCase == false) {
                    if (holeWords == true) {
                        p = Pattern.compile("\\b" + textToSearchFor + "\\b", Pattern.CASE_INSENSITIVE);
                    } else {
                        p = Pattern.compile(textToSearchFor, Pattern.CASE_INSENSITIVE);
                    }
                } else {
                    if (holeWords == true) {
                        p = Pattern.compile("\\b" + textToSearchFor + "\\b");
                    } else {
                        p = Pattern.compile(textToSearchFor);
                    }
                }
                m = p.matcher(textToSearchIn);
                searchChanged = false;
            }
            int start, end;
            while (m.find() == true) {
                start = m.start();
                if (start < index) {
                    continue;
                }
                end = m.end();
                area.setCaretPosition(start);
                area.moveCaretPosition(end);
                formerSelectionDeleted = true;
                return;
            }
            continueAnswer = JOptionPane.showConfirmDialog(this, SearchDialogResources.getString("StartFromBeginMsg"), title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (continueAnswer == JOptionPane.YES_OPTION) {
                index = 0;
                searchChanged = true;
                area.setCaretPosition(index);
                findAction();
            }
        }
    }

    public void replaceAction() {
        String textReplacement = replaceField.getText();
        String textToReplace = area.getSelectedText();
        if (formerSelectionDeleted == false) {
            if (textToReplace != null && !(textToReplace.equals(""))) {
                area.setCaretPosition(area.getCaretPosition() - (textToReplace.length() - 1));
            }
            formerSelectionDeleted = true;
        }
        textToReplace = area.getSelectedText();
        if ((textToReplace != null) && !(textToReplace.equals(""))) {
            area.replaceSelection(textReplacement);
        }
        searchChanged = true;
        findAction();
    }

    public void replaceAllAction() {
        String replacedText = null;
        String textReplacement = replaceField.getText();
        if (index >= 0) {
            index = area.getCaretPosition();
            firstIndex = index;
            textToSearchFor = textField.getText();
            textToSearchIn = area.getText().substring(index);
            if (matchCase == false) {
                if (holeWords == true) {
                    p = Pattern.compile("\\b" + textToSearchFor + "\\b", Pattern.CASE_INSENSITIVE);
                } else {
                    p = Pattern.compile(textToSearchFor, Pattern.CASE_INSENSITIVE);
                }
            } else {
                if (holeWords == true) {
                    p = Pattern.compile("\\b" + textToSearchFor + "\\b");
                } else {
                    p = Pattern.compile(textToSearchFor);
                }
            }
            m = p.matcher(textToSearchIn);
            replacedText = m.replaceAll(textReplacement);
            if ((replacedText != null) && !(replacedText.equals(""))) {
                area.replaceRange(replacedText, index, area.getText().length());
            }
            continueAnswer = JOptionPane.showConfirmDialog(this, SearchDialogResources.getString("StartFromBeginMsg"), title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (continueAnswer == JOptionPane.YES_OPTION) {
                index = 0;
                textToSearchIn = area.getText().substring(index, firstIndex);
                m.reset();
                m = p.matcher(textToSearchIn);
                replacedText = m.replaceAll(textReplacement);
                if ((replacedText != null) && !(replacedText.equals(""))) {
                    area.replaceRange(replacedText, 0, firstIndex);
                }
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        searchString = textField.getText();
        if ((e.getActionCommand() == Enums.SearchOperation.FIND.toString()) && (searchString != null) && (searchString.length() > 0) && (area.getText().length() > 0)) {
            findAction();
        }
        if ((e.getActionCommand() == Enums.SearchOperation.REPLACE.toString()) && (searchString != null) && (searchString.length() > 0) && (area.getText().length() > 0)) {
            replaceAction();
        }
        if ((e.getActionCommand() == Enums.SearchOperation.REPLACEALL.toString()) && (searchString != null) && (searchString.length() > 0) && (area.getText().length() > 0)) {
            replaceAllAction();
        }
        if (e.getActionCommand() == Enums.SearchOperation.CLOSE.toString()) {
            texteditor.setSearchString(searchString);
            texteditor.setReplaceString(replaceField.getText());
            texteditor.setMatchCase(matchCase);
            texteditor.setHoleWords(holeWords);
            setVisible(false);
        }
    }

    /**
   * catch ItemEvents for JCheckBoxes
   */
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == matchCaseCb) {
            JCheckBox item = (JCheckBox) e.getItem();
            matchCase = item.isSelected();
        }
        if (e.getSource() == holeWordsCb) {
            JCheckBox item = (JCheckBox) e.getItem();
            holeWords = item.isSelected();
        }
    }

    /**
   * catch CaretEvents for JTextFields
   */
    public void caretUpdate(CaretEvent e) {
        if (e.getSource() == textField) {
            searchChanged = true;
        }
        if (e.getSource() == replaceField) {
            searchChanged = true;
        }
    }

    /**
   * @return last entered search string
   */
    public String getSearchString() {
        return searchString;
    }

    /**
   * @return status of matchCase
   */
    public boolean getMatchCase() {
        return matchCase;
    }

    /**
   * @return status of holeWords
   */
    public boolean getHoleWords() {
        return holeWords;
    }
}
