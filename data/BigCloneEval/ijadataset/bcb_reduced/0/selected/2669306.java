package com.weonline.shortcut.ui.control;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import com.weonline.shortcut.ShortcutMain;
import com.weonline.shortcut.ui.model.GuiComponentNames;
import com.weonline.shortcut.ui.view.SettingsFrame;
import com.jeta.forms.components.panel.FormPanel;

/**
 * A class that implements the control for Buffer checker
 * @author jdelcroix
 *
 */
public class ControlComboBox {

    /** Application Main Panel	 */
    private FormPanel panel;

    /** Get prefs for settings */
    Preferences prefs = Preferences.userNodeForPackage(SettingsFrame.class);

    /** Type 
	 * 0 : no action
	 * 1 : Normal
	 * 2 : Button 1
	 * 3 : Button 2
	 * 4 : Button 3
	 * 5 : Special key
	 * 6 : Einkaufsteil
	 * 7 : ULP
	 * 8 : Costumer
	 * 9 : Article in Fxxxxx
	 * */
    int type = 0;

    String command = "";

    /**
	 * Default constructor
	 * @param panel Application Main Panel
	 */
    public ControlComboBox(FormPanel panel) {
        this.panel = panel;
        JComboBox jCombo = panel.getFormAccessor().getComboBox(GuiComponentNames.ID_COMBOBOX);
        Component c = jCombo.getEditor().getEditorComponent();
        JTextField textField = (JTextField) c;
        textField.addKeyListener(new ActionFolder());
        panel.getButton(GuiComponentNames.ID_FUBUTTON).addActionListener(new ActionFUFolder());
        panel.getButton(GuiComponentNames.ID_PDBUTTON).addActionListener(new ActionPDFolder());
        panel.getButton(GuiComponentNames.ID_ULPBUTTON).addActionListener(new ActionULPFolder());
        panel.getButton(GuiComponentNames.ID_CLIPBOARD_BUTTON).addActionListener(new ActionClipBoard());
    }

    public String GetFolder(String command, int typeOp) {
        String path = new String("");
        String option = new String("");
        File test = null;
        switch(typeOp) {
            case 1:
                path = "P:\\projekt\\" + command.substring(0, 1) + "\\" + command.substring(0, 2) + "\\" + command.substring(0, 3) + "\\" + command.substring(0, command.length());
                break;
            case 2:
                option = prefs.get("TF_PATH1", "\\7_Produktionsunterlagen\\1_Fertigungsunterlagen");
                test = new File(option);
                if (test.isDirectory() || test.isFile()) {
                    path = option;
                } else if (command.length() != 0 && type != 0) {
                    path = "P:\\projekt\\" + command.substring(0, 1) + "\\" + command.substring(0, 2) + "\\" + command.substring(0, 3) + "\\" + command.substring(0, command.length()) + option;
                }
                break;
            case 3:
                option = prefs.get("TF_PATH2", "\\6_ULP");
                test = new File(option);
                if (test.isDirectory() || test.isFile()) {
                    path = option;
                } else if (command.length() != 0 && type != 0) {
                    path = "P:\\projekt\\" + command.substring(0, 1) + "\\" + command.substring(0, 2) + "\\" + command.substring(0, 3) + "\\" + command.substring(0, command.length()) + option;
                }
                break;
            case 4:
                option = prefs.get("TF_PATH3", "\\3_ProjektDaten");
                test = new File(option);
                if (test.isDirectory() || test.isFile()) {
                    path = option;
                } else if (command.length() != 0 && type != 0) {
                    path = "P:\\projekt\\" + command.substring(0, 1) + "\\" + command.substring(0, 2) + "\\" + command.substring(0, 3) + "\\" + command.substring(0, command.length()) + option;
                }
                break;
            case 5:
                for (int i = 0; i < 15; i++) {
                    if (command.equals(prefs.get("KEY" + i, "").toLowerCase())) {
                        path = prefs.get("PATH" + i, "");
                        break;
                    }
                }
                break;
            case 6:
                path = "P:\\Einkaufsteile\\" + command.substring(0, 1) + "\\" + command.substring(0, 6);
                break;
            case 7:
                path = "P:\\projekt\\" + command.substring(1, 2) + "\\" + command.substring(1, 3) + "\\" + command.substring(1, 4) + "\\" + command.substring(1, 6) + "\\6_ULP";
                break;
            case 8:
                path = "Not supported yet";
                break;
            case 9:
                path = "P:\\projekt\\" + command.substring(1, 2) + "\\" + command.substring(1, 3) + "\\" + command.substring(1, 4) + "\\" + command.substring(1, command.length());
                break;
        }
        return path;
    }

    public void LaunchPath(String path) {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                try {
                    File test = new File(path);
                    if (test.isDirectory() || test.isFile()) {
                        desktop.open(new File(path));
                    } else {
                        ShortcutMain.getInstance().showMessage("Does not exist : " + path);
                    }
                } catch (IOException ex) {
                    System.out.println("IOException  ::  " + ex);
                } catch (IllegalArgumentException ex) {
                    System.out.println("IllegalArgumentException  ::  " + ex);
                }
            }
        }
    }

    public void TreatString() {
        type = 0;
        command = command.toLowerCase();
        for (int i = 0; i < 15; i++) {
            if ((command.length() != 0) && (command.equals(prefs.get("KEY" + i, "").toLowerCase()))) {
                type = 5;
                return;
            }
        }
        if (command.length() == 5) {
            Pattern p = Pattern.compile("(9|8|7|6|1)\\d\\d\\d\\d");
            Matcher m = p.matcher(command);
            boolean b = m.matches();
            if (b) {
                type = 1;
                return;
            }
        } else if (command.length() == 6) {
            Pattern p = Pattern.compile("(v|x|s|k|p|a|u|f)\\d\\d\\d\\d\\d");
            Matcher m = p.matcher(command);
            boolean b = m.matches();
            if (b) {
                if (command.substring(0, 1).equals("u")) {
                    type = 7;
                } else if (command.substring(0, 1).equals("f")) {
                    type = 9;
                } else {
                    type = 6;
                }
                return;
            }
            boolean b2 = Pattern.matches("\\d\\d\\d\\d\\d\\d", command);
            if (b2 && command.substring(0, 1).equals("2")) {
                type = 8;
                return;
            } else if (b2 && command.substring(0, 1).equals("1")) {
                type = 1;
                return;
            }
        } else type = 0;
        return;
    }

    public void OrganiseComboList(String folder) {
        JComboBox jCombo = panel.getFormAccessor().getComboBox(GuiComponentNames.ID_COMBOBOX);
        Object comboContent = null;
        String[] monTableau = { "", "", "", "", "", "", "", "", "", "", "" };
        for (int i = 0; i < 10; i++) {
            comboContent = jCombo.getItemAt(i);
            monTableau[0] = jCombo.getSelectedItem().toString();
            if (comboContent != null) {
                String number = comboContent.toString();
                monTableau[i + 1] = number;
            }
        }
        jCombo.removeAllItems();
        jCombo.setModel(new javax.swing.DefaultComboBoxModel(monTableau));
    }

    /**
	 * Class used to browse a file on the Buffer checker Panel
	 * @author jdelcroix
	 *
	 */
    public class ActionFolder implements KeyListener {

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == 10) {
                JComboBox jCombo = panel.getFormAccessor().getComboBox(GuiComponentNames.ID_COMBOBOX);
                Object comboContent = jCombo.getSelectedItem();
                if (comboContent == null) {
                    return;
                }
                command = comboContent.toString();
                TreatString();
                if (command != null) {
                    String path = "";
                    path = GetFolder(command, type);
                    LaunchPath(path);
                    OrganiseComboList(command);
                }
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }
    }

    /**
	 * Class used to browse a file on the Buffer checker Panel
	 * @author jdelcroix
	 *
	 */
    public class ActionFUFolder implements ActionListener {

        public void actionPerformed(ActionEvent arg0) {
            JComboBox jCombo = panel.getFormAccessor().getComboBox(GuiComponentNames.ID_COMBOBOX);
            Object comboContent = jCombo.getSelectedItem();
            if (comboContent == null) {
                return;
            }
            command = comboContent.toString();
            TreatString();
            if (command != null) {
                String path = "";
                path = GetFolder(command, 2);
                LaunchPath(path);
                OrganiseComboList(command);
            }
        }
    }

    /**
	 * Class used to browse a file on the Buffer checker Panel
	 * @author jdelcroix
	 *
	 */
    public class ActionPDFolder implements ActionListener {

        public void actionPerformed(ActionEvent arg0) {
            JComboBox jCombo = panel.getFormAccessor().getComboBox(GuiComponentNames.ID_COMBOBOX);
            Object comboContent = jCombo.getSelectedItem();
            if (comboContent == null) {
                return;
            }
            command = comboContent.toString();
            TreatString();
            if (command != null) {
                String path = "";
                path = GetFolder(command, 4);
                LaunchPath(path);
                OrganiseComboList(command);
            }
        }
    }

    /**
	 * Class used to browse a file on the Buffer checker Panel
	 * @author jdelcroix
	 *
	 */
    public class ActionULPFolder implements ActionListener {

        public void actionPerformed(ActionEvent arg0) {
            JComboBox jCombo = panel.getFormAccessor().getComboBox(GuiComponentNames.ID_COMBOBOX);
            Object comboContent = jCombo.getSelectedItem();
            if (comboContent == null) {
                return;
            }
            command = comboContent.toString();
            TreatString();
            if (command != null) {
                String path = "";
                path = GetFolder(command, 3);
                LaunchPath(path);
                OrganiseComboList(command);
            }
        }
    }

    /**
	 * Class used to browse a file on the Buffer checker Panel
	 * @author jdelcroix
	 *
	 */
    public class ActionClipBoard implements ActionListener {

        public void actionPerformed(ActionEvent arg0) {
            JComboBox jCombo = panel.getFormAccessor().getComboBox(GuiComponentNames.ID_COMBOBOX);
            Object comboContent = jCombo.getSelectedItem();
            if (comboContent == null) {
                return;
            }
            command = comboContent.toString();
            TreatString();
            if (command != null) {
                String path = GetFolder(command, type);
                StringSelection test = new StringSelection(path);
                Clipboard clipBoard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipBoard.setContents(test, null);
            }
        }
    }
}
