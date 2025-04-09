public class Test {    public String getChannelID(String networkDeviceName) {
        return networkDeviceName.split(":")[1];
    }
}