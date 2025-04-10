public class Test {    public static Collection providers(Class service, ClassLoader loader) {
        List classList = new ArrayList();
        List nameSet = new ArrayList();
        String name = "META-INF/services/" + service.getName();
        Enumeration services;
        try {
            services = (loader == null) ? ClassLoader.getSystemResources(name) : loader.getResources(name);
        } catch (IOException ioe) {
            System.err.println("Service: cannot load " + name);
            return classList;
        }
        while (services.hasMoreElements()) {
            URL url = (URL) services.nextElement();
            InputStream input = null;
            BufferedReader reader = null;
            try {
                input = url.openStream();
                reader = new BufferedReader(new InputStreamReader(input, "utf-8"));
                String line = reader.readLine();
                while (line != null) {
                    int ci = line.indexOf('#');
                    if (ci >= 0) line = line.substring(0, ci);
                    line = line.trim();
                    int si = line.indexOf(' ');
                    if (si >= 0) line = line.substring(0, si);
                    line = line.trim();
                    if (line.length() > 0) {
                        if (!nameSet.contains(line)) nameSet.add(line);
                    }
                    line = reader.readLine();
                }
            } catch (IOException ioe) {
                System.err.println("Service: problem with: " + url);
            } finally {
                try {
                    if (input != null) input.close();
                    if (reader != null) reader.close();
                } catch (IOException ioe2) {
                    System.err.println("Service: problem with: " + url);
                }
            }
        }
        Iterator names = nameSet.iterator();
        while (names.hasNext()) {
            String className = (String) names.next();
            try {
                classList.add(Class.forName(className, true, loader).newInstance());
            } catch (ClassNotFoundException e) {
                System.err.println("Service: cannot find class: " + className);
            } catch (InstantiationException e) {
                System.err.println("Service: cannot instantiate: " + className);
            } catch (IllegalAccessException e) {
                System.err.println("Service: illegal access to: " + className);
            } catch (NoClassDefFoundError e) {
                System.err.println("Service: " + e + " for " + className);
            } catch (Exception e) {
                System.err.println("Service: exception for: " + className + " " + e);
            }
        }
        return classList;
    }
}