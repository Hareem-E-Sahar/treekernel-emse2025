public class Test {    public static void pipe(final InputStream in, final OutputStream out, final boolean isBlocking, final ByteFilter filter) throws IOException {
        byte[] buf = new byte[MAX_BUFFER_SIZE];
        int nread;
        int navailable;
        int total = 0;
        synchronized (in) {
            while (((navailable = isBlocking ? buf.length : in.available()) > 0) && ((nread = in.read(buf, 0, Math.min(buf.length, navailable))) >= 0)) {
                if (filter == null) {
                    out.write(buf, 0, nread);
                } else {
                    final byte[] filtered = filter.filter(buf, nread);
                    out.write(filtered);
                }
                total += nread;
            }
        }
        out.flush();
        buf = null;
    }
}