package org.gudy.azureus2.core3.config.impl;

import org.gudy.azureus2.core3.config.*;
import org.gudy.azureus2.core3.global.GlobalManager;
import com.aelitis.azureus.core.AzureusCore;
import com.aelitis.azureus.core.speedmanager.SpeedManager;

/**
 * Provides validation for transfer speed settings
 * @version 1.0
 * @since 1.4
 * @author James Yeh
 */
public final class TransferSpeedValidator {

    public static final String AUTO_UPLOAD_ENABLED_CONFIGKEY = "Auto Upload Speed Enabled";

    public static final String AUTO_UPLOAD_SEEDING_ENABLED_CONFIGKEY = "Auto Upload Speed Seeding Enabled";

    public static final String UPLOAD_CONFIGKEY = "Max Upload Speed KBs";

    public static final String UPLOAD_SEEDING_CONFIGKEY = "Max Upload Speed Seeding KBs";

    public static final String DOWNLOAD_CONFIGKEY = "Max Download Speed KBs";

    public static final String UPLOAD_SEEDING_ENABLED_CONFIGKEY = "enable.seedingonly.upload.rate";

    public static final String[] CONFIG_PARAMS = { AUTO_UPLOAD_ENABLED_CONFIGKEY, AUTO_UPLOAD_SEEDING_ENABLED_CONFIGKEY, UPLOAD_CONFIGKEY, UPLOAD_SEEDING_CONFIGKEY, DOWNLOAD_CONFIGKEY, UPLOAD_SEEDING_ENABLED_CONFIGKEY };

    private final String configKey;

    private final Number configValue;

    private static boolean auto_upload_enabled;

    private static boolean auto_upload_seeding_enabled;

    private static boolean seeding_upload_enabled;

    static {
        COConfigurationManager.addAndFireParameterListeners(new String[] { UPLOAD_SEEDING_ENABLED_CONFIGKEY, AUTO_UPLOAD_ENABLED_CONFIGKEY, AUTO_UPLOAD_SEEDING_ENABLED_CONFIGKEY }, new PriorityParameterListener() {

            public void parameterChanged(String parameterName) {
                if (parameterName == null || parameterName.equals(UPLOAD_SEEDING_ENABLED_CONFIGKEY)) {
                    seeding_upload_enabled = COConfigurationManager.getBooleanParameter(UPLOAD_SEEDING_ENABLED_CONFIGKEY);
                }
                if (parameterName == null || parameterName.equals(AUTO_UPLOAD_ENABLED_CONFIGKEY)) {
                    auto_upload_enabled = COConfigurationManager.getBooleanParameter(AUTO_UPLOAD_ENABLED_CONFIGKEY);
                }
                if (parameterName == null || parameterName.equals(AUTO_UPLOAD_SEEDING_ENABLED_CONFIGKEY)) {
                    auto_upload_seeding_enabled = COConfigurationManager.getBooleanParameter(AUTO_UPLOAD_SEEDING_ENABLED_CONFIGKEY);
                }
            }
        });
    }

    /**
     * Creates a TransferSpeedValidator with the given configuration key and value
     * @param configKey Configuration key; must be "Max Upload Speed KBs" or "Max Download Speed KBs"
     * @param value Configuration value to be validated
     */
    public TransferSpeedValidator(final String configKey, final Number value) {
        this.configKey = configKey;
        configValue = value;
    }

    /**
     * Gets the transformed value as an Integer
     */
    private static Object validate(final String configKey, final Number value) {
        int newValue = value.intValue();
        if (newValue < 0) {
            newValue = 0;
        }
        if (configKey == UPLOAD_CONFIGKEY) {
            final int downValue = COConfigurationManager.getIntParameter(DOWNLOAD_CONFIGKEY);
            if (newValue != 0 && newValue < COConfigurationManager.CONFIG_DEFAULT_MIN_MAX_UPLOAD_SPEED && (downValue == 0 || downValue > newValue * 2)) {
                newValue = (downValue + 1) / 2;
            }
        } else if (configKey == DOWNLOAD_CONFIGKEY) {
            final int upValue = COConfigurationManager.getIntParameter(UPLOAD_CONFIGKEY);
            if (upValue != 0 && upValue < COConfigurationManager.CONFIG_DEFAULT_MIN_MAX_UPLOAD_SPEED) {
                if (newValue > upValue * 2) {
                    newValue = upValue * 2;
                } else if (newValue == 0) {
                    newValue = upValue * 2;
                }
            }
        } else if (configKey == UPLOAD_SEEDING_CONFIGKEY) {
        } else {
            throw new IllegalArgumentException("Invalid Configuation Key; use key for max upload and max download");
        }
        return new Integer(newValue);
    }

    /**
     * Validates the given configuration key/value pair and returns the validated value
     * @return Modified configuration value that conforms to validation as an Integer
     */
    public Object getValue() {
        return validate(configKey, configValue);
    }

    public static String getActiveUploadParameter(GlobalManager gm) {
        if (seeding_upload_enabled && gm.isSeedingOnly()) {
            return (TransferSpeedValidator.UPLOAD_SEEDING_CONFIGKEY);
        } else {
            return (TransferSpeedValidator.UPLOAD_CONFIGKEY);
        }
    }

    public static String getDownloadParameter() {
        return (DOWNLOAD_CONFIGKEY);
    }

    public static int getGlobalDownloadRateLimitBytesPerSecond() {
        return (COConfigurationManager.getIntParameter(getDownloadParameter()) * 1024);
    }

    public static void setGlobalDownloadRateLimitBytesPerSecond(int bytes_per_second) {
        COConfigurationManager.setParameter(getDownloadParameter(), (bytes_per_second + 1023) / 1024);
    }

    public static boolean isAutoUploadAvailable(AzureusCore core) {
        SpeedManager speedManager = core.getSpeedManager();
        return speedManager == null ? false : speedManager.isAvailable();
    }

    public static boolean isAutoSpeedActive(GlobalManager gm) {
        if (auto_upload_enabled) {
            return auto_upload_enabled;
        }
        if (gm.isSeedingOnly()) {
            return auto_upload_seeding_enabled;
        } else {
            return auto_upload_enabled;
        }
    }

    public static String getActiveAutoUploadParameter(GlobalManager gm) {
        if (!auto_upload_enabled && gm.isSeedingOnly()) {
            return AUTO_UPLOAD_SEEDING_ENABLED_CONFIGKEY;
        }
        return AUTO_UPLOAD_ENABLED_CONFIGKEY;
    }
}
