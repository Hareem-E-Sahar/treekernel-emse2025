package com.mycila.testing.plugins.jetty.config;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.mycila.testing.plugins.jetty.JettyRunWar;
import com.mycila.testing.plugins.jetty.NopServerLifeCycleListener;
import com.mycila.testing.plugins.jetty.ServerLifeCycleListener;
import com.mycila.testing.plugins.jetty.locator.FileLocator;
import com.mycila.testing.plugins.jetty.locator.StrategyFileLocator;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Iterables.get;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;

/**
 * The default implementation with default values of {@link RawConfig} and provides {@link Config}uration extension.
 */
public class DefaultConfig implements Config {

    /**
     * Instantiates.
     */
    public DefaultConfig() {
    }

    /**
     * Instantiates with a {@link Properties} configuration such as keys :
     * <ul>
     * <li>{@code warLocation} : String # {@link #getWarLocation()}, ie : <i>ant:*.war</i></li>
     * <li>{@code serverPort} : int # {@link #getServerPort()}, ie : <i>9090</i></li>
     * <li>{@code contextPath} : String # {@link #getContextPath()}, ie : <i>/test</i></li>
     * <li>{@code startServer} : boolean (true|false) # {@link #isStartServer()}, ie : <i>true</i></li>
     * <li>{@code deployWebapp} : boolean (true|false) # {@link #isDeployWebapp()}, ie : <i>false</i></li>
     * <li>{@code skip} : boolean (true|false) # {@link #isSkip()}, ie : <i>true</i></li>
     * <li>{@code lifeCycleListenerClass} : String classname # {@link #getServerLifeCycleListenerClass()}, ie :
     * <i>com.mycila.testing.plugins.jetty.NopServerLifeCycleListener</i></li>
     * </ul>
     * 
     * @param config
     *            the {@link Properties} configuration.
     * 
     * @throws RuntimeException
     *             if the {@link #setServerLifeCycleListenerClass(Class)} does not exist in the VM.
     * @throws NumberFormatException
     *             if the {@link #setServerPort(int)} is not an int.
     */
    public DefaultConfig(final Properties config) {
        try {
            this.setWarLocation(config.getProperty("warLocation", DEFAULT_WAR_LOCATION));
            this.setServerPort(parseInt(config.getProperty("serverPort", DEFAULT_SERVER_PORT_AS_STRING)));
            this.setContextPath(config.getProperty("contextPath", DEFAULT_CONTEXT_PATH));
            this.setStartServer(parseBoolean(config.getProperty("startServer", DEFAULT_START_SERVER_AS_STRING)));
            this.setDeployWebapp(parseBoolean(config.getProperty("deployWebapp", DEFAULT_DEPLOY_WEBAPP_AS_STRING)));
            this.setSkip(parseBoolean(config.getProperty("skip", DEFAULT_SKIP_AS_STRING)));
            this.setServerLifeCycleListenerClass((Class<? extends ServerLifeCycleListener>) Class.forName(config.getProperty("lifeCycleListenerClass", DEFAULT_CYCLE_LISTENER_CLASS_AS_STRING)));
        } catch (final ClassNotFoundException e) {
            throw propagate(e);
        } catch (final NumberFormatException e) {
            throw propagate(e);
        }
    }

    /**
     * Instantiates with a {@link RawConfig}uration;
     * 
     * @param config
     *            the raw configuration;
     */
    public DefaultConfig(final RawConfig config) {
        this.setWarLocation(config.getWarLocation());
        this.setContextPath(config.getContextPath());
        this.setServerPort(config.getServerPort());
        this.setStartServer(config.isStartServer());
        this.setDeployWebapp(config.isDeployWebapp());
        this.setSkip(config.isSkip());
        this.setServerLifeCycleListenerClass(config.getServerLifeCycleListenerClass());
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mycila.testing.plugins.jetty.config.Config#getWarLocationUrl()
     */
    public URL getWarLocationUrl() {
        try {
            final FileLocator fileLocator = new StrategyFileLocator();
            final File file = get(fileLocator.locate(this.warLocation), 0);
            final URL url = file.toURI().toURL();
            return url;
        } catch (final FileNotFoundException e) {
            throw propagate(e);
        } catch (final MalformedURLException e) {
            throw propagate(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mycila.testing.plugins.jetty.config.RawConfig#getWarLocation()
     */
    public String getWarLocation() {
        return this.warLocation;
    }

    /**
     * Sets the location of the WAR file to load.
     * 
     * @param warLocation
     *            the location of the WAR file to load.
     * 
     * @see #getWarLocation()
     */
    void setWarLocation(final String warLocation) {
        this.warLocation = warLocation;
    }

    /**
     * Returns true if {@link #getWarLocation()} value is default one, false else.
     * 
     * @return true if {@link #getWarLocation()} value is default one, false else.
     */
    boolean isDefaultWarLocation() {
        return DEFAULT_WAR_LOCATION.equals(this.warLocation);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mycila.testing.plugins.jetty.config.RawConfig#getServerPort()
     */
    public int getServerPort() {
        return this.serverPort;
    }

    /**
     * Sets the web application server port.
     * 
     * @param serverPort
     *            the web application server port.
     */
    void setServerPort(final int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Returns true if {@link #getServerPort()} value is default one, false else.
     * 
     * @return true if {@link #getServerPort()} value is default one, false else.
     */
    boolean isDefaultServerPort() {
        return DEFAULT_SERVER_PORT == this.serverPort;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mycila.testing.plugins.jetty.config.RawConfig#getContextPath()
     */
    public String getContextPath() {
        return this.contextPath;
    }

    /**
     * Sets web application context path.
     * 
     * @param contextPath
     *            the web application context path.
     */
    void setContextPath(final String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Returns true if {@link #getContextPath()} value is default one, false else.
     * 
     * @return true if {@link #getContextPath()} value is default one, false else.
     */
    boolean isDefaultContextPath() {
        return DEFAULT_CONTEXT_PATH.equals(this.contextPath);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mycila.testing.plugins.jetty.config.RawConfig#isStartServer()
     */
    public boolean isStartServer() {
        return this.startServer;
    }

    /**
     * Sets true to start a new server (and stop the old one), false to start a server only if there is no running one.
     * 
     * @param startServer
     *            true to start a new server (and stop the old one), false to start a server only if there is no running
     *            one.
     */
    void setStartServer(final boolean startServer) {
        this.startServer = startServer;
    }

    /**
     * Returns true if {@link #isStartServer()} value is default one, false else.
     * 
     * @return true if {@link #isStartServer()} value is default one, false else.
     */
    boolean isDefaultStartServer() {
        return DEFAULT_START_SERVER == this.startServer;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mycila.testing.plugins.jetty.config.RawConfig#isDeployWebapp()
     */
    public boolean isDeployWebapp() {
        return this.deployWebapp;
    }

    /**
     * Sets true to deploy a new webapp (and undeploy the old one), false to deploy a webapp only if there is no
     * deployed one.
     * 
     * @param deployWebapp
     *            true to deploy a new webapp (and undeploy the old one), false to deploy a webapp only if there is no
     *            deployed one.
     */
    void setDeployWebapp(final boolean deployWebapp) {
        this.deployWebapp = deployWebapp;
    }

    /**
     * Returns true if {@link #isDeployWebapp()} value is default one, false else.
     * 
     * @return true if {@link #isDeployWebapp()} value is default one, false else.
     */
    boolean isDefaultDeployWebapp() {
        return DEFAULT_DEPLOY_WEBAPP == this.deployWebapp;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mycila.testing.plugins.jetty.config.RawConfig#isSkip()
     */
    public boolean isSkip() {
        return this.skip;
    }

    /**
     * Sets true to skip starting server or deploying webapp.
     * 
     * @param skip
     *            true to skip starting server or deploying webapp.
     */
    void setSkip(final boolean skip) {
        this.skip = skip;
    }

    /**
     * Returns true if {@link #isSkip()} value is default one, false else.
     * 
     * @return true if {@link #isSkip()} value is default one, false else.
     */
    boolean isDefaultSkip() {
        return DEFAULT_SKIP == this.skip;
    }

    public ServerLifeCycleListener getServerLifeCycleListener(final Map context) {
        try {
            final Constructor[] constructors = this.serverLifeCycleListenerClass.getConstructors();
            final ServerLifeCycleListener listener;
            if (constructors.length == 1) {
                listener = this.serverLifeCycleListenerClass.getConstructor().newInstance();
            } else {
                final Constructor<? extends ServerLifeCycleListener> constructor = this.serverLifeCycleListenerClass.getConstructor(Map.class);
                listener = constructor.newInstance(context);
            }
            return listener;
        } catch (final InstantiationException e) {
            throw propagate(e);
        } catch (final IllegalAccessException e) {
            throw propagate(e);
        } catch (final NoSuchMethodException e) {
            throw propagate(e);
        } catch (InvocationTargetException e) {
            throw propagate(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mycila.testing.plugins.jetty.config.RawConfig#getServerLifeCycleListenerClass()
     */
    public Class<? extends ServerLifeCycleListener> getServerLifeCycleListenerClass() {
        return this.serverLifeCycleListenerClass;
    }

    /**
     * Sets the ServerLifeCycleListener class which allow customization of the server configuration.
     * 
     * @param serverLifeCycleListenerClass
     *            the ServerLifeCycleListener class which allow customization of the server configuration.
     */
    void setServerLifeCycleListenerClass(final Class<? extends ServerLifeCycleListener> serverLifeCycleListenerClass) {
        this.serverLifeCycleListenerClass = serverLifeCycleListenerClass;
    }

    /**
     * Returns true if {@link #getServerLifeCycleListenerClass()} value is default one, false else.
     * 
     * @return true if {@link #getServerLifeCycleListenerClass()} value is default one, false else.
     */
    boolean isDefaultServerLifeCycleListenerClass() {
        return DEFAULT_CYCLE_LISTENER_CLASS.equals(this.serverLifeCycleListenerClass);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mycila.testing.plugins.jetty.config.Config#getSourceConfig()
     */
    public JettyRunWar getSourceConfig() {
        return this.sourceConfig;
    }

    /**
     * Sets the {@link JettyRunWar} source configuration.
     * 
     * @param sourceConfig
     *            the {@link JettyRunWar} source configuration.
     */
    void setSourceConfig(final JettyRunWar sourceConfig) {
        this.sourceConfig = sourceConfig;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mycila.testing.plugins.jetty.config.Config#getSourceMethod()
     */
    public Method getSourceMethod() {
        return this.sourceMethod;
    }

    /**
     * Sets the {@link JettyRunWar} source method.
     * 
     * @param sourceMethod
     *            the {@link JettyRunWar} source method.
     */
    void setSourceMethod(final Method sourceMethod) {
        this.sourceMethod = sourceMethod;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.mycila.testing.plugins.jetty.config.Config#getSourceClass()
     */
    public Class<?> getSourceClass() {
        return this.sourceClass;
    }

    /**
     * Sets the {@link JettyRunWar} source class.
     * 
     * @param sourceClass
     *            the {@link JettyRunWar} source class.
     */
    void setSourceClass(final Class<?> sourceClass) {
        this.sourceClass = sourceClass;
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final ToStringHelper toString = Objects.toStringHelper(this);
        toString.add("warLocation", this.getWarLocation());
        toString.add("serverPort", Integer.toString(this.getServerPort()));
        toString.add("contextPath", this.getContextPath());
        toString.add("doStartServer", Boolean.toString(this.isStartServer()));
        toString.add("doDeployWebapp", Boolean.toString(this.isDeployWebapp()));
        toString.add("skip", Boolean.toString(this.isSkip()));
        toString.add("serverLifeCycleListenerClass", this.getServerLifeCycleListenerClass());
        return toString.toString();
    }

    /**
     * Returns true if the {@code method} is annotation with {@link JettyRunWar}, false else.
     * 
     * @param method
     *            the method to test if annotated with {@link JettyRunWar}.
     * 
     * @return true if the {@code method} is annotation with {@link JettyRunWar}, false else.
     */
    public static boolean hasJettyPlugin(final Method method) {
        final boolean hasMethodAnno = method.isAnnotationPresent(JettyRunWar.class);
        final boolean hasClassAnno = hasJettyPlugin(method.getDeclaringClass());
        final boolean hasPlugin = (hasMethodAnno && hasClassAnno) || hasClassAnno;
        return hasPlugin;
    }

    /**
     * Returns true if the {@code klass} is annotation with {@link JettyRunWar}, false else.
     * 
     * @param klass
     *            the class to test if annotated with {@link JettyRunWar}.
     * 
     * @return true if the {@code klass} is annotation with {@link JettyRunWar}, false else.
     */
    public static boolean hasJettyPlugin(final Class<?> klass) {
        final boolean hasClassAnno = klass.isAnnotationPresent(JettyRunWar.class);
        return hasClassAnno;
    }

    /**
     * Returns the {@link JettyRunWar} {@link Config}uration for the {@code method}.
     * 
     * @param method
     *            the method for which returns the {@link JettyRunWar} {@link Config}uration.
     * 
     * @return the {@link JettyRunWar} {@link Config}uration.
     */
    public static Config configFrom(final Method method) {
        if (!hasJettyPlugin(method)) {
            throw new IllegalArgumentException("at least " + method.getDeclaringClass() + " should be annotated with " + JettyRunWar.class);
        }
        final JettyRunWar methodAnno = method.getAnnotation(JettyRunWar.class);
        final boolean isMethodDefaultConfig = DefaultConfig.DEFAULT_CONFIG_CLASS.equals((methodAnno == null) ? null : methodAnno.value());
        final JettyRunWar classAnno = method.getDeclaringClass().getAnnotation(JettyRunWar.class);
        final boolean isClassDefaultConfig = DefaultConfig.DEFAULT_CONFIG_CLASS.equals((classAnno == null) ? null : classAnno.value());
        if (isClassDefaultConfig && isMethodDefaultConfig) {
            throw new IllegalArgumentException(method + " should not be annotated with " + JettyRunWar.class + " because it has the same configuration has its class " + method.getDeclaringClass());
        }
        if (!isClassDefaultConfig && isMethodDefaultConfig) {
            throw new IllegalArgumentException(method + " must be annotated with " + JettyRunWar.class + " and has a custom configuration because its class " + method.getDeclaringClass() + " has a custom configuration");
        }
        final JettyRunWar sourceConfig = firstNonNull(methodAnno, classAnno);
        return configFrom(sourceConfig, method, method.getDeclaringClass());
    }

    /**
     * Returns the {@link JettyRunWar} {@link Config}uration for the {@code klass}.
     * 
     * @param klass
     *            the class for which returns the {@link JettyRunWar} {@link Config}uration.
     * 
     * @return the {@link JettyRunWar} {@link Config}uration.
     */
    public static Config configFrom(final Class<?> klass) {
        if (!hasJettyPlugin(klass)) {
            throw new IllegalArgumentException("at least " + klass + " should be annotated with " + JettyRunWar.class);
        }
        final JettyRunWar classAnno = klass.getAnnotation(JettyRunWar.class);
        return configFrom(classAnno, null, klass);
    }

    private static DefaultConfig configFrom(final JettyRunWar sourceConfig, final Method method, final Class<?> klass) {
        try {
            final RawConfig rawConfig = sourceConfig.value().newInstance();
            final DefaultConfig config = new DefaultConfig(rawConfig);
            config.setSourceConfig(sourceConfig);
            config.setSourceMethod(method);
            config.setSourceClass(klass);
            return config;
        } catch (final InstantiationException e) {
            throw propagate(e);
        } catch (final IllegalAccessException e) {
            throw propagate(e);
        }
    }

    /**
     * The default {@link JettyRunWar#value()} {@link RawConfig}uration class.
     */
    public static final Class<? extends RawConfig> DEFAULT_CONFIG_CLASS = DefaultConfig.class;

    /**
     * The default value of {@link #getWarLocation()}.
     */
    public static final String DEFAULT_WAR_LOCATION = "reg:.*\\.war";

    /**
     * The default value of {@link #getServerPort()}.
     */
    public static final int DEFAULT_SERVER_PORT = 9090;

    /**
     * The default value of {@link #getServerPort()} as string.
     */
    public static final String DEFAULT_SERVER_PORT_AS_STRING = Integer.toString(DEFAULT_SERVER_PORT);

    /**
     * The default value of {@link #getContextPath()}.
     */
    public static final String DEFAULT_CONTEXT_PATH = "/its";

    /**
     * The default value of {@link #isStartServer()}.
     */
    public static final boolean DEFAULT_START_SERVER = true;

    /**
     * The default value of {@link #isStartServer()} as string.
     */
    public static final String DEFAULT_START_SERVER_AS_STRING = Boolean.toString(DEFAULT_START_SERVER);

    /**
     * The default value of {@link #isDeployWebapp()}.
     */
    public static final boolean DEFAULT_DEPLOY_WEBAPP = true;

    /**
     * The default value of {@link #isDeployWebapp()} as string.
     */
    public static final String DEFAULT_DEPLOY_WEBAPP_AS_STRING = Boolean.toString(DEFAULT_DEPLOY_WEBAPP);

    /**
     * The default value of {@link #isSkip()}.
     */
    public static final boolean DEFAULT_SKIP = false;

    /**
     * The default value of {@link #isSkip()} as string.
     */
    public static final String DEFAULT_SKIP_AS_STRING = Boolean.toString(DEFAULT_SKIP);

    /**
     * The default value of {@link #getServerLifeCycleListenerClass()}.
     */
    public static final Class<? extends ServerLifeCycleListener> DEFAULT_CYCLE_LISTENER_CLASS = NopServerLifeCycleListener.class;

    /**
     * The default value of {@link #getServerLifeCycleListenerClass()} as string.
     */
    public static final String DEFAULT_CYCLE_LISTENER_CLASS_AS_STRING = DEFAULT_CYCLE_LISTENER_CLASS.getName();

    private String warLocation = DEFAULT_WAR_LOCATION;

    private int serverPort = DEFAULT_SERVER_PORT;

    private String contextPath = DEFAULT_CONTEXT_PATH;

    private boolean startServer = DEFAULT_START_SERVER;

    private boolean deployWebapp = DEFAULT_DEPLOY_WEBAPP;

    private boolean skip = DEFAULT_SKIP;

    private Class<? extends ServerLifeCycleListener> serverLifeCycleListenerClass = DEFAULT_CYCLE_LISTENER_CLASS;

    private Class<?> sourceClass;

    private Method sourceMethod;

    private JettyRunWar sourceConfig;
}
