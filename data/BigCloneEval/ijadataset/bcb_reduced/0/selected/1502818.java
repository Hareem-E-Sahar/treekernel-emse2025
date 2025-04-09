package src.lib.FDR;

import java.io.IOException;
import java.util.Vector;
import src.lib.Utilities;
import src.lib.Graphics.GraphImage;
import src.lib.ioInterfaces.Log_Buffer;
import src.lib.objects.PeakPairIdx;
import src.projects.findPeaks.FPConstants;
import src.projects.findPeaks.PeakDataSetParent;
import src.projects.findPeaks.filewriters.RegionWriter;
import src.projects.findPeaks.objects.Compare;

/**
 * Hyperbolic control is a method by which hyperbolas are used on either side of
 * the best fit (non-linear regression regression line) to find the best values
 * for control/compare
 * 
 * @author afejes
 * @version $Revision: 2855 $
 */
public class HyperbolicControl {

    private static Log_Buffer LB;

    private static boolean display_version = true;

    private static final int ITERATIONS = 100;

    private static final float FOCUS_MAX = 1000f;

    private static final float FOCUS_MIN = 0f;

    private Compare comp = null;

    /**
     * 
     * @param logbuffer
     * @param sample
     * @param compare
     * @param file
     * @param chromosome
     * @param minimum
     * @param window_size
     * @param alpha
     * @param log_transform
     * @param keepcontrol
     */
    public HyperbolicControl(Log_Buffer logbuffer, PeakDataSetParent sample, PeakDataSetParent compare, String file, String chromosome, int minimum, int window_size, float alpha, boolean log_transform, boolean keepcontrol) {
        LB = logbuffer;
        if (display_version) {
            LB.Version("HyperbolicControl", "$Revision: 2855 $");
            display_version = false;
        }
        comp = new Compare(LB);
        comp.set_pairs(MakePairs.makepairs_window(sample, compare, window_size));
        process_data(file, alpha, log_transform, keepcontrol);
        try {
            RegionWriter rw = new RegionWriter(LB, file);
            rw.generate_region_file(sample, compare, comp.get_array(), chromosome, alpha, minimum, window_size);
            rw.close();
        } catch (IOException io) {
            LB.error("Error writing header to Region file - could not create file.");
            LB.error("Message thrown by Java environment (may be null):" + io.getMessage());
            LB.die();
        }
    }

    /**
     * 
     * @param logbuffer
     * @param pairs
     * @param file
     * @param alpha
     * @param log_transform
     * @param keepcontrol
     */
    public HyperbolicControl(Log_Buffer logbuffer, PeakPairIdx[] pairs, String file, float alpha, boolean log_transform, boolean keepcontrol) {
        LB = logbuffer;
        if (display_version) {
            LB.Version("HyperbolicControl", "$Revision: 2855 $");
            display_version = false;
        }
        comp = new Compare(LB);
        comp.set_pairs(pairs);
        process_data(file, alpha, log_transform, keepcontrol);
    }

    private void process_data(String file, float alpha, boolean log_transform, boolean keepcontrol) {
        if (log_transform) {
            comp.log_transform();
        }
        double s_s_h = 0;
        double c_s_h = 0;
        PeakPairIdx[] data = comp.get_array();
        for (PeakPairIdx ppi : data) {
            float s = ppi.get_height_1();
            float c = ppi.get_height_2();
            if (s > 0 && c > 0) {
                s_s_h += ppi.get_height_1();
                c_s_h += ppi.get_height_2();
            }
        }
        double slope = c_s_h / s_s_h;
        double intercept = 0d;
        LB.notice("Sums of paired (non-zero) peak heights.  Sample: : " + Utilities.DecimalPoints(s_s_h, 2) + " \tControl: " + Utilities.DecimalPoints(c_s_h, 2));
        LB.notice("Slope of normalization line (ratio of peak height sums): " + slope);
        int above = 0;
        int below = 0;
        int s_inz = 0;
        int c_inz = 0;
        for (PeakPairIdx ppi : data) {
            float s = ppi.get_height_1();
            float c = ppi.get_height_2();
            if (above_line(slope, intercept, s, c)) {
                above += 1;
            } else {
                below += 1;
            }
        }
        LB.debug("above: = " + above);
        LB.debug("below: = " + below);
        float ax = 0;
        LB.debug(" keepcontrol = " + keepcontrol);
        if (keepcontrol) {
            ax = compare_mode(slope, intercept, above, alpha);
        } else {
            ax = control_mode(slope, intercept, above, alpha);
        }
        LB.notice("Estimated cutoff for alpha = " + alpha + " is @ focus" + ax);
        LB.notice("Actual values - estimated number of false peaks: " + Utilities.DecimalPoints((float) c_inz / above * s_inz, 1) + "\test. percent: " + Utilities.DecimalPoints((float) c_inz / above * FPConstants.PERCENTAGE, 3) + "%");
        export_to_graph_slope(file + "_all_points_slope", comp.get_array(), (float) slope, 0f);
        PeakPairIdx[] filteredPoints = null;
        if (keepcontrol) {
            filteredPoints = filter_both((float) slope, (float) intercept, ax);
        } else {
            filteredPoints = filter_sample((float) slope, ax);
        }
        export_to_graph_slope(file + "_filtered_points", filteredPoints, (float) slope, 0f);
        int s0 = getCountSample(comp.get_array(), slope, ax, ax);
        int c0 = getCountControl(comp.get_array(), slope, ax * slope, ax * slope);
        double csnought = ((double) s0 / (c0 + s0));
        double tanos1 = Math.atan(slope);
        for (PeakPairIdx ppi : filteredPoints) {
            double newSlope1 = getSlopeSample(ppi.get_height_1(), ppi.get_height_2(), ax);
            double tanns1 = Math.atan(newSlope1);
            double ratio = tanns1 / tanos1;
            double invSlope = Math.tan((Math.PI / 2) - (((Math.PI / 2) - tanos1) * ratio));
            int c = getCountControl(comp.get_array(), invSlope, ax * slope, slope * ppi.get_height_1() * slope);
            int s = getCountSample(comp.get_array(), newSlope1, ax, ppi.get_height_1());
            ppi.set_p_value(((double) s / (c + s)) / csnought);
        }
        comp.set_pairs(filteredPoints);
        if (log_transform) {
            comp.reverse_log();
        }
        LB.notice("Linear Regresion: Remaining:\t" + comp.size());
    }

    /**
	 * @param slope
	 * @param intercept
	 * @param above
	 * @param alpha
	 * @return
	 */
    private float control_mode(double slope, double intercept, int above, float alpha) {
        float min_focus_x = FOCUS_MIN;
        float max_focus_x = FOCUS_MAX;
        float ax = (max_focus_x - min_focus_x) / 2;
        double diff = -1;
        double lastPercentage = 0;
        for (int x = 1; x <= ITERATIONS; x++) {
            int s_inz = 0;
            int c_inz = 0;
            double ay = slope * ax;
            for (PeakPairIdx ppi : comp.get_array()) {
                float s = ppi.get_height_1();
                float c = ppi.get_height_2();
                if (above_line(slope, intercept, s, c)) {
                    if (in_zone_top(slope, ay, s, c)) {
                        c_inz += 1;
                    }
                } else {
                    if (in_zone_bottom(slope, ax, s, c)) {
                        s_inz += 1;
                    }
                }
            }
            double percentage = (float) c_inz / above;
            LB.notice("zone: " + x + "\tc_inz: " + c_inz + "\ts_inz: " + s_inz + "\test. fp.: " + Utilities.DecimalPoints(percentage * s_inz, 1) + "\t%: " + Utilities.DecimalPoints(percentage * FPConstants.PERCENTAGE, 2));
            if (percentage < alpha * 1.005 && percentage > alpha * 0.995) {
                break;
            }
            if (percentage < alpha) {
                max_focus_x = ax;
            } else if (percentage > alpha) {
                min_focus_x = ax;
            }
            ax = (min_focus_x + max_focus_x) / 2;
            diff = Math.abs(lastPercentage - percentage);
            LB.debug("delta = " + diff);
            lastPercentage = percentage;
        }
        return ax;
    }

    private float compare_mode(double slope, double intercept, int above, float alpha) {
        double percentages = 0;
        float min_focus_x = FOCUS_MIN;
        float max_focus_x = FOCUS_MAX;
        float ax = (max_focus_x - min_focus_x) / 2;
        for (int x = 1; x <= ITERATIONS; x++) {
            int s_inz = 0;
            int c_inz = 0;
            double ay = slope * ax;
            for (PeakPairIdx ppi : comp.get_array()) {
                float s = ppi.get_height_1();
                float c = ppi.get_height_2();
                if (above_line(slope, intercept, s, c)) {
                    if (in_zone_top(slope, ay, s, c)) {
                        c_inz += 1;
                    }
                } else {
                    if (in_zone_bottom(slope, ax, s, c)) {
                        s_inz += 1;
                    }
                }
            }
            percentages = (float) c_inz / above;
            LB.notice("zone: " + x + "\tc_inz: " + c_inz + "\ts_inz: " + s_inz + "\test. fp.: " + Utilities.DecimalPoints(percentages * s_inz, 1) + "\t%: " + Utilities.DecimalPoints(percentages * FPConstants.PERCENTAGE, 2));
            if (percentages < alpha) {
                LB.debug("max_focus_x = ax;\t" + max_focus_x + "\t" + ax);
                max_focus_x = ax;
            } else if (percentages > alpha) {
                LB.debug("min_focus_x = ax;\t" + min_focus_x + "\t" + ax);
                min_focus_x = ax;
            }
            ax = (min_focus_x + max_focus_x) / 2;
        }
        return ax;
    }

    /**
	 * Keep peaks that are in the sample, only
	 * @param slope
	 * @param ax position of the x intercept of the hyperbola
	 * @return
	 */
    public final PeakPairIdx[] filter_sample(float slope, float ax) {
        Vector<PeakPairIdx> temp = new Vector<PeakPairIdx>();
        for (PeakPairIdx p : comp.get_array()) {
            if (p.get_pk_idx_1() != -1 && in_zone_bottom(slope, ax, p.get_height_1(), p.get_height_2())) {
                temp.add(p);
            }
        }
        return temp.toArray(new PeakPairIdx[temp.size()]);
    }

    /**
	 * Keep peaks that are in either sample or compare
	 * @param slope
	 * @param intercept the intercept of the line
	 * @param ax the y=0 value for the hyperbola
	 * @return
	 */
    public final PeakPairIdx[] filter_both(float slope, float intercept, float ax) {
        Vector<PeakPairIdx> temp = new Vector<PeakPairIdx>();
        double ay = slope * ax;
        for (PeakPairIdx p : comp.get_array()) {
            if (!above_line(slope, intercept, p.get_height_1(), p.get_height_2())) {
                if (p.get_pk_idx_1() != -1 && in_zone_bottom(slope, ax, p.get_height_1(), p.get_height_2())) {
                    temp.add(p);
                }
            } else {
                if (p.get_pk_idx_2() != -1 && in_zone_top(slope, ay, p.get_height_1(), p.get_height_2())) {
                    temp.add(p);
                }
            }
        }
        return temp.toArray(new PeakPairIdx[temp.size()]);
    }

    private static void export_to_graph_slope(String file, PeakPairIdx[] dataset, float slope, float intercept) {
        GraphImage gi = new GraphImage(LB, "png");
        for (PeakPairIdx p : dataset) {
            float x = p.get_height_1();
            float y = p.get_height_2();
            if (x < 0) {
                x = 0;
            }
            if (y < 0) {
                y = 0;
            }
            gi.add_point(x, y);
        }
        gi.add_line(slope, intercept);
        gi.write_to_disc(file + "_graph.png");
        gi.close();
    }

    private static boolean above_line(double slope, double intercept, double x, double y) {
        if (y > (slope * x + intercept)) {
            return true;
        }
        return false;
    }

    private static boolean in_zone_top(double slope, double a, double x, double y) {
        double b = slope * a;
        if (y > Math.sqrt(a * a * (((x * x) / (b * b)) + 1))) {
            return true;
        }
        return false;
    }

    private static double getSlopeSample(double x, double y, double a) {
        return Math.sqrt((y * y) / ((x * x) - (a * a)));
    }

    /**
	 * @param ppi
	 * @param slope
	 * @param a
	 * @param y
	 * @return count of peaks in control zone. 
	 */
    private static int getCountControl(PeakPairIdx[] ppi, double slope, double a, double y) {
        int count = 0;
        double b = slope * a;
        for (PeakPairIdx i : ppi) {
            if (i.get_pk_idx_1() != -1 && i.get_height_1() >= y && i.get_height_2() > a * Math.sqrt(((i.get_height_1() * i.get_height_1()) / (b * b)) + 1)) {
                count++;
            }
        }
        return count;
    }

    private static int getCountSample(PeakPairIdx[] ppi, double slope, double a, double x) {
        int count = 0;
        double b = slope * a;
        for (PeakPairIdx i : ppi) {
            if (i.get_pk_idx_1() != 1 && i.get_height_1() >= x && i.get_height_2() < b * Math.sqrt(((i.get_height_1() * i.get_height_1()) / (a * a)) - 1)) {
                count++;
            }
        }
        return count;
    }

    private static boolean in_zone_bottom(double slope, double a, double x, double y) {
        double b = slope * a;
        if (y < Math.sqrt(b * b * (((x * x) / (a * a)) - 1))) {
            return true;
        }
        return false;
    }

    /**
	 * this should be deprecated or replaced.
	 * @return
	 */
    public Compare get_compare() {
        return this.comp;
    }
}
