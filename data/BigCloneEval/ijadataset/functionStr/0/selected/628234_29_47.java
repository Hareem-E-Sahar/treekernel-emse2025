public class Test {    private static final void compressFile(final ZipOutputStream out, final String parentFolder, final File file) throws IOException {
        final String zipName = new StringBuilder(parentFolder).append(file.getName()).append(file.isDirectory() ? '/' : "").toString();
        final ZipEntry entry = new ZipEntry(zipName);
        entry.setSize(file.length());
        entry.setTime(file.lastModified());
        out.putNextEntry(entry);
        if (file.isDirectory()) {
            for (final File f : file.listFiles()) compressFile(out, zipName.toString(), f);
            return;
        }
        final InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            final byte[] buf = new byte[8192];
            int bytesRead;
            while (-1 != (bytesRead = in.read(buf))) out.write(buf, 0, bytesRead);
        } finally {
            in.close();
        }
    }
}