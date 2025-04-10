package org.simpleframework.util.select;

import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * The <code>EventDistributor</code> is used to execute operations
 * that have an interested I/O event ready. This acts much like a
 * scheduler would in that it delays the execution of the operations
 * until such time as the associated <code>SelectableChannel</code>
 * has an interested I/O event ready.
 * <p>
 * This distributor has two modes, one mode is used to cancel the
 * channel once an I/O event has occurred. This means that the channel
 * is removed from the <code>Selector</code> so that the selector 
 * does not break when asked to select again. Canceling the channel
 * is useful when the operation execution may not fully read the 
 * payload or when the operation takes a significant amount of time.
 *
 * @see org.simpleframework.util.select.ExecutorReactor
 */
class EventDistributor extends Thread implements Distributor {

    /**
   * This is used to determine the operations that need canceling.
   */
    private ChannelMap table;

    /**
   * This is the queue that is used to provide the operations.
   */
    private EventQueue ready;

    /**
   * This is used to execute the operations that are ready.
   */
    private Executor executor;

    /**
   * This is the selector used to select for interested events.
   */
    private Selector selector;

    /**
   * This is the duration in milliseconds the operation expires in.
   */
    private long expiry;

    /**
   * This is used to determine the mode the distributor uses.
   */
    private boolean cancel;

    /**
   * This is used to determine when the distributor is closed.
   */
    private volatile boolean dead;

    /**
   * Constructor for the <code>EventDistributor</code> object. This 
   * will create a distributor that distributes operations when those
   * operations show that they are ready for a given I/O event. The
   * interested I/O events are provided as a bitmask taken from the
   * events of the <code>SelectionKey</code>. Distribution of the
   * operations is passed to the provided executor object.
   *
   * @param executor this is the executor used to execute operations
   */
    public EventDistributor(Executor executor) throws Exception {
        this(executor, true);
    }

    /**
   * Constructor for the <code>EventDistributor</code> object. This 
   * will create a distributor that distributes operations when those
   * operations show that they are ready for a given I/O event. The
   * interested I/O events are provided as a bitmask taken from the
   * events of the <code>SelectionKey</code>. Distribution of the
   * operations is passed to the provided executor object.
   *
   * @param executor this is the executor used to execute operations
   * @param cancel should the channel be removed from selection
   */
    public EventDistributor(Executor executor, boolean cancel) throws Exception {
        this(executor, cancel, 300000);
    }

    /**
   * Constructor for the <code>EventDistributor</code> object. This 
   * will create a distributor that distributes operations when those
   * operations show that they are ready for a given I/O event. The
   * interested I/O events are provided as a bitmask taken from the
   * events of the <code>SelectionKey</code>. Distribution of the
   * operations is passed to the provided executor object.
   *
   * @param executor this is the executor used to execute operations
   * @param cancel should the channel be removed from selection
   */
    public EventDistributor(Executor executor, boolean cancel, long expiry) throws Exception {
        this.selector = Selector.open();
        this.table = new ChannelMap();
        this.ready = new EventQueue();
        this.executor = executor;
        this.cancel = cancel;
        this.expiry = expiry;
        this.start();
    }

    /**
   * Performs the execution of the distributor. Each distributor runs 
   * on an asynchronous thread to the <code>Reactor</code> which is
   * used to perform the selection on a set of channels. Each  time 
   * there is a new operation to be processed this will take the
   * operation from the ready queue, cancel all outstanding channels,
   * and register the operations associated channel for selection.   
   */
    public void run() {
        while (!dead) {
            try {
                register();
                cancel();
                expire();
                distribute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
   * This is used to process the <code>Operation</code> object. This
   * will wake up the selector if it is currently blocked selecting
   * and register the operations associated channel. Once the 
   * selector is awake it will acquire the operation from the queue
   * and register the associated <code>SelectableChannel</code> for
   * selection. The operation will then be executed when the channel
   * is ready for the interested I/O events.
   * 
   * @param task this is the task that is scheduled for distribution   
   * @param interest this is the bit-mask value for interested events
   */
    public void process(Operation task, int require) throws Exception {
        Event event = new SelectEvent(task, require, expiry);
        if (!dead) {
            ready.offer(event);
            selector.wakeup();
        }
    }

    /**
   * This is used to close the distributor such that it cancels all
   * of the registered channels and closes down the selector. This
   * is used when the distributor is no longer required, after the
   * close further attempts to process operations will fail.
   */
    public void close() throws Exception {
        dead = true;
        selector.close();
    }

    /**
   * This method is used to expire registered operations that remain
   * idle within the selector. Operations specify a time at which 
   * point they wish to be canceled if the I/O event they wait on
   * has not arisen. This will enables the canceled operation to be
   * canceled so that the resources it occupies can be released. 
   */
    private void expire() throws Exception {
        Set<SelectionKey> set = selector.keys();
        if (cancel) {
            long time = System.currentTimeMillis();
            for (SelectionKey key : set) {
                expire(key, time);
            }
        }
    }

    /**
   * This method is used to expire registered operations that remain
   * idle within the selector. Operations specify a time at which 
   * point they wish to be canceled if the I/O event they wait on
   * has not arisen. This will enables the cancelled operation to be
   * canceled so that the resources it occupies can be released.
   * 
   * @param key this is the selection key for the operation
   */
    private void expire(SelectionKey key, long time) throws Exception {
        Event task = (Event) key.attachment();
        if (task != null) {
            long expiry = task.getExpiry();
            if (expiry < time) {
                expire(key, task);
            }
        }
    }

    /**
   * This method is used to expire registered operations that remain
   * idle within the selector. Operations specify a time at which 
   * point they wish to be canceled if the I/O event they wait on
   * has not arisen. This will enables the canceled operation to be
   * canceled so that the resources it occupies can be released. 
   * 
   * @param key this is the selection key for the operation
   * @param task this is the actual task which is to be canceled        
   */
    private void expire(SelectionKey key, Event event) throws Exception {
        Event cancel = new CancelEvent(event);
        if (key != null) {
            key.attach(cancel);
            key.cancel();
        }
        process(key);
    }

    /**
   * This is used to cancel any selection keys that have previously
   * been selected with an interested I/O event. Performing a cancel
   * here ensures that on a the next select the associated channel
   * is not considered, this ensures the select does not break.
   */
    private void cancel() throws Exception {
        Collection<SelectionKey> list = table.values();
        for (SelectionKey key : list) {
            key.cancel();
        }
        table.clear();
    }

    /**
   * Here all the enqueued <code>Operation</code> objects will be 
   * registered for selection. Each operations channel is used for
   * selection on the interested I/O events. Once the I/O event
   * occurs for the channel the operation is scheduled for execution.   
   */
    private void register() throws Exception {
        while (!ready.isEmpty()) {
            Event event = ready.poll();
            if (event != null) {
                register(event);
            }
        }
    }

    /**
   * Here the specified <code>Operation</code> object is registered
   * with the selector. If the associated channel had previously 
   * been canceled it is removed from the cancel map to ensure it
   * is not removed from the selector when cancellation is done.
   *
   * @param event this is the operation that is to be registered
   */
    private void register(Event event) throws Exception {
        int require = event.getInterest();
        register(event, require);
    }

    /**
   * Here the specified <code>Operation</code> object is registered
   * with the selector. If the associated channel had previously 
   * been canceled it is removed from the cancel map to ensure it
   * is not removed from the selector when cancellation is done.
   *
   * @param task this is the operation that is to be registered 
   * @param interest this is the bit-mask value for interested events    
   */
    private void register(Event event, int require) throws Exception {
        SelectableChannel channel = event.getChannel();
        SelectionKey key = table.remove(channel);
        if (key != null) {
            key.interestOps(require);
        } else {
            if (channel.isOpen()) {
                select(channel, require).attach(event);
            }
        }
    }

    /**
   * This method is used to perform an actual select on a channel. It
   * will register the channel with the internal selector using the
   * required I/O event bit mask. In order to ensure that selection 
   * is performed correctly the provided channel must be connected.
   * 
   * @param channel this is the channel to register for selection
   * @param require this is the I/O bit mask that is required
   * 
   * @return this returns the selection key used for selection
   */
    private SelectionKey select(SelectableChannel channel, int require) throws Exception {
        return channel.register(selector, require);
    }

    /**
   * This method is used to perform the select and if required queue
   * the operations that are ready for execution. If the selector 
   * is woken up without any ready channels then this will return
   * quietly. If however there are a number of channels ready to be
   * processed then they are handed to the executor object and 
   * marked as ready for cancellation.
   */
    private void distribute() throws Exception {
        if (selector.select(5000) > 0) {
            if (!dead) {
                process();
            }
        }
    }

    /**
   * This will iterate over the set of selection keys and process each
   * of them. The <code>Operation</code> associated with the selection
   * key is handed to the executor to perform the channel operation.
   * Also, if configured to cancel, this method will add the channel
   * and the associated selection key to the cancellation map.
   */
    private void process() throws Exception {
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> ready = keys.iterator();
        while (ready.hasNext()) {
            SelectionKey key = ready.next();
            if (key != null) {
                ready.remove();
            }
            if (key != null) {
                process(key);
            }
        }
    }

    /**
   * This will use the specified selection key to acquire the channel
   * and <code>Operation</code> associated with it to hand to the
   * executor to perform the channel operation. Also, if configured to
   * cancel, this method will add the channel and the associated 
   * selection key to the cancellation map.
   *
   * @param key this is the selection key that is to be processed
   */
    private void process(SelectionKey key) throws Exception {
        Runnable task = (Runnable) key.attachment();
        Channel channel = (Channel) key.channel();
        if (cancel) {
            table.put(channel, key);
        }
        executor.execute(task);
    }

    /**
   * The <code>ChannelMap</code> object is used to store selection
   * keys using a given channel. This is used to determine which of
   * the registered operations has been executed, and thus should be
   * removed from the selector so that it does not break on further
   * selections of the interested operations.
   *
   * @author Niall Gallagher
   */
    private class ChannelMap extends HashMap<Channel, SelectionKey> {

        /**
      * Constructor for the <code>ChannelMap</code> object. This is
      * used to create a map for channels to selection keys. This will
      * allows the selection keys that need to be canceled quickly
      * to be retrieved using the associated channel object.
      */
        public ChannelMap() {
            super();
        }
    }
}
