package org.monet.deployservice.utils;

import java.io.*;
import java.util.ArrayList;
import org.apache.log4j.Logger;

public class Files {

    private Logger logger;

    public Files() {
        logger = Logger.getLogger(this.getClass());
    }

    public void copy(String src, String dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public void remove(String fileName) {
        String command = "";
        String[] commands;
        Process process;
        command = " sudo rm -f \"" + fileName + "\"";
        commands = new String[] { "sh", "-c", command };
        logger.info(command);
        try {
            process = Runtime.getRuntime().exec(commands, null, new File(fileName).getParentFile());
            process.waitFor();
        } catch (Exception e) {
        }
    }

    public void removeDir(String dir) {
        logger.info("Removing dir: " + dir);
        File directory = new File(dir);
        File[] files = directory.listFiles();
        for (int x = 0; x < files.length; x++) {
            if (files[x].isDirectory()) {
                this.removeDir(dir + "/" + files[x].getName());
            }
            File file = new File(dir + "/" + files[x].getName());
            file.delete();
        }
        directory.delete();
    }

    public void makeDir(String dir) {
        logger.info("Making dir: " + dir);
        File directory = new File(dir);
        if (!directory.exists()) directory.mkdir();
    }

    public Boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public String[] directoryList(String dir) {
        File directory = new File(dir);
        File[] files = directory.listFiles();
        ArrayList<String> fileList = new ArrayList<String>();
        for (int x = 0; x < files.length; x++) {
            if (files[x].isDirectory()) {
                String[] list = this.directoryList(files[x].getAbsolutePath());
                for (int y = 0; y < list.length; y++) {
                    fileList.add(list[y]);
                }
            } else {
                fileList.add(files[x].getAbsoluteFile().toString());
            }
        }
        String[] result = new String[fileList.size()];
        for (int x = 0; x < result.length; x++) {
            result[x] = fileList.get(x);
        }
        return result;
    }

    public String baseName(String fileAbs) {
        File file = new File(fileAbs);
        return file.getName();
    }

    public void renameFile(String fileFrom, String fileTo) {
        File toBeRenamed = new File(fileFrom);
        File newFile = new File(fileTo);
        toBeRenamed.renameTo(newFile);
    }

    public void chown(String dir, String user, String group) throws IOException, InterruptedException {
        String command = "";
        String[] commands;
        Process process;
        command = "chown " + user + "." + group + " " + dir;
        commands = new String[] { "sh", "-c", command };
        logger.info(command);
        process = Runtime.getRuntime().exec(commands, null, new File(dir));
        process.waitFor();
        command = "chown " + user + "." + group + " * -R";
        commands = new String[] { "sh", "-c", command };
        logger.info(command);
        process = Runtime.getRuntime().exec(commands, null, new File(dir));
        process.waitFor();
    }

    public void ln(String fileSource, String fileLink) throws IOException, InterruptedException {
        String command = "";
        String[] commands;
        Process process;
        command = " sudo ln -s \"" + fileSource + "\" \"" + fileLink + "\"";
        commands = new String[] { "sh", "-c", command };
        logger.info(command);
        process = Runtime.getRuntime().exec(commands, null, new File(fileSource).getParentFile());
        process.waitFor();
    }

    public void chmod(String fileName, String parameters) throws IOException, InterruptedException {
        String command = "";
        String[] commands;
        Process process;
        command = " sudo chmod " + parameters + " \"" + fileName + "\"";
        commands = new String[] { "sh", "-c", command };
        logger.info(command);
        process = Runtime.getRuntime().exec(commands, null, new File(fileName).getParentFile());
        process.waitFor();
    }

    public void replaceTextInFile(String fileName, String fromText, String toText) {
        try {
            File file = new File(fileName);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "", oldtext = "";
            while ((line = reader.readLine()) != null) {
                oldtext += line + "\n";
            }
            reader.close();
            String newtext = oldtext.replaceAll(fromText, toText);
            FileWriter writer = new FileWriter(fileName);
            writer.write(newtext);
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String loadTextFile(String fileName) {
        File file = new File(fileName);
        StringBuffer contents = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            while ((text = reader.readLine()) != null) {
                contents.append(text).append(System.getProperty("line.separator"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contents.toString();
    }

    /**
	 * This function will copy files or directories from one location to another.
	 * note that the source and the destination must be mutually exclusive. This 
	 * function can not be used to copy a directory to a sub directory of itself.
	 * The function will also have problems if the destination files already exist.
	 * @param src -- A File object that represents the source for the copy
	 * @param dest -- A File object that represnts the destination for the copy.
	 * @throws IOException if unable to copy.
	 */
    public void copyFiles(File src, File dest) throws IOException {
        if (!src.exists()) {
            throw new IOException("copyFiles: Can not find source: " + src.getAbsolutePath() + ".");
        } else if (!src.canRead()) {
            throw new IOException("copyFiles: No right to source: " + src.getAbsolutePath() + ".");
        }
        if (src.isDirectory()) {
            if (!dest.exists()) {
                if (!dest.mkdirs()) {
                    throw new IOException("copyFiles: Could not create direcotry: " + dest.getAbsolutePath() + ".");
                }
            }
            String list[] = src.list();
            for (int i = 0; i < list.length; i++) {
                File dest1 = new File(dest, list[i]);
                File src1 = new File(src, list[i]);
                copyFiles(src1, dest1);
            }
        } else {
            FileInputStream fin = null;
            FileOutputStream fout = null;
            byte[] buffer = new byte[4096];
            int bytesRead;
            try {
                fin = new FileInputStream(src);
                fout = new FileOutputStream(dest);
                while ((bytesRead = fin.read(buffer)) >= 0) {
                    fout.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                IOException wrapper = new IOException("copyFiles: Unable to copy file: " + src.getAbsolutePath() + "to" + dest.getAbsolutePath() + ".");
                wrapper.initCause(e);
                wrapper.setStackTrace(e.getStackTrace());
                throw wrapper;
            } finally {
                if (fin != null) {
                    fin.close();
                }
                if (fout != null) {
                    fout.close();
                }
            }
        }
    }
}
