package actions.menugeneral;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Action, which is occurred when user clicks "Context help..." component, it
 * tries to open help-file in the browser.
 * 
 * @author <a href="mailto:sukharevd@gmail.com">Sukharev Dmitriy</a>
 * 
 */
public class HelpingAction extends AbstractAction {

    /**
     * JDK 1.1 serialVersionUID.
     */
    private static final long serialVersionUID = 2960508224617600516L;

    private JFrame mainFrame;

    /**
     * @param mainFrame
     */
    public HelpingAction(JFrame mainFrame) {
        super();
        this.mainFrame = mainFrame;
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(final ActionEvent arg0) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            File file = new File("UserGuide.htm");
            try {
                desktop.open(file);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame, "Help file \"UserGuide.htm\" can't be opened.", "Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("Error: Help file \"UserGuide.htm\"" + "can't be opened.");
            }
        }
    }
}
