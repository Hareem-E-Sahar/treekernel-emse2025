public class Test {    public void testCodingBeyondContentLimitFile() throws Exception {
        ReadableByteChannel channel = new ReadableByteChannelMockup(new String[] { "stuff;", "more stuff; and a lot more stuff" }, "US-ASCII");
        HttpParams params = new BasicHttpParams();
        SessionInputBuffer inbuf = new SessionInputBufferImpl(1024, 256, params);
        HttpTransportMetricsImpl metrics = new HttpTransportMetricsImpl();
        LengthDelimitedDecoder decoder = new LengthDelimitedDecoder(channel, inbuf, metrics, 16);
        File fileHandle = File.createTempFile("testFile", ".txt");
        RandomAccessFile testfile = new RandomAccessFile(fileHandle, "rw");
        FileChannel fchannel = testfile.getChannel();
        long bytesRead = decoder.transfer(fchannel, 0, 6);
        assertEquals(6, bytesRead);
        assertFalse(decoder.isCompleted());
        assertEquals(6, metrics.getBytesTransferred());
        bytesRead = decoder.transfer(fchannel, 0, 10);
        assertEquals(10, bytesRead);
        assertTrue(decoder.isCompleted());
        assertEquals(16, metrics.getBytesTransferred());
        bytesRead = decoder.transfer(fchannel, 0, 1);
        assertEquals(-1, bytesRead);
        assertTrue(decoder.isCompleted());
        assertEquals(16, metrics.getBytesTransferred());
        fileHandle.delete();
    }
}