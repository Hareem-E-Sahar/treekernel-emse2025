package com.jsu.judge;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import com.jsu.io.Matcher;
import com.jsu.io.StreamHandler;
import com.jsu.util.JudgeConfig;

/**
 * @author Administrator
 *
 */
public class Runner {

    private static final Logger log = Logger.getLogger(Runner.class);

    public static final int Judging = 0;

    public static final int Accepted = 1;

    public static final int Presentation_Error = 2;

    public static final int Time_Limit_Exceeded = 3;

    public static final int Memory_Limit_Exceeded = 4;

    public static final int Wrong_Answer = 5;

    public static final int Compilation_Error = 6;

    public static final int Runtime_Error = 7;

    public static final int System_Error = 8;

    private static String JUDGE_BASE = JudgeConfig.getValue("JUDGE_BASE");

    private static String RUN = JUDGE_BASE + "temp\\";

    public static long timeWaste;

    public static long memoryWaste;

    private static int status;

    private static Process process;

    private static String getUserOut() {
        try {
            StringBuilder userOut = new StringBuilder();
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = in.readLine()) != null) {
                userOut.append(line.trim());
                userOut.append("\n");
            }
            in.close();
            return userOut.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int run(String runId, long timeLimit, long memoryLimit, File input, File output) {
        timeWaste = 0;
        memoryWaste = 0;
        status = System_Error;
        process = null;
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {

            public void run() {
                process.destroy();
                status = Time_Limit_Exceeded;
            }
        };
        Runtime runtime = Runtime.getRuntime();
        long before = runtime.freeMemory();
        long after = before;
        try {
            process = runtime.exec(RUN + runId + "\\Main.exe");
            after = runtime.freeMemory();
            memoryWaste = before - after;
            if (memoryWaste > memoryLimit) {
                process.destroy();
                return Memory_Limit_Exceeded;
            }
            String stdIn = StreamHandler.read(input);
            StreamHandler.write(process.getOutputStream(), stdIn);
            Date startTime = new Date();
            timer.schedule(task, timeLimit);
            String userOut = getUserOut();
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 1 && exitValue != 0) {
                task.cancel();
                process.destroy();
                return Runtime_Error;
            }
            timeWaste = timeLimit;
            if (status == System_Error) {
                task.cancel();
                Date endTime = new Date();
                timeWaste = endTime.getTime() - startTime.getTime();
                int result = Matcher.matchDirectly(output, userOut);
                status = result;
            }
        } catch (Exception e) {
            log.error("----judging error in:" + runId + "----");
            e.printStackTrace();
            if (process.exitValue() != 0) {
                process.destroy();
            }
            return System_Error;
        }
        process.destroy();
        return status;
    }
}
