package hsvin.reader;

import hsvin.player.videoPlayer;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.imageio.stream.FileImageInputStream;

public class vv2Reader implements videoReader {

    private videoPlayer vp;

    private RandomAccessFile randFile;

    private FileImageInputStream input;

    private Image currentImage;

    private final int sizeOfLong = 8;

    private final int sizeOfInt = 4;

    private final int blockSize = 100, maxBlocks = 100;

    private int numberOfFrames, duration;

    private int numberOfBlocks, currentBlock;

    private int currentFrameNumber;

    private int currentTimeStamp, nextTimeStamp;

    private long currentBlockLocation;

    private byte[] b;

    public vv2Reader(videoPlayer vp) {
        this.vp = vp;
    }

    public boolean setVideoStream(File file) {
        try {
            input = new FileImageInputStream(file);
            System.out.printf("Length of file: %d bytes\n", file.length());
            this.randFile = new RandomAccessFile(file, "r");
            this.numberOfFrames = input.readInt();
            this.duration = input.readInt();
            this.numberOfBlocks = input.readInt();
            this.currentBlock = 1;
            System.out.printf("%d frames, video duration %d milliseconds, number of blocks: %d\n", numberOfFrames, duration, numberOfBlocks);
            this.currentBlockLocation = input.readLong();
            input.seek(this.currentBlockLocation);
            input.seek(input.readLong());
            this.currentFrameNumber = 1;
            this.randFile.seek(this.currentBlockLocation + this.blockSize * this.sizeOfLong);
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
            if (nextTimeStamp == -1) {
                randFile.seek(this.sizeOfInt * 3 + currentBlock * this.sizeOfLong);
                this.currentBlockLocation = randFile.readLong();
                randFile.seek(this.currentBlockLocation);
                input.seek(randFile.readLong());
                randFile.seek(this.currentBlockLocation + this.blockSize * this.sizeOfLong);
                this.nextTimeStamp = randFile.readInt();
                this.currentBlock++;
            } else if (nextTimeStamp == -2) this.nextTimeStamp = this.duration;
            System.out.printf("FrameNumber : %d, TimeStamp: %d, Size: %d\n", this.currentFrameNumber, this.currentTimeStamp, this.b.length);
            vp.setDisplayImage(this.currentImage, this.currentFrameNumber, this.currentTimeStamp, (this.nextTimeStamp - this.currentTimeStamp));
            this.currentFrameNumber++;
            this.currentTimeStamp = this.nextTimeStamp;
            System.out.println();
        } catch (IOException e) {
            System.err.println(e + " caught inside play()");
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
            int temp = number % this.blockSize;
            this.currentBlock = (temp == 0) ? number / this.blockSize : number / this.blockSize + 1;
            int frameInBlock = (temp == 0) ? this.blockSize : temp;
            this.currentFrameNumber = number;
            randFile.seek(this.currentBlock * this.sizeOfLong + this.sizeOfInt);
            this.currentBlockLocation = randFile.readLong();
            System.out.printf("Current block: %d, Location: %d\n", this.currentBlock, this.currentBlockLocation);
            input.seek(this.currentBlockLocation + (frameInBlock - 1) * this.sizeOfLong);
            input.seek(input.readLong());
            randFile.seek(this.currentBlockLocation + this.blockSize * this.sizeOfLong + (frameInBlock - 1) * this.sizeOfInt);
            this.currentTimeStamp = randFile.readInt();
            play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekByTimeStamp(int number) {
        System.out.printf("\nTimeStamp slider released at value:  %d\n", number);
        long indexPointer = this.sizeOfInt * 3 + this.maxBlocks * this.sizeOfLong;
        this.currentBlock = binaryChop(number, 1, this.numberOfBlocks, indexPointer);
        try {
            randFile.seek(this.sizeOfInt * 3 + (this.currentBlock - 1) * this.sizeOfLong);
            this.currentBlockLocation = randFile.readLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int initialFrame = (this.currentBlock - 1) * this.blockSize + 1;
        indexPointer = this.currentBlockLocation + this.blockSize * this.sizeOfLong;
        int finalFrame = this.numberOfFrames % this.blockSize == 0 ? this.currentBlock * this.blockSize : this.currentBlock == this.numberOfBlocks ? this.numberOfFrames : this.currentBlock * this.blockSize;
        System.out.printf("Initial frame: %d, final frame: %d\n", initialFrame, finalFrame);
        seekByFrameNumber(binaryChop(number, initialFrame, finalFrame, indexPointer));
    }

    private int binaryChop(int number, int iValue, int fValue, long indexPointer) {
        int currentTS;
        int initialValue = iValue;
        int finalValue = fValue;
        int median = (initialValue + finalValue) / 2;
        long currentSeekPosition = indexPointer + sizeOfInt * (median - iValue);
        System.out.printf("\nMedian : %d, CurrentSeekPosition : %d\n", median, currentSeekPosition);
        try {
            while (initialValue <= finalValue) {
                randFile.seek(currentSeekPosition);
                currentTS = randFile.readInt();
                System.out.printf("SeekByNumber : %d, TimeStamp: %d.\n", median, currentTS);
                System.out.println();
                if (number > currentTS) initialValue = median + 1; else if (number < currentTS) finalValue = median - 1; else {
                    initialValue = median + 1;
                    finalValue = median - 1;
                }
                median = (initialValue + finalValue) / 2;
                currentSeekPosition = indexPointer + sizeOfInt * (median - iValue);
                System.out.printf("Median : %d, CurrentSeekPosition : %d\n", median, currentSeekPosition);
            }
        } catch (IOException e) {
            System.err.println(e + " caught inside binaryChop()");
        }
        System.out.printf("\nRequired number:  %d\n", median);
        return median;
    }
}
