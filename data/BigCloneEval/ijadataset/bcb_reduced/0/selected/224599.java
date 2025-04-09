package com.mea.lab.ca;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

class G {

    public static final KillProcessThread killThread = new KillProcessThread();

    public static final PG defaultPG = new PG();

    public static final String fileSuffix = ".exe";

    public static final int SAFE_BYTE_SIZE = 10;

    public static final int PG_INIT_SIZE = 10240;
}

public class PGs {

    /**
	 * @param args
	 */
    public static void main(String[] args) {
        final int PG_SIZE = 100;
        int g = 1000;
        List<PG> pgList = new ArrayList<PG>();
        for (File f : PG.PG_DIR.listFiles()) {
            if (!f.getName().endsWith(G.fileSuffix)) continue;
            pgList.add(new PG(f));
            f.delete();
        }
        while (pgList.size() < PG_SIZE) {
            pgList.add(new PG());
        }
        for (PG pg : pgList) {
            pg.generatePG();
        }
        while (g-- > 0) {
            if (pgList.size() == 0) pgList.add(G.defaultPG.clonePG());
            int index = 0;
            while (pgList.size() < PG_SIZE) {
                PG pg = pgList.get(index++ % pgList.size()).clonePG();
                pg.generatePG();
                pgList.add(pg);
            }
            for (int i = pgList.size() - 1; i >= 0; i--) {
                PG pg = pgList.get(i);
                int exitCode = pg.runPG("1");
                if (exitCode != 0) {
                    pg.killSelf();
                    pgList.remove(i);
                }
            }
            for (int i = pgList.size() - 1; i >= 0; i--) {
                PG pg = pgList.get(i);
                PG newPG = pg.evolutionPG();
                newPG.generatePG();
                pg.okPG();
                pgList.set(i, newPG);
            }
        }
        System.exit(0);
    }
}

class PG extends WindowsPG {

    public PG() {
        super();
    }

    public PG(File file) {
        super(file);
    }
}

class WindowsPG extends PGBasic {

    public static final File PG_DIR = new File("d:/pgs");

    public WindowsPG() {
        super();
        setExeHeader();
    }

    public WindowsPG(File file) {
        super(file);
        setExeHeader();
    }

    private void setExeHeader() {
        this.pgValue[0] = 'M';
        this.pgValue[1] = 'Z';
    }

    @Override
    public int runPG(String input) {
        try {
            String processName = "cmd /C " + PG_DIR.getAbsolutePath() + "/" + getPGName();
            Process process = Runtime.getRuntime().exec(processName);
            G.killThread.setProcess(process);
            InputStream inputStream = process.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int size = -1;
            while ((size = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, size);
            }
            out.close();
            String output = new String(out.toByteArray());
            System.out.println(String.format("[%s]:%s", this.pgID, output));
            if (output.trim().endsWith("")) return -1;
            return process.exitValue();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public File getPGDir() {
        return PG_DIR;
    }
}

abstract class PGBasic {

    protected static Integer PG_ID_GENERATOR = 0;

    protected int pgID;

    protected byte[] pgValue;

    public PGBasic() {
        synchronized (PG_ID_GENERATOR) {
            pgID = PG_ID_GENERATOR++;
        }
        pgValue = new byte[G.PG_INIT_SIZE];
        for (int i = 0; i < pgValue.length; i++) {
            pgValue[i] = randomByte();
        }
    }

    public PGBasic(File file) {
        this();
        try {
            InputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[512];
            int size = -1;
            while ((size = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, size);
            }
            inputStream.close();
            out.close();
            this.pgValue = out.toByteArray();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract File getPGDir();

    public abstract int runPG(String input);

    protected String getPGName() {
        return "pg_" + this.pgID + G.fileSuffix;
    }

    protected byte randomByte() {
        int value = (Byte.MIN_VALUE + (int) (Math.random() * (Byte.MAX_VALUE - Byte.MIN_VALUE)));
        return (byte) value;
    }

    public int getPGID() {
        return this.pgID;
    }

    public void okPG() {
        File okFile = new File(getPGDir().getAbsolutePath() + "/" + getPGName());
        okFile.renameTo(new File(okFile.getParentFile().getAbsoluteFile() + "/ok_" + okFile.getName()));
    }

    public void killSelf() {
        File deleteFile = new File(getPGDir().getAbsolutePath() + "/" + getPGName());
        deleteFile.delete();
        System.out.println("Delete:" + deleteFile.getAbsolutePath());
    }

    public void generatePG() {
        File pgFile = new File(getPGDir(), getPGName());
        try {
            pgFile.createNewFile();
            OutputStream out = new FileOutputStream(pgFile);
            for (byte b : this.pgValue) {
                out.write(b);
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PG clonePG() {
        PG pg = new PG();
        pg.pgValue = new byte[this.pgValue.length];
        for (int i = 0; i < this.pgValue.length; i++) {
            pg.pgValue[i] = this.pgValue[i];
        }
        int index = G.SAFE_BYTE_SIZE + (int) (Math.random() * (pg.pgValue.length - G.SAFE_BYTE_SIZE));
        pg.pgValue[index] = this.randomByte();
        return pg;
    }

    public PG evolutionPG() {
        PG pg = new PG();
        pg.pgValue = new byte[this.pgValue.length + 1];
        for (int i = 0; i < this.pgValue.length; i++) {
            pg.pgValue[i] = this.pgValue[i];
        }
        pg.pgValue[this.pgValue.length] = this.randomByte();
        return pg;
    }
}

class KillProcessThread extends Thread {

    private long time;

    private static long SLEEP_TIME = 3000;

    public void run() {
        while (true) {
            if ((System.currentTimeMillis() - time) > SLEEP_TIME) {
                try {
                    Runtime.getRuntime().exec("TASKKILL /F /IM ntvdm.exe /T");
                    Runtime.getRuntime().exec("TASKKILL /F /IM cmd.exe /T");
                    System.out.println("KillProcessThread:");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setProcess(Process process) {
        time = System.currentTimeMillis();
    }
}
