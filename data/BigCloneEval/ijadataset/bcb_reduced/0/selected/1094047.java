package net.sourceforge.vietpad.utilities;

import com.stibocatalog.hunspell.Hunspell;
import java.awt.Color;
import java.io.*;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import net.sourceforge.vietocr.Gui;
import net.sourceforge.vietocr.utilities.Utilities;

public class SpellCheckHelper {

    JTextComponent textComp;

    Highlighter.HighlightPainter myPainter = new WavyLineHighlighter(Color.red);

    String localeId;

    File baseDir;

    static List<DocumentListener> lstList = new ArrayList<DocumentListener>();

    Hunspell.Dictionary spellDict;

    static List<String> userWordList = new ArrayList<String>();

    static long mapLastModified = Long.MIN_VALUE;

    /**
     * Constructor.
     *
     * @param textComp
     * @param localeId
     */
    public SpellCheckHelper(JTextComponent textComp, String localeId) {
        this.textComp = textComp;
        this.localeId = localeId;
        baseDir = Utilities.getBaseDir(SpellCheckHelper.this);
    }

    public void enableSpellCheck() {
        if (localeId == null) {
            return;
        }
        try {
            spellDict = Hunspell.getInstance().getDictionary(new File(baseDir, "dict/" + localeId).getPath());
            loadUserDictionary();
            SpellcheckDocumentListener docListener = new SpellcheckDocumentListener();
            lstList.add(docListener);
            this.textComp.getDocument().addDocumentListener(docListener);
            spellCheck();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), Gui.APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    public void disableSpellCheck() {
        if (lstList.size() > 0) {
            this.textComp.getDocument().removeDocumentListener(lstList.remove(0));
            this.textComp.getHighlighter().removeAllHighlights();
        }
    }

    public void spellCheck() {
        Highlighter hi = textComp.getHighlighter();
        hi.removeAllHighlights();
        List<String> words = parseText(textComp.getText());
        List<String> misspelledWords = spellCheck(words);
        if (misspelledWords.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String word : misspelledWords) {
            sb.append(word).append("|");
        }
        sb.setLength(sb.length() - 1);
        String patternStr = "\\b(" + sb.toString() + ")\\b";
        Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(textComp.getText());
        while (matcher.find()) {
            try {
                hi.addHighlight(matcher.start(), matcher.end(), myPainter);
            } catch (BadLocationException ex) {
            }
        }
    }

    List<String> spellCheck(List<String> words) {
        List<String> misspelled = new ArrayList<String>();
        for (String word : words) {
            if (spellDict.misspelled(word)) {
                if (!userWordList.contains(word.toLowerCase())) {
                    misspelled.add(word);
                }
            }
        }
        return misspelled;
    }

    List<String> parseText(String text) {
        List<String> words = new ArrayList<String>();
        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(text);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            if (!Character.isLetter(text.charAt(start))) {
                continue;
            }
            words.add(text.substring(start, end));
        }
        return words;
    }

    public List<String> suggest(String misspelled) {
        List<String> list = new ArrayList<String>();
        list.add(misspelled);
        if (spellCheck(list).isEmpty()) {
            return null;
        } else {
            try {
                return spellDict.suggest(misspelled);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public void ignoreWord(String word) {
        if (!userWordList.contains(word.toLowerCase())) {
            userWordList.add(word.toLowerCase());
        }
    }

    public void addWord(String word) {
        if (!userWordList.contains(word.toLowerCase())) {
            userWordList.add(word.toLowerCase());
            try {
                File userDict = new File(baseDir, "dict/user.dic");
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(userDict, true), "UTF8"));
                out.write(word);
                out.newLine();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Loads user dictionary only once.
     */
    void loadUserDictionary() {
        try {
            File userDict = new File(baseDir, "dict/user.dic");
            long fileLastModified = userDict.lastModified();
            if (fileLastModified <= mapLastModified) {
                return;
            }
            mapLastModified = fileLastModified;
            userWordList.clear();
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(userDict), "UTF8"));
            String str;
            while ((str = in.readLine()) != null) {
                userWordList.add(str.toLowerCase());
            }
            in.close();
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(null, ioe.getMessage(), Gui.APP_NAME, JOptionPane.ERROR_MESSAGE);
        }
    }

    class SpellcheckDocumentListener implements DocumentListener {

        @Override
        public void removeUpdate(DocumentEvent e) {
            spellCheck();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            spellCheck();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
    }
}
