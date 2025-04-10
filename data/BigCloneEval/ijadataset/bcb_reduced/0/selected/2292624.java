package COM.winserver.wildcat;

import java.io.IOException;

public class TFileRecord extends WcRecord {

    public int Status;

    public int Area;

    public String SFName;

    public String Description;

    public String Password;

    public int FileFlags;

    public int Size;

    public long FileTime;

    public long LastAccessed;

    public boolean NeverOverwrite;

    public boolean NeverDelete;

    public boolean FreeFile;

    public boolean CopyBeforeDownload;

    public boolean Offline;

    public boolean FailedScan;

    public boolean FreeTime;

    public int Downloads;

    public int Cost;

    public TUserInfo Uploader;

    public int UserInfo;

    public boolean HasLongDescription;

    public long PostTime;

    public int PrivateUserId;

    public String Name;

    public static final int SIZE = 0 + 4 + 4 + 16 + 76 + 32 + 4 + 4 + 8 + 8 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + 4 + TUserInfo.SIZE + 4 + 4 + 8 + 4 + 32 * 1 + MAX_PATH + 100 * 1;

    public TFileRecord() {
    }

    public TFileRecord(byte[] x) {
        fromByteArray(x);
    }

    protected void writeTo(WcOutputStream out) throws IOException {
        super.writeTo(out);
        out.writeInt(Status);
        out.writeInt(Area);
        out.writeString(SFName, 16);
        out.writeString(Description, 76);
        out.writeString(Password, 32);
        out.writeInt(FileFlags);
        out.writeInt(Size);
        out.writeLong(FileTime);
        out.writeLong(LastAccessed);
        out.writeBoolean(NeverOverwrite);
        out.writeBoolean(NeverDelete);
        out.writeBoolean(FreeFile);
        out.writeBoolean(CopyBeforeDownload);
        out.writeBoolean(Offline);
        out.writeBoolean(FailedScan);
        out.writeBoolean(FreeTime);
        out.writeInt(Downloads);
        out.writeInt(Cost);
        Uploader.writeTo(out);
        out.writeInt(UserInfo);
        out.writeBoolean(HasLongDescription);
        out.writeLong(PostTime);
        out.writeInt(PrivateUserId);
        out.write(new byte[32 * 1]);
        out.writeString(Name, MAX_PATH);
        out.write(new byte[100 * 1]);
    }

    protected void readFrom(WcInputStream in) throws IOException {
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
