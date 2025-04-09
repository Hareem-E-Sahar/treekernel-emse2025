public class Test {    public Channel<Job> getJobChannel() {
        return getChannel(JOB_CHANNEL_ID);
    }
}