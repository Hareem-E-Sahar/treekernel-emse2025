public class Test {    public static synchronized File getFileViaHTTPRequest(URL url) throws Exception {
        File file = File.createTempFile("state", ".zip");
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file.getAbsolutePath()));
        URLConnection con = url.openConnection();
        InputStream in = con.getInputStream();
        byte[] buffer = new byte[1024];
        int numRead;
        while ((numRead = in.read(buffer)) != -1) {
            bos.write(buffer, 0, numRead);
            bos.flush();
        }
        return file;
    }
}