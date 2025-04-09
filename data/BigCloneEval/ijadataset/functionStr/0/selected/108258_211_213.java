public class Test {        public synchronized int available() {
            return closed ? 0 : writePos - readPos;
        }
}