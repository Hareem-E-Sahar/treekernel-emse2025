public class Test {    public static InputStream getResourceAsStream(final String path, final boolean fromFileFirst) {
        return (InputStream) java.security.AccessController.doPrivileged(new java.security.PrivilegedAction() {

            public Object run() {
                try {
                    if (fromFileFirst) try {
                        return new FileInputStream(Const.userDir + Const.fileSep + path);
                    } catch (IOException e) {
                        return Utils.getResourceAsStream(path, PropertiesResourceBundle.class);
                    } else {
                        URL url = Utils.getResource(path, PropertiesResourceBundle.class);
                        if (url != null) return url.openStream(); else return new FileInputStream(Const.userDir + Const.fileSep + path);
                    }
                } catch (Exception e) {
                    return null;
                }
            }
        });
    }
}