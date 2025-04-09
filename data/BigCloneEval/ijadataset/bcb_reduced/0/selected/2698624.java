package com.tencent.tendon.convert.json.listeners;

import com.tencent.tendon.convert.json.*;

/**
 *
 * @author nbzhang
 */
public final class JsonBytesListener extends JsonListener<byte[]> {

    private static final char hex[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static final JsonBytesListener instance = new JsonBytesListener();

    private JsonBytesListener() {
    }

    @Override
    public void convertTo(JsonWriter out, byte[] value) {
        if (value == null) {
            out.writeNull();
            return;
        }
        out.write('"');
        out.write(hexTo(value));
        out.write('"');
    }

    @Override
    public byte[] convertFrom(char[] text, int start, int len) {
        if (text == null) return null;
        return hexFrom(text, start, len);
    }

    @Override
    public Class<byte[]> getType() {
        return byte[].class;
    }

    private static byte[] hexFrom(final char[] chars, int start, int len) {
        int size = (len + 1) / 2;
        final byte[] b = new byte[size];
        for (int i = 0; i < size; i++) {
            int n = 0;
            char ch1 = Character.toLowerCase(chars[start + i * 2]);
            char ch2 = Character.toLowerCase(chars[start + i * 2 + 1]);
            int flag = 0;
            for (int j = 0; j < hex.length; j++) {
                if (hex[j] == ch1) {
                    n += j * 0x10;
                    flag++;
                }
                if (hex[j] == ch2) {
                    n += j;
                    flag++;
                }
                if (flag == 2) break;
            }
            b[i] = (byte) n;
        }
        return b;
    }

    private static char[] hexTo(final byte[] bytes) {
        char[] sb = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) {
            sb[index++] = hex[((b >> 4) & 0xF)];
            sb[index++] = hex[(b & 0xF)];
        }
        return sb;
    }
}
