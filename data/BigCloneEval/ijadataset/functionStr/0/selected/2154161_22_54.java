public class Test {    public void parse(PsdInputStream stream) throws IOException {
        boolean rle = stream.readShort() == 1;
        short[] lineLengths = null;
        int height = header.getHeight();
        if (rle) {
            int nLines = height * header.getChannelsCount();
            lineLengths = new short[nLines];
            for (int i = 0; i < nLines; i++) {
                lineLengths[i] = stream.readShort();
            }
        }
        for (int channelNumber = 0; channelNumber < header.getChannelsCount(); channelNumber++) {
            int channelId = channelNumber == 3 ? -1 : channelNumber;
            int width = header.getWidth();
            byte[] data = new byte[width * height];
            if (rle) {
                byte[] s = new byte[width * 2];
                int pos = 0;
                int lineIndex = channelNumber * height;
                for (int i = 0; i < height; i++) {
                    int len = lineLengths[lineIndex++];
                    stream.readBytes(s, len);
                    RleLineUncompressor.decodeRleLine(s, 0, len, data, pos);
                    pos += width;
                }
            } else {
                stream.readFully(data);
            }
            if (handler != null) {
                handler.channelLoaded(channelId, data);
            }
        }
    }
}