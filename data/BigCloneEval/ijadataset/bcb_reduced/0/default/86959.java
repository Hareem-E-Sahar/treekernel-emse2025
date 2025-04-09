import org.jfree.data.time.TimeSeries;
import java.io.DataInputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;

public class Receiver {

    private static final int CRC_BYTES = Long.SIZE / Byte.SIZE;

    private static final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;

    private final AtomicBoolean running = new AtomicBoolean(true);

    private final TimeSeries plotter;

    public Receiver(final TimeSeries plotter) {
        this.plotter = plotter;
    }

    public Thread receiverThread(final Socket s, final CountDownLatch abortLatch) {
        final Thread theThread = new Thread() {

            @Override
            public void run() {
                final Counter counter = new Counter("TRANSMIT", plotter);
                counter.start();
                try {
                    final DataInputStream dis = new DataInputStream(s.getInputStream());
                    while (!Thread.interrupted()) {
                        if (!running.get()) {
                            Thread.sleep(1000);
                            continue;
                        }
                        final int nextPacketSize = dis.readInt();
                        counter.addBytes(INTEGER_BYTES);
                        final byte[] packet = new byte[nextPacketSize];
                        dis.readFully(packet);
                        counter.addBytes(nextPacketSize);
                        final long otherSideCrc = dis.readLong();
                        counter.addBytes(CRC_BYTES);
                        final CRC32 crc = new CRC32();
                        crc.update(packet);
                        if (otherSideCrc != crc.getValue()) {
                            throw new RuntimeException("CRC values don't match: this: " + crc.getValue() + ", other: " + otherSideCrc);
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("Receiver thread interrupted!");
                } catch (Exception e) {
                    System.err.println("Unexpected error in receiver thread: " + e.getMessage());
                } finally {
                    abortLatch.countDown();
                    counter.interrupt();
                }
            }
        };
        return theThread;
    }

    public void toggle() {
        running.set(!running.get());
    }
}
