public class Test {    public Dbf(String name) throws java.io.IOException, DbfFileException {
        if (DEBUG) System.out.println("---->uk.ac.leeds.ccg.dbffile.Dbf constructed. Will identify itself as " + DBC);
        URL url = new URL(name);
        URLConnection uc = url.openConnection();
        InputStream in = uc.getInputStream();
        EndianDataInputStream sfile = new EndianDataInputStream(in);
        init(sfile);
    }
}