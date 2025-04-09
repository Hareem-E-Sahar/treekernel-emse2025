public class Test {    public static ArrayList<Class<?>> packageClasses(Class<?> c) throws Exception {
        if (c.isPrimitive() || c.isArray()) throw new Exception("invalid class " + c);
        String p = c.getPackage().getName();
        URL url = c.getResource("/" + packageName(c).replace('.', '/') + "/");
        ArrayList<Class<?>> clas = new ArrayList<Class<?>>();
        if (url.getProtocol().equals("file")) {
            File path = new File(url.getPath());
            if (!path.isDirectory()) throw new Exception(path + " must be directory");
            for (String _ : path.list()) if (_.endsWith(".class")) clas.add(Class.forName(p + "." + _.substring(0, _.lastIndexOf('.'))));
        } else if (url.getProtocol().equals("jar")) {
            JarURLConnection conn = (JarURLConnection) url.openConnection();
            JarEntry path = conn.getJarEntry();
            if (!path.isDirectory()) throw new Exception(path + " must be directory");
            String name;
            for (Enumeration<JarEntry> es = conn.getJarFile().entries(); es.hasMoreElements(); ) {
                name = es.nextElement().getName();
                if (name.startsWith(path.getName()) && name.endsWith(".class")) {
                    name = name.substring(path.getName().length(), name.lastIndexOf('.'));
                    if (name.indexOf('/') < 0) clas.add(Class.forName(p + "." + name));
                }
            }
        } else throw new Exception(url.getProtocol() + " not supported yet");
        return clas;
    }
}