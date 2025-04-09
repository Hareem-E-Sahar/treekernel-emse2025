package jodd.mail;

import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
	 * 加密解密QQ消息的工具类. QQ消息的加密算法是一个16次的迭代过程，并且是反馈的，每一个加密单元是8字节，输出也是8字节，密钥是16字节
	 * 我们以prePlain表示前一个明文块，plain表示当前明文块，crypt表示当前明文块加密得到的密文块，preCrypt表示前一个密文块
	 * f表示加密算法，d表示解密算法 那么从plain得到crypt的过程是: crypt = f(plain &circ; preCrypt) &circ;
	 * prePlain 所以，从crypt得到plain的过程自然是 plain = d(crypt &circ; prePlain) &circ;
	 * preCrypt 此外，算法有它的填充机制，其会在明文前和明文后分别填充一定的字节数，以保证明文长度是8字节的倍数
	 * 填充的字节数与原始明文长度有关，填充的方法是:
	 * 
	 * <pre>
	 * <code>
	 * 
	 *      ------- 消息填充算法 ----------- 
	 *      a = (明文长度 + 10) mod 8
	 *      if(a 不等于 0) a = 8 - a;
	 *      b = 随机数 &amp; 0xF8 | a;              这个的作用是把a的值保存了下来
	 *      plain[0] = b;         	          然后把b做为明文的第0个字节，这样第0个字节就保存了a的信息，这个信息在解密时就要用来找到真正明文的起始位置
	 *      plain[1 至 a+2] = 随机数 &amp; 0xFF;    这里用随机数填充明文的第1到第a+2个字节
	 *      plain[a+3 至 a+3+明文长度-1] = 明文; 从a+3字节开始才是真正的明文
	 *      plain[a+3+明文长度, 最后] = 0;       在最后，填充0，填充到总长度为8的整数为止。到此为止，结束了，这就是最后得到的要加密的明文内容
	 *      ------- 消息填充算法 ------------
	 *   
	 * </code>
	 * </pre>
	 * 
	 * @author luma
	 * @author notXX
	 */
public class Crypter {

    private byte[] plain;

    private byte[] prePlain;

    private byte[] out;

    private int crypt, preCrypt;

    private int pos;

    private int padding;

    private byte[] key;

    private boolean header = true;

    private int contextStart;

    private static Random random = new Random();

    private ByteArrayOutputStream baos;

    /**
		 * 构造函数
		 */
    public Crypter() {
        baos = new ByteArrayOutputStream(8);
    }

    /**
		 * 把字节数组从offset开始的len个字节转换成一个unsigned int， 因为java里面没有unsigned，所以unsigned
		 * int使用long表示的， 如果len大于8，则认为len等于8。如果len小于8，则高位填0 <br>
		 * (edited by notxx) 改变了算法, 性能稍微好一点. 在我的机器上测试10000次, 原始算法花费18s, 这个算法花费12s.
		 * 
		 * @param in
		 *                   字节数组.
		 * @param offset
		 *                   从哪里开始转换.
		 * @param len
		 *                   转换长度, 如果len超过8则忽略后面的
		 * @return
		 */
    private static long getUnsignedInt(byte[] in, int offset, int len) {
        long ret = 0;
        int end = 0;
        if (len > 8) end = offset + 8; else end = offset + len;
        for (int i = offset; i < end; i++) {
            ret <<= 8;
            ret |= in[i] & 0xff;
        }
        return (ret & 0xffffffffl) | (ret >>> 32);
    }

    /**
	     * 解密
	     * @param in 密文
	     * @param offset 密文开始的位置
	     * @param len 密文长度
	     * @param k 密钥
	     * @return 明文
	     */
    public byte[] decrypt(byte[] in, int offset, int len, byte[] k) {
        if (k == null) return null;
        crypt = preCrypt = 0;
        this.key = k;
        int count;
        byte[] m = new byte[offset + 8];
        if ((len % 8 != 0) || (len < 16)) return null;
        prePlain = decipher(in, offset);
        pos = prePlain[0] & 0x7;
        count = len - pos - 10;
        if (count < 0) return null;
        for (int i = offset; i < m.length; i++) m[i] = 0;
        out = new byte[count];
        preCrypt = 0;
        crypt = 8;
        contextStart = 8;
        pos++;
        padding = 1;
        while (padding <= 2) {
            if (pos < 8) {
                pos++;
                padding++;
            }
            if (pos == 8) {
                m = in;
                if (!decrypt8Bytes(in, offset, len)) return null;
            }
        }
        int i = 0;
        while (count != 0) {
            if (pos < 8) {
                out[i] = (byte) (m[offset + preCrypt + pos] ^ prePlain[pos]);
                i++;
                count--;
                pos++;
            }
            if (pos == 8) {
                m = in;
                preCrypt = crypt - 8;
                if (!decrypt8Bytes(in, offset, len)) return null;
            }
        }
        for (padding = 1; padding < 8; padding++) {
            if (pos < 8) {
                if ((m[offset + preCrypt + pos] ^ prePlain[pos]) != 0) return null;
                pos++;
            }
            if (pos == 8) {
                m = in;
                preCrypt = crypt;
                if (!decrypt8Bytes(in, offset, len)) return null;
            }
        }
        return out;
    }

    /**
	     * @param in
	     *            需要被解密的密文
	     * @param inLen
	     *            密文长度
	     * @param k
	     *            密钥
	     * @return Message 已解密的消息
	     */
    public byte[] decrypt(byte[] in, byte[] k) {
        return decrypt(in, 0, in.length, k);
    }

    /**
	     * 加密
	     * @param in 明文字节数组
	     * @param offset 开始加密的偏移
	     * @param len 加密长度
	     * @param k 密钥
	     * @return 密文字节数组
	     */
    public byte[] encrypt(byte[] in, int offset, int len, byte[] k) {
        if (k == null) return in;
        plain = new byte[8];
        prePlain = new byte[8];
        pos = 1;
        padding = 0;
        crypt = preCrypt = 0;
        this.key = k;
        header = true;
        pos = (len + 0x0A) % 8;
        if (pos != 0) pos = 8 - pos;
        out = new byte[len + pos + 10];
        plain[0] = (byte) ((rand() & 0xF8) | pos);
        for (int i = 1; i <= pos; i++) plain[i] = (byte) (rand() & 0xFF);
        pos++;
        for (int i = 0; i < 8; i++) prePlain[i] = 0x0;
        padding = 1;
        while (padding <= 2) {
            if (pos < 8) {
                plain[pos++] = (byte) (rand() & 0xFF);
                padding++;
            }
            if (pos == 8) encrypt8Bytes();
        }
        int i = offset;
        while (len > 0) {
            if (pos < 8) {
                plain[pos++] = in[i++];
                len--;
            }
            if (pos == 8) encrypt8Bytes();
        }
        padding = 1;
        while (padding <= 7) {
            if (pos < 8) {
                plain[pos++] = 0x0;
                padding++;
            }
            if (pos == 8) encrypt8Bytes();
        }
        return out;
    }

    /**
	     * @param in
	     *            需要加密的明文
	     * @param inLen
	     *            明文长度
	     * @param k
	     *            密钥
	     * @return Message 密文
	     */
    public byte[] encrypt(byte[] in, byte[] k) {
        return encrypt(in, 0, in.length, k);
    }

    /**
	     * 加密一个8字节块
	     * 
	     * @param in
	     * 		明文字节数组
	     * @return
	     * 		密文字节数组
	     */
    private byte[] encipher(byte[] in) {
        int loop = 0x10;
        long y = getUnsignedInt(in, 0, 4);
        long z = getUnsignedInt(in, 4, 4);
        long a = getUnsignedInt(key, 0, 4);
        long b = getUnsignedInt(key, 4, 4);
        long c = getUnsignedInt(key, 8, 4);
        long d = getUnsignedInt(key, 12, 4);
        long sum = 0;
        long delta = 0x9E3779B9;
        delta &= 0xFFFFFFFFL;
        while (loop-- > 0) {
            sum += delta;
            sum &= 0xFFFFFFFFL;
            y += ((z << 4) + a) ^ (z + sum) ^ ((z >>> 5) + b);
            y &= 0xFFFFFFFFL;
            z += ((y << 4) + c) ^ (y + sum) ^ ((y >>> 5) + d);
            z &= 0xFFFFFFFFL;
        }
        baos.reset();
        writeInt((int) y);
        writeInt((int) z);
        return baos.toByteArray();
    }

    /**
	     * 解密从offset开始的8字节密文
	     * 
	     * @param in
	     * 		密文字节数组
	     * @param offset
	     * 		密文开始位置
	     * @return
	     * 		明文
	     */
    private byte[] decipher(byte[] in, int offset) {
        int loop = 0x10;
        long y = getUnsignedInt(in, offset, 4);
        long z = getUnsignedInt(in, offset + 4, 4);
        long a = getUnsignedInt(key, 0, 4);
        long b = getUnsignedInt(key, 4, 4);
        long c = getUnsignedInt(key, 8, 4);
        long d = getUnsignedInt(key, 12, 4);
        long sum = 0xE3779B90;
        sum &= 0xFFFFFFFFL;
        long delta = 0x9E3779B9;
        delta &= 0xFFFFFFFFL;
        while (loop-- > 0) {
            z -= ((y << 4) + c) ^ (y + sum) ^ ((y >>> 5) + d);
            z &= 0xFFFFFFFFL;
            y -= ((z << 4) + a) ^ (z + sum) ^ ((z >>> 5) + b);
            y &= 0xFFFFFFFFL;
            sum -= delta;
            sum &= 0xFFFFFFFFL;
        }
        baos.reset();
        writeInt((int) y);
        writeInt((int) z);
        return baos.toByteArray();
    }

    /**
	     * 写入一个整型到输出流，高字节优先
	     * 
	     * @param t
	     */
    private void writeInt(int t) {
        baos.write(t >>> 24);
        baos.write(t >>> 16);
        baos.write(t >>> 8);
        baos.write(t);
    }

    /**
	     * 解密
	     * 
	     * @param in
	     * 		密文
	     * @return
	     * 		明文
	     */
    private byte[] decipher(byte[] in) {
        return decipher(in, 0);
    }

    /**
	     * 加密8字节 
	     */
    private void encrypt8Bytes() {
        for (pos = 0; pos < 8; pos++) {
            if (header) plain[pos] ^= prePlain[pos]; else plain[pos] ^= out[preCrypt + pos];
        }
        byte[] crypted = encipher(plain);
        System.arraycopy(crypted, 0, out, crypt, 8);
        for (pos = 0; pos < 8; pos++) out[crypt + pos] ^= prePlain[pos];
        System.arraycopy(plain, 0, prePlain, 0, 8);
        preCrypt = crypt;
        crypt += 8;
        pos = 0;
        header = false;
    }

    /**
	     * 解密8个字节
	     * 
	     * @param in
	     * 		密文字节数组
	     * @param offset
	     * 		从何处开始解密
	     * @param len
	     * 		密文的长度
	     * @return
	     * 		true表示解密成功
	     */
    private boolean decrypt8Bytes(byte[] in, int offset, int len) {
        for (pos = 0; pos < 8; pos++) {
            if (contextStart + pos >= len) return true;
            prePlain[pos] ^= in[offset + crypt + pos];
        }
        prePlain = decipher(prePlain);
        if (prePlain == null) return false;
        contextStart += 8;
        crypt += 8;
        pos = 0;
        return true;
    }

    /**
	     * 这是个随机因子产生器，用来填充头部的，如果为了调试，可以用一个固定值
	     * 随机因子可以使相同的明文每次加密出来的密文都不一样
	     * 
	     * @return
	     * 		随机因子
	     */
    private int rand() {
        return random.nextInt();
    }
}
