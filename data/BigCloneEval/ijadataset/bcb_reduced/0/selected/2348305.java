package jmri.jmrit.symbolicprog;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Vector;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Extends EnumVariableValue to represent a composition of variable values.
 * <P>
 * Internally, each "choice" is stored as a list of "setting" items.
 * Numerical values for this type of variable (itself) are strictly sequential,
 * because they are arbitrary.
 * <p>
 * This version of the class has certain limitations:
 *<OL>
 * <LI>Variables referenced in the definition of one of these must have
 * already been declared earlier in the decoder file.  This prevents
 * circular references, and makes it much easier to find the target variables.
 * <LI>
 * This version of the variable never changes "State" (color), though it does
 * track it's value from changes to other variables.
 * <LI>The should be a final choice (entry) that doesn't define any 
 * settings.  This will then form the default value when the target variables
 * change.
 * <LI>Programming operations on a variable of this type doesn't do anything, because
 * there doesn't seem to be a consistent model of what "read changes" and "write changes"
 * should do.  
 * This has two implications:
 *  <UL>
 *  <LI>Variables referenced as targets must appear on some programming pane,
 *      or they won't be updated by programming operations.
 *  <LI>If this variable references variables that are not on this pane,
 *      the user needs to do a read/write all panes operation to record
 *      the changes made to this variable.  
 *  </UL>
 *  It's therefore recommended that a CompositeVariableValue just make changes
 *  to target variables on the same programming page.
 *</ol>
 * <P>
 * @author	Bob Jacobsen   Copyright (C) 2001, 2005
 * @version	$Revision: 1.18 $
 *
 */
public class CompositeVariableValue extends EnumVariableValue implements ActionListener, PropertyChangeListener {

    public CompositeVariableValue(String name, String comment, String cvName, boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly, int cvNum, String mask, int minVal, int maxVal, Vector<CvValue> v, JLabel status, String stdname) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname);
        _maxVal = maxVal;
        _minVal = minVal;
        _value = new JComboBox();
        if (log.isDebugEnabled()) log.debug("New Composite named " + name);
    }

    /**
     * Create a null object.  Normally only used for tests and to pre-load classes.
     */
    public CompositeVariableValue() {
        _value = new JComboBox();
    }

    public CvValue[] usesCVs() {
        HashSet<CvValue> cvSet = new HashSet<CvValue>(20);
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            CvValue[] cvs = v.usesCVs();
            for (int k = 0; k < cvs.length; k++) cvSet.add(cvs[k]);
        }
        CvValue[] retval = new CvValue[cvSet.size()];
        Iterator<CvValue> j = cvSet.iterator();
        int index = 0;
        while (j.hasNext()) {
            retval[index++] = j.next();
        }
        return retval;
    }

    /**
     * Define objects to save and manipulate a particular setting
     */
    static class Setting {

        String varName;

        VariableValue variable;

        int value;

        Setting(String varName, VariableValue variable, String value) {
            this.varName = varName;
            this.variable = variable;
            this.value = Integer.valueOf(value).intValue();
            if (log.isDebugEnabled()) log.debug("    cTor Setting " + varName + " = " + value);
        }

        void setValue() {
            if (log.isDebugEnabled()) log.debug("    Setting.setValue of " + varName + " to " + value);
            variable.setIntValue(value);
        }

        boolean match() {
            if (log.isDebugEnabled()) log.debug("         Match checks " + variable.getIntValue() + " == " + value);
            return (variable.getIntValue() == value);
        }
    }

    /**
     * Defines a list of Setting objects.
     * <P> Serves as a home for various service methods
     */
    static class SettingList extends ArrayList<Setting> {

        public SettingList() {
            super();
            if (log.isDebugEnabled()) log.debug("New setting list");
        }

        void addSetting(String varName, VariableValue variable, String value) {
            Setting s = new Setting(varName, variable, value);
            add(s);
        }

        void setValues() {
            if (log.isDebugEnabled()) log.debug(" setValues in length " + size());
            for (int i = 0; i < this.size(); i++) {
                Setting s = this.get(i);
                s.setValue();
            }
        }

        boolean match() {
            for (int i = 0; i < size(); i++) {
                if (!this.get(i).match()) {
                    if (log.isDebugEnabled()) log.debug("      No match in setting list of length " + size() + " at position " + i);
                    return false;
                }
            }
            if (log.isDebugEnabled()) log.debug("      Match in setting list of length " + size());
            return true;
        }
    }

    Hashtable<String, SettingList> choiceHash = new Hashtable<String, SettingList>();

    HashSet<VariableValue> variables = new HashSet<VariableValue>(20);

    /**
     * Create a new possible selection.
     * @param name  Name of the choice being added
     */
    public void addChoice(String name) {
        SettingList l = new SettingList();
        choiceHash.put(name, l);
        _value.addItem(name);
    }

    /**
     * Add a setting to an existing choice.
     */
    public void addSetting(String choice, String varName, VariableValue variable, String value) {
        SettingList s = choiceHash.get(choice);
        s.addSetting(varName, variable, value);
        if (variable != null) {
            variables.add(variable);
            if (!variable.label().equals(varName)) log.warn("Unexpected label /" + variable.label() + "/ for varName /" + varName + "/ during addSetting");
        } else log.error("Variable pointer null when varName=" + varName + " in choice " + choice + "; ignored");
    }

    /** 
     * Do end of initialization processing.
     */
    @SuppressWarnings("null")
    public void lastItem() {
        _defaultColor = _value.getBackground();
        super.setState(READ);
        findValue();
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            if (v == null) log.error("Variable found as null in last item");
            v.addPropertyChangeListener(this);
        }
        _value.setActionCommand("");
        _value.addActionListener(this);
    }

    public void setToolTipText(String t) {
        super.setToolTipText(t);
        _value.setToolTipText(t);
    }

    public Object rangeVal() {
        return "composite: " + _minVal + " - " + _maxVal;
    }

    public void actionPerformed(ActionEvent e) {
        if (!(e.getActionCommand().equals(""))) {
            _value.setSelectedItem(e.getActionCommand());
        }
        if (log.isDebugEnabled()) log.debug("action event: " + e);
        prop.firePropertyChange("Value", null, Integer.valueOf(getIntValue()));
        selectValue(getIntValue());
    }

    /**
     * This variable doesn't change state, hence doesn't change color.
     */
    public void setState(int state) {
        if (log.isDebugEnabled()) log.debug("Ignore setState(" + state + ")");
    }

    /**
     * Set to a specific value.
     *<P>
     * Does this by delegating to the SettingList
     * @param value
     */
    protected void selectValue(int value) {
        if (log.isDebugEnabled()) log.debug("selectValue(" + value + ")");
        if (value > _value.getItemCount() - 1) {
            log.error("Saw unreasonable internal value: " + value);
            return;
        }
        String choice = (String) _value.getItemAt(value);
        SettingList sl = choiceHash.get(choice);
        sl.setValues();
    }

    public int getIntValue() {
        return _value.getSelectedIndex();
    }

    public Component getCommonRep() {
        return _value;
    }

    public void setValue(int value) {
        int oldVal = getIntValue();
        selectValue(value);
        if (oldVal != value || getState() == VariableValue.UNKNOWN) prop.firePropertyChange("Value", null, Integer.valueOf(value));
    }

    /**
     * Notify the connected CVs of a state change from above
     * by way of the variables (e.g. not direct to CVs)
     * @param state
     */
    public void setCvState(int state) {
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            v.setCvState(state);
        }
    }

    public boolean isChanged() {
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            if (v.isChanged()) return true;
        }
        return false;
    }

    public void setToRead(boolean state) {
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            v.setToRead(state);
        }
    }

    /**
     * This variable needs to be read if any of it's subsidiary 
     * variables needs to be read.
     */
    public boolean isToRead() {
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            if (v.isToRead()) return true;
        }
        return false;
    }

    public void setToWrite(boolean state) {
        if (log.isDebugEnabled()) log.debug("Start setToWrite with " + state);
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            v.setToWrite(state);
        }
        log.debug("End setToWrite");
    }

    /**
     * This variable needs to be written if any of it's subsidiary 
     * variables needs to be written.
     */
    public boolean isToWrite() {
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            if (v.isToWrite()) return true;
        }
        return false;
    }

    public void readChanges() {
        if (isChanged()) {
            readingChanges = true;
            amReading = true;
            continueRead();
        }
    }

    public void writeChanges() {
        if (isChanged()) {
            writingChanges = true;
            amWriting = true;
            continueWrite();
        }
    }

    public void readAll() {
        readingChanges = false;
        amReading = true;
        continueRead();
    }

    boolean amReading = false;

    boolean readingChanges = false;

    /**
     * See if there's anything to read, and if so do it.
     */
    protected void continueRead() {
        if (log.isDebugEnabled()) log.debug("Start continueRead");
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            if (v.isToRead() && (!readingChanges || v.isChanged())) {
                amReading = true;
                setBusy(true);
                if (readingChanges) v.readChanges(); else v.readAll();
                return;
            }
        }
        amReading = false;
        super.setState(READ);
        setBusy(false);
        log.debug("End continueRead, nothing to do");
    }

    public void writeAll() {
        if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
        writingChanges = false;
        amWriting = true;
        continueWrite();
    }

    boolean amWriting = false;

    boolean writingChanges = false;

    /**
     * See if there's anything to write, and if so do it.
     */
    protected void continueWrite() {
        if (log.isDebugEnabled()) log.debug("Start continueWrite");
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            if (v.isToWrite() && (!writingChanges || v.isChanged())) {
                amWriting = true;
                setBusy(true);
                log.debug("request write of " + v.label() + " writing changes " + writingChanges);
                if (writingChanges) v.writeChanges(); else v.writeAll();
                log.debug("return from starting write request");
                return;
            }
        }
        amWriting = false;
        super.setState(STORED);
        setBusy(false);
        log.debug("End continueWrite, nothing to do");
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("propertyChange in " + label() + " type " + e.getPropertyName() + " new value " + e.getNewValue());
        if (e.getPropertyName().equals("Busy")) {
            if (((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
                log.debug("busy change continues programming");
                if (amReading) {
                    continueRead();
                    return;
                } else if (amWriting) {
                    continueWrite();
                    return;
                }
            }
        } else if (e.getPropertyName().equals("Value")) {
            findValue();
        }
    }

    /**
     * Suspect underlying variables have changed value; check.
     * First match will succeed, so there should not be multiple 
     * matches possible. ("First match" is defined in 
     * choice-sequence)
     */
    void findValue() {
        if (log.isDebugEnabled()) log.debug("findValue invoked on " + label());
        for (int i = 0; i < _value.getItemCount(); i++) {
            String choice = (String) _value.getItemAt(i);
            SettingList sl = choiceHash.get(choice);
            if (sl.match()) {
                if (log.isDebugEnabled()) log.debug("  match in " + i);
                _value.setSelectedItem(choice);
                return;
            }
        }
        if (log.isDebugEnabled()) log.debug("   no match");
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            v.removePropertyChangeListener(this);
        }
        disposeReps();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CompositeVariableValue.class.getName());
}
