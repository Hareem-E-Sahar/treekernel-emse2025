package COM.winserver.wildcat;

import java.io.IOException;

public class TMsgHeader extends WcRecord {

    public int Status;

    public int Conference;

    public int Id;

    public int Number;

    public TUserInfo From;

    public TUserInfo To;

    public String Subject;

    public long PostedTimeGMT;

    public long MsgTime;

    public long ReadTime;

    public boolean Private;

    public boolean Received;

    public boolean ReceiptRequested;

    public boolean Deleted;

    public boolean Tagged;

    public int Reference;

    public int ReplyCount;

    public TFidoAddress FidoFrom;

    public TFidoAddress FidoTo;

    public int FidoFlags;

    public int MsgSize;

    public int PrevUnread;

    public int NextUnread;

    public String Network;

    public String Attachment;

    public boolean AllowDisplayMacros;

    public int AddedByUserId;

    public boolean Exported;

    public int MailFlags;

    public int NextAttachment;

    public int ReadCount;

    public static final int SIZE = 0 + 4 + 4 + 4 + 4 + TUserInfo.SIZE + TUserInfo.SIZE + 72 + 8 + 8 + 8 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + TFidoAddress.SIZE + TFidoAddress.SIZE + 4 + 4 + 4 + 4 + 12 + 16 + 4 + 4 + 4 + 4 + 4 + 4 + 112 * 1;

    public TMsgHeader() {
    }

    public TMsgHeader(byte[] x) {
        fromByteArray(x);
    }

    protected void writeTo(WcOutputStream out) throws IOException {
        super.writeTo(out);
        out.writeInt(Status);
        out.writeInt(Conference);
        out.writeInt(Id);
        out.writeInt(Number);
        From.writeTo(out);
        To.writeTo(out);
        out.writeString(Subject, 72);
        out.writeLong(PostedTimeGMT);
        out.writeLong(MsgTime);
        out.writeLong(ReadTime);
        out.writeBoolean(Private);
        out.writeBoolean(Received);
        out.writeBoolean(ReceiptRequested);
        out.writeBoolean(Deleted);
        out.writeBoolean(Tagged);
        out.writeInt(Reference);
        out.writeInt(ReplyCount);
        FidoFrom.writeTo(out);
        FidoTo.writeTo(out);
        out.writeInt(FidoFlags);
        out.writeInt(MsgSize);
        out.writeInt(PrevUnread);
        out.writeInt(NextUnread);
        out.writeString(Network, 12);
        out.writeString(Attachment, 16);
        out.writeBoolean(AllowDisplayMacros);
        out.writeInt(AddedByUserId);
        out.writeBoolean(Exported);
        out.writeInt(MailFlags);
        out.writeInt(NextAttachment);
        out.writeInt(ReadCount);
        out.write(new byte[112 * 1]);
    }

    protected void readFrom(WcInputStream in) throws IOException {
        super.readFrom(in);
        Status = in.readInt();
        Conference = in.readInt();
        Id = in.readInt();
        Number = in.readInt();
        From = new TUserInfo();
        From.readFrom(in);
        To = new TUserInfo();
        To.readFrom(in);
        Subject = in.readString(72);
        PostedTimeGMT = in.readLong();
        MsgTime = in.readLong();
        ReadTime = in.readLong();
        Private = in.readBoolean();
        Received = in.readBoolean();
        ReceiptRequested = in.readBoolean();
        Deleted = in.readBoolean();
        Tagged = in.readBoolean();
        Reference = in.readInt();
        ReplyCount = in.readInt();
        FidoFrom = new TFidoAddress();
        FidoFrom.readFrom(in);
        FidoTo = new TFidoAddress();
        FidoTo.readFrom(in);
        FidoFlags = in.readInt();
        MsgSize = in.readInt();
        PrevUnread = in.readInt();
        NextUnread = in.readInt();
        Network = in.readString(12);
        Attachment = in.readString(16);
        AllowDisplayMacros = in.readBoolean();
        AddedByUserId = in.readInt();
        Exported = in.readBoolean();
        MailFlags = in.readInt();
        NextAttachment = in.readInt();
        ReadCount = in.readInt();
        in.skip(112 * 1);
    }
}
