public class Test {    public jnamed(String conffile) throws IOException {
        FileInputStream fs;
        boolean started = false;
        try {
            fs = new FileInputStream(conffile);
        } catch (Exception e) {
            System.out.println("Cannot open " + conffile);
            return;
        }
        cache = null;
        znames = new Hashtable();
        TSIGs = new Hashtable();
        BufferedReader br = new BufferedReader(new InputStreamReader(fs));
        String line = null;
        while ((line = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(line);
            if (!st.hasMoreTokens()) continue;
            String keyword = st.nextToken();
            if (!st.hasMoreTokens()) {
                System.out.println("Invalid line: " + line);
                continue;
            }
            if (keyword.charAt(0) == '#') continue;
            if (keyword.equals("primary")) addPrimaryZone(st.nextToken());
            if (keyword.equals("secondary")) addSecondaryZone(st.nextToken(), st.nextToken()); else if (keyword.equals("cache")) cache = new Cache(st.nextToken()); else if (keyword.equals("key")) addTSIG(st.nextToken(), st.nextToken()); else if (keyword.equals("port")) {
                short port = Short.parseShort(st.nextToken());
                addUDP(port);
                addTCP(port);
                started = true;
            }
        }
        if (cache == null) {
            System.out.println("no cache specified");
            System.exit(-1);
        }
        if (!started) {
            addUDP((short) 53);
            addTCP((short) 53);
        }
    }
}