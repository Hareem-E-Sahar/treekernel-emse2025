package model.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/** 
 * UTF-8 friendly ResourceBundle support 
 *  
 * Utility that allows having multi-byte characters inside java .property files. 
 * It removes the need for Sun's native2ascii application, you can simply have 
 * UTF-8 encoded editable .property files. 
 *  
 * Use:  
 * ResourceBundle bundle = Utf8ResourceBundle.getBundle("bundle_name"); 
 *  
 * @author Tomas Varaneckas <tomas.varaneckas@gmail.com> 
 */
public class UTF8Control extends Control {

    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");
        ResourceBundle bundle = null;
        InputStream stream = null;
        if (reload) {
            URL url = loader.getResource(resourceName);
            if (url != null) {
                URLConnection connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(false);
                    stream = connection.getInputStream();
                }
            }
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }
        if (stream != null) {
            try {
                bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
            } finally {
                stream.close();
            }
        }
        return bundle;
    }
}
