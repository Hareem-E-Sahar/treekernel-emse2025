public class Test {    public void appletCancelled() {
        try {
            String message = MessageFormat.format(HttpFeedback.APPLET_CANCELLED_URL, new Object[] { URLEncoder.encode(this.customerID, "UTF-8") });
            URL url = new URL(message);
            URLConnection con = url.openConnection();
            con.connect();
            con.getHeaderField(0);
        } catch (MalformedURLException e) {
            System.out.println("Applet error " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Applet error " + e.getMessage());
        }
    }
}