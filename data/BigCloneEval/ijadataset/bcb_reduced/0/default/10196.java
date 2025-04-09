import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.util.StringTokenizer;

public class ExtractMPI {

    int gi = 0;

    int pid = 0;

    int align = 0;

    int gap = 0;

    int evalue = 0;

    int score = 0;

    int num_frame;

    int num_sub_sequence;

    BufferedWriter[][] bb;

    public ExtractMPI(int _gi, int _pid, int _align, int _gap, int _evalue, int _score, int frame, int sub_seq, BufferedWriter[][] _bb) {
        gi = _gi;
        pid = _pid;
        align = _align;
        gap = _gap;
        evalue = _evalue;
        score = _score;
        num_frame = frame;
        num_sub_sequence = sub_seq;
        bb = _bb;
    }

    /**
			 * the command correspondent to that extract class is: lynx -source"http://www.ncbi.nlm.nih.gov/blast/Blast.cgi?RID=1HT0M2H101N&CMD=Get&ALIGNMENT_VIEW=Tabular&FORMAT_TYPE=Text&SHOW_OVERVIEW=no"
			 * >result.txt
			 */
    public String[][] extract(String file, Nature nat) throws Exception {
        int pos;
        if (nat == Nature.DNA) {
            pos = 3;
        } else {
            pos = 4;
        }
        String[][] tabData = new String[0][0];
        try {
            String[][] empty = {};
            BufferedReader in = new BufferedReader(new FileReader(file));
            String line2;
            while ((line2 = in.readLine()) != null) {
                if (line2.indexOf("No significant similarity found") != -1) return empty;
            }
            FileReader FR = new FileReader(file);
            BufferedReader buff = new BufferedReader(FR);
            int nbData = gi + pid + align + gap + evalue + score;
            int nbLines = 0;
            while (buff.ready()) {
                buff.readLine();
                nbLines++;
            }
            tabData = new String[nbLines][nbData + 2];
            FileReader lec = new FileReader(file);
            BufferedReader BF = new BufferedReader(lec);
            int i = 0;
            while (i < nbLines) {
                String line = BF.readLine();
                StringTokenizer tok = new StringTokenizer(line, "\t");
                int c = 0;
                for (int j = 0; j < 12; j++) {
                    if (!tok.hasMoreTokens()) {
                        System.out.println("Error: incomplete file");
                        return tabData;
                    }
                    if (j == 1 && gi == 1) {
                        String gi = tok.nextToken();
                        int gi_end = gi.indexOf('|', 4);
                        tabData[i][c] = gi.substring(3, gi_end);
                        bb[num_frame][num_sub_sequence].write("gi:" + tabData[i][c]);
                        c++;
                    } else {
                        if (j == 2 && pid == 1) {
                            tabData[i][c] = tok.nextToken();
                            bb[num_frame][num_sub_sequence].write(", PI:" + tabData[i][c]);
                            c++;
                        } else {
                            if (j == pos && align == 1) {
                                tabData[i][c] = tok.nextToken();
                                bb[num_frame][num_sub_sequence].write(", QC:" + tabData[i][c]);
                                c++;
                            } else {
                                if (j == (pos + 3) && align == 1) {
                                    tabData[i][c] = tok.nextToken();
                                    bb[num_frame][num_sub_sequence].write(", start:" + tabData[i][c]);
                                    c++;
                                } else {
                                    if (j == (pos + 4) && align == 1) {
                                        tabData[i][c] = tok.nextToken();
                                        bb[num_frame][num_sub_sequence].write(", end:" + tabData[i][c]);
                                        bb[num_frame][num_sub_sequence].newLine();
                                    } else {
                                        tok.nextToken();
                                    }
                                }
                            }
                        }
                    }
                }
                i++;
            }
        } catch (Exception e) {
            System.out.println("************************ERROR********************");
            System.out.println("Error : " + e);
            throw e;
        }
        return tabData;
    }
}
