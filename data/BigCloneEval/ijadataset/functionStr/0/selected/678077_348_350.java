public class Test {    public String toString() {
        return "PageLock writers: " + this.getWriters() + " readers: " + this.getReaders();
    }
}