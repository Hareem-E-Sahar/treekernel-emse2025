package jpcsp.HLE.modules150;

import jpcsp.HLE.HLEFunction;
import jpcsp.HLE.HLEUidClass;
import jpcsp.HLE.HLEUidObjectMapping;
import jpcsp.HLE.TPointer;
import jpcsp.HLE.TPointer32;
import java.nio.ByteBuffer;
import java.util.HashMap;
import jpcsp.Emulator;
import jpcsp.Memory;
import jpcsp.HLE.Modules;
import jpcsp.HLE.modules.HLEModule;
import jpcsp.media.MediaEngine;
import jpcsp.media.PacketChannel;
import jpcsp.settings.AbstractBoolSettingsListener;
import org.apache.log4j.Logger;

public class sceMp3 extends HLEModule {

    private static Logger log = Modules.getLogger("sceMp3");

    private class EnableMediaEngineSettingsListerner extends AbstractBoolSettingsListener {

        @Override
        protected void settingsValueChanged(boolean value) {
            setEnableMediaEngine(value);
        }
    }

    @Override
    public String getName() {
        return "sceMp3";
    }

    @Override
    public void start() {
        mp3Map = new HashMap<Integer, Mp3Stream>();
        setSettingsListener("emu.useMediaEngine", new EnableMediaEngineSettingsListerner());
        super.start();
    }

    protected int mp3HandleCount;

    protected HashMap<Integer, Mp3Stream> mp3Map;

    protected static final int compressionFactor = 10;

    protected static final int PSP_MP3_LOOP_NUM_INFINITE = -1;

    protected static final int mp3DecodeDelay = 4000;

    private boolean useMediaEngine = false;

    protected boolean checkMediaEngineState() {
        return useMediaEngine;
    }

    private void setEnableMediaEngine(boolean state) {
        useMediaEngine = state;
    }

    protected int endianSwap(int x) {
        return (x << 24) | ((x << 8) & 0xFF0000) | ((x >> 8) & 0xFF00) | ((x >> 24) & 0xFF);
    }

    public int makeFakeMp3StreamHandle() {
        return 0x0000A300 | (mp3HandleCount++ & 0xFFFF);
    }

    private void delayThread(long startMicros, int delayMicros) {
        long now = Emulator.getClock().microTime();
        int threadDelayMicros = delayMicros - (int) (now - startMicros);
        if (threadDelayMicros > 0) {
            Modules.ThreadManForUserModule.hleKernelDelayThread(threadDelayMicros, false);
        }
    }

    static final int ERROR_MP3_NOT_FOUND = 0;

    @HLEUidClass(moduleMethodUidGenerator = "makeFakeMp3StreamHandle", errorValueOnNotFound = ERROR_MP3_NOT_FOUND)
    protected class Mp3Stream {

        private static final int ME_READ_AHEAD = 7 * 32 * 1024;

        private final long mp3StreamStart;

        private final long mp3StreamEnd;

        private final int mp3Buf;

        private final int mp3BufSize;

        private final int mp3PcmBuf;

        private final int mp3PcmBufSize;

        private int mp3InputFileSize;

        private int mp3InputFileReadPos;

        private int mp3InputBufWritePos;

        private int mp3InputBufSize;

        private int mp3DecodedBytes;

        private int mp3SampleRate;

        private int mp3LoopNum;

        private int mp3BitRate;

        private int mp3MaxSamples;

        private int mp3Channels;

        protected MediaEngine me;

        protected PacketChannel mp3Channel;

        private byte[] mp3PcmBuffer;

        public Mp3Stream(int args) {
            Memory mem = Memory.getInstance();
            mp3StreamStart = mem.read64(args);
            mp3StreamEnd = mem.read64(args + 8);
            mp3Buf = mem.read32(args + 16);
            mp3BufSize = mem.read32(args + 20);
            mp3PcmBuf = mem.read32(args + 24);
            mp3PcmBufSize = mem.read32(args + 28);
            mp3InputFileReadPos = (int) mp3StreamStart;
            mp3InputFileSize = 0;
            mp3InputBufWritePos = mp3InputFileReadPos;
            mp3InputBufSize = 0;
            mp3MaxSamples = mp3PcmBufSize / 4;
            mp3LoopNum = PSP_MP3_LOOP_NUM_INFINITE;
            mp3DecodedBytes = 0;
            if (checkMediaEngineState()) {
                me = new MediaEngine();
                me.setAudioSamplesSize(mp3MaxSamples);
                mp3Channel = new PacketChannel();
            }
            mp3PcmBuffer = new byte[mp3PcmBufSize];
        }

        private void parseMp3FrameHeader() {
            Memory mem = Memory.getInstance();
            int header = endianSwap(mem.read32(mp3Buf));
            mp3Channels = calculateMp3Channels((header >> 6) & 0x3);
            mp3SampleRate = calculateMp3SampleRate((header >> 10) & 0x3);
            mp3BitRate = calculateMp3Bitrate((header >> 12) & 0xF);
        }

        private int calculateMp3Bitrate(int bitVal) {
            switch(bitVal) {
                case 0:
                    return 0;
                case 1:
                    return 32;
                case 2:
                    return 40;
                case 3:
                    return 48;
                case 4:
                    return 56;
                case 5:
                    return 64;
                case 6:
                    return 80;
                case 7:
                    return 96;
                case 8:
                    return 112;
                case 9:
                    return 128;
                case 10:
                    return 160;
                case 11:
                    return 192;
                case 12:
                    return 224;
                case 13:
                    return 256;
                case 14:
                    return 320;
                default:
                    return -1;
            }
        }

        private int calculateMp3SampleRate(int bitVal) {
            if (bitVal == 0) {
                return 44100;
            } else if (bitVal == 1) {
                return 48000;
            } else if (bitVal == 2) {
                return 32000;
            } else {
                return 0;
            }
        }

        private int calculateMp3Channels(int bitVal) {
            if (bitVal == 0 || bitVal == 1 || bitVal == 2) {
                return 2;
            } else if (bitVal == 3) {
                return 1;
            } else {
                return 0;
            }
        }

        /**
         * @return number of bytes that can be read from the buffer
         */
        public int getMp3AvailableReadSize() {
            return mp3InputBufSize;
        }

        /**
         * @return number of bytes that can be written into the buffer
         */
        public int getMp3AvailableWriteSize() {
            return mp3BufSize - getMp3AvailableReadSize();
        }

        /**
         * @return number of bytes that can be written sequentially into the buffer
         */
        public int getMp3AvailableSequentialWriteSize() {
            return (mp3BufSize - mp3InputBufWritePos);
        }

        /**
         * Read bytes from the buffer.
         *
         * @param size    number of byte read
         * @return        number of bytes actually read
         */
        private int consumeRead(int size) {
            size = Math.min(size, getMp3AvailableReadSize());
            mp3InputBufSize -= size;
            mp3InputFileReadPos += size;
            return size;
        }

        /**
         * Write bytes into the buffer.
         *
         * @param size    number of byte written
         * @return        number of bytes actually written
         */
        private int consumeWrite(int size) {
            size = Math.min(size, getMp3AvailableWriteSize());
            mp3InputBufSize += size;
            mp3InputBufWritePos += size;
            if (mp3InputBufWritePos >= mp3BufSize) {
                mp3InputBufWritePos -= mp3BufSize;
            }
            return size;
        }

        public void init() {
            parseMp3FrameHeader();
            if (checkMediaEngineState()) {
                me.finish();
            }
        }

        private boolean checkMediaEngineChannel() {
            if (checkMediaEngineState()) {
                if (mp3Channel.length() < ME_READ_AHEAD) {
                    int neededLength = ME_READ_AHEAD - mp3Channel.length();
                    if (getMp3AvailableWriteSize() < neededLength) {
                        consumeRead(neededLength - getMp3AvailableWriteSize());
                    }
                    return false;
                }
            }
            return true;
        }

        public int decode() {
            if (checkMediaEngineState()) {
                if (checkMediaEngineChannel()) {
                    if (me.getContainer() == null) {
                        me.init(mp3Channel, false, true);
                    }
                    me.stepAudio(0);
                    mp3DecodedBytes = copySamplesToMem(mp3PcmBuf, mp3PcmBufSize, mp3PcmBuffer);
                } else {
                    mp3DecodedBytes = 0;
                }
            } else {
                mp3DecodedBytes = mp3MaxSamples * 4;
                int mp3BufReadConsumed = Math.min(mp3DecodedBytes / compressionFactor, getMp3AvailableReadSize());
                consumeRead(mp3BufReadConsumed);
            }
            return mp3DecodedBytes;
        }

        public boolean isStreamDataNeeded() {
            if (checkMediaEngineState()) {
                checkMediaEngineChannel();
                if (mp3Channel.length() >= ME_READ_AHEAD) {
                    return false;
                }
            }
            return getMp3AvailableWriteSize() > 0;
        }

        public boolean isStreamDataEnd() {
            return (mp3InputFileSize >= (int) mp3StreamEnd);
        }

        public int addMp3StreamData(int size) {
            if (checkMediaEngineState()) {
                mp3Channel.write(getMp3BufWriteAddr(), size);
            }
            mp3InputFileSize += size;
            return consumeWrite(size);
        }

        private int copySamplesToMem(int address, int maxLength, byte[] buffer) {
            Memory mem = Memory.getInstance();
            int bytes = me.getCurrentAudioSamples(buffer);
            if (bytes > 0) {
                mem.copyToMemory(address, ByteBuffer.wrap(buffer, 0, bytes), bytes);
            }
            return bytes;
        }

        public int getMp3BufWriteAddr() {
            return getMp3BufAddr() + getMp3InputBufWritePos();
        }

        public int getMp3LoopNum() {
            return mp3LoopNum;
        }

        public int getMp3BufAddr() {
            return mp3Buf;
        }

        public int getMp3PcmBufAddr() {
            return mp3PcmBuf;
        }

        public int getMp3PcmBufSize() {
            return mp3PcmBufSize;
        }

        public int getMp3InputFileReadPos() {
            return mp3InputFileReadPos;
        }

        public int getMp3InputBufWritePos() {
            return mp3InputBufWritePos;
        }

        public int getMp3BufSize() {
            return mp3BufSize;
        }

        public int getMp3DecodedSamples() {
            return mp3DecodedBytes / 4;
        }

        public int getMp3MaxSamples() {
            return mp3MaxSamples;
        }

        public int getMp3BitRate() {
            return mp3BitRate;
        }

        public int getMp3ChannelNum() {
            return mp3Channels;
        }

        public int getMp3SamplingRate() {
            return mp3SampleRate;
        }

        public int getMp3InputFileSize() {
            return mp3InputFileSize;
        }

        public void setMp3LoopNum(int n) {
            mp3LoopNum = n;
        }

        public void setMp3BufCurrentPos(int pos) {
            mp3InputFileReadPos = pos;
            mp3InputBufWritePos = pos;
            mp3InputBufSize = 0;
            mp3InputFileSize = 0;
            mp3DecodedBytes = 0;
        }

        @Override
        public String toString() {
            return String.format("Mp3Stream(maxSize=%d, availableSize=%d, readPos=%d, writePos=%d)", mp3BufSize, mp3InputBufSize, mp3InputFileReadPos, mp3InputBufWritePos);
        }
    }

    @HLEFunction(nid = 0x07EC321A, version = 150, checkInsideInterrupt = true)
    public Mp3Stream sceMp3ReserveMp3Handle(TPointer mp3args) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3ReserveMp3Handle mp3args=%s", mp3args));
        }
        Mp3Stream mp3Stream = new Mp3Stream(mp3args.getAddress());
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3ReserveMp3Handle returning %s", mp3Stream));
        }
        return mp3Stream;
    }

    @HLEFunction(nid = 0x0DB149F4, version = 150, checkInsideInterrupt = true)
    public int sceMp3NotifyAddStreamData(Mp3Stream mp3Stream, int size) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3NotifyAddStreamData mp3stream=%s, size=%d", mp3Stream, size));
        }
        mp3Stream.addMp3StreamData(size);
        return 0;
    }

    @HLEFunction(nid = 0x2A368661, version = 150, checkInsideInterrupt = true)
    public int sceMp3ResetPlayPosition(Mp3Stream mp3Stream) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3ResetPlayPosition mp3stream=%s", mp3Stream));
        }
        mp3Stream.setMp3BufCurrentPos(0);
        return 0;
    }

    @HLEFunction(nid = 0x35750070, version = 150, checkInsideInterrupt = true)
    public int sceMp3InitResource() {
        if (log.isInfoEnabled()) {
            log.info("sceMp3InitResource");
        }
        return 0;
    }

    @HLEFunction(nid = 0x3C2FA058, version = 150, checkInsideInterrupt = true)
    public int sceMp3TermResource() {
        if (log.isInfoEnabled()) {
            log.info("sceMp3TermResource");
        }
        return 0;
    }

    @HLEFunction(nid = 0x3CEF484F, version = 150, checkInsideInterrupt = true)
    public int sceMp3SetLoopNum(Mp3Stream mp3Stream, int loopNbr) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3SetLoopNum mp3stream=%s, loopNbr=%d", mp3Stream, loopNbr));
        }
        mp3Stream.setMp3LoopNum(loopNbr);
        return 0;
    }

    @HLEFunction(nid = 0x44E07129, version = 150, checkInsideInterrupt = true)
    public int sceMp3Init(Mp3Stream mp3Stream) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3Init mp3stream=%s", mp3Stream));
        }
        mp3Stream.init();
        if (log.isInfoEnabled()) {
            log.info(String.format("Initializing Mp3 data: channels=%d, samplerate=%dkHz, bitrate=%dkbps.", mp3Stream.getMp3ChannelNum(), mp3Stream.getMp3SamplingRate(), mp3Stream.getMp3BitRate()));
        }
        return 0;
    }

    @HLEFunction(nid = 0x7F696782, version = 150, checkInsideInterrupt = true)
    public int sceMp3GetMp3ChannelNum(Mp3Stream mp3Stream) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3GetMp3ChannelNum mp3stream=%s", mp3Stream));
        }
        return mp3Stream.getMp3ChannelNum();
    }

    @HLEFunction(nid = 0x8F450998, version = 150, checkInsideInterrupt = true)
    public int sceMp3GetSamplingRate(Mp3Stream mp3Stream) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3GetSamplingRate mp3stream=%s", mp3Stream));
        }
        return mp3Stream.getMp3SamplingRate();
    }

    @HLEFunction(nid = 0xA703FE0F, version = 150, checkInsideInterrupt = true)
    public int sceMp3GetInfoToAddStreamData(Mp3Stream mp3Stream, TPointer32 mp3BufPtr, TPointer32 mp3BufToWritePtr, TPointer32 mp3PosPtr) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3GetInfoToAddStreamData mp3stream=%s, mp3BufAddr=%s, mp3BufToWriteAddr=%s, mp3PosAddr=%s", mp3Stream, mp3BufPtr, mp3BufToWritePtr, mp3PosPtr));
        }
        mp3BufPtr.setValue(mp3Stream.isStreamDataEnd() ? 0 : mp3Stream.getMp3BufWriteAddr());
        mp3BufToWritePtr.setValue(mp3Stream.isStreamDataEnd() ? 0 : mp3Stream.getMp3AvailableSequentialWriteSize());
        mp3PosPtr.setValue(mp3Stream.getMp3InputFileSize());
        return 0;
    }

    @HLEFunction(nid = 0xD021C0FB, version = 150, checkInsideInterrupt = true)
    public int sceMp3Decode(Mp3Stream mp3Stream, TPointer32 outPcmPtr) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3Decode mp3stream=%s, outPcmAddr=%s", mp3Stream, outPcmPtr));
        }
        int pcmSamples = 0;
        long startTime = Emulator.getClock().microTime();
        pcmSamples = mp3Stream.decode();
        outPcmPtr.setValue(mp3Stream.getMp3PcmBufAddr());
        delayThread(startTime, mp3DecodeDelay);
        return pcmSamples;
    }

    @HLEFunction(nid = 0xD0A56296, version = 150, checkInsideInterrupt = true)
    public boolean sceMp3CheckStreamDataNeeded(Mp3Stream mp3Stream) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3CheckStreamDataNeeded mp3stream=%s", mp3Stream));
        }
        return mp3Stream.isStreamDataNeeded();
    }

    @HLEFunction(nid = 0xF5478233, version = 150, checkInsideInterrupt = true)
    public int sceMp3ReleaseMp3Handle(Mp3Stream mp3Stream) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3ReleaseMp3Handle mp3stream=%s", mp3Stream));
        }
        HLEUidObjectMapping.removeObject(mp3Stream);
        return 0;
    }

    @HLEFunction(nid = 0x354D27EA, version = 150)
    public int sceMp3GetSumDecodedSample(Mp3Stream mp3Stream) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3GetSumDecodedSample mp3Stream=%s", mp3Stream));
        }
        return mp3Stream.getMp3DecodedSamples();
    }

    @HLEFunction(nid = 0x87677E40, version = 150, checkInsideInterrupt = true)
    public int sceMp3GetBitRate(Mp3Stream mp3Stream) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3GetBitRate mp3stream=%s", mp3Stream));
        }
        return mp3Stream.getMp3BitRate();
    }

    @HLEFunction(nid = 0x87C263D1, version = 150, checkInsideInterrupt = true)
    public int sceMp3GetMaxOutputSample(Mp3Stream mp3Stream) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3GetMaxOutputSample mp3stream=%s", mp3Stream));
        }
        return mp3Stream.getMp3MaxSamples();
    }

    @HLEFunction(nid = 0xD8F54A51, version = 150, checkInsideInterrupt = true)
    public int sceMp3GetLoopNum(Mp3Stream mp3Stream) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("sceMp3GetLoopNum mp3stream=%s", mp3Stream));
        }
        return mp3Stream.getMp3LoopNum();
    }
}
