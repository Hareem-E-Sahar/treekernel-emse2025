package net.sf.jcgm.core;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import net.sf.jcgm.core.ScalingMode.Mode;

/**
 * The main class for Computer Graphics Metafile support.
 * @author xphc (Philippe Cadé)
 * @author BBNT Solutions
 * @version $Id: CGM.java 46 2011-12-14 08:26:44Z phica $
 */
public class CGM implements Cloneable {

    private List<Command> commands;

    private final List<ICommandListener> commandListeners = new ArrayList<ICommandListener>();

    private static final int INITIAL_NUM_COMMANDS = 500;

    public CGM() {
    }

    public CGM(File cgmFile) throws IOException {
        if (cgmFile == null) throw new NullPointerException("unexpected null parameter");
        InputStream inputStream;
        String cgmFilename = cgmFile.getName();
        if (cgmFilename.endsWith(".cgm.gz") || cgmFilename.endsWith(".cgmz")) {
            inputStream = new GZIPInputStream(new FileInputStream(cgmFile));
        } else {
            inputStream = new FileInputStream(cgmFile);
        }
        DataInputStream in = new DataInputStream(new BufferedInputStream(inputStream));
        read(in);
        in.close();
    }

    public void read(DataInput in) throws IOException {
        reset();
        this.commands = new ArrayList<Command>(INITIAL_NUM_COMMANDS);
        while (true) {
            Command c = Command.read(in);
            if (c == null) break;
            for (ICommandListener listener : this.commandListeners) {
                listener.commandProcessed(c);
            }
            c.cleanUpArguments();
            this.commands.add(c);
        }
    }

    /**
	 * Splits a CGM file containing several CGM files into pieces. Each single
	 * CGM file will be extracted to an own file. The name of that file is
	 * provided by the given {@code extractor}.
	 * 
	 * @param cgmFile
	 *            The CGM file to split
	 * @param outputDir
	 *            The output directory to use. Must exist and be writable.
	 * @param extractor
	 *            The extractor in charge of naming the split CGM files
	 * @throws IOException
	 *             If the given CGM file could not be read or there was an error
	 *             splitting the file
	 */
    public static void split(File cgmFile, File outputDir, IBeginMetafileNameExtractor extractor) throws IOException {
        if (cgmFile == null || outputDir == null || extractor == null) throw new NullPointerException("unexpected null argument");
        if (!outputDir.isDirectory()) throw new IllegalArgumentException("outputDir must be a directory");
        if (!outputDir.canWrite()) throw new IllegalArgumentException("outputDir must be writable");
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(cgmFile, "r");
            FileChannel channel = randomAccessFile.getChannel();
            Command c;
            long startPosition = 0;
            long currentPosition = 0;
            String currentFileName = null;
            while ((c = Command.read(randomAccessFile)) != null) {
                if (c instanceof BeginMetafile) {
                    if (currentFileName != null) {
                        dumpToFile(outputDir, extractor, channel, startPosition, currentPosition, currentFileName);
                    }
                    startPosition = currentPosition;
                    BeginMetafile beginMetafile = (BeginMetafile) c;
                    currentFileName = beginMetafile.getFileName();
                }
                currentPosition = randomAccessFile.getFilePointer();
            }
            if (currentFileName != null) {
                dumpToFile(outputDir, extractor, channel, startPosition, currentPosition, currentFileName);
            }
        } finally {
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
    }

    private static void dumpToFile(File outputDir, IBeginMetafileNameExtractor extractor, FileChannel channel, long startPosition, long currentPosition, String currentFileName) throws IOException {
        MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, startPosition, currentPosition - startPosition);
        writeFile(byteBuffer, outputDir, extractor.extractFileName(currentFileName));
        Messages.getInstance().reset();
    }

    /**
	 * Splits a CGM file containing several CGM files into pieces. The given
	 * extractor is in charge of dealing with the extracted CGM file.
	 * 
	 * @param cgmFile
	 *            The CGM file to split
	 * @param extractor
	 *            An extractor that knows what to do with the extracted CGM file
	 * @throws IOException
	 *             If an error happened reading the CGM file
	 * @throws CgmException
	 *             If an error happened during the handling of the extracted CGM
	 *             file
	 */
    public static void split(File cgmFile, ICgmExtractor extractor) throws IOException, CgmException {
        if (cgmFile == null || extractor == null) throw new NullPointerException("unexpected null argument");
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(cgmFile, "r");
            FileChannel channel = randomAccessFile.getChannel();
            Command c;
            long startPosition = 0;
            long currentPosition = 0;
            String currentFileName = null;
            while ((c = Command.read(randomAccessFile)) != null) {
                if (c instanceof BeginMetafile) {
                    if (currentFileName != null) {
                        dumpToStream(extractor, channel, startPosition, currentPosition, currentFileName);
                    }
                    startPosition = currentPosition;
                    BeginMetafile beginMetafile = (BeginMetafile) c;
                    currentFileName = beginMetafile.getFileName();
                }
                currentPosition = randomAccessFile.getFilePointer();
            }
            if (currentFileName != null) {
                dumpToStream(extractor, channel, startPosition, currentPosition, currentFileName);
            }
        } finally {
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
    }

    private static void dumpToStream(ICgmExtractor extractor, FileChannel channel, long startPosition, long currentPosition, String currentFileName) throws IOException, CgmException {
        MappedByteBuffer byteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, startPosition, currentPosition - startPosition);
        byte[] byteArray = new byte[(int) (currentPosition - startPosition)];
        byteBuffer.get(byteArray);
        extractor.handleExtracted(extractor.extractFileName(currentFileName), new ByteArrayInputStream(byteArray), byteArray.length);
        Messages.getInstance().reset();
    }

    /**
	 * Writes the given bytes to a file
	 * 
	 * @param byteBuffer
	 *            The bytes to write to the file
	 * @param outputDir
	 *            The output directory to use, assumed to be existing and
	 *            writable
	 * @param fileName
	 *            The file name to use
	 * @throws IOException
	 *             On I/O error
	 */
    private static void writeFile(ByteBuffer byteBuffer, File outputDir, String fileName) throws IOException {
        File outputFile = new File(outputDir, fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            FileChannel channel = out.getChannel();
            channel.write(byteBuffer);
            out.close();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
	 * Adds the given listener to the list of command listeners
	 * @param listener The listener to add
	 */
    public void addCommandListener(ICommandListener listener) {
        this.commandListeners.add(listener);
    }

    /**
	 * All the command classes with static data need to be reset here
	 */
    private void reset() {
        ColourIndexPrecision.reset();
        ColourModel.reset();
        ColourPrecision.reset();
        ColourSelectionMode.reset();
        ColourValueExtent.reset();
        EdgeWidthSpecificationMode.reset();
        IndexPrecision.reset();
        IntegerPrecision.reset();
        LineWidthSpecificationMode.reset();
        MarkerSizeSpecificationMode.reset();
        RealPrecision.reset();
        RestrictedTextType.reset();
        VDCIntegerPrecision.reset();
        VDCRealPrecision.reset();
        VDCType.reset();
        Messages.getInstance().reset();
    }

    public List<Message> getMessages() {
        return Messages.getInstance();
    }

    public void paint(CGMDisplay d) {
        for (Command c : this.commands) {
            if (filter(c)) {
                c.paint(d);
            }
        }
    }

    private boolean filter(Command c) {
        return true;
    }

    /**
	 * Returns the size of the CGM graphic.
	 * @return The dimension or null if no {@link VDCExtent} command was found.
	 */
    public Dimension getSize() {
        return getSize(96);
    }

    /**
	 * Returns the size of the CGM graphic taking into account a specific DPI setting
	 * @param dpi The DPI value to use
	 * @return The dimension or null if no {@link VDCExtent} command was found.
	 */
    public Dimension getSize(double dpi) {
        Point2D.Double[] extent = extent();
        if (extent == null) return null;
        double factor = 1;
        ScalingMode scalingMode = getScalingMode();
        if (scalingMode != null) {
            Mode mode = scalingMode.getMode();
            if (ScalingMode.Mode.METRIC.equals(mode)) {
                double metricScalingFactor = scalingMode.getMetricScalingFactor();
                if (metricScalingFactor != 0) {
                    factor = (dpi * metricScalingFactor) / 25.4;
                }
            }
        }
        int width = (int) Math.ceil((Math.abs(extent[1].x - extent[0].x) * factor));
        int height = (int) Math.ceil((Math.abs(extent[1].y - extent[0].y) * factor));
        return new Dimension(width, height);
    }

    public Point2D.Double[] extent() {
        for (Command c : this.commands) {
            if (c instanceof VDCExtent) {
                Point2D.Double[] extent = ((VDCExtent) c).extent();
                return extent;
            }
        }
        return null;
    }

    private ScalingMode getScalingMode() {
        for (Command c : this.commands) {
            if (c instanceof ScalingMode) {
                return (ScalingMode) c;
            }
        }
        return null;
    }

    public void showCGMCommands() {
        showCGMCommands(System.out);
    }

    public void showCGMCommands(PrintStream stream) {
        for (Command c : this.commands) {
            stream.println("Command: " + c);
        }
    }

    public List<Command> getCommands() {
        return Collections.unmodifiableList(this.commands);
    }
}
