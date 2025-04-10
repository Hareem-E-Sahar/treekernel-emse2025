public class Test {    protected static byte[] loadTileInBuffer(HttpURLConnection conn) throws IOException {
        InputStream input = conn.getInputStream();
        int bufSize = Math.max(input.available(), 32768);
        ByteArrayOutputStream bout = new ByteArrayOutputStream(bufSize);
        byte[] buffer = new byte[2048];
        boolean finished = false;
        do {
            int read = input.read(buffer);
            if (read >= 0) bout.write(buffer, 0, read); else finished = true;
        } while (!finished);
        if (bout.size() == 0) return null;
        return bout.toByteArray();
    }
}