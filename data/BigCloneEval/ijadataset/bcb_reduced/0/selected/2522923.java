package src.lib.analysisTools;

import java.util.ArrayList;
import java.util.List;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Location;
import src.lib.Ensembl;
import src.lib.ioInterfaces.Log_Buffer;

/**
 * @version $Revision: 2772 $
 * @author 
 */
public class Indel_Analysis {

    private static Exon[] all_exons = null;

    private static Log_Buffer LB = null;

    private String chromosome = null;

    /**
	 * the longest exon I would imagine is worth looking for. (Actually, I
	 * confirmed it's ~19000, so 20,000 is a good approximation)
	 */
    private static int SEARCH_WINDOW = 20000;

    public Indel_Analysis(Log_Buffer logbuffer) {
        LB = logbuffer;
        chromosome = "";
    }

    public void close() {
        if (all_exons != null) {
            all_exons = null;
        }
        LB = null;
        chromosome = null;
    }

    public void move_to_chromosome(String chr) {
        if (chromosome.compareTo(chr) != 0) {
            chromosome = chr;
            String c = chromosome;
            if (chromosome.startsWith("chr")) {
                c = chromosome.substring(3);
            }
            Location loc = new Location("chromosome", c);
            LB.notice("Fetching Transcript & Exon Locations for chromosome " + chr);
            List<Exon> t = Ensembl.get_exons(loc);
            all_exons = t.toArray(new Exon[t.size()]);
        } else {
        }
    }

    public String this_chromosome() {
        return chromosome;
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
	 * Version to use when you don't have a list of exons for the chromosome containing the feature of interest.
	 * @param chr
	 * @param startPos
	 * @param endPos
	 * @return
	 * @deprecated
	 */
    public ArrayList<Exon> get_exons(String chr, int startPos, int endPos) {
        if (chromosome.compareTo(chr) != 0) {
            LB.debug("Wrong chromosome: " + chr + " and " + this_chromosome());
            return null;
        }
        ArrayList<Exon> ovlp = new ArrayList<Exon>();
        int bound_l = exonLowerBoundBinarySearch(all_exons, startPos);
        if (bound_l < 0) {
            return ovlp;
        }
        bound_l = 0;
        for (int i = bound_l; i < all_exons.length && startPos + SEARCH_WINDOW + (endPos - startPos) < all_exons[i].getLocation().getStart(); i++) {
            Location l = all_exons[i].getLocation();
            if (endPos >= l.getStart() && startPos <= l.getEnd()) {
                ovlp.add(all_exons[i]);
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
	 * @param exons
	 * @param startPos
	 * @param endPos
	 * @return
	 */
    public static ArrayList<Exon> get_exons(Exon[] exons, int startPos, int endPos) {
        ArrayList<Exon> ovlp = new ArrayList<Exon>();
        int bound_l = exonLowerBoundBinarySearch(exons, startPos);
        if (bound_l < 0) {
            return ovlp;
        }
        for (int i = bound_l; (i < exons.length && startPos + SEARCH_WINDOW + (endPos - startPos) > exons[i].getLocation().getStart()); i++) {
            Location l = exons[i].getLocation();
            if (endPos >= l.getStart() && startPos <= l.getEnd()) {
                ovlp.add(exons[i]);
            }
        }
        return ovlp;
    }
}
