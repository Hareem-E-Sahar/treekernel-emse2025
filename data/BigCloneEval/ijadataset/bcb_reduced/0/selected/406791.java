package pogvue.gui;

import pogvue.analysis.NJTree;
import pogvue.datamodel.*;
import pogvue.gui.menus.CommandParser;
import pogvue.datamodel.KmerFrequency;
import pogvue.io.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public final class AlignViewport {

    LinkedHashMap confighash;

    private AlignmentPanel minipog = null;

    public Hashtable mathash;

    public Vector matrices;

    public boolean showGaps = true;

    public String tffile = "data/tf9.4.fa";

    public SequenceI[] tffasta = null;

    public Vector hide;

    private int startRes;

    private int endRes;

    private int startSeq;

    private int endSeq;

    private double[][] csm;

    private Hashtable names;

    private double csmMax;

    private double csmMin;

    private boolean showScores;

    private boolean showText;

    private boolean showBoxes;

    private boolean wrapAlignment;

    private boolean translateExons = true;

    private boolean csmExons = true;

    private boolean groupEdit = false;

    private RendererI renderer = new SequenceRenderer();

    private String gapCharacter = "-";

    private int blockLength = 10;

    private int charHeight;

    private double charWidth = 0;

    private int chunkWidth;

    private int chunkHeight;

    private Color backgroundColour;

    private Font font;

    private Font idFont;

    private AlignmentI alignment;

    private final Selection sel = new Selection();

    private final ColumnSelection colSel = new ColumnSelection();

    private final CommandParser log;

    private Rectangle pixelBounds = new Rectangle();

    public String gff_config_file = "http://www.broad.mit.edu/~mclamp/pogvue/gff.conf";

    private String gffFileType = "URL";

    private LinkedHashMap gff_config;

    private int threshold;

    private int increment;

    private NJTree currentTree = null;

    private int window = 20;

    private int baseline = 70;

    private KmerFrequency kfreq = null;

    private double[][] methmousematrix;

    private double[][] unmethmousematrix;

    private double[][] methratmatrix;

    private double[][] unmethratmatrix;

    private double[][] methdogmatrix;

    private double[][] unmethdogmatrix;

    private double[] methpi;

    private double[] unmethpi;

    private int offset = 0;

    private Vector kmers;

    public boolean showSequence = false;

    private Controller controller;

    private static final String fasta_url = "http://www.broad.mit.edu/~mclamp/fetchmam.php?";

    private static final String gff_url = "http://www.broad.mit.edu/~mclamp/fetchmamgff.php?";

    private static final String grf_url = "http://www.broad.mit.edu/~mclamp/fetchmamgraph.php?";

    private static final String blt_url = "http://www.broad.mit.edu/~mclamp/fetch_mrna.php?";

    private static final String info_url = "http://www.broad.mit.edu/~mclamp/fetch_geneinfo.php?";

    public AlignViewport(AlignmentI da, boolean showScores, boolean showText, boolean showBoxes, boolean wrapAlignment) {
        this(0, da.getWidth() - 1, 0, da.getHeight() - 1, showScores, showText, showBoxes, wrapAlignment);
        readGFFConfig();
        setAlignment(da);
    }

    private AlignViewport(int startRes, int endRes, int startSeq, int endSeq, boolean showScores, boolean showText, boolean showBoxes, boolean wrapAlignment) {
        this.startRes = startRes;
        this.endRes = endRes;
        this.startSeq = startSeq;
        this.endSeq = endSeq;
        this.showScores = showScores;
        this.showText = showText;
        this.showBoxes = showBoxes;
        this.wrapAlignment = wrapAlignment;
        log = new CommandParser();
        setFont(new Font("Helvetica", Font.PLAIN, 10));
        setIdFont(new Font("Helvetica", Font.PLAIN, 10));
        setCharHeight(20);
        setCharWidth(1, "AlignViewport");
        readGFFConfig();
        hide = new Vector();
        kmers = new Vector();
        kmers.addElement("AAAAGAG");
        kmers.addElement("CTCTTTT");
    }

    public AlignViewport(int startRes, int endRes, int startSeq, int endSeq, boolean showScores, boolean showText, boolean showBoxes, boolean wrapAlignment, Color backgroundColour) {
        this(startRes, endRes, startSeq, endSeq, showScores, showText, showBoxes, wrapAlignment);
        this.backgroundColour = backgroundColour;
    }

    public void setMinipog(AlignmentPanel minipog) {
        System.out.println("Minipog here");
        this.minipog = minipog;
    }

    public AlignmentPanel getMinipog() {
        return minipog;
    }

    public void setVisibleSequence(boolean show) {
        this.showSequence = show;
    }

    public boolean getVisibleSequence() {
        return showSequence;
    }

    public void showSequence(SequenceI seq) {
        if (alignment != null) {
            if (hide.contains(seq)) {
                hide.removeElement(seq);
            }
        }
    }

    public void hideSequence(SequenceI seq) {
        if (alignment != null) {
            if (!hide.contains(seq)) {
                hide.addElement(seq);
            }
        }
    }

    public void showGaps(boolean show) {
        showGaps = show;
    }

    public boolean showGaps() {
        return showGaps;
    }

    public Vector hiddenSequences() {
        return hide;
    }

    public int getStartRes() {
        return startRes;
    }

    public int getEndRes() {
        return endRes;
    }

    public int getStartSeq() {
        return startSeq;
    }

    public void setController(Controller c) {
        this.controller = c;
    }

    public Controller getController() {
        return controller;
    }

    public void setPixelBounds(Rectangle rect) {
        pixelBounds = rect;
    }

    private Rectangle getPixelBounds() {
        return pixelBounds;
    }

    public void setStartRes(int res) {
        this.startRes = res;
    }

    public void setStartSeq(int seq) {
        this.startSeq = seq;
    }

    public void setEndRes(int res) {
        if (res > alignment.getWidth() - 1) {
            res = alignment.getWidth() - 1;
        }
        if (res < 0) {
            res = 0;
        }
        this.endRes = res;
    }

    public void setEndSeq(int seq) {
        if (seq > alignment.getHeight()) {
            seq = alignment.getHeight();
        }
        if (seq < 0) {
            seq = 0;
        }
        this.endSeq = seq;
    }

    public int getEndSeq() {
        return endSeq;
    }

    private void setIdFont(Font f) {
        this.idFont = f;
    }

    public Font getIdFont() {
        return idFont;
    }

    public void setFont(Font f) {
        this.font = f;
    }

    public Font getFont() {
        return font;
    }

    public void setCharWidth(double w, String from) {
        if (charWidth == 0) {
            charWidth = w;
        }
        if (w != charWidth) {
            int startRes = getStartRes();
            int endRes = getEndRes();
            double charWidth = getCharWidth();
            int currentWidth = (int) ((endRes - startRes + 1) * charWidth);
            int centreRes = (startRes + endRes) / 2;
            int prevstart = startRes;
            startRes = centreRes - (int) (currentWidth / (2 * w));
            endRes = centreRes + (int) (currentWidth / (2 * w));
            if (startRes < 0) {
                startRes = 0;
                endRes = startRes + (int) (currentWidth / w);
            }
            this.startRes = startRes;
            this.endRes = endRes;
            this.charWidth = w;
        }
    }

    public double getCharWidth() {
        return charWidth;
    }

    public void setCharHeight(int h) {
        this.charHeight = h;
    }

    public int getCharHeight() {
        return charHeight;
    }

    public void setChunkWidth(int w) {
        this.chunkWidth = w;
    }

    public int getChunkWidth() {
        return chunkWidth;
    }

    public void setChunkHeight(int h) {
        this.chunkHeight = h;
    }

    public int getChunkHeight() {
        return chunkHeight;
    }

    public AlignmentI getAlignment() {
        return alignment;
    }

    public void setAlignment(AlignmentI align) {
        this.alignment = align;
        if (align.getSequenceRegion() != null) {
            setOffset(align.getSequenceRegion().getStart());
        }
        Vector types = new Vector();
        for (int i = 0; i < hide.size(); i++) {
            Sequence seq = (Sequence) hide.elementAt(i);
            if (seq instanceof GFF) {
                types.addElement((String) ((GFF) seq).getType());
            }
        }
        hide.removeAllElements();
        for (int i = 0; i < align.getHeight(); i++) {
            SequenceI seq = align.getSequenceAt(i);
            if (seq instanceof GFF) {
                GFF gff = (GFF) seq;
                if (types.contains(gff.getType())) {
                    hide.addElement(seq);
                }
            }
            if (seq.getSequence().length() > 0 && showSequence == false) {
                hide.addElement(seq);
            }
        }
    }

    public void setShowScores(boolean state) {
        showScores = state;
    }

    public void setOffset(int offset) {
        System.out.println("Setting offset " + offset);
        this.offset = offset;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setWrapAlignment(boolean state) {
        wrapAlignment = state;
    }

    public void setShowText(boolean state) {
        showText = state;
    }

    public void setShowBoxes(boolean state) {
        showBoxes = state;
    }

    public boolean getShowScores() {
        return showScores;
    }

    public boolean getWrapAlignment() {
        return wrapAlignment;
    }

    public boolean getShowText() {
        return showText;
    }

    public boolean getShowBoxes() {
        return showBoxes;
    }

    public CommandParser getCommandLog() {
        return log;
    }

    public boolean getGroupEdit() {
        return groupEdit;
    }

    public void setGroupEdit(boolean state) {
        groupEdit = state;
    }

    public String getGapCharacter() {
        return gapCharacter;
    }

    public void setGapCharacter(String gap) {
        gapCharacter = gap;
        if (getAlignment() != null) {
            getAlignment().setGapCharacter(gapCharacter);
        }
    }

    public void setThreshold(int thresh) {
        threshold = thresh;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setIncrement(int inc) {
        increment = inc;
    }

    public int getIncrement() {
        return increment;
    }

    public int getIndex(int y) {
        int y1 = 0;
        int starty = getStartSeq();
        int endy = getEndSeq();
        int i = 0;
        int count = 0;
        while (i <= alignment.getHeight()) {
            if (!hide.contains(alignment.getSequenceAt(i))) {
                count++;
                if (count >= starty && count <= endy) {
                    int y2 = y1 + getCharHeight();
                    if (y >= y1 && y <= y2) {
                        return i + 1;
                    }
                    y1 = y2;
                }
            }
            i++;
        }
        return -1;
    }

    public Selection getSelection() {
        return sel;
    }

    public ColumnSelection getColumnSelection() {
        return colSel;
    }

    public void resetSeqLimits() {
        setStartSeq(0);
        setEndSeq(getPixelBounds().height / getCharHeight());
    }

    public void setCurrentTree(NJTree tree) {
        currentTree = tree;
    }

    public NJTree getCurrentTree() {
        return currentTree;
    }

    public void setRenderer(RendererI rend) {
        this.renderer = rend;
    }

    public RendererI getRenderer() {
        return renderer;
    }

    public int getPIDWindow() {
        return window;
    }

    public void setPIDWindow(int window) {
        this.window = window;
    }

    public void setBlockLength(int length) {
        this.blockLength = length;
    }

    public int getBlockLength() {
        return blockLength;
    }

    public int getPIDBaseline() {
        return baseline;
    }

    public void setPIDBaseline(int baseline) {
        this.baseline = baseline;
    }

    public void setKmers(Vector kmers) {
        this.kmers = kmers;
    }

    public Vector getKmers() {
        return this.kmers;
    }

    public void setMethDogMatrix(double[][] mat) {
        this.methdogmatrix = mat;
    }

    public double[][] getMethDogMatrix() {
        return methdogmatrix;
    }

    public void setUnmethDogMatrix(double[][] mat) {
        this.unmethdogmatrix = mat;
    }

    public double[][] getUnmethDogMatrix() {
        return unmethdogmatrix;
    }

    public void setMethMouseMatrix(double[][] mat) {
        this.methmousematrix = mat;
    }

    public double[][] getMethMouseMatrix() {
        return methmousematrix;
    }

    public void setUnmethMouseMatrix(double[][] mat) {
        this.unmethmousematrix = mat;
    }

    public double[][] getUnmethMouseMatrix() {
        return unmethmousematrix;
    }

    public void setMethRatMatrix(double[][] mat) {
        this.methratmatrix = mat;
    }

    public double[][] getMethRatMatrix() {
        return methratmatrix;
    }

    public void setUnmethRatMatrix(double[][] mat) {
        this.unmethratmatrix = mat;
    }

    public double[][] getUnmethRatMatrix() {
        return unmethratmatrix;
    }

    public void setMethPi(double[] pi) {
        this.methpi = pi;
    }

    public double[] getMethPi() {
        return methpi;
    }

    public void setUnmethPi(double[] pi) {
        this.unmethpi = pi;
    }

    public double[] getUnmethPi() {
        return unmethpi;
    }

    public LinkedHashMap getGFFConfig() {
        return readGFFConfig();
    }

    public void setGFFConfig(LinkedHashMap hash) {
        this.gff_config = hash;
    }

    public void setGFFFileType(String type) {
        gffFileType = type;
    }

    public String getGFFFileType() {
        return gffFileType;
    }

    public void setGFFConfigFile(String file) {
        gff_config_file = file;
    }

    public String getGFFConfigFile() {
        return gff_config_file;
    }

    public static String getFastaURL() {
        return fasta_url;
    }

    public static String getGFFURL() {
        return gff_url;
    }

    public static String getGRFURL() {
        return grf_url;
    }

    public static String getBLTURL() {
        return blt_url;
    }

    public static String getInfoURL() {
        return info_url;
    }

    public boolean getTranslateExons() {
        return translateExons;
    }

    public void setTranslateExons(boolean flag) {
        this.translateExons = flag;
    }

    public boolean getCSMExons() {
        return csmExons;
    }

    public void setCSMExons(boolean flag) {
        this.csmExons = flag;
    }

    public void setCsmMatrix(double[][] csm) {
        this.csm = csm;
    }

    public double[][] getCsmMatrix() {
        return csm;
    }

    public void setCsmMax(double max) {
        this.csmMax = max;
    }

    public void setCsmMin(double min) {
        this.csmMin = min;
    }

    public double getCsmMax() {
        return csmMax;
    }

    public double getCsmMin() {
        return csmMin;
    }

    public void setCsmNames(Hashtable names) {
        this.names = names;
    }

    public Hashtable getCsmNames() {
        return this.names;
    }

    public void setKmerFrequency(KmerFrequency kfreq) {
        this.kfreq = kfreq;
    }

    public KmerFrequency getKmerFrequency() {
        return kfreq;
    }

    public void writeGFFConfig() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(gff_config_file));
            Iterator str = gff_config.keySet().iterator();
            while (str.hasNext()) {
                String name = (String) str.next();
                Color val = (Color) gff_config.get(name);
                pw.println(name + " " + val.getRed() + "," + val.getGreen() + "," + val.getBlue());
            }
            pw.flush();
            pw.close();
        } catch (IOException e) {
            System.out.println("Can't write to config file " + gff_config_file);
        }
    }

    public LinkedHashMap readGFFConfig() {
        if (confighash == null) {
            confighash = readGFFConfig(gff_config_file, gffFileType);
        }
        return confighash;
    }

    public static LinkedHashMap readGFFConfig(String file, String type) {
        String line = "";
        LinkedHashMap out = new LinkedHashMap();
        try {
            FileParse fp = new FileParse(file, type);
            while ((line = fp.nextLine()) != null) {
                StringTokenizer str = new StringTokenizer(line);
                if (str.countTokens() == 2) {
                    String name = str.nextToken();
                    String val = str.nextToken();
                    StringTokenizer str2 = new StringTokenizer(val, ",");
                    int red = Integer.parseInt(str2.nextToken());
                    int green = Integer.parseInt(str2.nextToken());
                    int blue = Integer.parseInt(str2.nextToken());
                    Color c = new Color(red, green, blue);
                    out.put(name, c);
                } else {
                    System.out.println("Wrong format for gff config file " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Exception reading gff config file " + e);
        } catch (NumberFormatException e) {
            System.out.println("Exception reading gff config line " + line + e);
        }
        return out;
    }

    public Vector getTransfacMatrices() {
        if (matrices == null) {
            mathash = new Hashtable();
            try {
                TFMatrixFile tffile = new TFMatrixFile("http://www.broad.mit.edu/~mclamp/pogvue/jaspar_core_2008.tfc", "URL");
                matrices = tffile.getMatrices();
                for (int i = 0; i < matrices.size(); i++) {
                    TFMatrix mat = (TFMatrix) matrices.elementAt(i);
                    mathash.put(mat.getName(), mat);
                }
            } catch (IOException e) {
                System.out.println("Can't read TFMatrix File");
            }
        }
        return matrices;
    }

    public TFMatrix getMatrix(String name) {
        if (matrices == null) {
            getTransfacMatrices();
        }
        if (mathash != null && mathash.containsKey(name)) {
            return (TFMatrix) mathash.get(name);
        }
        return null;
    }

    public SequenceI[] getTFFasta() {
        if (tffasta == null) {
            try {
                FastaFile file = new FastaFile(tffile, "File");
                tffasta = file.getSeqsAsArray();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tffasta;
    }

    public void saveGFFConfig() {
        try {
            PrintStream ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(gff_config_file)));
            Iterator en = gff_config.keySet().iterator();
            while (en.hasNext()) {
                String key = (String) en.next();
                Color c = (Color) gff_config.get(key);
                ps.print(key + " " + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + "\n");
            }
            ps.flush();
            ps.close();
        } catch (IOException ex) {
            System.out.println("Exception : " + ex);
        }
    }
}
