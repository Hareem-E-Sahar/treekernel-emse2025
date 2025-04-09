package hadit;

import genomeUtils.GenotypeUtils;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import nutils.ArrayUtils;
import nutils.CompareUtils;
import nutils.IOUtils;

public class AffySNPMap {

    public static final char[] NucleotideMap = { 'N', 'A', 'C', 'G', 'T' };

    public static final int NucleotideMapUnknownAlleleIndex = 0;

    public static final int IndexAlleleA = 0;

    public static final int IndexAlleleB = 1;

    public static final int IndexAlleleBoth = 2;

    private long[][] mSNPs;

    private long[][] mSNPsByRsId;

    private int[] mNumSNPs;

    public static final int DefaultRsId = 0;

    public static final boolean DefaultStrand = true;

    public AffySNPMap(int[] chromCounts, int numChromUsed) {
        mSNPs = new long[numChromUsed][];
        mSNPsByRsId = new long[numChromUsed][];
        mNumSNPs = new int[numChromUsed];
        for (int i = 0; i < numChromUsed; i++) {
            mSNPs[i] = new long[chromCounts[i]];
            mSNPsByRsId[i] = new long[chromCounts[i]];
            Arrays.fill(mSNPs[i], 0);
            Arrays.fill(mSNPsByRsId[i], 0);
            mNumSNPs[i] = 0;
        }
    }

    public static boolean isMissingAllele(char allele) {
        return (allele == NucleotideMap[0]);
    }

    public void printMe(boolean orderedByPosition, String outFilename) {
        BufferedWriter out = IOUtils.getBufferedWriter(outFilename);
        long[][] tableToPrint = orderedByPosition ? mSNPs : mSNPsByRsId;
        printMe(tableToPrint, out, orderedByPosition);
        IOUtils.closeBufferedWriter(out);
    }

    private static void printMe(long[][] tableToPrint, BufferedWriter out, boolean orderedByPosition) {
        byte[] nucs = new byte[CopyNumberACPTranslator.NumAllelesInCallSet];
        for (int i = 0; i < tableToPrint.length; i++) {
            IOUtils.writeToBufferedWriter(out, "Chromosome: " + (i + 1), true);
            for (int j = 0; j < tableToPrint[i].length; j++) {
                long compactUnit = tableToPrint[i][j];
                IOUtils.writeToBufferedWriter(out, "\tPosition: " + extractPositionFromCompactForm(compactUnit, !orderedByPosition), false);
                IOUtils.writeToBufferedWriter(out, "\tNucleotides: " + getStringForBytes(extractNucleotideIDsFromCompactForm(compactUnit, nucs)), false);
                IOUtils.writeToBufferedWriter(out, "\tRS ID: " + extractRsIdFromCompactForm(compactUnit, !orderedByPosition), false);
                IOUtils.writeToBufferedWriter(out, "\tBitString: " + Long.toBinaryString(compactUnit), false);
                IOUtils.writeToBufferedWriter(out, "", true);
            }
            IOUtils.writeToBufferedWriter(out, "", true);
        }
    }

    /** Given a chromosome number, position, rsId, two nucleotides, and the
	 *  strand, all of which represent a SNP, this stores the SNP in memory.
	 */
    public void registerSNP(byte chromNum, int position, int rsId, char nuc1, char nuc2, boolean strand) {
        CompareUtils.ensureTrue(rsId >= 0, "ERROR: AffySNPMap.registerSNP(): rsId cannot be < 0!");
        byte nucleotide1 = strand ? (byte) getIntegerForNucleotide(nuc1) : (byte) getComplementForNucleotideCharAsInt(nuc1);
        byte nucleotide2 = strand ? (byte) getIntegerForNucleotide(nuc2) : (byte) getComplementForNucleotideCharAsInt(nuc2);
        int i = chromNum - 1;
        mSNPs[i][mNumSNPs[i]] = compactSNPInfo(position, rsId, nucleotide1, nucleotide2, false);
        mSNPsByRsId[i][mNumSNPs[i]] = compactSNPInfo(position, rsId, nucleotide1, nucleotide2, true);
        mNumSNPs[i]++;
        if (mNumSNPs[i] == mSNPs[i].length) {
            Arrays.sort(mSNPs[i]);
            Arrays.sort(mSNPsByRsId[i]);
        }
    }

    /** Given a chromosome number, position, rsId, two nucleotides, and the
	 *  indicator of whether it is Illumina Top or Bottom -- all of which 
	 *  represent a SNP, this stores the SNP in memory.
	 *  TODO - UNFINISHED FUNCTION
	 */
    public void registerSNPinIllumina(byte chromNum, int position, int rsId, char nuc1, char nuc2, boolean isIlluminaTop) {
        if (isIlluminaTop) {
            CompareUtils.ensureTrue(nuc1 == 'A', "ERROR: AffySNPMap.registerSNP(): nuc1 is " + nuc1 + "instead of A");
            CompareUtils.ensureTrue((nuc2 == 'C' || nuc2 == 'G'), "ERROR: AffySNPMap.registerSNP(): nuc2 is " + nuc2 + "instead of C or G");
        } else {
            CompareUtils.ensureTrue(nuc2 == 'T', "ERROR: AffySNPMap.registerSNP(): nuc2 is " + nuc2 + "instead of T");
            CompareUtils.ensureTrue((nuc1 == 'C' || nuc1 == 'G'), "ERROR: AffySNPMap.registerSNP(): nuc1 is " + nuc1 + "instead of C or G");
        }
    }

    /** Compacts the information into a single unit. */
    private static long compactSNPInfo(int position, int rsId, byte nuc1, byte nuc2, boolean flipPositionAndRsId) {
        int bitsToShiftForPosition = flipPositionAndRsId ? 6 : 33;
        int bitsToShiftForRsId = flipPositionAndRsId ? 37 : 6;
        long rV = (long) 0 | (long) ((((long) position) & 0x7FFFFFFF) << bitsToShiftForPosition) | (long) ((((long) rsId) & 0x07FFFFFF) << bitsToShiftForRsId) | (long) ((nuc1 & 0x07) << 3) | (long) (nuc2 & 0x07);
        return rV;
    }

    /** Extracts the position from the compact form. */
    private static int extractPositionFromCompactForm(long compactUnit, boolean positionAndRsIdAreFlipped) {
        return ((int) (compactUnit >> (positionAndRsIdAreFlipped ? 6 : 33))) & 0x7FFFFFFF;
    }

    /** Extracts the RS ID from the compact form. */
    private static int extractRsIdFromCompactForm(long compactUnit, boolean positionAndRsIdAreFlipped) {
        return ((int) (compactUnit >> (positionAndRsIdAreFlipped ? 37 : 6))) & 0x07FFFFFF;
    }

    /** Extracts the nucleotide IDs as integers from the compact form. */
    private static byte[] extractNucleotideIDsFromCompactForm(long compactUnit, byte[] nucleotideAlleles) {
        for (int i = CopyNumberACPTranslator.NumAllelesInCallSet - 1; i >= 0; i--) {
            nucleotideAlleles[i] = (byte) (compactUnit & 0x07);
            compactUnit = (long) (compactUnit >> 3);
        }
        return nucleotideAlleles;
    }

    private long[] getArrayForChrom(byte chromNum, boolean orderedByPosition) {
        return (orderedByPosition ? mSNPs[chromNum - 1] : mSNPsByRsId[chromNum - 1]);
    }

    /** Retrieves the nucleotides as byte variables for a particular chromosome and position. */
    public byte[] getNucleotidesInMap(byte chromNum, int position) {
        byte[] rV = new byte[CopyNumberACPTranslator.NumAllelesInCallSet];
        return getNucleotidesInMap(chromNum, position, rV);
    }

    /** Retrieves the nucleotides as byte variables for a particular chromosome and position. */
    public byte[] getNucleotidesInMap(byte chromNum, int position, byte[] nucleotideAlleles) {
        long[] arrayForChrom = getArrayForChrom(chromNum, true);
        int resultIndex = binarySearchValue(position, arrayForChrom, arrayForChrom.length, true);
        if (resultIndex >= 0) {
            return extractNucleotideIDsFromCompactForm(arrayForChrom[resultIndex], nucleotideAlleles);
        } else {
            return null;
        }
    }

    /** Retrives the nucleotides as byte variables for a particular chromosome and rsID number. 
	 *  This runs in linear time. */
    public byte[] getNucleotidesInMapByRsID(final byte chromNum, final int rsId) {
        byte[] rV = new byte[CopyNumberACPTranslator.NumAllelesInCallSet];
        return getNucleotidesInMapByRsID(chromNum, rsId, rV);
    }

    public byte[] getNucleotidesInMapByRsID(final byte chromNum, final int rsId, byte[] nucleotideAlleles) {
        int resultIndex = getIndexOfRsIdInMapHelper(chromNum, rsId);
        if (resultIndex >= 0) {
            long[] arrayForChrom = getArrayForChrom(chromNum, false);
            if (rsId == 34408665) {
                int position = extractPositionFromCompactForm(arrayForChrom[resultIndex], true);
                System.out.println("position = " + position);
            }
            return extractNucleotideIDsFromCompactForm(arrayForChrom[resultIndex], nucleotideAlleles);
        } else {
            if (rsId == 34408665) {
                System.out.println("Couldn't find!!");
            }
            return null;
        }
    }

    public byte[] getNucleotidesInMapByRsID(final int rsId, byte[] nucleotideAlleles) {
        int numChroms = mNumSNPs.length;
        for (byte chromNum = 1; chromNum <= numChroms; chromNum++) {
            byte[] result = getNucleotidesInMapByRsID(chromNum, rsId, nucleotideAlleles);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /** First tries to get the bytes by rsID.  If that doesn't work, then it tries for
	 *  the position
	 */
    public byte[] getNucleotidesInMapByRsIDOrPosition(final byte chromNum, final int rsId, final int position, byte[] nucleotideAlleles) {
        byte[] result = getNucleotidesInMapByRsID(chromNum, rsId, nucleotideAlleles);
        if (result == null) {
            result = getNucleotidesInMap(chromNum, position, nucleotideAlleles);
        }
        return result;
    }

    public int getIndexOfPositionInMap(byte chromNum, int position) {
        long[] arrayForChrom = getArrayForChrom(chromNum, true);
        return binarySearchValue(position, arrayForChrom, arrayForChrom.length, true);
    }

    public int getIndexOfRsIdInMap(final byte chromNum, final int rsId) {
        int resultIndex = getIndexOfRsIdInMapHelper(chromNum, rsId);
        if (resultIndex >= 0) {
            long compactUnit = getArrayForChrom(chromNum, false)[resultIndex];
            int position = extractPositionFromCompactForm(compactUnit, true);
            return getIndexOfPositionInMap(chromNum, position);
        }
        return resultIndex;
    }

    private int getIndexOfRsIdInMapHelper(final byte chromNum, final int rsId) {
        long[] arrayForChrom = getArrayForChrom(chromNum, false);
        return binarySearchValue(rsId, arrayForChrom, arrayForChrom.length, false);
    }

    public int[] getPositionsBetweenPositionsInMap(byte chromNum, int positionStart, int positionEnd, boolean posOrRsID) {
        int indexStart = getIndexOfPositionInMap(chromNum, positionStart);
        int indexEnd = getIndexOfPositionInMap(chromNum, positionEnd);
        if ((indexStart >= 0) && (indexEnd >= indexStart)) {
            long[] arrayForChrom = getArrayForChrom(chromNum, true);
            int numPositions = indexEnd - indexStart + 1;
            int[] positions = new int[numPositions];
            for (int i = indexStart; i <= indexEnd; i++) {
                if (posOrRsID) {
                    positions[i - indexStart] = extractPositionFromCompactForm(arrayForChrom[i], false);
                } else {
                    positions[i - indexStart] = extractRsIdFromCompactForm(arrayForChrom[i], false);
                }
            }
            return positions;
        } else {
            return null;
        }
    }

    /** Retrieves the rsId as an integer for a particular chromosome and position. */
    public int getRsIdInMap(byte chromNum, int position) {
        long[] arrayForChrom = getArrayForChrom(chromNum, true);
        int resultIndex = binarySearchValue(position, arrayForChrom, arrayForChrom.length, true);
        if (resultIndex >= 0) {
            return extractRsIdFromCompactForm(arrayForChrom[resultIndex], false);
        } else {
            return -1;
        }
    }

    public static int getIntegerForNucleotide(char nucleotide) {
        for (int i = 0; i < NucleotideMap.length; i++) {
            if (nucleotide == NucleotideMap[i]) {
                return i;
            }
        }
        return 0;
    }

    public static char getComplementForNucleotideCharAsChar(char nuc) {
        return getNucleotideForInteger(getComplementForNucleotideCharAsInt(nuc));
    }

    public static int getComplementForNucleotideCharAsInt(char nuc) {
        return getComplementForNucleotideIntAsInt(getIntegerForNucleotide(nuc));
    }

    public static int getComplementForNucleotideIntAsInt(int nuc) {
        return (NucleotideMap.length - nuc);
    }

    public static char getNucleotideForInteger(int nucleotideInt) {
        return NucleotideMap[nucleotideInt];
    }

    public static char[] getNucleotideForIntegerAll(byte[] nucs, char[] nucsChar) {
        for (int i = 0; i < nucs.length; i++) {
            nucsChar[i] = AffySNPMap.getNucleotideForInteger(nucs[i]);
        }
        return nucsChar;
    }

    /** The purpose of this method is to conserve memory.  The strings that are 
	 *  returned are string literals stored in a static part of memory.
	 */
    public static String getStringForNucleotide(char nucChar) {
        switch(nucChar) {
            case 'A':
                return "A";
            case 'C':
                return "C";
            case 'G':
                return "G";
            case 'T':
                return "T";
            default:
                return "N";
        }
    }

    /** This method compares the allele-pairs at a SNP between two platforms.  The
	 *  first argument is a byte[] that represents the allele-pair from the first 
	 *  platform, and the second byte[] argument represents the allele-pair from
	 *  the second platform.  The third byte[] argument result[] is provided by the 
	 *  caller as an output buffer; its length should be 2.  
	 *  
	 *  This method compares the two allele-pairs to test whether they are the same.
	 *  It goes even as far as to take the complement of the alleles in the second
	 *  pair or flip them if need be.  If it takes the complement, then result[0] is
	 *  set to true.  If it flips them, then result[1] is set to true.  Otherwise,
	 *  the result[] array values are set to false.  
	 *  
	 *  The method returns true if the if the allele-pairs are the same, false otherwise.
	 */
    public static boolean compareAllelePairs(final byte[] pair1, byte[] pair2, boolean[] result) {
        Arrays.fill(result, false);
        if (Arrays.equals(pair1, pair2)) {
            return true;
        } else {
            byte[] pair2Temp = new byte[pair2.length];
            ArrayUtils.arrayCopy(pair2Temp, pair2, pair2Temp.length);
            for (int i = 0; i < pair2Temp.length; i++) {
                pair2Temp[i] = (byte) getComplementForNucleotideIntAsInt(pair2Temp[i]);
            }
            if (Arrays.equals(pair1, pair2Temp)) {
                result[0] = true;
                return true;
            }
            ArrayUtils.reverseArray(pair2Temp);
            if (Arrays.equals(pair1, pair2Temp)) {
                result[0] = result[1] = true;
                return true;
            }
            ArrayUtils.arrayCopy(pair2Temp, pair2, pair2Temp.length);
            ArrayUtils.reverseArray(pair2Temp);
            if (Arrays.equals(pair1, pair2Temp)) {
                result[1] = true;
                return true;
            }
        }
        return false;
    }

    /** Given a genotype code, this fills in the characters for that genotype 
	 *  given the mapping characters.  For heterozygous cases, the first mapping
	 *  char is written first, then the second (unless flipForHet is true)
	 * @param genotypeCode - A genotype code represented by HardyWeinbergCalculator
	 * @param nucsChar - The array that the genotype chars will be written to
	 * @param mapChar - The array that houses the reference nucleotide alleles for a marker
	 * @return - false if an invalid genotype code, true otherwise
	 */
    public static boolean getGenotypeAsChars(int genotypeCode, char callChar[], final char mapChar[], boolean flipForHet) {
        switch(genotypeCode) {
            case GenotypeConstants.EnumHomozygous00:
                callChar[0] = mapChar[0];
                callChar[1] = mapChar[0];
                break;
            case GenotypeConstants.EnumHomozygous11:
                callChar[0] = mapChar[1];
                callChar[1] = mapChar[1];
                break;
            case GenotypeConstants.EnumHeterozygous:
                callChar[0] = flipForHet ? mapChar[1] : mapChar[0];
                callChar[1] = flipForHet ? mapChar[0] : mapChar[1];
                break;
            case GenotypeConstants.EnumHemizygous10:
                callChar[0] = mapChar[0];
                callChar[1] = AffySNPMap.NucleotideMap[AffySNPMap.NucleotideMapUnknownAlleleIndex];
                break;
            case GenotypeConstants.EnumHemizygous01:
                callChar[0] = mapChar[1];
                callChar[1] = AffySNPMap.NucleotideMap[AffySNPMap.NucleotideMapUnknownAlleleIndex];
                break;
            default:
                callChar[0] = AffySNPMap.NucleotideMap[AffySNPMap.NucleotideMapUnknownAlleleIndex];
                callChar[1] = AffySNPMap.NucleotideMap[AffySNPMap.NucleotideMapUnknownAlleleIndex];
                return false;
        }
        return true;
    }

    /** Checks whether the given genotype concords with the alleles in the map.  If one
	 *  of the alleles is unknown, then null is returned.  Else a boolean value is returned.
	 */
    public static Boolean testForConcordanceWithMap(char[] callChar, char[] mapChar) {
        for (int i = 0; i < callChar.length; i++) {
            Boolean result = testForConcordanceWithMap(callChar[i], mapChar);
            if (CompareUtils.isNull(result)) {
                return null;
            } else if (result == Boolean.FALSE) {
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    /** Checks whether a given char concords with one of the alleles in the map.  If the
	 *  allele is an known allele, then null is returned.  Else a boolean value is returned.
	 */
    public static Boolean testForConcordanceWithMap(char callChar, char[] mapChar) {
        if (callChar == NucleotideMap[NucleotideMapUnknownAlleleIndex]) {
            return null;
        } else {
            for (int i = 0; i < mapChar.length; i++) {
                if (callChar == mapChar[i]) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }

    /** Given a sample index and integer array, this performs a binary search
	 *  for the sample index on the integer array.  If the sample index is 
	 *  found, an index >= 0 is returned, else -1 is returned. */
    private static int binarySearchValue(final int value, long[] longArray, final int trueLength, final boolean searchByPosition) {
        int lowerIndex = 0;
        int upperIndex = trueLength - 1;
        int midIndex = 0;
        int valueAtMidIndex;
        while (lowerIndex <= upperIndex) {
            midIndex = (lowerIndex + upperIndex) / 2;
            valueAtMidIndex = searchByPosition ? extractPositionFromCompactForm(longArray[midIndex], false) : extractRsIdFromCompactForm(longArray[midIndex], true);
            if (value == valueAtMidIndex) {
                return midIndex;
            } else if (value > valueAtMidIndex) {
                lowerIndex = midIndex + 1;
            } else {
                upperIndex = midIndex - 1;
            }
        }
        return -1;
    }

    /** Given an array of bytes and a nucleotide, this returns the index of the 
	 *  nucleotide in the byte array. */
    public static int getIndexOfNucleotideInByteArray(final byte[] theArray, final char nuc) {
        for (int i = 0; i < theArray.length; i++) {
            if (nuc == getNucleotideForInteger(theArray[i])) {
                return i;
            }
        }
        return -1;
    }

    public static void TestAffySNPMapWhole() {
        String inFilename = IOUtils.pathConcat(new String[] { "..", "..", "..", "..", "TCGA", "GenomeWideSNP_6.na24.annot.csv.All2.autosomes.csv" });
        AffySNPMap asm = CopyNumberParse.parseAffySNPFileHelper(inFilename);
        byte[] nucs = asm.getNucleotidesInMapByRsID((byte) 1, 34408665);
        asm.printMe(false, "AffyMapByRsID.txt");
        asm.printMe(true, "AffyMapByPosition.txt");
    }

    public static void TestAffySNPMap() {
        int numChromUsed = 4;
        int dummyBuffer = 3;
        int[] chromCount = new int[numChromUsed + dummyBuffer];
        Arrays.fill(chromCount, 0);
        for (int i = 0; i < chromCount.length; i++) {
            chromCount[i] = i + 1;
        }
        AffySNPMap aMap = new AffySNPMap(chromCount, numChromUsed);
        char aChar = 'A';
        char cChar = 'C';
        char gChar = 'G';
        char tChar = 'T';
        aMap.registerSNP((byte) 1, 100, 10000, aChar, gChar, false);
        aMap.registerSNP((byte) 2, 200, 11000, cChar, gChar, true);
        aMap.registerSNP((byte) 2, 100, 12000, aChar, gChar, true);
        aMap.registerSNP((byte) 3, 150, 13000, cChar, gChar, true);
        aMap.registerSNP((byte) 3, 100, 14000, aChar, gChar, true);
        aMap.registerSNP((byte) 3, 200, 15000, gChar, gChar, true);
        aMap.registerSNP((byte) 4, 133, 16000, cChar, gChar, true);
        aMap.registerSNP((byte) 4, 100, 17000, aChar, gChar, true);
        aMap.registerSNP((byte) 4, 200, 18000, tChar, gChar, true);
        aMap.registerSNP((byte) 4, 166, 19000, gChar, gChar, false);
        aMap.printMe(true, "AffyMap.txt");
        System.out.println(getStringForBytes(aMap.getNucleotidesInMap((byte) 4, 166)));
        System.out.println(getStringForBytes(aMap.getNucleotidesInMap((byte) 3, 150)));
        System.out.println(getStringForBytes(aMap.getNucleotidesInMap((byte) 4, 133)));
        System.out.println(getStringForBytes(aMap.getNucleotidesInMap((byte) 2, 200)));
        System.out.println(getStringForBytes(aMap.getNucleotidesInMap((byte) 1, 100)));
        System.out.println(getStringForBytes(aMap.getNucleotidesInMap((byte) 3, 100)));
        System.out.println(getStringForBytes(aMap.getNucleotidesInMap((byte) 3, 125)));
    }

    public static String getStringForBytes(byte[] theBytes) {
        if (theBytes == null) return "null";
        StringBuilder sb = new StringBuilder(1024);
        sb.append('(');
        for (int i = 0; i < theBytes.length; i++) {
            sb.append(theBytes[i]);
            if (i < (theBytes.length - 1)) {
                sb.append(", ");
            }
        }
        sb.append(')');
        return sb.toString();
    }

    public static void TestSNPCompact() {
        int rsId = 67108863;
        int position = 2147483647;
        boolean strand = true;
        System.out.println("Strand: " + strand);
        char nucCh1 = 'A';
        char nucCh2 = 'T';
        byte nuc1 = strand ? (byte) getIntegerForNucleotide(nucCh1) : (byte) getComplementForNucleotideCharAsInt(nucCh1);
        System.out.println("Nuc1 Integer: " + nuc1);
        byte nuc2 = strand ? (byte) getIntegerForNucleotide(nucCh2) : (byte) getComplementForNucleotideCharAsInt(nucCh2);
        System.out.println("Nuc2 Integer: " + nuc2);
        long result = compactSNPInfo(position, rsId, nuc1, nuc2, false);
        System.out.println("Result = " + result);
        System.out.println("BinString = " + Long.toBinaryString(result));
        System.out.println("BinString Length = " + Long.toBinaryString(result).length());
        System.out.println("Extracted position = " + extractPositionFromCompactForm(result, false));
        System.out.println("Extracted RS ID = " + extractRsIdFromCompactForm(result, false));
        byte[] nucs = new byte[CopyNumberACPTranslator.NumAllelesInCallSet];
        extractNucleotideIDsFromCompactForm(result, nucs);
        System.out.println("Extracted Nuc1, Nuc2: " + getStringForBytes(nucs));
    }

    public static void TestCompareAllelePairs() {
        byte[] pair1 = new byte[] { 1, 3 };
        byte[] pair2 = new byte[] { 1, 3 };
        boolean[] result = new boolean[pair2.length];
        boolean testResult = AffySNPMap.compareAllelePairs(pair1, pair2, result);
        System.out.println(testResult + "\tComp:" + result[0] + "\tFlip:" + result[1]);
    }

    public static void TestCompareAllelePairsAffyAndIllumina() {
        String affyInFilename = "GenomeWideSNP_6.na24.annot.csv.All2.autosomes.csv";
        String illuminaInFilename = "Illumina_Hap550.bpm.edited.autosomes.strandFixed.csv";
        String commonRsIdInFilename = "affy.gbm.common.illumina550K.fromAscnCompare.rsIdList.txt";
        byte[] pairAffy = new byte[2];
        byte[] pairIllumina = new byte[pairAffy.length];
        boolean[] result = new boolean[pairAffy.length];
        ArrayList<String> rsIdList = IOUtils.readAllLinesFromFile(commonRsIdInFilename);
        AffySNPMap asmAffy = CopyNumberParse.parseAffySNPFileHelper(affyInFilename);
        AffySNPMap asmIllumina = CopyNumberParse.parseAffySNPFileHelper(illuminaInFilename);
        for (Iterator<String> iter = rsIdList.iterator(); iter.hasNext(); ) {
            int rsId = GenotypeUtils.getNumberFromRsId(iter.next());
            byte[] resultAffy = asmAffy.getNucleotidesInMapByRsID(rsId, pairAffy);
            byte[] resultIllumina = asmIllumina.getNucleotidesInMapByRsID(rsId, pairIllumina);
            CompareUtils.ensureTrue(resultAffy != null, "ERROR: RsID does not exist in Affy!: " + rsId);
            CompareUtils.ensureTrue(resultIllumina != null, "ERROR: RsID does not exist in Illumina!: " + rsId);
            boolean compResult = compareAllelePairs(pairAffy, pairIllumina, result);
            if (!compResult) {
                String errString = "ERROR: SNPs do not match between platforms!\trs" + rsId + "Affy: {" + pairAffy[0] + "," + pairAffy[1] + "}\tIllumina: {" + pairIllumina[0] + "," + pairIllumina[1] + "}";
                CompareUtils.ensureTrue(false, errString);
            } else {
                String outString = "Match:\trs" + rsId + "\tResult: {" + result[0] + "," + result[1] + "}";
                System.out.println(outString);
            }
        }
        System.out.println("All matched!");
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        TestCompareAllelePairs();
    }
}
