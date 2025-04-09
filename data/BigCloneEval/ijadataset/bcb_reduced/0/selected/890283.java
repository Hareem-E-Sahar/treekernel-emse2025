package uk.ac.ebi.pride.mol;

import java.util.HashMap;
import java.util.Map;

/**
 * IsoelectricPointUtils is used to calculate the theoretical isoelectric point of a peptide
 * <p/>
 * At the moment we only support one method, and the peptide PTMs are not taken into account
 *
 * <p/>
 * <p/>
 * User: Yasset Perez Riverol
 * Date: 03/08/2011
 * Time: 13:55
 */
public class IsoelectricPointUtils {

    private static final BjellpI bjellpI = new BjellpI();

    public static double calculate(String peptideSeq) {
        peptideSeq = replaceSpecialAA(peptideSeq);
        return bjellpI.calculate(peptideSeq);
    }

    private static String replaceSpecialAA(String seq) {
        for (int i = 0; i < seq.length(); i++) {
            if (!bjellpI.Cterm_pI_expasy.containsKey(String.valueOf(seq.charAt(i)))) {
                seq = seq.replaceFirst(String.valueOf(seq.charAt(i)), "");
                i--;
            }
        }
        return seq;
    }

    public static class BjellpI {

        private Map<String, Double> Cterm_pI_expasy = new HashMap<String, Double>();

        private Map<String, Double> Nterm_pI_expasy = new HashMap<String, Double>();

        private Map<String, Double> sideGroup_pI_expasy = new HashMap<String, Double>();

        private double FoRmU = 0.0D;

        private String seq = null;

        public BjellpI() {
            fillMaps();
        }

        public double calculate(String sequence) {
            this.seq = sequence;
            final double epsilon = 0.001;
            final int iterationMax = 10000;
            int counter = 0;
            double pHs = -2;
            double pHe = 16;
            double pHm = 0;
            while ((counter < iterationMax) && (Math.abs(pHs - pHe) >= epsilon)) {
                pHm = (pHs + pHe) / 2;
                final double pcs = getpI(Nterm_pI_expasy, Cterm_pI_expasy, sideGroup_pI_expasy, pHs);
                final double pcm = getpI(Nterm_pI_expasy, Cterm_pI_expasy, sideGroup_pI_expasy, pHm);
                if (pcs < 0) {
                    return pHs;
                }
                if (((pcs < 0) && (pcm > 0)) || ((pcs > 0) && (pcm < 0))) {
                    pHe = pHm;
                } else {
                    pHs = pHm;
                }
                counter++;
            }
            double pHround = Math.round(((pHs + pHe) / 2) * 100.0D);
            return (pHround / 100.0D);
        }

        private double getpI(Map<String, Double> AApI_n, Map<String, Double> AApI_c, Map<String, Double> AApI_side, double PH) {
            String sideAA;
            double pHpK = 0.0D;
            double FoRmU = 0.0D;
            String ntermAA = String.valueOf(this.seq.charAt(0));
            pHpK = PH - Double.valueOf(AApI_n.get(ntermAA).toString()).doubleValue();
            FoRmU += 1.0D / (1.0D + Math.pow(10.0D, pHpK));
            String cterm = String.valueOf(this.seq.charAt(this.seq.length() - 1));
            pHpK = Double.valueOf(AApI_c.get(cterm).toString()).doubleValue() - PH;
            FoRmU += -1.0D / (1.0D + Math.pow(10.0D, pHpK));
            for (int t = 0; t < this.seq.length(); ++t) {
                sideAA = String.valueOf(this.seq.charAt(t));
                if (AApI_side.containsKey(sideAA)) {
                    double value = Double.valueOf(AApI_side.get(sideAA).toString()).doubleValue();
                    if (value < 0.0D) {
                        pHpK = PH + value;
                        FoRmU += 1.0D / (1.0D + Math.pow(10.0D, pHpK));
                    } else {
                        pHpK = value - PH;
                        FoRmU += -1.0D / (1.0D + Math.pow(10.0D, pHpK));
                    }
                }
            }
            return FoRmU;
        }

        private void fillMaps() {
            this.Cterm_pI_expasy.put("A", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("R", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("N", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("D", Double.valueOf(4.55D));
            this.Cterm_pI_expasy.put("C", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("E", Double.valueOf(4.75D));
            this.Cterm_pI_expasy.put("Q", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("G", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("H", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("I", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("L", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("K", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("M", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("F", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("P", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("S", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("T", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("W", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("Y", Double.valueOf(3.55D));
            this.Cterm_pI_expasy.put("V", Double.valueOf(3.55D));
            this.Nterm_pI_expasy.put("A", Double.valueOf(7.59D));
            this.Nterm_pI_expasy.put("R", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("N", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("D", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("C", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("E", Double.valueOf(7.7D));
            this.Nterm_pI_expasy.put("Q", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("G", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("H", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("I", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("L", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("K", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("M", Double.valueOf(7.0D));
            this.Nterm_pI_expasy.put("F", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("P", Double.valueOf(8.359999999999999D));
            this.Nterm_pI_expasy.put("S", Double.valueOf(6.93D));
            this.Nterm_pI_expasy.put("T", Double.valueOf(6.82D));
            this.Nterm_pI_expasy.put("W", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("Y", Double.valueOf(7.5D));
            this.Nterm_pI_expasy.put("V", Double.valueOf(7.44D));
            this.sideGroup_pI_expasy.put("R", Double.valueOf(-12.0D));
            this.sideGroup_pI_expasy.put("D", Double.valueOf(4.05D));
            this.sideGroup_pI_expasy.put("C", Double.valueOf(9.0D));
            this.sideGroup_pI_expasy.put("E", Double.valueOf(4.45D));
            this.sideGroup_pI_expasy.put("H", Double.valueOf(-5.98D));
            this.sideGroup_pI_expasy.put("K", Double.valueOf(-10.0D));
            this.sideGroup_pI_expasy.put("Y", Double.valueOf(10.0D));
        }
    }
}
