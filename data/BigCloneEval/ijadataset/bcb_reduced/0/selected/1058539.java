package org.bd.banglasms.bangla;

import javax.microedition.lcdui.Image;
import org.bd.banglasms.App;
import org.bd.banglasms.Logger;

/**
 * This class maps GSM 7bit characters with BanglaSMS characters.
 *
 */
class Converter {

    String dir;

    final boolean debugOn = false;

    private String modeDir;

    static final int[] imgArray = { 0, 10, 20, 100, 101, 102, 103, 110, 111, 112, 120, 200, 201, 210, 211, 220, 221, 230, 300, 301, 310, 311, 320, 321, 330, 400, 401, 410, 411, 420, 430, 500, 501, 510, 511, 520, 530, 600, 601, 610, 611, 612, 620, 621, 630, 700, 701, 710, 711, 712, 713, 720, 721, 722, 730, 800, 801, 802, 810, 811, 820, 821, 830, 900, 901, 910, 1000, 1001, 1002, 1010, 1011, 1012, 1020, 1021, 1022, 1030, 1031, 1032, 1033, 1034 };

    static final int[] gsmArray = { ' ', '!', '"', '#', '$', '%', '\'', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '�', '�', '�', '�', '�', '�' };

    static final int[] imgEngArray = { 1, 2, 11, 12, 21, 22, 101, 102, 111, 112, 121, 122, 131, 132, 141, 142, 201, 202, 211, 212, 221, 222, 231, 232, 301, 302, 311, 312, 321, 322, 331, 332, 401, 402, 411, 412, 421, 422, 431, 432, 501, 502, 511, 512, 521, 522, 531, 532, 601, 602, 611, 612, 621, 622, 631, 632, 701, 702, 711, 712, 721, 722, 731, 732, 741, 742, 801, 802, 811, 812, 821, 822, 831, 832, 901, 902, 911, 912, 921, 922, 931, 932, 941, 942 };

    static final char[] gsmEngArray = { ' ', ' ', '0', '0', '�', '�', '.', '.', ',', ',', '?', '?', '!', '!', '1', '1', 'A', 'a', 'B', 'b', 'C', 'c', '2', '2', 'D', 'd', 'E', 'e', 'F', 'f', '3', '3', 'G', 'g', 'H', 'h', 'I', 'i', '4', '4', 'J', 'j', 'K', 'k', 'L', 'l', '5', '5', 'M', 'm', 'N', 'n', 'O', 'o', '6', '6', 'P', 'p', 'Q', 'q', 'R', 'r', 'S', 's', '7', '7', 'T', 't', 'U', 'u', 'V', 'v', '8', '8', 'W', 'w', 'X', 'x', 'Y', 'y', 'Z', 'z', '9', '9' };

    static final int[] imgConjArray = { 46250, 48248, 51246, 51251, 58251, 58271, 62271, 64273, 66265, 66267, 66283, 66285, 66287, 68265, 68277, 70271, 70277, 72250, 72251, 72269, 72287, 72290, 72396, 74247, 78271, 78287, 78290, 84271, 84278, 84287, 84288, 85247, 85267, 85271, 86277, 86290, 88271, 90001, 90002, 90003, 90004, 90005, 90006, 90007, 90008, 90009, 90010 };

    static final char[] gsmConjArray = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u' };

    Converter(String dir) {
        this.dir = dir;
    }

    public char getGSM(int key, int times, int pos, boolean bMode) {
        char c = ' ';
        int searchKey = key * 100 + times * 10 + pos;
        if (bMode) {
            int loc = this.binarySearch(imgArray, searchKey);
            c = (char) gsmArray[loc];
            if (debugOn) System.out.println("For search Key: " + searchKey + " at location: " + loc + " character found: " + c);
        } else {
            int loc = this.binarySearch(imgEngArray, searchKey);
            c = gsmEngArray[loc];
            if (debugOn) System.out.println("For search Key: " + searchKey + " at location: " + loc + " character found: " + c);
        }
        return c;
    }

    public char getGSM(int key) {
        return this.gsmConjArray[key];
    }

    public BanglaFont getFont(char gsm, boolean bMode, boolean esc) {
        BanglaFont font;
        Image fontImage = null;
        String imageString;
        int loc;
        int imageKey;
        int key;
        int rem;
        int times;
        int pos;
        if (esc) {
            modeDir = BanglaRenderer.conjDir;
            loc = linearSearch(gsmConjArray, gsm);
            imageKey = imgConjArray[loc];
            imageString = dir + modeDir + "_" + imageKey + ".png";
            key = -1;
            times = -1;
            pos = -1;
        } else {
            if (bMode) {
                modeDir = BanglaRenderer.bangDir;
                loc = binarySearch(gsmArray, gsm);
                imageKey = imgArray[loc];
            } else {
                modeDir = BanglaRenderer.engDir;
                loc = linearSearch(gsmEngArray, gsm);
                imageKey = imgEngArray[loc];
            }
            key = imageKey / 100;
            rem = imageKey % 100;
            times = rem / 10;
            pos = rem % 10;
            imageString = dir + modeDir + key + "_" + times + "_" + pos + ".png";
        }
        try {
            fontImage = App.getResourceManager().fetchImage(imageString);
        } catch (Exception e) {
            App.getLogger().log("Converter.getFont( " + gsm + "," + bMode + "," + esc + " caught while trying to load image : " + e, Logger.LEVEL_ERROR);
        }
        font = new BanglaFont(fontImage, key, times, pos);
        return font;
    }

    private int binarySearch(int[] A, int N) {
        int lowestPossibleLoc = 0;
        int highestPossibleLoc = A.length - 1;
        while (highestPossibleLoc >= lowestPossibleLoc) {
            int middle = (lowestPossibleLoc + highestPossibleLoc) / 2;
            if (A[middle] == N) {
                return middle;
            } else if (A[middle] > N) {
                highestPossibleLoc = middle - 1;
            } else {
                lowestPossibleLoc = middle + 1;
            }
        }
        return -1;
    }

    public int linearSearch(char[] tbl, char n) {
        for (int i = 0; i < tbl.length; i++) {
            if (n == tbl[i]) return i;
        }
        return -1;
    }
}
