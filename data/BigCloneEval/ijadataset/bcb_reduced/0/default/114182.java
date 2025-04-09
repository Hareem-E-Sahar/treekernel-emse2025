import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.HashMap;

class SkyLocalizer {

    SkyLocalizer(String dataDir) throws StellariumException {
        String cultureName;
        String cultureDirectory;
        String fileName = dataDir + "skycultures.fab";
        try {
            File fic = new File(fileName);
            BufferedReader reader = new BufferedReader(new FileReader(fic));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line, "\t");
                    if (st.hasMoreTokens()) {
                        cultureName = st.nextToken();
                        if (st.hasMoreTokens()) {
                            cultureDirectory = st.nextToken();
                            nameToDir.put(cultureName, cultureDirectory);
                            dirToName.put(cultureDirectory, cultureName);
                        }
                    }
                }
            } finally {
                reader.close();
            }
        } catch (IOException e) {
            throw new StellariumException("Could not create SkyLocalizer with dataDir=" + dataDir);
        }
        localeToName.put("eng", "English");
        localeToName.put("esl", "Spanish");
        localeToName.put("fra", "French");
        localeToName.put("haw", "Hawaiian");
        for (Object o : localeToName.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            nameToLocale.put((String) entry.getValue(), (String) entry.getKey());
        }
    }

    /**
     * returns newline delimited list of human readable culture names
     */
    String getSkyCultureList() {
        StringBuffer cultures = new StringBuffer();
        for (Object o : nameToDir.values()) {
            cultures.append(o).append('\n');
        }
        return cultures.toString();
    }

    String convertDirectoryToSkyCulture(String _directory) {
        return dirToName.get(_directory);
    }

    String convertSkyCultureToDirectory(String _name) {
        return nameToDir.get(_name);
    }

    /**
     * returns newline delimited list of human readable culture names
     */
    String get_sky_locale_list() {
        String locales = "";
        for (String s : nameToLocale.values()) {
            locales += s + "\n";
        }
        return locales;
    }

    /**
     * locale is used by code, locale name is human readable
     * e.g. fra = French
     */
    String convert_locale_to_name(String _locale) {
        return localeToName.get(_locale);
    }

    String convert_name_to_locale(String _name) {
        return nameToLocale.get(_name);
    }

    private Map<String, String> nameToDir = new HashMap<String, String>();

    private Map<String, String> dirToName = new HashMap<String, String>();

    private Map<String, String> nameToLocale = new HashMap<String, String>();

    private Map<String, String> localeToName = new HashMap<String, String>();
}
