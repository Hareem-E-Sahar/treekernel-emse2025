package net.sourceforge.theba.core;

import net.sourceforge.theba.core.gui.ThebaGUI;
import javax.swing.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class PluginLoader extends ClassLoader {

    private JFrame mainWindow;

    private ThebaGUI gui;

    private ArrayList<RegionDescriptor> descriptors;

    private ArrayList<Tracker> trackers;

    private ArrayList<Plugin> plugins;

    private Hashtable<String, Integer> jarEntrySizes = new Hashtable<String, Integer>();

    private Hashtable<String, byte[]> jarEntryContents = new Hashtable<String, byte[]>();

    @SuppressWarnings("unchecked")
    private Hashtable<String, Class> classes = new Hashtable<String, Class>();

    private final boolean debug = false;

    private boolean jarFileIsAvailable;

    private boolean isPlugin(String name) {
        boolean retval = false;
        if (name.startsWith("net/sourceforge/theba/descriptors/")) retval = true; else if (name.startsWith("net/sourceforge/theba/plugins/")) retval = true; else if (name.startsWith("net/sourceforge/theba/trackers/")) retval = true;
        if (debug) {
            System.out.println("Checking name " + name + " found " + retval);
        }
        return retval;
    }

    public PluginLoader(final ThebaGUI gui, final String directory, final String jarFilePrefix) {
        this.gui = gui;
        mainWindow = gui.getWindow();
        final File fileDir = new File(directory);
        final File[] arrFile = fileDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.toLowerCase().matches(jarFilePrefix + ".*.jar");
            }
        });
        jarFileIsAvailable = false;
        String jarFileName = null;
        if (arrFile != null && arrFile.length > 0) {
            jarFileName = arrFile[0].getName();
            jarFileIsAvailable = true;
        }
        if (!jarFileIsAvailable) {
            System.out.println("Jar file " + directory + "/theba*.jar" + " not found, loading plugins from filesystem instead.");
        } else {
            JarFile jarFile;
            try {
                jarFile = new JarFile(jarFileName);
                Enumeration<JarEntry> e = jarFile.entries();
                while (e.hasMoreElements()) {
                    JarEntry jarEntry = e.nextElement();
                    if (!isPlugin(jarEntry.getName())) {
                        continue;
                    }
                    jarEntrySizes.put(jarEntry.getName(), (int) jarEntry.getSize());
                }
                jarFile.close();
                FileInputStream fis = new FileInputStream(jarFileName);
                BufferedInputStream bis = new BufferedInputStream(fis);
                JarInputStream jis = new JarInputStream(bis);
                JarEntry jarEntry;
                while ((jarEntry = jis.getNextJarEntry()) != null) {
                    if (jarEntry.isDirectory()) {
                        continue;
                    }
                    if (!isPlugin(jarEntry.getName())) {
                        continue;
                    }
                    int size = (int) jarEntry.getSize();
                    if (size == -1) {
                        size = jarEntrySizes.get(jarEntry.getName());
                    }
                    byte[] b = new byte[size];
                    int rb = 0;
                    int chunk = 0;
                    while ((size - rb) > 0) {
                        chunk = jis.read(b, rb, size - rb);
                        if (chunk == -1) {
                            break;
                        }
                        rb += chunk;
                    }
                    jarEntryContents.put(jarEntry.getName(), b);
                }
            } catch (IOException why) {
                why.printStackTrace();
            }
        }
        init();
    }

    private void init() {
        descriptors = loadRegionDescriptors();
        plugins = loadPlugins();
        trackers = loadTrackers();
        if (trackers.size() == 0 || descriptors.size() == 0 || plugins.size() == 0) {
            String errorMessage = "Could not get all required plugins.\n" + "Got " + trackers.size() + " tracker plugins.\n" + "Got " + descriptors.size() + " region descriptor plugins.\n" + "Got " + plugins.size() + " other descriptor plugins.\n";
            JOptionPane.showMessageDialog(mainWindow, errorMessage, "Loading problem", JOptionPane.ERROR_MESSAGE);
        }
    }

    private byte[] getClassBytes(String name) {
        return jarEntryContents.get(name);
    }

    private String classToFile(String name) {
        name = name + ".class";
        char[] clsName = name.toCharArray();
        for (int i = 0; i < clsName.length - 6; i++) {
            if (clsName[i] == '.') {
                clsName[i] = '/';
            }
        }
        return new String(clsName);
    }

    private String fileToClass(String name) {
        char[] clsName = name.toCharArray();
        for (int i = clsName.length - 6; i >= 0; i--) {
            if (clsName[i] == '/') {
                clsName[i] = '.';
            }
        }
        return new String(clsName, 0, clsName.length - 6);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<RegionDescriptor> loadRegionDescriptors() {
        ArrayList<RegionDescriptor> descriptors = new ArrayList<RegionDescriptor>();
        ArrayList<String> classes = getPluginClassNames("descriptors");
        for (String classname : classes) {
            try {
                Class c = loadClass(classname);
                if (c.getInterfaces().length > 0 && c.getInterfaces()[0].getName().equals("net.sourceforge.theba.core.RegionDescriptor")) {
                    Object o = c.newInstance();
                    descriptors.add((RegionDescriptor) o);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return descriptors;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Plugin> loadPlugins() {
        ArrayList<Plugin> plugins = new ArrayList<Plugin>();
        ArrayList<String> classes = getPluginClassNames("plugins");
        for (String classname : classes) {
            try {
                Class c = loadClass(classname);
                if (c.getInterfaces().length > 0 && c.getInterfaces()[0].getName().equals("net.sourceforge.theba.core.Plugin")) {
                    Object o = c.newInstance();
                    plugins.add((Plugin) o);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return plugins;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Tracker> loadTrackers() {
        ArrayList<Tracker> trackers = new ArrayList<Tracker>();
        ArrayList<String> classes = getPluginClassNames("trackers");
        for (String classname : classes) {
            try {
                Class c = loadClass(classname);
                Constructor cs = c.getConstructors()[0];
                Object[] args = new Object[1];
                args[0] = gui;
                Object o = cs.newInstance(args);
                trackers.add((Tracker) o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return trackers;
    }

    private ArrayList<String> getPluginClassNames(final String dirName) {
        ArrayList<String> names = new ArrayList<String>();
        File dir = new File("net/sourceforge/theba/" + dirName);
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                boolean retval = name.endsWith(".class");
                retval &= !name.contains("$");
                return retval;
            }
        };
        String[] files = dir.list(filter);
        if (files != null) {
            for (String file : files) {
                names.add("net.sourceforge.theba." + dirName + "." + fileToClass(file));
            }
        }
        if (debug) System.out.println("Found " + names.size() + " classes in " + dirName);
        if (names.size() == 0 && jarFileIsAvailable) {
            Enumeration<String> keys = jarEntryContents.keys();
            while (keys.hasMoreElements()) {
                String name = keys.nextElement();
                if (name.startsWith("net/sourceforge/theba/" + dirName) && !name.contains("$") && name.endsWith(".class")) {
                    names.add(fileToClass(name));
                    if (debug) {
                        System.out.println("Added class " + fileToClass(name));
                    }
                }
            }
        }
        return names;
    }

    public ArrayList<RegionDescriptor> getRegionDescriptors() {
        return descriptors;
    }

    public ArrayList<Plugin> getPlugins() {
        return plugins;
    }

    public ArrayList<Tracker> getTrackers() {
        return trackers;
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (debug) System.out.println("Call to loadClass " + name + "with resolve=" + resolve);
        Class c = classes.get(name);
        if (c != null) {
            if (debug) {
                System.out.println("already loaded Class " + name);
            }
            return c;
        }
        if (jarEntryContents.containsKey(classToFile(name))) {
            if (debug) {
                System.out.println("Loading class " + name + " from jar file.");
            }
            byte[] classData = getClassBytes(classToFile(name));
            c = defineClass(name, classData, 0, classData.length);
            if (c == null) {
                throw new ClassFormatError();
            }
        }
        if (c == null) {
            if (debug) {
                System.out.println("Using parent class loader to load " + name);
            }
            return super.loadClass(name, resolve);
        } else {
            if (resolve) {
                resolveClass(c);
            }
            classes.put(name, c);
            return c;
        }
    }
}
