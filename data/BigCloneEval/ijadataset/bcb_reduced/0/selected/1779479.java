package org.tolk.gui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.tolk.TolkInstance;
import org.tolk.constants.Constants;
import org.tolk.interfaces.TolkCustomClassInterface;
import sun.misc.Launcher;

public class InstancePanel extends JPanel implements ActionListener, KeyListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final JPanel parentContentPanel;

    private final JPanel inputPanel;

    private final JPanel dataEnginePanel;

    private final JPanel outputPanel;

    private final JPanel inputComboboxPanel;

    private final JPanel dataEngineComboboxPanel;

    private final JPanel outputComboboxPanel;

    private final JButton addInputButton;

    private final JButton addDataEngineButton;

    private final JButton addOutputButton;

    private int noInputs;

    private int noOutputs;

    private int noDataEngines;

    private static Map<String, String> helpTextMap;

    private static Map<String, List<String>> comboBoxValues;

    public InstancePanel(JPanel parentPanel) {
        super();
        helpTextMap = new HashMap<String, String>();
        comboBoxValues = new HashMap<String, List<String>>();
        this.noInputs = 1;
        this.noOutputs = 1;
        this.noDataEngines = 1;
        this.parentContentPanel = parentPanel;
        setLayout(new GridLayout(1, 4));
        this.addInputButton = new JButton(Constants.ADD);
        this.addInputButton.addActionListener(this);
        this.addDataEngineButton = new JButton(Constants.ADD);
        this.addDataEngineButton.addActionListener(this);
        this.addOutputButton = new JButton(Constants.ADD);
        this.addOutputButton.addActionListener(this);
        this.inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.inputComboboxPanel = new JPanel(new GridLayout(1, 1));
        this.inputComboboxPanel.add(getNewComboBox(Constants.INPUT));
        this.inputPanel.add(new JCheckBox());
        this.inputPanel.add(this.inputComboboxPanel);
        this.inputPanel.add(this.addInputButton);
        this.add(this.inputPanel);
        this.dataEnginePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.dataEngineComboboxPanel = new JPanel(new GridLayout(1, 1));
        this.dataEngineComboboxPanel.add(getNewComboBox(Constants.DATA_ENGINE));
        this.dataEnginePanel.add(this.dataEngineComboboxPanel);
        this.dataEnginePanel.add(this.addDataEngineButton);
        this.add(this.dataEnginePanel);
        this.outputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.outputComboboxPanel = new JPanel(new GridLayout(1, 1));
        this.outputComboboxPanel.add(getNewComboBox(Constants.OUTPUT));
        this.outputPanel.add(this.outputComboboxPanel);
        this.outputPanel.add(this.addOutputButton);
        this.add(this.outputPanel);
    }

    public void setupInstancePanel(TolkInstance instance) {
        setupType(instance.getInputs(), this.inputComboboxPanel, Constants.INPUT);
        setupType(instance.getDataEngines(), this.dataEngineComboboxPanel, Constants.DATA_ENGINE);
        setupType(instance.getOutputs(), this.outputComboboxPanel, Constants.OUTPUT);
    }

    private void setupType(Collection<TolkCustomClassInterface> instanceCollection, JPanel comboboxPanel, String type) {
        boolean firstOne = true;
        for (TolkCustomClassInterface customClass : instanceCollection) {
            String className = customClass.getClass().getName();
            className = className.substring(className.lastIndexOf(Constants.DOT) + 1);
            JComboBox comboBox = null;
            if (firstOne) {
                comboBox = (JComboBox) comboboxPanel.getComponent(0);
                firstOne = false;
            } else {
                comboBox = getNewComboBox(type);
                comboboxPanel.add(comboBox);
                if (type.equals(Constants.INPUT)) {
                    this.noInputs++;
                } else if (type.equals(Constants.OUTPUT)) {
                    this.noOutputs++;
                } else if (type.equals(Constants.DATA_ENGINE)) {
                    this.noDataEngines++;
                }
            }
            for (int i = 0; i < comboBox.getItemCount(); i++) {
                String item = (String) comboBox.getItemAt(i);
                if (item.startsWith(className)) {
                    item = className + " " + customClass.getCurrentParms();
                    comboBox.addItem(item);
                    comboBox.setSelectedItem(item);
                    break;
                }
            }
        }
        if (type.equals(Constants.INPUT)) {
            comboboxPanel.setLayout(new GridLayout(this.noInputs, 1));
        } else if (type.equals(Constants.OUTPUT)) {
            comboboxPanel.setLayout(new GridLayout(this.noOutputs, 1));
        } else if (type.equals(Constants.DATA_ENGINE)) {
            comboboxPanel.setLayout(new GridLayout(this.noDataEngines, 1));
        }
        comboboxPanel.repaint();
    }

    public String getInputsXmlString() {
        StringBuffer xmlString = new StringBuffer("");
        for (int i = 0; i < this.noInputs; i++) {
            JComboBox comboBox = (JComboBox) this.inputComboboxPanel.getComponent(i);
            String selectedItem = (String) comboBox.getSelectedItem();
            int firstSpacePos = selectedItem.indexOf(" ");
            String className = selectedItem.substring(0, firstSpacePos);
            String parms = selectedItem.substring(firstSpacePos + 1, selectedItem.length());
            xmlString.append(Constants.INPUT_OPEN_TAG + className + Constants.XML_CLOSE_TAG + parms + Constants.INPUT_CLOSE_TAG);
        }
        return xmlString.toString();
    }

    public String getOutputsXmlString() {
        StringBuffer xmlString = new StringBuffer("");
        for (int i = 0; i < this.noOutputs; i++) {
            JComboBox comboBox = (JComboBox) this.outputComboboxPanel.getComponent(i);
            String selectedItem = (String) comboBox.getSelectedItem();
            int firstSpacePos = selectedItem.indexOf(" ");
            String className = selectedItem.substring(0, firstSpacePos);
            String parms = selectedItem.substring(firstSpacePos + 1, selectedItem.length());
            xmlString.append(Constants.OUTPUT_OPEN_TAG + className + Constants.XML_CLOSE_TAG + parms + Constants.OUTPUT_CLOSE_TAG);
        }
        return xmlString.toString();
    }

    public String getDataEnginesXmlString() {
        StringBuffer xmlString = new StringBuffer("");
        for (int i = 0; i < this.noDataEngines; i++) {
            JComboBox comboBox = (JComboBox) this.dataEngineComboboxPanel.getComponent(i);
            String selectedItem = (String) comboBox.getSelectedItem();
            int firstSpacePos = selectedItem.indexOf(" ");
            String className = selectedItem.substring(0, firstSpacePos);
            String parms = selectedItem.substring(firstSpacePos + 1, selectedItem.length());
            xmlString.append(Constants.DATA_ENGINE_OPEN_TAG + className + Constants.XML_CLOSE_TAG + parms + Constants.DATA_ENGINE_CLOSE_TAG);
        }
        return xmlString.toString();
    }

    @SuppressWarnings("unchecked")
    private void addClassNamesToCombobox(String type, JComboBox comboBox) {
        List values = comboBoxValues.get(type);
        if (values != null) {
            for (Iterator<String> iterator = values.iterator(); iterator.hasNext(); ) {
                String value = iterator.next();
                comboBox.addItem(value);
            }
            return;
        } else {
            values = new ArrayList();
            comboBoxValues.put(type, values);
        }
        String packageName = null;
        if (type.equals(Constants.INPUT)) {
            packageName = Constants.INPUT_PACKAGE_NAME;
        } else if (type.equals(Constants.DATA_ENGINE)) {
            packageName = Constants.DATA_ENGINE_PACKAGE_NAME;
        } else if (type.equals(Constants.OUTPUT)) {
            packageName = Constants.OUTPUT_PACKAGE_NAME;
        }
        String folderPath = "/" + packageName;
        folderPath = folderPath.replace('.', '/');
        URL url = Launcher.class.getResource(folderPath);
        File directory = new File(url.getFile());
        if (directory.exists()) {
            String[] files = directory.list();
            for (int i = 0; i < files.length; i++) {
                if (files[i].endsWith(Constants.CLASS_EXTENSION) && !files[i].contains(Constants.INNERCLASS_SYMBOL)) {
                    String classname = files[i].substring(0, files[i].length() - 6);
                    String defaultParms = "";
                    try {
                        Class myclass = Class.forName(packageName + classname);
                        Constructor constructor = myclass.getConstructor();
                        Object myObject = constructor.newInstance();
                        Method getDefaultParmsMethod = myclass.getMethod(Constants.DEFAULT_PARMS_METHOD_NAME);
                        defaultParms = (String) getDefaultParmsMethod.invoke(myObject);
                        Method getHelpTextMethod = myclass.getMethod(Constants.HELP_TEXT_METHOD_NAME);
                        String helpText = (String) getHelpTextMethod.invoke(myObject);
                        helpTextMap.put(classname, helpText);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    comboBox.addItem(classname + " " + defaultParms);
                    values.add(classname + " " + defaultParms);
                }
            }
        }
    }

    private JComboBox getNewComboBox(String type) {
        JComboBox comboBox = new JComboBox();
        addClassNamesToCombobox(type, comboBox);
        comboBox.addItem(Constants.REMOVE_THIS + type);
        comboBox.setEditable(true);
        comboBox.addActionListener(this);
        JTextComponent editor = (JTextComponent) comboBox.getEditor().getEditorComponent();
        editor.addKeyListener(this);
        return comboBox;
    }

    private void removeCombobox(JComboBox comboBox, String type) {
        if (type.equals(Constants.INPUT)) {
            if (this.noInputs == 1) {
                comboBox.setSelectedIndex(0);
            } else {
                this.inputComboboxPanel.remove(comboBox);
                this.noInputs--;
                this.inputComboboxPanel.setLayout(new GridLayout(this.noInputs, 1));
            }
        } else if (type.equals(Constants.OUTPUT)) {
            if (this.noOutputs == 1) {
                comboBox.setSelectedIndex(0);
            } else {
                this.outputComboboxPanel.remove(comboBox);
                this.noOutputs--;
                this.outputComboboxPanel.setLayout(new GridLayout(this.noOutputs, 1));
            }
        } else if (type.equals(Constants.DATA_ENGINE)) {
            if (this.noDataEngines == 1) {
                comboBox.setSelectedIndex(0);
            } else {
                this.dataEngineComboboxPanel.remove(comboBox);
                this.noDataEngines--;
                this.dataEngineComboboxPanel.setLayout(new GridLayout(this.noDataEngines, 1));
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object sourceObject = e.getSource();
        if (sourceObject == this.addInputButton) {
            this.inputComboboxPanel.add(getNewComboBox(Constants.INPUT));
            this.noInputs++;
            this.inputComboboxPanel.setLayout(new GridLayout(this.noInputs, 1));
        } else if (sourceObject == this.addOutputButton) {
            this.outputComboboxPanel.add(getNewComboBox(Constants.OUTPUT));
            this.noOutputs++;
            this.outputComboboxPanel.setLayout(new GridLayout(this.noOutputs, 1));
        } else if (sourceObject == this.addDataEngineButton) {
            this.dataEngineComboboxPanel.add(getNewComboBox(Constants.DATA_ENGINE));
            this.noDataEngines++;
            this.dataEngineComboboxPanel.setLayout(new GridLayout(this.noDataEngines, 1));
        } else if (sourceObject instanceof JComboBox) {
            JComboBox comboBox = (JComboBox) sourceObject;
            String selectedItem = (String) comboBox.getSelectedItem();
            if (selectedItem.startsWith(Constants.REMOVE_THIS)) {
                removeCombobox(comboBox, selectedItem.substring(Constants.REMOVE_THIS.length()));
                selectedItem = (String) comboBox.getItemAt(0);
            }
        }
        this.parentContentPanel.updateUI();
    }

    public String getLastSelectedItemHelpTextForType(String type) {
        JComboBox comboBox = null;
        if (type.equals(Constants.DATA_ENGINE.toUpperCase())) {
            comboBox = (JComboBox) this.dataEngineComboboxPanel.getComponent(this.noDataEngines - 1);
        } else if (type.equals(Constants.INPUT.toUpperCase())) {
            comboBox = (JComboBox) this.inputComboboxPanel.getComponent(this.noInputs - 1);
        } else if (type.equals(Constants.OUTPUT.toUpperCase())) {
            comboBox = (JComboBox) this.outputComboboxPanel.getComponent(this.noOutputs - 1);
        }
        String selectedClassName = (String) comboBox.getSelectedItem();
        selectedClassName = selectedClassName.substring(0, selectedClassName.indexOf(" "));
        return helpTextMap.get(selectedClassName);
    }

    private void handleKeyEvent(KeyEvent e) {
        JTextComponent textComponent = (JTextComponent) e.getSource();
        String selectedItem = textComponent.getText();
        if (selectedItem.startsWith(Constants.REMOVE_THIS)) {
            e.consume();
        }
        int index = selectedItem.indexOf(" ");
        int keyLocation = textComponent.getCaretPosition();
        if (keyLocation <= index) {
            e.consume();
            textComponent.setCaretPosition(index + 1);
        }
    }

    public void keyTyped(KeyEvent e) {
        handleKeyEvent(e);
    }

    public void keyPressed(KeyEvent e) {
        handleKeyEvent(e);
    }

    public void keyReleased(KeyEvent e) {
        handleKeyEvent(e);
    }
}
