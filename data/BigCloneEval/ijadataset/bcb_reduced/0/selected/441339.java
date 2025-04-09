package org.one.stone.soup.xapp.plugin.manager;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
import org.one.stone.soup.file.FileHelper;
import org.one.stone.soup.stringhelper.FileNameHelper;
import org.one.stone.soup.stringhelper.StringArrayHelper;
import org.one.stone.soup.xapp.XappPlugin;
import org.one.stone.soup.xapp.XappRootApplication;
import org.one.stone.soup.xapp.factories.XappMenuFactory;
import org.one.stone.soup.xapp.factories.XappPluginFactory;
import org.one.stone.soup.xapp.filebrowser.XappFileBrowser;
import org.one.stone.soup.xml.XmlElement;
import org.one.stone.soup.xml.helper.XmlCloner;
import org.one.stone.soup.xml.helper.XmlPathSearch;
import org.one.stone.soup.xml.stream.XmlLoader;

public class PluginManager extends XappPlugin {

    private XmlElement xmlData;

    public PluginManager(String pluginFileName, XmlElement definition) {
        super(pluginFileName, definition);
    }

    public void initialise() {
        if (xmlData == null) {
            xmlData = new XmlElement("plugins");
        }
        for (int loop = 0; loop < xmlData.getElementCount(); loop++) {
            XmlElement plugin = xmlData.getElementByIndex(loop);
            XappPlugin xappPlugin = XappRootApplication.loadPlugin(new File(plugin.getValue()).getAbsoluteFile());
            xappPlugin.initialise();
        }
        XappRootApplication.getComponentFactory().getDragAndDropFactory().buildXappDropReceiver(XappRootApplication.getComponentStore().getRoot(), this, "plugin manager", null);
    }

    public XmlElement getData() {
        return xmlData;
    }

    public void setData(XmlElement data) {
        xmlData = data;
    }

    public XmlElement getMenu() {
        XmlElement xMenu = XappMenuFactory.getMenuDefinition("Tools");
        XappMenuFactory.addMenuItem(xMenu, "Add Plugin", "addPlugin");
        XappMenuFactory.addMenuItem(xMenu, "Remove Plugin", "removePlugin");
        XappMenuFactory.addMenuItem(xMenu, "Edit Plugin", "editPlugin");
        XappMenuFactory.addMenuItem(xMenu, "Create New Plugin", "createPlugin");
        return xMenu;
    }

    public XmlElement getActions() {
        XmlElement xActions = XappPluginFactory.getActionsDefinition();
        XappPluginFactory.addAction(this, xActions, "addPlugin");
        XappPluginFactory.addAction(this, xActions, "removePlugin");
        XappPluginFactory.addAction(this, xActions, "editPlugin");
        XappPluginFactory.addAction(this, xActions, "createPlugin");
        return xActions;
    }

    public void addPlugin() {
        XappFileBrowser fileBrowser = XappRootApplication.getComponentFactory().buildFileBrowser("Add Plugin", "Xapp Plugin", "plugin");
        String file = fileBrowser.getOpenFile();
        addPlugin(file);
    }

    private void addPlugin(String file) {
        if (file != null && FileNameHelper.getExt(file).equals("plugin")) {
            XappRootApplication.loadPlugin(new File(file).getAbsoluteFile());
            xmlData.addChild("plugin").addValue(file);
        }
    }

    public void createPlugin() {
        try {
            XmlElement editors = getPluginEditors();
            String[] keys = new String[editors.getElementCount()];
            Hashtable editorsMap = new Hashtable();
            for (int loop = 0; loop < editors.getElementCount(); loop++) {
                XmlElement editor = editors.getElementByIndex(loop);
                keys[loop] = Class.forName(editor.getAttributeValueByName("className")).getName();
                editorsMap.put(keys[loop], editor);
            }
            String pluginClassName = XappRootApplication.askForSelection("New Plugin type", "Select a Plugin type", keys, keys[0]);
            if (pluginClassName == null) {
                return;
            }
            XmlElement editor = ((XmlElement) editorsMap.get(pluginClassName));
            pluginClassName = editor.getAttributeValueByName("className");
            String pluginDefinitionFile = File.createTempFile("untitled", ".plugin").getAbsolutePath();
            XmlElement xDefinition = new XmlElement("plugin");
            xDefinition.addAttribute("class", pluginClassName);
            xDefinition.addAttribute("name", "untitled");
            XappPlugin plugin = (XappPlugin) Class.forName(pluginClassName).getConstructor(new Class[] { String.class, XmlElement.class }).newInstance(new Object[] { pluginDefinitionFile, xDefinition });
            if (editor == null) {
                XappRootApplication.displayMessage("No editor found for plugin of type " + plugin.getClass().getName());
                return;
            }
            String editorClassName = editor.getValue();
            String editorFormsName = editor.getAttributeValueByName("formsName");
            XmlPathSearch search = new XmlPathSearch(XmlCloner.getClone(getDefinition()));
            XmlElement forms = search.findElement("plugin/forms[@name='" + editorFormsName + "']");
            Class.forName(editorClassName).getConstructor(new Class[] { XmlElement.class, XappPlugin.class }).newInstance(new Object[] { forms, plugin });
        } catch (Exception e) {
            XappRootApplication.displayException(e);
        }
    }

    public XmlElement getEditor(String pluginName) {
        XappPlugin plugin = XappRootApplication.getPluginStore().getPlugin(pluginName);
        XmlElement editors = getPluginEditors();
        XmlPathSearch search = new XmlPathSearch(editors);
        return search.findElement("editors/editor[@className='" + plugin.getClass().getName() + "']");
    }

    public void editPlugin() {
        String[] plugins = XappRootApplication.getPluginStore().getPluginNames();
        Vector editablePlugins = new Vector();
        for (int loop = 0; loop < plugins.length; loop++) {
            if (getEditor(plugins[loop]) != null) {
                editablePlugins.addElement(plugins[loop]);
            }
        }
        plugins = StringArrayHelper.vectorToStringArray(editablePlugins);
        String pluginName = XappRootApplication.askForSelection("Plugin to edit", "Select a Plugin", plugins, plugins[0]);
        if (pluginName == null) {
            return;
        }
        XmlElement editor = getEditor(pluginName);
        if (editor == null) {
            XappRootApplication.displayMessage("No editor found for plugin " + pluginName);
            return;
        }
        String editorClassName = editor.getValue();
        String editorFormsName = editor.getAttributeValueByName("formsName");
        XmlPathSearch search = new XmlPathSearch(XmlCloner.getClone(getDefinition()));
        XmlElement forms = search.findElement("plugin/forms[@name='" + editorFormsName + "']");
        XappPlugin plugin = XappRootApplication.getPluginStore().getPlugin(pluginName);
        try {
            Class.forName(editorClassName).getConstructor(new Class[] { XmlElement.class, XappPlugin.class }).newInstance(new Object[] { forms, plugin });
        } catch (Exception e) {
            XappRootApplication.displayException(e);
        }
    }

    public void removePlugin() {
        String[] plugins = XappRootApplication.getPluginStore().getPluginNames();
        String pluginName = XappRootApplication.askForSelection("Plugin to remove", "Select a Plugin", plugins, plugins[0]);
        if (pluginName == null) {
            return;
        }
        XappRootApplication.getPluginStore().removePlugin(pluginName);
    }

    public long receiveXappData(XmlElement data) {
        String file = data.getElementByName("file").getValue();
        addPlugin(file);
        return -1;
    }

    private XmlElement getPluginEditors() {
        XmlPathSearch search = new XmlPathSearch(XmlCloner.getClone(getDefinition()));
        XmlElement editors = search.findElements("plugin/editors/editor");
        String folder = new File(getPersistanceFile()).getParent();
        loadPluginEditors(editors, folder);
        editors.setName("editors");
        return editors;
    }

    private void loadPluginEditors(XmlElement editors, String folderName) {
        File folder = new File(folderName).getAbsoluteFile();
        if (!folder.isDirectory()) {
            return;
        }
        File[] list = folder.listFiles();
        for (int loop = 0; loop < list.length; loop++) {
            try {
                if (FileHelper.getFileExtension(list[loop].getName()).equals("plugin-editor")) {
                    editors.addChild(XmlLoader.load(list[loop].getAbsolutePath()));
                }
            } catch (Exception e) {
                XappRootApplication.displayException(e);
            }
        }
    }
}
