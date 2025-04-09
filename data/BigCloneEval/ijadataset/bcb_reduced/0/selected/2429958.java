package org.vardb.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.vardb.CConstants;
import org.vardb.CVardbException;
import org.vardb.alignment.CAlignmentResults;
import org.vardb.alignment.CConsensus;
import org.vardb.alignment.IAlignmentService;
import org.vardb.analysis.dao.CGblocksAnalysis;
import org.vardb.blast.CBlastClustParams;
import org.vardb.blast.CClusters;
import org.vardb.blast.IBlastService;
import org.vardb.sequences.CSequenceFileParser;
import org.vardb.sequences.CSequenceHelper;
import org.vardb.sequences.CSimpleLocation;
import org.vardb.tasks.ITaskService;
import org.vardb.util.CStringHelper;

public class CHvHelper {

    private static final int MINSIZE_FOR_REALIGN = 3;

    private static final float MAXIDENTITY_FOR_REALIGN = 0.5f;

    private static final int MIN_BLASTCLUST_LENGTH = 8;

    private IAlignmentService alignmentService;

    private IAnalysisService analysisService;

    private IBlastService blastService;

    private ITaskService taskService;

    private CHvParams params;

    public CHvHelper(IAnalysisService analysisService, IAlignmentService alignmentService, IBlastService blastService) {
        this.analysisService = analysisService;
        this.alignmentService = alignmentService;
        this.blastService = blastService;
    }

    public CHvHelper(IAnalysisService analysisService, IAlignmentService alignmentService, IBlastService blastService, ITaskService taskService) {
        this(analysisService, alignmentService, blastService);
        this.taskService = taskService;
    }

    public Map<String, String> align(CHvParams params) {
        this.params = params;
        return divideAndRealign(params.getSequences());
    }

    private Map<String, String> divideAndRealign(Map<String, String> sequences) {
        List<Map<String, String>> subalignments = splitByGblocks(sequences);
        checkCancelled();
        List<Map<String, String>> realigned_subalignments = new ArrayList<Map<String, String>>();
        int num = 0;
        for (Map<String, String> subalignment : subalignments) {
            num++;
            writeFile("subalignment" + num + ".fasta", subalignment);
            Map<String, String> realigned_subalignment = realignHypervariableRegions(subalignment, num);
            checkAlignment(subalignment, realigned_subalignment);
            realigned_subalignments.add(realigned_subalignment);
            writeFile("part" + num + ".aln", CSequenceFileParser.writeFasta(realigned_subalignment));
            checkCancelled();
        }
        Map<String, String> joined = join(sequences, realigned_subalignments, "");
        checkAlignment(sequences, joined);
        return joined;
    }

    private List<Map<String, String>> splitByGblocks(Map<String, String> sequences) {
        updateJob("splitting alignment along conserved blocks (GBlocks stage)");
        CGblocksAnalysis analysis = this.analysisService.gblocks(sequences, this.params.getGblocks());
        CSimpleLocation location = analysis.getLocation();
        List<Map<String, String>> subalignments = new ArrayList<Map<String, String>>();
        List<Integer> columns = new ArrayList<Integer>();
        for (CSimpleLocation.SubLocation sublocation : location.getSublocations()) {
            int start = sublocation.getStart();
            int end = sublocation.getEnd();
            int column = start + (end - start) / 2;
            columns.add(column);
        }
        System.out.println("location=" + location);
        System.out.println("columns=" + columns);
        int lastcolumn = 0;
        for (int column : columns) {
            Map<String, String> subalignment = new LinkedHashMap<String, String>();
            subalignments.add(subalignment);
            for (Map.Entry<String, String> entry : sequences.entrySet()) {
                String accession = entry.getKey();
                String sequence = sequences.get(accession);
                if (column >= sequence.length()) sequence = sequence.substring(lastcolumn); else sequence = sequence.substring(lastcolumn, column);
                subalignment.put(accession, sequence);
            }
            lastcolumn = column;
        }
        Map<String, String> subalignment = new LinkedHashMap<String, String>();
        subalignments.add(subalignment);
        for (Map.Entry<String, String> entry : sequences.entrySet()) {
            String accession = entry.getKey();
            String sequence = entry.getValue().substring(lastcolumn);
            subalignment.put(accession, sequence);
        }
        return subalignments;
    }

    private Map<String, String> join(Map<String, String> sequences, List<Map<String, String>> subalignments, String delimiter) {
        if (subalignments.isEmpty()) throw new CVardbException("subalignments list is empty");
        if (subalignments.size() == 1) throw new CVardbException("only one subalignment");
        updateJob("re-assembling partial alignments");
        Map<String, String> joined = new LinkedHashMap<String, String>();
        for (String accession : sequences.keySet()) {
            joined.put(accession, "");
        }
        for (Map<String, String> subalignment : subalignments) {
            int length = findLength(subalignment);
            System.out.println("subalignment length=" + length);
            for (Map.Entry<String, String> entry : joined.entrySet()) {
                String accession = entry.getKey();
                StringBuilder buffer = new StringBuilder();
                buffer.append(entry.getValue()).append(delimiter);
                if (subalignment.containsKey(accession)) buffer.append(subalignment.get(accession)); else buffer.append(CStringHelper.repeatString(CConstants.GAP, length));
                joined.put(accession, buffer.toString());
            }
        }
        return joined;
    }

    private Map<String, String> clusterSubalignments(Map<String, String> sequences, List<Map<String, String>> subclusters, CBlastClustParams params) {
        Map<String, String> unclustered = new LinkedHashMap<String, String>();
        params.setSequences(sequences);
        String str = this.blastService.blastclust(params);
        CClusters clusters = new CClusters(str);
        for (CClusters.Cluster cluster : clusters.getClusters()) {
            System.out.println("cluster: " + cluster.getIds());
            if (cluster.getIds().size() >= params.getMinimumSize()) {
                Map<String, String> subcluster = new LinkedHashMap<String, String>();
                addSelectedSequences(cluster.getIds(), sequences, subcluster);
                subclusters.add(subcluster);
            } else addSelectedSequences(cluster.getIds(), sequences, unclustered);
        }
        return unclustered;
    }

    private void addSelectedSequences(Collection<String> accessions, Map<String, String> source, Map<String, String> destination) {
        for (String accession : accessions) {
            if (!CStringHelper.hasContent(accession)) {
                System.err.println("accession has no content: [" + accession + "]");
                continue;
            }
            String sequence = source.get(accession);
            if (!CStringHelper.hasContent(sequence)) {
                System.err.println("sequence has no content: [" + accession + "]=[" + sequence + "]");
                continue;
            }
            destination.put(accession, sequence);
        }
    }

    private Map<String, String> realignHypervariableRegions(Map<String, String> subalignment, int num) {
        if (!shouldRealign(subalignment)) {
            System.out.println("skipping realignment for subalignment " + num);
            return subalignment;
        }
        updateJob("realigning hypervariable region");
        Map<String, String> sequences = new LinkedHashMap<String, String>();
        Map<String, String> excluded = new LinkedHashMap<String, String>();
        partitionSubalignment(subalignment, sequences, excluded);
        List<Map<String, String>> subclusters = new ArrayList<Map<String, String>>();
        Map<String, String> unclustered = clusterSubalignments(sequences, subclusters, this.params.getBlastclust());
        if (subclusters.isEmpty()) {
            System.out.println("no subclusters - keeping original");
            return subalignment;
        }
        List<Map<String, String>> profiles = alignSubclusterSeeds(subclusters, num);
        Map<String, String> realigned = profileAlign(profiles, unclustered, num);
        int length = findLength(realigned);
        System.out.println("padding to length: " + length);
        for (Map.Entry<String, String> entry : excluded.entrySet()) {
            String accession = entry.getKey();
            String sequence = entry.getValue();
            if (sequence == null) {
                System.out.println("sequence is null");
                continue;
            }
            sequence = CSequenceHelper.pad(sequence, length);
            System.out.println("restoring excluded fragment: " + accession + "=[" + sequence + "]");
            realigned.put(accession, sequence);
        }
        return realigned;
    }

    private void partitionSubalignment(Map<String, String> subalignment, Map<String, String> sequences, Map<String, String> excluded) {
        for (Map.Entry<String, String> entry : subalignment.entrySet()) {
            String accession = entry.getKey();
            String sequence = entry.getValue();
            sequence = CSequenceHelper.removeGaps(entry.getValue()).trim();
            if (sequence.length() < MIN_BLASTCLUST_LENGTH) excluded.put(accession, sequence); else sequences.put(accession, sequence);
        }
    }

    private Map<String, String> profileAlign(List<Map<String, String>> profiles, Map<String, String> unclustered, int num) {
        Map<String, String> realigned = this.alignmentService.mafftProfile(profiles, unclustered, this.params.getMafft()).getMap();
        writeFile("profilealigned." + num + ".aln", CSequenceFileParser.writeFasta(realigned));
        return realigned;
    }

    private boolean shouldRealign(Map<String, String> sequences) {
        CConsensus consensus = new CConsensus(sequences);
        if (consensus.getPositions().size() < MINSIZE_FOR_REALIGN) return false;
        float identity = consensus.getIdentity();
        System.out.println("identity=" + identity);
        System.out.println("consensus=" + consensus.getSequence());
        if (identity >= MAXIDENTITY_FOR_REALIGN) return false;
        return true;
    }

    private List<Map<String, String>> alignSubclusterSeeds(List<Map<String, String>> subclusters, int num) {
        List<Map<String, String>> seeds = new ArrayList<Map<String, String>>();
        int counter = 0;
        for (Map<String, String> subcluster : subclusters) {
            CAlignmentResults results = this.alignmentService.mafft(this.params.getMafft(), subcluster);
            seeds.add(results.getMap());
            writeFile("seed." + num + "." + counter + ".aln", results.getAlignment());
            counter++;
        }
        return seeds;
    }

    private boolean checkAlignment(Map<String, String> before, Map<String, String> after) {
        System.out.println("checking final alignment");
        Integer length = null;
        for (Map.Entry<String, String> entry : before.entrySet()) {
            String accession = entry.getKey();
            String sequence1 = entry.getValue();
            String sequence2 = after.get(accession);
            if (sequence2 == null) throw new CVardbException("can't find sequence: " + accession);
            String stripped1 = CSequenceHelper.removeGaps(sequence1);
            String stripped2 = CSequenceHelper.removeGaps(sequence2);
            if (stripped1.length() != stripped2.length()) throw new CVardbException("sequence length changed for sequence " + accession + " before=" + stripped1.length() + ", after=" + stripped2.length());
            if (!stripped1.equals(stripped2)) throw new CVardbException("sequences differ for sequence " + accession + " before=" + stripped1 + ", after=" + stripped2);
            if (length == null) length = sequence2.length(); else if (length != sequence2.length()) throw new CVardbException("sequence lengths differ for sequence " + accession + " " + length + " vs " + sequence2.length());
        }
        for (String accession : after.keySet()) {
            if (!before.containsKey(accession)) throw new CVardbException("can't find sequence " + accession + " in original alignment");
        }
        return true;
    }

    private int findLength(Map<String, String> sequences) {
        if (sequences.isEmpty()) return 0;
        return sequences.values().iterator().next().length();
    }

    private void writeFile(String filename, Map<String, String> sequences) {
        writeFile(filename, CSequenceFileParser.writeFasta(sequences));
    }

    private void writeFile(String filename, String str) {
    }

    private void checkCancelled() {
    }

    private void updateJob(String message) {
        if (this.taskService != null) this.taskService.updateTask(this.params.getTaskId(), message);
    }
}
