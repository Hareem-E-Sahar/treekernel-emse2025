import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Logger;

/**
 * @author Maha
 *
 */
public class App implements Runnable {

    protected static final String FILE_NAME = "F:\\AUC\\Databases\\Arabic Digits Databases\\MAHDBase\\MAHDBase_TrainingSet\\Part01\\" + "writer041_pass02_digit6.bmp";

    protected static final int CATEGORY_SIZE = 10;

    protected static final int NO_OF_PASSES = 10;

    protected static final int NO_OF_TRAIN_WRITERS = 500;

    protected static final int NO_OF_VALIDATION_WRITERS = 600;

    protected static final int NO_OF_TEST_WRITERS = 700;

    protected static final int NO_OF_PARTS = 10;

    protected static final int NO_OF_DIGITS = 10;

    private static final String TrainingProg = null;

    private static final String TrainingProgramName = "Neural.exe";

    private static final String TestProgramName = "Neuraltest.exe";

    public static String ProgramFile = "";

    private static DatabaseFeatureManager data = new DatabaseFeatureManager();

    private static final transient Logger logger = Logger.getLogger(App.class);

    protected static final int TRAIN = 0;

    protected static final int TEST = 1;

    protected static final int TRAIN_THEN_TEST = 2;

    public static int State = TRAIN;

    protected static int Task = 1;

    protected static boolean UseZero = true;

    /**
	 * 
	 */
    public App() {
    }

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        org.apache.log4j.PropertyConfigurator.configure("log4j.properties");
        ImageFrame frame = new ImageFrame();
        frame.setVisible(true);
    }

    @Override
    public void run() {
        if (Task == 1) RunAllTask();
        if (Task == 2) {
            RunTrainTask();
        }
        if (Task == 3) {
            if (State == TRAIN_THEN_TEST) {
                State = TRAIN;
                TrainAllFilesTask();
                State = TEST;
                TrainAllFilesTask();
            } else {
                TrainAllFilesTask();
            }
        }
    }

    public static void RunAll(String dirName, int type) {
        Task = 1;
        data.setDataBaseType(type);
        data.setDataBaseDir(dirName);
        App runApp = new App();
        Thread th = new Thread(runApp);
        th.run();
    }

    private void RunAllTask() {
        data.generateStateForAllDigits();
        Date d = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_hh-mm");
        formatter.format(d);
        String dat = formatter.format(d);
        data.storeAllDataToFile("result_" + dat + ".xls");
    }

    public static BufferedImage RunFile(String filename) {
        DigitImage Digit = new DigitImage();
        Digit.ReadImage(filename);
        Digit.computeAllFeatures();
        return Digit.getImage();
    }

    public static void RunTrain(String dirName, int type) {
        data.setDataBaseType(type);
        data.setDataBaseDir(dirName);
        Task = 2;
        App runApp = new App();
        Thread th = new Thread(runApp);
        th.run();
    }

    private void RunTrainTask() {
        if (UseZero) data.setStartD(0); else data.setStartD(1);
        if (State == TRAIN || State == TRAIN_THEN_TEST) {
            data.db.Status = data.db.TRAIN;
            data.compteFeaturesToTrainFileAllDigit();
        }
        if (State == TEST || State == TRAIN_THEN_TEST) {
            data.db.Status = data.db.TEST;
            data.compteFeaturesToTrainFileAllDigit();
        }
        System.out.println("--------------------------------");
    }

    public static BufferedImage RunGetFile(String filename, String dirName, int type, String fullFileName) {
        String tempFile;
        tempFile = filename;
        if (filename != "") {
            DataBaseConnector db = new DataBaseConnector();
            db.setDataBaseType(type);
            db.setDataBaseDir(dirName);
            if (State == TRAIN) db.Status = db.TRAIN;
            if (State == TEST) db.Status = db.TEST;
            filename = db.getFullPath(filename);
        } else filename = App.FILE_NAME;
        DigitImage Digit = new DigitImage();
        if (type == DataBaseConnector.MNIST) {
            if (filename.equals("")) Digit.ReadImage(fullFileName, Integer.parseInt(tempFile)); else Digit.ReadImage(filename, Integer.parseInt(tempFile));
        } else {
            Digit.ReadImage(filename);
        }
        Digit.computeAllFeatures();
        return Digit.getImage();
    }

    public static void StartTrainingClassifers() {
        Task = 3;
        Thread th;
        App runApp = new App();
        th = new Thread(runApp);
        th.run();
    }

    private void TrainAllFilesTask() {
        Runtime r = Runtime.getRuntime();
        Process p = null;
        try {
            String s;
            if (ProgramFile.equals("")) {
                if (State == TRAIN) s = TrainingProgramName; else {
                    s = TestProgramName;
                }
            } else s = ProgramFile;
            p = r.exec(s);
            InputStream in = p.getInputStream();
            OutputStream out = p.getOutputStream();
            InputStream err = p.getErrorStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = input.readLine()) != null) {
                logger.info(line);
            }
            input.close();
            out.write(4);
        } catch (Exception e) {
            System.out.println("error===" + e.getMessage());
            e.printStackTrace();
        }
    }
}
