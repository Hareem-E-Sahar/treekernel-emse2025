package src.lib.analysisTools;

import java.util.Vector;
import src.lib.IterableIterator;
import src.lib.Utilities;
import src.lib.Error_handling.UnexpectedCharacterException;
import src.lib.ioInterfaces.Log_Buffer;
import src.lib.ioInterfaces.MAQJunctionMapIterator;
import src.lib.objects.AlignedRead;
import src.lib.objects.MAQRyanMap;
import src.lib.objects.SimpleAlignedRead;

/**
 * @version $Revision: 2530 $
 * @author 
 */
public class Exon_Junction_Map {

    private Exon_Junction_Map() {
    }

    public static MAQRyanMap[] get_all_junctions_map(Log_Buffer LB, String filename) {
        Vector<MAQRyanMap> v = new Vector<MAQRyanMap>();
        MAQJunctionMapIterator m = new MAQJunctionMapIterator(LB, "JunctionMap", filename);
        for (MAQRyanMap n : new IterableIterator<MAQRyanMap>(m)) {
            v.add(n);
        }
        MAQRyanMap[] ar = new MAQRyanMap[v.size()];
        ar = v.toArray(ar);
        return ar;
    }

    /**
	 * Custom binary search algorithm to find correct mapping for junctions.
	 * @param map
	 * @param value
	 * @return
	 */
    private static int get_index(MAQRyanMap[] map, int value) {
        int low = 0;
        int high = map.length - 1;
        int lastMid = -1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (map[mid].get_fa_coord_st() < value && map[mid].get_fa_coord_end() > value) {
                return mid;
            } else {
                if (map[mid].get_fa_coord_st() < value) {
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
        return -1;
    }

    public static Vector<SimpleAlignedRead> TranslateJunction(Log_Buffer LB, AlignedRead junction, MAQRyanMap[] map, int JUNCTION_HALF_WIDTH) {
        Vector<SimpleAlignedRead> n = new Vector<SimpleAlignedRead>();
        int s = junction.get_alignStart();
        String st = junction.get_sequence();
        int idx = get_index(map, s);
        if (idx < 0) {
            LB.notice("Read start outside junction's bounds");
            return null;
        }
        int shift = s - map[idx].get_fa_coord_st();
        if (shift > JUNCTION_HALF_WIDTH || shift < 0) {
            LB.notice("Read found that does not map to junction correctly.  Skipping.");
            return null;
        }
        int left = 0;
        int right = 0;
        if (!map[idx].get_direction()) {
            try {
                st = Utilities.reverseCompliment(st);
            } catch (UnexpectedCharacterException uce) {
                LB.error(uce.getMessage());
            }
            left = map[idx].get_start() - 1;
            right = map[idx].get_end() + 2;
            byte[] prb = junction.get_prb_byte_rev();
            SimpleAlignedRead l = new SimpleAlignedRead(map[idx].get_chr(), (left - shift) + 1, left, map[idx].get_direction(), st.substring(0, shift), Utilities.part_of_byte_array(prb, 0, shift - 1));
            SimpleAlignedRead r = new SimpleAlignedRead(map[idx].get_chr(), right, right + (JUNCTION_HALF_WIDTH - shift), map[idx].get_direction(), st.substring(shift, st.length()), Utilities.part_of_byte_array(prb, shift, st.length() - 1));
            n.add(l);
            n.add(r);
        } else {
            left = map[idx].get_start() - 1;
            right = map[idx].get_end() + 2;
            SimpleAlignedRead l = new SimpleAlignedRead(map[idx].get_chr(), left - (JUNCTION_HALF_WIDTH - shift), left, map[idx].get_direction(), st.substring(0, JUNCTION_HALF_WIDTH + 1 - shift), junction.get_prb(0, JUNCTION_HALF_WIDTH + 1 - shift));
            SimpleAlignedRead r = new SimpleAlignedRead(map[idx].get_chr(), right, right + shift - 1, map[idx].get_direction(), st.substring(JUNCTION_HALF_WIDTH + 1 - shift, st.length()), junction.get_prb(JUNCTION_HALF_WIDTH + 1 - shift, st.length() - 1));
            n.add(l);
            n.add(r);
        }
        return n;
    }
}
