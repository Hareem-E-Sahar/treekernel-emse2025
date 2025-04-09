package api.client.bpmModel.bprPacker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 
 * @author M. Heinbuch University of Siegen
 * 
 * This class creates a .bpr file in which are all relevant files to run a Web Service, including
 * wsdl, bpel, pdd, manifest
 *
 */
public class BPRMaker {

    /**
	 * 
	 * @param destinationFile the .bpr-file which should be created
	 * @param sourcePath the path in which the original files are located
	 * @param processName the name of the process
	 * @param extWSDLs 
	 */
    public static void createBPR(File destinationFile, String processName, String bpel, String wsdl, String pdd, String cat, Vector<String> extWSDLs) {
        try {
            ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(destinationFile));
            zout.setMethod(ZipOutputStream.DEFLATED);
            String mani = "Manifest-Version: 1.0\n Ant-Version: Apache Ant 1.6.5\n Created-By: 4.1.2 20060928 (prerelease) (Ubuntu 4.1.1-14ubuntu7) (Free/nSoftware Foundation, Inc.)";
            addToZip(zout, processName + ".bpel", bpel, "bpel/");
            addToZip(zout, "MANIFEST.MF", mani, "META-INF/");
            addToZip(zout, "wsdlCatalog.xml", cat, "META-INF/");
            addToZip(zout, processName + "Defs.wsdl", wsdl, "wsdl/");
            addToZip(zout, processName + ".pdd", pdd, "");
            Iterator<String> it = extWSDLs.iterator();
            int wsdlNumber = 1;
            while (it.hasNext()) {
                addToZip(zout, wsdlNumber + ".wsdl", it.next(), "");
                wsdlNumber++;
            }
            zout.close();
        } catch (FileNotFoundException f) {
            f.printStackTrace();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    /**
	 * @param currentDirectory
	 *            the directory in which the files should be saved
	 */
    private static void addToZip(ZipOutputStream zout, String file, String code, String currentDirectory) {
        try {
            ZipEntry entry = new ZipEntry(currentDirectory + file);
            zout.putNextEntry(entry);
            zout.write(code.getBytes(), 0, code.length());
            zout.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
