package com.qspin.qtaste.ui;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import org.apache.log4j.Logger;
import com.qspin.qtaste.config.StaticConfiguration;
import com.qspin.qtaste.ui.tools.ResourceManager;
import com.qspin.qtaste.util.Log4jLoggerFactory;

/**
 *
 * @author vdubois
 */
@SuppressWarnings("serial")
public class CommonShortcutsPanel extends JPanel {

    public CommonShortcutsPanel() {
        super(new SpringLayout());
        genUI();
    }

    private void genUI() {
        showTestAPIButton.setIcon(ResourceManager.getInstance().getImageIcon("icons/testAPIDoc"));
        showTestAPIButton.setToolTipText("Show test API documentation");
        showTestAPIButton.setVisible(true);
        showTestAPIButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String filename = StaticConfiguration.TEST_API_DOC_DIR + File.separator + "index.html";
                File resultsFile = new File(filename);
                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(resultsFile);
                    } else {
                        logger.error("Feature not supported by this platform");
                    }
                } catch (IOException ex) {
                    logger.error("Could not open " + filename);
                }
            }
        });
        this.add(showTestAPIButton);
    }

    protected JButton showTestAPIButton = new JButton();

    private static Logger logger = Log4jLoggerFactory.getLogger(CommonShortcutsPanel.class);
}
