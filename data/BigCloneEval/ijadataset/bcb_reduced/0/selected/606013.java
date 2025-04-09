package ch.idsia.benchmark.mario.engine;

import ch.idsia.tools.ReplayerOptions;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, sergey@idsia.ch
 * Date: May 5, 2009
 * Time: 9:34:33 PM
 * Package: ch.idsia.utils
 */
public class Recorder {

    private ZipOutputStream zos;

    boolean lastRecordingState = false;

    private Queue<ReplayerOptions.Interval> chunks = new LinkedList<ReplayerOptions.Interval>();

    private ReplayerOptions.Interval chunk;

    private ByteArrayOutputStream byteOut;

    private boolean saveReady = false;

    private boolean canRecord;

    private boolean lazyRec = false;

    public Recorder(String fileName) throws FileNotFoundException {
        if (!fileName.endsWith(".zip")) fileName += ".zip";
        zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
        canRecord = true;
    }

    public Recorder() {
        byteOut = new ByteArrayOutputStream();
        zos = new ZipOutputStream(byteOut);
        canRecord = true;
        lazyRec = true;
    }

    public void saveLastRun(String filename) throws IOException {
        FileOutputStream fo = new FileOutputStream(filename);
        byteOut.writeTo(fo);
    }

    public void createFile(String filename) throws IOException {
        zos.putNextEntry(new ZipEntry(filename));
    }

    public void writeObject(Object object) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(zos);
        oos.writeObject(object);
        oos.flush();
    }

    public void closeFile() throws IOException {
        zos.flush();
        zos.closeEntry();
    }

    public void closeRecorder(int time) throws IOException {
        changeRecordingState(false, time);
        if (!chunks.isEmpty()) {
            createFile("chunks");
            writeObject(chunks);
            closeFile();
        }
        zos.flush();
        zos.close();
        canRecord = false;
        if (lazyRec) saveReady = true;
    }

    public void writeAction(final boolean[] bo) throws IOException {
        byte action = 0;
        for (int i = 0; i < bo.length; i++) if (bo[i]) action |= (1 << i);
        zos.write(action);
    }

    public void changeRecordingState(boolean state, int time) {
        if (state && !lastRecordingState) {
            chunk = new ReplayerOptions.Interval();
            chunk.from = time;
            lastRecordingState = state;
        } else if (!state && lastRecordingState) {
            chunk.to = time;
            chunks.add(chunk);
            lastRecordingState = state;
        }
    }

    public boolean canRecord() {
        return canRecord;
    }

    public boolean canSave() {
        return saveReady;
    }
}
