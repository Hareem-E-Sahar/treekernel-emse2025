package COM.winserver.wildcat;

import java.io.IOException;

public class TGuiMsgHeader extends WcRecord {

    public int Id;

    public int Number;

    public TUserInfo From;

    public TUserInfo To;

    public String Subject;

    public long MsgTime;

    public int Reference;

    public int MsgFlags;

    public int MsgSize;

    public int Conference;

    public int PrevUnread;

    public int NextUnread;

    public String Attachment;

    public static final int SIZE = 0 + 4 + 4 + TUserInfo.SIZE + TUserInfo.SIZE + 72 + 8 + 4 + 2 + 4 + 4 + 4 + 4 + 16;

    public TGuiMsgHeader() {
    }

    public TGuiMsgHeader(byte[] x) {
        fromByteArray(x);
    }

    protected void writeTo(WcOutputStream out) throws IOException {
        super.writeTo(out);
        out.writeInt(Id);
        out.writeInt(Number);
        From.writeTo(out);
        To.writeTo(out);
        out.writeString(Subject, 72);
        out.writeLong(MsgTime);
        out.writeInt(Reference);
        out.writeShort(MsgFlags);
        out.writeInt(MsgSize);
        out.writeInt(Conference);
        out.writeInt(PrevUnread);
        out.writeInt(NextUnread);
        out.writeString(Attachment, 16);
    }

    protected void readFrom(WcInputStream in) throws IOException {
        super.readFrom(in);
        Id = in.readInt();
        Number = in.readInt();
        From = new TUserInfo();
        From.readFrom(in);
        To = new TUserInfo();
        To.readFrom(in);
        Subject = in.readString(72);
        MsgTime = in.readLong();
        Reference = in.readInt();
        MsgFlags = in.readUnsignedShort();
        MsgSize = in.readInt();
        Conference = in.readInt();
        PrevUnread = in.readInt();
        NextUnread = in.readInt();
        Attachment = in.readString(16);
    }
}
