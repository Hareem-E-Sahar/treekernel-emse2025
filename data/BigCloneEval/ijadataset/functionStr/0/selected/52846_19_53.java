public class Test {    @Override
    public void launchInstead(String game, String gamesPath, String dataPath) throws IOException {
        File dataDir = LauncherUtils.getDataDir();
        if (!dataDir.exists()) {
            URLConnection uc = Launcher.class.getResource("/res//data.zip").openConnection();
            File _ = null;
            ReadableByteChannel urlRead = null;
            FileChannel fch = null;
            try {
                _ = File.createTempFile("instead_data", null);
                urlRead = Channels.newChannel(uc.getInputStream());
                fch = new FileOutputStream(_).getChannel();
                fch.transferFrom(urlRead, 0, uc.getContentLength());
                Helper.extractArchive(_, dataDir);
            } catch (IOException ioe) {
            } finally {
                if (urlRead != null) {
                    try {
                        fch.close();
                    } catch (IOException ioe) {
                    }
                }
                if (urlRead != null) {
                    try {
                        urlRead.close();
                    } catch (IOException ioe) {
                    }
                }
                if (_ != null) {
                    _.delete();
                }
            }
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
}