package ioutil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class UtilIo {

    public static void uploadFile(String hostAddr, int port, String fileName) throws FileNotFoundException, IOException {
        byte[] buf = new byte[1000];
        int len = -1;
        Socket sock = new Socket(hostAddr, port);
        FileInputStream in = new FileInputStream(fileName);
        OutputStream out = sock.getOutputStream();
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
            out.flush();
        }
        out.close();
        sock.close();
        in.close();
    }

    public void createZipFile(final String inputFileName, final String zipFileName) {
        try {
            FileInputStream inStream = new FileInputStream(inputFileName);
            ZipOutputStream outStream = new ZipOutputStream(new FileOutputStream(zipFileName));
            outStream.putNextEntry(new ZipEntry(inputFileName));
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, bytesRead);
            }
            outStream.closeEntry();
            outStream.close();
            inStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String[] deleteFileDaDirectory2(final String Dir) {
        final File dir = new File(Dir);
        String[] files = null;
        if (dir != null && dir.isDirectory()) {
            files = dir.list();
            for (final String file : files) {
                final File f = new File(dir, file);
                f.delete();
            }
        }
        return files;
    }

    public static String[] deleteFileDaDirectory(final String Dir, final String treCharIniziali) {
        final File dir = new File(Dir);
        final String[] files = dir.list();
        for (final String file : files) {
            final File f = new File(dir, file);
            if (f.isDirectory() == false && f.getName().substring(0, 3).equals(treCharIniziali)) {
                f.delete();
            }
        }
        return files;
    }

    public static void scriviFileSuPiuRighe(final File file, final ArrayList<String> righe) {
        try {
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final String type : righe) {
                out.write(type);
                out.newLine();
            }
            out.close();
        } catch (final IOException e) {
        }
    }

    public boolean check(final String estensione, final File file) {
        return file.exists() && file.isFile() ? checkEstensione(estensione, file) && checkLunghezzaNome(estensione, file) && checkAssenzaParentesi(estensione, file) : true;
    }

    public boolean checkEstensione(final String estensione, final File file) {
        if (file.getName().endsWith(estensione)) {
            return true;
        }
        return false;
    }

    public boolean checkLunghezzaNome(final String estensione, final File file) {
        boolean ok = true;
        final String nomeFile = file.getName();
        if (nomeFile.length() < estensione.length()) {
            ok = false;
        }
        return ok;
    }

    public boolean checkAssenzaParentesi(final String estensione, final File file) {
        boolean ok = true;
        if (file.getName().contains("(") || file.getName().contains(")")) {
            ok = false;
        }
        return ok;
    }

    protected static boolean rename(final File mp3, final String nome_dopo) throws IOException {
        final File file2 = new File(nome_dopo);
        final boolean success = mp3.renameTo(file2);
        return success;
    }

    public boolean moveFile(final File origine, final File destinazione) {
        return origine.renameTo(destinazione);
    }

    public static String slash() {
        String slash = "";
        final String os = System.getProperty("os.name");
        if (os.startsWith("Win")) {
            slash = "\\";
        } else {
            slash = "/";
        }
        return slash;
    }
}
