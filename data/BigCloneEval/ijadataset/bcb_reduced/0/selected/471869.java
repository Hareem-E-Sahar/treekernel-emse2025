package com.outbrain.pajamasproxy.memcached.server.protocol;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.outbrain.pajamasproxy.memcached.adapter.CacheElement;
import com.outbrain.pajamasproxy.memcached.proxy.AsyncCache;
import com.outbrain.pajamasproxy.memcached.proxy.value.DeleteResponse;
import com.outbrain.pajamasproxy.memcached.proxy.value.StoreResponse;
import com.outbrain.pajamasproxy.memcached.server.protocol.command.AsyncDeleteCommand;
import com.outbrain.pajamasproxy.memcached.server.protocol.command.AsyncFlushCommand;
import com.outbrain.pajamasproxy.memcached.server.protocol.command.AsyncGetCommand;
import com.outbrain.pajamasproxy.memcached.server.protocol.command.AsyncMutateCommand;
import com.outbrain.pajamasproxy.memcached.server.protocol.command.AsyncStoreCommand;
import com.outbrain.pajamasproxy.memcached.server.protocol.command.CommandQueue;
import com.outbrain.pajamasproxy.memcached.server.protocol.command.SimpleCommand;
import com.outbrain.pajamasproxy.memcached.server.protocol.command.StatsCommand;
import com.outbrain.pajamasproxy.memcached.server.protocol.command.VersionCommand;
import com.outbrain.pajamasproxy.memcached.server.protocol.exceptions.UnknownCommandException;
import com.outbrain.pajamasproxy.memcached.server.protocol.value.CommandMessage;
import com.outbrain.pajamasproxy.memcached.server.protocol.value.Op;

/**
 * The actual command handler, which is responsible for processing the CommandMessage instances
 * that are inbound from the protocol decoders.
 * <p/>
 * One instance is shared among the entire pipeline, since this handler is stateless, apart from some globals
 * for the entire daemon.
 * <p/>
 * The command handler produces ResponseMessages which are destined for the response encoder.
 */
@ChannelHandler.Sharable
public final class MemcachedCommandHandler extends SimpleChannelUpstreamHandler implements ServerConnectionStatistics {

    final Logger logger = LoggerFactory.getLogger(MemcachedCommandHandler.class);

    private final AtomicInteger currentConnectionCount = new AtomicInteger();

    private final AtomicInteger totalConnectionCount = new AtomicInteger();

    /**
   * The following state variables are universal for the entire daemon. These are used for statistics gathering.
   * In order for these values to work properly, the handler _must_ be declared with a ChannelPipelineCoverage
   * of "all".
   */
    private final String version;

    private final boolean verbose;

    /**
   * The actual physical data storage.
   */
    private final AsyncCache cache;

    /**
   * The channel group for the entire daemon, used for handling global cleanup on shutdown.
   */
    private final ChannelGroup channelGroup;

    private final CommandQueue commandQueue;

    /**
   * Construct the server session handler
   *
   * @param cache            the cache to use
   * @param memcachedVersion the version string to return to clients
   * @param verbosity        verbosity level for debugging
   * @param channelGroup
   */
    public MemcachedCommandHandler(final AsyncCache cache, final String memcachedVersion, final boolean verbosity, final ChannelGroup channelGroup, final CommandQueue commandQueue) {
        this.cache = cache;
        this.version = memcachedVersion;
        this.verbose = verbosity;
        this.channelGroup = channelGroup;
        this.commandQueue = commandQueue;
    }

    /**
   * On open we manage some statistics, and add this connection to the channel group.
   *
   * @param channelHandlerContext
   * @param channelStateEvent
   * @throws Exception
   */
    @Override
    public void channelOpen(final ChannelHandlerContext channelHandlerContext, final ChannelStateEvent channelStateEvent) throws Exception {
        totalConnectionCount.incrementAndGet();
        currentConnectionCount.incrementAndGet();
        channelGroup.add(channelHandlerContext.getChannel());
    }

    /**
   * On close we manage some statistics, and remove this connection from the channel group.
   *
   * @param channelHandlerContext
   * @param channelStateEvent
   * @throws Exception
   */
    @Override
    public void channelClosed(final ChannelHandlerContext channelHandlerContext, final ChannelStateEvent channelStateEvent) throws Exception {
        currentConnectionCount.decrementAndGet();
        channelGroup.remove(channelHandlerContext.getChannel());
    }

    /**
   * The actual meat of the matter.  Turn CommandMessages into executions against the physical cache, and then
   * pass on the downstream messages.
   *
   * @param channelHandlerContext
   * @param messageEvent
   * @throws Exception
   */
    @Override
    public void messageReceived(final ChannelHandlerContext channelHandlerContext, final MessageEvent messageEvent) throws Exception {
        if (!(messageEvent.getMessage() instanceof CommandMessage)) {
            channelHandlerContext.sendUpstream(messageEvent);
            return;
        }
        final CommandMessage command = (CommandMessage) messageEvent.getMessage();
        final Op cmd = command.op;
        final int cmdKeysSize = command.keys == null ? 0 : command.keys.size();
        if (this.verbose) {
            final StringBuilder log = new StringBuilder();
            log.append(cmd);
            if (command.element != null) {
                log.append(" ").append(command.element.getKey());
            }
            for (int i = 0; i < cmdKeysSize; i++) {
                log.append(" ").append(command.keys.get(i));
            }
            logger.info(log.toString());
        }
        final Channel channel = messageEvent.getChannel();
        if (cmd == null) {
            handleNoOp(channelHandlerContext, command, channel);
        } else {
            switch(cmd) {
                case GET:
                case GETS:
                    handleGets(channelHandlerContext, command, channel);
                    break;
                case APPEND:
                    handleAppend(channelHandlerContext, command, channel);
                    break;
                case PREPEND:
                    handlePrepend(channelHandlerContext, command, channel);
                    break;
                case DELETE:
                    handleDelete(channelHandlerContext, command, channel);
                    break;
                case DECR:
                    handleDecr(channelHandlerContext, command, channel);
                    break;
                case INCR:
                    handleIncr(channelHandlerContext, command, channel);
                    break;
                case REPLACE:
                    handleReplace(channelHandlerContext, command, channel);
                    break;
                case ADD:
                    handleAdd(channelHandlerContext, command, channel);
                    break;
                case SET:
                    handleSet(channelHandlerContext, command, channel);
                    break;
                case CAS:
                    handleCas(channelHandlerContext, command, channel);
                    break;
                case STATS:
                    handleStats(channelHandlerContext, command, cmdKeysSize, channel);
                    break;
                case VERSION:
                    handleVersion(channelHandlerContext, command, channel);
                    break;
                case QUIT:
                    handleQuit(channel);
                    break;
                case FLUSH_ALL:
                    handleFlush(channelHandlerContext, command, channel);
                    break;
                case VERBOSITY:
                    handleVerbosity(channelHandlerContext, command, channel);
                    break;
                default:
                    throw new UnknownCommandException("unknown command");
            }
        }
    }

    protected void handleNoOp(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        commandQueue.enqueueFutureResponse(new SimpleCommand(channelHandlerContext, command, channel));
    }

    protected void handleFlush(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        final Future<Boolean> futureResponse = cache.flushAll();
        commandQueue.enqueueFutureResponse(new AsyncFlushCommand(channelHandlerContext, command, channel, futureResponse));
    }

    protected void handleVerbosity(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        commandQueue.enqueueFutureResponse(new SimpleCommand(channelHandlerContext, command, channel));
    }

    protected void handleQuit(final Channel channel) {
        channel.disconnect();
    }

    protected void handleVersion(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        commandQueue.enqueueFutureResponse(new VersionCommand(channelHandlerContext, command, channel, version));
    }

    protected void handleStats(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final int cmdKeysSize, final Channel channel) {
        String option = "";
        if (cmdKeysSize > 0) {
            option = command.keys.get(0).bytes.toString();
        }
        commandQueue.enqueueFutureResponse(new StatsCommand(channelHandlerContext, command, channel, cache.stats(option)));
    }

    protected void handleDelete(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        final Future<DeleteResponse> futureResponse = cache.delete(command.keys.get(0));
        commandQueue.enqueueFutureResponse(new AsyncDeleteCommand(channelHandlerContext, command, channel, futureResponse));
    }

    protected void handleDecr(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        final Future<Long> futureResponse = cache.decrement(command.keys.get(0), command.incrAmount);
        commandQueue.enqueueFutureResponse(new AsyncMutateCommand(channelHandlerContext, command, channel, futureResponse));
    }

    protected void handleIncr(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        final Future<Long> futureResponse = cache.increment(command.keys.get(0), command.incrAmount);
        commandQueue.enqueueFutureResponse(new AsyncMutateCommand(channelHandlerContext, command, channel, futureResponse));
    }

    protected void handlePrepend(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        final Future<StoreResponse> futureResponse = cache.prepend(command.element);
        commandQueue.enqueueFutureResponse(new AsyncStoreCommand(channelHandlerContext, command, channel, futureResponse));
    }

    protected void handleAppend(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        final Future<StoreResponse> futureResponse = cache.append(command.element);
        commandQueue.enqueueFutureResponse(new AsyncStoreCommand(channelHandlerContext, command, channel, futureResponse));
    }

    protected void handleReplace(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        final Future<StoreResponse> futureResponse = cache.replace(command.element);
        commandQueue.enqueueFutureResponse(new AsyncStoreCommand(channelHandlerContext, command, channel, futureResponse));
    }

    protected void handleAdd(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        final Future<StoreResponse> futureResponse = cache.add(command.element);
        commandQueue.enqueueFutureResponse(new AsyncStoreCommand(channelHandlerContext, command, channel, futureResponse));
    }

    protected void handleCas(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        final Future<StoreResponse> futureResponse = cache.cas(command.cas_key, command.element);
        commandQueue.enqueueFutureResponse(new AsyncStoreCommand(channelHandlerContext, command, channel, futureResponse));
    }

    protected void handleSet(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        final Future<StoreResponse> futureResponse = cache.set(command.element);
        commandQueue.enqueueFutureResponse(new AsyncStoreCommand(channelHandlerContext, command, channel, futureResponse));
    }

    protected void handleGets(final ChannelHandlerContext channelHandlerContext, final CommandMessage command, final Channel channel) {
        final Future<CacheElement[]> futureResponse = cache.get(command.keys);
        commandQueue.enqueueFutureResponse(new AsyncGetCommand(channelHandlerContext, command, channel, futureResponse));
    }

    @Override
    public int getCurrentConnectionCount() {
        return currentConnectionCount.get();
    }

    @Override
    public int getTotalConnectionCount() {
        return totalConnectionCount.get();
    }
}
