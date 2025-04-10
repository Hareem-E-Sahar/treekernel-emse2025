package hermes.ext.mq;

import hermes.Domain;
import hermes.Hermes;
import hermes.HermesAdmin;
import hermes.HermesException;
import hermes.browser.MessageRenderer;
import hermes.config.DestinationConfig;
import hermes.ext.HermesAdminSupport;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.jms.JMSException;
import javax.jms.Message;
import org.apache.log4j.Logger;
import com.ibm.mq.MQC;
import com.ibm.mq.MQEnvironment;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.MQSecurityExit;
import com.ibm.mq.jms.MQConnectionFactory;
import com.ibm.mq.jms.MQQueueEnumeration;
import com.ibm.mq.pcf.CMQC;
import com.ibm.mq.pcf.CMQCFC;
import com.ibm.mq.pcf.PCFException;
import com.ibm.mq.pcf.PCFMessage;
import com.ibm.mq.pcf.PCFMessageAgent;

/**
 * @author colincrist@hermesjms.com
 * @version $Id: MQSeriesAdmin.java,v 1.17 2006/07/13 07:35:35 colincrist Exp $
 */
public class MQSeriesAdmin extends HermesAdminSupport implements HermesAdmin {

    private static final Logger log = Logger.getLogger(MQSeriesAdmin.class);

    private static Field baseMessageField;

    private MQQueueManager queueManager;

    private MQConnectionFactory mqCF;

    private MQSeriesMessageRenderer messageRenderer = new MQSeriesMessageRenderer(this);

    private WeakHashMap jmsToNativeMap = new WeakHashMap();

    static {
        try {
            baseMessageField = MQQueueEnumeration.class.getDeclaredField("baseMessage");
            baseMessageField.setAccessible(true);
        } catch (Throwable t) {
            log.error("cannot location baseMessage field in MQEnumeration, access to native messags unavailable");
        }
    }

    /**
    *  
    */
    public MQSeriesAdmin(Hermes hermes, MQConnectionFactory mqCF) {
        super(hermes);
        this.mqCF = mqCF;
    }

    private synchronized MQQueueManager getQueueManager() throws Exception {
        if (queueManager == null) {
            MQEnvironment.channel = mqCF.getChannel();
            MQEnvironment.port = mqCF.getPort();
            MQEnvironment.hostname = mqCF.getHostName();
            MQEnvironment.properties.put(MQC.TRANSPORT_PROPERTY, MQC.TRANSPORT_MQSERIES);
            if (mqCF.getSecurityExit() != null) {
                Class clazz = getClass().getClassLoader().loadClass(mqCF.getSecurityExit());
                MQSecurityExit securityExit = null;
                if (mqCF.getSecurityExitInit() != null) {
                    securityExit = (MQSecurityExit) clazz.getConstructor(String.class).newInstance(mqCF.getSecurityExitInit());
                } else {
                    securityExit = (MQSecurityExit) clazz.newInstance();
                }
                MQEnvironment.securityExit = securityExit;
            }
            queueManager = new MQQueueManager(mqCF.getQueueManager());
        }
        return queueManager;
    }

    public Enumeration createBrowserProxy(final Enumeration iter) throws JMSException {
        if (false) {
            final MQQueueEnumeration mqEnum = (MQQueueEnumeration) iter;
            return new Enumeration() {

                public boolean hasMoreElements() {
                    return iter.hasMoreElements();
                }

                public Object nextElement() {
                    final Message m = (Message) iter.nextElement();
                    try {
                        if (baseMessageField != null) {
                            final Object o = baseMessageField.get(iter);
                            if (o instanceof MQMessage) {
                                synchronized (jmsToNativeMap) {
                                    jmsToNativeMap.put(m, new WeakReference(o));
                                }
                            }
                        }
                    } catch (Throwable e) {
                        log.error(e.getMessage(), e);
                    }
                    return m;
                }
            };
        } else {
            return iter;
        }
    }

    MQMessage getMQMessage(Message m) throws JMSException {
        synchronized (jmsToNativeMap) {
            if (jmsToNativeMap.containsKey(m)) {
                WeakReference ref = (WeakReference) jmsToNativeMap.get(m);
                return (MQMessage) ref.get();
            } else {
                throw new JMSException("No reference found to native message");
            }
        }
    }

    @Override
    public String getRealDestinationName(DestinationConfig dConfig) throws JMSException {
        String queueName = super.getRealDestinationName(dConfig);
        if (queueName.startsWith("queue:///")) {
            queueName = queueName.substring(9);
        }
        if (queueName.indexOf("?") != -1) {
            queueName = queueName.substring(0, queueName.indexOf("?"));
        }
        log.debug("real name=" + queueName);
        return queueName;
    }

    public int getDepth(DestinationConfig dConfig) throws JMSException {
        try {
            final String queueName = getRealDestinationName(dConfig);
            final MQQueue queue = getQueueManager().accessQueue(queueName, MQC.MQOO_INQUIRE | MQC.MQOO_INPUT_AS_Q_DEF, null, null, null);
            final int depth = queue.getCurrentDepth();
            queue.close();
            return depth;
        } catch (Exception e) {
            close();
            throw new HermesException(e);
        }
    }

    public synchronized void close() throws JMSException {
        try {
            if (queueManager != null) {
                try {
                    queueManager.disconnect();
                    queueManager.close();
                } finally {
                    queueManager = null;
                }
            }
        } catch (MQException e) {
            throw new HermesException(e);
        }
    }

    private void getQueueByType(PCFMessageAgent agent, int type, Collection destinations) throws PCFException, MQException, IOException {
        final PCFMessage request = new PCFMessage(CMQCFC.MQCMD_INQUIRE_Q_NAMES);
        request.addParameter(CMQC.MQCA_Q_NAME, "*");
        request.addParameter(CMQC.MQIA_Q_TYPE, type);
        final PCFMessage[] responses = agent.send(request);
        final String[] names = (String[]) responses[0].getParameterValue(CMQCFC.MQCACF_Q_NAMES);
        for (int i = 0; i < names.length; i++) {
            final DestinationConfig dConfig = new DestinationConfig();
            dConfig.setName(names[i].trim());
            dConfig.setDomain(Domain.QUEUE.getId());
            destinations.add(dConfig);
        }
    }

    public synchronized Collection discoverDestinationConfigs() throws JMSException {
        final Collection rval = new ArrayList();
        PCFMessageAgent agent = null;
        try {
            agent = new PCFMessageAgent(getQueueManager());
            getQueueByType(agent, MQC.MQQT_LOCAL, rval);
            getQueueByType(agent, MQC.MQQT_ALIAS, rval);
        } catch (MQException ex) {
            if (ex.reasonCode != 2033) {
                throw new HermesException(ex);
            } else {
                log.debug("PCF calls gave a 2033 reason code, ignoring");
            }
        } catch (Exception ex) {
            throw new HermesException(ex);
        } finally {
            if (agent != null) {
                try {
                    agent.disconnect();
                } catch (MQException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return rval;
    }

    public synchronized Map getStatistics(DestinationConfig dConfig) throws JMSException {
        final Map stats = new LinkedHashMap();
        MQQueue queue = null;
        try {
            final String queueName = getRealDestinationName(dConfig);
            queue = getQueueManager().accessQueue(queueName, MQC.MQOO_INQUIRE | MQC.MQOO_INPUT_AS_Q_DEF, null, null, null);
            stats.put("Description", queue.getDescription().trim());
            stats.put("CurrentDepth", new Integer(queue.getCurrentDepth()));
            stats.put("OpenOutputCount", new Integer(queue.getOpenOutputCount()));
            stats.put("OpenInputCount", new Integer(queue.getOpenInputCount()));
            if (queue.getInhibitGet() == MQC.MQQA_GET_INHIBITED) {
                stats.put("InhibitGet", Boolean.TRUE);
            } else {
                stats.put("InhibitGet", Boolean.FALSE);
            }
            if (queue.getInhibitPut() == MQC.MQQA_PUT_INHIBITED) {
                stats.put("InhibitPut", Boolean.TRUE);
            } else {
                stats.put("InhibitPut", Boolean.FALSE);
            }
            if (queue.getShareability() == MQC.MQQA_SHAREABLE) {
                stats.put("Sharable", Boolean.TRUE);
            } else {
                stats.put("Sharable", Boolean.FALSE);
            }
            if (queue.getTriggerControl() == MQC.MQTC_ON) {
                stats.put("TriggerControl", Boolean.TRUE);
                stats.put("TriggerData", queue.getTriggerData());
                stats.put("TriggerDepth", new Integer(queue.getTriggerDepth()));
                stats.put("TriggerMessagePriority", new Integer(queue.getTriggerMessagePriority()));
                switch(queue.getTriggerType()) {
                    case MQC.MQTT_NONE:
                        stats.put("TriggerType", "None");
                        break;
                    case MQC.MQTT_DEPTH:
                        stats.put("TriggerType", "Depth");
                        break;
                    case MQC.MQTT_EVERY:
                        stats.put("TriggerType", "Every");
                        break;
                    case MQC.MQTT_FIRST:
                        stats.put("TriggerType", "First");
                        break;
                    default:
                        stats.put("TriggerType", "Unknown");
                }
            } else {
                stats.put("TriggerControl", Boolean.FALSE);
            }
            stats.put("MaximumDepth", new Integer(queue.getMaximumDepth()));
            stats.put("MaximumMessageLength", new Integer(queue.getMaximumMessageLength()));
        } catch (MQException ex) {
            if (ex.reasonCode != 2033) {
                throw new HermesException(ex);
            } else {
                log.debug("PCF calls gave a 2033 reason code, ignoring");
            }
        } catch (Exception ex) {
            throw new HermesException(ex);
        } finally {
            if (queue != null) {
                try {
                    queue.close();
                } catch (MQException ex) {
                    log.error("ignoring error closing queue: " + ex.getMessage(), ex);
                }
            }
        }
        return stats;
    }

    public MessageRenderer getMessageRenderer() throws JMSException {
        return null;
    }
}
