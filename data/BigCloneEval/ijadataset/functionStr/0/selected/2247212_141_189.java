public class Test {    @GET
    @Path("/geocode/")
    public Model geocode(@QueryParam("q") String queryString) throws Exception {
        delayer.doDelay();
        String geocodeService = "http://open.mapquestapi.com/nominatim/v1/search";
        String uri = geocodeService + "?format=json&q=" + queryString;
        URL url = new URL(uri);
        URLConnection c = url.openConnection();
        c.setRequestProperty("User-Agent", "http://linkedgeodata.org, mailto:cstadler@informatik.uni-leipzig.de");
        InputStream ins = c.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamUtils.copy(ins, out);
        String json = out.toString();
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<JsonResponseItem>>() {
        }.getType();
        Collection<JsonResponseItem> items = gson.fromJson(json, collectionType);
        List<Resource> resources = new ArrayList<Resource>();
        for (JsonResponseItem item : items) {
            Resource resource = null;
            if (item.getOsm_type().equals("node")) {
                resource = vocab.createNIRNodeURI(item.getOsm_id());
            } else if (item.getOsm_type().equals("way")) {
                resource = vocab.createNIRWayURI(item.getOsm_id());
            } else {
                continue;
            }
            resources.add(resource);
        }
        Model result = createModel();
        String lgdService = "http://test.linkedgeodata.org/sparql";
        QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp(lgdService, Collections.singleton("http://linkedgeodata.org"));
        for (Resource resource : resources) {
            String serviceUri = "http://test.linkedgeodata.org/sparql?format=text%2Fplain&default-graph-uri=http%3A%2F%2Flinkedgeodata.org&query=DESCRIBE+<" + StringUtils.urlEncode(resource.toString()) + ">";
            URL serviceUrl = new URL(serviceUri);
            URLConnection conn = serviceUrl.openConnection();
            conn.addRequestProperty("Accept", "text/plain");
            InputStream in = null;
            try {
                in = conn.getInputStream();
                result.read(in, null, "N-TRIPLE");
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
        return result;
    }
}