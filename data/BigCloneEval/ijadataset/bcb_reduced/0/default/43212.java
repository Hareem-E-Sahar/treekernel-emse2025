import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class MPIblast {

    private String file_in;

    private String file_out;

    int num_frame;

    int num_sub_sequence;

    BufferedWriter[][] bb;

    /**
		 * Constructor
		 * @param fi file which will contain the query
		 * @param fo file which will contain the blast output
		 * @param num_f current frame
		 * @param num_sub_s current sub sequence
		 * @param b tab of files to write in
		 */
    public MPIblast(String fi, String fo, int num_f, int num_sub_s, BufferedWriter[][] b) {
        file_in = fi;
        file_out = fo;
        num_frame = num_f;
        num_sub_sequence = num_sub_s;
        bb = b;
    }

    /**
		 * do the local Blast and extract results
		 * @param sc set of parameters of the current screening
		 * @param s sequence to be blasted
		 * @return array of parameters we want
		 */
    public String[][] blast(Screen sc, Seq s) throws Exception {
        String database;
        String program;
        if (s.getNature() == Nature.DNA) {
            database = sc.get_dataN();
            program = "blastn";
        } else {
            database = sc.get_dataP();
            program = "blastp";
        }
        try {
            FileWriter fstream = new FileWriter(file_in);
            BufferedWriter out = new BufferedWriter(fstream);
            bb[num_frame][num_sub_sequence].write("sequence blasted: " + s.getSymbolList().seqString());
            bb[num_frame][num_sub_sequence].newLine();
            bb[num_frame][num_sub_sequence].write("Blast results:");
            bb[num_frame][num_sub_sequence].newLine();
            out.write(">seq");
            out.newLine();
            out.write(s.getSymbolList().seqString());
            out.close();
            fstream.close();
        } catch (IOException e) {
            System.out.println("**************************ERROR**************************");
            System.out.println("error while writing the sequence on file");
            System.out.println("sequence " + s.getSymbolList().seqString());
            throw e;
        }
        try {
            String[] commands = { "sh", "-c", "mpirun -np $NSLOTS mpiblast -d " + database + " -i " + file_in + " -p " + program + " -o " + file_out + " -F F -e 1e-3 -m 9 --removedb" };
            Process child = Runtime.getRuntime().exec(commands);
            child.waitFor();
            child.getErrorStream().close();
            child.getInputStream().close();
            child.getOutputStream().close();
            child.destroy();
        } catch (InterruptedException e) {
            System.out.println("**************************ERROR**************************");
            System.out.println("interuption while executing the blast request");
            throw e;
        }
        ExtractMPI res = new ExtractMPI(1, 1, 1, 0, 0, 0, num_frame, num_sub_sequence, bb);
        return res.extract(file_out, s.getNature());
    }
}
