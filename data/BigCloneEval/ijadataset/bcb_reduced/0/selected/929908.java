package hsvin.reader;

import hsvin.player.videoPlayer;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.stream.FileImageInputStream;

public class vsvfLDMReader implements videoReader {

    private videoPlayer vp;

    private FileImageInputStream input;

    private Image currentImage;

    private int numberOfFrames, duration;

    private int currentFrameNumber;

    private int currentTimeStamp, nextTimeStamp;

    private int[] timeStampList;

    private long[] framePointerList;

    private byte[] b;

    public vsvfLDMReader(videoPlayer vp) {
        this.vp = vp;
        System.out.println("vsvfLDMReader is loaded");
    }

    public boolean setVideoStream(File file) {
        try {
            input = new FileImageInputStream(file);
            System.out.printf("Length of file: %d bytes\n", file.length());
            this.numberOfFrames = input.readInt();
            this.duration = input.readInt();
            System.out.printf("%d frames, video duration %d milliseconds\n", numberOfFrames, duration);
            this.currentFrameNumber = 1;
            this.framePointerList = new long[this.numberOfFrames];
            this.timeStampList = new int[this.numberOfFrames];
            for (int i = 0; i < this.numberOfFrames; i++) this.framePointerList[i] = input.readLong();
            for (int i = 0; i < this.numberOfFrames; i++) this.timeStampList[i] = input.readInt();
            this.currentTimeStamp = this.timeStampList[0];
            input.readInt();
        } catch (FileNotFoundException e) {
            System.err.println(e + " caught inside setVideoStream()");
            return false;
        } catch (IOException e) {
            System.err.println(e + " caught inside setVideoStream()");
            return false;
        }
        return true;
    }

    public void play() {
        try {
            b = new byte[input.readInt()];
            input.read(b);
            this.currentImage = Toolkit.getDefaultToolkit().createImage(b);
            try {
                this.nextTimeStamp = this.timeStampList[this.currentFrameNumber];
            } catch (ArrayIndexOutOfBoundsException e) {
                this.nextTimeStamp = this.duration;
            }
            System.out.printf("FrameNumber : %d, TimeStamp: %d, Size: %d\n", this.currentFrameNumber, this.currentTimeStamp, this.b.length);
            vp.setDisplayImage(this.currentImage, this.currentFrameNumber, this.currentTimeStamp, (this.nextTimeStamp - this.currentTimeStamp));
            this.currentFrameNumber++;
            this.currentTimeStamp = this.nextTimeStamp;
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (input != null) try {
            input.close();
        } catch (IOException e) {
            System.err.println(e + " caught inside fsffReader.stop()");
        }
    }

    public void seekByFrameNumber(int number) {
        System.out.printf("\nFrame slider released at value:  %d\n", number);
        try {
            this.currentFrameNumber = number;
            input.seek(this.framePointerList[number - 1]);
            this.currentTimeStamp = this.timeStampList[number - 1];
            play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekByTimeStamp(int number) {
        System.out.printf("\nTimeStamp slider released at value:  %d\n", number);
        int currentTS;
        int initialFrame = 1, finalFrame = this.numberOfFrames;
        int median = (initialFrame + finalFrame) / 2;
        while (initialFrame <= finalFrame) {
            currentTS = this.timeStampList[median - 1];
            if (number > currentTS) initialFrame = median + 1; else if (number < currentTS) finalFrame = median - 1; else {
                initialFrame = median + 1;
                finalFrame = median - 1;
            }
            median = (initialFrame + finalFrame) / 2;
        }
        seekByFrameNumber(median);
    }
}
