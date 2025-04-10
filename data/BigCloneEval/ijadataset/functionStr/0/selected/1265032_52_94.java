public class Test {    private static JarFile retrieve(URL url, final InputStream in) throws IOException {
        JarFile jar = getFromCache(url);
        if (jar == null) {
            try {
                jar = AccessController.doPrivileged(new PrivilegedExceptionAction<JarFile>() {

                    public JarFile run() throws IOException {
                        File tmpFile = null;
                        OutputStream out = null;
                        try {
                            tmpFile = File.createTempFile("jap_cache", null);
                            tmpFile.deleteOnExit();
                            out = new FileOutputStream(tmpFile);
                            int read = 0;
                            byte[] buf = new byte[BUF_SIZE];
                            while ((read = in.read(buf)) != -1) {
                                out.write(buf, 0, read);
                            }
                            out.close();
                            out = null;
                            return new JarFile(tmpFile);
                        } catch (IOException e) {
                            if (tmpFile != null) {
                                tmpFile.delete();
                            }
                            throw e;
                        } finally {
                            if (in != null) {
                                in.close();
                            }
                            if (out != null) {
                                out.close();
                            }
                        }
                    }
                });
                putInCache(url, jar);
            } catch (PrivilegedActionException e) {
                throw new IOException(e);
            }
        }
        return jar;
    }
}