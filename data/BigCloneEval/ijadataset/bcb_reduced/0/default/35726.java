import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 *Execute Blast parallely, either in local machine or in a server
 *Needs to have a local copy of ncbi blast and ncbi database nt and nr
 */
public class ParallelBlast {

    Screen sc;

    boolean[] bool_tab = { false, false };

    Seq[] list;

    Seq[] todo;

    BufferedWriter[][] bb;

    BufferedWriter bgeneral;

    int id_test;

    int id_seq;

    int num_blast;

    String SA = "";

    int blast_version;

    int data;

    int nb_subsequences = 0;

    /**
	 * Gives select agent found
	 * @return select agent found, "" if not
	 */
    public String get_SA() {
        return SA;
    }

    /**
	 * Gives the number of blast done
	 * @return total number of blast
	 */
    public int get_num_blast() {
        return num_blast;
    }

    private Seq[] concat(Seq[] A, Seq[] B) {
        Seq[] C = new Seq[A.length + B.length];
        System.arraycopy(A, 0, C, 0, A.length);
        System.arraycopy(B, 0, C, A.length, B.length);
        return C;
    }

    /**
	 * Constructor
	 * @param s set of parameters
	 * @param _list list of sequence to blast
	 * @param b tab of file to write in
	 * @param id_t test id to be done
	 * @param id_s sequence screened
	 */
    public ParallelBlast(Screen s, Seq[] _list, BufferedWriter[][] b, int id_t, int id_s, int bversion, int _data) {
        sc = s;
        list = _list;
        todo = new Seq[0];
        bb = b;
        id_test = id_t;
        id_seq = id_s;
        blast_version = bversion;
        data = _data;
    }

    /**
 * Execute the blast in different threads 
 * @param tab thread to use
 * @param count current position in the blast list
 * @param second_time extension or not
 * @throws Exception
 */
    public void thread_blast(LocalBlastThread[] tab, int count, int second_time) throws Exception {
        InterpreteResult IR = new InterpreteResult();
        for (int j = 0; j < tab.length; j++) {
            tab[j] = new LocalBlastThread(list[count + j], "output/" + id_seq + "/fi" + j + ".txt", "output/" + id_seq + "/fo" + j + ".txt", sc, list[count + j].get_num_frame(), list[count + j].get_sub_sequence(), bb, blast_version);
            tab[j].start();
        }
        for (int j = 0; j < tab.length; j++) tab[j].join();
        for (int i = 0; i < tab.length; i++) {
            if (tab[i].getExcept() == 1) throw tab[i].getExceptContent();
            String[][] br = tab[i].getBlastResult();
            int nat = 1;
            if (list[count + i].getNature() == Nature.DNA) nat = 0;
            br = IR.set_QC(br);
            boolean hit = IR.interpreteResult(br, list[count + i].getSymbolList().length(), sc, list[count + i].get_num_frame(), list[count + i].get_sub_sequence(), bb);
            if (hit) {
                nb_subsequences = nb_subsequences + 1;
                SA = IR.get_SA();
                if (second_time == 0) {
                    bb[list[count + i].get_num_frame()][list[count + i].get_sub_sequence()].write("hit in the middle");
                    bb[list[count + i].get_num_frame()][list[count + i].get_sub_sequence()].newLine();
                    bgeneral.write("hit in the middle for frame: " + list[count + i].get_num_frame() + ", subsequence: " + list[count + i].get_sub_sequence());
                    bgeneral.newLine();
                } else {
                    bb[list[count + i].get_num_frame()][list[count + i].get_sub_sequence()].write("hit on an extension (left or right)");
                    bb[list[count + i].get_num_frame()][list[count + i].get_sub_sequence()].newLine();
                    bgeneral.write("hit on an extension for frame: " + list[count + i].get_num_frame() + ", subsequence: " + list[count + i].get_sub_sequence());
                    bgeneral.newLine();
                }
            } else {
                if (second_time == 0) {
                    bb[list[count + i].get_num_frame()][list[count + i].get_sub_sequence()].write("No hit in the middle");
                    bb[list[count + i].get_num_frame()][list[count + i].get_sub_sequence()].newLine();
                } else {
                    bb[list[count + i].get_num_frame()][list[count + i].get_sub_sequence()].write("No hit for this extension (left or right)");
                    bb[list[count + i].get_num_frame()][list[count + i].get_sub_sequence()].newLine();
                }
            }
            if (IR.get_found()) {
                bool_tab[nat] = bool_tab[nat] || hit;
            } else {
                if (second_time == 0) {
                    extension_right(list[count + i], IR, br);
                    extension_left(list[count + i], IR, br);
                }
            }
        }
    }

    /**
	 * Search if extension has to be done on right
	 * @param sequence current sequence to extend
	 * @param IR result object
	 * @param br blast result
	 * @throws Exception
	 */
    public void extension_right(Seq sequence, InterpreteResult IR, String[][] br) throws Exception {
        ExtensionMethod EM = new ExtensionMethod(sequence.getNature(), sequence.get_num_frame(), sequence.get_sub_sequence(), bb);
        if (sequence.getSymbolListAfter() != null) {
            String[] SpotOnRight = EM.findSpotOnRight(br, sequence.get_div(), sc, sequence.getSymbolListAfter().length());
            for (int i = 0; i < SpotOnRight.length; i++) {
                int pos = Integer.parseInt(SpotOnRight[i]);
                Seq[] stab = { EM.new_Seq(sequence, pos, true) };
                todo = concat(todo, stab);
            }
        }
    }

    /**
		 * Search if extension has to be done on left
		 * @param sequence current sequence to extend
		 * @param IR result object
		 * @param br blast result
		 * @throws Exception
		 */
    public void extension_left(Seq sequence, InterpreteResult IR, String[][] br) throws Exception {
        ExtensionMethod EM = new ExtensionMethod(sequence.getNature(), sequence.get_num_frame(), sequence.get_sub_sequence(), bb);
        if (sequence.getSymbolListBefore() != null) {
            String[] SpotOnLeft = EM.findSpotOnLeft(br, sequence.get_div(), sc, sequence.getSymbolListBefore().length());
            for (int i = 0; i < SpotOnLeft.length; i++) {
                int pos = Integer.parseInt(SpotOnLeft[i]);
                Seq[] stab = { EM.new_Seq(sequence, pos, false) };
                todo = concat(todo, stab);
            }
        }
    }

    /**
 * Destroy files in output folder
 * @throws Exception
 */
    public void destroy() throws Exception {
        String[] rmcom = { "sh", "-c", "rm output/" + id_seq + "/*" };
        Process rm = Runtime.getRuntime().exec(rmcom);
        rm.waitFor();
        rm.getErrorStream().close();
        rm.getInputStream().close();
        rm.getOutputStream().close();
        rm.destroy();
    }

    /**
		 * Manage the blast list to do in wages of num blast
		 * @param num number of blast in one time
		 * @param ext extension or not
		 * @throws Exception
		 */
    public void doAllBlast(int num, int ext) throws Exception {
        int count = 0;
        while (count + num < list.length) {
            LocalBlastThread[] tab = new LocalBlastThread[num];
            thread_blast(tab, count, ext);
            destroy();
            count += num;
        }
        if (count < list.length) {
            LocalBlastThread[] tab = new LocalBlastThread[list.length - count];
            thread_blast(tab, count, ext);
            destroy();
        }
    }

    /**
		 * Screen the list of sequence
		 * @param num number of blast in one time
		 * @return hit or not (nucleotide, amino acid)
		 * @throws Exception
		 */
    public boolean[] hit(int num) throws Exception {
        bgeneral = new BufferedWriter(new FileWriter("analyse_results/" + data + "_" + id_test + "_" + id_seq + "/GeneralInformation.txt"));
        doAllBlast(num, 0);
        num_blast = list.length + todo.length;
        list = todo;
        doAllBlast(num, 1);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < bb[i].length; j++) {
                bb[i][j].close();
            }
        }
        bgeneral.close();
        return bool_tab;
    }
}
