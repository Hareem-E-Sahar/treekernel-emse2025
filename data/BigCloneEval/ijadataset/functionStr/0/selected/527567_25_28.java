public class Test {            @Override
            Channel getChannel(String name) {
                return new Channel(name, session);
            }
}