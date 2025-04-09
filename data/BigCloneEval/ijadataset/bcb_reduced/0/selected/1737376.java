package org.rhwlab.snight;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.GregorianCalendar;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.rhwlab.help.AceTreeHelp;
import org.rhwlab.utils.C;
import org.rhwlab.utils.EUtils;
import org.rhwlab.utils.Log;

/**
 * takes the current contents of the NucleiMgr and prepares a zip
 * file with a structure like the nuclei portion of a normal input file
 * <br> this can be called following an edit
 * @author biowolp
 * @version 1.0 Feb 15, 2005
 */
public class NucZipper {

    Log iEditLog;

    /**
     * all the work is done in the constructor
     * @param file File object to store into (only the name is used)
     * @param nucDir String path to the storage location for this file
     * @param nucleiMgr NucleiMgr object which has the nuclei data
     */
    public NucZipper(File file, NucleiMgr nucleiMgr) {
        super();
        String nucDir = nucleiMgr.getConfig().iZipNucDir;
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ZipOutputStream zos = new ZipOutputStream(fos);
            Vector nuclei = (Vector) nucleiMgr.getNucleiRecord();
            for (int i = 0; i < nuclei.size(); i++) {
                String ename = nucDir + "t" + EUtils.makePaddedInt(i + 1) + "-nuclei";
                zos.putNextEntry(new ZipEntry(ename));
                String s;
                for (int j = 0; j < ((Vector) nuclei.elementAt(i)).size(); j++) {
                    Nucleus n = (Nucleus) ((Vector) nuclei.elementAt(i)).elementAt(j);
                    s = formatNucleus(j, n);
                    byte[] b = s.getBytes();
                    zos.write(b, 0, b.length);
                }
                zos.closeEntry();
            }
            addParameters(zos, nucleiMgr);
            zos.close();
        } catch (IOException ioe) {
            System.out.println("NucZipper exception: " + ioe);
            new AceTreeHelp("/org/rhwlab/help/messages/PermissionError.html", 200, 200);
        }
    }

    private void addParameters(ZipOutputStream zos, NucleiMgr nucleiMgr) throws IOException {
        ZipNuclei zn = nucleiMgr.getZipNuclei();
        String ename = nucleiMgr.getParameterEntry();
        zos.putNextEntry(new ZipEntry(ename));
        Vector parameterFileInfo = nucleiMgr.getParameterFileInfo();
        String s = null;
        if (parameterFileInfo != null) {
            for (int i = 0; i < parameterFileInfo.size(); i++) {
                s = (String) parameterFileInfo.elementAt(i);
                s += "\n";
                byte[] b = s.getBytes();
                zos.write(b, 0, b.length);
            }
        }
        zos.closeEntry();
    }

    private void addEditLog(ZipOutputStream zos, NucleiMgr nucleiMgr) throws IOException {
        ZipNuclei zn = nucleiMgr.getZipNuclei();
        String ename = nucleiMgr.PARAMETERS + C.Fileseparator + "EditLog.txt";
        zos.putNextEntry(new ZipEntry(ename));
        zos.write(iEditLog.getText().getBytes());
        zos.closeEntry();
    }

    private String formatNucleus(int j, Nucleus n) {
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf(j + 1) + CS);
        int status = 0;
        if (n.status > 0) status = 1;
        sb.append(String.valueOf(status) + CS);
        sb.append(String.valueOf(n.predecessor) + CS);
        sb.append(String.valueOf(n.successor1) + CS);
        sb.append(String.valueOf(n.successor2) + CS);
        sb.append(String.valueOf(n.x) + CS);
        sb.append(String.valueOf(n.y) + CS);
        sb.append(String.valueOf(n.z) + CS);
        sb.append(String.valueOf(n.size) + CS);
        sb.append(n.identity + CS);
        sb.append(String.valueOf(n.weight) + CS);
        sb.append(String.valueOf(n.rweight) + CS);
        sb.append(String.valueOf(n.rsum) + CS);
        sb.append(String.valueOf(n.rcount) + CS);
        sb.append(n.assignedID + CS);
        sb.append(String.valueOf(n.rwraw) + CS);
        sb.append(String.valueOf(n.rwcorr1) + CS);
        sb.append(String.valueOf(n.rwcorr2) + CS);
        sb.append(String.valueOf(n.rwcorr3) + CS);
        sb.append(String.valueOf(n.rwcorr4) + CS);
        sb.append("\n");
        return sb.toString();
    }

    private static final String CS = ", ";

    /**
     * unused main function
     * @param args String []
     */
    public static void main(String[] args) {
    }
}
