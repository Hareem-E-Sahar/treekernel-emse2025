public class Test {                        public void run() {
                            server.outputDataArrivedToServer((BigEndianHeapChannelBuffer) e.getMessage(), e.getChannel().getId().toString());
                        }
}