package org.boblight4j.client.grabber;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.util.Random;
import org.boblight4j.client.LightsHolder;
import org.boblight4j.client.SocketClientImpl;
import org.boblight4j.client.FlagManager;
import org.boblight4j.exception.BoblightException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;

public class AbstractActiveGrabberTest {

    private static final int SCREEN_WIDTH_MIN = 320;

    private static final int SCREEN_WIDTH_MAX = 1920;

    private static final int SCREEN_HEIGHT_MIN = 240;

    private static final int SCREEN_HEIGHT_MAX = 1080;

    private AbstractActiveGrabber testable;

    private SocketClientImpl client;

    @Before
    public void setUp() throws Exception {
        client = mock(SocketClientImpl.class);
        when(client.getLightsHolder()).thenReturn(mock(LightsHolder.class));
    }

    /**
	 * Tests whether
	 * 
	 * @throws IOException
	 * @throws BoblightException
	 */
    @Test
    public void testGrabPixelsWhileRunning() throws IOException, BoblightException {
        final Random random = new Random();
        final int scrWidth = SCREEN_WIDTH_MIN + random.nextInt(SCREEN_WIDTH_MAX - SCREEN_WIDTH_MIN);
        final int scrHeight = SCREEN_HEIGHT_MIN + random.nextInt(SCREEN_HEIGHT_MAX - SCREEN_HEIGHT_MIN);
        testable = spy(new AbstractActiveGrabber(client, false, 100, 80) {

            @Override
            public int[] grabPixelAt(int xpos, int ypos) {
                return null;
            }

            @Override
            protected void updateDimensions() {
            }

            @Override
            protected int getScreenWidth() {
                return scrWidth;
            }

            @Override
            protected int getScreenHeight() {
                return scrHeight;
            }

            @Override
            public void setup(FlagManager flagManager) throws BoblightException {
            }

            @Override
            public void cleanup() {
            }
        });
        int size = 2 + random.nextInt(64 - 2);
        Whitebox.setInternalState(testable, "width", size);
        Whitebox.setInternalState(testable, "height", size);
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Whitebox.setInternalState(testable, "stop", true);
                return null;
            }
        }).when(client).sendRgb(false, null);
        System.out.println("Starting test method.");
        testable.run();
        double cellWidth = (double) scrWidth / (double) size;
        double cellHeight = (double) scrHeight / size;
        System.out.println("Starting verification.");
        for (int width = 0; width < size; width++) {
            for (int height = 0; height < size; height++) {
                final int xpos = (int) (cellWidth / 2 + cellWidth * width);
                final int ypos = (int) (cellHeight / 2 + cellHeight * height);
                verify(testable).grabPixelAt(xpos, ypos);
            }
        }
    }
}
