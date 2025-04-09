import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class server implements ProviderInterface {

    InetAddress serveraddr;

    Integer port;

    BufferedReader in;

    PrintWriter out;

    Socket socket;

    Map<String, String> serviceMap;

    private ConfigurationManager propertymanager;

    private static Logger log = Logger.getLogger();

    public server(InetAddress serveraddr, Integer port) {
        this.serveraddr = serveraddr;
        this.port = port;
        serviceMap = new HashMap<String, String>();
    }

    private void bindToRegistry(String bindName) throws RemoteException {
        ProviderInterface serverstub = (ProviderInterface) UnicastRemoteObject.exportObject(new server(serveraddr, port), 0);
        Registry registry;
        registry = LocateRegistry.getRegistry();
        try {
            registry.bind(bindName, serverstub);
        } catch (AlreadyBoundException ae) {
            log.info("Service :" + bindName + " already bound!");
            try {
                registry.unbind(bindName);
                registry.bind(bindName, serverstub);
            } catch (Exception e) {
                log.error("Exception e :" + e.getMessage());
                e.printStackTrace();
            }
        } catch (ExportException ee) {
        }
        log.info("Service added to Registry.");
    }

    private void removeFromRegistry(String bindName) throws RemoteException {
        Registry registry;
        registry = LocateRegistry.getRegistry();
        try {
            registry.unbind(bindName);
        } catch (NotBoundException nbe) {
            log.info("Service " + bindName + " not bound!");
        }
        log.info("Service removed from Registry.");
    }

    private void init() throws IOException {
        propertymanager = ConfigurationManager.getPropertyManager();
        socket = new Socket(serveraddr, port);
        log.info("Connected to Server: " + socket);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        out.println("HELLO");
        out.flush();
        String strout = in.readLine();
        if (!strout.equalsIgnoreCase("HI")) {
            log.error("Uncorrect Server Response");
            return;
        }
    }

    @Override
    public String add_number(String var1, String var2) throws RemoteException {
        int i, j, k;
        String result;
        try {
            i = Integer.parseInt(var1);
            j = Integer.parseInt(var2);
            k = i + j;
            result = Integer.toString(k);
        } catch (Exception ex) {
            result = "FALSE";
        }
        return result;
    }

    @Override
    public String date(String var1) throws RemoteException {
        String response = "";
        try {
            Process p = Runtime.getRuntime().exec("date " + var1);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String s;
            while ((s = stdInput.readLine()) != null) {
                response += s;
            }
            while ((s = stdError.readLine()) != null) {
                response = "FALSE";
            }
        } catch (IOException e) {
            response = "Error in Execution";
            log.info("Cought exception: " + e.getMessage());
        }
        return response;
    }

    @Override
    public String divide_number(String var1, String var2) throws RemoteException {
        int i, j;
        Float k;
        String result;
        try {
            i = Integer.parseInt(var1);
            j = Integer.parseInt(var2);
            k = ((float) i) / ((float) j);
            result = k + "";
        } catch (Exception ex) {
            result = "FALSE";
        }
        return result;
    }

    public String generate_random(String var1, String var2) throws RemoteException {
        String result = "FALSE";
        try {
            int i = Integer.parseInt(var1);
            int j = Integer.parseInt(var2);
            int x = i + (int) (Math.random() * Math.abs(i - j));
            return i > j ? "FALSE" : x + "";
        } catch (Exception ex) {
            result = "FALSE";
        }
        return result;
    }

    @Override
    public String multiply_number(String var1, String var2) throws RemoteException {
        int i, j, k;
        String result;
        try {
            i = Integer.parseInt(var1);
            j = Integer.parseInt(var2);
            k = i * j;
            result = Integer.toString(k);
        } catch (Exception ex) {
            result = "FALSE";
        }
        return result;
    }

    @Override
    public String subtract_number(String var1, String var2) throws RemoteException {
        int i, j, k;
        String result;
        try {
            i = Integer.parseInt(var1);
            j = Integer.parseInt(var2);
            k = i - j;
            result = Integer.toString(k);
        } catch (Exception ex) {
            result = "FALSE";
        }
        return result;
    }

    public String getBindName() throws IOException {
        return "provider-" + propertymanager.getBindName();
    }

    public void run(BufferedReader scriptfile, boolean isscriptfile) throws IOException {
        int queryNum = 0;
        String strout = "";
        while (true) {
            System.out.print("Command>");
            String strin = scriptfile.readLine();
            if (strin == null) break;
            strin = strin.trim();
            if (strin.equalsIgnoreCase("END") || strin.equalsIgnoreCase("BYE")) continue;
            if (strin.equalsIgnoreCase("EXIT")) break;
            if (strin.contains("%")) {
                strin = strin.substring(0, strin.indexOf('%'));
            }
            if (strin.length() == 0) continue;
            String cmd[] = strin.split(" ");
            if (cmd[0].equalsIgnoreCase("QUERY")) {
                queryNum++;
                String query = "";
                for (int i = 1; i < cmd.length; i++) query += " " + cmd[i];
                strin = "QUERY " + queryNum + " " + query;
            }
            if (cmd[0].equalsIgnoreCase("SHUTDOWN")) {
                System.out.println("NO LONGER SUPPORTED!");
                continue;
            }
            out.println(strin);
            out.flush();
            strout = in.readLine();
            String servList[] = strout.split(" ");
            System.out.println("Server Response:" + strout);
            if (servList[1].equalsIgnoreCase("TRUE") && cmd[0].equalsIgnoreCase("REGISTER")) {
                if (serviceMap.containsKey(cmd[1])) {
                    clean(cmd[1]);
                }
                bindToRegistry(cmd[1]);
                serviceMap.put(cmd[1], "REMOVE " + cmd[1] + " " + cmd[2] + " " + cmd[3]);
            }
            if (servList[1].equalsIgnoreCase("TRUE") && cmd[0].equalsIgnoreCase("REMOVE")) {
                removeFromRegistry(cmd[1]);
                serviceMap.remove(cmd[1]);
            }
            if (servList[0].equalsIgnoreCase("QUERY-REPLY")) {
                int results = Integer.parseInt(servList[2]);
                for (int i = 0; i < results; i++) {
                    String queryresult = in.readLine();
                    System.out.println("Query Result: " + queryresult);
                }
            }
        }
        if (!isscriptfile) {
            cleanup();
            out.println("BYE");
            out.flush();
            strout = in.readLine();
        }
    }

    private void clean(String key) throws IOException {
        String strin = serviceMap.get(key);
        log.info("Sending: " + strin);
        out.println(strin);
        out.flush();
        String strout = in.readLine();
        String servList[] = strout.split(" ");
        System.out.println("Server Response:" + strout);
        if (servList[1].equalsIgnoreCase("TRUE")) {
            try {
                removeFromRegistry(key);
            } catch (Exception e) {
            }
        }
    }

    private void cleanup() throws IOException {
        log.info("Cleaning up..");
        for (String key : serviceMap.keySet()) {
            clean(key);
        }
    }

    public static void main(String args[]) {
        if (args.length < 2) {
            System.out.println("Usage java provider broker-ip port [scriptfile] ");
            System.exit(1);
        }
        try {
            InetAddress addr = InetAddress.getByName(args[0]);
            Integer port = Integer.parseInt(args[1]);
            BufferedReader reader = null;
            server server = new server(addr, port);
            if (args.length >= 3) reader = new BufferedReader(new FileReader(args[2]));
            server.init();
            log.info("Server Ready. Type exit at the command prompt to quit.");
            if (args.length >= 3) server.run(reader, true);
            server.run(new BufferedReader(new InputStreamReader(System.in)), false);
            System.exit(0);
        } catch (Exception e) {
            log.error("Cought Exception : " + e);
            e.printStackTrace();
        }
    }
}
