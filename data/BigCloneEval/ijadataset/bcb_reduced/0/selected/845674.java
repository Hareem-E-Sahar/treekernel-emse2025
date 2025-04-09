package org.expasy.jpl.core.mol.polymer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.expasy.jpl.commons.base.math.DoubleComparator;
import org.expasy.jpl.commons.base.record.CountableRecords;
import org.expasy.jpl.commons.collection.IntegerSequence;
import org.expasy.jpl.commons.collection.symbol.Symbol;
import org.expasy.jpl.commons.collection.symbol.seq.SequenceSymbolCounter;
import org.expasy.jpl.commons.collection.symbol.seq.SymbolSequence;
import org.expasy.jpl.core.mol.chem.MassCalculator;
import org.expasy.jpl.core.mol.chem.api.Molecule;
import org.expasy.jpl.core.mol.modif.IonizableModification;
import org.expasy.jpl.core.mol.modif.Modification;
import org.expasy.jpl.core.mol.modif.ModificationFactory;
import org.expasy.jpl.core.mol.monomer.aa.AAHydropathyManager;
import org.expasy.jpl.core.mol.monomer.aa.AAPKaManager;
import org.expasy.jpl.core.mol.monomer.aa.AminoAcid;
import org.expasy.jpl.core.mol.polymer.modif.ModifContainerManager;
import org.expasy.jpl.core.mol.polymer.pept.AASequence;
import org.expasy.jpl.core.mol.polymer.pept.Peptide;
import org.expasy.jpl.core.mol.polymer.pept.PeptideMerger;

/**
 * A class providing utility methods for {@code BioPolymer}s.
 * 
 * @author nikitin
 * @author Yasset Perez Riverol (for its contribution to the pI part)
 * 
 * @since 1.0
 */
public class BioPolymerUtils {

    private static Log log = LogFactory.getLog(BioPolymerUtils.class);

    private static final DoubleComparator DOUBLE_COMP = DoubleComparator.getDefaultInstance();

    private static final ModifContainerManager MODIF_MANAGER = ModifContainerManager.getInstance();

    private BioPolymerUtils() {
    }

    /**
	 * Merge peptides.
	 * 
	 * @param peptides the peptides to merge.
	 * 
	 * @return the concatenated peptide.
	 */
    public static Peptide merge(final List<Peptide> peptides) {
        PeptideMerger merger = PeptideMerger.newInstance();
        merger.process(peptides);
        return merger.getMergedSequence();
    }

    /**
	 * Merge two peptides.
	 * 
	 * @param peptide1 the first peptide to merge.
	 * @param peptide2 the second peptide to merge.
	 * 
	 * @return the concatenated peptide.
	 */
    public static Peptide merge(final Peptide peptide1, final Peptide peptide2) {
        PeptideMerger merger = PeptideMerger.newInstance();
        merger.process(peptide1, peptide2);
        return merger.getMergedSequence();
    }

    /**
	 * Get the occurrences of monomers in the given sequence.
	 * 
	 * @param sequence the sequence to count monomer on.
	 * @return an instance of SequenceSymbolCounter.
	 */
    public static <T extends Molecule> SequenceSymbolCounter<T> getSymbolCounter(final SymbolSequence<T> sequence) {
        final SequenceSymbolCounter<T> aaCounter = SequenceSymbolCounter.newInstance();
        aaCounter.process(sequence);
        return aaCounter;
    }

    /**
	 * @return the list of monomer's symbol found at given indices in the given
	 *         sequence.
	 */
    public static <T extends Molecule> List<Symbol<T>> getSymbolsAt(final SymbolSequence<T> sequence, final List<Integer> indices) {
        final List<Symbol<T>> aas = new ArrayList<Symbol<T>>();
        for (final int index : indices) {
            aas.add(sequence.getSymbolAt(index));
        }
        return aas;
    }

    /**
	 * @return the list of indices where symbol is located in the sequence.
	 */
    public static <T extends Molecule> List<Integer> getSymbolIndices(final SymbolSequence<T> sequence, final Symbol<T> symbol) {
        final SequenceSymbolCounter<T> aaCounter = SequenceSymbolCounter.newInstance();
        aaCounter.process(sequence);
        return aaCounter.getIndices(symbol);
    }

    /**
	 * @return the list of positions of all the given amino-acids.
	 */
    public static <T extends Molecule> List<Integer> getSymbolIndices(final SymbolSequence<T> sequence, final Set<Symbol<T>> symbols) {
        final SequenceSymbolCounter<T> aaCounter = SequenceSymbolCounter.newInstance();
        aaCounter.process(sequence);
        return aaCounter.getIndices(symbols);
    }

    /**
	 * Compute and return the gravy score of the sequence given the specific
	 * scoring system.
	 * 
	 * @param peptide the peptide to compute the gravy on.
	 * @param hydropathyManager the hydropathy manager.
	 * 
	 * @return gravy score of the given peptide.
	 * 
	 */
    public static float getGravyScore(final AASequence peptide, final AAHydropathyManager hydropathyManager) {
        float score = 0;
        SequenceSymbolCounter<AminoAcid> occs = getSymbolCounter(peptide);
        for (Symbol<AminoAcid> symbol : occs.getSymbols()) {
            score += hydropathyManager.getValueOf(symbol) * occs.getSymbolNumber(symbol);
        }
        return score / peptide.length();
    }

    /**
	 * Compute and return the kyle-doolittle gravy score of the peptide.
	 * 
	 * @param peptide the peptide to compute the gravy on.
	 * 
	 * @return gravy score of the given peptide.
	 * 
	 */
    public static float getGravyScore(final AASequence peptide) {
        return getGravyScore(peptide, AAHydropathyManager.newInstance());
    }

    /**
	 * Compute the gravy scores of the peptide with a given window size and a
	 * specific scoring system.
	 * 
	 * @param peptide the peptide to compute the gravy on.
	 * @param windowSize the window size.
	 * @param hydropathyManager the hydropathy manager.
	 * 
	 * @return gravy scores of the given peptide.
	 * 
	 * @throws IllegalArgumentException if window size has illegal value.
	 * 
	 */
    public static float[] getGravyScores(final AASequence peptide, int windowSize, AAHydropathyManager hydropathyManager) {
        final int seqLength = peptide.length();
        if (windowSize <= 0) {
            throw new IllegalArgumentException(windowSize + " : bad window size.");
        } else if (windowSize > seqLength) {
            windowSize = seqLength;
        }
        final float[] scores = new float[(int) Math.ceil((float) seqLength / windowSize)];
        for (int i = 0; i < seqLength; i++) {
            final Symbol<AminoAcid> aa = peptide.getSymbolAt(i);
            final int scoreIndex = i / windowSize;
            if ((i > 0) && (i % windowSize == 0)) {
                scores[scoreIndex - 1] = scores[scoreIndex - 1] / windowSize;
            }
            scores[scoreIndex] += hydropathyManager.getValueOf(aa);
        }
        if (seqLength % windowSize != 0) {
            scores[seqLength / windowSize] /= seqLength % windowSize;
        }
        return scores;
    }

    /**
	 * Compute the gravy scores of the sequence with a given window size in the
	 * default aa Kyle and Doolittle scoring system (-4.6 < score < 4.6).
	 * 
	 * @param peptide the peptide to compute the gravy on.
	 * @param windowSize the window size.
	 * 
	 * @return gravy scores of the given peptide.
	 * 
	 * @throws IllegalArgumentException if window size has illegal value.
	 */
    public static float[] getGravyScores(final AASequence peptide, final int windowSize) {
        return getGravyScores(peptide, windowSize, AAHydropathyManager.newInstance());
    }

    /**
	 * Compute the net polypeptide charge as the sum of each amino-acid +
	 * terminal partial charges (with the default nozaki pka table).
	 * 
	 * @param polypeptide the polypeptide to compute partial charges on.
	 * @param pH the pH at which the fraction is evaluated
	 * 
	 * @return the net charge of the polypeptide.
	 */
    public static double getNetCharge(final Peptide polypeptide, final double pH) {
        return getNetCharge(polypeptide, pH, AAPKaManager.getInstance());
    }

    /**
	 * Compute the net polypeptide charge as the sum of each amino-acid +
	 * terminal partial charges.
	 * 
	 * @param polypeptide the polypeptide to compute net charge on.
	 * @param pH the pH at which the fraction is evaluated.
	 * @param pkaManager the pka manager.
	 * 
	 * @return the net charge of the polypeptide.
	 */
    public static double getNetCharge(final Peptide polypeptide, final double pH, AAPKaManager pkaManager) {
        double q = 0.0;
        ModifContainerManager modifManager = ModifContainerManager.getInstance();
        if (modifManager.hasNtModif(polypeptide)) {
            CountableRecords<Modification> records = modifManager.getNtModif(polypeptide);
            for (Modification modif : records.getRecords()) {
                if (modif instanceof IonizableModification) {
                    if (log.isDebugEnabled()) {
                        log.debug("nt has a ionizable modif: " + modif);
                    }
                    q = ((IonizableModification) modif).getAvgCharge(pH);
                    if (log.isDebugEnabled()) {
                        log.debug("nt pc at pH " + pH + " = " + q);
                    }
                }
            }
        } else {
            q = pkaManager.getNtPartialCharge(pH);
            if (log.isDebugEnabled()) {
                log.debug("nt: " + pkaManager.getNtPartialCharge(pH));
            }
        }
        for (int i = 0; i < polypeptide.length(); i++) {
            final Symbol<AminoAcid> aa = polypeptide.getSymbolAt(i);
            double aapc = 0;
            if (modifManager.hasModifAt(polypeptide, i)) {
                CountableRecords<Modification> records = modifManager.getModifAt(polypeptide, i);
                for (Modification modif : records.getRecords()) {
                    if (modif instanceof IonizableModification) {
                        aapc += ((IonizableModification) modif).getAvgCharge(pH);
                        if (log.isDebugEnabled()) {
                            log.debug(aa + " at index " + i + " has an ionizable modif: " + modif + " = " + aapc);
                        }
                    }
                }
            }
            aapc += pkaManager.getPartialChargeForAA(aa, pH);
            if (log.isDebugEnabled()) {
                log.debug(aa + " at pH " + pH + " = " + aapc);
            }
            q += aapc;
        }
        if (modifManager.hasCtModif(polypeptide)) {
            CountableRecords<Modification> records = modifManager.getCtModif(polypeptide);
            for (Modification modif : records.getRecords()) {
                if (modif instanceof IonizableModification) {
                    if (log.isDebugEnabled()) {
                        log.debug("ct has a ionizable modif: " + modif);
                    }
                    q += ((IonizableModification) modif).getAvgCharge(pH);
                    if (log.isDebugEnabled()) {
                        log.debug("ct pc at pH " + pH + " = " + ((IonizableModification) modif).getAvgCharge(pH));
                    }
                }
            }
        } else {
            q += pkaManager.getCtPartialCharge(pH);
            if (log.isDebugEnabled()) {
                log.debug("ct: " + pkaManager.getCtPartialCharge(pH));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("### seq pc at pH " + pH + " = " + q);
        }
        return q;
    }

    /**
	 * Get the isoelectric point that is when global charge of a protein/peptide
	 * is neutral.
	 * 
	 * @param peptide the peptide to compute isoelectric point on.
	 * 
	 * @return the isoelectric point.
	 */
    public static double getIsoelectricPoint(final Peptide peptide) {
        return getIsoelectricPoint(peptide, AAPKaManager.getInstance());
    }

    /**
	 * Get the isoelectric point that is when global charge of a protein/peptide
	 * is neutral.
	 * 
	 * @param peptide the peptide to compute isoelectric point on.
	 * @param pkaManager the pka manager.
	 * 
	 * @return the isoelectric point.
	 */
    public static double getIsoelectricPoint(final Peptide peptide, AAPKaManager pkaManager) {
        final double epsilon = 0.001;
        final int iterationMax = 10000;
        int counter = 0;
        double pHs = 0;
        double pHe = 14;
        double pHm = 0;
        while ((counter < iterationMax) && (Math.abs(pHs - pHe) >= epsilon)) {
            pHm = (pHs + pHe) / 2;
            if (log.isDebugEnabled()) {
                log.debug("[" + pHs + ", " + pHm + "]");
            }
            final double ncs = getNetCharge(peptide, pHs, pkaManager);
            final double ncm = getNetCharge(peptide, pHm, pkaManager);
            if (ncs < 0) {
                return pHs;
            }
            if (((ncs < 0) && (ncm > 0)) || ((ncs > 0) && (ncm < 0))) {
                pHe = pHm;
            } else {
                pHs = pHm;
            }
            counter++;
        }
        if (log.isDebugEnabled()) {
            log.debug("[" + pHs + "," + pHe + "], iteration = " + counter);
        }
        return (pHs + pHe) / 2;
    }

    /**
	 * Get modification {@code peptide1} - {@code peptide2}.
	 * 
	 * @param peptide1 the first peptide operand.
	 * @param peptide2 the second peptide operand.
	 * @param interval the interval in peptideFrom such as interval(peptideFrom)
	 *        = peptideTo
	 * @return the modif difference of null if no difference.
	 */
    public static Modification getModifDifference(Peptide peptide1, IntegerSequence interval1, Peptide peptide2, IntegerSequence interval2, MassCalculator massCalc) {
        List<Modification> fromModifs = MODIF_MANAGER.getListOfModifs(peptide1, interval1);
        List<Modification> toModifs = MODIF_MANAGER.getListOfModifs(peptide2, interval2);
        double diff = 0;
        for (Modification modif : fromModifs) {
            diff += massCalc.getMass(modif);
        }
        for (Modification modif : toModifs) {
            diff -= massCalc.getMass(modif);
        }
        int cmp = DOUBLE_COMP.compare(diff, 0.0);
        if (cmp != 0) {
            return ModificationFactory.valueOf(diff);
        }
        return null;
    }
}
