package it.jnrpe.net;

import java.util.zip.CRC32;

/**
 * This object represent a generic response packet
 * 
 * @author Massimiliano Ziccardi
 */
public class JNRPEResponse extends JNRPEProtocolPacket {

    public JNRPEResponse() {
        super();
        setPacketType(IJNRPEConstants.RESPONSE_PACKET);
    }

    public void updateCRC() {
        setCRC(0);
        int iCRC = 0;
        CRC32 crcAlg = new CRC32();
        crcAlg.update(toByteArray());
        iCRC = (int) crcAlg.getValue();
        setCRC(iCRC);
    }

    public void setMessage(String sMessage) {
        initRandomBuffer();
        _setMessage(sMessage);
    }
}
