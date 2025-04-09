package extjsdyntran.translation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.*;
import org.slf4j.Logger;
import ws4is.engine.utils.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import extjsdyntran.servlets.Constants;
import extjsdyntran.translation.store.StoreManager;

/**
 * @description Abstract base class for translation service.
 * When creating custom translation service, new class must be extended from this one. 
 */
public abstract class TranslationServiceAbstract implements ITranslationService {

    private static final long serialVersionUID = 1L;

    public static final String defaultLanguage = "ENGLISH";

    protected final Logger logger = LoggerFactory.get();

    protected Map<String, Map<String, String>> translations = new HashMap<String, Map<String, String>>();

    protected String username;

    protected String password;

    protected String path;

    public TranslationServiceAbstract(String username, String password, String path) {
        super();
        this.username = username;
        this.password = password;
        this.path = path;
        logger.info("Service is initialized!");
    }

    protected Properties getPropfromHashMap(Map<String, String> map) {
        Properties prop = new Properties();
        Set<Entry<String, String>> set = map.entrySet();
        for (Entry<String, String> entry : set) {
            prop.put(entry.getKey(), entry.getValue());
        }
        return prop;
    }

    private void compressSingleCache(String language, Properties lang, ZipOutputStream zout) throws Exception {
        ByteArrayOutputStream langout = new ByteArrayOutputStream();
        lang.store(langout, "");
        zout.putNextEntry(new ZipEntry(language + ".properties"));
        zout.write(langout.toByteArray(), 0, langout.size());
        zout.closeEntry();
    }

    protected ByteArrayOutputStream compressCache(String language) throws Exception {
        if (translations.size() == 0) return null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(bout);
        Map<String, String> lang = null;
        if (language == null) {
            Set<Entry<String, Map<String, String>>> set = translations.entrySet();
            for (Entry<String, Map<String, String>> entry : set) {
                compressSingleCache(entry.getKey(), getPropfromHashMap(entry.getValue()), zout);
            }
            zout.finish();
            return bout;
        }
        if (translations.containsKey(language)) {
            lang = translations.get(language);
            compressSingleCache(language, getPropfromHashMap(lang), zout);
            zout.finish();
            return bout;
        }
        return bout;
    }

    protected boolean deCompressCache(byte[] bin) throws Exception {
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(bin));
        ZipEntry zEntry = null;
        boolean result = false;
        while ((zEntry = zin.getNextEntry()) != null) {
            int size;
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((size = zin.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, size);
            }
            String lang = zEntry.getName();
            int whereDot = lang.lastIndexOf('.');
            String ext = lang.substring(whereDot + 1, lang.length());
            lang = lang.substring(0, whereDot);
            if (!("properties".equals(ext))) continue;
            Map<String, String> language = translations.get(lang);
            if (language == null) {
                language = new HashMap<String, String>();
                translations.put(lang, language);
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            Properties prop = new Properties();
            prop.load(bis);
            language.putAll(new HashMap(prop));
            bos.close();
            bis.close();
            result = true;
        }
        zin.close();
        return result;
    }

    protected static <T extends Enum<T>> String[] enumNameToStringArray(T[] values) {
        int i = 0;
        String[] result = new String[values.length];
        for (T value : values) {
            result[i++] = value.name();
        }
        return result;
    }

    protected static String[] enumerationToStringArray(Map<String, String> values) {
        String str[] = new String[values.size()];
        values.keySet().toArray(str);
        return str;
    }

    public boolean appendTranslationToCache(String language, Map<String, String> p) {
        Map<String, String> lang = translations.get(language);
        if (lang == null) {
            translations.put(language, p);
            return true;
        }
        lang.putAll(p);
        return true;
    }

    public boolean appendTranslationToCache(String language, String fromValue, String toValue) {
        Map<String, String> lang = translations.get(language);
        if (lang == null) {
            lang = new HashMap<String, String>();
            translations.put(language, lang);
        }
        lang.put(fromValue, toValue);
        return true;
    }

    public Map<String, Map<String, String>> getAllTranslations() {
        return translations;
    }

    public Map<String, String> getTranslations(String language) {
        return translations.get(language);
    }

    public String getTranslationsToJson(String language) {
        if (defaultLanguage.equals(language)) {
            return Constants.json_false;
        }
        try {
            Map<String, String> lang = translations.get(language);
            if (lang == null) {
                logger.error("Invalid value ({}) for parameter \"language\" when trying to get language translations.", language);
                return Constants.json_false;
            }
            JsonArray jarr = new JsonArray();
            Set<Entry<String, String>> set = lang.entrySet();
            for (Entry<String, String> e : set) {
                JsonObject jp = new JsonObject();
                jp.addProperty(defaultLanguage, e.getKey());
                jp.addProperty(language, e.getValue());
                jarr.add(jp);
            }
            Gson gs = new Gson();
            return gs.toJson(jarr);
        } catch (Exception e) {
            logger.error("Error when converting translations pairs to JSON", e);
            return Constants.json_false;
        }
    }

    public boolean saveTranslation(String toLanguage, String fromValue, String toValue) {
        if (StoreManager.saveTranslation(toLanguage, fromValue, toValue)) {
            Map<String, String> lang = getTranslations(toLanguage);
            lang.put(fromValue, toValue);
            return true;
        } else return false;
    }

    public boolean deleteTranslation(String fromLanguage, String fromValue) {
        if (StoreManager.deleteTranslation(fromLanguage, fromValue)) {
            Map<String, String> lang = getTranslations(fromLanguage);
            lang.remove(fromValue);
            return true;
        } else return false;
    }

    public boolean saveLanguages(String language) {
        if (language == null) {
            logger.error("Invalid value ({}) for parameter \"language\" when trying to save language.", language);
            return false;
        }
        return StoreManager.save(this, language);
    }

    protected void loadLanguages(String[] lg) {
        StoreManager.load(this, lg);
    }

    public void checkLanguage(String language) throws Exception {
    }
}
