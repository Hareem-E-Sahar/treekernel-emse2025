public class Test {        public int getBitsPerPixel() {
            if (isPaletteIndexed()) {
                return getBitDepth();
            } else {
                return getBitDepth() * getChannelCount();
            }
        }
}