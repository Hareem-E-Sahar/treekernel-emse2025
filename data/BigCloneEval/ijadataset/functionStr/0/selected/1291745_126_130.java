public class Test {    @Override
    SelectableChannel getChannel() {
        assert isTransportLayerThread();
        return serverChannel_;
    }
}