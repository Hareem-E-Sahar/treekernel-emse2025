public class Test {    public static void copyFile(File source, File dest) throws IOException {
        FileChannel in = null, out = null;
        try {
            in = new FileInputStream(source).getChannel();
            out = new FileOutputStream(dest).getChannel();
            in.transferTo(0, in.size(), out);
        } catch (Exception e) {
            log.error(e, e);
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
        }
    }
}