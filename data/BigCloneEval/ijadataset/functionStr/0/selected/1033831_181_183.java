public class Test {    boolean willSkip() {
        return (writePos - readPos) >= MAX_PACKETS;
    }
}