package com.hifiremote.jp1;

import java.awt.Color;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * The Class DeviceButtonTableModel.
 */
public class DeviceButtonTableModel extends JP1TableModel<DeviceButton> {

    /**
   * Instantiates a new device button table model.
   */
    public DeviceButtonTableModel() {
        deviceTypeEditor = new DeviceTypeEditor(deviceTypeBox, softHT);
        deviceTypeEditor.setClickCountToStart(RMConstants.ClickCountToStart);
        punchThroughEditor = new DefaultCellEditor(deviceButtonBox);
        punchThroughEditor.setClickCountToStart(RMConstants.ClickCountToStart);
        sequenceEditor = new DefaultCellEditor(sequenceBox);
        sequenceEditor.setClickCountToStart(RMConstants.ClickCountToStart);
    }

    /**
   * Sets the.
   * 
   * @param remoteConfig
   *          the remote config
   */
    public void set(RemoteConfiguration remoteConfig) {
        this.remoteConfig = remoteConfig;
        if (remoteConfig != null) {
            colorEditor = new RMColorEditor(remoteConfig.getOwner());
            Remote remote = remoteConfig.getRemote();
            setData(remote.getDeviceButtons());
            SoftDevices softDevices = remote.getSoftDevices();
            if (remote.getSoftHomeTheaterType() >= 0) {
                softHT.setUse(true);
                softHT.setDeviceType(remote.getSoftHomeTheaterType());
                softHT.setDeviceCode(remote.getSoftHomeTheaterCode());
            }
            DefaultComboBoxModel comboModel = new DefaultComboBoxModel(remote.getAllDeviceTypes());
            if (softDevices != null && softDevices.inUse() && !softDevices.isSetupCodesOnly()) {
                comboModel.addElement(new DeviceType("", 0, 0xFFFF));
            }
            deviceTypeBox.setModel(comboModel);
            comboModel = new DefaultComboBoxModel(remote.getDeviceButtons());
            comboModel.insertElementAt(DeviceButton.noButton, 0);
            deviceButtonBox.setModel(comboModel);
            if (softDevices != null && softDevices.usesSequence()) {
                adjustSequenceRange();
            }
            setupCodeRenderer = new SetupCodeRenderer(remoteConfig);
            setupCodeEditor = new SetupCodeEditor(setupCodeRenderer);
        }
    }

    private int getDeviceCount() {
        int len = 0;
        for (int i = 0; i < getRowCount(); i++) {
            if (getExtendedTypeIndex(i) != 0xFF) {
                len++;
            }
        }
        return len;
    }

    private void adjustSequenceRange() {
        int len = getDeviceCount();
        Integer[] values = new Integer[len];
        for (int i = 0; i < len; i++) {
            values[i] = i + 1;
        }
        sequenceBox.setModel(new DefaultComboBoxModel(values));
    }

    /**
   * Sets the editable.
   * 
   * @param flag
   *          the new editable
   */
    public void setEditable(boolean flag) {
        editable = flag;
    }

    public int getColumnCount() {
        int count = 5;
        if (remoteConfig != null) {
            Remote remote = remoteConfig.getRemote();
            if (remote.getDeviceLabels() != null) {
                ++count;
            }
            SoftDevices softDevices = remote.getSoftDevices();
            if (softDevices != null && softDevices.usesSequence()) {
                ++count;
            }
            if (remoteConfig.hasSegments()) {
                count += 3;
            }
            if (remoteConfig.allowHighlighting()) {
                ++count;
            }
        }
        return count;
    }

    private int getEffectiveColumn(int col) {
        if (remoteConfig != null) {
            Remote remote = remoteConfig.getRemote();
            if (!remoteConfig.hasSegments() && col > 3) {
                col += 3;
            }
            if (remote.getDeviceLabels() == null && col >= 8) {
                col++;
            }
            SoftDevices softDevices = remote.getSoftDevices();
            if ((softDevices == null || !softDevices.usesSequence()) && col >= 9) {
                col++;
            }
        } else if (col > 3) {
            col += 3;
        }
        return col;
    }

    private int getExtendedTypeIndex(int row) {
        short[] data = getData(row);
        Remote remote = remoteConfig.getRemote();
        DeviceButton db = getRow(row);
        SoftDevices softDevices = remote.getSoftDevices();
        if (softDevices == null || softDevices.isSetupCodesOnly() || db.getDeviceSlot(data) != 0xFFFF) {
            return db.getDeviceTypeIndex(data);
        } else {
            return 0xFF;
        }
    }

    /** The Constant colNames. */
    private static final String[] colNames = { "#", "Device Button", "Type", "<html>Setup<br>Code</html>", "<html>Volume<br>PunchThrough</html>", "<html>Transport<br>PunchThrough</html>", "<html>Channel<br>PunchThrough</html>", "Note", "Label", "Seq", "<html>Size &amp<br>Color</html>" };

    @Override
    public String getColumnName(int col) {
        return colNames[getEffectiveColumn(col)];
    }

    /** The col prototype names. */
    private static String[] colPrototypeNames = { " 00 ", "Device Button", "__VCR/DVD__", "Setup", "PunchThrough_", "PunchThrough_", "PunchThrough_", "A Meaningful, Reasonable Note", "Label", "Seq", "Color_" };

    @Override
    public String getColumnPrototypeName(int col) {
        return colPrototypeNames[getEffectiveColumn(col)];
    }

    /** The Constant colClasses. */
    private static final Class<?>[] colClasses = { Integer.class, String.class, DeviceType.class, SetupCode.class, DeviceButton.class, DeviceButton.class, DeviceButton.class, String.class, String.class, Integer.class, Color.class };

    @Override
    public Class<?> getColumnClass(int col) {
        return colClasses[getEffectiveColumn(col)];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        SoftDevices softDevices = remoteConfig.getRemote().getSoftDevices();
        return editable && col > 1 && (col == 2 || col == 7 || getExtendedTypeIndex(row) != 0xFF) && (col != 3 || (softDevices != null && softDevices.isSetupCodesOnly()) || getValueAt(row, col) != null);
    }

    private short[] getData(int row) {
        short[] data = null;
        if (remoteConfig.hasSegments()) {
            DeviceButton db = getRow(row);
            data = db.getSegment().getHex().getData();
        } else {
            data = remoteConfig.getData();
        }
        return data;
    }

    public Object getValueAt(int row, int column) {
        Remote remote = remoteConfig.getRemote();
        DeviceButton db = getRow(row);
        short[] data = getData(row);
        int typeIndex = getExtendedTypeIndex(row);
        int group = db.getDeviceGroup(data);
        column = getEffectiveColumn(column);
        if (typeIndex == 0xFF && column > 1 && column < 7) {
            return null;
        }
        switch(column) {
            case 0:
                return new Integer(row + 1);
            case 1:
                return db.getName();
            case 2:
                {
                    return remote.getDeviceTypeByIndexAndGroup(typeIndex, group);
                }
            case 3:
                {
                    if (softHT.inUse() && typeIndex == softHT.getDeviceType()) {
                        return null;
                    }
                    short value = db.getSetupCode(data);
                    return value < 0 ? null : new SetupCode(value);
                }
            case 4:
                {
                    return db.getVolumePT();
                }
            case 5:
                {
                    return db.getTransportPT();
                }
            case 6:
                {
                    return db.getChannelPT();
                }
            case 7:
                {
                    String[] notes = remoteConfig.getDeviceButtonNotes();
                    String note = null;
                    if (notes != null) {
                        note = notes[row];
                    }
                    if (note == null) {
                        DeviceUpgrade deviceUpgrade = remoteConfig.getAssignedDeviceUpgrade(db);
                        if (deviceUpgrade != null) {
                            note = deviceUpgrade.getDescription();
                        }
                    }
                    if (note == null) {
                        return "";
                    } else {
                        return note;
                    }
                }
            case 8:
                {
                    DeviceLabels labels = remote.getDeviceLabels();
                    return labels.getText(data, row);
                }
            case 9:
                {
                    SoftDevices softDevices = remote.getSoftDevices();
                    int seq = softDevices.getSequencePosition(row, getRowCount(), data);
                    if (seq == -1) {
                        return null;
                    } else {
                        return seq + 1;
                    }
                }
            case 10:
                {
                    return db.getHighlight();
                }
            default:
                return null;
        }
    }

    public boolean hasInvalidCodes() {
        Remote remote = remoteConfig.getRemote();
        if (remote.getSetupValidation() == Remote.SetupValidation.OFF) {
            return false;
        }
        boolean result = false;
        for (int i = 0; i < remote.getDeviceButtons().length; i++) {
            DeviceButton deviceButton = remote.getDeviceButtons()[i];
            DeviceType deviceType = (DeviceType) getValueAt(i, 2);
            SetupCode setupCode = (SetupCode) getValueAt(i, 3);
            if (deviceType != null && setupCode != null) {
                setupCodeRenderer.setDeviceButton(deviceButton);
                setupCodeRenderer.setDeviceType(deviceType);
                result = result || !setupCodeRenderer.isValid(setupCode.getValue());
            }
        }
        return result;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        short[] data = getData(row);
        Remote remote = remoteConfig.getRemote();
        DeviceButton db = getRow(row);
        SoftDevices softDevices = remote.getSoftDevices();
        DeviceType oldDevType = null;
        SetupCode oldSetupCode = null;
        col = getEffectiveColumn(col);
        if (col == 2 || col == 3) {
            oldDevType = (DeviceType) getValueAt(row, 2);
            oldSetupCode = (SetupCode) getValueAt(row, 3);
        }
        if (col == 2) {
            DeviceType newDevType = (DeviceType) value;
            if (!isValidDevice(row, newDevType, oldSetupCode)) {
                return;
            }
            int newIndex = newDevType.getNumber();
            int newGroup = newDevType.getGroup();
            int oldIndex = getExtendedTypeIndex(row);
            int oldGroup = db.getDeviceGroup(data);
            DeviceLabels labels = remote.getDeviceLabels();
            if ((oldIndex == newIndex) && (oldGroup == -1 || oldGroup == newGroup)) {
                return;
            }
            if (isUpgradeWithKeymoves(row, oldDevType, oldSetupCode, true)) {
                preserveKeyMoves(row, oldDevType, oldSetupCode);
            }
            if (softHT.inUse() && newIndex == softHT.getDeviceType()) {
                db.zeroDeviceSlot(data);
                db.setDeviceTypeIndex((short) newIndex, data);
                db.setSetupCode((short) softHT.getDeviceCode(), data);
            } else {
                if (oldIndex == 0xFF) {
                    db.zeroDeviceSlot(data);
                }
                db.setDeviceTypeIndex((short) newIndex, data);
                db.setDeviceGroup((short) newGroup, data);
            }
            if (labels != null) {
                String name = newIndex == 0xFF ? "" : remote.getDeviceTypeByIndex(newIndex).getName();
                labels.setText(name, row, data);
                if (labels.usesDefaultLabels()) {
                    labels.setDefaultText(name, row, data);
                }
            }
            if (softDevices != null && softDevices.usesFilledSlotCount()) {
                softDevices.setFilledSlotCount(getDeviceCount(), data);
            }
            if (softDevices != null && softDevices.usesSequence()) {
                adjustSequenceRange();
                if (oldIndex == 0xFF) {
                    softDevices.setSequenceIndex(row, sequenceBox.getItemCount() - 1, data);
                } else if (newIndex == 0xFF) {
                    softDevices.deleteSequenceIndex(row, getRowCount(), data);
                }
            }
        } else if (col == 3) {
            SetupCode newSetupCode = null;
            if (value.getClass() == String.class) {
                newSetupCode = new SetupCode((String) value, softDevices != null && softDevices.isSetupCodesOnly());
            } else {
                newSetupCode = (SetupCode) value;
            }
            if (oldSetupCode != null) {
                if (newSetupCode.getValue() == oldSetupCode.getValue() || !isValidDevice(row, oldDevType, newSetupCode)) {
                    return;
                }
                if (isUpgradeWithKeymoves(row, oldDevType, oldSetupCode, true)) {
                    preserveKeyMoves(row, oldDevType, oldSetupCode);
                }
            }
            db.setSetupCode((short) newSetupCode.getValue(), data);
        } else if (col == 4) {
            db.setVolumePT((DeviceButton) value);
        } else if (col == 5) {
            db.setTransportPT((DeviceButton) value);
        } else if (col == 6) {
            db.setChannelPT((DeviceButton) value);
        } else if (col == 7) {
            String strValue = ((String) value).trim();
            if ("".equals(strValue)) {
                strValue = null;
            }
            remoteConfig.getDeviceButtonNotes()[row] = strValue;
        } else if (col == 8) {
            remote.getDeviceLabels().setText((String) value, row, data);
        } else if (col == 9) {
            int rows = getRowCount();
            int newSeq = ((Integer) value).intValue() - 1;
            int oldSeq = softDevices.getSequencePosition(row, rows, data);
            if (newSeq == oldSeq) {
                return;
            }
            softDevices.deleteSequenceIndex(row, rows, data);
            softDevices.insertSequenceIndex(row, newSeq, rows, data);
            fireTableDataChanged();
        } else if (col == 10) {
            db.setHighlight((Color) value);
        }
        propertyChangeSupport.firePropertyChange(col == 7 ? "highlight" : "value", null, null);
    }

    private boolean isValidDevice(int row, DeviceType devType, SetupCode setupCode) {
        Remote remote = remoteConfig.getRemote();
        DeviceButton db = getRow(row);
        Button button = remote.getButton(db.getName());
        if (isUpgradeWithKeymoves(-1, devType, setupCode, false) && ((button != null && !button.allowsKeyMove()) || (row > 7 && remote.getAdvCodeBindFormat() == AdvancedCode.BindFormat.NORMAL))) {
            String message = "Device " + devType.getName() + " " + setupCode.getValue() + " cannot be assigned to\nbutton " + db.getName() + " as it is an upgrade " + "that contains\nkeymoves";
            String title = "Device Button Assignment";
            JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean isUpgradeWithKeymoves(int devBtnIndex, DeviceType devType, SetupCode setupCode, boolean ask) {
        if (devType != null && setupCode != null) {
            DeviceUpgrade du = remoteConfig.findDeviceUpgrade(devType.getNumber(), setupCode.getValue());
            if (du != null && du.getKeyMoves().size() > 0) {
                if (ask) {
                    String message = "The current device " + devType.getName() + " " + setupCode.getValue() + " contains keymoves.  Do you want to preserve them?";
                    String title = "Device Change";
                    boolean confirmed = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
                    if (!confirmed) {
                        du.assignmentColors.remove(devBtnIndex);
                    }
                    return confirmed;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private void preserveKeyMoves(int devButtonIndex, DeviceType devType, SetupCode setupCode) {
        if (devType == null || setupCode == null) {
            return;
        }
        DeviceUpgrade du = remoteConfig.findDeviceUpgrade(devType.getNumber(), setupCode.getValue());
        if (du == null) {
            return;
        }
        for (KeyMove keyMove : du.getKeyMoves(devButtonIndex)) {
            DeviceButton db = remoteConfig.getRemote().getDeviceButtons()[devButtonIndex];
            keyMove.setDeviceButtonIndex(db.getButtonIndex());
            remoteConfig.getKeyMoves().add(keyMove);
        }
    }

    @Override
    public TableCellRenderer getColumnRenderer(int col) {
        if (col == 0) {
            return new RowNumberRenderer();
        } else if (col == 3) {
            return setupCodeRenderer;
        } else if (getEffectiveColumn(col) == 10) {
            return colorRenderer;
        }
        return null;
    }

    @Override
    public TableCellEditor getColumnEditor(int col) {
        if (!editable) {
            return null;
        }
        switch(getEffectiveColumn(col)) {
            case 2:
                return deviceTypeEditor;
            case 3:
                return setupCodeEditor;
            case 4:
            case 5:
            case 6:
                return punchThroughEditor;
            case 7:
            case 8:
                return selectAllEditor;
            case 9:
                return sequenceEditor;
            case 10:
                return colorEditor;
            default:
                return null;
        }
    }

    /** The remote config. */
    private RemoteConfiguration remoteConfig = null;

    /** The device type box. */
    private DefaultCellEditor deviceTypeEditor = null;

    private JComboBox deviceTypeBox = new JComboBox();

    private DefaultCellEditor punchThroughEditor = null;

    private JComboBox deviceButtonBox = new JComboBox();

    /** The setup code editor */
    private SelectAllCellEditor selectAllEditor = new SelectAllCellEditor();

    private SetupCodeRenderer setupCodeRenderer = null;

    private SetupCodeEditor setupCodeEditor = null;

    private RMColorEditor colorEditor = null;

    private RMColorRenderer colorRenderer = new RMColorRenderer();

    private DefaultCellEditor sequenceEditor = null;

    private JComboBox sequenceBox = new JComboBox();

    /** The editable. */
    private boolean editable = true;

    private SoftHomeTheater softHT = new SoftHomeTheater();
}
