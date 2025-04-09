public class Test {        protected int getSampleSizeInBytes() {
            return getFormat().getFrameSize() / getFormat().getChannels();
        }
}