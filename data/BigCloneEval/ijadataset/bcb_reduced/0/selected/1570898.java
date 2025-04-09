package pt.igeo.snig.mig.editor.ui.topMenu;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Locale;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.OceanTheme;
import pt.igeo.snig.mig.editor.constants.Constants;
import pt.igeo.snig.mig.editor.defaults.DefaultValuesManager;
import pt.igeo.snig.mig.editor.i18n.StringsManager;
import pt.igeo.snig.mig.editor.i18n.ToolTipStringsManager;
import pt.igeo.snig.mig.editor.record.Record;
import pt.igeo.snig.mig.editor.ui.topMenu.aboutDialog.AboutDialogController;
import pt.igeo.snig.mig.undoManager.UndoManager;
import fi.mmm.yhteinen.swing.core.YApplicationEvent;
import fi.mmm.yhteinen.swing.core.YController;

/**
 * The controller for top menu view and model.
 * 
 * @author Ant�nio Silva
 * @version $Revision: 11325 $
 * @since 1.0
 */
public class TopMenuController extends YController {

    /**
	 * The top menu view component
	 */
    private TopMenuView topMenuView = new TopMenuView();

    /**
	 * The top menu controller component
	 */
    private TopMenuModel topMenuModel = new TopMenuModel();

    /** the about dialog component */
    private AboutDialogController aboutDialogController = new AboutDialogController();

    /**
	 * Constructor for topmenu
	 * 
	 */
    public TopMenuController() {
        topMenuView.setShowToolbarName(topMenuModel.getShowToolbarName());
        topMenuView.setAutoUpdatesName(topMenuModel.getAutoUpdatesName());
        this.setUpMVC(topMenuModel, topMenuView);
        this.register(Constants.translateEvent);
        this.register(Constants.recordFieldsChanged);
        this.register(Constants.recordsSavedSuccess);
        this.register(Constants.recordChangedEvent);
        this.register(Constants.noMoreRecords);
        this.register(Constants.hasTransformation);
        this.register(Constants.noTransformationEvent);
        this.register(Constants.openBrowser);
        this.register(Constants.currentRecordSavedSuccess);
        this.register(Constants.htmlFocus);
        this.register(Constants.htmlOutOfFocus);
    }

    /**
	 * Handling of Global events.
	 * @param ev 
	 */
    public void receivedApplicationEvent(YApplicationEvent ev) {
        if (ev.getName() == Constants.translateEvent) {
            topMenuView.translate();
        } else if (ev.getName() == Constants.recordFieldsChanged) {
            topMenuView.enableSaveOption();
        } else if (ev.getName() == Constants.recordsSavedSuccess) {
            topMenuView.disableSaveOption();
        } else if (ev.getName() == Constants.recordChangedEvent) {
            topMenuView.enableRecordOptions();
            if (ev.getValue() != null) {
                Record record = (Record) ev.getValue();
                topMenuView.setSaveCurrentOptionState(!record.getSavedState());
            }
        } else if (ev.getName() == Constants.noMoreRecords) {
            topMenuView.disableRecordOptions();
            topMenuView.disableSaveOption();
        } else if (ev.getName() == Constants.hasTransformation) {
            topMenuView.enableHtmlExportOption();
        } else if (ev.getName() == Constants.noTransformationEvent) {
            topMenuView.disableHtmlExportOption();
        } else if (ev.getName() == Constants.openBrowser) {
            openBrowser((String) ev.getValue());
        } else if (ev.getName() == Constants.currentRecordSavedSuccess) {
            topMenuView.setSaveCurrentOptionState(false);
        } else if (ev.getName() == Constants.htmlFocus) {
            topMenuView.setExportXSLState(true);
        } else if (ev.getName() == Constants.htmlOutOfFocus) {
            topMenuView.setExportXSLState(false);
        }
    }

    /**
	 * Handling of Exit top menu pressed
	 */
    public void fileExitMISelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.fileExitMI));
    }

    /** change language to en */
    public void optionsLanguageEnMISelected() {
        if (!topMenuModel.getLanguage().equals("en")) {
            StringsManager.getInstance().configLocale(Locale.ROOT);
            ToolTipStringsManager.getInstance().configLocale(Locale.ROOT);
            DefaultValuesManager.getInstance().configLocale(Locale.ROOT);
            sendApplicationEvent(new YApplicationEvent(Constants.translateEvent));
            topMenuModel.setLanguage("en");
        }
    }

    /** change language to pt */
    public void optionsLanguagePtMISelected() {
        if (!topMenuModel.getLanguage().equals("pt")) {
            StringsManager.getInstance().configLocale(new Locale("pt"));
            ToolTipStringsManager.getInstance().configLocale(new Locale("pt"));
            DefaultValuesManager.getInstance().configLocale(new Locale("pt"));
            sendApplicationEvent(new YApplicationEvent(Constants.translateEvent));
            topMenuModel.setLanguage("pt");
        }
    }

    /** use the ocean theme */
    public void optionsUiOceanMISelected() {
        MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
            sendApplicationEvent(new YApplicationEvent(Constants.changeThemeEvent));
            topMenuModel.setVisual("ocean");
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    /** use the native OS theme */
    public void optionsUiNativeMISelected() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            sendApplicationEvent(new YApplicationEvent(Constants.changeThemeEvent));
            topMenuModel.setVisual("native");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** use the old metal theme (for your darkest pleasures) */
    public void optionsUiMetalMISelected() {
        MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
            sendApplicationEvent(new YApplicationEvent(Constants.changeThemeEvent));
            topMenuModel.setVisual("metal");
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    /** shows about dialog */
    public void helpAboutMISelected() {
        aboutDialogController.makeActive();
    }

    /** shows the HTML help on the browser */
    public void helpContentsMISelected() {
        YController.sendApplicationEvent(new YApplicationEvent(Constants.helpF1Pressed));
    }

    /** shows the HTML of profile */
    public void profileAboutMISelected() {
        openBrowser("http://snig.igeo.pt/Portal/docs/PerfilMIG_WebHelp/");
    }

    /**
	 * Open given file in external browser 
	 * @param string
	 */
    private void openBrowser(String string) {
        Desktop desktop;
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    URI url = null;
                    if (string.contains("http")) {
                        url = new URI(string);
                        System.out.println("[" + string + "]");
                    } else {
                        File index = new File(string);
                        String path = index.getAbsolutePath();
                        path = "file:///" + encodePath(path);
                        url = new URI(path);
                        System.out.println("[" + path + "]");
                    }
                    desktop.browse(url);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
	 * hide or show toolbar
	 */
    public void optionsViewToolbarSelected() {
        topMenuModel.invertShowToolbarState();
        topMenuView.setShowToolbarName(topMenuModel.getShowToolbarName());
        sendApplicationEvent(new YApplicationEvent(Constants.changeShowToolbar, topMenuModel.getShowToolbar()));
    }

    /**
	 * enable or disable auto updates
	 */
    public void autoUpdatesSelected() {
        topMenuModel.invertAutoUpdatesState();
        topMenuView.setAutoUpdatesName(topMenuModel.getAutoUpdatesName());
    }

    /**
	 * Save all records
	 */
    public void saveAllSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.saveRecords));
    }

    /**
	 * Save current record
	 */
    public void saveCurrentSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.saveCurrentRecord));
    }

    /**
	 * Revert current file
	 */
    public void revertCurrentSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.revertCurrentRecord));
    }

    /**
	 * create new record
	 */
    public void newRecordSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.addRTool));
    }

    /**
	 * duplicate current record
	 */
    public void duplicateRecordSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.duplicateRTool));
    }

    /**
	 * remove current record
	 */
    public void removeRecordSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.removeRTool));
    }

    /**
	 * import record
	 */
    public void importRecordSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.importRTool));
    }

    /**
	 * export record
	 */
    public void exportRecordSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.exportRTool));
    }

    /**
	 * view contact browser
	 */
    public void contactBrowserSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.showContactBrowser));
    }

    /**
	 * export to html selected
	 */
    public void exportRecordHtmlSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.exportToHTMLTool));
    }

    /**
	 * Open browser selected
	 */
    public void openBrowserSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.getUrlFromView));
    }

    /**
	 * Import xsl selected
	 */
    public void importXSLSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.importXSL));
    }

    /**
	 * GPT Code selected
	 */
    public void gptCodeSelected() {
        sendApplicationEvent(new YApplicationEvent(Constants.setGPTCode));
    }

    /**
	 * undo selected
	 */
    public void undoSelected() {
        UndoManager.getInstance().undo();
    }

    /**
	 * redo selected
	 */
    public void redoSelected() {
        UndoManager.getInstance().redo();
    }

    /**
	 * converts " " to %20, special chars to %hex
	 * while preserving / for directory paths
	 * @param path
	 * @return the encoded URL
	 * @throws UnsupportedEncodingException 
	 */
    private static String encodePath(String path) throws UnsupportedEncodingException {
        path = path.replace('\\', '/');
        String path2 = "";
        String[] parts = path.split("/");
        for (String part : parts) {
            path2 += URLEncoder.encode(part, "UTF-8") + "/";
        }
        path2 = path2.substring(0, path2.length() - 1);
        path2 = path2.replace("+", "%20");
        path2 = path2.replace("%3A", ":");
        return path2;
    }
}
