public class Test {        public FileOutChannel() throws IOException {
            file = File.createTempFile("xsockettest", ".tempMail");
            fileChannel = new RandomAccessFile(file, "rw").getChannel();
        }
}