package com.koutra.dist.proc.sink;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.log4j.Logger;
import com.koutra.dist.proc.model.ContentType;
import com.koutra.dist.proc.model.IFaucet;
import com.koutra.dist.proc.model.IPipelineItem;
import com.koutra.dist.proc.model.XformationException;
import com.koutra.dist.proc.util.ByteArrayUtil;

/**
 * A stream zip mux sink consumes a sequence of byte streams generated by a template
 * item and stores them into a zip file with a single zip file entry for each
 * individual byte stream. The user specifies the file in their local file system.
 * 
 * @author Pafsanias Ftakas
 */
public class StreamZipMuxSink extends AbstractFileOrStreamMuxSink {

    private static final Logger logger = Logger.getLogger(StreamZipMuxSink.class);

    protected static class DequePayload {

        public IFaucet faucet;

        public InputStream inputStream;

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DequePayload)) return false;
            DequePayload other = (DequePayload) obj;
            if (inputStream == null && other.inputStream != null) return false;
            return faucet.equals(other.faucet) && inputStream.equals(other.inputStream);
        }
    }

    protected BlockingDeque<DequePayload> deque;

    protected ZipOutputStream outputStream;

    /**
	 * @deprecated Use any of the initializing constructors instead.
	 */
    public StreamZipMuxSink() {
    }

    /**
	 * Initializing constructor for the Stream type.
	 * @param id the ID of the sink.
	 * @param os the output stream to write to.
	 */
    public StreamZipMuxSink(String id, ZipOutputStream os) {
        super(id);
        this.deque = new LinkedBlockingDeque<DequePayload>();
        this.outputStream = os;
    }

    /**
	 * Initializing constructor for the File type.
	 * @param id the ID of the sink.
	 * @param path the path to the file to write to.
	 */
    public StreamZipMuxSink(String id, String path) {
        super(id, path);
        this.deque = new LinkedBlockingDeque<DequePayload>();
        this.outputStream = null;
    }

    /**
	 * Implementation of the <code>ISink</code> interface.
	 * 
	 * @param contentType the type that we want this sink to support.
	 * @return true iff this sink supports the content type argument.
	 */
    @Override
    public boolean supportsInput(ContentType contentType) {
        switch(contentType) {
            case ByteStream:
                return true;
            case CharStream:
            case XML:
            case ResultSet:
            default:
                return false;
        }
    }

    /**
	 * Override the implementation in the abstract sink to add a check that the faucet
	 * supports the proper content type.
	 */
    @Override
    protected void checkFaucetValidity(IFaucet faucet) {
        super.checkFaucetValidity(faucet);
        if (!faucet.supportsOutput(ContentType.ByteStream)) throw new IllegalArgumentException("Faucet '" + faucet.getId() + "' must support the ByteStream content type.");
    }

    /**
	 * Implementation of the <code>ISink</code> interface.
	 */
    @Override
    public void registerSource(Object source) {
        InputStream inputStream = (InputStream) source;
        DequePayload payload = new DequePayload();
        payload.faucet = faucet;
        payload.inputStream = inputStream;
        if (deque.contains(payload)) return;
        if (logger.isTraceEnabled()) logger.trace("Registering input stream: " + inputStream + " with the mux deque");
        while (true) {
            try {
                deque.putLast(payload);
                break;
            } catch (InterruptedException e) {
            }
        }
        if (inputStream != null) {
            if (faucet instanceof IPipelineItem) {
                ((IPipelineItem) faucet).consume(this);
            }
        }
    }

    /**
	 * Implementation of the <code>ISink</code> interface.
	 */
    @Override
    public String dumpPipeline() {
        DequePayload payload = deque.peekFirst();
        return getClass().getName() + ": " + (payload == null ? "null" : payload.inputStream) + "->" + outputStream;
    }

    /**
	 * Implementation of the <code>ISink</code> interface.
	 */
    @Override
    public void dispose() {
        switch(type) {
            case File:
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new XformationException("Unable to close output stream", e);
                }
                break;
            case Stream:
                break;
        }
    }

    /**
	 * Implementation of the <code>ISink</code> interface.
	 */
    @Override
    public void consume() {
        if (!hookedUp && faucetTemplate == null) throw new XformationException("Sink has not been set up correctly: " + "faucet has not been set");
        switch(type) {
            case File:
                try {
                    outputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
                } catch (FileNotFoundException e) {
                    throw new XformationException("Unable to create output stream", e);
                }
                break;
            case Stream:
                break;
        }
        if (!hookedUp) {
            if (faucetTemplate instanceof IPipelineItem) {
                ((IPipelineItem) faucetTemplate).consume(this);
            }
        }
        try {
            int count;
            byte[] buffer = new byte[8 * 1024];
            int counter = 0;
            while (true) {
                DequePayload payload = null;
                try {
                    payload = deque.takeFirst();
                } catch (InterruptedException ie) {
                }
                if (payload == null) break;
                IFaucet faucet = payload.faucet;
                InputStream inputStream = payload.inputStream;
                if (logger.isTraceEnabled()) logger.trace("Removed input stream: " + inputStream + " from the deque.");
                if (inputStream == null) break;
                if (logger.isTraceEnabled()) logger.trace("Using the reader " + inputStream + " in the mux sink");
                ZipEntry entry = new ZipEntry("entry" + counter++);
                outputStream.putNextEntry(entry);
                while ((count = inputStream.read(buffer)) != -1) {
                    if (logger.isTraceEnabled()) {
                        String readInput = ByteArrayUtil.getHexDump(buffer, 0, count);
                        logger.trace("Read " + readInput + " from input stream " + inputStream);
                    }
                    outputStream.write(buffer, 0, count);
                }
                outputStream.closeEntry();
                faucet.dispose();
            }
            outputStream.close();
            faucetTemplate.dispose();
        } catch (IOException ioe) {
            logger.error("Error while consuming input", ioe);
            throw new XformationException("Unable to transform stream", ioe);
        }
    }

    /**
	 * Override the <code>Streamable</code> implementation in order to deserialize
	 * local members.
	 */
    @Override
    public void readFrom(DataInputStream in) throws IOException, IllegalAccessException, InstantiationException {
        super.readFrom(in);
        this.deque = new LinkedBlockingDeque<DequePayload>();
        this.outputStream = null;
    }
}
