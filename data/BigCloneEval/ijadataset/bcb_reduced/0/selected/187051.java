package org.jsresources.apps.jsinfo;

import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

public class LineLifeCycleAudioInputStream extends AudioInputStream {

    private AudioFormat m_audioFormat;

    private float[] m_afBuffer;

    private byte[] m_abBuffer;

    private int m_nBufferPointer;

    public LineLifeCycleAudioInputStream(AudioFormat audioFormat) {
        super(new ByteArrayInputStream(new byte[0]), audioFormat, AudioSystem.NOT_SPECIFIED);
        m_audioFormat = audioFormat;
        byte[] abData;
        AudioFormat m_format;
        int nSignalType = 1;
        float fAmplitude = 20000.0F;
        int nSampleFrequency = (int) m_audioFormat.getFrameRate();
        int nSignalFrequency = 1000;
        int nPeriodLengthInFrames = nSampleFrequency / nSignalFrequency;
        int nNumPeriodsInBuffer = 1;
        int nNumFramesInBuffer = nNumPeriodsInBuffer * nPeriodLengthInFrames;
        int nBufferLength = nNumFramesInBuffer * 4;
        m_afBuffer = new float[nPeriodLengthInFrames];
        for (int nFrame = 0; nFrame < nPeriodLengthInFrames; nFrame++) {
            float fValue = 0;
            switch(nSignalType) {
                case 1:
                    fValue = (float) (Math.sin(((double) nFrame / (double) nPeriodLengthInFrames) * 2.0 * Math.PI) * fAmplitude);
                    break;
                case 2:
                    if (nFrame < nPeriodLengthInFrames / 2) {
                        fValue = fAmplitude;
                    } else {
                        fValue = -fAmplitude;
                    }
            }
            m_afBuffer[nFrame] = fValue;
        }
        int nByteBufferLengthInBytes = nBufferLength * m_audioFormat.getFrameSize();
        m_abBuffer = new byte[nByteBufferLengthInBytes];
        int nBytePointer = 0;
        for (int nFrame = 0; nFrame < nPeriodLengthInFrames; nFrame++) {
            float fSample = m_afBuffer[nFrame];
            int nSample = (int) fSample;
            for (int nChannel = 0; nChannel < m_audioFormat.getChannels(); nChannel++) {
                if (m_audioFormat.isBigEndian()) {
                    m_abBuffer[nBytePointer] = (byte) ((nSample >> 8) & 0xFF);
                    nBytePointer++;
                    m_abBuffer[nBytePointer] = (byte) (nSample & 0xFF);
                    nBytePointer++;
                } else {
                    m_abBuffer[nBytePointer] = (byte) (nSample & 0xFF);
                    nBytePointer++;
                    m_abBuffer[nBytePointer] = (byte) ((nSample >> 8) & 0xFF);
                    nBytePointer++;
                }
            }
        }
        m_nBufferPointer = 0;
    }

    private void advanceBufferPointer(int nAmount) {
        m_nBufferPointer += nAmount;
        if (m_nBufferPointer >= m_abBuffer.length) {
            m_nBufferPointer = 0;
        }
    }

    public int read() {
        int nResult = m_abBuffer[m_nBufferPointer];
        advanceBufferPointer(1);
        return nResult;
    }

    public int read(byte[] abData, int nOffset, int nLength) {
        int nRemainingLength = nLength;
        while (nRemainingLength > 0) {
            int nAvailable = m_abBuffer.length - m_nBufferPointer;
            int nCopyLength = Math.min(nAvailable, nRemainingLength);
            System.arraycopy(m_abBuffer, m_nBufferPointer, abData, nOffset, nCopyLength);
            advanceBufferPointer(nCopyLength);
            nOffset += nCopyLength;
            nRemainingLength -= nCopyLength;
        }
        return nLength;
    }
}
