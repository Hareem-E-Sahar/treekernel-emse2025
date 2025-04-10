public class Test {    public String getDataAsString(String url) throws Exception {
        try {
            String responseBody = "";
            urlc = (HttpURLConnection) new URL(url).openConnection();
            urlc.setUseCaches(false);
            urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlc.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-GB; rv:1.9.1.9) Gecko/20100414 Iceweasel/3.5.9 (like Firefox/3.5.9)");
            urlc.setRequestProperty("Accept-Encoding", "gzip");
            InputStreamReader re = new InputStreamReader(urlc.getInputStream());
            BufferedReader rd = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                responseBody += line;
                responseBody += "\n";
            }
            rd.close();
            re.close();
            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}