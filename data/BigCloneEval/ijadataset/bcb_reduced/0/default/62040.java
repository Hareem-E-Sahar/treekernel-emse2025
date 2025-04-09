import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JFileChooser;

public class birdieCron {

    private db classConnect = null;

    private MsgBox msg = new MsgBox();

    protected static gui fraGUI = null;

    protected static boolean serviceActive = false;

    private JFileChooser openFile = new JFileChooser();

    private static final String usage = "Usage:\n" + "-h\t\t\t\t- show this help\n" + "-v\t\t\t\t- show version\n" + "--start\t\t\t\t- start service\n" + "--stop\t\t\t\t- stop service\n" + "--set-calendar\t\t\t- set Sunbird's calendar to monitor";

    @SuppressWarnings("static-access")
    public void serviceAction(String action) {
        if (action.equals("start")) {
            classConnect = new db(this, getPathtoDb());
            classConnect.query("SELECT title, event_start, event_end, alarm_offset FROM " + classConnect.db_table[0]);
            fraGUI = new gui();
            fraGUI.init();
        } else if (action.equals("stop")) {
            serviceActive = false;
        }
    }

    public File getPathtoDb() {
        String tmpLine = null;
        String usrFolder = null;
        String getOs = null;
        String localPath = null;
        String[] configLine = new String[2];
        String dbfile = null;
        try {
            FileReader input = null;
            BufferedReader bufInput = null;
            getOs = System.getProperty("os.name");
            usrFolder = System.getProperty("user.home");
            for (int i = 0; i < constants.osVer.length; i++) {
                while (getOs.equals(constants.osVer[i])) {
                    localPath = constants.folderPath[i];
                    input = new FileReader(usrFolder + constants.profileCfg[i]);
                    bufInput = new BufferedReader(input);
                    while ((tmpLine = bufInput.readLine()) != null) {
                        if (tmpLine.startsWith("Path=")) {
                            configLine = tmpLine.split("Path=");
                        }
                    }
                    dbfile = usrFolder + localPath + configLine[1] + "/" + constants.db_file;
                    break;
                }
            }
            if (dbfile == null) {
                openFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int result = openFile.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    dbfile = openFile.getSelectedFile().toString();
                } else {
                    dbfile = null;
                    msg.shwMsg("You must choose DB file in order to proceed", "Error:", 0, 1);
                    System.exit(0);
                }
            } else {
                dbfile = usrFolder + localPath + configLine[1] + "/" + constants.db_file;
            }
            System.out.println("Using DB:" + dbfile);
            return (new File(dbfile));
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        birdieCron main = new birdieCron();
        main.serviceAction("start");
    }
}

class refresh extends Thread {

    private birdieCron main;

    public refresh(birdieCron main) {
        this.main = main;
    }

    @SuppressWarnings("static-access")
    public void run() {
        while (main.serviceActive) {
            try {
                this.sleep(1000);
            } catch (InterruptedException ie) {
            }
        }
    }
}
