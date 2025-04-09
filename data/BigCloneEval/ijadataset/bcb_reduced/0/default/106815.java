import java.util.zip.*;

class LCheckSum {

    public static long compute(String str) {
        CRC32 crc = new CRC32();
        Adler32 adler = new Adler32();
        crc.update(str.getBytes());
        adler.update(str.getBytes());
        long dg1 = crc.getValue();
        long dg2 = adler.getValue();
        crc.update(String.valueOf(dg2).getBytes());
        adler.update(String.valueOf(dg1).getBytes());
        long d3 = crc.getValue();
        long d4 = adler.getValue();
        dg1 ^= d4;
        dg2 ^= d3;
        return (dg2 ^ ((dg1 >>> 32) | (dg1 << 32)));
    }

    public static void main(String[] args) {
        System.out.println("a:          " + LCheckSum.compute(new String("a")));
        System.out.println("b:          " + LCheckSum.compute(new String("b")));
        System.out.println("moose:      " + LCheckSum.compute(new String("moose")));
        System.out.println("mouse:      " + LCheckSum.compute(new String("mouse")));
        System.out.println("house:      " + LCheckSum.compute(new String("house")));
        System.out.println("louse:      " + LCheckSum.compute(new String("louse")));
        System.out.println("lousy:      " + LCheckSum.compute(new String("lousy")));
        System.out.println("pink floyd: " + LCheckSum.compute(new String("pink floyd")));
    }
}
