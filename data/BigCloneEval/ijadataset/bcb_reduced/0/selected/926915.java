package com.unboundid.buildtools.digest;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This class provides an Ant task that can be used to generate a cryptographic
 * message digest of a specified file.
 */
public class GenerateDigest extends Task {

    private File file;

    private File toFile;

    private String algorithm;

    /**
   * Creates a new instance of this task.
   */
    public GenerateDigest() {
        file = null;
        toFile = null;
        algorithm = null;
    }

    /**
   * Specifies the path of the file for which to generate the digest.
   *
   * @param  file  The path of the file for which to generate the digest.
   */
    public void setFile(final File file) {
        this.file = file;
    }

    /**
   * Specifies the path of the file to which the generated digest should be
   * written.
   *
   * @param  toFile  The path of the file to which the generated digest should
   *                 be written.
   */
    public void setToFile(final File toFile) {
        this.toFile = toFile;
    }

    /**
   * Specifies the digest algorithm that should be used.
   *
   * @param  algorithm  The digest algorithm that should be used.
   */
    public void setAlgorithm(final String algorithm) {
        this.algorithm = algorithm;
    }

    /**
   * Performs the appropriate processing for this Ant task.
   *
   * @throws  BuildException  If the configuration for this task is not
   *                          sufficient, or if a problem occurs during
   *                          processing.
   */
    @Override()
    public void execute() throws BuildException {
        if (file == null) {
            throw new BuildException("No value specified for the file property.");
        }
        if (!(file.exists() && file.isFile())) {
            throw new BuildException("File " + file.getAbsolutePath() + " does not exist or is not a file");
        }
        if (toFile == null) {
            throw new BuildException("No value specified for the toFile property.");
        }
        if (algorithm == null) {
            throw new BuildException("No value specified for the algorithm property.");
        }
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            throw new BuildException("Unable to create a message digest for the " + algorithm + " algorithm:  " + e, e);
        }
        final byte[] buffer = new byte[8192];
        try {
            final FileInputStream inputStream = new FileInputStream(file);
            while (true) {
                final int bytesRead = inputStream.read(buffer);
                if (bytesRead < 0) {
                    break;
                }
                digest.update(buffer, 0, bytesRead);
            }
            inputStream.close();
        } catch (Exception e) {
            throw new BuildException("An error occurred while attempting to " + "generate the " + algorithm + " digest for file " + file.getAbsolutePath() + ":  " + e, e);
        }
        try {
            final PrintWriter digestFileWriter = new PrintWriter(toFile);
            final byte[] digestBytes = digest.digest();
            for (final byte b : digestBytes) {
                switch(b & 0xF0) {
                    case 0x00:
                        digestFileWriter.print('0');
                        break;
                    case 0x10:
                        digestFileWriter.print('1');
                        break;
                    case 0x20:
                        digestFileWriter.print('2');
                        break;
                    case 0x30:
                        digestFileWriter.print('3');
                        break;
                    case 0x40:
                        digestFileWriter.print('4');
                        break;
                    case 0x50:
                        digestFileWriter.print('5');
                        break;
                    case 0x60:
                        digestFileWriter.print('6');
                        break;
                    case 0x70:
                        digestFileWriter.print('7');
                        break;
                    case 0x80:
                        digestFileWriter.print('8');
                        break;
                    case 0x90:
                        digestFileWriter.print('9');
                        break;
                    case 0xA0:
                        digestFileWriter.print('a');
                        break;
                    case 0xB0:
                        digestFileWriter.print('b');
                        break;
                    case 0xC0:
                        digestFileWriter.print('c');
                        break;
                    case 0xD0:
                        digestFileWriter.print('d');
                        break;
                    case 0xE0:
                        digestFileWriter.print('e');
                        break;
                    case 0xF0:
                        digestFileWriter.print('f');
                        break;
                }
                switch(b & 0x0F) {
                    case 0x00:
                        digestFileWriter.print('0');
                        break;
                    case 0x01:
                        digestFileWriter.print('1');
                        break;
                    case 0x02:
                        digestFileWriter.print('2');
                        break;
                    case 0x03:
                        digestFileWriter.print('3');
                        break;
                    case 0x04:
                        digestFileWriter.print('4');
                        break;
                    case 0x05:
                        digestFileWriter.print('5');
                        break;
                    case 0x06:
                        digestFileWriter.print('6');
                        break;
                    case 0x07:
                        digestFileWriter.print('7');
                        break;
                    case 0x08:
                        digestFileWriter.print('8');
                        break;
                    case 0x09:
                        digestFileWriter.print('9');
                        break;
                    case 0x0A:
                        digestFileWriter.print('a');
                        break;
                    case 0x0B:
                        digestFileWriter.print('b');
                        break;
                    case 0x0C:
                        digestFileWriter.print('c');
                        break;
                    case 0x0D:
                        digestFileWriter.print('d');
                        break;
                    case 0x0E:
                        digestFileWriter.print('e');
                        break;
                    case 0x0F:
                        digestFileWriter.print('f');
                        break;
                }
            }
            digestFileWriter.println();
            digestFileWriter.close();
        } catch (Exception e) {
            throw new BuildException("An error occurred while attempting to write " + "the digest to " + toFile.getAbsolutePath() + ":  " + e, e);
        }
    }
}
