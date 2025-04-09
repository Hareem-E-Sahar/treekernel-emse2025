package extjsdyntran.translation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.fileupload.FileItem;
import org.slf4j.Logger;
import ws4is.engine.utils.LoggerFactory;

/**
 * @description Main class for translation services.
 * It is used to register translation services. When translation is in progress,
 * if there is no supported language for current service, next service will be used for translation   
 */
public class TranslationFactory {

    static final Logger logger = LoggerFactory.get();

    private static ArrayList<TranslationServiceAbstract> services = new ArrayList<TranslationServiceAbstract>();

    public static ArrayList<TranslationServiceAbstract> getServices() {
        return services;
    }

    public static void appendService(String service, String username, String pasword, String path) {
        if (service == null) {
            logger.warn("Trying to initialize null service");
            return;
        }
        try {
            TranslationServiceAbstract transervice = initService(service, username, pasword, path);
            services.add(transervice);
        } catch (Exception e) {
            logger.error("Error initializing service", e);
        }
    }

    private static TranslationServiceAbstract initService(String service, String username, String password, String path) throws Exception {
        Class<?> cl = Class.forName(service);
        Constructor<?> ct = cl.getConstructor(String.class, String.class, String.class);
        Object retObj = ct.newInstance(username, password, path);
        return (TranslationServiceAbstract) retObj;
    }

    public static TranslationServiceAbstract getRealService(String language) {
        TranslationServiceAbstract service = getServiceByType(language, null);
        if (service == null) return null;
        if (service.getServiceType() != service.isLanguageSupported(language)) {
            service = TranslationFactory.getServiceByType(language, ServiceTypes.NORMAL);
        }
        return service;
    }

    public static TranslationServiceAbstract getService(String language) {
        TranslationServiceAbstract service = getServiceByType(language, null);
        return service;
    }

    public static TranslationServiceAbstract getServiceByType(String language, ServiceTypes type) {
        for (TranslationServiceAbstract service : services) {
            if (type == null) {
                if (service.isLanguageSupported(language) != null) {
                    return service;
                }
                ;
                continue;
            }
            if (service.getServiceType() != type) continue;
            if (service.isLanguageSupported(language) != null) {
                return service;
            }
            ;
        }
        return null;
    }

    public static TranslationServiceAbstract getEditableService(String language, ServiceTypes type) {
        for (TranslationServiceAbstract service : services) {
            if (!service.isNewLanguageSupported()) continue;
            if (type == null) return service;
            if (service.getServiceType() == type) return service;
        }
        return null;
    }

    public static boolean saveTranslation(String toLanguage, String fromValue, String toValue) {
        TranslationServiceAbstract service = getService(toLanguage);
        return service.saveTranslation(toLanguage, fromValue, toValue);
    }

    public static boolean deleteTranslation(String fromLanguage, String fromValue) {
        TranslationServiceAbstract service = getService(fromLanguage);
        return service.deleteTranslation(fromLanguage, fromValue);
    }

    public static boolean saveLanguages(String language) {
        if ("*".equals(language)) {
            logger.info("Saving all language translations to storage!");
            for (TranslationServiceAbstract service : services) {
                service.saveLanguages(language);
            }
            return true;
        } else {
            TranslationServiceAbstract service = getRealService(language);
            if (service == null) return false;
            logger.info("Saving translations for language {} to storage!", language);
            return service.saveLanguages(language);
        }
    }

    public static int size() {
        return services.size();
    }

    private static Collection<String[]> getLanguagesAsJsonArray(Map<String, String> langs, Collection<String> langList) {
        Collection<String[]> result = new ArrayList<String[]>();
        for (String key : langList) {
            String[] rec = { (String) langs.get(key), key };
            result.add(rec);
        }
        return result;
    }

    public static Collection<String[]> getLanguages(ArrayList<ServiceTypes> excludes) {
        Map<String, String> langs = new HashMap<String, String>();
        for (TranslationServiceAbstract service : services) {
            if (excludes != null) if (excludes.contains(service.getServiceType())) continue;
            langs.putAll(service.getListOfLanguages());
        }
        List<String> langList = new ArrayList(langs.keySet());
        Collections.sort(langList);
        return getLanguagesAsJsonArray(langs, langList);
    }

    public static void reloadLanguages() {
        logger.info("Reloading all language translations from storage...");
        for (TranslationServiceAbstract service : services) {
            service.loadLanguages();
        }
    }

    public static Map<String, Boolean> test() {
        Map<String, Boolean> response = new HashMap<String, Boolean>();
        for (TranslationServiceAbstract service : services) {
            try {
                boolean test = service.test();
                logger.info("Translation service availability is {} !", test ? "sucessful" : "unsucessful");
                response.put(service.getClass().getCanonicalName(), new Boolean(test));
            } catch (Exception e) {
                response.put(service.getClass().getCanonicalName(), new Boolean(false));
                logger.error("Error testing availability for service {}!", service.getClass().getCanonicalName());
                logger.error("Service test availability error:", e);
            }
        }
        return response;
    }

    private static void compressSingleService(ZipOutputStream zout, TranslationServiceAbstract service, String language) throws Exception {
        ByteArrayOutputStream langout = service.compressCache(language);
        if (langout == null) return;
        zout.putNextEntry(new ZipEntry(service.getClass().getCanonicalName()));
        zout.write(langout.toByteArray(), 0, langout.size());
        zout.closeEntry();
    }

    public static ByteArrayOutputStream compressCache(String language) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zout = new ZipOutputStream(bout);
        if (language != null) {
            TranslationServiceAbstract service = getRealService(language);
            compressSingleService(zout, service, language);
        } else {
            for (TranslationServiceAbstract service : services) {
                compressSingleService(zout, service, null);
            }
        }
        zout.finish();
        return bout;
    }

    private static TranslationServiceAbstract getServiceByClassName(String className) {
        for (TranslationServiceAbstract service : services) {
            if (className.equals(service.getClass().getCanonicalName())) return service;
        }
        return null;
    }

    public static boolean deCompressCache(FileItem item) throws IOException {
        ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(item.get()));
        ZipEntry zEntry = null;
        while ((zEntry = zin.getNextEntry()) != null) {
            TranslationServiceAbstract service = getServiceByClassName(zEntry.getName());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int size;
            byte[] buffer = new byte[1024];
            while ((size = zin.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, size);
            }
            try {
                service.deCompressCache(bos.toByteArray());
            } catch (Exception e) {
                logger.error("Error decompressing imported translations.", e);
                return false;
            }
        }
        return true;
    }
}
