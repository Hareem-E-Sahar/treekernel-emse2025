public class Test {    private void extractNativeLibs() throws IOException {
        String name = "tray.dll";
        File f = new File(name);
        if (!f.exists()) {
            if (T.t) T.info("Extracting lib: " + name);
            FileOutputStream out = new FileOutputStream(f);
            InputStream in = rl.getResourceStream(name);
            byte buf[] = new byte[10 * KB];
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
            }
            out.flush();
            out.close();
            if (T.t) T.info("Done.");
        }
    }
}