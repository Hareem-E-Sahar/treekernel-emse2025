public class Test {    public static byte[] download(String url, Map<String, String> queryParams) {
        try {
            String queryParam = compQueryParams(queryParams);
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(queryParam != null ? queryParam.length() : 0));
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            if (queryParams != null) {
                OutputStream os = conn.getOutputStream();
                os.write(queryParam.getBytes("utf-8"));
                os.flush();
                os.close();
            }
            int resStatus = conn.getResponseCode();
            if (resStatus == HttpURLConnection.HTTP_OK) {
                byte[] result = getByte(conn.getInputStream());
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}