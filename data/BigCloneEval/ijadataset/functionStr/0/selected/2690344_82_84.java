public class Test {                        public void run() {
                            server.outputDataArrivedToServerAndForwardedOut((BigEndianHeapChannelBuffer) e.getMessage(), e.getChannel().getId().toString());
                        }
}