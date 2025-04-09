public class Test {    String buildTimeSeriesKey(HomenetHardware hardware, Integer channel) {
        return hardware.getChannelDescription(channel) + " [CH-" + channel + "]";
    }
}