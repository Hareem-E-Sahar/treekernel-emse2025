public class Test {    String query(final String query) {
        HttpGet request = null;
        try {
            request = buildRequest(query, url.getProtocol(), url.getHost(), url.getPort(), url.getPath());
            final HttpResponse response = execute(request);
            final StatusLine status = response.getStatusLine();
            final String responseBody = extractStringFrom(response);
            ensureResponse(status.getStatusCode() == HttpStatus.SC_OK, format("Server responded with %s to request %s", responseBody, request.getURI()), status.getStatusCode(), status.getReasonPhrase());
            return responseBody;
        } catch (final RuntimeException anyException) {
            rethrow(anyException, afterAborting(request));
        }
        return null;
    }
}