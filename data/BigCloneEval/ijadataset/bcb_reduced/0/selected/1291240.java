package video;

import java.awt.Rectangle;
import java.awt.Robot;
import com.cattura.packet_multibroadcaster.constants.AudioVideoTypes;
import com.cattura.packet_multibroadcaster.implementations.Source;
import com.cattura.packet_multibroadcaster.value_objects.VideoPacket;

/**
 * 
 * @author Petri Tuononen
 *
 */
public class RobotVideoSource extends Source {

    private final Rectangle SCREEN_RECTANGLE;

    private final int SCREEN_WIDTH;

    private final int SCREEN_HEIGHT;

    private Robot _robot;

    /**
     * Default constructor.
     * 
     * @param $id
     * @param $screenWidth
     * @param $screenHeight
     * @throws Exception
     */
    public RobotVideoSource(String $id, int $screenWidth, int $screenHeight) throws Exception {
        super($id, AudioVideoTypes.VIDEO);
        SCREEN_WIDTH = $screenWidth;
        SCREEN_HEIGHT = $screenHeight;
        SCREEN_RECTANGLE = new Rectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        _robot = new Robot();
    }

    /**
     * Constructor.
     * For capturing screen on specific location.
     * 
     * @param $id
     * @param $screenWidth
     * @param $screenHeight
     * @param $startLocX
     * @param $startLocY
     * @throws Exception
     */
    public RobotVideoSource(String $id, int $screenWidth, int $screenHeight, int $startLocX, int $startLocY) throws Exception {
        super($id, AudioVideoTypes.VIDEO);
        SCREEN_WIDTH = $screenWidth;
        SCREEN_HEIGHT = $screenHeight;
        SCREEN_RECTANGLE = new Rectangle($startLocX, $startLocY, SCREEN_WIDTH, SCREEN_HEIGHT);
        _robot = new Robot();
    }

    @Override
    protected void packVideoPacket(VideoPacket $videoPacket) {
        $videoPacket.pack(System.nanoTime(), _robot.createScreenCapture(SCREEN_RECTANGLE));
    }
}
