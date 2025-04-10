public class Test {    public static void streamCopy(FileInputStream fis, OutputStream ostream) throws IOException {
        byte[] readBytes = new byte[1024];
        int nBytes;
        while (true) {
            nBytes = fis.read(readBytes, 0, readBytes.length);
            if (nBytes == -1 || nBytes == 0) break;
            ostream.write(readBytes, 0, nBytes);
            ostream.flush();
        }
    }
}