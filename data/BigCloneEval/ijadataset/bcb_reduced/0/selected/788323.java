package net.jxta.endpoint;

import net.jxta.logging.Logging;
import net.jxta.peergroup.PeerGroupID;
import java.io.IOException;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a messenger meant to be shared by multiple channels and automatically
 * distribute the available bandwidth among the channels. This one is implemented
 * with a dedicated background thread.
 */
public abstract class ThreadedMessenger extends AbstractMessenger implements Runnable {

    /**
     * Logger
     */
    private static final transient Logger LOG = Logger.getLogger(ThreadedMessenger.class.getName());

    /**
     * Our thread group.
     */
    private static final transient ThreadGroup myThreadGroup = new ThreadGroup("Threaded Messengers");

    /**
     * The logical destination address of the other party (if we know it).
     */
    private volatile EndpointAddress logicalDestination = null;

    /**
     * true if we have deliberately closed our input queue.
     */
    private volatile boolean inputClosed = false;

    /**
     * Need to know which group the transports we use live in, so that we can suppress channel redirection when in the same group.
     * This is currently the norm.
     */
    private PeerGroupID homeGroupID = null;

    /**
     * The duration in milliseconds which the background thread will remain
     * idle before quitting.
     */
    private static final long THREAD_IDLE_DEAD = 15000;

    private enum DeferredAction {

        /**
         * No action deferred.
         */
        ACTION_NONE, /**
         * Must send the current message.
         */
        ACTION_SEND, /**
         * Must report failure to connect.
         */
        ACTION_CONNECT
    }

    /**
     * The current deferred action.
     */
    private DeferredAction deferredAction = DeferredAction.ACTION_NONE;

    /**
     * The current background thread.
     */
    private volatile Thread bgThread = null;

    /**
     * The number of messages which may be queued for in each channel.
     */
    private final int channelQueueSize;

    /**
     * The active channel queue.
     */
    private final BlockingQueue<ThreadedMessengerChannel> activeChannels = new LinkedBlockingQueue<ThreadedMessengerChannel>();

    /**
     * The resolving channels set. This is unordered. We use a weak hash map because abandoned channels could otherwise
     * accumulate in-there until the resolution attempt completes. A buggy application could easily do much damage.
     * <p/>
     * Note: a channel with at least one message in it is not considered abandoned. To prevent it from disappearing we set a
     * strong reference as the value in the map. A buggy application can do great damage, still, by queuing a single message
     * and then abandoning the channel. This is has to be dealt with at another level; limiting the number of channels
     * per application, or having a global limit on messages...TBD.
     */
    private final WeakHashMap<ThreadedMessengerChannel, ThreadedMessengerChannel> resolvingChannels = new WeakHashMap<ThreadedMessengerChannel, ThreadedMessengerChannel>(4);

    /**
     * A default channel where we put messages that are send directly through
     * this messenger rather than via one of its channels.
     */
    private ThreadedMessengerChannel defaultChannel = null;

    /**
     * State lock and engine.
     */
    private final ThreadedMessengerState stateMachine = new ThreadedMessengerState();

    /**
     * The implementation of channel messenger that getChannelMessenger returns:
     */
    private class ThreadedMessengerChannel extends AsyncChannelMessenger {

        public ThreadedMessengerChannel(EndpointAddress baseAddress, PeerGroupID redirection, String origService, String origServiceParam, int queueSize, boolean connected) {
            super(baseAddress, redirection, origService, origServiceParam, queueSize, connected);
        }

        /**
         * {@inheritDoc}
         * <p/>
         * We're supposed to return the complete destination, including
         * service and param specific to that channel.  It is not clear, whether
         * this should include the cross-group mangling, though. Historically,
         * it does not.
         */
        public EndpointAddress getLogicalDestinationAddress() {
            return logicalDestination;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void startImpl() {
            if (!addToActiveChannels(this)) {
                down();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void connectImpl() {
            if (!addToResolvingChannels(this)) {
                if ((ThreadedMessenger.this.getState() & USABLE) != 0) {
                    up();
                } else {
                    down();
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void resolPendingImpl() {
            strongRefResolvingChannel(this);
        }
    }

    /**
     * Our statemachine implementation; just connects the standard AbstractMessengerState action methods to
     * this object.
     */
    private class ThreadedMessengerState extends MessengerState {

        protected ThreadedMessengerState() {
            super(false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void connectAction() {
            deferAction(DeferredAction.ACTION_CONNECT);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void startAction() {
            deferAction(DeferredAction.ACTION_SEND);
        }

        /**
         * {@inheritDoc}
         * <p/>
         * This is a synchronous action. The state machine assumes that it
         * is done when we return. There is No need (nor means) to signal
         * completion.  No need for synchronization either: we're already
         * synchronized.
         */
        @Override
        protected void closeInputAction() {
            inputClosed = true;
            ThreadedMessengerChannel[] channels = resolvingChannels.keySet().toArray(new ThreadedMessengerChannel[0]);
            resolvingChannels.clear();
            int i = channels.length;
            while (i-- > 0) {
                channels[i].down();
            }
            channels = null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void closeOutputAction() {
            closeImpl();
        }

        /**
         * {@inheritDoc}
         * <p/>
         * The input is now closed, so we can rest assured that the last
         * channel is really the last one.
         * This is a synchronous action. The state machine assumes that it is
         * done when we return. There is no need to signal completion with an
         * idleEvent.
         * No need for synchronization either: we're already synchronized.
         */
        @Override
        protected void failAllAction() {
            while (true) {
                ThreadedMessengerChannel theChannel;
                theChannel = activeChannels.poll();
                if (theChannel == null) {
                    break;
                }
                theChannel.down();
            }
        }
    }

    /**
     * Create a new ThreadedMessenger.
     *
     * @param homeGroupID        the group that this messenger works for. This is the group of the endpoint service or transport
     *                           that created this messenger.
     * @param destination        where messages should be addressed to
     * @param logicalDestination the expected logical address of the destination. Pass null if unknown/irrelevant
     * @param channelQueueSize   The queue size that channels should have.
     */
    public ThreadedMessenger(PeerGroupID homeGroupID, EndpointAddress destination, EndpointAddress logicalDestination, int channelQueueSize) {
        super(destination);
        this.homeGroupID = homeGroupID;
        setStateLock(stateMachine);
        this.logicalDestination = logicalDestination;
        this.channelQueueSize = channelQueueSize;
    }

    /**
     * Runs the state machine until there's nothing left to do.
     * <p/>
     * Three exposed methods may need to inject new events in the system: sendMessageN, close, and shutdown. Since they can both
     * cause actions, and since connectAction and startAction are deferred, it seems possible that one of the
     * actions caused by send, close, or shutdown be called while connectAction or startAction are in progress.
     * <p/>
     * However, the state machine gives us a few guarantees: All the actions except closeInput and closeOutput have an *end*
     * event. No state transition that results in an action other than closeInput or closeOutput, may occur until the end event
     * for an on-going action has been called.
     * <p/>
     * We perform closeInput and closeOutput on the fly, so none of the exposed methods are capable of producing deferred actions
     * while an action is already deferred. So, there is at most one deferred action after returning from an event method,
     * regardless the number of concurrent threads invoking the exposed methods, and it can only happen once per deferred action
     * performed.
     */
    public void run() {
        try {
            while (true) {
                switch(nextAction()) {
                    case ACTION_NONE:
                        return;
                    case ACTION_SEND:
                        send();
                        break;
                    case ACTION_CONNECT:
                        connect();
                        break;
                }
            }
        } catch (Throwable any) {
            if (Logging.SHOW_SEVERE && LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Uncaught throwable in background thread", any);
            }
        } finally {
            synchronized (stateMachine) {
                bgThread = null;
            }
        }
    }

    private void deferAction(DeferredAction action) {
        deferredAction = action;
        if (bgThread == null) {
            bgThread = new Thread(myThreadGroup, this, "ThreadedMessenger for " + getDestinationAddress());
            bgThread.setDaemon(true);
            bgThread.start();
        }
    }

    private DeferredAction nextAction() {
        long quitAt = System.currentTimeMillis() + THREAD_IDLE_DEAD;
        synchronized (stateMachine) {
            while (deferredAction == DeferredAction.ACTION_NONE) {
                if (System.currentTimeMillis() > quitAt) {
                    return DeferredAction.ACTION_NONE;
                }
                try {
                    stateMachine.wait(THREAD_IDLE_DEAD);
                } catch (InterruptedException ie) {
                    Thread.interrupted();
                }
            }
            DeferredAction action = deferredAction;
            deferredAction = DeferredAction.ACTION_NONE;
            return action;
        }
    }

    /**
     * Performs the ACTION_SEND deferred action: sends the messages in our channel queues until there's none left or
     * we are forced to stop by connection breakage.
     * @throws InterruptedException if interrupted
     */
    private void send() throws InterruptedException {
        ThreadedMessengerChannel theChannel;
        synchronized (stateMachine) {
            theChannel = activeChannels.peek();
            if (theChannel == null) {
                stateMachine.idleEvent();
                stateMachine.notifyAll();
                return;
            }
        }
        while (true) {
            AsyncChannelMessenger.PendingMessage theMsg = theChannel.peek();
            if (theMsg == null) {
                synchronized (stateMachine) {
                    activeChannels.poll();
                    theChannel = activeChannels.peek();
                    if (theChannel != null) {
                        continue;
                    }
                    stateMachine.idleEvent();
                    stateMachine.notifyAll();
                }
                notifyChange();
                return;
            }
            Message currentMsg = theMsg.msg;
            String currentService = theMsg.service;
            String currentParam = theMsg.param;
            try {
                sendMessageBImpl(currentMsg, currentService, currentParam);
            } catch (Throwable any) {
                synchronized (stateMachine) {
                    if (theMsg.failure != null) {
                        theChannel.poll();
                        currentMsg.setMessageProperty(Messenger.class, new OutgoingMessageEvent(currentMsg, theMsg.failure));
                    } else {
                        theMsg.failure = any;
                    }
                    stateMachine.downEvent();
                    stateMachine.notifyAll();
                }
                notifyChange();
                return;
            }
            synchronized (stateMachine) {
                theChannel.poll();
                boolean empty = (theChannel.peek() == null);
                if ((activeChannels.size() != 1) || empty) {
                    activeChannels.poll();
                    if (!empty) {
                        activeChannels.put(theChannel);
                    }
                    theChannel = activeChannels.peek();
                    if (theChannel == null) {
                        stateMachine.idleEvent();
                        stateMachine.notifyAll();
                    }
                }
            }
            if (theChannel == null) {
                notifyChange();
                Thread.yield();
                return;
            }
        }
    }

    /**
     * Performs the ACTION_CONNECT deferred action. Generates a down event if it does not work.
     */
    private void connect() {
        boolean worked = connectImpl();
        ThreadedMessengerChannel[] channels = null;
        synchronized (stateMachine) {
            if (worked) {
                EndpointAddress effectiveLogicalDest = getLogicalDestinationImpl();
                if (logicalDestination == null) {
                    logicalDestination = effectiveLogicalDest;
                    stateMachine.upEvent();
                    channels = resolvingChannels.keySet().toArray(new ThreadedMessengerChannel[0]);
                    resolvingChannels.clear();
                } else if (logicalDestination.equals(effectiveLogicalDest)) {
                    stateMachine.upEvent();
                    channels = resolvingChannels.keySet().toArray(new ThreadedMessengerChannel[0]);
                    resolvingChannels.clear();
                } else {
                    closeImpl();
                    stateMachine.downEvent();
                }
            } else {
                stateMachine.downEvent();
            }
            stateMachine.notifyAll();
        }
        if (channels != null) {
            int i = channels.length;
            while (i-- > 0) {
                channels[i].up();
            }
            channels = null;
        }
        notifyChange();
    }

    /**
     * The endpoint service may call this to cause an orderly closure of its messengers.
     */
    protected final void shutdown() {
        synchronized (stateMachine) {
            stateMachine.shutdownEvent();
            stateMachine.notifyAll();
        }
        notifyChange();
    }

    /**
     * {@inheritDoc}
     */
    public EndpointAddress getLogicalDestinationAddress() {
        return logicalDestination;
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        synchronized (stateMachine) {
            stateMachine.closeEvent();
            stateMachine.notifyAll();
        }
        notifyChange();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * In this case, this method is here out of principle but is not really expected to be invoked.  The normal way
     * of using a ThreadedMessenger is through its channels. We do provide a default channel that all invokers that go around
     * channels will share. That could be useful to send rare out of band messages for example.
     */
    public final boolean sendMessageN(Message msg, String service, String serviceParam) {
        synchronized (stateMachine) {
            if (defaultChannel == null) {
                defaultChannel = new ThreadedMessengerChannel(getDestinationAddress(), null, null, null, channelQueueSize, false);
            }
        }
        return defaultChannel.sendMessageN(msg, service, serviceParam);
    }

    /**
     * {@inheritDoc}
     */
    public final void sendMessageB(Message msg, String service, String serviceParam) throws IOException {
        synchronized (stateMachine) {
            if (defaultChannel == null) {
                defaultChannel = new ThreadedMessengerChannel(getDestinationAddress(), null, null, null, channelQueueSize, false);
            }
        }
        defaultChannel.sendMessageB(msg, service, serviceParam);
    }

    private boolean addToActiveChannels(ThreadedMessengerChannel channel) {
        synchronized (stateMachine) {
            if (inputClosed) {
                return false;
            }
            try {
                activeChannels.put(channel);
            } catch (InterruptedException failed) {
                Thread.interrupted();
                return false;
            }
            stateMachine.msgsEvent();
            stateMachine.notifyAll();
        }
        notifyChange();
        return true;
    }

    private void strongRefResolvingChannel(ThreadedMessengerChannel channel) {
        synchronized (stateMachine) {
            if (resolvingChannels.containsKey(channel)) {
                resolvingChannels.put(channel, channel);
            }
        }
    }

    private boolean addToResolvingChannels(ThreadedMessengerChannel channel) {
        synchronized (stateMachine) {
            if ((stateMachine.getState() & (RESOLVED | TERMINAL)) != 0) {
                return false;
            }
            resolvingChannels.put(channel, null);
            stateMachine.resolveEvent();
            stateMachine.notifyAll();
        }
        notifyChange();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public final void resolve() {
        synchronized (stateMachine) {
            stateMachine.resolveEvent();
            stateMachine.notifyAll();
        }
        notifyChange();
    }

    /**
     * {@inheritDoc}
     */
    public final int getState() {
        return stateMachine.getState();
    }

    /**
     * {@inheritDoc}
     */
    public Messenger getChannelMessenger(PeerGroupID redirection, String service, String serviceParam) {
        return new ThreadedMessengerChannel(getDestinationAddress(), homeGroupID.equals(redirection) ? null : redirection, service, serviceParam, channelQueueSize, (stateMachine.getState() & (RESOLVED & USABLE)) != 0);
    }

    /**
     * {@inheritDoc}
     */
    protected abstract void closeImpl();

    /**
     * Make underlying connection.
     *
     * @return true if successful
     */
    protected abstract boolean connectImpl();

    /**
     * Send a message blocking as needed until the message is sent.
     *
     * @param msg The message to send.
     * @param service The destination service.
     * @param param The destination serivce param.
     * @throws IOException Thrown for errors encountered while sending the message.
     */
    protected abstract void sendMessageBImpl(Message msg, String service, String param) throws IOException;

    /**
     * {@inheritDoc}
     */
    protected abstract EndpointAddress getLogicalDestinationImpl();
}
