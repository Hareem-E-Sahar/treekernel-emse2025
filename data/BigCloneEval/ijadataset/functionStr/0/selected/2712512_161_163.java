public class Test {    public boolean isUsed() {
        return (readcount != 0) || (writecount != 0);
    }
}