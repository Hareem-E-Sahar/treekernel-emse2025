package uk.ac.ebi.pride.tools.converter.dao;

import uk.ac.ebi.pride.tools.converter.report.model.CvParam;
import uk.ac.ebi.pride.tools.converter.report.model.Identification;
import uk.ac.ebi.pride.tools.converter.report.model.Peptide;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class containing some commonly used
 * functions.
 *
 * @author jg
 */
public class Utils {

    /**
     * Regular expressions
     */
    public static final String MOD_REGEX = "\\[-?\\d+[\\.\\d+]?\\]";

    /**
     * An ENUM containing all known peptide
     * score parameters.
     *
     * @author jg
     */
    public enum PEPTIDE_SCORE_PARAM {

        /**
         * PRIDE CV
         */
        PRIDE_XTANDEM_CN("PRIDE:0000052", "Cn"), PRIDE_XTANDEM_DETAL_CN("PRIDE:0000012", "Delta Cn"), PRIDE_PEPTIDE_PROPHET_DISCRIMINANT_SCORE("PRIDE:0000138", "Discriminant score"), PRIDE_MASCOT_SCORE("PRIDE:0000069", "Mascot score"), PRIDE_OMSSA_E("PRIDE:0000185", "OMSSA E-value"), PRIDE_OMSSA_P("PRIDE:0000186", "OMSSA P-value"), PRIDE_PEPTIDE_PROPHET_PROBABILITY("PRIDE:0000099", "PeptideProphet probability score"), PRIDE_SEQUEST_SCORE("PRIDE:0000053", "Sequest score"), PRIDE_SPECTRUMMILL_SCORE("PRIDE:0000177", "Spectrum Mill peptide score"), PRIDE_XTANDEM_HYPERSCORE("PRIDE:0000176", "X!Tandem Hyperscore"), MS_MASCOT("MS:1001171", "Mascot:score"), MS_MYRIMATCH_MVH("MS:1001589", "MyriMatch:MVH"), MS_OMSSA_E("MS:1001328", "OMSSA:evalue"), MS_PARAGON_SCORE("MS:1001166", "Paragon:score"), MS_PHENYX_SCORE("MS:1001390", "Phenyx:Score"), MS_PROTEIN_EXTRACTOR_SCORE("MS:1001507", "ProteinExtractor:Score"), MS_PROTEIN_LYNC_SCORE("MS:1001571", "ProteinLync:Lputer Score"), MS_SEQUEST_METASCORE("MS:1001506", "ProteinScape:SequestMetaScore"), MS_SEQUEST_CONSENSUS_SCORE("MS:1001163", "Sequest:consensus score"), MS_SONAR_SCORE("MS:1001502", "Sonar:Score"), MS_SPECTRUMMILL_SCORE("MS:1001572", "SpectrumMill:Score"), MS_XTANDEM_HYPERSCORE("MS:1001331", "X!Tandem:hyperscore"), MS_PERCULATOR_SCORE("MS:1001492", "percolator:score");

        private String accession;

        private String name;

        private PEPTIDE_SCORE_PARAM(String accession, String name) {
            this.accession = accession;
            this.name = name;
        }

        public static boolean isScoreAccession(String accession) {
            for (PEPTIDE_SCORE_PARAM p : values()) {
                if (accession.equals(p.getAccession())) return true;
            }
            return false;
        }

        public String getAccession() {
            return accession;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * This class is static only and shouldn't
     * ever be used to created objects.
     */
    private Utils() {
    }

    /**
     * Extracts the peptide's score from the peptide
     * object and returns all found scores in a HashMap
     * with the score's accession as key and the score
     * as value.
     *
     * @param peptide The peptide to extract the score from.
     * @return A HashMap containing the found scores with the score's accession as key.
     */
    public static Map<String, Double> extractPeptideScores(Peptide peptide) {
        HashMap<String, Double> peptideScores = new HashMap<String, Double>();
        for (CvParam param : peptide.getAdditional().getCvParam()) {
            if (PEPTIDE_SCORE_PARAM.isScoreAccession(param.getAccession())) peptideScores.put(param.getAccession(), Double.parseDouble(param.getValue()));
        }
        return peptideScores;
    }

    /**
     * Returns all indistinguishable accessions associated
     * with this identification.
     *
     * @param identification
     * @return
     */
    public static Collection<String> getIndistinguishableAccessions(Identification identification) {
        if (identification.getAdditional() == null) return Collections.emptyList();
        ArrayList<String> indistinguishableAccessions = new ArrayList<String>();
        for (CvParam p : identification.getAdditional().getCvParam()) {
            if (p.getAccession().equals(DAOCvParams.INDISTINGUISHABLE_ACCESSION.getAccession())) indistinguishableAccessions.add(p.getValue());
        }
        return indistinguishableAccessions;
    }

    /**
     * Changes the primary accession used and replaces by a
     * "INDISTINGUISHABLE_ACCESSION". The old primary accession
     * is then reported as "INDISTINGUISHABLE_ACCESSION".
     *
     * @param identification    The identification to change.
     * @param newAccession      The new accession to use.
     * @param resolvedAccession The new accession as processed by the accession resolver. NULL if not available
     * @return The changed identification object.
     */
    public static Identification changePrimaryAccession(Identification identification, String newAccession, String resolvedAccession) {
        String oldAccession = identification.getAccession();
        identification.setAccession(newAccession);
        if (resolvedAccession != null && !resolvedAccession.equals(newAccession)) identification.setCuratedAccession(resolvedAccession); else identification.setCuratedAccession("");
        for (CvParam param : identification.getAdditional().getCvParam()) {
            if (param.getAccession().equals(DAOCvParams.INDISTINGUISHABLE_ACCESSION.getAccession())) {
                if (param.getValue().equals(newAccession)) {
                    param.setValue(oldAccession);
                    break;
                }
            }
        }
        return identification;
    }

    /**
     * Returns a map of modifications and their positions. It has to be true that the position of a modification at
     * the end of the input is equals to the length of the peptide sequence with modifications not counting for this
     * length. Modifications match the regular expression: \\[\\d+\\.\\d+\\]      (e.g. [19.9956])
     * IMPORTANT: Modifications in the result are in the same order that in the sequence.
     *
     * @param sequence The peptide sequence containing peptides and modifications
     * @return A Map of modifications and their positions. Mods include the preceding AA.
     */
    public static Map<Integer, String> getModifications(String sequence) {
        HashMap<Integer, String> res = new HashMap<Integer, String>();
        Pattern regex = Pattern.compile(MOD_REGEX);
        Matcher m = regex.matcher(sequence);
        int totalModsSize = 0;
        int numMods = 1;
        while (m.find()) {
            int pos = m.start() - 1;
            String mod = sequence.substring(pos, m.end());
            res.put(pos - totalModsSize + numMods, mod);
            totalModsSize += mod.length();
            numMods++;
        }
        return res;
    }
}
