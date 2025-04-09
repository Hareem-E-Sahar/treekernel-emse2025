public class Test {    public void compact() {
        ByteBuffer buffer = getBuffer();
        if (buffer.remaining() > 0) {
            m_previousBytes = NIOUtils.copy(buffer);
        }
    }
}