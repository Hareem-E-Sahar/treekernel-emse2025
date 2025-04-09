package ch.unifr.nio.framework.transform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import junit.framework.TestCase;

/**
 * tests the BufferForwarder
 * @author Ronny Standtke <Ronny.Standtke@gmx.net>
 */
public class BufferForwarderTest extends TestCase {

    private final Random random = new Random();

    private final ByteBufferToArrayTransformer byteBufferToArrayTransformer = new ByteBufferToArrayTransformer();

    /**
     * Test of forward method, of class BufferForwarder.
     * @throws IOException if an I/O exception occurs
     */
    public void testTransform() throws IOException {
        System.out.println("transform");
        BufferForwarder instance = new BufferForwarder(ByteBufferForwardingMode.DIRECT);
        byteBufferToArrayTransformer.setNextForwarder(instance);
        for (int i = 0, sum = 0; i < 10; i++) {
            ByteBuffer input = createRandomBuffer(0);
            byteBufferToArrayTransformer.forward(input);
            sum += input.capacity();
            int remaining = instance.remaining();
            assertEquals(sum, remaining);
        }
    }

    /**
     * Test of forward method, of class BufferForwarder.
     * @throws IOException if an I/O exception occurs
     */
    public void testForward() throws IOException {
        System.out.println("\nforward");
        BufferForwarder bufferTransformer = new BufferForwarder(ByteBufferForwardingMode.DIRECT);
        byteBufferToArrayTransformer.setNextForwarder(bufferTransformer);
        MyByteBufferCopyForwarder myByteBufferCopyForwarder = new MyByteBufferCopyForwarder();
        bufferTransformer.setNextForwarder(myByteBufferCopyForwarder);
        System.out.println("one input - one forward");
        ByteBuffer input = createRandomBuffer(100);
        int size = input.remaining();
        byteBufferToArrayTransformer.forward(input);
        ByteBuffer result = myByteBufferCopyForwarder.getInput();
        assertNull(result);
        bufferTransformer.forward(size);
        result = myByteBufferCopyForwarder.getInput();
        assertNotNull(result);
        assertEquals(size, result.remaining());
        System.out.println("one input - two forwards");
        input = createRandomBuffer(100);
        size = input.remaining();
        byteBufferToArrayTransformer.forward(input);
        result = myByteBufferCopyForwarder.getInput();
        int firstHalf = random.nextInt(size + 1);
        bufferTransformer.forward(firstHalf);
        result = myByteBufferCopyForwarder.getInput();
        assertNotNull(result);
        assertEquals(firstHalf, result.remaining());
        int secondHalf = size - firstHalf;
        bufferTransformer.forward(secondHalf);
        result = myByteBufferCopyForwarder.getInput();
        assertNotNull(result);
        assertEquals(secondHalf, result.remaining());
        System.out.println("two inputs - one forward");
        input = createRandomBuffer(100);
        size = input.remaining();
        byteBufferToArrayTransformer.forward(input);
        input = createRandomBuffer(100);
        size += input.remaining();
        byteBufferToArrayTransformer.forward(input);
        bufferTransformer.forward(size);
        result = myByteBufferCopyForwarder.getInput();
        assertNotNull(result);
        assertEquals(size, result.remaining());
        System.out.println("two inputs - two forwards");
        input = createRandomBuffer(100);
        size = input.remaining();
        byteBufferToArrayTransformer.forward(input);
        input = createRandomBuffer(100);
        size += input.remaining();
        byteBufferToArrayTransformer.forward(input);
        firstHalf = random.nextInt(size + 1);
        bufferTransformer.forward(firstHalf);
        result = myByteBufferCopyForwarder.getInput();
        assertNotNull(result);
        assertEquals(firstHalf, result.remaining());
        secondHalf = size - firstHalf;
        bufferTransformer.forward(secondHalf);
        result = myByteBufferCopyForwarder.getInput();
        assertNotNull(result);
        assertEquals(secondHalf, result.remaining());
    }

    private ByteBuffer createRandomBuffer(int minimumSize) {
        int randomSize = minimumSize + random.nextInt(100);
        System.out.println("randomSize = " + randomSize);
        return ByteBuffer.allocate(randomSize);
    }
}
