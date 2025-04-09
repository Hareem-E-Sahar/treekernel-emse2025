package src.lib.analysisTools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.ensembl.datamodel.Exon;
import org.ensembl.datamodel.Gene;
import org.ensembl.datamodel.Location;
import src.lib.Ensembl;
import src.lib.ioInterfaces.Log_Buffer;
import src.lib.objects.Aberation;

/**
 * @version $Revision: 2056 $
 * @author 
 */
public class Exon_Overlap {

    private Exon_Overlap() {
    }

    static final int max_size_backwards = 50000;

    private static final boolean overlaps(Location el, int start, int end, int ext_width) {
        return (el.getStart() < end + ext_width && el.getEnd() > start - ext_width) ? true : false;
    }

    /**
	 * Custom binary search algorithm to find overlaps with exons.
	 * 
	 * This algorithm assumes that all exons are non-overlapping OR that
	 * overlapping exons are adjacent in the list. This will fail for large data
	 * sets that have long elements and are not in order by end()
	 * 
	 * @param exons
	 * @param ap
	 * @param ext_width
	 * @return null, if none found
	 */
    private static int[] overlap_search(Exon[] exons, Aberation ap, int ext_width) {
        int[] overlaps_found = null;
        ArrayList<Integer> o = new ArrayList<Integer>(0);
        int low = 0;
        int high = exons.length - 1;
        int lastMid = -1;
        while (low <= high) {
            int mid = (low + high) / 2;
            Location el = exons[mid].getLocation();
            if (overlaps(el, ap.get_start(), ap.get_end(), ext_width)) {
                o.add(mid);
                int n = 1;
                while (mid - n >= 0 && exons[mid - n].getLocation().getStart() + max_size_backwards < ap.get_start()) {
                    if (overlaps(exons[mid - n].getLocation(), ap.get_start(), ap.get_end(), ext_width)) {
                        o.add(mid - n);
                    }
                    n++;
                }
                n = 1;
                while (mid + n <= high && exons[mid + n].getLocation().getStart() > ap.get_end()) {
                    if (overlaps(exons[mid + n].getLocation(), ap.get_start(), ap.get_end(), ext_width)) {
                        o.add(mid + n);
                    }
                    n++;
                }
                Collections.sort(o);
                overlaps_found = new int[o.size()];
                int z = 0;
                for (Integer x : o) {
                    overlaps_found[z] = x;
                    z++;
                }
                return overlaps_found;
            } else {
                if (el.getStart() < ap.get_start()) {
                    low = mid;
                } else {
                    high = mid;
                }
            }
            if (lastMid == mid) {
                break;
            }
            lastMid = mid;
        }
        return null;
    }

    /**
	 * Custom binary search algorithm to find overlaps with Genes.
	 * 
	 * This algorithm assumes that all exons are non-overlapping OR that
	 * overlapping exons are adjacent in the list. This will fail for large data
	 * sets that have long elements and are not in order by end()
	 * 
	 * @param exons
	 * @param ap
	 * @param ext_width
	 * @return null, if none found
	 */
    private static int[] overlap_search(Gene[] exons, Aberation ap, int ext_width) {
        int[] overlaps_found = null;
        ArrayList<Integer> o = new ArrayList<Integer>(0);
        int low = 0;
        int high = exons.length - 1;
        int lastMid = -1;
        while (low <= high) {
            int mid = (low + high) / 2;
            Location el = exons[mid].getLocation();
            if (overlaps(el, ap.get_start(), ap.get_end(), ext_width)) {
                o.add(mid);
                int n = 1;
                while (mid - n >= 0 && exons[mid - n].getLocation().getStart() + max_size_backwards < ap.get_start()) {
                    if (overlaps(exons[mid - n].getLocation(), ap.get_start(), ap.get_end(), ext_width)) {
                        o.add(mid - n);
                    }
                    n++;
                }
                n = 1;
                while (mid + n <= high && exons[mid + n].getLocation().getStart() > ap.get_end()) {
                    if (overlaps(exons[mid + n].getLocation(), ap.get_start(), ap.get_end(), ext_width)) {
                        o.add(mid + n);
                    }
                    n++;
                }
                Collections.sort(o);
                overlaps_found = new int[o.size()];
                int z = 0;
                for (Integer x : o) {
                    overlaps_found[z] = x;
                    z++;
                }
                return overlaps_found;
            } else {
                if (el.getStart() < ap.get_start()) {
                    low = mid;
                } else {
                    high = mid;
                }
            }
            if (lastMid == mid) {
                break;
            }
            lastMid = mid;
        }
        return null;
    }

    /**
	 * 
	 * @param LB
	 * @param aber
	 * @param Const
	 * @param current_chromosome
	 * @param ext_width
	 * @param bw
	 */
    public static void process_exons(Log_Buffer LB, Aberation[] aber, Ensembl Const, int current_chromosome, int ext_width, BufferedWriter bw) {
        Location loc = new Location("chromosome", Const.get_chromosome(current_chromosome));
        LB.notice("Fetching Transcript/Exon Locations for chromosome...");
        List<Exon> exon_list = Ensembl.get_exons(loc);
        Exon[] exons = new Exon[exon_list.size()];
        exons = exon_list.toArray(exons);
        LB.notice("Fetching Transcript/Gene Locations for chromosome...");
        List<Gene> gene_list = Ensembl.get_genes(loc);
        Gene[] genes = new Gene[gene_list.size()];
        genes = gene_list.toArray(genes);
        for (Aberation a : aber) {
            int[] overlap_w_exons = overlap_search(exons, a, ext_width);
            if (overlap_w_exons != null) {
                write_out_info(LB, overlap_w_exons, a, genes, exons, true, bw);
            } else {
                int[] overlap_w_genes = overlap_search(genes, a, ext_width);
                if (overlap_w_genes != null) {
                    write_out_info(LB, overlap_w_genes, a, genes, exons, false, bw);
                }
            }
        }
    }

    /**
	 * 
	 * @param LB
	 * @param aber
	 * @param genes A list of all of the genes on the chromosome
	 * @param exons A list of all of the exons on the chromosome
	 * @param ext_width
	 * @param bw
	 */
    public static void process_exons(Log_Buffer LB, Aberation[] aber, Gene[] genes, Exon[] exons, int ext_width, BufferedWriter bw) {
        for (Aberation a : aber) {
            int[] overlap_w_exons = overlap_search(exons, a, ext_width);
            if (overlap_w_exons != null) {
                write_out_info(LB, overlap_w_exons, a, genes, exons, true, bw);
            } else {
                int[] overlap_w_genes = overlap_search(genes, a, ext_width);
                if (overlap_w_genes != null) {
                    write_out_info(LB, overlap_w_genes, a, genes, exons, false, bw);
                }
            }
        }
    }

    private static void write_out_info(Log_Buffer LB, int[] overlap_set, Aberation a, Gene[] genes, Exon[] exons, boolean ex, BufferedWriter bw) {
        StringBuffer sb = new StringBuffer();
        sb.append(((ex) ? "Exon:" : "Intron:") + "\t" + a.get_chromosome() + "\t" + a.get_start() + "\t" + a.get_end() + "\t" + a.get_comment() + "\t");
        String[] ensg = new String[overlap_set.length];
        String[] type = new String[overlap_set.length];
        String[] desc = new String[overlap_set.length];
        String[] exon = new String[overlap_set.length];
        int min_start = 0;
        int max_end = 0;
        int c = 0;
        for (int x : overlap_set) {
            Location l = null;
            Gene g = null;
            if (ex) {
                g = exons[x].getGene();
                l = exons[x].getLocation();
                exon[c] = exons[x].getAccessionID();
            } else {
                g = genes[x];
                l = genes[x].getLocation();
                exon[c] = genes[x].getAccessionID();
            }
            ensg[c] = g.getAccessionID();
            type[c] = g.getBioType();
            String d_tmp = g.getDescription();
            desc[c] = ((d_tmp != null) ? d_tmp : "");
            if (min_start == 0 || min_start > l.getStart()) {
                min_start = l.getStart();
            }
            if (max_end == 0 || max_end < l.getEnd()) {
                max_end = l.getEnd();
            }
            c++;
        }
        Arrays.sort(ensg);
        Arrays.sort(type);
        Arrays.sort(desc);
        sb.append(min_start + "\t" + max_end + "\t" + ensg[0]);
        for (int x = 1; x < ensg.length; x++) {
            if (!ensg[x].equals(ensg[x - 1])) {
                sb.append("," + ensg[x]);
            }
        }
        sb.append("\t" + type[0]);
        for (int x = 1; x < type.length; x++) {
            if (!type[x].equals(type[x - 1])) {
                sb.append("," + type[x]);
            }
        }
        sb.append("\t" + desc[0]);
        for (int x = 1; x < desc.length; x++) {
            if (!desc[x].equals(desc[x - 1])) {
                sb.append("," + desc[x]);
            }
        }
        sb.append("\t" + exon[0]);
        for (int x = 1; x < exon.length; x++) {
            if (!exon[x].equals(exon[x - 1])) {
                sb.append("," + exon[x]);
            }
        }
        try {
            bw.append(sb.toString());
            bw.newLine();
        } catch (IOException io) {
            LB.error("Could not write to bufferedwriter output file.");
            LB.error("Message thrown by Java environment (may be null):" + io.getMessage());
            LB.die();
        }
    }
}
