package ti.plato.ui.views.logger.menu;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import ti.plato.components.logger.constants.LoggerConstants;
import ti.plato.components.logger.feedback.LoggerMain;
import ti.plato.hook.MenuHookAdapter;
import ti.plato.shared.types.RegExCombo;
import ti.plato.ui.views.logger.LoggerPlugin;
import ti.plato.ui.views.logger.WorkspaceSaveContainerGlobal;
import ti.plato.ui.views.logger.constants.Constants;
import ti.plato.ui.views.logger.views.LoggerView;

public class EditFind extends MenuHookAdapter {

    public String getId() {
        return LoggerView.class.getName();
    }

    public boolean isFindSupported() {
        return true;
    }

    private final WorkspaceSaveContainerGlobal state = LoggerPlugin.getDefault().getWorkspaceSaveContainerGlobal();

    public void buildFindDialog(Shell dialogArg) {
        stopFind();
        dialog = dialogArg;
        if (columnNames.size() == 0) getColumnInfo();
        labelFind = addLabel(null, "Find:");
        comboFind = addRegExCombo(labelFind, state.findHistoryList, state.findCurrent, true);
        comboFind.setContentAssistsEnablement(state.regularExpression);
        labelColumn = addLabel(comboFind.getLayoutControl(), "Column:");
        comboColumn = addCombo(labelColumn, columnNames, (String) columnNames.get(state.columnCurrent), false);
        groupDirection = addGroup(comboColumn, "Direction");
        radioForward = addRadio((Group) groupDirection, "Forward", null, null, null, true, state.directionForward);
        radioBackward = addRadio((Group) groupDirection, "Backward", null, null, null, false, !state.directionForward);
        groupOptions = addGroup(groupDirection, "Options");
        checkCaseSensitive = addCheck(checkBoxWidth, (Group) groupOptions, "Case Sensitive", null, null, null, true, state.caseSensitive);
        checkRegExp = addCheck(checkBoxWidth, (Group) groupOptions, "Regular Expression", null, null, null, false, state.regularExpression);
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
        ((Combo) comboColumn).addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent arg0) {
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
        labelColumn.setVisible(visible);
        comboColumn.setVisible(visible);
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

    public int[] getFindDialogRange() {
        return new int[] { widthMin, widthMax, heightMin, heightMax };
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

    private int padding = 10;

    private Control labelFind = null;

    private RegExCombo comboFind = null;

    private Control labelColumn = null;

    private Control comboColumn = null;

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

    private ArrayList columnNames = new ArrayList();

    private boolean nowFinding = false;

    private static final LoggerMain extensionFeedback = LoggerMain.getDefault();

    private Canvas marginCanvasTmp = null;

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

    private Control addCombo(Control controlTop, ArrayList fieldsArray, String findCurrent, boolean editable) {
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

    private void find() {
        stopFind();
        state.findCurrent = comboFind.getText();
        state.columnCurrent = ((Combo) comboColumn).getSelectionIndex();
        state.directionForward = ((Button) radioForward).getSelection();
        state.regularExpression = ((Button) checkRegExp).getSelection();
        state.caseSensitive = ((Button) checkCaseSensitive).getSelection();
        if (state.findCurrent.equals("")) {
            ((StyledText) editResult).setText("The string to look for is empty.");
            colorizeEdit(((StyledText) editResult));
            return;
        }
        boolean found = false;
        int findHistoryCount = state.findHistoryList.size();
        int findHistoryIndex;
        for (findHistoryIndex = 0; findHistoryIndex < findHistoryCount; findHistoryIndex++) {
            String elt = (String) state.findHistoryList.get(findHistoryIndex);
            if (elt.equals(state.findCurrent)) {
                state.findHistoryList.remove(findHistoryIndex);
                found = true;
                break;
            }
        }
        state.findHistoryList.add(0, state.findCurrent);
        if (!found && state.findHistoryList.size() > comboItemCount) state.findHistoryList.remove(state.findHistoryList.size() - 1);
        findHistoryCount = state.findHistoryList.size();
        String[] fields = new String[findHistoryCount];
        for (findHistoryIndex = 0; findHistoryIndex < findHistoryCount; findHistoryIndex++) {
            fields[findHistoryIndex] = (String) state.findHistoryList.get(findHistoryIndex);
        }
        comboFind.setItems(fields);
        comboFind.setText(state.findCurrent);
        comboFind.setSelection(new Point(0, state.findCurrent.length()));
        comboFind.setFocus();
        LoggerView activeLoggerView = (LoggerView) viewReference.getView(false);
        StringBuffer findResultStr = new StringBuffer();
        nowFinding = activeLoggerView.find(findResultStr, onFindCompletedNotification, state.findCurrent, state.columnCurrent - 1, state.directionForward, state.regularExpression, state.caseSensitive);
        if (!nowFinding) {
            ((StyledText) editResult).setText(findResultStr.toString());
            colorizeEdit(((StyledText) editResult));
            update();
        } else {
            ((StyledText) editResult).setText("Now finding...");
            colorizeEdit(((StyledText) editResult));
            update();
            buttonStopFind.setFocus();
        }
    }

    private void stopFind() {
        if (!nowFinding) return;
        extensionFeedback.stopFind();
        nowFinding = false;
        if (dialog != null) {
            ((StyledText) editResult).setText("Interrupted.");
            colorizeEdit(((StyledText) editResult));
            update();
            comboFind.setFocus();
        }
        PlatformUI.getWorkbench().getDisplay().getShells()[0].setCursor(new Cursor(PlatformUI.getWorkbench().getDisplay(), SWT.CURSOR_ARROW));
    }

    private void update() {
        buttonFind.setEnabled(!nowFinding);
        buttonStopFind.setEnabled(nowFinding);
        comboFind.setEnabled(!nowFinding);
        comboColumn.setEnabled(!nowFinding);
        groupDirection.setEnabled(!nowFinding);
        radioForward.setEnabled(!nowFinding);
        radioBackward.setEnabled(!nowFinding);
        groupOptions.setEnabled(!nowFinding);
        checkRegExp.setEnabled(!nowFinding);
        checkCaseSensitive.setEnabled(!nowFinding);
    }

    private void findNext() {
        stopFind();
        if (state.findCurrent.equals("")) {
            return;
        }
        LoggerView activeLoggerView = (LoggerView) viewReference.getView(false);
        nowFinding = activeLoggerView.find(null, onFindCompletedNotification, state.findCurrent, state.columnCurrent - 1, true, state.regularExpression, state.caseSensitive);
        if (nowFinding) PlatformUI.getWorkbench().getDisplay().getShells()[0].setCursor(new Cursor(PlatformUI.getWorkbench().getDisplay(), SWT.CURSOR_WAIT));
    }

    private void findPrevious() {
        stopFind();
        if (state.findCurrent.equals("")) {
            return;
        }
        LoggerView activeLoggerView = (LoggerView) viewReference.getView(false);
        nowFinding = activeLoggerView.find(null, onFindCompletedNotification, state.findCurrent, state.columnCurrent - 1, false, state.regularExpression, state.caseSensitive);
        if (nowFinding) PlatformUI.getWorkbench().getDisplay().getShells()[0].setCursor(new Cursor(PlatformUI.getWorkbench().getDisplay(), SWT.CURSOR_WAIT));
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
        if (!state.findCurrent.equals("")) {
            final Color COLOR_RED = dialog.getDisplay().getSystemColor(SWT.COLOR_RED);
            if (state.regularExpression) {
                Pattern pattern = null;
                try {
                    if (state.caseSensitive) pattern = Pattern.compile(state.findCurrent); else pattern = Pattern.compile(state.findCurrent, Pattern.CASE_INSENSITIVE);
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
                String findCurrentLowerCase = state.findCurrent.toLowerCase();
                String findResultLowerCase = findResult.toLowerCase();
                if (state.caseSensitive) index = findResult.indexOf(state.findCurrent); else index = findResultLowerCase.indexOf(findCurrentLowerCase);
                while (index != -1) {
                    StyleRange style = new StyleRange();
                    style.start = index;
                    style.length = state.findCurrent.length();
                    style.fontStyle = SWT.BOLD;
                    style.foreground = COLOR_RED;
                    ((StyledText) editResult).setStyleRange(style);
                    if (state.caseSensitive) index = findResult.indexOf(state.findCurrent, index + 1); else index = findResultLowerCase.indexOf(findCurrentLowerCase, index + 1);
                }
            }
        }
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

    private void getColumnInfo() {
        columnNames.add(Constants.allColumns);
        for (String name : LoggerConstants.COLUMN_NAMES) columnNames.add(name);
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
        LoggerView activeLoggerView = (LoggerView) viewReference.getView(false);
        String findResult = activeLoggerView.getFindResult();
        nowFinding = false;
        if (dialog != null) {
            ((StyledText) editResult).setText(findResult);
            colorizeEdit(((StyledText) editResult));
            update();
            comboFind.setFocus();
        }
        PlatformUI.getWorkbench().getDisplay().getShells()[0].setCursor(new Cursor(PlatformUI.getWorkbench().getDisplay(), SWT.CURSOR_ARROW));
    }

    public EditFind() {
    }

    public boolean isGotoLineSupported() {
        return false;
    }

    public void buildGotoLineDialog(Shell dialog) {
    }

    public void setGotoLineVisible(boolean visible) {
    }

    public int[] getGotoLineDialogRange() {
        return null;
    }

    public boolean isCopySupported() {
        return false;
    }

    public void runCopy() {
    }
}
