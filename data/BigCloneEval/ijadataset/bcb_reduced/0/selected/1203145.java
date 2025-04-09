package cantop;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import gnu.io.*;
import java.nio.ByteBuffer;

/**
 * Class declaration
 *
 * @author Andrew Ewert
 */
public class CanbusPoll extends Thread {

    private Enumeration portList;

    private CommPortIdentifier portId;

    private SerialPort serialPort;

    private OutputStream outputStream;

    private InputStream inputStream;

    private Reader bufReader;

    private StreamTokenizer inputTokenizer;

    private ArrayList<String> rxBuffer;

    private ArrayList<String> txBuffer;

    private CanbusVariables canbusVariables;

    private ZipOutputStream zipLog;

    private String defaultPort;

    private boolean sampledata;

    private boolean datalogging;

    public CanbusPoll(CanbusVariables canbusVariables, ArrayList<String> rxBuffer, ArrayList<String> txBuffer, boolean sampledata, boolean datalogging) {
        this.canbusVariables = canbusVariables;
        this.rxBuffer = rxBuffer;
        this.txBuffer = txBuffer;
        defaultPort = "";
        this.sampledata = sampledata;
        String osname = System.getProperty("os.name", "").toLowerCase();
        if (osname.startsWith("windows")) {
            defaultPort = "";
        } else if (osname.startsWith("linux")) {
            defaultPort = "/dev/ttyUSB0";
        } else if (osname.startsWith("mac")) {
            defaultPort = "";
        } else {
            System.out.println("Sorry, your operating system is not supported");
            System.exit(1);
        }
        if (sampledata == true) defaultPort = "SAMPLEDATA";
        if (datalogging == true) {
            try {
                zipLog = new ZipOutputStream(new FileOutputStream("log.zip"));
                zipLog.putNextEntry(new ZipEntry("log.txt"));
            } catch (IOException io) {
            }
            this.datalogging = datalogging;
        }
    }

    /**
     * Method declaration
     *
     *
     * @param args
     *
     * @see
     */
    @Override
    public void run() {
        boolean portFound = false;
        int mesgCount = 0;
        if (defaultPort.equals("SAMPLEDATA")) {
            try {
                inputStream = new FileInputStream("sampledata.txt");
                bufReader = new BufferedReader(new InputStreamReader(inputStream));
            } catch (IOException e) {
                System.err.println(e);
                System.exit(-1);
            }
            portFound = true;
        } else {
            portList = CommPortIdentifier.getPortIdentifiers();
            while (portList.hasMoreElements()) {
                portId = (CommPortIdentifier) portList.nextElement();
                if (!(defaultPort.equals(portId.getName())) && !(defaultPort.equals(""))) continue;
                if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    try {
                        serialPort = (SerialPort) portId.open("CANTOP", 2000);
                    } catch (PortInUseException e) {
                        continue;
                    }
                    try {
                        serialPort.enableReceiveTimeout(100);
                        serialPort.enableReceiveThreshold(0);
                    } catch (UnsupportedCommOperationException e) {
                        System.err.println(e);
                        e.printStackTrace();
                    }
                    try {
                        outputStream = serialPort.getOutputStream();
                        inputStream = serialPort.getInputStream();
                        bufReader = new BufferedReader(new InputStreamReader(inputStream));
                        String messageString = "V\015";
                        outputStream.write(messageString.getBytes());
                        if (!((BufferedReader) bufReader).readLine().startsWith("V")) {
                            inputStream.close();
                            outputStream.close();
                            bufReader.close();
                            continue;
                        }
                        try {
                            serialPort.enableReceiveTimeout(100000);
                        } catch (UnsupportedCommOperationException e) {
                            System.err.println(e);
                            e.printStackTrace();
                        }
                        System.out.println("Found port: " + portId.getName());
                        portFound = true;
                    } catch (IOException e) {
                        System.err.println(e);
                        portFound = false;
                        if (serialPort != null) serialPort.close();
                        try {
                            if (inputStream != null) inputStream.close();
                            if (outputStream != null) outputStream.close();
                            if (bufReader != null) bufReader.close();
                        } catch (IOException io) {
                        }
                        continue;
                    }
                    try {
                        serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                        serialPort.notifyOnOutputEmpty(true);
                    } catch (UnsupportedCommOperationException e) {
                    } catch (Exception e) {
                        System.out.println(e.toString());
                        continue;
                    }
                    try {
                        String messageString = "";
                        messageString = "\015\015\015";
                        outputStream.write(messageString.getBytes());
                        messageString = "S6\015";
                        outputStream.write(messageString.getBytes());
                        messageString = "Z0\015";
                        outputStream.write(messageString.getBytes());
                        messageString = "O\015";
                        outputStream.write(messageString.getBytes());
                    } catch (IOException e) {
                    }
                }
            }
        }
        if (portFound == true) {
            String chunk = "";
            String sid = "";
            String mesg = "";
            String validChars = "0123456789ABCDEF";
            boolean isNumber = true;
            int offset = 1, i = 1;
            System.out.println("Starting read");
            inputTokenizer = new StreamTokenizer(bufReader);
            inputTokenizer.parseNumbers();
            inputTokenizer.wordChars('t', 't');
            inputTokenizer.eolIsSignificant(false);
            try {
                while (inputTokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                    isNumber = true;
                    if (inputTokenizer.sval == null) {
                        continue;
                    }
                    for (i = 1; i < inputTokenizer.sval.length(); ++i) {
                        if (validChars.indexOf(inputTokenizer.sval.charAt(i)) == -1) {
                            isNumber = false;
                            break;
                        }
                    }
                    if (isNumber == false || inputTokenizer.sval.length() <= 4 || inputTokenizer.sval.length() % 2 == 0) {
                        continue;
                    }
                    sid = (inputTokenizer.sval).substring(1, 4);
                    mesg = (inputTokenizer.sval).substring(4);
                    ++mesgCount;
                    ByteBuffer buffer = ByteBuffer.allocate(mesg.length());
                    chunk = "";
                    offset = 1;
                    for (i = 0; i < mesg.length() / 2; ++i) {
                        chunk = mesg.substring(offset, offset + 2);
                        offset += 2;
                        buffer.put((byte) (Integer.parseInt(chunk, 16) & 0x000000FF));
                    }
                    try {
                        if (rxBuffer.size() < 100) rxBuffer.add(sid + mesg);
                    } catch (NumberFormatException ex) {
                        System.out.println("Illegal line: " + inputTokenizer.sval);
                        continue;
                    }
                    if (defaultPort.equals("SAMPLEDATA")) {
                        try {
                            Thread.sleep(0, 1);
                        } catch (Exception e) {
                        }
                    }
                    canbusVariables.updateMessage(Integer.parseInt(sid, 16), buffer);
                    if (datalogging == true) {
                        zipLog.write((inputTokenizer.sval + "\n").getBytes(), 0, inputTokenizer.sval.length() + 1);
                    }
                }
            } catch (IOException ex) {
                System.out.println("IO Exception Ocurred: " + ex);
            }
            System.out.println("Stopping read");
            System.out.println("Processed messages: " + mesgCount);
            if (datalogging == true) {
                try {
                    zipLog.closeEntry();
                    zipLog.close();
                } catch (IOException io2) {
                    System.out.println(io2);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
            if (serialPort != null) {
                serialPort.close();
            }
            System.exit(1);
        }
        if (portFound == false) {
            System.out.println("An available serial port was not found.");
            System.exit(-1);
        }
    }

    public OutputStream getStream() {
        return outputStream;
    }
}
