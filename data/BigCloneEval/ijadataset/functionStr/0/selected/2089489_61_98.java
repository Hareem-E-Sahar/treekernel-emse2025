public class Test {    private SystemProperties() {
        Properties p = new Properties();
        ClassLoader classLoader = getClass().getClassLoader();
        try {
            URL url = classLoader.getResource("system.properties");
            if (url != null) {
                InputStream is = url.openStream();
                p.load(is);
                is.close();
                Logger.info(this, "Loading " + url);
            }
        } catch (Exception e) {
            Logger.error(this, e.getMessage(), e);
        }
        try {
            URL url = classLoader.getResource("system-ext.properties");
            if (url != null) {
                InputStream is = url.openStream();
                p.load(is);
                is.close();
                Logger.info(this, "Loading " + url);
            }
        } catch (Exception e) {
            Logger.error(this, e.getMessage(), e);
        }
        boolean systemPropertiesLoad = GetterUtil.get(System.getProperty(SYSTEM_PROPERTIES_LOAD), true);
        boolean systemPropertiesFinal = GetterUtil.get(System.getProperty(SYSTEM_PROPERTIES_FINAL), true);
        if (systemPropertiesLoad) {
            Enumeration enu = p.propertyNames();
            while (enu.hasMoreElements()) {
                String key = (String) enu.nextElement();
                if (systemPropertiesFinal || Validator.isNull(System.getProperty(key))) {
                    System.setProperty(key, (String) p.get(key));
                }
            }
        }
        PropertiesUtil.fromProperties(p, _props);
    }
}