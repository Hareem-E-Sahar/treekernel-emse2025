import java.io.PrintStream;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Receiver;

/**	Displays the file format information of a MIDI file.
 */
public class DumpReceiver implements Receiver {

    public static long seByteCount = 0;

    public static long smByteCount = 0;

    public static long seCount = 0;

    public static long smCount = 0;

    private static final String[] sm_astrKeyNames = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

    private static final String[] sm_astrKeySignatures = { "Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#" };

    private static final String[] SYSTEM_MESSAGE_TEXT = { "System Exclusive (should not be in ShortMessage!)", "MTC Quarter Frame: ", "Song Position: ", "Song Select: ", "Undefined", "Undefined", "Tune Request", "End of SysEx (should not be in ShortMessage!)", "Timing clock", "Undefined", "Start", "Continue", "Stop", "Undefined", "Active Sensing", "System Reset" };

    private static final String[] QUARTER_FRAME_MESSAGE_TEXT = { "frame count LS: ", "frame count MS: ", "seconds count LS: ", "seconds count MS: ", "minutes count LS: ", "minutes count MS: ", "hours count LS: ", "hours count MS: " };

    private static final String[] FRAME_TYPE_TEXT = { "24 frames/second", "25 frames/second", "30 frames/second (drop)", "30 frames/second (non-drop)" };

    private PrintStream m_printStream;

    private boolean m_bDebug;

    private boolean m_bPrintTimeStampAsTicks;

    public DumpReceiver(PrintStream printStream) {
        this(printStream, false);
    }

    public DumpReceiver(PrintStream printStream, boolean bPrintTimeStampAsTicks) {
        m_printStream = printStream;
        m_bDebug = false;
        m_bPrintTimeStampAsTicks = bPrintTimeStampAsTicks;
    }

    public void close() {
    }

    public void send(MidiMessage message, long lTimeStamp) {
        String strMessage = null;
        if (message instanceof ShortMessage) {
            strMessage = decodeMessage((ShortMessage) message);
        } else if (message instanceof SysexMessage) {
            strMessage = decodeMessage((SysexMessage) message);
        } else if (message instanceof MetaMessage) {
            strMessage = decodeMessage((MetaMessage) message);
        } else {
            strMessage = "unknown message type";
        }
        String strTimeStamp = null;
        if (m_bPrintTimeStampAsTicks) {
            strTimeStamp = "tick " + lTimeStamp + ": ";
        } else {
            if (lTimeStamp == -1L) {
                strTimeStamp = "timestamp [unknown]: ";
            } else {
                strTimeStamp = "timestamp " + lTimeStamp + " us: ";
            }
        }
        m_printStream.println(strTimeStamp + strMessage);
    }

    public String decodeMessage(ShortMessage message) {
        String strMessage = null;
        switch(message.getCommand()) {
            case 0x80:
                strMessage = "note Off " + getKeyName(message.getData1()) + " velocity: " + message.getData2();
                break;
            case 0x90:
                strMessage = "note On " + getKeyName(message.getData1()) + " velocity: " + message.getData2();
                break;
            case 0xa0:
                strMessage = "polyphonic key pressure " + getKeyName(message.getData1()) + " pressure: " + message.getData2();
                break;
            case 0xb0:
                strMessage = "control change " + message.getData1() + " value: " + message.getData2();
                break;
            case 0xc0:
                strMessage = "program change " + message.getData1();
                break;
            case 0xd0:
                strMessage = "key pressure " + getKeyName(message.getData1()) + " pressure: " + message.getData2();
                break;
            case 0xe0:
                strMessage = "pitch wheel change " + get14bitValue(message.getData1(), message.getData2());
                break;
            case 0xF0:
                strMessage = SYSTEM_MESSAGE_TEXT[message.getChannel()];
                switch(message.getChannel()) {
                    case 0x1:
                        int nQType = (message.getData1() & 0x70) >> 4;
                        int nQData = message.getData1() & 0x0F;
                        if (nQType == 7) {
                            nQData = nQData & 0x1;
                        }
                        strMessage += QUARTER_FRAME_MESSAGE_TEXT[nQType] + nQData;
                        if (nQType == 7) {
                            int nFrameType = (message.getData1() & 0x06) >> 1;
                            strMessage += ", frame type: " + FRAME_TYPE_TEXT[nFrameType];
                        }
                        break;
                    case 0x2:
                        strMessage += get14bitValue(message.getData1(), message.getData2());
                        break;
                    case 0x3:
                        strMessage += message.getData1();
                        break;
                }
                break;
            default:
                strMessage = "unknown message: status = " + message.getStatus() + ", byte1 = " + message.getData1() + ", byte2 = " + message.getData2();
                break;
        }
        if (message.getCommand() != 0xF0) {
            int nChannel = message.getChannel() + 1;
            String strChannel = "channel " + nChannel + ": ";
            strMessage = strChannel + strMessage;
        }
        smCount++;
        smByteCount += message.getLength();
        return "[" + getHexString(message) + "] " + strMessage;
    }

    public String decodeMessage(SysexMessage message) {
        byte[] abData = message.getData();
        String strMessage = null;
        if (message.getStatus() == SysexMessage.SYSTEM_EXCLUSIVE) {
            strMessage = "Sysex message: F0" + getHexString(abData);
        } else if (message.getStatus() == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) {
            strMessage = "Continued Sysex message F7" + getHexString(abData);
            seByteCount--;
        }
        seByteCount += abData.length + 1;
        seCount++;
        return strMessage;
    }

    public String decodeMessage(MetaMessage message) {
        byte[] abMessage = message.getMessage();
        byte[] abData = message.getData();
        int nDataLength = message.getLength();
        String strMessage = null;
        switch(message.getType()) {
            case 0:
                int nSequenceNumber = ((abData[0] & 0xFF) << 8) | (abData[1] & 0xFF);
                strMessage = "Sequence Number: " + nSequenceNumber;
                break;
            case 1:
                String strText = new String(abData);
                strMessage = "Text Event: " + strText;
                break;
            case 2:
                String strCopyrightText = new String(abData);
                strMessage = "Copyright Notice: " + strCopyrightText;
                break;
            case 3:
                String strTrackName = new String(abData);
                strMessage = "Sequence/Track Name: " + strTrackName;
                break;
            case 4:
                String strInstrumentName = new String(abData);
                strMessage = "Instrument Name: " + strInstrumentName;
                break;
            case 5:
                String strLyrics = new String(abData);
                strMessage = "Lyric: " + strLyrics;
                break;
            case 6:
                String strMarkerText = new String(abData);
                strMessage = "Marker: " + strMarkerText;
                break;
            case 7:
                String strCuePointText = new String(abData);
                strMessage = "Cue Point: " + strCuePointText;
                break;
            case 0x20:
                int nChannelPrefix = abData[0] & 0xFF;
                strMessage = "MIDI Channel Prefix: " + nChannelPrefix;
                break;
            case 0x2F:
                strMessage = "End of Track";
                break;
            case 0x51:
                int nTempo = ((abData[0] & 0xFF) << 16) | ((abData[1] & 0xFF) << 8) | (abData[2] & 0xFF);
                float bpm = convertTempo(nTempo);
                bpm = (float) (Math.round(bpm * 100.0f) / 100.0f);
                strMessage = "Set Tempo: " + bpm + " bpm";
                break;
            case 0x54:
                strMessage = "SMTPE Offset: " + (abData[0] & 0xFF) + ":" + (abData[1] & 0xFF) + ":" + (abData[2] & 0xFF) + "." + (abData[3] & 0xFF) + "." + (abData[4] & 0xFF);
                break;
            case 0x58:
                strMessage = "Time Signature: " + (abData[0] & 0xFF) + "/" + (1 << (abData[1] & 0xFF)) + ", MIDI clocks per metronome tick: " + (abData[2] & 0xFF) + ", 1/32 per 24 MIDI clocks: " + (abData[3] & 0xFF);
                break;
            case 0x59:
                String strGender = (abData[1] == 1) ? "minor" : "major";
                strMessage = "Key Signature: " + sm_astrKeySignatures[abData[0] + 7] + " " + strGender;
                break;
            case 0x7F:
                String strDataDump = getHexString(abData);
                strMessage = "Sequencer-Specific Meta event: " + strDataDump;
                break;
            default:
                String strUnknownDump = getHexString(abData);
                strMessage = "unknown Meta event: " + strUnknownDump;
                break;
        }
        return strMessage;
    }

    public static String getKeyName(int nKeyNumber) {
        if (nKeyNumber > 127) {
            return "illegal value";
        } else {
            int nNote = nKeyNumber % 12;
            int nOctave = nKeyNumber / 12;
            return sm_astrKeyNames[nNote] + (nOctave - 1);
        }
    }

    public static int get14bitValue(int nLowerPart, int nHigherPart) {
        return (nLowerPart & 0x7F) | ((nHigherPart & 0x7F) << 7);
    }

    private static int signedByteToUnsigned(byte b) {
        return b & 0xFF;
    }

    private static float convertTempo(float value) {
        if (value <= 0) {
            value = 0.1f;
        }
        return 60000000.0f / value;
    }

    private static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static String getHexString(byte[] aByte) {
        StringBuffer sbuf = new StringBuffer(aByte.length * 3 + 2);
        for (int i = 0; i < aByte.length; i++) {
            sbuf.append(' ');
            sbuf.append(hexDigits[(aByte[i] & 0xF0) >> 4]);
            sbuf.append(hexDigits[aByte[i] & 0x0F]);
        }
        return new String(sbuf);
    }

    private static String intToHex(int i) {
        return "" + hexDigits[(i & 0xF0) >> 4] + hexDigits[i & 0x0F];
    }

    public static String getHexString(ShortMessage sm) {
        int status = sm.getStatus();
        String res = intToHex(sm.getStatus());
        switch(status) {
            case 0xF6:
            case 0xF7:
            case 0xF8:
            case 0xF9:
            case 0xFA:
            case 0xFB:
            case 0xFC:
            case 0xFD:
            case 0xFE:
            case 0xFF:
                return res;
        }
        res += ' ' + intToHex(sm.getData1());
        switch(status) {
            case 0xF1:
            case 0xF3:
                return res;
        }
        switch(sm.getCommand()) {
            case 0xC0:
            case 0xD0:
                return res;
        }
        res += ' ' + intToHex(sm.getData2());
        return res;
    }
}
