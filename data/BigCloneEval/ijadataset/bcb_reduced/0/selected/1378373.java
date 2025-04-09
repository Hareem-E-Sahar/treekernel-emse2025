package com.microfly.job.iptools;

import com.microfly.core.Config;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * IpTools �����ѯ QQ IP�������ݿ�
 * <p/>
 * Copyright (c) 2008
 *
 * @author jialin
 * @version 1.0
 */
public class QQIPQuery {

    private final String DBPATH = Config.WEB_ROOT + "/qq/QQWry.Dat";

    private String country, localStr;

    private long IPN;

    private int recordcount, countryflag;

    private long rangE, rangB, offset, startIP, endIP, firstStartIP, lastStartIP, endIPOff;

    private RandomAccessFile fis;

    private byte[] buff;

    public void Query(String ip) throws Exception {
        this.IPN = IPToInt(ip);
        fis = new RandomAccessFile(this.DBPATH, "r");
        buff = new byte[4];
        fis.seek(0);
        fis.read(buff);
        firstStartIP = this.B2L(buff);
        fis.read(buff);
        lastStartIP = this.B2L(buff);
        recordcount = (int) ((lastStartIP - firstStartIP) / 7);
        if (recordcount <= 1) {
            localStr = country = "Unknown";
            return;
        }
        rangB = 0;
        rangE = recordcount;
        long RecNo;
        do {
            RecNo = (rangB + rangE) / 2;
            GetStartIP(RecNo);
            if (IPN == startIP) {
                rangB = RecNo;
                break;
            }
            if (IPN > startIP) rangB = RecNo; else rangE = RecNo;
        } while (rangB < rangE - 1);
        GetStartIP(rangB);
        GetEndIP();
        GetCountry(IPN);
        try {
            fis.close();
        } catch (Exception e) {
        }
    }

    private long B2L(byte[] b) {
        long ret = 0;
        for (int i = 0; i < b.length; i++) {
            long t = 1L;
            for (int j = 0; j < i; j++) t = t * 256L;
            ret += ((b[i] < 0) ? 256 + b[i] : b[i]) * t;
        }
        return ret;
    }

    private long IPToInt(String ip) {
        String[] arr = ip.split("\\.");
        long ret = 0;
        for (int i = 0; i < arr.length; i++) {
            long l = 1;
            for (int j = 0; j < i; j++) l *= 256;
            try {
                ret += Long.parseLong(arr[arr.length - i - 1]) * l;
            } catch (Exception e) {
                ret += 0;
            }
        }
        return ret;
    }

    private String GetFlagStr(long offset) throws IOException {
        int flag = 0;
        do {
            fis.seek(offset);
            buff = new byte[1];
            fis.read(buff);
            flag = (buff[0] < 0) ? 256 + buff[0] : buff[0];
            if (flag == 1 || flag == 2) {
                buff = new byte[3];
                fis.read(buff);
                if (flag == 2) {
                    countryflag = 2;
                    endIPOff = offset - 4;
                }
                offset = this.B2L(buff);
            } else break;
        } while (true);
        if (offset < 12) {
            return "";
        } else {
            fis.seek(offset);
            return GetStr();
        }
    }

    private String GetStr() throws IOException {
        long l = fis.length();
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        byte c = fis.readByte();
        do {
            byteout.write(c);
            c = fis.readByte();
        } while (c != 0 && fis.getFilePointer() < l);
        return byteout.toString();
    }

    private void GetCountry(long ip) throws IOException {
        if (countryflag == 1 || countryflag == 2) {
            country = GetFlagStr(endIPOff + 4);
            if (countryflag == 1) {
                localStr = GetFlagStr(fis.getFilePointer());
                if (IPN >= IPToInt("255.255.255.0") && IPN <= IPToInt("255.255.255.255")) {
                    localStr = GetFlagStr(endIPOff + 21);
                    country = GetFlagStr(endIPOff + 12);
                }
            } else {
                localStr = GetFlagStr(endIPOff + 8);
            }
        } else {
            country = GetFlagStr(endIPOff + 4);
            localStr = GetFlagStr(fis.getFilePointer());
        }
    }

    private long GetEndIP() throws IOException {
        fis.seek(endIPOff);
        buff = new byte[4];
        fis.read(buff);
        endIP = this.B2L(buff);
        buff = new byte[1];
        fis.read(buff);
        countryflag = (buff[0] < 0) ? 256 + buff[0] : buff[0];
        return endIP;
    }

    private long GetStartIP(long RecNo) throws IOException {
        offset = firstStartIP + RecNo * 7;
        fis.seek(offset);
        buff = new byte[4];
        fis.read(buff);
        startIP = this.B2L(buff);
        buff = new byte[3];
        fis.read(buff);
        endIPOff = this.B2L(buff);
        return startIP;
    }

    public String GetLocal() {
        return localStr;
    }

    public String GetCountry() {
        return country;
    }

    public static void main(String[] args) throws Exception {
        QQIPQuery qry = new QQIPQuery();
        qry.Query("213.160.129.158");
        System.out.println(qry.GetCountry() + " " + qry.GetLocal());
    }
}
