public class Test {    public void downloadImageFromUrl(IProgressMonitor monitor, String address, String filePath, String fileName) {
        if (address.isEmpty() || filePath.isEmpty() || fileName.isEmpty()) return;
        URLConnection conn = null;
        URL url;
        try {
            File outputFile = new File(filePath + fileName);
            if (outputFile.exists()) return;
            url = new URL(address);
            conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setConnectTimeout(4000);
            File directory = new File(filePath);
            if (!directory.exists()) directory.mkdirs();
            InterruptConnection interruptConnection = new InterruptConnection(conn);
            new Thread(interruptConnection).start();
            while (!monitor.isCanceled() && !interruptConnection.isFinished() && !interruptConnection.isError()) ;
            if (!interruptConnection.isFinished) {
                ((HttpURLConnection) conn).disconnect();
                return;
            }
            InputStream content = (InputStream) interruptConnection.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(content));
            BufferedInputStream bis = new BufferedInputStream(content);
            FileOutputStream fos = new FileOutputStream(outputFile);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int current = 0;
            while ((current = bis.read()) != -1) {
                byteArrayOutputStream.write((byte) current);
            }
            fos.write(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.close();
            fos.close();
            in.close();
        } catch (MalformedURLException e) {
            Logger.logError(e, _("Malformated URL:") + " " + address);
        } catch (IOException e) {
            Logger.logError(e, _("Error downloading picture from:") + " " + address);
        }
    }
}