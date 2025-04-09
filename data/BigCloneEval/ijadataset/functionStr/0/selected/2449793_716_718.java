public class Test {    public Channel getChannel(final int fd) {
        return this.channels.get(fd);
    }
}