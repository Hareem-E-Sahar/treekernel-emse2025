public class Test {    private String streamAsString(InputStream is) throws IOException {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        byte b[] = new byte[1024];
        int read;
        while ((read = is.read(b)) >= 0) s.write(b, 0, read);
        is.close();
        return s.toString("ASCII");
    }
}