package src.projects.maq_utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Vector;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Location;
import src.lib.Chromosome;
import src.lib.CommandLine;
import src.lib.Constants;
import src.lib.CurrentVersion;
import src.lib.Ensembl;
import src.lib.IterableIterator;
import src.lib.Utilities;
import src.lib.Error_handling.CommandLineProcessingException;
import src.lib.Error_handling.DoesNotMatchException;
import src.lib.Error_handling.OverflowException;
import src.lib.analysisTools.Exon_Junction_Map;
import src.lib.ioInterfaces.FastaIterator;
import src.lib.ioInterfaces.FileOut;
import src.lib.ioInterfaces.Log_Buffer;
import src.lib.ioInterfaces.MAQJunctionMapIterator;
import src.lib.ioInterfaces.MAQmapIterator;
import src.lib.objects.AlignedRead;
import src.lib.objects.MAQRyanMap;
import src.lib.objects.SimpleAlignedRead;
import src.projects.maq_utilities.objects.ExonBoundaries;
import src.projects.maq_utilities.objects.Exonboundary;
import src.projects.maq_utilities.objects.GeneMap;

public class ReadsUsed {

    private static final int JUNCTION_HALF_WIDTH = 35;

    private static final String JUNCTION_READ_NAME = "all_junctions.36.version2";

    private static Ensembl Const;

    private static Chromosome Chr;

    private static int current_chromosome;

    private static String elandfile_path;

    private static String output_path;

    private static String name;

    private static String input_species;

    private static String input_chr;

    private static float min_percent;

    private static int min_observed;

    private static String conf_file;

    private static String aligner;

    private static String junction_map;

    private static int maq_read_size;

    private static int running_bases_covered = 0;

    static int reads_used = 0;

    static HashMap<String, Integer> map_transcript_exon;

    private ReadsUsed() {
    }

    private static Log_Buffer LB = null;

    /**
	 * Processing command line arguments for program.
	 * 
	 * @param Variables
	 *            Command line arguments: input path, output path, Species,
	 *            Chromosome(s), min snp percent, min snp observed.
	 */
    private static void parse_input(HashMap<String, String> Variables) {
        if (Variables == null) {
            usage();
        }
        assert (Variables != null);
        if (Variables.containsKey("help")) {
            usage();
        }
        if (Variables.containsKey("name")) {
            CommandLine.test_parameter_count(LB, "species", Variables.get("name"), 1);
            name = Variables.get("name");
        } else {
            LB.notice("file names must be supplied with -name flag");
            usage();
        }
        if (Variables.containsKey("output")) {
            CommandLine.test_parameter_count(LB, "output", Variables.get("output"), 1);
            output_path = Variables.get("output");
            if (!output_path.endsWith(System.getProperty("file.separator"))) {
                output_path = output_path.concat(System.getProperty("file.separator"));
            }
            LB.notice("Log File: " + output_path + name + ".log");
            LB.addLogFile(output_path + name + ".log");
        } else {
            LB.error("An output directory must be supplied with the -output flag");
            usage();
        }
        LB.notice(" * Output directory  : " + output_path);
        LB.notice(" * File naming       : " + name);
        if (Variables.containsKey("aligner")) {
            CommandLine.test_parameter_count_min(LB, "aligner", Variables.get("aligner"), 1);
            aligner = Variables.get("aligner");
            LB.notice(" * Input aligner     : " + aligner);
        } else {
            LB.error("chomosome must be supplied with -chr flag");
            usage();
        }
        if (Variables.containsKey("force32")) {
            CommandLine.test_parameter_count(LB, "force32", Variables.get("force32"), 0);
            Chromosome.set_force32(true);
            LB.notice(" * Filter Duplicates : On");
        } else {
            Chromosome.set_force32(false);
            LB.notice(" * Filter Duplicates : Off");
        }
        if (Variables.containsKey("min_alt")) {
            CommandLine.test_parameter_count(LB, "min_alt", Variables.get("min_alt"), 1);
            min_percent = Float.parseFloat(Variables.get("min_alt"));
            if (min_percent > 1 || min_percent < 0) {
                LB.error("Min_alt value must be in the range of zero to one.");
                LB.die();
            }
        } else {
            LB.error("Must specify minimum alternative base percent for SNP positions with the -min_alt flag");
            usage();
        }
        LB.notice(" * Min. change fract : " + min_percent);
        if (Variables.containsKey("min_obs")) {
            CommandLine.test_parameter_count(LB, "min_obs", Variables.get("min_obs"), 1);
            min_observed = Integer.parseInt(Variables.get("min_obs"));
        } else {
            LB.error("Must specify minimum observed base count for SNP positions with the -min_obs flag");
            usage();
        }
        LB.notice(" * Minimum coverage  : " + min_observed);
        if (Variables.containsKey("conf")) {
            CommandLine.test_parameter_count(LB, "conf", Variables.get("conf"), 1);
            conf_file = Variables.get("conf");
            LB.notice(" * Config file       : " + conf_file);
        } else {
            LB.error("Must specify config file with the -conf flag");
            usage();
        }
        if (Variables.containsKey("junctionmap")) {
            CommandLine.test_parameter_count(LB, "junctionmap", Variables.get("junctionmap"), 1);
            junction_map = Variables.get("junctionmap");
            LB.notice(" * Junction map      : " + junction_map);
        } else {
            LB.error("Must specify config file with the -junctionmap flag");
            usage();
        }
        if (Variables.containsKey("maq_read_size")) {
            if (!aligner.equals(Constants.FILE_TYPE_MAQ)) {
                LB.notice("-maq_read_size only used for maq aligner");
                usage();
            }
            CommandLine.test_parameter_count(LB, "maq_read_size", Variables.get("maq_read_size"), 1);
            maq_read_size = Integer.valueOf(Variables.get("maq_read_size"));
            LB.notice(" * Maq read size     : " + maq_read_size);
        } else {
            LB.error("Must specify list of size of the maq reads with the -maq_read_size flag." + " Use 64 for maq 0.6.x use 128 for maq 0.7+");
            usage();
        }
        if (Variables.containsKey("chr")) {
            CommandLine.test_parameter_count_min(LB, "chr", Variables.get("chr"), 1);
            input_chr = Variables.get("chr");
            LB.notice(" * Chromosome in use : " + input_chr);
        } else {
            LB.error("chomosome must be supplied with -chr flag");
            usage();
        }
        if (Variables.containsKey("species")) {
            CommandLine.test_parameter_count(LB, "species", Variables.get("species"), 1);
            input_species = Variables.get("species");
            LB.notice(" * Input Species     : " + input_species);
        } else {
            LB.error("input species must be supplied with -input flag");
            usage();
        }
        if (Variables.containsKey("input")) {
            CommandLine.test_parameter_count(LB, "input", Variables.get("input"), 1);
            elandfile_path = Variables.get("input");
            if (!elandfile_path.endsWith(System.getProperty("file.separator"))) {
                elandfile_path = elandfile_path.concat(System.getProperty("file.separator"));
            }
            LB.notice(" * Input directory   : " + elandfile_path);
        } else {
            LB.error("An input directory must be supplied with the -input flag");
            usage();
        }
        Variables.remove("input");
        Variables.remove("output");
        Variables.remove("species");
        Variables.remove("chr");
        Variables.remove("min_alt");
        Variables.remove("min_obs");
        Variables.remove("force32");
        Variables.remove("conf");
        Variables.remove("name");
        Variables.remove("aligner");
        Variables.remove("junctionmap");
        Variables.remove("maq_read_size");
        Iterator<String> keys = Variables.keySet().iterator();
        if (keys.hasNext()) {
            StringBuffer sb = new StringBuffer();
            sb.append("Could not process the following flags:");
            for (String k : new IterableIterator<String>(keys)) {
                sb.append("  " + k);
            }
            LB.error(sb.toString());
            LB.die();
        }
    }

    private static void usage() {
        LB.notice("This program requires the following parameters:");
        LB.notice(" -input      | <String> | provide the full path to the eland files.");
        LB.notice(" -output     | <String> | provide a valid path for the output.");
        LB.notice(" -species    | <String> | Provide a Species handled in the conf file");
        LB.notice(" -chr        | <String> | Indicate which chromosome to run, or \"A\" for all.");
        LB.notice(" -min_alt    | <Float>  | Indicate the minimum fraction for calling a snp (eg 0.5");
        LB.notice(" -min_obs    | <Int>    | Indicate the minimum coverage that must be observed to call. (eg 4)");
        LB.notice(" -force32    |          | use to force the maximum read length to be 32 bases.");
        LB.notice(" -aligner    | <String> | Name of the aligner that provided the reads (defines file format).");
        LB.notice(" -name       | <String> | Provides an identifier at the start of each file name.");
        LB.notice(" -conf       | <String> | The location of the configuration file to use.");
        LB.notice(" -junctionmap| <String> | The location of the configuration file to use.");
        LB.notice(" -maq_read_size | <int> | The file for intron spanning mapped reads.");
        LB.die();
    }

    /**
	 * 
	 * @param filename
	 * @param chrname
	 * @return
	 */
    private static Chromosome new_chr_from_fasta_file(String filename, String chrname) {
        FastaIterator fi = new FastaIterator(LB, filename);
        String[] rrr = fi.next();
        fi.close();
        return new Chromosome(LB, name, rrr[1], chrname, 0);
    }

    private static MAQRyanMap[] get_all_junctions_map(String filename) {
        Vector<MAQRyanMap> v = new Vector<MAQRyanMap>();
        MAQJunctionMapIterator m = new MAQJunctionMapIterator(LB, "JunctionMap", filename);
        for (MAQRyanMap n : new IterableIterator<MAQRyanMap>(m)) {
            v.add(n);
        }
        MAQRyanMap[] ar = new MAQRyanMap[v.size()];
        ar = v.toArray(ar);
        return ar;
    }

    private static int add_spanned_reads_to_chr(Vector<SimpleAlignedRead> mapped_junctions, String last_chromosome) {
        int number_of_mapped_reads = 0;
        for (SimpleAlignedRead x : mapped_junctions) {
            if (last_chromosome.equals(x.get_chr())) {
                try {
                    Chr.addSimpleAlignedReadBaseCount(x, 1);
                    number_of_mapped_reads++;
                } catch (DoesNotMatchException dnme) {
                    LB.warning("Failed to Parse Eland Read: " + dnme.getMessage());
                    LB.warning(x.toString());
                } catch (OverflowException oe) {
                    LB.warning(oe.getMessage());
                    LB.warning(x.toString());
                }
            }
        }
        return number_of_mapped_reads;
    }

    private static HashMap<String, ExonBoundaries> build_exon_list(List<Exon> exon_list) {
        HashMap<String, ExonBoundaries> map = new HashMap<String, ExonBoundaries>();
        for (Exon e : exon_list) {
            String key = e.getGene().getAccessionID();
            if (map.containsKey(key)) {
                Location l = e.getLocation();
                int l_start = l.getStart();
                int l_end = l.getEnd();
                ExonBoundaries xbs = map.get(key);
                int c = 0;
                boolean processed = false;
                while (c < xbs.get_size()) {
                    Exonboundary xb = xbs.get(c);
                    if ((l_start >= xb.get_start() && l_start <= xb.get_end()) || (l_end <= xb.get_end() && l_end >= xb.get_start())) {
                        if (xb.get_start() == l_start && xb.get_end() == l_end) {
                            processed = true;
                            break;
                        }
                        if (xb.get_start() > l_start) {
                            xb.set_start(l_start);
                            if (c > 0) {
                                if (xbs.get(c - 1).get_end() >= l_start) {
                                    c--;
                                    xb.set_start(xbs.get(c).get_start());
                                    xbs.remove(c);
                                }
                            }
                            processed = true;
                        }
                        if (xb.get_end() < l_end) {
                            xb.set_end(l_end);
                            if (c < xbs.get_size() - 1) {
                                if (xbs.get(c + 1).get_start() <= l_end) {
                                    xb.set_end(xbs.get(c + 1).get_end());
                                    xbs.remove(c + 1);
                                }
                            }
                            processed = true;
                        }
                        if (processed) {
                            xbs.set(c, xb);
                            break;
                        }
                    }
                    c++;
                }
                if (!processed) {
                    Exonboundary xb = new Exonboundary(l.getStart(), l.getEnd());
                    if (l_start < xbs.get(0).get_start()) {
                        xbs.add(0, xb);
                    } else {
                        if (xbs.get_size() == 1) {
                            xbs.add(xb);
                        } else {
                            if (l_start > xbs.get(xbs.get_size() - 1).get_start()) {
                                xbs.add(xb);
                            } else {
                                for (c = 0; c < xbs.get_size() - 1; c++) {
                                    int s1 = xbs.get(c).get_start();
                                    int s2 = xbs.get(c + 1).get_start();
                                    if (l_start > s1 && l_start < s2) {
                                        xbs.add(c + 1, xb);
                                        processed = true;
                                        break;
                                    }
                                }
                                if (!processed) {
                                    LB.warning("Building database - found location, but could not place.  unexpected error - bug!");
                                    LB.warning("l_start = " + l_start + "\tl_end: " + l_end);
                                    for (c = 0; c < xbs.get_size(); c++) {
                                        int s1 = xbs.get(c).get_start();
                                        int e1 = xbs.get(c).get_end();
                                        LB.notice(c + " start: " + s1 + "\tend: " + e1);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Location l = e.getLocation();
                Exonboundary xb = new Exonboundary(l.getStart(), l.getEnd());
                ExonBoundaries xbs = new ExonBoundaries();
                xbs.add(xb);
                map.put(key, xbs);
            }
        }
        return map;
    }

    private static ArrayList<GeneMap> build_squashed_transcriptome(FileOut fo, HashMap<String, ExonBoundaries> exon_map) {
        Iterator<String> keys = exon_map.keySet().iterator();
        ArrayList<GeneMap> squash_transc = new ArrayList<GeneMap>();
        for (String k : new IterableIterator<String>(keys)) {
            ExonBoundaries xbs = exon_map.get(k);
            int bases_for_gene = 0;
            for (Exonboundary xb : new IterableIterator<Exonboundary>(xbs)) {
                for (int w = xb.get_start(); w <= xb.get_end(); w++) {
                    if (Chr.get_occupancy(w) >= min_observed) {
                        bases_for_gene++;
                    }
                }
            }
            if (bases_for_gene > 0) {
                running_bases_covered += bases_for_gene;
                squash_transc.add(new GeneMap(k, running_bases_covered));
            } else {
                fo.writeln(k);
            }
        }
        return squash_transc;
    }

    private static int Maq_process() {
        int number_of_added_reads = 0;
        LB.notice("Reading in Ryan's junction to genome mappings");
        MAQRyanMap[] all_junctions = get_all_junctions_map(junction_map);
        LB.notice("First Pass - getting chromosome names");
        MAQmapIterator m = new MAQmapIterator(LB, "mapfile", elandfile_path, 0, maq_read_size, 0);
        String[] chromosome_names = m.get_chromosomes();
        for (String c : chromosome_names) {
            LB.notice(c);
        }
        boolean first_pass_needed = false;
        Vector<SimpleAlignedRead> mapped_junctions = new Vector<SimpleAlignedRead>();
        if (!chromosome_names[0].trim().equals(JUNCTION_READ_NAME)) {
            first_pass_needed = true;
            LB.notice("Will process in two passes");
        } else {
            first_pass_needed = false;
            LB.notice("Will process in one pass only");
        }
        if (first_pass_needed) {
            LB.notice("first pass started");
            int count_processed = 0;
            int count_failed = 0;
            while (m.hasNext()) {
                AlignedRead x = null;
                try {
                    x = m.next();
                } catch (NoSuchElementException nsee) {
                    continue;
                }
                if (x.get_chromosome().equals(JUNCTION_READ_NAME)) {
                    Vector<SimpleAlignedRead> tmp = Exon_Junction_Map.TranslateJunction(LB, x, all_junctions, JUNCTION_HALF_WIDTH);
                    if (tmp != null) {
                        mapped_junctions.addAll(tmp);
                        count_processed++;
                        number_of_added_reads++;
                    } else {
                        LB.notice("Skipped a junction - could not map it.");
                        LB.notice("Sequence: " + x.get_sequence());
                        count_failed++;
                        continue;
                    }
                }
            }
            LB.notice("first pass ended. " + count_processed + "reads processed and " + count_failed + " reads failed");
            m.close(false);
            LB.notice("creating maq map iterator.");
            m = new MAQmapIterator(LB, "mapfile", elandfile_path, 0, maq_read_size, 0);
        }
        int nbLoop = 0;
        String last_chromosome = "";
        String Chromosome_name = "";
        ArrayList<GeneMap> map_entries = new ArrayList<GeneMap>();
        LB.notice("beginning to process aligned .map file.");
        FileOut fo = new FileOut(LB, output_path + "genes_with_no_coverage.txt", false);
        while (m.hasNext()) {
            nbLoop++;
            AlignedRead x = null;
            try {
                x = m.next();
            } catch (NoSuchElementException nsee) {
                continue;
            }
            if (x.get_chromosome().equals(JUNCTION_READ_NAME)) {
                Vector<SimpleAlignedRead> tmp = Exon_Junction_Map.TranslateJunction(LB, x, all_junctions, JUNCTION_HALF_WIDTH);
                if (tmp != null) {
                    mapped_junctions.addAll(tmp);
                    number_of_added_reads++;
                } else {
                    continue;
                }
            }
            if (!last_chromosome.equals(x.get_chromosome())) {
                current_chromosome = Const.index_chromosomes(Utilities.translate_Current_Chromosome(last_chromosome));
                if (current_chromosome >= 0) {
                    LB.notice("Adding in mapped Spanning reads");
                    number_of_added_reads += add_spanned_reads_to_chr(mapped_junctions, last_chromosome);
                    LB.notice("would be looking for snps now.");
                    LB.notice("The coverage at " + min_observed + "x is : " + Chr.coverage(min_observed));
                    LB.notice("Fetching Exon Locations for chromosome and building hash table of exon locations");
                    Location loc = new Location("chromosome", Const.get_chromosome(current_chromosome));
                    List<Exon> exon_list = Ensembl.get_exons(loc);
                    HashMap<String, ExonBoundaries> exon_map = build_exon_list(exon_list);
                    LB.notice("Done");
                    LB.notice("Building map of genes with coverage");
                    map_entries.addAll(build_squashed_transcriptome(fo, exon_map));
                    exon_map.clear();
                    LB.notice("Done");
                    Chr.destroy();
                    Chr = null;
                }
                Chromosome_name = Utilities.translate_Current_Chromosome(x.get_chromosome());
                if (Const.index_chromosomes(Chromosome_name) >= 0) {
                    LB.notice("Retrieving canonical sequence for chromosome: " + Chromosome_name);
                    String ffile = Const.getFastaFilename(Chromosome_name, "chr");
                    Chr = new_chr_from_fasta_file(ffile, Chromosome_name);
                    LB.notice("Done");
                }
            }
            try {
                if (Chr != null) {
                    if (x.get_identity() == 130 && x.get_maq_indel_len() != 0) {
                        Chr.addGappedRawBaseCount(x, 1, false);
                    } else {
                        Chr.addSimpleAlignedReadBaseCount(x.toSimpleAlignedRead(), 1);
                    }
                    number_of_added_reads++;
                }
            } catch (DoesNotMatchException dnme) {
                LB.warning("Failed to Parse Maq Read: " + dnme.getMessage());
                LB.warning(x.toString());
            } catch (OverflowException oe) {
                LB.warning(oe.getMessage());
                LB.warning(x.toString());
            }
            last_chromosome = x.get_chromosome();
        }
        if (Chr != null) {
            current_chromosome = Const.index_chromosomes(Utilities.translate_Current_Chromosome(last_chromosome));
            if (current_chromosome >= 0) {
                LB.notice("Adding in mapped Spanning reads");
                number_of_added_reads += add_spanned_reads_to_chr(mapped_junctions, last_chromosome);
                LB.notice("would be looking for snps now.");
                LB.notice("The coverage at " + min_observed + "x is : " + Chr.coverage(min_observed));
                LB.notice("Fetching Exon Locations for chromosome and building hash table of exon locations");
                Location loc = new Location("chromosome", Const.get_chromosome(current_chromosome));
                List<Exon> exon_list = Ensembl.get_exons(loc);
                HashMap<String, ExonBoundaries> exon_map = build_exon_list(exon_list);
                LB.notice("Done");
                LB.notice("Building map of genes with coverage");
                map_entries.addAll(build_squashed_transcriptome(fo, exon_map));
                exon_map.clear();
                LB.notice("Done");
                Chr.destroy();
                Chr = null;
            }
        }
        fo.close();
        GeneMap[] map_items = new GeneMap[map_entries.size()];
        map_items = map_entries.toArray(map_items);
        LB.notice("Writing out map so that it doesn't need to be regenerated");
        FileOut map = new FileOut(LB, output_path + "map.txt", false);
        for (GeneMap g : map_items) {
            map.writeln(g.get_Accesssion() + "\t" + g.get_end());
        }
        map.close();
        LB.notice("Done");
        LB.notice("Beginning Simulation");
        int max_size = map_items[map_items.length - 1].get_end();
        HashMap<String, String> genes = new HashMap<String, String>();
        final int it = 10;
        final int sn = 245;
        Random generator = new Random();
        for (int iterations = 0; iterations < it; iterations++) {
            for (int snps = 0; snps < sn; snps++) {
                int randomIndex = generator.nextInt(max_size);
                String key = get_gene_name_from_map(map_items, randomIndex);
                if (!genes.containsKey(key)) {
                    genes.put(key, "");
                }
            }
            Iterator<String> keys = genes.keySet().iterator();
            FileOut sim = new FileOut(LB, output_path + "simulation_" + iterations + "_" + sn + ".txt", false);
            for (String k : new IterableIterator<String>(keys)) {
                sim.writeln(k);
            }
            sim.close();
        }
        LB.notice("Done");
        return number_of_added_reads;
    }

    /**
	 * 
	 * @param a
	 * @param x
	 * @return
	 */
    public static String get_gene_name_from_map(GeneMap[] a, int x) {
        int low = 0;
        int high = a.length - 1;
        if (a[low].get_end() >= x) {
            return a[low].get_Accesssion();
        }
        while (low < high) {
            int mid = (low + high) / 2;
            if (a[mid - 1].get_end() < x && a[mid].get_end() >= x) {
                return a[mid].get_Accesssion();
            }
            if (a[mid].get_end() < x) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return "broken algorithm!";
    }

    /**
	 * Main function for processing Transcriptome data.
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
        LB = Log_Buffer.getLogBufferInstance();
        LB.addPrintStream(System.out);
        Thread th = new Thread(LB);
        th.start();
        HashMap<String, String> Variables = null;
        try {
            Variables = CommandLine.process_CLI(args);
        } catch (CommandLineProcessingException CLPE) {
            LB.error(CLPE.getMessage());
            LB.die();
        }
        parse_input(Variables);
        new CurrentVersion(LB);
        LB.Version("ReadsUsed", "$Revision: 3429 $");
        Const = Ensembl.init(LB, input_species, conf_file, input_chr);
        reads_used = Maq_process();
        Const.destroy();
        Const = null;
        map_transcript_exon = null;
        LB.notice("\nStatistics:");
        LB.notice("Total sequence reads used:" + reads_used);
        LB.close();
    }
}
