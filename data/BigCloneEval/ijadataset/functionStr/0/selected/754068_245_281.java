public class Test {    protected File createArchive(File dir2Archive, File newArchive) {
        byte buf[] = new byte[BUF_SIZE];
        if (dir2Archive.getParentFile().canWrite()) {
            ZipOutputStream out = null;
            String prefix = dir2Archive.getName() + "/";
            ZipEntry entry;
            int cRead;
            try {
                FileOutputStream stream = new FileOutputStream(newArchive);
                out = new ZipOutputStream(stream);
                entry = new ZipEntry(prefix);
                entry.setTime(dir2Archive.lastModified());
                out.putNextEntry(entry);
                for (File cur : dir2Archive.listFiles()) {
                    if (cur.isFile() && cur.canRead()) {
                        entry = new ZipEntry(prefix + cur.getName());
                        entry.setTime(cur.lastModified());
                        entry.setSize(cur.length());
                        out.putNextEntry(entry);
                        FileInputStream in = new FileInputStream(cur);
                        while ((cRead = in.read(buf, 0, buf.length)) > 0) {
                            out.write(buf, 0, cRead);
                        }
                        in.close();
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                try {
                    if (out != null) out.close();
                } catch (Throwable t) {
                }
            }
        }
        return newArchive;
    }
}