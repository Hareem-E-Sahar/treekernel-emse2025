package org.vardb.alignment;

import java.util.*;
import org.vardb.*;
import org.vardb.converters.parsers.*;
import org.vardb.blast.*;
import org.vardb.analysis.*;
import org.vardb.sequences.*;
import org.vardb.util.*;

public class CHvHelper {

    private static final int MINSIZE_FOR_REALIGN = 3;

    private static final float MAXIDENTITY_FOR_REALIGN = 0.5f;

    private static final int MIN_BLASTCLUST_LENGTH = 8;

    private IVardbService m_vardbService;

    private CHvParams m_params;

    public static void main(String[] argv) {
        CVardbServiceImpl vardbService = new CVardbServiceImpl();
        CAlignmentServiceImpl alignmentService = new CAlignmentServiceImpl();
        CBlastServiceImpl blastService = new CBlastServiceImpl();
        vardbService.setAlignmentService(alignmentService);
        vardbService.setBlastService(blastService);
        CHvHelper helper = new CHvHelper(vardbService);
        String filename = "d:/projects/vardb/data/alignments/rifin_stevor-p.falciparum_3d7/rifin_stevor-p.falciparum_3d7.aa.aln";
        String fasta = CFileHelper.readFile(filename);
        CHvParams params = new CHvParams(fasta);
        Map<String, String> aligned = helper.align(params);
        helper.writeFile("final.aln", aligned);
    }

    public CHvHelper(IVardbService vardbService) {
        m_vardbService = vardbService;
    }

    public Map<String, String> align(CHvParams params) {
        m_params = params;
        String fasta = params.getAlignment();
        if (params.getPreAlign()) {
            updateJob("running pre-alignment");
            fasta = m_vardbService.getAlignmentService().einsi(fasta, m_params.getMafft()).getAlignment();
            writeFile("prealign.aln", fasta);
        }
        if (params.getPreCluster()) {
            Map<String, String> aligned = preCluster(fasta);
            writeFile("majorclusters.aln", fasta);
            return aligned;
        } else {
            Map<String, String> sequences = CMultipleAlignmentParser.getInstance().parse(fasta);
            return divideAndRealign(sequences);
        }
    }

    private Map<String, String> preCluster(String fasta) {
        updateJob("pre-clustering");
        Map<String, String> sequences = CMultipleAlignmentParser.getInstance().parse(fasta);
        CClusters clusters = m_vardbService.getBlastService().blastclust(fasta, m_params.getBlastclust());
        List<Map<String, String>> subclusters = new ArrayList<Map<String, String>>();
        Map<String, String> unclustered = new LinkedHashMap<String, String>();
        for (CClusters.CCluster cluster : clusters.getClusters()) {
            System.out.println("cluster: " + cluster.getIds());
            if (cluster.getIds().size() < m_params.getBlastclust().getMinimumSize()) {
                addSelectedSequences(cluster.getIds(), sequences, unclustered);
            } else {
                Map<String, String> subcluster = new LinkedHashMap<String, String>();
                subclusters.add(subcluster);
                addSelectedSequences(cluster.getIds(), sequences, subcluster);
            }
        }
        List<Map<String, String>> realigned_clusters = alignPreClusters(subclusters);
        CAlignmentResults results = m_vardbService.getAlignmentService().mafftProfile(realigned_clusters, unclustered, m_params.getMafft());
        return results.getMap();
    }

    private List<Map<String, String>> alignPreClusters(List<Map<String, String>> clusters) {
        List<Map<String, String>> seeds = new ArrayList<Map<String, String>>();
        int counter = 0;
        for (Map<String, String> cluster : clusters) {
            CAlignmentResults results = m_vardbService.getAlignmentService().einsi(cluster, m_params.getMafft());
            Map<String, String> realigned = divideAndRealign(results.getMap());
            seeds.add(realigned);
            writeFile("cluster." + counter + ".aln", CSequenceFileParser.writeFasta(realigned));
            counter++;
        }
        return seeds;
    }

    private Map<String, String> divideAndRealign(Map<String, String> sequences) {
        List<Map<String, String>> subalignments = splitByGblocks(sequences);
        List<Map<String, String>> realigned_subalignments = new ArrayList<Map<String, String>>();
        int num = 0;
        for (Map<String, String> subalignment : subalignments) {
            num++;
            writeFile("subalignment" + num + ".fasta", subalignment);
            Map<String, String> realigned_subalignment = realignHypervariableRegions(m_params, subalignment, num, m_vardbService);
            checkAlignment(subalignment, realigned_subalignment);
            realigned_subalignments.add(realigned_subalignment);
            writeFile("part" + num + ".aln", CSequenceFileParser.writeFasta(realigned_subalignment));
        }
        Map<String, String> joined = join(sequences, realigned_subalignments, "");
        checkAlignment(sequences, joined);
        return joined;
    }

    private List<Map<String, String>> splitByGblocks(Map<String, String> sequences) {
        updateJob("splitting alignment along conserved blocks (GBlocks stage)");
        String gblocks = m_vardbService.getAlignmentService().gblocks(sequences, m_params.getGblocks());
        System.out.println("glocks=" + gblocks);
        CLocation loc = new CLocation(gblocks);
        List<Map<String, String>> subalignments = new ArrayList<Map<String, String>>();
        List<Integer> columns = new ArrayList<Integer>();
        for (CLocation.Location location : loc.getLocations()) {
            for (CLocation.SubLocation sublocation : location.getSublocations()) {
                int start = sublocation.getStart();
                int end = sublocation.getEnd();
                int column = start + (end - start) / 2;
                columns.add(column);
            }
        }
        System.out.println("location=" + loc);
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
        if (subalignments.isEmpty()) throw new RuntimeException("subalignments list is empty");
        if (subalignments.size() == 1) throw new RuntimeException("only one subalignment");
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
                String sequence = entry.getValue() + delimiter;
                if (subalignment.containsKey(accession)) sequence += subalignment.get(accession); else sequence += CStringHelper.repeatString("-", length);
                joined.put(accession, sequence);
            }
        }
        return joined;
    }

    private Map<String, String> clusterSubalignments(Map<String, String> sequences, List<Map<String, String>> subclusters, CBlastClustParams params) {
        Map<String, String> unclustered = new LinkedHashMap<String, String>();
        CClusters clusters = m_vardbService.getBlastService().blastclust(sequences, params);
        for (CClusters.CCluster cluster : clusters.getClusters()) {
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
            if (!hasContent(accession)) {
                System.err.println("accession has no content: [" + accession + "]");
                continue;
            }
            String sequence = source.get(accession);
            if (!hasContent(sequence)) {
                System.err.println("sequence has no content: [" + accession + "]=[" + sequence + "]");
                continue;
            }
            destination.put(accession, sequence);
        }
    }

    private Map<String, String> realignHypervariableRegions(CHvParams params, Map<String, String> subalignment, int num, IVardbService m_vardbService) {
        if (!shouldRealign(subalignment)) {
            System.out.println("skipping realignment for subalignment " + num);
            return subalignment;
        }
        updateJob("realigning hypervariable region");
        Map<String, String> sequences = new LinkedHashMap<String, String>();
        Map<String, String> excluded = new LinkedHashMap<String, String>();
        partitionSubalignment(subalignment, sequences, excluded);
        List<Map<String, String>> subclusters = new ArrayList<Map<String, String>>();
        Map<String, String> unclustered = clusterSubalignments(sequences, subclusters, m_params.getBlastclust());
        if (subclusters.size() == 0) {
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
        Map<String, String> realigned = m_vardbService.getAlignmentService().mafftProfile(profiles, unclustered, m_params.getMafft()).getMap();
        writeFile("profilealigned." + num + ".aln", CSequenceFileParser.writeFasta(realigned));
        return realigned;
    }

    private boolean hasContent(String value) {
        return value != null && !value.equals("");
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
            CAlignmentResults results = m_vardbService.getAlignmentService().einsi(subcluster, m_params.getMafft());
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
            if (sequence2 == null) throw new RuntimeException("can't find sequence: " + accession);
            String stripped1 = CSequenceHelper.removeGaps(sequence1);
            String stripped2 = CSequenceHelper.removeGaps(sequence2);
            if (stripped1.length() != stripped2.length()) throw new RuntimeException("sequence length changed for sequence " + accession + " before=" + stripped1.length() + ", after=" + stripped2.length());
            if (!stripped1.equals(stripped2)) throw new RuntimeException("sequences differ for sequence " + accession + " before=" + stripped1 + ", after=" + stripped2);
            if (length == null) length = sequence2.length(); else if (length != sequence2.length()) throw new RuntimeException("sequence lengths differ for sequence " + accession + " " + length + " vs " + sequence2.length());
        }
        for (String accession : after.keySet()) {
            if (!before.containsKey(accession)) throw new RuntimeException("can't find sequence " + accession + " in original alignment");
        }
        return true;
    }

    private int findLength(Map<String, String> sequences) {
        if (sequences.size() == 0) return 0;
        return sequences.values().iterator().next().length();
    }

    private void writeFile(String filename, Map<String, String> sequences) {
        writeFile(filename, CSequenceFileParser.writeFasta(sequences));
    }

    private void writeFile(String filename, String str) {
    }

    private void updateJob(String message) {
        m_vardbService.getJobService().updateJob(m_params.getJobId(), message);
    }
}
