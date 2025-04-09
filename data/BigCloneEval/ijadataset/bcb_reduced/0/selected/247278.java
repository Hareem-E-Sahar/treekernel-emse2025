package org.simulare.gui;

import java.lang.reflect.*;
import javax.swing.table.*;
import org.simulare.*;

/**
 * The item model represetation for GUI items.
 */
public class ItemTableModel extends AbstractTableModel {

    private Item item;

    private String[] columnNames = new String[] { "Name", "Type", "Value" };

    public ItemTableModel(Item item) {
        setItem(item);
    }

    public void setItem(Item item) {
        this.item = item;
        fireTableDataChanged();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return item.getAttributesLength();
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public Class getColumnClass(int col) {
        Class result = null;
        switch(col) {
            case 0:
                result = String.class;
                break;
            case 1:
                result = Type.class;
                break;
            case 2:
                result = Object.class;
                break;
        }
        return result;
    }

    public Object getValueAt(int row, int col) {
        Object result = null;
        Attribute att = (Attribute) item.getAttribute(row);
        switch(col) {
            case 0:
                result = att.getName();
                break;
            case 1:
                result = Type.getName(att.getType());
                break;
            case 2:
                result = att.getValue();
                break;
        }
        return result;
    }

    public void setValueAt(Object value, int row, int col) {
        Attribute att = (Attribute) item.getAttribute(row);
        switch(col) {
            case 0:
                int index = item.getAttributeIndex(att.getName());
                item.removeAttribute(index);
                att.setName((String) value);
                item.addAttribute(index, att);
                break;
            case 1:
                att.setType(((Type) value).type);
                break;
            case 2:
                try {
                    if (att.getType() == Character.class) {
                        String str = (String) value;
                        if (str.length() > 0) {
                            att.setValue(new Character(str.charAt(0)));
                        } else {
                            att.setValue(null);
                        }
                    } else {
                        String str = (String) value;
                        if (str.length() > 0) {
                            Constructor c = att.getType().getConstructor(new Class[] { String.class });
                            att.setValue(c.newInstance(new Object[] { value }));
                        } else {
                            att.setValue(null);
                        }
                    }
                } catch (InvocationTargetException ex) {
                    ex.printStackTrace();
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                } catch (IllegalAccessException ex) {
                    ex.printStackTrace();
                } catch (InstantiationException ex) {
                    ex.printStackTrace();
                } catch (SecurityException ex) {
                    ex.printStackTrace();
                } catch (NoSuchMethodException ex) {
                    ex.printStackTrace();
                }
                break;
        }
        fireTableCellUpdated(row, col);
    }

    public void add(int index) {
        if (index <= 0) {
            index = 0;
        }
        Attribute att = new Attribute("", String.class, "");
        item.addAttribute(att);
        fireTableRowsInserted(index - 1, index);
    }

    public void remove(int index) {
        if (0 <= index && index < item.getAttributesLength()) {
            item.removeAttribute(index);
            fireTableDataChanged();
        }
    }

    public void up(int index) {
        move(1, item.getAttributesLength(), -1, index);
    }

    public void down(int index) {
        move(0, item.getAttributesLength() - 1, 1, index);
    }

    private void move(int min, int max, int inc, int index) {
        if (min <= index && index < max) {
            Attribute att = (Attribute) item.getAttribute(index);
            item.removeAttribute(index);
            item.addAttribute(index + inc, att);
            fireTableDataChanged();
        }
    }
}
