import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

class SimpleProteinPopulata {

    private String filename;

    private Vector<String> fNames;

    private Vector<Protein> proteinList;

    private int[] nReplicates = { 0, 0 };

    private Vector<String> groupNames;

    private Vector<String> hdrNames;

    Vector<Protein> getProteinList() {
        return proteinList;
    }

    Vector<String> getGroupNames() {
        return groupNames;
    }

    Vector<String> getHdrNames() {
        return hdrNames;
    }

    String getFilename() {
        return this.filename;
    }

    int getReplicateCount() {
        return Math.min(nReplicates[0], nReplicates[1]);
    }

    SimpleProteinPopulata() {
    }

    public void parseProtxml(String fname, boolean useCtrl, int group1size) {
        try {
            this.filename = fname;
            this.proteinList = new Vector<Protein>();
            this.groupNames = new Vector<String>();
            this.hdrNames = new Vector<String>();
            this.fNames = new Vector<String>();
            hdrNames.add("IPI");
            loadInformation(fname, useCtrl, group1size);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void loadInformation(String fname, boolean useCtrl, int group1size) throws IOException {
        if (!useCtrl) {
            groupNames.add("A");
            groupNames.add("B");
            int c = 0;
            String[] fs = fname.split("\\s+");
            for (int i = 0; i < fs.length; i++) {
                String str = fs[i].trim();
                if (!str.equals("")) {
                    fNames.add(str);
                    if (c < group1size) nReplicates[0]++; else nReplicates[1]++;
                    c++;
                }
            }
        } else {
            BufferedReader inputStream = new BufferedReader(new FileReader(fname));
            StringTokenizer tokens;
            String str;
            int cgroup = 0;
            while ((str = inputStream.readLine()) != null && (tokens = new StringTokenizer(str)).hasMoreTokens()) {
                str = tokens.nextToken();
                if (str.equals("GROUP:")) {
                    groupNames.add(tokens.nextToken());
                    cgroup++;
                    if (cgroup > 2) System.exit(1);
                } else {
                    nReplicates[cgroup - 1]++;
                    fNames.add(str);
                }
            }
        }
        ProteinLoader pload = new ProteinLoader(fNames);
        this.proteinList = pload.getProteinList();
        for (int i = proteinList.size(); i-- > 0; ) {
            double[] counts = proteinList.get(i).getSpectralCounts();
            for (int rep = 0; rep < 2; rep++) {
                double avg = 0;
                for (int u = 0; u < nReplicates[rep]; u++) {
                    int indx = u;
                    if (rep == 1) indx += nReplicates[0];
                    avg += counts[indx];
                }
                avg /= nReplicates[rep];
                (proteinList.get(i).getProteinInfo()).add(Double.toString(avg));
            }
        }
        hdrNames.add(this.groupNames.get(0) + " avgSC");
        hdrNames.add(this.groupNames.get(1) + " avgSC");
    }

    public void parseCSV(String fname) {
        this.filename = fname;
        this.proteinList = new Vector<Protein>();
        this.groupNames = new Vector<String>();
        this.hdrNames = new Vector<String>();
        parseSimpleProtFile(this.filename);
    }

    private void parseSimpleProtFile(String fname) {
        try {
            BufferedReader inputStream = new BufferedReader(new FileReader(fname));
            String line;
            String seperators = ",\t;: ";
            String sep = null;
            line = inputStream.readLine();
            int col = 0;
            int whichsep = 0;
            StringTokenizer tokens = null;
            while ((col <= 1) && (whichsep < seperators.length())) {
                sep = seperators.substring(whichsep, whichsep + 1);
                tokens = new StringTokenizer(line, sep);
                col = tokens.countTokens();
                whichsep++;
            }
            if (col <= 1) {
                System.out.println("Header line in " + fname + " does not contain identifiable seperators (tried comma, tab, semicolon, colon, space) - quitting");
                System.exit(1);
            }
            for (; col-- > 0; ) {
                hdrNames.add(tokens.nextToken());
            }
            Pattern pat = Pattern.compile("(\\D+)(\\d+)");
            col = hdrNames.size();
            Vector<String> tmp_groupNames = new Vector<String>();
            for (int replicate = 2; replicate-- > 0; ) {
                for (; col-- > 1; ) {
                    Matcher anum2 = pat.matcher(hdrNames.get(col));
                    Boolean match2 = anum2.matches();
                    Matcher anum1 = pat.matcher(hdrNames.get(col - 1));
                    nReplicates[replicate] += 1;
                    if ((!anum1.matches()) || (!match2) || (anum1.groupCount() != 2) || (anum2.groupCount() != 2) || (!anum1.group(1).equals(anum2.group(1)))) {
                        tmp_groupNames.add(anum2.group(1));
                        break;
                    }
                }
            }
            for (int i = tmp_groupNames.size(); i-- > 0; ) {
                groupNames.add(tmp_groupNames.get(i));
            }
            while (col < hdrNames.size()) {
                hdrNames.remove(col);
            }
            int minReplicates = Math.min(nReplicates[0], nReplicates[1]);
            while ((line = inputStream.readLine()) != null) {
                tokens = new StringTokenizer(line, sep);
                if (tokens.countTokens() != (nReplicates[0] + nReplicates[1] + hdrNames.size())) {
                    continue;
                }
                Vector<String> proteinInfo = new Vector<String>();
                for (int h = hdrNames.size(); h-- > 0; ) {
                    proteinInfo.add(tokens.nextToken());
                }
                double[] counts = new double[2 * minReplicates];
                double[] averages = { 0, 0 };
                int c = 0;
                for (int rep = 0; rep < 2; rep++) {
                    for (int i = 0; i < nReplicates[rep]; i++) {
                        if (i < minReplicates) {
                            counts[c] = Double.parseDouble(tokens.nextToken());
                            averages[rep] += counts[c++];
                        } else {
                            Double.parseDouble(tokens.nextToken());
                        }
                    }
                }
                proteinInfo.add(Double.toString(averages[0] / minReplicates));
                proteinInfo.add(Double.toString(averages[1] / minReplicates));
                Protein p = new Protein(proteinInfo, counts);
                proteinList.add(p);
            }
            inputStream.close();
            hdrNames.add(this.groupNames.get(0) + " avgSC");
            hdrNames.add(this.groupNames.get(1) + " avgSC");
        } catch (IOException e) {
            System.out.println("Error opening file " + fname + ": " + e.toString());
            System.exit(1);
        }
    }
}
