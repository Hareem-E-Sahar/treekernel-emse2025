public class Test {    public SearchResponseType spmlSearchRequest(SearchRequestType request) {
        try {
            return (SearchResponseType) mediator.sendMessage(request, doMakeDestination(request), psp.getChannel());
        } catch (IdentityMediationException e) {
            throw new RuntimeException(e);
        }
    }
}