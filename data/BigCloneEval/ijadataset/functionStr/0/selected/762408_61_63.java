public class Test {    public Buffer(Buffer buffer) {
        writeBytes(buffer.data, buffer.readPos, buffer.remaining());
    }
}