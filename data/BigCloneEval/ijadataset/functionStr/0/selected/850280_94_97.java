public class Test {    public Channel send(ByteBuffer message) {
        getChannel().send(null, message);
        return this;
    }
}