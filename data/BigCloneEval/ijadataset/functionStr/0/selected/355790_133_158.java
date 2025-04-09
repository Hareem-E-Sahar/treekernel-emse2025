public class Test {    private static Manifest find(String name) throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> e = loader.getResources("org/fpse/forum/Forum");
        while (e.hasMoreElements()) {
            URL url = e.nextElement();
            InputStream in = null;
            try {
                in = url.openStream();
                Manifest manifest = new Manifest(in);
                Attributes attributes = manifest.getMainAttributes();
                String value = attributes.getValue("Id");
                if (name.equals(value)) {
                    LOG.debug("Found the manifest file: " + url);
                    return manifest;
                }
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException _) {
                    }
                }
            }
        }
        return null;
    }
}