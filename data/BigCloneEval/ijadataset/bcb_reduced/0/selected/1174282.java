package net.sourceforge.jepesi.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import net.sourceforge.jepesi.addon.AddonInterface;
import net.sourceforge.jepesi.addon.PluginLoader;
import net.sourceforge.jepesi.model.Host;
import net.sourceforge.jepesi.model.AddonStore;
import net.sourceforge.jepesi.model.Lang;
import net.sourceforge.jepesi.plugin.OsPluginInterface;
import net.sourceforge.jepesi.plugin.ToolPluginInterface;

public class PluginControl {

    private JepesiControl jepseiControl;

    private PluginLoader pluginLoader;

    private final String AddonDir = "addons/";

    private Map<Integer, AddonStore> addons = new HashMap<Integer, AddonStore>();

    private List<String> activeAddons = new ArrayList<String>();

    private List<Integer> loadedAddons = new ArrayList<Integer>();

    private final String osPluginDir = "plugins/os/";

    private ArrayList<OsPluginInterface> osPlugins = new ArrayList<OsPluginInterface>();

    private List<String> activeOsPlugins = new ArrayList<String>();

    private List<String> loadedOsPlugins = new ArrayList<String>();

    private final String toolPluginDir = "plugins/tool/";

    private ArrayList<ToolPluginInterface> toolPlugins = new ArrayList<ToolPluginInterface>();

    private List<String> activeToolPlugins = new ArrayList<String>();

    private List<String> loadedToolPlugins = new ArrayList<String>();

    public PluginControl(JepesiControl jepseiControl, String resPrePath) {
        this.jepseiControl = jepseiControl;
        this.pluginLoader = new PluginLoader(PluginControl.class.getClassLoader(), resPrePath);
        activeAddons = jepseiControl.getConfig().getActiveAddons();
        activeOsPlugins = jepseiControl.getConfig().getActiveOsPlugins();
        activeToolPlugins = jepseiControl.getConfig().getActiveToolPlugins();
        if (jepseiControl.isTestRun()) {
            loadBundelsTestRun();
        }
    }

    private void loadBundelsTestRun() {
        for (String activeAddon : activeAddons) {
            try {
                InputStream in = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jepesi/addon/" + activeAddon.toLowerCase() + "/" + activeAddon + "_en_US.properties");
                byte[] bundleData = toBytes(in);
                Lang.addAddonBundle(activeAddon + "_en_EN", bundleData);
            } catch (Exception e) {
            }
        }
    }

    public ImageIcon getIcon(String name) {
        if (jepseiControl.isTestRun()) {
            String[] parts = name.split("\\.");
            name = parts[parts.length - 1];
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jepesi/addon/" + name.toLowerCase() + "/icon.gif");
            byte[] imageData;
            try {
                imageData = toBytes(in);
                return new ImageIcon(imageData);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return pluginLoader.getIcon(name);
        }
    }

    public BufferedImage getImage(String name) {
        if (jepseiControl.isTestRun()) {
            String[] parts = name.split("\\.");
            String ex = parts[parts.length - 1];
            String fileName = parts[parts.length - 2];
            String packageName = parts[parts.length - 3];
            InputStream in = this.getClass().getClassLoader().getResourceAsStream("net/sourceforge/jepesi/addon/" + packageName + "/" + fileName + "." + ex);
            try {
                return ImageIO.read(in);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return pluginLoader.getImage(name);
        }
    }

    private byte[] toBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    public void loadAddons(Host host) {
        if (loadedAddons.indexOf(host.getId()) == -1) {
            AddonStore store = new AddonStore();
            for (String addonName : activeAddons) {
                try {
                    AddonInterface addon = getAddon(addonName);
                    if (addon.isEnabled(host.getOs(), host.getTools())) {
                        store.add(addon);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            addons.put(host.getId(), store);
            loadedAddons.add(host.getId());
            this.jepseiControl.log("Loading Addons for " + host.getName());
        }
    }

    public void loadOsPlugins() {
        for (String osPluginName : activeOsPlugins) {
            if (loadedOsPlugins.indexOf(osPluginName) == -1) {
                try {
                    osPlugins.add(getOsPlugin(osPluginName));
                    loadedOsPlugins.add(osPluginName);
                    this.jepseiControl.log("Loading os Plugin " + osPluginName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void loadToolPlugins() {
        for (String toolPluginName : activeToolPlugins) {
            if (loadedOsPlugins.indexOf(toolPluginName) == -1) {
                try {
                    toolPlugins.add(getToolPlugin(toolPluginName));
                    loadedToolPlugins.add(toolPluginName);
                    this.jepseiControl.log("Loading tool Plugin " + toolPluginName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized List<AddonInterface> getAddons(Host host) {
        AddonStore store = addons.get(host.getId());
        if (store == null) {
            store = new AddonStore();
            addons.put(host.getId(), store);
        }
        return Collections.unmodifiableList(store);
    }

    public synchronized List<OsPluginInterface> getOsPlugins() {
        List<OsPluginInterface> osTempPlugins = new ArrayList<OsPluginInterface>();
        for (String osPluginName : loadedOsPlugins) {
            try {
                Class<?> PluginClass = pluginLoader.loadClass("net.sourceforge.jepesi.plugin.os." + osPluginName);
                osTempPlugins.add((OsPluginInterface) PluginClass.newInstance());
            } catch (Exception e) {
                System.err.println("Error on loading predfined Plugin class (PluginControl.getOsPlugins())");
            }
        }
        return Collections.unmodifiableList(osTempPlugins);
    }

    public synchronized List<ToolPluginInterface> getToolPlugins() {
        List<ToolPluginInterface> toolTempPlugins = new ArrayList<ToolPluginInterface>();
        for (String toolPluginName : loadedToolPlugins) {
            try {
                Class<?> PluginClass = pluginLoader.loadClass("net.sourceforge.jepesi.plugin.tool." + toolPluginName);
                toolTempPlugins.add((ToolPluginInterface) PluginClass.newInstance());
            } catch (Exception e) {
                System.err.println("Error on loading predfined Plugin class (PluginControl.getToolPlugins())");
            }
        }
        return Collections.unmodifiableList(toolTempPlugins);
    }

    public AddonInterface getAddon(final String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Class<?> PluginClass;
        try {
            PluginClass = pluginLoader.loadClass("net.sourceforge.jepesi.addon." + name.toLowerCase() + "." + name);
        } catch (Exception e) {
            PluginClass = pluginLoader.loadPlugin("net.sourceforge.jepesi.addon." + name.toLowerCase(), AddonDir + name + ".jar", name);
        }
        Object[] aoParams = new Object[1];
        aoParams[0] = jepseiControl;
        Class<?>[] acParams = new Class[1];
        acParams[0] = JepesiInterface.class;
        Constructor<?> oConstr;
        oConstr = PluginClass.getConstructor(acParams);
        AddonInterface pli = (AddonInterface) oConstr.newInstance(aoParams);
        return pli;
    }

    public OsPluginInterface getOsPlugin(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Class<?> PluginClass;
        try {
            PluginClass = pluginLoader.loadClass("net.sourceforge.jepesi.plugin.os." + name);
        } catch (Exception e) {
            PluginClass = pluginLoader.loadPlugin("net.sourceforge.jepesi.plugin.os", osPluginDir + name + ".jar", name);
        }
        OsPluginInterface osplg = (OsPluginInterface) PluginClass.newInstance();
        return osplg;
    }

    public ToolPluginInterface getToolPlugin(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Class<?> PluginClass;
        try {
            PluginClass = pluginLoader.loadClass("net.sourceforge.jepesi.plugin.tool." + name);
        } catch (Exception e) {
            PluginClass = pluginLoader.loadPlugin("net.sourceforge.jepesi.plugin.tool", toolPluginDir + name + ".jar", name);
        }
        ToolPluginInterface toplg = (ToolPluginInterface) PluginClass.newInstance();
        return toplg;
    }
}
