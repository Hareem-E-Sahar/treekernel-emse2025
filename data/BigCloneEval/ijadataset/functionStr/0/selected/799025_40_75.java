public class Test {    public static InputStream getWebSiteAsInputStream(String strUrl) {
        URL url;
        FileOutputStream out = null;
        DataInputStream dis = null;
        URLConnection urlc = null;
        try {
            url = new URL(strUrl);
            urlc = (URLConnection) url.openConnection();
            String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.8) Gecko/20051111 Firefox/1.5";
            urlc.setRequestProperty("User-Agent", userAgent);
            urlc.setRequestProperty("Connection", "close");
            urlc.setReadTimeout(DEFAULT_TIMEOUT);
            urlc.setAllowUserInteraction(false);
            urlc.setDoInput(true);
            urlc.setDoOutput(false);
            urlc.setUseCaches(false);
            urlc.connect();
            return urlc.getInputStream();
        } catch (SocketTimeoutException e) {
            logger.info("SocketTimeoutException ...");
        } catch (Exception e) {
            logger.info(strUrl + " not found.");
        } finally {
            if (out != null) try {
                out.close();
            } catch (Exception e) {
            }
            ;
            if (dis != null) try {
                dis.close();
            } catch (Exception e) {
            }
            ;
        }
        return null;
    }
}