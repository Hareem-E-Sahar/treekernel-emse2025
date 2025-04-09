package org.sqlexp.controls;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BidiSegmentListener;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.MovementListener;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.sqlexp.controls.accessibility.ICaseControl;
import org.sqlexp.controls.accessibility.ICopyPasteControl;
import org.sqlexp.controls.accessibility.IFindReplaceControl;
import org.sqlexp.controls.accessibility.ILineControl;
import org.sqlexp.controls.accessibility.ISelectableControl;
import org.sqlexp.controls.accessibility.ITextControlViewer;
import org.sqlexp.controls.accessibility.IUndoRedoControl;
import org.sqlexp.controls.actions.ActionFactory;
import org.sqlexp.controls.undoredo.ModifyStackManager;
import org.sqlexp.preferences.EditorsPreference;
import org.sqlexp.preferences.FindReplacePreference;
import org.sqlexp.preferences.IPreferenceListener;
import org.sqlexp.preferences.SqlEditorPreference;
import org.sqlexp.util.syntax.ISyntaxViewer;
import org.sqlexp.util.syntax.TokenContent;

/**
 * Custom text editor.
 * @see StyledText
 * @author Matthieu Réjou
 */
public class ExpTextEditor extends ExpControl implements ITextControlViewer, ISyntaxViewer, IUndoRedoControl, ICopyPasteControl, ISelectableControl, IFindReplaceControl, ICaseControl, ILineControl {

    private static final int LEFT_MARGIN = 20;

    private StyledText textControl;

    private Composite margin;

    private ModifyStackManager modifyStackManager;

    private boolean marginEnabled = false;

    private int lineSelectionStart;

    private boolean lineSelection;

    private TokenContent syntaxContent;

    private ArrayList<Point> searchResults;

    private IPreferenceListener fontPreferenceListener;

    private IPreferenceListener editorPreferenceListener;

    /**
	 * Constructs a text editor,
	 * with default style as <code>SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL</code>.
	 * @param parent composite
	 */
    public ExpTextEditor(final Composite parent) {
        this(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    }

    /**
	 * Constructs a text editor.
	 * @param parent composite
	 * @param style text control style
	 */
    public ExpTextEditor(final Composite parent, final int style) {
        this(parent, style, null);
    }

    /**
	 * Constructs a text editor,
	 * with default style is <code>SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL.</code>
	 * @param parent composite
	 * @param initialText to display on creation
	 */
    public ExpTextEditor(final Composite parent, final String initialText) {
        this(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, initialText);
    }

    /**
	 * Constructs a text editor.
	 * @param parent composite
	 * @param style text control style
	 * @param initialText to display on creation
	 */
    public ExpTextEditor(final Composite parent, final int style, final String initialText) {
        super(parent, new FormLayout());
        textControl = new StyledText(this, style);
        setMainControl(textControl);
        setFont();
        setForeground();
        if (initialText != null) {
            textControl.setText(initialText);
        }
        margin = new Composite(this, SWT.NONE);
        modifyStackManager = new ModifyStackManager(this);
        FormData fd;
        fd = new FormData();
        fd.top = new FormAttachment(0);
        fd.bottom = new FormAttachment(100);
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(0, 0);
        margin.setLayoutData(fd);
        fd = new FormData();
        fd.top = new FormAttachment(0);
        fd.bottom = new FormAttachment(100);
        fd.left = new FormAttachment(margin);
        fd.right = new FormAttachment(100);
        textControl.setLayoutData(fd);
        enablePreferenceManagement();
        enableActionUpdate();
    }

    /**
	 * Enables preference management.
	 */
    private void enablePreferenceManagement() {
        fontPreferenceListener = new IPreferenceListener() {

            @Override
            public void preferenceChanged(final String property) {
                if ("fontName".equals(property)) {
                    setFont();
                } else if ("fontSize".equals(property)) {
                    setFont();
                } else if ("editorTextFont".equals(property)) {
                    setFont();
                }
            }
        };
        EditorsPreference.getInstance().addListener(fontPreferenceListener);
        textControl.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                EditorsPreference.getInstance().removeListener(fontPreferenceListener);
                SqlEditorPreference.getInstance().removeListener(editorPreferenceListener);
            }
        });
    }

    /**
	 * Enables action states updates (consequence in tool bar).
	 */
    private void enableActionUpdate() {
        textControl.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                ActionFactory.getInstance().updateActionState();
            }
        });
    }

    /**
	 * Enables all margin control functionalities.
	 */
    public final void enableMarginFunction() {
        if (marginEnabled) {
            return;
        }
        marginEnabled = true;
        FormData fd = (FormData) margin.getLayoutData();
        fd.right = new FormAttachment(0, LEFT_MARGIN);
        margin.setLayoutData(fd);
        margin.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(final MouseEvent e) {
                super.mouseDown(e);
                StyledText textControl = ExpTextEditor.this.textControl;
                lineSelection = true;
                lineSelectionStart = textControl.getLineIndex(e.y);
                int offset = textControl.getOffsetAtLine(lineSelectionStart);
                int length = textControl.getLine(lineSelectionStart).length();
                setSelectionRange(offset, length);
            }

            @Override
            public void mouseUp(final MouseEvent e) {
                lineSelection = false;
            }
        });
        margin.addMouseMoveListener(new MouseMoveListener() {

            @Override
            public void mouseMove(final MouseEvent e) {
                if (lineSelection) {
                    StyledText textControl = ExpTextEditor.this.textControl;
                    int lineSelectionStart = ExpTextEditor.this.lineSelectionStart;
                    int line = textControl.getLineIndex(e.y);
                    int offset1 = textControl.getOffsetAtLine(lineSelectionStart);
                    int offset2 = textControl.getOffsetAtLine(line);
                    int offset = Math.min(offset1, offset2);
                    int length = Math.abs(offset1 - offset2);
                    if (line < ExpTextEditor.this.lineSelectionStart) {
                        length += textControl.getLine(lineSelectionStart).length();
                    } else {
                        length += textControl.getLine(line).length();
                    }
                    setSelectionRange(offset, length);
                }
            }
        });
        margin.addMouseTrackListener(new MouseTrackAdapter() {

            @Override
            public void mouseExit(final MouseEvent e) {
                lineSelection = false;
            }
        });
    }

    /**
	 * Enables all tabulation functionalities.
	 */
    public final void enableTabManagement() {
        textControl.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(final TraverseEvent e) {
                e.doit = false;
            }
        });
        textControl.setKeyBinding('\t', -1);
        textControl.setKeyBinding(SWT.MOD2 | '\t', -1);
        EditorsPreference preference = EditorsPreference.getInstance();
        textControl.setTabs(preference.getTabWidth());
        editorPreferenceListener = new IPreferenceListener() {

            @Override
            public void preferenceChanged(final String property) {
                if ("tabWidth".equals(property)) {
                    EditorsPreference preference = EditorsPreference.getInstance();
                    textControl.setTabs(preference.getTabWidth());
                }
            }
        };
        preference.addListener(editorPreferenceListener);
        textControl.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.character == '\t') {
                    EditorsPreference preference = EditorsPreference.getInstance();
                    Point selection = textControl.getSelectionRange();
                    if (selection.y == 0 && e.stateMask == 0) {
                        if (preference.isReplaceTabs()) {
                            for (int i = 0; i < preference.getTabWidth(); i++) {
                                textControl.insert(" ");
                                setSelection(selection.x + i + 1);
                            }
                        } else {
                            textControl.insert("\t");
                            setSelection(selection.x + 1);
                        }
                    } else if (e.stateMask == SWT.MOD2) {
                        indentLeft();
                    } else if (selection.y > 0 && e.stateMask == 0) {
                        indentRight();
                    }
                }
            }
        });
    }

    /**
	 * Defines font stored in <code>FontPreference</code> object.
	 */
    private void setFont() {
        EditorsPreference fontPreference = EditorsPreference.getInstance();
        Font font = new Font(textControl.getDisplay(), fontPreference.getFontName(), fontPreference.getFontSize(), SWT.NONE);
        textControl.setFont(font);
    }

    /**
	 * Defines foreground color stored in <code>FontPreference</code> object.
	 */
    private void setForeground() {
        EditorsPreference fontPreference = EditorsPreference.getInstance();
        int[] rgb = fontPreference.getEditorTextFont().getRgb();
        RGB foreGroundRgb = new RGB(rgb[0], rgb[1], rgb[2]);
        Color foreGround = new Color(textControl.getDisplay(), foreGroundRgb);
        textControl.setForeground(foreGround);
    }

    @Override
    public final void undo() {
        modifyStackManager.undo();
    }

    @Override
    public final void redo() {
        modifyStackManager.redo();
    }

    @Override
    public final void showFind() {
        ExpFindReplace.showFindReplace(this);
    }

    @Override
    public final boolean find(final String string, final boolean isForward, final boolean isSelection, final boolean isFirst, final boolean next) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        FindReplacePreference preference = FindReplacePreference.getInstance();
        Point searchBounds = getSearchBounds(isSelection);
        if (searchBounds.y == 0) {
            return false;
        }
        if (isFirst) {
            searchResults = getSearchResults(searchBounds, string);
        }
        int initialOffset = getInitialSearchOffset(isForward, isSelection, isFirst, next, searchBounds);
        Point choice = null;
        boolean available;
        int paths = 1;
        if (preference.isWrap()) {
            paths = 2;
        }
        for (int i = 0; i < paths; i++) {
            for (Point result : searchResults) {
                available = false;
                if (isForward && result.x >= initialOffset) {
                    available = true;
                } else if (!isForward && result.y <= initialOffset) {
                    available = true;
                }
                if (available) {
                    if (choice == null) {
                        choice = result;
                    } else {
                        if (isForward && result.x < choice.x) {
                            choice = result;
                        } else if (!isForward && result.y > choice.y) {
                            choice = result;
                        }
                    }
                }
            }
            if (choice != null) {
                break;
            } else {
                if (isForward) {
                    initialOffset = searchBounds.x;
                } else {
                    initialOffset = searchBounds.x + searchBounds.y;
                }
            }
        }
        if (choice == null) {
            return false;
        }
        setSelection(choice);
        textControl.showSelection();
        return true;
    }

    @Override
    @Deprecated
    public final void replace(final String replacement) {
        Point selection = textControl.getSelection();
        textControl.replaceTextRange(selection.x, selection.y - selection.x + 1, replacement);
    }

    @Override
    @Deprecated
    public final boolean replaceAll(final String string, final boolean isForward, final boolean isSelection, final String replacement) {
        if (string == null || string.isEmpty()) {
            return false;
        }
        FindReplacePreference preference = FindReplacePreference.getInstance();
        Point searchBounds = getSearchBounds(isSelection);
        if (searchBounds.y == 0) {
            return false;
        }
        ArrayList<Point> results = getSearchResults(searchBounds, string);
        int initialSearchOffset = 0;
        if (!preference.isWrap()) {
            initialSearchOffset = getInitialSearchOffset(isForward, isSelection, true, false, searchBounds);
        }
        int start = 0, step = 1;
        if (!isForward) {
            start = results.size() - 1;
            step = -1;
        }
        modifyStackManager.beginGroup(false);
        Point last = null;
        boolean replace;
        Point result;
        for (int i = start; i < results.size() && i >= 0; i += step) {
            result = results.get(i);
            replace = false;
            if (preference.isWrap()) {
                replace = true;
            } else if (isForward && result.x >= initialSearchOffset) {
                replace = true;
            } else if (!isForward && result.y <= initialSearchOffset) {
                replace = true;
            }
            if (replace) {
                for (int j = i + step; j < results.size() && j >= 0; j += step) {
                    Point following = results.get(j);
                    if (isForward && following.x <= result.y) {
                        i += step;
                    } else if (!isForward && following.y >= result.x) {
                        i += step;
                    } else if (isForward) {
                        int dLength = replacement.length() - (result.y - result.x);
                        following.x += dLength;
                        following.y += dLength;
                    } else {
                        continue;
                    }
                }
                textControl.replaceTextRange(result.x, result.y - result.x, replacement);
                last = result;
            }
        }
        modifyStackManager.endGroup();
        if (last == null) {
            return false;
        }
        setSelection(last.x, last.x + replacement.length());
        textControl.showSelection();
        return true;
    }

    /**
	 * Computes search bounds.<br>
	 * Used by find and replaceAll methods.
	 * @param isSelection true if search is limited to selection range
	 * @return point representing search bounds as in <code>StyledText.getSelectionRange()</code>
	 * @see StyledText#getSelectionRange()
	 */
    private Point getSearchBounds(final boolean isSelection) {
        if (isSelection) {
            Point searchBounds = textControl.getSelectionRange();
            if (searchBounds.y == 0) {
                int line = textControl.getLineAtOffset(searchBounds.x);
                searchBounds.x = textControl.getOffsetAtLine(line);
                searchBounds.y = textControl.getLine(line).length();
            }
            return searchBounds;
        } else {
            return new Point(0, textControl.getCharCount());
        }
    }

    /**
	 * Computes initial search offset used to filter result tokens.
	 * @param isForward true if search is forward, false if backward
	 * @param isSelection true if search is limited to selection range
	 * @param isFirst true if search is the first one for this find session
	 * @param next true if, even if current selection matches a result, next one is chosen
	 * @param searchBounds computed with <code>getSearchBounds()</code>
	 * @return initial search offset, usually cursor location
	 * @see #getSearchBounds(boolean)
	 */
    private int getInitialSearchOffset(final boolean isForward, final boolean isSelection, final boolean isFirst, final boolean next, final Point searchBounds) {
        if (isFirst && isSelection) {
            if (isForward) {
                return searchBounds.x;
            } else {
                return searchBounds.x + searchBounds.y;
            }
        } else {
            Point selectionRange = textControl.getSelectionRange();
            if (selectionRange.y > 0) {
                int nextOffset = 0;
                if (next) {
                    nextOffset = 1;
                }
                if (isForward) {
                    return selectionRange.x + nextOffset;
                } else {
                    return selectionRange.x + selectionRange.y - nextOffset;
                }
            } else {
                return textControl.getCaretOffset();
            }
        }
    }

    /**
	 * Computes search results list.
	 * @param searchBounds computed with <code>getSearchBounds()</code>
	 * @param string searched for
	 * @return ordered list of found tokens
	 * @see #getSearchBounds(boolean)
	 */
    private ArrayList<Point> getSearchResults(final Point searchBounds, final String string) {
        FindReplacePreference preference = FindReplacePreference.getInstance();
        boolean regex = preference.isRegex();
        boolean caseSensitive = preference.isCaseSensitive();
        String text = textControl.getText(searchBounds.x, searchBounds.x + searchBounds.y - 1);
        if (regex) {
            return regexSearch(text, string, caseSensitive);
        } else {
            boolean wholeWord = preference.isWholeWord();
            return regularSearch(text, string, caseSensitive, wholeWord);
        }
    }

    /**
	 * Performs a regular expression specific search.<br>
	 * Do not use outside search() method.
	 * @param text containing searched string
	 * @param string searched for
	 * @param caseSensitive true if search is case sensitive
	 * @return ordered list of found tokens
	 * @see ExpTextEditor#computeSearchResults(String, String, boolean, boolean, boolean)
	 */
    private ArrayList<Point> regexSearch(final String text, final String string, final boolean caseSensitive) {
        int loc = 0;
        Point match;
        int caseFlag = 0;
        if (!caseSensitive) {
            caseFlag = Pattern.CASE_INSENSITIVE;
        }
        Pattern pattern = Pattern.compile(string, caseFlag);
        Matcher matcher = pattern.matcher(Matcher.quoteReplacement(text));
        ArrayList<Point> results = new ArrayList<Point>();
        while (matcher.find(loc)) {
            match = new Point(matcher.start(), matcher.end());
            results.add(match);
            loc = match.x + 1;
        }
        return results;
    }

    /**
	 * Performs a regular specific search.<br>
	 * Do not use outside search() method.
	 * @param text containing searched string
	 * @param string searched for
	 * @param caseSensitive true if search is case sensitive
	 * @param wholeWord true if results bounding chars have to be separators
	 * @return ordered list of found tokens
	 * @see ExpTextEditor#computeSearchResults(String, String, boolean, boolean, boolean)
	 */
    private ArrayList<Point> regularSearch(final String text, final String string, final boolean caseSensitive, final boolean wholeWord) {
        String searchText = text;
        String searchString = string;
        if (!caseSensitive) {
            searchText = text.toUpperCase();
            searchString = string.toUpperCase();
        }
        int loc = 0;
        Point match;
        ArrayList<Point> results = new ArrayList<Point>();
        int size = searchString.length();
        loc = 0;
        do {
            loc = searchText.indexOf(searchString, loc);
            if (loc != -1) {
                match = new Point(loc, loc + size);
                if (wholeWord) {
                    if (loc > 0 && Character.isJavaIdentifierPart(searchText.charAt(loc - 1))) {
                        loc = match.x + 1;
                        continue;
                    }
                    if (loc < searchText.length() - 1 && Character.isJavaIdentifierPart(searchText.charAt(loc + size))) {
                        loc = match.x + 1;
                        continue;
                    }
                }
                results.add(match);
                loc = match.x + 1;
            }
        } while (loc != -1);
        return results;
    }

    @Override
    public final boolean canUndo() {
        return modifyStackManager.canUndo();
    }

    @Override
    public final boolean canRedo() {
        return modifyStackManager.canRedo();
    }

    @Override
    public final boolean canCopy() {
        return !getSelectionText().isEmpty();
    }

    @Override
    public final boolean canCut() {
        return canCopy();
    }

    @Override
    public final boolean canPaste() {
        Clipboard clipboard = new Clipboard(getDisplay());
        TextTransfer transfer = TextTransfer.getInstance();
        String content = (String) clipboard.getContents(transfer);
        return content != null && !content.isEmpty();
    }

    @Override
    public final void copy() {
        Clipboard clipboard = new Clipboard(getDisplay());
        String text = textControl.getSelectionText();
        text = text.replace("\0", "");
        clipboard.setContents(new Object[] { text }, new Transfer[] { TextTransfer.getInstance() });
    }

    @Override
    public final void cut() {
        copy();
        Point selection = textControl.getSelectionRange();
        textControl.replaceTextRange(selection.x, selection.y, "");
    }

    @Override
    public final void paste() {
        textControl.paste();
    }

    @Override
    public final void toUpper() {
        Point selection = textControl.getSelectionRange();
        String replacement = textControl.getSelectionText().toUpperCase();
        textControl.insert(replacement);
        setSelectionRange(selection.x, selection.y);
    }

    @Override
    public final void toLower() {
        Point selection = textControl.getSelectionRange();
        String replacement = textControl.getSelectionText().toLowerCase();
        textControl.insert(replacement);
        setSelectionRange(selection.x, selection.y);
    }

    /**
	 * Indents selected lines to right.
	 */
    public final void indentRight() {
        modifyStackManager.beginGroup(true);
        String tab;
        EditorsPreference preference = EditorsPreference.getInstance();
        if (preference.isReplaceTabs()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < preference.getTabWidth(); i++) {
                sb.append(' ');
            }
            tab = sb.toString();
        } else {
            tab = "\t";
        }
        Point selection = textControl.getSelectionRange();
        int firstLine = textControl.getLineAtOffset(selection.x);
        int lastLine = textControl.getLineAtOffset(selection.x + selection.y);
        int offset;
        for (int line = firstLine; line <= lastLine; line++) {
            offset = textControl.getOffsetAtLine(line);
            textControl.replaceTextRange(offset, 0, tab);
        }
        int selectionStart = selection.x + tab.length();
        int selectionLength = selection.y;
        if (firstLine != lastLine) {
            selectionLength += tab.length() * (lastLine - firstLine);
        }
        setSelection(selectionStart, selectionStart + selectionLength);
        modifyStackManager.endGroup();
    }

    /**
	 * Indents selected lines to left.
	 */
    public final void indentLeft() {
        modifyStackManager.beginGroup(true);
        String spaces;
        EditorsPreference preference = EditorsPreference.getInstance();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < preference.getTabWidth(); i++) {
            sb.append(' ');
        }
        spaces = sb.toString();
        Point selection = textControl.getSelectionRange();
        int firstLine = textControl.getLineAtOffset(selection.x);
        int lastLine = textControl.getLineAtOffset(selection.x + selection.y);
        int offset, replacement;
        int firstReplacement = 0, totalReplacement = 0;
        String lineText;
        for (int line = firstLine; line <= lastLine; line++) {
            lineText = textControl.getLine(line);
            replacement = 0;
            if (lineText != null && !lineText.isEmpty()) {
                if (lineText.charAt(0) == '\t') {
                    replacement = 1;
                } else if (lineText.startsWith(spaces)) {
                    replacement = spaces.length();
                }
            }
            if (replacement > 0) {
                offset = textControl.getOffsetAtLine(line);
                textControl.replaceTextRange(offset, replacement, "");
                if (line == firstLine) {
                    firstReplacement = replacement;
                } else {
                    totalReplacement += replacement;
                }
            }
        }
        int selectionStart = selection.x - firstReplacement;
        int selectionLength = selection.y;
        if (firstLine != lastLine) {
            selectionLength -= totalReplacement;
        }
        setSelection(selectionStart, selectionStart + selectionLength);
        modifyStackManager.endGroup();
    }

    @Override
    public final boolean canSelect() {
        return textControl.getCharCount() > 0;
    }

    @Override
    public final TokenContent getTokenContent() {
        return syntaxContent;
    }

    @Override
    public final void setTokenContent(final TokenContent content) {
        syntaxContent = content;
    }

    /**
	 * Gets the character at given offset.
	 * @param offset index (offset >= 0 or offset < getCharCount() -1)
	 * @return character at offset
	 */
    public final char getCharAt(final int offset) {
        return textControl.getText(offset, offset + 1).charAt(0);
    }

    @Override
    public final void goToLine() {
        new ExpGoToLineControl(this);
    }

    @Override
    public final void goToLine(final int line) {
        setSelectedLine(line, line);
    }

    @Override
    public final int getCurrentLine() {
        return textControl.getLineAtOffset(textControl.getSelection().x);
    }

    @Override
    public final int getLineCount() {
        return textControl.getLineCount();
    }

    @Override
    public final void setSelectedLine(final int firstLine, final int lastLine) {
        int start = textControl.getOffsetAtLine(firstLine);
        int end = textControl.getOffsetAtLine(lastLine) + textControl.getLine(lastLine).length();
        setSelectionRange(start, end - start);
        textControl.showSelection();
    }

    /**
	 * Gets the modify stack manager.
	 * @return the modify stack manager
	 */
    protected final ModifyStackManager getModifyStackManager() {
        return modifyStackManager;
    }

    /**
	 * Notifies selections listeners.<br>
	 * Used when selection is updated by code.
	 */
    private void notifySelectionListeners() {
        Event event = new Event();
        event.display = getDisplay();
        event.doit = true;
        event.widget = textControl;
        textControl.notifyListeners(SWT.Selection, event);
        ActionFactory.getInstance().updateActionState();
    }

    /**
	 * @return the text
	 * @see org.eclipse.swt.custom.StyledText#getText()
	 */
    @Override
    public String getText() {
        return textControl.getText();
    }

    /**
	 * @param start
	 * @param end
	 * @return text
	 * @see org.eclipse.swt.custom.StyledText#getText(int, int)
	 */
    @Override
    public String getText(final int start, final int end) {
        return textControl.getText(start, end);
    }

    /**
	 * @param text
	 * @see org.eclipse.swt.custom.StyledText#setText(java.lang.String)
	 */
    public void setText(final String text) {
        textControl.setText(text);
    }

    /**
	 * @param string
	 * @see org.eclipse.swt.custom.StyledText#insert(java.lang.String)
	 */
    public void insert(final String string) {
        textControl.insert(string);
    }

    /**
	 * @return text
	 * @see org.eclipse.swt.custom.StyledText#getText(int, int)
	 */
    public String getSelectionText() {
        return textControl.getSelectionText();
    }

    /**
	 * @return caret offset
	 * @see org.eclipse.swt.custom.StyledText#getCaretOffset()
	 */
    public int getCaretOffset() {
        return textControl.getCaretOffset();
    }

    /**
	 * @param offset
	 * @see org.eclipse.swt.custom.StyledText#setCaretOffset(int)
	 */
    public void setCaretOffset(final int offset) {
        textControl.setCaretOffset(offset);
    }

    /**
	 * @param caretOffset
	 * @return location
	 * @see org.eclipse.swt.custom.StyledText#getLocationAtOffset(int)
	 */
    public Point getLocationAtOffset(final int caretOffset) {
        return textControl.getLocationAtOffset(caretOffset);
    }

    /**
	 * @param point
	 * @return offset
	 * @see org.eclipse.swt.custom.StyledText#getOffsetAtLocation(org.eclipse.swt.graphics.Point)
	 */
    public int getOffsetAtLocation(final Point point) {
        return textControl.getOffsetAtLocation(point);
    }

    /**
	 * @return line height
	 * @see org.eclipse.swt.custom.StyledText#getLocationAtOffset(int)
	 */
    public int getLineHeight() {
        return textControl.getLineHeight();
    }

    /**
	 * @param lineIndex
	 * @return offset
	 * @see org.eclipse.swt.custom.StyledText#getLocationAtOffset(int)
	 */
    public int getOffsetAtLine(final int lineIndex) {
        return textControl.getOffsetAtLine(lineIndex);
    }

    /**
	 * @param lineIndex
	 * @return line text
	 * @see org.eclipse.swt.custom.StyledText#getLine(int)
	 */
    public String getLine(final int lineIndex) {
        return textControl.getLine(lineIndex);
    }

    /**
	 * @param offset
	 * @return line offset
	 * @see org.eclipse.swt.custom.StyledText#getLineAtOffset(int)
	 */
    @Override
    public int getLineAtOffset(final int offset) {
        return textControl.getLineAtOffset(offset);
    }

    /**
	 * @param start
	 * @param length
	 * @param text
	 * @see org.eclipse.swt.custom.StyledText#replaceTextRange(int, int, String)
	 */
    @Override
    public void replaceTextRange(final int start, final int length, final String text) {
        textControl.replaceTextRange(start, length, text);
    }

    /**
	 * @return char count
	 * @see org.eclipse.swt.custom.StyledText#getCharCount()
	 */
    @Override
    public int getCharCount() {
        return textControl.getCharCount();
    }

    /**
	 * @param key
	 * @return key binding
	 * @see org.eclipse.swt.custom.StyledText#getKeyBinding(int)
	 */
    public int getKeyBinding(final int key) {
        return textControl.getKeyBinding(key);
    }

    /**
	 * @param key
	 * @param action
	 * @see org.eclipse.swt.custom.StyledText#setKeyBinding(int, int)
	 */
    public void setKeyBinding(final int key, final int action) {
        textControl.setKeyBinding(key, action);
    }

    /**
	 * @see org.eclipse.swt.custom.StyledText#selectAll()
	 */
    @Override
    public void selectAll() {
        int location = textControl.getTopPixel();
        textControl.selectAll();
        textControl.setTopPixel(location);
        notifySelectionListeners();
    }

    /**
	 * @param start
	 * @see org.eclipse.swt.custom.StyledText#setSelection(int, int)
	 */
    public void setSelection(final int start) {
        textControl.setSelection(start);
        notifySelectionListeners();
    }

    /**
	 * @param start
	 * @param end
	 * @see org.eclipse.swt.custom.StyledText#setSelection(int, int)
	 */
    @Override
    public void setSelection(final int start, final int end) {
        textControl.setSelection(start, end);
        notifySelectionListeners();
    }

    /**
	 * @param point
	 * @see org.eclipse.swt.custom.StyledText#setSelection(org.eclipse.swt.graphics.Point)
	 */
    protected void setSelection(final Point point) {
        textControl.setSelection(point);
        notifySelectionListeners();
    }

    /**
	 * @param start
	 * @param length
	 * @see org.eclipse.swt.custom.StyledText#setSelectionRange(int, int)
	 */
    protected void setSelectionRange(final int start, final int length) {
        textControl.setSelectionRange(start, length);
        notifySelectionListeners();
    }

    /**
	 * @return selection range
	 * @see org.eclipse.swt.custom.StyledText#getSelectionRange()
	 */
    public Point getSelectionRange() {
        return textControl.getSelectionRange();
    }

    /**
	 * @return selection character count
	 * @see org.eclipse.swt.custom.StyledText#getSelectionCount()
	 */
    public int getSelectionCount() {
        return textControl.getSelectionCount();
    }

    /**
	 * @param editable
	 * @see org.eclipse.swt.custom.StyledText#setEditable(boolean)
	 */
    public void setEditable(final boolean editable) {
        textControl.setEditable(editable);
    }

    /**
	 * @return true if editable
	 * @see org.eclipse.swt.custom.StyledText#getEditable()
	 */
    public boolean getEditable() {
        return textControl.getEditable();
    }

    /**
	 * @return true if enabled
	 * @see org.eclipse.swt.widgets.Control#getEnabled()
	 */
    @Override
    public boolean getEnabled() {
        return textControl.getEnabled();
    }

    /**
	 * @param color
	 * @see org.eclipse.swt.custom.StyledText#setForeground(org.eclipse.swt.graphics.Color)
	 */
    @Override
    public final void setForeground(final Color color) {
        textControl.setForeground(color);
    }

    /**
	 * @return the receiver's font
	 * @see org.eclipse.swt.widgets.Control#getFont()
	 */
    @Override
    public Font getFont() {
        return textControl.getFont();
    }

    /**
	 * @param font
	 * @see org.eclipse.swt.custom.StyledText#setFont(org.eclipse.swt.graphics.Font)
	 */
    @Override
    public void setFont(final Font font) {
        textControl.setFont(font);
    }

    /**
	 * @param range
	 * @see org.eclipse.swt.custom.StyledText#setStyleRange(org.eclipse.swt.custom.StyleRange)
	 */
    public void setStyleRange(final StyleRange range) {
        textControl.setStyleRange(range);
    }

    /**
	 * @param start
	 * @param length
	 * @param ranges
	 * @see org.eclipse.swt.custom.StyledText#replaceStyleRanges(int, int, org.eclipse.swt.custom.StyleRange[])
	 */
    public void replaceStyleRanges(final int start, final int length, final StyleRange[] ranges) {
        textControl.replaceStyleRanges(start, length, ranges);
    }

    /**
	 * @param ranges
	 * @param styles
	 * @see org.eclipse.swt.custom.StyledText#setStyleRanges(int[], org.eclipse.swt.custom.StyleRange[])
	 */
    public void setStyleRanges(final int[] ranges, final StyleRange[] styles) {
        textControl.setStyleRanges(ranges, styles);
    }

    /**
	 * @param ranges
	 * @see org.eclipse.swt.custom.StyledText#setStyleRanges(org.eclipse.swt.custom.StyleRange[])
	 */
    public void setStyleRanges(final StyleRange[] ranges) {
        textControl.setStyleRanges(ranges);
    }

    /**
	 * @return defined style ranges
	 * @see org.eclipse.swt.custom.StyledText#getStyleRanges()
	 */
    public StyleRange[] getStyleRanges() {
        return textControl.getStyleRanges();
    }

    /**
	 * @param start
	 * @param length
	 * @return defined style ranges
	 * @see org.eclipse.swt.custom.StyledText#getStyleRanges()
	 */
    public StyleRange[] getStyleRanges(final int start, final int length) {
        return textControl.getStyleRanges(start, length);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#addBidiSegmentListener(org.eclipse.swt.custom.BidiSegmentListener)
	 */
    public void addBidiSegmentListener(final BidiSegmentListener listener) {
        textControl.addBidiSegmentListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addControlListener(org.eclipse.swt.events.ControlListener)
	 */
    @Override
    public void addControlListener(final ControlListener listener) {
        textControl.addControlListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Widget#addDisposeListener(org.eclipse.swt.events.DisposeListener)
	 */
    @Override
    public void addDisposeListener(final DisposeListener listener) {
        textControl.addDisposeListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addDragDetectListener(org.eclipse.swt.events.DragDetectListener)
	 */
    @Override
    public void addDragDetectListener(final DragDetectListener listener) {
        textControl.addDragDetectListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addFocusListener(org.eclipse.swt.events.FocusListener)
	 */
    @Override
    public void addFocusListener(final FocusListener listener) {
        textControl.addFocusListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addHelpListener(org.eclipse.swt.events.HelpListener)
	 */
    @Override
    public void addHelpListener(final HelpListener listener) {
        textControl.addHelpListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addKeyListener(org.eclipse.swt.events.KeyListener)
	 */
    @Override
    public void addKeyListener(final KeyListener listener) {
        textControl.addKeyListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#addLineBackgroundListener(org.eclipse.swt.custom.LineBackgroundListener)
	 */
    public void addLineBackgroundListener(final LineBackgroundListener listener) {
        textControl.addLineBackgroundListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#addLineStyleListener(org.eclipse.swt.custom.LineStyleListener)
	 */
    public void addLineStyleListener(final LineStyleListener listener) {
        textControl.addLineStyleListener(listener);
    }

    /**
	 * @param eventType
	 * @param listener
	 * @see org.eclipse.swt.widgets.Widget#addListener(int, org.eclipse.swt.widgets.Listener)
	 */
    @Override
    public void addListener(final int eventType, final Listener listener) {
        textControl.addListener(eventType, listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addMenuDetectListener(org.eclipse.swt.events.MenuDetectListener)
	 */
    @Override
    public void addMenuDetectListener(final MenuDetectListener listener) {
        textControl.addMenuDetectListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addMouseListener(org.eclipse.swt.events.MouseListener)
	 */
    @Override
    public void addMouseListener(final MouseListener listener) {
        textControl.addMouseListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addMouseMoveListener(org.eclipse.swt.events.MouseMoveListener)
	 */
    @Override
    public void addMouseMoveListener(final MouseMoveListener listener) {
        textControl.addMouseMoveListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addMouseTrackListener(org.eclipse.swt.events.MouseTrackListener)
	 */
    @Override
    public void addMouseTrackListener(final MouseTrackListener listener) {
        textControl.addMouseTrackListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addMouseWheelListener(org.eclipse.swt.events.MouseWheelListener)
	 */
    @Override
    public void addMouseWheelListener(final MouseWheelListener listener) {
        textControl.addMouseWheelListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addPaintListener(org.eclipse.swt.events.PaintListener)
	 */
    @Override
    public void addPaintListener(final PaintListener listener) {
        textControl.addPaintListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#addPaintObjectListener(org.eclipse.swt.custom.PaintObjectListener)
	 */
    public void addPaintObjectListener(final PaintObjectListener listener) {
        textControl.addPaintObjectListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#addSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
    public void addSelectionListener(final SelectionListener listener) {
        textControl.addSelectionListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#addTraverseListener(org.eclipse.swt.events.TraverseListener)
	 */
    @Override
    public void addTraverseListener(final TraverseListener listener) {
        textControl.addTraverseListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#addVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
    public void addVerifyKeyListener(final VerifyKeyListener listener) {
        textControl.addVerifyKeyListener(listener);
    }

    /**
	 * @param verifyListener
	 * @see org.eclipse.swt.custom.StyledText#addVerifyListener(org.eclipse.swt.events.VerifyListener)
	 */
    public void addVerifyListener(final VerifyListener verifyListener) {
        textControl.addVerifyListener(verifyListener);
    }

    /**
	 * @param modifyListener
	 * @see org.eclipse.swt.custom.StyledText#addModifyListener(org.eclipse.swt.events.ModifyListener)
	 */
    public void addModifyListener(final ModifyListener modifyListener) {
        textControl.addModifyListener(modifyListener);
    }

    /**
	 * @param extendedModifyListener
	 * @see org.eclipse.swt.custom.StyledText#addExtendedModifyListener(org.eclipse.swt.custom.ExtendedModifyListener)
	 */
    @Override
    public void addExtendedModifyListener(final ExtendedModifyListener extendedModifyListener) {
        textControl.addExtendedModifyListener(extendedModifyListener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#removeBidiSegmentListener(org.eclipse.swt.custom.BidiSegmentListener)
	 */
    public void removeBidiSegmentListener(final BidiSegmentListener listener) {
        textControl.removeBidiSegmentListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeControlListener(org.eclipse.swt.events.ControlListener)
	 */
    @Override
    public void removeControlListener(final ControlListener listener) {
        textControl.removeControlListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Widget#removeDisposeListener(org.eclipse.swt.events.DisposeListener)
	 */
    @Override
    public void removeDisposeListener(final DisposeListener listener) {
        textControl.removeDisposeListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeDragDetectListener(org.eclipse.swt.events.DragDetectListener)
	 */
    @Override
    public void removeDragDetectListener(final DragDetectListener listener) {
        textControl.removeDragDetectListener(listener);
    }

    /**
	 * @param extendedModifyListener
	 * @see org.eclipse.swt.custom.StyledText#removeExtendedModifyListener(org.eclipse.swt.custom.ExtendedModifyListener)
	 */
    public void removeExtendedModifyListener(final ExtendedModifyListener extendedModifyListener) {
        textControl.removeExtendedModifyListener(extendedModifyListener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeFocusListener(org.eclipse.swt.events.FocusListener)
	 */
    @Override
    public void removeFocusListener(final FocusListener listener) {
        textControl.removeFocusListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeHelpListener(org.eclipse.swt.events.HelpListener)
	 */
    @Override
    public void removeHelpListener(final HelpListener listener) {
        textControl.removeHelpListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeKeyListener(org.eclipse.swt.events.KeyListener)
	 */
    @Override
    public void removeKeyListener(final KeyListener listener) {
        textControl.removeKeyListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#removeLineBackgroundListener(org.eclipse.swt.custom.LineBackgroundListener)
	 */
    public void removeLineBackgroundListener(final LineBackgroundListener listener) {
        textControl.removeLineBackgroundListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#removeLineStyleListener(org.eclipse.swt.custom.LineStyleListener)
	 */
    public void removeLineStyleListener(final LineStyleListener listener) {
        textControl.removeLineStyleListener(listener);
    }

    /**
	 * @param eventType
	 * @param handler
	 * @see org.eclipse.swt.widgets.Widget#removeListener(int, org.eclipse.swt.widgets.Listener)
	 */
    @Override
    public void removeListener(final int eventType, final Listener handler) {
        textControl.removeListener(eventType, handler);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeMenuDetectListener(org.eclipse.swt.events.MenuDetectListener)
	 */
    @Override
    public void removeMenuDetectListener(final MenuDetectListener listener) {
        textControl.removeMenuDetectListener(listener);
    }

    /**
	 * @param modifyListener
	 * @see org.eclipse.swt.custom.StyledText#removeModifyListener(org.eclipse.swt.events.ModifyListener)
	 */
    public void removeModifyListener(final ModifyListener modifyListener) {
        textControl.removeModifyListener(modifyListener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeMouseListener(org.eclipse.swt.events.MouseListener)
	 */
    @Override
    public void removeMouseListener(final MouseListener listener) {
        textControl.removeMouseListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeMouseMoveListener(org.eclipse.swt.events.MouseMoveListener)
	 */
    @Override
    public void removeMouseMoveListener(final MouseMoveListener listener) {
        textControl.removeMouseMoveListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeMouseTrackListener(org.eclipse.swt.events.MouseTrackListener)
	 */
    @Override
    public void removeMouseTrackListener(final MouseTrackListener listener) {
        textControl.removeMouseTrackListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeMouseWheelListener(org.eclipse.swt.events.MouseWheelListener)
	 */
    @Override
    public void removeMouseWheelListener(final MouseWheelListener listener) {
        textControl.removeMouseWheelListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removePaintListener(org.eclipse.swt.events.PaintListener)
	 */
    @Override
    public void removePaintListener(final PaintListener listener) {
        textControl.removePaintListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#removePaintObjectListener(org.eclipse.swt.custom.PaintObjectListener)
	 */
    public void removePaintObjectListener(final PaintObjectListener listener) {
        textControl.removePaintObjectListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#removeSelectionListener(org.eclipse.swt.events.SelectionListener)
	 */
    public void removeSelectionListener(final SelectionListener listener) {
        textControl.removeSelectionListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.widgets.Control#removeTraverseListener(org.eclipse.swt.events.TraverseListener)
	 */
    @Override
    public void removeTraverseListener(final TraverseListener listener) {
        textControl.removeTraverseListener(listener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#removeVerifyKeyListener(org.eclipse.swt.custom.VerifyKeyListener)
	 */
    public void removeVerifyKeyListener(final VerifyKeyListener listener) {
        textControl.removeVerifyKeyListener(listener);
    }

    /**
	 * @param verifyListener
	 * @see org.eclipse.swt.custom.StyledText#removeVerifyListener(org.eclipse.swt.events.VerifyListener)
	 */
    public void removeVerifyListener(final VerifyListener verifyListener) {
        textControl.removeVerifyListener(verifyListener);
    }

    /**
	 * @param listener
	 * @see org.eclipse.swt.custom.StyledText#removeWordMovementListener(org.eclipse.swt.custom.MovementListener)
	 */
    public void removeWordMovementListener(final MovementListener listener) {
        textControl.removeWordMovementListener(listener);
    }
}
