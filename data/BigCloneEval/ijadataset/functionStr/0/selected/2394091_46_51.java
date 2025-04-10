public class Test {    public void testGet() throws Exception {
        HttpGet request = new HttpGet(baseUri + "/test");
        HttpResponse response = client.execute(request);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("test", TestUtil.getResponseAsString(response));
    }
}