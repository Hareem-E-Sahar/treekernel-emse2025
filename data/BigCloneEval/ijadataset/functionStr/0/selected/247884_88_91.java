public class Test {    @Override
    public String toString() {
        return getType() + " wire " + getChannel() + " (" + wireData.size() + ", " + getLastValue() + ")";
    }
}