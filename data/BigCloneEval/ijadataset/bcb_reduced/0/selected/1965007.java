package org.proteomecommons.MSExpedite.Graph;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

public class AAUtilities implements AminoAcids {

    public static final int RESIDUE_DOES_NOT_EXIST = -1;

    static final short TRAVERSE_RIGHT = 0;

    static final short TRAVERSE_LEFT = 1;

    public static float[] ptmMasses = null;

    public static String[] ptmNames = null;

    static final HashMap<String, Float> massMap = new HashMap<String, Float>();

    static final HashMap<String, Float> ptmMap = new HashMap<String, Float>(300, 0.7f);

    public static Dipeptide[] dipeptide;

    public static PTM[] ptms;

    public static Residue[] residues;

    public static String[] names = AminoAcids.names;

    public static String[] names3 = AminoAcids.names3;

    public static float[] masses = AminoAcids.masses;

    public static int[][] immoniumMass = AminoAcids.immoniumMass;

    public static final String RESIDUE_SEPARATOR = "/";

    public static float resolution = 0.5f;

    public AAUtilities() {
    }

    public static final float getMass(String name, int type) {
        float f = Float.MIN_VALUE;
        switch(type) {
            case RESIDUE:
            case DIPEPTIDE:
                f = getMass(name);
                break;
            case PTM:
                f = getPTM(name).floatValue();
                break;
            default:
                break;
        }
        return f;
    }

    public static final float getMass(String name) {
        if (massMap.size() == 0) {
            buildMassMap();
        }
        char c[] = name.toCharArray();
        float mass = 0.0f;
        for (int i = 0; i < c.length; i++) {
            char carr[] = { c[i] };
            String s = new String(carr);
            if (s.equals(RESIDUE_SEPARATOR)) {
                ++i;
                continue;
            }
            if (massMap.get(s) == null) continue;
            mass += ((Float) massMap.get(new String(s))).floatValue();
        }
        return mass;
    }

    public static final void buildMassMap(Residue[] residues) {
        if (residues != null) {
            for (int i = 0; i < residues.length; i++) {
                massMap.put(residues[i].getSingleLetterDesignation(), new Float(residues[i].getMass()));
            }
        } else {
            buildMassMap();
        }
    }

    static void buildPTMMap() {
        if (ptmMasses == null) return;
        ptmMap.clear();
        for (int i = 0; i < ptmMasses.length; i++) {
            ptmMap.put(ptmNames[i], new Float(ptmMasses[i]));
        }
    }

    static Float getPTM(String name) {
        if (ptmMap.size() == 0) buildPTMMap();
        return (Float) ptmMap.get(name);
    }

    static void buildMassMap() {
        massMap.put("A", new Float(71.0371));
        massMap.put("R", new Float(156.1011));
        massMap.put("N", new Float(114.0429));
        massMap.put("D", new Float(115.0270));
        massMap.put("C", new Float(103.0092));
        massMap.put("E", new Float(129.0426));
        massMap.put("Q", new Float(128.0586));
        massMap.put("G", new Float(57.0215));
        massMap.put("H", new Float(137.0589));
        massMap.put("I", new Float(113.0841));
        massMap.put("L", new Float(113.0841));
        massMap.put("K", new Float(128.0950));
        massMap.put("M", new Float(131.0405));
        massMap.put("F", new Float(147.0684));
        massMap.put("P", new Float(97.0528));
        massMap.put("S", new Float(87.0320));
        massMap.put("T", new Float(101.0477));
        massMap.put("W", new Float(186.0793));
        massMap.put("Y", new Float(163.0633));
        massMap.put("V", new Float(99.0684));
    }

    public static final AnnotationElement[] getIons(Point2f refIndex, Array2D peaks, short direction) {
        AnnotationElement al[] = new AnnotationElement[0];
        switch(direction) {
            case TRAVERSE_LEFT:
                al = getLHIons(refIndex, peaks);
                break;
            case TRAVERSE_RIGHT:
                al = getRHIons(refIndex, peaks);
                break;
        }
        return al;
    }

    public static final LinkedList<AnnotationElement> getBIons(String sequence, float tol, Array2D peaks) {
        float tmpMass = 0.0f;
        LinkedList<AnnotationElement> aes = new LinkedList<AnnotationElement>();
        for (int i = 0; i < sequence.length(); i++) {
            String s = null;
            if ((i + 1) == sequence.length()) s = sequence.substring(i); else s = sequence.substring(i, i + 1);
            tmpMass += massMap.get(s);
            int index = hasIon(tmpMass, peaks, tol);
            if (index == RESIDUE_DOES_NOT_EXIST) {
            } else {
            }
        }
        return aes;
    }

    public static int hasIon(float mass, Array2D peaks, float tol) {
        for (int i = 0; i < peaks.length(); i++) {
            float diff = Math.abs(mass - peaks.x[i] + 1);
            if (diff < tol) return i;
        }
        return RESIDUE_DOES_NOT_EXIST;
    }

    public static final LinkedList<AnnotationSegment> annotateSequences(String sequence, float tol, Array2D peaks) {
        LinkedList<AnnotationSegment> list = new LinkedList<AnnotationSegment>();
        int lastSegmentIndex = 0;
        AnnotationSegment seg = new AnnotationSegment(lastSegmentIndex++);
        for (int i = 0; i < peaks.length(); i++) {
            annotateSequence(seg, sequence, tol, i, peaks);
            if (seg.size() > 0) {
                list.add(seg);
                seg = new AnnotationSegment(lastSegmentIndex++);
            } else {
            }
        }
        return list;
    }

    public static final AnnotationSegment annotateSequence(AnnotationSegment destSeg, String sequence, float tol, int start, Array2D peaks) {
        int lastIndex = start;
        for (int i = start; i < peaks.length(); i++) {
            for (int j = 0; j < sequence.length(); j++) {
                String s = null;
                if ((j + 1) == sequence.length()) s = sequence.substring(j); else s = sequence.substring(j, j + 1);
                int resIndex = residueExists(s, tol, lastIndex + 1, peaks);
                if (resIndex == RESIDUE_DOES_NOT_EXIST) {
                    return destSeg;
                }
                if (i == start) {
                    destSeg.setReferencePeak(new Point2f(peaks.x[start], peaks.y[start]));
                }
                AnnotationElement element = new AnnotationElement();
                element.setIonName(s);
                element.setPeakIndex(resIndex);
                element.setLocation(peaks.x[resIndex], peaks.y[resIndex]);
                lastIndex = resIndex;
                destSeg.add(element);
            }
        }
        return destSeg;
    }

    public static final int residueExists(String residue, float tol, int start, Array2D peaks) {
        double resMass = massMap.get(residue);
        double diff = 0;
        for (int i = start; i < peaks.length(); i++) {
            double m1 = peaks.x[i];
            for (int j = i + 1; j < peaks.length(); j++) {
                double m2 = peaks.x[j];
                diff = Math.abs(m2 - m1);
                if (Math.abs(diff - resMass) <= tol) return j;
                if (diff > resMass) break;
            }
        }
        return RESIDUE_DOES_NOT_EXIST;
    }

    public static final AnnotationElement[] getPTMs(Point2f refIndex, Array2D peaks, short direction) {
        AnnotationElement al[] = new AnnotationElement[0];
        switch(direction) {
            case TRAVERSE_LEFT:
                al = getLHPTMs(refIndex, peaks);
                break;
            case TRAVERSE_RIGHT:
                al = getRHPTMs(refIndex, peaks);
                break;
        }
        return al;
    }

    public static final AnnotationElement[] getDipeptides(Point2f refIndex, Array2D peaks, short direction) {
        AnnotationElement al[] = new AnnotationElement[0];
        switch(direction) {
            case TRAVERSE_LEFT:
                al = getLHDipeptides(refIndex, peaks);
                break;
            case TRAVERSE_RIGHT:
                al = getRHDipeptides(refIndex, peaks);
                break;
        }
        return al;
    }

    public static final synchronized void buildDipeptideTable() {
        float dmDiPeptide = 0.0f;
        ArrayList dipeptides = new ArrayList();
        for (int j = 0; j < masses.length; j++) {
            for (int k = j; k < masses.length; k++) {
                dmDiPeptide = masses[j] + masses[k];
                String name = names[j] + names[k];
                dipeptides.add(new Dipeptide(name, dmDiPeptide));
            }
        }
        dipeptide = (Dipeptide[]) dipeptides.toArray(new Dipeptide[0]);
        Arrays.sort(dipeptide, new Comparator() {

            public int compare(Object a1, Object a2) {
                Dipeptide a = (Dipeptide) a1;
                Dipeptide b = (Dipeptide) a2;
                if (a.mass < b.mass) {
                    return -1;
                } else if (a.mass == b.mass) {
                    return 0;
                } else if (a.mass > b.mass) {
                    return 1;
                }
                return -2;
            }
        });
    }

    public static final float getDipeptideMass(String peptides) {
        if (peptides == null) return 0;
        char c[] = peptides.toCharArray();
        float mass = 0;
        for (int i = 0; i < c.length; i++) {
            char[] carray = { c[i] };
            String s = new String(carray);
            mass += getMass(s);
        }
        return mass;
    }

    public static final String getThreeLetterNameFor(String s) {
        for (int i = 0; i < names.length; i++) {
            if (s.equals(names[i])) {
                return names3[i];
            }
        }
        return "";
    }

    public static final int getNearestDipeptideIndex(float refMass) {
        float dm = 0, dmDiPeptide = 0;
        if (dipeptide == null || dipeptide.length == 0) {
            buildDipeptideTable();
        }
        if ((refMass - dipeptide[0].mass) <= resolution) return 0;
        if (Math.abs(refMass - dipeptide[dipeptide.length - 1].mass) <= resolution) return (dipeptide.length - 1);
        if (refMass > (dipeptide[dipeptide.length - 1].mass + resolution)) return dipeptide.length - 1;
        for (int i = 1; i < dipeptide.length; i++) {
            if (refMass > dipeptide[i - 1].mass && dipeptide[i].mass > refMass) {
                int index = getNearestDipeptideIndex(refMass, i - 1, i);
                return index;
            }
        }
        return -1;
    }

    public static final String getDipeptideName(float mass) {
        if (dipeptide == null || dipeptide.length == 0) {
            buildDipeptideTable();
        }
        for (int i = 0; i < dipeptide.length; i++) {
            if (mass == dipeptide[i].mass) return dipeptide[i].name;
        }
        return "";
    }

    public static final String getNearestDipeptide(float refMass) {
        float dm = 0, dmDiPeptide = 0;
        String s = null;
        if (dipeptide == null || dipeptide.length == 0) {
            buildDipeptideTable();
        }
        if ((refMass - dipeptide[0].mass) <= resolution) return dipeptide[0].name;
        if (Math.abs(refMass - dipeptide[dipeptide.length - 1].mass) <= resolution) return dipeptide[dipeptide.length - 1].name;
        if (refMass > (dipeptide[dipeptide.length - 1].mass + resolution)) return "";
        for (int i = 1; i < dipeptide.length; i++) {
            if (refMass > dipeptide[i - 1].mass && dipeptide[i].mass > refMass) {
                int index = getNearestDipeptideIndex(refMass, i - 1, i);
                return dipeptide[i].name;
            }
        }
        return "";
    }

    public static final double getPercentError(float refMass, float theoreticalMass) {
        return (100 * (Math.abs(theoreticalMass - refMass)) / theoreticalMass);
    }

    public static final float getPPM(float refMass, float theoreticalMass) {
        return (float) (1E6 * (Math.abs(theoreticalMass - Math.abs(refMass))) / theoreticalMass);
    }

    public static final String getDegenerateAminoAcidName(String aa) {
        if (aa.equalsIgnoreCase("Q")) return "K";
        if (aa.equalsIgnoreCase("K")) return "Q";
        if (aa.equalsIgnoreCase("I")) return "L";
        if (aa.equalsIgnoreCase("L")) return "I";
        return "";
    }

    public static final int getNearestIndex(float refMass, float[] m) {
        if (m == null || m.length == 0) return -1;
        if (refMass < m[0]) return 0;
        if (refMass > m[m.length - 1]) return (m.length - 1);
        for (int i = 1; i < m.length; i++) {
            if (refMass > m[i - 1] && m[i] > refMass) {
                int index = getNearestIndex(refMass, m, i - 1, i);
                return index;
            }
        }
        return -1;
    }

    public static final int getNearestIndex(float mass, float[] masses, int lowIndex, int highIndex) {
        float lm = masses[lowIndex];
        float hm = masses[highIndex];
        float m = (lm + hm) / 2;
        if (mass > m) return highIndex;
        return lowIndex;
    }

    public static final float getNearestDipeptideMass(float mass) {
        int index = getNearestDipeptideIndex(mass);
        return dipeptide[index].mass;
    }

    public static final float getNearestPTMMass(float mass) {
        int index = getNearestPTMIndex(mass);
        if (index == -1) return Float.MAX_VALUE;
        return ptmMasses[index];
    }

    public static final float getNearestResidueMass(float mass) {
        int index = getNearestResidueIndex(mass);
        return masses[index];
    }

    public static final int getNearestResidueIndex(float mass) {
        return getNearestIndex(mass, masses);
    }

    public static final int getNearestPTMIndex(float mass) {
        return getNearestIndex(mass, ptmMasses);
    }

    public static final String getPTMName(float mass) {
        if (ptmNames == null || ptmMasses == null) return "";
        for (int i = 0; i < ptmMasses.length; i++) {
            if (ptmMasses[i] == mass) return ptmNames[i];
        }
        return "";
    }

    public static final String getResidueName(float aaMass) {
        for (int i = 0; i < masses.length; i++) {
            if (aaMass == masses[i]) {
                return names[i];
            }
        }
        return "";
    }

    public static final String getNearestAminoAcidName(float mass) {
        if ((mass - masses[0]) <= resolution) return names[0];
        if (Math.abs(mass - masses[masses.length - 1]) <= resolution) return names[masses.length - 1];
        if (mass > (masses[masses.length - 1] + resolution)) return "";
        for (int i = 1; i < masses.length; i++) {
            if (mass > masses[i - 1] && masses[i] > mass) {
                int index = getNearestIndex(mass, masses, i - 1, i);
                return names[index];
            }
        }
        return "";
    }

    static int getNearestDipeptideIndex(float mass, int lowIndex, int highIndex) {
        float lm = dipeptide[lowIndex].mass;
        float hm = dipeptide[highIndex].mass;
        float m = (lm + hm) / 2;
        if (mass > m) return highIndex;
        return lowIndex;
    }

    public static final AnnotationElement[] getLHPTMs(Point2f refIndex, Array2D peaks) {
        if (peaks == null || ptmMasses == null || ptmMasses.length == 0) return new AnnotationElement[0];
        final float refMass = (float) refIndex.getX();
        double dm = 0;
        final LinkedList<AnnotationElement> al = new LinkedList<AnnotationElement>();
        final int length = peaks.length();
        for (int i = 0; i < length; i++) {
            dm = refMass - peaks.x[i];
            if (dm < ptmMasses[0]) continue;
            if (dm > ptmMasses[ptmMasses.length - 1]) return (AnnotationElement[]) al.toArray(new AnnotationElement[0]);
            for (int j = 0; j < ptmMasses.length; j++) {
                if (Math.abs(dm - ptmMasses[j]) < 0.1) {
                    AnnotationElement annot = new AnnotationElement(new Point2f(peaks.x[i], peaks.y[i]), ptmNames[j], (float) dm);
                    annot.setObservedMass((float) dm);
                    annot.setType(PTM);
                    al.add(annot);
                }
            }
        }
        return (AnnotationElement[]) al.toArray(new AnnotationElement[0]);
    }

    public static final AnnotationElement[] getRHPTMs(Point2f refIndex, Array2D peaks) {
        if (peaks == null || ptmMasses == null || ptmMasses.length == 0) return new AnnotationElement[0];
        final float refMass = (float) refIndex.getX();
        double dm = 0;
        final ArrayList al = new ArrayList();
        final int length = peaks.length();
        for (int i = 0; i < length; i++) {
            dm = peaks.x[i] - refMass;
            if (dm < ptmMasses[0]) continue;
            if (dm > ptmMasses[ptmMasses.length - 1]) return (AnnotationElement[]) al.toArray(new AnnotationElement[0]);
            for (int j = 0; j < ptmMasses.length; j++) {
                if (Math.abs(dm - ptmMasses[j]) < 0.1) {
                    AnnotationElement annot = new AnnotationElement(new Point2f(peaks.x[i], peaks.y[i]), ptmNames[j], (float) dm);
                    annot.setObservedMass((float) dm);
                    annot.setType(PTM);
                    al.add(annot);
                }
            }
        }
        return (AnnotationElement[]) al.toArray(new AnnotationElement[0]);
    }

    public static void set(Residue[] res) {
        residues = res;
    }

    public static final AnnotationElement[] getRHIons(Point2f refIndex, Array2D peaks) {
        if (peaks == null) return new AnnotationElement[0];
        final float refMass = (float) refIndex.getX();
        double dm = 0;
        final ArrayList al = new ArrayList();
        final int length = peaks.length();
        for (int i = 0; i < length; i++) {
            dm = peaks.x[i] - refMass;
            if (dm < MIN_AMINOACID_MASS - 1) continue;
            if (dm > MAX_AMINOACID_MASS + 1) return (AnnotationElement[]) al.toArray(new AnnotationElement[0]);
            for (int j = 0; j < masses.length; j++) {
                if (Math.abs(dm - masses[j]) < 0.1) {
                    AnnotationElement annot = new AnnotationElement(new Point2f(peaks.x[i], peaks.y[i]), names[j], (float) dm);
                    annot.setType(RESIDUE);
                    al.add(annot);
                }
            }
        }
        return (AnnotationElement[]) al.toArray(new AnnotationElement[0]);
    }

    public static final int[] getImmoniumMassesForAA(String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                return immoniumMass[i];
            }
        }
        return new int[0];
    }

    public static final float getDipeptideMass(String name1, String name2) {
        Float f1 = (Float) massMap.get(name1);
        Float f2 = (Float) massMap.get(name2);
        return f1.floatValue() + f2.floatValue();
    }

    public static final AnnotationElement[] getRHDipeptides(Point2f refIndex, Array2D peaks) {
        if (peaks == null) return new AnnotationElement[0];
        final float refMass = (float) refIndex.getX();
        double dm = 0, dmDiPeptide = 0;
        ArrayList annotation = new ArrayList();
        final int length = peaks.length();
        for (int i = 0; i < length; i++) {
            dm = peaks.x[i] - refMass;
            if (dm < MIN_DIPEPTIDE_MASS) continue;
            if (dm > MAX_DIPEPTIDE_MASS) return (AnnotationElement[]) annotation.toArray(new AnnotationElement[0]);
            ;
            for (int j = 0; j < masses.length; j++) {
                for (int k = j; k < masses.length; k++) {
                    dmDiPeptide = masses[j] + masses[k];
                    if ((int) dm == (int) dmDiPeptide) {
                        AnnotationElement element = new AnnotationElement(new Point2f(peaks.x[i], peaks.y[i]), names[j] + names[k], (float) dm);
                        element.setType(DIPEPTIDE);
                        annotation.add(element);
                    }
                }
            }
        }
        return (AnnotationElement[]) annotation.toArray(new AnnotationElement[0]);
    }

    public static void set(PTM[] ptm) {
        ptms = ptm;
    }

    public static void reset() {
        makeTableOfResidues();
        resetPTMs();
    }

    public static void resetPTMs() {
        makeTableOfPTMs(ptms);
        buildPTMMap();
    }

    static void makeTableOfResidues() {
        residues = orderResidues(residues);
        buildMassMap(residues);
        names = new String[residues.length];
        names3 = new String[residues.length];
        masses = new float[residues.length];
        immoniumMass = new int[residues.length][];
        for (int i = 0; i < residues.length; i++) {
            names[i] = residues[i].getSingleLetterDesignation();
            names3[i] = residues[i].getThreeLetterDesignation();
            masses[i] = residues[i].getMass();
            immoniumMass[i] = residues[i].getImmoniumMasses();
        }
    }

    static PTM[] orderPTMs(PTM[] ptm) {
        Arrays.sort(ptm, new Comparator() {

            public int compare(Object a1, Object a2) {
                PTM a = (PTM) a1;
                PTM b = (PTM) a2;
                if (a.getMass() < b.getMass()) {
                    return -1;
                } else if (a.getMass() == b.getMass()) {
                    return 0;
                } else if (a.getMass() > b.getMass()) {
                    return 1;
                }
                return -2;
            }
        });
        return ptm;
    }

    static Residue[] orderResidues(Residue[] residues) {
        Arrays.sort(residues, new Comparator() {

            public int compare(Object a1, Object a2) {
                Residue a = (Residue) a1;
                Residue b = (Residue) a2;
                if (a.getMass() < b.getMass()) {
                    return -1;
                } else if (a.getMass() == b.getMass()) {
                    return 0;
                } else if (a.getMass() > b.getMass()) {
                    return 1;
                }
                return -2;
            }
        });
        return residues;
    }

    static void makeTableOfPTMs(PTM[] ptm) {
        if (ptm == null) {
            ptmMasses = null;
            ptmNames = null;
            return;
        }
        ptm = orderPTMs(ptm);
        ptmMasses = new float[ptm.length];
        ptmNames = new String[ptm.length];
        for (int i = 0; i < ptm.length; i++) {
            ptmMasses[i] = ptm[i].getMass();
            ptmNames[i] = ptm[i].getName();
        }
    }

    public static final AnnotationElement[] getLHDipeptides(Point2f refIndex, Array2D peaks) {
        final double refMass = (float) refIndex.getX();
        double dm = 0, dmDiPeptide = 0;
        ArrayList annotation = new ArrayList();
        for (int i = peaks.length() - 1; i > 0; i--) {
            dm = refMass - peaks.x[i];
            if (dm < MIN_DIPEPTIDE_MASS) continue;
            if (dm > MAX_DIPEPTIDE_MASS) return (AnnotationElement[]) annotation.toArray(new AnnotationElement[0]);
            for (int j = 0; j < masses.length; j++) {
                for (int k = j; k < masses.length; k++) {
                    dmDiPeptide = masses[j] + masses[k];
                    if ((int) dm == (int) dmDiPeptide) {
                        AnnotationElement element = new AnnotationElement(new Point2f(peaks.x[i], peaks.y[i]), names[j] + names[k], (float) dm);
                        element.setType(DIPEPTIDE);
                        annotation.add(element);
                    }
                }
            }
        }
        return (AnnotationElement[]) annotation.toArray(new AnnotationElement[0]);
    }

    public static final AnnotationElement[] getLHIons(Point2f refIndex, Array2D peaks) {
        final double refMass = (float) refIndex.getX();
        double dm = 0;
        final ArrayList al = new ArrayList();
        final int length = peaks.length();
        for (int i = peaks.length() - 1; i > 0; i--) {
            dm = refMass - peaks.x[i];
            if (dm < MIN_AMINOACID_MASS - 1) continue;
            if (dm > MAX_AMINOACID_MASS + 1) return (AnnotationElement[]) al.toArray(new AnnotationElement[0]);
            for (int j = 0; j < masses.length; j++) {
                if (Math.abs(dm - masses[j]) < 0.1) {
                    AnnotationElement annot = new AnnotationElement(new Point2f(peaks.x[i], peaks.y[i]), names[j], (float) dm);
                    annot.setType(RESIDUE);
                    al.add(annot);
                }
            }
        }
        return (AnnotationElement[]) al.toArray(new AnnotationElement[0]);
    }

    public static final Integer[] getIndexes(float[] matrix, float mass, float cutoff) {
        if (matrix == null) return new Integer[0];
        ArrayList al = new ArrayList();
        for (int i = 0; i < matrix.length; i++) {
            if (Math.abs(matrix[i] - mass) <= cutoff) {
                al.add(new Integer(i));
            }
            if (matrix[i] > mass + cutoff) break;
        }
        return (Integer[]) al.toArray(new Integer[0]);
    }

    public static final Integer[] getPTMs(float mass, float cutoff) {
        return getIndexes(ptmMasses, mass, cutoff);
    }

    public static final Integer[] getResidues(float mass, float cutoff) {
        return getIndexes(masses, mass, cutoff);
    }

    public static final Integer[] getDipeptide(float mass, float cutoff) {
        ArrayList al = new ArrayList();
        Dipeptide matrix[] = dipeptide;
        if (matrix == null) {
            buildDipeptideTable();
        }
        matrix = dipeptide;
        for (int i = 0; i < matrix.length; i++) {
            if (Math.abs(matrix[i].mass - mass) <= cutoff) {
                al.add(new Integer(i));
            }
            if (matrix[i].mass > mass + cutoff) break;
        }
        return (Integer[]) al.toArray(new Integer[0]);
    }

    public static float getDeltaMass(AnnotationElement e) {
        String ionName = e.getIonName();
        int type = e.getType();
        float mass = Float.MIN_VALUE;
        switch(type) {
            case AminoAcids.RESIDUE:
                mass = massMap.get(ionName);
                break;
            case AminoAcids.DIPEPTIDE:
                mass = getDipeptideMass(ionName);
                break;
            case AminoAcids.PTM:
                mass = ptmMap.get(ionName);
                break;
        }
        return Math.abs(mass - e.getObservedMass());
    }
}
