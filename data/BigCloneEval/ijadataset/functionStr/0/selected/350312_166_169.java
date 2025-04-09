public class Test {        public int remove() {
            assert read < write;
            return array[read++];
        }
}