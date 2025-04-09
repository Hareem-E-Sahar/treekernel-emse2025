import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.TestCase;
import com.medsol.common.action.TaskExecuteAction;

/**
 * Created by IntelliJ IDEA.
 * User: vinay
 * Date: 1 Sep, 2008
 * Time: 11:23:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class JustTest extends TestCase {

    private void testPrivate() {
    }

    private void testPrivate2() {
    }

    public void testStringBufferLastIndexOf() {
        StringBuffer s = new StringBuffer(" test and why");
        System.out.println(s.lastIndexOf("and"));
        System.out.println(s.lastIndexOf("why"));
        System.out.println(s.lastIndexOf("a"));
        System.out.println(s.lastIndexOf("goodness"));
        System.out.println(s.substring(0, s.lastIndexOf("and")));
    }

    public void testSystemProperies() {
        System.out.println(System.getProperty("java.io.tmpdir"));
    }

    public static void main(String[] args) {
        JustTest justTest = new JustTest();
        justTest.testSystemProperies();
    }

    public void testFiles() {
        File file = new File("D:/MedSol/Database/Backup/DBCopy/aanda/fulldata");
        System.out.println(file.list());
        if (file.list() == null) {
            file.mkdirs();
        }
        String[] fileList = file.list();
        System.out.println(fileList.length);
        for (int i = 0; i < fileList.length; i++) {
            System.out.println(fileList[i]);
        }
    }

    public void testMySqlCall() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec("ping localhost");
            DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
            for (String outptFromPrc = inputStream.readLine(); (outptFromPrc = inputStream.readLine()) != null; ) {
                System.out.println(outptFromPrc);
            }
            BufferedReader errorInputStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            for (String outptFromPrc = errorInputStream.readLine(); (outptFromPrc = errorInputStream.readLine()) != null; ) {
                System.out.println(outptFromPrc);
            }
            System.out.println(process.exitValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testBatchFileCreate() {
    }

    public void testFileArchive() {
        try {
            File file = new File("D:/MedSol/Database/Backup/DBCopy/");
            File dest = new File("D:/MedSol/Database/Backup/Archive/");
            dest.mkdirs();
            System.out.println(file.renameTo(new File(dest, file.getName())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
        System.out.println(dateFormat.format(null) + "-" + dateFormat.format(new Date()));
    }
}
