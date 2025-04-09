package org.hardtokenmgmt.ui.investigate;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.hardtokenmgmt.core.ui.BaseView;
import org.hardtokenmgmt.core.ui.UIHelper;
import org.hardtokenmgmt.core.util.CommonUtils;
import org.hardtokenmgmt.ui.ToLiMaGUI;

/**
 * View of the View Certificate Page
 * 
 * 
 * @author Philip Vendil 2007 feb 16
 *
 * @version $Id$
 */
public class ViewCertView extends BaseView {

    private static final long serialVersionUID = 1L;

    private JLabel logoLabel = null;

    private JLabel titleLabel = null;

    public JLabel cardIconLabel = null;

    public String iconname = "smartcard_h.gif";

    public JLabel certLabels[] = null;

    private JButton backButton = null;

    private JButton fetchCertButton = null;

    private JScrollPane scrollingArea = null;

    private JTextArea certTextArea = null;

    private JLabel copyInfo = null;

    /**
	 * Default constuct
	 *
	 */
    public ViewCertView() {
        super();
        initialize();
    }

    @Override
    protected void initialize() {
        copyInfo = new JLabel();
        copyInfo.setBounds(new Rectangle(288, 381, 346, 17));
        copyInfo.setText(UIHelper.getText("certinfo.youcancopy"));
        this.setSize(new Dimension(UIHelper.getAppWidth(), UIHelper.getAppHeight()));
        this.setLayout(null);
        logoLabel = new JLabel();
        logoLabel.setBounds(ToLiMaGUI.getLogoPos());
        logoLabel.setIcon(UIHelper.getLogo());
        titleLabel = new JLabel();
        titleLabel.setBounds(ToLiMaGUI.getTitleLabelPos());
        titleLabel.setFont(UIHelper.getTitleFont());
        titleLabel.setText(UIHelper.getText("certinfo.viewcert"));
        cardIconLabel = new JLabel();
        cardIconLabel.setBounds(ToLiMaGUI.getIconLabelPos());
        cardIconLabel.setIcon(UIHelper.getImage(iconname));
        this.add(logoLabel, null);
        this.add(titleLabel, null);
        this.add(cardIconLabel, null);
        this.add(getCertTextArea(), null);
        this.add(getBackButton(), null);
        this.add(getFetchCertificateButton(), null);
        this.add(copyInfo, null);
    }

    public JButton getBackButton() {
        if (backButton == null) {
            backButton = new JButton(UIHelper.getText("back"));
            backButton.setBounds(ToLiMaGUI.getBackButtonPos());
            backButton.setIcon(UIHelper.getImage("back.gif"));
        }
        return backButton;
    }

    public JButton getFetchCertificateButton() {
        if (fetchCertButton == null) {
            fetchCertButton = new JButton(UIHelper.getText("investigatecard.opencert"));
            fetchCertButton.setBounds(ToLiMaGUI.getNextButtonPos());
            fetchCertButton.setIcon(UIHelper.getImage("cert_view.gif"));
            fetchCertButton.setVisible(CommonUtils.isDesktopSupported());
        }
        return fetchCertButton;
    }

    public JLabel[] getCertLabels(String labelText[], int startX, int startY) {
        int len = labelText.length;
        int yStep = 30;
        int labelWidth = 600;
        int labelHeight = 21;
        if (certLabels != null) {
            int lLen = certLabels.length;
            for (int i = 0; i < lLen; i++) {
                certLabels[i].setVisible(false);
                this.remove(certLabels[i]);
            }
            certLabels = null;
        }
        certLabels = new JLabel[len];
        for (int i = 0; i < len; i++) {
            certLabels[i] = new JLabel(labelText[i]);
            certLabels[i].setBounds(new Rectangle(startX, startY, labelWidth, labelHeight));
            startY += yStep;
        }
        return certLabels;
    }

    /**
	 * This method initializes certTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
    public JScrollPane getCertTextArea() {
        if (scrollingArea == null) {
            certTextArea = new JTextArea();
            scrollingArea = new JScrollPane(certTextArea);
            JPanel content = new JPanel();
            content.setLayout(new BorderLayout());
            content.add(scrollingArea, BorderLayout.CENTER);
            scrollingArea.setBounds(new Rectangle(288, 404, 450, 85));
        }
        return scrollingArea;
    }

    public void setCertTextArea(String base64cert) {
        this.certTextArea.setText(base64cert);
    }
}
