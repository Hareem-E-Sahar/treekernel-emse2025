public class Test {    public BBeBOutputStream(File fn, int xor, int root, int num) throws IOException {
        fcha = new FileInputStream(fn).getChannel();
        mbb = fcha.map(MapMode.READ_WRITE, 0, 0);
        mbb.order(ByteOrder.LITTLE_ENDIAN);
        mbb.put((byte) 'L');
        mbb.put((byte) 0);
        mbb.put((byte) 'R');
        mbb.put((byte) 0);
        mbb.put((byte) 'F');
        mbb.put((byte) 0);
        mbb.put((byte) 0);
        mbb.put((byte) 0);
        mbb.putShort((short) 999);
        mbb.putShort((short) xor);
        mbb.putInt(root);
        mbb.putLong(num);
        mbb.putLong(0);
        mbb.putInt(0);
        mbb.put((byte) 1);
        mbb.put((byte) 0);
        mbb.putShort((short) 170);
        mbb.putShort((short) 0);
        mbb.putShort((short) 600);
        mbb.putShort((short) 800);
        mbb.put((byte) 24);
        mbb.put((byte) 0);
        for (int i = 0; i < 20; i++) mbb.put((byte) 0);
        mbb.putInt(0);
        mbb.putInt(0);
        mbb.putShort((short) 0);
        mbb.putShort((short) 0);
        mbb.putInt(0);
        mbb.putInt(0);
    }
}