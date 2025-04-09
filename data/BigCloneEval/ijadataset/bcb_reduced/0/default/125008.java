import java.sql.ResultSet;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

/**
 * To scan the GenoCAD database
 */
public class Screen_Sequences {

    Connection conn;

    public Screen_Sequences(Connection con) {
        conn = con;
    }

    /**
	 * Screen one sequence
	 * @param seq sequence to be screened
	 * @param id_seq its id
	 * @param test test to be done
	 * @param read to read data from database
	 * @param reg registry id
	 * @param num number of blast in one time
	 * @throws Exception
	 */
    public void one_seq(String seq, int id_seq, int test, DatabaseReader read, String reg, int num, int system) throws Exception {
        ResultSet test_rset = read.read_Parameter(test);
        Screen s = new Screen(test_rset, conn, test, num, 5, system);
        long start = System.currentTimeMillis();
        seq = seq.toLowerCase();
        boolean[] hit = s.screen(seq, id_seq);
        long end = System.currentTimeMillis();
        write(reg, hit, (int) ((end - start) / 1000), test, id_seq, s.nb_subsequences);
        System.out.println("sequence screened " + id_seq);
        if (!hit[1] && !hit[0]) {
            String[] commands = { "sh", "-c", "rm -r -f analyse_results/5_" + test + "_" + id_seq };
            Process child = Runtime.getRuntime().exec(commands);
            child.waitFor();
            child.getErrorStream().close();
            child.getInputStream().close();
            child.getOutputStream().close();
            child.destroy();
        }
    }

    /**
	 * Write the result in database
	 * @param reg registry id
	 * @param hit tab with first one is 1 if hit
	 * @param time execution time
	 * @param test test done
	 * @param id id of the sequence in the database
	 * @throws Exception
	 */
    public void write(String reg, boolean[] hit, int time, int test, int id, int nb_subsequences) throws Exception {
        PreparedStatement esq = (PreparedStatement) conn.prepareStatement("select * from results_screening where id = ? and test=?");
        esq.setInt(1, id);
        esq.setInt(2, test);
        ResultSet rset = esq.executeQuery();
        String res = "no";
        if (hit[0] || hit[1]) res = "yes";
        if (rset.first()) {
            esq = (PreparedStatement) conn.prepareStatement("update results_screening set hit = ?, time = ?, nb_subsequences = ? where id = ? and test = ? ");
            esq.setString(1, res);
            esq.setInt(2, time);
            esq.setInt(3, nb_subsequences);
            esq.setInt(4, id);
            esq.setInt(5, test);
            esq.executeUpdate();
        } else {
            esq = (PreparedStatement) conn.prepareStatement("insert into results_screening values (?, ?, ?, ?, ?,?)");
            esq.setString(1, res);
            esq.setInt(2, time);
            esq.setInt(3, test);
            esq.setInt(4, id);
            esq.setString(5, reg);
            esq.setInt(6, nb_subsequences);
            esq.executeUpdate();
        }
    }

    /**
	 * Run the screen for the sequence asked
	 * @param test test to do
	 * @param seq id of the sequence
	 * @param read to read in the database
	 * @param num number of blast in one time
	 */
    public void run(int test, int seq, DatabaseReader read, int num, int system) {
        try {
            PreparedStatement esq = (PreparedStatement) conn.prepareStatement("select sequence, name as description from sequences_for_screening where id = ? ");
            esq.setInt(1, seq);
            ResultSet entry = esq.executeQuery();
            if (!entry.first()) {
                System.out.print("no entry in the database with id= ");
                System.out.println(seq);
            } else {
                one_seq(entry.getString(1), seq, test, read, entry.getString(2), num, system);
            }
        } catch (Exception e) {
            System.out.println("***********************");
            System.out.println("Error in running GenoTHREAT");
            e.printStackTrace();
        }
    }
}
