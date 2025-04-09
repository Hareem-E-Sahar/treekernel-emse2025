package org.gudy.azureus2.core3.ipfilter.impl;

import java.util.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.gudy.azureus2.core3.ipfilter.IpFilterManagerFactory;
import org.gudy.azureus2.core3.ipfilter.IpRange;
import org.gudy.azureus2.core3.logging.*;
import org.gudy.azureus2.core3.util.*;
import org.gudy.azureus2.core3.tracker.protocol.PRHelpers;

public class IPAddressRangeManager {

    private static final LogIDs LOGID = LogIDs.CORE;

    protected ArrayList entries = new ArrayList();

    protected long total_span;

    protected boolean rebuild_required;

    protected long last_rebuild_time;

    protected IpRange[] mergedRanges = new IpRange[0];

    protected AEMonitor this_mon = new AEMonitor("IPAddressRangeManager");

    protected IPAddressRangeManager() {
    }

    public void addRange(IpRange range) {
        try {
            this_mon.enter();
            entries.add(range);
            rebuild_required = true;
        } finally {
            this_mon.exit();
        }
    }

    public void removeRange(IpRange range) {
        try {
            this_mon.enter();
            entries.remove(range);
            rebuild_required = true;
        } finally {
            this_mon.exit();
        }
    }

    public Object isInRange(String ip) {
        if (entries.size() == 0) {
            return (null);
        }
        try {
            this_mon.enter();
            long address_long = addressToInt(ip);
            if (address_long < 0) {
                address_long += 0x100000000L;
            }
            Object res = isInRange(address_long);
            return (res);
        } finally {
            this_mon.exit();
        }
    }

    public Object isInRange(InetAddress ip) {
        if (entries.size() == 0) {
            return (null);
        }
        try {
            this_mon.enter();
            long address_long = addressToInt(ip);
            if (address_long < 0) {
                address_long += 0x100000000L;
            }
            Object res = isInRange(address_long);
            return (res);
        } finally {
            this_mon.exit();
        }
    }

    protected Object isInRange(long address_long) {
        try {
            this_mon.enter();
            checkRebuild();
            if (mergedRanges.length == 0) {
                return (null);
            }
            int bottom = 0;
            int top = mergedRanges.length - 1;
            int current = -1;
            while (top >= 0 && bottom < mergedRanges.length && bottom <= top) {
                current = (bottom + top) / 2;
                IpRange e = mergedRanges[current];
                long this_start = e.getStartIpLong();
                long this_end = e.getMergedEndLong();
                if (address_long == this_start) {
                    break;
                } else if (address_long > this_start) {
                    if (address_long <= this_end) {
                        break;
                    }
                    bottom = current + 1;
                } else if (address_long == this_end) {
                    break;
                } else {
                    if (address_long >= this_start) {
                        break;
                    }
                    top = current - 1;
                }
            }
            if (top >= 0 && bottom < mergedRanges.length && bottom <= top) {
                IpRange e = mergedRanges[current];
                if (address_long <= e.getEndIpLong()) {
                    return (e);
                }
                IpRange[] merged = e.getMergedEntries();
                if (merged == null) {
                    Debug.out("IPAddressRangeManager: inconsistent merged details - no entries");
                    return (null);
                }
                for (int i = 0; i < merged.length; i++) {
                    IpRange me = merged[i];
                    if (me.getStartIpLong() <= address_long && me.getEndIpLong() >= address_long) {
                        return (me);
                    }
                }
                Debug.out("IPAddressRangeManager: inconsistent merged details - entry not found");
            }
            return (null);
        } finally {
            this_mon.exit();
        }
    }

    protected int addressToInt(String address) {
        try {
            return (PRHelpers.addressToInt(address));
        } catch (UnknownHostException e) {
            return (UnresolvableHostManager.getPseudoAddress(address));
        }
    }

    protected int addressToInt(InetAddress address) {
        return (PRHelpers.addressToInt(address));
    }

    protected void checkRebuild() {
        try {
            this_mon.enter();
            if (rebuild_required) {
                long now = SystemTime.getCurrentTime();
                long secs_since_last_build = (now - last_rebuild_time) / 1000;
                if (secs_since_last_build > entries.size() / 2000) {
                    last_rebuild_time = now;
                    rebuild_required = false;
                    rebuild();
                }
            }
        } finally {
            this_mon.exit();
        }
    }

    protected void rebuild() {
        if (Logger.isEnabled()) Logger.log(new LogEvent(LOGID, "IPAddressRangeManager: rebuilding " + entries.size() + " entries starts"));
        IpRange[] ents = new IpRange[entries.size()];
        entries.toArray(ents);
        for (int i = 0; i < ents.length; i++) {
            ents[i].resetMergeInfo();
        }
        Arrays.sort(ents, new Comparator() {

            public int compare(Object o1, Object o2) {
                IpRange e1 = (IpRange) o1;
                IpRange e2 = (IpRange) o2;
                long diff = e1.getStartIpLong() - e2.getStartIpLong();
                if (diff == 0) {
                    diff = e2.getEndIpLong() - e1.getEndIpLong();
                }
                return signum(diff);
            }

            public boolean equals(Object obj) {
                return (false);
            }
        });
        List me = new ArrayList(ents.length);
        for (int i = 0; i < ents.length; i++) {
            IpRange entry = ents[i];
            if (entry.getMerged()) {
                continue;
            }
            me.add(entry);
            int pos = i + 1;
            while (pos < ents.length) {
                long end_pos = entry.getMergedEndLong();
                IpRange e2 = ents[pos++];
                if (!e2.getMerged()) {
                    if (end_pos >= e2.getStartIpLong()) {
                        e2.setMerged();
                        if (e2.getEndIpLong() > end_pos) {
                            entry.setMergedEnd(e2.getEndIpLong());
                            entry.addMergedEntry(e2);
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        mergedRanges = new IpRange[me.size()];
        me.toArray(mergedRanges);
        total_span = 0;
        for (int i = 0; i < mergedRanges.length; i++) {
            IpRange e = mergedRanges[i];
            long span = (e.getMergedEndLong() - e.getStartIpLong()) + 1;
            total_span += span;
        }
        if (Logger.isEnabled()) Logger.log(new LogEvent(LOGID, "IPAddressRangeManager: rebuilding " + entries.size() + " entries ends"));
    }

    /**
	 * @param diff
	 * @return
	 */
    protected int signum(long diff) {
        if (diff > 0) {
            return 1;
        }
        if (diff < 0) {
            return -1;
        }
        return 0;
    }

    protected long getTotalSpan() {
        checkRebuild();
        return (total_span);
    }

    public static void main(String[] args) {
        IPAddressRangeManager manager = new IPAddressRangeManager();
        Random r = new Random();
        for (int i = 0; i < 1000000; i++) {
            int ip1 = r.nextInt(0x0fffffff);
            int ip2 = ip1 + r.nextInt(255);
            String start = PRHelpers.intToAddress(ip1);
            String end = PRHelpers.intToAddress(ip2);
            manager.addRange(new IpRangeImpl("test_" + i, start, end, true));
        }
        int num = 0;
        int hits = 0;
        while (true) {
            if (num % 1000 == 0) {
                System.out.println(num + "/" + hits);
            }
            num++;
            int ip = r.nextInt();
            Object res = manager.isInRange(ip);
            if (res != null) {
                hits++;
            }
        }
    }

    public ArrayList getEntries() {
        return entries;
    }

    public void clearAllEntries() {
        try {
            this_mon.enter();
            entries.clear();
            IpFilterManagerFactory.getSingleton().deleteAllDescriptions();
            rebuild_required = true;
        } finally {
            this_mon.exit();
        }
    }
}
