package mainPackage;

import java.awt.event.MouseEvent;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;

/**
 * @author Francesco Pagano
 *
 * deriva da JTable per introdurre i metodi per la selezione di una riga e
 * l'aggiornamento dopo la ricerca
 */
public class MiaJTable extends JTable {

    int numRighe = -1;

    /**
	 * Constructor for MiaJTable.
	 */
    public MiaJTable() {
        super();
        initialize();
    }

    /**
	 * Constructor for MiaJTable.
	 * @param arg0
	 */
    public MiaJTable(TableModel arg0) {
        super(arg0);
        initialize();
    }

    /**
	 * Constructor for MiaJTable.
	 * @param arg0
	 * @param arg1
	 */
    public MiaJTable(TableModel arg0, TableColumnModel arg1) {
        super(arg0, arg1);
        initialize();
    }

    /**
	 * Constructor for MiaJTable.
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
    public MiaJTable(TableModel arg0, TableColumnModel arg1, ListSelectionModel arg2) {
        super(arg0, arg1, arg2);
        initialize();
    }

    /**
	 * Constructor for MiaJTable.
	 * @param arg0
	 * @param arg1
	 */
    public MiaJTable(int arg0, int arg1) {
        super(arg0, arg1);
        initialize();
    }

    /**
	 * Constructor for MiaJTable.
	 * @param arg0
	 * @param arg1
	 */
    public MiaJTable(Vector arg0, Vector arg1) {
        super(arg0, arg1);
        initialize();
    }

    /**
	 * Constructor for MiaJTable.
	 * @param arg0
	 * @param arg1
	 */
    public MiaJTable(Object[][] arg0, Object[] arg1) {
        super(arg0, arg1);
        initialize();
    }

    /**
	 * @see javax.swing.JTable#setRowSelectionInterval(int, int)
	 */
    public void setRowSelectionInterval(int arg0, int arg1) {
        super.setRowSelectionInterval(arg0, arg1);
    }

    /**
	 * Method selezionaRiga.
	 * @param riga da selezionare
	 * si posiziona su riga, la seleziona e la rende visibile
	 */
    public void selezionaRiga(int riga) {
        selezionaRiga(riga, riga);
    }

    /**
	 * Method selezionaRiga.
	 * @param daRiga, aRiga: righe da selezionare (estremi compresi)
	 * si posiziona su riga, la seleziona e la rende visibile
	 */
    public void selezionaRiga(int daRiga, int aRiga) {
        setRowSelectionInterval(daRiga, aRiga);
        scrollRectToVisible(getCellRect(daRiga, aRiga, true));
    }

    /**
	 * @see javax.swing.JTable#getValueAt(int, int)
	 */
    public Object getValueAt(int arg0, int arg1) {
        Object ob;
        if (arg1 < 0) ob = getModel().getValueAt(arg0, -arg1 - 1); else ob = super.getValueAt(arg0, arg1);
        return ob;
    }

    public int ricercaBinaria(String pattern, int colonna) {
        if (pattern == null) return 0;
        int pos = 1, estremo1 = 1, estremo2, confronto;
        String trovato = "";
        boolean finito = false;
        pos = 0;
        try {
            trovato = getValueAt(pos, colonna).toString();
        } catch (RuntimeException e) {
        }
        confronto = trovato.compareToIgnoreCase(pattern);
        if (confronto > 0) return -pos; else if (confronto == 0) return pos;
        pos = getRowCount() - 1;
        trovato = "";
        try {
            trovato = getValueAt(pos, colonna).toString();
        } catch (RuntimeException e) {
        }
        confronto = trovato.compareToIgnoreCase(pattern);
        if (confronto < 0) return -pos; else if (confronto == 0) {
            while (pos > 0 && getValueAt(pos - 1, colonna).toString().compareToIgnoreCase(pattern) == 0) pos--;
            return pos;
        }
        estremo2 = pos;
        int tentativi = 1;
        while (!finito) {
            finito = (estremo1 - estremo2 >= -1);
            pos = estremo1 + (estremo2 - estremo1) / 2;
            trovato = "";
            try {
                trovato = getValueAt(pos, colonna).toString();
            } catch (RuntimeException e) {
            }
            confronto = trovato.compareToIgnoreCase(pattern);
            CostantiDavide.msgInfo("tentativo: " + tentativi + " da cercare: " + pattern + " trovato: " + trovato);
            if (confronto == 0) {
                while (pos > 0 && getValueAt(pos - 1, colonna).toString().compareToIgnoreCase(pattern) == 0) pos--;
                return pos;
            }
            if (confronto > 0) estremo2 = pos; else estremo1 = pos;
            tentativi++;
        }
        return -pos;
    }

    public void ricercaLineareValore(String pattern, int colonna) {
        int pos = 0;
        if (pattern == null) return;
        boolean finito = false;
        for (; (pos < getRowCount()) && !finito; pos++) {
            String trovato = "";
            try {
                trovato = getValueAt(pos, colonna).toString();
            } catch (RuntimeException e) {
            }
            int confronto = trovato.compareToIgnoreCase(pattern);
            finito = (confronto == 0);
        }
        try {
            selezionaRiga(pos - 1);
        } catch (RuntimeException e) {
            mainPackage.CostantiDavide.msgEccezione("valore non trovato");
            e.printStackTrace();
        }
    }

    public boolean ricercaValore(String pattern, int colonna, JDialog jd) {
        int pos = ricercaBinaria(pattern, colonna);
        boolean retVal = true;
        if (pos < 0) {
            retVal = false;
            pos = -pos;
            if (pos < getRowCount() - 1) pos++;
        }
        selezionaRiga(pos);
        return retVal;
    }

    public boolean ricercaValore(String pattern, int colonna, JFrame jf) {
        int pos = ricercaBinaria(pattern, colonna);
        boolean retVal = true;
        if (pos < 0) {
            retVal = false;
            pos = -pos;
            if (pos < getRowCount() - 1) pos++;
        }
        selezionaRiga(pos);
        return retVal;
    }

    public boolean ricercaValoreConChiave(String pattern, int colonna, String chiave, int posModel, JDialog jd) {
        int pos = ricercaBinaria(pattern, colonna);
        boolean retVal = true;
        if (pos < 0) {
            retVal = false;
            pos = -pos;
            if (pos < getRowCount() - 1) pos++;
        } else {
            int max = getRowCount() - 1;
            boolean trovato = false;
            while (pos < max && getValueAt(pos, colonna).toString().compareToIgnoreCase(pattern) == 0 && getValueAt(pos, -posModel - 1).toString().compareToIgnoreCase(chiave) != 0) pos++;
        }
        selezionaRiga(pos);
        return retVal;
    }

    public void initialize() {
        disattivaToolTipText();
    }

    /**
	 * @see javax.swing.JTable#getRowCount()
	 */
    public int getRowCount() {
        if (numRighe == -1) {
            numRighe = super.getRowCount();
        }
        return numRighe;
    }

    /**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(ListSelectionEvent)
	 */
    public void valueChanged(ListSelectionEvent arg0) {
        super.valueChanged(arg0);
    }

    /**
	 * @see javax.swing.JTable#getToolTipText(MouseEvent)
	 */
    public String getToolTipText(MouseEvent arg0) {
        CostantiDavide.msgInfo("tooltip");
        return super.getToolTipText(arg0);
    }

    public void disattivaToolTipText() {
        ToolTipManager.sharedInstance().unregisterComponent(this);
        ToolTipManager.sharedInstance().unregisterComponent(this.getTableHeader());
    }

    public void riattivaToolTipText() {
        ToolTipManager.sharedInstance().registerComponent(this);
        ToolTipManager.sharedInstance().registerComponent(this.getTableHeader());
    }

    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        numRighe = -1;
    }
}
