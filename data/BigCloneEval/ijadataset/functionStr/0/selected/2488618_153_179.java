public class Test {    public List<IPEntry> getIPEntries(String s) {
        List<IPEntry> ret = new ArrayList<IPEntry>();
        try {
            if (mbb == null) {
                FileChannel fc = ipFile.getChannel();
                mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, ipFile.length());
                mbb.order(ByteOrder.LITTLE_ENDIAN);
            }
            int endOffset = (int) ipEnd;
            for (int offset = (int) ipBegin + 4; offset <= endOffset; offset += IP_RECORD_LENGTH) {
                int temp = readInt3(offset);
                if (temp != -1) {
                    IPLocation loc = getIPLocation(temp);
                    IPEntry entry = new IPEntry();
                    entry.country = loc.country;
                    entry.area = loc.area;
                    readIP(offset - 4, b4);
                    entry.beginIp = IPUtils.getIpStringFromBytes(b4);
                    readIP(temp, b4);
                    entry.endIp = IPUtils.getIpStringFromBytes(b4);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return ret;
    }
}