package src.projects.VariationDatabase.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;
import org.ensembl.datamodel.Transcript;
import src.lib.Ensembl;
import src.lib.ioInterfaces.Log_Buffer;
import src.lib.objects.Tuple;

/**
 * 
 * @author afejes
 * @version $Revision: 3565 $
 */
public class EnsemblMaps {

    static HashMap<String, Exon[]> ENS_exon_map = new HashMap<String, Exon[]>();

    static HashMap<String, Gene[]> ENS_gene_map = new HashMap<String, Gene[]>();

    static HashMap<String, Transcript[]> ENS_transcr_map = new HashMap<String, Transcript[]>();

    private static Log_Buffer LB = null;

    private static int SEARCH_WINDOW = 20000;

    private static int GENE_SEARCH_WINDOW = 1400000;

    public EnsemblMaps(Log_Buffer logbuffer, Ensembl ens) {
        LB = logbuffer;
        if (!ens.isInitialized()) {
            LB.error("Ensembl object must be initialized before using EnsemblMaps object");
            LB.die();
        }
    }

    public void close() {
        for (String s : ENS_exon_map.keySet()) {
            ENS_exon_map.put(s, null);
        }
        for (String s : ENS_gene_map.keySet()) {
            ENS_gene_map.put(s, null);
        }
        for (String s : ENS_transcr_map.keySet()) {
            ENS_transcr_map.put(s, null);
        }
        ENS_exon_map.clear();
        ENS_gene_map.clear();
        ENS_transcr_map.clear();
    }

    private static void retrieve_chromsome(String chr) {
        LB.debug(" * (Buffering chromosome: " + chr + ")");
        Location loc = new Location("chromosome", chr);
        List<Exon> tempExon = Ensembl.get_exons(loc);
        List<Gene> tempGene = Ensembl.get_genes(loc);
        List<Transcript> tempTran = Ensembl.get_transcripts(loc);
        if (!tempExon.isEmpty() && !tempGene.isEmpty() && !tempTran.isEmpty()) {
            ArrayList<Exon> exon_list = (ArrayList<Exon>) tempExon;
            ENS_exon_map.put(chr, exon_list.toArray(new Exon[exon_list.size()]));
            ArrayList<Gene> gene_list = (ArrayList<Gene>) tempGene;
            Gene[] ga = gene_list.toArray(new Gene[gene_list.size()]);
            ENS_gene_map.put(chr, ga);
            ArrayList<Transcript> trans_list = (ArrayList<Transcript>) tempTran;
            ENS_transcr_map.put(chr, trans_list.toArray(new Transcript[trans_list.size()]));
        } else {
            LB.error("couldn't find data for chromosome " + chr);
        }
    }

    public ArrayList<Transcript> get_transcripts(String chr, int position) {
        if (!ENS_transcr_map.containsKey(chr)) {
            retrieve_chromsome(chr);
        }
        Transcript[] all_transcripts = ENS_transcr_map.get(chr);
        ArrayList<Transcript> ovlp = new ArrayList<Transcript>();
        int bound_l = transcriptLowerBoundBinarySearch(all_transcripts, position);
        if (bound_l < 0) {
            return ovlp;
        }
        bound_l = 0;
        for (int i = bound_l; i < all_transcripts.length && position < all_transcripts[i].getLocation().getStart() + SEARCH_WINDOW; i++) {
            Location l = all_transcripts[i].getLocation();
            if (position >= l.getStart() && position <= l.getEnd()) {
                ovlp.add(all_transcripts[i]);
            }
        }
        return ovlp;
    }

    public ArrayList<Exon> get_exons(String chr, int position) {
        if (!ENS_exon_map.containsKey(chr)) {
            retrieve_chromsome(chr);
        }
        Exon[] exons = ENS_exon_map.get(chr);
        ArrayList<Exon> ovlp = new ArrayList<Exon>();
        int bound_l = exonLowerBoundBinarySearch(exons, position);
        if (bound_l < 0) {
            return ovlp;
        }
        for (int i = bound_l; (i < exons.length && position + SEARCH_WINDOW > exons[i].getLocation().getStart()); i++) {
            Location l = exons[i].getLocation();
            if (position >= l.getStart() && position <= l.getEnd()) {
                ovlp.add(exons[i]);
            }
        }
        return ovlp;
    }

    public ArrayList<Exon> get_junction_exons(String chr, int position) {
        if (!ENS_exon_map.containsKey(chr)) {
            retrieve_chromsome(chr);
        }
        Exon[] exons = ENS_exon_map.get(chr);
        ArrayList<Exon> ovlp = new ArrayList<Exon>();
        int bound_l = exonLowerBoundBinarySearch(exons, position - 4);
        if (bound_l < 0) {
            return ovlp;
        }
        for (int i = bound_l; (i < exons.length && position + SEARCH_WINDOW > exons[i].getLocation().getStart()); i++) {
            Location l = exons[i].getLocation();
            if (position >= (l.getStart() - 2) && position <= (l.getStart() + 2)) {
                ovlp.add(exons[i]);
            } else if (position >= (l.getEnd() - 2) && position <= (l.getEnd() + 2)) {
                ovlp.add(exons[i]);
            }
        }
        return ovlp;
    }

    /**
	 *
	 * @param chr
	 * @param position
	 * @return
	 */
    public ArrayList<Gene> get_genes(String chr, int position) {
        if (!ENS_gene_map.containsKey(chr)) {
            retrieve_chromsome(chr);
        }
        Gene[] genes = ENS_gene_map.get(chr);
        ArrayList<Gene> ovlp = new ArrayList<Gene>();
        for (Gene g : genes) {
            Location l = g.getLocation();
            int start = l.getStart();
            int end = l.getEnd();
            if (position < start) {
                break;
            }
            if (position >= start && position <= end) {
                ovlp.add(g);
            }
        }
        return ovlp;
    }

    /**
	 * Returns the closest genes (from both direction) to a region. 
	 * If the region overlaps genes it will return the overlapping genes.
	 *
	 * @param chr
	 * @param startPos
	 * @param endPos
	 * @return
	 */
    public ArrayList<Gene> get_closest_genes_region(String chr, int startPos, int endPos) {
        if (!ENS_gene_map.containsKey(chr)) {
            retrieve_chromsome(chr);
        }
        Gene[] genes = ENS_gene_map.get(chr);
        if (genes == null || genes.length == 0) {
            return null;
        }
        ArrayList<Gene> ovlp = get_genes_region(chr, startPos, endPos);
        if (ovlp.size() != 0) {
            return ovlp;
        }
        ovlp = new ArrayList<Gene>();
        Gene left = null, right = null;
        int minLeftDist = Integer.MAX_VALUE;
        int minRightDist = Integer.MAX_VALUE;
        for (Gene g : genes) {
            Location l = g.getLocation();
            int start = l.getStart();
            int end = l.getEnd();
            if (startPos >= end) {
                if (startPos - end < minLeftDist) {
                    minLeftDist = startPos - end;
                    left = g;
                }
            }
            if (endPos <= start) {
                if (start - endPos < minRightDist) {
                    minRightDist = start - endPos;
                    right = g;
                }
            }
        }
        ovlp.add(left);
        ovlp.add(right);
        return ovlp;
    }

    /**
	 * Version of get_genes for which the list of genes for a given chromosome
	 * is supplied, and does not need to be taken from the SNP_Analysis object
	 * itself - useful when the snps are not in order, and a pre-generated list
	 * of snps for each chromosome is held.
	 *
	 * @param chr
	 * @param startPos
	 * @param endPos
	 * @return
	 */
    public static ArrayList<Gene> get_genes_region(String chr, int startPos, int endPos) {
        if (!ENS_gene_map.containsKey(chr)) {
            retrieve_chromsome(chr);
        }
        Gene[] genes = ENS_gene_map.get(chr);
        ArrayList<Gene> ovlp = new ArrayList<Gene>();
        if (genes == null) {
            return ovlp;
        }
        int bound_l = geneLowerBoundBinarySearch(genes, startPos);
        if (bound_l < 0) {
            return ovlp;
        }
        for (int i = bound_l; (i < genes.length && endPos + SEARCH_WINDOW > genes[i].getLocation().getStart()); i++) {
            Location l = genes[i].getLocation();
            if (endPos >= l.getStart() && startPos <= l.getEnd()) {
                ovlp.add(genes[i]);
            }
        }
        return ovlp;
    }

    /**
	 * Version of get_exons for which the list of exons for a given chromosome
	 * is supplied, and does not need to be taken from the SNP_Analysis object
	 * itself - useful when the snps are not in order, and a pre-generated list
	 * of snps for each chromosome is held.
	 *
	 * @param chr
	 * @param startPos
	 * @param endPos
	 * @return
	 */
    public ArrayList<Exon> get_exons_region(String chr, int startPos, int endPos) {
        if (!ENS_exon_map.containsKey(chr)) {
            retrieve_chromsome(chr);
        }
        Exon[] exons = ENS_exon_map.get(chr);
        ArrayList<Exon> ovlp = new ArrayList<Exon>();
        int bound_l = exonLowerBoundBinarySearch(exons, startPos);
        if (bound_l < 0) {
            return ovlp;
        }
        for (int i = bound_l; (i < exons.length && endPos + SEARCH_WINDOW > exons[i].getLocation().getStart()); i++) {
            Location l = exons[i].getLocation();
            if (endPos >= l.getStart() && startPos <= l.getEnd()) {
                ovlp.add(exons[i]);
            }
        }
        return ovlp;
    }

    /**
	 * Based on code from java-tips.org
	 * @param a
	 * @param x
	 * @return
	 */
    public static int exonLowerBoundBinarySearch(Exon[] a, int x) {
        int low = 0;
        int high = a.length - 1;
        int mid = -1;
        while (low < high - 1) {
            mid = (low + high) / 2;
            if (a[mid].getLocation().getStart() < x - SEARCH_WINDOW) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return low;
    }

    /**
	 * Based on code from java-tips.org
	 * @param a
	 * @param x
	 * @return
	 */
    public static int transcriptLowerBoundBinarySearch(Transcript[] a, int x) {
        int low = 0;
        int high = a.length - 1;
        int mid = -1;
        while (low < high - 1) {
            mid = (low + high) / 2;
            if (a[mid].getLocation().getStart() < x - SEARCH_WINDOW) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return low;
    }

    /**
	 * Based on code from java-tips.org
	 * @param a
	 * @param x
	 * @return
	 */
    public static int geneLowerBoundBinarySearch(Gene[] a, int x) {
        if (a == null) {
            return -1;
        }
        int low = 0;
        int high = a.length - 1;
        int mid = -1;
        while (low < high - 1) {
            mid = (low + high) / 2;
            if (a[mid].getLocation().getStart() < (x - GENE_SEARCH_WINDOW)) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return low;
    }

    public static String getGeneIDInRegion(String chromosome, int start, int end) {
        List<Gene> genes = get_genes_region(chromosome, start, end);
        if (genes.size() > 0) {
            return genes.get(0).getDisplayName();
        }
        return null;
    }

    /**
	 * returns the geneId and the EnsembleGenenId for the the specified region.
	 * @param regionName
	 * @param chromosome
	 * @param start
	 * @param end
	 * @return
	 */
    public static Tuple<String, String> getEnsmeblIDInRegion(String regionName, String chromosome, int start, int end) {
        List<Gene> genes = Ensembl.get_genes(regionName);
        LB.debug("retreiveing ensemblid for " + regionName);
        if (genes.size() > 0) {
            return new Tuple<String, String>(regionName, genes.get(0).getAccessionID());
        }
        LB.debug("no ensemlbe id found searching by location");
        genes = get_genes_region(chromosome, start, end);
        if (genes.size() > 0) {
            LB.debug(genes.get(0).getAccessionID() + " ensemlbe id found searching by location");
            return new Tuple<String, String>(genes.get(0).getDisplayName(), genes.get(0).getAccessionID());
        }
        LB.debug("no ensemlbe id found by searching by location");
        return null;
    }
}
