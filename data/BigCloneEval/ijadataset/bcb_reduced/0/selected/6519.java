package org.qsari.effectopedia.history;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.CRC32;
import org.jdom.Element;
import org.jdom.Namespace;
import org.qsari.effectopedia.base.EffectopediaObject;
import org.qsari.effectopedia.base.XMLExportable;
import org.qsari.effectopedia.base.XMLImportable;
import org.qsari.effectopedia.core.Effectopedia;
import org.qsari.effectopedia.gui.util.HexCharByteConverter;

public class SourceID implements XMLImportable, XMLExportable {

    private static byte[] getMAC() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                return (mac.length != 0) ? mac : DEFAULT_MAC;
            }
        } catch (Exception e) {
            return DEFAULT_MAC;
        }
        return DEFAULT_MAC;
    }

    private byte[] getIP() {
        try {
            byte[] ip = InetAddress.getLocalHost().getAddress();
            if ((ip != null) || (mac.length != 0)) return ip;
        } catch (UnknownHostException e) {
            return DEFAULT_IP;
        }
        return DEFAULT_IP;
    }

    private long getSessionID() {
        return 1324829848126L - (new Date()).getTime();
    }

    public boolean isValid(String sourceID) {
        return isValid(sourceID.getBytes());
    }

    public boolean isValid(byte[] bytes) {
        ByteArrayInputStream sourceID = new ByteArrayInputStream(bytes);
        DataInputStream sourceIDStream = new DataInputStream(sourceID);
        byte[] content = new byte[bytes.length - Long.SIZE];
        long storedCRC;
        long contentCRC;
        try {
            sourceIDStream.read(content, Long.SIZE, bytes.length - Long.SIZE);
            CRC32 crc32 = new CRC32();
            crc32.update(content);
            storedCRC = sourceIDStream.readLong();
            contentCRC = crc32.getValue();
            sourceIDStream.close();
        } catch (IOException e) {
            return false;
        }
        return contentCRC == storedCRC;
    }

    public boolean setSourceIDFromByteArray(byte[] bytes) {
        if (bytes == null) return false;
        ByteArrayInputStream sourceID = new ByteArrayInputStream(bytes);
        DataInputStream sourceIDStream = new DataInputStream(sourceID);
        byte[] content = new byte[bytes.length - (Long.SIZE >> 3)];
        long storedCRC;
        long contentCRC;
        try {
            sourceIDStream.read(content, 0, content.length);
            CRC32 crc32 = new CRC32();
            crc32.update(content);
            storedCRC = sourceIDStream.readLong();
            contentCRC = crc32.getValue();
            sourceIDStream.close();
            if (contentCRC == storedCRC) {
                sourceID = new ByteArrayInputStream(content);
                sourceIDStream = new DataInputStream(sourceID);
                sourceIDStream.read(mac, 0, 6);
                sourceIDStream.read(ip, 0, 4);
                userId = sourceIDStream.readLong();
                date = new Date(sourceIDStream.readLong());
                revision = sourceIDStream.readLong();
                ID = sourceIDStream.readLong();
                sessionId = sourceIDStream.readLong();
                crc = contentCRC;
            }
        } catch (IOException e) {
            return false;
        }
        return contentCRC == storedCRC;
    }

    public void setSourceIDFromString(String SourceID) {
        setSourceIDFromByteArray(SourceID.getBytes());
    }

    public void setSourceIDFromHEXString(String SourceID) {
        setSourceIDFromByteArray(HexCharByteConverter.convert(SourceID.toCharArray()));
    }

    public String getSourceIDAsString() {
        byte[] sourceId = getSourceIDAsByteArray();
        if (sourceId == null) return null; else return sourceId.toString();
    }

    public String getSourceIDAsHEXString() {
        byte[] sourceId = getSourceIDAsByteArray();
        if (sourceId == null) return null; else {
            char[] chars = HexCharByteConverter.convert(sourceId);
            return new String(chars);
        }
    }

    public byte[] getSourceIDAsByteArray() {
        ByteArrayOutputStream sourceID = new ByteArrayOutputStream(DEFAULT_MAC.length + DEFAULT_IP.length + (Long.SIZE >> 3) * 6);
        DataOutputStream sourceIDStream = new DataOutputStream(sourceID);
        if (!updateStream(sourceIDStream)) return null;
        crc = getCRC(sourceID.toByteArray());
        try {
            sourceIDStream.writeLong(crc);
            sourceIDStream.flush();
        } catch (IOException e) {
            return null;
        }
        return sourceID.toByteArray();
    }

    public SourceID newID() {
        if (ID != 0) {
            ID = -SourceIDs++;
        }
        return this;
    }

    public void loadFromXMLElement(Element element, Namespace namespace) {
        if (element != null) setSourceIDFromHEXString(element.getText());
    }

    public Element storeToXMLElement(Element element, Namespace namespace, boolean visualAttributes) {
        if (element != null) element.addContent(getSourceIDAsHEXString());
        return element;
    }

    public SourceID setIfDefault(EffectopediaObject defaultObject) {
        if ((defaultObject != null) & (defaultObject.getDefaultID() != EffectopediaObject.NON_DEFAULT)) {
            mac = DEFAULT_MAC;
            ip = DEFAULT_IP;
            revision = 0L;
            date = new Date(0L);
            userId = 0L;
            sessionId = 0L;
            ID = defaultObject.getDefaultID();
            ByteArrayOutputStream sourceID = new ByteArrayOutputStream(DEFAULT_MAC.length + DEFAULT_IP.length + (Long.SIZE >> 3) * 6);
            DataOutputStream sourceIDStream = new DataOutputStream(sourceID);
            if (updateStream(sourceIDStream)) crc = getCRC(sourceID.toByteArray());
        }
        return this;
    }

    private boolean updateStream(DataOutputStream sourceIDStream) {
        try {
            sourceIDStream.write(mac);
            sourceIDStream.write(ip);
            sourceIDStream.writeLong(userId);
            sourceIDStream.writeLong(date.getTime());
            sourceIDStream.writeLong(revision);
            sourceIDStream.writeLong(ID);
            sourceIDStream.writeLong(sessionId);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private long getCRC(byte[] bytes) {
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return crc32.getValue();
    }

    public int hashCode() {
        return (int) crc;
    }

    public boolean equals(Object o) {
        if (o instanceof SourceID) {
            SourceID s = (SourceID) o;
            return Arrays.equals(mac, SourceID.mac) && Arrays.equals(ip, s.ip) && revision == s.revision && userId == s.userId && date.equals(s.date) && ID == s.ID && sessionId == s.sessionId && crc == s.crc;
        } else return false;
    }

    private static long SourceIDs = 1;

    public static byte[] DEFAULT_MAC = new byte[] { -1, -1, -1, -1, -1, -1 };

    public static byte[] DEFAULT_IP = new byte[] { -1, -1, -1, -1 };

    private static byte[] mac = getMAC();

    private byte[] ip = getIP();

    private long revision = Effectopedia.EFFECTOPEDIA.getRevision();

    private long userId = Effectopedia.EFFECTOPEDIA.getCurrentUserID();

    private Date date = new Date();

    private long sessionId = getSessionID();

    private long ID = 0;

    private long crc;
}
