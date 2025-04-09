import org.json.JSONStringer;
import org.json.JSONException;
import java.io.*;
import java.util.*;
import java.text.*;
import java.awt.geom.Rectangle2D;

class Experiment {

    private int nPermutationsTried = 0;

    private int nMaxPermutations;

    private double[][] overallFilterPassCount;

    private double[][] nullhypothesisFilterPassCount;

    private Vector<Vector<Vector<Protein>>> nullhypothesisFilterPassProteins;

    private final DoubleList GStatisticCutoffs;

    private final DoubleList PValueCutoffs;

    private double FDRcutoff;

    private final String expName;

    private final String inputFile;

    public static double MAX_REASONABLE_FDR = .20;

    public static double FDR_SLIDER_PRECISION = .01;

    private final Vector<Protein> proteinList;

    private final Vector<String> hdrNames;

    private final Vector<Class> hdrClasses;

    private final Vector<String> groupNames;

    private String project_path;

    private static final String jsonCallback = "here";

    Experiment(String name, SimpleProteinPopulata simpleLoader, double[] gstatistic_range, double[] ttest_range, int maxPerms, String outdir, int testType) {
        this.expName = name;
        this.nMaxPermutations = maxPerms;
        System.out.print("Parsing Proteins...");
        this.inputFile = simpleLoader.getFilename();
        GStatisticCutoffs = new DoubleList(gstatistic_range);
        PValueCutoffs = new DoubleList(ttest_range);
        proteinList = simpleLoader.getProteinList();
        hdrNames = simpleLoader.getHdrNames();
        groupNames = simpleLoader.getGroupNames();
        hdrClasses = new Vector<Class>();
        for (int c = 0; c < getHdrNames().size(); c++) {
            Boolean allDouble = true;
            for (Protein p : getProteins()) {
                try {
                    Double.parseDouble(p.getProteinInfo().get(c));
                } catch (NumberFormatException nfe) {
                    allDouble = false;
                    break;
                }
            }
            if (allDouble) {
                hdrClasses.add(Double.class);
            } else {
                hdrClasses.add(String.class);
            }
        }
        System.out.println(" OK");
        int nBioReps = simpleLoader.getReplicateCount();
        Combination leftside_nullhyp = new Combination(nBioReps);
        for (int i = nBioReps; i-- > 0; ) {
            leftside_nullhyp.setAt(i, i);
        }
        Combination rightside_nullhyp = leftside_nullhyp.inverse();
        CombinationPair nullhyp = new CombinationPair(leftside_nullhyp, rightside_nullhyp);
        System.out.print("Calculating false discovery rates...");
        calculateDiscoveryRates(nBioReps, nullhyp);
        setFDRCutoff(MAX_REASONABLE_FDR / 2);
        System.out.println(" OK");
        System.out.print("Outputting Files... ");
        if ((outdir != null) && !(new File("filename")).exists()) (new File(outdir)).mkdirs();
        if (outdir == null) project_path = expName; else if (testType == 3) project_path = outdir; else project_path = outdir + "/" + expName;
        makeOutputFiles(testType);
        System.out.println("OK");
    }

    public String getName() {
        return this.expName;
    }

    public String getInputFileName() {
        return this.inputFile;
    }

    public double getFDRCutoff() {
        return FDRcutoff;
    }

    public String getFDRCutoffString() {
        return new String(String.format("%.2f", getFDRCutoff() * 100));
    }

    public void setFDRCutoff(double FDR) {
        FDRcutoff = FDR;
    }

    public Vector<String> getHdrNames() {
        return hdrNames;
    }

    public Vector<Class> getHdrClasses() {
        return hdrClasses;
    }

    public DoubleList getGStatisticCutoffs() {
        return GStatisticCutoffs;
    }

    public DoubleList getPValueCutoffs() {
        return PValueCutoffs;
    }

    public double getNullhypothesisFilterPassCount(int G, int P) {
        return nullhypothesisFilterPassCount[G][P];
    }

    void writeStuff() throws IOException {
        File file = new File(project_path + "/pepcResults.txt");
        FileWriter fwriter = new FileWriter(file);
        WriteCutoffs(fwriter);
        fwriter.close();
    }

    void WriteCutoffs(FileWriter fwriter) throws IOException {
        for (int gi = 0; gi < GStatisticCutoffs.size(); gi++) {
            for (int pi = 0; pi < PValueCutoffs.size(); pi++) {
                String str = GStatisticCutoffs.get(gi) + "\t" + PValueCutoffs.get(pi) + "\t" + this.getTPbyIndex(gi, pi) + "\t" + this.getFDRbyIndex(gi, pi) + "\n";
                fwriter.write(str);
            }
            fwriter.write("\n");
        }
    }

    int getGStatisticCutoffsCount(Rectangle2D subset) {
        if (subset != null) {
            int low = (int) Math.max(0, subset.getMinX());
            int hi = (int) Math.min(GStatisticCutoffs.size() - 1, subset.getMaxX());
            if (low <= hi) {
                return 1 + (hi - low);
            }
        }
        return GStatisticCutoffs.size();
    }

    double getGStatisticCutoffByIndex(int index) {
        return GStatisticCutoffs.get(index);
    }

    int getPValueCutoffsCount(Rectangle2D subset) {
        if (subset != null) {
            int low = (int) Math.max(0, subset.getMinY());
            int hi = (int) Math.min(PValueCutoffs.size() - 1, subset.getMaxY());
            if (low <= hi) {
                return 1 + (hi - low);
            }
        }
        return PValueCutoffs.size();
    }

    double getPValueCutoffByIndex(int index) {
        return PValueCutoffs.get(index);
    }

    void calculateDiscoveryRates(int nBioReps, CombinationPair nullhyp) {
        if (proteinList == null) return;
        this.overallFilterPassCount = new double[this.GStatisticCutoffs.size()][this.PValueCutoffs.size()];
        this.nullhypothesisFilterPassCount = new double[this.GStatisticCutoffs.size()][this.PValueCutoffs.size()];
        this.nullhypothesisFilterPassProteins = new Vector<Vector<Vector<Protein>>>();
        for (int gi = 0; gi < this.GStatisticCutoffs.size(); gi++) {
            this.nullhypothesisFilterPassProteins.add(new Vector<Vector<Protein>>());
            for (int pi = 0; pi < this.PValueCutoffs.size(); pi++) {
                this.nullhypothesisFilterPassProteins.elementAt(gi).add(new Vector<Protein>());
                this.overallFilterPassCount[gi][pi] = 0;
                this.nullhypothesisFilterPassCount[gi][pi] = 0;
            }
        }
        CombinationGenerator combos = new CombinationGenerator(nBioReps);
        int[] groupsCount = new int[nBioReps];
        BitSet[] selections = new BitSet[nBioReps];
        double selectionRate = (double) this.nMaxPermutations / combos.getTotal().doubleValue();
        if (selectionRate < 1.0) {
            System.out.println("Randomly sampling " + Math.round(combos.getTotal().doubleValue() * selectionRate) + " (" + (int) Math.ceil(selectionRate * 100.0) + "%) of " + combos.getTotal().intValue() + " data permutations...");
        } else {
            System.out.println("Processing " + combos.getTotal().intValue() + " data permutations...");
        }
        for (int order = 0; order < nBioReps; order++) {
            int orderSize = combos.getOrderSize(order);
            selections[order] = new BitSet(orderSize);
            if (selectionRate < 1.0) {
                Random r = new Random(orderSize);
                int nSelect = 1 + (int) ((double) orderSize * selectionRate);
                nSelect = Math.min(nSelect, orderSize);
                for (int i = nSelect; i > 0; ) {
                    int slot = r.nextInt(orderSize);
                    if (!selections[order].get(slot)) {
                        selections[order].set(slot);
                        i--;
                    }
                }
            } else {
                selections[order].set(0, orderSize);
            }
        }
        double progress = 0;
        int last_pct = 0;
        while (combos.hasMore()) {
            CombinationPair pair = combos.getNext();
            int order = pair.getOrder();
            int pctdone = (int) (100.0 * ++progress / combos.getTotal().doubleValue());
            if (pctdone != last_pct) {
                last_pct = pctdone;
                System.out.print(((last_pct % 5) == 0) ? (last_pct + "%") : ".");
            }
            if (selections[order].get(groupsCount[order]++)) {
                this.nPermutationsTried++;
                boolean bNullHyp = pair.equals(nullhyp);
                for (Protein prot : this.proteinList) {
                    ProteinPermutation perm = prot.doPermutation(pair);
                    if (bNullHyp) {
                        prot.nullHypothesis = perm;
                    }
                    for (int gi = 0; gi < GStatisticCutoffs.size(); gi++) {
                        if (perm.getGStatistic() >= GStatisticCutoffs.get(gi)) {
                            for (int pi = PValueCutoffs.size(); pi-- > 0; ) {
                                if (perm.getPValue() <= PValueCutoffs.get(pi)) {
                                    this.overallFilterPassCount[gi][pi]++;
                                    if (bNullHyp) {
                                        this.nullhypothesisFilterPassCount[gi][pi]++;
                                        this.nullhypothesisFilterPassProteins.elementAt(gi).elementAt(pi).add(prot);
                                    }
                                } else {
                                    break;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }

    public int getGStatisticIndex(double GStatistic) {
        return GStatisticCutoffs.getIndex(GStatistic);
    }

    public int getPValueIndex(double PValue) {
        return PValueCutoffs.getIndex(PValue);
    }

    double getFPbyIndex(int GStatisticIndex, int PTestIndex) {
        return this.overallFilterPassCount[GStatisticIndex][PTestIndex] / (double) this.nPermutationsTried;
    }

    public double getTPbyIndex(int GStatisticIndex, int PTestIndex) {
        double FP = getFPbyIndex(GStatisticIndex, PTestIndex);
        double nullHypPassCount = this.nullhypothesisFilterPassCount[GStatisticIndex][PTestIndex];
        double TP = (nullHypPassCount - FP);
        return Math.max(TP, 0.0);
    }

    public double getFDRbyIndex(int GStatisticIndex, int PTestIndex) {
        double FP = getFPbyIndex(GStatisticIndex, PTestIndex);
        double TP = getTPbyIndex(GStatisticIndex, PTestIndex);
        double FDR = ((FP + TP) != 0) ? (FP / (FP + TP)) : 1.0;
        return (FDR);
    }

    Vector<Protein> getProteins() {
        return this.proteinList;
    }

    Vector<Protein> getProteinsByCutoffIndices(int GstatisticIndex, int pvalueIndex) {
        Vector<Protein> result = new Vector<Protein>();
        for (Protein p : this.nullhypothesisFilterPassProteins.get(GstatisticIndex).get(pvalueIndex)) {
            result.add(p);
        }
        return result;
    }

    void writeProteins(FileWriter fwriter) throws IOException {
        for (Protein p : this.proteinList) {
            String str = "";
            for (String s : p.getProteinInfo()) {
                str += s + "\t";
            }
            str += p.getGStatistic() + "\t" + p.getPValue() + "\n";
            fwriter.write(str);
        }
    }

    void writeSummary(String sname) throws IOException {
        File file = new File(project_path + "/" + sname);
        FileWriter fwriter = new FileWriter(file);
        fwriter.write("Pepc Summary File:\n\n");
        fwriter.write("Summary Version: 1.0\n");
        fwriter.write("Analysis name: " + this.expName + "\n");
        Date now = new Date();
        DateFormat df = DateFormat.getDateInstance();
        fwriter.write("Summary file created on " + df.format(now) + "\n");
        fwriter.write("Input File: \n\n");
        fwriter.write(this.inputFile + "\n");
        fwriter.write("\n");
        fwriter.write("Number of proteins analyzed: " + this.proteinList.size() + "\n");
        fwriter.write("Number of permutations per protein: " + this.nPermutationsTried + "\n");
        fwriter.write("\n");
        fwriter.write("Proteins\n");
        writeProteins(fwriter);
        fwriter.write("\n");
        fwriter.write("Cutoffs\n");
        WriteCutoffs(fwriter);
        fwriter.close();
    }

    void makePlots() throws IOException {
        Process proc = Runtime.getRuntime().exec("GNUplot");
        if (proc == null) {
            System.out.println("Error opening GNUplot- it may not be installed or else path variable is not set");
            System.out.println("Cannot create sexy graphs");
            return;
        }
        OutputStream os = proc.getOutputStream();
        PrintStream ps = new PrintStream(os);
        makeCutoffPlotFile(ps);
        ps.close();
    }

    void makeCutoffPlotFile(PrintStream ps) {
        ps.println("reset");
        ps.println("clear");
        ps.println("cd \"" + project_path + "\"");
        ps.println("set term png large size 960,720");
        ps.println("set output \"CutoffsSurfPlot.png\"");
        ps.println("set border");
        ps.println("set grid");
        ps.println("set log yzcb");
        ps.println("set pm3d at s");
        ps.println("set title \"TP and FDR by G-statistic & T-Test p-value Cutoffs\"");
        ps.println("set xlabel \"G-statistic\"");
        ps.println("set ylabel \"p-value\"");
        ps.println("set label 1 \"TP\" center rotate by 90 at graph 0, graph 0, graph 0.5 offset -5");
        ps.println("set key off");
        ps.println("splot \"pepcResults.txt\"");
        ps.println("reset");
        ps.println("set term png large size 960,720");
        ps.println("set output \"Cutoffs.png\"");
        ps.println("set border");
        ps.println("set log ycb");
        ps.println("set title \"FDR by G-statistict & T-Test p-value Cutoffs\"");
        ps.println("set xlabel \"G-statistic\"");
        ps.println("set ylabel \"p-value\"");
        ps.println("splot \"pepcResults.txt\" with view");
        ps.flush();
    }

    void createOwnDirectory() {
        new File(project_path).mkdirs();
    }

    void makeOutputFiles(int testType) {
        try {
            if (testType == 2) {
                createOwnDirectory();
                writeStuff();
                writeSummary("regTest.txt");
                writeSummary("pepcSummary.txt");
                File file = new File(project_path + "/pepcResults3.txt");
                FileWriter fwriter = new FileWriter(file);
                writeProteins(fwriter);
                fwriter.close();
            } else if (testType == 3) {
                writeStuff();
                File file = new File(project_path + "/" + expName + ".js");
                FileWriter fwriter = new FileWriter(file);
                String jstr = toJSON();
                if (jstr != null) {
                    fwriter.write(jsonCallback + '(');
                    fwriter.write(jstr);
                    fwriter.write(");");
                }
                fwriter.close();
            } else {
                createOwnDirectory();
                writeStuff();
                writeSummary("pepcSummary.txt");
                File file = new File(project_path + "/pepcResults3.txt");
                FileWriter fwriter = new FileWriter(file);
                writeProteins(fwriter);
                fwriter.close();
            }
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Error writing output files");
        }
    }

    String toJSON() {
        JSONStringer ret = new JSONStringer();
        try {
            ret.object();
            ret.key("Array_by_GStatistic");
            ret.array();
            for (int i = 0; i < GStatisticCutoffs.size(); i++) {
                ret.object();
                ret.key("GStatistic");
                ret.value(GStatisticCutoffs.get(i));
                ret.key("Array_by_TTest");
                ret.array();
                for (int ii = 0; ii < PValueCutoffs.size(); ii++) {
                    ret.object();
                    ret.key("TTest");
                    double tt = PValueCutoffs.get(ii);
                    ret.value(tt);
                    ret.key("Pos");
                    ret.value(getNullhypothesisFilterPassCount(i, ii));
                    ret.key("FPos");
                    ret.value(getFPbyIndex(i, ii));
                    ret.endObject();
                }
                ret.endArray();
                ret.endObject();
            }
            ret.endArray();
            ret.key("Headers");
            ret.array();
            ret.value("IPI");
            ret.value("SC Avg (A)");
            ret.value("SC Avg (B)");
            ret.value("G-Statistic");
            ret.value("T-Test");
            ret.endArray();
            ret.key("ProteinList");
            ret.array();
            for (Protein p : proteinList) {
                if (p != null) {
                    ret.object();
                    ret.key("PValue");
                    double pv = p.getPValue();
                    ret.value(pv);
                    double gt = p.getGStatistic();
                    ret.key("GStatistic");
                    ret.value(gt);
                    ret.key("Protein_Info");
                    ret.array();
                    if ((p.getProteinInfo() != null) && p.getProteinInfo().size() > 0) for (String str : p.getProteinInfo()) {
                        ret.value(str);
                    } else {
                        ret.value("Unknown_Protein");
                    }
                    ret.endArray();
                    ret.endObject();
                }
            }
            ret.endArray();
            ret.endObject();
        } catch (JSONException e) {
            System.out.println("Error :" + e.toString());
            return null;
        }
        return ret.toString();
    }
}
