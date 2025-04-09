package org.isb.mzxml;

import org.isb.mzxml.ScanHeader;
import java.io.File;
import org.isb.mzxml.Chromatogram;
import org.isb.mzxml.Scan;
import java.io.*;
import org.isb.mzxml.MzXMLParser;

/**
 *
 * @author  jeddes
 */
public class MzXML {

    private ScanHeader[] scanHeaders;

    private Scan[] scans;

    private long[][] scanOffsetIndex;

    private File file;

    private MzXMLParser parser;

    /** Creates a new instance of MzXML */
    public MzXML(File file) {
        this.file = checkFilePath(file);
        parser = new MzXMLParser();
        parser.setParse_mode(MzXMLParser.OFFSETINDEX);
        scanOffsetIndex = parser.getScanOffsetIndex(file);
    }

    public static File checkFilePath(File check) {
        try {
            String filename = check.getName();
            File test = new File("./" + filename);
            return new File(test.getCanonicalPath());
        } catch (Exception ex) {
            return null;
        }
    }

    public MzXML(File file, long[][] scanOffsetIndex, ScanHeader[] scanHeaders) {
        this.file = checkFilePath(file);
        this.scanHeaders = scanHeaders;
        this.scanOffsetIndex = scanOffsetIndex;
        for (int s = 0; s < scanOffsetIndex.length - 1; s++) {
            for (int t = s + 1; t < scanOffsetIndex.length; s++) {
                if (scanOffsetIndex[s][0] > scanOffsetIndex[t][0]) {
                    long[] tmp = scanOffsetIndex[s];
                    scanOffsetIndex[s] = scanOffsetIndex[t];
                    scanOffsetIndex[t] = tmp;
                }
            }
        }
    }

    public String getFileName() {
        return file.getAbsolutePath();
    }

    public Chromatogram getChromatogram() {
        return new Chromatogram(scanHeaders);
    }

    public long[] getScanIndex() {
        long[] ret = new long[scanOffsetIndex.length + 1];
        for (int s = 0; s < scanOffsetIndex.length; s++) {
            ret[s] = getScanOffset(s);
        }
        return ret;
    }

    public long getScanOffset(int scanNumber) {
        int high = scanOffsetIndex.length, low = -1, test;
        while (high - low > 1) {
            test = (high + low) / 2;
            if (scanOffsetIndex[test][0] > scanNumber) high = test; else low = test;
        }
        if (low == -1 || scanOffsetIndex[low][0] != scanNumber) return -1; else return scanOffsetIndex[low][1];
    }

    /**
     * Read a particular scan from a MSXML file and return a generic Scan object
     * with it's data. Note: scanNumbers are 1-based, so scanNumber must be at
     * least 1 and be not greater than getScanCount() + 1
     */
    public Scan rap(int scan_number) {
        if (scan_number < scanOffsetIndex[0][0] || scan_number > scanOffsetIndex[scanOffsetIndex.length - 1][0]) return null;
        long offset = getScanOffset(scan_number);
        Scan ret = parser.rapScan(file, scan_number, offset);
        return ret;
    }

    public Scan rap(int scan_number, long offset) {
        Scan ret = parser.rapScan(file, scan_number, offset);
        return ret;
    }

    public Scan[] rapScans(int msLevel) {
        Scan[] ret = parser.getScans(file, msLevel);
        return ret;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("");
            System.out.println("JReadMzXML - Institute for Systems Biology");
            System.out.println("------------------------------------------");
            System.out.println("Usage:\t JReadMzXML <mzXML file>");
            System.out.println("");
        } else {
            File file = new File(args[0]);
            if (!file.exists()) {
                System.out.println("cannot find mzXML file '" + args[0] + "'");
                System.exit(0);
            } else {
                MzXML mzxml = new MzXML(file);
                System.out.println(mzxml.rapScans(2).length);
                while (true) {
                    System.out.print("Enter scan number: ");
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    int scan_number;
                    try {
                        String input = br.readLine();
                        System.out.println("seeking scan:\t" + input);
                        if (input.indexOf("q") == 0) {
                            System.exit(1);
                        } else {
                            scan_number = Integer.parseInt(input);
                            Scan scan = mzxml.rap(scan_number);
                            System.out.println("");
                            if (scan == null) {
                                System.out.println("No corresponding scan.");
                            } else {
                                System.out.println(scan.toString());
                            }
                            System.out.println("");
                        }
                    } catch (IOException ioe) {
                        System.out.println("IO error trying to read from System.in");
                        System.exit(1);
                    }
                }
            }
        }
    }
}
