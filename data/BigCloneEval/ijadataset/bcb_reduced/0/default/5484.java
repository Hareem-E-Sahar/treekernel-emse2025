import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.StringTokenizer;

public class Rstart {

    static int runningPrograms = 0;

    /** Hallo Wolfgang,
	* Port 1599 ist der simbaport, kannst Du nicht einen anderen nehmen ?
	* 
	* ja, ich werde auf einen anderen Port (50502 ?! bzw. Idee: 
	* getPortByName("RStart")) umstellen. -> gibt es nicht in JAVA :-(
	*/
    static final int socketPort = 50502;

    private static int uReqNr = 0;

    private static String rpc_call(Socket socket, int iFktnr, String buf) throws IOException {
        send(socket, iFktnr, buf);
        String ret = receive(socket);
        System.out.println(ret);
        return ret;
    }

    /**
	 * @param socket
	 * @param cmd
	 * @throws IOException
	 */
    static void send(Socket socket, int iFktnr, String buf) throws IOException {
        buf = buf + (char) 0;
        byte[] iobuffer = new byte[Math.max(18, buf.length())];
        uReqNr = (uReqNr + 1) % 0x10000;
        ("" + iFktnr + " " + buf.length() + " " + uReqNr + "                                ").getBytes(0, 18, iobuffer, 0);
        socket.getOutputStream().write(iobuffer, (int) 0, 18);
        socket.getOutputStream().write(buf.getBytes(), (int) 0, buf.length());
        socket.getOutputStream().flush();
    }

    static String receive(Socket socket) throws IOException {
        InputStream iStream = socket.getInputStream();
        byte[] header = new byte[18];
        int length = iStream.read(header);
        String str = new String(header, 0, 0, 18);
        StringTokenizer t = new StringTokenizer(str);
        int piFktnr = Integer.parseInt(t.nextToken());
        int puDataLen = Integer.parseInt(t.nextToken());
        int puReqNr = Integer.parseInt(t.nextToken());
        byte[] data = new byte[puDataLen];
        socket.getInputStream().read(data, 0, puDataLen);
        String ret = new String(data, 0, 0, puDataLen - 1);
        return ret;
    }

    public void launch(String programm, String[] args) {
        String cmd = programm;
        for (int i = 0; i < args.length; i++) cmd += " " + args[i];
        boolean launched = false;
        while (!launched) {
            System.out.println("Trying to launch:" + cmd + ";");
            Socket s = findService();
            if (s != null) {
                System.out.println("found service");
                try {
                    rpc_call(s, 1, cmd);
                    launched = true;
                    System.out.println(cmd);
                    s.close();
                } catch (IOException e) {
                    System.out.println("Couldn't talk to service");
                }
            } else {
                try {
                    System.out.println("Starting new service");
                    ServerSocket server = new ServerSocket(socketPort);
                    Rstart.go(cmd);
                    Thread listener = new ListenerThread(server, "Launcher");
                    listener.start();
                    launched = true;
                    System.out.println("started service listener");
                } catch (IOException e) {
                    System.out.println("Socket contended, will try again");
                    System.out.println(e);
                }
            }
        }
    }

    protected Socket findService() {
        try {
            Socket s = new Socket(InetAddress.getLocalHost(), socketPort);
            return s;
        } catch (IOException e) {
            return null;
        }
    }

    static int RobotServer = 0;

    public static void goClass(final String cmd) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        String[] args = cmd.split("[ \t]");
        final String[] realargs = new String[args.length - 1];
        String prog = args[0];
        if (args.length > 1) System.arraycopy(args, 1, realargs, 0, args.length - 1);
        final Class clazz = Class.forName(args[0]);
        final Class[] argsTypes = { String[].class };
        final Method method = clazz.getMethod("main", argsTypes);
        Thread thread = new Thread(args[0]) {

            public void run() {
                System.out.println("running " + cmd);
                try {
                    Object[] obj = { realargs };
                    method.invoke(clazz, obj);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println(e);
                    System.out.println("coudn't run the " + cmd);
                }
            }
        };
        thread.start();
    }

    public static void go(final String cmd) {
        System.out.println("running a " + cmd);
        String[] args = cmd.split("[ \t]");
        try {
            if (args[0].endsWith(".exe")) {
                goExec(cmd);
            } else {
                goClass(cmd);
            }
            runningPrograms++;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
	 * @param is InputStream von dem line zeilenweise gelesen wird
	 * @param stdout Dateiname in dem die line geschrieben wird oder System.out (falls sdtout==null|"")  
	 */
    private static void stdio(InputStream is, String stdout) {
        new Thread() {

            InputStream is;

            String stdout;

            public void run() {
                PrintStream pw = null;
                try {
                    pw = System.out;
                    if (stdout != null && !stdout.equals("")) pw = new PrintStream(new FileOutputStream(stdout));
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = br.readLine()) != null) {
                        System.out.println(line);
                        if (pw != null) {
                            pw.println(line);
                            pw.flush();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (stdout != null && pw != null && pw != System.out) pw.close();
            }

            public void start(InputStream is, String stdout) {
                this.is = is;
                this.stdout = stdout;
                start();
            }

            ;
        }.start(is, stdout);
    }

    private static final String OS = System.getProperty("os.name").toLowerCase();

    private static Runtime runtime = Runtime.getRuntime();

    /**
	 * @param cmd
	 */
    private static int system(String cmd, String stderr) {
        String io;
        Process p;
        boolean background = false;
        try {
            if (stderr != null) io = " >" + stderr; else io = "";
            if (cmd.endsWith("&")) {
                background = true;
                cmd = cmd.substring(0, cmd.length() - 1);
            }
            if ((OS.indexOf("windows ") > -1) || (OS.indexOf("nt") > -1)) {
                p = runtime.exec(cmd);
            } else {
                p = runtime.exec(new String[] { "ksh", "-c", cmd });
            }
            if (background) {
                return 0;
            } else {
                stdio(p.getInputStream(), null);
                stdio(p.getErrorStream(), stderr);
                p.waitFor();
                return p.exitValue();
            }
        } catch (IOException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private static void goExec(String cmd) {
        system(cmd, null);
    }

    /**
	Launcher programQuit method
	You also need to add a runningPrograms++ operation to 
	the Launcher.go method so the Launcher can track how 
	many programs are still running. When this number hits 
	zero, the Launcher itself should shut down so that its 
	resources are freed up for other uses. 

	In each application to be started through the Launcher, 
	you also need to replace System.exit with 


	 Launcher.programQuit();
	 e.getWindow().dispose();
	*/
    public static void programQuit() {
        runningPrograms--;
        if (runningPrograms <= 0) {
            System.exit(0);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Rstart l = new Rstart();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-wait")) {
                Thread.sleep(3000);
            } else {
                l.launch(args[i].substring(1), args);
            }
            ;
        }
        ;
    }
}

class ListenerThread extends Thread {

    ServerSocket server;

    public ListenerThread(ServerSocket socket, String name) {
        super(name);
        this.server = socket;
    }

    public void run() {
        try {
            while (true) {
                System.out.println("about to wait");
                Socket socket = server.accept();
                System.out.println("opened socket from client");
                String cmd = Rstart.receive(socket);
                Rstart.send(socket, 1, "ok;");
                Rstart.go(cmd);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to start");
        }
    }
}
