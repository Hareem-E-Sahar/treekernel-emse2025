package jsattrak.gui;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import javax.media.MediaLocator;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import jsattrak.utilities.J3DEarthComponent;
import jsattrak.utilities.JpegImagesToMovie;
import name.gano.astro.time.Time;
import name.gano.file.SaveImageFile;

/**
 *
 * @author  sgano
 */
public class JCreateMovieDialog extends javax.swing.JDialog {

    Time startTime, endTime;

    double timeStep = 1.0;

    int playbackFPS = 16;

    private SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS z");

    private SimpleDateFormat dateformatShort1 = new SimpleDateFormat("dd MMM y H:m:s.S z");

    private SimpleDateFormat dateformatShort2 = new SimpleDateFormat("dd MMM y H:m:s z");

    String tempDirStr = "temp_images";

    String rootNameTempImages = "screen_";

    JSatTrak app;

    int movieMode = 0;

    J3DEarthComponent threeDpanel;

    J2DEarthPanel twoDpanel;

    Container otherPanel;

    /** Creates new form JCreateMovieDialog for a 3D window
     * @param parent
     * @param modal
     * @param threeDpanel
     * @param app 
     */
    public JCreateMovieDialog(java.awt.Frame parent, boolean modal, J3DEarthComponent threeDpanel, JSatTrak app) {
        super(parent, modal);
        this.threeDpanel = threeDpanel;
        this.app = app;
        movieMode = 0;
        iniGUI(threeDpanel.getDialogTitle());
    }

    /** Creates new form JCreateMovieDialog for a 2D window
     * @param parent
     * @param modal
     * @param twoDpanel 
     * @param app 
     */
    public JCreateMovieDialog(java.awt.Frame parent, boolean modal, J2DEarthPanel twoDpanel, JSatTrak app) {
        super(parent, modal);
        this.twoDpanel = twoDpanel;
        this.app = app;
        movieMode = 1;
        iniGUI(twoDpanel.getName());
    }

    /** Creates new form JCreateMovieDialog for an generic Container
     * @param parent
     * @param modal
     * @param otherPanel 
     * @param app
     * @param windowTitle Title of the window - to be shown in dialog so user knows which window movie will be made from
     */
    public JCreateMovieDialog(java.awt.Frame parent, boolean modal, Container otherPanel, JSatTrak app, String windowTitle) {
        super(parent, modal);
        this.otherPanel = otherPanel;
        this.app = app;
        movieMode = 2;
        iniGUI(windowTitle);
    }

    private void iniGUI(String windowTitle) {
        initComponents();
        windowNameLabel.setText(windowTitle);
        startTime = new Time();
        startTime.setDateFormat((SimpleDateFormat) app.getCurrentJulianDay().getDateFormat());
        startTime.set(app.getCurrentJulianDay().getCurrentGregorianCalendar().getTimeInMillis());
        endTime = new Time();
        endTime.setDateFormat((SimpleDateFormat) app.getCurrentJulianDay().getDateFormat());
        endTime.set(app.getCurrentJulianDay().getCurrentGregorianCalendar().getTimeInMillis());
        endTime.addSeconds(60.0 * 60.0 * 3.0);
        startTimeField.setText(startTime.getDateTimeStr());
        endTimeField.setText(endTime.getDateTimeStr());
        timeStepField.setText("" + app.getCurrentTimeStep());
        timeStep = app.getCurrentTimeStep();
        playBackRateSpinner.setValue(playbackFPS);
        updateDisplayData();
    }

    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        windowNameLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        startTimeField = new javax.swing.JTextField();
        endTimeField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        timeStepField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        playBackRateSpinner = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        movieLengthLabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        numFramesLabel = new javax.swing.JLabel();
        genMovieButton = new javax.swing.JButton();
        movieStatusBar = new javax.swing.JProgressBar();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/applications-multimedia.png")));
        jLabel1.setText("Movie Capture Tool");
        windowNameLabel.setFont(new java.awt.Font("Tahoma", 2, 11));
        windowNameLabel.setForeground(new java.awt.Color(102, 102, 102));
        windowNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        windowNameLabel.setText("name of window here");
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addComponent(jLabel1).addGap(18, 18, 18).addComponent(windowNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel1).addComponent(windowNameLabel)).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jLabel2.setText("Start Time:");
        jLabel3.setText("Stop Time:");
        startTimeField.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startTimeFieldActionPerformed(evt);
            }
        });
        endTimeField.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                endTimeFieldActionPerformed(evt);
            }
        });
        jLabel5.setText("Time Step [s]:");
        timeStepField.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timeStepFieldActionPerformed(evt);
            }
        });
        jLabel6.setText("Playback Rate [FPS]:");
        playBackRateSpinner.addChangeListener(new javax.swing.event.ChangeListener() {

            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                playBackRateSpinnerStateChanged(evt);
            }
        });
        jLabel7.setText("Length of Movie:");
        movieLengthLabel.setForeground(new java.awt.Color(102, 102, 102));
        movieLengthLabel.setText("hh:mm:ss.sss");
        jLabel9.setText("Number of Frames:");
        numFramesLabel.setForeground(new java.awt.Color(102, 102, 102));
        numFramesLabel.setText("nnnnn");
        genMovieButton.setText("Generate Movie");
        genMovieButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genMovieButtonActionPerformed(evt);
            }
        });
        movieStatusBar.setStringPainted(true);
        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel2Layout.createSequentialGroup().addContainerGap().addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(movieStatusBar, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE).addGroup(jPanel2Layout.createSequentialGroup().addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel2).addComponent(jLabel3)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(startTimeField, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE).addComponent(endTimeField, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE))).addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup().addComponent(jLabel5).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(timeStepField)).addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup().addComponent(jLabel6).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(playBackRateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))).addGroup(jPanel2Layout.createSequentialGroup().addComponent(jLabel7).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(movieLengthLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)).addGroup(jPanel2Layout.createSequentialGroup().addComponent(jLabel9).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(numFramesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(genMovieButton, javax.swing.GroupLayout.Alignment.TRAILING)).addContainerGap()));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel2Layout.createSequentialGroup().addContainerGap().addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2).addComponent(startTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel3).addComponent(endTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(30, 30, 30).addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel5).addComponent(timeStepField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel6).addComponent(playBackRateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel7).addComponent(movieLengthLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel9).addComponent(numFramesLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(genMovieButton).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE).addComponent(movieStatusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)));
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0, 0, 0).addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        pack();
    }

    private void startTimeFieldActionPerformed(java.awt.event.ActionEvent evt) {
        updateTime(startTime, startTimeField);
        updateDisplayData();
    }

    private void genMovieButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            updateTime(startTime, startTimeField);
            updateTime(endTime, endTimeField);
            timeStep = Double.parseDouble(timeStepField.getText());
            if (((Integer) playBackRateSpinner.getValue()).intValue() < 1) {
                playBackRateSpinner.setValue(1);
            }
            playbackFPS = ((Integer) playBackRateSpinner.getValue()).intValue();
            updateDisplayData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "PARAMETER ERROR : " + e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String outputMoviePath = "test.mov";
        final JFileChooser fc = new JFileChooser();
        jsattrak.utilities.CustomFileFilter movFilter = new jsattrak.utilities.CustomFileFilter("mov", "*.mov");
        fc.addChoosableFileFilter(movFilter);
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String fileExtension = "mov";
            if (fc.getFileFilter() == movFilter) {
                fileExtension = "mov";
            }
            String extension = getExtension(file);
            if (extension != null) {
                fileExtension = extension;
            } else {
                file = new File(file.getAbsolutePath() + "." + fileExtension);
            }
            outputMoviePath = "file:" + file.getAbsolutePath();
        } else {
            return;
        }
        boolean success = (new File(tempDirStr)).mkdir();
        double deltaTsec = (endTime.getMJD() - startTime.getMJD()) * 24 * 60 * 60.0;
        final int numFrames = (int) Math.ceil(deltaTsec / timeStep);
        final String outputMoviePathFinal = outputMoviePath;
        SwingWorker<Object, Integer> worker = new SwingWorker<Object, Integer>() {

            public Vector<String> doInBackground() {
                Vector<String> inputFiles = new Vector<String>();
                for (int i = 0; i < numFrames; i++) {
                    app.setTime(startTime.getCurrentGregorianCalendar().getTimeInMillis());
                    if (movieMode == 0) {
                        threeDpanel.getWwd().redrawNow();
                    } else if (movieMode == 1) {
                    } else {
                    }
                    createScreenCapture(tempDirStr + "/" + rootNameTempImages + i + ".jpg");
                    inputFiles.addElement(tempDirStr + "/" + rootNameTempImages + i + ".jpg");
                    publish((int) (100.0 * (i + 1.0) / numFrames));
                    startTime.addSeconds(timeStep);
                }
                movieStatusBar.setString("Building Movie File.....");
                movieStatusBar.setIndeterminate(true);
                int width;
                int height;
                if (movieMode == 0) {
                    width = threeDpanel.getWwdWidth();
                    height = threeDpanel.getWwdHeight();
                } else if (movieMode == 1) {
                    int[] twoDinfo = calculate2DMapSizeAndScreenLoc(twoDpanel);
                    width = twoDinfo[0];
                    height = twoDinfo[1];
                } else {
                    width = otherPanel.getWidth();
                    height = otherPanel.getHeight();
                }
                MediaLocator oml;
                if ((oml = createMediaLocator(outputMoviePathFinal)) == null) {
                    JOptionPane.showMessageDialog(null, "ERROR Creating Output File (check permissions)", "ERROR", JOptionPane.ERROR_MESSAGE);
                    System.err.println("Cannot build media locator from: " + outputMoviePathFinal);
                    publish(0);
                    movieStatusBar.setString("ERROR!");
                    return inputFiles;
                }
                JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
                imageToMovie.doIt(width, height, playbackFPS, inputFiles, oml);
                boolean cleanSuccess = deleteDirectory(tempDirStr);
                publish(0);
                movieStatusBar.setString("Finished!");
                return inputFiles;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int val = chunks.get(chunks.size() - 1);
                movieStatusBar.setValue(val);
                movieStatusBar.repaint();
            }

            @Override
            protected void done() {
                movieStatusBar.setIndeterminate(false);
                movieStatusBar.setValue(0);
                app.forceRepainting();
            }
        };
        worker.execute();
    }

    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public static boolean deleteDirectory(String path2Dir) {
        File path = new File(path2Dir);
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i].toString());
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    private void playBackRateSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {
        if (((Integer) playBackRateSpinner.getValue()).intValue() < 1) {
            playBackRateSpinner.setValue(1);
        }
        playbackFPS = ((Integer) playBackRateSpinner.getValue()).intValue();
        updateDisplayData();
    }

    private void timeStepFieldActionPerformed(java.awt.event.ActionEvent evt) {
        timeStep = Double.parseDouble(timeStepField.getText());
        updateDisplayData();
    }

    private void endTimeFieldActionPerformed(java.awt.event.ActionEvent evt) {
        updateTime(endTime, endTimeField);
        updateDisplayData();
    }

    private javax.swing.JTextField endTimeField;

    private javax.swing.JButton genMovieButton;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel5;

    private javax.swing.JLabel jLabel6;

    private javax.swing.JLabel jLabel7;

    private javax.swing.JLabel jLabel9;

    private javax.swing.JPanel jPanel1;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JLabel movieLengthLabel;

    private javax.swing.JProgressBar movieStatusBar;

    private javax.swing.JLabel numFramesLabel;

    private javax.swing.JSpinner playBackRateSpinner;

    private javax.swing.JTextField startTimeField;

    private javax.swing.JTextField timeStepField;

    private javax.swing.JLabel windowNameLabel;

    public void updateDisplayData() {
        double deltaTsec = (endTime.getMJD() - startTime.getMJD()) * 24 * 60 * 60.0;
        int numFrames = (int) Math.ceil(deltaTsec / timeStep);
        double clipLenSec = (numFrames + 0.0) / playbackFPS;
        movieLengthLabel.setText("" + clipLenSec + " [sec]");
        numFramesLabel.setText("" + numFrames);
    }

    private void updateTime(Time time, JTextField timeTextField) {
        double prevJulDate = time.getJulianDate();
        GregorianCalendar currentTimeDate = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        boolean dateAccepted = true;
        try {
            currentTimeDate.setTime(dateformatShort1.parse(timeTextField.getText()));
            timeTextField.setText(dateformat.format(currentTimeDate.getTime()));
        } catch (Exception e2) {
            try {
                currentTimeDate.setTime(dateformatShort2.parse(timeTextField.getText()));
                timeTextField.setText(dateformat.format(currentTimeDate.getTime()));
            } catch (Exception e3) {
                timeTextField.setText(dateformat.format(currentTimeDate.getTime()));
                dateAccepted = false;
            }
        }
        if (dateAccepted) {
            time.set(currentTimeDate.getTimeInMillis());
        }
    }

    public void createScreenCapture(String outputFileName) {
        try {
            Point pt = new Point();
            int width = 0;
            int height = 0;
            if (movieMode == 0) {
                pt = threeDpanel.getWwdLocationOnScreen();
                width = threeDpanel.getWwdWidth();
                height = threeDpanel.getWwdHeight();
            } else if (movieMode == 1) {
                int[] twoDinfo = calculate2DMapSizeAndScreenLoc(twoDpanel);
                pt.setLocation(twoDinfo[2], twoDinfo[3]);
                width = twoDinfo[0];
                height = twoDinfo[1];
            } else {
                pt = otherPanel.getLocationOnScreen();
                width = otherPanel.getWidth();
                height = otherPanel.getHeight();
            }
            if (height <= 0 || width <= 0) {
                JOptionPane.showInternalMessageDialog(this, "A Screenshot was not possible - too small of size", "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BufferedImage screencapture = new Robot().createScreenCapture(new Rectangle(pt.x, pt.y, width, height));
            String fileExtension = "jpg";
            File file = new File(outputFileName);
            Exception e = SaveImageFile.saveImage(fileExtension, file, screencapture, 0.9f);
            if (e != null) {
                System.out.println("ERROR SCREEN CAPTURE:" + e.toString());
                return;
            }
        } catch (Exception e4) {
            System.out.println("ERROR SCREEN CAPTURE:" + e4.toString());
        }
    }

    /**
     * Create a media locator from the given string.
     */
    static MediaLocator createMediaLocator(String url) {
        MediaLocator ml;
        if (url.indexOf(":") > 0 && (ml = new MediaLocator(url)) != null) return ml;
        if (url.startsWith(File.separator)) {
            if ((ml = new MediaLocator("file:" + url)) != null) return ml;
        } else {
            String file = "file:" + System.getProperty("user.dir") + File.separator + url;
            if ((ml = new MediaLocator(file)) != null) return ml;
        }
        return null;
    }

    private int[] calculate2DMapSizeAndScreenLoc(J2DEarthPanel twoDmapPanel) {
        Point pt = twoDmapPanel.getLocationOnScreen();
        int width = twoDmapPanel.getWidth();
        int height = twoDmapPanel.getHeight();
        double aspectRatio = 2.0;
        int newWidth = 1, newHeight = 1;
        if (height != 0) {
            if (width / height > aspectRatio) {
                newHeight = height;
                newWidth = (int) (height * aspectRatio);
            } else {
                newWidth = width;
                newHeight = (int) (width * 1.0 / aspectRatio);
            }
            int deltaW = width - newWidth;
            int deltaH = height - newHeight;
            pt.y = pt.y + (int) (deltaH / 2.0);
            pt.x = pt.x + (int) (deltaW / 2.0);
            width = newWidth;
            height = newHeight;
        }
        return new int[] { width, height, pt.x, pt.y };
    }
}
