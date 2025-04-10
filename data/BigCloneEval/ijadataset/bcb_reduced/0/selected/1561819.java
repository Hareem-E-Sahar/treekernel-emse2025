package jmri.jmrit.symbolicprog;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.Document;

/**
 * Extends VariableValue to represent an indexed variable
 *
 * @author    Howard G. Penny   Copyright (C) 2005
 * @author    Bob Jacobsen   Copyright (C) 2010
 * @version   $Revision: 1.22 $
 */
public class IndexedVariableValue extends VariableValue implements ActionListener, PropertyChangeListener, FocusListener {

    public IndexedVariableValue(int row, String name, String comment, String cvName, boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly, int cvNum, String mask, int minVal, int maxVal, Vector<CvValue> v, JLabel status, String stdname) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        if (log.isDebugEnabled()) log.debug("ctor with cvName " + cvName + ", cvNum " + cvNum);
        _row = row;
        _maxVal = maxVal;
        _minVal = minVal;
        _value = new JTextField("0", 3);
        _defaultColor = _value.getBackground();
        CvValue cv = (_cvVector.elementAt(_row));
        if (log.isDebugEnabled()) log.debug("cv found as " + cv);
        cv.addPropertyChangeListener(this);
        if (cv.getInfoOnly()) {
            cv.setState(CvValue.READ);
        } else {
            cv.setState(CvValue.FROMFILE);
        }
    }

    /**
     * Create a null object.  Normally only used for tests and to pre-load classes.
     */
    protected IndexedVariableValue() {
    }

    int _row;

    int _maxVal;

    int _minVal;

    public void setToolTipText(String t) {
        super.setToolTipText(t);
        _value.setToolTipText(t);
    }

    public CvValue[] usesCVs() {
        return new CvValue[] { _cvVector.elementAt(_row) };
    }

    public Object rangeVal() {
        return "Decimal: " + _minVal + " - " + _maxVal;
    }

    String oldContents = "";

    void enterField() {
        oldContents = _value.getText();
    }

    void exitField() {
        if (_value != null && !oldContents.equals(_value.getText())) {
            int newVal = (Integer.valueOf(_value.getText()).intValue());
            int oldVal = (Integer.valueOf(oldContents).intValue());
            updatedTextField();
            prop.firePropertyChange("Value", Integer.valueOf(oldVal), Integer.valueOf(newVal));
        }
    }

    void updatedTextField() {
        if (log.isDebugEnabled()) log.debug("enter updatedTextField");
        CvValue cv = _cvVector.elementAt(_row);
        if (log.isDebugEnabled()) log.debug("updatedTextField refs CV " + cv);
        int oldVal = cv.getValue();
        int newVal;
        try {
            newVal = Integer.valueOf(_value.getText()).intValue();
        } catch (java.lang.NumberFormatException ex) {
            newVal = 0;
        }
        int newCv = newValue(oldVal, newVal, getMask());
        if (oldVal != newVal) cv.setValue(newCv);
    }

    /** ActionListener implementations */
    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) log.debug("actionPerformed");
        int newVal = (Integer.valueOf(_value.getText()).intValue());
        updatedTextField();
        prop.firePropertyChange("Value", null, Integer.valueOf(newVal));
    }

    /** FocusListener implementations */
    public void focusGained(FocusEvent e) {
        if (log.isDebugEnabled()) log.debug("focusGained");
        enterField();
    }

    public void focusLost(FocusEvent e) {
        if (log.isDebugEnabled()) log.debug("focusLost");
        exitField();
    }

    public String getValueString() {
        return _value.getText();
    }

    public void setIntValue(int i) {
        setValue(i);
    }

    public int getIntValue() {
        return (Integer.valueOf(_value.getText()).intValue());
    }

    public Object getValueObject() {
        return Integer.valueOf(_value.getText());
    }

    public Component getCommonRep() {
        if (getReadOnly()) {
            JLabel r = new JLabel(_value.getText());
            updateRepresentation(r);
            return r;
        } else return _value;
    }

    public Component getNewRep(String format) {
        if (format.equals("vslider")) {
            IndexedVarSlider b = new IndexedVarSlider(this, _minVal, _maxVal);
            b.setOrientation(JSlider.VERTICAL);
            sliders.add(b);
            updateRepresentation(b);
            if (!getAvailable()) b.setVisible(false);
            return b;
        } else if (format.equals("hslider")) {
            IndexedVarSlider b = new IndexedVarSlider(this, _minVal, _maxVal);
            b.setOrientation(JSlider.HORIZONTAL);
            if (_maxVal > 20) {
                b.setMajorTickSpacing(_maxVal / 2);
                b.setMinorTickSpacing((_maxVal + 1) / 8);
            } else {
                b.setMajorTickSpacing(5);
                b.setMinorTickSpacing(1);
                b.setSnapToTicks(true);
            }
            b.setSize(b.getWidth(), 28);
            Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
            labelTable.put(Integer.valueOf(0), new JLabel("Off"));
            if (_maxVal == 63) {
                labelTable.put(Integer.valueOf(_maxVal / 2), new JLabel("25%"));
                labelTable.put(Integer.valueOf(_maxVal), new JLabel("50%"));
            } else {
                labelTable.put(Integer.valueOf(_maxVal / 2), new JLabel("50%"));
                labelTable.put(Integer.valueOf(_maxVal), new JLabel("100%"));
            }
            b.setLabelTable(labelTable);
            b.setPaintTicks(true);
            b.setPaintLabels(true);
            sliders.add(b);
            updateRepresentation(b);
            if (!getAvailable()) b.setVisible(false);
            return b;
        } else {
            VarTextField value = new VarTextField(_value.getDocument(), _value.getText(), 3, this);
            if (getReadOnly() || getInfoOnly()) {
                value.setEditable(false);
            }
            valuereps.add(value);
            updateRepresentation(value);
            if (!getAvailable()) value.setVisible(false);
            return value;
        }
    }

    public void setAvailable(boolean a) {
        for (int i = 0; i < sliders.size(); i++) sliders.get(i).setVisible(a);
        for (int i = 0; i < valuereps.size(); i++) valuereps.get(i).setVisible(a);
        super.setAvailable(a);
    }

    ArrayList<IndexedVarSlider> sliders = new ArrayList<IndexedVarSlider>();

    ArrayList<VarTextField> valuereps = new ArrayList<VarTextField>();

    private int _progState = 0;

    private static final int IDLE = 0;

    private static final int WRITING_PI4R = 1;

    private static final int WRITING_PI4W = 2;

    private static final int WRITING_SI4R = 3;

    private static final int WRITING_SI4W = 4;

    private static final int READING_CV = 5;

    private static final int WRITING_CV = 6;

    private static final int WRITING_PI4C = 7;

    private static final int WRITING_SI4C = 8;

    private static final int COMPARE_CV = 9;

    /**
     * Count number of retries done
     */
    private int retries = 0;

    /**
     * Define maximum number of retries of read/write operations before moving on
     */
    private static final int RETRY_MAX = 2;

    /**
     * Set a new value, including notification as needed.  This does the
     * conversion from string to int, so if the place where formatting
     * needs to be applied
     */
    public void setValue(int value) {
        int oldVal;
        try {
            oldVal = (Integer.valueOf(_value.getText()).intValue());
        } catch (java.lang.NumberFormatException ex) {
            oldVal = -999;
        }
        if (log.isDebugEnabled()) log.debug("setValue with new value " + value + " old value " + oldVal);
        if (oldVal != value) {
            _value.setText("" + value);
            updatedTextField();
            prop.firePropertyChange("Value", Integer.valueOf(oldVal), Integer.valueOf(value));
        }
    }

    Color _defaultColor;

    Color getColor() {
        return _value.getBackground();
    }

    void setColor(Color c) {
        if (c != null) _value.setBackground(c); else _value.setBackground(_defaultColor);
    }

    /**
     * Notify the connected CVs of a state change from above
     * @param state
     */
    public void setCvState(int state) {
        (_cvVector.elementAt(_row)).setState(state);
    }

    public void setToRead(boolean state) {
        if (getInfoOnly() || getWriteOnly() || !getAvailable()) state = false;
        (_cvVector.elementAt(_row)).setToRead(state);
    }

    public boolean isToRead() {
        return getAvailable() && (_cvVector.elementAt(_row)).isToRead();
    }

    public void setToWrite(boolean state) {
        if (getInfoOnly() || getReadOnly() || !getAvailable()) state = false;
        (_cvVector.elementAt(_row)).setToWrite(state);
    }

    public boolean isToWrite() {
        return getAvailable() && (_cvVector.elementAt(_row)).isToWrite();
    }

    public boolean isChanged() {
        CvValue cv = (_cvVector.elementAt(_row));
        return considerChanged(cv);
    }

    public void readChanges() {
        if (isChanged()) readAll();
    }

    public void writeChanges() {
        if (isChanged()) writeAll();
    }

    public void readAll() {
        setBusy(true);
        setToRead(false);
        if (_progState != IDLE) log.warn("Programming state " + _progState + ", not IDLE, in read()");
        if ((_cvVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4R;
        } else {
            _progState = WRITING_SI4R;
        }
        retries = 0;
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV read");
        (_cvVector.elementAt(_row)).writePI(_status);
    }

    public void writeAll() {
        if (getReadOnly()) {
            log.error("unexpected write operation when readOnly is set");
        }
        setBusy(true);
        setToWrite(false);
        if (_progState != IDLE) log.warn("Programming state " + _progState + ", not IDLE, in write()");
        if ((_cvVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4W;
        } else {
            _progState = WRITING_SI4W;
        }
        retries = 0;
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV write");
        (_cvVector.elementAt(_row)).writePI(_status);
    }

    public void confirmAll() {
        setBusy(true);
        setToRead(false);
        if (_progState != IDLE) log.warn("Programming state " + _progState + ", not IDLE, in read()");
        if ((_cvVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4C;
        } else {
            _progState = WRITING_SI4C;
        }
        retries = 0;
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV confirm");
        (_cvVector.elementAt(_row)).writePI(_status);
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("Property changed: " + e.getPropertyName());
        if (e.getPropertyName().equals("Busy") && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
            switch(_progState) {
                case IDLE:
                    if (log.isDebugEnabled()) log.error("Busy goes false with state IDLE");
                    return;
                case WRITING_PI4R:
                case WRITING_PI4C:
                case WRITING_PI4W:
                    if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_PI");
                    if ((retries < RETRY_MAX) && ((_cvVector.elementAt(_row)).getState() != CvValue.STORED)) {
                        log.debug("retry");
                        retries++;
                        (_cvVector.elementAt(_row)).writePI(_status);
                        return;
                    }
                    retries = 0;
                    if (_progState == WRITING_PI4R) _progState = WRITING_SI4R; else if (_progState == WRITING_PI4C) _progState = WRITING_SI4C; else _progState = WRITING_SI4W;
                    (_cvVector.elementAt(_row)).writeSI(_status);
                    return;
                case WRITING_SI4R:
                case WRITING_SI4C:
                case WRITING_SI4W:
                    if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_SI");
                    if ((retries < RETRY_MAX) && ((_cvVector.elementAt(_row)).getState() != CvValue.STORED)) {
                        log.debug("retry");
                        retries++;
                        (_cvVector.elementAt(_row)).writeSI(_status);
                        return;
                    }
                    retries = 0;
                    if (_progState == WRITING_SI4R) {
                        _progState = READING_CV;
                        (_cvVector.elementAt(_row)).readIcV(_status);
                    } else if (_progState == WRITING_SI4C) {
                        _progState = COMPARE_CV;
                        (_cvVector.elementAt(_row)).confirmIcV(_status);
                    } else {
                        _progState = WRITING_CV;
                        (_cvVector.elementAt(_row)).writeIcV(_status);
                    }
                    return;
                case READING_CV:
                    if (log.isDebugEnabled()) log.debug("Finished reading the Indexed CV");
                    if ((retries < RETRY_MAX) && ((_cvVector.elementAt(_row)).getState() != CvValue.READ)) {
                        log.debug("retry");
                        retries++;
                        (_cvVector.elementAt(_row)).readIcV(_status);
                        return;
                    }
                    retries = 0;
                    _progState = IDLE;
                    setBusy(false);
                    return;
                case COMPARE_CV:
                    if (log.isDebugEnabled()) log.debug("Finished reading the Indexed CV for compare");
                    if ((retries < RETRY_MAX) && ((_cvVector.elementAt(_row)).getState() != CvValue.SAME) && ((_cvVector.elementAt(_row)).getState() != CvValue.DIFF)) {
                        log.debug("retry");
                        retries++;
                        (_cvVector.elementAt(_row)).confirmIcV(_status);
                        return;
                    }
                    retries = 0;
                    _progState = IDLE;
                    setBusy(false);
                    return;
                case WRITING_CV:
                    if (log.isDebugEnabled()) log.debug("Finished writing the Indexed CV");
                    if ((retries < RETRY_MAX) && ((_cvVector.elementAt(_row)).getState() != CvValue.STORED)) {
                        log.debug("retry");
                        retries++;
                        (_cvVector.elementAt(_row)).writeIcV(_status);
                        return;
                    }
                    retries = 0;
                    _progState = IDLE;
                    super.setState(STORED);
                    setBusy(false);
                    return;
                default:
                    log.error("Unexpected state found: " + _progState);
                    _progState = IDLE;
                    return;
            }
        } else if (e.getPropertyName().equals("State")) {
            CvValue cv = _cvVector.elementAt(_row);
            setState(cv.getState());
        } else if (e.getPropertyName().equals("Value")) {
            CvValue cv = _cvVector.elementAt(_row);
            int newVal = (cv.getValue() & maskVal(getMask())) >>> offsetVal(getMask());
            setValue(newVal);
        }
    }

    JTextField _value = null;

    public class VarTextField extends JTextField {

        VarTextField(Document doc, String text, int col, IndexedVariableValue var) {
            super(doc, text, col);
            _var = var;
            setBackground(_var._value.getBackground());
            addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    thisActionPerformed(e);
                }
            });
            addFocusListener(new java.awt.event.FocusListener() {

                public void focusGained(FocusEvent e) {
                    if (log.isDebugEnabled()) log.debug("focusGained");
                    enterField();
                }

                public void focusLost(FocusEvent e) {
                    if (log.isDebugEnabled()) log.debug("focusLost");
                    exitField();
                }
            });
            _var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    originalPropertyChanged(e);
                }
            });
        }

        IndexedVariableValue _var;

        void thisActionPerformed(java.awt.event.ActionEvent e) {
            _var.actionPerformed(e);
        }

        void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("State")) {
                setBackground(_var._value.getBackground());
            }
        }
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        if (_value != null) {
            _value.removeActionListener(this);
            _value.removeFocusListener(this);
            _value.removePropertyChangeListener(this);
            _value = null;
        }
        (_cvVector.elementAt(_row)).removePropertyChangeListener(this);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndexedVariableValue.class.getName());
}
