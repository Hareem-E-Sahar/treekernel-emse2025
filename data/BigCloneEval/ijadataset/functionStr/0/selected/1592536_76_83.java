public class Test {    public static InputStream getResourceAsStream(String resName, Class<?> clazz) {
        URL url = getResource(resName, clazz);
        try {
            return (url != null) ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }
}