package hci.gnomex.useq.data;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import hci.gnomex.useq.*;

/**Container for a sorted PositionScore[] and it's associated meta data.
 * @author david.nix@hci.utah.edu*/
public class PositionScoreData extends USeqData implements Comparable<PositionScoreData> {

    private PositionScore[] sortedPositionScores;

    private int[] basePositions;

    private float[] scores;

    public PositionScoreData() {
    }

    /**Note, be sure to sort the PositionScore[].*/
    public PositionScoreData(PositionScore[] sortedPositionScores, SliceInfo sliceInfo) {
        this.sortedPositionScores = sortedPositionScores;
        this.sliceInfo = sliceInfo;
    }

    public PositionScoreData(File binaryFile) throws IOException {
        sliceInfo = new SliceInfo(binaryFile.getName());
        read(binaryFile);
    }

    public PositionScoreData(DataInputStream dis, SliceInfo sliceInfo) {
        this.sliceInfo = sliceInfo;
        read(dis);
    }

    public PositionScoreData(int[] positions, float[] scoreArray, SliceInfo sliceInfo) {
        this.sliceInfo = sliceInfo;
        sortedPositionScores = new PositionScore[basePositions.length];
        for (int i = 0; i < sortedPositionScores.length; i++) sortedPositionScores[i] = new PositionScore(positions[i], scoreArray[i]);
    }

    /**Writes slices of data to the save directory, adds the entries to the ArrayList.*/
    public void sliceWritePositionScoreData(int rowChunkSize, File saveDirectory, ArrayList<File> files2Zip) {
        int beginningIndex = 0;
        int endIndex = 0;
        int numberPositions = sortedPositionScores.length;
        while (true) {
            PositionScore[] slice;
            if (rowChunkSize == -1) {
                beginningIndex = 0;
                endIndex = numberPositions;
                slice = sortedPositionScores;
            } else {
                beginningIndex = endIndex;
                endIndex = beginningIndex + rowChunkSize;
                if (endIndex > numberPositions) {
                    endIndex = numberPositions;
                } else {
                    int endBP = sortedPositionScores[endIndex - 1].getPosition();
                    for (int i = endIndex; i < numberPositions; i++) {
                        if (sortedPositionScores[i].getPosition() != endBP) {
                            break;
                        }
                        endIndex++;
                    }
                }
                int num = endIndex - beginningIndex;
                slice = new PositionScore[num];
                System.arraycopy(sortedPositionScores, beginningIndex, slice, 0, num);
            }
            PositionScoreData.updateSliceInfo(slice, sliceInfo);
            PositionScoreData pd = new PositionScoreData(slice, sliceInfo);
            File savedFile = pd.write(saveDirectory, true);
            files2Zip.add(savedFile);
            if (endIndex == numberPositions) break;
        }
    }

    /**Updates the SliceInfo setting just the FirstStartPosition, LastStartPosition, and NumberRecords.*/
    public static void updateSliceInfo(PositionScore[] sortedPositionScores, SliceInfo sliceInfo) {
        sliceInfo.setFirstStartPosition(sortedPositionScores[0].position);
        sliceInfo.setLastStartPosition(sortedPositionScores[sortedPositionScores.length - 1].position);
        sliceInfo.setNumberRecords(sortedPositionScores.length);
    }

    /**By position, smallest to largest, assumes same chromosome strand.*/
    public int compareTo(PositionScoreData other) {
        if (sortedPositionScores[0].position < other.sortedPositionScores[0].position) return -1;
        if (sortedPositionScores[0].position > other.sortedPositionScores[0].position) return 1;
        return 0;
    }

    public int[] getBasePositions() {
        if (basePositions == null) {
            basePositions = new int[sortedPositionScores.length];
            scores = new float[sortedPositionScores.length];
            for (int i = 0; i < basePositions.length; i++) {
                basePositions[i] = sortedPositionScores[i].position;
                scores[i] = sortedPositionScores[i].score;
            }
        }
        return basePositions;
    }

    public float[] getBaseScores() {
        if (scores == null) getBasePositions();
        return scores;
    }

    /**Assumes all are of the same chromosome and strand! Sorts PositionScoreData prior to merging*/
    public static PositionScoreData merge(ArrayList<PositionScoreData> pdAL) {
        PositionScoreData[] pdArray = new PositionScoreData[pdAL.size()];
        pdAL.toArray(pdArray);
        Arrays.sort(pdArray);
        int num = 0;
        for (int i = 0; i < pdArray.length; i++) num += pdArray[i].sortedPositionScores.length;
        PositionScore[] concatinate = new PositionScore[num];
        int index = 0;
        for (int i = 0; i < pdArray.length; i++) {
            PositionScore[] slice = pdArray[i].sortedPositionScores;
            System.arraycopy(slice, 0, concatinate, index, slice.length);
            index += slice.length;
        }
        SliceInfo sliceInfo = pdArray[0].sliceInfo;
        PositionScoreData.updateSliceInfo(concatinate, sliceInfo);
        return new PositionScoreData(concatinate, sliceInfo);
    }

    public static PositionScoreData mergeUSeqData(ArrayList<USeqData> useqDataAL) {
        int num = useqDataAL.size();
        ArrayList<PositionScoreData> a = new ArrayList<PositionScoreData>(num);
        for (int i = 0; i < num; i++) a.add((PositionScoreData) useqDataAL.get(i));
        return merge(a);
    }

    /**Returns the position of the last position in the sortedPositionScores array.*/
    public int fetchLastBase() {
        return sortedPositionScores[sortedPositionScores.length - 1].position;
    }

    /**Writes six column xxx.bed formatted lines to the PrintWriter. Note score must be between 0 and 1000 to meet bed format.*/
    public void writeBed(PrintWriter out, boolean fixBedScores) {
        String chrom = sliceInfo.getChromosome();
        String strand = sliceInfo.getStrand();
        for (int i = 0; i < sortedPositionScores.length; i++) {
            if (fixBedScores) {
                int score = USeqUtilities.fixBedScore(sortedPositionScores[i].score);
                out.println(chrom + "\t" + sortedPositionScores[i].position + "\t" + (sortedPositionScores[i].position + 1) + "\t" + ".\t" + score + "\t" + strand);
            } else out.println(chrom + "\t" + sortedPositionScores[i].position + "\t" + (sortedPositionScores[i].position + 1) + "\t" + ".\t" + sortedPositionScores[i].score + "\t" + strand);
        }
    }

    /**Writes native format to the PrintWriter*/
    public void writeNative(PrintWriter out) {
        String chrom = sliceInfo.getChromosome();
        String strand = sliceInfo.getStrand();
        if (strand.equals(".")) {
            out.println("#Chr\tPosition\tScore");
            for (int i = 0; i < sortedPositionScores.length; i++) out.println(chrom + "\t" + sortedPositionScores[i].position + "\t" + sortedPositionScores[i].score);
        } else {
            out.println("#Chr\tPosition\tScore\tStrand");
            for (int i = 0; i < sortedPositionScores.length; i++) {
                out.println(chrom + "\t" + sortedPositionScores[i].position + "\t" + sortedPositionScores[i].score + "\t" + strand);
            }
        }
    }

    /**Writes position score format to the PrintWriter, 1bp positions*/
    public void writePositionScore(PrintWriter out) {
        int prior = -1;
        for (int i = 0; i < sortedPositionScores.length; i++) {
            if (prior != sortedPositionScores[i].position) {
                out.println((sortedPositionScores[i].position + 1) + "\t" + sortedPositionScores[i].score);
                prior = sortedPositionScores[i].position;
            }
        }
    }

    /**Writes the PositionScore[] to a binary file.
	 * @param saveDirectory, the binary file will be written using the chromStrandStartBP-StopBP.extension notation to this directory
	 * @param attemptToSaveAsShort, scans to see if the offsets exceed 65536 bp, a bit slower to write but potentially a considerable size reduction, set to false for max speed
	 * @return the binaryFile written to the saveDirectory
	 * */
    public File write(File saveDirectory, boolean attemptToSaveAsShort) {
        boolean useShort = false;
        if (attemptToSaveAsShort) {
            int bp = sortedPositionScores[0].position;
            useShort = true;
            for (int i = 1; i < sortedPositionScores.length; i++) {
                int currentStart = sortedPositionScores[i].position;
                int diff = currentStart - bp;
                if (diff > 65536) {
                    useShort = false;
                    break;
                }
                bp = currentStart;
            }
        }
        String fileType;
        if (useShort) fileType = USeqUtilities.SHORT + USeqUtilities.FLOAT; else fileType = USeqUtilities.INT + USeqUtilities.FLOAT;
        sliceInfo.setBinaryType(fileType);
        binaryFile = new File(saveDirectory, sliceInfo.getSliceName());
        FileOutputStream workingFOS = null;
        DataOutputStream workingDOS = null;
        try {
            workingFOS = new FileOutputStream(binaryFile);
            workingDOS = new DataOutputStream(new BufferedOutputStream(workingFOS));
            workingDOS.writeUTF(header);
            workingDOS.writeInt(sortedPositionScores[0].position);
            workingDOS.writeFloat(sortedPositionScores[0].score);
            if (useShort) {
                int bp = sortedPositionScores[0].position;
                for (int i = 1; i < sortedPositionScores.length; i++) {
                    int currentStart = sortedPositionScores[i].position;
                    int diff = currentStart - bp - 32768;
                    workingDOS.writeShort((short) (diff));
                    workingDOS.writeFloat(sortedPositionScores[i].score);
                    bp = currentStart;
                }
            } else {
                int bp = sortedPositionScores[0].position;
                for (int i = 1; i < sortedPositionScores.length; i++) {
                    int currentStart = sortedPositionScores[i].position;
                    int diff = currentStart - bp;
                    workingDOS.writeInt(diff);
                    workingDOS.writeFloat(sortedPositionScores[i].score);
                    bp = currentStart;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            USeqUtilities.safeClose(workingDOS);
            USeqUtilities.safeClose(workingFOS);
        }
        return binaryFile;
    }

    /**Writes the PositionScore[] to a ZipOutputStream.
	 * @param	attemptToSaveAsShort	if true, scans to see if the offsets exceed 65536 bp, a bit slower to write but potentially a considerable size reduction, set to false for max speed
	 * */
    public void write(ZipOutputStream out, DataOutputStream dos, boolean attemptToSaveAsShort) {
        boolean useShort = false;
        if (attemptToSaveAsShort) {
            int bp = sortedPositionScores[0].position;
            useShort = true;
            for (int i = 1; i < sortedPositionScores.length; i++) {
                int currentStart = sortedPositionScores[i].position;
                int diff = currentStart - bp;
                if (diff > 65536) {
                    useShort = false;
                    break;
                }
                bp = currentStart;
            }
        }
        String fileType;
        if (useShort) fileType = USeqUtilities.SHORT + USeqUtilities.FLOAT; else fileType = USeqUtilities.INT + USeqUtilities.FLOAT;
        sliceInfo.setBinaryType(fileType);
        binaryFile = null;
        try {
            out.putNextEntry(new ZipEntry(sliceInfo.getSliceName()));
            dos.writeUTF(header);
            dos.writeInt(sortedPositionScores[0].position);
            dos.writeFloat(sortedPositionScores[0].score);
            if (useShort) {
                int bp = sortedPositionScores[0].position;
                for (int i = 1; i < sortedPositionScores.length; i++) {
                    int currentStart = sortedPositionScores[i].position;
                    int diff = currentStart - bp - 32768;
                    dos.writeShort((short) (diff));
                    dos.writeFloat(sortedPositionScores[i].score);
                    bp = currentStart;
                }
            } else {
                int bp = sortedPositionScores[0].position;
                for (int i = 1; i < sortedPositionScores.length; i++) {
                    int currentStart = sortedPositionScores[i].position;
                    int diff = currentStart - bp;
                    dos.writeInt(diff);
                    dos.writeFloat(sortedPositionScores[i].score);
                    bp = currentStart;
                }
            }
            out.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
            USeqUtilities.safeClose(out);
            USeqUtilities.safeClose(dos);
        }
    }

    /**Reads a DataInputStream into this PositionScoreData.*/
    public void read(DataInputStream dis) {
        try {
            header = dis.readUTF();
            int numberPositions = sliceInfo.getNumberRecords();
            sortedPositionScores = new PositionScore[numberPositions];
            sortedPositionScores[0] = new PositionScore(dis.readInt(), dis.readFloat());
            String fileType = sliceInfo.getBinaryType();
            if (USeqUtilities.POSITION_SCORE_INT_FLOAT.matcher(fileType).matches()) {
                for (int i = 1; i < numberPositions; i++) {
                    sortedPositionScores[i] = new PositionScore(sortedPositionScores[i - 1].position + dis.readInt(), dis.readFloat());
                }
            } else if (USeqUtilities.POSITION_SCORE_SHORT_FLOAT.matcher(fileType).matches()) {
                for (int i = 1; i < numberPositions; i++) {
                    sortedPositionScores[i] = new PositionScore(sortedPositionScores[i - 1].position + dis.readShort() + 32768, dis.readFloat());
                }
            } else {
                throw new IOException("Incorrect file type for creating a PositionScore[] -> '" + fileType + "' in " + binaryFile + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            USeqUtilities.safeClose(dis);
        }
    }

    public PositionScore[] getPositionScores() {
        return sortedPositionScores;
    }

    public void setPositionScores(PositionScore[] sortedPositionScores) {
        this.sortedPositionScores = sortedPositionScores;
        updateSliceInfo(sortedPositionScores, sliceInfo);
    }

    /**Returns whether data remains.*/
    public boolean trim(int beginningBP, int endingBP) {
        ArrayList<PositionScore> al = new ArrayList<PositionScore>();
        for (int i = 0; i < sortedPositionScores.length; i++) {
            if (sortedPositionScores[i].isContainedBy(beginningBP, endingBP)) al.add(sortedPositionScores[i]);
        }
        if (al.size() == 0) return false;
        sortedPositionScores = new PositionScore[al.size()];
        al.toArray(sortedPositionScores);
        updateSliceInfo(sortedPositionScores, sliceInfo);
        return true;
    }
}
