import java.io.File;
import java.io.IOException;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Random;
import org.xiph.speex.OggCrc;
import org.xiph.speex.NbEncoder;
import org.xiph.speex.SbEncoder;
import org.xiph.speex.SpeexDecoder;
import org.xiph.speex.AudioFileWriter;
import org.xiph.speex.PcmWaveWriter;
import org.xiph.speex.RawWriter;

/**
 * Java Speex Command Line Decoder.
 * 
 * Decodes SPX files created by Speex's speexenc utility to WAV entirely in pure java.
 * Currently this code has been updated to be compatible with release 1.0.3.
 *
 * NOTE!!! A number of advanced options are NOT supported. 
 * 
 * --  DTX implemented but untested.
 * --  Packet loss support implemented but untested.
 * --  SPX files with more than one comment. 
 * --  Can't force decoder to run at another rate, mode, or channel count. 
 * 
 * @author Jim Lawrence, helloNetwork.com
 * @author Marc Gimpel, Wimba S.A. (mgimpel@horizonwimba.com)
 * @version $Revision$
 */
public class JSpeexDec {

    /** Version of the Speex Encoder */
    public static final String VERSION = "Java Speex Command Line Decoder v0.9.7 ($Revision$)";

    /** Copyright display String */
    public static final String COPYRIGHT = "Copyright (C) 2002-2004 Wimba S.A.";

    /** Print level for messages : Print debug information */
    public static final int DEBUG = 0;

    /** Print level for messages : Print basic information */
    public static final int INFO = 1;

    /** Print level for messages : Print only warnings and errors */
    public static final int WARN = 2;

    /** Print level for messages : Print only errors */
    public static final int ERROR = 3;

    /** Print level for messages */
    protected int printlevel = INFO;

    /** File format for input or output audio file: Raw */
    public static final int FILE_FORMAT_RAW = 0;

    /** File format for input or output audio file: Ogg */
    public static final int FILE_FORMAT_OGG = 1;

    /** File format for input or output audio file: Wave */
    public static final int FILE_FORMAT_WAVE = 2;

    /** Defines File format for input audio file (Raw, Ogg or Wave). */
    protected int srcFormat = FILE_FORMAT_OGG;

    /** Defines File format for output audio file (Raw or Wave). */
    protected int destFormat = FILE_FORMAT_WAVE;

    /** Random number generator for packet loss simulation. */
    protected static Random random = new Random();

    /** Speex Decoder */
    protected SpeexDecoder speexDecoder;

    /** Defines whether or not the perceptual enhancement is used. */
    protected boolean enhanced = true;

    /** If input is raw, defines the decoder mode (0=NB, 1=WB and 2-UWB). */
    private int mode = 0;

    /** If input is raw, defines the quality setting used by the encoder. */
    private int quality = 8;

    /** If input is raw, defines the number of frmaes per packet. */
    private int nframes = 1;

    /** If input is raw, defines the sample rate of the audio. */
    private int sampleRate = -1;

    /** */
    private float vbr_quality = -1;

    /** */
    private boolean vbr = false;

    /** If input is raw, defines th number of channels (1=mono, 2=stereo). */
    private int channels = 1;

    /** The percentage of packets to lose in the packet loss simulation. */
    private int loss = 0;

    /** The audio input file */
    protected String srcFile;

    /** The audio output file */
    protected String destFile;

    /**
   * Builds a plain JSpeex Decoder with default values.
   */
    public JSpeexDec() {
    }

    /**
   * Command line entrance:
   * <pre>
   * Usage: JSpeexDec [options] input_file output_file
   * </pre>
   * @param args Command line parameters.
   * @exception IOException
   */
    public static void main(final String[] args) throws IOException {
        JSpeexDec decoder = new JSpeexDec();
        if (decoder.parseArgs(args)) {
            decoder.decode();
        }
    }

    /**
   * Parse the command line arguments.
   * @param args Command line parameters.
   * @return true if the parsed arguments are sufficient to run the decoder.
   */
    public boolean parseArgs(final String[] args) {
        if (args.length < 2) {
            if (args.length == 1 && (args[0].equals("-v") || args[0].equals("--version"))) {
                version();
                return false;
            }
            usage();
            return false;
        }
        srcFile = args[args.length - 2];
        destFile = args[args.length - 1];
        if (srcFile.toLowerCase().endsWith(".spx")) {
            srcFormat = FILE_FORMAT_OGG;
        } else if (srcFile.toLowerCase().endsWith(".wav")) {
            srcFormat = FILE_FORMAT_WAVE;
        } else {
            srcFormat = FILE_FORMAT_RAW;
        }
        if (destFile.toLowerCase().endsWith(".wav")) {
            destFormat = FILE_FORMAT_WAVE;
        } else {
            destFormat = FILE_FORMAT_RAW;
        }
        for (int i = 0; i < args.length - 2; i++) {
            if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--help")) {
                usage();
                return false;
            } else if (args[i].equalsIgnoreCase("-v") || args[i].equalsIgnoreCase("--version")) {
                version();
                return false;
            } else if (args[i].equalsIgnoreCase("--verbose")) {
                printlevel = DEBUG;
            } else if (args[i].equalsIgnoreCase("--quiet")) {
                printlevel = WARN;
            } else if (args[i].equalsIgnoreCase("--enh")) {
                enhanced = true;
            } else if (args[i].equalsIgnoreCase("--no-enh")) {
                enhanced = false;
            } else if (args[i].equalsIgnoreCase("--packet-loss")) {
                try {
                    loss = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    usage();
                    return false;
                }
            } else if (args[i].equalsIgnoreCase("-n") || args[i].equalsIgnoreCase("-nb") || args[i].equalsIgnoreCase("--narrowband")) {
                mode = 0;
            } else if (args[i].equalsIgnoreCase("-w") || args[i].equalsIgnoreCase("-wb") || args[i].equalsIgnoreCase("--wideband")) {
                mode = 1;
            } else if (args[i].equalsIgnoreCase("-u") || args[i].equalsIgnoreCase("-uwb") || args[i].equalsIgnoreCase("--ultra-wideband")) {
                mode = 2;
            } else if (args[i].equalsIgnoreCase("-q") || args[i].equalsIgnoreCase("--quality")) {
                try {
                    vbr_quality = Float.parseFloat(args[++i]);
                    quality = (int) vbr_quality;
                } catch (NumberFormatException e) {
                    usage();
                    return false;
                }
            } else if (args[i].equalsIgnoreCase("--nframes")) {
                try {
                    nframes = Integer.parseInt(args[++i]);
                } catch (NumberFormatException e) {
                    usage();
                    return false;
                }
            } else if (args[i].equalsIgnoreCase("--vbr")) {
                vbr = true;
            } else if (args[i].equalsIgnoreCase("--stereo")) {
                channels = 2;
            } else {
                usage();
                return false;
            }
        }
        if (sampleRate < 0) {
            switch(mode) {
                case 0:
                    sampleRate = 8000;
                    break;
                case 1:
                    sampleRate = 16000;
                    break;
                case 2:
                    sampleRate = 32000;
                    break;
                default:
                    sampleRate = 8000;
            }
        }
        return true;
    }

    /**
   * Prints the usage guidelines.
   */
    public static void usage() {
        version();
        System.out.println("Usage: JSpeexDec [options] input_file output_file");
        System.out.println("Where:");
        System.out.println("  input_file can be:");
        System.out.println("    filename.spx  an Ogg Speex file");
        System.out.println("    filename.wav  a Wave Speex file (beta!!!)");
        System.out.println("    filename.*    a raw Speex file");
        System.out.println("  output_file can be:");
        System.out.println("    filename.wav  a PCM wav file");
        System.out.println("    filename.*    a raw PCM file (any extension other than .wav)");
        System.out.println("Options: -h, --help     This help");
        System.out.println("         -v, --version    Version information");
        System.out.println("         --verbose        Print detailed information");
        System.out.println("         --quiet          Print minimal information");
        System.out.println("         --enh            Enable perceptual enhancement (default)");
        System.out.println("         --no-enh         Disable perceptual enhancement");
        System.out.println("         --packet-loss n  Simulate n % random packet loss");
        System.out.println("         if the input file is raw Speex (not Ogg Speex)");
        System.out.println("         -n, -nb          Narrowband (8kHz)");
        System.out.println("         -w, -wb          Wideband (16kHz)");
        System.out.println("         -u, -uwb         Ultra-Wideband (32kHz)");
        System.out.println("         --quality n      Encoding quality (0-10) default 8");
        System.out.println("         --nframes n      Number of frames per Ogg packet, default 1");
        System.out.println("         --vbr            Enable varible bit-rate (VBR)");
        System.out.println("         --stereo         Consider input as stereo");
        System.out.println("More information is available from: http://jspeex.sourceforge.net/");
        System.out.println("This code is a Java port of the Speex codec: http://www.speex.org/");
    }

    /**
   * Prints the version.
   */
    public static void version() {
        System.out.println(VERSION);
        System.out.println("using " + SpeexDecoder.VERSION);
        System.out.println(COPYRIGHT);
    }

    /**
   * Decodes a Speex file to PCM.
   * @exception IOException
   */
    public void decode() throws IOException {
        decode(new File(srcFile), new File(destFile));
    }

    /**
   * Decodes a Speex file to PCM.
   * @param srcPath
   * @param destPath
   * @exception IOException
   */
    public void decode(final File srcPath, final File destPath) throws IOException {
        byte[] header = new byte[2048];
        byte[] payload = new byte[65536];
        byte[] decdat = new byte[44100 * 2 * 2];
        final int WAV_HEADERSIZE = 8;
        final short WAVE_FORMAT_SPEEX = (short) 0xa109;
        final String RIFF = "RIFF";
        final String WAVE = "WAVE";
        final String FORMAT = "fmt ";
        final String DATA = "data";
        final int OGG_HEADERSIZE = 27;
        final int OGG_SEGOFFSET = 26;
        final String OGGID = "OggS";
        int segments = 0;
        int curseg = 0;
        int bodybytes = 0;
        int decsize = 0;
        int packetNo = 0;
        if (printlevel <= INFO) version();
        if (printlevel <= DEBUG) System.out.println("");
        if (printlevel <= DEBUG) System.out.println("Input File: " + srcPath);
        speexDecoder = new SpeexDecoder();
        DataInputStream dis = new DataInputStream(new FileInputStream(srcPath));
        AudioFileWriter writer = null;
        int origchksum;
        int chksum;
        try {
            while (true) {
                if (srcFormat == FILE_FORMAT_OGG) {
                    dis.readFully(header, 0, OGG_HEADERSIZE);
                    origchksum = readInt(header, 22);
                    header[22] = 0;
                    header[23] = 0;
                    header[24] = 0;
                    header[25] = 0;
                    chksum = OggCrc.checksum(0, header, 0, OGG_HEADERSIZE);
                    if (!OGGID.equals(new String(header, 0, 4))) {
                        System.err.println("missing ogg id!");
                        return;
                    }
                    segments = header[OGG_SEGOFFSET] & 0xFF;
                    dis.readFully(header, OGG_HEADERSIZE, segments);
                    chksum = OggCrc.checksum(chksum, header, OGG_HEADERSIZE, segments);
                    for (curseg = 0; curseg < segments; curseg++) {
                        bodybytes = header[OGG_HEADERSIZE + curseg] & 0xFF;
                        if (bodybytes == 255) {
                            System.err.println("sorry, don't handle 255 sizes!");
                            return;
                        }
                        dis.readFully(payload, 0, bodybytes);
                        chksum = OggCrc.checksum(chksum, payload, 0, bodybytes);
                        if (packetNo == 0) {
                            if (readSpeexHeader(payload, 0, bodybytes)) {
                                if (printlevel <= DEBUG) {
                                    System.out.println("File Format: Ogg Speex");
                                    System.out.println("Sample Rate: " + sampleRate);
                                    System.out.println("Channels: " + channels);
                                    System.out.println("Encoder mode: " + (mode == 0 ? "Narrowband" : (mode == 1 ? "Wideband" : "UltraWideband")));
                                    System.out.println("Frames per packet: " + nframes);
                                }
                                if (destFormat == FILE_FORMAT_WAVE) {
                                    writer = new PcmWaveWriter(speexDecoder.getSampleRate(), speexDecoder.getChannels());
                                    if (printlevel <= DEBUG) {
                                        System.out.println("");
                                        System.out.println("Output File: " + destPath);
                                        System.out.println("File Format: PCM Wave");
                                        System.out.println("Perceptual Enhancement: " + enhanced);
                                    }
                                } else {
                                    writer = new RawWriter();
                                    if (printlevel <= DEBUG) {
                                        System.out.println("");
                                        System.out.println("Output File: " + destPath);
                                        System.out.println("File Format: Raw Audio");
                                        System.out.println("Perceptual Enhancement: " + enhanced);
                                    }
                                }
                                writer.open(destPath);
                                writer.writeHeader(null);
                                packetNo++;
                            } else {
                                packetNo = 0;
                            }
                        } else if (packetNo == 1) {
                            packetNo++;
                        } else {
                            if (loss > 0 && random.nextInt(100) < loss) {
                                speexDecoder.processData(null, 0, bodybytes);
                                for (int i = 1; i < nframes; i++) {
                                    speexDecoder.processData(true);
                                }
                            } else {
                                speexDecoder.processData(payload, 0, bodybytes);
                                for (int i = 1; i < nframes; i++) {
                                    speexDecoder.processData(false);
                                }
                            }
                            if ((decsize = speexDecoder.getProcessedData(decdat, 0)) > 0) {
                                writer.writePacket(decdat, 0, decsize);
                            }
                            packetNo++;
                        }
                    }
                    if (chksum != origchksum) throw new IOException("Ogg CheckSums do not match");
                } else {
                    if (packetNo == 0) {
                        if (srcFormat == FILE_FORMAT_WAVE) {
                            dis.readFully(header, 0, WAV_HEADERSIZE + 4);
                            if (!RIFF.equals(new String(header, 0, 4)) && !WAVE.equals(new String(header, 8, 4))) {
                                System.err.println("Not a WAVE file");
                                return;
                            }
                            dis.readFully(header, 0, WAV_HEADERSIZE);
                            String chunk = new String(header, 0, 4);
                            int size = readInt(header, 4);
                            while (!chunk.equals(DATA)) {
                                dis.readFully(header, 0, size);
                                if (chunk.equals(FORMAT)) {
                                    if (readShort(header, 0) != WAVE_FORMAT_SPEEX) {
                                        System.err.println("Not a Wave Speex file");
                                        return;
                                    }
                                    channels = readShort(header, 2);
                                    sampleRate = readInt(header, 4);
                                    bodybytes = readShort(header, 12);
                                    if (readShort(header, 16) < 82) {
                                        System.err.println("Possibly corrupt Speex Wave file.");
                                        return;
                                    }
                                    readSpeexHeader(header, 20, 80);
                                    if (printlevel <= DEBUG) {
                                        System.out.println("File Format: Wave Speex");
                                        System.out.println("Sample Rate: " + sampleRate);
                                        System.out.println("Channels: " + channels);
                                        System.out.println("Encoder mode: " + (mode == 0 ? "Narrowband" : (mode == 1 ? "Wideband" : "UltraWideband")));
                                        System.out.println("Frames per packet: " + nframes);
                                    }
                                }
                                dis.readFully(header, 0, WAV_HEADERSIZE);
                                chunk = new String(header, 0, 4);
                                size = readInt(header, 4);
                            }
                            if (printlevel <= DEBUG) System.out.println("Data size: " + size);
                        } else {
                            if (printlevel <= DEBUG) {
                                System.out.println("File Format: Raw Speex");
                                System.out.println("Sample Rate: " + sampleRate);
                                System.out.println("Channels: " + channels);
                                System.out.println("Encoder mode: " + (mode == 0 ? "Narrowband" : (mode == 1 ? "Wideband" : "UltraWideband")));
                                System.out.println("Frames per packet: " + nframes);
                            }
                            speexDecoder.init(mode, sampleRate, channels, enhanced);
                            if (!vbr) {
                                switch(mode) {
                                    case 0:
                                        bodybytes = NbEncoder.NB_FRAME_SIZE[NbEncoder.NB_QUALITY_MAP[quality]];
                                        break;
                                    case 1:
                                        bodybytes = SbEncoder.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[quality]];
                                        bodybytes += SbEncoder.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[quality]];
                                        break;
                                    case 2:
                                        bodybytes = SbEncoder.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[quality]];
                                        bodybytes += SbEncoder.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[quality]];
                                        bodybytes += SbEncoder.SB_FRAME_SIZE[SbEncoder.UWB_QUALITY_MAP[quality]];
                                        break;
                                    default:
                                        throw new IOException("Illegal mode encoundered.");
                                }
                                bodybytes = (bodybytes + 7) >> 3;
                            } else {
                                bodybytes = 0;
                            }
                        }
                        if (destFormat == FILE_FORMAT_WAVE) {
                            writer = new PcmWaveWriter(sampleRate, channels);
                            if (printlevel <= DEBUG) {
                                System.out.println("");
                                System.out.println("Output File: " + destPath);
                                System.out.println("File Format: PCM Wave");
                                System.out.println("Perceptual Enhancement: " + enhanced);
                            }
                        } else {
                            writer = new RawWriter();
                            if (printlevel <= DEBUG) {
                                System.out.println("");
                                System.out.println("Output File: " + destPath);
                                System.out.println("File Format: Raw Audio");
                                System.out.println("Perceptual Enhancement: " + enhanced);
                            }
                        }
                        writer.open(destPath);
                        writer.writeHeader(null);
                        packetNo++;
                    } else {
                        dis.readFully(payload, 0, bodybytes);
                        if (loss > 0 && random.nextInt(100) < loss) {
                            speexDecoder.processData(null, 0, bodybytes);
                            for (int i = 1; i < nframes; i++) {
                                speexDecoder.processData(true);
                            }
                        } else {
                            speexDecoder.processData(payload, 0, bodybytes);
                            for (int i = 1; i < nframes; i++) {
                                speexDecoder.processData(false);
                            }
                        }
                        if ((decsize = speexDecoder.getProcessedData(decdat, 0)) > 0) {
                            writer.writePacket(decdat, 0, decsize);
                        }
                        packetNo++;
                    }
                }
            }
        } catch (EOFException eof) {
        }
        writer.close();
    }

    /**
   * Reads the header packet.
   * <pre>
   *  0 -  7: speex_string: "Speex   "
   *  8 - 27: speex_version: "speex-1.0"
   * 28 - 31: speex_version_id: 1
   * 32 - 35: header_size: 80
   * 36 - 39: rate
   * 40 - 43: mode: 0=narrowband, 1=wb, 2=uwb
   * 44 - 47: mode_bitstream_version: 4
   * 48 - 51: nb_channels
   * 52 - 55: bitrate: -1
   * 56 - 59: frame_size: 160
   * 60 - 63: vbr
   * 64 - 67: frames_per_packet
   * 68 - 71: extra_headers: 0
   * 72 - 75: reserved1
   * 76 - 79: reserved2
   * </pre>
   * @param packet
   * @param offset
   * @param bytes
   * @return
   */
    private boolean readSpeexHeader(final byte[] packet, final int offset, final int bytes) {
        if (bytes != 80) {
            System.out.println("Oooops");
            return false;
        }
        if (!"Speex   ".equals(new String(packet, offset, 8))) {
            return false;
        }
        mode = packet[40 + offset] & 0xFF;
        sampleRate = readInt(packet, offset + 36);
        channels = readInt(packet, offset + 48);
        nframes = readInt(packet, offset + 64);
        return speexDecoder.init(mode, sampleRate, channels, enhanced);
    }

    /**
   * Converts Little Endian (Windows) bytes to an int (Java uses Big Endian).
   * @param data the data to read.
   * @param offset the offset from which to start reading.
   * @return the integer value of the reassembled bytes.
   */
    protected static int readInt(final byte[] data, final int offset) {
        return (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8) | ((data[offset + 2] & 0xff) << 16) | (data[offset + 3] << 24);
    }

    /**
   * Converts Little Endian (Windows) bytes to an short (Java uses Big Endian).
   * @param data the data to read.
   * @param offset the offset from which to start reading.
   * @return the integer value of the reassembled bytes.
   */
    protected static int readShort(final byte[] data, final int offset) {
        return (data[offset] & 0xff) | (data[offset + 1] << 8);
    }
}
