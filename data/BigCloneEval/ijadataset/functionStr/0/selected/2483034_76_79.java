public class Test {        @Override
        public int getBitRate() {
            return getChannelCount() * getDepth() / 8 * samplesPerSecond;
        }
}