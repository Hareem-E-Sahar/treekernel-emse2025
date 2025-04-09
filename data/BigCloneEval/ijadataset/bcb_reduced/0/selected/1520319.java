package org.j2eespider.build.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MessageDetailDialog extends org.eclipse.jface.dialogs.MessageDialog {

    private String messageDetail;

    private StyledText styledText;

    private Map<String, StyleRange> styles;

    private Control detailArea;

    private int detailButtonID = 1;

    private boolean enableDetail = false;

    private Composite compositeDetail;

    public MessageDetailDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage, String dialogMessageDetail, Map<String, StyleRange> dialogStyles, int dialogImageType, String[] dialogButtonLabels, int defaultIndex, boolean dialogEnableDetail) {
        super(parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
        this.messageDetail = dialogMessageDetail;
        this.styles = dialogStyles;
        this.enableDetail = dialogEnableDetail;
    }

    /**
     * Convenience method to open a standard error dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     */
    public static void openError(Shell parent, String title, String message, String messageDetail, Map<String, StyleRange> styles, boolean dialogEnableDetail) {
        MessageDetailDialog dialog = new MessageDetailDialog(parent, title, null, message, messageDetail, styles, ERROR, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.SHOW_DETAILS_LABEL }, 0, dialogEnableDetail);
        dialog.open();
        return;
    }

    /**
     * Convenience method to open a standard information dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     */
    public static void openInformation(Shell parent, String title, String message, String messageDetail, Map<String, StyleRange> styles, boolean dialogEnableDetail) {
        MessageDetailDialog dialog = new MessageDetailDialog(parent, title, null, message, messageDetail, styles, INFORMATION, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.SHOW_DETAILS_LABEL }, 0, dialogEnableDetail);
        dialog.open();
        return;
    }

    /**
     * Convenience method to open a standard warning dialog.
     * 
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the dialog's title, or <code>null</code> if none
     * @param message
     *            the message
     */
    public static void openWarning(Shell parent, String title, String message, String messageDetail, Map<String, StyleRange> styles, boolean dialogEnableDetail) {
        MessageDetailDialog dialog = new MessageDetailDialog(parent, title, null, message, messageDetail, styles, WARNING, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.SHOW_DETAILS_LABEL }, 0, dialogEnableDetail);
        dialog.open();
        return;
    }

    /**
	 * Create the dialog area and the button bar for the receiver.
	 * 
	 * @param parent
	 */
    protected void createDialogAndButtonArea(Composite parent) {
        dialogArea = createDialogArea(parent);
        buttonBar = createButtonBar(parent);
        applyDialogFont(parent);
        if (isEnableDetail()) {
            detailArea = createDetailArea(parent);
            setEnableDetail(true);
        } else {
            if (compositeDetail != null) {
                compositeDetail.dispose();
            }
            setEnableDetail(false);
        }
    }

    private Control createDetailArea(Composite parent) {
        compositeDetail = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        compositeDetail.setLayout(layout);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        compositeDetail.setLayoutData(data);
        createStyledText(compositeDetail);
        return parent;
    }

    private void createStyledText(Composite composite) {
        styledText = new StyledText(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        styledText.setText(messageDetail);
        styledText.setEditable(false);
        styledText.setBackground(new Color(Display.getDefault(), 255, 255, 255));
        styledText.setFont(new Font(Display.getDefault(), "Courier New", 8, SWT.NORMAL));
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.widthHint = 200;
        layoutData.heightHint = 110;
        styledText.setLayoutData(layoutData);
        if (styles != null && styles.size() > 0) {
            setStyleRange(styles);
        }
    }

    public void setStyleRange(Map<String, StyleRange> styles) {
        List<StyleRange> finalStyles = new ArrayList<StyleRange>();
        for (String str : styles.keySet()) {
            StyleRange styleRange = styles.get(str);
            if (styleRange.length == 0) {
                Pattern patternVariable = Pattern.compile(str);
                Matcher matcher = patternVariable.matcher(messageDetail);
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    int length = end - start;
                    StyleRange newStyleRange = new StyleRange(start, length, styleRange.foreground, styleRange.background, styleRange.fontStyle);
                    finalStyles.add(newStyleRange);
                }
            } else {
                finalStyles.add(styleRange);
            }
        }
        for (StyleRange styleRange : finalStyles) {
            styledText.setStyleRange(styleRange);
        }
    }

    private void toggleDetailsArea() {
        Point windowSize = getShell().getSize();
        Point oldSize = getContents().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (isEnableDetail()) {
            if (compositeDetail != null) {
                compositeDetail.dispose();
            }
            setEnableDetail(false);
        } else {
            detailArea = createDetailArea((Composite) getContents());
            setEnableDetail(true);
        }
        Point newSize = getContents().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == detailButtonID) {
            toggleDetailsArea();
        } else {
            setReturnCode(buttonId);
            close();
        }
    }

    public boolean isEnableDetail() {
        return enableDetail;
    }

    public void setEnableDetail(boolean enableDetail) {
        if (enableDetail) {
            getButton(detailButtonID).setText(IDialogConstants.HIDE_DETAILS_LABEL);
        } else {
            getButton(detailButtonID).setText(IDialogConstants.SHOW_DETAILS_LABEL);
        }
        this.enableDetail = enableDetail;
    }
}
