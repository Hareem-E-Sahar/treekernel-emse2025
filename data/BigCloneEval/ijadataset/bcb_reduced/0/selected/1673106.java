package jmri.jmrit.symbolicprog;

import jmri.progdebugger.ProgDebugger;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.util.*;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test LongAddrVariableValue class.
 *
 * @todo need a check of the MIXED state model for long address
 * @author	Bob Jacobsen Copyright 2001, 2002
 * @version $Revision: 17977 $
 */
public class LongAddrVariableValueTest extends VariableValueTest {

    ProgDebugger p = new ProgDebugger();

    VariableValue makeVar(String label, String comment, String cvName, boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly, int cvNum, String mask, int minVal, int maxVal, Vector<CvValue> v, JLabel status, String item) {
        CvValue cvNext = new CvValue(cvNum + 1, p);
        cvNext.setValue(0);
        v.setElementAt(cvNext, cvNum + 1);
        return new LongAddrVariableValue(label, comment, "", readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, item);
    }

    void setValue(VariableValue var, String val) {
        ((JTextField) var.getCommonRep()).setText(val);
        ((JTextField) var.getCommonRep()).postActionEvent();
    }

    void setReadOnlyValue(VariableValue var, String val) {
        ((LongAddrVariableValue) var).setValue(Integer.valueOf(val).intValue());
    }

    void checkValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, ((JTextField) var.getCommonRep()).getText());
    }

    void checkReadOnlyValue(VariableValue var, String comment, String val) {
        Assert.assertEquals(comment, val, ((JLabel) var.getCommonRep()).getText());
    }

    public void testVariableValueCreate() {
    }

    public void testVariableFromCV() {
    }

    public void testVariableValueRead() {
    }

    public void testVariableValueWrite() {
    }

    public void testVariableCvWrite() {
    }

    public void testWriteSynch2() {
    }

    public void testLongAddressCreate() {
        Vector<CvValue> v = createCvVector();
        CvValue cv17 = new CvValue(17, p);
        CvValue cv18 = new CvValue(18, p);
        cv17.setValue(2);
        cv18.setValue(3);
        v.setElementAt(cv17, 17);
        v.setElementAt(cv18, 18);
        LongAddrVariableValue var = new LongAddrVariableValue("label", "comment", "", false, false, false, false, 17, "VVVVVVVV", 0, 255, v, null, null);
        Assert.assertTrue(var.label() == "label");
        ((JTextField) var.getCommonRep()).setText("4797");
        Assert.assertTrue(((JTextField) var.getCommonRep()).getText().equals("4797"));
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        Assert.assertTrue(cv17.getValue() == 210);
        Assert.assertTrue(cv18.getValue() == 189);
    }

    public void testLongAddressFromCV() {
        Vector<CvValue> v = createCvVector();
        CvValue cv17 = new CvValue(17, p);
        CvValue cv18 = new CvValue(18, p);
        cv17.setValue(2);
        cv18.setValue(3);
        v.setElementAt(cv17, 17);
        v.setElementAt(cv18, 18);
        LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", "", false, false, false, false, 17, "VVVVVVVV", 0, 255, v, null, null);
        ((JTextField) var.getCommonRep()).setText("1029");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        cv17.setValue(210);
        Assert.assertTrue(cv17.getValue() == 210);
        cv18.setValue(189);
        Assert.assertTrue(((JTextField) var.getCommonRep()).getText().equals("4797"));
        Assert.assertTrue(cv18.getValue() == 189);
    }

    List<java.beans.PropertyChangeEvent> evtList = null;

    public void testLongAddressRead() {
        log.debug("testLongAddressRead starts");
        Vector<CvValue> v = createCvVector();
        CvValue cv17 = new CvValue(17, p);
        CvValue cv18 = new CvValue(18, p);
        v.setElementAt(cv17, 17);
        v.setElementAt(cv18, 18);
        LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", "", false, false, false, false, 17, "XXVVVVXX", 0, 255, v, null, null);
        java.beans.PropertyChangeListener listen = new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent e) {
                evtList.add(e);
                if (e.getPropertyName().equals("Busy") && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) log.debug("Busy false seen in test");
            }
        };
        evtList = new ArrayList<java.beans.PropertyChangeEvent>();
        var.addPropertyChangeListener(listen);
        ((JTextField) var.getCommonRep()).setText("5");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        var.readAll();
        int i = 0;
        while (var.isBusy() && i++ < 100) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i=" + i + " value=" + ((JTextField) var.getCommonRep()).getText() + " state=" + var.getState());
        Assert.assertTrue("wait satisfied ", i < 100);
        int nBusyFalse = 0;
        for (int k = 0; k < evtList.size(); k++) {
            java.beans.PropertyChangeEvent e = evtList.get(k);
            if (e.getPropertyName().equals("Busy") && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) nBusyFalse++;
        }
        Assert.assertEquals("only one Busy -> false transition ", 1, nBusyFalse);
        Assert.assertEquals("text value ", "15227", ((JTextField) var.getCommonRep()).getText());
        Assert.assertEquals("Var state", AbstractValue.READ, var.getState());
        Assert.assertEquals("CV 17 value ", 251, cv17.getValue());
        Assert.assertEquals("CV 18 value ", 123, cv18.getValue());
    }

    public void testLongAddressWrite() {
        Vector<CvValue> v = createCvVector();
        CvValue cv17 = new CvValue(17, p);
        CvValue cv18 = new CvValue(18, p);
        v.setElementAt(cv17, 17);
        v.setElementAt(cv18, 18);
        LongAddrVariableValue var = new LongAddrVariableValue("name", "comment", "", false, false, false, false, 17, "XXVVVVXX", 0, 255, v, null, null);
        ((JTextField) var.getCommonRep()).setText("4797");
        var.actionPerformed(new java.awt.event.ActionEvent(var, 0, ""));
        var.writeAll();
        int i = 0;
        while (var.isBusy() && i++ < 100) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        if (log.isDebugEnabled()) log.debug("past loop, i=" + i + " value=" + ((JTextField) var.getCommonRep()).getText() + " state=" + var.getState() + " last write: " + p.lastWrite());
        Assert.assertTrue("wait satisfied ", i < 100);
        Assert.assertEquals("CV 17 value ", 210, cv17.getValue());
        Assert.assertEquals("CV 18 value ", 189, cv18.getValue());
        Assert.assertTrue(((JTextField) var.getCommonRep()).getText().equals("4797"));
        Assert.assertEquals("Var state", AbstractValue.STORED, var.getState());
        Assert.assertTrue(p.lastWrite() == 189);
    }

    protected Vector<CvValue> createCvVector() {
        Vector<CvValue> v = new Vector<CvValue>(512);
        for (int i = 0; i < 512; i++) v.addElement(null);
        return v;
    }

    public LongAddrVariableValueTest(String s) {
        super(s);
    }

    public static void main(String[] args) {
        String[] testCaseName = { "-noloading", LongAddrVariableValueTest.class.getName() };
        junit.swingui.TestRunner.main(testCaseName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(LongAddrVariableValueTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LongAddrVariableValueTest.class.getName());

    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
