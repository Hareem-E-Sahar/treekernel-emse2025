public class Test {    public byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int readed = in.read(buffer);
        while (readed > -1) {
            bos.write(buffer, 0, readed);
            readed = in.read(buffer);
        }
        in.close();
        bos.flush();
        body = bos.toByteArray();
        return body;
    }
}