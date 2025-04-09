import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;

public class GroupEditDialog extends JDialog {

    private JPanel myPanel = null;

    JTextField inputGroupName = null;

    JTextField inputFileName = null;

    private JButton okButton = null;

    private JButton cancelButton = null;

    private JButton browseButton = null;

    private GroupItem group = null;

    public GroupEditDialog(JFrame frame, boolean modal, GroupItem inputGroup) {
        super(frame, modal);
        group = inputGroup;
        if (group == null) {
            this.setTitle("Add New Group");
        } else {
            this.setTitle("Edit Group Information");
        }
        myPanel = new JPanel();
        SpringLayout layout = new SpringLayout();
        myPanel.setLayout(layout);
        getContentPane().add(myPanel);
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onOkButton();
            }
        });
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onCancelButton();
            }
        });
        browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onBrowseButton();
            }
        });
        JLabel lblGroupName = new JLabel("Group Name:");
        JLabel lblFileName = new JLabel("Associated File:");
        inputGroupName = new JTextField(20);
        inputFileName = new JTextField(20);
        if (group != null) {
            inputGroupName.setText(group.getGroupName());
            inputFileName.setText(group.getFileName());
        }
        myPanel.add(okButton);
        myPanel.add(cancelButton);
        myPanel.add(browseButton);
        myPanel.add(okButton);
        myPanel.add(lblGroupName);
        myPanel.add(lblFileName);
        myPanel.add(inputGroupName);
        myPanel.add(inputFileName);
        layout.putConstraint(SpringLayout.WEST, lblGroupName, 5, SpringLayout.WEST, myPanel);
        layout.putConstraint(SpringLayout.NORTH, lblGroupName, 5, SpringLayout.NORTH, myPanel);
        layout.putConstraint(SpringLayout.WEST, inputGroupName, 0, SpringLayout.WEST, lblGroupName);
        layout.putConstraint(SpringLayout.NORTH, inputGroupName, 5, SpringLayout.SOUTH, lblGroupName);
        layout.putConstraint(SpringLayout.WEST, lblFileName, 0, SpringLayout.WEST, inputGroupName);
        layout.putConstraint(SpringLayout.NORTH, lblFileName, 5, SpringLayout.SOUTH, inputGroupName);
        layout.putConstraint(SpringLayout.WEST, inputFileName, 0, SpringLayout.WEST, lblFileName);
        layout.putConstraint(SpringLayout.NORTH, inputFileName, 5, SpringLayout.SOUTH, lblFileName);
        layout.putConstraint(SpringLayout.EAST, browseButton, -5, SpringLayout.EAST, myPanel);
        layout.putConstraint(SpringLayout.NORTH, browseButton, 5, SpringLayout.SOUTH, inputFileName);
        layout.putConstraint(SpringLayout.EAST, cancelButton, -5, SpringLayout.EAST, myPanel);
        layout.putConstraint(SpringLayout.SOUTH, cancelButton, -5, SpringLayout.SOUTH, myPanel);
        layout.putConstraint(SpringLayout.EAST, okButton, -5, SpringLayout.WEST, cancelButton);
        layout.putConstraint(SpringLayout.NORTH, okButton, 0, SpringLayout.NORTH, cancelButton);
        SpringLayout.Constraints browseConstraints = layout.getConstraints(browseButton);
        SpringLayout.Constraints inputFileConstraints = layout.getConstraints(inputFileName);
        SpringLayout.Constraints inputGroupConstraints = layout.getConstraints(inputGroupName);
        SpringLayout.Constraints okConstraints = layout.getConstraints(okButton);
        SpringLayout.Constraints cancelConstraints = layout.getConstraints(cancelButton);
        browseConstraints.setHeight(inputFileConstraints.getHeight());
        okConstraints.setHeight(inputFileConstraints.getHeight());
        cancelConstraints.setHeight(inputFileConstraints.getHeight());
        Spring dialogWidth = Spring.sum(browseConstraints.getX(), Spring.minus(Spring.constant(5)));
        dialogWidth = Spring.sum(dialogWidth, browseConstraints.getWidth());
        inputFileConstraints.setWidth(dialogWidth);
        inputGroupConstraints.setWidth(dialogWidth);
        Dimension minimumSize = new Dimension(300, 200);
        myPanel.setMinimumSize(minimumSize);
        myPanel.setPreferredSize(minimumSize);
        myPanel.setMaximumSize(minimumSize);
        this.setMinimumSize(minimumSize);
        this.setPreferredSize(minimumSize);
        this.setMaximumSize(minimumSize);
        this.setLocationRelativeTo(frame);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public GroupItem getGroup() {
        return group;
    }

    private void onBrowseButton() {
        JFileChooser chooser = null;
        if (inputFileName.getText().length() > 0) {
            File currentFile = new File(inputFileName.getText());
            if (currentFile.exists()) {
                chooser = new JFileChooser(currentFile);
            }
        }
        if (chooser == null) {
            if (MailSorterFrame.getCurrentFile() == null) {
                chooser = new JFileChooser();
            } else {
                chooser = new JFileChooser(MailSorterFrame.getCurrentFile());
            }
        }
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            inputFileName.setText(selectedFile.getAbsolutePath());
        }
    }

    private void onOkButton() {
        String newGroupName = inputGroupName.getText();
        String newFileName = inputFileName.getText();
        if (newGroupName.length() == 0 || newFileName.length() == 0) {
            JOptionPane.showMessageDialog(this, "Both the Group Name and the Associated File cannot be empty.", "Invalid Empty Value", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Vector<GroupItem> groups = MailSorterPanel.groups;
        for (int index = 0; index < groups.size(); ++index) {
            GroupItem curGroup = groups.elementAt(index);
            if (curGroup == group) continue;
            if (curGroup.getGroupName().equals(newGroupName)) {
                JOptionPane.showMessageDialog(this, "Cannot create a group with the same name as an existing group", "Group Exists", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        if (group == null) {
            group = new GroupItem(null, null);
        }
        group.setGroupName(inputGroupName.getText());
        group.setFileName(inputFileName.getText());
        setVisible(false);
    }

    private void onCancelButton() {
        setVisible(false);
    }
}
