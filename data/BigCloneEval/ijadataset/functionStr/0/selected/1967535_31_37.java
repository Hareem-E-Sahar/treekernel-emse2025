public class Test {    protected void writeBody(CDROutputPacket os) {
        this.readerId.write(os);
        this.writerId.write(os);
        this.writerSN.write(os);
        this.fn.write(os);
        this.count.write(os);
    }
}