public class Test {    public void testCookieVersionSupportHeader2() throws Exception {
        this.localServer.register("*", new CookieVer1Service());
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext context = new BasicHttpContext();
        context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpGet httpget = new HttpGet("/test/");
        HttpResponse response1 = client.execute(getServerHttp(), httpget, context);
        HttpEntity e1 = response1.getEntity();
        if (e1 != null) {
            e1.consumeContent();
        }
        List<Cookie> cookies = cookieStore.getCookies();
        assertNotNull(cookies);
        assertEquals(2, cookies.size());
        HttpResponse response2 = client.execute(getServerHttp(), httpget, context);
        HttpEntity e2 = response2.getEntity();
        if (e2 != null) {
            e2.consumeContent();
        }
        HttpRequest reqWrapper = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
        Header cookiesupport = reqWrapper.getFirstHeader(SM.COOKIE2);
        assertNotNull(cookiesupport);
        assertEquals("$Version=1", cookiesupport.getValue());
    }
}