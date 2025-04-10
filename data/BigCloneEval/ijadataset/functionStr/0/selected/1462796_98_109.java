public class Test {    @Test
    public void encodeReadData() throws IOException {
        byte[] qualities = new byte[] { 20, 30, 40, 35 };
        short[] values = new short[] { 100, 8, 97, 4, 200 };
        byte[] indexes = new byte[] { 1, 2, 2, 0 };
        String bases = "TATT";
        DefaultSFFReadData readData = new DefaultSFFReadData(bases, indexes, values, qualities);
        byte[] expected = encodeExpectedReadData(readData);
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        SffWriter.writeReadData(readData, actual);
        assertArrayEquals(expected, actual.toByteArray());
    }
}