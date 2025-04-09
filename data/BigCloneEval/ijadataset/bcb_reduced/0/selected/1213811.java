package genomemap.data.provider.impl.organismdata;

import commons.provider.DefaultBaseProvider;
import commons.provider.ProviderException;
import commons.util.ArrayUtil;
import genomemap.data.OrganismData;
import genomemap.data.provider.OrganismDataProvider;
import genomemap.provider.impl.XMLUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import la.data.KSA00Data;
import la.data.TAB08Data;
import la.data.provider.KSA00DataProvider;
import la.data.provider.TAB08DataProvider;
import la.model.KSA00;
import la.model.TAB08;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @since Jul 8, 2011
 * @author Susanta Tewari
 */
public class OrganismDataProviderImpl extends DefaultBaseProvider<OrganismData> implements OrganismDataProvider {

    private File dataDir;

    private Validator validator;

    private Set<Integer> linkageGroups;

    @Override
    public void setDataDirectory(File dataDir) throws ProviderException {
        if (!dataDir.isDirectory()) reportError(5, dataDir);
        if (this.dataDir == null || !this.dataDir.equals(dataDir)) {
            this.dataDir = dataDir;
            Validator local_validator = new Validator(dataDir);
            local_validator.validate();
            this.validator = local_validator;
        }
    }

    @Override
    public Set<Integer> getAvailableLinkageGroups() throws ProviderException {
        if (validator == null) reportError(10);
        return validator.getAvailableLinkageGroups();
    }

    @Override
    public void setLinkageGroups(Set<Integer> linkageGroups) throws ProviderException {
        Set<Integer> available_linkage_groups = validator.getAvailableLinkageGroups();
        if (!available_linkage_groups.containsAll(linkageGroups)) reportError(20, linkageGroups, available_linkage_groups);
        this.linkageGroups = linkageGroups;
    }

    @Override
    public OrganismData create() throws ProviderException {
        return new InputDataImpl1(linkageGroups, dataDir);
    }

    @Override
    public TAB08DataProvider getTAB08DataProvider(Integer linkageGroup) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public KSA00DataProvider getKSA00DataProvider(Integer linkageGroup) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * An implementation of OrganismData supported by the parent class. It uses flat files specified by the
     * parent class to create such an instance. It manages a delegate for parsing, implements the
     * cache necessary to support frequent use of this instance and supports the read-only behaviour
     * specified by the interface.
     *
     */
    private class InputDataImpl1 implements OrganismData {

        final Set<Integer> linkageGroups;

        final File filesDir;

        private Map<Integer, List<String>> genesCache = new HashMap<Integer, List<String>>();

        private Map<Integer, Object> genotypeDataCache = new HashMap<Integer, Object>();

        private Map<Integer, List<String>> clonesCache = new HashMap<Integer, List<String>>();

        private Map<Integer, Object> hybridizationDataCache = new HashMap<Integer, Object>();

        private Map<Integer, List<String>> probesCache = new HashMap<Integer, List<String>>();

        private GenotypeMissingValAlgo missingValAlgo = GenotypeMissingValAlgo.getNearestScoreAlgo();

        private ProbesPruningAlgo pruningAlgo = ProbesPruningAlgo.getDefaultAlgo();

        private Map<Integer, GenomeInfo> genomeInfoCache = new HashMap<Integer, GenomeInfo>();

        /**
         * Upon successfully creating an instance clients can be reasonablly confident that further
         * exceptions (runtime) while excuting the interface methods are indicative of bugs (programming
         * errors) than modeled exceptions (invariant or contract violations).
         *
         * <p>
         * Checks are performed (for the specified linkage groups only) to ensure that
         * <ol>
         *  <li> necessary files are present</li>
         *  <li> files have the correct format, any validation checks</li>
         *  <li> data invariants are preserved</li>
         * </ol>
         *
         * @param linkageGroups linkage groups to be supported by the created instance
         * @param filesDir directory containing the specified files, used by a parser delegate
         * @throws ProviderException If any of the checks mentioned above fails
         */
        public InputDataImpl1(Set<Integer> linkageGroups, File filesDir) throws ProviderException {
            Parser.checkPresenceOfNecessaryFiles(linkageGroups, filesDir);
            Parser.checkFormatOfNecessaryFiles(linkageGroups, filesDir);
            Parser.checkDataInvariantsOfNecessaryFiles(linkageGroups, filesDir);
            this.linkageGroups = Collections.unmodifiableSet(linkageGroups);
            this.filesDir = filesDir;
        }

        @Override
        public Set<Integer> getLinkageGroups() {
            return linkageGroups;
        }

        @Override
        public List<String> genes(Integer linkageGroup) {
            check_argument_linkage_group(linkageGroup);
            if (genesCache.get(linkageGroup) == null) {
                Parser parser = new Parser(linkageGroup, filesDir);
                genesCache.put(linkageGroup, Collections.unmodifiableList(parser.genes()));
            }
            return genesCache.get(linkageGroup);
        }

        /**
         * Caching is done on the full genotype data, not on data for <code>genes</code>.
         * @return newly created array for the specified <code>genes</code> is returned.
         */
        @Override
        public TAB08Data createTAB08Data(Integer linkageGroup, List<String> genes) {
            check_argument_linkage_group(linkageGroup);
            if (!genes(linkageGroup).containsAll(genes)) throw new IllegalArgumentException("Some genes in " + genes + " are unavailable");
            if (genotypeDataCache.get(linkageGroup) == null) {
                Parser parser = new Parser(linkageGroup, filesDir);
                genotypeDataCache.put(linkageGroup, missingValAlgo.apply(parser.genotypeData()));
            }
            byte[][] fullData = (byte[][]) genotypeDataCache.get(linkageGroup);
            int[] indices = ArrayUtil.indicesOf(genes(linkageGroup), genes);
            if (indices == null) throw new IllegalArgumentException("Some genes in " + genes + " are unavailable");
            byte[][] result = new byte[fullData.length][indices.length];
            for (int i = 0; i < result.length; i++) for (int j = 0; j < result[0].length; j++) result[i][j] = fullData[i][indices[j]];
            return new TAB08Data(new TAB08(genes), result);
        }

        @Override
        public List<String> clones(Integer linkageGroup) {
            check_argument_linkage_group(linkageGroup);
            if (clonesCache.get(linkageGroup) == null) {
                Parser parser = new Parser(linkageGroup, filesDir);
                clonesCache.put(linkageGroup, Collections.unmodifiableList(parser.clones()));
            }
            return clonesCache.get(linkageGroup);
        }

        @Override
        public List<String> probes(Integer linkageGroup) {
            check_argument_linkage_group(linkageGroup);
            if (probesCache.get(linkageGroup) == null) {
                Parser parser = new Parser(linkageGroup, filesDir);
                List<String> fullProbes = parser.probes();
                Set<Integer> removedIndices = pruningAlgo.removedIndices(parser.hybridizationData(), parser.clones(), fullProbes);
                List<String> result = new ArrayList<String>();
                for (int i = 0; i < fullProbes.size(); i++) {
                    if (!removedIndices.contains(i)) result.add(fullProbes.get(i));
                }
                probesCache.put(linkageGroup, Collections.unmodifiableList(result));
            }
            return probesCache.get(linkageGroup);
        }

        /**
         * Caching is done on the full hybridization data (after correcting for pruned probes). It is
         * crucial that the puning algorithm used is deterministic, else the data returned may not
         * correctly correspond to the <code>probes</code> in the argument or a
         * <code>RuntimeException</code> may result.
         *
         * @return newly created array for the specified <code>probes</code>
         */
        @Override
        public KSA00Data createKSA00Data(Integer linkageGroup, List<String> probes) {
            check_argument_linkage_group(linkageGroup);
            if (hybridizationDataCache.get(linkageGroup) == null) {
                Parser parser = new Parser(linkageGroup, filesDir);
                byte[][] fullData = parser.hybridizationData();
                Set<Integer> removedIndices = pruningAlgo.removedIndices(parser.hybridizationData(), parser.clones(), parser.probes());
                byte[][] result = new byte[fullData.length][fullData[0].length - removedIndices.size()];
                for (int i = 0; i < fullData.length; i++) {
                    int counter = 0;
                    for (int j = 0; j < fullData[0].length; j++) {
                        if (!removedIndices.contains(j)) {
                            result[i][counter] = fullData[i][j];
                            counter++;
                        }
                    }
                }
                hybridizationDataCache.put(linkageGroup, result);
            }
            byte[][] fullData = (byte[][]) hybridizationDataCache.get(linkageGroup);
            int[] indices = ArrayUtil.indicesOf(probes(linkageGroup), probes);
            if (indices == null) throw new IllegalArgumentException("Some probes in " + probes + " are unavailable");
            byte[][] result = new byte[fullData.length][indices.length];
            for (int i = 0; i < result.length; i++) for (int j = 0; j < result[0].length; j++) result[i][j] = fullData[i][indices[j]];
            if (genomeInfoCache.get(linkageGroup) == null) {
                Parser parser = new Parser(linkageGroup, filesDir);
                genomeInfoCache.put(linkageGroup, parser.getGenomeInfo());
            }
            GenomeInfo genomeInfo = genomeInfoCache.get(linkageGroup);
            KSA00 m = new KSA00(probes, genomeInfo.chLength, genomeInfo.cloneLength, genomeInfo.falsePostiveProb, genomeInfo.falseNegativeProb);
            return new KSA00Data(m, clones(linkageGroup), result);
        }

        private void check_argument_linkage_group(Integer linkageGroup) {
            if (!getLinkageGroups().contains(linkageGroup)) throw new IllegalArgumentException("Linkage group " + linkageGroup + " is unavailable");
        }
    }

    private static class Validator {

        private final File filesDir;

        private Set<Integer> linkageGroups;

        public Validator(File filesDir) throws ProviderException {
            this.filesDir = filesDir;
            validate();
        }

        /**
         * validates the data directory and also finds the available linkage groups
         * @throws ProviderException
         */
        private void validate() throws ProviderException {
            linkageGroups = Parser.checkAndReturnLinkageGroups(filesDir);
            Parser.checkall(linkageGroups, filesDir);
        }

        Set<Integer> getAvailableLinkageGroups() {
            return linkageGroups;
        }
    }

    private static class GenomeInfo {

        int chLength;

        int cloneLength;

        double falsePostiveProb;

        double falseNegativeProb;
    }

    /**
     * Parses all the necessary files and provides the parent class with data objects.
     *
     * @version 1.0 Nov 6, 2010
     * @author Susanta Tewari
     */
    private static class Parser {

        private static final String FILE_GENOTYPES = "genotypes.xml";

        private static final String FILE_ASSIGNMENT = "assignment.xml";

        private static final String FILE_PROBES = "probe_names.xml";

        private static final String FILE_GENOME = "genome.xml";

        static void checkall(Set<Integer> linkageGroups, File filesDir) throws ProviderException {
            checkPresenceOfNecessaryFiles(linkageGroups, filesDir);
            checkFormatOfNecessaryFiles(linkageGroups, filesDir);
            checkDataInvariantsOfNecessaryFiles(linkageGroups, filesDir);
        }

        static Set<Integer> checkAndReturnLinkageGroups(File filesDir) throws ProviderException {
            Set<Integer> fromGenotypes = getLinkageGroups(filesDir, FILE_GENOME);
            Set<Integer> fromProbes = getLinkageGroups(filesDir, FILE_PROBES);
            if (!fromGenotypes.equals(fromProbes)) throw new ProviderException("mismathcing linkage groups");
            Set<Integer> fromGenome = getLinkageGroups(filesDir, FILE_GENOME);
            if (!fromGenotypes.equals(fromGenome)) throw new ProviderException("mismathcing linkage groups");
            return fromGenotypes;
        }

        private static Set<Integer> getLinkageGroups(File filesDir, String fileName) {
            Set<Integer> result = new HashSet<Integer>();
            File genotypesFile = new File(filesDir, fileName);
            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(genotypesFile);
            } catch (DocumentException ex) {
                throw new RuntimeException(ex);
            }
            Element root = document.getRootElement();
            for (Object obj : root.elements("ch")) {
                Element element = (Element) obj;
                Integer parsedLinkagGroup = Integer.valueOf(element.attribute("id").getText());
                result.add(parsedLinkagGroup);
            }
            return result;
        }

        /**
         * Checks if the necessary files for the specified linkage groups are present in the given
         * directory.
         *
         * @linkageGroups linkage groups for which the necessary files are checked
         * @param filesDir directory containing all the necessary files.
         * @throws ProviderException if any of the necessary files for the specified linkage
         *         groups is missing
         */
        static void checkPresenceOfNecessaryFiles(Set<Integer> linkageGroups, File filesDir) throws ProviderException {
            List<String> files = Arrays.asList(filesDir.list());
            if (!files.contains(FILE_ASSIGNMENT)) throw new ProviderException("File " + FILE_ASSIGNMENT + " is missing");
            if (!files.contains(FILE_GENOTYPES)) throw new ProviderException("File " + FILE_GENOTYPES + " is missing");
            if (!files.contains(FILE_PROBES)) throw new ProviderException("File " + FILE_PROBES + " is missing");
            if (!files.contains(FILE_GENOME)) throw new ProviderException("File " + FILE_GENOME + " is missing");
            for (Integer linkageGroup : linkageGroups) {
                String file = getHybridizationFile(linkageGroup);
                if (!files.contains(file)) throw new ProviderException("File " + file + " is missing");
            }
        }

        /**
         * file name: ch_<linkageGroup>.cosmid
         */
        private static String getHybridizationFile(Integer linkageGroup) {
            return "ch_" + linkageGroup + ".cosmid";
        }

        /**
         * @todo Checks format of the necessary files
         *
         * @param linkageGroups linkage groups of the necessary files of which the formats are checked
         * @param filesDir directory containing all the necessary files.
         * @throws ProviderException If any format error occurs
         */
        static void checkFormatOfNecessaryFiles(Set<Integer> linkageGroups, File filesDir) throws ProviderException {
            try {
                XMLUtil.validateSchema(new File(filesDir, FILE_GENOTYPES));
                XMLUtil.validateSchema(new File(filesDir, FILE_PROBES));
                XMLUtil.validateSchema(new File(filesDir, FILE_GENOME));
                XMLUtil.validateSchema(new File(filesDir, FILE_ASSIGNMENT));
            } catch (Exception ex) {
                throw new ProviderException(ex);
            }
        }

        /**
         * @todo Checks data invariants of the necessary files
         *
         * @param linkageGroups linkage groups of the necessary files of which the data invariants are checked
         * @param filesDir directory containing all the necessary files.
         * @throws ProviderException If any data invariance fails
         */
        static void checkDataInvariantsOfNecessaryFiles(Set<Integer> linkageGroups, File filesDir) throws ProviderException {
        }

        static Set<Integer> getAvailabaleLinkageGroups() throws ProviderException {
            return null;
        }

        private final int linkageGroup;

        private final File filesDir;

        Parser(int linkageGroup, File filesDir) {
            this.linkageGroup = linkageGroup;
            this.filesDir = filesDir;
        }

        /**
         * @return genes in genotypes.xml for this linkage group
         * @throws RuntimeException If any file reading or parsing error occurs
         */
        List<String> genes() {
            List<String> genes = new ArrayList<String>();
            String genotypeText = getGenotypeText();
            char pos18;
            for (String line : genotypeText.split("\n")) {
                line = line.trim();
                if (line.length() > 18) {
                    pos18 = line.charAt(17);
                    if (pos18 == 'M' || pos18 == 'O' || pos18 == '-') {
                        processGenes(line.substring(0, 17), genes);
                    } else {
                        processGenes(line, genes);
                    }
                } else {
                    processGenes(line, genes);
                }
            }
            return genes;
        }

        private static int MISSING_VALUE_CODE = 9;

        /**
         * Extracts all genotype scores as is from genotypes.xml for this linkage group (may contain
         * missing values coded by {@link #MISSING_VALUE_CODE MISSING_VALUE_CODE}). Rows indicate
         * samples and columns genes.
         *
         * @return all genotype scores with missing values for this linkage group.
         * @throws RuntimeException If any file reading or parsing error occurs
         */
        byte[][] genotypeData() {
            List<String> scores = new ArrayList<String>();
            String genotypeText = getGenotypeText();
            char pos18;
            String genotypeScore = null;
            for (String line : genotypeText.split("\n")) {
                line = line.trim();
                if (line.length() > 18) {
                    pos18 = line.charAt(17);
                    if (pos18 == 'M' || pos18 == 'O' || pos18 == '-') {
                        genotypeScore = line.substring(17).replace("M", "1").replace("O", "0").replace("|", " ").replace("-", "" + MISSING_VALUE_CODE);
                        processScores(line.substring(0, 17), genotypeScore, scores);
                    } else {
                        processScores(line, genotypeScore, scores);
                    }
                } else {
                    processScores(line, genotypeScore, scores);
                }
            }
            byte[][] data1 = new byte[scores.size()][];
            for (int k = 0; k < scores.size(); k++) {
                String[] vals = scores.get(k).split("\\s");
                byte[] row = new byte[vals.length];
                for (int i = 0; i < vals.length; i++) row[i] = Byte.valueOf(vals[i]).byteValue();
                data1[k] = row;
            }
            byte[][] data2 = new byte[data1[0].length][data1.length];
            for (int i = 0; i < data1.length; i++) {
                for (int j = 0; j < data1[0].length; j++) {
                    data2[j][i] = data1[i][j];
                }
            }
            return data2;
        }

        /**
         * @return clones in ch_[LINKAGE-GROUP].cosmid for this linkage group
         * @throws RuntimeException If any file reading or parsing error occurs
         */
        List<String> clones() {
            List<String> clones = new ArrayList<String>();
            File cosmidFile = new File(filesDir, getHybridizationFile(linkageGroup));
            try {
                BufferedReader reader = new BufferedReader(new FileReader(cosmidFile));
                reader.readLine();
                String line = null;
                while ((line = reader.readLine()) != null) clones.add(line.split("\\s")[0]);
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return clones;
        }

        /**
         * @return probes in probe_names.xml for this linkage group
         * @throws RuntimeException If any file reading or parsing error occurs
         */
        List<String> probes() {
            List<String> probesList = new ArrayList<String>();
            String probeNamesText = getProbeNamesText();
            for (String probe : probeNamesText.split("\\s")) {
                if (probe.length() > 0) probesList.add(probe);
            }
            return probesList;
        }

        /**
         * parses hybridization data from file {@link Parser#getHybridizationFile(java.lang.Integer)
         * Parser#getHybridizationFile} for all probes
         */
        byte[][] hybridizationData() {
            byte[][] data = null;
            File cosmidFile = new File(filesDir, "ch_" + linkageGroup + ".cosmid");
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(cosmidFile));
                String firstLine = reader.readLine();
                String[] sizes = firstLine.split("\\s");
                int probeCount = Integer.valueOf(sizes[0]);
                int cloneCount = Integer.valueOf(sizes[1]);
                String line = null;
                data = new byte[cloneCount][probeCount];
                int lineCounter = 0;
                while ((line = reader.readLine()) != null) {
                    String text = line.split("\\s")[1];
                    for (int j = 0; j < text.length(); j++) data[lineCounter][j] = Byte.valueOf("" + text.charAt(j));
                    lineCounter++;
                }
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } finally {
                try {
                    if (reader != null) reader.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return data;
        }

        private void processGenes(String line, List<String> genes) {
            for (String gene : line.split(",")) {
                gene = gene.replace(" ", "");
                if (gene.length() > 0) {
                    genes.add(gene);
                }
            }
        }

        private void processScores(String line, String genotypeScore, List<String> scores) {
            for (String gene : line.split(",")) {
                gene = gene.replace(" ", "");
                if (gene.length() > 0) {
                    scores.add(genotypeScore);
                }
            }
        }

        /**
         * Parses genotypes.xml to extract the text containing genes and their genotypes for this
         * linkage group
         *
         * @return string containing genes and their genotypes for this linkage group in genotypes.xml
         * @throws RuntimeException If any file reading or parsing error occurs
         */
        private String getGenotypeText() {
            String genotypeText = null;
            File genotypesFile = new File(filesDir, FILE_GENOTYPES);
            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(genotypesFile);
            } catch (DocumentException ex) {
                throw new RuntimeException(ex);
            }
            Element root = document.getRootElement();
            for (Object obj : root.elements("ch")) {
                Element element = (Element) obj;
                Integer parsedLinkagGroup = Integer.valueOf(element.attribute("id").getText());
                if (parsedLinkagGroup.equals(Integer.valueOf(linkageGroup))) {
                    genotypeText = element.getText();
                    break;
                }
            }
            return genotypeText;
        }

        private String getProbeNamesText() {
            String probeNamesText = null;
            File genotypesFile = new File(filesDir, FILE_PROBES);
            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(genotypesFile);
            } catch (DocumentException ex) {
                throw new RuntimeException(ex);
            }
            Element root = document.getRootElement();
            for (Object obj : root.elements("ch")) {
                Element element = (Element) obj;
                Integer parsedLinkagGroup = Integer.valueOf(element.attribute("id").getText());
                if (parsedLinkagGroup.equals(Integer.valueOf(linkageGroup))) {
                    probeNamesText = element.getText();
                    break;
                }
            }
            return probeNamesText;
        }

        private GenomeInfo getGenomeInfo() {
            File genomeFile = new File(filesDir, FILE_GENOME);
            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(genomeFile);
            } catch (DocumentException ex) {
                throw new RuntimeException(ex);
            }
            Element root = document.getRootElement();
            for (Object obj : root.elements("ch")) {
                Element element = (Element) obj;
                Integer parsedLinkagGroup = Integer.valueOf(element.attribute("id").getText());
                if (parsedLinkagGroup.equals(Integer.valueOf(linkageGroup))) {
                    GenomeInfo genomeInfo = new GenomeInfo();
                    genomeInfo.chLength = Integer.parseInt(element.elementText("chromosome-length"));
                    genomeInfo.cloneLength = Integer.parseInt(element.elementText("clone-length"));
                    genomeInfo.falsePostiveProb = Integer.parseInt(element.elementText("false-positive-prob"));
                    genomeInfo.falseNegativeProb = Integer.parseInt(element.elementText("false-negative-prob"));
                    return genomeInfo;
                }
            }
            throw new RuntimeException("Linkage group " + linkageGroup + " not found.");
        }
    }
}
