package jasmin.gui;

import jasmin.core.*;
import java.awt.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.JTextPane;
import javax.swing.text.*;

/**
 * This is a syntax highlighter for a jasmin document. It is used by JasDocument.
 * Copyright 2006 by Kai Orend
 * Copyright 2009 by Jakob Kummerow
 */
public class SyntaxHighlighter extends DefaultStyledDocument {

    private static final long serialVersionUID = 1L;

    private JTextPane editor = null;

    private JasDocument document = null;

    private Style normal, command, register, error, label, comment, constant, variable;

    private ArrayList<LineInfo> lineInfo;

    private HashMap<String, LineInfo> labelDefinitions;

    private HashSet<LineInfo> toDoList;

    private HashMap<String, HashSet<LineInfo>> labelUses;

    private HashSet<LineInfo> errorLines;

    public SyntaxHighlighter(JasDocument doc) {
        document = doc;
        editor = doc.getEditor();
        if (editor == null) {
            System.out.println("SyntaxHighlighter(constructor): editor=null");
            return;
        }
        editor.setDocument(this);
        editor.setFont(new java.awt.Font(doc.frame.getProperty("font"), Font.PLAIN, doc.frame.getProperty("font.size", 12)));
        initStyles();
        lineInfo = new ArrayList<LineInfo>();
        lineInfo.add(new LineInfo());
        labelDefinitions = new HashMap<String, LineInfo>();
        labelUses = new HashMap<String, HashSet<LineInfo>>();
        errorLines = new HashSet<LineInfo>();
        toDoList = new HashSet<LineInfo>();
    }

    private void initStyles() {
        normal = editor.addStyle("normal", null);
        command = editor.addStyle("command", null);
        StyleConstants.setForeground(command, new java.awt.Color(0, 0, 144));
        StyleConstants.setBold(command, true);
        register = editor.addStyle("register", null);
        StyleConstants.setForeground(register, new java.awt.Color(0, 144, 40));
        StyleConstants.setBold(register, true);
        error = editor.addStyle("error", null);
        StyleConstants.setForeground(error, Color.red);
        StyleConstants.setUnderline(error, true);
        label = editor.addStyle("label", null);
        StyleConstants.setForeground(label, new java.awt.Color(255, 144, 0));
        StyleConstants.setBold(label, true);
        constant = editor.addStyle("constant", null);
        StyleConstants.setForeground(constant, new java.awt.Color(174, 0, 204));
        StyleConstants.setBold(constant, true);
        variable = editor.addStyle("variable", null);
        StyleConstants.setForeground(variable, new java.awt.Color(0, 128, 128));
        StyleConstants.setBold(variable, true);
        comment = editor.addStyle("comment", null);
        StyleConstants.setForeground(comment, new java.awt.Color(128, 128, 128));
        StyleConstants.setItalic(comment, true);
    }

    /**
	 * @param offset
	 * @param str
	 * @param a
	 * @throws javax.swing.text.BadLocationException
	 */
    @Override
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        if ((str.length() == 1) && str.endsWith("\n")) {
            String lineBefore = getLineByOffset(offset);
            str += getLeadingWhiteSpace(lineBefore, offset - getLineStartOffsetByOffset(offset));
        }
        int currentLineNumber = getLineNumberByOffset(offset);
        toDoList.add(lineInfo.get(currentLineNumber));
        if (str.indexOf('\n') != -1) {
            int index = str.indexOf('\n');
            while (index != -1) {
                LineInfo info = new LineInfo();
                lineInfo.add(currentLineNumber + 1, info);
                toDoList.add(info);
                index = str.indexOf('\n', index + 1);
            }
        }
        super.insertString(offset, str, a);
        parseAllOnToDoList();
    }

    /**
	 * Returns the leading whitespace of a given line, up to the given maximal length
	 * 
	 * @param line
	 * @param maxLength
	 * @return
	 */
    private static String getLeadingWhiteSpace(String line, int maxLength) {
        if (line.length() == 0) {
            return "";
        }
        String result = "";
        char c = line.charAt(0);
        int index = 1;
        while ((index < line.length()) && (index < maxLength) && ((c == ' ') || (c == '\t'))) {
            result += c;
            c = line.charAt(index);
            index++;
        }
        if ((c == ' ') || (c == '\t')) {
            result += c;
        }
        return result;
    }

    /**
	 * @param offset
	 * @param length
	 * @throws javax.swing.text.BadLocationException
	 */
    @Override
    public void remove(int offset, int length) throws BadLocationException {
        int currentLineNumber = getLineNumberByOffset(offset);
        toDoList.add(lineInfo.get(currentLineNumber));
        String str = getText(offset, length);
        if (str.indexOf('\n') != -1) {
            int index = str.indexOf('\n');
            while (index != -1) {
                removeLineInfo(currentLineNumber + 1);
                index = str.indexOf('\n', index + 1);
            }
        }
        super.remove(offset, length);
        parseAllOnToDoList();
    }

    /**
	 * Initiates parsing (and subsequent highlighting) of the given line. Performs a parsing of the previous
	 * line if necessary; adds related lines to the toDoList where appropriate (mainly based on
	 * definition/usage of labels).
	 * When changing this method's behaviour, care must be taken not to create endless loops.
	 * 
	 * @param lineNumber
	 *        The number of the line to parse and highlight
	 */
    private void parseLine(int lineNumber) {
        LineInfo info = lineInfo.get(lineNumber);
        ParseResult prOld = info.pr;
        String lastLabel = getLastLabel(lineNumber);
        int oldLabelType = 0;
        if ((prOld != null) && (prOld.label != null)) {
            oldLabelType = getLabelType(prOld.label);
        }
        int oldLastLabelType = 0;
        if (lastLabel != null) {
            oldLastLabelType = getLabelType(lastLabel);
        }
        Element lineElement = getLineElementByNumber(lineNumber);
        int startOffset = lineElement.getStartOffset();
        String line = getTextToOffset(startOffset, lineElement.getEndOffset());
        ParseResult prNew = document.parser.parse(line, lastLabel);
        if ((lastLabel != null) && (prNew.mnemo != null)) {
            int newLastLabelType = getLabelType(lastLabel);
            if ((newLastLabelType != oldLastLabelType) || (newLastLabelType == 3)) {
                LineInfo definingLine = labelDefinitions.get(lastLabel);
                int defLineNum = getLineNumberByLineInfo(definingLine);
                Element defLineElem = getLineElementByNumber(defLineNum);
                String defLine = getTextToOffset(defLineElem.getStartOffset(), defLineElem.getEndOffset());
                highlight(defLineNum, defLine, defLineElem.getStartOffset());
                toDoList.addAll(labelUses.get(lastLabel));
                toDoList.addAll(errorLines);
            }
        }
        if (prOld != null) {
            if (prOld.label != null) {
                if (!prOld.label.equals(prNew.label)) {
                    if (labelDefinitions.get(prOld.label) == info) {
                        labelDefinitions.remove(prOld.label);
                    }
                    toDoList.addAll(labelUses.get(prOld.label));
                    document.data.unregisterConstant(prOld.label);
                    document.data.unregisterVariable(prOld.label);
                    toDoList.addAll(errorLines);
                }
                int newLabelType = getLabelType(prNew.label);
                if ((newLabelType != oldLabelType) || (newLabelType == 3)) {
                    toDoList.addAll(labelUses.get(prOld.label));
                }
            }
            for (String label : prOld.usedLabels) {
                if (!prNew.usedLabels.contains(label)) {
                    labelUses.get(label).remove(info);
                }
            }
            for (String label : prNew.usedLabels) {
                if (!prOld.usedLabels.contains(label)) {
                    labelUses.get(label).add(info);
                }
            }
        } else {
            for (String label : prNew.usedLabels) {
                labelUses.get(label).add(info);
            }
        }
        while (prNew.label != null) {
            LineInfo existingDefinition = labelDefinitions.get(prNew.label);
            if ((existingDefinition != null) && (existingDefinition != info)) {
                prNew.error = new ParseError(prNew.originalLine, prNew.label, 0, "Label already defined in line " + getLineNumberByLineInfo(existingDefinition));
                break;
            }
            labelDefinitions.put(prNew.label, info);
            if (!labelUses.containsKey(prNew.label)) {
                labelUses.put(prNew.label, new HashSet<LineInfo>());
            }
            int newLabelType = getLabelType(prNew.label);
            if (newLabelType != oldLabelType) {
                toDoList.addAll(errorLines);
            }
            if (newLabelType == 3) {
                toDoList.addAll(labelUses.get(prNew.label));
            }
            break;
        }
        if (prNew.error != null) {
            errorLines.add(info);
        } else {
            errorLines.remove(info);
        }
        info.pr = prNew;
        highlight(lineNumber, line, startOffset);
        document.parsingDone(lineNumber, prNew.mnemo, prNew.error);
        toDoList.remove(info);
    }

    /**
	 * Goes backward in the line list as long as empty lines are encountered until it finds a line consisting
	 * of only a label (which is then returned) or a non-empty, non-label-only line (in which case null is
	 * returned).
	 * 
	 * @param lineNumber
	 *        The line for which to search for the previous label
	 * @return The last label that occurred before the given line (and isn't associated with another line), or
	 *         null if no such label exists
	 */
    private String getLastLabel(int lineNumber) {
        int previousLineNumber = lineNumber - 1;
        String lastLabel = null;
        while (previousLineNumber >= 0) {
            LineInfo prevInfo = lineInfo.get(previousLineNumber);
            if (toDoList.contains(prevInfo)) {
                parseLine(previousLineNumber);
            }
            if (prevInfo.pr.empty) {
                previousLineNumber--;
            } else if (prevInfo.pr.labelOnly) {
                lastLabel = prevInfo.pr.label;
                break;
            } else {
                break;
            }
        }
        return lastLabel;
    }

    /**
	 * Uses the dataspace's information to determine the type of the given label string. For internal use.
	 * 
	 * @param label
	 * @return
	 */
    private int getLabelType(String label) {
        if (label == null) {
            return 0;
        }
        if (document.data.isConstant(label)) {
            return 3;
        }
        if (document.data.isVariable(label)) {
            return 2;
        }
        return 1;
    }

    /**
	 * Removes a lineInfo object from the corresponding list, taking care of deleting all references.
	 * 
	 * @param lineNumber
	 *        The number of the line whose lineInfo is to be removed
	 */
    private void removeLineInfo(int lineNumber) {
        LineInfo info = lineInfo.remove(lineNumber);
        if (info.pr.label != null) {
            labelDefinitions.remove(info.pr.label);
            for (LineInfo usingLine : labelUses.get(info.pr.label)) {
                toDoList.add(usingLine);
            }
        }
        for (String label : info.pr.usedLabels) {
            labelUses.get(label).remove(info);
        }
        if (info.pr.error != null) {
            errorLines.remove(info);
        }
        if (toDoList.contains(info)) {
            toDoList.remove(info);
        }
    }

    /**
	 * Parses lines from the toDoList in arbitrary order, until the toDoList is empty.
	 */
    private void parseAllOnToDoList() {
        while (!toDoList.isEmpty()) {
            LineInfo info = toDoList.iterator().next();
            parseLine(getLineNumberByLineInfo(info));
        }
    }

    public void reparseAll() {
        for (LineInfo info : lineInfo) {
            toDoList.add(info);
        }
        parseAllOnToDoList();
    }

    /**
	 * Executes the line with the given number, parsing it first if necessary.
	 * 
	 * @param lineNumber
	 * @param cached
	 * @return a ParseError describing the result of the execution (or previous parsing)
	 */
    public ParseError executeLine(int lineNumber, boolean cached) {
        if (cached && document.cachedLineDone[lineNumber]) {
            return document.parser.execute(null, null, lineNumber);
        }
        if (toDoList.contains(lineInfo.get(lineNumber))) {
            parseLine(lineNumber);
        }
        String lastLabel = getLastLabel(lineNumber);
        String line = getLineByNumber(lineNumber);
        if (cached) {
            document.cachedLineDone[lineNumber] = true;
        }
        return document.parser.execute(line, lastLabel, (cached ? lineNumber : -1));
    }

    /**
	 * Applies highlighting to the line with the given number. The line must have been parsed before.
	 * 
	 * @param lineNumber
	 *        The number of the line to be highlighted
	 * @param line
	 *        The text of the line
	 * @param startOffset
	 *        The start offset of this line in the document's text
	 */
    private void highlight(int lineNumber, String line, int startOffset) {
        int length = line.length();
        ParseResult pr = lineInfo.get(lineNumber).pr;
        setCharacterAttributes(startOffset, length, normal, true);
        if (pr.mnemo != null) {
            applyStyle(pr.mnemo, command, line, startOffset);
        }
        String[] rlist = DataSpace.getRegisterList();
        for (int i = 0; i < rlist.length; i++) {
            applyStyle(rlist[i], register, line, startOffset);
        }
        if (pr.label != null) {
            highlightLabel(pr.label, line, startOffset);
        }
        for (String labelstring : pr.usedLabels) {
            highlightLabel(labelstring, line, startOffset);
        }
        if (pr.error != null) {
            setCharacterAttributes(startOffset + pr.error.startPos, pr.error.length, error, true);
        }
        if (pr.commentStartPos > -1) {
            int commentStartOffset = startOffset + pr.commentStartPos;
            int commentLength = (startOffset + length) - commentStartOffset;
            setCharacterAttributes(commentStartOffset, commentLength, comment, true);
        }
    }

    /**
	 * Determines the type of the given label and applies the appropriate formatting
	 * 
	 * @param labelstring
	 *        The label string to highlight
	 * @param line
	 *        The text of the line containing the label
	 * @param startOffset
	 *        The start offset of this line in the document's text
	 */
    private void highlightLabel(String labelstring, String line, int startOffset) {
        if (document.data.isConstant(labelstring)) {
            applyStyle(labelstring, constant, line, startOffset);
        } else if (document.data.isVariable(labelstring)) {
            applyStyle(labelstring, variable, line, startOffset);
        } else {
            applyStyle(labelstring, label, line, startOffset);
        }
    }

    /**
	 * Applies the given style to all occurrences of the given keyword in the given line, if the keyword is
	 * surrounded by any of the delimiters ' [ ] + - * : ; . , (tab) (space)
	 * 
	 * @param keyword
	 *        The keyword to apply the style to.
	 * @param style
	 *        The style to apply.
	 * @param containingLine
	 *        The text of the line containing the keyword's occurrence(s)
	 * @param lineStartOffset
	 *        The start offset of the line in the document's text
	 */
    private void applyStyle(String keyword, Style style, String containingLine, int lineStartOffset) {
        String quoted = Pattern.quote(keyword);
        Pattern p = Pattern.compile("(^|" + Parser.DELIM + ")(" + quoted + ")(" + Parser.DELIM + "|$)");
        int matchstart = 0;
        Matcher m = p.matcher(containingLine.toUpperCase());
        while (m.find(matchstart)) {
            setCharacterAttributes(lineStartOffset + m.start(2), keyword.length(), style, true);
            matchstart = m.end(2);
        }
    }

    /**
	 * Returns the document's text from startOffset to endOffset
	 */
    public String getTextToOffset(int startOffset, int endOffset) {
        try {
            return super.getText(startOffset, endOffset - startOffset);
        } catch (BadLocationException e) {
        }
        return "";
    }

    public String getText() {
        return getTextToOffset(0, getLength());
    }

    public int getNumberOfLines() {
        return getParagraphElement(0).getParentElement().getElementCount();
    }

    public Element getLineElementByNumber(int lineNumber) {
        if (lineNumber < getNumberOfLines()) {
            return getParagraphElement(0).getParentElement().getElement(lineNumber);
        }
        return null;
    }

    public String getLineByNumber(int lineNumber) {
        Element lineElement = getLineElementByNumber(lineNumber);
        if (lineElement != null) {
            return getTextToOffset(lineElement.getStartOffset(), lineElement.getEndOffset());
        }
        return "";
    }

    public String getLineByOffset(int offset) {
        Element lineElement = getParagraphElement(offset);
        return getTextToOffset(lineElement.getStartOffset(), lineElement.getEndOffset());
    }

    public int getLineNumberByOffset(int offset) {
        return getParagraphElement(0).getParentElement().getElementIndex(offset);
    }

    private int getLineNumberByLineInfo(LineInfo info) {
        if (info != null) {
            for (int i = 0; i < lineInfo.size(); i++) {
                if (lineInfo.get(i) == info) {
                    return i;
                }
            }
        }
        System.out.println("Error in SyntaxHighlighter.getLineNumberByLineInfo: no matching line found!");
        return -1;
    }

    public int getLineStartOffsetByOffset(int offset) {
        return getParagraphElement(offset).getStartOffset();
    }

    public ParseError getErrorByLineNumber(int lineNumber) {
        if ((lineNumber >= lineInfo.size()) || (lineInfo.get(lineNumber).pr == null)) {
            return null;
        }
        return lineInfo.get(lineNumber).pr.error;
    }

    public String getMnemoByLineNumber(int lineNumber) {
        if ((lineNumber >= lineInfo.size()) || (lineInfo.get(lineNumber).pr == null)) {
            return null;
        }
        return lineInfo.get(lineNumber).pr.mnemo;
    }

    public int getLabelDefinitionLine(String label) {
        LineInfo info = labelDefinitions.get(label);
        if (info == null) {
            return -1;
        }
        return getLineNumberByLineInfo(info);
    }
}
