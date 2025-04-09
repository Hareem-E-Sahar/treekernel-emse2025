package Tot_PSE_Com;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class Zip {

    public void be(String zipFileName, String[] filesToZip, int mennyire, String akt_konyvtar) throws Exception {
        byte[] buffer = new byte[18024];
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
        switch(mennyire) {
            case 0:
                {
                    out.setLevel(Deflater.BEST_SPEED);
                    break;
                }
            case 1:
                {
                    out.setLevel(Deflater.DEFAULT_COMPRESSION);
                    break;
                }
            case 2:
                {
                    out.setLevel(Deflater.BEST_COMPRESSION);
                    break;
                }
        }
        for (int i = 0; i < filesToZip.length; i++) {
            FileInputStream in = new FileInputStream(filesToZip[i]);
            String utvonal = filesToZip[i].substring(akt_konyvtar.length() - 1, filesToZip[i].length());
            out.putNextEntry(new ZipEntry(utvonal));
            File u = new File(filesToZip[i]);
            if (u.isDirectory()) continue;
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
            in.close();
        }
        out.close();
    }

    public void ki(String inFileName, String destinationDirectory) throws Exception {
        int BUFFER = 2048;
        File sourceZipFile = new File(inFileName);
        File unzipDestinationDirectory = new File(destinationDirectory);
        ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);
        Enumeration zipFileEntries = zipFile.entries();
        while (zipFileEntries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
            String currentEntry = entry.getName();
            File destFile = new File(unzipDestinationDirectory, currentEntry);
            File destinationParent = destFile.getParentFile();
            destinationParent.mkdirs();
            if (!entry.isDirectory()) {
                BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
                int currentByte;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new FileOutputStream(destFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }
        }
        zipFile.close();
    }
}
