package PRISM.VRW;

import PRISM.RobotCtrl.*;
import java.util.*;
import java.lang.*;
import java.io.*;

/**
 * 
 * @author Mauro Dragone
 */
public class VRWBall implements Runnable {

    boolean m_bRunning = false;

    int m_msDelay = 40;

    Thread m_thread = null;

    VRWRobot m_VRWRobot = null;

    double m_seno;

    double m_coseno;

    EnvironmentMap m_map = null;

    double piConst = Math.PI / 1800;

    double m_v = 0;

    private int m_x = 400;

    private int m_y = 0;

    boolean debug = false;

    double m_d2 = 0;

    long m_elapsed;

    double r_seno = 0;

    double r_coseno = 0;

    int m_xr = 0;

    int m_yr = 0;

    int m_vr = 0;

    public VRWBall() {
        m_thread = new Thread(this);
        m_VRWRobot = new VRWRobot("ball");
        m_map = EnvironmentMap.getMap();
        ref = this;
    }

    public void activate(boolean startThread) {
        m_bRunning = true;
        if (startThread && VRWClock.getVRWClock().getSpeed() > 0) m_thread.start();
        updateVRW();
    }

    public void updateVRW() {
        m_VRWRobot.x = m_x;
        m_VRWRobot.y = m_y;
        m_VRWRobot.alpha = 0;
        VRWClient.getVRWClient().update(m_VRWRobot);
    }

    public static synchronized VRWBall getBall() {
        return ref;
    }

    public synchronized void setPosition(int x, int y) {
        m_x = x;
        m_y = y;
        System.out.println("Ball position set to " + m_x + "," + m_y);
    }

    public synchronized void setDestination(double v, double cosine, double sine) {
        m_seno = sine;
        m_coseno = cosine;
        m_v = v;
    }

    public boolean isActive() {
        return m_bRunning;
    }

    public void activate() {
        m_bRunning = true;
        m_thread.start();
    }

    public synchronized int getX() {
        return m_x;
    }

    public synchronized int getY() {
        return m_y;
    }

    public void run() {
        System.out.println("Ball started" + m_bRunning);
        long tmLast = VRWClock.getVRWClock().currentTimeMillis();
        m_seno = 0;
        m_coseno = 0;
        m_v = 0;
        int res[] = new int[6];
        int old_x = 400;
        int old_y = 0;
        boolean bFirstTime = true;
        boolean verticalHit = false;
        boolean horizontalHit = false;
        boolean bGoal = false;
        while (m_bRunning) {
            long tmNow = VRWClock.getVRWClock().currentTimeMillis();
            m_elapsed = tmNow - tmLast;
            tmLast = tmNow;
            if (m_v != 0) {
                m_x += Math.floor(m_v * m_elapsed * m_coseno / 1000);
                m_y += Math.floor(m_v * m_elapsed * m_seno / 1000);
                verticalHit = false;
                horizontalHit = false;
                if (Math.abs(m_y) + 40 > 788) {
                    if (m_seno >= 0) m_y = (788 - 40); else m_y = (-788 + 40);
                    m_seno = -m_seno;
                    horizontalHit = true;
                }
                if (Math.abs(m_x) + 40 > 1576) {
                    if (m_coseno >= 0) m_x = (1576 - 40); else m_x = (-1576 + 40);
                    m_coseno = -m_coseno;
                    verticalHit = true;
                    if (Math.abs(m_y) + 40 < 394) {
                        m_x = 0;
                        m_y = 0;
                        bGoal = true;
                    }
                }
            }
            boolean bImpact = false;
            double minD2 = 1000000;
            for (VRWRobot robot : VRWClient.getVRWClient().m_mapRobots.values()) {
                if (VRWClock.getVRWClock().getSpeed() == 0) break;
                if (robot.strAgentName.equalsIgnoreCase("ball")) continue;
                double d2 = (m_x - robot.x) * (m_x - robot.x) + (m_y - robot.y) * (m_y - robot.y);
                if (debug) System.out.println("d2 from to " + m_d2 + " " + d2 + " d is " + Math.sqrt(d2) + " I'm in " + m_x + " , " + m_y);
                m_d2 = d2;
                if (d2 > 12000) continue;
                if (d2 < minD2) minD2 = d2;
                bImpact = true;
                double d = Math.sqrt(d2);
                if (debug) System.out.println("d2 < 10000-> impact ");
                r_coseno = Math.cos(robot.alpha * piConst);
                r_seno = Math.sin(robot.alpha * piConst);
                m_xr = robot.x;
                m_yr = robot.y;
                m_vr = robot.v;
                adjustPosition();
                if (debug) System.out.println("robot (" + r_coseno + ", " + r_seno + ")");
                double impact_seno = -(m_seno * m_v - r_seno * robot.v);
                double impact_coseno = -(m_coseno * m_v - r_coseno * robot.v);
                if (debug) System.out.println("impact (" + impact_coseno + ", " + impact_seno + ")");
                double impulse_seno = -(m_y - robot.y) / d;
                double impulse_coseno = -(m_x - robot.x) / d;
                if (debug) System.out.println("impulse (" + impulse_coseno + ", " + impulse_seno + ")");
                double impactSpeed = impulse_seno * impact_seno + impulse_coseno * impact_coseno;
                if (debug) System.out.println("Impact Speed: " + impactSpeed);
                double impulse = impactSpeed * 0.4 * 30 * 0.06;
                if (debug) System.out.println("Impulse: " + impulse);
                m_seno = m_seno * m_v + impulse * impulse_seno / 0.4;
                m_coseno = m_coseno * m_v + impulse * impulse_coseno / 0.4;
                if (debug) System.out.println("new ball (" + m_coseno + ", " + m_seno + ")");
                m_v = Math.sqrt(m_seno * m_seno + m_coseno * m_coseno);
                if (debug) System.out.println("v: " + m_v);
                m_seno /= m_v;
                m_coseno /= m_v;
                if (verticalHit) m_seno = 0;
                if (horizontalHit) m_coseno = 0;
                m_x += (int) Math.floor(m_coseno * m_elapsed * m_v / 1000);
                m_y += (int) Math.floor(m_seno * m_elapsed * m_v / 1000);
                break;
            }
            if (!bImpact) {
                m_v = m_v - 5 * m_elapsed / (float) 1000;
                if (m_v < 0) m_v = 0;
            }
            if (debug) System.out.println("Pos= (" + m_x + ", " + m_y + ") v= " + m_v);
            if ((m_v > 0) || (bFirstTime)) {
                m_VRWRobot.x = m_x;
                m_VRWRobot.y = m_y;
                m_VRWRobot.alpha = 0;
                VRWClient.getVRWClient().update(m_VRWRobot);
            }
            if (bGoal) {
                m_v = 0;
                bGoal = false;
            }
            if (minD2 > 4000) {
                if (VRWClock.getVRWClock().getSpeed() > 0) VRWClock.getVRWClock().sleep(m_msDelay);
            } else {
                if (VRWClock.getVRWClock().getSpeed() > 0) VRWClock.getVRWClock().sleep(m_msDelay);
            }
            if (VRWClock.getVRWClock().getSpeed() == 0) m_thread.yield();
            bFirstTime = false;
            old_x = m_x;
            old_y = m_y;
        }
    }

    void adjustPosition() {
        int xb = m_x;
        int yb = m_y;
        int xr = m_xr;
        int yr = m_yr;
        double d2 = m_d2;
        long t0 = 0;
        long t1 = 3 * m_elapsed;
        if (t1 == 0) t1 = 500;
        long t = t0;
        if (debug) {
            System.out.println("Elapsed = " + m_elapsed + " D2= " + d2 + " searching to " + t1 + " ms ago");
            System.out.println("Ball : " + xb + ", " + yb + ")  going (" + m_coseno + ", " + m_seno + ") with vb= " + m_v);
            System.out.println("Robot: " + xr + ", " + yr + ")  going (" + r_coseno + ", " + r_seno + ") with vr= " + m_vr);
        }
        while (t1 - t0 > 2) {
            t = t0 + (t1 - t0) / 2;
            xb = m_x - (int) Math.floor(m_v * t * m_coseno / 1000);
            yb = m_y - (int) Math.floor(m_v * t * m_seno / 1000);
            xr = m_xr - (int) Math.floor(m_vr * t * r_coseno / 1000);
            yr = m_yr - (int) Math.floor(m_vr * t * r_seno / 1000);
            if (debug) System.out.println("  -->Ball : " + xb + ", " + yb + ")");
            if (debug) System.out.println("  -->Robot: " + xr + ", " + yr + ")");
            d2 = (xb - xr) * (xb - xr) + (yb - yr) * (yb - yr);
            if (debug) System.out.println("  d2 was " + d2 + "  " + t + "  ms ago");
            if (d2 < 12000) t0 = t; else t1 = t;
        }
        m_x = xb;
        m_y = yb;
        if (debug) System.out.println("D2 from " + m_d2 + " to " + d2 + " Impact was " + t + " ms ago. pos corrected from (" + m_x + ", " + m_y + ") to (" + xb + ", " + yb + ")");
        m_elapsed = t;
    }

    private static VRWBall ref = null;
}
