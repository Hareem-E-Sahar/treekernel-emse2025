package net.sf.fmj.media.codec.audio.gsm;

import javax.media.*;
import javax.media.format.*;
import net.sf.fmj.media.*;

/**
 * 
 * DePacketizer for GSM/RTP. Doesn't have to do much, just copies input to
 * output. Uses buffer-swapping observed in debugging and seen in other
 * open-source DePacketizer implementations.
 * 
 * @author Martin Harvan
 * @author Damian Minkov
 */
public class DePacketizer extends AbstractDePacketizer {

    protected Format[] outputFormats = new Format[] { new AudioFormat(AudioFormat.GSM, 8000, 8, 1, -1, AudioFormat.SIGNED, 264, -1.0, Format.byteArray) };

    public DePacketizer() {
        super();
        this.inputFormats = new Format[] { new AudioFormat(AudioFormat.GSM_RTP, 8000, 8, 1, Format.NOT_SPECIFIED, AudioFormat.SIGNED, 264, Format.NOT_SPECIFIED, Format.byteArray) };
    }

    @Override
    public void close() {
    }

    @Override
    public String getName() {
        return "GSM DePacketizer";
    }

    @Override
    public Format[] getSupportedOutputFormats(Format input) {
        if (input == null) return outputFormats; else {
            if (!(input instanceof AudioFormat)) {
                return new Format[] { null };
            }
            final AudioFormat inputCast = (AudioFormat) input;
            if (!inputCast.getEncoding().equals(AudioFormat.GSM_RTP)) {
                return new Format[] { null };
            }
            final AudioFormat result = new AudioFormat(AudioFormat.GSM, inputCast.getSampleRate(), inputCast.getSampleSizeInBits(), inputCast.getChannels(), inputCast.getEndian(), inputCast.getSigned(), inputCast.getFrameSizeInBits(), inputCast.getFrameRate(), inputCast.getDataType());
            return new Format[] { result };
        }
    }

    @Override
    public void open() {
    }

    @Override
    public Format setInputFormat(Format f) {
        return super.setInputFormat(f);
    }

    @Override
    public Format setOutputFormat(Format f) {
        return super.setOutputFormat(f);
    }
}
