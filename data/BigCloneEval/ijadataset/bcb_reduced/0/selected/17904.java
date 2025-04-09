package com.eminsenay.FacebookContactsForPicasa.UI;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseEvent;

public class AboutDialog extends Dialog {

    protected Object result;

    protected Shell shlFacebookContactsFor;

    private Label lblHomepage;

    private Display display;

    /**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
    public AboutDialog(Shell parent, int style) {
        super(parent, style);
        setText("SWT Dialog");
    }

    /**
	 * Open the dialog.
	 * @return the result
	 */
    public Object open() {
        createContents();
        Rectangle pDisplayBounds = shlFacebookContactsFor.getParent().getBounds();
        Rectangle dialogBounds = shlFacebookContactsFor.getBounds();
        int nLeft = pDisplayBounds.x + pDisplayBounds.width / 2 - dialogBounds.width / 2;
        int nTop = pDisplayBounds.y + pDisplayBounds.height / 2 - dialogBounds.height / 2;
        shlFacebookContactsFor.setLocation(nLeft, nTop);
        shlFacebookContactsFor.open();
        shlFacebookContactsFor.layout();
        while (!shlFacebookContactsFor.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    /**
	 * Create contents of the dialog.
	 */
    private void createContents() {
        shlFacebookContactsFor = new Shell(getParent(), getStyle());
        shlFacebookContactsFor.setSize(391, 142);
        shlFacebookContactsFor.setText("Facebook Contacts for Picasa - About");
        shlFacebookContactsFor.setLayout(new FormLayout());
        display = getParent().getDisplay();
        Label lblFacebookContactsFor = new Label(shlFacebookContactsFor, SWT.NONE);
        FormData fd_lblFacebookContactsFor = new FormData();
        fd_lblFacebookContactsFor.right = new FormAttachment(0, 385);
        fd_lblFacebookContactsFor.top = new FormAttachment(0, 3);
        fd_lblFacebookContactsFor.left = new FormAttachment(0);
        lblFacebookContactsFor.setLayoutData(fd_lblFacebookContactsFor);
        lblFacebookContactsFor.setAlignment(SWT.CENTER);
        lblFacebookContactsFor.setText("Facebook Contacts for Picasa");
        Label lblVersion = new Label(shlFacebookContactsFor, SWT.NONE);
        lblVersion.setAlignment(SWT.CENTER);
        FormData fd_lblVersion = new FormData();
        fd_lblVersion.right = new FormAttachment(lblFacebookContactsFor, 0, SWT.RIGHT);
        fd_lblVersion.top = new FormAttachment(lblFacebookContactsFor, 6);
        fd_lblVersion.left = new FormAttachment(0);
        lblVersion.setLayoutData(fd_lblVersion);
        lblVersion.setText("Version 0.2");
        Label lblAuthorEminenay = new Label(shlFacebookContactsFor, SWT.NONE);
        lblAuthorEminenay.setAlignment(SWT.CENTER);
        FormData fd_lblAuthorEminenay = new FormData();
        fd_lblAuthorEminenay.right = new FormAttachment(lblFacebookContactsFor, 0, SWT.RIGHT);
        fd_lblAuthorEminenay.top = new FormAttachment(lblVersion, 6);
        fd_lblAuthorEminenay.left = new FormAttachment(0);
        lblAuthorEminenay.setLayoutData(fd_lblAuthorEminenay);
        lblAuthorEminenay.setText("Author: Emin Şenay");
        lblHomepage = new Label(shlFacebookContactsFor, SWT.NONE);
        lblHomepage.addMouseTrackListener(new MouseTrackAdapter() {

            @Override
            public void mouseEnter(MouseEvent arg0) {
                lblHomepageMouseEnter(arg0);
            }
        });
        lblHomepage.addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(MouseEvent arg0) {
                lblHomepageMouseUp(arg0);
            }

            @Override
            public void mouseDown(MouseEvent arg0) {
            }

            @Override
            public void mouseDoubleClick(MouseEvent arg0) {
            }
        });
        lblHomepage.setAlignment(SWT.CENTER);
        FormData fd_lblHttpfcontactspicasasourceforgenet = new FormData();
        fd_lblHttpfcontactspicasasourceforgenet.right = new FormAttachment(lblFacebookContactsFor, 0, SWT.RIGHT);
        fd_lblHttpfcontactspicasasourceforgenet.top = new FormAttachment(lblAuthorEminenay, 6);
        fd_lblHttpfcontactspicasasourceforgenet.left = new FormAttachment(0);
        lblHomepage.setLayoutData(fd_lblHttpfcontactspicasasourceforgenet);
        lblHomepage.setText("http://fcontactspicasa.sourceforge.net");
        Color blue = getParent().getDisplay().getSystemColor(SWT.COLOR_BLUE);
        lblHomepage.setForeground(blue);
    }

    /**
	 * Calls the default browser.
	 * */
    private void lblHomepageMouseUp(MouseEvent evt) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            return;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            return;
        }
        try {
            java.net.URI uri = new java.net.URI(this.lblHomepage.getText());
            desktop.browse(uri);
        } catch (Exception e) {
        }
    }

    private void lblHomepageMouseEnter(MouseEvent evt) {
        final Cursor cursor1 = new Cursor(display, SWT.CURSOR_HAND);
        lblHomepage.setCursor(cursor1);
    }
}
