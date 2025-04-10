package org.granite.gravity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.ObjectName;
import javax.servlet.http.HttpSession;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.Destination;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.gravity.adapters.AdapterFactory;
import org.granite.gravity.adapters.ServiceAdapter;
import org.granite.gravity.security.GravityDestinationSecurizer;
import org.granite.gravity.security.GravityInvocationContext;
import org.granite.jmx.MBeanServerLocator;
import org.granite.jmx.OpenMBean;
import org.granite.logging.Logger;
import org.granite.messaging.amf.process.AMF3MessageInterceptor;
import org.granite.messaging.service.security.SecurityService;
import org.granite.messaging.service.security.SecurityServiceException;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.util.UUIDUtil;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

/**
 * @author William DRAI
 * @author Franck WOLFF
 */
public class DefaultGravity implements Gravity, DefaultGravityMBean {

    private static final Logger log = Logger.getLogger(Gravity.class);

    private final Map<String, Object> applicationMap = new HashMap<String, Object>();

    private final ConcurrentHashMap<String, TimeChannel> channels = new ConcurrentHashMap<String, TimeChannel>();

    private GravityConfig gravityConfig = null;

    private ServicesConfig servicesConfig = null;

    private GraniteConfig graniteConfig = null;

    private Channel serverChannel = null;

    private AdapterFactory adapterFactory = null;

    private GravityPool gravityPool = null;

    private Timer channelsTimer;

    private boolean started;

    public DefaultGravity(GravityConfig gravityConfig, ServicesConfig servicesConfig, GraniteConfig graniteConfig) {
        if (gravityConfig == null || servicesConfig == null || graniteConfig == null) throw new NullPointerException("All arguments must be non null.");
        this.gravityConfig = gravityConfig;
        this.servicesConfig = servicesConfig;
        this.graniteConfig = graniteConfig;
    }

    public GravityConfig getGravityConfig() {
        return gravityConfig;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public GraniteConfig getGraniteConfig() {
        return graniteConfig;
    }

    public boolean isStarted() {
        return started;
    }

    public ServiceAdapter getServiceAdapter(String messageType, String destinationId) {
        return adapterFactory.getServiceAdapter(messageType, destinationId);
    }

    public void start() throws Exception {
        log.info("Starting Gravity...");
        synchronized (this) {
            if (!started) {
                adapterFactory = new AdapterFactory(this);
                internalStart();
                serverChannel = new ServerChannel(this, gravityConfig, ServerChannel.class.getName());
                started = true;
            }
        }
        log.info("Gravity successfully started.");
    }

    protected void internalStart() {
        gravityPool = new GravityPool(gravityConfig);
        channelsTimer = new Timer();
        if (graniteConfig.isRegisterMBeans()) {
            try {
                ObjectName name = new ObjectName("org.granite:type=Gravity,context=" + graniteConfig.getMBeanContextName());
                log.info("Registering MBean: %s", name);
                OpenMBean mBean = OpenMBean.createMBean(this);
                MBeanServerLocator.getInstance().register(mBean, name, true);
            } catch (Exception e) {
                log.error(e, "Could not register Gravity MBean for context: %s", graniteConfig.getMBeanContextName());
            }
        }
    }

    public void restart() throws Exception {
        synchronized (this) {
            stop();
            start();
        }
    }

    public void reconfigure(GravityConfig gravityConfig, GraniteConfig graniteConfig) {
        this.gravityConfig = gravityConfig;
        this.graniteConfig = graniteConfig;
        if (gravityPool != null) gravityPool.reconfigure(gravityConfig);
    }

    public void stop() throws Exception {
        stop(true);
    }

    public void stop(boolean now) throws Exception {
        log.info("Starting Gravity (now=%s)...", now);
        synchronized (this) {
            if (adapterFactory != null) {
                try {
                    adapterFactory.stopAll();
                } catch (Exception e) {
                    log.error(e, "Error while stopping adapter factory");
                }
                adapterFactory = null;
            }
            if (serverChannel != null) {
                try {
                    removeChannel(serverChannel.getId());
                } catch (Exception e) {
                    log.error(e, "Error while removing server channel: %s", serverChannel);
                }
                serverChannel = null;
            }
            if (channelsTimer != null) {
                try {
                    channelsTimer.cancel();
                } catch (Exception e) {
                    log.error(e, "Error while cancelling channels timer");
                }
                channelsTimer = null;
            }
            if (gravityPool != null) {
                try {
                    if (now) gravityPool.shutdownNow(); else gravityPool.shutdown();
                } catch (Exception e) {
                    log.error(e, "Error while stopping thread pool");
                }
                gravityPool = null;
            }
            started = false;
        }
        log.info("Gravity sucessfully stopped.");
    }

    public String getGravityFactoryName() {
        return gravityConfig.getGravityFactory();
    }

    public String getChannelFactoryName() {
        if (gravityConfig.getChannelFactory() != null) return gravityConfig.getChannelFactory().getClass().getName();
        return null;
    }

    public long getChannelIdleTimeoutMillis() {
        return gravityConfig.getChannelIdleTimeoutMillis();
    }

    public void setChannelIdleTimeoutMillis(long channelIdleTimeoutMillis) {
        gravityConfig.setChannelIdleTimeoutMillis(channelIdleTimeoutMillis);
    }

    public boolean isRetryOnError() {
        return gravityConfig.isRetryOnError();
    }

    public void setRetryOnError(boolean retryOnError) {
        gravityConfig.setRetryOnError(retryOnError);
    }

    public long getLongPollingTimeoutMillis() {
        return gravityConfig.getLongPollingTimeoutMillis();
    }

    public void setLongPollingTimeoutMillis(long longPollingTimeoutMillis) {
        gravityConfig.setLongPollingTimeoutMillis(longPollingTimeoutMillis);
    }

    public int getMaxMessagesQueuedPerChannel() {
        return gravityConfig.getMaxMessagesQueuedPerChannel();
    }

    public void setMaxMessagesQueuedPerChannel(int maxMessagesQueuedPerChannel) {
        gravityConfig.setMaxMessagesQueuedPerChannel(maxMessagesQueuedPerChannel);
    }

    public long getReconnectIntervalMillis() {
        return gravityConfig.getReconnectIntervalMillis();
    }

    public int getReconnectMaxAttempts() {
        return gravityConfig.getReconnectMaxAttempts();
    }

    public int getCorePoolSize() {
        if (gravityPool != null) return gravityPool.getCorePoolSize();
        return gravityConfig.getCorePoolSize();
    }

    public void setCorePoolSize(int corePoolSize) {
        gravityConfig.setCorePoolSize(corePoolSize);
        if (gravityPool != null) gravityPool.setCorePoolSize(corePoolSize);
    }

    public long getKeepAliveTimeMillis() {
        if (gravityPool != null) return gravityPool.getKeepAliveTimeMillis();
        return gravityConfig.getKeepAliveTimeMillis();
    }

    public void setKeepAliveTimeMillis(long keepAliveTimeMillis) {
        gravityConfig.setKeepAliveTimeMillis(keepAliveTimeMillis);
        if (gravityPool != null) gravityPool.setKeepAliveTimeMillis(keepAliveTimeMillis);
    }

    public int getMaximumPoolSize() {
        if (gravityPool != null) return gravityPool.getMaximumPoolSize();
        return gravityConfig.getMaximumPoolSize();
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        gravityConfig.setMaximumPoolSize(maximumPoolSize);
        if (gravityPool != null) gravityPool.setMaximumPoolSize(maximumPoolSize);
    }

    public int getQueueCapacity() {
        if (gravityPool != null) return gravityPool.getQueueCapacity();
        return gravityConfig.getQueueCapacity();
    }

    public int getQueueRemainingCapacity() {
        if (gravityPool != null) return gravityPool.getQueueRemainingCapacity();
        return gravityConfig.getQueueCapacity();
    }

    public int getQueueSize() {
        if (gravityPool != null) return gravityPool.getQueueSize();
        return 0;
    }

    protected Channel createChannel() {
        Channel channel = gravityConfig.getChannelFactory().newChannel(UUIDUtil.randomUUID());
        TimeChannel timeChannel = new TimeChannel(channel);
        for (int i = 0; channels.putIfAbsent(channel.getId(), timeChannel) != null; i++) {
            if (i >= 10) throw new RuntimeException("Could not find random new clientId after 10 iterations");
            channel.destroy();
            channel = gravityConfig.getChannelFactory().newChannel(UUIDUtil.randomUUID());
            timeChannel = new TimeChannel(channel);
        }
        access(channel.getId());
        return channel;
    }

    public Channel getChannel(String channelId) {
        if (channelId == null) return null;
        TimeChannel timeChannel = channels.get(channelId);
        if (timeChannel != null) return timeChannel.getChannel();
        return null;
    }

    public Channel removeChannel(String channelId) {
        if (channelId == null) return null;
        TimeChannel timeChannel = channels.get(channelId);
        Channel channel = null;
        if (timeChannel != null) {
            try {
                if (timeChannel.getTimerTask() != null) timeChannel.getTimerTask().cancel();
            } catch (Exception e) {
            }
            channel = timeChannel.getChannel();
            try {
                for (Subscription subscription : channel.getSubscriptions()) {
                    try {
                        Message message = subscription.getUnsubscribeMessage();
                        handleMessage(message, true);
                    } catch (Exception e) {
                        log.error(e, "Error while unsubscribing channel: %s from subscription: %s", channel, subscription);
                    }
                }
            } finally {
                try {
                    channel.destroy();
                } finally {
                    channels.remove(channelId);
                }
            }
        }
        return channel;
    }

    public boolean access(String channelId) {
        if (channelId != null) {
            TimeChannel timeChannel = channels.get(channelId);
            if (timeChannel != null) {
                synchronized (timeChannel) {
                    TimerTask timerTask = timeChannel.getTimerTask();
                    if (timerTask != null) {
                        log.debug("Canceling TimerTask: %s", timerTask);
                        timerTask.cancel();
                        timeChannel.setTimerTask(null);
                    }
                    timerTask = new ChannelTimerTask(this, channelId);
                    timeChannel.setTimerTask(timerTask);
                    long timeout = gravityConfig.getChannelIdleTimeoutMillis();
                    log.debug("Scheduling TimerTask: %s for %s ms.", timerTask, timeout);
                    channelsTimer.schedule(timerTask, timeout);
                    return true;
                }
            }
        }
        return false;
    }

    public void execute(AsyncChannelRunner runner) {
        if (gravityPool == null) {
            runner.reset();
            throw new NullPointerException("Gravity not started or pool disabled");
        }
        gravityPool.execute(runner);
    }

    public boolean cancel(AsyncChannelRunner runner) {
        if (gravityPool == null) {
            runner.reset();
            throw new NullPointerException("Gravity not started or pool disabled");
        }
        return gravityPool.remove(runner);
    }

    public Message handleMessage(Message message) {
        return handleMessage(message, false);
    }

    public Message handleMessage(Message message, boolean skipInterceptor) {
        AMF3MessageInterceptor interceptor = null;
        if (!skipInterceptor) interceptor = GraniteContext.getCurrentInstance().getGraniteConfig().getAmf3MessageInterceptor();
        Message reply = null;
        try {
            if (interceptor != null) interceptor.before(message);
            if (message instanceof CommandMessage) {
                CommandMessage command = (CommandMessage) message;
                switch(command.getOperation()) {
                    case CommandMessage.LOGIN_OPERATION:
                    case CommandMessage.LOGOUT_OPERATION:
                        return handleSecurityMessage(command);
                    case CommandMessage.CLIENT_PING_OPERATION:
                        return handlePingMessage(command);
                    case CommandMessage.CONNECT_OPERATION:
                        return handleConnectMessage(command);
                    case CommandMessage.DISCONNECT_OPERATION:
                        return handleDisconnectMessage(command);
                    case CommandMessage.SUBSCRIBE_OPERATION:
                        return handleSubscribeMessage(command);
                    case CommandMessage.UNSUBSCRIBE_OPERATION:
                        return handleUnsubscribeMessage(command);
                    default:
                        throw new UnsupportedOperationException("Unsupported command operation: " + command);
                }
            }
            reply = handlePublishMessage((AsyncMessage) message);
        } finally {
            if (interceptor != null) interceptor.after(message, reply);
        }
        if (reply != null) {
            GraniteContext context = GraniteContext.getCurrentInstance();
            if (context instanceof HttpGraniteContext) {
                HttpSession session = ((HttpGraniteContext) context).getRequest().getSession(false);
                if (session != null) reply.setHeader("org.granite.sessionId", session.getId());
            }
        }
        return reply;
    }

    public GraniteContext initThread() {
        GraniteContext context = GraniteContext.getCurrentInstance();
        if (context == null) context = SimpleGraniteContext.createThreadIntance(graniteConfig, servicesConfig, applicationMap);
        return context;
    }

    public void releaseThread() {
        GraniteContext.release();
    }

    public Message publishMessage(AsyncMessage message) {
        return publishMessage(serverChannel, message);
    }

    public Message publishMessage(Channel fromChannel, AsyncMessage message) {
        initThread();
        return handlePublishMessage(message, fromChannel != null ? fromChannel : serverChannel);
    }

    private Message handlePingMessage(CommandMessage message) {
        Channel channel = createChannel();
        AsyncMessage reply = new AcknowledgeMessage(message);
        reply.setClientId(channel.getId());
        Map<String, Object> advice = new HashMap<String, Object>();
        advice.put(RECONNECT_INTERVAL_MS_KEY, Long.valueOf(gravityConfig.getReconnectIntervalMillis()));
        advice.put(RECONNECT_MAX_ATTEMPTS_KEY, Long.valueOf(gravityConfig.getReconnectMaxAttempts()));
        reply.setBody(advice);
        reply.setDestination(message.getDestination());
        log.debug("handshake.handle: reply=%s", reply);
        return reply;
    }

    private Message handleSecurityMessage(CommandMessage message) {
        GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
        Message response = null;
        if (!config.hasSecurityService()) log.warn("Ignored security operation (no security settings in granite-config.xml): %s", message); else {
            SecurityService securityService = config.getSecurityService();
            try {
                if (message.isLoginOperation()) securityService.login(message.getBody()); else securityService.logout();
            } catch (Exception e) {
                if (e instanceof SecurityServiceException) log.debug(e, "Could not process security operation: %s", message); else log.error(e, "Could not process security operation: %s", message);
                response = new ErrorMessage(message, e, true);
            }
        }
        if (response == null) {
            response = new AcknowledgeMessage(message, true);
            if (message.isSecurityOperation()) response.setBody("success");
        }
        return response;
    }

    private Message handleConnectMessage(CommandMessage message) {
        Channel client = getChannel((String) message.getClientId());
        if (client == null) return handleUnknownClientMessage(message);
        return null;
    }

    private Message handleDisconnectMessage(CommandMessage message) {
        Channel client = getChannel((String) message.getClientId());
        if (client == null) return handleUnknownClientMessage(message);
        removeChannel(client.getId());
        AcknowledgeMessage reply = new AcknowledgeMessage(message);
        reply.setDestination(message.getDestination());
        reply.setClientId(client.getId());
        return reply;
    }

    private Message handleSubscribeMessage(CommandMessage message) {
        GraniteContext context = GraniteContext.getCurrentInstance();
        Destination destination = context.getServicesConfig().findDestinationById(message.getMessageRefType(), message.getDestination());
        if (destination == null) return getInvalidDestinationError(message);
        GravityInvocationContext invocationContext = new GravityInvocationContext(message, destination);
        if (destination.getSecurizer() instanceof GravityDestinationSecurizer) {
            try {
                ((GravityDestinationSecurizer) destination.getSecurizer()).canSubscribe(invocationContext);
            } catch (Exception e) {
                return new ErrorMessage(message, e);
            }
        }
        GraniteConfig config = context.getGraniteConfig();
        if (config.hasSecurityService()) {
            try {
                config.getSecurityService().authorize(invocationContext);
            } catch (Exception e) {
                return new ErrorMessage(message, e);
            }
        }
        Channel channel = getChannel((String) message.getClientId());
        if (channel == null) return handleUnknownClientMessage(message);
        String subscriptionId = (String) message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
        if (subscriptionId == null) {
            subscriptionId = UUIDUtil.randomUUID();
            message.setHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER, subscriptionId);
        }
        HttpSession session = null;
        if (context instanceof HttpGraniteContext) session = ((HttpGraniteContext) context).getSession(false);
        if (session != null && Boolean.TRUE.toString().equals(destination.getProperties().get("session-selector"))) {
            String selector = (String) session.getAttribute("org.granite.gravity.selector." + destination.getId());
            log.debug("Session selector found in session %s: %s", session.getId(), selector);
            if (selector != null) message.setHeader(CommandMessage.SELECTOR_HEADER, selector);
        }
        ServiceAdapter adapter = adapterFactory.getServiceAdapter(message);
        AsyncMessage reply = (AsyncMessage) adapter.manage(channel, message);
        postManage(channel);
        reply.setDestination(message.getDestination());
        reply.setClientId(channel.getId());
        reply.getHeaders().putAll(message.getHeaders());
        if (session != null && message.getDestination() != null) {
            session.setAttribute("org.granite.gravity.channel.clientId." + message.getDestination(), channel.getId());
            session.setAttribute("org.granite.gravity.channel.subscriptionId." + message.getDestination(), subscriptionId);
        }
        return reply;
    }

    private Message handleUnsubscribeMessage(CommandMessage message) {
        Channel channel = getChannel((String) message.getClientId());
        if (channel == null) return handleUnknownClientMessage(message);
        AsyncMessage reply = null;
        ServiceAdapter adapter = adapterFactory.getServiceAdapter(message);
        reply = (AcknowledgeMessage) adapter.manage(channel, message);
        postManage(channel);
        reply.setDestination(message.getDestination());
        reply.setClientId(channel.getId());
        reply.getHeaders().putAll(message.getHeaders());
        return reply;
    }

    protected void postManage(Channel channel) {
    }

    private Message handlePublishMessage(AsyncMessage message) {
        return handlePublishMessage(message, null);
    }

    private Message handlePublishMessage(AsyncMessage message, Channel channel) {
        GraniteContext context = GraniteContext.getCurrentInstance();
        Destination destination = context.getServicesConfig().findDestinationById(message.getClass().getName(), message.getDestination());
        if (destination == null) return getInvalidDestinationError(message);
        GravityInvocationContext invocationContext = new GravityInvocationContext(message, destination);
        if (destination.getSecurizer() instanceof GravityDestinationSecurizer) {
            try {
                ((GravityDestinationSecurizer) destination.getSecurizer()).canPublish(invocationContext);
            } catch (Exception e) {
                return new ErrorMessage(message, e, true);
            }
        }
        GraniteConfig config = context.getGraniteConfig();
        if (config.hasSecurityService() && context instanceof HttpGraniteContext) {
            try {
                config.getSecurityService().authorize(invocationContext);
            } catch (Exception e) {
                return new ErrorMessage(message, e, true);
            }
        }
        Channel fromChannel = channel;
        if (fromChannel == null) fromChannel = getChannel((String) message.getClientId());
        if (fromChannel == null) return handleUnknownClientMessage(message);
        ServiceAdapter adapter = adapterFactory.getServiceAdapter(message);
        AsyncMessage reply = (AsyncMessage) adapter.invoke(fromChannel, message);
        reply.setDestination(message.getDestination());
        reply.setClientId(fromChannel.getId());
        return reply;
    }

    private Message handleUnknownClientMessage(Message message) {
        ErrorMessage reply = new ErrorMessage(message, true);
        reply.setFaultCode("Server.Call.UnknownClient");
        reply.setFaultString("Unknown client");
        return reply;
    }

    private ErrorMessage getInvalidDestinationError(Message message) {
        String messageType = message.getClass().getName();
        if (message instanceof CommandMessage) messageType += '[' + ((CommandMessage) message).getMessageRefType() + ']';
        ErrorMessage reply = new ErrorMessage(message, true);
        reply.setFaultCode("Server.Messaging.InvalidDestination");
        reply.setFaultString("No configured destination for id: " + message.getDestination() + " and message type: " + messageType);
        return reply;
    }

    private static class ServerChannel extends AbstractChannel implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Gravity gravity;

        public ServerChannel(Gravity gravity, GravityConfig gravityConfig, String channelId) {
            super(null, gravityConfig, channelId);
            this.gravity = gravity;
        }

        @Override
        public Gravity getGravity() {
            return gravity;
        }

        @Override
        public void receive(AsyncMessage message) throws MessageReceivingException {
        }

        @Override
        protected boolean hasAsyncHttpContext() {
            return false;
        }

        @Override
        protected AsyncHttpContext acquireAsyncHttpContext() {
            return null;
        }

        @Override
        protected void releaseAsyncHttpContext(AsyncHttpContext context) {
        }
    }
}
