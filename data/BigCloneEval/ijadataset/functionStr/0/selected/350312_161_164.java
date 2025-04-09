public class Test {        public int peek() {
            assert read < write;
            return array[read];
        }
}