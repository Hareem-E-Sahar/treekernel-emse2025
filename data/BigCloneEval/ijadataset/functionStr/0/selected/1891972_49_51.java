public class Test {    public static void loadSession(Model model, URL url) throws IOException {
        loadSession(model, url.openStream());
    }
}