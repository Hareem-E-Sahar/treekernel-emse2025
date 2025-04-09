package net.sourceforge.regexview.views;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.regexview.RegExViewPlugin;
import net.sourceforge.regexview.preferences.IPreferenceConstants;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

/**
 * A view to evaluate regular expressions against a text.
 * 
 * @author Reto Christen
 */
public class RegExView extends ViewPart implements KeyListener, SelectionListener, VerifyListener {

    /** The property name for action commands. */
    private static final String ACTION_COMMAND = "actionCommand";

    /** The property name for regular expression commands. */
    private static final String REG_EX_COMMAND = "regExCommand";

    /** The action command for the live evaluation checkbox. */
    private static final int LIVE_EVAL_ACTION_COMMAND = 1;

    /** The action command for the evaluation button. */
    private static final int EVALUATION_ACTION_COMMAND = 2;

    /** The action command for the popup menu items. */
    private static final int POPUP_MENUITEM_ACTION_COMMAND = 3;

    /** The entry field for the regular expression. */
    private Text fRegEx;

    /** The entry field. */
    private StyledText fText;

    /** The result field. */
    private Text fResult;

    /** The line styler for regular expressions. */
    private final RegExLineStyler fLineStyler = new RegExLineStyler();

    /** Flag to turn on live evaluation. */
    private boolean fLiveEval = true;

    /**
     * Creates the SWT controls for this workbench part.
     * 
     * @param parent
     *            The parent control.
     */
    public void createPartControl(final Composite parent) {
        GridLayout gridLayout = new GridLayout();
        parent.setLayout(gridLayout);
        Group group = new Group(parent, SWT.NONE);
        group.setText(RegExViewPlugin.getResourceString("expression"));
        gridLayout = new GridLayout();
        gridLayout.marginWidth = 0;
        gridLayout.numColumns = 2;
        group.setLayout(gridLayout);
        GridData compositeGridData = new GridData();
        compositeGridData.horizontalAlignment = GridData.FILL;
        compositeGridData.grabExcessHorizontalSpace = true;
        group.setLayoutData(compositeGridData);
        Menu popupMenu = new Menu(parent.getShell(), SWT.POP_UP);
        Menu cascade = addCascadeMenu(popupMenu, "Characters");
        addMenuItem(cascade, "\\", "The backslash character");
        addMenuItem(cascade, "\\0n", "The character with octal value 0n  (0 <= n <= 7)");
        addMenuItem(cascade, "\\0nn", "The character with octal value 0n  (0 <= n <= 7)");
        addMenuItem(cascade, "\\0mnn", "The character with octal value 0mnn  (0 <= m <= 3, 0 <= n <= 7)");
        addMenuItem(cascade, "\\0xhh", "The character with hexadecimal value 0xhh");
        addMenuItem(cascade, "\\0xhhhh", "The character with hexadecimal value 0xhhhh");
        addMenuItem(cascade, "\\t", "The tab character ('	')");
        addMenuItem(cascade, "\\n", "The newline (line feed) character ('\\u000A')");
        addMenuItem(cascade, "\\r", "The carriage-return character ('\\u000D')");
        addMenuItem(cascade, "\\f", "The form-feed character ('\\u000C')");
        addMenuItem(cascade, "\\a", "The alert (bell) character ('\\u0007')");
        addMenuItem(cascade, "\\e", "The escape character ('')");
        addMenuItem(cascade, "\\cx", "The control character corresponding to x");
        cascade = addCascadeMenu(popupMenu, "Predefined character classes");
        addMenuItem(cascade, ".", "Any character (may or may not match line terminators)");
        addMenuItem(cascade, "\\d", "A digit: [0-9]");
        addMenuItem(cascade, "\\D", "A non-digit: [^0-9]");
        addMenuItem(cascade, "\\s", "A whitespace character: [ \\t\\n\\x0B\\f\\r]");
        addMenuItem(cascade, "\\S", "A non-whitespace character: [^\\s]");
        addMenuItem(cascade, "\\w", "A word character: [a-zA-Z_0-9]");
        addMenuItem(cascade, "\\W", "A non-word character: [^\\w]");
        cascade = addCascadeMenu(popupMenu, "Boundary matchers");
        addMenuItem(cascade, "^", "The beginning of a line");
        addMenuItem(cascade, "$", "The end of a line");
        addMenuItem(cascade, "\\b", "A word boundary");
        addMenuItem(cascade, "\\B", "A non-word boundary");
        addMenuItem(cascade, "\\A", "The beginning of the input");
        addMenuItem(cascade, "\\G", "The end of the previous match");
        addMenuItem(cascade, "\\Z", "The end of the input but for the final terminator, if any");
        addMenuItem(cascade, "\\z", "The end of the input");
        GridData regExGridData = new GridData();
        regExGridData.horizontalAlignment = GridData.FILL;
        regExGridData.grabExcessHorizontalSpace = true;
        fRegEx = new Text(group, SWT.BORDER);
        fRegEx.setLayoutData(regExGridData);
        fRegEx.setMenu(popupMenu);
        fRegEx.addKeyListener(this);
        GridData liveEvalGridData = new GridData();
        liveEvalGridData.horizontalAlignment = GridData.END;
        liveEvalGridData.grabExcessHorizontalSpace = false;
        Button liveEval = new Button(group, SWT.CHECK);
        liveEval.setLayoutData(liveEvalGridData);
        liveEval.setText(RegExViewPlugin.getResourceString("live-eval"));
        liveEval.setSelection(fLiveEval);
        liveEval.setData(ACTION_COMMAND, String.valueOf(LIVE_EVAL_ACTION_COMMAND));
        liveEval.addSelectionListener(this);
        GridData textGridData = new GridData();
        textGridData.horizontalAlignment = GridData.FILL;
        textGridData.verticalAlignment = GridData.FILL;
        textGridData.grabExcessHorizontalSpace = true;
        textGridData.grabExcessVerticalSpace = true;
        fText = new StyledText(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        fText.setLayoutData(textGridData);
        fText.addLineStyleListener(fLineStyler);
        fText.addVerifyListener(this);
        GridData resultGridData = new GridData();
        resultGridData.horizontalAlignment = GridData.FILL;
        resultGridData.verticalAlignment = GridData.FILL;
        resultGridData.grabExcessHorizontalSpace = true;
        resultGridData.grabExcessVerticalSpace = true;
        fResult = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        fResult.setEditable(false);
        fResult.setLayoutData(resultGridData);
        Button eval = new Button(parent, SWT.BORDER);
        eval.setText(RegExViewPlugin.getResourceString("evaluate"));
        eval.setData(ACTION_COMMAND, String.valueOf(EVALUATION_ACTION_COMMAND));
        eval.addSelectionListener(this);
    }

    /**
     * Adds a new cascade menu to the parent menu.
     * 
     * @param parent
     *            The parent menu.
     * @param text
     *            The menu text.
     * @return The menu added to the parent menu.
     */
    private Menu addCascadeMenu(final Menu parent, final String text) {
        Menu cascade = new Menu(parent.getShell(), SWT.DROP_DOWN);
        MenuItem cascadeMenuItem = new MenuItem(parent, SWT.CASCADE);
        cascadeMenuItem.setText(text);
        cascadeMenuItem.setMenu(cascade);
        return cascade;
    }

    /**
     * Adds a new sub-menu to the parent menu.
     * 
     * @param parent
     *            The parent menu.
     * @param text
     *            The menu text.
     * @return The menu added to the parent menu.
     */
    private MenuItem addMenuItem(final Menu parent, final String text) {
        MenuItem menuItem = new MenuItem(parent, SWT.CASCADE);
        menuItem.setText(text);
        return menuItem;
    }

    /**
     * Adds a new menu item to the parent menu.
     * 
     * @param parent
     *            The parent menu.
     * @param text
     *            The menu item text.
     * @param regExCommand
     *            The regular expression command.
     * @return The menu item added to the parent menu.
     */
    private MenuItem addMenuItem(final Menu parent, final String regExCommand, final String text) {
        MenuItem menuItem = new MenuItem(parent, SWT.PUSH);
        menuItem.setText(text);
        menuItem.setData(ACTION_COMMAND, String.valueOf(POPUP_MENUITEM_ACTION_COMMAND));
        menuItem.setData(REG_EX_COMMAND, regExCommand);
        menuItem.addSelectionListener(this);
        return menuItem;
    }

    /**
     * Evaluates the regular expression and updates the GUI.
     */
    private void evaluate() {
        colorizeText();
        updateResult();
    }

    /**
     * Colorizes the text.
     */
    private void colorizeText() {
        fLineStyler.setMatchForegroundColor(PreferenceConverter.getColor(RegExViewPlugin.getDefault().getPreferenceStore(), IPreferenceConstants.P_MATCH_FOREGROUND_COLOR));
        fLineStyler.setMatchBackgroundColor(PreferenceConverter.getColor(RegExViewPlugin.getDefault().getPreferenceStore(), IPreferenceConstants.P_MATCH_BACKGROUND_COLOR));
        fLineStyler.setNonMatchForegroundColor(PreferenceConverter.getColor(RegExViewPlugin.getDefault().getPreferenceStore(), IPreferenceConstants.P_NON_MATCH_FOREGROUND_COLOR));
        fLineStyler.setNonMatchBackgroundColor(PreferenceConverter.getColor(RegExViewPlugin.getDefault().getPreferenceStore(), IPreferenceConstants.P_NON_MATCH_BACKGROUND_COLOR));
        final String text = fText.getText();
        fLineStyler.performRegEx(fRegEx.getText(), text);
        Display display = fText.getDisplay();
        display.asyncExec(new Runnable() {

            public void run() {
                fText.setText(text);
            }
        });
    }

    /**
     * Updates the result label.
     */
    private void updateResult() {
        final String text = fText.getText();
        final Pattern pattern = Pattern.compile(fRegEx.getText());
        final Matcher matcher = pattern.matcher(text);
        final StringBuffer result = new StringBuffer();
        int start = 0;
        int end = 0;
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            result.append(text.substring(start, end));
            result.append(' ');
            result.append(start);
            result.append(' ');
            result.append(end);
            result.append('\n');
        }
        Display display = fText.getDisplay();
        display.asyncExec(new Runnable() {

            public void run() {
                fResult.setText(result.toString());
            }
        });
    }

    /**
     * Does nothing.
     * 
     * @param e
     *            An event containing information about the key press.
     */
    public void keyPressed(final KeyEvent e) {
    }

    /**
     * If live evaluation is turned on the text will be colorized.
     * 
     * @param e
     *            An event containing information about the key press.
     */
    public void keyReleased(final KeyEvent e) {
        if (fLiveEval || e.keyCode == SWT.CR) {
            evaluate();
        }
    }

    /**
     * Does nothing.
     * 
     * @param e
     *            An event containing information about the default selection.
     */
    public void widgetDefaultSelected(final SelectionEvent e) {
    }

    /**
     * Called when the evaluate button is pressed.
     * 
     * @param e
     *            An event containing information about the selection.
     */
    public void widgetSelected(final SelectionEvent e) {
        if (e.getSource() instanceof Button) {
            Button button = (Button) e.getSource();
            String data = (String) button.getData(ACTION_COMMAND);
            int actionCommand = Integer.parseInt(data);
            switch(actionCommand) {
                case LIVE_EVAL_ACTION_COMMAND:
                    fLiveEval = button.getSelection();
                    break;
                case EVALUATION_ACTION_COMMAND:
                    evaluate();
                    break;
                default:
                    break;
            }
        } else if (e.getSource() instanceof MenuItem) {
            MenuItem menuItem = (MenuItem) e.getSource();
            String data = (String) menuItem.getData(ACTION_COMMAND);
            int actionCommand = Integer.parseInt(data);
            switch(actionCommand) {
                case POPUP_MENUITEM_ACTION_COMMAND:
                    String regExCommand = (String) menuItem.getData(REG_EX_COMMAND);
                    StringBuffer buffer = new StringBuffer(fRegEx.getText());
                    buffer.append(regExCommand);
                    fRegEx.setText(buffer.toString());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Called when the text is edited. Will clear the colorization.
     * 
     * @param e
     *            An event containing information about the verify.
     */
    public void verifyText(final VerifyEvent e) {
        final String textToVerify = e.text;
        final String text = fText.getText();
        if (textToVerify.length() != text.length() || !textToVerify.equals(text)) {
            fLineStyler.reset();
        }
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        fRegEx.setFocus();
    }
}
