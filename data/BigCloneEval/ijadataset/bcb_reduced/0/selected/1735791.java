package uk.ac.ebi.pride.tools.converter.dao_crux_txt.model;

import uk.ac.ebi.pride.tools.converter.dao_crux_txt.parsers.CruxParametersParserResult;
import uk.ac.ebi.pride.tools.converter.report.model.PTM;
import uk.ac.ebi.pride.tools.converter.report.model.PeptidePTM;
import uk.ac.ebi.pride.tools.converter.utils.ConverterException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Its pretty much a POJO representing a peptide
 * @author Jose A. Dianes
 * @version $Id$
 */
public class CruxPeptide {

    /**
     * General expressions
     */
    private static final String modRegex = "\\[-?\\d+\\.\\d+\\]";

    private final int scan;

    private final int charge;

    private final double specPrecursorMZ;

    private final double specNeutralMass;

    private final double peptideMass;

    private final double deltaCn;

    private final double xcorrScore;

    private final int xcorrRank;

    private final int matchesSpectrum;

    private final String sequence;

    private final String claveageType;

    private Map<String, String> flankingAA;

    public CruxPeptide(int scan, int charge, double specPrecursorMZ, double specNeutralMass, double peptideMass, double deltaCn, double xcorrScore, int xcorrRank, int matchesSpectrum, String sequence, String claveageType, String[] proteinIds, String[] flankingAA) {
        if (proteinIds.length != flankingAA.length) throw new ConverterException("Not enough flanking AA for proteins");
        this.scan = scan;
        this.charge = charge;
        this.specPrecursorMZ = specPrecursorMZ;
        this.specNeutralMass = specNeutralMass;
        this.peptideMass = peptideMass;
        this.deltaCn = deltaCn;
        this.xcorrScore = xcorrScore;
        this.xcorrRank = xcorrRank;
        this.matchesSpectrum = matchesSpectrum;
        this.sequence = sequence;
        this.claveageType = claveageType;
        this.flankingAA = new HashMap<String, String>();
        int pos = 0;
        for (String proteinId : proteinIds) {
            this.flankingAA.put(proteinId, flankingAA[pos]);
            pos++;
        }
    }

    public int getScan() {
        return scan;
    }

    public int getCharge() {
        return charge;
    }

    public double getSpecPrecursorMZ() {
        return specPrecursorMZ;
    }

    public double getSpecNeutralMass() {
        return specNeutralMass;
    }

    public double getPeptideMass() {
        return peptideMass;
    }

    public double getDeltaCn() {
        return deltaCn;
    }

    public double getXcorrScore() {
        return xcorrScore;
    }

    public int getXcorrRank() {
        return xcorrRank;
    }

    public int getMatchesSpectrum() {
        return matchesSpectrum;
    }

    public String getSequence() {
        return sequence;
    }

    public String getClaveageType() {
        return claveageType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + scan;
        result = prime * result + charge;
        long temp;
        temp = Double.doubleToLongBits(specPrecursorMZ);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(specNeutralMass);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(peptideMass);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(deltaCn);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(xcorrScore);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + xcorrRank;
        result = prime * result + matchesSpectrum;
        result = prime * result + ((sequence == null) ? 0 : sequence.hashCode());
        result = prime * result + ((claveageType == null) ? 0 : claveageType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        CruxPeptide other = (CruxPeptide) obj;
        if (charge != other.scan) return false;
        if (charge != other.charge) return false;
        if (Double.doubleToLongBits(specPrecursorMZ) != Double.doubleToLongBits(other.specPrecursorMZ)) return false;
        if (Double.doubleToLongBits(specNeutralMass) != Double.doubleToLongBits(other.specNeutralMass)) return false;
        if (Double.doubleToLongBits(peptideMass) != Double.doubleToLongBits(other.peptideMass)) return false;
        if (Double.doubleToLongBits(deltaCn) != Double.doubleToLongBits(other.deltaCn)) return false;
        if (Double.doubleToLongBits(xcorrScore) != Double.doubleToLongBits(other.xcorrScore)) return false;
        if (xcorrRank != other.xcorrRank) return false;
        if (matchesSpectrum != other.matchesSpectrum) return false;
        if (sequence == null) {
            if (other.sequence != null) return false;
        } else if (!sequence.equals(other.sequence)) return false;
        if (claveageType == null) {
            if (other.claveageType != null) return false;
        } else if (!claveageType.equals(other.claveageType)) return false;
        return true;
    }

    /**
     * Returns a list of peptide PTMs associated with this Crux peptide. It includes variable and fixed PTMs (including
     * terminal modifications). It makes use of the parameter parsing result where it can find information about the 
     * variable and fixed modifications defined during the search.
     *
     * It makes several checks about the existence or not in the param file of the modifications in the sequence, throwing
     * ConverterExcetion (unchecked) when wrong.
     * 
     * @return A Collection of PeptidePTM associated with this Crux peptide.
     */
    public Collection<PeptidePTM> getPTMs(CruxParametersParserResult params) {
        LinkedList<PeptidePTM> result = new LinkedList<PeptidePTM>();
        Map<Integer, String> mods = getModifications(sequence);
        if (mods.containsKey(1)) {
            String[] modDelta = mods.get(1).split("[\\[\\]]");
            if ((params.ntermPTM != null) && (params.ntermPTM.getSearchEnginePTMLabel().equals(modDelta[1] + "@nterm"))) {
                PeptidePTM newPeptidePTM = newPeptidePtm(params.ntermPTM, 0);
                result.add(newPeptidePTM);
                mods.remove(1);
            }
        }
        String lastMod = getLastMod();
        if (lastMod != null) {
            String[] modDelta = lastMod.split("[\\[\\]]");
            if ((params.ctermPTM != null) && (params.ctermPTM.getSearchEnginePTMLabel().equals(modDelta[1] + "@cterm"))) {
                PeptidePTM newPeptidePTM = newPeptidePtm(params.ctermPTM, (Integer) mods.keySet().toArray()[mods.size() - 1] + 1);
                result.add(newPeptidePTM);
                mods.remove((Integer) mods.keySet().toArray()[mods.size() - 1]);
            }
        }
        for (Map.Entry<Integer, String> mod : mods.entrySet()) {
            String[] modDelta = mod.getValue().split("[\\[\\]]");
            PTM ptm = params.aaToPtm.get(mod.getValue());
            if (ptm == null) throw new ConverterException("Undefined variable PTM in peptide sequence: " + sequence); else {
                PeptidePTM newPeptidePtm = newPeptidePtm(ptm, mod.getKey());
                result.add(newPeptidePtm);
            }
        }
        int pos = 0;
        for (String aa : getAllAAInSequence(sequence)) {
            if (params.aaToFixedPtm.containsKey(aa)) {
                PeptidePTM newPTM = newPeptidePtm(params.aaToFixedPtm.get(aa), pos + 1);
                result.add(newPTM);
            }
            pos++;
        }
        if (params.ntermFixedPTM != null) {
            PeptidePTM newPtm = newPeptidePtm(params.ntermFixedPTM, 0);
            result.add(newPtm);
        }
        if (params.ctermFixedPTM != null) {
            PeptidePTM newPtm = newPeptidePtm(params.ntermFixedPTM, (Integer) mods.keySet().toArray()[mods.size() - 1] + 1);
            result.add(newPtm);
        }
        return result;
    }

    /**
     * Gets the last modification in the sequence if present
     * @return The last modification including the aa. Null if not present.
     */
    private String getLastMod() {
        String res = null;
        Pattern regex = Pattern.compile(modRegex + "$");
        Matcher m = regex.matcher(sequence);
        if (m.find()) {
            int pos = m.start() - 1;
            res = sequence.substring(pos, m.end());
        }
        return res;
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
    private Map<Integer, String> getModifications(String sequence) {
        HashMap<Integer, String> res = new HashMap<Integer, String>();
        Pattern regex = Pattern.compile(modRegex);
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

    private String[] getAllAAInSequence(String sequence) {
        String[] tempAss = sequence.replaceAll(modRegex, "").split("");
        String[] aas = Arrays.copyOfRange(tempAss, 1, tempAss.length);
        return aas;
    }

    private PeptidePTM newPeptidePtm(PTM ptm, long location) {
        PeptidePTM newPeptidePtm = new PeptidePTM();
        newPeptidePtm.setSearchEnginePTMLabel(ptm.getSearchEnginePTMLabel());
        newPeptidePtm.setFixedModification(ptm.isFixedModification());
        newPeptidePtm.setResidues(ptm.getResidues());
        newPeptidePtm.setModLocation(location);
        for (String monoDelta : ptm.getModMonoDelta()) {
            newPeptidePtm.getModMonoDelta().add(monoDelta);
        }
        return newPeptidePtm;
    }

    public String getPrevAA(String proteinId) {
        return "" + this.flankingAA.get(proteinId).charAt(0);
    }

    public String getNextAA(String proteinId) {
        return "" + this.flankingAA.get(proteinId).charAt(1);
    }

    /**
     * Creates a new CruxPeptide object from the passed
     * fields and header.
     * @param fields The fields of the line representing the peptide.
     * @param header A Map mapping a given column name to its 0-based index.
     * @return The CruxPeptide object representing the line.
     */
    public static CruxPeptide createCruxPeptide(String[] fields, Map<String, Integer> header) {
        String[] proteinIds = fields[header.get("protein id")].split(",");
        String[] flankingAA = fields[header.get("flanking aa")].split(",");
        CruxPeptide peptide = new CruxPeptide(Integer.parseInt(fields[header.get("scan")]), Integer.parseInt(fields[header.get("charge")]), Double.parseDouble(fields[header.get("spectrum precursor m/z")]), Double.parseDouble(fields[header.get("spectrum neutral mass")]), Double.parseDouble(fields[header.get("peptide mass")]), Double.parseDouble(fields[header.get("delta_cn")]), Double.parseDouble(fields[header.get("xcorr score")]), Integer.parseInt(fields[header.get("xcorr rank")]), Integer.parseInt(fields[header.get("matches/spectrum")]), fields[header.get("sequence")], fields[header.get("cleavage type")], proteinIds, flankingAA);
        return peptide;
    }
}
