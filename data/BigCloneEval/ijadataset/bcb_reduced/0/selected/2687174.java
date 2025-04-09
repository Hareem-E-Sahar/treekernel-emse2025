package votebox.middle.datacollection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;
import auditorium.IAuditoriumParams;
import auditorium.IKeyStore;
import sexpression.ASExpression;
import sexpression.ListExpression;
import votebox.middle.datacollection.evil.EvilObserver;
import votebox.middle.driver.Driver;
import votebox.middle.view.AWTViewFactory;

/**
 * This is a launcher for the vote system. Use this launcher in order to do
 * human factors type testing.
 * @author Kyle
 */
public class Launcher {

    private static final File SettingsFile = new File("settings");

    /**
     * This is the gui for this launcher.
     */
    private LauncherView _view = null;

    /**
     * This is the votebox that is currently running.
     */
    private Driver _voteBox = null;

    /**
     * Launch the votebox software after doing some brief sanity checking. These
     * checks won't catch everything but they will catch enough problems caused
     * by simple accidents.
     * @param ballotLocation This is the location of the ballot. (zip)
     * @param logDir This is the directory that log files shuld be written out
     *            to. (dir)
     * @param logFilename This is the desired filename for the log file.
     * @param debug Passed to AWTViewFactory to determine windowed/fullscreen mode.
     */
    public void launch(final String ballotLocation, String logDir, String logFilename, boolean debug, final String vvpat, final int vvpatWidth, final int vvpatHeight, final int printableWidth, final int printableHeight, final EvilObserver evilObserver) {
        File baldir;
        try {
            baldir = File.createTempFile("ballot", "");
            baldir.delete();
            baldir.mkdirs();
            Driver.unzip(ballotLocation, baldir.getAbsolutePath());
            Driver.deleteRecursivelyOnExit(baldir.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println(baldir.getAbsolutePath());
        File logdir = new File(logDir);
        File logfile = new File(logdir, logFilename);
        if (!baldir.isDirectory()) {
            _view.statusMessage("Supplied 'ballot location' is not a directory.", "Please make sure that you select a directory which contains a ballot configuration file and media directory. Do not select a file.");
            return;
        }
        if (!Arrays.asList(baldir.list()).contains("ballotbox.cfg")) {
            _view.statusMessage("Supplied 'ballot location' does not contain the file 'ballotbox.cfg'", "Please specify a valid ballot.zip or ballot directory.");
            return;
        }
        if (!logdir.isDirectory()) {
            _view.statusMessage("Supplied 'log directory' is not a directory.", "Please make sure that you select a directory\nfor 'log directory' field. Do not select a file.");
            return;
        }
        if (logFilename.equals("")) {
            _view.statusMessage("Log Filename blank.", "Please specify a log filename.");
            return;
        }
        if (logfile.exists()) {
            int i = 2;
            String startname = logfile.getName();
            while (logfile.exists()) logfile = new File(startname + "-" + i++);
            if (!_view.askQuestion("Supplied 'log file' exists", "If you choose to continue, event data will be recorded to the file: " + logfile.getName())) return;
        }
        DataLogger.init(logfile);
        save(ballotLocation, logDir, logFilename);
        _voteBox = null;
        System.gc();
        _voteBox = new Driver(baldir.getAbsolutePath(), new AWTViewFactory(debug, false), false);
        final Driver vbcopy = _voteBox;
        _view.setRunning(true);
        new Thread(new Runnable() {

            public void run() {
                final IAuditoriumParams constants = new IAuditoriumParams() {

                    public boolean getAllowUIScaling() {
                        return true;
                    }

                    public boolean getUseWindowedView() {
                        return true;
                    }

                    public String getBroadcastAddress() {
                        return null;
                    }

                    public boolean getCastBallotEncryptionEnabled() {
                        return false;
                    }

                    public String getChallengeBallotFile() {
                        return null;
                    }

                    public int getChallengePort() {
                        return 0;
                    }

                    public int getDefaultSerialNumber() {
                        return 0;
                    }

                    public int getDiscoverPort() {
                        return 0;
                    }

                    public int getDiscoverReplyPort() {
                        return 0;
                    }

                    public int getDiscoverReplyTimeout() {
                        return 0;
                    }

                    public int getDiscoverTimeout() {
                        return 0;
                    }

                    public String getEloTouchScreenDevice() {
                        return null;
                    }

                    public int getHttpPort() {
                        return 0;
                    }

                    public int getJoinTimeout() {
                        return 0;
                    }

                    public IKeyStore getKeyStore() {
                        return null;
                    }

                    public int getListenPort() {
                        return 0;
                    }

                    public String getLogLocation() {
                        return null;
                    }

                    public int getPaperHeightForVVPAT() {
                        return vvpatHeight;
                    }

                    public int getPaperWidthForVVPAT() {
                        return vvpatWidth;
                    }

                    public int getPrintableHeightForVVPAT() {
                        return printableHeight;
                    }

                    public int getPrintableWidthForVVPAT() {
                        return printableWidth;
                    }

                    public String getPrinterForVVPAT() {
                        return vvpat;
                    }

                    public String getReportAddress() {
                        return null;
                    }

                    public String getRuleFile() {
                        return null;
                    }

                    public boolean getUseCommitChallengeModel() {
                        return false;
                    }

                    public boolean getUseEloTouchScreen() {
                        return false;
                    }

                    public int getViewRestartTimeout() {
                        return 1;
                    }

                    public boolean getEnableNIZKs() {
                        return false;
                    }

                    public boolean getUsePiecemealEncryption() {
                        return false;
                    }

                    public boolean getUseSimpleTallyView() {
                        return false;
                    }

                    public boolean getUseTableTallyView() {
                        return false;
                    }
                };
                vbcopy.registerForReview(evilObserver);
                vbcopy.run(new Observer() {

                    ListExpression _lastSeenBallot = null;

                    public void update(Observable o, Object arg) {
                        Object[] obj = (Object[]) arg;
                        if (!((Boolean) obj[0])) return;
                        ListExpression ballot = (ListExpression) obj[1];
                        boolean reject = !ballot.toString().equals("" + _lastSeenBallot);
                        try {
                            if (reject) {
                                if (_lastSeenBallot != null) Driver.printBallotRejected(constants, new File(ballotLocation));
                                Driver.printCommittedBallot(constants, ballot, new File(ballotLocation));
                            }
                        } catch (Exception e) {
                        } finally {
                            _lastSeenBallot = ballot;
                        }
                    }
                }, new Observer() {

                    public void update(Observable o, Object arg) {
                        ASExpression ballot = (ASExpression) ((Object[]) arg)[0];
                        System.out.println("Preparing to dump ballot:\n\t" + ballot);
                        DataLogger.DumpBallot(ballot);
                        Driver.printBallotAccepted(constants, new File(ballotLocation));
                        vbcopy.getView().nextPage();
                    }
                });
                _view.setRunning(true);
            }
        }).start();
    }

    public void kill() {
        _voteBox.kill();
        _view.setRunning(false);
    }

    /**
     * Call this method to run the launcher.
     */
    public void run() {
        _view = new LauncherView(this);
        load();
        _view.setRunning(false);
        _view.setVisible(true);
    }

    /**
     * Load the state of the fields from disk.
     */
    private void load() {
        String ballot, logdir, logfile;
        if (SettingsFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(SettingsFile));
                logdir = reader.readLine();
                ballot = reader.readLine();
                logfile = reader.readLine();
            } catch (Exception e) {
                return;
            }
            _view.setFields(logdir, ballot, logfile);
        }
    }

    /**
     * Save the state of the fields to disk.
     */
    private void save(String ballot, String logdir, String logfile) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(SettingsFile));
            writer.write(logdir + "\n");
            writer.write(ballot + "\n");
            writer.write(logfile + "\n");
            writer.close();
        } catch (Exception e) {
            return;
        }
    }

    public static void main(String[] args) {
        new Launcher().run();
    }
}
