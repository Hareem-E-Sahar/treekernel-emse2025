import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Client {

    static Socket socket = null;

    static BufferedReader in = null;

    static PrintWriter out = null;

    static int reqNo = 0;

    static String hostName = null;

    static int serverPort1;

    static int serverPort2;

    static int serverPort3;

    static int serverPort4;

    static int serverPort5;

    static int chunkSize = 1024;

    static FileInputStream fis = null;

    static boolean terminate = false;

    static Random randomGenerator;

    static String logFile = "client_" + Calendar.getInstance().getTimeInMillis() + ".log";

    static FileOutputStream fos = null;

    static Vector<Socket> listOfServers = new Vector<Socket>();

    static HashMap<Socket, Integer> listOfServerChunk = new HashMap<Socket, Integer>();

    static CentralServerInterface cStub;

    static String centralServerhostName = "";

    static String rmiServer = "//" + centralServerhostName + "/" + "CentralServer";

    static byte[] iv = new byte[] { (byte) 0x8E, 0x12, 0x39, (byte) 0x9C, 0x07, 0x72, 0x6F, 0x5A };

    public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
        centralServerhostName = args[0];
        rmiServer = "//" + centralServerhostName + "/" + "CentralServer";
        cStub = (CentralServerInterface) Naming.lookup(rmiServer);
        String xChoice = "None";
        String xFileName = " ";
        String xNextOperation = "<Next operation: < hello | list | file-put | file-get | file-aput | file-aget | bye >";
        BufferedReader xStdIn = new BufferedReader(new InputStreamReader(System.in));
        try {
            StringTokenizer xTok = null;
            int xPriority;
            int xChunks;
            while (!terminate) {
                System.out.println(xNextOperation);
                String xCommandLine = xStdIn.readLine();
                System.out.println(xCommandLine);
                xTok = new StringTokenizer(xCommandLine);
                xChoice = xTok.nextToken();
                if (xChoice.equalsIgnoreCase("\"file-aput\"") || xChoice.equalsIgnoreCase("file-aput")) {
                    xFileName = xTok.nextToken();
                    xChunks = Integer.parseInt(xTok.nextToken());
                    xPriority = Integer.parseInt(xTok.nextToken());
                    Putwrapper(xFileName, xChunks, xPriority);
                } else if (xChoice.equalsIgnoreCase("\"file-aget\"") || xChoice.equalsIgnoreCase("file-aget")) {
                    xFileName = xTok.nextToken();
                    xChunks = Integer.parseInt(xTok.nextToken());
                    xPriority = Integer.parseInt(xTok.nextToken());
                    Getwrapper(xFileName, xChunks, xPriority);
                } else if (xChoice.equalsIgnoreCase("\"file-put\"") || xChoice.equalsIgnoreCase("file-put")) {
                    xFileName = xTok.nextToken();
                    xPriority = Integer.parseInt(xTok.nextToken());
                    Putwrapper(xFileName, 0, xPriority);
                } else if (xChoice.equalsIgnoreCase("\"file-get\"") || xChoice.equalsIgnoreCase("file-get")) {
                    xFileName = xTok.nextToken();
                    xPriority = Integer.parseInt(xTok.nextToken());
                    Getwrapper(xFileName, 0, xPriority);
                } else if (xChoice.equalsIgnoreCase("\"list\"") || xChoice.equalsIgnoreCase("list")) {
                    DirectoryList();
                } else if (xChoice.equalsIgnoreCase("\"hello\"") || xChoice.equalsIgnoreCase("hello")) {
                    if (!HelloOperation()) {
                        fos.close();
                        break;
                    }
                } else if (xChoice.equalsIgnoreCase("bye") || xChoice.equalsIgnoreCase("\"bye\"")) {
                    TerminateConnection();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void TerminateConnection() throws IOException {
        String xToServer = "bye " + reqNo;
        SendAndReceive(xToServer);
        terminate = true;
        fos.close();
    }

    private static void DirectoryList() throws IOException {
        String xListOfFiles = cStub.mList();
        String str[] = xListOfFiles.split(":");
        for (int i = 0; i < str.length; i++) {
            System.out.println(str[i]);
        }
    }

    private static void Getwrapper(String pFilename, int pNumchunks, int xPriority) throws Exception {
        File file_obj = new File(pFilename);
        long xFileSize = file_obj.length();
        String xOrigFile = pFilename;
        pFilename = "e_" + pFilename;
        while (true) {
            System.out.println("Waiting....");
            if (cStub.mIsLocked(pFilename, true, xPriority)) {
                System.out.println("Wait done...Execute PutOperation");
                String xTest = cStub.mGetServers(pFilename, xFileSize, xPriority, true);
                GetOperation(pFilename, pNumchunks, xTest);
                SecretKey xKey = cStub.mGetKey(pFilename);
                mDecrypt(new FileInputStream(pFilename), new FileOutputStream(xOrigFile), xKey);
                break;
            }
            System.out.println("Waiting...." + xFileSize);
        }
    }

    private static void GetOperation(String pFileName, int pNumChunks, String xServer) throws IOException {
        String str[] = xServer.split(":");
        int xNumOfPortions = Integer.parseInt(str[0]);
        String[] serverdet = null;
        String xTempFile = "tmp";
        File xf = new File(xTempFile);
        FileOutputStream xfos = new FileOutputStream(xTempFile);
        for (int i = 1; i < str.length; i++) {
            serverdet = str[i].split(" ");
            Socket sock = InitializeConnection(serverdet[0], Integer.parseInt(serverdet[1]));
            String xToServer = " ";
            String xFromServer = " ";
            String[] xTokens;
            int xFileNo;
            xToServer = "get" + " " + reqNo + " " + pFileName;
            xFromServer = SendAndReceive(xToServer);
            if (xFromServer.contains("FAILURE")) {
                System.out.println("File Not Found ");
                return;
            } else if (xFromServer.contains("WAIT")) {
                xFromServer = in.readLine();
            }
            xTokens = xFromServer.split(" ");
            xFileNo = Integer.parseInt(xTokens[4]);
            StartPulling(xFileNo, xfos, pFileName, pNumChunks, sock, xNumOfPortions, xf);
        }
    }

    private static void StartPulling(int pFileNo, FileOutputStream xfos, String pFileName, int pNumChunks, Socket socket, int pNumOfPortions, File xf) throws IOException {
        String xToServer = "pull " + reqNo + " " + pFileNo + " " + chunkSize;
        DataInputStream xdis = new DataInputStream(socket.getInputStream());
        boolean xdone = false;
        int xStartByte = 0;
        int countChunks = 0;
        while (!xdone) {
            xToServer = "pull " + reqNo + " " + pFileNo + " " + xStartByte + " " + chunkSize;
            String xFromServer = SendAndReceive(xToServer);
            String xTokens[] = xFromServer.split(" ");
            int xlength = Integer.parseInt(xTokens[5]);
            xStartByte += xlength;
            byte[] xbyteServer = new byte[xlength];
            xdis.readFully(xbyteServer);
            System.out.println("i am here" + xbyteServer);
            xfos.write(xbyteServer);
            ++countChunks;
            if (countChunks == pNumChunks) {
                xToServer = "abort " + reqNo + " " + pFileNo;
                SendAndReceive(xToServer);
                break;
            }
            if (xTokens[3].equalsIgnoreCase("LAST")) {
                xdone = true;
                pNumOfPortions--;
            }
        }
        if (pNumOfPortions == 0) {
            xfos.close();
        }
        if (pNumChunks == 0) {
            xf.renameTo(new File(pFileName));
        } else {
            xf.delete();
        }
    }

    private static boolean HelloOperation() throws IOException, InterruptedException {
        String xFromServer = " ";
        String xToServer = "hello " + reqNo;
        xFromServer = SendAndReceive(xToServer);
        if (xFromServer.contains("FAILURE")) {
            System.out.println("FAILED NOTICE FROM SERVER ");
            terminate = true;
            return false;
        }
        return true;
    }

    private static String SendAndReceive(String pToServer) throws IOException {
        String xFromServer = " ";
        System.out.println("pToServer:" + pToServer);
        out.println(pToServer);
        WriteToLog(true, pToServer);
        xFromServer = in.readLine();
        WriteToLog(false, xFromServer);
        return xFromServer;
    }

    private static void WriteToLog(boolean pToServer, String pString) throws IOException {
        String xToWrite = " ";
        if (pToServer) {
            xToWrite = "Client Server \"Req ";
        } else {
            xToWrite = "Server Client \"Rsp ";
        }
        System.out.println(pString);
        StringTokenizer xTok = new StringTokenizer(pString);
        xToWrite += xTok.nextToken() + "\"";
        while (xTok.hasMoreTokens()) {
            xToWrite += " " + xTok.nextToken();
        }
        xToWrite += "\n";
        fos.write(xToWrite.getBytes());
    }

    private static void Putwrapper(String pFilename, int pNumchunks, int xPriority) throws Exception {
        File file_obj = new File(pFilename);
        String xOrigFile = pFilename;
        pFilename = "e_" + pFilename;
        long xFileSize = file_obj.length();
        while (true) {
            System.out.println("Waiting....");
            if (cStub.mIsLocked(pFilename, false, xPriority)) {
                System.out.println("Wait done...Execute PutOperation");
                SecretKey xKey = KeyGenerator.getInstance("DES").generateKey();
                cStub.mPutKey(pFilename, xKey);
                mEncrypt(new FileInputStream(xOrigFile), new FileOutputStream(pFilename), xKey);
                String xTest = cStub.mGetServers(pFilename, xFileSize, xPriority, false);
                String str[] = xTest.split(":");
                String lString1 = str[0] + ":";
                String lString2 = str[0] + ":";
                for (int i = 1; i < str.length; i = i + 2) {
                    lString1 += str[i] + ":";
                    lString2 += str[i + 1] + ":";
                }
                PutOperation(pFilename, pNumchunks, lString1);
                PutOperation(pFilename, pNumchunks, lString2);
                break;
            }
        }
    }

    private static void PutOperation(String pFileName, int pNumchunks, String xServer) throws IOException {
        String str[] = xServer.split(":");
        String[] serverdet = null;
        FileInputStream xfis = new FileInputStream(pFileName);
        for (int i = 1; i < str.length; ++i) {
            serverdet = str[i].split(" ");
            Socket s = InitializeConnection(serverdet[0], Integer.parseInt(serverdet[1]));
            System.out.println("  Inside Push ");
            String xToServer = " ";
            String xFromServer = " ";
            String[] xTokens;
            int xFileNo;
            xToServer = "put" + " " + reqNo + " " + pFileName;
            xFromServer = SendAndReceive(xToServer);
            System.out.println("Before #Wait");
            if (xFromServer.contains("WAIT")) {
                System.out.println("  Inside Wait");
                xFromServer = in.readLine();
            }
            xTokens = xFromServer.split(" ");
            xFileNo = Integer.parseInt(xTokens[4]);
            System.out.println("Going to Start Push");
            StartPushing(xFileNo, xfis, pNumchunks, Integer.parseInt(serverdet[2]), s);
        }
    }

    private static void StartPushing(int pFileNo, FileInputStream xFis, int pNumChunks, int pNchunk, Socket s) throws IOException {
        String xFromServer = " ";
        int xStartByte = 0;
        String xheader = "push " + reqNo + " " + pFileNo;
        DataOutputStream xdos = new DataOutputStream(s.getOutputStream());
        chunkSize = 1024;
        byte[] xbyteFile = new byte[chunkSize];
        int xReadBytes = 0;
        int countChunks = 1;
        boolean xIsDone = false;
        while ((xReadBytes = xFis.read(xbyteFile, 0, chunkSize)) != -1) {
            System.out.println("RE " + countChunks + " E " + pNchunk);
            if (xReadBytes < chunkSize || countChunks == pNchunk) {
                xheader += " LAST " + xStartByte + " " + xReadBytes;
                xIsDone = true;
            } else {
                xheader += " NOTLAST " + xStartByte + " " + xReadBytes;
                xStartByte += xReadBytes;
            }
            out.println(xheader);
            WriteToLog(true, xheader);
            xheader = "push " + reqNo + " " + pFileNo;
            xdos.write(xbyteFile, 0, xReadBytes);
            System.out.println("Waiting for Server ------------------");
            xFromServer = in.readLine();
            WriteToLog(false, xFromServer);
            xbyteFile = new byte[chunkSize];
            ++countChunks;
            if (countChunks == pNumChunks) {
                xheader = "abort " + reqNo + " " + pFileNo;
                SendAndReceive(xheader);
                break;
            }
            if (xIsDone == true) {
                break;
            }
        }
        System.out.println(xReadBytes);
    }

    private static Socket InitializeConnection(String pHostName, int port) throws UnknownHostException, IOException {
        socket = new Socket(pHostName, port);
        System.out.println("connected Client to" + port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        randomGenerator = new Random();
        reqNo = randomGenerator.nextInt(1000);
        fos = new FileOutputStream(logFile);
        return socket;
    }

    private static void ReadConfigFile() throws IOException {
        BufferedReader xbr = new BufferedReader(new InputStreamReader(fis));
        hostName = ReadNextLine(xbr);
        serverPort1 = Integer.parseInt(ReadNextLine(xbr));
        serverPort2 = Integer.parseInt(ReadNextLine(xbr));
        serverPort3 = Integer.parseInt(ReadNextLine(xbr));
        serverPort4 = Integer.parseInt(ReadNextLine(xbr));
        serverPort5 = Integer.parseInt(ReadNextLine(xbr));
        chunkSize = Integer.parseInt(ReadNextLine(xbr));
    }

    private static String ReadNextLine(BufferedReader pbr) throws IOException {
        StringTokenizer xTok;
        String xStr = pbr.readLine();
        xTok = new StringTokenizer(xStr);
        xTok.nextToken();
        return xTok.nextToken();
    }

    private static void mDecrypt(InputStream pIn, OutputStream pOut, SecretKey pKey) throws Exception {
        Cipher xCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        xCipher.init(Cipher.DECRYPT_MODE, pKey, paramSpec);
        byte[] buf = new byte[1024];
        pIn = new CipherInputStream(pIn, xCipher);
        int numRead = 0;
        while ((numRead = pIn.read(buf)) >= 0) {
            pOut.write(buf, 0, numRead);
        }
        pOut.close();
    }

    public static void mEncrypt(InputStream pIn, OutputStream pOut, SecretKey pKey) throws Exception {
        Cipher xCipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        xCipher.init(Cipher.ENCRYPT_MODE, pKey, paramSpec);
        byte[] buf = new byte[1024];
        pOut = new CipherOutputStream(pOut, xCipher);
        int numRead = 0;
        while ((numRead = pIn.read(buf)) >= 0) {
            pOut.write(buf, 0, numRead);
        }
        pOut.close();
    }
}
