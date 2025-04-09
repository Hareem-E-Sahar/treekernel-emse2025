package mindtct;

import java.awt.Rectangle;
import java.io.PrintStream;
import java.util.Comparator;
import mindtct.Contour.ContourSearchResult;
import mindtct.Contour.MinContourThetaResult;
import mindtct.Lfs.LFSPARMS;
import mindtct.Lfs.MinutiaApp;
import mindtct.Lfs.MinutiaType;
import mindtct.Lfs.RectilinearScanDirection;
import mindtct.Lfs.RotationalScanDirection;
import mindtct.Loop.LoopDirection;
import mindtct.Matchpat.HorizontalSkipResult;
import mindtct.Matchpat.VerticalSkipResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** //TODO OPT replace minutia.get(i) calls with reference to single MINUTIA object
 * @author mchaberski
 *
 */
public class MinutiaUtility {

    private static Log log = LogFactory.getLog(MinutiaUtility.class);

    public static enum UpdateResult {

        UPDATE_SUCCESS, UPDATE_IGNORE
    }

    ;

    /*************************************************************************
	**************************************************************************
	#cat: detect_minutiae_V2 - Takes a binary image and its associated
	#cat:            Direction and Low Flow Maps and scans each image block
	#cat:            with valid direction for minutia points.  Minutia points
	#cat:            detected in LOW FLOW blocks are set with lower reliability.

	   Input:
	      bdata     - binary image data (0==while & 1==black)
	      iw        - width (in pixels) of image
	      ih        - height (in pixels) of image
	      direction_map  - map of image blocks containing directional ridge flow
	      low_flow_map   - map of image blocks flagged as LOW RIDGE FLOW
	      high_curve_map - map of image blocks flagged as HIGH CURVATURE
	      mw        - width (in blocks) of the maps
	      mh        - height (in blocks) of the maps
	      lfsparms  - parameters and thresholds for controlling LFS
	   Output:
	      minutiae   - points to a list of detected minutia structures
	   Return Code:
	      Zero      - successful completion
	      Negative  - system error
	 ************************************************************************
	**/
    public static void detect_minutiae_V2(MINUTIAE minutiae, boolean[] bdata, final int iw, final int ih, int[] direction_map, int[] low_flow_map, int[] high_curve_map, final int mw, final int mh, final LFSPARMS lfsparms) {
        int[] pdirection_map, plow_flow_map, phigh_curve_map;
        pdirection_map = Maps.pixelize_map(iw, ih, direction_map, mw, mh, lfsparms.blocksize);
        plow_flow_map = Maps.pixelize_map(iw, ih, low_flow_map, mw, mh, lfsparms.blocksize);
        phigh_curve_map = Maps.pixelize_map(iw, ih, high_curve_map, mw, mh, lfsparms.blocksize);
        scan4minutiae_horizontally_V2(minutiae, bdata, iw, ih, pdirection_map, plow_flow_map, phigh_curve_map, lfsparms);
        scan4minutiae_vertically_V2(minutiae, bdata, iw, ih, pdirection_map, plow_flow_map, phigh_curve_map, lfsparms);
    }

    /*************************************************************************
	**************************************************************************
	#cat: update_minutiae - Takes a detected minutia point and (if it is not
	#cat:                determined to already be in the minutiae list) adds it to
	#cat:                the list.

	   Input:
	      minutia   - minutia structure for detected point
	      bdata     - binary image data (0==while & 1==black)
	      iw        - width (in pixels) of image
	      ih        - height (in pixels) of image
	      lfsparms  - parameters and thresholds for controlling LFS
	   Output:
	      minutiae   - points to a list of detected minutia structures
	   Return Code:
	      Zero      - minutia added to successfully added to minutiae list
	      IGNORE    - minutia is to be ignored (already in the minutiae list)
	      Negative  - system error
	**************************************************************************/
    public static UpdateResult update_minutiae(MINUTIAE minutiae, MINUTIA minutia, boolean[] bdata, final int iw, final int ih, final LFSPARMS lfsparms) {
        final int qtr_ndirs = lfsparms.num_directions / 4;
        final int full_ndirs = lfsparms.num_directions * 2;
        if (minutiae.getNum() > 0) {
            for (int i = 0; i < minutiae.getNum(); i++) {
                final int dx = Math.abs(minutiae.get(i).y - minutia.y);
                if (dx < lfsparms.max_minutia_delta) {
                    final int dy = Math.abs(minutiae.get(i).y - minutia.y);
                    if (dy < lfsparms.max_minutia_delta) {
                        if (minutiae.get(i).type == minutia.type) {
                            int delta_dir = Math.abs(minutiae.get(i).direction - minutia.direction);
                            delta_dir = Math.min(delta_dir, full_ndirs - delta_dir);
                            if (delta_dir <= qtr_ndirs) {
                                if ((dx == 0) && (dy == 0)) {
                                    return (UpdateResult.UPDATE_IGNORE);
                                }
                                boolean contourSearchResult = Contour.search_contour(minutia.x, minutia.y, lfsparms.max_minutia_delta, minutiae.get(i).x, minutiae.get(i).y, minutiae.get(i).ex, minutiae.get(i).ey, RotationalScanDirection.SCAN_CLOCKWISE, bdata, iw, ih);
                                if (contourSearchResult) {
                                    return UpdateResult.UPDATE_IGNORE;
                                }
                                if (Contour.search_contour(minutia.x, minutia.y, lfsparms.max_minutia_delta, minutiae.get(i).x, minutiae.get(i).y, minutiae.get(i).ex, minutiae.get(i).ey, RotationalScanDirection.SCAN_COUNTERCLOCKWISE, bdata, iw, ih)) {
                                    return (UpdateResult.UPDATE_IGNORE);
                                }
                            }
                        }
                    }
                }
            }
        }
        minutiae.add(minutia);
        return UpdateResult.UPDATE_SUCCESS;
    }

    /**
	 * @param minutiae
	 * @param minutia
	 * @param scan_dir
	 * @param dmapval
	 * @param bdata
	 * @param iw
	 * @param ih
	 * @param lfsparms
	 * @return true if list updated, false if minutia is to be ignored (comparable to 
	 * returning IGNORE in original)
	 */
    private static UpdateResult update_minutiae_V2(MINUTIAE minutiae, MINUTIA minutia, final RectilinearScanDirection scan_dir, final int dmapval, boolean[] bdata, final int iw, final int ih, final LFSPARMS lfsparms) {
        final int qtr_ndirs = lfsparms.num_directions / 4;
        final int full_ndirs = lfsparms.num_directions * 2;
        if (minutiae.getNum() > 0) {
            for (int i = minutiae.getNum() - 1; i >= 0; i--) {
                final int dx = Math.abs(minutiae.get(i).x - minutia.x);
                if (dx < lfsparms.max_minutia_delta) {
                    final int dy = Math.abs(minutiae.get(i).y - minutia.y);
                    if (dy < lfsparms.max_minutia_delta) {
                        if (minutiae.get(i).type == minutia.type) {
                            final int delta_dir_tmp = Math.abs(minutiae.get(i).direction - minutia.direction);
                            final int delta_dir = Math.min(delta_dir_tmp, full_ndirs - delta_dir_tmp);
                            if (delta_dir <= qtr_ndirs) {
                                if ((dx == 0) && (dy == 0)) {
                                    return (UpdateResult.UPDATE_IGNORE);
                                }
                                if (Contour.search_contour(minutia.x, minutia.y, lfsparms.max_minutia_delta, minutiae.get(i).x, minutiae.get(i).y, minutiae.get(i).ex, minutiae.get(i).ey, RotationalScanDirection.SCAN_CLOCKWISE, bdata, iw, ih) || Contour.search_contour(minutia.x, minutia.y, lfsparms.max_minutia_delta, minutiae.get(i).x, minutiae.get(i).y, minutiae.get(i).ex, minutiae.get(i).ey, RotationalScanDirection.SCAN_COUNTERCLOCKWISE, bdata, iw, ih)) {
                                    if (dmapval >= 0) {
                                        final RectilinearScanDirection map_scan_dir = choose_scan_direction(dmapval, lfsparms.num_directions);
                                        if (map_scan_dir == scan_dir) {
                                            minutiae.remove(i);
                                            log.debug("removing minutia at index i = " + i);
                                        } else return (UpdateResult.UPDATE_IGNORE);
                                    } else {
                                        return (UpdateResult.UPDATE_IGNORE);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        minutiae.add(minutia);
        return UpdateResult.UPDATE_SUCCESS;
    }

    /*************************************************************************
	**************************************************************************
	#cat: sort_minutiae_y_x - Takes a list of minutia points and sorts them
	#cat:                 top-to-bottom and then left-to-right.

	   Input:
	      minutiae  - list of minutiae
	      iw        - width (in pixels) of image
	      ih        - height (in pixels) of image
	   Output:
	      minutiae  - list of sorted minutiae
	   Return Code:
	      Zero     - successful completion
	      Negative - system error
	**************************************************************************/
    public static void sort_minutiae_y_x(MINUTIAE minutiae, final int iw, final int ih) {
        minutiae.sort(MINUTIAComparatorFactory.getYXComparator());
    }

    /*************************************************************************
	**************************************************************************
	#cat: sort_minutiae_x_y - Takes a list of minutia points and sorts them
	#cat:                 left-to-right and then top-to-bottom.

	   Input:
	      minutiae  - list of minutiae
	      iw        - width (in pixels) of image
	      ih        - height (in pixels) of image
	   Output:
	      minutiae  - list of sorted minutiae
	   Return Code:
	      Zero     - successful completion
	      Negative - system error
	**************************************************************************/
    public static void sort_minutiae_x_y(MINUTIAE minutiae, final int iw, final int ih) {
        minutiae.sort(MINUTIAComparatorFactory.getXYComparator());
    }

    private static class MINUTIAComparatorFactory {

        private static Comparator<MINUTIA> xyComparator;

        private static Comparator<MINUTIA> yxComparator;

        /** 
		 * @return sorter left-to-right and then top-to-bottom.
		 */
        public static Comparator<MINUTIA> getXYComparator() {
            if (xyComparator == null) {
                xyComparator = new Comparator<MINUTIA>() {

                    public int compare(MINUTIA o1, MINUTIA o2) {
                        return (o1.x != o2.x) ? o1.x - o2.x : o1.y - o2.y;
                    }
                };
            }
            return xyComparator;
        }

        /** 
		 * @return sorter top-to-bottom and then left-to-right.
		 */
        public static Comparator<MINUTIA> getYXComparator() {
            if (yxComparator == null) {
                yxComparator = new Comparator<MINUTIA>() {

                    public int compare(MINUTIA o1, MINUTIA o2) {
                        return (o1.y != o2.y) ? o1.y - o2.y : o1.x - o2.x;
                    }
                };
            }
            return yxComparator;
        }
    }

    /*************************************************************************
	**************************************************************************
	#cat: dump_minutiae - Given a minutiae list, writes a formatted text report of
	#cat:            the list's contents to the specified open file pointer.

	   Input:
	      minutiae - list of minutia structures
	   Output:
	      fpout - open file pointer
	**************************************************************************/
    public static void dump_minutiae(PrintStream fpout, final MINUTIAE minutiae) {
        throw new UnsupportedOperationException();
    }

    /*************************************************************************
	**************************************************************************
	#cat: dump_minutiae_pts - Given a minutiae list, writes the coordinate point
	#cat:            for each minutia in the list to the specified open
	#cat:            file pointer.

	   Input:
	      minutiae - list of minutia structures
	   Output:
	      fpout - open file pointer
	**************************************************************************/
    private static void dump_minutiae_pts(PrintStream fpout, final MINUTIAE minutiae) {
        throw new UnsupportedOperationException();
    }

    /*************************************************************************
	**************************************************************************
	#cat: dump_reliable_minutiae_pts - Given a minutiae list, writes the
	#cat:            coordinate point for each minutia in the list that has
	#cat:            the specified reliability to the specified open
	#cat:            file pointer.

	   Input:
	      minutiae    - list of minutia structures
	      reliability - desired reliability level for minutiae to be reported
	   Output:
	      fpout - open file pointer
	**************************************************************************/
    private static void dump_reliable_minutiae_pts(PrintStream fpout, final MINUTIAE minutiae, final double reliability) {
        throw new UnsupportedOperationException();
    }

    /*************************************************************************
	**************************************************************************
	#cat: minutia_type - Given the pixel color of the detected feature, returns
	#cat:            whether the minutia is a ridge-ending (black pixel) or
	#cat:            bifurcation (white pixel).

	   Input:
	      feature_pix - pixel color of the feature's interior
	   Return Code:
	      RIDGE_ENDING - minutia is a ridge-ending
	      BIFURCATION  - minutia is a bifurcation (valley-ending)
	**************************************************************************/
    public static MinutiaType minutia_type(final boolean feature_pix) {
        final MinutiaType type;
        if (feature_pix == Lfs.WHITE_PIXEL_BOOL) {
            type = MinutiaType.BIFURCATION;
        } else {
            assert feature_pix == Lfs.BLACK_PIXEL_BOOL;
            type = MinutiaType.RIDGE_ENDING;
        }
        return (type);
    }

    /*************************************************************************
	**************************************************************************
	#cat: is_minutia_appearing - Given the pixel location of a minutia feature
	#cat:            and its corresponding adjacent edge pixel, returns whether
	#cat:            the minutia is appearing or disappearing.  Remeber, that
	#cat:            "feature" refers to either a ridge or valley-ending.

	   Input:
	      x_loc      - x-pixel coord of feature (interior to feature)
	      y_loc      - y-pixel coord of feature (interior to feature)
	      x_edge     - x-pixel coord of corresponding edge pixel
	                   (exterior to feature)
	      y_edge     - y-pixel coord of corresponding edge pixel
	                   (exterior to feature)
	   Return Code:
	      APPEARING    - minutia is appearing (TRUE==1)
	      DISAPPEARING - minutia is disappearing (FALSE==0)
	      Negative     - system error
	**************************************************************************/
    public static MinutiaApp is_minutia_appearing(final int x_loc, final int y_loc, final int x_edge, final int y_edge) {
        assert x_loc != x_edge && y_loc != y_edge;
        if (x_edge < x_loc) return (MinutiaApp.APPEARING);
        if (x_edge > x_loc) return (MinutiaApp.DISAPPEARING);
        if (y_edge < y_loc) return (MinutiaApp.APPEARING);
        if (y_edge > y_loc) return (MinutiaApp.DISAPPEARING);
        throw new PixelConfigurationException(String.format("x_loc=%d, y_loc=%d, x_edge=%d, y_edge=%d", x_loc, y_loc, x_edge, y_edge));
    }

    /*************************************************************************
	**************************************************************************
	#cat: choose_scan_direction - Determines the orientation (horizontal or
	#cat:            vertical) in which a block is to be scanned for minutiae.
	#cat:            The orientation is based on the blocks corresponding IMAP
	#cat:            direction.

	   Input:
	      imapval   - Block's IMAP direction
	      ndirs     - number of possible IMAP directions (within semicircle)
	   Return Code:
	      SCAN_HORIZONTAL - horizontal orientation
	      SCAN_VERTICAL   - vertical orientation
	**************************************************************************/
    public static Lfs.RectilinearScanDirection choose_scan_direction(final int imapval, final int ndirs) {
        int qtr_ndirs;
        qtr_ndirs = ndirs >> 2;
        if ((imapval <= qtr_ndirs) || (imapval > (qtr_ndirs * 3))) return (Lfs.RectilinearScanDirection.SCAN_HORIZONTAL); else return (Lfs.RectilinearScanDirection.SCAN_VERTICAL);
    }

    /*************************************************************************
	**************************************************************************
	#cat: scan4minutiae_horizontally_V2 - Scans an entire binary image
	#cat:                horizontally, detecting potential minutiae points.
	#cat:                Minutia detected via the horizontal scan process are
	#cat:                by nature vertically oriented (orthogonal to the scan).

	   Input:
	      bdata     - binary image data (0==while & 1==black)
	      iw        - width (in pixels) of image
	      ih        - height (in pixels) of image
	      pdirection_map  - pixelized Direction Map
	      plow_flow_map   - pixelized Low Ridge Flow Map
	      phigh_curve_map - pixelized High Curvature Map
	      lfsparms  - parameters and thresholds for controlling LFS
	   Output:
	      minutiae   - points to a list of detected minutia structures
	   Return Code:
	      Zero      - successful completion
	      Negative  - system error
	**************************************************************************/
    private static void scan4minutiae_horizontally_V2(MINUTIAE minutiae, boolean[] bdata, final int iw, final int ih, int[] pdirection_map, int[] plow_flow_map, int[] phigh_curve_map, final LFSPARMS lfsparms) {
        final int sx = 0;
        final int ex = iw;
        final int sy = 0;
        final int ey = ih;
        int cy = sy;
        while (cy + 1 < ey) {
            int cx = sx;
            while (cx < ex) {
                int p1index = cy * iw + cx, p2index = (cy + 1) * iw + cx;
                assert p2index < bdata.length;
                boolean p1value = bdata[p1index], p2value = bdata[p2index];
                int[] possible = new int[Lfs.NFEATURES];
                int nposs;
                if ((nposs = Matchpat.match_1st_pair(p1value, p2value, possible)) != 0) {
                    cx++;
                    p1index++;
                    p2index++;
                    if (cx < ex) {
                        p1value = bdata[p1index];
                        p2value = bdata[p2index];
                        if ((nposs = Matchpat.match_2nd_pair(p1value, p2value, possible, nposs)) != 0) {
                            int x2 = cx;
                            HorizontalSkipResult skipResult = Matchpat.skip_repeated_horizontal_pair(bdata, cx, ex, p1index, p2index, iw, ih);
                            cx = skipResult.cx;
                            p1index = skipResult.p1index;
                            p2index = skipResult.p2index;
                            p1value = bdata[p1index];
                            p2value = bdata[p2index];
                            if (cx < ex) {
                                if ((nposs = Matchpat.match_3rd_pair(p1value, p2value, possible, nposs)) != 0) {
                                    ScanResult scanResult = process_horizontal_scan_minutia_V2(minutiae, cx, cy, x2, possible[0], bdata, iw, ih, pdirection_map, plow_flow_map, phigh_curve_map, lfsparms);
                                }
                                if (p1value != p2value) {
                                    cx--;
                                }
                            }
                        }
                    }
                } else {
                    cx++;
                }
            }
            cy++;
        }
    }

    /*************************************************************************
	**************************************************************************
	#cat: scan4minutiae_vertically_V2 - Scans an entire binary image
	#cat:                vertically, detecting potential minutiae points.
	#cat:                Minutia detected via the vetical scan process are
	#cat:                by nature horizontally oriented (orthogonal to  the scan).

	   Input:
	      bdata     - binary image data (0==while & 1==black)
	      iw        - width (in pixels) of image
	      ih        - height (in pixels) of image
	      pdirection_map  - pixelized Direction Map
	      plow_flow_map   - pixelized Low Ridge Flow Map
	      phigh_curve_map - pixelized High Curvature Map
	      lfsparms  - parameters and thresholds for controlling LFS
	   Output:
	      minutiae   - points to a list of detected minutia structures
	   Return Code:
	      Zero      - successful completion
	      Negative  - system error
	**************************************************************************/
    private static void scan4minutiae_vertically_V2(MINUTIAE minutiae, boolean[] bdata, final int iw, final int ih, int[] pdirection_map, int[] plow_flow_map, int[] phigh_curve_map, final LFSPARMS lfsparms) {
        assert bdata.length > 1;
        final int sx = 0;
        final int ex = iw;
        final int sy = 0;
        final int ey = ih;
        int possible[] = new int[Lfs.NFEATURES];
        int cx = sx;
        while (cx + 1 < ex) {
            int cy = sy;
            while (cy < ey) {
                int p1index = (cy * iw) + cx;
                int p2index = (p1index + 1);
                assert p1index < bdata.length && p2index < bdata.length : String.format("\n\tp1index = %d and p2index = %d" + " must be < bdata.length = %d", p1index, p2index, bdata.length) + String.format("\n\tcx = %d, ex = %d", cx, ex) + String.format("\n\tcy = %d, ey = %d", cy, ey);
                boolean p1value = bdata[p1index];
                boolean p2value = bdata[p2index];
                int nposs;
                if ((nposs = Matchpat.match_1st_pair(p1value, p2value, possible)) > 0) {
                    cy++;
                    p1index += iw;
                    p2index += iw;
                    if (cy < ey) {
                        assert p1index < bdata.length && p2index < bdata.length : String.format("\n\tp1index = %d and p2index = %d" + " must be < bdata.length = %d", p1index, p2index, bdata.length) + String.format("\n\tcx = %d, ex = %d", cx, ex) + String.format("\n\tcy = %d, ey = %d", cy, ey);
                        p1value = bdata[p1index];
                        p2value = bdata[p2index];
                        if ((nposs = Matchpat.match_2nd_pair(p1value, p2value, possible, nposs)) > 0) {
                            int y2 = cy;
                            VerticalSkipResult vSkipResult = Matchpat.skip_repeated_vertical_pair(bdata, cy, ey, p1index, p2index, iw, ih);
                            cy = vSkipResult.cy;
                            p1index = vSkipResult.p1index;
                            p2index = vSkipResult.p2index;
                            p1value = bdata[p1index];
                            p2value = bdata[p2index];
                            if (cy < ey) {
                                if ((nposs = Matchpat.match_3rd_pair(p1value, p2value, possible, nposs)) > 0) {
                                    ProcessVerticalResult vProcessResult = process_vertical_scan_minutia_V2(minutiae, cx, cy, y2, possible[0], bdata, iw, ih, pdirection_map, plow_flow_map, phigh_curve_map, lfsparms);
                                    log.debug("process vertical result = " + vProcessResult);
                                }
                                if (p1value != p2value) {
                                    cy--;
                                }
                            }
                        }
                    }
                } else {
                    cy++;
                }
            }
            cx++;
        }
    }

    /*************************************************************************
	**************************************************************************
	#cat: adjust_horizontal_rescan - Determines the portion of an image block to
	#cat:                be rescanned horizontally based on a specified neighbor.

	   Input:
	      nbr_dir   - specifies which block neighbor {NORTH, SOUTH, EAST, WEST}
	      scan_x    - x-pixel coord of origin of image region
	      scan_y    - y-pixel coord of origin of image region
	      scan_w    - width (in pixels) of image region
	      scan_h    - height (in pixels) of image region
	      blocksize - dimension of image blocks (in pixels)
	   Output:
	      rescan_x    - x-pixel coord of origin of region to be rescanned
	      rescan_y    - y-pixel coord of origin of region to be rescanned
	      rescan_w    - width (in pixels) of region to be rescanned
	      rescan_h    - height (in pixels) of region to be rescanned
	   Return Code:
	      Zero      - successful completion
	      Negative  - system error
	**************************************************************************/
    private static void adjust_horizontal_rescan(final int nbr_dir, Rectangle rescan, final int scan_x, final int scan_y, final int scan_w, final int scan_h, final int blocksize) {
        throw new UnsupportedOperationException();
    }

    /*************************************************************************
	**************************************************************************
	#cat: adjust_vertical_rescan - Determines the portion of an image block to
	#cat:                be rescanned vertically based on a specified neighbor.

	   Input:
	      nbr_dir   - specifies which block neighbor {NORTH, SOUTH, EAST, WEST}
	      scan_x    - x-pixel coord of origin of image region
	      scan_y    - y-pixel coord of origin of image region
	      scan_w    - width (in pixels) of image region
	      scan_h    - height (in pixels) of image region
	      blocksize - dimension of image blocks (in pixels)
	   Output:
	      rescan_x    - x-pixel coord of origin of region to be rescanned
	      rescan_y    - y-pixel coord of origin of region to be rescanned
	      rescan_w    - width (in pixels) of region to be rescanned
	      rescan_h    - height (in pixels) of region to be rescanned
	   Return Code:
	      Zero      - successful completion
	      Negative  - system error
	**************************************************************************/
    private static void adjust_vertical_rescan(final int nbr_dir, Rectangle rescan, final int scan_x, final int scan_y, final int scan_w, final int scan_h, final int blocksize) {
        throw new UnsupportedOperationException();
    }

    private static enum ScanResult {

        SUCCESS, IGNORE
    }

    ;

    /**
	 * @param minutiae
	 * @param cx
	 * @param cy
	 * @param x2
	 * @param feature_id
	 * @param bdata
	 * @param iw
	 * @param ih
	 * @param pdirection_map
	 * @param plow_flow_map
	 * @param phigh_curve_map
	 * @param lfsparms
	 * @return true if minutia is to be ignored, false if successful completion (successful processing)
	 */
    private static ScanResult process_horizontal_scan_minutia_V2(MINUTIAE minutiae, final int cx, final int cy, final int x2, final int feature_id, boolean[] bdata, final int iw, final int ih, int[] pdirection_map, int[] plow_flow_map, int[] phigh_curve_map, final LFSPARMS lfsparms) {
        int x_loc = (cx + x2) / 2;
        int x_edge = x_loc;
        int y_loc, y_edge;
        if (Matchpat.feature_patterns[feature_id].appearing == MinutiaApp.APPEARING) {
            y_loc = cy + 1;
            y_edge = cy;
        } else {
            y_loc = cy;
            y_edge = cy + 1;
        }
        int dmapval = pdirection_map[y_loc * iw + x_loc];
        int fmapval = plow_flow_map[y_loc * iw + x_loc];
        int cmapval = phigh_curve_map[y_loc * iw + x_loc];
        if (dmapval == Lfs.INVALID_DIR) return (ScanResult.IGNORE);
        int idir;
        if (cmapval != 0) {
            AdjustmentResult adjustmentResult;
            adjustmentResult = adjust_high_curvature_minutia_V2(x_loc, y_loc, x_edge, y_edge, bdata, iw, ih, plow_flow_map, minutiae, lfsparms);
            if (adjustmentResult.status == AdjustmentResult.Status.IGNORE) {
                return ScanResult.IGNORE;
            }
            x_loc = adjustmentResult.ox_loc;
            y_loc = adjustmentResult.oy_loc;
            x_edge = adjustmentResult.ox_edge;
            y_edge = adjustmentResult.oy_edge;
            idir = adjustmentResult.oidir;
        } else {
            idir = get_low_curvature_direction(RectilinearScanDirection.SCAN_HORIZONTAL, Matchpat.feature_patterns[feature_id].appearing, dmapval, lfsparms.num_directions);
        }
        double reliability;
        if (fmapval != 0) {
            reliability = Lfs.MEDIUM_RELIABILITY;
        } else reliability = Lfs.HIGH_RELIABILITY;
        MINUTIA minutia = new MINUTIA(x_loc, y_loc, x_edge, y_edge, idir, reliability, Matchpat.feature_patterns[feature_id].type, Matchpat.feature_patterns[feature_id].appearing, feature_id);
        UpdateResult updateResult = update_minutiae_V2(minutiae, minutia, RectilinearScanDirection.SCAN_HORIZONTAL, dmapval, bdata, iw, ih, lfsparms);
        if (updateResult == UpdateResult.UPDATE_IGNORE) {
        }
        return ScanResult.SUCCESS;
    }

    /**
	 * @param minutiae
	 * @param cx
	 * @param cy
	 * @param y2
	 * @param feature_id
	 * @param bdata
	 * @param iw
	 * @param ih
	 * @param pdirection_map
	 * @param plow_flow_map
	 * @param phigh_curve_map
	 * @param lfsparms
	 * @return true if minutia is to be ignored, false if successful completion
	 */
    private static enum ProcessVerticalResult {

        SUCCESS, IGNORE
    }

    ;

    private static ProcessVerticalResult process_vertical_scan_minutia_V2(MINUTIAE minutiae, final int cx, final int cy, final int y2, final int feature_id, boolean[] bdata, final int iw, final int ih, int[] pdirection_map, int[] plow_flow_map, int[] phigh_curve_map, final LFSPARMS lfsparms) {
        int x_loc, y_loc;
        int x_edge, y_edge;
        if (Matchpat.feature_patterns[feature_id].appearing == MinutiaApp.APPEARING) {
            x_loc = cx + 1;
            x_edge = cx;
        } else {
            x_loc = cx;
            x_edge = cx + 1;
        }
        y_loc = (cy + y2) >> 1;
        y_edge = y_loc;
        int dmapval, fmapval, cmapval;
        final int mapval_index = y_loc * iw + x_loc;
        dmapval = pdirection_map[mapval_index];
        fmapval = plow_flow_map[mapval_index];
        cmapval = phigh_curve_map[mapval_index];
        if (dmapval == Lfs.INVALID_DIR) return (ProcessVerticalResult.IGNORE);
        final int idir;
        if (cmapval != 0) {
            AdjustmentResult adjustmentResult = adjust_high_curvature_minutia_V2(x_loc, y_loc, x_edge, y_edge, bdata, iw, ih, plow_flow_map, minutiae, lfsparms);
            x_loc = adjustmentResult.ox_loc;
            y_loc = adjustmentResult.oy_loc;
            x_edge = adjustmentResult.ox_edge;
            y_edge = adjustmentResult.oy_edge;
            idir = adjustmentResult.oidir;
            if (adjustmentResult.status == AdjustmentResult.Status.IGNORE) {
                return ProcessVerticalResult.IGNORE;
            }
        } else {
            idir = get_low_curvature_direction(RectilinearScanDirection.SCAN_VERTICAL, Matchpat.feature_patterns[feature_id].appearing, dmapval, lfsparms.num_directions);
        }
        final double reliability;
        if (fmapval != 0) {
            reliability = Lfs.MEDIUM_RELIABILITY;
        } else {
            reliability = Lfs.HIGH_RELIABILITY;
        }
        MINUTIA minutia = new MINUTIA(x_loc, y_loc, x_edge, y_edge, idir, reliability, Matchpat.feature_patterns[feature_id].type, Matchpat.feature_patterns[feature_id].appearing, feature_id);
        UpdateResult updateResult = update_minutiae_V2(minutiae, minutia, RectilinearScanDirection.SCAN_VERTICAL, dmapval, bdata, iw, ih, lfsparms);
        if (updateResult == UpdateResult.UPDATE_IGNORE) {
        }
        return ProcessVerticalResult.SUCCESS;
    }

    private static class AdjustmentResult {

        public static enum Status {

            SUCCESS, IGNORE
        }

        ;

        public final int oidir, ox_loc, oy_loc, ox_edge, oy_edge;

        public final Status status;

        AdjustmentResult(int oidir, int ox_edge, int ox_loc, int oy_edge, int oy_loc, Status status) {
            this.oidir = oidir;
            this.ox_edge = ox_edge;
            this.ox_loc = ox_loc;
            this.oy_edge = oy_edge;
            this.oy_loc = oy_loc;
            this.status = status;
        }

        public static final AdjustmentResult IGNORE = new AdjustmentResult(Integer.MIN_VALUE, -1, -1, -1, -1, Status.IGNORE);
    }

    /*************************************************************************
	**************************************************************************
	#cat: adjust_high_curvature_minutia_V2 - Takes an initial minutia point
	#cat:            in a high-curvature area and adjusts its location and
	#cat:            direction.  First, it walks and extracts the contour
	#cat:            of the detected feature looking for and processing any loop
	#cat:            discovered along the way.  Once the contour is extracted,
	#cat:            the point of highest-curvature is determined and used to
	#cat:            adjust the location of the minutia point.  The angle of
	#cat:            the line perpendicular to the tangent on the high-curvature
	#cat:            contour at the minutia point is used as the mintutia's
	#cat:            direction.

	   Input:
	      x_loc     - starting x-pixel coord of feature (interior to feature)
	      y_loc     - starting y-pixel coord of feature (interior to feature)
	      x_edge    - x-pixel coord of corresponding edge pixel
	                  (exterior to feature)
	      y_edge    - y-pixel coord of corresponding edge pixel
	                  (exterior to feature)
	      bdata     - binary image data (0==while & 1==black)
	      iw        - width (in pixels) of image
	      ih        - height (in pixels) of image
	      plow_flow_map - pixelized Low Ridge Flow Map
	      lfsparms  - parameters and thresholds for controlling LFS
	   Output:
	      oidir     - direction of adjusted minutia point
	      ox_loc    - adjusted x-pixel coord of feature
	      oy_loc    - adjusted y-pixel coord of feature
	      ox_edge   - adjusted x-pixel coord of corresponding edge pixel
	      oy_edge   - adjusted y-pixel coord of corresponding edge pixel
	      minutiae   - points to a list of detected minutia structures
	   Return Code:
	      Zero      - minutia point processed successfully
	      IGNORE    - minutia point is to be ignored
	      Negative  - system error
	**************************************************************************/
    private static AdjustmentResult adjust_high_curvature_minutia_V2(final int x_loc, final int y_loc, final int x_edge, final int y_edge, boolean[] bdata, final int iw, final int ih, int[] plow_flow_map, MINUTIAE minutiae, final LFSPARMS lfsparms) throws ContourSearchResultException {
        final int half_contour = lfsparms.high_curve_half_contour;
        final int angle_edge = half_contour / 2;
        final boolean feature_pix = bdata[y_loc * iw + x_loc];
        ContourSearchResult contourSearchResult = Contour.get_high_curvature_contour(half_contour, x_loc, y_loc, x_edge, y_edge, bdata, iw, ih);
        int[] contour_x = contourSearchResult.contour_x, contour_y = contourSearchResult.contour_y, contour_ex = contourSearchResult.contour_ex, contour_ey = contourSearchResult.contour_ey;
        int ncontour = contourSearchResult.ncontour;
        if (contourSearchResult.status != ContourSearchResult.Status.SUCCESS) {
            if (contourSearchResult.status == ContourSearchResult.Status.LOOP_FOUND) {
                if (Loop.is_loop_clockwise(contour_x, contour_y, ncontour, LoopDirection.CLOCKWISE) != LoopDirection.CLOCKWISE) {
                    return (AdjustmentResult.IGNORE);
                }
                Loop.process_loop_V2(minutiae, contour_x, contour_y, contour_ex, contour_ey, ncontour, bdata, iw, ih, plow_flow_map, lfsparms);
                return AdjustmentResult.IGNORE;
            }
            throw new ContourSearchResultException("Not a loop, so get_high_curvature_contour should have incurred a system error earlier.");
        }
        if (ncontour == 0) return (AdjustmentResult.IGNORE);
        MinContourThetaResult result;
        result = Contour.min_contour_theta(angle_edge, contour_x, contour_y, ncontour);
        if (result.status == MinContourThetaResult.Status.IGNORE) {
            return AdjustmentResult.IGNORE;
        }
        final double min_theta = result.omin_theta;
        final int min_i = result.omin_i;
        if (min_theta >= lfsparms.max_high_curve_theta) {
            return (AdjustmentResult.IGNORE);
        }
        final int mid_x = (contour_x[min_i - angle_edge] + contour_x[min_i + angle_edge]) >> 1;
        final int mid_y = (contour_y[min_i - angle_edge] + contour_y[min_i + angle_edge]) >> 1;
        final boolean mid_pix = bdata[mid_y * iw + mid_x];
        if (mid_pix != feature_pix) {
            return (AdjustmentResult.IGNORE);
        }
        final int idir = Util.line2direction(contour_x[min_i], contour_y[min_i], mid_x, mid_y, lfsparms.num_directions);
        AdjustmentResult successResult = new AdjustmentResult(idir, contour_x[min_i], contour_y[min_i], contour_ex[min_i], contour_ey[min_i], AdjustmentResult.Status.SUCCESS);
        return successResult;
    }

    /*************************************************************************
	**************************************************************************
	#cat: get_low_curvature_direction - Converts a bi-direcitonal IMAP direction
	#cat:            (based on a semi-circle) to a uni-directional value covering
	#cat:            a full circle based on the scan orientation used to detect
	#cat:            a minutia feature (horizontal or vertical) and whether the
	#cat:            detected minutia is appearing or disappearing.

	   Input:
	      scan_dir   - designates the feature scan orientation
	      appearing  - designates the minutia as appearing or disappearing
	      imapval    - IMAP block direction
	      ndirs      - number of IMAP directions (in semicircle)
	   Return Code:
	      New direction - bi-directonal integer direction on full circle
	*************************************************************************/
    private static int get_low_curvature_direction(final RectilinearScanDirection scan_dir, final MinutiaApp appearing, final int imapval, final int ndirs) {
        assert appearing == MinutiaApp.APPEARING || appearing == MinutiaApp.DISAPPEARING;
        int idir;
        idir = imapval;
        if (imapval <= (ndirs / 2)) {
            if (scan_dir == RectilinearScanDirection.SCAN_HORIZONTAL) {
                if (appearing == MinutiaApp.APPEARING) {
                    idir += ndirs;
                }
            } else {
                if (appearing != MinutiaApp.APPEARING) {
                    idir += ndirs;
                }
            }
        } else {
            if (scan_dir == RectilinearScanDirection.SCAN_HORIZONTAL) {
                if (appearing != MinutiaApp.APPEARING) {
                    idir += ndirs;
                }
            } else {
                if (appearing != MinutiaApp.APPEARING) {
                    idir += ndirs;
                }
            }
        }
        return (idir);
    }
}
