public class Test {            public FileChannel createFileChannel() throws IOException {
                return new FileInputStream(file.getAbsolutePath()).getChannel();
            }
}