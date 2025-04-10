public class Test {    public static void createModelZip(String filename, String tempdir, boolean overwrite) throws Exception {
        FileTools.checkOutput(filename, overwrite);
        BufferedInputStream origin = null;
        FileOutputStream dest = new FileOutputStream(filename);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        int BUFFER = 2048;
        byte data[] = new byte[BUFFER];
        File f = new File(tempdir);
        for (File fs : f.listFiles()) {
            FileInputStream fi = new FileInputStream(fs.getAbsolutePath());
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(fs.getName());
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) out.write(data, 0, count);
            out.closeEntry();
            origin.close();
        }
        out.close();
    }
}