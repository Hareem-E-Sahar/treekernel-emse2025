public class Test {    public String getChannelName() {
        return closed ? null : !connected ? null : cluster_name;
    }
}