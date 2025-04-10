public class Test {    public static void register(URL codeBase) throws Exception {
        log.info("----- URL Context Initialization Start -----");
        urlConfigMap = new LinkedHashMap();
        Properties properties = new Properties();
        try {
            URL url = new URL(codeBase + CONFIG_FILE_PATH);
            properties.load(url.openStream());
            parseConfig(properties, url);
        } catch (Exception e) {
            log.fatal(e);
            throw e;
        }
        log.info("------ URL Context Initialization End ------");
    }
}