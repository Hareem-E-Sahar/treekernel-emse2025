package org.expasy.jpl.bio.sequence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.expasy.jpl.bio.exceptions.JPLAAByteUndefinedException;
import org.expasy.jpl.bio.molecule.JPLIModification;
import org.expasy.jpl.bio.molecule.JPLIonizableModification;
import org.expasy.jpl.bio.molecule.aa.JPLAAEncoding;
import org.expasy.jpl.bio.molecule.aa.JPLAAModification;
import org.expasy.jpl.bio.molecule.aa.JPLAminoAcidProperties;
import org.expasy.jpl.bio.molecule.aa.property.JPLHydropathyScoringSystem;
import org.expasy.jpl.bio.sequence.rich.JPLIRichAASequence;
import org.expasy.jpl.bio.sequence.rich.JPLPFFSequence;
import org.expasy.jpl.bio.sequence.rich.JPLPMFSequence;
import org.expasy.jpl.bio.sequence.tools.JPLSequenceAACounter;
import org.expasy.jpl.utils.math.combinatorial.MixedRadixNtuples;

/**
 * A class providing utility sequences methods.
 * 
 * @author nikitin
 *
 */
public class JPLSequences {

    private static Log log = LogFactory.getLog(JPLSequences.class);

    private JPLSequences() {
    }

    /**
	 * Convert String sequence to JPLIAASequence.
	 * 
	 * @param sequenceString the string to convert.
	 * @return the converted JPLIAASequence.
	 * 
	 * @throws JPLBuilderException if sequenceString is badly formatted
	 */
    public static JPLIAASequence toJPLIAASequence(String sequenceString) {
        if (JPLPFFSequence.isSequenceStringPFFType(sequenceString)) {
            return new JPLPFFSequence.Builder(sequenceString).build();
        } else {
            return new JPLAASequence.Builder(sequenceString).build();
        }
    }

    /**
	 * Merge sequences (JPLPFFSequence or JPLAASequence) in one.
	 * 
	 * @param seq1 the first sequence to merge.
	 * @param seq2 the second sequence to merge.
	 *
	 * @return the merge of both sequence of 
	 */
    public static JPLIAASequence merge(JPLIAASequence seq1, JPLIAASequence seq2) {
        StringBuilder seqString = new StringBuilder();
        boolean isPFF2Build = false;
        if (seq1 instanceof JPLPFFSequence) {
            seqString.append(((JPLPFFSequence) seq1).toAAString());
            isPFF2Build = true;
        } else if (seq1 instanceof JPLPMFSequence) {
            log.error("not yet implement for JPLPMFSequence instances");
        } else {
            seqString.append(((JPLAASequence) seq1).toAAString());
        }
        if (seq2 instanceof JPLPFFSequence) {
            seqString.append(((JPLPFFSequence) seq2).toAAString());
            isPFF2Build = true;
        } else if (seq2 instanceof JPLPMFSequence) {
            log.error("not yet implement for JPLPMFSequence instances");
        } else {
            seqString.append(((JPLAASequence) seq2).toAAString());
        }
        if (isPFF2Build) {
            return new JPLPFFSequence.Builder(seqString.toString()).nTerm(seq1.getNTerm()).cTerm(seq2.getCTerm()).build();
        } else {
            return new JPLAASequence.Builder(seqString.toString()).nTerm(seq1.getNTerm()).cTerm(seq2.getCTerm()).build();
        }
    }

    /**
	 * Compute the occurrences of AA in the given sequence.
	 * 
	 * @param sequence the sequence to count AA on.
	 * @return a JPLAACalculator instance.
	 */
    public static JPLSequenceAACounter getAAOccurrences(JPLIAASequence sequence) {
        JPLSequenceAACounter aaCounter = new JPLSequenceAACounter();
        aaCounter.compute(sequence);
        return aaCounter;
    }

    /** 
	 * @return the list of amino-acids found at given positions in the given sequence.
	 * 
	 * @throws JPLAAByteUndefinedException 
	 */
    public static List<Byte> getAAsAtPositions(JPLIAASequence sequence, List<Integer> positions) {
        List<Byte> aas = new ArrayList<Byte>();
        for (int pos : positions) {
            aas.add(sequence.getAAByteAt(pos));
        }
        return aas;
    }

    /**
	 * @return the list of positions of the amino-acid aa byte.
	 * @throws JPLAAByteUndefinedException if byte is unknown.
	 */
    public static List<Integer> getPositionsOfAAByte(JPLIAASequence sequence, byte aa) throws JPLAAByteUndefinedException {
        JPLSequenceAACounter aaCounter = new JPLSequenceAACounter();
        aaCounter.compute(sequence);
        return aaCounter.getPositionsForAA(aa);
    }

    /**
	 * @return the list of positions of all the given amino-acids.
	 * @throws JPLAAByteUndefinedException if a byte is unknown.
	 */
    public static List<Integer> getPositionsOfAABytes(JPLIAASequence sequence, Set<Byte> aas) throws JPLAAByteUndefinedException {
        JPLSequenceAACounter aaCounter = new JPLSequenceAACounter();
        aaCounter.compute(sequence);
        return aaCounter.getPositionsForAAs(aas);
    }

    /**
	 * Add a modification in a given sequence at a given position.
	 * 
	 * @param seq the sequence to modify.
	 * @param modif the modif to add.
	 * @param position the position of aa to modify.
	 * @return the modified sequence.
	 */
    public static JPLIRichAASequence addModif(JPLIAASequence seq, JPLIModification modif, int position) {
        JPLIRichAASequence modifiedSequence = null;
        if (seq instanceof JPLIRichAASequence) {
            if (seq instanceof JPLPFFSequence) {
                ((JPLPFFSequence) seq).addModif(modif, position, false);
                modifiedSequence = (JPLIRichAASequence) seq;
            } else if (seq instanceof JPLPMFSequence) {
                ((JPLPMFSequence) seq).addModif(modif, 1);
            }
        } else {
            modifiedSequence = new JPLPFFSequence.Builder(seq).build();
            ((JPLPFFSequence) modifiedSequence).addModif(modif, position, false);
        }
        return modifiedSequence;
    }

    /**
	 * Add a type of modification in a given sequence at given positions.
	 * 
	 * @param seq the sequence to modify.
	 * @param modif the modif to add.
	 * @param positions the positions of aas to modify.
	 * @return the modified sequence.
	 */
    public static JPLIRichAASequence addModif(JPLIAASequence seq, JPLIModification modif, List<Integer> positions) {
        JPLIRichAASequence modifiedSequence = null;
        if (seq instanceof JPLIRichAASequence) {
            if (seq instanceof JPLPFFSequence) {
                ((JPLPFFSequence) seq).addModif(modif, positions);
                modifiedSequence = (JPLIRichAASequence) seq;
            } else if (seq instanceof JPLPMFSequence) {
                ((JPLPMFSequence) seq).addModif(modif, positions.size());
            }
        } else {
            modifiedSequence = new JPLPFFSequence.Builder(seq).build();
            ((JPLPFFSequence) modifiedSequence).addModif(modif, positions);
        }
        return modifiedSequence;
    }

    /**
	 * Add the same type of modification for all amino-acids of a given sequence.
	 * 
	 * @param seq the sequence to modify.
	 * @param modif the aa modif to add.
	 * @return the modified sequence.
	 */
    public static JPLIRichAASequence addModif(JPLIAASequence seq, JPLAAModification modif) {
        JPLIRichAASequence modifiedSequence = null;
        List<Integer> positions = null;
        try {
            positions = JPLSequences.getPositionsOfAAByte(seq, modif.getTargetAA());
            log.debug(positions);
        } catch (JPLAAByteUndefinedException e1) {
            log.warn(e1.getMessage());
        }
        if (seq instanceof JPLIRichAASequence) {
            if (seq instanceof JPLPFFSequence) {
                ((JPLPFFSequence) seq).addModif(modif, positions);
                modifiedSequence = (JPLIRichAASequence) seq;
            } else if (seq instanceof JPLPMFSequence) {
                ((JPLPMFSequence) seq).addModif(modif, positions.size());
            }
        } else {
            modifiedSequence = new JPLPFFSequence.Builder(seq).build();
            ((JPLPFFSequence) modifiedSequence).addModif(modif, positions);
        }
        log.debug(modifiedSequence);
        return modifiedSequence;
    }

    /**
	 * Add different amino-acid modifications in a given sequence.
	 * 
	 * @param seq the sequence to modify.
	 * @param modifs the aa modifs to add.
	 * @return the modified sequence.
	 * 
	 * Note : only one modif by amino-acid.
	 * @see List<JPLIRichAASequence> addModifs()
	 */
    public static JPLIRichAASequence addModifs(JPLIAASequence seq, List<JPLAAModification> modifs) {
        JPLIAASequence curSeq = seq;
        for (JPLAAModification modif : modifs) {
            JPLIRichAASequence modifiedSequence = addModif(curSeq, modif);
            curSeq = modifiedSequence;
        }
        return (JPLIRichAASequence) curSeq;
    }

    /**
	 * Add different amino-acid modifications in a given sequence.
	 * Note : allows multi-modif by amino-acid.
	 * 
	 * @param seq the sequence to modify.
	 * @param modifs the aa modifs to add (many potential modif by aa).
	 * @return the modified sequences.
	 */
    public static List<JPLPFFSequence> addModifs(JPLIAASequence seq, Map<Byte, ? extends Collection<JPLAAModification>> modifs, int[] positions) {
        List<JPLPFFSequence> modSequences = new ArrayList<JPLPFFSequence>();
        Map<Byte, List<JPLAAModification>> modifLists = new HashMap<Byte, List<JPLAAModification>>();
        int[] numberOfModifs = new int[positions.length];
        for (int i = 0; i < numberOfModifs.length; i++) {
            byte aaByte = seq.getAAByteAt(positions[i]);
            modifLists.put(aaByte, new ArrayList<JPLAAModification>(modifs.get(aaByte)));
            numberOfModifs[i] = modifs.get(aaByte).size();
        }
        MixedRadixNtuples nTuples = MixedRadixNtuples.generate(numberOfModifs);
        log.debug("NTuples number : " + nTuples.getNb());
        for (int[] nTuple : nTuples.getNtuples()) {
            JPLPFFSequence modifiedSequence = null;
            modifiedSequence = new JPLPFFSequence.Builder(seq).build();
            for (int i = 0; i < positions.length; i++) {
                byte aaByte = seq.getAAByteAt(positions[i]);
                JPLSequences.addModif(modifiedSequence, modifLists.get(aaByte).get(nTuple[i]), positions[i]);
            }
            log.debug(modifiedSequence);
            modSequences.add(modifiedSequence);
        }
        return modSequences;
    }

    /**
	 * Compute and return the gravy score of the sequence given
	 * the specific scoring system.
	 * 
	 * @param sequence the JPLIAASequence to compute the gravy on.
	 * @param scoringSystem the scoring system.
	 * 
	 * @return gravy score of the given sequence.
	 * 
	 * @throws JPLAAByteUndefinedException if a aa byte is not defined.
	 */
    public static float getGravyScore(JPLIAASequence sequence, JPLHydropathyScoringSystem scoringSystem) throws JPLAAByteUndefinedException {
        float score = 0;
        JPLSequenceAACounter occs = JPLSequences.getAAOccurrences(sequence);
        Iterator<Byte> aaIterator = occs.getAAs().iterator();
        while (aaIterator.hasNext()) {
            byte aa = aaIterator.next();
            score += JPLAminoAcidProperties.getHydropathyScore(aa, scoringSystem) * occs.getOccOfAA(aa);
        }
        return score / sequence.length();
    }

    /**
	 * Compute and return the gravy score of the sequence in the default
	 * aa Kyle and Doolittle scoring system (-4.6 < score < 4.6).
	 * 
	 * @param sequence the JPLIAASequence to compute the gravy on.
	 * 
	 * @return gravy score of the given sequence.
	 * 
	 * @throws JPLAAByteUndefinedException if a aa byte is not defined.
	 */
    public static float getGravyScore(JPLIAASequence sequence) throws JPLAAByteUndefinedException {
        return getGravyScore(sequence, JPLHydropathyScoringSystem.KYLE_DOOLITTLE);
    }

    /**
	 * Compute the gravy scores of the sequence with a given window size and 
	 * a specific scoring system.
	 * 
	 * @param sequence the JPLIAASequence to compute the gravy on.
	 * @param windowSize the window size.
	 * @param scoringSystem the scoring system.
	 * 
	 * @return gravy scores of the given sequence.
	 * 
	 * @throws JPLAAByteUndefinedException if a aa byte is not defined.
	 */
    public static float[] getGravyScores(JPLIAASequence sequence, int windowSize, JPLHydropathyScoringSystem scoringSystem) throws JPLAAByteUndefinedException {
        int seqLength = sequence.length();
        if (windowSize <= 0) {
            throw new IllegalArgumentException(windowSize + " : bad window size.");
        } else if (windowSize > seqLength) {
            windowSize = seqLength;
        }
        float[] scores = new float[(int) Math.ceil((float) seqLength / windowSize)];
        for (int i = 0; i < seqLength; i++) {
            byte aa = sequence.getAAByteAt(i);
            int scoreIndex = i / windowSize;
            if (i > 0 && i % windowSize == 0) {
                log.info("next index= " + scoreIndex + ", previous index value = " + scores[scoreIndex - 1]);
                scores[scoreIndex - 1] = scores[scoreIndex - 1] / windowSize;
                log.info("updated previous index value = " + scores[scoreIndex - 1]);
            }
            log.info("index= " + scoreIndex + ", " + scores[scoreIndex] + " + (" + JPLAminoAcidProperties.getHydropathyScore(aa, scoringSystem) + ")");
            scores[scoreIndex] += JPLAminoAcidProperties.getHydropathyScore(aa, scoringSystem);
            log.info(" = " + scores[scoreIndex]);
        }
        if (seqLength % windowSize != 0) {
            scores[seqLength / windowSize] /= seqLength % windowSize;
            log.info(" = " + scores[seqLength / windowSize]);
        }
        return scores;
    }

    /**
	 * Compute the gravy scores of the sequence with a given window size
	 * in the default aa Kyle and Doolittle scoring system (-4.6 < score < 4.6).
	 * 
	 * @param sequence the JPLIAASequence to compute the gravy on.
	 * @param windowSize the window size.
	 * 
	 * @return gravy scores of the given sequence.
	 *  
	 * @throws JPLAAByteUndefinedException if a aa byte is not defined.
	 */
    public static float[] getGravyScores(JPLIAASequence sequence, int windowSize) throws JPLAAByteUndefinedException {
        return getGravyScores(sequence, windowSize, JPLHydropathyScoringSystem.KYLE_DOOLITTLE);
    }

    public static double getPartialCharges(JPLIAASequence sequence, double pH) {
        if (sequence instanceof JPLPFFSequence) {
            return JPLSequences.getPartialCharges((JPLPFFSequence) sequence, pH);
        } else {
            return JPLSequences.getPartialCharges((JPLAASequence) sequence, pH);
        }
    }

    /**
	 * Compute the protonated (acid) fraction of sequences (acid + base) at given pH.
	 * 
	 * @param aa the amino-acid byte code
	 * @param pH the pH at which the fraction is evaluated
	 * @return the protonated fraction value between 0 and 1
	 *  at pH 0,  all aa on acid form (protonated) -> 1
	 *  at pH 14, all aa on basic form (deprotonated) -> 0
	 * 
	 * @throws JPLAAByteUndefinedException if undefined aa byte
	 */
    public static double getPartialCharges(JPLAASequence sequence, double pH) {
        double q = JPLAminoAcidProperties.getNtPartialCharge(pH);
        if (log.isDebugEnabled()) {
            log.debug("nt: " + q);
        }
        for (int i = 0; i < sequence.length(); i++) {
            byte aa = sequence.getAAByteAt(i);
            try {
                if (log.isDebugEnabled()) {
                    log.debug("aa " + JPLAAEncoding.toChar(aa) + ": " + JPLAminoAcidProperties.getPartialChargeForAA(aa, pH));
                }
                q += JPLAminoAcidProperties.getPartialChargeForAA(aa, pH);
            } catch (JPLAAByteUndefinedException e) {
                e.printStackTrace();
            }
        }
        q += JPLAminoAcidProperties.getCtPartialCharge(pH);
        if (log.isDebugEnabled()) {
            log.debug("ct: " + JPLAminoAcidProperties.getCtPartialCharge(pH));
        }
        return q;
    }

    public static double getPartialCharges(JPLPFFSequence sequence, double pH) {
        double q = 0.0;
        if (sequence.hasNTermModif()) {
            if (sequence.getNtModif() instanceof JPLIonizableModification) {
                JPLIonizableModification modif = (JPLIonizableModification) sequence.getNtModif();
                if (log.isDebugEnabled()) {
                    log.debug("nt has a ionizable modif: " + modif);
                }
                q = modif.getPartialCharge(pH);
                if (log.isDebugEnabled()) {
                    log.debug("nt pc at pH " + pH + " = " + q);
                }
            }
        } else {
            q = JPLAminoAcidProperties.getNtPartialCharge(pH);
            if (log.isDebugEnabled()) {
                log.debug("nt: " + JPLAminoAcidProperties.getNtPartialCharge(pH));
            }
        }
        for (int i = 0; i < sequence.length(); i++) {
            byte aa = sequence.getAAByteAt(i);
            double aapc = 0;
            try {
                if (sequence.hasModifAt(i)) {
                    if (sequence.getModifAt(i) instanceof JPLIonizableModification) {
                        JPLIonizableModification modif = (JPLIonizableModification) sequence.getModifAt(i);
                        aapc = modif.getPartialCharge(pH);
                        if (log.isDebugEnabled()) {
                            log.debug(JPLAAEncoding.toChar(aa) + " at index " + i + " has a ionizable modif: " + modif + " = " + aapc);
                        }
                    }
                } else {
                    aapc = JPLAminoAcidProperties.getPartialChargeForAA(aa, pH);
                    if (log.isDebugEnabled()) {
                        log.debug("aa " + JPLAAEncoding.toChar(aa) + " at pH " + pH + " = " + aapc);
                    }
                }
            } catch (JPLAAByteUndefinedException e) {
                e.printStackTrace();
            }
            q += aapc;
        }
        if (sequence.hasCTermModif()) {
            if (sequence.getCtModif() instanceof JPLIonizableModification) {
                JPLIonizableModification modif = (JPLIonizableModification) sequence.getCtModif();
                if (log.isDebugEnabled()) {
                    log.debug("ct has a ionizable modif: " + modif);
                }
                q += modif.getPartialCharge(pH);
                if (log.isDebugEnabled()) {
                    log.debug("ct pc at pH " + pH + " = " + modif.getPartialCharge(pH));
                }
            }
        } else {
            q += JPLAminoAcidProperties.getCtPartialCharge(pH);
            if (log.isDebugEnabled()) {
                log.debug("ct: " + JPLAminoAcidProperties.getCtPartialCharge(pH));
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("### seq pc at pH " + pH + " = " + q);
        }
        return q;
    }

    /**
	 * Get the isoelectric point that is when global charge of 
	 * a protein/peptide is neutral.
	 * 
	 * @param sequence the protein/peptide sequence.
	 * @return the isoelectric point.
	 */
    public static double getIsoelectricPoint(JPLIAASequence sequence) {
        double epsilon = 0.001;
        int iterationMax = 10000;
        int counter = 0;
        double pHs = -2;
        double pHe = 16;
        double pHm = 0;
        while (counter < iterationMax && Math.abs(pHs - pHe) >= epsilon) {
            pHm = (pHs + pHe) / 2;
            if (log.isDebugEnabled()) {
                log.debug("[" + pHs + ", " + pHm + "]");
            }
            double pcs = getPartialCharges(sequence, pHs);
            double pcm = getPartialCharges(sequence, pHm);
            if (pcs < 0) {
                System.err.println("at pH " + pHs + ", partial charge is " + pcs);
                return pHs;
            }
            if ((pcs < 0 && pcm > 0) || (pcs > 0 && pcm < 0)) {
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
}
