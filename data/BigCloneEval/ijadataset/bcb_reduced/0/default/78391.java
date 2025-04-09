import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class AssignmentEdit extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;

    private JLabel lblTaskDate;

    private JLabel lblTaskStart;

    private JLabel lblTaskStop;

    private JTextField txtTaskDate;

    private JCheckBox chkAvailable;

    private JButton btnCancel;

    private JButton btnSaveApply;

    private JButton btnBrowse;

    private JTextField txtFileDescr;

    private JLabel lblFileDescr;

    private JTextField txtTaskEnd;

    private JLabel lblTaskDesc;

    private JTextField txtFilePath;

    private JLabel lblFilePath;

    private JTextField txtTaskDescr;

    private JComboBox cboTaskType;

    private JLabel lblTaskType;

    private JTextField txtTaskStart;

    private Mediator mediator;

    private Task thisTask;

    private boolean edit;

    private JFileChooser NewFileChooser;

    public AssignmentEdit(Mediator paramMediator) {
        super();
        mediator = paramMediator;
    }

    private boolean errorCheckTextBoxes() {
        if (dateFormatCheck(txtTaskDate.getText()) == false) {
            JOptionPane.showMessageDialog(null, "Creation Date does not meet required format!\n" + "Please use YYYY-MM-DD date format.", "Save File Denied", JOptionPane.ERROR_MESSAGE);
            txtTaskDate.grabFocus();
            return false;
        }
        if (trueDateCheck(txtTaskDate.getText()) == false) {
            JOptionPane.showMessageDialog(null, "Creation Date is not an acceptable date!\n" + "Please use a real date after 2007-08-23.", "Save File Denied", JOptionPane.ERROR_MESSAGE);
            txtTaskDate.grabFocus();
            return false;
        }
        if (dateFormatCheck(txtTaskStart.getText()) == false) {
            JOptionPane.showMessageDialog(null, "Task Start Date does not meet required format!\n" + "Please use YYYY-MM-DD date format.", "Save File Denied", JOptionPane.ERROR_MESSAGE);
            txtTaskStart.grabFocus();
            return false;
        }
        if (trueDateCheck(txtTaskStart.getText()) == false) {
            JOptionPane.showMessageDialog(null, "Task Start Date is not an acceptable date!\n" + "Please use real a date after 2007-08-23.", "Save File Denied", JOptionPane.ERROR_MESSAGE);
            txtTaskStart.grabFocus();
            return false;
        }
        if (dateFormatCheck(txtTaskEnd.getText()) == false) {
            JOptionPane.showMessageDialog(null, "Task End Date does not meet required format!\n" + "Please use YYYY-MM-DD date format.", "Save File Denied", JOptionPane.ERROR_MESSAGE);
            txtTaskEnd.grabFocus();
            return false;
        }
        if (trueDateCheck(txtTaskEnd.getText()) == false) {
            JOptionPane.showMessageDialog(null, "Task End Date is not an acceptable date!\n" + "Please use a real date after 2007-08-23.", "Save File Denied", JOptionPane.ERROR_MESSAGE);
            txtTaskEnd.grabFocus();
            return false;
        }
        if (txtTaskDescr.getText().length() == 0) {
            JOptionPane.showMessageDialog(null, "Task Description required!", "Save File Denied", JOptionPane.ERROR_MESSAGE);
            txtTaskDescr.grabFocus();
            return false;
        }
        if (txtFilePath.getText().length() == 0) {
            JOptionPane.showMessageDialog(null, "File Path required!", "Save File Denied", JOptionPane.ERROR_MESSAGE);
            btnBrowse.grabFocus();
            return false;
        }
        if (txtFileDescr.getText().length() == 0) {
            JOptionPane.showMessageDialog(null, "File Description required!", "Save File Denied", JOptionPane.ERROR_MESSAGE);
            txtFileDescr.grabFocus();
            return false;
        }
        return true;
    }

    private boolean dateFormatCheck(String string) {
        char[] tempCharArray = new char[string.length()];
        if (string.length() != 10) {
            return false;
        }
        for (int i = 0; i < string.length(); i++) {
            tempCharArray[i] = string.charAt(i);
            System.out.println(tempCharArray[i]);
        }
        for (int i = 0; i < 3; i++) {
            if (tempCharArray[i] >= '9' && tempCharArray[i] <= '0') {
                return false;
            }
        }
        for (int i = 5; i < 6; i++) {
            if (tempCharArray[i] >= '9' && tempCharArray[i] <= '0') {
                return false;
            }
        }
        for (int i = 8; i < 9; i++) {
            if (tempCharArray[i] >= '9' && tempCharArray[i] <= '0') {
                return false;
            }
        }
        if (tempCharArray[4] != '-' || tempCharArray[7] != '-') {
            return false;
        }
        return true;
    }

    private boolean trueDateCheck(String date) {
        if (Integer.valueOf(date.substring(0, 4)).intValue() < 2007) {
            return false;
        }
        if (Integer.valueOf(date.substring(5, 7)).intValue() < 1 || (Integer.valueOf(date.substring(5, 7)).intValue() > 12)) {
            return false;
        }
        if (Integer.valueOf(date.substring(5, 7)).intValue() == 4 || Integer.valueOf(date.substring(5, 7)).intValue() == 6 || Integer.valueOf(date.substring(5, 7)).intValue() == 8 || Integer.valueOf(date.substring(5, 7)).intValue() == 11) {
            if (Integer.valueOf(date.substring(8, 10)).intValue() < 1 || Integer.valueOf(date.substring(8, 10)).intValue() > 30) {
                return false;
            }
        }
        if (Integer.valueOf(date.substring(5, 7)).intValue() == 1 || Integer.valueOf(date.substring(5, 7)).intValue() == 3 || Integer.valueOf(date.substring(5, 7)).intValue() == 5 || Integer.valueOf(date.substring(5, 7)).intValue() == 7 || Integer.valueOf(date.substring(5, 7)).intValue() == 9 || Integer.valueOf(date.substring(5, 7)).intValue() == 10 || Integer.valueOf(date.substring(5, 7)).intValue() == 12) {
            if (Integer.valueOf(date.substring(8, 10)).intValue() < 1 || Integer.valueOf(date.substring(8, 10)).intValue() > 31) {
                return false;
            }
        }
        if (Integer.valueOf(date.substring(5, 7)).intValue() == 2) {
            if (Integer.valueOf(date.substring(0, 4)).intValue() % 4 == 0) {
                if (Integer.valueOf(date.substring(8, 10)).intValue() < 1 || Integer.valueOf(date.substring(8, 10)).intValue() > 29) {
                    return false;
                }
            } else if (Integer.valueOf(date.substring(8, 10)).intValue() < 1 || Integer.valueOf(date.substring(8, 10)).intValue() > 28) {
                return false;
            }
        }
        return true;
    }

    private Task saveTask(Task editTask) {
        Task tempTask = new Task();
        if (editTask != null) {
            editTask.setEndDate(txtTaskEnd.getText());
            editTask.setName(txtTaskDescr.getText());
            editTask.setSection(mediator.ClassroomOptionsTab.getSection());
            editTask.setStartDate(txtTaskStart.getText());
            editTask.setTDate(txtTaskDate.getText());
            editTask.setType(cboTaskType.getSelectedItem().toString());
            mediator.dbInterface.WriteTask(editTask);
            return (editTask);
        } else {
            tempTask.setEndDate(txtTaskEnd.getText());
            tempTask.setName(txtTaskDescr.getText());
            tempTask.setSection(mediator.ClassroomOptionsTab.getSection());
            tempTask.setStartDate(txtTaskStart.getText());
            tempTask.setTDate(txtTaskDate.getText());
            tempTask.setType(cboTaskType.getSelectedItem().toString());
            mediator.dbInterface.WriteTask(tempTask);
            return (tempTask);
        }
    }

    private void saveFile(Task paramTask) {
        FauntleroyFile tempFile = new FauntleroyFile();
        tempFile = mediator.dbInterface.fileTaskQuery(paramTask.getTask_ID());
        tempFile.setAvailable(chkAvailable.isSelected());
        tempFile.setDescription(txtFileDescr.getText().trim());
        tempFile.setLogin("INSTRUCTOR");
        tempFile.setPath(placeFile(txtFilePath.getText()));
        System.out.println(tempFile.getPath());
        tempFile.setTask_ID(paramTask.getTask_ID());
        tempFile.setTitle(paramTask.getName());
        tempFile.setAvailable(chkAvailable.isSelected());
        mediator.dbInterface.WriteFile(tempFile);
    }

    public void activate(Task paramTask, boolean paramEdit) {
        thisTask = paramTask;
        edit = paramEdit;
        initGUI();
        if (paramTask != null) {
            refreshTxtBoxes(paramTask);
        } else {
            refreshTxtBoxes(null);
        }
    }

    private void refreshTxtBoxes(Task paramTask) {
        if (paramTask != null) {
            FauntleroyFile tempFile = mediator.dbInterface.fileTaskQuery(paramTask.getTask_ID());
            txtTaskDate.setText(paramTask.getTDate());
            txtTaskStart.setText(paramTask.getStartDate());
            txtTaskEnd.setText(paramTask.getEndDate());
            cboTaskType.setSelectedItem(paramTask.getType());
            txtTaskDescr.setText(paramTask.getName());
            txtFilePath.setText(mediator.getCurDir() + tempFile.getPath());
            txtFileDescr.setText(tempFile.getDescription());
            chkAvailable.setSelected(tempFile.getAvailable());
        } else {
            txtTaskDate.setText("");
            txtTaskStart.setText("");
            txtTaskEnd.setText("");
            cboTaskType.setSelectedItem("");
            txtTaskDescr.setText("");
            txtFilePath.setText("");
            txtFileDescr.setText("");
            chkAvailable.setSelected(false);
        }
    }

    private void initGUI() {
        try {
            FormLayout thisLayout = new FormLayout("5dlu, right:60dlu, 5dlu, 60dlu, 5dlu, right:60dlu, 5dlu, max(p;60dlu), 5dlu, max(p;60dlu), 5dlu, 60dlu", "max(p;5dlu), max(p;5dlu), 5dlu, max(p;5dlu), 5dlu, max(p;5dlu), 5dlu, max(p;15dlu), 5dlu, max(p;15dlu), 5dlu, max(p;15dlu), 5dlu, max(p;15dlu)");
            this.setLayout(thisLayout);
            {
                lblTaskDate = new JLabel();
                this.add(lblTaskDate, new CellConstraints("2, 2, 1, 1, default, default"));
                lblTaskDate.setText("Task Date");
            }
            {
                lblTaskStart = new JLabel();
                this.add(lblTaskStart, new CellConstraints("2, 4, 1, 1, default, default"));
                lblTaskStart.setText("Task Start Date");
            }
            {
                lblTaskStop = new JLabel();
                this.add(lblTaskStop, new CellConstraints("2, 6, 1, 1, default, default"));
                lblTaskStop.setText("Task End Date");
            }
            {
                txtTaskDate = new JTextField();
                this.add(txtTaskDate, new CellConstraints("4, 2, 1, 1, default, default"));
                txtTaskDate.setText("task date");
                txtTaskDate.setToolTipText("Date of creation   ie.   1886-08-16");
            }
            {
                txtTaskStart = new JTextField();
                this.add(txtTaskStart, new CellConstraints("4, 4, 1, 1, default, default"));
                txtTaskStart.setText("task start");
                txtTaskStart.setToolTipText("Date assignment should be issued   ie.   1886-08-16");
            }
            {
                txtTaskEnd = new JTextField();
                this.add(txtTaskEnd, new CellConstraints("4, 6, 1, 1, default, default"));
                txtTaskEnd.setText("task end");
                txtTaskEnd.setToolTipText("Last date of assignment availablity   ie.   1886-08-16");
            }
            {
                lblTaskType = new JLabel();
                this.add(lblTaskType, new CellConstraints("6, 2, 1, 1, default, default"));
                lblTaskType.setText("Task Type");
            }
            {
                lblTaskDesc = new JLabel();
                this.add(lblTaskDesc, new CellConstraints("6, 4, 1, 1, default, default"));
                lblTaskDesc.setText("Task Description");
            }
            {
                ComboBoxModel cboTaskTypeModel = new DefaultComboBoxModel(new String[] { "Handout", "Homework", "Quiz", "Test" });
                cboTaskType = new JComboBox();
                this.add(cboTaskType, new CellConstraints("8, 2, 1, 1, default, default"));
                cboTaskType.setModel(cboTaskTypeModel);
                cboTaskType.setEditable(false);
            }
            {
                txtTaskDescr = new JTextField();
                this.add(txtTaskDescr, new CellConstraints("8, 4, 3, 1, default, default"));
                txtTaskDescr.setText("task description");
                txtTaskDescr.setToolTipText("Short description of task");
            }
            {
                lblFilePath = new JLabel();
                this.add(lblFilePath, new CellConstraints("2, 10, 1, 1, default, default"));
                lblFilePath.setText("File Path");
            }
            {
                txtFilePath = new JTextField();
                this.add(txtFilePath, new CellConstraints("4, 10, 5, 1, default, default"));
                txtFilePath.setText("file path");
                txtFilePath.setEditable(false);
                txtFilePath.setFocusable(false);
                txtFilePath.setToolTipText("Relative file path   ie.   .\\Fauntleroy\\HW1.doc");
            }
            {
                lblFileDescr = new JLabel();
                this.add(lblFileDescr, new CellConstraints("2, 12, 1, 1, default, default"));
                lblFileDescr.setText("File Description");
            }
            {
                txtFileDescr = new JTextField();
                this.add(txtFileDescr, new CellConstraints("4, 12, 3, 1, default, default"));
                txtFileDescr.setText("file descripton");
                txtFileDescr.setToolTipText("Short description of file");
            }
            {
                btnBrowse = new JButton();
                this.add(btnBrowse, new CellConstraints("10, 10, 1, 1, default, default"));
                btnBrowse.setText("Browse");
                btnBrowse.setToolTipText("Browse for the file to be associated");
                btnBrowse.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent evt) {
                        System.out.println("File Browse button pressed.");
                        NewFileChooser = new JFileChooser(mediator.getCurDir());
                        int returnVal = NewFileChooser.showOpenDialog(AssignmentEdit.this);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            String file = mediator.molestPath(NewFileChooser.getSelectedFile().toString());
                            txtFilePath.setText(mediator.molestPath(file));
                            System.out.println(mediator.molestPath(file.replaceFirst(mediator.getCurDir(), "").trim()));
                        } else {
                            System.out.print("Open command cancelled by user.\n");
                        }
                    }
                });
            }
            {
                btnSaveApply = new JButton();
                this.add(btnSaveApply, new CellConstraints("10, 14, 1, 1, default, default"));
                if (edit == true) {
                    btnSaveApply.setText("Save");
                } else {
                    btnSaveApply.setText("Apply");
                }
                btnSaveApply.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent evt) {
                        System.out.println("SaveApply button pressed.");
                        if (errorCheckTextBoxes() == true) {
                            if (edit == true) {
                                saveFile(saveTask(null));
                            } else {
                                saveFile(saveTask(thisTask));
                            }
                            mediator.pullTab(mediator.AssignmentEditTab);
                            mediator.showLastTab();
                            mediator.ClassroomOptionsTab.refreshTaskTable();
                            mediator.deactivate(mediator.AssignmentEditTab);
                            mediator.ClassroomOptionsTab.tableVisibility(false, true);
                        }
                    }
                });
            }
            {
                btnCancel = new JButton();
                this.add(btnCancel, new CellConstraints("12, 14, 1, 1, default, default"));
                btnCancel.setText("Cancel");
                btnCancel.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent evt) {
                        System.out.println("Cancel button pressed.");
                        mediator.ClassroomOptionsTab.refreshTaskTable();
                        mediator.showLastTab();
                        mediator.pullTab(mediator.AssignmentEditTab);
                        mediator.deactivate(mediator.AssignmentEditTab);
                    }
                });
            }
            {
                chkAvailable = new JCheckBox();
                this.add(chkAvailable, new CellConstraints("4, 8, 3, 1, default, default"));
                chkAvailable.setText("Make this assignment available");
                chkAvailable.setVisible(true);
            }
            setSize(800, 600);
            this.setPreferredSize(new java.awt.Dimension(600, 300));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String placeFile(String fromName) {
        String fileTokens[];
        String toName;
        File fromFile, toFile;
        FileInputStream from;
        FileOutputStream to;
        fileTokens = fromName.split("/");
        toName = fileTokens[fileTokens.length - 1];
        fromFile = new File(fromName);
        toFile = new File("./files/" + toName);
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte buf[] = new byte[1024];
            int len;
            while ((len = from.read(buf)) > 0) {
                to.write(buf, 0, len);
            }
            to.close();
            from.close();
        } catch (IOException e) {
        }
        return toName;
    }
}
