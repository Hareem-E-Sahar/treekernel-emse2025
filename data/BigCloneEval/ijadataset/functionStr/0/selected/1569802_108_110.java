public class Test {    public void loadJar(final URL url) throws IOException {
        loadJar(new JarInputStream(url.openStream()));
    }
}