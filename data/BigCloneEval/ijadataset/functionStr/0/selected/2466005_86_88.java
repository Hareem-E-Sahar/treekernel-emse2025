public class Test {    protected HttpResponse executeSocialRequest(HttpRequest request) throws GadgetException {
        return requestPipeline.execute(request);
    }
}