package model.configuration;

import controller.systemtray.*;
import java.awt.*;
import java.awt.TrayIcon.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.mail.internet.*;
import model.encryption.*;
import model.ipmonitor.*;
import model.ipmonitor.exceptions.*;
import model.notification.*;
import model.notification.configuration.*;

public class PropertiesManager {

    private IPMonitor ipMonitor;

    public PropertiesManager(IPMonitor ipMonitor) {
        this.ipMonitor = ipMonitor;
    }

    public void saveToFile() {
        Properties properties = new Properties();
        saveMainViewLocation(properties);
        saveMainViewSize(properties);
        saveInterval(properties);
        saveAutostart(properties);
        saveURL(properties);
        saveRegularExpression(properties);
        saveAudioNotification(properties);
        saveMailNotification(properties);
        saveVisualNotification(properties);
        saveCommandNotification(properties);
        saveAudioNotificationConfigurationPath(properties);
        saveMailNotificationConfigurationServer(properties);
        saveMallNotificationConfigurationPort(properties);
        saveMailNotificationConfigurationUser(properties);
        saveMailNotificationConfigurationPassword(properties);
        saveMailNotificationConfigurationAuthenticationRequired(properties);
        saveMailNotificationConfigurationUseSSL(properties);
        saveMailNotificationConfigurationFromName(properties);
        saveMailNotificationConfigurationFromAddress(properties);
        saveMailNotificationConfigurationToAddresses(properties);
        saveMailNotificationConfigurationSubject(properties);
        saveMailNotificationConfigurationText(properties);
        saveMailNotificationConfigurationUseHTML(properties);
        saveVisualNotificationConfigurationTitle(properties);
        saveVisualNotificationConfigurationText(properties);
        saveVisualNotificationConfigurationIcon(properties);
        saveCommandNotificationConfigurationPath(properties);
        saveLookAndFeelClassName(properties);
        try {
            properties.store(new FileOutputStream(new File(ConfigurationManager.getInstance().getConfigurationFile())), null);
        } catch (Exception e) {
        }
    }

    public void loadFromFile() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(new File(ConfigurationManager.getInstance().getConfigurationFile())));
        } catch (Exception e) {
        }
        loadMainViewLocation(properties);
        loadMainViewSize(properties);
        loadInterval(properties);
        loadAutostart(properties);
        loadURL(properties);
        loadRegularExpression(properties);
        loadAudioNotification(properties);
        loadMailNotification(properties);
        loadVisualNotification(properties);
        loadCommandNotification(properties);
        loadAudioNotificationConfigurationPath(properties);
        loadMailNotificationConfigurationServer(properties);
        loadMallNotificationConfigurationPort(properties);
        loadMailNotificationConfigurationUser(properties);
        loadMailNotificationConfigurationPassword(properties);
        loadMailNotificationConfigurationAuthenticationRequired(properties);
        loadMailNotificationConfigurationUseSSL(properties);
        loadMailNotificationConfigurationFromName(properties);
        loadMailNotificationConfigurationFromAddress(properties);
        loadMailNotificationConfigurationToAddresses(properties);
        loadMailNotificationConfigurationSubject(properties);
        loadMailNotificationConfigurationText(properties);
        loadMailNotificationConfigurationUseHTML(properties);
        loadVisualNotificationConfigurationTitle(properties);
        loadVisualNotificationConfigurationText(properties);
        loadVisualNotificationConfigurationIcon(properties);
        loadCommandNotificationConfigurationPath(properties);
        loadLookAndFeelClassName(properties);
    }

    private void loadMainViewLocation(Properties properties) {
        try {
            int x = Integer.valueOf(properties.getProperty(IPMonitorProperties.MAINVIEW_LOCATION_X));
            int y = Integer.valueOf(properties.getProperty(IPMonitorProperties.MAINVIEW_LOCATION_Y));
            ConfigurationManager.getInstance().setMainViewLocation(new Point(x, y));
        } catch (Exception e) {
        }
    }

    private void loadMainViewSize(Properties properties) {
        try {
            int x = Integer.valueOf(properties.getProperty(IPMonitorProperties.MAINVIEW_SIZE_X));
            int y = Integer.valueOf(properties.getProperty(IPMonitorProperties.MAINVIEW_SIZE_Y));
            ConfigurationManager.getInstance().setMainViewSize(new Dimension(x, y));
        } catch (Exception e) {
        }
    }

    private void saveMainViewLocation(Properties properties) {
        properties.setProperty(IPMonitorProperties.MAINVIEW_LOCATION_X, String.valueOf(ConfigurationManager.getInstance().getMainViewLocation().x));
        properties.setProperty(IPMonitorProperties.MAINVIEW_LOCATION_Y, String.valueOf(ConfigurationManager.getInstance().getMainViewLocation().y));
    }

    private void saveMainViewSize(Properties properties) {
        properties.setProperty(IPMonitorProperties.MAINVIEW_SIZE_X, String.valueOf(ConfigurationManager.getInstance().getMainViewSize().width));
        properties.setProperty(IPMonitorProperties.MAINVIEW_SIZE_Y, String.valueOf(ConfigurationManager.getInstance().getMainViewSize().height));
    }

    private void loadInterval(Properties properties) {
        int interval;
        try {
            interval = Integer.valueOf(properties.getProperty(IPMonitorProperties.OPTIONS_MONITOR_INTERVAL, String.valueOf(IPMonitorProperties.OPTIONS_MONITOR_INTERVAL_VALUE)));
        } catch (NumberFormatException e) {
            interval = IPMonitorProperties.OPTIONS_MONITOR_INTERVAL_VALUE;
        }
        try {
            ipMonitor.setInterval(interval);
        } catch (InvalidIntervalException e) {
            try {
                ipMonitor.setInterval(IPMonitorProperties.OPTIONS_MONITOR_INTERVAL_VALUE);
            } catch (InvalidIntervalException e1) {
            }
        }
    }

    private void saveInterval(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_MONITOR_INTERVAL, String.valueOf(ipMonitor.getInterval()));
    }

    private void loadAutostart(Properties properties) {
        ConfigurationManager.getInstance().setAutostart(Boolean.valueOf(properties.getProperty(IPMonitorProperties.OPTIONS_MONITOR_AUTOSTART, String.valueOf(IPMonitorProperties.OPTIONS_MONITOR_AUTOSTART_VALUE))));
        if (ConfigurationManager.getInstance().getAutostart() && !ConfigurationManager.getInstance().isService()) {
            ipMonitor.start();
        }
    }

    private void saveAutostart(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_MONITOR_AUTOSTART, String.valueOf(ConfigurationManager.getInstance().getAutostart()));
    }

    private void loadURL(Properties properties) {
        try {
            ipMonitor.setUrl(properties.getProperty(IPMonitorProperties.OPTIONS_MONITOR_URL, IPMonitorProperties.OPTIONS_MONITOR_URL_VALUE));
        } catch (MalformedURLException e1) {
            try {
                ipMonitor.setUrl(IPMonitorProperties.OPTIONS_MONITOR_URL_VALUE);
            } catch (MalformedURLException e2) {
            }
        }
    }

    private void saveURL(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_MONITOR_URL, ipMonitor.getUrl());
    }

    private void loadRegularExpression(Properties properties) {
        try {
            ipMonitor.setRegularExpression(properties.getProperty(IPMonitorProperties.OPTIONS_MONITOR_REGULAR_EXPRESSION, IPMonitorProperties.OPTIONS_MONITOR_REGULAR_EXPRESSION_VALUE));
        } catch (PatternSyntaxException e1) {
            try {
                ipMonitor.setRegularExpression(IPMonitorProperties.OPTIONS_MONITOR_REGULAR_EXPRESSION_VALUE);
            } catch (PatternSyntaxException e2) {
            }
        }
    }

    private void saveRegularExpression(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_MONITOR_REGULAR_EXPRESSION, ipMonitor.getRegularExpression());
    }

    private void loadAudioNotification(Properties properties) {
        if (Boolean.valueOf(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_AUDIO, String.valueOf(IPMonitorProperties.OPTIONS_NOTIFICATION_AUDIO_VALUE)))) {
            ipMonitor.addNotification(AudioNotification.getInstance());
        }
    }

    private void saveAudioNotification(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_AUDIO, String.valueOf(ipMonitor.hasNotification(AudioNotification.getInstance())));
    }

    private void loadMailNotification(Properties properties) {
        if (Boolean.valueOf(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_MAIL, String.valueOf(IPMonitorProperties.OPTIONS_NOTIFICATION_MAIL_VALUE)))) {
            ipMonitor.addNotification(MailNotification.getInstance());
        }
    }

    private void saveMailNotification(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_MAIL, String.valueOf(ipMonitor.hasNotification(MailNotification.getInstance())));
    }

    private void loadVisualNotification(Properties properties) {
        if (!IPMonitorSystemTray.getInstance().isSupported()) {
            return;
        }
        if (Boolean.valueOf(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_VISUAL, String.valueOf(IPMonitorProperties.OPTIONS_NOTIFICATION_VISUAL_VALUE)))) {
            ipMonitor.addNotification(VisualNotification.getInstance());
        }
    }

    private void saveVisualNotification(Properties properties) {
        if (!IPMonitorSystemTray.getInstance().isSupported()) {
            return;
        }
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_VISUAL, String.valueOf(ipMonitor.hasNotification(VisualNotification.getInstance())));
    }

    private void loadCommandNotification(Properties properties) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            return;
        }
        if (Boolean.valueOf(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_COMMAND, String.valueOf(IPMonitorProperties.OPTIONS_NOTIFICATION_COMMAND_VALUE)))) {
            ipMonitor.addNotification(CommandNotification.getInstance());
        }
    }

    private void saveCommandNotification(Properties properties) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            return;
        }
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_COMMAND, String.valueOf(ipMonitor.hasNotification(CommandNotification.getInstance())));
    }

    private void loadAudioNotificationConfigurationPath(Properties properties) {
        AudioConfiguration.getInstance().setFileName(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_AUDIO_PATH, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_AUDIO_PATH_VALUE));
    }

    private void saveAudioNotificationConfigurationPath(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_AUDIO_PATH, AudioConfiguration.getInstance().getFileName());
    }

    private void loadMailNotificationConfigurationServer(Properties properties) {
        MailConfiguration.getInstance().setHost(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_SERVER, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_SERVER_VALUE));
    }

    private void saveMailNotificationConfigurationServer(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_SERVER, MailConfiguration.getInstance().getHost());
    }

    private void loadMallNotificationConfigurationPort(Properties properties) {
        int port;
        try {
            port = Integer.valueOf(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_PORT, String.valueOf(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_PORT_VALUE)));
        } catch (NumberFormatException e) {
            port = IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_PORT_VALUE;
        }
        try {
            MailConfiguration.getInstance().setPort(port);
        } catch (NumberFormatException e1) {
            try {
                MailConfiguration.getInstance().setPort(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_PORT_VALUE);
            } catch (NumberFormatException e2) {
            }
        }
    }

    private void saveMallNotificationConfigurationPort(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_PORT, String.valueOf(MailConfiguration.getInstance().getPort()));
    }

    private void loadMailNotificationConfigurationUser(Properties properties) {
        MailConfiguration.getInstance().setUser(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_USER, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_USER_VALUE));
    }

    private void saveMailNotificationConfigurationUser(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_USER, MailConfiguration.getInstance().getUser());
    }

    private void loadMailNotificationConfigurationPassword(Properties properties) {
        MailConfiguration.getInstance().setPassword(DESAlgorithm.getInstance().decrypt(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_PASSWORD, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_PASSWORD_VALUE)));
    }

    private void saveMailNotificationConfigurationPassword(Properties properties) {
        String password = DESAlgorithm.getInstance().encrypt(MailConfiguration.getInstance().getPassword());
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_PASSWORD, (password == null) ? IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_PASSWORD_VALUE : password);
    }

    private void loadMailNotificationConfigurationAuthenticationRequired(Properties properties) {
        MailConfiguration.getInstance().setAuthenticationRequired(Boolean.valueOf(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_AUTHENTICATION_REQUIRED, String.valueOf(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_AUTHENTICATION_REQUIRED_VALUE))));
    }

    private void saveMailNotificationConfigurationAuthenticationRequired(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_AUTHENTICATION_REQUIRED, String.valueOf(MailConfiguration.getInstance().isAuthenticationRequired()));
    }

    private void loadMailNotificationConfigurationUseSSL(Properties properties) {
        MailConfiguration.getInstance().setSSL(Boolean.valueOf(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_USE_SSL, String.valueOf(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_USE_SSL_VALUE))));
    }

    private void saveMailNotificationConfigurationUseSSL(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_USE_SSL, String.valueOf(MailConfiguration.getInstance().isSSL()));
    }

    private void loadMailNotificationConfigurationFromName(Properties properties) {
        MailConfiguration.getInstance().setFromName(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_FROM_NAME, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_FROM_NAME_VALUE));
    }

    private void saveMailNotificationConfigurationFromName(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_FROM_NAME, MailConfiguration.getInstance().getFromName());
    }

    private void loadMailNotificationConfigurationFromAddress(Properties properties) {
        try {
            MailConfiguration.getInstance().setFromAddress(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_FROM_ADDRESS, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_FROM_ADDRESS_VALUE));
        } catch (AddressException e) {
            try {
                MailConfiguration.getInstance().setFromAddress(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_FROM_ADDRESS_VALUE);
            } catch (AddressException e1) {
            }
        }
    }

    private void saveMailNotificationConfigurationFromAddress(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_FROM_ADDRESS, MailConfiguration.getInstance().getFromAddress());
    }

    private void loadMailNotificationConfigurationToAddresses(Properties properties) {
        try {
            MailConfiguration.getInstance().setToAddresses(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_TO_ADDRESSES, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_TO_ADDRESSES_VALUE));
        } catch (AddressException e) {
            try {
                MailConfiguration.getInstance().setToAddresses(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_TO_ADDRESSES_VALUE);
            } catch (AddressException e1) {
            }
        }
    }

    private void saveMailNotificationConfigurationToAddresses(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_TO_ADDRESSES, MailConfiguration.getInstance().getToAddresses());
    }

    private void loadMailNotificationConfigurationSubject(Properties properties) {
        MailConfiguration.getInstance().setSubject(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_SUBJECT, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_SUBJECT_VALUE));
    }

    private void saveMailNotificationConfigurationSubject(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_SUBJECT, MailConfiguration.getInstance().getSubject());
    }

    private void loadMailNotificationConfigurationText(Properties properties) {
        MailConfiguration.getInstance().setText(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_TEXT, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_TEXT_VALUE));
    }

    private void saveMailNotificationConfigurationText(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_TEXT, MailConfiguration.getInstance().getText());
    }

    private void loadMailNotificationConfigurationUseHTML(Properties properties) {
        MailConfiguration.getInstance().setHTML(Boolean.valueOf(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_USE_HTML, String.valueOf(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_USE_HTML_VALUE))));
    }

    private void saveMailNotificationConfigurationUseHTML(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_MAIL_USE_HTML, String.valueOf(MailConfiguration.getInstance().isHTML()));
    }

    private void loadVisualNotificationConfigurationTitle(Properties properties) {
        VisualConfiguration.getInstance().setTitle(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_VISUAL_TITLE, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_VISUAL_TITLE_VALUE));
    }

    private void saveVisualNotificationConfigurationTitle(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_VISUAL_TITLE, VisualConfiguration.getInstance().getTitle());
    }

    private void loadVisualNotificationConfigurationText(Properties properties) {
        VisualConfiguration.getInstance().setText(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_VISUAL_TEXT, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_VISUAL_TEXT_VALUE));
    }

    private void saveVisualNotificationConfigurationText(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_VISUAL_TEXT, VisualConfiguration.getInstance().getText());
    }

    private void loadVisualNotificationConfigurationIcon(Properties properties) {
        String iconText = properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_VISUAL_ICON, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_VISUAL_ICON_VALUE);
        MessageType icon;
        try {
            icon = MessageType.valueOf(iconText);
        } catch (IllegalArgumentException e) {
            icon = MessageType.valueOf(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_VISUAL_ICON_VALUE);
        }
        VisualConfiguration.getInstance().setIcon(icon);
    }

    private void saveVisualNotificationConfigurationIcon(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_VISUAL_ICON, VisualConfiguration.getInstance().getIcon().name());
    }

    private void loadCommandNotificationConfigurationPath(Properties properties) {
        CommandConfiguration.getInstance().setCommand(properties.getProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_COMMAND_PATH, IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_COMMAND_PATH_VALUE));
    }

    private void saveCommandNotificationConfigurationPath(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_NOTIFICATION_CONFIGURATION_COMMAND_PATH, CommandConfiguration.getInstance().getCommand());
    }

    private void loadLookAndFeelClassName(Properties properties) {
        ConfigurationManager.getInstance().setLookAndFeelClassName(properties.getProperty(IPMonitorProperties.OPTIONS_INTERFACE_LOOK_AND_FEEL_CLASS_NAME, IPMonitorProperties.OPTIONS_INTERFACE_LOOK_AND_FEEL_CLASS_NAME_VALUE));
    }

    private void saveLookAndFeelClassName(Properties properties) {
        properties.setProperty(IPMonitorProperties.OPTIONS_INTERFACE_LOOK_AND_FEEL_CLASS_NAME, ConfigurationManager.getInstance().getLookAndFeelClassName());
    }
}
