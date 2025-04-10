package org.nexopenframework.management.spring.monitor.channels;

import static org.springframework.util.Assert.notNull;
import static org.springframework.util.StringUtils.hasText;
import java.util.Date;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nexopenframework.management.module.Module;
import org.nexopenframework.management.module.model.NodeInfo;
import org.nexopenframework.management.monitor.MonitorException;
import org.nexopenframework.management.monitor.channels.ChannelNotification;
import org.nexopenframework.management.monitor.channels.ChannelNotificationInfo;
import org.nexopenframework.management.monitor.channels.ChannelNotificationInfo.ChannelMedia;
import org.nexopenframework.management.monitor.channels.ChannelNotificationInfo.Severity;
import org.nexopenframework.management.monitor.core.Lifecycle;
import org.nexopenframework.management.support.NamingThreadFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.SchedulingTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * <p>NexOpen Framework</p>
 * 
 * <p>Implementation of {@link ChannelNotification} for sending notifications thru
 * an Email channel using Spring Framework support.</p>
 * 
 * @see org.nexopenframework.management.monitor.core.Lifecycle
 * @see org.nexopenframework.management.spring.monitor.channels.ChannelNotification
 * @author Francesc Xavier Magdaleno
 * @version 1.0
 * @since 1.0.0.m1
 */
public class EmailChannelNotification implements Lifecycle, ChannelNotification {

    /**Default protocol. Value is <code>smtp</code>*/
    public static final String DEFAULT_PROTOCOL = "smtp";

    /**Logging Facility based in Jakarta Commons Logging*/
    protected static final Log logger = LogFactory.getLog(EmailChannelNotification.class);

    /**Default period of time for {@link WatchDog} class to sleep before asks again for information. Value is 20 minutes*/
    private static final long DEFAULT_PERIOD = 20 * 60 * 1000;

    /**Queue of rejected emails*/
    protected final Queue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();

    /**JavaMail {@link Session} for sending messages*/
    private Session session;

    /** The Spring */
    private JavaMailSender sender;

    /**who is from this message*/
    private String from;

    /**who is addressed*/
    private String to;

    /**who are addressed*/
    private String[] toArray;

    /**which should also be notified with copy*/
    private String[] cc;

    /**which mail protocol uses. Default value is <code>smtp</code>*/
    private String protocol = DEFAULT_PROTOCOL;

    /**If we should notify <code>ERROR</code> severity*/
    private boolean notifyErrors;

    /**Spring contract for sending asynchronous tasks using a suitable implementation (j2se 5.0, commonj,...)*/
    private SchedulingTaskExecutor service;

    /**
	 * <p>Interface for resolving messages for sending to e-mail. 
	 * If none is configured , default {@link ResourceBundleMessageSource} is assumed with 
	 * inner <code>messages.properties</code> file.</p>
	 */
    private MessageSource source;

    /**period of execution list of queue of rejected emails*/
    private long period = DEFAULT_PERIOD;

    /**if current WatchDog must be cancelled [cancellation requested flag]*/
    private volatile boolean cancelled;

    /**
	 * <p></p>
	 * 
	 * @see #DEFAULT_PERIOD
	 */
    public void setPeriod(final long period) {
        if (period > 0) {
            this.period = period;
        }
    }

    public String getFrom() {
        return this.from;
    }

    /**
	 * @param from
	 */
    public void setFrom(final String from) {
        this.from = from;
    }

    /**
	 * @return
	 */
    public String getTo() {
        return this.to;
    }

    /**
	 * <p></p>
	 * 
	 * @param to
	 */
    public void setTo(final String to) {
        this.to = to;
    }

    /**
	 * <p></p>
	 * 
	 * @param to
	 */
    public void setTo(final String[] to) {
        this.toArray = to;
    }

    public void setCc(final String cc) {
        this.cc = new String[] { cc };
    }

    /**
	 * @param from
	 */
    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    /**
	 * <p>if we should notify events with severity <code>ERROR</code>. Default value is <code>false</code> and only
	 * <code>FATAL</code> are notified by email</p>
	 * 
	 * @param notifyErrors
	 */
    public void setNotifyErrors(final boolean notifyErrors) {
        this.notifyErrors = notifyErrors;
    }

    /**
	 * <p></p>
	 * 
	 * @param service
	 */
    public void setSchedulingTaskExecutor(final SchedulingTaskExecutor service) {
        this.service = service;
    }

    /**
	 * <p></p>
	 * 
	 * @param sender
	 */
    public void setJavaMailSender(final JavaMailSender sender) {
        this.sender = sender;
    }

    /**
	 * <p>Set the JavaMail {@link Session} to send messages to subscribers.</p>
	 * 
	 * @param session JavaMail {@link Session} object to send messages.
	 */
    public void setSession(final Session session) {
        this.session = session;
    }

    /**
	 * <p></p>
	 * 
	 * @return
	 */
    public Session getSession() {
        return this.session;
    }

    /**
	 * <p></p>
	 * 
	 * @param source
	 */
    public void setMessageSource(final MessageSource source) {
        this.source = source;
    }

    /**
     * <p>Init method of this {@link ChannelNotification} implementation. It creates a {@link JavaMailSenderImpl}
     * if no one is injected. We also check or {@link MessageSource} for template of email and 
     * {@link SchedulingTaskExecutor} for scheduling send of e-mails in asynchronous way. Besides, we start
     * our {@link WatchDog} for sending rejected e-mails from thread pool.</p>
     */
    public void init() {
        if (this.sender == null) {
            notNull(session);
            this.sender = new JavaMailSenderImpl();
            ((JavaMailSenderImpl) sender).setSession(session);
            if (hasText(protocol)) {
                ((JavaMailSenderImpl) sender).setProtocol(protocol);
            }
        }
        if (this.source == null) {
            final ResourceBundleMessageSource source = new ResourceBundleMessageSource();
            source.setBasename("messages");
            source.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
            this.source = source;
        }
        if (service == null) {
            final ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
            final NamingThreadFactory threadFactory = new NamingThreadFactory("nexopen-email-channel");
            threadFactory.setUncaughtExceptionHandler(new EmailUncaughtExceptionHandler());
            threadPool.setThreadFactory(threadFactory);
            threadPool.setCorePoolSize(10);
            threadPool.setRejectedExecutionHandler(new EmailChannelPolicy());
            threadPool.afterPropertiesSet();
            service = threadPool;
        }
        final WatchDog wd = new WatchDog();
        this.service.execute(wd);
    }

    /**
     * <p>Free resources and invoke shutdown methods</p>
     */
    public void destroy() {
        if (service instanceof DisposableBean) {
            try {
                ((DisposableBean) service).destroy();
            } catch (final Exception e) {
                if (logger.isInfoEnabled()) {
                    logger.info("Problem in destroy method invoking destroy of SchedulingTaskExecutor", e);
                }
            }
        }
        this.cancelled = true;
        if (!queue.isEmpty()) {
            logger.warn("Shutdown process. Automatic clearing of queue with rejected email notifications.");
            logger.warn("Size of queue :: " + queue.size());
        }
    }

    /**
	 * <p></p>
	 * 
	 * @see #sendMessageAsyncIfNecessary(org.nexopenframework.management.spring.monitor.channels.ChannelNotificationInfo)
	 * @see org.nexopenframework.management.spring.monitor.channels.ChannelNotification#processNotification(org.nexopenframework.management.spring.monitor.channels.ChannelNotificationInfo)
	 */
    public final void processNotification(final ChannelNotificationInfo info) {
        final Severity severity = info.getSeverity();
        if ((severity == Severity.WARN || severity == Severity.ERROR) && !notifyErrors) {
            if (logger.isInfoEnabled()) {
                logger.info("Not sending notification with warn/error severity");
                logger.info("If you want to send warn/errors, please change notifyErrors property to true");
            }
            return;
        }
        switch(severity) {
            case WARN:
            case ERROR:
            case FATAL:
                sendMessageAsyncIfNecessary(info);
                break;
            default:
                break;
        }
    }

    /**
	 * <p>If media is synchronous, we execute asynchronously in order to avoid problems 
	 * with current thread invocation.</p>
	 * 
	 * @param info Information to be notified to receivers
	 */
    protected final void sendMessageAsyncIfNecessary(final ChannelNotificationInfo info) throws MonitorException {
        final ChannelMedia media = info.getChannelMedia();
        if (media.equals(ChannelMedia.ASYNC)) {
            sendMessage(info);
        } else {
            service.execute(new Runnable() {

                public void run() {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invoking asynchronously channel notification information [" + info + "]");
                    }
                    sendMessage(info);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Invoked asynchronously [" + info + "]");
                    }
                }
            });
        }
    }

    /**
	 * <p>Send Mail Message with HTML format. In order to avoid bottlenecks, it is recommended 
	 *    executing asynchronously.</p>
	 * 
	 * @param info needed info to be send to receivers with detailed suggestions and so on.
	 */
    protected void sendMessage(final ChannelNotificationInfo info) throws MonitorException {
        final Locale locale = Locale.getDefault();
        try {
            final MimeMessage mimeMessage = sender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            final NodeInfo node = info.getNodeInfo();
            final Module module = info.getModule();
            final Number gauge = info.getDerivedGauge();
            final String text = source.getMessage("org.nexopenframework.management.spring.monitor.channels.html", new Object[] { module.getName(), info.getMessage(), info.getThreadName(), new Date(info.getTimestamp()), node.getHostAddress(), node.getHostName(), info.getSeverity().name(), new Date(module.getTimestamp()), module.getDescription(), info.getMonitorName(), info.getObjectName(), info.getThreshold(), ((gauge != null) ? gauge : "") }, locale);
            if (logger.isDebugEnabled()) {
                logger.debug("HTML message " + text);
            }
            helper.setText(text, true);
            if (this.toArray != null && this.toArray.length > 0) {
                helper.setTo(toArray);
            } else {
                helper.setTo(to);
            }
            if (cc != null && cc.length > 0) {
                helper.setCc(cc);
            }
            helper.setSubject(source.getMessage("org.nexopenframework.management.spring.monitor.channels.subject", new Object[] { module.getName() }, locale));
            helper.setFrom(from);
            sender.send(mimeMessage);
        } catch (final MailException e) {
            throw new MonitorException("JavaMail exception arised. See stack trace for more information.", e);
        } catch (final MessagingException e) {
            throw new MonitorException(e);
        } finally {
            info.clearOptionalInfo();
        }
    }

    public class EmailUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        public void uncaughtException(final Thread t, final Throwable e) {
            logger.error("Problem in Email Channel :: " + e.getMessage(), e);
        }
    }

    public class EmailChannelPolicy implements RejectedExecutionHandler {

        /**
		 * <p></p>
		 * 
		 * @see java.util.concurrent.RejectedExecutionHandler#rejectedExecution(java.lang.Runnable, java.util.concurrent.ThreadPoolExecutor)
		 */
        public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                logger.info("Rejected Runnable " + r);
                queue.add(r);
            }
        }
    }

    /**
	 * <p>WatchDog which deals with emails that it has not been send previously because
	 * a rejection condition has been fulfilled.</p>
	 */
    class WatchDog implements Runnable {

        public void run() {
            while (!cancelled) {
                if (!queue.isEmpty()) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Sending emails rejected by thread pool");
                    }
                    while (!queue.isEmpty()) {
                        final Runnable r = queue.poll();
                        if (r != null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("processing runnable " + r);
                            }
                            r.run();
                        }
                    }
                }
                try {
                    Thread.sleep(period);
                } catch (final InterruptedException e) {
                    logger.debug("Interrupted exception", e);
                }
            }
        }
    }
}
