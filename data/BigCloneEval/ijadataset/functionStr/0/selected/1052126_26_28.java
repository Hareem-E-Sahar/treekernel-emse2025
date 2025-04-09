public class Test {    public ClusterClient() throws ClusterException {
        this(ClusterChannel.getChannel());
    }
}