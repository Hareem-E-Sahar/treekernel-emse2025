public class Test {    @Test
    public void testApacheHttpClient4ExecutorSharedHttpClientClose() throws Throwable {
        HttpClient httpClient = new DefaultHttpClient();
        ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(httpClient);
        ClientRequest request = new ClientRequest(generateURL("/test"), executor);
        ClientResponse<?> response = request.post();
        Assert.assertEquals(204, response.getStatus());
        executor.close();
        Assert.assertEquals(httpClient, executor.getHttpClient());
        HttpPost post = new HttpPost(generateURL("/test"));
        HttpResponse httpResponse = httpClient.execute(post);
        Assert.assertEquals(204, httpResponse.getStatusLine().getStatusCode());
        httpClient.getConnectionManager().shutdown();
    }
}