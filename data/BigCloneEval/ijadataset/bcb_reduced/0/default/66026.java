import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.*;
import javax.swing.plaf.metal.*;

public class RenameDir extends JFrame implements ActionListener {

    private static final String TITLE = "File Rename Tool  ver2.0";

    private static final int WIDTH = 500;

    private static final int HEIGHT = 300;

    private static final String DESCRIPTION = "Select a folder, to batch rename all the files in the directory.";

    private boolean OUTPUT_ON = false;

    private String newNamePrefix;

    private File directory;

    private Container contentPane;

    private JPanel pnlDescription, pnlDirectory, pnlPrefix, pnlRename, pnlSuffix, pnlCtrl, pnlOption;

    private JLabel lblDesc, lblSequence, lblDirectory;

    private JTextField txtDirectory, txtPrefix, txtSuffix, txtRename, txtSequence;

    private JButton btnOk, btnCancel, btnAbout;

    private JCheckBox cbxPrefix, cbxSuffix, cbxRename, cbxIgnoreExtension, cbxExperiment, cbxOutput;

    private JComboBox cboSequence;

    private Dimension stdDim;

    private static JFrame outputFrame;

    private JScrollPane scrOutput;

    private JTextArea txaOutput;

    public RenameDir() {
        setSize(WIDTH, HEIGHT);
        setTitle(TITLE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setLocation(400, 300);
        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                int response = JOptionPane.showConfirmDialog(null, "Are you sure want to exit?", "Close Program", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) System.exit(0);
            }
        });
        buildGUI();
    }

    public static void main(String[] args) {
        Rename renameTask = new Rename();
        renameTask.setVisible(true);
    }

    private void renameFile() {
        boolean operationResult = false;
        boolean overallResult = true;
        int failCount = 0;
        String[] fileList = directory.list();
        String Prefix = txtPrefix.getText();
        String Rename = txtRename.getText();
        String Suffix = txtSuffix.getText();
        String digits = (String) cboSequence.getSelectedItem();
        int StartingNum;
        String generatedSequence;
        File oldFile;
        if (cbxOutput.isSelected() && OUTPUT_ON == false) {
            buildOutput();
            OUTPUT_ON = true;
        }
        for (int i = 0; i < fileList.length; i++) {
            oldFile = new File(directory.getPath() + "/" + fileList[i]);
            String readability = fileList[i] + " - readable?: " + oldFile.canRead();
            System.out.println(readability);
            if (OUTPUT_ON) txaOutput.append("\n" + readability);
        }
        for (int i = 0; i < fileList.length; i++) {
            oldFile = new File(directory.getPath() + "/" + fileList[i]);
            String fileExtension;
            if (cbxIgnoreExtension.isSelected() == true) {
                fileExtension = "";
            } else fileExtension = getFileExtension(fileList[i]);
            String fileName = getFileName(fileList[i]);
            String inputInfo = "The input filename->" + fileList[i] + "\nfile name->" + fileName + "\nextension->" + fileExtension;
            System.out.println(inputInfo);
            if (OUTPUT_ON) txaOutput.append("\n" + inputInfo);
            if (digits.equals("None") == true) {
                generatedSequence = "";
            } else {
                StartingNum = Integer.parseInt(txtSequence.getText());
                generatedSequence = nameSequence(StartingNum + i, digits);
            }
            if (cbxRename.isSelected() == true) {
                fileName = Rename + generatedSequence;
            } else {
                fileName = fileName + generatedSequence;
            }
            String newFileName = Prefix + fileName + Suffix + fileExtension;
            String tentativeName = "new Filename will be ->" + newFileName + "\n";
            System.out.println(tentativeName);
            if (OUTPUT_ON) txaOutput.append("\n" + tentativeName);
            if (cbxExperiment.isSelected() == false) {
                operationResult = oldFile.renameTo(new File(directory.getPath() + "/" + newFileName));
                String renameResult = "\t*Rename successfully?: " + operationResult + "\n\n";
                System.out.println(renameResult);
                if (operationResult == false) failCount++;
                if (OUTPUT_ON) txaOutput.append("\n" + renameResult);
                overallResult = (operationResult && overallResult);
            }
        }
        if (cbxExperiment.isSelected() == false) {
            System.out.println("Overall Result: " + overallResult);
            System.out.println("dir rename: " + directory.renameTo(new File("test")));
            if (overallResult) JOptionPane.showMessageDialog(null, "All files renamed successfully!"); else JOptionPane.showMessageDialog(null, "File renamed with " + failCount + " failure(s)");
        }
    }

    private boolean chooseDirectory() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        int returnval = fc.showOpenDialog(this);
        if (returnval == JFileChooser.APPROVE_OPTION) {
            directory = fc.getSelectedFile();
            btnOk.setEnabled(true);
            return true;
        }
        return false;
    }

    private boolean welcomeScreen() {
        JOptionPane.showMessageDialog(null, "Select a folder, to rename all the files inside", "File Rename Tool", JOptionPane.OK_OPTION);
        String prefix = JOptionPane.showInputDialog(null, "Please specify the name prefix", "File Rename Tool", JOptionPane.YES_NO_OPTION);
        if (prefix == null) {
            prefix = "";
        }
        System.out.println(prefix);
        newNamePrefix = prefix;
        int agree = JOptionPane.showConfirmDialog(null, "Are you sure with this?", "Confirmation", JOptionPane.YES_NO_OPTION);
        if (agree == JOptionPane.YES_OPTION) return true; else return false;
    }

    private void buildOutput() {
        outputFrame = new JFrame("Output");
        outputFrame.setSize(WIDTH + 100, HEIGHT);
        Container outputPane = outputFrame.getContentPane();
        outputPane.setBackground(Color.BLACK);
        outputPane.setForeground(Color.WHITE);
        txaOutput = new JTextArea();
        txaOutput.setEditable(false);
        txaOutput.setBackground(Color.BLACK);
        txaOutput.setForeground(Color.WHITE);
        txaOutput.setFont(new Font("Courier New", 1, 15));
        scrOutput = new JScrollPane(txaOutput);
        outputFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        outputFrame.setVisible(true);
        outputFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                outputFrame.dispose();
                OUTPUT_ON = false;
            }
        });
        outputPane.add(scrOutput);
    }

    private void buildGUI() {
        stdDim = new Dimension(WIDTH, 25);
        contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        buildDescPanel();
        buildDirPanel();
        buildPrefixPanel();
        buildRenamePanel();
        buildSuffixPanel();
        buildOptPanel();
        buildCtrlPanel();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
        } catch (ClassNotFoundException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        }
    }

    private void buildDescPanel() {
        pnlDescription = new JPanel();
        pnlDescription.setLayout(new FlowLayout(FlowLayout.LEADING));
        Border blackline = BorderFactory.createLineBorder(Color.BLACK);
        pnlDescription.setBorder(BorderFactory.createTitledBorder(blackline, "Description"));
        lblDesc = new JLabel(DESCRIPTION);
        lblDesc.setMaximumSize(new Dimension(WIDTH - 10, HEIGHT / 10));
        pnlDescription.add(lblDesc);
        contentPane.add(pnlDescription);
    }

    private void buildDirPanel() {
        pnlDirectory = new JPanel();
        pnlDirectory.setLayout(new BoxLayout(pnlDirectory, BoxLayout.X_AXIS));
        pnlDirectory.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        lblDirectory = new JLabel("Directory: ");
        txtDirectory = new JTextField();
        txtDirectory.setEditable(false);
        txtDirectory.setPreferredSize(stdDim);
        txtDirectory.setMaximumSize(stdDim);
        JButton btnSelectDirectory = new JButton("select");
        ActionListener selectAction = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (chooseDirectory()) txtDirectory.setText(directory.getPath());
            }
        };
        btnSelectDirectory.addActionListener(selectAction);
        pnlDirectory.add(lblDirectory);
        pnlDirectory.add(txtDirectory);
        pnlDirectory.add(btnSelectDirectory);
        contentPane.add(pnlDirectory);
    }

    private void buildPrefixPanel() {
        pnlPrefix = new JPanel();
        pnlPrefix.setLayout(new BoxLayout(pnlPrefix, BoxLayout.X_AXIS));
        pnlPrefix.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        txtPrefix = new JTextField();
        txtPrefix.setMaximumSize(stdDim);
        txtPrefix.setEditable(false);
        cbxPrefix = new JCheckBox("Prefix");
        cbxPrefix.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (cbxPrefix.isSelected()) txtPrefix.setEditable(true); else {
                    txtPrefix.setEditable(false);
                    txtPrefix.setText("");
                }
            }
        });
        pnlPrefix.add(cbxPrefix);
        pnlPrefix.add(txtPrefix);
        contentPane.add(pnlPrefix);
    }

    private void buildRenamePanel() {
        pnlRename = new JPanel();
        pnlRename.setLayout(new BoxLayout(pnlRename, BoxLayout.X_AXIS));
        pnlRename.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        txtRename = new JTextField();
        txtRename.setMaximumSize(stdDim);
        txtRename.setEditable(false);
        cbxRename = new JCheckBox("Rename");
        lblSequence = new JLabel("Sequence: [Leading Zero]");
        txtSequence = new JTextField();
        txtSequence.setEditable(false);
        Dimension seqDim = new Dimension(150, 25);
        txtSequence.setMaximumSize(seqDim);
        txtSequence.setEditable(false);
        String[] sequenceValue = { "None", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
        cboSequence = new JComboBox(sequenceValue);
        cboSequence.setMaximumSize(new Dimension(80, 25));
        cboSequence.setEnabled(true);
        cbxRename.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (cbxRename.isSelected()) {
                    txtRename.setEditable(true);
                } else {
                    txtRename.setEditable(false);
                    txtRename.setText("");
                }
            }
        });
        cboSequence.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String value = (String) cboSequence.getSelectedItem();
                if (value.equals("None")) {
                    txtSequence.setEditable(false);
                    txtSequence.setText("");
                } else {
                    int digit = Integer.parseInt(value);
                    txtSequence.setEditable(true);
                    txtSequence.setColumns(digit);
                    txtSequence.setDocument(new JTextFieldLimit(digit));
                }
            }
        });
        pnlRename.add(cbxRename);
        pnlRename.add(txtRename);
        pnlRename.add(lblSequence);
        pnlRename.add(txtSequence);
        pnlRename.add(cboSequence);
        contentPane.add(pnlRename);
    }

    private void buildSuffixPanel() {
        pnlSuffix = new JPanel();
        pnlSuffix.setLayout(new BoxLayout(pnlSuffix, BoxLayout.X_AXIS));
        pnlSuffix.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        txtSuffix = new JTextField();
        txtSuffix.setMaximumSize(stdDim);
        txtSuffix.setEditable(false);
        cbxSuffix = new JCheckBox("Suffix");
        cbxIgnoreExtension = new JCheckBox("Ignore File Extension");
        ActionListener cbxSuffixListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (cbxSuffix.isSelected()) txtSuffix.setEditable(true); else {
                    txtSuffix.setEditable(false);
                    txtSuffix.setText("");
                }
            }
        };
        cbxSuffix.addActionListener(cbxSuffixListener);
        pnlSuffix.add(cbxSuffix);
        pnlSuffix.add(txtSuffix);
        pnlSuffix.add(cbxIgnoreExtension);
        contentPane.add(pnlSuffix);
    }

    private void buildOptPanel() {
        pnlOption = new JPanel();
        pnlOption.setLayout(new FlowLayout());
        pnlOption.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        cbxExperiment = new JCheckBox("Experimental Mode");
        cbxOutput = new JCheckBox("Output Window");
        pnlOption.add(cbxExperiment);
        pnlOption.add(cbxOutput);
        contentPane.add(pnlOption);
    }

    private void buildCtrlPanel() {
        pnlCtrl = new JPanel();
        pnlCtrl.setLayout(new BoxLayout(pnlCtrl, BoxLayout.X_AXIS));
        pnlCtrl.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btnOk = new JButton("Ok");
        btnOk.setEnabled(false);
        btnOk.addActionListener(this);
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(this);
        btnAbout = new JButton("About");
        btnAbout.addActionListener(this);
        pnlCtrl.add(btnOk);
        pnlCtrl.add(btnCancel);
        pnlCtrl.add(btnAbout);
        contentPane.add(pnlCtrl);
    }

    public String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            String fileXT = filename.substring(dotIndex);
            return fileXT;
        } else return "";
    }

    public String getFileName(String filename) {
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            String fileName = filename.substring(0, dotIndex);
            return fileName;
        } else return "";
    }

    private String nameSequence(int number, String digits) {
        String leadingZeroSpecifier = "%0" + digits + "d";
        String generatedSequence = String.format(leadingZeroSpecifier, number);
        return generatedSequence;
    }

    public void actionPerformed(ActionEvent e) {
        JButton clickedButton = (JButton) e.getSource();
        JOptionPane.showMessageDialog(null, clickedButton.getText() + " clicked");
        if (clickedButton == btnOk) {
            String cboString = (String) cboSequence.getSelectedItem();
            if (cboString.equals("None") == false) {
                String temp = txtSequence.getText();
                if (temp.equals("") || temp == null) {
                    JOptionPane.showMessageDialog(null, "Oops... Please fill up the sequence number field");
                    txtSequence.grabFocus();
                    return;
                }
            }
            JOptionPane.showMessageDialog(null, "rename invoked");
            renameFile();
        } else if (clickedButton == btnCancel) {
            btnOk.setEnabled(false);
            txtDirectory.setText("");
            directory = null;
            cbxPrefix.setSelected(false);
            cbxRename.setSelected(false);
            cbxSuffix.setSelected(false);
            txtPrefix.setEditable(false);
            txtPrefix.setText("");
            txtSuffix.setEditable(false);
            txtSuffix.setText("");
            txtRename.setEditable(false);
            txtRename.setText("");
            cbxIgnoreExtension.setSelected(false);
            txtSequence.setEditable(false);
            txtSequence.setText("");
            cboSequence.setSelectedIndex(0);
        } else if (clickedButton == btnAbout) {
        }
    }

    class JTextFieldLimit extends PlainDocument {

        private int limit;

        private boolean toUpperCase = false;

        JTextFieldLimit(int limit) {
            super();
            this.limit = limit;
        }

        JTextFieldLimit(int limit, boolean upperCase) {
            super();
            this.limit = limit;
            toUpperCase = upperCase;
        }

        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null) return;
            if ((getLength() + str.length()) <= limit) {
                if (toUpperCase) str = str.toUpperCase();
                super.insertString(offset, str, attr);
            }
        }
    }
}
