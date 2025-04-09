package tuner3d.genome;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.ListIterator;
import tuner3d.graphics.Palette;
import tuner3d.io.Sequence;

public class Genome implements Serializable {

    public static final byte CI99 = 1;

    public static final byte CI95 = 2;

    public static final byte RANGE = 3;

    public static final byte REGION = 4;

    public static final byte CDS = 5;

    public static final byte RNA = 6;

    public static final byte PSEUDO_GENE = 7;

    public static final float CI99_COI = 2.58f;

    public static final float CI95_COI = 1.96f;

    public static final int RANGE_DIVISION = 100;

    private int id = 0;

    private ArrayList<Dot> gcContent;

    private ArrayList<Dot> agContent;

    private ArrayList<Dot> gcSkew;

    private ArrayList<Cds> cdss;

    private ArrayList<Rna> rnas;

    private ArrayList<Region> regions;

    private ArrayList<PseudoGene> pseudoGenes;

    private ArrayList<Region> pies;

    private ArrayList<Range> genes, abnormalGcRegion, abnormalCdsRegion, clusterCdsRegion, clusterRnaRegion, lowGeneRegion;

    private boolean hasAbnormalGcRegion, hasAbnormalCdsRegion, hasClusterCdsRegion, hasClusterRnaRegion, hasLowGeneRegion;

    private Sequence sequence;

    private Parameter parameter;

    private Statistics statistics;

    private float yLevel, angle;

    private boolean requestTreeNode = true;

    public Genome(Parameter parameter) {
        this.parameter = new Parameter(parameter);
        statistics = new Statistics();
        gcContent = new ArrayList<Dot>();
        agContent = new ArrayList<Dot>();
        gcSkew = new ArrayList<Dot>();
        cdss = new ArrayList<Cds>();
        rnas = new ArrayList<Rna>();
        regions = new ArrayList<Region>();
        pseudoGenes = new ArrayList<PseudoGene>();
        pies = new ArrayList<Region>();
        genes = new ArrayList<Range>();
        sequence = new Sequence();
    }

    public Genome(int id, Parameter parameter) {
        this(parameter);
        this.id = id;
    }

    public void setName(String name) {
        this.statistics.setName(name);
    }

    public void setTag(String tag) {
        this.statistics.setTag(tag);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.statistics.getName();
    }

    public String getTag() {
        return this.statistics.getTag();
    }

    public int getSize() {
        return this.statistics.getSize();
    }

    public void setSize(int size) {
        this.statistics.setSize(size);
    }

    public void addCds(Cds cds) {
        this.cdss.add(cds);
    }

    public void addGcContent(Dot dot) {
        this.gcContent.add(dot);
    }

    public void addAgContent(Dot dot) {
        this.agContent.add(dot);
    }

    public void addGcSkew(Dot dot) {
        this.gcSkew.add(dot);
    }

    public void addRna(Rna rna) {
        this.rnas.add(rna);
    }

    public void addRegion(Region region) {
        this.regions.add(region);
    }

    public void setRegions(ArrayList<Region> regions) {
        this.regions = regions;
    }

    public void addPie(Region pie) {
        this.pies.add(pie);
    }

    public void addPies(ArrayList<Range> pies, Color color) {
        for (Iterator<Range> iterator = pies.iterator(); iterator.hasNext(); ) {
            Range range = iterator.next();
            this.pies.add(new Region(range, color));
        }
    }

    /**
	 * add analysis pies and save analysis results
	 * @param pies result pies
	 * @param color pie color
	 * @param palette identify result types
	 */
    public void addPies(ArrayList<Range> pies, Color color, Palette palette) {
        addPies(pies, color);
        if (color == palette.abnormal_gc) {
            abnormalGcRegion = pies;
            hasAbnormalGcRegion = true;
        } else if (color == palette.abnormal_cds) {
            abnormalCdsRegion = pies;
            hasAbnormalCdsRegion = true;
        } else if (color == palette.cluster_cds) {
            clusterCdsRegion = pies;
            hasClusterCdsRegion = true;
        } else if (color == palette.cluster_rna) {
            clusterRnaRegion = pies;
            hasClusterRnaRegion = true;
        } else if (color == palette.low_gene) {
            lowGeneRegion = pies;
            hasLowGeneRegion = true;
        }
    }

    public void addPies(ArrayList<Region> pies) {
        for (Iterator<Region> iterator = pies.iterator(); iterator.hasNext(); ) this.pies.add(iterator.next());
    }

    public void addGene(Range gene) {
        this.genes.add(gene);
    }

    public void addPseudoGene(PseudoGene gene) {
        this.pseudoGenes.add(gene);
    }

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public int getNumOfCds() {
        return this.cdss.size();
    }

    public int getNumOfRna() {
        return this.rnas.size();
    }

    public int getNumOfRRna() {
        int num = 0;
        for (Iterator<Rna> iterator = rnas.iterator(); iterator.hasNext(); ) {
            Rna rna = iterator.next();
            if (rna.getType() == Rna.rRNA) num++;
        }
        return num;
    }

    public int getNumOfTRna() {
        int num = 0;
        for (Iterator<Rna> iterator = rnas.iterator(); iterator.hasNext(); ) {
            Rna rna = iterator.next();
            if (rna.getType() == Rna.tRNA) num++;
        }
        return num;
    }

    public ArrayList<Dot> getGcContent() {
        return gcContent;
    }

    public ArrayList<Dot> getGcSkew() {
        return gcSkew;
    }

    public ArrayList<Cds> getCds() {
        return cdss;
    }

    public ArrayList<Rna> getRna() {
        return rnas;
    }

    public ArrayList<Region> getGenesInRange(int begin, int end) {
        ArrayList<Region> part = new ArrayList<Region>();
        for (Iterator<Cds> iterator = cdss.iterator(); iterator.hasNext(); ) {
            Region cds = (iterator.next()).degenerate();
            if (cds.getBegin() >= begin && cds.getEnd() <= end) part.add(cds);
        }
        for (Iterator<Rna> iterator = rnas.iterator(); iterator.hasNext(); ) {
            Region rna = (iterator.next()).degenerate();
            if (rna.getBegin() >= begin && rna.getEnd() <= end) part.add(rna);
        }
        return part;
    }

    public float getGeneCoverageInRange(int begin, int end) {
        int length = end - begin;
        float length2 = 0.0f;
        ListIterator<Range> iterator = genes.listIterator();
        while (((Range) iterator.next()).getBegin() < begin) {
            genes.listIterator(genes.size() / 2);
            if (!iterator.hasNext()) break;
        }
        iterator.previous();
        while (iterator.hasNext()) {
            Range gene = (Range) iterator.next();
            if (gene.getEnd() < begin) continue;
            if (gene.getBegin() > end) break;
            int begin2 = begin - gene.getBegin() > 0 ? begin : gene.getBegin();
            int end2 = end - gene.getEnd() > 0 ? gene.getEnd() : end;
            length2 += (end2 - begin2 + 1);
            begin = end2;
        }
        return length2 / length;
    }

    public ArrayList<Cds> getCdsInRange(int begin, int end) {
        ArrayList<Cds> part = new ArrayList<Cds>();
        for (Iterator<Cds> iterator = cdss.iterator(); iterator.hasNext(); ) {
            Cds cds = iterator.next();
            if (cds.getBegin() >= begin && cds.getEnd() <= end) part.add(cds);
        }
        return part;
    }

    public ArrayList<Rna> getRnaInRange(int begin, int end) {
        ArrayList<Rna> part = new ArrayList<Rna>();
        for (Iterator<Rna> iterator = rnas.iterator(); iterator.hasNext(); ) {
            Rna rna = iterator.next();
            if (rna.getBegin() >= begin && rna.getEnd() <= end) part.add(rna);
        }
        return part;
    }

    public int getCdsNumInRange(int begin, int end) {
        int num = 0;
        for (Iterator<Cds> iterator = cdss.iterator(); iterator.hasNext(); ) {
            Cds cds = iterator.next();
            if (cds.getBegin() >= begin && cds.getEnd() <= end) num++;
        }
        return num;
    }

    public int getRnaNumInRange(int begin, int end) {
        int num = 0;
        for (Iterator<Rna> iterator = rnas.iterator(); iterator.hasNext(); ) {
            Rna rna = iterator.next();
            if (rna.getBegin() >= begin && rna.getEnd() <= end) num++;
        }
        return num;
    }

    public ArrayList<Region> getRegion() {
        return regions;
    }

    /**
	 * get the region at specific position
	 * @param i
	 * @return
	 */
    public Region getRegion(int i) throws IndexOutOfBoundsException {
        return regions.get(i);
    }

    public ArrayList<Region> getPies() {
        return pies;
    }

    public void calcGcContent() {
        int gcStep = (int) (sequence.getLength() * parameter.getGcDivision());
        if (gcStep == 0) gcStep = 1;
        parameter.setGcStep(gcStep);
        gcContent.ensureCapacity(getSize() * Parameter.VECTOR_RESERVE_COEFFICIENT / gcStep);
        agContent.ensureCapacity(getSize() * Parameter.VECTOR_RESERVE_COEFFICIENT / gcStep);
        for (int pos = 0; pos + gcStep <= sequence.getLength(); pos += gcStep) {
            Sequence sub = sequence.subSequence(pos, gcStep);
            gcContent.add(new Dot(pos, gcStep, sub.gcContent()));
            agContent.add(new Dot(pos, gcStep, sub.agContent()));
        }
    }

    public void calcGcSkew() {
        int skewStep = (int) (sequence.getLength() * parameter.getSkewDivision());
        if (skewStep == 0) skewStep = 1;
        parameter.setSkewStep(skewStep);
        gcSkew.ensureCapacity(getSize() * Parameter.VECTOR_RESERVE_COEFFICIENT / skewStep);
        for (int pos = 0; pos + skewStep <= sequence.getLength(); pos += skewStep) {
            Sequence sub = sequence.subSequence(pos, skewStep);
            gcSkew.add(new Dot(pos, skewStep, sub.gcSkew()));
        }
    }

    public float getYLevel() {
        return yLevel;
    }

    public void setYLevel(float level) {
        yLevel = level;
    }

    public float getAngle() {
        return angle;
    }

    public float getAbsoluteAngle() {
        return Math.abs(angle);
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void gainAngle(float angle) {
        this.angle += angle;
    }

    public void changeAngle(float angle) {
        this.angle += angle;
    }

    public int getGcStep() {
        return this.parameter.getGcStep();
    }

    public int getSkewStep() {
        return this.parameter.getSkewStep();
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public Cds getCds(int id) {
        return new Cds();
    }

    public Cds getCds(String locusTag) {
        return new Cds();
    }

    public boolean hasCds() {
        return !cdss.isEmpty();
    }

    public boolean hasRna() {
        return !rnas.isEmpty();
    }

    public void sort() {
        Collections.sort(cdss);
        Collections.sort(rnas);
        Collections.sort(pseudoGenes);
    }

    /**
	 * find a feature matching the locus_tag
	 * @param locus_tag - the locus_tag provided
	 * @param type find - in what type of storage
	 * @return the finding result
	 */
    public Comparable find(String locus_tag, byte type) {
        switch(type) {
            case CDS:
                return binarySearch(cdss, new Cds(locus_tag));
            case RNA:
                return binarySearch(rnas, new Rna(locus_tag));
            case PSEUDO_GENE:
                return binarySearch(pseudoGenes, new PseudoGene(locus_tag));
            default:
                return null;
        }
    }

    /**
	 * find a feature matching the begin position
	 * @param begin - the begin position provided
	 * @param type - find in what type of storage
	 * @return the finding result
	 */
    public Comparable find(int begin, byte type) {
        switch(type) {
            case REGION:
                return binarySearch(regions, new Region(begin));
            case CDS:
                return binarySearch(cdss, new Cds(begin));
            case RNA:
                return binarySearch(rnas, new Rna(begin));
            case PSEUDO_GENE:
                return binarySearch(pseudoGenes, new PseudoGene(begin));
            default:
                return null;
        }
    }

    /**
	 * Searches the specified list for the specified object using the binary search algorithm
	 * @param list - the list to be searched 
	 * @param x - the key to be searched for
	 * @return the element matching the key
	 */
    private static Comparable binarySearch(ArrayList<? extends Comparable> list, Comparable x) {
        int low = 0;
        int high = list.size() - 1;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (list.get(mid).compareTo(x) < 0) low = mid + 1; else if (list.get(mid).compareTo(x) > 0) high = mid - 1; else return list.get(mid);
        }
        return null;
    }

    private static Comparable binarySearch(Comparable[] a, Comparable x) {
        int low = 0;
        int high = a.length - 1;
        int mid;
        while (low <= high) {
            mid = (low + high) / 2;
            if (a[mid].compareTo(x) < 0) low = mid + 1; else if (a[mid].compareTo(x) > 0) high = mid - 1; else return a[mid];
        }
        return null;
    }

    public void markCds(int id) {
    }

    public void markCds(String locusTag) {
    }

    public void setStatistics() {
        this.statistics.setGcContent(sequence.gcContent());
        this.statistics.setAgContent(sequence.agContent());
        this.statistics.setGcStatistics(gcContent);
        this.statistics.setCdsNum(cdss.size());
        this.statistics.setRRnaNum(getNumOfRRna());
        this.statistics.setTRnaNum(getNumOfTRna());
        int total = this.statistics.setCdsStatistics(cdss) + this.statistics.setRnaStatistics(rnas);
        this.statistics.setGenePercentage(total);
    }

    public String getSummary() {
        return statistics.getSummary();
    }

    public void mapSequences() {
        for (Iterator<Cds> iterator = cdss.iterator(); iterator.hasNext(); ) {
            Cds cds = iterator.next();
            addGene(new Range(cds.getBegin(), cds.getEnd()));
            cds.setSequence(sequence.subString(cds.getBegin(), cds.getEnd(), cds.getStrand()));
        }
        for (Iterator<Rna> iterator = rnas.iterator(); iterator.hasNext(); ) {
            Rna rna = iterator.next();
            addGene(new Range(rna.getBegin(), rna.getEnd()));
            rna.setSequence(sequence.subString(rna.getBegin(), rna.getEnd(), rna.getStrand()));
        }
        Collections.sort(genes);
    }

    public void addOrthologs() {
        for (Iterator<Cds> iterator = cdss.iterator(); iterator.hasNext(); ) {
            Cds cds = iterator.next();
            cds.addOrthologs();
        }
    }

    /**
	 * Add the hierarchy of the genome to the tree pane, set requestTreeNode false afterwards
	 * @return requestTreeNode
	 */
    public boolean requestTreeNodeOnce() {
        boolean isRequest = requestTreeNode;
        requestTreeNode = false;
        return isRequest;
    }

    public void analyze(boolean[] checks, byte ci, Palette palette) {
        if (checks[0] && hasAbnormalGcRegion) {
            addPies(abnormalGcRegion, palette.abnormal_gc);
            checks[0] = false;
        }
        if (checks[1] && hasAbnormalCdsRegion) {
            addPies(abnormalCdsRegion, palette.abnormal_cds);
            checks[1] = false;
        }
        if (checks[2] && hasClusterCdsRegion) {
            addPies(clusterCdsRegion, palette.cluster_cds);
            checks[2] = false;
        }
        if (checks[3] && hasClusterRnaRegion) {
            addPies(clusterRnaRegion, palette.cluster_rna);
            checks[3] = false;
        }
        if (checks[4] && hasLowGeneRegion) {
            addPies(lowGeneRegion, palette.low_gene);
            checks[4] = false;
        }
        double min = 0.0;
        double max = 0.0;
        if (checks[0]) {
            switch(ci) {
                case CI95:
                    min = statistics.getGcContent() - CI95_COI * statistics.getStdDevGcContent();
                    max = statistics.getGcContent() + CI95_COI * statistics.getStdDevGcContent();
                    break;
                case CI99:
                    min = statistics.getGcContent() - CI99_COI * statistics.getStdDevGcContent();
                    max = statistics.getGcContent() + CI99_COI * statistics.getStdDevGcContent();
                    break;
                default:
                    break;
            }
            ArrayList<Range> ranges = new ArrayList<Range>();
            for (Iterator<Dot> iterator = gcContent.iterator(); iterator.hasNext(); ) {
                Dot dot = iterator.next();
                if (dot.getVal() > max || dot.getVal() < min) ranges.add(new Range(new Float(dot.getPos() - parameter.gcStep / 2).intValue(), new Float(dot.getPos() + parameter.gcStep / 2).intValue()));
            }
            Collections.sort(ranges);
            if (ranges.size() > 2) addPies(mergeRanges(ranges), palette.abnormal_gc, palette); else addPies(ranges, palette.abnormal_gc);
        }
        if (checks[1]) {
            switch(ci) {
                case CI95:
                    min = statistics.getAvgCdsLength() - CI95_COI * statistics.getStdDevCdsLength();
                    max = statistics.getAvgCdsLength() + CI95_COI * statistics.getStdDevCdsLength();
                    break;
                case CI99:
                    min = statistics.getAvgCdsLength() - CI99_COI * statistics.getStdDevCdsLength();
                    max = statistics.getAvgCdsLength() + CI99_COI * statistics.getStdDevCdsLength();
                    break;
                default:
                    break;
            }
            for (Iterator<Cds> iterator = cdss.iterator(); iterator.hasNext(); ) {
                Cds cds = iterator.next();
                if (cds.getLength() > max || cds.getLength() < min) cds.setMark(true);
            }
        }
        if (checks[2]) {
            int step = statistics.getSize() / RANGE_DIVISION;
            double stddev = 0.0;
            double variance = 0.0;
            double mean = (double) statistics.getCdsNum() / RANGE_DIVISION;
            int end = statistics.getSize() - step;
            int i = 0;
            int[] cdsNumInRange = new int[RANGE_DIVISION + 1];
            for (int pos = 0; pos < end; pos += step) {
                cdsNumInRange[i] = getCdsNumInRange(pos, pos + step);
                variance += Math.pow((cdsNumInRange[i] - mean), 2.0);
                i++;
            }
            variance /= RANGE_DIVISION;
            stddev = Math.sqrt(variance);
            switch(ci) {
                case CI95:
                    min = mean - CI95_COI * stddev;
                    max = mean + CI95_COI * stddev;
                    break;
                case CI99:
                    min = mean - CI99_COI * stddev;
                    max = mean + CI99_COI * stddev;
                    break;
                default:
                    break;
            }
            ArrayList<Range> ranges = new ArrayList<Range>(RANGE_DIVISION + 1);
            i = 0;
            for (int pos = 0; pos < end; pos += step) {
                if (cdsNumInRange[i] > max || cdsNumInRange[i] < min) ranges.add(new Range(pos, pos + step));
                i++;
            }
            addPies(ranges, palette.cluster_cds, palette);
        }
        if (checks[3]) {
            int step = statistics.getSize() / RANGE_DIVISION;
            double stddev = 0.0;
            double variance = 0.0;
            double mean = (double) statistics.getRnaNum() / RANGE_DIVISION;
            int end = statistics.getSize() - step;
            int i = 0;
            int[] rnaNumInRange = new int[RANGE_DIVISION + 1];
            for (int pos = 0; pos < end; pos += step) {
                rnaNumInRange[i] = getRnaNumInRange(pos, pos + step);
                variance += Math.pow((rnaNumInRange[i] - mean), 2.0);
                i++;
            }
            variance /= RANGE_DIVISION;
            stddev = Math.sqrt(variance);
            switch(ci) {
                case CI95:
                    min = mean - CI95_COI * stddev;
                    max = mean + CI95_COI * stddev;
                    break;
                case CI99:
                    min = mean - CI99_COI * stddev;
                    max = mean + CI99_COI * stddev;
                    break;
                default:
                    break;
            }
            ArrayList<Range> ranges = new ArrayList<Range>(RANGE_DIVISION + 1);
            i = 0;
            for (int pos = 0; pos < end; pos += step) {
                if (rnaNumInRange[i] > max || rnaNumInRange[i] < min) ranges.add(new Range(pos, pos + step));
                i++;
            }
            addPies(ranges, palette.cluster_rna, palette);
        }
        if (checks[4]) {
            int step = statistics.getSize() / RANGE_DIVISION;
            double stddev = 0.0;
            double variance = 0.0;
            int end = statistics.getSize() - step;
            int i = 0;
            float[] geneCoverage = new float[RANGE_DIVISION + 1];
            for (int pos = 0; pos < end; pos += step) {
                geneCoverage[i] = getGeneCoverageInRange(pos, pos + step);
                variance += Math.pow((geneCoverage[i] - statistics.getGenePercentage()), 2.0);
                i++;
            }
            variance /= RANGE_DIVISION;
            stddev = Math.sqrt(variance);
            switch(ci) {
                case CI95:
                    min = statistics.getGenePercentage() - CI95_COI * stddev;
                    max = statistics.getGenePercentage() + CI95_COI * stddev;
                    break;
                case CI99:
                    min = statistics.getGenePercentage() - CI99_COI * stddev;
                    max = statistics.getGenePercentage() + CI99_COI * stddev;
                    break;
                default:
                    break;
            }
            ArrayList<Range> ranges = new ArrayList<Range>(RANGE_DIVISION + 1);
            i = 0;
            for (int pos = 0; pos < end; pos += step) {
                if (geneCoverage[i] > max || geneCoverage[i] < min) ranges.add(new Range(pos, pos + step));
                i++;
            }
            addPies(ranges, palette.low_gene, palette);
        }
    }

    /**
	 * merge closely adjacent ranges to one
	 * @param ranges input ranges
	 * @return merged ranges
	 */
    public ArrayList<Range> mergeRanges(ArrayList<Range> ranges) {
        boolean next = true;
        Range range = new Range();
        ArrayList<Range> newRanges = new ArrayList<Range>();
        ListIterator<Range> iterator = ranges.listIterator();
        while (iterator.hasNext()) {
            Range range1 = (Range) iterator.next();
            if (!iterator.hasNext()) break;
            Range range2 = (Range) iterator.next();
            if (next) {
                range.setBegin(range1.getBegin());
            }
            if ((double) (range2.getBegin() - range1.getEnd()) / statistics.getSize() < 0.01) {
                next = false;
            } else {
                next = true;
                range.setEnd(range1.getEnd());
                newRanges.add(range);
            }
            iterator.previous();
        }
        if (next) {
            newRanges.add((Range) iterator.previous());
        } else {
            range.setEnd(((Range) iterator.previous()).getEnd());
            newRanges.add(range);
        }
        return newRanges;
    }
}
