public class Test {    public void loginUploadBox() throws Exception {
        loginsuccessful = false;
        HttpParams params = new BasicHttpParams();
        params.setParameter("http.useragent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB; rv:1.9.2) Gecko/20100115 Firefox/3.6");
        DefaultHttpClient httpclient = new DefaultHttpClient(params);
        NULogger.getLogger().info("Trying to log in to uploadbox.com");
        HttpPost httppost = new HttpPost("http://www.uploadbox.com/en");
        httppost.setHeader("Cookie", sidcookie);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("login", getUsername()));
        formparams.add(new BasicNameValuePair("passwd", getPassword()));
        formparams.add(new BasicNameValuePair("ac", "auth"));
        formparams.add(new BasicNameValuePair("back", ""));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        httppost.setEntity(entity);
        HttpResponse httpresponse = httpclient.execute(httppost);
        if (httpresponse.getStatusLine().toString().contains("302")) {
            loginsuccessful = true;
            NULogger.getLogger().info("UploadBox Login Success");
            username = getUsername();
            password = getPassword();
        } else {
            loginsuccessful = false;
            NULogger.getLogger().info("UploadBox Login failed");
            username = "";
            password = "";
            JOptionPane.showMessageDialog(NeembuuUploader.getInstance(), "<html><b>" + HOSTNAME + "</b> " + TranslationProvider.get("neembuuuploader.accounts.loginerror") + "</html>", HOSTNAME, JOptionPane.WARNING_MESSAGE);
            AccountsManager.getInstance().setVisible(true);
        }
    }
}