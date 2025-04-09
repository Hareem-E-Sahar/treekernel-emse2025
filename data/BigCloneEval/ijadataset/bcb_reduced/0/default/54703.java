import java.io.*;
import java.util.*;

/**
 * Class to extract data from a local blast result.
 */
public class ExtractLocal {

    int nbData;

    int num_frame;

    int num_sub_sequence;

    BufferedWriter[][] bb;

    /**
	 * Constructor
	 * @param dat number of data we want
	 * @param num_f current frame
	 * @param num_sub_s current sub sequence
	 * @param b tab of files for writing information
	 */
    public ExtractLocal(int dat, int num_f, int num_sub_s, BufferedWriter[][] b) {
        nbData = dat;
        num_frame = num_f;
        num_sub_sequence = num_sub_s;
        bb = b;
    }

    /**
	 * Extract the data we want and put them in an array
	 * Requiert the file to be a blast result from local: just a tab containing data we asked for
	 * @param file input file containing the data
	 * @return array of the data we want
	 */
    public String[][] extract(String file) throws Exception {
        String[][] tabData = new String[0][0];
        try {
            FileReader FR = new FileReader(file);
            BufferedReader buff = new BufferedReader(FR);
            int nbLines = 0;
            while (buff.ready()) {
                buff.readLine();
                nbLines++;
            }
            buff.close();
            FR.close();
            tabData = new String[nbLines][nbData];
            FileReader lec = new FileReader(file);
            BufferedReader BF = new BufferedReader(lec);
            int i = 0;
            while (BF.ready()) {
                String line = BF.readLine();
                StringTokenizer tok = new StringTokenizer(line, "\t");
                for (int j = 0; j < nbData; j++) {
                    if (tok.hasMoreTokens()) tabData[i][j] = tok.nextToken();
                    bb[num_frame][num_sub_sequence].write(tabData[i][j] + " ");
                }
                bb[num_frame][num_sub_sequence].newLine();
                i++;
            }
            BF.close();
            lec.close();
        } catch (IOException e) {
            System.out.println("**********************************ERROR***********************");
            System.out.println("error in extraction of local blast");
            throw e;
        }
        return tabData;
    }
}
