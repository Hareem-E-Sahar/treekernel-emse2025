public class Test {    public void testWriteBeyondFileSize() throws Exception {
        ReadableByteChannel channel = new ReadableByteChannelMockup(new String[] { "a" }, "US-ASCII");
        HttpParams params = new BasicHttpParams();
        SessionInputBuffer inbuf = new SessionInputBufferImpl(1024, 256, params);
        HttpTransportMetricsImpl metrics = new HttpTransportMetricsImpl();
        LengthDelimitedDecoder decoder = new LengthDelimitedDecoder(channel, inbuf, metrics, 1);
        File fileHandle = File.createTempFile("testFile", ".txt");
        RandomAccessFile testfile = new RandomAccessFile(fileHandle, "rw");
        FileChannel fchannel = testfile.getChannel();
        assertEquals(0, testfile.length());
        try {
            decoder.transfer(fchannel, 5, 10);
            fail("expected IOException");
        } catch (IOException iox) {
        }
        fileHandle.delete();
    }
}