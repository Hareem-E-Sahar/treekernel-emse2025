package ssg.tools.common.fileUtilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import ssg.tools.common.fragmentedFile.StreamDataSource;

/**
 * Broken RAR fixer: scans RAR file for broken headers.
 * On finding broken headers scans rest of RAR file until next valid header.
 * Region between location of broken and next valid header is cutt off.
 * 
 * Uses limited built-in RAR file parsing (no multifile archives support).
 * Output file is created using FragmentedFileInfo technique.
 *
 * @author ssg
 */
public class RARFix {

    public static enum HEAD_TYPE {

        MARK, MAIN, FILE, COMM, AV, SUB, PROTECT, COMMENT, EOF
    }

    ;

    public static Map<HEAD_TYPE, Byte> head2code = new HashMap<HEAD_TYPE, Byte>() {

        {
            put(HEAD_TYPE.MARK, (byte) 0x72);
            put(HEAD_TYPE.MAIN, (byte) 0x73);
            put(HEAD_TYPE.FILE, (byte) 0x74);
            put(HEAD_TYPE.COMM, (byte) 0x75);
            put(HEAD_TYPE.AV, (byte) 0x76);
            put(HEAD_TYPE.SUB, (byte) 0x77);
            put(HEAD_TYPE.PROTECT, (byte) 0x78);
            put(HEAD_TYPE.COMMENT, (byte) 0x7a);
            put(HEAD_TYPE.EOF, (byte) 0x7b);
        }
    };

    public static Map<Byte, HEAD_TYPE> code2head = new HashMap<Byte, HEAD_TYPE>() {

        {
            for (HEAD_TYPE ht : head2code.keySet()) {
                Byte htc = head2code.get(ht);
                put(htc, ht);
            }
        }
    };

    static class RH {

        byte[] bStd = new byte[7];

        byte[] bExt = null;

        public RH(InputStream is) throws IOException, RARException {
            is.read(bStd);
            int hs = getSize();
            if (hs > bStd.length) {
                bExt = new byte[hs - bStd.length];
                is.read(bExt);
            }
        }

        public RH(byte[] bStd, byte[] bExt) throws RARException {
            setData(bStd, bExt);
        }

        public void setData(byte[] bStd, byte[] bExt) throws RARException {
            if (bStd == null || bStd.length != this.bStd.length) {
                throw new RARException("Standard header is invalid.");
            }
            for (int i = 0; i < this.bStd.length; i++) {
                this.bStd[i] = bStd[i];
            }
            if (bExt != null) {
                this.bExt = new byte[bExt.length];
                for (int i = 0; i < this.bExt.length; i++) {
                    this.bExt[i] = bExt[i];
                }
            }
        }

        public boolean validate() {
            return getCRC() == calculateCRC();
        }

        public int getCRC() {
            return getShort(0);
        }

        public byte getType() {
            return bStd[2];
        }

        public int getFlags() {
            return getShort(3);
        }

        public int getSize() {
            return getShort(5);
        }

        public long getLong(int offset) {
            return (long) ((getByte(offset + 7) & 0xFF) << 56 | (getByte(offset + 6) & 0xFF) << 48 | (getByte(offset + 5) & 0xFF) << 40 | (getByte(offset + 4) & 0xFF) << 32 | (getByte(offset + 3) & 0xFF) << 24 | (getByte(offset + 2) & 0xFF) << 16 | (getByte(offset + 1) & 0xFF) << 8 | (getByte(offset) & 0xFF));
        }

        public int getInt(int offset) {
            return ((getByte(offset + 3) & 0xFF) << 24 | (getByte(offset + 2) & 0xFF) << 16 | (getByte(offset + 1) & 0xFF) << 8 | (getByte(offset) & 0xFF));
        }

        public short getShort(int offset) {
            return (short) ((getByte(offset + 1) & 0xFF) << 8 | (getByte(offset) & 0xFF));
        }

        public byte getByte(int offset) {
            if (offset < bStd.length) {
                return bStd[offset];
            } else if (bExt != null) {
                return bExt[offset - bStd.length];
            } else {
                throw new ArrayIndexOutOfBoundsException();
            }
        }

        public int calculateCRC() {
            byte[] b = new byte[bStd.length - 2 + ((bExt != null) ? bExt.length : 0)];
            for (int i = 2; i < bStd.length; i++) {
                b[i - 2] = bStd[i];
            }
            if (bExt != null) {
                int j = bStd.length - 2;
                for (int i = 0; i < bExt.length; i++) {
                    b[j + i] = bExt[i];
                }
            }
            CRC32 crc = new CRC32();
            crc.reset();
            crc.update(b);
            return (short) (crc.getValue());
        }

        public boolean compare(byte[] a, byte[] b, int len) {
            if (a == b) {
                return true;
            }
            if (a == null || b == null) {
                return false;
            }
            if (a.length != b.length && (a.length < len || b.length < len)) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * MARK header
     */
    static class RH_MARK extends RH {

        private static final byte[] oldRarHeader = { 0x52, 0x45, 0x7e, 0x5e };

        private static final byte[] rar2Header = { 0x52, 0x61, 0x72, 0x21, 0x1a, 0x07, 0x00 };

        private static final byte[] uniqueHeader = { 0x55, 0x6e, 0x69, 0x71, 0x75, 0x45, 0x21 };

        public RH_MARK(InputStream is) throws IOException, RARException {
            super(is);
            if (!validate()) {
                throw new RuntimeException("Unknown RAR MARK header.");
            }
        }

        public RH_MARK(RH rh) throws IOException, RARException {
            super(rh.bStd, rh.bExt);
            if (!validate()) {
                throw new RuntimeException("Unknown RAR MARK header.");
            }
        }

        @Override
        public boolean validate() {
            return (compare(bStd, oldRarHeader, 4) || compare(bStd, rar2Header, bStd.length) || compare(bStd, uniqueHeader, bStd.length));
        }
    }

    /**
     * FILE header
     */
    static class RH_FILE extends RH {

        public RH_FILE(RH header) throws RARException {
            super(header.bStd, header.bExt);
        }

        public int getPackedSize() {
            return getInt(7);
        }

        public int getOriginalSize() {
            return getInt(11);
        }

        public byte getHostOS() {
            return getByte(15);
        }

        public int getFileCRC() {
            return getInt(16);
        }

        public int getFileTime() {
            return getInt(20);
        }

        public byte getUnpackVersion() {
            return getByte(24);
        }

        public byte getMethod() {
            return getByte(25);
        }

        public int getNameSize() {
            return getShort(26);
        }

        public int getFileAttributes() {
            return getInt(28);
        }

        public String getFileName() {
            byte[] s = new byte[getNameSize()];
            for (int i = 0; i < s.length; i++) {
                s[i] = getByte(32 + i);
            }
            return new String(s);
        }
    }

    public static class RARException extends Exception {

        public RARException(String msg) {
            super(msg);
        }

        public RARException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    public static class RARFileEntry {

        File f;

        long offset;

        RH_FILE fh;

        public RARFileEntry(File f, long offset, RH_FILE fh) {
            this.f = f;
            this.offset = offset;
            this.fh = fh;
        }

        public long getOffset() {
            return offset;
        }

        public int getPackedSize() {
            return (fh != null) ? fh.getPackedSize() : -1;
        }

        public String getFileName() {
            return (fh != null) ? fh.getFileName() : null;
        }
    }

    public static class RARBrokenFileEntry extends RARFileEntry {

        long end;

        public RARBrokenFileEntry(File f, long start, long end) {
            super(f, start, null);
            this.end = end;
        }

        public long getSize() {
            return end - offset;
        }

        public String getFileName() {
            return "--- BROKEN ---";
        }
    }

    public List<RARFileEntry> listFiles(File f) throws IOException, RARException {
        StreamDataSource sds = new StreamDataSource("test", f);
        InputStream is = sds.getInputStream(0);
        StreamDataSource.RAFInputStream ris = null;
        if (is instanceof StreamDataSource.RAFInputStream) {
            ris = (StreamDataSource.RAFInputStream) is;
        }
        long pos = 0L;
        List<RARFileEntry> files = new LinkedList<RARFileEntry>();
        RH_MARK mh = new RH_MARK(is);
        pos += mh.getSize();
        boolean b = true;
        while (b) {
            try {
                RH rh = new RH(is);
                if (!rh.validate()) {
                    long brokenPosStart = pos;
                    long brokenPosEnd = pos;
                    if (ris != null) {
                        while (!ris.eof() && !rh.validate()) {
                            ris.seek(++pos);
                            rh = new RH(ris);
                        }
                    } else {
                        throw new RARException("Invalid RAR header. Can't scan for next valid header - exiting.");
                    }
                    if (rh != null) {
                    }
                    brokenPosEnd = pos;
                    if (brokenPosStart != brokenPosEnd) {
                        files.add(new RARBrokenFileEntry(f, brokenPosStart, brokenPosEnd));
                    }
                }
                if (code2head.get(rh.getType()) == HEAD_TYPE.FILE) {
                    RH_FILE fh = new RH_FILE(rh);
                    if (fh != null) {
                        files.add(new RARFileEntry(f, pos, fh));
                        pos += is.skip(fh.getPackedSize());
                    }
                }
                pos += rh.getSize();
                b = rh.validate();
            } catch (IOException ioex) {
                b = false;
            }
        }
        is.close();
        return files;
    }

    public static void help(String title, CommandLineParser cmd) {
        System.out.println("RAR fixing utility. (c) 2010, SSG.");
        System.out.println("Usage: java " + RARFix.class.getName() + " <broken RAR file> [<fixed RAR file>]");
        System.out.println("Description:");
        System.out.println("  Scans input file for invalid RAR headers and");
        System.out.println("  generates fixed one (default is 'fixed.rar')");
        System.out.println("  if problems were found.");
        System.out.println("NOTE:");
        System.out.println("  this tool does not check actual files and does not");
        System.out.println("  automatically work with multiple volumes,");
        System.out.println("  but only inidividual RAR files.");
        System.out.println("  Primary purpose is to pre-fix broken");
        System.out.println("  RAR files so they might be further");
        System.out.println("  process with standard tools.");
    }

    public static void main(String[] args) throws Exception {
        CommandLineParser cmd = new CommandLineParser();
        cmd.addOption(new CommandLineParser.CMDOption("help", null));
        cmd.addOption(new CommandLineParser.CMDOption("?", null));
        cmd.parse(args);
        if (cmd.getArguments().length == 0 || cmd.getOption("help").isDefined() || cmd.getOption("?").isDefined()) {
            help("No arguments or help.", cmd);
            return;
        }
        String inputFileName = cmd.getArgument(0);
        String outputFileName = (cmd.getArguments().length > 1) ? cmd.getArgument(1) : "fixed.rar";
        File f = new File(inputFileName);
        File fout = new File(outputFileName);
        System.out.println("RARFix: scanning '" + inputFileName + "' ...");
        RARFix rf = new RARFix();
        List<RARFileEntry> files = rf.listFiles(f);
        List<RARFileEntry> cutOffs = new LinkedList<RARFileEntry>();
        for (RARFileEntry rfe : files) {
            if (rfe instanceof RARBrokenFileEntry) {
                RARBrokenFileEntry rbfe = (RARBrokenFileEntry) rfe;
                System.out.println("BROKEN ENTRY FROM " + rbfe.offset + " to " + rbfe.end + ", length=" + rbfe.getSize() + "");
                cutOffs.add(rbfe);
            } else {
                System.out.println("FILE [o/s/p " + rfe.offset + "/" + rfe.fh.getOriginalSize() + "/" + rfe.fh.getPackedSize() + "]: " + rfe.fh.getFileName());
            }
        }
        if (cutOffs.size() > 0) {
            System.out.println("\nRARFix: detected " + cutOffs.size() + " problem" + ((cutOffs.size() > 1) ? "s" : "") + ", will create '" + outputFileName + "'.");
            ArrayList al = new ArrayList();
            al.add("-command=SOURCE sourceRAR," + f.getAbsolutePath() + "");
            al.add("-command=COPY sourceRAR,0");
            for (int i = cutOffs.size() - 1; i >= 0; i--) {
                RARFileEntry rfe = cutOffs.get(i);
                if (rfe instanceof RARBrokenFileEntry) {
                    RARBrokenFileEntry rbfe = (RARBrokenFileEntry) rfe;
                    al.add("-command=CUT " + rbfe.offset + "," + rbfe.getSize());
                } else {
                }
            }
            al.add("" + fout.getAbsolutePath());
            System.out.println("\nCommands for FileComposer:");
            for (int i = 0; i < al.size(); i++) {
                System.out.println(al.get(i));
            }
            System.out.println("\nRARFix: building '" + outputFileName + "' ...");
            FileComposer.main((String[]) al.toArray(new String[al.size()]));
        } else {
            System.out.println("\nRARFix: no header problems found in '" + inputFileName + "'. Exiting.");
        }
    }
}
