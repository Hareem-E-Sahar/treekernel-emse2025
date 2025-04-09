package src.projects.findPeaks.FDR;

import java.io.IOException;
import java.util.Vector;
import src.lib.Utilities;
import src.lib.Graphics.GraphImage;
import src.lib.ioInterfaces.Log_Buffer;
import src.projects.findPeaks.FPConstants;
import src.projects.findPeaks.PeakDataSetParent;
import src.projects.findPeaks.filewriters.RegionWriter;
import src.projects.findPeaks.objects.Compare;
import src.projects.findPeaks.objects.PeakPairIdx;

public class HyperbolicControl {

    private static Log_Buffer LB;

    private static boolean display_version = true;

    private static final int ARRAY_LEN = 100;

    private static final int ZOOM_MAX = 30;

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
     */
    public HyperbolicControl(Log_Buffer logbuffer, PeakDataSetParent sample, PeakDataSetParent compare, String file, String chromosome, int minimum, int window_size, float alpha, boolean log_transform) {
        LB = logbuffer;
        if (display_version) {
            LB.Version("HyperbolicControl", "$Revision: 1789 $");
            display_version = false;
        }
        comp = new Compare(LB);
        comp.set_pairs(MakePairs.makepairs_window(sample, compare, window_size));
        if (log_transform) {
            comp.log_transform();
        }
        double s_s_h = 0;
        double c_s_h = 0;
        for (PeakPairIdx ppi : comp.get_array()) {
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
        for (PeakPairIdx ppi : comp.get_array()) {
            float s = ppi.get_height_1();
            float c = ppi.get_height_2();
            if (above_line(slope, intercept, s, c)) {
                above += 1;
            } else {
                below += 1;
            }
        }
        double[] percentages = new double[ARRAY_LEN + 1];
        for (int x = 1; x <= ARRAY_LEN; x++) {
            s_inz = 0;
            c_inz = 0;
            double y = slope * x;
            for (PeakPairIdx ppi : comp.get_array()) {
                float s = ppi.get_height_1();
                float c = ppi.get_height_2();
                if (above_line(slope, intercept, s, c)) {
                    if (in_zone_top(slope, y, s, c)) {
                        c_inz += 1;
                    }
                } else {
                    if (in_zone_bottom(slope, x, s, c)) {
                        s_inz += 1;
                    }
                }
            }
            percentages[x] = (float) c_inz / above;
            LB.notice("zone: " + x + "\tc_inz: " + c_inz + "\ts_inz: " + s_inz + "\test. fp.: " + Utilities.DecimalPoints(percentages[x] * s_inz, 1) + "\t%: " + Utilities.DecimalPoints(percentages[x] * FPConstants.PERCENTAGE, 2));
        }
        double interpolate = 0;
        double x_s = 0;
        double x_l = 0;
        double y_s = 0;
        double y_l = 0;
        int iterations = 0;
        float approx = 0;
        while (iterations < 10 && (approx < (alpha + FPConstants.FLOAT_TOLERANCE) || approx > (alpha - FPConstants.FLOAT_TOLERANCE))) {
            if (iterations == 0) {
                for (int x = 1; x < ARRAY_LEN; x++) {
                    if (alpha < percentages[x] && alpha > percentages[x + 1]) {
                        x_s = x;
                        x_l = x + 1;
                        y_s = percentages[x];
                        y_l = percentages[x + 1];
                    }
                }
            } else {
                if (alpha > approx) {
                    x_s = interpolate;
                    y_s = approx;
                } else {
                    x_l = interpolate;
                    y_l = approx;
                }
            }
            double slp = (y_l - y_s) / (x_l - x_s);
            interpolate = ((alpha - y_s) / slp) + x_s;
            if (Double.isInfinite(interpolate)) {
                interpolate = (x_l + x_s) / 2;
            }
            s_inz = 0;
            c_inz = 0;
            double y = slope * interpolate;
            for (PeakPairIdx ppi : comp.get_array()) {
                float s = ppi.get_height_1();
                float c = ppi.get_height_2();
                if (above_line(slope, intercept, s, c)) {
                    if (in_zone_top(slope, y, s, c)) {
                        c_inz += 1;
                    }
                } else {
                    if (in_zone_bottom(slope, interpolate, s, c)) {
                        s_inz += 1;
                    }
                }
            }
            approx = (float) c_inz / above;
            iterations += 1;
        }
        LB.notice("Estimated cutoff for alpha = " + alpha + " is " + interpolate);
        LB.notice("Actual values - estimated number of false peaks: " + Utilities.DecimalPoints((float) c_inz / above * s_inz, 1) + "\test. percent: " + Utilities.DecimalPoints((float) c_inz / above * FPConstants.PERCENTAGE, 3) + "%");
        export_to_graph_slope(file + "_all_points_slope", comp.get_array(), (float) slope, 0f);
        comp.set_pairs(filter_sample((float) slope, (float) interpolate));
        export_to_graph_slope(file + "_filtered_points", comp.get_array(), (float) slope, 0f);
        Vector<PeakPairIdx> temp = new Vector<PeakPairIdx>();
        for (PeakPairIdx ppi : comp.get_array()) {
            float s = ppi.get_height_1();
            float c = ppi.get_height_2();
            if (s < ZOOM_MAX && c < ZOOM_MAX) {
                temp.add(ppi);
            }
        }
        export_to_graph_slope(file + "_filtered_points_zoom", temp.toArray(new PeakPairIdx[temp.size()]), (float) slope, 0f);
        if (log_transform) {
            comp.reverse_log();
        }
        LB.notice("Linear Regresion: Remaining:\t" + comp.size());
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
	 * Keep peaks that are in the sample, only
	 * @param slope
	 * @param x position of the foci of the hyperbola
	 * @return
	 */
    public final PeakPairIdx[] filter_sample(float slope, float x) {
        Vector<PeakPairIdx> temp = new Vector<PeakPairIdx>();
        for (PeakPairIdx p : comp.get_array()) {
            if (in_zone_bottom(slope, x, p.get_height_1(), p.get_height_2())) {
                temp.add(p);
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
        if (y > Math.sqrt((x * x) / (slope * slope) + (a * a))) {
            return true;
        }
        return false;
    }

    private static boolean in_zone_bottom(double slope, double a, double x, double y) {
        if (y < Math.sqrt((slope * slope) * ((x * x) - (a * a)))) {
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
