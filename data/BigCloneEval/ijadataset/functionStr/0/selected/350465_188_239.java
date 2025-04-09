public class Test {    @Override
    public boolean hostServer(String name, String advPath, int capacity, int port) {
        try {
            serverName = name;
            maxCapacity = capacity;
            serverPort = port;
            server = new ServerSocket(serverPort);
            exec = Executors.newFixedThreadPool(maxCapacity + 1);
            adv = filehandling.Opening.readFromFile(advPath);
        } catch (IOException e) {
            return false;
        }
        if (adv != null) {
            players = new PlayerManagerImp(netprefs.getPlayerFilePath(), adv);
            ((AdventureEdit) adv).setPlayerManager(players);
            ((AdventureEdit) adv).setBroadcaster(broadcaster);
            advInp = new AdventureInputImp(adv);
            setupTicker();
            if (((AdventureEdit) adv).ready()) {
                terminal.writeTo("\nAdventure file read, starting server...\n");
                terminal.writeTo(Colour.colourise("\nWITNA Server v" + version, Colour.green));
                terminal.writeTo(Colour.colourise("\n-----", Colour.white));
                terminal.writeTo(Colour.colourise("\n-", Colour.white) + Colour.colourise("Server      : ", Colour.green) + Colour.colourise(serverName + "/" + adv.getName() + " v" + adv.getVersion(), Colour.yellow));
                terminal.writeTo(Colour.colourise("\n-", Colour.white) + Colour.colourise("Global ip   : ", Colour.green));
                final int serverPort = port;
                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        String globalip = getGlobalIP();
                        String localip = getLocalIP();
                        terminal.writeTo(Colour.colourise(globalip + ":" + serverPort, Colour.yellow));
                        terminal.writeTo(Colour.colourise("\n-", Colour.white) + Colour.colourise("Local  ip   : ", Colour.green) + Colour.colourise(localip + ":" + serverPort, Colour.yellow));
                        terminal.writeTo(Colour.colourise("\n-", Colour.white) + Colour.colourise("Max capacity: ", Colour.green) + Colour.colourise(maxCapacity + "", Colour.yellow));
                        String status = getNetStatus(globalip, localip);
                        terminal.writeTo(Colour.colourise("\n-", Colour.white) + Colour.colourise("Connection  : ", Colour.green) + Colour.colourise(status, Colour.yellow) + Colour.colourise("\n", Colour.grey));
                        advHelp = new AdventureHelp(terminal);
                        ((AdventureEdit) adv).setHelpFile(advHelp);
                    }
                };
                new Thread(task).start();
                hostingServer = true;
                return true;
            } else {
                terminal.writeTo("\nInternal error: PlayerManager or Broadcaster not yet set!");
                return true;
            }
        } else {
            terminal.writeTo("\nAdventure file not found. Cancelling serverhost..");
            return false;
        }
    }
}