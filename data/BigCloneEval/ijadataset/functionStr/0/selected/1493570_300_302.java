public class Test {    public boolean writeToFile(RandomAccessFile file) throws UnableToWriteLumpFileException {
        return writeToFile(file.getChannel());
    }
}