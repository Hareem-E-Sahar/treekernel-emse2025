/*
 * Copyright 2010 Susanta Tewari.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package provider.impl.genomemap.data;

import commons.provider.impl.BaseProviderImpl;
import commons.provider.ProviderException;
import commons.provider.Providers.ModelDataProvider;
import commons.util.ArrayUtil;
import genomemap.data.OrganismData;
import provider.impl.genomemap.XMLUtil;
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
import genomemap.data.KSA00Data;
import genomemap.data.TAB08Data;
import genomemap.model.KSA00;
import genomemap.model.TAB08;
import genomemap.provider.Providers.OrganismDataProvider;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * @since Jul 8, 2011
 * @author Susanta Tewari
 */
public class OrganismDataProviderImpl extends BaseProviderImpl<OrganismData>
        implements OrganismDataProvider {

    private File dataDir;
    private Validator validator;

    private Set<Integer> linkageGroups;

    private InputDataImpl1 impl1;

    @Override
    public void setDataDirectory(File dataDir) throws ProviderException{

        // check if valid dir
        if(!dataDir.isDirectory())reportError(5,dataDir);
        
        if (this.dataDir == null || !this.dataDir.equals(dataDir)) {

            this.dataDir = dataDir;
            
            Validator local_validator = new Validator(dataDir);
            local_validator.validate();
            
            this.validator = local_validator;

        }
    }

    @Override
    public Set<Integer> getAvailableLinkageGroups() throws ProviderException {
        
        // make sure setDataDirectory() was called successfully
        if (validator == null) reportError(10);
        
        return validator.getAvailableLinkageGroups();
    }

    @Override
    public void setLinkageGroups(Set<Integer> linkageGroups) throws ProviderException {

        // check specified linkage groups is the subset of the available linkage groups
        Set<Integer> available_linkage_groups = validator.getAvailableLinkageGroups();        
        if (!available_linkage_groups.containsAll(linkageGroups)) 
            reportError(20, linkageGroups, available_linkage_groups);
        
        this.linkageGroups = linkageGroups;
        
        this.impl1 = new InputDataImpl1(linkageGroups,dataDir);
    }

    @Override
    public OrganismData create() throws ProviderException {
         return impl1;
    }

    @Override
    public ModelDataProvider<TAB08,TAB08Data> getTAB08DataProvider(final Integer linkageGroup) {
        
        return new ModelDataProvider<TAB08,TAB08Data>() {

            @Override
            public Class<TAB08> getModelClass() {
                return TAB08.class;
            }

            @Override
            public Class<TAB08Data> getDataClass() {
                return TAB08Data.class;
            }

            @Override
            public String getDescription() {
                return "Model: TAB08; Data: TAB08Data; Format: organismdata";
            }

            @Override
            public String getImplCode() {
                return "tab08:tab08data:organismdata";
            }

            @Override
            public TAB08Data create() throws ProviderException {
                return impl1.createTAB08Data(linkageGroup, impl1.genes(linkageGroup));
            }
        };
    }

    @Override
    public ModelDataProvider<KSA00, KSA00Data> getKSA00DataProvider(final Integer linkageGroup) {
        
        return new ModelDataProvider<KSA00, KSA00Data>() {

            @Override
            public Class<KSA00> getModelClass() {
                return KSA00.class;
            }

            @Override
            public Class<KSA00Data> getDataClass() {
                return KSA00Data.class;
            }

            @Override
            public String getDescription() {
                return "Model: KSA00; Data: KSA00Data; Format: organismdata";
            }

            @Override
            public String getImplCode() {
                return "ksa00:ksa00data:organismdata";
            }

            @Override
            public KSA00Data create() throws ProviderException {
                return impl1.createKSA00Data(linkageGroup, impl1.probes(linkageGroup));
            }
        };
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
        private Map<Integer,List<String>> genesCache = new HashMap<>();
        private Map<Integer,Object> genotypeDataCache = new HashMap<>();
        private Map<Integer,List<String>> clonesCache = new HashMap<>();
        private Map<Integer,Object> hybridizationDataCache = new HashMap<>();
        private Map<Integer,List<String>> probesCache = new HashMap<>();

        // applies the missing value algorithm on the extracted genotype scores
        private GenotypeMissingValAlgo missingValAlgo = GenotypeMissingValAlgo.getNearestScoreAlgo();

        // probe pruning algorithm
        private ProbesPruningAlgo pruningAlgo = ProbesPruningAlgo.getDefaultAlgo();
                
        private Map<Integer,GenomeInfo> genomeInfoCache = new HashMap<>();

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
        public InputDataImpl1(Set<Integer> linkageGroups,File filesDir) throws ProviderException {

            // check presence of necessary files
            Parser.checkPresenceOfNecessaryFiles(linkageGroups, filesDir);

            // check format of necessary files
            Parser.checkFormatOfNecessaryFiles(linkageGroups, filesDir);

            // check data invariants of necessary files
            Parser.checkDataInvariantsOfNecessaryFiles(linkageGroups, filesDir);

            // init
            this.linkageGroups = Collections.unmodifiableSet(linkageGroups);

            this.filesDir = filesDir;
        }

        @Override
        public Set<Integer> getLinkageGroups() {
            return linkageGroups;
        }

        @Override
        public List<String> genes(Integer linkageGroup) {

            // illegal argument checking
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

            // illegal argument checking
            check_argument_linkage_group(linkageGroup);

            if (!genes(linkageGroup).containsAll(genes))
                throw new IllegalArgumentException("Some genes in " + genes + " are unavailable");

            // check full data cache
            if (genotypeDataCache.get(linkageGroup) == null) {

                Parser parser = new Parser(linkageGroup, filesDir);

                genotypeDataCache.put(linkageGroup, missingValAlgo.apply(parser.genotypeData()));

            }

            // full data
            byte[][] fullData = (byte[][]) genotypeDataCache.get(linkageGroup);

            // get indices for argument genes
            int[] indices = ArrayUtil.indicesOf(genes(linkageGroup), genes);
            if(indices == null)
                throw new IllegalArgumentException("Some genes in " + genes + " are unavailable");

            // apply the indices
            byte[][] result = new byte[fullData.length][indices.length];
            for (int i = 0; i < result.length; i++) {
                for (int j = 0; j < result[0].length; j++) {
                    result[i][j] = fullData[i][indices[j]];
                }
            }


            return new TAB08Data(new TAB08(genes), result);
        }

        @Override
        public List<String> clones(Integer linkageGroup) {

            // illegal argument checking
            check_argument_linkage_group(linkageGroup);

            if (clonesCache.get(linkageGroup) == null) {

                Parser parser = new Parser(linkageGroup, filesDir);

                clonesCache.put(linkageGroup, Collections.unmodifiableList(parser.clones()));

            }

            return clonesCache.get(linkageGroup);
        }

        @Override
        public List<String> probes(Integer linkageGroup) {

            // illegal argument checking
            check_argument_linkage_group(linkageGroup);

            if (probesCache.get(linkageGroup) == null) {

                Parser parser = new Parser(linkageGroup, filesDir);

                // cache only the cleaned probes
                List<String> fullProbes = parser.probes();
                Set<Integer> removedIndices = pruningAlgo.removedIndices(
                        parser.hybridizationData(),parser.clones(),fullProbes);

                List<String> result = new ArrayList<>();
                for (int i = 0; i < fullProbes.size(); i++) {
                    if(!removedIndices.contains(i)) result.add(fullProbes.get(i));

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

            // illegal argument checking
            check_argument_linkage_group(linkageGroup);

            // check full data cache
            if (hybridizationDataCache.get(linkageGroup) == null) {

                Parser parser = new Parser(linkageGroup, filesDir);

                // create data after correcting for pruned probes
                byte[][] fullData = parser.hybridizationData();
                Set<Integer> removedIndices = pruningAlgo.removedIndices(
                        parser.hybridizationData(),parser.clones(),parser.probes());

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

            // get full data
            byte[][] fullData = (byte[][]) hybridizationDataCache.get(linkageGroup);

            // get indices for argument probes
            int[] indices = ArrayUtil.indicesOf(probes(linkageGroup), probes);
            if(indices == null)
                throw new IllegalArgumentException("Some probes in " + probes + " are unavailable");

            // return new array for the specified probes
            byte[][] result = new byte[fullData.length][indices.length];
            for (int i = 0; i < result.length; i++) {
                for (int j = 0; j < result[0].length; j++) {
                    result[i][j] = fullData[i][indices[j]];
                }
            }
            
            
            // get genomeinfo
            if (genomeInfoCache.get(linkageGroup) == null) {
                
                Parser parser = new Parser(linkageGroup, filesDir);
                
                genomeInfoCache.put(linkageGroup, parser.getGenomeInfo());
                
            }
            
            GenomeInfo genomeInfo = genomeInfoCache.get(linkageGroup);            

            KSA00 m = new KSA00(probes, genomeInfo.chLength, genomeInfo.cloneLength,
                    genomeInfo.falsePostiveProb, genomeInfo.falseNegativeProb);

            return new KSA00Data(m, clones(linkageGroup), result);

        }

        private void check_argument_linkage_group(Integer linkageGroup){

            if (!getLinkageGroups().contains(linkageGroup))
                throw new IllegalArgumentException("Linkage group " + linkageGroup + " is unavailable");
        }

    }

    private static class Validator {

        private final File filesDir;

        private Set<Integer> linkageGroups;

        public Validator(File filesDir) throws ProviderException{

            this.filesDir = filesDir;

            // validate data directory and find the available linkage groups
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

        Set<Integer> getAvailableLinkageGroups(){

            return linkageGroups;
        }

    }
    
    // genome information
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
            
            if(!fromGenotypes.equals(fromProbes))
                throw new ProviderException("mismathcing linkage groups");
            
            Set<Integer> fromGenome = getLinkageGroups(filesDir, FILE_GENOME);
            
            if(!fromGenotypes.equals(fromGenome))
                throw new ProviderException("mismathcing linkage groups");
            
            return fromGenotypes;
        }
        
        private static Set<Integer> getLinkageGroups(File filesDir, String fileName){
            
            Set<Integer> result = new HashSet<>();
            
            // read the file probe_names.xml @todo close reader
            File genotypesFile = new File(filesDir, fileName);
            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(genotypesFile);
            } catch (DocumentException ex) {throw new RuntimeException(ex);}

            // obtain the probe names for the given linkage group

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

            if (!files.contains(FILE_ASSIGNMENT))
                throw new ProviderException("File " + FILE_ASSIGNMENT + " is missing");

            if (!files.contains(FILE_GENOTYPES))
                throw new ProviderException("File " + FILE_GENOTYPES + " is missing");

            if (!files.contains(FILE_PROBES))
                throw new ProviderException("File " + FILE_PROBES + " is missing");
            
            if (!files.contains(FILE_GENOME))
                throw new ProviderException("File " + FILE_GENOME + " is missing");

            for (Integer linkageGroup : linkageGroups) {

                String file = getHybridizationFile(linkageGroup);

                if (!files.contains(file))
                    throw new ProviderException("File " + file + " is missing");
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
                
                XMLUtil.validateSchema(new File(filesDir,FILE_GENOTYPES));
                XMLUtil.validateSchema(new File(filesDir,FILE_PROBES));
                XMLUtil.validateSchema(new File(filesDir,FILE_GENOME));
                XMLUtil.validateSchema(new File(filesDir,FILE_ASSIGNMENT));
                
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

            List<String> genes = new ArrayList<>();

            // get the genotype text
            String genotypeText = getGenotypeText();

            // parse the genotype text for genes
            char pos18;

            for (String line : genotypeText.split("\n")) {

                line = line.trim();

                if (line.length() > 18) {

                    pos18 = line.charAt(17);

                    if (pos18 == 'M' || pos18 == 'O' || pos18 == '-') {

                        processGenes(line.substring(0, 17), genes);

                    } else {
                        // only genes (line length > 18)
                        processGenes(line, genes);
                    }

                } else {
                    // only genes (line length <= 18)
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

            // parse the genotype information in scores strings
            List<String> scores = new ArrayList<>();

            // get the genotype text
            String genotypeText = getGenotypeText();

            // parse genotype text for scores
            char pos18;
            String genotypeScore = null;

            for (String line : genotypeText.split("\n")) {

                line = line.trim();

                if (line.length() > 18) {

                    pos18 = line.charAt(17);

                    if (pos18 == 'M' || pos18 == 'O' || pos18 == '-') {

                        // genes and scores
                        genotypeScore = line.substring(17).replace("M", "1").
                                replace("O", "0").replace("|", " ").replace("-", "" + MISSING_VALUE_CODE);

                        processScores(line.substring(0, 17), genotypeScore, scores);

                    } else {
                        // only genes (line length > 18)
                        processScores(line, genotypeScore, scores);
                    }

                } else {
                    // only genes (line length <= 18)
                    processScores(line, genotypeScore, scores);

                }

            }

            // turn the score strings into int array
            byte[][] data1 = new byte[scores.size()][];
            for (int k = 0; k < scores.size(); k++) {

                String[] vals = scores.get(k).split("\\s");
                byte[] row = new byte[vals.length];
                for (int i = 0; i < vals.length; i++) {
                    row[i] = Byte.valueOf(vals[i]).byteValue();
                }

                data1[k] = row;

            }

            // transpose the array row: samples col: genes (original had row: gene, col: samples)
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

            List<String> clones = new ArrayList<>();

            // define the cosmid file
            File cosmidFile = new File(filesDir, getHybridizationFile(linkageGroup));

            // define the reader and parse
            try {

                BufferedReader reader = new BufferedReader(new FileReader(cosmidFile));
                reader.readLine(); // ignore the first line

                String line = null;
                while ((line = reader.readLine()) != null)
                    clones.add(line.split("\\s")[0]);

            } catch (FileNotFoundException ex) {throw new RuntimeException(ex);}
            catch (IOException ex) {throw new RuntimeException(ex);}

            return clones;
        }


         /**
         * @return probes in probe_names.xml for this linkage group
         * @throws RuntimeException If any file reading or parsing error occurs
         */
        List<String> probes() {

            List<String> probesList = new ArrayList<>();

            // get the genotype text
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

            // define the cosmid file
            File cosmidFile = new File(filesDir, "ch_" + linkageGroup + ".cosmid");

            // define the reader and parse
            BufferedReader reader = null;
            try {

                reader = new BufferedReader(new FileReader(cosmidFile));

                // read the first line to get the number of probes and clones
                String firstLine = reader.readLine();
                String[] sizes = firstLine.split("\\s");

                int probeCount = Integer.valueOf(sizes[0]);
                int cloneCount = Integer.valueOf(sizes[1]);

                String line = null;
                data = new byte[cloneCount][probeCount];
                int lineCounter = 0;
                while ((line = reader.readLine()) != null) {

                    String text = line.split("\\s")[1];
                    for (int j = 0; j < text.length(); j++) {
                        data[lineCounter][j] = Byte.valueOf("" + text.charAt(j));
                    }

                    lineCounter++;
                }

            } catch (FileNotFoundException ex) {throw new RuntimeException(ex);}
            catch (IOException ex) {throw new RuntimeException(ex);}
            finally {
                try {
                    if (reader != null) reader.close();
                } catch (IOException ex) {throw new RuntimeException(ex);}

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
        private String getGenotypeText(){

            String genotypeText = null;

            // read the file genotypes.xml
            File genotypesFile = new File(filesDir, FILE_GENOTYPES);

            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(genotypesFile);
            } catch (DocumentException ex) { throw new RuntimeException(ex);}

            // obtain the gene names for the given linkage group
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

            // read the file probe_names.xml @todo close reader
            File genotypesFile = new File(filesDir, FILE_PROBES);
            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(genotypesFile);
            } catch (DocumentException ex) {throw new RuntimeException(ex);}

            // obtain the probe names for the given linkage group

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
            
            // read the file probe_names.xml @todo close reader
            File genomeFile = new File(filesDir, FILE_GENOME);
            SAXReader reader = new SAXReader();
            Document document = null;
            try {
                document = reader.read(genomeFile);
            } catch (DocumentException ex) {throw new RuntimeException(ex);}

            // obtain the genome for the given linkage group

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
