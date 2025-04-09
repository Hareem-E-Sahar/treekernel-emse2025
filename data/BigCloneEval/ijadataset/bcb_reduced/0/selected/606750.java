package org.activision.util.rs2cache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import org.activision.util.rs2cache.bzip.BZ2Decompressor;
import org.activision.util.rs2cache.bzip.GZIPDecompressor;

public class CacheContainer {

    private byte[] bytes;

    private int raw_size;

    private byte compression;

    private int real_size;

    public CacheContainer(byte[] bytes) throws IOException {
        RSInputStream hdr_stream = new RSInputStream(new ByteArrayInputStream(bytes));
        this.bytes = bytes;
        compression = hdr_stream.readByte();
        raw_size = hdr_stream.readInt();
        if (compression != 0) {
            real_size = hdr_stream.readInt();
        } else {
            real_size = raw_size;
        }
    }

    public byte[] decompress() throws Exception {
        byte[] data = new byte[real_size];
        switch(compression) {
            case 0:
                System.arraycopy(bytes, 5, data, 0, real_size);
                break;
            case 1:
                BZ2Decompressor.decompress(raw_size, 9, bytes, data);
                break;
            default:
                GZIPDecompressor.decompress(raw_size, 9, bytes, data);
                break;
        }
        return data;
    }

    public boolean check_crc32_rawdata(int crc32) {
        CRC32 cc = new CRC32();
        cc.reset();
        cc.update(bytes);
        return cc.getValue() == crc32;
    }

    public long calc_crc32() {
        CRC32 cc = new CRC32();
        cc.reset();
        cc.update(bytes);
        return cc.getValue();
    }
}
