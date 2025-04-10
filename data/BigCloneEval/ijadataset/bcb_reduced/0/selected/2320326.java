package hci.gnomex.useq.data;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import hci.gnomex.useq.*;
import hci.gnomex.useq.apps.*;

/**Container for a sorted RegionScoreText[].
 * @author david.nix@hci.utah.edu*/
public class RegionScoreTextData extends USeqData {

    private RegionScoreText[] sortedRegionScoreTexts;

    public RegionScoreTextData() {
    }

    /**Note, be sure to sort the RegionScoreText[].*/
    public RegionScoreTextData(RegionScoreText[] sortedRegionScoreTexts, SliceInfo sliceInfo) {
        this.sortedRegionScoreTexts = sortedRegionScoreTexts;
        this.sliceInfo = sliceInfo;
    }

    public RegionScoreTextData(File binaryFile) throws IOException {
        sliceInfo = new SliceInfo(binaryFile.getName());
        read(binaryFile);
    }

    public RegionScoreTextData(DataInputStream dis, SliceInfo sliceInfo) {
        this.sliceInfo = sliceInfo;
        read(dis);
    }

    /**Updates the SliceInfo setting just the FirstStartPosition, LastStartPosition, and NumberRecords.*/
    public static void updateSliceInfo(RegionScoreText[] sortedRegionScoreTexts, SliceInfo sliceInfo) {
        sliceInfo.setFirstStartPosition(sortedRegionScoreTexts[0].getStart());
        sliceInfo.setLastStartPosition(sortedRegionScoreTexts[sortedRegionScoreTexts.length - 1].start);
        sliceInfo.setNumberRecords(sortedRegionScoreTexts.length);
    }

    /**Returns the bp of the last end position in the array.*/
    public int fetchLastBase() {
        int lastBase = -1;
        for (RegionScoreText r : sortedRegionScoreTexts) {
            int end = r.getStop();
            if (end > lastBase) lastBase = end;
        }
        return lastBase;
    }

    /**Writes six or 12 column xxx.bed formatted lines to the PrintWriter*/
    public void writeBed(PrintWriter out, boolean fixScore) {
        String chrom = sliceInfo.getChromosome();
        String strand = sliceInfo.getStrand();
        for (int i = 0; i < sortedRegionScoreTexts.length; i++) {
            String[] tokens = Text2USeq.PATTERN_TAB.split(sortedRegionScoreTexts[i].text);
            if (fixScore) {
                int score = USeqUtilities.fixBedScore(sortedRegionScoreTexts[i].score);
                if (tokens.length == 7) out.println(chrom + "\t" + sortedRegionScoreTexts[i].start + "\t" + sortedRegionScoreTexts[i].stop + "\t" + tokens[0] + "\t" + score + "\t" + strand + "\t" + tokens[1] + "\t" + tokens[2] + "\t" + tokens[3] + "\t" + tokens[4] + "\t" + tokens[5] + "\t" + tokens[6]); else out.println(chrom + "\t" + sortedRegionScoreTexts[i].start + "\t" + sortedRegionScoreTexts[i].stop + "\t" + sortedRegionScoreTexts[i].text + "\t" + score + "\t" + strand);
            } else {
                if (tokens.length == 7) out.println(chrom + "\t" + sortedRegionScoreTexts[i].start + "\t" + sortedRegionScoreTexts[i].stop + "\t" + tokens[0] + "\t" + sortedRegionScoreTexts[i].score + "\t" + strand + "\t" + tokens[1] + "\t" + tokens[2] + "\t" + tokens[3] + "\t" + tokens[4] + "\t" + tokens[5] + "\t" + tokens[6]); else out.println(chrom + "\t" + sortedRegionScoreTexts[i].start + "\t" + sortedRegionScoreTexts[i].stop + "\t" + sortedRegionScoreTexts[i].text + "\t" + sortedRegionScoreTexts[i].score + "\t" + strand);
            }
        }
    }

    /**Writes native format to the PrintWriter*/
    public void writeNative(PrintWriter out) {
        String chrom = sliceInfo.getChromosome();
        String strand = sliceInfo.getStrand();
        if (strand.equals(".")) {
            out.println("#Chr\tStart\tStop\tScore\t(Text(s)");
            for (int i = 0; i < sortedRegionScoreTexts.length; i++) out.println(chrom + "\t" + sortedRegionScoreTexts[i].start + "\t" + sortedRegionScoreTexts[i].stop + "\t" + sortedRegionScoreTexts[i].score + "\t" + sortedRegionScoreTexts[i].text);
        } else {
            out.println("#Chr\tStart\tStop\tScore\tText(s)\tStrand");
            for (int i = 0; i < sortedRegionScoreTexts.length; i++) out.println(chrom + "\t" + sortedRegionScoreTexts[i].start + "\t" + sortedRegionScoreTexts[i].stop + "\t" + sortedRegionScoreTexts[i].score + "\t" + sortedRegionScoreTexts[i].text + "\t" + strand);
        }
    }

    /**Writes the RegionScoreText[] to a binary file.  Each region's start/stop is converted to a running offset/length which are written as either as ints or shorts.
	 * @param saveDirectory, the binary file will be written using the chromStrandStartBP-StopBP.extension notation to this directory
	 * @param attemptToSaveAsShort, scans to see if the offsets and region lengths exceed 65536 bp, a bit slower to write but potentially a considerable size reduction, set to false for max speed
	 * @return the binaryFile written to the saveDirectory
	 * */
    public File write(File saveDirectory, boolean attemptToSaveAsShort) {
        boolean useShortBeginning = false;
        boolean useShortLength = false;
        if (attemptToSaveAsShort) {
            int bp = sortedRegionScoreTexts[0].start;
            useShortBeginning = true;
            for (int i = 1; i < sortedRegionScoreTexts.length; i++) {
                int currentStart = sortedRegionScoreTexts[i].start;
                int diff = currentStart - bp;
                if (diff > 65536) {
                    useShortBeginning = false;
                    break;
                }
                bp = currentStart;
            }
            useShortLength = true;
            for (int i = 0; i < sortedRegionScoreTexts.length; i++) {
                int diff = sortedRegionScoreTexts[i].stop - sortedRegionScoreTexts[i].start;
                if (diff > 65536) {
                    useShortLength = false;
                    break;
                }
            }
        }
        String fileType;
        if (useShortBeginning) fileType = USeqUtilities.SHORT; else fileType = USeqUtilities.INT;
        if (useShortLength) fileType = fileType + USeqUtilities.SHORT; else fileType = fileType + USeqUtilities.INT;
        fileType = fileType + USeqUtilities.FLOAT + USeqUtilities.TEXT;
        sliceInfo.setBinaryType(fileType);
        binaryFile = new File(saveDirectory, sliceInfo.getSliceName());
        FileOutputStream workingFOS = null;
        DataOutputStream workingDOS = null;
        try {
            workingFOS = new FileOutputStream(binaryFile);
            workingDOS = new DataOutputStream(new BufferedOutputStream(workingFOS));
            workingDOS.writeUTF(header);
            workingDOS.writeInt(sortedRegionScoreTexts[0].start);
            int bp = sortedRegionScoreTexts[0].start;
            if (useShortBeginning) {
                if (useShortLength == false) {
                    workingDOS.writeInt(sortedRegionScoreTexts[0].stop - sortedRegionScoreTexts[0].start);
                    workingDOS.writeFloat(sortedRegionScoreTexts[0].score);
                    workingDOS.writeUTF(sortedRegionScoreTexts[0].text);
                    for (int i = 1; i < sortedRegionScoreTexts.length; i++) {
                        int currentStart = sortedRegionScoreTexts[i].start;
                        int diff = currentStart - bp - 32768;
                        workingDOS.writeShort((short) (diff));
                        workingDOS.writeInt(sortedRegionScoreTexts[i].stop - sortedRegionScoreTexts[i].start);
                        workingDOS.writeFloat(sortedRegionScoreTexts[i].score);
                        workingDOS.writeUTF(sortedRegionScoreTexts[i].text);
                        bp = currentStart;
                    }
                } else {
                    workingDOS.writeShort((short) (sortedRegionScoreTexts[0].stop - sortedRegionScoreTexts[0].start - 32768));
                    workingDOS.writeFloat(sortedRegionScoreTexts[0].score);
                    workingDOS.writeUTF(sortedRegionScoreTexts[0].text);
                    for (int i = 1; i < sortedRegionScoreTexts.length; i++) {
                        int currentStart = sortedRegionScoreTexts[i].start;
                        int diff = currentStart - bp - 32768;
                        workingDOS.writeShort((short) (diff));
                        workingDOS.writeShort((short) (sortedRegionScoreTexts[i].stop - sortedRegionScoreTexts[i].start - 32768));
                        workingDOS.writeFloat(sortedRegionScoreTexts[i].score);
                        workingDOS.writeUTF(sortedRegionScoreTexts[i].text);
                        bp = currentStart;
                    }
                }
            } else {
                if (useShortLength == false) {
                    workingDOS.writeInt(sortedRegionScoreTexts[0].stop - sortedRegionScoreTexts[0].start);
                    workingDOS.writeFloat(sortedRegionScoreTexts[0].score);
                    workingDOS.writeUTF(sortedRegionScoreTexts[0].text);
                    for (int i = 1; i < sortedRegionScoreTexts.length; i++) {
                        int currentStart = sortedRegionScoreTexts[i].start;
                        int diff = currentStart - bp;
                        workingDOS.writeInt(diff);
                        workingDOS.writeInt(sortedRegionScoreTexts[i].stop - sortedRegionScoreTexts[i].start);
                        workingDOS.writeFloat(sortedRegionScoreTexts[i].score);
                        workingDOS.writeUTF(sortedRegionScoreTexts[i].text);
                        bp = currentStart;
                    }
                } else {
                    workingDOS.writeShort((short) (sortedRegionScoreTexts[0].stop - sortedRegionScoreTexts[0].start - 32768));
                    workingDOS.writeFloat(sortedRegionScoreTexts[0].score);
                    workingDOS.writeUTF(sortedRegionScoreTexts[0].text);
                    for (int i = 1; i < sortedRegionScoreTexts.length; i++) {
                        int currentStart = sortedRegionScoreTexts[i].start;
                        int diff = currentStart - bp;
                        workingDOS.writeInt(diff);
                        workingDOS.writeShort((short) (sortedRegionScoreTexts[i].stop - sortedRegionScoreTexts[i].start - 32768));
                        workingDOS.writeFloat(sortedRegionScoreTexts[i].score);
                        workingDOS.writeUTF(sortedRegionScoreTexts[i].text);
                        bp = currentStart;
                    }
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

    /**Assumes all are of the same chromosome and strand!*/
    public static RegionScoreTextData merge(ArrayList<RegionScoreTextData> pdAL) {
        RegionScoreTextData[] pdArray = new RegionScoreTextData[pdAL.size()];
        pdAL.toArray(pdArray);
        Arrays.sort(pdArray);
        int num = 0;
        for (int i = 0; i < pdArray.length; i++) num += pdArray[i].sortedRegionScoreTexts.length;
        RegionScoreText[] concatinate = new RegionScoreText[num];
        int index = 0;
        for (int i = 0; i < pdArray.length; i++) {
            RegionScoreText[] slice = pdArray[i].sortedRegionScoreTexts;
            System.arraycopy(slice, 0, concatinate, index, slice.length);
            index += slice.length;
        }
        SliceInfo sliceInfo = pdArray[0].sliceInfo;
        RegionScoreTextData.updateSliceInfo(concatinate, sliceInfo);
        return new RegionScoreTextData(concatinate, sliceInfo);
    }

    public static RegionScoreTextData mergeUSeqData(ArrayList<USeqData> useqDataAL) {
        int num = useqDataAL.size();
        ArrayList<RegionScoreTextData> a = new ArrayList<RegionScoreTextData>(num);
        for (int i = 0; i < num; i++) a.add((RegionScoreTextData) useqDataAL.get(i));
        return merge(a);
    }

    /**Writes the RegionScoreTextData[] to a ZipOutputStream.
	 * @param	attemptToSaveAsShort	if true, scans to see if the offsets exceed 65536 bp, a bit slower to write but potentially a considerable size reduction, set to false for max speed
	 */
    public void write(ZipOutputStream out, DataOutputStream dos, boolean attemptToSaveAsShort) {
        boolean useShortBeginning = false;
        boolean useShortLength = false;
        if (attemptToSaveAsShort) {
            int bp = sortedRegionScoreTexts[0].start;
            useShortBeginning = true;
            for (int i = 1; i < sortedRegionScoreTexts.length; i++) {
                int currentStart = sortedRegionScoreTexts[i].start;
                int diff = currentStart - bp;
                if (diff > 65536) {
                    useShortBeginning = false;
                    break;
                }
                bp = currentStart;
            }
            useShortLength = true;
            for (int i = 0; i < sortedRegionScoreTexts.length; i++) {
                int diff = sortedRegionScoreTexts[i].stop - sortedRegionScoreTexts[i].start;
                if (diff > 65536) {
                    useShortLength = false;
                    break;
                }
            }
        }
        String fileType;
        if (useShortBeginning) fileType = USeqUtilities.SHORT; else fileType = USeqUtilities.INT;
        if (useShortLength) fileType = fileType + USeqUtilities.SHORT; else fileType = fileType + USeqUtilities.INT;
        fileType = fileType + USeqUtilities.FLOAT + USeqUtilities.TEXT;
        sliceInfo.setBinaryType(fileType);
        binaryFile = null;
        try {
            out.putNextEntry(new ZipEntry(sliceInfo.getSliceName()));
            dos.writeUTF(header);
            dos.writeInt(sortedRegionScoreTexts[0].start);
            int bp = sortedRegionScoreTexts[0].start;
            if (useShortBeginning) {
                if (useShortLength == false) {
                    dos.writeInt(sortedRegionScoreTexts[0].stop - sortedRegionScoreTexts[0].start);
                    dos.writeFloat(sortedRegionScoreTexts[0].score);
                    dos.writeUTF(sortedRegionScoreTexts[0].text);
                    for (int i = 1; i < sortedRegionScoreTexts.length; i++) {
                        int currentStart = sortedRegionScoreTexts[i].start;
                        int diff = currentStart - bp - 32768;
                        dos.writeShort((short) (diff));
                        dos.writeInt(sortedRegionScoreTexts[i].stop - sortedRegionScoreTexts[i].start);
                        dos.writeFloat(sortedRegionScoreTexts[i].score);
                        dos.writeUTF(sortedRegionScoreTexts[i].text);
                        bp = currentStart;
                    }
                } else {
                    dos.writeShort((short) (sortedRegionScoreTexts[0].stop - sortedRegionScoreTexts[0].start - 32768));
                    dos.writeFloat(sortedRegionScoreTexts[0].score);
                    dos.writeUTF(sortedRegionScoreTexts[0].text);
                    for (int i = 1; i < sortedRegionScoreTexts.length; i++) {
                        int currentStart = sortedRegionScoreTexts[i].start;
                        int diff = currentStart - bp - 32768;
                        dos.writeShort((short) (diff));
                        dos.writeShort((short) (sortedRegionScoreTexts[i].stop - sortedRegionScoreTexts[i].start - 32768));
                        dos.writeFloat(sortedRegionScoreTexts[i].score);
                        dos.writeUTF(sortedRegionScoreTexts[i].text);
                        bp = currentStart;
                    }
                }
            } else {
                if (useShortLength == false) {
                    dos.writeInt(sortedRegionScoreTexts[0].stop - sortedRegionScoreTexts[0].start);
                    dos.writeFloat(sortedRegionScoreTexts[0].score);
                    dos.writeUTF(sortedRegionScoreTexts[0].text);
                    for (int i = 1; i < sortedRegionScoreTexts.length; i++) {
                        int currentStart = sortedRegionScoreTexts[i].start;
                        int diff = currentStart - bp;
                        dos.writeInt(diff);
                        dos.writeInt(sortedRegionScoreTexts[i].stop - sortedRegionScoreTexts[i].start);
                        dos.writeFloat(sortedRegionScoreTexts[i].score);
                        dos.writeUTF(sortedRegionScoreTexts[i].text);
                        bp = currentStart;
                    }
                } else {
                    dos.writeShort((short) (sortedRegionScoreTexts[0].stop - sortedRegionScoreTexts[0].start - 32768));
                    dos.writeFloat(sortedRegionScoreTexts[0].score);
                    dos.writeUTF(sortedRegionScoreTexts[0].text);
                    for (int i = 1; i < sortedRegionScoreTexts.length; i++) {
                        int currentStart = sortedRegionScoreTexts[i].start;
                        int diff = currentStart - bp;
                        dos.writeInt(diff);
                        dos.writeShort((short) (sortedRegionScoreTexts[i].stop - sortedRegionScoreTexts[i].start - 32768));
                        dos.writeFloat(sortedRegionScoreTexts[i].score);
                        dos.writeUTF(sortedRegionScoreTexts[i].text);
                        bp = currentStart;
                    }
                }
            }
            out.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
            USeqUtilities.safeClose(out);
            USeqUtilities.safeClose(dos);
        }
    }

    /**Reads a DataInputStream into this RegionScoreTextData.*/
    public void read(DataInputStream dis) {
        try {
            header = dis.readUTF();
            int numberRegionScoreTexts = sliceInfo.getNumberRecords();
            sortedRegionScoreTexts = new RegionScoreText[numberRegionScoreTexts];
            String fileType = sliceInfo.getBinaryType();
            if (USeqUtilities.REGION_SCORE_TEXT_INT_INT_FLOAT_TEXT.matcher(fileType).matches()) {
                int start = dis.readInt();
                sortedRegionScoreTexts[0] = new RegionScoreText(start, start + dis.readInt(), dis.readFloat(), dis.readUTF());
                for (int i = 1; i < numberRegionScoreTexts; i++) {
                    start = sortedRegionScoreTexts[i - 1].start + dis.readInt();
                    sortedRegionScoreTexts[i] = new RegionScoreText(start, start + dis.readInt(), dis.readFloat(), dis.readUTF());
                }
            } else if (USeqUtilities.REGION_SCORE_TEXT_INT_SHORT_FLOAT_TEXT.matcher(fileType).matches()) {
                int start = dis.readInt();
                sortedRegionScoreTexts[0] = new RegionScoreText(start, start + dis.readShort() + 32768, dis.readFloat(), dis.readUTF());
                for (int i = 1; i < numberRegionScoreTexts; i++) {
                    start = sortedRegionScoreTexts[i - 1].start + dis.readInt();
                    sortedRegionScoreTexts[i] = new RegionScoreText(start, start + dis.readShort() + 32768, dis.readFloat(), dis.readUTF());
                }
            } else if (USeqUtilities.REGION_SCORE_TEXT_SHORT_SHORT_FLOAT_TEXT.matcher(fileType).matches()) {
                int start = dis.readInt();
                sortedRegionScoreTexts[0] = new RegionScoreText(start, start + dis.readShort() + 32768, dis.readFloat(), dis.readUTF());
                for (int i = 1; i < numberRegionScoreTexts; i++) {
                    start = sortedRegionScoreTexts[i - 1].start + dis.readShort() + 32768;
                    sortedRegionScoreTexts[i] = new RegionScoreText(start, start + dis.readShort() + 32768, dis.readFloat(), dis.readUTF());
                }
            } else if (USeqUtilities.REGION_SCORE_TEXT_SHORT_INT_FLOAT_TEXT.matcher(fileType).matches()) {
                int start = dis.readInt();
                sortedRegionScoreTexts[0] = new RegionScoreText(start, start + dis.readInt(), dis.readFloat(), dis.readUTF());
                for (int i = 1; i < numberRegionScoreTexts; i++) {
                    start = sortedRegionScoreTexts[i - 1].start + dis.readShort() + 32768;
                    sortedRegionScoreTexts[i] = new RegionScoreText(start, start + dis.readInt(), dis.readFloat(), dis.readUTF());
                }
            } else {
                throw new IOException("Incorrect file type for creating a RegionScoreText[] -> '" + fileType + "' in " + binaryFile + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            USeqUtilities.safeClose(dis);
        }
    }

    public RegionScoreText[] getRegionScoreTexts() {
        return sortedRegionScoreTexts;
    }

    public void setRegionScoreTexts(RegionScoreText[] sortedRegionScoreTexts) {
        this.sortedRegionScoreTexts = sortedRegionScoreTexts;
        updateSliceInfo(sortedRegionScoreTexts, sliceInfo);
    }

    /**Returns whether data remains.*/
    public boolean trim(int beginningBP, int endingBP) {
        ArrayList<RegionScoreText> al = new ArrayList<RegionScoreText>();
        for (int i = 0; i < sortedRegionScoreTexts.length; i++) {
            if (sortedRegionScoreTexts[i].isContainedBy(beginningBP, endingBP)) al.add(sortedRegionScoreTexts[i]);
        }
        if (al.size() == 0) return false;
        sortedRegionScoreTexts = new RegionScoreText[al.size()];
        al.toArray(sortedRegionScoreTexts);
        updateSliceInfo(sortedRegionScoreTexts, sliceInfo);
        return true;
    }
}
