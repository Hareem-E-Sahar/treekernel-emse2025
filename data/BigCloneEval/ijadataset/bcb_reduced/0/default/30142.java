import java.io.*;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import java.util.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.OptionsDialog;
import org.gjt.sp.util.Log;

/**
 * A jEdit plugin for adding a templating function.
 */
public class TemplatesPlugin extends EditPlugin {

    private static String defaultTemplateDir;

    private static String sepChar;

    private static TemplateDir templates = null;

    /**
	 * Returns the root TemplateDir object, which represents templates as a  
	 * hierarchical tree of TemplateDir and TemplateFile objects.
	 * @return The current TemplateDir object.
	 */
    public static TemplateDir getTemplates() {
        return templates;
    }

    /**
	 * Sets the root TemplateDir object to another value.
	 * @param newTemplates The new TemplateDir object
	 */
    public static void setTemplates(TemplateDir newTemplates) {
        templates = newTemplates;
    }

    /**
	 * Returns the directory where templates are stored
	 * @return A string containing the template directory path.
	 */
    public static String getTemplateDir() {
        String templateDir = jEdit.getProperty("plugin.TemplatesPlugin.templateDir.0", "");
        if (templateDir.equals("")) {
            templateDir = defaultTemplateDir;
        }
        return templateDir;
    }

    /** 
	 * Change the directory where templates are stored
	 * @param templateDirVal The new templates directory
	 */
    public static void setTemplateDir(String templateDirVal) {
        if (!templateDirVal.endsWith(sepChar)) {
            templateDirVal = templateDirVal + sepChar;
        }
        if (defaultTemplateDir.equals(templateDirVal)) {
            jEdit.unsetProperty("plugin.TemplatesPlugin.templateDir.0");
        } else {
            jEdit.setProperty("plugin.TemplatesPlugin.templateDir.0", templateDirVal);
        }
        templates = new TemplateDir(new File(templateDirVal));
        TemplatesPlugin.refreshTemplates();
    }

    /**
	 * Initializes the TemplatesAction and registers it with jEdit.
	 */
    public void start() {
        sepChar = System.getProperty("file.separator");
        defaultTemplateDir = jEdit.getSettingsDirectory() + sepChar + "templates" + sepChar;
        templates = new TemplateDir(new File(this.getTemplateDir()));
        this.refreshTemplates();
    }

    /**
	 * Not used.
	 */
    public void stop() {
    }

    /**
	 * Create the "Templates" menu item.
	 * @param menuItems Used to add menus and menu items
	 */
    public void createMenuItems(Vector menuItems) {
        TemplatesMenu templatesMenu = new TemplatesMenu();
        menuItems.addElement(templatesMenu);
        templatesMenu.addNotify();
    }

    /**
	 * Create the plugins option pane
	 * @param optionsDialog The dialog in which the OptionPane is to be displayed.
	 */
    public void createOptionPanes(OptionsDialog optionsDialog) {
        optionsDialog.addOptionPane(new TemplatesOptionPane());
    }

    /**
	 * Scans the templates directory and sends an EditBus message to all 
	 * TemplatesMenu objects to update themselves. Backup files are ignored
	 * based on the values of the backup prefix and suffix in the "Global
	 * Options" settings.
	 */
    public static void refreshTemplates() {
        String templateDirStr = getTemplateDir();
        File templateDir = new File(templateDirStr);
        try {
            if (!templateDir.exists()) {
                Log.log(Log.DEBUG, jEdit.getPlugin(jEdit.getProperty("plugin.TemplatesPlugin.name")), "Attempting to create templates directory: " + templateDirStr);
                templateDir.mkdir();
                if (!templateDir.exists()) throw new java.lang.SecurityException();
            }
            setTemplates(new TemplateDir(templateDir));
            getTemplates().refreshTemplates();
            buildAllMenus();
        } catch (java.lang.SecurityException se) {
            Log.log(Log.ERROR, jEdit.getPlugin(jEdit.getProperty("plugin.TemplatesPlugin.name")), jEdit.getProperty("plugin.TemplatesPlugin.error.create-dir") + templateDir);
        }
    }

    private static void buildAllMenus() {
        EditBus.send(new TemplatesChanged());
    }

    /**
	 * Prompt the user for a template file and load it into the view from
	 * which the request was initiated.
	 * @param view The view from which the "Edit Template" request was made.
	 */
    public static void editTemplate(View view) {
        JFileChooser chooser = new JFileChooser(jEdit.getProperty("plugin.TemplatesPlugin.templateDir.0", "."));
        int retVal = chooser.showOpenDialog(view);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                try {
                    jEdit.openFile(view, file.getCanonicalPath());
                } catch (IOException e) {
                }
            }
        }
    }

    /**
	 * Save the current buffer as a template. The file chooser displayed
	 * uses the Templates directory as the default.
	 * @param view The view from which the "Save Template" request was made.
	 */
    public static void saveTemplate(View view) {
        JFileChooser chooser = new JFileChooser(jEdit.getProperty("plugin.TemplatesPlugin.templateDir.0", "."));
        int retVal = chooser.showSaveDialog(view);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                try {
                    view.getBuffer().save(view, file.getCanonicalPath());
                } catch (IOException e) {
                }
            }
        }
    }

    /**
	 * Process the template file indicated by the given path string, and
	 * insert its text into the given view.
	 * @param path The absolute path to the desired template file.
	 * @param view The view into which the template text is to be inserted.
	 */
    public static void processTemplate(String path, View view) {
        File templateFile = new File(path);
        Template template = new Template(templateFile);
        template.processTemplate(view);
    }
}
