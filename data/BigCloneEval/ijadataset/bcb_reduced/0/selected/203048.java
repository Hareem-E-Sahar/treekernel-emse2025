package src.lib;

import java.util.List;
import org.ensembl.datamodel.Location;
import org.ensembl.variation.datamodel.VariationFeature;
import src.lib.objects.SNP;

;

/**
 * @version $Revision: 2530 $
 * @author 
 */
public class SNPDB implements Comparable<SNPDB> {

    private int start;

    private String details;

    public SNPDB() {
        this.start = 0;
        this.details = null;
    }

    /**
	 * 
	 * @param Const
	 * @param current_chromosome
	 * @return
	 */
    public static SNPDB[] get_variation_SNPs(Ensembl Const, int current_chromosome) {
        SNPDB[] SNP_list = null;
        List<VariationFeature> variations_list = Ensembl.get_vfa(new Location("chromosome", Const.get_chromosome(current_chromosome)));
        SNP_list = new SNPDB[variations_list.size()];
        for (int x = 0; x < variations_list.size(); x++) {
            VariationFeature vfa = variations_list.get(x);
            SNP_list[x] = new SNPDB();
            SNP_list[x].details = vfa.getAlleleString().toString();
            SNP_list[x].start = vfa.getLocation().getStart();
        }
        variations_list.clear();
        return SNP_list;
    }

    /**
	 * Setters - start
	 * @param x
	 */
    public void set_start(int x) {
        this.start = x;
    }

    /**
	 * Setters - details
	 * @param x
	 */
    public void set_details(String x) {
        this.details = x;
    }

    /**
	 * Getters - start
	 * @return
	 */
    public int get_start() {
        return this.start;
    }

    /**
	 * Getter - details
	 * @return
	 */
    public String get_details() {
        return this.details;
    }

    ;

    /**
	 * Function re-written by Nawar Malhis, to correct for defects in original version.
	 * @param a
	 * @param x
	 * @return
	 */
    public static int get_bound_low(SNP[] a, int x) {
        int low = 0;
        int high = a.length - 1;
        int mid = -1;
        if (a[low].get_position() > x) {
            return low;
        }
        if (a[high].get_position() < x) {
            return high + 1;
        }
        while (low < high) {
            mid = (low + high + 1) / 2;
            if (a[mid].get_position() < x) {
                low = mid;
            } else if (a[mid].get_position() > x) {
                high = mid;
            } else {
                return mid;
            }
            if (low >= high - 1) {
                return high;
            }
        }
        return mid + 1;
    }

    /**
	 * Function re-written by Nawar Malhis, to correct for defects in original version.
	 * @param a
	 * @param x
	 * @return
	 */
    public static int get_bound_high(SNP[] a, int x) {
        int low = 0;
        int high = a.length - 1;
        int mid = -1;
        if (a[low].get_position() > x) {
            return -1;
        }
        if (a[high].get_position() < x) {
            return high;
        }
        while (low < high) {
            mid = (low + high + 1) / 2;
            if (a[mid].get_position() < x) {
                low = mid;
            } else if (a[mid].get_position() > x) {
                high = mid;
            } else {
                return mid;
            }
            if (low >= high - 1) {
                return low;
            }
        }
        return mid - 1;
    }

    public int compareTo(SNPDB o) {
        if (o.start < this.start) {
            return 1;
        }
        if (o.start > this.start) {
            return -1;
        }
        return 0;
    }

    /**
	 * Based on code from java-tips.org
	 * @param a
	 * @param x
	 * @return
	 */
    public static int SNPbinarySearch(SNPDB[] a, int x) {
        int low = 0;
        int high = a.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (a[mid].start < x) {
                low = mid + 1;
            } else if (a[mid].start > x) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return -1;
    }
}
