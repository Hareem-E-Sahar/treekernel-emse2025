public class Test {    @Override
    public Channel getChannelByCode(String code) {
        return code2channel.get(code);
    }
}