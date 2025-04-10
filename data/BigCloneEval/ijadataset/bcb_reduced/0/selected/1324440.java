package com.sun.corba.se.impl.activation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Vector;
import java.util.Properties;
import java.util.StringTokenizer;
import org.omg.CORBA.ORB;
import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.CompletionStatus;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.CorbaResourceUtil;
import com.sun.corba.se.spi.activation.*;
import com.sun.corba.se.spi.activation.ServerHeldDown;
import com.sun.corba.se.spi.activation.RepositoryPackage.ServerDef;
import com.sun.corba.se.spi.activation.LocatorPackage.ServerLocation;
import com.sun.corba.se.spi.activation.LocatorPackage.ServerLocationPerORB;

/**
 * 
 * @version 	1.7, 97/10/19
 * @author	Anita Jindal
 * @since	JDK1.3
 */
public class ServerTool {

    static final String helpCommand = "help";

    static final String toolName = "servertool";

    static final String commandArg = "-cmd";

    static int getServerIdForAlias(ORB orb, String applicationName) throws ServerNotRegistered {
        try {
            Repository rep = RepositoryHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_REPOSITORY_NAME));
            int serverid = rep.getServerID(applicationName);
            return rep.getServerID(applicationName);
        } catch (Exception ex) {
            throw (new ServerNotRegistered());
        }
    }

    void run(String[] args) {
        String[] cmd = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(commandArg)) {
                int cmdLen = args.length - i - 1;
                cmd = new String[cmdLen];
                for (int j = 0; j < cmdLen; j++) cmd[j] = args[++i];
                break;
            }
        }
        try {
            Properties props = System.getProperties();
            props.put("org.omg.CORBA.ORBClass", "com.sun.corba.se.impl.orb.ORBImpl");
            orb = (ORB) ORB.init(args, props);
            if (cmd != null) executeCommand(cmd); else {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                System.out.println(CorbaResourceUtil.getText("servertool.banner"));
                while (true) {
                    cmd = readCommand(in);
                    if (cmd != null) executeCommand(cmd); else printAvailableCommands();
                }
            }
        } catch (Exception ex) {
            System.out.println(CorbaResourceUtil.getText("servertool.usage", "servertool"));
            System.out.println();
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ServerTool tool = new ServerTool();
        tool.run(args);
    }

    String[] readCommand(BufferedReader in) {
        System.out.print(toolName + " > ");
        try {
            int i = 0;
            String cmd[] = null;
            String cmdLine = in.readLine();
            if (cmdLine != null) {
                StringTokenizer st = new StringTokenizer(cmdLine);
                if (st.countTokens() != 0) {
                    cmd = new String[st.countTokens()];
                    while (st.hasMoreTokens()) cmd[i++] = st.nextToken();
                }
            }
            return cmd;
        } catch (Exception ex) {
            System.out.println(CorbaResourceUtil.getText("servertool.usage", "servertool"));
            System.out.println();
            ex.printStackTrace();
        }
        return null;
    }

    void printAvailableCommands() {
        CommandHandler handler;
        System.out.println(CorbaResourceUtil.getText("servertool.shorthelp"));
        for (int i = 0; i < handlers.size(); i++) {
            handler = (CommandHandler) handlers.elementAt(i);
            System.out.print("\t" + handler.getCommandName());
            for (int j = handler.getCommandName().length(); j < maxNameLen; j++) System.out.print(" ");
            System.out.print(" - ");
            handler.printCommandHelp(System.out, CommandHandler.shortHelp);
        }
        System.out.println();
    }

    void executeCommand(String[] cmd) {
        boolean result;
        CommandHandler handler;
        if (cmd[0].equals(helpCommand)) {
            if (cmd.length == 1) printAvailableCommands(); else {
                for (int i = 0; i < handlers.size(); i++) {
                    handler = (CommandHandler) handlers.elementAt(i);
                    if (handler.getCommandName().equals(cmd[1])) {
                        handler.printCommandHelp(System.out, CommandHandler.longHelp);
                    }
                }
            }
            return;
        }
        for (int i = 0; i < handlers.size(); i++) {
            handler = (CommandHandler) handlers.elementAt(i);
            if (handler.getCommandName().equals(cmd[0])) {
                String[] cmdArgs = new String[cmd.length - 1];
                for (int j = 0; j < cmdArgs.length; j++) cmdArgs[j] = cmd[j + 1];
                try {
                    System.out.println();
                    result = handler.processCommand(cmdArgs, orb, System.out);
                    if (result == CommandHandler.parseError) {
                        handler.printCommandHelp(System.out, CommandHandler.longHelp);
                    }
                    System.out.println();
                } catch (Exception ex) {
                }
                return;
            }
        }
        printAvailableCommands();
    }

    private static final boolean debug = false;

    ORB orb = null;

    static Vector handlers;

    static int maxNameLen;

    static {
        handlers = new Vector();
        handlers.addElement(new RegisterServer());
        handlers.addElement(new UnRegisterServer());
        handlers.addElement(new GetServerID());
        handlers.addElement(new ListServers());
        handlers.addElement(new ListAliases());
        handlers.addElement(new ListActiveServers());
        handlers.addElement(new LocateServer());
        handlers.addElement(new LocateServerForORB());
        handlers.addElement(new ListORBs());
        handlers.addElement(new ShutdownServer());
        handlers.addElement(new StartServer());
        handlers.addElement(new Help());
        handlers.addElement(new Quit());
        maxNameLen = 0;
        int cmdNameLen;
        for (int i = 0; i < handlers.size(); i++) {
            CommandHandler handler = (CommandHandler) handlers.elementAt(i);
            cmdNameLen = handler.getCommandName().length();
            if (cmdNameLen > maxNameLen) maxNameLen = cmdNameLen;
        }
    }
}

class RegisterServer implements CommandHandler {

    public String getCommandName() {
        return "register";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.register"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.register1"));
        }
    }

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        int i = 0;
        String applicationName = "";
        String name = "";
        String classpath = "";
        String args = "";
        String vmargs = "";
        int serverId = 0;
        String arg;
        while (i < cmdArgs.length) {
            arg = cmdArgs[i++];
            if (arg.equals("-server")) {
                if (i < cmdArgs.length) name = cmdArgs[i++]; else return parseError;
            } else if (arg.equals("-applicationName")) {
                if (i < cmdArgs.length) applicationName = cmdArgs[i++]; else return parseError;
            } else if (arg.equals("-classpath")) {
                if (i < cmdArgs.length) classpath = cmdArgs[i++]; else return parseError;
            } else if (arg.equals("-args")) {
                while ((i < cmdArgs.length) && !cmdArgs[i].equals("-vmargs")) {
                    args = args.equals("") ? cmdArgs[i] : args + " " + cmdArgs[i];
                    i++;
                }
                if (args.equals("")) return parseError;
            } else if (arg.equals("-vmargs")) {
                while ((i < cmdArgs.length) && !cmdArgs[i].equals("-args")) {
                    vmargs = vmargs.equals("") ? cmdArgs[i] : vmargs + " " + cmdArgs[i];
                    i++;
                }
                if (vmargs.equals("")) return parseError;
            } else return parseError;
        }
        if (name.equals("")) return parseError;
        try {
            Repository repository = RepositoryHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_REPOSITORY_NAME));
            ServerDef server = new ServerDef(applicationName, name, classpath, args, vmargs);
            serverId = repository.registerServer(server);
            Activator activator = ActivatorHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_ACTIVATOR_NAME));
            activator.activate(serverId);
            activator.install(serverId);
            out.println(CorbaResourceUtil.getText("servertool.register2", serverId));
        } catch (ServerNotRegistered ex) {
        } catch (ServerAlreadyActive ex) {
        } catch (ServerHeldDown ex) {
            out.println(CorbaResourceUtil.getText("servertool.register3", serverId));
        } catch (ServerAlreadyRegistered ex) {
            out.println(CorbaResourceUtil.getText("servertool.register4", serverId));
        } catch (BadServerDefinition ex) {
            out.println(CorbaResourceUtil.getText("servertool.baddef", ex.reason));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return commandDone;
    }
}

class UnRegisterServer implements CommandHandler {

    public String getCommandName() {
        return "unregister";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.unregister"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.unregister1"));
        }
    }

    static final int illegalServerId = -1;

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        int serverId = illegalServerId;
        try {
            if (cmdArgs.length == 2) {
                if (cmdArgs[0].equals("-serverid")) serverId = (Integer.valueOf(cmdArgs[1])).intValue(); else if (cmdArgs[0].equals("-applicationName")) serverId = ServerTool.getServerIdForAlias(orb, cmdArgs[1]);
            }
            if (serverId == illegalServerId) return parseError;
            try {
                Activator activator = ActivatorHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_ACTIVATOR_NAME));
                activator.uninstall(serverId);
            } catch (ServerHeldDown ex) {
            }
            Repository repository = RepositoryHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_REPOSITORY_NAME));
            repository.unregisterServer(serverId);
            out.println(CorbaResourceUtil.getText("servertool.unregister2"));
        } catch (ServerNotRegistered ex) {
            out.println(CorbaResourceUtil.getText("servertool.nosuchserver"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return commandDone;
    }
}

class LocateServer implements CommandHandler {

    public String getCommandName() {
        return "locate";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.locate"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.locate1"));
        }
    }

    static final int illegalServerId = -1;

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        int serverId = illegalServerId;
        String endPointType = IIOP_CLEAR_TEXT.value;
        try {
            String arg;
            int i = 0;
            while (i < cmdArgs.length) {
                arg = cmdArgs[i++];
                if (arg.equals("-serverid")) {
                    if (i < cmdArgs.length) serverId = (Integer.valueOf(cmdArgs[i++])).intValue(); else return parseError;
                } else if (arg.equals("-applicationName")) {
                    if (i < cmdArgs.length) serverId = ServerTool.getServerIdForAlias(orb, cmdArgs[i++]); else return parseError;
                } else if (arg.equals("-endpointType")) {
                    if (i < cmdArgs.length) endPointType = cmdArgs[i++];
                }
            }
            if (serverId == illegalServerId) return parseError;
            Locator locator = LocatorHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_LOCATOR_NAME));
            ServerLocation location = locator.locateServer(serverId, endPointType);
            out.println(CorbaResourceUtil.getText("servertool.locate2", location.hostname));
            int numEntries = location.ports.length;
            for (i = 0; i < numEntries; i++) {
                ORBPortInfo orbPort = location.ports[i];
                out.println("\t\t" + orbPort.port + "\t\t" + endPointType + "\t\t" + orbPort.orbId);
            }
        } catch (NoSuchEndPoint ex) {
        } catch (ServerHeldDown ex) {
            out.println(CorbaResourceUtil.getText("servertool.helddown"));
        } catch (ServerNotRegistered ex) {
            out.println(CorbaResourceUtil.getText("servertool.nosuchserver"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return commandDone;
    }
}

class LocateServerForORB implements CommandHandler {

    public String getCommandName() {
        return "locateperorb";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.locateorb"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.locateorb1"));
        }
    }

    static final int illegalServerId = -1;

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        int serverId = illegalServerId;
        String orbId = "";
        try {
            String arg;
            int i = 0;
            while (i < cmdArgs.length) {
                arg = cmdArgs[i++];
                if (arg.equals("-serverid")) {
                    if (i < cmdArgs.length) serverId = (Integer.valueOf(cmdArgs[i++])).intValue(); else return parseError;
                } else if (arg.equals("-applicationName")) {
                    if (i < cmdArgs.length) serverId = ServerTool.getServerIdForAlias(orb, cmdArgs[i++]); else return parseError;
                } else if (arg.equals("-orbid")) {
                    if (i < cmdArgs.length) orbId = cmdArgs[i++];
                }
            }
            if (serverId == illegalServerId) return parseError;
            Locator locator = LocatorHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_LOCATOR_NAME));
            ServerLocationPerORB location = locator.locateServerForORB(serverId, orbId);
            out.println(CorbaResourceUtil.getText("servertool.locateorb2", location.hostname));
            int numEntries = location.ports.length;
            for (i = 0; i < numEntries; i++) {
                EndPointInfo Port = location.ports[i];
                out.println("\t\t" + Port.port + "\t\t" + Port.endpointType + "\t\t" + orbId);
            }
        } catch (InvalidORBid ex) {
            out.println(CorbaResourceUtil.getText("servertool.nosuchorb"));
        } catch (ServerHeldDown ex) {
            out.println(CorbaResourceUtil.getText("servertool.helddown"));
        } catch (ServerNotRegistered ex) {
            out.println(CorbaResourceUtil.getText("servertool.nosuchserver"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return commandDone;
    }
}

class GetServerID implements CommandHandler {

    public String getCommandName() {
        return "getserverid";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.getserverid"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.getserverid1"));
        }
    }

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        if ((cmdArgs.length == 2) && cmdArgs[0].equals("-applicationName")) {
            String str = (String) cmdArgs[1];
            try {
                Repository repository = RepositoryHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_REPOSITORY_NAME));
                try {
                    int result = repository.getServerID(str);
                    out.println();
                    out.println(CorbaResourceUtil.getText("servertool.getserverid2", str, Integer.toString(result)));
                    out.println();
                } catch (ServerNotRegistered e) {
                    out.println(CorbaResourceUtil.getText("servertool.nosuchserver"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return commandDone;
        } else return parseError;
    }
}

class ListServers implements CommandHandler {

    public String getCommandName() {
        return "list";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.list"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.list1"));
        }
    }

    static final int illegalServerId = -1;

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        int serverId = illegalServerId;
        boolean listOneServer = false;
        ServerDef serverDef;
        listOneServer = (cmdArgs.length != 0);
        if ((cmdArgs.length == 2) && cmdArgs[0].equals("-serverid")) serverId = (Integer.valueOf(cmdArgs[1])).intValue();
        if ((serverId == illegalServerId) && listOneServer) return parseError;
        try {
            Repository repository = RepositoryHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_REPOSITORY_NAME));
            if (listOneServer) {
                try {
                    serverDef = repository.getServer(serverId);
                    out.println();
                    printServerDef(serverDef, serverId, out);
                    out.println();
                } catch (ServerNotRegistered e) {
                    out.println(CorbaResourceUtil.getText("servertool.nosuchserver"));
                }
            } else {
                int[] servers = repository.listRegisteredServers();
                out.println(CorbaResourceUtil.getText("servertool.list2"));
                sortServers(servers);
                for (int i = 0; i < servers.length; i++) {
                    try {
                        serverDef = repository.getServer(servers[i]);
                        out.println("\t   " + servers[i] + "\t\t" + serverDef.serverName + "\t\t" + serverDef.applicationName);
                    } catch (ServerNotRegistered e) {
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return commandDone;
    }

    static void printServerDef(ServerDef serverDef, int serverId, PrintStream out) {
        out.println(CorbaResourceUtil.getText("servertool.appname", serverDef.applicationName));
        out.println(CorbaResourceUtil.getText("servertool.name", serverDef.serverName));
        out.println(CorbaResourceUtil.getText("servertool.classpath", serverDef.serverClassPath));
        out.println(CorbaResourceUtil.getText("servertool.args", serverDef.serverArgs));
        out.println(CorbaResourceUtil.getText("servertool.vmargs", serverDef.serverVmArgs));
        out.println(CorbaResourceUtil.getText("servertool.serverid", serverId));
    }

    /** 
 * Do a simple bubble sort to sort the server ids in ascending
 * order.
 */
    static void sortServers(int[] serverIds) {
        int size = serverIds.length;
        int lowest;
        for (int i = 0; i < size; i++) {
            lowest = i;
            for (int j = i + 1; j < size; j++) {
                if (serverIds[j] < serverIds[lowest]) lowest = j;
            }
            if (lowest != i) {
                int temp = serverIds[i];
                serverIds[i] = serverIds[lowest];
                serverIds[lowest] = temp;
            }
        }
    }
}

class ListActiveServers implements CommandHandler {

    public String getCommandName() {
        return "listactive";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.listactive"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.listactive1"));
        }
    }

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        ServerDef serverDef;
        try {
            Repository repository = RepositoryHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_REPOSITORY_NAME));
            Activator activator = ActivatorHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_ACTIVATOR_NAME));
            int[] servers = activator.getActiveServers();
            out.println(CorbaResourceUtil.getText("servertool.list2"));
            ListServers.sortServers(servers);
            for (int i = 0; i < servers.length; i++) {
                try {
                    serverDef = repository.getServer(servers[i]);
                    out.println("\t   " + servers[i] + "\t\t" + serverDef.serverName + "\t\t" + serverDef.applicationName);
                } catch (ServerNotRegistered e) {
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return commandDone;
    }
}

class ListAliases implements CommandHandler {

    public String getCommandName() {
        return "listappnames";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.listappnames"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.listappnames1"));
        }
    }

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        try {
            Repository repository = RepositoryHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_REPOSITORY_NAME));
            String[] applicationNames = repository.getApplicationNames();
            out.println(CorbaResourceUtil.getText("servertool.listappnames2"));
            out.println();
            for (int i = 0; i < applicationNames.length; i++) out.println("\t" + applicationNames[i]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return commandDone;
    }
}

class ShutdownServer implements CommandHandler {

    public String getCommandName() {
        return "shutdown";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.shutdown"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.shutdown1"));
        }
    }

    static final int illegalServerId = -1;

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        int serverId = illegalServerId;
        try {
            if (cmdArgs.length == 2) if (cmdArgs[0].equals("-serverid")) serverId = (Integer.valueOf(cmdArgs[1])).intValue(); else if (cmdArgs[0].equals("-applicationName")) serverId = ServerTool.getServerIdForAlias(orb, cmdArgs[1]);
            if (serverId == illegalServerId) return parseError;
            Activator activator = ActivatorHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_ACTIVATOR_NAME));
            activator.shutdown(serverId);
            out.println(CorbaResourceUtil.getText("servertool.shutdown2"));
        } catch (ServerNotActive ex) {
            out.println(CorbaResourceUtil.getText("servertool.servernotrunning"));
        } catch (ServerNotRegistered ex) {
            out.println(CorbaResourceUtil.getText("servertool.nosuchserver"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return commandDone;
    }
}

class StartServer implements CommandHandler {

    public String getCommandName() {
        return "startup";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.startserver"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.startserver1"));
        }
    }

    static final int illegalServerId = -1;

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        int serverId = illegalServerId;
        try {
            if (cmdArgs.length == 2) if (cmdArgs[0].equals("-serverid")) serverId = (Integer.valueOf(cmdArgs[1])).intValue(); else if (cmdArgs[0].equals("-applicationName")) serverId = ServerTool.getServerIdForAlias(orb, cmdArgs[1]);
            if (serverId == illegalServerId) return parseError;
            Activator activator = ActivatorHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_ACTIVATOR_NAME));
            activator.activate(serverId);
            out.println(CorbaResourceUtil.getText("servertool.startserver2"));
        } catch (ServerNotRegistered ex) {
            out.println(CorbaResourceUtil.getText("servertool.nosuchserver"));
        } catch (ServerAlreadyActive ex) {
            out.println(CorbaResourceUtil.getText("servertool.serverup"));
        } catch (ServerHeldDown ex) {
            out.println(CorbaResourceUtil.getText("servertool.helddown"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return commandDone;
    }
}

class Quit implements CommandHandler {

    public String getCommandName() {
        return "quit";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.quit"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.quit1"));
        }
    }

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        System.exit(0);
        return commandDone;
    }
}

class Help implements CommandHandler {

    public String getCommandName() {
        return "help";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.help"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.help1"));
        }
    }

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        return commandDone;
    }
}

class ListORBs implements CommandHandler {

    public String getCommandName() {
        return "orblist";
    }

    public void printCommandHelp(PrintStream out, boolean helpType) {
        if (helpType == longHelp) {
            out.println(CorbaResourceUtil.getText("servertool.orbidmap"));
        } else {
            out.println(CorbaResourceUtil.getText("servertool.orbidmap1"));
        }
    }

    static final int illegalServerId = -1;

    public boolean processCommand(String[] cmdArgs, ORB orb, PrintStream out) {
        int serverId = illegalServerId;
        try {
            if (cmdArgs.length == 2) {
                if (cmdArgs[0].equals("-serverid")) serverId = (Integer.valueOf(cmdArgs[1])).intValue(); else if (cmdArgs[0].equals("-applicationName")) serverId = ServerTool.getServerIdForAlias(orb, cmdArgs[1]);
            }
            if (serverId == illegalServerId) return parseError;
            Activator activator = ActivatorHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_ACTIVATOR_NAME));
            String[] orbList = activator.getORBNames(serverId);
            out.println(CorbaResourceUtil.getText("servertool.orbidmap2"));
            for (int i = 0; i < orbList.length; i++) {
                out.println("\t " + orbList[i]);
            }
        } catch (ServerNotRegistered ex) {
            out.println("\tno such server found.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return commandDone;
    }
}
