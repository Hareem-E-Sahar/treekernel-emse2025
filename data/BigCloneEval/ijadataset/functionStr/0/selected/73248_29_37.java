public class Test {    public void displayItems() throws IOException {
        URL url = new URL(SNIPPETS_FEED + "?bq=" + URLEncoder.encode(QUERY, "UTF-8"));
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = httpConnection.getInputStream();
        int ch;
        while ((ch = inputStream.read()) > 0) {
            System.out.print((char) ch);
        }
    }
}