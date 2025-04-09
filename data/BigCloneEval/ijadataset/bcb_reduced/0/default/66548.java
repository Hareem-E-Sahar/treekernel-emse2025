import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import java.util.Date;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import javax.swing.event.*;

public class AntFarm extends JPanel implements DockableWindow, ActionListener, KeyListener {

    private HistoryTextField buildField = new HistoryTextField("build file");

    private JButton fileChooser = new JButton("Browse");

    private JLabel target = new JLabel("Target: ");

    private HistoryTextField targetField = new HistoryTextField("target");

    private JButton build = new JButton("Build");

    private JButton edit = new JButton("Edit");

    private JList buildResults;

    private DefaultListModel listModel = new DefaultListModel();

    private AntFarmPlugin parent;

    private View view;

    private static AntFarm antfarm;

    private AntFarm(AntFarmPlugin afp, View view) {
        parent = afp;
        this.view = view;
        setLayout(new BorderLayout());
        GridBagLayout gl = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel pane = new JPanel();
        pane.setLayout(gl);
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        JPanel leftPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.weightx = 0;
        c.insets = new Insets(1, 8, 1, 8);
        leftPanel.add(new JLabel("Build File:  "), c);
        c.gridx = 1;
        c.weightx = 100;
        c.fill = GridBagConstraints.HORIZONTAL;
        leftPanel.add(buildField, c);
        if (buildField.getModel().getSize() >= 1) buildField.setText(buildField.getModel().getItem(0));
        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        fileChooser.addActionListener(this);
        fileChooser.addKeyListener(this);
        leftPanel.add(fileChooser, c);
        JPanel rightPanel = new JPanel(new GridBagLayout());
        c.gridx = 0;
        c.weightx = 0;
        edit.addActionListener(this);
        edit.addKeyListener(this);
        rightPanel.add(edit, c);
        c.gridx = 1;
        c.weightx = 0;
        rightPanel.add(target, c);
        c.gridx = 2;
        c.weightx = 100;
        c.fill = GridBagConstraints.HORIZONTAL;
        rightPanel.add(targetField, c);
        if (targetField.getModel().getSize() >= 1) targetField.setText(targetField.getModel().getItem(0));
        c.gridx = 3;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        build.addActionListener(this);
        build.addKeyListener(this);
        build.setNextFocusableComponent(buildField);
        rightPanel.add(build, c);
        topPanel.add(leftPanel);
        topPanel.add(rightPanel);
        c.gridx = 0;
        c.weightx = 100;
        c.fill = GridBagConstraints.HORIZONTAL;
        pane.add(topPanel, c);
        buildResults = new JList(listModel);
        buildResults.setCellRenderer(new AntCellRenderer());
        buildResults.setRequestFocusEnabled(false);
        JScrollPane jsp = new JScrollPane(buildResults);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 100;
        c.weighty = 100;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 2, 2, 2);
        pane.add(jsp, c);
        add(BorderLayout.CENTER, pane);
    }

    public static synchronized AntFarm getAntFarm() {
        return antfarm;
    }

    public static synchronized AntFarm setAntFarm(AntFarmPlugin afp, View view) {
        antfarm = new AntFarm(afp, view);
        return antfarm;
    }

    public void appendToTextArea(String message) {
        appendToTextArea(message, buildResults.getForeground());
    }

    public void appendToTextArea(String message, Color color) {
        ListObject lo = new ListObject(message, color);
        listModel.addElement(lo);
    }

    private class ListObject {

        private String message;

        private Color color;

        ListObject(String message, Color color) {
            this.message = message;
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public String toString() {
            return message;
        }
    }

    public void build() {
        view.getDockableWindowManager().addDockableWindow(parent.NAME);
        listModel.removeAllElements();
        parent.clearErrors();
        String buildString = buildField.getText().trim();
        buildField.addCurrentToHistory();
        String targetString = targetField.getText().trim();
        targetField.addCurrentToHistory();
        File buildFile = new File(buildString);
        TargetExecutor executor = new TargetExecutor(parent, buildFile, targetString);
        try {
            executor.execute();
        } catch (Exception e) {
            System.out.println("Error executing build!");
            e.printStackTrace();
        }
    }

    public void edit() {
        String buildString = buildField.getText().trim();
        buildField.addCurrentToHistory();
        jEdit.openFile(view, null, buildField.getText(), false, false);
    }

    private void browse() {
        JFileChooser chooser = new JFileChooser(buildField.getText().trim());
        chooser.addChoosableFileFilter(new AntFileFilter());
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            setBuildFile(file);
        }
    }

    public void setBuildFile(File file) {
        buildField.setText(file.getAbsolutePath());
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == fileChooser) browse();
        if (source == build) build();
        if (source == edit) edit();
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        Object source = e.getSource();
        int keyCode = e.getKeyCode();
        if (source == build) if (keyCode == KeyEvent.VK_ENTER) build();
        if (source == fileChooser) if (keyCode == KeyEvent.VK_ENTER) browse();
        if (source == edit) if (keyCode == KeyEvent.VK_ENTER) edit();
    }

    public String getName() {
        return AntFarmPlugin.NAME;
    }

    public Component getComponent() {
        return this;
    }

    /**
   * This method is called when the dockable window is added to
   * the view, or closed if it is floating.
   */
    public void addNotify() {
        super.addNotify();
    }

    /**
   * This method is called when the dockable window is removed from
   * the view, or closed if it is floating.
   */
    public void removeNotify() {
        super.removeNotify();
    }

    private class AntCellRenderer extends JLabel implements ListCellRenderer {

        public AntCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String s = value.toString();
            setText(s);
            if (value instanceof ListObject) setForeground(((ListObject) value).getColor()); else setForeground(list.getForeground());
            setBackground(list.getBackground());
            setFont(list.getFont());
            return this;
        }
    }
}
