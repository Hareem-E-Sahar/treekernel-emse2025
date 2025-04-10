public class Test {    private void runLongTest(Output write) throws IOException {
        write.writeLong(0);
        write.writeLong(63);
        write.writeLong(64);
        write.writeLong(127);
        write.writeLong(128);
        write.writeLong(8192);
        write.writeLong(16384);
        write.writeLong(2097151);
        write.writeLong(1048575);
        write.writeLong(134217727);
        write.writeLong(268435455);
        write.writeLong(134217728);
        write.writeLong(268435456);
        write.writeLong(-2097151);
        write.writeLong(-1048575);
        write.writeLong(-134217727);
        write.writeLong(-268435455);
        write.writeLong(-134217728);
        write.writeLong(-268435456);
        assertEquals(1, write.writeLong(0, true));
        assertEquals(1, write.writeLong(0, false));
        assertEquals(1, write.writeLong(63, true));
        assertEquals(1, write.writeLong(63, false));
        assertEquals(1, write.writeLong(64, true));
        assertEquals(2, write.writeLong(64, false));
        assertEquals(1, write.writeLong(127, true));
        assertEquals(2, write.writeLong(127, false));
        assertEquals(2, write.writeLong(128, true));
        assertEquals(2, write.writeLong(128, false));
        assertEquals(3, write.writeLong(8191, true));
        assertEquals(3, write.writeLong(8191, false));
        assertEquals(3, write.writeLong(8192, true));
        assertEquals(3, write.writeLong(8192, false));
        assertEquals(3, write.writeLong(16383, true));
        assertEquals(3, write.writeLong(16383, false));
        assertEquals(3, write.writeLong(16384, true));
        assertEquals(3, write.writeLong(16384, false));
        assertEquals(4, write.writeLong(2097151, true));
        assertEquals(4, write.writeLong(2097151, false));
        assertEquals(3, write.writeLong(1048575, true));
        assertEquals(4, write.writeLong(1048575, false));
        assertEquals(4, write.writeLong(134217727, true));
        assertEquals(4, write.writeLong(134217727, false));
        assertEquals(4, write.writeLong(268435455l, true));
        assertEquals(5, write.writeLong(268435455l, false));
        assertEquals(4, write.writeLong(134217728l, true));
        assertEquals(5, write.writeLong(134217728l, false));
        assertEquals(5, write.writeLong(268435456l, true));
        assertEquals(5, write.writeLong(268435456l, false));
        assertEquals(1, write.writeLong(-64, false));
        assertEquals(9, write.writeLong(-64, true));
        assertEquals(2, write.writeLong(-65, false));
        assertEquals(9, write.writeLong(-65, true));
        assertEquals(3, write.writeLong(-8192, false));
        assertEquals(9, write.writeLong(-8192, true));
        assertEquals(4, write.writeLong(-1048576, false));
        assertEquals(9, write.writeLong(-1048576, true));
        assertEquals(4, write.writeLong(-134217728, false));
        assertEquals(9, write.writeLong(-134217728, true));
        assertEquals(5, write.writeLong(-134217729, false));
        assertEquals(9, write.writeLong(-134217729, true));
        Input read = new Input(write.toBytes());
        assertEquals(0, read.readLong());
        assertEquals(63, read.readLong());
        assertEquals(64, read.readLong());
        assertEquals(127, read.readLong());
        assertEquals(128, read.readLong());
        assertEquals(8192, read.readLong());
        assertEquals(16384, read.readLong());
        assertEquals(2097151, read.readLong());
        assertEquals(1048575, read.readLong());
        assertEquals(134217727, read.readLong());
        assertEquals(268435455, read.readLong());
        assertEquals(134217728, read.readLong());
        assertEquals(268435456, read.readLong());
        assertEquals(-2097151, read.readLong());
        assertEquals(-1048575, read.readLong());
        assertEquals(-134217727, read.readLong());
        assertEquals(-268435455, read.readLong());
        assertEquals(-134217728, read.readLong());
        assertEquals(-268435456, read.readLong());
        assertEquals(0, read.readLong(true));
        assertEquals(0, read.readLong(false));
        assertEquals(63, read.readLong(true));
        assertEquals(63, read.readLong(false));
        assertEquals(64, read.readLong(true));
        assertEquals(64, read.readLong(false));
        assertEquals(127, read.readLong(true));
        assertEquals(127, read.readLong(false));
        assertEquals(128, read.readLong(true));
        assertEquals(128, read.readLong(false));
        assertEquals(8191, read.readLong(true));
        assertEquals(8191, read.readLong(false));
        assertEquals(8192, read.readLong(true));
        assertEquals(8192, read.readLong(false));
        assertEquals(16383, read.readLong(true));
        assertEquals(16383, read.readLong(false));
        assertEquals(16384, read.readLong(true));
        assertEquals(16384, read.readLong(false));
        assertEquals(2097151, read.readLong(true));
        assertEquals(2097151, read.readLong(false));
        assertEquals(1048575, read.readLong(true));
        assertEquals(1048575, read.readLong(false));
        assertEquals(134217727, read.readLong(true));
        assertEquals(134217727, read.readLong(false));
        assertEquals(268435455, read.readLong(true));
        assertEquals(268435455, read.readLong(false));
        assertEquals(134217728, read.readLong(true));
        assertEquals(134217728, read.readLong(false));
        assertEquals(268435456, read.readLong(true));
        assertEquals(268435456, read.readLong(false));
        assertEquals(-64, read.readLong(false));
        assertEquals(-64, read.readLong(true));
        assertEquals(-65, read.readLong(false));
        assertEquals(-65, read.readLong(true));
        assertEquals(-8192, read.readLong(false));
        assertEquals(-8192, read.readLong(true));
        assertEquals(-1048576, read.readLong(false));
        assertEquals(-1048576, read.readLong(true));
        assertEquals(-134217728, read.readLong(false));
        assertEquals(-134217728, read.readLong(true));
        assertEquals(-134217729, read.readLong(false));
        assertEquals(-134217729, read.readLong(true));
    }
}