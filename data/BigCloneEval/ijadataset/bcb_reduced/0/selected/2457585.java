package ru.amse.jsynchro.kernel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Random;
import ru.amse.jsynchro.fileSystem.FileSystem;
import ru.amse.jsynchro.kernel.util.FileSystemUtil;

public class RandomFolder {

    private static int maxDepth = 5;

    private static int maxSubElements = 15;

    private static int maxFileSize = 1024;

    private static int fileNameSize = 8;

    private static int codeA = (int) 'A';

    private int elementsNumber;

    private boolean force;

    private static Random random = new Random();

    private FileSystem FS;

    /**
     * @param force, if true existing folder will be deleted
     * @param elementsNumber, maximal creating elements number
     * @param FS, local or remote
     */
    public RandomFolder(boolean forceDelete, int elementsNumber, FileSystem FS) {
        this.force = forceDelete;
        this.elementsNumber = elementsNumber;
        this.FS = FS;
    }

    /**
     * a folder with random elements will be created:
     * {@code File(rootName, name)}
     * @param rootName
     * @param name
     * @return
     */
    public File generate(String rootName, String name) {
        File root = new File(rootName, name);
        if (root.exists()) {
            if (force) {
                FileSystemUtil.deleteDirR(root, FS);
            } else {
                System.out.printf("%s: such folder already exists!", root.getName());
                return null;
            }
        }
        int depth = random.nextInt(maxDepth + 1);
        generateDir(new File(rootName), name, depth);
        return root;
    }

    private boolean generateDir(File root, String name, int depth) {
        File dir = new File(root, name);
        if (dir.mkdir()) {
            if (--elementsNumber <= 0) {
                return true;
            }
            if (!generateSubElements(dir, depth)) {
                return false;
            }
        }
        return true;
    }

    private boolean generateSubElements(File root, int depth) {
        int subElementsNumber = random.nextInt(maxSubElements + 1);
        if (depth > 0) {
            for (int i = 1; i <= subElementsNumber; i++) {
                if (random.nextInt(100) >= 50) {
                    if (!generateFile(root)) {
                        return false;
                    }
                    if (--elementsNumber <= 0) {
                        return true;
                    }
                } else {
                    if (!generateDir(root, generateName(), depth - 1)) {
                        return false;
                    }
                }
            }
        } else {
            for (int i = 1; i <= subElementsNumber; i++) {
                if (!generateFile(root)) {
                    return false;
                }
                if (--elementsNumber <= 0) {
                    return true;
                }
            }
        }
        return true;
    }

    /**
     * @param root
     * @param changeLevel, probability of changing an element (file, folder)
     *  of root
     * @return if success
     */
    public static boolean changeDir(File root, double changeLevel) {
        File[] childs = root.listFiles();
        if (childs.length == 0) {
            for (int i = 0; i < 5; i++) {
                if (!generateFile(root)) {
                    return false;
                }
            }
        }
        for (File f : childs) {
            if (f.isFile() && (random.nextFloat() < changeLevel)) {
                float rnd = random.nextFloat();
                if (rnd <= 0.33) {
                    if (!f.renameTo(new File(root, generateName()))) {
                        return false;
                    }
                } else if (rnd < 0.75) {
                    if (!f.delete()) {
                        return false;
                    }
                } else if (rnd <= 1) {
                    if (!generateFile(root)) {
                        return false;
                    }
                } else {
                }
            } else if (f.isDirectory()) {
                if (!changeDir(f, changeLevel)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean changeFile(File file) {
        RandomAccessFile in;
        try {
            in = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException e) {
            return false;
        }
        int c;
        try {
            while ((c = in.read()) > 0) {
                if (random.nextFloat() < 0.5) {
                    in.write(c);
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean generateFile(File root) {
        String name = generateName();
        File file = new File(root, name);
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            int fileSize = random.nextInt(maxFileSize);
            byte[] buf = new byte[fileSize];
            random.nextBytes(buf);
            out.write(buf);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String generateName() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fileNameSize; i++) {
            char c = (char) (random.nextInt(26) + codeA);
            result.append(c);
        }
        return result.toString();
    }
}
