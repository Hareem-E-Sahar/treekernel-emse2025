import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Shane Santner
 * This class burns the DVD file structure previously created by
 * dvd-author to a DVD disc.
 */
public class Burn {

    /** Creates a new instance of Burn */
    public Burn() {
    }

    /** 
     * Creates a new instance of Burn 
     * @param   burnToDVD   boolean value used to determine whether or not to burn to a DVD
     *                      when dvd-homevideo completes
     */
    public Burn(boolean burnToDVD) {
        m_Burn = burnToDVD;
    }

    /** Burn Member Variables */
    private boolean m_Burn;

    private String m_growisofs = "growisofs -speed=4 -Z /dev/dvd -dvd-video temp/DVD/";

    private String m_BaseErr = "Burn Error - ";

    /**
     * Optionally burn to DVD when complete and no errors, uses growisofs
     * @param   DVD_GUI    This is the GUI object used to control the form
     * return   A boolean to determine if an error occurred in the function
     */
    public boolean BurnToDVD(GUI DVD_GUI) {
        m_growisofs = m_growisofs.replaceAll("temp", DVD_GUI.strOutputDir);
        if (m_Burn == true) {
            try {
                Process p = Runtime.getRuntime().exec(m_growisofs);
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader err_in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                BufferedWriter out = new BufferedWriter(new FileWriter(DVD_GUI.strOutputDir + "/log/burn.log"));
                BufferedWriter err_out = new BufferedWriter(new FileWriter(DVD_GUI.strOutputDir + "/log/burn.err"));
                String line;
                Thread.sleep(50);
                if (err_in.ready()) {
                    line = err_in.readLine();
                    if (line.equals("/bin/sh: growisofs: command not found")) {
                        DVD_GUI.MessageBox("Could not locate growisofs in your path." + "\nPlease install all necessary dependencies" + "\nand rerun dvd-homevideo.", 0);
                        in.close();
                        out.close();
                        err_in.close();
                        err_out.close();
                        return true;
                    } else {
                        DVD_GUI.txtAreaOutput.append(line + "\n");
                        out.write(line);
                        out.newLine();
                    }
                }
                out.write("burn.log--->dvd-homevideo");
                out.newLine();
                while ((line = in.readLine()) != null) {
                    DVD_GUI.txtAreaOutput.append(line + "\n");
                    out.write(line);
                    out.newLine();
                }
                in.close();
                out.close();
                err_in.close();
                err_out.close();
                p.waitFor();
            } catch (IOException ex) {
                SaveStackTrace.printTrace(DVD_GUI.strOutputDir, ex);
                DVD_GUI.MessageBox(m_BaseErr + "IO Error\n" + ex.toString(), 0);
                ex.printStackTrace();
                return true;
            } catch (InterruptedException ex) {
                SaveStackTrace.printTrace(DVD_GUI.strOutputDir, ex);
                DVD_GUI.MessageBox(m_BaseErr + "dvd-homevideo thread was interrupted\n" + ex.toString(), 0);
                ex.printStackTrace();
                return true;
            } catch (NullPointerException ex) {
                SaveStackTrace.printTrace(DVD_GUI.strOutputDir, ex);
                DVD_GUI.MessageBox(m_BaseErr + "Error executing Runtime.getRuntime().exec()\n" + ex.toString(), 0);
                ex.printStackTrace();
                return true;
            } catch (IllegalArgumentException ex) {
                SaveStackTrace.printTrace(DVD_GUI.strOutputDir, ex);
                DVD_GUI.MessageBox(m_BaseErr + "Illegal argument sent to Runtime.getRuntime().exec()\n" + ex.toString(), 0);
                ex.printStackTrace();
                return true;
            } catch (Exception ex) {
                SaveStackTrace.printTrace(DVD_GUI.strOutputDir, ex);
                DVD_GUI.MessageBox(m_BaseErr + "Unknown Error occurred\n" + ex.toString(), 0);
                ex.printStackTrace();
                return true;
            }
        }
        if (m_Burn) return DVD_GUI.ErrorCheck(DVD_GUI.strOutputDir + "/log/burn.log");
        return false;
    }
}
