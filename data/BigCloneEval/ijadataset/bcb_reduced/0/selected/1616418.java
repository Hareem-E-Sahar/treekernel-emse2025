package de.nava.informa.utils.toolkit;

import de.nava.informa.core.ChannelIF;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Scheduler of channel-related events. It uses <code>Timer</code> object to build events
 * firing plan. Using this class it is possible to scheduler big number of events related
 * to different channels with individual period and priority settings. It is also possible
 * to unsechedule processing of channels, reschedule to another period and request
 * immediate firing of processing event with consequent rebuilding of events plan.
 *
 * @author Aleksey Gureev (spyromus@noizeramp.com)
 */
public class Scheduler {

    private Timer timer;

    private SchedulerCallbackIF callback;

    private Map<ChannelIF, SchedulerTask> timers = new IdentityHashMap<ChannelIF, SchedulerTask>();

    /**
   * Creates scheduler object.
   *
   * @param callback callback object.
   */
    public Scheduler(SchedulerCallbackIF callback) {
        this.callback = callback;
        timer = new Timer(true);
    }

    /**
   * Schedule single channel for poller.
   *
   * @param channel  channel to schedule for poller.
   * @param period   period of poller.
   * @param priority priority of the task.
   */
    public final void schedule(ChannelIF channel, long period, int priority) {
        schedule(channel, 0, period, priority);
    }

    /**
   * Schedule single channel for poller.
   *
   * @param channel  channel to schedule for poller.
   * @param delay    delay before first polling.
   * @param period   period of poller.
   * @param priority priority of the task.
   */
    public final void schedule(ChannelIF channel, long delay, long period, int priority) {
        if (channel != null) {
            ChannelRecord record = new ChannelRecord(channel, period, priority);
            unschedule(channel);
            sched(record, delay, period);
        }
    }

    /**
   * Stop poller the channel.
   *
   * @param channel channel to poll no more.
   */
    public final void unschedule(ChannelIF channel) {
        ChannelRecord record = null;
        synchronized (timers) {
            SchedulerTask tt = (SchedulerTask) timers.get(channel);
            if (tt != null) {
                timers.remove(channel);
                tt.cancel();
                record = tt.getRecord();
                record.setCanceled(true);
            }
        }
    }

    /**
   * Triggers channel event immediately (if it is registered) and reschedules consequent events.
   *
   * @param channel channel.
   */
    public final void triggerNow(ChannelIF channel) {
        final SchedulerTask task;
        synchronized (timers) {
            task = (SchedulerTask) timers.get(channel);
        }
        if (task != null) {
            final ChannelRecord record = task.getRecord();
            resched(record, record.getPeriod());
        }
    }

    /**
   * Reschedules all of the tasks with new period setting.
   *
   * @param period period in millis.
   */
    public final synchronized void rescheduleAll(long period) {
        final ChannelIF[] channels;
        synchronized (timers) {
            channels = (ChannelIF[]) timers.keySet().toArray(new ChannelIF[0]);
        }
        for (int i = 0; i < channels.length; i++) {
            final ChannelIF channel = channels[i];
            rescheduleChannel(channel, period);
        }
    }

    /**
   * Reschedules single channel. If channel isn't registered yet it will be registered with
   * normal priority.
   *
   * @param channel channel.
   * @param period  new period.
   */
    public final void rescheduleChannel(final ChannelIF channel, long period) {
        final SchedulerTask task = (SchedulerTask) timers.get(channel);
        if (task == null) {
            schedule(channel, period, ChannelRecord.PRIO_NORMAL);
        } else {
            final ChannelRecord record = task.getRecord();
            synchronized (timers) {
                timers.remove(channel);
            }
            task.cancel();
            long timePassed = System.currentTimeMillis() - task.scheduledExecutionTime();
            long delay = 0;
            if (timePassed >= 0) {
                delay = period - timePassed;
                if (delay < 0) {
                    delay = 0;
                }
            }
            sched(record, delay, period);
        }
    }

    /**
   * Reschedule single record.
   *
   * @param record record.
   * @param period period.
   */
    private void resched(ChannelRecord record, long period) {
        ChannelIF channel = record.getChannel();
        unschedule(channel);
        sched(record, 0, period);
    }

    /**
   * Unconditional scheduling of single record.
   *
   * @param record record.
   * @param delay  delay before the first run.
   * @param period period in millis.
   */
    private void sched(ChannelRecord record, long delay, long period) {
        record.setCanceled(false);
        ChannelIF channel = record.getChannel();
        SchedulerTask tt = new SchedulerTask(record);
        synchronized (timers) {
            timers.put(channel, tt);
        }
        timer.schedule(tt, delay, period);
    }

    /**
   * Periodical task which checks given channel from time to time.
   */
    private class SchedulerTask extends TimerTask {

        private ChannelRecord record;

        /**
     * Creates scheduler task for given channel record.
     *
     * @param record record.
     */
        public SchedulerTask(ChannelRecord record) {
            this.record = record;
        }

        /**
     * The action to be performed by this timer task.
     */
        public void run() {
            callback.process(record);
        }

        /**
     * Returns channel record.
     *
     * @return record.
     */
        public ChannelRecord getRecord() {
            return record;
        }
    }
}
