package com.tivo.kmttg.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Hashtable;
import java.util.Stack;
import com.tivo.kmttg.main.config;
import com.tivo.kmttg.main.jobData;
import com.tivo.kmttg.main.jobMonitor;
import com.tivo.kmttg.util.backgroundProcess;
import com.tivo.kmttg.util.debug;
import com.tivo.kmttg.util.file;
import com.tivo.kmttg.util.log;
import com.tivo.kmttg.util.string;

public class push implements Serializable {

    private static final long serialVersionUID = 1L;

    private Thread thread = null;

    private Boolean thread_running = false;

    private String host = "localhost";

    private String port = "9032";

    private int timeout_http = 60;

    private int timeout_ffmpeg = 10;

    private Stack<Hashtable<String, String>> shares = null;

    private String videoFile = null;

    private String share = null;

    private String path = null;

    private String push_file = null;

    private Boolean success = false;

    private backgroundProcess process;

    public jobData job;

    public push(jobData job) {
        debug.print("job=" + job);
        this.job = job;
        if (config.pyTivo_config != null) {
            shares = parsePyTivoConf(config.pyTivo_config);
        }
        host = config.pyTivo_host;
        videoFile = lowerCaseVolume(job.videoFile);
    }

    public backgroundProcess getProcess() {
        return process;
    }

    public Boolean launchJob() {
        debug.print("");
        Boolean schedule = true;
        if (shares == null) {
            log.error("No pyTivo video shares found in pyTivo config file: " + config.pyTivo_config);
            return false;
        }
        if (shares.size() == 0) {
            log.error("No pyTivo video shares found in pyTivo config file: " + config.pyTivo_config);
            return false;
        }
        if (!inPyTivoShare(videoFile)) {
            log.error("This file is not located in a pyTivo share directory");
            log.error("Available pyTivo shares:");
            log.error(getShareInfo());
            schedule = false;
        }
        if (job.pyTivo_tivo == null) {
            log.error("tivoName to push to not defined");
            schedule = false;
        }
        if (!isVideo(videoFile)) {
            log.error("This is not a valid video file to be pushed");
            schedule = false;
        }
        if (schedule) {
            if (start()) {
                job.process_push = this;
                jobMonitor.updateJobStatus(job, "running");
                job.time = new Date().getTime();
            }
            return true;
        } else {
            return false;
        }
    }

    private Boolean start() {
        debug.print("");
        class AutoThread implements Runnable {

            AutoThread() {
            }

            public void run() {
                success = push_file(job.pyTivo_tivo, share, path, push_file);
            }
        }
        thread_running = true;
        AutoThread t = new AutoThread();
        thread = new Thread(t);
        thread.start();
        return true;
    }

    public void kill() {
        debug.print("");
        thread.interrupt();
        log.warn("Killing '" + job.type + "' file: " + videoFile);
    }

    public Boolean check() {
        if (thread_running) {
            if (config.GUIMODE) {
                config.gui.jobTab_UpdateJobMonitorRowStatus(job, "running");
            }
            return true;
        } else {
            jobMonitor.removeFromJobList(job);
            if (success) {
                log.warn("push job completed: " + jobMonitor.getElapsedTime(job.time));
                log.print("---DONE--- job=" + job.type + " video=" + videoFile);
            }
        }
        return false;
    }

    private String getShareInfo() {
        String s = "";
        if (shares != null && shares.size() > 0) {
            for (int i = 0; i < shares.size(); ++i) {
                s += "share=" + shares.get(i).get("share");
                s += " path=" + shares.get(i).get("path");
                s += "\n";
            }
        }
        return s;
    }

    private Boolean inPyTivoShare(String videoFile) {
        if (shares == null) {
            return false;
        }
        if (shares.size() == 0) {
            return false;
        }
        for (int i = 0; i < shares.size(); ++i) {
            if (videoFile.startsWith(shares.get(i).get("path"))) {
                String shareDir = shares.get(i).get("path");
                share = shares.get(i).get("share");
                push_file = videoFile;
                path = string.dirname(videoFile.substring(shareDir.length() + 1, videoFile.length()));
                if (config.OS.equals("windows")) {
                    path = path.replaceAll("\\\\", "/");
                }
                if (path.endsWith("/")) {
                    path = path.substring(0, path.length() - 1);
                }
                return true;
            }
        }
        return false;
    }

    private String lowerCaseVolume(String fileName) {
        String lowercased = fileName;
        if (config.OS.equals("windows")) {
            if (fileName.matches("^(.+):.*$")) {
                String[] l = fileName.split(":");
                if (l.length > 0) {
                    lowercased = l[0].toLowerCase() + ":";
                    for (int i = 1; i < l.length; i++) {
                        lowercased += l[i];
                    }
                }
            }
        }
        return lowercased;
    }

    private Stack<Hashtable<String, String>> parsePyTivoConf(String config) {
        Stack<Hashtable<String, String>> s = new Stack<Hashtable<String, String>>();
        String username = null;
        String password = null;
        try {
            BufferedReader ifp = new BufferedReader(new FileReader(config));
            String line = null;
            String key = null;
            Hashtable<String, String> h = new Hashtable<String, String>();
            while ((line = ifp.readLine()) != null) {
                line = line.replaceFirst("^\\s*(.*$)", "$1");
                line = line.replaceFirst("^(.*)\\s*$", "$1");
                if (line.length() == 0) continue;
                if (line.matches("^#.+")) continue;
                if (line.matches("^\\[.+\\]")) {
                    key = line.replaceFirst("\\[", "");
                    key = key.replaceFirst("\\]", "");
                    if (!h.isEmpty()) {
                        if (h.containsKey("share") && h.containsKey("path")) {
                            s.add(h);
                        }
                        h = new Hashtable<String, String>();
                    }
                    continue;
                }
                if (key == null) continue;
                if (key.equalsIgnoreCase("server")) {
                    if (line.matches("(?i)^port\\s*=.+")) {
                        String[] l = line.split("=");
                        if (l.length > 1) {
                            port = string.removeLeadingTrailingSpaces(l[1]);
                        }
                    }
                    if (line.matches("(?i)^tivo_username\\s*=.+")) {
                        String[] l = line.split("=");
                        if (l.length > 1) {
                            username = string.removeLeadingTrailingSpaces(l[1]);
                        }
                    }
                    if (line.matches("(?i)^tivo_password\\s*=.+")) {
                        String[] l = line.split("=");
                        if (l.length > 1) {
                            password = string.removeLeadingTrailingSpaces(l[1]);
                        }
                    }
                    continue;
                }
                if (line.matches("(?i)^type\\s*=.+")) {
                    if (line.matches("(?i)^.+=\\s*video.*")) {
                        if (!h.containsKey("share")) {
                            h.put("share", key);
                        }
                    }
                    continue;
                }
                if (line.matches("(?i)^path\\s*=.+")) {
                    String[] l = line.split("=");
                    if (l.length > 1) {
                        String p = lowerCaseVolume(string.removeLeadingTrailingSpaces(l[1]));
                        char separator = File.separator.charAt(0);
                        if (p.charAt(p.length() - 1) == separator) {
                            p = p.substring(0, p.length() - 1);
                        }
                        h.put("path", p);
                    }
                }
            }
            ifp.close();
            if (!h.isEmpty()) {
                if (h.containsKey("share") && h.containsKey("path")) {
                    s.add(h);
                }
            }
            if (username == null) {
                log.error("Required 'tivo_username' is not set in pyTivo config file: " + config);
            }
            if (password == null) {
                log.error("Required 'tivo_password' is not set in pyTivo config file: " + config);
            }
            if (username == null || password == null) {
                return null;
            }
        } catch (Exception ex) {
            log.error("Problem parsing pyTivo config file: " + config);
            log.error(ex.toString());
            return null;
        }
        return s;
    }

    private Boolean isVideo(String testFile) {
        if (file.isDir(testFile)) {
            return false;
        }
        if (testFile.matches("^.+\\.txt$")) {
            return false;
        }
        Stack<String> command = new Stack<String>();
        command.add(config.ffmpeg);
        command.add("-i");
        command.add(testFile);
        backgroundProcess process = new backgroundProcess();
        if (process.run(command)) {
            try {
                process.Wait(timeout_ffmpeg * 1000);
                Stack<String> l = process.getStderr();
                if (l.size() > 0) {
                    for (int i = 0; i < l.size(); ++i) {
                        if (l.get(i).matches("^.+\\s+Video:\\s+.+$")) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Timing out command that was taking too long: " + process.toString());
            }
            return false;
        } else {
            process.printStderr();
        }
        return false;
    }

    private Boolean push_file(String tivoName, String share, String path, String push_file) {
        if (file.isFile(push_file)) {
            String header = "http://" + host + ":" + port + "/TiVoConnect?Command=Push&Container=";
            String path_entry;
            if (path.length() == 0) {
                path_entry = "&File=/";
            } else {
                path_entry = "&File=/" + urlEncode(path) + "/";
            }
            String urlString = header + urlEncode(share) + path_entry + urlEncode(string.basename(push_file)) + "&tsn=" + urlEncode(tivoName);
            try {
                URL url = new URL(urlString);
                log.warn(">> Pushing " + push_file + " to " + tivoName);
                log.print(url.toString());
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.addRequestProperty("REFERER", "/");
                c.setRequestMethod("GET");
                c.setReadTimeout(timeout_http * 1000);
                c.connect();
                String response = c.getResponseMessage();
                if (response.equals("OK")) {
                    thread_running = false;
                    return true;
                } else {
                    log.error("Received unexpected response for: " + urlString);
                    log.error(response);
                    thread_running = false;
                    return false;
                }
            } catch (Exception e) {
                log.error("Connection failed: " + urlString);
                log.error(e.toString());
            }
        } else {
            log.error("File does not exist - " + push_file);
        }
        thread_running = false;
        return false;
    }

    public static String urlEncode(String s) {
        String encoded;
        try {
            encoded = URLEncoder.encode(s, "UTF-8");
            return encoded;
        } catch (Exception e) {
            log.error("Cannot encode url: " + s);
            log.error(e.toString());
            return s;
        }
    }
}
