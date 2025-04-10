package eyes.blue;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import android.util.Log;

public class Util {

    static String logTag = "LamrimReader";

    public static boolean unZip(String zipname, String extractTo) {
        InputStream is;
        ZipInputStream zis;
        try {
            is = new FileInputStream(extractTo + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int count;
                String filename = ze.getName();
                FileOutputStream fout = new FileOutputStream(extractTo + filename);
                while ((count = zis.read(buffer)) != -1) {
                    baos.write(buffer, 0, count);
                    byte[] bytes = baos.toByteArray();
                    fout.write(bytes);
                    baos.reset();
                }
                fout.flush();
                fout.close();
                zis.closeEntry();
            }
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean isFileCorrect(String fileName, long crc32) throws Exception {
        return isFileCorrect(new File(fileName), crc32);
    }

    public static boolean isFileCorrect(File file, long crc32) throws Exception {
        long startTime = System.currentTimeMillis();
        Checksum checksum = new CRC32();
        InputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[16384];
        int readLen = -1;
        while ((readLen = fis.read(buffer)) != -1) checksum.update(buffer, 0, readLen);
        fis.close();
        long sum = checksum.getValue();
        boolean isCorrect = (crc32 == sum);
        int spend = (int) (System.currentTimeMillis() - startTime);
        Log.d(logTag, "Utility Check file: File index: " + file.getAbsolutePath() + ", length: " + file.length() + ", CRC32 check: " + ((isCorrect) ? " Correct!" : " Error!" + " (" + sum + "/" + crc32) + "), spend time: " + spend + "ms");
        return isCorrect;
    }

    public static SubtitleElement[] loadSubtitle(File file) {
        ArrayList<SubtitleElement> subtitleList = new ArrayList<SubtitleElement>();
        try {
            System.out.println("Open " + file.getAbsolutePath() + " for read subtitle.");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String stemp;
            int lineCounter = 0;
            int step = 0;
            int serial = 0;
            SubtitleElement se = null;
            while ((stemp = br.readLine()) != null) {
                lineCounter++;
                if (step == 0) {
                    if (stemp.matches("[0-9]+")) {
                        se = new SubtitleElement();
                        serial = Integer.parseInt(stemp);
                        step = 1;
                    }
                } else if (step == 1) {
                    if (stemp.matches("[0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3} +-+> +[0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}")) {
                        int startTimeMs;
                        String ts = stemp.substring(0, 2);
                        startTimeMs = Integer.parseInt(ts) * 3600000;
                        ts = stemp.substring(3, 5);
                        startTimeMs += Integer.parseInt(ts) * 60000;
                        ts = stemp.substring(6, 8);
                        startTimeMs += Integer.parseInt(ts) * 1000;
                        ts = stemp.substring(9, 12);
                        startTimeMs += Integer.parseInt(ts);
                        se.startTimeMs = startTimeMs;
                        step = 2;
                    } else {
                        step = 0;
                    }
                } else if (step == 2) {
                    se.text = stemp;
                    step = 0;
                    subtitleList.add(se);
                    System.out.println("get Subtitle: " + stemp);
                    if (stemp.length() == 0) System.err.println("Load Subtitle: Warring: Get a Subtitle with no content at line " + lineCounter);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (SubtitleElement[]) subtitleList.toArray(new SubtitleElement[0]);
    }
}
