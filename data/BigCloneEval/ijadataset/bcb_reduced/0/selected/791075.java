package jnewsgate.plugin;

import jnewsgate.*;
import jnewsgate.Authenticator;
import jnewsgate.auth.*;
import jnewsgate.impl.*;
import jnewsgate.config.*;
import java.util.*;
import java.io.*;
import java.util.logging.*;
import java.lang.reflect.*;
import java.net.*;

/**
 * Loads and manages plugins and their factories.
 */
public class PluginManager {

    private static PluginManager instance;

    public static PluginManager getInstance() {
        if (instance == null) instance = new PluginManager();
        return instance;
    }

    private Map authenticators = new HashMap(), groupSources = new HashMap(), hashes = new HashMap();

    private Map authConfigs = new HashMap(), sourceConfigs = new HashMap();

    private static Logger l = Log.get();

    private PluginManager() {
        try {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            InputStream in = cl.getResourceAsStream("res/builtinspec.properties");
            parseProperties(in, cl);
            in.close();
        } catch (IOException ex) {
            l.log(Level.SEVERE, "Cannot load internal plugin spec", ex);
            return;
        }
        File f = new File("plugins");
        if (!f.exists()) {
            l.warning("No plugin folder found.");
            return;
        }
        File[] ps = f.listFiles();
        URL[] urls = new URL[ps.length];
        for (int i = 0; i < ps.length; i++) {
            try {
                urls[i] = ps[i].toURI().toURL();
            } catch (MalformedURLException ex) {
                l.log(Level.SEVERE, "Cannot add file to classloader", ex);
                return;
            }
        }
        URLClassLoader ucl = new URLClassLoader(urls);
        try {
            for (int i = 0; i < ps.length; i++) {
                URLClassLoader resl = new URLClassLoader(new URL[] { urls[i] });
                InputStream in = resl.getResourceAsStream("res/pluginspec.properties");
                if (in == null) continue;
                l.finer("Loading plugin: " + urls[i]);
                parseProperties(in, ucl);
                in.close();
            }
        } catch (NumberFormatException ex) {
            l.log(Level.SEVERE, "Cannot parse plugin spec", ex);
            return;
        } catch (IOException ex) {
            l.log(Level.SEVERE, "Cannot load plugin spec", ex);
            return;
        }
    }

    private void parseProperties(InputStream in, ClassLoader ucl) throws IOException {
        Properties prs = new Properties();
        prs.load(in);
        int cnt = Integer.parseInt(prs.getProperty("auth.count", "0"));
        for (int j = 0; j < cnt; j++) {
            String name = prs.getProperty("auth." + j + ".name");
            String clazz = prs.getProperty("auth." + j + ".class");
            if (name == null || clazz == null) {
                l.severe("Cannot load auth." + j + " settings");
                return;
            }
            AuthenticatorFactory a = (AuthenticatorFactory) loadFactory(ucl, clazz, prs, "auth." + j + ".");
            authenticators.put(name, a);
            String config = prs.getProperty("auth." + j + ".config");
            if (config != null) {
                if (config.equals("same")) {
                    if (!(a instanceof PluginConfig)) {
                        l.severe("Cannot use authenticator as config.");
                        return;
                    }
                    authConfigs.put(name, a);
                } else {
                    PluginConfig pc = (PluginConfig) loadFactory(ucl, config, prs, "auth." + j + ".");
                    authConfigs.put(name, pc);
                }
            }
        }
        cnt = Integer.parseInt(prs.getProperty("source.count", "0"));
        for (int j = 0; j < cnt; j++) {
            String name = prs.getProperty("source." + j + ".name");
            String clazz = prs.getProperty("source." + j + ".class");
            if (name == null || clazz == null) {
                l.severe("Cannot load source." + j + " settings");
                return;
            }
            l.finer("Found mapping: " + name + " -> " + clazz);
            GroupSourceFactory a = (GroupSourceFactory) loadFactory(ucl, clazz, prs, "source." + j + ".");
            groupSources.put(name, a);
            String config = prs.getProperty("source." + j + ".config");
            if (config != null) {
                if (config.equals("same")) {
                    if (!(a instanceof PluginConfig)) {
                        l.severe("Cannot use authenticator as config.");
                        return;
                    }
                    sourceConfigs.put(name, a);
                } else {
                    PluginConfig pc = (PluginConfig) loadFactory(ucl, config, prs, "source." + j + ".");
                    sourceConfigs.put(name, pc);
                }
            }
        }
        cnt = Integer.parseInt(prs.getProperty("hash.count", "0"));
        for (int j = 0; j < cnt; j++) {
            String name = prs.getProperty("hash." + j + ".name");
            String clazz = prs.getProperty("hash." + j + ".class");
            if (name == null || clazz == null) {
                l.severe("Cannot load auth." + j + " settings");
                return;
            }
            l.finer("Found mapping: " + name + " -> " + clazz);
            Hash h = (Hash) loadFactory(ucl, clazz, prs, "hash." + j + ".");
            hashes.put(name, h);
        }
    }

    private Object loadFactory(ClassLoader cl, String className, Properties ps, String prefix) {
        try {
            Class clazz = cl.loadClass(className);
            try {
                Constructor c = clazz.getConstructor(new Class[] { Properties.class, String.class, ClassLoader.class });
                return c.newInstance(new Object[] { ps, prefix, cl });
            } catch (NoSuchMethodException ex) {
                return clazz.newInstance();
            }
        } catch (Exception ex) {
            l.log(Level.SEVERE, "Cannot load class", ex);
            return null;
        }
    }

    public Authenticator createAuthenticator(Properties settings, String prefix) {
        String type = settings.getProperty(prefix + "type");
        AuthenticatorFactory af = (AuthenticatorFactory) authenticators.get(type);
        if (af == null) throw new IllegalArgumentException();
        return af.createAuthenticator(settings, prefix);
    }

    public GroupSource createGroupSource(Properties settings, String prefix) {
        String type = settings.getProperty(prefix + "type");
        GroupSourceFactory gsf = (GroupSourceFactory) groupSources.get(type);
        if (gsf == null) throw new IllegalArgumentException("No GroupSource found for " + "type " + type);
        return gsf.createGroupSource(settings, prefix);
    }

    public Hash getHash(String type) {
        return (Hash) hashes.get(type);
    }

    public Properties readConfigFile() {
        Properties props = new Properties();
        try {
            InputStream in = new FileInputStream(System.getProperty("jnewsgate.configfile", "jnewsgate.conf"));
            props.load(in);
            in.close();
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            l.log(Level.SEVERE, "Error reading config file", ex);
        }
        return props;
    }

    public void saveConfigFile(Properties props) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            props.store(baos, "Generated config file");
            BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()), "ISO-8859-1"));
            List l = new ArrayList();
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) l.add(line);
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(System.getProperty("jnewsgate.configfile", "jnewsgate.conf")), "ISO-8859-1"));
            List l2 = new ArrayList();
            while ((line = br.readLine()) != null) {
                if (!line.trim().startsWith("#") && line.indexOf("=") != -1) {
                    boolean found = false;
                    String pfx = line.substring(0, line.indexOf("=") + 1);
                    for (int i = 0; i < l.size(); i++) {
                        if (((String) l.get(i)).startsWith(pfx)) {
                            line = (String) l.get(i);
                            l.remove(i);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println("Not found: " + line);
                        line = "#" + line;
                    }
                }
                l2.add(line);
            }
            if (l.size() > 0) {
                l2.add("");
                l2.add("## New options:");
                l2.addAll(l);
            }
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(System.getProperty("jnewsgate.configfile", "jnewsgate.conf")), "ISO-8859-1"));
            for (int i = 0; i < l2.size(); i++) {
                out.write((String) l2.get(i));
                out.newLine();
            }
            out.flush();
            out.close();
        } catch (IOException ex) {
            l.log(Level.SEVERE, "IOException while saving config file", ex);
        }
    }

    public Map getAuthConfigs() {
        return authConfigs;
    }

    public Map getSourceConfigs() {
        return sourceConfigs;
    }
}
