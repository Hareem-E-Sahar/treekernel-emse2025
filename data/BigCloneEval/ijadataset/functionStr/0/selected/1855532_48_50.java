public class Test {    public static Translator createTranslator(URL url) throws IOException {
        return createTranslator(url.openConnection());
    }
}