public class Test {    public void downloadWebFile(String connectionString, String fileName) {
        try {
            if (createFolder(fileName) == false) {
                return;
            }
            URL url = new URL(connectionString);
            URLConnection urlCon = url.openConnection();
            urlCon.setRequestProperty("User-agent", "Mozilla/4.0");
            DataInputStream in = new DataInputStream(urlCon.getInputStream());
            FileOutputStream out = new FileOutputStream(fileName);
            byte[] buf = new byte[1024];
            int dem;
            while ((dem = in.read(buf)) > 0) {
                out.write(buf, 0, dem);
            }
            in.close();
            out.close();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}