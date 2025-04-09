package com.solidmatrix.regxmaker.util.shared;

import java.io.BufferedReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;

/**
 * <PRE>
 * Name   : com.solidmatrix.regxmaker.util.shared.ConsoleUtils
 * Project: RegXmaker Library
 * Version: 1.1
 * Tier   : N/A (Function Class)
 * Author : Gennadiy Shafranovich
 * Purpose: General utilities for console based applications
 *
 * Copyright (C) 2001, 2004 SolidMatrix Technologies, Inc.
 * This file is part of RegXmaker Library.
 *
 * RegXmaker Library is is free software; you can redistribute it and/or modify
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * RegXmaker library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Comments: Full, with javadoc.
 *
 * Modification History
 *
 * 01-28-2001  GS Created
 * 07-02-2001  GS Added a more general readFull() method to read 
 *                entire contents of a stream. readFullFile now
 *                used this method to read from an underlying
 *                FileInputStream.
 *
 * 07-05-2004 YS Added licensing information
 * </PRE>
 */
public class ConsoleUtils {

    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    private static String line = System.getProperty("line.separator");

    /**
	 * This method could be used to prompt a user with a message and
     * return the user response
	 *
	 * @param   msg  Message string
	 * @return     a String containing the user's response or "" if an exeption occured
	 */
    public static String prompt(String msg) {
        try {
            System.out.print(msg);
            return br.readLine();
        } catch (Exception e) {
            return "";
        }
    }

    /**
	 * This method could be used to read all chars in a file. The
     * chars will be read according to the given encoding scheme or
     * the default one if none is provided. 
	 *
	 * @param   filename  name of file to read
	 * @param   enc  desired character encoding scheme
	 * @return     A String containing the contents of the file or empty String if an error occured while reading the file
	 */
    public static String readFullFile(String filename, String enc) throws IOException {
        return readFull(new FileInputStream(filename), enc);
    }

    /**
	 * This method could be used to read all chars from a stream. The
     * chars will be read according to the given encoding scheme or
     * the default one if none is provided. The chars will be read
     * using a Reader, bytes from the underlying stream will be
     * converted to chars by that reader.
	 *
	 * @param   is stream to read from
	 * @param   enc  desired character encoding scheme
	 * @return     A String containing the contents of the stream or empty String if an error occured
	 */
    public static String readFull(InputStream is, String enc) throws IOException {
        InputStreamReader isr = null;
        if (enc != null && !enc.equals("")) {
            try {
                isr = new InputStreamReader(is, enc);
            } catch (UnsupportedEncodingException e) {
                System.out.println("Character encoding (" + enc + ") is not supported, using default");
                isr = new InputStreamReader(is);
            }
        } else isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        StringBuffer read = new StringBuffer();
        String rd = br.readLine();
        while (rd != null) {
            read.append(rd);
            read.append(line);
            rd = br.readLine();
        }
        br.close();
        return read.toString();
    }

    /**
	 * This method could be used to read the entire contents of a file
     * into a byte array.
	 *
	 * @param   filename name of file to read 
	 * @return     a byte array containing all the bytes in the file or empty byte array if an error occured
	 */
    public static byte[] readFullFileBytes(String filename) {
        try {
            RandomAccessFile raf = new RandomAccessFile(filename, "r");
            byte[] read = new byte[(int) raf.length()];
            raf.readFully(read);
            return read;
        } catch (IOException e) {
            return new byte[0];
        }
    }

    /**
	 * This method could be used to read the entire contents of a stream
     * into a byte array.
	 *
	 * @param   stream stream to read from 
	 * @return     a byte array containing all the bytes in the stream or empty byte array if an error occured
	 */
    public static byte[] readFullBytes(InputStream stream) {
        try {
            BufferedInputStream bis = new BufferedInputStream(stream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean active = true;
            while (active) {
                byte[] r = new byte[1];
                int read = bis.read(r, 0, 1);
                if (read == -1) active = false; else baos.write(r);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    /**
	 * This method counts how many lines an input string would take if
     * it were outputed onto the screen or into a file. Count depends on 
     * the number of line separators in the string.
	 *
	 * @param   o  String to count line for
	 * @return     an int with the number of output lines the string would take
	 */
    public static int countLines(String o) {
        int lines = 0;
        int start = o.indexOf(line);
        while (start >= 0) {
            lines++;
            start = o.indexOf(line, start);
        }
        return lines;
    }

    /**
	 * Use this method to calculate the CRC32 signature of a byte array.
	 *
	 * @param   in  input array of bytes
	 * @return  The CRC32 signature of the array as an int or -1 if in == null or byte array is empty
	 */
    public static int getCrc32asInt(byte[] in) {
        if (in == null || in.length == 0) return -1;
        CRC32 crc = new CRC32();
        crc.update(in);
        return (int) crc.getValue();
    }

    /**
	 * Convert a byte array to a hexidacimal representation as a string.
     * 
	 * @param   data  bytes to be converted
	 * @param   perLine  hex pairs to before line seperator
	 * @param   offset  output offsets
	 * @return     a string represination of the hexadecimal convertion of the byte array
	 */
    public static String toHex(byte[] data, int perLine, boolean offset) {
        int size = data.length * 3;
        int lines = size / perLine;
        if (offset) size += lines * 13;
        size += lines;
        StringBuffer sb = new StringBuffer(size);
        StringBuffer sb1 = new StringBuffer(13);
        for (int i = 0; i < data.length; i += perLine) {
            if (offset) {
                sb1.delete(0, sb1.length());
                String t = Integer.toHexString(i);
                while (t.length() + sb1.length() < 8) sb1.append("0");
                sb1.append(t.toUpperCase());
                sb.append(sb1.toString());
                sb.append(":    ");
            }
            for (int j = i; j < i + perLine && j < data.length; j++) {
                String b = Integer.toHexString(data[j]).toUpperCase();
                if (b.length() == 1) sb.append("0"); else b = b.substring(b.length() - 2, b.length());
                sb.append(b);
                sb.append(" ");
            }
            sb.append(line);
        }
        return sb.toString();
    }
}
