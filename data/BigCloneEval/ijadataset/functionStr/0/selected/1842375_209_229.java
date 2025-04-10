public class Test {    public void testBasicDecodingFile() throws Exception {
        ReadableByteChannel channel = new ReadableByteChannelMockup(new String[] { "stuff; ", "more stuff; ", "a lot more stuff!!!" }, "US-ASCII");
        HttpParams params = new BasicHttpParams();
        SessionInputBuffer inbuf = new SessionInputBufferImpl(1024, 256, params);
        HttpTransportMetricsImpl metrics = new HttpTransportMetricsImpl();
        LengthDelimitedDecoder decoder = new LengthDelimitedDecoder(channel, inbuf, metrics, 36);
        File fileHandle = File.createTempFile("testFile", ".txt");
        RandomAccessFile testfile = new RandomAccessFile(fileHandle, "rw");
        FileChannel fchannel = testfile.getChannel();
        long pos = 0;
        while (!decoder.isCompleted()) {
            long bytesRead = decoder.transfer(fchannel, pos, 10);
            if (bytesRead > 0) {
                pos += bytesRead;
            }
        }
        assertEquals(testfile.length(), metrics.getBytesTransferred());
        fchannel.close();
        assertEquals("stuff; more stuff; a lot more stuff!", readFromFile(fileHandle));
        fileHandle.delete();
    }
}