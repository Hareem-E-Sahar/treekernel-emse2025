public class Test {    public static InputStream getResourceAsStream(String resourceName, Class callingClass) {
        URL url = getResource(resourceName, callingClass);
        try {
            return (url != null) ? url.openStream() : null;
        } catch (IOException e) {
            return null;
        }
    }
}