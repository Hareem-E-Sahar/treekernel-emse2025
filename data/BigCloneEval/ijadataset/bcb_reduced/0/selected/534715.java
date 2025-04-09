package linkbuild;

import java.io.*;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * La class per la scrittura su disco dei dati permanenti
 * Ã¨ statica, Essa infatti nn necessita di instanze.
 * Seguono una serie di metodi atti alla lettura e scrittura di dati su file.
 */
public class FileManager {

    /**
     * Create a directory by path string
     */
    public static boolean createDirectory(String dir) {
        try {
            File directory = new File(dir);
            if (!directory.exists()) {
                directory.mkdir();
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Delete a file specificated
     */
    public static boolean deleteFile(String file_name) {
        File f = new File(file_name);
        return f.delete();
    }

    /**
     * Delete a specific directory and its sub-directories
     */
    public static boolean deleteDirectory(String dir) {
        File d = new File(dir);
        if ((!d.isDirectory()) || (!d.exists())) return false;
        if (!d.delete()) {
            String[] lista_file = d.list();
            for (int i = 0; i < lista_file.length; i++) {
                File f = new File(d.getAbsolutePath() + File.separator + lista_file[i]);
                if (f.isDirectory()) {
                    deleteDirectory(f.getAbsolutePath());
                } else {
                    deleteFile(f.getAbsolutePath());
                }
            }
            return d.delete();
        }
        return false;
    }

    /**
     * Create of new empty file
     */
    public static boolean createFile(String file_name) {
        try {
            File new_file = new File(file_name);
            if (!new_file.exists()) {
                new_file.createNewFile();
            }
            return new_file.exists();
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Return the number of objects inside a file
     */
    public static int countObject(String file_path) {
        int count = 0;
        ObjectInputStream file = null;
        try {
            file = new ObjectInputStream(new FileInputStream(file_path));
            try {
                while (true) {
                    file.readObject();
                    count++;
                }
            } catch (EOFException e) {
                file.close();
            }
            return count;
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    /**
     * Return a list of Objects cointained in file and return an array
     */
    public static Object[] objectList(String file_path) {
        try {
            ObjectInput in_stream = new ObjectInputStream(new FileInputStream(file_path));
            Object[] array = new Object[countObject(file_path)];
            for (int i = 0; i < array.length; i++) {
                array[i] = in_stream.readObject();
            }
            return array;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    /**
     * Append a new object in at the end of file
     */
    public static boolean appendObject(String file_path, Object obj) {
        ObjectInputStream in_stream;
        ObjectOutputStream out_stream;
        int n = countObject(file_path);
        Object[] objects = new Object[n];
        try {
            in_stream = new ObjectInputStream(new FileInputStream(file_path));
            for (int i = 0; i < n; i++) {
                objects[i] = in_stream.readObject();
            }
            out_stream = new ObjectOutputStream(new FileOutputStream(file_path));
            for (int i = 0; i < n; i++) {
                out_stream.writeObject(objects[i]);
            }
            out_stream.writeObject(obj);
            in_stream.close();
            out_stream.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Write object in File
     */
    public static boolean writeObject(String file_path, Object obj) {
        File file = new File(file_path);
        if (file.exists() && fileIsEmpty(file_path) == 0) {
            appendObject(file_path, obj);
        } else {
            try {
                ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file_path));
                stream.writeObject(obj);
                stream.close();
                return true;
            } catch (Exception e) {
                System.out.println(e);
                return false;
            }
        }
        return true;
    }

    /**
     * write object list
     */
    public static boolean writeObject(String file_path, Object[] obj) {
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file_path));
            for (int i = 0; i < obj.length; i++) stream.writeObject(obj[i]);
            stream.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Copy file by string path to string path
     */
    public static boolean copyFile(String file_in, String file_out) {
        try {
            File f_in = new File(file_in);
            File f_out = new File(file_out);
            InputStream in = new FileInputStream(f_in);
            OutputStream out = new FileOutputStream(f_out);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            in.close();
            out.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Copy directory and sub-diretory to destinanion path
     */
    public static boolean copyDirectory(String dir_in, String dir_out) {
        File f_in = new File(dir_in);
        File f_out = new File(dir_out);
        if ((!f_out.isDirectory() && f_out.exists()) || !f_in.exists()) return false;
        if (!f_out.isDirectory()) f_out.mkdir();
        String[] lista_file = f_in.list();
        for (int i = 0; i < lista_file.length; i++) {
            File f = new File(f_in.getAbsolutePath() + File.separator + lista_file[i]);
            if (f.isDirectory()) {
                copyDirectory(f.getAbsolutePath(), f_out.getAbsolutePath() + File.separator + f.getName());
            } else {
                copyFile(f_in.getAbsolutePath() + File.separator + f.getName(), f_out.getAbsolutePath() + File.separator + f.getName());
            }
        }
        return true;
    }

    public static boolean moveFile(String file_in, String file_out) {
        if (copyFile(file_in, file_out)) return deleteFile(file_in);
        return false;
    }

    public static boolean moveDirectory(String dir_in, String dir_out) {
        if (copyDirectory(dir_in, dir_out)) return deleteDirectory(dir_in);
        return false;
    }

    public static boolean renameFile(String old_name, String new_name) {
        return moveFile(old_name, new_name);
    }

    public static boolean renameDirectory(String old_name, String new_name) {
        return moveDirectory(old_name, new_name);
    }

    public static boolean rename(String old_name, String new_name) {
        File f = new File(old_name);
        if (f.exists() && f.isDirectory()) {
            return moveDirectory(old_name, new_name);
        } else if (f.exists() && !f.isDirectory()) {
            return moveFile(old_name, new_name);
        }
        return false;
    }

    /**
     * write file
     */
    public static boolean writeFile(String file_path, String context) {
        try {
            RandomAccessFile fileIn = new RandomAccessFile(file_path, "rwd");
            fileIn.seek(fileIn.length());
            fileIn.writeBytes(context + "\n");
            fileIn.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * write file 
     */
    public static boolean writeFile(String file_path, String[] context) {
        try {
            FileWriter fw = new FileWriter(file_path);
            for (int i = 0; i < context.length; i++) fw.write(context[i] + "\n");
            fw.flush();
            fw.close();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * read file
     */
    public static String[] readFile(String file_path) {
        long n = countFileLine(file_path);
        if (n < 0) return null;
        String s[] = new String[(int) n];
        int i = 0;
        try {
            RandomAccessFile fileIn = new RandomAccessFile(file_path, "r");
            String str;
            while ((str = fileIn.readLine()) != null) {
                s[i] = str;
                i++;
            }
            fileIn.close();
            return s;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    /**
     * count file line
     */
    public static long countFileLine(String file_path) {
        long n = 0;
        try {
            RandomAccessFile fileIn = new RandomAccessFile(file_path, "r");
            while (fileIn.readLine() != null) {
                n++;
            }
            fileIn.close();
            return n;
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    /**
     * file line compare
     */
    public static boolean searchLine(String file_path, String line) {
        try {
            RandomAccessFile fileIn = new RandomAccessFile(file_path, "r");
            String s;
            while ((s = fileIn.readLine()) != null) {
                if (s.equals(line)) return true;
            }
            fileIn.close();
            return false;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    public static int fileIsEmpty(String file_path) {
        try {
            FileInputStream stream_in = new FileInputStream(file_path);
            while (stream_in.read() == -1) return 1;
            return 0;
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    /**
     * List files and directories inside a directory; flags can be:
     * 0 = only list files
     * 1 = only list directories
     * other values = list everything
     */
    public static File[] listDirectory(String _dir, int flags) {
        File dir = new File(_dir);
        File[] lista;
        switch(flags) {
            case 0:
                lista = dir.listFiles(new FilterForFile());
            case 1:
                lista = dir.listFiles(new FilterForDirectory());
            default:
                lista = dir.listFiles();
        }
        return lista;
    }

    /**
     * List both files and directories inside a directory
     */
    public static File[] listDirectory(String _dir) {
        return listDirectory(_dir, 2);
    }

    /**
     *  File Select in a graphical chooser
     * @return
     */
    public static File selectFile(String ext) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FilterForChooser(ext));
        int risp = fc.showOpenDialog((JFrame) null);
        if (risp == JFileChooser.APPROVE_OPTION) return fc.getSelectedFile(); else return null;
    }
}

class FilterForChooser extends javax.swing.filechooser.FileFilter {

    private String ext;

    public FilterForChooser(String ext) {
        this.ext = ext;
    }

    public boolean accept(File f) {
        String str = f.getName().toLowerCase();
        return str.endsWith(ext) || f.isDirectory();
    }

    public String getDescription() {
        return "Java Application" + ext;
    }
}

/**
 * 
 * 
 * @author giuseppe
 * 
 * 
 * Gets directories out of files
 */
class FilterForDirectory implements FileFilter {

    /**
 * Gets directories out of files
 * @param pathname
 * @return
 */
    public boolean accept(File pathname) {
        return (pathname.isDirectory());
    }
}

/**
 * 
 * @author giuseppe
 * 
 * Gets files out of directories
 */
class FilterForFile implements FileFilter {

    public boolean accept(File pathname) {
        return (!pathname.isDirectory());
    }
}
