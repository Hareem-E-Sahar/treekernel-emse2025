public class Test {        public int getAccessCount() {
            return this.reads.size() + this.writes.size();
        }
}