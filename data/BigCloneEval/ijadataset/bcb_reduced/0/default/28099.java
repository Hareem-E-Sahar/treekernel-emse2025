import java.io.*;
import java.net.*;
import java.util.Vector;

public class FTP {

    private String host;

    private int port;

    private boolean passiveMode;

    private Socket controlSocket;

    private BufferedReader controlReader;

    private PrintWriter controlWriter;

    private int lastresult = 0;

    private boolean newresult = false;

    private Vector lastlines;

    private Socket dataSocket;

    private ServerSocket dataSSocket;

    private FTPPassiveConnection dataPassiveConnection;

    private FTPPortConnection dataPortConnection;

    private int portrange_lower = 10000;

    private int portrange_upper = 10001;

    private final java.util.Random random = new java.util.Random();

    private String localip = "127,0,0,1";

    public static final int OK = 0;

    public static final int ERROR = -1;

    public static final int ERROR_HOST_LOOKUP = 1;

    public static final int ERROR_SOCKET_IO = 2;

    public static final int ERROR_TIMEOUT = 3;

    public static final int SOCKET_TIMEOUT = 5000;

    public static final int FTP_COMMAND_OK = 200;

    public static final int FTP_COMMAND_NOT_IMPLEMENTED = 502;

    public static final int FTP_SERVICE_READY = 220;

    public static final int FTP_USER_LOGGED_IN = 230;

    public static final int FTP_PASSWORD_NEEDED = 331;

    public static final int FTP_USER_NOT_LOGGED_IN = 530;

    public FTP() {
        passiveMode = true;
        lastlines = new Vector();
    }

    ;

    public int getUpperPortrange() {
        return portrange_upper;
    }

    public int getLowerPortrange() {
        return portrange_lower;
    }

    public void setPortrange(int lowerbound, int upperbound) {
        portrange_lower = lowerbound;
        portrange_upper = upperbound;
    }

    public int connect(String host, int port) {
        InputStream controlIn;
        OutputStream controlOut;
        this.host = host;
        this.port = port;
        try {
            controlSocket = new Socket(host, port);
        } catch (UnknownHostException e) {
            return ERROR_HOST_LOOKUP;
        } catch (InterruptedIOException e) {
            return ERROR_TIMEOUT;
        } catch (IOException e) {
            return ERROR_SOCKET_IO;
        }
        try {
            controlSocket.setSoTimeout(SOCKET_TIMEOUT);
        } catch (SocketException e) {
            System.out.println(e);
            return ERROR_SOCKET_IO;
        }
        try {
            controlIn = controlSocket.getInputStream();
            controlOut = controlSocket.getOutputStream();
            controlReader = new BufferedReader(new InputStreamReader(controlIn));
            controlWriter = new PrintWriter(new OutputStreamWriter(controlOut));
        } catch (IOException e) {
            return ERROR_SOCKET_IO;
        }
        waitForResult();
        if (getLastResult() == FTP_SERVICE_READY) return OK; else return ERROR;
    }

    public int login(String user, String pass) {
        printCommand("USER " + user);
        controlWriter.flush();
        try {
            controlSocket.getOutputStream().flush();
        } catch (IOException e) {
            System.out.println("in login: " + e);
        }
        waitForResult();
        if (getLastResult() == FTP_PASSWORD_NEEDED) {
            printCommand("PASS " + pass);
            controlWriter.flush();
            waitForResult();
        }
        if (getLastResult() == FTP_USER_LOGGED_IN) return OK; else return ERROR;
    }

    public void waitForResult() {
        while (!newResults() && readln() != null) ;
    }

    public void close() {
        try {
            printCommand("QUIT");
            controlWriter.flush();
            waitForResult();
            controlSocket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private String readln() {
        String line;
        try {
            line = controlReader.readLine();
        } catch (IOException e) {
            System.out.println("in readln(): " + e);
            return null;
        }
        if (line == null) return null;
        if ((line.charAt(0) >= '0') && (line.charAt(0) <= '9') && (line.charAt(3) == ' ')) {
            lastresult = Integer.parseInt(line.substring(0, 3));
            newresult = true;
        }
        lastlines.add(line);
        return line;
    }

    public int getLastResult() {
        newresult = false;
        return lastresult;
    }

    public boolean newResults() {
        return newresult;
    }

    public Vector getLastLines() {
        Vector lines;
        lines = lastlines;
        lastlines = new Vector();
        return lines;
    }

    public void printLastLines() {
        int i;
        for (i = 0; i < lastlines.size(); i++) System.out.println((String) lastlines.get(i));
        lastlines = new Vector();
    }

    public boolean newLines() {
        if (lastlines.size() > 0) return true; else return false;
    }

    private int[] parsePassiveParentheses(String s) {
        String b;
        int[] ba;
        int i2 = 0;
        int i1 = 0;
        ba = new int[6];
        s = s.substring(s.indexOf('(') + 1, s.indexOf(')')).concat(",");
        for (i2 = 0; i2 < 6; i2++) {
            ba[i2] = Integer.parseInt(s.substring(0, s.indexOf(',')));
            s = s.substring(s.indexOf(',') + 1);
        }
        return ba;
    }

    private String port2ascii(int port) {
        return Integer.toString((port >> 8) & 255) + "," + Integer.toString(port & 255);
    }

    private void printCommand(String cmd) {
        controlWriter.print(cmd + "\r\n");
        controlWriter.flush();
    }

    private OutputStream dataOutStreamCommand(String cmd) {
        OutputStream dataStream;
        if (passiveMode) {
            printCommand("PASV");
            waitForResult();
            getLastResult();
            int[] i = parsePassiveParentheses((String) lastlines.get(lastlines.size() - 1));
            String datahost = i[0] + "." + i[1] + "." + i[2] + "." + i[3];
            int dataport = (i[4] * 256) + i[5];
            try {
                dataPassiveConnection = new FTPPassiveConnection(datahost, dataport);
                dataStream = dataPassiveConnection.getOut();
            } catch (IOException e) {
                System.out.println(e);
                return null;
            }
            printCommand(cmd);
            waitForResult();
            getLastResult();
        } else {
            try {
                dataPortConnection = new FTPPortConnection(portrange_lower + random.nextInt(portrange_upper));
            } catch (IOException e) {
                System.out.println(e);
                return null;
            }
            printCommand(cmd);
            waitForResult();
            getLastResult();
            try {
                dataPortConnection.waitForConnection();
            } catch (InterruptedException e) {
                System.out.println("Interrupted in join(): " + e);
            }
            try {
                dataStream = dataPortConnection.getOut();
            } catch (IOException e) {
                System.out.println("Can not establish connection :" + e);
                return null;
            }
        }
        return dataStream;
    }

    private InputStream dataInStreamCommand(String cmd) {
        InputStream dataStream;
        if (passiveMode) {
            printCommand("PASV");
            waitForResult();
            getLastResult();
            int[] i = parsePassiveParentheses((String) lastlines.get(lastlines.size() - 1));
            String datahost = i[0] + "." + i[1] + "." + i[2] + "." + i[3];
            int dataport = (i[4] * 256) + i[5];
            try {
                dataPassiveConnection = new FTPPassiveConnection(datahost, dataport);
                dataStream = dataPassiveConnection.getIn();
            } catch (IOException e) {
                System.out.println(e);
                return null;
            }
            printCommand(cmd);
            waitForResult();
            getLastResult();
        } else {
            int port = portrange_lower + random.nextInt(portrange_upper - portrange_lower);
            System.out.println("port " + port);
            try {
                dataPortConnection = new FTPPortConnection(port);
            } catch (IOException e) {
                System.out.println(e);
                return null;
            }
            printCommand("PORT " + localip + "," + port2ascii(port));
            waitForResult();
            getLastResult();
            printCommand(cmd);
            waitForResult();
            getLastResult();
            try {
                dataPortConnection.waitForConnection();
            } catch (InterruptedException e) {
                System.out.println("Interrupted in join(): " + e);
            }
            try {
                dataStream = dataPortConnection.getIn();
            } catch (IOException e) {
                System.out.println("Can not establish connection :" + e);
                return null;
            }
        }
        return dataStream;
    }

    private void closeData() {
        if (passiveMode) {
            try {
                dataPassiveConnection.close();
            } catch (IOException e) {
                System.out.println("In closeData(): " + e);
            }
        } else {
            try {
                dataPortConnection.close();
                dataPortConnection = null;
                System.gc();
            } catch (IOException e) {
                System.out.println("In closeData(): " + e);
            }
        }
    }

    public Vector ls() {
        InputStream ins = dataInStreamCommand("NLST");
        if (ins == null) return null;
        BufferedReader in = new BufferedReader(new InputStreamReader(ins));
        Vector list;
        String line;
        list = new Vector();
        if (in == null) return null;
        try {
            while ((line = in.readLine()) != null) list.add(line);
        } catch (IOException e) {
            System.out.println("in ls(): " + e);
            return null;
        }
        closeData();
        waitForResult();
        return list;
    }

    public Vector longls() {
        InputStream ins = dataInStreamCommand("LIST");
        if (ins == null) return null;
        BufferedReader in = new BufferedReader(new InputStreamReader(ins));
        Vector list;
        String line;
        list = new Vector();
        if (in == null) return null;
        try {
            while ((line = in.readLine()) != null) list.add(line);
        } catch (IOException e) {
            System.out.println("in longls(): " + e);
            return null;
        }
        closeData();
        waitForResult();
        return list;
    }

    public int cd(String path) {
        printCommand("CWD " + path);
        waitForResult();
        getLastResult();
        return OK;
    }

    public int rm(String path) {
        printCommand("DELE " + path);
        waitForResult();
        getLastResult();
        return OK;
    }

    public int mv(String from, String to) {
        printCommand("RNFR " + from);
        waitForResult();
        getLastResult();
        printCommand("RNTO " + to);
        waitForResult();
        getLastResult();
        return OK;
    }

    public int mkdir(String dir) {
        printCommand("MKD " + dir);
        waitForResult();
        getLastResult();
        return OK;
    }

    public int rmdir(String dir) {
        printCommand("RMD " + dir);
        waitForResult();
        getLastResult();
        return OK;
    }

    public int setAsciiMode() {
        printCommand("TYPE A");
        waitForResult();
        getLastResult();
        return OK;
    }

    public int setBinaryMode() {
        printCommand("TYPE I");
        waitForResult();
        getLastResult();
        return OK;
    }

    public int quote(String arg) {
        printCommand(arg);
        waitForResult();
        getLastResult();
        return OK;
    }

    public void setPassiveMode(boolean b) {
        this.passiveMode = b;
    }

    public boolean getPassiveMode() {
        return passiveMode;
    }

    public int getFile(String path, OutputStream dest) {
        InputStream ins = dataInStreamCommand("RETR " + path);
        if (ins == null) return ERROR;
        BufferedInputStream in = new BufferedInputStream(ins);
        BufferedOutputStream out = new BufferedOutputStream(dest);
        byte[] buf = new byte[4096];
        int count;
        try {
            while ((count = in.read(buf, 0, 4096)) != -1) {
                out.write(buf, 0, count);
                System.out.println(count + " bytes written");
            }
            out.close();
            in.close();
            closeData();
        } catch (IOException e) {
            System.out.println("in getFile: " + e);
            try {
                out.close();
            } catch (IOException e2) {
            }
            return ERROR;
        }
        waitForResult();
        getLastResult();
        return OK;
    }

    public int putFile(String path, InputStream data) {
        OutputStream outs = dataOutStreamCommand("STOR " + path);
        BufferedOutputStream out = new BufferedOutputStream(outs);
        BufferedInputStream in = new BufferedInputStream(data);
        byte[] buf = new byte[4096];
        int count;
        try {
            while ((count = in.read(buf, 0, 4096)) != -1) {
                out.write(buf, 0, count);
                System.out.println(count + "bytes transferred");
            }
            out.close();
            closeData();
            in.close();
        } catch (IOException e) {
            System.out.println("in putFile: " + e);
            try {
                out.close();
            } catch (IOException e2) {
            }
            return ERROR;
        }
        waitForResult();
        getLastResult();
        return OK;
    }
}
