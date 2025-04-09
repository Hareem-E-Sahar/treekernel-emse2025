public class Test {    @Override
    public ArchiveEntry getNextEntry() throws IOException {
        return tarIn.getNextEntry();
    }
}