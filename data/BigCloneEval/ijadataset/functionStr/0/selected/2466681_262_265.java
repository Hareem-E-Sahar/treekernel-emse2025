public class Test {    @Override
    public Channel getChannel() {
        return readable() ? readChannel : writeChannel;
    }
}