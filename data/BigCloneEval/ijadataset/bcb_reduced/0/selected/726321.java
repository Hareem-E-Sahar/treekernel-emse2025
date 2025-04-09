package org.kootox.episodesmanager.ui;

import org.kootox.episodesmanager.EpisodesManagerConfig;
import org.kootox.episodesmanager.EpisodesManagerContext;
import jaxx.runtime.swing.ErrorDialogUI;
import jaxx.runtime.JAXXContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.nuiton.i18n.I18n._;
import static org.nuiton.i18n.I18n.n_;
import java.awt.Desktop;
import java.net.URL;
import java.util.Locale;
import javax.swing.JPanel;
import jaxx.runtime.context.DefaultApplicationContext;
import jaxx.runtime.context.DefaultApplicationContext.AutoLoad;
import jaxx.runtime.context.JAXXInitialContext;
import jaxx.runtime.decorator.DecoratorProvider;
import jaxx.runtime.swing.AboutPanel;
import jaxx.runtime.swing.editor.config.ConfigUI;
import jaxx.runtime.swing.editor.config.ConfigUIBuilder;
import jaxx.runtime.swing.editor.config.model.ConfigUIModel;
import jaxx.runtime.swing.renderer.DecoratorProviderListCellRenderer;
import org.kootox.episodesmanager.content.EpisodesListHandler;
import org.kootox.episodesmanager.ui.admin.SearchResultHandler;
import org.kootox.episodesmanager.ui.admin.SearchTVRage;
import org.kootox.episodesmanager.ui.admin.SearchTVRageResults;
import org.kootox.episodesmanager.ui.admin.SeriesNavigationTree;
import org.kootox.episodesmanager.ui.admin.content.ContentUIHandler;
import org.nuiton.i18n.I18n;

/**
 * Le handler de l'ui principale.
 *
 * @author kootox
 * @see EpisodesManagerMainUI
 */
@AutoLoad
public class EpisodesManagerMainUIHandler {

    /** to use log facility, just put in your code: log.info(\"...\"); */
    private static Log log = LogFactory.getLog(EpisodesManagerMainUIHandler.class);

    DefaultApplicationContext rootContext;

    EpisodesListHandler episodesListHandler = new EpisodesListHandler();

    /**
     * Methode pour initialiser l'ui principale sans l'afficher.
     *
     * @param rootContext le context applicatif
     * @param fullscreen  flag pour indiquer si on doit ouvrir l'ui en model console (pleine ecran).
     * @return l'ui instancie et initialisee mais non visible encore
     */
    public EpisodesManagerMainUI initUI(DefaultApplicationContext rootContext, boolean fullscreen) {
        this.rootContext = rootContext;
        JAXXInitialContext context = new JAXXInitialContext();
        context.add(rootContext);
        DecoratorProvider decoratorProvider = rootContext.getContextValue(DecoratorProvider.class);
        context.add(decoratorProvider);
        context.add(new DecoratorProviderListCellRenderer(decoratorProvider));
        final EpisodesManagerMainUI ui = new EpisodesManagerMainUI(context);
        EpisodesManagerContext.MAIN_UI_ENTRY_DEF.setContextValue(rootContext, ui);
        ErrorDialogUI.init(ui);
        ui.getGraphicsConfiguration().getDevice().setFullScreenWindow(fullscreen ? ui : null);
        context.add(episodesListHandler);
        changeContent(context, episodesListHandler.initUI(rootContext, this));
        return ui;
    }

    public void changeContent(JAXXContext context, JPanel content) {
        EpisodesManagerMainUI ui = getUI(context);
        ui.setContentPane(content);
        ui.setVisible(true);
    }

    public void changeLanguage(EpisodesManagerMainUI mainUI, Locale newLocale) {
        EpisodesManagerConfig config = mainUI.getConfig();
        config.setLocale(newLocale);
        I18n.init(newLocale);
        reloadUI(EpisodesManagerContext.get(), config.isFullScreen());
    }

    /**
     * Ferme l'application.
     *
     * @param ui l'ui principale de l'application
     */
    public void close(EpisodesManagerMainUI ui) {
        log.info("Episodes Manager quitting...");
        boolean canContinue = ensureModification(ui);
        if (!canContinue) {
            return;
        }
        try {
            ui.dispose();
        } finally {
            System.exit(0);
        }
    }

    /**
     * Méthode pour changer de mode d'affichage.
     * <p/>
     * Si <code>fullscreen</code> est à <code>true</code> alors on passe en
     * mode console (c'est à dire en mode plein écran exclusif), sinon on
     * passe en mode fenetré normal.
     *
     * @param ui         l'ui principale de l'application
     * @param fullscreen le nouvel état requis.
     */
    public void changeScreen(EpisodesManagerMainUI ui, final boolean fullscreen) {
        boolean canContinue = ensureModification(ui);
        if (!canContinue) {
            return;
        }
        ui.getConfig().setFullscreen(fullscreen);
        reloadUI(EpisodesManagerContext.get(), fullscreen);
    }

    public void showConfig(JAXXContext context) {
        EpisodesManagerMainUI ui = getUI(context);
        EpisodesManagerConfig config = context.getContextValue(EpisodesManagerConfig.class);
        ConfigUIModel model = new ConfigUIModel(config);
        model.addCategory(n_("episodesmanager.config.category.directories"), n_("episodesmanager.config.category.directories.description"), EpisodesManagerConfig.Option.CONFIG_FILE, EpisodesManagerConfig.Option.BACKUP_DIRECTORY, EpisodesManagerConfig.Option.TMP_DIRECTORY);
        model.addCategory(n_("episodesmanager.config.category.other"), n_("episodesmanager.config.category.other.description"), EpisodesManagerConfig.Option.FULL_SCREEN, EpisodesManagerConfig.Option.LOCALE);
        ConfigUI configUI = ConfigUIBuilder.newConfigUI(context, model, "episodesmanager.config.category.directories");
        ConfigUIBuilder.showConfigUI(configUI, ui, false);
    }

    public void showHelp(JAXXContext context, String helpId) {
    }

    public void closeHelp(JAXXContext context) {
    }

    public void gotoSite(JAXXContext rootContext) {
        EpisodesManagerConfig config = rootContext.getContextValue(EpisodesManagerConfig.class);
        URL siteURL = config.getOptionAsURL("application.site.url");
        log.info("goto " + siteURL);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(siteURL.toURI());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                ErrorDialogUI.showError(ex);
            }
        }
    }

    public void showAbout(EpisodesManagerMainUI ui) {
        AboutPanel about = new AboutPanel() {

            private static final long serialVersionUID = 1L;
        };
        about.setTitle(_("episodesmanager.title.about"));
        about.setAboutText(_("episodesmanager.about.message"));
        about.setBottomText(ui.getConfig().getCopyrightText());
        about.setLicenseFile("META-INF/episodesmanager-swing-LICENSE.txt");
        about.setThirdpartyFile("META-INF/episodesmanager-swing-THIRD-PARTY.txt");
        about.init();
        about.showInDialog(ui, true);
    }

    /**
     * Permet de recharger l'ui principale et de changer de le mode d'affichage.
     *
     * @param rootContext le contexte applicatif
     * @param fullscreen  le type de fenetre à reouvrir
     */
    protected void reloadUI(EpisodesManagerContext rootContext, boolean fullscreen) {
        rootContext.getContextValue(EpisodesManagerConfig.class).removeJaxxPropertyChangeListener();
        EpisodesManagerMainUI ui = getUI(rootContext);
        if (ui != null) {
            ErrorDialogUI.init(null);
            EpisodesManagerContext.MAIN_UI_ENTRY_DEF.removeContextValue(rootContext);
            ui.dispose();
            ui.setVisible(false);
        }
        ui = initUI(rootContext, fullscreen);
        ui.setVisible(true);
    }

    /**
     * Test if there is some modification on screen,
     *
     * @param rootContext the context
     * @return <code>true</code> if no more modification is detected
     * @throws IllegalArgumentException if rootContext is null
     */
    protected boolean ensureModification(JAXXContext rootContext) throws IllegalArgumentException {
        if (rootContext == null) {
            throw new IllegalArgumentException("rootContext can not be null");
        }
        EpisodesManagerMainUI ui = getUI(rootContext);
        if (ui == null) {
            return true;
        }
        return true;
    }

    EpisodesManagerMainUI getUI(JAXXContext context) {
        if (context instanceof EpisodesManagerMainUI) {
            return (EpisodesManagerMainUI) context;
        }
        EpisodesManagerMainUI ui = EpisodesManagerContext.MAIN_UI_ENTRY_DEF.getContextValue(context);
        return ui;
    }

    public void showAdminSerie(JAXXContext context) {
        openAdminPopup(context, 0);
    }

    protected void openAdminPopup(JAXXContext context, int i) {
        SeriesNavigationTree ui = EpisodesManagerContext.ADMIN_UI_ENTRY_DEF.getContextValue(context);
        if (ui == null) {
            context.setContextValue(new ContentUIHandler());
            ui = new SeriesNavigationTree(context);
            EpisodesManagerContext.ADMIN_UI_ENTRY_DEF.setContextValue(context, ui);
        }
        ui.setVisible(true);
    }

    public void search(JAXXContext context) {
        search(context, 0);
    }

    protected void search(JAXXContext context, int i) {
        SearchTVRage ui = EpisodesManagerContext.SEARCH_UI_ENTRY_DEF.getContextValue(rootContext);
        if (ui == null) {
            rootContext.setContextValue(new ContentUIHandler());
            ui = new SearchTVRage(rootContext);
            EpisodesManagerContext.SEARCH_UI_ENTRY_DEF.setContextValue(rootContext, ui);
        }
        ui.setVisible(true);
    }

    public void searchResults(JAXXContext context, String search) {
        searchResults(context, 0, search);
    }

    protected void searchResults(JAXXContext context, int i, String search) {
        SearchResultHandler handler = new SearchResultHandler(search);
        handler.initUI(rootContext, this);
        rootContext.setContextValue(handler);
        SearchTVRageResults ui = new SearchTVRageResults(rootContext);
        EpisodesManagerContext.SEARCHRESULTS_UI_ENTRY_DEF.setContextValue(rootContext, ui);
        ui.setVisible(true);
    }
}
