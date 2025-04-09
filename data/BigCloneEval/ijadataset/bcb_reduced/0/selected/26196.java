package recfganalyz;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class NewBarMaker {

    ZipFile barFile = null;

    /**
	 * prende in ingresso un tipo File @file (vediamo dopo se pu� essere una cartella in modo da metterci nel bar anche hook e mapper etctec ) 
	 * 	e lo comprime dandogli il nome passato @newbarfilename
	 *  
	 *  @param file : file da comprimere (per ora un xpdl..ma vediamo se passare anche una dir) 
	 *  @param newbarfilename : nome da dare al bar
	 *  @return un File (sostanzialmente un zip con estensione .bar) 
	 */
    public static File createBar(File file, String newbarfilename) {
        try {
            if (newbarfilename.toLowerCase().endsWith(".xpdl")) newbarfilename = newbarfilename.toLowerCase().replace(".xpdl", "");
            File barcreato = new File(newbarfilename + ".bar");
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(barcreato)));
            byte[] data = new byte[1000];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file.getCanonicalPath()));
            int count;
            out.putNextEntry(new ZipEntry(file.getName()));
            while ((count = in.read(data, 0, 1000)) != -1) {
                out.write(data, 0, count);
            }
            in.close();
            out.flush();
            out.close();
            System.out.println("BAR File successfully created!");
            return barcreato;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
