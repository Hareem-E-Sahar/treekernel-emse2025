public class Test {    private void runDoubleTest(Output write) throws IOException {
        write.writeDouble(0);
        write.writeDouble(63);
        write.writeDouble(64);
        write.writeDouble(127);
        write.writeDouble(128);
        write.writeDouble(8192);
        write.writeDouble(16384);
        write.writeDouble(32767);
        write.writeDouble(-63);
        write.writeDouble(-64);
        write.writeDouble(-127);
        write.writeDouble(-128);
        write.writeDouble(-8192);
        write.writeDouble(-16384);
        write.writeDouble(-32768);
        assertEquals(1, write.writeDouble(0, 1000, true));
        assertEquals(1, write.writeDouble(0, 1000, false));
        assertEquals(3, write.writeDouble(63, 1000, true));
        assertEquals(3, write.writeDouble(63, 1000, false));
        assertEquals(3, write.writeDouble(64, 1000, true));
        assertEquals(3, write.writeDouble(64, 1000, false));
        assertEquals(3, write.writeDouble(127, 1000, true));
        assertEquals(3, write.writeDouble(127, 1000, false));
        assertEquals(3, write.writeDouble(128, 1000, true));
        assertEquals(3, write.writeDouble(128, 1000, false));
        assertEquals(4, write.writeDouble(8191, 1000, true));
        assertEquals(4, write.writeDouble(8191, 1000, false));
        assertEquals(4, write.writeDouble(8192, 1000, true));
        assertEquals(4, write.writeDouble(8192, 1000, false));
        assertEquals(4, write.writeDouble(16383, 1000, true));
        assertEquals(4, write.writeDouble(16383, 1000, false));
        assertEquals(4, write.writeDouble(16384, 1000, true));
        assertEquals(4, write.writeDouble(16384, 1000, false));
        assertEquals(4, write.writeDouble(32767, 1000, true));
        assertEquals(4, write.writeDouble(32767, 1000, false));
        assertEquals(3, write.writeDouble(-64, 1000, false));
        assertEquals(9, write.writeDouble(-64, 1000, true));
        assertEquals(3, write.writeDouble(-65, 1000, false));
        assertEquals(9, write.writeDouble(-65, 1000, true));
        assertEquals(4, write.writeDouble(-8192, 1000, false));
        assertEquals(9, write.writeDouble(-8192, 1000, true));
        write.writeDouble(1.23456d);
        Input read = new Input(write.toBytes());
        assertEquals(read.readDouble(), 0d);
        assertEquals(read.readDouble(), 63d);
        assertEquals(read.readDouble(), 64d);
        assertEquals(read.readDouble(), 127d);
        assertEquals(read.readDouble(), 128d);
        assertEquals(read.readDouble(), 8192d);
        assertEquals(read.readDouble(), 16384d);
        assertEquals(read.readDouble(), 32767d);
        assertEquals(read.readDouble(), -63d);
        assertEquals(read.readDouble(), -64d);
        assertEquals(read.readDouble(), -127d);
        assertEquals(read.readDouble(), -128d);
        assertEquals(read.readDouble(), -8192d);
        assertEquals(read.readDouble(), -16384d);
        assertEquals(read.readDouble(), -32768d);
        assertEquals(read.readDouble(1000, true), 0d);
        assertEquals(read.readDouble(1000, false), 0d);
        assertEquals(read.readDouble(1000, true), 63d);
        assertEquals(read.readDouble(1000, false), 63d);
        assertEquals(read.readDouble(1000, true), 64d);
        assertEquals(read.readDouble(1000, false), 64d);
        assertEquals(read.readDouble(1000, true), 127d);
        assertEquals(read.readDouble(1000, false), 127d);
        assertEquals(read.readDouble(1000, true), 128d);
        assertEquals(read.readDouble(1000, false), 128d);
        assertEquals(read.readDouble(1000, true), 8191d);
        assertEquals(read.readDouble(1000, false), 8191d);
        assertEquals(read.readDouble(1000, true), 8192d);
        assertEquals(read.readDouble(1000, false), 8192d);
        assertEquals(read.readDouble(1000, true), 16383d);
        assertEquals(read.readDouble(1000, false), 16383d);
        assertEquals(read.readDouble(1000, true), 16384d);
        assertEquals(read.readDouble(1000, false), 16384d);
        assertEquals(read.readDouble(1000, true), 32767d);
        assertEquals(read.readDouble(1000, false), 32767d);
        assertEquals(read.readDouble(1000, false), -64d);
        assertEquals(read.readDouble(1000, true), -64d);
        assertEquals(read.readDouble(1000, false), -65d);
        assertEquals(read.readDouble(1000, true), -65d);
        assertEquals(read.readDouble(1000, false), -8192d);
        assertEquals(read.readDouble(1000, true), -8192d);
        assertEquals(1.23456d, read.readDouble());
    }
}