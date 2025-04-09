package jnewsdruid;

import java.io.*;
import java.util.zip.CRC32;
import java.util.*;

/**
 * <p>Überschrift: </p>
 * <p>Beschreibung: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organisation: </p>
 * @author unbekannt
 * @version 1.0
 */
public class decode {

    public static boolean decodeFile(String sourceFile) {
        boolean decoded = false;
        try {
            BufferedReader in;
            FileOutputStream out;
            File source = new File(sourceFile);
            in = new BufferedReader(new FileReader(source));
            File dest;
            String line;
            while ((line = in.readLine()) != null) {
                if (line.length() >= 5) {
                    if (line.length() >= 7 && line.substring(0, 7).equals("=ybegin")) {
                        JNewsDruidFrame.appendlog("Yenc gefunden: " + sourceFile);
                        String fileNameOut = parseForName(line, "name");
                        decoded = YEncDecode(sourceFile, fileNameOut, line);
                    }
                    if (line.substring(0, 5).equals("begin")) {
                        JNewsDruidFrame.appendlog("UUEncoding gefunden: " + sourceFile);
                        StringTokenizer strtok = new StringTokenizer(line, " ");
                        strtok.nextToken();
                        strtok.nextToken();
                        String fileName = strtok.nextToken();
                        dest = new File(fileName);
                        dest.createNewFile();
                        JNewsDruidFrame.appendlog(fileName);
                        out = new FileOutputStream(dest);
                        UUDecode(in, out);
                        System.out.print("here");
                        if (dest.length() == 0) {
                            decoded = false;
                            JNewsDruidFrame.appendlog("UUDecode Error: " + fileName + ":  Dateigr��e ist 0!");
                        } else {
                            JNewsDruidFrame.appendlog("UUDecode: " + fileName + ": decodiert ;-)");
                            decoded = true;
                        }
                    } else if (line.indexOf("Content-Transfer-Encoding: base64") >= 0) {
                        while ((line = in.readLine()) != null) {
                            if (line.indexOf("filename=") >= 0) {
                                int begin = line.indexOf("filename=\"") + 10;
                                int end = line.indexOf("\"", begin);
                                if (begin > 0 && end > 0) {
                                    String fileName = line.substring(begin, end);
                                    line = in.readLine();
                                    StringBuffer strBuffer = new StringBuffer();
                                    line = in.readLine();
                                    while ((line != null) && !line.startsWith("----------")) {
                                        strBuffer.append(line);
                                        line = in.readLine();
                                    }
                                    sun.misc.BASE64Decoder base64Decoder = new sun.misc.BASE64Decoder();
                                    InputStream in64 = new ByteArrayInputStream(strBuffer.toString().getBytes());
                                    out = new FileOutputStream(fileName, false);
                                    base64Decoder.decodeBuffer(in64, out);
                                    in64.close();
                                    out.close();
                                    JNewsDruidFrame.appendlog("Base64: " + fileName + ": decodiert ;-)");
                                    decoded = true;
                                } else {
                                    JNewsDruidFrame.appendlog("Error: Base64-Encoding Korrupt");
                                }
                            }
                        }
                    }
                }
            }
            if (decoded == false) {
                JNewsDruidFrame.appendlog("Error: " + sourceFile + " konnte nicht decodiert werden :-(");
            }
            in.close();
        } catch (IOException io) {
            io.printStackTrace();
            JNewsDruidFrame.viewException(io);
        } catch (Exception e) {
            e.printStackTrace();
            JNewsDruidFrame.viewException(e);
        }
        return decoded;
    }

    public static String parseForName(String line, String param) {
        int indexStart = line.indexOf(param + "=");
        int indexEnd = line.indexOf(" ", indexStart);
        if (indexEnd == -1) {
            indexEnd = line.length();
        }
        if (indexStart > -1) return line.substring(indexStart + param.length() + 1, indexEnd);
        return "";
    }

    public static boolean YEncDecode(String fileNameIn, String fileNameOut, String yencheader) throws IOException {
        RandomAccessFile in = new RandomAccessFile(fileNameIn, "r");
        RandomAccessFile out = new RandomAccessFile(fileNameOut, "rw");
        String line = in.readLine();
        while (line != null && !line.startsWith("=ybegin ")) {
            line = in.readLine();
        }
        if (line == null) {
            in.close();
            throw new IOException("yEnc: " + fileNameOut + ": unexpected end of file");
        }
        int lineLength = Integer.parseInt(parseForName(line, "line"));
        long totalSize = Long.parseLong(parseForName(line, "size"));
        String fileCRC32 = "";
        int partSize;
        boolean success = true;
        String partNo = parseForName(line, "part");
        System.out.println(partNo);
        System.out.println("Line length: " + lineLength + ", total size: " + totalSize);
        if (partNo.equals("") == false && partNo != null) {
            System.out.println("multipart");
            while (line != null && !line.startsWith("=ypart")) {
                line = in.readLine();
            }
            if (line == null) {
                return false;
            }
            long begin = Long.parseLong(parseForName(line, "begin")) - 1;
            if (out.length() < begin) out.setLength(begin - 1);
            out.seek(begin);
            long end = Long.parseLong(parseForName(line, "end"));
            partSize = (int) (end - begin);
        } else {
            out.setLength(0);
            partSize = (int) totalSize;
        }
        boolean special = false;
        System.out.println("Line length: " + lineLength + ", part size: " + partSize);
        byte[] bufferIn = new byte[lineLength + 1];
        byte[] bufferOut = new byte[partSize];
        int byteCount = 0;
        lineLength = in.read(bufferIn);
        while (lineLength != -1 && byteCount < partSize) {
            for (int i = 0; i < lineLength; i++) {
                if (bufferIn[i] == '=') {
                    special = true;
                } else if (bufferIn[i] == 13) {
                    Thread.yield();
                    break;
                } else {
                    if (special) {
                        bufferIn[i] -= 106;
                    } else {
                        bufferIn[i] -= 42;
                    }
                    if (bufferIn[i] < 0) {
                        bufferIn[i] += 256;
                    }
                    bufferOut[byteCount] = bufferIn[i];
                    byteCount++;
                    special = false;
                }
            }
            if (byteCount == partSize) {
                break;
            }
            while (in.readByte() != 10) {
            }
            lineLength = in.read(bufferIn);
            System.out.println(new String(bufferIn));
        }
        System.out.println("last: " + (new String(bufferIn)));
        if (bufferIn[0] == '=' && bufferIn[1] == 'y' && bufferIn[2] == 'e' && bufferIn[3] == 'n' && bufferIn[4] == 'd') {
            System.out.println("yend");
            line = new String(bufferIn);
            fileCRC32 = parseForName(line, "crc32");
        }
        out.write(bufferOut);
        bufferIn = null;
        bufferOut = null;
        in.close();
        out.close();
        if (!fileCRC32.equals("")) {
            byte[] fileBuffer = new byte[(int) totalSize];
            RandomAccessFile fileCheck = new RandomAccessFile(fileNameOut, "r");
            fileCheck.readFully(fileBuffer);
            fileCheck.close();
            CRC32 crc = new CRC32();
            crc.update(fileBuffer);
            if (fileCRC32.equals(Long.toHexString(crc.getValue()).toUpperCase())) {
                System.out.println("yEnc: " + fileNameOut + " CRC32 OK");
            } else {
                success = false;
                System.out.println("Failed to confirm correctly decode " + fileNameOut + ": CRC32 not OK");
            }
        }
        System.out.println("yEnc: " + fileNameOut + ": decoding completed");
        return success;
    }

    public static void UUDecode(BufferedReader in, FileOutputStream out) throws IOException {
        String line = in.readLine();
        int written, a, b, c;
        while (line != null && line.charAt(0) != '`' && line.charAt(0) != ' ' && line.charAt(0) != 'e') {
            written = 0;
            for (int atom = 0; atom < (line.length() - 1) / 4; atom++) {
                byte[] buf = new byte[4];
                for (int i = 0; i < 4; i++) {
                    buf[i] = (byte) ((line.charAt(atom * 4 + i + 1) - ' ') & 0x3f);
                }
                a = ((buf[0] << 2) & 0xfc) | ((buf[1] >>> 4) & 3);
                b = ((buf[1] << 4) & 0xf0) | ((buf[2] >>> 2) & 0xf);
                c = ((buf[2] << 6) & 0xc0) | (buf[3] & 0x3f);
                if (line.charAt(0) == 'M') {
                    out.write((byte) (a & 0xff));
                    out.write((byte) (b & 0xff));
                    out.write((byte) (c & 0xff));
                } else {
                    int toWrite = (line.charAt(0) & 0xff) - 32;
                    if (toWrite - written > 0) {
                        out.write((byte) (a & 0xff));
                        written++;
                    }
                    if (toWrite - written > 0) {
                        out.write((byte) (b & 0xff));
                        written++;
                    }
                    if (toWrite - written > 0) {
                        out.write((byte) (c & 0xff));
                        written++;
                    }
                }
            }
            line = in.readLine();
            while (line != null && line.equals("") == true) {
                line = in.readLine();
            }
        }
        out.close();
    }

    public static void main(String[] args) {
        decodeFile("11.txt");
    }
}
