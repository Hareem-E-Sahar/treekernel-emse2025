package edu.utah.seq.useq.data;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import edu.utah.seq.useq.*;

/**Container for a sorted Position[] and it's associated meta data.
 * @author david.nix@hci.utah.edu*/
public class PositionData extends USeqData implements Comparable<PositionData> {

    private Position[] sortedPositions;

    public PositionData() {
    }

    /**Note, be sure to sort the Position[].*/
    public PositionData(Position[] sortedPositions, SliceInfo sliceInfo) {
        this.sortedPositions = sortedPositions;
        this.sliceInfo = sliceInfo;
    }

    public PositionData(File binaryFile) throws IOException {
        sliceInfo = new SliceInfo(binaryFile.getName());
        read(binaryFile);
    }

    public PositionData(DataInputStream dis, SliceInfo sliceInfo) {
        this.sliceInfo = sliceInfo;
        read(dis);
    }

    /**Updates the SliceInfo setting just the FirstStartPosition, LastStartPosition, and NumberRecords.*/
    public static void updateSliceInfo(Position[] sortedPositions, SliceInfo sliceInfo) {
        sliceInfo.setFirstStartPosition(sortedPositions[0].position);
        sliceInfo.setLastStartPosition(sortedPositions[sortedPositions.length - 1].position);
        sliceInfo.setNumberRecords(sortedPositions.length);
    }

    /**Assumes all are of the same chromosome and strand!*/
    public static PositionData merge(ArrayList<PositionData> pdAL) {
        PositionData[] pdArray = new PositionData[pdAL.size()];
        pdAL.toArray(pdArray);
        Arrays.sort(pdArray);
        int num = 0;
        for (int i = 0; i < pdArray.length; i++) num += pdArray[i].sortedPositions.length;
        Position[] concatinate = new Position[num];
        int index = 0;
        for (int i = 0; i < pdArray.length; i++) {
            Position[] slice = pdArray[i].sortedPositions;
            System.arraycopy(slice, 0, concatinate, index, slice.length);
            index += slice.length;
        }
        SliceInfo sliceInfo = pdArray[0].sliceInfo;
        PositionData.updateSliceInfo(concatinate, sliceInfo);
        return new PositionData(concatinate, sliceInfo);
    }

    public static PositionData mergeUSeqData(ArrayList<USeqData> useqDataAL) {
        int num = useqDataAL.size();
        ArrayList<PositionData> a = new ArrayList<PositionData>(num);
        for (int i = 0; i < num; i++) a.add((PositionData) useqDataAL.get(i));
        return merge(a);
    }

    /**By position, smallest to largest, assumes same chromosome strand.*/
    public int compareTo(PositionData other) {
        if (sortedPositions[0].position < other.sortedPositions[0].position) return -1;
        if (sortedPositions[0].position > other.sortedPositions[0].position) return 1;
        return 0;
    }

    /**Writes six column xxx.bed formatted lines to the PrintWriter*/
    public void writeBed(PrintWriter out) {
        String chrom = sliceInfo.getChromosome();
        String strand = sliceInfo.getStrand();
        for (int i = 0; i < sortedPositions.length; i++) {
            out.println(chrom + "\t" + sortedPositions[i].position + "\t" + (sortedPositions[i].position + 1) + "\t" + ".\t0\t" + strand);
        }
    }

    /**Returns the position of the last position in the sortedPositions array.*/
    public int fetchLastBase() {
        return sortedPositions[sortedPositions.length - 1].position;
    }

    /**Writes native format to the PrintWriter*/
    public void writeNative(PrintWriter out) {
        String chrom = sliceInfo.getChromosome();
        String strand = sliceInfo.getStrand();
        if (strand.equals(".")) {
            out.println("#Chr\tPosition");
            for (int i = 0; i < sortedPositions.length; i++) out.println(chrom + "\t" + sortedPositions[i].position);
        } else {
            out.println("#Chr\tPosition\tStrand");
            for (int i = 0; i < sortedPositions.length; i++) {
                out.println(chrom + "\t" + sortedPositions[i].position + "\t" + strand);
            }
        }
    }

    /**Writes native format to the PrintWriter, 1 based, skips dups*/
    public void writePositionScore(PrintWriter out) {
        int priorPosition = -1;
        for (int i = 0; i < sortedPositions.length; i++) {
            if (priorPosition != sortedPositions[i].position) {
                out.println((sortedPositions[i].position + 1) + "\t0");
                priorPosition = sortedPositions[i].position;
            }
        }
    }

    /**Writes the Position[] to a binary file.
	 * @param	saveDirectory	the binary file will be written using the chromStrandStartBP-StopBP.extension notation to this directory
	 * @param	attemptToSaveAsShort	if true, scans to see if the offsets exceed 65536 bp, a bit slower to write but potentially a considerable size reduction, set to false for max speed
	 * @return	the binaryFile written to the saveDirectory
	 * */
    public File write(File saveDirectory, boolean attemptToSaveAsShort) {
        boolean useShort = false;
        if (attemptToSaveAsShort) {
            int bp = sortedPositions[0].position;
            useShort = true;
            for (int i = 1; i < sortedPositions.length; i++) {
                int currentStart = sortedPositions[i].position;
                int diff = currentStart - bp;
                if (diff > 65536) {
                    useShort = false;
                    break;
                }
                bp = currentStart;
            }
        }
        String fileType;
        if (useShort) fileType = USeqUtilities.SHORT; else fileType = USeqUtilities.INT;
        sliceInfo.setBinaryType(fileType);
        binaryFile = new File(saveDirectory, sliceInfo.getSliceName());
        FileOutputStream workingFOS = null;
        DataOutputStream workingDOS = null;
        try {
            workingFOS = new FileOutputStream(binaryFile);
            workingDOS = new DataOutputStream(new BufferedOutputStream(workingFOS));
            workingDOS.writeUTF(header);
            workingDOS.writeInt(sortedPositions[0].position);
            if (useShort) {
                int bp = sortedPositions[0].position;
                for (int i = 1; i < sortedPositions.length; i++) {
                    int currentStart = sortedPositions[i].position;
                    int diff = currentStart - bp - 32768;
                    workingDOS.writeShort((short) (diff));
                    bp = currentStart;
                }
            } else {
                int bp = sortedPositions[0].position;
                for (int i = 1; i < sortedPositions.length; i++) {
                    int currentStart = sortedPositions[i].position;
                    int diff = currentStart - bp;
                    workingDOS.writeInt(diff);
                    bp = currentStart;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            binaryFile = null;
        } finally {
            USeqUtilities.safeClose(workingDOS);
            USeqUtilities.safeClose(workingFOS);
        }
        return binaryFile;
    }

    /**Writes the Position[] to a ZipOutputStream.
	 * @param	attemptToSaveAsShort	if true, scans to see if the offsets exceed 65536 bp, a bit slower to write but potentially a considerable size reduction, set to false for max speed
	 * */
    public void write(ZipOutputStream out, DataOutputStream dos, boolean attemptToSaveAsShort) {
        boolean useShort = false;
        if (attemptToSaveAsShort) {
            int bp = sortedPositions[0].position;
            useShort = true;
            for (int i = 1; i < sortedPositions.length; i++) {
                int currentStart = sortedPositions[i].position;
                int diff = currentStart - bp;
                if (diff > 65536) {
                    useShort = false;
                    break;
                }
                bp = currentStart;
            }
        }
        String fileType;
        if (useShort) fileType = USeqUtilities.SHORT; else fileType = USeqUtilities.INT;
        sliceInfo.setBinaryType(fileType);
        binaryFile = null;
        try {
            out.putNextEntry(new ZipEntry(sliceInfo.getSliceName()));
            dos.writeUTF(header);
            dos.writeInt(sortedPositions[0].position);
            if (useShort) {
                int bp = sortedPositions[0].position;
                for (int i = 1; i < sortedPositions.length; i++) {
                    int currentStart = sortedPositions[i].position;
                    int diff = currentStart - bp - 32768;
                    dos.writeShort((short) (diff));
                    bp = currentStart;
                }
            } else {
                int bp = sortedPositions[0].position;
                for (int i = 1; i < sortedPositions.length; i++) {
                    int currentStart = sortedPositions[i].position;
                    int diff = currentStart - bp;
                    dos.writeInt(diff);
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

    /**Reads a useries xxx.i or xxx.s DataInputStream into this PositionData.*/
    public void read(DataInputStream dis) {
        try {
            header = dis.readUTF();
            int numberPositions = sliceInfo.getNumberRecords();
            sortedPositions = new Position[numberPositions];
            sortedPositions[0] = new Position(dis.readInt());
            String fileType = sliceInfo.getBinaryType();
            if (USeqUtilities.POSITION_INT.matcher(fileType).matches()) {
                for (int i = 1; i < numberPositions; i++) {
                    sortedPositions[i] = new Position(sortedPositions[i - 1].getPosition() + dis.readInt());
                }
            } else if (USeqUtilities.POSITION_SHORT.matcher(fileType).matches()) {
                for (int i = 1; i < numberPositions; i++) {
                    sortedPositions[i] = new Position(sortedPositions[i - 1].getPosition() + dis.readShort() + 32768);
                }
            } else {
                throw new IOException("Incorrect file type for creating a Position[] -> '" + fileType + "' in " + binaryFile + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            USeqUtilities.safeClose(dis);
        }
    }

    public Position[] getPositions() {
        return sortedPositions;
    }

    public void setPositions(Position[] sortedPositions) {
        this.sortedPositions = sortedPositions;
        updateSliceInfo(sortedPositions, sliceInfo);
    }

    /**Returns whether data remains.*/
    public boolean trim(int beginningBP, int endingBP) {
        ArrayList<Position> al = new ArrayList<Position>();
        for (int i = 0; i < sortedPositions.length; i++) {
            if (sortedPositions[i].isContainedBy(beginningBP, endingBP)) al.add(sortedPositions[i]);
        }
        if (al.size() == 0) return false;
        sortedPositions = new Position[al.size()];
        al.toArray(sortedPositions);
        updateSliceInfo(sortedPositions, sliceInfo);
        return true;
    }
}
