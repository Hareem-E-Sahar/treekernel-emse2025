public class Test {    public static void unjar(File jarFile, File targetDir) throws IOException {
        if (targetDir.exists()) {
            targetDir.delete();
        }
        targetDir.mkdirs();
        String targetPath = targetDir.getAbsolutePath() + File.separatorChar;
        byte[] buffer = new byte[BUFFER_LENGTH * BUFFER_LENGTH];
        JarFile input = new JarFile(jarFile, false, ZipFile.OPEN_READ);
        Enumeration<JarEntry> enumeration = input.entries();
        for (; enumeration.hasMoreElements(); ) {
            JarEntry entry = (JarEntry) enumeration.nextElement();
            if (!entry.isDirectory()) {
                if (entry.getName().indexOf("package cache") == -1) {
                    String path = targetPath + entry.getName();
                    File file = new File(path);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    FileOutputStream out = new FileOutputStream(file);
                    InputStream in = input.getInputStream(entry);
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    in.close();
                    out.close();
                }
            }
        }
    }
}