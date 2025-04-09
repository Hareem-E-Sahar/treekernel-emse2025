package com.eminsenay.FacebookContactsForPicasa.UI;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class AboutBox extends Composite {

    private Label headerLabel;

    private Label versionLabel;

    private Label authorLabel;

    private Text helpTextBox;

    private Label labelHomepage;

    public AboutBox(Composite parent, int style) {
        super(parent, style);
        initialize();
    }

    private void initialize() {
        FormLayout thisLayout = new FormLayout();
        this.setLayout(thisLayout);
        {
            helpTextBox = new Text(this, SWT.MULTI | SWT.WRAP);
            FormData helpTextBoxLData = new FormData();
            helpTextBoxLData.left = new FormAttachment(0, 1000, 12);
            helpTextBoxLData.top = new FormAttachment(0, 1000, 105);
            helpTextBoxLData.width = 489;
            helpTextBoxLData.height = 170;
            helpTextBoxLData.right = new FormAttachment(1000, 1000, -12);
            helpTextBoxLData.bottom = new FormAttachment(1000, 1000, -12);
            helpTextBox.setLayoutData(helpTextBoxLData);
            helpTextBox.setText("Default contacts.xml paths:\r\n\r\nWindows 7: C:\\Users\\<UserName>\\AppData\\Local\\Google\\Picasa2\\contacts\\contacts.xml\r\nWindows XP: C:\\Documents and Settings\\<UserName>\\Local Settings\\Application Data\\Google\\Picasa2\\contacts\\contacts.xml\r\nMacOS X: ~/Library/Application\\ Support/Google/Picasa3/contacts/contacts.xml");
            helpTextBox.setEditable(false);
        }
        {
            labelHomepage = new Label(this, SWT.NONE);
            FormData labelHomepageLData = new FormData();
            labelHomepageLData.left = new FormAttachment(0, 1000, 12);
            labelHomepageLData.top = new FormAttachment(0, 1000, 75);
            labelHomepageLData.width = 685;
            labelHomepageLData.height = 18;
            labelHomepageLData.right = new FormAttachment(1000, 1000, -12);
            labelHomepage.setLayoutData(labelHomepageLData);
            labelHomepage.setText("http://fcontactspicasa.sourceforge.net");
            labelHomepage.setAlignment(SWT.CENTER);
            labelHomepage.addMouseTrackListener(new MouseTrackAdapter() {

                public void mouseEnter(MouseEvent evt) {
                    labelHomepageMouseEnter(evt);
                }
            });
            labelHomepage.addMouseListener(new MouseAdapter() {

                public void mouseUp(MouseEvent evt) {
                    labelHomepageMouseUp(evt);
                }
            });
        }
        {
            authorLabel = new Label(this, SWT.NONE);
            FormData authorLabelLData = new FormData();
            authorLabelLData.left = new FormAttachment(0, 1000, 12);
            authorLabelLData.top = new FormAttachment(0, 1000, 54);
            authorLabelLData.width = 685;
            authorLabelLData.height = 18;
            authorLabelLData.right = new FormAttachment(1000, 1000, -12);
            authorLabel.setLayoutData(authorLabelLData);
            authorLabel.setText("Author: Emin Şenay");
            authorLabel.setAlignment(SWT.CENTER);
        }
        {
            versionLabel = new Label(this, SWT.NONE);
            FormData versionLabelLData = new FormData();
            versionLabelLData.left = new FormAttachment(0, 1000, 12);
            versionLabelLData.top = new FormAttachment(0, 1000, 33);
            versionLabelLData.width = 685;
            versionLabelLData.height = 18;
            versionLabelLData.right = new FormAttachment(1000, 1000, -12);
            versionLabel.setLayoutData(versionLabelLData);
            versionLabel.setText("version 0.1.1");
            versionLabel.setAlignment(SWT.CENTER);
        }
        {
            headerLabel = new Label(this, SWT.NONE);
            FormData headerLabelLData = new FormData();
            headerLabelLData.left = new FormAttachment(0, 1000, 12);
            headerLabelLData.top = new FormAttachment(0, 1000, 12);
            headerLabelLData.width = 685;
            headerLabelLData.height = 18;
            headerLabelLData.right = new FormAttachment(1000, 1000, -12);
            headerLabel.setLayoutData(headerLabelLData);
            headerLabel.setText("Facebook Contacts for Picasa");
            headerLabel.setAlignment(SWT.CENTER);
        }
        pack();
        this.setSize(710, 205);
    }

    /**
	 * Calls the default browser.
	 * */
    private void labelHomepageMouseUp(MouseEvent evt) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            return;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            return;
        }
        try {
            java.net.URI uri = new java.net.URI(this.labelHomepage.getText());
            desktop.browse(uri);
        } catch (Exception e) {
        }
    }

    private void labelHomepageMouseEnter(MouseEvent evt) {
        final Cursor cursor1 = new Cursor(this.getDisplay(), SWT.CURSOR_HAND);
        labelHomepage.setCursor(cursor1);
    }
}
