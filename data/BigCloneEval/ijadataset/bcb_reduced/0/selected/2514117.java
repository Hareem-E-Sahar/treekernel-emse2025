package COM.winserver.wildcat;

import java.io.IOException;

public class TGetVolatileConfInfo_Response extends TWildcatRequest {

    public int Conf;

    public int HiMsg;

    public int HiMsgId;

    public int LoMsg;

    public int LoMsgId;

    public int LastRead;

    public int FirstUnread;

    public static final int SIZE = TWildcatRequest.SIZE + 4 + 4 + 4 + 4 + 4 + 4 + 4;

    public TGetVolatileConfInfo_Response() {
    }

    public TGetVolatileConfInfo_Response(byte[] x) {
        fromByteArray(x);
    }

    protected void writeTo(WcOutputStream out) throws IOException {
        super.writeTo(out);
        out.writeInt(Conf);
        out.writeInt(HiMsg);
        out.writeInt(HiMsgId);
        out.writeInt(LoMsg);
        out.writeInt(LoMsgId);
        out.writeInt(LastRead);
        out.writeInt(FirstUnread);
    }

    protected void readFrom(WcInputStream in) throws IOException {
        super.readFrom(in);
        Conf = in.readInt();
        HiMsg = in.readInt();
        HiMsgId = in.readInt();
        LoMsg = in.readInt();
        LoMsgId = in.readInt();
        LastRead = in.readInt();
        FirstUnread = in.readInt();
    }
}
