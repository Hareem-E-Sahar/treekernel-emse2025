package dalsong.mp3info;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class represents a structure for storing and retrieving information
 * about the codec respectively the encoding parameters.<br>
 * Most of the parameters are available for nearly each audio format. Some
 * others would result in standard values.<br>
 * <b>Consider:</b> None of the setter methods will actually affect the audio
 * file. This is just a structure for retrieving information, not manipulating
 * the audio file.<br>
 * 
 * @author Raphael Slinckx
 */
public class EncodingInfo {

    /**
	 * The key for the Bitrate.({@link Integer})<br>
	 * 
	 * @see #content
	 */
    public static final String FIELD_BITRATE = "BITRATE";

    /**
	 * The key for the number of audio channels.({@link Integer})<br>
	 * 
	 * @see #content
	 */
    public static final String FIELD_CHANNEL = "CHANNB";

    /**
	 * The key for the extra encoding information.({@link String})<br>
	 * 
	 * @see #content
	 */
    public static final String FIELD_INFOS = "INFOS";

    /**
	 * The key for the audio clip duration in seconds. ({@link java.lang.Float})<br>
	 * 
	 * @see #content
	 */
    public static final String FIELD_LENGTH = "LENGTH";

    /**
	 * The key for the audio sample rate in &quot;Hz&quot;. ({@link Integer})<br>
	 * 
	 * @see #content
	 */
    public static final String FIELD_SAMPLERATE = "SAMPLING";

    public static final String FIELD_TAGSIZE = "TAGSIZE";

    /**
	 * The key for the audio type.({@link String})<br>
	 * 
	 * @see #content
	 */
    public static final String FIELD_TYPE = "TYPE";

    /**
	 * The key for the VBR flag. ({@link Boolean})<br>
	 * 
	 * @see #content
	 */
    public static final String FIELD_VBR = "VBR";

    /**
	 * This table containts the parameters.<br>
	 */
    private Hashtable content;

    /**
	 * Creates an instance with emtpy values.<br>
	 */
    public EncodingInfo() {
        content = new Hashtable(6);
        content.put(FIELD_BITRATE, new Integer(-1));
        content.put(FIELD_CHANNEL, new Integer(-1));
        content.put(FIELD_TYPE, "");
        content.put(FIELD_INFOS, "");
        content.put(FIELD_SAMPLERATE, new Integer(-1));
        content.put(FIELD_LENGTH, new Float(-1));
        content.put(FIELD_VBR, new Boolean(true));
        content.put(FIELD_TAGSIZE, new Integer(0));
    }

    public void setTagSize(int tagsize) {
        content.put(FIELD_TAGSIZE, new Integer(tagsize));
    }

    public int getTagSize() {
        return ((Integer) content.get(FIELD_TAGSIZE)).intValue();
    }

    /**
	 * This method returns the bitrate of the represented audio clip in
	 * &quot;Kbps&quot;.<br>
	 * 
	 * @return The bitrate in Kbps.
	 */
    public int getBitrate() {
        return ((Integer) content.get(FIELD_BITRATE)).intValue();
    }

    /**
	 * This method returns the number of audio channels the clip contains.<br>
	 * (The stereo, mono thing).
	 * 
	 * @return The number of channels. (2 for stereo, 1 for mono)
	 */
    public int getChannelNumber() {
        return ((Integer) content.get(FIELD_CHANNEL)).intValue();
    }

    /**
	 * Returns the encoding type.
	 * 
	 * @return The encoding type
	 */
    public String getEncodingType() {
        return (String) content.get(FIELD_TYPE);
    }

    /**
	 * This method returns some extra information about the encoding.<br>
	 * This may not contain anything for some audio formats.<br>
	 * 
	 * @return Some extra information.
	 */
    public String getExtraEncodingInfos() {
        return (String) content.get(FIELD_INFOS);
    }

    /**
	 * This method returns the duration of the represented audio clip in
	 * seconds.<br>
	 * 
	 * @see #getPreciseLength()
	 * @return The duration in seconds.
	 */
    public int getLength() {
        return (int) getPreciseLength();
    }

    /**
	 * This method returns the duration of the represented audio clip in seconds
	 * (single-precision).<br>
	 * 
	 * @see #getLength()
	 * @return The duration in seconds.
	 */
    public float getPreciseLength() {
        return ((Float) content.get(FIELD_LENGTH)).floatValue();
    }

    /**
	 * This method returns the sample rate, the audio clip was encoded with.<br>
	 * 
	 * @return Sample rate of the audio clip in &quot;Hz&quot;.
	 */
    public int getSamplingRate() {
        return ((Integer) content.get(FIELD_SAMPLERATE)).intValue();
    }

    /**
	 * This method returns <code>true</code>, if the audio file is encoded
	 * with &quot;Variable Bitrate&quot;.<br>
	 * 
	 * @return <code>true</code> if audio clip is encoded with VBR.
	 */
    public boolean isVbr() {
        return ((Boolean) content.get(FIELD_VBR)).booleanValue();
    }

    /**
	 * This Method sets the bitrate in &quot;Kbps&quot;.<br>
	 * 
	 * @param bitrate
	 *            bitrate in kbps.
	 */
    public void setBitrate(int bitrate) {
        content.put(FIELD_BITRATE, new Integer(bitrate));
    }

    /**
	 * Sets the number of channels.
	 * 
	 * @param chanNb
	 *            number of channels (2 for stereo, 1 for mono).
	 */
    public void setChannelNumber(int chanNb) {
        content.put(FIELD_CHANNEL, new Integer(chanNb));
    }

    /**
	 * Sets the type of the encoding.<br>
	 * This is a bit format specific.<br>
	 * eg:Layer I/II/III
	 * 
	 * @param encodingType
	 *            Encoding type.
	 */
    public void setEncodingType(String encodingType) {
        content.put(FIELD_TYPE, encodingType);
    }

    /**
	 * A string contianing anything else that might be interesting
	 * 
	 * @param infos
	 *            Extra information.
	 */
    public void setExtraEncodingInfos(String infos) {
        content.put(FIELD_INFOS, infos);
    }

    /**
	 * This method sets the audio duration of the represented clip.<br>
	 * 
	 * @param length
	 *            The duration of the audio clip in seconds.
	 */
    public void setLength(int length) {
        content.put(FIELD_LENGTH, new Float(length));
    }

    /**
	 * This method sets the audio duration of the represented clip.<br>
	 * 
	 * @param seconds
	 *            The duration of the audio clip in seconds (single-precision).
	 */
    public void setPreciseLength(float seconds) {
        content.put(FIELD_LENGTH, new Float(seconds));
    }

    /**
	 * Sets the Sampling rate in &quot;Hz&quot;<br>
	 * 
	 * @param samplingRate
	 *            Sample rate.
	 */
    public void setSamplingRate(int samplingRate) {
        content.put(FIELD_SAMPLERATE, new Integer(samplingRate));
    }

    /**
	 * Sets the VBR flag for the represented audio clip.<br>
	 * 
	 * @param b
	 *            <code>true</code> if VBR.
	 */
    public void setVbr(boolean b) {
        content.put(FIELD_VBR, new Boolean(b));
    }

    /**
	 * Pretty prints this encoding info
	 * 
	 * @see java.lang.Object#toString()
	 */
    public String toString() {
        StringBuffer out = new StringBuffer(50);
        out.append("Encoding infos content:\n");
        Enumeration en = content.keys();
        while (en.hasMoreElements()) {
            Object key = en.nextElement();
            Object val = content.get(key);
            out.append("\t");
            out.append(key);
            out.append(" : ");
            out.append(val);
            out.append("\n");
        }
        return out.toString().substring(0, out.length() - 1);
    }
}
