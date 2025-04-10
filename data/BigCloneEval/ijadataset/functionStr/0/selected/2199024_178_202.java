public class Test {    private File getRemoteFile(String reportLocation, String localPath) {
        try {
            URL reportURL = new URL(reportLocation);
            InputStream in = reportURL.openStream();
            File downloadedFile = new File(localPath);
            if (downloadedFile.exists()) {
                downloadedFile.delete();
            }
            FileOutputStream fout = new FileOutputStream(downloadedFile);
            byte buf[] = new byte[1024];
            int s = 0;
            long tl = 0;
            while ((s = in.read(buf, 0, 1024)) > 0) fout.write(buf, 0, s);
            in.close();
            fout.flush();
            fout.close();
            return downloadedFile;
        } catch (FileNotFoundException e) {
            if (reportLocation.indexOf("Subreport") == -1) log.warning("404 not found: Report cannot be found on server " + e.getMessage());
            return null;
        } catch (IOException e) {
            log.severe("I/O error when trying to download (sub)report from server " + e.getMessage());
            return null;
        }
    }
}