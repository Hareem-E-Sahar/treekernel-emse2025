package sample.ftpdriver;

import com.sun.faban.driver.*;
import com.sun.faban.driver.util.Random;
import sun.net.ftp.FtpClient;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.util.logging.Logger;

/**
 * FTP Driver. Please note that we're using the sun.net.ftp.FtpClient api.
 * While this api has been relatively stable, it is not a public api and
 * may change without notice.
 */
@BenchmarkDefinition(name = "Sample FTP Workload 101", version = "0.2", configPrecedence = true)
@BenchmarkDriver(name = "FTPDriver", threadPerScale = 1)
@FlatMix(operations = { "GET", "PUT", "Connect" }, mix = { 80, 15, 5 }, deviation = 2)
@NegativeExponential(cycleType = CycleType.CYCLETIME, cycleMean = 2000, cycleDeviation = 2)
public class FTPDriver {

    /** The driver context for this instance */
    private DriverContext ctx;

    Logger logger;

    Random random;

    FtpClient ftpClient;

    int fileCount;

    String host;

    int port = -1;

    int threadId;

    int putSequence = 1;

    String localFileName;

    String uploadPrefix;

    String user;

    String password;

    byte[] buffer = new byte[8192];

    public FTPDriver() throws XPathExpressionException, IOException {
        ctx = DriverContext.getContext();
        logger = ctx.getLogger();
        random = ctx.getRandom();
        threadId = ctx.getThreadId();
        uploadPrefix = "up" + threadId + '_';
        localFileName = "/tmp/ftp" + threadId;
        host = ctx.getXPathValue("/ftpBenchmark/serverConfig/host").trim();
        String port = ctx.getXPathValue("/ftpBenchmark/serverConfig/port").trim();
        fileCount = Integer.parseInt(ctx.getXPathValue("/ftpBenchmark/serverConfig/fileCount").trim());
        user = ctx.getProperty("user");
        password = ctx.getProperty("password");
        ftpClient = new FtpClient();
        if (port == null || port.length() == 0) {
            ftpClient.openServer(host);
        } else {
            this.port = Integer.parseInt(port);
            ftpClient.openServer(host, this.port);
        }
        ftpClient.login(user, password);
        ftpClient.binary();
        ftpClient.cd("pub");
        int fileNo = random.random(1, fileCount);
        String fileName = "File" + fileNo;
        FileOutputStream download = new FileOutputStream(localFileName);
        int count;
        int size = 0;
        InputStream ftpIn = ftpClient.get(fileName);
        while ((count = ftpIn.read(buffer)) != -1) {
            download.write(buffer, 0, count);
            size += count;
        }
        if (size == 0) throw new FatalException("Cannot get file :" + fileName);
        ftpIn.close();
        download.close();
    }

    @BenchmarkOperation(name = "GET", max90th = 2, timing = Timing.MANUAL)
    public void doGet() throws IOException {
        int fileNo = random.random(1, fileCount);
        String fileName = "File" + fileNo;
        logger.finest("Getting " + fileName);
        FileOutputStream download = new FileOutputStream(localFileName);
        int count;
        ctx.recordTime();
        InputStream ftpIn = ftpClient.get(fileName);
        while ((count = ftpIn.read(buffer)) != -1) download.write(buffer, 0, count);
        ftpIn.close();
        ctx.recordTime();
        download.close();
    }

    @NegativeExponential(cycleType = CycleType.CYCLETIME, cycleMean = 4000, cycleDeviation = 2)
    @BenchmarkOperation(name = "PUT", max90th = 2, timing = Timing.MANUAL)
    public void doPut() throws IOException {
        String fileName = uploadPrefix + putSequence++;
        logger.finest("Putting " + fileName);
        FileInputStream upload = new FileInputStream(localFileName);
        int count;
        ctx.recordTime();
        OutputStream ftpOut = ftpClient.put(fileName);
        while ((count = upload.read(buffer)) != -1) ftpOut.write(buffer, 0, count);
        ftpOut.close();
        ctx.recordTime();
        upload.close();
    }

    @BenchmarkOperation(name = "Connect", max90th = 2, timing = Timing.MANUAL)
    public void doConnect() throws IOException {
        ftpClient.closeServer();
        ctx.recordTime();
        if (port == -1) {
            ftpClient.openServer(host);
        } else {
            ftpClient.openServer(host, this.port);
        }
        ftpClient.login(user, password);
        ftpClient.binary();
        ftpClient.cd("pub");
        ctx.recordTime();
    }
}
