public class Test {    public int getChannel() {
        return packedMsg & 0x0F;
    }
}