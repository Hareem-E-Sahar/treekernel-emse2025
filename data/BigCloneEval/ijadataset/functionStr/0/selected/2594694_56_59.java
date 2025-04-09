public class Test {    public FederationChannel getChannel() {
        if (defaultFederationService == null) return null;
        return defaultFederationService.getChannel();
    }
}