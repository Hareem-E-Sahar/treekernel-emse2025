package jvc.util.compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZIPString {

    /** 
	* ѹ���ַ�Ϊ byte[] 
	* �������ʹ��new sun.misc.BASE64Encoder().encodeBuffer(byte[] b)���� 
	* ����Ϊ�ַ� 
	* 
	* @param str ѹ��ǰ���ı� 
	* @return 
	*/
    public static void ZipString(StringBuffer data, String FileName, OutputStream os) throws IOException {
        StringBuffer sb[] = { data };
        String st[] = { FileName };
        ZipString(sb, st, os);
    }

    public static void ZipString(StringBuffer[] data, String[] FileName, OutputStream os) throws IOException {
        ZipOutputStream tempZStream = null;
        ZipEntry tempEntry = null;
        tempZStream = new ZipOutputStream(os);
        for (int i = 0; i < data.length; i++) {
            tempEntry = new ZipEntry(FileName[i]);
            tempEntry.setMethod(ZipEntry.DEFLATED);
            tempEntry.setSize((long) data[i].toString().getBytes().length);
            tempZStream.putNextEntry(tempEntry);
            tempZStream.write(data[i].toString().getBytes(), 0, data[i].toString().getBytes().length);
        }
        tempZStream.flush();
        os.flush();
        tempZStream.close();
    }

    public static final byte[] compress(String str) {
        if (str == null) return null;
        byte[] compressed;
        ByteArrayOutputStream out = null;
        ZipOutputStream zout = null;
        try {
            out = new ByteArrayOutputStream();
            zout = new ZipOutputStream(out);
            ZipEntry zipentry = new ZipEntry("0.txt");
            zipentry.setMethod(ZipEntry.STORED);
            zipentry.setSize(str.getBytes().length);
            CRC32 crc32 = new CRC32();
            crc32.update(str.getBytes(), 0, str.getBytes().length);
            zipentry.setCrc(crc32.getValue());
            zout.putNextEntry(zipentry);
            zout.write(str.getBytes());
            zout.closeEntry();
            compressed = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            compressed = null;
        } finally {
            if (zout != null) {
                try {
                    zout.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return compressed;
    }

    /** 
	* ��ѹ����� byte[] ��ݽ�ѹ�� 
	* 
	* @param compressed ѹ����� byte[] ��� 
	* @return ��ѹ����ַ� 
	*/
    public static final String decompress(byte[] compressed) {
        if (compressed == null) return null;
        ByteArrayOutputStream out = null;
        ByteArrayInputStream in = null;
        ZipInputStream zin = null;
        String decompressed;
        try {
            out = new ByteArrayOutputStream();
            in = new ByteArrayInputStream(compressed);
            zin = new ZipInputStream(in);
            byte[] buffer = new byte[1024];
            int offset = -1;
            while ((offset = zin.read(buffer)) != -1) {
                out.write(buffer, 0, offset);
            }
            decompressed = out.toString();
        } catch (IOException e) {
            decompressed = null;
        } finally {
            if (zin != null) {
                try {
                    zin.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return decompressed;
    }

    public static void main(String args[]) throws Exception {
        System.out.println("abcdeadasfa23423423432423423".getBytes().length);
    }
}
