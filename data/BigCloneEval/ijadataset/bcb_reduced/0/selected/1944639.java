package ceha.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupMysql {

    private int BUFFER = 10485760;

    private final String filename;

    public BackupMysql(String filename) throws FileNotFoundException, IOException, Exception {
        System.out.println(filename);
        this.filename = filename;
        store();
    }

    public void store() throws FileNotFoundException, IOException, Exception {
        byte[] data = getData("localhost", "3306", "root", "", "ceha").getBytes();
        File filedst = new File(filename);
        FileOutputStream dest = new FileOutputStream(filedst);
        ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(dest));
        zip.setMethod(ZipOutputStream.DEFLATED);
        zip.setLevel(Deflater.BEST_COMPRESSION);
        zip.putNextEntry(new ZipEntry("data.sql"));
        zip.write(data);
        zip.close();
        dest.close();
    }

    private String getData(String host, String port, String user, String password, String db) throws Exception {
        Process run = Runtime.getRuntime().exec("C:/wamp/bin/mysql/mysql5.1.36/bin/mysqldump.exe --host=" + host + " --port=" + port + " --user=" + user + " --password=" + password + " --compact --complete-insert --extended-insert " + "--skip-comments --skip-triggers " + db);
        InputStream in = run.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuffer temp = new StringBuffer();
        int count;
        char[] cbuf = new char[BUFFER];
        while ((count = br.read(cbuf, 0, BUFFER)) != -1) {
            temp.append(cbuf, 0, count);
        }
        br.close();
        in.close();
        return temp.toString();
    }
}
