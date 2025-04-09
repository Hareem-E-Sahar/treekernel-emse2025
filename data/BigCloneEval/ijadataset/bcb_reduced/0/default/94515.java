import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.*;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import gnu.getopt.*;
import rrd.Rrd;

public class perf2rrd {

    String NAG_CONFIG = "/etc/nagios/nagios.cfg";

    String NAG_COMMAND = "/usr/sbin/nagios";

    String RRD_REPOSITORY = "/var/log/nagios/rrd";

    String OBJECT_CACHE = "";

    String INTERVAL_FIELD = "check_interval";

    int INT_LENGTH = 1;

    boolean DEBUG_MODE = false;

    boolean RUN_ONCE = false;

    int DATA_POINTS = 500;

    int DAY_POINTS = 86400;

    int[] TIME_PERIODS = { 7, 31, 180 };

    int SLEEP_MILLIS = 10000;

    int NAGIOS_VERSION = 1;

    int TIME_INDEX;

    int HOST_INDEX;

    int SVC_INDEX;

    int PERF_INDEX;

    int COUNT_INDEXES = 0;

    boolean READ_FIFO = false;

    public static void main(String args[]) {
        System.out.println("perf2rrd starting");
        perf2rrd perfParser = new perf2rrd();
        perfParser.startParsing(args);
    }

    void startParsing(String[] args) {
        getOptions(args);
        StringBuffer perfData;
        String perfDataFile = "";
        System.out.println("Using Nagios Config: " + NAG_CONFIG);
        System.out.println("Using Nagios Command: " + NAG_COMMAND);
        System.out.println("Using RRD Repository: " + RRD_REPOSITORY);
        if (DEBUG_MODE) {
            System.out.println("Debug Mode is on");
        }
        NAGIOS_VERSION = findNagiosVersion();
        if (getNagiosParameter("service_perfdata_file_mode").equals("w") || getNagiosParameter("service_perfdata_file_mode").equals("p")) {
            READ_FIFO = true;
            System.out.println("Reading perfdata from named pipe.");
        }
        File repo = new File(RRD_REPOSITORY);
        repo.mkdir();
        if (!(repo.isDirectory())) {
            System.out.println("Unable to create RRD Repository");
            System.exit(1);
        }
        if (NAGIOS_VERSION == 1) {
            perfDataFile = getNagiosParameter("xpdfile_service_perfdata_file");
        } else {
            perfDataFile = getNagiosParameter("service_perfdata_file");
        }
        if (!(new File(perfDataFile).exists())) {
            System.out.println("Unable to find Perf Data File: " + perfDataFile);
            System.exit(1);
        }
        System.out.println("Perf Data File is : " + perfDataFile);
        System.out.println("I believe we are using Nagios ver. " + NAGIOS_VERSION);
        if (NAGIOS_VERSION >= 2) {
            System.out.println("Using Nagios Config: " + NAG_CONFIG);
            OBJECT_CACHE = getNagiosParameter("object_cache_file");
            if (!(new File(OBJECT_CACHE).exists())) {
                System.out.println("If using Nagios 2 or higher the object_cache_file is REQUIRED by perf2rrd.");
                System.exit(1);
            } else {
                System.out.println("Object Cache File is : " + OBJECT_CACHE);
            }
        }
        if (NAGIOS_VERSION <= 2) {
            INTERVAL_FIELD = "normal_check_interval";
        }
        System.out.print("Time periods for RRAs (days): ");
        boolean comma = false;
        for (int d : TIME_PERIODS) {
            System.out.print((comma ? "," : "") + d);
            comma = true;
        }
        System.out.println("\nData points per time period: " + DATA_POINTS);
        try {
            INT_LENGTH = Integer.parseInt(getNagiosParameter("interval_length"));
        } catch (Exception ex) {
            System.out.println("Trouble parsing interval_length, using 1");
            INT_LENGTH = 1;
        }
        if (INT_LENGTH < 1) {
            INT_LENGTH = 1;
        }
        if (DEBUG_MODE) {
            System.out.println("Nagios interval_length: " + INT_LENGTH);
        }
        if (DEBUG_MODE) {
            System.out.println("Nagios interval_field name: " + INTERVAL_FIELD);
        }
        setIndexes();
        if (READ_FIFO) {
            while (true) {
                if (DEBUG_MODE) {
                    System.out.println("Starting FIFO reading loop");
                }
                fifoLoop(perfDataFile);
            }
        } else {
            while (true) {
                perfData = getPerfData(perfDataFile);
                if (perfData.length() != 0) {
                    processPerfData(perfData);
                }
                if (RUN_ONCE) {
                    break;
                }
                perfData = null;
                try {
                    Thread.sleep(SLEEP_MILLIS);
                } catch (Exception ex) {
                }
            }
        }
    }

    void fifoLoop(final String perfDataFile) {
        final BlockingQueue queue = new LinkedBlockingQueue();
        final String StopQueue = "--stop--";
        Thread t = new Thread() {

            public void run() {
                String line;
                try {
                    BufferedReader input = new BufferedReader(new FileReader(perfDataFile));
                    try {
                        while ((line = input.readLine()) != null) {
                            queue.put(line);
                            if (DEBUG_MODE) {
                                System.out.println("Put Queue size is " + queue.size());
                            }
                        }
                    } catch (IOException ioe) {
                        System.out.println("Trouble Reading Fifo: " + ioe);
                    } catch (InterruptedException ie) {
                        System.out.println("Trouble queueing");
                    }
                } catch (IOException fnfe) {
                    System.out.println("Trouble Reading Fifo: " + fnfe);
                }
                if (DEBUG_MODE) {
                    System.out.println("Thread is exiting");
                }
                try {
                    queue.put(StopQueue);
                } catch (InterruptedException ie) {
                    System.out.println("Trouble queueing");
                }
            }
        };
        t.start();
        try {
            while (t.getState() != Thread.State.TERMINATED) {
                String line = queue.take().toString();
                if (line.equals(StopQueue)) {
                    break;
                }
                if (DEBUG_MODE) {
                    System.out.println("Take Queue size is " + queue.size());
                }
                processPerfData(line);
            }
        } catch (InterruptedException ie) {
            System.out.println("Trouble queueing");
        }
    }

    void getOptions(String[] args) {
        Getopt g = new Getopt("perf2rrd", args, "c:n:d:t:p:xo?h");
        int o, val = 0;
        String arg;
        while ((o = g.getopt()) != -1) {
            arg = g.getOptarg();
            try {
                val = Integer.parseInt(arg);
            } catch (Exception ex) {
            }
            switch(o) {
                case 'c':
                    NAG_CONFIG = arg;
                    break;
                case 'n':
                    NAG_COMMAND = arg;
                    break;
                case 'd':
                    RRD_REPOSITORY = arg;
                    break;
                case 'x':
                    DEBUG_MODE = true;
                    break;
                case 'o':
                    RUN_ONCE = true;
                    break;
                case 'p':
                    DATA_POINTS = val;
                    break;
                case 't':
                    String[] periods = arg.split(",");
                    TIME_PERIODS = new int[periods.length];
                    int i = 0;
                    try {
                        for (String p : periods) {
                            TIME_PERIODS[i] = Integer.valueOf(p);
                            i++;
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Bad value for timeperiod\nSee usage documentation");
                        System.exit(1);
                    }
                    break;
                default:
                    System.out.println("\nUsage:\n");
                    System.out.println("\t-c Nagios Config File (default /etc/nagios/nagios.cfg)");
                    System.out.println("\t-n Nagios Command File (default /usr/sbin/nagios)");
                    System.out.println("\t-d RRD Repository Dir (default /var/log/nagios/rrd)");
                    System.out.println("\t-x Debug mode on");
                    System.out.println("\t-o Process data file once then exit");
                    System.out.println("\t-p Data points per time period (default 500)");
                    System.out.println("\t-t Comma separated list of time periods for RRAs, given as days.  (default 7,31,180)");
                    System.out.println("\t   Note: 24 hours worth of unconsolidated data points are always maintained in one RRA");
                    System.out.println("\n");
                    System.exit(0);
            }
        }
    }

    int findNagiosVersion() {
        try {
            String line = null;
            Process vercheck = Runtime.getRuntime().exec(NAG_COMMAND + " --version");
            BufferedReader verout = new BufferedReader(new InputStreamReader(vercheck.getInputStream()));
            Pattern nagVer = Pattern.compile("^Nagios\\s*(\\d*).*");
            while ((line = verout.readLine()) != null) {
                Matcher m = nagVer.matcher(line);
                if (m.find()) {
                    return (Integer.parseInt(m.group(1)));
                }
            }
        } catch (Exception ex) {
            System.out.println("Problem reading output from command " + NAG_COMMAND + "\n");
            System.out.println("You may need to set the -n flag\n");
            System.exit(0);
        }
        return (2);
    }

    String getNagiosParameter(String sToken) {
        String retval = "", line;
        StringBuffer config = parseConfig(NAG_CONFIG);
        BufferedReader br = new BufferedReader(new StringReader(config.toString()));
        try {
            while (((line = br.readLine()) != null) && (retval == "")) {
                StringTokenizer tokens = new StringTokenizer(line, "=");
                while (tokens.hasMoreTokens()) {
                    if (tokens.nextToken().trim().equals(sToken)) {
                        retval = tokens.nextToken().trim();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        return (retval);
    }

    StringBuffer parseConfig(String nagConfig) {
        StringBuffer retval = new StringBuffer();
        try {
            FileReader config = new FileReader(nagConfig);
            BufferedReader br = new BufferedReader(config);
            String line;
            while ((line = br.readLine()) != null) {
                if ((line.startsWith("#")) || (line.trim().equals(""))) {
                    continue;
                }
                StringTokenizer tokens = new StringTokenizer(line, "=");
                while (tokens.hasMoreTokens()) {
                    String tkn = tokens.nextToken().trim();
                    if (tkn.equals("cfg_file")) {
                        retval.append(parseConfig(tokens.nextToken().trim()));
                    }
                    if (tkn.equals("cfg_dir")) {
                        String[] cfgs = getConfigs(tokens.nextToken().trim());
                        for (int i = 0; i < cfgs.length; i++) {
                            retval.append(parseConfig(cfgs[i]));
                        }
                    }
                }
                retval.append(line + "\n");
            }
        } catch (Exception ex) {
            System.out.println("Problem Parsing Config File: " + ex);
        }
        return (retval);
    }

    String[] getConfigs(String cfgdir) {
        ArrayList templist = new ArrayList();
        File[] files = new File(cfgdir).listFiles();
        int count = 0;
        for (int i = 0; i < files.length; i++) {
            String name = files[i].toString();
            if (files[i].isDirectory()) {
                String[] sublist = getConfigs(name);
                for (int j = 0; j < sublist.length; j++) {
                    templist.add(sublist[j]);
                }
            }
            if (name.endsWith(".cfg")) {
                templist.add(name);
            }
        }
        String[] retval = (String[]) templist.toArray(new String[templist.size()]);
        return (retval);
    }

    void setIndexes() {
        String tmpl = "", tkn;
        if (NAGIOS_VERSION == 1) {
            tmpl = getNagiosParameter("xpdfile_service_perfdata_template");
        } else {
            tmpl = getNagiosParameter("service_perfdata_file_template");
        }
        StringTokenizer tokens = new StringTokenizer(tmpl, "\\t");
        COUNT_INDEXES = tokens.countTokens();
        for (int i = 0; i < COUNT_INDEXES; i++) {
            tkn = tokens.nextToken();
            if (tkn.equals("$TIMET$")) {
                TIME_INDEX = i;
            }
            if (tkn.equals("$HOSTNAME$")) {
                HOST_INDEX = i;
            }
            if (tkn.equals("$SERVICEDESC$")) {
                SVC_INDEX = i;
            }
            if (tkn.equals("$SERVICEPERFDATA$") || tkn.equals("$PERFDATA$")) {
                PERF_INDEX = i;
            }
        }
    }

    StringBuffer getPerfData(String filename) {
        StringBuffer retval = new StringBuffer("");
        String line;
        try {
            RandomAccessFile perfFile = new RandomAccessFile(filename, "rws");
            while ((line = perfFile.readLine()) != null) {
                if (line != "") {
                    retval.append(line + "\n");
                }
            }
            perfFile.setLength(0);
            perfFile.close();
        } catch (Exception ex) {
            System.out.println("Trouble Reading Perfdata");
        }
        return retval;
    }

    void processPerfData(StringBuffer perfData) {
        String line = "";
        BufferedReader br = new BufferedReader(new StringReader(perfData.toString()));
        try {
            while ((line = br.readLine()) != null) {
                processPerfData(line);
            }
        } catch (Exception ex) {
            System.out.println("Problem reading perfData StringBuffer");
        }
    }

    void processPerfData(String line) {
        String host = "", service = "", perfOut = "";
        Long date = new Long(0);
        if (DEBUG_MODE) {
            System.out.println("processPerfData called with: " + line);
        }
        StringTokenizer tokens = new StringTokenizer(line, "\t");
        if (tokens.countTokens() == COUNT_INDEXES) {
            for (int i = 0; i < COUNT_INDEXES; i++) {
                String tkn = tokens.nextToken();
                if (i == TIME_INDEX) {
                    date = Long.valueOf(tkn);
                }
                if (i == HOST_INDEX) {
                    host = tkn;
                }
                if (i == SVC_INDEX) {
                    service = tkn;
                }
                if (i == PERF_INDEX) {
                    perfOut = tkn;
                }
            }
            PerfItem[] perfItems = getItems(perfOut);
            for (int i = 0; i < perfItems.length; i++) {
                String rrd = getRRD(host, service, perfItems[i], date.longValue());
                if (!(rrd.equals(""))) {
                    String updateCMD = "update " + rrd + " " + date + ":" + perfItems[i].getItemValue();
                    try {
                        Rrd.getInstance().update(updateCMD);
                        if (DEBUG_MODE) {
                            System.out.println("called update with: " + updateCMD);
                        }
                    } catch (Exception ex) {
                        System.out.println("Problem updating: " + rrd + ": " + ex);
                    }
                }
            }
        }
    }

    PerfItem[] getItems(String s) {
        ArrayList tempList = new ArrayList();
        boolean inQuotes = false;
        String stemp = "";
        char ctemp;
        if (s.indexOf('\'') != -1) {
            for (int i = 0; i < s.length(); i++) {
                ctemp = s.charAt(i);
                if (ctemp == '\'') {
                    inQuotes = !(inQuotes);
                    continue;
                }
                if ((inQuotes) && (ctemp == ' ')) {
                    stemp = stemp + '_';
                } else {
                    stemp = stemp + ctemp;
                }
            }
            s = stemp;
        }
        StringTokenizer tokens = new StringTokenizer(s, " ");
        while (tokens.hasMoreTokens()) {
            String temp = tokens.nextToken();
            tempList.add(new PerfItem(temp));
        }
        PerfItem[] retval = (PerfItem[]) tempList.toArray(new PerfItem[tempList.size()]);
        return (retval);
    }

    String getRRD(String h, String s, PerfItem p, long d) {
        String fname = "";
        String hostFilename = stringFix(h);
        String serviceFilename = stringFix(s);
        String perfFilename = stringFix(p.getItemName());
        fname = RRD_REPOSITORY + "/" + hostFilename + "+" + serviceFilename + "+" + perfFilename + ".rrd";
        if (!(new File(fname).exists())) {
            if (!(createRRD(h, s, p, d, fname))) {
                return ("");
            }
        }
        return (fname);
    }

    boolean createRRD(String h, String s, PerfItem p, long d, String fname) {
        StringBuffer createCMD = new StringBuffer("");
        String[] RRAs = { "AVERAGE", "MIN", "MAX" };
        int steps;
        int storePoints = (int) (DATA_POINTS * 1.25);
        double temp_interval = -1.0;
        p.parseFull();
        if (h.equals("") || s.equals("") || (p.getItemName()).equals("")) {
            return (false);
        }
        try {
            temp_interval = Double.parseDouble(getServiceDirective(h, s, INTERVAL_FIELD));
        } catch (Exception ex) {
            System.out.println("Trouble Parsing interval- host:" + h + " service: " + s + " error: " + ex);
        }
        if (temp_interval < 0.0) {
            return (false);
        }
        temp_interval = temp_interval * INT_LENGTH;
        int interval = (int) temp_interval;
        int heartbeat = interval * 2;
        String rrdType = "GAUGE";
        int type = p.getItemType();
        if (type == PerfItem.COUNTER) {
            rrdType = "COUNTER";
        }
        String rrdMin = "U";
        String rrdMax = "U";
        Double min = p.getItemMin();
        Double max = p.getItemMax();
        if (!(min == null)) {
            rrdMin = min.toString();
        }
        if (!(max == null)) {
            rrdMax = max.toString();
        }
        createCMD.append("create " + fname + " --step " + interval + " --start " + (d - interval));
        createCMD.append(" DS:val:" + rrdType + ":" + heartbeat + ":" + rrdMin + ":" + rrdMax);
        for (int i = 0; i < RRAs.length; i++) {
            createCMD.append(" RRA:" + RRAs[i] + ":0.5:1:" + (DAY_POINTS / interval));
            for (int j = 0; j < TIME_PERIODS.length; j++) {
                createCMD.append(" RRA:" + RRAs[i] + ":0.5:");
                steps = (int) ((TIME_PERIODS[j] * DAY_POINTS) / (interval * DATA_POINTS));
                steps = (steps < 1 ? 1 : steps);
                createCMD.append(steps + ":" + storePoints);
            }
        }
        try {
            Rrd.getInstance().create(createCMD.toString());
        } catch (Exception ex) {
            System.out.println("Problem creating file: " + ex);
            return (false);
        }
        System.out.println(fname + " created.");
        return (true);
    }

    String getServiceDirective(String h, String s, String directive) {
        Hashtable services = new Hashtable();
        if (NAGIOS_VERSION == 2) {
            services = parseNagios2Services();
        } else {
            Hashtable hosts = parseHosts();
            Hashtable hostgroups = parseHostGroups(hosts);
            services = parseServices(hosts, hostgroups);
        }
        return (getObjDirective(services, h + "+" + s, directive));
    }

    Hashtable parseHosts() {
        return (parseNagObjects("host"));
    }

    Hashtable parseHostGroups(Hashtable hosts) {
        Hashtable hostgroups = new Hashtable();
        Hashtable hg_config = parseNagObjects("hostgroup");
        Enumeration keys = hg_config.keys();
        while (keys.hasMoreElements()) {
            Object currKey = keys.nextElement();
            String s = getObjDirective(hg_config, currKey, "members");
            StringTokenizer members = new StringTokenizer(s, ",");
            Vector membersList = new Vector();
            while (members.hasMoreTokens()) {
                membersList.add(((String) members.nextToken()).trim());
            }
            hostgroups.put(currKey, membersList);
        }
        return (hostgroups);
    }

    Hashtable parseServices(Hashtable hosts, Hashtable hostgroups) {
        Hashtable services = new Hashtable();
        Hashtable svc_cfg = parseNagObjects("service");
        Enumeration keys = svc_cfg.keys();
        while (keys.hasMoreElements()) {
            Object currKey = keys.nextElement();
            if (!(currKey.toString().startsWith("perf2rrd+"))) {
                services.put(currKey, svc_cfg.get(currKey));
            }
            String sname = getObjDirective(svc_cfg, currKey, "service_description");
            String hosts_cfg = getObjDirective(svc_cfg, currKey, "host_name");
            if (!(hosts_cfg.equals(""))) {
                StringTokenizer hostList = new StringTokenizer(hosts_cfg, ",");
                while (hostList.hasMoreTokens()) {
                    services.put(((String) hostList.nextToken()).trim() + "+" + sname, svc_cfg.get(currKey));
                }
            } else {
                String hostgroupList = getObjDirective(svc_cfg, currKey, "hostgroup_name");
                if (!(hostgroupList.equals(""))) {
                    StringTokenizer groupList = new StringTokenizer(hostgroupList, ",");
                    while (groupList.hasMoreTokens()) {
                        Vector hlist = (Vector) hostgroups.get(groupList.nextToken().trim());
                        Enumeration e = hlist.elements();
                        while (e.hasMoreElements()) {
                            services.put(((String) e.nextElement()).trim() + "+" + sname, svc_cfg.get(currKey));
                        }
                    }
                }
            }
        }
        return (services);
    }

    Hashtable parseNagios2Services() {
        Hashtable services = new Hashtable();
        Hashtable svc_cfg = parseNagObjects("service");
        Enumeration keys = svc_cfg.keys();
        while (keys.hasMoreElements()) {
            Object currKey = keys.nextElement();
            String sname = getObjDirective(svc_cfg, currKey, "service_description");
            String hostname = getObjDirective(svc_cfg, currKey, "host_name");
            services.put(hostname + "+" + sname, svc_cfg.get(currKey));
        }
        return (services);
    }

    String getObjDirective(Hashtable allObjects, Object key, String dName) {
        String retval = "";
        Hashtable currObj = (Hashtable) allObjects.get(key);
        if (currObj.containsKey(dName)) {
            retval = (String) currObj.get(dName);
        } else {
            if (currObj.containsKey("use")) {
                retval = getObjDirective(allObjects, currObj.get("use"), dName);
            }
        }
        return (retval);
    }

    boolean findList(String l, String f) {
        StringTokenizer tokens = new StringTokenizer(l, ",");
        while (tokens.hasMoreTokens()) {
            String aToken = tokens.nextToken().trim();
            if (aToken.equals(f)) {
                return (true);
            }
        }
        return (false);
    }

    Hashtable parseNagObjects(String objName) {
        Hashtable tempTable = new Hashtable();
        StringBuffer config = new StringBuffer();
        if (NAGIOS_VERSION == 1) {
            config = parseConfig(NAG_CONFIG);
        } else {
            config = parseConfig(OBJECT_CACHE);
        }
        String line;
        BufferedReader br = new BufferedReader(new StringReader(config.toString()));
        try {
            while (((line = br.readLine()) != null)) {
                if (line.equals("")) {
                    continue;
                }
                ;
                StringTokenizer tokens = new StringTokenizer(line);
                if (tokens.nextToken().equals("define") && tokens.nextToken().equals(objName)) {
                    Hashtable nagObj = new Hashtable();
                    String name = "";
                    line = br.readLine();
                    while ((line != null) && (parseNagLine(line, nagObj))) {
                        line = br.readLine();
                    }
                    if (nagObj.containsKey("name")) {
                        name = (String) nagObj.get("name");
                    } else if (nagObj.containsKey(objName + "_name")) {
                        name = (String) nagObj.get(objName + "_name");
                    } else {
                        name = "perf2rrd+" + objName + (tempTable.size());
                    }
                    tempTable.put(name, nagObj);
                }
            }
        } catch (Exception ex) {
            System.out.println("Problem parsing Nagios Objects: " + ex);
        }
        return (tempTable);
    }

    StringBuffer parseObjectCache() {
        StringBuffer config = new StringBuffer("");
        try {
            FileReader cache = new FileReader(OBJECT_CACHE);
            BufferedReader br = new BufferedReader(cache);
            String line;
            while ((line = br.readLine()) != null) {
                config.append(line + "\n");
            }
        } catch (Exception ex) {
            System.out.println("Problem reading Object Cache file.");
            return (config);
        }
        return (config);
    }

    boolean parseNagLine(String line, Hashtable obj) {
        StringTokenizer tokens = new StringTokenizer(line);
        String key = "", value;
        if (tokens.hasMoreTokens()) {
            try {
                key = tokens.nextToken();
                if (key.equals("}") || key.equals("")) {
                    return (false);
                }
            } catch (Exception ex) {
            }
        }
        value = line.substring(line.indexOf(key) + key.length()).trim();
        obj.put(key, value.toString());
        return (true);
    }

    String stringFix(String fix) {
        String[] search = { "/", " " };
        String[] replace = { "-", "_" };
        for (int i = 0; i < search.length; i++) {
            fix = fix.replaceAll(search[i], replace[i]);
        }
        return (fix);
    }

    class PerfItem {

        public static final int GAUGE = 0;

        public static final int COUNTER = 1;

        public static final int PERCENT = 2;

        String perfData = "";

        String itemName = "";

        String itemVal = "";

        Double itemMax = null;

        Double itemMin = null;

        int itemType = GAUGE;

        Pattern NVT = Pattern.compile("([^=]*)=([\\d-.]*)(.*)");

        PerfItem() {
            super();
        }

        PerfItem(String s) {
            perfData = s;
            StringTokenizer tokens = new StringTokenizer(s, ";", true);
            String aToken;
            try {
                aToken = tokens.nextToken();
                if (!(aToken.equals(";"))) {
                    Matcher m = NVT.matcher(aToken);
                    if (m.find()) {
                        itemName = m.group(1);
                        itemVal = (m.group(2));
                    }
                }
            } catch (Exception ex) {
            }
        }

        void parseFull() {
            StringTokenizer tokens = new StringTokenizer(perfData, ";", true);
            String aToken;
            String tempType;
            try {
                aToken = tokens.nextToken();
                if (!(aToken.equals(";"))) {
                    Matcher m = NVT.matcher(aToken);
                    if (m.find()) {
                        itemName = m.group(1);
                        itemVal = (m.group(2));
                        if (!m.group(3).equals("")) {
                            tempType = m.group(3);
                            if (tempType.equals("%")) {
                                itemType = PERCENT;
                            } else if (tempType.equals("c")) {
                                itemType = COUNTER;
                            } else {
                                itemType = GAUGE;
                            }
                        }
                    }
                    tokens.nextToken();
                }
                if (itemType == PERCENT) {
                    itemMin = new Double(0.0);
                    itemMax = new Double(100.0);
                }
                if (!tokens.nextToken().equals(";")) {
                    tokens.nextToken();
                }
                if (!tokens.nextToken().equals(";")) {
                    tokens.nextToken();
                }
                aToken = tokens.nextToken();
                if (!(aToken.equals(";"))) {
                    itemMin = new Double(Double.parseDouble(aToken));
                    tokens.nextToken();
                }
                aToken = tokens.nextToken();
                if (!(aToken.equals(";"))) {
                    itemMax = new Double(Double.parseDouble(aToken));
                }
            } catch (Exception ex) {
            }
        }

        public String toString() {
            return ("Name: " + itemName + " Val: " + itemVal + " Min:" + itemMin + " Max: " + itemMax + " Type: " + itemType);
        }

        public String getItemName() {
            return itemName;
        }

        public int getItemType() {
            return itemType;
        }

        public Double getItemMin() {
            return itemMin;
        }

        public Double getItemMax() {
            return itemMax;
        }

        public String getItemValue() {
            return itemVal;
        }
    }
}
