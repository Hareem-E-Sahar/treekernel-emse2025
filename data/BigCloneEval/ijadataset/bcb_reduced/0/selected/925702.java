package org.neuroph.contrib.neat.gen.persistence.impl.serialize;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.neuroph.contrib.neat.gen.FitnessScores;
import org.neuroph.contrib.neat.gen.Generation;
import org.neuroph.contrib.neat.gen.Innovations;
import org.neuroph.contrib.neat.gen.persistence.Persistence;

public class DirectoryObjectSerializationPersistence implements Persistence {

    private static final String FILE_EXTENSION = ".neat";

    public static final String DEFAULT_BASE_DIRECTORY = "output";

    private String baseDirectory = DEFAULT_BASE_DIRECTORY;

    private boolean useCompression = true;

    public DirectoryObjectSerializationPersistence() {
    }

    public DirectoryObjectSerializationPersistence(String string) {
        this.baseDirectory = string;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public void addGeneration(Innovations i, Generation g, FitnessScores fitness) {
        PersistedGeneration gp = new PersistedGeneration(g, i, fitness);
        File outputFile = new File(getDirectory(), g.getGenerationNumber() + FILE_EXTENSION);
        ObjectOutputStream oos = null;
        ZipOutputStream zos = null;
        try {
            if (useCompression) {
                zos = new ZipOutputStream(new FileOutputStream(outputFile));
                zos.putNextEntry(new ZipEntry("PersistedGeneration.neat"));
                oos = new ObjectOutputStream(zos);
                oos.writeObject(gp);
                zos.closeEntry();
            } else {
                oos = new ObjectOutputStream(new FileOutputStream(outputFile));
                oos.writeObject(gp);
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Could not write generation " + g.getGenerationNumber() + " to disk.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write generation " + g.getGenerationNumber() + " to disk.", e);
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public Innovations getInnovations() {
        return load(getHighestGenerationNumber()).getInnovations();
    }

    public Generation loadGeneration() {
        return load(getHighestGenerationNumber()).getGeneration();
    }

    public FitnessScores getFitness() {
        return load(getHighestGenerationNumber()).getFitnessScores();
    }

    private PersistedGeneration load(long generation) {
        File inputFile = new File(getDirectory(), generation + FILE_EXTENSION);
        PersistedGeneration gp = null;
        ObjectInputStream ois = null;
        ZipInputStream zis = null;
        try {
            if (useCompression) {
                zis = new ZipInputStream(new FileInputStream(inputFile));
                ZipEntry entry = zis.getNextEntry();
                if (entry.getName().equals("PersistedGeneration.neat")) {
                    ois = new ObjectInputStream(zis);
                    gp = (PersistedGeneration) ois.readObject();
                } else {
                    throw new IllegalStateException("Zip file contains file content.");
                }
            } else {
                ois = new ObjectInputStream(new FileInputStream(inputFile));
                gp = (PersistedGeneration) ois.readObject();
            }
            return gp;
        } catch (IOException e) {
            throw new IllegalStateException("Could not load from file " + inputFile.getAbsolutePath() + ".", e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not load from file " + inputFile.getAbsolutePath() + ".", e);
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (zis != null) {
                    zis.close();
                }
            } catch (IOException e) {
            }
        }
    }

    private long getHighestGenerationNumber() {
        File[] files = getDirectory().listFiles();
        long max = Long.MIN_VALUE;
        for (File f : files) {
            if (f.isFile()) {
                String name = f.getName();
                if (name.endsWith(FILE_EXTENSION)) {
                    int index = name.indexOf(FILE_EXTENSION);
                    long temp = Long.valueOf(name.substring(0, index));
                    max = (long) Math.max(max, temp);
                }
            }
        }
        return max;
    }

    private File getDirectory() {
        File f = new File(baseDirectory);
        if (f.exists()) {
            if (!f.isDirectory()) {
                throw new IllegalArgumentException("File " + baseDirectory + " exists, but it is not a directory.");
            }
        } else {
            if (!f.mkdir()) {
                throw new IllegalArgumentException("Could not create directory " + baseDirectory + ".");
            }
        }
        return f;
    }
}
