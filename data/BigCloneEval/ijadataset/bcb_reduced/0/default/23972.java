import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

class SetDir extends JInternalFrame {

    private static final long serialVersionUID = 1L;

    JLabel dirLabel;

    JTextField dirText;

    JButton uploadButton;

    JButton saveButton;

    JButton cancelButton;

    JButton helpButton;

    String pathDir;

    DatabaseCon d = null;

    public SetDir() {
        d = DatabaseCon.createCon();
        setSize(460, 260);
        setTitle("Set base directory");
        setMaximizable(false);
        setIconifiable(false);
        setClosable(false);
        setResizable(false);
        dirLabel = new JLabel("Base Directory");
        dirLabel.setBounds(60, 40, 90, 40);
        dirText = new JTextField(40);
        dirText.setEditable(false);
        dirText.setBounds(180, 50, 140, 20);
        String query = "SELECT Dir_Loc FROM Dir_Info";
        d.execQuery(query);
        if (d.goNext()) {
            dirText.setText(d.getString(1).replace(';', '\\'));
        }
        uploadButton = new JButton("Browse");
        uploadButton.addActionListener(new UploadHandler());
        uploadButton.setBounds(320, 50, 90, 20);
        saveButton = new JButton("Enter");
        saveButton.addActionListener(new SaveHandler());
        saveButton.setBounds(110, 140, 90, 20);
        cancelButton = new JButton("Close");
        cancelButton.addActionListener(new CancelHandler());
        cancelButton.setBounds(240, 140, 90, 20);
        helpButton = new JButton("Help");
        helpButton.addActionListener(new HelpHandler());
        helpButton.setBounds(270, 10, 60, 20);
        getContentPane().setLayout(null);
        getContentPane().add(dirLabel);
        getContentPane().add(dirText);
        getContentPane().add(uploadButton);
        getContentPane().add(saveButton);
        getContentPane().add(cancelButton);
        setVisible(true);
    }

    class HelpHandler implements ActionListener {

        public void actionPerformed(ActionEvent arg) {
            DatabaseCon d = null;
            d = DatabaseCon.createCon();
            HelpClass HC = new HelpClass();
            HC.createGui("Set Base Directory", "Set base directory: By using this option" + " the user can set the base directory of his choice to manipulate" + " shape file�s.");
        }
    }

    class UploadHandler implements ActionListener {

        public void actionPerformed(ActionEvent arg) {
            try {
                File f = promptFile();
                dirText.setText(f.getPath());
            } catch (Exception e) {
                System.err.println("Error in opening directory");
            }
        }

        private File promptFile() throws FileNotFoundException {
            File file;
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Open Shapefile Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setFileFilter(new FileFilter() {

                public boolean accept(File f) {
                    return f.isDirectory();
                }

                public String getDescription() {
                    return "Directory";
                }
            });
            chooser.showOpenDialog(null);
            file = chooser.getSelectedFile();
            return file;
        }
    }

    class SaveHandler implements ActionListener {

        public void actionPerformed(ActionEvent arg) {
            String query = "SELECT * FROM dir_info where Dir_ID =1";
            d.execQuery(query);
            String loc = dirText.getText();
            loc = loc.replace('\\', ';');
            String queryStr;
            if (d.isNotNull()) {
                queryStr = "UPDATE `Dir_info` SET  `Dir_Loc` = \"" + loc + "\" WHERE `Dir_ID` = 1";
            } else {
                queryStr = "INSERT INTO Dir_Info VALUES (1,'" + loc + "')";
            }
            int result = 0;
            {
                Login.displayMessage(queryStr);
                result = d.execUpdate(queryStr);
            }
            if (result == 1) Login.displayMessage("Updated successfully"); else Login.displayMessage("Not updated");
        }
    }

    class CancelHandler implements ActionListener {

        public void actionPerformed(ActionEvent arg) {
            Login.m.formMenu.setEnabled(true);
            Login.m.reportMenu.setEnabled(true);
            setVisible(false);
        }
    }
}
