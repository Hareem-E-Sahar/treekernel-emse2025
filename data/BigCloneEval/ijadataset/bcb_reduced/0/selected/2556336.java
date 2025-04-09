package com.mockturtlesolutions.snifflib.extensions;

import java.util.LinkedHashSet;
import java.io.*;
import java.net.URLClassLoader;
import java.net.URL;
import java.util.zip.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.Set;
import java.util.Enumeration;
import java.util.HashSet;
import javax.swing.JOptionPane;

public abstract class AbstractModuleList implements ModuleListing {

    protected LinkedHashMap modules;

    protected File xmlfile;

    private String modulePath;

    private Vector moduleListListeners;

    private HashSet moduleClasses;

    public AbstractModuleList() {
        this.modules = new LinkedHashMap();
        this.moduleListListeners = new Vector();
        this.moduleClasses = new HashSet();
    }

    public void addModuleListListener(ModuleListListener l) {
        this.moduleListListeners.add(l);
    }

    public void removeModuleListListener(ModuleListListener l) {
        this.moduleListListeners.remove(l);
    }

    public void fireModuleAdded(String modname) {
        ModuleListEvent ev = new ModuleListEvent(ModuleListEvent.MODULE_ADDED, modname);
        for (int k = 0; k < this.moduleListListeners.size(); k++) {
            ((ModuleListListener) this.moduleListListeners.get(k)).actionPerformed(ev);
        }
    }

    public void fireModuleRemoved(String modname) {
        ModuleListEvent ev = new ModuleListEvent(ModuleListEvent.MODULE_REMOVED, modname);
        for (int k = 0; k < this.moduleListListeners.size(); k++) {
            ((ModuleListListener) this.moduleListListeners.get(k)).actionPerformed(ev);
        }
    }

    public void addModuleClass(Class x) {
        this.moduleClasses.add(x);
    }

    public void removeModuleClass(Class x) {
        this.moduleClasses.remove(x);
    }

    public void setModulePath(String path) {
        this.modulePath = path;
    }

    public String getModulePath() {
        return (this.modulePath);
    }

    public void removeAllModules() {
        Vector names = this.getModuleNames();
        for (int k = 0; k < names.size(); k++) {
            this.removeModule((String) names.get(k));
            this.fireModuleRemoved((String) names.get(k));
        }
    }

    public void transferListing(ModuleListing that) {
        Vector modnames = that.getModuleNames();
        this.removeAllModules();
        for (int i = 0; i < modnames.size(); i++) {
            String name = (String) modnames.get(i);
            this.addModule(name);
            this.setAuthor(name, that.getAuthor(name));
            this.setDescription(name, that.getDescription(name));
            this.setVersion(name, that.getVersion(name));
            this.setLastModified(name, that.getLastModified(name));
            this.setCopyRight(name, that.getCopyRight(name));
            this.setShortName(name, that.getShortName(name));
            this.fireModuleAdded(name);
        }
    }

    public Class getModule(String modname) {
        String path = this.modulePath;
        File moduleDir = new File(path);
        if (!moduleDir.exists()) {
            throw new RuntimeException("The modules directory " + moduleDir.getAbsolutePath() + " does not exist.");
        }
        URL[] thisjarurl = new URL[1];
        try {
            thisjarurl[0] = new URL("file://" + path + "/");
        } catch (java.net.MalformedURLException err) {
            throw new RuntimeException("Problem creating URL to jar directory.", err);
        }
        URLClassLoader loader = new URLClassLoader(thisjarurl);
        Class out = null;
        try {
            out = Class.forName(modname, true, loader);
        } catch (java.lang.ClassNotFoundException err) {
            throw new RuntimeException("Problem loading class.", err);
        }
        return (out);
    }

    public void update() {
        this.removeAllModules();
        if (this.modulePath == null) {
            throw new RuntimeException("The module path can not be null.");
        }
        String path = this.modulePath;
        File moduleDir = new File(path);
        if (!moduleDir.exists() || !moduleDir.isDirectory()) {
            int ans = JOptionPane.showConfirmDialog(null, "Create modules directory " + this.modulePath + "?", "Extension modules directory does not exist!", JOptionPane.YES_NO_CANCEL_OPTION);
            if (ans == JOptionPane.YES_OPTION) {
                try {
                    moduleDir.mkdir();
                } catch (SecurityException err) {
                    throw new RuntimeException("Problem creating and saving new configuration file.", err);
                }
            } else {
                return;
            }
        }
        File[] jarfiles = moduleDir.listFiles(new FileFilter() {

            public boolean accept(File f) {
                boolean out = false;
                if (f.getName().endsWith(".jar")) {
                    out = true;
                }
                return (out);
            }
        });
        if (jarfiles == null) {
            return;
        }
        if (jarfiles.length == 0) {
            return;
        }
        URL[] thisjarurl = new URL[1];
        URLClassLoader loader;
        for (int k = 0; k < jarfiles.length; k++) {
            ZipFile zip = null;
            try {
                zip = new ZipFile(jarfiles[k]);
            } catch (java.util.zip.ZipException err) {
                throw new IllegalArgumentException("Problem accessing jar file " + jarfiles[k] + ".", err);
            } catch (java.io.IOException err) {
                throw new IllegalArgumentException("Problem accessing jar file " + jarfiles[k] + ".", err);
            }
            Enumeration enumer = zip.entries();
            ZipEntry entry;
            try {
                thisjarurl[0] = new URL("file://" + jarfiles[k].getAbsolutePath());
            } catch (java.net.MalformedURLException err) {
                throw new IllegalArgumentException("Problem creating URL for file " + jarfiles[k] + ".", err);
            }
            loader = new URLClassLoader(thisjarurl);
            while (enumer.hasMoreElements()) {
                entry = (ZipEntry) enumer.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    String classname = name.replace(".class", "");
                    classname = classname.replace("/", ".");
                    Class maybemodule = null;
                    try {
                        maybemodule = loader.loadClass(classname);
                    } catch (ClassNotFoundException err) {
                        throw new RuntimeException("Problem loading class from jar.", err);
                    }
                    Class[] intfces = maybemodule.getInterfaces();
                    if ((maybemodule != null) && (Module.class.isAssignableFrom(maybemodule))) {
                        Iterator iter = this.moduleClasses.iterator();
                        boolean filterPass = false;
                        while (iter.hasNext()) {
                            Class filter = (Class) iter.next();
                            if (filter.isAssignableFrom(maybemodule.getSuperclass())) {
                                filterPass = true;
                                break;
                            } else {
                            }
                        }
                        if (!filterPass) {
                            break;
                        }
                        Date d = new Date(entry.getTime());
                        String time = d.toString();
                        Module mod = null;
                        try {
                            mod = (Module) maybemodule.newInstance();
                        } catch (java.lang.InstantiationException err) {
                            throw new RuntimeException("Problem instantiating class " + classname + ".", err);
                        } catch (java.lang.IllegalAccessException err) {
                            throw new RuntimeException("Problem instantiating class " + classname + ".", err);
                        }
                        this.addModule(classname);
                        this.setDescription(classname, mod.getDescription());
                        this.setAuthor(classname, mod.getAuthor());
                        this.setCopyRight(classname, mod.getCopyRight());
                        this.setLastModified(classname, mod.getLastModified());
                        this.setVersion(classname, mod.getVersion());
                        this.setShortName(classname, mod.getShortName());
                        this.fireModuleAdded(classname);
                    } else {
                    }
                }
            }
        }
    }
}
