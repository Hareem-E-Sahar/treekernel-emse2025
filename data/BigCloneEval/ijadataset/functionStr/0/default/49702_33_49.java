public class Test {    private void makeConn(String filename1, String filename2) {
        String basename = "http://www.mm52.com/";
        String urlname = basename + "otheridols/" + filename1 + "/" + filename1 + filename2 + ".jpg";
        URL url = null;
        try {
            url = new URL(urlname);
        } catch (MalformedURLException e) {
            System.err.println("URL Format Error!");
            System.exit(1);
        }
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            System.err.println("Error IO");
            System.exit(2);
        }
    }
}