package com.hadeslee.audiotag.audio.ogg.util;

import com.hadeslee.audiotag.audio.ogg.VorbisVersion;
import com.hadeslee.audiotag.audio.generic.Utils;
import java.util.logging.Logger;

/**
 * Vorbis Identification header
 *
 * From http://xiph.org/vorbis/doc/Vorbis_I_spec.html#id326710
 *
 * The identification header is a short header of only a few fields used to declare the stream definitively as Vorbis,
 * and provide a few externally relevant pieces of information about the audio stream. The identification header is
 * coded as follows:
 *
 * 1) [vorbis_version] = read 32 bits as unsigned integer
 * 2) [audio_channels] = read 8 bit integer as unsigned
 * 3) [audio_sample_rate] = read 32 bits as unsigned integer
 * 4) [bitrate_maximum] = read 32 bits as signed integer
 * 5) [bitrate_nominal] = read 32 bits as signed integer
 * 6) [bitrate_minimum] = read 32 bits as signed integer
 * 7) [blocksize_0] = 2 exponent (read 4 bits as unsigned integer)
 * 8) [blocksize_1] = 2 exponent (read 4 bits as unsigned integer)
 * 9) [framing_flag] = read one bit
 *
 * $Id: VorbisIdentificationHeader.java,v 1.5 2007/11/27 17:03:32 paultaylor Exp $
 *
 * @author Raphael Slinckx (KiKiDonK)
 * @version 16 d�cembre 2003
 */
public class VorbisIdentificationHeader implements VorbisHeader {

    public static Logger logger = Logger.getLogger("com.hadeslee.jaudiotagger.audio.ogg.atom");

    private int audioChannels;

    private boolean isValid = false;

    private int vorbisVersion, audioSampleRate;

    private int bitrateMinimal, bitrateNominal, bitrateMaximal;

    public static final int FIELD_VORBIS_VERSION_POS = 7;

    public static final int FIELD_AUDIO_CHANNELS_POS = 11;

    public static final int FIELD_AUDIO_SAMPLE_RATE_POS = 12;

    public static final int FIELD_BITRATE_MAX_POS = 16;

    public static final int FIELD_BITRATE_NOMAIML_POS = 20;

    public static final int FIELD_BITRATE_MIN_POS = 24;

    public static final int FIELD_BLOCKSIZE_POS = 28;

    public static final int FIELD_FRAMING_FLAG_POS = 29;

    public static final int FIELD_VORBIS_VERSION_LENGTH = 4;

    public static final int FIELD_AUDIO_CHANNELS_LENGTH = 1;

    public static final int FIELD_AUDIO_SAMPLE_RATE_LENGTH = 4;

    public static final int FIELD_BITRATE_MAX_LENGTH = 4;

    public static final int FIELD_BITRATE_NOMAIML_LENGTH = 4;

    public static final int FIELD_BITRATE_MIN_LENGTH = 4;

    public static final int FIELD_BLOCKSIZE_LENGTH = 1;

    public static final int FIELD_FRAMING_FLAG_LENGTH = 1;

    public VorbisIdentificationHeader(byte[] vorbisData) {
        decodeHeader(vorbisData);
    }

    public int getChannelNumber() {
        return audioChannels;
    }

    public String getEncodingType() {
        return VorbisVersion.values()[vorbisVersion].toString();
    }

    public int getSamplingRate() {
        return audioSampleRate;
    }

    public int getNominalBitrate() {
        return bitrateNominal;
    }

    public int getMaxBitrate() {
        return bitrateMaximal;
    }

    public int getMinBitrate() {
        return bitrateMinimal;
    }

    public boolean isValid() {
        return isValid;
    }

    public void decodeHeader(byte[] b) {
        int packetType = b[FIELD_PACKET_TYPE_POS];
        logger.fine("packetType" + packetType);
        String vorbis = Utils.getString(b, VorbisHeader.FIELD_CAPTURE_PATTERN_POS, VorbisHeader.FIELD_CAPTURE_PATTERN_LENGTH, "ISO-8859-1");
        if (packetType == VorbisPacketType.IDENTIFICATION_HEADER.getType() && vorbis.equals(CAPTURE_PATTERN)) {
            this.vorbisVersion = b[7] + (b[8] << 8) + (b[9] << 16) + (b[10] << 24);
            logger.fine("vorbisVersion" + vorbisVersion);
            this.audioChannels = u(b[FIELD_AUDIO_CHANNELS_POS]);
            logger.fine("audioChannels" + audioChannels);
            this.audioSampleRate = u(b[12]) + (u(b[13]) << 8) + (u(b[14]) << 16) + (u(b[15]) << 24);
            logger.fine("audioSampleRate" + audioSampleRate);
            logger.fine("audioSampleRate" + b[12] + " " + b[13] + " " + b[14]);
            this.bitrateMinimal = u(b[16]) + (u(b[17]) << 8) + (u(b[18]) << 16) + (u(b[19]) << 24);
            this.bitrateNominal = u(b[20]) + (u(b[21]) << 8) + (u(b[22]) << 16) + (u(b[23]) << 24);
            this.bitrateMaximal = u(b[24]) + (u(b[25]) << 8) + (u(b[26]) << 16) + (u(b[27]) << 24);
            int framingFlag = b[FIELD_FRAMING_FLAG_POS];
            logger.fine("framingFlag" + framingFlag);
            if (framingFlag != 0) {
                isValid = true;
            }
        }
    }

    private int u(int i) {
        return i & 0xFF;
    }
}
