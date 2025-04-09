package pilrcedit;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

public class ConstantFrame extends Frame implements ItemListener, TextListener, KeyListener {

    List list;

    TextField name, value;

    Choice includes;

    String nameVal, valueVal;

    int selected = 0;

    Project project;

    ConstantFile current;

    boolean nameValid = false;

    boolean valueValid = false;

    String[] names;

    public ConstantFrame(Project project) {
        super("Constants");
        boolean reg = Preferences.registerFrame(this);
        Panel p = new Panel(new BorderLayout(5, 5));
        includes = new Choice();
        includes.addItemListener(this);
        p.add("North", includes);
        p.add(name = new TextField(20));
        name.addTextListener(this);
        p.add("East", value = new TextField(10));
        value.addTextListener(this);
        add("North", p);
        list = new List(10, false);
        refresh(project);
        list.addItemListener(this);
        list.select(0);
        add(list);
        list.addKeyListener(this);
        if (!reg) pack();
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == e.VK_DELETE && selected != 0) {
            int sind = selected - 1;
            String[] temp = new String[names.length - 1];
            System.arraycopy(names, 0, temp, 0, sind);
            if (sind < names.length - 1) System.arraycopy(names, sind + 1, temp, sind, names.length - sind - 1);
            names = temp;
            project.removeConstant(nameVal);
            list.remove(selected);
            if (sind >= names.length) {
                selected = names.length;
                list.select(selected);
            } else {
                list.select(selected);
                itemStateChanged(null);
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void refresh(Project project) {
        this.project = project;
        current = project.getCurrentConstantFile();
        includes.removeAll();
        Enumeration e = project.getConstantFiles();
        if (e != null) {
            int i = 0;
            int select = 0;
            while (e.hasMoreElements()) {
                ConstantFile cf = (ConstantFile) e.nextElement();
                if (cf == current) select = i;
                includes.add(cf.file == null ? "Untitled.h" : cf.file);
                i++;
            }
            includes.select(select);
            refreshConstants();
        } else includes.add("No include files.");
    }

    public void refreshConstants() {
        list.removeAll();
        if (current == null) return;
        boolean pilrc = current.isPilRCGenerated();
        if (!pilrc) list.add("New Constant..."); else list.add("");
        int size = current.getConstantCount();
        Enumeration keys = current.getAllConstants();
        if (keys != null) {
            names = new String[size];
            int i = 0;
            while (keys.hasMoreElements()) names[i++] = (String) keys.nextElement();
            sort(names);
            for (i = 0; i < names.length; i++) if (pilrc) list.add(names[i]); else list.add(names[i] + " (" + current.getConstant(names[i]) + ")");
        } else names = null;
        name.setEnabled(!pilrc);
        value.setEnabled(!pilrc);
    }

    public void textValueChanged(TextEvent e) {
        boolean changed = false;
        if (e.getSource() == name) {
            if (nameVal == null || !name.getText().equals(nameVal)) {
                project.removeConstant(nameVal);
                nameVal = name.getText();
                if (nameVal.indexOf(' ') == -1 && nameVal.length() > 0) {
                    name.setForeground(Color.black);
                    changed = true;
                    nameValid = true;
                } else {
                    name.setForeground(Color.red);
                    nameValid = false;
                }
            }
        } else if (e.getSource() == value) {
            if (valueVal == null || !value.getText().equals(valueVal)) {
                valueVal = value.getText();
                try {
                    Integer.parseInt(valueVal);
                    value.setForeground(Color.black);
                    changed = true;
                    valueValid = true;
                } catch (NumberFormatException ex) {
                    value.setForeground(Color.red);
                    valueValid = false;
                }
            }
        }
        if (changed && nameValid && valueValid) {
            String toAdd = nameVal + " (" + valueVal + ")";
            int pos = getPos(names, nameVal, selected - 1);
            if (selected != 0) {
                list.remove(selected);
                if (pos + 1 > selected) pos--;
            } else if (names == null) {
                names = new String[1];
                names[0] = nameVal;
            } else {
                String[] temp = new String[names.length + 1];
                System.arraycopy(names, 0, temp, 0, pos);
                temp[pos] = nameVal;
                System.arraycopy(names, pos, temp, pos + 1, names.length - pos);
                names = temp;
            }
            list.add(toAdd, selected = pos + 1);
            project.setConstant(nameVal, Integer.parseInt(valueVal), false);
            list.select(selected);
            list.makeVisible(selected);
        }
    }

    public void itemStateChanged(ItemEvent e) {
        if (e != null && e.getSource() == includes) {
            current = project.setCurrentConstantFile(includes.getSelectedIndex());
            refreshConstants();
        } else {
            selected = list.getSelectedIndex();
            String s = list.getSelectedItem();
            if (current.isPilRCGenerated()) {
                nameVal = s;
                return;
            }
            int bracket = s.lastIndexOf('(');
            if (selected == 0) {
                name.setText(nameVal = "");
                value.setText(valueVal = "");
                nameValid = valueValid = false;
                name.requestFocus();
            } else {
                name.setText(nameVal = s.substring(0, bracket - 1));
                value.setText(valueVal = s.substring(bracket + 1, s.length() - 1));
                nameValid = valueValid = true;
            }
        }
    }

    public int getPos(String[] a, String o, int pos) {
        if (a == null) return 0;
        int ret = a.length;
        for (int i = 0; i < a.length; i++) {
            int v = o.compareTo(a[i]);
            if (v <= 0) {
                ret = i;
                break;
            }
        }
        if (pos != -1) {
            if (pos < ret) {
                System.arraycopy(a, pos + 1, a, pos, ret - pos - 1);
                a[ret - 1] = o;
            } else if (pos > ret) {
                System.arraycopy(a, ret, a, ret + 1, pos - ret);
                a[ret] = o;
            } else a[ret] = o;
        }
        return ret;
    }

    /**
	 * Mergesort ripped from java.util.Arrays
	 */
    public static void sort(String[] a) {
        String aux[] = (String[]) a.clone();
        mergeSort(aux, a, 0, a.length);
    }

    private static void mergeSort(String src[], String dest[], int low, int high) {
        int length = high - low;
        if (length < 7) {
            for (int i = low; i < high; i++) for (int j = i; j > low && dest[j - 1].compareTo(dest[j]) > 0; j--) swap(dest, j, j - 1);
            return;
        }
        int mid = (low + high) / 2;
        mergeSort(dest, src, low, mid);
        mergeSort(dest, src, mid, high);
        if (src[mid - 1].compareTo(src[mid]) <= 0) {
            System.arraycopy(src, low, dest, low, length);
            return;
        }
        for (int i = low, p = low, q = mid; i < high; i++) {
            if (q >= high || p < mid && src[p].compareTo(src[q]) <= 0) dest[i] = src[p++]; else dest[i] = src[q++];
        }
    }

    /**
	 * Swaps x[a] with x[b].
	 */
    private static void swap(String x[], int a, int b) {
        String t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
}
