package org.jmol.modelsetbio;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import org.jmol.constant.EnumStructure;
import org.jmol.modelset.Atom;
import org.jmol.modelset.LabelToken;
import org.jmol.modelset.ModelSet;
import org.jmol.util.Logger;
import org.jmol.util.Measure;
import org.jmol.util.OutputStringBuffer;
import org.jmol.viewer.Viewer;

public class AlphaPolymer extends BioPolymer {

    AlphaPolymer(Monomer[] monomers) {
        super(monomers);
    }

    @Override
    protected Point3f getControlPoint(int i, Vector3f v) {
        if (!monomers[i].isSheet()) return leadPoints[i];
        v.sub(leadMidpoints[i], leadPoints[i]);
        v.scale(sheetSmoothing);
        Point3f pt = new Point3f(leadPoints[i]);
        pt.add(v);
        return pt;
    }

    @Override
    public void getPdbData(Viewer viewer, char ctype, char qtype, int mStep, int derivType, BitSet bsAtoms, BitSet bsSelected, boolean bothEnds, boolean isDraw, boolean addHeader, LabelToken[] tokens, OutputStringBuffer pdbATOM, StringBuffer pdbCONECT, BitSet bsWritten) {
        getPdbData(viewer, this, ctype, qtype, mStep, derivType, bsAtoms, bsSelected, bothEnds, isDraw, addHeader, tokens, pdbATOM, pdbCONECT, bsWritten);
    }

    @Override
    public void addSecondaryStructure(EnumStructure type, String structureID, int serialID, int strandCount, char startChainID, int startSeqcode, char endChainID, int endSeqcode) {
        int indexStart, indexEnd;
        if ((indexStart = getIndex(startChainID, startSeqcode)) == -1 || (indexEnd = getIndex(endChainID, endSeqcode)) == -1) return;
        addSecondaryStructure(type, structureID, serialID, strandCount, indexStart, indexEnd);
    }

    protected void addSecondaryStructure(EnumStructure type, String structureID, int serialID, int strandCount, int indexStart, int indexEnd) {
        if (indexEnd < indexStart) {
            Logger.error("AlphaPolymer:addSecondaryStructure error: " + " indexStart:" + indexStart + " indexEnd:" + indexEnd);
            return;
        }
        int structureCount = indexEnd - indexStart + 1;
        ProteinStructure proteinstructure = null;
        if (type == null) System.out.println("alhapoly null type");
        switch(type) {
            case HELIX:
            case HELIX_ALPHA:
            case HELIX_310:
            case HELIX_PI:
                proteinstructure = new Helix(this, indexStart, structureCount, 0, type);
                break;
            case SHEET:
                proteinstructure = new Sheet(this, indexStart, structureCount, 0, type);
                break;
            case TURN:
                proteinstructure = new Turn(this, indexStart, structureCount, 0);
                break;
            default:
                Logger.error("unrecognized secondary structure type");
                return;
        }
        proteinstructure.structureID = structureID;
        proteinstructure.serialID = serialID;
        proteinstructure.strandCount = strandCount;
        for (int i = indexStart; i <= indexEnd; ++i) monomers[i].setStructure(proteinstructure);
    }

    /**
   * 
   * Algorithm of George Phillips phillips@biochem.wisc.edu
   * 
   * originally a contribution to pyMol as struts.py; adapted here by Bob Hanson
   * for Jmol 1/2010
   * 
   * Return a vector of support posts for rapid prototyping models along the
   * lines of George Phillips for Pymol except on actual molecular segments
   * (biopolymers), not PDB chains (which may or may not be continuous).
   * 
   * Like George, we go from thresh-4 to thresh in units of 1 Angstrom, but we
   * do not require this threshold to be an integer. In addition, we prevent
   * double-creation of struts by tracking where struts are, and we do not look
   * for any addtional end struts if there is a strut already to an atom at a
   * particular biopolymer end. The three parameters are:
   * 
   * set strutDefaultRadius 0.3 set strutSpacingMinimum 6 set strutLengthMaximum
   * 7.0
   * 
   * Struts will be introduced by:
   * 
   * calculate struts {atom set A} {atom set B}
   * 
   * where the two atom sets are optional and default to the currently selected
   * set.
   * 
   * They can be manipulated using the STRUTS command much like any "bond"
   * 
   * struts 0.3 color struts opaque pink connect {atomno=3} {atomno=4} strut
   * 
   * struts only
   * 
   * command
   * 
   * @param modelSet
   * @param bs1
   * @param bs2
   * @param vCA
   * @param thresh
   * @param delta
   * @param allowMultiple
   * @return vector of pairs of atoms
   * 
   */
    @Override
    public List<Atom[]> calculateStruts(ModelSet modelSet, BitSet bs1, BitSet bs2, List<Atom> vCA, float thresh, int delta, boolean allowMultiple) {
        return calculateStrutsStatic(modelSet, bs1, bs2, vCA, thresh, delta, allowMultiple);
    }

    private List<Atom[]> calculateStrutsStatic(ModelSet modelSet, BitSet bs1, BitSet bs2, List<Atom> vCA, float thresh, int delta, boolean allowMultiple) {
        List<Atom[]> vStruts = new ArrayList<Atom[]>();
        float thresh2 = thresh * thresh;
        int n = vCA.size();
        int nEndMin = 3;
        BitSet bsStruts = new BitSet();
        BitSet bsNotAvailable = new BitSet();
        BitSet bsNearbyResidues = new BitSet();
        Atom a1 = vCA.get(0);
        Atom a2;
        int nBiopolymers = modelSet.getBioPolymerCountInModel(a1.modelIndex);
        int[][] biopolymerStartsEnds = new int[nBiopolymers][nEndMin * 2];
        for (int i = 0; i < n; i++) {
            a1 = vCA.get(i);
            int polymerIndex = a1.getPolymerIndexInModel();
            int monomerIndex = a1.getMonomerIndex();
            int bpt = monomerIndex;
            if (bpt < nEndMin) biopolymerStartsEnds[polymerIndex][bpt] = i + 1;
            bpt = ((Monomer) a1.getGroup()).getBioPolymerLength() - monomerIndex - 1;
            if (bpt < nEndMin) biopolymerStartsEnds[polymerIndex][nEndMin + bpt] = i + 1;
        }
        float[] d2 = new float[n * (n - 1) / 2];
        for (int i = 0; i < n; i++) {
            a1 = vCA.get(i);
            for (int j = i + 1; j < n; j++) {
                int ipt = strutPoint(i, j, n);
                a2 = vCA.get(j);
                int resno1 = a1.getResno();
                int polymerIndex1 = a1.getPolymerIndexInModel();
                int resno2 = a2.getResno();
                int polymerIndex2 = a2.getPolymerIndexInModel();
                if (polymerIndex1 == polymerIndex2 && Math.abs(resno2 - resno1) < delta) bsNearbyResidues.set(ipt);
                float d = d2[ipt] = a1.distanceSquared(a2);
                if (d >= thresh2) bsNotAvailable.set(ipt);
            }
        }
        for (int t = 5; --t >= 0; ) {
            thresh2 = (thresh - t) * (thresh - t);
            for (int i = 0; i < n; i++) if (allowMultiple || !bsStruts.get(i)) for (int j = i + 1; j < n; j++) {
                int ipt = strutPoint(i, j, n);
                if (!bsNotAvailable.get(ipt) && !bsNearbyResidues.get(ipt) && (allowMultiple || !bsStruts.get(j)) && d2[ipt] <= thresh2) setStrut(i, j, n, vCA, bs1, bs2, vStruts, bsStruts, bsNotAvailable, bsNearbyResidues, delta);
            }
        }
        for (int b = 0; b < nBiopolymers; b++) {
            for (int k = 0; k < nEndMin * 2; k++) {
                int i = biopolymerStartsEnds[b][k] - 1;
                if (i >= 0 && bsStruts.get(i)) {
                    for (int j = 0; j < nEndMin; j++) {
                        int pt = (k / nEndMin) * nEndMin + j;
                        if ((i = biopolymerStartsEnds[b][pt] - 1) >= 0) bsStruts.set(i);
                        biopolymerStartsEnds[b][pt] = -1;
                    }
                }
            }
            if (biopolymerStartsEnds[b][0] == -1 && biopolymerStartsEnds[b][nEndMin] == -1) continue;
            boolean okN = false;
            boolean okC = false;
            int iN = 0;
            int jN = 0;
            int iC = 0;
            int jC = 0;
            float minN = Float.MAX_VALUE;
            float minC = Float.MAX_VALUE;
            for (int j = 0; j < n; j++) for (int k = 0; k < nEndMin * 2; k++) {
                int i = biopolymerStartsEnds[b][k] - 1;
                if (i == -2) {
                    k = (k / nEndMin + 1) * nEndMin - 1;
                    continue;
                }
                if (j == i || i == -1) continue;
                int ipt = strutPoint(i, j, n);
                if (bsNearbyResidues.get(ipt) || d2[ipt] > (k < nEndMin ? minN : minC)) continue;
                if (k < nEndMin) {
                    if (bsNotAvailable.get(ipt)) okN = true;
                    jN = j;
                    iN = i;
                    minN = d2[ipt];
                } else {
                    if (bsNotAvailable.get(ipt)) okC = true;
                    jC = j;
                    iC = i;
                    minC = d2[ipt];
                }
            }
            if (okN) setStrut(iN, jN, n, vCA, bs1, bs2, vStruts, bsStruts, bsNotAvailable, bsNearbyResidues, delta);
            if (okC) setStrut(iC, jC, n, vCA, bs1, bs2, vStruts, bsStruts, bsNotAvailable, bsNearbyResidues, delta);
        }
        return vStruts;
    }

    private static int strutPoint(int i, int j, int n) {
        return (j < i ? j * (2 * n - j - 1) / 2 + i - j - 1 : i * (2 * n - i - 1) / 2 + j - i - 1);
    }

    private static void setStrut(int i, int j, int n, List<Atom> vCA, BitSet bs1, BitSet bs2, List<Atom[]> vStruts, BitSet bsStruts, BitSet bsNotAvailable, BitSet bsNearbyResidues, int delta) {
        Atom a1 = vCA.get(i);
        Atom a2 = vCA.get(j);
        if (!bs1.get(a1.index) || !bs2.get(a2.index)) return;
        vStruts.add(new Atom[] { a1, a2 });
        bsStruts.set(i);
        bsStruts.set(j);
        for (int k1 = Math.max(0, i - delta); k1 <= i + delta && k1 < n; k1++) {
            for (int k2 = Math.max(0, j - delta); k2 <= j + delta && k2 < n; k2++) {
                if (k1 == k2) {
                    continue;
                }
                int ipt = strutPoint(k1, k2, n);
                if (!bsNearbyResidues.get(ipt)) {
                    bsNotAvailable.set(ipt);
                }
            }
        }
    }

    /**
   * Uses Levitt & Greer algorithm to calculate protein secondary
   * structures using only alpha-carbon atoms.
   *<p>
   * Levitt and Greer <br />
   * Automatic Identification of Secondary Structure in Globular Proteins <br />
   * J.Mol.Biol.(1977) 114, 181-293 <br />
   *<p>
   * <a
   * href='http://csb.stanford.edu/levitt/Levitt_JMB77_Secondary_structure.pdf'>
   * http://csb.stanford.edu/levitt/Levitt_JMB77_Secondary_structure.pdf
   * </a>
   */
    @Override
    public void calculateStructures(boolean alphaOnly) {
        if (monomerCount < 4) return;
        float[] angles = calculateAnglesInDegrees();
        Code[] codes = calculateCodes(angles);
        checkBetaSheetAlphaHelixOverlap(codes, angles);
        EnumStructure[] tags = calculateRunsFourOrMore(codes);
        extendRuns(tags);
        searchForTurns(codes, angles, tags);
        addStructuresFromTags(tags);
    }

    private enum Code {

        NADA, RIGHT_HELIX, BETA_SHEET, LEFT_HELIX, LEFT_TURN, RIGHT_TURN
    }

    private float[] calculateAnglesInDegrees() {
        float[] angles = new float[monomerCount];
        for (int i = monomerCount - 1; --i >= 2; ) angles[i] = Measure.computeTorsion(monomers[i - 2].getLeadAtom(), monomers[i - 1].getLeadAtom(), monomers[i].getLeadAtom(), monomers[i + 1].getLeadAtom(), true);
        return angles;
    }

    private Code[] calculateCodes(float[] angles) {
        Code[] codes = new Code[monomerCount];
        for (int i = monomerCount - 1; --i >= 2; ) {
            float degrees = angles[i];
            codes[i] = ((degrees >= 10 && degrees < 120) ? Code.RIGHT_HELIX : ((degrees >= 120 || degrees < -90) ? Code.BETA_SHEET : ((degrees >= -90 && degrees < 0) ? Code.LEFT_HELIX : Code.NADA)));
        }
        return codes;
    }

    private void checkBetaSheetAlphaHelixOverlap(Code[] codes, float[] angles) {
        for (int i = monomerCount - 2; --i >= 2; ) if (codes[i] == Code.BETA_SHEET && angles[i] <= 140 && codes[i - 2] == Code.RIGHT_HELIX && codes[i - 1] == Code.RIGHT_HELIX && codes[i + 1] == Code.RIGHT_HELIX && codes[i + 2] == Code.RIGHT_HELIX) codes[i] = Code.RIGHT_HELIX;
    }

    private EnumStructure[] calculateRunsFourOrMore(Code[] codes) {
        EnumStructure[] tags = new EnumStructure[monomerCount];
        EnumStructure tag = EnumStructure.NONE;
        Code code = Code.NADA;
        int runLength = 0;
        for (int i = 0; i < monomerCount; ++i) {
            if (codes[i] == code && code != Code.NADA && code != Code.BETA_SHEET) {
                ++runLength;
                if (runLength == 4) {
                    tag = (code == Code.BETA_SHEET ? EnumStructure.SHEET : EnumStructure.HELIX);
                    for (int j = 4; --j >= 0; ) tags[i - j] = tag;
                } else if (runLength > 4) tags[i] = tag;
            } else {
                runLength = 1;
                code = codes[i];
            }
        }
        return tags;
    }

    private void extendRuns(EnumStructure[] tags) {
        for (int i = 1; i < monomerCount - 4; ++i) if (tags[i] == EnumStructure.NONE && tags[i + 1] != EnumStructure.NONE) tags[i] = tags[i + 1];
        tags[0] = tags[1];
        tags[monomerCount - 1] = tags[monomerCount - 2];
    }

    private void searchForTurns(Code[] codes, float[] angles, EnumStructure[] tags) {
        for (int i = monomerCount - 1; --i >= 2; ) {
            codes[i] = Code.NADA;
            if (tags[i] == null || tags[i] == EnumStructure.NONE) {
                float angle = angles[i];
                if (angle >= -90 && angle < 0) codes[i] = Code.LEFT_TURN; else if (angle >= 0 && angle < 90) codes[i] = Code.RIGHT_TURN;
            }
        }
        for (int i = monomerCount - 1; --i >= 0; ) {
            if (codes[i] != Code.NADA && codes[i + 1] == codes[i] && tags[i] == EnumStructure.NONE) tags[i] = EnumStructure.TURN;
        }
    }

    private void addStructuresFromTags(EnumStructure[] tags) {
        int i = 0;
        while (i < monomerCount) {
            EnumStructure tag = tags[i];
            if (tag == null || tag == EnumStructure.NONE) {
                ++i;
                continue;
            }
            int iMax;
            for (iMax = i + 1; iMax < monomerCount && tags[iMax] == tag; ++iMax) {
            }
            addSecondaryStructure(tag, null, 0, 0, i, iMax - 1);
            i = iMax;
        }
    }
}
