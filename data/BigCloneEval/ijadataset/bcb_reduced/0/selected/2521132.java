package javafx_20092010_reeks2.samsegers.Screencapturing.AVIpack;

import javafx_20092010_reeks2.samsegers.Screencapturing.AVIpack.AVIOutputStream.VideoFormat;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.*;
import javax.swing.Timer;

/**
 * @author Samjay
 */
public class Video {

    private VideoFormat format;

    private File file;

    private float quality;

    private Timer timer;

    private String naam;

    private Rectangle bounds;

    private AVIOutputStream out = null;

    private Robot robot;

    public Video(String naam) {
        try {
            this.naam = naam;
            this.format = AVIOutputStream.VideoFormat.JPG;
            this.quality = (float) 0.3;
            this.timer = new Timer(250, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    try {
                        Video.this.addPicture(Picture.capture(Video.this));
                    } catch (Exception ex) {
                        Logger.getLogger(Video.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            this.robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(Video.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public VideoFormat getFormat() {
        return format;
    }

    public void setFormat(VideoFormat format) {
        this.format = format;
    }

    public float getQuality() {
        return quality;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        this.bounds = bounds;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public AVIOutputStream getOut() {
        return out;
    }

    public void setOut(AVIOutputStream out) {
        this.out = out;
    }

    public Robot getRobot() {
        return robot;
    }

    public void setRobot(Robot robot) {
        this.robot = robot;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public void setQuality(float quality) {
        this.quality = quality;
    }

    public void addPicture(File file) throws IOException {
        out.writeFrame(file);
    }

    public void addPicture() throws IOException {
        if (out == null) {
            this.start(bounds);
        }
        out.writeFrame(robot.createScreenCapture(bounds));
    }

    public void addPicture(BufferedImage image) throws IOException {
        if (out == null) {
            this.start(bounds);
        }
        out.writeFrame(image);
    }

    public Dimension getDimension() {
        return new Dimension(600, 460);
    }

    public Point getPoint() {
        return new Point(20, 10);
    }

    public void start(Rectangle bounds) throws IOException {
        if (out == null) {
            this.file = new File(naam + ".avi");
            out = new AVIOutputStream(file, format);
            out.setVideoCompressionQuality(quality);
            out.setVideoDimension(bounds.getSize());
            out.setTimeScale(4);
            out.setFrameRate(12);
            this.bounds = bounds;
            timer.start();
        } else {
            this.pause();
        }
    }

    public void pause() {
        if (out != null) {
            if (timer.isRunning()) {
                timer.stop();
            } else {
                timer.restart();
            }
        }
    }

    public void stop() throws IOException {
        if (out != null) {
            if (timer.isRunning()) {
                timer.stop();
            }
            out.close();
        }
    }
}
