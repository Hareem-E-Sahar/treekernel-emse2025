package net.sourceforge.ondex.ovtk2.ui.gds;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.ovtk2.config.Config;
import net.sourceforge.ondex.ovtk2.config.OVTK2PluginLoader;
import net.sourceforge.ondex.ovtk2.ui.dialog.DialogAttribute;

/**
 * Panel displaying AttributeName information and GDSEditor.
 * 
 * @author taubertj
 */
public class AttributePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 5100753014972731016L;

    private ONDEXGraph aog = null;

    private Attribute attribute = null;

    private DialogAttribute dialog = null;

    private JTextField id = null;

    private JTextField full = null;

    private JTextField desc = null;

    private JTextField classField = null;

    private JComboBox unit = null;

    private JComboBox specialisationOf = null;

    private JCheckBox doIndex = null;

    private JScrollPane editorScroll = null;

    private JComponent editor = null;

    private JPanel anPanel = null;

    private JPanel indexPanel = null;

    /**
	 * Creates a panel displaying the attribute name and value editor.
	 * 
	 * @param dialog
	 *            parent DialogAttribute
	 * @param aog
	 *            AbstractONDEXGraph
	 * @param attribute
	 *            ConceptAttribute or RelationAttribute
	 */
    public AttributePanel(DialogAttribute dialog, ONDEXGraph aog, Attribute attribute) {
        this.dialog = dialog;
        this.aog = aog;
        this.attribute = attribute;
        initUI(attribute == null);
    }

    /**
	 * Return AttributeName id.
	 * 
	 * @return String
	 */
    public String getID() {
        return id.getText();
    }

    /**
	 * Return AttributeName fullname.
	 * 
	 * @return String
	 */
    public String getFullname() {
        return full.getText();
    }

    /**
	 * Return AttributeName description.
	 * 
	 * @return String
	 */
    public String getDescription() {
        return desc.getText();
    }

    /**
	 * Return AttributeName unit id.
	 * 
	 * @return String
	 */
    public String getUnit() {
        return (String) unit.getSelectedItem();
    }

    /**
	 * Return AttributeName specialisationOf id.
	 * 
	 * @return String
	 */
    public String getSpecialisationOf() {
        return (String) specialisationOf.getSelectedItem();
    }

    /**
	 * Constructs the UI for this Attribute.
	 */
    private void initUI(boolean editable) {
        this.setLayout(new BorderLayout());
        String anID = "";
        String anFull = "";
        String anDesc = "";
        String className = "";
        Unit selectedUnit = null;
        AttributeName selectedAn = null;
        if (!editable) {
            AttributeName an = attribute.getOfType();
            anID = an.getId();
            anFull = an.getFullname();
            anDesc = an.getDescription();
            className = an.getDataTypeAsString();
            selectedUnit = an.getUnit();
            selectedAn = an.getSpecialisationOf();
        }
        anPanel = new JPanel();
        BoxLayout contentLayout = new BoxLayout(anPanel, BoxLayout.PAGE_AXIS);
        anPanel.setLayout(contentLayout);
        TitledBorder anBorder = BorderFactory.createTitledBorder(Config.language.getProperty("Dialog.Attribute.AttributeName"));
        anPanel.setBorder(anBorder);
        JPanel idPanel = new JPanel(new BorderLayout());
        idPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.AttributeName.ID")), BorderLayout.WEST);
        id = new JTextField(anID);
        id.setBackground(dialog.getRequiredColor());
        id.setPreferredSize(new Dimension(dialog.getFieldWidth(), dialog.getFieldHeight()));
        id.setEnabled(editable);
        idPanel.add(id, BorderLayout.EAST);
        anPanel.add(idPanel);
        JPanel fullPanel = new JPanel(new BorderLayout());
        fullPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.AttributeName.FullName")), BorderLayout.WEST);
        full = new JTextField(anFull);
        full.setPreferredSize(new Dimension(dialog.getFieldWidth(), dialog.getFieldHeight()));
        fullPanel.add(full, BorderLayout.EAST);
        anPanel.add(fullPanel);
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.AttributeName.Description")), BorderLayout.WEST);
        desc = new JTextField(anDesc);
        desc.setPreferredSize(new Dimension(dialog.getFieldWidth(), dialog.getFieldHeight()));
        descPanel.add(desc, BorderLayout.EAST);
        anPanel.add(descPanel);
        JPanel classPanel = new JPanel(new BorderLayout());
        classPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.AttributeName.Class")), BorderLayout.WEST);
        classField = new JTextField(className);
        classField.setBackground(dialog.getRequiredColor());
        classField.setPreferredSize(new Dimension(dialog.getFieldWidth(), dialog.getFieldHeight()));
        classField.setEnabled(editable);
        classPanel.add(classField, BorderLayout.EAST);
        anPanel.add(classPanel);
        JPanel unitPanel = new JPanel(new BorderLayout());
        unitPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.AttributeName.Unit")), BorderLayout.WEST);
        unit = dialog.makeUnit(selectedUnit);
        unitPanel.add(unit, BorderLayout.EAST);
        anPanel.add(unitPanel);
        JPanel specialisationOfPanel = new JPanel(new BorderLayout());
        specialisationOfPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.AttributeName.SpecialisationOf")), BorderLayout.WEST);
        specialisationOf = dialog.makeSpecialisationOf(selectedAn);
        specialisationOfPanel.add(specialisationOf, BorderLayout.EAST);
        anPanel.add(specialisationOfPanel);
        if (editable) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(anPanel, BorderLayout.CENTER);
            JButton toggle = new JButton(Config.language.getProperty("Dialog.Attribute.AttributeName.ToggleHide"));
            toggle.setActionCommand("toggleHide");
            toggle.addActionListener(this);
            panel.add(toggle, BorderLayout.SOUTH);
            this.add(panel, BorderLayout.NORTH);
            JPanel buttonPanel = new JPanel(new BorderLayout());
            JButton create = new JButton(Config.language.getProperty("Dialog.Attribute.AttributeName.Create"));
            create.setActionCommand("create");
            create.addActionListener(this);
            buttonPanel.add(create, BorderLayout.WEST);
            JButton select = new JButton(Config.language.getProperty("Dialog.Attribute.AttributeName.Select"));
            select.setActionCommand("select");
            select.addActionListener(this);
            buttonPanel.add(select, BorderLayout.EAST);
            anPanel.add(buttonPanel);
            indexPanel = null;
            editorScroll = new JScrollPane(new JTable());
            this.add(editorScroll, BorderLayout.CENTER);
        } else {
            JButton toggle = new JButton(Config.language.getProperty("Dialog.Attribute.AttributeName.ToggleShow"));
            toggle.setActionCommand("toggleShow");
            toggle.addActionListener(this);
            this.add(toggle, BorderLayout.NORTH);
            indexPanel = new JPanel(new BorderLayout());
            indexPanel.add(new JLabel(Config.language.getProperty("Dialog.Attribute.DoIndex")), BorderLayout.WEST);
            doIndex = new JCheckBox();
            doIndex.setSelected(attribute.isDoIndex());
            doIndex.setActionCommand("index");
            doIndex.addActionListener(this);
            indexPanel.add(doIndex, BorderLayout.EAST);
            this.add(indexPanel, BorderLayout.SOUTH);
            editor = findEditor(attribute);
            if (editor != null) {
                editorScroll = new JScrollPane(editor);
                this.add(editorScroll, BorderLayout.CENTER);
            } else {
                String[] columnNames = { Config.language.getProperty("Dialog.Attribute.EditorError") };
                String[][] data = new String[][] { columnNames };
                editorScroll = new JScrollPane(new JTable(data, columnNames));
                this.add(editorScroll, BorderLayout.CENTER);
            }
        }
    }

    /**
	 * Validate data entry.
	 * 
	 * @return true if data is valid
	 */
    private boolean validateEntry() {
        if (id.getText().trim().length() == 0 || id.getText().contains(" ")) {
            JOptionPane.showInternalMessageDialog(dialog, Config.language.getProperty("Dialog.Attribute.AttributeName.InvalidID"), Config.language.getProperty("Dialog.Attribute.AttributeName.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (aog.getMetaData().checkAttributeName(id.getText())) {
            JOptionPane.showInternalMessageDialog(dialog, Config.language.getProperty("Dialog.Attribute.AttributeName.DuplicateID"), Config.language.getProperty("Dialog.Attribute.AttributeName.DuplicateTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (classField.getText().trim().length() == 0) {
            JOptionPane.showInternalMessageDialog(dialog, Config.language.getProperty("Dialog.Attribute.AttributeName.InvalidClass"), Config.language.getProperty("Dialog.Attribute.AttributeName.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
    public void actionPerformed(ActionEvent arg0) {
        String cmd = arg0.getActionCommand();
        if (cmd.equals("create") && validateEntry()) {
            try {
                AttributeName an = aog.getMetaData().getFactory().createAttributeName(id.getText(), full.getText(), desc.getText(), Class.forName(classField.getText()));
                if (!unit.getSelectedItem().equals("")) {
                    Unit anUnit = aog.getMetaData().getUnit((String) unit.getSelectedItem());
                    an.setUnit(anUnit);
                }
                if (!specialisationOf.getSelectedItem().equals("")) {
                    AttributeName anAn = aog.getMetaData().getAttributeName((String) specialisationOf.getSelectedItem());
                    an.setSpecialisationOf(anAn);
                }
                dialog.createNewAttribute(an, true);
            } catch (ClassNotFoundException cnfe) {
                JOptionPane.showInternalMessageDialog(dialog, Config.language.getProperty("Dialog.Attribute.AttributeName.InvalidClass"), Config.language.getProperty("Dialog.Attribute.AttributeName.InvalidTitle"), JOptionPane.ERROR_MESSAGE);
            }
        } else if (cmd.equals("select")) {
            Vector<String> ans = new Vector<String>();
            for (AttributeName an : aog.getMetaData().getAttributeNames()) {
                ans.add(an.getId());
            }
            String name = (String) JOptionPane.showInternalInputDialog(dialog, Config.language.getProperty("Dialog.Attribute.AttributeName.SelectText"), Config.language.getProperty("Dialog.Attribute.AttributeName.SelectTitle"), JOptionPane.PLAIN_MESSAGE, dialog.getFrameIcon(), ans.toArray(new String[0]), null);
            if ((name != null) && (name.length() > 0)) {
                AttributeName an = aog.getMetaData().getAttributeName(name);
                dialog.createNewAttribute(an, false);
            }
        } else if (cmd.equals("index")) {
            attribute.setDoIndex(doIndex.isSelected());
        } else if (cmd.equals("toggleShow")) {
            this.removeAll();
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(anPanel, BorderLayout.CENTER);
            JButton toggle = new JButton(Config.language.getProperty("Dialog.Attribute.AttributeName.ToggleHide"));
            toggle.setActionCommand("toggleHide");
            toggle.addActionListener(this);
            panel.add(toggle, BorderLayout.SOUTH);
            this.add(panel, BorderLayout.NORTH);
            this.add(editorScroll, BorderLayout.CENTER);
            if (indexPanel != null) this.add(indexPanel, BorderLayout.SOUTH);
            this.updateUI();
        } else if (cmd.equals("toggleHide")) {
            this.removeAll();
            JButton toggle = new JButton(Config.language.getProperty("Dialog.Attribute.AttributeName.ToggleShow"));
            toggle.setActionCommand("toggleShow");
            toggle.addActionListener(this);
            this.add(toggle, BorderLayout.NORTH);
            this.add(editorScroll, BorderLayout.CENTER);
            if (indexPanel != null) this.add(indexPanel, BorderLayout.SOUTH);
            this.updateUI();
        }
    }

    /**
	 * forces changes in progress to be flushed to attribute
	 */
    public void flushChanges() {
        ((GDSEditor) editor).flushChanges();
    }

    /**
	 * Load GDSEditor for displaying purpose
	 * 
	 * @param attribute
	 */
    public static JComponent findEditor(Attribute attribute) {
        JComponent editor = null;
        AttributeName an = attribute.getOfType();
        String className = an.getDataTypeAsString();
        String editorName = Config.config.getProperty("Dialog.Attribute.Editor." + attribute.getOfType().getId());
        if (editorName == null) editorName = Config.config.getProperty("Dialog.Attribute." + className);
        if (editorName == null) editorName = "DefaultEditor";
        try {
            Class<?> editorClass = null;
            try {
                editorClass = Class.forName("net.sourceforge.ondex.ovtk2.ui.gds." + editorName);
            } catch (ClassNotFoundException cnfe) {
            }
            if (editorClass == null) {
                editor = (JComponent) OVTK2PluginLoader.getInstance().loadAttributeEditor(editorName, attribute);
            } else {
                Class<?>[] args = new Class<?>[] { Attribute.class };
                Constructor<?> constr = editorClass.getConstructor(args);
                editor = (JComponent) constr.newInstance(attribute);
            }
            if (!(editor instanceof GDSEditor)) {
                throw new RuntimeException(editor.getClass().getName() + " does not implement required " + GDSEditor.class + " interface");
            }
            editor.setMinimumSize(new Dimension(100, 100));
            editor.setPreferredSize(new Dimension(100, 100));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return editor;
    }
}
