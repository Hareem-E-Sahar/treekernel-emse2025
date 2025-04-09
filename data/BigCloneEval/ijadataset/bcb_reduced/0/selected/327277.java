package net.mikro2nd.s;

import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Web application lifecycle listener.
 */
public class App implements ServletContextListener {

    public static final String PKG = App.class.getPackage().getName() + ".";

    public static final String CFG_URLMAP_CLASSNAME = PKG + "urlmap";

    public static final String CFG_SHRTNR_CLASS = PKG + "shortenStrategy";

    public static final String CFG_LOG_SYSTEM = PKG + "log.sys";

    public static final String CFG_LOG_STATS = PKG + "log.stats";

    public static final String CTX_MAP = PKG + "urlmap";

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Logger.getLogger(getLogName(sce.getServletContext())).log(Level.CONFIG, "Initialising context {0}", sce.getServletContext().getContextPath() != null ? sce.getServletContext().getContextPath() : "ROOT");
        final Properties configParams = getContextParamsAsProperties(sce.getServletContext());
        initUrlMap(sce.getServletContext(), configParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        final UrlMap map = (UrlMap) sce.getServletContext().getAttribute(CTX_MAP);
        if (map == null) {
            Logger.getLogger(getLogName(sce.getServletContext())).log(Level.SEVERE, "No UrlMap defined in context {0} during shutdown.", sce.getServletContext().getServletContextName());
            return;
        }
        map.shutdown();
        sce.getServletContext().removeAttribute(CTX_MAP);
    }

    private void initUrlMap(final ServletContext ctx, final Properties config) {
        final String urlMapClassname = config.getProperty(CFG_URLMAP_CLASSNAME);
        if (urlMapClassname == null) throw new RuntimeException();
        try {
            @SuppressWarnings("unchecked") Class<? extends UrlMap> mapClass = (Class<? extends UrlMap>) Class.forName(urlMapClassname);
            Constructor<? extends UrlMap> ctor = mapClass.getConstructor(Properties.class);
            UrlMap map = ctor.newInstance(config);
            ctx.setAttribute(CTX_MAP, map);
        } catch (Exception e) {
            Logger.getLogger(getLogName(ctx)).log(Level.SEVERE, "Exception instantiating UrlMap class {0} in context {1}: {2}", new Object[] { urlMapClassname, ctx.getServletContextName(), e });
            throw new RuntimeException(e);
        }
    }

    private static String getLogName(final ServletContext ctx) {
        return ctx.getInitParameter(CFG_LOG_SYSTEM);
    }

    /**
     * Collect all the context's initialisation paramters into a Properties instance,
     * making it easier to pass around params in a generic way.
     * @param ctx The ServletContext we're in.
     * @return A non-null Properties instance.
     */
    private Properties getContextParamsAsProperties(ServletContext ctx) {
        final Properties p = new Properties();
        @SuppressWarnings("unchecked") final Enumeration<String> paramNames = (Enumeration<String>) ctx.getInitParameterNames();
        while (paramNames.hasMoreElements()) {
            final String paramName = paramNames.nextElement();
            p.setProperty(paramName, ctx.getInitParameter(paramName));
        }
        return p;
    }
}
