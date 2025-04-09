import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import com.mysql.jdbc.Connection;

/**
 * Main class of the entire project: runs the Sequence Screening program This
 * class implements a possible interpretation of the Government guidelines.
 */
public class Screen {

    static final Scanner com = new Scanner(System.in);

    int div = 0;

    String programP = "";

    String dataP = "";

    int wordSP = 3;

    String matrix = "BLOSUM62";

    int gapopP = 11;

    int gapextP = 1;

    String programN = "";

    String dataN = "";

    int wordSN = 28;

    int match = 1;

    int mismatch = -2;

    int gapopN = 0;

    int gapextN = 0;

    double threshold = 0;

    String keyData = "";

    int antikey = 0;

    int version = 1;

    String[][] keywords = null;

    Connection conn = null;

    int data = 0;

    int id_test;

    int id_seq;

    int num_blast;

    int num_th;

    int system = 0;

    String SA = "";

    int nb_subsequences = -1;

    HashMap<String, String> gi_screened;

    public int get_num_blast() {
        return num_blast;
    }

    public int get_version() {
        return version;
    }

    public int get_div() {
        return div;
    }

    public String get_programP() {
        return programP;
    }

    public String get_dataP() {
        return dataP;
    }

    public int get_wordSP() {
        return wordSP;
    }

    public String get_matrix() {
        return matrix;
    }

    public int get_gapopP() {
        return gapopP;
    }

    public int get_gapextP() {
        return gapextP;
    }

    public String get_programN() {
        return programN;
    }

    public String get_dataN() {
        return dataN;
    }

    public int get_wordSN() {
        return wordSN;
    }

    public int get_match() {
        return match;
    }

    public int get_mismatch() {
        return mismatch;
    }

    public int get_gapopN() {
        return gapopN;
    }

    public int get_gapextN() {
        return gapextN;
    }

    public double get_threshold() {
        return threshold;
    }

    public String get_keyData() {
        return keyData;
    }

    public int get_antikey() {
        return antikey;
    }

    public String[][] get_keywords() {
        return keywords;
    }

    public Connection get_connection() {
        return conn;
    }

    public String get_SA() {
        return SA;
    }

    public HashMap<String, String> get_GiScreened() {
        return gi_screened;
    }

    /**
	 * Constructor
	 * @param para parameters set
	 * @param _conn connection to database
	 * @param id_t test id
	 * @param num number of blast in one time
	 * @param dat database used
	 * @param sys operating system used
	 * @throws SQLException
	 * @throws Exception
	 */
    public Screen(ResultSet para, Connection _conn, int id_t, int num, int dat, int sys) throws SQLException, Exception {
        if (!para.first()) {
            System.out.println("**************************ERROR**************************");
            System.out.println("Error : parameters set empty");
            throw new Exception();
        }
        system = sys;
        data = dat;
        id_test = id_t;
        conn = _conn;
        div = para.getInt(3);
        programP = para.getString(5);
        dataP = para.getString(6);
        wordSP = para.getInt(7);
        matrix = para.getString(8);
        gapopP = para.getInt(9);
        gapextP = para.getInt(10);
        programN = para.getString(11);
        dataN = para.getString(12);
        wordSN = para.getInt(14);
        match = para.getInt(15);
        mismatch = para.getInt(16);
        gapopN = para.getInt(17);
        gapextN = para.getInt(18);
        threshold = para.getDouble(20);
        if (para.getString(21).equals("extensive")) keyData = "keywords_extensive"; else {
            if (para.getString(21).equals("new")) keyData = "keywords_extensive_new"; else keyData = "keyword_limited";
        }
        if (para.getString(22).equals("yes")) antikey = 1; else antikey = 0;
        String blastv = para.getString(23);
        if (blastv.equals("local_parallel") || blastv.equals("sirion_100_parallel") || blastv.equals("hermes_blast") || blastv.equals("mpi_blast")) {
            programP = "blastp -task " + para.getString(5);
            programN = "blastn -task " + para.getString(11);
            version = 1;
            if (blastv.equals("mpi_blast")) version = 3;
        } else {
            if (blastv.equals("local_serial")) {
                version = 1;
            } else {
                if (blastv.equals("online")) version = 2;
            }
        }
        Keywords K = new Keywords(_conn);
        keywords = K.getKeywords(keyData);
        gi_screened = new HashMap<String, String>();
        num_th = num;
    }

    /**
 * second constructor
 * @param _conn
 * @param vers
 * @throws SQLException
 * @throws Exception
 */
    public Screen(Connection _conn, int vers) throws SQLException, Exception {
        conn = _conn;
        dataN = "nt";
        dataP = "nr";
        programN = "blastn";
        programP = "blastp";
        keyData = "keywords_extensive";
        antikey = 1;
        version = vers;
        Keywords K = new Keywords(_conn);
        keywords = K.getKeywords(keyData);
        div = 200;
        gi_screened = new HashMap<String, String>();
    }

    /**create one sequence array from two sequence arrays.
	 * @param A first sequence array to be completed
	 * @param B second sequence array to add
	 * @return new sequence array containing the two sequence arrays.
	 */
    private Seq[] concat(Seq[] A, Seq[] B) {
        Seq[] C = new Seq[A.length + B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);
        return C;
    }

    /** make the blast in parallel
	 * @param query sequence to be screened
	 * @return boolean array: first result indicates if it is a hit on the nucleotide or not, the second one indicates if there is a hit on the protein.
	 */
    private boolean[] parallel(Seq query, int seq_id) throws Exception {
        Seq[] Final = {};
        Seq[] list = query.doSixFrame();
        int k = 0;
        BufferedWriter[][] bb = null;
        for (int i = 0; i < 8; i++) {
            Seq[] div = list[i].divide(this.get_div());
            if (i == 0) {
                k = div.length + 1;
                bb = new BufferedWriter[8][k];
            }
            for (int j = 0; j < k; j++) {
                bb[i][j] = new BufferedWriter(new FileWriter("analyse_results/" + data + "_" + id_test + "_" + id_seq + "/" + i + "_" + j + ".txt"));
            }
            Final = concat(Final, div);
        }
        ParallelBlast parallelblast = new ParallelBlast(this, Final, bb, id_test, id_seq, version, data);
        boolean[] tab = parallelblast.hit(num_th);
        SA = parallelblast.get_SA();
        num_blast = parallelblast.get_num_blast();
        nb_subsequences = parallelblast.nb_subsequences;
        for (int i = 0; i < bb.length; i++) {
            for (int j = 0; j < bb[i].length; j++) bb[i][j].close();
        }
        return tab;
    }

    private boolean[] serial(Seq query) throws Exception {
        boolean[] bool_tab = { false, false };
        Seq[] list = query.doSixFrame();
        ArrayList<Integer> frame = new ArrayList<Integer>();
        ArrayList<Integer> subsequence = new ArrayList<Integer>();
        BufferedWriter[][] bb = new BufferedWriter[0][0];
        int z = 0;
        for (int i = 0; i < 2; i++) {
            Seq[] div = list[i].divide(this.get_div());
            if (i == 0) z = div.length + 1;
            if (i == 0) bb = new BufferedWriter[8][z];
            for (int k = 0; k < z; k++) bb[i][k] = new BufferedWriter(new FileWriter("analyse_results/" + data + "_" + id_test + "_" + id_seq + "/" + i + "_" + k + ".txt"));
            num_blast = div.length * 8;
            for (int j = 0; j < div.length; j++) {
                bb[i][j].write("nucleotide check: frame: " + (i + 1) + " subsequence: " + (j + 1));
                bb[i][j].newLine();
                if (div[j].hit(version, this, bb)) {
                    frame.add(i + 1);
                    subsequence.add(j + 1);
                    SA = div[j].get_SA();
                    bool_tab[0] = true;
                }
            }
        }
        for (int i = 2; i < 8; i++) {
            Seq[] div = list[i].divide(this.get_div());
            for (int k = 0; k < z; k++) bb[i][k] = new BufferedWriter(new FileWriter(i + "_" + k + ".txt"));
            num_blast = div.length * 8;
            for (int j = 0; j < div.length; j++) {
                bb[i][j].write("protein check: frame: " + (i + 1) + " subsequence: " + (j + 1));
                bb[i][j].newLine();
                if (div[j].hit(version, this, bb)) {
                    frame.add(i + 1);
                    subsequence.add(j + 1);
                    SA = div[j].get_SA();
                    bool_tab[1] = true;
                }
            }
        }
        for (int i = 0; i < bb.length; i++) {
            for (int j = 0; j < bb[i].length; j++) bb[i][j].close();
        }
        return bool_tab;
    }

    public boolean[] screen(String sequence, int seq_id) throws SQLException, Exception {
        id_seq = seq_id;
        String[] commands = { "mkdir", "analyse_results/" + data + "_" + id_test + "_" + id_seq };
        if (system == 1) {
            BufferedWriter bw = new BufferedWriter(new FileWriter("del.bat"));
            bw.write("cd analyse_results");
            bw.newLine();
            bw.write("md " + data + "_" + id_test + "_" + id_seq);
            bw.close();
            commands[0] = "del.bat";
            commands[1] = "";
        }
        Process child = Runtime.getRuntime().exec(commands);
        child.waitFor();
        String[] command = { "mkdir", "output/" + id_seq };
        if (system == 1) {
            BufferedWriter bw = new BufferedWriter(new FileWriter("del.bat"));
            bw.write("cd output");
            bw.newLine();
            bw.write("md " + id_seq);
            bw.close();
            command[0] = "del.bat";
            command[1] = "";
        }
        child = Runtime.getRuntime().exec(command);
        child.waitFor();
        child.getErrorStream().close();
        child.getInputStream().close();
        child.getOutputStream().close();
        child.destroy();
        boolean[] tab = { false, false };
        sequence = sequence.trim();
        sequence = sequence.toLowerCase();
        sequence = sequence.replaceAll("\\s", " ");
        Seq query = new Seq(Nature.DNA, sequence, div);
        if (query.getSymbolList().length() < 200) {
            System.out.println("DNA sequences smaller than 200 bp: non hit");
            return tab;
        }
        if ((version == 1 || version == 3)) {
            tab = parallel(query, seq_id);
        } else {
            tab = serial(query);
        }
        String[] command2 = { "sh", "-c", "rm -r -f output/" + id_seq };
        if (system == 1) {
            BufferedWriter bw = new BufferedWriter(new FileWriter("del.bat"));
            bw.write("cd output");
            bw.newLine();
            bw.write("rd " + id_seq);
            bw.close();
            command2[0] = "del.bat";
            command2[1] = "";
            command2[2] = "";
        }
        Process child2 = Runtime.getRuntime().exec(command2);
        child2.waitFor();
        child2.getErrorStream().close();
        child2.getInputStream().close();
        child2.getOutputStream().close();
        child2.destroy();
        return tab;
    }
}
