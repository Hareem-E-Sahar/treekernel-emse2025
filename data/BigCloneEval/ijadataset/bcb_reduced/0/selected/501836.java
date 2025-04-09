package com.peterhi.server.admin;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;

public class AdminServer implements Runnable {

    private static final String UI_LANG_BUNDLE = "com/peterhi/server/admin/resource/lang";

    private static final String DELIMITER = ".";

    private static final String SERVER_CONF = "server.conf";

    private static final String USER_ROOT = "com.peterhi.server.admin.user.";

    private static final String COMMAND_ROOT = "com.peterhi.server.admin.command.";

    private static final String LOCALE_ROOT = "com.peterhi.server.admin.locale.";

    private CommandLineParser parser = new PosixParser();

    private Map users = new HashMap();

    private Map commands = new HashMap();

    private Map locales = new HashMap();

    private ServerSocket socket;

    public AdminServer() throws IOException {
        socket = new ServerSocket(8080);
        FileInputStream fis = new FileInputStream(SERVER_CONF);
        Properties props = new Properties();
        props.load(fis);
        fis.close();
        initAdminUsers(props);
        initCommands(props);
        initLocales(props);
    }

    public String[] supportedLocales() {
        return (String[]) locales.keySet().toArray(new String[locales.size()]);
    }

    public String getLocaleDisplayString(String locale) {
        return (String) locales.get(locale);
    }

    public boolean authenticate(String user, String password) {
        if (!users.containsKey(user)) return false;
        return users.get(user).equals(password);
    }

    public AdminCommand getCommand(String name, Locale locale) {
        try {
            Class commandType = Class.forName(commands.get(name).toString());
            Constructor ctor = commandType.getConstructor((Class[]) null);
            return (AdminCommand) ctor.newInstance((Object[]) null);
        } catch (Exception ex) {
            return null;
        }
    }

    public String[] commandNames() {
        return (String[]) commands.keySet().toArray(new String[commands.size()]);
    }

    public CommandLineParser getParser() {
        return parser;
    }

    public void run() {
        while (!socket.isClosed()) {
            try {
                Socket client = socket.accept();
                AdminClientWorker clientWorker = new AdminClientWorker(this, client);
                new Thread(clientWorker).start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public String getString(String key, Locale locale) {
        if (locale == null) locale = Locale.getDefault();
        try {
            return ResourceBundle.getBundle(UI_LANG_BUNDLE, locale).getString(key);
        } catch (Exception ex) {
            return key;
        }
    }

    private void initAdminUsers(Properties props) {
        for (Iterator itor = props.entrySet().iterator(); itor.hasNext(); ) {
            Map.Entry e = (Map.Entry) itor.next();
            String key = e.getKey().toString();
            String value = e.getValue().toString();
            if (key.toLowerCase().startsWith(USER_ROOT)) users.put(key.substring(key.lastIndexOf(DELIMITER) + 1), value);
        }
    }

    private void initCommands(Properties props) {
        for (Iterator itor = props.entrySet().iterator(); itor.hasNext(); ) {
            Map.Entry e = (Map.Entry) itor.next();
            String key = e.getKey().toString();
            String value = e.getValue().toString();
            if (key.toLowerCase().startsWith(COMMAND_ROOT)) commands.put(key.substring(key.lastIndexOf(DELIMITER) + 1), value);
        }
    }

    private void initLocales(Properties props) {
        for (Iterator itor = props.entrySet().iterator(); itor.hasNext(); ) {
            Map.Entry e = (Map.Entry) itor.next();
            String key = e.getKey().toString();
            String value = e.getValue().toString();
            if (key.toLowerCase().startsWith(LOCALE_ROOT)) locales.put(key.substring(key.lastIndexOf(DELIMITER) + 1), value);
        }
    }
}
