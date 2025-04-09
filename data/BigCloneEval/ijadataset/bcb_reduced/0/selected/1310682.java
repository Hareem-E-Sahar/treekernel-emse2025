package com.mdps.mactive;

import java.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

/**
 * @author coordt
 *
 */
public class MactiveEPS extends MactiveBlob {

    protected int mHeight;

    protected int mWidth;

    ByteArrayOutputStream mEPSFile;

    protected class CMYKColor {

        public double c;

        public double m;

        public double y;

        public double k;

        public CMYKColor(double cyan, double magenta, double yellow, double black) {
            c = cyan;
            m = magenta;
            y = yellow;
            k = black;
        }
    }

    public MactiveEPS(MactiveConnection theCon) {
        super(theCon);
    }

    public MactiveEPS(MactiveConnection con, String adNumber, String pickupNumber, byte[] blob) {
        super(con, adNumber, pickupNumber, blob);
        if ((blob == null) && (con != null) && !adNumber.equals("")) {
            byte[] newblob = con.getBlobByAdNumber(adNumber, MactiveConnection.EPSBLOB);
            super.SetBlob(newblob, adNumber);
        }
    }

    public void processBlob() {
        mEPSFile = new ByteArrayOutputStream();
        try {
            StripPreview();
            FindHeightWidth();
            AttachHeader();
            ConvertSpotColors();
            try {
                if (mBlob.length != 0) EmbedLogos();
            } catch (Exception e) {
                System.out.println("EmbedLogos: Received an error: " + e.getMessage());
                System.out.println(e.getStackTrace());
            }
        } catch (Exception e) {
            System.out.println("ProcessBlob: Received an error: " + e.getMessage());
            System.out.println(e.getStackTrace());
        }
        if (mEPSFile == null) System.out.println("mEPSFile is NULL");
    }

    public int getHeight() {
        return mHeight;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getLength() {
        return mEPSFile.size();
    }

    public ByteArrayOutputStream getEPSFile() {
        if (mEPSFile != null) return mEPSFile; else {
            System.out.println("The mEPSFile of Ad Number " + mAdNumber + " is NULL!");
            return new ByteArrayOutputStream();
        }
    }

    protected void StripPreview() {
    }

    protected void FindHeightWidth() {
        int pos1 = 0;
        int pos2 = 0;
        String blobAsString = new String(mBlob);
        String bbox = "";
        pos1 = blobAsString.indexOf("%%BoundingBox:");
        pos2 = blobAsString.indexOf("\r", pos1);
        bbox = blobAsString.substring(pos1, pos2);
        mWidth = Integer.valueOf(bbox.replaceAll("%%BoundingBox: \\d* \\d* (\\d*) \\d*.*", "$1")).intValue();
        mHeight = Integer.valueOf(bbox.replaceAll("%%BoundingBox: \\d* \\d* \\d* (\\d*).*", "$1")).intValue();
        mWidth = Integer.valueOf(bbox.replaceAll("%%BoundingBox: \\d* \\d* (\\d*) \\d*.*", "$1")).intValue();
    }

    protected void AttachHeader() throws IOException {
    }

    protected CMYKColor RGBToCMYK(double r, double g, double b) {
        CMYKColor out = new CMYKColor(0.0, 0.0, 0.0, 0.0);
        double min = 0.0;
        if (r > 1) r = 1;
        if (g > 1) g = 1;
        if (b > 1) b = 1;
        out.c = 1 - r;
        out.m = 1 - g;
        out.y = 1 - b;
        min = out.c;
        if (out.m < min) min = out.m;
        if (out.y < min) min = out.y;
        out.k = min;
        if (min > 0) {
            out.c = out.c - out.k;
            out.m = out.m - out.k;
            out.y = out.y - out.k;
        }
        return out;
    }

    protected String GetCMYKCommand(String rgbCommand) {
        DecimalFormat dfmt = new DecimalFormat("#0.0000");
        String[] cmdParams = rgbCommand.split(" ");
        String cmykCommand = "";
        CMYKColor theColor = RGBToCMYK(Double.valueOf(cmdParams[0]), Double.valueOf(cmdParams[1]), Double.valueOf(cmdParams[2]));
        cmykCommand = dfmt.format(theColor.c) + " " + dfmt.format(theColor.m) + " ";
        cmykCommand += dfmt.format(theColor.y) + " " + dfmt.format(theColor.k) + " ";
        cmykCommand += "setcmykcolor";
        return cmykCommand;
    }

    /**
	 * This command converts the setrgbcolor to setcmykcolor
	 *
	 * It completely replaces the mBlob variable and should be done before
	 * filling in logos.
	 *
	 */
    protected void ConvertSpotColors() {
        int pos = 0, lastPos = 0, prevLastPos = 0;
        String blobAsString = new String(mBlob);
        String rgbCommand = "";
        String cmykCommand = "";
        ByteArrayOutputStream theBlob = new ByteArrayOutputStream();
        Pattern p = Pattern.compile("\\d+.\\d+ \\d+.\\d+ \\d+.\\d+ setrgbcolor");
        Matcher m = p.matcher(blobAsString);
        while (m.find()) {
            lastPos = m.end();
            pos = m.start();
            rgbCommand = blobAsString.substring(pos, lastPos);
            cmykCommand = GetCMYKCommand(rgbCommand);
            theBlob.write(mBlob, prevLastPos, pos - prevLastPos);
            theBlob.write(cmykCommand.getBytes(), 0, cmykCommand.length());
            prevLastPos = lastPos;
        }
        theBlob.write(mBlob, prevLastPos, mBlob.length - prevLastPos);
        mBlob = theBlob.toByteArray();
    }

    protected void EmbedLogos() {
        int pos = 0, lastPos = 0, prevLastPos = 0;
        String epsCode = "";
        String[] codeParams = null;
        byte[] logo = null;
        String logoHeader = "";
        String logoTrailer = "";
        String blobAsString = new String(mBlob);
        do {
            if ((pos = blobAsString.indexOf("%MactiveGraphic", lastPos)) != -1) {
                lastPos = blobAsString.indexOf("\r", pos);
                epsCode = blobAsString.substring(pos, lastPos);
                codeParams = epsCode.split(" ");
                logoHeader = GetLogoHeader(codeParams[2], codeParams[3], codeParams[4], codeParams[5]);
                logo = GetEmbededLogo(codeParams[1]);
                logoTrailer = GetLogoTrailer();
                mEPSFile.write(mBlob, prevLastPos, pos - prevLastPos);
                mEPSFile.write(logoHeader.getBytes(), 0, logoHeader.length());
                mEPSFile.write(logo, 0, logo.length);
                mEPSFile.write(logoTrailer.getBytes(), 0, logoTrailer.length());
                prevLastPos = lastPos;
            } else {
                mEPSFile.write(mBlob, prevLastPos, mBlob.length - prevLastPos);
            }
        } while (pos != -1);
    }

    protected String GetLogoHeader(String param1, String param2, String param3, String param4) {
        String logoHeader = "";
        DecimalFormat df1 = new DecimalFormat("####.000000");
        logoHeader += "/b4_Inc_state save def\r\n";
        logoHeader += " /dict_count countdictstack def\r\n";
        logoHeader += " /op_count count 1 sub def\r\n";
        logoHeader += " userdict begin\r\n";
        logoHeader += " /showpage {} def\r\n";
        logoHeader += " 0 setgray 0 setlinecap\r\n";
        logoHeader += " 1 setlinewidth 0 setlinejoin\r\n";
        logoHeader += " 10 setmiterlimit [] 0 setdash newpath\r\n";
        logoHeader += " /languagelevel where\r\n";
        logoHeader += " {pop languagelevel where\r\n";
        logoHeader += " 1 ne\r\n";
        logoHeader += "    {false setstrokeadjust false setoverprint\r\n";
        logoHeader += "    } if\r\n";
        logoHeader += " } if\r\n";
        logoHeader += param1 + " " + param2 + " translate\r\n";
        logoHeader += df1.format(Double.valueOf(param4).doubleValue() / 5.000000) + " ";
        logoHeader += df1.format(Double.valueOf(param3).doubleValue() / 5.000000) + " scale\r\n";
        return logoHeader;
    }

    protected String GetLogoTrailer() {
        String logoTrailer = "count op_count sub {pop} repeat\r\n";
        logoTrailer += " countdictstack dict_count sub {end} repeat\r\n";
        logoTrailer += " b4_Inc_state restore\r";
        return logoTrailer;
    }

    protected byte[] GetEmbededLogo(String logoID) {
        byte[] logo = null;
        String blobAsString = null;
        byte[] blobAsBytes = null;
        int bloblength = 0;
        blobAsBytes = macCon.GetLogo(Integer.valueOf(logoID), mMactiveAdNumber, mPickupNumber);
        blobAsString = new String(blobAsBytes);
        bloblength = (int) blobAsBytes.length;
        logo = new byte[bloblength];
        long pos = 0;
        int endpos = 0;
        pos = blobAsString.indexOf("%!PS");
        if (pos == 0) return new byte[0];
        endpos = blobAsString.indexOf("%EOF") + 5;
        if (endpos < 5) endpos = (int) blobAsString.length();
        System.arraycopy(blobAsBytes, (int) pos, logo, 0, endpos - (int) pos);
        return logo;
    }
}
