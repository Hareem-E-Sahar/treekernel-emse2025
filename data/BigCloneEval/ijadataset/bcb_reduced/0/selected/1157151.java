package net.sf.dsig.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHomeSettingsParser {

    private static final Logger logger = LoggerFactory.getLogger(UserHomeSettingsParser.class);

    public static Properties parse() {
        try {
            String userHome = System.getProperty("user.home");
            File dsigFolder = new File(userHome, ".dsig");
            if (!dsigFolder.exists() && !dsigFolder.mkdir()) {
                throw new IOException("Could not create .dsig folder in user home directory");
            }
            File settingsFile = new File(dsigFolder, "settings.properties");
            if (!settingsFile.exists()) {
                InputStream is = UserHomeSettingsParser.class.getResourceAsStream("/defaultSettings.properties");
                if (is != null) {
                    IOUtils.copy(is, new FileOutputStream(settingsFile));
                }
            }
            if (settingsFile.exists()) {
                Properties p = new Properties();
                FileInputStream fis = new FileInputStream(settingsFile);
                p.load(fis);
                IOUtils.closeQuietly(fis);
                return p;
            }
        } catch (IOException e) {
            logger.warn("Error while initialize settings", e);
        }
        return null;
    }
}
