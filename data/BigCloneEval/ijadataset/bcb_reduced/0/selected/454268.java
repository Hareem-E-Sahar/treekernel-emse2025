package src.projects.MutationAnalysis.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import src.lib.StatisticsTools;
import src.lib.ioInterfaces.FileIn;
import src.lib.ioInterfaces.Log_Buffer;
import src.lib.objects.SNP;
import src.lib.objects.Tuple;
import src.projects.MutationAnalysis.objects.SNPSignatureRecord;
import src.projects.MutationAnalysis.objects.Variation;
import src.projects.Utilities.Utils;
import src.projects.VariationDatabase.libs.VariationDButils;
import src.projects.VariationDatabase.objects.LibraryInfo;

/**
 * 
 * @author alirezak
 * @version 1.0
 */
public class VariationUtils {

    private static final double GENOME_LENGTH = 3e9;

    private static final int NUM_SUMATIC_SNVS = 1000;

    private static HashMap<String, ArrayList<Tuple<Integer, Integer>>> UCSC_CpG_Track;

    private static double[] logFactorialTable;

    private VariationUtils() {
    }

    static final Comparator<Tuple<Integer, HashSet<String>>> ORDER_VARIATIONS = new Comparator<Tuple<Integer, HashSet<String>>>() {

        public int compare(Tuple<Integer, HashSet<String>> e1, Tuple<Integer, HashSet<String>> e2) {
            return e1.get_first() - e2.get_first();
        }
    };

    private static final String UCSC_CPG_FILE = "/projects/alirezak_prj/genomes/hg18/hg18_ucsc_cpg_islands.txt";

    private static final int LOG_FACTORIAL_TABLE_SIZE = 1000000;

    private static final int MAX_TRANSCRIPT_LIBS = 5;

    private static final int GENOME_LIB_FACTOR = 5;

    public static void readVariations(Log_Buffer LB, String chromosome, String variationPath, HashMap<String, ArrayList<Tuple<Integer, HashSet<String>>>> variationPos) {
        if (variationPos.containsKey(chromosome)) {
            return;
        }
        String varFile = null;
        for (String f : Utils.getFilesInDir(variationPath)) {
            if (f.startsWith("chr" + chromosome + ".")) {
                varFile = variationPath + "/" + f;
            }
        }
        if (varFile == null) {
            LB.error("cannot find the variation file for chr" + chromosome + " in " + variationPath);
            LB.die();
            return;
        }
        FileIn in = new FileIn(LB, varFile, "\t", false);
        String[] line = null;
        LB.debug("reading the variation in chr" + chromosome);
        ArrayList<Tuple<Integer, HashSet<String>>> variations = new ArrayList<Tuple<Integer, HashSet<String>>>();
        while ((line = in.get_next()) != null) {
            if (line[0].startsWith("#")) {
                continue;
            }
            if (!line[1].equals(chromosome)) {
                LB.error("not a chromosome " + chromosome + " variation file");
                LB.die();
            }
            int pos = Integer.valueOf(line[3]);
            HashSet<String> libs = new HashSet<String>();
            for (String l : line[10].split(",")) {
                libs.add(l);
            }
            variations.add(new Tuple<Integer, HashSet<String>>(pos, libs));
        }
        Collections.sort(variations, ORDER_VARIATIONS);
        variationPos.put(chromosome, variations);
    }

    /**
	 * returns the log of choose(n, k): log(n!) - log(k!) - log((n-k)!)
	 * @param n
	 * @param k
	 * @return
	 */
    public static double logChoose(int n, int k) {
        return logFactorial(n) - logFactorial(k) - logFactorial(n - k);
    }

    /**
	 * return log(n!)
	 * @param n
	 * @return
	 */
    private static double logFactorial(int n) {
        if (logFactorialTable == null) {
            logFactorialTable = new double[LOG_FACTORIAL_TABLE_SIZE];
        } else if (n < LOG_FACTORIAL_TABLE_SIZE && logFactorialTable[n] > 0.0) {
            return logFactorialTable[n];
        }
        double r = 0;
        for (int i = 1; i <= n; i++) {
            r += Math.log10((double) i);
        }
        if (n < LOG_FACTORIAL_TABLE_SIZE) {
            logFactorialTable[n] = r;
        }
        return r;
    }

    /**
	 * returns the probability that we see x or more success after n trial if the probability of one success is p
	 * this binomial probability is approximated by normal distribution using erf function  
	 * @param p
	 * @param n
	 * @param x
	 * @return
	 */
    public static double ErfBasedProb(double p, int n, int x) {
        double mu = p * n;
        double sigma = Math.sqrt(n * p * (1 - p));
        double z = (x - mu) / sigma;
        double ret = 0.0;
        ret = 0.5 * (1.0 - StatisticsTools.erf(z / Math.sqrt(2.0)));
        return ret;
    }

    /**
	 * returns the probability that we see x success after n trial if the probability of one success is p
	 * @param p
	 * @param n
	 * @param x
	 * @return
	 */
    public static double logBinomialBasedProb(double p, int n, int x) {
        return logChoose(n, x) + x * Math.log10(p) + (n - x) * Math.log10(1 - p);
    }

    /**
	 * P-Value calculation method.
	 * If there was one mutation let q be the probability that the mutation occur in the specified region.
	 * q = target_region_length/genome_length   
	 * Let p_i be the probability that sample_i has k mutation out of total n mutation in the specified region
	 * p_i = c(n, k) . q^k . (1-q)^(n - k)
	 * Then the probability that we see k_0 mutations in sample_0, k_1 mutations in sample_1, ...., k_s mutations in sample_s is
	 * P = p_0 . p_1 . p_2 ... p_s
	 * Now we need to take into account that the order of the libraries with similar number of mutations is not important.
	 * To do so we need multiply P by the number of possible combinations that the we see same mutation numbers {k_0, k_1, ..., k_s} in the 
	 * set of the samples.
	 * For instance if there are no similar k's in the set {k_0, k_1, ..., k_s} the combinations would be s!.
	 * Assume that there are m_0 samples with k_0 mutations, m_1 samples with k_1 mutations and ... . Clearly Sum(m_i) = s
	 * Then the combination coefficient for P, C = c(s, m_0).c(s-m_0, m_1).c(s-m_0-m_1, m_2)....c(s - m_0 - m_1... - m_(k-2), m_(k-1))
	 * The p-value will be C.P 
	 * 
	 * 
	 *  
	 * @param libVarCount
	 * @param cancerType
	 * @param numTargetRegion
	 * @param targetRegionLength
	 * @param libsInCancerType
	 * @param libCancerTypeMap
	 * @return
	 */
    public static double getP_Value(HashMap<String, Integer> libVarCount, String cancerType, int numTargetRegion, int targetRegionLength, HashMap<String, Integer> libsInCancerType, HashMap<String, String> libCancerTypeMap) {
        double probTargetRegionHasOneMutatedInOneLib = (((double) targetRegionLength) / GENOME_LENGTH);
        double logPValue = 0;
        int libsWithMutations = 0;
        HashMap<Integer, Integer> countCount = new HashMap<Integer, Integer>();
        for (String lib : libVarCount.keySet()) {
            if (libCancerTypeMap.containsKey(lib) && libCancerTypeMap.get(lib).contains(cancerType)) {
                libsWithMutations++;
                int k = libVarCount.get(lib);
                if (!countCount.containsKey(k)) {
                    countCount.put(k, 0);
                }
                countCount.put(k, countCount.get(k) + 1);
                double logProbCurLibHasKmutation = logChoose(NUM_SUMATIC_SNVS, k) + k * Math.log10(probTargetRegionHasOneMutatedInOneLib) + (NUM_SUMATIC_SNVS - k) * Math.log10(1 - probTargetRegionHasOneMutatedInOneLib);
                logPValue += logProbCurLibHasKmutation;
            }
        }
        countCount.put(0, libsInCancerType.get(cancerType) - libsWithMutations);
        logPValue += (logChoose(NUM_SUMATIC_SNVS, 0) + (NUM_SUMATIC_SNVS) * Math.log10(1 - probTargetRegionHasOneMutatedInOneLib)) * (libsInCancerType.get(cancerType) - libsWithMutations);
        Double C = (double) 0;
        int sum = 0;
        int N = libsInCancerType.get(cancerType);
        for (int key : countCount.keySet()) {
            sum += countCount.get(key);
            C += logChoose(N, countCount.get(key));
            N -= countCount.get(key);
        }
        assert (libsInCancerType.get(cancerType) == sum);
        logPValue += C;
        logPValue += Math.log10((double) numTargetRegion);
        return logPValue;
    }

    /**
	 * returns the smallest index of the variation that is greater than or equal to start 
	 * @param start
	 * @param variations
	 * @return
	 */
    public static int findUpperBoundary(int start, ArrayList<Tuple<Integer, HashSet<String>>> variations) {
        if (variations == null) {
            return Integer.MAX_VALUE;
        }
        int top = 0;
        int bot = variations.size();
        int mid = 0;
        while (top < bot) {
            mid = (top + bot) / 2;
            if (start < variations.get(mid).get_first()) {
                bot = mid - 1;
            } else if (start > variations.get(mid).get_first()) {
                top = mid + 1;
            } else {
                return mid;
            }
        }
        return Utils.min2(top, variations.size() - 1);
    }

    /**
	 * returns the smallest index of the variation that is greater than or equal to start 
	 * @param start
	 * @param variations
	 * @return
	 */
    public static int findUpperBoundarySNP(int start, ArrayList<Tuple<SNP, HashSet<String>>> variations) {
        if (variations == null) {
            return Integer.MAX_VALUE;
        }
        int top = 0;
        int bot = variations.size();
        int mid = 0;
        while (top < bot) {
            mid = (top + bot) / 2;
            if (start < variations.get(mid).get_first().get_position()) {
                bot = mid - 1;
            } else if (start > variations.get(mid).get_first().get_position()) {
                top = mid + 1;
            } else {
                return mid;
            }
        }
        return Utils.max2(0, Utils.min2(top, variations.size() - 1));
    }

    /**
	 * returns the smallest index of the variation that is greater than or equal to start 
	 * @param s
	 * @param variations
	 * @return
	 */
    public static int findUpperBoundarySNP(SNP s, ArrayList<SNP> variations) {
        if (variations == null) {
            return Integer.MAX_VALUE;
        }
        int top = 0;
        int bot = variations.size();
        int mid = 0;
        while (top < bot) {
            mid = (top + bot) / 2;
            if (s.compareTo(variations.get(mid)) < 0) {
                bot = mid - 1;
            } else if (s.compareTo(variations.get(mid)) > 0) {
                top = mid + 1;
            } else {
                return mid;
            }
        }
        return Utils.max2(0, Utils.min2(top, variations.size() - 1));
    }

    /**
	 * returns the smallest index of the variation that is greater than or equal to start 
	 * @param var
	 * @param variations
	 * @return
	 */
    public static int findUpperBoundaryVariation(Variation var, ArrayList<Variation> variations) {
        if (variations == null) {
            return Integer.MAX_VALUE;
        }
        int top = 0;
        int bot = variations.size();
        int mid = 0;
        while (top < bot) {
            mid = (top + bot) / 2;
            if (var.compareTo(variations.get(mid)) < 0) {
                bot = mid - 1;
            } else if (var.compareTo(variations.get(mid)) > 0) {
                top = mid + 1;
            } else {
                return mid;
            }
        }
        return Utils.max2(0, Utils.min2(top, variations.size() - 1));
    }

    public static ArrayList<Tuple<Integer, HashSet<String>>> readVariations(Log_Buffer LB, String variationPath, String chromosome, int start, int end, int window) {
        String varFile = null;
        for (String f : Utils.getFilesInDir(variationPath)) {
            if (f.startsWith("chr" + chromosome + ".")) {
                varFile = variationPath + "/" + f;
            }
        }
        if (varFile == null) {
            LB.error("cannot find the variation file for chr" + chromosome + " in " + variationPath);
            LB.die();
            return null;
        }
        FileIn in = new FileIn(LB, varFile, "\t", false);
        ArrayList<Tuple<Integer, HashSet<String>>> variationPos = new ArrayList<Tuple<Integer, HashSet<String>>>();
        String[] line = null;
        LB.debug("reading the variation in ranger chr" + chromosome + ":" + start + "-" + end);
        while ((line = in.get_next()) != null) {
            if (line[0].startsWith("#")) {
                continue;
            }
            if (!line[1].equals(chromosome)) {
                LB.error("not a chromosome " + chromosome + " variation file");
                LB.die();
            }
            int pos = Integer.valueOf(line[3]);
            if (pos > end + window) {
                break;
            }
            if (pos >= start - window && pos <= end + window) {
                HashSet<String> libs = new HashSet<String>();
                for (String l : line[10].split(",")) {
                    libs.add(l);
                }
                variationPos.add(new Tuple<Integer, HashSet<String>>(pos, libs));
            }
        }
        return variationPos;
    }

    public static ArrayList<Tuple<SNP, HashSet<String>>> readSNPS(Log_Buffer LB, String variationPath, String chromosome, int start, int end, int window) {
        String varFile = null;
        for (String f : Utils.getFilesInDir(variationPath)) {
            if (f.startsWith("chr" + chromosome + ".")) {
                varFile = variationPath + "/" + f;
            }
        }
        if (varFile == null) {
            LB.error("cannot find the variation file for chr" + chromosome + " in " + variationPath);
            LB.die();
            return null;
        }
        FileIn in = new FileIn(LB, varFile, "\t", false);
        ArrayList<Tuple<SNP, HashSet<String>>> variationPos = new ArrayList<Tuple<SNP, HashSet<String>>>();
        String[] line = null;
        LB.debug("reading the variation in ranger chr" + chromosome + ":" + start + "-" + end);
        while ((line = in.get_next()) != null) {
            if (line[0].startsWith("#") || line[0].charAt(0) != 'S') {
                continue;
            }
            if (!line[1].equals(chromosome)) {
                LB.error("not a chromosome " + chromosome + " variation file");
                LB.die();
            }
            int pos = Integer.valueOf(line[3]);
            char alt = line[5].charAt(0);
            char ref = line[6].charAt(0);
            SNP s = new SNP(chromosome, pos, alt, ref, -1, -1, -1, -1, -1, -1, Integer.valueOf(line[2]), -1, -1, -1);
            if (pos > end + window) {
                break;
            }
            if (pos >= start - window && pos <= end + window) {
                HashSet<String> libs = new HashSet<String>();
                for (String l : line[10].split(",")) {
                    libs.add(l);
                }
                variationPos.add(new Tuple<SNP, HashSet<String>>(s, libs));
            }
        }
        return variationPos;
    }

    public static ArrayList<Tuple<SNP, HashSet<String>>> readSNPS(Log_Buffer LB, String variationPath, String chromosome, boolean includeMatching, HashMap<String, String> libProtocols, boolean unannotatedSnps, boolean excludeTranscriptome) {
        return readSNPS(LB, variationPath, chromosome, null, includeMatching, libProtocols, unannotatedSnps, excludeTranscriptome);
    }

    /**
	 * 
	 * @param LB
	 * @param variationPath
	 * @param chromosome
	 * @param targetLibraries //returns snps that are in the target libraries.
	 * @param includeMatching //if true include the snps and exists in the matching samples of the targetLibraries.
	 * @param libProtocols
	 * @param unannotatedSnps
	 * @param excludeTranscriptome
	 * @return
	 */
    public static ArrayList<Tuple<SNP, HashSet<String>>> readSNPS(Log_Buffer LB, String variationPath, String chromosome, HashSet<String> targetLibraries, boolean includeMatching, HashMap<String, String> libProtocols, boolean unannotatedSnps, boolean excludeTranscriptome) {
        String varFile = null;
        for (String f : Utils.getFilesInDir(variationPath)) {
            if (f.startsWith("chr" + chromosome + ".")) {
                varFile = variationPath + "/" + f;
            }
        }
        if (varFile == null) {
            LB.error("cannot find the variation file for chr" + chromosome + " in " + variationPath);
            return null;
        }
        HashMap<String, HashSet<String>> matchedLibs = new HashMap<String, HashSet<String>>();
        if (includeMatching) {
            for (String l : targetLibraries) {
                for (String matchedLib : VariationDButils.getMatchingLibraries(l)) {
                    LibraryInfo li = VariationDButils.getLibraryRecord(matchedLib);
                    if (li.getCancer()) {
                        if (!excludeTranscriptome || !li.getProtocol().toLowerCase().contains("transcript")) {
                            if (!matchedLibs.containsKey(matchedLib)) {
                                matchedLibs.put(matchedLib, new HashSet<String>());
                            }
                            matchedLibs.get(matchedLib).add(l);
                        }
                    }
                }
            }
        }
        FileIn in = new FileIn(LB, varFile, "\t", false);
        ArrayList<Tuple<SNP, HashSet<String>>> variationPos = new ArrayList<Tuple<SNP, HashSet<String>>>();
        int chrIndex = 1;
        int idIndex = 2;
        int posIndex = 3;
        int altIndex = 5;
        int refIndex = 6;
        int tagIndex = 7;
        int libIndex = 10;
        if (unannotatedSnps) {
            chrIndex = 1;
            idIndex = 0;
            posIndex = 2;
            altIndex = 3;
            refIndex = 4;
            libIndex = 9;
            tagIndex = 11;
        }
        String[] line = null;
        LB.debug("reading the variation in chr" + chromosome);
        while ((line = in.get_next()) != null) {
            if (line[0].startsWith("#")) {
                continue;
            }
            if (!unannotatedSnps && line[0].charAt(0) != 'S') {
                continue;
            }
            if (!line[chrIndex].equals(chromosome)) {
                LB.error("not a chromosome " + chromosome + " variation file");
                LB.die();
            }
            int tag = 0;
            if (line[tagIndex].equals("nc")) {
                tag = 0;
            } else if (line[tagIndex].equals("syn")) {
                tag = 1;
            } else if (line[tagIndex].equals("nsyn")) {
                tag = 2;
            } else if (line[tagIndex].equals("unknown")) {
                tag = -1;
            } else {
                LB.debug("unknown snp tag " + line[tagIndex]);
                LB.die();
            }
            int pos = Integer.valueOf(line[posIndex]);
            char alt = line[altIndex].charAt(0);
            char ref = line[refIndex].charAt(0);
            SNP s = new SNP(chromosome, pos, alt, ref, -1, -1, -1, tag, -1, -1, Integer.valueOf(line[idIndex]), -1, -1, -1);
            HashSet<String> libs = new HashSet<String>();
            boolean rnaEditTested = false;
            boolean isRnaEdit = false;
            for (String l : line[libIndex].split(",")) {
                if (targetLibraries.contains(l)) {
                    libs.add(l);
                } else if (includeMatching && matchedLibs.containsKey(l)) {
                    if (rnaEditTested && !isRnaEdit) {
                        for (String m : matchedLibs.get(l)) {
                            libs.add(m);
                        }
                    } else if (!rnaEditTested) {
                        int transcrptomeLibs = 0;
                        int genomicLibs = 0;
                        rnaEditTested = true;
                        for (String ll : line[libIndex].split(",")) {
                            if (!libProtocols.containsKey(ll)) {
                                continue;
                            }
                            String protocol = libProtocols.get(ll).toLowerCase();
                            if (protocol != null) {
                                if (protocol.contains("genom") || protocol.contains("exo")) {
                                    genomicLibs++;
                                } else if (protocol.contains("transc")) {
                                    transcrptomeLibs++;
                                }
                            }
                        }
                        if (transcrptomeLibs < MAX_TRANSCRIPT_LIBS + GENOME_LIB_FACTOR * genomicLibs) {
                            for (String m : matchedLibs.get(l)) {
                                libs.add(m);
                            }
                            isRnaEdit = false;
                        } else {
                            isRnaEdit = true;
                        }
                    }
                }
            }
            if (libs.size() != 0) {
                variationPos.add(new Tuple<SNP, HashSet<String>>(s, libs));
            }
        }
        LB.debug("read " + variationPos.size() + " variations in chromosome " + chromosome);
        return variationPos;
    }

    /**
	 * returns true if the snp is within a CpG island.
	 * @param LB
	 * @param s
	 * @return
	 */
    public static boolean isInCpG(Log_Buffer LB, SNP s) {
        if (UCSC_CpG_Track == null) {
            loadUCSC_CPG_Track(LB);
        }
        int top = 0;
        int bot = UCSC_CpG_Track.get(s.get_chromosome()).size();
        int mid = 0;
        while (top <= bot) {
            mid = (top + bot) / 2;
            if (UCSC_CpG_Track.get(s.get_chromosome()).get(mid).get_first() < s.get_position()) {
                top = mid + 1;
            } else if (UCSC_CpG_Track.get(s.get_chromosome()).get(mid).get_first() > s.get_position()) {
                bot = mid - 1;
            } else {
                return true;
            }
        }
        if (UCSC_CpG_Track.get(s.get_chromosome()).get(mid).get_first() > s.get_position()) {
            mid--;
        }
        if (mid < 0) {
            return false;
        }
        assert (UCSC_CpG_Track.get(s.get_chromosome()).get(mid).get_first() < s.get_position());
        if (s.get_position() <= UCSC_CpG_Track.get(s.get_chromosome()).get(mid).get_second()) {
            return true;
        }
        return false;
    }

    private static void loadUCSC_CPG_Track(Log_Buffer LB) {
        UCSC_CpG_Track = new HashMap<String, ArrayList<Tuple<Integer, Integer>>>();
        FileIn in = new FileIn(LB, UCSC_CPG_FILE, "\t", false);
        String[] line = null;
        while ((line = in.get_next()) != null) {
            if (line[0].startsWith("#")) {
                continue;
            }
            String chromosome = line[0].substring(3);
            if (!UCSC_CpG_Track.containsKey(chromosome)) {
                UCSC_CpG_Track.put(chromosome, new ArrayList<Tuple<Integer, Integer>>());
            }
            UCSC_CpG_Track.get(chromosome).add(new Tuple<Integer, Integer>(Integer.valueOf(line[1]) + 1, Integer.valueOf(line[2])));
        }
        for (String chr : UCSC_CpG_Track.keySet()) {
            Collections.sort(UCSC_CpG_Track.get(chr), Utils.ORDER_TUPLE_INTEGER_INTEGER);
        }
    }

    private static char complement(char base) {
        switch(base) {
            case 'A':
                return 'T';
            case 'C':
                return 'G';
            case 'G':
                return 'C';
            case 'T':
                return 'A';
            default:
                return 'N';
        }
    }

    private static String complement(String base) {
        switch(base.charAt(0)) {
            case 'A':
                return "T";
            case 'C':
                return "G";
            case 'G':
                return "C";
            case 'T':
                return "A";
            default:
                return base;
        }
    }

    public static String reveverseComplement(String seq) {
        String ret = "";
        for (char c : seq.toCharArray()) {
            ret = complement(String.valueOf(c)) + ret;
        }
        return ret;
    }

    private static String motifBases(char c, boolean strand) {
        String ret = "";
        switch(c) {
            case 'A':
            case 'C':
            case 'G':
            case 'T':
                ret = String.valueOf(c);
                break;
            case 'D':
                ret = "AGT";
                break;
            case 'Y':
                ret = "CT";
                break;
            case 'W':
                ret = "AT";
                break;
            case 'H':
                ret = "ACT";
                break;
            case 'R':
                ret = "AG";
                break;
            default:
                return null;
        }
        if (strand) {
            return ret;
        } else {
            return VariationUtils.reveverseComplement(ret);
        }
    }

    /**
	 * if the seq of motif are a subsequence of each other return true
	 * @param seq
	 * @param motif
	 * @param strand
	 * @return
	 */
    private static boolean isAMotif(String seq, String motif, Boolean strand) {
        int i = 0;
        for (char c : seq.toCharArray()) {
            if (i >= motif.length()) {
                break;
            }
            if (strand) {
                if (!motifBases(motif.charAt(i), strand).contains(String.valueOf(c))) {
                    return false;
                }
            } else {
                if (!motifBases(motif.charAt(i), strand).contains(String.valueOf(complement(c)))) {
                    return false;
                }
            }
            i++;
        }
        return true;
    }

    private static boolean isInMotif(String chromosome, int snpLoc, boolean strand, String motif, GenomeItrIndexed genomeItr) {
        for (int p = snpLoc - motif.length() + 1; p <= snpLoc + motif.length() - 1; p++) {
            String seq = genomeItr.substringFast(chromosome, p, p + motif.length() - 1);
            if (isAMotif(seq, motif, strand)) {
                return true;
            }
        }
        return false;
    }

    /**
	 * this function returns information such as frequency of mutated bases, the density of mutations, etc.
	 * @param chromosome
	 * @param start
	 * @param end
	 * @param strand
	 * @param snps
	 * @param uniqueSNVsUsed
	 * @param genomeItr
	 * @param Motifs
	 * @param targetLibsNum
	 * @return
	 */
    public static SNPSignatureRecord getSNPSignatureRecord(String chromosome, int start, int end, boolean strand, ArrayList<Tuple<SNP, HashSet<String>>> snps, boolean uniqueSNVsUsed, GenomeItrIndexed genomeItr, ArrayList<String> Motifs, int targetLibsNum) {
        int numSnps = 0;
        int uniqueSnps = 0;
        int transitions = 0;
        int transversions = 0;
        int regionLength = end - start + 1;
        HashMap<String, Integer> mutationsInMotif = new HashMap<String, Integer>();
        HashMap<String, Integer> basesInMotif = new HashMap<String, Integer>();
        HashMap<Character, Integer> baseMutationCount = new HashMap<Character, Integer>();
        HashMap<Character, Integer> baseCount = new HashMap<Character, Integer>();
        HashMap<String, Integer> changeCount = new HashMap<String, Integer>();
        for (char c1 : "ACGT".toCharArray()) {
            baseCount.put(c1, 0);
            baseMutationCount.put(c1, 0);
            for (char c2 : "ACGT".toCharArray()) {
                changeCount.put(c1 + ">" + c2, 0);
            }
        }
        for (String mf : Motifs) {
            basesInMotif.put(mf, 0);
            mutationsInMotif.put(mf, 0);
        }
        String seq = genomeItr.substringFast(chromosome, start, end);
        int i = start;
        for (char c : seq.toCharArray()) {
            if (!"ACGT".contains(c + "")) {
                continue;
            }
            for (String mf : Motifs) {
                if (isInMotif(chromosome, i++, strand, mf, genomeItr)) {
                    basesInMotif.put(mf, basesInMotif.get(mf) + 1);
                }
            }
            if (strand) {
                baseCount.put(c, baseCount.get(c) + 1);
            } else {
                baseCount.put(complement(c), baseCount.get(complement(c)) + 1);
            }
        }
        HashSet<String> mutatedLibs = new HashSet<String>();
        for (Tuple<SNP, HashSet<String>> t : snps) {
            start = Utils.min2(start, t.get_first().get_position());
            end = Utils.max2(end, t.get_first().get_position());
            numSnps += t.get_second().size();
            uniqueSnps++;
            mutatedLibs.addAll(t.get_second());
            int count = 1;
            if (!uniqueSNVsUsed) {
                count = t.get_second().size();
            }
            for (String mf : Motifs) {
                if (isInMotif(t.get_first().get_chromosome(), t.get_first().get_position(), strand, mf, genomeItr)) {
                    mutationsInMotif.put(mf, mutationsInMotif.get(mf) + 1);
                }
            }
            char ref = t.get_first().get_snp_cannonical();
            char obs = t.get_first().get_new_base();
            if (strand) {
                String k = ref + ">" + obs;
                changeCount.put(k, changeCount.get(k) + count);
            } else {
                String k = complement(ref) + ">" + complement(obs);
                changeCount.put(k, changeCount.get(k) + count);
            }
            if (ref == 'A') {
                if (obs == 'G') {
                    transitions += count;
                } else {
                    transversions += count;
                }
            } else if (ref == 'G') {
                if (obs == 'A') {
                    transitions += count;
                } else {
                    transversions += count;
                }
            } else if (ref == 'C') {
                if (obs == 'T') {
                    transitions += count;
                } else {
                    transversions += count;
                }
            } else if (ref == 'T') {
                if (obs == 'C') {
                    transitions += count;
                } else {
                    transversions += count;
                }
            }
        }
        for (char c1 : "ACGT".toCharArray()) {
            for (char c2 : "ACGT".toCharArray()) {
                if (c1 != c2) {
                    if (changeCount.containsKey(c1 + ">" + c2)) {
                        baseMutationCount.put(c1, baseMutationCount.get(c1) + changeCount.get(c1 + ">" + c2));
                    }
                }
            }
        }
        return new SNPSignatureRecord(numSnps, uniqueSnps, mutatedLibs.size(), targetLibsNum, transitions, transversions, regionLength, mutationsInMotif, basesInMotif, baseMutationCount, baseCount, changeCount, uniqueSNVsUsed);
    }

    /**
	 * returns the overlap between the two regions. returns 0 if the regions don't overlap
	 * @param s1
	 * @param e1
	 * @param s2
	 * @param e2
	 * @return
	 */
    public static int getOverlap(int s1, int e1, int s2, int e2) {
        int overlap = Utils.min2(e1 - s2, e2 - s1);
        overlap = Utils.min2(overlap, e1 - s1 + 1);
        overlap = Utils.min2(overlap, e2 - s2 + 1);
        return Utils.max2(0, overlap);
    }

    public static ArrayList<Double> benjaminiCorrection(ArrayList<Double> pvalues) {
        Collections.sort(pvalues);
        ArrayList<Double> qvalues = new ArrayList<Double>(pvalues);
        for (int i = pvalues.size() - 2; i >= 0; i--) {
            double q = pvalues.get(i) * pvalues.size() / (i + 1);
            q = Utils.min2(q, qvalues.get(i + 1));
            qvalues.set(i, q);
        }
        return qvalues;
    }
}
