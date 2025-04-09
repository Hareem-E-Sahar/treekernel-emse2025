package hsvin.reader;

import hsvin.player.videoPlayer;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.stream.FileImageInputStream;

public class vv2LDMReader implements videoReader {

    private videoPlayer vp;

    private FileImageInputStream input;

    private Image currentImage;

    private final int sizeOfLong = 8;

    private final int sizeOfInt = 4;

    private final int blockSize = 100;

    private int numberOfFrames, duration;

    private int numberOfBlocks, currentBlock;

    private int currentFrameNumber, frameInBlock;

    private int currentTimeStamp, nextTimeStamp;

    private byte[] b;

    private long[] blockPointerList;

    private long[][] framePointerList;

    private int[] blockTSList;

    private int[][] timeStampList;

    public vv2LDMReader(videoPlayer vp) {
        this.vp = vp;
    }

    public boolean setVideoStream(File file) {
        try {
            input = new FileImageInputStream(file);
            System.out.printf("Length of file: %d bytes\n", file.length());
            this.numberOfFrames = input.readInt();
            this.duration = input.readInt();
            this.numberOfBlocks = input.readInt();
            this.blockPointerList = new long[this.numberOfBlocks];
            this.blockTSList = new int[this.numberOfBlocks];
            this.framePointerList = new long[this.numberOfBlocks][this.blockSize];
            this.timeStampList = new int[this.numberOfBlocks][this.blockSize + 1];
            for (int i = 0, n = 0, m = 0; i < this.numberOfBlocks; i++) {
                input.seek(3 * this.sizeOfInt + i * this.sizeOfLong);
                this.blockPointerList[i] = input.readLong();
                input.seek(this.blockPointerList[i]);
                for (int j = 0; j < this.blockSize && n < this.numberOfFrames; j++, n++) this.framePointerList[i][j] = input.readLong();
                if (n == this.numberOfFrames) input.seek(this.blockPointerList[i] + this.blockSize * this.sizeOfLong);
                for (int k = 0; k <= this.blockSize && m <= this.numberOfFrames; k++, m++) this.timeStampList[i][k] = input.readInt();
                m--;
                this.blockTSList[i] = this.timeStampList[i][0];
            }
            this.currentBlock = 1;
            System.out.printf("%d frames, video duration %d milliseconds, number of blocks: %d\n", numberOfFrames, duration, numberOfBlocks);
            input.seek(this.framePointerList[0][0]);
            this.currentFrameNumber = 1;
            this.currentTimeStamp = this.timeStampList[this.currentBlock - 1][this.currentFrameNumber - 1];
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
            this.frameInBlock = this.currentFrameNumber % this.blockSize;
            this.frameInBlock = frameInBlock == 0 ? this.blockSize : frameInBlock;
            this.nextTimeStamp = this.timeStampList[this.currentBlock - 1][frameInBlock];
            if (nextTimeStamp == -1) {
                this.currentBlock++;
                input.seek(this.framePointerList[this.currentBlock - 1][0]);
                this.nextTimeStamp = this.timeStampList[this.currentBlock - 1][0];
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
            this.frameInBlock = (temp == 0) ? this.blockSize : temp;
            this.currentFrameNumber = number;
            System.out.printf("Current block: %d\n", this.currentBlock);
            input.seek(this.framePointerList[this.currentBlock - 1][frameInBlock - 1]);
            this.currentTimeStamp = this.timeStampList[this.currentBlock - 1][frameInBlock - 1];
            play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void seekByTimeStamp(int number) {
        System.out.printf("\nTimeStamp slider released at value:  %d\n", number);
        this.currentBlock = binaryChop(number, 1, this.numberOfBlocks, this.blockTSList);
        int initialFrame = (this.currentBlock - 1) * this.blockSize + 1;
        int finalFrame = this.numberOfFrames % this.blockSize;
        finalFrame = finalFrame == 0 ? this.blockSize : finalFrame;
        seekByFrameNumber(binaryChop(number, initialFrame, finalFrame, this.timeStampList[this.currentBlock - 1]));
    }

    private int binaryChop(int number, int iValue, int fValue, int[] array) {
        int currentTS;
        int initialValue = 1;
        int finalValue = fValue;
        int median = (initialValue + finalValue) / 2;
        System.out.printf("\nMedian : %d\n", median + iValue - 1);
        while (initialValue <= finalValue) {
            currentTS = array[median - 1];
            System.out.printf("SeekByNumber : %d, TimeStamp: %d.\n", median + iValue - 1, currentTS);
            System.out.println();
            if (number > currentTS) initialValue = median + 1; else if (number < currentTS) finalValue = median - 1; else {
                initialValue = median + 1;
                finalValue = median - 1;
            }
            median = (initialValue + finalValue) / 2;
            System.out.printf("Median : %d\n", median + iValue - 1);
        }
        System.out.printf("\nRequired number:  %d\n", median + iValue - 1);
        return median + iValue - 1;
    }
}
