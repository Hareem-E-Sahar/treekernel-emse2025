package cn.edu.thss.iise.beehivez.server.metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.Transition;
import cn.edu.thss.iise.beehivez.server.util.PetriNetUtil;

/**
 * 
 * @author Wu Nianhua
 * Similarity based on process dependency Matri
 *
 */
public class ProcessDependencyDistance extends PetriNetDistance {

    @Override
    public int distance(PetriNet pn1, PetriNet pn2) {
        ArrayList<Transition> T1 = pn1.getTransitions();
        ArrayList<Transition> T2 = pn2.getTransitions();
        ArrayList<Transition> T = new ArrayList<Transition>();
        Iterator it1 = T1.iterator();
        while (it1.hasNext()) {
            Transition t1 = (Transition) it1.next();
            boolean flag = false;
            Iterator it = T.iterator();
            while (it.hasNext()) {
                Transition t = (Transition) it.next();
                if (t1.getIdentifier().equals(t.getIdentifier())) {
                    flag = true;
                    break;
                }
            }
            if (flag == false) {
                T.add(t1);
            }
        }
        Iterator it2 = T2.iterator();
        while (it2.hasNext()) {
            Transition t2 = (Transition) it2.next();
            boolean flag = false;
            Iterator it = T.iterator();
            while (it.hasNext()) {
                Transition t = (Transition) it.next();
                if (t2.getIdentifier().equals(t.getIdentifier())) {
                    flag = true;
                    break;
                }
            }
            if (flag == false) {
                T.add(t2);
            }
        }
        HashMap<String, Integer> StoT = new HashMap<String, Integer>();
        int i = 0;
        Iterator it = T.iterator();
        while (it.hasNext()) {
            Transition t = (Transition) it.next();
            StoT.put(t.getIdentifier(), new Integer(i++));
            System.out.print(t.getIdentifier() + "  ");
        }
        int totalsize = T.size();
        int[][] NM1 = new int[totalsize][totalsize];
        int[][] NM2 = new int[totalsize][totalsize];
        Iterator iterator1 = T1.iterator();
        while (iterator1.hasNext()) {
            Transition t = (Transition) iterator1.next();
            Iterator op = t.getSuccessors().iterator();
            while (op.hasNext()) {
                Place place = (Place) op.next();
                Iterator ot = place.getSuccessors().iterator();
                while (ot.hasNext()) {
                    Transition dt = (Transition) ot.next();
                    int x = StoT.get(t.getIdentifier());
                    int y = StoT.get(dt.getIdentifier());
                    NM1[x][y] = 1;
                }
            }
        }
        Iterator iterator2 = T2.iterator();
        while (iterator2.hasNext()) {
            Transition t = (Transition) iterator2.next();
            Iterator op = t.getSuccessors().iterator();
            while (op.hasNext()) {
                Place place = (Place) op.next();
                Iterator ot = place.getSuccessors().iterator();
                while (ot.hasNext()) {
                    Transition dt = (Transition) ot.next();
                    int x = StoT.get(t.getIdentifier());
                    int y = StoT.get(dt.getIdentifier());
                    NM2[x][y] = 1;
                }
            }
        }
        int[][] NM = new int[totalsize][totalsize];
        int[][] MN = new int[totalsize][totalsize];
        int[][] result_matri = new int[totalsize][totalsize];
        int result = 0;
        for (int k = 0; k < totalsize; k++) {
            for (int j = 0; j < totalsize; j++) {
                NM[k][j] = NM1[k][j] - NM2[k][j];
                MN[j][k] = NM[k][j];
            }
        }
        for (int m = 0; m < totalsize; m++) {
            for (int n = 0; n < totalsize; n++) {
                for (int k = 0; k < totalsize; k++) {
                    result_matri[m][n] += NM[m][k] * MN[k][n];
                }
                if (m == n) result += result_matri[m][n];
            }
        }
        return result;
    }

    public static void main(String[] args) {
        ProcessDependencyDistance PDD = new ProcessDependencyDistance();
        PetriNetUtil pnu = new PetriNetUtil();
        PetriNet pn1 = pnu.getPetriNetFromPnmlFile("2.pnml");
        PetriNet pn2 = pnu.getPetriNetFromPnmlFile("2.pnml");
        int result = PDD.distance(pn1, pn2);
        System.out.println(result);
    }
}
