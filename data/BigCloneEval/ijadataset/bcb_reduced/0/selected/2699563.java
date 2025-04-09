package com.tdcs.lords.client.print;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Vector;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.Sides;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 *
 * @author david
 */
public class PrintMgr {

    public static boolean printPdfFiles(File[] files) throws MalformedURLException, PrintException, FileNotFoundException, IOException {
        DocFlavor doc_flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        PrintRequestAttributeSet attr_set = new HashPrintRequestAttributeSet();
        Copies copies = new Copies(1);
        Sides sides = Sides.ONE_SIDED;
        attr_set.add(javax.print.attribute.standard.MediaName.NA_LETTER_WHITE);
        attr_set.add(copies);
        attr_set.add(sides);
        PrintService[] service = PrintServiceLookup.lookupPrintServices(doc_flavor, attr_set);
        if (service.length == 0) {
            System.out.println("No specific services, looking up basic services");
            service = PrintServiceLookup.lookupPrintServices(doc_flavor, null);
        }
        System.out.println("Found serivces:  " + service.length);
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        PrinterDialog pdg = new PrinterDialog(f, true);
        pdg.setList(service);
        pdg.setSize(500, 400);
        pdg.setVisible(true);
        pdg.setModal(true);
        if (!pdg.isCancelled()) {
            PrintService ps = pdg.getSelectedService();
            System.out.println("Service selected was " + ps.getName());
            System.out.println("Selected Service attributes:  ");
            DocFlavor[] supFlavors = ps.getSupportedDocFlavors();
            for (int x = 0; x < supFlavors.length; x++) {
                System.out.println("\t" + supFlavors[x].getMimeType());
            }
            System.out.println("Printing " + files.length + " files.");
            for (int i = 0; i < files.length; i++) {
                System.out.println("Printing " + files[i].getAbsolutePath());
                FileInputStream fis = new FileInputStream(files[i]);
                DocPrintJob job = ps.createPrintJob();
                SimpleDoc sd = new SimpleDoc(fis, doc_flavor, null);
                job.print(sd, attr_set);
                fis.close();
            }
            f.dispose();
            f = null;
            return true;
        }
        System.out.println("Service was not selected.");
        f.dispose();
        f = null;
        return false;
    }

    public static void printFiles(PrintService service, Vector<File> files) throws FileNotFoundException, PrintException, IOException {
        DocFlavor doc_flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        PrintRequestAttributeSet attr_set = new HashPrintRequestAttributeSet();
        Copies copies = new Copies(1);
        Sides sides = Sides.ONE_SIDED;
        attr_set.add(javax.print.attribute.standard.MediaName.NA_LETTER_WHITE);
        attr_set.add(copies);
        attr_set.add(sides);
        System.out.println("Service selected was " + service.getName());
        System.out.println("Selected Service attributes:  ");
        System.out.println("Printing " + files.size() + " files.");
        Iterator<File> fit = files.iterator();
        while (fit.hasNext()) {
            File f = fit.next();
            System.out.println("Printing " + f.getAbsolutePath());
            FileInputStream fis = new FileInputStream(f);
            DocPrintJob job = service.createPrintJob();
            SimpleDoc sd = new SimpleDoc(fis, doc_flavor, null);
            job.print(sd, attr_set);
            fis.close();
        }
    }

    public static byte[] loadByteArray(File f) throws FileNotFoundException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(f);
        int i = 0;
        while ((i = fis.read()) != -1) {
            baos.write(i);
        }
        fis.close();
        return baos.toByteArray();
    }

    public static void main(String[] args) {
        JFileChooser jfc = new JFileChooser();
        jfc.setMultiSelectionEnabled(true);
        int val = jfc.showOpenDialog(new JFrame());
        if (val == jfc.APPROVE_OPTION) {
            File[] files = jfc.getSelectedFiles();
            try {
                printPdfFiles(files);
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (PrintException ex) {
                ex.printStackTrace();
            }
        }
        System.exit(0);
    }
}
