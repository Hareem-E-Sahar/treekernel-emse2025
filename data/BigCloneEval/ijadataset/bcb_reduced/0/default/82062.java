import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import com.rbnb.utility.ArgHandler;
import com.rbnb.sapi.ChannelMap;
import com.rbnb.sapi.SAPIException;
import com.rbnb.sapi.Sink;
import com.rbnb.sapi.Source;

public class SkyRouterClient {

    public String address = "localhost:3333";

    public String sourceName = "SkyRouterOutput";

    public int cacheFrames = 10;

    public int archiveFrames = 0;

    public String archiveMode = "none";

    public String username = null;

    public String password = null;

    public long imeiNum = 0;

    public boolean bCatchup = false;

    public int pollPeriod = 30;

    public static int MIN_POLL_PERIOD = 15;

    public Source source = null;

    public boolean bKeepRunning = true;

    public boolean bShutdown = false;

    public boolean bImmediateShutdown = false;

    public boolean bVerbose = false;

    public static void main(String[] argsI) throws Exception {
        new SkyRouterClient(argsI);
    }

    public SkyRouterClient(String[] argsI) {
        try {
            ArgHandler ah = new ArgHandler(argsI);
            if (ah.checkFlag('a')) {
                String addressL = ah.getOption('a');
                if (addressL != null) {
                    address = addressL;
                } else {
                    System.err.println("WARNING: Null argument to the \"-a\"" + " command line option.");
                }
            }
            if (ah.checkFlag('c')) {
                try {
                    String framesStr = ah.getOption('c');
                    if (framesStr != null) {
                        cacheFrames = Integer.parseInt(framesStr);
                        if (cacheFrames <= 0) {
                            System.err.println("ERROR: The cache frames specified with " + "the \"-c\" flag must be an integer greater " + "than 0");
                            bImmediateShutdown = true;
                            System.exit(0);
                        }
                    } else {
                        System.err.println("WARNING: Null argument to the \"-c\"" + " command line option.");
                    }
                } catch (NumberFormatException nfe) {
                    System.err.println("ERROR: The cache frames specified with the " + "\"-c\" flag is not a number.");
                    bImmediateShutdown = true;
                    System.exit(0);
                }
            }
            if (ah.checkFlag('h')) {
                System.err.println("SkyRouterClient command line options");
                System.err.println("   -a <RBNB address>");
                System.err.println("       default: " + address);
                System.err.println("   -c <cache frames>");
                System.err.println("       default: " + cacheFrames + " frames");
                System.err.println("   -h (display this help message)");
                System.err.println("   -I <IMEI number>");
                System.err.println("       default: none; this is a required " + "argument");
                System.err.println("   -k <archive frames>");
                System.err.println("       default: " + archiveFrames + " frames, append archive");
                System.err.println("   -K <archive frames>");
                System.err.println("       default: " + archiveFrames + " frames, create archive");
                System.err.println("   -n <output source name>");
                System.err.println("       default: " + sourceName);
                System.err.println("   -o (Catchup on older/missing data)");
                System.err.println("   -p <password>");
                System.err.println("       default: none; this is a required " + "argument");
                System.err.println("   -t <poll period, in seconds>");
                System.err.println("       default: " + pollPeriod);
                System.err.println("   -u <username>");
                System.err.println("       default: none; this is a required " + "argument");
                System.err.println("   -v (Verbose mode)");
                bImmediateShutdown = true;
                System.exit(0);
            }
            if (ah.checkFlag('I')) {
                try {
                    String imeiStr = ah.getOption('I');
                    if (imeiStr != null) {
                        imeiNum = Long.parseLong(imeiStr);
                        if (imeiNum <= 0) {
                            System.err.println("ERROR: The IMEI number specified with " + "the \"-I\" flag must be an integer greater " + "than 0");
                            bImmediateShutdown = true;
                            System.exit(0);
                        }
                    } else {
                        System.err.println("ERROR: Must provide the IMEI number with the " + "\"-I\" command line option.");
                        bImmediateShutdown = true;
                        System.exit(0);
                    }
                } catch (NumberFormatException nfe) {
                    System.err.println("ERROR: The IMEI number specified with the " + "\"-I\" flag is not a number.");
                    bImmediateShutdown = true;
                    System.exit(0);
                }
            } else {
                System.err.println("ERROR: Must provide the IMEI number with the \"-I\" " + "command line option.");
                bImmediateShutdown = true;
                System.exit(0);
            }
            if (ah.checkFlag('k')) {
                try {
                    String framesStr = ah.getOption('k');
                    if (framesStr != null) {
                        archiveFrames = Integer.parseInt(framesStr);
                        if (archiveFrames <= 0) {
                            System.err.println("ERROR: The archive frames specified with " + "the \"-k\" flag must be an integer greater " + "than 0");
                            bImmediateShutdown = true;
                            System.exit(0);
                        }
                        archiveMode = new String("append");
                    } else {
                        System.err.println("WARNING: Null argument to the \"-k\"" + " command line option.");
                    }
                } catch (NumberFormatException nfe) {
                    System.err.println("ERROR: The archive frames specified with the " + "\"-k\" flag is not a number.");
                    bImmediateShutdown = true;
                    System.exit(0);
                }
            }
            if (ah.checkFlag('K')) {
                try {
                    String framesStr = ah.getOption('K');
                    if (framesStr != null) {
                        archiveFrames = Integer.parseInt(framesStr);
                        if (archiveFrames <= 0) {
                            System.err.println("ERROR: The archive frames specified with " + "the \"-K\" flag must be an integer greater " + "than 0");
                            bImmediateShutdown = true;
                            System.exit(0);
                        }
                        archiveMode = new String("create");
                    } else {
                        System.err.println("WARNING: Null argument to the \"-K\"" + " command line option.");
                    }
                } catch (NumberFormatException nfe) {
                    System.err.println("ERROR: The archive frames specified with the " + "\"-K\" flag is not a number.");
                    bImmediateShutdown = true;
                    System.exit(0);
                }
            }
            if (ah.checkFlag('n')) {
                String sourceNameL = ah.getOption('n');
                if (sourceNameL != null) {
                    sourceName = sourceNameL;
                } else {
                    System.err.println("WARNING: Null argument to the \"-n\"" + " command line option.");
                }
            }
            if (ah.checkFlag('o')) {
                bCatchup = true;
            }
            if (ah.checkFlag('p')) {
                String passwordL = ah.getOption('p');
                if (passwordL != null) {
                    password = passwordL;
                } else {
                    System.err.println("ERROR: Must provide a password with the \"-p\" " + "command line option.");
                    bImmediateShutdown = true;
                    System.exit(0);
                }
            } else {
                System.err.println("ERROR: Must provide a password with the \"-p\" command " + "line option.");
                bImmediateShutdown = true;
                System.exit(0);
            }
            if (ah.checkFlag('t')) {
                try {
                    String pollPeriodStr = ah.getOption('t');
                    if (pollPeriodStr != null) {
                        pollPeriod = Integer.parseInt(pollPeriodStr);
                        if (pollPeriod < MIN_POLL_PERIOD) {
                            System.err.println("ERROR: The poll period specified with the " + "\"-t\" flag must be an integer greater than " + MIN_POLL_PERIOD);
                            bImmediateShutdown = true;
                            System.exit(0);
                        }
                    } else {
                        System.err.println("WARNING: Null argument to the \"-t\"" + " command line option.");
                    }
                } catch (NumberFormatException nfe) {
                    System.err.println("ERROR: The poll period specified with the " + "\"-t\" flag is not a number.");
                    bImmediateShutdown = true;
                    System.exit(0);
                }
            }
            if (ah.checkFlag('u')) {
                String usernameL = ah.getOption('u');
                if (usernameL != null) {
                    username = usernameL;
                } else {
                    System.err.println("ERROR: Must provide a username with the \"-u\" command line option.");
                    bImmediateShutdown = true;
                    System.exit(0);
                }
            } else {
                System.err.println("ERROR: Must provide a username with the \"-u\" command line option.");
                bImmediateShutdown = true;
                System.exit(0);
            }
            if (ah.checkFlag('v')) {
                bVerbose = true;
            }
        } catch (Exception e) {
            System.err.println("SkyRouterClient argument exception " + e.getMessage());
            e.printStackTrace();
            bImmediateShutdown = true;
            System.exit(0);
        }
        System.err.println("\nArguments:");
        System.err.println("RBNB address: " + address);
        System.err.println("RBNB source: " + sourceName);
        System.err.println("Cache frames: " + cacheFrames);
        if (archiveFrames == 0) {
            System.err.println("No archive");
        } else {
            System.err.println("Archive frames: " + archiveFrames);
            System.err.println("Archive mode: " + archiveMode);
        }
        System.err.println("Poll period: " + pollPeriod + " sec");
        if (bCatchup) {
            System.err.println("Catchup on older/missing data");
        } else {
            System.err.println("Ignore older/missing data");
        }
        System.err.println("IMEI number: " + imeiNum);
        MyShutdownHook shutdownHook = new MyShutdownHook();
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        try {
            source = new Source(cacheFrames, archiveMode, archiveFrames);
            source.OpenRBNBConnection(address, sourceName);
            System.err.println("\nOpened RBNB connection to " + source.GetServerName() + ", source = " + source.GetClientName() + "\n");
        } catch (SAPIException e) {
            System.err.println(e);
            bImmediateShutdown = true;
            System.exit(0);
        }
        long startRequestTime = System.currentTimeMillis();
        if (bCatchup) {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            cal.set(2010, 01, 01, 00, 00, 00);
            startRequestTime = cal.getTimeInMillis();
            ChannelMap dataMap = null;
            try {
                ChannelMap cm = new ChannelMap();
                cm.Add(new String(source.GetClientName() + "/" + imeiNum + "/" + "Lat"));
                Sink sink = new Sink();
                sink.OpenRBNBConnection(address, "TmpSink");
                sink.Request(cm, 0.0, 0.0, "newest");
                dataMap = sink.Fetch(10000);
                sink.CloseRBNBConnection();
            } catch (SAPIException e) {
                System.err.println("Error trying to determine timestamp of most recent " + "data point.:");
                System.err.println(e);
                bImmediateShutdown = true;
                System.exit(0);
            }
            if (dataMap.NumberOfChannels() == 1) {
                startRequestTime = (long) (dataMap.GetTimeStart(0) * 1000.0);
                startRequestTime = startRequestTime + 1000;
            }
        }
        Date requestDate = new Date(startRequestTime);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy'-'MM'-'dd'+'HH'%3A'mm'%3A'ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateStr = sdf.format(requestDate);
        String urlStr = new String("https://www.skyrouter.com/DataExchange/get.php?userid=" + username + "&pw=" + password + "&source=ft&cmd=since&since=" + dateStr);
        System.err.println("\nInitial \"since\" request URL = " + urlStr);
        try {
            URL skyRouterURL = new URL(urlStr);
            HttpURLConnection skyRouterCon = (HttpURLConnection) skyRouterURL.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(skyRouterCon.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (bVerbose) {
                    System.out.println(inputLine);
                }
                try {
                    String reportDateStr = processData(inputLine);
                    if (reportDateStr != null) {
                        System.err.println(reportDateStr);
                    }
                } catch (Exception e1) {
                    System.err.println("Caught exception processing message:\n" + e1);
                    e1.printStackTrace();
                }
            }
            in.close();
        } catch (Exception e) {
            System.err.println("Caught exception with initial SkyRouter request:\n" + e);
            bImmediateShutdown = true;
            System.exit(0);
        }
        try {
            Thread.sleep(pollPeriod * 1000);
        } catch (Exception e) {
        }
        urlStr = new String("https://www.skyrouter.com/DataExchange/get.php?userid=" + username + "&pw=" + password + "&source=ft&cmd=last&since=");
        System.err.println("\nPolling \"last\" request URL = " + urlStr);
        while (bKeepRunning) {
            try {
                URL skyRouterURL = new URL(urlStr);
                HttpURLConnection skyRouterCon = (HttpURLConnection) skyRouterURL.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(skyRouterCon.getInputStream()));
                String inputLine;
                if (bVerbose) {
                    System.err.println("Checking for data...");
                }
                while ((inputLine = in.readLine()) != null) {
                    if (bVerbose) {
                        System.out.println(inputLine);
                    }
                    try {
                        String reportDateStr = processData(inputLine);
                        if (reportDateStr != null) {
                            System.err.println(reportDateStr);
                        }
                    } catch (Exception e1) {
                        System.err.println("Caught exception processing message:\n" + e1);
                        e1.printStackTrace();
                    }
                }
                in.close();
                Thread.sleep(pollPeriod * 1000);
            } catch (Exception e) {
                System.err.println("Caught exception with SkyRouter request:\n" + e);
                continue;
            }
        }
        source.CloseRBNBConnection();
        bShutdown = true;
    }

    private String processData(String strI) throws Exception {
        if ((strI == null) || (strI.length() == 0)) {
            System.err.println("empty data string, ignoring");
            return null;
        }
        char[] chars = strI.toCharArray();
        int commaCount = 0;
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == ',') {
                ++commaCount;
            }
        }
        if (commaCount != 17) {
            System.err.println("unrecognized message format, ignoring");
            return null;
        }
        String[] strArray = strI.split(",");
        String reportType = strArray[2];
        if ((!reportType.equals("POS")) && (!reportType.equals("TOF")) && (!reportType.equals("LAN")) && (!reportType.equals("OGA")) && (!reportType.equals("IGA")) && (!reportType.equals("FPL")) && (!reportType.equals("QPS")) && (!reportType.equals("CKN"))) {
            System.err.println("ignoring message with Report Type = " + reportType);
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = sdf.parse(new String(strArray[7] + strArray[8]), new ParsePosition(0));
        double rbnbTimestamp = date.getTime() / 1000.0;
        String imeiNumStr = strArray[4];
        long imeiNumLong = 0;
        try {
            imeiNumLong = Long.parseLong(imeiNumStr);
        } catch (NumberFormatException nfe) {
            throw new Exception(new String("Error parsing IMEI number, " + imeiNumStr));
        }
        if (imeiNumLong != imeiNum) {
            System.err.println("ERROR: IMEI number does not match; expecting " + imeiNum + ", got " + imeiNumLong);
            return null;
        }
        ChannelMap dataMap = new ChannelMap();
        int fieldIdx = 0;
        int numFields = strArray.length;
        dataMap.PutTime(rbnbTimestamp, 0.0);
        int idx = dataMap.Add(new String(imeiNumStr + "/SystemDate"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/SystemTime"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/ReportType"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/UnitType"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/IMEINumber"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/Name"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/Registration"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/AcquisitionDate"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/AcquisitionTime"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/Lat"));
        putDataAsFloat64(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/Lon"));
        putDataAsFloat64(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/Alt"));
        putDataAsFloat64(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/Velocity"));
        putDataAsFloat64(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/Heading"));
        putDataAsFloat64(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/DOP"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/ReceiverStatus"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/Origin"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/Destination"));
        putDataAsString(idx, dataMap, fieldIdx, strArray);
        ++fieldIdx;
        idx = dataMap.Add(new String(imeiNumStr + "/_CSV"));
        dataMap.PutDataAsString(idx, strI);
        source.Flush(dataMap);
        return date.toString();
    }

    private void putDataAsString(int chanMapIdxI, ChannelMap dataMapI, int arrayIdxI, String[] dataArrayI) throws SAPIException {
        String str = "N/A\n";
        int numFields = dataArrayI.length;
        if ((numFields > arrayIdxI) && (dataArrayI[arrayIdxI] != null) && (dataArrayI[arrayIdxI].length() > 0)) {
            str = new String(dataArrayI[arrayIdxI] + "\n");
        }
        if (bVerbose) {
            System.err.print("str[" + arrayIdxI + "] = " + str);
        }
        dataMapI.PutDataAsString(chanMapIdxI, str);
    }

    private void putDataAsFloat64(int chanMapIdxI, ChannelMap dataMapI, int arrayIdxI, String[] dataArrayI) throws Exception {
        int numFields = dataArrayI.length;
        double[] data = new double[1];
        data[0] = -999.99;
        if (numFields > arrayIdxI) {
            if (bVerbose) {
                System.err.println("str[" + arrayIdxI + "] = " + dataArrayI[arrayIdxI]);
            }
            try {
                data[0] = Double.parseDouble(dataArrayI[arrayIdxI]);
            } catch (NumberFormatException nfe) {
                throw new Exception(new String("Error parsing field " + arrayIdxI + ": " + dataArrayI[arrayIdxI]));
            }
        } else {
            if (bVerbose) {
                System.err.println("str[" + arrayIdxI + "] = N/A");
            }
        }
        dataMapI.PutDataAsFloat64(chanMapIdxI, data);
    }

    private class MyShutdownHook extends Thread {

        public void run() {
            if (bImmediateShutdown) {
                return;
            }
            System.err.println("\nShutting down the application (NOTE: may have to wait up " + "to one full poll period before program shuts down)...\n");
            bKeepRunning = false;
            while (!bShutdown) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
            System.err.println("...shutdown is complete.");
        }
    }
}
