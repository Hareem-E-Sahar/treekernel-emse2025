public class Test {    static ResourceDef.ResourceBundle load(URL url) throws IOException {
        return load(url.openStream());
    }
}