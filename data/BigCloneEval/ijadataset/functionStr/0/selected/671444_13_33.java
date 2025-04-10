public class Test {    protected URLConnection openConnection(URL url) throws java.io.IOException {
        String cln = url.getHost();
        String resrce = url.getFile().substring(1);
        URL realURL;
        if (cln != null && cln.length() != 0) {
            Class clz;
            ClassLoader cl = getContextClassLoader();
            try {
                clz = Class.forName(cln, true, cl);
            } catch (ClassNotFoundException ex) {
                throw new java.net.MalformedURLException("Class " + cln + " cannot be found (" + ex + ")");
            }
            realURL = cl.getResource(resrce);
            if (realURL == null) throw new java.io.FileNotFoundException("Class resource " + resrce + " of class " + cln + " cannot be found");
        } else {
            ClassLoader cl = getContextClassLoader();
            realURL = cl.getResource(resrce);
            if (realURL == null) throw new java.io.FileNotFoundException("System resource " + resrce + " cannot be found");
        }
        return realURL.openConnection();
    }
}