public class Test {    public static List<JsonObject> get_contents_by_tag(String sessionid, String tag) {
        ArrayList files = new ArrayList();
        try {
            Log.d("current running function name:", "get_contents_by_tag");
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("https://mt0-app.cloud.cm/rpc/json");
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("offset", "0"));
            nameValuePairs.add(new BasicNameValuePair("tag", tag));
            nameValuePairs.add(new BasicNameValuePair("c", "Storage"));
            nameValuePairs.add(new BasicNameValuePair("count", "30"));
            nameValuePairs.add(new BasicNameValuePair("m", "get_contents_by_tag"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            httppost.setHeader("Cookie", "PHPSESSID=" + sessionid);
            HttpResponse response = httpclient.execute(httppost);
            String jsonstring = EntityUtils.toString(response.getEntity());
            Log.d("jsonStringReturned:", jsonstring);
            JsonObject result = JsonObject.parse(jsonstring);
            Float status = (Float) result.getValue("result_code");
            Log.d("responseStatusCode", status.toString());
            if (!status.equals((float) 0.0)) {
                throw new RuntimeException("Error! get files info failed");
            }
            JsonObject data = (JsonObject) result.getValue("data");
            files = (ArrayList) data.getValue("files");
            if (files.size() == 0) {
                files = new ArrayList();
                return files;
            } else {
                return files;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }
}