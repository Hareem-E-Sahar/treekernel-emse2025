package com.hongbo.cobweb.nmr.runtime.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessageExchangeFactory;
import javax.jbi.messaging.MessagingException;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import com.hongbo.cobweb.nmr.api.AbortedException;
import com.hongbo.cobweb.nmr.api.Channel;
import com.hongbo.cobweb.nmr.api.Endpoint;
import com.hongbo.cobweb.nmr.api.Exchange;
import com.hongbo.cobweb.nmr.api.Pattern;
import com.hongbo.cobweb.nmr.api.Reference;
import com.hongbo.cobweb.nmr.api.NMR;
import com.hongbo.cobweb.nmr.api.service.ServiceHelper;
import com.hongbo.cobweb.nmr.api.internal.InternalExchange;
import com.hongbo.cobweb.nmr.api.internal.InternalEndpoint;

/**
 * Implementation of the DeliveryChannel.
 *
 */
public class DeliveryChannelImpl implements DeliveryChannel {

    public static final String SEND_SYNC = "javax.jbi.messaging.sendSync";

    private static final String SENDER_ENDPOINT = "com.hongbo.cobweb.senderEndpoint";

    /** Mutable boolean indicating if the channe has been closed */
    private final AtomicBoolean closed;

    /** The Component Context **/
    private final AbstractComponentContext context;

    /** Holds exchanges to be polled by the component */
    private final BlockingQueue<Exchange> queue;

    /** The underlying Channel */
    private final Channel channel;

    /** The default QName for endpoints not having this property */
    private static final QName DEFAULT_SERVICE_NAME = new QName("urn:servicemix.apache.org", "jbi");

    public DeliveryChannelImpl(AbstractComponentContext context, Channel channel, BlockingQueue<Exchange> queue) {
        this.context = context;
        this.channel = channel;
        this.queue = queue;
        this.closed = new AtomicBoolean(false);
    }

    public void close() throws MessagingException {
        channel.close();
        closed.set(true);
    }

    public MessageExchangeFactory createExchangeFactory() {
        return new MessageExchangeFactoryImpl(closed);
    }

    public MessageExchangeFactory createExchangeFactory(QName interfaceName) {
        MessageExchangeFactoryImpl factory = new MessageExchangeFactoryImpl(closed);
        factory.setInterfaceName(interfaceName);
        return factory;
    }

    public MessageExchangeFactory createExchangeFactoryForService(QName serviceName) {
        MessageExchangeFactoryImpl factory = new MessageExchangeFactoryImpl(closed);
        factory.setServiceName(serviceName);
        return factory;
    }

    public MessageExchangeFactory createExchangeFactory(ServiceEndpoint endpoint) {
        MessageExchangeFactoryImpl factory = new MessageExchangeFactoryImpl(closed);
        factory.setEndpoint(endpoint);
        return factory;
    }

    public MessageExchange accept() throws MessagingException {
        try {
            Exchange exchange = queue.take();
            if (exchange == null) {
                return null;
            }
            MessageExchange me = getMessageExchange(exchange);
            ((MessageExchangeImpl) me).beforeReceived();
            return me;
        } catch (InterruptedException e) {
            throw new MessagingException(e);
        }
    }

    public MessageExchange accept(long timeout) throws MessagingException {
        try {
            long t0 = System.currentTimeMillis();
            long cur = t0;
            while (cur - t0 < timeout) {
                Exchange exchange = queue.poll(t0 + timeout - cur, TimeUnit.MILLISECONDS);
                if (exchange == null || exchange.getError() instanceof AbortedException) {
                    cur = System.currentTimeMillis();
                    continue;
                }
                MessageExchange me = getMessageExchange(exchange);
                ((MessageExchangeImpl) me).beforeReceived();
                return me;
            }
            return null;
        } catch (InterruptedException e) {
            throw new MessagingException(e);
        }
    }

    protected MessageExchange getMessageExchange(Exchange exchange) {
        MessageExchange me;
        synchronized (exchange) {
            me = exchange.getProperty(MessageExchange.class);
            if (me == null) {
                if (exchange.getPattern() == Pattern.InOnly) {
                    me = new InOnlyImpl(exchange);
                } else if (exchange.getPattern() == Pattern.InOptionalOut) {
                    me = new InOptionalOutImpl(exchange);
                } else if (exchange.getPattern() == Pattern.InOut) {
                    me = new InOutImpl(exchange);
                } else if (exchange.getPattern() == Pattern.RobustInOnly) {
                    me = new RobustInOnlyImpl(exchange);
                } else {
                    throw new IllegalStateException("Unknown pattern: " + exchange.getPattern());
                }
                exchange.setProperty(MessageExchange.class, me);
            }
        }
        if (((InternalExchange) exchange).getDestination() != null && me.getEndpoint() == null) {
            Endpoint ep = ((InternalExchange) exchange).getDestination();
            Map<String, ?> props = context.getNmr().getEndpointRegistry().getProperties(ep);
            String strSvcName = (String) props.get(Endpoint.SERVICE_NAME);
            QName serviceName = (strSvcName != null && strSvcName.length() > 0) ? QName.valueOf(strSvcName) : DEFAULT_SERVICE_NAME;
            String endpointName = (String) props.get(Endpoint.ENDPOINT_NAME);
            if (endpointName == null) {
                endpointName = (String) props.get(Endpoint.NAME);
            }
            me.setEndpoint(new ServiceEndpointImpl(serviceName, endpointName));
        }
        return me;
    }

    public void send(MessageExchange exchange) throws MessagingException {
        assert exchange != null;
        createTarget(context.getNmr(), exchange);
        exchange.setProperty(SEND_SYNC, null);
        ((MessageExchangeImpl) exchange).afterSend();
        InternalExchange ie = (InternalExchange) ((MessageExchangeImpl) exchange).getInternalExchange();
        getChannelToUse(ie).send(ie);
    }

    public boolean sendSync(MessageExchange exchange) throws MessagingException {
        assert exchange != null;
        createTarget(context.getNmr(), exchange);
        exchange.setProperty(SEND_SYNC, Boolean.TRUE);
        ((MessageExchangeImpl) exchange).afterSend();
        InternalExchange ie = (InternalExchange) ((MessageExchangeImpl) exchange).getInternalExchange();
        return getChannelToUse(ie).sendSync(ie);
    }

    public boolean sendSync(MessageExchange exchange, long timeout) throws MessagingException {
        assert exchange != null;
        createTarget(context.getNmr(), exchange);
        exchange.setProperty(SEND_SYNC, Boolean.TRUE);
        ((MessageExchangeImpl) exchange).afterSend();
        InternalExchange ie = (InternalExchange) ((MessageExchangeImpl) exchange).getInternalExchange();
        return getChannelToUse(ie).sendSync(ie, timeout);
    }

    protected Channel getChannelToUse(InternalExchange exchange) {
        Channel channelToUse = channel;
        if (exchange.getSource() == null) {
            try {
                String sender = (String) exchange.getProperty(SENDER_ENDPOINT);
                if (sender != null) {
                    int idx = sender.lastIndexOf(':');
                    String svc = sender.substring(0, idx);
                    String ep = sender.substring(idx + 1);
                    Map<String, Object> props = ServiceHelper.createMap(Endpoint.SERVICE_NAME, svc, Endpoint.ENDPOINT_NAME, ep);
                    List<Endpoint> eps = channel.getNMR().getEndpointRegistry().query(props);
                    if (eps != null && eps.size() == 1) {
                        channelToUse = ((InternalEndpoint) eps.get(0)).getChannel();
                    }
                }
            } catch (Throwable t) {
            }
        } else {
            channelToUse = exchange.getSource().getChannel();
        }
        return channelToUse;
    }

    public static void createTarget(NMR nmr, MessageExchange messageExchange) {
        createTarget(nmr, ((MessageExchangeImpl) messageExchange).getInternalExchange());
    }

    public static void createTarget(NMR nmr, Exchange exchange) {
        if (exchange.getTarget() == null) {
            Map<String, Object> props = new HashMap<String, Object>();
            ServiceEndpoint ep = MessageExchangeImpl.getEndpoint(exchange);
            if (ep != null) {
                props.put(Endpoint.SERVICE_NAME, ep.getServiceName().toString());
                props.put(Endpoint.ENDPOINT_NAME, ep.getEndpointName());
            } else {
                QName serviceName = MessageExchangeImpl.getService(exchange);
                if (serviceName != null) {
                    props.put(Endpoint.SERVICE_NAME, serviceName.toString());
                } else {
                    QName interfaceName = MessageExchangeImpl.getInterfaceName(exchange);
                    if (interfaceName != null) {
                        props.put(Endpoint.INTERFACE_NAME, interfaceName.toString());
                    }
                }
            }
            if (props.isEmpty()) {
                throw new IllegalStateException("No endpoint, service or interface name specified for routing");
            }
            Reference target = nmr.getEndpointRegistry().lookup(props);
            exchange.setTarget(target);
        }
    }
}
