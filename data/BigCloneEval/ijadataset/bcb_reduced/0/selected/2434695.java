package genomemap.provider.impl.organismdata;

import commons.util.ArrayUtil;
import commons.util.PropertyUtil;
import genomemap.data.OrganismData;
import genomemap.provider.ProviderException;
import genomemap.provider.ProviderManager;
import genomemap.provider.OrganismDataProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * <p>Implements OrganismData and its driver OrganismDataProvider.</p>
 *
 * Requires a directory that contains all the necessary read-only files. The following is a list
 * of the required files with their brief description:
 * <ul>
 *  <li>
 *    genotypes.xml: contains genes and their genotypes on a number of offsprings for all the
 *    linkage groups under study. The same offsprings used for all the linkage groups. Genotype
 *    contains missing values. A deterministic (i.e. reproducible) missing value algorithm is used
 *    for imputation.
 *  </li>
 *  <li>
 *    ch_[LINKAGE-GROUP].cosmid: contains hybridization data for clones and probes. Clone names are
 *    included but probe names are given in a separate file. Some of these probes are not suitable
 *    for analysis as they violate model conditions, e.g., non-overlapping, self-hybridizing, unique etc. An
 *    algorithm is used to prune these probes to select a subset of probes suitable for analysis.
 *    These are big files, size being about 1 MB. Linkage groups 5 and 1 are the biggest and linkage
 *    group 7 being the smallest.
 *  </li>
 *  <li>
 *    probe_names.xml: contains probe names for probes in ch_[LINKAGE-GROUP].cosmid
 *  </li>
 *  <li>
 *    assignment.xml: contains gene-clone assignment information for genes in genotypes.xml and clones
 *    in probe_names.xml in all linkage groups under study. Assignment indicates a region of overlap
 *    between the gene and clone in question. All the information has been manually (assisted by a
 *    GROOVY script) extracted from the website:  http://www.broadinstitute.org/annotation/genome/neurospora/FeatureSearch.html.
 *    This assignment relation is Many-To-Many. Note that some probes present in asignment may get
 *    pruned and may not enter into the final analysis. The pruning algorithm takes this into account
 *    and tries to keep probes found in the assignment, whenever possible, without violating pruning conditions.
 *    
 *    <br>The assignment information is used to update maps based on borrowed information from other maps.
 *    For example, probe based physical maps can be updated with information from genetic maps and
 *    <i>vice versa</i>. The many-to-many relation must be reduced to a one-to-one before this updation
 *    is possible. But this cannot be done a-priori and must be delath with case by case. That is, we
 *    wil need an assignment algorithm that will use the many-to many relation to maximum benefit (most
 *    number of assignments) to update a map.
 *    This
 *  </li>
 * </ul>
 *
 * It uses the URL protocol: impl1//[DATA-DIRECTORY], where 'impl1' acts as a marker tag for this
 * implementation.
 *
 * <p>
 * Every connection creates a new instance of Inputdata. Since, Inputdata is immutable, clients are
 * expected to manage a single instance of this class unless a different instance is desired either
 * for different url or properties info.
 * 
 * @version 1.0 Aug 20, 2010
 * @author Susanta Tewari
 */
public class OrganismDataProviderImpl implements genomemap.provider.OrganismDataProvider {

    /**
     *
     * @param url the URL string according to the protocol specified in the class javadoc
     * @return true if the url is by the specification and contains the string 'impl1' at right
     *         place; false otherwise
     *         @todo better algo: if the url contains the 'impl1' in the left substring of the split
     *         by "//".
     */
    public boolean acceptsURL(String url) {
        if (!url.contains("impl1")) return false;
        String[] parts = url.split("//");
        if (parts.length != 2) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Invalid URL: " + url);
            return false;
        }
        if (!new File(parts[1]).exists()) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Invalid URL: " + url + ". " + "Directory " + parts[1] + " does not exist.");
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * By contract, this method is invoked  by a {@link ProviderManager#getInputData(java.lang.String, java.util.Properties)
     * driver manager} iff {@link #acceptsURL(java.lang.String) acceptsURL} returns <code>true</code>.
     * Nonetheless, this method defensively calls acceptsURL again.
     *
     * <p>
     * Creates a new instance of OrganismData for the specified arguments.
     * 
     * @param url the URL string according to the protocol specified in the class javadoc
     * @throws ProviderException If,
     * <ul>
     *  <li>
     *   <code>url</code> is invalid as specified by the class javadoc of this class.
     *  </li>
     * <li>
     *  Invalid property key {@link OrganismDataProvider#LINKAGE_GROUPS_PROP LINKAGE_GROUPS_PROP}
     * </li>
     * </ul>
     */
    public OrganismData connect(String url, Properties info) throws ProviderException {
        if (!acceptsURL(url)) throw new ProviderException("Invalid URL: " + url);
        try {
            PropertyUtil.validateProperty(OrganismDataProvider.LINKAGE_GROUPS_PROP, info.getProperty(OrganismDataProvider.LINKAGE_GROUPS_PROP));
        } catch (RuntimeException ex) {
            throw new ProviderException("Missing/empty input data property: " + OrganismDataProvider.LINKAGE_GROUPS_PROP, ex);
        }
        Set<Integer> linkageGroups = new LinkedHashSet<Integer>();
        String propValue = info.getProperty(OrganismDataProvider.LINKAGE_GROUPS_PROP);
        for (String value : propValue.split(",")) {
            try {
                linkageGroups.add(Integer.valueOf(value));
            } catch (NumberFormatException ex) {
                throw new ProviderException("Input data property: " + OrganismDataProvider.LINKAGE_GROUPS_PROP + " value: " + propValue);
            }
        }
        File filesDir = new File(url.split("//")[1]);
        return new InputDataImpl1(linkageGroups, filesDir);
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

        public Set<Integer> getLinkageGroups() {
            return linkageGroups;
        }

        public Integer getHydDataCloneLength() {
            return Integer.valueOf("34");
        }

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
        public byte[][] genotypeData(Integer linkageGroup, List<String> genes) {
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
            return result;
        }

        public List<String> clones(Integer linkageGroup) {
            check_argument_linkage_group(linkageGroup);
            if (clonesCache.get(linkageGroup) == null) {
                Parser parser = new Parser(linkageGroup, filesDir);
                clonesCache.put(linkageGroup, Collections.unmodifiableList(parser.clones()));
            }
            return clonesCache.get(linkageGroup);
        }

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
        public byte[][] hybridizationData(Integer linkageGroup, List<String> probes) {
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
            return result;
        }

        public byte hybridizationScore(Integer linkageGroup, String clone, String probe) {
            check_argument_linkage_group(linkageGroup);
            int cloneIndex = clones(linkageGroup).indexOf(clone);
            if (cloneIndex == -1) throw new IllegalArgumentException("Clone " + clone + " is unavailable");
            int probeIndex = probes(linkageGroup).indexOf(probe);
            if (probeIndex == -1) throw new IllegalArgumentException("Probe " + probe + " is unavailable");
            byte[][] data = hybridizationData(linkageGroup, probes(linkageGroup));
            return data[cloneIndex][probeIndex];
        }

        private void check_argument_linkage_group(Integer linkageGroup) {
            if (!getLinkageGroups().contains(linkageGroup)) throw new IllegalArgumentException("Linkage group " + linkageGroup + " is unavailable");
        }
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
    }
}
