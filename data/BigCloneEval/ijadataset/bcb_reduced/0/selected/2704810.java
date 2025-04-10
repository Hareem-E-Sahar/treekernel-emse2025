package org.jumpmind.symmetric.route;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jumpmind.db.util.BinaryEncoding;
import org.jumpmind.symmetric.common.Constants;
import org.jumpmind.symmetric.model.NodeChannel;
import org.jumpmind.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleRouterContext extends Context {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected NodeChannel channel;

    protected boolean encountedTransactionBoundary = false;

    protected Map<String, Long> stats = new HashMap<String, Long>();

    protected String nodeId;

    public SimpleRouterContext() {
    }

    public SimpleRouterContext(String nodeId, NodeChannel channel) {
        this.nodeId = nodeId;
        this.channel = channel;
    }

    public BinaryEncoding getBinaryEncoding() {
        return null;
    }

    public long getBatchId() {
        return -1;
    }

    public String getSourceNodeId() {
        return nodeId;
    }

    public NodeChannel getChannel() {
        return this.channel;
    }

    public Map<String, Object> getContextCache() {
        return this.context;
    }

    public void setEncountedTransactionBoundary(boolean encountedTransactionBoundary) {
        this.encountedTransactionBoundary = encountedTransactionBoundary;
    }

    public boolean isEncountedTransactionBoundary() {
        return this.encountedTransactionBoundary;
    }

    public synchronized void incrementStat(long amount, String name) {
        Long val = stats.get(name);
        if (val == null) {
            val = 0l;
        }
        val += amount;
        stats.put(name, val);
    }

    public synchronized long getStat(String name) {
        Long val = (Long) stats.get(name);
        if (val == null) {
            val = 0l;
        }
        return val;
    }

    public synchronized void logStats(Logger log, long totalTimeInMs) {
        boolean infoLevel = totalTimeInMs > Constants.LONG_OPERATION_THRESHOLD;
        Set<String> keys = new TreeSet<String>(stats.keySet());
        StringBuilder statsPrintout = new StringBuilder(channel.getChannelId());
        for (String key : keys) {
            statsPrintout.append(", " + key + "=" + stats.get(key));
        }
        if (infoLevel) {
            log.info("Routing {}", statsPrintout);
        } else {
            log.debug("Routing {}", statsPrintout);
        }
    }

    public synchronized void transferStats(SimpleRouterContext ctx) {
        Set<String> keys = new HashSet<String>(ctx.stats.keySet());
        for (String key : keys) {
            Long value = stats.get(key);
            if (value == null) {
                value = 0l;
            }
            incrementStat(value, key);
        }
    }
}
