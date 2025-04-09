public class Test {        public boolean isReadAvailable() {
            return (readIndex < writeIndex);
        }
}