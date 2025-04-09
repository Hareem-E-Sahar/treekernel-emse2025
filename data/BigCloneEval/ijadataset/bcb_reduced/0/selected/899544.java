package com.frinika.sequencer.model.audio;

import com.frinika.audio.io.*;
import java.io.IOException;
import uk.org.toot.audio.core.AudioBuffer;
import com.frinika.sequencer.model.AudioPart.Envelope;

public class EnvelopedAudioReader extends AudioReader {

    protected double gain = 1.0;

    protected long attackEnd;

    protected long decayStart;

    public EnvelopedAudioReader(RandomAccessFileIF fis, float Fs) throws IOException {
        super(fis, Fs);
    }

    public void setEvelope(Envelope e) {
        setBoundsInMicros(e.getTOn(), e.getTOff());
        gain = e.getGain();
        attackEnd = startByte + milliToByte(e.getTRise());
        decayStart = endByte - milliToByte(e.getTFall());
        if (attackEnd > decayStart) {
            long av = (attackEnd + decayStart) / 2;
            attackEnd = decayStart = (av / (nChannels * 2)) * nChannels * 2;
        }
    }

    protected void processAudioImp(AudioBuffer buffer, int startChunk, int endChunk) {
        long fPtr1 = fPtrBytes + startChunk;
        long fPtr2 = fPtrBytes + endChunk;
        if (fPtr1 <= decayStart && fPtr2 >= attackEnd) {
            fillConstantGain(buffer, startChunk, endChunk, gain);
        } else if (fPtr1 < attackEnd && fPtr2 >= attackEnd) {
            double gainNow = gain * (fPtr1 - startByte) / (attackEnd - startByte);
            fillLinearInterpolate(buffer, startChunk, endChunk, gainNow, gain);
        } else if (fPtr1 < attackEnd) {
            double gainNow = gain * (fPtr1 - startByte) / (attackEnd - startByte);
            double gainNext = gain * (fPtr2 - startByte) / (attackEnd - startByte);
            fillLinearInterpolate(buffer, startChunk, endChunk, gainNow, gainNext);
        } else if (fPtr1 <= decayStart && fPtr2 > decayStart) {
            double gainNext = gain * (endByte - fPtr2) / (endByte - decayStart);
            fillLinearInterpolate(buffer, startChunk, endChunk, gain, gainNext);
        } else {
            assert (fPtr1 > decayStart);
            double gainNow = gain * (endByte - fPtr1) / (endByte - decayStart);
            double gainNext = gain * (endByte - fPtr2) / (endByte - decayStart);
            fillLinearInterpolate(buffer, startChunk, endChunk, gainNow, gainNext);
        }
    }
}
