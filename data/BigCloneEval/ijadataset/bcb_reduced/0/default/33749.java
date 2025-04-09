import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

class SaveStreamAction implements ActionListener {

    String fileName;

    InputStream input;

    Component parent;

    public SaveStreamAction(String fileName, InputStream input, Component parent) {
        this.fileName = fileName;
        this.input = input;
        this.parent = parent;
    }

    public void actionPerformed(ActionEvent e) {
        File suggest = null;
        if (fileName != null) suggest = new File(fileName);
        Options.saveFileChooser.setSelectedFile(suggest);
        int returnVal = Options.saveFileChooser.showSaveDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = Options.saveFileChooser.getSelectedFile();
            try {
                Utilities.copyStream(new BufferedInputStream(input), new BufferedOutputStream(new FileOutputStream(file)));
            } catch (Exception ex) {
                LogFrame.log(ex);
            }
        }
    }
}
