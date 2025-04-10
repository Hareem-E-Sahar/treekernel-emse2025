public class Test {    public static void main(String[] args) throws Exception {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        httpclient.getCredentialsProvider().setCredentials(new AuthScope("localhost", 80), new UsernamePasswordCredentials("username", "password"));
        BasicHttpContext localcontext = new BasicHttpContext();
        BasicScheme basicAuth = new BasicScheme();
        localcontext.setAttribute("preemptive-auth", basicAuth);
        httpclient.addRequestInterceptor(new PreemptiveAuth(), 0);
        HttpHost targetHost = new HttpHost("localhost", 80, "http");
        HttpGet httpget = new HttpGet("/");
        System.out.println("executing request: " + httpget.getRequestLine());
        System.out.println("to target: " + targetHost);
        for (int i = 0; i < 3; i++) {
            HttpResponse response = httpclient.execute(targetHost, httpget, localcontext);
            HttpEntity entity = response.getEntity();
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            if (entity != null) {
                System.out.println("Response content length: " + entity.getContentLength());
                entity.consumeContent();
            }
        }
    }
}