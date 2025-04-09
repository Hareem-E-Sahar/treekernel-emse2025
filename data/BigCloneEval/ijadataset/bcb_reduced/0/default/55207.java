import java.awt.event.*;
import java.io.*;
import javax.swing.*;

class OpenFileAction extends AbstractAction {

    VisibleFrame frame;

    SimpleFrame dataFrame;

    JFileChooser chooser;

    File file;

    OpenFileAction(SimpleFrame dataframe, JFileChooser chooser) {
        this.chooser = chooser;
        this.frame = dataframe.getViewFrame();
        this.dataFrame = dataframe;
    }

    public void actionPerformed(ActionEvent evt) {
        int returnValue = chooser.showOpenDialog(frame);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            this.dataFrame.setFile(chooser.getSelectedFile());
        }
    }
}
