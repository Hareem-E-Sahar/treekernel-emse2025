public class Test {    public void testDecodingFileWithBufferedSessionData() throws Exception {
        ReadableByteChannel channel = new ReadableByteChannelMockup(new String[] { "stuff; ", "more stuff; ", "a lot more stuff!" }, "US-ASCII");
        HttpParams params = new BasicHttpParams();
        SessionInputBuffer inbuf = new SessionInputBufferImpl(1024, 256, params);
        HttpTransportMetricsImpl metrics = new HttpTransportMetricsImpl();
        IdentityDecoder decoder = new IdentityDecoder(channel, inbuf, metrics);
        int i = inbuf.fill(channel);
        assertEquals(7, i);
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
        assertEquals(testfile.length() - 7, metrics.getBytesTransferred());
        fchannel.close();
        assertEquals("stuff; more stuff; a lot more stuff!", readFromFile(fileHandle));
        fileHandle.delete();
    }
}