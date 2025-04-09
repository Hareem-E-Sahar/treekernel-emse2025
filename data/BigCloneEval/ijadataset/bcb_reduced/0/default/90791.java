import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.lang.NullPointerException;
import java.lang.IllegalArgumentException;

/**
 *
 * @author Shane Santner
 * This class create the DVD filesystem that will be burned
 * to disc.
 *
 * TODO - Need to handle input, output and error streams
 *        more appropriatly.
 */
public class Author {

    /** Creates a new instance of Author */
    public Author() {
    }

    private String m_dvdauthor = "dvdauthor -x vmgm.xml";

    private String m_BaseErr = "Authoring Error - ";

    /**
     * Create a DVD filestructure necessary to play in dvd player using dvdauthor
     * @param   DVD_GUI This is the GUI object used to control the form
     * @return  A boolean to determine if an error occurred in the function  
     */
    public boolean CreateDVDFileStructure(GUI DVD_GUI) {
        try {
            DVD_GUI.prgAuthor.setValue(50);
            DVD_GUI.lblAuthorProg.setText("50%");
            String[] dvdauthor_cmd = { "/bin/sh", "-c", m_dvdauthor };
            Process p = Runtime.getRuntime().exec(dvdauthor_cmd, null, new File(DVD_GUI.strOutputDir));
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader err_in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                BufferedWriter out = new BufferedWriter(new FileWriter(DVD_GUI.strOutputDir + "/log/dvdauthor.log"));
                BufferedWriter err_out = new BufferedWriter(new FileWriter(DVD_GUI.strOutputDir + "/log/dvdauthor.err"));
                String line;
                if (err_in.ready()) {
                    line = err_in.readLine();
                    if (line.equals("/bin/sh: dvdauthor: command not found")) {
                        DVD_GUI.MessageBox("Could not locate dvdauthor in your path." + "\nPlease install all necessary dependencies" + "\nand rerun dvd-homevideo.", 0);
                        in.close();
                        out.close();
                        err_in.close();
                        err_out.close();
                        return true;
                    }
                }
                StringTokenizer st;
                while ((line = err_in.readLine()) != null) {
                    st = new StringTokenizer(line, ":");
                    if (st.hasMoreTokens()) {
                        if (!st.nextToken().equals("WARN")) {
                            DVD_GUI.txtAreaOutput.append(line + "\n");
                            DVD_GUI.txtAreaOutput.setCaretPosition(DVD_GUI.txtAreaOutput.getDocument().getLength());
                            out.write(line);
                            out.newLine();
                        }
                    }
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
            } catch (NoSuchElementException ex) {
                SaveStackTrace.printTrace(DVD_GUI.strOutputDir, ex);
                DVD_GUI.MessageBox(m_BaseErr + "Looked for a token that didn't exist\n" + ex.toString(), 0);
                ex.printStackTrace();
                return true;
            } catch (InterruptedException ex) {
                SaveStackTrace.printTrace(DVD_GUI.strOutputDir, ex);
                DVD_GUI.MessageBox(m_BaseErr + "dvd-homevideo thread was interrupted\n" + ex.toString(), 0);
                ex.printStackTrace();
                return true;
            }
            DVD_GUI.prgAuthor.setIndeterminate(false);
            DVD_GUI.prgAuthor.setValue(100);
            DVD_GUI.lblAuthorProg.setText("100%");
            DVD_GUI.lblAuthor.setEnabled(false);
            DVD_GUI.lblAuthorProg.setEnabled(false);
            DVD_GUI.prgAuthor.setEnabled(false);
            Thread.sleep(2000);
        } catch (IOException ex) {
            SaveStackTrace.printTrace(DVD_GUI.strOutputDir, ex);
            DVD_GUI.MessageBox(m_BaseErr + "IO Error\n" + ex.toString(), 0);
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
        return DVD_GUI.ErrorCheck(DVD_GUI.strOutputDir + "/log/dvdauthor.log");
    }
}
