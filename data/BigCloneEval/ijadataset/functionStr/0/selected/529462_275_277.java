public class Test {    public Image createSystemImage(URL url) throws IOException {
        return new SwtImmutableImage(SwtDeviceComponent.createImage(url.openStream()));
    }
}