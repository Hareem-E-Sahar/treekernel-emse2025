package pl.olek.textmash.workspace;

import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledEditorKit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import pl.olek.clojure.Configuration;
import pl.olek.clojure.Process;
import pl.olek.clojure.Remote;
import pl.olek.clojure.RemoteManager;
import pl.olek.clojure.Stream;
import pl.olek.textmash.Support;
import pl.olek.textmash.TextMash;
import pl.olek.textmash.matching.BracketMatchingResult;
import pl.olek.textmash.matching.Dictionary;
import pl.olek.textmash.matching.OccurrencesMatcher;
import pl.olek.textmash.menu.MenuBuilder;

/**
 * 
 * @author anaszko
 *
 */
public class TextEditor extends JTextPane {

    private static final long serialVersionUID = -4397739980167584177L;

    LineNumbers lines;

    BracketMatchingResult result;

    HashMap<Object, Action> actions = new HashMap<Object, Action>();

    public UndoAction undoAction = new UndoAction();

    public RedoAction redoAction = new RedoAction();

    Workspace workspace;

    public CompoundUndoManager undo;

    Action copy;

    Action cut;

    Caret defaultCaret;

    Caret verticalCaret;

    Remote clojure;

    String terminalName;

    public String getTerminal() {
        return terminalName;
    }

    public void closeClojurePipe() {
        try {
            if (clojure != null) {
                clojure.close();
                clojure = null;
            }
        } catch (Exception e) {
        }
    }

    public void setTerminal(String name) {
        closeClojurePipe();
        terminalName = name;
    }

    public boolean toggleVerticalSelection() {
        boolean result = getCaret() == defaultCaret;
        setVerticalSelection(result);
        return result;
    }

    public void setVerticalSelection(boolean state) {
        int pos = getCaretPosition();
        getHighlighter().removeAllHighlights();
        if (state) {
            setCaret(verticalCaret);
        } else {
            setCaret(defaultCaret);
        }
        setCaretPosition(pos);
    }

    public TextEditor(Workspace workspace) {
        this.workspace = workspace;
        undo = new CompoundUndoManager(workspace);
        verticalCaret = new VerticalSelection();
        verticalCaret.setBlinkRate(getCaret().getBlinkRate());
        defaultCaret = new BoldCaret();
        defaultCaret.setBlinkRate(getCaret().getBlinkRate());
        Action[] actionsArray = getActions();
        for (int i = 0; i < actionsArray.length; i++) {
            Action a = actionsArray[i];
            actions.put(a.getValue(Action.NAME), a);
        }
        actions.put("undo", undoAction);
        actions.put("redo", redoAction);
        actions.put("deleteLine", new DeleteLineAction());
        actions.put("deleteRest", new DeleteRestAction());
        actions.put("delete", new DeleteAction());
        actions.put("cut", cut = new CutAction());
        actions.put("copy", copy = new CopyAction());
        actions.put("paste", new GenericAction("Paste", KeyEvent.VK_V, MenuBuilder.NONE, DefaultEditorKit.pasteAction));
        actions.put("selectAll", new GenericAction("Select All", KeyEvent.VK_A, MenuBuilder.NONE, DefaultEditorKit.selectAllAction));
        actions.put("selectLine", new GenericAction("Select Line", KeyEvent.VK_A, MenuBuilder.SHIFT, DefaultEditorKit.selectLineAction));
        actions.put("shiftRight", new ShiftRightAction());
        actions.put("shiftLeft", new ShiftLeftAction());
        actions.put("upperCase", new UppercaseAction());
        actions.put("lowerCase", new LowercaseAction());
        actions.put("capitalizeWords", new CapitalizeWordsAction());
        actions.put("capitalizeSentences", new CapitalizeSentencesAction());
        actions.put("toggleComment", new ToggleCommentAction());
        actions.put("suggestWord", new SuggestWordAction());
        actions.put("defaultOpen", new OpenInDefaultAction());
        actions.put("deleteEmptyLines", new DeleteEmptyLinesAction());
        actions.put("spawnTerminal", new SpawnClojureTerminalAction());
        actions.put("executeStatement", new ExecuteStatementAction());
        actions.put("executeFile", new ExecuteFileAction());
        actions.put("configureClojure", new ConfigureClojureAction());
        getInputMap().put(MenuBuilder.getKeyStroke(KeyEvent.VK_TAB), getAction("shiftRight"));
        getInputMap().put(MenuBuilder.getStdKeyStroke(KeyEvent.VK_C, MenuBuilder.NONE), copy);
        getInputMap().put(MenuBuilder.getStdKeyStroke(KeyEvent.VK_X, MenuBuilder.NONE), cut);
        getInputMap().put(MenuBuilder.getStdKeyStroke(KeyEvent.VK_M, MenuBuilder.NONE), new AbstractAction() {

            /**
					 * 
					 */
            private static final long serialVersionUID = -802399126186289200L;

            @Override
            public void actionPerformed(ActionEvent e) {
                TextEditor.this.workspace.setState(Frame.ICONIFIED);
            }
        });
        getInputMap().put(MenuBuilder.getKeyStroke(KeyEvent.VK_SPACE, MenuBuilder.CTRL), getAction("suggestWord"));
        getInputMap().put(MenuBuilder.getKeyStroke(KeyEvent.VK_ESCAPE, MenuBuilder.ALT), getAction("suggestWord"));
        getInputMap().put(MenuBuilder.getStdKeyStroke(KeyEvent.VK_T, MenuBuilder.NONE), getAction("spawnTerminal"));
        getInputMap().put(MenuBuilder.getStdKeyStroke(KeyEvent.VK_E, MenuBuilder.NONE), getAction("executeStatement"));
        getInputMap().put(MenuBuilder.getStdKeyStroke(KeyEvent.VK_E, MenuBuilder.SHIFT), getAction("executeFile"));
    }

    class SuggestWordAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -2276100296939789468L;

        public SuggestWordAction() {
            super("Suggest Word");
            putValue(ACCELERATOR_KEY, MenuBuilder.getKeyStroke(KeyEvent.VK_SPACE, MenuBuilder.CTRL));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                JPopupMenu menu = null;
                final String word = getLastWord();
                for (final String itemName : Dictionary.getInstance().propose(word, 9)) {
                    if (menu == null) {
                        menu = new JPopupMenu();
                    }
                    final JMenuItem it = new JMenuItem(itemName);
                    it.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                getDocument().insertString(getCaretPosition(), itemName.substring(word.length()), null);
                                ((JPopupMenu) it.getParent()).setVisible(false);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                    menu.add(it);
                }
                if (menu != null) {
                    Rectangle pos = modelToView(getCaretPosition());
                    menu.show(TextEditor.this, (int) pos.getX(), (int) pos.getY());
                } else {
                    getAction(StyledEditorKit.beepAction).actionPerformed(e);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public String getLastWord() {
        try {
            int c = getCaretPosition();
            int from = Math.max(0, c - 40);
            String txt = getText(from, c - from);
            int i = txt.length();
            while (--i >= 0) {
                char d = txt.charAt(i);
                if (!Character.isLetterOrDigit(d) && d != '_' && d != '-') {
                    break;
                }
            }
            return txt.substring(i + 1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setLineEnding(String lineEding) {
        ((ConcurrentDocument) getDocument()).setLineEnding(lineEding);
    }

    public String getTextForSearching(boolean caseinsensitive) {
        String editing = ((ConcurrentDocument) getDocument()).getLineEnding();
        setLineEnding("\r\n");
        String text = getText().replaceAll("\n", "");
        if (caseinsensitive) {
            text = text.toLowerCase();
        }
        setLineEnding(editing);
        return text;
    }

    public Action getAction(String name) {
        return actions.get(name);
    }

    public void discardUndoRedo() {
        undo.discardAllEdits();
        undoAction.updateUndoState();
        redoAction.updateRedoState();
    }

    public void setBrackets(BracketMatchingResult result) {
        this.result = result;
    }

    public void setLineNumbers(LineNumbers lines) {
        this.lines = lines;
    }

    public void paint(Graphics g) {
        super.paint(g);
        if (lines != null) {
            lines.repaint();
        }
        if (result != null) {
            result.paint(g, this);
        }
        OccurrencesMatcher.getInstance().paint(g, this);
    }

    public class UndoAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 6140621825301217056L;

        boolean state;

        public UndoAction() {
            super("Undo");
            putValue(ACCELERATOR_KEY, MenuBuilder.getStdKeyStroke(KeyEvent.VK_Z, MenuBuilder.NONE));
        }

        public void toggleState(boolean requestState) {
            if (!requestState) {
                state = isEnabled();
                setEnabled(false);
            } else {
                setEnabled(state);
            }
        }

        public void actionPerformed(ActionEvent e) {
            try {
                OccurrencesMatcher.getInstance().dontDraw = true;
                undo.undo();
                updateUndoState();
                redoAction.updateRedoState();
            } catch (CannotUndoException ex) {
            }
        }

        public void updateUndoState() {
            setEnabled(undo.canUndo());
        }
    }

    public class RedoAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1299024672322927764L;

        boolean state;

        public RedoAction() {
            super("Redo");
            putValue(ACCELERATOR_KEY, Support.getInstance().getRedoKey());
        }

        public void toggleState(boolean requestState) {
            if (!requestState) {
                state = isEnabled();
                setEnabled(false);
            } else {
                setEnabled(state);
            }
        }

        public void actionPerformed(ActionEvent e) {
            try {
                OccurrencesMatcher.getInstance().dontDraw = true;
                undo.redo();
                updateRedoState();
                undoAction.updateUndoState();
            } catch (CannotRedoException ex) {
            }
        }

        public void updateRedoState() {
            setEnabled(undo.canRedo());
        }
    }

    class DeleteLineAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -7062954737555732356L;

        public DeleteLineAction() {
            super("Current Line");
            putValue(ACCELERATOR_KEY, MenuBuilder.getStdKeyStroke(KeyEvent.VK_D, MenuBuilder.NONE));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                undo.setManualEdit(true);
                getAction(StyledEditorKit.selectLineAction).actionPerformed(e);
                Document doc = getDocument();
                doc.remove(getSelectionStart(), getSelectionEnd() - getSelectionStart());
                int where = getCaretPosition();
                int current = doc.getLength();
                getAction(StyledEditorKit.deletePrevCharAction).actionPerformed(e);
                if (doc.getLength() == current && current > 0) {
                    getAction(StyledEditorKit.deleteNextCharAction).actionPerformed(e);
                }
                if (where > doc.getLength()) {
                    getAction(StyledEditorKit.beginLineAction).actionPerformed(e);
                } else {
                    setCaretPosition(where);
                }
                undo.setManualEdit(false);
                undoAction.updateUndoState();
                redoAction.updateRedoState();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class GenericAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1784029395035666124L;

        String what;

        public GenericAction(String name, int key, int mod, String what) {
            super(name);
            putValue(ACCELERATOR_KEY, MenuBuilder.getStdKeyStroke(key, mod));
            this.what = what;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            getAction(what).actionPerformed(e);
        }
    }

    class DeleteRestAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -3234806572842230664L;

        public DeleteRestAction() {
            super("Rest after Caret");
            putValue(ACCELERATOR_KEY, MenuBuilder.getStdKeyStroke(KeyEvent.VK_D, MenuBuilder.SHIFT));
        }

        public void actionPerformed(ActionEvent e) {
            try {
                getAction(StyledEditorKit.selectionEndLineAction).actionPerformed(e);
                Document doc = getDocument();
                doc.remove(getSelectionStart(), getSelectionEnd() - getSelectionStart());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class DeleteEmptyLinesAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -8436749266370636041L;

        public DeleteEmptyLinesAction() {
            super("Empty Lines");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                undo.setManualEdit(true);
                Element rootElement = getDocument().getDefaultRootElement();
                int start = rootElement.getElementIndex(getSelectionStart());
                int end = rootElement.getElementIndex(getSelectionEnd());
                while (start <= end) {
                    Element curElement = rootElement.getElement(start);
                    if (curElement == null) {
                        continue;
                    }
                    int s = curElement.getStartOffset();
                    int en = curElement.getEndOffset();
                    String txt = getText(s, en - s);
                    if (txt.trim().length() == 0) {
                        getDocument().remove(s, en - s);
                        end = rootElement.getElementIndex(getSelectionEnd());
                        continue;
                    }
                    ++start;
                }
                undo.setManualEdit(false);
                undoAction.updateUndoState();
                redoAction.updateRedoState();
            } catch (BadLocationException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    class ShiftRightAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 9134655582214579284L;

        public ShiftRightAction() {
            super("Shift Right");
            putValue(ACCELERATOR_KEY, MenuBuilder.getStdKeyStroke(KeyEvent.VK_CLOSE_BRACKET, MenuBuilder.NONE));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (getSelectionStart() == getSelectionEnd()) {
                getAction(DefaultEditorKit.insertTabAction).actionPerformed(e);
            } else {
                try {
                    undo.setManualEdit(true);
                    Element rootElement = getDocument().getDefaultRootElement();
                    int start = rootElement.getElementIndex(getSelectionStart());
                    int end = rootElement.getElementIndex(getSelectionEnd());
                    int selBegin = start;
                    int selEnd = start;
                    while (start <= end) {
                        getDocument().insertString(rootElement.getElement(start).getStartOffset(), "\t", null);
                        selEnd = start;
                        ++start;
                    }
                    select(rootElement.getElement(selBegin).getStartOffset(), rootElement.getElement(selEnd).getEndOffset() - 1);
                    undo.setManualEdit(false);
                    undoAction.updateUndoState();
                    redoAction.updateRedoState();
                } catch (BadLocationException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    class ToggleCommentAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -7363452920767873454L;

        public ToggleCommentAction() {
            super("Toggle Comment");
            putValue(ACCELERATOR_KEY, MenuBuilder.getStdKeyStroke(KeyEvent.VK_C, MenuBuilder.SHIFT));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                boolean multiple = (getSelectionStart() != getSelectionEnd());
                String comment = workspace.config.get(Settings.COMMENT);
                undo.setManualEdit(true);
                Element rootElement = getDocument().getDefaultRootElement();
                int start = rootElement.getElementIndex(getSelectionStart());
                int end = rootElement.getElementIndex(getSelectionEnd());
                boolean hasMode = false;
                boolean doComment = false;
                Element endEle = null;
                while (start <= end) {
                    rootElement = getDocument().getDefaultRootElement();
                    Element ele = rootElement.getElement(start);
                    if (!hasMode) {
                        hasMode = true;
                        int eleStart = ele.getStartOffset();
                        int eleEnd = ele.getEndOffset();
                        int eleLen = eleEnd - eleStart;
                        if (eleLen < comment.length()) {
                            doComment = true;
                        } else {
                            String eleCmt = getDocument().getText(eleStart, eleEnd - eleStart);
                            doComment = (!eleCmt.trim().startsWith(comment));
                        }
                    }
                    if (doComment) {
                        modelToView(ele.getStartOffset());
                        getDocument().insertString(ele.getStartOffset(), comment, null);
                    } else {
                        int eleStart = ele.getStartOffset();
                        int eleEnd = ele.getEndOffset();
                        int eleLen = eleEnd - eleStart;
                        if (eleLen >= comment.length()) {
                            String eleCmt = getDocument().getText(eleStart, eleEnd - eleStart);
                            int a = eleCmt.indexOf(comment);
                            if (a != -1) {
                                getDocument().remove(eleStart + a, comment.length());
                            }
                        }
                    }
                    endEle = ele;
                    ++start;
                }
                undo.setManualEdit(false);
                undoAction.updateUndoState();
                redoAction.updateRedoState();
                if (multiple) {
                } else if (endEle != null) {
                    setCaretPosition(endEle.getStartOffset());
                }
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    class ShiftLeftAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -6882201555959073767L;

        public ShiftLeftAction() {
            super("Shift Left");
            putValue(ACCELERATOR_KEY, MenuBuilder.getStdKeyStroke(KeyEvent.VK_OPEN_BRACKET, MenuBuilder.NONE));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Element rootElement = getDocument().getDefaultRootElement();
                int start = rootElement.getElementIndex(getSelectionStart());
                int end = rootElement.getElementIndex(getSelectionEnd());
                undo.setManualEdit(true);
                while (start <= end) {
                    int offset = rootElement.getElement(start).getStartOffset();
                    String txt = getText(offset, 1);
                    if (txt.length() > 0 && Character.isWhitespace(txt.charAt(0))) {
                        getDocument().remove(offset, 1);
                    }
                    ++start;
                }
                undo.setManualEdit(false);
                undoAction.updateUndoState();
                redoAction.updateRedoState();
            } catch (BadLocationException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    class CutAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1090564600650147085L;

        public CutAction() {
            super("Cut");
            putValue(ACCELERATOR_KEY, MenuBuilder.getStdKeyStroke(KeyEvent.VK_X, MenuBuilder.NONE));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (getCaret() instanceof VerticalSelection) {
                undo.setManualEdit(true);
                VerticalSelection.cut(TextEditor.this);
                undo.setManualEdit(false);
                undoAction.updateUndoState();
                redoAction.updateRedoState();
            } else {
                getAction(DefaultEditorKit.cutAction).actionPerformed(e);
            }
        }
    }

    class CopyAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -5713931470181571465L;

        public CopyAction() {
            super("Copy");
            putValue(ACCELERATOR_KEY, MenuBuilder.getStdKeyStroke(KeyEvent.VK_C, MenuBuilder.NONE));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (getCaret() instanceof VerticalSelection) {
                VerticalSelection.copy(TextEditor.this);
            } else {
                getAction(DefaultEditorKit.copyAction).actionPerformed(e);
            }
        }
    }

    class LowercaseAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -5567143906412111991L;

        public LowercaseAction() {
            super("Make Lower Case");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int from = getSelectionStart();
                int to = getSelectionEnd();
                if (from == to) {
                    String newText = getText().toLowerCase();
                    setText(newText);
                    setCaretPosition(from);
                } else {
                    String newText = getSelectedText().toLowerCase();
                    getDocument().remove(from, to - from);
                    getDocument().insertString(from, newText, null);
                    setSelectionStart(from);
                    setSelectionEnd(to);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class UppercaseAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 2122033799599262829L;

        public UppercaseAction() {
            super("Make Upper Case");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int from = getSelectionStart();
                int to = getSelectionEnd();
                if (from == to) {
                    String newText = getText().toUpperCase();
                    setText(newText);
                    setCaretPosition(from);
                } else {
                    String newText = getSelectedText().toUpperCase();
                    getDocument().remove(from, to - from);
                    getDocument().insertString(from, newText, null);
                    setSelectionStart(from);
                    setSelectionEnd(to);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class CapitalizeWordsAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -6834322073295659702L;

        public CapitalizeWordsAction() {
            super("Capitalize Words");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int from = getSelectionStart();
                int to = getSelectionEnd();
                if (from == to) {
                    String newText = capitalizeWords(getText());
                    setText(newText);
                    setCaretPosition(from);
                } else {
                    String newText = capitalizeWords(getSelectedText());
                    getDocument().remove(from, to - from);
                    getDocument().insertString(from, newText, null);
                    setSelectionStart(from);
                    setSelectionEnd(to);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class OpenInDefaultAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -2698661673477971245L;

        public OpenInDefaultAction() {
            super("Open with Defaults");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (Desktop.isDesktopSupported()) {
                    if (workspace.getDataSource().length() > 0) {
                        Desktop.getDesktop().open(new File(workspace.getDataSource()));
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class DeleteAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -5085814595423304961L;

        public DeleteAction() {
            super("Selection");
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (getCaret() instanceof VerticalSelection) {
                    undo.setManualEdit(true);
                    VerticalSelection.delete(TextEditor.this);
                    undo.setManualEdit(false);
                    undoAction.updateUndoState();
                    redoAction.updateRedoState();
                } else {
                    int len = getSelectionEnd() - getSelectionStart();
                    if (len == 0) {
                        getAction(StyledEditorKit.deleteNextCharAction).actionPerformed(e);
                    } else {
                        getDocument().remove(getSelectionStart(), getSelectionEnd() - getSelectionStart());
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    class CapitalizeSentencesAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -7711085600857160409L;

        public CapitalizeSentencesAction() {
            super("Capitalize Sentences");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int from = getSelectionStart();
                int to = getSelectionEnd();
                if (from == to) {
                    String newText = capitalizeSentences(getText());
                    setText(newText);
                    setCaretPosition(from);
                } else {
                    String newText = capitalizeSentences(getSelectedText());
                    getDocument().remove(from, to - from);
                    getDocument().insertString(from, newText, null);
                    setSelectionStart(from);
                    setSelectionEnd(to);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public static final String capitalizeWords(String words) {
        if (words == null || words.length() == 0) {
            return words;
        }
        boolean first = true;
        StringBuilder builder = new StringBuilder();
        boolean alreadyCapitalized = false;
        for (int i = 0; i < words.length(); ++i) {
            char c = words.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                first = true;
                alreadyCapitalized = false;
            } else if (Character.isLetter(c)) {
                if (first) {
                    if (!alreadyCapitalized) {
                        c = Character.toUpperCase(c);
                    }
                    first = false;
                }
            } else {
                alreadyCapitalized = true;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public static final String capitalizeSentences(String words) {
        if (words == null || words.length() == 0) {
            return words;
        }
        int first = 2;
        StringBuilder builder = new StringBuilder();
        boolean alreadyCapitalized = false;
        for (int i = 0; i < words.length(); ++i) {
            char c = words.charAt(i);
            if (".?!".indexOf(c) != -1) {
                if (first == 0) first = 1;
                alreadyCapitalized = false;
            } else if (Character.isSpaceChar(c)) {
                if (first == 1) first = 2;
                alreadyCapitalized = false;
            } else if (Character.isLetterOrDigit(c)) {
                if (first == 2) {
                    if (!alreadyCapitalized) {
                        c = Character.toUpperCase(c);
                    }
                    first = 0;
                } else {
                    first = 0;
                }
            } else {
                alreadyCapitalized = true;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public static String extractWord(String msg, int pad) {
        try {
            boolean hasLetter = false;
            boolean cd;
            int from = pad;
            for (int i = pad - 1; i >= 0; --i) {
                char c = msg.charAt(i);
                if (!(cd = Character.isLetterOrDigit(c)) && c != '_' && c != '-') {
                    break;
                } else {
                    from = i;
                    if (cd) {
                        hasLetter = true;
                    }
                }
            }
            int to = pad;
            while (to < msg.length()) {
                char c = msg.charAt(to);
                if (!(cd = Character.isLetterOrDigit(c)) && c != '_' && c != '-') {
                    break;
                } else {
                    ++to;
                    if (cd) {
                        hasLetter = true;
                    }
                }
            }
            if (!hasLetter) {
                return "";
            }
            return msg.substring(from, to);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    class SpawnClojureTerminalAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 2581879892809763441L;

        public SpawnClojureTerminalAction() {
            super("Start New REPL");
            putValue(ACCELERATOR_KEY, MenuBuilder.getStdKeyStroke(KeyEvent.VK_T, MenuBuilder.NONE));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                RemoteManager manager = RemoteManager.getInstance();
                Properties params = new Properties();
                setTerminal(Configuration.getName());
                params.put("title", getTerminal());
                Configuration cfg = new Configuration(Support.getInstance().getId(), params);
                String workingDir = cfg.get("project-working-directory")[0];
                Process proc = new Process(new File(workingDir), cfg.get("terminal"));
                Stream.redirect(proc.getInput(), System.out);
                clojure = manager.access(getTerminal());
                if (clojure != null) {
                    clojure.run(workingDir, cfg.get("clojure"));
                }
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    class ExecuteStatementAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = -6358209935851394572L;

        public ExecuteStatementAction() {
            super("Execute Statement");
            putValue(ACCELERATOR_KEY, MenuBuilder.getStdKeyStroke(KeyEvent.VK_E, MenuBuilder.NONE));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (clojure == null) {
                    clojure = RemoteManager.getInstance().access(getTerminal());
                }
                int start = -1;
                int end = -1;
                String text = null;
                boolean found = false;
                if (clojure != null) {
                    if (TextEditor.this.getSelectionStart() != TextEditor.this.getSelectionEnd()) {
                        text = TextEditor.this.getSelectedText();
                        start = text.length();
                        end = 0;
                        found = true;
                    } else {
                        start = TextEditor.this.getCaretPosition();
                        text = TextEditor.this.getDocument().getText(0, start);
                        int matching = 0;
                        end = text.length() - 1;
                        for (; end >= 0; --end) {
                            if (text.charAt(end) == ')') {
                                ++matching;
                                if (!found) {
                                    start = end + 1;
                                }
                                found = true;
                            } else if (text.charAt(end) == '(') {
                                if (--matching == 0) {
                                    break;
                                }
                            }
                        }
                    }
                }
                if (found && end != -1) {
                    String statement = text.substring(end, start);
                    clojure.input(statement + '\n');
                } else {
                    getAction(DefaultEditorKit.beepAction).actionPerformed(e);
                }
            } catch (Exception e1) {
                closeClojurePipe();
                throw new RuntimeException(e1);
            }
        }
    }

    class ExecuteFileAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 2816603396117841678L;

        public ExecuteFileAction() {
            super("Execute File");
            putValue(ACCELERATOR_KEY, MenuBuilder.getStdKeyStroke(KeyEvent.VK_E, MenuBuilder.SHIFT));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (clojure == null) {
                    clojure = RemoteManager.getInstance().access(terminalName);
                }
                if (TextEditor.this.workspace.isUnnamed() || clojure == null) {
                    getAction(DefaultEditorKit.beepAction).actionPerformed(e);
                } else {
                    String path = Remote.getPath(workspace.getDataSource());
                    clojure.input("(load-file \"" + path + "\")\n");
                }
            } catch (Exception ex) {
                closeClojurePipe();
                throw new RuntimeException(ex);
            }
        }
    }

    class ConfigureClojureAction extends AbstractAction {

        /**
		 * 
		 */
        private static final long serialVersionUID = 281844340213958973L;

        public ConfigureClojureAction() {
            super("Configure");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            File f = Configuration.getConfigurationSource(Support.getInstance().getId());
            TextMash.actionOpenFile(f.getAbsolutePath());
        }
    }
}
