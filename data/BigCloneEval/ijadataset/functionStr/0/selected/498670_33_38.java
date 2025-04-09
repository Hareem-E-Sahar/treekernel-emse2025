public class Test {    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        to = in.readString();
        writerUid = in.readString();
    }
}