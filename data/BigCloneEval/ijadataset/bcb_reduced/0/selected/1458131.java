package Plan58.datatype;

import java.lang.reflect.Array;
import javax.swing.table.AbstractTableModel;

/**
 * The Class Model.
 * 
 * @author Sören Haag - 660553
 * Die Klasse Model ist ein Tabellenmodell für eine JTable. Dieses Tabellenmodell lässt sowohl löschen,
 * editieren und zurücksetzen ( reset ) zu.
 */
public class Model extends AbstractTableModel {

    /**
	 * Expand.
	 * 
	 * @param a the a
	 * 
	 * @return the object
	 */
    public static Object expand(Object a) {
        Class cl = a.getClass();
        if (!cl.isArray()) return null;
        int length = Array.getLength(a);
        int newLength = length + 1;
        Class componentType = a.getClass().getComponentType();
        Object newArray = Array.newInstance(componentType, newLength);
        System.arraycopy(a, 0, newArray, 0, length);
        return newArray;
    }

    /** The stringsc6. */
    private String[] stringsc1, stringsc2, stringsc3, stringsc4, stringsc5, stringsc6;

    /**
   	 * Instantiates a new model.
   	 */
    public Model() {
        stringsc1 = new String[10000];
        stringsc2 = new String[10000];
        stringsc3 = new String[10000];
        stringsc4 = new String[10000];
        stringsc5 = new String[10000];
        stringsc6 = new String[10000];
    }

    public int getColumnCount() {
        return 6;
    }

    public int getRowCount() {
        return stringsc1.length;
    }

    /**
      	 * Removes the all.
      	 */
    public void removeALL() {
        stringsc1 = new String[10000];
        stringsc2 = new String[10000];
        stringsc3 = new String[10000];
        stringsc4 = new String[10000];
        stringsc5 = new String[10000];
        stringsc6 = new String[10000];
    }

    public String getColumnName(int column) {
        switch(column) {
            case 0:
                return "Artikelnr";
            case 1:
                return "Beschreibung";
            case 2:
                return "Mengeneinheit";
            case 3:
                return "Preiseinheit";
            case 4:
                return "Preisart";
            case 5:
                return "Preis";
            default:
                return null;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex) {
            case 0:
                return stringsc1[rowIndex];
            case 1:
                return stringsc2[rowIndex];
            case 2:
                return stringsc3[rowIndex];
            case 3:
                return stringsc4[rowIndex];
            case 4:
                return stringsc5[rowIndex];
            case 5:
                return stringsc6[rowIndex];
            default:
                return null;
        }
    }

    public Class getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return String.class;
            case 4:
                return String.class;
            case 5:
                return String.class;
            default:
                return null;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    /**
	    * Lastrow.
	    * 
	    * @return the int
	    */
    public int lastrow() {
        int i = 0;
        while ((stringsc1[stringsc1.length - i].length() == 0) && (stringsc1.length > 0)) {
            i++;
        }
        return stringsc1.length;
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex >= stringsc1.length) {
            stringsc1 = (String[]) expand(stringsc1);
            stringsc2 = (String[]) expand(stringsc2);
            stringsc3 = (String[]) expand(stringsc3);
            stringsc4 = (String[]) expand(stringsc4);
        }
        switch(columnIndex) {
            case 0:
                stringsc1[rowIndex] = aValue.toString();
                break;
            case 1:
                stringsc2[rowIndex] = aValue.toString();
                break;
            case 2:
                stringsc3[rowIndex] = aValue.toString();
                break;
            case 3:
                stringsc4[rowIndex] = aValue.toString();
            case 4:
                stringsc5[rowIndex] = aValue.toString();
            case 5:
                stringsc6[rowIndex] = aValue.toString();
        }
    }
}
