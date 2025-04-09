import java.io.*;
import java.util.*;

class CGIResource extends HTTPResource {

    private String command;

    private Runtime runTime = null;

    private Process process = null;

    private BufferedReader commandsOut = null;

    private BufferedReader commandsErr = null;

    private CGILoader cgiLoader;

    private HTTPWorker hw;

    private WebSite ws;

    private int status = 200;

    private String type = null;

    private String location = null;

    private String path;

    private Hashtable env;

    private boolean isClose;

    CGIResource(String path, HTTPWorker hw, WebSite ws) {
        super(path);
        this.hw = hw;
        this.ws = ws;
        this.path = pathConvert(path);
    }

    private String pathConvert(String path) {
        String convertPath;
        int first = 0;
        int last = path.indexOf(ws.cgiLocation) + ws.cgiLocation.length() + 1;
        if (path.indexOf(File.separator, last) < 0) return path; else last = path.indexOf(File.separator, last);
        convertPath = path.substring(first, last);
        return convertPath;
    }

    HTTPResource load() throws HTTPErrorException {
        cgiLoader = new CGILoader(ws, hw);
        if (!cgiLoader.isCGIExist(path)) {
            status = 404;
            throw new HTTPErrorException(404);
        }
        try {
            env = cgiLoader.setCGIEnvirovment();
        } catch (Exception e) {
            status = 500;
            dlhttpd.logger.info("Script " + path + " envirovment ERROR: " + e.getMessage());
            throw new HTTPErrorException(500);
        }
        if ((command = cgiLoader.getCommandLine()) == null) {
            status = 404;
            throw new HTTPErrorException(404);
        }
        try {
            run();
        } catch (Exception e) {
            if (process != null) try {
                close();
            } catch (IOException ioe) {
                dlhttpd.logger.info("Script " + path + " IO ERROR: " + ioe.getMessage());
            }
            status = 500;
            dlhttpd.logger.info("Script " + path + " ERROR: " + e.getMessage());
            throw new HTTPErrorException(500);
        }
        return this;
    }

    private void run() throws Exception {
        runTime = Runtime.getRuntime();
        String perlCommand = "perl ".concat(command);
        String execCommand = command;
        dlhttpd.logger.info("Script " + path + " run with command: " + execCommand);
        File workDir = new File(ws.documentRoot.concat(ws.cgiLocation));
        process = runTime.exec(execCommand, cgiLoader.getEnvArray(env), workDir);
        commandsOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
        commandsErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        (new CGIError(this, commandsErr)).start();
        String line = null;
        boolean isStatus = false;
        do {
            if ((line = commandsOut.readLine()) == null || "".equals(line)) break;
            if (line.indexOf(":") >= 0) {
                String field = line.substring(0, line.indexOf(":")).trim();
                String value = line.substring(line.indexOf(":") + 1).trim();
                if (field.compareToIgnoreCase("Content-type") == 0) if (type == null) type = value; else throw new Exception("Script " + path + " ERROR: double header Content-type"); else if (field.compareToIgnoreCase("Location") == 0) if (location == null) location = value; else throw new Exception("Script " + path + " ERROR: double header Location"); else if (field.compareToIgnoreCase("Status") == 0) {
                    if (!isStatus) try {
                        status = Integer.parseInt(value);
                        isStatus = true;
                    } catch (NumberFormatException e) {
                        throw new Exception("Script " + path + " ERROR: parse header Status: " + e.getMessage());
                    } else throw new Exception("Script " + path + " ERROR: double header Status");
                }
            }
        } while (true);
        if (type == null || "".equals(type)) if (location == null || "".equals(location)) throw new Exception("Script " + path + "  ERROR: bad headers Content-type and Location"); else {
            isClose = true;
            close();
        }
    }

    int read(byte[] b) throws IOException {
        if (isClose) return -1;
        int i = 0;
        while (i < b.length) {
            int ii = commandsOut.read();
            if (ii == -1) break;
            b[i++] = (byte) ii;
        }
        return i;
    }

    int getStatusCode() {
        return status;
    }

    String getType() {
        return type;
    }

    String getLength() {
        return null;
    }

    /**
  * Check if script is NPH
  * @return true if NPH
  */
    boolean isNPH() {
        if (path.toLowerCase().indexOf("nph-") < 0) return false; else return true;
    }

    void close() throws IOException {
        try {
            process.exitValue();
        } catch (IllegalThreadStateException e) {
            try {
                Thread.currentThread();
                Thread.sleep(500);
            } catch (InterruptedException i) {
            }
        }
        commandsOut.close();
    }

    protected static void sendTo(CGIResource cgi, BufferedReader errReader) {
        cgi.toLogger(errReader);
    }

    protected void toLogger(BufferedReader errReader) {
        String line = null;
        int lineCount = 0;
        try {
            while ((line = errReader.readLine()) != null) {
                dlhttpd.logger.info("Script " + path + "  ERR_LINE: " + line);
                lineCount++;
            }
        } catch (IOException e) {
            dlhttpd.logger.info("Script " + path + "  ERR_READ_LINE: " + e.getMessage());
        } finally {
            try {
                errReader.close();
            } catch (IOException e) {
                e.getMessage();
                dlhttpd.logger.info("Script " + path + "  ERR_READ_LINE: " + e.getMessage());
            }
        }
        if (lineCount > 0) dlhttpd.logger.info("Script " + path + " " + lineCount + " lines received on ERR_READER");
    }
}
