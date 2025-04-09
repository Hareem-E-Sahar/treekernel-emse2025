package org.barred;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.barred.helper.ByteDisp;
import org.barred.block.BlockMan;
import org.barred.helper.Digest;
import org.barred.helper.ZipHandler;
import org.barred.helper.DirHelper;
import org.barred.helper.StringHelper;
import org.barred.helper.BWTHelper;
import org.barred.stream.BAROutputStream;
import org.barred.stream.BARInputStream;
import org.barred.helper.ArtHelper;

public class Bar {

    private static boolean isNeg = false;

    private static boolean isD = false;

    private static boolean protectA = false;

    private static boolean view = false;

    private static boolean verify = false;

    private static boolean find = false;

    private static boolean add = false;

    private static float[] bpsA = null;

    private static boolean isComment = false;

    private static boolean eSingle = false;

    private static boolean isBwt = true;

    private static int blockSize = 1;

    private static boolean modeLock = false;

    /**
     *  Appears on typing Help
     */
    private static void display() {
        System.out.println("\nBARRED 2.0_SVN_Sep23_09");
        System.out.println("  Copyright (C) 2009 by Frank Jennings (fermatjen@yahoo.com).\r\n");
        System.out.println("  Usage: -c/x/v/i/a <inputfile> <outputfile> [-secure].\n");
        System.out.println("        -a Add Files/DIR specified by <inputfile> to <outputfile>.");
        System.out.println("        -v View the content in the <inputfile> archive.");
        System.out.println("        -c Compress the specified <inputfile> and save as <outputfile>.");
        System.out.println("        -cc Add a comment to the archive and compress it.");
        System.out.println("        -x Decompress the specified <inputfile> and save as <outputfile>.");
        System.out.println("        -xf Decompress the specified file.");
        System.out.println("        -i Perform integrity check in DIR specified by <ifile>\n");
        System.out.println("        -b (Advanced) Manually setting the block size (1-n) MB (Default is 10).\n");
        System.out.println("	View Archive: <o-o>");
        System.out.println("        e-g. -v mp3s.bar\n");
        System.out.println("	Check Archive: <*-*>");
        System.out.println("        e-g. -i mp3s.bar\n");
        System.out.println("	Search File: <O-O>");
        System.out.println("        e-g. -f mp3s.bar bill.mp3\n");
        System.out.println("	Add Dir to Archive: <++>");
        System.out.println("        e-g. -a new_mp3s/ mp3s.bar\n");
        System.out.println("	Compressing: >101<");
        System.out.println("        e-g. -c test.mpg test.bar");
        System.out.println("        e-g. -c -b 3 test.mpg test.bar");
        System.out.println("        e-g. -c /home/afj/mp3s /home/backup/mp3s.bar");
        System.out.println("        e-g. -cc /home/afj/mp3s /home/backup/mp3s.bar\n");
        System.out.println("	De-compressing: <01010>");
        System.out.println("        e-g. -x test.bar test.mpg");
        System.out.println("        e-g. -x /home/backup/mp3s.bar /home/afj/mp3s");
        System.out.println("        e-g. -xf billy.mp3 mp3s.bar\n");
        System.out.println("	Securing Content (Optional): {$$$}");
        System.out.println("        e-g. -c payroll.doc payroll.bar -secure");
        System.out.println("        e-g. -c /home/afj/personal personal.bar -secure\n");
        System.exit(0);
    }

    /**
     *  Prints stats after compression routine
     *
     *@param  orig  Description of the Parameter
     *@param  comp  Description of the Parameter
     *@return       Description of the Return Value
     */
    private static void stats(float orig, float comp, boolean file) {
        if (file) {
            System.out.println("Average File Stats:");
        } else {
            System.out.println("Dir. Stats:");
        }
        System.out.println("  Orig. Size : " + (int) orig + " Bytes " + "(" + ByteDisp.convert((long) orig) + ")");
        System.out.println("  Comp. Size : " + (int) comp + " Bytes " + "(" + ByteDisp.convert((long) comp) + ")");
        System.out.println("  Comp. Ratio: " + (((orig - comp) * 100) / orig) + " %");
        if (!file) {
            System.out.println("Aveg. bps  : " + averageBps() + " bps");
        }
        if (isNeg) {
            if (isD) {
                System.out.println("One/more files had minimum entropy. So negative compression occured.\n This message can be ignored. Your Dir. content is safe.");
            } else {
                System.out.println("WARNING! The file, which you are compressing has minimum entropy. Could not be compressed further.\n So negative compression occured. Please delete the bar file. This file is already compressed!");
            }
        }
    }

    private static float cratio(float orig, float comp) {
        return (((orig - comp) * 100) / orig);
    }

    private static float averageBps() {
        float total = 0;
        for (int i = 0; i < bpsA.length; i++) {
            total = total + bpsA[i];
        }
        return (total / bpsA.length);
    }

    /**
     *  Program Start
     *
     *@param  ar             Description of the Parameter
     *@exception  Exception  Description of the Exception
     */
    public static void main(String ar[]) throws Exception {
        try {
            String mode = new String();
            String ifile = new String();
            String ofile = new String();
            String pass = new String();
            int arLength = ar.length;
            try {
                if (arLength != 0) {
                    if (ar[0].indexOf("help") != -1) {
                        display();
                    }
                    for (int i = 0; i < arLength; i++) {
                        if (ar[i].equalsIgnoreCase("-b")) {
                            if (i == arLength) {
                                display();
                                System.exit(0);
                            }
                            blockSize = Integer.parseInt(ar[i + 1]);
                            System.out.println(" WARNING: Block size specified manually - " + blockSize);
                            break;
                        }
                    }
                    for (int i = 0; i < arLength; i++) {
                        if (ar[i].equalsIgnoreCase("-c")) {
                            if (i == arLength) {
                                display();
                                System.exit(0);
                            }
                            mode = "compress";
                            modeLock = true;
                            ifile = ar[i + 1];
                            ofile = ar[i + 2];
                            break;
                        }
                    }
                    for (int i = 0; i < arLength; i++) {
                        if (ar[i].equalsIgnoreCase("-x")) {
                            if (i == arLength) {
                                display();
                                System.exit(0);
                            }
                            mode = "decompress";
                            modeLock = true;
                            ifile = ar[i + 1];
                            if (!ifile.endsWith("zip")) {
                                ofile = ar[i + 2];
                            } else {
                                if (arLength > 2) {
                                    System.out.println("Cannot specify output file for formats other than bar. Files will be extracted in bar_ext Dir.");
                                    System.exit(0);
                                }
                                ofile = "bar_ext";
                                File ini = new File(ofile);
                                ini.mkdir();
                            }
                            break;
                        }
                    }
                    for (int i = 0; i < arLength; i++) {
                        if (ar[i].equalsIgnoreCase("-v")) {
                            if (i == arLength) {
                                display();
                                System.exit(0);
                            }
                            mode = "view";
                            modeLock = true;
                            ifile = ar[i + 1];
                            view = true;
                            break;
                        }
                    }
                    for (int i = 0; i < arLength; i++) {
                        if (ar[i].equalsIgnoreCase("-i")) {
                            if (i == arLength) {
                                display();
                                System.exit(0);
                            }
                            mode = "verify";
                            modeLock = true;
                            ifile = ar[i + 1];
                            verify = true;
                            break;
                        }
                    }
                    for (int i = 0; i < arLength; i++) {
                        if (ar[i].equalsIgnoreCase("-f")) {
                            if (i == arLength) {
                                display();
                                System.exit(0);
                            }
                            mode = "find";
                            modeLock = true;
                            ifile = ar[i + 1];
                            ofile = ar[i + 2];
                            find = true;
                            break;
                        }
                    }
                    for (int i = 0; i < arLength; i++) {
                        if (ar[i].equalsIgnoreCase("-a")) {
                            if (i == arLength) {
                                display();
                                System.exit(0);
                            }
                            mode = "add";
                            modeLock = true;
                            ifile = ar[i + 1];
                            ofile = ar[i + 2];
                            add = true;
                            break;
                        }
                    }
                    for (int i = 0; i < arLength; i++) {
                        if (ar[i].equalsIgnoreCase("-cc")) {
                            if (i == arLength) {
                                display();
                                System.exit(0);
                            }
                            mode = "compress";
                            modeLock = true;
                            isComment = true;
                            ifile = ar[i + 1];
                            ofile = ar[i + 2];
                            break;
                        }
                    }
                    for (int i = 0; i < arLength; i++) {
                        if (ar[i].equalsIgnoreCase("-xf")) {
                            if (i == arLength) {
                                display();
                                System.exit(0);
                            }
                            mode = "decompress";
                            modeLock = true;
                            eSingle = true;
                            ifile = ar[i + 2];
                            ofile = ar[i + 1];
                            break;
                        }
                    }
                    if (!modeLock) {
                        display();
                        System.exit(0);
                    }
                } else {
                    display();
                    System.exit(0);
                }
                for (int i = 0; i < arLength; i++) {
                    if (ar[i].equalsIgnoreCase("-secure")) {
                        if (i == arLength) {
                            display();
                            System.exit(0);
                        }
                        if (mode.equals("decompress") || mode.equals("view") || mode.equals("verify")) {
                            System.out.println(" -secure option should be used for compression only.");
                            System.exit(0);
                        }
                        protectA = true;
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
                display();
                System.exit(0);
            }
            if (mode.equals("compress") || mode.equals("add")) {
                boolean isDir = false;
                System.out.println(" Block Size: " + blockSize + " MB");
                boolean isZip = false;
                boolean isGZip = false;
                FileOutputStream zips = null;
                if (ofile.endsWith("zip")) {
                    zips = new FileOutputStream(ofile);
                }
                if (add) {
                    File te = new File(ofile);
                    if (!te.exists()) {
                        System.out.println("The output file is invalid!. It should be a Bar compressed file");
                        System.exit(0);
                    }
                    FileInputStream tSec = new FileInputStream(te);
                    int fb = tSec.read();
                    tSec.close();
                    if (fb == 10 || fb == 9) {
                        System.out.println("The archive: " + ofile + " is secured. Permission Denied");
                        System.exit(0);
                    }
                    if (fb == 1) {
                        System.out.println(ofile + " does not have DIR. content. Permission Denied");
                        System.exit(0);
                    }
                    isDir = true;
                }
                if (protectA) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                    System.out.println(" Please enter the passphrase:");
                    pass = in.readLine();
                    System.out.println(" Please re-enter the passphrase:");
                    String conf = in.readLine();
                    if (!pass.equals(conf)) {
                        System.out.println(" Passphrase Mismatch.");
                        System.exit(0);
                    }
                }
                File te = new File(ifile);
                if (!te.exists()) {
                    System.out.println("Invalid Input File!");
                    System.exit(0);
                }
                char fsep = '\\';
                if (te.isDirectory()) {
                    isDir = true;
                    isD = true;
                }
                ArtHelper arts = DirHelper.getArtifacts(ifile);
                fsep = arts.getSeperator();
                ArrayList alist = arts.getAlist();
                bpsA = new float[alist.size()];
                if (!(ofile).endsWith(".bar")) {
                    System.out.println("Output file should have extension .bar");
                    System.exit(0);
                }
                File check = new File(ofile);
                if (!add) {
                    if (check.exists()) {
                        check.delete();
                    }
                }
                if (check.isDirectory()) {
                    System.out.println("Output file is a Directory. Try again.\n Enter a valid file name such as /home/afj/test.bar.");
                    System.exit(0);
                }
                FileOutputStream fout = null;
                if (!isZip) {
                    fout = new FileOutputStream(ofile, true);
                }
                BAROutputStream bos = new BAROutputStream(fout);
                int sosize = 0;
                int sosize_f = 0;
                ByteBuffer buf = ByteBuffer.allocate(1);
                if (!add && !isZip) {
                    if (isDir) {
                        if (!protectA) {
                            if (isComment) {
                                buf.put((byte) 20);
                                fout.write(buf.array());
                            } else {
                                buf.put((byte) 2);
                                fout.write(buf.array());
                            }
                        } else {
                            if (isComment) {
                                buf.put((byte) 100);
                                fout.write(buf.array());
                            } else {
                                buf.put((byte) 10);
                                fout.write(buf.array());
                            }
                        }
                        String comment = null;
                        if (isComment) {
                            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                            System.out.println("Please enter the archive comment:");
                            comment = in.readLine();
                            byte[] com = comment.getBytes();
                            short clen = (short) com.length;
                            ByteBuffer buf0 = ByteBuffer.allocate(2);
                            buf0.putShort(clen);
                            fout.write(buf0.array());
                            fout.write(com);
                        }
                        buf = ByteBuffer.allocate(1);
                        buf.put((byte) fsep);
                        fout.write(buf.array());
                    } else {
                        if (isComment) {
                            System.out.println("Invalid Comment Switch. Comment can be added only for Dir. content");
                            System.exit(0);
                        }
                        if (!protectA) {
                            buf.put((byte) 1);
                            fout.write(buf.array());
                        } else {
                            buf.put((byte) 9);
                            fout.write(buf.array());
                        }
                    }
                    if (protectA) {
                        byte[] dig = Digest.getDigest(pass.getBytes());
                        byte _digLength = (byte) dig.length;
                        fout.write(_digLength);
                        fout.write(dig);
                    }
                }
                int bpsInd = 0;
                boolean initOut = false;
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < alist.size(); i++) {
                    if (isZip) {
                        if (!isDir) {
                            System.out.println("Adding ZIP Entry: " + alist.get(i));
                            ZipHandler.initO(zips);
                            ZipHandler.writeFile(null, (String) alist.get(i));
                            continue;
                        } else {
                            File abs = null;
                            if (!initOut) {
                                ZipHandler.initO(zips);
                                initOut = true;
                            }
                            System.out.println("Adding ZIP Entry: " + alist.get(i));
                            try {
                                abs = new File(ifile, (String) alist.get(i));
                                ZipHandler.writeFile((String) alist.get(i), abs.getPath());
                            } catch (Exception e) {
                                System.out.println("Error:" + e);
                                System.exit(0);
                            }
                            continue;
                        }
                    }
                    FileInputStream in = null;
                    if (((String) alist.get(i)).endsWith(".bar")) {
                        System.out.println("Skipping.." + alist.get(i));
                    } else {
                        if (isDir) {
                            File abs = null;
                            try {
                                abs = new File(ifile, (String) alist.get(i));
                                in = new FileInputStream(abs);
                            } catch (Exception e) {
                                System.out.println("Error:" + e);
                                System.exit(0);
                            }
                        } else {
                            in = new FileInputStream(ifile);
                        }
                        int size = in.available();
                        if (size == 0) {
                            System.out.println("Skipping: " + alist.get(i) + " 0 Byte!");
                            continue;
                        }
                        if (!isDir) {
                            System.out.println("Compressing: " + alist.get(i) + " \n(" + size + ") - " + ByteDisp.convert((long) size));
                        }
                        int asize = 0;
                        sosize = sosize + size;
                        int bsize = (BlockMan.getBlock(size)) * blockSize;
                        int start = 0;
                        int end = bsize;
                        if (size < bsize) {
                            end = size;
                        }
                        long parts = (size / bsize) + 1;
                        if (size == bsize) {
                            parts = 1;
                        }
                        int part = 0;
                        if (isDir) {
                            buf = ByteBuffer.allocate(1);
                            buf.put((byte) 1);
                            fout.write(buf.array());
                            byte[] alength = ((String) alist.get(i)).getBytes();
                            buf = ByteBuffer.allocate(2);
                            buf.putShort((short) alength.length);
                            fout.write(buf.array());
                            buf = ByteBuffer.wrap(alength);
                            buf.put(alength);
                            fout.write(buf.array());
                        }
                        boolean isFirst = true;
                        boolean isBWT = true;
                        asize = 1;
                        System.out.println(" Reading " + alist.get(i) + " as " + parts + " block(s).");
                        while (true) {
                            if (end > size && start < size) {
                                end = size;
                            }
                            if (start >= size) {
                                break;
                            }
                            int fsize = end - start;
                            if (fsize < bsize) {
                                if (!isFirst) {
                                    fsize++;
                                }
                            }
                            part++;
                            buf = ByteBuffer.allocate(1);
                            buf.put((byte) 0);
                            fout.write(buf.array());
                            asize++;
                            System.out.print("[" + part + "/" + parts + "]");
                            byte[] input = new byte[fsize];
                            in.read(input);
                            BWTHelper bwt = bos.write(input);
                            int _bsize = bwt.getBytesOut();
                            isBWT = bwt.getIsBWT();
                            isBwt = isBWT;
                            asize = asize + 8;
                            asize = asize + _bsize;
                            start = end + 1;
                            end = end + bsize;
                            isFirst = false;
                        }
                        System.out.println();
                        float t1 = (float) asize;
                        float t2 = (float) size;
                        bpsA[bpsInd] = (float) ((t1 * 8) / t2);
                        sosize_f = sosize_f + asize;
                        if (isDir) {
                            if (isBWT) {
                                System.out.println("    + " + alist.get(i) + " @ " + asize + " : " + size + " -> CR: [" + cratio((float) size, (float) asize) + " %] - [" + bpsA[bpsInd] + " bps]");
                            } else {
                                System.out.println("    + " + alist.get(i) + " @ " + asize + " : " + size + " -> CR: [" + cratio((float) size, (float) asize) + " %] - [" + bpsA[bpsInd] + " bps] - [No BWT]");
                            }
                            if (cratio((float) size, (float) asize) < 0.0) {
                                isNeg = true;
                            }
                        }
                        bpsInd++;
                    }
                }
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime) / 1000;
                System.out.println(" Processed in " + duration + " secs.");
                if (!add) {
                    System.out.println("File saved as : " + ofile);
                } else {
                    System.out.println("Added :" + ifile + " to " + ofile);
                }
                if (isZip) {
                    ZipHandler.closeOut();
                } else {
                    fout.close();
                }
                File test = new File(ofile);
                if (cratio((float) sosize, (float) test.length()) < 0.0) {
                    isNeg = true;
                }
                if (!add && !isZip) {
                    stats(sosize, (float) sosize_f, true);
                    stats(sosize, (float) test.length(), false);
                }
            } else if (mode.equals("decompress") || mode.equals("view") || mode.equals("verify") || mode.equals("find")) {
                if (ifile.endsWith("zip")) {
                    if (mode.equals("decompress")) {
                        System.out.println("Reading ZIP Stream: " + ifile);
                        File ou = new File(System.getProperty("user.dir"), ofile);
                        ou.mkdir();
                        ZipHandler.copyFile(ifile, ou.getPath());
                    } else if (mode.equals("view")) {
                        System.out.println("Reading ZIP Stream: " + ifile + "");
                        File ou = new File(System.getProperty("user.dir"), ofile);
                        ou.mkdir();
                        ZipHandler.viewArc(ifile);
                    }
                    System.out.println("Done!");
                    System.exit(0);
                }
                if (!view && !verify && !find) {
                    if (eSingle) {
                        System.out.println("Searching File: " + ofile);
                    } else {
                        System.out.println("De-compressing File: " + ifile);
                    }
                } else {
                    System.out.println("Archive Content for: " + ifile);
                }
                FileInputStream fin = null;
                File test = new File(ifile);
                if (!test.exists() || test.isDirectory()) {
                    System.out.println("Not a valid input file. Input file should be a Barred compressed file.");
                    System.exit(0);
                }
                try {
                    fin = new FileInputStream(ifile);
                } catch (Exception e) {
                    System.out.println("Invalid File!");
                    System.exit(0);
                }
                boolean isDir = false;
                char fsep = '\\';
                byte[] dig = null;
                int fb = fin.read();
                if (fb == 2) {
                    isDir = true;
                    fsep = (char) fin.read();
                }
                if (fb == 10) {
                    isDir = true;
                    fsep = (char) fin.read();
                    int _diglen = (byte) fin.read();
                    dig = new byte[_diglen];
                    fin.read(dig);
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    System.out.println("Archive Protected. Enter the passphrase:");
                    pass = br.readLine();
                    if (!Digest.verifyDigest(Digest.getDigest(pass.getBytes()), dig)) {
                        System.out.println("Invalid passphrase!");
                        System.exit(0);
                    }
                }
                if (fb == 9) {
                    int _diglen = (byte) fin.read();
                    dig = new byte[_diglen];
                    fin.read(dig);
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    System.out.println("File Protected. Enter the passphrase:");
                    pass = br.readLine();
                    if (!Digest.verifyDigest(Digest.getDigest(pass.getBytes()), dig)) {
                        System.out.println("Invalid passphrase!");
                        System.exit(0);
                    }
                }
                if (fb == 20) {
                    byte len[] = new byte[2];
                    fin.read(len);
                    ByteBuffer bu0 = ByteBuffer.wrap(len);
                    short clen = bu0.getShort();
                    byte com[] = new byte[clen];
                    fin.read(com);
                    System.out.println("\n\".." + new String(com) + "..\"");
                    isDir = true;
                    fsep = (char) fin.read();
                }
                if (fb == 100) {
                    byte len[] = new byte[2];
                    fin.read(len);
                    ByteBuffer bu0 = ByteBuffer.wrap(len);
                    short clen = bu0.getShort();
                    byte com[] = new byte[clen];
                    fin.read(com);
                    isDir = true;
                    fsep = (char) fin.read();
                    int _diglen = (byte) fin.read();
                    dig = new byte[_diglen];
                    fin.read(dig);
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    System.out.println("Archive Protected. Enter the passphrase:");
                    pass = br.readLine();
                    if (!Digest.verifyDigest(Digest.getDigest(pass.getBytes()), dig)) {
                        System.out.println("Invalid passphrase!");
                        System.exit(0);
                    }
                    System.out.println("\n\".." + new String(com) + "..\"");
                }
                if (isDir) {
                    if (!view && !verify && !eSingle && !find) {
                        test = new File(ofile);
                        test.mkdirs();
                        if (!test.isDirectory()) {
                            System.out.println("\r\nEnter a valid output directory");
                            System.exit(0);
                        }
                    }
                }
                if (!isDir) {
                    if (view) {
                        System.out.println("\r\nThe Barred file does not have DIR. information. No files to view! (You are trying to view a compressed file.)");
                        System.exit(0);
                    }
                    if (verify) {
                        System.out.println("\r\nThe Barred file does not have DIR. information. No files to check! (You are trying to verify a compressed file.)");
                        System.exit(0);
                    }
                }
                boolean act = false;
                if (view || find) {
                    act = true;
                }
                BARInputStream bin = new BARInputStream(fin, act);
                FileOutputStream fos = null;
                if (!isDir) {
                    if (!view && !verify) {
                        File check = new File(ofile);
                        if (check.exists()) {
                            check.delete();
                        }
                        fos = new FileOutputStream(check, true);
                    }
                }
                int aIndex = 0;
                int aSize = 0;
                boolean aSizeFirst = true;
                boolean eSingleDone = false;
                System.out.print("Wait");
                while (fin.available() != 0) {
                    if (!view && !verify) {
                        System.out.print(".");
                    }
                    boolean _nextArt = false;
                    int _segid = fin.read();
                    if (_segid == 1) {
                        _nextArt = true;
                    }
                    if (isDir && _nextArt) {
                        byte[] _rlengthB = new byte[2];
                        fin.read(_rlengthB);
                        ByteBuffer buf = ByteBuffer.wrap(_rlengthB);
                        short rlength = buf.getShort();
                        byte[] _artifact = new byte[rlength];
                        fin.read(_artifact);
                        String artifact = new String(_artifact);
                        String cleaned = StringHelper.cleanString(artifact, fsep, File.separatorChar);
                        if (view) {
                            System.out.println("\r\n" + cleaned);
                            aIndex++;
                        }
                        if (verify) {
                            System.out.println("\r\nChecking..." + cleaned + " - ");
                            aIndex++;
                        }
                        if (find) {
                            if ((cleaned.indexOf(ofile) != -1)) {
                                System.out.println("    " + cleaned);
                                aIndex++;
                            }
                        }
                        if (!view && !verify && !eSingle && !find) {
                            File _art = new File(ofile, cleaned);
                            File parent = new File(_art.getParent());
                            if (!parent.exists()) {
                                parent.mkdirs();
                            }
                            fos = new FileOutputStream(_art);
                        }
                        if (eSingle) {
                            if (cleaned.equals(ofile)) {
                                File ini = new File("bar_ext");
                                ini.mkdir();
                                File esin = new File(ini, ofile);
                                File parent = new File(esin.getParent());
                                if (!parent.exists()) {
                                    parent.mkdirs();
                                }
                                fos = new FileOutputStream(esin);
                                eSingleDone = true;
                            }
                        }
                        aSize = 0;
                        int _dupId = fin.read();
                    }
                    byte byt[] = null;
                    try {
                        byt = bin.read();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("\r\nThe Archive is corrupted!");
                        System.exit(0);
                    }
                    if (verify) {
                        aSize = aSize + byt.length;
                        System.out.println("\r\nB: " + aSize + " Bytes " + "(" + ByteDisp.convert((long) aSize) + ")");
                    }
                    if (!view && !verify && !find) {
                        if (eSingle) {
                            if (eSingleDone) {
                                fos.write(byt);
                                fos.close();
                                System.out.println("\r\nDone. " + ofile + " is extracted into BARRED temp. dir. bar_ext.");
                                System.exit(0);
                            }
                        } else {
                            fos.write(byt);
                        }
                    }
                }
                if (view || verify || find) {
                    System.out.println("\r\nTotal Files: " + aIndex);
                }
                fin.close();
                if (!isDir && !view && !find) {
                    fos.close();
                }
                if (!view && !verify && !eSingle && !find) {
                    System.out.println("\r\nSaved as: " + ofile);
                }
                if (eSingle && !eSingleDone) {
                    System.out.println("\r\nThe specified file: " + ofile + " is not in the archive " + ifile + ". Please use -v and view the archive content. Note down the prefix/suffix, if any and try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
