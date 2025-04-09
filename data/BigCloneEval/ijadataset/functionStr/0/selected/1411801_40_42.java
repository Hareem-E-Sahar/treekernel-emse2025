public class Test {    public HttpResponse execute(HttpUriRequest req) throws IOException, ClientProtocolException {
        return execute(req, (HttpContext) null);
    }
}