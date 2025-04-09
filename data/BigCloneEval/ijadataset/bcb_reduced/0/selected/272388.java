package cloudSMAP;

import java.io.IOException;
import java.util.Arrays;
import org.apache.hadoop.io.BytesWritable;

public class DNAString {

    public static final byte dna_a = (byte) 0x00;

    public static final byte dna_c = (byte) 0x01;

    public static final byte dna_g = (byte) 0x02;

    public static final byte dna_t = (byte) 0x04;

    public static final byte dna_n = (byte) 0x08;

    public static final byte space = (byte) 0x0F;

    public static final byte hardstop = (byte) 0xFF;

    public static byte[] letterToDNA = initializeLetterToDNA();

    public static byte[] letterToSeed = initializeLetterToSeed();

    public static byte[] dnaToLetter = initializeDNAToLetter();

    public static byte[] seedToLetter = initializeSeedToLetter();

    public static byte[] rcLetter = initializeRC();

    public static final byte[] nostr = new byte[0];

    private static final StringBuilder builder = new StringBuilder();

    public static String VERSION = "2.0";

    public static byte[] initializeRC() {
        byte[] retval = new byte[256];
        java.util.Arrays.fill(retval, (byte) 'N');
        retval['A'] = 'T';
        retval['a'] = 't';
        retval['T'] = 'A';
        retval['t'] = 'a';
        retval['C'] = 'G';
        retval['c'] = 'g';
        retval['G'] = 'C';
        retval['g'] = 'c';
        return retval;
    }

    public static byte rc(byte letter) {
        return rcLetter[letter & 0xFF];
    }

    public static byte[] rcarr_new(byte[] arr) {
        byte[] retval = new byte[arr.length];
        for (int i = 0; i < arr.length; i++) {
            retval[arr.length - 1 - i] = rcLetter[arr[i] & 0xFF];
        }
        return retval;
    }

    public static void rcarr_inplace(byte[] arr) {
        int i = 0, j = arr.length - 1;
        for (; i < j; i++, j--) {
            byte t = arr[i];
            arr[i] = rcLetter[arr[j] & 0xFF];
            arr[j] = rcLetter[t & 0xFF];
        }
        if (i == j) {
            arr[i] = rcLetter[arr[i] & 0xFF];
        }
    }

    public static byte[] initializeLetterToDNA() {
        byte[] retval = new byte[256];
        java.util.Arrays.fill(retval, dna_n);
        retval['A'] = dna_a;
        retval['a'] = dna_a;
        retval['C'] = dna_c;
        retval['c'] = dna_c;
        retval['G'] = dna_g;
        retval['g'] = dna_g;
        retval['T'] = dna_t;
        retval['T'] = dna_t;
        retval[' '] = space;
        retval[';'] = hardstop;
        return retval;
    }

    public static byte[] initializeLetterToSeed() {
        byte[] retval = new byte[256];
        java.util.Arrays.fill(retval, (byte) 0);
        retval['A'] = 0;
        retval['a'] = 0;
        retval['C'] = 1;
        retval['c'] = 1;
        retval['G'] = 2;
        retval['g'] = 2;
        retval['T'] = 3;
        retval['T'] = 3;
        return retval;
    }

    public static byte[] initializeSeedToLetter() {
        byte[] retval = new byte[256];
        java.util.Arrays.fill(retval, (byte) 'N');
        retval[0] = 'A';
        retval[1] = 'C';
        retval[2] = 'G';
        retval[3] = 'T';
        return retval;
    }

    public static byte[] initializeDNAToLetter() {
        byte[] retval = new byte[256];
        java.util.Arrays.fill(retval, (byte) 'N');
        retval[dna_a & 0xFF] = 'A';
        retval[dna_c & 0xFF] = 'C';
        retval[dna_g & 0xFF] = 'G';
        retval[dna_t & 0xFF] = 'T';
        retval[dna_n & 0xFF] = 'N';
        retval[space & 0xFF] = ' ';
        retval[hardstop & 0xFF] = ';';
        return retval;
    }

    public static int dnaArrLen(byte[] arr) {
        int retval = arr.length * 2;
        if (arr.length > 0) {
            if ((arr[arr.length - 1] & 0x0F) == space) {
                retval--;
            }
        }
        return retval;
    }

    public static byte byteToDNA(byte letter) {
        return letterToDNA[letter & 0xFF];
    }

    public static byte byteToSeed(byte letter) {
        return letterToSeed[letter & 0xFF];
    }

    public static byte seedToByte(int seed) {
        return seedToLetter[seed & 0x03];
    }

    public static byte dnaToByte(byte dna) {
        return dnaToLetter[dna & 0xFF];
    }

    public static int arrToDNALen(int len) {
        return (len + 1) / 2;
    }

    public static int arrToSeedLen(int len, int REDUNDANCY) {
        if (REDUNDANCY > 1) {
            return ((len + 3) / 4) + 1 + 1;
        }
        return (len + 3) / 4 + 1;
    }

    public static int arrToSeed(byte[] arr, int arrpos, int len, byte[] seed, int seedpos, int id, int REDUNDANCY, int ISQRY) {
        int seedlen = (len + 3) / 4 + 1;
        int arrend = arrpos + len;
        while (arrpos + 3 < arrend) {
            seed[seedpos] = (byte) (((byteToSeed(arr[arrpos]) << 6) | (byteToSeed(arr[arrpos + 1]) << 4) | (byteToSeed(arr[arrpos + 2]) << 2) | (byteToSeed(arr[arrpos + 3]))));
            seedpos++;
            arrpos += 4;
        }
        int remaining = arrend - arrpos;
        if (remaining == 3) {
            seed[seedpos] = (byte) (((byteToSeed(arr[arrpos]) << 6) | (byteToSeed(arr[arrpos + 1]) << 4) | (byteToSeed(arr[arrpos + 2]) << 2)));
            seedpos++;
        } else if (remaining == 2) {
            seed[seedpos] = (byte) (((byteToSeed(arr[arrpos]) << 6) | (byteToSeed(arr[arrpos + 1]) << 4)));
            seedpos++;
        } else if (remaining == 1) {
            seed[seedpos] = (byte) (((byteToSeed(arr[arrpos]) << 6)));
            seedpos++;
        }
        if (REDUNDANCY > 1) {
            seed[seedpos] = (byte) ((id % REDUNDANCY) & 0xff);
            seedpos++;
            seedlen++;
        }
        seed[seedpos] = (byte) ISQRY;
        return seedlen;
    }

    public static boolean repseed(byte[] seq, int start, int SEED_LEN) {
        byte first = seq[start];
        for (int i = 1; i < SEED_LEN; i++) {
            if (seq[i + start] != first) {
                return false;
            }
        }
        return true;
    }

    public static byte[] seedToArr(byte[] seed, int SEEDLEN, int REDUNDANCY) {
        byte[] retval = new byte[SEEDLEN];
        int outpos = 0;
        int seedpos = 0;
        int slen = arrToSeedLen(SEEDLEN, 0);
        while (seedpos < slen - 2) {
            retval[outpos] = seedToByte(seed[seedpos] >> 6);
            retval[outpos + 1] = seedToByte(seed[seedpos] >> 4);
            retval[outpos + 2] = seedToByte(seed[seedpos] >> 2);
            retval[outpos + 3] = seedToByte(seed[seedpos]);
            outpos += 4;
            seedpos++;
        }
        int diff = SEEDLEN - outpos;
        if (diff == 4) {
            retval[outpos] = seedToByte(seed[seedpos] >> 6);
            retval[outpos + 1] = seedToByte(seed[seedpos] >> 4);
            retval[outpos + 2] = seedToByte(seed[seedpos] >> 2);
            retval[outpos + 3] = seedToByte(seed[seedpos]);
        } else if (diff == 3) {
            retval[outpos] = seedToByte(seed[seedpos] >> 6);
            retval[outpos + 1] = seedToByte(seed[seedpos] >> 4);
            retval[outpos + 2] = seedToByte(seed[seedpos] >> 2);
        } else if (diff == 2) {
            retval[outpos] = seedToByte(seed[seedpos] >> 6);
            retval[outpos + 1] = seedToByte(seed[seedpos] >> 4);
        } else if (diff == 1) {
            retval[outpos] = seedToByte(seed[seedpos] >> 6);
        }
        return retval;
    }

    public static int arrToDNAStr(byte[] arr, int arrpos, int len, byte[] out, int outpos) {
        int dnalen = (len + 1) / 2;
        int arrend = arrpos + len;
        while (arrpos + 1 < arrend) {
            out[outpos] = (byte) ((byteToDNA(arr[arrpos]) << 4) | (byteToDNA(arr[arrpos + 1])));
            outpos++;
            arrpos += 2;
        }
        if (arrpos < arrend) {
            out[outpos] = (byte) ((byteToDNA(arr[arrpos]) << 4) | (space));
        }
        return dnalen;
    }

    public static int arrToDNAStrRev(byte[] arr, int arrstart, int len, byte[] out, int outpos) {
        int dnalen = (len + 1) / 2;
        int arrpos = arrstart + len - 1;
        while (arrpos > arrstart) {
            out[outpos] = (byte) ((byteToDNA(arr[arrpos]) << 4) | (byteToDNA(arr[arrpos - 1])));
            outpos++;
            arrpos -= 2;
        }
        if (arrpos == arrstart) {
            out[outpos] = (byte) ((byteToDNA(arr[arrpos]) << 4) | (space));
        }
        return dnalen;
    }

    public static byte[] arrToDNA(byte[] arr, int start, int len) {
        int dnalen = (len + 1) / 2;
        byte[] dna = new byte[dnalen];
        arrToDNAStr(arr, start, len, dna, 0);
        return dna;
    }

    public static byte[] arrToDNA(byte[] arr) {
        return arrToDNA(arr, 0, arr.length);
    }

    public static byte[] dnaToArr(byte[] dna, int dnapos, int dnalen) {
        if (dnalen == 0) {
            return nostr;
        }
        int arrlen = dnalen * 2;
        if ((dna[dnapos + dnalen - 1] & 0x0F) == space) {
            arrlen--;
        }
        byte[] arr = new byte[arrlen];
        int arrpos = 0;
        int dnaend = dnapos + dnalen - 1;
        while (dnapos < dnaend) {
            arr[arrpos] = dnaToByte((byte) ((dna[dnapos] & 0xF0) >> 4));
            arr[arrpos + 1] = dnaToByte((byte) ((dna[dnapos] & 0x0F)));
            dnapos++;
            arrpos += 2;
        }
        arr[arrpos] = dnaToByte((byte) ((dna[dnapos] & 0xF0) >> 4));
        arrpos++;
        if ((dna[dnapos] & 0x0F) != space) {
            arr[arrpos] = dnaToByte((byte) ((dna[dnapos] & 0x0F)));
        }
        return arr;
    }

    public static byte[] dnaToArr(byte[] dna) {
        return dnaToArr(dna, 0, dna.length);
    }

    public static byte[] stringToBytes(String src) {
        int srclen = src.length();
        byte[] ret = new byte[srclen];
        for (int i = 0; i < srclen; i++) {
            ret[i] = (byte) src.charAt(i);
        }
        return ret;
    }

    public static String bytesToString(byte[] arr) {
        builder.setLength(0);
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                builder.append((char) arr[i]);
            }
        }
        return builder.toString();
    }

    public static String bytesToString(byte[] arr, int start, int len) {
        builder.setLength(0);
        if (arr != null) {
            for (int i = 0; i < len; i++) {
                builder.append((char) arr[start + i]);
            }
        }
        return builder.toString();
    }

    public static byte[] reverseBytes(byte[] arr) {
        return reverseBytes(arr, arr.length);
    }

    public static byte[] reverseBytes(byte[] arr, int len) {
        for (int i = 0, j = len - 1; i < j; i++, j--) {
            byte t = arr[i];
            arr[i] = arr[j];
            arr[j] = t;
        }
        return arr;
    }

    public static byte[] bytesWritableDNAToArr(BytesWritable bw) {
        return dnaToArr(bw.getBytes(), 0, bw.getLength());
    }

    public static boolean arrHasN(byte[] seq, int start, int len) {
        for (int n = start; n < start + len; n++) {
            if (DNAString.letterToDNA[seq[n]] == DNAString.dna_n) {
                return true;
            }
        }
        return false;
    }

    public static void testConvert(String seq) throws IOException {
        byte[] orig = stringToBytes(seq);
        byte[] dna = arrToDNA(orig);
        byte[] letter = dnaToArr(dna);
        if (!Arrays.equals(orig, letter)) {
            throw new IOException("err \"" + bytesToString(orig) + "\"[" + orig.length + "] != \"" + bytesToString(letter) + "\"[" + letter.length + "] dnalen:" + dna.length);
        }
    }

    public static void quickcopy(byte[] src, int srcpos, byte[] dest, int destpos, int len) {
        for (; len > 0; len--, destpos++, srcpos++) {
            dest[destpos] = src[srcpos];
        }
    }

    public static void printHex(String name, byte[] arr, int offset, int len) {
        System.out.print(name + "[" + arr.length + "]:");
        for (int i = offset; i < len; i++) {
            System.out.print(' ');
            String hex = Integer.toHexString(0xff & arr[i]);
            if (hex.length() < 2) {
                hex = "0" + hex;
            }
            System.out.print(hex);
        }
        System.out.println();
    }

    public static void printHex(String name, byte[] arr) {
        printHex(name, arr, 0, arr.length);
    }
}
