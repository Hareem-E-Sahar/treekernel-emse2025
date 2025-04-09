package hsvin.reader;

import javax.imageio.stream.FileImageInputStream;
import hsvin.player.videoPlayer;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class vsvfReader implements videoReader {

    private videoPlayer vp;

    private RandomAccessFile randFile;

    private FileImageInputStream input;

    private Image currentImage;

    private final int sizeOfLong = 8;

    private final int sizeOfInt = 4;

    private int numberOfFrames, duration;

    private int currentFrameNumber;

    private int currentTimeStamp, nextTimeStamp;

    private int timeStampListPointer;

    private byte[] b;

    public vsvfReader(videoPlayer vp) {
        this.vp = vp;
        System.out.println("vsvfReader is loaded");
    }

    public boolean setVideoStream(File file) {
        try {
            input = new FileImageInputStream(file);
            System.out.printf("Length of file: %d bytes\n", file.length());
            this.randFile = new RandomAccessFile(file, "r");
            this.numberOfFrames = input.readInt();
            this.duration = input.readInt();
            System.out.printf("%d frames, video duration %d milliseconds\n", numberOfFrames, duration);
            input.seek(input.readLong());
            this.currentFrameNumber = 1;
            this.timeStampListPointer = (this.numberOfFrames + 1) * this.sizeOfLong;
            this.randFile.seek(this.timeStampListPointer);
            this.currentTimeStamp = randFile.readInt();
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
            this.nextTimeStamp = randFile.readInt();
            if (nextTimeStamp == -1) this.nextTimeStamp = this.duration;
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
            input.seek(number * this.sizeOfLong);
            input.seek(input.readLong());
            randFile.seek(this.timeStampListPointer + (number - 1) * this.sizeOfInt);
            this.currentTimeStamp = randFile.readInt();
            play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void oldFashionedSeekByTimeStamp(int number) {
        try {
            int frameNumber = 0;
            int currentTS = 0;
            randFile.seek(this.timeStampListPointer);
            while (currentTS <= number) {
                System.out.printf("Current timestamp: %d\n", currentTS);
                currentTS = randFile.readInt();
                frameNumber++;
            }
            seekByFrameNumber(frameNumber - 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekByTimeStamp(int number) {
        System.out.printf("\nTimeStamp slider released at value:  %d\n", number);
        try {
            int currentTS;
            int initialFrame = 1, finalFrame = this.numberOfFrames;
            int median = (initialFrame + finalFrame) / 2;
            int currentSeekPosition = this.timeStampListPointer + this.sizeOfInt * (median - 1);
            System.out.printf("\nMedian : %d, CurrentSeekPosition : %d\n", median, currentSeekPosition);
            while (initialFrame <= finalFrame) {
                randFile.seek(currentSeekPosition);
                currentTS = randFile.readInt();
                System.out.printf("FrameNumber : %d, TimeStamp: %d.\n", median, currentTS);
                System.out.println();
                if (number > currentTS) initialFrame = median + 1; else if (number < currentTS) finalFrame = median - 1; else {
                    initialFrame = median + 1;
                    finalFrame = median - 1;
                }
                median = (initialFrame + finalFrame) / 2;
                currentSeekPosition = this.timeStampListPointer + this.sizeOfInt * (median - 1);
                System.out.printf("Median : %d, CurrentSeekPosition : %d\n", median, currentSeekPosition);
            }
            System.out.printf("\nRequired frame number:  %d\n", median);
            seekByFrameNumber(median);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
