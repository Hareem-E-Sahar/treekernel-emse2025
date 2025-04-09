package de.schwarzrot.app.support;

import java.lang.reflect.Constructor;
import java.util.List;
import javax.swing.JFrame;
import org.springframework.context.support.AbstractApplicationContext;
import de.schwarzrot.app.MainEntry;
import de.schwarzrot.app.config.DesktopConfig;
import de.schwarzrot.app.errors.ApplicationException;
import de.schwarzrot.data.support.AbstractEntity;
import de.schwarzrot.data.transaction.TORead;
import de.schwarzrot.data.transaction.Transaction;
import de.schwarzrot.data.transaction.support.TransactionFactory;
import de.schwarzrot.ui.Desktop;
import de.schwarzrot.ui.util.LookAndFeelConfigurer;

/**
 * cut off the gui-part of application initialization from
 * {@code ApplicationLauncher}, so launching of application can be done for
 * background services too.
 * 
 * @author <a href="mailto:rmantey@users.sourceforge.net">Reinhard Mantey</a>
 */
public class DesktopApplicationLauncher extends AbstractApplicationLauncher<DesktopConfig> {

    /**
     * constructor - loads the given context's and validates configuration.
     * 
     * @param starter
     *            - the application startup class
     * @param appContextFiles
     *            - the list of context-files to setup application
     * @param appArgs
     *            - list of commandline parameters
     * 
     */
    public DesktopApplicationLauncher(MainEntry<DesktopConfig> starter, String[] appContextFiles, List<String> appArgs) {
        super(starter, appContextFiles, appArgs);
    }

    @Override
    public void start() {
        super.start();
        javax.swing.SwingUtilities.invokeLater(getStarter());
    }

    @Override
    protected AbstractApplicationContext init(String[] appContextFiles) {
        LookAndFeelConfigurer lafConfigurer = null;
        AbstractApplicationContext appContext = super.init(appContextFiles);
        if (appContext != null) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            if (appContext.containsBean("lookAndFeelConfigurer")) {
                getLogger().info("look and feel configurer bean in context found ...");
                Object tmp = appContext.getBean("lookAndFeelConfigurer");
                if (tmp != null && tmp instanceof LookAndFeelConfigurer) {
                    lafConfigurer = (LookAndFeelConfigurer) tmp;
                    lafConfigurer.apply();
                    ApplicationServiceProvider.registerService(LookAndFeelConfigurer.class, lafConfigurer);
                }
            }
            if (appContext.containsBean("appConfig")) {
                getLogger().warn("appConfig found ...");
                Object tmp = appContext.getBean("appConfig");
                if (tmp != null && tmp instanceof DesktopConfig) {
                    getLogger().warn("gonna read saved configuration and patch application setup");
                    getStarter().setConfig((DesktopConfig) tmp);
                    Desktop stdDesktop = ApplicationServiceProvider.getService(Desktop.class);
                    Desktop newDesktop = null;
                    TransactionFactory taFactory = ApplicationServiceProvider.getService(TransactionFactory.class);
                    TORead<DesktopConfig> tor = new TORead<DesktopConfig>(getStarter().getConfig());
                    Transaction ta = taFactory.createTransaction();
                    ta.add(tor);
                    ta.setRollbackOnly();
                    ta.execute();
                    if (tor.getResult() != null && tor.getResult().size() == 1) {
                        DesktopConfig cfg = tor.getResult().get(0);
                        if (tmp != cfg) getStarter().setConfig(cfg);
                    }
                    AbstractEntity.setSchemaName(getStarter().getConfig().getDsSchema());
                    if (getStarter().getConfig().getLafDesktop() != null) {
                        try {
                            Class<?> desktopClass = Thread.currentThread().getContextClassLoader().loadClass(getStarter().getConfig().getLafDesktop());
                            if (desktopClass != stdDesktop.getClass()) {
                                Constructor<?> constructor = desktopClass.getConstructor(new Class[] { String.class });
                                Object desktop = constructor.newInstance(new Object[] { stdDesktop.getName() });
                                newDesktop = (Desktop) desktop;
                                newDesktop.setupFrom(stdDesktop);
                                stdDesktop = null;
                                ApplicationServiceProvider.registerService(Desktop.class, newDesktop);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (lafConfigurer != null) {
                        lafConfigurer.setConfig(getStarter().getConfig());
                        lafConfigurer.apply();
                    }
                }
            }
        } else throw new ApplicationException("can't live without context!");
        return appContext;
    }
}
