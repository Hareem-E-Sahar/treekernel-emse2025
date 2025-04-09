package jgrx.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author  Admin
 */
public class VariableListPanel extends javax.swing.JPanel {

    DefaultTableModel tableModel;

    Map map;

    /** Creates new form FctListPanel */
    public VariableListPanel(String ss[], Map map) {
        this.map = map;
        tableModel = new DefaultTableModel(ss, 0);
        update();
        initComponents();
        addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent me) {
                update();
            }
        });
    }

    public void update() {
        Iterator iter = map.keySet().iterator();
        Vector labels = new Vector();
        while (iter.hasNext()) {
            Object obj = iter.next();
            labels.add(obj);
        }
        int len = tableModel.getRowCount();
        for (int i = 0; i < Math.min(len, labels.size()); i++) {
            tableModel.setValueAt(labels.get(i), i, 0);
            tableModel.setValueAt(map.get(labels.get(i)), i, 1);
        }
        if (len == Math.max(len, labels.size())) {
            for (int i = labels.size(); i < len; i++) {
                tableModel.removeRow(i);
            }
        } else {
            for (int i = len; i < labels.size(); i++) {
                Object oj[] = { labels.get(i), map.get(labels.get(i)) };
                tableModel.addRow(oj);
            }
        }
    }

    private void sort(Vector v) {
        sortR(v, new Vector(v.size()), 0, v.size() - 1);
    }

    private void sortR(Vector v, Vector temp, int s, int l) {
        if (l - s >= 1) {
            int mid = (l + s) / 2;
            sortR(v, temp, s, mid);
            sortR(v, temp, mid + 1, l);
            merge(v, temp, s, l);
        }
    }

    private void merge(Vector v, Vector temp, int s, int l) {
        temp.clear();
        int mid = (l + s) / 2;
        int s1 = s;
        int s2 = mid + 1;
        while (s1 <= mid && s2 <= l) {
            if (((Comparable) v.get(s1)).compareTo((Comparable) v.get(s2)) < 0) {
                temp.add(v.get(s1));
                s1++;
            } else {
                temp.add(v.get(s2));
                s2++;
            }
        }
        while (s1 <= mid) {
            temp.add(v.get(s1));
            s1++;
        }
        while (s2 <= l) {
            temp.add(v.get(s2));
            s2++;
        }
    }

    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTable1.setModel(tableModel);
        jTable1.setEnabled(false);
        jTable1.addInputMethodListener(new java.awt.event.InputMethodListener() {

            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }

            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                jTable1InputMethodTextChanged(evt);
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {

            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTable1KeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 375, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap(15, Short.MAX_VALUE)));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().addContainerGap().add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 275, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap(14, Short.MAX_VALUE)));
    }

    private void jTable1KeyReleased(java.awt.event.KeyEvent evt) {
        System.out.println("key");
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            System.out.println("hiyar");
        }
    }

    private void jTable1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
        System.out.println("akiyu");
    }

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JTable jTable1;
}
