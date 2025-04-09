package org.expasy.jpl.bio.sequence.rich;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.expasy.jpl.bio.exceptions.JPLAAByteUndefinedException;
import org.expasy.jpl.bio.exceptions.JPLAACharUndefinedRTException;
import org.expasy.jpl.bio.exceptions.JPLAASequenceBuilderException;
import org.expasy.jpl.bio.exceptions.JPLMolecularExpressionParseException;
import org.expasy.jpl.bio.exceptions.JPLInvalidAAModificationRuntimeException;
import org.expasy.jpl.bio.exceptions.JPLPFFSequenceBuilderException;
import org.expasy.jpl.bio.exceptions.JPLSequenceIndexOutOfBoundException;
import org.expasy.jpl.bio.molecule.JPLIModification;
import org.expasy.jpl.bio.molecule.JPLMass;
import org.expasy.jpl.bio.molecule.JPLModification;
import org.expasy.jpl.bio.molecule.JPLMolecularExpression;
import org.expasy.jpl.bio.molecule.aa.JPLAAEncoding;
import org.expasy.jpl.bio.molecule.aa.JPLAAResiduesMassSingleton;
import org.expasy.jpl.bio.molecule.aa.JPLNeutralLoss;
import org.expasy.jpl.bio.sequence.JPLAASequence;
import org.expasy.jpl.bio.sequence.JPLCTerminus;
import org.expasy.jpl.bio.sequence.JPLNTerminus;
import org.expasy.jpl.commons.ms.JPLMassAccuracy;
import org.expasy.jpl.commons.ms.peak.JPLIMSnPeakType;
import org.expasy.jpl.bio.sequence.JPLIAASequence;
import org.expasy.jpl.bio.sequence.tools.positions.JPLPositionIterable;
import org.expasy.jpl.utils.parser.JPLParseException;

/**
 *  A <code>JPLPFFSequence</code> is a {@code JPLAASequence} where
 *  modif-type annotations are enabled.
 *  <p>
 *  The amino-acid sequence is coming from MS-MS on which PTMs
 *  (post-translational modifications) can be annotated on aa loci.
 *  <p>
 *  [The profile of the masses of the ions obtained by MS-MS is compared
 *  by means of a computer with all predicted ion fragments.
 *  This is termed peptide fragment fingerprinting (PFF)].
 *  <p>
 *  By now, each PFF sequence has an array of modifications.
 *  A modification is found at index i if the amino-acid i is modified.
 *  
 *  @see <code>JPLPMFSequence</code> for peptide-level PTMs.
 */
public class JPLPFFSequence extends JPLAASequence implements JPLIRichAASequence, JPLPositionIterable {

    static Log logger = LogFactory.getLog(JPLPFFSequence.class);

    private static char NTermModifDelimitor = '.';

    private static char CTermModifDelimitor = '.';

    /** 
	 * An array of modifications with the same size than aa sequence
	 * allows site-dependant annotations.
	 * This structure limits to 1 modification max by aa locus.
	 */
    private JPLIModification[] modifs;

    /** Modifications can affect a terminus */
    private JPLIModification nTermModif;

    private JPLIModification cTermModif;

    /** the root cumulative masses of modifs by position */
    private double[] cumulModifMasses;

    /** is sequence has been checked for invalid neutral losses ? */
    private boolean isNeutralLossModifications;

    public static class Builder extends JPLAASequence.Builder {

        private Map<Integer, Double> masses = new HashMap<Integer, Double>();

        private Map<Integer, JPLMass> modifs = new HashMap<Integer, JPLMass>();

        private boolean isNeutralLossToCheck = false;

        public Builder(String sequence) {
            super(sequence);
        }

        public Builder(JPLIAASequence sequence) {
            super(sequence);
        }

        public Builder massType(JPLMassAccuracy massType) {
            return (Builder) super.massType(massType);
        }

        public Builder from(int from) {
            return (Builder) super.from(from);
        }

        public Builder to(int to) {
            return (Builder) super.to(to);
        }

        public Builder len(int len) {
            return (Builder) super.len(len);
        }

        public Builder nTerm(JPLNTerminus nTerm) {
            return (Builder) super.nTerm(nTerm);
        }

        public Builder cTerm(JPLCTerminus cTerm) {
            return (Builder) super.cTerm(cTerm);
        }

        public Builder seqType(JPLIMSnPeakType type) {
            return (Builder) super.seqType(type);
        }

        public Builder addModif(double mass, int pos) {
            masses.put(pos, mass);
            return this;
        }

        public Builder addModifs(double mass, Collection<Integer> positions) {
            for (int pos : positions) {
                masses.put(pos, mass);
            }
            return this;
        }

        public Builder checkNeutralLosses() {
            isNeutralLossToCheck = true;
            return this;
        }

        protected JPLPFFSequence makeInstance() throws JPLParseException {
            if (sequenceString != null) {
                sequenceString = JPLPFFSequence.parsePFFSequenceString(sequenceString, modifs, massType);
            }
            return new JPLPFFSequence(this);
        }

        private void checkNeutralLossAtAA(int pos, JPLMass neutralLossMass) throws JPLPFFSequenceBuilderException {
            if (pos == -1 || pos == this.sequenceString.length()) {
            } else {
                char aa = this.sequenceString.charAt(pos);
                JPLAAResiduesMassSingleton singleton = JPLAAResiduesMassSingleton.getInstance();
                double aaLateralChainMass;
                try {
                    aaLateralChainMass = singleton.getResidueMass(JPLAAEncoding.toByte(aa), this.massType) - singleton.getResidueSkeletonMass(this.massType);
                    if (aaLateralChainMass + neutralLossMass.getValue() < 0) {
                        throw new JPLPFFSequenceBuilderException("cannot build " + "PFF sequence, lateral chain of " + aa + " at position " + pos + " with a mass of " + aaLateralChainMass + " cannot lose a neutral loss mass of " + (-neutralLossMass.getValue()));
                    }
                } catch (JPLAACharUndefinedRTException e) {
                    e.printStackTrace();
                } catch (JPLAAByteUndefinedException e) {
                    e.printStackTrace();
                }
            }
        }

        public JPLPFFSequence build() throws JPLPFFSequenceBuilderException {
            JPLPFFSequence seq = null;
            try {
                seq = (JPLPFFSequence) super.build();
            } catch (JPLAASequenceBuilderException e) {
                throw new JPLPFFSequenceBuilderException("cannot build PFF sequence", e);
            }
            if (parentSequence != null && parentSequence instanceof JPLPFFSequence) {
                if (from == 0) {
                    JPLIModification modif = ((JPLPFFSequence) parentSequence).getNtModif();
                    if (modif != null) {
                        masses.put(-1, modif.getMassValue());
                    }
                }
                if (to == parentSequence.length() - 1) {
                    JPLIModification modif = ((JPLPFFSequence) parentSequence).getCtModif();
                    if (modif != null) {
                        masses.put(to + 1, modif.getMassValue());
                    }
                }
            }
            if (masses.size() > 0) {
                for (int pos : masses.keySet()) {
                    JPLMass mass = null;
                    if (massType == JPLMassAccuracy.MONOISOTOPIC) {
                        mass = JPLMass.MonoisotopicMass(masses.get(pos));
                    } else if (massType == JPLMassAccuracy.MONOISOTOPIC) {
                        mass = JPLMass.AverageMass(masses.get(pos));
                    } else {
                        throw new JPLPFFSequenceBuilderException("cannot build " + "PFF sequence : mass type is not defined");
                    }
                    if (!modifs.containsKey(pos)) {
                        modifs.put(pos, mass);
                    } else {
                        throw new JPLPFFSequenceBuilderException("cannot build " + "PFF sequence aa at position " + pos + " has been " + "already modified with " + modifs.get(pos) + " : cannot add mass " + mass);
                    }
                }
            }
            if (modifs.size() > 0) {
                for (int pos : modifs.keySet()) {
                    JPLMass mass = modifs.get(pos);
                    JPLIModification modif = null;
                    if (mass.getValue() < 0) {
                        seq.isNeutralLossModifications = true;
                        if (isNeutralLossToCheck) {
                            checkNeutralLossAtAA(pos, mass);
                        }
                        modif = JPLNeutralLoss.noLabel(mass);
                    } else {
                        modif = JPLModification.noLabel(mass);
                    }
                    if (pos == -1) {
                        seq.addNtModif(modif);
                    } else if (pos == seq.length()) {
                        seq.addCtModif(modif);
                    } else {
                        seq.addModif(modif, pos);
                    }
                }
            }
            return seq;
        }
    }

    private JPLPFFSequence(Builder builder) throws JPLParseException {
        super(builder);
        if (builder.getSequence() instanceof JPLPFFSequence) {
            copyModifsInInterval((JPLPFFSequence) builder.getSequence(), builder.getFrom(), builder.getTo());
        }
    }

    public JPLPFFSequence clone() {
        JPLPFFSequence clone = new JPLPFFSequence.Builder(this.toAAString()).seqType(this.type).build();
        return clone;
    }

    /**
	 * Parse the sequence string, extract and store potential modifs.
	 * 
	 * @param richStr the string to parse.
	 * @param masses the masses to store in.
	 * @param massTypethe mass type.
	 * 
	 * @return the "naked" (no modifs) sequence string 
	 * @throws JPLParseException 
	 * 
	 */
    private static String parsePFFSequenceString(String richStr, Map<Integer, JPLMass> masses, JPLMassAccuracy massType) throws JPLParseException {
        if (isSequenceWithMolecularFormulaTypeModif(richStr)) {
            richStr = modifyMolecFormula2DoubleMassModifInSequence(richStr, massType);
        }
        Pattern modPattern = Pattern.compile("\\(\\{-?[\\d.]+\\}\\)");
        Matcher matcher = modPattern.matcher(richStr);
        StringBuilder nakedSeqStr = new StringBuilder();
        int cumulatedPatternLength = 0;
        int richSeqFrom = 0;
        while (matcher.find()) {
            cumulatedPatternLength += matcher.end() - matcher.start();
            int pos = matcher.end() - 1 - cumulatedPatternLength;
            String massStr = richStr.substring(matcher.start() + 2, matcher.end() - 2);
            JPLMass mass = null;
            if (massType == JPLMassAccuracy.MONOISOTOPIC) {
                mass = JPLMass.MonoisotopicMass(Double.parseDouble(massStr));
            } else if (massType == JPLMassAccuracy.AVERAGE) {
                mass = JPLMass.AverageMass(Double.parseDouble(massStr));
            } else {
                throw new IllegalStateException("mass type is not defined");
            }
            if (pos == -1) {
                if (richStr.charAt(matcher.end()) == NTermModifDelimitor) {
                    cumulatedPatternLength++;
                    richSeqFrom = matcher.end() + 1;
                } else {
                    throw new JPLParseException("bad character : '" + richStr.charAt(matcher.end()) + "'", matcher.end());
                }
            } else if (richStr.charAt(matcher.start() - 1) == CTermModifDelimitor) {
                nakedSeqStr.append(richStr.substring(richSeqFrom, matcher.start() - 1));
                richSeqFrom = matcher.end();
            } else {
                nakedSeqStr.append(richStr.substring(richSeqFrom, matcher.start()));
                richSeqFrom = matcher.end();
            }
            masses.put(pos, mass);
        }
        if (richSeqFrom < richStr.length()) {
            nakedSeqStr.append(richStr.substring(richSeqFrom));
        }
        return nakedSeqStr.toString();
    }

    /**
	 * Parse sequence string and replace molecular formula modification format
	 * by mass format modification (i.e. (O) -> ({15.99})).
	 * 
	 * @param sequenceString the sequence to parse
	 * @param massType the mass type
	 * @return the mass modified sequence string
	 * 
	 * @throws JPLMolecularExpressionParseException if molecular formula
	 *  is not valid.
	 */
    private static String modifyMolecFormula2DoubleMassModifInSequence(String sequenceString, JPLMassAccuracy massType) throws JPLMolecularExpressionParseException {
        String modifRegex = "\\([^{)]+\\)";
        StringBuilder sb = new StringBuilder();
        Matcher matcher = Pattern.compile(modifRegex).matcher(sequenceString);
        int from = 0;
        while (matcher.find()) {
            String group = matcher.group(0);
            sb.append(sequenceString.substring(from, matcher.start()));
            String formula = group.substring(1, group.length() - 1);
            JPLMolecularExpression molecularExpression = new JPLMolecularExpression(formula, massType);
            sb.append("({");
            sb.append(molecularExpression.getMw());
            sb.append("})");
            from = matcher.end();
        }
        if (from < sequenceString.length()) {
            sb.append(sequenceString.substring(from));
        }
        if (sb.length() != 0) {
            return sb.toString();
        } else {
            return sequenceString;
        }
    }

    public static boolean isSequenceStringPFFType(String sequence) {
        return (isSequenceStringMassValueModifType(sequence) || isSequenceWithMolecularFormulaTypeModif(sequence));
    }

    private static boolean isSequenceStringMassValueModifType(String sequence) {
        String molecularModifRegex = "^([A-Z]+\\(\\{-?[\\d.]+\\}\\))+[A-Z]*$";
        return sequence.matches(molecularModifRegex);
    }

    public static boolean isSequenceWithMolecularFormulaTypeModif(String sequence) {
        String molecularModifRegex = "^.*?\\([^{)]+\\).*?$";
        return sequence.matches(molecularModifRegex);
    }

    /**
	 * Same length JPLAASequence have the same hash code.
	 */
    public int hashCode() {
        return length;
    }

    /**
	 * Return true if 2 pff sequences are identical
	 */
    public boolean equals(Object obj) {
        if ((obj instanceof JPLPFFSequence) && ((JPLPFFSequence) obj).toAAString().equals(this.toAAString())) {
            return true;
        }
        return false;
    }

    public void setMassType(JPLMassAccuracy massType) {
        this.massType = massType;
    }

    /**
	 * Once in sequences hierarchy, this process of computing
	 * cumulative masses is done.
	 */
    protected double[] processCumulMassesCalculations() throws JPLAAByteUndefinedException {
        double[] masses = super.processCumulMassesCalculations();
        if (hasModif()) {
            processCumulModifMassesCalculations();
        }
        return masses;
    }

    /**
	 * This process is called in 3 cases :
	 * <ol>
	 * <li>processCumulMassesCalculations has been called</li>
	 * <li>A modification has been added</li>
	 * <li>A modification has been removed</li>
	 * </ol>
	 */
    private double[] processCumulModifMassesCalculations() {
        double[] masses = new double[length];
        double currentMass = 0.0;
        for (int i = 0; i < length; i++) {
            JPLIModification modif = modifs[i];
            if (modif != null) {
                currentMass += modif.getMassValue();
            }
            masses[i] = currentMass;
            if (logger.isDebugEnabled()) {
                logger.debug(i + " = " + masses[i]);
            }
        }
        cumulModifMasses = masses;
        if (logger.isDebugEnabled()) {
            logger.debug("MODIFS = " + Arrays.toString(masses));
        }
        return masses;
    }

    public double[] getCumulModifMasses() {
        double[] masses = cumulModifMasses;
        if (masses == null) {
            return processCumulModifMassesCalculations();
        }
        return masses;
    }

    public double getSumOfModifMasses() throws JPLAAByteUndefinedException {
        double[] cumulModifMasses = getCumulModifMasses();
        return cumulModifMasses[length() - 1];
    }

    public double getNTermModifMass() {
        if (nTermModif != null) {
            return nTermModif.getMassValue();
        }
        return 0.0;
    }

    public double getCTermModifMass() {
        if (cTermModif != null) {
            return cTermModif.getMassValue();
        }
        return 0.0;
    }

    public double getNTermDeltaMass() {
        return super.getNTermDeltaMass() + getNTermModifMass();
    }

    public double getCTermDeltaMass() {
        return super.getCTermDeltaMass() + getCTermModifMass();
    }

    public double getSumOfResiduesMass() {
        double mass = super.getSumOfResiduesMass();
        if (modifs != null) {
            double[] cumulModifMasses = getCumulModifMasses();
            mass += cumulModifMasses[length() - 1];
        }
        return mass;
    }

    public double getSumOfResidueMasses(int from, int to) {
        double mass = super.getSumOfResidueMasses(from, to);
        if (modifs != null) {
            double[] cumulModifMasses = getCumulModifMasses();
            if (from == 0) {
                mass += cumulModifMasses[to];
            } else {
                mass += cumulModifMasses[to] - cumulModifMasses[from - 1];
            }
        }
        return mass;
    }

    public double getNtCumulResidueMassAt(int position) {
        double mass = super.getNtCumulResidueMassAt(position);
        if (modifs != null) {
            double[] cumulModifMasses = getCumulModifMasses();
            mass += cumulModifMasses[position] + getNTermModifMass() + getCTermModifMass();
        }
        return mass;
    }

    /**
     * Returns the cumulative mass of amino-acids met from a specific
     * position to C-terminal (aa[i]-------Ct) in a given type of mass.
     * 
     * @param position the position index where to look for cumulative mass.
     * @param massType the type of mass.
     * 
     * @return the cumulative mass at position index.
	 * @throws JPLAAByteUndefinedException 
     * 
     * @throws JPLSequenceIndexOutOfBoundException if position is out of bounds.
     */
    public double getCtCumulResidueMassAt(int position) {
        return getNeutralMass() - getNtCumulResidueMassAt(position);
    }

    /**
     * Returns the mass of amino-acid at specific position (1..n-2)
     * or termini (0 or n-1) in a given type of mass.
     * 
     * @param position the position index where to look for mass.
     * @param massType the type of mass.
     *  
     * @return the mass at specific position.
     *  
     * @throws JPLSequenceIndexOutOfBoundException if position is out of bounds.
     */
    public double getAAMassAt(int position) {
        if (position == 0) {
            return getNtCumulResidueMassAt(position);
        } else if (position > 0) {
            return getNtCumulResidueMassAt(position) - getNtCumulResidueMassAt(position - 1);
        }
        return 0.0;
    }

    public Iterator iterator() {
        return new Iterator();
    }

    /** inner class Iterator */
    class Iterator extends JPLPositionIterable.Iterator {

        private int currentPos = -1;

        private int nextPosition = -1;

        public boolean hasNext() {
            if (modifs == null) {
                return false;
            }
            return ((nextPosition == -1) ? findNextPosition() : true);
        }

        /**
		 * Returns the next <code>Integer</code> element of the iterator.
		 * 
		 * @return next position.
		 */
        public Integer next() {
            if (nextPosition != -1) {
                return consumePosition();
            }
            if (!findNextPosition()) {
                throw new NoSuchElementException("No more elements");
            }
            return consumePosition();
        }

        /**
		 * @return <code>true</code> if the next position is found.
		 */
        private boolean findNextPosition() {
            while (++currentPos < modifs.length) {
                if (modifs[currentPos] != null) {
                    nextPosition = currentPos;
                    return true;
                }
            }
            return false;
        }

        private Integer consumePosition() {
            Integer tmp = nextPosition;
            nextPosition = -1;
            return tmp;
        }
    }

    public void addModif(JPLIModification modif, int position) {
        addModif(modif, position, true);
    }

    /**
	 * Add a modification at a given locus.
	 * 
	 * @param modif to add.
	 * @param position to modify.
	 * @param update true if update has to be done.
	 * @throws JPLInvalidAAModificationException 
	 * 
	 * @throws NullPointerException if {@code modif} to add is null.
	 * @throws IllegalStateException if aa has already been modified at locus.
	 * @throws ArrayIndexOutOfBoundsException if index is out of range.
	 */
    public void addModif(JPLIModification modif, int position, boolean update) {
        if (modif == null) {
            throw new NullPointerException("modif to add cannot be null");
        }
        if (modifs == null) {
            modifs = new JPLIModification[this.length()];
        }
        if (modif instanceof JPLNeutralLoss) {
            checkNeutralLossAtPos(modif, position);
        } else {
            checkValidModifPosition(position);
        }
        if (modifs[position] != null) {
            throw new IllegalStateException("position='" + Integer.toString(position) + "' is already associated to " + "a modif");
        }
        modifs[position] = modif;
        if (update) {
            cumulModifMasses = null;
        }
    }

    /**
	 * Add the same modification at different given loci.
	 *  
	 * @param modif to add.
	 * @param positions list loci to modify.
	 * 
	 * @throws NullPointerException if the {@code modif} to add is null.
	 * @throws IllegalStateException if aa has already been modified at locus.
	 * @throws ArrayIndexOutOfBoundsException if index is out of range.
	 */
    public void addModif(JPLIModification modif, List<Integer> positions) {
        for (Integer i : positions) {
            addModif(modif, i, false);
        }
        if (cumulModifMasses != null) {
            cumulModifMasses = null;
        }
    }

    /**
	 * Copy target subsequence aa modifications.  
	 * 
	 * @param target the target sequence to copy modif from.
	 * @param start the beginning index of subsequence.
	 * @param end the end index of subsequence.
	 */
    private void copyModifsInInterval(JPLPFFSequence target, int start, int end) {
        JPLPFFSequence.Iterator it = target.iterator();
        while (it.hasNext()) {
            Integer pos = it.next();
            if (pos >= start && pos <= end) {
                if (modifs == null) {
                    modifs = new JPLIModification[end - start + 1];
                }
                modifs[pos - start] = target.getModifAt(pos).clone();
            }
        }
    }

    public void addCtModif(JPLIModification modif) {
        if (modif == null) {
            throw new NullPointerException("modif to add cannot be null");
        }
        if (cTermModif != null) {
            throw new IllegalStateException("cTermModif is already associated to " + "a modif : " + cTermModif);
        }
        cTermModif = modif;
    }

    public void addNtModif(JPLIModification modif) {
        if (modif == null) {
            throw new NullPointerException("modif to add cannot be null");
        }
        if (nTermModif != null) {
            throw new IllegalStateException("nTermModif is already associated to " + "a modif : " + nTermModif);
        }
        nTermModif = modif;
    }

    /**
	 * Has a modification occurred at the given site position ?
	 * 
	 * @param position the position to search for modif existence.
	 * @return true if a modif exists at given position.
	 */
    public boolean hasModifAt(int position) {
        if (modifs == null) {
            return false;
        }
        if (modifs[position] == null) {
            return false;
        }
        return true;
    }

    /**
	 * 
	 * @param position gives the locus in the sequence.
	 * @return the modification at the defined locus
	 * or null if there is no modification.
	 * 
	 * @throws JPLSequenceIndexOutOfBoundException if index out of sequence bounds.
	 */
    public JPLIModification getModifAt(int position) {
        checkValidModifPosition(position);
        return modifs[position];
    }

    public JPLIModification getNtModif() {
        return nTermModif;
    }

    public JPLIModification getCtModif() {
        return cTermModif;
    }

    /**
	 * Check the validity of position.
	 * 
	 * @param position the position to check
	 */
    private void checkValidModifPosition(int position) {
        if (position < 0 || position > endIndexInRoot - startIndexInRoot) throw new JPLSequenceIndexOutOfBoundException(position + " is out of range : [" + startIndexInRoot + ":" + endIndexInRoot + "]");
    }

    public boolean hasModif() {
        if (modifs == null || modifs.length == 0) {
            return false;
        }
        return true;
    }

    public boolean hasNTermModif() {
        return (nTermModif != null);
    }

    public boolean hasCTermModif() {
        return (cTermModif != null);
    }

    public boolean hasTermModif() {
        if (nTermModif == null && cTermModif == null) {
            return false;
        }
        return true;
    }

    public int getNumberOfModif(JPLIModification modif) {
        int motifNumber = 0;
        if (hasModif()) {
            for (JPLIModification currentModif : modifs) {
                if (currentModif != null) {
                    if (currentModif.equals(modif)) {
                        motifNumber++;
                    }
                }
            }
        }
        if (hasTermModif()) {
            if (nTermModif != null) {
                motifNumber++;
            }
            if (cTermModif != null) {
                motifNumber++;
            }
        }
        return motifNumber;
    }

    public int getNumberOfModifs() {
        int motifNumber = 0;
        if (hasModif()) {
            for (JPLIModification modif : modifs) {
                if (modif != null) {
                    motifNumber++;
                }
            }
        }
        return motifNumber;
    }

    /**
	 * Remove modification at given locus.
	 * 
	 * @param position give locus to unmodify.
	 * @throws IllegalArgumentException when no modif exists at given locus.
	 * @throws IllegalStateException when modifs is not defined.
	 * @throws JPLSequenceIndexOutOfBoundException if index out of sequence bounds.
	 */
    public void removeModifAt(int position) {
        checkValidModifPosition(position);
        if (modifs != null) {
            modifs[position] = null;
            cumulModifMasses = null;
        }
    }

    public void removeNtModif() {
        nTermModif = null;
    }

    public void removeCtModif() {
        cTermModif = null;
    }

    /**
	 * Remove all modifications.
	 */
    public void removeAllModifs() {
        modifs = null;
        nTermModif = null;
        cTermModif = null;
        cumulModifMasses = null;
    }

    public String toModifString() {
        StringBuilder sb = new StringBuilder();
        JPLPFFSequence.Iterator it = iterator();
        sb.append("#modifs = ");
        sb.append(getNumberOfModifs());
        sb.append(" : ");
        while (it.hasNext()) {
            Integer pos = it.next();
            try {
                sb.append(JPLAAEncoding.toChar(getAAByteAt(pos)));
            } catch (JPLAAByteUndefinedException e) {
                e.printStackTrace();
            }
            sb.append("[");
            sb.append(pos);
            sb.append("](");
            sb.append(getModifAt(pos));
            sb.append(") ");
        }
        return sb.toString();
    }

    public String toAAString() {
        StringBuffer sb = new StringBuffer();
        int currentPos = -1;
        JPLIModification currentModif = null;
        String modifBeg = "({";
        String modifEnd = "})";
        JPLPFFSequence.Iterator it = iterator();
        NumberFormat formatter = new DecimalFormat("0.0000");
        double termModif = getNTermModifMass();
        if (termModif > 0) {
            sb.append(modifBeg + formatter.format(termModif) + modifEnd + ".");
        }
        if (it.hasNext()) {
            currentPos = it.next();
            currentModif = getModifAt(currentPos);
        }
        for (int i = startIndexInRoot; i <= endIndexInRoot; i++) {
            try {
                sb.append(JPLAAEncoding.toChar(root.getAAByteAt(i)));
                if (i - startIndexInRoot == currentPos) {
                    sb.append(modifBeg);
                    sb.append(formatter.format(currentModif.getMass().getValue()));
                    sb.append(modifEnd);
                    if (it.hasNext()) {
                        currentPos = it.next();
                        currentModif = getModifAt(currentPos);
                    } else {
                        currentPos = -1;
                    }
                }
            } catch (JPLAAByteUndefinedException e) {
                sb.append('?');
            }
        }
        termModif = getCTermModifMass();
        if (termModif > 0) {
            sb.append("." + modifBeg + formatter.format(termModif) + modifEnd);
        }
        return sb.toString();
    }

    /**
	 * TODO : FindMod Tools on expasy suggestion here
	 * insert its standard abbreviation, surrounded by
	 * parentheses, after the residue where it occurs.
	 * ex :
	 * M(MSONE) for double oxydation of Met in Methionine sulfone.
	 */
    public String toStringOld() {
        return super.toString() + ", " + toModifString();
    }

    /**
	 * Is sequence contains neutral loss modifications ?
	 * 
	 * @return true if contains neutral loss
	 */
    public boolean isNeutralLossModifications() {
        return isNeutralLossModifications;
    }

    /**
	 * Check the neutral loss modifications at given position.
	 * 
	 * @param modif the modif to test
	 * @param pos the position to check
	 * 
	 * @throws JPLInvalidAAModificationException if a neutral loss is invalid.
	 */
    public void checkNeutralLossAtPos(JPLIModification modif, int pos) {
        JPLAAResiduesMassSingleton singleton = JPLAAResiduesMassSingleton.getInstance();
        double aaLateralChainMass;
        checkValidModifPosition(pos);
        if (modif instanceof JPLNeutralLoss) {
            try {
                aaLateralChainMass = singleton.getResidueMass(getAAByteAt(pos), this.massType) - singleton.getResidueSkeletonMass(this.massType);
                if (aaLateralChainMass + modif.getMassValue() < 0) {
                    throw new JPLInvalidAAModificationRuntimeException(getAAByteAt(pos), "the lateral chain of aa " + JPLAAEncoding.toChar(getAAByteAt(pos)) + " at position " + pos + " with a mass of " + aaLateralChainMass + " cannot lose a neutral loss mass of " + -modif.getMassValue());
                }
            } catch (JPLAAByteUndefinedException e) {
                throw new JPLInvalidAAModificationRuntimeException(getAAByteAt(pos), "byte " + getAAByteAt(pos) + " is an invalid byte code aa, neutral loss" + " checking cancelled !");
            }
        }
    }

    /**
	 * Check the neutral loss modifications.
	 * 
	 * @throws JPLInvalidAAModificationRuntimeException if a neutral loss is invalid.
	 */
    public void checkNeutralLosses() {
        if (isNeutralLossModifications) {
            JPLAAResiduesMassSingleton singleton = JPLAAResiduesMassSingleton.getInstance();
            double aaLateralChainMass;
            for (int i = 0; i < modifs.length; i++) {
                if (modifs[i] instanceof JPLNeutralLoss) {
                    try {
                        aaLateralChainMass = singleton.getResidueMass(getAAByteAt(i), this.massType) - singleton.getResidueSkeletonMass(this.massType);
                        if (aaLateralChainMass + modifs[i].getMassValue() < 0) {
                            throw new JPLInvalidAAModificationRuntimeException(getAAByteAt(i), "the lateral chain of aa " + JPLAAEncoding.toChar(getAAByteAt(i)) + " at position " + i + " with a mass of " + aaLateralChainMass + " cannot lose a neutral loss mass of " + -modifs[i].getMassValue());
                        }
                    } catch (JPLAAByteUndefinedException e) {
                        throw new JPLInvalidAAModificationRuntimeException(getAAByteAt(i), "byte " + getAAByteAt(i) + " is an invalid byte code aa, neutral loss" + " checking cancelled !");
                    }
                }
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (JPLAAEncoding.isNTerm(getNTerm().toByte())) {
            sb.append(getNTerm() + "-");
        }
        sb.append(toAAString());
        if (JPLAAEncoding.isCTerm(getCTerm().toByte())) {
            sb.append("-" + getCTerm());
        }
        return sb.toString();
    }
}
