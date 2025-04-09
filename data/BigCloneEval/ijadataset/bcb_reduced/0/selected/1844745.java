package org.vardb.analysis.structure;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.vardb.util.*;
import org.vardb.*;
import org.vardb.analysis.*;
import org.vardb.sequences.CLocation;
import org.vardb.sequences.ISequence;
import org.vardb.sequences.dao.*;

public class CStructureHelper {

    protected static final String NNPREDICT_URL = "http://alexander.compbio.ucsf.edu/cgi-bin/nnpredict.pl";

    protected static final String TSEG_URL = "http://www.genome.jp/sit-bin/tsegdir/tseg.cgi";

    public static void main(String[] argv) {
        String aa = "ESLIPYHDKYKLTYGDSQICTVLARSFADIGDIIRGKDLFRGNNKEKKQREKLDENLKNNFREYIYKDVTSSGRNVQKLQKRYNDDNENYYQLR";
        List<ISequence> sequences = new ArrayList<ISequence>();
        sequences.add(new CSequence("test", aa));
        CMotifResults results = findSecondaryStructures(sequences);
        StringBuilder buffer = new StringBuilder();
        String features = buffer.toString();
        CFileHelper.writePlatformFile("features.txt", features);
    }

    public static CMotifResults findSecondaryStructures(List<ISequence> sequences) {
        CMotifResults results = new CMotifResults();
        CMotif helix = new CMotif(CConstants.FeatureType.SECONDARY_STRUCTURE, "Alpha helix", "H+");
        CMotif sheet = new CMotif(CConstants.FeatureType.SECONDARY_STRUCTURE, "Beta sheet", "E+");
        results.add(helix);
        results.add(sheet);
        for (ISequence sequence : sequences) {
            String prediction = predictAlignedSecondaryStructure(sequence.getSequence());
            CStructureHelper.getFeatures(sequence, prediction, helix);
            CStructureHelper.getFeatures(sequence, prediction, sheet);
        }
        return results;
    }

    protected static void getFeatures(ISequence sequence, String prediction, CMotif motif) {
        Pattern pattern = Pattern.compile(motif.getRegex());
        Matcher matcher = pattern.matcher(prediction);
        boolean result = matcher.find();
        while (result) {
            CFeature feature = new CFeature();
            feature.setStart(matcher.start());
            feature.setEnd(matcher.end());
            feature.setType(motif.getRegex());
            feature.setGroup(CConstants.FeatureType.SECONDARY_STRUCTURE.name());
            feature.setMatch(sequence.getSequence().substring(matcher.start(), matcher.end()));
            feature.setDescription(motif.getName());
            motif.add(feature);
            result = matcher.find();
        }
    }

    public static CTable findSecondaryStructures(Map<String, String> sequences, CMessageWriter writer) {
        CTable table = new CTable();
        table.getHeader().add("SEQUENCE");
        table.getHeader().add("secondary");
        writer.write("SEQUENCE\tsecondary\n");
        writer.flush();
        for (String accession : sequences.keySet()) {
            try {
                String sequence = sequences.get(accession);
                String secondary = predictSecondaryStructure(sequence);
                CLocation location = convertSecondaryStructure(secondary);
                if (location.getLocations().isEmpty()) continue;
                CTable.Row row = table.addRow();
                row.add(accession);
                row.add(location.toString());
                writer.write(accession + "\t" + location.toString() + "\n");
                writer.flush();
                CRuntimeHelper.sleep(500);
            } catch (Exception e) {
                writer.error(e);
                CRuntimeHelper.sleep(5000);
            }
        }
        return table;
    }

    public static String predictAlignedSecondaryStructure(String original) {
        String sequence = CStringHelper.replace(original, "-", "");
        String structure = predictSecondaryStructure(sequence);
        StringBuilder buffer = new StringBuilder();
        int position = 0;
        for (int index = 0; index < original.length(); index++) {
            if (position >= structure.length()) {
                buffer.append("-");
                continue;
            }
            char aa = original.charAt(index);
            if (aa == '-') buffer.append('-'); else {
                buffer.append(structure.charAt(position));
                position++;
            }
        }
        String aligned = buffer.toString();
        return aligned;
    }

    public static String predictSecondaryStructure(String sequence) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("option", "none");
        model.put("name", "");
        model.put("text", sequence);
        String response = CHttpHelper.postRequest(NNPREDICT_URL, model);
        int start = response.lastIndexOf("<tt>");
        start += 4;
        int end = response.indexOf("</tt>", start);
        String structure = response.substring(start, end);
        structure = CStringHelper.replace(structure, "<br>", "").trim();
        return structure;
    }

    public static CLocation convertSecondaryStructure(String secondary) {
        CLocation location = new CLocation();
        String regex = "H+|E+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(secondary);
        while (matcher.find()) {
            if (matcher.group().substring(0, 1).equals("H")) location.add("H", matcher.start(), matcher.end()); else if (matcher.group().substring(0, 1).equals("E")) location.add("E", matcher.start(), matcher.end());
        }
        return location;
    }

    public static String predictTransmembraneDomains(String sequence) {
        String filename = CFileHelper.createTempFile("vardb-tm", "fasta");
        CFileHelper.appendFile(filename, sequence);
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("prog", "one");
        model.put("file", filename);
        String response = CHttpHelper.postMultipartRequest(TSEG_URL, model);
        return response;
    }
}
