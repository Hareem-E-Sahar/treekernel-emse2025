public class Test {    public String computeHash(String filename, String alg) throws NoSuchAlgorithmException, IOException {
        FileInputStream fis = new FileInputStream(new File(filename));
        if (alg == null || alg.trim().length() < 1) alg = "SHA1";
        MessageDigest md = MessageDigest.getInstance(alg);
        DigestOutputStream dos = new DigestOutputStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
            }
        }, md);
        int len;
        int size = 0;
        while ((len = fis.read(buf)) > 0) {
            dos.write(buf, 0, len);
            size += len;
        }
        String hash = toHexString(md.digest());
        dos.close();
        log.info("SHA1 write digest (alg:" + alg + "):" + hash);
        return hash;
    }
}