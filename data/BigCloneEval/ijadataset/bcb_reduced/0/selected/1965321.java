package uk.ac.mmu.manmetassembly.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class SyntaxHighlighter implements KeyListener {

    private JTextPane textArea;

    private int lineEnd = 1;

    public SyntaxHighlighter(JTextPane theTextArea) {
        textArea = theTextArea;
    }

    /** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e) {
    }

    /** Handle the key pressed event from the text field. */
    public void keyPressed(KeyEvent e) {
    }

    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e) {
        applyHighlighting();
    }

    public void applyHighlighting() {
        try {
            String line = textArea.getText().replaceAll("\n", " ");
            int caretPosition = textArea.getCaretPosition();
            Element root = textArea.getDocument().getDefaultRootElement();
            int line2 = root.getElementIndex(caretPosition);
            lineEnd = root.getElement(line2).getStartOffset();
            java.util.List styles = new java.util.ArrayList();
            System.out.println("SUB TEST " + line.substring(lineEnd));
            line = line.substring(lineEnd);
            Pattern pattern;
            Matcher matcher;
            if (line.trim().length() > 0) if (line.trim().charAt(0) != ';') {
                for (int i = 0; i < reservedWords.length; i++) {
                    pattern = Pattern.compile("\\s" + reservedWords[i] + "(?![^;\\s]+)");
                    matcher = pattern.matcher("\n" + line.toUpperCase().split(";")[0] + "\n");
                    while (matcher.find()) {
                        System.out.println("reservedWords find");
                        StyledDocument doc = (StyledDocument) textArea.getDocument();
                        MutableAttributeSet attr = new SimpleAttributeSet();
                        StyleConstants.setForeground(attr, new Color(0, 0, 204));
                        textArea.setCharacterAttributes(attr, false);
                        StyleConstants.setBold(attr, true);
                        doc.setCharacterAttributes(lineEnd + matcher.start(), reservedWords[i].length(), attr, true);
                        System.out.println("RESERVED WORDS :" + (lineEnd + matcher.start()) + " " + reservedWords[i].length());
                    }
                }
                for (int i = 0; i < registers.length; i++) {
                    pattern = Pattern.compile("(\\s|,)" + registers[i] + "(?![^,;\\s]+)");
                    matcher = pattern.matcher("\n" + line.toUpperCase().split(";")[0] + "\n");
                    while (matcher.find()) {
                        System.out.println("registers find");
                        StyledDocument doc = (StyledDocument) textArea.getDocument();
                        MutableAttributeSet attr = new SimpleAttributeSet();
                        StyleConstants.setForeground(attr, new Color(102, 0, 51));
                        StyleConstants.setBold(attr, true);
                        textArea.setCharacterAttributes(attr, false);
                        doc.setCharacterAttributes(lineEnd + matcher.start(), registers[i].length(), attr, true);
                        System.out.println("REGISTERS: " + (lineEnd + matcher.start()) + " " + reservedWords[i].length());
                    }
                }
                for (int i = 0; i < declarations.length; i++) {
                    pattern = Pattern.compile("\\s" + declarations[i] + "(?![^;\\s]+)");
                    matcher = pattern.matcher("\n" + line.toUpperCase().split(";")[0] + "\n");
                    while (matcher.find()) {
                        System.out.println("declarations find");
                        StyledDocument doc = (StyledDocument) textArea.getDocument();
                        MutableAttributeSet attr = new SimpleAttributeSet();
                        StyleConstants.setForeground(attr, new Color(00, 100, 100));
                        StyleConstants.setBold(attr, true);
                        textArea.setCharacterAttributes(attr, false);
                        doc.setCharacterAttributes(matcher.start(), declarations[i].length(), attr, true);
                    }
                }
                pattern = Pattern.compile("^\\s\\w+:");
                matcher = pattern.matcher("\n" + line.toUpperCase().split(";")[0] + "\n");
                while (matcher.find()) {
                    System.out.println("labels find");
                    StyledDocument doc = (StyledDocument) textArea.getDocument();
                    MutableAttributeSet attr = new SimpleAttributeSet();
                    StyleConstants.setForeground(attr, new Color(00, 100, 100));
                    StyleConstants.setBold(attr, true);
                    textArea.setCharacterAttributes(attr, false);
                    doc.setCharacterAttributes(lineEnd + matcher.start() - 1, matcher.end() - (matcher.start()), attr, true);
                }
                pattern = Pattern.compile("(\\s|,)((0B([0-1]+))|(0X([0-9A-F]+))|([0-9]+))(?![^;\\s]+)");
                matcher = pattern.matcher("\n" + line.toUpperCase().split(";")[0] + "\n");
                while (matcher.find()) {
                    System.out.println("(bin/hex/dec numbers) find");
                    StyledDocument doc = (StyledDocument) textArea.getDocument();
                    MutableAttributeSet attr = new SimpleAttributeSet();
                    StyleConstants.setForeground(attr, new Color(240, 51, 0));
                    StyleConstants.setBold(attr, true);
                    textArea.setCharacterAttributes(attr, false);
                    doc.setCharacterAttributes(lineEnd + matcher.start(), matcher.end() - matcher.start(), attr, true);
                }
                pattern = Pattern.compile("(\"[^\"]*\"|'[^']*')");
                matcher = pattern.matcher("\n" + line.toUpperCase().split(";")[0] + "\n");
                while (matcher.find()) System.out.println("strings find");
                StyledDocument doc = (StyledDocument) textArea.getDocument();
                MutableAttributeSet attr = new SimpleAttributeSet();
                StyleConstants.setForeground(attr, new Color(204, 0, 0));
                StyleConstants.setBold(attr, true);
                textArea.setCharacterAttributes(attr, false);
                doc.setCharacterAttributes(lineEnd + matcher.start() - 1, matcher.end() - matcher.start(), attr, true);
            }
            pattern = Pattern.compile("\\Q;\\E");
            matcher = pattern.matcher(line);
            if (matcher.find()) System.out.println("comments find");
            StyledDocument doc = (StyledDocument) textArea.getDocument();
            MutableAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setForeground(attr, new Color(63, 127, 95));
            textArea.setCharacterAttributes(attr, false);
            StyleConstants.setBold(attr, false);
            doc.setCharacterAttributes(lineEnd + matcher.start(), (line.length() - matcher.start()), attr, true);
            System.out.println(lineEnd + matcher.start() + " " + (line.length() - matcher.start()));
            System.out.println("ddd" + textArea.getText(lineEnd + matcher.start(), (line.length() - matcher.start())));
        } catch (Exception ex) {
        }
    }

    protected int lastLine = 0;

    protected final String reservedWords[] = { "AAA", "AAD", "AAM", "AAS", "ADC", "ADD", "AND", "CALL", "CBW", "CLC", "CLD", "CLI", "CMC", "CMP", "CMPSB", "CMPSW", "CWD", "DAA", "DAS", "DEC", "DIV", "HLT", "IDIV", "IMUL", "IN", "INC", "INT", "INTO", "IRET", "JA", "JAE", "JB", "JBE", "JC", "JCXZ", "JE", "JG", "JGE", "JL", "JLE", "JMP", "JNA", "JNAE", "JNB", "JNBE", "JNC", "JNE", "JNG", "JNGE", "JNL", "JNLE", "JNO", "JNP", "JNS", "JNZ", "JO", "JP", "JPE", "JPO", "JS", "JZ", "LAHF", "LDS", "LEA", "LES", "LODSB", "LODSW", "LOOP", "LOOPE", "LOOPNE", "LOOPNZ", "LOOPZ", "MOV", "MOVSB", "MOVSW", "MUL", "NEG", "NOP", "NOT", "OR", "OUT", "POP", "POPA", "POPF", "PUSH", "PUSHA", "PUSHF", "RCL", "RCR", "REP", "REPE", "REPNE", "REPNZ", "REPZ", "RET", "RETF", "ROL", "ROR", "SAHF", "SAL", "SAR", "SBB", "SCASB", "SCASW", "SHL", "SHR", "STC", "STD", "STI", "STOSB", "STOSW", "SUB", "TEST", "XCHG", "XLATB", "XOR" };

    protected final String declarations[] = { "DB", "DW" };

    protected final String registers[] = { "AX", "AH", "AL", "BX", "BH", "BL", "CX", "CH", "CL", "DX", "DH", "DL", "BP", "SP", "BI", "SI" };
}
