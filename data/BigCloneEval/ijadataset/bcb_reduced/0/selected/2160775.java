package org.tigr.microarray.mev.cgh.CGHGuiObj;

import javax.swing.DefaultListModel;
import org.tigr.microarray.mev.cluster.gui.IData;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */
public class CGHDisplayOrderChanger extends javax.swing.JDialog {

    DisplayOrderObj[] dataObjs;

    int[] indicesOrder;

    IData data;

    boolean cancel;

    /** Creates new form CGHDisplayOrderChanger */
    public CGHDisplayOrderChanger(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public CGHDisplayOrderChanger(IData data, java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        cancel = false;
        this.data = data;
        initComponents();
        lstData.setModel(new DefaultListModel());
        this.data = data;
        indicesOrder = data.getSamplesOrder();
        for (int i = 0; i < indicesOrder.length; i++) {
            ((DefaultListModel) lstData.getModel()).addElement(data.getSampleName(indicesOrder[i]));
        }
        setSize(400, 400);
    }

    private void initializeIndices() {
        indicesOrder = new int[data.getFeaturesCount()];
        int[] experimentIndices = data.getSamplesOrder();
        for (int i = 0; i < data.getFeaturesCount(); i++) {
            indicesOrder[experimentIndices[i]] = i;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstData = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        btnMoveUp = new javax.swing.JButton();
        btnMoveDown = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        jPanel1.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(lstData);
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));
        btnMoveUp.setText("Up");
        btnMoveUp.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveUpActionPerformed(evt);
            }
        });
        jPanel3.add(btnMoveUp);
        btnMoveDown.setText("Down");
        btnMoveDown.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMoveDownActionPerformed(evt);
            }
        });
        jPanel3.add(btnMoveDown);
        jPanel2.add(jPanel3);
        btnOk.setText("Ok");
        btnOk.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOkActionPerformed(evt);
            }
        });
        jPanel4.add(btnOk);
        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        jPanel4.add(btnCancel);
        jPanel2.add(jPanel4);
        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);
        pack();
    }

    private void btnMoveDownActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedIndex = lstData.getSelectedIndex();
        if (selectedIndex < lstData.getModel().getSize() - 1) {
            Object obj = ((DefaultListModel) lstData.getModel()).remove(selectedIndex);
            ((DefaultListModel) lstData.getModel()).insertElementAt(obj, selectedIndex + 1);
            lstData.setSelectedIndex(selectedIndex + 1);
            int tmp = indicesOrder[selectedIndex];
            indicesOrder[selectedIndex] = indicesOrder[selectedIndex + 1];
            indicesOrder[selectedIndex + 1] = tmp;
        }
    }

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {
        setVisible(false);
        dispose();
    }

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        cancel = true;
        dispose();
    }

    private void btnMoveUpActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedIndex = lstData.getSelectedIndex();
        if (selectedIndex > 0) {
            Object obj = ((DefaultListModel) lstData.getModel()).remove(selectedIndex);
            ((DefaultListModel) lstData.getModel()).insertElementAt(obj, selectedIndex - 1);
            lstData.setSelectedIndex(selectedIndex - 1);
            int tmp = indicesOrder[selectedIndex];
            indicesOrder[selectedIndex] = indicesOrder[selectedIndex - 1];
            indicesOrder[selectedIndex - 1] = tmp;
        }
    }

    public boolean isCancelled() {
        return cancel;
    }

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        setVisible(false);
        dispose();
    }

    /** Getter for property indicesOrder.
     * @return Value of property indicesOrder.
     */
    public int[] getIndicesOrder() {
        generateIndicesOrder();
        return this.indicesOrder;
    }

    private void generateIndicesOrder() {
        indicesOrder = new int[dataObjs.length];
        for (int i = 0; i < indicesOrder.length; i++) {
            indicesOrder[((DisplayOrderObj) lstData.getModel().getElementAt(i)).getOrigIndex()] = i;
        }
    }

    /** Setter for property indicesOrder.
     * @param indicesOrder New value of property indicesOrder.
     */
    public void setIndicesOrder(int[] indicesOrder) {
        this.indicesOrder = indicesOrder;
    }

    private class DisplayOrderObj {

        Object value;

        int origIndex;

        DisplayOrderObj(Object value, int origIndex) {
            this.value = value;
            this.origIndex = origIndex;
        }

        /** Getter for property value.
         * @return Value of property value.
         */
        public java.lang.Object getValue() {
            return value;
        }

        /** Setter for property value.
         * @param value New value of property value.
         */
        public void setValue(java.lang.Object value) {
            this.value = value;
        }

        /** Getter for property origIndex.
         * @return Value of property origIndex.
         */
        public int getOrigIndex() {
            return origIndex;
        }

        /** Setter for property origIndex.
         * @param origIndex New value of property origIndex.
         */
        public void setOrigIndex(int origIndex) {
            this.origIndex = origIndex;
        }

        public String toString() {
            return value.toString();
        }
    }

    private javax.swing.JPanel jPanel4;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JButton btnMoveDown;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JButton btnMoveUp;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JList lstData;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JButton btnOk;

    private javax.swing.JButton btnCancel;
}
