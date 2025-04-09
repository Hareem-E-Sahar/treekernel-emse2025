package org.ala.layers.util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.ala.layers.dao.LayerIntersectDAO;

/**
 *
 * @author Adam
 */
public class BatchConsumer {

    static BatchConsumerThread thread = null;

    static LinkedBlockingQueue<String> waitingBatchDirs;

    static LayerIntersectDAO layerIntersectDao;

    public static void start(LayerIntersectDAO layerIntersectDao) {
        if (thread == null) {
            layerIntersectDao = layerIntersectDao;
            waitingBatchDirs = new LinkedBlockingQueue<String>();
            thread = new BatchConsumerThread(waitingBatchDirs, layerIntersectDao);
            thread.start();
        }
    }

    public static void addBatch(String batchDir) throws InterruptedException {
        waitingBatchDirs.put(batchDir);
    }

    static void end() {
        thread.interrupt();
    }
}

class BatchConsumerThread extends Thread {

    LinkedBlockingQueue<String> waitingBatchDirs;

    LayerIntersectDAO layerIntersectDao;

    public BatchConsumerThread(LinkedBlockingQueue<String> waitingBatchDirs, LayerIntersectDAO layerIntersectDao) {
        this.waitingBatchDirs = waitingBatchDirs;
        this.layerIntersectDao = layerIntersectDao;
    }

    @Override
    public void run() {
        while (true) {
            String currentBatch = null;
            try {
                currentBatch = waitingBatchDirs.take();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm:ss:SSS");
                writeToFile(currentBatch + "status.txt", "started at " + sdf.format(new Date()), true);
                writeToFile(currentBatch + "started.txt", sdf.format(new Date()), true);
                String fids = readFile(currentBatch + "fids.txt");
                String points = readFile(currentBatch + "points.txt");
                ArrayList<String> sample = layerIntersectDao.sampling(fids, points);
                FileOutputStream fos = new FileOutputStream(currentBatch + "sample.zip");
                ZipOutputStream zip = new ZipOutputStream(fos);
                zip.putNextEntry(new ZipEntry("sample.csv"));
                IntersectUtil.writeSampleToStream(fids.split(","), points.split(","), sample, zip);
                zip.close();
                fos.close();
                writeToFile(currentBatch + "status.txt", "finished at " + sdf.format(new Date()), true);
                writeToFile(currentBatch + "finished.txt", sdf.format(new Date()), true);
                currentBatch = null;
            } catch (Exception e) {
                if (currentBatch != null) {
                    try {
                        writeToFile(currentBatch + "status.txt", "error " + e.getMessage(), true);
                        writeToFile(currentBatch + "error.txt", e.getMessage(), true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                e.printStackTrace();
            }
        }
    }

    private static void writeToFile(String filename, String string, boolean append) throws IOException {
        FileWriter fw = new FileWriter(filename, append);
        fw.write(string);
        fw.close();
    }

    private static String readFile(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(line);
        }
        return sb.toString();
    }
}
