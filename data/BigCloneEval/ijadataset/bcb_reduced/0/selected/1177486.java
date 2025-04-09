package editor.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import editor.source.SourceException;

public class FileUtil implements IConstants {

    public static Object readObject(String fileName) {
        Object object = null;
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (file.length() != 0) {
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(new FileInputStream(fileName));
                object = in.readObject();
            } catch (Exception e) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                file.delete();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }

    public static void writeObject(String fileName, Object object) {
        ObjectOutputStream out;
        try {
            out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(object);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Properties readProperties(String fileName) {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static String getFileContentAsString(String absolutePath) throws SourceException {
        StringBuffer buffer = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(absolutePath));
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line + NEW_LINE);
                line = reader.readLine();
            }
        } catch (Exception e) {
            throw new SourceException(e);
        }
        return buffer.toString();
    }

    public static void save(String fileName, String content) throws SourceException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(fileName);
            writer.write(content);
        } catch (IOException e) {
            throw new SourceException(e);
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copyFile(String from, String to) throws SourceException {
        BufferedReader reader = null;
        FileWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(from));
            writer = new FileWriter(to);
            String line = reader.readLine();
            while (line != null) {
                writer.write(line + NEW_LINE);
                line = reader.readLine();
            }
        } catch (Exception e) {
            throw new SourceException(e);
        } finally {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copy(String from, String to) throws SourceException {
        File fromFile = new File(from);
        if (fromFile.isDirectory()) {
            File toDir = new File(to);
            if (!toDir.exists()) toDir.mkdir();
            for (File myFile : fromFile.listFiles()) {
                if (myFile.isDirectory()) copy(myFile.getAbsolutePath(), to + File.separator + myFile.getName()); else copyFile(myFile.getAbsolutePath(), to + File.separator + myFile.getName());
            }
        } else {
            copyFile(from, to);
        }
    }

    public static void deleteDirectory(File myFile) {
        if (myFile.isDirectory()) {
            if (myFile.listFiles().length == 0) {
                myFile.delete();
            } else {
                for (File file : myFile.listFiles()) {
                    if (file.isDirectory()) deleteDirectory(file); else file.delete();
                }
            }
        } else {
            myFile.delete();
        }
    }

    public static void delete(String location) {
        File file = new File(location);
        file.delete();
    }
}
