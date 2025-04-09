package ch.unifr.nio.framework.transform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import junit.framework.TestCase;

/**
 * tests the SimpleBufferTransformer
 * @author ronny
 */
public class BufferTransformerTest extends TestCase {

    private final Random random = new Random();

    /**
     * Test of transform method, of class SimpleBufferTransformer.
     * @throws IOException if an I/O exception occurs
     */
    public void testTransform() throws IOException {
        System.out.println("transform");
        SimpleBufferTransformer instance = new SimpleBufferTransformer();
        for (int i = 0, sum = 0; i < 10; i++) {
            ByteBuffer input = createRandomBuffer(0);
            instance.transform(input);
            sum += input.capacity();
            int remaining = instance.remaining();
            assertEquals(sum, remaining);
        }
    }

    /**
     * Test of forward method, of class SimpleBufferTransformer.
     * @throws IOException if an I/O exception occurs
     */
    public void testForward() throws IOException {
        System.out.println("\nforward");
        SimpleBufferTransformer bufferTransformer = new SimpleBufferTransformer();
        MyTransformer myTransformer = new MyTransformer();
        bufferTransformer.setNextTransformer(myTransformer);
        System.out.println("one input - one forward");
        ByteBuffer input = createRandomBuffer(100);
        int size = input.remaining();
        bufferTransformer.transform(input);
        ByteBuffer result = myTransformer.getInput();
        assertNull(result);
        bufferTransformer.forward(size);
        result = myTransformer.getInput();
        assertNotNull(result);
        assertEquals(size, result.remaining());
        System.out.println("one input - two forwards");
        input = createRandomBuffer(100);
        size = input.remaining();
        bufferTransformer.transform(input);
        result = myTransformer.getInput();
        int firstHalf = random.nextInt(size + 1);
        bufferTransformer.forward(firstHalf);
        result = myTransformer.getInput();
        assertNotNull(result);
        assertEquals(firstHalf, result.remaining());
        int secondHalf = size - firstHalf;
        bufferTransformer.forward(secondHalf);
        result = myTransformer.getInput();
        assertNotNull(result);
        assertEquals(secondHalf, result.remaining());
        System.out.println("two inputs - one forward");
        input = createRandomBuffer(100);
        size = input.remaining();
        bufferTransformer.transform(input);
        input = createRandomBuffer(100);
        size += input.remaining();
        bufferTransformer.transform(input);
        bufferTransformer.forward(size);
        result = myTransformer.getInput();
        assertNotNull(result);
        assertEquals(size, result.remaining());
        System.out.println("two inputs - two forwards");
        input = createRandomBuffer(100);
        size = input.remaining();
        bufferTransformer.transform(input);
        input = createRandomBuffer(100);
        size += input.remaining();
        bufferTransformer.transform(input);
        firstHalf = random.nextInt(size + 1);
        bufferTransformer.forward(firstHalf);
        result = myTransformer.getInput();
        assertNotNull(result);
        assertEquals(firstHalf, result.remaining());
        secondHalf = size - firstHalf;
        bufferTransformer.forward(secondHalf);
        result = myTransformer.getInput();
        assertNotNull(result);
        assertEquals(secondHalf, result.remaining());
    }

    private ByteBuffer createRandomBuffer(int minimumSize) {
        int randomSize = minimumSize + random.nextInt(100);
        System.out.println("randomSize = " + randomSize);
        return ByteBuffer.allocate(randomSize);
    }

    private class MyTransformer extends AbstractTransformer<ByteBuffer, Void> {

        private ByteBuffer copy;

        @Override
        public void transform(ByteBuffer input) throws IOException {
            copy = ByteBuffer.allocate(input.remaining());
            copy.put(input);
            copy.flip();
        }

        public ByteBuffer getInput() {
            return copy;
        }
    }
}
