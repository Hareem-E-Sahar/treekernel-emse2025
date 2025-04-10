public class Test {    public String share(String access_token, Map<String, String> params) throws Exception {
        String shareUrl = "http://openapi.qzone.qq.com/share/add_share";
        Map<String, String> tokens = ConnectUtils.parseTokenResult(access_token);
        String oauth_token = tokens.get("oauth_token");
        String oauth_token_secret = tokens.get("oauth_token_secret");
        String openid = tokens.get("openid");
        String oauth_timestamp = ConnectUtils.getOauthTimestamp();
        String oauth_nonce = ConnectUtils.getOauthNonce();
        List<NameValuePair> shareParameters = new ArrayList<NameValuePair>();
        shareParameters.add(new BasicNameValuePair("format", "xml"));
        shareParameters.add(new BasicNameValuePair("images", params.get("images")));
        shareParameters.add(new BasicNameValuePair("oauth_consumer_key", QQConfig.appid));
        shareParameters.add(new BasicNameValuePair("oauth_nonce", oauth_nonce));
        shareParameters.add(new BasicNameValuePair("oauth_signature_method", "HMAC-SHA1"));
        shareParameters.add(new BasicNameValuePair("oauth_timestamp", oauth_timestamp));
        shareParameters.add(new BasicNameValuePair("oauth_token", oauth_token));
        shareParameters.add(new BasicNameValuePair("oauth_version", "1.0"));
        shareParameters.add(new BasicNameValuePair("openid", openid));
        shareParameters.add(new BasicNameValuePair("title", params.get("title")));
        shareParameters.add(new BasicNameValuePair("url", params.get("url")));
        String oauth_signature = ConnectUtils.getOauthSignature("POST", shareUrl, shareParameters, oauth_token_secret);
        shareParameters.add(new BasicNameValuePair("oauth_signature", oauth_signature));
        HttpPost sharePost = new HttpPost(shareUrl);
        sharePost.setHeader("Referer", "http://openapi.qzone.qq.com");
        sharePost.setHeader("Host", "openapi.qzone.qq.com");
        sharePost.setHeader("Accept-Language", "zh-cn");
        sharePost.setHeader("Content-Type", "application/x-www-form-urlencoded");
        sharePost.setEntity(new UrlEncodedFormEntity(shareParameters, "UTF-8"));
        DefaultHttpClient httpclient = HttpClientUtils.getHttpClient();
        HttpResponse loginPostRes = httpclient.execute(sharePost);
        String shareHtml = HttpClientUtils.getHtml(loginPostRes, "UTF-8", false);
        return shareHtml;
    }
}