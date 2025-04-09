package SMS;

import java.util.*;
import combinereport.query.In;

/**
 * OutMessage objects are the messages that will be sent through the GSM Modem. 
 * These objects need to be initilised before calling the send method on it.
 * @author Administrator
 */
public class OutMessage {

    private static int maxSize = 160;

    private int srcPort = -1;

    private int dstPort = -1;

    private int validityPeriod = -1;

    private boolean statusReport = true;

    protected Date dispatchDate;

    protected MessageEncoding messageEncoding;

    protected int refNo = -1;

    private int outMpRefNo;

    private int mpRefNo = 0, partNo = 0;

    public static enum MessageEncoding {

        Enc7Bit, Enc8Bit, EncUcs2
    }

    String sms;

    String no;

    /** Creates a new instance of OutMessage */
    public OutMessage(String no, String msg) {
        sms = msg;
        this.no = no;
        this.dispatchDate = null;
        outMpRefNo = new Random().nextInt();
        if (outMpRefNo < 0) outMpRefNo *= -1;
        outMpRefNo %= 65536;
    }

    /** Used to check if the message to be sent is greater than 160 chars (ie. big) */
    protected boolean isBig() throws Exception {
        int messageLength;
        messageLength = sms.length();
        return (messageLength > maxSize ? true : false);
    }

    /** 
     * Converts the message to be sent in the PDU format. Adds the reference code to it. 
     * Handles the big message situation.
     */
    public String getPDU(String smscNumber, int mpRefNo, int partNo) throws Exception {
        String pdu, udh;
        String str1, str2;
        int i, high, low;
        char c;
        pdu = "";
        udh = "";
        if ((smscNumber != null) && (smscNumber.length() != 0)) {
            str1 = "91" + toBCDFormat(smscNumber.substring(1));
            str2 = Integer.toHexString(str1.length() / 2);
            if (str2.length() != 2) str2 = "0" + str2;
            pdu = pdu + str2 + str1;
        } else if ((smscNumber != null) && (smscNumber.length() == 0)) pdu = pdu + "00";
        if (((srcPort != -1) && (dstPort != -1)) || (isBig())) {
            if (statusReport) pdu = pdu + "71"; else pdu = pdu + "51";
        } else {
            if (statusReport) pdu = pdu + "31"; else pdu = pdu + "11";
        }
        pdu = pdu + "00";
        str1 = getRecipient();
        if (str1.charAt(0) == '+') {
            str1 = toBCDFormat(str1.substring(1));
            str2 = Integer.toHexString(getRecipient().length() - 1);
            str1 = "91" + str1;
        } else {
            str1 = toBCDFormat(str1);
            str2 = Integer.toHexString(getRecipient().length());
            str1 = "81" + str1;
        }
        if (str2.length() != 2) str2 = "0" + str2;
        pdu = pdu + str2 + str1;
        pdu = pdu + "00";
        switch(getMessageEncoding()) {
            case Enc7Bit:
                pdu = pdu + "00";
                break;
            default:
                throw new Exception();
        }
        pdu = pdu + getValidityPeriodBits();
        if ((srcPort != -1) && (dstPort != -1)) {
            String s;
            udh += "060504";
            s = Integer.toHexString(dstPort);
            while (s.length() < 4) s = "0" + s;
            udh += s;
            s = Integer.toHexString(srcPort);
            while (s.length() < 4) s = "0" + s;
            udh += s;
        }
        if (isBig()) {
            String s;
            if ((srcPort != -1) && (dstPort != -1)) udh = "0C" + udh.substring(2) + "0804"; else udh += "060804";
            s = Integer.toHexString(mpRefNo);
            while (s.length() < 4) s = "0" + s;
            udh += s;
            s = Integer.toHexString(getNoOfParts());
            while (s.length() < 2) s = "0" + s;
            udh += s;
            s = Integer.toHexString(partNo);
            while (s.length() < 2) s = "0" + s;
            udh += s;
        }
        switch(getMessageEncoding()) {
            case Enc7Bit:
                str2 = textToPDU(getPart(sms, partNo));
                i = CGSMAlphabet.stringToBytes(getPart(sms, partNo), new byte[400]);
                if ((srcPort != -1) && (dstPort != -1)) str1 = Integer.toHexString(i + 8); else if (isBig()) str1 = Integer.toHexString(i + 8); else str1 = Integer.toHexString(i);
                break;
            default:
                throw new Exception();
        }
        if (str1.length() != 2) str1 = "0" + str1;
        if (((srcPort != -1) && (dstPort != -1)) || (isBig())) pdu = pdu + str1 + udh + str2; else pdu = pdu + str1 + str2;
        return pdu.toUpperCase();
    }

    /** Converts the String to the BCD Format */
    private String toBCDFormat(String s) {
        String bcd;
        int i;
        if ((s.length() % 2) != 0) s = s + "F";
        bcd = "";
        for (i = 0; i < s.length(); i += 2) bcd = bcd + s.charAt(i + 1) + s.charAt(i);
        return bcd;
    }

    /** Used to get the recipitent identfication number */
    public String getRecipient() {
        return no;
    }

    /** Return the validity period. */
    private String getValidityPeriodBits() {
        String bits;
        int value;
        if (validityPeriod == -1) bits = "FF"; else {
            if (validityPeriod <= 12) value = (validityPeriod * 12) - 1; else if (validityPeriod <= 24) value = (((validityPeriod - 12) * 2) + 143); else if (validityPeriod <= 720) value = (validityPeriod / 24) + 166; else value = (validityPeriod / 168) + 192;
            bits = Integer.toHexString(value);
            if (bits.length() != 2) bits = "0" + bits;
            if (bits.length() > 2) bits = "FF";
        }
        return bits;
    }

    /**
     * In case of long message this is used to get the number of messages that the 
     * long message should be splited in. 
     */
    protected int getNoOfParts() {
        int noOfParts = 0;
        int partSize;
        int messageLength;
        partSize = maxSize - 8;
        messageLength = sms.length();
        noOfParts = messageLength / partSize;
        if ((noOfParts * partSize) < (messageLength)) noOfParts++;
        return noOfParts;
    }

    /** Converts the text to be sent in the pdu format */
    private String textToPDU(String txt) throws Exception {
        String pdu, str1;
        byte[] bytes, oldBytes, newBytes;
        BitSet bitSet;
        int i, j, value1, value2;
        bytes = new byte[400];
        i = CGSMAlphabet.stringToBytes(txt, bytes);
        oldBytes = new byte[i];
        for (j = 0; j < i; j++) oldBytes[j] = bytes[j];
        bitSet = new BitSet(oldBytes.length * 8);
        value1 = 0;
        for (i = 0; i < oldBytes.length; i++) for (j = 0; j < 7; j++) {
            value1 = (i * 7) + j;
            if ((oldBytes[i] & (1 << j)) != 0) bitSet.set(value1);
        }
        value1++;
        if (((value1 / 56) * 56) != value1) value2 = (value1 / 8) + 1; else value2 = (value1 / 8);
        if (value2 == 0) value2 = 1;
        newBytes = new byte[value2];
        for (i = 0; i < value2; i++) for (j = 0; j < 8; j++) if ((value1 + 1) > ((i * 8) + j)) if (bitSet.get(i * 8 + j)) newBytes[i] |= (byte) (1 << j);
        pdu = "";
        for (i = 0; i < value2; i++) {
            str1 = Integer.toHexString(newBytes[i]);
            if (str1.length() != 2) str1 = "0" + str1;
            str1 = str1.substring(str1.length() - 2, str1.length());
            pdu += str1;
        }
        return pdu;
    }

    /** Returns the part requested, of the long messages. Splits the long message*/
    private String getPart(String txt, int partNo) throws Exception {
        String textPart;
        int partSize;
        textPart = txt;
        if (partNo != 0) {
            partSize = maxSize - 8;
            if (((partSize * (partNo - 1)) + partSize) > txt.length()) textPart = txt.substring(partSize * (partNo - 1)); else textPart = txt.substring(partSize * (partNo - 1), (partSize * (partNo - 1)) + partSize);
        }
        return textPart;
    }

    void processBigSMS() throws Exception {
        String pdu = null;
        int j;
        Modem modem = new Modem("COM4", "115200");
        if (!isBig()) {
            try {
                pdu = getPDU(no, 0, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            j = pdu.length();
            j /= 2;
            if (no == null) ; else if (no.length() == 0) j--; else {
                int smscNumberLen = no.length();
                if (no.charAt(0) == '+') smscNumberLen--;
                if (smscNumberLen % 2 != 0) smscNumberLen++;
                int smscLen = (2 + smscNumberLen) / 2;
                j = j - smscLen - 1;
            }
        } else {
            System.out.println("Sending Long Message. In " + getNoOfParts() + " parts.");
            for (int partNo = 1; partNo <= getNoOfParts(); partNo++) {
                System.out.println("__ Sending part " + partNo + " __");
                try {
                    pdu = getPDU(no, mpRefNo, partNo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                j = pdu.length();
                j /= 2;
                if (no == null) ; else if (no.length() == 0) j--; else {
                    int smscNumberLen = no.length();
                    if (no.charAt(0) == '+') smscNumberLen--;
                    if (smscNumberLen % 2 != 0) smscNumberLen++;
                    int smscLen = (2 + smscNumberLen) / 2;
                    j = j - smscLen - 1;
                }
                System.out.println("Message index ::" + j + "\n Message to Send :" + pdu);
                modem.SendSms("+919922930640", pdu, 2);
            }
            outMpRefNo = (outMpRefNo + 1) % 65536;
        }
    }

    public String hexToString(String txtInHex) {
        byte[] txtInByte = new byte[txtInHex.length() / 2];
        int j = 0;
        for (int i = 0; i < txtInHex.length(); i += 2) {
            String str = txtInHex.substring(i, i + 2);
            txtInByte[j++] = Byte.parseByte(str, 16);
        }
        String txt = new String(txtInByte);
        System.out.println(txt);
        return txt;
    }

    public MessageEncoding getMessageEncoding() {
        return messageEncoding;
    }

    public void setMessageEncoding(MessageEncoding messageEncoding) {
        this.messageEncoding = messageEncoding;
    }

    protected void setRefNo(int refNo) {
        this.refNo = refNo;
    }

    public static void main(String[] args) {
        try {
            OutMessage sm = new OutMessage("+919922930640", "afhghdflhglhfghkldfhghdfklhg dhshgkldgfhkj hgdhfhg ldfhgyre sdhgfhf ghgfjasd sudfhsg asdhg asdguih" + "dfhgfhoitogih dfhgif ghigfgf[hgf higfh ] hkf gihpigfhopigh idoifsejrkj  dfgidf gj kgyhpfk h oyirotritiyitr kl dgfh");
            sm.setMessageEncoding(MessageEncoding.Enc7Bit);
            sm.processBigSMS();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
