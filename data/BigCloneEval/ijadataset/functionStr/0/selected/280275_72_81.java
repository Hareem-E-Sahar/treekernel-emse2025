public class Test {    private FileChannel getFile(String filename) throws IOException {
        FileChannel fc = openFiles.get(filename);
        if (fc == null) {
            File dbTable = new File(dbDir, filename);
            RandomAccessFile f = new RandomAccessFile(dbTable, "rws");
            fc = f.getChannel();
            openFiles.put(filename, fc);
        }
        return fc;
    }
}