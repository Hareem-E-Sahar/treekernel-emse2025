public class Test {    public static String GetRedirectLocation(URL url) {
        String retVal = "";
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(false);
            retVal = con.getHeaderField("Location");
            con.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retVal;
    }
}