public class Test {    public ChannelManager(ServerFacilities serverFacilities) {
        this.serverFacilities = serverFacilities;
        initChannelsFile(serverFacilities.getJNerveConfiguration().getChannelsFilePath());
    }
}