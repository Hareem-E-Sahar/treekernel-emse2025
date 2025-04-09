package hsvin.reader;

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.RandomAccessFile;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import hsvin.player.videoPlayer;
import hsvin.util.videoCodec;

public class fsvfReader implements videoReader {

    private DataInputStream input;

    private BufferedImage image;

    private File localfile;

    private RandomAccessFile indexer;

    private videoPlayer vp;

    private int numberOfFrames;

    private int width = 320, height = 240;

    private final int FRAMESIZE = 230408;

    private final int sizeOfInteger = 4;

    private int currentFrameNumber, nextFrameNumber;

    private int currentTimeStamp, nextTimeStamp;

    private byte[] currentPixels;

    public fsvfReader(videoPlayer vp) {
        this.vp = vp;
        this.currentPixels = new byte[3 * width * height];
    }

    public boolean setVideoStream(File file) {
        try {
            input = new DataInputStream(new FileInputStream(file));
            indexer = new RandomAccessFile(file, "r");
            this.localfile = file;
            this.numberOfFrames = input.readInt();
            System.out.printf("%d frames. Video length: %d milliseconds\n", numberOfFrames, input.readInt());
            input.skipBytes(this.sizeOfInteger * this.numberOfFrames);
            this.currentFrameNumber = input.readInt();
            this.currentTimeStamp = input.readInt();
            input.read(this.currentPixels);
            image = videoCodec.decodeByteArray(currentPixels, width, height);
        } catch (FileNotFoundException e) {
            System.err.println("Caught inside fsvfReader.setVideoStream()");
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.err.println("Caught inside fsvfReader.setVideoStream()");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void play() {
        try {
            this.nextFrameNumber = input.readInt();
            this.nextTimeStamp = input.readInt();
        } catch (EOFException e) {
            System.err.println("Will display the last frame next");
        } catch (IOException e) {
            System.err.println("Caught inside fsvfReader.play()");
            e.printStackTrace();
        } finally {
            System.out.printf("\nFrame number: %d, created on %d, RGB Color(%d, %d, %d)\n", this.currentFrameNumber, this.currentTimeStamp, 128 + currentPixels[230397], 128 + currentPixels[230398], 128 + currentPixels[230399]);
            vp.setDisplayImage(image, this.currentFrameNumber, (int) (this.currentTimeStamp), (int) (this.nextTimeStamp - this.currentTimeStamp));
            this.currentFrameNumber = this.nextFrameNumber;
            this.currentTimeStamp = this.nextTimeStamp;
            try {
                input.read(this.currentPixels);
                image = videoCodec.decodeByteArray(currentPixels, width, height);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            input = new DataInputStream(new FileInputStream(localfile));
            System.out.printf("%d frames, duration: %d milliseconds.\n", input.readInt(), input.readInt());
            input.skipBytes(this.sizeOfInteger * this.numberOfFrames);
            input.skipBytes((number - 1) * this.FRAMESIZE);
            this.currentFrameNumber = input.readInt();
            this.currentTimeStamp = input.readInt();
            input.read(this.currentPixels);
            image = videoCodec.decodeByteArray(currentPixels, width, height);
            play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekByTimeStamp(int number) {
        System.out.printf("\nTimeStamp slider released at value:  %d\n", number);
        try {
            int currentTS;
            int indexPointer = 8, initialFrame = 1, finalFrame = this.numberOfFrames;
            int median = (initialFrame + finalFrame) / 2;
            int currentSeekPosition = indexPointer + sizeOfInteger * (median - 1);
            System.out.printf("\nMedian : %d, CurrentSeekPosition : %d\n", median, currentSeekPosition);
            while (initialFrame <= finalFrame) {
                indexer.seek(currentSeekPosition);
                currentTS = indexer.readInt();
                System.out.printf("FrameNumber : %d, TimeStamp: %d.\n", median, currentTS);
                System.out.println();
                if (number > currentTS) initialFrame = median + 1; else if (number < currentTS) finalFrame = median - 1; else {
                    initialFrame = median + 1;
                    finalFrame = median - 1;
                }
                median = (initialFrame + finalFrame) / 2;
                currentSeekPosition = indexPointer + sizeOfInteger * (median - 1);
                System.out.printf("Median : %d, CurrentSeekPosition : %d\n", median, currentSeekPosition);
            }
            System.out.printf("\nRequired frame number:  %d\n", median);
            seekByFrameNumber(median);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
