package src.projects.graphics;

import java.util.HashMap;
import java.util.Iterator;
import src.lib.CommandLine;
import src.lib.CurrentVersion;
import src.lib.IterableIterator;
import src.lib.Error_handling.CommandLineProcessingException;
import src.lib.Graphics.SVGWriter;
import src.lib.ioInterfaces.Log_Buffer;

public class TwoWayVenn {

    private TwoWayVenn() {
    }

    private static Log_Buffer LB = null;

    private static String output_file;

    private static int value1;

    private static int value2;

    private static String name1;

    private static String name2;

    private static int[] RGB1 = new int[3];

    private static int[] RGB2 = new int[3];

    private static int overlap;

    private static final int iterations = 40;

    private static final int WIDTH = 600;

    private static final int HEIGHT = 400;

    private static final int MID_HEIGHT = HEIGHT / 2;

    private static final int FONT_SIZE = 14;

    private static final int PADDING = 10;

    private static void parse_input(HashMap<String, String> Variables) {
        if (Variables == null) {
            usage();
        }
        assert (Variables != null);
        if (Variables.containsKey("help")) {
            usage();
        }
        if (Variables.containsKey("values")) {
            CommandLine.test_parameter_count(LB, "values", Variables.get("values"), 2);
            String[] s = Variables.get("values").split(",");
            value1 = Integer.valueOf(s[0]);
            value2 = Integer.valueOf(s[1]);
            LB.notice(" * Value 1           : " + value1);
            LB.notice(" * Value 2           : " + value2);
        } else {
            LB.error("Missing parameters for -values");
            usage();
        }
        if (Variables.containsKey("names")) {
            String[] s = Variables.get("names").split(",");
            name1 = s[0];
            name2 = s[1];
            LB.notice(" * Name 1            : " + name1);
            LB.notice(" * Name 2            : " + name2);
        } else {
            name1 = null;
            name2 = null;
        }
        if (Variables.containsKey("rgb1")) {
            CommandLine.test_parameter_count(LB, "rgb1", Variables.get("rgb1"), 3);
            String[] s = Variables.get("rgb1").split(",");
            RGB1[0] = Integer.valueOf(s[0]);
            RGB1[1] = Integer.valueOf(s[1]);
            RGB1[2] = Integer.valueOf(s[2]);
            LB.notice(" * RGB1              : " + RGB1[0] + ", " + RGB1[1] + ", " + RGB1[2]);
        } else {
            RGB1[0] = SVGWriter.COL_SAT;
            RGB1[1] = SVGWriter.COL_SAT;
            RGB1[2] = 0;
        }
        if (Variables.containsKey("rgb2")) {
            CommandLine.test_parameter_count(LB, "rgb2", Variables.get("rgb2"), 3);
            String[] s = Variables.get("rgb2").split(",");
            RGB2[0] = Integer.valueOf(s[0]);
            RGB2[1] = Integer.valueOf(s[1]);
            RGB2[2] = Integer.valueOf(s[2]);
            LB.notice(" * RGB2              : " + RGB2[0] + ", " + RGB2[1] + ", " + RGB2[2]);
        } else {
            RGB2[0] = 0;
            RGB2[1] = SVGWriter.COL_SAT;
            RGB2[2] = SVGWriter.COL_SAT;
        }
        if (Variables.containsKey("overlap")) {
            CommandLine.test_parameter_count(LB, "overlap", Variables.get("overlap"), 1);
            overlap = Integer.valueOf(Variables.get("overlap"));
            LB.notice(" * Overlap           : " + overlap);
        } else {
            overlap = 0;
        }
        if (Variables.containsKey("output_file")) {
            CommandLine.test_parameter_count(LB, "output_file", Variables.get("output_file"), 1);
            output_file = Variables.get("output_file");
            LB.notice(" * Output file       : " + output_file);
        } else {
            LB.error("Must specify output file with the -output_file flag");
            usage();
        }
        Variables.remove("output_file");
        Variables.remove("values");
        Variables.remove("overlap");
        Variables.remove("names");
        Variables.remove("rgb1");
        Variables.remove("rgb2");
        Iterator<String> keys = Variables.keySet().iterator();
        if (keys.hasNext()) {
            LB.error("Could not process the following flags:");
            for (String k : new IterableIterator<String>(keys)) {
                LB.error("  * " + k);
            }
            LB.die();
        }
    }

    private static void usage() {
        LB.notice("This program requires the following parameters:");
        LB.notice("-output_file");
        LB.notice("-values");
        LB.notice("-overlap");
        LB.notice("This program can also use the following parameters:");
        LB.notice("-names");
        LB.notice("-RGB1");
        LB.notice("-RGB2");
        LB.die();
    }

    public static void main(String[] args) {
        LB = Log_Buffer.getLogBufferInstance();
        LB.addPrintStream(System.out);
        Thread th = new Thread(LB);
        th.start();
        new CurrentVersion(LB);
        LB.Version("Two way Venn diagram generator", "$Revision: 2740 $");
        HashMap<String, String> Variables = null;
        try {
            Variables = CommandLine.process_CLI(args);
        } catch (CommandLineProcessingException CLPE) {
            LB.error(CLPE.getMessage());
            LB.die();
        }
        parse_input(Variables);
        assert (Variables != null);
        if (!output_file.endsWith(".svg")) {
            output_file = output_file.concat(".svg");
        }
        int AREA1 = 50000;
        int max = Math.max(value1, value2);
        double r1 = Math.sqrt((double) AREA1 / Math.PI);
        int min = Math.min(value1, value2);
        double r2 = Math.sqrt(((double) AREA1 * min) / (max * Math.PI));
        double overlap_pixels = (double) AREA1 * overlap / max;
        double maxd = r1 + r2;
        double mind = r1 - r2;
        double d = r1;
        for (int x = 0; x < iterations; x++) {
            double area = area_of_overlap(r1, r2, d);
            if (area < overlap_pixels) {
                maxd = d;
            } else if (area > overlap_pixels) {
                mind = d;
            } else {
                continue;
            }
            d = (mind + maxd) / 2;
        }
        double margin = (WIDTH - (r1 + r2 + d)) / 2;
        SVGWriter s1 = new SVGWriter(LB, output_file);
        s1.create_header(WIDTH, HEIGHT);
        s1.add_defs();
        s1.BlendGroupOn();
        s1.circle((float) (margin + r1), (float) MID_HEIGHT, (float) r1, RGB1[0], RGB1[1], RGB1[2], 4);
        s1.circle((float) (margin + r1 + d), (float) MID_HEIGHT, (float) r2, RGB2[0], RGB2[1], RGB2[2], 4);
        s1.BlendGroupOff();
        s1.AnchorText(SVGWriter.ANCHORLEFT);
        s1.text((int) (margin + PADDING), MID_HEIGHT, FONT_SIZE, 0, 0, 0, ((value1 == max) ? String.valueOf(value1 - overlap) : String.valueOf(value2 - overlap)));
        s1.AnchorText(SVGWriter.ANCHORRIGHT);
        s1.text((int) (WIDTH - (margin + PADDING)), MID_HEIGHT, FONT_SIZE, 0, 0, 0, ((value2 == min) ? String.valueOf(value2 - overlap) : String.valueOf(value1 - overlap)));
        s1.AnchorText(SVGWriter.ANCHORCENTER);
        s1.text((int) (margin + (2 * r1) - ((r1 + r2 - d) / 2)), MID_HEIGHT, FONT_SIZE, 0, 0, 0, String.valueOf(overlap));
        if (name1 != null && name2 != null) {
            s1.AnchorText(SVGWriter.ANCHORRIGHT);
            s1.text((int) (margin - 5 + (r1 - (Math.cos(Math.PI / 4) * r1))), (int) (MID_HEIGHT - r1 * Math.sin(Math.PI / 4)), FONT_SIZE, 0, 0, 0, ((value1 == max) ? name1 + " - " + value1 : name2 + " - " + value2));
            s1.AnchorText(SVGWriter.ANCHORLEFT);
            s1.text((int) (WIDTH - margin - (r2 - (Math.cos(Math.PI / 4) * r2)) + 5), (int) (MID_HEIGHT - r2 * Math.sin(Math.PI / 4)), FONT_SIZE, 0, 0, 0, ((value2 == min) ? name2 + " - " + value2 : name1 + " - " + value1));
        }
        s1.close();
        LB.close();
    }

    private static double area_of_overlap(double r1, double r2, double d) {
        double t1 = r1 * r1 * Math.acos(((d * d) + (r1 * r1) - (r2 * r2)) / (2 * d * r1));
        double t2 = r2 * r2 * Math.acos(((d * d) + (r2 * r2) - (r1 * r1)) / (2 * d * r2));
        double t3 = 0.5 * Math.sqrt((-d + r1 + r2) * (d + r1 - r2) * (d - r1 + r2) * (d + r1 + r2));
        return t1 + t2 - t3;
    }
}
