public class Test {    public void testCodingCompletedFromFile() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel channel = newChannel(baos);
        HttpParams params = new BasicHttpParams();
        SessionOutputBuffer outbuf = new SessionOutputBufferImpl(1024, 128, params);
        HttpTransportMetricsImpl metrics = new HttpTransportMetricsImpl();
        LengthDelimitedEncoder encoder = new LengthDelimitedEncoder(channel, outbuf, metrics, 5);
        encoder.write(wrap("stuff"));
        File tmpFile = File.createTempFile("testFile", "txt");
        FileOutputStream fout = new FileOutputStream(tmpFile);
        OutputStreamWriter wrtout = new OutputStreamWriter(fout);
        wrtout.write("more stuff");
        wrtout.flush();
        wrtout.close();
        try {
            FileChannel fchannel = new FileInputStream(tmpFile).getChannel();
            encoder.transfer(fchannel, 0, 10);
            fail("IllegalStateException should have been thrown");
        } catch (IllegalStateException ex) {
        } finally {
            tmpFile.delete();
        }
    }
}