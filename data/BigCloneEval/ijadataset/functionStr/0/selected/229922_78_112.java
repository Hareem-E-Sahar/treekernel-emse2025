public class Test {    protected void writeTo(WcOutputStream out) throws IOException {
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
}