package org.basegen.base.commons;

import java.io.IOException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

/**
 * Utility class for file operations
 */
public class FileOperations {

    /**
     * Constant buffer size 1 megabyte
     */
    private static final int BUFFER_SIZE = 1024 * 1024;

    /**
     * Constant short counter
     */
    private static final int SHORT_COUNTER = 12;

    /**
     * Constant big counter
     */
    private static final int BIG_COUNTER = 500000;

    /**
     * Constant three
     */
    private static final int THREE = 3;

    /**
     * Private class to check if the file is a directory
     */
    private static class DirectoryFileFilter implements FileFilter {

        /**
         * Check if the file is directory
         * @param file file
         * @return boolean
         */
        public boolean accept(File file) {
            return file.isDirectory();
        }
    }

    /**
     * Obt�m os subdiret�rios de um diret�rio
     * @param path path
     * @return the sub directories
     */
    public static File[] getSubDirs(File path) {
        return path.listFiles(new DirectoryFileFilter());
    }

    /**
     * Obt�m uma lista de arquivos de um diret�rio passando um FileFilter como filter.
     * @param path path
     * @param filter filter
     * @return list of files
     */
    public static List getList(File path, FileFilter filter) {
        return getFiles(new ArrayList(), path, filter);
    }

    /**
     * Lista arquivos de um diret�rio passado como par�metro atrav�s de um FileFilter.
     * @param colFiles colFiles
     * @param path path
     * @param filter file filter
     * @return list of files
     */
    private static List getFiles(List colFiles, File path, FileFilter filter) {
        File[] files = path.listFiles();
        for (int contador = 0; contador < files.length; contador++) {
            File file = files[contador];
            if (file.isDirectory()) {
                colFiles = getFiles(colFiles, file, filter);
            } else {
                if (filter.accept(file)) {
                    colFiles.add(file);
                }
            }
        }
        return colFiles;
    }

    /**
     * Compress file using zip format
     * @param pathToBeZipped path to be zipped
     * @param pathZippedFile path of zipped file
     * @return file
     * @throws IOException io exception
     */
    public static File zipFile(String pathToBeZipped, String pathZippedFile) throws IOException {
        if (pathZippedFile.indexOf(".zip") < 0) {
            pathZippedFile += ".zip";
        }
        File fileASerZipado = new File(pathToBeZipped);
        byte[] buf = new byte[BUFFER_SIZE];
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(pathZippedFile));
        FileInputStream in = new FileInputStream(pathToBeZipped);
        out.putNextEntry(new ZipEntry(fileASerZipado.getName()));
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.closeEntry();
        in.close();
        out.close();
        return new File(pathZippedFile);
    }

    /**
     * Verifies if directory exist. If false, it is created
     * @param dir directory
     */
    public static void verifyDir(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Format the file
     * @param args args
     * @throws IOException io exception
     */
    public static void format(String[] args) throws IOException {
        String separador = args[0];
        File arquivo = new File(args[1]);
        int numero = Integer.parseInt(args[2]);
        format(separador, numero, arquivo);
    }

    /**
     * Format the line file into table file, using separator and number
     * @param separator separator
     * @param number number
     * @param file file
     * @throws IOException io exception
     */
    public static void format(String separator, int number, File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file), BUFFER_SIZE);
        File fout = new File(StringOperations.replace(file.toString(), ".txt", ".csv"));
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(fout), BUFFER_SIZE));
        String linha;
        String buffer = "";
        int contador = 0;
        int contador2 = 0;
        while ((linha = in.readLine()) != null) {
            buffer += StringOperations.replace(linha, separator, " ") + separator;
            if (contador == number) {
                out.println(buffer);
                buffer = "";
                contador = 0;
            } else {
                contador++;
            }
            contador2++;
            if (contador2 % BIG_COUNTER == 0) {
                System.out.println(contador2);
            }
        }
        if (buffer.length() > 0) {
            out.println(buffer);
        }
        in.close();
        out.close();
    }

    /**
     * Adjust table file
     * @param args args
     * @throws IOException io exception
     */
    public static void adjustTableFile(String[] args) throws IOException {
        String separadorAntigo = args[0];
        String separadorNovo = args[1];
        File arquivo = new File(args[2]);
        int numeroColumns = Integer.parseInt(args[THREE]);
        adjustTableFile(separadorAntigo, separadorNovo, arquivo, numeroColumns);
    }

    /**
     * Format line file into table file, using separator and number of registers per line
     * @param oldSeparator old separator
     * @param newSeparator new separator
     * @param file file
     * @param numberOfColumns number of columns
     * @throws IOException io exception 
     */
    public static void adjustTableFile(String oldSeparator, String newSeparator, File file, int numberOfColumns) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file), BUFFER_SIZE);
        File fout = new File(StringOperations.replace(file.toString(), ".txt", ".csv"));
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(fout), BUFFER_SIZE));
        String linha;
        int contadorLinha = 0;
        ArrayList linhas = new ArrayList();
        while ((linha = in.readLine()) != null) {
            String buffer = "";
            for (int contador = 0; contador < numberOfColumns; contador++) {
                int indiceAspasDuplas = linha.indexOf("\"");
                int indiceSeparadorAntigo = linha.indexOf(oldSeparator);
                if (indiceSeparadorAntigo < indiceAspasDuplas) {
                    buffer += linha.substring(0, linha.indexOf(oldSeparator)) + newSeparator;
                    linha = linha.substring(linha.indexOf(oldSeparator) + 1);
                } else {
                    linha = linha.substring(indiceAspasDuplas + 1);
                    indiceAspasDuplas = linha.indexOf("\"");
                    buffer += linha.substring(0, indiceAspasDuplas) + newSeparator;
                    linha = linha.substring(indiceAspasDuplas + 2);
                }
            }
            contadorLinha++;
            if (linha.indexOf(oldSeparator) != -1) {
                linhas.add(new Integer(contadorLinha));
            }
            out.println(buffer);
        }
        if (linhas.size() > 0) {
            System.out.println(linhas);
        }
        in.close();
        out.close();
    }

    /**
     * Adjust line file
     * @param args args
     * @throws IOException io exception
     */
    public static void adjustLineFile(String[] args) throws IOException {
        String separadorAntigo = args[0];
        File arquivo = new File(args[1]);
        int numeroColumns = Integer.parseInt(args[2]);
        adjustLineFile(separadorAntigo, arquivo, numeroColumns);
    }

    /**
     * Format table file into line file, using separator and number of records per line
     * @param oldSeparator old separator
     * @param file file
     * @param numberOfColumns number of columns
     * @throws IOException io exception
     */
    public static void adjustLineFile(String oldSeparator, File file, int numberOfColumns) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file), BUFFER_SIZE);
        File fout = new File(StringOperations.replace(file.toString(), ".txt", ".csv"));
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(fout), BUFFER_SIZE));
        String linha;
        int contadorLinha = 0;
        ArrayList linhas = new ArrayList();
        while ((linha = in.readLine()) != null) {
            for (int contador = 0; contador < numberOfColumns; contador++) {
                int indiceAspasDuplas = linha.indexOf("\"");
                int indiceSeparadorAntigo = linha.indexOf(oldSeparator);
                if ((indiceSeparadorAntigo != -1)) {
                    if (indiceSeparadorAntigo < indiceAspasDuplas) {
                        out.println(linha.substring(0, linha.indexOf(oldSeparator)));
                        linha = linha.substring(linha.indexOf(oldSeparator) + 1);
                    } else {
                        linha = linha.substring(indiceAspasDuplas + 1);
                        indiceAspasDuplas = linha.indexOf("\"");
                        out.println(linha.substring(0, indiceAspasDuplas));
                        linha = linha.substring(indiceAspasDuplas + 2);
                    }
                } else {
                    if (indiceAspasDuplas != -1) {
                        linha = linha.substring(indiceAspasDuplas + 1);
                        indiceAspasDuplas = linha.indexOf("\"");
                        out.println(linha.substring(0, indiceAspasDuplas));
                    } else {
                        out.println(linha);
                    }
                }
            }
            contadorLinha++;
            if (linha.indexOf(oldSeparator) != -1) {
                linhas.add(new Integer(contadorLinha));
            }
        }
        if (linhas.size() > 0) {
            System.out.println(linhas);
        }
        in.close();
        out.close();
    }

    /**
     * Count
     * @param args args
     * @throws IOException io exception
     */
    public static void count(String[] args) throws IOException {
        File arquivo = new File(args[0]);
        if (args.length == 2) {
            System.out.println(count(args[1], arquivo));
        } else {
            System.out.println(count("\"", arquivo));
        }
    }

    /**
     * Format line file into table file
     * @param str string
     * @param file file
     * @throws IOException io exception
     * @return count
     */
    public static int count(String str, File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file), BUFFER_SIZE);
        File fout = new File(StringOperations.replace(file.toString(), ".txt", ".csv"));
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(fout), BUFFER_SIZE));
        String linha;
        int result = 0;
        while ((linha = in.readLine()) != null) {
            String linhaOriginal = linha;
            int soma = StringOperations.count(linha, str);
            if (soma != SHORT_COUNTER) {
                System.out.println(linhaOriginal);
            }
            result += soma;
        }
        in.close();
        out.close();
        return result;
    }

    /**
     * Create the dir file if it does not exist or remove the files
     * @param file file
     * @throws IOException io exception
     */
    public static void checkDir(File file) throws IOException {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            File[] files = dir.listFiles();
            for (int counter = 0; counter < files.length; counter++) {
                File internalFile = files[counter];
                if (!internalFile.delete()) {
                    throw new IOException("unable to delete internalFile: " + internalFile);
                }
            }
        }
    }

    /**
     * Replace matches of map from file in to file out
     * @param fin file in
     * @param fout file out
     * @param map map
     * @throws IOException io exception
     */
    public static void replace(File fin, File fout, Map<String, String> map) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(fin), BUFFER_SIZE);
        PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(fout), BUFFER_SIZE));
        String s = null;
        while ((s = in.readLine()) != null) {
            out.println(StringOperations.replace(s, map));
        }
        in.close();
        out.close();
    }

    /**
     * Saves is to file
     * @param is input stream
     * @param file file
     * @return dumped file
     * @throws IOException io exception
     */
    public static File dump(InputStream is, File file) throws IOException {
        File parentFile = file.getParentFile();
        if ((parentFile != null) && !parentFile.exists() && !parentFile.mkdirs()) {
            throw new IOException("Unable to create dir: " + parentFile.getAbsolutePath());
        }
        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
        byte[] buffer = new byte[BUFFER_SIZE];
        int byteRead = -1;
        while ((byteRead = is.read(buffer)) != -1) {
            fos.write(buffer, 0, byteRead);
        }
        fos.close();
        is.close();
        return file;
    }

    /**
     * Main method
     * @param args args
     * @throws IOException io exception
     */
    public static void main(String[] args) throws IOException {
        adjustLineFile(args);
        System.out.println("done");
    }
}
