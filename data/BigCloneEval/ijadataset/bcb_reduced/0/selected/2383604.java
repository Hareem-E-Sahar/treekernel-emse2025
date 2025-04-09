package org.digitall.common.cashflow.classes;

import java.util.Vector;
import org.digitall.common.cashflow.classes.CostsCentre;

public class CostsCentreGroups extends Vector {

    public CostsCentreGroups() {
    }

    public void sort() {
        Object ccTmp;
        for (int i = 0; i < size() - 1; i++) {
            for (int j = i + 1; j < size(); j++) {
                int idCCi = ((CostsCentre) elementAt(i)).getIdCostCentre();
                int idCCj = ((CostsCentre) elementAt(j)).getIdCostCentre();
                if (idCCi > idCCj) {
                    ccTmp = elementAt(i);
                    setElementAt(elementAt(j), i);
                    setElementAt(ccTmp, j);
                }
            }
        }
    }

    public CostsCentre getElement(int _idCostCentre) {
        int initIndex = 0;
        int endIndex = size() - 1;
        int midIndex = 0;
        while (initIndex <= endIndex) {
            midIndex = (initIndex + endIndex) / 2;
            int idCC = ((CostsCentre) elementAt(midIndex)).getIdCostCentre();
            if (_idCostCentre == idCC) {
                break;
            } else if (_idCostCentre > idCC) {
                initIndex = midIndex + 1;
            } else if (_idCostCentre < idCC) {
                endIndex = midIndex - 1;
            }
        }
        if (initIndex > endIndex) {
            return null;
        } else {
            return (CostsCentre) elementAt(midIndex);
        }
    }
}
