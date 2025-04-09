public class Test {    
public Channel getVC(int col) {
        return getChannel(vChannels, col, false);
    }
      private Channel getChannel(List<Channel> channels, int col, boolean fallback) {
        if (channels == null || col < 0 || col >= channels.size()) {
            return fallback ? getDefaultChannel() : null;
        }
        return channels.get(col);
    }

    private Channel getDefaultChannel() {
        return new Channel("Default");
    }
}


