package ti.plato.ui.views.console.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import ti.mcore.u.PluginUtil;
import ti.plato.hook.IMenuHook;
import ti.plato.shared.types.RegExCombo;
import ti.plato.ui.views.console.IConsoleFactory;
import ti.plato.ui.views.console.IConsoleView;
import ti.plato.ui.views.console.IOConsole;
import ti.plato.ui.views.console.TextConsoleViewer;
import ti.plato.ui.views.console.WorkspaceSaveContainer;
import ti.plato.ui.views.console.constants.Constants;

public class EditFind implements IMenuHook {

    private int padding = 10;

    private Control labelFind = null;

    private RegExCombo comboFind = null;

    private Control labelResult = null;

    private Control editResult = null;

    private Control marginCanvasEditResult = null;

    private Control buttonFind = null;

    private Control buttonStopFind = null;

    private Control buttonClose = null;

    private Control groupDirection = null;

    private Control radioForward = null;

    private Control radioBackward = null;

    private Control groupOptions = null;

    private Control checkRegExp = null;

    private Control checkCaseSensitive = null;

    private int comboFiltersHeight = 21;

    private int comboVisibleItemCount = 30;

    private int comboItemCount = 60;

    private int buttonWidth = 70;

    private int checkBoxWidth = 110;

    private int groupHeight = 40;

    private Shell dialog = null;

    private int widthMin = 275;

    private int widthMax = -1;

    private int heightMin = 350;

    private int heightMax = -1;

    private IViewReference viewReference = null;

    private boolean nowFinding = false;

    private IConsoleFactory extensionConsoleFactory = null;

    private Canvas marginCanvasTmp = null;

    private static int widgetOffset = 0;

    private IOConsole console = null;

    private IDocument document = null;

    private int textOffset = 0;

    public EditFind() {
        IConsoleFactory[] extensions = (IConsoleFactory[]) PluginUtil.getExecutableExtensions(Constants.consoleFactoryExtensionPointID, IConsoleFactory.class);
        if (extensions.length == 0) {
            showMessage("points.length error " + extensions.length);
            return;
        }
        extensionConsoleFactory = extensions[0];
    }

    public void buildGotoLineDialog(Shell dialog) {
    }

    private void find() {
        stopFind();
        console = extensionConsoleFactory.getLastOpenedConsole();
        document = console.getDocument();
        WorkspaceSaveContainer.findCurrent = comboFind.getText();
        WorkspaceSaveContainer.directionForward = ((Button) radioForward).getSelection();
        WorkspaceSaveContainer.regularExpression = ((Button) checkRegExp).getSelection();
        WorkspaceSaveContainer.caseSensitive = ((Button) checkCaseSensitive).getSelection();
        if (WorkspaceSaveContainer.findCurrent.equals("")) {
            ((StyledText) editResult).setText("The string to look for is empty.");
            colorizeEdit(((StyledText) editResult));
            return;
        }
        boolean found = false;
        int findHistoryCount = WorkspaceSaveContainer.findHistoryList.size();
        int findHistoryIndex;
        for (findHistoryIndex = 0; findHistoryIndex < findHistoryCount; findHistoryIndex++) {
            String elt = (String) WorkspaceSaveContainer.findHistoryList.get(findHistoryIndex);
            if (elt.equals(WorkspaceSaveContainer.findCurrent)) {
                WorkspaceSaveContainer.findHistoryList.remove(findHistoryIndex);
                found = true;
                break;
            }
        }
        WorkspaceSaveContainer.findHistoryList.add(0, WorkspaceSaveContainer.findCurrent);
        if (!found && WorkspaceSaveContainer.findHistoryList.size() > comboItemCount) WorkspaceSaveContainer.findHistoryList.remove(WorkspaceSaveContainer.findHistoryList.size() - 1);
        findHistoryCount = WorkspaceSaveContainer.findHistoryList.size();
        String[] fields = new String[findHistoryCount];
        for (findHistoryIndex = 0; findHistoryIndex < findHistoryCount; findHistoryIndex++) {
            fields[findHistoryIndex] = (String) WorkspaceSaveContainer.findHistoryList.get(findHistoryIndex);
        }
        comboFind.setItems(fields);
        comboFind.setText(WorkspaceSaveContainer.findCurrent);
        comboFind.setSelection(new Point(0, WorkspaceSaveContainer.findCurrent.length()));
        comboFind.setFocus();
        nowFinding = find(onFindCompletedNotification, WorkspaceSaveContainer.findCurrent, WorkspaceSaveContainer.directionForward, WorkspaceSaveContainer.regularExpression, WorkspaceSaveContainer.caseSensitive);
        if (nowFinding) {
            PlatformUI.getWorkbench().getDisplay().getShells()[0].setCursor(new Cursor(PlatformUI.getWorkbench().getDisplay(), SWT.CURSOR_WAIT));
        }
    }

    public boolean find(Runnable onFindCompletedNotification, String findStr, boolean directionForward, boolean regularExpression, boolean caseSensitive) {
        TextConsoleViewer textConsoleViewer = null;
        try {
            IViewPart viewPart = (IViewPart) viewReference.getView(false);
            if (viewPart instanceof IConsoleView) {
                textConsoleViewer = ((IConsoleView) viewPart).getTextConsoleViewer();
                if (textConsoleViewer != null) {
                    widgetOffset = textConsoleViewer.getFindReplaceTarget().findAndSelect(widgetOffset, findStr, directionForward, caseSensitive, regularExpression);
                    if (widgetOffset == -1) {
                        widgetOffset = directionForward ? 0 : (document.getLength() - 1);
                        widgetOffset = textConsoleViewer.getFindReplaceTarget().findAndSelect(widgetOffset, findStr, directionForward, caseSensitive, regularExpression);
                    }
                    textOffset = widgetOffset;
                    onFindCompletedNotification.run();
                    if (directionForward) {
                        widgetOffset++;
                        if (widgetOffset == (document.getLength() - 1)) {
                            widgetOffset = 0;
                        }
                    } else {
                        widgetOffset--;
                        if (widgetOffset == 0) {
                            widgetOffset = (document.getLength() - 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in 'EditFind': " + e);
        }
        return true;
    }

    public void findNext() {
        stopFind();
        if (WorkspaceSaveContainer.findCurrent.equals("")) {
            return;
        }
        nowFinding = find(onFindCompletedNotification, WorkspaceSaveContainer.findCurrent, WorkspaceSaveContainer.directionForward, WorkspaceSaveContainer.regularExpression, WorkspaceSaveContainer.caseSensitive);
        if (nowFinding) PlatformUI.getWorkbench().getDisplay().getShells()[0].setCursor(new Cursor(PlatformUI.getWorkbench().getDisplay(), SWT.CURSOR_WAIT));
    }

    private void update() {
        buttonFind.setEnabled(!nowFinding);
        buttonStopFind.setEnabled(nowFinding);
        comboFind.setEnabled(!nowFinding);
        groupDirection.setEnabled(!nowFinding);
        radioForward.setEnabled(!nowFinding);
        radioBackward.setEnabled(!nowFinding);
        groupOptions.setEnabled(!nowFinding);
        checkRegExp.setEnabled(!nowFinding);
        checkCaseSensitive.setEnabled(!nowFinding);
    }

    private void stopFind() {
        if (!nowFinding) return;
        nowFinding = false;
        if (dialog != null) {
            ((StyledText) editResult).setText("Interrupted.");
            colorizeEdit(((StyledText) editResult));
            update();
            comboFind.setFocus();
        }
        PlatformUI.getWorkbench().getDisplay().getShells()[0].setCursor(new Cursor(PlatformUI.getWorkbench().getDisplay(), SWT.CURSOR_ARROW));
    }

    private void findPrevious() {
        stopFind();
        if (WorkspaceSaveContainer.findCurrent.equals("")) {
            return;
        }
        nowFinding = find(onFindCompletedNotification, WorkspaceSaveContainer.findCurrent, false, WorkspaceSaveContainer.regularExpression, WorkspaceSaveContainer.caseSensitive);
        if (nowFinding) PlatformUI.getWorkbench().getDisplay().getShells()[0].setCursor(new Cursor(PlatformUI.getWorkbench().getDisplay(), SWT.CURSOR_WAIT));
    }

    public int[] getFindDialogRange() {
        return null;
    }

    public int[] getGotoLineDialogRange() {
        return null;
    }

    public String getId() {
        return "ti.plato.ui.views.console.ConsoleView";
    }

    public boolean isCopySupported() {
        return false;
    }

    public boolean isFindSupported() {
        return true;
    }

    private Control addGroup(Control controlTop, String text) {
        Group group = new Group(dialog, 0);
        group.setLayout(new FormLayout());
        group.setText(text);
        FormData formDataGroup = new FormData();
        formDataGroup.top = new FormAttachment(controlTop, padding, SWT.BOTTOM);
        formDataGroup.bottom = new FormAttachment(controlTop, padding + groupHeight, SWT.BOTTOM);
        formDataGroup.left = new FormAttachment(0, padding);
        formDataGroup.right = new FormAttachment(100, -padding);
        group.setLayoutData(formDataGroup);
        return group;
    }

    private Control addRadio(Group parent, String text, Control controlLeft, Control controlRight, Control controlTop, boolean leftAlign, boolean selected) {
        Button button = new Button(parent, SWT.RADIO);
        FormData formDataButton = new FormData();
        if (controlRight != null) {
            formDataButton.left = new FormAttachment(controlRight, -buttonWidth, SWT.RIGHT);
            formDataButton.right = new FormAttachment(controlRight, 0, SWT.RIGHT);
        } else if (controlLeft != null) {
            formDataButton.left = new FormAttachment(controlLeft, padding, SWT.RIGHT);
            formDataButton.right = new FormAttachment(controlLeft, padding + buttonWidth, SWT.RIGHT);
        } else {
            if (leftAlign) {
                formDataButton.left = new FormAttachment(0, padding);
                formDataButton.right = new FormAttachment(0, padding + buttonWidth);
            } else {
                formDataButton.left = new FormAttachment(100, -padding - buttonWidth);
                formDataButton.right = new FormAttachment(100, -padding);
            }
        }
        if (controlTop != null) {
            formDataButton.top = new FormAttachment(controlTop, padding, SWT.BOTTOM);
            formDataButton.bottom = new FormAttachment(controlTop, 21 + padding, SWT.BOTTOM);
        } else {
            formDataButton.top = new FormAttachment(0, 0);
            formDataButton.bottom = new FormAttachment(0, 21);
        }
        button.setLayoutData(formDataButton);
        if (selected) button.setSelection(true);
        button.setText(text);
        return button;
    }

    private Control addCheck(int width, Group parent, String text, Control controlLeft, Control controlRight, Control controlTop, boolean leftAlign, boolean selected) {
        Button button = new Button(parent, SWT.CHECK);
        FormData formDataButton = new FormData();
        if (controlRight != null) {
            formDataButton.left = new FormAttachment(controlRight, -width, SWT.RIGHT);
            formDataButton.right = new FormAttachment(controlRight, 0, SWT.RIGHT);
        } else if (controlLeft != null) {
            formDataButton.left = new FormAttachment(controlLeft, padding, SWT.RIGHT);
            formDataButton.right = new FormAttachment(controlLeft, padding + width, SWT.RIGHT);
        } else {
            if (leftAlign) {
                formDataButton.left = new FormAttachment(0, padding);
                formDataButton.right = new FormAttachment(0, padding + width);
            } else {
                formDataButton.left = new FormAttachment(100, -padding - width);
                formDataButton.right = new FormAttachment(100, -padding);
            }
        }
        if (controlTop != null) {
            formDataButton.top = new FormAttachment(controlTop, padding, SWT.BOTTOM);
            formDataButton.bottom = new FormAttachment(controlTop, 21 + padding, SWT.BOTTOM);
        } else {
            formDataButton.top = new FormAttachment(0, 0);
            formDataButton.bottom = new FormAttachment(0, 21);
        }
        button.setLayoutData(formDataButton);
        if (selected) button.setSelection(true);
        button.setText(text);
        return button;
    }

    private Runnable onFindCompletedNotification = new Runnable() {

        public void run() {
            onFindCompleted();
        }
    };

    private void onFindCompleted() {
        String resultLine = null;
        try {
            int lineOffset = document.getLineOfOffset(textOffset);
            resultLine = document.get(document.getLineOffset(lineOffset), document.getLineLength(lineOffset));
        } catch (Exception e) {
            System.out.println("Exception occured in Console Find Dialog: " + e);
        }
        if (dialog != null) {
            ((StyledText) editResult).setText(resultLine);
            colorizeEdit(((StyledText) editResult));
            update();
            comboFind.setFocus();
        }
        PlatformUI.getWorkbench().getDisplay().getShells()[0].setCursor(new Cursor(PlatformUI.getWorkbench().getDisplay(), SWT.CURSOR_ARROW));
    }

    private void colorizeEdit(StyledText edit) {
        String findResult = edit.getText();
        final Color COLOR_BLUE = dialog.getDisplay().getSystemColor(SWT.COLOR_BLUE);
        int start = 0;
        int index = findResult.indexOf("\n");
        while (index != -1) {
            String subString = findResult.substring(start, index);
            if (subString.length() != 0 && subString.startsWith("(") && subString.endsWith(")")) {
                StyleRange style = new StyleRange();
                style.start = start;
                style.length = subString.length();
                style.underline = true;
                ((StyledText) editResult).setStyleRange(style);
            } else {
                StyleRange style = new StyleRange();
                style.start = start;
                style.length = subString.length();
                style.foreground = COLOR_BLUE;
                ((StyledText) editResult).setStyleRange(style);
            }
            start = index + 1;
            index = findResult.indexOf("\n", index + 1);
        }
        String subString = findResult.substring(start).replace("\n", "");
        if (subString.length() != 0 && subString.startsWith("(") && subString.endsWith(")")) {
            StyleRange style = new StyleRange();
            style.start = start;
            style.length = subString.length();
            style.underline = true;
            ((StyledText) editResult).setStyleRange(style);
        } else {
            StyleRange style = new StyleRange();
            style.start = start;
            style.length = subString.length();
            style.foreground = COLOR_BLUE;
            ((StyledText) editResult).setStyleRange(style);
        }
        if (!WorkspaceSaveContainer.findCurrent.equals("")) {
            final Color COLOR_RED = dialog.getDisplay().getSystemColor(SWT.COLOR_RED);
            if (WorkspaceSaveContainer.regularExpression) {
                Pattern pattern = null;
                try {
                    if (WorkspaceSaveContainer.caseSensitive) pattern = Pattern.compile(WorkspaceSaveContainer.findCurrent); else pattern = Pattern.compile(WorkspaceSaveContainer.findCurrent, Pattern.CASE_INSENSITIVE);
                } catch (PatternSyntaxException e) {
                    return;
                }
                String[] findResultList = findResult.split("\n");
                int findResultListCount = findResultList.length;
                int findResultListIndex;
                int offset = 0;
                for (findResultListIndex = 0; findResultListIndex < findResultListCount; findResultListIndex++) {
                    Matcher matcher = pattern.matcher(findResultList[findResultListIndex]);
                    while (matcher.find()) {
                        int startIndex = matcher.start();
                        int endIndex = matcher.end();
                        StyleRange style = new StyleRange();
                        style.start = startIndex + offset;
                        style.length = endIndex - startIndex;
                        style.fontStyle = SWT.BOLD;
                        style.foreground = COLOR_RED;
                        ((StyledText) editResult).setStyleRange(style);
                    }
                    offset += findResultList[findResultListIndex].length() + 1;
                }
            } else {
                String findCurrentLowerCase = WorkspaceSaveContainer.findCurrent.toLowerCase();
                String findResultLowerCase = findResult.toLowerCase();
                if (WorkspaceSaveContainer.caseSensitive) index = findResult.indexOf(WorkspaceSaveContainer.findCurrent); else index = findResultLowerCase.indexOf(findCurrentLowerCase);
                while (index != -1) {
                    StyleRange style = new StyleRange();
                    style.start = index;
                    style.length = WorkspaceSaveContainer.findCurrent.length();
                    style.fontStyle = SWT.BOLD;
                    style.foreground = COLOR_RED;
                    ((StyledText) editResult).setStyleRange(style);
                    if (WorkspaceSaveContainer.caseSensitive) index = findResult.indexOf(WorkspaceSaveContainer.findCurrent, index + 1); else index = findResultLowerCase.indexOf(findCurrentLowerCase, index + 1);
                }
            }
        }
    }

    public boolean isGotoLineSupported() {
        return false;
    }

    public void runCopy() {
    }

    public void setViewReference(IViewReference viewReference) {
        this.viewReference = viewReference;
        stopFind();
        if (dialog != null) ((StyledText) editResult).setText("");
    }

    public void runFindNext() {
        findNext();
    }

    public void runFindPrevious() {
        findPrevious();
    }

    private Control addLabel(Control controlTop, String text) {
        Label label = new Label(dialog, SWT.LEFT);
        FormData formDatalabel = new FormData();
        if (controlTop == null) {
            formDatalabel.top = new FormAttachment(0, padding);
            formDatalabel.bottom = new FormAttachment(0, padding + 16);
        } else {
            formDatalabel.top = new FormAttachment(controlTop, padding, SWT.BOTTOM);
            formDatalabel.bottom = new FormAttachment(controlTop, padding + 16, SWT.BOTTOM);
        }
        formDatalabel.left = new FormAttachment(0, padding);
        formDatalabel.right = new FormAttachment(100, -padding);
        label.setLayoutData(formDatalabel);
        label.setText(text);
        return label;
    }

    private Control addCombo(Control controlTop, List fieldsArray, String findCurrent, boolean editable) {
        int fieldsArrayCount = fieldsArray.size();
        String[] fields = new String[fieldsArrayCount];
        int fieldsArrayIndex;
        for (fieldsArrayIndex = 0; fieldsArrayIndex < fieldsArrayCount; fieldsArrayIndex++) {
            fields[fieldsArrayIndex] = (String) fieldsArray.get(fieldsArrayIndex);
        }
        int style = SWT.DROP_DOWN;
        if (!editable) style |= SWT.READ_ONLY;
        Combo comboFilters = new Combo(dialog, style);
        comboFilters.setItems(fields);
        FormData formDataComboFilters = new FormData();
        formDataComboFilters.top = new FormAttachment(controlTop, 0, SWT.BOTTOM);
        formDataComboFilters.bottom = new FormAttachment(controlTop, 0 + comboFiltersHeight, SWT.BOTTOM);
        formDataComboFilters.left = new FormAttachment(0, padding);
        formDataComboFilters.right = new FormAttachment(100, -padding);
        comboFilters.setLayoutData(formDataComboFilters);
        comboFilters.setText(findCurrent);
        if (editable) comboFilters.setSelection(new Point(0, findCurrent.length()));
        comboFilters.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        comboFilters.setVisibleItemCount(comboVisibleItemCount);
        return comboFilters;
    }

    private Control addButton(String text, Control controlLeft, Control controlRight, Control controlTop) {
        Button button = new Button(dialog, SWT.PUSH);
        FormData formDataButton = new FormData();
        if (controlRight != null && controlLeft != null) {
            formDataButton.left = new FormAttachment(50, -buttonWidth / 2);
            formDataButton.right = new FormAttachment(50, buttonWidth / 2);
        } else if (controlRight != null) {
            formDataButton.left = new FormAttachment(controlRight, -buttonWidth, SWT.RIGHT);
            formDataButton.right = new FormAttachment(controlRight, 0, SWT.RIGHT);
        } else if (controlLeft != null) {
            formDataButton.left = new FormAttachment(controlLeft, padding, SWT.RIGHT);
            formDataButton.right = new FormAttachment(controlLeft, padding + buttonWidth, SWT.RIGHT);
        } else {
            formDataButton.left = new FormAttachment(0, padding);
            formDataButton.right = new FormAttachment(0, padding + buttonWidth);
        }
        if (controlTop != null) {
            formDataButton.top = new FormAttachment(controlTop, padding, SWT.BOTTOM);
            formDataButton.bottom = new FormAttachment(controlTop, 21 + padding, SWT.BOTTOM);
        } else {
            formDataButton.top = new FormAttachment(100, -padding - 21);
            formDataButton.bottom = new FormAttachment(100, -padding);
        }
        button.setLayoutData(formDataButton);
        button.setText(text);
        return button;
    }

    private Control addEdit(Control controlTop, Control controlBottom, String EditText) {
        final Color COLOR_LIGHT_GRAY = dialog.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        marginCanvasTmp = new Canvas(dialog, 0);
        marginCanvasTmp.setLayout(new FormLayout());
        marginCanvasTmp.setBackground(COLOR_LIGHT_GRAY);
        FormData formDataCanvas = new FormData();
        formDataCanvas.top = new FormAttachment(controlTop, 0, SWT.BOTTOM);
        formDataCanvas.bottom = new FormAttachment(controlBottom, -padding, SWT.TOP);
        formDataCanvas.left = new FormAttachment(0, padding);
        formDataCanvas.right = new FormAttachment(100, -padding);
        marginCanvasTmp.setLayoutData(formDataCanvas);
        final Color BORDER = dialog.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
        marginCanvasTmp.addListener(SWT.Paint, new Listener() {

            public void handleEvent(Event event) {
                Rectangle rect = marginCanvasTmp.getClientArea();
                rect.height--;
                rect.width--;
                event.gc.setForeground(BORDER);
                event.gc.drawRectangle(rect);
            }
        });
        StyledText textDescription = new StyledText(marginCanvasTmp, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
        textDescription.setBackground(COLOR_LIGHT_GRAY);
        FormData formDataTextFilter = new FormData();
        formDataTextFilter.top = new FormAttachment(0, 2);
        formDataTextFilter.bottom = new FormAttachment(100, -2);
        formDataTextFilter.left = new FormAttachment(0, 6);
        formDataTextFilter.right = new FormAttachment(100, -2);
        textDescription.setLayoutData(formDataTextFilter);
        textDescription.setText(EditText);
        return textDescription;
    }

    private RegExCombo addRegExCombo(Control controlTop, ArrayList fieldsArray, String findCurrent, boolean editable) {
        int fieldsArrayCount = fieldsArray.size();
        String[] fields = new String[fieldsArrayCount];
        int fieldsArrayIndex;
        for (fieldsArrayIndex = 0; fieldsArrayIndex < fieldsArrayCount; fieldsArrayIndex++) {
            fields[fieldsArrayIndex] = (String) fieldsArray.get(fieldsArrayIndex);
        }
        int style = SWT.DROP_DOWN;
        if (!editable) style |= SWT.READ_ONLY;
        RegExCombo comboFilters = new RegExCombo(dialog, style);
        comboFilters.setItems(fields);
        FormData formDataComboFilters = new FormData();
        formDataComboFilters.top = new FormAttachment(controlTop, 0, SWT.BOTTOM);
        formDataComboFilters.bottom = new FormAttachment(controlTop, 0 + comboFiltersHeight, SWT.BOTTOM);
        formDataComboFilters.left = new FormAttachment(0, padding);
        formDataComboFilters.right = new FormAttachment(100, -padding);
        comboFilters.setLayoutData(formDataComboFilters);
        comboFilters.setText(findCurrent);
        if (editable) comboFilters.setSelection(new Point(0, findCurrent.length()));
        comboFilters.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        comboFilters.setVisibleItemCount(comboVisibleItemCount);
        return comboFilters;
    }

    public void buildFindDialog(Shell dialogArg) {
        widgetOffset = 0;
        stopFind();
        dialog = dialogArg;
        labelFind = addLabel(null, "Find:");
        comboFind = addRegExCombo(labelFind, (ArrayList) WorkspaceSaveContainer.findHistoryList, WorkspaceSaveContainer.findCurrent, true);
        comboFind.setContentAssistsEnablement(WorkspaceSaveContainer.regularExpression);
        groupDirection = addGroup(comboFind.getLayoutControl(), "Direction");
        radioForward = addRadio((Group) groupDirection, "Forward", null, null, null, true, WorkspaceSaveContainer.directionForward);
        radioBackward = addRadio((Group) groupDirection, "Backward", null, null, null, false, !(WorkspaceSaveContainer.directionForward));
        groupOptions = addGroup(groupDirection, "Options");
        checkCaseSensitive = addCheck(checkBoxWidth, (Group) groupOptions, "Case Sensitive", null, null, null, true, WorkspaceSaveContainer.caseSensitive);
        checkRegExp = addCheck(checkBoxWidth, (Group) groupOptions, "Regular Expression", null, null, null, false, WorkspaceSaveContainer.regularExpression);
        ((Button) checkRegExp).addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                comboFind.setContentAssistsEnablement(((Button) checkRegExp).getSelection());
            }

            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }
        });
        labelResult = addLabel(groupOptions, "Result:");
        buttonFind = addButton("Find", null, null, null);
        buttonClose = addButton("Close", null, comboFind.getLayoutControl(), null);
        buttonStopFind = addButton("Stop Find", buttonFind, buttonClose, null);
        editResult = addEdit(labelResult, buttonClose, "");
        marginCanvasEditResult = marginCanvasTmp;
        update();
        dialog.addShellListener(new ShellListener() {

            public void shellActivated(ShellEvent arg0) {
            }

            public void shellClosed(ShellEvent arg0) {
                dialog = null;
                stopFind();
            }

            public void shellDeactivated(ShellEvent arg0) {
            }

            public void shellDeiconified(ShellEvent arg0) {
            }

            public void shellIconified(ShellEvent arg0) {
            }
        });
        ((Button) buttonClose).addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                dialog.close();
            }
        });
        ((Button) buttonFind).addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                find();
            }
        });
        ((Button) buttonStopFind).addSelectionListener(new SelectionListener() {

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                stopFind();
            }
        });
        comboFind.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent arg0) {
                if (!arg0.doit) return;
                if (arg0.keyCode == SWT.CR || arg0.keyCode == SWT.KEYPAD_CR) {
                    find();
                }
            }

            public void keyReleased(KeyEvent arg0) {
            }
        });
    }

    public void setFindVisible(boolean visible) {
        labelFind.setVisible(visible);
        comboFind.setVisible(visible);
        groupDirection.setVisible(visible);
        radioForward.setVisible(visible);
        radioBackward.setVisible(visible);
        groupOptions.setVisible(visible);
        checkRegExp.setVisible(visible);
        checkCaseSensitive.setVisible(visible);
        labelResult.setVisible(visible);
        editResult.setVisible(visible);
        marginCanvasEditResult.setVisible(visible);
        buttonFind.setVisible(visible);
        buttonStopFind.setVisible(visible);
        buttonClose.setVisible(visible);
        if (visible) {
            comboFind.setFocus();
            int tmpWidth = dialog.getSize().x;
            int tmpHeight = dialog.getSize().y;
            if (widthMin != -1 && tmpWidth < widthMin) tmpWidth = widthMin;
            if (widthMax != -1 && tmpWidth > widthMax) tmpWidth = widthMax;
            if (heightMin != -1 && tmpHeight < heightMin) tmpHeight = heightMin;
            if (heightMax != -1 && tmpHeight > heightMax) tmpHeight = heightMax;
            dialog.setSize(tmpWidth, tmpHeight);
        }
    }

    public void setGotoLineVisible(boolean visible) {
    }

    private void showMessage(final String message) {
        ti.mcore.Environment.getEnvironment().showInfoMessage(message);
    }

    public boolean isFindCommentSupported() {
        return false;
    }

    public void runFindNextComment() {
    }

    public void runFindPreComment() {
    }

    public void setEditorReference(IEditorReference editorReference) {
    }

    public boolean isCutSupported() {
        return false;
    }

    public void runCut() {
    }

    public boolean isPasteSupported() {
        return false;
    }

    public void runPaste() {
    }

    public boolean isDeleteSupported() {
        return false;
    }

    public boolean isRedoSupported() {
        return false;
    }

    public boolean isSelectAllSupported() {
        return false;
    }

    public boolean isUndoSupported() {
        return false;
    }

    public void runDelete() {
    }

    public void runRedo() {
    }

    public void runSelectAll() {
    }

    public void runUndo() {
    }
}
