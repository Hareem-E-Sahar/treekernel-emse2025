public class Test {    int getChannel() {
        return (getStatus() & 0x0F);
    }
}