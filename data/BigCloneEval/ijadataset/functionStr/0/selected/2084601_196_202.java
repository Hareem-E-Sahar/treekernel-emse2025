public class Test {    public ResponseType spmlBulkModifyRequest(BulkModifyRequestType request) {
        try {
            return (ResponseType) mediator.sendMessage(request, doMakeDestination(request), psp.getChannel());
        } catch (IdentityMediationException e) {
            throw new RuntimeException(e);
        }
    }
}