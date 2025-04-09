package base.gui.action;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import base.exception.ExceptionHandler;
import base.gui.MenuHelper;
import base.resources.Resources;
import docBuilder.DocBuilderSettings;
import org.apache.log4j.Logger;
import rgzm.RgZmSettings;

/**
 * this action opens the help dialog for the user
 */
public class HelpAction extends AbstractAction implements UpdateAction, DocBuilderSettings {

    private static final Logger LOG = Logger.getLogger(HelpAction.class.getName());

    private static final String ACTION_NAME = "action.rgzm_help";

    private static final String DEFAULT_LOCALE = Locale.GERMAN.toString().toLowerCase();

    public HelpAction() {
        super();
        updateLocale();
        this.setEnabled(true);
    }

    public void actionPerformed(final ActionEvent e) {
        if (Desktop.isDesktopSupported()) {
            final String cy = RgZmSettings.getInstance().getLocale().getLanguage().toLowerCase();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Current locale: " + cy);
            }
            File docFile = new File(SAVE_TO_DIRECTORY, getIndexPage(cy) + ".html");
            if (!docFile.exists()) {
                docFile = new File(SAVE_TO_DIRECTORY, getIndexPage(DEFAULT_LOCALE) + ".html");
            }
            try {
                Desktop.getDesktop().browse(docFile.toURI());
            } catch (Throwable ex) {
                ExceptionHandler.ERRORHANDLER.handleException(ex);
            }
        }
    }

    public void updateLocale() {
        putValue(Action.NAME, Resources.getText(ACTION_NAME));
        putValue(Action.MNEMONIC_KEY, MenuHelper.getMnemonicFromString(Resources.getText(ACTION_NAME + ".mnemonic")));
    }

    private String getIndexPage(final String locale) {
        return WIKI_PAGES.get(new Locale(locale)).split("\\s*,\\s*")[0];
    }
}
