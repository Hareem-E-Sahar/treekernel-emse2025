package org.vardb.analysis;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.vardb.sequences.CCompoundLocation;
import org.vardb.util.CHttpHelper;
import org.vardb.util.CMessageWriter;
import org.vardb.util.CStringHelper;
import org.vardb.util.CThreadHelper;

public class CNnPredictHelper {

    public static final String DEFAULT_SERVER = "http://alexander.compbio.ucsf.edu/cgi-bin/nnpredict.pl";

    protected String server = DEFAULT_SERVER;

    public CNnPredictHelper() {
    }

    public CNnPredictHelper(String server) {
        this.server = server;
    }

    public Map<String, String> findSecondaryStructures(Map<String, String> sequences, CMessageWriter writer) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        writer.write("SEQUENCE\tsecondary\n");
        writer.flush();
        for (String accession : sequences.keySet()) {
            try {
                String sequence = sequences.get(accession);
                String secondary = predictSecondaryStructure(this.server, sequence);
                CCompoundLocation location = convertSecondaryStructure(secondary);
                if (location.getLocations().isEmpty()) continue;
                map.put(accession, location.toString());
                writer.write(accession + "\t" + location.toString() + "\n");
                writer.flush();
                CThreadHelper.sleep(500);
            } catch (Exception e) {
                writer.error(e);
                CThreadHelper.sleep(5000);
            }
        }
        return map;
    }

    public String predictSecondaryStructure(String nnpredictServer, String sequence) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("option", "none");
        model.put("name", "");
        model.put("text", sequence);
        String response = CHttpHelper.postRequest(nnpredictServer, model);
        int start = response.lastIndexOf("<tt>");
        start += 4;
        int end = response.indexOf("</tt>", start);
        String structure = response.substring(start, end);
        structure = CStringHelper.replace(structure, "<br>", "").trim();
        return structure;
    }

    private static final String H = "H";

    private static final String E = "E";

    public CCompoundLocation convertSecondaryStructure(String secondary) {
        CCompoundLocation location = new CCompoundLocation();
        CCompoundLocation.NamedLocation hloc = new CCompoundLocation.NamedLocation(H);
        CCompoundLocation.NamedLocation eloc = new CCompoundLocation.NamedLocation(E);
        location.add(hloc);
        location.add(eloc);
        String regex = "H+|E+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(secondary);
        while (matcher.find()) {
            String value = matcher.group().substring(0, 1);
            int start = matcher.start() + 1;
            int end = matcher.end();
            if (H.equals(value)) hloc.add(start, end); else if (E.equals(value)) eloc.add(start, end);
        }
        return location;
    }
}
