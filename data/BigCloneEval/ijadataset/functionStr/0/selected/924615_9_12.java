public class Test {    @Override
    public CompositeKey getKey() {
        return CompositeKey.instance(getNodeId(), getChannelId());
    }
}