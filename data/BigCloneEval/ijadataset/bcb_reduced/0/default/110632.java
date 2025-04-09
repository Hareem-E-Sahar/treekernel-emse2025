import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import translators.TransformerUtil;

/**
 * DefuddleClient Simple client for choosing data, schema, and output files
 * 
 * @author d3m305
 * @date 6/27/2006
 */
public class DefuddleClient extends JFrame {

    private JPanel myPanel;

    private JButton translateButton;

    private JTextField dataFile;

    private JLabel xsltlabel;

    private JLabel dfdllabel;

    private JLabel datalabel;

    private JButton xsltfileBrowse;

    private JTextField xsltFile;

    private JButton dfdlschemabrowse;

    private JButton dataBrowse;

    private JTextField dfdlSchema;

    private String mLastDirectory;

    private String mBaseDirectory;

    private String mLibDirectory;

    ActionListener translateActionListener;

    ActionListener dataFileActionListener;

    ActionListener dfdlFileActionListener;

    ActionListener xsltFileActionListener;

    public static void main(String args[]) {
        if (args.length > 2) System.err.println("Usage: DefuddleClient <base directory> <relative lib directory>");
        DefuddleClient inst = new DefuddleClient();
        inst.setVisible(true);
        inst.mBaseDirectory = args[0];
        inst.mLibDirectory = args[1];
    }

    public DefuddleClient() {
        mLastDirectory = "";
        translateActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                byte result[] = null;
                InputStream dfdlSchemaStream = null;
                InputStream xsltSchemaStream = null;
                InputStream dataStream = null;
                InputStream dataStreams[] = new InputStream[1];
                String packageName = "";
                String baseDir = "./";
                String buildPath = "transforms";
                String buildDir = "build";
                String libDir = "lib";
                try {
                    if (dfdlSchema.getText() != null && dfdlSchema.getText().length() > 0) {
                        dfdlSchemaStream = new FileInputStream(dfdlSchema.getText());
                        packageName = dfdlSchema.getText();
                        if (packageName.indexOf("/") >= 0) packageName = packageName.substring(packageName.lastIndexOf("/") + 1); else if (packageName.indexOf("\\") >= 0) packageName = packageName.substring(packageName.lastIndexOf("\\") + 1);
                        if (packageName.indexOf(".") >= 0) packageName = packageName.substring(0, packageName.lastIndexOf("."));
                        packageName = packageName + "Pkg";
                    }
                    if (xsltFile.getText() != null && xsltFile.getText().length() > 0) xsltSchemaStream = new FileInputStream(xsltFile.getText());
                    if (dataFile.getText() != null && dataFile.getText().length() > 0) {
                        dataStream = new FileInputStream(dataFile.getText());
                        dataStreams[0] = dataStream;
                    }
                    result = TransformerUtil.dfdlTransform(dfdlSchemaStream, xsltSchemaStream, dataStreams, packageName, baseDir, buildPath, buildDir, libDir);
                } catch (Throwable ex) {
                    System.err.println("Exception while translating: " + ex);
                    ex.printStackTrace();
                }
                if (result != null) {
                    File tmpOutputFile = chooseFile("Save Result To...");
                    if (tmpOutputFile != null) {
                        try {
                            if (!tmpOutputFile.exists()) tmpOutputFile.createNewFile();
                            OutputStream out = new FileOutputStream(tmpOutputFile);
                            out.write(result);
                            out.close();
                            JOptionPane.showMessageDialog(new JFrame(), "Transformation was performed successfully \n  and data saved to: \n" + tmpOutputFile.getCanonicalPath() + "\n", "", 1);
                        } catch (Exception ex) {
                            System.err.println("Error while saving result: " + ex);
                        }
                    } else {
                        System.err.println("Error: no save path given");
                    }
                }
            }
        };
        dataFileActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                File tmpDataFile = chooseFile("Choose Data File...");
                if (tmpDataFile != null) dataFile.setText(tmpDataFile.getAbsolutePath());
            }
        };
        dfdlFileActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                File tmpDfdlFile = chooseFile("Choose DFDL Schema...");
                if (tmpDfdlFile != null) dfdlSchema.setText(tmpDfdlFile.getAbsolutePath());
            }
        };
        xsltFileActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                File tmpXSLTFile = chooseFile("Choose XSLT File...");
                if (tmpXSLTFile != null) xsltFile.setText(tmpXSLTFile.getAbsolutePath());
            }
        };
        initGUI();
    }

    private void initGUI() {
        try {
            addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent event) {
                    System.exit(0);
                }
            });
            setTitle("Run Defuddle Transform");
            myPanel = new JPanel();
            GridBagLayout myPanelLayout = new GridBagLayout();
            myPanelLayout.rowWeights = (new double[] { 0.10000000000000001D, 0.10000000000000001D, 0.10000000000000001D, 0.10000000000000001D });
            myPanelLayout.rowHeights = (new int[] { 7, 7, 7, 7 });
            myPanelLayout.columnWeights = (new double[] { 0.10000000000000001D, 0.10000000000000001D, 0.10000000000000001D, 0.10000000000000001D });
            myPanelLayout.columnWidths = (new int[] { 7, 7, 7, 7 });
            myPanel.setLayout(myPanelLayout);
            getContentPane().add(myPanel, "Center");
            translateButton = new JButton();
            myPanel.add(translateButton, new GridBagConstraints(1, 3, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
            translateButton.setText("Translate");
            translateButton.setPreferredSize(new Dimension(108, 22));
            translateButton.addActionListener(translateActionListener);
            dataFile = new JTextField();
            myPanel.add(dataFile, new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
            dfdlSchema = new JTextField();
            myPanel.add(dfdlSchema, new GridBagConstraints(1, 1, 1, 1, 0.0D, 0.0D, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
            dataBrowse = new JButton();
            myPanel.add(dataBrowse, new GridBagConstraints(2, 0, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
            dataBrowse.setText("Browse");
            dataBrowse.addActionListener(dataFileActionListener);
            dfdlschemabrowse = new JButton();
            myPanel.add(dfdlschemabrowse, new GridBagConstraints(2, 1, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
            dfdlschemabrowse.setText("Browse");
            dfdlschemabrowse.addActionListener(dfdlFileActionListener);
            xsltFile = new JTextField();
            myPanel.add(xsltFile, new GridBagConstraints(1, 2, 1, 1, 0.0D, 0.0D, 10, 2, new Insets(0, 0, 0, 0), 0, 0));
            xsltfileBrowse = new JButton();
            myPanel.add(xsltfileBrowse, new GridBagConstraints(2, 2, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
            xsltfileBrowse.setText("Browse");
            xsltfileBrowse.addActionListener(xsltFileActionListener);
            datalabel = new JLabel();
            myPanel.add(datalabel, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
            datalabel.setText("Data:");
            dfdllabel = new JLabel();
            myPanel.add(dfdllabel, new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
            dfdllabel.setText("DFDL Schema:");
            xsltlabel = new JLabel();
            myPanel.add(xsltlabel, new GridBagConstraints(0, 2, 1, 1, 0.0D, 0.0D, 10, 0, new Insets(0, 0, 0, 0), 0, 0));
            xsltlabel.setText("XSLT(optional):");
            setDefaultCloseOperation(2);
            pack();
            setSize(400, 300);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File chooseFile(String title) {
        File selectedFile = null;
        JFileChooser fileUploadDialog;
        if (mLastDirectory.equals("")) fileUploadDialog = new JFileChooser("."); else fileUploadDialog = new JFileChooser(mLastDirectory);
        fileUploadDialog.setDialogTitle(title);
        int returnVal = fileUploadDialog.showOpenDialog(this);
        if (returnVal == 0) {
            File theFile = fileUploadDialog.getSelectedFile();
            mLastDirectory = theFile.getAbsolutePath();
            if (theFile != null) selectedFile = theFile;
        }
        return selectedFile;
    }
}
