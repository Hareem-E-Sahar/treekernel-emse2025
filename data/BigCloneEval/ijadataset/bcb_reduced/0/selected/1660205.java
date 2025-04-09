package leeon.subtitle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MKVToPMP {

    private static Log logger = LogFactory.getLog(MKVToPMP.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args == null || args.length == 0 || args[0] == null || args[0].length() == 0) {
            logger.info("必须输入第一个参数");
            return;
        }
        String fileDir = args[0];
        File dir = new File(fileDir);
        if (!dir.isDirectory()) {
            logger.info("输入的参数不是合法目录");
            return;
        }
        File[] mkvFiles = dir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".mkv");
            }
        });
        File pmpDir = new File(fileDir + (fileDir.endsWith("\\") ? "pmp" : "\\pmp"));
        File srtDir = new File(fileDir + (fileDir.endsWith("\\") ? "srt" : "\\srt"));
        pmpDir.mkdir();
        srtDir.mkdir();
        StringBuffer cmd = new StringBuffer();
        for (File mkvFile : mkvFiles) {
            String name = mkvFile.getName();
            cmd.append("mkvextract tracks \"").append(dir.getAbsolutePath() + "\\" + name).append("\" 3:\"").append(srtDir.getAbsolutePath() + "\\" + name.substring(0, name.length() - 4)).append(".ass\"").append("\r\n");
        }
        logger.info(execute(createBatFile(srtDir, "mkv2ass.bat", cmd.toString())));
        File[] assFiles = srtDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.endsWith(".ass");
            }
        });
        cmd.setLength(0);
        for (File assFile : assFiles) {
            if (isASS(assFile)) {
                cmd.append("ass2srt \"").append(assFile.getAbsolutePath()).append("\"\r\n");
            } else {
                assFile.renameTo(new File(assFile.getAbsolutePath().substring(0, assFile.getAbsolutePath().length() - 4) + ".srt"));
            }
        }
        logger.info(execute(createBatFile(srtDir, "ass2srtbat.bat", cmd.toString())));
        cmd.setLength(0);
        for (File mkvFile : mkvFiles) {
            String name = mkvFile.getName();
            cmd.append("start /b /wait /low mencoder.exe  ").append("-ofps 23.976 -vf harddup,scale=480:272 -ovc x264 -ffourcc H264 -x264encopts crf=24:threads=2:pictiming:nopsnr:nossim -srate 44100  -af volnorm -oac faac -faacopts br=112:mpeg=4:object=2  \"").append(mkvFile.getAbsolutePath()).append("\" -o video.avi 2>2.txt");
            cmd.append("\r\n\r\n");
            cmd.append("start /b /wait /low mencoder.exe -oac copy -ovc copy -of rawaudio video.avi -o audio.aac\r\n");
            cmd.append("pmp_muxer_avc -v video.avi -a audio.aac -s 1000 -r 23976 -d 1 -o \"").append(pmpDir.getAbsolutePath() + "\\" + name.substring(0, name.length() - 4)).append(".pmp\"\r\n");
            cmd.append("del video.avi\r\n");
            cmd.append("del audio.*\r\n");
            cmd.append("del *.log\r\n");
            cmd.append("End\r\n\r\n\r\n");
        }
        cmd.append("del *.txt\r\n");
        createBatFile(pmpDir, "mkv2pmp.bat", cmd.toString());
        cmd.setLength(0);
        for (File mkvFile : mkvFiles) {
            String name = mkvFile.getName();
            cmd.append("mkvextract tracks \"").append(dir.getAbsolutePath() + "\\" + name).append("\" 1:\"").append(pmpDir.getAbsolutePath() + "\\" + name.substring(0, name.length() - 4)).append(".avi\"").append("\r\n");
            cmd.append("mkvextract tracks \"").append(dir.getAbsolutePath() + "\\" + name).append("\" 2:\"").append(pmpDir.getAbsolutePath() + "\\" + name.substring(0, name.length() - 4)).append(".ac3\"").append("\r\n");
        }
        createBatFile(pmpDir, "mkv2avi.bat", cmd.toString());
    }

    /**
	 * 判断是否ass
	 * @param file
	 * @return
	 * @throws IOException
	 */
    private static boolean isASS(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        byte[] b = new byte[2000];
        in.read(b, 0, 2000);
        in.close();
        String c = new String(b);
        return c.indexOf("[Events]") != -1;
    }

    /**
	 * 写bat执行文件的方法
	 * @param dir
	 * @param name
	 * @param cmd
	 * @throws IOException
	 */
    private static String createBatFile(File dir, String name, String cmd) throws IOException {
        String ret = dir.getAbsolutePath() + "\\" + name;
        FileOutputStream out = new FileOutputStream(ret);
        out.write(cmd.getBytes());
        out.flush();
        out.close();
        return ret;
    }

    /**
	 * 执行命令行的方法
	 * @param cmdLine
	 * @return
	 * @throws InterruptedException 
	 * @throws Exception
	 */
    private static String execute(String cmdLine) throws IOException, InterruptedException {
        StringBuffer out = new StringBuffer();
        String line = null;
        Process p = Runtime.getRuntime().exec(cmdLine);
        BufferedReader commandResult = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((line = commandResult.readLine()) != null) {
            out.append(line).append("\n\r");
        }
        commandResult.close();
        p.waitFor();
        p.destroy();
        return out.toString();
    }
}
