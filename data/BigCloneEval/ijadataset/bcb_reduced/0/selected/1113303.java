package org.myrobotlab.service;

import java.text.FieldPosition;
import java.text.NumberFormat;
import javaclient3.PlayerClient;
import javaclient3.PlayerException;
import javaclient3.Position2DInterface;
import javaclient3.SonarInterface;
import javaclient3.structures.PlayerConstants;
import org.apache.log4j.Logger;
import org.myrobotlab.framework.Service;

/**
 * @author GroG
 *
 * Interface service for player/stage http://playerstage.sourceforge.net/
 * using Javaclient3 http://java-player.sourceforge.net/examples-3.php#Navigator
 */
public class PlayerStage extends Service {

    private static final long serialVersionUID = 1L;

    public static final Logger LOG = Logger.getLogger(PlayerStage.class.getCanonicalName());

    public PlayerStage(String n) {
        this(n, null);
    }

    public PlayerStage(String n, String serviceDomain) {
        super(n, PlayerStage.class.getCanonicalName(), serviceDomain);
    }

    @Override
    public void loadDefaultConfiguration() {
    }

    static float SONAR_MIN_VALUE = 0.2f;

    static float SONAR_MAX_VALUE = 5.0f;

    static float SONAR_THRESHOLD = 0.5f;

    static float WHEEL_DIAMETER = 24.0f;

    static float DEF_YAW_SPEED = 0.50f;

    static float[] sonarValues;

    static float xspeed, yawspeed;

    static float leftSide, rightSide;

    static NumberFormat fmt = NumberFormat.getInstance();

    public static void main(String[] args) {
        PlayerClient robot = null;
        Position2DInterface posi = null;
        SonarInterface soni = null;
        try {
            robot = new PlayerClient("localhost", 6665);
            posi = robot.requestInterfacePosition2D(0, PlayerConstants.PLAYER_OPEN_MODE);
            soni = robot.requestInterfaceSonar(0, PlayerConstants.PLAYER_OPEN_MODE);
        } catch (PlayerException e) {
            System.err.println("SpaceWandererExample: > Error connecting to Player: ");
            System.err.println("    [ " + e.toString() + " ]");
            System.exit(1);
        }
        robot.runThreaded(-1, -1);
        while (true) {
            while (!soni.isDataReady()) ;
            sonarValues = soni.getData().getRanges();
            for (int i = 0; i < soni.getData().getRanges_count(); i++) if (sonarValues[i] < SONAR_MIN_VALUE) sonarValues[i] = SONAR_MIN_VALUE; else if (sonarValues[i] > SONAR_MAX_VALUE) sonarValues[i] = SONAR_MAX_VALUE;
            System.out.println(decodeSonars(soni));
            leftSide = (sonarValues[1] + sonarValues[2]) / 2;
            rightSide = (sonarValues[5] + sonarValues[6]) / 2;
            leftSide = leftSide / 10;
            rightSide = rightSide / 10;
            xspeed = (leftSide + rightSide) / 2;
            yawspeed = (float) ((leftSide - rightSide) * (180 / Math.PI) / WHEEL_DIAMETER);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            if (((sonarValues[1] > SONAR_THRESHOLD) && (sonarValues[2] > SONAR_THRESHOLD) && (sonarValues[3] > SONAR_THRESHOLD)) || ((sonarValues[4] > SONAR_THRESHOLD) && (sonarValues[5] > SONAR_THRESHOLD) && (sonarValues[6] > SONAR_THRESHOLD))) posi.setSpeed(xspeed, yawspeed); else if (sonarValues[0] < sonarValues[7]) posi.setSpeed(0, -DEF_YAW_SPEED); else posi.setSpeed(0, DEF_YAW_SPEED);
        }
    }

    static String align(NumberFormat fmt, float n, int sp) {
        StringBuffer buf = new StringBuffer();
        FieldPosition fpos = new FieldPosition(NumberFormat.INTEGER_FIELD);
        fmt.format(n, buf, fpos);
        for (int i = 0; i < sp - fpos.getEndIndex(); ++i) buf.insert(0, ' ');
        return buf.toString();
    }

    public static String decodeSonars(SonarInterface soni) {
        String out = "\nSonar vars: \n";
        for (int i = 0; i < soni.getData().getRanges_count(); i++) {
            out += " [" + align(fmt, i + 1, 2) + "] = " + align(fmt, soni.getData().getRanges()[i], 5);
            if (((i + 1) % 8) == 0) out += "\n";
        }
        return out;
    }

    @Override
    public String getToolTip() {
        return "stubbed out for Player Stage - partially implemented";
    }
}
