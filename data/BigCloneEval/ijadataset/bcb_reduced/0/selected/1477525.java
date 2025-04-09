package ch.unifr.nio.framework.transform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 * Unit tests that check, if the FramingInputTransformer works as expected.
 * @author Ronny.Standtke@gmx.net
 */
public class FramingInputTransformerTest extends TestCase {

    private final Random random = new Random();

    /**
     * Test of transform method, of class FramingInputTransformer.
     * @throws IOException if an I/O Exception occurs
     */
    public void testTransform() throws IOException {
        System.out.println("transform");
        Logger logger = Logger.getLogger("ch.unifr.nio.framework");
        logger.setLevel(Level.FINEST);
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.FINEST);
        logger.addHandler(consoleHandler);
        for (int frames = 1; frames <= 10; frames++) {
            testHeaderLength(1, frames);
        }
        for (int frames = 1; frames <= 8; frames++) {
            testHeaderLength(2, frames);
        }
    }

    private void testHeaderLength(int headerLength, int frames) throws IOException {
        int MAX = (int) Math.pow(2, 8 * headerLength) - 1;
        FramedMessage maxMessage = createFramedMessage(headerLength, MAX);
        FramingInputTransformer inputTransformer = new FramingInputTransformer(headerLength);
        inputTransformer.transform(maxMessage.both);
        ByteBuffer transformedInput = inputTransformer.getMessage();
        System.out.println("original message: " + maxMessage.message);
        System.out.println("transformedInput: " + transformedInput);
        assertEquals(maxMessage.message, transformedInput);
        maxMessage = null;
        ByteBuffer[] originalMessages = new ByteBuffer[frames];
        FramedMessage allFrames = null;
        for (int i = 0; i < frames; i++) {
            FramedMessage framedMessage = createFramedMessage(headerLength);
            originalMessages[i] = framedMessage.message;
            if (allFrames == null) {
                allFrames = framedMessage;
            } else {
                allFrames = new FramedMessage(allFrames.both, framedMessage.both);
            }
        }
        inputTransformer.transform(allFrames.both);
        for (int i = 0; i < frames; i++) {
            transformedInput = inputTransformer.getMessage();
            System.out.println("original message: " + originalMessages[i]);
            System.out.println("transformedInput: " + transformedInput);
            assertEquals(originalMessages[i], transformedInput);
        }
    }

    private FramedMessage createFramedMessage(int headerLength) throws IOException {
        int MAX = (int) Math.pow(2, 8 * headerLength) - 1;
        System.out.println("MAX = " + MAX);
        final int MESSAGE_LENGTH = random.nextInt(MAX + 1);
        System.out.println("MESSAGE_LENGTH = " + MESSAGE_LENGTH + " Byte");
        return createFramedMessage(headerLength, MESSAGE_LENGTH);
    }

    private FramedMessage createFramedMessage(int headerLength, int messageLength) throws IOException {
        ByteBuffer message = ByteBuffer.allocate(messageLength);
        byte[] randomBytes = new byte[message.remaining()];
        random.nextBytes(randomBytes);
        message.put(randomBytes);
        message.flip();
        SimpleFramingOutputTransformer outputTransformer = new SimpleFramingOutputTransformer(headerLength);
        MyByteBufferArrayTransformer myTransformer = new MyByteBufferArrayTransformer();
        outputTransformer.setNextTransformer(myTransformer);
        outputTransformer.transform(message);
        ByteBuffer header = myTransformer.getInput()[0];
        message.rewind();
        return new FramedMessage(header, message);
    }

    private class FramedMessage {

        public ByteBuffer header;

        public ByteBuffer message;

        public ByteBuffer both;

        public FramedMessage(ByteBuffer header, ByteBuffer message) {
            this.header = header;
            this.message = message;
            both = ByteBuffer.allocate(header.remaining() + message.remaining());
            both.put(header);
            header.rewind();
            both.put(message);
            message.rewind();
            both.flip();
        }
    }
}
