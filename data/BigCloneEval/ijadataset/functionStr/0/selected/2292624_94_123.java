public class Test {    protected void readFrom(WcInputStream in) throws IOException {
        super.readFrom(in);
        Status = in.readInt();
        Area = in.readInt();
        SFName = in.readString(16);
        Description = in.readString(76);
        Password = in.readString(32);
        FileFlags = in.readInt();
        Size = in.readInt();
        FileTime = in.readLong();
        LastAccessed = in.readLong();
        NeverOverwrite = in.readBoolean();
        NeverDelete = in.readBoolean();
        FreeFile = in.readBoolean();
        CopyBeforeDownload = in.readBoolean();
        Offline = in.readBoolean();
        FailedScan = in.readBoolean();
        FreeTime = in.readBoolean();
        Downloads = in.readInt();
        Cost = in.readInt();
        Uploader = new TUserInfo();
        Uploader.readFrom(in);
        UserInfo = in.readInt();
        HasLongDescription = in.readBoolean();
        PostTime = in.readLong();
        PrivateUserId = in.readInt();
        in.skip(32 * 1);
        Name = in.readString(MAX_PATH);
        in.skip(100 * 1);
    }
}