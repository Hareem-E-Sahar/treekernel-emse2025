public class Test {        private String getApplicationData() {
            return getApplicationServerRoot() + callParameters.getChannelApplet().getChannelAppletLocation().getStringRepresentation();
        }
}