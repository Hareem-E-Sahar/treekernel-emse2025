public class Test {        public int available() throws IOException {
            return (ais.available() / sourceChannels) * targetChannels;
        }
}