public class Test {    public static Bitmap download_picture_thumb(String sessionid, String key) {
        OutputStream os = null;
        String urlString = "https://mt0-s2.cloud.cm/rpc/raw?c=Pictures&m=download_picture_thumb&thumb_size=medium&key=" + key;
        Bitmap bitmap = null;
        ArrayList files = new ArrayList();
        try {
            URL url = new URL(urlString);
            Log.d("current running function name:", "download_picture_thumb");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Cookie", "PHPSESSID=" + sessionid);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}