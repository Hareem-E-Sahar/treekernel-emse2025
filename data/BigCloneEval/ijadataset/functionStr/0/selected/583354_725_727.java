public class Test {        private int getSampleSizeInBytes() {
            return getFormat().getFrameSize() / getFormat().getChannels();
        }
}